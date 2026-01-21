-- ============================================================================
-- IoT 设备发现表增强 SQL
-- 
-- 功能：
-- 1. 添加设备状态管理（已发现/已通知/已忽略/待处理/已注册）
-- 2. 添加通知管理（通知次数、最后通知时间）
-- 3. 添加忽略功能（忽略操作人、忽略时间）
--
-- 创建日期：2025-10-26
-- 作者：长辉信息科技有限公司
-- ============================================================================

-- 1. 添加状态列
ALTER TABLE iot_discovered_device 
ADD COLUMN `status` INT DEFAULT 1 COMMENT '状态：1=已发现 2=已通知 3=已忽略 4=待处理 5=已注册' AFTER `discovery_time`;

-- 2. 添加通知管理列
ALTER TABLE iot_discovered_device 
ADD COLUMN `notified_count` INT DEFAULT 0 COMMENT '通知次数' AFTER `status`,
ADD COLUMN `last_notified_time` DATETIME COMMENT '最后通知时间' AFTER `notified_count`;

-- 3. 添加忽略功能列
ALTER TABLE iot_discovered_device 
ADD COLUMN `ignored_by` BIGINT COMMENT '忽略操作人ID' AFTER `last_notified_time`,
ADD COLUMN `ignored_time` DATETIME COMMENT '忽略时间' AFTER `ignored_by`,
ADD COLUMN `ignore_reason` VARCHAR(500) COMMENT '忽略原因' AFTER `ignored_time`,
ADD COLUMN `ignore_until` DATETIME COMMENT '忽略截止时间（NULL表示永久忽略）' AFTER `ignore_reason`;

-- 4. 添加索引
CREATE INDEX `idx_status_time` ON `iot_discovered_device`(`status`, `discovery_time`) COMMENT '状态+时间索引';
CREATE INDEX `idx_ignored_until` ON `iot_discovered_device`(`ignore_until`) COMMENT '忽略截止时间索引';

-- 5. 更新已有数据的状态（已添加的标记为已注册，未添加的标记为已发现）
UPDATE `iot_discovered_device` 
SET `status` = CASE 
    WHEN `added` = 1 THEN 5  -- 已注册
    ELSE 1                   -- 已发现
END
WHERE `status` IS NULL OR `status` = 0;

-- 6. 添加注释
ALTER TABLE `iot_discovered_device` 
MODIFY COLUMN `status` INT DEFAULT 1 COMMENT '状态：1=已发现 2=已通知 3=已忽略 4=待处理 5=已注册';

-- ============================================================================
-- 说明：
-- 
-- 状态流转：
--   DISCOVERED(1, 已发现) 
--     → NOTIFIED(2, 已通知) 
--     → IGNORED(3, 已忽略) / PENDING(4, 待处理) / REGISTERED(5, 已注册)
--
-- 通知策略：
--   - 首次发现：立即通知，status=1→2
--   - 已忽略（status=3）：不再通知
--   - 待处理（status=4）：24小时后提醒一次
--   - 已注册（status=5）：标记为已处理，不再通知
--
-- 忽略功能：
--   - 用户可以手动忽略某个设备
--   - 支持永久忽略（ignore_until=NULL）
--   - 支持临时忽略（ignore_until=具体时间）
--   - 记录忽略原因和操作人
-- ============================================================================



