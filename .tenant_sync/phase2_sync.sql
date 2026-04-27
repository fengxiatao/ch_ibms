-- =============================================================
-- Phase 2 �?智慧建筑 同步脚本
-- �?tenant_id=1 �?目标 tenant_id=162
-- 备份后缀：_t162_20260426
-- 唯一外部标识�?-ibms 后缀（device.sn / channel.mac+device_sn / lighting_gateway.mac_address+ip_address�?-- 自增 PK 不指定，依赖 UK �?marker 建立映射
-- 执行：父表先（建映射）→ 子表�?-- =============================================================

SET NAMES utf8mb4;
SET @from := 1;
SET @to   := 162;
SET @suf  := '-ibms';
SET @cre  := '162';
SET SESSION group_concat_max_len = 1048576;

-- -------------------------------------------------------------
-- 0) 备份 t162 现有数据�?7 张表�?-- -------------------------------------------------------------
DROP TABLE IF EXISTS bak_campus_t162_20260426;                    CREATE TABLE bak_campus_t162_20260426                    AS SELECT * FROM campus                    WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_building_t162_20260426;                  CREATE TABLE bak_building_t162_20260426                  AS SELECT * FROM building                  WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_floor_t162_20260426;                     CREATE TABLE bak_floor_t162_20260426                     AS SELECT * FROM floor                     WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_area_t162_20260426;                      CREATE TABLE bak_area_t162_20260426                      AS SELECT * FROM area                      WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_space_t162_20260426;                CREATE TABLE bak_ibms_space_t162_20260426                AS SELECT * FROM ibms_space                WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_device_t162_20260426;               CREATE TABLE bak_ibms_device_t162_20260426               AS SELECT * FROM ibms_device               WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_channel_t162_20260426;              CREATE TABLE bak_ibms_channel_t162_20260426              AS SELECT * FROM ibms_channel              WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_device_runtime_t162_20260426;       CREATE TABLE bak_ibms_device_runtime_t162_20260426       AS SELECT * FROM ibms_device_runtime       WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_hvac_device_t162_20260426;          CREATE TABLE bak_ibms_hvac_device_t162_20260426          AS SELECT * FROM ibms_hvac_device          WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_water_device_t162_20260426;         CREATE TABLE bak_ibms_water_device_t162_20260426         AS SELECT * FROM ibms_water_device         WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_lighting_gateway_t162_20260426;     CREATE TABLE bak_ibms_lighting_gateway_t162_20260426     AS SELECT * FROM ibms_lighting_gateway     WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_lighting_controller_t162_20260426;  CREATE TABLE bak_ibms_lighting_controller_t162_20260426  AS SELECT * FROM ibms_lighting_controller  WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_lighting_circuit_t162_20260426;     CREATE TABLE bak_ibms_lighting_circuit_t162_20260426     AS SELECT * FROM ibms_lighting_circuit     WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_lighting_scene_t162_20260426;       CREATE TABLE bak_ibms_lighting_scene_t162_20260426       AS SELECT * FROM ibms_lighting_scene       WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_lighting_schedule_t162_20260426;    CREATE TABLE bak_ibms_lighting_schedule_t162_20260426    AS SELECT * FROM ibms_lighting_schedule    WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_lighting_alarm_t162_20260426;       CREATE TABLE bak_ibms_lighting_alarm_t162_20260426       AS SELECT * FROM ibms_lighting_alarm       WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_lighting_operation_log_t162_20260426; CREATE TABLE bak_ibms_lighting_operation_log_t162_20260426 AS SELECT * FROM ibms_lighting_operation_log WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_env_data_record_t162_20260426;      CREATE TABLE bak_ibms_env_data_record_t162_20260426      AS SELECT * FROM ibms_env_data_record      WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_env_alarm_t162_20260426;            CREATE TABLE bak_ibms_env_alarm_t162_20260426            AS SELECT * FROM ibms_env_alarm            WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_bac_alarm_t162_20260426;            CREATE TABLE bak_ibms_bac_alarm_t162_20260426            AS SELECT * FROM ibms_bac_alarm            WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_bac_system_log_t162_20260426;       CREATE TABLE bak_ibms_bac_system_log_t162_20260426       AS SELECT * FROM ibms_bac_system_log       WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_energy_meter_t162_20260426;         CREATE TABLE bak_ibms_energy_meter_t162_20260426         AS SELECT * FROM ibms_energy_meter         WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_energy_rate_t162_20260426;          CREATE TABLE bak_ibms_energy_rate_t162_20260426          AS SELECT * FROM ibms_energy_rate          WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_energy_record_t162_20260426;        CREATE TABLE bak_ibms_energy_record_t162_20260426        AS SELECT * FROM ibms_energy_record        WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_energy_statistics_daily_t162_20260426; CREATE TABLE bak_ibms_energy_statistics_daily_t162_20260426 AS SELECT * FROM ibms_energy_statistics_daily WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_energy_alarm_t162_20260426;         CREATE TABLE bak_ibms_energy_alarm_t162_20260426         AS SELECT * FROM ibms_energy_alarm         WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_ibms_energy_manual_reading_t162_20260426; CREATE TABLE bak_ibms_energy_manual_reading_t162_20260426 AS SELECT * FROM ibms_energy_manual_reading WHERE tenant_id=162;

