# 报警主机连接配置指南

## 问题分析

当前错误：`主机未连接 (account=null)`

### 根本原因

1. **数据库中主机记录的account字段为空**
2. **报警主机尚未连接到Gateway的TCP服务器**

## PS600 OPC 协议连接模式

根据厂家协议文档，PS600报警主机采用**主动连接模式**：

```
报警主机（客户端） → 主动连接 → 中心服务器（Gateway）
```

**不是**：
```
中心服务器 → 主动连接 → 报警主机  ❌
```

### 连接流程

1. **Gateway启动TCP服务器**
   - 监听端口：9988（配置在`application.yaml`）
   - 等待报警主机连接

2. **报警主机主动连接**
   - 报警主机配置中心IP和端口
   - 主机主动发起TCP连接到Gateway

3. **认证和注册**
   - 主机连接后发送认证信息
   - Gateway验证并注册连接
   - 建立account与TCP连接的映射

4. **保持连接**
   - 定期心跳
   - 事件上报
   - 接收控制命令

## 解决方案

### 方案一：配置报警主机连接到Gateway（推荐）

#### 1. 确认Gateway TCP服务器已启动

查看Gateway日志，应该看到：
```
[IotAlarmUpstreamProtocol] 报警主机协议已启动，监听端口: 9988
```

#### 2. 配置报警主机

在报警主机设备上配置中心服务器信息：

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 中心IP | `192.168.1.xxx` | Gateway服务器的IP地址 |
| 中心端口 | `9988` | Gateway监听端口 |
| 主机账号 | `1234` | 主机唯一标识 |
| 密码 | `123456` | 认证密码（可选） |

**配置位置**：
- 通过报警主机的Web管理界面配置
- 或通过厂家提供的配置工具（如`F:\work\ch_ibms\opc_src`中的客户端工具）

#### 3. 更新数据库中的主机记录

```sql
-- 更新主机账号（必须与报警主机配置的账号一致）
UPDATE iot_alarm_host 
SET 
  account = '1234',           -- 主机账号
  password = '123456',        -- 密码（可选）
  ip_address = '192.168.1.210', -- 主机IP（用于识别）
  port = 9988                 -- Gateway端口
WHERE id = 1;
```

**重要**：`account`字段必须与报警主机配置的账号完全一致！

#### 4. 报警主机连接到Gateway

- 重启报警主机或触发连接
- 主机会主动连接到Gateway的9988端口
- Gateway收到连接后会进行认证

#### 5. 验证连接

查看Gateway日志：
```
[IotAlarmUpstreamHandler] 收到新连接: 192.168.1.210:xxxxx
[IotAlarmUpstreamHandler] 设备认证成功: account=1234
[IotAlarmConnectionManager] 注册连接: account=1234
```

### 方案二：使用厂家客户端工具测试

如果暂时无法配置实际的报警主机，可以使用厂家提供的客户端工具模拟：

#### 1. 编译客户端工具

```bash
# 进入源码目录
cd F:\work\ch_ibms\opc_src

# 使用Visual Studio打开项目
# 编译生成客户端程序
```

#### 2. 配置客户端连接参数

在客户端工具中配置：
- 服务器IP：Gateway服务器IP
- 服务器端口：9988
- 主机账号：1234
- 密码：123456（如果需要）

#### 3. 启动客户端连接

- 点击"连接"按钮
- 客户端会连接到Gateway
- 可以发送测试命令

## 数据库配置检查清单

### 1. 检查主机记录

```sql
SELECT 
  id,
  host_name,
  account,
  password,
  ip_address,
  port,
  online_status
FROM iot_alarm_host
WHERE id = 1;
```

**必须确保**：
- ✅ `account`字段不为空
- ✅ `account`与报警主机配置一致
- ✅ `ip_address`是报警主机的实际IP
- ✅ `port`是Gateway的监听端口（9988）

### 2. 更新主机配置

```sql
-- 示例：更新ID为1的主机
UPDATE iot_alarm_host 
SET 
  account = '1234',
  password = '123456',
  ip_address = '192.168.1.210',
  port = 9988
WHERE id = 1;
```

## Gateway配置检查

### 1. 检查配置文件

`yudao-module-iot-gateway/src/main/resources/application.yaml`

```yaml
yudao:
  iot:
    gateway:
      protocol:
        alarm:
          enabled: true           # ✅ 必须为true
          port: 9988             # ✅ TCP监听端口
          idle-timeout: 300      # 空闲超时（秒）
          ssl-enabled: false     # 是否启用SSL
```

### 2. 检查防火墙

确保9988端口未被防火墙阻止：

```powershell
# Windows防火墙检查
netstat -an | findstr 9988

# 应该看到：
# TCP    0.0.0.0:9988           0.0.0.0:0              LISTENING
```

### 3. 检查Gateway日志

启动Gateway后，查看日志：

```
✅ 正常日志：
[IotAlarmUpstreamProtocol] 报警主机协议已启动，监听端口: 9988
[IotAlarmConnectionManager] 连接管理器已初始化

❌ 异常日志：
Failed to bind to port 9988  # 端口被占用
Connection refused           # 防火墙阻止
```

## 连接状态管理

### 连接注册流程

```java
// 1. 报警主机连接到Gateway
Socket -> Gateway:9988

// 2. 发送认证帧
主机 -> Gateway: "1234,0,0,9876,001\n"

// 3. Gateway验证并注册
IotAlarmConnectionManager.register(account="1234", socket)

// 4. 建立映射
Map<String, Socket> connections
  "1234" -> Socket实例
```

### 查询时的连接查找

