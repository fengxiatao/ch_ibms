-- TDengine 报警事件超级表
-- 用于存储报警主机的所有事件（时序数据）

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS ch_ibms KEEP 3650 DURATION 365 BUFFER 256 PAGES 128;

USE ch_ibms;

-- 创建报警事件超级表
CREATE STABLE IF NOT EXISTS alarm_event (
    -- 时间戳（主键）
    ts TIMESTAMP,
    
    -- 事件信息
    event_code NCHAR(10),           -- 事件码（如1130、3401等）
    event_type NCHAR(20),            -- 事件类型：ALARM-报警, ARM-布防, DISARM-撤防, FAULT-故障, BYPASS-旁路
    event_level NCHAR(20),           -- 事件级别：INFO-信息, WARNING-警告, ERROR-错误, CRITICAL-严重
    event_desc NCHAR(500),           -- 事件描述
    
    -- 位置信息
    area_no INT,                     -- 分区号
    zone_no INT,                     -- 防区号
    user_no INT,                     -- 用户号
    
    -- 原始数据
    sequence NCHAR(20),              -- 序列号
    raw_data NCHAR(1000),            -- 原始数据
    
    -- 事件标识
    is_new_event BOOL,               -- 是否新事件：true-新事件（千位1），false-恢复事件（千位3）
    is_handled BOOL,                 -- 是否已处理
    handled_by NCHAR(64),            -- 处理人
    handled_time TIMESTAMP,          -- 处理时间
    handle_remark NCHAR(500)         -- 处理备注
) TAGS (
    -- 标签（用于分组和过滤）
    host_id BIGINT,                  -- 报警主机ID
    host_name NCHAR(100),            -- 报警主机名称
    tenant_id BIGINT                 -- 租户ID
);

-- 创建报警心跳超级表
CREATE STABLE IF NOT EXISTS alarm_heartbeat (
    ts TIMESTAMP,                    -- 时间戳
    sequence NCHAR(20),              -- 序列号
    response_time INT                -- 响应时间（毫秒）
) TAGS (
    host_id BIGINT,                  -- 报警主机ID
    host_name NCHAR(100),            -- 报警主机名称
    tenant_id BIGINT                 -- 租户ID
);

-- 创建状态变更日志超级表
CREATE STABLE IF NOT EXISTS alarm_status_log (
    ts TIMESTAMP,                    -- 时间戳
    zone_no INT,                     -- 防区号
    old_status NCHAR(20),            -- 旧状态
    new_status NCHAR(20),            -- 新状态
    alarm_status INT,                -- 报警状态
    change_reason NCHAR(200)         -- 变更原因
) TAGS (
    host_id BIGINT,                  -- 报警主机ID
    host_name NCHAR(100),            -- 报警主机名称
    tenant_id BIGINT                 -- 租户ID
);

-- 示例：创建子表（每个报警主机一个子表）
-- CREATE TABLE alarm_event_host_1 USING alarm_event TAGS (1, '报警主机-1234', 1);

-- 示例：插入数据
-- INSERT INTO alarm_event_host_1 VALUES (
--     NOW,                          -- ts
--     '1130',                       -- event_code
--     'ALARM',                      -- event_type
--     'CRITICAL',                   -- event_level
--     '179号防区报警',              -- event_desc
--     0,                            -- area_no
--     179,                          -- zone_no
--     0,                            -- user_no
--     '0123',                       -- sequence
--     'E1234,1130001790123',        -- raw_data
--     true,                         -- is_new_event
--     false,                        -- is_handled
--     NULL,                         -- handled_by
--     NULL,                         -- handled_time
--     NULL                          -- handle_remark
-- );

-- 查询示例
-- 1. 查询最近1小时的所有报警事件
-- SELECT * FROM alarm_event WHERE ts > NOW - 1h AND event_type = 'ALARM';

-- 2. 查询指定主机的未处理事件
-- SELECT * FROM alarm_event WHERE host_id = 1 AND is_handled = false ORDER BY ts DESC;

-- 3. 按小时统计报警数量
-- SELECT _wstart, COUNT(*) FROM alarm_event 
-- WHERE event_type = 'ALARM' 
-- INTERVAL(1h) 
-- GROUP BY host_id;

-- 4. 查询最近24小时各类型事件数量
-- SELECT event_type, COUNT(*) FROM alarm_event 
-- WHERE ts > NOW - 1d 
-- GROUP BY event_type;
