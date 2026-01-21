-- =============================================
-- 修复IoT表结构以匹配DO定义
-- 创建时间: 2025-10-25
-- =============================================

USE ch_ibms;

-- =============================================
-- 1. 修复 iot_device 表
-- =============================================

ALTER TABLE `iot_device`
-- 基础字段
ADD COLUMN `nickname` varchar(64) NULL COMMENT '设备备注名称' AFTER `device_name`,
ADD COLUMN `serial_number` varchar(64) NULL COMMENT '设备序列号' AFTER `nickname`,
ADD COLUMN `pic_url` varchar(255) NULL COMMENT '设备图片' AFTER `serial_number`,
ADD COLUMN `group_ids` varchar(500) NULL COMMENT '设备分组编号集合（JSON）' AFTER `pic_url`,

-- 产品关联
ADD COLUMN `product_key` varchar(64) NULL COMMENT '产品标识（冗余）' AFTER `product_id`,
ADD COLUMN `device_type` tinyint NULL COMMENT '设备类型（冗余）' AFTER `product_key`,

-- 子系统
ADD COLUMN `subsystem_code` varchar(100) NULL COMMENT '所属子系统代码' AFTER `device_type`,
ADD COLUMN `subsystem_override` bit(1) DEFAULT b'0' COMMENT '是否手动覆盖子系统归属' AFTER `subsystem_code`,

-- 菜单
ADD COLUMN `menu_ids` varchar(500) NULL COMMENT '关联的菜单ID列表（JSON）' AFTER `subsystem_override`,
ADD COLUMN `primary_menu_id` bigint NULL COMMENT '主要菜单ID' AFTER `menu_ids`,
ADD COLUMN `menu_override` bit(1) DEFAULT b'0' COMMENT '是否手动覆盖菜单配置' AFTER `primary_menu_id`,

-- 状态（重命名status为state）
ADD COLUMN `state` tinyint DEFAULT 1 COMMENT '设备状态（0-激活,1-未激活,2-禁用）' AFTER `gateway_id`,
ADD COLUMN `online_time` datetime NULL COMMENT '最后上线时间' AFTER `state`,
ADD COLUMN `offline_time` datetime NULL COMMENT '最后离线时间' AFTER `online_time`,

-- IP和固件
ADD COLUMN `firmware_id` bigint NULL COMMENT '固件编号' AFTER `ip`,

-- 认证
ADD COLUMN `auth_type` varchar(32) NULL COMMENT '认证类型（如一机一密、动态注册）' AFTER `device_secret`,

-- GPS定位
ADD COLUMN `location_type` tinyint NULL COMMENT '定位方式（1-GPS,2-北斗,3-WiFi,4-基站,5-其他）' AFTER `auth_type`,
ADD COLUMN `latitude` decimal(10, 6) NULL COMMENT '设备位置的纬度' AFTER `location_type`,
ADD COLUMN `longitude` decimal(10, 6) NULL COMMENT '设备位置的经度' AFTER `latitude`,
ADD COLUMN `area_id` int NULL COMMENT '地区编码' AFTER `longitude`,
ADD COLUMN `address` varchar(255) NULL COMMENT '设备详细地址' AFTER `area_id`,

-- 室内空间定位
ADD COLUMN `campus_id` bigint NULL COMMENT '所属园区ID' AFTER `address`,
ADD COLUMN `building_id` bigint NULL COMMENT '所属建筑ID' AFTER `campus_id`,
ADD COLUMN `floor_id` bigint NULL COMMENT '所属楼层ID' AFTER `building_id`,
ADD COLUMN `room_id` bigint NULL COMMENT '所属区域ID（房间）' AFTER `floor_id`,
ADD COLUMN `local_x` decimal(10, 2) NULL COMMENT 'X坐标（米）' AFTER `room_id`,
ADD COLUMN `local_y` decimal(10, 2) NULL COMMENT 'Y坐标（米）' AFTER `local_x`,
ADD COLUMN `local_z` decimal(10, 2) NULL COMMENT 'Z坐标（安装高度，米）' AFTER `local_y`,
ADD COLUMN `install_location` varchar(200) NULL COMMENT '安装位置描述' AFTER `local_z`,
ADD COLUMN `install_height_type` varchar(20) NULL COMMENT '安装高度类型' AFTER `install_location`,

