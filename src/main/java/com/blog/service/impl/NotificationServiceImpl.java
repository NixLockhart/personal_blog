package com.blog.service.impl;

import com.blog.dao.NotificationDao;
import com.blog.dao.UserDao;
import com.blog.dao.PostDao;
import com.blog.dao.impl.NotificationDaoImpl;
import com.blog.dao.impl.UserDaoImpl;
import com.blog.dao.impl.PostDaoImpl;
import com.blog.model.Notification;
import com.blog.model.User;
import com.blog.service.NotificationService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationServiceImpl implements NotificationService {
    private final NotificationDao notificationDao;
    private final UserDao userDao;
    private final PostDao postDao;

    public NotificationServiceImpl() {
        this.notificationDao = new NotificationDaoImpl();
        this.userDao = new UserDaoImpl();
        this.postDao = new PostDaoImpl();
    }

    @Override
    public boolean createNotification(Notification notification) {
        try {
            int result = notificationDao.insert(notification);
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean createSystemNotification(Integer userId, String title, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(Notification.TYPE_SYSTEM);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(false);

        return createNotification(notification);
    }

    @Override
    public boolean createAnnouncementForAllUsers(String title, String content) {
        try {
            List<User> allUsers = userDao.findAll();
            List<Notification> notifications = new ArrayList<>();

            for (User user : allUsers) {
                Notification notification = new Notification();
                notification.setUserId(user.getId());
                notification.setType(Notification.TYPE_ANNOUNCEMENT);
                notification.setTitle(title);
                notification.setContent(content);
                notification.setIsRead(false);
                notifications.add(notification);
            }

            int result = notificationDao.batchInsert(notifications);
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean createLikeNotification(Integer toUserId, Integer fromUserId, Integer postId) {
        try {
            // 不要给自己发通知
            if (toUserId.equals(fromUserId)) {
                return true;
            }

            User fromUser = userDao.findById(fromUserId);
            String postTitle = postDao.findById(postId).getTitle();

            Notification notification = new Notification();
            notification.setUserId(toUserId);
            notification.setType(Notification.TYPE_LIKE);
            notification.setTitle("收到新的点赞");
            notification.setContent(fromUser.getUsername() + " 赞了你的文章《" + postTitle + "》");
            notification.setPostId(postId);
            notification.setFromUserId(fromUserId);
            notification.setIsRead(false);

            return createNotification(notification);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean createCommentNotification(Integer toUserId, Integer fromUserId, Integer postId, Integer commentId, String commentContent) {
        try {
            // 不要给自己发通知
            if (toUserId.equals(fromUserId)) {
                return true;
            }

            User fromUser = userDao.findById(fromUserId);
            String postTitle = postDao.findById(postId).getTitle();

            Notification notification = new Notification();
            notification.setUserId(toUserId);
            notification.setType(Notification.TYPE_COMMENT);
            notification.setTitle("收到新的评论");
            notification.setContent(fromUser.getUsername() + " 评论了你的文章《" + postTitle + "》: " + commentContent);
            notification.setPostId(postId);
            notification.setSourceId(commentId);
            notification.setFromUserId(fromUserId);
            notification.setIsRead(false);

            return createNotification(notification);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean createReplyNotification(Integer toUserId, Integer fromUserId, Integer postId, Integer commentId, String replyContent) {
        try {
            // 不要给自己发通知
            if (toUserId.equals(fromUserId)) {
                return true;
            }

            User fromUser = userDao.findById(fromUserId);
            String postTitle = postDao.findById(postId).getTitle();

            Notification notification = new Notification();
            notification.setUserId(toUserId);
            notification.setType(Notification.TYPE_REPLY);
            notification.setTitle("收到评论回复");
            notification.setContent(fromUser.getUsername() + " 回复了你在《" + postTitle + "》的评论: " + replyContent);
            notification.setPostId(postId);
            notification.setSourceId(commentId);
            notification.setFromUserId(fromUserId);
            notification.setIsRead(false);

            return createNotification(notification);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean markAsRead(Integer notificationId) {
        try {
            int result = notificationDao.updateReadStatus(notificationId, true);
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean markAllAsRead(Integer userId) {
        try {
            int result = notificationDao.markAllAsRead(userId);
            return result >= 0; // 即使没有通知被更新也算成功
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Notification> getUserNotifications(Integer userId) {
        try {
            return notificationDao.findByUserId(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<Notification> getUserUnreadNotifications(Integer userId) {
        try {
            return notificationDao.findUnreadByUserId(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<Notification> getUserNotificationsByType(Integer userId, String type) {
        try {
            return notificationDao.findByUserIdAndType(userId, type);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public int getUnreadNotificationCount(Integer userId) {
        try {
            return notificationDao.countUnreadByUserId(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean deleteNotification(Integer notificationId) {
        try {
            int result = notificationDao.deleteById(notificationId);
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteAllUserNotifications(Integer userId) {
        try {
            int result = notificationDao.deleteAllByUserId(userId);
            return result >= 0; // 即使没有通知被删除也算成功
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Notification getNotificationById(Integer id) {
        try {
            return notificationDao.findById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}