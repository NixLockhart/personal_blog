<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>关于我 - 个人博客</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/element/website_logo.png">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <!-- 本地Remixicon资源 -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/fonts/remixicon.css">
    <!-- 添加Vditor支持 -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/vditor/dist/index.css">
    <script src="${pageContext.request.contextPath}/static/vditor/dist/index.min.js"></script>
    <style>
        .loading-container {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 40px 0;
            text-align: center;
            min-height: 200px;
        }

        .loading-spinner {
            width: 50px;
            height: 50px;
            border: 4px solid rgba(0, 0, 0, 0.1);
            border-radius: 50%;
            border-left-color: var(--theme-color);
            animation: spin 1s linear infinite;
            margin-bottom: 15px;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        .loading-container p {
            font-size: 16px;
            color: var(--text-secondary);
            margin: 0;
        }

        [data-theme="dark"] .loading-spinner {
            border: 4px solid rgba(255, 255, 255, 0.1);
            border-left-color: var(--theme-color);
        }

        @media (max-width: 768px) {
            .loading-container {
                padding: 30px 0;
            }

            .loading-spinner {
                width: 40px;
                height: 40px;
            }
        }

        .vditor-preview {
            opacity: 1 !important;
        }
    </style>
</head>
<body>
<jsp:include page="../components/header.jsp"/>
<div class="container">
    <div class="content">
        <div class="markdown-body" id="content">
            <div id="loading-container" class="loading-container">
                <div class="loading-spinner"></div>
                <p>内容正在加载中...</p>
            </div>
            <div id="markdown-content" style="display: none;">${content}</div>
            <input type="hidden" id="originalContent" value="${content}">
        </div>
    </div>
    <script type="text/javascript">
        document.addEventListener('DOMContentLoaded', function() {
            setupMarkdownLoading();
        });

        function setupMarkdownLoading() {
            if (typeof Vditor !== 'undefined') {
                renderMarkdownContent();
            } else {
                console.log('Vditor尚未加载完成，等待加载...');
            }
        }

        function renderMarkdownContent() {
            var contentElement = document.getElementById('markdown-content');
            var loadingContainer = document.getElementById('loading-container');
            var originalContentElement = document.getElementById('originalContent');

            if (contentElement) {
                var markdownContent = contentElement.textContent;

                if (originalContentElement && (!markdownContent || markdownContent.trim() === '')) {
                    markdownContent = originalContentElement.value;
                }

                window.originalMarkdownContent = markdownContent;

                if (!markdownContent.trim()) {
                    if (loadingContainer) {
                        loadingContainer.innerHTML = '<p>内容为空</p>';
                    }
                    return;
                }

                var previewElement = document.createElement('div');
                previewElement.className = 'vditor-preview';
                previewElement.style.opacity = '1';

                var currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
                var renderMode = currentTheme === 'dark' ? 'dark' : 'light';

                Vditor.preview(previewElement, markdownContent, {
                    mode: renderMode,
                    anchor: 0,
                    hljs: {
                        enable: true,
                        style: currentTheme === 'dark' ? 'monokai' : 'github',
                        lineNumber: true
                    },
                    math: {
                        inlineDigit: false
                    },
                    speech: {
                        enable: false
                    },
                    after: function() {
                        console.log('Markdown渲染完成');
                        if (loadingContainer) {
                            loadingContainer.style.display = 'none';
                        }

                        contentElement.parentNode.replaceChild(previewElement, contentElement);

                        setTimeout(function() {
                            previewElement.style.opacity = '1';
                        }, 0);
                    }
                });
            }
        }

        window.onload = function() {
            renderMarkdownContent();

            const observer = new MutationObserver(function(mutations) {
                mutations.forEach(function(mutation) {
                    if (mutation.attributeName === 'data-theme') {
                        const newTheme = document.documentElement.getAttribute('data-theme') || 'light';
                        const newRenderMode = newTheme === 'dark' ? 'dark' : 'light';
                        const newCodeStyle = newTheme === 'dark' ? 'monokai' : 'github';

                        const previewElement = document.querySelector('.vditor-preview');
                        if (previewElement) {
                            let markdownContent = window.originalMarkdownContent || '';

                            if (!markdownContent) {
                                const originalContent = document.getElementById('markdown-content');
                                if (originalContent) {
                                    markdownContent = originalContent.textContent;
                                    window.originalMarkdownContent = markdownContent;
                                } else {
                                    const backupContent = document.getElementById('originalContent');
                                    if (backupContent) {
                                        markdownContent = backupContent.value;
                                        window.originalMarkdownContent = markdownContent;
                                    } else {
                                        console.log('无法找到原始Markdown内容');
                                        return;
                                    }
                                }
                            }

                            Vditor.preview(previewElement, markdownContent, {
                                mode: newRenderMode,
                                anchor: 0,
                                hljs: {
                                    enable: true,
                                    style: newCodeStyle,
                                    lineNumber: true
                                },
                                math: {
                                    inlineDigit: false
                                },
                                speech: {
                                    enable: false
                                },
                                after: function() {
                                    console.log('主题变化，Markdown重新渲染完成');
                                }
                            });
                        }
                    }
                });
            });

            observer.observe(document.documentElement, { attributes: true });
        };
    </script>
</div>
<jsp:include page="../components/footer.jsp"/>
</body>
</html>