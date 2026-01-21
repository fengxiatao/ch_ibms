-- ===================================
-- 电子地图系统 - FloorDO表结构扩展
-- 数据库：ibms_gis (PostgreSQL)
-- 表名：floor
-- 
-- ⚠️ 使用说明（图形化工具版本）：
-- 1. 先在工具中手动连接到 ibms_gis 数据库
-- 2. 然后执行本脚本
-- ===================================

BEGIN;

-- ===================================
-- 1. 0图层SVG数据（TEXT，可存储大文本）
-- ===================================
ALTER TABLE floor ADD COLUMN IF NOT EXISTS dxf_layer0_svg TEXT;
COMMENT ON COLUMN floor.dxf_layer0_svg IS '0图层SVG矢量数据，用于Canvas渲染建筑轮廓';

-- ===================================
-- 2. 0图层JSON数据（JSONB，支持索引查询）
-- ===================================
ALTER TABLE floor ADD COLUMN IF NOT EXISTS dxf_layer0_json JSONB;
COMMENT ON COLUMN floor.dxf_layer0_json IS '0图层结构化数据，包含边界和实体信息，格式: {"bounds": {...}, "entities": [...], "scale": 0.001}';

-- 在JSONB列上创建GIN索引（提升查询性能）
CREATE INDEX IF NOT EXISTS idx_floor_layer0_json ON floor USING GIN (dxf_layer0_json);

-- ===================================
-- 3. 平面图栅格图URL
-- ===================================
ALTER TABLE floor ADD COLUMN IF NOT EXISTS floor_plan_image_url VARCHAR(500);
COMMENT ON COLUMN floor.floor_plan_image_url IS '平面图PNG/JPG文件URL（可选，用于快速展示）';

-- ===================================
-- 4. 平面图像素尺寸
-- ===================================
ALTER TABLE floor ADD COLUMN IF NOT EXISTS floor_plan_width INTEGER DEFAULT 1920;
COMMENT ON COLUMN floor.floor_plan_width IS '平面图宽度（像素），默认1920px';

ALTER TABLE floor ADD COLUMN IF NOT EXISTS floor_plan_height INTEGER DEFAULT 1080;
COMMENT ON COLUMN floor.floor_plan_height IS '平面图高度（像素），默认1080px';

-- ===================================
-- 5. 实际建筑尺寸（米）
-- ===================================
ALTER TABLE floor ADD COLUMN IF NOT EXISTS building_width NUMERIC(10, 2);
COMMENT ON COLUMN floor.building_width IS '建筑实际宽度（米），用于坐标转换';

ALTER TABLE floor ADD COLUMN IF NOT EXISTS building_length NUMERIC(10, 2);
COMMENT ON COLUMN floor.building_length IS '建筑实际长度（米），用于坐标转换';

-- ===================================
-- 6. 坐标转换比例
-- ===================================
ALTER TABLE floor ADD COLUMN IF NOT EXISTS coordinate_scale NUMERIC(10, 4);
COMMENT ON COLUMN floor.coordinate_scale IS '像素/米比例（floor_plan_width / building_width），用于设备坐标转换';

-- ===================================
-- 7. 最后处理时间
-- ===================================
ALTER TABLE floor ADD COLUMN IF NOT EXISTS floor_plan_generated_at TIMESTAMP;
COMMENT ON COLUMN floor.floor_plan_generated_at IS '平面图最后生成时间，用于判断是否需要重新生成';

-- ===================================
-- 8. 创建索引（提升查询性能）
-- ===================================

-- 用于快速查询已有平面图的楼层
CREATE INDEX IF NOT EXISTS idx_floor_has_plan 
  ON floor(id) 
  WHERE dxf_layer0_svg IS NOT NULL;

-- 用于按生成时间排序
CREATE INDEX IF NOT EXISTS idx_floor_generated_at 
  ON floor(floor_plan_generated_at DESC NULLS LAST);

-- 用于快速查询指定建筑下已有平面图的楼层
CREATE INDEX IF NOT EXISTS idx_floor_building_has_plan 
  ON floor(building_id) 
  WHERE dxf_layer0_svg IS NOT NULL;

-- ===================================
-- 9. 数据完整性约束
-- ===================================

-- 确保如果有平面图，必须有尺寸信息
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint 
    WHERE conname = 'chk_floor_plan_consistency'
  ) THEN
    ALTER TABLE floor ADD CONSTRAINT chk_floor_plan_consistency 
      CHECK (
        (dxf_layer0_svg IS NULL) OR 
        (floor_plan_width IS NOT NULL AND floor_plan_height IS NOT NULL AND coordinate_scale IS NOT NULL)
      );
  END IF;
END $$;

-- 确保像素尺寸为正数
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint 
    WHERE conname = 'chk_floor_plan_size'
  ) THEN
    ALTER TABLE floor ADD CONSTRAINT chk_floor_plan_size 
      CHECK (floor_plan_width > 0 AND floor_plan_height > 0);
  END IF;
END $$;

-- 确保建筑尺寸为正数
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint 
    WHERE conname = 'chk_building_size'
  ) THEN
    ALTER TABLE floor ADD CONSTRAINT chk_building_size 
      CHECK (
        (building_width IS NULL AND building_length IS NULL) OR 
        (building_width > 0 AND building_length > 0)
      );
  END IF;
END $$;

-- 确保坐标比例为正数
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint 
    WHERE conname = 'chk_coordinate_scale'
  ) THEN
    ALTER TABLE floor ADD CONSTRAINT chk_coordinate_scale 
      CHECK (coordinate_scale IS NULL OR coordinate_scale > 0);
  END IF;
END $$;

COMMIT;

-- ===================================
-- 10. 验证结果
-- ===================================

-- 查看新增字段
SELECT 
  column_name,
  data_type,
  character_maximum_length,
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
ORDER BY ordinal_position;

-- 查看新增索引
SELECT 
  indexname,
  indexdef
FROM pg_indexes
WHERE tablename = 'floor' 
  AND (indexname LIKE '%floor_plan%' OR indexname LIKE '%layer0%');

-- 统计当前已有平面图的楼层数量
SELECT 
  COUNT(*) AS total_floors,
  COUNT(dxf_file_path) AS has_dxf_file,
  COUNT(dxf_layer0_svg) AS has_layer0_svg,
  COUNT(floor_plan_image_url) AS has_floor_plan_image
FROM floor;

-- ===================================
-- 完成！
-- ===================================
-- 执行结果说明：
-- ✅ 新增 9 个字段用于电子地图功能
-- ✅ 创建 4 个索引提升查询性能
-- ✅ 添加 4 个约束确保数据完整性
-- ✅ 支持 DXF解析、SVG渲染、设备坐标转换
-- ===================================



