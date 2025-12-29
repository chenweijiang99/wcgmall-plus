# WCG Mall Plus 技术架构文档

## 一、项目概述

WCG Mall Plus 是一个基于 Spring Boot 3.x 和 Vue 3.x 的全栈文创商城管理系统，专注于传统文化商品和文创产品的在线销售。项目采用前后端分离架构，包含管理后台和用户端两个独立应用。

可以快速重构为其他任意的商城系统。大部分页面都是动态配置，只需要在后台进行配置即可。

支持auth2第三方登录，支持多个存储平台，接入了支付宝沙盒支付和顺丰api。

目前正在缓慢开发中，欢迎大家提issue和pr。

### 1.1 项目特色

- 🎨 **文创特色**：集成博客系统，支持文创内容营销
- 🚀 **现代化技术栈**：Spring Boot 3.x + Vue 3.x + Element Plus
- 📱 **响应式设计**：支持PC端和移动端访问（仅限用户端）
- 🔐 **安全认证**：基于Sa-Token的完整权限管理系统
- 📊 **数据可视化**：集成ECharts实现数据统计和分析
- 🤖 **AI集成**：接入豆包推理AI，支持智能摘要生成
- 📦 **多云存储**：支持多种云存储平台，如阿里云、腾讯云、七牛云、MinIO
- 💳 **其他功能**： 集成支付宝沙盒支付，支持顺丰api，支持邮件服务。

### 1.2 在线地址

- **用户端**:  http://117.72.179.87:86/
- **管理端**:  http://117.72.179.87:87/
- **测试账户**: test / 123456
- **支付宝支付账户**: oworsb4854@sandbox.com / 111111

## 二、后端技术架构

### 2.1 技术栈详情

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.1.2 | 核心框架 |
| Java | 17 (LTS) | 编程语言 |
| MyBatis Plus | 3.5.5 | ORM框架 |
| MySQL | 5.7+ | 关系型数据库 |
| Redis | 6+ | 缓存数据库 |
| Sa-Token | 1.44.0 | 权限认证框架 |
| Knife4j | 4.4.0 | API文档工具 |
| Quartz | - | 定时任务 |
| Hutool | 5.8.18 | 工具类库 |
| Lombok | 1.18.22 | 代码简化工具 |

### 2.2 模块化架构

项目采用多模块Maven架构，模块划分清晰：

```
river/                            # 后端根项目
├── common/                       # 公共模块
│   ├── pom.xml                   # 核心依赖管理
│   └── src/main/java/com/river/  # 公共类、工具、配置
├── admin/                        # 管理后台API模块
├── api/                          # 用户端API模块
├── auth/                         # 认证授权模块
├── chat/                         # 实时聊天模块
├── file/                         # 文件管理模块
├── quartz/                       # 定时任务模块
├── generator/                    # 代码生成器
└── server/                       # 启动模块
    ├── pom.xml                   # 聚合所有模块
    └── src/main/java/com/river/
        └── ServerApplication.java # 启动类
```

### 2.3 核心功能模块

#### 2.3.1 用户认证与授权

**技术实现**：
- 使用Sa-Token实现无状态JWT认证
- Redis存储用户会话信息
- 基于RBAC模型的权限控制
- 支持多端登录（允许/禁止）

**配置要点**：
```yaml
sa-token:
  token-name: Authorization
  timeout: 604800        # 7天有效期
  is-concurrent: true    # 允许多地登录
  token-style: uuid
```

#### 2.3.2 商品管理系统

**功能特点**：
- 商品分类树形结构管理
- 商品品牌管理
- 商品SKU/库存管理
- 商品状态控制（上架/下架）
- 支持商品主图和详情图

**数据模型**：
```java
// 商品核心字段
private String productName;      // 商品名称
private BigDecimal productPrice;  // 价格
private Integer productStock;     // 库存
private String productMainImage;  // 主图
private String productImages;     // 详情图
private Integer productStatus;    // 状态
```

#### 2.3.3 订单管理系统

**订单状态流转**：
```
待付款 → 已付款 → 待发货 → 已发货 → 已完成
   ↓        ↓        ↓        ↓
已取消   退款中   退款中   退款中
```

**核心功能**：
- 购物车商品下单
- 订单支付（支付宝）
- 订单取消和退款
- 物流信息跟踪

#### 2.3.4 支付集成

**支付宝沙盒集成**：
- 完整的支付流程
- 异步通知处理
- 退款功能
- 交易状态查询

```yaml
alipay:
  app-id: [应用ID]
  private-key: [私钥]
  gateway-url: https://openapi-sandbox.dl.alipaydev.com/gateway.do
```

#### 2.3.5 文件管理

