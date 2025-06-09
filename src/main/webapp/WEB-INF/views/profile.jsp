<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="com.blog.util.IPUtil" %>
<%@ page import="com.blog.util.AvatarUtil" %>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>个人中心 - 星光小栈</title>
  <!-- 添加网站logo -->
  <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/element/website_logo.png">
  <!-- 本地Remixicon资源 -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/fonts/remixicon.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<!-- 导航栏 -->
<jsp:include page="/WEB-INF/components/header.jsp" />

<div class="profile-container">
  <div class="profile-header">
    <h1><i class="ri-user-line"></i> 个人信息</h1>
  </div>

  <div class="profile-info">
    <div class="profile-avatar">
      <c:choose>
        <c:when test="${not empty user.avatarUrl}">
          <img src="${pageContext.request.contextPath}/avatar/${user.avatarUrl}" alt="用户头像" class="avatar-large">
        </c:when>
        <c:otherwise>
          <img src="${pageContext.request.contextPath}/avatar/default.png" alt="默认头像" class="avatar-large">
        </c:otherwise>
      </c:choose>
      <div class="user-title">${user.username}</div>
    </div>

    <div class="info-group">
      <div class="info-item">
        <i class="ri-user-line"></i>
        <div class="info-content">
          <label>用户名</label>
          <span>${user.username}</span>
        </div>
      </div>

      <div class="info-item">
        <i class="ri-mail-line"></i>
        <div class="info-content">
          <label>邮箱</label>
          <span>${user.email}</span>
        </div>
      </div>

      <div class="info-item">
        <i class="ri-calendar-line"></i>
        <div class="info-content">
          <label>生日</label>
          <span><fmt:formatDate value="${user.birthday}" pattern="yyyy-MM-dd" var="birthdayStr"/>${not empty birthdayStr ? birthdayStr : '未设置'}</span>
        </div>
      </div>

      <div class="info-item">
        <i class="ri-global-line"></i>
        <div class="info-content">
          <label>IP地址</label>
          <span><%= IPUtil.getClientIP(request) %></span>
        </div>
      </div>

      <div class="info-item">
        <i class="ri-map-pin-line"></i>
        <div class="info-content">
          <label>所在地</label>
          <span><%= IPUtil.getProvinceByIP(IPUtil.getClientIP(request)) %></span>
        </div>
      </div>

      <div class="info-item">
        <i class="ri-time-line"></i>
        <div class="info-content">
          <label>注册时间</label>
          <span><fmt:formatDate value="${user.createdAt}" pattern="yyyy-MM-dd" var="createdAtStr"/>${not empty createdAtStr ? createdAtStr : '未知'}</span>
        </div>
      </div>
    </div>

    <div class="profile-actions">
      <a href="${pageContext.request.contextPath}/profile/edit" class="btn-edit">
        <i class="ri-edit-line"></i> 修改资料
      </a>
      <a href="${pageContext.request.contextPath}/profile/password" class="btn-password">
        <i class="ri-lock-line"></i> 修改密码
      </a>
    </div>
  </div>
</div>

<!-- 页脚 -->
<jsp:include page="/WEB-INF/components/footer.jsp" />

<script>
  // 添加随机查询参数强制刷新头像缓存
  document.addEventListener('DOMContentLoaded', function() {
    const avatarImages = document.querySelectorAll('img[src*="/avatar/"]');
    const timestamp = new Date().getTime();

    avatarImages.forEach(function(img) {
      const currentSrc = img.getAttribute('src');
      img.setAttribute('src', currentSrc + '?t=' + timestamp);
    });
  });
</script>
</body>
</html>