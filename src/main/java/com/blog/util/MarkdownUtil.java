package com.blog.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MarkdownUtil {
    private static final String BASE_DIR;
    private static final String ARTICLES_DIR;
    private static final LogUtil logUtil = LogUtil.getInstance();

    static {
        // 判断是否为生产环境
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("linux")) {
            // 生产环境
            BASE_DIR = "/var";
            ARTICLES_DIR = "articles";
            logUtil.log("MARKDOWN_INIT", "system", "检测到Linux环境，使用生产环境路径");
        } else {
            // 开发环境
            BASE_DIR = "C:\\Users\\Lockhart\\Desktop";
            ARTICLES_DIR = "articles";
            logUtil.log("MARKDOWN_INIT", "system", "检测到非Linux环境，使用开发环境路径");
        }
    }

    /**
     * 获取Markdown文件的完整路径
     * @param postId 文章ID
     * @param contentUrl 内容URL
     * @return Markdown文件的路径
     */
    public static String getMarkdownFilePath(Integer postId, String contentUrl) {
        logUtil.log("MARKDOWN_PATH", "system", "计算Markdown路径 - 文章ID: " + postId + ", 内容URL: " + contentUrl);

        if (contentUrl == null || contentUrl.trim().isEmpty()) {
            // 如果contentUrl为空，则使用旧的方式作为备用
            String fileName = postId + ".md";
            String path = Paths.get(BASE_DIR, ARTICLES_DIR, fileName).toString();
            logUtil.log("MARKDOWN_PATH", "system", "内容URL为空，使用默认文件名: " + fileName + ", 完整路径: " + path);
            return path;
        }

        // 判断是否为绝对路径
        if (Paths.get(contentUrl).isAbsolute()) {
            logUtil.log("MARKDOWN_PATH", "system", "使用绝对路径: " + contentUrl);
            return contentUrl;
        } else {
            // 如果是相对路径，则基于BASE_DIR解析
            String path = Paths.get(BASE_DIR, ARTICLES_DIR, contentUrl).toString();
            logUtil.log("MARKDOWN_PATH", "system", "使用相对路径: " + contentUrl + ", 完整路径: " + path);
            return path;
        }
    }

    /**
     * 从指定路径读取Markdown文件内容
     * @param filePath 文件路径
     * @return Markdown文件内容
     */
    public static String readMarkdownContent(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            logUtil.logError("MARKDOWN_READ", "文件路径为空");
            return "";
        }

        long startTime = System.currentTimeMillis();
        logUtil.log("MARKDOWN_READ", "system", "开始读取Markdown文件: " + filePath);

        try {
            Path path = Paths.get(filePath);
            logUtil.log("MARKDOWN_READ", "system", "解析文件路径: " + path.toAbsolutePath());

            // 检查文件存在性
            if (!Files.exists(path)) {
                logUtil.logError("MARKDOWN_READ", "Markdown文件不存在: " + path.toAbsolutePath());
                return "";
            }

            // 检查文件大小
            long fileSize = Files.size(path);
            logUtil.log("MARKDOWN_READ", "system", "Markdown文件大小: " + fileSize + " 字节");

            // 检查文件权限
            if (!Files.isReadable(path)) {
                logUtil.logError("MARKDOWN_READ", "Markdown文件无法读取: " + path.toAbsolutePath());
                return "";
            }

            // 读取文件内容
            byte[] bytes = Files.readAllBytes(path);
            String content = new String(bytes);

            long endTime = System.currentTimeMillis();
            logUtil.log("MARKDOWN_READ", "system", "成功读取Markdown文件，内容长度: " + content.length() +
                    " 字节，耗时: " + (endTime - startTime) + " 毫秒");

            return content;
        } catch (IOException e) {
            logUtil.logError("MARKDOWN_READ", "读取Markdown文件失败: " + e.getMessage() +
                    ", 路径: " + filePath);
            return "";
        } catch (SecurityException e) {
            logUtil.logError("MARKDOWN_READ", "无权限访问Markdown文件: " + e.getMessage() +
                    ", 路径: " + filePath);
            return "";
        } catch (Exception e) {
            logUtil.logError("MARKDOWN_READ", "读取过程中发生未知错误: " + e.getMessage() +
                    ", 路径: " + filePath);
            e.printStackTrace(); // 便于调试
            return "";
        } finally {
            long endTime = System.currentTimeMillis();
            logUtil.log("MARKDOWN_READ", "system", "完成Markdown文件读取尝试，总耗时: " +
                    (endTime - startTime) + " 毫秒");
        }
    }

    /**
     * 根据文章ID读取Markdown文件内容
     * @param postId 文章ID
     * @param contentUrl 内容URL
     * @return Markdown文件内容
     */
    public static String readMarkdownContentById(Integer postId, String contentUrl) {
        logUtil.log("MARKDOWN_READ_BY_ID", "system", "开始根据ID读取Markdown内容 - 文章ID: " + postId + ", 文件名: " + contentUrl);

        if (postId == null) {
            logUtil.logError("MARKDOWN_READ_BY_ID", "文章ID为空");
            return "";
        }

        try {
            String filePath = getMarkdownFilePath(postId, contentUrl);
            logUtil.log("MARKDOWN_READ_BY_ID", "system", "计算Markdown文件路径: " + filePath);

            String content = readMarkdownContent(filePath);
            if (content.isEmpty()) {
                logUtil.logError("MARKDOWN_READ_BY_ID", "读取到的内容为空 - 文章ID: " + postId + ", 文件名: " + contentUrl);
            } else {
                logUtil.log("MARKDOWN_READ_BY_ID", "system", "成功读取Markdown内容 - 文章ID: " + postId +
                        ", 文件名: " + contentUrl + ", 内容长度: " + content.length() + " 字节");
            }

            return content;
        } catch (Exception e) {
            logUtil.logError("MARKDOWN_READ_BY_ID", "读取过程中发生错误: " + e.getMessage());
            return "";
        }
    }

    /**
     * 保存Markdown文件内容
     * @param postId 文章ID
     * @param contentUrl 内容URL
     * @param content Markdown内容
     * @return 是否保存成功
     */
    public static boolean saveMarkdownContent(Integer postId, String contentUrl, String content) {
        logUtil.log("MARKDOWN_SAVE", "system", "开始保存Markdown内容 - 文章ID: " + postId + ", 文件名: " + contentUrl);

        if (postId == null || contentUrl == null) {
            logUtil.logError("MARKDOWN_SAVE", "参数无效 - 文章ID: " + (postId == null ? "缺失" : postId) +
                    ", 文件名: " + (contentUrl == null ? "缺失" : contentUrl));
            return false;
        }

        if (content == null) {
            logUtil.logError("MARKDOWN_SAVE", "内容为空 - 文章ID: " + postId + ", 文件名: " + contentUrl);
            return false;
        }

        try {
            String filePath = getMarkdownFilePath(postId, contentUrl);
            logUtil.log("MARKDOWN_SAVE", "system", "计算Markdown文件保存路径: " + filePath);

            Path path = Paths.get(filePath);

            // 确保目录存在
            try {
                Files.createDirectories(path.getParent());
                logUtil.log("MARKDOWN_SAVE", "system", "确保父目录存在: " + path.getParent().toString());
            } catch (IOException e) {
                logUtil.logError("MARKDOWN_SAVE", "创建目录失败: " + path.getParent() + ", 错误: " + e.getMessage());
                return false;
            }

            // 检查文件是否已存在
            boolean fileExists = Files.exists(path);
            if (fileExists) {
                logUtil.log("MARKDOWN_SAVE", "system", "文件已存在，将覆盖: " + path.toString());
            }

            // 写入文件内容
            try {
                Files.write(path, content.getBytes());
                logUtil.log("MARKDOWN_SAVE", "system", "成功写入文件，内容长度: " + content.length() +
                        " 字节, 路径: " + path.toString());
                return true;
            } catch (IOException e) {
                logUtil.logError("MARKDOWN_SAVE", "写入文件失败: " + path.toString() + ", 错误: " + e.getMessage());
                return false;
            }
        } catch (Exception e) {
            logUtil.logError("MARKDOWN_SAVE", "保存过程中发生未知错误: " + e.getMessage());
            e.printStackTrace(); // 打印堆栈跟踪到控制台，方便调试
            return false;
        }
    }

    /**
     * 处理Markdown内容中的图片路径
     * @param content Markdown内容
     * @param postId 文章ID
     * @return 处理后的Markdown内容
     */
    private static String processImagePaths(String content, Integer postId) {
        if (content == null || postId == null) {
            return content;
        }

        // 替换相对路径的图片引用为正确的URL路径
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("linux")) {
            // Linux环境下使用不带前缀的路径
            return content.replaceAll("!\\[(.*?)\\]\\(\\./img/" + postId + "/(.*?)\\)",
                    "![$1](/img/" + postId + "/$2)");
        } else {
            // 非Linux环境下使用带前缀的路径
            return content.replaceAll("!\\[(.*?)\\]\\(\\./img/" + postId + "/(.*?)\\)",
                    "![$1](/personal_blog_war/img/" + postId + "/$2)");
        }
    }

    /**
     * 加载Markdown文件内容
     * @param contentUrl 文件路径
     * @param postId 文章ID
     * @return Markdown文件内容
     */
    public static String loadMarkdownContent(String contentUrl, Integer postId) throws IOException {
        if (contentUrl == null || contentUrl.isEmpty()) {
            throw new IOException("Markdown文件路径为空");
        }

        // 构建完整的文件路径
        Path filePath = Paths.get(BASE_DIR, ARTICLES_DIR, contentUrl);
        logUtil.log("MARKDOWN_LOAD", "system", "尝试加载Markdown文件: " + filePath);

        // 检查文件是否存在
        if (!Files.exists(filePath)) {
            String errorMsg = "Markdown文件不存在: " + filePath;
            logUtil.logError("MARKDOWN_LOAD", errorMsg);
            throw new IOException(errorMsg);
        }

        // 读取文件内容
        try {
            String content = new String(Files.readAllBytes(filePath));
            // 处理图片路径
            content = processImagePaths(content, postId);
            logUtil.log("MARKDOWN_LOAD", "system", "成功加载Markdown文件: " + filePath);
            return content;
        } catch (IOException e) {
            String errorMsg = "读取Markdown文件失败: " + e.getMessage();
            logUtil.logError("MARKDOWN_LOAD", errorMsg);
            throw new IOException(errorMsg);
        }
    }

    /**
     * 获取基础目录路径
     * @return 基础目录路径
     */
    public static String getBaseDir() {
        return BASE_DIR;
    }

    /**
     * 获取文章目录路径
     * @return 文章目录路径
     */
    public static String getArticlesDir() {
        return ARTICLES_DIR;
    }
}