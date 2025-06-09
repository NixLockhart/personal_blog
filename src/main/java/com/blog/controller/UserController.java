package com.blog.controller;

import com.blog.model.User;
import com.blog.service.UserService;
import com.blog.util.LogUtil;
import com.blog.util.PasswordUtil;
import com.blog.util.AvatarUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

@WebServlet(urlPatterns = {"/profile", "/profile/update", "/profile/edit", "/profile/password"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,  // 1 MB
        maxFileSize = 1024 * 1024 * 2,    // 2 MB
        maxRequestSize = 1024 * 1024 * 10  // 10 MB
)
public class UserController extends HttpServlet {
    private UserService userService = new UserService();
    private final LogUtil logUtil = LogUtil.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Object userObj = session.getAttribute("user");

        if (userObj == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = null;
        try {
            if (userObj instanceof Map) {
                Map<String, Object> userMap = (Map<String, Object>) userObj;
                Integer userId = (Integer) userMap.get("id");
                user = userService.getUserById(userId);
            } else if (userObj instanceof User) {
                user = (User) userObj;
                // 刷新用户信息
                user = userService.getUserById(user.getId());
            }

            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            // 将用户信息设置为request属性，以便在JSP页面中显示
            request.setAttribute("user", user);

            // 处理URL参数中的消息
            String message = request.getParameter("message");
            String messageType = request.getParameter("messageType");
            if (message != null && !message.isEmpty()) {
                request.setAttribute("message", message);
                request.setAttribute("messageType", messageType != null ? messageType : "info");
            }

            // 根据请求路径跳转到不同的页面
            String requestPath = request.getServletPath();
            if ("/profile/edit".equals(requestPath)) {
                request.getRequestDispatcher("/WEB-INF/views/profile/edit.jsp").forward(request, response);
                logUtil.log("PROFILE_ACCESS", user.getUsername(), "用户访问了修改资料页面");
            } else if ("/profile/password".equals(requestPath)) {
                request.getRequestDispatcher("/WEB-INF/views/profile/password.jsp").forward(request, response);
                logUtil.log("PROFILE_ACCESS", user.getUsername(), "用户访问了修改密码页面");
            } else {
                request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
                logUtil.log("PROFILE_ACCESS", user.getUsername(), "用户访问了个人中心页面");
            }

        } catch (Exception e) {
            request.setAttribute("message", "获取用户信息失败：" + e.getMessage());
            request.setAttribute("messageType", "error");
            request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
            logUtil.logError("PROFILE_ERROR", "获取用户信息失败: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Object userObj = session.getAttribute("user");
        User currentUser = null;

        try {
            if (userObj instanceof Map) {
                Map<String, Object> userMap = (Map<String, Object>) userObj;
                Integer userId = (Integer) userMap.get("id");
                currentUser = userService.getUserById(userId);
            } else if (userObj instanceof User) {
                currentUser = (User) userObj;
                // 刷新用户信息
                currentUser = userService.getUserById(currentUser.getId());
            }

            if (currentUser == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            // 检查请求是否为多部分请求
            boolean isMultipart = request.getContentType() != null &&
                    request.getContentType().toLowerCase().startsWith("multipart/form-data");

            String username = null;
            String email = null;
            String password = null;
            String birthdayStr = null;
            String avatarPath = currentUser.getAvatarUrl(); // 保留现有头像路径

            if (isMultipart) {
                // 处理multipart/form-data表单
                username = getValue(request.getPart("username"));
                email = currentUser.getEmail(); // 保留现有邮箱
                password = null; // 多部分表单不处理密码更新
                birthdayStr = getValue(request.getPart("birthday"));

                // 处理头像上传
                Part avatarPart = request.getPart("avatar");
                if (avatarPart != null && avatarPart.getSize() > 0) {
                    // 使用AvatarUtil保存头像
                    String newAvatarPath = AvatarUtil.saveAvatar(currentUser.getId(), avatarPart);
                    if (newAvatarPath != null) {
                        // 如果保存成功，更新头像路径
                        // 只保存文件名部分
                        String fileName = currentUser.getId() + AvatarUtil.getExtension(avatarPart);
                        avatarPath = fileName;

                        // 更新session中的头像更新时间戳
                        long currentTime = System.currentTimeMillis();
                        session.setAttribute("lastAvatarUpdate", currentTime);

                        // 更新用户对象中的头像路径
                        currentUser.setAvatarUrl(fileName);

                        // 更新数据库中的头像路径
                        userService.updateUser(currentUser);

                        logUtil.log("PROFILE_UPDATE", currentUser.getUsername(), "用户上传了新头像: " + fileName);
                    } else {
                        logUtil.logError("PROFILE_UPDATE_ERROR", "头像上传失败");
                    }
                }
            } else {
                // 处理常规表单
                username = request.getParameter("username");
                email = request.getParameter("email");
                password = request.getParameter("password");
                birthdayStr = request.getParameter("birthday");
            }

            // 记录要更新的字段
            StringBuilder updateFields = new StringBuilder();

            // 检查用户名是否更改（添加null检查）
            if (username != null && currentUser.getUsername() != null) {
                if (!username.equals(currentUser.getUsername())) {
                    updateFields.append("用户名更改为 ").append(username).append("; ");
                }
            } else if (username != null && currentUser.getUsername() == null) {
                updateFields.append("用户名设置为 ").append(username).append("; ");
            } else if (username == null && currentUser.getUsername() != null) {
                updateFields.append("用户名被清除; ");
            }

            // 检查邮箱是否更改（添加null检查）
            if (email != null && currentUser.getEmail() != null) {
                if (!email.equals(currentUser.getEmail())) {
                    updateFields.append("邮箱更改为 ").append(email).append("; ");
                }
            } else if (email != null && currentUser.getEmail() == null) {
                updateFields.append("邮箱设置为 ").append(email).append("; ");
            } else if (email == null && currentUser.getEmail() != null) {
                updateFields.append("邮箱被清除; ");
            }

            // 检查是否更新密码
            if (password != null && !password.trim().isEmpty()) {
                updateFields.append("密码已更新; ");
            }

            // 检查生日是否更改
            boolean birthdayChanged = false;
            if (birthdayStr != null && !birthdayStr.trim().isEmpty()) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    Date birthday = sdf.parse(birthdayStr);
                    if (currentUser.getBirthday() == null || !birthday.equals(currentUser.getBirthday())) {
                        updateFields.append("生日更改为 ").append(birthdayStr).append("; ");
                        birthdayChanged = true;
                    }
                } catch (Exception e) {
                    // 日期解析错误
                    logUtil.logError("PROFILE_UPDATE_ERROR", "生日日期格式解析错误: " + birthdayStr);
                }
            } else if (currentUser.getBirthday() != null) {
                updateFields.append("生日已清除; ");
                birthdayChanged = true;
            }

            // 检查头像是否更改
            if (!avatarPath.equals(currentUser.getAvatarUrl())) {
                updateFields.append("头像已更新; ");
            }

            // 设置更新的用户信息
            User updatedUser = new User();
            updatedUser.setId(currentUser.getId());
            updatedUser.setUsername(username != null ? username : currentUser.getUsername());
            updatedUser.setEmail(email != null ? email : currentUser.getEmail());
            updatedUser.setAvatarUrl(avatarPath);

            // 处理生日字段
            if (birthdayStr != null && !birthdayStr.trim().isEmpty()) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    Date birthday = sdf.parse(birthdayStr);
                    updatedUser.setBirthday(birthday);
                } catch (Exception e) {
                    // 如果日期解析失败，保持原有生日不变
                    updatedUser.setBirthday(currentUser.getBirthday());
                }
            } else {
                updatedUser.setBirthday(currentUser.getBirthday());
            }

            // 如果提供了新密码，则更新密码
            if (password != null && !password.trim().isEmpty()) {
                String salt = PasswordUtil.generateSalt();
                String hashedPassword = PasswordUtil.hashPassword(password, salt);
                updatedUser.setPassword(hashedPassword);
                updatedUser.setSalt(salt);
            } else {
                updatedUser.setPassword(currentUser.getPassword());
                updatedUser.setSalt(currentUser.getSalt());
            }

            // 更新用户信息
            userService.updateUser(updatedUser);

            // 更新session中的用户信息
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", updatedUser.getId());
            userMap.put("username", updatedUser.getUsername());
            userMap.put("email", updatedUser.getEmail());

            // 使用AvatarUtil处理头像URL
            String avatarUrl = AvatarUtil.processAvatarUrl(updatedUser.getAvatarUrl());
            userMap.put("avatarUrl", avatarUrl);

            // 判断是否为管理员（ID小于5的用户为管理员）
            userMap.put("isAdmin", updatedUser.getId() < 5);
            session.setAttribute("user", userMap);

            // 记录日志
            if (updateFields.length() > 0) {
                logUtil.log("PROFILE_UPDATE", updatedUser.getUsername(), "用户更新了个人信息: " + updateFields.toString());
            } else {
                logUtil.log("PROFILE_UPDATE", updatedUser.getUsername(), "用户提交了个人信息表单，但未进行任何更改");
            }

            // 重定向到个人资料页面并携带成功消息
            response.sendRedirect(request.getContextPath() + "/profile?message=个人信息更新成功！&messageType=success");
        } catch (Exception e) {
            request.setAttribute("message", "更新失败：" + e.getMessage());
            request.setAttribute("messageType", "error");
            request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
            logUtil.logError("PROFILE_UPDATE_ERROR", "更新用户信息失败: " + e.getMessage());
        }
    }

    // 从Part获取字符串值
    private String getValue(Part part) throws IOException {
        if (part == null || part.getSize() == 0) {
            return null;
        }

        java.io.InputStream is = part.getInputStream();
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return new String(baos.toByteArray(), "UTF-8");
    }
}