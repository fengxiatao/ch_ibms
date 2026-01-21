# 大华无插件播放SDK集成方案

## 发现的资源

您找到的 `F:\work\ch_ibms\dh_test\NVR\webs` 是大华官方提供的无插件播放SDK，这是最佳的视频播放解决方案！

## 🎯 集成方案

### 1. SDK文件结构分析

```
F:\work\ch_ibms\dh_test\NVR\webs\
├── index.js                 // 主要逻辑文件
├── module/
│   ├── PlayerControl.js     // 播放控制
│   ├── videoWorker.worker.js // 视频解码Worker
│   └── ...
├── css/                     // 样式文件
└── libs/                    // 依赖库
```

### 2. 核心功能特性

从代码分析看，该SDK支持：
- ✅ **无插件播放**：基于WebAssembly技术
- ✅ **多窗口播放**：支持宫格布局
- ✅ **实时预览**：RTSP流播放
- ✅ **录像回放**：历史视频播放
- ✅ **通道管理**：真实通道名称显示
- ✅ **PTZ控制**：云台控制功能

### 3. 集成到您的项目

#### 步骤1：复制SDK文件
```bash
# 将大华SDK复制到前端项目
cp -r F:\work\ch_ibms\dh_test\NVR\webs\* f:\work\ch_ibms\yudao-ui-admin-vue3\public\dahua-sdk\
```

#### 步骤2：在Vue组件中引入
```vue
<!-- MultiScreenPreview/index.vue -->
<template>
  <div class="video-surveillance">
    <!-- 左侧通道列表 -->
    <div class="left-panel">
      <div class="channel-list">
        <div 
          v-for="channel in channels" 
          :key="channel.id"
          class="channel-item"
          @click="playChannel(channel)"
        >
          <span>{{ channel.name }}</span>  <!-- 显示真实通道名称 -->
          <span class="channel-status" :class="{ online: channel.state === 1 }">
            {{ channel.state === 1 ? '在线' : '离线' }}
          </span>
        </div>
      </div>
    </div>
    
    <!-- 右侧视频播放区域 -->
    <div class="right-panel">
      <!-- 大华SDK播放容器 -->
      <div id="dahua-player-container" class="video-grid">
        <div 
          v-for="(screen, index) in videoScreens" 
          :key="index"
          :id="`h5_video_wrap_${index}`"
          class="video-screen"
          :class="{ active: selectedScreen === index }"
          @click="selectScreen(index)"
        >
          <canvas :id="`h5_canvas_${index}`" class="video-canvas"></canvas>
          <video :id="`h5_video_${index}`" class="video-element" style="display: none;"></video>
          <div :id="`h5_loading_${index}`" class="video-loading">加载中...</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

// 引入大华SDK
let DahuaPlayer = null

onMounted(async () => {
  // 动态加载大华SDK
  await loadDahuaSDK()
  initDahuaPlayer()
})

const loadDahuaSDK = () => {
  return new Promise((resolve) => {
    const script = document.createElement('script')
    script.src = '/dahua-sdk/index.js'
    script.onload = () => {
      // SDK加载完成后，DahuaPlayer应该可用
      resolve()
    }
    document.head.appendChild(script)
  })
}

const initDahuaPlayer = () => {
  // 初始化大华播放器
  // 参考 F:\work\ch_ibms\dh_test\NVR\webs\index.js 的初始化逻辑
}

const playChannel = (channel) => {
  if (selectedScreen.value !== null) {
    // 使用大华SDK播放指定通道
    startPlay(channel.channelNo, selectedScreen.value)
  }
}

const startPlay = (channelNo, screenIndex) => {
  // 调用大华SDK的播放方法
  // 参考原始代码中的播放逻辑
}
</script>
```

### 4. 修改后端通道名称逻辑

我已经修复了通道名称显示问题：

```java
// 使用SDK返回的真实通道名称
String name = m.get("name") != null ? String.valueOf(m.get("name")) : ("通道" + (channelNo + 1));
```

现在API会返回：
```json
{
  "name": "前门摄像头",  // 真实通道名称
  "channelNo": 0
}
```

而不是：
```json
{
  "name": "通道1",      // 默认名称
  "channelNo": 0  
}
```

### 5. 大华SDK播放器集成示例

基于您提供的SDK代码，核心播放逻辑：

```javascript
// 参考 F:\work\ch_ibms\dh_test\NVR\webs\index.js

// 1. 初始化播放器
const initPlayer = () => {
  // 设置视频DOM
  $canvas = document.getElementById(`h5_canvas_${WndIndex}`)
  $video = document.getElementById(`h5_video_${WndIndex}`)
  $videoLoading = document.getElementById(`h5_loading_${WndIndex}`)
}

// 2. 开始播放
const startPlay = (channelNo, screenIndex) => {
  // 设置当前窗口
  WndIndex = screenIndex
  setVideoDom()
  
  // 调用大华SDK播放方法
  if (playerInstance[WndIndex]) {
    playerInstance[WndIndex].startPlay({
      channel: channelNo,
      // 其他播放参数...
    })
  }
}

// 3. 停止播放
const stopPlay = (screenIndex) => {
  if (playerInstance[screenIndex]) {
    playerInstance[screenIndex].stopPlay()
  }
}
```

### 6. 完整集成步骤

#### 第一步：准备SDK文件
1. 复制 `F:\work\ch_ibms\dh_test\NVR\webs` 到前端项目
2. 确保所有依赖文件完整

#### 第二步：修改Vue组件
1. 引入大华SDK脚本
2. 初始化播放器实例
3. 实现通道点击播放逻辑

#### 第三步：测试验证
1. 测试通道列表显示真实名称
2. 测试点击通道播放视频
3. 验证多窗口播放功能

### 7. 优势对比

| 方案 | 优势 | 劣势 |
|------|------|------|
| **大华官方SDK** | ✅ 无插件<br>✅ 性能优秀<br>✅ 功能完整<br>✅ 官方支持 | ❌ 仅支持大华设备 |
| **WebRTC转换** | ✅ 通用性好<br>✅ 实时性强 | ❌ 需要额外服务<br>❌ 部署复杂 |
| **快照轮询** | ✅ 简单易实现<br>✅ 兼容性好 | ❌ 不是真正视频<br>❌ 体验较差 |

### 8. 推荐实施方案

**强烈推荐使用大华官方SDK！** 因为：

1. **您已经有现成的SDK** - `F:\work\ch_ibms\dh_test\NVR\webs`
2. **无插件播放** - 基于WebAssembly，浏览器原生支持
3. **功能完整** - 支持实时预览、录像回放、PTZ控制等
4. **性能优秀** - 官方优化，播放流畅
5. **真实通道名称** - 已修复后端返回真实名称

### 9. 下一步行动

1. **立即可做**：测试修复后的通道名称显示
2. **短期目标**：集成大华SDK到Vue组件
3. **长期规划**：完善播放控制和用户体验

您的发现非常有价值！使用官方SDK是最佳选择。
