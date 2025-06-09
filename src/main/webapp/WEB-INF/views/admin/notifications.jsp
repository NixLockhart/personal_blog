<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="admin-card">
  <div class="admin-card-header">
    <h2><i class="ri-notification-3-line"></i> 通知管理</h2>
  </div>

  <!-- 发布通知表单 -->
  <div class="notification-form-container mb-4">
    <h3>发布新通知</h3>

    <c:if test="${not empty error}">
      <div class="alert alert-danger">
          ${error}
      </div>
    </c:if>

    <c:if test="${not empty success}">
      <div class="alert alert-success">
          ${success}
      </div>
    </c:if>

    <form action="${pageContext.request.contextPath}/admin/notifications/publish" method="post" class="notification-form">
      <div class="form-group mb-3">
        <label for="title">通知标题</label>
        <input type="text" id="title" name="title" class="form-control" required>
      </div>

      <div class="form-group mb-3">
        <label for="content">通知内容</label>
        <textarea id="content" name="content" class="form-control" rows="4" required></textarea>
      </div>

      <div class="form-group mb-3">
        <label for="type">通知类型</label>
        <select id="type" name="type" class="form-control" required>
          <option value="system">系统通知</option>
          <option value="announcement">网站公告</option>
        </select>
      </div>

      <div class="form-group mb-3">
        <label for="recipient">接收者</label>
        <select id="recipient" name="recipient" class="form-control" required>
          <option value="all">全站用户</option>
          <c:forEach items="${users}" var="user">
            <option value="${user.id}">${user.username}</option>
          </c:forEach>
        </select>
      </div>

      <button type="submit" class="btn btn-primary">发布通知</button>
    </form>
  </div>

  <!-- 通知历史记录 -->
  <div class="notification-history">
    <h3>历史通知记录</h3>

    <!-- 搜索和筛选区域 -->
    <div class="search-filter-container mb-4">
      <form id="searchForm" action="${pageContext.request.contextPath}/admin/notifications" method="get" class="search-filter-form">
        <div class="search-row">
          <div class="search-input-group">
            <label for="searchKeyword">搜索</label>
            <input type="text" id="searchKeyword" name="keyword" class="form-control" placeholder="搜索标题..." value="${param.keyword}">
          </div>

          <div class="search-input-group">
            <label for="filterType">类型筛选</label>
            <select id="filterType" name="type" class="form-control">
              <option value="">全部类型</option>
              <option value="system" ${param.type eq 'system' ? 'selected' : ''}>系统通知</option>
              <option value="announcement" ${param.type eq 'announcement' ? 'selected' : ''}>网站公告</option>
              <option value="like" ${param.type eq 'like' ? 'selected' : ''}>点赞</option>
              <option value="comment" ${param.type eq 'comment' ? 'selected' : ''}>评论</option>
              <option value="reply" ${param.type eq 'reply' ? 'selected' : ''}>回复</option>
            </select>
          </div>

          <div class="search-input-group">
            <label for="filterUser">接收者筛选</label>
            <select id="filterUser" name="userId" class="form-control">
              <option value="">全部用户</option>
              <c:forEach items="${users}" var="user">
                <option value="${user.id}" ${param.userId eq user.id ? 'selected' : ''}>${user.username}</option>
              </c:forEach>
            </select>
          </div>

          <div class="search-input-group search-buttons">
            <button type="submit" class="btn btn-primary">
              <i class="ri-search-line"></i> 搜索
            </button>
            <button type="button" class="btn btn-secondary" onclick="resetFilters()">
              <i class="ri-refresh-line"></i> 重置
            </button>
          </div>
        </div>
      </form>
    </div>

    <div class="table-responsive">
      <table class="admin-table">
        <thead>
        <tr>
          <th>ID</th>
          <th>标题</th>
          <th>类型</th>
          <th>接收者</th>
          <th>发布时间</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${notifications}" var="notification">
          <tr>
            <td>${notification.id}</td>
            <td>${notification.title}</td>
            <td>
              <c:choose>
                <c:when test="${notification.type eq 'system'}">系统通知</c:when>
                <c:when test="${notification.type eq 'announcement'}">网站公告</c:when>
                <c:when test="${notification.type eq 'like'}">点赞</c:when>
                <c:when test="${notification.type eq 'comment'}">评论</c:when>
                <c:when test="${notification.type eq 'reply'}">回复</c:when>
                <c:otherwise>${notification.type}</c:otherwise>
              </c:choose>
            </td>
            <td>
              <c:choose>
                <c:when test="${empty notification.recipientUsername}">未知用户</c:when>
                <c:otherwise>${notification.recipientUsername}</c:otherwise>
              </c:choose>
            </td>
            <td>
              <fmt:formatDate value="${notification.createdAt}" pattern="yyyy-MM-dd HH:mm:ss" />
            </td>
            <td class="action-buttons">
              <button type="button" class="btn-view view-notification-btn"
                      data-id="${notification.id}"
                      data-title="${notification.title}"
                      data-content="${notification.content}">
                <i class="ri-eye-line"></i> 查看
              </button>
              <form action="${pageContext.request.contextPath}/admin/notifications/delete" method="post" style="display: inline;">
                <input type="hidden" name="_method" value="DELETE">
                <input type="hidden" name="notificationId" value="${notification.id}">
                <button type="submit" class="btn-delete" onclick="return confirm('确定要删除此通知吗？')">
                  <i class="ri-delete-bin-line"></i> 删除
                </button>
              </form>
            </td>
          </tr>
        </c:forEach>

        <c:if test="${empty notifications}">
          <tr>
            <td colspan="6" class="text-center">暂无通知记录</td>
          </tr>
        </c:if>
        </tbody>
      </table>
    </div>
  </div>
