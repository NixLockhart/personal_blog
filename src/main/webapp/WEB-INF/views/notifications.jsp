<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <!-- 防止主题闪烁 -->
    <script src="${pageContext.request.contextPath}/js/theme-init.js"></script>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>我的通知 - 星光小栈</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/element/website_logo.png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/fonts/remixicon.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .notification-container {
            max-width: 800px;
            margin: 20px auto;
            padding: 20px;
            background-color: var(--bg-primary);
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }

        .notification-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 1px solid var(--border-color);
        }

        .notification-header h1 {
            font-size: 1.5rem;
            margin: 0;
            color: var(--text-primary);
        }

        .notification-actions {
            display: flex;
            gap: 10px;
        }

        .notification-filter {
            margin-bottom: 20px;
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
            padding: 10px;
            background-color: var(--bg-secondary);
            border-radius: 8px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
        }

        .filter-btn {
            padding: 8px 16px;
            background-color: var(--bg-primary);
            border: 1px solid var(--border-color);
            border-radius: 20px;
            cursor: pointer;
            color: var(--text-primary);
            transition: all 0.2s;
            font-size: 0.9rem;
        }

        .filter-btn:hover {
            background-color: var(--bg-hover);
        }

        .filter-btn.active {
            background-color: #2196F3;
            color: white;
            border-color: #2196F3;
        }

        .filter-btn.active:hover {
            background-color: #1976D2;
        }

        .notification-list {
            display: flex;
            flex-direction: column;
            gap: 10px;
        }

        .notification-item {
            padding: 15px;
            background-color: var(--bg-primary);
            border-radius: 8px;
            border-left: 3px solid transparent;
            transition: all 0.2s;
            position: relative;
            display: flex;
            flex-direction: column;
            margin-bottom: 10px;
            box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
            border: 1px solid var(--border-color);
        }

        .notification-item .btn {
            align-self: flex-start;
            margin-top: 15px;
            display: inline-flex;
            align-items: center;
            gap: 5px;
        }

        .notification-item:hover {
            transform: translateX(2px);
            box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15);
            border-color: var(--primary-color);
        }

        .notification-item.unread {
            border-left-color: #f44336;
            background-color: var(--bg-secondary);
            border: 1px solid rgba(244, 67, 54, 0.2);
        }

        .notification-item.unread:before {
            content: '';
            position: absolute;
            top: 15px;
            right: 15px;
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background-color: #f44336;
            box-shadow: 0 0 0 2px var(--bg-secondary);
        }

        .notification-type {
            position: absolute;
            top: 10px;
            left: 10px;
            font-size: 0.8rem;
            padding: 4px 10px;
            border-radius: 12px;
            display: inline-block;
            color: white;
            z-index: 1;
            font-weight: 500;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
        }

        .notification-type.system {
            background-color: #2196F3;
        }

        .notification-type.announcement {
            background-color: #4CAF50;
        }

        .notification-type.like {
            background-color: #E91E63;
        }

        .notification-type.comment {
            background-color: #FF9800;
        }

        .notification-type.reply {
            background-color: #9C27B0;
        }

        .notification-user {
            display: flex;
            align-items: center;
            margin-bottom: 5px;
            margin-top: 30px;
            position: relative;
            z-index: 0;
            padding: 5px;
            background-color: var(--bg-hover);
            border-radius: 6px;
        }

        .notification-avatar {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            margin-right: 10px;
            object-fit: cover;
            border: 2px solid var(--border-color);
        }

        .notification-title {
            font-weight: 600;
            color: var(--text-primary);
            margin: 5px 0;
            margin-top: 20px;
            font-size: 1.1rem;
        }

        .notification-content {
            color: var(--text-secondary);
            margin: 5px 0;
            font-size: 0.95rem;
            line-height: 1.5;
        }

        .notification-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 0.8rem;
            color: var(--text-tertiary);
            margin-top: 15px;
            padding-top: 10px;
            border-top: 1px solid var(--border-color);
        }

        .notification-actions-item {
            display: flex;
            gap: 10px;
        }

        .notification-action {
            cursor: pointer;
            color: var(--text-tertiary);
            transition: all 0.2s;
            padding: 4px 8px;
            border-radius: 4px;
        }

        .notification-action:hover {
            color: var(--primary-color);
            background-color: var(--bg-hover);
        }

        .notification-username {
            font-weight: 600;
            color: var(--text-primary);
        }

        .notification-empty {
            text-align: center;
            padding: 30px;
            color: var(--text-tertiary);
        }

        .btn {
            padding: 8px 16px;
            border-radius: 4px;
            cursor: pointer;
            font-size: 0.9rem;
            transition: all 0.2s;
            background-color: var(--bg-secondary);
            color: var(--text-primary);
            border: 1px solid var(--border-color);
        }

        .btn-primary {
            background-color: #2196F3;
            color: white;
            border: none;
        }

        .btn-danger {
            background-color: #f44336;
            color: white;
            border: none;
        }

        .btn:hover {
            opacity: 0.9;
        }

        @media (max-width: 768px) {
            .notification-container {
                padding: 15px;
                margin: 10px;
            }

            .notification-header {
                flex-direction: column;
                align-items: flex-start;
                gap: 10px;
            }

            .notification-actions {
                width: 100%;
                justify-content: space-between;
            }
        }
    </style>