```java
// Biz层调用
GET /api/alarm/host/1234/query-status

// Gateway查找连接
Socket socket = connectionManager.getConnection("1234");
if (socket == null) {
    throw new RuntimeException("主机未连接");  // ← 当前错误
}

// 发送查询命令
socket.write("C1234,10,0,9876,131\n");
```

## 协议帧格式

### 认证帧（主机→中心）

```
格式：{account},0,0,9876,{sequence}\n

示例：1234,0,0,9876,001\n

说明：
- account: 主机账号（如：1234）
- 0,0: 固定参数
- 9876: 随机数
- sequence: 序列号
```

### 心跳帧（主机→中心）

```
格式：{account},1,0,9876,{sequence}\n

示例：1234,1,0,9876,002\n
```

### 查询命令（中心→主机）

```
格式：C{account},10,0,9876,{sequence}\n

示例：C1234,10,0,9876,131\n

说明：
- C: 命令前缀（大写）
- 10: 查询状态命令码
```

### 查询响应（主机→中心）

```
格式：c{account},0,{sequence}É{data}\n

示例：c1234,0,131ÉS0aaaaaaAB\n

说明：
- c: 响应前缀（小写）
- 0: 成功码
- É: 分隔符
- S0aaaaaaAB: 状态数据
```

## 故障排查步骤

### 1. 检查Gateway是否启动

```bash
# 查看进程
jps | findstr gateway

# 查看端口
netstat -an | findstr 9988
```

### 2. 检查数据库配置

```sql
-- 查看主机配置
SELECT * FROM iot_alarm_host WHERE id = 1;

-- 确认account不为空
-- 确认ip_address正确
```

### 3. 检查报警主机配置

- 登录报警主机Web界面
- 查看"中心服务器"配置
- 确认IP和端口正确
- 确认账号一致

### 4. 检查网络连通性

```bash
# 从报警主机ping Gateway服务器
ping 192.168.1.xxx

# 从Gateway服务器telnet报警主机（如果支持）
telnet 192.168.1.210 80
```

### 5. 查看Gateway日志

```bash
# 查看连接日志
tail -f ~/logs/iot-gateway-server.log | grep "IotAlarm"

# 应该看到：
# [IotAlarmUpstreamHandler] 收到新连接
# [IotAlarmConnectionManager] 注册连接: account=1234
```

### 6. 查看Biz层日志

```bash
# 查看查询日志
tail -f ~/logs/yudao-server.log | grep "IotAlarmPartition"

# 正常日志：
# [queryAndSyncHostStatus] 调用Gateway查询主机状态 account=1234
# [queryAndSyncHostStatus] 查询成功 systemStatus=0, zoneCount=8

# 异常日志：
# [queryAndSyncHostStatus] 调用Gateway查询主机状态 account=null  ← account为空
# [queryAndSyncHostStatus] 查询失败 主机未连接  ← 主机未连接
```

## 常见错误及解决方案

### 错误1：account=null

**原因**：数据库中主机记录的account字段为空

**解决**：
```sql
UPDATE iot_alarm_host SET account = '1234' WHERE id = 1;
```

### 错误2：主机未连接

**原因**：报警主机尚未连接到Gateway

**解决**：
1. 确认Gateway已启动并监听9988端口
2. 配置报警主机连接到Gateway
3. 检查网络连通性
4. 查看Gateway日志确认连接状态

### 错误3：端口被占用

**原因**：9988端口已被其他程序占用

**解决**：
```bash
# 查找占用进程
netstat -ano | findstr 9988

# 结束进程或更换端口
```

### 错误4：认证失败

**原因**：账号或密码不匹配

**解决**：
1. 确认数据库中的account与报警主机配置一致
2. 确认密码正确（如果启用密码验证）

## 测试步骤

### 1. 准备工作

```sql
-- 1. 更新主机配置
UPDATE iot_alarm_host 
SET 
  account = '1234',
  password = '123456',
  ip_address = '192.168.1.210',
  port = 9988
WHERE id = 1;

-- 2. 验证配置
SELECT account, ip_address, port FROM iot_alarm_host WHERE id = 1;
```

### 2. 启动Gateway

```bash
# 启动Gateway服务
# 查看日志确认TCP服务器已启动
```

### 3. 配置报警主机

- 登录报警主机Web界面
- 配置中心服务器：
  - IP: Gateway服务器IP
  - 端口: 9988
  - 账号: 1234
  - 密码: 123456

### 4. 连接报警主机

- 保存配置
- 重启报警主机或触发连接
- 查看Gateway日志确认连接成功

### 5. 测试查询

```bash
# 访问API测试
curl "http://localhost:3001/admin-api/iot/alarm/host/1/partitions"

# 应该返回分区列表，不再报错
```

## 参考资料

- 厂家协议文档：PS600 OPC v1.2
- 客户端示例代码：`F:\work\ch_ibms\opc_src`
- Gateway实现：`yudao-module-iot-gateway/src/main/java/cn/iocoder/yudao/module/iot/gateway/protocol/alarm/`
- 架构文档：`docs/alarm_host_architecture.md`

## 总结

**关键点**：
1. ✅ 报警主机主动连接到Gateway（不是Gateway连接主机）
2. ✅ 数据库中的account字段必须有值且与主机配置一致
3. ✅ Gateway必须启动并监听9988端口
4. ✅ 网络必须连通，防火墙不能阻止
5. ✅ 主机连接后才能进行查询操作

**连接顺序**：
```
1. 启动Gateway → 监听9988端口
2. 配置数据库 → 设置account
3. 配置报警主机 → 设置中心IP/端口/账号
4. 主机连接 → 建立TCP连接
5. 认证注册 → 建立account映射
6. 开始通信 → 查询、控制、事件上报
```