-- -------------------------------------------------------------
-- 1) 清空 t162（逆依赖顺序）
-- -------------------------------------------------------------
DELETE FROM ibms_energy_manual_reading    WHERE tenant_id=162;
DELETE FROM ibms_energy_alarm             WHERE tenant_id=162;
DELETE FROM ibms_energy_statistics_daily  WHERE tenant_id=162;
DELETE FROM ibms_energy_record            WHERE tenant_id=162;
DELETE FROM ibms_energy_meter             WHERE tenant_id=162;
DELETE FROM ibms_energy_rate              WHERE tenant_id=162;
DELETE FROM ibms_bac_system_log           WHERE tenant_id=162;
DELETE FROM ibms_bac_alarm                WHERE tenant_id=162;
DELETE FROM ibms_env_alarm                WHERE tenant_id=162;
DELETE FROM ibms_env_data_record          WHERE tenant_id=162;
DELETE FROM ibms_lighting_operation_log   WHERE tenant_id=162;
DELETE FROM ibms_lighting_alarm           WHERE tenant_id=162;
DELETE FROM ibms_lighting_schedule        WHERE tenant_id=162;
DELETE FROM ibms_lighting_scene           WHERE tenant_id=162;
DELETE FROM ibms_lighting_circuit         WHERE tenant_id=162;
DELETE FROM ibms_lighting_controller      WHERE tenant_id=162;
DELETE FROM ibms_lighting_gateway         WHERE tenant_id=162;
DELETE FROM ibms_water_device             WHERE tenant_id=162;
DELETE FROM ibms_hvac_device              WHERE tenant_id=162;
DELETE FROM ibms_device_runtime           WHERE tenant_id=162;
DELETE FROM ibms_channel                  WHERE tenant_id=162;
DELETE FROM ibms_device                   WHERE tenant_id=162;
DELETE FROM ibms_space                    WHERE tenant_id=162;
DELETE FROM area                          WHERE tenant_id=162;
DELETE FROM floor                         WHERE tenant_id=162;
DELETE FROM building                      WHERE tenant_id=162;
DELETE FROM campus                        WHERE tenant_id=162;

-- -------------------------------------------------------------
-- 2) 重建 Phase 1 产物�?_tmp_map_product
-- -------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS _tmp_map_product;
CREATE TEMPORARY TABLE _tmp_map_product (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL);
INSERT INTO _tmp_map_product (old_id, new_id)
SELECT s.id, t.id
FROM ibms_product s
JOIN ibms_product t ON t.product_code = CONCAT(s.product_code, @suf) AND t.tenant_id = @to AND t.deleted = 0
WHERE s.tenant_id = @from AND s.deleted = 0;

-- =============================================================
-- 3) 父表层级：campus �?building �?floor �?area
--    通过 remark 末尾�?__MIG_OLD_<id>__ 标记建立映射
-- =============================================================

-- 3.1 campus
INSERT INTO campus (tenant_id, name, code, address, province, city, district, alias, campus_type,
  area, building_area, green_rate, floor_area_ratio, postal_code,
  contact_person, contact_phone, contact_email, property_company, management_mode, operation_status,
  geom, center_point, elevation, remark, job_config,
  creator, create_time, updater, update_time, deleted)
SELECT @to, name, code, address, province, city, district, alias, campus_type,
  area, building_area, green_rate, floor_area_ratio, postal_code,
  contact_person, contact_phone, contact_email, property_company, management_mode, operation_status,
  geom, center_point, elevation,
  CONCAT(IFNULL(remark,''), '__MIG_OLD_', id, '__'), job_config,
  @cre, NOW(), @cre, NOW(), deleted
FROM campus WHERE tenant_id=@from AND deleted=0;

DROP TEMPORARY TABLE IF EXISTS _tmp_map_campus;
CREATE TEMPORARY TABLE _tmp_map_campus (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL);
INSERT INTO _tmp_map_campus
SELECT CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(remark,'__MIG_OLD_',-1),'__',1) AS UNSIGNED), id
FROM campus WHERE tenant_id=@to AND remark LIKE '%__MIG_OLD_%';

UPDATE campus SET remark = NULLIF(TRIM(REGEXP_REPLACE(IFNULL(remark,''),'__MIG_OLD_[0-9]+__','')),'')
WHERE tenant_id=@to AND remark LIKE '%__MIG_OLD_%';

-- 3.2 building (campus_id �?_tmp_map_campus 映射)
INSERT INTO building (tenant_id, campus_id, name, code, alias, building_type, building_structure, fire_rating,
  total_floors, above_ground_floors, underground_floors, building_height, building_area, usable_area,
  construction_year, completion_date, design_unit, construction_unit,
  has_elevator, elevator_count, has_central_ac, has_fire_system, has_security_system,
  power_capacity, water_capacity, manager, manager_phone, operation_status,
  geom, entrance_point, floor_height, remark, job_config,
  creator, create_time, updater, update_time, deleted)
SELECT @to, mc.new_id, b.name, b.code, b.alias, b.building_type, b.building_structure, b.fire_rating,
  b.total_floors, b.above_ground_floors, b.underground_floors, b.building_height, b.building_area, b.usable_area,
  b.construction_year, b.completion_date, b.design_unit, b.construction_unit,
  b.has_elevator, b.elevator_count, b.has_central_ac, b.has_fire_system, b.has_security_system,
  b.power_capacity, b.water_capacity, b.manager, b.manager_phone, b.operation_status,
  b.geom, b.entrance_point, b.floor_height,
  CONCAT(IFNULL(b.remark,''), '__MIG_OLD_', b.id, '__'), b.job_config,
  @cre, NOW(), @cre, NOW(), b.deleted
FROM building b
LEFT JOIN _tmp_map_campus mc ON mc.old_id = b.campus_id
WHERE b.tenant_id=@from AND b.deleted=0;

DROP TEMPORARY TABLE IF EXISTS _tmp_map_building;
CREATE TEMPORARY TABLE _tmp_map_building (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL);
INSERT INTO _tmp_map_building
SELECT CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(remark,'__MIG_OLD_',-1),'__',1) AS UNSIGNED), id
FROM building WHERE tenant_id=@to AND remark LIKE '%__MIG_OLD_%';

UPDATE building SET remark = NULLIF(TRIM(REGEXP_REPLACE(IFNULL(remark,''),'__MIG_OLD_[0-9]+__','')),'')
WHERE tenant_id=@to AND remark LIKE '%__MIG_OLD_%';

