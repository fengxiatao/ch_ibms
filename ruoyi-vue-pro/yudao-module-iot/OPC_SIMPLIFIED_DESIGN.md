# IP9500 OPC协议集成 - 简化设计方案

## 一、核心原则

**不创建新表，复用现有IoT设备体系！**

## 二、数据库设计

### 2.1 复用现有表

#### 1. `iot_device` 表（已存在）
OPC报警主机作为一种IoT设备，直接使用现有设备表：

```java
IotDeviceDO {
    id              // 设备ID
    deviceName      // 设备名称，如"1号楼报警主机"
    serialNumber    // 存储OPC账号（account），如"1001"
    productId       // 关联OPC产品
    deviceKey       // 设备唯一标识，如"opc_1001"
    state           // 设备状态（在线/离线）
    ip              // IP地址（连接时自动获取）
    onlineTime      // 最后上线时间
    offlineTime     // 最后离线时间
    subsystemCode   // "security.alarm"（报警子系统）
}
```

**字段映射：**
- `serialNumber` → OPC账号（account）
- `ip` → 报警主机IP地址
- `state` → 连接状态

#### 2. `iot_product` 表（已存在）
创建"OPC报警主机"产品类型：

```sql
INSERT INTO iot_product (
    name, product_key, device_type, 
    subsystem_code, protocol_type
) VALUES (
    'IP9500报警主机', 'opc_alarm_host', 1,
    'security.alarm', 'OPC'
);
```

### 2.2 新增配置表（仅1张）

#### `iot_opc_zone_config` - 防区配置表

```sql
CREATE TABLE `iot_opc_zone_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `device_id` bigint NOT NULL COMMENT '设备ID（关联iot_device.id）',
  `area` int NOT NULL COMMENT '防区号（01-99）',
  `point` int NOT NULL COMMENT '点位号（001-999）',
  `zone_name` varchar(100) COMMENT '防区名称（手动配置）',
  `location` varchar(200) COMMENT '位置信息（手动配置）',
  `camera_id` bigint COMMENT '关联摄像头ID（手动配置）',
  `enabled` bit(1) DEFAULT b'1' COMMENT '是否启用',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_area_point` (`device_id`, `area`, `point`, `deleted`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OPC防区配置表';
```

**说明：**
- `device_id` 关联到 `iot_device.id`
- 防区配置是手动维护的本地数据
- 协议本身不提供防区配置信息

### 2.3 TDengine表（已实现）

```sql
-- OPC报警记录超级表（已实现）
CREATE STABLE opc_alarm_record (
    ts TIMESTAMP,
    event_code INT,
    area SMALLINT,
    point SMALLINT,
    sequence INT,
    event_description NCHAR(200),
    level NCHAR(20),
    type NCHAR(20),
    receive_time TIMESTAMP,
    remote_address NCHAR(50),
    remote_port INT,
    raw_message NCHAR(500),
    device_id BIGINT,
    device_name NCHAR(100)
) TAGS (
    account INT
);
```

## 三、业务流程

### 3.1 设备添加流程

```
1. 创建OPC产品（一次性）
   ↓
2. 添加OPC设备
   - 输入设备名称："1号楼报警主机"
   - 输入OPC账号（serialNumber）："1001"
   - 选择产品："IP9500报警主机"
   - 系统自动生成 deviceKey："opc_1001"
   ↓
3. 配置防区（可选）
   - 手动添加防区配置
   - 配置防区名称、位置
   - 关联摄像头
   ↓
4. 报警主机连接
   - 主机发送事件：E1001,11010030011234
   - Gateway根据account(1001)查找设备
   - 更新设备状态为在线
   - 更新设备IP地址
```

### 3.2 事件处理流程

```
报警主机发送事件
    ↓
Gateway接收（IotOpcTextMessageHandler）
    ↓
解析消息：account=1001, eventCode=1101, area=01, point=003
    ↓
查询设备：SELECT * FROM iot_device WHERE serial_number='1001'
    ↓
查询防区配置：SELECT * FROM iot_opc_zone_config 
              WHERE device_id=? AND area=1 AND point=3
    ↓
构建报警事件（OpcAlarmEvent）
    - deviceId: 从iot_device获取
    - deviceName: 从iot_device获取
    - zoneName: 从iot_opc_zone_config获取（如果有配置）
    - location: 从iot_opc_zone_config获取（如果有配置）
    - cameraId: 从iot_opc_zone_config获取（如果有配置）
    ↓
发布到消息总线
    ↓
Biz层消费
    ├─→ 保存到TDengine
    ├─→ 推送到WebSocket
    └─→ 触发视频联动（如果配置了摄像头）
```

## 四、代码实现

### 4.1 设备查询Service

