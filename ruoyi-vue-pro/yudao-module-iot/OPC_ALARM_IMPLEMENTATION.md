# OPC报警数据处理完整实现

## 概述

本文档说明IP9500 OPC报警主机的数据处理完整流程，从Gateway接收到前端展示的全链路实现。

## 数据流程图

```
IP9500报警主机 (192.168.1.210)
    ↓ TCP连接 (端口8092)
Gateway - IotOpcTextMessageHandler
    ↓ 解析文本协议
Gateway - OpcProtocolCodec
    ↓ 发布到消息总线
IotMessageBus (Topic: iot_opc_alarm_event)
    ↓ 订阅消费
Biz - OpcAlarmEventConsumer
    ├─→ TDengine (时序数据库)
    └─→ WebSocket (实时推送)
         ↓
    前端 (实时显示报警)
```

## 已实现的组件

### 1. Gateway层

#### 1.1 消息处理器
**文件**: `IotOpcTextMessageHandler.java`

**功能**:
- 接收TCP连接的文本消息
- 解析OPC协议（E/e/C消息）
- 发送ACK确认
- 发布到消息总线

**关键代码**:
```java
private void publishAlarmEvent(OpcMessage message) {
    OpcAlarmEvent event = OpcAlarmEvent.builder()
            .account(message.getAccount())
            .eventCode(message.getEventCode())
            // ... 其他字段
            .build();
    
    messageBus.post(IotMessageTopics.OPC_ALARM_EVENT, event);
}
```

#### 1.2 协议编解码器
**文件**: `OpcProtocolCodec.java`

**支持的消息格式**:
- 事件上报: `E{account},{event_code}{area}{point}{sequence}\n`
- 事件确认: `e{account},{sequence}\n`
- 控制命令: `C{account},{cmd},{area},{password},{sequence}\n`

### 2. 消息总线

#### 2.1 主题定义
**文件**: `IotMessageTopics.java`

```java
String OPC_ALARM_EVENT = "iot_opc_alarm_event";
```

#### 2.2 消息类型
**文件**: `OpcAlarmEvent.java`

**字段**:
- account: 账号（主机标识）
- eventCode: 事件代码
- area: 防区号
- point: 点位号
- sequence: 序列号
- level: 事件级别（info/warning/error/critical）
- type: 事件类型（alarm/restore/status/test）
- 其他扩展字段...

### 3. Biz层

#### 3.1 消息消费者
**文件**: `OpcAlarmEventConsumer.java`

**功能**:
1. 订阅OPC报警事件
2. 保存到TDengine
3. 推送到WebSocket

**关键代码**:
```java
@Override
public void onMessage(OpcAlarmEvent event) {
    // 1. 保存到TDengine
    alarmRecordService.saveAlarmRecord(event);
    
    // 2. 推送到WebSocket
    pushToWebSocket(event);
}
```

#### 3.2 存储服务
**文件**: `OpcAlarmRecordServiceImpl.java`

**功能**: 将报警事件保存到TDengine时序数据库

#### 3.3 数据访问层
**文件**: 
- `OpcAlarmRecordDO.java` - 数据对象
- `OpcAlarmRecordMapper.java` - MyBatis Mapper
- `OpcAlarmRecordMapper.xml` - SQL映射

### 4. TDengine数据库

#### 4.1 表结构
**文件**: `opc_alarm_record.sql`

**超级表定义**:
```sql
CREATE STABLE opc_alarm_record (
    ts TIMESTAMP,              -- 时间戳
    event_code INT,            -- 事件代码
    area SMALLINT,             -- 防区号
    point SMALLINT,            -- 点位号
    -- ... 其他字段
) TAGS (
    account INT                -- 账号（TAG）
);
```

**特点**:
- 使用超级表（STABLE）支持多账号
- account作为TAG，自动分表
- 时序存储，高效查询

### 5. WebSocket推送

#### 5.1 推送服务
**文件**: `IotMessagePushService.java`

**已有方法**:
```java
public void pushAlarmEvent(AlarmEventMessage alarm)
```

**消息格式**:
```java
AlarmEventMessage.builder()
    .id(...)
    .type("opc_alarm")
    .level("critical")
    .title("防区报警 - 账号1001 防区01 点位003")
    .content("...")
    .timestamp(...)
    .build()
```

## 配置说明

### 1. Gateway配置

**文件**: `application.yml`

```yaml
yudao:
  iot:
    gateway:
      protocol:
        opc:
          enabled: true           # 启用OPC协议
          port: 8092             # TCP监听端口
          center-code: "0001"    # 中心编号
```

### 2. TDengine配置

确保TDengine数据源已配置：

