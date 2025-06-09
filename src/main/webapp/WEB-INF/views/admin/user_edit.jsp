<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- 用户编辑内容 -->
<div class="admin-card">
  <div class="admin-card-header">
    <h2><i class="ri-user-settings-line"></i> 编辑用户</h2>
  </div>

  <div class="user-edit-form-container">
    <c:if test="${not empty message}">
      <div class="alert alert-${messageType}">
          ${message}
      </div>
    </c:if>

    <form id="userEditForm" action="${pageContext.request.contextPath}/admin/users/update" method="post" enctype="multipart/form-data" class="admin-form">
      <input type="hidden" name="userId" value="${editUser.id}">

      <div class="form-group">
        <label for="id">用户ID</label>
        <input type="text" id="id" value="${editUser.id}" readonly class="form-control readonly">
        <small class="form-text text-muted">用户ID不可修改</small>
      </div>

      <div class="form-group">
        <label for="username">用户名</label>
        <input type="text" id="username" name="username" value="${editUser.username}" class="form-control" required>
      </div>

      <div class="form-group">
        <label for="email">邮箱</label>
        <input type="email" id="email" name="email" value="${editUser.email}" class="form-control" required>
      </div>

      <div class="form-group">
        <label>当前头像</label>
        <div class="avatar-preview">
          <c:choose>
            <c:when test="${not empty editUser.avatarUrl}">
              <img src="${pageContext.request.contextPath}/avatar/${editUser.avatarUrl}?t=${sessionScope.lastAvatarUpdate}" alt="用户头像" id="currentAvatar">
            </c:when>
            <c:otherwise>
              <img src="${pageContext.request.contextPath}/avatar/default.png" alt="默认头像" id="currentAvatar">
            </c:otherwise>
          </c:choose>
        </div>
      </div>

      <div class="form-group">
        <label for="avatar"><i class="ri-image-line"></i> 更新头像</label>
        <div class="avatar-upload">
          <input type="file" id="avatar" name="avatar" accept="image/jpeg,image/png" class="form-control-file">
          <label for="avatar" class="custom-file-label">
            <i class="ri-upload-cloud-line"></i> 选择新头像
          </label>
          <div class="avatar-tip">
            <i class="ri-information-line"></i> 支持JPG、PNG格式，大小不超过2MB
          </div>
        </div>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn btn-primary"><i class="ri-save-line"></i> 保存更改</button>
        <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-secondary"><i class="ri-arrow-go-back-line"></i> 返回</a>
      </div>
    </form>
  </div>
</div>

