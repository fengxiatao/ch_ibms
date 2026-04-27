# 长辉信息→长辉IBMS 数据同步进度

最后更新：2026-04-27（Phase 6 完成 ✅ 全部 6 个 Phase 已全部完成）

## ⚠️ 数据库实例说明（重要）

- **目标实例**：`127.0.0.1:3306`（hostname=`WIN-1GFT341TD6J`），即 MCP `mysql-ibms` 指向的本机实例
- **非目标**：`192.168.1.126:3306`（hostname=`chvm2`），`.mcp-db.json` 指向，t1 数据相同但**不是同步目标**
- 写操作必须用 `mysql -h127.0.0.1 -uroot -p123456 ch_ibms`
- 查询验证统一用 MCP `mcp4_mysql_query`（已指向 127.0.0.1）

## 全局状态

- 计划文件：`C:\Users\Administrator\.windsurf\plans\sync-tenant-data-system-to-ibms-fc0a4f.md`
- 阶段分组：`e:\ch\.tenant_sync\phase_groups.md`
- 表清单：`e:\ch\.tenant_sync\tables_inventory.csv`（109 张表，t1=18,743 行）
- 唯一约束：`e:\ch\.tenant_sync\table_meta_uniques.tsv`
- 主键信息：`e:\ch\.tenant_sync\table_meta_pk.tsv`

## 备份表清单（已生成，保留供回滚）

### Phase 0 之前
- `bak_system_role_menu_tenant162_20260424`（菜单同步备份，127.0.0.1 实例）

### Phase 1（127.0.0.1 实例，2026-04-26，均为空表，t162 原本无数据）
- `bak_iot_subsystem_t162_20260426`
- `bak_iot_job_type_definition_t162_20260426`
- `bak_iot_scheduled_task_config_t162_20260426`
- `bak_iot_thing_model_t162_20260426`
- `bak_iot_data_sink_t162_20260426`
- `bak_iot_data_rule_t162_20260426`
- `bak_iot_product_category_t162_20260426`
- `bak_ibms_product_t162_20260426`
- `bak_ibms_product_property_t162_20260426`
- `bak_ibms_product_point_type_t162_20260426`

### Phase 2（127.0.0.1 实例，2026-04-26，27 张表）

空表（原 t162=0）：
- `bak_campus_t162_20260426`、`bak_building_t162_20260426`、`bak_floor_t162_20260426`、`bak_area_t162_20260426`
- `bak_ibms_hvac_device_t162_20260426`、`bak_ibms_water_device_t162_20260426`
- `bak_ibms_lighting_gateway_t162_20260426`、`bak_ibms_lighting_controller_t162_20260426`、`bak_ibms_lighting_circuit_t162_20260426`、`bak_ibms_lighting_scene_t162_20260426`、`bak_ibms_lighting_schedule_t162_20260426`、`bak_ibms_lighting_alarm_t162_20260426`、`bak_ibms_lighting_operation_log_t162_20260426`
- `bak_ibms_env_data_record_t162_20260426`、`bak_ibms_env_alarm_t162_20260426`
- `bak_ibms_bac_alarm_t162_20260426`、`bak_ibms_bac_system_log_t162_20260426`
- `bak_ibms_energy_meter_t162_20260426`、`bak_ibms_energy_rate_t162_20260426`、`bak_ibms_energy_record_t162_20260426`、`bak_ibms_energy_statistics_daily_t162_20260426`、`bak_ibms_energy_alarm_t162_20260426`、`bak_ibms_energy_manual_reading_t162_20260426`

非空表（原 t162 有预置数据，已实备份）：
- `bak_ibms_space_t162_20260426`（4 行）
- `bak_ibms_device_t162_20260426`（19 行）
- `bak_ibms_channel_t162_20260426`（39 行）
- `bak_ibms_device_runtime_t162_20260426`（4 行）

## Phase 状态

