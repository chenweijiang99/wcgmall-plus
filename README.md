# Spring Boot 多模块商城项目

## 项目说明

这是一个基于 Spring Boot 的多模块商城项目，包含后台管理、API 接口、认证授权、公共模块、文件管理和服务模块等多个模块。

## 模块说明

- `admin`: 后台管理模块，提供系统管理界面
- `api`: API 接口模块，提供商城核心功能接口
- `auth`: 认证授权模块，处理用户登录、权限控制等
- `common`: 公共模块，包含通用工具类和组件
- `file`: 文件管理模块，处理文件上传、存储等
- `server`: 服务模块，包含主要业务逻辑

## 技术栈

- Spring Boot
- MyBatis Plus
- Sa-Token (权限认证)
- Redis
- MySQL
- Vue.js (前端)

## 功能特性

- 用户认证与授权 (JWT, Sa-Token)
- 商品管理
- 订单处理
- 支付集成 (支付宝等)
- 文件上传与管理
- 数据统计与监控
- 代码生成器

## 构建和运行

```bash
mvn clean install
```