**支持多种存储方式**：
- 本地存储
- 阿里云OSS
- 腾讯云COS
- 七牛云
- MinIO

### 2.4 第三方服务集成

1. **支付系统**：支付宝沙盒完整集成
2. **物流系统**：顺丰快递API
3. **云存储**：多云存储支持
4. **AI服务**：豆包推理AI SDK
5. **邮件服务**：Spring Mail

## 三、前端技术架构

### 3.1 管理后台（wcgmall-admin）

#### 3.1.1 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.2.47 | 前端框架 |
| TypeScript | 5.4.5 | 类型系统 |
| Vite | 5.4.21 | 构建工具 |
| Element Plus | 2.3.0 | UI组件库 |
| Pinia | 2.0.33 | 状态管理 |
| Vue Router | 4.1.6 | 路由管理 |
| ECharts | 5.5.1 | 图表库 |
| Tailwind CSS | 4.1.16 | 样式框架 |
| Axios | 1.3.4 | HTTP客户端 |

#### 3.1.2 功能模块

```
src/views/
├── dashboard/           # 数据仪表盘
│   └── index.vue       # ECharts数据可视化
├── system/            # 系统管理
│   ├── user/          # 用户管理
│   ├── role/          # 角色管理
│   ├── menu/          # 菜单管理
│   └── dict/          # 字典管理
├── product/           # 商品管理
│   ├── category/      # 商品分类
│   ├── brand/         # 品牌管理
│   └── list/          # 商品列表
├── order/             # 订单管理
│   ├── list/          # 订单列表
│   └── logistics/     # 物流管理
├── blog/              # 博客管理
├── comments/          # 评论管理
├── slider/            # 轮播图管理
└── monitor/           # 系统监控
    ├── server/        # 服务监控
    ├── cache/         # 缓存监控
    └── job/           # 定时任务
```

#### 3.1.3 技术亮点

1. **动态路由**：根据用户权限动态生成菜单
2. **组件自动导入**：使用unplugin-auto-import
3. **SVG图标系统**：自定义SVG图标管理
4. **权限指令**：v-permission指令控制按钮权限
5. **响应式布局**：适配不同屏幕尺寸

### 3.2 用户端（wcgmall-user）

#### 3.2.1 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.2.47 | 前端框架 |
| TypeScript | 5.9.3 | 类型系统 |
| Element Plus | 2.3.0 | UI组件库 |
| Pinia | 3.0.4 | 状态管理 |
| Swiper | 12.0.3 | 轮播组件 |
| md-editor-v3 | 5.8.5 | Markdown编辑器 |

#### 3.2.2 用户端功能

```
src/views/
├── Home.vue           # 首页（轮播图、推荐商品）
├── Shop.vue           # 商品列表（分类、筛选）
├── ProductDetail.vue  # 商品详情
├── Cart.vue           # 购物车
├── Checkout.vue       # 结算页面
├── Pay.vue            # 支付页面
├── Profile.vue        # 个人中心
├── Blog.vue           # 博客列表
├── BlogDetail.vue     # 博客详情
└── CreateBlog.vue     # 发布博客
```

#### 3.2.3 特色功能

1. **购物车管理**：本地存储 + 同步服务端
2. **商品筛选**：多维度筛选和搜索
3. **评论系统**：支持图文评论和回复
4. **博客系统**：Markdown编辑器 + AI摘要

## 四、数据库设计

### 4.1 数据库概览

- **数据库**：MySQL 5.7+
- **表数量**：30张核心业务表
- **存储引擎**：MyISAM（高性能读写）
- **字符集**：UTF-8

### 4.2 核心表结构

#### 4.2.1 用户权限相关

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| sys_user | 系统用户表 | username, password, real_name, status |
| sys_role | 角色表 | role_name, role_key, sort |
| sys_menu | 菜单权限表 | menu_name, path, component, perms |
| sys_user_role | 用户角色关联表 | user_id, role_id |

#### 4.2.2 商品相关

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| product | 商品表 | product_name, price, stock, category_id |
| product_category | 商品分类表 | name, parent_id, sort |
| product_brand | 品牌表 | name, logo, description |
| shopping_cart | 购物车表 | user_id, product_id, quantity |

#### 4.2.3 订单相关

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| product_order | 订单表 | order_no, user_id, total_amount, status |
| order_detail | 订单详情表 | order_id, product_id, quantity, price |
| address_book | 地址簿表 | user_id, consignee, phone, address |
| order_logistics | 物流信息表 | order_id, express_no, logistics_info |

#### 4.2.4 内容相关

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| blog | 博客文章表 | title, content, author, view_count |
| comments | 评论表 | content, user_id, blog_id, parent_id |
| index_slider | 首页轮播图 | title, image_url, link_url |
| shop_slider | 商城轮播图 | title, image_url, link_url |

