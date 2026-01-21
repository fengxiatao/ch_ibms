-- IoT 报警主机 - 字典类型与字典数据（MySQL）
-- 执行前请备份。可多次执行，已存在时将跳过。

INSERT INTO `system_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `deleted_time`)
SELECT 'IoT 防区布防状态', 'iot_zone_arm_status', 0, '0撤防 1布防 2旁路', 'system', NOW(), 'system', NOW(), b'0', NULL
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_type` WHERE `type` = 'iot_zone_arm_status');

-- 2) 字典类型：分区布防状态
INSERT INTO `system_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `deleted_time`)
SELECT 'IoT 分区布防状态', 'iot_partition_arm_status', 0, '0撤防 1布防 2居家布防', 'system', NOW(), 'system', NOW(), b'0', NULL
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_type` WHERE `type` = 'iot_partition_arm_status');

-- 3) 字典类型：防区报警状态
INSERT INTO `system_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `deleted_time`)
SELECT 'IoT 防区报警状态', 'iot_zone_alarm_status', 0, '0无报警 1报警中 11~17各类报警', 'system', NOW(), 'system', NOW(), b'0', NULL
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_type` WHERE `type` = 'iot_zone_alarm_status');

-- 4) 字典数据：iot_zone_arm_status
-- 颜色建议：0 撤防=info，1 布防=warning，2 旁路=success
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 0, '撤防', '0', 'iot_zone_arm_status', 0, 'info', '', '防区撤防', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type`='iot_zone_arm_status' AND `value`='0');
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, '布防', '1', 'iot_zone_arm_status', 0, 'warning', '', '防区布防', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type`='iot_zone_arm_status' AND `value`='1');
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 2, '旁路', '2', 'iot_zone_arm_status', 0, 'success', '', '防区旁路', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type`='iot_zone_arm_status' AND `value`='2');

-- 5) 字典数据：iot_partition_arm_status
-- 颜色建议：0 撤防=info，1 布防=warning，2 居家布防=success
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 0, '分区撤防', '0', 'iot_partition_arm_status', 0, 'info', '', '分区撤防', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type`='iot_partition_arm_status' AND `value`='0');
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, '分区布防', '1', 'iot_partition_arm_status', 0, 'warning', '', '分区外出布防', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type`='iot_partition_arm_status' AND `value`='1');
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 2, '分区居家布防', '2', 'iot_partition_arm_status', 0, 'success', '', '分区居家布防', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type`='iot_partition_arm_status' AND `value`='2');

-- 6) 字典数据：iot_zone_alarm_status
-- 颜色建议：0 无报警=success，其他报警=danger
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 0, '无报警', '0', 'iot_zone_alarm_status', 0, 'success', '', '防区无报警', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type`='iot_zone_alarm_status' AND `value`='0');
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, '正在报警', '1', 'iot_zone_alarm_status', 0, 'danger', '', '防区正在报警', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type`='iot_zone_alarm_status' AND `value`='1');
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 11, '剪断报警', '11', 'iot_zone_alarm_status', 0, 'danger', '', '剪断报警', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type`='iot_zone_alarm_status' AND `value`='11');
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 12, '短路报警', '12', 'iot_zone_alarm_status', 0, 'danger', '', '短路报警', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type`='iot_zone_alarm_status' AND `value`='12');
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 13, '触网报警', '13', 'iot_zone_alarm_status', 0, 'danger', '', '触网报警', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type`='iot_zone_alarm_status' AND `value`='13');
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 14, '松弛报警', '14', 'iot_zone_alarm_status', 0, 'danger', '', '松弛报警', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type`='iot_zone_alarm_status' AND `value`='14');
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 15, '拉紧报警', '15', 'iot_zone_alarm_status', 0, 'danger', '', '拉紧报警', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type`='iot_zone_alarm_status' AND `value`='15');
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 16, '攀爬报警', '16', 'iot_zone_alarm_status', 0, 'danger', '', '攀爬报警', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type`='iot_zone_alarm_status' AND `value`='16');
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 17, '开路报警', '17', 'iot_zone_alarm_status', 0, 'danger', '', '开路报警', 'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type`='iot_zone_alarm_status' AND `value`='17');
