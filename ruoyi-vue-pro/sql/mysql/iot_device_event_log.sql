-- ----------------------------
-- Table structure for iot_device_event_log
-- IoT 设备事件日志表
-- ----------------------------
DROP TABLE IF EXISTS `iot_device_event_log`;
CREATE TABLE `iot_device_event_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号，主键自增',
  `device_id` bigint NOT NULL COMMENT '设备编号',
  `product_id` bigint NULL DEFAULT NULL COMMENT '产品编号',
  `product_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品标识',
  `device_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备名称',
  `event_identifier` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件标识符',
  `event_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '事件名称',
  `event_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件类型（info, alert, error）',
  `event_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '事件数据（JSON格式）',
  `event_time` datetime NOT NULL COMMENT '事件发生时间',
  `onvif_topic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'ONVIF原始Topic',
  `processed` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否已处理',
  `triggered_scene_rule_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '触发的场景规则ID列表（JSON数组）',
  `generated_alert_record_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '生成的告警记录ID列表（JSON数组）',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_device_id`(`device_id`) USING BTREE COMMENT '设备ID索引',
  INDEX `idx_product_id`(`product_id`) USING BTREE COMMENT '产品ID索引',
  INDEX `idx_event_identifier`(`event_identifier`) USING BTREE COMMENT '事件标识符索引',
  INDEX `idx_event_type`(`event_type`) USING BTREE COMMENT '事件类型索引',
  INDEX `idx_event_time`(`event_time`) USING BTREE COMMENT '事件时间索引',
  INDEX `idx_processed`(`processed`) USING BTREE COMMENT '处理状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'IoT 设备事件日志表' ROW_FORMAT = Dynamic;












