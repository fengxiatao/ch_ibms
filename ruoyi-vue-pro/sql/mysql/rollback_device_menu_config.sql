-- ========================================
-- 回滚设备菜单配置功能
-- 版本：1.0
-- 日期：2025-10-24
-- 警告：此操作将删除所有设备的菜单配置数据，请谨慎执行！
-- ========================================

USE `ruoyi-vue-pro`;

-- 1. 备份提示
-- 请在执行回滚前确保已备份数据库
-- mysqldump -u root -p ruoyi-vue-pro > backup_before_rollback.sql

-- 2. 删除索引
ALTER TABLE `iot_device` DROP INDEX `idx_menu_override`;

-- 3. 删除字段
ALTER TABLE `iot_device` 
DROP COLUMN `menu_override`,
DROP COLUMN `primary_menu_id`,
DROP COLUMN `menu_ids`;

-- 4. 验证回滚
SELECT 
    COUNT(*) as column_count
FROM 
    information_schema.COLUMNS 
WHERE 
    TABLE_SCHEMA = 'ruoyi-vue-pro' 
    AND TABLE_NAME = 'iot_device' 
    AND COLUMN_NAME IN ('menu_ids', 'primary_menu_id', 'menu_override');
-- 预期结果：column_count = 0

-- ========================================
-- 回滚完成
-- ========================================



