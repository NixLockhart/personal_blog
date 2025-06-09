<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html>
<head>
    <!-- 防止主题闪烁 -->
    <script src="${pageContext.request.contextPath}/js/theme-init.js"></script>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>星光小栈</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/element/website_logo.png">
    <!-- 本地Remixicon资源 -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/fonts/remixicon.css">
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="css/poem.css">
</head>
<body>
<!-- 导航栏 -->
<jsp:include page="/WEB-INF/components/header.jsp" />

<!-- 主要内容 -->
<div class="container">
    <div class="main-content">
        <!-- 文章列表区域 -->
        <div class="article-section">
            <!-- 分类导航栏 -->
            <div class="category-nav">
                <div class="category-list">
                    <a href="${pageContext.request.contextPath}/posts" class="category-item ${empty param.category ? 'active' : ''}">
                        <i class="ri-layout-grid-line"></i> 全部
                    </a>
                    <c:forEach items="${categories}" var="category">
                        <a href="${pageContext.request.contextPath}/posts?category=${category.id}"
                           class="category-item ${param.category eq category.id ? 'active' : ''}">
                            <i class="ri-folder-line"></i> ${category.name}
                        </a>
                    </c:forEach>
                </div>
            </div>

            <!-- 文章列表 -->
            <div class="article-list">
                <c:choose>
                    <c:when test="${not empty posts}">
                        <c:forEach items="${posts}" var="post">
                            <div class="article-item" onclick="window.location.href='${pageContext.request.contextPath}/posts/${post.id}'">
                                <h2 class="article-title"><a href="${pageContext.request.contextPath}/posts/${post.id}">${post.title}</a></h2>
                                <div class="article-meta">
                                    <span><i class="ri-calendar-line"></i> <fmt:formatDate value="${post.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/></span>
                                    <span><i class="ri-folder-line"></i> ${post.categoryName}</span>
                                    <span><i class="ri-user-line"></i> ${post.authorName}</span>
                                </div>
                                <div class="article-summary">
                                    <c:choose>
                                        <c:when test="${fn:length(post.content) > 200}">
                                            ${fn:substring(post.content, 0, 200)}...
                                        </c:when>
                                        <c:otherwise>
                                            ${post.content}
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </c:forEach>

                        <!-- 查看更多按钮 -->
                        <c:if test="${total > 5}">
                            <div class="load-more">
                                <a href="${pageContext.request.contextPath}/categories" class="btn">
                                    <i class="ri-archive-line"></i> 点击查看更多
                                </a>
                            </div>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-state">
                            <i class="ri-file-list-3-line"></i>
                            <p>当前分类下没有内容</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <!-- 侧边栏 -->
        <div class="sidebar">
            <!-- 万年历 -->
            <div class="calendar-section">
                <h2 class="calendar-title">万年历</h2>
                <div class="calendar-card" id="calendar-card">
                    <div class="calendar-content">
                        <div class="calendar-date">
                            <div class="solar-date">
                                <span id="solar-year" class="year"></span>年
                                <span id="solar-month" class="month"></span>月
                                <span id="solar-day" class="day"></span>日
                                <span id="weekday" class="weekday"></span>
                            </div>
                            <div class="lunar-date">
                                <span id="lunar-year" class="lunar-year"></span>
                                <span id="lunar-month" class="lunar-month"></span>
                                <span id="lunar-day" class="lunar-day"></span>
                            </div>
                        </div>
                        <div class="calendar-info">
                            <div class="basic-info">
                                <span id="zodiac" class="zodiac"></span>
                                <span id="constellation" class="constellation"></span>
                                <span id="season" class="season"></span>
                            </div>
                            <div class="term-section">
                                <div class="term-names">
                                    <span id="current-term" class="term-name"></span>
                                    <span id="next-term" class="term-name"></span>
                                </div>
                                <div class="term-days-section">
                                    <span id="current-term-days" class="term-days"></span>
                                    <span id="next-term-days" class="term-days"></span>
                                </div>
                            </div>
                        </div>
                        <div class="fortune-info">
                            <div class="suitable" id="suitable">
                                <span class="label">宜：</span>
                                <span class="content"></span>
                            </div>
                            <div class="avoid" id="avoid">
                                <span class="label">忌：</span>
                                <span class="content"></span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- 每日诗词 -->
            <div class="poem-section">
                <h2 class="poem-title">今日诗词</h2>
                <div class="poem-card" id="poem-card">
                    <div class="poem-content">
                        <div id="poem_sentence" class="poem-sentence"></div>
                        <div id="poem_info" class="poem-info"></div>
                    </div>
                </div>
            </div>
            <!-- 联系方式 -->
            <div class="contact-section">
                <h2 class="contact-title">联系我</h2>
                <div class="social-links">
                    <!-- 自行替换为实际的信息 -->
                    <div class="social-link">
                        <i class="ri-qq-line"></i>
                        QQ: xxxxxxxxx
                    </div>
                    <div class="social-link">
                        <i class="ri-mail-line"></i>
                        邮箱: xxxxxx@xxx.com
                    </div>
                    <div class="social-link">
                        <i class="ri-wechat-line"></i>
                        公众号: xxxxxxxxx
                    </div>
                </div>
            </div>
            <!-- 关注我 -->
            <div class="follow-section">
                <h2 class="follow-title">关注我</h2>
                <div class="follow-links">
                    /*自行替换为实际的链接*/
                    <a href="https://blog.csdn.net/" class="follow-link">
                        <i class="ri-code-box-line"></i>
                        CSDN
                    </a>
                    <a href="https://github.com/" target="_blank" class="follow-link">
                        <i class="ri-github-fill"></i>
                        <span>GitHub</span>
                    </a>
                    <a href="https://gitee.com/" class="follow-link">
                        <i class="ri-git-branch-line"></i>
                        Gitee
                    </a>
                    <a href="https://music.163.com/" class="follow-link">
                        <i class="ri-music-line"></i>
                        网易云音乐
                    </a>
                    <a href="https://space.bilibili.com/" target="_blank" class="follow-link">
                        <i class="ri-bilibili-line"></i>
                        Bilibili
                    </a>
                    <a href="https://www.zhihu.com/" class="follow-link">
                        <i class="ri-zhihu-line"></i>
                        知乎
                    </a>
                </div>
            </div>
            <!-- 开源项目板块 -->
            <!-- 替换为实际的项目链接和信息或删除该板块 -->
            <div class="open-source">
                <h2 class="section-title">开源项目</h2>
                <a href="https://github.com/NixLockhart/SmartFlowerPort" target="_blank" class="project-card">
                    <div class="project-header">
                        <h3 class="project-title">STM32智能花盆</h3>
                        <span class="project-link">
                            <i class="ri-github-fill"></i>
                        </span>
                    </div>
                    <p class="project-desc">基于STM32F103ZET6的智能花盆项目，支持土壤湿度监测、自动浇水、环境温湿度监测等功能。</p>
                    <div class="project-tags">
                        <span class="tag">STM32</span>
                        <span class="tag">C语言</span>
                        <span class="tag">嵌入式</span>
                    </div>
                </a>
            </div>
        </div>
    </div>
