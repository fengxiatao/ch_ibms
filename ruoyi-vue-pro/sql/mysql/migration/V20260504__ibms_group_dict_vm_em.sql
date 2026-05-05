-- ============================================================================
-- V20260504__ibms_group_dict_vm_em.sql
-- 说明：补全 ibms_group 字典 ST/SB 大类下 VM(访客)/EM(环境监测) 子系统映射
-- 关联：IbmsBusinessMappingHelper Java 默认 buildDefault() 已含 VM/EM，
--      字典向 Java 对齐（见 IbmsBusinessMappingDictLoader 启动加载器）
-- 影响：仅修改 system_dict_data 2 条 remark JSON，dict_type='ibms_group'
-- 幂等：使用 dict_type+value 双条件 + JSON 全文覆盖；重复执行结果一致
-- ============================================================================

-- 给 excel_v22 版本的 ST(安防安全) 大类加 VM(访客系统)
UPDATE system_dict_data
   SET remark = '{"systems": ["AC", "IC", "CA", "VM"], "icon": "fa-door-open", "color": "purple", "desc": "门禁、停车场、对讲、访客", "systemCount": 8}'
 WHERE dict_type = 'ibms_group'
   AND value     = 'ST'
   AND deleted   = 0;

-- 给 excel_v22 版本的 SB(楼宇设备) 大类加 EM(环境监测)
UPDATE system_dict_data
   SET remark = '{"systems": ["BA", "LI", "EL", "EM"], "icon": "fa-building", "color": "cyan", "desc": "楼宇自控、照明、电梯、环境监测", "systemCount": 10}'
 WHERE dict_type = 'ibms_group'
   AND value     = 'SB'
   AND deleted   = 0;

-- 验证（手工执行，结果应为 ST=4 systems / SB=4 systems）：
-- SELECT id, value, label, remark FROM system_dict_data
--  WHERE dict_type='ibms_group' AND value IN ('ST','SB') AND deleted=0;
