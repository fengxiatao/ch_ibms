# IoT 模块文档中心

## 📚 文档导航

### 🎯 快速开始
- [消息通信架构说明](./消息通信架构说明.md) ⭐ **必读** - 理解 RocketMQ 和 MQTT 的使用场景
- [RocketMQ Topic命名紧急修正说明](./RocketMQ_Topic命名紧急修正说明.md) 🚨 **重要** - Topic 命名规则修正
- [错误记录索引](./errors/INDEX.md) 🔧 **问题排查** - 常见错误及解决方案

### 📋 Topic 规范文档

#### 1. RocketMQ Topic（内部微服务通信）
- [IoT消息主题命名规范](./IoT消息主题命名规范.md) - 基础命名规则
- [IoT消息主题使用清单](./IoT消息主题使用清单.md) - 当前使用情况
- [IoT消息主题设计方案_V2](./IoT消息主题设计方案_V2.md) - 优化方案（参考 AIOT 标准）

#### 2. MQTT Topic（物理设备接入）
- 参考：AIOT 设备接入标准.docx（项目根目录）
- 使用场景：物理设备 ↔ Gateway 通信

### 📖 最佳实践
- [代码开发检查清单](./best-practices/代码开发检查清单.md)
- [设备适用模块配置指南](./best-practices/设备适用模块配置指南.md)
- [后端开发最佳实践](./best-practices/backend.md)

### 🐛 错误处理
- [错误索引](./errors/INDEX.md)
- [快速指南](./errors/QUICK_GUIDE.md)

### 📝 开发会话记录
- [会话索引](./sessions/INDEX.md)

---

## 🔍 关键概念澄清

### RocketMQ vs MQTT

很多开发者会混淆这两个消息系统，这里做一个清晰的区分：

| 对比项 | RocketMQ | MQTT |
|--------|----------|------|
| **使用场景** | 内部微服务通信 | 物理设备接入 |
| **通信双方** | Gateway ↔ Biz | 设备 ↔ Gateway |
| **Topic 格式** | `iot.device.online` | `$oc/devices/{device_id}/sys/...` |
| **协议特点** | 企业级消息队列 | 轻量级 IoT 协议 |
| **项目实现** | IotRocketMQMessageBus | EMQX + Vert.x MQTT |

### 完整通信链路

```
物理设备 ─MQTT→ EMQX ─订阅→ Gateway ─RocketMQ→ Biz ─WebSocket→ 前端
   │                      │           │                │
 (摄像头)              (MQTT Server) (内部消息总线)    (管理界面)
```

**关键点：**
1. ✅ RocketMQ 用于 Gateway 和 Biz 之间的可靠通信
2. ✅ MQTT 用于设备和 Gateway 之间的轻量级通信
3. ✅ 两者不冲突，各司其职

详细说明请阅读：[消息通信架构说明](./消息通信架构说明.md)

---

## 📌 文档维护规范

### 新增文档
1. 在对应目录创建文档
2. 更新本 README.md 索引
3. 添加文档版本和最后更新时间

### 文档分类
- `best-practices/` - 最佳实践和开发规范
- `errors/` - 错误记录和解决方案
- `sessions/` - 开发会话记录
- 根目录 - Topic 规范和架构文档

### 文档模板
请参考：[错误文档模板](./errors/ERROR_TEMPLATE.md)

---

## 🚀 快速链接

### 外部参考
- [AIOT 设备接入标准.docx](../../AIOT设备接入标准.docx) - 行业标准
- [芋道源码 - MyBatis 文档](https://doc.iocoder.cn/mybatis/)
- [RocketMQ 官方文档](https://rocketmq.apache.org/)
- [MQTT 协议规范](https://mqtt.org/mqtt-specification/)

### 项目配置
- [Gateway 配置文件](../yudao-module-iot-gateway/src/main/resources/application.yaml)
- [Biz 配置文件](../yudao-module-iot-biz/src/main/resources/application.yaml)

---

**文档维护者**：长辉信息科技有限公司  
**最后更新**：2025-10-27

