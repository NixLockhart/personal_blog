<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- 用户管理内容 -->
<div class="admin-card">
  <div class="admin-card-header">
    <h2><i class="ri-user-settings-line"></i> 用户管理</h2>
  </div>

  <div class="user-management">
    <c:choose>
      <c:when test="${not empty users}">
        <table class="admin-table">
          <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>邮箱</th>
            <th>注册时间</th>
            <th>管理员</th>
            <th>操作</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach items="${users}" var="user">
            <tr>
              <td>${user.id}</td>
              <td>${user.username}</td>
              <td>${user.email}</td>
              <td><fmt:formatDate value="${user.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
              <td>
                <c:choose>
                  <c:when test="${user.id < 5}">
                    <span style="color: var(--accent-color);"><i class="ri-checkbox-circle-line"></i> 是</span>
                  </c:when>
                  <c:otherwise>
                    <span>否</span>
                  </c:otherwise>
                </c:choose>
              </td>
              <td class="action-buttons">
                <button class="btn-edit" onclick="editUser(${user.id})"><i class="ri-edit-line"></i> 编辑</button>
                <c:if test="${sessionScope.user.id != user.id}">
                  <button class="btn-delete" onclick="deleteUser(${user.id}, '${user.username}')"><i class="ri-delete-bin-line"></i> 删除</button>
                </c:if>
              </td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
      </c:when>
      <c:otherwise>
        <p>暂无用户数据</p>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<!-- 用户管理脚本 -->
<script>
  function editUser(userId) {
    // 跳转到用户编辑页面
    window.location.href = "${pageContext.request.contextPath}/admin/users/edit?userId=" + userId;
  }

  function deleteUser(userId, username) {
    if (confirm('确定要删除用户 "' + username + '" 吗？此操作不可恢复！')) {
      // 发送删除请求
      const form = document.createElement('form');
      form.method = 'POST';
      form.action = '${pageContext.request.contextPath}/admin/users/delete';

      const idInput = document.createElement('input');
      idInput.type = 'hidden';
      idInput.name = 'userId';
      idInput.value = userId;

      form.appendChild(idInput);
      document.body.appendChild(form);
      form.submit();
    }
  }
</script>