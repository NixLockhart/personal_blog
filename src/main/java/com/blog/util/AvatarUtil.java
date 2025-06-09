package com.blog.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.servlet.http.Part;

public class AvatarUtil {
    private static final String AVATAR_DIR = "avatar";
    private static final LogUtil logUtil = LogUtil.getInstance();

    /**
     * 获取头像存储的基础路径，根据环境进行区分
     * @return 头像存储基础路径
     */
    public static String getAvatarBasePath() {
        // 首先检查环境变量
        String customPath = System.getenv("AVATAR_BASE_PATH");
        if (customPath != null && !customPath.trim().isEmpty()) {
            return customPath;
        }

        // 如果没有设置环境变量，则根据系统和环境判断
        String environment = System.getProperty("app.environment", "dev");
        String osName = System.getProperty("os.name").toLowerCase();

        if ("prod".equals(environment) || osName.contains("linux")) {
            // 生产环境
            return "/var/avatar";
        } else {
            // 开发环境 - 使用指定的固定路径
            return "C:\\Users\\Lockhart\\Desktop\\avatar";
        }
    }

    /**
     * 处理头像URL，确保返回正确的访问路径
     * @param avatarFileName 头像文件名
     * @return 处理后的头像URL
     */
    public static String processAvatarUrl(String avatarFileName) {
        if (avatarFileName == null || avatarFileName.isEmpty()) {
            // 如果头像URL为空，返回默认头像路径
            return "/avatar/default.png";
        }

        return "/avatar/" + avatarFileName;
    }

    /**
     * 获取头像文件扩展名
     * @param part 文件上传Part对象
     * @return 文件扩展名（.jpg或.png）
     */
    public static String getExtension(Part part) {
        if (part == null) {
            return ".jpg"; // 默认扩展名
        }

        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition == null) {
            return ".jpg"; // 默认扩展名
        }

        String[] elements = contentDisposition.split(";");
        for (String element : elements) {
            element = element.trim();
            if (element.startsWith("filename")) {
                String filename = element.substring(element.indexOf('=') + 1).replace("\"", "");
                if (filename == null || filename.isEmpty()) {
                    return ".jpg"; // 文件名为空
                }

                int dotIndex = filename.lastIndexOf('.');
                if (dotIndex > 0) {
                    String ext = filename.substring(dotIndex).toLowerCase();
                    // 只允许.jpg, .jpeg和.png扩展名
                    if (ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png")) {
                        return ext;
                    }
                }
                break; // 找到文件名元素后退出
            }
        }

        return ".jpg"; // 默认扩展名
    }

    /**
     * 获取用户头像的完整路径
     * @param userId 用户ID
     * @param fileExtension 文件扩展名（jpg或png）
     * @return 头像文件的路径
     */
    public static String getAvatarPath(Integer userId, String fileExtension) {
        if (userId == null || fileExtension == null) {
            return null;
        }
        String fileName = userId + "." + fileExtension.toLowerCase();
        return Paths.get(getAvatarBasePath(), fileName).toString();
    }

    /**
     * 保存用户头像
     * @param userId 用户ID
     * @param avatarPart 上传的头像文件Part
     * @return 头像文件的相对路径（成功）或null（失败）
     */
    public static String saveAvatar(Integer userId, Part avatarPart) {
        if (userId == null || avatarPart == null || avatarPart.getSize() == 0) {
            logUtil.logError("AVATAR_SAVE", "参数无效或文件为空");
            return null;
        }

        // 获取文件扩展名
        String fileExtension = getExtension(avatarPart);

        // 记录文件信息
        logUtil.log("AVATAR_SAVE", "system", String.format("开始保存头像 - 用户ID: %d, 文件大小: %d, 扩展名: %s",
                userId, avatarPart.getSize(), fileExtension));

        try {
            // 确保头像目录存在
            File avatarDir = new File(getAvatarBasePath());
            if (!avatarDir.exists()) {
                boolean created = avatarDir.mkdirs();
                logUtil.log("AVATAR_SAVE", "system", "创建头像目录: " + getAvatarBasePath() + ", 结果: " + created);
            }

            // 检查目录权限
            if (!avatarDir.canWrite()) {
                logUtil.logError("AVATAR_SAVE", "头像目录没有写入权限: " + getAvatarBasePath());
                return null;
            }

            // 生成头像文件路径
            String fileName = userId + fileExtension;
            String fullPath = getAvatarBasePath() + File.separator + fileName;
            logUtil.log("AVATAR_SAVE", "system", "头像保存路径: " + fullPath);

            // 尝试删除可能存在的不同格式的旧头像文件
            deleteAvatar(userId);

            // 保存文件
            try {
                avatarPart.write(fullPath);
                logUtil.log("AVATAR_SAVE", "system", "成功保存头像: " + fullPath);
            } catch (IOException e) {
                logUtil.logError("AVATAR_SAVE", "保存头像文件失败: " + e.getMessage());
                return null;
            }

            // 验证文件是否成功保存
            File savedFile = new File(fullPath);
            if (!savedFile.exists() || savedFile.length() == 0) {
                logUtil.logError("AVATAR_SAVE", "头像文件保存后验证失败");
                return null;
            }

            // 复制头像到webapp目录，确保它立即可访问
            copyToWebApp(userId, fileExtension, fullPath);

            // 返回相对路径（只有文件名）
            return fileName;
        } catch (Exception e) {
            logUtil.logError("AVATAR_SAVE", "保存头像失败: " + e.getMessage());
            e.printStackTrace(); // 添加堆栈跟踪
            return null;
        }
    }

    /**
     * 将头像文件复制到webapp目录，确保它可以立即通过Web访问
     * @param userId 用户ID
     * @param fileExtension 文件扩展名
     * @param sourcePath 源文件路径
     */
    private static void copyToWebApp(Integer userId, String fileExtension, String sourcePath) {
        try {
            // 获取Tomcat webapps目录下的头像目录路径
            String Path = getAvatarBasePath();
            Path targetDir = Paths.get(Path);

            // 确保目录存在
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
                logUtil.log("AVATAR_COPY", "system", "创建目标目录: " + targetDir);
            }

            // 复制文件
            Path sourceFile = Paths.get(sourcePath);
            Path targetFile = targetDir.resolve(userId + fileExtension);
            Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);

            logUtil.log("AVATAR_COPY", "system", "成功复制头像到webapp目录: " + targetFile.toString());
        } catch (Exception e) {
            logUtil.logError("AVATAR_COPY", "复制头像到webapp目录失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户头像
     * @param userId 用户ID
     * @return 是否删除成功
     */
    public static boolean deleteAvatar(Integer userId) {
        try {
            // 尝试删除jpg和png格式的头像
            Path jpgPath = Paths.get(getAvatarPath(userId, "jpg"));
            Path pngPath = Paths.get(getAvatarPath(userId, "png"));

            boolean deleted = false;
            if (Files.exists(jpgPath)) {
                Files.delete(jpgPath);
                deleted = true;
            }
            if (Files.exists(pngPath)) {
                Files.delete(pngPath);
                deleted = true;
            }

            if (deleted) {
                logUtil.log("AVATAR_DELETE", "system", "成功删除用户头像: " + userId);
            }
            return deleted;
        } catch (IOException e) {
            logUtil.logError("AVATAR_DELETE", "删除头像失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取基础目录路径
     * @return 基础目录路径
     */
    public static String getBaseDir() {
        return getAvatarBasePath();
    }

    /**
     * 获取头像目录路径
     * @return 头像目录路径
     */
    public static String getAvatarDir() {
        return AVATAR_DIR;
    }
}