-- 更新防区表，将所有防区分配到对应主机的第一个分区
-- 执行此脚本前请备份数据库！

-- 方法1：使用子查询更新（适用于MySQL 5.7+）
UPDATE iot_alarm_zone z
SET z.partition_id = (
    SELECT p.id
    FROM iot_alarm_partition p
    WHERE p.host_id = z.host_id
    ORDER BY p.partition_no ASC
    LIMIT 1
)
WHERE z.partition_id IS NULL;

-- 查看更新结果
SELECT 
    z.id AS zone_id,
    z.zone_no,
    z.zone_name,
    z.host_id,
    z.partition_id,
    p.partition_no,
    p.partition_name
FROM iot_alarm_zone z
LEFT JOIN iot_alarm_partition p ON z.partition_id = p.id
ORDER BY z.host_id, z.zone_no;
