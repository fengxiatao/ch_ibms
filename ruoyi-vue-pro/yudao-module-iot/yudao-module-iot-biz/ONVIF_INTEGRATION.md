# ONVIF 通道同步集成说明

## 📋 功能概述

本模块实现了基于 Apache CXF 的 ONVIF 协议集成，用于自动查询 IPC 设备（包括球机）的通道数和云台能力。

## 🎯 主要功能

### 1. 自动查询设备通道
- ✅ 通过 ONVIF GetProfiles 接口查询设备的所有视频流配置
- ✅ 自动识别通道数量（支持双通道球机）
- ✅ 自动检测云台支持（PTZ）
- ✅ 自动检测音频支持
- ✅ 获取视频分辨率信息

### 2. 智能容错机制
- ✅ ONVIF 查询失败时自动使用默认配置（1个通道）
- ✅ 支持自定义认证信息（从设备配置中读取）
- ✅ 详细的日志记录，便于问题排查

## 📦 依赖说明

### Maven 依赖（已添加到 pom.xml）

```xml
<!-- Apache CXF (ONVIF 支持) -->
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-frontend-jaxws</artifactId>
    <version>3.5.5</version>
</dependency>

<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-ws-security</artifactId>
    <version>3.5.5</version>
</dependency>

<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-transports-http</artifactId>
    <version>3.5.5</version>
</dependency>

<!-- JAXB API (Java 11+ 需要) -->
<dependency>
    <groupId>javax.xml.bind</groupId>
    <artifactId>jaxb-api</artifactId>
    <version>2.3.1</version>
</dependency>

<dependency>
    <groupId>javax.xml.ws</groupId>
    <artifactId>jaxws-api</artifactId>
    <version>2.3.1</version>
</dependency>
```

## 🔧 核心类说明

### 1. OnvifClient
**位置**: `cn.iocoder.yudao.module.iot.service.onvif.OnvifClient`

**功能**: ONVIF 客户端主类，封装了设备连接、认证、查询等功能

**主要方法**:
```java
// 查询设备的所有视频流配置（Profiles）
List<OnvifChannelInfo> getProfiles()
```

**返回信息**:
- `channelNo`: 通道号
- `channelName`: 通道名称
- `ptzSupport`: 是否支持云台
- `audioSupport`: 是否支持音频
- `resolution`: 视频分辨率（如 "1920x1080"）
- `profileToken`: ONVIF Profile Token
- `ptzToken`: PTZ 配置 Token

### 2. OnvifMediaService
**位置**: `cn.iocoder.yudao.module.iot.service.onvif.OnvifMediaService`

**功能**: ONVIF Media Service WSDL 接口定义

**支持的接口**:
- `GetProfiles`: 获取设备的所有视频流配置

### 3. OnvifPasswordCallback
**位置**: `cn.iocoder.yudao.module.iot.service.onvif.OnvifPasswordCallback`

**功能**: WS-Security 密码回调处理器，处理 ONVIF 的 Digest 认证

## 🚀 使用流程

### 1. 设备激活时自动同步通道

当 IPC 设备（包括球机）激活并上线后，系统会自动：

1. 从设备配置中读取认证信息（username、password）
2. 创建 `OnvifClient` 连接设备
3. 调用 `getProfiles()` 查询设备通道
4. 根据查询结果创建通道记录

### 2. 手动触发通道同步

```java
// 调用通道同步接口
Integer syncCount = iotDeviceChannelService.syncDeviceChannels(deviceId);
```

## 📝 日志示例

### 成功场景

```log
[通道同步] 开始通过ONVIF查询设备通道: deviceId=123, ip=192.168.1.206
[ONVIF] 查询设备Profiles: ip=192.168.1.206
[ONVIF] 设备返回 2 个Profiles: ip=192.168.1.206
[ONVIF] 解析通道 1: name=可见光, ptz=true, audio=false, resolution=1920x1080
[ONVIF] 解析通道 2: name=热成像, ptz=false, audio=false, resolution=640x480
[通道同步] ✅ 创建IPC通道: deviceId=123, channelNo=1, name=可见光, isPtz=true, hasAudio=false, resolution=1920x1080
[通道同步] ✅ 创建IPC通道: deviceId=123, channelNo=2, name=热成像, isPtz=false, hasAudio=false, resolution=640x480
[通道同步] IPC设备通道同步完成: deviceId=123, syncCount=2
```

