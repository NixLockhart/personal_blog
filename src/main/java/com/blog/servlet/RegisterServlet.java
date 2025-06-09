package com.blog.servlet;

import com.blog.dao.UserDao;
import com.blog.dao.impl.UserDaoImpl;
import com.blog.model.User;
import com.blog.service.VerificationCodeService;
import com.blog.util.PasswordUtil;
import com.blog.util.LogUtil;
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

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private UserDao userDao;
    private LogUtil logUtil;
    private final VerificationCodeService verificationCodeService = VerificationCodeService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        userDao = new UserDaoImpl();
        logUtil = LogUtil.getInstance();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        String verificationCode = request.getParameter("verificationCode");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> result = new HashMap<>();

        // 参数验证
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                verificationCode == null || verificationCode.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "所有字段都是必填的");
            objectMapper.writeValue(response.getWriter(), result);
            return;
        }

        // 验证验证码
        if (!verificationCodeService.verifyCode(email, verificationCode)) {
            result.put("success", false);
            result.put("message", "验证码无效或已过期");
            objectMapper.writeValue(response.getWriter(), result);
            return;
        }

        try {
            // 检查用户名是否已存在
            if (userDao.isUsernameExists(username)) {
                result.put("success", false);
                result.put("message", "用户名已被使用");
                objectMapper.writeValue(response.getWriter(), result);
                return;
            }

            // 检查邮箱是否已存在
            if (userDao.isEmailExists(email)) {
                result.put("success", false);
                result.put("message", "邮箱已被注册");
                objectMapper.writeValue(response.getWriter(), result);
                return;
            }

            // 创建新用户
            User user = new User();
            user.setUsername(username);
            String salt = PasswordUtil.generateSalt(); // 生成随机盐值
            user.setSalt(salt); // 设置盐值
            user.setPassword(PasswordUtil.hashPassword(password, salt)); // 使用盐值加密密码
            user.setEmail(email);

            // 保存用户
            userDao.insert(user);

            // 记录注册成功日志
            logUtil.log("USER_REGISTER", username, "User registration successful");

            // 返回成功响应和重定向信息
            result.put("success", true);
            result.put("message", "注册成功");
            result.put("redirect", request.getContextPath() + "/");
            objectMapper.writeValue(response.getWriter(), result);

        } catch (SQLException e) {
            logUtil.logError("USER_REGISTER", "Registration failed: " + e.getMessage());
            result.put("success", false);
            result.put("message", "注册失败，请稍后重试");
            objectMapper.writeValue(response.getWriter(), result);
        }
    }
}