</div>

<!-- 页脚 -->
<jsp:include page="/WEB-INF/components/footer.jsp" />

<!-- 万年历API -->
<script type="text/javascript">
    // 万年历API调用
    function loadCalendar() {
        /*替换为实际的API地址和密钥*/
        fetch('https://cn.apihz.cn/api/time/getday.php?id=xxxxxxxx&key=xxxxxxxxxxxxx')
            .then(response => {
                if (!response.ok) {
                    throw new Error('网络请求失败: ' + response.status);
                }
                return response.text(); // 先获取原始文本
            })
            .then(text => {
                let data;
                try {
                    data = JSON.parse(text); // 尝试解析JSON
                } catch (e) {
                    console.error('JSON解析失败:', e);
                    throw new Error('API返回的不是有效的JSON格式');
                }


                if (data && data.code === 200) {
                    // API直接返回数据，不是嵌套在data字段中
                    const calendarData = data;

                    // 验证必要字段是否存在（使用实际的字段名）
                    if (!calendarData.ynian || !calendarData.yyue || !calendarData.yri) {
                        throw new Error('API返回数据格式不正确，缺少必要的日期字段');
                    }

                    // 阳历日期
                    document.getElementById('solar-year').textContent = calendarData.ynian || '';
                    document.getElementById('solar-month').textContent = calendarData.yyue || '';
                    document.getElementById('solar-day').textContent = calendarData.yri || '';
                    document.getElementById('weekday').textContent = calendarData.xingqi || '';

                    // 农历日期
                    document.getElementById('lunar-year').textContent = calendarData.ganzhinian || '';
                    document.getElementById('lunar-month').textContent = calendarData.nyue || '';
                    document.getElementById('lunar-day').textContent = calendarData.nri || '';

                    // 生肖和星座
                    document.getElementById('zodiac').textContent = (calendarData.shengxiao ? calendarData.shengxiao + '年' : '');
                    document.getElementById('constellation').textContent = calendarData.xingzuo || '';

                    // 季节
                    document.getElementById('season').textContent = calendarData.jijie || '';

                    // 节气信息处理
                    const jieqiMsg = calendarData.jieqimsg || '';

                    // 解析节气信息，格式类似："芒种 第3天 （距下一个节气"夏至"，还有14天）"
                    if (jieqiMsg) {
                        // 提取当前节气和天数
                        const currentTermMatch = jieqiMsg.match(/^([^\s]+)\s*第(\d+)天/);
                        if (currentTermMatch) {
                            document.getElementById('current-term').textContent = currentTermMatch[1];
                            document.getElementById('current-term-days').textContent = '第' + currentTermMatch[2] + '天';
                        } else {
                            document.getElementById('current-term').textContent = '无';
                            document.getElementById('current-term-days').textContent = '';
                        }

                        // 提取下一个节气和剩余天数
                        const nextTermMatch = jieqiMsg.match(/距下一个节气“([^"]+)”，还有(\d+)天/);
                        if (nextTermMatch) {
                            document.getElementById('next-term').textContent = nextTermMatch[1];
                            document.getElementById('next-term-days').textContent = '还有' + nextTermMatch[2] + '天';
                        } else {
                            document.getElementById('next-term').textContent = '无';
                            document.getElementById('next-term-days').textContent = '';
                        }
                    } else {
                        // 如果没有节气信息，显示默认值
                        document.getElementById('current-term').textContent = '无';
                        document.getElementById('current-term-days').textContent = '';
                        document.getElementById('next-term').textContent = '无';
                        document.getElementById('next-term-days').textContent = '';
                    }

                    // 宜忌
                    const suitableElement = document.querySelector('#suitable .content');
                    const avoidElement = document.querySelector('#avoid .content');

                    if (suitableElement) {
                        if (calendarData.yi) {
                            // 将用|分隔的字符串转换为数组，然后用、连接
                            const suitableArray = calendarData.yi.split('|');
                            suitableElement.textContent = suitableArray.join('、');
                        } else {
                            suitableElement.textContent = '无特别事宜';
                        }
                    }

                    if (avoidElement) {
                        if (calendarData.ji) {
                            // 将用|分隔的字符串转换为数组，然后用、连接
                            const avoidArray = calendarData.ji.split('|');
                            avoidElement.textContent = avoidArray.join('、');
                        } else {
                            avoidElement.textContent = '无特别禁忌';
                        }
                    }
                } else {
                    const errorMsg = data && data.msg ? data.msg : '未知错误';
                    console.error('万年历API调用失败:', errorMsg);
                    throw new Error('API返回错误: ' + errorMsg);
                }
            })
            .catch(error => {
                console.error('万年历API请求错误:', error);
                document.querySelector('.calendar-content').innerHTML = '<div class="error-message">万年历加载失败，请检查网络连接</div>';
            });
    }

    // 页面加载完成后调用万年历API
    document.addEventListener('DOMContentLoaded', function() {
        loadCalendar();
    });
</script>

<!-- 今日诗词API -->
<script src="https://sdk.jinrishici.com/v2/browser/jinrishici.js" charset="utf-8"></script>
<script type="text/javascript">
    jinrishici.load(function(result) {
        var sentence = document.querySelector("#poem_sentence");
        var info = document.querySelector("#poem_info");
        var card = document.querySelector("#poem-card");

        // 设置诗句
        sentence.innerHTML = result.data.content;
        // 设置出处
        info.innerHTML = '【' + result.data.origin.dynasty + '】' + result.data.origin.author + '《' + result.data.origin.title + '》';

        // 添加点击事件，跳转到诗词详情页
        card.addEventListener('click', function() {
            // 创建隐藏表单
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = '${pageContext.request.contextPath}/poem';

            // 创建隐藏输入框
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'poem';
            input.value = JSON.stringify(result.data);

            // 添加输入框到表单
            form.appendChild(input);

            // 添加表单到页面并提交
            document.body.appendChild(form);
            form.submit();
        });
    });
</script>
</body>
</html>
