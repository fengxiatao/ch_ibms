# NVR通道同步测试指南

## 📋 测试目标

验证NVR通道同步功能能够正确识别并设置 `ptz_support` 字段。

---

## 🔧 准备工作

### 1. 清空现有通道数据（可选）

```sql
-- 删除NVR 70的所有通道
DELETE FROM iot_device_channel WHERE device_id = 70;

-- 验证已删除
SELECT COUNT(*) FROM iot_device_channel WHERE device_id = 70;
-- 应该返回 0
```

### 2. 重启后端服务

确保新的同步逻辑生效。

---

## 🧪 测试步骤

### 步骤1：触发同步

在前端多屏预览页面，点击NVR左侧的"刷新通道"按钮。

### 步骤2：查看后端日志

查找以下关键日志：

```
[通道同步] SDK返回的config: nvrId=70, channelNo=3, config={...}
[通道同步] ✅ 从SDK获取ptzSupport: channelNo=3, ptzObj=true, ptzSupport=true
[通道同步] 最终ptzSupport值: channelNo=3, ptzSupport=true
[通道同步] 新增通道: nvrId=70, channelNo=3, name=BF0EFF4PANCD599
```

**期望结果**：
- 如果SDK返回了 `ptzSupport=true`，日志应该显示"从SDK获取ptzSupport"
- 如果SDK返回了 `ptzSupport=null`，日志应该显示"通过deviceType推断"或"通过设备名称推断"

### 步骤3：验证数据库

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
- 通道3的 `ptz_support` 应该是 `1`
- 其他球机通道的 `ptz_support` 也应该是 `1`

### 步骤4：验证前端显示

1. 在多屏预览页面，将通道3拖拽到视频格子
2. 点击该格子，选中通道
3. 查看右侧是否显示"云台控制"面板

**期望结果**：
- 应该显示云台控制面板
- 面板中应该有方向控制按钮

---

## 🐛 问题排查

### 问题1：SDK返回 `ptzSupport=null`

**日志特征**：
```
[通道同步] SDK返回ptzSupport=null，尝试其他方式: channelNo=3
[通道同步] 通过deviceType推断ptzSupport: channelNo=3, deviceType=IPC, ptzSupport=false
```

**原因**：SDK没有返回云台支持信息

**解决方案**：
1. 检查SDK网关是否正确实现了 `ptzSupport` 字段
2. 或者在SDK网关中根据设备型号返回 `ptzSupport`

### 问题2：deviceType也无法推断

**日志特征**：
```
[通道同步] 通过设备名称推断ptzSupport: channelNo=3, deviceName=BF0EFF4PANCD599, ptzSupport=false
[通道同步] 最终ptzSupport值: channelNo=3, ptzSupport=false
```

**原因**：设备名称中没有"PTZ"、"DOME"、"球机"等关键字

**临时解决方案**：
```sql
-- 手动设置球机通道
UPDATE iot_device_channel
SET ptz_support = 1
WHERE device_id = 70 
  AND channel_no IN (3, 5);  -- 替换为实际的球机通道号
```

**长期解决方案**：
1. 在SDK网关中实现 `ptzSupport` 字段
2. 或者维护一个球机IP列表，通过IP判断

---

## 📊 同步逻辑优先级

```
1. SDK直接返回 ptzSupport ✅ 最准确
    ↓ (如果为null)
2. 通过 deviceType 推断 ⚠️ 较准确
    ↓ (如果无法推断)
3. 通过设备名称推断 ⚠️ 不太准确
    ↓ (如果都失败)
4. 默认值 false ❌ 需要手动修复
```

---

## ✅ 成功标准

- [x] 后端日志显示正确识别了 `ptzSupport`
- [x] 数据库中 `ptz_support = 1`
- [x] 前端显示云台控制面板
- [x] 云台控制按钮可以正常工作

---

## 📝 测试记录

### 测试时间：____________________

### 测试结果：

| 通道号 | 通道名称 | SDK返回ptzSupport | 数据库ptz_support | 前端显示 | 备注 |
|--------|---------|-------------------|-------------------|----------|------|
| 0      |         |                   |                   |          |      |
| 1      |         |                   |                   |          |      |
| 2      |         |                   |                   |          |      |
| 3      |         |                   |                   |          |      |
| 4      |         |                   |                   |          |      |
| 5      |         |                   |                   |          |      |

### 问题记录：

1. _______________________________________________
2. _______________________________________________
3. _______________________________________________

### 解决方案：

1. _______________________________________________
2. _______________________________________________
3. _______________________________________________
