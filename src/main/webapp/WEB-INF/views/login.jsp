<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>用户登录 - 星光小栈</title>
  <!-- 添加网站logo -->
  <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/element/website_logo.png">
  <!-- 使用本地Remixicon资源替代CDN -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/fonts/remixicon.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
</head>
<body class="login-page">
<!-- 导航栏 -->
<jsp:include page="/WEB-INF/components/header_login.jsp" />

<div class="auth-container">
  <div class="auth-card">
    <form id="loginForm" class="login-form" onsubmit="return handleLogin(event)">
      <h1 class="auth-title"><i class="ri-login-circle-line"></i> 登录</h1>

      <!-- 登录方式选择 -->
      <div class="form-group">
        <label><i class="ri-settings-line"></i> 登录方式</label>
        <div class="login-type-selector">
          <label class="radio-option">
            <input type="radio" name="loginType" value="password" checked onchange="switchLoginType()">
            <span>密码登录</span>
          </label>
          <label class="radio-option">
            <input type="radio" name="loginType" value="verification" onchange="switchLoginType()">
            <span>验证码登录</span>
          </label>
        </div>
      </div>

      <div class="form-group">
        <label for="username"><i class="ri-user-line"></i> <span id="usernameLabel">用户名或邮箱</span></label>
        <input type="text" id="username" name="username" required>
      </div>

      <div class="form-group" id="passwordGroup">
        <label for="password"><i class="ri-lock-line"></i> 密码</label>
        <input type="password" id="password" name="password" required>
      </div>

      <div class="form-group" id="loginVerificationCodeGroup" style="display: none;">
        <label for="loginVerificationCode"><i class="ri-key-line"></i> 验证码</label>
        <div style="display: flex;">
          <input type="text" id="loginVerificationCode" name="verificationCode" style="flex: 1; margin-right: 10px;">
          <button type="button" id="sendLoginCode" class="btn btn-secondary" style="width: 120px;"
                  onclick="sendVerificationCodeForLogin()">发送验证码</button>
        </div>
      </div>

      <button type="submit" class="btn">登录</button>
      <div class="auth-links">
        <a href="${pageContext.request.contextPath}/forgot-password" class="forgot-password-link">
          <i class="ri-question-line"></i> 忘记密码
        </a>
        <a href="#" onclick="toggleForm()" class="register-link">
          <i class="ri-user-add-line"></i> 立即注册
        </a>
      </div>
    </form>

    <!-- 注册表单 -->
    <form id="registerForm" class="login-form" onsubmit="return handleRegister(event)" style="display: none;">
      <h1 class="auth-title"><i class="ri-user-add-line"></i> 注册</h1>
      <div class="form-group">
        <label for="regUsername"><i class="ri-user-line"></i> 用户名</label>
        <input type="text" id="regUsername" name="username" required>
      </div>
      <div class="form-group">
        <label for="regEmail"><i class="ri-mail-line"></i> 邮箱</label>
        <input type="email" id="regEmail" name="email" required>
      </div>
      <div class="form-group">
        <label for="regVerificationCode"><i class="ri-key-line"></i> 验证码</label>
        <div style="display: flex;">
          <input type="text" id="regVerificationCode" name="verificationCode" required style="flex: 1; margin-right: 10px;">
          <button type="button" id="sendRegisterCode" class="btn btn-secondary" style="width: 120px;"
                  onclick="sendVerificationCode(document.getElementById('regEmail').value, 'register')">发送验证码</button>
        </div>
      </div>
      <div class="form-group">
        <label for="regPassword"><i class="ri-lock-line"></i> 密码</label>
        <input type="password" id="regPassword" name="password" required>
      </div>
      <button type="submit" class="btn">注册</button>
      <div class="toggle-form">
        <a href="#" onclick="toggleForm()">已有账号？立即登录</a>
      </div>
    </form>

    <div id="errorMessage" class="error-message" style="margin-top: 10px; padding: 12px; border-radius: 4px; text-align: center; font-size: 14px; transition: all 0.3s ease;"></div>
  </div>
</div>

<!-- 页脚 -->
<jsp:include page="/WEB-INF/components/footer.jsp" />

