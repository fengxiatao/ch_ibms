-- ----------------------------
-- Table structure for iot_device_service_log
-- IoT 设备服务调用日志表
-- ----------------------------
DROP TABLE IF EXISTS `iot_device_service_log`;
CREATE TABLE `iot_device_service_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号，主键自增',
  `device_id` bigint NOT NULL COMMENT '设备编号',
  `product_id` bigint NULL DEFAULT NULL COMMENT '产品编号',
  `product_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品标识',
  `device_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备名称',
  `service_identifier` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '服务标识符',
  `service_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '服务名称',
  `request_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '请求ID',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求参数（JSON格式）',
  `request_time` datetime NOT NULL COMMENT '请求时间',
  `status_code` int NULL DEFAULT NULL COMMENT '响应状态码（200-成功，其他-失败）',
  `response_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '响应消息',
  `response_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '响应数据（JSON格式）',
  `response_time` datetime NULL DEFAULT NULL COMMENT '响应时间',
  `execution_time` bigint NULL DEFAULT NULL COMMENT '执行耗时（毫秒）',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作人名称',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_request_id`(`request_id`) USING BTREE COMMENT '请求ID唯一索引',
  INDEX `idx_device_id`(`device_id`) USING BTREE COMMENT '设备ID索引',
  INDEX `idx_product_id`(`product_id`) USING BTREE COMMENT '产品ID索引',
  INDEX `idx_service_identifier`(`service_identifier`) USING BTREE COMMENT '服务标识符索引',
  INDEX `idx_status_code`(`status_code`) USING BTREE COMMENT '状态码索引',
  INDEX `idx_request_time`(`request_time`) USING BTREE COMMENT '请求时间索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'IoT 设备服务调用日志表' ROW_FORMAT = Dynamic;