### 失败场景（自动降级）

```log
[通道同步] 开始通过ONVIF查询设备通道: deviceId=123, ip=192.168.1.206
[通道同步] ONVIF查询失败，使用默认配置: deviceId=123, ip=192.168.1.206
[通道同步] 创建默认通道: deviceId=123, channelNo=1
```

## 🔍 问题排查

### 1. ONVIF 查询失败

**可能原因**:
- 设备不支持 ONVIF 协议
- 认证信息错误（username/password）
- 网络连接问题
- 设备 ONVIF 服务未启用

**解决方法**:
1. 检查设备是否支持 ONVIF
2. 确认设备配置中的认证信息正确
3. 使用 ONVIF Device Manager 工具测试设备连接
4. 查看详细的异常日志

### 2. 通道数不正确

**可能原因**:
- 设备返回的 Profiles 数量与实际通道数不符
- 设备配置问题

**解决方法**:
1. 使用 ONVIF Device Manager 查看设备的 Profiles
2. 检查设备固件版本
3. 联系设备厂商确认 ONVIF 实现

### 3. 云台识别错误

**可能原因**:
- 设备的 PTZConfiguration 配置不正确
- 设备虽然支持云台但未配置

**解决方法**:
1. 在设备 Web 界面确认云台功能已启用
2. 检查设备的 PTZ 配置
3. 手动修改通道的 `ptzSupport` 字段

## 🎨 扩展开发

### 添加更多 ONVIF 接口

如需支持更多 ONVIF 功能（如 PTZ 控制、事件订阅等），可以：

1. 在 `OnvifMediaService` 中添加新的接口定义
2. 在 `OnvifClient` 中实现对应的方法
3. 添加相应的 JAXB 数据类

### 示例：添加 PTZ 控制

```java
// 1. 在 OnvifMediaService 中添加接口
@WebMethod(operationName = "AbsoluteMove")
void absoluteMove(
    @WebParam(name = "ProfileToken") String profileToken,
    @WebParam(name = "Position") PTZVector position
);

// 2. 在 OnvifClient 中实现
public void ptzAbsoluteMove(String profileToken, float pan, float tilt, float zoom) {
    OnvifPTZService ptzService = createPTZServiceClient(...);
    PTZVector position = new PTZVector(pan, tilt, zoom);
    ptzService.absoluteMove(profileToken, position);
}
```

## 📚 参考资料

- [ONVIF 官方网站](https://www.onvif.org/)
- [ONVIF Core Specification](https://www.onvif.org/specs/core/ONVIF-Core-Specification.pdf)
- [Apache CXF 文档](https://cxf.apache.org/docs/)
- [ONVIF Device Manager](https://sourceforge.net/projects/onvifdm/)

## ✅ 测试建议

### 1. 单元测试

```java
@Test
public void testOnvifClientGetProfiles() {
    OnvifClient client = new OnvifClient("192.168.1.206", "admin", "admin123");
    List<OnvifChannelInfo> profiles = client.getProfiles();
    
    assertNotNull(profiles);
    assertTrue(profiles.size() > 0);
    
    // 验证第一个通道
    OnvifChannelInfo channel1 = profiles.get(0);
    assertEquals(1, channel1.getChannelNo());
    assertNotNull(channel1.getChannelName());
}
```

### 2. 集成测试

1. 准备一台支持 ONVIF 的设备（IPC 或球机）
2. 在设备管理页面激活设备
3. 等待设备上线
4. 检查通道管理页面，确认通道已自动创建
5. 验证通道信息（通道数、云台支持、音频支持等）

## 🐛 已知问题

1. **部分厂商的 ONVIF 实现不标准**
   - 解决方案：添加厂商特定的兼容性处理

2. **网络延迟可能导致查询超时**
   - 解决方案：已设置 5 秒超时，可根据实际情况调整

3. **某些设备的 Profile 名称为空**
   - 解决方案：已添加默认名称生成逻辑

## 📞 技术支持

如有问题，请联系：
- 开发团队：长辉信息科技有限公司
- 邮箱：support@example.com