### 4.3 设计特点

1. **统一规范**：所有表包含create_time、update_time字段
2. **软删除**：重要表支持逻辑删除（is_deleted字段）
3. **索引优化**：关键字段建立索引提升查询性能
4. **状态管理**：使用整型字段管理状态

## 五、API接口设计

### 5.1 接口规范

- **RESTful风格**：遵循REST API设计规范
- **统一响应格式**：
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {}
  }
  ```

- **认证方式**：Header中携带Authorization Token
- **API文档**：集成Knife4j，访问 `/doc.html`

### 5.2 接口分组

| 分组 | 路径前缀 | 说明 |
|------|----------|------|
| 管理端 | /sys/** | 管理后台接口 |
| 用户端 | /user/** | 用户端接口 |
| 认证中心 | /auth/** | 登录注册接口 |
| 文件中心 | /file/** | 文件上传接口 |
| 聚合登录 | /api/juhe/** | 第三方登录接口 |

## 六、部署架构

### 6.1 开发环境

- **后端**：`mvn spring-boot:run`，端口8080
- **管理后台**：`npm run dev`，端口3000
- **用户端**：`npm run dev`，端口3001
- **数据库**：MySQL 5.7+，端口3306
- **缓存**：Redis 6+，端口6379

### 6.2 生产环境部署

#### 6.2.1 后端部署

```bash
# 打包
mvn clean package -DskipTests

# 运行
java -jar river-server.jar --spring.profiles.active=prod
```

#### 6.2.2 前端部署

```bash
# 构建生产版本
npm run build

# Nginx配置
server {
    listen 80;
    server_name wcgmall-plus-admin.wcgmallcwj.online;
    root /path/to/dist;

    location /api {
        proxy_pass http://localhost:8080;
    }
}
```

### 6.3 容器化部署（可选）

项目提供Docker支持，可使用Docker Compose一键部署：
- MySQL容器
- Redis容器
- 后端应用容器
- Nginx容器（前端）

## 七、性能优化

### 7.1 后端优化

1. **缓存策略**：
   - Redis缓存热点数据
   - Spring Cache注解管理缓存
   - 分类、品牌等静态数据缓存

2. **数据库优化**：
   - MyBatis Plus分页插件
   - 复杂查询优化
   - 索引合理使用

3. **接口优化**：
   - 批量查询减少N+1问题
   - 延迟加载
   - 异步处理

### 7.2 前端优化

1. **构建优化**：
   - Vite快速构建
   - 代码分割
   - Tree shaking

2. **运行时优化**：
   - 路由懒加载
   - 组件异步加载
   - 图片懒加载

## 八、安全机制

### 8.1 认证安全

- JWT Token认证
- Token有效期控制
- Redis存储会话
- 登录失败限制

### 8.2 权限控制

- 基于RBAC的权限模型
- 接口级权限控制
- 前端按钮级权限控制
- 数据权限隔离

### 8.3 数据安全

- 密码BCrypt加密
- SQL注入防护
- XSS攻击防护
- CSRF防护

## 九、监控与运维

### 9.1 系统监控

- JVM监控
- CPU/内存使用率
- 接口响应时间
- 错误日志追踪

### 9.2 业务监控

- 用户行为日志
- 操作日志记录
- 订单状态监控
- 支付状态追踪

### 9.3 日志管理

- Logback日志框架
- 日志分级管理
- 日志文件滚动
- 异步日志输出

## 十、项目亮点

1. **现代化技术栈**：采用最新的Spring Boot 3和Vue 3，技术领先
2. **模块化设计**：清晰的模块划分，便于团队协作和维护
3. **完整的电商功能**：从商品到订单到支付，形成电商闭环
4. **文创特色**：集成博客系统，支持内容营销
5. **权限系统完善**：基于Sa-Token的现代化权限管理
6. **易于扩展**：代码生成器 + 标准化接口设计
7. **运维友好**：完整的监控体系和定时任务支持

## 十一、扩展性

通过调整商品数据，项目可以快速重构为：
- 宠物用品商城
- 鲜花商城
- 水果商城
- 图书商城
- 数字商品商城

核心架构不变，只需替换商品分类和相关UI元素即可。

## 十二、总结

WCG Mall Plus 是一个技术先进、功能完善的全栈电商管理系统。项目采用了业界最佳实践，具有良好的可扩展性和维护性。无论是作为学习参考还是商业项目基础，都是一个优秀的选择。

项目持续开发中，欢迎贡献代码和提出建议！

---
**文档版本**：v1.0
**更新日期**：2025-12-11
**联系方式**：1774532899@qq.com
