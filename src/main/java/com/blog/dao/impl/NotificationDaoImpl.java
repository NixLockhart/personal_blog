package com.blog.dao.impl;

import com.blog.dao.NotificationDao;
import com.blog.model.Notification;
import com.blog.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDaoImpl implements NotificationDao {

    @Override
    public int insert(Notification notification) throws SQLException {
        String sql = "INSERT INTO tb_notifications (user_id, type, title, content, source_id, post_id, from_user_id, is_read) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, notification.getUserId());
            stmt.setString(2, notification.getType());
            stmt.setString(3, notification.getTitle());
            stmt.setString(4, notification.getContent());
            if (notification.getSourceId() != null) {
                stmt.setInt(5, notification.getSourceId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            if (notification.getPostId() != null) {
                stmt.setInt(6, notification.getPostId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            if (notification.getFromUserId() != null) {
                stmt.setInt(7, notification.getFromUserId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            stmt.setBoolean(8, notification.getIsRead() != null ? notification.getIsRead() : false);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating notification failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    notification.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating notification failed, no ID obtained.");
                }
            }
            return affectedRows;
        }
    }

    @Override
    public int batchInsert(List<Notification> notifications) throws SQLException {
        String sql = "INSERT INTO tb_notifications (user_id, type, title, content, source_id, post_id, from_user_id, is_read) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        int totalAffectedRows = 0;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            for (Notification notification : notifications) {
                stmt.setInt(1, notification.getUserId());
                stmt.setString(2, notification.getType());
                stmt.setString(3, notification.getTitle());
                stmt.setString(4, notification.getContent());
                if (notification.getSourceId() != null) {
                    stmt.setInt(5, notification.getSourceId());
                } else {
                    stmt.setNull(5, Types.INTEGER);
                }
                if (notification.getPostId() != null) {
                    stmt.setInt(6, notification.getPostId());
                } else {
                    stmt.setNull(6, Types.INTEGER);
                }
                if (notification.getFromUserId() != null) {
                    stmt.setInt(7, notification.getFromUserId());
                } else {
                    stmt.setNull(7, Types.INTEGER);
                }
                stmt.setBoolean(8, notification.getIsRead() != null ? notification.getIsRead() : false);

                stmt.addBatch();
            }

            int[] batchResults = stmt.executeBatch();
            for (int result : batchResults) {
                totalAffectedRows += result;
            }

            return totalAffectedRows;
        }
    }

    @Override
    public int updateReadStatus(Integer id, Boolean isRead) throws SQLException {
        String sql = "UPDATE tb_notifications SET is_read = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, isRead);
            stmt.setInt(2, id);
            return stmt.executeUpdate();
        }
    }

    @Override
    public int markAllAsRead(Integer userId) throws SQLException {
        String sql = "UPDATE tb_notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate();
        }
    }

    @Override
    public List<Notification> findByUserId(Integer userId) throws SQLException {
        String sql = "SELECT n.*, u.username as from_username, u.avatar_url as from_user_avatar_url, p.title as post_title " +
                "FROM tb_notifications n " +
                "LEFT JOIN tb_users u ON n.from_user_id = u.id " +
                "LEFT JOIN tb_posts p ON n.post_id = p.id " +
                "WHERE n.user_id = ? " +
                "ORDER BY n.created_at DESC";

        List<Notification> notifications = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapResultSetToNotification(rs));
                }
            }
        }
        return notifications;
    }

    @Override
    public List<Notification> findUnreadByUserId(Integer userId) throws SQLException {
        String sql = "SELECT n.*, u.username as from_username, u.avatar_url as from_user_avatar_url, p.title as post_title " +
                "FROM tb_notifications n " +
                "LEFT JOIN tb_users u ON n.from_user_id = u.id " +
                "LEFT JOIN tb_posts p ON n.post_id = p.id " +
                "WHERE n.user_id = ? AND n.is_read = 0 " +
                "ORDER BY n.created_at DESC";

        List<Notification> notifications = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapResultSetToNotification(rs));
                }
            }
        }
        return notifications;
    }

    @Override
    public List<Notification> findByUserIdAndType(Integer userId, String type) throws SQLException {
        String sql = "SELECT n.*, u.username as from_username, u.avatar_url as from_user_avatar_url, p.title as post_title " +
                "FROM tb_notifications n " +
                "LEFT JOIN tb_users u ON n.from_user_id = u.id " +
                "LEFT JOIN tb_posts p ON n.post_id = p.id " +
                "WHERE n.user_id = ? AND n.type = ? " +
                "ORDER BY n.created_at DESC";

        List<Notification> notifications = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, type);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapResultSetToNotification(rs));
                }
            }
        }
        return notifications;
    }

    @Override
    public int countByUserId(Integer userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tb_notifications WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }

    @Override
    public int countUnreadByUserId(Integer userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tb_notifications WHERE user_id = ? AND is_read = 0";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }

    @Override
    public int deleteById(Integer id) throws SQLException {
        String sql = "DELETE FROM tb_notifications WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        }
    }

    @Override
    public int deleteAllByUserId(Integer userId) throws SQLException {
        String sql = "DELETE FROM tb_notifications WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate();
        }
    }

    @Override
    public Notification findById(Integer id) throws SQLException {
        String sql = "SELECT n.*, u.username as from_username, u.avatar_url as from_user_avatar_url, p.title as post_title " +
                "FROM tb_notifications n " +
                "LEFT JOIN tb_users u ON n.from_user_id = u.id " +
                "LEFT JOIN tb_posts p ON n.post_id = p.id " +
                "WHERE n.id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToNotification(rs);
                }
                return null;
            }
        }
    }

    @Override
    public List<Notification> findAll() throws SQLException {
        String sql = "SELECT n.*, u.username as from_username, u.avatar_url as from_user_avatar_url, p.title as post_title, " +
                "ru.username as recipient_username " +
                "FROM tb_notifications n " +
                "LEFT JOIN tb_users u ON n.from_user_id = u.id " +
                "LEFT JOIN tb_users ru ON n.user_id = ru.id " +
                "LEFT JOIN tb_posts p ON n.post_id = p.id " +
                "ORDER BY n.created_at DESC";

        List<Notification> notifications = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Notification notification = mapResultSetToNotification(rs);
                // 获取接收者用户名
                notification.setRecipientUsername(rs.getString("recipient_username"));
                notifications.add(notification);
            }
        }
        return notifications;
    }

    /**
     * 将结果集映射为通知对象
     * @param rs 结果集
     * @return 通知对象
     * @throws SQLException SQL异常
     */
    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId(rs.getInt("id"));
        notification.setUserId(rs.getInt("user_id"));
        notification.setType(rs.getString("type"));
        notification.setTitle(rs.getString("title"));
        notification.setContent(rs.getString("content"));

        // 处理可能为null的字段
        int sourceId = rs.getInt("source_id");
        if (!rs.wasNull()) {
            notification.setSourceId(sourceId);
        }

        int postId = rs.getInt("post_id");
        if (!rs.wasNull()) {
            notification.setPostId(postId);
        }

        int fromUserId = rs.getInt("from_user_id");
        if (!rs.wasNull()) {
            notification.setFromUserId(fromUserId);
        }

        notification.setIsRead(rs.getBoolean("is_read"));
        notification.setCreatedAt(rs.getTimestamp("created_at"));

        // 设置关联信息
        try {
            notification.setFromUsername(rs.getString("from_username"));
            String avatarUrl = rs.getString("from_user_avatar_url");
            if (avatarUrl != null) {
                // 如果头像URL不是以http开头，则添加上下文路径
                if (!avatarUrl.startsWith("http")) {
                    avatarUrl = "/avatar" + (avatarUrl.startsWith("/") ? "" : "/") + avatarUrl;
                }
            }
            notification.setFromUserAvatarUrl(avatarUrl);
            notification.setPostTitle(rs.getString("post_title"));
        } catch (SQLException e) {
            // 忽略这些字段的错误，因为它们可能不存在
        }

        return notification;
    }
}