```java
@Service
public class OpcDeviceService {
    
    @Resource
    private IotDeviceService deviceService;
    
    /**
     * 根据OPC账号查询设备
     */
    public IotDeviceDO getDeviceByAccount(Integer account) {
        return deviceService.getDeviceBySerialNumber(String.valueOf(account));
    }
    
    /**
     * 更新设备在线状态
     */
    public void updateDeviceOnline(Long deviceId, String ip) {
        IotDeviceDO device = new IotDeviceDO();
        device.setId(deviceId);
        device.setState(IotDeviceStateEnum.ONLINE.getState());
        device.setIp(ip);
        device.setOnlineTime(LocalDateTime.now());
        deviceService.updateDevice(device);
    }
}
```

### 4.2 消息处理器（修改）

```java
@Slf4j
public class IotOpcTextMessageHandler implements Handler<NetSocket> {
    
    private final IotMessageBus messageBus;
    private final OpcDeviceService opcDeviceService;
    private final OpcZoneConfigService zoneConfigService;
    
    @Override
    public void handle(NetSocket socket) {
        socket.handler(buffer -> {
            String message = buffer.toString();
            OpcMessage opcMessage = codec.decode(message);
            
            if (opcMessage.getType() == OpcMessageType.EVENT) {
                handleEvent(opcMessage, socket);
            }
        });
    }
    
    private void handleEvent(OpcMessage message, NetSocket socket) {
        // 1. 根据account查询设备
        IotDeviceDO device = opcDeviceService.getDeviceByAccount(message.getAccount());
        if (device == null) {
            log.warn("[handleEvent][设备不存在] account={}", message.getAccount());
            return;
        }
        
        // 2. 更新设备在线状态
        String remoteAddress = socket.remoteAddress().host();
        opcDeviceService.updateDeviceOnline(device.getId(), remoteAddress);
        
        // 3. 查询防区配置
        OpcZoneConfigDO zoneConfig = zoneConfigService.getZoneConfig(
            device.getId(), message.getArea(), message.getPoint());
        
        // 4. 构建报警事件
        OpcAlarmEvent event = OpcAlarmEvent.builder()
                .account(message.getAccount())
                .eventCode(message.getEventCode())
                .area(message.getArea())
                .point(message.getPoint())
                .sequence(message.getSequence())
                .deviceId(device.getId())
                .deviceName(device.getDeviceName())
                .zoneName(zoneConfig != null ? zoneConfig.getZoneName() : null)
                .location(zoneConfig != null ? zoneConfig.getLocation() : null)
                .cameraId(zoneConfig != null ? zoneConfig.getCameraId() : null)
                .receiveTime(LocalDateTime.now())
                .remoteAddress(remoteAddress)
                .remotePort(socket.remoteAddress().port())
                .rawMessage(message.getRawMessage())
                .tenantId(device.getTenantId())
                .build();
        
        // 5. 发布到消息总线
        messageBus.post(IotMessageTopics.OPC_ALARM_EVENT, event);
        
        // 6. 发送ACK
        if (ackEnabled) {
            sendAck(socket, message.getAccount(), message.getSequence());
        }
    }
}
```

### 4.3 布撤防控制Service

```java
@Service
public class OpcControlService {
    
    @Resource
    private IotOpcConnectionManager connectionManager;
    
    @Resource
    private OpcDeviceService opcDeviceService;
    
    /**
     * 布防
     */
    public void arm(Long deviceId) {
        IotDeviceDO device = opcDeviceService.getDeviceById(deviceId);
        Integer account = Integer.valueOf(device.getSerialNumber());
        
        String cmd = String.format("C%d,2,0,1234,%d\n", account, generateSequence());
        connectionManager.sendCommand(account, cmd);
    }
    
    /**
     * 撤防
     */
    public void disarm(Long deviceId) {
        IotDeviceDO device = opcDeviceService.getDeviceById(deviceId);
        Integer account = Integer.valueOf(device.getSerialNumber());
        
        String cmd = String.format("C%d,1,0,1234,%d\n", account, generateSequence());
        connectionManager.sendCommand(account, cmd);
    }
}
```

## 五、前端集成

### 5.1 设备管理

复用现有的设备管理页面：
- 路径：`/iot/device`
- 添加设备时选择"IP9500报警主机"产品
- 输入OPC账号到"设备序列号"字段

### 5.2 防区配置页面（新增）

```vue
<!-- 防区配置页面 -->
<template>
  <div class="opc-zone-config">
    <!-- 设备选择 -->
    <el-select v-model="selectedDeviceId">
      <el-option v-for="device in opcDevices" 
                 :key="device.id" 
                 :label="device.deviceName" 
                 :value="device.id" />
    </el-select>

    <!-- 防区列表 -->
    <el-table :data="zoneList">
      <el-table-column label="防区号" prop="area" />
      <el-table-column label="点位号" prop="point" />
      <el-table-column label="防区名称" prop="zoneName" />
      <el-table-column label="位置" prop="location" />
      <el-table-column label="关联摄像头" prop="cameraName" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button link @click="handleEdit(row)">编辑</el-button>
          <el-button link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
```

