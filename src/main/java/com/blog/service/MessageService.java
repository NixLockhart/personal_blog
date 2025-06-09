package com.blog.service;

import com.blog.model.Message;
import java.util.List;
import java.util.Map;

public interface MessageService {
    /**
     * 添加留言
     * @param message 留言对象
     * @return 是否成功
     */
    boolean addMessage(Message message);

    /**
     * 删除留言
     * @param messageId 留言ID
     * @param currentUserId 当前用户ID，可为null表示游客
     * @return 是否成功
     */
    boolean deleteMessage(Integer messageId, Integer currentUserId);

    /**
     * 回复留言
     * @param parentId 父留言ID
     * @param message 回复内容
     * @return 是否成功
     */
    boolean replyToMessage(Integer parentId, Message message);

    /**
     * 标记留言为已读
     * @param messageId 留言ID
     * @return 是否成功
     */
    boolean markAsRead(Integer messageId);

    /**
     * 标记所有留言为已读
     * @return 是否成功
     */
    boolean markAllAsRead();

    /**
     * 获取所有顶级留言及其回复
     * @return 留言列表，包含回复
     */
    List<Map<String, Object>> getAllMessagesWithReplies();

    /**
     * 获取未读留言数量
     * @return 未读留言数量
     */
    int getUnreadCount();

    /**
     * 获取所有未读留言
     * @return 未读留言列表
     */
    List<Message> getAllUnreadMessages();

    /**
     * 分页获取所有留言
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页留言列表
     */
    List<Message> getMessagesByPage(int page, int pageSize);

    /**
     * 获取留言总数
     * @return 留言总数
     */
    int getTotalCount();

    /**
     * 检查用户是否有权限删除指定留言
     * @param messageId 留言ID
     * @param userId 用户ID，可为null表示游客
     * @return 是否有权限删除
     */
    boolean canDeleteMessage(Integer messageId, Integer userId);
}