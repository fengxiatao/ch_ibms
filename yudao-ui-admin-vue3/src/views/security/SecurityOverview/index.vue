<template>
  <ContentWrap style="margin-top: 70px">
    <div class="dark-theme-page">
      <div class="security-overview">
        <!-- 顶部标题 -->
        <div class="page-header">
          <h1 class="page-title">🚨 智慧安防监控平台 - 安防概览</h1>
          <div class="system-status">
            <span class="status-indicator online"></span>
            <span class="status-text">系统正常</span>
            <span class="current-time">{{ currentTime }}</span>
          </div>
        </div>

        <!-- 主要内容区域 -->
        <div class="main-content">
          <!-- 左侧监控大屏区域 -->
          <div class="left-section">
            <div class="video-wall-container">
              <div class="section-header">
                <Icon icon="ep:video-camera" />
                <span>实时监控大屏</span>
                <div v-if="totalPages > 0" class="page-indicator">
                  <span class="current-page">{{ currentPage + 1 }}</span>
                  <span class="separator">/</span>
                  <span class="total-pages">{{ totalPages }}</span>
                </div>
              </div>
              <div class="video-grid">
                <div
                  v-for="(camera, index) in cameras"
                  :key="index"
                  :class="['video-item', { empty: camera.isEmpty, playing: camera.isPlaying }]"
                  @click="!camera.isEmpty && toggleCameraPlay(camera)"
                >
                  <div class="video-placeholder">
                    <!-- 空槽位 -->
                    <div v-if="camera.isEmpty" class="empty-slot">
                      <Icon icon="ep:video-camera" :size="60" />
                      <div class="empty-text">暂无摄像头</div>
                    </div>
                    <!-- 离线设备 -->
                    <div v-else-if="camera.status === 'offline'" class="offline-slot">
                      <Icon icon="ep:video-camera-filled" :size="50" color="#ef4444" />
                      <div class="camera-info">
                        <div class="camera-name">{{ camera.name }}</div>
                        <div class="camera-location">{{ camera.location }}</div>
                      </div>
                    </div>
                    <!-- 在线设备 - 显示快照或视频 -->
                    <div v-else class="online-slot">
                      <!-- Jessibuca 播放器（支持 H.265, WebAssembly 解码，延时 < 500ms）-->
                      <div v-if="camera.playUrl && camera.isPlaying" class="camera-video"></div>
                      
                      <!-- 快照（未播放时显示） -->
                      <img 
                        v-if="!camera.isPlaying && camera.snapshotUrl && camera.snapshotUrl !== 'loading' && camera.snapshotUrl !== 'error'" 
                        :src="camera.snapshotUrl" 
                        :alt="camera.name"
                        class="camera-snapshot"
                        @error="camera.snapshotUrl = 'error'"
                      />
                      
                      <!-- 快照失败 -->
                      <div v-else-if="!camera.isPlaying && camera.snapshotUrl === 'error'" class="error-slot">
                        <Icon icon="ep:warning" :size="50" color="#ef4444" />
                        <div class="error-text">抓图失败</div>
                      </div>
                      
                      <!-- 加载中 -->
                      <div v-if="camera.isLoading" class="loading-slot">
                        <Icon icon="ep:loading" :size="40" class="loading-icon" />
                        <div class="loading-text">加载中...</div>
                      </div>
                      
                      <!-- 播放/暂停图标 -->
                      <div v-if="!camera.isLoading" class="play-control">
                        <Icon 
                          v-if="!camera.isPlaying" 
                          icon="ep:video-play" 
                          :size="50" 
                          class="play-icon"
                        />
                        <Icon 
                          v-else 
                          icon="ep:video-pause" 
                          :size="50" 
                          class="pause-icon"
                        />
                      </div>
                      
                      <div class="camera-info">
                        <div class="camera-name">{{ camera.name }}</div>
                        <div class="camera-location">{{ camera.location }}</div>
                      </div>
                    </div>
                  </div>
                  <div v-if="!camera.isEmpty" class="camera-status" :class="camera.status">
                    <span class="status-dot"></span>
                    {{ camera.statusText }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 右侧信息面板 -->
          <div class="right-section">
            <!-- 告警事件中心 -->
            <div class="alarm-center">
              <div class="section-header">
                <Icon icon="ep:warning" />
                <span>告警事件中心</span>
              </div>
              <div class="alarm-list">
                <div
                  v-for="(alarm, index) in alarms"
                  :key="index"
                  class="alarm-item"
                  :class="alarm.level"
                >
                  <div class="alarm-icon">
                    <Icon :icon="getAlarmIcon(alarm.type)" />
                  </div>
                  <div class="alarm-content">
                    <div class="alarm-title">{{ alarm.title }}</div>
                    <div class="alarm-time">{{ alarm.time || formatTimestamp(alarm.timestamp) }}</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 设备状态总览 -->
            <div class="device-status">
              <div class="section-header">
                <Icon icon="ep:monitor" />
                <span>设备状态总览</span>
              </div>
              <div class="status-grid">
                <div class="status-item">
                  <div class="status-number online">{{ deviceStats.online }}</div>
                  <div class="status-label">在线设备</div>
                </div>
                <div class="status-item">
                  <div class="status-number offline">{{ deviceStats.offline }}</div>
                  <div class="status-label">离线设备</div>
                </div>
                <div class="status-item">
                  <div class="status-number alarm">{{ deviceStats.alarm }}</div>
                  <div class="status-label">告警设备</div>
                </div>
                <div class="status-item">
                  <div class="status-number rate">{{ deviceStats.rate }}%</div>
                  <div class="status-label">在线率</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部功能模块 -->
        <div class="bottom-modules">
          <!-- 周界防护中心 -->
          <div class="module-card" @click="navigateTo('perimeter')">
            <div class="module-icon">
              <Icon icon="ep:shield" :size="32" />
            </div>
            <div class="module-content">
              <div class="module-title">周界防护中心</div>
              <div class="module-desc">实时监控周界状态，智能识别入侵行为</div>
            </div>
          </div>

          <!-- 智能分析中心 -->
          <div class="module-card active" @click="navigateTo('analysis')">
            <div class="module-icon">
              <Icon icon="ep:data-analysis" :size="32" />
            </div>
            <div class="module-content">
              <div class="module-title">智能分析中心</div>
              <div class="module-desc">AI行为分析，异常事件智能识别</div>
            </div>
          </div>

          <!-- 巡检任务中心 -->
          <div class="module-card" @click="navigateTo('patrol')">
            <div class="module-icon">
              <Icon icon="ep:location" :size="32" />
            </div>
            <div class="module-content">
              <div class="module-title">巡检任务中心</div>
              <div class="module-desc">巡检任务管理，任务执行跟踪</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts" name="SecurityOverview">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import * as SecurityOverviewApi from '@/api/iot/security-overview'
 
import { useIotWebSocket } from '@/hooks/iot/useIotWebSocket'
import type { AlarmEventMessage, DeviceStatsMessage, DeviceStatusMessage } from '@/hooks/iot/useIotWebSocket'
import { useUserStore } from '@/store/modules/user'

/** 安防概览页面 */
defineOptions({ name: 'SecurityOverview' })

const router = useRouter()
const userStore = useUserStore()

// 当前时间
const currentTime = ref('')

// 安防概览摄像头数据
const securityCameras = ref<any[]>([]) // 所有安防概览摄像头
const currentPage = ref(0) // 当前显示的页码
const switchTimer = ref<any>(null) // 切换定时器
const refreshTimer = ref<any>(null) // 快照刷新定时器

// 显示的摄像头（6个，包括空槽位）
const cameras = computed(() => {
  const startIndex = currentPage.value * 6
  const endIndex = startIndex + 6
  const cameraSlice = securityCameras.value.slice(startIndex, endIndex)
  
  // 确保始终返回6个元素
  const result = [...cameraSlice]
  while (result.length < 6) {
    result.push({
      id: `empty_${result.length}`,
      name: '',
      location: '',
      status: 'empty',
      statusText: '暂无摄像头',
      isEmpty: true,
      snapshotUrl: ''
    })
  }
  
  return result
})

// 总页数
const totalPages = computed(() => {
  return Math.max(1, Math.ceil(securityCameras.value.length / 6))
})

// 告警数据（WebSocket 实时推送）
const alarms = ref<AlarmEventMessage[]>([])
const maxAlarms = 10 // 最多显示10条告警

// 设备统计（WebSocket 实时推送）
const deviceStats = ref<DeviceStatsMessage>({
  online: 0,
  offline: 0,
  alarm: 0,
  total: 0,
  rate: 0
})

// ============= WebSocket 实时通信 =============

/**
 * IoT WebSocket 连接
 */
const { connect: connectIotWs, disconnect: disconnectIotWs, isConnected: isIotWsConnected } = useIotWebSocket({
  // 设备状态更新
  onDeviceStatus: (data: DeviceStatusMessage) => {
    console.log('[安防概览] 📡 设备状态更新:', data)
    
    // 更新摄像头列表中对应设备的状态
    const camera = securityCameras.value.find(c => c.id === data.deviceId)
    if (camera) {
      camera.status = data.status
      console.log(`[安防概览] 设备 ${data.deviceName} 状态变更为: ${data.status}`)
    }
  },

  // 告警事件推送
  onAlarmEvent: (data: AlarmEventMessage) => {
    console.log('[安防概览] 🚨 收到告警事件:', data)
    
    // 添加到告警列表（最新的在前）
    alarms.value.unshift({
      ...data,
      time: formatTimestamp(data.timestamp)
    } as any)
    
    // 限制告警数量
    if (alarms.value.length > maxAlarms) {
      alarms.value = alarms.value.slice(0, maxAlarms)
    }
    
    // 桌面通知
    ElNotification({
      title: `🚨 ${data.level === 'high' ? '高级' : data.level === 'warning' ? '警告' : '信息'}告警`,
      message: data.title,
      type: data.level === 'high' ? 'error' : data.level === 'warning' ? 'warning' : 'info',
      duration: 5000
    })
  },

  // 设备统计数据更新
  onDeviceStats: (data: DeviceStatsMessage) => {
    console.log('[安防概览] 📊 设备统计更新:', data)
    deviceStats.value = data
  },

  onConnected: () => {
    console.log('[安防概览] ✅ WebSocket 连接成功')
    ElMessage.success('实时通知连接成功')
  },

  onDisconnected: () => {
    console.log('[安防概览] ⚠️ WebSocket 连接断开')
  },

  onError: (error) => {
    console.error('[安防概览] ❌ WebSocket 错误:', error)
  }
})

/**
 * 格式化时间戳为相对时间
 */
const formatTimestamp = (timestamp: number): string => {
  const now = Date.now()
  const diff = Math.floor((now - timestamp) / 1000) // 秒
  
  if (diff < 60) return `${diff}秒前`
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
  return `${Math.floor(diff / 86400)}天前`
}

// 更新时间
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 获取告警图标
const getAlarmIcon = (type: string) => {
  const iconMap = {
    personnel: 'ep:user',
    device: 'ep:warning',
    behavior: 'ep:bell'
  }
  return iconMap[type] || 'ep:warning'
}

// ============= 视频播放功能 =============

// Video 播放器引用映射
const playerRefs = ref<Map<number, any>>(new Map())

// 当前正在播放的摄像头ID（单播放模式：同时只播放一路，避免性能问题）
const currentPlayingCameraId = ref<number | null>(null)

/**
 * 设置播放器引用
 */
const setPlayerRef = (cameraId: number, el: any) => {
  if (el) {
    playerRefs.value.set(cameraId, el)
  }
}

/**
 * 切换摄像头播放/暂停
 * 
 * 🚀 性能优化策略：
 * 1. 单播放模式：点击新摄像头时，自动暂停之前的播放
 * 2. 懒加载：只有点击播放时才获取播放地址
 * 3. 优先使用FLV格式：性能优于HLS，延迟更低
 */
const toggleCameraPlay = async (camera: any) => {
  if (camera.isEmpty || camera.status !== 'online') {
    ElMessage.warning('该摄像头暂不可用')
    return
  }
  
  console.log('[安防概览] 切换播放状态:', camera.name, camera.id, '当前状态:', camera.isPlaying)
  
  // 如果正在播放，则暂停
  if (camera.isPlaying) {
    pauseCamera(camera)
    return
  }
  
  // ✅ 单播放模式：停止其他正在播放的摄像头
  if (currentPlayingCameraId.value !== null && currentPlayingCameraId.value !== camera.id) {
    const playingCamera = securityCameras.value.find(c => c.id === currentPlayingCameraId.value)
    if (playingCamera && playingCamera.isPlaying) {
      console.log('[安防概览] 停止之前的播放:', playingCamera.name)
      pauseCamera(playingCamera)
    }
  }
  
  // 如果未播放，则开始播放
  playCamera(camera)
}

/**
 * 播放摄像头
 */
const playCamera = async (camera: any) => {
  try {
    camera.isLoading = true
    
    // 如果没有播放地址，先获取
    if (!camera.playUrl) {
      console.log('[安防概览] 获取播放地址:', camera.id)
      const playUrlData = await SecurityOverviewApi.getPlayUrl(camera.id)
      
      // ✅ 优先使用 WebSocket-FMP4（无并发限制，低延时 ~1-2秒）
      // 低延时优先：使用 HTTP-FLV（延时 ~1秒，最低延时方案）
      console.log('[安防概览] 🔍 可用格式:', {
        wsFlv: playUrlData.wsFlvUrl,
        webrtc: playUrlData.webrtcUrl,
        wsFmp4: playUrlData.wsFmp4Url,
        fmp4: playUrlData.fmp4Url,
        flv: playUrlData.flvUrl,
        hls: playUrlData.hlsUrl,
        rtmp: playUrlData.rtmpUrl
      })
      
        // ✅ 优先使用 HTTP-FLV（Jessibuca 完美支持，ZLMediaKit 100% 兼容）
        if (playUrlData && playUrlData.flvUrl) {
          camera.playUrl = playUrlData.flvUrl
          camera.playFormat = 'HTTP-FLV'
          console.log('[安防概览] ⭐⭐⭐⭐⭐ 使用 HTTP-FLV 播放（Jessibuca，延时~1秒，稳定）:', playUrlData.flvUrl)
        } else if (playUrlData && playUrlData.wsFlvUrl) {
          camera.playUrl = playUrlData.wsFlvUrl
          camera.playFormat = 'WebSocket-FLV'
          console.log('[安防概览] ✅ 使用 WebSocket-FLV 播放（超低延时<500ms）:', playUrlData.wsFlvUrl)
        } else if (playUrlData && playUrlData.fmp4Url) {
          camera.playUrl = playUrlData.fmp4Url
          camera.playFormat = 'HTTP-FMP4'
          console.warn('[安防概览] ⚠️ 降级到 HTTP-FMP4（延时1-2秒）:', playUrlData.fmp4Url)
        } else if (playUrlData && playUrlData.hlsUrl) {
          camera.playUrl = playUrlData.hlsUrl
          camera.playFormat = 'HLS'
          console.warn('[安防概览] ⚠️ 降级到 HLS（延时5-10秒）:', playUrlData.hlsUrl)
        } else {
          console.error('[安防概览] ❌ 所有格式都不可用:', playUrlData)
          throw new Error('未获取到有效的播放地址')
        }
      
      camera.streamKey = playUrlData.streamKey
      
      // 等待 DOM 更新
      await new Promise(resolve => setTimeout(resolve, 100))
    }
    
    // Video 标签会自动播放（autoplay），只需要设置状态
    camera.isPlaying = true
    camera.isLoading = false
    currentPlayingCameraId.value = camera.id
    
    console.log(`[安防概览] ✅ 开始播放: ${camera.name} (${camera.playFormat})`)
    
  } catch (error: any) {
    console.error('[安防概览] 播放失败:', camera.id, error)
    ElMessage.error('播放失败: ' + (error?.message || '未知错误'))
    camera.isLoading = false
    camera.isPlaying = false
  }
}

/**
 * 处理 Video 播放器错误
 */
const handlePlayerError = (camera: any, error: Error) => {
  console.error('[安防概览] 播放错误:', camera.id, camera.name)
  camera.isPlaying = false
  camera.isLoading = false
  
  ElMessage.error(`播放失败: ${camera.name} - ${error.message}`)
  
  // 如果是当前播放的摄像头，清空状态
  if (currentPlayingCameraId.value === camera.id) {
    currentPlayingCameraId.value = null
  }
}

/**
 * 暂停摄像头
 */
const pauseCamera = (camera: any) => {
  // 设置播放状态为false，video 元素会被销毁
  camera.isPlaying = false
  
  // 如果暂停的是当前正在播放的摄像头，清空状态
  if (currentPlayingCameraId.value === camera.id) {
    currentPlayingCameraId.value = null
  }
  
  console.log('[安防概览] ⏸️ 暂停播放:', camera.id, camera.name)
}

/**
 * 停止所有播放
 * 
 * 🔧 用途：
 * 1. 页面切换时调用，释放资源
 * 2. 分页切换时调用，避免内存泄漏
 */
const stopAllCameras = () => {
  securityCameras.value.forEach(camera => {
    if (camera.isPlaying) {
      pauseCamera(camera)
    }
  })
  currentPlayingCameraId.value = null
  console.log('[安防概览] 🛑 已停止所有播放')
}

// 导航到其他页面
const navigateTo = (module: string) => {
  const routeMap = {
    perimeter: '/security/perimeter-intrusion',
    analysis: '/security/video-analysis',
    patrol: '/security/electronic-patrol'
  }
  if (routeMap[module]) {
    router.push(routeMap[module])
  }
}

// ============= 安防概览摄像头功能 =============

/**
 * 加载安防概览摄像头列表
 * 
 * ✅ 使用新的后端专用API：/iot/security-overview/cameras
 * - 后端已处理菜单继承逻辑
 * - 返回专门的SecurityOverviewCameraVO
 * - 前端只需要简单调用和展示
 */
const loadSecurityCameras = async () => {
  try {
    console.log('[安防概览] 开始加载摄像头列表...')
    
    // 🆕 阶段1：快速加载列表（不获取快照，避免超时）
    const allCameras: any[] = []
    let pageNo = 1
    const pageSize = 100
    let hasMore = true
    
    while (hasMore) {
      const res = await SecurityOverviewApi.getSecurityOverviewCameras({
        pageNo,
        pageSize,
        includeSnapshot: false, // 🆕 第一阶段不获取快照，快速加载列表
        onlineOnly: false // 显示所有摄像头（包括离线）
      })
      
      if (res && res.list) {
        allCameras.push(...res.list)
        
        // 判断是否还有更多数据
        hasMore = res.list.length === pageSize
        pageNo++
        
        console.log(`[安防概览] 第${pageNo - 1}页: ${res.list.length} 个摄像头`)
      } else {
        hasMore = false
      }
    }
    
    console.log(`[安防概览] ✅ 快速加载完成，共 ${allCameras.length} 个摄像头`)
    
    // 转换为前端格式
    securityCameras.value = allCameras.map((camera: any) => {
      console.log('[安防概览] 处理摄像头:', {
        id: camera.id,
        name: camera.nickname || camera.deviceName,
        online: camera.online,
        status: camera.status
      })
      
      return {
        id: camera.id,
        name: camera.nickname || camera.deviceName || `设备_${camera.id}`,
        location: camera.location || '未知位置',
        status: camera.status,
        statusText: camera.statusText,
        snapshotUrl: 'loading', // 🆕 初始为loading，稍后异步加载
        deviceKey: camera.deviceKey,
        isEmpty: false
      }
    })
    
    console.log(`[安防概览] 成功加载 ${securityCameras.value.length} 个摄像头`)
    
    // 更新设备统计
    deviceStats.value.online = securityCameras.value.filter(c => c.status === 'online').length
    deviceStats.value.offline = securityCameras.value.filter(c => c.status === 'offline').length
    deviceStats.value.rate = securityCameras.value.length > 0
      ? Math.round((deviceStats.value.online / securityCameras.value.length) * 1000) / 10
      : 0
    
    // 🆕 阶段2：异步加载快照（不阻塞页面）
    console.log('[安防概览] 开始异步加载快照...')
    loadSnapshotsAsync()
      
  } catch (error: any) {
    console.error('[安防概览] 加载摄像头失败:', error)
    
    // 友好的错误提示
    let errorMsg = '加载摄像头列表失败'
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      errorMsg = '加载摄像头列表超时，可能是设备较多或网络较慢，请刷新重试'
    } else if (error.message) {
      errorMsg = `加载摄像头列表失败: ${error.message}`
    }
    
    ElMessage.error({
      message: errorMsg,
      duration: 5000,
      showClose: true
    })
    securityCameras.value = []
  }
}

