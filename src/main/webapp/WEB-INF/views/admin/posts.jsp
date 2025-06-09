<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- 消息显示区域 -->
<div class="admin-card">
  <c:if test="${not empty sessionScope.success}">
    <div class="alert alert-success">
      <i class="ri-checkbox-circle-line"></i> ${sessionScope.success}
    </div>
    <c:remove var="success" scope="session"/>
  </c:if>

  <c:if test="${not empty sessionScope.warning}">
    <div class="alert alert-warning">
      <i class="ri-alert-line"></i> ${sessionScope.warning}
    </div>
    <c:remove var="warning" scope="session"/>
  </c:if>

  <c:if test="${not empty sessionScope.error}">
    <div class="alert alert-error">
      <i class="ri-error-warning-line"></i> ${sessionScope.error}
    </div>
    <c:remove var="error" scope="session"/>
  </c:if>
</div>

<!-- 内容管理 -->
<div class="admin-card">
  <div class="admin-card-header">
    <h2><i class="ri-file-list-line"></i> 内容管理</h2>
    <div>
      <a href="${pageContext.request.contextPath}/admin/posts/new" class="admin-action-btn">
        <i class="ri-add-line"></i> 新建博文
      </a>
    </div>
  </div>

  <div class="post-management">
    <c:choose>
      <c:when test="${not empty posts}">
        <table class="admin-table">
          <thead>
          <tr>
            <th>ID</th>
            <th>标题</th>
            <th>作者</th>
            <th>分类</th>
            <th>发布时间</th>
            <th>浏览量</th>
            <th>点赞数</th>
            <th>操作</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach items="${posts}" var="post">
            <tr>
              <td>${post.id}</td>
              <td>
                <c:choose>
                  <c:when test="${fn:length(post.title) > 20}">
                    ${fn:substring(post.title, 0, 20)}...
                  </c:when>
                  <c:otherwise>
                    ${post.title}
                  </c:otherwise>
                </c:choose>
              </td>
              <td>${post.authorName}</td>
              <td>${post.categoryName}</td>
              <td><fmt:formatDate value="${post.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
              <td>${post.views}</td>
              <td>${post.likes}</td>
              <td class="action-buttons">
                <button class="btn-edit view-btn" style="background-color: var(--success-color);" data-id="${post.id}">
                  <i class="ri-eye-line"></i> 查看
                </button>
                <button class="btn-edit edit-btn" data-id="${post.id}">
                  <i class="ri-edit-line"></i> 编辑
                </button>
                <button class="btn-delete delete-btn" data-id="${post.id}" data-title="${post.title}">
                  <i class="ri-delete-bin-line"></i> 删除
                </button>
              </td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
      </c:when>
      <c:otherwise>
        <p>暂无文章数据</p>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<!-- 内容管理脚本 -->
<script>
  document.addEventListener('DOMContentLoaded', function() {
    // 给查看按钮添加事件监听
    document.querySelectorAll('.view-btn').forEach(function(btn) {
      btn.addEventListener('click', function() {
        var postId = this.getAttribute('data-id');
        window.location.href = '${pageContext.request.contextPath}/posts/' + postId;
      });
    });

    // 给编辑按钮添加事件监听
    document.querySelectorAll('.edit-btn').forEach(function(btn) {
      btn.addEventListener('click', function() {
        var postId = this.getAttribute('data-id');
        editPost(postId);
      });
    });

    // 给删除按钮添加事件监听
    document.querySelectorAll('.delete-btn').forEach(function(btn) {
      btn.addEventListener('click', function() {
        var postId = this.getAttribute('data-id');
        var title = this.getAttribute('data-title');
        deletePost(postId, title);
      });
    });
  });

  function editPost(postId) {
    // 跳转到编辑页面
    window.location.href = '${pageContext.request.contextPath}/admin/posts/edit/' + postId;
  }

  function deletePost(postId, title) {
    if (confirm('确定要删除文章 "' + title + '" 吗？此操作不可恢复！')) {
      // 发送删除请求
      const form = document.createElement('form');
      form.method = 'POST';
      form.action = '${pageContext.request.contextPath}/admin/posts/delete';

      const idInput = document.createElement('input');
      idInput.type = 'hidden';
      idInput.name = 'postId';
      idInput.value = postId;

      form.appendChild(idInput);
      document.body.appendChild(form);
      form.submit();
    }
  }
</script>