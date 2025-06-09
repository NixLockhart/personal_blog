/*
 Navicat Premium Dump SQL

 Source Server         : MySQL_Local
 Source Server Type    : MySQL
 Source Server Version : 80401 (8.4.1)
 Source Host           : localhost:3306
 Source Schema         : db_blog

 Target Server Type    : MySQL
 Target Server Version : 80401 (8.4.1)
 File Encoding         : 65001

 Date: 09/06/2025 22:01:26
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_categories
-- ----------------------------
DROP TABLE IF EXISTS `tb_categories`;
CREATE TABLE `tb_categories`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_categories
-- ----------------------------
INSERT INTO `tb_categories` VALUES (1, '技术', '2025-03-07 20:50:31', '2025-03-07 20:50:31');
INSERT INTO `tb_categories` VALUES (2, '生活', '2025-03-07 20:50:31', '2025-03-07 20:50:31');
INSERT INTO `tb_categories` VALUES (3, '旅行', '2025-03-07 20:50:31', '2025-03-07 20:50:31');
INSERT INTO `tb_categories` VALUES (4, '美食', '2025-03-07 20:50:31', '2025-03-07 20:50:31');

-- ----------------------------
-- Table structure for tb_comments
-- ----------------------------
DROP TABLE IF EXISTS `tb_comments`;
CREATE TABLE `tb_comments`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `post_id` int NULL DEFAULT NULL,
  `user_id` int NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `parent_id` int NULL DEFAULT NULL COMMENT '父评论ID，为空表示一级评论',
  `reply_to_user_id` int NULL DEFAULT NULL COMMENT '回复的用户ID，为空表示对博文的评论',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `post_id`(`post_id` ASC) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `reply_to_user_id`(`reply_to_user_id` ASC) USING BTREE,
  CONSTRAINT `tb_comments_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `tb_posts` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `tb_comments_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `tb_users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `tb_comments_ibfk_3` FOREIGN KEY (`parent_id`) REFERENCES `tb_comments` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `tb_comments_ibfk_4` FOREIGN KEY (`reply_to_user_id`) REFERENCES `tb_users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_comments
-- ----------------------------
INSERT INTO `tb_comments` VALUES (1, 1, 1, '你好', NULL, NULL, '2025-04-20 10:43:39');
INSERT INTO `tb_comments` VALUES (2, 1, 5, '你好', NULL, NULL, '2025-04-20 10:43:59');

-- ----------------------------
-- Table structure for tb_like_records
-- ----------------------------
DROP TABLE IF EXISTS `tb_like_records`;
CREATE TABLE `tb_like_records`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `post_id` int NOT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_post_unique`(`user_id` ASC, `post_id` ASC) USING BTREE,
  INDEX `tb_like_records_ibfk_2`(`post_id` ASC) USING BTREE,
  CONSTRAINT `tb_like_records_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `tb_users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `tb_like_records_ibfk_2` FOREIGN KEY (`post_id`) REFERENCES `tb_posts` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_like_records
-- ----------------------------

-- ----------------------------
-- Table structure for tb_messages
-- ----------------------------
DROP TABLE IF EXISTS `tb_messages`;
CREATE TABLE `tb_messages`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NULL DEFAULT NULL COMMENT '用户ID，NULL表示游客留言',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `parent_id` int NULL DEFAULT NULL COMMENT '父留言ID，用于回复功能',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '游客头像URL',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'IP地址，用于游客标识',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0未读，1已读',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  CONSTRAINT `tb_messages_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `tb_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `tb_messages_ibfk_2` FOREIGN KEY (`parent_id`) REFERENCES `tb_messages` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_messages
-- ----------------------------

-- ----------------------------
-- Table structure for tb_notifications
-- ----------------------------
DROP TABLE IF EXISTS `tb_notifications`;
CREATE TABLE `tb_notifications`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '接收通知的用户ID',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通知类型：system(系统通知), announcement(网站公告), like(点赞), comment(评论), reply(回复)',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '通知内容',
  `source_id` int NULL DEFAULT NULL COMMENT '通知来源ID（如评论ID，帖子ID等）',
  `post_id` int NULL DEFAULT NULL COMMENT '关联的文章ID',
  `from_user_id` int NULL DEFAULT NULL COMMENT '通知来源用户ID（如点赞用户、评论用户）',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读：0未读，1已读',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  CONSTRAINT `tb_notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `tb_users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_notifications
-- ----------------------------

-- ----------------------------
-- Table structure for tb_posts
-- ----------------------------
DROP TABLE IF EXISTS `tb_posts`;
CREATE TABLE `tb_posts`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `category_id` int NULL DEFAULT NULL,
  `published_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `likes` int NULL DEFAULT 0,
  `views` int NULL DEFAULT NULL,
  `user_id` int NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `category_id`(`category_id` ASC) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `tb_posts_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `tb_categories` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `tb_posts_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `tb_users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_posts
-- ----------------------------
INSERT INTO `tb_posts` VALUES (1, 'Markdown测试', '这是一篇Markdown测试文章', 'Markdown.md', NULL, 1, '2025-03-07 20:50:31', 1, 6, 1, '2025-03-07 20:50:31', '2025-06-08 22:55:47');
INSERT INTO `tb_posts` VALUES (2, 'Latex测试', '这是单纯的Latex测试', 'Latex.md', NULL, 1, '2025-03-14 19:38:14', 0, 4, 1, '2025-03-14 19:38:14', '2025-06-09 10:44:46');
INSERT INTO `tb_posts` VALUES (3, '《算法与数据结构》第一章：数据结构', '学！狠狠学！！！', 'Data-st.md', NULL, 1, '2025-04-05 22:45:51', 0, 2, 1, '2023-11-30 22:45:51', '2025-06-09 11:05:37');

-- ----------------------------
-- Table structure for tb_users
-- ----------------------------
DROP TABLE IF EXISTS `tb_users`;
CREATE TABLE `tb_users`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `salt` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户头像文件路径',
  `birthday` date NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_users
-- ----------------------------
INSERT INTO `tb_users` VALUES (1, 'admin', 'giijORN/TKEeBU4dNds9LKow6zl/9ZRkwP1uFQkRXuw=', 'z//PnNP4rFYi4ykVJlutcw==', '1@1.com', '2025-04-07 18:56:04', '2025-06-09 22:00:40', '1.jpg', '2004-04-22');
INSERT INTO `tb_users` VALUES (5, 'user', 'GsekmOIKsi+OgR1k8zw2TQpn4q+NNIXFt7jsMXeE83E=', 'Q1fI4stsdRwpeHLVHbk4qA==', '1@12.com', '2025-04-11 19:19:27', '2025-06-09 22:00:43', '5.jpg', NULL);

-- ----------------------------
-- Table structure for tb_verification_codes
-- ----------------------------
DROP TABLE IF EXISTS `tb_verification_codes`;
CREATE TABLE `tb_verification_codes`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `code` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `expires_at` timestamp NOT NULL,
  `is_used` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_email_code`(`email` ASC, `code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_verification_codes
-- ----------------------------

-- ----------------------------
-- Table structure for tb_view_records
-- ----------------------------
DROP TABLE IF EXISTS `tb_view_records`;
CREATE TABLE `tb_view_records`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `visitor_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '访客ID（登录用户为user_id，未登录用户为cookie标识）',
  `post_id` int NOT NULL,
  `view_date` date NOT NULL COMMENT '浏览日期',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `visitor_post_date_unique`(`visitor_id` ASC, `post_id` ASC, `view_date` ASC) USING BTREE,
  INDEX `tb_view_records_ibfk_1`(`post_id` ASC) USING BTREE,
  CONSTRAINT `tb_view_records_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `tb_posts` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_view_records
-- ----------------------------

-- ----------------------------
-- Table structure for tb_visits
-- ----------------------------
DROP TABLE IF EXISTS `tb_visits`;
CREATE TABLE `tb_visits`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '访客浏览器标识',
  `visit_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_agent_unique`(`user_agent` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_visits
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
