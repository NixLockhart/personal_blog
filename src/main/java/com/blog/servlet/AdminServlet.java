package com.blog.servlet;

import com.blog.dao.NotificationDao;
import com.blog.dao.PostDao;
import com.blog.dao.UserDao;
import com.blog.dao.VisitsDao;
import com.blog.dao.CommentDao;
import com.blog.dao.impl.NotificationDaoImpl;
import com.blog.dao.impl.PostDaoImpl;
import com.blog.dao.impl.UserDaoImpl;
import com.blog.dao.impl.VisitsDaoImpl;
import com.blog.dao.impl.CommentDaoImpl;
import com.blog.dao.CategoryDao;
import com.blog.dao.impl.CategoryDaoImpl;
import com.blog.model.Category;
import com.blog.model.Comment;
import com.blog.model.Notification;
import com.blog.model.Post;
import com.blog.model.User;
import com.blog.service.MessageService;
import com.blog.service.impl.MessageServiceImpl;
import com.blog.util.LogUtil;
import com.blog.util.MarkdownUtil;
import com.blog.util.AvatarUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.io.File;
import javax.servlet.http.Part;

@WebServlet("/admin/*")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024, // 1MB
        maxFileSize = 1024 * 1024 * 10,  // 10MB
        maxRequestSize = 1024 * 1024 * 30 // 30MB
)
public class AdminServlet extends HttpServlet {
    private final UserDao userDao = new UserDaoImpl();
    private final PostDao postDao = new PostDaoImpl();
    private final CategoryDao categoryDao = new CategoryDaoImpl();
    private final NotificationDao notificationDao = new NotificationDaoImpl();
    private final VisitsDao visitsDao = new VisitsDaoImpl();
    private final MessageService messageService = new MessageServiceImpl();
    private final CommentDao commentDao = new CommentDaoImpl();
    private final LogUtil logUtil = LogUtil.getInstance();

    public AdminServlet() {
        // 构造函数中不需要再初始化字段，因为已经在声明时初始化
    }

    @Override
    public void init() throws ServletException {
        // 初始化方法中不需要再初始化字段，因为已经在声明时初始化
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String method = request.getParameter("_method");
        if ("DELETE".equalsIgnoreCase(method)) {
            doDelete(request, response);
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith("/users/delete")) {
            deleteUser(request, response);
            return;
        } else if (pathInfo != null && pathInfo.equals("/users/update")) {
            updateUser(request, response);
            return;
        } else if (pathInfo != null && pathInfo.equals("/notifications/publish")) {
            publishNotification(request, response);
            return;
        } else if (pathInfo != null && pathInfo.equals("/posts/save")) {
            savePost(request, response);
            return;
        } else if (pathInfo != null && pathInfo.equals("/posts/delete")) {
            deletePost(request, response);
            return;
        }
        // 处理其他POST请求...
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 验证用户是否登录且是管理员
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Map<String, Object> userInfo = (Map<String, Object>) session.getAttribute("user");
        Boolean isAdmin = (Boolean) userInfo.get("isAdmin");

        if (isAdmin == null || !isAdmin) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith("/notifications/delete")) {
            deleteNotification(request, response);
        }
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 验证用户是否登录且是管理员
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Map<String, Object> userInfo = (Map<String, Object>) session.getAttribute("user");
        Boolean isAdmin = (Boolean) userInfo.get("isAdmin");
        Integer currentUserId = (Integer) userInfo.get("id");
        String username = (String) userInfo.get("username");

        if (isAdmin == null || !isAdmin) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        try {
            String userId = request.getParameter("userId");
            if (userId != null && !userId.trim().isEmpty()) {
                int userIdToDelete = Integer.parseInt(userId);

                // 检查是否试图删除自己
                if (userIdToDelete == currentUserId) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "不能删除当前登录用户");
                    return;
                }

