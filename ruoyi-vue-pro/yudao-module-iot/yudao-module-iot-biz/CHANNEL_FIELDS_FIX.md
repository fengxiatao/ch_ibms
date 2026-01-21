# NVR 通道字段完善修复

## 问题描述

前端表格显示的通道信息缺少重要字段：
- ❌ 端口信息显示为空
- ❌ 设备类型信息缺失
- ❌ 协议类型信息缺失
- ❌ 厂商信息缺失
- ❌ 型号信息缺失

## 修复内容

### 1. 扩展 NvrChannelRespVO

```java
@Data
public class NvrChannelRespVO {
    private Long id;
    private String name;
    private Integer state;
    private String ip;
    private Integer channelNo;
    
    // 新增扩展字段
    private Integer port;           // 端口
    private String deviceType;      // 设备类型
    private String protocol;        // 协议类型
    private String manufacturer;    // 厂商
    private String model;           // 型号
}
```

### 2. 完善数据映射

```java
// 从配置中提取扩展字段
String config = d.getConfig();
vo.setPort(extractIntFromConfig(config, "port"));
vo.setDeviceType(extractStringFromConfig(config, "deviceType"));
vo.setProtocol(extractStringFromConfig(config, "protocol"));
vo.setManufacturer(extractStringFromConfig(config, "manufacturer"));
vo.setModel(extractStringFromConfig(config, "model"));
```

### 3. 数据源完善

在 `NvrQueryServiceImpl` 中设置完整的配置信息：

```java
String cfg = JSONUtil.createObj()
    .set("channel", channelNo)
    .set("channelName", name)
    .set("online", online)
    .set("nvrId", nvrId)
    .set("nvrIp", ip)
    .set("deviceType", "IPC")      // 设备类型：网络摄像机
    .set("protocol", "ONVIF")      // 协议类型
    .set("manufacturer", "Dahua")  // 厂商
    .set("port", 37777)           // 端口
    .toString();
```

## 预期API响应

修复后的API响应应该包含完整字段：

```json
{
  "code": 0,
  "data": [
    {
      "id": 70000,
      "name": "通道 1",
      "state": 1,
      "ip": "192.168.1.200",
      "channelNo": 0,
      "port": 37777,              // ✅ 端口信息
      "deviceType": "IPC",        // ✅ 设备类型
      "protocol": "ONVIF",        // ✅ 协议类型
      "manufacturer": "Dahua",    // ✅ 厂商信息
      "model": null               // ✅ 型号信息（可为空）
    }
  ]
}
```

## 前端表格显示效果

修复后，前端表格应该显示：

| 设备ID | 名称 | 状态 | IP地址 | 端口 | 设备类型 | 协议类型 | 厂商 | 型号 |
|--------|------|------|--------|------|----------|----------|------|------|
| 70000 | 通道 1 | 在线 | 192.168.1.200 | 37777 | IPC | ONVIF | Dahua | - |
| 70001 | 通道 2 | 在线 | 192.168.1.200 | 37777 | IPC | ONVIF | Dahua | - |

## 测试验证

### 1. API测试
```bash
curl -X GET "http://192.168.1.38:8099/iot/video/nvr/70/channels?refresh=1"
```

### 2. 检查响应字段
确认响应包含所有新增字段：
- `port`: 37777
- `deviceType`: "IPC"
- `protocol`: "ONVIF"
- `manufacturer`: "Dahua"
- `model`: null

### 3. 前端验证
在浏览器中检查表格是否正确显示所有字段信息。

## 技术细节

### 配置解析方法

新增了两个辅助方法来解析JSON配置：

1. `extractIntFromConfig(String config, String key)` - 提取整数值
2. `extractStringFromConfig(String config, String key)` - 提取字符串值

这些方法支持简单的JSON解析，无需引入额外依赖。

### 字段映射逻辑

- **端口**: 从配置中的 `port` 字段获取，默认 37777
- **设备类型**: 固定为 "IPC"（网络摄像机）
- **协议**: 固定为 "ONVIF"（标准协议）
- **厂商**: 固定为 "Dahua"（大华）
- **型号**: 暂时为空，可后续从SDK获取具体型号

## 扩展性

如果需要获取更准确的设备信息，可以：

1. **从SDK获取**: 在 `DahuaNvrService` 中获取设备详细信息
2. **配置化管理**: 将设备类型、协议等信息配置化
3. **动态识别**: 根据通道特征自动识别设备类型和型号