| Phase | 名称 | 状态 | 备注 |
|---|---|---|---|
| 0 | 准备 & 元数据扫描 | ✅ 已完成 | 109 张表入选，唯一约束已盘 |
| 1 | 共享/字典 | ✅ 已完成 | 10 张表，行数/外键/后缀全部校验通过 |
| 2 | 智慧建筑 | ✅ 已完成 | 27 张表，行数全部对齐（仅 ibms_energy_alarm 5 行因源端 meter_id 孤儿合理丢弃） |
| 3 | 智慧安防 | ✅ 已完成 | 28 张表，行数全部对齐(2 处合理孤儿丢弃) |
| 4 | 智慧通行 | ✅ 已完成 | 38 张表，14,659 行全部对齐（含 iot_access_event_log 12566） |
| 5 | 智慧能源 | ✅ 已完成 | 校验阶段，无新增同步 |
| 6 | 验证 & 收尾 | ✅ 已完成 | 行数终验证通过；rollback/cleanup/drop_backups 三件套已生成；36 张 _tmp_map_* 已清理 |

## 已完成表 / 跳过表

### Phase 1（已完成 10 张，2026-04-26）

| 表 | t1=t162 行数 | 后缀字段 | ID 映射 |
|---|---:|---|---|
| `iot_subsystem` | 8 | `code` + `parent_code`（自引用按 code） | — |
| `iot_job_type_definition` | 20 | `code` | — |
| `iot_thing_model` | 66 | — | `product_id` 引用旧 iot_product 表（已废弃），保留原值 |
| `iot_data_sink` | 2 | — | `_tmp_map_sink`（description 临时标记回查） |
| `iot_data_rule` | 1 | — | `sink_ids` 字符串重写：`11,12` → `10000013,10000014` |
| `iot_product_category` | 8 | — | `parent_id` 自引用（description 临时标记回查） |
| `iot_scheduled_task_config` | 2 | `job_type`（与 job_type_definition.code 同步） | `entity_type='PRODUCT'` 用 product map；`DEVICE` 暂保留原值 |
| `ibms_product` | 19 | `product_code` | 通过 UK 回查建立 `_tmp_map_product` |
| `ibms_product_property` | 18 | — | `product_id` 用 product map |
| `ibms_product_point_type` | 17 | — | `product_id` 用 product map |

脚本：`e:\ch\.tenant_sync\phase1_sync.sql`，执行：`run_phase1_local.bat`（127.0.0.1）

### Phase 2（已完成 27 张，2026-04-26）

