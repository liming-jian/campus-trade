# Release Notes - v2026.05.31

## 亮点
- 前后端分离：Spring Boot 3 + Vue3（Vite）
- JWT 无状态鉴权，接口按角色区分 USER / ADMIN
- WebSocket 私信聊天：会话、消息、已读回执推送
- 管理后台：用户管理、学生认证审核、商品审核、举报处理

## 变更列表
- 后端：
  - 新增认证与用户接口（注册/登录/资料/头像/认证/重置密码）
  - 新增商品、收藏、订单、地址、聊天相关接口
  - 新增管理端接口（dashboard/users/products/orders/reports）
  - MyBatis-Plus + MySQL 数据访问
- 前端：
  - 用户端页面：首页、登录/注册、商品详情、发布、订单、地址、聊天、个人中心等
  - 管理端页面：登录、仪表盘、用户/商品/订单/举报/分类管理

## 运行要求
- JDK 17
- Maven 3.8+
- Node.js 18+（建议 18/20 LTS）
- MySQL 8

## 已知事项
- 数据库建表SQL未随仓库提供时，需要自行补充建表脚本（仅实体类无法完全还原约束/索引）。

## 下载
- `campus-trade-backend-*.jar`：后端可执行包
- `campus-trade-frontend-dist.zip`：前端构建产物
