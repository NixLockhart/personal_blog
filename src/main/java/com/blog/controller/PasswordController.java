package com.blog.controller;

import com.blog.model.User;
import com.blog.service.UserService;
import com.blog.service.VerificationCodeService;
import com.blog.util.LogUtil;
import com.blog.util.PasswordUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/profile/password/update", "/profile/password/send-code", "/forgot-password"})
public class PasswordController extends HttpServlet {
    private final UserService userService = new UserService();
    private final VerificationCodeService verificationCodeService = VerificationCodeService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LogUtil logUtil = LogUtil.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/forgot-password".equals(path)) {
            // 处理忘记密码页面
            request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
            logUtil.log("PASSWORD_RECOVERY", "匿名用户", "访问了忘记密码页面");
            return;
        }

        // 其他路径需要登录验证
        HttpSession session = request.getSession();
        if (session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();

        // 发送验证码请求
        if ("/profile/password/send-code".equals(path)) {
            handleSendCode(request, response);
            return;
        }

        // 修改密码请求
        if ("/profile/password/update".equals(path)) {
            handlePasswordUpdate(request, response);
            return;
        }

        // 忘记密码请求
        if ("/forgot-password".equals(path)) {
            handleForgotPassword(request, response);
            return;
        }
    }

    /**
     * 处理发送验证码请求
     */
    private void handleSendCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> result = new HashMap<>();

        String email = request.getParameter("email");

        if (email == null || email.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "邮箱不能为空");
            objectMapper.writeValue(response.getWriter(), result);
            logUtil.logError("VERIFICATION_CODE_ERROR", "邮箱为空，无法发送验证码");
            return;
        }

        // 检查请求来源是否为忘记密码页面
        String referer = request.getHeader("Referer");
        boolean isForgotPasswordRequest = referer != null && referer.contains("forgot-password");

        // 如果是忘记密码请求，先检查邮箱是否已注册
        if (isForgotPasswordRequest) {
            try {
                User user = userService.getUserByEmail(email);
                if (user == null) {
                    result.put("success", false);
                    result.put("message", "该邮箱未注册");
                    objectMapper.writeValue(response.getWriter(), result);
                    logUtil.logError("VERIFICATION_CODE_ERROR", "邮箱 " + email + " 未注册，无法发送验证码");
                    return;
                }
            } catch (Exception e) {
                result.put("success", false);
                result.put("message", "系统错误，请稍后重试");
                objectMapper.writeValue(response.getWriter(), result);
                logUtil.logError("VERIFICATION_CODE_ERROR", "检查邮箱注册状态时发生错误: " + e.getMessage());
                return;
            }
        }

        // 发送验证码
        boolean sent = verificationCodeService.sendCode(email);

        if (sent) {
            result.put("success", true);
            result.put("message", "验证码已发送，请查收邮件");
            logUtil.log("VERIFICATION_CODE_SENT", email, "成功发送验证码到邮箱");
        } else {
            result.put("success", false);
            result.put("message", "验证码发送失败，请稍后重试");
            logUtil.logError("VERIFICATION_CODE_ERROR", "发送验证码到邮箱 " + email + " 失败");
        }

        objectMapper.writeValue(response.getWriter(), result);
    }

    /**
     * 处理修改密码请求
     */
    private void handlePasswordUpdate(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Object userObj = session.getAttribute("user");
        User currentUser = null;

        try {
            // 获取当前用户
            if (userObj instanceof Map) {
                Map<String, Object> userMap = (Map<String, Object>) userObj;
                Integer userId = (Integer) userMap.get("id");
                currentUser = userService.getUserById(userId);
            } else if (userObj instanceof User) {
                currentUser = (User) userObj;
                currentUser = userService.getUserById(currentUser.getId());
            }

            if (currentUser == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            // 获取请求参数
            String email = request.getParameter("email");
            String verifyCode = request.getParameter("verifyCode");
            String newPassword = request.getParameter("newPassword");
            String confirmPassword = request.getParameter("confirmPassword");

            // 参数验证
            if (email == null || verifyCode == null || newPassword == null || confirmPassword == null) {
                request.setAttribute("message", "所有字段都不能为空");
                request.setAttribute("messageType", "error");
                request.getRequestDispatcher("/WEB-INF/views/profile/password.jsp").forward(request, response);
                logUtil.logError("PASSWORD_UPDATE_ERROR", "用户[" + currentUser.getUsername() + "]修改密码时提交了空字段");
                return;
            }

            // 验证码验证
            if (!verificationCodeService.verifyCode(email, verifyCode)) {
                request.setAttribute("message", "验证码无效或已过期");
                request.setAttribute("messageType", "error");
                request.getRequestDispatcher("/WEB-INF/views/profile/password.jsp").forward(request, response);
                logUtil.logError("PASSWORD_UPDATE_ERROR", "用户[" + currentUser.getUsername() + "]验证码无效或已过期");
                return;
            }

            // 密码匹配验证
            if (!newPassword.equals(confirmPassword)) {
                request.setAttribute("message", "两次输入的密码不一致");
                request.setAttribute("messageType", "error");
                request.getRequestDispatcher("/WEB-INF/views/profile/password.jsp").forward(request, response);
                logUtil.logError("PASSWORD_UPDATE_ERROR", "用户[" + currentUser.getUsername() + "]两次输入的密码不一致");
                return;
            }

            // 更新密码
            String salt = PasswordUtil.generateSalt();
            String hashedPassword = PasswordUtil.hashPassword(newPassword, salt);

            currentUser.setPassword(hashedPassword);
            currentUser.setSalt(salt);

            // 保存更新
            userService.updateUser(currentUser);

            // 更新会话中的用户信息
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", currentUser.getId());
            userMap.put("username", currentUser.getUsername());
            userMap.put("email", currentUser.getEmail());

            // 使用AvatarUtil处理头像URL
            String avatarUrl = com.blog.util.AvatarUtil.processAvatarUrl(currentUser.getAvatarUrl());
            userMap.put("avatarUrl", avatarUrl);

            // 判断是否为管理员（ID小于5的用户为管理员）
            userMap.put("isAdmin", currentUser.getId() < 5);
            session.setAttribute("user", userMap);

            // 记录密码修改日志
            logUtil.log("PASSWORD_UPDATE", currentUser.getUsername(), "用户成功修改了密码");

            // 设置成功消息并返回用户资料页
            request.setAttribute("message", "密码修改成功！");
            request.setAttribute("messageType", "success");
            response.sendRedirect(request.getContextPath() + "/profile?message=密码修改成功&messageType=success");

        } catch (Exception e) {
            String username = "未知用户";
            if (currentUser != null) {
                username = currentUser.getUsername();
            }

            request.setAttribute("message", "密码修改失败：" + e.getMessage());
            request.setAttribute("messageType", "error");
            request.getRequestDispatcher("/WEB-INF/views/profile/password.jsp").forward(request, response);
            logUtil.logError("PASSWORD_UPDATE_ERROR", "用户[" + username + "]修改密码失败: " + e.getMessage());
        }
    }

    /**
     * 处理忘记密码请求
     */
    private void handleForgotPassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 获取请求参数
        String email = request.getParameter("email");
        String verifyCode = request.getParameter("verifyCode");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        // 参数验证
        if (email == null || verifyCode == null || newPassword == null || confirmPassword == null) {
            request.setAttribute("message", "所有字段都不能为空");
            request.setAttribute("messageType", "error");
            request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
            logUtil.logError("PASSWORD_RECOVERY_ERROR", "邮箱[" + email + "]重置密码时提交了空字段");
            return;
        }

        try {
            // 根据邮箱查找用户
            User user = userService.getUserByEmail(email);

            if (user == null) {
                request.setAttribute("message", "该邮箱未注册");
                request.setAttribute("messageType", "error");
                request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
                logUtil.logError("PASSWORD_RECOVERY_ERROR", "邮箱[" + email + "]尝试重置密码但未注册");
                return;
            }

            // 验证码验证
            if (!verificationCodeService.verifyCode(email, verifyCode)) {
                request.setAttribute("message", "验证码无效或已过期");
                request.setAttribute("messageType", "error");
                request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
                logUtil.logError("PASSWORD_RECOVERY_ERROR", "邮箱[" + email + "]重置密码时验证码无效或已过期");
                return;
            }

            // 密码匹配验证
            if (!newPassword.equals(confirmPassword)) {
                request.setAttribute("message", "两次输入的密码不一致");
                request.setAttribute("messageType", "error");
                request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
                logUtil.logError("PASSWORD_RECOVERY_ERROR", "邮箱[" + email + "]重置密码时两次输入的密码不一致");
                return;
            }

            // 更新密码
            String salt = PasswordUtil.generateSalt();
            String hashedPassword = PasswordUtil.hashPassword(newPassword, salt);

            user.setPassword(hashedPassword);
            user.setSalt(salt);

            // 保存更新
            userService.updateUser(user);

            // 记录密码重置日志
            logUtil.log("PASSWORD_RECOVERY", user.getUsername(), "用户通过忘记密码功能成功重置了密码");

            // 设置成功消息并重定向到登录页
            response.sendRedirect(request.getContextPath() + "/login?message=密码重置成功，请使用新密码登录&messageType=success");

        } catch (Exception e) {
            request.setAttribute("message", "密码重置失败：" + e.getMessage());
            request.setAttribute("messageType", "error");
            request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
            logUtil.logError("PASSWORD_RECOVERY_ERROR", "邮箱[" + email + "]重置密码失败: " + e.getMessage());
        }
    }
}