<script>
  document.addEventListener('DOMContentLoaded', function() {
    // 初始化表单显示
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
      registerForm.style.display = 'none';
    }

    // 检查URL参数，显示错误消息
    const urlParams = new URLSearchParams(window.location.search);
    const error = urlParams.get('error');
    if (error) {
      showError(error);
    }

    // 检查是否需要显示验证码输入框
    const requireVerification = urlParams.get('requireVerification');
    if (requireVerification === 'true') {
      document.getElementById('loginVerificationCodeGroup').style.display = 'block';
    }
  });

  function toggleForm() {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const errorMessage = document.getElementById('errorMessage');

    if (loginForm.style.display === 'none') {
      loginForm.style.display = 'flex';
      registerForm.style.display = 'none';
    } else {
      loginForm.style.display = 'none';
      registerForm.style.display = 'flex';
    }
    errorMessage.style.display = 'none';
  }

  // 显示错误消息
  function showError(message) {
    const errorMessage = document.getElementById('errorMessage');
    errorMessage.textContent = message;
    errorMessage.style.display = 'block';
    errorMessage.style.backgroundColor = '#fff5f5';
    errorMessage.style.color = '#ff6b6b';
    errorMessage.style.border = '1px solid #ffd6d6';

    // 5秒后隐藏消息
    setTimeout(() => {
      errorMessage.style.display = 'none';
    }, 5000);
  }

  // 显示成功消息
  function showSuccess(message) {
    const errorMessage = document.getElementById('errorMessage');
    errorMessage.textContent = message;
    errorMessage.style.display = 'block';
    errorMessage.style.backgroundColor = '#f5fff5';
    errorMessage.style.color = '#4caf50';
    errorMessage.style.border = '1px solid #d6ffd6';

    // 3秒后隐藏消息
    setTimeout(() => {
      errorMessage.style.display = 'none';
    }, 3000);
  }

  // 切换登录方式
  function switchLoginType() {
    const loginType = document.querySelector('input[name="loginType"]:checked').value;
    const usernameLabel = document.getElementById('usernameLabel');
    const passwordGroup = document.getElementById('passwordGroup');
    const verificationCodeGroup = document.getElementById('loginVerificationCodeGroup');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');

    // 重置表单
    usernameInput.value = '';
    passwordInput.value = '';
    document.getElementById('loginVerificationCode').value = '';

    // 先隐藏所有可变元素，避免布局闪烁
    passwordGroup.style.display = 'none';
    verificationCodeGroup.style.display = 'none';

    // 使用setTimeout确保DOM更新完成后再显示相应元素
    setTimeout(() => {
      if (loginType === 'password') {
        usernameLabel.textContent = '用户名或邮箱';
        usernameInput.placeholder = '请输入用户名或邮箱';
        usernameInput.type = 'text';
        passwordGroup.style.display = 'block';
        passwordInput.required = true;
      } else if (loginType === 'verification') {
        usernameLabel.textContent = '邮箱';
        usernameInput.placeholder = '请输入邮箱地址';
        usernameInput.type = 'email';
        verificationCodeGroup.style.display = 'block';
        passwordInput.required = false;
      }

      // 强制重绘以确保布局正确
      const loginForm = document.getElementById('loginForm');
      if (loginForm) {
        loginForm.style.height = 'auto';
        // 触发重排
        loginForm.offsetHeight;
      }
    }, 10);
  }

  // 发送验证码（登录用）
  function sendVerificationCodeForLogin() {
    const username = document.getElementById('username').value;
    const loginType = document.querySelector('input[name="loginType"]:checked').value;

    if (!username) {
      showError('请输入用户名或邮箱');
      return;
    }

    // 如果是验证码登录模式，需要先获取用户邮箱
    if (loginType === 'verification') {
      sendVerificationCode(username, 'login');
    } else {
      showError('当前登录方式不需要验证码');
    }
  }

  // 处理登录表单提交
  function handleLogin(event) {
    event.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const verificationCode = document.getElementById('loginVerificationCode')?.value;
    const loginType = document.querySelector('input[name="loginType"]:checked').value;

    if (!username) {
      showError('用户名/邮箱不能为空');
      return false;
    }

    // 验证码登录模式
    if (loginType === 'verification') {
      if (!verificationCode) {
        showError('请输入验证码');
        return false;
      }
    } else {
      // 用户名/邮箱+密码登录模式
      if (!password) {
        showError('密码不能为空');
        return false;
      }
    }

    // 创建表单数据
    const formData = new FormData();
    formData.append('username', username);
    formData.append('loginType', loginType);

    if (loginType === 'verification') {
      formData.append('verificationCode', verificationCode);
    } else if (loginType === 'password') {
      formData.append('password', password);
    }

    // 发送登录请求
    fetch('${pageContext.request.contextPath}/login', {
      method: 'POST',
      body: new URLSearchParams(formData)
    })
            .then(response => response.json())
            .then(data => {
              if (data.success) {
                // 登录成功，重定向到指定页面
                window.location.href = data.redirect;
              } else {
                // 登录失败，显示错误信息
                showError(data.message || '登录失败');

                // 如果需要验证码，显示验证码输入框
                if (data.requireVerification) {
                  document.getElementById('loginVerificationCodeGroup').style.display = 'block';
                }
              }
            })
            .catch(error => {
              console.error('登录请求失败:', error);
              showError('登录请求失败，请稍后重试');
            });

    return false;
  }

  // 发送验证码
  function sendVerificationCode(email, type) {
    if (!email) {
      showError('请输入邮箱地址');
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      showError('请输入有效的邮箱地址');
      return;
    }

    // 禁用发送按钮并开始倒计时
    const sendButton = document.getElementById(type === 'login' ? 'sendLoginCode' : 'sendRegisterCode');
    if (sendButton) {
      const originalText = sendButton.textContent;
      sendButton.disabled = true;
      let countdown = 60;
      sendButton.textContent = countdown + 's';

      const timer = setInterval(() => {
        countdown--;
        if (countdown > 0) {
          console.log('Current countdown:', countdown);
          sendButton.textContent = countdown + 's';
        } else {
          clearInterval(timer);
          sendButton.disabled = false;
          sendButton.textContent = originalText;
        }
      }, 1000);

      // 发送请求获取验证码
      fetch('${pageContext.request.contextPath}/sendVerificationCode', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
          email: email,
          type: type
        })
      })
              .then(response => response.json())
              .then(data => {
                if (data.success) {
                } else {
                  showError(data.message || '验证码发送失败');
                  // 发送失败时重置倒计时
                  clearInterval(timer);
                  sendButton.disabled = false;
                  sendButton.textContent = '发送验证码';
                }
              })
              .catch(error => {
                console.error('验证码请求失败:', error);
                showError('验证码请求失败，请稍后重试');
                // 发送失败时重置倒计时
                clearInterval(timer);
                sendButton.disabled = false;
                sendButton.textContent = '发送验证码';
              });
    }
  }

  // 处理注册表单提交
  function handleRegister(event) {
    event.preventDefault(); // 阻止表单默认提交行为

    const username = document.getElementById('regUsername').value;
    const email = document.getElementById('regEmail').value;
    const password = document.getElementById('regPassword').value;
    const verificationCode = document.getElementById('regVerificationCode').value;

    if (!username || !email || !password || !verificationCode) {
      showError('所有字段都是必填的');
      return false;
    }

    // 验证邮箱格式
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      showError('请输入有效的邮箱地址');
      return false;
    }

    // 创建表单数据
    const formData = new FormData();
    formData.append('username', username);
    formData.append('email', email);
    formData.append('password', password);
    formData.append('verificationCode', verificationCode);

    // 发送注册请求
    fetch('${pageContext.request.contextPath}/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: new URLSearchParams(formData)
    })
            .then(response => response.json())
            .then(data => {
              if (data.success) {
                // 注册成功，显示成功消息并切换到登录表单
                showSuccess('注册成功！请登录');
                setTimeout(() => {
                  document.getElementById('loginForm').style.display = 'flex';
                  document.getElementById('registerForm').style.display = 'none';
                }, 2000);
              } else {
                // 注册失败，显示错误信息
                showError(data.message || '注册失败');
              }
            })
            .catch(error => {
              console.error('注册请求失败:', error);
              showError('注册请求失败，请稍后重试');
            });

    return false;
  }
</script>
</body>
</html>