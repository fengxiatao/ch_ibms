<template>
  <div class="scene-wizard">
    <!-- 顶部工具栏：场景配置表单 + 操作按钮 -->
    <div v-if="currentSceneIndex < scenes.length && scenes[currentSceneIndex]" class="top-toolbar">
      <!-- 左侧：场景配置表单 -->
      <el-form :inline="true" size="small" class="toolbar-form">
        <el-form-item label="场景名称">
          <el-input 
            v-model="scenes[currentSceneIndex].sceneName" 
            placeholder="请输入场景名称"
            @input="updateSceneName"
            style="width: 150px;"
          />
        </el-form-item>
        <el-form-item label="分屏布局">
          <el-select 
            v-model="scenes[currentSceneIndex].gridLayout" 
            @change="onLayoutChange(currentSceneIndex)"
            style="width: 120px;"
          >
            <el-option label="1x1 单屏" value="1x1" />
            <el-option label="2x2 四分屏" value="2x2" />
            <el-option label="3x3 九分屏" value="3x3" />
            <el-option label="4x4 十六分屏" value="4x4" />
          </el-select>
        </el-form-item>
        <el-form-item label="播放时长">
          <el-input-number 
            v-model="scenes[currentSceneIndex].duration" 
            :min="5" 
            :max="600" 
            :step="5"
            @change="updateDuration"
            style="width: 100px;"
          />
          <span style="margin-left: 4px; color: rgba(255,255,255,0.6);">秒</span>
        </el-form-item>
      </el-form>
      
      <!-- 右侧：场景操作按钮 -->
      <div class="toolbar-actions">
        <el-button 
          @click="prevScene" 
          :disabled="currentSceneIndex === 0"
          size="small"
        >
          <Icon icon="ep:arrow-left" /> 上一场景
        </el-button>
        <el-button 
          @click="nextScene"
          :disabled="currentSceneIndex >= scenes.length - 1"
          size="small"
        >
          下一场景 <Icon icon="ep:arrow-right" />
        </el-button>
        <el-button 
          type="danger" 
          @click="deleteCurrentScene"
          :disabled="scenes.length === 0"
          size="small"
        >
          <Icon icon="ep:delete" /> 删除场景
        </el-button>
        <el-button 
          type="primary" 
          @click="addNewScene"
          size="small"
        >
          <Icon icon="ep:plus" /> 添加场景
        </el-button>
        <span class="scene-info">场景 {{ currentSceneIndex + 1 }} / {{ scenes.length }}</span>
      </div>
    </div>

    <!-- 当前场景配置区域 -->
    <div class="scene-config-area">
      <!-- 已有场景的配置 -->
      <div v-if="currentSceneIndex < scenes.length && scenes[currentSceneIndex]" class="scene-editor">
        <div class="scene-form-compact">

          <!-- 多屏预览配置区域 - 左右分栏布局 -->
          <div class="multi-screen-container">
            <!-- 左侧：摄像头选择器 -->
            <div class="camera-selector-section">
              <div class="section-header">
                <span class="section-title">
                  <Icon icon="ep:camera" /> 摄像头列表
                </span>
                <span v-if="selectedGridPosition" class="selected-tip">
                  当前选择：格子 {{ selectedGridPosition }}
                </span>
              </div>

              <div class="camera-selector-wrapper">
                <CameraSelector 
                  v-if="selectedGridPosition"
                  @select="onCameraSelect"
                />
              </div>
            </div>

            <!-- 右侧：多屏预览网格 -->
            <div class="preview-section">
              <div class="section-header">
                <span class="section-title">
                  <Icon icon="ep:monitor" /> 多屏预览
                </span>
              </div>

              <div 
                class="screen-grid"
                :style="{
                  gridTemplateColumns: `repeat(${GRID_LAYOUTS[scenes[currentSceneIndex].gridLayout].cols}, 1fr)`,
                  gridTemplateRows: `repeat(${GRID_LAYOUTS[scenes[currentSceneIndex].gridLayout].rows}, 1fr)`
                }"
              >
                <div 
                  v-for="position in scenes[currentSceneIndex].gridCount" 
                  :key="position"
                  class="screen-cell"
                  :class="{ 'is-selected': selectedGridPosition === position }"
                  @click="selectGridPosition(position)"
                >
                  <div v-if="getChannelByPosition(currentSceneIndex, position)" class="cell-filled">
                    <!-- ✅ ZLMediaKit + mpegts.js 播放器区域 -->
                    <div class="video-player">
                      <!-- Video元素（mpegts.js 播放器） -->
                      <video 
                        :id="`patrol_video_${currentSceneIndex}_${position}`"
                        class="mpegts-video"
                        muted autoplay playsinline webkit-playsinline
                        style="width: 100%; height: 100%; object-fit: contain; background: #000;"
                      ></video>
                      
                      <!-- 加载状态 -->
                      <div 
                        v-show="getScreenState(position)?.loading"
                        class="mpegts-loading"
                      >
                        <el-icon class="is-loading"><Loading /></el-icon>
                        <span>加载中...</span>
                      </div>
                    </div>
                    
                    <!-- 视频信息叠加层 -->
                    <div class="video-overlay">
                      <div class="camera-info">
                        <div class="camera-name">{{ getChannelByPosition(currentSceneIndex, position)?.cameraName }}</div>
                        <div class="camera-code">{{ getChannelByPosition(currentSceneIndex, position)?.cameraCode }}</div>
                      </div>
                      <el-button 
                        size="small" 
                        type="danger" 
                        circle
                        class="remove-btn"
                        @click.stop="removeChannel(currentSceneIndex, position)"
                      >
                        <Icon icon="ep:close" />
                      </el-button>
                    </div>
                  </div>
                  <div v-else class="cell-empty">
                    <Icon icon="ep:video-camera" class="add-icon" />
                    <div class="add-text">选择摄像头</div>
                    <div class="position-num">{{ position }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 添加新场景 -->
      <div v-else class="add-scene-panel">
        <el-empty description="点击下方按钮添加新场景">
          <el-button type="primary" size="large" @click="addNewScene">
            <Icon icon="ep:plus" /> 添加场景
          </el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import CameraSelector from './CameraSelector.vue'
import mpegts from 'mpegts.js'
import { getLivePlayUrl, stopStream } from '@/api/iot/video/zlm'

// 浏览器检测
const isFirefox = navigator.userAgent.toLowerCase().includes('firefox')

// 智能路由：内网/公网自动选择
const isIntranetAccess = (): boolean => {
  const hostname = window.location.hostname
  if (hostname === 'localhost' || hostname === '127.0.0.1') return true
  if (hostname.startsWith('192.168.')) return true
  if (hostname.startsWith('10.')) return true
  if (hostname.startsWith('172.')) {
    const secondOctet = parseInt(hostname.split('.')[1])
    if (secondOctet >= 16 && secondOctet <= 31) return true
  }
  return false
}

const adaptPlayUrls = (urls: any): any => {
  if (!urls) return urls
  const intranet = isIntranetAccess()
  if (intranet) return urls
  
  const adapted = { ...urls }
  const publicHost = window.location.hostname
  const publicPort = window.location.port ? parseInt(window.location.port) : 80
  const publicAddr = publicPort === 80 || publicPort === 443 ? publicHost : `${publicHost}:${publicPort}`
  const isHttps = window.location.protocol === 'https:'
  const httpProtocol = isHttps ? 'https' : 'http'
  const wsProtocol = isHttps ? 'wss' : 'ws'
  
  const replaceUrl = (url: string, isWs = false): string => {
    if (!url) return url
    return url
      .replace(/192\.168\.\d+\.\d+:\d+/g, publicAddr)
      .replace(/192\.168\.\d+\.\d+/g, publicHost)
      .replace(/^(http|ws):/, isWs ? `${wsProtocol}:` : `${httpProtocol}:`)
  }
  
  adapted.wsFlvUrl = replaceUrl(urls.wsFlvUrl, true)
  adapted.flvUrl = replaceUrl(urls.flvUrl)
  adapted.hlsUrl = replaceUrl(urls.hlsUrl)
  if (urls.webrtcUrl) adapted.webrtcUrl = replaceUrl(urls.webrtcUrl)
  
  return adapted
}

interface ChannelConfig {
  gridPosition: number
  cameraId?: number | null
  cameraName?: string
  cameraCode?: string
  streamUrl?: string
  streamType?: number
  isEmpty?: boolean
  // ✅ 播放所需的完整信息
  nvrId?: number
  channelNo?: number
  ip?: string
  ipAddress?: string
  nvrIp?: string
  nvrPort?: number
  ptzSupport?: boolean
}

// ✅ 播放器状态接口
interface ScreenState {
  loading: boolean
  isPlaying: boolean
  player: any | null
}

interface PatrolScene {
  sceneId?: number  // 与后端 API 保持一致
  sceneName: string
  sceneOrder: number
  duration: number
  gridLayout: '1x1' | '2x2' | '3x3' | '4x4'
  gridCount: number
  channels: ChannelConfig[]
}

const props = withDefaults(defineProps<{
  modelValue?: PatrolScene[]
}>(), {
  modelValue: () => []
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: PatrolScene[]): void
}>()

const GRID_LAYOUTS = {
  '1x1': { rows: 1, cols: 1, count: 1 },
  '2x2': { rows: 2, cols: 2, count: 4 },
  '3x3': { rows: 3, cols: 3, count: 9 },
  '4x4': { rows: 4, cols: 4, count: 16 }
}

const scenes = computed({
  get: () => props.modelValue || [],
  set: (value) => emit('update:modelValue', value)
})

const currentSceneIndex = ref(0)
const selectedGridPosition = ref<number | null>(null)

// ✅ 播放器状态管理（使用 Map 存储每个格子的状态）
const screenStates = ref<Map<string, ScreenState>>(new Map())

// 获取格子的播放器状态
const getScreenState = (position: number): ScreenState | undefined => {
  const key = `${currentSceneIndex.value}_${position}`
  return screenStates.value.get(key)
}

// 设置格子的播放器状态
const setScreenState = (position: number, state: Partial<ScreenState>) => {
  const key = `${currentSceneIndex.value}_${position}`
  const current = screenStates.value.get(key) || { loading: false, isPlaying: false, player: null }
  screenStates.value.set(key, { ...current, ...state })
}

// 添加新场景
const addNewScene = async () => {
  const newScene: PatrolScene = {
    sceneName: `场景${scenes.value.length + 1}`,
    sceneOrder: scenes.value.length,
    duration: 30,
    gridLayout: '2x2',
    gridCount: 4,
    channels: []
  }
  const newScenes = [...scenes.value, newScene]
  emit('update:modelValue', newScenes)
  
  // 等待 DOM 更新后再设置索引
  await nextTick()
  currentSceneIndex.value = newScenes.length - 1
}

// 删除当前场景
const deleteCurrentScene = () => {
  ElMessageBox.confirm('确定要删除当前场景吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const newScenes = [...scenes.value]
    newScenes.splice(currentSceneIndex.value, 1)
    // 更新场景顺序
    newScenes.forEach((scene, idx) => {
      scene.sceneOrder = idx
    })
    emit('update:modelValue', newScenes)
    
    // 调整当前索引
    if (currentSceneIndex.value >= newScenes.length) {
      currentSceneIndex.value = Math.max(0, newScenes.length - 1)
    }
    
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// 上一个场景
const prevScene = async () => {
  if (currentSceneIndex.value > 0) {
    // 停止当前场景的所有播放器
    await stopCurrentScenePlayers()
    currentSceneIndex.value--
    // watch 会自动触发播放，不需要手动调用
  }
}

// 下一个场景
const nextScene = async () => {
  if (currentSceneIndex.value < scenes.value.length - 1) {
    // 停止当前场景的所有播放器
    await stopCurrentScenePlayers()
    currentSceneIndex.value++
    // watch 会自动触发播放，不需要手动调用
  }
}

// 分屏布局改变
const onLayoutChange = (sceneIndex: number) => {
  const scene = scenes.value[sceneIndex]
  if (!scene) return
  
  const layoutInfo = GRID_LAYOUTS[scene.gridLayout]
  
  // 清空超出范围的摄像头配置
  const newChannels = scene.channels.filter(
    ch => ch.gridPosition <= layoutInfo.count
  )
  
  const newScenes = [...scenes.value]
  newScenes[sceneIndex] = {
    ...scene,
    gridCount: layoutInfo.count,
    channels: newChannels
  }
  emit('update:modelValue', newScenes)
}

// 选择网格位置
const selectGridPosition = (position: number) => {
  selectedGridPosition.value = position
}

// 选择摄像头回调
const onCameraSelect = async (camera: any) => {
  if (!selectedGridPosition.value) return
  
  const scene = scenes.value[currentSceneIndex.value]
  if (!scene) return
  
  console.log('[场景配置] ========== 选择摄像头 ==========')
  console.log('[场景配置] 摄像头完整信息:', JSON.stringify(camera, null, 2))
  console.log('[场景配置] 关键字段检查:', {
    id: camera.id,
    name: camera.name,
    code: camera.code,
    nvrId: camera.nvrId,
    nvrIp: camera.nvrIp,
    nvrPort: camera.nvrPort,
    channelNo: camera.channelNo,
    ipAddress: camera.ipAddress,
    ptzSupport: camera.ptzSupport
  })
  
  // ⚠️ 检查必要字段
  if (!camera.nvrIp) {
    console.warn('[场景配置] ⚠️ 警告: 摄像头缺少 nvrIp 字段')
    ElMessage.warning('该摄像头缺少 NVR IP 地址，可能无法播放')
  }
  if (camera.channelNo === undefined || camera.channelNo === null) {
    console.warn('[场景配置] ⚠️ 警告: 摄像头缺少 channelNo 字段')
    ElMessage.warning('该摄像头缺少通道号，可能无法播放')
  }
  
  // ✅ 保存完整的通道配置信息
  const channelConfig: ChannelConfig = {
    gridPosition: selectedGridPosition.value,
    cameraId: camera.id,
    cameraName: camera.name,
    cameraCode: camera.code,
    streamUrl: camera.streamUrl,
    streamType: 1,
    // ✅ 播放所需的完整信息
    nvrId: camera.nvrId,
    channelNo: camera.channelNo,
    ipAddress: camera.ipAddress,
    nvrIp: camera.nvrIp,
    nvrPort: camera.nvrPort || 80,
    ptzSupport: camera.ptzSupport
  }
  
  const existingIndex = scene.channels.findIndex(
    ch => ch.gridPosition === selectedGridPosition.value
  )
  
  // 创建新的 channels 数组以触发响应式更新
  const newChannels = [...scene.channels]
  
  if (existingIndex >= 0) {
    // 先停止旧的播放
    await stopPlayChannel(selectedGridPosition.value)
    newChannels[existingIndex] = channelConfig
  } else {
    newChannels.push(channelConfig)
  }
  
  // 更新场景的 channels
  const newScenes = [...scenes.value]
  newScenes[currentSceneIndex.value] = {
    ...scene,
    channels: newChannels
  }
  emit('update:modelValue', newScenes)
  
  // ✅ 立即播放视频
  await nextTick()
  await startPlayChannel(selectedGridPosition.value, channelConfig)
  
  ElMessage.success('摄像头配置成功')
}

// 根据位置获取通道
const getChannelByPosition = (sceneIndex: number, position: number) => {
  const scene = scenes.value[sceneIndex]
  if (!scene) return null
  return scene.channels.find(ch => ch.gridPosition === position)
}

// 移除通道
const removeChannel = (sceneIndex: number, position: number) => {
  const scene = scenes.value[sceneIndex]
  if (!scene) return
  
  const index = scene.channels.findIndex(ch => ch.gridPosition === position)
  if (index >= 0) {
    const newChannels = [...scene.channels]
    newChannels.splice(index, 1)
    
    const newScenes = [...scenes.value]
    newScenes[sceneIndex] = {
      ...scene,
      channels: newChannels
    }
    emit('update:modelValue', newScenes)
  }
}

// 更新场景名称
const updateSceneName = () => {
  const newScenes = [...scenes.value]
  console.log('[场景配置] 更新场景名称:', newScenes)
  emit('update:modelValue', newScenes)
}

// 更新播放时长
const updateDuration = () => {
  const newScenes = [...scenes.value]
  console.log('[场景配置] 更新播放时长:', newScenes)
  emit('update:modelValue', newScenes)
}

// ========== 视频播放控制 (ZLMediaKit + mpegts.js) ==========

/**
 * 开始播放通道视频 - 使用 ZLMediaKit + mpegts.js
 */
const startPlayChannel = async (position: number, channel: ChannelConfig) => {
  try {
    console.log('[场景配置] ========== 开始播放 (ZLMediaKit) ==========')
    console.log('[场景配置] 位置:', position)
    console.log('[场景配置] 通道信息:', channel.cameraName, 'ID:', channel.cameraId)
    
    // 参数验证
    if (!channel.cameraId) {
      const errorMsg = `格子${position}: 缺少通道ID`
      console.error('[场景配置]', errorMsg)
      ElMessage.error(errorMsg)
      setScreenState(position, { loading: false, isPlaying: false })
      return
    }
    
    // 设置加载状态
    setScreenState(position, { loading: true, isPlaying: false })
    
    const videoId = `patrol_video_${currentSceneIndex.value}_${position}`
    
    // 等待DOM渲染
    await nextTick()
    
    const videoElement = document.getElementById(videoId) as HTMLVideoElement
    
    if (!videoElement) {
      console.error('[场景配置] 找不到视频元素:', videoId)
      setScreenState(position, { loading: false })
      return
    }
    
    // 通过 ZLMediaKit API 获取播放地址
    const rawPlayUrls = await getLivePlayUrl(channel.cameraId, 1) // 1=子码流
    const playUrls = adaptPlayUrls(rawPlayUrls)
    
    console.log('[场景配置] 获取到播放地址:', playUrls)
    
    if (!playUrls || !playUrls.wsFlvUrl) {
      throw new Error('未获取到有效的播放地址')
    }
    
    // 使用 mpegts.js 播放 FLV 流
    const success = await playWithFLV(position, videoElement, playUrls.wsFlvUrl)
    
    if (success) {
      console.log(`[场景配置] ✅ 格子${position} 播放成功: ${channel.cameraName}`)
    } else {
      throw new Error('播放失败')
    }
    
  } catch (error: any) {
    console.error('[场景配置] ❌ 播放失败:', error)
    setScreenState(position, { loading: false, isPlaying: false })
    ElMessage.error(`视频播放失败: ${error.message || '未知错误'}`)
  }
}

/**
 * 使用 mpegts.js 播放 FLV 流
 */
const playWithFLV = async (position: number, videoEl: HTMLVideoElement, wsFlvUrl: string, disableAudio: boolean = false): Promise<boolean> => {
  return new Promise((resolve) => {
    try {
      if (!mpegts.isSupported()) {
        throw new Error('浏览器不支持 FLV 播放')
      }
      
      const player = mpegts.createPlayer({
        type: 'flv',
        url: wsFlvUrl,
        isLive: true,
        hasAudio: !disableAudio,
        hasVideo: true
      }, {
        enableWorker: false,
        enableStashBuffer: true,
        stashInitialSize: isFirefox ? 256 : 128,
        lazyLoad: false,
        autoCleanupSourceBuffer: true,
        autoCleanupMaxBackwardDuration: isFirefox ? 5 : 3,
        autoCleanupMinBackwardDuration: isFirefox ? 2 : 1,
        liveBufferLatencyChasing: true,
        liveBufferLatencyMaxLatency: isFirefox ? 2.0 : 1.5,
        liveBufferLatencyMinRemain: isFirefox ? 0.5 : 0.3,
        liveSync: true,
        fixAudioTimestampGap: true
      })
      
      let audioUnsupported = false
      
      player.on(mpegts.Events.ERROR, (_errorType: any, errorDetail: any) => {
        const errorStr = String(errorDetail || '')
        // 检测音频编解码器不支持
        if (errorStr.includes('CodecUnsupported') && errorStr.includes('audio')) {
          audioUnsupported = true
          console.warn(`[场景配置] 格子${position} 音频编码不支持，将以纯视频模式重试`)
          // 停止当前播放器
          try {
            player.pause()
            player.unload()
            player.detachMediaElement()
            player.destroy()
          } catch {}
          // 以无音频模式重试
          playWithFLV(position, videoEl, wsFlvUrl, true).then(resolve)
          return
        }
        // 忽略其他 MSE 警告
        if (errorStr.includes('MSEError') || errorStr.includes('SourceBuffer')) return
        console.error(`[场景配置] 格子${position} 播放错误:`, errorDetail)
        setScreenState(position, { loading: false, isPlaying: false })
        resolve(false)
      })
      
      player.attachMediaElement(videoEl)
      player.load()
      
      const playDelay = isFirefox ? 500 : 100
      setTimeout(async () => {
        if (audioUnsupported) return // 如果已触发重试，不继续
        try {
          await player.play()
          setScreenState(position, { loading: false, isPlaying: true, player })
          if (disableAudio) {
            console.log(`[场景配置] ✅ 格子${position} 以纯视频模式播放成功`)
          }
          resolve(true)
        } catch {
          setScreenState(position, { loading: false, isPlaying: false })
          resolve(false)
        }
      }, playDelay)
    } catch {
      resolve(false)
    }
  })
}

/**
 * 停止播放通道视频 - mpegts.js 正确销毁顺序
 */
const stopPlayChannel = async (position: number) => {
  try {
    const state = getScreenState(position)
    if (state?.player) {
      console.log(`[场景配置] 停止播放格子${position}`)
      const player = state.player
      try {
        if (typeof player.pause === 'function') player.pause()
        if (typeof player.unload === 'function') player.unload()
        if (typeof player.detachMediaElement === 'function') player.detachMediaElement()
        if (typeof player.destroy === 'function') player.destroy()
      } catch (e) {
        console.warn('[场景配置] 销毁播放器异常:', e)
      }
      setScreenState(position, { loading: false, isPlaying: false, player: null })
    }
  } catch (error) {
    console.error('[场景配置] 停止播放失败:', error)
  }
}

/**
 * 停止当前场景的所有播放器 - mpegts.js 正确销毁顺序
 */
const stopCurrentScenePlayers = async () => {
  const scene = scenes.value[currentSceneIndex.value]
  if (!scene) return
  
  console.log(`[场景配置] 停止场景 ${currentSceneIndex.value} 的所有播放器`)
  
  // 遍历当前场景的所有通道
  for (const channel of scene.channels) {
    const key = `${currentSceneIndex.value}_${channel.gridPosition}`
    const state = screenStates.value.get(key)
    
    if (state?.player) {
      try {
        console.log(`[场景配置] 停止格子 ${channel.gridPosition} 的播放器`)
        const player = state.player
        if (typeof player.pause === 'function') player.pause()
        if (typeof player.unload === 'function') player.unload()
        if (typeof player.detachMediaElement === 'function') player.detachMediaElement()
        if (typeof player.destroy === 'function') player.destroy()
        screenStates.value.delete(key)
      } catch (error) {
        console.error(`[场景配置] 停止播放器失败: ${key}`, error)
      }
    }
  }
}

/**
 * 播放当前场景的所有已配置摄像头 - 使用 ZLMediaKit
 */
const playCurrentSceneChannels = async () => {
  const scene = scenes.value[currentSceneIndex.value]
  if (!scene) return
  
  console.log(`[场景配置] ========== 播放场景 ${currentSceneIndex.value} 的所有摄像头 (ZLMediaKit) ==========`)
  console.log(`[场景配置] 场景名称: ${scene.sceneName}`)
  console.log(`[场景配置] 已配置通道数: ${scene.channels.length}`)
  
  // 等待 DOM 更新
  await nextTick()
  
  // 遍历所有已配置的通道并播放
  for (const channel of scene.channels) {
    // ZLMediaKit 只需要 cameraId（通道ID）即可获取播放地址
    if (channel.cameraId) {
      console.log(`[场景配置] 自动播放格子 ${channel.gridPosition}: ${channel.cameraName}`)
      await startPlayChannel(channel.gridPosition, channel)
      // 添加延迟，避免同时初始化太多播放器
      await new Promise(resolve => setTimeout(resolve, 200))
    }
  }
}

/**
 * 停止所有播放 - mpegts.js 正确销毁顺序
 */
const stopAllPlayers = () => {
  console.log('[场景配置] 停止所有播放器')
  screenStates.value.forEach((state, key) => {
    if (state.player) {
      try {
        const player = state.player
        if (typeof player.pause === 'function') player.pause()
        if (typeof player.unload === 'function') player.unload()
        if (typeof player.detachMediaElement === 'function') player.detachMediaElement()
        if (typeof player.destroy === 'function') player.destroy()
      } catch (error) {
        console.error(`[场景配置] 停止播放器失败: ${key}`, error)
      }
    }
  })
  screenStates.value.clear()
}

// ========== 监听场景切换 ==========

/**
 * 监听场景索引变化，自动播放新场景的摄像头
 */
watch(currentSceneIndex, async (newIndex, oldIndex) => {
  console.log(`[场景配置] 场景切换: ${oldIndex} -> ${newIndex}`)
  
  // 等待 DOM 更新后播放新场景的摄像头
  await nextTick()
  await playCurrentSceneChannels()
}, { immediate: false })

/**
 * 监听 scenes 数据变化（用于编辑任务时加载已有场景）
 */
watch(() => props.modelValue, async (newScenes, oldScenes) => {
  // 只在从无到有，或者场景数量变化时触发
  if (newScenes && newScenes.length > 0) {
    const oldLength = oldScenes?.length || 0
    const newLength = newScenes.length
    
    console.log(`[场景配置] 场景数据变化: ${oldLength} -> ${newLength} 个场景`)
    
    // 如果是首次加载场景数据（编辑任务时），重置到第一个场景并自动播放
    if (oldLength === 0 && newLength > 0) {
      console.log('[场景配置] 检测到编辑任务，加载已有场景数据')
      
      // ✅ 重置场景索引为 0（显示第一个场景）
      currentSceneIndex.value = 0
      
      // 停止所有旧的播放器
      stopAllPlayers()
      
      console.log('[场景配置] 重置到第一个场景，准备自动播放')
      await nextTick()
      await playCurrentSceneChannels()
    }
  }
}, { deep: true, immediate: false })

// 组件挂载时播放第一个场景
onMounted(async () => {
  console.log('[场景配置] 组件挂载，播放第一个场景')
  await nextTick()
  
  // 延迟一下，确保数据已经加载
  setTimeout(async () => {
    await playCurrentSceneChannels()
  }, 500)
})

// 组件卸载时停止所有播放
onBeforeUnmount(() => {
  console.log('[场景配置] 组件卸载，停止所有播放器')
  stopAllPlayers()
})
</script>

<style scoped lang="scss">
.scene-wizard {
  background: #1a1a1a;  // 深色背景
  padding: 16px;
  border-radius: 8px;
  
  // 顶部工具栏
  .top-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    padding-bottom: 12px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    
    .toolbar-form {
      flex: 1;
      margin: 0;
      
      :deep(.el-form-item) {
        margin-bottom: 0;
        margin-right: 12px;
      }
      
      :deep(.el-form-item__label) {
        font-size: 12px;
        color: rgba(255, 255, 255, 0.85);
      }
    }
    
    .toolbar-actions {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .scene-info {
        margin-left: 12px;
        font-size: 13px;
        color: rgba(255, 255, 255, 0.7);
        font-weight: 600;
      }
    }
  }
  
  .scene-config-area {
    min-height: 500px;
    margin-bottom: 0;  // 移除底部间距
    
    .scene-editor {
      .scene-form-compact {
        .compact-form {
          margin-bottom: 4px;  // 进一步减小间距
          
          :deep(.el-form-item) {
            margin-bottom: 0;  // 移除间距
            margin-right: 8px;  // 进一步减小间距
          }
          
          :deep(.el-form-item__label) {
            font-size: 12px;  // 减小字体
          }
        }
        
        .multi-screen-container {
          margin-top: 4px;  // 进一步减小间距
          display: flex;
          gap: 12px;  // 减小间距
          min-height: 450px;
          align-items: flex-start;  // 顶部对齐
          
          .camera-selector-section {
            width: 350px;
            display: flex;
            flex-direction: column;
            // 移除背景和内边距，使其与右侧对齐
            
            .section-header {
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 6px;  // 进一步减小间距
              padding-bottom: 4px;  // 进一步减小间距
              border-bottom: 1px solid rgba(255, 255, 255, 0.1);
              
              .section-title {
                display: flex;
                align-items: center;
                gap: 4px;  // 进一步减小间距
                font-size: 13px;  // 进一步减小字体
                font-weight: 600;
                color: #fff;
              }
              
              .selected-tip {
                font-size: 11px;  // 减小字体
                color: #409eff;
                background: rgba(64, 158, 255, 0.1);
                padding: 2px 6px;  // 减小内边距
                border-radius: 3px;
              }
            }
            
            .camera-selector-wrapper {
              flex: 1;
              overflow: hidden;
            }
          }
          
          .preview-section {
            flex: 1;
            display: flex;
            flex-direction: column;
            
            .section-header {
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 6px;  // 与左侧保持一致
              padding-bottom: 4px;  // 与左侧保持一致
              border-bottom: 1px solid rgba(255, 255, 255, 0.1);
              
              .section-title {
                display: flex;
                align-items: center;
                gap: 4px;  // 与左侧保持一致
                font-size: 13px;  // 与左侧保持一致
                font-weight: 600;
                color: #fff;
              }
            }
            
            .screen-grid {
              flex: 1;
              display: grid;
              gap: 12px;
              padding: 20px;
              background: rgba(0, 0, 0, 0.3);
              border-radius: 8px;
              
              .screen-cell {
                aspect-ratio: 16/9;
                border: 2px dashed rgba(255, 255, 255, 0.2);
                border-radius: 8px;
                cursor: pointer;
                transition: all 0.3s;
                position: relative;
                overflow: hidden;
                background: rgba(255, 255, 255, 0.02);
                
                &:hover {
                  border-color: #409eff;
                  background: rgba(64, 158, 255, 0.05);
                }
                
                &.is-selected {
                  border-color: #409eff;
                  border-style: solid;
                  border-width: 3px;
                  background: rgba(64, 158, 255, 0.1);
                  box-shadow: 0 0 10px rgba(64, 158, 255, 0.3);
                }
                
                .cell-empty {
                  display: flex;
                  flex-direction: column;
                  align-items: center;
                  justify-content: center;
                  height: 100%;
                  color: rgba(255, 255, 255, 0.5);
                  
                  .add-icon {
                    font-size: 32px;
                    margin-bottom: 8px;
                  }
                  
                  .add-text {
                    font-size: 14px;
                    margin-bottom: 4px;
                  }
                  
                  .position-num {
                    font-size: 12px;
                    color: rgba(255, 255, 255, 0.3);
                  }
                }
                
                .cell-filled {
                  height: 100%;
                  background: #000;
                  border: 2px solid #409eff;
                  border-radius: 8px;
                  position: relative;
                  overflow: hidden;
                  
                  // ✅ 视频播放器容器
                  .video-player {
                    width: 100%;
                    height: 100%;
                    position: relative;
                    background: #000;
                    
                    .mpegts-video {
                      width: 100%;
                      height: 100%;
                      object-fit: contain;
                    }
                    
                    .mpegts-loading {
                      position: absolute;
                      top: 50%;
                      left: 50%;
                      transform: translate(-50%, -50%);
                      display: flex;
                      flex-direction: column;
                      align-items: center;
                      gap: 8px;
                      color: #409eff;
                      font-size: 14px;
                      
                      .el-icon {
                        font-size: 32px;
                      }
                    }
                  }
                  
                  // ✅ 视频信息叠加层
                  .video-overlay {
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    pointer-events: none;
                    
                    .camera-info {
                      position: absolute;
                      bottom: 0;
                      left: 0;
                      right: 0;
                      padding: 8px 12px;
                      background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
                      pointer-events: none;
                      
                      .camera-name {
                        font-size: 14px;
                        font-weight: 600;
                        color: #fff;
                        margin-bottom: 4px;
                        white-space: nowrap;
                        overflow: hidden;
                        text-overflow: ellipsis;
                      }
                      
                      .camera-code {
                        font-size: 12px;
                        color: rgba(255, 255, 255, 0.6);
                      }
                    }
                    
                    .remove-btn {
                      position: absolute;
                      top: 8px;
                      right: 8px;
                      pointer-events: auto;
                      z-index: 10;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    
    .add-scene-panel {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 400px;
    }
  }
}

:deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.85);
}

:deep(.el-steps) {
  .el-step__title {
    color: rgba(255, 255, 255, 0.7);
  }
  
  .el-step__description {
    color: rgba(255, 255, 255, 0.5);
  }
  
  .is-finish .el-step__title,
  .is-process .el-step__title {
    color: #fff;
  }
}
</style>
