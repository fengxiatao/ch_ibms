-- ================================================
-- IoT 设备表 - 修复 Boolean 字段类型
-- ================================================
-- 说明：修复 subsystem_override 和 menu_override 字段类型不匹配问题
-- 原因：Java 中定义为 Boolean，数据库中可能是 VARCHAR
-- 日期：2025-10-27
-- ================================================

USE `ruoyi-vue-pro`;

-- 检查当前字段类型
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ruoyi-vue-pro'
  AND TABLE_NAME = 'iot_device'
  AND COLUMN_NAME IN ('subsystem_override', 'menu_override');

-- 修改 subsystem_override 字段为 BIT(1)
ALTER TABLE `iot_device`
MODIFY COLUMN `subsystem_override` bit(1) NULL DEFAULT b'0' COMMENT '是否手动覆盖子系统归属（0=继承产品 1=手动设置）';

-- 修改 menu_override 字段为 BIT(1)
ALTER TABLE `iot_device`
MODIFY COLUMN `menu_override` bit(1) NULL DEFAULT b'0' COMMENT '是否覆盖产品菜单配置（0=继承产品 1=手动覆盖）';

-- 验证修改后的字段类型
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ruoyi-vue-pro'
  AND TABLE_NAME = 'iot_device'
  AND COLUMN_NAME IN ('subsystem_override', 'menu_override');

-- ================================================
-- 执行完成提示
-- ================================================
SELECT '✅ Boolean 字段类型修复成功！' AS message;














