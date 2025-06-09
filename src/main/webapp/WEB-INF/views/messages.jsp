<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <!-- 防止主题闪烁 -->
    <script src="${pageContext.request.contextPath}/js/theme-init.js"></script>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>留言板 - 个人博客</title>
    <!-- 添加网站logo -->
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/element/website_logo.png">
    <!-- 使用本地Remixicon资源替代CDN -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/fonts/remixicon.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .chat-container {
            max-width: 800px;
            margin: 30px auto;
            padding: 20px;
            background: var(--card-bg);
            border-radius: 15px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
        }

        .chat-header {
            text-align: center;
            margin-bottom: 30px;
            padding-bottom: 20px;
            border-bottom: 2px solid var(--border-color);
        }

        .chat-header h1 {
            color: var(--accent-color);
            font-size: 2rem;
            margin-bottom: 10px;
        }

        .chat-messages {
            max-height: 600px;
            overflow-y: auto;
            padding: 20px;
            margin-bottom: 20px;
            background: var(--bg-color);
            border-radius: 10px;
            display: flex;
            flex-direction: column;
        }

        .message {
            display: flex;
            margin-bottom: 20px;
            position: relative;
            align-items: flex-start;
            width: 100%;
        }

        .message.own {
            justify-content: flex-end;
        }

        .message.other {
            justify-content: flex-start;
        }

        .user-info-container {
            display: flex;
            flex-direction: column;
            align-items: center;
            min-width: 60px;
            max-width: 60px;
        }

        .user-name {
            font-size: 12px;
            color: var(--text-secondary);
            margin: 5px 0;
            max-width: 60px;
            text-align: center;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .message-avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            object-fit: cover;
            box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
        }

        .message-content {
            max-width: 70%;
            padding: 12px 16px;
            border-radius: 18px;
            position: relative;
            word-break: break-word;
            box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
            margin: 0 10px;
        }

        .message.own .message-content {
            background: var(--accent-color);
            color: white;
            border-top-right-radius: 4px;
            order: 1;
        }

        .message.own .user-info-container {
            order: 2;
        }

        .message.other .message-content {
            background: var(--card-bg);
            color: var(--text-primary);
            border-top-left-radius: 4px;
            order: 2;
        }

        .message.other .user-info-container {
            order: 1;
        }

        .message-info {
            display: flex;
            align-items: center;
            margin-bottom: 5px;
        }

        .message-time {
            font-size: 0.8rem;
            color: var(--text-secondary);
        }

        .message-text {
            line-height: 1.5;
        }

        .message-actions {
            position: absolute;
            top: 0;
            right: 0;
            display: none;
            background: var(--card-bg);
            padding: 5px;
            border-radius: 5px;
            box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
            z-index: 10;
        }

        .message:hover .message-actions {
            display: flex;
        }

        .action-btn {
            background: none;
            border: none;
            color: var(--text-secondary);
            cursor: pointer;
            padding: 5px;
            margin: 0 2px;
            transition: color 0.3s;
            display: flex;
            align-items: center;
            justify-content: center;
            width: 30px;
            height: 30px;
            border-radius: 50%;
        }

        .action-btn:hover {
            color: var(--accent-color);
            background: var(--hover-bg);
        }

        .action-btn.delete-btn:hover {
            color: var(--danger-color);
        }

        .message-form {
            display: flex;
            flex-direction: column;
            gap: 10px;
            padding: 20px;
            background: var(--card-bg);
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            position: relative;
        }

        .message-input-container {
            display: flex;
            gap: 10px;
            width: 100%;
        }

        .message-input {
            flex: 1;
            padding: 12px 20px;
            border: 1px solid var(--border-color);
            border-radius: 25px;
            background: var(--bg-color);
            color: var(--text-primary);
            resize: none;
            min-height: 40px;
            max-height: 120px;
            transition: all 0.3s;
            font-size: 1rem;
        }

        .message-input:focus {
            outline: none;
            border-color: var(--accent-color);
            box-shadow: 0 0 0 2px rgba(var(--accent-rgb), 0.2);
        }

        .send-btn {
            background: var(--accent-color);
            color: white;
            border: none;
            border-radius: 50%;
            width: 45px;
            height: 45px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            transition: all 0.3s;
            box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
        }

        .send-btn:hover {
            background: var(--accent-hover-color);
            transform: translateY(-2px);
        }

        .send-btn i {
            font-size: 1.2rem;
        }

        .visitor-info {
            display: flex;
            gap: 10px;
            margin-bottom: 10px;
            width: 100%;
        }

        .visitor-input {
            flex: 1;
            padding: 8px 15px;
            border: 1px solid var(--border-color);
            border-radius: 20px;
            background: var(--bg-color);
            color: var(--text-primary);
            transition: all 0.3s;
        }

        .visitor-input:focus {
            outline: none;
            border-color: var(--accent-color);
            box-shadow: 0 0 0 2px rgba(var(--accent-rgb), 0.2);
        }

        .error-message {
            padding: 20px;
            margin: 20px;
            background-color: var(--error-bg);
            border: 1px solid var(--error-border);
            border-radius: 8px;
            color: var(--error-text);
            text-align: center;
            display: none;
        }

        .error-message p {
            margin: 10px 0;
        }

        .error-detail {
            font-size: 0.9em;
            color: var(--error-detail);
            margin-top: 10px;
        }

        .success-message {
            color: var(--success-color);
            background: var(--success-light-bg);
            padding: 10px 15px;
            border-radius: 5px;
            margin-bottom: 10px;
            display: none;
            box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
        }

        .admin-badge {
            background: var(--accent-color);
            color: white;
            font-size: 0.7rem;
            padding: 2px 6px;
            border-radius: 4px;
            margin-left: 5px;
        }

        @media (max-width: 768px) {
            .chat-container {
                margin: 15px;
                padding: 15px;
                width: auto;
            }

            .chat-header h1 {
                font-size: 1.5rem;
            }

            .chat-messages {
                padding: 10px;
            }

            .message-content {
                max-width: 75%;
                padding: 10px 12px;
            }

            .user-info-container {
                min-width: 40px;
                max-width: 40px;
            }

            .user-name {
                font-size: 10px;
                max-width: 40px;
            }

            .message-avatar {
                width: 35px;
                height: 35px;
            }

            .message-actions {
                display: none;
            }

            .message-form {
                padding: 15px 10px;
            }

            .message-input-container {
                flex-direction: row;
                align-items: center;
            }

            .visitor-info {
                flex-direction: column;
                gap: 8px;
            }

            .send-btn {
                width: 40px;
                height: 40px;
                min-width: 40px;
            }
        }

        @media (max-width: 480px) {
            .chat-container {
                margin: 10px;
                padding: 10px;
            }

            .chat-header {
                margin-bottom: 20px;
                padding-bottom: 15px;
            }

            .chat-header h1 {
                font-size: 1.3rem;
            }

            .chat-messages {
                max-height: 500px;
            }

            .message-content {
                max-width: 70%;
                padding: 8px 10px;
            }

            .user-info-container {
                min-width: 36px;
                max-width: 36px;
            }

            .user-name {
                font-size: 9px;
                max-width: 36px;
            }

            .message-avatar {
                width: 30px;
                height: 30px;
            }

            .message-text {
                font-size: 0.9rem;
            }

            .message-time {
                font-size: 0.7rem;
            }
        }

        .loading {
            text-align: center;
            padding: 20px;
            color: var(--text-secondary);
        }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/components/header.jsp" />

