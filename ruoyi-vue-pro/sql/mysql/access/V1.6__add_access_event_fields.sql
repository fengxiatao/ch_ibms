-- =============================================
-- 智慧通行模块字段补充
-- 基于需求清单差异分析，补充门禁记录相关字段
-- =============================================

-- 1. 在门禁事件日志表中添加人员证件号字段
ALTER TABLE `iot_access_event_log` 
ADD COLUMN `id_card` varchar(30) DEFAULT NULL COMMENT '人员证件号（身份证号等）' AFTER `person_name`;

-- 2. 在门禁事件日志表中添加出入方向字段
ALTER TABLE `iot_access_event_log` 
ADD COLUMN `direction` tinyint DEFAULT NULL COMMENT '出入方向：1-进门，2-出门' AFTER `verify_result`;

-- 3. 添加索引以支持按方向查询
CREATE INDEX `idx_direction` ON `iot_access_event_log` (`direction`);

-- =============================================
-- 说明：
-- 此迁移脚本用于补充门禁记录列表展示所需的字段：
-- - id_card: 用于展示人员证件号信息
-- - direction: 用于区分进门/出门状态
-- 
-- 执行后需要更新以下Java类：
-- - IotAccessEventLogDO.java (已更新)
-- - 前端门禁记录列表组件
-- =============================================
