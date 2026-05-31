# 校园闲置（二手）交易平台（Spring Boot + Vue3）

## 项目简介
本项目是一个面向校园场景的二手/闲置交易平台，提供用户端商品浏览、发布、收藏、下单、聊天等能力，并提供管理员后台用于用户治理、商品审核、订单/举报管理等。

- 后端：Spring Boot 3.2.5 + Spring Security + JWT + MyBatis-Plus + WebSocket
- 前端：Vue 3 + Vite 5 + Vue Router + Pinia + Element Plus
- 数据库：MySQL（默认库名：`campus_trade`）

仓库为前后端分离结构：
- `backend/`：后端服务
- `frontend/`：前端项目

## 主要功能

### 用户端
- 注册 / 登录（JWT）
- 短信验证码（接口已预留：`/api/auth/send-code`）
- 个人资料：查看当前用户、修改昵称、上传头像
- 学生认证：上传学生证照片 + 学校/学号信息
- 商品：
  - 首页商品分页列表、分类筛选、关键字搜索、排序（后端支持：`/api/products`）
  - 商品详情（`/api/products/{id}`）
  - 发布商品（支持多图上传）
  - 我的发布列表
  - 举报商品、查看我的举报
- 收藏：收藏/取消收藏、收藏列表
- 订单：创建订单、订单列表、订单详情、支付/发货/确认收货/取消
- 私信聊天：
  - 会话列表、消息列表
  - 发送消息
  - 已读回执（WebSocket 推送）

### 管理端
- 管理员登录
- 仪表盘统计：`GET /api/admin/dashboard`
- 用户管理：查询、封禁/解封、警告、学生认证审核
- 商品审核：按状态筛选、审核通过/驳回（包含理由）
- 订单管理：按状态/关键字查询
- 举报管理：列表、处理（动作 + 处理结果）
- 分类管理（接口存在：`/api/categories`，注意鉴权策略见后端安全配置）

## 技术栈与关键配置

### 后端（backend）
- Java 17
- Spring Boot 3.2.5
- spring-boot-starter-web / security / websocket / validation
- MyBatis-Plus：3.5.6
- JWT：jjwt 0.12.5

#### 后端端口
- 默认端口：`8080`
- 配置文件：`backend/src/main/resources/application.yml`

#### 数据库配置（application.yml）
- 数据库：MySQL
- 默认连接：
  - `jdbc:mysql://localhost:3306/campus_trade...`
  - 用户名：`root`
  - 密码：空（你需要自行填写）

#### 文件上传
- 上传目录：`uploads/`
- 访问：后端已放行 `/uploads/**`

#### 鉴权与权限
- JWT 无状态（Stateless Session）
- 关键放行：
  - `/api/auth/register`、`/api/auth/login`、`/api/auth/send-code`、`/api/auth/reset-password`
  - `/api/admin/login`
  - `GET /api/products`、`GET /api/products/*`
  - `GET /api/categories`、`GET /api/categories/*`
  - `/uploads/**`、`/ws/**`

### 前端（frontend）
- Vue 3 + Vite
- Element Plus UI
- Pinia 状态管理

#### 前端端口与代理
- dev server：`5173`
- 代理：见 `frontend/vite.config.js`
  - `/api -> http://localhost:8080`
  - `/ws -> ws://localhost:8080`
  - `/uploads -> http://localhost:8080`

