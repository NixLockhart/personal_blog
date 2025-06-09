<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>404 - 页面未找到</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/element/website_logo.png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/fonts/remixicon.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .error-container {
            text-align: center;
            padding: 50px 20px;
            max-width: 600px;
            margin: 0 auto;
            font-family: 'Microsoft YaHei', sans-serif;
        }
        .error-code {
            font-size: 72px;
            font-weight: bold;
            color: #e74c3c;
            margin-bottom: 20px;
        }
        .error-message {
            font-size: 24px;
            margin-bottom: 30px;
        }
        .back-button {
            display: inline-block;
            padding: 10px 20px;
            background-color: #3498db;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            transition: background-color 0.3s;
        }
        .back-button:hover {
            background-color: #2980b9;
        }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/components/header.jsp" />

<div class="container">
    <div class="error-container">
        <div class="error-code">404</div>
        <div class="error-message">抱歉，您请求的页面不存在</div>
        <p>可能的原因：</p>
        <ul style="text-align: left; display: inline-block;">
            <li>URL地址输入错误</li>
            <li>该页面已被移除或重命名</li>
            <li>您没有访问该页面的权限</li>
        </ul>
        <div style="margin-top: 30px;">
            <a href="${pageContext.request.contextPath}/" class="back-button">返回首页</a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/components/footer.jsp" />
</body>
</html>