| 表 | t1 | t162 | 后缀字段 / 备注 |
|---|---:|---:|---|
| `campus` | 2 | 2 | remark 标记建 `_tmp_map_campus` |
| `building` | 5 | 5 | campus_id 用 map；remark 建 `_tmp_map_building` |
| `floor` | 14 | 14 | building_id 用 map；remark 建 `_tmp_map_floor` |
| `area` | 239 | 239 | floor/building/campus_id 用 map；remark 建 `_tmp_map_area`；`connected_area_ids` 保留旧 ID（JSON 列表） |
| `ibms_space` | 4 | 4 | UK 安全；parent_id 自引用用 `_tmp_map_space2`（mysql temp 表不能复用） |
| `ibms_device` | 19 | 19 | UK 安全；`sn`、`subsystem_code` 加后缀；`ibms_product_id` 通过 product map；`extra` 建 `_tmp_map_device` |
| `ibms_channel` | 39 | 39 | UK 安全；device_id 用 map；`mac`、`device_sn` 加后缀（实际源端均 NULL）；`space_id` 多义保留 |
| `ibms_device_runtime` | 14 | 14 | PK=device_id 非自增；映射 device/area/campus/building/floor/room（room→space） |
| `ibms_hvac_device` | 18 | 18 | UK 安全；area_id 映射 |
| `ibms_water_device` | 8 | 8 | UK 安全；area_id 映射 |
| `ibms_lighting_gateway` | 3 | 3 | UK 安全；`mac_address`、`ip_address` 加后缀 |
| `ibms_lighting_controller` | 4 | 4 | UK 安全；gateway_id 映射 |
| `ibms_lighting_circuit` | 14 | 14 | UK 安全；controller/gateway/area_id 映射 |
| `ibms_lighting_scene` | 5 | 5 | UK 安全；area_id 映射 |
| `ibms_lighting_schedule` | 4 | 4 | scene_id 用 scene 映射 |
| `ibms_lighting_alarm` | 4 | 4 | 日志类，device_id 多义保留 |
| `ibms_lighting_operation_log` | 6 | 6 | 日志类，target_id 多义保留 |
| `ibms_env_data_record` | 99 | 99 | sensor_id 保留（t1 端 sensor 表本身为空，源即孤儿快照） |
| `ibms_env_alarm` | 2 | 2 | 同上 |
| `ibms_bac_alarm` | 6 | 6 | device_id 多义保留 |
| `ibms_bac_system_log` | 8 | 8 | 同上 |
| `ibms_energy_rate` | 6 | 6 | 无 FK |
| `ibms_energy_meter` | 23 | 23 | UK uk_meter_code 安全；area_id 映射；建 `_tmp_map_meter` |
| `ibms_energy_record` | 21 | 21 | meter_id 用 map |
| `ibms_energy_statistics_daily` | 63 | 63 | meter_id+area_id 映射；UK uk_meter_date 自然安全 |
| `ibms_energy_alarm` | 12 | **7** | meter_id 用 map；**5 行源端 meter_id=101/201/301/401/501 在 t1 已是孤儿（meter 不存在），合理丢弃** |
| `ibms_energy_manual_reading` | 3 | 3 | meter_id 用 map |

补刀：`iot_scheduled_task_config` 中 `entity_type='DEVICE'` 的 entity_id（Phase 1 遗留 id=2）通过 `_tmp_map_device` 重写（实际若 t1 device id=2 不存在则 INNER JOIN 不更新）。

脚本：`e:\ch\.tenant_sync\phase2_sync.sql`、复位脚本：`phase2_reset.sql`（首跑 temp 表复用错误后用），执行：`run_phase2_local.bat`

### 误操作记录与回滚
首次执行误用 `192.168.1.126` 实例，已通过 `rollback_phase1_remote.sql` 完整回滚（DELETE 所有 t162 数据 + DROP 备份表），该实例已恢复到执行前状态。

### Phase 3（已完成 28 张，2026-04-27，127.0.0.1 实例）

