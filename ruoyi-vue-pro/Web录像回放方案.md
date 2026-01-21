# Web 录像回放方案

## 问题分析

### ❌ CLIENT_PlayBackByTimeEx 不适合 Web 项目

您提到的 `CLIENT_PlayBackByTimeEx` API 是**桌面应用专用**的，不适合 Web 项目：

```java
public LLong CLIENT_PlayBackByTimeEx(
    LLong lLoginID,           // 登录句柄
    int nChannelID,           // 通道号
    NET_TIME lpStartTime,     // 开始时间
    NET_TIME lpStopTime,      // 结束时间
    Pointer hWnd,             // ❌ 窗口句柄（仅 Windows 有效）
    Callback cbDownLoadPos,   // ❌ 本地回调函数
    Pointer dwPosUser,
    Callback fDownLoadDataCallBack, // ❌ 本地数据回调
    Pointer dwDataUser
);
```

**不适合的原因**：

1. **需要窗口句柄 (hWnd)**：Web 浏览器中不存在 Windows 窗口句柄
2. **本地回调函数**：回调需要在服务器进程中处理，无法传递到浏览器
3. **返回本地句柄**：返回的是播放句柄，不是流媒体 URL
4. **架构不匹配**：这是 C/S 架构的 API，不是 B/S 架构

## ✅ Web 项目的正确方案

### 方案 1: RTSP 回放 URL（推荐）

大华设备支持通过 RTSP URL 直接回放录像，这是 Web 项目的标准方案。

#### RTSP 回放 URL 格式

**按时间回放**：
```
rtsp://username:password@ip:port/playback?channel=通道号&starttime=开始时间&endtime=结束时间
```

**按文件回放**：
```
rtsp://username:password@ip:port/playback?file=文件名
```

#### 示例

```
# 按时间回放（2025-11-24 08:00:00 ~ 10:00:00，通道1）
rtsp://admin:admin123@192.168.1.108:554/playback?channel=1&starttime=20251124T080000&endtime=20251124T100000

# 按文件回放
rtsp://admin:admin123@192.168.1.108:554/playback?file=/mnt/sd/2025-11-24/record_001.mp4
```

#### 后端实现

已在 `DahuaNvrService.java` 中添加了生成回放 URL 的方法：

```java
/**
 * 生成录像回放 URL（按时间）
 */
public String generatePlaybackUrl(
        String ip, Integer rtspPort, String username, String password,
        int channelNo, String startTime, String endTime) {
    
    int port = rtspPort != null ? rtspPort : 554;
    int channel = channelNo + 1;  // 大华通道从1开始
    
    // 时间格式转换：yyyy-MM-dd HH:mm:ss -> yyyyMMddTHHmmss
    String start = startTime.replaceAll("[- :]", "").replace(" ", "T");
    String end = endTime.replaceAll("[- :]", "").replace(" ", "T");
    
    return String.format(
            "rtsp://%s:%s@%s:%d/playback?channel=%d&starttime=%s&endtime=%s",
            username, password, ip, port, channel, start, end
    );
}

/**
 * 生成录像回放 URL（按文件）
 */
public String generateFilePlaybackUrl(
        String ip, Integer rtspPort, String username, String password,
        String fileName) {
    
    int port = rtspPort != null ? rtspPort : 554;
    
    return String.format(
            "rtsp://%s:%s@%s:%d/playback?file=%s",
            username, password, ip, port, fileName
    );
}
```

#### 前端播放器

使用大华 PlayerControl 播放器（已在实时预览中使用）：

