-- ============================================================
-- 设备发现列表 - 清理已激活设备脚本
-- ============================================================
-- 
-- 问题描述：
--   设备激活后，没有立即从发现列表中移除，导致已激活的设备
--   仍然显示在"设备发现"页面中。
--
-- 原因分析：
--   旧逻辑：只有当设备上线后，才调用 markAsActivated() 从列表移除
--   新逻辑：激活时立即从列表移除（不等待设备上线）
--
-- 解决方案：
--   1. 手动清理：执行本SQL脚本，标记已激活的设备
--   2. 代码修复：已修改 IotDeviceActivationServiceImpl.activateDevice()
--              在激活时立即调用 markAsActivated()
--
-- 执行时间：2025-11-03
-- 作者：AI Assistant
-- ============================================================

-- 1. 查看当前问题：已在平台注册但未标记为已激活的设备
SELECT 
    dd.id AS '发现记录ID',
    dd.ip AS 'IP地址',
    dd.vendor AS '制造商',
    dd.discovery_time AS '发现时间',
    dd.activated AS '已激活标记',
    d.id AS '平台设备ID',
    d.device_name AS '设备名称',
    d.state AS '设备状态(0离线1在线)',
    d.create_time AS '激活时间'
FROM iot_discovered_device dd
INNER JOIN iot_device d ON dd.ip = d.ip  -- 通过IP关联
WHERE dd.activated = FALSE  -- 发现记录未标记为已激活
  AND d.id IS NOT NULL      -- 但设备已在平台注册
ORDER BY dd.discovery_time DESC;

-- 预期结果：显示所有已激活但还在发现列表中的设备
-- 如果有数据，说明存在问题，需要执行下面的修复SQL


-- ============================================================
-- 2. 修复SQL：标记已激活的设备
-- ============================================================

-- ⚠️ 执行前请先备份数据！
-- mysqldump -u root -p ibms_db iot_discovered_device > backup_discovered_device.sql

-- 开始事务
START TRANSACTION;

-- 2.1 标记已激活的设备（通过IP匹配）
UPDATE iot_discovered_device dd
INNER JOIN iot_device d ON dd.ip = d.ip
SET 
    dd.activated = TRUE,                    -- 标记为已激活
    dd.activated_device_id = d.id,          -- 关联设备ID
    dd.activated_time = d.create_time,      -- 使用设备创建时间作为激活时间
    dd.status = 2,                          -- 2=已激活（参考 DiscoveredDeviceStatusEnum）
    dd.update_time = NOW()                  -- 更新时间
WHERE dd.activated = FALSE                  -- 只更新未标记的
  AND d.id IS NOT NULL;                     -- 且设备已注册

-- 查看更新结果
SELECT ROW_COUNT() AS '更新的记录数';

-- 2.2 验证修复结果
SELECT 
    dd.id AS '发现记录ID',
    dd.ip AS 'IP地址',
    dd.vendor AS '制造商',
    dd.activated AS '已激活标记',
    dd.activated_device_id AS '设备ID',
    dd.status AS '状态',
    d.device_name AS '设备名称',
    d.state AS '设备状态'
FROM iot_discovered_device dd
INNER JOIN iot_device d ON dd.ip = d.ip
WHERE dd.activated = TRUE
  AND dd.update_time >= DATE_SUB(NOW(), INTERVAL 1 MINUTE)  -- 最近1分钟更新的
ORDER BY dd.update_time DESC;

-- 如果结果正确，提交事务
COMMIT;

-- 如果结果不正确，回滚事务
-- ROLLBACK;


-- ============================================================
-- 3. 清理历史数据（可选）
-- ============================================================

-- 3.1 查看历史已激活设备（超过30天的）
SELECT 
    COUNT(*) AS '30天前已激活的设备数',
    MIN(discovery_time) AS '最早发现时间',
    MAX(discovery_time) AS '最晚发现时间'
FROM iot_discovered_device
WHERE activated = TRUE
  AND activated_time < DATE_SUB(NOW(), INTERVAL 30 DAY);

-- 3.2 删除历史已激活设备（可选，谨慎执行！）
-- DELETE FROM iot_discovered_device
-- WHERE activated = TRUE
--   AND activated_time < DATE_SUB(NOW(), INTERVAL 30 DAY);


-- ============================================================
-- 4. 验证查询：确认发现列表只显示未激活的设备
-- ============================================================

-- 这是 getUnaddedDevices() 的查询逻辑
SELECT 
    id,
    ip,
    vendor,
    model,
    serial_number,
    http_port,
    rtsp_port,
    onvif_port,
    discovery_time,
    activated,
    status
FROM iot_discovered_device
WHERE added = FALSE
  AND activated = FALSE  -- ✅ 已激活的设备不会显示
ORDER BY discovery_time DESC;

-- 预期结果：只显示未激活的设备
-- 如果还有已激活的设备，说明修复失败


-- ============================================================
-- 5. 统计信息
-- ============================================================

SELECT 
    COUNT(*) AS '总发现设备数',
    SUM(CASE WHEN activated = TRUE THEN 1 ELSE 0 END) AS '已激活数',
    SUM(CASE WHEN activated = FALSE THEN 1 ELSE 0 END) AS '未激活数',
    SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) AS '已忽略数'
FROM iot_discovered_device;


-- ============================================================
-- 6. 常见问题排查
-- ============================================================

-- 问题1：设备在平台注册了，但IP地址不匹配
-- 原因：设备激活后IP地址发生变化，或者使用不同的IP
-- 解决：手动更新 iot_device 表的 ip 字段，或者重新激活设备

SELECT 
    d.id AS '设备ID',
    d.device_name AS '设备名称',
    d.ip AS '平台IP',
    dd.ip AS '发现IP',
    d.state AS '状态',
    dd.discovery_time AS '发现时间'
FROM iot_device d
LEFT JOIN iot_discovered_device dd ON d.ip = dd.ip
WHERE d.product_id IN (3, 61, 62)  -- 摄像头产品
  AND dd.id IS NULL                -- 找不到对应的发现记录
ORDER BY d.create_time DESC
LIMIT 10;


-- 问题2：设备激活失败，但在发现列表中消失了
-- 原因：新逻辑在激活时立即标记，即使激活失败也会标记
-- 解决：重新标记为未激活

-- UPDATE iot_discovered_device
-- SET activated = FALSE,
--     activated_device_id = NULL,
--     activated_time = NULL,
--     status = 1
-- WHERE ip = '192.168.1.XXX';  -- 替换为实际IP


-- ============================================================
-- 执行完成
-- ============================================================
-- 
-- 执行后：
-- 1. 刷新前端"设备发现"页面
-- 2. 确认已激活的设备不再显示
-- 3. 如果还有问题，查看后端日志：
--    tail -f logs/application.log | grep "markAsActivated"
--
-- ============================================================





