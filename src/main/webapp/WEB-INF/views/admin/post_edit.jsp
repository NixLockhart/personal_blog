<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- 添加Vditor支持 -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/vditor/dist/index.css">
<script src="${pageContext.request.contextPath}/static/vditor/dist/index.min.js"></script>

<!-- 博文编辑 -->
<div class="admin-card">
    <div class="admin-card-header">
        <h2><i class="ri-file-edit-line"></i> ${post != null ? '编辑' : '新建'}博文</h2>
        <div>
            <button id="submitPost" class="admin-action-btn">
                <i class="ri-save-line"></i> 提交博文
            </button>
        </div>
    </div>

    <div class="post-edit-container">
        <!-- 文章标题和文件名 -->
        <div class="post-edit-header">
            <div class="post-edit-title">
                <label for="postTitle">文章标题</label>
                <input type="text" id="postTitle" class="post-title-input" placeholder="请输入文章标题" value="${post != null ? post.title : ''}">
            </div>
            <div class="post-edit-filename">
                <label for="postFilename">文件名</label>
                <input type="text" id="postFilename" class="post-filename-input" placeholder="请输入文件名（例如：article.md）" value="${post != null ? post.contentUrl : ''}">
            </div>
        </div>

        <!-- Vditor编辑器 -->
        <div id="vditor" class="vditor-edit-area"></div>

        <!-- 调试信息 -->
        <div style="display: none;">
            <p>文章ID: ${post != null ? post.id : '新文章'}</p>
            <p>文章标题: ${post != null ? post.title : ''}</p>
            <p>文章内容长度: ${post != null && post.content != null ? post.content.length() : 0}</p>
            <p>文章内容: ${post != null ? post.content : ''}</p>
        </div>
    </div>
</div>

<!-- 提交确认模态框 -->
<div id="submitModal" class="modal">
    <div class="modal-content">
        <div class="modal-header">
            <h2>文章设置</h2>
            <span class="close">&times;</span>
        </div>
        <div class="modal-body">
            <form id="postForm" action="${pageContext.request.contextPath}/admin/posts/save" method="post">
                <input type="hidden" id="postId" name="postId" value="${post != null ? post.id : ''}">

                <div class="form-group">
                    <label for="modalTitle">文章标题</label>
                    <input type="text" id="modalTitle" name="title" class="form-control" required>
                </div>

                <div class="form-group">
                    <label for="modalFilename">文件名</label>
                    <input type="text" id="modalFilename" name="contentUrl" class="form-control" required>
                </div>

                <div class="form-group">
                    <label for="summary">文章简介</label>
                    <textarea id="summary" name="summary" class="form-control" rows="3" placeholder="请输入文章简介（选填）">${post != null ? post.summary : ''}</textarea>
                </div>

                <div class="form-group">
                    <label for="categoryId">文章分类</label>
                    <select id="categoryId" name="categoryId" class="form-control" required>
                        <c:forEach items="${categories}" var="category">
                            <option value="${category.id}" ${post != null && post.categoryId == category.id ? 'selected' : ''}>${category.name}</option>
                        </c:forEach>
                    </select>
                </div>

                <input type="hidden" id="content" name="content">

                <div class="form-buttons">
                    <button type="button" class="btn-cancel" id="cancelSubmit">取消</button>
                    <button type="submit" class="btn-submit">确认提交</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- 博文编辑脚本 -->
