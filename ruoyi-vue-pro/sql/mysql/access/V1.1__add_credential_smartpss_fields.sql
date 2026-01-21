-- =============================================
-- SmartPSS认证功能对齐 - 凭证表字段扩展
-- 添加发卡时间、换卡时间、卡状态、指纹名称等字段
-- =============================================

-- 添加新字段到凭证表
ALTER TABLE `iot_access_person_credential`
    ADD COLUMN `issue_time` datetime DEFAULT NULL COMMENT '发卡时间' AFTER `card_no`,
    ADD COLUMN `replace_time` datetime DEFAULT NULL COMMENT '换卡时间（最近一次换卡）' AFTER `issue_time`,
    ADD COLUMN `card_status` tinyint NOT NULL DEFAULT 0 COMMENT '卡状态：0-正常，1-挂失，2-注销' AFTER `replace_time`,
    ADD COLUMN `finger_name` varchar(50) DEFAULT NULL COMMENT '指纹名称（如：右手食指）' AFTER `finger_index`,
    ADD COLUMN `old_card_no` varchar(50) DEFAULT NULL COMMENT '旧卡号（换卡时记录）' AFTER `card_status`;

-- 更新已有卡片记录的发卡时间为创建时间
UPDATE `iot_access_person_credential` 
SET `issue_time` = `create_time` 
WHERE `credential_type` = 'CARD' AND `issue_time` IS NULL;