/**
 * 🆕 异步加载快照（批量，不阻塞页面）
 * 
 * 策略：
 * 1. 分批加载（每批20个）
 * 2. 每批之间延迟500ms
 * 3. 失败不影响其他快照
 */
const loadSnapshotsAsync = async () => {
  const batchSize = 20
  const batchDelay = 500 // 毫秒
  
  // 只获取在线设备的快照
  const onlineCameras = securityCameras.value.filter(c => c.status === 'online')
  console.log(`[安防概览] 需要加载 ${onlineCameras.length} 个在线摄像头的快照`)
  
  // 分批处理
  for (let i = 0; i < onlineCameras.length; i += batchSize) {
    const batch = onlineCameras.slice(i, i + batchSize)
    console.log(`[安防概览] 加载快照批次 ${Math.floor(i / batchSize) + 1}/${Math.ceil(onlineCameras.length / batchSize)}`)
    
    // 并发加载当前批次
    await Promise.allSettled(
      batch.map(async (camera) => {
        try {
          const snapshotBase64 = await SecurityOverviewApi.getDeviceSnapshot(camera.id)
          if (snapshotBase64) {
            camera.snapshotUrl = `data:image/jpeg;base64,${snapshotBase64}`
            console.log(`[安防概览] ✅ 快照加载成功: ${camera.name}`)
          } else {
            camera.snapshotUrl = 'error'
            console.warn(`[安防概览] ⚠️ 快照为空: ${camera.name}`)
          }
        } catch (error: any) {
          camera.snapshotUrl = 'error'
          console.warn(`[安防概览] ❌ 快照加载失败: ${camera.name}`, error.message)
        }
      })
    )
    
    // 批次间延迟
    if (i + batchSize < onlineCameras.length) {
      await new Promise(resolve => setTimeout(resolve, batchDelay))
    }
  }
  
  console.log('[安防概览] ✅ 所有快照加载完成')
}

