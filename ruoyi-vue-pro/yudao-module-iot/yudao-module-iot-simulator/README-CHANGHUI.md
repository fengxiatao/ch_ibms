# 长辉设备模拟器

用于模拟长辉协议设备的行为，支持客户演示和测试环境使用。

## 功能特性

### 1. 心跳保活
- 自动发送心跳包保持设备在线状态
- 可配置心跳间隔（默认30秒）
- 支持断线自动重连

### 2. 固件升级流程
支持完整的升级流程模拟：
1. **升级触发** - 接收服务端下发的升级触发命令
2. **升级开始** - 响应升级开始状态
3. **URL接收** - 接收固件下载URL
4. **进度上报** - 实时上报下载/升级进度 (0-100%)
5. **升级完成/失败** - 上报最终升级结果

### 3. 多种测试模式

| 模式 | 说明 | 使用场景 |
|------|------|----------|
| SUCCESS | 正常完成所有升级流程 | 演示正常升级流程 |
| REJECT | 拒绝升级命令 | 测试设备拒绝升级场景 |
| FRAME_FAIL | 在指定进度时失败 | 测试升级中断恢复 |
| TIMEOUT | 延迟响应 | 测试超时处理逻辑 |

## 快速开始

### 1. 编译

```bash
cd ruoyi-vue-pro
mvn clean package -pl yudao-module-iot/yudao-module-iot-simulator -am -DskipTests
```

### 2. 使用启动脚本

```bash
cd yudao-module-iot/yudao-module-iot-simulator

# 默认配置启动
start-changhui-demo.bat

# 指定测站编码
start-changhui-demo.bat 01020304050607080910

# 指定服务器和模式
start-changhui-demo.bat --host 192.168.1.100 --port 9700 --mode SUCCESS
```

### 3. 使用 Java 命令

```bash
java -jar target/yudao-module-iot-simulator.jar \
    --spring.profiles.active=changhui \
    --simulator.changhui.server-host=localhost \
    --simulator.changhui.server-port=9700
```

## 配置说明

### application-changhui.yml

```yaml
simulator:
  changhui:
    enabled: true
    server-host: localhost           # Gateway服务器地址
    server-port: 9700                # Gateway服务器端口
    heartbeat-interval: 30           # 心跳间隔（秒）
    progress-report-interval: 1000   # 进度上报间隔（毫秒）
    download-delay: 5000             # 模拟下载延迟（毫秒）
    max-retries: 3                   # 重连次数
    retry-interval: 5                # 重连间隔（秒）
    
    devices:
      - station-code: "01020304050607080910"
        device-type: INTEGRATED_GATE
        mode: SUCCESS
```

### 命令行参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `--protocol` | 协议类型 | changhui |
| `--station-code` | 测站编码（20字符十六进制） | 01020304050607080910 |
| `--host` | Gateway服务器地址 | localhost |
| `--port` | Gateway服务器端口 | 9700 |
| `--mode` | 运行模式 | SUCCESS |

## 协议格式

长辉协议采用自定义TCP二进制格式：

```
+--------+--------+------------+-------+---+-------+---+-----+------+------+------+----+------+
| 帧头   | 长度   | 测站编码   | 起始  | L | 起始  | C | AFN | 数据 | 密码 | 时间 | CS | 结束 |
| 3字节  | 4字节  | 10字节     | 1字节 | 1 | 1字节 | 1 | 1   | 可变 | 2字节| 5字节| 1  | 1字节|
| EF7EEF |        |            | 68    |   | 68    |   |     |      |      |      |    | 16   |
+--------+--------+------------+-------+---+-------+---+-----+------+------+------+----+------+
```

### AFN 消息类型

| AFN | 消息类型 | 方向 | 说明 |
|-----|----------|------|------|
| 0x3C | 心跳 | 双向 | 保活消息 |
| 0x02 | 升级触发 | 下行 | 触发升级流程 |
| 0x10 | 升级URL | 下行 | 下发固件URL |
| 0x15 | 升级开始 | 上行 | 设备开始升级 |
| 0x66 | 升级进度 | 上行 | 上报升级进度 |
| 0x67 | 升级完成 | 上行 | 升级成功 |
| 0x68 | 升级失败 | 上行 | 升级失败 |

## 演示场景

### 场景1: 设备上线

1. 启动模拟器
2. 前端查看设备列表
3. 设备显示"在线"状态

### 场景2: 正常升级

1. 启动模拟器（SUCCESS模式）
2. 前端选择设备，下发升级任务
3. 前端实时显示升级进度 0% → 100%
4. 升级完成，显示新固件版本

### 场景3: 升级拒绝

1. 启动模拟器（REJECT模式）
2. 前端下发升级任务
3. 设备拒绝升级，显示失败原因

### 场景4: 升级失败

1. 启动模拟器（FRAME_FAIL模式）
2. 前端下发升级任务
3. 升级进行到一定进度后失败
4. 显示失败原因和失败时的进度

## 开发说明

### 核心类

- `ChanghuiDeviceSimulator` - 设备模拟器主类
- `ChanghuiProtocolCodec` - 协议编解码器
- `ChanghuiSimulatorConfig` - 配置类
- `ChanghuiMessageType` - 消息类型枚举

### 扩展指南

如需添加新的消息类型或行为：

1. 在 `ChanghuiMessageType` 中添加新的 AFN 定义
2. 在 `ChanghuiProtocolCodec` 中添加编解码方法
3. 在 `ChanghuiDeviceSimulator.ChanghuiMessageHandler` 中添加处理逻辑

## 注意事项

1. 测站编码必须是20字符的十六进制字符串
2. 确保 Gateway 服务已启动并监听正确的端口
3. 生产环境请勿使用模拟器
4. 升级文件URL需确保设备可访问

## 常见问题

### Q: 连接失败怎么办？
A: 检查 Gateway 服务是否启动，端口是否正确，防火墙是否允许连接

### Q: 升级进度不更新？
A: 检查 progress-report-interval 配置，确认网络连接正常

### Q: 设备显示离线？
A: 检查心跳间隔配置，确认连接未被意外断开

