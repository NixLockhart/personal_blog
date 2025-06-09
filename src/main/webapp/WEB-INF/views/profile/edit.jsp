<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>修改资料 - 星光小栈</title>
  <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/element/website_logo.png">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/fonts/remixicon.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<!-- 导航栏 -->
<jsp:include page="/WEB-INF/components/header.jsp" />

<div class="profile-container">
  <div class="profile-header">
    <h1><i class="ri-user-settings-line"></i> 修改个人资料</h1>
  </div>

  <div class="profile-form">
    <form action="${pageContext.request.contextPath}/profile/update" method="post" enctype="multipart/form-data">
      <div class="form-group">
        <label for="username"><i class="ri-user-3-line"></i> 用户名</label>
        <input type="text" id="username" name="username" value="${user.username}" required placeholder="请输入用户名">
      </div>

      <div class="form-group">
        <label for="birthday"><i class="ri-calendar-line"></i> 生日</label>
        <input type="date" id="birthday" name="birthday"
               value="<fmt:formatDate value='${user.birthday}' pattern='yyyy-MM-dd'/>">
      </div>

      <div class="form-group">
        <label for="avatar"><i class="ri-image-line"></i> 头像</label>
        <div class="avatar-upload">
          <img src="${pageContext.request.contextPath}/avatar/${user.avatarUrl}?t=${sessionScope.lastAvatarUpdate}"
               alt="当前头像" class="avatar-preview" id="avatarPreview">
          <input type="file" id="avatar" name="avatar" accept="image/jpeg,image/png">
          <label for="avatar" class="avatar-upload-btn">
            <i class="ri-upload-cloud-line"></i> 选择新头像
          </label>
          <div class="avatar-tip">
            <i class="ri-information-line"></i> 支持JPG、PNG格式，大小不超过2MB
          </div>
        </div>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn-update">
          <i class="ri-save-line"></i> 保存修改
        </button>
        <a href="${pageContext.request.contextPath}/profile" class="btn-cancel">
          <i class="ri-arrow-left-line"></i> 返回
        </a>
      </div>
    </form>
  </div>
</div>

<!-- 页脚 -->
<jsp:include page="/WEB-INF/components/footer.jsp" />

<script>
  document.getElementById('avatar').addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (file) {
      if (file.size > 2 * 1024 * 1024) {
        alert('图片大小不能超过2MB');
        this.value = '';
        return;
      }

      const reader = new FileReader();
      reader.onload = function(e) {
        document.getElementById('avatarPreview').src = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  });
</script>
</body>
</html>