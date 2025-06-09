<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.blog.model.Post" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.blog.util.AvatarUtil" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
  <!-- 防止主题闪烁 -->
  <script src="${pageContext.request.contextPath}/js/theme-init.js"></script>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${post.title} - 星光小栈</title>
  <!-- 添加网站logo -->
  <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/element/website_logo.png">
  <!-- 使用本地Remixicon资源替代CDN -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/fonts/remixicon.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <!-- 添加Vditor支持 -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/vditor/dist/index.css">
  <script src="${pageContext.request.contextPath}/static/vditor/dist/index.min.js"></script>
  <!-- 添加加载动画样式 -->
  <style>
    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 40px 0;
      text-align: center;
      min-height: 200px; /* 确保加载容器有最小高度 */
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

    /* 修改淡入效果，确保内容始终可见 */
    .vditor-preview {
      opacity: 1 !important; /* 确保内容始终可见 */
    }

    /* 可以选择保留淡入但改为更可靠的实现
    .fade-in {
      animation: fadeIn 0.5s ease-in;
    }

    @keyframes fadeIn {
      0% { opacity: 0.8; }
      100% { opacity: 1; }
    }
    */

    /* 移动端适配 */
    @media (max-width: 768px) {
      .loading-container {
        padding: 30px 0;
      }

      .loading-spinner {
        width: 40px;
        height: 40px;
      }
    }
  </style>
</head>
<body>
<jsp:include page="/WEB-INF/components/header.jsp" />

<div class="container">
  <% Post post = (Post) request.getAttribute("post"); %>
  <% if (post != null) { %>
  <!-- 添加隐藏的文章ID -->
  <input type="hidden" id="postId" value="<%= post.getId() %>">
  <!-- 添加隐藏的原始内容备份 -->
  <input type="hidden" id="originalContent" value="<%= post.getContent().replaceAll("\"", "&quot;") %>">

  <article class="post-detail">
    <h1 class="post-title"><%= post.getTitle() %></h1>
    <div class="post-meta">
      <span class="post-date">
        <%= new SimpleDateFormat("yyyy-MM-dd HH:mm").format(post.getCreatedAt()) %>
      </span>
      <span class="post-author">
        作者: <%= post.getAuthorName() %>
      </span>
    </div>
    <% String postSummary = (String) request.getAttribute("postSummary"); %>
    <% if(postSummary != null && !postSummary.isEmpty()) { %>
    <div class="post-summary">
      <h3>文章简介</h3>
      <p><%= postSummary %></p>
    </div>
    <% } %>
    <div class="post-content" id="content">
      <%
        String markdownContent = "";
        if(post.getContent() != null && !post.getContent().isEmpty()) { %>
      <!-- 添加加载动画容器 -->
      <div id="loading-container" class="loading-container">
        <div class="loading-spinner"></div>
        <p>文章正在加载中...</p>
      </div>
      <!-- 隐藏原始Markdown内容 -->
      <div id="post-content" style="display: none;"><%= post.getContent() %></div>
      <% } else { %>
      <p>暂无内容</p>
      <% if(request.getAttribute("markdownError") != null) { %>
      <div class="error-message">
        <p><strong>错误信息:</strong> <%= request.getAttribute("markdownError") %></p>
      </div>
      <% } %>
      <% } %>
    </div>
    <div class="post-footer">
      <div class="post-stats">
        <span class="post-tag views-count"><i class="ri-eye-line"></i> <%= post.getViews() %></span>
        <button class="post-tag likes-count" id="likeBtn"><i class="ri-thumb-up-line"></i> <%= post.getLikes() %></button>
      </div>
    </div>

    <!-- 添加评论区 -->
    <div class="comment-section">
      <h3>评论区 <button class="close-comment-btn" id="closeCommentBtn"><i class="ri-close-line"></i></button></h3>
      <div class="comment-list" id="commentList">
        <!-- 评论将通过JavaScript动态加载 -->
      </div>
      <div class="comment-form">
        <textarea placeholder="写下您的评论..." id="commentText"></textarea>
        <button id="submitComment">发表评论</button>
        <div style="clear: both;"></div>
      </div>
    </div>
  </article>

  <% } else { %>
  <div class="error-message">
    <h2>文章不存在</h2>
    <p>抱歉，您请求的文章未找到。</p>
    <a href="${pageContext.request.contextPath}/" class="btn">返回首页</a>
  </div>
  <% } %>

  <!-- 目录切换按钮 -->
  <div class="toc-toggle" id="tocToggle">
    <i class="ri-list-unordered"></i> 目录
  </div>

  <!-- 目录结构 -->
  <div class="toc" id="toc">
    <h3>目录</h3>
    <ul class="toc-list" id="tocList">
      <!-- 将由JavaScript动态生成 -->
    </ul>
  </div>

  <!-- 移动端底部导航栏 -->
  <div class="mobile-footer">
    <button id="mobileLikeBtn">
      <i class="ri-thumb-up-line"></i>
      <span>点赞</span>
    </button>
    <button id="mobileTocBtn">
      <i class="ri-list-unordered"></i>
      <span>目录</span>
    </button>
    <button id="mobileCommentBtn">
      <i class="ri-chat-1-line"></i>
      <span>评论</span>
    </button>
  </div>
