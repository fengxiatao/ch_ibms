-- 回滚 192.168.1.126 上的 Phase 1 写入（t162 原本为空，DELETE 即恢复）
SET NAMES utf8mb4;
DELETE FROM ibms_product_property    WHERE tenant_id = 162;
DELETE FROM ibms_product_point_type  WHERE tenant_id = 162;
DELETE FROM ibms_product             WHERE tenant_id = 162;
DELETE FROM iot_data_rule            WHERE tenant_id = 162;
DELETE FROM iot_data_sink            WHERE tenant_id = 162;
DELETE FROM iot_product_category     WHERE tenant_id = 162;
DELETE FROM iot_scheduled_task_config WHERE tenant_id = 162;
DELETE FROM iot_thing_model          WHERE tenant_id = 162;
DELETE FROM iot_job_type_definition  WHERE tenant_id = 162;
DELETE FROM iot_subsystem            WHERE tenant_id = 162;

DROP TABLE IF EXISTS `bak_iot_subsystem_t162_20260426`;
DROP TABLE IF EXISTS `bak_iot_job_type_definition_t162_20260426`;
DROP TABLE IF EXISTS `bak_iot_scheduled_task_config_t162_20260426`;
DROP TABLE IF EXISTS `bak_iot_thing_model_t162_20260426`;
DROP TABLE IF EXISTS `bak_iot_data_sink_t162_20260426`;
DROP TABLE IF EXISTS `bak_iot_data_rule_t162_20260426`;
DROP TABLE IF EXISTS `bak_iot_product_category_t162_20260426`;
DROP TABLE IF EXISTS `bak_ibms_product_t162_20260426`;
DROP TABLE IF EXISTS `bak_ibms_product_property_t162_20260426`;
DROP TABLE IF EXISTS `bak_ibms_product_point_type_t162_20260426`;

SELECT 'rollback_done' status,
  (SELECT COUNT(*) FROM iot_subsystem WHERE tenant_id=162) sub162,
  (SELECT COUNT(*) FROM ibms_product WHERE tenant_id=162) prod162;
