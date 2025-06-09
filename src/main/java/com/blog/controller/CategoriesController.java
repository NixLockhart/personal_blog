package com.blog.controller;

import com.blog.dao.PostDao;
import com.blog.dao.impl.PostDaoImpl;
import com.blog.model.Post;
import com.blog.util.LogUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/categories")
public class CategoriesController extends HttpServlet {
    private PostDao postDao;
    private final LogUtil logUtil = LogUtil.getInstance();

    @Override
    public void init() throws ServletException {
        postDao = new PostDaoImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // 获取所有文章用于归档页面
            List<Post> articles = postDao.findAll();
            request.setAttribute("articles", articles);

            // 记录日志
            logUtil.log("CATEGORIES_PAGE", "system", "访问归档页面，加载" + articles.size() + "篇文章");

            // 转发到categories.jsp页面
            request.getRequestDispatcher("/WEB-INF/views/categories.jsp").forward(request, response);
        } catch (SQLException e) {
            logUtil.log("CATEGORIES_ERROR", "system", "加载归档页面失败: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "数据库错误");
        }
    }
}