-- 3.3 floor (building_id 映射)
INSERT INTO floor (tenant_id, building_id, name, code, floor_number, floor_type, floor_height, floor_area, usable_area,
  primary_function, occupancy_rate, max_occupancy,
  has_sprinkler, has_smoke_detector, has_emergency_exit, emergency_exit_count,
  ac_type, design_temp_summer, design_temp_winter,
  geom, absolute_elevation, z_base, z_top,
  remark, dxf_file_path, dxf_file_name, dxf_file_size, dxf_upload_time, job_config,
  dxf_layer0_svg, dxf_layer0_json, floor_plan_image_url,
  floor_plan_width, floor_plan_height, building_width, building_length, coordinate_scale, floor_plan_generated_at,
  creator, create_time, updater, update_time, deleted)
SELECT @to, mb.new_id, f.name, f.code, f.floor_number, f.floor_type, f.floor_height, f.floor_area, f.usable_area,
  f.primary_function, f.occupancy_rate, f.max_occupancy,
  f.has_sprinkler, f.has_smoke_detector, f.has_emergency_exit, f.emergency_exit_count,
  f.ac_type, f.design_temp_summer, f.design_temp_winter,
  f.geom, f.absolute_elevation, f.z_base, f.z_top,
  CONCAT(IFNULL(f.remark,''), '__MIG_OLD_', f.id, '__'),
  f.dxf_file_path, f.dxf_file_name, f.dxf_file_size, f.dxf_upload_time, f.job_config,
  f.dxf_layer0_svg, f.dxf_layer0_json, f.floor_plan_image_url,
  f.floor_plan_width, f.floor_plan_height, f.building_width, f.building_length, f.coordinate_scale, f.floor_plan_generated_at,
  @cre, NOW(), @cre, NOW(), f.deleted
FROM floor f
JOIN _tmp_map_building mb ON mb.old_id = f.building_id
WHERE f.tenant_id=@from AND f.deleted=0;

DROP TEMPORARY TABLE IF EXISTS _tmp_map_floor;
CREATE TEMPORARY TABLE _tmp_map_floor (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL);
INSERT INTO _tmp_map_floor
SELECT CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(remark,'__MIG_OLD_',-1),'__',1) AS UNSIGNED), id
FROM floor WHERE tenant_id=@to AND remark LIKE '%__MIG_OLD_%';

UPDATE floor SET remark = NULLIF(TRIM(REGEXP_REPLACE(IFNULL(remark,''),'__MIG_OLD_[0-9]+__','')),'')
WHERE tenant_id=@to AND remark LIKE '%__MIG_OLD_%';

-- 3.4 area (floor_id, building_id, campus_id 映射)
INSERT INTO area (tenant_id, floor_id, building_id, campus_id, name, code, area_type, sub_type,
  area_sqm, capacity, geom, local_geom, center_point, z_min, z_max, geom_3d, centroid_3d, connected_area_ids,
  fill_color, stroke_color, opacity, display_order, is_visible,
  indoorgml_id, indoorgml_type, navigation_network, description, properties, status,
  remark, job_config,
  creator, create_time, updater, update_time, deleted)
SELECT @to, mf.new_id, mb.new_id, mc.new_id, a.name, a.code, a.area_type, a.sub_type,
  a.area_sqm, a.capacity, a.geom, a.local_geom, a.center_point, a.z_min, a.z_max, a.geom_3d, a.centroid_3d, a.connected_area_ids,
  a.fill_color, a.stroke_color, a.opacity, a.display_order, a.is_visible,
  a.indoorgml_id, a.indoorgml_type, a.navigation_network, a.description, a.properties, a.status,
  CONCAT(IFNULL(a.remark,''), '__MIG_OLD_', a.id, '__'), a.job_config,
  @cre, NOW(), @cre, NOW(), a.deleted
FROM area a
JOIN _tmp_map_floor mf ON mf.old_id = a.floor_id
LEFT JOIN _tmp_map_building mb ON mb.old_id = a.building_id
LEFT JOIN _tmp_map_campus mc ON mc.old_id = a.campus_id
WHERE a.tenant_id=@from AND a.deleted=0;

DROP TEMPORARY TABLE IF EXISTS _tmp_map_area;
CREATE TEMPORARY TABLE _tmp_map_area (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL);
INSERT INTO _tmp_map_area
SELECT CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(remark,'__MIG_OLD_',-1),'__',1) AS UNSIGNED), id
FROM area WHERE tenant_id=@to AND remark LIKE '%__MIG_OLD_%';

UPDATE area SET remark = NULLIF(TRIM(REGEXP_REPLACE(IFNULL(remark,''),'__MIG_OLD_[0-9]+__','')),'')
WHERE tenant_id=@to AND remark LIKE '%__MIG_OLD_%';

-- =============================================================
-- 4) ibms_space (4 行；parent_id 自引用；UK uk_space_code_tenant 跨租户安�?
-- =============================================================
INSERT INTO ibms_space (parent_id, space_code, code, sub_code, name, type, sort, extra,
  creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 0, space_code, code, sub_code, name, type, sort,
  CONCAT(IFNULL(extra,''), '__MIG_OLD_', id, '__'),
  @cre, NOW(), @cre, NOW(), deleted, @to
FROM ibms_space WHERE tenant_id=@from AND deleted=0;

DROP TEMPORARY TABLE IF EXISTS _tmp_map_space;
CREATE TEMPORARY TABLE _tmp_map_space (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL);
INSERT INTO _tmp_map_space
SELECT CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(extra,'__MIG_OLD_',-1),'__',1) AS UNSIGNED), id
FROM ibms_space WHERE tenant_id=@to AND extra LIKE '%__MIG_OLD_%';