<div class="chat-container">
    <div class="chat-header">
        <h1>留言板</h1>
        <p>在这里留下您的想法和意见</p>
    </div>

    <div id="errorMessage" class="error-message"></div>
    <div id="successMessage" class="success-message"></div>

    <div id="chatMessages" class="chat-messages">
        <div class="loading">加载留言中...</div>
    </div>

    <form id="messageForm" class="message-form">
        <c:if test="${empty sessionScope.user}">
            <div class="visitor-info">
                <input type="text" name="name" placeholder="您的昵称" required class="visitor-input">
                <input type="email" name="email" placeholder="您的邮箱" required class="visitor-input">
            </div>
        </c:if>
        <div class="message-input-container">
            <textarea name="content" placeholder="写下您的留言..." required class="message-input"></textarea>
            <button type="submit" class="send-btn">
                <i class="ri-send-plane-fill"></i>
            </button>
        </div>
    </form>
</div>

<jsp:include page="/WEB-INF/components/footer.jsp" />

<script>
    document.addEventListener('DOMContentLoaded', () => {
        const messageForm = document.getElementById('messageForm');
        const chatMessages = document.getElementById('chatMessages');
        const errorMessage = document.getElementById('errorMessage');
        const successMessage = document.getElementById('successMessage');

        // 当前用户ID
        let currentUserId = null;
        <c:if test="${not empty sessionScope.user}">
        currentUserId = ${sessionScope.user.id};
        </c:if>

        // 加载留言列表
        loadMessages();

        // 提交留言表单
        messageForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const formData = new FormData(messageForm);

            try {
                const response = await fetch('${pageContext.request.contextPath}/messages', {
                    method: 'POST',
                    body: new URLSearchParams(formData)
                });

                const data = await response.json();

                if (data.success) {
                    messageForm.reset();
                    showSuccessMessage(data.message || '留言发送成功');
                    loadMessages();
                } else {
                    showErrorMessage(data.message || '留言发送失败');
                }
            } catch (error) {
                console.error('发送留言失败:', error);
                showErrorMessage('留言发送失败，请稍后重试');
            }
        });

        // 渲染留言列表
        function renderMessages(messagesData) {
            console.log('收到的留言数据:', messagesData);

            chatMessages.innerHTML = '';

            if (!messagesData || messagesData.length === 0) {
                chatMessages.innerHTML = '<div class="loading">暂无留言，快来留下第一条留言吧！</div>';
                return;
            }

            // 按日期升序排序（旧的在上，新的在下）
            messagesData.sort((a, b) => {
                const dateA = new Date(a.message.createdAt);
                const dateB = new Date(b.message.createdAt);
                return dateA - dateB;
            });

            messagesData.forEach(messageData => {
                const message = messageData.message;
                const replies = messageData.replies || [];

                // 获取头像URL
                let avatarUrl = '${pageContext.request.contextPath}/avatar/default.png';
                if (message.userId && message.userId > 0) {
                    // 用户有ID，使用用户头像
                    avatarUrl = '${pageContext.request.contextPath}/avatar/user_' + message.userId + '.jpg';
                    // 如果用户有自定义头像路径，优先使用
                    if (message.userAvatarUrl) {
                        avatarUrl = '${pageContext.request.contextPath}/avatar/' + message.userAvatarUrl;
                    }
                } else if (message.avatarUrl) {
                    // 如果有直接提供的头像URL，则使用
                    avatarUrl = message.avatarUrl;
                }

                // 获取用户昵称
                let userName = '';
                if (message.userId && message.userId > 0) {
                    // 用户登录状态
                    userName = message.username || ('用户' + message.userId);
                } else {
                    // 访客留言
                    userName = message.name || '匿名访客';
                }

                // 格式化日期
                const date = new Date(message.createdAt);
                const formattedDate = date.toLocaleString('zh-CN', {
                    year: 'numeric',
                    month: '2-digit',
                    day: '2-digit',
                    hour: '2-digit',
                    minute: '2-digit'
                });

                // 创建留言元素
                const messageElement = document.createElement('div');
                messageElement.className = `message ${message.userId == currentUserId ? 'own' : 'other'}`;
                messageElement.dataset.id = message.id;

                // 添加用户信息容器
                const userInfoContainer = document.createElement('div');
                userInfoContainer.className = 'user-info-container';

                // 创建用户名元素
                const userNameElement = document.createElement('div');
                userNameElement.className = 'user-name';
                userNameElement.textContent = userName;

                // 创建头像元素
                const avatarElement = document.createElement('img');
                avatarElement.src = avatarUrl;
                avatarElement.alt = '用户头像';
                avatarElement.className = 'message-avatar';
                avatarElement.onerror = function() {
                    this.src = '${pageContext.request.contextPath}/avatar/default.png';
                };

                // 组装用户信息容器
                userInfoContainer.appendChild(userNameElement);
                userInfoContainer.appendChild(avatarElement);

                // 创建留言内容容器
                const messageContentContainer = document.createElement('div');
                messageContentContainer.className = 'message-content';

                // 创建留言信息
                const messageInfo = document.createElement('div');
                messageInfo.className = 'message-info';

                // 创建留言时间
                const messageTime = document.createElement('span');
                messageTime.className = 'message-time';
                messageTime.textContent = formattedDate;

                // 创建留言文本
                const messageText = document.createElement('div');
                messageText.className = 'message-text';
                messageText.textContent = message.message || '无内容';

                // 组装留言内容
                messageInfo.appendChild(messageTime);
                if (message.isAdmin) {
                    const adminBadge = document.createElement('span');
                    adminBadge.className = 'admin-badge';
                    adminBadge.textContent = '管理员';
                    messageInfo.appendChild(adminBadge);
                }

                messageContentContainer.appendChild(messageInfo);
                messageContentContainer.appendChild(messageText);

                // 将内容和用户信息添加到留言元素
                if (message.userId == currentUserId) {
                    // 本账号留言，头像在右，消息在左
                    messageElement.appendChild(messageContentContainer);
                    messageElement.appendChild(userInfoContainer);
                } else {
                    // 非本账号留言，头像在左，消息在右
                    messageElement.appendChild(userInfoContainer);
                    messageElement.appendChild(messageContentContainer);
                }

                // 添加删除按钮（如果有权限）
                if (message.canDelete) {
                    const actionsContainer = document.createElement('div');
                    actionsContainer.className = 'message-actions';

                    const deleteBtn = document.createElement('button');
                    deleteBtn.className = 'action-btn delete-btn';
                    deleteBtn.dataset.id = message.id;

                    const deleteIcon = document.createElement('i');
                    deleteIcon.className = 'ri-delete-bin-line';

                    deleteBtn.appendChild(deleteIcon);
                    actionsContainer.appendChild(deleteBtn);
                    messageElement.appendChild(actionsContainer);
                }

                chatMessages.appendChild(messageElement);

                // 添加回复，如果有的话
                if (replies && replies.length > 0) {
                    // 按日期升序排序
                    replies.sort((a, b) => {
                        const dateA = new Date(a.createdAt);
                        const dateB = new Date(b.createdAt);
                        return dateA - dateB;
                    });

                    replies.forEach(reply => {
                        // 获取回复头像URL
                        let replyAvatarUrl = '${pageContext.request.contextPath}/avatar/default.png';
                        if (reply.userId && reply.userId > 0) {
                            // 用户有ID，使用用户头像
                            replyAvatarUrl = '${pageContext.request.contextPath}/avatar/user_' + reply.userId + '.jpg';
                            // 如果用户有自定义头像路径，优先使用
                            if (reply.userAvatarUrl) {
                                replyAvatarUrl = '${pageContext.request.contextPath}/avatar/' + reply.userAvatarUrl;
                            }
                        } else if (reply.avatarUrl) {
                            // 如果有直接提供的头像URL，则使用
                            replyAvatarUrl = reply.avatarUrl;
                        }

                        // 获取回复用户昵称
                        let replyUserName = '';
                        if (reply.userId && reply.userId > 0) {
                            // 用户登录状态
                            replyUserName = reply.username || ('用户' + reply.userId);
                        } else {
                            // 访客留言
                            replyUserName = reply.name || '匿名访客';
                        }

                        // 格式化回复日期
                        const replyDate = new Date(reply.createdAt);
                        const replyFormattedDate = replyDate.toLocaleString('zh-CN', {
                            year: 'numeric',
                            month: '2-digit',
                            day: '2-digit',
                            hour: '2-digit',
                            minute: '2-digit'
                        });

                        const replyElement = document.createElement('div');
                        replyElement.className = `message ${reply.userId == currentUserId ? 'own' : 'other'}`;
                        replyElement.dataset.id = reply.id;
                        replyElement.style.marginLeft = '50px';

                        // 添加用户信息容器
                        const replyUserInfoContainer = document.createElement('div');
                        replyUserInfoContainer.className = 'user-info-container';

                        // 创建用户名元素
                        const replyUserNameElement = document.createElement('div');
                        replyUserNameElement.className = 'user-name';
                        replyUserNameElement.textContent = replyUserName;

                        // 创建头像元素
                        const replyAvatarElement = document.createElement('img');
                        replyAvatarElement.src = replyAvatarUrl;
                        replyAvatarElement.alt = '用户头像';
                        replyAvatarElement.className = 'message-avatar';
                        replyAvatarElement.onerror = function() {
                            this.src = '${pageContext.request.contextPath}/avatar/default.png';
                        };

                        // 组装用户信息容器
                        replyUserInfoContainer.appendChild(replyUserNameElement);
                        replyUserInfoContainer.appendChild(replyAvatarElement);

                        // 创建回复内容容器
                        const replyContentContainer = document.createElement('div');
                        replyContentContainer.className = 'message-content';

                        // 创建回复信息
                        const replyInfo = document.createElement('div');
                        replyInfo.className = 'message-info';

                        // 创建回复时间
                        const replyTime = document.createElement('span');
                        replyTime.className = 'message-time';
                        replyTime.textContent = replyFormattedDate;

                        // 创建回复文本
                        const replyText = document.createElement('div');
                        replyText.className = 'message-text';
                        replyText.textContent = reply.message || '无内容';

                        // 组装回复内容
                        replyInfo.appendChild(replyTime);
                        if (reply.isAdmin) {
                            const replyAdminBadge = document.createElement('span');
                            replyAdminBadge.className = 'admin-badge';
                            replyAdminBadge.textContent = '管理员';
                            replyInfo.appendChild(replyAdminBadge);
                        }

                        replyContentContainer.appendChild(replyInfo);
                        replyContentContainer.appendChild(replyText);

                        // 将内容和用户信息添加到回复元素
                        if (reply.userId == currentUserId) {
                            // 本账号回复，头像在右，消息在左
                            replyElement.appendChild(replyContentContainer);
                            replyElement.appendChild(replyUserInfoContainer);
                        } else {
                            // 非本账号回复，头像在左，消息在右
                            replyElement.appendChild(replyUserInfoContainer);
                            replyElement.appendChild(replyContentContainer);
                        }

                        // 添加删除按钮（如果有权限）
                        if (reply.canDelete) {
                            const replyActionsContainer = document.createElement('div');
                            replyActionsContainer.className = 'message-actions';

                            const replyDeleteBtn = document.createElement('button');
                            replyDeleteBtn.className = 'action-btn delete-btn';
                            replyDeleteBtn.dataset.id = reply.id;

                            const replyDeleteIcon = document.createElement('i');
                            replyDeleteIcon.className = 'ri-delete-bin-line';

                            replyDeleteBtn.appendChild(replyDeleteIcon);
                            replyActionsContainer.appendChild(replyDeleteBtn);
                            replyElement.appendChild(replyActionsContainer);
                        }

                        chatMessages.appendChild(replyElement);
                    });
                }
            });

            // 添加事件监听器
            addEventListeners();

            // 滚动到底部
            chatMessages.scrollTop = chatMessages.scrollHeight;
        }

        // 加载留言列表
        async function loadMessages() {
            try {
                chatMessages.innerHTML = '<div class="loading">加载留言中...</div>';

                const response = await fetch('${pageContext.request.contextPath}/messages/list');

                // 检查响应状态
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }

                // 检查响应类型
                const contentType = response.headers.get('content-type');
                if (!contentType || !contentType.includes('application/json')) {
                    throw new Error('服务器返回了非JSON响应');
                }

                const data = await response.json();

                if (data.success) {
                    renderMessages(data.data);
                } else {
                    console.error('加载留言失败:', data.message, data.error);
                    const errorDetail = data.error || '未知错误';
                    chatMessages.innerHTML = `
                    <div class="error-message" style="display:block">
                        <p>${data.message || '加载留言失败'}</p>
                        <p class="error-detail">错误详情: ${errorDetail}</p>
                    </div>
                `;
                }
            } catch (error) {
                console.error('加载留言失败:', error);
                const errorDetail = error.message || '未知错误';
                chatMessages.innerHTML = `
                <div class="error-message" style="display:block">
                    <p>加载留言失败，请刷新页面重试</p>
                    <p class="error-detail">错误详情: ${errorDetail}</p>
                </div>
            `;
            }
        }

        // 添加事件监听器
        function addEventListeners() {
            // 删除按钮事件
            document.querySelectorAll('.delete-btn').forEach(btn => {
                btn.addEventListener('click', function() {
                    const messageId = this.dataset.id;
                    if (confirm('确定要删除这条留言吗？')) {
                        deleteMessage(messageId);
                    }
                });
            });
        }

        // 删除留言
        async function deleteMessage(messageId) {
            try {
                const response = await fetch('${pageContext.request.contextPath}/messages?messageId=' + messageId, {
                    method: 'DELETE'
                });

                const data = await response.json();

                if (data.success) {
                    showSuccessMessage('留言删除成功');
                    loadMessages();
                } else {
                    showErrorMessage(data.message || '删除留言失败');
                }
            } catch (error) {
                console.error('删除留言失败:', error);
                showErrorMessage('删除留言失败，请稍后重试');
            }
        }

        // 显示错误消息
        function showErrorMessage(message) {
            errorMessage.textContent = message;
            errorMessage.style.display = 'block';
            successMessage.style.display = 'none';

            setTimeout(() => {
                errorMessage.style.display = 'none';
            }, 5000);
        }

        // 显示成功消息
        function showSuccessMessage(message) {
            successMessage.textContent = message;
            successMessage.style.display = 'block';
            errorMessage.style.display = 'none';

            setTimeout(() => {
                successMessage.style.display = 'none';
            }, 3000);
        }
    });
</script>
</body>
</html>