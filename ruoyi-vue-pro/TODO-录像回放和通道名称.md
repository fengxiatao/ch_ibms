# 待实现功能清单

## 1. 录像回放功能 - 使用大华 SDK 搜索录像

### 当前状态
- ✅ 前端页面已完成（VideoPlayback/index.vue）
- ❌ 后端接口未实现

### 需要实现的功能

#### 1.1 后端接口 - 录像搜索
**位置**: `yudao-module-iot-gateway`

创建录像搜索服务：
```java
// DahuaNvrService.java 中添加方法

/**
 * 搜索录像文件
 * 
 * @param ip NVR IP地址
 * @param port NVR端口
 * @param username 用户名
 * @param password 密码
 * @param channelNo 通道号
 * @param startTime 开始时间
 * @param endTime 结束时间
 * @param recordType 录像类型（0=所有, 1=定时, 2=移动侦测, 3=报警）
 * @return 录像文件列表
 */
public List<RecordFileInfo> searchRecordFiles(
    String ip, Integer port, String username, String password,
    int channelNo, LocalDateTime startTime, LocalDateTime endTime, int recordType) {
    
    // 1. 登录 NVR
    NvrLoginResult loginResult = loginNvr(ip, port, username, password);
    
    // 2. 调用 SDK 搜索录像
    // CLIENT_FindFile / CLIENT_FindNextFile
    
    // 3. 返回录像列表
}
```

#### 1.2 录像回放
使用大华 RTSP 录像回放 URL 格式：
```
rtsp://username:password@ip:port/cam/playback?channel=1&starttime=2024-11-24_00:00:00&endtime=2024-11-24_23:59:59
```

#### 1.3 前端调用
前端已有 `loadRecordingData()` 方法，需要对接后端接口：
```typescript
// VideoPlayback/index.vue
const loadRecordingData = async () => {
  const res = await CameraRecordingApi.getRecordingPage({
    deviceId: selectedDevice.value,
    channelNo: selectedChannel.value,
    startTime: timeRange.value[0],
    endTime: timeRange.value[1],
    recordType: selectedRecordingType.value
  })
  recordingList.value = res.list
}
```

---

## 2. 通道名称显示问题

### 问题描述
- **当前显示**: 设备序列号（如 "BF0EFF4PANCD9"）
- **期望显示**: 用户友好名称（如 "IPC2", "IPC3"）

### 根本原因
大华 SDK 返回的通道名称 (`szName`) 可能是：
1. 空字符串
2. 设备序列号
3. 用户自定义名称

### 解决方案

#### 方案 1：使用通道配置名称（推荐）
修改 `DahuaNvrService.java` 第 330-347 行：

```java
// 当前代码
if (chName.isEmpty() && chId >= 0) {
    // 尝试获取通道配置名称
    NetSDKLib.AV_CFG_ChannelName title = new NetSDKLib.AV_CFG_ChannelName();
    boolean nameOk = ToolKits.GetDevConfig(loginResult.getLoginHandle(), chId, NetSDKLib.CFG_CMD_CHANNELTITLE, title);
    if (nameOk) {
        chName = new String(title.szName).trim();
    }
}

// 修改为：始终尝试获取配置名称
if (chId >= 0) {
    try {
        NetSDKLib.AV_CFG_ChannelName title = new NetSDKLib.AV_CFG_ChannelName();
        boolean nameOk = ToolKits.GetDevConfig(loginResult.getLoginHandle(), chId, NetSDKLib.CFG_CMD_CHANNELTITLE, title);
        if (nameOk) {
            String configName = new String(title.szName).trim();
            if (!configName.isEmpty() && !configName.equals(chSn)) {
                // 如果配置名称不为空且不是序列号，使用配置名称
                chName = configName;
            }
        }
    } catch (Exception ignore) {}
}

// 最终名称设置
String finalName;
if (chName.isEmpty() || chName.equals(chSn)) {
    // 如果名称为空或等于序列号，使用默认格式
    finalName = "IPC" + chId;  // 或 "通道" + chId
} else {
    finalName = chName;
}
channel.put("name", finalName);
```

#### 方案 2：允许用户修改通道名称
在通道管理界面添加"重命名"功能：
1. 用户可以手动修改通道名称
2. 保存到数据库 `iot_device_channel.channel_name`
3. 显示时优先使用用户自定义名称

---

## 3. 实施优先级

### 高优先级
1. ✅ 修复通道名称显示（立即修改）
2. ⏳ 实现录像搜索接口（本周完成）

### 中优先级
3. ⏳ 实现录像回放功能
4. ⏳ 添加通道重命名功能

### 低优先级
5. ⏳ 录像导出功能
6. ⏳ 录像上传功能

---

## 4. 相关文件

### 后端
- `yudao-module-iot-gateway/src/main/java/cn/iocoder/yudao/module/iot/gateway/service/DahuaNvrService.java`
- `yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/service/device/activation/IotDeviceActivationServiceImpl.java`

### 前端
- `yudao-ui-admin-vue3/src/views/security/VideoSurveillance/VideoPlayback/index.vue`
- `yudao-ui-admin-vue3/src/api/iot/channel/index.ts`

---

## 5. 参考资料

### 大华 SDK 文档
- 录像搜索：`CLIENT_FindFile` / `CLIENT_FindNextFile`
- 通道配置：`CLIENT_GetDevConfig` (CFG_CMD_CHANNELTITLE)
- 录像回放：RTSP playback URL

### RTSP 录像回放 URL 格式
```
# 大华
rtsp://username:password@ip:80/cam/playback?channel=1&starttime=2024-11-24_00:00:00&endtime=2024-11-24_23:59:59

# 海康
rtsp://username:password@ip:554/Streaming/tracks/101?starttime=20241124t000000z&endtime=20241124t235959z
```