<script>
    // 等待页面完全加载
    window.onload = function() {
        console.log('页面完全加载完成，开始初始化编辑器');

        // 初始化变量
        const postTitleInput = document.getElementById('postTitle');
        const postFilenameInput = document.getElementById('postFilename');
        const submitBtn = document.getElementById('submitPost');
        const modal = document.getElementById('submitModal');
        const closeBtn = document.querySelector('.close');
        const cancelBtn = document.getElementById('cancelSubmit');
        const postForm = document.getElementById('postForm');
        const modalTitleInput = document.getElementById('modalTitle');
        const modalFilenameInput = document.getElementById('modalFilename');
        const contentInput = document.getElementById('content');

        // 获取文章内容
        const initialContent = `${post != null ? post.content : ''}`;
        console.log('获取到的初始内容长度:', initialContent ? initialContent.length : 0);

        // 确保编辑器容器存在
        const editorContainer = document.getElementById('vditor');
        if (!editorContainer) {
            console.error('找不到编辑器容器');
            return;
        }

        // 初始化Vditor编辑器
        const vditor = new Vditor('vditor', {
            height: 500,
            mode: 'sv', // 直接使用分屏模式(Split View)
            outline: { // 启用大纲支持
                enable: true,
                position: 'right'
            },
            preview: {
                hljs: {
                    enable: true,
                    style: 'github',
                    lineNumber: true
                },
                math: {
                    inlineDigit: true
                },
                delay: 500, // 增加延迟以确保预览渲染完成
                maxWidth: 800,
                mode: 'both', // 设置预览模式为同时显示
                actions: [], // 禁用预览操作按钮
                theme: {
                    current: document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light'
                },
                adapt: {
                    enable: true // 启用自适应宽度
                }
            },
            typewriterMode: false, // 关闭打字机模式，避免可能的冲突
            toolbar: [ // 自定义工具栏
                'headings', 'bold', 'italic', 'strike', 'link', '|',
                'list', 'ordered-list', 'check', 'outdent', 'indent', '|',
                'quote', 'line', 'code', 'inline-code', 'table', '|',
                'upload', 'record', '|',
                'undo', 'redo', '|',
                'edit-mode', 'both', 'preview', 'fullscreen'
            ],
            toolbarConfig: {
                pin: true
            },
            debugger: true, // 开启调试模式，帮助诊断问题
            cache: {
                enable: false // 暂时禁用缓存，避免缓存引起的问题
            },
            counter: {
                enable: true // 显示计数器
            },
            upload: { // 支持上传图片
                accept: 'image/*',
                handler: (files) => {
                    // 仅作为示例，可以在这里实现图片上传功能
                    console.log('上传文件:', files);
                    // 返回一个Promise，表示上传成功
                    return Promise.resolve('https://example.com/image.png');
                }
            },
            icon: 'material', // 使用Material图标，加载更快
            lang: 'zh_CN', // 使用中文
            theme: document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'classic', // 跟随系统主题
            placeholder: '请输入Markdown格式的文章内容...',
            width: '100%', // 设置编辑器宽度为100%
            resize: {
                enable: false, // 禁用手动调整大小
                position: 'bottom'
            },
            after: () => {
                console.log('Vditor编辑器初始化完成');

                // 设置初始内容
                if (!initialContent || !initialContent.trim()) {
                    console.log('使用默认内容');
                    const defaultContent = `# 开始撰写你的博客

这是一个简单的Markdown编辑器，你可以:

- 使用**粗体**或*斜体*
- 添加[链接](https://example.com)
- 插入图片
- 创建表格

## 二级标题示例

正文内容示例，支持各种Markdown语法。`;
                    vditor.setValue(defaultContent);
                } else {
                    console.log('使用现有内容，长度:', initialContent.length);
                    vditor.setValue(initialContent);
                }

                // 确保编辑器处于分屏预览模式
                setTimeout(() => {
                    try {
                        vditor.setPreviewMode('both');

                        // 手动触发预览更新
                        const previewElement = document.querySelector('.vditor-preview');
                        if (previewElement) {
                            previewElement.style.display = 'block';
                            previewElement.style.width = 'auto';
                            previewElement.style.maxWidth = '50%';
                            previewElement.style.overflow = 'auto';

                            // 设置编辑器容器样式，确保不会溢出父容器
                            const vditorContainer = document.querySelector('.vditor');
                            if (vditorContainer) {
                                vditorContainer.style.maxWidth = '100%';
                                vditorContainer.style.overflow = 'hidden';
                            }

                            // 强制更新预览
                            vditor.getValue();
                        }

                        console.log('已切换到分屏预览模式');
                    } catch (e) {
                        console.error('切换预览模式失败:', e);
                    }
                }, 800);
            }
        });

        // 提交按钮点击事件
        submitBtn.addEventListener('click', function() {
            // 填充模态框中的值
            modalTitleInput.value = postTitleInput.value || '未命名文章';
            modalFilenameInput.value = postFilenameInput.value || 'article.md';

            // 获取编辑器内容并确保它不为空
            const editorContent = vditor.getValue();
            if (!editorContent || editorContent.trim().length === 0) {
                alert('文章内容不能为空');
                return;
            }
            contentInput.value = editorContent;

            console.log('模态框打开前数据检查:');
            console.log('标题:', modalTitleInput.value);
            console.log('文件名:', modalFilenameInput.value);
            console.log('内容长度:', contentInput.value.length);

            // 显示模态框
            modal.style.display = 'flex';
        });

        // 关闭模态框
        closeBtn.addEventListener('click', function() {
            modal.style.display = 'none';
        });

        cancelBtn.addEventListener('click', function() {
            modal.style.display = 'none';
        });

        // 点击模态框外部区域关闭
        window.addEventListener('click', function(event) {
            if (event.target === modal) {
                modal.style.display = 'none';
            }
        });

        // 表单提交事件
        postForm.addEventListener('submit', function(e) {
            // 先阻止默认提交
            e.preventDefault();

            // 获取表单数据前确保表单字段已正确填充
            console.log('提交前最终检查表单数据:');
            console.log('标题:', modalTitleInput.value);
            console.log('文件名:', modalFilenameInput.value);
            console.log('内容长度:', contentInput.value.length);
            console.log('分类ID:', document.getElementById('categoryId').value);

            // 再次确保表单字段已填充 - 关键字段二次验证
            if (!modalTitleInput.value || modalTitleInput.value.trim() === '') {
                alert('文章标题不能为空');
                return false;
            }

            if (!modalFilenameInput.value || modalFilenameInput.value.trim() === '') {
                alert('文件名不能为空');
                return false;
            }

            if (!contentInput.value || contentInput.value.trim() === '') {
                alert('文章内容不能为空');
                return false;
            }

            // 检查分类是否已选择
            const categorySelect = document.getElementById('categoryId');
            if (!categorySelect.value) {
                alert('请选择文章分类');
                categorySelect.focus(); // 聚焦到分类选择框
                return false;
            }

            // 所有验证通过，提交表单
            this.submit();
        });

        // 监听主题变化更新编辑器主题
        const observer = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                if (mutation.attributeName === 'data-theme') {
                    const theme = document.documentElement.getAttribute('data-theme');
                    vditor.setTheme(theme === 'dark' ? 'dark' : 'classic');
                }
            });
        });

        observer.observe(document.documentElement, {
            attributes: true,
            attributeFilter: ['data-theme']
        });
    }