## 目录结构（完整版）
```
.
├─ .agents/
│  └─ skills/
│     └─ design-taste-frontend/
│        └─ SKILL.md
├─ backend/
│  ├─ campus-trade-backend.iml
│  ├─ pom.xml
│  └─ src/
│     ├─ main/
│     │  ├─ java/
│     │  │  └─ com/
│     │  │     └─ campus_trade/
│     │  │        ├─ CampusTradeApplication.java
│     │  │        ├─ common/
│     │  │        │  ├─ FileUploadUtils.java
│     │  │        │  ├─ GlobalExceptionHandler.java
│     │  │        │  └─ Result.java
│     │  │        ├─ config/
│     │  │        │  ├─ CorsConfig.java
│     │  │        │  ├─ DataInitializer.java
│     │  │        │  ├─ JwtAuthFilter.java
│     │  │        │  ├─ JwtUtils.java
│     │  │        │  ├─ MyMetaObjectHandler.java
│     │  │        │  ├─ MybatisPlusConfig.java
│     │  │        │  ├─ SecurityConfig.java
│     │  │        │  └─ WebMvcConfig.java
│     │  │        ├─ controller/
│     │  │        │  ├─ AddressController.java
│     │  │        │  ├─ AdminController.java
│     │  │        │  ├─ AdminManagementController.java
│     │  │        │  ├─ AuthController.java
│     │  │        │  ├─ CategoryController.java
│     │  │        │  ├─ ChatController.java
│     │  │        │  ├─ FavoriteController.java
│     │  │        │  ├─ OrderController.java
│     │  │        │  └─ ProductController.java
│     │  │        ├─ dto/
│     │  │        │  ├─ AddressDTO.java
│     │  │        │  ├─ AddressRequest.java
│     │  │        │  ├─ AdminLoginRequest.java
│     │  │        │  ├─ ConversationDTO.java
│     │  │        │  ├─ LoginRequest.java
│     │  │        │  ├─ MessageDTO.java
│     │  │        │  ├─ OrderDTO.java
│     │  │        │  ├─ OrderRequest.java
│     │  │        │  ├─ ProductDTO.java
│     │  │        │  ├─ ProductRequest.java
│     │  │        │  ├─ RegisterRequest.java
│     │  │        │  ├─ ResetPwdRequest.java
│     │  │        │  ├─ SendCodeRequest.java
│     │  │        │  ├─ UserDTO.java
│     │  │        │  ├─ VerifyRequest.java
│     │  │        │  └─ (其他DTO同目录)
│     │  │        ├─ entity/
│     │  │        │  ├─ Address.java
│     │  │        │  ├─ Admin.java
│     │  │        │  ├─ Category.java
│     │  │        │  ├─ Conversation.java
│     │  │        │  ├─ Favorite.java
│     │  │        │  ├─ Message.java
│     │  │        │  ├─ Order.java
│     │  │        │  ├─ Product.java
│     │  │        │  ├─ ProductImage.java
│     │  │        │  ├─ Report.java
│     │  │        │  ├─ SmsCode.java
│     │  │        │  ├─ User.java
│     │  │        │  ├─ Violation.java
│     │  │        │  └─ Warning.java
│     │  │        ├─ mapper/
│     │  │        │  ├─ AddressMapper.java
│     │  │        │  ├─ AdminMapper.java
│     │  │        │  ├─ CategoryMapper.java
│     │  │        │  ├─ ConversationMapper.java
│     │  │        │  ├─ FavoriteMapper.java
│     │  │        │  ├─ MessageMapper.java
│     │  │        │  ├─ OrderMapper.java
│     │  │        │  ├─ ProductImageMapper.java
│     │  │        │  ├─ ProductMapper.java
│     │  │        │  ├─ ReportMapper.java
│     │  │        │  ├─ SmsCodeMapper.java
│     │  │        │  ├─ UserMapper.java
│     │  │        │  ├─ ViolationMapper.java
│     │  │        │  └─ WarningMapper.java
│     │  │        ├─ service/
│     │  │        │  ├─ AddressService.java
│     │  │        │  ├─ AdminManagementService.java
│     │  │        │  ├─ AdminService.java
│     │  │        │  ├─ AiAuditService.java
│     │  │        │  ├─ CategoryService.java
│     │  │        │  ├─ ChatService.java
│     │  │        │  ├─ FavoriteService.java
│     │  │        │  ├─ OrderService.java
│     │  │        │  ├─ ProductService.java
│     │  │        │  ├─ SmsService.java
│     │  │        │  └─ UserService.java
│     │  │        └─ websocket/
│     │  │           ├─ ChatWebSocketHandler.java
│     │  │           ├─ WebSocketConfig.java
│     │  │           └─ WebSocketHandshakeInterceptor.java
│     │  └─ resources/
│     │     ├─ application.yml
│     │     └─ db/
│     │        └─ init.sql
│     └─ test/
│        └─ java/
│           └─ com/
│              └─ campus_trade/
│                 ├─ service/
│                 │  ├─ ChatServiceTest.java
│                 │  └─ ProductServiceTest.java
│                 └─ websocket/
│                    └─ ChatWebSocketHandlerTest.java
├─ frontend/
│  ├─ index.html
│  ├─ package.json
│  ├─ package-lock.json
│  ├─ vite.config.js
│  ├─ public/
│  │  ├─ favicon.svg
│  │  └─ sample-products/
│  │     ├─ advanced-math-7.png
│  │     ├─ cet4-vocabulary.png
│  │     ├─ folding-chair.png
│  │     ├─ ikbc-keyboard.png
│  │     ├─ iphone-13-midnight.png
│  │     ├─ kaoyan-english-yellow-book.png
│  │     ├─ lancome-foundation.png
│  │     ├─ macbook-pro-2021.png
│  │     ├─ mini-fridge.png
│  │     └─ nike-air-force-1.png
│  └─ src/
│     ├─ App.vue
│     ├─ main.js
│     ├─ api/
│     │  ├─ address.js
│     │  ├─ admin.js
│     │  ├─ auth.js
│     │  ├─ chat.js
│     │  ├─ favorite.js
│     │  ├─ order.js
│     │  ├─ product.js
│     │  └─ request.js
│     ├─ assets/
│     │  └─ global.css
│     ├─ components/
│     │  ├─ GlobalNotification.vue
│     │  ├─ NavBar.vue
│     │  └─ TabBar.vue
│     ├─ router/
│     │  └─ index.js
│     ├─ stores/
│     │  ├─ chat.js
│     │  ├─ notification.js
│     │  └─ user.js
│     ├─ utils/
│     │  └─ chinaRegions.js
│     └─ views/
│        ├─ admin/
│        │  ├─ AdminCategoriesView.vue
│        │  ├─ AdminDashboardView.vue
│        │  ├─ AdminLoginView.vue
│        │  ├─ AdminOrdersView.vue
│        │  ├─ AdminProductsView.vue
│        │  ├─ AdminReportsView.vue
│        │  ├─ AdminSidebar.vue
│        │  └─ AdminUsersView.vue
│        └─ user/
│           ├─ AddressView.vue
│           ├─ ChatView.vue
│           ├─ ForgotPasswordView.vue
│           ├─ HomeView.vue
│           ├─ LoginView.vue
│           ├─ MyProductsView.vue
│           ├─ MyReportsView.vue
│           ├─ OrderConfirmView.vue
│           ├─ OrderListView.vue
│           ├─ ProductDetailView.vue
│           ├─ ProfileView.vue
│           ├─ PublishView.vue
│           ├─ RegisterView.vue
│           ├─ VerifyView.vue
├─ CHANGELOG.md
├─ README.md
├─ RELEASE_NOTES.md
├─ campus-trade-frontend-dist.zip
└─ skills-lock.json
```

