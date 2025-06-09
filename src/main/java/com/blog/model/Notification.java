package com.blog.model;

import java.util.Date;

public class Notification {
    private Integer id;
    private Integer userId;
    private String type;
    private String title;
    private String content;
    private Integer sourceId;
    private Integer postId;
    private Integer fromUserId;
    private Boolean isRead;
    private Date createdAt;

    // 额外的非数据库字段
    private String fromUsername;
    private String postTitle;
    private String fromUserAvatarUrl;
    private String recipientUsername;

    // 通知类型常量
    public static final String TYPE_SYSTEM = "system";
    public static final String TYPE_ANNOUNCEMENT = "announcement";
    public static final String TYPE_LIKE = "like";
    public static final String TYPE_COMMENT = "comment";
    public static final String TYPE_REPLY = "reply";

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getSourceId() {
        return sourceId;
    }

    public void setSourceId(Integer sourceId) {
        this.sourceId = sourceId;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public Integer getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Integer fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getFromUserAvatarUrl() {
        return fromUserAvatarUrl;
    }

    public void setFromUserAvatarUrl(String fromUserAvatarUrl) {
        this.fromUserAvatarUrl = fromUserAvatarUrl;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }
}