-- 配置
ADD COLUMN `config` text NULL COMMENT '设备配置（JSON格式）' AFTER `install_height_type`;

-- 添加索引
ALTER TABLE `iot_device`
ADD INDEX `idx_subsystem` (`subsystem_code`),
ADD INDEX `idx_campus` (`campus_id`),
ADD INDEX `idx_building` (`building_id`),
ADD INDEX `idx_floor` (`floor_id`),
ADD INDEX `idx_room` (`room_id`),
ADD INDEX `idx_state` (`state`);

-- 数据迁移：将status值迁移到state
UPDATE `iot_device` SET `state` = `status` WHERE `status` IS NOT NULL;

-- 数据迁移：将last_online_time迁移到online_time
UPDATE `iot_device` SET `online_time` = `last_online_time` WHERE `last_online_time` IS NOT NULL;

-- 数据迁移：将location迁移到address
UPDATE `iot_device` SET `address` = `location` WHERE `location` IS NOT NULL;

SELECT '[INFO] iot_device表结构已更新' AS message;

-- =============================================
-- 2. 修复 iot_product 表
-- =============================================

ALTER TABLE `iot_product`
-- 子系统
ADD COLUMN `subsystem_code` varchar(100) NULL COMMENT '所属子系统代码' AFTER `codec_type`,

-- 菜单
ADD COLUMN `menu_ids` varchar(500) NULL COMMENT '关联的菜单ID列表（JSON）' AFTER `subsystem_code`,
ADD COLUMN `primary_menu_id` bigint NULL COMMENT '主要菜单ID' AFTER `menu_ids`;

-- 添加索引
ALTER TABLE `iot_product`
ADD INDEX `idx_subsystem` (`subsystem_code`);

SELECT '[INFO] iot_product表结构已更新' AS message;

-- =============================================
-- 3. 验证表结构
-- =============================================

-- 验证iot_device
SELECT 
    '[VERIFY] iot_device表字段统计' AS info,
    COUNT(*) AS total_columns
FROM information_schema.columns
WHERE table_schema = 'ch_ibms'
  AND table_name = 'iot_device';

-- 验证iot_product
SELECT 
    '[VERIFY] iot_product表字段统计' AS info,
    COUNT(*) AS total_columns
FROM information_schema.columns
WHERE table_schema = 'ch_ibms'
  AND table_name = 'iot_product';

-- 检查新增字段
SELECT 
    '[CHECK] iot_device新增字段' AS info,
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_COMMENT
FROM information_schema.columns
WHERE table_schema = 'ch_ibms'
  AND table_name = 'iot_device'
  AND COLUMN_NAME IN (
      'nickname', 'serial_number', 'pic_url', 'group_ids',
      'product_key', 'device_type', 'subsystem_code', 'subsystem_override',
      'menu_ids', 'primary_menu_id', 'menu_override',
      'state', 'online_time', 'offline_time',
      'firmware_id', 'auth_type', 'location_type',
      'latitude', 'longitude', 'area_id', 'address',
      'campus_id', 'building_id', 'floor_id', 'room_id',
      'local_x', 'local_y', 'local_z',
      'install_location', 'install_height_type', 'config'
  );

SELECT 
    '[CHECK] iot_product新增字段' AS info,
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_COMMENT
FROM information_schema.columns
WHERE table_schema = 'ch_ibms'
  AND table_name = 'iot_product'
  AND COLUMN_NAME IN ('subsystem_code', 'menu_ids', 'primary_menu_id');

-- =============================================
-- 4. 完成提示
-- =============================================

SELECT '
[SUCCESS] IoT表结构修复完成！

修复内容：
1. iot_device表：添加31个字段
   - 空间定位：campus_id, building_id, floor_id, room_id, local_x/y/z
   - 子系统和菜单：subsystem_code, menu_ids等
   - 状态和时间：state, online_time, offline_time
   - 其他：nickname, config等

2. iot_product表：添加3个字段
   - subsystem_code
   - menu_ids
   - primary_menu_id

3. 数据迁移：
   - status → state
   - last_online_time → online_time
   - location → address

下一步：
1. 重启应用（让MyBatis重新加载表结构）
2. 测试设备添加功能
3. 测试空间绑定功能

' AS message;

















