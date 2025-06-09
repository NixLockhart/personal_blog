package com.blog.service;

import com.blog.model.Notification;
import java.util.List;

public interface NotificationService {
    /**
     * 创建通知
     * @param notification 通知对象
     * @return 是否成功
     */
    boolean createNotification(Notification notification);

    /**
     * 创建系统通知
     * @param userId 用户ID
     * @param title 标题
     * @param content 内容
     * @return 是否成功
     */
    boolean createSystemNotification(Integer userId, String title, String content);

    /**
     * 创建公告通知（发送给所有用户）
     * @param title 标题
     * @param content 内容
     * @return 是否成功
     */
    boolean createAnnouncementForAllUsers(String title, String content);

    /**
     * 创建点赞通知
     * @param toUserId 接收通知的用户ID
     * @param fromUserId 点赞用户ID
     * @param postId 文章ID
     * @return 是否成功
     */
    boolean createLikeNotification(Integer toUserId, Integer fromUserId, Integer postId);

    /**
     * 创建评论通知
     * @param toUserId 接收通知的用户ID
     * @param fromUserId 评论用户ID
     * @param postId 文章ID
     * @param commentId 评论ID
     * @param commentContent 评论内容
     * @return 是否成功
     */
    boolean createCommentNotification(Integer toUserId, Integer fromUserId, Integer postId, Integer commentId, String commentContent);

    /**
     * 创建回复通知
     * @param toUserId 接收通知的用户ID
     * @param fromUserId 回复用户ID
     * @param postId 文章ID
     * @param commentId 评论ID
     * @param replyContent 回复内容
     * @return 是否成功
     */
    boolean createReplyNotification(Integer toUserId, Integer fromUserId, Integer postId, Integer commentId, String replyContent);

    /**
     * 根据ID获取通知
     * @param id 通知ID
     * @return 通知对象，如果不存在则返回null
     */
    Notification getNotificationById(Integer id);

    /**
     * 将通知标记为已读
     * @param notificationId 通知ID
     * @return 是否成功
     */
    boolean markAsRead(Integer notificationId);

    /**
     * 将用户所有通知标记为已读
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAllAsRead(Integer userId);

    /**
     * 获取用户的所有通知
     * @param userId 用户ID
     * @return 通知列表
     */
    List<Notification> getUserNotifications(Integer userId);

    /**
     * 获取用户的未读通知
     * @param userId 用户ID
     * @return 未读通知列表
     */
    List<Notification> getUserUnreadNotifications(Integer userId);

    /**
     * 获取用户特定类型的通知
     * @param userId 用户ID
     * @param type 通知类型
     * @return 通知列表
     */
    List<Notification> getUserNotificationsByType(Integer userId, String type);

    /**
     * 获取用户未读通知数量
     * @param userId 用户ID
     * @return 未读通知数量
     */
    int getUnreadNotificationCount(Integer userId);

    /**
     * 删除通知
     * @param notificationId 通知ID
     * @return 是否成功
     */
    boolean deleteNotification(Integer notificationId);

    /**
     * 删除用户所有通知
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteAllUserNotifications(Integer userId);
}