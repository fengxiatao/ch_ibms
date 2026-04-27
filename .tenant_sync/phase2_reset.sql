-- 复位首跑失败后 t162 的部分写入：
-- 1) campus/building/floor/area/ibms_space 在首跑前 t162 行数为 0/0/0/0/4，回滚到原状态
-- 2) ibms_device/channel/runtime 首跑未到达，已是空（实际原状态：19/39/4），从备份恢复
SET NAMES utf8mb4;

DELETE FROM area     WHERE tenant_id=162;
DELETE FROM floor    WHERE tenant_id=162;
DELETE FROM building WHERE tenant_id=162;
DELETE FROM campus   WHERE tenant_id=162;

DELETE FROM ibms_space          WHERE tenant_id=162;
INSERT INTO ibms_space          SELECT * FROM bak_ibms_space_t162_20260426;

DELETE FROM ibms_device         WHERE tenant_id=162;
INSERT INTO ibms_device         SELECT * FROM bak_ibms_device_t162_20260426;

DELETE FROM ibms_channel        WHERE tenant_id=162;
INSERT INTO ibms_channel        SELECT * FROM bak_ibms_channel_t162_20260426;

DELETE FROM ibms_device_runtime WHERE tenant_id=162;
INSERT INTO ibms_device_runtime SELECT * FROM bak_ibms_device_runtime_t162_20260426;

SELECT 'campus' tbl,COUNT(*) cnt FROM campus WHERE tenant_id=162
UNION ALL SELECT 'building',COUNT(*) FROM building WHERE tenant_id=162
UNION ALL SELECT 'floor',COUNT(*) FROM floor WHERE tenant_id=162
UNION ALL SELECT 'area',COUNT(*) FROM area WHERE tenant_id=162
UNION ALL SELECT 'ibms_space',COUNT(*) FROM ibms_space WHERE tenant_id=162
UNION ALL SELECT 'ibms_device',COUNT(*) FROM ibms_device WHERE tenant_id=162
UNION ALL SELECT 'ibms_channel',COUNT(*) FROM ibms_channel WHERE tenant_id=162
UNION ALL SELECT 'ibms_device_runtime',COUNT(*) FROM ibms_device_runtime WHERE tenant_id=162;
