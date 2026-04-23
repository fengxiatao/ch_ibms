-- 将原 IoT「设备发现」页切到 IBMS 视图目录，并隐藏与 IBMS 重复/已弃用的 IoT 设备·产品·通道相关菜单。
-- 在目标库执行后，需刷新后台菜单缓存（重新登录或清 Redis 菜单缓存）。
-- visible：与前端 routerHelper `hidden: !route.visible` 一致，visible=1 表示在侧边栏展示，0 表示隐藏。

USE ch_ibms;

-- 设备发现 → IBMS 页面组件（若历史数据为软删，一并恢复为未删除）
UPDATE `system_menu`
SET `component` = 'ibms/discovery/index',
    `component_name` = 'IbmsDeviceDiscovery',
    `deleted` = b'0',
    `visible` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `component` = 'iot/device/discovery/index';

-- 下线原 IoT 产品/设备/通道及同目录下配置页等（前端已移除对应 views）
UPDATE `system_menu`
SET `visible` = b'0',
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND `component` IN (
    'iot/product/product/index',
    'iot/product/category/index',
    'iot/device/device/index',
    'iot/device/group/index',
    'iot/channel/index',
    'iot/device/config/index',
    'iot/device/control/index',
    'iot/device/event/index'
  );
