package com.blog.dao;

public interface VisitsDao {
    /**
     * 记录新的访问
     * @param userAgent 访客浏览器标识
     * @return 是否记录成功
     */
    boolean recordVisit(String userAgent);

    /**
     * 获取总访问量（不同UserAgent的数量）
     * @return 总访问量
     */
    int getTotalVisits();
}