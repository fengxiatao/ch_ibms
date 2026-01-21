# PTZ支持字段问题排查指南

## 📋 问题现象

SDK可以获取到通道支持PTZ，但数据库中 `ptz_support = 0`，前端不显示云台控制面板。

---

## 🔍 数据流分析

```
1. SDK网关层 (DahuaNvrService)
   ↓
   CLIENT_QueryDevInfo 获取 ptzSet = [3, 5]  ✅
   ↓
   CLIENT_GetChannelInfo 获取通道列表
   ↓
   根据 ptzSet 设置每个通道的 ptzSupport
   ↓
   返回 channel.put("ptzSupport", true/false)

2. 业务服务层 (NvrQueryServiceImpl)
   ↓
   接收SDK返回的通道列表
   ↓
   放入 config: {"ptzSupport": true}  ✅
   ↓
   返回 IotDeviceDO.config

3. 通道同步层 (IotDeviceChannelServiceImpl)
   ↓
   解析 config.ptzSupport  ✅
   ↓
   保存到数据库 ptz_support=1

4. 前端展示层
   ↓
   获取通道 ptzSupport=true
   ↓
   设置 canPtz=true
   ↓
   显示云台控制面板 ✅
```

---

## 🐛 可能的问题点

### 问题1：SDK索引匹配错误

**现象**：
- SDK返回 `ptzSet = [3, 5]`（1-based）
- 但通道ID是 `[2, 4]`（0-based）
- 导致匹配失败，`ptzSupport = false`

**排查方法**：
查看SDK网关日志：
```
[DahuaSDK] 获取PTZ支持通道: 2 个
[DahuaSDK] PTZ索引匹配: ptzSet=[3, 5], channelIds=[0, 1, 2, 3, 4, 5], useOneBased=true
[DahuaSDK] 通道BF0EFF4PANCD599云台支持: channelId=2, ptzSupport=true  ← 检查这里
```

**解决方案**：
已在代码中实现自动检测0-based/1-based（第370行）。

---

### 问题2：SDK未返回PTZ信息

**现象**：
- `gotPtzMap = false`
- 所有通道的 `ptzSupport = false`

**排查方法**：
查看SDK网关日志：
```
[DahuaSDK] CLIENT_QueryDevInfo(NET_QUERY_VIDEOCHANNELSINFO) 失败: xxx
```

**解决方案**：
1. 检查SDK版本是否支持 `NET_QUERY_VIDEOCHANNELSINFO`
2. 检查NVR固件版本
3. 使用备选方案：通过设备类型或名称推断

---

### 问题3：业务侧未正确解析

**现象**：
- SDK返回了 `ptzSupport=true`
- 但数据库中 `ptz_support=0`

**排查方法**：
查看业务侧日志：
```
[通道同步] SDK返回的config: nvrId=70, channelNo=3, config={"ptzSupport":true,...}
[通道同步] ✅ 从SDK获取ptzSupport: channelNo=3, ptzObj=true, ptzSupport=true
[通道同步] 最终ptzSupport值: channelNo=3, ptzSupport=true
```

**解决方案**：
已优化解析逻辑，支持Boolean和String类型。

---

## 🧪 完整测试流程

### 步骤1：清空数据

```sql
DELETE FROM iot_device_channel WHERE device_id = 70;
```

### 步骤2：重启服务

```bash
# 重启SDK网关
cd yudao-module-iot-gateway
mvn spring-boot:run

# 重启业务服务
cd yudao-server
mvn spring-boot:run
```

### 步骤3：触发同步

在前端点击"刷新通道"按钮。

### 步骤4：查看SDK网关日志

```
[DahuaSDK] 获取PTZ支持通道: 2 个
[DahuaSDK] PTZ索引匹配: ptzSet=[3, 5], channelIds=[0, 1, 2, 3, 4, 5], useOneBased=true
[DahuaSDK] 通道BF0EFF4PANCD599云台支持: channelId=2, ptzSupport=true
[DahuaSDK] 通道7J08E65PAGD63E7云台支持: channelId=4, ptzSupport=true
```

**期望结果**：
- `ptzSet` 不为空
- `ptzSupport=true` 的通道数量正确

### 步骤5：查看业务服务日志