/**
 * 刷新当前显示的摄像头快照
 * 
 * ✅ 直接从ONVIF设备获取快照（绕过ZLMediaKit鉴权问题）
 */
const refreshSnapshots = async () => {
  console.log('[安防概览] 开始刷新快照，摄像头数量:', cameras.value.length)
  
  for (const camera of cameras.value) {
    if (!camera.isEmpty && camera.status === 'online') {
      // ✅ 如果已经有快照（且不是error状态），跳过，避免重复请求
      if (camera.snapshotUrl && camera.snapshotUrl !== 'error') {
        console.log('[安防概览] 快照已缓存，跳过:', camera.id, camera.name)
        continue
      }
      
      try {
        console.log('[安防概览] 请求快照:', camera.id, camera.name)
        
        // 后端返回 Base64 图片
        const snapshotData = await SecurityOverviewApi.getDeviceSnapshot(camera.id)
        
        console.log('[安防概览] 快照响应:', camera.id, typeof snapshotData, snapshotData?.substring(0, 50))
        
        if (snapshotData && typeof snapshotData === 'string' && snapshotData.startsWith('data:image')) {
          camera.snapshotUrl = snapshotData
          console.log('[安防概览] ✅ 快照成功:', camera.id)
        } else {
          console.warn('[安防概览] ⚠️ 快照数据无效:', camera.id)
          camera.snapshotUrl = 'error'
        }
      } catch (error: any) {
        console.error('[安防概览] ❌ 获取快照失败:', camera.id, error?.message || error)
        camera.snapshotUrl = 'error'
      }
    }
  }
  
  console.log('[安防概览] 快照刷新完成')
}

