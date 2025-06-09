package com.blog.util;

import org.apache.commons.dbcp2.BasicDataSource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBUtil {
    private static final BasicDataSource dataSource = new BasicDataSource();
    private static final LogUtil logUtil = LogUtil.getInstance();
    private static String lastConnectionStatus = "未初始化";
    private static String lastErrorCode = "";

    static {
        try {
            logUtil.log("DB_INIT", "system", "开始初始化数据库连接池");
            // 设置数据库连接参数
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            logUtil.log("DB_INIT", "system", "已加载数据库驱动");
            
            dataSource.setUrl("jdbc:mysql://localhost:3306/db_blog?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true");
            dataSource.setUsername("db_blog");  //替换为实际的数据库用户名
            dataSource.setPassword("root");     //替换为实际的数据库密码
            logUtil.log("DB_INIT", "system", "已配置数据库连接参数");

            // 设置连接池参数
            dataSource.setInitialSize(5);
            dataSource.setMaxTotal(20);
            dataSource.setMaxIdle(10);
            dataSource.setMinIdle(5);
            dataSource.setMaxWaitMillis(3000);
            logUtil.log("DB_INIT", "system", "已配置连接池参数");
            
            // 测试连接
            testConnection();
        } catch (Exception e) {
            lastConnectionStatus = "初始化失败";
            lastErrorCode = e.getClass().getName() + ": " + e.getMessage();
            logUtil.logError("DB_INIT", "数据库初始化失败: " + lastErrorCode);
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = dataSource.getConnection();
            lastConnectionStatus = "连接成功";
            lastErrorCode = "";
            return conn;
        } catch (SQLException e) {
            lastConnectionStatus = "连接失败";
            lastErrorCode = e.getClass().getName() + ": " + e.getMessage();
            logUtil.logError("DB_CONNECTION", "获取数据库连接失败: " + lastErrorCode);
            throw e;
        }
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static String getConnectionStatus() {
        return lastConnectionStatus;
    }

    public static String getLastErrorCode() {
        return lastErrorCode;
    }

    private static void testConnection() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn != null) {
                lastConnectionStatus = "初始化成功";
                lastErrorCode = "";
            }
        } catch (SQLException e) {
            lastConnectionStatus = "初始化失败";
            lastErrorCode = e.getClass().getName() + ": " + e.getMessage();
            logUtil.logError("DB_INIT", "数据库连接测试失败: " + lastErrorCode);
        }
    }
}