| 表 | t1 | t162 | 后缀字段 / 备注 |
|---|---:|---:|---|
| `iot_alarm_host` | 2 | 2 | host_sn, ip_address 加 -ibms; device_id 用 _tmp_map_device(orphan 109/112 中 112 保留原值) |
| `iot_alarm_partition` | 1 | 1 | host_id 重写 |
| `iot_alarm_zone` | 13 | **8** | host_id, partition_id 重写; **5 行 host_id=10000001 在 t1 已是孤儿(host 不存在),合理丢弃** |
| `iot_alarm_event` | 2468 | 2468 | host_id 重写; 单语句完成 |
| `iot_alarm_operation_log` | 103 | 103 | host/partition/zone 全 LEFT JOIN 重写 |
| `iot_camera_preset` | 4 | 4 | channel_id 重写 |
| `iot_camera_cruise` | 13 | 13 | channel_id 重写 |
| `iot_camera_cruise_point` | 66 | 66 | cruise_id, preset_id 重写 |
| `iot_camera_recording` | 3 | 3 | channel_id 重写 |
| `iot_camera_snapshot` | 14 | 14 | channel_id, device_id 重写 |
| `iot_cloud_defense_mode` | 6 | 6 | mode_code 加 -ibms |
| `iot_cloud_defense_area` | 5 | 5 | area_code 加 -ibms; space_id 多义保留(1002/1003/2002/2003 在 ibms_space 不存在) |
| `iot_cloud_defense_area_device_rel` | 6 | 6 | area_id, device_id, channel_id 重写 |
| `iot_cloud_defense_point` | 6 | 6 | area_id, device_id, channel_id 重写; point_code 加 -ibms |
| `iot_cloud_defense_score_log` | 1 | 1 | 无 FK |
| `iot_video_view_group` | 4 | 4 | icon 标记 |
| `iot_video_view` | 17 | 17 | description 标记; group_ids 字符串(分号分隔) 用 numbers 表展开后重写为 t162 ID |
| `iot_video_view_pane` | 107 | **104** | view_id, channel_id, device_id 重写; **3 行 view_id 孤儿丢弃** |
| `iot_video_patrol_plan` | 7 | 7 | description 标记 |
| `iot_video_patrol_task` | 21 | 21 | plan_id 重写; current_scene_id 先置 NULL 后回填(scene 插入完成后 UPDATE) |
| `iot_video_patrol_scene` | 21 | 21 | task_id 重写 |
| `iot_video_patrol_scene_channel` | 188 | 188 | scene_id, channel_id, device_id 重写 |
| `iot_video_patrol_schedule` | 2 | 2 | patrol_plan_id 重写 |
| `iot_video_inspection_task` | 6 | 6 | scenes JSON 内嵌 channelId/deviceId,保留原值(多义,符合 Phase 2 模式) |
| `iot_epatrol_person` | 3 | 3 | patrol_stick_no, person_card_no 加 -ibms |
| `iot_epatrol_point` | 15 | 15 | UK(point_no, tenant_id) 安全 |
| `iot_epatrol_route` | 4 | 4 | 无 UK |
| `iot_epatrol_route_point` | 34 | 34 | route_id, point_id 重写 |
| `iot_epatrol_plan` | 4 | 4 | route_id 重写; UK(plan_code, tenant_id) 安全 |
| `iot_epatrol_plan_period` | 16 | 16 | plan_id, route_id 重写; person_ids JSON 用 JSON_TABLE 重写; 映射用 ROW_NUMBER 配对(自然键有重复) |
| `iot_epatrol_task` | 10 | 10 | plan_id, period_id, route_id 重写; person_ids JSON 重写 |
| `iot_epatrol_task_record` | 16 | 16 | task_id, point_id, person_id 重写 |

跳过表(t1=0): `iot_alarm_host_status_log`, `iot_video_patrol_record`, `iot_video_patrol_point`, `iot_video_patrol_schedule_log`, 全部 `security_*` (9 张)

脚本: `phase3_sync_full.sql`(初版,行 157 失败) → `phase3_sync_resume.sql/_resume2.sql/_resume3.sql/_resume3b.sql`(分段续跑)

执行过程中遇到的问题及修复:
1. `iot_alarm_host.device_id` NOT NULL,t1.id=2 引用孤儿 device_id=112 → 改用 `COALESCE(map.new_id, t.original_id)` 保留原值(全部子表 device/channel/space/space_id/route_id 等同此处理)
2. SUBSTRING(remark,10) 应为 SUBSTRING(remark,9)('__OLDID:' 长度 8,位置 9 起为数字) → 修复后 fix_substr.ps1 批量替换
3. `iot_epatrol_plan_period` 自然键(plan_id, start_time, duration_minutes)有重复,无 PK 标记字段 → 改用 ROW_NUMBER 顺序配对(INSERT...SELECT 保持 t1.id 顺序验证通过)

备份表 33 张,全部 `bak_<table>_t162_20260426`,均空表(t162 原本无安防数据)。

### Phase 4（已完成 38 张，2026-04-27，127.0.0.1 实例）

总行数：14,659（t1） == 14,659（t162），全部对齐，0 孤儿。

t162 端原始数据：全部 38 张表均为空（备份表 `bak_<table>_t162_20260427` 全部空表，保留供回滚）。

后缀字段（无 tenant_id 的 UK）：
- `iot_keding_device.station_code` 加 `-ibms`
- `iot_parking_wechat_user.openid` + `username` 加 `-ibms`