-- MySQL 不允许同一查询里两次打开同一个 TEMPORARY 表，复制一份用作 parent 查找
DROP TEMPORARY TABLE IF EXISTS _tmp_map_space2;
CREATE TEMPORARY TABLE _tmp_map_space2 (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL);
INSERT INTO _tmp_map_space2 SELECT old_id, new_id FROM _tmp_map_space;

-- 重写 parent_id：通过 t1 源表 join 映射
UPDATE ibms_space tgt
JOIN _tmp_map_space mt ON mt.new_id = tgt.id
JOIN ibms_space src ON src.id = mt.old_id AND src.tenant_id=@from
LEFT JOIN _tmp_map_space2 mp ON mp.old_id = src.parent_id
SET tgt.parent_id = COALESCE(mp.new_id, 0)
WHERE tgt.tenant_id=@to;

UPDATE ibms_space SET extra = NULLIF(TRIM(REGEXP_REPLACE(IFNULL(extra,''),'__MIG_OLD_[0-9]+__','')),'')
WHERE tenant_id=@to AND extra LIKE '%__MIG_OLD_%';

-- =============================================================
-- 5) ibms_device (UK uk_device_code_tenant 安全；ibms_product_id �?product map�?--    subsystem_code 加后缀（与 phase1 iot_subsystem 一致）；sn 加后缀（设备外标）)
-- =============================================================
INSERT INTO ibms_device (tenant_id, device_code, name, nickname, pic_url, device_key, device_secret, auth_type,
  subsystem_code, subsystem_override, menu_ids, primary_menu_id, menu_override, dxf_entity_id,
  device_type, group_code, system_code, device_type_code, product_model, brand, access_type,
  ip, protocol, sn, product_key, ibms_product_id, group_ids,
  point_count, points_online, points_alarm, space, extra,
  creator, create_time, updater, update_time, deleted)
SELECT @to, d.device_code, d.name, d.nickname, d.pic_url, d.device_key, d.device_secret, d.auth_type,
  CASE WHEN d.subsystem_code IS NULL OR d.subsystem_code='' THEN d.subsystem_code ELSE CONCAT(d.subsystem_code,@suf) END,
  d.subsystem_override, d.menu_ids, d.primary_menu_id, d.menu_override, d.dxf_entity_id,
  d.device_type, d.group_code, d.system_code, d.device_type_code, d.product_model, d.brand, d.access_type,
  d.ip, d.protocol,
  CASE WHEN d.sn IS NULL OR d.sn='' THEN d.sn ELSE CONCAT(d.sn,@suf) END,
  d.product_key, mp.new_id, d.group_ids,
  d.point_count, 0, 0, d.space,
  CONCAT(IFNULL(d.extra,''), '__MIG_OLD_', d.id, '__'),
  @cre, NOW(), @cre, NOW(), d.deleted
FROM ibms_device d
LEFT JOIN _tmp_map_product mp ON mp.old_id = d.ibms_product_id
WHERE d.tenant_id=@from AND d.deleted=0;

DROP TEMPORARY TABLE IF EXISTS _tmp_map_device;
CREATE TEMPORARY TABLE _tmp_map_device (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL);
INSERT INTO _tmp_map_device
SELECT CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(extra,'__MIG_OLD_',-1),'__',1) AS UNSIGNED), id
FROM ibms_device WHERE tenant_id=@to AND extra LIKE '%__MIG_OLD_%';

UPDATE ibms_device SET extra = NULLIF(TRIM(REGEXP_REPLACE(IFNULL(extra,''),'__MIG_OLD_[0-9]+__','')),'')
WHERE tenant_id=@to AND extra LIKE '%__MIG_OLD_%';

-- =============================================================
-- 6) ibms_channel (UK uk_code_tenant 安全；device_id �?device map�?--    space_id 跨表多义[non-FK] 保留原值；mac/device_sn 加后缀)
-- =============================================================
INSERT INTO ibms_channel (tenant_id, space_id, device_id, code, channel_no, name, business, type_code, category,
  system_type, data_source, ip, mac, device_sn, device_name, space, current_value, status, extra,
  creator, create_time, updater, update_time, deleted)
SELECT @to, c.space_id, md.new_id, c.code, c.channel_no, c.name, c.business, c.type_code, c.category,
  c.system_type, c.data_source, c.ip,
  CASE WHEN c.mac IS NULL OR c.mac='' THEN c.mac ELSE CONCAT(c.mac,@suf) END,
  CASE WHEN c.device_sn IS NULL OR c.device_sn='' THEN c.device_sn ELSE CONCAT(c.device_sn,@suf) END,
  c.device_name, c.space, c.current_value, c.status, c.extra,
  @cre, NOW(), @cre, NOW(), c.deleted
FROM ibms_channel c
JOIN _tmp_map_device md ON md.old_id = c.device_id
WHERE c.tenant_id=@from AND c.deleted=0;

-- =============================================================
-- 7) ibms_device_runtime (PK=device_id 非自增；映射 device/area/campus/building/floor)
-- =============================================================
INSERT INTO ibms_device_runtime (device_id, tenant_id, state, online_time, offline_time, active_time,
  firmware_id, gateway_id, location_type, latitude, longitude,
  area_id, address, campus_id, building_id, floor_id, room_id,
  local_x, local_y, local_z, install_location, install_height_type,
  config, job_config, creator, create_time, updater, update_time, deleted)
SELECT md.new_id, @to, r.state, r.online_time, r.offline_time, r.active_time,
  r.firmware_id, r.gateway_id, r.location_type, r.latitude, r.longitude,
  ma.new_id, r.address, mc.new_id, mb.new_id, mf.new_id, ms.new_id,
  r.local_x, r.local_y, r.local_z, r.install_location, r.install_height_type,
  r.config, r.job_config, @cre, NOW(), @cre, NOW(), r.deleted
