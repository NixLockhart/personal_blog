package com.blog.dao.impl;

import com.blog.dao.PostDao;
import com.blog.model.Post;
import com.blog.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostDaoImpl implements PostDao {
    @Override
    public int insert(Post post) throws SQLException {
        String sql = "INSERT INTO tb_posts (title, content, content_url, cover_image, category_id, user_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, post.getTitle());
            stmt.setString(2, post.getContent());
            stmt.setString(3, post.getContentUrl());
            stmt.setString(4, post.getCoverImage());
            stmt.setInt(5, post.getCategoryId());
            stmt.setInt(6, post.getUserId());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating post failed, no rows affected.");
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating post failed, no ID obtained.");
                }
            }
            return affectedRows;
        }
    }

    @Override
    public int deleteById(Integer id) throws SQLException {
        String sql = "DELETE FROM tb_posts WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        }
    }

    @Override
    public int update(Post post) throws SQLException {
        String sql = "UPDATE tb_posts SET title = ?, content = ?, content_url = ?, cover_image = ?, category_id = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, post.getTitle());
            stmt.setString(2, post.getContent());
            stmt.setString(3, post.getContentUrl());
            stmt.setString(4, post.getCoverImage());
            stmt.setInt(5, post.getCategoryId());
            stmt.setInt(6, post.getId());
            return stmt.executeUpdate();
        }
    }

    @Override
    public Post findById(Integer id) throws SQLException {
        String sql = "SELECT p.*, c.name as category_name, u.username as author_name " +
                "FROM tb_posts p " +
                "LEFT JOIN tb_categories c ON p.category_id = c.id " +
                "LEFT JOIN tb_users u ON p.user_id = u.id " +
                "WHERE p.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPost(rs);
                }
                return null;
            }
        }
    }

    @Override
    public List<Post> findByCategoryId(Integer categoryId) throws SQLException {
        String sql = "SELECT p.*, c.name as category_name, u.username as author_name " +
                "FROM tb_posts p " +
                "LEFT JOIN tb_categories c ON p.category_id = c.id " +
                "LEFT JOIN tb_users u ON p.user_id = u.id " +
                "WHERE p.category_id = ? " +
                "ORDER BY p.created_at DESC " +
                "LIMIT 5";
        List<Post> posts = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs));
                }
            }
        }
        return posts;
    }

    @Override
    public List<Post> findByUserId(Integer userId) throws SQLException {
        String sql = "SELECT p.*, c.name as category_name, u.username as author_name " +
                "FROM tb_posts p " +
                "LEFT JOIN tb_categories c ON p.category_id = c.id " +
                "LEFT JOIN tb_users u ON p.user_id = u.id " +
                "WHERE p.user_id = ? " +
                "ORDER BY p.created_at DESC";
        List<Post> posts = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs));
                }
            }
        }
        return posts;
    }

    @Override
    public List<Post> findAll() throws SQLException {
        String sql = "SELECT p.*, c.name as category_name, u.username as author_name " +
                "FROM tb_posts p " +
                "LEFT JOIN tb_categories c ON p.category_id = c.id " +
                "LEFT JOIN tb_users u ON p.user_id = u.id " +
                "ORDER BY p.created_at DESC";
        List<Post> posts = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                posts.add(mapResultSetToPost(rs));
            }
        }
        return posts;
    }

    @Override
    public List<Post> findByPage(int page, int pageSize) throws SQLException {
        String sql = "SELECT p.*, c.name as category_name, u.username as author_name " +
                "FROM tb_posts p " +
                "LEFT JOIN tb_categories c ON p.category_id = c.id " +
                "LEFT JOIN tb_users u ON p.user_id = u.id " +
                "ORDER BY p.created_at DESC LIMIT ? OFFSET ?";
        List<Post> posts = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pageSize);
            stmt.setInt(2, (page - 1) * pageSize);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs));
                }
            }
        }
        return posts;
    }

    @Override
    public int getTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tb_posts";
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
    public int incrementLikes(Integer id) throws SQLException {
        String sql = "UPDATE tb_posts SET likes = likes + 1 WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        }
    }

    @Override
    public int decrementLikes(Integer id) throws SQLException {
        String sql = "UPDATE tb_posts SET likes = GREATEST(likes - 1, 0) WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        }
    }

    @Override
    public int incrementViews(Integer id) throws SQLException {
        String sql = "UPDATE tb_posts SET views = COALESCE(views, 0) + 1 WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        }
    }

    @Override
    public int decrementViews(Integer id) throws SQLException {
        String sql = "UPDATE tb_posts SET views = GREATEST(COALESCE(views, 0) - 1, 0) WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        }
    }

    @Override
    public int getLikesCount(Integer id) throws SQLException {
        String sql = "SELECT likes FROM tb_posts WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("likes");
                }
                return 0;
            }
        }
    }

    @Override
    public int getViewsCount(Integer id) throws SQLException {
        String sql = "SELECT COALESCE(views, 0) as views FROM tb_posts WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("views");
                }
                return 0;
            }
        }
    }

    @Override
    public List<Post> search(String keyword) throws SQLException {
        String sql = "SELECT p.*, c.name as category_name, u.username as author_name " +
                "FROM tb_posts p " +
                "LEFT JOIN tb_categories c ON p.category_id = c.id " +
                "LEFT JOIN tb_users u ON p.user_id = u.id " +
                "WHERE p.title LIKE ? OR p.content LIKE ? " +
                "ORDER BY p.created_at DESC";
        List<Post> posts = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs));
                }
            }
        }
        return posts;
    }

    @Override
    public boolean hasUserLiked(Integer postId, Integer userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tb_like_records WHERE post_id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    @Override
    public boolean hasDeviceViewedToday(Integer postId, String deviceId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tb_view_records WHERE post_id = ? AND visitor_id = ? AND view_date = CURRENT_DATE";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setString(2, deviceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    @Override
    public void recordView(Integer postId, String deviceId) throws SQLException {
        String sql = "INSERT INTO tb_view_records (visitor_id, post_id, view_date) VALUES (?, ?, CURRENT_DATE)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, deviceId);
            stmt.setInt(2, postId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void recordLike(Integer postId, Integer userId) throws SQLException {
        String sql = "INSERT INTO tb_like_records (user_id, post_id) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, postId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void removeLike(Integer postId, Integer userId) throws SQLException {
        String sql = "DELETE FROM tb_like_records WHERE user_id = ? AND post_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, postId);
            stmt.executeUpdate();
        }
    }

    @Override
    public int getTotalByCategory(Integer categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tb_posts WHERE category_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private Post mapResultSetToPost(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getInt("id"));
        post.setTitle(rs.getString("title"));
        post.setContent(rs.getString("content"));
        post.setContentUrl(rs.getString("content_url"));
        post.setCoverImage(rs.getString("cover_image"));
        post.setCategoryId(rs.getInt("category_id"));

        Timestamp publishedAt = rs.getTimestamp("published_at");
        post.setPublishedAt(publishedAt != null ? new Date(publishedAt.getTime()) : null);

        post.setLikes(rs.getInt("likes"));
        post.setUserId(rs.getInt("user_id"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        post.setCreatedAt(createdAt != null ? new Date(createdAt.getTime()) : null);

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        post.setUpdatedAt(updatedAt != null ? new Date(updatedAt.getTime()) : null);

        post.setCategoryName(rs.getString("category_name"));
        post.setAuthorName(rs.getString("author_name"));

        try {
            post.setViews(rs.getInt("views"));
        } catch (SQLException e) {
            // 如果views字段不存在，设置默认值为0
            post.setViews(0);
        }

        return post;
    }
}