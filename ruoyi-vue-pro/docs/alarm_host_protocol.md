# 报警主机通信协议 (PS600 OPC v1.2)

## 1. 连接方式

- **TCP** 或 **UDP**
- Gateway默认监听端口：**9988**
- 报警主机主动连接到Gateway

## 2. 事件上报（主机→中心）

### 2.1 数据格式

```
E{account},{event_code}{area}{point}{sequence}\n
```

### 2.2 字段说明

| 字段 | 长度 | 说明 |
|------|------|------|
| E | 1 | 事件标识（固定） |
| account | 可变 | 主机编号（账号） |
| , | 1 | 分隔符 |
| event_code | 4位 | CID事件码<br>- 千位为1：新事件<br>- 千位为3：事件恢复<br>- 0000：心跳 |
| area | 2位 | 分区号（无分区系统为00） |
| point | 3位 | 防区号/用户号/模块号 |
| sequence | 4位 | 序列号（0000-9999循环） |
| \n | 1 | 结束符 |

### 2.3 示例

#### 示例1：心跳
```
主机→中心：E1234,0000000000125
中心→主机：e1234,125
```

#### 示例2：179号防区报警
```
主机→中心：E1234,1130001790123
中心→主机：e1234,123
```

#### 示例3：1号用户布防
```
主机→中心：E1234,3401000010124
中心→主机：e1234,124
```

## 3. 应答确认（中心→主机）

### 3.1 数据格式

```
e{account},{sequence}\n
```

### 3.2 字段说明

| 字段 | 说明 |
|------|------|
| e | 应答标识（小写） |
| account | 主机编号（与上报一致） |
| sequence | 序列号（与上报一致） |

### 3.3 重要说明

⚠️ **必须应答！** 报警主机发送事件后，必须收到中心的应答确认，否则会认为连接失败。

## 4. 反向控制（中心→主机）

### 4.1 控制指令格式

```
C{account},{control},{param},{password},{sequence}\n
```

### 4.2 控制指令码

| 指令码 | 功能 | 参数说明 |
|--------|------|----------|
| 0 | 查询设备状态 | 无 |
| 1 | 撤防 | 分区号 |
| 2 | 外出布防 | 分区号 |
| 3 | 居家布防 | 分区号 |
| 4 | 防区旁路 | 防区号 |
| 5 | 撤销防区旁路 | 防区号 |
| 6 | 打开输出 | 输出号*输出时间（可选） |
| 7 | 关闭输出 | 输出号 |
| 8 | 单防区布防 | 防区号 |
| 9 | 单防区撤防 | 防区号 |
| 10 | 查询分区和防区状态 | 0 |
| 11 | 报警复位 | 分区号 |

### 4.3 控制响应格式

```
c{account},{result},{sequence}\n
```

| 字段 | 说明 |
|------|------|
| c | 响应标识（小写） |
| result | 执行结果：0=成功，1=失败 |

### 4.4 示例

#### 示例1：布防成功
```
中心→主机：C1234,2,0,9876,126
主机→中心：c1234,0,126
```

#### 示例2：撤防失败
```
中心→主机：C1234,1,0,9877,127
主机→中心：c1234,1,127
```

#### 示例3：打开输出（2秒后自动关闭）
```
中心→主机：C4321,6,10*2,9876,128
主机→中心：c4321,0,128
```

#### 示例4：打开输出（预编程时间关闭）
```
中心→主机：C4321,6,11,9876,129
主机→中心：c4321,0,129
```

#### 示例5：关闭输出
```
中心→主机：C4321,7,12,9876,130
主机→中心：c4321,0,130
```

## 5. 查询分区和防区状态

### 5.1 状态查询响应格式

```
S{status_string}\n
```

### 5.2 状态字符说明

| 字符 | 状态 |
|------|------|
| 0 | 分区撤防 |
| 1 | 分区布防 |
| 其他 | 待补充 |

## 6. 序列号规则

- 主机复位后第一次报文为 **0**
- 每次报文 **+1**
- 超过 **9999** 时折返为 **1**（注意不是0）
- 应答报文序列号等于报告报文序列号

## 7. Gateway实现要点

### 7.1 TCP服务器配置

```java
NetServerOptions options = new NetServerOptions()
    .setHost("0.0.0.0")              // 监听所有网卡
    .setPort(9988)                   // 默认端口
    .setTcpKeepAlive(true)           // 启用 TCP Keep-Alive
    .setTcpNoDelay(true)             // 禁用 Nagle 算法
    .setReuseAddress(true);          // 允许端口复用
```

### 7.2 数据帧解析

```java
// 1. 检查以E开头
if (!frame.startsWith("E")) return;

// 2. 去掉E，按逗号分隔
String content = frame.substring(1);
String[] parts = content.split(",");

// 3. 解析字段
String account = parts[0];
String eventData = parts[1];
String eventCode = eventData.substring(0, 4);
String area = eventData.substring(4, 6);
String point = eventData.substring(6, 9);
String sequence = eventData.substring(9);
```

### 7.3 应答发送

```java
String ack = String.format("e%s,%s\n", account, sequence);
socket.write(Buffer.buffer(ack));
```

### 7.4 心跳判断

```java
if ("0000".equals(eventCode)) {
    // 这是心跳消息
    handleHeartbeat(account);
}
```

## 8. 常见问题

### Q1: 为什么TCP工具收不到消息？

**A**: 报警主机协议是双向的，必须发送应答。如果不应答，报警主机会认为连接失败，断开连接或不再发送数据。

### Q2: 报警主机连接后立即断开？

**A**: 检查是否正确发送了应答。每收到一条消息（包括心跳），都必须回复应答。

### Q3: 如何区分心跳和事件？

**A**: 检查event_code字段：
- `0000` = 心跳
- `1xxx` = 新事件（千位为1）
- `3xxx` = 事件恢复（千位为3）

### Q4: 序列号如何处理？

**A**: 
- 接收时：从数据帧中提取sequence字段
- 应答时：使用相同的sequence
- 发送控制指令时：使用上次接收的sequence + 1

## 9. 参考文件

- Gateway实现：`yudao-module-iot-gateway/src/main/java/cn/iocoder/yudao/module/iot/gateway/protocol/alarm/`
- 厂家Demo：`F:\work\ch_ibms\opc_src\OpcServer.cpp`
- 配置文件：`yudao-module-iot-gateway/src/main/resources/application.yaml`
