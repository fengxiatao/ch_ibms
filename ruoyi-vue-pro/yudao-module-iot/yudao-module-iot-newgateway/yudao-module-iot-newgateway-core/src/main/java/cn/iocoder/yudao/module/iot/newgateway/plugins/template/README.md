# 设备插件模板

本目录提供了一个完整的设备插件参考实现，开发新的设备插件时可以复制此模板并修改。

## 目录结构

```
template/
├── TemplatePlugin.java           # 插件入口类（实现 PassiveDeviceHandler）
├── TemplateConnectionManager.java # 连接管理器
├── TemplateConfig.java           # 插件配置类
├── package-info.java             # 包文档
└── README.md                     # 本文档
```

## 快速开始

### 1. 复制模板

将 `template` 目录复制到 `plugins/{your_device_type}/`：

```bash
cp -r template plugins/mydevice
```

### 2. 重命名文件和类

将所有文件和类名中的 `Template` 替换为你的设备类型名称：

| 原文件名 | 新文件名（示例） |
|---------|-----------------|
| TemplatePlugin.java | MyDevicePlugin.java |
| TemplateConnectionManager.java | MyDeviceConnectionManager.java |
| TemplateConfig.java | MyDeviceConfig.java |

### 3. 修改 @DevicePlugin 注解

```java
@DevicePlugin(
    id = "mydevice",                    // 插件唯一标识
    name = "我的设备插件",               // 显示名称
    deviceType = "MYDEVICE",            // 设备类型（大写）
    vendor = "MyVendor",                // 厂商名称
    description = "我的设备插件描述",    // 插件描述
    enabledByDefault = true             // 是否默认启用
)
```

### 4. 修改配置前缀

在 `MyDeviceConfig.java` 中修改配置前缀：

```java
@ConfigurationProperties(prefix = "iot.newgateway.plugins.mydevice")
public class MyDeviceConfig {
    // ...
}
```

### 5. 实现核心方法

#### 5.1 解析设备标识符

```java
@Override
public String parseDeviceIdentifier(ByteBuf data) {
    // 根据你的设备协议解析设备标识符
    // 例如：账号、序列号、测站编码等
}
```

#### 5.2 处理设备命令

```java
@Override
public CommandResult executeCommand(Long deviceId, DeviceCommand command) {
    switch (command.getCommandType()) {
        case "YOUR_COMMAND":
            return executeYourCommand(deviceId, command);
        default:
            return CommandResult.failure("不支持的命令类型");
    }
}
```

### 6. 配置插件

在 `application.yaml` 中添加配置：

```yaml
iot:
  newgateway:
    plugins:
      mydevice:
        enabled: true
        port: 9900
        heartbeat-timeout: 60000
        connection-timeout: 30000
```

## 插件类型

### 被动连接插件（PassiveDeviceHandler）

适用于设备主动连接平台的场景，如：
- 报警主机（PS600 OPC 协议）
- 长辉 TCP 模拟设备
- 其他通过 TCP 心跳连接的设备

**工作流程：**
1. 设备连接到平台指定端口
2. 平台调用 `parseDeviceIdentifier()` 解析设备标识
3. 平台调用 `onConnect()` 处理连接事件
4. 设备定期发送心跳，平台调用 `onHeartbeat()` 处理
5. 设备断开时，平台调用 `onDisconnect()` 处理

### 主动连接插件（ActiveDeviceHandler）

适用于平台主动连接设备的场景，如：
- 大华 NVR/IPC（SDK 登录）
- 大华门禁设备（SDK 登录）

**工作流程：**
1. 平台调用 `login()` 登录设备
2. 平台调用 `keepalive()` 保持连接
3. 平台调用 `executeCommand()` 执行命令
4. 平台调用 `logout()` 登出设备

## 核心接口

### DeviceHandler（基础接口）

```java
public interface DeviceHandler {
    String getDeviceType();                              // 获取设备类型
    String getVendor();                                  // 获取厂商
    boolean supports(DeviceInfo deviceInfo);             // 判断是否支持该设备
    CommandResult executeCommand(Long deviceId, DeviceCommand command);  // 执行命令
    DeviceStatus queryStatus(Long deviceId);             // 查询状态
}
```

### PassiveDeviceHandler（被动连接接口）

```java
public interface PassiveDeviceHandler extends DeviceHandler {
    int getListenPort();                                 // 获取监听端口
    void onConnect(ChannelHandlerContext ctx, String deviceIdentifier);  // 连接事件
    void onHeartbeat(Long deviceId, HeartbeatData data); // 心跳事件
    void onDisconnect(Long deviceId);                    // 断开事件
    String parseDeviceIdentifier(ByteBuf data);          // 解析设备标识
}
```

### ConnectionManager（连接管理器接口）

```java
public interface ConnectionManager<T> {
    void register(Long deviceId, String identifier, T connection);  // 注册连接
    void unregister(Long deviceId);                      // 注销连接
    T getConnection(Long deviceId);                      // 获取连接
    Long getDeviceIdByIdentifier(String identifier);     // 根据标识获取设备ID
    boolean isOnline(Long deviceId);                     // 检查是否在线
    void updateHeartbeat(Long deviceId);                 // 更新心跳时间
}
```

## 最佳实践

### 1. 日志规范

使用统一的日志前缀格式：

```java
private static final String LOG_PREFIX = "[MyDevicePlugin]";

log.info("{} 设备连接: deviceId={}", LOG_PREFIX, deviceId);
```

### 2. 异常处理

所有方法应捕获异常，避免影响其他插件：

```java
try {
    // 业务逻辑
} catch (Exception e) {
    log.error("{} 处理失败: deviceId={}", LOG_PREFIX, deviceId, e);
    return CommandResult.failure("处理异常: " + e.getMessage());
}
```

### 3. 线程安全

连接管理器使用 `ConcurrentHashMap`，确保线程安全：

```java
private final Map<Long, Channel> deviceConnections = new ConcurrentHashMap<>();
```

### 4. 资源清理

在 `onStop()` 方法中清理资源：

```java
public void onStop() {
    connectionManager.closeAll();
}
```

### 5. 配置验证

在配置类中添加验证逻辑：

```java
@PostConstruct
public void validate() {
    if (port <= 0 || port > 65535) {
        throw new IllegalArgumentException("端口号无效: " + port);
    }
}
```

## 常见问题

### Q: 如何添加协议编解码器？

A: 创建一个独立的编解码器类：

```java
@Component
public class MyDeviceProtocolCodec {
    public String parseDeviceIdentifier(ByteBuf data) { ... }
    public byte[] buildCommandFrame(DeviceCommand command) { ... }
}
```

### Q: 如何发布设备事件？

A: 使用 `GatewayMessagePublisher`：

```java
messagePublisher.publishEvent("iot_mydevice_event", eventData);
```

### Q: 如何处理设备重连？

A: 在 `onConnect()` 中检查并关闭旧连接：

```java
@Override
public void onConnect(ChannelHandlerContext ctx, String identifier) {
    Long deviceId = lookupDeviceId(identifier);
    // ConnectionManager.register() 会自动关闭旧连接
    connectionManager.register(deviceId, identifier, ctx.channel());
}
```

## 参考资料

- [设计文档](../../../../../../../.kiro/specs/gateway-plugin-architecture/design.md)
- [需求文档](../../../../../../../.kiro/specs/gateway-plugin-architecture/requirements.md)
- [核心接口定义](../../core/handler/)
- [连接管理器接口](../../core/connection/)
