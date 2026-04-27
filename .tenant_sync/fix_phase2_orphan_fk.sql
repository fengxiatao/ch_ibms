-- ============================================================
-- 修复：Phase 2 同步时被误判"多义保留"的 FK 字段
-- 这些字段配合 *_type 字段实为单义，应按 type 分类映射重写
-- 涉及 4 张表：
--   1) ibms_bac_alarm.device_id            （type: 1=hvac, 2=water）
--   2) ibms_bac_system_log.device_id       （type: 1=hvac, 2=water）
--   3) ibms_lighting_alarm.device_id       （type: 1=circuit, 2=gateway, 3=controller）
--   4) ibms_lighting_operation_log.target_id（type: 1=circuit, 2=scene, 3=gateway, 4=controller）
-- 映射键：使用各业务表的 code 自然键（hvac/water=device_code, lighting_*=*_code）
-- ============================================================

USE ch_ibms;

-- ===== 备份 =====
DROP TABLE IF EXISTS bak_ibms_bac_alarm_devid_t162_20260427;
CREATE TABLE bak_ibms_bac_alarm_devid_t162_20260427 AS SELECT id, device_type, device_id FROM ibms_bac_alarm WHERE tenant_id=162;

DROP TABLE IF EXISTS bak_ibms_bac_system_log_devid_t162_20260427;
CREATE TABLE bak_ibms_bac_system_log_devid_t162_20260427 AS SELECT id, device_type, device_id FROM ibms_bac_system_log WHERE tenant_id=162;

DROP TABLE IF EXISTS bak_ibms_lighting_alarm_devid_t162_20260427;
CREATE TABLE bak_ibms_lighting_alarm_devid_t162_20260427 AS SELECT id, device_type, device_id FROM ibms_lighting_alarm WHERE tenant_id=162;

DROP TABLE IF EXISTS bak_ibms_lighting_op_log_tid_t162_20260427;
CREATE TABLE bak_ibms_lighting_op_log_tid_t162_20260427 AS SELECT id, target_type, target_id FROM ibms_lighting_operation_log WHERE tenant_id=162;

-- ===== 1) ibms_bac_alarm =====
SELECT '=== bac_alarm BEFORE ===' AS m;
SELECT device_type, device_id, COUNT(*) FROM ibms_bac_alarm WHERE tenant_id=162 GROUP BY device_type, device_id ORDER BY device_type, device_id;

UPDATE ibms_bac_alarm a
JOIN ibms_hvac_device s ON s.id=a.device_id AND s.tenant_id=1
JOIN ibms_hvac_device d ON d.device_code=s.device_code AND d.tenant_id=162
SET a.device_id=d.id WHERE a.tenant_id=162 AND a.device_type=1;

UPDATE ibms_bac_alarm a
JOIN ibms_water_device s ON s.id=a.device_id AND s.tenant_id=1
JOIN ibms_water_device d ON d.device_code=s.device_code AND d.tenant_id=162
SET a.device_id=d.id WHERE a.tenant_id=162 AND a.device_type=2;

SELECT '=== bac_alarm AFTER ===' AS m;
SELECT device_type, device_id, COUNT(*) FROM ibms_bac_alarm WHERE tenant_id=162 GROUP BY device_type, device_id ORDER BY device_type, device_id;

-- ===== 2) ibms_bac_system_log =====
SELECT '=== bac_log BEFORE ===' AS m;
SELECT device_type, device_id, COUNT(*) FROM ibms_bac_system_log WHERE tenant_id=162 GROUP BY device_type, device_id ORDER BY device_type, device_id;

UPDATE ibms_bac_system_log a
JOIN ibms_hvac_device s ON s.id=a.device_id AND s.tenant_id=1
JOIN ibms_hvac_device d ON d.device_code=s.device_code AND d.tenant_id=162
SET a.device_id=d.id WHERE a.tenant_id=162 AND a.device_type=1;

