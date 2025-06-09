package com.blog.servlet;

import com.blog.dao.UserDao;
import com.blog.dao.impl.UserDaoImpl;
import com.blog.model.User;
import com.blog.service.VerificationCodeService;
import com.blog.util.AvatarUtil;
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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private UserDao userDao;
    private final LogUtil logUtil = LogUtil.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VerificationCodeService verificationCodeService = VerificationCodeService.getInstance();

    @Override
    public void init() throws ServletException {
        userDao = new UserDaoImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            // 已登录用户重定向到首页
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String remember = request.getParameter("remember");
        String verificationCode = request.getParameter("verificationCode");
        String loginType = request.getParameter("loginType"); // "password" 或 "verification"

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> result = new HashMap<>();

        // 参数验证
        if (username == null || username.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "用户名/邮箱不能为空");
            objectMapper.writeValue(response.getWriter(), result);
            return;
        }

        // 验证码登录模式
        if ("verification".equals(loginType)) {
            if (verificationCode == null || verificationCode.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "请输入验证码");
                objectMapper.writeValue(response.getWriter(), result);
                return;
            }
            handleVerificationLogin(username, verificationCode, request, response, result);
            return;
        }

        // 用户名/邮箱+密码登录模式
        if (password == null || password.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "密码不能为空");
            objectMapper.writeValue(response.getWriter(), result);
            return;
        }

        // 去除首尾空格
        username = username.trim();
        password = password.trim();

        if (username.isEmpty() || password.isEmpty()) {
            result.put("success", false);
            result.put("message", "用户名和密码不能为空格");
            objectMapper.writeValue(response.getWriter(), result);
            return;
        }

        try {
            // 查询用户信息（支持用户名或邮箱登录）
            User user = findUserByUsernameOrEmail(username);
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户名/邮箱或密码错误");
                objectMapper.writeValue(response.getWriter(), result);
                return;
            }

            // 验证密码
            if (!PasswordUtil.verifyPassword(password, user.getPassword(), user.getSalt())) {
                result.put("success", false);
                result.put("message", "用户名/邮箱或密码错误");
                logUtil.log("LOGIN_FAILED", username, "密码验证失败");
                objectMapper.writeValue(response.getWriter(), result);
                return;
            }

            // 创建会话并存储用户信息
            HttpSession session = request.getSession(true);
            // 设置会话超时时间（30分钟）
            session.setMaxInactiveInterval(30 * 60);

            // 存储用户信息（不包含敏感信息）
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("avatarUrl", AvatarUtil.processAvatarUrl(user.getAvatarUrl()));
            // 添加管理员标识，ID小于5的用户为管理员
            userInfo.put("isAdmin", user.getId() < 5);
            session.setAttribute("user", userInfo);

            // 记录登录日志
            logUtil.log("LOGIN_SUCCESS", username, "用户登录成功");

            result.put("success", true);
            result.put("message", "登录成功");
            result.put("redirect", request.getContextPath() + "/");
            objectMapper.writeValue(response.getWriter(), result);

        } catch (SQLException e) {
            logUtil.logError("LOGIN_ERROR", "数据库访问错误: " + e.getMessage());
            result.put("success", false);
            result.put("message", "系统错误，请稍后重试");
            objectMapper.writeValue(response.getWriter(), result);
        }
    }

    /**
     * 根据用户名或邮箱查找用户
     */
    private User findUserByUsernameOrEmail(String usernameOrEmail) throws SQLException {
        // 判断输入是否为邮箱格式
        if (usernameOrEmail.contains("@")) {
            return userDao.findByEmail(usernameOrEmail);
        } else {
            return userDao.findByUsername(usernameOrEmail);
        }
    }

    /**
     * 处理验证码登录
     */
    private void handleVerificationLogin(String usernameOrEmail, String verificationCode,
                                         HttpServletRequest request, HttpServletResponse response,
                                         Map<String, Object> result) throws IOException {
        try {
            // 查找用户
            User user = findUserByUsernameOrEmail(usernameOrEmail);
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                objectMapper.writeValue(response.getWriter(), result);
                return;
            }

            // 验证验证码
            if (!verificationCodeService.verifyCode(user.getEmail(), verificationCode)) {
                result.put("success", false);
                result.put("message", "验证码无效或已过期");
                objectMapper.writeValue(response.getWriter(), result);
                return;
            }

            // 验证码验证成功，创建会话
            HttpSession session = request.getSession(true);
            session.setMaxInactiveInterval(30 * 60);

            // 存储用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("avatarUrl", AvatarUtil.processAvatarUrl(user.getAvatarUrl()));
            userInfo.put("isAdmin", user.getId() < 5);
            session.setAttribute("user", userInfo);

            // 记录登录日志
            logUtil.log("LOGIN_SUCCESS_VERIFICATION", user.getUsername(), "用户通过验证码登录成功");

            result.put("success", true);
            result.put("message", "验证码登录成功");
            result.put("redirect", request.getContextPath() + "/");
            objectMapper.writeValue(response.getWriter(), result);

        } catch (SQLException e) {
            logUtil.logError("LOGIN_ERROR", "验证码登录数据库访问错误: " + e.getMessage());
            result.put("success", false);
            result.put("message", "系统错误，请稍后重试");
            objectMapper.writeValue(response.getWriter(), result);
        }
    }
}