/**
 * 切换到下一组摄像头
 */
const switchToNextPage = () => {
  if (totalPages.value === 0) return
  
  // ✅ 切换前先停止所有播放，释放资源
  stopAllCameras()
  
  currentPage.value = (currentPage.value + 1) % totalPages.value
  console.log(`[安防概览] 切换到第 ${currentPage.value + 1}/${totalPages.value} 组`)
  
  setTimeout(() => {
    refreshSnapshots()
  }, 100)
}

/**
 * 启动自动切换定时器（每1分钟）
 */
const startAutoSwitch = () => {
  if (switchTimer.value) {
    clearInterval(switchTimer.value)
  }
  
  switchTimer.value = setInterval(() => {
    switchToNextPage()
  }, 60000) // 60秒
  
  console.log('[安防概览] 自动切换定时器已启动（每1分钟）')
}

/**
 * 启动快照刷新定时器（每10秒）
 */
const startSnapshotRefresh = () => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
  }
  
  refreshTimer.value = setInterval(() => {
    refreshSnapshots()
  }, 10000) // 10秒
  
  console.log('[安防概览] 快照刷新定时器已启动（每10秒）')
}

/**
 * 停止所有定时器
 */
const stopTimers = () => {
  if (switchTimer.value) {
    clearInterval(switchTimer.value)
    switchTimer.value = null
  }
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
    refreshTimer.value = null
  }
  console.log('[安防概览] 所有定时器已停止')
}

