package com.blog.dao.impl;

import com.blog.dao.CommentDao;
import com.blog.model.Comment;
import com.blog.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDaoImpl implements CommentDao {
    @Override
    public int insert(Comment comment) throws SQLException {
        String sql = "INSERT INTO tb_comments (post_id, user_id, content, parent_id, reply_to_user_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, comment.getPostId());
            stmt.setInt(2, comment.getUserId());
            stmt.setString(3, comment.getContent());

            if (comment.getParentId() != null) {
                stmt.setInt(4, comment.getParentId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            if (comment.getReplyToUserId() != null) {
                stmt.setInt(5, comment.getReplyToUserId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating comment failed, no rows affected.");
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    comment.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating comment failed, no ID obtained.");
                }
            }
            return affectedRows;
        }
    }

    @Override
    public int deleteById(Integer id) throws SQLException {
        String sql = "DELETE FROM tb_comments WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        }
    }

    @Override
    public int deleteByPostId(Integer postId) throws SQLException {
        String sql = "DELETE FROM tb_comments WHERE post_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            return stmt.executeUpdate();
        }
    }

    @Override
    public int update(Comment comment) throws SQLException {
        String sql = "UPDATE tb_comments SET content = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, comment.getContent());
            stmt.setInt(2, comment.getId());
            return stmt.executeUpdate();
        }
    }

    @Override
    public Comment findById(Integer id) throws SQLException {
        String sql = "SELECT c.*, u.username, u.avatar_url, p.title as post_title " +
                "FROM tb_comments c " +
                "LEFT JOIN tb_users u ON c.user_id = u.id " +
                "LEFT JOIN tb_posts p ON c.post_id = p.id " +
                "WHERE c.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToComment(rs);
                }
                return null;
            }
        }
    }

    @Override
    public List<Comment> findByPostId(Integer postId) throws SQLException {
        String sql = "SELECT c.*, u.username, u.avatar_url, p.title as post_title, ru.username as reply_to_username " +
                "FROM tb_comments c " +
                "LEFT JOIN tb_users u ON c.user_id = u.id " +
                "LEFT JOIN tb_posts p ON c.post_id = p.id " +
                "LEFT JOIN tb_users ru ON c.reply_to_user_id = ru.id " +
                "WHERE c.post_id = ? " +
                "ORDER BY c.created_at DESC";
        List<Comment> comments = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapResultSetToComment(rs));
                }
            }
        }
        return comments;
    }

    @Override
    public List<Comment> findByUserId(Integer userId) throws SQLException {
        String sql = "SELECT c.*, u.username, u.avatar_url, p.title as post_title " +
                "FROM tb_comments c " +
                "LEFT JOIN tb_users u ON c.user_id = u.id " +
                "LEFT JOIN tb_posts p ON c.post_id = p.id " +
                "WHERE c.user_id = ? " +
                "ORDER BY c.created_at DESC";
        List<Comment> comments = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapResultSetToComment(rs));
                }
            }
        }
        return comments;
    }

    @Override
    public List<Comment> findAll() throws SQLException {
        String sql = "SELECT c.*, u.username, u.avatar_url, p.title as post_title " +
                "FROM tb_comments c " +
                "LEFT JOIN tb_users u ON c.user_id = u.id " +
                "LEFT JOIN tb_posts p ON c.post_id = p.id " +
                "ORDER BY c.created_at DESC";
        List<Comment> comments = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                comments.add(mapResultSetToComment(rs));
            }
        }
        return comments;
    }

    @Override
    public List<Comment> findByPage(int page, int pageSize) throws SQLException {
        String sql = "SELECT c.*, u.username, u.avatar_url, p.title as post_title " +
                "FROM tb_comments c " +
                "LEFT JOIN tb_users u ON c.user_id = u.id " +
                "LEFT JOIN tb_posts p ON c.post_id = p.id " +
                "ORDER BY c.created_at DESC LIMIT ? OFFSET ?";
        List<Comment> comments = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pageSize);
            stmt.setInt(2, (page - 1) * pageSize);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapResultSetToComment(rs));
                }
            }
        }
        return comments;
    }

    @Override
    public int getTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tb_comments";
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
    public int getPostCommentCount(Integer postId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tb_comments WHERE post_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }

    private Comment mapResultSetToComment(ResultSet rs) throws SQLException {
        Comment comment = new Comment();
        comment.setId(rs.getInt("id"));
        comment.setPostId(rs.getInt("post_id"));
        comment.setUserId(rs.getInt("user_id"));
        comment.setContent(rs.getString("content"));
        comment.setCreatedAt(rs.getTimestamp("created_at"));
        comment.setUsername(rs.getString("username"));
        comment.setPostTitle(rs.getString("post_title"));
        comment.setAvatarUrl(rs.getString("avatar_url"));

        // 获取父评论ID
        int parentId = rs.getInt("parent_id");
        if (!rs.wasNull()) {
            comment.setParentId(parentId);
        }

        // 获取回复用户ID
        int replyToUserId = rs.getInt("reply_to_user_id");
        if (!rs.wasNull()) {
            comment.setReplyToUserId(replyToUserId);
        }

        // 获取被回复用户名，可能为null
        try {
            comment.setReplyToUsername(rs.getString("reply_to_username"));
        } catch (SQLException e) {
            // 忽略字段不存在的异常，因为某些查询可能没有此字段
        }

        return comment;
    }
}