FROM ibms_device_runtime r
JOIN _tmp_map_device md ON md.old_id = r.device_id
LEFT JOIN _tmp_map_area ma ON ma.old_id = r.area_id
LEFT JOIN _tmp_map_campus mc ON mc.old_id = r.campus_id
LEFT JOIN _tmp_map_building mb ON mb.old_id = r.building_id
LEFT JOIN _tmp_map_floor mf ON mf.old_id = r.floor_id
LEFT JOIN _tmp_map_space ms ON ms.old_id = r.room_id
WHERE r.tenant_id=@from AND r.deleted=0;

-- =============================================================
-- 8) ibms_hvac_device (UK 安全；area_id 映射)
-- =============================================================
INSERT INTO ibms_hvac_device (device_code, device_name, device_type, area_id, area_name, floor, location,
  status, run_mode, set_temp, room_temp, wind_speed, filter_status, pressure, run_hours,
  maintain_status, next_maintain_date, last_update_time, remark,
  creator, create_time, updater, update_time, deleted, tenant_id)
SELECT h.device_code, h.device_name, h.device_type, ma.new_id, h.area_name, h.floor, h.location,
  h.status, h.run_mode, h.set_temp, h.room_temp, h.wind_speed, h.filter_status, h.pressure, h.run_hours,
  h.maintain_status, h.next_maintain_date, h.last_update_time, h.remark,
  @cre, NOW(), @cre, NOW(), h.deleted, @to
FROM ibms_hvac_device h
LEFT JOIN _tmp_map_area ma ON ma.old_id = h.area_id
WHERE h.tenant_id=@from AND h.deleted=0;

-- =============================================================
-- 9) ibms_water_device (UK 安全；area_id 映射)
-- =============================================================
INSERT INTO ibms_water_device (device_code, device_name, device_type, area_id, area_name, location,
  status, run_mode, pressure, water_level, run_hours, maintain_status, last_update_time, remark,
  creator, create_time, updater, update_time, deleted, tenant_id)
SELECT w.device_code, w.device_name, w.device_type, ma.new_id, w.area_name, w.location,
  w.status, w.run_mode, w.pressure, w.water_level, w.run_hours, w.maintain_status, w.last_update_time, w.remark,
  @cre, NOW(), @cre, NOW(), w.deleted, @to
FROM ibms_water_device w
LEFT JOIN _tmp_map_area ma ON ma.old_id = w.area_id
WHERE w.tenant_id=@from AND w.deleted=0;

-- =============================================================
-- 10) ibms_lighting_gateway (UK 安全；mac_address/ip_address 加后缀)
-- =============================================================
INSERT INTO ibms_lighting_gateway (gateway_code, gateway_name, gateway_model, ip_address, mac_address,
  area_name, firmware_version, device_count, signal_strength, status, last_online_time, remark,
  creator, create_time, updater, update_time, deleted, tenant_id)
SELECT g.gateway_code, g.gateway_name, g.gateway_model,
  CASE WHEN g.ip_address IS NULL OR g.ip_address='' THEN g.ip_address ELSE CONCAT(g.ip_address,@suf) END,
  CASE WHEN g.mac_address IS NULL OR g.mac_address='' THEN g.mac_address ELSE CONCAT(g.mac_address,@suf) END,
  g.area_name, g.firmware_version, g.device_count, g.signal_strength, g.status, g.last_online_time, g.remark,
  @cre, NOW(), @cre, NOW(), g.deleted, @to
FROM ibms_lighting_gateway g WHERE g.tenant_id=@from AND g.deleted=0;

DROP TEMPORARY TABLE IF EXISTS _tmp_map_lgw;
CREATE TEMPORARY TABLE _tmp_map_lgw (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL);
INSERT INTO _tmp_map_lgw
SELECT s.id, t.id
FROM ibms_lighting_gateway s
JOIN ibms_lighting_gateway t ON t.gateway_code = s.gateway_code AND t.tenant_id=@to AND t.deleted=0
WHERE s.tenant_id=@from AND s.deleted=0;

-- =============================================================
-- 11) ibms_lighting_controller (UK 安全；gateway_id 映射)
-- =============================================================
INSERT INTO ibms_lighting_controller (controller_code, controller_name, controller_model, area_name,
  channel_count, rated_load, current_load, gateway_id, status, last_online_time, remark,
  creator, create_time, updater, update_time, deleted, tenant_id)
SELECT c.controller_code, c.controller_name, c.controller_model, c.area_name,
  c.channel_count, c.rated_load, c.current_load, mg.new_id, c.status, c.last_online_time, c.remark,
  @cre, NOW(), @cre, NOW(), c.deleted, @to
FROM ibms_lighting_controller c
LEFT JOIN _tmp_map_lgw mg ON mg.old_id = c.gateway_id
WHERE c.tenant_id=@from AND c.deleted=0;

DROP TEMPORARY TABLE IF EXISTS _tmp_map_lctl;
CREATE TEMPORARY TABLE _tmp_map_lctl (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL);
INSERT INTO _tmp_map_lctl
SELECT s.id, t.id
FROM ibms_lighting_controller s
JOIN ibms_lighting_controller t ON t.controller_code = s.controller_code AND t.tenant_id=@to AND t.deleted=0
WHERE s.tenant_id=@from AND s.deleted=0;

-- =============================================================
-- 12) ibms_lighting_circuit (UK 安全；controller_id, gateway_id, area_id 映射)
-- =============================================================
INSERT INTO ibms_lighting_circuit (circuit_code, circuit_name, circuit_type, area_id, area_name, floor, location,
  load_desc, rated_power, status, brightness, color_temp, controller_id, gateway_id, last_operate_time, remark,
  creator, create_time, updater, update_time, deleted, tenant_id)
SELECT c.circuit_code, c.circuit_name, c.circuit_type, ma.new_id, c.area_name, c.floor, c.location,
  c.load_desc, c.rated_power, c.status, c.brightness, c.color_temp,
  mc.new_id, mg.new_id, c.last_operate_time, c.remark,
  @cre, NOW(), @cre, NOW(), c.deleted, @to