let timeInterval: NodeJS.Timeout

onMounted(async () => {
  console.log('[安防概览] 页面已挂载，开始初始化...')
  
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
  
  // ✅ 加载安防概览摄像头（同时获取快照）
  await loadSecurityCameras()
  
  // ✅ 补充刷新：只获取后端未返回的快照（loading状态）
  const needRefresh = securityCameras.value.some(c => c.snapshotUrl === 'loading')
  if (needRefresh) {
    console.log('[安防概览] 部分快照未返回，补充刷新...')
    await refreshSnapshots()
  } else {
    console.log('[安防概览] ✅ 所有快照已从后端获取，无需补充刷新')
  }
  
  // 启动自动切换（每1分钟）
  if (securityCameras.value.length > 6) {
    startAutoSwitch()
  } else {
    console.log('[安防概览] 摄像头不足7个，不启动自动切换')
  }
  
  // ✅ 已禁用定时快照刷新（使用缓存，避免重复请求）
  // 如需定期刷新，可取消注释以下代码
  // if (securityCameras.value.length > 0) {
  //   startSnapshotRefresh()
  // }
  
  // ✅ 连接 IoT WebSocket（接收实时设备状态、告警事件）
  const userId = userStore.getUser?.id
  if (userId) {
    console.log('[安防概览] 🔌 正在连接 IoT WebSocket...')
    connectIotWs(userId)
  } else {
    console.warn('[安防概览] ⚠️ 用户未登录，无法连接 WebSocket')
  }
  
  console.log('[安防概览] 初始化完成')
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
  stopTimers()
  stopAllCameras() // ✅ 页面销毁时停止所有播放，释放资源
  
  // ✅ 断开 IoT WebSocket 连接
  disconnectIotWs()
  console.log('[安防概览] 🔌 WebSocket 已断开')
  
  console.log('[安防概览] 页面已卸载，资源已释放')
})
</script>

