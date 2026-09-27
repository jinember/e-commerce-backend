mysqldump: [Warning] Using a password on the command line interface can be insecure.
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_ad` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `position` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `link` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `img_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '广告图片',
  `start_time` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '投放开始时间',
  `end_time` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '投放结束时间',
  `sort` int DEFAULT '1',
  `status` int DEFAULT '1',
  `click_count` int DEFAULT '0' COMMENT '点击量',
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_address` (
  `id` int NOT NULL AUTO_INCREMENT,
  `member_id` int DEFAULT NULL COMMENT '会员ID',
  `receiver` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '收货人',
  `phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '手机号',
  `province` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '省',
  `city` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '市',
  `district` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '区/县',
  `detail` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '详细地址',
  `is_default` int DEFAULT '0' COMMENT '是否默认 1是 0否',
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='收货地址表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_brand` (
  `id` int NOT NULL AUTO_INCREMENT,
  `brand_name` varchar(32) DEFAULT NULL,
  `logo_name` varchar(64) DEFAULT NULL,
  `brand_desc` varchar(512) DEFAULT NULL,
  `show_status` int DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_brand_category` (
  `brand_id` int DEFAULT NULL,
  `brand_name` varchar(32) DEFAULT NULL,
  `category_id` int DEFAULT NULL,
  `category_name` varchar(32) DEFAULT NULL,
  UNIQUE KEY `uni_brand_category` (`brand_id`,`category_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_browse_history` (
  `id` int NOT NULL AUTO_INCREMENT,
  `member_id` int DEFAULT NULL COMMENT '会员ID',
  `spu_id` int DEFAULT NULL COMMENT '商品SPU ID',
  `browse_time` datetime DEFAULT NULL COMMENT '浏览时间',
  `stay_duration` int DEFAULT '0' COMMENT '停留时长(秒)',
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=58 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='浏览历史表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_cart` (
  `id` int NOT NULL AUTO_INCREMENT,
  `member_id` int DEFAULT NULL,
  `spu_id` int DEFAULT NULL,
  `sku_id` int DEFAULT NULL,
  `quantity` int DEFAULT '1',
  `selected` int DEFAULT '1',
  `add_time` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_chat_session` (
  `id` int NOT NULL AUTO_INCREMENT,
  `chat_no` varchar(32) DEFAULT NULL,
  `member_id` int DEFAULT NULL,
  `spu_id` int DEFAULT NULL,
  `category_id` int DEFAULT NULL,
  `session_id` varchar(50) DEFAULT NULL,
  `chat_type` varchar(20) DEFAULT NULL,
  `user_msg_count` int DEFAULT '0',
  `merchant_reply_count` int DEFAULT '0',
  `first_response_sec` int DEFAULT '0',
  `session_duration_sec` int DEFAULT '0',
  `is_converted` int DEFAULT '0',
  `convert_action` varchar(20) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_chat_member` (`member_id`),
  KEY `idx_chat_type` (`chat_type`)
) ENGINE=InnoDB AUTO_INCREMENT=15151 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_comment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int DEFAULT NULL,
  `spu_id` int DEFAULT NULL,
  `member_id` int DEFAULT NULL,
  `content` text COLLATE utf8mb4_general_ci,
  `rating` int DEFAULT '5',
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20222 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_coupon` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `type` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `value` decimal(10,2) DEFAULT NULL,
  `min_amount` decimal(10,2) DEFAULT NULL,
  `total_count` int DEFAULT NULL,
  `used_count` int DEFAULT '0',
  `start_time` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `end_time` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status` int DEFAULT '1',
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_coupon_record` (
  `id` int NOT NULL AUTO_INCREMENT,
  `member_id` int DEFAULT NULL COMMENT '会员ID',
  `coupon_id` int DEFAULT NULL COMMENT '优惠券ID',
  `status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'unused' COMMENT '状态: unused/used/expired',
  `receive_time` datetime DEFAULT NULL COMMENT '领取时间',
  `use_time` datetime DEFAULT NULL COMMENT '使用时间',
  `order_id` int DEFAULT NULL COMMENT '使用的订单ID',
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='优惠券领取记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_dept` (
  `id` int NOT NULL AUTO_INCREMENT,
  `dept_name` varchar(64) DEFAULT NULL,
  `dept_desc` varchar(64) DEFAULT NULL,
  `parent_id` int DEFAULT NULL,
  `p_ids` varchar(128) DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  `menu_perms` varchar(255) DEFAULT NULL COMMENT '部门允许查看的模块，逗号分隔',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=107 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_dim_date` (
  `date_id` int NOT NULL COMMENT '日期ID: 20260919',
  `date` date DEFAULT NULL COMMENT '日期',
  `year` int DEFAULT NULL COMMENT '年',
  `quarter` int DEFAULT NULL COMMENT '季度',
  `month` int DEFAULT NULL COMMENT '月',
  `week` int DEFAULT NULL COMMENT '周',
  `day` int DEFAULT NULL COMMENT '日',
  `weekday` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '星期几',
  `is_weekend` int DEFAULT NULL COMMENT '是否周末',
  `is_holiday` int DEFAULT NULL COMMENT '是否节假日',
  PRIMARY KEY (`date_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='时间维度表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_favorite` (
  `id` int NOT NULL AUTO_INCREMENT,
  `member_id` int DEFAULT NULL,
  `spu_id` int DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_goods_attr` (
  `id` int NOT NULL AUTO_INCREMENT,
  `attr_name` varchar(32) DEFAULT NULL,
  `category_id` int DEFAULT NULL,
  `attr_type` int DEFAULT NULL,
  `value_type` int DEFAULT NULL,
  `attr_value` varchar(255) DEFAULT NULL,
  `search_enable` int DEFAULT NULL,
  `enable` int DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=99 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_goods_attr_group` (
  `id` int NOT NULL AUTO_INCREMENT,
  `group_name` varchar(32) DEFAULT NULL,
  `category_id` int DEFAULT NULL,
  `descript` varchar(255) DEFAULT NULL,
  `icons` varchar(255) DEFAULT NULL,
  `sort` int DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=94 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_goods_attr_value` (
  `id` int NOT NULL AUTO_INCREMENT,
  `spu_id` int DEFAULT NULL,
  `attr_id` int DEFAULT NULL,
  `value_type` int DEFAULT NULL,
  `attr_value` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_goods_category` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '类别编号',
  `category_name` varchar(32) DEFAULT NULL COMMENT '类别名称',
  `parent_id` int unsigned DEFAULT NULL COMMENT '父类别编号',
  `p_ids` varchar(64) DEFAULT NULL,
  `show_status` tinyint unsigned DEFAULT '1' COMMENT '状态：1上架 2下架',
  `sort` int unsigned DEFAULT NULL COMMENT '优先级',
  `icons` varchar(32) DEFAULT NULL,
  `product_unit` varchar(32) DEFAULT NULL,
  `product_count` int DEFAULT NULL,
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `pid` (`parent_id`) USING BTREE,
  CONSTRAINT `tbl_goods_category_ibfk_1` FOREIGN KEY (`parent_id`) REFERENCES `tbl_goods_category` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='商品类别表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_goods_draft` (
  `id` int NOT NULL AUTO_INCREMENT,
  `draft_name` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '草稿名称',
  `spu_form` text COLLATE utf8mb4_general_ci COMMENT '表单JSON数据',
  `pub_key` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '发布缓存钥匙',
  `step` int DEFAULT '1' COMMENT '保存时所在步骤1-4',
  `status` tinyint DEFAULT '0' COMMENT '0草稿 1定时待发布 2已发布',
  `publish_time` datetime DEFAULT NULL COMMENT '定时发布时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_group_attr_relation` (
  `id` int NOT NULL AUTO_INCREMENT,
  `attr_group_id` int DEFAULT NULL,
  `attr_id` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=82 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_invoice` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int DEFAULT NULL COMMENT '订单ID',
  `member_id` int DEFAULT NULL COMMENT '会员ID',
  `type` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '类型: personal/company',
  `title` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '发票抬头',
  `tax_no` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '税号',
  `amount` double DEFAULT NULL COMMENT '开票金额',
  `status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'pending' COMMENT '状态: pending/issued',
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='发票管理表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_logistics` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int DEFAULT NULL,
  `company` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `tracking_no` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `ship_time` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `receive_time` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status` int DEFAULT '0',
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_member` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nickname` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `gender` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '性别',
  `age` int DEFAULT NULL COMMENT '年龄',
  `city` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '城市',
  `channel` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '注册渠道',
  `phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `level` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `source` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'APP',
  `points` int DEFAULT '0',
  `balance` decimal(10,2) DEFAULT '0.00',
  `avatar` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '',
  `status` int DEFAULT '1',
  `register_time` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '123456' COMMENT '登录密码(默认123456)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2022 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_message` (
  `id` int NOT NULL AUTO_INCREMENT,
  `member_id` int DEFAULT NULL COMMENT '会员ID',
  `title` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '消息标题',
  `content` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '消息内容',
  `type` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '类型: order/logistics/promotion/system',
  `is_read` int DEFAULT '0' COMMENT '是否已读 1已读 0未读',
  `send_time` datetime DEFAULT NULL COMMENT '发送时间',
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='站内消息表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_order_info` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单编号',
  `user_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '下单用户',
  `member_id` int DEFAULT NULL COMMENT '会员ID',
  `goods_id` int DEFAULT NULL COMMENT '商品SPU-ID',
  `goods_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品名称',
  `sku_id` int DEFAULT NULL COMMENT 'SKU-ID',
  `sku_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'SKU名称',
  `price` decimal(10,2) DEFAULT NULL COMMENT '成交单价',
  `count` int DEFAULT NULL COMMENT '购买数量',
  `total_price` decimal(10,2) DEFAULT NULL COMMENT '订单总价',
  `status` int DEFAULT '0' COMMENT '0待支付/1已支付/2已发货/3已完成/4已取消',
  `receiver` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '收货人',
  `phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系电话',
  `member_level` varchar(20) COLLATE utf8mb4_general_ci DEFAULT '普通' COMMENT '会员等级',
  `address` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '收货地址',
  `remark` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `create_date` datetime DEFAULT NULL COMMENT '下单时间',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30450 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_order_op_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int DEFAULT NULL COMMENT '订单id',
  `operator` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人',
  `op_type` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作类型',
  `from_status` int DEFAULT NULL COMMENT '变更前状态',
  `to_status` int DEFAULT NULL COMMENT '变更后状态',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注/说明',
  `create_time` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单操作日志';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_payment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int DEFAULT NULL,
  `pay_type` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `pay_amount` decimal(10,2) DEFAULT NULL,
  `pay_time` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `pay_status` int DEFAULT '0',
  `transaction_no` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_permission` (
  `id` int NOT NULL AUTO_INCREMENT,
  `permission` varchar(32) DEFAULT NULL,
  `path` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_point_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `member_id` int DEFAULT NULL,
  `order_id` int DEFAULT NULL COMMENT '关联订单ID',
  `change_type` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `points` int DEFAULT NULL,
  `balance` int DEFAULT NULL,
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_promotion` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `type` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `discount` decimal(5,2) DEFAULT NULL,
  `start_time` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `end_time` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status` int DEFAULT '1',
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_refund` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int DEFAULT NULL,
  `reason` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `amount` decimal(10,2) DEFAULT NULL,
  `status` int DEFAULT '0',
  `apply_time` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `handle_time` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_role` (
  `id` int NOT NULL AUTO_INCREMENT,
  `role_name` varchar(64) DEFAULT NULL,
  `descript` varchar(64) DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  `menu_perms` varchar(255) DEFAULT NULL COMMENT '可访问业务板块(逗号分隔)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_role_permission` (
  `id` int NOT NULL AUTO_INCREMENT,
  `role_id` int DEFAULT NULL,
  `permission_id` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_sale_strategy` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '策略ID',
  `strategy_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '策略名称',
  `strategy_type` int DEFAULT '1' COMMENT '1折扣/2满减/3会员价',
  `strategy_value` decimal(10,2) DEFAULT NULL COMMENT '策略值(折扣率/满减金额/会员折扣)',
  `threshold_value` decimal(10,2) DEFAULT NULL COMMENT '满减门槛(满XX元)',
  `status` int DEFAULT '1' COMMENT '0停用/1启用',
  `remark` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '策略说明',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='销售策略表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_sign_in` (
  `id` int NOT NULL AUTO_INCREMENT,
  `member_id` int DEFAULT NULL COMMENT '会员ID',
  `sign_date` date DEFAULT NULL COMMENT '签到日期',
  `continuous_days` int DEFAULT '1' COMMENT '连续签到天数',
  `points` int DEFAULT '5' COMMENT '获得积分',
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='签到打卡表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_sign_rule` (
  `id` int NOT NULL AUTO_INCREMENT,
  `base_points` int DEFAULT '5',
  `continuous_bonus` int DEFAULT '2',
  `max_points` int DEFAULT '20',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_sku_album` (
  `id` int NOT NULL AUTO_INCREMENT,
  `goods_id` int DEFAULT NULL,
  `sku_id` int DEFAULT NULL,
  `images` varchar(500) DEFAULT NULL,
  `default_image` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_sku_info` (
  `sku_id` int NOT NULL AUTO_INCREMENT,
  `spu_id` int DEFAULT NULL,
  `sku_name` varchar(128) DEFAULT NULL,
  `sku_desc` varchar(255) DEFAULT NULL,
  `category_id` int DEFAULT NULL,
  `brand_id` int DEFAULT NULL,
  `sku_title` varchar(128) DEFAULT NULL,
  `sku_subtitle` varchar(128) DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `sale_count` int DEFAULT '0',
  PRIMARY KEY (`sku_id`)
) ENGINE=InnoDB AUTO_INCREMENT=117 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_sku_sale_attr_value` (
  `id` int NOT NULL AUTO_INCREMENT,
  `spu_id` int DEFAULT NULL,
  `sku_id` int DEFAULT NULL,
  `attr_id` int DEFAULT NULL,
  `attr_value` varchar(32) DEFAULT NULL,
  `attr_name` varchar(64) DEFAULT NULL,
  `attr_sort` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_spu_detail` (
  `id` int NOT NULL AUTO_INCREMENT,
  `goods_name` varchar(64) DEFAULT NULL,
  `goods_details` varchar(64) DEFAULT NULL,
  `category_id` int DEFAULT NULL,
  `brand_id` int DEFAULT NULL,
  `weight` float DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  `main_image` varchar(64) DEFAULT NULL,
  `status` int DEFAULT '0',
  `view_count` int DEFAULT '0' COMMENT '浏览量',
  `comment_count` int DEFAULT '0' COMMENT '评价数',
  `good_rate` double DEFAULT '5' COMMENT '好评率',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_spu_goods_attr` (
  `id` int NOT NULL AUTO_INCREMENT,
  `spu_id` int DEFAULT NULL,
  `attr_id` int DEFAULT NULL,
  `attr_name` varchar(64) DEFAULT NULL,
  `attr_value` varchar(255) DEFAULT NULL,
  `attr_sort` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_stock` (
  `id` int NOT NULL AUTO_INCREMENT,
  `goods_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sku_spec` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `warehouse` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `stock` int DEFAULT '0',
  `safe_stock` int DEFAULT '0',
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  `sku_id` int DEFAULT NULL COMMENT '关联SKU',
  `supplier_id` int DEFAULT NULL COMMENT '主供应商ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=200 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_stock_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `stock_id` int DEFAULT NULL,
  `change_type` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `operator` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  `supplier_id` int DEFAULT NULL COMMENT '本次出入库供应商ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_supplier` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `contact` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `address` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status` int DEFAULT '1',
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_ticket` (
  `id` int NOT NULL AUTO_INCREMENT,
  `member_id` int DEFAULT NULL COMMENT '会员ID',
  `order_id` int DEFAULT NULL COMMENT '关联订单ID',
  `title` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '工单标题',
  `content` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '问题描述',
  `type` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '类型: consult/afterSale/complaint',
  `status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'pending' COMMENT '状态: pending/processing/resolved',
  `reply` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '客服回复',
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='客服工单表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `account` varchar(32) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `nick_name` varchar(32) DEFAULT NULL,
  `phone` varchar(32) DEFAULT NULL,
  `sex` enum('男','女') DEFAULT NULL,
  `email` varchar(32) DEFAULT NULL,
  `no` varchar(32) DEFAULT NULL,
  `dept_id` int DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_user_behavior` (
  `id` int NOT NULL AUTO_INCREMENT,
  `member_id` int DEFAULT NULL,
  `session_id` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '会话ID',
  `spu_id` int DEFAULT NULL,
  `behavior_type` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `search_keyword` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `page_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `stay_time` int DEFAULT NULL,
  `device` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=151629 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_user_role` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `role_id` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=80 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

