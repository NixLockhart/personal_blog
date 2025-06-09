<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
  <!-- 防止闪烁的主题初始化脚本，必须放在所有样式表之前 -->
  <script>
    // 立即执行，在DOM和CSS加载前应用主题
    (function() {
      var savedTheme = localStorage.getItem('theme');
      if(savedTheme === 'dark') {
        document.documentElement.setAttribute('data-theme', 'dark');
      }
    })();
  </script>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>后台管理 - 星光小栈</title>
  <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/element/website_logo.png">
  <!-- 本地Remixicon资源 -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/fonts/remixicon.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <script>
    // 初始化主题
    document.addEventListener('DOMContentLoaded', function() {
      // 获取保存的主题
      const savedTheme = localStorage.getItem('theme') || 'light';

      // 设置初始主题
      if (savedTheme === 'dark') {
        document.documentElement.setAttribute('data-theme', 'dark');
      }

      // 更新图标
      const adminThemeIcon = document.getElementById('adminThemeIcon');
      if (adminThemeIcon) {
        adminThemeIcon.className = savedTheme === 'dark' ? 'ri-moon-line' : 'ri-sun-line';
      }

      // 确保主题切换按钮可点击且有正确的事件监听
      const adminThemeToggle = document.getElementById('adminThemeToggle');
      if (adminThemeToggle) {
        adminThemeToggle.addEventListener('click', function(e) {
          if (typeof window.toggleTheme === 'function') {
            window.toggleTheme(e);
          }
        });
      }
    });
  </script>
  <script src="${pageContext.request.contextPath}/js/theme.js"></script>
  <style>
    /* 基础样式设置 */
    html, body {
      height: 100%;
      margin: 0;
      padding: 0;
      overflow: hidden;
    }

    /* 后台管理界面样式 */
    .admin-container {
      display: flex;
      height: 100%;
      background-color: var(--primary-color);
    }

    /* 左侧导航栏 - 浅色WinUI3风格 */
    .admin-sidebar {
      width: 250px;
      background: #fafafa;
      color: #202020;
      padding: 8px 0;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
      position: relative;
      overflow: hidden;
      border-right: 1px solid #e1e1e1;
      transition: background-color 0.3s ease, color 0.3s ease, border-color 0.3s ease;
    }

    /* 深色模式下的侧边栏 */
    [data-theme="dark"] .admin-sidebar {
      background: #2c2c2c;
      color: #e4e6eb;
      border-right: 1px solid #3a3b3c;
    }

    .admin-sidebar-header {
      padding: 16px 24px;
      margin-bottom: 16px;
      border-bottom: 1px solid #eaeaea;
      transition: border-color 0.3s ease;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    [data-theme="dark"] .admin-sidebar-header {
      border-bottom: 1px solid #3a3b3c;
    }

    .admin-sidebar-header h2 {
      margin: 0;
      font-size: 20px;
      display: flex;
      align-items: center;
      font-weight: 600;
      color: #202020;
      transition: color 0.3s ease;
    }

    [data-theme="dark"] .admin-sidebar-header h2 {
      color: #e4e6eb;
    }

    .admin-sidebar-header h2 i {
      margin-right: 12px;
      font-size: 20px;
      color: #0078D4;
    }

    .admin-nav {
      list-style: none;
      padding: 0;
      margin: 0;
    }

    .admin-nav-item {
      padding: 10px 24px;
      margin: 4px 8px;
      display: flex;
      align-items: center;
      border-radius: 4px;
      position: relative;
      transition: all 0.2s ease;
      cursor: pointer;
      background: transparent;
    }

    .admin-nav-item::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.03);
      border-radius: 4px;
      opacity: 0;
      transition: opacity 0.2s ease-in-out;
    }

    [data-theme="dark"] .admin-nav-item::before {
      background: rgba(255, 255, 255, 0.05);
    }

    .admin-nav-item:hover::before {
      opacity: 1;
    }

    .admin-nav-item.active {
      background: rgba(0, 120, 212, 0.1);
    }

    [data-theme="dark"] .admin-nav-item.active {
      background: rgba(0, 120, 212, 0.2);
    }

    .admin-nav-item.active::after {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 18px;
      background: #0078D4;
      border-radius: 0 2px 2px 0;
    }

    .admin-nav-item i {
      margin-right: 12px;
      width: 20px;
      text-align: center;
      font-size: 18px;
      color: #0078D4;
    }

    .admin-nav-item a {
      color: #202020;
      text-decoration: none;
      flex-grow: 1;
      font-size: 14px;
      font-weight: 500;
      letter-spacing: 0.25px;
      transition: color 0.3s ease;
    }

    [data-theme="dark"] .admin-nav-item a {
      color: #e4e6eb;
    }

    /* WinUI3动画效果 */
    @keyframes reveal {
      from { opacity: 0; transform: translateY(6px); }
      to { opacity: 1; transform: translateY(0); }
    }

    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }

    .admin-nav-item {
      animation: reveal 0.2s ease-out forwards;
      animation-delay: calc(0.05s * var(--item-index, 0));
      opacity: 0;
    }

    .admin-nav-item:nth-child(1) { --item-index: 1; }
    .admin-nav-item:nth-child(2) { --item-index: 2; }
    .admin-nav-item:nth-child(3) { --item-index: 3; }
    .admin-nav-item:nth-child(4) { --item-index: 4; }

    .admin-content {
      animation: fadeIn 0.3s ease-out;
    }

    .admin-nav-item:active {
      transform: scale(0.98);
    }

    /* 右侧内容区 */
    .admin-content {
      flex-grow: 1;
      padding: 24px;
      background-color: #f9f9f9;
      overflow-y: auto;
      transition: background-color 0.3s ease;
    }

    [data-theme="dark"] .admin-content {
      background-color: #18191a;
    }

    .admin-card {
      background-color: #ffffff;
      border-radius: 8px;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
      padding: 20px;
      margin-bottom: 24px;
      border: 1px solid #eaeaea;
      transition: background-color 0.3s ease, border-color 0.3s ease, box-shadow 0.3s ease;
    }

    [data-theme="dark"] .admin-card {
      background-color: #242526;
      border: 1px solid #3a3b3c;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
    }

    .admin-card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      border-bottom: 1px solid #f0f0f0;
      padding-bottom: 16px;
      transition: border-color 0.3s ease;
    }

    [data-theme="dark"] .admin-card-header {
      border-bottom: 1px solid #3a3b3c;
    }

    .admin-card-header h2 {
      margin: 0;
      font-size: 1.2rem;
      color: #202020;
      font-weight: 600;
      display: flex;
      align-items: center;
      transition: color 0.3s ease;
    }

    [data-theme="dark"] .admin-card-header h2 {
      color: #e4e6eb;
    }

    .admin-card-header h2 i {
      margin-right: 10px;
      color: #0078D4;
    }

    /* 数据统计卡片 */
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 20px;
      margin-bottom: 24px;
    }

    .stat-card {
      background: white;
      border-radius: 8px;
      padding: 20px;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
      display: flex;
      flex-direction: column;
      border: 1px solid #eaeaea;
      transition: transform 0.2s ease, box-shadow 0.2s ease, background-color 0.3s ease, border-color 0.3s ease, color 0.3s ease;
    }

    [data-theme="dark"] .stat-card {
      background: #242526;
      border: 1px solid #3a3b3c;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
    }

    .stat-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
    }

    [data-theme="dark"] .stat-card:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    }

    .stat-card-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      background: rgba(0, 120, 212, 0.1);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 16px;
      transition: background-color 0.3s ease;
    }

    [data-theme="dark"] .stat-card-icon {
      background: rgba(0, 120, 212, 0.2);
    }

    .stat-card-icon i {
      color: #0078D4;
      font-size: 1.4rem;
    }

    .stat-card-value {
      font-size: 1.8rem;
      font-weight: 700;
      margin-bottom: 8px;
      color: #202020;
      transition: color 0.3s ease;
    }

    [data-theme="dark"] .stat-card-value {
      color: #e4e6eb;
    }

    .stat-card-label {
      color: #555555;
      font-size: 0.9rem;
      font-weight: 500;
      transition: color 0.3s ease;
    }

    [data-theme="dark"] .stat-card-label {
      color: #b0b3b8;
    }

    /* 表格样式 */
    .admin-table {
      width: 100%;
      border-collapse: separate;
      border-spacing: 0;
      border-radius: 8px;
      overflow: hidden;
      background: white;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
      border: 1px solid #eaeaea;
      transition: background-color 0.3s ease, border-color 0.3s ease, box-shadow 0.3s ease;
    }

    [data-theme="dark"] .admin-table {
      background: #242526;
      border: 1px solid #3a3b3c;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
    }

    .admin-table th, .admin-table td {
      padding: 12px 16px;
      text-align: left;
      border-bottom: 1px solid #f0f0f0;
      transition: border-color 0.3s ease, color 0.3s ease;
    }

    [data-theme="dark"] .admin-table th, [data-theme="dark"] .admin-table td {
      border-bottom: 1px solid #3a3b3c;
      color: #e4e6eb;
    }

    .admin-table th {
      background-color: #f5f5f5;
      color: #202020;
      font-weight: 600;
      font-size: 0.9rem;
      transition: background-color 0.3s ease, color 0.3s ease;
    }

    [data-theme="dark"] .admin-table th {
      background-color: #3a3b3c;
      color: #e4e6eb;
    }

    .admin-table tr:hover {
      background-color: #f7f9fc;
      transition: background-color 0.3s ease;
    }

    [data-theme="dark"] .admin-table tr:hover {
      background-color: #333333;
    }

    .admin-table .action-buttons {
      display: flex;
      gap: 8px;
    }

    .admin-table .btn-edit, .admin-table .btn-delete {
      padding: 6px 12px;
      border-radius: 4px;
      border: none;
      cursor: pointer;
      transition: all 0.2s ease;
      font-weight: 500;
      font-size: 0.8rem;
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .admin-table .btn-edit {
      background-color: #0078D4;
      color: white;
    }

    .admin-table .btn-edit:hover {
      background-color: #106ebe;
    }

    .admin-table .btn-delete {
      background-color: #d13438;
      color: white;
    }

    .admin-table .btn-delete:hover {
      background-color: #c42b2f;
    }

    /* 响应式设计 */
    @media (max-width: 768px) {
      .admin-container {
        flex-direction: column;
      }

      .admin-sidebar {
        width: 100%;
        margin-bottom: 20px;
        border-right: none;
        border-bottom: 1px solid #e1e1e1;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
      }

      .admin-sidebar-header {
        margin-bottom: 8px;
      }

      .admin-nav {
        display: flex;
        overflow-x: auto;
        padding-bottom: 8px;
      }

      .admin-nav-item {
        margin: 0 4px;
        padding: 8px 16px;
        white-space: nowrap;
      }

      .admin-nav-item.active::after {
        left: 50%;
        top: auto;
        bottom: 0;
        transform: translateX(-50%);
        width: 16px;
        height: 3px;
        border-radius: 2px 2px 0 0;
      }

      .stats-grid {
        grid-template-columns: 1fr;
      }

      .admin-table .action-buttons {
        flex-direction: column;
        gap: 6px;
      }
    }

    /* 修复后台管理的主题切换按钮 */
    .admin-sidebar-header .theme-toggle {
      position: relative;
      z-index: 10;
      margin-right: 0;
      cursor: pointer;
      pointer-events: auto;
    }

    /* 系统信息表格文本 */
    .system-info {
      transition: color 0.3s ease;
    }

    [data-theme="dark"] .system-info {
      color: #e4e6eb;
    }

    [data-theme="dark"] .system-info .admin-table {
      background: #242526;
      color: #e4e6eb;
    }

    [data-theme="dark"] .system-info p {
      color: #b0b3b8;
    }

    /* 最近活动文本 */
    .recent-activity p {
      transition: color 0.3s ease;
    }

    [data-theme="dark"] .recent-activity p {
      color: #b0b3b8;
    }
  </style>
  <script>
    // WinUI3风格的涟漪效果
    document.addEventListener('DOMContentLoaded', function() {
      const navItems = document.querySelectorAll('.admin-nav-item');

      // 添加主题切换按钮事件监听
      const adminThemeToggle = document.getElementById('adminThemeToggle');
      if (adminThemeToggle) {
        adminThemeToggle.addEventListener('click', function(e) {
          toggleTheme(e);
        });
      }

      navItems.forEach(item => {
        item.addEventListener('click', function(e) {
          const rect = this.getBoundingClientRect();
          const x = e.clientX - rect.left;
          const y = e.clientY - rect.top;

          const ripple = document.createElement('span');
          ripple.style.position = 'absolute';
          ripple.style.width = '1px';
          ripple.style.height = '1px';
          ripple.style.backgroundColor = 'rgba(0, 120, 212, 0.3)';
          ripple.style.borderRadius = '50%';
          ripple.style.left = x + 'px';
          ripple.style.top = y + 'px';
          ripple.style.transform = 'scale(0)';
          ripple.style.transition = 'transform 0.6s, opacity 0.6s';
          ripple.style.opacity = '1';
          ripple.style.pointerEvents = 'none';

          this.appendChild(ripple);

          setTimeout(() => {
            ripple.style.transform = 'scale(100)';
            ripple.style.opacity = '0';

            setTimeout(() => {
              this.removeChild(ripple);
            }, 600);
          }, 10);
        });
      });

      // 主题切换
      const adminThemeIcon = document.getElementById('adminThemeIcon');

      if (adminThemeIcon) {
        // 根据当前主题设置图标
        const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
        adminThemeIcon.className = currentTheme === 'dark' ? 'ri-moon-line' : 'ri-sun-line';

        // 监听主题变化
        const observer = new MutationObserver(function(mutations) {
          mutations.forEach(function(mutation) {
            if (mutation.attributeName === 'data-theme') {
              const newTheme = document.documentElement.getAttribute('data-theme');
              adminThemeIcon.className = newTheme === 'dark' ? 'ri-moon-line' : 'ri-sun-line';
            }
          });
        });

        // 开始观察
        observer.observe(document.documentElement, { attributes: true });
      }
    });

    // 主题切换函数
    function toggleTheme(event) {
      // 使用window.toggleTheme如果可用，否则使用本地实现
      if (typeof window.toggleTheme === 'function') {
        window.toggleTheme(event);
        return;
      }

      // 备用本地实现
      const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
      const newTheme = currentTheme === 'light' ? 'dark' : 'light';

      // 创建ripple效果
      const x = event ? event.clientX : window.innerWidth / 2;
      const y = event ? event.clientY : window.innerHeight / 2;

      const ripple = document.createElement('div');
      ripple.className = 'theme-ripple';
      ripple.style.backgroundColor = newTheme === 'dark' ? '#18191a' : '#f8f9fa';
      ripple.style.left = `${x}px`;
      ripple.style.top = `${y}px`;
      document.body.appendChild(ripple);

      // 强制重绘
      void ripple.offsetWidth;

      // 添加扩散动画
      ripple.classList.add('active');

      // 设置新主题
      document.documentElement.setAttribute('data-theme', newTheme);
      localStorage.setItem('theme', newTheme);

      // 更新图标
      const adminThemeIcon = document.getElementById('adminThemeIcon');
      if (adminThemeIcon) {
        adminThemeIcon.className = newTheme === 'dark' ? 'ri-moon-line' : 'ri-sun-line';
      }

      // 动画结束后清理
      setTimeout(() => {
        ripple.classList.remove('active');
        document.body.removeChild(ripple);
      }, 1100);

      // 阻止事件冒泡
      if (event) {
        event.stopPropagation();
      }
    }
  </script>
</head>
<body>
<div class="admin-container">
  <!-- 左侧导航栏 -->
  <div class="admin-sidebar">
    <div class="admin-sidebar-header">
      <h2><i class="ri-settings-3-line"></i> 后台管理</h2>
      <div class="theme-toggle" id="adminThemeToggle">
        <div class="theme-toggle-icon">
          <i class="ri-sun-line" id="adminThemeIcon"></i>
        </div>
      </div>
    </div>
    <ul class="admin-nav">
      <li class="admin-nav-item ${activeTab == 'dashboard' ? 'active' : ''}" onclick="window.location.href='${pageContext.request.contextPath}/admin/'">
        <i class="ri-dashboard-line"></i>
        <a>仪表盘</a>
      </li>
      <li class="admin-nav-item ${activeTab == 'users' ? 'active' : ''}" onclick="window.location.href='${pageContext.request.contextPath}/admin/users'">
        <i class="ri-user-settings-line"></i>
        <a>用户管理</a>
      </li>
      <li class="admin-nav-item ${activeTab == 'posts' ? 'active' : ''}" onclick="window.location.href='${pageContext.request.contextPath}/admin/posts'">
        <i class="ri-file-list-line"></i>
        <a>内容管理</a>
      </li>
      <li class="admin-nav-item ${activeTab == 'notifications' ? 'active' : ''}" onclick="window.location.href='${pageContext.request.contextPath}/admin/notifications'">
        <i class="ri-notification-3-line"></i>
        <a>通知管理</a>
      </li>
      <li class="admin-nav-item" onclick="window.location.href='${pageContext.request.contextPath}/'">
        <i class="ri-home-line"></i>
        <a>返回首页</a>
      </li>
    </ul>
  </div>

  <!-- 右侧内容区 -->
  <div class="admin-content">
    <jsp:include page="${contentPage}" />
  </div>
</div>
</body>
</html>