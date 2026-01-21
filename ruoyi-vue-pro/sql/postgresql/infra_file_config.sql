/*
 Navicat Premium Data Transfer

 Source Server         : 126
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43-0ubuntu0.22.04.2)
 Source Host           : 192.168.1.126:3306
 Source Schema         : ch_ibms

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43-0ubuntu0.22.04.2)
 File Encoding         : 65001

 Date: 06/11/2025 14:24:29
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for infra_file_config
-- ----------------------------
DROP TABLE IF EXISTS `infra_file_config`;
CREATE TABLE `infra_file_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(63) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置名',
  `storage` tinyint NOT NULL COMMENT '存储器',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `master` bit(1) NOT NULL COMMENT '是否为主配置',
  `config` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '存储配置',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of infra_file_config
-- ----------------------------
INSERT INTO `infra_file_config` VALUES (4, '数据库（示例）', 1, '我是数据库', b'0', '{\"@class\":\"cn.iocoder.yudao.module.infra.framework.file.core.client.db.DBFileClientConfig\",\"domain\":\"http://127.0.0.1:48080\"}', '1', '2022-03-15 23:56:24', '1', '2025-08-18 08:35:14', b'0');
INSERT INTO `infra_file_config` VALUES (22, '七牛存储器（示例）', 20, '请换成你自己的密钥！！！', b'0', '{\"@class\":\"cn.iocoder.yudao.module.infra.framework.file.core.client.s3.S3FileClientConfig\",\"endpoint\":\"s3.cn-south-1.qiniucs.com\",\"domain\":\"http://test.yudao.iocoder.cn\",\"bucket\":\"ruoyi-vue-pro\",\"accessKey\":\"3TvrJ70gl2Gt6IBe7_IZT1F6i_k0iMuRtyEv4EyS\",\"accessSecret\":\"wd0tbVBYlp0S-ihA8Qg2hPLncoP83wyrIq24OZuY\",\"enablePathStyleAccess\":false,\"enablePublicAccess\":true}', '1', '2024-01-13 22:11:12', '1', '2025-08-18 08:35:14', b'0');
INSERT INTO `infra_file_config` VALUES (24, '腾讯云存储（示例）', 20, '请换成你的密钥！！！', b'0', '{\"@class\":\"cn.iocoder.yudao.module.infra.framework.file.core.client.s3.S3FileClientConfig\",\"endpoint\":\"https://cos.ap-shanghai.myqcloud.com\",\"domain\":\"http://tengxun-oss.iocoder.cn\",\"bucket\":\"aoteman-1255880240\",\"accessKey\":\"AKIDAF6WSh1uiIjwqtrOsGSN3WryqTM6cTMt\",\"accessSecret\":\"X\",\"enablePathStyleAccess\":false,\"enablePublicAccess\":true}', '1', '2024-11-09 16:03:22', '1', '2025-08-18 08:35:14', b'0');
INSERT INTO `infra_file_config` VALUES (25, '阿里云存储（示例）', 20, '', b'0', '{\"@class\":\"cn.iocoder.yudao.module.infra.framework.file.core.client.s3.S3FileClientConfig\",\"endpoint\":\"oss-cn-beijing.aliyuncs.com\",\"domain\":\"http://ali-oss.iocoder.cn\",\"bucket\":\"yunai-aoteman\",\"accessKey\":\"LTAI5tEQLgnDyjh3WpNcdMKA\",\"accessSecret\":\"X\",\"enablePathStyleAccess\":false,\"enablePublicAccess\":true}', '1', '2024-11-09 16:47:08', '1', '2025-08-18 08:35:14', b'0');
INSERT INTO `infra_file_config` VALUES (26, '火山云存储（示例）', 20, '', b'0', '{\"@class\":\"cn.iocoder.yudao.module.infra.framework.file.core.client.s3.S3FileClientConfig\",\"endpoint\":\"tos-s3-cn-beijing.volces.com\",\"domain\":null,\"bucket\":\"yunai\",\"accessKey\":\"AKLTZjc3Zjc4MzZmMjU3NDk0ZTgxYmIyMmFkNTIwMDI1ZGE\",\"accessSecret\":\"X==\",\"enablePathStyleAccess\":false,\"enablePublicAccess\":true}', '1', '2024-11-09 16:56:42', '1', '2025-08-18 08:35:14', b'0');
INSERT INTO `infra_file_config` VALUES (27, '华为云存储（示例）', 20, '', b'0', '{\"@class\":\"cn.iocoder.yudao.module.infra.framework.file.core.client.s3.S3FileClientConfig\",\"endpoint\":\"obs.cn-east-3.myhuaweicloud.com\",\"domain\":\"\",\"bucket\":\"yudao\",\"accessKey\":\"PVDONDEIOTW88LF8DC4U\",\"accessSecret\":\"X\",\"enablePathStyleAccess\":false}', '1', '2024-11-09 17:18:41', '1', '2025-08-18 08:35:14', b'0');
INSERT INTO `infra_file_config` VALUES (28, 'MinIO 存储（示例）', 20, '', b'0', '{\"@class\":\"cn.iocoder.yudao.module.infra.framework.file.core.client.s3.S3FileClientConfig\",\"endpoint\":\"http://127.0.0.1:9000\",\"domain\":\"http://127.0.0.1:9000/yudao\",\"bucket\":\"yudao\",\"accessKey\":\"admin\",\"accessSecret\":\"password\",\"enablePathStyleAccess\":false,\"enablePublicAccess\":true}', '1', '2024-11-09 17:43:10', '1', '2025-08-18 08:35:14', b'0');
INSERT INTO `infra_file_config` VALUES (29, '本地存储（示例）', 10, '仅适合 mac 或 windows', b'0', '{\"@class\":\"cn.iocoder.yudao.module.infra.framework.file.core.client.local.LocalFileClientConfig\",\"basePath\":\"/Users/yunai/tmp/file\",\"domain\":\"http://127.0.0.1:48080\"}', '1', '2025-05-02 11:25:45', '1', '2025-08-18 08:35:14', b'0');
INSERT INTO `infra_file_config` VALUES (30, 'SFTP 存储（示例）', 12, '', b'1', '{\"@class\":\"cn.iocoder.yudao.module.infra.framework.file.core.client.sftp.SftpFileClientConfig\",\"basePath\":\"/home/admin\",\"domain\":\"http://ibms.gzchanghui.cn/images\",\"host\":\"192.168.1.246\",\"port\":5022,\"username\":\"admin\",\"password\":\"ch333888\"}', '1', '2025-05-02 16:34:10', '1', '2025-11-06 11:26:21', b'0');
INSERT INTO `infra_file_config` VALUES (34, '七牛云存储【私有】（示例）', 20, '请换成你自己的密钥！！！', b'0', '{\"@class\":\"cn.iocoder.yudao.module.infra.framework.file.core.client.s3.S3FileClientConfig\",\"endpoint\":\"s3.cn-south-1.qiniucs.com\",\"domain\":\"http://t151glocd.hn-bkt.clouddn.com\",\"bucket\":\"ruoyi-vue-pro-private\",\"accessKey\":\"3TvrJ70gl2Gt6IBe7_IZT1F6i_k0iMuRtyEv4EyS\",\"accessSecret\":\"wd0tbVBYlp0S-ihA8Qg2hPLncoP83wyrIq24OZuY\",\"enablePathStyleAccess\":false,\"enablePublicAccess\":false}', '1', '2025-08-17 21:22:00', '1', '2025-08-18 08:35:55', b'0');

SET FOREIGN_KEY_CHECKS = 1;
