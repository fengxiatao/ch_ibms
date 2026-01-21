-- 停车退款记录表
CREATE TABLE IF NOT EXISTS `iot_parking_refund_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '退款记录ID',
    `record_id` bigint DEFAULT NULL COMMENT '关联的停车记录ID',
    `plate_number` varchar(20) DEFAULT NULL COMMENT '车牌号',
    `out_trade_no` varchar(64) DEFAULT NULL COMMENT '原支付订单号（商户订单号）',
    `transaction_id` varchar(64) DEFAULT NULL COMMENT '微信支付交易号',
    `out_refund_no` varchar(64) DEFAULT NULL COMMENT '退款单号（商户退款单号）',
    `refund_id` varchar(64) DEFAULT NULL COMMENT '微信退款单号',
    `total_fee` decimal(10, 2) DEFAULT NULL COMMENT '原订单金额（元）',
    `refund_fee` decimal(10, 2) DEFAULT NULL COMMENT '退款金额（元）',
    `refund_reason` varchar(255) DEFAULT NULL COMMENT '退款原因',
    `refund_status` tinyint DEFAULT '0' COMMENT '退款状态：0-申请中，1-退款成功，2-退款失败，3-退款关闭',
    `apply_time` datetime DEFAULT NULL COMMENT '退款申请时间',
    `refund_time` datetime DEFAULT NULL COMMENT '退款完成时间',
    `apply_user` varchar(64) DEFAULT NULL COMMENT '申请人',
    `audit_user` varchar(64) DEFAULT NULL COMMENT '审核人',
    `fail_reason` varchar(255) DEFAULT NULL COMMENT '失败原因',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_record_id` (`record_id`),
    KEY `idx_plate_number` (`plate_number`),
    KEY `idx_out_trade_no` (`out_trade_no`),
    KEY `idx_out_refund_no` (`out_refund_no`),
    KEY `idx_refund_status` (`refund_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='停车退款记录表';

-- 更新停车记录表，添加支付状态3-已退款
-- ALTER TABLE `iot_parking_record` MODIFY COLUMN `payment_status` tinyint DEFAULT '0' COMMENT '支付状态：0-未支付，1-已支付，2-免费，3-已退款';
