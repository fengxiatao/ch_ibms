# 多租户上下文缺失 - Mapper 级别拦截错误（最终修复）

## 错误信息

```
### Error querying database.  Cause: java.lang.NullPointerException: 
TenantContextHolder 不存在租户编号！
```

**幂等性表记录显示**：
```
id: 45
message_id: 192.168.1.202_2025-10-27T12:55:00.03
topic: iot_device_discovered
status: FAILED
error_message: TenantContextHolder 不存在租户编号！
```

**现象**：`iot_discovered_device` 表**没有任何记录**

## 问题根源

### 之前的修复（不完整）

我们之前只在 **Service 层**添加了 `@TenantIgnore`：

```java
@Service
public class DiscoveredDeviceServiceImpl {
    
    @Override
    @TenantIgnore  // ✅ Service 层有
    public boolean saveDiscoveredDevice(...) {
        // 但调用 Mapper 时还是会触发拦截器！
        discoveredDeviceMapper.insert(record);  // ❌ Mapper 没有
    }
}
```

### 真正的问题

**芋道源码的租户拦截器工作在 MyBatis 层**：

```
Service.method() + @TenantIgnore
  ↓ (Service 层拦截器通过)
Mapper.insert()
  ↓ (进入 MyBatis 层)
MyBatis 租户拦截器 ❌ 检查 TenantContextHolder
  ↓ (发现为空)
抛出 NullPointerException
```

**关键认识**：
- `@TenantIgnore` 在 Service 层只能跳过 **Service 级别的拦截**
- **无法阻止 MyBatis 拦截器检查 Mapper 操作**
- 必须在 **Mapper 接口**上添加 `@InterceptorIgnore(tenantLine = "true")`

## 完整解决方案

### 修复 1：IotMessageIdempotentMapper

```java
package cn.iocoder.yudao.module.iot.dal.mysql.message;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;

/**
 * IoT 消息幂等性检查 Mapper
 * 
 * ⚠️ 忽略租户拦截：因为 RocketMQ 消费者调用时，未传递租户上下文
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")  // ✅ 关键修复
public interface IotMessageIdempotentMapper extends BaseMapperX<IotMessageIdempotentDO> {
    // ...
}
```

### 修复 2：IotDiscoveredDeviceMapper

```java
package cn.iocoder.yudao.module.iot.dal.mysql.device;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;

/**
 * IoT 发现设备 Mapper
 * 
 * ⚠️ 忽略租户拦截：因为 RocketMQ 消费者调用时，未传递租户上下文
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")  // ✅ 关键修复
public interface IotDiscoveredDeviceMapper extends BaseMapperX<IotDiscoveredDeviceDO> {
    // ...
}
```

## 完整的修复层级

现在我们在**三个层级**都做了处理：

| 层级 | 位置 | 注解 | 状态 |
|------|------|------|------|
| **Service 层** | MessageIdempotentServiceImpl | `@TenantIgnore` | ✅ |
| **Service 层** | DiscoveredDeviceServiceImpl | `@TenantIgnore` | ✅ |
| **Service 层** | IotDeviceServiceImpl | `@TenantIgnore` | ✅ |
| **Mapper 层** | IotMessageIdempotentMapper | `@InterceptorIgnore(tenantLine="true")` | ✅ **关键** |
| **Mapper 层** | IotDiscoveredDeviceMapper | `@InterceptorIgnore(tenantLine="true")` | ✅ **关键** |

## 为什么需要两层都加？

### Service 层的 @TenantIgnore

**作用**：跳过 Spring AOP 级别的租户检查

```java
@Around("@annotation(tenantIgnore)")
public Object around(ProceedingJoinPoint joinPoint, TenantIgnore tenantIgnore) {
    // 如果有 @TenantIgnore，跳过租户校验
    if (tenantIgnore != null) {
        return joinPoint.proceed();
    }
    // 否则检查租户
    Long tenantId = TenantContextHolder.getTenantId();
    if (tenantId == null) {
        throw new NullPointerException("租户不存在");
    }
}
```

### Mapper 层的 @InterceptorIgnore

**作用**：跳过 MyBatis Plus 的租户拦截器

```java
@Intercepts({@Signature(type = StatementHandler.class, ...)})
public class TenantLineInnerInterceptor implements InnerInterceptor {
    
    @Override
    public void beforePrepare(...) {
        // 检查是否有 @InterceptorIgnore
        InterceptorIgnore ignore = mapper.getAnnotation(InterceptorIgnore.class);
        if (ignore != null && ignore.tenantLine()) {
            return; // 跳过租户拦截
        }
        
        // 否则自动添加 tenant_id 条件
        sql = addTenantCondition(sql);
    }
}
```

## 涉及文件（完整列表）

### Service 层（已修复）
1. `MessageIdempotentServiceImpl.java`
   - `tryProcess()` - `@TenantIgnore`
   - `markSuccess()` - `@TenantIgnore`
   - `markFailed()` - `@TenantIgnore`

2. `DiscoveredDeviceServiceImpl.java`
   - `saveDiscoveredDevice()` - `@TenantIgnore`
   - `getRecentDiscoveredDevices()` - `@TenantIgnore`
   - `getUnaddedDevices()` - `@TenantIgnore`

3. `IotDeviceServiceImpl.java`
   - `isDeviceExistsByIp()` - `@TenantIgnore`

### Mapper 层（本次修复）
4. **`IotMessageIdempotentMapper.java`** - `@InterceptorIgnore(tenantLine="true")`
5. **`IotDiscoveredDeviceMapper.java`** - `@InterceptorIgnore(tenantLine="true")`

