-- =============================================
-- 长辉设备类型数据字典初始化脚本
-- 用于将硬编码的设备类型改为可配置的数据字典
-- =============================================

-- 1) 创建字典类型
INSERT INTO `system_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `deleted_time`)
SELECT '长辉设备类型', 'changhui_device_type', 0, '长辉/德通TCP协议设备的子类型', 'system', NOW(), 'system', NOW(), b'0', NULL
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_type` WHERE `type` = 'changhui_device_type');

-- 2) 创建字典数据
-- 颜色说明：primary=蓝色（闸门类）, success=绿色（仪表类）, warning=橙色（传感器类）

-- 闸门类设备
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, '测控一体化闸门', '1', 'changhui_device_type', 0, 'primary', '', '一体化控制闸门', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type` = 'changhui_device_type' AND `value` = '1');

INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 2, '测控分体式闸门', '2', 'changhui_device_type', 0, 'primary', '', '分体式控制闸门', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type` = 'changhui_device_type' AND `value` = '2');

INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 3, '退水闸', '3', 'changhui_device_type', 0, 'primary', '', '退水闸', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type` = 'changhui_device_type' AND `value` = '3');

INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 4, '节制闸', '4', 'changhui_device_type', 0, 'primary', '', '节制闸', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type` = 'changhui_device_type' AND `value` = '4');

INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 5, '进水闸', '5', 'changhui_device_type', 0, 'primary', '', '进水闸', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type` = 'changhui_device_type' AND `value` = '5');

-- 仪表类设备
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 6, '水位计', '6', 'changhui_device_type', 0, 'success', '', '水位测量仪', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type` = 'changhui_device_type' AND `value` = '6');

INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 7, '流量计', '7', 'changhui_device_type', 0, 'success', '', '流量测量仪', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type` = 'changhui_device_type' AND `value` = '7');

INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 8, '流速仪', '8', 'changhui_device_type', 0, 'success', '', '流速测量仪', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type` = 'changhui_device_type' AND `value` = '8');

-- 传感器类设备
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 9, '渗压计', '9', 'changhui_device_type', 0, 'warning', '', '渗压传感器', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type` = 'changhui_device_type' AND `value` = '9');

INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 10, '荷载计', '10', 'changhui_device_type', 0, 'warning', '', '荷载传感器', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type` = 'changhui_device_type' AND `value` = '10');

INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 11, '温度计', '11', 'changhui_device_type', 0, 'warning', '', '温度传感器', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type` = 'changhui_device_type' AND `value` = '11');

-- 控制设备
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 12, '启闭机', '12', 'changhui_device_type', 0, 'danger', '', '闸门启闭控制机', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type` = 'changhui_device_type' AND `value` = '12');

-- =============================================
-- 执行完毕后，可在"系统管理 > 字典管理"中维护
-- 新增设备类型只需添加新的字典数据即可
-- =============================================
