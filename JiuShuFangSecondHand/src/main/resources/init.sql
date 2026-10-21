-- ============================================================
-- 旧书坊（JiuShuFangSecondHand）数据库初始化脚本
-- 包含：全部表结构 + 书籍分类基础数据
-- 使用：mysql -uroot -p < init.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS `zhi-yue` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `zhi-yue`;

DROP TABLE IF EXISTS `address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `address` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `receiver` varchar(20) NOT NULL COMMENT '收件人',
  `phone` varchar(11) NOT NULL,
  `province` varchar(20) DEFAULT NULL,
  `city` varchar(20) DEFAULT NULL,
  `district` varchar(20) DEFAULT NULL,
  `detail` varchar(100) NOT NULL COMMENT '详细地址',
  `is_default` tinyint DEFAULT '0' COMMENT '是否默认地址',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2048772039778148354 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户地址表';

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '分类名',
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1113 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='书籍分类';

DROP TABLE IF EXISTS `chat_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_list` (
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `conversation_id` varchar(32) NOT NULL,
  `conversation_name` varchar(255) NOT NULL,
  `create_time` timestamp NOT NULL,
  PRIMARY KEY (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ai聊天列表\r\n';

DROP TABLE IF EXISTS `chat_memory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_memory` (
  `id` bigint NOT NULL,
  `conversation_id` varchar(48) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT (now()),
  `content` text NOT NULL,
  `type` varchar(10) NOT NULL,
  `user_id` bigint NOT NULL COMMENT '用户id',
  `order_id` bigint DEFAULT NULL COMMENT '订单id',
  PRIMARY KEY (`id`),
  KEY `chat_memory_user_id_index` (`user_id`),
  KEY `chat_memory_conversation_id_index` (`conversation_id`),
  KEY `SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TEMESTAMP_INDEX` (`user_id`,`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `from_id` bigint NOT NULL COMMENT '发送者ID',
  `to_id` bigint NOT NULL COMMENT '接收者ID',
  `content` text COMMENT '消息内容',
  `msg_type` varchar(20) DEFAULT 'text' COMMENT 'text/image/order/product',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '0未读 1已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_to_id_is_read` (`to_id`,`is_read`)
) ENGINE=InnoDB AUTO_INCREMENT=594 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `chat_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `buyer_id` bigint NOT NULL COMMENT '买家ID',
  `seller_id` bigint NOT NULL COMMENT '卖家ID',
  `product_id` bigint DEFAULT NULL COMMENT '关联商品ID',
  `order_id` bigint DEFAULT NULL COMMENT '关联订单ID',
  `last_msg` varchar(512) DEFAULT NULL COMMENT '最后一条消息',
  `last_time` datetime DEFAULT NULL COMMENT '最后消息时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_buyer_seller_product` (`buyer_id`,`seller_id`,`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2049088554452832258 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `collect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `collect` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `goods_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收藏表';

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `goods_id` bigint NOT NULL,
  `order_id` bigint NOT NULL COMMENT '订单id',
  `user_id` bigint NOT NULL COMMENT '评论人',
  `content` varchar(500) NOT NULL,
  `picture` varchar(1000) DEFAULT NULL COMMENT '商品评论图',
  `score` tinyint DEFAULT '5' COMMENT '1-5星',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=101891 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品评论表';

DROP TABLE IF EXISTS `goods`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `book_name` varchar(100) NOT NULL COMMENT '书籍名称',
  `author` varchar(50) DEFAULT NULL COMMENT '作者',
  `publisher` varchar(50) DEFAULT NULL COMMENT '出版社',
  `price` decimal(10,2) NOT NULL COMMENT '出售价格',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `stock` int NOT NULL DEFAULT '1' COMMENT '库存数量（二手书默认1）',
  `total_number` int DEFAULT NULL COMMENT '总数量',
  `book_img` varchar(255) NOT NULL COMMENT '书籍封面图片',
  `description` text COMMENT '书籍描述（新旧程度、笔记、破损等）',
  `degree` tinyint NOT NULL DEFAULT '1' COMMENT '成色：1全新 2九成新 3八成新 4七成新及以下',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `user_id` bigint NOT NULL COMMENT '卖家ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '商品状态：1上架 2下架 3已售',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2052736203479486466 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品表（二手书）';

DROP TABLE IF EXISTS `order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '用户ID（买家）',
  `total_price` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `receiver_name` varchar(20) NOT NULL COMMENT '收件人',
  `receiver_phone` varchar(11) NOT NULL COMMENT '收件电话',
  `receiver_address` varchar(255) NOT NULL COMMENT '收货地址',
  `pay_type` int DEFAULT NULL COMMENT '支付类型1 支付宝支付 2余额支付',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '订单状态：1待付款 2待发货 3待收货 4已完成 5已取消 6已评价',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime DEFAULT NULL COMMENT '确认收货时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `cancel_reason` varchar(200) DEFAULT NULL COMMENT '取消原因',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `comment_time` timestamp NULL DEFAULT NULL COMMENT '评论时间',
  `current_comment_num` int DEFAULT '0' COMMENT '当前已经评论数量',
  `max_comment_num` int DEFAULT NULL COMMENT '总共需要评论的数量',
  `address_id` bigint DEFAULT NULL COMMENT '地址id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2053755164635615235 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';

DROP TABLE IF EXISTS `order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `seller_id` bigint NOT NULL COMMENT '卖家id',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `book_name` varchar(100) NOT NULL COMMENT '书籍名称（快照）',
  `book_img` varchar(255) NOT NULL COMMENT '封面图',
  `price` decimal(10,2) NOT NULL COMMENT '单价',
  `num` int NOT NULL COMMENT '数量',
  `total_price` decimal(10,2) NOT NULL COMMENT '小计',
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_comment` tinyint(1) DEFAULT '0' COMMENT '是够已经评价了该商品',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2053755164635615236 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表';

DROP TABLE IF EXISTS `pay_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pay_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` bigint NOT NULL COMMENT '商户订单号（唯一）',
  `out_trade_no` bigint DEFAULT NULL COMMENT '支付宝/微信订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '订单状态 0-待支付 1-已支付 2-已关闭 3-已退款',
  `pay_type` tinyint DEFAULT '1' COMMENT '支付类型 1-支付宝 2-余额',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付订单表';

DROP TABLE IF EXISTS `pay_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pay_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` bigint NOT NULL COMMENT '商户订单号',
  `out_trade_no` varchar(32) DEFAULT NULL COMMENT '支付宝订单号',
  `trade_status` varchar(32) DEFAULT NULL COMMENT '交易状态 TRADE_SUCCESS-支付成功',
  `total_amount` decimal(10,2) DEFAULT NULL COMMENT '支付金额',
  `notify_params` text COMMENT '支付宝回调完整参数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付流水记录表';

DROP TABLE IF EXISTS `pay_refund`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pay_refund` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` bigint NOT NULL COMMENT '订单号',
  `refund_no` bigint NOT NULL COMMENT '退款单号',
  `refund_amount` decimal(10,2) NOT NULL COMMENT '退款金额',
  `status` tinyint DEFAULT '0' COMMENT '0-申请中 1-成功 2-失败',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='退款表';

DROP TABLE IF EXISTS `shopping_cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shopping_cart` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `price` decimal(10,2) NOT NULL COMMENT '商品单价',
  `num` int NOT NULL DEFAULT '1' COMMENT '购买数量',
  `total_price` decimal(10,2) NOT NULL COMMENT '小计总价',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2053654489209419266 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购物车表';

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL COMMENT '用户ID',
  `nick_name` varchar(50) DEFAULT NULL COMMENT '用户名',
  `password` varchar(100) DEFAULT NULL COMMENT '密码',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `create_time` datetime DEFAULT (now()) COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `login_type` tinyint DEFAULT NULL COMMENT '登录类型 1-手机号 2-微信扫码',
  `openid` varchar(100) DEFAULT NULL COMMENT '微信openid',
  `unionid` varchar(100) DEFAULT NULL COMMENT '微信unionid',
  `balance` decimal(10,2) DEFAULT '1000000.00' COMMENT '余额',
  `sex` tinyint DEFAULT NULL COMMENT '性别 0-未知 1-男 2-女',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `role` int DEFAULT '2' COMMENT '角色 1 admin 2 user ',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`) COMMENT 'openid唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- 书籍分类基础数据（AI 购书助手的分类检索依赖此表）
INSERT INTO `category` VALUES (1,'教材教辅',1,'2026-04-09 16:25:44',0),(2,'计算机编程',2,'2026-04-09 16:25:44',0),(3,'文学小说',3,'2026-04-09 16:25:44',0),(4,'历史传记',4,'2026-04-09 16:25:44',0),(5,'考研考公',5,'2026-04-09 16:25:44',0),(6,'英语四六级',6,'2026-04-09 16:25:44',0),(7,'经济管理',7,'2026-04-09 16:25:44',0),(8,'艺术设计',8,'2026-04-09 16:25:44',0),(9,'法律法学',9,'2026-04-09 16:25:44',0),(10,'医学护理',10,'2026-04-09 16:25:44',0),(11,'哲学心理',11,'2026-04-09 16:25:44',0),(12,'其他书籍',12,'2026-04-09 16:25:44',0);


-- ============================================================
-- 秒杀模块（限量书抢购 / 高并发演示）
-- ============================================================

DROP TABLE IF EXISTS `seckill_book`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seckill_book` (
  `id` bigint NOT NULL COMMENT '秒杀活动ID（雪花ID风格）',
  `goods_id` bigint NOT NULL COMMENT '关联商品ID',
  `book_name` varchar(255) NOT NULL COMMENT '书籍名称（快照）',
  `book_img` varchar(500) DEFAULT NULL COMMENT '封面图（快照）',
  `price` decimal(10,2) NOT NULL COMMENT '秒杀价',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `initial_stock` int NOT NULL COMMENT '初始库存（压测复位用）',
  `seckill_stock` int NOT NULL COMMENT '当前剩余库存（DB 侧权威值）',
  `start_time` datetime NOT NULL COMMENT '开抢时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1进行中 2已结束',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  PRIMARY KEY (`id`),
  KEY `idx_status_time` (`status`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='秒杀书籍表';

DROP TABLE IF EXISTS `seckill_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seckill_order` (
  `id` bigint NOT NULL COMMENT '秒杀订单ID',
  `seckill_id` bigint NOT NULL COMMENT '秒杀活动ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `user_id` bigint NOT NULL COMMENT '抢购用户ID',
  `book_name` varchar(255) NOT NULL COMMENT '书籍名称（快照）',
  `price` decimal(10,2) NOT NULL COMMENT '成交价',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1已抢到待付款',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_seckill` (`user_id`, `seckill_id`) COMMENT '一人一单的数据库侧最终保证',
  KEY `idx_seckill_id` (`seckill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='秒杀订单表';

-- 演示秒杀数据：库存 100，关联一条真实上架商品（取不到则本行为空，可手动插入）
INSERT INTO `seckill_book` (id, goods_id, book_name, book_img, price, original_price,
       initial_stock, seckill_stock, start_time, end_time, status)
SELECT 9000000000000000001, id, book_name, book_img,
       ROUND(price * 0.5, 2), price, 100, 100, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 365 DAY, 1
FROM `goods` WHERE status = 1 ORDER BY id LIMIT 1;

