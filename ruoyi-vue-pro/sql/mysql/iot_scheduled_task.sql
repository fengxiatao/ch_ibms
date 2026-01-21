-- ----------------------------
-- Table structure for iot_scheduled_task_config
-- ----------------------------
DROP TABLE IF EXISTS `iot_scheduled_task_config`;
CREATE TABLE `iot_scheduled_task_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `entity_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '实体类型: PRODUCT/DEVICE/CAMPUS/BUILDING/FLOOR/AREA',
  `entity_id` bigint NOT NULL COMMENT '实体ID',
  `entity_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '实体名称（冗余字段）',
  `job_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务类型编码',
  `job_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '任务描述',
  `enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用: 0-禁用, 1-启用',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'STOPPED' COMMENT '运行状态: RUNNING/STOPPED/EXECUTING',
  `cron_expression` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'CRON表达式',
  `interval_seconds` int NULL DEFAULT NULL COMMENT '执行间隔(秒)',
  `job_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '任务配置参数（JSON格式）',
  `priority` int NOT NULL DEFAULT 5 COMMENT '优先级: 1-最高, 10-最低',
  `conflict_strategy` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'SKIP' COMMENT '冲突策略: SKIP/QUEUE/INTERRUPT/CONCURRENT',
  `timeout_seconds` int NULL DEFAULT 300 COMMENT '超时时间(秒)',
  `retry_count` int NULL DEFAULT 0 COMMENT '失败重试次数',
  `alert_on_failure` bit(1) NULL DEFAULT b'1' COMMENT '失败告警: 0-否, 1-是',
  `from_product` bit(1) NULL DEFAULT b'0' COMMENT '是否继承自产品: 0-否, 1-是',
  `product_id` bigint NULL DEFAULT NULL COMMENT '所属产品ID（设备任务时有值）',
  `last_execution_time` datetime NULL DEFAULT NULL COMMENT '上次执行时间',
  `last_execution_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '上次执行状态: SUCCESS/FAILURE/RUNNING',
  `last_execution_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '上次执行消息',
  `next_execution_time` datetime NULL DEFAULT NULL COMMENT '下次执行时间',
  `execution_count` int NOT NULL DEFAULT 0 COMMENT '总执行次数',
  `success_count` int NOT NULL DEFAULT 0 COMMENT '成功次数',
  `fail_count` int NOT NULL DEFAULT 0 COMMENT '失败次数',
  `avg_duration_ms` int NULL DEFAULT NULL COMMENT '平均执行时长(毫秒)',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_entity`(`entity_type` ASC, `entity_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC, `enabled` ASC) USING BTREE,
  INDEX `idx_product`(`product_id` ASC) USING BTREE,
  INDEX `idx_job_type`(`job_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'IoT定时任务配置表';

-- ----------------------------
-- Table structure for iot_task_execution_log
-- ----------------------------
DROP TABLE IF EXISTS `iot_task_execution_log`;
CREATE TABLE `iot_task_execution_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_config_id` bigint NOT NULL COMMENT '任务配置ID',
  `task_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
  `execute_time` datetime NOT NULL COMMENT '执行时间',
  `duration_ms` int NULL DEFAULT NULL COMMENT '执行耗时(毫秒)',
  `trigger_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发方式: CRON/MANUAL/RETRY',
  `result` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '执行结果: SUCCESS/FAILURE/TIMEOUT',
  `result_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '结果消息',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误消息',
  `stack_trace` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '堆栈信息',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_config`(`task_config_id` ASC, `execute_time` ASC) USING BTREE,
  INDEX `idx_result`(`result` ASC) USING BTREE,
  INDEX `idx_execute_time`(`execute_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'IoT任务执行日志表';

-- ----------------------------
-- Records of iot_scheduled_task_config（示例数据）
-- ----------------------------
BEGIN;
-- 海康摄像头产品的示例任务配置
INSERT INTO `iot_scheduled_task_config` VALUES (1, 'PRODUCT', 1, '海康摄像头', 'DEVICE_OFFLINE_CHECK', '设备离线检查', '定期检查设备是否在线', b'1', 'RUNNING', NULL, 600, '{\"checkInterval\":10,\"alertThreshold\":5}', 3, 'SKIP', 300, 1, b'1', b'0', NULL, '2025-10-22 10:00:00', 'SUCCESS', '检查完成，所有设备在线', '2025-10-22 10:10:00', 100, 98, 2, 2500, '', NOW(), '', NOW(), b'0', 1);
INSERT INTO `iot_scheduled_task_config` VALUES (2, 'PRODUCT', 1, '海康摄像头', 'DEVICE_HEALTH_CHECK', '设备健康检查', '定期检查设备健康状态', b'1', 'RUNNING', NULL, 1800, '{\"healthCheckItems\":[\"cpu\",\"memory\",\"disk\"]}', 5, 'SKIP', 600, 2, b'1', b'0', NULL, '2025-10-22 09:30:00', 'SUCCESS', '健康检查完成', '2025-10-22 10:00:00', 50, 49, 1, 3200, '', NOW(), '', NOW(), b'0', 1);
COMMIT;

