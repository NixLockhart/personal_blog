package com.blog.servlet;

import com.blog.dao.CommentDao;
import com.blog.dao.PostDao;
import com.blog.dao.impl.CommentDaoImpl;
import com.blog.dao.impl.PostDaoImpl;
import com.blog.model.Comment;
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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/comments/*")
public class CommentServlet extends HttpServlet {
    private final CommentDao commentDao = new CommentDaoImpl();
    private final PostDao postDao = new PostDaoImpl();
    private final NotificationService notificationService = new NotificationServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LogUtil logUtil = LogUtil.getInstance();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Object userObj = session.getAttribute("user");
        User user = null;

        if (userObj instanceof Map) {
            Map<String, Object> userMap = (Map<String, Object>) userObj;
            user = new User();
            user.setId((Integer) userMap.get("id"));
            user.setUsername((String) userMap.get("username"));
        } else if (userObj instanceof User) {
            user = (User) userObj;
        }

        if (user == null) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
            return;
        }

        String postIdStr = request.getParameter("postId");
        String content = request.getParameter("content");
        String parentIdStr = request.getParameter("parentId");
        String replyToUserIdStr = request.getParameter("replyToUserId");

        if (postIdStr == null || content == null || content.trim().isEmpty()) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "参数错误");
            return;
        }

        try {
            int postId = Integer.parseInt(postIdStr);
            Comment comment = new Comment();
            comment.setPostId(postId);
            comment.setUserId(user.getId());
            comment.setContent(content);

            // 处理回复评论的情况
            if (parentIdStr != null && !parentIdStr.trim().isEmpty()) {
                int parentId = Integer.parseInt(parentIdStr);
                comment.setParentId(parentId);

                // 获取被回复的评论
                Comment parentComment = commentDao.findById(parentId);
                if (parentComment == null) {
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "被回复的评论不存在");
                    return;
                }
            }

            // 处理回复用户的情况
            if (replyToUserIdStr != null && !replyToUserIdStr.trim().isEmpty()) {
                int replyToUserId = Integer.parseInt(replyToUserIdStr);
                comment.setReplyToUserId(replyToUserId);
            }

            int commentId = commentDao.insert(comment);

            // 发送评论通知给文章作者
            try {
                Post post = postDao.findById(postId);
                Integer authorId = post.getUserId();

                // 如果是回复评论，则还要给被回复的用户发通知（如果不是回复自己的评论）
                if (comment.getReplyToUserId() != null && !comment.getReplyToUserId().equals(user.getId())) {
                    notificationService.createReplyNotification(
                            comment.getReplyToUserId(), // 接收通知的用户
                            user.getId(),               // 发送通知的用户
                            postId,                     // 文章ID
                            comment.getId(),            // 评论ID
                            content                     // 评论内容
                    );
                    logUtil.log("NOTIFICATION", "user_" + user.getId(), "向用户" + comment.getReplyToUserId() + "发送回复通知");
                }
                // 如果不是自己的文章，则给文章作者发通知
                else if (authorId != null && !authorId.equals(user.getId())) {
                    notificationService.createCommentNotification(
                            authorId,                   // 接收通知的用户
                            user.getId(),               // 发送通知的用户
                            postId,                     // 文章ID
                            comment.getId(),            // 评论ID
                            content                     // 评论内容
                    );
                    logUtil.log("NOTIFICATION", "user_" + user.getId(), "向用户" + authorId + "发送评论通知");
                }
            } catch (Exception e) {
                logUtil.logError("NOTIFICATION_ERROR", "发送评论/回复通知失败: " + e.getMessage());
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "评论成功");
            sendJsonResponse(response, result);
        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "参数格式错误");
        } catch (SQLException e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "服务器错误");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String postIdStr = request.getParameter("postId");
        if (postIdStr == null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "参数错误");
            return;
        }

        try {
            int postId = Integer.parseInt(postIdStr);
            List<Comment> comments = commentDao.findByPostId(postId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", comments);
            sendJsonResponse(response, result);
        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "文章ID格式错误");
        } catch (SQLException e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "服务器错误");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 获取评论ID
        String commentIdStr = request.getParameter("commentId");
        if (commentIdStr == null || commentIdStr.trim().isEmpty()) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "评论ID不能为空");
            return;
        }

        // 获取当前登录用户
        HttpSession session = request.getSession();
        Object userObj = session.getAttribute("user");
        if (userObj == null) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
            return;
        }

        try {
            int commentId = Integer.parseInt(commentIdStr);

            // 获取评论信息
            Comment comment = commentDao.findById(commentId);
            if (comment == null) {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "评论不存在");
                return;
            }

            // 检查权限：只有评论作者或ID小于5的用户（管理员）可以删除评论
            Integer currentUserId = null;
            if (userObj instanceof Map) {
                Map<String, Object> userMap = (Map<String, Object>) userObj;
                currentUserId = (Integer) userMap.get("id");
            } else if (userObj instanceof User) {
                currentUserId = ((User) userObj).getId();
            }

            if (currentUserId == null) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "无法获取用户信息");
                return;
            }

            // 检查是否是评论作者或管理员
            if (comment.getUserId() != currentUserId && currentUserId >= 5) {
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "没有权限删除此评论");
                return;
            }

            // 执行删除操作
            int result = commentDao.deleteById(commentId);
            if (result > 0) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("message", "评论已删除");
                sendJsonResponse(response, responseData);
                logUtil.log("COMMENT_DELETE", "user_" + currentUserId, "删除评论ID: " + commentId);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "删除评论失败");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "评论ID格式错误");
        } catch (SQLException e) {
            logUtil.logError("COMMENT_DELETE_ERROR", "删除评论失败: " + e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "删除评论失败：" + e.getMessage());
        }
    }

    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), data);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        sendJsonResponse(response, result);
    }
}