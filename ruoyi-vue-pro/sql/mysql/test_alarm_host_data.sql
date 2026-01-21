-- =============================================
-- 报警主机测试数据
-- 用于测试分区和防区钻取功能
-- =============================================

USE ch_ibms;

-- 插入测试分区数据（假设主机ID为109）
INSERT INTO iot_alarm_partition (host_id, partition_no, partition_name, status, description, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES 
(109, 1, '一楼分区', 0, '一楼所有防区', '', NOW(), '', NOW(), 0, 0),
(109, 2, '二楼分区', 1, '二楼所有防区', '', NOW(), '', NOW(), 0, 0)
ON DUPLICATE KEY UPDATE 
  partition_name = VALUES(partition_name),
  status = VALUES(status),
  description = VALUES(description);

-- 插入测试防区数据（假设主机ID为109）
INSERT INTO iot_alarm_zone (host_id, zone_no, zone_name, zone_type, area_location, zone_status, online_status, is_important, is_24h, alarm_count, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES 
(109, 1, '前门', 'DOOR', '一楼大堂', 'DISARM', 1, 1, 0, 0, '主入口门磁', '', NOW(), '', NOW(), 0, 0),
(109, 2, '后门', 'DOOR', '一楼后门', 'DISARM', 1, 1, 0, 0, '后门门磁', '', NOW(), '', NOW(), 0, 0),
(109, 3, '大堂红外', 'PIR', '一楼大堂', 'DISARM', 1, 0, 0, 0, '大堂红外探测器', '', NOW(), '', NOW(), 0, 0),
(109, 4, '走廊红外', 'PIR', '一楼走廊', 'DISARM', 1, 0, 0, 0, '走廊红外探测器', '', NOW(), '', NOW(), 0, 0),
(109, 5, '烟雾探测器', 'SMOKE', '一楼大堂', 'DISARM', 1, 1, 1, 0, '烟雾报警器', '', NOW(), '', NOW(), 0, 0),
(109, 6, '燃气探测器', 'GAS', '一楼厨房', 'DISARM', 1, 1, 1, 0, '燃气泄漏探测器', '', NOW(), '', NOW(), 0, 0),
(109, 7, '二楼门磁', 'DOOR', '二楼入口', 'ARM', 1, 1, 0, 0, '二楼门磁', '', NOW(), '', NOW(), 0, 0),
(109, 8, '二楼红外', 'PIR', '二楼走廊', 'ARM', 1, 0, 0, 1, '二楼红外探测器', '', NOW(), '', NOW(), 0, 0)
ON DUPLICATE KEY UPDATE 
  zone_name = VALUES(zone_name),
  zone_type = VALUES(zone_type),
  area_location = VALUES(area_location),
  zone_status = VALUES(zone_status),
  online_status = VALUES(online_status);

-- 查看插入结果
SELECT '分区数据:' AS info;
SELECT * FROM iot_alarm_partition WHERE host_id = 109;

SELECT '防区数据:' AS info;
SELECT * FROM iot_alarm_zone WHERE host_id = 109;
