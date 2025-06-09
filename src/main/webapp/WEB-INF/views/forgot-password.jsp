<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>忘记密码 - 星光小栈</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/element/website_logo.png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/fonts/remixicon.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<!-- 导航栏 -->
<jsp:include page="/WEB-INF/components/header.jsp" />

<div class="profile-container">
    <div class="profile-header">
        <h1><i class="ri-key-2-line"></i> 重置密码</h1>
    </div>

    <div class="profile-form">
        <c:if test="${not empty message}">
            <div class="message ${messageType}">
                <i class="ri-${messageType == 'success' ? 'check-line' : 'error-warning-line'}"></i>
                    ${message}
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/forgot-password" method="post" id="passwordForm">
            <div class="form-group">
                <label for="email"><i class="ri-mail-line"></i> 邮箱</label>
                <div class="email-verify">
                    <input type="email" id="email" name="email" required placeholder="请输入注册时的邮箱">
                    <button type="button" class="btn-verify" id="sendCode">
                        <i class="ri-mail-send-line"></i> 发送验证码
                    </button>
                </div>
                <div id="emailFeedback" class="feedback-message"></div>
            </div>

            <div class="form-group">
                <label for="verifyCode"><i class="ri-shield-keyhole-line"></i> 验证码</label>
                <input type="text" id="verifyCode" name="verifyCode" required placeholder="请输入收到的验证码">
            </div>

            <div class="form-group">
                <label for="newPassword"><i class="ri-lock-line"></i> 新密码</label>
                <input type="password" id="newPassword" name="newPassword" required placeholder="请输入新密码，至少6位">
                <div id="passwordStrength" class="password-strength">
                    <div class="strength-bar"></div>
                    <span class="strength-text"></span>
                </div>
            </div>

            <div class="form-group">
                <label for="confirmPassword"><i class="ri-check-line"></i> 确认密码</label>
                <input type="password" id="confirmPassword" name="confirmPassword" required placeholder="请再次输入新密码">
                <div id="passwordMatch" class="feedback-message"></div>
            </div>

            <div class="form-actions">
                <button type="submit" class="btn-update">
                    <i class="ri-save-line"></i> 重置密码
                </button>
                <a href="${pageContext.request.contextPath}/login" class="btn-cancel">
                    <i class="ri-arrow-left-line"></i> 返回登录
                </a>
            </div>
        </form>
    </div>
</div>

<!-- 页脚 -->
<jsp:include page="/WEB-INF/components/footer.jsp" />

<script>
    let countdown = 60;
    const sendCodeBtn = document.getElementById('sendCode');
    const passwordForm = document.getElementById('passwordForm');
    const emailFeedback = document.getElementById('emailFeedback');
    const passwordStrength = document.getElementById('passwordStrength');
    const strengthBar = passwordStrength.querySelector('.strength-bar');
    const strengthText = passwordStrength.querySelector('.strength-text');
    const passwordMatch = document.getElementById('passwordMatch');

    // 检查两次密码输入是否一致
    document.getElementById('confirmPassword').addEventListener('input', function() {
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = this.value;

        if (!confirmPassword) {
            passwordMatch.innerHTML = '';
            passwordMatch.className = 'feedback-message';
            return;
        }

        if (newPassword === confirmPassword) {
            passwordMatch.innerHTML = '<i class="ri-check-line"></i> 密码匹配';
            passwordMatch.className = 'feedback-message success';
        } else {
            passwordMatch.innerHTML = '<i class="ri-close-line"></i> 密码不匹配';
            passwordMatch.className = 'feedback-message error';
        }
    });

    // 检查密码强度
    document.getElementById('newPassword').addEventListener('input', function() {
        const password = this.value;
        let strength = 0;
        let feedback = '';

        if (!password) {
            passwordStrength.style.display = 'none';
            return;
        } else {
            passwordStrength.style.display = 'block';
        }

        // 长度检查
        if (password.length < 6) {
            feedback = '密码太短';
        } else {
            strength += 1;
            if (password.length >= 8) strength += 1;
        }

        // 复杂度检查
        if (/[A-Z]/.test(password)) strength += 1;
        if (/[0-9]/.test(password)) strength += 1;
        if (/[^A-Za-z0-9]/.test(password)) strength += 1;

        // 设置强度条和文本
        if (strength < 2) {
            strengthBar.style.width = '20%';
            strengthBar.style.backgroundColor = '#e74c3c';
            strengthText.textContent = '弱';
            strengthText.style.color = '#e74c3c';
        } else if (strength < 4) {
            strengthBar.style.width = '50%';
            strengthBar.style.backgroundColor = '#f1c40f';
            strengthText.textContent = '中';
            strengthText.style.color = '#f1c40f';
        } else {
            strengthBar.style.width = '100%';
            strengthBar.style.backgroundColor = '#2ecc71';
            strengthText.textContent = '强';
            strengthText.style.color = '#2ecc71';
        }
    });

    // 发送验证码
    sendCodeBtn.addEventListener('click', function() {
        const email = document.getElementById('email').value;
        if (!email) {
            emailFeedback.innerHTML = '<i class="ri-error-warning-line"></i> 请输入邮箱地址';
            emailFeedback.className = 'feedback-message error';
            return;
        }

        // 重置反馈信息
        emailFeedback.innerHTML = '<i class="ri-loader-4-line ri-spin"></i> 正在发送验证码...';
        emailFeedback.className = 'feedback-message';

        // 发送验证码请求
        fetch('${pageContext.request.contextPath}/profile/password/send-code', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: 'email=' + encodeURIComponent(email)
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    startCountdown();
                    emailFeedback.innerHTML = '<i class="ri-check-line"></i> ' + data.message;
                    emailFeedback.className = 'feedback-message success';
                } else {
                    emailFeedback.innerHTML = '<i class="ri-error-warning-line"></i> ' + data.message;
                    emailFeedback.className = 'feedback-message error';
                }
            })
            .catch(error => {
                emailFeedback.innerHTML = '<i class="ri-error-warning-line"></i> 发送验证码失败，请稍后重试';
                emailFeedback.className = 'feedback-message error';
            });
    });

    // 倒计时
    function startCountdown() {
        sendCodeBtn.disabled = true;
        sendCodeBtn.innerHTML = countdown + "秒后重试";

        const timer = setInterval(() => {
            countdown--;
            sendCodeBtn.innerHTML = countdown + "秒后重试";

            if (countdown <= 0) {
                clearInterval(timer);
                sendCodeBtn.disabled = false;
                sendCodeBtn.innerHTML = '<i class="ri-mail-send-line"></i> 发送验证码';
                countdown = 60;
            }
        }, 1000);
    }

    // 表单提交验证
    passwordForm.addEventListener('submit', function(e) {
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        if (newPassword !== confirmPassword) {
            e.preventDefault();
            passwordMatch.innerHTML = '<i class="ri-close-line"></i> 两次输入的密码不一致';
            passwordMatch.className = 'feedback-message error';
            document.getElementById('confirmPassword').focus();
            return;
        }

        if (newPassword.length < 6) {
            e.preventDefault();
            emailFeedback.innerHTML = '<i class="ri-error-warning-line"></i> 密码长度不能少于6位';
            emailFeedback.className = 'feedback-message error';
            document.getElementById('newPassword').focus();
            return;
        }
    });
</script>
</body>
</html>