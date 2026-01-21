-- ========================================
-- 修复 PostgreSQL 数据库中 building 表的结构
-- 使其与 BuildingDO 匹配
-- ========================================

-- 修复已存在字段的类型（如果类型不正确）
-- 将 campus_id 从 VARCHAR 改为 BIGINT
DO $$
BEGIN
    -- 检查 campus_id 字段的类型
    IF (SELECT data_type FROM information_schema.columns 
        WHERE table_name = 'building' AND column_name = 'campus_id') != 'bigint' THEN
        -- 如果不是 BIGINT，则转换
        ALTER TABLE building ALTER COLUMN campus_id TYPE BIGINT USING campus_id::bigint;
    END IF;
END $$;

-- 添加缺失的字段
ALTER TABLE building ADD COLUMN IF NOT EXISTS alias VARCHAR(100);
ALTER TABLE building ADD COLUMN IF NOT EXISTS building_type VARCHAR(50);
ALTER TABLE building ADD COLUMN IF NOT EXISTS building_structure VARCHAR(50);
ALTER TABLE building ADD COLUMN IF NOT EXISTS fire_rating VARCHAR(50);
ALTER TABLE building ADD COLUMN IF NOT EXISTS total_floors INTEGER;
ALTER TABLE building ADD COLUMN IF NOT EXISTS above_ground_floors INTEGER;
ALTER TABLE building ADD COLUMN IF NOT EXISTS underground_floors INTEGER;
ALTER TABLE building ADD COLUMN IF NOT EXISTS building_height NUMERIC(10, 2);
ALTER TABLE building ADD COLUMN IF NOT EXISTS building_area NUMERIC(10, 2);
ALTER TABLE building ADD COLUMN IF NOT EXISTS usable_area NUMERIC(10, 2);
ALTER TABLE building ADD COLUMN IF NOT EXISTS construction_year INTEGER;
ALTER TABLE building ADD COLUMN IF NOT EXISTS completion_date DATE;
ALTER TABLE building ADD COLUMN IF NOT EXISTS design_unit VARCHAR(200);
ALTER TABLE building ADD COLUMN IF NOT EXISTS construction_unit VARCHAR(200);
ALTER TABLE building ADD COLUMN IF NOT EXISTS has_elevator SMALLINT DEFAULT 0;
ALTER TABLE building ADD COLUMN IF NOT EXISTS elevator_count INTEGER;
ALTER TABLE building ADD COLUMN IF NOT EXISTS has_central_ac SMALLINT DEFAULT 0;
ALTER TABLE building ADD COLUMN IF NOT EXISTS has_fire_system SMALLINT DEFAULT 0;
ALTER TABLE building ADD COLUMN IF NOT EXISTS has_security_system SMALLINT DEFAULT 0;
ALTER TABLE building ADD COLUMN IF NOT EXISTS power_capacity NUMERIC(10, 2);
ALTER TABLE building ADD COLUMN IF NOT EXISTS water_capacity NUMERIC(10, 2);
ALTER TABLE building ADD COLUMN IF NOT EXISTS manager VARCHAR(100);
ALTER TABLE building ADD COLUMN IF NOT EXISTS manager_phone VARCHAR(20);
ALTER TABLE building ADD COLUMN IF NOT EXISTS operation_status VARCHAR(50);
ALTER TABLE building ADD COLUMN IF NOT EXISTS floor_height NUMERIC(10, 2);
ALTER TABLE building ADD COLUMN IF NOT EXISTS remark TEXT;

-- 添加注释
COMMENT ON COLUMN building.alias IS '别名';
COMMENT ON COLUMN building.building_type IS '建筑类型';
COMMENT ON COLUMN building.building_structure IS '建筑结构类型';
COMMENT ON COLUMN building.fire_rating IS '消防等级';
COMMENT ON COLUMN building.total_floors IS '总楼层数';
COMMENT ON COLUMN building.above_ground_floors IS '地上楼层数';
COMMENT ON COLUMN building.underground_floors IS '地下楼层数';
COMMENT ON COLUMN building.building_height IS '建筑高度（米）';
COMMENT ON COLUMN building.building_area IS '建筑面积（平方米）';
COMMENT ON COLUMN building.usable_area IS '可用面积（平方米）';
COMMENT ON COLUMN building.construction_year IS '建设年份';
COMMENT ON COLUMN building.completion_date IS '竣工日期';
COMMENT ON COLUMN building.design_unit IS '设计单位';
COMMENT ON COLUMN building.construction_unit IS '施工单位';
COMMENT ON COLUMN building.has_elevator IS '是否有电梯（0=否，1=是）';
COMMENT ON COLUMN building.elevator_count IS '电梯数量';
COMMENT ON COLUMN building.has_central_ac IS '是否有中央空调（0=否，1=是）';
COMMENT ON COLUMN building.has_fire_system IS '是否有消防系统（0=否，1=是）';
COMMENT ON COLUMN building.has_security_system IS '是否有安防系统（0=否，1=是）';
COMMENT ON COLUMN building.power_capacity IS '电力容量（kVA）';
COMMENT ON COLUMN building.water_capacity IS '水容量（立方米）';
COMMENT ON COLUMN building.manager IS '管理员';
COMMENT ON COLUMN building.manager_phone IS '管理员电话';
COMMENT ON COLUMN building.operation_status IS '运营状态';
COMMENT ON COLUMN building.floor_height IS '层高（米）';
COMMENT ON COLUMN building.remark IS '备注';

-- 确保 deleted 字段是 SMALLINT 类型（用于兼容 BooleanToIntTypeHandler）
-- 如果 deleted 已经是 SMALLINT，则跳过转换；如果是 BOOLEAN，则转换
DO $$
BEGIN
    -- 检查 deleted 字段的类型
    IF (SELECT data_type FROM information_schema.columns 
        WHERE table_name = 'building' AND column_name = 'deleted') = 'boolean' THEN
        -- 如果是 BOOLEAN，转换为 SMALLINT
        ALTER TABLE building ALTER COLUMN deleted TYPE SMALLINT USING (CASE WHEN deleted THEN 1 ELSE 0 END);
    END IF;
    
    -- 设置默认值
    ALTER TABLE building ALTER COLUMN deleted SET DEFAULT 0;
END $$;

COMMENT ON COLUMN building.deleted IS '是否删除（0=未删除，1=已删除）';