UPDATE ibms_bac_system_log a
JOIN ibms_water_device s ON s.id=a.device_id AND s.tenant_id=1
JOIN ibms_water_device d ON d.device_code=s.device_code AND d.tenant_id=162
SET a.device_id=d.id WHERE a.tenant_id=162 AND a.device_type=2;

SELECT '=== bac_log AFTER ===' AS m;
SELECT device_type, device_id, COUNT(*) FROM ibms_bac_system_log WHERE tenant_id=162 GROUP BY device_type, device_id ORDER BY device_type, device_id;

-- ===== 3) ibms_lighting_alarm =====
SELECT '=== light_alarm BEFORE ===' AS m;
SELECT device_type, device_id, COUNT(*) FROM ibms_lighting_alarm WHERE tenant_id=162 GROUP BY device_type, device_id ORDER BY device_type, device_id;

UPDATE ibms_lighting_alarm a
JOIN ibms_lighting_circuit s ON s.id=a.device_id AND s.tenant_id=1
JOIN ibms_lighting_circuit d ON d.circuit_code=s.circuit_code AND d.tenant_id=162
SET a.device_id=d.id WHERE a.tenant_id=162 AND a.device_type=1;

UPDATE ibms_lighting_alarm a
JOIN ibms_lighting_gateway s ON s.id=a.device_id AND s.tenant_id=1
JOIN ibms_lighting_gateway d ON d.gateway_code=s.gateway_code AND d.tenant_id=162
SET a.device_id=d.id WHERE a.tenant_id=162 AND a.device_type=2;

UPDATE ibms_lighting_alarm a
JOIN ibms_lighting_controller s ON s.id=a.device_id AND s.tenant_id=1
JOIN ibms_lighting_controller d ON d.controller_code=s.controller_code AND d.tenant_id=162
SET a.device_id=d.id WHERE a.tenant_id=162 AND a.device_type=3;

SELECT '=== light_alarm AFTER ===' AS m;
SELECT device_type, device_id, COUNT(*) FROM ibms_lighting_alarm WHERE tenant_id=162 GROUP BY device_type, device_id ORDER BY device_type, device_id;

-- ===== 4) ibms_lighting_operation_log =====
SELECT '=== light_op_log BEFORE ===' AS m;
SELECT target_type, target_id, COUNT(*) FROM ibms_lighting_operation_log WHERE tenant_id=162 GROUP BY target_type, target_id ORDER BY target_type, target_id;

UPDATE ibms_lighting_operation_log a
JOIN ibms_lighting_circuit s ON s.id=a.target_id AND s.tenant_id=1
JOIN ibms_lighting_circuit d ON d.circuit_code=s.circuit_code AND d.tenant_id=162
SET a.target_id=d.id WHERE a.tenant_id=162 AND a.target_type=1;

UPDATE ibms_lighting_operation_log a
JOIN ibms_lighting_scene s ON s.id=a.target_id AND s.tenant_id=1
JOIN ibms_lighting_scene d ON d.scene_code=s.scene_code AND d.tenant_id=162
SET a.target_id=d.id WHERE a.tenant_id=162 AND a.target_type=2;

UPDATE ibms_lighting_operation_log a
JOIN ibms_lighting_gateway s ON s.id=a.target_id AND s.tenant_id=1
JOIN ibms_lighting_gateway d ON d.gateway_code=s.gateway_code AND d.tenant_id=162
SET a.target_id=d.id WHERE a.tenant_id=162 AND a.target_type=3;

UPDATE ibms_lighting_operation_log a
JOIN ibms_lighting_controller s ON s.id=a.target_id AND s.tenant_id=1
JOIN ibms_lighting_controller d ON d.controller_code=s.controller_code AND d.tenant_id=162
SET a.target_id=d.id WHERE a.tenant_id=162 AND a.target_type=4;

SELECT '=== light_op_log AFTER ===' AS m;
SELECT target_type, target_id, COUNT(*) FROM ibms_lighting_operation_log WHERE tenant_id=162 GROUP BY target_type, target_id ORDER BY target_type, target_id;
