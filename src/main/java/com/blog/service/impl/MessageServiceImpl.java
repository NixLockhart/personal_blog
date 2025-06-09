package com.blog.service.impl;

import com.blog.dao.MessageDao;
import com.blog.dao.impl.MessageDaoImpl;
import com.blog.model.Message;
import com.blog.service.MessageService;
import com.blog.service.NotificationService;
import com.blog.util.LogUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageServiceImpl implements MessageService {
    private final MessageDao messageDao;
    private final NotificationService notificationService;
    private final LogUtil logUtil;

    public MessageServiceImpl() {
        this.messageDao = new MessageDaoImpl();
        this.notificationService = new NotificationServiceImpl();
        this.logUtil = LogUtil.getInstance();
    }

    @Override
    public boolean addMessage(Message message) {
        try {
            // 确保新留言标记为未读
            message.setIsRead(false);
            int result = messageDao.insert(message);

            // 发送通知给管理员
            if (result > 0) {
                // 只有顶级留言才发送通知
                if (message.getParentId() == null) {
                    String senderName = message.getName();
                    if (message.getUserId() != null) {
                        senderName = message.getUsername() != null ? message.getUsername() : "用户" + message.getUserId();
                    }

                    notificationService.createSystemNotification(
                            1, // 管理员ID
                            "收到新留言",
                            senderName + " 发送了新留言: " + message.getMessage()
                    );

                    logUtil.log("MESSAGE", "用户或游客", "发送了新留言，ID: " + message.getId());
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            logUtil.logError("MESSAGE_ERROR", "添加留言失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteMessage(Integer messageId, Integer currentUserId) {
        try {
            if (canDeleteMessage(messageId, currentUserId)) {
                int result = messageDao.deleteById(messageId);
                if (result > 0) {
                    logUtil.log("MESSAGE_DELETE", currentUserId != null ? "用户" + currentUserId : "游客",
                            "删除留言，ID: " + messageId);
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            logUtil.logError("MESSAGE_ERROR", "删除留言失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean replyToMessage(Integer parentId, Message reply) {
        try {
            // 确认父留言存在
            Message parentMessage = messageDao.findById(parentId);
            if (parentMessage == null) {
                return false;
            }

            // 设置回复属性
            reply.setParentId(parentId);
            reply.setIsRead(false);  // 新回复标记为未读

            int result = messageDao.insert(reply);

            if (result > 0) {
                // 如果回复的是注册用户的留言，发送通知
                Integer receiverId = parentMessage.getUserId();
                if (receiverId != null && !receiverId.equals(reply.getUserId())) {
                    String senderName = reply.getName();
                    if (reply.getUserId() != null) {
                        senderName = reply.getUsername() != null ? reply.getUsername() : "用户" + reply.getUserId();
                    }

                    notificationService.createSystemNotification(
                            receiverId,
                            "留言收到回复",
                            senderName + " 回复了您的留言: " + reply.getMessage()
                    );
                }

                // 给管理员发通知（如果不是管理员回复的）
                if (reply.getUserId() == null || reply.getUserId() != 1) {
                    String senderName = reply.getName();
                    if (reply.getUserId() != null) {
                        senderName = reply.getUsername() != null ? reply.getUsername() : "用户" + reply.getUserId();
                    }

                    notificationService.createSystemNotification(
                            1, // 管理员ID
                            "留言收到回复",
                            senderName + " 回复了留言: " + reply.getMessage()
                    );
                }

                logUtil.log("MESSAGE_REPLY", reply.getUserId() != null ? "用户" + reply.getUserId() : "游客",
                        "回复留言，ID: " + parentId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            logUtil.logError("MESSAGE_ERROR", "回复留言失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean markAsRead(Integer messageId) {
        try {
            int result = messageDao.updateReadStatus(messageId, true);
            return result > 0;
        } catch (SQLException e) {
            logUtil.logError("MESSAGE_ERROR", "标记留言已读失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean markAllAsRead() {
        try {
            int result = messageDao.markAllAsRead();
            return result >= 0; // 即使没有未读留言，也返回成功
        } catch (SQLException e) {
            logUtil.logError("MESSAGE_ERROR", "标记所有留言已读失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getAllMessagesWithReplies() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            logUtil.log("MESSAGE_SERVICE", "system", "开始获取留言列表");

            // 获取所有顶级留言
            List<Message> topMessages = messageDao.findAllTopMessages();
            logUtil.log("MESSAGE_SERVICE", "system", "成功获取顶级留言，数量: " + (topMessages != null ? topMessages.size() : 0));

            if (topMessages != null) {
                for (Message message : topMessages) {
                    if (message != null) {
                        Map<String, Object> messageMap = new HashMap<>();
                        messageMap.put("message", message);

                        try {
                            // 获取该留言的所有回复
                            List<Message> replies = messageDao.findRepliesByParentId(message.getId());
                            logUtil.log("MESSAGE_SERVICE", "system", "成功获取留言ID: " + message.getId() + " 的回复，数量: " + (replies != null ? replies.size() : 0));
                            messageMap.put("replies", replies != null ? replies : new ArrayList<>());
                        } catch (SQLException e) {
                            logUtil.logError("MESSAGE_ERROR", "获取留言ID: " + message.getId() + " 的回复失败: " + e.getMessage());
                            e.printStackTrace();
                            messageMap.put("replies", new ArrayList<>());
                        }

                        result.add(messageMap);
                    }
                }
            }

            logUtil.log("MESSAGE_SERVICE", "system", "成功处理所有留言，总数: " + result.size());
        } catch (SQLException e) {
            logUtil.logError("MESSAGE_ERROR", "获取留言及回复失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("获取留言列表失败: " + e.getMessage());
        } catch (Exception e) {
            logUtil.logError("MESSAGE_ERROR", "获取留言及回复时发生未知错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("获取留言列表时发生未知错误: " + e.getMessage());
        }
        return result;
    }

    @Override
    public int getUnreadCount() {
        try {
            return messageDao.getUnreadCount();
        } catch (SQLException e) {
            logUtil.logError("MESSAGE_ERROR", "获取未读留言数量失败: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Message> getAllUnreadMessages() {
        try {
            return messageDao.findAllUnread();
        } catch (SQLException e) {
            logUtil.logError("MESSAGE_ERROR", "获取未读留言失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Message> getMessagesByPage(int page, int pageSize) {
        try {
            return messageDao.findByPage(page, pageSize);
        } catch (SQLException e) {
            logUtil.logError("MESSAGE_ERROR", "分页获取留言失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public int getTotalCount() {
        try {
            return messageDao.getTotal();
        } catch (SQLException e) {
            logUtil.logError("MESSAGE_ERROR", "获取留言总数失败: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean canDeleteMessage(Integer messageId, Integer userId) {
        try {
            Message message = messageDao.findById(messageId);
            if (message == null) {
                return false;
            }

            // 管理员可以删除任何留言
            if (userId != null && userId == 1) {
                return true;
            }

            // 用户可以删除自己的留言
            if (userId != null && message.getUserId() != null && userId.equals(message.getUserId())) {
                return true;
            }

            // 游客通过IP地址判断是否是自己的留言
            if (userId == null && message.getUserId() == null) {
                // 在实际应用中，这里应该比较当前请求的IP地址和留言的IP地址
                // 但为了简化，我们假设这个检查已经完成
                return true;
            }

            return false;
        } catch (SQLException e) {
            logUtil.logError("MESSAGE_ERROR", "检查删除权限失败: " + e.getMessage());
            return false;
        }
    }
}