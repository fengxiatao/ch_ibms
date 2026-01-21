-- ============================================================
-- 清理设备发现表中的重复IP记录
-- ============================================================
-- 
-- 问题描述：
--   旧逻辑使用24小时去重，导致第二天再次扫描时会插入新记录。
--   结果：数据库中存在多条相同IP的记录。
--
-- 修复方案：
--   1. 代码已修改为"插入或更新"逻辑
--   2. 需要清理历史重复数据
--
-- 执行时间：2025-11-03
-- 作者：AI Assistant
-- ============================================================

-- ⚠️ 执行前请先备份数据！
-- mysqldump -u root -p ibms_db iot_discovered_device > backup_discovered_device_before_dedup.sql

-- 1. 查看重复IP的统计
SELECT 
    ip,
    COUNT(*) AS record_count,
    MIN(discovery_time) AS first_discovered,
    MAX(discovery_time) AS last_discovered,
    GROUP_CONCAT(id ORDER BY discovery_time DESC) AS all_ids
FROM iot_discovered_device
GROUP BY ip
HAVING COUNT(*) > 1
ORDER BY record_count DESC, ip;

-- 预期结果：显示所有有重复记录的IP
-- 例如：
-- ip              | record_count | first_discovered     | last_discovered      | all_ids
-- ----------------|--------------|----------------------|----------------------|---------
-- 192.168.1.173   | 3            | 2025-11-01 09:00:00 | 2025-11-03 09:00:00 | 45,32,18
-- 192.168.1.174   | 2            | 2025-11-02 10:00:00 | 2025-11-03 10:00:00 | 46,33


-- ============================================================
-- 2. 清理策略说明
-- ============================================================
-- 
-- 对于每个重复的IP，保留以下记录：
--   - 如果有已激活的记录 → 保留已激活的那条（activated = true）
--   - 如果都未激活 → 保留最新发现的那条（discovery_time 最大）
--   - 删除其他所有重复记录
--
-- ============================================================


-- ============================================================
-- 3. 执行清理（方案1：保留最新记录）
-- ============================================================

-- 开始事务
START TRANSACTION;

-- 3.1 创建临时表，记录要保留的记录ID
DROP TEMPORARY TABLE IF EXISTS temp_keep_ids;
CREATE TEMPORARY TABLE temp_keep_ids AS
SELECT 
    t1.id,
    t1.ip,
    t1.discovery_time,
    t1.activated
FROM iot_discovered_device t1
INNER JOIN (
    -- 对于每个IP，找出要保留的记录
    SELECT 
        ip,
        CASE 
            -- 优先保留已激活的记录
            WHEN MAX(CASE WHEN activated = TRUE THEN 1 ELSE 0 END) = 1
            THEN MAX(CASE WHEN activated = TRUE THEN id END)
            -- 否则保留最新发现的记录
            ELSE (
                SELECT id 
                FROM iot_discovered_device AS sub 
                WHERE sub.ip = main.ip 
                ORDER BY discovery_time DESC 
                LIMIT 1
            )
        END AS keep_id
    FROM iot_discovered_device main
    GROUP BY ip
) t2 ON t1.id = t2.keep_id;

-- 查看要保留的记录
SELECT 
    '要保留的记录' AS action,
    id,
    ip,
    vendor,
    discovery_time,
    activated,
    status
FROM temp_keep_ids
ORDER BY ip;

-- 3.2 查看要删除的记录（预览）
SELECT 
    '将要删除的记录' AS action,
    dd.id,
    dd.ip,
    dd.vendor,
    dd.discovery_time,
    dd.activated,
    dd.status
FROM iot_discovered_device dd
LEFT JOIN temp_keep_ids tk ON dd.id = tk.id
WHERE tk.id IS NULL
ORDER BY dd.ip, dd.discovery_time DESC;

-- 3.3 执行删除
DELETE dd FROM iot_discovered_device dd
LEFT JOIN temp_keep_ids tk ON dd.id = tk.id
WHERE tk.id IS NULL;

-- 查看删除结果
SELECT ROW_COUNT() AS '删除的记录数';

-- 3.4 验证：确认每个IP只有一条记录
SELECT 
    ip,
    COUNT(*) AS record_count
FROM iot_discovered_device
GROUP BY ip
HAVING COUNT(*) > 1;

-- 预期结果：空结果集（没有重复IP）

-- 如果结果正确，提交事务
COMMIT;

-- 如果结果不正确，回滚事务
-- ROLLBACK;


-- ============================================================
-- 4. 清理后的统计信息
-- ============================================================

-- 4.1 总记录数
SELECT 
    COUNT(*) AS '总记录数',
    COUNT(DISTINCT ip) AS '唯一IP数',
    SUM(CASE WHEN activated = TRUE THEN 1 ELSE 0 END) AS '已激活数',
    SUM(CASE WHEN activated = FALSE THEN 1 ELSE 0 END) AS '未激活数'
FROM iot_discovered_device;

-- 4.2 按状态统计
SELECT 
    status,
    CASE status
        WHEN 1 THEN '已发现'
        WHEN 2 THEN '已激活'
        WHEN 3 THEN '已忽略'
        ELSE '未知'
    END AS status_name,
    COUNT(*) AS count
FROM iot_discovered_device
GROUP BY status
ORDER BY status;

-- 4.3 最近发现的设备
SELECT 
    ip,
    vendor,
    model,
    discovery_time,
    activated,
    status
FROM iot_discovered_device
ORDER BY discovery_time DESC
LIMIT 20;


-- ============================================================
-- 5. 验证新逻辑（可选测试）
-- ============================================================

-- 5.1 查看当前所有记录
SELECT 
    id,
    ip,
    vendor,
    discovery_time,
    activated
FROM iot_discovered_device
ORDER BY ip, discovery_time DESC;

-- 5.2 模拟第二天再次发现同一设备
-- （新逻辑应该更新现有记录，而不是插入新记录）
-- 这个由代码测试验证，SQL无法直接模拟


-- ============================================================
-- 6. 清理临时表
-- ============================================================

DROP TEMPORARY TABLE IF EXISTS temp_keep_ids;


-- ============================================================
-- 执行完成
-- ============================================================
-- 
-- 执行后：
-- 1. 每个IP只有一条记录
-- 2. 优先保留已激活的记录
-- 3. 否则保留最新发现的记录
-- 4. 重启后端服务，新逻辑生效
-- 5. 测试：扫描设备，验证不会产生重复记录
--
-- ============================================================





