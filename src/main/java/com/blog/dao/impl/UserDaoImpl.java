package com.blog.dao.impl;

import com.blog.dao.UserDao;
import com.blog.model.User;
import com.blog.util.DBUtil;
import com.blog.util.LogUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDaoImpl implements UserDao {
    private final LogUtil logUtil = LogUtil.getInstance();

    @Override
    public int insert(User user) throws SQLException {
        String sql = "INSERT INTO tb_users (username, password, salt, email, avatar_url, birthday) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getSalt());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getAvatarUrl());
            stmt.setDate(6, user.getBirthday() != null ? new java.sql.Date(user.getBirthday().getTime()) : null);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
            logUtil.log("USER_REGISTER", user.getUsername(), "New user registered");
            return affectedRows;
        } catch (SQLException e) {
            logUtil.logError("USER_REGISTER", e.getMessage());
            throw e;
        }
    }

    @Override
    public int deleteById(Integer id) throws SQLException {
        String sql = "DELETE FROM tb_users WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            User user = findById(id);
            stmt.setInt(1, id);
            int result = stmt.executeUpdate();
            if (result > 0 && user != null) {
                logUtil.log("USER_DELETE", user.getUsername(), "User account deleted");
            }
            return result;
        } catch (SQLException e) {
            logUtil.logError("USER_DELETE", e.getMessage());
            throw e;
        }
    }

    @Override
    public int update(User user) throws SQLException {
        String sql = "UPDATE tb_users SET username = ?, password = ?, salt = ?, email = ?, avatar_url = ?, birthday = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getSalt());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getAvatarUrl());
            stmt.setDate(6, user.getBirthday() != null ? new java.sql.Date(user.getBirthday().getTime()) : null);
            stmt.setInt(7, user.getId());
            int result = stmt.executeUpdate();
            if (result > 0) {
                logUtil.log("USER_UPDATE", user.getUsername(), "User information updated");
            }
            return result;
        } catch (SQLException e) {
            logUtil.logError("USER_UPDATE", e.getMessage());
            throw e;
        }
    }

    @Override
    public User findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM tb_users WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
                return null;
            }
        }
    }

    @Override
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM tb_users WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
                return null;
            }
        }
    }

    @Override
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM tb_users WHERE email = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
                return null;
            }
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM tb_users";
        List<User> users = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
        }
    }

    @Override
    public User login(String username, String password) throws SQLException {
        String sql = "SELECT * FROM tb_users WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    if (password.equals(user.getPassword())) {
                        logUtil.log("USER_LOGIN", username, "User logged in successfully");
                        return user;
                    }
                }
                logUtil.log("USER_LOGIN_FAILED", username, "Login attempt failed");
                return null;
            }
        } catch (SQLException e) {
            logUtil.logError("USER_LOGIN", e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean isUsernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tb_users WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    @Override
    public boolean isEmailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tb_users WHERE email = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setSalt(rs.getString("salt"));
        user.setEmail(rs.getString("email"));
        user.setAvatarUrl(rs.getString("avatar_url"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        user.setBirthday(rs.getDate("birthday"));
        return user;
    }
}