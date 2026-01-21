# SDK 通道查询实现

## 实现原则

直接返回通过 SDK 查询的真实通道数据，如果 NVR 没有添加通道则返回空集合。

## 核心逻辑

### 1. 数据来源
- ✅ 直接从大华 SDK 获取通道信息
- ✅ 不创建虚拟设备对象
- ✅ 不存储到数据库

### 2. 返回规则
```java
// 如果 NVR 没有配置通道 → 返回空集合
if (data.isEmpty()) {
    return new ArrayList<>();
}

// 如果有通道 → 返回真实通道数据
for (Map<String, Object> m : data) {
    IotDeviceDO channelDevice = new IotDeviceDO();
    channelDevice.setId(Long.valueOf(channelNo)); // 使用通道编号作为ID
    channelDevice.setDeviceName(name); // SDK返回的真实通道名称
    channelDevice.setState(online ? 1 : 2); // 实时在线状态
}
```

### 3. 数据格式
```java
// 通道配置信息
String cfg = JSONUtil.createObj()
    .set("channel", channelNo)      // 通道编号
    .set("channelName", name)       // 通道名称
    .set("online", online)          // 在线状态
    .set("nvrId", nvrId)           // 所属NVR ID
    .toString();
```

## API 响应示例

### 有通道的情况
```json
{
  "code": 0,
  "data": [
    {
      "id": 0,
      "name": "通道 1",
      "state": 1,
      "ip": "192.168.1.200",
      "channelNo": 0,
      "config": "{\"channel\":0,\"channelName\":\"通道 1\",\"online\":true,\"nvrId\":70}"
    },
    {
      "id": 1,
      "name": "通道 2", 
      "state": 1,
      "ip": "192.168.1.200",
      "channelNo": 1,
      "config": "{\"channel\":1,\"channelName\":\"通道 2\",\"online\":true,\"nvrId\":70}"
    }
  ]
}
```

### 无通道的情况
```json
{
  "code": 0,
  "data": []
}
```

## 日志输出

### 成功获取通道
```
[NVR] 获取通道列表，直接从设备获取实时数据 nvrId=70
[NVR] 网关返回通道数据: [6个通道数据]
[NVR] SDK通道数据: channelNo=0, name=通道 1, online=true
[NVR] SDK通道数据: channelNo=1, name=通道 2, online=true
[NVR] ✅ 成功获取 6 个真实NVR通道
```

### NVR无通道
```
[NVR] 获取通道列表，直接从设备获取实时数据 nvrId=70
[NVR] 网关返回通道数据: []
[NVR] ✅ 成功获取 0 个真实NVR通道
```

### 网络异常
```
[NVR] 刷新通道异常 nvrId=70
[NVR] ✅ 成功获取 0 个真实NVR通道 (返回空集合)
```

## 优势

1. **真实性**：返回的都是 NVR 设备实际配置的通道
2. **实时性**：每次查询都获取最新的通道状态
3. **简洁性**：没有通道就是空集合，有通道就返回真实数据
4. **一致性**：通道信息与 NVR 设备完全同步

## 测试验证

### 1. 有通道的 NVR
```bash
curl -X GET "http://192.168.1.38:8099/iot/video/nvr/70/channels?refresh=1"
# 预期：返回6个真实通道
```

### 2. 无通道的 NVR  
```bash
curl -X GET "http://192.168.1.38:8099/iot/video/nvr/71/channels?refresh=1"
# 预期：返回空数组 []
```

### 3. 网络异常
```bash
# 断开网络连接后测试
curl -X GET "http://192.168.1.38:8099/iot/video/nvr/70/channels?refresh=1"
# 预期：返回空数组 []
```
