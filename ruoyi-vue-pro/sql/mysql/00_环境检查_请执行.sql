-- =============================================
-- 智慧安防环境检查SQL
-- 请使用MySQL MCP工具执行本文件的SQL
-- =============================================

-- =============================================
-- 检查1: 智慧安防菜单情况
-- =============================================
SELECT 
    '=== 检查1: 智慧安防菜单 ===' AS 检查项;

SELECT 
    id AS 菜单ID,
    name AS 菜单名称,
    parent_id AS 父菜单ID,
    type AS 类型,
    path AS 路径
FROM system_menu 
WHERE name LIKE '%安防%' 
   OR path LIKE '%security%'
   OR component LIKE '%security%'
ORDER BY id;

SELECT 
    COUNT(*) AS 安防菜单总数,
    MIN(id) AS 最小ID,
    MAX(id) AS 最大ID
FROM system_menu 
WHERE (name LIKE '%安防%' OR path LIKE '%security%')
  AND deleted = 0;

-- =============================================
-- 检查2: IoT产品情况
-- =============================================
SELECT 
    '=== 检查2: IoT产品（摄像头） ===' AS 检查项;

SELECT 
    id AS 产品ID,
    name AS 产品名称,
    product_key AS 产品标识,
    device_type AS 设备类型,
    status AS 状态
FROM iot_product 
WHERE name LIKE '%大华%' 
   OR name LIKE '%摄像头%'
   OR name LIKE '%camera%';

-- =============================================
-- 检查3: 安防数据表情况
-- =============================================
SELECT 
    '=== 检查3: 安防数据表 ===' AS 检查项;

SELECT 
    table_name AS 表名,
    table_rows AS 预估行数,
    ROUND(data_length/1024/1024, 2) AS 数据大小MB
FROM information_schema.tables 
WHERE table_schema = 'ch_ibms' 
  AND table_name LIKE 'security_%'
ORDER BY table_name;

SELECT 
    COUNT(*) AS 安防表总数
FROM information_schema.tables 
WHERE table_schema = 'ch_ibms' 
  AND table_name LIKE 'security_%';

-- =============================================
-- 检查4: 空间管理数据（MySQL部分）
-- =============================================
SELECT 
    '=== 检查4: 空间管理基础数据 ===' AS 检查项;

-- 检查是否有iot_space相关表
SELECT table_name AS 空间表
FROM information_schema.tables 
WHERE table_schema = 'ch_ibms' 
  AND (table_name LIKE '%space%' OR table_name LIKE '%area%');

-- =============================================
-- 检查5: IoT设备情况
-- =============================================
SELECT 
    '=== 检查5: IoT设备 ===' AS 检查项;

SELECT 
    COUNT(*) AS 设备总数,
    SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS 在线设备,
    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS 离线设备
FROM iot_device
WHERE deleted = 0;

-- 查看是否已有摄像头设备
SELECT 
    id AS 设备ID,
    name AS 设备名称,
    device_key AS 设备标识,
    product_id AS 产品ID,
    status AS 状态
FROM iot_device
WHERE name LIKE '%摄像头%'
   OR name LIKE '%camera%'
   OR device_key LIKE '%camera%'
LIMIT 5;

-- =============================================
-- 总结
-- =============================================
SELECT 
    '=== 检查完成 ===' AS 提示,
    '请查看以上5项检查结果' AS 说明;

















