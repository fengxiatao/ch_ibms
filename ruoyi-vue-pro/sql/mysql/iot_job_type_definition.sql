-- ========================================
-- IoT 定时任务类型定义表
-- ========================================
-- 说明：这个表定义了系统中所有可用的定时任务类型
-- 用于：动态配置不同实体（产品、设备、园区、建筑等）的定时任务
-- ========================================

SET NAMES utf8mb4;

-- ----------------------------
-- Table structure for iot_job_type_definition
-- ----------------------------
DROP TABLE IF EXISTS `iot_job_type_definition`;
CREATE TABLE `iot_job_type_definition` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务编码（唯一标识）',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '任务描述',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务类型：IOT_DEVICE-物联网设备, SPATIAL-空间设施, ENERGY-能源, SECURITY-安防, HVAC-空调, SYSTEM-系统',
  `applicable_entities` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '适用实体类型（逗号分隔）：PRODUCT-产品, DEVICE-设备, CAMPUS-园区, BUILDING-建筑, FLOOR-楼层, AREA-区域',
  `default_config_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '默认配置模板（JSON格式）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_code` (`code`) USING BTREE COMMENT '任务编码唯一索引',
  KEY `idx_business_type` (`business_type`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 定时任务类型定义表';

-- ----------------------------
-- 插入预定义的任务类型
-- ----------------------------

-- ==================== 物联网设备相关任务 ====================

-- 1. 设备离线检查
INSERT INTO `iot_job_type_definition` (`name`, `code`, `description`, `business_type`, `applicable_entities`, `default_config_template`, `status`) VALUES
('设备离线检查', 'DEVICE_OFFLINE_CHECK', '定期检查设备是否在线，及时发现离线设备', 'IOT_DEVICE', 'PRODUCT,DEVICE,CAMPUS,BUILDING,FLOOR,AREA', 
'{"enabled":true,"interval":10,"unit":"MINUTE","priority":3,"description":"检查设备是否在线"}', 1);

-- 2. 设备健康检查
INSERT INTO `iot_job_type_definition` (`name`, `code`, `description`, `business_type`, `applicable_entities`, `default_config_template`, `status`) VALUES
('设备健康检查', 'DEVICE_HEALTH_CHECK', '检查设备运行状态和健康指标', 'IOT_DEVICE', 'PRODUCT,DEVICE,CAMPUS,BUILDING,FLOOR,AREA', 
'{"enabled":false,"interval":30,"unit":"MINUTE","priority":5,"description":"检查设备健康状态"}', 1);

-- 3. 设备数据采集
INSERT INTO `iot_job_type_definition` (`name`, `code`, `description`, `business_type`, `applicable_entities`, `default_config_template`, `status`) VALUES
('设备数据采集', 'DEVICE_DATA_COLLECT', '定期采集设备传感器数据', 'IOT_DEVICE', 'PRODUCT,DEVICE,CAMPUS,BUILDING,FLOOR,AREA', 
'{"enabled":false,"interval":15,"unit":"MINUTE","priority":5,"description":"采集设备传感器数据"}', 1);

-- 4. 设备状态同步
INSERT INTO `iot_job_type_definition` (`name`, `code`, `description`, `business_type`, `applicable_entities`, `default_config_template`, `status`) VALUES
('设备状态同步', 'DEVICE_STATUS_SYNC', '同步设备状态到云端平台', 'IOT_DEVICE', 'PRODUCT,DEVICE', 
'{"enabled":false,"interval":5,"unit":"MINUTE","priority":7,"description":"同步设备状态"}', 1);

-- 5. 设备固件检查
INSERT INTO `iot_job_type_definition` (`name`, `code`, `description`, `business_type`, `applicable_entities`, `default_config_template`, `status`) VALUES
('设备固件检查', 'DEVICE_FIRMWARE_CHECK', '检查设备固件版本并提示更新', 'IOT_DEVICE', 'PRODUCT,DEVICE', 
'{"enabled":false,"interval":1,"unit":"HOUR","priority":7,"description":"检查设备固件版本"}', 1);

-- ==================== 安防相关任务 ====================

-- 6. 摄像头巡检
INSERT INTO `iot_job_type_definition` (`name`, `code`, `description`, `business_type`, `applicable_entities`, `default_config_template`, `status`) VALUES
('摄像头巡检', 'CAMERA_INSPECTION', '定期检查摄像头画面质量和录像状态', 'SECURITY', 'PRODUCT,DEVICE,CAMPUS,BUILDING,FLOOR,AREA', 
'{"enabled":false,"interval":1,"unit":"HOUR","priority":3,"description":"检查摄像头状态"}', 1);

-- 7. 门禁记录同步
INSERT INTO `iot_job_type_definition` (`name`, `code`, `description`, `business_type`, `applicable_entities`, `default_config_template`, `status`) VALUES
('门禁记录同步', 'ACCESS_CONTROL_SYNC', '同步门禁刷卡记录', 'SECURITY', 'DEVICE,BUILDING,FLOOR,AREA', 
'{"enabled":false,"interval":10,"unit":"MINUTE","priority":5,"description":"同步门禁记录"}', 1);

-- 8. 报警事件处理
INSERT INTO `iot_job_type_definition` (`name`, `code`, `description`, `business_type`, `applicable_entities`, `default_config_template`, `status`) VALUES
('报警事件处理', 'ALARM_EVENT_PROCESS', '处理设备报警事件并通知相关人员', 'SECURITY', 'DEVICE,CAMPUS,BUILDING,FLOOR,AREA', 
'{"enabled":false,"interval":1,"unit":"MINUTE","priority":1,"description":"处理报警事件"}', 1);

-- ==================== 能源管理相关任务 ====================

-- 9. 能耗数据采集
INSERT INTO `iot_job_type_definition` (`name`, `code`, `description`, `business_type`, `applicable_entities`, `default_config_template`, `status`) VALUES
('能耗数据采集', 'ENERGY_DATA_COLLECT', '采集水电气等能耗数据', 'ENERGY', 'CAMPUS,BUILDING,FLOOR,AREA', 
'{"enabled":false,"interval":30,"unit":"MINUTE","priority":5,"description":"采集能耗数据"}', 1);

-- 10. 能耗异常检测
INSERT INTO `iot_job_type_definition` (`name`, `code`, `description`, `business_type`, `applicable_entities`, `default_config_template`, `status`) VALUES
('能耗异常检测', 'ENERGY_ANOMALY_DETECT', '检测能耗异常情况并告警', 'ENERGY', 'CAMPUS,BUILDING,FLOOR,AREA', 
'{"enabled":false,"interval":1,"unit":"HOUR","priority":3,"description":"检测能耗异常"}', 1);

-- ==================== 环境监测相关任务 ====================

-- 11. 环境数据监测
INSERT INTO `iot_job_type_definition` (`name`, `code`, `description`, `business_type`, `applicable_entities`, `default_config_template`, `status`) VALUES
('环境数据监测', 'ENVIRONMENT_MONITOR', '监测温湿度、PM2.5等环境数据', 'IOT_DEVICE', 'DEVICE,CAMPUS,BUILDING,FLOOR,AREA', 
'{"enabled":false,"interval":15,"unit":"MINUTE","priority":5,"description":"监测环境数据"}', 1);

-- 12. 空气质量分析
INSERT INTO `iot_job_type_definition` (`name`, `code`, `description`, `business_type`, `applicable_entities`, `default_config_template`, `status`) VALUES
('空气质量分析', 'AIR_QUALITY_ANALYSIS', '分析空气质量并生成报告', 'IOT_DEVICE', 'CAMPUS,BUILDING,FLOOR', 
'{"enabled":false,"interval":1,"unit":"HOUR","priority":7,"description":"分析空气质量"}', 1);

-- ==================== 空调暖通相关任务 ====================

-- 13. 空调设备监控
INSERT INTO `iot_job_type_definition` (`name`, `code`, `description`, `business_type`, `applicable_entities`, `default_config_template`, `status`) VALUES
('空调设备监控', 'HVAC_MONITOR', '监控空调设备运行状态', 'HVAC', 'DEVICE,BUILDING,FLOOR,AREA', 
'{"enabled":false,"interval":10,"unit":"MINUTE","priority":5,"description":"监控空调设备"}', 1);

-- 14. 温度自动调节
INSERT INTO `iot_job_type_definition` (`name`, `code`, `description`, `business_type`, `applicable_entities`, `default_config_template`, `status`) VALUES
('温度自动调节', 'TEMPERATURE_AUTO_ADJUST', '根据环境温度自动调节空调', 'HVAC', 'BUILDING,FLOOR,AREA', 
'{"enabled":false,"interval":30,"unit":"MINUTE","priority":5,"description":"自动调节温度"}', 1);

-- ==================== 系统维护相关任务 ====================

-- 15. 数据备份
INSERT INTO `iot_job_type_definition` (`name`, `code`, `description`, `business_type`, `applicable_entities`, `default_config_template`, `status`) VALUES
('数据备份', 'DATA_BACKUP', '定期备份设备数据和配置', 'SYSTEM', 'CAMPUS,BUILDING', 
'{"enabled":false,"interval":1,"unit":"HOUR","priority":7,"description":"备份数据"}', 1);

-- 16. 日志清理
INSERT INTO `iot_job_type_definition` (`name`, `code`, `description`, `business_type`, `applicable_entities`, `default_config_template`, `status`) VALUES
('日志清理', 'LOG_CLEANUP', '清理过期的日志数据', 'SYSTEM', 'CAMPUS', 
'{"enabled":false,"interval":24,"unit":"HOUR","priority":7,"description":"清理过期日志"}', 1);

-- 17. 设备自动重启
INSERT INTO `iot_job_type_definition` (`name`, `code`, `description`, `business_type`, `applicable_entities`, `default_config_template`, `status`) VALUES
('设备自动重启', 'DEVICE_AUTO_RESTART', '定期重启设备以保持最佳状态', 'SYSTEM', 'DEVICE', 
'{"enabled":false,"interval":7,"unit":"HOUR","priority":7,"description":"定期重启设备"}', 1);

-- ==================== 空间设施相关任务 ====================

-- 18. 空间设备统计
INSERT INTO `iot_job_type_definition` (`name`, `code`, `description`, `business_type`, `applicable_entities`, `default_config_template`, `status`) VALUES
('空间设备统计', 'SPATIAL_DEVICE_STATS', '统计空间内的设备数量和状态', 'SPATIAL', 'CAMPUS,BUILDING,FLOOR,AREA', 
'{"enabled":false,"interval":1,"unit":"HOUR","priority":7,"description":"统计设备信息"}', 1);

-- 19. 空间巡检任务
INSERT INTO `iot_job_type_definition` (`name`, `code`, `description`, `business_type`, `applicable_entities`, `default_config_template`, `status`) VALUES
('空间巡检任务', 'SPATIAL_INSPECTION', '对指定空间进行定期巡检', 'SPATIAL', 'CAMPUS,BUILDING,FLOOR,AREA', 
'{"enabled":false,"interval":2,"unit":"HOUR","priority":5,"description":"执行空间巡检"}', 1);

-- 20. 占用率分析
INSERT INTO `iot_job_type_definition` (`name`, `code`, `description`, `business_type`, `applicable_entities`, `default_config_template`, `status`) VALUES
('占用率分析', 'OCCUPANCY_ANALYSIS', '分析空间占用率情况', 'SPATIAL', 'BUILDING,FLOOR,AREA', 
'{"enabled":false,"interval":1,"unit":"HOUR","priority":7,"description":"分析空间占用率"}', 1);

-- 完成
SELECT '✅ IoT定时任务类型定义表已成功创建并初始化！' as message;
SELECT CONCAT('📊 共插入 ', COUNT(*), ' 条任务类型定义') as summary FROM iot_job_type_definition WHERE deleted = 0;

