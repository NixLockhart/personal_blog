package com.blog.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogUtil {
    private static LogUtil instance;
    private final Logger logger;

    private LogUtil() {
        logger = LogManager.getLogger(LogUtil.class);
    }

    public static LogUtil getInstance() {
        if (instance == null) {
            synchronized (LogUtil.class) {
                if (instance == null) {
                    instance = new LogUtil();
                }
            }
        }
        return instance;
    }

    public void log(String operation, String username, String details) {
        String message = String.format("[%s] %s - %s", operation, username, details);
        logger.info(message);
    }

    public void logError(String operation, String error) {
        String message = String.format("[%s] - %s", operation, error);
        logger.error(message);
    }
}