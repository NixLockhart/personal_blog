package com.blog.dao;

import com.blog.model.Notification;
import java.sql.SQLException;
import java.util.List;

public interface NotificationDao {
    /**
     * 插入一条通知
     * @param notification 通知对象
     * @return 影响的行数
     */
    int insert(Notification notification) throws SQLException;

    /**
     * 批量插入通知（如系统公告）
     * @param notifications 通知列表
     * @return 影响的行数
     */
    int batchInsert(List<Notification> notifications) throws SQLException;

    /**
     * 更新通知已读状态
     * @param id 通知ID
     * @param isRead 是否已读
     * @return 影响的行数
     */
    int updateReadStatus(Integer id, Boolean isRead) throws SQLException;

    /**
     * 更新用户的所有通知为已读
     * @param userId 用户ID
     * @return 影响的行数
     */
    int markAllAsRead(Integer userId) throws SQLException;

    /**
     * 获取用户的所有通知
     * @param userId 用户ID
     * @return 通知列表
     */
    List<Notification> findByUserId(Integer userId) throws SQLException;

    /**
     * 获取用户的未读通知列表
     * @param userId 用户ID
     * @return 未读通知列表
     */
    List<Notification> findUnreadByUserId(Integer userId) throws SQLException;

    /**
     * 根据类型获取用户的通知
     * @param userId 用户ID
     * @param type 通知类型
     * @return 通知列表
     */
    List<Notification> findByUserIdAndType(Integer userId, String type) throws SQLException;

    /**
     * 获取用户通知数量
     * @param userId 用户ID
     * @return 通知数量
     */
    int countByUserId(Integer userId) throws SQLException;

    /**
     * 获取用户未读通知数量
     * @param userId 用户ID
     * @return 未读通知数量
     */
    int countUnreadByUserId(Integer userId) throws SQLException;

    /**
     * 根据ID删除通知
     * @param id 通知ID
     * @return 影响的行数
     */
    int deleteById(Integer id) throws SQLException;

    /**
     * 删除用户的所有通知
     * @param userId 用户ID
     * @return 影响的行数
     */
    int deleteAllByUserId(Integer userId) throws SQLException;

    /**
     * 根据ID获取通知
     * @param id 通知ID
     * @return 通知对象，如果不存在则返回null
     * @throws SQLException SQL异常
     */
    Notification findById(Integer id) throws SQLException;

    /**
     * 获取所有通知
     * @return 通知列表
     * @throws SQLException SQL异常
     */
    List<Notification> findAll() throws SQLException;
}