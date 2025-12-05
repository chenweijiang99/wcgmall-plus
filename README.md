# WCG Mall Plus - 文创商城管理系统

## 项目简介

WCG Mall Plus 是一个基于 Spring Boot 3.x 和 Vue 3.x 的全栈电商管理系统，专注于文创商品销售和传统文化推广。项目采用前后端分离架构，包含管理后台和用户端两个独立应用。

接入了支付宝沙盒支付和顺丰快递沙盒api，实现订单支付和物流信息

理论上，删除所有的商品数据及分类，可以重构为任意主题的商城（宠物、水果、鲜花等）

目前还有部分功能在开发中，如遇问题请联系邮箱或者提交Issues

邮箱：1774532899@qq.com

访问 http://wcgmall-plus-web.wcgmallcwj.online/ 查看用户端

访问 http://wcgmall-plus-admin.wcgmallcwj.online/ 查看管理端

账户：test
密码：123456

支付账户：oworsb4854@sandbox.com
密码： 111111


### 项目特色
- 🎨 **文创特色**：专注于传统文化商品和文创产品
- 🚀 **现代化技术栈**：Spring Boot 3.x + Vue 3.x + Element Plus
- 📱 **响应式设计**：支持PC端和移动端访问
- 🔐 **安全认证**：基于JWT的完整权限管理系统
- 📊 **数据可视化**：集成ECharts实现数据统计和分析

## 项目结构

```
wcgmall-plus/
├── river/                          # 后端Java项目
│   ├── admin/                     # 管理后台API模块
│   ├── api/                       # 用户端API模块
│   ├── auth/                      # 认证授权模块
│   ├── common/                    # 公共模块
│   ├── file/                      # 文件管理模块
│   ├── generator/                 # 代码生成器
│   ├── quartz/                    # 定时任务模块
│   └── server/                    # 服务启动模块
├── wcgmall-admin/                 # 管理后台前端
└── wcgmall-user/                  # 用户端前端
```

## 技术栈

### 后端技术栈
- **框架**: Spring Boot 3.1.2
- **Java版本**: 17
- **ORM**: MyBatis Plus 3.5.5
- **数据库**: MySQL 5.7+
- **缓存**: Redis
- **认证**: JWT
- **任务调度**: Quartz
- **构建工具**: Maven

### 前端技术栈
- **框架**: Vue 3.2.47
- **构建工具**: Vite 5.4.21
- **UI组件**: Element Plus 2.3.0
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **样式**: Tailwind CSS + SCSS
- **TypeScript**: 5.4.5/5.9.3

## 功能模块

### 管理后台功能
- 📊 **仪表盘**：数据统计和可视化
- 👥 **用户管理**：用户信息、权限管理
- 🛍️ **商品管理**：商品分类、商品信息、库存管理
- 📝 **内容管理**：博客文章、轮播图管理
- 📦 **订单管理**：订单处理、物流跟踪
- 🔧 **系统管理**：菜单管理、角色管理、操作日志

### 用户端功能
- 🏠 **首页展示**：商品推荐、轮播图
- 🛒 **商品浏览**：分类浏览、搜索筛选
- 📋 **购物车**：商品添加、数量修改
- 💳 **订单管理**：下单、支付、订单跟踪
- 👤 **个人中心**：个人信息、收货地址管理
- 📝 **博客阅读**：文创知识、传统文化文章

## 快速开始

### 环境要求
- JDK 17+
- Node.js 16+
- MySQL 5.7+
- Redis 6+
- Maven 3.6+

### 数据库初始化
1. 创建数据库：`wcg_mall_plus`
2. 执行SQL脚本：`wcgmall-plus.sql`

### 后端启动
```bash
# 进入后端目录
cd river

# 编译项目
mvn clean compile

# 打包
mvn clean package

# 运行
java -jar server/target/river-server.jar
```

### 前端启动

#### 管理后台
```bash
# 进入管理后台目录
cd wcgmall-admin

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

#### 用户端
```bash
# 进入用户端目录
cd wcgmall-user

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

## 配置说明

### 后端配置
在 `river/server/src/main/resources/application.yml` 中配置：
- 数据库连接
- Redis配置
- 文件上传路径
- JWT密钥

### 前端配置
在对应项目的 `.env.development` 和 `.env.production` 中配置：
- API接口地址
- 端口号
- 其他环境变量

## 部署说明

### 生产环境部署

#### 后端部署
1. 修改生产环境配置文件
2. 打包：`mvn clean package -DskipTests`
3. 上传jar包到服务器
4. 使用systemd或docker运行

#### 前端部署
1. 构建生产版本：`npm run build`
2. 部署dist目录到Nginx或CDN
3. 配置反向代理

### Docker部署（可选）
项目支持Docker容器化部署，参考各目录下的Dockerfile。

## API文档

项目提供完整的API接口文档，可通过以下方式访问：
- Swagger UI: `http://localhost:8080/swagger-ui.html`

---

**注意**: 本项目仍在持续开发中，部分功能可能尚未完善。欢迎提出建议和贡献代码！