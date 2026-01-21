<template>
  <div class="improved-multi-screen-container">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <el-space>
        <el-select v-model="currentLayout" placeholder="选择布局" @change="handleLayoutChange">
          <el-option label="单屏 (1x1)" value="1x1" />
          <el-option label="4宫格 (2x2)" value="2x2" />
          <el-option label="9宫格 (3x3)" value="3x3" />
          <el-option label="16宫格 (4x4)" value="4x4" />
        </el-select>

        <el-button type="primary" :icon="VideoPlay" @click="showCameraDialog = true">
          选择摄像头
        </el-button>

        <el-button :icon="Refresh" @click="handleRefreshAll">刷新全部</el-button>

        <el-button :icon="FullScreen" @click="handleFullscreen">全屏</el-button>

        <span class="info-text">
          已选择 {{ activeCameras.length }} / {{ maxScreens }} 个摄像头
        </span>
      </el-space>
    </div>

    <!-- 多屏视频网格 -->
    <div 
      ref="containerRef" 
      class="video-grid-container" 
      :class="`layout-${currentLayout}`"
    >
      <div
        v-for="(screen, index) in videoScreens"
        :key="index"
        class="video-screen"
        :class="{ 
          empty: !screen.camera,
          playing: screen.isPlaying 
        }"
      >
        <!-- 有摄像头：显示播放器 -->
        <template v-if="screen.camera">
          <!-- JessibucaPlayer 播放器 -->
          <div
            v-if="screen.playUrl && screen.isPlaying"
            class="video-player"
          ></div>

          <!-- 快照（未播放时） -->
          <img
            v-if="!screen.isPlaying && screen.snapshot"
            :src="screen.snapshot"
            :alt="screen.camera.deviceName"
            class="camera-snapshot"
            @error="screen.snapshot = null"
          />

          <!-- 加载状态 -->
          <div v-if="screen.isLoading" class="loading-overlay">
            <el-icon class="loading-icon" :size="40">
              <Loading />
            </el-icon>
            <span>加载中...</span>
          </div>

          <!-- 摄像头信息 -->
          <div class="camera-info">
            <span class="camera-name">{{ screen.camera.deviceName || screen.camera.nickname }}</span>
            <span class="camera-location">{{ screen.camera.location || screen.camera.address }}</span>
          </div>

          <!-- 操作按钮 -->
          <div class="camera-controls">
            <el-button
              v-if="!screen.isPlaying"
              :icon="VideoPlay"
              circle
              type="primary"
              size="small"
              @click="playScreen(index)"
            />
            <el-button
              v-else
              :icon="VideoPause"
              circle
              type="warning"
              size="small"
              @click="stopScreen(index)"
            />
            <el-button
              :icon="Close"
              circle
              type="danger"
              size="small"
              @click="removeScreen(index)"
            />
          </div>
        </template>

        <!-- 空槽位 -->
        <div v-else class="empty-screen" @click="showCameraDialog = true">
          <el-icon :size="60" color="#666">
            <VideoCamera />
          </el-icon>
          <span class="empty-text">点击添加摄像头</span>
        </div>
      </div>
    </div>

    <!-- 摄像头选择对话框 -->
    <el-dialog
      v-model="showCameraDialog"
      title="选择摄像头"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-alert
        type="info"
        :closable="false"
        style="margin-bottom: 15px"
      >
        当前布局: {{ currentLayout }}, 最多可选择 {{ maxScreens }} 个摄像头
      </el-alert>

      <!-- 搜索栏 -->
      <el-input
        v-model="searchKeyword"
        placeholder="搜索摄像头名称或位置"
        :prefix-icon="Search"
        clearable
        style="margin-bottom: 15px"
      />

      <!-- 摄像头列表（带快照） -->
      <div class="camera-grid">
        <div
          v-for="camera in filteredCameras"
          :key="camera.id"
          class="camera-card"
          :class="{ 
            selected: isSelected(camera.id),
            disabled: !isSelectable(camera)
          }"
          @click="toggleSelectCamera(camera)"
        >
          <!-- 快照 -->
          <div class="camera-snapshot-preview">
            <img
              v-if="camera.snapshotUrl"
              :src="camera.snapshotUrl"
              :alt="camera.deviceName"
              @error="camera.snapshotUrl = null"
            />
            <div v-else class="no-snapshot">
              <el-icon :size="40">
                <VideoCamera />
              </el-icon>
              <span>无快照</span>
            </div>

            <!-- 选中标记 -->
            <div v-if="isSelected(camera.id)" class="selected-badge">
              <el-icon :size="20">
                <Select />
              </el-icon>
            </div>

            <!-- 状态标签 -->
            <el-tag
              :type="camera.state === 1 ? 'success' : 'danger'"
              size="small"
              class="status-tag"
            >
              {{ camera.state === 1 ? '在线' : '离线' }}
            </el-tag>
          </div>

          <!-- 摄像头信息 -->
          <div class="camera-card-info">
            <div class="camera-card-name">{{ camera.deviceName || camera.nickname }}</div>
            <div class="camera-card-location">{{ camera.location || camera.address || '未知位置' }}</div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="showCameraDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmSelection">
          确定 ({{ tempSelectedIds.length }}/{{ maxScreens }})
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  VideoPlay,
  VideoPause,
  Refresh,
  FullScreen,
  VideoCamera,
  Loading,
  Close,
  Search,
  Select
} from '@element-plus/icons-vue'
 