FROM ibms_lighting_circuit c
LEFT JOIN _tmp_map_area ma ON ma.old_id = c.area_id
LEFT JOIN _tmp_map_lctl mc ON mc.old_id = c.controller_id
LEFT JOIN _tmp_map_lgw mg ON mg.old_id = c.gateway_id
WHERE c.tenant_id=@from AND c.deleted=0;

-- =============================================================
-- 13) ibms_lighting_scene (UK 安全；area_id 映射)
-- =============================================================
INSERT INTO ibms_lighting_scene (scene_code, scene_name, scene_icon, scene_desc, area_id, area_name,
  is_active, sort, remark, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT s.scene_code, s.scene_name, s.scene_icon, s.scene_desc, ma.new_id, s.area_name,
  s.is_active, s.sort, s.remark, @cre, NOW(), @cre, NOW(), s.deleted, @to
FROM ibms_lighting_scene s
LEFT JOIN _tmp_map_area ma ON ma.old_id = s.area_id
WHERE s.tenant_id=@from AND s.deleted=0;

DROP TEMPORARY TABLE IF EXISTS _tmp_map_lscene;
CREATE TEMPORARY TABLE _tmp_map_lscene (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL);
INSERT INTO _tmp_map_lscene
SELECT s.id, t.id
FROM ibms_lighting_scene s
JOIN ibms_lighting_scene t ON t.scene_code = s.scene_code AND t.tenant_id=@to AND t.deleted=0
WHERE s.tenant_id=@from AND s.deleted=0;

-- =============================================================
-- 14) ibms_lighting_schedule (�?UK；scene_id 映射；scene_name 保留)
-- =============================================================
INSERT INTO ibms_lighting_schedule (schedule_name, execute_time, weekdays, scene_id, scene_name,
  enabled, last_execute_time, remark, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT s.schedule_name, s.execute_time, s.weekdays, ms.new_id, s.scene_name,
  s.enabled, s.last_execute_time, s.remark, @cre, NOW(), @cre, NOW(), s.deleted, @to
FROM ibms_lighting_schedule s
JOIN _tmp_map_lscene ms ON ms.old_id = s.scene_id
WHERE s.tenant_id=@from AND s.deleted=0;

-- =============================================================
-- 15) ibms_lighting_alarm (日志类；device_id 多义保留原�?
-- =============================================================
INSERT INTO ibms_lighting_alarm (device_type, device_id, device_name, alarm_level, alarm_content,
  alarm_time, duration, status, handler, handle_time, handle_remark, create_time, update_time, tenant_id)
SELECT device_type, device_id, device_name, alarm_level, alarm_content,
  alarm_time, duration, status, handler, handle_time, handle_remark, create_time, update_time, @to
FROM ibms_lighting_alarm WHERE tenant_id=@from;

-- =============================================================
-- 16) ibms_lighting_operation_log (日志类；target_id 多义保留)
-- =============================================================
INSERT INTO ibms_lighting_operation_log (operation_type, target_type, target_id, target_name,
  operation_content, operator, operator_ip, result, error_msg, operate_time, create_time, tenant_id)
SELECT operation_type, target_type, target_id, target_name,
  operation_content, operator, operator_ip, result, error_msg, operate_time, create_time, @to
FROM ibms_lighting_operation_log WHERE tenant_id=@from;

-- =============================================================
-- 17) ibms_env_data_record (sensor_id 引用 ibms_env_sensor[t1=0]，保留原值快照含 sensor_code)
-- =============================================================
INSERT INTO ibms_env_data_record (sensor_id, sensor_code, temperature, humidity, pm25, pm10, co2,
  formaldehyde, illuminance, noise, collect_time, create_time, tenant_id)
SELECT sensor_id, sensor_code, temperature, humidity, pm25, pm10, co2,
  formaldehyde, illuminance, noise, collect_time, create_time, @to
FROM ibms_env_data_record WHERE tenant_id=@from;

-- =============================================================
-- 18) ibms_env_alarm (sensor_id 同上；snapshot 字段已含 sensor_code/sensor_name)
-- =============================================================
INSERT INTO ibms_env_alarm (sensor_id, sensor_code, sensor_name, alarm_type, alarm_level, alarm_content,
  alarm_value, threshold_value, alarm_time, recover_time, status, handler, handle_time, handle_remark,
  create_time, update_time, tenant_id)
SELECT sensor_id, sensor_code, sensor_name, alarm_type, alarm_level, alarm_content,
  alarm_value, threshold_value, alarm_time, recover_time, status, handler, handle_time, handle_remark,
  create_time, update_time, @to
FROM ibms_env_alarm WHERE tenant_id=@from;

-- =============================================================
-- 19) ibms_bac_alarm (device_id 多义保留；快�?device_name 已存)
-- =============================================================
INSERT INTO ibms_bac_alarm (device_type, device_id, device_name, alarm_level, alarm_content, alarm_time,
  duration, status, handler, handle_time, handle_remark, create_time, update_time, tenant_id)
SELECT device_type, device_id, device_name, alarm_level, alarm_content, alarm_time,
  duration, status, handler, handle_time, handle_remark, create_time, update_time, @to
FROM ibms_bac_alarm WHERE tenant_id=@from;

-- =============================================================
-- 20) ibms_bac_system_log
-- =============================================================
INSERT INTO ibms_bac_system_log (log_type, device_type, device_id, device_name, event_desc, event_value,
  operator, log_time, create_time, tenant_id)
SELECT log_type, device_type, device_id, device_name, event_desc, event_value,
  operator, log_time, create_time, @to
FROM ibms_bac_system_log WHERE tenant_id=@from;