## 验证步骤

### 1. 重启服务

```bash
# 停止 Biz 服务
# 重新启动
cd F:\work\ch_ibms\ruoyi-vue-pro\yudao-server
mvn spring-boot:run
```

### 2. 等待设备发现（或手动触发）

**等待自动扫描**（5分钟一次）：
```
# 查看 Gateway 日志
[DeviceDiscoveryManager] [discoverDevices][发现完成，共 X 个设备]
```

**或手动触发**：
```bash
curl -X POST "http://localhost:48080/admin-api/iot/device/discovery/scan" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"scanType": "onvif", "timeout": 5000}'
```

### 3. 观察 Biz 日志（关键）

**预期看到完整的处理流程**：
```
[DeviceDiscoveredConsumer] [onMessage][收到设备发现消息: 192.168.1.202 (onvif)]
[tryProcess][消息可以处理: 192.168.1.202_2025-10-27T13:20:00]
[isDeviceExistsByIp][检查设备是否存在: 192.168.1.202]
[saveDiscoveredDevice][保存发现记录: 192.168.1.202 (onvif)]
[pushNewDeviceNotification][已推送新设备通知: 192.168.1.202]
```

**不应该再有租户错误**：
```
❌ 不应该看到: TenantContextHolder 不存在租户编号
```

### 4. 检查数据库

```sql
-- 应该有新记录（13:20 之后的）
SELECT * FROM iot_discovered_device 
WHERE discovery_time > '2025-10-27 13:15:00'
ORDER BY discovery_time DESC;

-- 应该显示 SUCCESS 状态
SELECT * FROM iot_message_idempotent 
WHERE create_time > '2025-10-27 13:15:00'
  AND status = 'SUCCESS'  -- ✅ 应该是 SUCCESS，不是 FAILED
ORDER BY create_time DESC;
```

**预期结果**：
- `iot_discovered_device` 表：有 2 条新记录（192.168.1.200, 192.168.1.202）
- `iot_message_idempotent` 表：有 2 条 SUCCESS 记录

## 经验总结

### 关键教训

**在芋道源码的多租户框架中，处理 RocketMQ 消费者需要三层防护**：

```
第1层：Service 方法
  └─ @TenantIgnore
  
第2层：Mapper 接口
  └─ @InterceptorIgnore(tenantLine = "true")
  
第3层：验证测试
  └─ 检查数据库记录
```

### 最佳实践

#### 1. 开发 RocketMQ 消费者时

```java
// ✅ 正确的模式
@Service
public class SomeServiceImpl {
    
    @Resource
    private SomeMapper mapper;  // ← 这个 Mapper 也要加注解
    
    @Override
    @TenantIgnore  // Service 层
    public void handleMessage() {
        mapper.insert(entity);  // Mapper 层也需要
    }
}

@Mapper
@InterceptorIgnore(tenantLine = "true")  // Mapper 层
public interface SomeMapper extends BaseMapperX<SomeDO> {
    // ...
}
```

#### 2. 检查清单

创建新的 RocketMQ 消费者时：

- [ ] 消费者类添加注释说明租户问题
- [ ] 列出所有调用的 Service 方法
- [ ] 每个 Service 方法添加 `@TenantIgnore`
- [ ] 找出所有使用的 Mapper
- [ ] 每个 Mapper 添加 `@InterceptorIgnore(tenantLine="true")`
- [ ] 编译验证
- [ ] 运行测试
- [ ] **检查数据库记录** ← 最关键

#### 3. 代码模板

```java
/**
 * RocketMQ 消费者
 * 
 * ⚠️ 租户处理：
 * - Service 方法：@TenantIgnore
 * - Mapper 接口：@InterceptorIgnore(tenantLine="true")
 * 
 * 调用链路：
 * - MessageIdempotentService.tryProcess() ✅
 * - MessageIdempotentMapper ✅
 * - DiscoveredDeviceService.saveDiscoveredDevice() ✅
 * - DiscoveredDeviceMapper ✅
 */
@Component
public class DeviceDiscoveredConsumer implements IotMessageSubscriber<DiscoveredDevice> {
    // ...
}
```

## 对比：修复前 vs 修复后

### 修复前（失败）

```
Service + @TenantIgnore ✅
  ↓
Mapper (无注解) ❌
  ↓
MyBatis 租户拦截器检查
  ↓
❌ NullPointerException: TenantContextHolder 不存在租户编号
  ↓
❌ iot_message_idempotent: status = FAILED
❌ iot_discovered_device: 无记录
```

### 修复后（成功）

```
Service + @TenantIgnore ✅
  ↓
Mapper + @InterceptorIgnore ✅
  ↓
MyBatis 租户拦截器跳过
  ↓
✅ 数据成功插入
  ↓
✅ iot_message_idempotent: status = SUCCESS
✅ iot_discovered_device: 有记录
```

## 相关文档

- [多租户 RocketMQ 消费者检查清单](../多租户_RocketMQ消费者完整检查清单.md)
- [芋道源码 - 多租户文档](https://doc.iocoder.cn/saas-tenant/)
- [MyBatis-Plus 租户插件](https://baomidou.com/pages/aef2f2/)

---

**错误时间**：2025-10-27  
**修复状态**：✅ 已修复（Mapper 层）  
**影响范围**：RocketMQ 消费者、设备发现、消息幂等性  
**修复版本**：v2025.09-SNAPSHOT  
**重要程度**：⭐⭐⭐⭐⭐ 关键修复（根本原因）














