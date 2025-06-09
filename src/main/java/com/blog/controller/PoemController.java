package com.blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet("/poem")
public class PoemController extends HttpServlet {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 获取诗词数据
        String poemJson = request.getParameter("poem");

        if (poemJson == null || poemJson.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        try {
            // 解析JSON数据
            Map<String, Object> poemData = objectMapper.readValue(poemJson, Map.class);
            // 将诗词数据存储到session中
            request.getSession().setAttribute("poem", poemData);
            // 重定向到GET请求
            response.sendRedirect(request.getContextPath() + "/poem");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 从session中获取诗词数据
        Map<String, Object> poemData = (Map<String, Object>) request.getSession().getAttribute("poem");

        if (poemData == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        // 将诗词数据传递给JSP
        request.setAttribute("poem", poemData);
        request.getRequestDispatcher("/WEB-INF/views/poem.jsp").forward(request, response);
    }
}