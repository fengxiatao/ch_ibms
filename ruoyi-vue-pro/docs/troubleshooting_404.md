# 404错误排查指南

## 问题现象

访问新接口时返回404错误：
```
http://localhost:3001/admin-api/iot/alarm/host/1/partitions
{"code":404,"msg":"请求地址不存在:admin-api/iot/alarm/host/1/partitions","data":null}
```

## 原因分析

### 1. 后端服务未重启 ⭐（最可能）

新添加的接口需要重启后端服务才能生效。

**解决方法**：
```bash
# 停止后端服务
# 重新启动 yudao-module-iot-biz 服务
```

### 2. 接口路径配置错误

检查Controller的 `@RequestMapping` 和方法的 `@GetMapping` 路径是否正确。

**当前配置**：
```java
@RestController
@RequestMapping("/iot/alarm/host")
public class IotAlarmHostController {
    
    @GetMapping("/{id}/partitions")
    public CommonResult<List<IotAlarmPartitionRespVO>> getPartitionList(@PathVariable("id") Long id) {
        return success(partitionService.getPartitionListByHostId(id));
    }
}
```

**完整路径**：`/admin-api/iot/alarm/host/{id}/partitions`

### 3. 前端代理配置问题

检查 `vite.config.ts` 中的代理配置：

```typescript
proxy: {
  '/admin-api': {
    target: env.VITE_BASE_URL || 'http://127.0.0.1:48888',
    ws: false,
    changeOrigin: true,
  }
}
```

### 4. 后端服务端口不对

检查 `.env.dev` 配置：
```
VITE_BASE_URL='http://127.0.0.1:48888'
```

确保后端服务运行在48888端口。

## 排查步骤

### 步骤1：测试后端服务是否正常

访问已有接口：
```
http://localhost:3001/admin-api/iot/alarm/host/page?pageNo=1&pageSize=10
```

- ✅ 如果返回数据 → 后端服务正常，继续步骤2
- ❌ 如果404 → 后端服务未启动或端口错误

### 步骤2：检查新接口是否注册

查看后端启动日志，搜索：
```
Mapped "{[/iot/alarm/host/{id}/partitions]
```

- ✅ 如果找到 → 接口已注册，继续步骤3
- ❌ 如果没找到 → 代码未编译或服务未重启

### 步骤3：直接访问后端

绕过前端代理，直接访问后端：
```
http://127.0.0.1:48888/admin-api/iot/alarm/host/1/partitions
```

需要添加认证头：
```
Authorization: Bearer <your_token>
```

### 步骤4：检查数据库

确保数据库表已创建：
```sql
USE ch_ibms;
SHOW TABLES LIKE 'iot_alarm_partition';
SHOW TABLES LIKE 'iot_alarm_zone';
```

## 解决方案

### 方案1：重启后端服务（推荐）

1. 停止 `yudao-module-iot-biz` 服务
2. 清理编译缓存（可选）
3. 重新启动服务
4. 查看启动日志，确认接口已注册

### 方案2：检查代码编译

1. 确认代码已保存
2. IDE自动编译是否开启
3. 手动触发编译（Build → Rebuild Project）

### 方案3：检查依赖注入

确保Service已正确注入到Controller：

```java
@Resource
private IotAlarmPartitionService partitionService;

@Resource
private IotAlarmZoneMapper zoneMapper;
```

### 方案4：检查权限配置

虽然404通常不是权限问题，但也要确认：

```java
@PreAuthorize("@ss.hasPermission('iot:alarm-host:query')")
```

## 验证方法

### 1. 使用Postman测试

```
GET http://127.0.0.1:48888/admin-api/iot/alarm/host/1/partitions
Headers:
  Authorization: Bearer <token>
  tenant-id: 1
```

### 2. 使用curl测试

```bash
curl -X GET "http://127.0.0.1:48888/admin-api/iot/alarm/host/1/partitions" \
  -H "Authorization: Bearer <token>" \
  -H "tenant-id: 1"
```

### 3. 查看Swagger文档

访问：
```
http://127.0.0.1:48888/doc.html
```

搜索 "报警主机" 或 "partitions"，查看接口是否已注册。

## 常见错误信息

### 错误1：404 Not Found
```json
{"code":404,"msg":"请求地址不存在:admin-api/iot/alarm/host/1/partitions","data":null}
```
**原因**：接口未注册或路径错误
**解决**：重启后端服务

### 错误2：500 Internal Server Error
```json
{"code":500,"msg":"系统异常","data":null}
```
**原因**：代码逻辑错误或数据库问题
**解决**：查看后端日志

### 错误3：403 Forbidden
```json
{"code":403,"msg":"没有权限","data":null}
```
**原因**：权限不足
**解决**：检查权限配置和角色分配

## 检查清单

- [ ] 后端服务已启动
- [ ] 后端服务运行在48888端口
- [ ] 代码已编译
- [ ] 接口已在Swagger中显示
- [ ] 数据库表已创建
- [ ] 前端代理配置正确
- [ ] 前端开发服务器已重启
- [ ] 浏览器缓存已清除

## 下一步

如果以上方法都无法解决，请提供：

1. 后端启动日志
2. 前端控制台错误信息
3. 网络请求详情（F12 → Network）
4. Swagger文档截图

这样可以更准确地定位问题。