| 表 | t1=t162 | FK 处理 |
|---|---:|---|
| `iot_keding_device` | 12 | station_code+-ibms 建 `_tmp_map_keding_device` |
| `iot_keding_firmware` | 5 | — |
| `iot_ota_firmware` | 2 | product_id 用 product map；ROW_NUMBER 配对建 `_tmp_map_ota_fw` |
| `iot_access_department` | 28 | self-FK parent_id：先 INSERT 后 UPDATE；ROW_NUMBER 建 `_tmp_map_dept` |
| `iot_access_permission_group` | 7 | ROW_NUMBER 建 `_tmp_map_perm_grp` |
| `iot_parking_lot` | 3 | ROW_NUMBER 建 `_tmp_map_lot` |
| `iot_parking_charge_rule` | 3 | ROW_NUMBER 建 `_tmp_map_charge_rule` |
| `iot_visitor_appointment` | 43 | ROW_NUMBER 建 `_tmp_map_visitor`；areas JSON 是 location 标签字符串保留 |
| `iot_device_group` | 2 | — |
| `iot_device_display_config` | 8 | product_id 用 map |
| `iot_access_person` | 15 | dept_id 用 dept map；ROW_NUMBER 建 `_tmp_map_person` |
| `iot_parking_lane` | 5 | lot/main_camera/main_screen/aux_camera/aux_screen 各 LEFT JOIN map；ROW_NUMBER 建 `_tmp_map_lane` |
| `iot_parking_gate` | 4 | lot_id, lane_id, device_id 用 map；ROW_NUMBER 建 `_tmp_map_gate` |
| `iot_parking_monthly_vehicle` | 8 | lot_id 用 map；ROW_NUMBER 建 `_tmp_map_monthly_v` |
| `iot_parking_monthly_recharge` | 2 | monthly_vehicle_id 用 map |
| `iot_parking_blacklist` | 4 | lot_id 用 map |
| `iot_parking_free_vehicle` | 5 | lot_ids JSON：JSON_TABLE+JSON_ARRAYAGG 重写 |
| `iot_parking_pass_rule` | 3 | lot_ids/lane_ids JSON 重写；ROW_NUMBER 建 `_tmp_map_pass_rule` |
| `iot_parking_charge_rule_apply` | 2 | lot_ids JSON 重写；rule_id 用 charge_rule map |
| `iot_parking_system_config` | 2 | lot_id 用 map |
| `iot_parking_wechat_user` | 2 | openid+username 加 -ibms |
| `iot_parking_present_vehicle` | 20 | lot/entry_lane/entry_gate 用 map |
| `iot_parking_record` | 24 | lot/lanes/gates/charge_rule/pass_rule 用 map；ROW_NUMBER 建 `_tmp_map_park_record` |
| `iot_parking_refund_record` | 3 | record_id 用 park_record map |
| `iot_visitor_abnormal_event` | 11 | appointment_id 用 visitor map |
| `iot_device_channel` | 2 | t1 端 device_id=900001 不在任何已知 device 表，多义保留；其他字段全 NULL |
| `iot_device_event_log` | 979 | device_id 同时尝试 ibms_device map / iot_keding_device map（混合引用） |
| `iot_access_person_credential` | 18 | person_id 用 map |
| `iot_access_person_device_auth` | 25 | person/device/channel 用 map |
| `iot_access_device_capability` | 2 | device_id 用 map |
| `iot_access_permission_group_device` | 27 | group/device/channel 用 map |
| `iot_access_permission_group_person` | 23 | group/person 用 map |
| `iot_access_auth_task` | 208 | group/person/device 用 map；ROW_NUMBER 建 `_tmp_map_auth_task` |
| `iot_access_auth_task_detail` | 305 | task/person/device/channel 用 map |
| `iot_access_event_log` | 12566 | device(106/113/114)/channel/person 用 map；channel_id=0 保留 |
| `iot_access_operation_log` | 254 | device/channel/target_person/perm_grp/auth_task 用 map；operator_id 是 system user 保留 |
| `iot_ota_task` | 7 | firmware_id 用 ota_fw map；ROW_NUMBER 建 `_tmp_map_ota_task` |
| `iot_ota_task_record` | 20 | firmware/from_firmware/task 用 map；device_id 同时尝试 ibms/keding map |