import * as DeviceApi from '@/api/iot/device/device'
import * as SecurityOverviewApi from '@/api/iot/security-overview'

// ==================== 类型定义 ====================

interface Camera {
  id: number
  deviceId: number
  deviceName: string
  nickname?: string
  location?: string
  address?: string
  state: number
  productId: number
  config?: string
  snapshotUrl?: string | null
}

interface VideoScreen {
  camera: Camera | null
  playUrl: string | null
  snapshot: string | null
  isPlaying: boolean
  isLoading: boolean
}

// ==================== 响应式数据 ====================

const currentLayout = ref('2x2')
const videoScreens = ref<VideoScreen[]>([])
const playerRefs = ref<any[]>([])

const availableCameras = ref<Camera[]>([])
const showCameraDialog = ref(false)
const searchKeyword = ref('')
const tempSelectedIds = ref<number[]>([])

const containerRef = ref<HTMLElement | null>(null)

// ==================== 计算属性 ====================

const maxScreens = computed(() => {
  const [rows, cols] = currentLayout.value.split('x').map(Number)
  return rows * cols
})

const activeCameras = computed(() => {
  return videoScreens.value.filter(screen => screen.camera !== null)
})

const filteredCameras = computed(() => {
  if (!searchKeyword.value) {
    return availableCameras.value
  }
  
  const keyword = searchKeyword.value.toLowerCase()
  return availableCameras.value.filter(camera => {
    const name = (camera.deviceName || camera.nickname || '').toLowerCase()
    const location = (camera.location || camera.address || '').toLowerCase()
    return name.includes(keyword) || location.includes(keyword)
  })
})

// ==================== 方法 ====================

/**
 * 判断设备是否为摄像机
 */
const isCameraDevice = (device: any): boolean => {
  try {
    const config = device.config ? JSON.parse(device.config) : {}
    const hasRtspPort = config.rtspPort !== undefined && config.rtspPort !== null
    const hasOnvifSupport = config.onvifSupported === true || config.onvifPort !== undefined
    const hasSnapshotConfig = config.snapshot !== undefined && config.snapshot !== null
    const hasVendor = config.vendor !== undefined && config.vendor !== null
    return hasRtspPort || hasOnvifSupport || hasSnapshotConfig || hasVendor
  } catch (error) {
    return false
  }
}

/**
 * 初始化视频屏幕
 */