```yaml
spring:
  datasource:
    tdengine:
      url: jdbc:TAOS://localhost:6030/iot_db
      username: root
      password: taosdata
```

### 3. 消息总线配置

默认使用本地消息总线，生产环境可切换到RocketMQ。

## 使用流程

### 1. 启动服务

```bash
# 启动Gateway
cd yudao-module-iot-gateway
mvn spring-boot:run

# 启动Biz
cd yudao-module-iot-biz
mvn spring-boot:run
```

### 2. 配置报警主机

在IP9500报警主机上配置：
- 中心IP: Gateway服务器IP
- 中心端口: 8092
- 通信协议: TCP

### 3. 测试连接

使用telnet测试：
```bash
telnet localhost 8092
E1001,11020030011234
# 应收到: e1001,1234
```

### 4. 查看数据

#### 4.1 查看TDengine数据
```sql
-- 查询最近100条报警
SELECT * FROM opc_alarm_record ORDER BY ts DESC LIMIT 100;

-- 查询指定账号的报警
SELECT * FROM opc_alarm_record WHERE account = 1001 ORDER BY ts DESC;

-- 按级别统计
SELECT level, COUNT(*) FROM opc_alarm_record GROUP BY level;
```

#### 4.2 查看WebSocket推送
前端连接WebSocket后，会实时收到报警推送：
```javascript
{
  "type": "alarm",
  "data": {
    "id": 1234,
    "type": "opc_alarm",
    "level": "critical",
    "title": "防区报警 - 账号1001 防区01 点位003",
    "content": "...",
    "timestamp": 1234567890
  }
}
```

## 事件代码说明

根据C++源码分析：

| 事件代码范围 | 说明 | 级别 | 类型 |
|------------|------|------|------|
| 0 | 链路测试 | info | test |
| 1100-1199 | 防区报警 | critical | alarm |
| 1200-1299 | 防区恢复 | info | restore |
| 1300-1399 | 设备状态 | warning | status |

## 扩展功能

### 1. 设备映射

在Consumer中可以根据account映射到系统中的设备：

```java
private Long getDeviceIdByAccount(Integer account) {
    // TODO: 从数据库查询account对应的deviceId
    return deviceService.getDeviceByOpcAccount(account);
}
```

### 2. 防区配置

可以维护防区配置表，存储防区名称、位置等信息：

```sql
CREATE TABLE opc_area_config (
    account INT,
    area INT,
    area_name VARCHAR(100),
    location VARCHAR(200),
    PRIMARY KEY (account, area)
);
```

### 3. 报警规则引擎

在Consumer中可以集成报警规则引擎：

```java
private void triggerAlarmRules(OpcAlarmEvent event) {
    // 根据事件代码、防区等条件触发不同的报警规则
    if (event.getEventCode() >= 1100 && event.getEventCode() < 1200) {
        // 触发防区报警规则
        alarmRuleEngine.trigger("zone_alarm", event);
    }
}
```

### 4. 报警处理

可以添加报警处理功能：

```java
public void handleAlarm(Long alarmId, String handleBy, String remark) {
    // 更新TDengine中的处理状态
    // 发送处理通知
}
```

## 监控和日志

### 1. 日志级别

```yaml
logging:
  level:
    cn.iocoder.yudao.module.iot.gateway.protocol.opc: DEBUG
    cn.iocoder.yudao.module.iot.mq.consumer.opc: INFO
```

### 2. 关键日志

- Gateway接收消息: `[handleEvent][事件上报]`
- 发布到消息总线: `[publishAlarmEvent][发布报警事件]`
- Biz消费消息: `[onMessage][收到OPC报警事件]`
- 保存到TDengine: `[saveAlarmRecord][保存成功]`
- WebSocket推送: `[pushToWebSocket][推送报警事件成功]`

## 故障排查

### 1. 收不到消息

检查：
- Gateway是否启动
- 端口8092是否开放
- 报警主机配置是否正确
- 网络连通性

### 2. 消息未保存到TDengine

检查：
- TDengine是否运行
- 数据源配置是否正确
- 表结构是否创建
- 日志中是否有错误

### 3. WebSocket未推送

检查：
- Biz服务是否启动
- 消息总线是否正常
- WebSocket连接是否建立
- 日志中是否有错误

## 总结

完整的OPC报警数据处理流程已实现：

✅ Gateway层接收和解析
✅ 消息总线解耦通信
✅ Biz层业务处理
✅ TDengine时序存储
✅ WebSocket实时推送

下一步可以：
- 添加前端报警展示页面
- 实现报警处理功能
- 集成报警规则引擎
- 添加报警统计分析

---

**文档版本**: v1.0  
**最后更新**: 2025-11-28  
**维护者**: 长辉信息科技有限公司
