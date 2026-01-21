# 12种智慧建筑设备产品与物模型导入指南

## 📋 概述

本文档提供12种智慧建筑常用设备的产品和物模型导入指南，适用于电子地图系统的设备管理。

## 🎯 设备类型清单

| 序号 | 设备名称 | Product Key | 设备分类 | 图标 |
|-----|---------|-------------|---------|------|
| 1 | 枪型摄像机 | bullet_camera_001 | 视频监控 | ep:camera |
| 2 | 半球摄像机 | dome_camera_001 | 视频监控 | ep:video-camera |
| 3 | 球形摄像机 | ptz_camera_001 | 视频监控 | ep:camera-filled |
| 4 | 车辆道闸 | vehicle_barrier_001 | 出入控制 | ep:unlock |
| 5 | 车辆识别一体机 | vehicle_recognition_001 | 出入控制 | ep:document-checked |
| 6 | 人行闸机 | pedestrian_gate_001 | 出入控制 | ep:lock |
| 7 | 人脸识别一体机 | face_recognition_001 | 出入控制 | ep:user |
| 8 | 巡更点 | patrol_point_001 | 安防管理 | ep:location |
| 9 | 水表 | water_meter_001 | 能耗监测 | ep:operation |
| 10 | 电表 | electricity_meter_001 | 能耗监测 | ep:odometer |
| 11 | 燃气表 | gas_meter_001 | 能耗监测 | ep:aim |
| 12 | 考勤机 | attendance_machine_001 | 人员管理 | ep:calendar |

## 📁 文件说明

```
ruoyi-vue-pro/sql/postgresql/
├── iot/
│   ├── insert_12_device_products.sql    # 产品插入SQL（必需）
│   └── README_12_DEVICES.md             # 本文档
└── thingmodel/
    └── 12_devices_thing_model.json      # 物模型定义JSON（可选）
```

## 🚀 快速开始

### 步骤1：执行产品插入SQL

**方式A：使用图形化工具（推荐）**

1. 打开 pgAdmin / DataGrip / Navicat
2. 连接到 `ibms_gis` 数据库
3. 打开 `insert_12_device_products.sql` 文件
4. 执行SQL脚本

**方式B：使用命令行**

```bash
psql -h 192.168.1.126 -U postgres -d ibms_gis -f insert_12_device_products.sql
```

### 步骤2：验证产品数据

```sql
SELECT id, name, product_key, status, device_type
FROM iot_product
WHERE product_key LIKE '%_001'
  AND product_key IN (
    'bullet_camera_001', 'dome_camera_001', 'ptz_camera_001',
    'vehicle_barrier_001', 'vehicle_recognition_001', 'pedestrian_gate_001',
    'face_recognition_001', 'patrol_point_001', 'water_meter_001',
    'electricity_meter_001', 'gas_meter_001', 'attendance_machine_001'
  )
ORDER BY id;
```

**预期结果**：应返回12条产品记录

### 步骤3：导入物模型（可选）

物模型可以通过以下方式导入：

#### 方式A：通过前端界面（推荐）

1. 登录系统管理后台
2. 进入 **产品管理** → **产品列表**
3. 点击对应产品的 **物模型** 按钮
4. 使用 **导入JSON** 功能
5. 选择 `12_devices_thing_model.json` 中对应设备的物模型定义

#### 方式B：使用API接口

```bash
# 示例：为枪型摄像机导入物模型
curl -X POST "http://localhost:48080/admin-api/iot/thing-model/import" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d @12_devices_thing_model.json
```

#### 方式C：直接插入数据库（不推荐）

需要根据 `12_devices_thing_model.json` 手动构造SQL插入语句。

## 📊 物模型概览

### 1. 通用属性（所有设备）

- `online_status` - 在线状态（布尔）
- `device_status` - 设备状态（枚举：离线/在线正常/在线异常）

### 2. 摄像机专用功能

**属性：**
- `rtsp_url` - RTSP流地址
- `resolution` - 分辨率
- `ptz_support` - 云台支持（仅球机）

**服务：**
- `snapshot` - 抓拍快照
- `getPlayUrl` - 获取播放地址
- `ptz_control` - 云台控制（仅球机）

### 3. 门禁/道闸专用功能

**属性：**
- `gate_status` - 闸机状态（关闭/开启/运动中）

