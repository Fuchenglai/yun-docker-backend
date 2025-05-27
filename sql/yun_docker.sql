/*
 Navicat Premium Data Transfer

 Source Server         : aliyun_nbuntu
 Source Server Type    : MySQL
 Source Server Version : 80041
 Source Host           : 114.215.191.146:3306
 Source Schema         : yun_docker

 Target Server Type    : MySQL
 Target Server Version : 80041
 File Encoding         : 65001

 Date: 20/04/2025 13:45:58
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号',
  `user_password` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `user_avatar` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户头像url',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户手机号',
  `balance` int NOT NULL DEFAULT 1000 COMMENT '余额',
  `user_role` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin/ban',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'zangsan', '1442de8d3dfff7f35f2a5f7c108b02b6', '活泼麦穗', NULL, NULL, 1000, 'user', '2025-04-07 20:11:58', '2025-04-07 20:11:58', 0);
INSERT INTO `user` VALUES (2, 'laifucheng', '1442de8d3dfff7f35f2a5f7c108b02b6', '优雅鲸鱼', NULL, NULL, 74117, 'user', '2025-04-07 20:12:20', '2025-04-18 00:10:45', 0);
INSERT INTO `user` VALUES (3, 'wangwu', '1442de8d3dfff7f35f2a5f7c108b02b6', '坚韧萤火虫', NULL, NULL, 1000, 'user', '2025-04-11 21:14:09', '2025-04-11 21:14:09', 0);

-- ----------------------------
-- Table structure for yun_container
-- ----------------------------
DROP TABLE IF EXISTS `yun_container`;
CREATE TABLE `yun_container`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `image_id` bigint NOT NULL COMMENT '镜像 id（主键）',
  `user_id` bigint NOT NULL COMMENT '创建用户 id',
  `container_id` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '容器 id',
  `command` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '命令',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态',
  `ports` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '端口',
  `container_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '容器名称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_postId`(`image_id`) USING BTREE,
  INDEX `idx_userId`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '容器' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of yun_container
-- ----------------------------
INSERT INTO `yun_container` VALUES (15, 6, 2, '8bea452b0984f2bc6f14618fafa1c19f6d5bbd6fabfd51718bd5911ea9d335bd', NULL, 'running', '9096:8080/tcp', 'yun-docker-demo_64382', '2025-04-18 00:10:45', '2025-04-18 00:10:45');

-- ----------------------------
-- Table structure for yun_image
-- ----------------------------
DROP TABLE IF EXISTS `yun_image`;
CREATE TABLE `yun_image`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `repository` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '镜像名称',
  `tag` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'latest' COMMENT 'tag',
  `image_id` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '镜像id',
  `image_size` double NULL DEFAULT NULL COMMENT '镜像大小',
  `image_type` tinyint NOT NULL DEFAULT 0 COMMENT '镜像类型：0-public,1-private',
  `user_id` bigint NULL DEFAULT NULL COMMENT '创建用户 id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_ime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
  `port` int NULL DEFAULT NULL COMMENT '对外暴露的端口',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_userId`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '镜像' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of yun_image
-- ----------------------------
INSERT INTO `yun_image` VALUES (1, 'redis', 'alpine', '3900abf41552', 32.67, 0, 2, '2025-04-07 20:42:09', '2025-04-11 15:30:34', 0, 6379);
INSERT INTO `yun_image` VALUES (2, 'nginx', 'alpine', 'cc44224bfe20', 23.45, 0, 2, '2025-04-07 20:48:02', '2025-04-11 15:30:46', 0, 80);
INSERT INTO `yun_image` VALUES (3, 'hello-world', 'linux', NULL, 0.2, 0, 1, '2025-04-07 20:50:15', '2025-04-11 15:31:39', 0, 0);
INSERT INTO `yun_image` VALUES (4, 'busybox', 'latest', NULL, NULL, 1, 2, '2025-04-07 21:04:13', '2025-04-11 15:31:44', 1, 0);
INSERT INTO `yun_image` VALUES (5, 'crpi-46ersz5rsr2d22cp.cn-guangzhou.personal.cr.aliyuncs.com/laifucheng/web-demo', 'latest', '52209645d450', NULL, 0, 1, '2025-04-07 21:42:15', '2025-04-11 15:31:46', 1, 0);
INSERT INTO `yun_image` VALUES (6, 'yun-docker-demo', 'latest', '52209645d450', 544, 0, 1, '2025-04-07 22:21:07', '2025-04-11 15:31:35', 0, 8080);
INSERT INTO `yun_image` VALUES (7, 'busybox', 'latest', 'beae173ccac6ad749f76713cf4440fe3d21d1043fe616dfbe30775815d1d0f6a', 1.2, 1, 2, '2025-04-11 13:43:56', '2025-04-11 15:31:50', 1, 0);
INSERT INTO `yun_image` VALUES (8, 'ubuntu/kafka', 'latest', 'eee90c2b0c900fff9db80c2816e98c71bfd264202323618b0724194219fdf9ad', 383.4, 1, 2, '2025-04-11 15:56:59', '2025-04-11 15:56:59', 0, 9092);

-- ----------------------------
-- Table structure for yun_port
-- ----------------------------
DROP TABLE IF EXISTS `yun_port`;
CREATE TABLE `yun_port`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `port` int NOT NULL COMMENT '端口',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `port`(`port`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '端口' ROW_FORMAT = Dynamic;



-- 订单表
CREATE TABLE yun_order
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    buyer_id     BIGINT                             NOT NULL COMMENT '用户ID',
    order_id    VARCHAR(64)                        NOT NULL COMMENT '订单ID',
    credit      INT                                NOT NULL COMMENT '充值积分数量',
    money       DECIMAL(10, 2)                     NOT NULL COMMENT '支付金额(元)',
    status      TINYINT                            NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付,1-支付成功,2-支付失败,3-已取消',
    trade_no    VARCHAR(64) COMMENT '支付宝交易号',
    create_time DATETIME default CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    finished_time    DATETIME COMMENT '完成时间',
    cancel_time DATETIME COMMENT '取消时间',
    is_delete   tinyint  default 0                 not null comment '是否删除'
) COMMENT '充值订单表' collate = utf8mb4_unicode_ci;

-- ----------------------------
-- Records of yun_port
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
