// 防止闪烁的主题初始化脚本
// 这个脚本必须在CSS加载前执行
(function() {
    // 立即读取保存的主题
    var savedTheme = localStorage.getItem('theme');

    // 如果是深色模式，立即应用
    if(savedTheme === 'dark') {
        document.documentElement.setAttribute('data-theme', 'dark');

        // 添加一个class用于控制过渡效果
        // 在页面加载时不使用过渡，避免闪烁
        document.documentElement.classList.add('theme-no-transition');

        // 页面加载后移除no-transition类，恢复过渡效果
        window.addEventListener('load', function() {
            setTimeout(function() {
                document.documentElement.classList.remove('theme-no-transition');
            }, 100);
        });
    }
})();