package com.blog.service;

import com.blog.model.User;
import com.blog.util.DBUtil;
import com.blog.util.LogUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class UserService {
    public User getUserById(Integer id) throws SQLException {
        String sql = "SELECT * FROM tb_users WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
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
            return null;
        } catch (SQLException e) {
            LogUtil.getInstance().logError("USER_SERVICE", "获取用户信息失败: " + e.getMessage());
            throw e;
        }
    }

    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM tb_users WHERE email = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
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
            return null;
        } catch (SQLException e) {
            LogUtil.getInstance().logError("USER_SERVICE", "根据邮箱获取用户信息失败: " + e.getMessage());
            throw e;
        }
    }

    public void updateUser(User user) throws SQLException {
        if (user == null || user.getId() == null) {
            LogUtil.getInstance().logError("USER_SERVICE", "更新用户信息失败：用户对象或ID为空");
            throw new SQLException("用户对象或ID不能为空");
        }

        StringBuilder sql = new StringBuilder("UPDATE tb_users SET username = ?, email = ?, updated_at = ?")
                .append(user.getPassword() != null ? ", password = ?, salt = ?" : "")
                .append(user.getBirthday() != null ? ", birthday = ?" : "")
                .append(user.getAvatarUrl() != null ? ", avatar_url = ?" : "")
                .append(" WHERE id = ?");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            stmt.setString(paramIndex++, user.getUsername());
            stmt.setString(paramIndex++, user.getEmail());
            stmt.setTimestamp(paramIndex++, new java.sql.Timestamp(new Date().getTime()));

            if (user.getPassword() != null) {
                stmt.setString(paramIndex++, user.getPassword());
                stmt.setString(paramIndex++, user.getSalt());
            }

            if (user.getBirthday() != null) {
                stmt.setDate(paramIndex++, new java.sql.Date(user.getBirthday().getTime()));
            }

            if (user.getAvatarUrl() != null) {
                stmt.setString(paramIndex++, user.getAvatarUrl());
            }

            stmt.setInt(paramIndex, user.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                String errorMsg = "更新用户信息失败，用户ID: " + user.getId() + " 不存在";
                LogUtil.getInstance().logError("USER_SERVICE", errorMsg);
                throw new SQLException(errorMsg);
            }
        } catch (SQLException e) {
            LogUtil.getInstance().logError("USER_SERVICE", "更新用户信息时发生错误: " + e.getMessage());
            throw e;
        }
    }
}