# NVR 视频流播放实现指南

## 功能需求

点击左侧 NVR 通道列表中的某个通道，在右侧视频卡片中播放该通道的视频流。

## 技术方案

### 1. 后端实现

#### 扩展通道响应数据
```java
// NvrChannelRespVO 新增字段
private String streamUrl;       // 主码流地址
private String subStreamUrl;    // 子码流地址  
private String snapshotUrl;     // 快照地址
```

#### 视频流地址生成
```java
// 大华NVR RTSP流地址格式
// 主码流：rtsp://admin:password@192.168.1.200:554/cam/realmonitor?channel=1&subtype=0
// 子码流：rtsp://admin:password@192.168.1.200:554/cam/realmonitor?channel=1&subtype=1

private String generateStreamUrl(String nvrIp, Integer channelNo, String streamType) {
    int subtype = "sub".equals(streamType) ? 1 : 0;
    int channel = channelNo + 1; // 大华通道从1开始
    
    return String.format("rtsp://admin:admin123@%s:554/cam/realmonitor?channel=%d&subtype=%d", 
                       nvrIp, channel, subtype);
}
```

#### 快照地址生成
```java
// 大华NVR HTTP快照地址
// http://admin:password@192.168.1.200/cgi-bin/snapshot.cgi?channel=1

private String generateSnapshotUrl(String nvrIp, Integer channelNo) {
    int channel = channelNo + 1;
    return String.format("http://admin:admin123@%s/cgi-bin/snapshot.cgi?channel=%d", 
                       nvrIp, channel);
}
```

### 2. API 响应示例

```json
{
  "code": 0,
  "data": [
    {
      "id": 70000,
      "name": "通道 1",
      "state": 1,
      "ip": "192.168.1.200",
      "channelNo": 0,
      "port": 37777,
      "deviceType": "IPC",
      "protocol": "ONVIF",
      "manufacturer": "Dahua",
      "streamUrl": "rtsp://admin:admin123@192.168.1.200:554/cam/realmonitor?channel=1&subtype=0",
      "subStreamUrl": "rtsp://admin:admin123@192.168.1.200:554/cam/realmonitor?channel=1&subtype=1",
      "snapshotUrl": "http://admin:admin123@192.168.1.200/cgi-bin/snapshot.cgi?channel=1"
    }
  ]
}
```

### 3. 前端实现

#### 修改通道点击事件
```javascript
// 在 MultiScreenPreview/index.vue 中
const handleChannelClick = (channel) => {
  // 设置选中的视频屏幕
  if (selectedScreen !== null) {
    videoScreens[selectedScreen].camera = {
      id: channel.id,
      name: channel.name,
      streamUrl: channel.streamUrl,        // 主码流
      subStreamUrl: channel.subStreamUrl,  // 子码流
      snapshotUrl: channel.snapshotUrl,    // 快照
      channelNo: channel.channelNo,
      state: channel.state
    }
  }
}
```

#### 视频播放组件
```vue
<template>
  <div v-if="screen.camera" class="video-container">
    <!-- 视频播放区域 -->
    <div class="video-player">
      <!-- 优先使用子码流（带宽较小） -->
      <video
        v-if="screen.camera.subStreamUrl"
        :poster="screen.camera.snapshotUrl"
        controls
        autoplay
        muted
        style="width: 100%; height: 100%; object-fit: cover;"
      >
        <source :src="screen.camera.subStreamUrl" type="application/x-rtsp" />
        <!-- 备用主码流 -->
        <source :src="screen.camera.streamUrl" type="application/x-rtsp" />
        您的浏览器不支持视频播放
      </video>
      
      <!-- 如果视频加载失败，显示快照 -->
      <img 
        v-else-if="screen.camera.snapshotUrl"
        :src="screen.camera.snapshotUrl"
        style="width: 100%; height: 100%; object-fit: cover;"
        @error="handleImageError"
      />
      
      <!-- 通道信息显示 -->
      <div class="channel-info">
        <span>{{ screen.camera.name }}</span>
        <span class="channel-status" :class="{ online: screen.camera.state === 1 }">
          {{ screen.camera.state === 1 ? '在线' : '离线' }}
        </span>
      </div>
    </div>
  </div>
</template>
```

### 4. 浏览器兼容性处理

#### RTSP 流播放方案

**方案1：WebRTC转换**
```javascript
// 使用 WebRTC 将 RTSP 转换为浏览器支持的格式
// 需要部署 WebRTC 网关服务
const playWebRTC = (rtspUrl) => {
  const webrtcUrl = `ws://your-webrtc-gateway/stream?url=${encodeURIComponent(rtspUrl)}`
  // 使用 WebRTC 播放
}
```

**方案2：HLS转换**
```javascript
// 使用 FFmpeg 将 RTSP 转换为 HLS
// 后端提供 HLS 流地址
const playHLS = (hlsUrl) => {
  if (Hls.isSupported()) {
    const hls = new Hls()
    hls.loadSource(hlsUrl)
    hls.attachMedia(video)
  }
}
```

**方案3：快照轮询**
```javascript
// 如果无法播放视频流，使用快照轮询模拟视频
const startSnapshotPolling = (snapshotUrl) => {
  setInterval(() => {
    const img = new Image()
    img.src = snapshotUrl + '?t=' + Date.now()
    img.onload = () => {
      // 更新显示的图片
    }
  }, 1000) // 每秒更新一次
}
```

### 5. 部署配置

#### Nginx 代理配置
```nginx
# 代理 RTSP 流
location /rtsp-proxy/ {
    proxy_pass http://192.168.1.200:554/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}

# 代理快照请求
location /snapshot-proxy/ {
    proxy_pass http://192.168.1.200/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
```

### 6. 测试验证

#### 1. 测试通道API
```bash
curl -X GET "http://192.168.1.38:8099/iot/video/nvr/70/channels?refresh=1"
```

#### 2. 验证流地址
```bash
# 测试RTSP流
ffplay rtsp://admin:admin123@192.168.1.200:554/cam/realmonitor?channel=1&subtype=0

# 测试快照
curl "http://admin:admin123@192.168.1.200/cgi-bin/snapshot.cgi?channel=1" -o snapshot.jpg
```

#### 3. 前端功能测试
1. 点击左侧通道列表
2. 检查右侧视频卡片是否显示
3. 验证视频流是否正常播放
4. 测试快照是否正常显示

## 注意事项

1. **认证信息**：实际部署时需要使用正确的用户名密码
2. **网络访问**：确保前端能访问 NVR 设备的网络
3. **浏览器限制**：现代浏览器不直接支持 RTSP，需要转换
4. **性能优化**：优先使用子码流减少带宽占用
5. **错误处理**：添加流播放失败的降级方案

## 推荐实现步骤

1. **第一步**：实现快照显示功能
2. **第二步**：部署 WebRTC 或 HLS 转换服务
3. **第三步**：实现视频流播放功能
4. **第四步**：添加播放控制和错误处理
