package com.blog.controller;

import com.blog.model.Notification;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "NotificationController", urlPatterns = {
        "/notifications",
        "/notifications/count",
        "/notifications/read/*",
        "/notifications/read-all",
        "/notifications/delete/*",
        "/notifications/delete-all"
})
public class NotificationController extends HttpServlet {
    private final NotificationService notificationService = new NotificationServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LogUtil logUtil = LogUtil.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();

        // 检查用户是否登录
        HttpSession session = request.getSession();
        Map<String, Object> userInfo = (Map<String, Object>) session.getAttribute("user");
        if (userInfo == null) {
            // 返回JSON响应而不是重定向
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "用户未登录");
            result.put("count", 0);
            sendJsonResponse(response, result);
            return;
        }

        Integer userId = (Integer) userInfo.get("id");

        if (uri.matches(".*?/notifications/count")) {
            // 获取未读通知数
            handleGetUnreadCount(request, response, userInfo);
        } else if (uri.matches(".*?/notifications")) {
            // 获取用户通知列表
            handleGetNotifications(request, response, userInfo);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();

        // 检查用户是否登录
        HttpSession session = request.getSession();
        Map<String, Object> userInfo = (Map<String, Object>) session.getAttribute("user");
        if (userInfo == null) {
            // 返回JSON响应而不是重定向
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "用户未登录");
            sendJsonResponse(response, result);
            return;
        }

        Integer userId = (Integer) userInfo.get("id");

        if (uri.matches(".*?/notifications/read-all")) {
            // 标记所有为已读
            handleMarkAllAsRead(request, response, userInfo);
        } else if (uri.matches(".*?/notifications/read/\\d+")) {
            // 标记单条为已读
            handleMarkAsRead(request, response, userInfo, uri);
        } else if (uri.matches(".*?/notifications/delete-all")) {
            // 删除所有通知
            handleDeleteAll(request, response, userInfo);
        } else if (uri.matches(".*?/notifications/delete/\\d+")) {
            // 删除单条通知
            handleDeleteNotification(request, response, userInfo, uri);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * 获取用户的未读通知数量
     */
    private void handleGetUnreadCount(HttpServletRequest request, HttpServletResponse response, Map<String, Object> userInfo) throws IOException {
        int count = notificationService.getUnreadNotificationCount((Integer) userInfo.get("id"));

        Map<String, Object> result = new HashMap<>();
        result.put("count", count);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), result);
    }

    /**
     * 获取用户的通知列表
     */
    private void handleGetNotifications(HttpServletRequest request, HttpServletResponse response, Map<String, Object> userInfo) throws ServletException, IOException {
        String type = request.getParameter("type");
        boolean unreadOnly = Boolean.parseBoolean(request.getParameter("unreadOnly"));
        Integer userId = (Integer) userInfo.get("id");

        List<Notification> notifications;

        if (unreadOnly) {
            notifications = notificationService.getUserUnreadNotifications(userId);
        } else if (type != null && !type.isEmpty()) {
            notifications = notificationService.getUserNotificationsByType(userId, type);
        } else {
            notifications = notificationService.getUserNotifications(userId);
        }

        request.setAttribute("notifications", notifications);
        request.getRequestDispatcher("/WEB-INF/views/notifications.jsp").forward(request, response);
    }

    /**
     * 标记通知为已读
     */
    private void handleMarkAsRead(HttpServletRequest request, HttpServletResponse response, Map<String, Object> userInfo, String uri) throws IOException {
        try {
            int notificationId = extractIdFromUri(uri);
            Integer userId = (Integer) userInfo.get("id");
            String username = (String) userInfo.get("username");

            logUtil.log("NOTIFICATION_MARK_READ", username, "开始标记通知为已读 - 通知ID: " + notificationId);

            // 验证通知是否属于当前用户
            Notification notification = notificationService.getNotificationById(notificationId);
            if (notification == null) {
                logUtil.logError("NOTIFICATION_MARK_READ", "通知不存在 - ID: " + notificationId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "通知不存在");
                sendJsonResponse(response, result);
                return;
            }

            if (!notification.getUserId().equals(userId)) {
                logUtil.logError("NOTIFICATION_MARK_READ", "权限验证失败 - 通知用户ID: " + notification.getUserId() + ", 当前用户ID: " + userId);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "无权操作此通知");
                sendJsonResponse(response, result);
                return;
            }

            boolean success = notificationService.markAsRead(notificationId);
            logUtil.log("NOTIFICATION_MARK_READ", username, "标记已读操作完成 - 通知ID: " + notificationId + ", 结果: " + success);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (!success) {
                result.put("message", "标记已读失败");
            }
            sendJsonResponse(response, result);
        } catch (Exception e) {
            logUtil.logError("NOTIFICATION_MARK_READ", "标记已读时发生错误: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "服务器内部错误: " + e.getMessage());
            sendJsonResponse(response, result);
        }
    }

    /**
     * 标记所有通知为已读
     */
    private void handleMarkAllAsRead(HttpServletRequest request, HttpServletResponse response, Map<String, Object> userInfo) throws IOException {
        boolean success = notificationService.markAllAsRead((Integer) userInfo.get("id"));

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), result);
    }

    /**
     * 删除通知
     */
    private void handleDeleteNotification(HttpServletRequest request, HttpServletResponse response, Map<String, Object> userInfo, String uri) throws IOException {
        try {
            int notificationId = extractIdFromUri(uri);
            Integer userId = (Integer) userInfo.get("id");
            String username = (String) userInfo.get("username");

            logUtil.log("NOTIFICATION_DELETE", username, "开始删除通知 - 通知ID: " + notificationId);

            // 验证通知是否属于当前用户
            Notification notification = notificationService.getNotificationById(notificationId);
            if (notification == null) {
                logUtil.logError("NOTIFICATION_DELETE", "通知不存在 - ID: " + notificationId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "通知不存在");
                sendJsonResponse(response, result);
                return;
            }

            if (!notification.getUserId().equals(userId)) {
                logUtil.logError("NOTIFICATION_DELETE", "权限验证失败 - 通知用户ID: " + notification.getUserId() + ", 当前用户ID: " + userId);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "无权操作此通知");
                sendJsonResponse(response, result);
                return;
            }

            boolean success = notificationService.deleteNotification(notificationId);
            logUtil.log("NOTIFICATION_DELETE", username, "删除通知操作完成 - 通知ID: " + notificationId + ", 结果: " + success);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (!success) {
                result.put("message", "删除通知失败");
            }
            sendJsonResponse(response, result);
        } catch (Exception e) {
            logUtil.logError("NOTIFICATION_DELETE", "删除通知时发生错误: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "服务器内部错误: " + e.getMessage());
            sendJsonResponse(response, result);
        }
    }

    /**
     * 删除所有通知
     */
    private void handleDeleteAll(HttpServletRequest request, HttpServletResponse response, Map<String, Object> userInfo) throws IOException {
        boolean success = notificationService.deleteAllUserNotifications((Integer) userInfo.get("id"));

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), result);
    }

    /**
     * 从URI中提取ID
     */
    private int extractIdFromUri(String uri) {
        String idStr = uri.substring(uri.lastIndexOf('/') + 1);
        return Integer.parseInt(idStr);
    }

    /**
     * 发送JSON响应
     */
    private void sendJsonResponse(HttpServletResponse response, Map<String, Object> result) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), result);
    }
}