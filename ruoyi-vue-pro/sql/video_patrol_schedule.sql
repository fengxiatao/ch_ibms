-- =============================================
-- 视频巡更任务 - 启动/暂停功能
-- =============================================-- 视频巡更任务 - 数据库迁移脚本（简化版）
-- 日期: 2024-11-18
-- 说明: 不需要单独的巡更计划表，直接使用 iot_patrol_task 表

-- ========================================
-- 第一部分：修改视频巡更任务表
-- ========================================

-- 1. 添加运行状态字段
ALTER TABLE iot_video_patrol_task 
ADD COLUMN running_status VARCHAR(20) DEFAULT 'stopped' 
COMMENT '运行状态：stopped-未启动，running-运行中，paused-已暂停' 
AFTER status;

-- 2. 添加负责人字段
ALTER TABLE iot_video_patrol_task 
ADD COLUMN executor BIGINT 
COMMENT '负责人ID（用户ID）' 
AFTER running_status;

-- 3. 添加索引
CREATE INDEX idx_running_status ON iot_video_patrol_task(running_status);

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
  
  -- 审计字段
  creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
  
  INDEX idx_task_id (task_id),
  INDEX idx_plan_date (plan_date),
  INDEX idx_schedule_status (schedule_status),
  INDEX idx_executor_id (executor_id),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频巡更计划表';
