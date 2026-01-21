-- =====================================================
-- 修复 infra_job 表结构
-- 添加缺失的字段
-- =====================================================

-- 检查表是否存在
SELECT '开始修复 infra_job 表结构...' AS message;

-- 添加字段（如果字段已存在会报错，可以忽略）
-- 方法：先检查字段是否存在，不存在才添加

-- 添加 business_type 字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'infra_job' 
  AND COLUMN_NAME = 'business_type';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `infra_job` ADD COLUMN `business_type` VARCHAR(50) NULL DEFAULT NULL COMMENT ''业务类型'' AFTER `monitor_timeout`',
    'SELECT ''字段 business_type 已存在'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 business_module 字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'infra_job' 
  AND COLUMN_NAME = 'business_module';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `infra_job` ADD COLUMN `business_module` VARCHAR(50) NULL DEFAULT NULL COMMENT ''业务模块'' AFTER `business_type`',
    'SELECT ''字段 business_module 已存在'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 priority 字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'infra_job' 
  AND COLUMN_NAME = 'priority';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `infra_job` ADD COLUMN `priority` INT NULL DEFAULT 0 COMMENT ''优先级（0-10，数字越大优先级越高）'' AFTER `business_module`',
    'SELECT ''字段 priority 已存在'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 concurrent 字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'infra_job' 
  AND COLUMN_NAME = 'concurrent';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `infra_job` ADD COLUMN `concurrent` TINYINT(1) NULL DEFAULT 1 COMMENT ''是否允许并发执行（0:否 1:是）'' AFTER `priority`',
    'SELECT ''字段 concurrent 已存在'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 job_group 字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'infra_job' 
  AND COLUMN_NAME = 'job_group';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `infra_job` ADD COLUMN `job_group` VARCHAR(50) NULL DEFAULT ''DEFAULT'' COMMENT ''任务组名'' AFTER `concurrent`',
    'SELECT ''字段 job_group 已存在'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 max_concurrent_count 字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'infra_job' 
  AND COLUMN_NAME = 'max_concurrent_count';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `infra_job` ADD COLUMN `max_concurrent_count` INT NULL DEFAULT 1 COMMENT ''最大并发数'' AFTER `job_group`',
    'SELECT ''字段 max_concurrent_count 已存在'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 conflict_strategy 字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'infra_job' 
  AND COLUMN_NAME = 'conflict_strategy';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `infra_job` ADD COLUMN `conflict_strategy` VARCHAR(20) NULL DEFAULT ''SKIP'' COMMENT ''冲突策略（SKIP:跳过 QUEUE:排队 OVERRIDE:覆盖）'' AFTER `max_concurrent_count`',
    'SELECT ''字段 conflict_strategy 已存在'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 depend_jobs 字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'infra_job' 
  AND COLUMN_NAME = 'depend_jobs';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `infra_job` ADD COLUMN `depend_jobs` VARCHAR(500) NULL DEFAULT NULL COMMENT ''依赖任务ID列表（逗号分隔）'' AFTER `conflict_strategy`',
    'SELECT ''字段 depend_jobs 已存在'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 resource_limit 字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'infra_job' 
  AND COLUMN_NAME = 'resource_limit';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `infra_job` ADD COLUMN `resource_limit` VARCHAR(500) NULL DEFAULT NULL COMMENT ''资源限制配置（JSON格式）'' AFTER `depend_jobs`',
    'SELECT ''字段 resource_limit 已存在'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证表结构
DESC infra_job;

-- 统计任务数量
SELECT 
    COUNT(*) AS total_jobs,
    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS enabled_jobs,
    SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS disabled_jobs
FROM infra_job
WHERE deleted = 0;

SELECT '=== infra_job 表结构修复完成 ===' AS message;

-- =====================================================
-- 为IoT模块的定时任务设置业务类型
-- =====================================================

-- 更新NVR通道同步任务
UPDATE infra_job 
SET 
    business_type = 'IOT',
    business_module = 'CHANNEL_SYNC',
    priority = 5,
    job_group = 'IOT_GROUP'
WHERE handler_name = 'nvrChannelSyncJob' 
  AND deleted = 0;

SELECT '=== IoT任务业务类型设置完成 ===' AS message;
