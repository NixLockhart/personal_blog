<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.blog.util.AvatarUtil" %>
<!-- 内联主题初始化代码，防止闪烁，优先级最高 -->
<script>
    // 立即执行，在DOM加载前应用主题
    (function() {
        var savedTheme = localStorage.getItem('theme');
        if(savedTheme === 'dark') {
            document.documentElement.setAttribute('data-theme', 'dark');
            document.documentElement.classList.add('theme-no-transition');
        }
    })();
</script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/fonts/remixicon.css">
<nav class="navbar">
    <div class="container nav-content">
        <div class="nav-brand-group">
            <a href="${pageContext.request.contextPath}/" class="nav-brand">
                <img src="${pageContext.request.contextPath}/images/element/nix.png" alt="nix" class="brand-logo">
            </a>
        </div>
        <div class="nav-links" id="navLinks">
            <a href="${pageContext.request.contextPath}/" class="nav-link">主页</a>
            <a href="${pageContext.request.contextPath}/categories" class="nav-link">文章</a>
            <a href="${pageContext.request.contextPath}/messages" class="nav-link">留言板</a>
            <a href="${pageContext.request.contextPath}/about" class="nav-link">关于本站</a>
            <c:if test="${sessionScope.user != null}">
                <!-- 移动端通知按钮 -->
                <a href="${pageContext.request.contextPath}/notifications" class="nav-link mobile-notification-link">
                    <i class="ri-notification-3-line"></i> 我的通知
                    <span class="notification-badge" id="mobileNotificationBadge"></span>
                </a>
            </c:if>
            <c:if test="${sessionScope.user != null && sessionScope.user.isAdmin}">
                <a href="${pageContext.request.contextPath}/admin/" class="nav-link"><i class="ri-settings-3-line"></i> 后台管理</a>
            </c:if>
            <c:if test="${sessionScope.user != null}">
                <!-- 移动端退出登录按钮 -->
                <a href="${pageContext.request.contextPath}/logout" class="nav-link mobile-logout-link">
                    <i class="ri-logout-box-r-line"></i> 退出登录
                </a>
            </c:if>
        </div>
        <div class="nav-actions">
            <!-- 主题切换按钮 - 桌面端显示为开关，移动端显示为按钮 -->
            <div class="theme-toggle desktop-theme-toggle" id="themeToggle">
                <div class="theme-toggle-icon">
                    <i class="ri-sun-line" id="themeIcon"></i>
                </div>
            </div>
            <!-- 移动端主题切换按钮 -->
            <button class="mobile-theme-btn" id="mobileThemeBtn">
                <i class="ri-sun-line" id="mobileThemeIcon"></i>
            </button>

            <c:choose>
                <c:when test="${sessionScope.user != null}">
                    <div class="user-profile-container">

                        <a href="${pageContext.request.contextPath}/profile" class="user-profile-link" title="个人中心" id="userProfileToggle">
                            <c:choose>
                                <c:when test="${not empty sessionScope.user.avatarUrl}">
                                    <img src="${pageContext.request.contextPath}${sessionScope.user.avatarUrl}?t=${sessionScope.lastAvatarUpdate}" alt="用户头像" class="user-avatar">
                                </c:when>
                                <c:otherwise>
                                    <img src="${pageContext.request.contextPath}<%= AvatarUtil.processAvatarUrl(null) %>" alt="默认头像" class="user-avatar">
                                </c:otherwise>
                            </c:choose>
                        </a>
                        <!-- 用户头像浮窗（仅桌面端显示） -->
                        <div class="user-dropdown" id="userDropdown">
                            <div class="user-dropdown-info">
                                <img src="${pageContext.request.contextPath}${not empty sessionScope.user.avatarUrl ? sessionScope.user.avatarUrl : AvatarUtil.processAvatarUrl(null)}?t=${sessionScope.lastAvatarUpdate}" alt="用户头像" class="dropdown-avatar">
                                <span class="dropdown-username">${sessionScope.user.username}</span>
                            </div>
                            <div class="user-dropdown-links">
                                <a href="${pageContext.request.contextPath}/profile" class="dropdown-link"><i class="ri-user-settings-line"></i> 个人中心</a>
                                <a href="${pageContext.request.contextPath}/notifications" class="dropdown-link">
                                    <i class="ri-notification-3-line"></i> 我的通知
                                    <span class="notification-badge" id="desktopNotificationBadge"></span>
                                </a>
                                <a href="${pageContext.request.contextPath}/logout" class="dropdown-link"><i class="ri-logout-box-r-line"></i> 退出登录</a>
                            </div>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/login" class="user-profile-link" title="登录/注册">
                        <img src="${pageContext.request.contextPath}<%= AvatarUtil.processAvatarUrl(null) %>" alt="默认头像" class="user-avatar">
                    </a>
                </c:otherwise>
            </c:choose>
            <button class="mobile-menu-btn" id="mobileMenuBtn">
                <i class="ri-menu-line"></i>
            </button>
        </div>
    </div>
