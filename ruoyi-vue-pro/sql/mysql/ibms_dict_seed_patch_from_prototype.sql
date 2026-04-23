-- =============================================
-- IBMS 编码规范字典补丁（来自前端原型硬编码）
-- 目的：补齐原型里用到、但 ibms_dict_seed.sql 未覆盖的码表项
-- 可重复执行：已存在时跳过
-- =============================================

-- 补齐：ibms_device_model（设备型号码）
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`,
                               `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT v.`sort`, v.`label`, v.`value`, v.`dict_type`, 0, '', '', v.`remark`,
       'system', NOW(), 'system', NOW(), b'0'
FROM (
         -- 原型新增：消防/广播/电能表相关型号
         SELECT 12 AS `sort`, '烟感探测器' AS `label`, 'SM' AS `value`, 'ibms_device_model' AS `dict_type`,
                '{"system":"FD","desc":"烟雾探测"}' AS `remark`
         UNION ALL
         SELECT 13 AS `sort`, '手报按钮' AS `label`, 'HM' AS `value`, 'ibms_device_model' AS `dict_type`,
                '{"system":"FD","desc":"手动报警"}' AS `remark`
         UNION ALL
         SELECT 14 AS `sort`, '广播分区' AS `label`, 'BC' AS `value`, 'ibms_device_model' AS `dict_type`,
                '{"system":"PA","desc":"广播通道"}' AS `remark`
         UNION ALL
         -- 原型产品使用了 model_code=PM（注意：原型未在 modelTypes 中声明，但产品里确实用了）
         SELECT 15 AS `sort`, '电能表' AS `label`, 'PM' AS `value`, 'ibms_device_model' AS `dict_type`,
                '{"system":"EP","desc":"电能计量仪表"}' AS `remark`
     ) v
         LEFT JOIN `system_dict_data` d
                   ON d.`dict_type` = v.`dict_type` AND d.`value` = v.`value` AND d.`deleted` = b'0'
WHERE d.`id` IS NULL;