const initVideoScreens = () => {
  const screens: VideoScreen[] = []
  for (let i = 0; i < maxScreens.value; i++) {
    screens.push({
      camera: null,
      playUrl: null,
      snapshot: null,
      isPlaying: false,
      isLoading: false
    })
  }
  videoScreens.value = screens
}

/**
 * 加载可用摄像头列表（带快照）
 */
const loadAvailableCameras = async () => {
  try {
    console.log('[多屏预览] 开始加载摄像头列表...')
    
    // 使用安防概览API（带快照）
    const res = await SecurityOverviewApi.getSecurityOverviewCameras({
      pageNo: 1,
      pageSize: 100,
      includeSnapshot: true,
      onlineOnly: false
    })

    if (res && res.list) {
      availableCameras.value = res.list.map((camera: any) => ({
        id: camera.id,
        deviceId: camera.id,
        deviceName: camera.deviceName,
        nickname: camera.nickname,
        location: camera.location,
        address: camera.location,
        state: camera.online ? 1 : 0,
        productId: 0,
        snapshotUrl: camera.snapshotUrl
      }))
      
      console.log(`[多屏预览] 成功加载 ${availableCameras.value.length} 个摄像头`)
    }

  } catch (error: any) {
    console.error('[多屏预览] 加载摄像头列表失败:', error)
    ElMessage.error('加载摄像头列表失败')
  }
}

/**
 * 是否已选中
 */
const isSelected = (cameraId: number): boolean => {
  return tempSelectedIds.value.includes(cameraId)
}

/**
 * 是否可选
 */
const isSelectable = (camera: Camera): boolean => {
  // 离线设备不可选
  if (camera.state !== 1) {
    return false
  }
  
  // 如果未选中且已达上限，不可选
  if (!isSelected(camera.id) && tempSelectedIds.value.length >= maxScreens.value) {
    return false
  }
  
  return true
}

/**
 * 切换选中状态
 */
const toggleSelectCamera = (camera: Camera) => {
  if (!isSelectable(camera) && !isSelected(camera.id)) {
    ElMessage.warning('该摄像头不可选')
    return
  }

  const index = tempSelectedIds.value.indexOf(camera.id)
  if (index > -1) {
    // 取消选中
    tempSelectedIds.value.splice(index, 1)
  } else {
    // 选中
    if (tempSelectedIds.value.length < maxScreens.value) {
      tempSelectedIds.value.push(camera.id)
    } else {
      ElMessage.warning(`最多只能选择 ${maxScreens.value} 个摄像头`)
    }
  }
}

/**
 * 确认选择
 */
const confirmSelection = () => {
  if (tempSelectedIds.value.length === 0) {
    ElMessage.warning('请至少选择一个摄像头')
    return
  }

  // 清空所有屏幕
  videoScreens.value.forEach(screen => {
    if (screen.isPlaying) {
      stopScreenInternal(screen)
    }
    screen.camera = null
    screen.playUrl = null
    screen.snapshot = null
  })

  // 填充选中的摄像头
  tempSelectedIds.value.forEach((cameraId, index) => {
    if (index < maxScreens.value) {
      const camera = availableCameras.value.find(c => c.id === cameraId)
      if (camera) {
        videoScreens.value[index].camera = camera
        videoScreens.value[index].snapshot = camera.snapshotUrl || null
      }
    }
  })

  showCameraDialog.value = false
  ElMessage.success(`已添加 ${tempSelectedIds.value.length} 个摄像头`)
}

/**
 * 播放指定屏幕
 */