**服务：**
- `open` - 开闸
- `close` - 关闸

### 4. 识别设备专用功能

**事件：**
- `vehicle_recognized` - 车辆识别事件（车牌号、置信度）
- `face_recognized` - 人脸识别事件（人员ID、置信度）
- `patrol_checked` - 巡更打卡事件（保安ID、打卡时间）
- `attendance_recorded` - 考勤打卡事件（员工ID、打卡时间、打卡类型）

### 5. 能耗表计专用功能

**属性：**
- `water_usage` - 用水量（水表，立方米）
- `electricity_usage` - 用电量（电表，千瓦时）
- `gas_usage` - 用气量（燃气表，立方米）
- `power` - 当前功率（电表，瓦）
- `voltage` - 电压（电表，伏）
- `current` - 电流（电表，安）
- `gas_pressure` - 燃气压力（燃气表，千帕）

**事件：**
- `gas_leak` - 燃气泄漏告警（燃气表）

## 🎨 前端集成

### 电子地图编辑器

**新增功能：所属区域选择**

在设备属性面板中，新增"所属区域"下拉框，可以选择当前楼层下的所有区域：

```vue
<el-form-item label="所属区域">
  <el-select 
    v-model="selectedDevice.roomId" 
    placeholder="选择区域"
    filterable
  >
    <el-option 
      v-for="area in floorAreas" 
      :key="area.id" 
      :label="area.areaName" 
      :value="area.id"
    />
  </el-select>
</el-form-item>
```

**数据绑定：**
- 字段名：`roomId` (Long)
- 关联表：`area` (区域表)
- 自动加载：组件加载时自动从API获取楼层区域列表
- 保存时自动更新到 `iot_device.room_id` 字段

## 🔧 自定义物模型

如需自定义物模型，可参考以下JSON结构：

```json
{
  "identifier": "custom_property",
  "name": "自定义属性",
  "description": "属性描述",
  "accessMode": "r",  // r-只读, rw-读写
  "required": false,
  "dataType": "int",  // bool, int, float, text, enum, struct, date
  "dataSpecs": {
    "dataType": "int",
    "min": "0",
    "max": "100",
    "unit": "°C",
    "unitName": "摄氏度"
  }
}
```

## ⚠️ 注意事项

1. **租户隔离**：所有产品数据都关联 `tenant_id = 1`，多租户环境需修改
2. **唯一约束**：`product_key` 有唯一约束，重复执行SQL不会报错（使用 `ON CONFLICT DO NOTHING`）
3. **菜单关联**：目前所有产品的 `menu_ids = '[1]'`，根据实际菜单ID调整
4. **codecType**：摄像机使用 `onvif`，其他设备使用 `custom`
5. **离线检查**：摄像机和门禁设备配置了离线检查定时任务

## 📞 问题排查

### 问题1：SQL执行失败（序列不存在）

**错误**：`ERROR: relation "iot_product_seq" does not exist`

**解决**：
```sql
-- 创建序列（PostgreSQL）
CREATE SEQUENCE IF NOT EXISTS iot_product_seq START 1000;
```

### 问题2：产品未显示在前端

**排查步骤**：
1. 检查 `status` 字段是否为 `1`（启用状态）
2. 检查 `tenant_id` 是否匹配当前用户的租户ID
3. 清除浏览器缓存，重新登录

### 问题3：区域下拉框为空

**排查步骤**：
1. 确认当前楼层是否已配置区域数据
2. 检查 `area` 表中是否有 `floor_id` 匹配的记录
3. 检查浏览器控制台是否有API错误

## 📚 相关文档

- [物模型定义规范](../thingmodel/README.md)
- [设备激活流程](../../../docs/best-practices/设备激活完整业务流程.md)
- [电子地图功能说明](../../../docs/摄像头坐标提取-功能实现完成报告.md)

## ✅ 完成检查清单

- [x] 执行 `insert_12_device_products.sql`
- [x] 验证12条产品记录已插入
- [ ] （可选）为每个产品导入物模型
- [ ] 测试前端设备选择产品类型
- [ ] 测试前端设备选择所属区域
- [ ] 测试设备创建和保存功能

---

**创建时间**：2025-11-04  
**版本**：v1.0  
**维护者**：长辉信息科技有限公司