</head>
<body>
<!-- 导航栏 -->
<jsp:include page="/WEB-INF/components/header.jsp" />

<!-- 主要内容 -->
<div class="container">
    <div class="notification-container">
        <div class="notification-header">
            <h1><i class="ri-notification-3-line"></i> 我的通知</h1>
            <div class="notification-actions">
                <button id="markAllReadBtn" class="btn"><i class="ri-check-double-line"></i> 全部标为已读</button>
                <button id="deleteAllBtn" class="btn btn-danger"><i class="ri-delete-bin-line"></i> 清空通知</button>
            </div>
        </div>

        <div class="notification-filter">
            <button class="filter-btn active" data-filter="all">全部</button>
            <button class="filter-btn" data-filter="unread">未读</button>
            <button class="filter-btn" data-filter="system">系统通知</button>
            <button class="filter-btn" data-filter="announcement">公告</button>
            <button class="filter-btn" data-filter="like">点赞</button>
            <button class="filter-btn" data-filter="comment">评论</button>
            <button class="filter-btn" data-filter="reply">回复</button>
        </div>

        <div class="notification-list">
            <c:choose>
                <c:when test="${empty notifications}">
                    <div class="notification-empty">
                        <i class="ri-inbox-line" style="font-size: 2rem;"></i>
                        <p>暂无通知</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <c:forEach var="notification" items="${notifications}">
                        <div class="notification-item ${notification.isRead ? '' : 'unread'}"
                             data-id="${notification.id}"
                             data-type="${notification.type}"
                             data-read="${notification.isRead}">

                            <c:if test="${notification.type == 'like' || notification.type == 'comment' || notification.type == 'reply'}">
                                <div class="notification-user">
                                    <img src="${pageContext.request.contextPath}${not empty notification.fromUserAvatarUrl ? notification.fromUserAvatarUrl : '/avatar/default.png'}?t=${sessionScope.lastAvatarUpdate}"
                                         onerror="this.src='${pageContext.request.contextPath}/avatar/default.png'"
                                         alt="${notification.fromUsername}"
                                         class="notification-avatar">
                                    <span class="notification-username">${notification.fromUsername}</span>
                                </div>
                            </c:if>

                            <span class="notification-type ${notification.type}">
                                <c:choose>
                                    <c:when test="${notification.type == 'system'}">系统通知</c:when>
                                    <c:when test="${notification.type == 'announcement'}">公告</c:when>
                                    <c:when test="${notification.type == 'like'}">点赞</c:when>
                                    <c:when test="${notification.type == 'comment'}">评论</c:when>
                                    <c:when test="${notification.type == 'reply'}">回复</c:when>
                                    <c:otherwise>${notification.type}</c:otherwise>
                                </c:choose>
                            </span>

                            <h3 class="notification-title">${notification.title}</h3>
                            <p class="notification-content">${notification.content}</p>

                            <div class="notification-meta">
                                <span><fmt:formatDate value="${notification.createdAt}" pattern="yyyy-MM-dd HH:mm:ss" /></span>
                                <div class="notification-actions-item">
                                    <c:if test="${!notification.isRead}">
                                        <span class="notification-action mark-read" data-id="${notification.id}">
                                            <i class="ri-check-line"></i> 标为已读
                                        </span>
                                    </c:if>
                                    <span class="notification-action delete-notification" data-id="${notification.id}">
                                        <i class="ri-delete-bin-line"></i> 删除
                                    </span>
                                </div>
                            </div>

                            <c:if test="${notification.postId != null}">
                                <a href="${pageContext.request.contextPath}/posts/${notification.postId}"
                                   class="btn" style="margin-top: 10px;">
                                    <i class="ri-link"></i> 查看相关文章
                                </a>
                            </c:if>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<!-- 页脚 -->
