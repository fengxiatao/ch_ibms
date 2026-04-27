# 阶段分组与执行顺序

## 总览
- 范围内多租户表：109 张（tenant=1 有数据）
- 系统租户(1) 总行数：~18,743
- 长辉IBMS(162) 现有行数：~91（已有少量预置数据，需备份后清空）

## 必须加 `-ibms` 后缀的唯一字段（无 tenant_id 的 UNIQUE 索引）

| 表 | 唯一字段 |
|---|---|
| `ibms_product` | product_code |
| `iot_keding_device` | station_code |
| `iot_subsystem` | code |
| `iot_cloud_defense_area` | area_code |
| `iot_cloud_defense_mode` | mode_code |
| `iot_cloud_defense_point` | point_code |
| `iot_job_type_definition` | code |
| `security_face_capture` | capture_no |
| `security_vehicle_capture` | capture_no |
| `security_video_analysis_alarm` | alarm_no |
| `iot_thing_model` | (待 Phase 1 确认) |
| `iot_product_category` | (待 Phase 1 确认) |
| `iot_data_rule` / `iot_data_sink` | (待 Phase 1 确认) |

含 tenant_id 的 UNIQUE（safe，不需要后缀）：ibms_channel, ibms_device, ibms_energy_meter, ibms_env_sensor, ibms_hvac_device, ibms_lighting_*, ibms_space, ibms_water_device, iot_access_person/credential/device_auth, iot_camera_preset, iot_device_channel, iot_epatrol_*, iot_video_patrol_*, security_statistics 等

## 跳过表（t1=0 / 工具类 / 与 4 模块无关）

- access_parking_*（系统租户为空）
- ibms_device_message, ibms_lighting_scene_circuit, ibms_env_sensor (t1=0)
- iot_*_idempotent（幂等表）
- iot_factory_*, iot_hydrology_*, iot_detong_*（不在 4 模块）
- iot_message_idempotent, iot_alarm_record/rule/schedule/statistics（t1=0）
- iot_keding_alarm/control_log/upgrade_log/upgrade_task（t1=0）
- security_*（t1=0 除 personnel_library）

## 阶段分组（按依赖顺序）

### Phase 1 — 共享/字典 (~10 张表)

无外键依赖、被其他模块引用的基础表，先复制。

| 表 | t1 | 后缀字段 |
|---|----|---|
| `iot_thing_model` | 139 | 待确认 |
| `iot_product_category` | 12 | 待确认 |
| `iot_subsystem` | 8 | code+`-ibms` |
| `iot_job_type_definition` | 20 | code+`-ibms` |
| `iot_data_rule` | 1 | 待确认 |
| `iot_data_sink` | 2 | 待确认 |
| `iot_scheduled_task_config` | 5 | - |
| `ibms_product` | 22 | product_code+`-ibms` |
| `ibms_product_point_type` | 22 | (跟随 ibms_product) |
| `ibms_product_property` | 19 | (跟随 ibms_product，需 product_id 映射) |

### Phase 2 — 智慧建筑 (~25 张表)

| 表 | t1 |
|---|----|
| `campus` / `building` / `floor` / `area` | 2/5/14/239 |
| `ibms_space` | 4（已有 4 条 162，需先清） |
| `ibms_channel` | 39（已有 39 条 162，需先清） |
| `ibms_device` | 19（已有 19 条 162，需先清） |
| `ibms_device_runtime` | 14 |
| `ibms_hvac_device` | 18 |
| `ibms_water_device` | 8 |
| `ibms_lighting_*` | gateway 3, controller 4, circuit 14, scene 5, schedule 4, alarm 4, operation_log 6 |
| `ibms_env_data_record` / `ibms_env_alarm` | 99/2 |
| `ibms_bac_alarm` / `ibms_bac_system_log` | 6/8 |
| `ibms_energy_*` | meter 23, rate 6, record 21, statistics_daily 63, alarm 12, manual_reading 3 |

### Phase 3 — 智慧安防 (~30 张表)

