-- =============================================
-- 更新报警主机账号配置
-- 创建时间: 2025-12-02
-- 说明: 为报警主机设置account字段，用于TCP连接识别
-- =============================================

USE ch_ibms;

-- 查看当前主机配置
SELECT 
  id,
  host_name,
  account,
  password,
  ip_address,
  port,
  online_status
FROM iot_alarm_host;

-- =============================================
-- 更新主机账号
-- =============================================
-- 注意：account必须与报警主机设备上配置的账号完全一致！
-- 
-- 示例：如果报警主机配置的账号是"1234"，则这里也必须是"1234"
-- =============================================

-- 更新ID为1的主机（根据实际情况修改）
UPDATE iot_alarm_host 
SET 
  account = '1234',              -- 主机账号（必须与设备配置一致）
  password = '123456',           -- 密码（可选，根据设备配置）
  ip_address = '192.168.1.210',  -- 主机IP地址
  port = 9988                    -- Gateway监听端口
WHERE id = 1;

-- 验证更新结果
SELECT 
  id,
  host_name,
  account,
  password,
  ip_address,
  port
FROM iot_alarm_host
WHERE id = 1;

-- =============================================
-- 批量更新多个主机（如果有多个主机）
-- =============================================
-- UPDATE iot_alarm_host SET account = '1234', ip_address = '192.168.1.210', port = 9988 WHERE id = 1;
-- UPDATE iot_alarm_host SET account = '5678', ip_address = '192.168.1.211', port = 9988 WHERE id = 2;
-- UPDATE iot_alarm_host SET account = '9012', ip_address = '192.168.1.212', port = 9988 WHERE id = 3;

-- =============================================
-- 重要说明
-- =============================================
-- 1. account字段是主机的唯一标识，用于TCP连接时的识别
-- 2. account必须与报警主机设备上配置的账号完全一致
-- 3. ip_address是主机的实际IP地址（用于识别和管理）
-- 4. port是Gateway的TCP监听端口（默认9988）
-- 5. 更新后需要确保报警主机已连接到Gateway
-- 
-- 连接验证：
-- - 查看Gateway日志：应该看到"注册连接: account=1234"
-- - 查看连接状态：online_status应该为1
-- =============================================