const playScreen = async (index: number) => {
  const screen = videoScreens.value[index]
  if (!screen.camera) {
    ElMessage.warning('请先选择摄像头')
    return
  }

  if (screen.isPlaying) {
    return
  }

  try {
    screen.isLoading = true
    console.log(`[多屏预览] 开始播放屏幕 ${index + 1}:`, screen.camera.deviceName)

    // 获取播放地址
    const playUrlData = await SecurityOverviewApi.getPlayUrl(screen.camera.deviceId)

    // 优先使用 HTTP-FLV
    if (playUrlData.flvUrl) {
      screen.playUrl = playUrlData.flvUrl
      console.log(`[多屏预览] 使用 HTTP-FLV: ${playUrlData.flvUrl}`)
    } else if (playUrlData.wsFlvUrl) {
      screen.playUrl = playUrlData.wsFlvUrl
      console.log(`[多屏预览] 使用 WebSocket-FLV: ${playUrlData.wsFlvUrl}`)
    } else if (playUrlData.hlsUrl) {
      screen.playUrl = playUrlData.hlsUrl
      console.log(`[多屏预览] 使用 HLS: ${playUrlData.hlsUrl}`)
    } else {
      throw new Error('未获取到有效的播放地址')
    }

    // 等待 DOM 更新
    await new Promise(resolve => setTimeout(resolve, 100))

    screen.isPlaying = true
    screen.isLoading = false

    console.log(`[多屏预览] ✅ 屏幕 ${index + 1} 开始播放`)

  } catch (error: any) {
    console.error(`[多屏预览] 播放失败:`, error)
    ElMessage.error(`播放失败: ${error?.message || '未知错误'}`)
    screen.isLoading = false
    screen.isPlaying = false
  }
}

/**
 * 停止指定屏幕
 */
const stopScreen = (index: number) => {
  const screen = videoScreens.value[index]
  stopScreenInternal(screen)
  console.log(`[多屏预览] ⏸️ 屏幕 ${index + 1} 已停止`)
}

/**
 * 内部停止方法
 */
const stopScreenInternal = (screen: VideoScreen) => {
  screen.isPlaying = false
  screen.playUrl = null
}

/**
 * 移除指定屏幕的摄像头
 */
const removeScreen = (index: number) => {
  const screen = videoScreens.value[index]
  if (screen.isPlaying) {
    stopScreenInternal(screen)
  }
  screen.camera = null
  screen.snapshot = null
  console.log(`[多屏预览] 🗑️ 屏幕 ${index + 1} 已清空`)
}

/**
 * 刷新全部
 */
const handleRefreshAll = () => {
  videoScreens.value.forEach((screen, index) => {
    if (screen.camera && screen.isPlaying) {
      stopScreenInternal(screen)
      setTimeout(() => {
        playScreen(index)
      }, 100)
    }
  })
  ElMessage.info('正在刷新全部播放器...')
}

/**
 * 全屏
 */
const handleFullscreen = () => {
  if (containerRef.value) {
    if (containerRef.value.requestFullscreen) {
      containerRef.value.requestFullscreen()
    }
  }
}

/**
 * 布局切换
 */
const handleLayoutChange = () => {
  // 停止所有播放
  videoScreens.value.forEach(screen => {
    if (screen.isPlaying) {
      stopScreenInternal(screen)
    }
  })

  // 重新初始化屏幕
  const oldCameras = videoScreens.value
    .filter(s => s.camera !== null)
    .map(s => s.camera)
  
  initVideoScreens()

  // 恢复之前的摄像头（如果数量允许）
  oldCameras.slice(0, maxScreens.value).forEach((camera, index) => {
    if (camera) {
      videoScreens.value[index].camera = camera
      videoScreens.value[index].snapshot = camera.snapshotUrl || null
    }
  })

  ElMessage.info(`切换到 ${currentLayout.value} 布局`)
}

/**
 * 播放器错误处理
 */
const handlePlayerError = (screen: VideoScreen, error: Error) => {
  console.error('[多屏预览] 播放器错误:', error)
  screen.isPlaying = false
  screen.isLoading = false
  ElMessage.error(`播放失败: ${error.message}`)
}

/**
 * 设置播放器引用
 */