</div>

<jsp:include page="/WEB-INF/components/footer.jsp" />

<script type="text/javascript">
  // 在DOM加载完成后立即设置加载状态
  document.addEventListener('DOMContentLoaded', function() {
    // 设置加载状态
    setupMarkdownLoading();

    // 页面加载时自动增加浏览量
    const postIdElement = document.getElementById('postId');
    if (postIdElement) {
      const postId = postIdElement.value;
      // 立即增加浏览量
      incrementViewCount(postId);
      // 获取最新的点赞和浏览数据
      fetchEngagementData(postId);
    }

    // 绑定按钮事件
    setupInteractionButtons();

    // 在页面加载完成后检查头像格式
    const avatarImages = document.querySelectorAll('.comment-avatar[data-user-id]');
    avatarImages.forEach(function(img) {
      const userId = img.getAttribute('data-user-id');
      const avatarUrl = img.getAttribute('data-avatar-url');

      if (avatarUrl) {
        // 如果评论有avatar_url属性，优先使用它
        img.src = '${pageContext.request.contextPath}' + avatarUrl + '?t=${sessionScope.lastAvatarUpdate}';
      } else if (userId) {
        // 否则尝试使用用户ID来加载头像
        const jpgUrl = '${pageContext.request.contextPath}/avatar/' + userId + '.jpg?t=${sessionScope.lastAvatarUpdate}';
        const pngUrl = '${pageContext.request.contextPath}/avatar/' + userId + '.png?t=${sessionScope.lastAvatarUpdate}';

        const checkImage = new Image();
        checkImage.onload = function() {
          img.src = jpgUrl;
        };
        checkImage.onerror = function() {
          img.src = pngUrl;
        };
        checkImage.src = jpgUrl;
      }
    });
  });

  // 添加设置Markdown加载的函数
  function setupMarkdownLoading() {
    // 预加载Vditor资源，确保渲染能尽快开始
    if (typeof Vditor !== 'undefined') {
      renderMarkdownContent();
    } else {
      // 如果Vditor还没加载完成，等待window.onload事件
      console.log('Vditor尚未加载完成，等待加载...');
    }
  }

  // 渲染Markdown内容
  function renderMarkdownContent() {
    var contentElement = document.getElementById('post-content');
    var loadingContainer = document.getElementById('loading-container');
    var originalContentElement = document.getElementById('originalContent');

    if (contentElement) {
      // 获取Markdown内容
      var markdownContent = contentElement.textContent;

      // 也可以从隐藏元素中获取原始内容的备份
      if (originalContentElement && (!markdownContent || markdownContent.trim() === '')) {
        markdownContent = originalContentElement.value;
      }

      // 保存原始Markdown内容到全局变量，以便主题切换时使用
      window.originalMarkdownContent = markdownContent;

      if (!markdownContent.trim()) {
        if (loadingContainer) {
          loadingContainer.innerHTML = '<p>文章内容为空</p>';
        }
        return;
      }

      // 创建一个预览容器
      var previewElement = document.createElement('div');
      previewElement.className = 'vditor-preview';
      // 设置预览容器的样式确保其可见
      previewElement.style.opacity = '1';

      // 检测当前主题
      var currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
      var renderMode = currentTheme === 'dark' ? 'dark' : 'light';

      // 使用Vditor.preview方法渲染Markdown
      Vditor.preview(previewElement, markdownContent, {
        mode: renderMode, // 根据当前主题选择渲染模式
        anchor: 0,
        hljs: {
          enable: true,
          style: currentTheme === 'dark' ? 'monokai' : 'github', // 根据主题选择代码高亮样式
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
          // 显示渲染后的内容，隐藏加载动画
          if (loadingContainer) {
            loadingContainer.style.display = 'none';
          }

          // 替换原始内容容器
          contentElement.parentNode.replaceChild(previewElement, contentElement);

          // 确保预览容器可见
          setTimeout(function() {
            previewElement.style.opacity = '1';
          }, 0);

          // Markdown渲染完成后生成目录
          generateTOC();
          // 为所有图片添加注释支持
          addImageCaptions();
        }
      });
    }
  }

  window.onload = function() {
    // 确保Markdown内容被渲染
    renderMarkdownContent();

    // 监听主题变化
    const observer = new MutationObserver(function(mutations) {
      mutations.forEach(function(mutation) {
        if (mutation.attributeName === 'data-theme') {
          const newTheme = document.documentElement.getAttribute('data-theme') || 'light';
          const newRenderMode = newTheme === 'dark' ? 'dark' : 'light';
          const newCodeStyle = newTheme === 'dark' ? 'monokai' : 'github';

          // 获取当前渲染的预览元素
          const previewElement = document.querySelector('.vditor-preview');
          if (previewElement) {
            // 使用之前保存的原始Markdown内容
            let markdownContent = window.originalMarkdownContent || '';

            if (!markdownContent) {
              // 尝试从隐藏元素获取内容
              const originalContent = document.getElementById('post-content');
              if (originalContent) {
                markdownContent = originalContent.textContent;
                window.originalMarkdownContent = markdownContent;
              } else {
                // 尝试从备份隐藏字段获取
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

            // 重新渲染Markdown内容
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
                // 重新生成目录
                generateTOC();
                // 重新添加图片注释
                addImageCaptions();
              }
            });
          }
        }
      });
    });

    // 开始观察
    observer.observe(document.documentElement, { attributes: true });
  };

  // 生成目录
  function generateTOC() {
    const tocList = document.getElementById('tocList');
    const contentArea = document.querySelector('.vditor-preview');

    if (!contentArea || !tocList) return;

    // 获取所有标题元素
    const headings = contentArea.querySelectorAll('h1, h2, h3, h4, h5, h6');

    if (headings.length === 0) {
      document.getElementById('toc').style.display = 'none';
      document.getElementById('tocToggle').style.display = 'none';
      return;
    }

    // 清空现有目录
    tocList.innerHTML = '';

    // 为每个标题添加id（如果没有）并创建目录项
    headings.forEach((heading, index) => {
      // 为没有id的标题添加id
      if (!heading.id) {
        heading.id = 'heading-' + index;
      }

      // 创建目录项
      const listItem = document.createElement('li');
      const link = document.createElement('a');
      link.href = '#' + heading.id;
      link.textContent = heading.textContent;
      link.dataset.target = heading.id;

      // 根据标题级别添加缩进
      const level = parseInt(heading.tagName.charAt(1));
      listItem.style.paddingLeft = (level - 1) * 10 + 'px';

      listItem.appendChild(link);
      tocList.appendChild(listItem);
    });

    // 设置目录跟随滚动
    setupScrollSpy();

    // 移动设备上的目录切换
    document.getElementById('tocToggle').addEventListener('click', function() {
      const toc = document.getElementById('toc');
      if (toc.classList.contains('show')) {
        toc.classList.remove('show');
        setTimeout(() => {
          toc.style.display = 'none';
        }, 300);
      } else {
        toc.style.display = 'block';
        setTimeout(() => {
          toc.classList.add('show');
        }, 10);
      }
    });
  }

  // 设置滚动监听，高亮当前所在的目录项
  function setupScrollSpy() {
    const headings = document.querySelectorAll('.vditor-preview h1, .vditor-preview h2, .vditor-preview h3, .vditor-preview h4, .vditor-preview h5, .vditor-preview h6');
    const tocLinks = document.querySelectorAll('#tocList a');

    if (headings.length === 0 || tocLinks.length === 0) return;

    // 监听滚动事件
    window.addEventListener('scroll', function() {
      let currentHeadingId = '';
      const scrollPosition = window.scrollY;

      // 找出当前视窗中最上方的标题
      headings.forEach(heading => {
        const headingTop = heading.getBoundingClientRect().top + scrollPosition - 100;
        if (scrollPosition >= headingTop) {
          currentHeadingId = heading.id;
        }
      });

      // 移除所有高亮
      tocLinks.forEach(link => {
        link.classList.remove('toc-active');
      });

      // 高亮当前项
      if (currentHeadingId) {
        const activeLink = document.querySelector(`#tocList a[data-target="${currentHeadingId}"]`);
        if (activeLink) {
          activeLink.classList.add('toc-active');
        }
      }
    });

    // 点击目录项平滑滚动到对应位置
    tocLinks.forEach(link => {
      link.addEventListener('click', function(e) {
        e.preventDefault();
        const targetId = this.getAttribute('href').substring(1);
        const targetElement = document.getElementById(targetId);

        if (targetElement) {
          window.scrollTo({
            top: targetElement.getBoundingClientRect().top + window.scrollY - 80,
            behavior: 'smooth'
          });
        }
      });
    });
  }

  /**
   * 获取文章的点赞和浏览数据
   */
  function fetchEngagementData(postId) {
    fetch("${pageContext.request.contextPath}/api/posts/engagement/" + postId)
            .then(response => {
              if (!response.ok) {
                throw new Error('获取数据失败');
              }
              return response.json();
            })
            .then(data => {
              // 更新UI显示，直接更新整个按钮内容，而不是尝试查找.count元素
              document.querySelector('#likeBtn').innerHTML = '<i class="ri-thumb-up-line"></i> ' + data.likes;

              // 根据用户是否已点赞更新按钮状态
              if (data.hasLiked) {
                document.querySelector('#likeBtn').classList.add('liked');
                // 更新移动端点赞按钮状态
                const mobileLikeBtn = document.getElementById('mobileLikeBtn');
                if (mobileLikeBtn) {
                  mobileLikeBtn.classList.add('active');
                }
              }

              // 更新文章页底部的统计数据
              updateStatsDisplay(data);
            })
            .catch(error => {
              console.error('获取数据失败:', error);
            });
  }

  /**
   * 更新文章的点赞状态
   */
  function updateEngagement(postId, action) {
    return fetch("${pageContext.request.contextPath}/api/posts/engagement/" + postId + "?action=" + action, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    })
            .then(response => {
              if (!response.ok) {
                if (response.status === 401) {
                  window.location.href = "${pageContext.request.contextPath}/login";
                  throw new Error('请先登录');
                }
                return response.json().then(data => {
                  throw new Error(data.error || '操作失败');
                });
              }
              return response.json();
            });
  }

  /**
   * 自动增加浏览量并更新显示
   */
  function incrementViewCount(postId) {
    return fetch("${pageContext.request.contextPath}/api/posts/engagement/" + postId + "?action=view", {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    })
            .then(response => {
              if (!response.ok) {
                throw new Error('更新浏览量失败');
              }
              return response.json();
            })
            .then(data => {
              // 更新文章页底部的统计数据
              updateStatsDisplay(data);
            })
            .catch(error => {
              console.error('更新浏览量失败:', error);
            });
  }

  /**
   * 更新统计显示
   */
  function updateStatsDisplay(data) {
    document.querySelector('.likes-count').innerHTML = '<i class="ri-thumb-up-line"></i> ' + data.likes;
    document.querySelector('.views-count').innerHTML = '<i class="ri-eye-line"></i> ' + data.views;
    // 更新点赞按钮内容
    document.querySelector('#likeBtn').innerHTML = '<i class="ri-thumb-up-line"></i> ' + data.likes;
  }

  // 设置交互按钮功能
  function setupInteractionButtons() {
    // 移动端底部导航栏交互
    const mobileLikeBtn = document.getElementById('mobileLikeBtn');
    const mobileTocBtn = document.getElementById('mobileTocBtn');
    const mobileCommentBtn = document.getElementById('mobileCommentBtn');
    const commentSection = document.querySelector('.comment-section');
    const toc = document.getElementById('toc');

    if (mobileLikeBtn) {
      mobileLikeBtn.addEventListener('click', function() {
        const likeBtn = document.getElementById('likeBtn');
        if (likeBtn) {
          likeBtn.click();
        }
        this.classList.toggle('active');
      });
    }

    if (mobileTocBtn) {
      // 确保目录在移动端可见
      const toc = document.getElementById('toc');
      if (window.innerWidth <= 1200) {
        toc.style.display = 'block';
      }

      mobileTocBtn.addEventListener('click', function() {
        toc.classList.toggle('show');
        this.classList.toggle('active');
      });

      // 点击目录外区域关闭目录
      document.addEventListener('click', function(event) {
        if (!toc.contains(event.target) && !mobileTocBtn.contains(event.target) && toc.classList.contains('show')) {
          toc.classList.remove('show');
          mobileTocBtn.classList.remove('active');
        }
      });

      // 监听窗口大小变化
      window.addEventListener('resize', function() {
        if (window.innerWidth <= 1200) {
          toc.style.display = 'block';
        } else {
          toc.style.display = 'none';
          toc.classList.remove('show');
          mobileTocBtn.classList.remove('active');
        }
      });
    }

    if (mobileCommentBtn) {
      mobileCommentBtn.addEventListener('click', function() {
        this.classList.toggle('active');
        commentSection.classList.toggle('show');
      });

      // 添加关闭评论区按钮事件
      const closeCommentBtn = document.getElementById('closeCommentBtn');
      if (closeCommentBtn) {
        closeCommentBtn.addEventListener('click', function() {
          commentSection.classList.remove('show');
          mobileCommentBtn.classList.remove('active');
        });
      }

      // 监听滑动手势，从底部向上滑动可以关闭评论区
      let touchStartY = 0;
      commentSection.addEventListener('touchstart', function(e) {
        touchStartY = e.touches[0].clientY;
      }, {passive: true});

      commentSection.addEventListener('touchmove', function(e) {
        // 只有当滑动评论区顶部区域时才触发关闭行为
        const touchTarget = e.target;
        const isHeader = touchTarget.tagName === 'H3' || touchTarget.closest('h3');
        const touchY = e.touches[0].clientY;
        const deltaY = touchY - touchStartY;

        // 如果向下滑动距离超过50像素并且是在标题区域滑动，则关闭评论区
        if (deltaY > 50 && isHeader) {
          commentSection.classList.remove('show');
          mobileCommentBtn.classList.remove('active');
        }
      }, {passive: true});
    }
    const likeBtn = document.getElementById('likeBtn');
    const submitCommentBtn = document.getElementById('submitComment');
    const postIdElement = document.getElementById('postId');

    if (!postIdElement) return;
    const postId = postIdElement.value;

    // 加载评论列表
    loadComments(postId);

    if (likeBtn) {
      likeBtn.addEventListener('click', function() {
        const isLiked = this.classList.contains('liked');
        const action = isLiked ? 'unlike' : 'like';

        // 发送AJAX请求更新点赞状态
        updateEngagement(postId, action)
                .then(data => {
                  // 更新UI
                  this.classList.toggle('liked');
                  // 直接更新整个按钮内容，而不是尝试查找.count元素
                  this.innerHTML = '<i class="ri-thumb-up-line"></i> ' + data.likes;
                  // 更新文章页底部的统计数据
                  updateStatsDisplay(data);
                })
                .catch(error => {
                  console.error('点赞操作失败:', error);
                  alert(error.message);
                });
      });
    }

    if (submitCommentBtn) {
      submitCommentBtn.addEventListener('click', function() {
        const commentText = document.getElementById('commentText').value.trim();
        if (!commentText) {
          alert('请输入评论内容');
          return;
        }

        const formData = new FormData();
        formData.append('postId', postId);
        formData.append('content', commentText);

        fetch('${pageContext.request.contextPath}/comments', {
          method: 'POST',
          body: new URLSearchParams(formData)
        })
                .then(response => response.json())
                .then(data => {
                  if (data.success) {
                    document.getElementById('commentText').value = '';
                    loadComments(postId); // 重新加载评论列表
                  } else {
                    alert(data.message || '评论失败');
                  }
                })
                .catch(error => {
                  console.error('提交评论失败:', error);
                  alert('评论失败，请稍后重试');
                });
      });
    }
  }

  // 加载评论列表
  function loadComments(postId) {
    fetch('${pageContext.request.contextPath}/comments?postId=' + postId)
            .then(response => response.json())
            .then(data => {
              if (data.success) {
                const commentList = document.getElementById('commentList');
                commentList.innerHTML = ''; // 清空现有评论

                // 先创建一个映射，用于组织父子评论关系
                const commentMap = new Map();
                const rootComments = [];

                // 第一步：将所有评论放入映射中，并识别顶级评论
                data.data.forEach(comment => {
                  commentMap.set(comment.id, {
                    comment: comment,
                    children: []
                  });

                  if (!comment.parentId) {
                    rootComments.push(comment);
                  }
                });

                // 第二步：构建评论树
                data.data.forEach(comment => {
                  if (comment.parentId) {
                    const parentCommentObj = commentMap.get(comment.parentId);
                    if (parentCommentObj) {
                      parentCommentObj.children.push(comment);
                    }
                  }
                });

                // 第三步：按照评论时间排序(最新的在前面)
                rootComments.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

                // 第四步：添加评论到DOM
                rootComments.forEach(comment => {
                  const commentObj = commentMap.get(comment.id);
                  addComment(comment, commentObj.children);
                });
              }
            })
            .catch(error => {
              console.error('加载评论失败:', error);
            });
  }

  // 添加评论到评论列表
  function addComment(comment, replies = []) {
    const commentList = document.getElementById('commentList');
    const commentContainer = document.createElement('div');
    commentContainer.className = 'comment-container';

    // 创建父评论元素
    const commentItem = document.createElement('div');
    commentItem.className = 'comment-item';
    commentItem.dataset.id = comment.id;
    commentItem.dataset.userId = comment.userId;
    commentItem.dataset.username = comment.username;

    const date = new Date(comment.createdAt);
    const formattedDate = date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });

    // 构建默认头像URL
    let avatarUrl = '${pageContext.request.contextPath}/avatar/default.png';
    // 如果用户有avatarUrl属性，则使用该属性
    if (comment.avatarUrl) {
      avatarUrl = '${pageContext.request.contextPath}/avatar/' + comment.avatarUrl + '?t=${sessionScope.lastAvatarUpdate}';
    }
    // 如果没有avatarUrl但有userId，则回退到以前的方式
    else if (comment.userId) {
      avatarUrl = '${pageContext.request.contextPath}/avatar/' + comment.userId + '.jpg?t=${sessionScope.lastAvatarUpdate}';
    }

    // 检查当前用户是否可以删除此评论
    let deleteButton = '';
    let replyButton = '';
    <% if (session.getAttribute("user") != null) { %>
    let currentUser = null;
    <% if (session.getAttribute("user") instanceof java.util.Map) { %>
    currentUser = {
      id: <%= ((java.util.Map)session.getAttribute("user")).get("id") %>,
      username: '<%= ((java.util.Map)session.getAttribute("user")).get("username") %>'
    };
    <% } %>

    if (currentUser && (currentUser.id < 5 || currentUser.id === comment.userId)) {
      deleteButton = '<button class="comment-delete-btn" data-id="' + comment.id + '"><i class="ri-delete-bin-line"></i></button>';
    }

    // 如果用户已登录，添加回复按钮
    if (currentUser) {
      replyButton = '<button class="comment-reply-btn" data-id="' + comment.id + '" data-user-id="' + comment.userId + '" data-username="' + comment.username + '"><i class="ri-reply-line"></i> 回复</button>';
    }
    <% } %>

    commentItem.innerHTML =
            '<div class="comment-header">' +
            '<div class="comment-author-group">' +
            '<img src="' + avatarUrl + '" alt="用户头像" class="comment-avatar" data-user-id="' + (comment.userId || '') + '" data-avatar-url="' + (comment.avatarUrl || '') + '">' +
            '<span class="comment-author">' + (comment.username || '访客') + '</span>' +
            '</div>' +
            '<div class="comment-actions">' +
            '<span class="comment-date">' + formattedDate + '</span>' +
            deleteButton +
            '</div>' +
            '</div>' +
            '<div class="comment-content">' +
            comment.content +
            '</div>' +
            '<div class="comment-footer">' +
            replyButton +
            '</div>';

    // 为删除按钮添加事件监听
    const deleteBtn = commentItem.querySelector('.comment-delete-btn');
    if (deleteBtn) {
      deleteBtn.addEventListener('click', function() {
        deleteComment(comment.id);
      });
    }

    // 为回复按钮添加事件监听
    const replyBtn = commentItem.querySelector('.comment-reply-btn');
    if (replyBtn) {
      replyBtn.addEventListener('click', function() {
        showReplyForm(this.dataset.id, this.dataset.userId, this.dataset.username);
      });
    }

    // 将评论项添加到容器
    commentContainer.appendChild(commentItem);

    // 如果有回复，添加回复区域
    if (replies && replies.length > 0) {
      const repliesContainer = document.createElement('div');
      repliesContainer.className = 'comment-replies';

      // 按时间顺序排序回复（最新的在前面）
      replies.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

      // 添加所有回复
      replies.forEach(reply => {
        const replyItem = createReplyElement(reply);
        repliesContainer.appendChild(replyItem);
      });

      commentContainer.appendChild(repliesContainer);
    }

    // 将整个评论容器添加到列表
    commentList.appendChild(commentContainer);
  }

  // 创建回复元素
  function createReplyElement(reply) {
    const replyItem = document.createElement('div');
    replyItem.className = 'reply-item';
    replyItem.dataset.id = reply.id;
    replyItem.dataset.userId = reply.userId;
    replyItem.dataset.username = reply.username;

    const date = new Date(reply.createdAt);
    const formattedDate = date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });

    // 构建默认头像URL
    let avatarUrl = '${pageContext.request.contextPath}/avatar/default.png';
    if (reply.avatarUrl) {
      avatarUrl = '${pageContext.request.contextPath}/avatar/' + reply.avatarUrl + '?t=${sessionScope.lastAvatarUpdate}';
    } else if (reply.userId) {
      avatarUrl = '${pageContext.request.contextPath}/avatar/' + reply.userId + '.jpg?t=${sessionScope.lastAvatarUpdate}';
    }

    // 检查当前用户是否可以删除此回复
    let deleteButton = '';
    let replyButton = '';
    <% if (session.getAttribute("user") != null) { %>
    let currentUser = null;
    <% if (session.getAttribute("user") instanceof java.util.Map) { %>
    currentUser = {
      id: <%= ((java.util.Map)session.getAttribute("user")).get("id") %>,
      username: '<%= ((java.util.Map)session.getAttribute("user")).get("username") %>'
    };
    <% } %>

    if (currentUser && (currentUser.id < 5 || currentUser.id === reply.userId)) {
      deleteButton = '<button class="comment-delete-btn" data-id="' + reply.id + '"><i class="ri-delete-bin-line"></i></button>';
    }

    // 如果用户已登录，添加回复按钮（二级评论）
    if (currentUser) {
      // 对于回复项，我们需要使用原始父评论的ID
      replyButton = '<button class="comment-reply-btn" data-id="' + reply.parentId + '" data-user-id="' + reply.userId + '" data-username="' + reply.username + '"><i class="ri-reply-line"></i> 回复</button>';
    }
    <% } %>

    // 构建回复内容，包含"@用户名"前缀
    let replyContent = reply.content;
    if (reply.replyToUsername) {
      replyContent = '<span class="reply-to">@' + reply.replyToUsername + '：</span> ' + replyContent;
    }

    replyItem.innerHTML =
            '<div class="reply-header">' +
            '<div class="reply-author-group">' +
            '<img src="' + avatarUrl + '" alt="用户头像" class="reply-avatar">' +
            '<span class="reply-author">' + (reply.username || '访客') + '</span>' +
            '</div>' +
            '<div class="reply-actions">' +
            '<span class="reply-date">' + formattedDate + '</span>' +
            deleteButton +
            '</div>' +
            '</div>' +
            '<div class="reply-content">' + replyContent + '</div>' +
            '<div class="reply-footer">' +
            replyButton +
            '</div>';

    // 为删除按钮添加事件监听
    const deleteBtn = replyItem.querySelector('.comment-delete-btn');
    if (deleteBtn) {
      deleteBtn.addEventListener('click', function() {
        deleteComment(reply.id);
      });
    }

    // 为回复按钮添加事件监听
    const replyBtn = replyItem.querySelector('.comment-reply-btn');
    if (replyBtn) {
      replyBtn.addEventListener('click', function() {
        showReplyForm(this.dataset.id, this.dataset.userId, this.dataset.username);
      });
    }

    return replyItem;
  }

  // 显示回复表单
  function showReplyForm(commentId, replyToUserId, replyToUsername) {
    // 移除任何现有的回复表单
    const existingForm = document.getElementById('replyForm');
    if (existingForm) {
      existingForm.remove();
    }

    // 获取评论元素 - 修复选择器语法
    const commentElement = document.querySelector('.comment-item[data-id="' + commentId + '"]');

    // 如果找不到评论元素，可能是因为这是回复项
    if (!commentElement) {
      console.log('尝试查找回复元素或父评论元素，评论ID:', commentId);
      const replyElement = document.querySelector('.reply-item[data-id="' + commentId + '"]');
      if (replyElement) {
        // 如果是回复元素，找到其所在的父评论容器
        const commentContainer = replyElement.closest('.comment-container');
        if (commentContainer) {
          const parentCommentElement = commentContainer.querySelector('.comment-item');
          if (parentCommentElement) {
            // 在父评论下添加回复表单
            addReplyFormAfterElement(parentCommentElement, commentId, replyToUserId, replyToUsername);
            return;
          }
        }
      }

      // 直接使用parentId查找父评论
      const parentCommentElement = document.querySelector('.comment-item[data-id="' + commentId + '"]');
      if (parentCommentElement) {
        addReplyFormAfterElement(parentCommentElement, commentId, replyToUserId, replyToUsername);
        return;
      }

      console.error('找不到评论元素:', commentId);
      return;
    }

    // 在评论元素后添加回复表单
    addReplyFormAfterElement(commentElement, commentId, replyToUserId, replyToUsername);
  }

  // 辅助函数：在指定元素后添加回复表单
  function addReplyFormAfterElement(element, commentId, replyToUserId, replyToUsername) {
    // 创建回复表单
    const replyForm = document.createElement('div');
    replyForm.id = 'replyForm';
    replyForm.className = 'reply-form';
    replyForm.innerHTML = '<div class="reply-form-header">回复 @' + replyToUsername + '：<button id="closeReplyForm" class="close-reply-form"><i class="ri-close-line"></i></button></div><textarea id="replyText" placeholder="输入回复内容..." class="reply-textarea"></textarea><div class="reply-form-footer"><button id="submitReply" class="submit-reply-btn">发送回复</button></div>';

    // 将表单添加到元素后面
    element.parentNode.insertBefore(replyForm, element.nextSibling);

    // 聚焦到回复文本框
    document.getElementById('replyText').focus();

    // 关闭回复表单的事件
    document.getElementById('closeReplyForm').addEventListener('click', function() {
      replyForm.remove();
    });

    // 提交回复的事件
    document.getElementById('submitReply').addEventListener('click', function() {
      const replyText = document.getElementById('replyText').value.trim();
      if (!replyText) {
        alert('请输入回复内容');
        return;
      }

      submitReply(commentId, replyToUserId, replyText);
    });
  }

  // 提交回复
  function submitReply(parentId, replyToUserId, content) {
    const postId = document.getElementById('postId').value;

    const formData = new FormData();
    formData.append('postId', postId);
    formData.append('content', content);
    formData.append('parentId', parentId);
    formData.append('replyToUserId', replyToUserId);

    fetch('${pageContext.request.contextPath}/comments', {
      method: 'POST',
      body: new URLSearchParams(formData)
    })
            .then(response => response.json())
            .then(data => {
              if (data.success) {
                // 移除回复表单
                const replyForm = document.getElementById('replyForm');
                if (replyForm) {
                  replyForm.remove();
                }

                // 重新加载评论列表
                loadComments(postId);
              } else {
                alert(data.message || '回复失败');
              }
            })
            .catch(error => {
              console.error('提交回复失败:', error);
              alert('回复失败，请稍后重试');
            });
  }

  // 添加删除评论的函数
  function deleteComment(commentId) {
    if (!confirm('确定要删除此评论吗？')) {
      return;
    }

    fetch('${pageContext.request.contextPath}/comments?commentId=' + commentId, {
      method: 'DELETE'
    })
            .then(response => response.json())
            .then(data => {
              if (data.success) {
                // 删除成功，从DOM中移除该评论或回复
                const commentElement = document.querySelector('.comment-item[data-id="' + commentId + '"]');
                const replyElement = document.querySelector('.reply-item[data-id="' + commentId + '"]');

                if (commentElement) {
                  // 如果是评论，删除整个评论容器
                  const commentContainer = commentElement.closest('.comment-container');
                  if (commentContainer) {
                    commentContainer.remove();
                  } else {
                    commentElement.remove();
                  }
                } else if (replyElement) {
                  // 如果是回复，只删除该回复元素
                  replyElement.remove();
                }

                // 刷新评论列表
                const postId = document.getElementById('postId').value;
                loadComments(postId);
              } else {
                alert(data.message || '删除评论失败');
              }
            })
            .catch(error => {
              console.error('删除评论失败:', error);
              alert('删除评论失败，请稍后重试');
            });
  }

  /**
   * 为图片添加注释支持
   */
  function addImageCaptions() {
    const images = document.querySelectorAll('.vditor-preview img');
    images.forEach(img => {
      // 获取图片的alt文本作为注释
      const caption = img.getAttribute('alt');
      if (caption) {
        // 创建注释容器
        const captionContainer = document.createElement('div');
        captionContainer.className = 'image-caption';
        captionContainer.style.textAlign = 'center';
        captionContainer.style.color = 'var(--text-secondary)';
        captionContainer.style.fontSize = '0.9em';
        captionContainer.style.marginTop = '5px';
        captionContainer.style.marginBottom = '20px';
        captionContainer.textContent = caption;

        // 将图片和注释包装在一个容器中
        const wrapper = document.createElement('div');
        wrapper.className = 'image-wrapper';
        wrapper.style.textAlign = 'center';

        // 替换原始图片
        img.parentNode.insertBefore(wrapper, img);
        wrapper.appendChild(img);
        wrapper.appendChild(captionContainer);
      }
    });
  }
</script>
</body>
</html>