<style>
  .user-edit-form-container {
    padding: 20px;
  }

  .admin-form {
    max-width: 600px;
    margin: 0 auto;
  }

  .form-group {
    margin-bottom: 20px;
  }

  .form-group label {
    display: block;
    margin-bottom: 8px;
    font-weight: 500;
    color: var(--text-primary);
  }

  .form-control {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid var(--border-color);
    border-radius: 4px;
    background-color: var(--bg-color);
    color: var(--text-primary);
    transition: border-color 0.3s, box-shadow 0.3s;
  }

  .form-control:focus {
    border-color: var(--accent-color);
    box-shadow: 0 0 0 2px rgba(var(--accent-rgb), 0.2);
    outline: none;
  }

  .form-control.readonly {
    background-color: var(--bg-secondary);
    cursor: not-allowed;
  }

  .form-text {
    font-size: 0.875rem;
    margin-top: 4px;
    color: var(--text-secondary);
  }

  .alert {
    padding: 12px 15px;
    margin-bottom: 20px;
    border-radius: 4px;
    border-left: 4px solid;
  }

  .alert-success {
    background-color: var(--success-light-bg);
    border-color: var(--success-color);
    color: var(--success-color);
  }

  .alert-error {
    background-color: var(--error-bg);
    border-color: var(--error-border);
    color: var(--error-text);
  }

  .avatar-upload {
    text-align: center;
    margin-bottom: 20px;
  }

  .avatar-preview {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    overflow: hidden;
    margin-bottom: 15px;
    border: 2px solid var(--border-color);
    margin: 0 auto;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  }

  .avatar-preview img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .custom-file-upload {
    position: relative;
    overflow: hidden;
    display: inline-block;
    margin-top: 10px;
  }

  .custom-file-upload input[type="file"] {
    opacity: 0;
    position: absolute;
    left: 0;
    top: 0;
    width: 0.1px;
    height: 0.1px;
  }

  .custom-file-label {
    display: inline-block;
    padding: 10px 15px;
    background-color: var(--accent-color);
    color: white;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.3s;
    margin-top: 10px;
  }

  .custom-file-label:hover {
    background-color: var(--accent-hover-color);
  }

  .avatar-tip {
    margin-top: 8px;
    font-size: 0.875rem;
    color: var(--text-secondary);
  }

  .form-actions {
    display: flex;
    gap: 10px;
    margin-top: 30px;
  }

  .btn {
    padding: 10px 15px;
    border: none;
    border-radius: 4px;
    font-weight: 500;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    transition: background-color 0.3s, transform 0.2s;
  }

  .btn i {
    margin-right: 6px;
  }

  .btn-primary {
    background-color: var(--accent-color);
    color: white;
  }

  .btn-primary:hover {
    background-color: var(--accent-hover-color);
    transform: translateY(-2px);
  }

  .btn-secondary {
    background-color: var(--bg-secondary);
    color: var(--text-primary);
    text-decoration: none;
  }

  .btn-secondary:hover {
    background-color: var(--hover-bg);
    transform: translateY(-2px);
  }

  @media (max-width: 768px) {
    .admin-form {
      max-width: 100%;
    }

    .form-actions {
      flex-direction: column;
    }

    .btn {
      width: 100%;
      margin-bottom: 10px;
    }
  }
</style>

<script>
  document.addEventListener('DOMContentLoaded', function() {
    // 文件上传预览
    const avatarInput = document.getElementById('avatar');
    const currentAvatar = document.getElementById('currentAvatar');

    if (avatarInput && currentAvatar) {
      avatarInput.addEventListener('change', function() {
        if (this.files && this.files[0]) {
          const file = this.files[0];

          // 检查文件大小
          if (file.size > 2 * 1024 * 1024) { // 2MB
            alert('图片大小不能超过2MB');
            this.value = '';
            return;
          }

          // 检查文件类型
          const fileType = file.type;
          if (fileType !== 'image/jpeg' && fileType !== 'image/png') {
            alert('只支持JPG和PNG格式的图片');
            this.value = '';
            return;
          }

          const reader = new FileReader();

          reader.onload = function(e) {
            currentAvatar.src = e.target.result;
          };

          reader.readAsDataURL(file);

          // 更新文件名显示
          const fileName = file.name;
          const fileLabel = document.querySelector('.custom-file-label');
          if (fileLabel) {
            fileLabel.innerHTML = '<i class="ri-upload-cloud-line"></i> ' +
                    (fileName.length > 20 ? fileName.substring(0, 17) + '...' : fileName);
          }
        }
      });
    }

    // 表单验证
    const userEditForm = document.getElementById('userEditForm');
    if (userEditForm) {
      userEditForm.addEventListener('submit', function(e) {
        const username = document.getElementById('username').value.trim();
        const email = document.getElementById('email').value.trim();

        let isValid = true;
        let errorMessage = '';

        // 只验证用户名和邮箱不为空
        if (username === '') {
          isValid = false;
          errorMessage = '用户名不能为空';
        } else if (email === '') {
          isValid = false;
          errorMessage = '邮箱不能为空';
        }

        if (!isValid) {
          e.preventDefault();
          alert(errorMessage);
        }
      });
    }
  });
</script>