<jsp:include page="/WEB-INF/components/footer.jsp" />

<script>
    document.addEventListener('DOMContentLoaded', function() {
        // 过滤功能
        const filterBtns = document.querySelectorAll('.filter-btn');
        const notificationItems = document.querySelectorAll('.notification-item');

        filterBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                // 移除所有活跃状态
                filterBtns.forEach(b => b.classList.remove('active'));
                // 设置当前按钮为活跃状态
                this.classList.add('active');

                const filter = this.getAttribute('data-filter');

                notificationItems.forEach(item => {
                    if (filter === 'all') {
                        item.style.display = 'block';
                    } else if (filter === 'unread') {
                        item.style.display = item.classList.contains('unread') ? 'block' : 'none';
                    } else {
                        item.style.display = item.getAttribute('data-type') === filter ? 'block' : 'none';
                    }
                });
            });
        });

        // 标记为已读
        const markReadBtns = document.querySelectorAll('.mark-read');
        markReadBtns.forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.stopPropagation();
                const id = this.getAttribute('data-id');
                // 立即移除按钮
                this.remove();
                markAsRead(id);
            });
        });

        // 删除通知
        const deleteButtons = document.querySelectorAll('.delete-notification');
        deleteButtons.forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.stopPropagation();
                const id = this.getAttribute('data-id');
                const item = document.querySelector(`.notification-item[data-id="${id}"]`);
                if (item) {
                    // 立即移除通知卡片
                    item.style.transition = 'all 0.3s ease';
                    item.style.opacity = '0';
                    item.style.transform = 'translateX(-20px)';
                    setTimeout(() => {
                        item.remove();
                        // 检查是否为空
                        checkIfEmpty();
                        // 更新通知计数
                        updateNotificationCount();
                    }, 300);
                }
                deleteNotification(id);
            });
        });

        // 全部标为已读
        document.getElementById('markAllReadBtn').addEventListener('click', function() {
            if (confirm('确定要将所有通知标记为已读吗？')) {
                markAllAsRead();
            }
        });

        // 清空通知
        document.getElementById('deleteAllBtn').addEventListener('click', function() {
            if (confirm('确定要删除所有通知吗？此操作不可恢复！')) {
                deleteAllNotifications();
            }
        });

        // 标记单个通知为已读
        function markAsRead(id) {
            console.log('开始标记通知为已读:', id);
            const baseUrl = '${pageContext.request.contextPath}';
            const url = baseUrl + '/notifications/read/' + id;
            console.log('请求URL:', url);

            fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                credentials: 'same-origin'
            })
                .then(response => {
                    console.log('服务器响应状态:', response.status);
                    console.log('服务器响应头:', response.headers);
                    if (!response.ok) {
                        return response.text().then(text => {
                            console.error('服务器响应内容:', text);
                            throw new Error(`HTTP error! status: ${response.status}, response: ${text}`);
                        });
                    }
                    return response.json();
                })
                .then(data => {
                    console.log('服务器返回数据:', data);
                    if (data.success) {
                        // 标记成功后重新加载页面
                        window.location.reload();
                    } else {
                        throw new Error(data.message || '标记已读失败');
                    }
                })
                .catch(error => {
                    console.error('标记已读失败:', error);
                    console.error('错误详情:', error.stack);
                    alert('标记已读失败: ' + error.message);
                });
        }

        // 全部标为已读
        function markAllAsRead() {
            fetch(`${pageContext.request.contextPath}/notifications/read-all`, {
                method: 'POST'
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        // 更新所有未读通知的UI
                        document.querySelectorAll('.notification-item.unread').forEach(item => {
                            item.classList.remove('unread');
                            item.setAttribute('data-read', 'true');
                            const readBtn = item.querySelector('.mark-read');
                            if (readBtn) {
                                readBtn.remove();
                            }
                        });

                        // 更新通知计数
                        updateNotificationCount();
                    }
                })
                .catch(error => console.error('全部标为已读失败:', error));
        }

        // 删除单个通知
        function deleteNotification(id) {
            console.log('开始删除通知:', id);
            const baseUrl = '${pageContext.request.contextPath}';
            const url = baseUrl + '/notifications/delete/' + id;
            console.log('请求URL:', url);

            fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                credentials: 'same-origin'
            })
                .then(response => {
                    console.log('服务器响应状态:', response.status);
                    console.log('服务器响应头:', response.headers);
                    if (!response.ok) {
                        // 如果请求失败，恢复UI状态
                        const item = document.querySelector(`.notification-item[data-id="${id}"]`);
                        if (item) {
                            item.style.opacity = '1';
                            item.style.transform = 'translateX(0)';
                        }
                        return response.text().then(text => {
                            console.error('服务器响应内容:', text);
                            throw new Error(`HTTP error! status: ${response.status}, response: ${text}`);
                        });
                    }
                    return response.json();
                })
                .then(data => {
                    console.log('服务器返回数据:', data);
                    if (!data.success) {
                        // 如果操作失败，恢复UI状态
                        const item = document.querySelector(`.notification-item[data-id="${id}"]`);
                        if (item) {
                            item.style.opacity = '1';
                            item.style.transform = 'translateX(0)';
                        }
                        throw new Error(data.message || '删除通知失败');
                    }
                    // 删除成功后重新加载通知列表
                    window.location.reload();
                })
                .catch(error => {
                    console.error('删除通知失败:', error);
                    console.error('错误详情:', error.stack);
                    alert('删除通知失败: ' + error.message);
                });
        }

        // 删除所有通知
        function deleteAllNotifications() {
            fetch(`${pageContext.request.contextPath}/notifications/delete-all`, {
                method: 'POST'
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        // 清空通知列表
                        const list = document.querySelector('.notification-list');
                        list.innerHTML = `
                        <div class="notification-empty">
                            <i class="ri-inbox-line" style="font-size: 2rem;"></i>
                            <p>暂无通知</p>
                        </div>
                    `;

                        // 更新通知计数
                        updateNotificationCount();
                    }
                })
                .catch(error => console.error('清空通知失败:', error));
        }

        // 更新通知计数（如果头部有显示的话）
        function updateNotificationCount() {
            fetch(`${pageContext.request.contextPath}/notifications/count`)
                .then(response => response.json())
                .then(data => {
                    const notificationCounter = document.getElementById('notificationCounter');
                    if (notificationCounter) {
                        notificationCounter.textContent = data.count;
                        notificationCounter.style.display = data.count > 0 ? 'block' : 'none';
                    }
                })
                .catch(error => console.error('获取通知数量失败:', error));
        }

        // 检查通知列表是否为空
        function checkIfEmpty() {
            const items = document.querySelectorAll('.notification-item');
            if (items.length === 0) {
                const list = document.querySelector('.notification-list');
                list.innerHTML = `
                    <div class="notification-empty">
                        <i class="ri-inbox-line" style="font-size: 2rem;"></i>
                        <p>暂无通知</p>
                    </div>
                `;
            }
        }
    });
</script>
</body>
</html>