                // 检查是否试图删除管理员用户
                if (userIdToDelete < 5) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "不能删除管理员用户");
                    return;
                }

                // 获取用户信息以便日志记录
                User userToDelete = userDao.findById(userIdToDelete);
                if (userToDelete == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "用户不存在");
                    return;
                }

                // 记录开始删除用户操作
                logUtil.log("USER_DELETE", username, "开始删除用户: " + userToDelete.getUsername() + " (ID: " + userIdToDelete + ")");

                // 先处理外键约束

                // 1. 处理评论表关联
                try {
                    // 获取用户的评论，并记录数量
                    List<Comment> userComments = commentDao.findByUserId(userIdToDelete);
                    logUtil.log("USER_DELETE", username, "找到用户评论 " + userComments.size() + " 条，准备删除");

                    // 删除评论
                    for (Comment comment : userComments) {
                        commentDao.deleteById(comment.getId());
                    }
                    logUtil.log("USER_DELETE", username, "成功删除用户评论 " + userComments.size() + " 条");
                } catch (SQLException e) {
                    logUtil.logError("USER_DELETE_ERROR", "删除用户评论失败: " + e.getMessage());
                    // 不要中断，继续尝试删除其他关联数据
                }

                // 2. 处理文章表关联
                try {
                    // 找到用户发布的所有文章
                    List<Post> userPosts = postDao.findByUserId(userIdToDelete);
                    logUtil.log("USER_DELETE", username, "找到用户文章 " + userPosts.size() + " 篇，准备处理");

                    // 方案1：删除文章及其相关数据
                    for (Post post : userPosts) {
                        // 先删除文章评论
                        commentDao.deleteByPostId(post.getId());
                        // 再删除文章本身
                        postDao.deleteById(post.getId());
                    }
                    logUtil.log("USER_DELETE", username, "成功删除用户文章 " + userPosts.size() + " 篇");

                    // 或者方案2：将文章转移给管理员（如果需要保留内容）
                    // 这里选择方案1直接删除
                } catch (SQLException e) {
                    logUtil.logError("USER_DELETE_ERROR", "删除用户文章失败: " + e.getMessage());
                    // 不要中断，继续尝试删除用户
                }

                // 3. 删除用户账号
                try {
                    int result = userDao.deleteById(userIdToDelete);
                    if (result > 0) {
                        logUtil.log("USER_DELETE", username, "成功删除用户: " + userToDelete.getUsername() + " (ID: " + userIdToDelete + ")");
                        // 设置成功消息
                        request.getSession().setAttribute("success", "用户删除成功");
                    } else {
                        logUtil.logError("USER_DELETE_ERROR", "删除用户记录失败，用户ID: " + userIdToDelete);
                        request.getSession().setAttribute("error", "删除用户失败，请重试");
                    }
                } catch (SQLException e) {
                    logUtil.logError("USER_DELETE_ERROR", "删除用户账号失败: " + e.getMessage());
                    throw e; // 重新抛出异常，让下面的 catch 块处理
                }

                // 重定向回用户管理页面
                response.sendRedirect(request.getContextPath() + "/admin/users");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "用户ID不能为空");
            }
        } catch (SQLException e) {
            logUtil.logError("USER_DELETE_ERROR", "删除用户失败: " + e.getMessage());
            // 设置错误消息并重定向，而不是返回 500 错误
            request.getSession().setAttribute("error", "删除用户失败: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/users");
        } catch (NumberFormatException e) {
            logUtil.logError("USER_DELETE_ERROR", "用户ID格式无效: " + e.getMessage());
            // 设置错误消息并重定向
            request.getSession().setAttribute("error", "用户ID格式无效");
            response.sendRedirect(request.getContextPath() + "/admin/users");
        } catch (Exception e) {
            logUtil.logError("USER_DELETE_ERROR", "删除用户时发生未知错误: " + e.getMessage());
            // 设置错误消息并重定向
            request.getSession().setAttribute("error", "删除用户时发生未知错误");
            response.sendRedirect(request.getContextPath() + "/admin/users");
        }
    }

    private void publishNotification(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 验证用户是否登录且是管理员
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Map<String, Object> userInfo = (Map<String, Object>) session.getAttribute("user");
        Boolean isAdmin = (Boolean) userInfo.get("isAdmin");
        Integer adminUserId = (Integer) userInfo.get("id");

        if (isAdmin == null || !isAdmin) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        try {
            String title = request.getParameter("title");
            String content = request.getParameter("content");
            String type = request.getParameter("type");
            String recipient = request.getParameter("recipient");

            if (title == null || title.trim().isEmpty() ||
                    content == null || content.trim().isEmpty() ||
                    type == null || type.trim().isEmpty() ||
                    recipient == null || recipient.trim().isEmpty()) {
                request.setAttribute("error", "所有字段都必须填写");
                showNotificationManagement(request, response);
                return;
            }

            List<Notification> notifications = new ArrayList<>();

            // 处理接收者
            if ("all".equals(recipient)) {
                // 发送给所有用户
                try {
                    List<User> allUsers = userDao.findAll();
                    for (User user : allUsers) {
                        Integer userId = user.getId();

                        Notification notification = new Notification();
                        notification.setUserId(userId);
                        notification.setType(type);
                        notification.setTitle(title);
                        notification.setContent(content);
                        notification.setFromUserId(adminUserId);
                        notification.setIsRead(false);

                        notifications.add(notification);
                    }

                    notificationDao.batchInsert(notifications);

                    request.setAttribute("success", "通知已成功发送给所有用户");
                } catch (SQLException e) {
                    logUtil.logError("NOTIFICATION_SEND_ERROR", "发送通知给所有用户失败: " + e.getMessage());
                    request.setAttribute("error", "发送通知失败: " + e.getMessage());
                }
            } else {
                // 发送给特定用户
                try {
                    Integer userId = Integer.parseInt(recipient);

                    Notification notification = new Notification();
                    notification.setUserId(userId);
                    notification.setType(type);
                    notification.setTitle(title);
                    notification.setContent(content);
                    notification.setFromUserId(adminUserId);
                    notification.setIsRead(false);

                    notificationDao.insert(notification);

                    request.setAttribute("success", "通知已成功发送给指定用户");
                } catch (NumberFormatException e) {
                    request.setAttribute("error", "无效的用户ID");
                } catch (SQLException e) {
                    logUtil.logError("NOTIFICATION_SEND_ERROR", "发送通知给特定用户失败: " + e.getMessage());
                    request.setAttribute("error", "发送通知失败: " + e.getMessage());
                }
            }

            // 重定向回通知管理页面
            showNotificationManagement(request, response);

        } catch (Exception e) {
            logUtil.logError("NOTIFICATION_SEND_ERROR", "发送通知失败: " + e.getMessage());
            request.setAttribute("error", "发送通知失败: " + e.getMessage());
            showNotificationManagement(request, response);
        }
    }

    private void deleteNotification(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 验证用户是否登录且是管理员
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Map<String, Object> userInfo = (Map<String, Object>) session.getAttribute("user");
        Boolean isAdmin = (Boolean) userInfo.get("isAdmin");

        if (isAdmin == null || !isAdmin) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        try {
            String notificationId = request.getParameter("notificationId");
            if (notificationId != null && !notificationId.trim().isEmpty()) {
                int id = Integer.parseInt(notificationId);
                notificationDao.deleteById(id);
                response.sendRedirect(request.getContextPath() + "/admin/notifications");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "通知ID不能为空");
            }
        } catch (SQLException e) {
            logUtil.logError("NOTIFICATION_DELETE_ERROR", "删除通知失败: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "删除通知失败");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 验证用户是否登录且是管理员
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Map<String, Object> userInfo = (Map<String, Object>) session.getAttribute("user");
        Boolean isAdmin = (Boolean) userInfo.get("isAdmin");

        if (isAdmin == null || !isAdmin) {
            // 非管理员用户重定向到首页
            response.sendRedirect(request.getContextPath() + "/");
            logUtil.log("ADMIN_ACCESS_DENIED", userInfo.get("username").toString(), "非管理员用户尝试访问后台");
            return;
        }

        // 获取请求的路径信息
        String pathInfo = request.getPathInfo();

        // 根据路径信息处理不同的请求
        if (pathInfo == null || pathInfo.equals("/")) {
            showDashboard(request, response);
        } else if (pathInfo.equals("/users")) {
            showUserManagement(request, response);
        } else if (pathInfo.startsWith("/users/edit")) {
            showUserEditForm(request, response);
        } else if (pathInfo.equals("/posts")) {
            showPostManagement(request, response);
        } else if (pathInfo.equals("/posts/new")) {
            showPostEditPage(request, response, null);
        } else if (pathInfo.startsWith("/posts/edit/")) {
            // 获取文章ID
            try {
                int postId = Integer.parseInt(pathInfo.substring("/posts/edit/".length()));
                Post post = postDao.findById(postId);
                if (post != null) {
                    // 从文件中读取Markdown内容
                    if (post.getContentUrl() != null && !post.getContentUrl().isEmpty()) {
                        try {
                            String markdownContent = MarkdownUtil.readMarkdownContentById(post.getId(), post.getContentUrl());
                            post.setContent(markdownContent);
                        } catch (Exception e) {
                            logUtil.logError("POST_EDIT_ERROR", "读取Markdown内容失败: " + e.getMessage());
                            request.setAttribute("error", "无法读取文章内容: " + e.getMessage());
                        }
                    }
                    showPostEditPage(request, response, post);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "文章不存在");
                }
            } catch (NumberFormatException | SQLException e) {
                logUtil.logError("ADMIN_ERROR", "获取文章数据失败: " + e.getMessage());
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取文章数据失败");
            }
        } else if (pathInfo.equals("/categories")) {
            // 暂时返回404，功能尚未实现
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "分类管理功能正在开发中");
        } else if (pathInfo.equals("/settings")) {
            // 暂时返回404，功能尚未实现
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "设置功能正在开发中");
        } else if (pathInfo.equals("/notifications")) {
            showNotificationManagement(request, response);
        } else {
            // 默认显示仪表盘
            showDashboard(request, response);
        }
    }

    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // 获取统计数据
            int totalUsers = userDao.findAll().size();
            int totalPosts = postDao.findAll().size();
            int totalViews = visitsDao.getTotalVisits();
            int totalMessages = messageService.getTotalCount();

            // 设置请求属性
            request.setAttribute("totalUsers", totalUsers);
            request.setAttribute("totalPosts", totalPosts);
            request.setAttribute("totalViews", totalViews);
            request.setAttribute("totalMessages", totalMessages);
            request.setAttribute("activeTab", "dashboard");

            // 转发到管理布局页面，并包含仪表盘内容
            request.setAttribute("contentPage", "/WEB-INF/views/admin/dashboard.jsp");
            request.getRequestDispatcher("/WEB-INF/views/admin/admin_layout.jsp").forward(request, response);
        } catch (SQLException e) {
            logUtil.logError("ADMIN_ERROR", "获取统计数据失败: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取统计数据失败");
        }
    }

    private void showUserManagement(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // 获取所有用户
            request.setAttribute("users", userDao.findAll());
            request.setAttribute("activeTab", "users");

            // 转发到管理布局页面，并包含用户管理内容
            request.setAttribute("contentPage", "/WEB-INF/views/admin/users.jsp");
            request.getRequestDispatcher("/WEB-INF/views/admin/admin_layout.jsp").forward(request, response);
        } catch (SQLException e) {
            logUtil.logError("ADMIN_ERROR", "获取用户数据失败: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取用户数据失败");
        }
    }

    private void showPostManagement(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // 获取所有文章
            request.setAttribute("posts", postDao.findAll());
            request.setAttribute("activeTab", "posts");

            // 转发到管理布局页面，并包含内容管理页面
            request.setAttribute("contentPage", "/WEB-INF/views/admin/posts.jsp");
            request.getRequestDispatcher("/WEB-INF/views/admin/admin_layout.jsp").forward(request, response);
        } catch (SQLException e) {
            logUtil.logError("ADMIN_ERROR", "获取文章数据失败: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取文章数据失败");
        }
    }

    private void showNotificationManagement(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // 获取搜索和筛选参数
            String keyword = request.getParameter("keyword");
            String type = request.getParameter("type");
            String userIdParam = request.getParameter("userId");
            Integer userId = null;

            if (userIdParam != null && !userIdParam.trim().isEmpty()) {
                try {
                    userId = Integer.parseInt(userIdParam);
                } catch (NumberFormatException e) {
                    // 忽略无效的userId参数
                }
            }

            // 获取所有通知
            List<Notification> allNotifications = notificationDao.findAll();
            List<Notification> filteredNotifications = new ArrayList<>();

            // 应用筛选条件
            for (Notification notification : allNotifications) {
                boolean matchesKeyword = keyword == null || keyword.trim().isEmpty() ||
                        notification.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                        (notification.getContent() != null && notification.getContent().toLowerCase().contains(keyword.toLowerCase())) ||
                        (notification.getRecipientUsername() != null && notification.getRecipientUsername().toLowerCase().contains(keyword.toLowerCase()));

                boolean matchesType = type == null || type.trim().isEmpty() ||
                        (notification.getType() != null && notification.getType().equals(type));

                boolean matchesUser = userId == null || notification.getUserId().equals(userId);

                if (matchesKeyword && matchesType && matchesUser) {
                    filteredNotifications.add(notification);
                }
            }

            // 获取用户列表（用于选择接收者）
            List<User> users = userDao.findAll();

            request.setAttribute("notifications", filteredNotifications);
            request.setAttribute("users", users);
            request.setAttribute("activeTab", "notifications");

            // 转发到管理布局页面，并包含通知管理页面
            request.setAttribute("contentPage", "/WEB-INF/views/admin/notifications.jsp");
            request.getRequestDispatcher("/WEB-INF/views/admin/admin_layout.jsp").forward(request, response);
        } catch (SQLException e) {
            logUtil.logError("ADMIN_ERROR", "获取通知数据失败: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取通知数据失败");
        }
    }

    /**
     * 显示博文编辑页面
     */
    private void showPostEditPage(HttpServletRequest request, HttpServletResponse response, Post post)
            throws ServletException, IOException {
        try {
            // 获取所有分类
            List<Category> categories = new CategoryDaoImpl().findAll();
            request.setAttribute("categories", categories);

            // 如果是编辑现有文章
            if (post != null) {
                // 从文件中读取Markdown内容
                if (post.getContentUrl() != null && !post.getContentUrl().isEmpty()) {
                    try {
                        String markdownContent = MarkdownUtil.readMarkdownContentById(post.getId(), post.getContentUrl());
                        if (markdownContent != null && !markdownContent.isEmpty()) {
                            post.setContent(markdownContent);
                            logUtil.log("POST_EDIT", "system", "成功加载文章内容，ID: " + post.getId() + ", 内容长度: " + markdownContent.length());
                        } else {
                            logUtil.logError("POST_EDIT_ERROR", "文章内容为空，ID: " + post.getId());
                            request.setAttribute("error", "文章内容为空");
                        }
                    } catch (Exception e) {
                        logUtil.logError("POST_EDIT_ERROR", "读取Markdown内容失败: " + e.getMessage());
                        request.setAttribute("error", "无法读取文章内容: " + e.getMessage());
                    }
                } else {
                    logUtil.logError("POST_EDIT_ERROR", "文章内容URL为空，ID: " + post.getId());
                    request.setAttribute("error", "文章内容URL为空");
                }

                // 设置文章信息到请求属性
                request.setAttribute("post", post);
                logUtil.log("POST_EDIT", "system", "设置文章信息到请求属性，ID: " + post.getId() +
                        ", 标题: " + post.getTitle() +
                        ", 内容长度: " + (post.getContent() != null ? post.getContent().length() : 0));
            }

            request.setAttribute("activeTab", "posts");
            request.setAttribute("contentPage", "/WEB-INF/views/admin/post_edit.jsp");
            request.getRequestDispatcher("/WEB-INF/views/admin/admin_layout.jsp").forward(request, response);
        } catch (Exception e) {
            logUtil.logError("ADMIN_ERROR", "获取分类数据失败: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取分类数据失败");
        }
    }

    /**
     * 保存博文
     */
    private void savePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 设置请求编码，确保UTF-8编码正确处理
        request.setCharacterEncoding("UTF-8");

        // 设置响应内容类型
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 添加日志：开始处理博文保存请求
        logUtil.log("POST_SAVE", "system", "开始处理博文保存请求");

        // 调试：列出所有请求参数
        logUtil.log("POST_SAVE", "system", "请求参数列表开始 ===============");
        logUtil.log("POST_SAVE", "system", "请求方法: " + request.getMethod());
        logUtil.log("POST_SAVE", "system", "请求内容类型: " + request.getContentType());
        logUtil.log("POST_SAVE", "system", "请求编码: " + request.getCharacterEncoding());

        java.util.Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            if (!"content".equals(paramName)) {
                // 对于非内容参数，直接记录值
                logUtil.log("POST_SAVE", "system", "参数: " + paramName + " = " + paramValue);
            } else {
                // 对于内容参数，只记录长度
                logUtil.log("POST_SAVE", "system", "参数: content, 长度 = " +
                        (paramValue != null ? paramValue.length() : 0));
            }
        }
        logUtil.log("POST_SAVE", "system", "请求参数列表结束 ===============");

        // 调试：检查内容类型
        logUtil.log("POST_SAVE", "system", "请求内容类型: " + request.getContentType());

        // 应急逻辑：如果没有获取到任何参数，尝试直接读取请求体
        boolean hasAnyParam = false;
        java.util.Enumeration<String> checkParams = request.getParameterNames();
        while (checkParams.hasMoreElements()) {
            hasAnyParam = true;
            break;
        }

        if (!hasAnyParam) {
            logUtil.log("POST_SAVE", "system", "未检测到任何参数，尝试直接读取请求体");
            try {
                StringBuilder requestBody = new StringBuilder();
                String line;
                java.io.BufferedReader reader = request.getReader();
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }

                // 记录请求体内容
                String bodyContent = requestBody.toString();
                logUtil.log("POST_SAVE", "system", "请求体内容长度: " + bodyContent.length());
                if (!bodyContent.isEmpty()) {
                    // 保存请求体到临时文件，以便查看
                    String tempDir = System.getProperty("java.io.tmpdir");
                    String requestBodyFile = Paths.get(tempDir, "blog_request_body_" + System.currentTimeMillis() + ".txt").toString();
                    Files.write(Paths.get(requestBodyFile), bodyContent.getBytes());
                    logUtil.log("POST_SAVE", "system", "已保存请求体内容到文件: " + requestBodyFile);
                }
            } catch (Exception e) {
                logUtil.logError("POST_SAVE_ERROR", "读取请求体失败: " + e.getMessage());
            }
        }

        try {
            // 从会话中获取用户ID
            HttpSession session = request.getSession(false);
            Map<String, Object> userInfo = (Map<String, Object>) session.getAttribute("user");
            Integer userId = (Integer) userInfo.get("id");
            String username = (String) userInfo.get("username");

            logUtil.log("POST_SAVE", username, "用户ID: " + userId + " 开始保存博文");

            // 获取表单数据
            String postIdStr = request.getParameter("postId");
            String title = request.getParameter("title");
            String contentUrl = request.getParameter("contentUrl");
            String content = request.getParameter("content");
            String categoryIdStr = request.getParameter("categoryId");
            String summary = request.getParameter("summary");

            // 日志记录表单数据
            logUtil.log("POST_SAVE", username, "提交的表单数据 - 标题: " + title +
                    ", 文件名: " + contentUrl +
                    ", 内容长度: " + (content != null ? content.length() : 0) +
                    ", 分类ID: " + categoryIdStr +
                    ", 文章ID: " + (postIdStr != null && !postIdStr.isEmpty() ? postIdStr : "新文章") +
                    ", 简介长度: " + (summary != null ? summary.length() : 0));

            // 创建表单数据备份，以备错误恢复
            if (content != null && content.length() > 0) {
                try {
                    // 保存备份到临时文件
                    String tempDir = System.getProperty("java.io.tmpdir");
                    String backupFilePath = Paths.get(tempDir, "blog_backup_" + System.currentTimeMillis() + ".txt").toString();

                    StringBuffer backupData = new StringBuffer();
                    backupData.append("提交时间: ").append(new Date()).append("\n");
                    backupData.append("用户ID: ").append(userId).append("\n");
                    backupData.append("用户名: ").append(username).append("\n");
                    backupData.append("文章ID: ").append(postIdStr).append("\n");
                    backupData.append("标题: ").append(title).append("\n");
                    backupData.append("文件名: ").append(contentUrl).append("\n");
                    backupData.append("分类ID: ").append(categoryIdStr).append("\n");
                    backupData.append("简介: ").append(summary).append("\n");
                    backupData.append("内容长度: ").append(content.length()).append("\n");
                    backupData.append("内容: \n").append(content);

                    Files.write(Paths.get(backupFilePath), backupData.toString().getBytes());
                    logUtil.log("POST_SAVE", username, "已创建表单数据备份: " + backupFilePath);
                } catch (Exception e) {
                    logUtil.logError("POST_SAVE_WARNING", "创建备份文件失败: " + e.getMessage());
                    // 继续处理，不中断流程
                }
            }

            // 验证必填字段
            if (title == null || title.trim().isEmpty() ||
                    contentUrl == null || contentUrl.trim().isEmpty() ||
                    content == null || content.trim().isEmpty() ||
                    categoryIdStr == null || categoryIdStr.trim().isEmpty()) {

                logUtil.logError("POST_SAVE_ERROR", "必填字段缺失 - 标题: " + (title == null ? "缺失" : "已填") +
                        ", 文件名: " + (contentUrl == null ? "缺失" : "已填") +
                        ", 内容: " + (content == null ? "缺失" : "已填") +
                        ", 分类: " + (categoryIdStr == null ? "缺失" : "已填"));

                out.println("{\"success\": false, \"message\": \"所有字段都必须填写\"}");
                return;
            }

            logUtil.log("POST_SAVE", username, "表单验证通过");

            // 检查文件名格式
            String originalContentUrl = contentUrl;
            if (!contentUrl.endsWith(".md")) {
                contentUrl += ".md";
                logUtil.log("POST_SAVE", username, "文件名格式调整: " + originalContentUrl + " -> " + contentUrl);
            }

            Integer categoryId = Integer.parseInt(categoryIdStr);
            Post post;
            boolean isNewPost = true;

            // 确定是新建还是更新
            if (postIdStr != null && !postIdStr.trim().isEmpty()) {
                // 更新现有文章
                logUtil.log("POST_SAVE", username, "尝试更新现有文章ID: " + postIdStr);
                post = postDao.findById(Integer.parseInt(postIdStr));
                if (post == null) {
                    logUtil.logError("POST_SAVE_ERROR", "文章不存在，ID: " + postIdStr);
                    out.println("{\"success\": false, \"message\": \"文章不存在\"}");
                    return;
                }
                isNewPost = false;
                logUtil.log("POST_SAVE", username, "找到现有文章，准备更新");
            } else {
                // 创建新文章
                logUtil.log("POST_SAVE", username, "准备创建新文章");
                post = new Post();
                post.setUserId(userId);
                post.setCreatedAt(new Date());
                post.setViews(0);
                post.setLikes(0);
            }

            // 更新文章属性
            post.setTitle(title);
            post.setContentUrl(contentUrl);
            post.setCategoryId(categoryId);
            post.setContent(summary != null && !summary.trim().isEmpty() ? summary : content);
            post.setSummary(summary);
            post.setUpdatedAt(new Date());

            logUtil.log("POST_SAVE", username, "文章对象准备完成，准备保存到数据库");

            // 保存到数据库
            int result;
            if (isNewPost) {
                logUtil.log("POST_SAVE", username, "执行插入新文章操作");
                result = postDao.insert(post);
                logUtil.log("POST_SAVE", username, "新文章插入结果: " + (result > 0 ? "成功" : "失败") +
                        ", 获得的文章ID: " + post.getId());
            } else {
                logUtil.log("POST_SAVE", username, "执行更新现有文章操作，ID: " + post.getId());
                result = postDao.update(post);
                logUtil.log("POST_SAVE", username, "文章更新结果: " + (result > 0 ? "成功" : "失败"));
            }

            if (result > 0) {
                // 保存成功后，将Markdown内容写入文件
                logUtil.log("POST_SAVE", username, "准备将Markdown内容保存到文件: " + contentUrl);
                boolean saved = MarkdownUtil.saveMarkdownContent(post.getId(), contentUrl, content);

                if (saved) {
                    logUtil.log("POST_SAVED", username,
                            "文章保存完成 - 类型: " + (isNewPost ? "新建" : "更新") +
                                    ", 标题: " + title +
                                    ", ID: " + post.getId() +
                                    ", 文件名: " + contentUrl);
                    // 重定向到内容管理页面
                    response.sendRedirect(request.getContextPath() + "/admin/posts");
                    return;
                } else {
                    // 文件保存失败，但数据库已更新
                    logUtil.logError("POST_SAVE_ERROR", "文章数据库保存成功，但内容保存到文件失败: " + contentUrl +
                            ", 文章ID: " + post.getId());
                    // 重定向到内容管理页面，但显示警告消息
                    request.getSession().setAttribute("warning", "警告：文章已保存到数据库，但内容保存到文件失败，请联系管理员");
                    response.sendRedirect(request.getContextPath() + "/admin/posts");
                    return;
                }
            } else {
                logUtil.logError("POST_SAVE_ERROR", "保存文章到数据库失败，返回结果: " + result);
                // 重定向到内容管理页面，显示错误消息
                request.getSession().setAttribute("error", "保存文章到数据库失败");
                response.sendRedirect(request.getContextPath() + "/admin/posts");
                return;
            }
        } catch (NumberFormatException e) {
            logUtil.logError("POST_SAVE_ERROR", "参数格式错误: " + e.getMessage() + "\n" + getStackTrace(e));
            out.println("{\"success\": false, \"message\": \"参数格式错误: " + e.getMessage() + "\"}");
        } catch (SQLException e) {
            logUtil.logError("POST_SAVE_ERROR", "数据库错误: " + e.getMessage() + "\n" + getStackTrace(e));
            out.println("{\"success\": false, \"message\": \"数据库错误: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            logUtil.logError("POST_SAVE_ERROR", "未知错误: " + e.getMessage() + "\n" + getStackTrace(e));
            out.println("{\"success\": false, \"message\": \"保存失败: " + e.getMessage() + "\"}");
        } finally {
            logUtil.log("POST_SAVE", "system", "博文保存请求处理完成");
        }
    }

    /**
     * 获取异常堆栈跟踪信息的字符串表示
     */
    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * 删除博文
     */
    private void deletePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 验证用户是否登录且是管理员
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Map<String, Object> userInfo = (Map<String, Object>) session.getAttribute("user");
        Boolean isAdmin = (Boolean) userInfo.get("isAdmin");
        String username = (String) userInfo.get("username");

        if (isAdmin == null || !isAdmin) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        try {
            String postIdStr = request.getParameter("postId");
            if (postIdStr != null && !postIdStr.trim().isEmpty()) {
                int postId = Integer.parseInt(postIdStr);

                // 获取文章信息用于日志记录
                Post post = postDao.findById(postId);
                if (post != null) {
                    // 删除文章
                    int result = postDao.deleteById(postId);
                    if (result > 0) {
                        // 删除成功，记录日志
                        logUtil.log("POST_DELETE", username, "成功删除文章: " + post.getTitle() + ", ID: " + postId);
                        request.getSession().setAttribute("success", "文章删除成功");
                    } else {
                        // 删除失败
                        logUtil.logError("POST_DELETE_ERROR", "删除文章失败，ID: " + postId);
                        request.getSession().setAttribute("error", "删除文章失败");
                    }
                } else {
                    // 文章不存在
                    logUtil.logError("POST_DELETE_ERROR", "文章不存在，ID: " + postId);
                    request.getSession().setAttribute("error", "文章不存在");
                }
            } else {
                // 参数错误
                logUtil.logError("POST_DELETE_ERROR", "文章ID不能为空");
                request.getSession().setAttribute("error", "文章ID不能为空");
            }
        } catch (NumberFormatException e) {
            logUtil.logError("POST_DELETE_ERROR", "文章ID格式错误: " + e.getMessage());
            request.getSession().setAttribute("error", "文章ID格式错误");
        } catch (SQLException e) {
            logUtil.logError("POST_DELETE_ERROR", "数据库错误: " + e.getMessage());
            request.getSession().setAttribute("error", "删除文章时发生数据库错误");
        } catch (Exception e) {
            logUtil.logError("POST_DELETE_ERROR", "未知错误: " + e.getMessage());
            request.getSession().setAttribute("error", "删除文章时发生未知错误");
        }

        // 重定向回内容管理页面
        response.sendRedirect(request.getContextPath() + "/admin/posts");
    }

    // 显示用户编辑表单
    private void showUserEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // 获取要编辑的用户ID
            String userIdParam = request.getParameter("userId");
            if (userIdParam == null || userIdParam.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "缺少用户ID参数");
                return;
            }

            int userId = Integer.parseInt(userIdParam);
            User user = userDao.findById(userId);

            if (user == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "找不到指定ID的用户");
                return;
            }

            // 将用户信息设置为request属性
            request.setAttribute("editUser", user);
            request.setAttribute("activeTab", "users");

            // 转发到管理布局页面，并包含用户编辑表单
            request.setAttribute("contentPage", "/WEB-INF/views/admin/user_edit.jsp");
            request.getRequestDispatcher("/WEB-INF/views/admin/admin_layout.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的用户ID");
        } catch (SQLException e) {
            logUtil.logError("ADMIN_ERROR", "获取用户数据失败: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取用户数据失败");
        }
    }

    // 更新用户信息
    private void updateUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 验证用户是否登录且是管理员
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Map<String, Object> userInfo = (Map<String, Object>) session.getAttribute("user");
        Boolean isAdmin = (Boolean) userInfo.get("isAdmin");
        String adminUsername = (String) userInfo.get("username");

        if (isAdmin == null || !isAdmin) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        logUtil.log("USER_UPDATE", adminUsername, "开始处理用户信息更新请求");

        try {
            // 请求参数记录
            logUtil.log("USER_UPDATE", adminUsername, "请求内容类型: " + request.getContentType());

            // 获取请求参数
            String userIdParam = request.getParameter("userId");
            logUtil.log("USER_UPDATE", adminUsername, "请求参数userId: " + userIdParam);

            if (userIdParam == null || userIdParam.trim().isEmpty()) {
                logUtil.logError("USER_UPDATE_ERROR", "缺少用户ID参数");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "缺少用户ID参数");
                return;
            }

            int userId = Integer.parseInt(userIdParam);
            User existingUser = userDao.findById(userId);

            if (existingUser == null) {
                logUtil.logError("USER_UPDATE_ERROR", "找不到指定ID的用户: " + userId);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "找不到指定ID的用户");
                return;
            }

            logUtil.log("USER_UPDATE", adminUsername, "成功获取用户信息，ID: " + userId + ", 用户名: " + existingUser.getUsername());

            // 检查是否试图修改系统管理员账号
            if (userId < 5 && !((Integer) userInfo.get("id")).equals(userId)) {
                logUtil.logError("USER_UPDATE_ERROR", "尝试修改其他管理员账号: " + userId);
                request.setAttribute("message", "无法修改其他管理员账号");
                request.setAttribute("messageType", "error");
                showUserEditForm(request, response);
                return;
            }

            // 获取并更新用户信息
            String username = request.getParameter("username");
            String email = request.getParameter("email");

            logUtil.log("USER_UPDATE", adminUsername, "请求参数username: " + username + ", email: " + email);

            // 进行基本的数据验证
            if (username == null || username.trim().isEmpty() || email == null || email.trim().isEmpty()) {
                logUtil.logError("USER_UPDATE_ERROR", "用户名或邮箱为空，username: " + username + ", email: " + email);
                request.setAttribute("message", "用户名和邮箱不能为空");
                request.setAttribute("messageType", "error");
                showUserEditForm(request, response);
                return;
            }

            // 更新用户对象
            existingUser.setUsername(username.trim());
            existingUser.setEmail(email.trim());

            // 处理头像上传
            try {
                Part avatarPart = request.getPart("avatar");
                logUtil.log("USER_UPDATE", adminUsername, "头像部分获取结果: " + (avatarPart != null ? "成功" : "失败"));

                if (avatarPart != null && avatarPart.getSize() > 0) {
                    logUtil.log("USER_UPDATE", adminUsername, "头像大小: " + avatarPart.getSize() + ", 内容类型: " + avatarPart.getContentType());

                    // 使用AvatarUtil保存头像
                    String newAvatarPath = AvatarUtil.saveAvatar(userId, avatarPart);
                    if (newAvatarPath != null) {
                        // 成功保存头像，更新用户头像URL
                        existingUser.setAvatarUrl(newAvatarPath);
                        logUtil.log("USER_UPDATE", adminUsername, "成功更新用户头像: " + newAvatarPath);

                        // 更新session中的头像更新时间戳
                        long currentTime = System.currentTimeMillis();
                        session.setAttribute("lastAvatarUpdate", currentTime);
                    } else {
                        logUtil.logError("USER_UPDATE_ERROR", "保存头像失败");
                        request.setAttribute("message", "头像上传失败，请稍后重试");
                        request.setAttribute("messageType", "error");
                        showUserEditForm(request, response);
                        return;
                    }
                } else {
                    logUtil.log("USER_UPDATE", adminUsername, "没有新的头像上传");
                }
            } catch (Exception e) {
                logUtil.logError("USER_UPDATE_ERROR", "处理头像上传时出错: " + e.getMessage() + "\n" + getStackTrace(e));
                // 继续更新其他信息，不因头像上传失败中断整个流程
            }

            // 更新用户信息
            userDao.update(existingUser);
            logUtil.log("USER_UPDATE", adminUsername, "成功更新用户基本信息，ID: " + userId);

            // 重定向到用户管理页面，显示成功消息
            response.sendRedirect(request.getContextPath() + "/admin/users?message=用户信息已成功更新&messageType=success");
            logUtil.log("USER_UPDATE", adminUsername, "用户更新完成，已重定向到用户管理页面");
        } catch (NumberFormatException e) {
            logUtil.logError("USER_UPDATE_ERROR", "无效的用户ID: " + e.getMessage() + "\n" + getStackTrace(e));
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的用户ID");
        } catch (SQLException e) {
            logUtil.logError("USER_UPDATE_ERROR", "数据库操作失败: " + e.getMessage() + "\n" + getStackTrace(e));
            request.setAttribute("message", "更新用户信息失败: 数据库错误");
            request.setAttribute("messageType", "error");
            try {
                int userId = Integer.parseInt(request.getParameter("userId"));
                User user = null;
                try {
                    user = userDao.findById(userId);
                } catch (SQLException ex) {
                    logUtil.logError("USER_UPDATE_ERROR", "在错误处理过程中获取用户数据失败: " + ex.getMessage());
                }
                request.setAttribute("editUser", user);
                request.setAttribute("activeTab", "users");
                request.setAttribute("contentPage", "/WEB-INF/views/admin/user_edit.jsp");
                request.getRequestDispatcher("/WEB-INF/views/admin/admin_layout.jsp").forward(request, response);
            } catch (Exception ex) {
                logUtil.logError("USER_UPDATE_ERROR", "在错误处理过程中发生二次异常: " + ex.getMessage());
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "更新用户数据失败，请返回用户管理页面重试");
            }
        } catch (Exception e) {
            logUtil.logError("USER_UPDATE_ERROR", "未知错误: " + e.getMessage() + "\n" + getStackTrace(e));
            request.setAttribute("message", "更新用户信息失败: " + e.getMessage());
            request.setAttribute("messageType", "error");
            try {
                int userId = Integer.parseInt(request.getParameter("userId"));
                User user = null;
                try {
                    user = userDao.findById(userId);
                } catch (SQLException ex) {
                    logUtil.logError("USER_UPDATE_ERROR", "在错误恢复过程中获取用户数据失败: " + ex.getMessage());
                }
                request.setAttribute("editUser", user);
                request.setAttribute("activeTab", "users");
                request.setAttribute("contentPage", "/WEB-INF/views/admin/user_edit.jsp");
                request.getRequestDispatcher("/WEB-INF/views/admin/admin_layout.jsp").forward(request, response);
            } catch (Exception ex) {
                // 如果转发也失败，则直接返回错误
                logUtil.logError("USER_UPDATE_ERROR", "在错误处理过程中发生二次异常: " + ex.getMessage());
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "更新用户数据失败，请返回用户管理页面重试");
            }
        }
    }
}