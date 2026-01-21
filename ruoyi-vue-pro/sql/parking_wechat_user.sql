-- =============================================
-- 停车场微信用户表
-- 用于存储通过微信小程序登录的用户信息
-- =============================================

DROP TABLE IF EXISTS `iot_parking_wechat_user`;
CREATE TABLE `iot_parking_wechat_user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `openid` varchar(64) NOT NULL COMMENT '微信OpenID',
    `unionid` varchar(64) DEFAULT NULL COMMENT '微信UnionID',
    `session_key` varchar(128) DEFAULT NULL COMMENT '微信会话密钥',
    `username` varchar(64) NOT NULL COMMENT '用户名（用于登录）',
    `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
    `avatar` varchar(512) DEFAULT NULL COMMENT '头像URL',
    `mobile` varchar(20) DEFAULT NULL COMMENT '手机号',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-禁用',
    `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`, `deleted`),
    UNIQUE KEY `uk_username` (`username`, `deleted`),
    KEY `idx_mobile` (`mobile`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='停车场微信用户表';
