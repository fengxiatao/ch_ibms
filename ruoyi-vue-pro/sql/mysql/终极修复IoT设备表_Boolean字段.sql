-- ================================================
-- IoT 设备表 - Boolean 字段终极修复
-- ================================================
-- 说明：处理所有可能的类型转换情况
-- 1. VARCHAR → BIT(1)
-- 2. TINYINT → BIT(1)
-- 3. INT → BIT(1)
-- 4. 清理异常数据
-- 日期：2025-10-27
-- ================================================

USE `ruoyi-vue-pro`;

-- ============================================
-- 第1步：检查当前字段类型
-- ============================================
SELECT '【第1步】检查当前字段类型' AS '执行步骤';

SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_TYPE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ruoyi-vue-pro'
  AND TABLE_NAME = 'iot_device'
  AND COLUMN_NAME IN ('subsystem_override', 'menu_override');

-- ============================================
-- 第2步：清理异常数据（如果字段已存在）
-- ============================================
SELECT '【第2步】清理异常数据' AS '执行步骤';

-- 检查字段是否存在
SET @subsystem_override_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'ruoyi-vue-pro' 
      AND TABLE_NAME = 'iot_device' 
      AND COLUMN_NAME = 'subsystem_override'
);

SET @menu_override_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'ruoyi-vue-pro' 
      AND TABLE_NAME = 'iot_device' 
      AND COLUMN_NAME = 'menu_override'
);

-- 如果字段存在，清理数据
-- 将 'true'/'false' 字符串转为 1/0
-- 将 NULL 转为 0
UPDATE `iot_device`
SET 
    subsystem_override = CASE 
        WHEN subsystem_override = 'true' OR subsystem_override = '1' OR subsystem_override = 1 THEN 1
        WHEN subsystem_override = 'false' OR subsystem_override = '0' OR subsystem_override = 0 THEN 0
        WHEN subsystem_override IS NULL THEN 0
        ELSE 0
    END
WHERE @subsystem_override_exists > 0;

UPDATE `iot_device`
SET 
    menu_override = CASE 
        WHEN menu_override = 'true' OR menu_override = '1' OR menu_override = 1 THEN 1
        WHEN menu_override = 'false' OR menu_override = '0' OR menu_override = 0 THEN 0
        WHEN menu_override IS NULL THEN 0
        ELSE 0
    END
WHERE @menu_override_exists > 0;

SELECT '数据清理完成' AS '结果';

-- ============================================
-- 第3步：修改字段类型为 BIT(1)
-- ============================================
SELECT '【第3步】修改字段类型为 BIT(1)' AS '执行步骤';

-- 方案A：如果字段已存在，修改类型
SET @alter_subsystem = IF(@subsystem_override_exists > 0,
    'ALTER TABLE `iot_device` MODIFY COLUMN `subsystem_override` bit(1) NULL DEFAULT b''0'' COMMENT ''是否手动覆盖子系统归属（0=继承产品 1=手动设置）''',
    'SELECT ''subsystem_override字段不存在，跳过'' AS 结果'
);

SET @alter_menu = IF(@menu_override_exists > 0,
    'ALTER TABLE `iot_device` MODIFY COLUMN `menu_override` bit(1) NULL DEFAULT b''0'' COMMENT ''是否覆盖产品菜单配置（0=继承产品 1=手动覆盖）''',
    'SELECT ''menu_override字段不存在，跳过'' AS 结果'
);

-- 执行修改
PREPARE stmt1 FROM @alter_subsystem;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

PREPARE stmt2 FROM @alter_menu;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- 方案B：如果字段不存在，添加字段
SET @add_subsystem = IF(@subsystem_override_exists = 0,
    'ALTER TABLE `iot_device` ADD COLUMN `subsystem_override` bit(1) NULL DEFAULT b''0'' COMMENT ''是否手动覆盖子系统归属'' AFTER `device_type`',
    'SELECT ''subsystem_override字段已存在，跳过添加'' AS 结果'
);

SET @add_menu = IF(@menu_override_exists = 0,
    'ALTER TABLE `iot_device` ADD COLUMN `menu_override` bit(1) NULL DEFAULT b''0'' COMMENT ''是否覆盖产品菜单配置'' AFTER `subsystem_code`',
    'SELECT ''menu_override字段已存在，跳过添加'' AS 结果'
);

PREPARE stmt3 FROM @add_subsystem;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

PREPARE stmt4 FROM @add_menu;
EXECUTE stmt4;
DEALLOCATE PREPARE stmt4;

SELECT '字段类型修改完成' AS '结果';

-- ============================================
-- 第4步：验证修复结果
-- ============================================
SELECT '【第4步】验证修复结果' AS '执行步骤';

SELECT 
    COLUMN_NAME AS '字段名',
    DATA_TYPE AS '数据类型',
    COLUMN_TYPE AS '完整类型',
    COLUMN_DEFAULT AS '默认值',
    CASE 
        WHEN DATA_TYPE = 'bit' AND COLUMN_TYPE = 'bit(1)' THEN '✅ 正确'
        ELSE '❌ 仍有问题'
    END AS '状态'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ruoyi-vue-pro'
  AND TABLE_NAME = 'iot_device'
  AND COLUMN_NAME IN ('subsystem_override', 'menu_override');

-- 检查数据
SELECT 
    '数据检查（前5条）' AS '检查项';

SELECT 
    id,
    device_name,
    subsystem_override AS 'subsystem (0/1)',
    menu_override AS 'menu (0/1)'
FROM iot_device
LIMIT 5;

-- ================================================
-- 执行完成提示
-- ================================================
SELECT '✅ Boolean 字段终极修复完成！' AS message;
SELECT '💡 请重启后端服务以生效' AS reminder;














