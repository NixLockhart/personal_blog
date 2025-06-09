package com.blog.servlet;

import com.blog.dao.PostDao;
import com.blog.dao.impl.PostDaoImpl;
import com.blog.model.Post;
import com.blog.model.User;
import com.blog.service.NotificationService;
import com.blog.service.impl.NotificationServiceImpl;
import com.blog.util.LogUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理文章点赞和浏览量的Servlet
 */
@WebServlet("/api/posts/engagement/*")
public class PostEngagementServlet extends HttpServlet {
    private PostDao postDao;
    private final NotificationService notificationService = new NotificationServiceImpl();
    private final LogUtil logUtil = LogUtil.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        postDao = new PostDaoImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "缺少文章ID");
            return;
        }

        try {
            // 从路径中提取帖子ID
            int postId = Integer.parseInt(pathInfo.substring(1));
            String action = request.getParameter("action");

            if (action == null) {
                // 如果没有指定动作，返回当前的点赞和浏览量以及用户点赞状态
                int likes = postDao.getLikesCount(postId);
                int views = postDao.getViewsCount(postId);

                // 检查用户是否已点赞
                boolean hasLiked = false;
                HttpSession session = request.getSession(false);
                if (session != null) {
                    Object userObj = session.getAttribute("user");
                    if (userObj != null) {
                        Integer userId;
                        if (userObj instanceof Map) {
                            Map<String, Object> userMap = (Map<String, Object>) userObj;
                            userId = (Integer) userMap.get("id");
                        } else {
                            userId = ((User) userObj).getId();
                        }

                        if (userId != null) {
                            hasLiked = postDao.hasUserLiked(postId, userId);
                        }
                    }
                }

                sendJsonResponseWithLikeStatus(response, likes, views, hasLiked);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的操作");
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的文章ID");
        } catch (SQLException e) {
            logUtil.log("POST_ENGAGEMENT_ERROR", "system", "数据库访问错误: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "数据库错误");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "缺少文章ID");
            return;
        }

        try {
            // 从路径中提取帖子ID
            int postId = Integer.parseInt(pathInfo.substring(1));
            String action = request.getParameter("action");

            if (action == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "缺少action参数");
                return;
            }

            int likes = 0;
            int views = 0;

            switch (action) {
                case "like":
                    // 检查用户是否登录
                    HttpSession session = request.getSession(false);
                    logUtil.log("POST_ENGAGEMENT_DEBUG", "system", "点赞请求 - 会话ID: " + (session != null ? session.getId() : "null"));

                    if (session == null) {
                        logUtil.log("POST_ENGAGEMENT_DEBUG", "system", "点赞请求 - 会话为空");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        sendErrorResponse(response, "请先登录");
                        return;
                    }

                    // 获取用户信息
                    Object userObj = session.getAttribute("user");
                    logUtil.log("POST_ENGAGEMENT_DEBUG", "system", "点赞请求 - 用户对象: " + (userObj != null ? userObj.toString() : "null"));

                    if (userObj == null) {
                        logUtil.log("POST_ENGAGEMENT_DEBUG", "system", "点赞请求 - 用户对象为空");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        sendErrorResponse(response, "请先登录");
                        return;
                    }

                    // 从用户信息中获取用户ID
                    Integer userId;
                    if (userObj instanceof Map) {
                        Map<String, Object> userMap = (Map<String, Object>) userObj;
                        userId = (Integer) userMap.get("id");
                    } else {
                        userId = ((User) userObj).getId();
                    }

                    if (userId == null) {
                        logUtil.log("POST_ENGAGEMENT_DEBUG", "system", "点赞请求 - 用户ID为空");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        sendErrorResponse(response, "请先登录");
                        return;
                    }

                    // 检查是否已经点赞
                    if (postDao.hasUserLiked(postId, userId)) {
                        sendErrorResponse(response, "您已经点赞过这篇文章了");
                        return;
                    }

                    // 记录点赞
                    postDao.recordLike(postId, userId);
                    postDao.incrementLikes(postId);
                    logUtil.log("POST_LIKE", "user_" + userId, "文章ID " + postId + " 增加点赞数");

                    // 查询文章作者ID，发送点赞通知
                    try {
                        Post post = postDao.findById(postId);
                        Integer authorId = post.getUserId();
                        if (authorId != null && !authorId.equals(userId)) { // 不向自己发送通知
                            notificationService.createLikeNotification(authorId, userId, postId);
                            logUtil.log("NOTIFICATION", "user_" + userId, "向用户" + authorId + "发送点赞通知");
                        }
                    } catch (Exception e) {
                        logUtil.logError("NOTIFICATION_ERROR", "发送点赞通知失败: " + e.getMessage());
                    }

                    likes = postDao.getLikesCount(postId);
                    views = postDao.getViewsCount(postId);
                    break;

                case "unlike":
                    // 检查用户是否登录
                    session = request.getSession(false);
                    if (session == null) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        sendErrorResponse(response, "请先登录");
                        return;
                    }

                    // 获取用户信息
                    userObj = session.getAttribute("user");
                    if (userObj == null) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        sendErrorResponse(response, "请先登录");
                        return;
                    }

                    // 从用户信息中获取用户ID
                    if (userObj instanceof Map) {
                        Map<String, Object> userMap = (Map<String, Object>) userObj;
                        userId = (Integer) userMap.get("id");
                    } else {
                        userId = ((User) userObj).getId();
                    }

                    if (userId == null) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        sendErrorResponse(response, "请先登录");
                        return;
                    }

                    // 检查是否已经点赞
                    if (!postDao.hasUserLiked(postId, userId)) {
                        sendErrorResponse(response, "您还没有点赞这篇文章");
                        return;
                    }

                    // 取消点赞
                    postDao.removeLike(postId, userId);
                    postDao.decrementLikes(postId);
                    logUtil.log("POST_UNLIKE", "user_" + userId, "文章ID " + postId + " 减少点赞数");
                    likes = postDao.getLikesCount(postId);
                    views = postDao.getViewsCount(postId);
                    break;

                case "view":
                    // 获取设备ID（使用User-Agent作为设备标识）
                    String deviceId = request.getHeader("User-Agent");
                    if (deviceId == null || deviceId.isEmpty()) {
                        deviceId = "unknown_device";
                    }

                    logUtil.log("POST_VIEW_DEBUG", "system", "浏览请求 - 设备ID: " + deviceId);

                    // 检查今天是否已经浏览过
                    if (!postDao.hasDeviceViewedToday(postId, deviceId)) {
                        // 记录浏览
                        postDao.recordView(postId, deviceId);
                        postDao.incrementViews(postId);
                        logUtil.log("POST_VIEW", "device_" + deviceId.hashCode(), "文章ID " + postId + " 增加浏览量");
                    } else {
                        logUtil.log("POST_VIEW", "device_" + deviceId.hashCode(), "文章ID " + postId + " 今日已浏览过，不增加浏览量");
                    }
                    likes = postDao.getLikesCount(postId);
                    views = postDao.getViewsCount(postId);
                    break;

                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的action参数");
                    return;
            }
            sendJsonResponse(response, likes, views);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的文章ID");
        } catch (SQLException e) {
            logUtil.log("POST_ENGAGEMENT_ERROR", "system", "数据库访问错误(doPost): " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "数据库错误");
        }
    }

    /**
     * 发送JSON格式的响应
     */
    private void sendJsonResponse(HttpServletResponse response, int likes, int views) throws IOException {
        sendJsonResponseWithLikeStatus(response, likes, views, false);
    }

    /**
     * 发送包含点赞状态的JSON格式响应
     */
    private void sendJsonResponseWithLikeStatus(HttpServletResponse response, int likes, int views, boolean hasLiked) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("likes", likes);
        responseData.put("views", views);
        responseData.put("hasLiked", hasLiked);

        PrintWriter out = response.getWriter();
        out.print(objectMapper.writeValueAsString(responseData));
        out.flush();
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("error", message);

        PrintWriter out = response.getWriter();
        out.print(objectMapper.writeValueAsString(responseData));
        out.flush();
    }
}