const setPlayerRef = (index: number, el: any) => {
  if (el) {
    playerRefs.value[index] = el
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  console.log('[多屏预览] 组件已挂载')
  initVideoScreens()
  loadAvailableCameras()
})

onUnmounted(() => {
  console.log('[多屏预览] 组件卸载，停止所有播放')
  videoScreens.value.forEach(screen => {
    if (screen.isPlaying) {
      stopScreenInternal(screen)
    }
  })
})
</script>

<style scoped lang="scss">
.improved-multi-screen-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

.toolbar {
  padding: 15px 20px;
  background: rgba(255, 255, 255, 0.05);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);

  .info-text {
    color: #94a3b8;
    font-size: 14px;
  }
}

.video-grid-container {
  flex: 1;
  display: grid;
  gap: 8px;
  padding: 8px;
  overflow: hidden;

  &.layout-1x1 {
    grid-template-columns: 1fr;
    grid-template-rows: 1fr;
  }

  &.layout-2x2 {
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: repeat(2, 1fr);
  }

  &.layout-3x3 {
    grid-template-columns: repeat(3, 1fr);
    grid-template-rows: repeat(3, 1fr);
  }

  &.layout-4x4 {
    grid-template-columns: repeat(4, 1fr);
    grid-template-rows: repeat(4, 1fr);
  }
}

.video-screen {
  position: relative;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;

  &.playing {
    border-color: #00ff88;
    box-shadow: 0 0 15px rgba(0, 255, 136, 0.4);
  }

  &.empty {
    border-style: dashed;
    cursor: pointer;

    &:hover {
      border-color: #00d4ff;
      background: rgba(0, 212, 255, 0.05);
    }
  }

  .video-player {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }

  .camera-snapshot {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .loading-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.8);
    color: #00d4ff;
    gap: 10px;

    .loading-icon {
      animation: spin 1s linear infinite;
    }
  }

  .camera-info {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 10px;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.9), transparent);
    color: #fff;
    display: flex;
    flex-direction: column;
    gap: 2px;

    .camera-name {
      font-weight: 600;
      font-size: 14px;
    }

    .camera-location {
      font-size: 12px;
      color: #94a3b8;
    }
  }

  .camera-controls {
    position: absolute;
    top: 10px;
    right: 10px;
    display: flex;
    gap: 5px;
    opacity: 0;
    transition: opacity 0.3s;
  }

  &:hover .camera-controls {
    opacity: 1;
  }

  .empty-screen {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 10px;
    color: #666;

    .empty-text {
      font-size: 14px;
    }
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// 摄像头选择对话框样式
.camera-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
  max-height: 500px;
  overflow-y: auto;
  padding: 10px;
}

.camera-card {
  border: 2px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;

  &.selected {
    border-color: #409EFF;
    box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
  }

  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  &:hover:not(.disabled) {
    transform: translateY(-4px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  }

  .camera-snapshot-preview {
    position: relative;
    width: 100%;
    height: 150px;
    background: #000;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .no-snapshot {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 10px;
      color: #666;
      font-size: 12px;
    }

    .selected-badge {
      position: absolute;
      top: 5px;
      right: 5px;
      width: 30px;
      height: 30px;
      border-radius: 50%;
      background: #409EFF;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
    }

    .status-tag {
      position: absolute;
      bottom: 5px;
      left: 5px;
    }
  }

  .camera-card-info {
    padding: 10px;
    background: #f9f9f9;

    .camera-card-name {
      font-weight: 600;
      font-size: 14px;
      color: #333;
      margin-bottom: 5px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .camera-card-location {
      font-size: 12px;
      color: #666;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

// 深色主题对话框
:deep(.el-dialog) {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border: 1px solid rgba(0, 212, 255, 0.3);

  .el-dialog__header {
    border-bottom: 1px solid rgba(0, 212, 255, 0.2);

    .el-dialog__title {
      color: #ffffff;
    }
  }

  .el-dialog__body {
    color: #e2e8f0;
  }
}
</style>

