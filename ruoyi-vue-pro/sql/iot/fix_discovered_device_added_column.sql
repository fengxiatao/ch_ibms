-- ============================================================================
-- 修复 iot_discovered_device 表 added 字段数据类型问题
-- 
-- 问题：added 字段可能定义为 VARCHAR 或其他类型，导致数据截断错误
-- 解决：统一修改为 BIT(1) 类型，与 Java Boolean 类型匹配
--
-- 错误信息：Data too long for column 'added' at row 1
-- ============================================================================

-- 1. 检查当前 added 字段的定义
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'iot_discovered_device' 
  AND COLUMN_NAME = 'added';

-- 2. 如果 added 字段不是 BIT(1) 类型，则修改为正确的类型
-- 注意：先备份数据，然后修改字段类型
ALTER TABLE `iot_discovered_device` 
MODIFY COLUMN `added` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否已添加到平台';

-- 3. 同时检查和修复 activated 字段（如果存在类似问题）
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'iot_discovered_device' 
  AND COLUMN_NAME = 'activated';

-- 4. 如果 activated 字段存在且不是 BIT(1) 类型，则修改
-- 首先检查该字段是否存在
SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'iot_discovered_device' 
      AND COLUMN_NAME = 'activated'
);

-- 如果字段存在，则修改类型
SET @sql = IF(@column_exists > 0, 
    'ALTER TABLE `iot_discovered_device` MODIFY COLUMN `activated` BIT(1) NOT NULL DEFAULT b\'0\' COMMENT \'是否已激活\'',
    'SELECT "activated column does not exist" as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5. 清理可能的无效数据
-- 将非布尔值转换为布尔值
UPDATE `iot_discovered_device` 
SET `added` = CASE 
    WHEN `added` IS NULL OR `added` = '' OR `added` = '0' OR `added` = 'false' THEN b'0'
    ELSE b'1'
END;

-- 6. 如果 activated 字段存在，也进行类似清理
UPDATE `iot_discovered_device` 
SET `activated` = CASE 
    WHEN `activated` IS NULL OR `activated` = '' OR `activated` = '0' OR `activated` = 'false' THEN b'0'
    ELSE b'1'
END
WHERE EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'iot_discovered_device' 
      AND COLUMN_NAME = 'activated'
);

-- 7. 验证修复结果
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'iot_discovered_device' 
  AND COLUMN_NAME IN ('added', 'activated')
ORDER BY COLUMN_NAME;

-- 8. 检查数据样本
SELECT id, ip, added, activated, status, discovery_time 
FROM `iot_discovered_device` 
ORDER BY id DESC 
LIMIT 5;

-- ============================================================================
-- 执行说明：
-- 1. 先执行查询语句检查当前字段定义
-- 2. 如果 added 字段不是 BIT(1) 类型，执行 ALTER TABLE 语句
-- 3. 执行数据清理语句
-- 4. 验证修复结果
-- ============================================================================
