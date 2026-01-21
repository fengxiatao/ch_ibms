-- =====================================================
-- IoT 设备通道历史记录表
-- =====================================================
-- 
-- 设计目的：
-- 1. 记录通道的所有变更历史（新增、更新、删除）
-- 2. 用于审计和追溯
-- 3. 不影响主表的查询性能
-- 
-- =====================================================

USE ch_ibms;

CREATE TABLE IF NOT EXISTS `iot_device_channel_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
  
  -- ========== 关联信息 ==========
  `channel_id` bigint(20) DEFAULT NULL COMMENT '通道ID（关联 iot_device_channel.id）',
  `device_id` bigint(20) NOT NULL COMMENT '所属设备ID',
  `channel_no` int(11) DEFAULT NULL COMMENT '通道号',
  
  -- ========== 操作信息 ==========
  `operation` varchar(10) NOT NULL COMMENT '操作类型（INSERT、UPDATE、DELETE）',
  `operation_desc` varchar(256) DEFAULT NULL COMMENT '操作描述',
  
  -- ========== 通道数据快照 ==========
  `channel_data` text NOT NULL COMMENT '通道数据快照（JSON格式，完整的通道信息）',
  
  -- ========== 变更信息 ==========
  `changed_fields` text DEFAULT NULL COMMENT '变更字段（JSON格式，记录哪些字段发生了变化）',
  `old_values` text DEFAULT NULL COMMENT '旧值（JSON格式）',
  `new_values` text DEFAULT NULL COMMENT '新值（JSON格式）',
  
  -- ========== 操作人信息 ==========
  `operator` varchar(64) DEFAULT 'SYSTEM' COMMENT '操作人（SYSTEM表示系统自动同步）',
  `operator_id` bigint(20) DEFAULT NULL COMMENT '操作人ID',
  `operate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `operate_ip` varchar(64) DEFAULT NULL COMMENT '操作IP',
  
  -- ========== 同步信息 ==========
  `sync_source` varchar(32) DEFAULT NULL COMMENT '同步来源（SDK、MANUAL、IMPORT）',
  `sync_batch_id` varchar(64) DEFAULT NULL COMMENT '同步批次ID（同一次同步的记录有相同的批次ID）',
  
  -- ========== 备注 ==========
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  
  PRIMARY KEY (`id`),
  KEY `idx_channel_id` (`channel_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_operate_time` (`operate_time`),
  KEY `idx_operation` (`operation`),
  KEY `idx_sync_batch_id` (`sync_batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IoT设备通道历史记录表';

-- =====================================================
-- 示例查询
-- =====================================================

-- 1. 查询某个通道的所有历史记录
-- SELECT * FROM iot_device_channel_history 
-- WHERE channel_id = 123 
-- ORDER BY operate_time DESC;

-- 2. 查询某次同步的所有变更
-- SELECT * FROM iot_device_channel_history 
-- WHERE sync_batch_id = 'SYNC-2025-11-17-001' 
-- ORDER BY operate_time;

-- 3. 查询最近删除的通道
-- SELECT * FROM iot_device_channel_history 
-- WHERE operation = 'DELETE' 
-- ORDER BY operate_time DESC 
-- LIMIT 10;

-- 4. 统计每天的通道变更次数
-- SELECT DATE(operate_time) as date, operation, COUNT(*) as count
-- FROM iot_device_channel_history
-- GROUP BY DATE(operate_time), operation
-- ORDER BY date DESC;
