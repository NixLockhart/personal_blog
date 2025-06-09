<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- 仪表盘内容 -->
<div class="admin-card">
  <div class="admin-card-header">
    <h2><i class="ri-dashboard-line"></i> 网站数据统计</h2>
  </div>

  <div class="stats-grid">
    <div class="stat-card">
      <div class="stat-card-icon">
        <i class="ri-user-settings-line"></i>
      </div>
      <div class="stat-card-value">${totalUsers}</div>
      <div class="stat-card-label">注册用户</div>
    </div>

    <div class="stat-card">
      <div class="stat-card-icon">
        <i class="ri-file-list-line"></i>
      </div>
      <div class="stat-card-value">${totalPosts}</div>
      <div class="stat-card-label">文章总数</div>
    </div>

    <div class="stat-card">
      <div class="stat-card-icon">
        <i class="ri-eye-line"></i>
      </div>
      <div class="stat-card-value">${totalViews != null ? totalViews : 0}</div>
      <div class="stat-card-label">总浏览量</div>
    </div>

    <div class="stat-card">
      <div class="stat-card-icon">
        <i class="ri-chat-3-line"></i>
      </div>
      <div class="stat-card-value">${totalMessages != null ? totalMessages : 0}</div>
      <div class="stat-card-label">总留言数</div>
    </div>
  </div>
</div>

<!-- 最近活动 -->
<div class="admin-card">
  <div class="admin-card-header">
    <h2><i class="ri-history-line"></i> 最近活动</h2>
  </div>

  <div class="recent-activity">
    <c:choose>
      <c:when test="${not empty recentPosts}">
        <table class="admin-table">
          <thead>
          <tr>
            <th>标题</th>
            <th>作者</th>
            <th>分类</th>
            <th>发布时间</th>
            <th>浏览量</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach items="${recentPosts}" var="post">
            <tr>
              <td>${post.title}</td>
              <td>${post.authorName}</td>
              <td>${post.categoryName}</td>
              <td><fmt:formatDate value="${post.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
              <td>${post.views}</td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
      </c:when>
      <c:otherwise>
        <p>暂无最近活动</p>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<!-- 系统信息 -->
<div class="admin-card">
  <div class="admin-card-header">
    <h2><i class="ri-server-line"></i> 系统信息</h2>
  </div>

  <div class="system-info">
    <table class="admin-table">
      <tr>
        <td>服务器时间</td>
        <td id="serverTime"><fmt:formatDate value="<%= new java.util.Date() %>" pattern="yyyy-MM-dd HH:mm:ss"/></td>
      </tr>
      <script>
        // 更新服务器时间的函数
        function updateServerTime() {
          const serverTimeElement = document.getElementById('serverTime');
          const initialTime = new Date(serverTimeElement.textContent);

          function updateTime() {
            const currentTime = new Date(initialTime.getTime() + (Date.now() - pageLoadTime));
            serverTimeElement.textContent = currentTime.toLocaleString('zh-CN', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
              hour: '2-digit',
              minute: '2-digit',
              second: '2-digit',
              hour12: false
            }).replace(/\//g, '-');
          }

          const pageLoadTime = Date.now();
          // 每秒更新一次时间
          setInterval(updateTime, 1000);
          updateTime(); // 立即更新一次
        }

        // 页面加载完成后启动时间更新
        document.addEventListener('DOMContentLoaded', updateServerTime);
      </script>
      <tr>
        <td>Java 版本</td>
        <td><%= System.getProperty("java.version") %></td>
      </tr>
      <tr>
        <td>操作系统</td>
        <td><%= System.getProperty("os.name") %> <%= System.getProperty("os.version") %></td>
      </tr>
      <tr>
        <td>服务器信息</td>
        <td><%= application.getServerInfo() %></td>
      </tr>
    </table>
  </div>
</div>