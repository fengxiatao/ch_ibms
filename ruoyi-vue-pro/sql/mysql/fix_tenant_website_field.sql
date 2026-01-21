-- 修复租户表website字段
-- 问题：后端查询website（单数），但表中是websites（复数）

USE ch_ibms;

-- 1. 添加website字段（单数）
ALTER TABLE `system_tenant`
ADD COLUMN `website` varchar(256) NULL COMMENT '租户网站（单个，用于快速查询）' AFTER `status`;

-- 2. 从websites字段中提取第一个网站到website字段
UPDATE `system_tenant`
SET `website` = CASE
    WHEN `websites` IS NOT NULL AND `websites` != '' THEN
        -- 提取第一个网站（假设逗号分隔）
        SUBSTRING_INDEX(`websites`, ',', 1)
    ELSE
        NULL
END;

-- 3. 为website字段创建索引（用于快速查询）
ALTER TABLE `system_tenant`
ADD INDEX `idx_website` (`website`);

-- 4. 验证修复
SELECT 
    id, name, website, websites, status
FROM system_tenant
ORDER BY id;

SELECT '[SUCCESS] website字段已添加并填充数据' AS message;

















