# 报警主机区域/防区钻取功能实现方案

## 1. 功能概述

实现报警主机列表的钻取功能，用户点击主机后可以查看：
- 主机的分区列表（Partition）
- 每个分区下的防区列表（Zone）
- 每个防区的实时状态（布防/撤防/报警）

## 2. 协议解析

### 2.1 查询命令格式
```
中心→主机：C{account},10,0,9876,{sequence}
```
- `C`: 查询命令标识
- `{account}`: 主机账号（如1234）
- `10`: 查询状态命令码
- `0`: 查询所有防区
- `9876`: 随机数（可固定）
- `{sequence}`: 序列号

### 2.2 响应格式
```
主机→中心：c{account},0,{sequence}ÉS0aaaaaaAB
```
- `c`: 响应标识（小写）
- `S`: 状态标记
- `S`后第一个字符：系统状态（0=撤防，1=布防）
- 后续字符：防区状态
  - 小写字母（a-z）：防区撤防
  - 大写字母（A-Z）：防区布防
  - 大写字母+数字：防区布防且报警

### 2.3 解析示例
响应：`c1234,0,131ÉS0aaaaaaAB`

解析结果：
- 系统状态：0（撤防）
- 防区1-6：撤防（aaaaaa）
- 防区7：布防（A）
- 防区8：布防且报警（B，后续可能有数字）

## 3. 数据库设计

### 3.1 分区表（iot_alarm_partition）
```sql
CREATE TABLE iot_alarm_partition (
  id bigint PRIMARY KEY AUTO_INCREMENT,
  host_id bigint NOT NULL COMMENT '报警主机ID',
  partition_no int NOT NULL COMMENT '分区编号',
  partition_name varchar(100) COMMENT '分区名称',
  status tinyint DEFAULT 0 COMMENT '布防状态：0-撤防，1-布防',
  description varchar(255) COMMENT '分区描述',
  -- 审计和租户字段
  creator varchar(64),
  create_time datetime,
  updater varchar(64),
  update_time datetime,
  deleted bit(1) DEFAULT 0,
  tenant_id bigint DEFAULT 0,
  UNIQUE KEY uk_host_partition (host_id, partition_no, deleted)
);
```

### 3.2 防区表（iot_alarm_zone）
```sql
CREATE TABLE iot_alarm_zone (
  id bigint PRIMARY KEY AUTO_INCREMENT,
  host_id bigint NOT NULL COMMENT '报警主机ID',
  partition_id bigint COMMENT '所属分区ID',
  zone_no int NOT NULL COMMENT '防区编号',
  zone_name varchar(100) COMMENT '防区名称',
  zone_type varchar(50) COMMENT '防区类型',
  status tinyint DEFAULT 0 COMMENT '布防状态：0-撤防，1-布防',
  alarm_status tinyint DEFAULT 0 COMMENT '报警状态：0-正常，1-报警',
  description varchar(255) COMMENT '防区描述',
  -- 审计和租户字段
  creator varchar(64),
  create_time datetime,
  updater varchar(64),
  update_time datetime,
  deleted bit(1) DEFAULT 0,
  tenant_id bigint DEFAULT 0,
  UNIQUE KEY uk_host_zone (host_id, zone_no, deleted)
);
```

### 3.3 状态查询日志表（iot_alarm_host_status_log）
```sql
CREATE TABLE iot_alarm_host_status_log (
  id bigint PRIMARY KEY AUTO_INCREMENT,
  host_id bigint NOT NULL,
  query_time datetime DEFAULT CURRENT_TIMESTAMP,
  system_status tinyint COMMENT '系统状态',
  zone_status_data text COMMENT '防区状态原始数据',
  partition_count int DEFAULT 0,
  zone_count int DEFAULT 0,
  creator varchar(64),
  create_time datetime,
  tenant_id bigint DEFAULT 0
);
```

## 4. 技术架构

```
前端（Vue3）
    ↓ HTTP API
Biz层（Spring Boot）
    ↓ HTTP API
Gateway层（Vert.x）
    ↓ TCP协议
报警主机设备
```

### 4.1 Gateway层

#### 已创建文件：
1. **AlarmHostStatusDTO.java** - 状态数据传输对象
2. **AlarmHostStatusParser.java** - 协议解析器
3. **IotAlarmDownstreamHandler.java** - 下行消息处理器
4. **IotAlarmConnectionManager.java** - 连接管理器（已增强）

#### 核心功能：
- 发送查询命令到报警主机
- 接收并解析响应数据
- 维护请求-响应映射（支持异步）
- 超时处理

### 4.2 Biz层

需要创建的文件：

