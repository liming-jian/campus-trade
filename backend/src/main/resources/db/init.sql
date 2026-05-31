-- =============================================
-- 校园二手交易平台 数据库初始化脚本
-- 数据库: campus_trade
-- 适用于 Wamp64 / phpMyAdmin 直接导入
-- =============================================

CREATE DATABASE IF NOT EXISTS campus_trade DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_trade;

-- =============================================
-- 1. 用户表
-- =============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `password` VARCHAR(128) NOT NULL COMMENT '密码(BCrypt)',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像路径',
  `school_name` VARCHAR(100) DEFAULT NULL COMMENT '学校名称',
  `student_id` VARCHAR(50) DEFAULT NULL COMMENT '学号',
  `student_card_img` VARCHAR(255) DEFAULT NULL COMMENT '学生证图片路径',
  `verify_status` TINYINT NOT NULL DEFAULT 0 COMMENT '认证状态: 0未认证 1审核中 2已认证 3已拒绝',
  `verify_reason` VARCHAR(255) DEFAULT NULL COMMENT '认证拒绝原因',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态: 0封禁 1正常',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 2. 管理员表
-- =============================================
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(128) NOT NULL COMMENT '密码(BCrypt)',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '管理员名称',
  `role` VARCHAR(20) NOT NULL DEFAULT 'ADMIN' COMMENT '角色',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- =============================================
-- 3. 商品分类表
-- =============================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标CSS类名',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- =============================================
-- 4. 商品表
--    注意: `condition` 是 MySQL 关键字，必须用反引号包裹
-- =============================================
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `user_id` BIGINT NOT NULL COMMENT '卖家用户ID',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `title` VARCHAR(128) NOT NULL COMMENT '商品标题',
  `description` TEXT COMMENT '商品描述',
  `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  `condition` VARCHAR(20) NOT NULL DEFAULT 'USED' COMMENT '成色: NEW几乎全新 USED使用过 OLD老旧',
  `shipping_free` TINYINT NOT NULL DEFAULT 1 COMMENT '是否包邮: 0不包邮 1包邮',
  `shipping_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '邮费',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏次数',
  `status` VARCHAR(20) NOT NULL DEFAULT 'REVIEW' COMMENT '状态: REVIEW审核中 PASS已通过 REJECT已拒绝 SOLD已售出 OFF下架',
  `ai_audit_result` VARCHAR(20) DEFAULT NULL COMMENT 'AI审核结果: PASS REJECT REVIEW',
  `ai_audit_reason` VARCHAR(255) DEFAULT NULL COMMENT 'AI审核原因',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- =============================================
-- 5. 商品图片表
-- =============================================
DROP TABLE IF EXISTS `product_image`;
CREATE TABLE `product_image` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `url` VARCHAR(255) NOT NULL COMMENT '图片路径',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品图片表';

-- =============================================
-- 6. 收藏表
-- =============================================
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- =============================================
-- 7. 收货地址表
-- =============================================
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人手机号',
  `province` VARCHAR(50) NOT NULL COMMENT '省份',
  `city` VARCHAR(50) NOT NULL COMMENT '城市',
  `district` VARCHAR(50) NOT NULL COMMENT '区/县',
  `detail` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认: 0否 1是',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货地址表';