</script>

<!-- 添加相关CSS样式 -->
<style>
    .post-edit-container {
        display: flex;
        flex-direction: column;
        gap: 20px;
        width: 100%;
        overflow-x: hidden;
    }

    .post-edit-header {
        display: flex;
        gap: 20px;
        margin-bottom: 10px;
        width: 100%;
    }

    .post-edit-title, .post-edit-filename {
        display: flex;
        flex-direction: column;
        gap: 8px;
    }

    .post-edit-title {
        flex: 2;
    }

    .post-edit-filename {
        flex: 1;
    }

    .post-title-input, .post-filename-input {
        padding: 8px 12px;
        border-radius: 4px;
        border: 1px solid #e1e1e1;
        background-color: var(--input-bg-color, #ffffff);
        color: var(--text-color, #333333);
        font-size: 1rem;
    }

    .vditor-edit-area {
        min-height: 500px;
        border: 1px solid #e1e1e1;
        border-radius: 4px;
        width: 100%;
        max-width: 100%;
        overflow-x: hidden;
    }

    /* 设置编辑器内部的预览区域最大宽度，防止溢出 */
    .vditor-reset {
        max-width: 100%;
        overflow-x: auto;
        word-break: break-word;
    }

    /* 确保编辑器和预览区域不会引起页面水平滚动 */
    .vditor-sv, .vditor-sv__panel, .vditor-preview {
        max-width: 100%;
        overflow-x: auto;
    }

    /* 模态框样式 */
    .modal {
        display: none;
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.5);
        z-index: 9999;
        justify-content: center;
        align-items: center;
        overflow-y: auto;
        padding: 20px;
    }

    .modal-content {
        background-color: var(--bg-color, #ffffff);
        border-radius: 8px;
        width: 90%;
        max-width: 600px;
        max-height: 90vh;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
        animation: modalFadeIn 0.3s ease;
        display: flex;
        flex-direction: column;
    }

    @keyframes modalFadeIn {
        from { opacity: 0; transform: translateY(-20px); }
        to { opacity: 1; transform: translateY(0); }
    }

    .modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px 20px;
        border-bottom: 1px solid #e1e1e1;
        flex-shrink: 0;
    }

    .modal-header h2 {
        margin: 0;
        font-size: 1.4rem;
        color: var(--text-color, #333333);
    }

    .close {
        font-size: 24px;
        cursor: pointer;
        color: var(--text-color, #333333);
    }

    .modal-body {
        padding: 20px;
        overflow-y: auto;
        flex-grow: 1;
    }

    .form-group {
        margin-bottom: 20px;
    }

    .form-group label {
        display: block;
        margin-bottom: 8px;
        font-weight: 500;
        color: var(--text-color, #333333);
    }

    .form-control {
        width: 100%;
        padding: 10px 12px;
        border-radius: 4px;
        border: 1px solid #e1e1e1;
        background-color: var(--input-bg-color, #ffffff);
        color: var(--text-color, #333333);
        font-size: 1rem;
        box-sizing: border-box;
    }

    textarea.form-control {
        resize: vertical;
        min-height: 80px;
        max-height: 200px;
    }

    .form-buttons {
        display: flex;
        justify-content: flex-end;
        gap: 12px;
        margin-top: 20px;
        padding-top: 20px;
        border-top: 1px solid #e1e1e1;
        flex-shrink: 0;
    }

    .btn-cancel, .btn-submit {
        padding: 8px 16px;
        border-radius: 4px;
        border: none;
        cursor: pointer;
        font-weight: 500;
        transition: all 0.2s ease;
    }

    .btn-cancel {
        background-color: var(--secondary-bg-color, #f5f5f5);
        color: var(--text-color, #333333);
    }

    .btn-submit {
        background-color: #0078D4;
        color: white;
    }

    .btn-cancel:hover, .btn-submit:hover {
        transform: translateY(-1px);
    }

    /* 响应式设计 */
    @media (max-width: 768px) {
        .post-edit-header {
            flex-direction: column;
        }

        .vditor {
            height: auto !important;
        }
    }

    /* 深色模式适配 */
    [data-theme="dark"] .post-title-input,
    [data-theme="dark"] .post-filename-input,
    [data-theme="dark"] .form-control {
        border-color: #3a3b3c;
        background-color: #242526;
        color: #e4e6eb;
    }

    [data-theme="dark"] .modal-header {
        border-color: #3a3b3c;
    }

    [data-theme="dark"] .modal-content {
        background-color: #242526;
    }

    [data-theme="dark"] .close,
    [data-theme="dark"] .modal-header h2,
    [data-theme="dark"] .form-group label {
        color: #e4e6eb;
    }

    [data-theme="dark"] .btn-cancel {
        background-color: #3a3b3c;
        color: #e4e6eb;
    }
</style> 