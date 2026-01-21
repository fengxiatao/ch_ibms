-- =============================================
-- 报警主机分区和防区表结构（幂等）
-- 适用数据库：MySQL
-- 数据库名：ch_ibms
-- 作用：存储报警主机的分区和防区信息
-- =============================================

USE ch_ibms;

-- ========== 1. 报警主机分区表 ==========
CREATE TABLE IF NOT EXISTS iot_alarm_partition (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '分区ID',
  host_id bigint NOT NULL COMMENT '报警主机ID',
  partition_no int NOT NULL COMMENT '分区编号',
  partition_name varchar(100) DEFAULT NULL COMMENT '分区名称',
  status tinyint NOT NULL DEFAULT 0 COMMENT '布防状态：0-撤防，1-布防',
  description varchar(255) DEFAULT NULL COMMENT '分区描述',
  
  -- 审计字段
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  
  PRIMARY KEY (id),
  UNIQUE KEY uk_host_partition (host_id, partition_no, deleted) COMMENT '主机+分区编号唯一',
  KEY idx_host_id (host_id),
  KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报警主机分区表';

-- ========== 2. 报警主机防区表 ==========
CREATE TABLE IF NOT EXISTS iot_alarm_zone (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '防区ID',
  host_id bigint NOT NULL COMMENT '报警主机ID',
  partition_id bigint DEFAULT NULL COMMENT '所属分区ID',
  zone_no int NOT NULL COMMENT '防区编号',
  zone_name varchar(100) DEFAULT NULL COMMENT '防区名称',
  zone_type varchar(50) DEFAULT NULL COMMENT '防区类型',
  status tinyint NOT NULL DEFAULT 0 COMMENT '布防状态：0-撤防，1-布防',
  alarm_status tinyint NOT NULL DEFAULT 0 COMMENT '报警状态：0-正常，1-报警',
  description varchar(255) DEFAULT NULL COMMENT '防区描述',
  
  -- 审计字段
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  
  PRIMARY KEY (id),
  UNIQUE KEY uk_host_zone (host_id, zone_no, deleted) COMMENT '主机+防区编号唯一',
  KEY idx_host_id (host_id),
  KEY idx_partition_id (partition_id),
  KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报警主机防区表';

-- ========== 3. 报警主机状态查询记录表 ==========
CREATE TABLE IF NOT EXISTS iot_alarm_host_status_log (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  host_id bigint NOT NULL COMMENT '报警主机ID',
  query_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '查询时间',
  system_status tinyint NOT NULL COMMENT '系统状态：0-撤防，1-布防',
  zone_status_data text COMMENT '防区状态原始数据',
  partition_count int DEFAULT 0 COMMENT '分区数量',
  zone_count int DEFAULT 0 COMMENT '防区数量',
  
  -- 审计字段
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  
  PRIMARY KEY (id),
  KEY idx_host_id (host_id),
  KEY idx_query_time (query_time),
  KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报警主机状态查询记录表';

-- ========== 使用说明 ==========
-- 1. iot_alarm_partition: 存储报警主机的分区信息
--    - partition_no: 分区编号（从1开始）
--    - status: 0-撤防，1-布防
--
-- 2. iot_alarm_zone: 存储报警主机的防区信息
--    - zone_no: 防区编号（从1开始）
--    - status: 0-撤防，1-布防
--    - alarm_status: 0-正常，1-报警
--
-- 3. iot_alarm_host_status_log: 记录每次查询主机状态的日志
--    - zone_status_data: 存储原始的状态字符串（如：S0aaaaaaAB）
--    - 用于历史追溯和调试
