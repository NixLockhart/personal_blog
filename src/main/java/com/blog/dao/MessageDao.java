package com.blog.dao;

import com.blog.model.Message;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface MessageDao {
    /**
     * 插入留言
     * @param message 留言对象
     * @return 受影响的行数
     * @throws SQLException 数据库异常
     */
    int insert(Message message) throws SQLException;

    /**
     * 根据ID删除留言
     * @param id 留言ID
     * @return 受影响的行数
     * @throws SQLException 数据库异常
     */
    int deleteById(Integer id) throws SQLException;

    /**
     * 更新留言已读状态
     * @param id 留言ID
     * @param isRead 是否已读
     * @return 受影响的行数
     * @throws SQLException 数据库异常
     */
    int updateReadStatus(Integer id, boolean isRead) throws SQLException;

    /**
     * 根据ID查询留言
     * @param id 留言ID
     * @return 留言对象
     * @throws SQLException 数据库异常
     */
    Message findById(Integer id) throws SQLException;

    /**
     * 查询所有顶级留言（不包括回复）
     * @return 留言列表
     * @throws SQLException 数据库异常
     */
    List<Message> findAllTopMessages() throws SQLException;

    /**
     * 查询指定留言的所有回复
     * @param parentId 父留言ID
     * @return 回复列表
     * @throws SQLException 数据库异常
     */
    List<Message> findRepliesByParentId(Integer parentId) throws SQLException;

    /**
     * 查询所有未读留言
     * @return 未读留言列表
     * @throws SQLException 数据库异常
     */
    List<Message> findAllUnread() throws SQLException;

    /**
     * 获取未读留言数量
     * @return 未读留言数量
     * @throws SQLException 数据库异常
     */
    int getUnreadCount() throws SQLException;

    /**
     * 查询所有留言（包括顶级留言和回复），按创建时间倒序排列
     * @return 留言列表
     * @throws SQLException 数据库异常
     */
    List<Message> findAll() throws SQLException;

    /**
     * 分页查询所有留言
     * @param page 页码
     * @param pageSize 每页条数
     * @return 留言列表
     * @throws SQLException 数据库异常
     */
    List<Message> findByPage(int page, int pageSize) throws SQLException;

    /**
     * 获取留言总数
     * @return 留言总数
     * @throws SQLException 数据库异常
     */
    int getTotal() throws SQLException;

    /**
     * 标记所有留言为已读
     * @return 受影响的行数
     * @throws SQLException 数据库异常
     */
    int markAllAsRead() throws SQLException;
}