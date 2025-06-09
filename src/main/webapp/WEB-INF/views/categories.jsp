<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/element/website_logo.png">
    <title>文章归档 - 个人博客</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <!-- 本地Remixicon资源 -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/fonts/remixicon.css">
</head>

<body>
<jsp:include page="../components/header.jsp"/>

<div class="timeline-container">
    <div class="timeline">
        <c:set var="currentYear" value=""/>
        <c:forEach items="${articles}" var="article">
        <c:set var="articleYear">
            <fmt:formatDate value="${article.createdAt}" pattern="yyyy"/>
        </c:set>

        <c:if test="${currentYear ne articleYear}">
        <c:if test="${not empty currentYear}">
    </div> <!-- 关闭上一个年份组 -->
    </c:if>
    <div class="year-group">
        <h2 class="year-title">${articleYear}</h2>
        <c:set var="currentYear" value="${articleYear}"/>
        </c:if>

        <div class="timeline-item">
            <div class="timeline-dot"></div>
            <div class="timeline-date">
                <fmt:formatDate value="${article.createdAt}" pattern="MM-dd"/>
            </div>
            <div class="timeline-content">
                <h3>
                    <a href="${pageContext.request.contextPath}/posts/${article.id}">
                            ${article.title}
                    </a>
                </h3>
                <p>
                    <c:choose>
                        <c:when test="${not empty article.content}">
                            <c:choose>
                                <c:when test="${fn:length(article.content) > 100}">
                                    ${fn:substring(article.content, 0, 100)}...
                                </c:when>
                                <c:otherwise>
                                    ${article.content}
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                    </c:choose>
                </p>
            </div>
        </div>
        </c:forEach>
    </div> <!-- 关闭最后一个年份组 -->
</div>
</div>

<jsp:include page="../components/footer.jsp"/>
</body>
</html>