### 5.3 实时报警监控（新增）

```vue
<!-- 实时报警监控页面 -->
<template>
  <div class="opc-alarm-monitor">
    <!-- 设备列表 -->
    <el-card class="device-list">
      <div v-for="device in opcDevices" :key="device.id" 
           :class="['device-card', device.state === 1 ? 'online' : 'offline']">
        <div class="device-name">{{ device.deviceName }}</div>
        <div class="device-status">
          <el-tag :type="device.state === 1 ? 'success' : 'danger'">
            {{ device.state === 1 ? '在线' : '离线' }}
          </el-tag>
        </div>
        <div class="device-actions">
          <el-button size="small" @click="handleArm(device)">布防</el-button>
          <el-button size="small" @click="handleDisarm(device)">撤防</el-button>
        </div>
      </div>
    </el-card>

    <!-- 实时报警列表 -->
    <el-card class="alarm-list">
      <el-table :data="alarmList">
        <el-table-column label="时间" prop="receiveTime" />
        <el-table-column label="设备" prop="deviceName" />
        <el-table-column label="防区" prop="zoneName" />
        <el-table-column label="位置" prop="location" />
        <el-table-column label="事件" prop="eventDescription" />
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button link @click="handleViewCamera(row)" v-if="row.cameraId">
              查看视频
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
```

## 六、API接口

### 6.1 设备管理（复用现有接口）

```
POST   /iot/device/create        - 创建设备
PUT    /iot/device/update        - 更新设备
DELETE /iot/device/delete        - 删除设备
GET    /iot/device/get           - 获取设备详情
GET    /iot/device/page          - 获取设备分页列表
```

### 6.2 防区配置（新增接口）

```java
@RestController
@RequestMapping("/iot/opc/zone")
public class OpcZoneConfigController {
    
    @PostMapping("/create")
    CommonResult<Long> createZoneConfig(@RequestBody OpcZoneConfigCreateReqVO reqVO);
    
    @PutMapping("/update")
    CommonResult<Boolean> updateZoneConfig(@RequestBody OpcZoneConfigUpdateReqVO reqVO);
    
    @DeleteMapping("/delete")
    CommonResult<Boolean> deleteZoneConfig(@RequestParam("id") Long id);
    
    @GetMapping("/list")
    CommonResult<List<OpcZoneConfigVO>> getZoneConfigList(@RequestParam("deviceId") Long deviceId);
}
```

### 6.3 布撤防控制（新增接口）

```java
@RestController
@RequestMapping("/iot/opc/control")
public class OpcControlController {
    
    @PostMapping("/arm")
    CommonResult<Boolean> arm(@RequestParam("deviceId") Long deviceId);
    
    @PostMapping("/disarm")
    CommonResult<Boolean> disarm(@RequestParam("deviceId") Long deviceId);
    
    @GetMapping("/status")
    CommonResult<OpcDeviceStatusVO> getStatus(@RequestParam("deviceId") Long deviceId);
}
```

### 6.4 报警记录（新增接口）

```java
@RestController
@RequestMapping("/iot/opc/alarm")
public class OpcAlarmController {
    
    @GetMapping("/page")
    CommonResult<PageResult<OpcAlarmRecordVO>> getAlarmPage(@Valid OpcAlarmPageReqVO reqVO);
    
    @GetMapping("/realtime")
    // WebSocket: ws://host:port/ws/opc/alarm
}
```

## 七、优势总结

### ✅ 复用现有体系
1. **不创建新表** - 只增加1张防区配置表
2. **复用设备管理** - 统一的设备管理界面
3. **复用权限体系** - 租户隔离、权限控制
4. **复用监控体系** - 设备在线/离线监控

### ✅ 简化实现
1. **数据库简单** - 2张表（iot_device + iot_opc_zone_config）
2. **代码量少** - 复用大量现有代码
3. **维护方便** - 统一的设备管理流程

### ✅ 功能完整
1. **实时报警** - 核心功能
2. **远程控制** - 布防/撤防
3. **视频联动** - 关联摄像头
4. **历史查询** - TDengine存储

## 八、实施步骤

1. ✅ **创建OPC产品** - 在产品管理中添加
2. ✅ **创建防区配置表** - 执行SQL
3. ✅ **实现防区配置接口** - Controller + Service
4. ✅ **修改消息处理器** - 关联设备和防区
5. ✅ **实现布撤防接口** - 控制命令发送
6. ✅ **开发前端页面** - 防区配置、实时监控

**总结：不需要 `iot_opc_device` 表，直接使用 `iot_device` 表！**
