package com.blog.dao.impl;

import com.blog.dao.MessageDao;
import com.blog.model.Message;
import com.blog.util.DBUtil;
import com.blog.util.LogUtil;
import com.blog.dao.UserDao;
import com.blog.dao.impl.UserDaoImpl;
import com.blog.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDaoImpl implements MessageDao {

    private final UserDao userDao;
    private final LogUtil logUtil;

    public MessageDaoImpl() {
        this.userDao = new UserDaoImpl();
        this.logUtil = LogUtil.getInstance();
    }

    @Override
    public int insert(Message message) throws SQLException {
        String sql = "INSERT INTO tb_messages (user_id, name, email, message, parent_id, avatar_url, ip_address) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (message.getUserId() != null) {
                stmt.setInt(1, message.getUserId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }

            stmt.setString(2, message.getName());
            stmt.setString(3, message.getEmail());
            stmt.setString(4, message.getMessage());

            if (message.getParentId() != null) {
                stmt.setInt(5, message.getParentId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setString(6, message.getAvatarUrl());
            stmt.setString(7, message.getIpAddress());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        message.setId(generatedKeys.getInt(1));
                    }
                }
            }

            return affectedRows;
        }
    }

    @Override
    public int deleteById(Integer id) throws SQLException {
        String sql = "DELETE FROM tb_messages WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        }
    }

    @Override
    public int updateReadStatus(Integer id, boolean isRead) throws SQLException {
        String sql = "UPDATE tb_messages SET is_read = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, isRead);
            stmt.setInt(2, id);
            return stmt.executeUpdate();
        }
    }

    @Override
    public Message findById(Integer id) throws SQLException {
        String sql = "SELECT m.*, u.username, u.avatar_url as user_avatar_url " +
                "FROM tb_messages m " +
                "LEFT JOIN tb_users u ON m.user_id = u.id " +
                "WHERE m.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMessage(rs);
                }
                return null;
            }
        }
    }

    @Override
    public List<Message> findAllTopMessages() throws SQLException {
        String sql = "SELECT m.*, u.username, u.avatar_url as user_avatar_url " +
                "FROM tb_messages m " +
                "LEFT JOIN tb_users u ON m.user_id = u.id " +
                "WHERE m.parent_id IS NULL " +
                "ORDER BY m.created_at DESC";
        List<Message> messages = new ArrayList<>();

        logUtil.log("MESSAGE_QUERY", "system", "开始获取留言列表");

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            logUtil.log("MESSAGE_QUERY", "system", "执行SQL查询成功");
            while (rs.next()) {
                try {
                    Message message = mapResultSetToMessage(rs);
                    if (message != null) {
                        messages.add(message);
                        logUtil.log("MESSAGE_QUERY", "system", "成功获取留言，ID: " + message.getId());
                    }
                } catch (SQLException e) {
                    logUtil.logError("MESSAGE_ERROR", "映射留言数据失败: " + e.getMessage());
                    // 继续处理下一条记录
                    continue;
                }
            }
        } catch (SQLException e) {
            logUtil.logError("MESSAGE_ERROR", "执行SQL查询失败: " + e.getMessage());
            throw e;
        }

        logUtil.log("MESSAGE_QUERY", "system", "成功获取留言列表，共" + messages.size() + "条记录");
        return messages;
    }

    @Override
    public List<Message> findRepliesByParentId(Integer parentId) throws SQLException {
        String sql = "SELECT m.*, u.username, u.avatar_url as user_avatar_url " +
                "FROM tb_messages m " +
                "LEFT JOIN tb_users u ON m.user_id = u.id " +
                "WHERE m.parent_id = ? " +
                "ORDER BY m.created_at ASC";
        List<Message> replies = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, parentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    replies.add(mapResultSetToMessage(rs));
                }
            }
        }
        return replies;
    }

    @Override
    public List<Message> findAllUnread() throws SQLException {
        String sql = "SELECT m.*, u.username, u.avatar_url as user_avatar_url " +
                "FROM tb_messages m " +
                "LEFT JOIN tb_users u ON m.user_id = u.id " +
                "WHERE m.is_read = 0 " +
                "ORDER BY m.created_at DESC";
        List<Message> messages = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        }
        return messages;
    }

    @Override
    public int getUnreadCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tb_messages WHERE is_read = 0";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    @Override
    public List<Message> findAll() throws SQLException {
        String sql = "SELECT m.*, u.username, u.avatar_url as user_avatar_url " +
                "FROM tb_messages m " +
                "LEFT JOIN tb_users u ON m.user_id = u.id " +
                "ORDER BY m.created_at DESC";
        List<Message> messages = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        }
        return messages;
    }

    @Override
    public List<Message> findByPage(int page, int pageSize) throws SQLException {
        String sql = "SELECT m.*, u.username, u.avatar_url as user_avatar_url " +
                "FROM tb_messages m " +
                "LEFT JOIN tb_users u ON m.user_id = u.id " +
                "ORDER BY m.created_at DESC " +
                "LIMIT ? OFFSET ?";
        List<Message> messages = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pageSize);
            stmt.setInt(2, (page - 1) * pageSize);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapResultSetToMessage(rs));
                }
            }
        }
        return messages;
    }

    @Override
    public int getTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tb_messages";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    @Override
    public int markAllAsRead() throws SQLException {
        String sql = "UPDATE tb_messages SET is_read = 1 WHERE is_read = 0";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }

    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        try {
            message.setId(rs.getInt("id"));
            message.setUserId(rs.getInt("user_id"));
            message.setName(rs.getString("name"));
            message.setEmail(rs.getString("email"));

            // 确保message字段不为null
            String messageContent = rs.getString("message");
            message.setMessage(messageContent != null ? messageContent : "");

            message.setParentId(rs.getInt("parent_id"));
            message.setAvatarUrl(rs.getString("avatar_url"));
            message.setIpAddress(rs.getString("ip_address"));
            message.setIsRead(rs.getBoolean("is_read"));
            message.setCreatedAt(rs.getTimestamp("created_at"));

            // 设置前端显示字段
            if (message.getUserId() != 0) {
                try {
                    User user = userDao.findById(message.getUserId());
                    if (user != null) {
                        message.setUsername(user.getUsername());
                        message.setUserAvatarUrl(user.getAvatarUrl());
                        message.setIsAdmin(user.getId() == 1); // 只有ID为1的用户是管理员
                    }
                } catch (Exception e) {
                    logUtil.logError("MESSAGE_ERROR", "获取用户信息失败，用户ID: " + message.getUserId() + ", 错误: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // 设置删除权限
            message.setCanDelete(true);

            logUtil.log("MESSAGE", "system", "成功映射留言数据，ID: " + message.getId() + ", 内容: " + message.getMessage());
        } catch (SQLException e) {
            logUtil.logError("MESSAGE_ERROR", "映射留言数据失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return message;
    }
}