-- =============================================================
-- 21) ibms_energy_rate (�?FK)
-- =============================================================
INSERT INTO ibms_energy_rate (rate_name, energy_type, rate_type, tier_level, tier_start, tier_end,
  time_period, start_time, end_time, unit_price, status, effective_date, expire_date, remark,
  creator, create_time, updater, update_time, deleted, tenant_id)
SELECT rate_name, energy_type, rate_type, tier_level, tier_start, tier_end,
  time_period, start_time, end_time, unit_price, status, effective_date, expire_date, remark,
  @cre, NOW(), @cre, NOW(), deleted, @to
FROM ibms_energy_rate WHERE tenant_id=@from AND deleted=0;

-- =============================================================
-- 22) ibms_energy_meter (UK uk_meter_code 安全；area_id 映射)
-- =============================================================
INSERT INTO ibms_energy_meter (meter_code, meter_name, meter_type, area_id, area_name, floor, location,
  status, current_reading, unit, multiplier, last_reading_time, communication_type, install_time, remark,
  creator, create_time, updater, update_time, deleted, tenant_id)
SELECT m.meter_code, m.meter_name, m.meter_type, ma.new_id, m.area_name, m.floor, m.location,
  m.status, m.current_reading, m.unit, m.multiplier, m.last_reading_time, m.communication_type, m.install_time, m.remark,
  @cre, NOW(), @cre, NOW(), m.deleted, @to
FROM ibms_energy_meter m
LEFT JOIN _tmp_map_area ma ON ma.old_id = m.area_id
WHERE m.tenant_id=@from AND m.deleted=0;

DROP TEMPORARY TABLE IF EXISTS _tmp_map_meter;
CREATE TEMPORARY TABLE _tmp_map_meter (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL);
INSERT INTO _tmp_map_meter
SELECT s.id, t.id
FROM ibms_energy_meter s
JOIN ibms_energy_meter t ON t.meter_code = s.meter_code AND t.tenant_id=@to AND t.deleted=0
WHERE s.tenant_id=@from AND s.deleted=0;

-- =============================================================
-- 23) ibms_energy_record (meter_id 映射；snapshot meter_code 保留)
-- =============================================================
INSERT INTO ibms_energy_record (meter_id, meter_code, meter_type, reading_value, consumption,
  reading_time, record_type, create_time, tenant_id)
SELECT mm.new_id, r.meter_code, r.meter_type, r.reading_value, r.consumption,
  r.reading_time, r.record_type, r.create_time, @to
FROM ibms_energy_record r
JOIN _tmp_map_meter mm ON mm.old_id = r.meter_id
WHERE r.tenant_id=@from;

-- =============================================================
-- 24) ibms_energy_statistics_daily (meter_id, area_id 映射；UK uk_meter_date �?meter_id+stat_date)
-- =============================================================
INSERT INTO ibms_energy_statistics_daily (meter_id, meter_code, meter_type, area_id, stat_date,
  consumption, cost, create_time, tenant_id)
SELECT mm.new_id, s.meter_code, s.meter_type, ma.new_id, s.stat_date,
  s.consumption, s.cost, s.create_time, @to
FROM ibms_energy_statistics_daily s
JOIN _tmp_map_meter mm ON mm.old_id = s.meter_id
LEFT JOIN _tmp_map_area ma ON ma.old_id = s.area_id
WHERE s.tenant_id=@from;

-- =============================================================
-- 25) ibms_energy_alarm (meter_id 映射)
-- =============================================================
INSERT INTO ibms_energy_alarm (meter_id, meter_code, meter_name, alarm_type, alarm_level, alarm_content,
  alarm_value, threshold_value, alarm_time, status, handler, handle_time, handle_remark,
  create_time, update_time, tenant_id)
SELECT mm.new_id, a.meter_code, a.meter_name, a.alarm_type, a.alarm_level, a.alarm_content,
  a.alarm_value, a.threshold_value, a.alarm_time, a.status, a.handler, a.handle_time, a.handle_remark,
  a.create_time, a.update_time, @to
FROM ibms_energy_alarm a
JOIN _tmp_map_meter mm ON mm.old_id = a.meter_id
WHERE a.tenant_id=@from;

-- =============================================================
-- 26) ibms_energy_manual_reading (meter_id 映射)
-- =============================================================
INSERT INTO ibms_energy_manual_reading (meter_id, meter_code, meter_name, reading_date, reading_time,
  last_reading, current_reading, consumption, reader, status, remark, reviewer, review_time,
  creator, create_time, updater, update_time, deleted, tenant_id)
SELECT mm.new_id, r.meter_code, r.meter_name, r.reading_date, r.reading_time,
  r.last_reading, r.current_reading, r.consumption, r.reader, r.status, r.remark, r.reviewer, r.review_time,
  @cre, NOW(), @cre, NOW(), r.deleted, @to
FROM ibms_energy_manual_reading r
JOIN _tmp_map_meter mm ON mm.old_id = r.meter_id
WHERE r.tenant_id=@from AND r.deleted=0;

-- =============================================================
-- 27) Phase 1 遗留：iot_scheduled_task_config DEVICE 类型 entity_id �?device map 重写
-- =============================================================
UPDATE iot_scheduled_task_config c
JOIN _tmp_map_device md ON md.old_id = c.entity_id
SET c.entity_id = md.new_id
WHERE c.tenant_id=@to AND c.entity_type='DEVICE' AND c.deleted=0;

