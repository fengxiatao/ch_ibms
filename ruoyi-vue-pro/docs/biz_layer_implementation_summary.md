# Biz层实现总结

## 已完成的工作

### 1. 数据访问层（DAL）

#### DO类
- **IotAlarmPartitionDO** - 分区数据对象
  - 位置：`yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/dal/dataobject/alarm/IotAlarmPartitionDO.java`
  - 字段：id, hostId, partitionNo, partitionName, status, description
  - 继承：BaseDO（包含审计和租户字段）

- **IotAlarmZoneDO** - 防区数据对象（已存在）
  - 位置：`yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/dal/dataobject/alarm/IotAlarmZoneDO.java`
  - 字段：id, hostId, zoneNo, zoneName, zoneType, areaLocation, zoneStatus, onlineStatus等

#### Mapper接口
- **IotAlarmPartitionMapper**
  - 位置：`yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/dal/mysql/alarm/IotAlarmPartitionMapper.java`
  - 方法：
    - `selectListByHostId(Long hostId)` - 查询主机的所有分区
    - `selectByHostIdAndPartitionNo(Long hostId, Integer partitionNo)` - 查询特定分区

- **IotAlarmZoneMapper**（已存在）
  - 位置：`yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/dal/mysql/alarm/IotAlarmZoneMapper.java`

### 2. 业务逻辑层（Service）

#### IotAlarmPartitionService
- 位置：`yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/service/alarm/IotAlarmPartitionService.java`
- 方法：
  - `getPartitionListByHostId(Long hostId)` - 查询分区列表
  - `queryAndSyncHostStatus(Long hostId)` - 查询并同步主机状态
  - `syncHostStatusToDatabase(Long hostId, IotAlarmHostStatusRespVO statusDTO)` - 同步状态到数据库

#### IotAlarmPartitionServiceImpl
- 位置：`yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/service/alarm/IotAlarmPartitionServiceImpl.java`
- 核心功能：
  1. **查询分区列表**：从数据库查询分区，统计防区数量
  2. **调用Gateway查询状态**：通过HTTP调用Gateway的查询接口
  3. **解析响应数据**：解析JSON响应，提取系统状态、分区状态、防区状态
  4. **同步到数据库**：将查询到的状态更新到数据库

### 3. 视图对象层（VO）

#### IotAlarmPartitionRespVO
- 位置：`yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/controller/admin/alarm/vo/partition/IotAlarmPartitionRespVO.java`
- 字段：id, hostId, partitionNo, partitionName, status, description, zoneCount, createTime

#### IotAlarmHostStatusRespVO
- 位置：`yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/controller/admin/alarm/vo/host/IotAlarmHostStatusRespVO.java`
- 字段：account, systemStatus, partitions, zones, queryTime, rawData
- 内部类：
  - `PartitionStatus` - 分区状态（partitionNo, status）
  - `ZoneStatus` - 防区状态（zoneNo, status, alarmStatus）

#### IotAlarmZoneRespVO（已存在）
- 位置：`yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/controller/admin/alarm/vo/zone/IotAlarmZoneRespVO.java`

### 4. 控制器层（Controller）

#### IotAlarmHostController（扩展）
- 位置：`yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/controller/admin/alarm/IotAlarmHostController.java`
- 新增接口：

1. **GET /iot/alarm/host/{id}/partitions**
   - 功能：查询主机分区列表
   - 权限：`iot:alarm-host:query`
   - 返回：`List<IotAlarmPartitionRespVO>`

2. **GET /iot/alarm/host/{id}/zones**
   - 功能：查询主机防区列表
   - 权限：`iot:alarm-host:query`
   - 返回：`List<IotAlarmZoneRespVO>`

3. **POST /iot/alarm/host/{id}/query-status**
   - 功能：查询主机实时状态
   - 权限：`iot:alarm-host:query`
   - 返回：`IotAlarmHostStatusRespVO`

### 5. Gateway层扩展

#### AlarmHostQueryController
- 位置：`yudao-module-iot-gateway/src/main/java/cn/iocoder/yudao/module/iot/gateway/controller/AlarmHostQueryController.java`
- 接口：

**GET /api/alarm/host/{account}/query-status**
- 参数：account（主机账号）, sequence（序列号）
- 功能：接收Biz层的查询请求，调用下行处理器查询主机状态
- 返回：JSON格式的主机状态数据

#### AlarmProtocolConfig
- 位置：`yudao-module-iot-gateway/src/main/java/cn/iocoder/yudao/module/iot/gateway/config/AlarmProtocolConfig.java`
- 功能：注册`IotAlarmDownstreamHandler`为Spring Bean

## 数据流程

```
前端 → Biz Controller → Biz Service → Gateway HTTP API → Gateway Downstream Handler → 报警主机
                                                                                          ↓
前端 ← Biz Controller ← Biz Service ← Gateway HTTP API ← Gateway Downstream Handler ← 报警主机
```

### 详细流程

1. **前端发起查询**
   - 用户点击"查看详情"按钮
   - 调用：`POST /admin-api/iot/alarm/host/109/query-status`