#### 4.2.1 DO（Data Object）
- `IotAlarmPartitionDO.java` - 分区数据对象
- `IotAlarmZoneDO.java` - 防区数据对象
- `IotAlarmHostStatusLogDO.java` - 状态日志数据对象

#### 4.2.2 Mapper
- `IotAlarmPartitionMapper.java` - 分区数据访问
- `IotAlarmZoneMapper.java` - 防区数据访问
- `IotAlarmHostStatusLogMapper.java` - 状态日志数据访问

#### 4.2.3 Service
- `IotAlarmPartitionService.java` - 分区业务接口
- `IotAlarmPartitionServiceImpl.java` - 分区业务实现
- `IotAlarmZoneService.java` - 防区业务接口
- `IotAlarmZoneServiceImpl.java` - 防区业务实现

#### 4.2.4 Controller
- 在 `IotAlarmHostController.java` 中添加：
  - `GET /admin-api/iot/alarm/host/{id}/partitions` - 查询分区列表
  - `GET /admin-api/iot/alarm/host/{id}/zones` - 查询防区列表
  - `POST /admin-api/iot/alarm/host/{id}/query-status` - 查询实时状态

#### 4.2.5 VO（View Object）
- `IotAlarmPartitionRespVO.java` - 分区响应对象
- `IotAlarmZoneRespVO.java` - 防区响应对象
- `IotAlarmHostStatusRespVO.java` - 主机状态响应对象

### 4.3 前端层

需要修改的文件：
- `AlarmHost/index.vue` - 主机列表页面

需要添加的功能：
1. **钻取按钮**：在操作列添加"查看详情"按钮
2. **分区/防区展示**：
   - 使用 `el-drawer` 或 `el-dialog` 展示
   - 使用 `el-tree` 或 `el-collapse` 展示层级结构
3. **实时状态刷新**：
   - 点击"刷新状态"按钮查询最新状态
   - 使用不同颜色标识布防/撤防/报警状态

## 5. API接口设计

### 5.1 查询主机分区列表
```
GET /admin-api/iot/alarm/host/{id}/partitions
```

响应示例：
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
      "zoneCount": 6
    }
  ]
}
```

### 5.2 查询主机防区列表
```
GET /admin-api/iot/alarm/host/{id}/zones?partitionId=1
```

响应示例：
```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "hostId": 109,
      "partitionId": 1,
      "zoneNo": 1,
      "zoneName": "前门",
      "zoneType": "门磁",
      "status": 0,
      "alarmStatus": 0
    }
  ]
}
```

### 5.3 查询主机实时状态
```
POST /admin-api/iot/alarm/host/{id}/query-status
```

响应示例：
```json
{
  "code": 0,
  "data": {
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
    "queryTime": "2025-12-01 21:00:00"
  }
}
```

## 6. 实现步骤

### 6.1 Gateway层（已完成）
- ✅ 创建状态DTO和解析器
- ✅ 实现下行消息处理器
- ✅ 增强连接管理器

### 6.2 数据库层（待执行）
- ⏳ 执行 `alarm_host_zone_tables.sql` 创建表结构

### 6.3 Biz层（待实现）
- ⏳ 创建DO、Mapper、Service、Controller
- ⏳ 实现查询分区/防区接口
- ⏳ 实现查询实时状态接口
- ⏳ 实现状态同步到数据库

### 6.4 前端层（待实现）
- ⏳ 添加"查看详情"按钮
- ⏳ 实现分区/防区展示组件
- ⏳ 实现实时状态查询和刷新

## 7. 测试验证

### 7.1 Gateway层测试
1. 启动Gateway服务
2. 报警主机连接到Gateway
3. 调用查询接口，验证命令发送和响应解析

### 7.2 Biz层测试
1. 测试分区/防区CRUD接口
2. 测试实时状态查询接口
3. 验证数据同步逻辑

### 7.3 前端测试
1. 测试钻取展示功能
2. 测试实时状态刷新
3. 测试不同状态的视觉效果

## 8. 注意事项

1. **并发处理**：多个用户同时查询同一主机时，需要使用不同的sequence避免冲突
2. **超时处理**：查询超时时间设置为10秒，超时后返回错误提示
3. **离线处理**：主机离线时，显示缓存的最后状态，并标注"离线"
4. **权限控制**：添加相应的权限注解，确保只有授权用户可以查询
5. **性能优化**：对于大量防区的主机，考虑分页或懒加载

## 9. 后续优化

1. **实时推送**：使用WebSocket推送防区状态变化
2. **历史记录**：展示防区状态变化历史
3. **报警联动**：防区报警时触发其他联动动作
4. **批量操作**：支持批量布防/撤防
5. **可视化**：使用图形化方式展示防区布局
