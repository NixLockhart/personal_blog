package com.blog.servlet;

import com.blog.util.LogUtil;
import com.blog.util.MarkdownUtil;

import javax.servlet.ServletException;
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

public class ImageServlet extends HttpServlet {
    private final LogUtil logUtil = LogUtil.getInstance();

    @Override
    public void init() throws ServletException {
        super.init();
        logUtil.log("IMAGE_SERVLET", "system", "ImageServlet初始化");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 获取请求的图片路径
        String pathInfo = request.getPathInfo();
        logUtil.log("IMAGE_REQUEST", "system", "收到图片请求: " + pathInfo);

        if (pathInfo == null || pathInfo.isEmpty()) {
            logUtil.logError("IMAGE_REQUEST", "图片路径为空");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 解析文章ID和图片文件名
        String[] pathParts = pathInfo.split("/");
        if (pathParts.length < 3) {
            logUtil.logError("IMAGE_REQUEST", "无效的图片路径格式: " + pathInfo);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            int postId = Integer.parseInt(pathParts[1]);
            String imageName = pathParts[2];
            logUtil.log("IMAGE_REQUEST", "system", String.format("解析图片请求 - 文章ID: %d, 图片名: %s", postId, imageName));

            // 构建图片文件路径
            Path imagePath = Paths.get(MarkdownUtil.getBaseDir(), MarkdownUtil.getArticlesDir(), "img",
                    String.valueOf(postId), imageName);
            logUtil.log("IMAGE_REQUEST", "system", "图片文件路径: " + imagePath.toAbsolutePath());

            // 检查文件是否存在
            if (!Files.exists(imagePath)) {
                logUtil.logError("IMAGE_REQUEST", "图片文件不存在: " + imagePath.toAbsolutePath());
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 检查文件是否可读
            if (!Files.isReadable(imagePath)) {
                logUtil.logError("IMAGE_REQUEST", "图片文件不可读: " + imagePath.toAbsolutePath());
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // 设置响应头
            String contentType = getContentType(imageName);
            logUtil.log("IMAGE_REQUEST", "system", "设置Content-Type: " + contentType);
            response.setContentType(contentType);
            response.setHeader("Cache-Control", "public, max-age=31536000");

            // 读取并输出图片
            try (FileInputStream fis = new FileInputStream(imagePath.toFile());
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                int totalBytes = 0;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }
                logUtil.log("IMAGE_REQUEST", "system", String.format("成功发送图片，大小: %d 字节", totalBytes));
            }

        } catch (NumberFormatException e) {
            logUtil.logError("IMAGE_REQUEST", "无效的文章ID: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            logUtil.logError("IMAGE_REQUEST", "加载图片失败: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String getContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }
}