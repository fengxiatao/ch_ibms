-- =============================================
-- 初始化子系统数据（从菜单表同步）
-- 创建时间: 2025-11-03
-- 说明: 将IBMS专业系统菜单同步到 iot_subsystem 表
-- =============================================

-- 清空旧数据（如果存在）
TRUNCATE TABLE `iot_subsystem`;

-- =============================================
-- 一级子系统（IBMS主要模块）
-- =============================================

-- 1. 智慧安防（菜单ID: 5000）
INSERT INTO `iot_subsystem` (
  `code`, `name`, `parent_code`, `level`, `menu_id`, `menu_path`, 
  `icon`, `description`, `sort`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`
) VALUES (
  'security', '智慧安防', NULL, 1, 5000, '/security',
  'fa-solid:shield-alt', 'IBMS智慧安防子系统，包括视频监控、人员布控、周界防护等功能', 10, 1,
  '1', NOW(), '1', NOW(), 0
);

-- 2. 智慧通行（预留，根据实际菜单调整）
INSERT INTO `iot_subsystem` (
  `code`, `name`, `parent_code`, `level`, `menu_id`, `menu_path`, 
  `icon`, `description`, `sort`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`
) VALUES (
  'access', '智慧通行', NULL, 1, NULL, '/access',
  'fa:id-card', 'IBMS智慧通行子系统，包括门禁管理、访客管理、考勤管理等功能', 20, 1,
  '1', NOW(), '1', NOW(), 0
);

-- 3. 智慧能源（预留）
INSERT INTO `iot_subsystem` (
  `code`, `name`, `parent_code`, `level`, `menu_id`, `menu_path`, 
  `icon`, `description`, `sort`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`
) VALUES (
  'energy', '智慧能源', NULL, 1, NULL, '/energy',
  'fa:bolt', 'IBMS智慧能源子系统，包括电力监测、照明控制、能耗分析等功能', 30, 1,
  '1', NOW(), '1', NOW(), 0
);

-- 4. 智慧消防（预留）
INSERT INTO `iot_subsystem` (
  `code`, `name`, `parent_code`, `level`, `menu_id`, `menu_path`, 
  `icon`, `description`, `sort`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`
) VALUES (
  'fire', '智慧消防', NULL, 1, NULL, '/fire',
  'fa:fire-extinguisher', 'IBMS智慧消防子系统，包括火灾报警、消防联动、应急疏散等功能', 40, 1,
  '1', NOW(), '1', NOW(), 0
);

-- 5. 智慧环境（预留）
INSERT INTO `iot_subsystem` (
  `code`, `name`, `parent_code`, `level`, `menu_id`, `menu_path`, 
  `icon`, `description`, `sort`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`
) VALUES (
  'environment', '智慧环境', NULL, 1, NULL, '/environment',
  'fa:leaf', 'IBMS智慧环境子系统，包括空调控制、空气质量监测、温湿度管理等功能', 50, 1,
  '1', NOW(), '1', NOW(), 0
);

-- =============================================
-- 二级子系统（智慧安防的子模块）
-- =============================================

-- 1. 视频监控（菜单ID: 5010）
INSERT INTO `iot_subsystem` (
  `code`, `name`, `parent_code`, `level`, `menu_id`, `menu_path`, 
  `icon`, `description`, `sort`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`
) VALUES (
  'security.video', '视频监控', 'security', 2, 5010, '/security/video-surveillance',
  'fa:video-camera', '网络摄像头监控、实时预览、录像回放、抓拍记录', 1, 1,
  '1', NOW(), '1', NOW(), 0
);

-- 2. 人员布控（菜单ID: 5020）
INSERT INTO `iot_subsystem` (
  `code`, `name`, `parent_code`, `level`, `menu_id`, `menu_path`, 
  `icon`, `description`, `sort`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`
) VALUES (
  'security.personnel', '人员布控', 'security', 2, 5020, '/security/personnel-control',
  'fa:users', '人脸识别、人员库管理、布控任务、抓拍记录', 2, 1,
  '1', NOW(), '1', NOW(), 0
);

-- 3. 周界防护（菜单ID: 5030）
INSERT INTO `iot_subsystem` (
  `code`, `name`, `parent_code`, `level`, `menu_id`, `menu_path`, 
  `icon`, `description`, `sort`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`
) VALUES (
  'security.perimeter', '周界防护', 'security', 2, 5030, '/security/perimeter-protection',
  'fa:crosshairs', '周界入侵检测、电子围栏、防区管理', 3, 1,
  '1', NOW(), '1', NOW(), 0
);

-- 4. 入侵报警（菜单ID: 5040）
INSERT INTO `iot_subsystem` (
  `code`, `name`, `parent_code`, `level`, `menu_id`, `menu_path`, 
  `icon`, `description`, `sort`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`
) VALUES (
  'security.intrusion', '入侵报警', 'security', 2, 5040, '/security/intrusion-alarm',
  'fa:bell', '入侵探测器、门磁、红外传感器、报警联动', 4, 1,
  '1', NOW(), '1', NOW(), 0
);

-- 5. 停车管理（菜单ID: 5050）
INSERT INTO `iot_subsystem` (
  `code`, `name`, `parent_code`, `level`, `menu_id`, `menu_path`, 
  `icon`, `description`, `sort`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`
) VALUES (
  'security.parking', '停车管理', 'security', 2, 5050, '/security/parking-management',
  'fa:car', '车牌识别、停车场管理、道闸控制、车位监测', 5, 1,
  '1', NOW(), '1', NOW(), 0
);

-- =============================================
-- 验证数据
-- =============================================

SELECT '✅ 子系统数据初始化完成！' AS message;

SELECT 
    '=== 一级子系统（IBMS主要模块） ===' AS title,
    `code` AS 代码,
    `name` AS 名称,
    `menu_id` AS 菜单ID,
    `menu_path` AS 菜单路径,
    `enabled` AS 启用
FROM `iot_subsystem`
WHERE `level` = 1
ORDER BY `sort`;

SELECT 
    '=== 二级子系统（智慧安防子模块） ===' AS title,
    `code` AS 代码,
    `name` AS 名称,
    `parent_code` AS 父代码,
    `menu_id` AS 菜单ID,
    `menu_path` AS 菜单路径,
    `enabled` AS 启用
FROM `iot_subsystem`
WHERE `level` = 2
ORDER BY `parent_code`, `sort`;

-- =============================================
-- 使用说明
-- =============================================

/*
📋 使用说明：

1. 执行本脚本初始化子系统数据
2. 如果需要添加其他IBMS模块（智慧通行、智慧能源等），请：
   - 先在 system_menu 表中创建对应菜单
   - 更新本脚本中的 menu_id 和 menu_path
   - 重新执行本脚本

3. 子系统代码规范：
   - 一级系统：单词（如 security、access、energy）
   - 二级系统：父系统.子系统（如 security.video、security.personnel）

4. 菜单ID对应关系：
   - 5000: 智慧安防（一级）
   - 5010: 视频监控（二级）
   - 5020: 人员布控（二级）
   - 5030: 周界防护（二级）
   - 5040: 入侵报警（二级）
   - 5050: 停车管理（二级）

5. 后续扩展：
   - 智慧通行、智慧能源、智慧消防等模块待菜单创建后填充
*/