</div>

<!-- 通知详情模态框 -->
<div id="notificationModal" class="modal" style="display: none;">
  <div class="modal-content">
    <span class="close" onclick="closeModal()">&times;</span>
    <h3 id="modal-title"></h3>
    <div id="modal-content"></div>
  </div>
</div>

<style>
  .notification-form-container {
    background-color: #f9f9f9;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 30px;
    border: 1px solid #eaeaea;
    transition: background-color 0.3s ease, border-color 0.3s ease;
  }

  [data-theme="dark"] .notification-form-container {
    background-color: #2d2d2d;
    border: 1px solid #3a3b3c;
  }

  .notification-form {
    max-width: 800px;
  }

  .notification-form h3, .notification-history h3 {
    font-size: 1.2rem;
    margin-bottom: 15px;
    color: #333;
    transition: color 0.3s ease;
  }

  [data-theme="dark"] .notification-form h3,
  [data-theme="dark"] .notification-history h3 {
    color: #e4e6eb;
  }

  .form-group {
    margin-bottom: 15px;
  }

  .form-group label {
    display: block;
    margin-bottom: 5px;
    font-weight: 500;
    color: #555;
    transition: color 0.3s ease;
  }

  [data-theme="dark"] .form-group label {
    color: #b0b3b8;
  }

  .form-control {
    width: 100%;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 4px;
    background-color: white;
    transition: border-color 0.2s ease, background-color 0.3s ease, color 0.3s ease;
  }

  [data-theme="dark"] .form-control {
    background-color: #3a3b3c;
    border-color: #4a4b4c;
    color: #e4e6eb;
  }

  .form-control:focus {
    border-color: #0078D4;
    outline: none;
    box-shadow: 0 0 0 2px rgba(0, 120, 212, 0.2);
  }

  .mb-3 {
    margin-bottom: 15px;
  }

  .mb-4 {
    margin-bottom: 20px;
  }

  .btn {
    display: inline-block;
    padding: 8px 16px;
    border-radius: 4px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
    border: none;
  }

  .btn-primary {
    background-color: #0078D4;
    color: white;
  }

  .btn-primary:hover {
    background-color: #106ebe;
  }

  .btn-view {
    background-color: #0078D4;
    color: white;
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
    margin-bottom: 5px;
  }

  .btn-view:hover {
    background-color: #106ebe;
  }

  .alert {
    padding: 12px 16px;
    margin-bottom: 15px;
    border-radius: 4px;
  }

  .alert-danger {
    background-color: #fdf3f2;
    border: 1px solid #f3bab6;
    color: #d13438;
  }

  [data-theme="dark"] .alert-danger {
    background-color: rgba(209, 52, 56, 0.2);
    border: 1px solid rgba(209, 52, 56, 0.4);
    color: #f3bab6;
  }

  .alert-success {
    background-color: #f0faf0;
    border: 1px solid #bfe6bf;
    color: #107c10;
  }

  [data-theme="dark"] .alert-success {
    background-color: rgba(16, 124, 16, 0.2);
    border: 1px solid rgba(16, 124, 16, 0.4);
    color: #bfe6bf;
  }

  .text-center {
    text-align: center;
  }

  /* 模态框样式 */
  .modal {
    position: fixed;
    z-index: 1000;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .modal-content {
    background-color: #fff;
    padding: 20px;
    border-radius: 8px;
    max-width: 600px;
    width: 100%;
    max-height: 80vh;
    overflow-y: auto;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    position: relative;
    transition: background-color 0.3s ease, color 0.3s ease;
  }

  [data-theme="dark"] .modal-content {
    background-color: #242526;
    color: #e4e6eb;
  }

  .close {
    position: absolute;
    right: 15px;
    top: 10px;
    font-size: 24px;
    cursor: pointer;
    color: #555;
    transition: color 0.3s ease;
  }

  [data-theme="dark"] .close {
    color: #b0b3b8;
  }

  .close:hover {
    color: #000;
  }

  [data-theme="dark"] .close:hover {
    color: #fff;
  }

  #modal-title {
    margin-top: 0;
    padding-right: 30px;
    color: #333;
    transition: color 0.3s ease;
  }

  [data-theme="dark"] #modal-title {
    color: #e4e6eb;
  }

  #modal-content {
    margin-top: 15px;
    white-space: pre-wrap;
    color: #555;
    transition: color 0.3s ease;
  }

  [data-theme="dark"] #modal-content {
    color: #b0b3b8;
  }

  /* 搜索和筛选样式 */
  .search-filter-container {
    background-color: #f9f9f9;
    padding: 15px;
    border-radius: 8px;
    border: 1px solid #eaeaea;
    margin-bottom: 20px;
    transition: background-color 0.3s ease, border-color 0.3s ease;
  }

  [data-theme="dark"] .search-filter-container {
    background-color: #2d2d2d;
    border: 1px solid #3a3b3c;
  }

  .search-filter-form {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .search-row {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
    align-items: flex-end;
  }

  .search-input-group {
    flex: 1;
    min-width: 200px;
  }

  .search-buttons {
    display: flex;
    gap: 10px;
    align-items: center;
  }

  .btn-secondary {
    background-color: #6c757d;
    color: white;
  }

  .btn-secondary:hover {
    background-color: #5a6268;
  }

  @media (max-width: 768px) {
    .search-row {
      flex-direction: column;
    }

    .search-input-group {
      width: 100%;
    }
  }
</style>

<script>
  // 查看通知详情
  function viewNotificationDetails(id, title, content) {
    document.getElementById('modal-title').innerText = title;
    document.getElementById('modal-content').innerText = content;
    document.getElementById('notificationModal').style.display = 'flex';
  }

  // 页面加载完成后绑定事件
  document.addEventListener('DOMContentLoaded', function() {
    // 为所有查看按钮添加点击事件
    const viewButtons = document.querySelectorAll('.view-notification-btn');
    viewButtons.forEach(button => {
      button.addEventListener('click', function() {
        const id = this.getAttribute('data-id');
        const title = this.getAttribute('data-title');
        const content = this.getAttribute('data-content');
        viewNotificationDetails(id, title, content);
      });
    });
  });

  // 关闭模态框
  function closeModal() {
    document.getElementById('notificationModal').style.display = 'none';
  }

  // 重置筛选
  function resetFilters() {
    document.getElementById('searchKeyword').value = '';
    document.getElementById('filterType').value = '';
    document.getElementById('filterUser').value = '';
    document.getElementById('searchForm').submit();
  }

  // 点击模态框外部关闭
  window.onclick = function(event) {
    const modal = document.getElementById('notificationModal');
    if (event.target === modal) {
      modal.style.display = 'none';
    }
  }

  // ESC键关闭模态框
  document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
      document.getElementById('notificationModal').style.display = 'none';
    }
  });
</script>