# 设备发现 API 测试指南

## 📋 可用接口列表

### 1. 获取最近发现的设备

**接口地址**：`GET /admin-api/iot/device/discovery/recent`

**请求参数**：
```
hours: 时间范围（小时），默认 24
```

**请求示例**：
```bash
# 获取最近 24 小时发现的设备
curl -X GET "http://localhost:48080/admin-api/iot/device/discovery/recent?hours=24" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 获取最近 48 小时发现的设备
curl -X GET "http://localhost:48080/admin-api/iot/device/discovery/recent?hours=48" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**响应示例**：
```json
{
  "code": 0,
  "data": [
    {
      "ip": "192.168.1.202",
      "mac": "00:11:22:33:44:55",
      "vendor": "Hikvision",
      "model": "DS-2CD2385G2-I",
      "serialNumber": "DS-2CD2385G2-I20230101AAWRJ1234567",
      "deviceType": "camera",
      "firmwareVersion": "V5.7.10",
      "discoveryMethod": "ONVIF",
      "discoveryTime": "2025-10-27T11:50:00"
    },
    {
      "ip": "192.168.1.200",
      "mac": null,
      "vendor": "onvif",
      "model": null,
      "serialNumber": null,
      "deviceType": "camera",
      "firmwareVersion": null,
      "discoveryMethod": "ONVIF",
      "discoveryTime": "2025-10-27T11:50:00"
    }
  ],
  "msg": "操作成功"
}
```

### 2. 获取未添加的发现设备

**接口地址**：`GET /admin-api/iot/device/discovery/unadded`

**请求示例**：
```bash
curl -X GET "http://localhost:48080/admin-api/iot/device/discovery/unadded" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**响应示例**：
```json
{
  "code": 0,
  "data": [
    {
      "ip": "192.168.1.202",
      "vendor": "Hikvision",
      "deviceType": "camera",
      "discoveryMethod": "ONVIF",
      "discoveryTime": "2025-10-27T11:50:00"
    }
  ],
  "msg": "操作成功"
}
```

### 3. 忽略发现的设备

**接口地址**：`POST /admin-api/iot/device/discovery/ignore/{id}`

**请求参数**：
```
id: 设备记录ID
ignoreDays: 忽略天数（可选，不传或传null表示永久忽略）
reason: 忽略原因（可选）
```

**请求示例**：
```bash
# 永久忽略
curl -X POST "http://localhost:48080/admin-api/iot/device/discovery/ignore/1" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "该设备不需要接入系统"
  }'

# 忽略7天
curl -X POST "http://localhost:48080/admin-api/iot/device/discovery/ignore/1?ignoreDays=7&reason=临时不接入" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4. 取消忽略设备

**接口地址**：`POST /admin-api/iot/device/discovery/unignore/{id}`

**请求示例**：
```bash
curl -X POST "http://localhost:48080/admin-api/iot/device/discovery/unignore/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 5. 启动设备扫描

**接口地址**：`POST /admin-api/iot/device/discovery/scan`

**请求体**：
```json
{
  "scanType": "onvif",
  "timeout": 5000
}
```

**请求示例**：
```bash
curl -X POST "http://localhost:48080/admin-api/iot/device/discovery/scan" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "scanType": "onvif",
    "timeout": 5000
  }'
```

## 🔧 测试步骤

### 步骤1：确认数据库表已创建

```sql
-- 检查表是否存在
SHOW TABLES LIKE 'iot_discovered_device';
SHOW TABLES LIKE 'iot_message_idempotent';

-- 查看发现的设备
SELECT * FROM iot_discovered_device ORDER BY create_time DESC;

-- 查看消息幂等性记录
SELECT * FROM iot_message_idempotent ORDER BY create_time DESC;
```

### 步骤2：启动服务并观察日志

```bash
cd F:\work\ch_ibms\ruoyi-vue-pro\yudao-server
mvn spring-boot:run
```

**关键日志**：
```
[DeviceDiscoveryManager] [discoverDevices][开始设备发现，扫描方式: onvif, ssdp, arp]
[OnvifScanner] [scan][ONVIF发现设备: 192.168.1.202 (onvif)]
[DeviceDiscoveryManager] [publishDiscoveryEvent][发布设备发现消息: 192.168.1.202 (onvif)]
[DeviceDiscoveredConsumer] [onMessage][收到设备发现消息: 192.168.1.202 (onvif)]
[MessageIdempotentService] [tryProcess][消息可以处理: 192.168.1.202_2025-10-27...]
[DiscoveredDeviceService] [saveDiscoveredDevice][保存发现记录: 192.168.1.202 (onvif)]
[AlertWebSocketHandler] [broadcastMessage][推送 WebSocket 消息到所有用户]
```

### 步骤3：获取 Token

