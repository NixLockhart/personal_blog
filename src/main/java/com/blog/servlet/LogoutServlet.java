package com.blog.servlet;

import com.blog.util.LogUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    private final LogUtil logUtil = LogUtil.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session != null) {
            // 记录用户退出日志
            Object userObj = session.getAttribute("user");
            if (userObj != null && userObj instanceof Map) {
                Map<String, Object> userInfo = (Map<String, Object>) userObj;
                String username = (String) userInfo.get("username");
                logUtil.log("LOGOUT", username, "用户退出登录");
            }

            // 使会话失效，清除所有会话属性
            session.invalidate();
        }

        // 重定向到首页
        response.sendRedirect(request.getContextPath() + "/");
    }
}