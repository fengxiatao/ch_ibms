-- =============================================
-- 修复报警主机与Gateway集成问题
-- 创建时间: 2025-12-01
-- 说明: 添加设备认证所需的字段，确保Gateway能正确识别报警主机
-- =============================================

USE ch_ibms;

-- 1. 为设备表添加account字段（用于Gateway认证）
ALTER TABLE iot_device 
ADD COLUMN IF NOT EXISTS account VARCHAR(50) COMMENT '设备账号（用于协议认证）' AFTER device_name;

-- 2. 为设备表添加索引，提高查询性能
ALTER TABLE iot_device 
ADD INDEX IF NOT EXISTS idx_account (account);

-- 3. 同步现有报警主机的account到设备表
UPDATE iot_device d
INNER JOIN iot_alarm_host h ON d.id = h.device_id
SET d.account = h.account
WHERE h.account IS NOT NULL AND h.account != '';

-- 4. 验证数据同步
SELECT 
    h.id AS alarm_host_id,
    h.host_name,
    h.account AS alarm_account,
    d.id AS device_id,
    d.device_name,
    d.account AS device_account,
    CASE 
        WHEN h.account = d.account THEN '✓ 已同步'
        WHEN d.account IS NULL THEN '⚠ 设备account为空'
        ELSE '✗ 不一致'
    END AS sync_status
FROM iot_alarm_host h
LEFT JOIN iot_device d ON h.device_id = d.id
ORDER BY h.id;

-- =============================================
-- 使用说明
-- =============================================
-- 执行此脚本后：
-- 1. 设备表将包含account字段
-- 2. 现有报警主机的account会同步到设备表
-- 3. Gateway可以通过account认证设备
-- 4. 报警事件可以正常上报和处理
-- =============================================

-- =============================================
-- 后续需要做的事情
-- =============================================
-- 1. 修改报警主机创建逻辑，同时设置设备的account字段
-- 2. 实现设备服务的getDeviceByAccount方法
-- 3. 测试Gateway与报警主机的连接和事件交互
-- =============================================