| 表 | t1 |
|---|----|
| `iot_alarm_host` | 2 |
| `iot_alarm_partition` | 1 |
| `iot_alarm_zone` | 13 |
| `iot_alarm_event` | 2468（大表，分批） |
| `iot_alarm_operation_log` | 103 |
| `iot_alert_config` / `iot_alert_record` | 1/2 |
| `iot_camera_*` | cruise 13, cruise_point 66, preset 4, recording 3, snapshot 14 |
| `iot_cloud_defense_*` | area 5, area_device_rel 6, mode 6, point 6, score_log 1 |
| `iot_video_view*` | view 17, group 4, pane 107（已有 t162 数据） |
| `iot_video_patrol_*` | plan 7, scene 21, scene_channel 188, schedule 2, task 21 |
| `iot_video_inspection_task` | 6 |
| `iot_epatrol_*` | person 3, plan 4, plan_period 16, point 15, route 4, route_point 34, task 10, task_record 16 |

### Phase 4 — 智慧通行 (~25 张表)

| 表 | t1 |
|---|----|
| `iot_keding_device` | 12 |
| `iot_keding_firmware` | 5 |
| `iot_access_*` | person 15, credential 18, device_auth 25, department 28, permission_group 7, group_device 27, group_person 23, auth_task 208, auth_task_detail 305, device_capability 2, event_log **12566（大表，分批）**, operation_log 254 |
| `iot_visitor_appointment` | 43 |
| `iot_visitor_abnormal_event` | 11 |
| `iot_parking_*` | lot 3, gate 4, lane 5, charge_rule 3, charge_rule_apply 2, blacklist 4, free_vehicle 5, monthly_vehicle 8, monthly_recharge 2, pass_rule 3, present_vehicle 20, record 24, refund_record 3, system_config 2, wechat_user 2 |
| `iot_device_channel` | 2 |
| `iot_device_event_log` | 979（大表，分批） |
| `iot_device_display_config` | 8 |
| `iot_device_group` | 2 |
| `iot_ota_*` | firmware 2, task 7, task_record 20 |

### Phase 5 — 智慧能源（独立 /energy）

智慧能源在 4 个一级模块中，但实际数据可能与 `ibms_energy_*` 重叠（已在 Phase 2）。
本阶段确认：是否还有独立的 `/energy` 路径下的业务表（暂未发现）。

预计 Phase 5 主要做：
- 校验智慧能源 6 个子页面读取的实际表
- 如有遗漏的能源相关表，补做同步

### Phase 6 — 验证 & 收尾

- admin 登录长辉IBMS，逐模块走查
- 整理回滚脚本
- 清理 tmp_* 文件、备份文件归档

## 复杂依赖关系（Phase 执行时需建立 ID 映射临时表）

主要的父→子关联：
- `ibms_product` → `ibms_product_property`, `ibms_product_point_type`
- `iot_alarm_host` → `iot_alarm_partition`, `iot_alarm_zone`, `iot_alarm_event`
- `iot_camera_cruise` → `iot_camera_cruise_point`
- `iot_cloud_defense_area` → `iot_cloud_defense_area_device_rel`
- `iot_video_view_group` → `iot_video_view`, `iot_video_view_pane`
- `iot_video_patrol_scene` → `iot_video_patrol_scene_channel`
- `iot_epatrol_route` → `iot_epatrol_route_point`
- `iot_epatrol_plan` → `iot_epatrol_plan_period`, `iot_epatrol_task`
- `iot_epatrol_task` → `iot_epatrol_task_record`
- `iot_access_permission_group` → `iot_access_permission_group_device`, `iot_access_permission_group_person`
- `iot_access_person` → `iot_access_person_credential`, `iot_access_person_device_auth`
- `iot_access_auth_task` → `iot_access_auth_task_detail`
- `iot_keding_device` → `iot_device_channel`, `iot_access_*`
- `iot_parking_lot` → `iot_parking_gate`, `iot_parking_lane`
- `iot_parking_charge_rule` → `iot_parking_charge_rule_apply`
- `iot_ota_task` → `iot_ota_task_record`
- `campus` → `building` → `floor` → `area`
- `ibms_device` → `ibms_channel`, `ibms_device_runtime`
- `ibms_lighting_gateway` → `ibms_lighting_controller` → `ibms_lighting_circuit` → `ibms_lighting_scene_circuit`
