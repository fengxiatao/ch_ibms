-- =============================================
-- 设备位置表 (PostgreSQL版本)
-- =============================================

-- 创建设备位置表
CREATE TABLE IF NOT EXISTS device_location (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  device_id BIGINT NOT NULL,
  floor_id BIGINT,
  building_id BIGINT,
  area_id BIGINT,
  local_x DECIMAL(10,3) NOT NULL,
  local_y DECIMAL(10,3) NOT NULL,
  local_z DECIMAL(10,3) DEFAULT 0.000,
  global_longitude DECIMAL(12,8),
  global_latitude DECIMAL(12,8),
  global_altitude DECIMAL(10,3),
  install_date DATE,
  installer VARCHAR(50),
  remark VARCHAR(500),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted SMALLINT NOT NULL DEFAULT 0
);

-- 添加注释
COMMENT ON TABLE device_location IS '设备位置信息表';
COMMENT ON COLUMN device_location.id IS 'ID';
COMMENT ON COLUMN device_location.tenant_id IS '租户ID';
COMMENT ON COLUMN device_location.device_id IS '设备ID';
COMMENT ON COLUMN device_location.floor_id IS '所属楼层ID';
COMMENT ON COLUMN device_location.building_id IS '所属建筑ID（冗余字段）';
COMMENT ON COLUMN device_location.area_id IS '所属区域ID';
COMMENT ON COLUMN device_location.local_x IS '本地X坐标（米）';
COMMENT ON COLUMN device_location.local_y IS '本地Y坐标（米）';
COMMENT ON COLUMN device_location.local_z IS '本地Z坐标（米）';
COMMENT ON COLUMN device_location.global_longitude IS '全局经度';
COMMENT ON COLUMN device_location.global_latitude IS '全局纬度';
COMMENT ON COLUMN device_location.global_altitude IS '全局海拔高度（米）';
COMMENT ON COLUMN device_location.install_date IS '安装日期';
COMMENT ON COLUMN device_location.installer IS '安装人员';
COMMENT ON COLUMN device_location.remark IS '备注';
COMMENT ON COLUMN device_location.creator IS '创建者';
COMMENT ON COLUMN device_location.create_time IS '创建时间';
COMMENT ON COLUMN device_location.updater IS '更新者';
COMMENT ON COLUMN device_location.update_time IS '更新时间';
COMMENT ON COLUMN device_location.deleted IS '是否删除(0=否,1=是)';

-- 创建唯一索引（设备ID + 删除标记）
CREATE UNIQUE INDEX uk_device_location_device_id ON device_location(device_id, deleted);

-- 创建索引
CREATE INDEX idx_device_location_floor_id ON device_location(floor_id);
CREATE INDEX idx_device_location_building_id ON device_location(building_id);
CREATE INDEX idx_device_location_area_id ON device_location(area_id);
CREATE INDEX idx_device_location_tenant_id ON device_location(tenant_id);

-- 创建更新时间自动更新触发器
CREATE OR REPLACE FUNCTION update_device_location_update_time()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_device_location_update_time
    BEFORE UPDATE ON device_location
    FOR EACH ROW
    EXECUTE FUNCTION update_device_location_update_time();

-- =============================================
-- 示例数据（可选）
-- =============================================

-- 插入示例设备位置数据（假设设备ID 4和5已存在）
-- INSERT INTO device_location (tenant_id, device_id, floor_id, building_id, area_id, local_x, local_y, local_z, creator, updater)
-- VALUES 
--   (1, 4, 1, 1, 1, 10.500, 20.300, 2.500, 'admin', 'admin'),
--   (1, 5, 1, 1, 1, 15.200, 18.700, 2.500, 'admin', 'admin');

-- =============================================
-- 查询验证
-- =============================================
-- SELECT * FROM device_location WHERE deleted = 0;