</nav>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        // 创建主题切换扩散元素
        const themeRipple = document.createElement('div');
        themeRipple.className = 'theme-ripple';
        document.body.appendChild(themeRipple);

        // 检测是否为移动设备
        const isMobile = window.innerWidth <= 768 || navigator.userAgent.match(/Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i);

        // 为移动端创建一个轻量级的淡入淡出效果
        const createFadeOverlay = () => {
            const overlay = document.createElement('div');
            overlay.style.position = 'fixed';
            overlay.style.top = '0';
            overlay.style.left = '0';
            overlay.style.width = '100%';
            overlay.style.height = '100%';
            overlay.style.backgroundColor = 'rgba(0,0,0,0)';
            overlay.style.transition = 'background-color 0.3s ease';
            overlay.style.zIndex = '999';
            overlay.style.pointerEvents = 'none';
            document.body.appendChild(overlay);
            return overlay;
        };

        // 定义主题切换函数（内部实现，不依赖外部JS）
        function toggleTheme(event) {
            // 获取当前主题
            const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
            const newTheme = currentTheme === 'light' ? 'dark' : 'light';

            // 移动端使用简化的动画，减少卡顿
            if (isMobile) {
                // 创建简单的淡入淡出效果
                const overlay = createFadeOverlay();
                overlay.style.backgroundColor = newTheme === 'dark' ? 'rgba(0,0,0,0.2)' : 'rgba(255,255,255,0.2)';

                // 直接更改主题
                document.documentElement.setAttribute('data-theme', newTheme);
                localStorage.setItem('theme', newTheme);
                updateThemeIcons(newTheme);

                // 淡出效果并移除overlay
                setTimeout(() => {
                    overlay.style.backgroundColor = 'rgba(0,0,0,0)';
                    setTimeout(() => {
                        document.body.removeChild(overlay);
                    }, 300);
                }, 100);
            } else {
                // 桌面端使用完整动画效果
                // 获取点击位置
                const x = event ? event.clientX : window.innerWidth / 2;
                const y = event ? event.clientY : window.innerHeight / 2;

                // 设置扩散元素的颜色和位置
                themeRipple.style.backgroundColor = newTheme === 'dark' ? '#18191a' : '#f8f9fa';
                themeRipple.style.left = `${x}px`;
                themeRipple.style.top = `${y}px`;

                // 先清除之前的动画
                themeRipple.classList.remove('active');

                // 强制重绘
                void themeRipple.offsetWidth;

                // 立即改变主题，让页面随动画逐渐变化
                document.documentElement.setAttribute('data-theme', newTheme);
                localStorage.setItem('theme', newTheme);

                // 添加扩散动画
                themeRipple.classList.add('active');

                // 更新所有主题图标
                updateThemeIcons(newTheme);

                // 动画结束后清理
                setTimeout(() => {
                    themeRipple.classList.remove('active');
                }, 1100);
            }

            // 阻止事件冒泡
            if (event) {
                event.stopPropagation();
            }
        }

        // 移动菜单切换
        const mobileMenuBtn = document.getElementById('mobileMenuBtn');
        const navLinks = document.getElementById('navLinks');

        if (mobileMenuBtn && navLinks) {
            mobileMenuBtn.addEventListener('click', function() {
                navLinks.classList.toggle('show');
            });
        }

        // 主题图标更新函数
        const themeIcon = document.getElementById('themeIcon');
        const mobileThemeIcon = document.getElementById('mobileThemeIcon');

        function updateThemeIcons(theme) {
            const iconClass = theme === 'dark' ? 'ri-moon-line' : 'ri-sun-line';
            if (themeIcon) themeIcon.className = iconClass;
            if (mobileThemeIcon) mobileThemeIcon.className = iconClass;
        }

        if (themeIcon || mobileThemeIcon) {
            // 根据当前主题设置图标
            const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
            updateThemeIcons(currentTheme);

            // 监听主题变化
            const observer = new MutationObserver(function(mutations) {
                mutations.forEach(function(mutation) {
                    if (mutation.attributeName === 'data-theme') {
                        const newTheme = document.documentElement.getAttribute('data-theme');
                        updateThemeIcons(newTheme);
                    }
                });
            });

            // 开始观察
            observer.observe(document.documentElement, { attributes: true });
        }

        // 移动端主题切换按钮事件
        const mobileThemeBtn = document.getElementById('mobileThemeBtn');
        if (mobileThemeBtn) {
            mobileThemeBtn.addEventListener('click', toggleTheme);
        }

        // 桌面端主题切换按钮事件
        const themeToggle = document.getElementById('themeToggle');
        if (themeToggle) {
            themeToggle.addEventListener('click', toggleTheme);
        }

        // 确保用户头像浮窗在所有页面都能正常工作
        const userProfileContainer = document.querySelector('.user-profile-container');
        const userDropdown = document.getElementById('userDropdown');

        if (userProfileContainer && userDropdown) {
            // 移除现有的hover效果（CSS中的）
            userProfileContainer.addEventListener('mouseenter', function() {
                userDropdown.style.opacity = '1';
                userDropdown.style.visibility = 'visible';
                userDropdown.style.transform = 'translateY(0)';
            });

            userProfileContainer.addEventListener('mouseleave', function() {
                userDropdown.style.opacity = '0';
                userDropdown.style.visibility = 'hidden';
                userDropdown.style.transform = 'translateY(10px)';
            });
        }

        // 加载通知数量
        loadNotificationCount();

        // 每60秒更新一次通知数量
        setInterval(loadNotificationCount, 60000);

        // 加载通知数量函数
        function loadNotificationCount() {
            // 检查用户是否登录
            if (document.querySelector('.user-profile-container')) {
                fetch('${pageContext.request.contextPath}/notifications/count')
                    .then(response => response.json())
                    .then(data => {
                        const counter = document.getElementById('notificationCounter');
                        if (counter) {
                            counter.textContent = data.count;
                            counter.style.display = data.count > 0 ? 'block' : 'none';
                        }
                    })
                    .catch(error => console.error('获取通知数量失败:', error));
            }
        }
    });
