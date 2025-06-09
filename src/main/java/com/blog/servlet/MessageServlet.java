package com.blog.servlet;

import com.blog.model.Message;
import com.blog.model.User;
import com.blog.service.MessageService;
import com.blog.service.impl.MessageServiceImpl;
import com.blog.util.LogUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageServlet extends HttpServlet {
    private final MessageService messageService = new MessageServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LogUtil logUtil = LogUtil.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            logUtil.log("MESSAGE_REQUEST", "system", "收到请求，pathInfo: " + pathInfo);

            // 设置响应类型为JSON
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            // 如果是访问留言板主页，显示页面
            if (pathInfo == null || pathInfo.equals("/")) {
                logUtil.log("MESSAGE_REQUEST", "system", "转发到留言板页面");
                request.getRequestDispatcher("/WEB-INF/views/messages.jsp").forward(request, response);
                return;
            }

            // 如果是API请求，返回JSON数据
            try {
                logUtil.log("MESSAGE_REQUEST", "system", "开始获取留言列表");
                // 获取留言列表，包括回复
                List<Map<String, Object>> messagesWithReplies = messageService.getAllMessagesWithReplies();
                logUtil.log("MESSAGE_REQUEST", "system", "成功获取留言列表，数量: " + messagesWithReplies.size());

                // 设置当前用户权限
                Integer currentUserId = getCurrentUserId(request);
                logUtil.log("MESSAGE_REQUEST", "system", "当前用户ID: " + currentUserId);

                // 处理留言数据
                for (Map<String, Object> messageData : messagesWithReplies) {
                    Message message = (Message) messageData.get("message");
                    if (message != null) {
                        // 确保留言内容不为空
                        if (message.getMessage() == null) {
                            message.setMessage("");
                        }

                        // 设置删除权限
                        message.setCanDelete(messageService.canDeleteMessage(message.getId(), currentUserId));

                        @SuppressWarnings("unchecked")
                        List<Message> replies = (List<Message>) messageData.get("replies");
                        if (replies != null) {
                            for (Message reply : replies) {
                                if (reply != null) {
                                    // 确保回复内容不为空
                                    if (reply.getMessage() == null) {
                                        reply.setMessage("");
                                    }

                                    // 设置删除权限
                                    reply.setCanDelete(messageService.canDeleteMessage(reply.getId(), currentUserId));
                                }
                            }
                        }
                    }
                }

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("data", messagesWithReplies);

                logUtil.log("MESSAGE_REQUEST", "system", "准备发送JSON响应");
                sendJsonResponse(response, result);
                logUtil.log("MESSAGE_REQUEST", "system", "JSON响应发送成功"+"JSON内容："+objectMapper.writeValueAsString(result));
            } catch (Exception e) {
                logUtil.logError("MESSAGE_ERROR", "获取留言列表失败: " + e.getMessage());
                e.printStackTrace();

                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "获取留言列表失败: " + e.getMessage());
                errorResult.put("error", e.getMessage());

                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                sendJsonResponse(response, errorResult);
            }
        } catch (Exception e) {
            logUtil.logError("MESSAGE_ERROR", "处理请求失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "服务器内部错误: " + e.getMessage());
            errorResult.put("error", e.getMessage());

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            sendJsonResponse(response, errorResult);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // 获取请求参数
            String content = request.getParameter("content");
            String parentIdStr = request.getParameter("parentId");

            if (content == null || content.trim().isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "留言内容不能为空");
                return;
            }

            // 创建留言对象
            Message message = new Message();
            message.setMessage(content.trim());

            // 设置留言用户信息
            HttpSession session = request.getSession();
            User user = getUserFromSession(session);

            if (user != null) {
                // 已登录用户留言
                message.setUserId(user.getId());
                message.setName(user.getUsername());
                message.setEmail(user.getEmail());
            } else {
                // 游客留言
                String name = request.getParameter("name");
                String email = request.getParameter("email");

                if (name == null || name.trim().isEmpty()) {
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "名称不能为空");
                    return;
                }

                if (email == null || email.trim().isEmpty()) {
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "邮箱不能为空");
                    return;
                }

                message.setName(name.trim());
                message.setEmail(email.trim());
            }

            // 保存IP地址
            message.setIpAddress(getClientIpAddress(request));

            boolean success;
            if (parentIdStr != null && !parentIdStr.trim().isEmpty()) {
                // 回复留言
                try {
                    int parentId = Integer.parseInt(parentIdStr);
                    success = messageService.replyToMessage(parentId, message);
                } catch (NumberFormatException e) {
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "回复ID格式错误");
                    return;
                }
            } else {
                // 新留言
                success = messageService.addMessage(message);
            }

            if (success) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "留言发送成功");
                sendJsonResponse(response, result);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "留言发送失败");
            }
        } catch (Exception e) {
            // 记录错误日志
            logUtil.logError("MESSAGES_ERROR", "发送留言失败: " + e.getMessage());
            e.printStackTrace();

            // 返回错误信息
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "服务器内部错误，请稍后重试");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // 获取留言ID
            String messageIdStr = request.getParameter("messageId");
            if (messageIdStr == null || messageIdStr.trim().isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "留言ID不能为空");
                return;
            }

            try {
                int messageId = Integer.parseInt(messageIdStr);
                Integer currentUserId = getCurrentUserId(request);

                boolean success = messageService.deleteMessage(messageId, currentUserId);
                if (success) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("message", "留言删除成功");
                    sendJsonResponse(response, result);
                } else {
                    sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "无权删除此留言");
                }
            } catch (NumberFormatException e) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "留言ID格式错误");
            }
        } catch (Exception e) {
            // 记录错误日志
            logUtil.logError("MESSAGES_ERROR", "删除留言失败: " + e.getMessage());
            e.printStackTrace();

            // 返回错误信息
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "服务器内部错误，请稍后重试");
        }
    }

    private Integer getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession();
        User user = getUserFromSession(session);
        return user != null ? user.getId() : null;
    }

    private User getUserFromSession(HttpSession session) {
        Object userObj = session.getAttribute("user");
        if (userObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> userMap = (Map<String, Object>) userObj;
            User user = new User();
            user.setId((Integer) userMap.get("id"));
            user.setUsername((String) userMap.get("username"));
            user.setEmail((String) userMap.get("email"));
            return user;
        } else if (userObj instanceof User) {
            return (User) userObj;
        }
        return null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // 多个代理的情况，取第一个IP地址
        if (ipAddress != null && ipAddress.length() > 15 && ipAddress.indexOf(",") > 0) {
            ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
        }

        return ipAddress;
    }

    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), data);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        sendJsonResponse(response, errorResponse);
    }
} 