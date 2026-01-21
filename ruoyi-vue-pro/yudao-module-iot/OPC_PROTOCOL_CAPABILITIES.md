# IP9500 OPC协议实际能力分析

## 一、协议能提供的信息（基于C++源码和协议文档）

### 1.1 报警主机 → 接警中心（上行）

#### 事件上报消息（E消息）
```
格式: E{account},{event_code}{area}{point}{sequence}\n
示例: E1001,11010030011234\n
```

**能获取的信息：**
- ✅ `account` - 主机账号（用户编号）
- ✅ `event_code` - 事件代码（4位）
- ✅ `area` - 防区号（2位，01-99）
- ✅ `point` - 点位号（3位，001-999）
- ✅ `sequence` - 序列号（4位）

**事件代码含义（根据协议截图）：**
```
0000 - 链路测试
1100-1199 - 防区报警
1200-1299 - 防区恢复
1300-1399 - 防区旁路
1400-1499 - 防区故障
3100-3199 - 主机状态（布防/撤防/故障等）
```

### 1.2 接警中心 → 报警主机（下行）

#### 1. 事件确认消息（e消息）
```
格式: e{account},{sequence}\n
示例: e1001,1234\n
```
**功能：** 确认收到事件

#### 2. 控制命令消息（C消息）
```
格式: C{account},{cmd},{area},{password},{sequence}\n
```

**支持的命令：**
- ✅ `cmd=0` - 查询状态
  ```
  C1001,0,0,0,1234\n
  ```
  
- ✅ `cmd=1` - 撤防
  ```
  C1001,1,0,1234,1234\n
  ```
  
- ✅ `cmd=2` - 布防
  ```
  C1001,2,0,1234,1234\n
  ```

**注意：** 
- 密码固定为 `1234`（硬编码在C++源码中）
- area参数为0表示全局操作

## 二、协议**不能提供**的信息

### ❌ 无法获取的配置信息
1. **防区配置**
   - 防区名称
   - 防区类型
   - 防区位置
   - 延时时间
   - 报警号

2. **设备信息**
   - 设备IP地址
   - 设备型号
   - 固件版本
   - 防区数量
   - 输出数量

3. **分区信息**
   - 分区配置
   - 分区名称
   - 分区状态

4. **输出状态**
   - 输出端口状态
   - 输出配置

5. **用户信息**
   - 用户列表
   - 用户权限

6. **历史记录**
   - 历史事件查询
   - 操作日志

### ❌ 无法执行的操作
1. **配置管理**
   - 修改防区配置
   - 修改分区配置
   - 修改系统参数

2. **输出控制**
   - 控制输出端口

3. **用户管理**
   - 添加/删除用户
   - 修改用户权限

## 三、实际可实现的功能

### 3.1 核心功能（基于协议）

#### ✅ 1. 实时事件接收
```java
/**
 * 接收报警主机上报的事件
 */
public void onEventReceived(OpcMessage message) {
    // 解析事件
    Integer account = message.getAccount();
    Integer eventCode = message.getEventCode();
    Integer area = message.getArea();
    Integer point = message.getPoint();
    Integer sequence = message.getSequence();
    
    // 根据事件代码判断事件类型
    String eventType = parseEventType(eventCode);
    String eventDescription = parseEventDescription(eventCode);
    
    // 保存到数据库
    saveAlarmRecord(account, eventCode, area, point, sequence);
    
    // 推送到前端
    pushToWebSocket(event);
}
```

#### ✅ 2. 事件确认
```java
/**
 * 向报警主机发送确认消息
 */
public void sendAck(Integer account, Integer sequence) {
    String ackMessage = String.format("e%d,%d\n", account, sequence);
    sendToDevice(ackMessage);
}
```

#### ✅ 3. 布防/撤防控制
```java
/**
 * 布防
 */
public void arm(Integer account) {
    String cmd = String.format("C%d,2,0,1234,%d\n", account, generateSequence());
    sendToDevice(cmd);
}

/**
 * 撤防
 */
public void disarm(Integer account) {
    String cmd = String.format("C%d,1,0,1234,%d\n", account, generateSequence());
    sendToDevice(cmd);
}
```

