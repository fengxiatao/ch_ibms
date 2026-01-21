# WebSocket 消息格式文档

## 概述

本文档描述了 IoT Biz 模块与前端之间 WebSocket 通信的统一消息格式。

**重构说明**：
- 所有设备类型（门禁、报警、NVR、科鼎等）现在使用统一的消息格式
- 旧的设备特定 WebSocket Handler 已合并到 `DeviceStatusWebSocketHandler`
- 通过 `deviceType` 字段区分不同设备类型

## WebSocket 端点

| 端点路径 | 说明 |
|---------|------|
| `/ws/device/status` | 统一设备状态推送端点 |
| `/ws/access/device/status` | 门禁设备状态（兼容旧端点） |
| `/ws/access/event` | 门禁事件（兼容旧端点） |
| `/ws/access/auth-task/progress` | 授权任务进度推送 |
| `/ws/keding/device` | 科鼎设备状态（兼容旧端点） |
| `/ws/keding/alarm` | 科鼎报警（兼容旧端点） |

## 消息类型

### 1. 设备状态消息 (DEVICE_STATUS)

当设备状态发生变化时推送。

```json
{
  "type": "DEVICE_STATUS",
  "data": {
    "messageType": "DEVICE_STATUS",
    "deviceId": 12345,
    "deviceType": "ACCESS_GEN2",
    "status": "ONLINE",
    "timestamp": 1703664000000
  },
  "timestamp": 1703664000000
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
|-----|------|------|
| `messageType` | String | 固定值 "DEVICE_STATUS" |
| `deviceId` | Long | 设备ID |
| `deviceType` | String | 设备类型，见下表 |
| `status` | String | 设备状态，见下表 |
| `timestamp` | Long | 时间戳（毫秒） |

**设备类型 (deviceType)**：

| 值 | 说明 |
|---|------|
| `ACCESS_GEN1` | 一代门禁设备 |
| `ACCESS_GEN2` | 二代门禁设备 |
| `ALARM` | 报警主机 |
| `NVR` | NVR 设备 |
| `CHANGHUI` | 长辉设备 |
| `KEDING` | 科鼎设备 |

**设备状态 (status)**：

| 值 | 说明 |
|---|------|
| `ONLINE` | 在线 |
| `OFFLINE` | 离线 |
| `ACTIVATED` | 已激活 |
| `INACTIVE` | 未激活 |
| `RECONNECTING` | 重连中 |
| `ACTIVATION_FAILED` | 激活失败 |

### 2. 设备事件消息 (DEVICE_EVENT)

当设备产生事件时推送。

```json
{
  "type": "DEVICE_EVENT",
  "data": {
    "messageType": "DEVICE_EVENT",
    "deviceId": 12345,
    "deviceType": "ACCESS_GEN2",
    "eventType": "CARD_SWIPE",
    "eventData": {
      "eventId": 67890,
      "personId": 100,
      "personName": "张三",
      "cardNo": "12345678",
      "verifyResult": 1,
      "captureUrl": "http://example.com/capture.jpg",
      "eventTime": "2024-01-01T10:00:00"
    },
    "timestamp": 1703664000000
  },
  "timestamp": 1703664000000
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
|-----|------|------|
| `messageType` | String | 固定值 "DEVICE_EVENT" |
| `deviceId` | Long | 设备ID |
| `deviceType` | String | 设备类型 |
| `eventType` | String | 事件类型，见下表 |
| `eventData` | Object | 事件数据，根据事件类型不同而不同 |
| `timestamp` | Long | 时间戳（毫秒） |

**门禁事件类型 (eventType)**：

| 值 | 说明 |
|---|------|
| `CARD_SWIPE` | 刷卡开门 |
| `PASSWORD` | 密码开门 |
| `FINGERPRINT` | 指纹开门 |
| `FACE_RECOGNIZE` | 人脸开门 |
| `QRCODE` | 二维码开门 |
| `REMOTE_OPEN` | 远程开门 |
| `BUTTON_OPEN` | 按钮开门 |
| `DOOR_NOT_CLOSED` | 门未关报警 |
| `BREAK_IN` | 闯入报警 |
| `DURESS` | 胁迫报警 |

**报警事件类型 (eventType)**：

| 值 | 说明 |
|---|------|
| `ALARM` | 报警事件 |
| `ARM` | 布防事件 |
| `DISARM` | 撤防事件 |
| `BYPASS` | 旁路事件 |

### 3. 命令结果消息 (COMMAND_RESULT)

当设备命令执行完成时推送。

```json
{
  "type": "COMMAND_RESULT",
  "data": {
    "messageType": "COMMAND_RESULT",
    "requestId": "abc123-def456",
    "deviceId": 12345,
    "deviceType": "ACCESS_GEN2",
    "success": true,
    "message": "命令执行成功",
    "data": {
      "doorOpened": true
    },
    "timestamp": 1703664000000
  },
  "timestamp": 1703664000000
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
|-----|------|------|
| `messageType` | String | 固定值 "COMMAND_RESULT" |
| `requestId` | String | 请求ID，用于关联命令请求 |
| `deviceId` | Long | 设备ID |
| `deviceType` | String | 设备类型 |
| `success` | Boolean | 命令是否执行成功 |
| `message` | String | 结果消息 |
| `data` | Object | 结果数据（可选） |
| `timestamp` | Long | 时间戳（毫秒） |

## 前端使用示例

### JavaScript/TypeScript

```typescript
// 连接 WebSocket
const ws = new WebSocket('ws://localhost:48080/ws/device/status');

ws.onmessage = (event) => {
  const message = JSON.parse(event.data);
  
  switch (message.type) {
    case 'DEVICE_STATUS':
      handleDeviceStatus(message.data);
      break;
    case 'DEVICE_EVENT':
      handleDeviceEvent(message.data);
      break;
    case 'COMMAND_RESULT':
      handleCommandResult(message.data);
      break;
  }
};

function handleDeviceStatus(data) {
  const { deviceId, deviceType, status } = data;
  console.log(`设备 ${deviceId} (${deviceType}) 状态变更为: ${status}`);
  
  // 根据设备类型处理
  switch (deviceType) {
    case 'ACCESS_GEN2':
      updateAccessDeviceStatus(deviceId, status);
      break;
    case 'ALARM':
      updateAlarmDeviceStatus(deviceId, status);
      break;
    // ...
  }
}

function handleDeviceEvent(data) {
  const { deviceId, deviceType, eventType, eventData } = data;
  console.log(`设备 ${deviceId} 产生事件: ${eventType}`);
  
  // 根据设备类型和事件类型处理
  if (deviceType.startsWith('ACCESS')) {
    handleAccessEvent(deviceId, eventType, eventData);
  } else if (deviceType === 'ALARM') {
    handleAlarmEvent(deviceId, eventType, eventData);
  }
}

function handleCommandResult(data) {
  const { requestId, success, message } = data;
  
  if (success) {
    console.log(`命令 ${requestId} 执行成功: ${message}`);
  } else {
    console.error(`命令 ${requestId} 执行失败: ${message}`);
  }
}
```

### Vue 3 组合式 API

```typescript
import { ref, onMounted, onUnmounted } from 'vue';

export function useDeviceWebSocket() {
  const deviceStatus = ref<Map<number, string>>(new Map());
  const events = ref<any[]>([]);
  let ws: WebSocket | null = null;

  const connect = () => {
    ws = new WebSocket('ws://localhost:48080/ws/device/status');
    
    ws.onmessage = (event) => {
      const message = JSON.parse(event.data);
      
      if (message.type === 'DEVICE_STATUS') {
        deviceStatus.value.set(message.data.deviceId, message.data.status);
      } else if (message.type === 'DEVICE_EVENT') {
        events.value.unshift(message.data);
        // 保留最近100条事件
        if (events.value.length > 100) {
          events.value.pop();
        }
      }
    };
  };

  onMounted(() => connect());
  onUnmounted(() => ws?.close());

  return { deviceStatus, events };
}
```

## 相关代码

- `DeviceMessagePushService` - 统一消息推送服务
- `DeviceStatusWebSocketHandler` - 统一 WebSocket Handler
- `UnifiedDeviceStatusMessage` - 设备状态消息 DTO
- `UnifiedDeviceEventMessage` - 设备事件消息 DTO
- `UnifiedCommandResultMessage` - 命令结果消息 DTO

## 版本历史

| 版本 | 日期 | 说明 |
|-----|------|------|
| 1.0 | 2024-12-27 | 初始版本，统一消息格式 |
