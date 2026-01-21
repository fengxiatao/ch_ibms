-- ===================================
-- 电子地图系统 - FloorDO表结构扩展（简化版）
-- 数据库：ibms_gis (PostgreSQL)
-- 表名：floor
-- 
-- ⚠️ 使用说明：
-- 1. 在图形化工具中连接到 ibms_gis 数据库
-- 2. 执行本脚本（不包含约束检查，更安全）
-- ===================================

-- 1. 0图层SVG数据
ALTER TABLE floor ADD COLUMN IF NOT EXISTS dxf_layer0_svg TEXT;

-- 2. 0图层JSON数据
ALTER TABLE floor ADD COLUMN IF NOT EXISTS dxf_layer0_json JSONB;

-- 3. 平面图栅格图URL
ALTER TABLE floor ADD COLUMN IF NOT EXISTS floor_plan_image_url VARCHAR(500);

-- 4. 平面图像素尺寸
ALTER TABLE floor ADD COLUMN IF NOT EXISTS floor_plan_width INTEGER DEFAULT 1920;
ALTER TABLE floor ADD COLUMN IF NOT EXISTS floor_plan_height INTEGER DEFAULT 1080;

-- 5. 实际建筑尺寸（米）
ALTER TABLE floor ADD COLUMN IF NOT EXISTS building_width NUMERIC(10, 2);
ALTER TABLE floor ADD COLUMN IF NOT EXISTS building_length NUMERIC(10, 2);

-- 6. 坐标转换比例
ALTER TABLE floor ADD COLUMN IF NOT EXISTS coordinate_scale NUMERIC(10, 4);

-- 7. 最后处理时间
ALTER TABLE floor ADD COLUMN IF NOT EXISTS floor_plan_generated_at TIMESTAMP;

-- 8. 创建索引
CREATE INDEX IF NOT EXISTS idx_floor_layer0_json ON floor USING GIN (dxf_layer0_json);
CREATE INDEX IF NOT EXISTS idx_floor_has_plan ON floor(id) WHERE dxf_layer0_svg IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_floor_generated_at ON floor(floor_plan_generated_at DESC NULLS LAST);
CREATE INDEX IF NOT EXISTS idx_floor_building_has_plan ON floor(building_id) WHERE dxf_layer0_svg IS NOT NULL;

-- 9. 添加字段注释
COMMENT ON COLUMN floor.dxf_layer0_svg IS '0图层SVG矢量数据';
COMMENT ON COLUMN floor.dxf_layer0_json IS '0图层JSON数据';
COMMENT ON COLUMN floor.floor_plan_image_url IS '平面图PNG/JPG文件URL';
COMMENT ON COLUMN floor.floor_plan_width IS '平面图宽度（像素）';
COMMENT ON COLUMN floor.floor_plan_height IS '平面图高度（像素）';
COMMENT ON COLUMN floor.building_width IS '建筑实际宽度（米）';
COMMENT ON COLUMN floor.building_length IS '建筑实际长度（米）';
COMMENT ON COLUMN floor.coordinate_scale IS '像素/米比例';
COMMENT ON COLUMN floor.floor_plan_generated_at IS '平面图生成时间';

-- 10. 验证结果
SELECT 
  column_name,
  data_type,
  column_default,
  is_nullable
FROM information_schema.columns
WHERE table_name = 'floor' 
  AND column_name IN (
    'dxf_layer0_svg',
    'dxf_layer0_json',
    'floor_plan_image_url',
    'floor_plan_width',
    'floor_plan_height',
    'building_width',
    'building_length',
    'coordinate_scale',
    'floor_plan_generated_at'
  )
ORDER BY column_name;



