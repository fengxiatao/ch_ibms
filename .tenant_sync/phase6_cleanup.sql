-- Phase 6 清理：删除全部 36 张 _tmp_map_* 临时映射表
-- 备份表（bak_*_t162_*）保留供回滚，由 rollback_tenant_162.sql 单独管理
USE ch_ibms;

DROP TABLE IF EXISTS `_tmp_map_alarm_host`;
DROP TABLE IF EXISTS `_tmp_map_alarm_partition`;
DROP TABLE IF EXISTS `_tmp_map_alarm_zone`;
DROP TABLE IF EXISTS `_tmp_map_auth_task`;
DROP TABLE IF EXISTS `_tmp_map_cd_area`;
DROP TABLE IF EXISTS `_tmp_map_channel`;
DROP TABLE IF EXISTS `_tmp_map_charge_rule`;
DROP TABLE IF EXISTS `_tmp_map_cruise`;
DROP TABLE IF EXISTS `_tmp_map_dept`;
DROP TABLE IF EXISTS `_tmp_map_device`;
DROP TABLE IF EXISTS `_tmp_map_eperiod`;
DROP TABLE IF EXISTS `_tmp_map_eperson`;
DROP TABLE IF EXISTS `_tmp_map_eplan`;
DROP TABLE IF EXISTS `_tmp_map_epoint`;
DROP TABLE IF EXISTS `_tmp_map_eroute`;
DROP TABLE IF EXISTS `_tmp_map_etask`;
DROP TABLE IF EXISTS `_tmp_map_gate`;
DROP TABLE IF EXISTS `_tmp_map_keding_device`;
DROP TABLE IF EXISTS `_tmp_map_lane`;
DROP TABLE IF EXISTS `_tmp_map_lot`;
DROP TABLE IF EXISTS `_tmp_map_monthly_v`;
DROP TABLE IF EXISTS `_tmp_map_ota_fw`;
DROP TABLE IF EXISTS `_tmp_map_ota_task`;
DROP TABLE IF EXISTS `_tmp_map_park_record`;
DROP TABLE IF EXISTS `_tmp_map_pass_rule`;
DROP TABLE IF EXISTS `_tmp_map_perm_grp`;
DROP TABLE IF EXISTS `_tmp_map_person`;
DROP TABLE IF EXISTS `_tmp_map_preset`;
DROP TABLE IF EXISTS `_tmp_map_product`;
DROP TABLE IF EXISTS `_tmp_map_space`;
DROP TABLE IF EXISTS `_tmp_map_view`;
DROP TABLE IF EXISTS `_tmp_map_view_group`;
DROP TABLE IF EXISTS `_tmp_map_visitor`;
DROP TABLE IF EXISTS `_tmp_map_vpplan`;
DROP TABLE IF EXISTS `_tmp_map_vpscene`;
DROP TABLE IF EXISTS `_tmp_map_vptask`;

SELECT '=== _tmp_map_* 残留检查（应为空） ===' AS msg;
SELECT table_name FROM information_schema.tables WHERE table_schema='ch_ibms' AND table_name LIKE '\_tmp\_map\_%';