```typescript
// 播放录像
const playRecording = (record) => {
  // 1. 生成回放 URL
  const playbackUrl = generatePlaybackUrl(record)
  
  // 2. 创建播放器配置
  const config = {
    rtspURL: playbackUrl,
    wsURL: `ws://${device.ip}:80`,  // WebSocket 端口
    username: device.username,
    password: device.password,
    lessRateCanvas: true,
    wndIndex: 0,
    h265AccelerationEnabled: true
  }
  
  // 3. 创建播放器实例
  const player = new PlayerControl(config)
  
  // 4. 绑定事件
  player.on('WorkerReady', () => {
    player.connect()
  })
  
  player.on('PlayStart', () => {
    console.log('录像开始播放')
  })
  
  player.on('Error', (error) => {
    console.error('播放错误:', error)
  })
  
  // 5. 初始化播放器
  player.init(canvasEl, videoEl)
}
```

### 方案 2: HTTP-FLV 流（备选）

如果 RTSP 有问题，可以考虑将 RTSP 转换为 HTTP-FLV：

```
服务器端：RTSP → FFmpeg → HTTP-FLV
浏览器端：HTTP-FLV → flv.js → Video 元素
```

#### 优点
- 浏览器原生支持（通过 flv.js）
- 延迟较低
- 穿透防火墙能力强

#### 缺点
- 需要服务器端转码（消耗资源）
- 增加系统复杂度

### 方案 3: HLS 流（备选）

将录像转换为 HLS 格式：

```
服务器端：录像文件 → FFmpeg → HLS (m3u8 + ts)
浏览器端：HLS → hls.js → Video 元素
```

#### 优点
- 浏览器原生支持
- 适合长时间录像
- CDN 友好

#### 缺点
- 延迟较高（6-30秒）
- 需要服务器端转码

## 推荐实现流程

### 1. 搜索录像

```
前端 → Biz API → Gateway API → DahuaNvrService.searchRecordFiles()
                                    ↓
                              大华 SDK CLIENT_FindFile()
                                    ↓
                              返回录像列表
```

### 2. 生成回放 URL

```
前端 → Biz API → DahuaNvrService.generatePlaybackUrl()
                        ↓
                  返回 RTSP URL
```

### 3. 播放录像

```
前端 → PlayerControl.init()
         ↓
     连接 RTSP 流
         ↓
     播放录像
```

## 完整示例

### 后端接口（Biz 层）

```java
@PostMapping("/playback/url")
@Operation(summary = "获取录像回放 URL")
public CommonResult<String> getPlaybackUrl(@Valid @RequestBody PlaybackUrlReqVO reqVO) {
    // 1. 查询通道信息
    IotDeviceChannelDO channel = channelService.getChannel(reqVO.getChannelId());
    IotDeviceDO device = deviceService.getDevice(channel.getDeviceId());
    
    // 2. 调用 Gateway 生成回放 URL
    String playbackUrl = gatewayDeviceClient.generatePlaybackUrl(
        device.getIp(),
        device.getRtspPort(),
        device.getUsername(),
        device.getPassword(),
        channel.getChannelNo(),
        reqVO.getStartTime(),
        reqVO.getEndTime()
    );
    
    return success(playbackUrl);
}
```

### 前端调用

```typescript
// 1. 搜索录像
const searchRecords = async () => {
  const response = await searchRecordsApi({
    channelId: selectedChannel.value.id,
    startTime: '2025-11-24 08:00:00',
    endTime: '2025-11-24 10:00:00',
    recordType: 0
  })
  
  recordingList.value = response.records
}

// 2. 播放录像
const playRecording = async (record) => {
  // 获取回放 URL
  const playbackUrl = await getPlaybackUrlApi({
    channelId: record.channelId,
    startTime: record.startTime,
    endTime: record.endTime
  })
  
  // 使用 PlayerControl 播放
  const player = new PlayerControl({
    rtspURL: playbackUrl,
    wsURL: `ws://${device.ip}:80`,
    username: device.username,
    password: device.password
  })
  
  player.on('WorkerReady', () => player.connect())
  player.init(canvasEl, videoEl)
}
```

## 总结

| 方案 | 适用场景 | 优点 | 缺点 |
|------|---------|------|------|
| **RTSP URL** | ✅ 推荐 | 简单、直接、低延迟 | 需要支持 RTSP 的播放器 |
| HTTP-FLV | 备选 | 浏览器友好、低延迟 | 需要服务器转码 |
| HLS | 长录像 | 浏览器原生支持 | 延迟高、需要转码 |
| CLIENT_PlayBackByTimeEx | ❌ 不适用 | - | 仅适用于桌面应用 |

**结论**：Web 项目应该使用 **RTSP 回放 URL + PlayerControl 播放器**，而不是 `CLIENT_PlayBackByTimeEx` API。
