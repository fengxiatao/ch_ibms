# IoT 网关迁移指南

## 概述

本文档描述了从旧网关模块 (`yudao-module-iot-gateway`) 迁移到新网关模块 (`yudao-module-iot-newgateway`) 的步骤、配置变更和兼容性说明。

新网关采用**设备插件化架构**，将每个设备类型作为独立的插件模块，实现设备间完全隔离、架构清晰、易于扩展的目标。

## 目录

1. [迁移策略](#迁移策略)
2. [迁移步骤](#迁移步骤)
3. [配置变更](#配置变更)
4. [兼容性说明](#兼容性说明)
5. [并行运行](#并行运行)
6. [回滚方案](#回滚方案)
7. [常见问题](#常见问题)

---

## 迁移策略

### 渐进式迁移

新网关设计支持与旧网关**并行运行**，可以逐个设备类型进行迁移：

```
阶段1: 部署新网关，禁用所有插件
阶段2: 启用单个插件（如 alarm），验证功能
阶段3: 逐步启用其他插件
阶段4: 验证所有功能正常后，停用旧网关
阶段5: 删除旧网关代码
```

### 风险控制

- **模块隔离**：新旧网关使用不同的 Maven 模块和包名
- **配置隔离**：新网关使用 `iot.newgateway.*` 配置前缀
- **端口隔离**：新网关插件可配置不同的监听端口
- **快速回滚**：如有问题可立即切回旧网关

---

## 迁移步骤

### 步骤 1: 添加新网关模块依赖

在 `yudao-server/pom.xml` 中添加新网关模块依赖：

```xml
<!-- 新网关模块 -->
<dependency>
    <groupId>cn.iocoder.boot</groupId>
    <artifactId>yudao-module-iot-newgateway</artifactId>
    <version>${revision}</version>
</dependency>
```

### 步骤 2: 配置新网关

在 `application.yaml` 中添加新网关配置：

```yaml
iot:
  newgateway:
    core:
      heartbeat-check-interval: 30000
      device-offline-threshold: 90000
      worker-threads: 4
      health-check-enabled: true
      metrics-enabled: true
    plugins:
      enabled:
        alarm: false        # 初始禁用，逐步启用
        changhui: false
        access-gen1: false
        access-gen2: false
        nvr: false
```

### 步骤 3: 逐步启用插件

按设备类型逐个启用插件并验证：

```yaml
# 第一步：启用报警主机插件
iot:
  newgateway:
    plugins:
      enabled:
        alarm: true
      alarm:
        port: 9500
        heartbeat-timeout: 60000
```

### 步骤 4: 验证功能

每启用一个插件后，验证以下功能：
- 设备连接/断开
- 心跳检测
- 命令执行
- 事件上报
- 状态同步

### 步骤 5: 禁用旧网关对应功能

确认新插件工作正常后，禁用旧网关中对应的设备处理逻辑。

### 步骤 6: 完成迁移

所有设备类型迁移完成后：

1. 从 `yudao-server/pom.xml` 移除旧网关依赖
2. 删除 `yudao-module-iot-gateway` 模块目录
3. 从父 `pom.xml` 移除模块引用

---

## 配置变更

### 配置前缀变更

| 旧配置前缀 | 新配置前缀 |
|-----------|-----------|
| `iot.gateway.*` | `iot.newgateway.*` |
| `iot.gateway.tcp.*` | `iot.newgateway.plugins.{type}.*` |
| `iot.gateway.alarm.*` | `iot.newgateway.plugins.alarm.*` |

### 核心配置对照

#### 旧网关配置

```yaml
iot:
  gateway:
    tcp:
      port: 9500
      heartbeat-timeout: 60000
    alarm:
      enabled: true
```

#### 新网关配置

```yaml
iot:
  newgateway:
    core:
      heartbeat-check-interval: 30000
      device-offline-threshold: 90000
    plugins:
      enabled:
        alarm: true
      alarm:
        port: 9500
        heartbeat-timeout: 60000
```

### 插件配置详情

#### 报警主机插件 (alarm)

```yaml
iot:
  newgateway:
    plugins:
      alarm:
        port: 9500                    # TCP监听端口
        heartbeat-timeout: 60000      # 心跳超时（毫秒）
```

#### 长辉TCP插件 (changhui)

```yaml
iot:
  newgateway:
    plugins:
      changhui:
        port: 9700                    # TCP监听端口
        heartbeat-timeout: 60000      # 心跳超时（毫秒）
        upgrade-timeout: 300000       # 升级超时（毫秒）
```

#### 门禁一代插件 (access-gen1)

```yaml
iot:
  newgateway:
    plugins:
      access-gen1:
        sdk-path: ""                  # 大华SDK路径
        reconnect-interval: 30000     # 重连间隔（毫秒）
```

#### 门禁二代插件 (access-gen2)

```yaml
iot:
  newgateway:
    plugins:
      access-gen2:
        sdk-path: ""                  # 大华SDK路径
        reconnect-interval: 30000     # 重连间隔（毫秒）
```

#### NVR插件 (nvr)

```yaml
iot:
  newgateway:
    plugins:
      nvr:
        sdk-path: ""                  # 大华SDK路径
        reconnect-interval: 30000     # 重连间隔（毫秒）
```

### 日志配置

```yaml
logging:
  level:
    # 核心框架
    cn.iocoder.yudao.module.iot.newgateway.core: INFO
    # 各插件
    cn.iocoder.yudao.module.iot.newgateway.plugins.alarm: INFO
    cn.iocoder.yudao.module.iot.newgateway.plugins.changhui: INFO
    cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1: INFO
    cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2: INFO
    cn.iocoder.yudao.module.iot.newgateway.plugins.nvr: INFO
```

### Actuator 端点配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,gateway,gateway-metrics,metrics
  endpoint:
    health:
      show-details: always
    gateway:
      enabled: true
    gateway-metrics:
      enabled: true
```

---

## 兼容性说明

### 消息主题兼容

新网关**复用** `iot-core` 中定义的消息主题，与旧网关保持一致：

| 主题 | 说明 |
|------|------|
| `iot_device_state_changed` | 设备状态变更 |
| `iot_alarm_host_event` | 报警主机事件 |
| `iot_alarm_host_control` | 报警主机控制命令 |
| `iot_access_control_device_command` | 门禁控制命令 |
| `iot_access_control_device_response` | 门禁控制响应 |
| `iot_access_control_event` | 门禁事件 |
| `iot_device_connect_request` | 设备连接请求 |
| `iot_device_connect_result` | 设备连接结果 |

### DTO 兼容

新网关复用 `iot-core` 中的公共 DTO：

| DTO | 包路径 |
|-----|--------|
| `DeviceStateChangeMessage` | `cn.iocoder.yudao.module.iot.core.gateway.dto` |
| `DeviceInfo` | `cn.iocoder.yudao.module.iot.core.gateway.dto` |
| `AccessControlDeviceCommand` | `cn.iocoder.yudao.module.iot.core.gateway.dto` |
| `AccessControlDeviceResponse` | `cn.iocoder.yudao.module.iot.core.gateway.dto` |
| `AccessControlEventMessage` | `cn.iocoder.yudao.module.iot.core.gateway.dto` |

### Biz 层无需修改

由于消息主题和 DTO 保持兼容，**Biz 层代码无需任何修改**。

### 数据库兼容

新网关不涉及数据库结构变更，与旧网关使用相同的数据表。

---

## 并行运行

### 端口规划

为避免端口冲突，建议新旧网关使用不同端口：

| 设备类型 | 旧网关端口 | 新网关端口 |
|---------|-----------|-----------|
| 报警主机 | 9500 | 9510 |
| 长辉TCP | 9700 | 9710 |

### 并行运行配置示例

```yaml
# 旧网关配置（保持不变）
iot:
  gateway:
    alarm:
      port: 9500

# 新网关配置（使用不同端口）
iot:
  newgateway:
    plugins:
      alarm:
        port: 9510
```

### 设备分流

在并行运行期间，可以将部分设备指向新网关端口进行测试：
1. 选择测试设备
2. 修改设备配置，指向新网关端口
3. 验证功能正常
4. 逐步迁移更多设备

---

## 回滚方案

### 快速回滚步骤

如果新网关出现问题，可以快速回滚：

1. **禁用新网关插件**
   ```yaml
   iot:
     newgateway:
       plugins:
         enabled:
           alarm: false
           changhui: false
           access-gen1: false
           access-gen2: false
           nvr: false
   ```

2. **重启应用**

3. **验证旧网关功能正常**

### 完全回滚

如需完全移除新网关：

1. 从 `yudao-server/pom.xml` 移除新网关依赖
2. 删除 `application.yaml` 中的 `iot.newgateway.*` 配置
3. 重新编译部署

---

## 常见问题

### Q1: 新旧网关可以同时处理同一设备吗？

**不建议**。同一设备应该只由一个网关处理，否则可能导致状态不一致。

### Q2: 迁移过程中设备会断线吗？

如果使用不同端口并行运行，设备不会断线。切换时需要修改设备配置指向新端口。

### Q3: 如何验证新网关功能正常？

1. 检查 Actuator 健康端点：`/actuator/health`
2. 检查网关状态端点：`/actuator/gateway`
3. 检查指标端点：`/actuator/gateway-metrics`
4. 查看日志确认设备连接和消息处理

### Q4: 新网关支持哪些设备类型？

| 插件ID | 设备类型 | 连接模式 |
|--------|---------|---------|
| alarm | 报警主机 | 被动连接 (TCP) |
| changhui | 长辉TCP设备 | 被动连接 (TCP) |
| access-gen1 | 门禁一代 | 主动连接 (SDK) |
| access-gen2 | 门禁二代 | 主动连接 (SDK) |
| nvr | NVR/IPC | 主动连接 (SDK) |

### Q5: 如何添加新的设备类型？

参考 `plugins/template/` 目录下的模板，复制并实现设备特定逻辑。详见 [插件开发指南](./PLUGIN_DEVELOPMENT.md)。

---

## 架构对比

### 旧网关架构

```
yudao-module-iot-gateway/
├── handler/           # 设备处理器（耦合）
├── protocol/          # 协议处理（分散）
├── connection/        # 连接管理（全局）
├── lifecycle/         # 生命周期（全局）
└── service/           # 业务服务（混合）
```

**问题**：
- 设备类型间存在耦合
- 修改一个设备可能影响其他设备
- 代码职责不清晰

### 新网关架构

```
yudao-module-iot-newgateway/
├── core/              # 核心框架（稳定）
│   ├── annotation/    # 插件注解
│   ├── config/        # 配置管理
│   ├── connection/    # 连接接口
│   ├── handler/       # 处理器接口
│   ├── health/        # 健康检查
│   ├── lifecycle/     # 生命周期
│   ├── logging/       # 日志工具
│   ├── message/       # 消息发布
│   ├── metrics/       # 指标收集
│   ├── model/         # 数据模型
│   └── registry/      # 插件注册
├── plugins/           # 设备插件（独立）
│   ├── alarm/         # 报警主机
│   ├── changhui/      # 长辉TCP
│   ├── accessgen1/    # 门禁一代
│   ├── accessgen2/    # 门禁二代
│   ├── nvr/           # NVR
│   └── template/      # 插件模板
└── consumer/          # 消息消费
```

**优势**：
- 设备类型完全隔离
- 插件可独立启用/禁用
- 架构清晰，易于维护
- 便于添加新设备类型

---

## 联系方式

如有迁移问题，请联系 IoT 网关开发团队。
