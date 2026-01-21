-- OPC报警记录表（TDengine超级表）
-- 用于存储IP9500报警主机的报警事件记录

-- 创建超级表
CREATE STABLE IF NOT EXISTS opc_alarm_record (
    ts TIMESTAMP,                    -- 时间戳（主键）
    event_code INT,                  -- 事件代码
    area SMALLINT,                   -- 防区号
    point SMALLINT,                  -- 点位号
    sequence INT,                    -- 序列号
    event_description NCHAR(200),    -- 事件描述
    level NCHAR(20),                 -- 事件级别（info/warning/error/critical）
    type NCHAR(20),                  -- 事件类型（alarm/restore/status/test）
    receive_time TIMESTAMP,          -- 接收时间
    remote_address NCHAR(50),        -- 远程地址
    remote_port INT,                 -- 远程端口
    raw_message NCHAR(500),          -- 原始消息
    device_id BIGINT,                -- 设备ID
    device_name NCHAR(100),          -- 设备名称
    area_name NCHAR(100),            -- 防区名称
    point_name NCHAR(100),           -- 点位名称
    location NCHAR(200),             -- 位置信息
    handled TINYINT,                 -- 是否已处理（0/1）
    handle_time TIMESTAMP,           -- 处理时间
    handle_by NCHAR(50),             -- 处理人
    handle_remark NCHAR(500),        -- 处理备注
    create_time TIMESTAMP            -- 创建时间
) TAGS (
    account INT                      -- 账号（主机标识，作为TAG）
);

-- 创建示例子表（实际使用时会自动创建）
-- CREATE TABLE IF NOT EXISTS opc_alarm_record_1001 USING opc_alarm_record TAGS (1001);

-- 创建索引（TDengine 3.0+支持）
-- CREATE INDEX IF NOT EXISTS idx_event_code ON opc_alarm_record (event_code);
-- CREATE INDEX IF NOT EXISTS idx_level ON opc_alarm_record (level);
-- CREATE INDEX IF NOT EXISTS idx_type ON opc_alarm_record (type);

-- 查询示例
-- 1. 查询最近100条报警记录
-- SELECT * FROM opc_alarm_record ORDER BY ts DESC LIMIT 100;

-- 2. 查询指定账号的报警记录
-- SELECT * FROM opc_alarm_record WHERE account = 1001 ORDER BY ts DESC LIMIT 100;

-- 3. 查询指定时间范围的报警记录
-- SELECT * FROM opc_alarm_record WHERE ts >= '2025-01-01 00:00:00' AND ts < '2025-01-02 00:00:00';

-- 4. 按事件级别统计
-- SELECT level, COUNT(*) as count FROM opc_alarm_record GROUP BY level;

-- 5. 查询未处理的报警
-- SELECT * FROM opc_alarm_record WHERE handled = 0 ORDER BY ts DESC;
