package com.blog.dao;

import com.blog.model.Comment;
import java.sql.SQLException;
import java.util.List;

public interface CommentDao {
    // 添加评论
    int insert(Comment comment) throws SQLException;

    // 根据ID删除评论
    int deleteById(Integer id) throws SQLException;

    // 根据博文ID删除评论
    int deleteByPostId(Integer postId) throws SQLException;

    // 更新评论信息
    int update(Comment comment) throws SQLException;

    // 根据ID查询评论
    Comment findById(Integer id) throws SQLException;

    // 根据博文ID查询评论列表
    List<Comment> findByPostId(Integer postId) throws SQLException;

    // 根据用户ID查询评论列表
    List<Comment> findByUserId(Integer userId) throws SQLException;

    // 查询所有评论
    List<Comment> findAll() throws SQLException;

    // 分页查询评论
    List<Comment> findByPage(int page, int pageSize) throws SQLException;

    // 获取评论总数
    int getTotal() throws SQLException;

    // 获取博文评论数
    int getPostCommentCount(Integer postId) throws SQLException;
}