```
[NVR] 网关返回通道数据: [{channelNo=0, name=xxx, ptzSupport=false}, {channelNo=2, name=BF0EFF4PANCD599, ptzSupport=true}, ...]
[通道同步] SDK返回的config: nvrId=70, channelNo=2, config={"channel":2,"ptzSupport":true,...}
[通道同步] ✅ 从SDK获取ptzSupport: channelNo=2, ptzObj=true, ptzSupport=true
[通道同步] 最终ptzSupport值: channelNo=2, ptzSupport=true
[通道同步] 新增通道: nvrId=70, channelNo=2, name=BF0EFF4PANCD599
```

**期望结果**：
- `config` 中包含 `ptzSupport=true`
- 解析成功
- 保存到数据库

### 步骤6：验证数据库

```sql
SELECT 
    channel_no,
    channel_name,
    target_ip,
    ptz_support,
    CASE 
        WHEN ptz_support = 1 THEN '✅ 支持云台'
        ELSE '❌ 不支持云台'
    END as ptz_status
FROM iot_device_channel
WHERE device_id = 70
ORDER BY channel_no;
```

**期望结果**：
```
channel_no | channel_name      | ptz_support | ptz_status
-----------|-------------------|-------------|------------
0          | 7A02240PAGD877D   | 0           | ❌ 不支持云台
1          | 9G0C4F4PAGD23C9   | 0           | ❌ 不支持云台
2          | BF0EFF4PANCD599   | 1           | ✅ 支持云台  ← 通道3（1-based）
3          | 7J08E65PAGD63E7   | 0           | ❌ 不支持云台
4          | BF0EFF4PANCD599   | 1           | ✅ 支持云台  ← 通道5（1-based）
```

### 步骤7：验证前端

1. 拖拽通道2到视频格子
2. 点击该格子
3. 查看右侧是否显示"云台控制"面板

**期望结果**：
- 显示云台控制面板
- 方向按钮可用

---

## 🔧 快速修复方案

### 方案1：手动修复数据库（临时）

```sql
-- 根据通道号手动设置
UPDATE iot_device_channel
SET ptz_support = 1
WHERE device_id = 70 
  AND channel_no IN (2, 4);  -- 0-based通道号
```

### 方案2：重新同步（推荐）

1. 删除现有通道
2. 重启服务
3. 点击"刷新通道"

---

## 📊 索引对应关系

| SDK ptzSet | 通道ID (0-based) | 通道号显示 (1-based) | 数据库channel_no |
|------------|------------------|---------------------|------------------|
| 3          | 2                | 通道3               | 2                |
| 5          | 4                | 通道5               | 4                |

**注意**：
- SDK返回的 `ptzSet` 通常是1-based（从1开始）
- 数据库的 `channel_no` 是0-based（从0开始）
- 前端显示的"通道X"是1-based（从1开始）

---

## ✅ 成功标准

- [x] SDK网关日志显示 `ptzSet` 不为空
- [x] SDK网关日志显示正确的 `ptzSupport=true`
- [x] 业务服务日志显示正确解析 `ptzSupport`
- [x] 数据库中 `ptz_support=1`
- [x] 前端显示云台控制面板
- [x] 云台控制功能正常

---

## 📝 常见错误

### 错误1：索引错位

```
[DahuaSDK] PTZ索引匹配: ptzSet=[3, 5], channelIds=[0, 1, 2, 3, 4, 5], useOneBased=false
[DahuaSDK] 通道BF0EFF4PANCD599云台支持: channelId=2, ptzSupport=false  ← 错误！
```

**原因**：`useOneBased=false` 导致匹配失败

**解决**：检查 `zeroMatches` 和 `oneMatches` 的计算逻辑

### 错误2：SDK未返回PTZ信息

```
[DahuaSDK] CLIENT_QueryDevInfo(NET_QUERY_VIDEOCHANNELSINFO) 失败: 0x20000000
```

**原因**：SDK版本或NVR固件不支持

**解决**：使用备选方案（设备类型推断）

### 错误3：config解析失败

```
[通道同步] SDK返回ptzSupport=null，尝试其他方式: channelNo=2
```

**原因**：SDK返回的 `ptzSupport` 为 `null`

**解决**：已实现三层备选方案（deviceType、deviceName）