<style lang="scss" scoped>
@use '@/styles/dark-theme.scss';

.security-overview {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 0 10px;

    .page-title {
      color: #ffffff;
      font-size: 28px;
      font-weight: 600;
      margin: 0;
    }

    .system-status {
      display: flex;
      align-items: center;
      color: #ffffff;
      font-size: 14px;

      .status-indicator {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        margin-right: 8px;

        &.online {
          background: #00ff88;
          box-shadow: 0 0 8px #00ff88;
        }
      }

      .status-text {
        margin-right: 20px;
      }

      .current-time {
        font-family: 'Courier New', monospace;
      }
    }
  }

  .main-content {
    display: flex;
    gap: 20px;
    margin-bottom: 20px;

    .left-section {
      flex: 2;

      .video-wall-container {
        background: rgba(255, 255, 255, 0.05);
        border-radius: 12px;
        padding: 20px;
        backdrop-filter: blur(10px);
        border: 1px solid rgba(255, 255, 255, 0.1);

        .section-header {
          display: flex;
          align-items: center;
          color: #ffffff;
          font-size: 18px;
          font-weight: 600;
          margin-bottom: 20px;

          .el-icon {
            margin-right: 8px;
            color: #00d4ff;
          }

          .page-indicator {
            margin-left: auto;
            display: flex;
            align-items: baseline;
            gap: 4px;
            padding: 4px 12px;
            background: rgba(0, 212, 255, 0.1);
            border-radius: 16px;
            font-size: 14px;

            .current-page {
              color: #00d4ff;
              font-weight: bold;
              font-size: 16px;
            }

            .separator {
              color: #64748b;
            }

            .total-pages {
              color: #94a3b8;
            }
          }
        }

        .video-grid {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          grid-template-rows: repeat(2, 1fr);
          gap: 15px;

          .video-item {
            background: rgba(0, 0, 0, 0.3);
            border-radius: 8px;
            overflow: hidden;
            cursor: pointer;
            transition: all 0.3s ease;
            border: 1px solid rgba(255, 255, 255, 0.1);

            &:hover:not(.empty) {
              transform: translateY(-2px);
              box-shadow: 0 8px 25px rgba(0, 212, 255, 0.3);
              border-color: #00d4ff;
              
              // 悬停时播放/暂停图标更明显
              .play-control {
                .play-icon,
                .pause-icon {
                  color: #00d4ff;
                  transform: scale(1.2);
                }
              }
            }

            // 正在播放时的边框效果
            &.playing {
              border-color: #00ff88;
              box-shadow: 0 0 15px rgba(0, 255, 136, 0.4);
            }

            &.empty {
              cursor: default;
              opacity: 0.5;
              background: rgba(15, 23, 42, 0.3);
              border: 1px dashed rgba(100, 116, 139, 0.3);
            }

            .video-placeholder {
              height: 180px;
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              color: #666;
              position: relative;

              .empty-slot,
              .offline-slot,
              .online-slot {
                width: 100%;
                height: 100%;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
              }

              .empty-slot {
                color: #475569;

                .empty-text {
                  margin-top: 10px;
                  color: #64748b;
                  font-size: 13px;
                }
              }

              .offline-slot {
                .camera-info {
                  text-align: center;
                  margin-top: 10px;

                  .camera-name {
                    color: #ffffff;
                    font-weight: 600;
                    margin-bottom: 4px;
                  }

                  .camera-location {
                    color: #888;
                    font-size: 12px;
                  }
                }
              }

              .online-slot {
                position: relative;

                // 视频播放器样式
                .camera-video {
                  position: absolute;
                  top: 0;
                  left: 0;
                  width: 100%;
                  height: 100%;
                  object-fit: cover;
                  z-index: 2;
                }

                // 快照图片样式
                .camera-snapshot {
                  position: absolute;
                  top: 0;
                  left: 0;
                  width: 100%;
                  height: 100%;
                  object-fit: cover;
                  z-index: 1;
                  transition: opacity 0.3s ease;
                  
                  &.hidden {
                    opacity: 0;
                    pointer-events: none;
                  }
                }

                // 播放/暂停控制图标
                .play-control {
                  position: absolute;
                  top: 50%;
                  left: 50%;
                  transform: translate(-50%, -50%);
                  z-index: 10;
                  pointer-events: none;
                  transition: opacity 0.3s ease;
                  
                  .play-icon,
                  .pause-icon {
                    color: rgba(255, 255, 255, 0.9);
                    filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.6));
                    transition: all 0.3s ease;
                  }
                }

                // 加载状态样式
                .loading-slot {
                  display: flex;
                  flex-direction: column;
                  align-items: center;
                  justify-content: center;
                  gap: 10px;
                  
                  .loading-icon {
                    color: #00d4ff;
                    animation: spin 1s linear infinite;
                  }
                  
                  .loading-text {
                    color: #00d4ff;
                    font-size: 14px;
                  }
                }

                .loading-icon {
                  color: #00d4ff;
                  animation: spin 1s linear infinite;
                }
                
                .error-slot {
                  display: flex;
                  flex-direction: column;
                  align-items: center;
                  justify-content: center;
                  gap: 10px;
                  
                  .error-text {
                    color: #ef4444;
                    font-size: 14px;
                    margin-top: 5px;
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

                .camera-info {
                  position: absolute;
                  bottom: 0;
                  left: 0;
                  right: 0;
                  text-align: center;
                  padding: 8px;
                  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);

                  .camera-name {
                    color: #ffffff;
                    font-weight: 600;
                    margin-bottom: 2px;
                    font-size: 13px;
                  }

                  .camera-location {
                    color: #ddd;
                    font-size: 11px;
                  }
                }
              }
            }

            .camera-status {
              padding: 8px 12px;
              display: flex;
              align-items: center;
              font-size: 12px;

              .status-dot {
                width: 6px;
                height: 6px;
                border-radius: 50%;
                margin-right: 6px;
              }

              &.online {
                background: rgba(0, 255, 136, 0.1);
                color: #00ff88;

                .status-dot {
                  background: #00ff88;
                  box-shadow: 0 0 8px #00ff88;
                }
              }

              &.offline {
                background: rgba(239, 68, 68, 0.1);
                color: #ef4444;

                .status-dot {
                  background: #ef4444;
                }
              }
            }
          }
        }
      }
    }

    .right-section {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 20px;

      .alarm-center,
      .device-status {
        background: rgba(255, 255, 255, 0.05);
        border-radius: 12px;
        padding: 20px;
        backdrop-filter: blur(10px);
        border: 1px solid rgba(255, 255, 255, 0.1);

        .section-header {
          display: flex;
          align-items: center;
          color: #ffffff;
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 15px;

          .el-icon {
            margin-right: 8px;
            color: #ff6b6b;
          }
        }
      }

      .alarm-center {
        .alarm-list {
          .alarm-item {
            display: flex;
            align-items: center;
            padding: 10px;
            margin-bottom: 8px;
            border-radius: 6px;
            transition: all 0.3s ease;

            &.high {
              background: rgba(255, 107, 107, 0.1);
              border-left: 3px solid #ff6b6b;
            }

            &.warning {
              background: rgba(255, 193, 7, 0.1);
              border-left: 3px solid #ffc107;
            }

            .alarm-icon {
              margin-right: 10px;
              color: #ff6b6b;
            }

            .alarm-content {
              flex: 1;

              .alarm-title {
                color: #ffffff;
                font-size: 14px;
                margin-bottom: 2px;
              }

              .alarm-time {
                color: #888;
                font-size: 12px;
              }
            }
          }
        }
      }

      .device-status {
        .status-grid {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 15px;

          .status-item {
            text-align: center;

            .status-number {
              font-size: 32px;
              font-weight: 700;
              margin-bottom: 5px;

              &.online {
                color: #00ff88;
              }

              &.offline {
                color: #00d4ff;
              }

              &.alarm {
                color: #ff6b6b;
              }

              &.rate {
                color: #ffc107;
              }
            }

            .status-label {
              color: #888;
              font-size: 12px;
            }
          }
        }
      }
    }
  }

  .bottom-modules {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;

    .module-card {
      background: rgba(255, 255, 255, 0.05);
      border-radius: 12px;
      padding: 20px;
      cursor: pointer;
      transition: all 0.3s ease;
      backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.1);
      display: flex;
      align-items: center;

      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 10px 30px rgba(0, 212, 255, 0.3);
        border-color: #00d4ff;
      }

      &.active {
        border-color: #00d4ff;
        background: rgba(0, 212, 255, 0.1);
      }

      .module-icon {
        margin-right: 15px;
        color: #00d4ff;
      }

      .module-content {
        .module-title {
          color: #ffffff;
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 5px;
        }

        .module-desc {
          color: #888;
          font-size: 12px;
          line-height: 1.4;
        }
      }
    }
  }
}