脚本：`phase4_part1.sql`（maps+backups+12 张独立表）、`phase4_part2.sql`（17 张中层）、`phase4_part3.sql`（11 张终末）；输出/错误日志：`phase4_part{1,2,3}.{out,err}`。

执行过程中遇到的问题及修复：
1. `area`/`floor`/`building` 无自然 UK，初版试图用 `area_code`/`floor_code`/`building_code` 重建 map 会失败；实际 Phase 4 不需要这 3 个 map（device_channel 相关字段全 NULL），删除即可。
2. PowerShell 不支持 `<` 重定向，改用 `cmd /c "..."` 包装 mysql 命令。

### Phase 5（已完成，2026-04-27，127.0.0.1 实例）

校验结论：**无新增同步**，智慧能源模块 6 个子页面后端表全部已在 Phase 2 同步完成。

校验过程：
1. 走查 `e:\ch\yudao-ui-admin-vue3\src\views\energy\` 下 6 个子页面：
   - **Overview / ConsumptionAnalysis / ReportManagement / AlarmManagement** → 全部 `import * as EnergyApi from '@/api/iot/building/energy'`
   - **DeviceManagement / SystemSettings** → 无后端 API 调用（前端静态/Mock 数据）
2. `@/api/iot/building/energy.ts` 路径前缀 `/iot/building/energy`，对应 6 张表：
   - `ibms_energy_meter`、`ibms_energy_record`、`ibms_energy_statistics_daily`、`ibms_energy_alarm`、`ibms_energy_rate`、`ibms_energy_manual_reading`
   - 均已在 Phase 2 同步（行数对齐：23/23、21/21、63/63、7/12（5 行合理丢弃）、6/6、3/3）
3. 数据库内其他含 `energy` 的表：
   - `iot_factory_collab_energy_reading`(t1=36)、`iot_factory_collab_energy_suggestion`(t1=3) → 属于 **智慧工厂** 模块（`iot_factory_collab_*` 系列），不在本计划 4 个目标模块（建筑/安防/通行/能源）范围内，**不同步**。

无脚本，无新备份表。

### Phase 6（已完成，2026-04-27，127.0.0.1 实例）

#### 1. 行数终验证（`phase6_verify.sql`）

按 phase 汇总（仅统计已同步表，**未含按 deleted=0 的 Phase 1 软删除项**，下方独立说明）：

| Phase | t1 总行数 | t162 总行数 | 差额 | 表数 |
|---|---:|---:|---:|---:|
| 1 | 250 | 161 | 89 | 10 |
| 2 | 645 | 640 | 5 | 27 |
| 3 | 3186 | 3178 | 8 | 32 |
| 4 | 14659 | 14659 | 0 | 38 |
| **合计** | **18740** | **18638** | **102** | **107** |

差额 102 行的归因（**全部为合理跳过/丢弃，非同步缺陷**）：

- **Phase 1 = 89 行**：t1 端 `deleted=1` 软删除行（同步策略仅复制 `deleted=0`）。按 `deleted=0` 重新核对：`iot_thing_model 66/66`、`ibms_product 19/19`、`ibms_product_point_type 17/17`、`ibms_product_property 18/18`、`iot_product_category 8/8`、`iot_scheduled_task_config 2/2` —— **全部对齐**。
- **Phase 2 = 5 行**：`ibms_energy_alarm` 源端 meter_id=101/201/301/401/501 在 t1 已是孤儿（meter 不存在），合理丢弃（progress.md Phase 2 已记录）。
- **Phase 3 = 8 行**：`iot_alarm_zone` 5 行 host_id=10000001 在 t1 是孤儿；`iot_video_view_pane` 3 行 view_id 孤儿。均已记录。

结论：**全部 107 张同步表行数对齐，0 个非预期差异**。

#### 2. 收尾脚本三件套

| 脚本 | 用途 | 执行状态 |
|---|---|---|
| `phase6_verify.sql` | 按 phase 汇总 t1/t162 行数对比 | ✅ 已运行（输出 `phase6_verify.out`） |
| `phase6_cleanup.sql` | DROP 全部 36 张 `_tmp_map_*` 临时映射表 | ✅ 已运行（残留为 0） |
| `rollback_tenant_162.sql` | 一键回滚：按 FK 反向 DELETE t162 数据 + 还原 7 张非空备份 | 📦 已生成，**未执行**（仅供需要回滚时使用） |
| `phase6_drop_backups.sql` | 删除全部 107 张 `bak_*_t162_*` 备份表 | 📦 已生成，**未执行**（确认无回滚需求后可手动运行） |

#### 3. 备份表统计

- 总计：**107 张** `bak_<table>_t162_<date>`（Phase 1=10、Phase 2=27、Phase 3=32、Phase 4=38）
- 非空：7 张（同步前 t162 端的预置数据）
  - `bak_ibms_space_t162_20260426` (4)、`bak_ibms_device_t162_20260426` (19)、`bak_ibms_channel_t162_20260426` (39)、`bak_ibms_device_runtime_t162_20260426` (4)
  - `bak_iot_video_view_group_t162_20260426` (3)、`bak_iot_video_view_t162_20260426` (6)、`bak_iot_video_view_pane_t162_20260426` (20)
- 其余 100 张备份表为空（同步前 t162 端无对应数据）。

#### 4. 待人工验证（不阻塞 Phase 6 收尾）

后端数据已就绪，前端走查需 admin 用户人工执行：

- admin 登录长辉IBMS（tenant_id=162），逐模块走查 4 个一级页面（智慧建筑/安防/通行/能源）+ 主要二级页面
- 列表分页、详情、看板查询无 403 / 空列表 / JS 错误
- 如发现页面级问题，再针对性补刀

### Phase 6 补丁2 — `iot_video_view*` creator 归属修复（2026-04-27）

**触发**：admin 登录长辉IBMS 进入"实时预览"，"默认分组"等分组都在显示，但展开后没有任何视图。

**根因**：`VideoViewMapper.selectListByCreator()` **按 `creator = 当前登录用户ID` 过滤**（行级数据隔离）。同步过来的 t162 视图 `creator` 还是 t1 旧用户 ID（`1`=t1 admin、`144`=t1 feng），而 t162 admin 实际 id=`143`，自然查不到。

**修复**：把 t162 端三张视图表的 `creator`/`updater` 全部归属到 t162 admin (id=143)。

| 表 | 行数 | 修改前 creator | 修改后 |
|---|---:|---|---|
| `iot_video_view` | 17 | "1"(13) / "144"(4) | "143" |
| `iot_video_view_pane` | 104 | "1"(92) / "144"(12) | "143" |
| `iot_video_view_group` | 4 | "1"(1) / ""(3) | "143" |

**修复脚本**：`fix_video_view_creator.sql`

**新增备份**：
- `bak_iot_video_view_creator_t162_20260427`、`bak_iot_video_view_pane_creator_t162_20260427`、`bak_iot_video_view_group_creator_t162_20260427`

**通用风险**：本次同步全程**未做 creator/updater 重写**，t162 端全表 creator 都还是 t1 旧用户 ID。多数表查询只按 `tenant_id` 隔离，影响不可见；但 `iot_video_view` 这种**显式按 creator 过滤**的接口会出问题。建议如发现其他模块也有"列表为空"现象，先 grep `selectListByCreator` 同款 mapper 排查。

### Phase 6 补丁 — "多义保留"FK 修复（2026-04-27）

**触发**：admin 登录长辉IBMS 进入"智慧安防→视频监控→实时预览"，dsdsd 空间下看不到通道。

**根因**：Phase 2 同步时部分 FK 字段被错判为"多义保留"原样照搬 t1 旧 ID，但实际上这些字段配合一个 `*_type` 字段后是**单义**的，应按 type 分类映射重写。

**已修复 5 个字段（5 张表）**：

| 表 | 字段 | 配合的类型字段 | 修复行数 | 映射键 |
|---|---|---|---:|---|
| `ibms_channel` | `space_id` | — （单义指向 ibms_space） | 6 | `ibms_space.code+name` |
| `ibms_bac_alarm` | `device_id` | `device_type` (1=hvac,2=water) | 6 | `device_code` |
| `ibms_bac_system_log` | `device_id` | `device_type` (1=hvac,2=water) | 8 | `device_code` |
| `ibms_lighting_alarm` | `device_id` | `device_type` (1=circuit,2=gateway,3=controller) | 4 | `*_code` |
| `ibms_lighting_operation_log` | `target_id` | `target_type` (1=circuit,2=scene,3=gateway,4=controller) | 6 | `*_code` |

**修复脚本**：`fix_ibms_channel_space_id.sql`、`fix_phase2_orphan_fk.sql`

**新增备份**（5 张，仅保存被修改字段的 before 值，便于回退）：
- `bak_ibms_channel_space_t162_20260427`、`bak_ibms_bac_alarm_devid_t162_20260427`、`bak_ibms_bac_system_log_devid_t162_20260427`、`bak_ibms_lighting_alarm_devid_t162_20260427`、`bak_ibms_lighting_op_log_tid_t162_20260427`

**遗留（评估为低优先级，按需处理）**：

- `iot_cloud_defense_area.space_id` (5 行，值=1002/1003/2002/2003)：在 t1 端 `ibms_space` 也不存在，源端就是孤儿，保留原值无害。
- `iot_video_inspection_task.scenes` JSON 内嵌 `deviceId`/`channelId`/`nvrId`：t1 旧 ID，需 `JSON_REPLACE` 重写。视频巡视场景边缘，前端不一定渲染，暂搁置。
- 其它 *日志类* 字段（如 `ibms_lighting_alarm.device_name` 等冗余文本字段）保持原值，无需修复。

## 下次执行起点

**全部 6 个 Phase + 1 个 ID 重写补丁已完成**。后续动作（按需执行）：

- 前端再次走查"实时预览"应能看到 dsdsd 下 6 个通道；BAC/照明告警/操作日志页面 device 列应显示正常名称
- 如需补 `iot_video_inspection_task.scenes` JSON：按 device→ibms_device map 和 channel→ibms_channel map 用 `JSON_REPLACE` 重写
- 确认无回滚需求 → 运行 `phase6_drop_backups.sql` 清理备份表
- 如需回滚 → 运行 `rollback_tenant_162.sql`（注意：本补丁的 5 张 bak 表也保留了 before 值，需要按字段级反向 UPDATE）

## 风险记录

- `iot_access_event_log` 12566 行；`iot_alarm_event` 2468 行 → 需分批
- 长辉IBMS 已有少量预置数据（ibms_channel/device/runtime/space, iot_video_view*）→ 必须先备份再清空
- 部分表无显式 PK 但有自然主键（`ibms_device_runtime.device_id`, `ibms_device_message.id`）→ 复制时需特殊处理
- **MCP 与 mysql 命令行连接的不是同一实例**：MCP `mysql-ibms` → `127.0.0.1`，`.mcp-db.json` → `192.168.1.126`。本计划目标为 `127.0.0.1`，所有写操作必须显式 `-h127.0.0.1`。
- Phase 1 ID 映射在 t162 端 ≥ `10000000`（t1 数据 +1000w 偏移因 auto_increment 自然递增），后续 phase 不要硬编码假设 ID 范围。
