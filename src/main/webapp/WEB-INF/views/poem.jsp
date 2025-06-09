<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>诗词详情 - 星光小栈</title>
  <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/element/website_logo.png">
  <!-- 本地Remixicon资源 -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/fonts/remixicon.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/poem.css">
</head>
<body>
<!-- 导航栏 -->
<jsp:include page="/WEB-INF/components/header.jsp" />

<!-- 主要内容 -->
<div class="container">
  <div class="poem-detail">
    <div class="poem-header">
      <h1 class="poem-title">${poem.origin.title}</h1>
      <div class="poem-meta">
        <span class="poem-author">【${poem.origin.dynasty}】${poem.origin.author}</span>
      </div>
    </div>

    <div class="poem-content">
      <c:forEach items="${poem.origin.content}" var="line">
        <p class="poem-line">${line}</p>
      </c:forEach>
    </div>

    <div class="poem-translation">
      <h3>译文</h3>
      <div class="translation-content">
        <c:choose>
          <c:when test="${empty poem.origin.translate}">
            <p class="translation-line">暂无译文</p>
          </c:when>
          <c:otherwise>
            <c:forEach items="${poem.origin.translate}" var="trans">
              <p class="translation-line">${trans}</p>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </div>
    </div>

    <div class="poem-tags">
      <h3>标签</h3>
      <div class="tag-list">
        <c:forEach items="${poem.matchTags}" var="tag">
          <span class="tag">${tag}</span>
        </c:forEach>
      </div>
    </div>

    <!-- 内容来源声明 -->
    <div class="poem-source">
      内容来源：<a href="https://www.jinrishici.com" target="_blank" rel="noopener noreferrer">
      <img src="https://www.jinrishici.com/img/logo.png" alt="今日诗词" class="poem-source-logo">
    </a>
    </div>
  </div>
</div>

<!-- 页脚 -->
<jsp:include page="/WEB-INF/components/footer.jsp" />
</body>
</html>