package com.blog.dao;

import com.blog.model.User;
import java.sql.SQLException;
import java.util.List;

public interface UserDao {
    // 添加用户
    int insert(User user) throws SQLException;

    // 根据ID删除用户
    int deleteById(Integer id) throws SQLException;

    // 更新用户信息
    int update(User user) throws SQLException;

    // 根据ID查询用户
    User findById(Integer id) throws SQLException;

    // 根据用户名查询用户
    User findByUsername(String username) throws SQLException;

    // 根据邮箱查询用户
    User findByEmail(String email) throws SQLException;

    // 查询所有用户
    List<User> findAll() throws SQLException;

    // 用户登录验证
    User login(String username, String password) throws SQLException;

    // 检查用户名是否存在
    boolean isUsernameExists(String username) throws SQLException;

    // 检查邮箱是否存在
    boolean isEmailExists(String email) throws SQLException;
}