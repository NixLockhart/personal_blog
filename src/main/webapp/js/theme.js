/**
 * 主题切换功能
 * 实现了带扩散效果的深色/浅色模式切换
 */
document.addEventListener('DOMContentLoaded', function() {
    // 获取或设置初始主题
    const savedTheme = localStorage.getItem('theme') || 'light';

    // 设置初始主题
    if (savedTheme === 'dark') {
        document.documentElement.setAttribute('data-theme', 'dark');
        document.body.classList.add('theme-transition');
    }

    // 创建主题切换扩散元素
    const themeRipple = document.createElement('div');
    themeRipple.className = 'theme-ripple';
    document.body.appendChild(themeRipple);

    // 在页面加载时添加过渡类
    setTimeout(() => {
        document.body.classList.add('theme-transition');
    }, 100);

    // 切换主题函数
    function toggleTheme(event) {
        // 获取点击位置
        const x = event ? event.clientX : window.innerWidth / 2;
        const y = event ? event.clientY : window.innerHeight / 2;

        // 获取当前主题
        const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
        const newTheme = currentTheme === 'light' ? 'dark' : 'light';

        // 设置扩散元素的颜色和位置
        themeRipple.style.backgroundColor = newTheme === 'dark' ? '#18191a' : '#f8f9fa';
        themeRipple.style.left = `${x}px`;
        themeRipple.style.top = `${y}px`;

        // 先清除之前的动画
        themeRipple.classList.remove('active');

        // 强制重绘
        void themeRipple.offsetWidth;

        // 立即改变主题，让页面随动画逐渐变化
        document.documentElement.setAttribute('data-theme', newTheme);
        localStorage.setItem('theme', newTheme);

        // 添加扩散动画
        themeRipple.classList.add('active');

        // 更新所有主题图标
        const themeIcons = [document.getElementById('themeIcon'), document.getElementById('adminThemeIcon')];
        themeIcons.forEach(icon => {
            if (icon) {
                icon.className = newTheme === 'dark' ? 'ri-moon-line' : 'ri-sun-line';
            }
        });

        // 动画结束后清理
        setTimeout(() => {
            themeRipple.classList.remove('active');
        }, 1100);

        // 阻止事件冒泡
        if (event) {
            event.stopPropagation();
        }
    }

    // 初始化主题切换按钮事件
    const themeToggles = document.querySelectorAll('.theme-toggle');
    themeToggles.forEach(toggle => {
        toggle.addEventListener('click', toggleTheme);
    });

    // 提供全局访问
    window.toggleTheme = toggleTheme;
});