#### ✅ 4. 状态查询
```java
/**
 * 查询主机状态
 */
public void queryStatus(Integer account) {
    String cmd = String.format("C%d,0,0,0,%d\n", account, generateSequence());
    sendToDevice(cmd);
}
```

### 3.2 扩展功能（需要本地配置）

#### ✅ 5. 防区配置管理（本地数据库）
由于协议不提供防区配置信息，需要在IBMS平台本地维护：

```sql
-- 本地维护防区配置
CREATE TABLE `iot_opc_zone_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account` int NOT NULL COMMENT '主机账号',
  `area` int NOT NULL COMMENT '防区号',
  `point` int NOT NULL COMMENT '点位号',
  `zone_name` varchar(100) COMMENT '防区名称（手动配置）',
  `location` varchar(200) COMMENT '位置（手动配置）',
  `camera_id` bigint COMMENT '关联摄像头（手动配置）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_area_point` (`account`, `area`, `point`)
);
```

#### ✅ 6. 事件代码映射（本地配置）
```java
/**
 * 事件代码映射表（本地维护）
 */
private static final Map<Integer, EventInfo> EVENT_CODE_MAP = new HashMap<>();

static {
    // 链路测试
    EVENT_CODE_MAP.put(0, new EventInfo("链路测试", "test", "info"));
    
    // 防区报警 1100-1199
    for (int i = 1100; i < 1200; i++) {
        EVENT_CODE_MAP.put(i, new EventInfo("防区报警", "alarm", "critical"));
    }
    
    // 防区恢复 1200-1299
    for (int i = 1200; i < 1300; i++) {
        EVENT_CODE_MAP.put(i, new EventInfo("防区恢复", "restore", "info"));
    }
    
    // 防区旁路 1300-1399
    for (int i = 1300; i < 1400; i++) {
        EVENT_CODE_MAP.put(i, new EventInfo("防区旁路", "bypass", "warning"));
    }
    
    // 主机状态 3100-3199
    EVENT_CODE_MAP.put(3100, new EventInfo("主机布防", "arm", "info"));
    EVENT_CODE_MAP.put(3101, new EventInfo("主机撤防", "disarm", "info"));
    EVENT_CODE_MAP.put(3102, new EventInfo("主机故障", "fault", "error"));
}
```

## 四、简化的数据库设计（基于实际协议能力）

### 4.1 MySQL表（配置数据）

```sql
-- OPC设备表（简化版）
CREATE TABLE `iot_opc_device` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account` int NOT NULL COMMENT '主机账号',
  `device_name` varchar(100) NOT NULL COMMENT '设备名称',
  `ip_address` varchar(50) COMMENT 'IP地址（连接时自动获取）',
  `port` int COMMENT '端口（连接时自动获取）',
  `connection_status` tinyint DEFAULT 0 COMMENT '连接状态',
  `last_heartbeat_time` datetime COMMENT '最后心跳时间',
  `tenant_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account` (`account`)
) COMMENT='OPC设备表';

-- OPC防区配置表（手动维护）
CREATE TABLE `iot_opc_zone_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account` int NOT NULL COMMENT '主机账号',
  `area` int NOT NULL COMMENT '防区号',
  `point` int NOT NULL COMMENT '点位号',
  `zone_name` varchar(100) COMMENT '防区名称',
  `location` varchar(200) COMMENT '位置信息',
  `camera_id` bigint COMMENT '关联摄像头ID',
  `enabled` bit(1) DEFAULT b'1' COMMENT '是否启用',
  `tenant_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_area_point` (`account`, `area`, `point`)
) COMMENT='OPC防区配置表';
```

### 4.2 TDengine表（事件数据）

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
    raw_message NCHAR(500)
) TAGS (
    account INT
);
```

## 五、简化的接口设计

### 5.1 设备管理接口

```java
@RestController
@RequestMapping("/iot/opc/device")
public class OpcDeviceController {
    