2. **Biz层处理**
   - `IotAlarmHostController.queryHostStatus()` 接收请求
   - 调用 `IotAlarmPartitionService.queryAndSyncHostStatus(109)`
   - 查询主机信息，获取account字段

3. **调用Gateway**
   - 构造URL：`http://127.0.0.1:48082/api/alarm/host/1234/query-status?sequence=xxx`
   - 使用HttpUtils.get()发送请求

4. **Gateway处理**
   - `AlarmHostQueryController.queryHostStatus()` 接收请求
   - 调用 `IotAlarmDownstreamHandler.queryHostStatus(account, sequence)`
   - 下行处理器发送查询命令到报警主机：`C1234,10,0,9876,131\n`

5. **报警主机响应**
   - 返回状态数据：`c1234,0,131ÉS0aaaaaaAB`
   - Gateway解析响应，提取系统状态和防区状态

6. **返回Biz层**
   - Gateway返回JSON格式的状态数据
   - Biz层解析JSON，构造`IotAlarmHostStatusRespVO`
   - 调用 `syncHostStatusToDatabase()` 同步到数据库

7. **返回前端**
   - Biz层返回完整的状态信息
   - 前端展示分区和防区状态

## API接口文档

### 1. 查询分区列表

**请求**
```
GET /admin-api/iot/alarm/host/109/partitions
```

**响应**
```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "hostId": 109,
      "partitionNo": 1,
      "partitionName": "一楼分区",
      "status": 0,
      "description": "一楼所有防区",
      "zoneCount": 8,
      "createTime": "2025-12-01T20:00:00"
    }
  ]
}
```

### 2. 查询防区列表

**请求**
```
GET /admin-api/iot/alarm/host/109/zones
```

**响应**
```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "hostId": 109,
      "zoneNo": 1,
      "zoneName": "前门",
      "zoneType": "DOOR",
      "zoneStatus": "DISARM",
      "onlineStatus": 1,
      "alarmCount": 0,
      "lastAlarmTime": null
    }
  ]
}
```

### 3. 查询实时状态

**请求**
```
POST /admin-api/iot/alarm/host/109/query-status
```

**响应**
```json
{
  "code": 0,
  "data": {
    "account": "1234",
    "systemStatus": 0,
    "partitions": [
      {
        "partitionNo": 1,
        "status": 0
      }
    ],
    "zones": [
      {
        "zoneNo": 1,
        "status": 0,
        "alarmStatus": 0
      },
      {
        "zoneNo": 7,
        "status": 1,
        "alarmStatus": 0
      },
      {
        "zoneNo": 8,
        "status": 1,
        "alarmStatus": 1
      }
    ],
    "queryTime": "2025-12-01T21:30:00",
    "rawData": "c1234,0,131ÉS0aaaaaaAB"
  }
}
```

## 后续步骤

### 1. 执行数据库脚本
```sql
-- 执行此脚本创建分区和防区表
f:\work\ch_ibms\ruoyi-vue-pro\sql\mysql\alarm_host_zone_tables.sql
```

### 2. 重启服务
- 重启Gateway服务（端口48082）
- 重启Biz服务（端口48888）

### 3. 测试接口
使用Postman或前端测试以下接口：
1. 查询分区列表
2. 查询防区列表
3. 查询实时状态

### 4. 前端实现
在报警主机列表页面添加：
- "查看详情"按钮
- 分区/防区展示组件
- 实时状态刷新功能

## 注意事项

1. **Gateway端口配置**
   - 确保Gateway运行在48082端口
   - Biz层配置：`iot.gateway.base-url=http://127.0.0.1:48082`

2. **主机账号配置**
   - 报警主机必须配置account字段
   - account用于识别和查询主机

3. **超时处理**
   - 查询超时时间：10秒
   - 超时后返回错误提示

4. **权限控制**
   - 所有接口都需要`iot:alarm-host:query`权限
   - 确保角色已分配相应权限

5. **错误处理**
   - 主机离线：返回"主机未连接"错误
   - 查询超时：返回"查询超时"错误
   - 解析失败：返回"查询失败"错误

## 文件清单

### Biz层
- `IotAlarmPartitionDO.java` - 分区DO
- `IotAlarmPartitionMapper.java` - 分区Mapper
- `IotAlarmPartitionRespVO.java` - 分区响应VO
- `IotAlarmHostStatusRespVO.java` - 主机状态响应VO
- `IotAlarmPartitionService.java` - 分区服务接口
- `IotAlarmPartitionServiceImpl.java` - 分区服务实现
- `IotAlarmHostController.java` - 主机控制器（扩展）

### Gateway层
- `AlarmHostQueryController.java` - 查询控制器
- `AlarmProtocolConfig.java` - 协议配置

### 数据库
- `alarm_host_zone_tables.sql` - 表结构脚本

### 文档
- `alarm_host_zone_drill_down_implementation.md` - 实现方案文档
- `biz_layer_implementation_summary.md` - Biz层实现总结（本文档）
