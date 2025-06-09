package com.blog.dao;

import com.blog.model.Post;
import java.sql.SQLException;
import java.util.List;

public interface PostDao {
    // 添加博文
    int insert(Post post) throws SQLException;

    // 根据ID删除博文
    int deleteById(Integer id) throws SQLException;

    // 更新博文信息
    int update(Post post) throws SQLException;

    // 根据ID查询博文
    Post findById(Integer id) throws SQLException;

    // 根据分类ID查询博文列表
    List<Post> findByCategoryId(Integer categoryId) throws SQLException;

    // 根据用户ID查询博文列表
    List<Post> findByUserId(Integer userId) throws SQLException;

    // 查询所有博文
    List<Post> findAll() throws SQLException;

    // 分页查询博文
    List<Post> findByPage(int page, int pageSize) throws SQLException;

    // 获取博文总数
    int getTotal() throws SQLException;

    // 增加博文点赞数
    int incrementLikes(Integer id) throws SQLException;

    // 减少博文点赞数
    int decrementLikes(Integer id) throws SQLException;

    // 增加博文浏览量
    int incrementViews(Integer id) throws SQLException;

    // 减少博文浏览量
    int decrementViews(Integer id) throws SQLException;

    // 获取博文点赞数
    int getLikesCount(Integer id) throws SQLException;

    // 获取博文浏览量
    int getViewsCount(Integer id) throws SQLException;

    // 搜索博文
    List<Post> search(String keyword) throws SQLException;

    // 检查用户是否已点赞该文章
    boolean hasUserLiked(Integer postId, Integer userId) throws SQLException;

    // 检查设备今天是否已浏览该文章
    boolean hasDeviceViewedToday(Integer postId, String deviceId) throws SQLException;

    // 记录浏览
    void recordView(Integer postId, String deviceId) throws SQLException;

    // 记录点赞
    void recordLike(Integer postId, Integer userId) throws SQLException;

    // 取消点赞
    void removeLike(Integer postId, Integer userId) throws SQLException;

    // 获取分类博文总数
    int getTotalByCategory(Integer categoryId) throws SQLException;
}