-- =============================================================
-- 28) 校验
-- =============================================================
SELECT tbl, t1, t162 FROM (
  SELECT 'campus' tbl,(SELECT COUNT(*) FROM campus WHERE tenant_id=1 AND deleted=0) t1,(SELECT COUNT(*) FROM campus WHERE tenant_id=162 AND deleted=0) t162
  UNION ALL SELECT 'building',(SELECT COUNT(*) FROM building WHERE tenant_id=1 AND deleted=0),(SELECT COUNT(*) FROM building WHERE tenant_id=162 AND deleted=0)
  UNION ALL SELECT 'floor',(SELECT COUNT(*) FROM floor WHERE tenant_id=1 AND deleted=0),(SELECT COUNT(*) FROM floor WHERE tenant_id=162 AND deleted=0)
  UNION ALL SELECT 'area',(SELECT COUNT(*) FROM area WHERE tenant_id=1 AND deleted=0),(SELECT COUNT(*) FROM area WHERE tenant_id=162 AND deleted=0)
  UNION ALL SELECT 'ibms_space',(SELECT COUNT(*) FROM ibms_space WHERE tenant_id=1 AND deleted=0),(SELECT COUNT(*) FROM ibms_space WHERE tenant_id=162 AND deleted=0)
  UNION ALL SELECT 'ibms_device',(SELECT COUNT(*) FROM ibms_device WHERE tenant_id=1 AND deleted=0),(SELECT COUNT(*) FROM ibms_device WHERE tenant_id=162 AND deleted=0)
  UNION ALL SELECT 'ibms_channel',(SELECT COUNT(*) FROM ibms_channel WHERE tenant_id=1 AND deleted=0),(SELECT COUNT(*) FROM ibms_channel WHERE tenant_id=162 AND deleted=0)
  UNION ALL SELECT 'ibms_device_runtime',(SELECT COUNT(*) FROM ibms_device_runtime WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_device_runtime WHERE tenant_id=162)
  UNION ALL SELECT 'ibms_hvac_device',(SELECT COUNT(*) FROM ibms_hvac_device WHERE tenant_id=1 AND deleted=0),(SELECT COUNT(*) FROM ibms_hvac_device WHERE tenant_id=162 AND deleted=0)
  UNION ALL SELECT 'ibms_water_device',(SELECT COUNT(*) FROM ibms_water_device WHERE tenant_id=1 AND deleted=0),(SELECT COUNT(*) FROM ibms_water_device WHERE tenant_id=162 AND deleted=0)
  UNION ALL SELECT 'ibms_lighting_gateway',(SELECT COUNT(*) FROM ibms_lighting_gateway WHERE tenant_id=1 AND deleted=0),(SELECT COUNT(*) FROM ibms_lighting_gateway WHERE tenant_id=162 AND deleted=0)
  UNION ALL SELECT 'ibms_lighting_controller',(SELECT COUNT(*) FROM ibms_lighting_controller WHERE tenant_id=1 AND deleted=0),(SELECT COUNT(*) FROM ibms_lighting_controller WHERE tenant_id=162 AND deleted=0)
  UNION ALL SELECT 'ibms_lighting_circuit',(SELECT COUNT(*) FROM ibms_lighting_circuit WHERE tenant_id=1 AND deleted=0),(SELECT COUNT(*) FROM ibms_lighting_circuit WHERE tenant_id=162 AND deleted=0)
  UNION ALL SELECT 'ibms_lighting_scene',(SELECT COUNT(*) FROM ibms_lighting_scene WHERE tenant_id=1 AND deleted=0),(SELECT COUNT(*) FROM ibms_lighting_scene WHERE tenant_id=162 AND deleted=0)
  UNION ALL SELECT 'ibms_lighting_schedule',(SELECT COUNT(*) FROM ibms_lighting_schedule WHERE tenant_id=1 AND deleted=0),(SELECT COUNT(*) FROM ibms_lighting_schedule WHERE tenant_id=162 AND deleted=0)
  UNION ALL SELECT 'ibms_lighting_alarm',(SELECT COUNT(*) FROM ibms_lighting_alarm WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_lighting_alarm WHERE tenant_id=162)
  UNION ALL SELECT 'ibms_lighting_operation_log',(SELECT COUNT(*) FROM ibms_lighting_operation_log WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_lighting_operation_log WHERE tenant_id=162)
  UNION ALL SELECT 'ibms_env_data_record',(SELECT COUNT(*) FROM ibms_env_data_record WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_env_data_record WHERE tenant_id=162)
  UNION ALL SELECT 'ibms_env_alarm',(SELECT COUNT(*) FROM ibms_env_alarm WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_env_alarm WHERE tenant_id=162)
  UNION ALL SELECT 'ibms_bac_alarm',(SELECT COUNT(*) FROM ibms_bac_alarm WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_bac_alarm WHERE tenant_id=162)
  UNION ALL SELECT 'ibms_bac_system_log',(SELECT COUNT(*) FROM ibms_bac_system_log WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_bac_system_log WHERE tenant_id=162)
  UNION ALL SELECT 'ibms_energy_meter',(SELECT COUNT(*) FROM ibms_energy_meter WHERE tenant_id=1 AND deleted=0),(SELECT COUNT(*) FROM ibms_energy_meter WHERE tenant_id=162 AND deleted=0)
  UNION ALL SELECT 'ibms_energy_rate',(SELECT COUNT(*) FROM ibms_energy_rate WHERE tenant_id=1 AND deleted=0),(SELECT COUNT(*) FROM ibms_energy_rate WHERE tenant_id=162 AND deleted=0)
  UNION ALL SELECT 'ibms_energy_record',(SELECT COUNT(*) FROM ibms_energy_record WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_energy_record WHERE tenant_id=162)
  UNION ALL SELECT 'ibms_energy_statistics_daily',(SELECT COUNT(*) FROM ibms_energy_statistics_daily WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_energy_statistics_daily WHERE tenant_id=162)
  UNION ALL SELECT 'ibms_energy_alarm',(SELECT COUNT(*) FROM ibms_energy_alarm WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_energy_alarm WHERE tenant_id=162)
  UNION ALL SELECT 'ibms_energy_manual_reading',(SELECT COUNT(*) FROM ibms_energy_manual_reading WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_energy_manual_reading WHERE tenant_id=162)
) v ORDER BY tbl;
