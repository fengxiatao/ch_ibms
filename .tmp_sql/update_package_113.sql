-- 更新套餐113（智慧安防）：
-- 1) 追加跨模块必需的 IoT 按钮权限（type=3）
--    - 4003 iot:product:query (通用产品查询)
--    - 4009 iot:device:query (通用设备查询)
--    - 5232 iot:building:query (建筑查询)
--    - 5237 iot:floor:query (楼层查询)
--    - 5242 iot:area:query (区域查询)
--    - 6198 iot:channel:query (通道查询)
--    - 6203 iot:channel:sync (通道同步)
--    - 72568 iot:ibms-product:query (IBMS产品查询，新建)
--    - 72569 iot:ibms-space:query (IBMS空间查询，新建)
-- 2) 设置 excluded_menu_ids=[4000] 排除智慧物联根菜单
--    归一化后将移除4000树下所有非按钮菜单，但保留按钮权限

UPDATE system_tenant_package
SET menu_ids = CONCAT(
      SUBSTRING(menu_ids, 1, LENGTH(menu_ids) - 1),
      ', 4003, 4009, 5232, 5237, 5242, 6198, 6203, 72568, 72569]'
    ),
    excluded_menu_ids = '[4000]',
    update_time = NOW(),
    updater = 'system-hide-iot-module'
WHERE id = 113;
