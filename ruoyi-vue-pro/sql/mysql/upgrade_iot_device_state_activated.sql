-- =============================================
-- 升级脚本：添加设备状态"已激活"字典项
-- 
-- 设备状态枚举（IotDeviceStateEnum）:
--   0 = 未激活 (INACTIVE)  - 设备已添加但从未连接
--   1 = 在线 (ONLINE)      - 设备当前在线
--   2 = 离线 (OFFLINE)     - 设备连接断开
--   3 = 已激活 (ACTIVATED) - 被动连接设备首次心跳后的状态
--
-- 执行前请先检查是否已存在该字典项
-- =============================================

-- 添加设备状态"已激活"字典项
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 3, '已激活', '3', 'iot_device_state', 0, 'primary', '', '被动连接设备首次心跳后的状态', '1', NOW(), '1', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `system_dict_data` 
    WHERE `dict_type` = 'iot_device_state' AND `value` = '3' AND `deleted` = b'0'
);

-- 验证结果
SELECT * FROM `system_dict_data` WHERE `dict_type` = 'iot_device_state' AND `deleted` = b'0' ORDER BY `sort`;