-- =============================================
-- 8. 订单表
--    注意: order 是 MySQL 关键字，必须用反引号包裹
-- =============================================
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
  `buyer_id` BIGINT NOT NULL COMMENT '买家用户ID',
  `seller_id` BIGINT NOT NULL COMMENT '卖家用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_title` VARCHAR(128) NOT NULL COMMENT '商品标题快照',
  `product_price` DECIMAL(10,2) NOT NULL COMMENT '商品单价',
  `shipping_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '邮费',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '实付总金额',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING_PAY' COMMENT '订单状态: PENDING_PAY待付款 PENDING_SHIP待发货 PENDING_RECEIVE待收货 COMPLETED已完成 CANCELLED已取消',
  `address_snapshot` JSON COMMENT '收货地址JSON快照',
  `paid_at` DATETIME DEFAULT NULL COMMENT '付款时间',
  `shipped_at` DATETIME DEFAULT NULL COMMENT '发货时间',
  `completed_at` DATETIME DEFAULT NULL COMMENT '完成时间',
  `cancelled_at` DATETIME DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- =============================================
-- 9. 会话表
-- =============================================
DROP TABLE IF EXISTS `conversation`;
CREATE TABLE `conversation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user1_id` BIGINT NOT NULL COMMENT '用户1 ID',
  `user2_id` BIGINT NOT NULL COMMENT '用户2 ID',
  `last_message` VARCHAR(500) DEFAULT NULL COMMENT '最后一条消息',
  `last_message_at` DATETIME DEFAULT NULL COMMENT '最后消息时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user1` (`user1_id`),
  KEY `idx_user2` (`user2_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- =============================================
-- 10. 消息表
-- =============================================
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
  `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
  `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
  `content` VARCHAR(2000) NOT NULL COMMENT '消息内容',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_conversation` (`conversation_id`),
  KEY `idx_receiver` (`receiver_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- =============================================
-- 11. 违规记录表
-- =============================================
DROP TABLE IF EXISTS `violation`;
CREATE TABLE `violation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT DEFAULT NULL COMMENT '关联商品ID',
  `type` VARCHAR(50) NOT NULL COMMENT '违规类型',
  `reason` VARCHAR(500) NOT NULL COMMENT '违规原因',
  `action` VARCHAR(50) NOT NULL COMMENT '处理动作: WARN警告 BAN封号 DELIST下架',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='违规记录表';

-- =============================================
-- 12. 警告记录表
-- =============================================
DROP TABLE IF EXISTS `warning`;
CREATE TABLE `warning` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '警告ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(128) NOT NULL COMMENT '警告标题',
  `content` VARCHAR(500) NOT NULL COMMENT '警告内容',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='警告记录表';

-- =============================================
-- 13. 举报记录表
-- =============================================
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `reporter_id` BIGINT NOT NULL COMMENT '举报人ID',
  `target_type` VARCHAR(20) NOT NULL COMMENT '举报目标类型: PRODUCT商品 USER用户',
  `target_id` BIGINT NOT NULL COMMENT '举报目标ID',
  `reason` VARCHAR(500) NOT NULL COMMENT '举报原因',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING待处理 RESOLVED已处理 DISMISSED已驳回',
  `handler_id` BIGINT DEFAULT NULL COMMENT '处理人ID',
  `handle_result` VARCHAR(500) DEFAULT NULL COMMENT '处理结果',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报记录表';

-- =============================================
-- 14. 短信验证码表
-- =============================================
DROP TABLE IF EXISTS `sms_code`;
CREATE TABLE `sms_code` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `code` VARCHAR(10) NOT NULL COMMENT '验证码',
  `type` VARCHAR(20) NOT NULL COMMENT '类型: REGISTER注册 LOGIN登录 RESET_PWD重置密码',
  `used` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已使用',
  `expires_at` DATETIME NOT NULL COMMENT '过期时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_phone_type` (`phone`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信验证码表';

-- =============================================
-- 默认数据
-- =============================================

-- 默认管理员: admin / admin123
INSERT INTO `admin` (`username`, `password`, `nickname`, `role`) VALUES
('admin', '$2a$10$ptFI8HlR1j64xkhbVxNEauNNIGhs2lj3lp8BZ.GrmLuUC7zarX3oe', '超级管理员', 'ADMIN');

-- 默认卖家: 13800000000 / Xy123456! （已认证校园卖家）
INSERT INTO `user` (`phone`, `password`, `nickname`, `school_name`, `verify_status`, `status`) VALUES
('13800000000', '$2a$10$QY/VE8fRPnG6.S6PD4b8wOMsSAWDAFu37MRcm2sG0k/rTWv/QPVCy', '校园卖家', '清华大学', 2, 1);

-- 商品分类
INSERT INTO `category` (`name`, `icon`, `sort_order`) VALUES
('手机数码', 'phone', 1),
('书籍教材', 'book', 2),
('服饰鞋包', 'clothing', 3),
('美妆护肤', 'beauty', 4),
('运动户外', 'sport', 5),
('生活用品', 'daily', 6),
('家电家具', 'home', 7),
('其他', 'other', 8);

-- 示例商品（注意 `condition` 字段用反引号包裹）: user_id 对应 13800000000 用户
-- 这里假设 13800000000 用户的 id = 1（首次插入自增）
SET @seller_id = (SELECT `id` FROM `user` WHERE `phone` = '13800000000');

INSERT INTO `product` (`user_id`, `category_id`, `title`, `description`, `price`, `condition`, `shipping_free`, `shipping_fee`, `view_count`, `favorite_count`, `status`) VALUES
(@seller_id, 1, 'iPhone 13 128G 午夜色', '自用iPhone13，128G版本，无拆无修，一直贴膜带壳使用。边框有轻微使用痕迹，屏幕完美。配件齐全含原装充电线。支持当面验货。', 3799.00, 'NEW', 1, 0.00, 156, 23, 'PASS'),
(@seller_id, 2, '高等数学第七版 同济大学', '考研用书，同济版高数上下册。书内少量笔记，整体8成新。课后习题答案齐全，适合考研复习使用。买就送配套习题全解一本。', 25.00, 'USED', 1, 0.00, 89, 5, 'PASS'),
(@seller_id, 1, 'MacBook Pro 2021 M1 Pro', 'M1 Pro芯片，16G内存，512G存储。95新，电池循环仅80次。原装充电器+包装盒齐全。因换新机出售，可小刀。', 8500.00, 'NEW', 1, 0.00, 203, 31, 'PASS'),
(@seller_id, 7, '宿舍折叠椅 人体工学', '宿舍用人体工学椅，可折叠不占空间。用了半年，腰部支撑很好，久坐不累。马上毕业了带不走，低价转让。', 120.00, 'USED', 1, 0.00, 67, 8, 'PASS'),
(@seller_id, 3, 'Nike Air Force 1 白色 42码', '正品空军一号，穿了两三次，几乎全新。42码偏小半码。买大了所以出，保真正品支持验货。', 399.00, 'NEW', 1, 0.00, 112, 14, 'PASS'),
(@seller_id, 2, '四级词汇闪过 全新未拆封', '买了没看全新未拆的四级词汇书，送真题册。价格就是书费的一半，需要的同学来。还可以顺便分享备考经验哈哈。', 15.00, 'NEW', 1, 0.00, 45, 3, 'PASS'),
(@seller_id, 1, '机械键盘 IKBC C87 青轴', 'ikbc c87机械键盘，樱桃青轴，打字很爽。用了大概一年，键帽轻微打油，功能一切正常。换无线键盘了所以出掉。', 150.00, 'USED', 1, 0.00, 88, 11, 'PASS'),
(@seller_id, 7, '宿舍用小冰箱 迷你款', '宿舍用迷你小冰箱，4L容量放饮料水果够用。功率小不跳闸，制冷效果不错。毕业甩卖，先到先得。', 80.00, 'OLD', 1, 0.00, 134, 19, 'PASS'),
(@seller_id, 4, '兰蔻持妆粉底液 PO-01', '兰蔻持妆粉底液，色号PO-01。只用过三四次，色号不适合我。专柜390买的，便宜出。', 180.00, 'NEW', 1, 0.00, 72, 9, 'PASS'),
(@seller_id, 2, '考研英语一 张剑黄皮书', '全套张剑黄皮书，真题+解析都有。认真做过一遍，笔记工整。附送作文模板和网课讲义，考研党必备。', 35.00, 'USED', 1, 0.00, 56, 7, 'PASS');

-- 商品图片（使用相对路径 /sample-products/，不写死本机路径）
-- 假设上述商品自增 id 从 1 开始
SET @p1 = (SELECT `id` FROM `product` WHERE `title` = 'iPhone 13 128G 午夜色' LIMIT 1);
SET @p2 = (SELECT `id` FROM `product` WHERE `title` = '高等数学第七版 同济大学' LIMIT 1);
SET @p3 = (SELECT `id` FROM `product` WHERE `title` = 'MacBook Pro 2021 M1 Pro' LIMIT 1);
SET @p4 = (SELECT `id` FROM `product` WHERE `title` = '宿舍折叠椅 人体工学' LIMIT 1);
SET @p5 = (SELECT `id` FROM `product` WHERE `title` = 'Nike Air Force 1 白色 42码' LIMIT 1);
SET @p6 = (SELECT `id` FROM `product` WHERE `title` = '四级词汇闪过 全新未拆封' LIMIT 1);
SET @p7 = (SELECT `id` FROM `product` WHERE `title` = '机械键盘 IKBC C87 青轴' LIMIT 1);
SET @p8 = (SELECT `id` FROM `product` WHERE `title` = '宿舍用小冰箱 迷你款' LIMIT 1);
SET @p9 = (SELECT `id` FROM `product` WHERE `title` = '兰蔻持妆粉底液 PO-01' LIMIT 1);
SET @p10 = (SELECT `id` FROM `product` WHERE `title` = '考研英语一 张剑黄皮书' LIMIT 1);

INSERT INTO `product_image` (`product_id`, `url`, `sort_order`) VALUES
(@p1, '/sample-products/iphone-13-midnight.png', 0),
(@p2, '/sample-products/advanced-math-7.png', 0),
(@p3, '/sample-products/macbook-pro-2021.png', 0),
(@p4, '/sample-products/folding-chair.png', 0),
(@p5, '/sample-products/nike-air-force-1.png', 0),
(@p6, '/sample-products/cet4-vocabulary.png', 0),
(@p7, '/sample-products/ikbc-keyboard.png', 0),
(@p8, '/sample-products/mini-fridge.png', 0),
(@p9, '/sample-products/lancome-foundation.png', 0),
(@p10, '/sample-products/kaoyan-english-yellow-book.png', 0);

-- =============================================
-- 导入完成！
-- 默认账号：
--   管理员: admin / admin123
--   校园卖家: 13800000000 / Xy123456!
--   前端访问: http://localhost:5173
--   管理员后台: http://localhost:5173/admin/login
-- =============================================
