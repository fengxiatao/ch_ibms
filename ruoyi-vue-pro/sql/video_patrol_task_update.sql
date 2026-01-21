-- =============================================
-- 视频巡更任务 - 数据库迁移脚本（简化版）
-- 日期: 2024-11-18
-- 说明: 不需要单独的巡更计划表，直接使用 iot_video_patrol_task 表
-- =============================================

-- ========================================
-- 第一部分：修改视频巡更任务表
-- ========================================

-- 1. 检查并添加运行状态字段（如果已存在则跳过）·
-- ALTER TABLE iot_video_patrol_task 
-- ADD COLUMN running_status VARCHAR(20) DEFAULT 'stopped' 
-- COMMENT '运行状态：stopped-未启动，running-运行中，paused-已暂停' 
-- AFTER status;

-- 2. 添加负责人ID字段
ALTER TABLE iot_video_patrol_task 
ADD COLUMN executor BIGINT 
COMMENT '负责人ID（用户ID）' 
AFTER status;

-- 3. 添加负责人姓名字段（冗余字段，便于查询显示）
ALTER TABLE iot_video_patrol_task 
ADD COLUMN executor_name VARCHAR(64) 
COMMENT '负责人姓名' 
AFTER executor;

-- 4. 添加索引
-- CREATE INDEX idx_running_status ON iot_video_patrol_task(running_status);
CREATE INDEX idx_executor ON iot_video_patrol_task(executor);

-- ========================================
-- 第二部分：修改通用巡更任务表
-- ========================================

-- 1. 添加任务类型字段
ALTER TABLE iot_patrol_task 
ADD COLUMN task_type INT DEFAULT 1 
COMMENT '任务类型：1-人工巡更，2-视频巡更' 
AFTER task_status;

-- 2. 添加来源任务ID字段
ALTER TABLE iot_patrol_task 
ADD COLUMN source_task_id BIGINT 
COMMENT '来源任务ID（视频巡更任务模板ID）' 
AFTER task_type;

-- 3. 添加场景配置字段（视频巡更专用）
ALTER TABLE iot_patrol_task 
ADD COLUMN scene_config JSON 
COMMENT '场景配置（视频巡更专用，JSON格式）' 
AFTER source_task_id;

-- 4. 添加索引
CREATE INDEX idx_task_type ON iot_patrol_task(task_type);
CREATE INDEX idx_source_task_id ON iot_patrol_task(source_task_id);

-- ========================================
-- 验证脚本
-- ========================================

-- 查看视频巡更任务表结构
SHOW COLUMNS FROM iot_video_patrol_task;

-- 查看通用巡更任务表结构
SHOW COLUMNS FROM iot_patrol_task;