## 数据表（根据实体类推断）
- `user`：用户
- `admin`：管理员
- `product`：商品
- `product_image`：商品图片
- `category`：分类
- `favorite`：收藏
- `order`：订单（注意为保留字，实体映射为 `@TableName("`order`")`）
- `address`：收货地址
- `conversation`：会话
- `message`：聊天消息
- `report`：举报
- `sms_code`：短信验证码记录
- `violation` / `warning`：违规/警告

> 具体字段以 `backend/src/main/java/com/campus_trade/entity/` 下实体为准。

## 接口概览（按模块）

### 认证与用户（/api/auth）
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/send-code`
- `GET /api/auth/me`
- `PUT /api/auth/profile`
- `PUT /api/auth/avatar`（multipart）
- `POST /api/auth/verify`（multipart）
- `POST /api/auth/reset-password`

### 商品（/api/products）
- `GET /api/products`（分页、分类、关键词、排序）
- `GET /api/products/{id}`
- `POST /api/products`（multipart：商品信息 + 图片数组）
- `PUT /api/products/{id}`
- `DELETE /api/products/{id}`
- `GET /api/products/my`
- `GET /api/products/reports/my`
- `POST /api/products/{id}/report`

### 分类（/api/categories）
- `GET /api/categories`
- `GET /api/categories/{id}`
- `POST /api/categories`
- `PUT /api/categories/{id}`
- `DELETE /api/categories/{id}`

### 收藏（/api/favorites）
- `POST /api/favorites/{productId}`（切换收藏状态）
- `GET /api/favorites`

### 地址（/api/addresses）
- `GET /api/addresses`
- `GET /api/addresses/{id}`
- `POST /api/addresses`
- `PUT /api/addresses/{id}`
- `DELETE /api/addresses/{id}`
- `PUT /api/addresses/{id}/default`

### 订单（/api/orders）
- `POST /api/orders`
- `GET /api/orders`（role/status/page/size）
- `GET /api/orders/{id}`
- `PUT /api/orders/{id}/pay`
- `PUT /api/orders/{id}/ship`
- `PUT /api/orders/{id}/confirm`
- `PUT /api/orders/{id}/cancel`

### 聊天（/api）
- `GET /api/conversations`
- `POST /api/conversations`
- `GET /api/conversations/{id}/messages`
- `PUT /api/conversations/{id}/read`
- `POST /api/messages`

### 管理员（/api/admin）
- `POST /api/admin/login`
- `GET /api/admin/dashboard`
- `GET /api/admin/users`
- `POST /api/admin/users/{id}/ban`
- `POST /api/admin/users/{id}/unban`
- `POST /api/admin/users/{id}/warn`
- `GET /api/admin/users/{id}/warnings`
- `POST /api/admin/users/{id}/verify`
- `GET /api/admin/products`
- `POST /api/admin/products/{id}/audit`
- `GET /api/admin/orders`
- `GET /api/admin/reports`
- `POST /api/admin/reports/{id}/handle`

## 本地运行

### 1）准备数据库
1. 安装并启动 MySQL
2. 创建数据库：`campus_trade`
3. 将 `backend/src/main/resources/application.yml` 中的 `spring.datasource.password` 填成你的本机密码
4. 创建表结构：
   - 如果你有 SQL 脚本，请导入
   - 如果没有脚本，需要你补充（仅从实体类无法100%还原索引/约束）

### 2）启动后端
```bash
cd backend
mvn -q -DskipTests spring-boot:run
```
后端地址：`http://localhost:8080`

### 3）启动前端
```bash
cd frontend
npm install
npm run dev
```
前端地址：`http://localhost:5173`

## 构建与发布

### 后端打包
```bash
cd backend
mvn -q -DskipTests package
```
输出：`backend/target/*.jar`

### 前端打包
```bash
cd frontend
npm install
npm run build
```
输出：`frontend/dist/`

## 常见问题
- 访问数据库失败：检查 MySQL 账号密码、数据库是否存在、端口是否为3306。
- 登录后仍提示未登录：确认前端本地 `localStorage.token` 是否写入、后端 JWT secret 是否一致。
- 图片无法访问：确认后端运行目录下存在 `uploads/`，并且前端请求走 `/uploads/...` 代理。

## License
如无特殊说明，默认仅用于学习与课程设计用途；如需开源协议请自行补充。
