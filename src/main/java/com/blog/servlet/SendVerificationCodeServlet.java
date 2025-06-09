package com.blog.servlet;

import com.blog.dao.UserDao;
import com.blog.dao.impl.UserDaoImpl;
import com.blog.model.User;
import com.blog.service.VerificationCodeService;
import com.blog.util.LogUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理验证码发送请求的Servlet
 */
@WebServlet("/sendVerificationCode")
public class SendVerificationCodeServlet extends HttpServlet {
    private final VerificationCodeService verificationCodeService = VerificationCodeService.getInstance();
    private final LogUtil logUtil = LogUtil.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private UserDao userDao;

    @Override
    public void init() throws ServletException {
        userDao = new UserDaoImpl();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 设置响应类型
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 获取请求参数
        String email = request.getParameter("email");
        String type = request.getParameter("type"); // login 或 register

        Map<String, Object> result = new HashMap<>();

        // 参数验证
        if (email == null || email.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "邮箱地址不能为空");
            objectMapper.writeValue(response.getWriter(), result);
            return;
        }

        // 处理用户名到邮箱的转换（用于登录验证码）
        String targetEmail = email;
        if ("login".equals(type) && !email.contains("@")) {
            // 如果是登录类型且输入的不是邮箱格式，则认为是用户名，需要查找对应的邮箱
            try {
                User user = userDao.findByUsername(email);
                if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                    result.put("success", false);
                    result.put("message", "用户不存在或未绑定邮箱");
                    objectMapper.writeValue(response.getWriter(), result);
                    return;
                }
                targetEmail = user.getEmail();
            } catch (Exception e) {
                logUtil.logError("VERIFICATION_CODE_ERROR", "Error finding user by username: " + e.getMessage());
                result.put("success", false);
                result.put("message", "系统错误，请稍后重试");
                objectMapper.writeValue(response.getWriter(), result);
                return;
            }
        }

        // 验证邮箱格式
        if (!targetEmail.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            result.put("success", false);
            result.put("message", "邮箱格式不正确");
            objectMapper.writeValue(response.getWriter(), result);
            return;
        }

        try {
            // 发送验证码
            boolean sent = verificationCodeService.sendCode(targetEmail);

            if (sent) {
                result.put("success", true);
                result.put("message", "验证码已发送到 " + targetEmail + "，请查收邮件");
                logUtil.log("VERIFICATION_CODE_SENT", targetEmail, "Verification code sent for " + type + " (original input: " + email + ")");
            } else {
                result.put("success", false);
                result.put("message", "验证码发送失败，请稍后重试");
            }
        } catch (Exception e) {
            logUtil.logError("VERIFICATION_CODE_ERROR", "Error sending verification code: " + e.getMessage());
            result.put("success", false);
            result.put("message", "系统错误，请稍后重试");
        }

        objectMapper.writeValue(response.getWriter(), result);
    }
}