</script>

<style>
    /* 通知图标样式 */
    /* 移动端通知链接，仅在移动端菜单中显示 */
    .mobile-notification-link {
        display: none !important;
        align-items: center !important;
        justify-content: center !important;
    }

    /* 移动端样式 */
    @media (max-width: 768px) {
        .notification-icon {
            display: none !important;
        }

        .mobile-notification-link {
            display: flex !important;
        }
    }

    .notification-badge {
        display: inline-block;
        min-width: 18px;
        height: 18px;
        padding: 0 6px;
        border-radius: 9px;
        background-color: #f44336;
        color: white;
        font-size: 12px;
        line-height: 18px;
        text-align: center;
        margin-left: 5px;
        vertical-align: middle;
    }

    .notification-badge:empty {
        display: none;
    }

    .mobile-notification-link {
        position: relative;
    }

    .mobile-notification-link .notification-badge {
        position: absolute;
        top: -5px;
        right: -5px;
    }

    .dropdown-link {
        position: relative;
        display: flex;
        align-items: center;
        gap: 8px;
    }

    .dropdown-link .notification-badge {
        margin-left: auto;
    }
</style>

<script>
    // 更新通知数量
    function updateNotificationCount() {
        fetch('${pageContext.request.contextPath}/notifications/count')
            .then(response => response.json())
            .then(data => {
                const count = data.count;
                const mobileBadge = document.getElementById('mobileNotificationBadge');
                const desktopBadge = document.getElementById('desktopNotificationBadge');

                if (count > 0) {
                    mobileBadge.textContent = count;
                    desktopBadge.textContent = count;
                } else {
                    mobileBadge.textContent = '';
                    desktopBadge.textContent = '';
                }
            })
            .catch(error => console.error('获取通知数量失败:', error));
    }

    // 页面加载时获取通知数量
    document.addEventListener('DOMContentLoaded', function() {
        updateNotificationCount();
        // 每30秒更新一次通知数量
        setInterval(updateNotificationCount, 30000);
    });
</script>