    /**
     * 创建OPC设备（只需配置account和名称）
     */
    @PostMapping("/create")
    CommonResult<Long> createDevice(@RequestBody OpcDeviceCreateReqVO reqVO);
    
    /**
     * 获取设备列表
     */
    @GetMapping("/list")
    CommonResult<List<OpcDeviceRespVO>> getDeviceList();
    
    /**
     * 布防
     */
    @PostMapping("/arm")
    CommonResult<Boolean> arm(@RequestParam("account") Integer account);
    
    /**
     * 撤防
     */
    @PostMapping("/disarm")
    CommonResult<Boolean> disarm(@RequestParam("account") Integer account);
    
    /**
     * 查询状态
     */
    @GetMapping("/status")
    CommonResult<OpcDeviceStatusVO> getStatus(@RequestParam("account") Integer account);
}
```

### 5.2 防区配置接口

```java
@RestController
@RequestMapping("/iot/opc/zone")
public class OpcZoneController {
    
    /**
     * 手动配置防区信息
     */
    @PostMapping("/config")
    CommonResult<Long> configZone(@RequestBody OpcZoneConfigReqVO reqVO);
    
    /**
     * 获取防区配置列表
     */
    @GetMapping("/list")
    CommonResult<List<OpcZoneConfigVO>> getZoneList(@RequestParam("account") Integer account);
}
```

### 5.3 报警记录接口

```java
@RestController
@RequestMapping("/iot/opc/alarm")
public class OpcAlarmController {
    
    /**
     * 获取报警记录
     */
    @GetMapping("/page")
    CommonResult<PageResult<OpcAlarmRecordVO>> getAlarmPage(@Valid OpcAlarmPageReqVO reqVO);
    
    /**
     * 获取实时报警（WebSocket推送）
     */
    // WebSocket: ws://host:port/ws/opc/alarm
}
```

## 六、实际可实现的前端功能

### 6.1 设备管理页面
- ✅ 添加设备（配置account和名称）
- ✅ 查看设备列表
- ✅ 查看连接状态
- ✅ 布防/撤防按钮

### 6.2 防区配置页面
- ✅ 手动配置防区名称
- ✅ 配置防区位置
- ✅ 关联摄像头
- ❌ 无法获取主机上的防区配置

### 6.3 实时报警页面
- ✅ 实时显示报警事件
- ✅ 显示防区号、点位号
- ✅ 显示事件类型（根据事件代码判断）
- ✅ 报警确认
- ✅ 关联视频查看（如果配置了摄像头）

### 6.4 报警历史页面
- ✅ 查询历史报警记录
- ✅ 按时间、设备、防区筛选
- ✅ 导出报表

## 七、总结

### ✅ 协议能做的
1. **接收事件** - 实时接收报警、恢复、旁路等事件
2. **布防/撤防** - 远程控制主机布防/撤防
3. **状态查询** - 查询主机状态
4. **事件确认** - 向主机发送确认消息

### ❌ 协议不能做的
1. **读取配置** - 无法获取主机上的防区配置、分区配置等
2. **修改配置** - 无法修改主机配置
3. **输出控制** - 无法控制输出端口
4. **历史查询** - 无法查询主机上的历史记录

### 💡 解决方案
1. **本地维护配置** - 在IBMS平台本地数据库维护防区配置
2. **事件代码映射** - 本地维护事件代码与描述的映射关系
3. **手动关联** - 手动配置防区与摄像头的关联关系
4. **实时存储** - 将接收到的事件实时存储到TDengine

### 🎯 核心价值
虽然协议功能有限，但我们可以实现：
1. ✅ **实时报警监控** - 最核心的功能
2. ✅ **远程布撤防** - 提高便利性
3. ✅ **历史记录查询** - 基于本地存储
4. ✅ **视频联动** - 通过本地配置实现
5. ✅ **统计分析** - 基于本地数据

这已经能满足大部分安防监控的需求！
