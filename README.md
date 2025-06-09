# 星光小栈 - 个人博客系统

![Java](https://img.shields.io/badge/Java-8+-orange.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)
![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

一个基于 Java Web 技术栈开发的现代化个人博客系统，支持文章发布、分类管理、用户交互等功能。

## ✨ 功能特性

- 📝 **文章管理**：支持文章的创建、编辑、删除和发布
- 🏷️ **分类管理**：灵活的文章分类系统
- 👥 **用户系统**：用户注册、登录、权限管理
- 💬 **评论系统**：支持文章评论和回复
- 🔍 **搜索功能**：全文搜索文章内容
- 📱 **响应式设计**：适配桌面端和移动端
- 🎨 **现代化UI**：简洁美观的用户界面
- 🔐 **安全防护**：XSS防护、SQL注入防护
- 📧 **邮件通知**：支持邮件发送功能
- 📊 **数据统计**：文章浏览量、用户活跃度统计

## 🎯 在线演示

🌐 **在线体验**：[NixStudio](http://nixstudio.cn)

> 注：在线演示环境仅供体验，请勿发布不当内容

## 🛠️ 技术栈

### 后端技术
- **Java 8**：核心开发语言
- **Servlet/JSP**：Web开发框架
- **MySQL 8.0**：数据库
- **Apache Commons DBCP2**：数据库连接池
- **Jackson**：JSON处理
- **JavaMail**：邮件发送
- **Apache HttpClient**：HTTP客户端
- **Log4j2**：日志框架

### 前端技术
- **HTML5/CSS3**：页面结构和样式
- **JavaScript**：交互逻辑
- **JSP/JSTL**：服务端渲染
- **Remixicon**：图标库
- **响应式设计**：适配多种设备

### 开发工具
- **Maven**：项目构建和依赖管理
- **IntelliJ IDEA**：开发IDE

## 📁 项目结构

```
personal_blog_Idea/
├── src/main/
│   ├── java/com/blog/
│   │   ├── controller/     # 控制器层
│   │   ├── dao/           # 数据访问层
│   │   ├── filter/        # 过滤器
│   │   ├── model/         # 实体类
│   │   ├── service/       # 业务逻辑层
│   │   ├── servlet/       # Servlet处理器
│   │   └── util/          # 工具类
│   ├── resources/         # 配置文件
│   └── webapp/           # Web资源
│       ├── WEB-INF/      # Web配置和组件
│       ├── css/          # 样式文件
│       ├── js/           # JavaScript文件
│       ├── images/       # 图片资源
│       └── index.jsp     # 首页
├── db_blog.sql           # 数据库脚本
├── pom.xml              # Maven配置
└── README.md            # 项目说明
```

## 📦 部署指南

### 🚀 快速开始

```bash
# 1. 克隆项目
git clone https://github.com/NixLockhart/personal_blog.git
cd personal_blog

# 2. 配置数据库（确保MySQL已启动）
mysql -u root -p < db_blog.sql

# 3. 修改数据库配置
# 编辑 src/main/java/com/blog/util/DBUtil.java

# 4. 编译运行
mvn clean package
# 部署到Tomcat或使用IDE运行
```

### 📋 环境准备
1. **JDK 8+**
2. **MySQL 8.0+**
3. **Maven 3.6+**
4. **Tomcat 9.0+**

### 🔧 详细部署步骤

1. **克隆或下载项目**

2. **数据库配置**
   - 创建MySQL数据库：`db_blog`
   - 导入数据库脚本：
     ```bash
     mysql -u root -p db_blog < db_blog.sql
     ```
   - 也可使用Navicat等工具直接运行`db_blog.sql`脚本

3. **配置数据库连接**
   - 修改 `src/main/java/com/blog/util/DBUtil.java` 文件中的数据库连接信息：
   ```java
   // 数据库连接配置(第18-23行)
   dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
   dataSource.setUrl("jdbc:mysql://localhost:3306/db_blog?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true");
   dataSource.setUsername("db_blog");  // 修改为您的数据库用户名
   dataSource.setPassword("root");     // 修改为您的数据库密码
   ```

4. **编译项目**
   ```bash
   mvn clean compile
   ```

5. **打包部署**
   ```bash
   mvn clean package
   ```
   - 将生成的 `target/personal-blog.war` 部署到Tomcat服务器

6. **启动服务**
   - 启动Tomcat服务器
   - 访问：`http://localhost:8080/personal-blog`

### 默认账户

- **管理员账户**：
  - 用户名：`admin`
  - 密码：`admin123!`
  - 邮箱：`1@1.com`

- **测试用户账户**：
  - 用户名：`user`
  - 密码：`1`


## IntelliJ IDEA 开发环境部署

### 环境准备
1. **JDK 8+**
2. **MySQL 8.0+**
3. **Maven 3.6+**
4. **Tomcat 9.0+**

### IDEA项目导入与配置

#### 1. 导入项目
1. 打开IntelliJ IDEA
2. 选择 `文件` → `打开` → 选择项目根目录
3. IDEA会自动识别为Maven项目并导入

#### 2. 项目结构配置
1. **File** → **Project Structure** (Ctrl+Alt+Shift+S)
2. **Project Settings** → **Project**：
   - Project SDK: 选择JDK 8或更高版本
   - Project language level: 8或更高
3. **Modules** → **Dependencies**：
   - 确保Maven依赖正确加载

#### 3. 数据库配置
1. **创建数据库**：
   ```sql
   CREATE DATABASE db_blog CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   ```

2. **导入数据**：
   - 在IDEA中打开 `db_blog.sql`
   - 连接到MySQL数据库
   - 执行SQL脚本导入数据

3. **修改数据库连接**：
   - 打开 `src/main/java/com/blog/util/DBUtil.java`
   - 修改第21-23行的数据库连接信息

#### 4. Tomcat服务器配置
1. **添加Tomcat配置**：
   - 点击右上角 `编辑配置`
   - 点击 `+` → `Tomcat服务器` → `本地`

2. **服务器设置**：
   - Name: `personal-blog`
   - Application server: 选择Tomcat安装路径
   - HTTP port: `8080`（默认）

3. **部署设置**：
   - 切换到 `部署` 标签
   - 点击 `+` → `工件`
   - 选择 `personal-blog:war exploded`
   - Application context: `/personal-blog`

#### 5. Maven配置
1. **刷新Maven依赖**：
   - 右侧Maven面板 → 点击刷新按钮
   - 或 `视图` → `工具窗口` → `Maven` → 刷新

2. **编译项目**：
   - 右侧Maven面板 → `生存期` → `compile`

#### 6. 运行项目
1. **启动方式一（推荐）**：
   - 点击右上角的运行按钮
   - 选择配置好的Tomcat服务器

2. **启动方式二**：
   - 在Terminal中执行：
   ```bash
   mvn clean package
   # 将target/personal-blog.war部署到Tomcat
   ```

3. **访问应用**：
   - 浏览器访问：`http://localhost:8080/personal-blog`

## 📋 系统要求

### 最低要求
- **操作系统**：Windows 7+ / macOS 10.12+ / Linux
- **Java**：JDK 8+
- **内存**：2GB RAM
- **存储**：500MB 可用空间
- **数据库**：MySQL 5.7+ 或 MySQL 8.0+

### 推荐配置
- **操作系统**：Windows 10+ / macOS 12+ / Ubuntu 20.04+
- **Java**：JDK 11+
- **内存**：4GB+ RAM
- **存储**：2GB+ 可用空间
- **数据库**：MySQL 8.0+

## 🔧 配置说明

### 数据库配置

项目的数据库配置位于 `src/main/java/com/blog/util/DBUtil.java`：

```java
// 根据您的环境修改以下配置
dataSource.setUrl("jdbc:mysql://localhost:3306/db_blog?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true");
dataSource.setUsername("your_username");  // 您的数据库用户名
dataSource.setPassword("your_password");   // 您的数据库密码
```

### 邮件配置

如需启用邮件功能，请配置邮件服务器信息：

**配置文件位置**：`src/main/java/com/blog/service/EmailService.java`

**需要配置的参数**：
- SMTP服务器地址
- 发件人邮箱
- 邮箱授权码（注意：不是邮箱密码，是第三方应用授权码）

**常用邮箱SMTP配置**：
- **QQ邮箱**：smtp.qq.com，端口587
- **163邮箱**：smtp.163.com，端口25
- **Gmail**：smtp.gmail.com，端口587

### 文件路径配置

#### 头像存储路径

**配置文件**：`src/main/java/com/blog/util/AvatarUtil.java`

```java
// Linux生产环境路径示例
private static final String AVATAR_PATH = "/var/www/blog/avatars/";

// Windows开发环境路径示例
private static final String AVATAR_PATH = "D:\\blog\\avatars\\";
```

#### 博文存储路径

**配置文件**：`src/main/java/com/blog/util/MarkdownUtil.java`

```java
// Linux生产环境路径示例
private static final String MARKDOWN_PATH = "/var/www/blog/articles/";

// Windows开发环境路径示例
private static final String MARKDOWN_PATH = "D:\\blog\\articles\\";
```

> **注意**：请根据实际部署环境选择合适的路径，并确保应用有读写权限

### 个性化定制

#### 网站Logo和图标

**存储位置**：`src/main/webapp/images/elements/`

#### 背景图片

**存储位置**：`src/main/webapp/images/background/`

**推荐规格**：
- Logo：建议尺寸 200x60px，PNG格式
- 背景图：建议尺寸 1920x1080px，JPG格式
- 图片大小控制在500KB以内，确保加载速度

## 🤝 贡献指南

我们欢迎所有形式的贡献！请遵循以下步骤：

### 如何贡献

1. **Fork** 本仓库
2. 创建您的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交您的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个 **Pull Request**

### 代码规范

- 遵循 Java 编码规范
- 添加适当的注释
- 确保代码通过所有测试
- 提交信息要清晰明了

### 报告问题

如果您发现了 bug 或有功能建议，请：

1. 检查 [Issues](https://github.com/NixLockhart/personal_blog/issues) 中是否已有相关问题
2. 如果没有，请创建一个新的 Issue
3. 详细描述问题或建议
4. 如果是 bug，请提供复现步骤

## ❓ 常见问题

### Q: 数据库连接失败怎么办？
A: 请检查：
- MySQL 服务是否启动
- 数据库连接信息是否正确
- 数据库是否已创建
- 用户权限是否足够

### Q: 如何修改默认端口？
A: 修改 Tomcat 配置文件中的端口设置，或在 IDE 中修改运行配置。

### Q: 如何自定义主题？
A: 修改 `webapp/css/` 目录下的样式文件，或创建新的主题文件。

## 🔗 相关链接

- 📺 [项目演示视频（还没录）]()

## 📄 许可证

本项目基于 [MIT License](LICENSE) 开源协议。

## 📞 联系方式

- **项目维护者**：[Nix Lockhart](mailto:18293516327@163.com)
- **项目主页**：[https://github.com/NixLockhart/personal_blog](https://github.com/NixLockhart/personal_blog)
- **问题反馈**：[Issues](https://github.com/NixLockhart/personal_blog/issues)
- **在线演示**：[NixStudio](http://nixstudio.cn)

---

⭐ 如果这个项目对您有帮助，请给我一个 Star！

📢 欢迎关注项目更新，获取最新功能和修复！
