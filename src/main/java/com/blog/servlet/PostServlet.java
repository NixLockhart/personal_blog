package com.blog.servlet;

import com.blog.dao.PostDao;
import com.blog.dao.impl.PostDaoImpl;
import com.blog.dao.CategoryDao;
import com.blog.dao.impl.CategoryDaoImpl;
import com.blog.model.Post;
import com.blog.model.Category;
import com.blog.util.LogUtil;
import com.blog.util.MarkdownUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class PostServlet extends HttpServlet {
    private PostDao postDao;
    private CategoryDao categoryDao;
    private final LogUtil logUtil = LogUtil.getInstance();

    @Override
    public void init() throws ServletException {
        postDao = new PostDaoImpl();
        categoryDao = new CategoryDaoImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        logUtil.log("POST_REQUEST", "system", "接收到博文请求：" + (pathInfo == null ? "/" : pathInfo));

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // 获取分类参数
                String categoryId = request.getParameter("category");
                List<Post> posts;
                int total = 0;

                if (categoryId != null && !categoryId.isEmpty()) {
                    // 获取指定分类的最近5篇博文
                    posts = postDao.findByCategoryId(Integer.parseInt(categoryId));
                    total = postDao.getTotalByCategory(Integer.parseInt(categoryId));
                    logUtil.log("POST_LIST", "system", String.format("获取分类 %s 的博文，共%d篇", categoryId, total));
                } else {
                    // 获取最近5篇博文
                    posts = postDao.findByPage(1, 5);
                    total = postDao.getTotal();
                    logUtil.log("POST_LIST", "system", String.format("获取到%d篇博文", posts.size()));
                }

                // 获取所有分类
                List<Category> categories = categoryDao.findAll();
                request.setAttribute("categories", categories);

                // 设置请求属性
                request.setAttribute("posts", posts);
                request.setAttribute("total", total);
                request.getRequestDispatcher("/index.jsp").forward(request, response);
            } else {
                // 提取文章ID
                String postIdStr = pathInfo.substring(1);
                int postId;
                try {
                    postId = Integer.parseInt(postIdStr);
                } catch (NumberFormatException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的文章ID");
                    return;
                }

                // 获取文章详情
                Post post = postDao.findById(postId);
                if (post == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "文章不存在");
                    return;
                }

                // 处理文章浏览量 - 使用User-Agent作为设备标识
                String deviceId = request.getHeader("User-Agent");
                if (deviceId == null || deviceId.isEmpty()) {
                    deviceId = "unknown_device";
                }

                // 检查今天是否已经浏览过这篇文章
                try {
                    if (!postDao.hasDeviceViewedToday(postId, deviceId)) {
                        // 记录浏览记录并增加浏览量
                        postDao.recordView(postId, deviceId);
                        postDao.incrementViews(postId);
                        logUtil.log("POST_VIEW", "device_" + deviceId, "文章ID " + postId + " 增加浏览量");
                    } else {
                        logUtil.log("POST_VIEW", "device_" + deviceId, "文章ID " + postId + " 今日已浏览过，不增加浏览量");
                    }
                } catch (SQLException e) {
                    logUtil.log("POST_VIEW_ERROR", "system", "增加浏览量失败: " + e.getMessage());
                    // 浏览量处理失败不应影响用户浏览文章
                }

                // 保存原始content作为文章简介
                String originalContent = post.getContent();
                request.setAttribute("postSummary", originalContent);

                // 如果有文章内容URL，加载Markdown内容
                if (post.getContentUrl() != null && !post.getContentUrl().isEmpty()) {
                    try {
                        String markdownContent = MarkdownUtil.loadMarkdownContent(post.getContentUrl(), post.getId());
                        if (markdownContent != null) {
                            // 使用Markdown内容替换post.content
                            post.setContent(markdownContent);
                        }
                    } catch (IOException e) {
                        logUtil.log("POST_MARKDOWN_ERROR", "system", "加载Markdown内容失败: " + e.getMessage());
                        request.setAttribute("markdownError", "无法加载Markdown内容: " + e.getMessage());
                    }
                }

                // 设置请求属性
                request.setAttribute("post", post);

                // 转发到文章详情页
                request.getRequestDispatcher("/WEB-INF/views/post.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            logUtil.log("POST_ERROR", "system", "数据库访问错误: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "数据库错误");
        }
    }
}