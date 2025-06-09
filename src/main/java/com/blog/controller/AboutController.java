package com.blog.controller;

import com.blog.util.MarkdownUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@WebServlet("/about")
public class AboutController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String markdownContent = MarkdownUtil.readMarkdownContent(MarkdownUtil.getMarkdownFilePath(null, "UpdateLog.md"));
        request.setAttribute("content", markdownContent);
        request.getRequestDispatcher("/WEB-INF/views/about.jsp").forward(request, response);
    }
}