@media (max-width: 1200px) {
  .security-overview {
    .main-content {
      flex-direction: column;
    }

    .bottom-modules {
      grid-template-columns: 1fr;
    }
  }
}

// ============= 视频播放弹窗样式 =============
.video-player-container {
  width: 100%;
  
  .video-player {
    width: 100%;
    height: auto;
    max-height: 70vh;
    background: #000;
    border-radius: 8px;
  }
  
  .loading-player {
    width: 100%;
    height: 500px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.9);
    border-radius: 8px;
    color: #00d4ff;
    
    .loading-icon {
      color: #00d4ff;
      animation: spin 1s linear infinite;
      margin-bottom: 20px;
    }
    
    @keyframes spin {
      from {
        transform: rotate(0deg);
      }
      to {
        transform: rotate(360deg);
      }
    }
    
    p {
      margin: 0;
      font-size: 16px;
    }
  }
  
  .play-info {
    margin-top: 20px;
    padding: 15px;
    background: rgba(0, 0, 0, 0.3);
    border-radius: 8px;
    
    .info-item {
      display: flex;
      align-items: center;
      margin-bottom: 10px;
      color: #ffffff;
      font-size: 14px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .label {
        min-width: 80px;
        color: #94a3b8;
        font-weight: 600;
      }
      
      .value {
        flex: 1;
        color: #e2e8f0;
        word-break: break-all;
      }
    }
  }
}

// Dialog 样式优化
:deep(.el-dialog) {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border: 1px solid rgba(0, 212, 255, 0.3);
  
  .el-dialog__header {
    padding: 20px 20px 15px;
    border-bottom: 1px solid rgba(0, 212, 255, 0.2);
    
    .el-dialog__title {
      color: #ffffff;
      font-size: 18px;
      font-weight: 600;
    }
    
    .el-dialog__headerbtn {
      .el-dialog__close {
        color: #94a3b8;
        
        &:hover {
          color: #00d4ff;
        }
      }
    }
  }
  
  .el-dialog__body {
    padding: 20px;
  }
}
</style>
