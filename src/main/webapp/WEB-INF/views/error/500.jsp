<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>500 - 服务器错误</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/element/website_logo.png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/fonts/remixicon.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .error-container {
            text-align: center;
            padding: 50px 20px;
            max-width: 800px;
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
        .error-details {
            background-color: #f8f9fa;
            padding: 20px;
            border-radius: 5px;
            margin: 20px 0;
            text-align: left;
            font-family: monospace;
            white-space: pre-wrap;
            word-wrap: break-word;
        }
        .error-details h3 {
            margin-top: 0;
            color: #e74c3c;
        }
        .error-stack {
            background-color: #2c3e50;
            color: #ecf0f1;
            padding: 15px;
            border-radius: 5px;
            margin-top: 10px;
            max-height: 300px;
            overflow-y: auto;
            font-size: 14px;
        }
        .back-button {
            display: inline-block;
            padding: 10px 20px;
            background-color: #3498db;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            transition: background-color 0.3s;
            margin-top: 20px;
        }
        .back-button:hover {
            background-color: #2980b9;
        }
        .toggle-details {
            background-color: #3498db;
            color: white;
            border: none;
            padding: 5px 10px;
            border-radius: 3px;
            cursor: pointer;
            margin-top: 10px;
        }
        .toggle-details:hover {
            background-color: #2980b9;
        }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/components/header.jsp" />

<div class="container">
    <div class="error-container">
        <div class="error-code">500</div>
        <div class="error-message">抱歉，服务器内部错误</div>

        <c:if test="${not empty requestScope.error}">
            <div class="error-details">
                <h3>错误信息</h3>
                <p>${requestScope.error}</p>

                <c:if test="${not empty requestScope.errorDetail}">
                    <h3>详细错误</h3>
                    <p>${requestScope.errorDetail}</p>
                </c:if>

                <c:if test="${not empty requestScope.errorType}">
                    <h3>错误类型</h3>
                    <p>${requestScope.errorType}</p>
                </c:if>

                <c:if test="${not empty requestScope.errorStack}">
                    <button class="toggle-details" onclick="toggleStack()">显示/隐藏错误堆栈</button>
                    <div id="errorStack" class="error-stack" style="display: none;">
                        <c:forEach items="${requestScope.errorStack}" var="stack">
                            ${stack}<br>
                        </c:forEach>
                    </div>
                </c:if>
            </div>
        </c:if>

        <p>可能的原因：</p>
        <ul style="text-align: left; display: inline-block;">
            <li>服务器临时过载或维护中</li>
            <li>应用程序出现错误</li>
            <li>数据库连接问题</li>
        </ul>
        <div style="margin-top: 30px;">
            <a href="${pageContext.request.contextPath}/" class="back-button">返回首页</a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/components/footer.jsp" />

<script>
    function toggleStack() {
        const stack = document.getElementById('errorStack');
        stack.style.display = stack.style.display === 'none' ? 'block' : 'none';
    }
</script>
</body>
</html>