```bash
# 登录获取 Token
curl -X POST "http://localhost:48080/admin-api/system/auth/login" \
  -H "Content-Type: application/json" \
  -H "tenant-id: 1" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "userId": 1,
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "...",
    "expiresTime": 1234567890
  }
}
```

### 步骤4：调用获取发现设备接口

```bash
# 使用 Postman 或 curl
curl -X GET "http://localhost:48080/admin-api/iot/device/discovery/recent?hours=24" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "tenant-id: 1"
```

### 步骤5：在浏览器中测试

1. 打开浏览器开发者工具 (F12)
2. 切换到 "Network" 标签
3. 访问：`http://localhost:48080/admin-api/iot/device/discovery/recent?hours=24`
4. 查看响应

## 🌐 前端集成建议

### Vue 3 示例代码

```typescript
// api/iot/discovery.ts
import request from '@/utils/request'

/**
 * 获取最近发现的设备
 */
export function getRecentDiscoveredDevices(hours: number = 24) {
  return request({
    url: '/iot/device/discovery/recent',
    method: 'get',
    params: { hours }
  })
}

/**
 * 获取未添加的设备
 */
export function getUnaddedDevices() {
  return request({
    url: '/iot/device/discovery/unadded',
    method: 'get'
  })
}

/**
 * 忽略设备
 */
export function ignoreDevice(id: number, ignoreDays?: number, reason?: string) {
  return request({
    url: `/iot/device/discovery/ignore/${id}`,
    method: 'post',
    params: { ignoreDays, reason }
  })
}
```

### WebSocket 消息监听

```typescript
// utils/websocket.ts
import { ElNotification } from 'element-plus'

// 连接 WebSocket
const ws = new WebSocket('ws://localhost:48080/ws/alert')

ws.onmessage = (event) => {
  const message = JSON.parse(event.data)
  
  if (message.type === 'device_discovered') {
    // 显示通知
    ElNotification({
      title: '发现新设备',
      message: `发现新设备：${message.data.device.ip} (${message.data.device.vendor})`,
      type: 'info',
      duration: 0,  // 不自动关闭
      onClick: () => {
        // 跳转到设备发现页面
        router.push('/iot/device/discovery')
      }
    })
    
    // 刷新设备列表
    refreshDiscoveredDevices()
  }
}
```

## 📊 数据验证

### 检查发现的设备数据

```sql
-- 查看最近发现的设备
SELECT 
    id,
    ip,
    vendor,
    device_type AS '设备类型',
    discovery_method AS '发现方式',
    discovery_time AS '发现时间',
    added AS '是否已添加',
    status AS '状态'
FROM iot_discovered_device
WHERE discovery_time > DATE_SUB(NOW(), INTERVAL 24 HOUR)
ORDER BY discovery_time DESC;

-- 查看消息处理状态
SELECT 
    message_id AS '消息ID',
    topic AS '主题',
    status AS '状态',
    processed_time AS '处理时间',
    create_time AS '创建时间'
FROM iot_message_idempotent
ORDER BY create_time DESC
LIMIT 10;
```

## ⚠️ 常见问题

### 1. 接口返回空数组 `[]`

**原因**：
- 数据库表为空
- 网关未发现设备
- 时间范围太小

**解决方案**：
```sql
-- 检查是否有数据
SELECT COUNT(*) FROM iot_discovered_device;

-- 查看所有记录
SELECT * FROM iot_discovered_device;
```

### 2. 401 Unauthorized

**原因**：Token 无效或过期

**解决方案**：
- 重新登录获取新 Token
- 确认 Header 中包含 `Authorization: Bearer YOUR_TOKEN`
- 确认 Header 中包含 `tenant-id: 1`

### 3. 403 Forbidden

**原因**：没有权限

**解决方案**：
- 检查用户是否有 `iot:device:discover` 权限
- 联系管理员分配权限

### 4. 设备发现后没有保存到数据库

**原因**：
- 消息幂等性检查失败
- 租户上下文缺失
- Service 方法异常

**解决方案**：
```bash
# 查看日志
tail -f logs/spring.log | grep -E "DeviceDiscovered|MessageIdempotent|DiscoveredDevice"

# 检查是否有错误
grep ERROR logs/spring.log | grep -E "DeviceDiscovered|MessageIdempotent"
```

## 🔗 相关文档

- [设备发现业务流程分析](./sessions/session_20251026_设备发现业务流程分析与改进.md)
- [错误记录：多租户上下文缺失](./errors/后端_多租户上下文缺失_RocketMQ消费者租户拦截错误.md)
- [错误记录：数据库表不存在](./errors/后端_数据库表不存在_设备发现和消息幂等性表.md)

---

**创建时间**：2025-10-27  
**作者**：长辉信息科技有限公司  
**版本**：v1.0














