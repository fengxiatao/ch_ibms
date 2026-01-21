# NVR 通道管理设计说明

## 设计理念

NVR 的通道不作为独立的设备存储在项目数据库中，而是作为 NVR 设备的属性来管理。

## 设计优势

### 1. 数据一致性
- ✅ 通道信息始终与 NVR 设备保持同步
- ✅ 避免数据库中的冗余和不一致数据
- ✅ 实时获取通道状态，无需担心缓存问题

### 2. 简化管理
- ✅ 不需要维护通道设备的生命周期
- ✅ 不需要处理通道设备的创建、更新、删除
- ✅ 减少数据库存储压力

### 3. 灵活性
- ✅ 支持 NVR 设备动态增减通道
- ✅ 通道配置变更时无需同步数据库
- ✅ 支持不同品牌 NVR 的通道差异

## 实现方案

### 1. 虚拟设备对象
```java
// 创建虚拟的设备对象，不存储到数据库
IotDeviceDO virtualDevice = new IotDeviceDO();
virtualDevice.setId(Long.valueOf(nvrId * 1000 + channelNo)); // 虚拟ID
virtualDevice.setDeviceName(name); // 使用真实通道名称
virtualDevice.setState(online ? 1 : 2); // 实时状态
```

### 2. 实时数据获取
```java
@Override
public List<IotDeviceDO> getChannelsByNvrId(Long nvrId) {
    // 直接从 NVR 设备获取实时通道数据
    return refreshChannelsByNvrId(nvrId);
}
```

### 3. 通道配置存储
```java
// 通道配置存储在虚拟对象的 config 字段中
String cfg = JSONUtil.createObj()
    .set("channel", channelNo)
    .set("channelName", name)
    .set("online", online)
    .toString();
```

## API 行为变化

### 前端调用
```javascript
// 前端调用保持不变
const channels = await getNvrChannels(nvrId, 1)
```

### 后端响应
```json
{
  "code": 0,
  "data": [
    {
      "id": 70000,        // 虚拟ID (nvrId * 1000 + channelNo)
      "name": "通道 1",    // 真实通道名称
      "state": 1,         // 实时在线状态
      "channelNo": 0      // 通道编号
    }
  ]
}
```

## 数据流程

```
前端请求 → NvrController → NvrQueryService → 网关API → 大华SDK → NVR设备
                                                                        ↓
前端显示 ← 虚拟设备对象 ← 数据转换 ← 通道数据 ← SDK响应 ← 设备响应
```

## 优化效果

### 1. 性能提升
- ❌ 旧方案：数据库查询 + 网络请求 + 数据库写入
- ✅ 新方案：网络请求 + 内存转换

### 2. 数据准确性
- ❌ 旧方案：可能存在数据库与设备不同步
- ✅ 新方案：始终显示设备实时状态

### 3. 维护成本
- ❌ 旧方案：需要维护通道设备的完整生命周期
- ✅ 新方案：只需要数据转换逻辑

## 注意事项

1. **虚拟ID规则**：`nvrId * 1000 + channelNo`，确保唯一性
2. **缓存策略**：如需缓存，应在应用层实现，而非数据库层
3. **错误处理**：网络异常时返回空列表，前端需要适当处理
4. **扩展性**：支持未来添加更多 NVR 厂商和协议

## 兼容性

- ✅ 前端 API 调用方式不变
- ✅ 响应数据格式保持兼容
- ✅ 现有业务逻辑无需修改
