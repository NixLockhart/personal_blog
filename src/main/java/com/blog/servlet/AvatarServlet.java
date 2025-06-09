package com.blog.servlet;

import com.blog.util.LogUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebServlet("/avatar/*")
public class AvatarServlet extends HttpServlet {
    private final LogUtil logUtil = LogUtil.getInstance();
    private static final String AVATAR_DIR = "avatar";
    private static final String BASE_DIR;

    static {
        // 判断是否为生产环境
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("linux")) {
            // 生产环境
            BASE_DIR = "/var";
        } else {
            // 开发环境
            BASE_DIR = "C:\\Users\\Lockhart\\Desktop";
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
        logUtil.log("AVATAR_SERVLET", "system", "AvatarServlet初始化");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 获取请求的头像路径
        String pathInfo = request.getPathInfo();
        logUtil.log("AVATAR_REQUEST", "system", "收到头像请求: " + pathInfo);

        if (pathInfo == null || pathInfo.isEmpty()) {
            logUtil.logError("AVATAR_REQUEST", "头像路径为空");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            // 构建头像文件路径
            Path avatarPath = Paths.get(BASE_DIR, AVATAR_DIR, pathInfo.substring(1));
            logUtil.log("AVATAR_REQUEST", "system", "头像文件路径: " + avatarPath.toAbsolutePath());

            // 检查文件是否存在
            if (!Files.exists(avatarPath)) {
                logUtil.logError("AVATAR_REQUEST", "头像文件不存在: " + avatarPath.toAbsolutePath());
                // 如果头像不存在，使用默认头像
                avatarPath = Paths.get(BASE_DIR, AVATAR_DIR, "default.png");
                if (!Files.exists(avatarPath)) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }

            // 检查文件是否可读
            if (!Files.isReadable(avatarPath)) {
                logUtil.logError("AVATAR_REQUEST", "头像文件不可读: " + avatarPath.toAbsolutePath());
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // 设置响应头
            String contentType = getContentType(avatarPath.getFileName().toString());
            logUtil.log("AVATAR_REQUEST", "system", "设置Content-Type: " + contentType);
            response.setContentType(contentType);

            // 设置无缓存头，确保浏览器每次都请求最新的头像
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, proxy-revalidate, no-transform");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            response.setHeader("Vary", "*");

            // 添加ETag和Last-Modified头
            long lastModified = Files.getLastModifiedTime(avatarPath).toMillis();
            response.setHeader("ETag", String.valueOf(lastModified));
            response.setDateHeader("Last-Modified", lastModified);

            // 检查If-Modified-Since和If-None-Match头
            long ifModifiedSince = request.getDateHeader("If-Modified-Since");
            String ifNoneMatch = request.getHeader("If-None-Match");

            if (ifModifiedSince != -1 && ifNoneMatch != null) {
                if (ifModifiedSince >= lastModified && ifNoneMatch.equals(String.valueOf(lastModified))) {
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return;
                }
            }

            // 读取并输出头像
            try (FileInputStream fis = new FileInputStream(avatarPath.toFile());
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                int totalBytes = 0;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }
                logUtil.log("AVATAR_REQUEST", "system", String.format("成功发送头像，大小: %d 字节", totalBytes));
            }

        } catch (Exception e) {
            logUtil.logError("AVATAR_REQUEST", "加载头像失败: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String getContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            default:
                return "application/octet-stream";
        }
    }
}