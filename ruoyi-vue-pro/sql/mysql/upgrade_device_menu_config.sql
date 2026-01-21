-- ========================================
-- 设备适用模块/页面配置功能
-- 版本：1.0
-- 日期：2025-10-24
-- 说明：为设备添加 menuIds 配置，支持继承产品配置或自定义配置
-- ========================================

USE `ruoyi-vue-pro`;

-- 1. 添加设备菜单配置字段
ALTER TABLE `iot_device` 
ADD COLUMN `menu_ids` varchar(500) NULL DEFAULT NULL COMMENT '关联的菜单ID列表（JSON数组），示例：[1001,1002]，为空或menu_override=0时继承产品配置' AFTER `product_key`,
ADD COLUMN `primary_menu_id` bigint NULL DEFAULT NULL COMMENT '主要菜单ID，应该是menu_ids中的一个，为空或menu_override=0时继承产品配置' AFTER `menu_ids`,
ADD COLUMN `menu_override` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否手动覆盖菜单配置：0-继承产品（默认） 1-手动设置不跟随产品变更' AFTER `primary_menu_id`;

-- 2. 添加索引（可选，如果需要频繁按 menu_override 查询）
ALTER TABLE `iot_device`
ADD INDEX `idx_menu_override` (`menu_override`) USING BTREE;

-- 3. 验证变更
SELECT 
    COLUMN_NAME, 
    COLUMN_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT, 
    COLUMN_COMMENT 
FROM 
    information_schema.COLUMNS 
WHERE 
    TABLE_SCHEMA = 'ruoyi-vue-pro' 
    AND TABLE_NAME = 'iot_device' 
    AND COLUMN_NAME IN ('menu_ids', 'primary_menu_id', 'menu_override');

-- 4. 统计信息
SELECT 
    COUNT(*) as total_devices,
    SUM(CASE WHEN menu_override = 0 THEN 1 ELSE 0 END) as inherit_count,
    SUM(CASE WHEN menu_override = 1 THEN 1 ELSE 0 END) as override_count
FROM 
    iot_device;

-- ========================================
-- 变更完成
-- ========================================



