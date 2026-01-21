<template>
  <ContentWrap 
    :body-style="{ padding: '0', height: '100%', display: 'flex', flexDirection: 'column' }" 
    style="height: calc(100vh - var(--page-top-gap, 70px)); padding-top: var(--page-top-gap, 70px); margin-bottom: 0"
  >
    <div class="dark-theme-page">
      <div class="video-preview-container">
        <div class="main-layout">
          <div class="smartpss-layout">
            <!-- 左侧设备树面板 -->
            <div class="left-panel">
              <el-collapse v-model="leftPanelActive" accordion>
                <el-collapse-item name="device">
                  <template #title>
                    <span>设备</span>
                  </template>
                  <div class="search-box">
                    <el-input 
                      v-model="deviceSearchKeyword" 
                      placeholder="搜索通道名称..." 
                      clearable 
                      size="small"
                      @keyup.enter="handleChannelSearch"
                      @clear="handleSearchClear"
                    >
                      <template #prefix>
                        <Icon icon="ep:search" />
                      </template>
                      <template #append>
                        <el-button :icon="Search" @click="handleChannelSearch" />
                      </template>
                    </el-input>
                  </div>
                  <el-tree 
                    :data="cameraTreeData" 
                    :props="treeProps" 
                    :lazy="true" 
                    :load="loadTreeNode" 
                    :accordion="true" 
                    node-key="id" 
                    @node-click="handleCameraSelect" 
                    class="device-tree" 
                    :allow-drag="allowDrag"
                  >
                    <template #default="{ data }">
                      <div 
                        class="tree-node" 
                        :draggable="data.type === 'channel'" 
                        @dragstart="handleDragStart($event, data)"
                        @dblclick="handleChannelDoubleClick(data)"
                      >
                        <Icon v-if="data.type === 'building'" icon="ep:office-building" style="color: #409eff" />
                        <Icon v-else-if="data.type === 'floor'" icon="ep:tickets" style="color: #67c23a" />
                        <Icon v-else-if="data.type === 'area'" icon="ep:location" style="color: #e6a23c" />
                        <Icon v-else-if="data.type === 'channels'" icon="ep:folder-opened" style="color: #909399" />
                        <Icon v-else-if="data.type === 'channel'" icon="ep:video-camera" style="color: #f56c6c" />
                        <Icon v-else icon="ep:video-camera" style="color: #f56c6c" />
                        <span>{{ data.name }}</span>
                      </div>
                    </template>
                  </el-tree>
                </el-collapse-item>
              </el-collapse>
            </div>

            <!-- 中间播放区域 -->
            <div class="center-panel">
              <div class="player-section">
                <!-- 播放器网格 -->
                <div class="player-grid" :class="gridLayoutClass" ref="playerGridRef">
                  <div 
                    v-for="(pane, idx) in panes" 
                    :key="idx" 
                    class="player-pane" 
                    :class="{ active: activePane === idx, 'drag-over': dragOverPane === idx }" 
                    @click="handlePaneClick(idx)" 
                    @drop="handleDrop($event, idx)" 
                    @dragover.prevent="handleDragOver($event, idx)" 
                    @dragleave="handleDragLeave"
                  >
                    <!-- FLV.js 播放器 -->
                    <video 
                      class="pane-video" 
                      :ref="el => setPaneVideoRef(el, idx)" 
                      :data-index="idx" 
                      muted 
                      playsinline 
                      autoplay
                    ></video>
                    
                    <div class="pane-overlay">
                      <!-- 加载中状态 -->
                      <div v-if="pane.isLoading" class="overlay-center loading">
                        <Icon icon="ep:loading" :size="64" class="loading-icon" />
                        <p class="window-label">正在连接流媒体...</p>
                        <p class="tip-text">{{ pane.channel?.channelName || pane.channel?.name }}</p>
                      </div>
                      
                      <!-- 未播放时显示提示信息 -->
                      <div v-else-if="!pane.isPlaying" class="overlay-center idle">
                        <Icon icon="ep:video-pause" :size="64" />
                        <p class="window-label">窗口 {{ idx + 1 }}</p>
                        <p class="tip-text">拖拽通道到此处播放实时视频</p>
                        <p class="tip-text">或双击通道自动添加</p>
                      </div>
                      
                      <!-- 错误状态 -->
                      <div v-if="pane.error" class="overlay-center error">
                        <Icon icon="ep:warning-filled" :size="48" style="color: #f56c6c" />
                        <p class="window-label" style="color: #f56c6c">{{ pane.error }}</p>
                        <el-button size="small" @click.stop="retryPane(idx)">重试</el-button>
                      </div>
                      
                      <!-- 底部通道名称 -->
                      <div v-if="pane.channel && pane.isPlaying" class="overlay-bottom">
                        <span class="device-name">
                          <span class="live-dot"></span>
                          {{ pane.channel.channelName || pane.channel.name }}
                        </span>
                        <span class="protocol-tag" :class="{ 'webrtc': pane.playMode === 'webrtc' }">
                          {{ pane.playMode === 'webrtc' ? 'WebRTC' : 'WS-FLV' }}
                        </span>
                      </div>
                      
                      <!-- 悬停工具栏 -->
                      <div v-if="pane.isPlaying" class="pane-toolbar">
                        <el-dropdown @command="(cmd: string) => handleQualityChange(idx, cmd)" trigger="click">
                          <el-button size="small" :title="currentQuality === 0 ? '高清' : '标清'">
                            <Icon icon="ep:view" />
                          </el-button>
                          <template #dropdown>
                            <el-dropdown-menu>
                              <el-dropdown-item command="0" :class="{ active: currentQuality === 0 }">
                                高清 (主码流)
                              </el-dropdown-item>
                              <el-dropdown-item command="1" :class="{ active: currentQuality === 1 }">
                                标清 (子码流)
                              </el-dropdown-item>
                            </el-dropdown-menu>
                          </template>
                        </el-dropdown>
                        <el-button size="small" @click.stop="handleSnapshot(idx)" title="截图">
                          <Icon icon="ep:camera" />
                        </el-button>
                        <el-button size="small" type="danger" @click.stop="handleStopPlay(idx)" title="停止播放">
                          <Icon icon="ep:video-camera-filled" />
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 底部控制栏 -->
                <div class="playback-controls">
                  <div class="controls-left">
                    <div class="protocol-info">
                      <Icon icon="ep:connection" style="color: #67c23a;" />
                      <span>ZLMediaKit 低延迟模式</span>
                      <el-tag size="small" type="success">延迟 &lt; 500ms</el-tag>
                    </div>
                  </div>
                  
                  <div class="controls-right">
                    <el-button size="small" @click="handleStopAllPlayers" type="danger" title="停止所有播放器" :disabled="!hasPlayingPanes">
                      <Icon icon="ep:video-camera-filled" />
                      停止全部
                    </el-button>
                    
                    <el-select v-model="gridLayout" size="small" style="width: 96px" @change="setLayout" title="分屏布局">
                      <el-option :value="1" label="1×1" />
                      <el-option :value="4" label="2×2" />
                      <el-option :value="6" label="2×3" />
                      <el-option :value="9" label="3×3" />
                      <el-option :value="16" label="4×4" />
                    </el-select>
                    
                    <el-button size="small" @click="handleFullscreen" title="全屏">
                      <Icon icon="ep:full-screen" />
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { ContentWrap } from '@/components/ContentWrap'
import { Icon } from '@/components/Icon'
import { getBuildingList } from '@/api/iot/spatial/building'
import { getFloorListByBuildingId } from '@/api/iot/spatial/floor'
import { getAreaListByFloorId } from '@/api/iot/spatial/area'
import { getChannelPage } from '@/api/iot/channel'
import { getLivePlayUrl, stopStream } from '@/api/iot/video/zlm'
import mpegts from 'mpegts.js'

defineOptions({ name: 'ZlmPreview' })

// ==================== 类型定义 ====================

interface PaneType {
  channel: any | null
  videoEl: HTMLVideoElement | null
  player: mpegts.Player | null
  rtcConnection: RTCPeerConnection | null  // WebRTC 连接
  playMode: 'webrtc' | 'flv' | null  // 当前播放模式
  isPlaying: boolean
  isLoading: boolean
  error: string | null
}

// ==================== 状态定义 ====================

const leftPanelActive = ref<string>('device')
const deviceSearchKeyword = ref('')
const cameraTreeData = ref<any[]>([])
const treeProps = { 
  children: 'children', 
  label: 'name', 
  isLeaf: (data: any) => data.type === 'device' || data.type === 'channel' 
}

// 分屏布局
const gridLayout = ref<number>(6)
const activePane = ref<number>(0)
const dragOverPane = ref<number>(-1)
const playerGridRef = ref<HTMLElement | null>(null)

// 窗格状态
const panes = ref<PaneType[]>([])

// 清晰度设置: 0=主码流/高清, 1=子码流/标清
const currentQuality = ref<number>(0)

// Firefox 浏览器检测 - Firefox 的 MSE 有并发限制
const isFirefox = navigator.userAgent.toLowerCase().includes('firefox')

// 播放队列 - Firefox 需要串行初始化播放器，避免 MSE 并发错误
let playQueue: Promise<void> = Promise.resolve()

// ==================== 智能路由：内网/公网自动选择 ====================

/**
 * 判断当前是否通过内网访问
 * 内网访问：使用内网 ZLMediaKit 地址，节省公网流量
 * 公网访问：使用公网代理/隧道地址
 */
const isIntranetAccess = (): boolean => {
  const hostname = window.location.hostname
  // 内网 IP 段
  if (hostname === 'localhost' || hostname === '127.0.0.1') return true
  if (hostname.startsWith('192.168.')) return true
  if (hostname.startsWith('10.')) return true
  if (hostname.startsWith('172.')) {
    const secondOctet = parseInt(hostname.split('.')[1])
    if (secondOctet >= 16 && secondOctet <= 31) return true
  }
  return false
}

/**
 * 公网 ZLMediaKit 配置
 * 
 * 🔧 配置说明：
 * - 这些配置可以通过 Nginx 或后端 API 动态获取
 * - 也可以在 .env.prod 中配置：VITE_ZLM_PUBLIC_HOST, VITE_ZLM_PUBLIC_RTC_PORT 等
 * - 当前使用动态检测：公网访问时使用当前域名，内网访问时使用后端返回的地址
 */
const PUBLIC_ZLM = {
  // HTTP 相关：使用当前访问的域名（Nginx 会代理到 ZLMediaKit）
  get host() { return window.location.hostname },
  get httpPort() { return window.location.port ? parseInt(window.location.port) : 80 },
  
  // WebRTC UDP 隧道配置（这个必须配置，因为 natapp 分配的端口是固定的）
  // 可以通过环境变量覆盖：import.meta.env.VITE_ZLM_RTC_HOST
  rtcHost: import.meta.env.VITE_ZLM_RTC_HOST || '39.108.87.226',
  rtcPort: parseInt(import.meta.env.VITE_ZLM_RTC_PORT || '48088')
}

/**
 * 转换播放地址：根据内网/公网访问自动选择合适的地址
 */
const adaptPlayUrls = (urls: any): any => {
  if (!urls) return urls
  
  const intranet = isIntranetAccess()
  console.log(`[智能路由] 当前访问方式: ${intranet ? '内网' : '公网'}`)
  
  if (intranet) {
    // 内网访问：直接使用内网地址
    return urls // 后端返回的地址应该就是内网地址
  }
  
  // 公网访问：替换为公网地址
  const adapted = { ...urls }
  const publicHost = PUBLIC_ZLM.host
  const publicPort = PUBLIC_ZLM.httpPort
  const publicAddr = publicPort === 80 || publicPort === 443 ? publicHost : `${publicHost}:${publicPort}`
  
  // 🔒 根据当前页面协议选择 http/https 和 ws/wss
  const isHttps = window.location.protocol === 'https:'
  const httpProtocol = isHttps ? 'https' : 'http'
  const wsProtocol = isHttps ? 'wss' : 'ws'
  
  // 替换 HTTP 相关地址（FLV/HLS/TS）
  const replaceHttpUrl = (url: string): string => {
    if (!url) return url
    // 1. 替换内网地址为公网地址
    let newUrl = url
      .replace(/192\.168\.\d+\.\d+:\d+/g, publicAddr)
      .replace(/192\.168\.\d+\.\d+/g, publicHost)
    // 2. 根据当前页面协议替换 http/https
    newUrl = newUrl.replace(/^http:/, `${httpProtocol}:`)
    return newUrl
  }
  
  // 替换 WebSocket 地址
  const replaceWsUrl = (url: string): string => {
    if (!url) return url
    let newUrl = url
      .replace(/192\.168\.\d+\.\d+:\d+/g, publicAddr)
      .replace(/192\.168\.\d+\.\d+/g, publicHost)
    // 根据当前页面协议替换 ws/wss
    newUrl = newUrl.replace(/^ws:/, `${wsProtocol}:`)
    return newUrl
  }
  
  adapted.wsFlvUrl = replaceWsUrl(urls.wsFlvUrl)
  adapted.flvUrl = replaceHttpUrl(urls.flvUrl)
  adapted.hlsUrl = replaceHttpUrl(urls.hlsUrl)
  adapted.wsFmp4Url = replaceWsUrl(urls.wsFmp4Url)
  adapted.tsUrl = replaceHttpUrl(urls.tsUrl)
  
  // WebRTC 信令走 HTTP（使用当前域名和协议）
  if (urls.webrtcUrl) {
    adapted.webrtcUrl = replaceHttpUrl(urls.webrtcUrl)
  }
  
  console.log(`[智能路由] 地址已转换: ${httpProtocol}://${publicAddr}`)
  return adapted
}

// ==================== 计算属性 ====================

const gridLayoutClass = computed<string>(() => {
  const map: Record<number, string> = {
    1: 'grid-1x1',
    4: 'grid-2x2',
    6: 'grid-2x3',
    9: 'grid-3x3',
    16: 'grid-4x4'
  }
  return map[gridLayout.value] || 'grid-2x3'
})

const hasPlayingPanes = computed(() => panes.value.some(p => p.isPlaying || p.isLoading))

// ==================== 初始化 ====================

const setLayout = (val: number) => {
  stopAllPlayersSilently()
  gridLayout.value = val
  panes.value = Array.from({ length: val }, () => ({
    channel: null,
    videoEl: null,
    player: null,
    rtcConnection: null,
    playMode: null,
    isPlaying: false,
    isLoading: false,
    error: null
  }))
  activePane.value = 0
}

const setPaneVideoRef = (el: any, idx: number) => {
  if (el && panes.value[idx]) {
    panes.value[idx].videoEl = el as HTMLVideoElement
  }
}

// ==================== 设备树加载（仿照 RealTimePreview） ====================

const loadSpaceTree = async () => {
  try {
    const buildings = await getBuildingList()
    cameraTreeData.value = buildings.map((b: any) => ({
      id: `building-${b.id}`,
      name: b.name,
      type: 'building',
      buildingId: b.id
    }))
  } catch (e: any) {
    ElMessage.error('加载空间树失败: ' + (e?.message || e || '未知错误'))
  }
}

const loadTreeNode = async (node: any, resolve: Function) => {
  try {
    const data = node.data
    let children: any[] = []
    
    if (data.type === 'building') {
      children.push({
        id: `channels-building-${data.buildingId}`,
        name: '通道',
        type: 'channels',
        buildingId: data.buildingId
      })
      const floors = await getFloorListByBuildingId(data.buildingId)
      children.push(...floors.map((f: any) => ({
        id: `floor-${f.id}`,
        name: f.name,
        type: 'floor',
        floorId: f.id,
        buildingId: data.buildingId,
        floor: f
      })))
    } else if (data.type === 'floor') {
      children.push({
        id: `channels-floor-${data.floorId}`,
        name: '通道',
        type: 'channels',
        floorId: data.floorId,
        buildingId: data.buildingId
      })
      const areas = await getAreaListByFloorId(data.floorId)
      children.push(...areas.map((a: any) => ({
        id: `area-${a.id}`,
        name: a.name,
        type: 'area',
        areaId: a.id,
        floorId: data.floorId
      })))
    } else if (data.type === 'area') {
      children.push({
        id: `channels-area-${data.areaId}`,
        name: '通道',
        type: 'channels',
        areaId: data.areaId,
        floorId: data.floorId,
        buildingId: data.buildingId
      })
    } else if (data.type === 'channels') {
      const params: any = { pageNo: 1, pageSize: 100 }
      if (data.buildingId) params.buildingId = data.buildingId
      if (data.floorId) params.floorId = data.floorId
      if (data.areaId) params.areaId = data.areaId
      
      const channelsRes = await getChannelPage(params)
      const channels = channelsRes.list || []
      children = channels.map((ch: any) => ({
        id: `channel-${ch.id}`,
        name: ch.channelName || `通道${ch.channelNo}`,
        type: 'channel',
        channelId: ch.id,
        channel: ch
      }))
    }
    
    resolve(children)
  } catch (e: any) {
    ElMessage.error('加载节点失败: ' + (e?.message || e || '未知错误'))
    resolve([])
  }
}

// 搜索通道
const handleChannelSearch = async () => {
  const keyword = deviceSearchKeyword.value.trim()
  if (!keyword) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  try {
    const result: any = await getChannelPage({ 
      channelName: keyword, 
      channelType: 'video', 
      pageNo: 1, 
      pageSize: 100 
    })
    const list = result?.list || []
    if (list.length > 0) {
      cameraTreeData.value = list.map((ch: any) => ({
        id: `channel-${ch.id}`,
        name: ch.channelName || `通道${ch.channelNo}`,
        type: 'channel',
        channelId: ch.id,
        channelNo: ch.channelNo,
        deviceId: ch.deviceId,
        channel: ch
      }))
      ElMessage.success(`找到 ${list.length} 个匹配的视频通道`)
    } else {
      cameraTreeData.value = []
      ElMessage.info('未找到匹配的视频通道')
    }
  } catch (e: any) {
    ElMessage.error('搜索失败: ' + (e?.message || e || '未知错误'))
  }
}

const handleSearchClear = () => {
  deviceSearchKeyword.value = ''
  loadSpaceTree()
}

const handleCameraSelect = (_data: any) => {
  // 点击选中，预留
}

const allowDrag = (node: any) => node.data.type === 'channel'

// ==================== 拖拽操作 ====================

const handleDragStart = (e: DragEvent, data: any) => {
  if (data.type !== 'channel') return
  e.dataTransfer!.effectAllowed = 'copy'
  e.dataTransfer!.setData('channel', JSON.stringify(data))
}

const handleDragOver = (_e: DragEvent, paneIndex: number) => {
  dragOverPane.value = paneIndex
}

const handleDragLeave = () => {
  dragOverPane.value = -1
}

const handleDrop = async (e: DragEvent, paneIndex: number) => {
  e.preventDefault()
  dragOverPane.value = -1
  try {
    const channelData = JSON.parse(e.dataTransfer!.getData('channel'))
    await playChannelInPane(channelData, paneIndex)
  } catch (err) {
    console.error('拖拽失败:', err)
  }
}

// ==================== 双击添加 ====================

const handleChannelDoubleClick = async (data: any) => {
  if (data.type !== 'channel') return
  
  const emptyPaneIndex = panes.value.findIndex(pane => !pane.channel)
  
  if (emptyPaneIndex === -1) {
    ElMessage.warning('所有窗口都已占用，请先关闭一个窗口或拖拽到指定窗口')
    return
  }
  
  try {
    await playChannelInPane(data, emptyPaneIndex)
    ElMessage.success(`已添加到窗口 ${emptyPaneIndex + 1}`)
  } catch (err) {
    console.error('[双击添加] 失败:', err)
    ElMessage.error('添加通道失败')
  }
}

const handlePaneClick = (paneIndex: number) => {
  activePane.value = paneIndex
}

// ==================== WebRTC 播放（超低延迟 ~200ms） ====================

const playWithWebRTC = async (
  pane: PaneType, 
  videoEl: HTMLVideoElement, 
  webrtcUrl: string
): Promise<boolean> => {
  return new Promise((resolve) => {
    try {
      console.log('[WebRTC] 开始建立连接...')
      
      // 1. 创建 RTCPeerConnection
      const pc = new RTCPeerConnection({
        iceServers: [] // 局域网不需要 STUN/TURN
      })
      
      pane.rtcConnection = pc
      
      // 2. 添加 transceiver（只接收视频）
      pc.addTransceiver('video', { direction: 'recvonly' })
      pc.addTransceiver('audio', { direction: 'recvonly' })
      
      // 3. 监听 track 事件
      let streamSet = false
      pc.ontrack = (event) => {
        console.log('[WebRTC] 收到媒体轨道:', event.track.kind)
        if (event.streams && event.streams[0] && !streamSet) {
          streamSet = true
          videoEl.srcObject = event.streams[0]
          // 使用 setTimeout 避免与其他 play() 调用冲突
          setTimeout(() => {
            videoEl.play().catch(() => {})
          }, 100)
        }
      }
      
      // 4. 监听 ICE 连接状态
      pc.oniceconnectionstatechange = () => {
        console.log('[WebRTC] ICE状态:', pc.iceConnectionState)
        if (pc.iceConnectionState === 'connected' || pc.iceConnectionState === 'completed') {
          pane.isPlaying = true
          pane.isLoading = false
          pane.playMode = 'webrtc'
          console.log('[WebRTC] ✅ 连接成功！')
          resolve(true)
        } else if (pc.iceConnectionState === 'failed' || pc.iceConnectionState === 'disconnected') {
          console.warn('[WebRTC] 连接失败，将降级到 FLV')
          resolve(false)
        }
      }
      
      // 5. 创建 offer 并发送到 ZLMediaKit
      pc.createOffer().then(offer => {
        return pc.setLocalDescription(offer)
      }).then(() => {
        // 等待 ICE 收集完成
        return new Promise<void>((resolveIce) => {
          if (pc.iceGatheringState === 'complete') {
            resolveIce()
          } else {
            pc.onicegatheringstatechange = () => {
              if (pc.iceGatheringState === 'complete') {
                resolveIce()
              }
            }
            // 超时处理
            setTimeout(resolveIce, 2000)
          }
        })
      }).then(() => {
        // 发送 offer 到 ZLMediaKit
        const offerSdp = pc.localDescription?.sdp
        if (!offerSdp) {
          throw new Error('SDP 创建失败')
        }
        
        console.log('[WebRTC] 发送 offer 到 ZLMediaKit')
        return fetch(webrtcUrl, {
          method: 'POST',
          headers: { 'Content-Type': 'application/sdp' },
          body: offerSdp
        })
      }).then(response => {
        if (!response.ok) {
          throw new Error(`WebRTC API 返回 ${response.status}`)
        }
        return response.text()
      }).then(async (responseText) => {
        console.log('[WebRTC] 收到响应')
        
        // ZLMediaKit 可能返回 JSON 格式或纯 SDP 格式
        let answerSdp: string
        try {
          // 尝试解析 JSON
          const json = JSON.parse(responseText)
          if (json.code !== 0) {
            throw new Error(json.msg || 'WebRTC 协商失败')
          }
          answerSdp = json.sdp
          console.log('[WebRTC] JSON 格式响应，提取 SDP')
        } catch {
          // 不是 JSON，直接使用（可能是纯 SDP）
          if (responseText.startsWith('v=')) {
            answerSdp = responseText
            console.log('[WebRTC] 纯 SDP 格式响应')
          } else {
            throw new Error('无效的 WebRTC 响应格式')
          }
        }
        
        // 🔧 自适应 SDP 修改：根据访问方式替换 ICE 候选地址
        const intranet = isIntranetAccess()
        
        if (intranet) {
          // 内网访问：将 SDP 中的公网 IP 替换为内网 IP
          const zlmInternalIp = '192.168.1.246'
          const zlmInternalPort = '8000'
          
          if (answerSdp.includes(PUBLIC_ZLM.rtcHost)) {
            console.log(`[WebRTC] 内网访问，替换 IP: ${PUBLIC_ZLM.rtcHost} -> ${zlmInternalIp}`)
            answerSdp = answerSdp.replace(new RegExp(PUBLIC_ZLM.rtcHost.replace(/\./g, '\\.'), 'g'), zlmInternalIp)
          }
          if (answerSdp.includes(`:${PUBLIC_ZLM.rtcPort} `) || answerSdp.includes(` ${PUBLIC_ZLM.rtcPort} `)) {
            console.log(`[WebRTC] 内网访问，替换端口: ${PUBLIC_ZLM.rtcPort} -> ${zlmInternalPort}`)
            answerSdp = answerSdp.replace(new RegExp(`([ :])${PUBLIC_ZLM.rtcPort}( )`, 'g'), `$1${zlmInternalPort}$2`)
          }
        } else {
          // 公网访问：将内网端口替换为 natapp 公网端口
          if (answerSdp.includes(':8000 ') || answerSdp.includes(' 8000 ')) {
            console.log('[WebRTC] 公网访问，替换端口 8000 -> ' + PUBLIC_ZLM.rtcPort)
            answerSdp = answerSdp.replace(/^(m=(?:video|audio) )8000( )/gm, `$1${PUBLIC_ZLM.rtcPort}$2`)
            answerSdp = answerSdp.replace(/( \d+\.\d+\.\d+\.\d+ )8000( typ )/g, `$1${PUBLIC_ZLM.rtcPort}$2`)
          }
        }
        
        return pc.setRemoteDescription({
          type: 'answer',
          sdp: answerSdp
        })
      }).catch(error => {
        console.error('[WebRTC] 建立连接失败:', error)
        resolve(false)
      })
      
      // 超时处理
      setTimeout(() => {
        if (!pane.isPlaying) {
          console.warn('[WebRTC] 连接超时')
          resolve(false)
        }
      }, 5000)
      
    } catch (error) {
      console.error('[WebRTC] 初始化失败:', error)
      resolve(false)
    }
  })
}

// ==================== FLV 播放（备用，延迟 ~1秒） ====================

/**
 * 创建 FLV 播放器的核心函数
 * Firefox 需要等待 MediaSource 完全打开后再初始化下一个播放器
 */
const createFlvPlayerCore = async (
  pane: PaneType, 
  videoEl: HTMLVideoElement, 
  wsFlvUrl: string
): Promise<boolean> => {
  return new Promise((resolve) => {
    try {
      console.log('[FLV] 创建播放器...', isFirefox ? '(Firefox模式)' : '')
      
      if (!mpegts.isSupported()) {
        throw new Error('浏览器不支持 FLV 播放')
      }
      
      const player = mpegts.createPlayer({
        type: 'flv',
        url: wsFlvUrl,
        isLive: true,
        hasAudio: false,
        hasVideo: true
      }, {
        enableWorker: false,
        enableStashBuffer: true,  // Firefox 需要启用缓冲
        stashInitialSize: isFirefox ? 256 : 128,
        lazyLoad: false,
        lazyLoadMaxDuration: 0,
        lazyLoadRecoverDuration: 0,
        deferLoadAfterSourceOpen: false,
        autoCleanupSourceBuffer: true,
        autoCleanupMaxBackwardDuration: isFirefox ? 5 : 3,
        autoCleanupMinBackwardDuration: isFirefox ? 2 : 1,
        liveBufferLatencyChasing: true,
        liveBufferLatencyMaxLatency: isFirefox ? 2.0 : 1.5,
        liveBufferLatencyMinRemain: isFirefox ? 0.5 : 0.3,
        liveSync: true,
        fixAudioTimestampGap: true
      })
      
      player.on(mpegts.Events.ERROR, (errorType: any, errorDetail: any, errorInfo: any) => {
        const errorStr = String(errorDetail || '')
        if (errorStr.includes('MSEError') || errorStr.includes('SourceBuffer') || errorStr.includes('remove')) {
          console.warn(`[FLV] ⚠️ MSE 警告: ${errorDetail}`)
          return
        }
        
        console.error(`[FLV] 错误: ${errorType} - ${errorDetail}`, errorInfo)
        pane.error = `播放错误: ${errorDetail}`
        pane.isPlaying = false
        pane.isLoading = false
      })
      
      player.on(mpegts.Events.MEDIA_INFO, (info: any) => {
        console.log('[FLV] 媒体信息:', info?.videoCodec, info?.width + 'x' + info?.height)
      })
      
      player.attachMediaElement(videoEl)
      player.load()
      
      const playDelay = isFirefox ? 500 : 100
      setTimeout(async () => {
        try {
          await player.play()
          
          pane.player = player
          pane.playMode = 'flv'
          pane.isPlaying = true
          pane.isLoading = false
          
          console.log('[FLV] ✅ 播放成功')
          resolve(true)
        } catch (playError) {
          console.error('[FLV] play() 失败:', playError)
          resolve(false)
        }
      }, playDelay)
      
    } catch (error) {
      console.error('[FLV] 创建播放器失败:', error)
      resolve(false)
    }
  })
}

/**
 * FLV 播放入口 - Firefox 使用串行队列避免 MSE 并发错误
 */
const playWithFLV = async (
  pane: PaneType, 
  videoEl: HTMLVideoElement, 
  wsFlvUrl: string
): Promise<boolean> => {
  console.log('[FLV] 开始播放...')
  
  if (isFirefox) {
    return new Promise((resolve) => {
      playQueue = playQueue.then(async () => {
        const result = await createFlvPlayerCore(pane, videoEl, wsFlvUrl)
        await new Promise(r => setTimeout(r, 300))
        resolve(result)
      })
    })
  } else {
    return createFlvPlayerCore(pane, videoEl, wsFlvUrl)
  }
}

// ==================== ZLMediaKit 播放（核心） ====================

const playChannelInPane = async (channelData: any, paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (!pane) return
  
  // 停止之前的播放
  if (pane.player || pane.rtcConnection) {
    stopPane(paneIndex)
  }
  
  const channel = channelData.channel || channelData
  pane.channel = channel
  pane.isLoading = true
  pane.isPlaying = false
  pane.error = null
  pane.playMode = null
  
  try {
    console.log('[ZLM播放] 开始播放通道:', channel.channelName || channel.name, 'ID:', channel.id || channelData.channelId)
    
    // 1. 调用后端获取播放地址（传递清晰度参数）
    const channelId = channel.id || channelData.channelId
    const quality = currentQuality.value
    const rawPlayUrls = await getLivePlayUrl(channelId, quality)
    
    // 2. 智能路由：根据内网/公网访问自动选择合适的地址
    const playUrls = adaptPlayUrls(rawPlayUrls)
    
    console.log('[ZLM播放] 获取到播放地址:', playUrls, '清晰度:', quality === 0 ? '高清' : '标清')
    
    if (!playUrls || (!playUrls.wsFlvUrl && !playUrls.webrtcUrl)) {
      throw new Error('未获取到有效的播放地址')
    }
    
    // 2. 等待 video 元素渲染
    await nextTick()
    
    const videoEl = pane.videoEl
    if (!videoEl) {
      throw new Error('视频元素未找到')
    }
    
    // 3. 自适应播放：优先 WebRTC，失败则回退 FLV
    // 内网/公网都尝试 WebRTC，SDP 会自动适配
    let success = false
    
    if (playUrls.webrtcUrl) {
      const networkType = isIntranetAccess() ? '内网' : '公网'
      console.log(`[ZLM播放] ${networkType}访问，尝试 WebRTC 模式（延迟 ~200ms）`)
      success = await playWithWebRTC(pane, videoEl, playUrls.webrtcUrl)
    }
    
    if (!success && playUrls.wsFlvUrl) {
      console.log('[ZLM播放] 降级到 FLV 模式（延迟 ~1秒）')
      success = await playWithFLV(pane, videoEl, playUrls.wsFlvUrl)
    }
    
    if (!success) {
      throw new Error('所有播放方式均失败')
    }
    
    console.log(`[ZLM播放] ✅ 窗口 ${paneIndex + 1} 播放成功 [${pane.playMode}]: ${channel.channelName || channel.name}`)
    
  } catch (error: any) {
    console.error(`[ZLM播放] 失败:`, error)
    pane.error = error.message || '播放失败'
    pane.isLoading = false
    pane.isPlaying = false
  }
}

// ==================== 停止播放 ====================

const stopPane = (paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (!pane) return
  
  // 停止 FLV 播放器
  if (pane.player) {
    try {
      pane.player.pause()
      pane.player.unload()
      pane.player.detachMediaElement()
      pane.player.destroy()
    } catch (e) {
      console.warn('[ZLM播放] 停止 FLV 播放器异常:', e)
    }
    pane.player = null
  }
  
  // 停止 WebRTC 连接
  if (pane.rtcConnection) {
    try {
      pane.rtcConnection.close()
    } catch (e) {
      console.warn('[ZLM播放] 停止 WebRTC 连接异常:', e)
    }
    pane.rtcConnection = null
  }
  
  // 清理 video 元素
  if (pane.videoEl) {
    pane.videoEl.srcObject = null
    pane.videoEl.src = ''
  }
  
  // 通知后端停止流（可选，节省资源）
  if (pane.channel) {
    const channelId = pane.channel.id || pane.channel.channelId
    if (channelId) {
      stopStream(channelId).catch(() => {})
    }
  }
  
  pane.playMode = null
  pane.isPlaying = false
  pane.isLoading = false
  pane.error = null
  pane.channel = null
}

const handleStopPlay = (paneIndex: number) => {
  stopPane(paneIndex)
  ElMessage.success('已停止播放')
}

// 切换清晰度
const handleQualityChange = async (paneIndex: number, quality: string) => {
  const newQuality = parseInt(quality)
  if (newQuality === currentQuality.value) return
  
  currentQuality.value = newQuality
  
  const pane = panes.value[paneIndex]
  if (pane && pane.channel) {
    // 重新播放以应用新的清晰度
    ElMessage.info(newQuality === 0 ? '切换到高清' : '切换到标清')
    await playChannelInPane(pane.channel, paneIndex)
  }
}

const retryPane = (paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (pane.channel) {
    const channel = pane.channel
    pane.error = null
    playChannelInPane({ channel }, paneIndex)
  }
}

const stopAllPlayersSilently = () => {
  panes.value.forEach((_, idx) => stopPane(idx))
}

const handleStopAllPlayers = async () => {
  const activeCount = panes.value.filter(pane => pane.isPlaying || pane.isLoading).length
  
  if (activeCount === 0) {
    ElMessage.warning('当前没有正在播放或加载的视频')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要停止所有正在播放的视频吗？（共 ${activeCount} 个窗口）`,
      '停止所有播放器',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    stopAllPlayersSilently()
    ElMessage.success(`已停止所有播放器（${activeCount}个窗口）`)
  } catch {
    // 用户取消
  }
}

// ==================== 截图 ====================

const handleSnapshot = (paneIndex: number) => {
  const pane = panes.value[paneIndex]
  
  if (!pane || !pane.isPlaying || !pane.videoEl) {
    ElMessage.warning('当前窗口没有正在播放的视频')
    return
  }
  
  try {
    const videoEl = pane.videoEl
    const canvas = document.createElement('canvas')
    canvas.width = videoEl.videoWidth || 1920
    canvas.height = videoEl.videoHeight || 1080
    
    const ctx = canvas.getContext('2d')
    if (!ctx) {
      throw new Error('无法创建Canvas上下文')
    }
    
    ctx.drawImage(videoEl, 0, 0, canvas.width, canvas.height)
    
    const link = document.createElement('a')
    const channelName = pane.channel?.channelName || pane.channel?.name || 'snapshot'
    link.download = `${channelName}_${Date.now()}.png`
    link.href = canvas.toDataURL('image/png')
    link.click()
    
    ElMessage.success('截图已保存')
  } catch (e: any) {
    console.error('[ZLM截图] 失败:', e)
    ElMessage.error('截图失败: ' + (e?.message || e))
  }
}

// ==================== 全屏 ====================

const handleFullscreen = () => {
  const el = playerGridRef.value
  if (el && (el as any).requestFullscreen) {
    (el as any).requestFullscreen()
  }
}

// ==================== 生命周期 ====================

onMounted(async () => {
  setLayout(gridLayout.value)
  await loadSpaceTree()
})

onUnmounted(() => {
  stopAllPlayersSilently()
})
</script>

<style lang="scss" scoped>
@use '@/styles/dark-theme.scss';

.video-preview-container {
  height: 100%;
  display: flex;
  flex-direction: column;

  .main-layout {
    flex: 1;
    overflow: hidden;

    .smartpss-layout {
      height: 100%;
      display: flex;
      gap: 10px;
      padding: 10px;

      .left-panel {
        width: 240px;
        background: #1e1e1e;
        border: 1px solid #3a3a3a;
        border-radius: 4px;
        display: flex;
        flex-direction: column;
        overflow: hidden;

        :deep(.el-collapse) { background: transparent; border: none; }
        :deep(.el-collapse-item__header) { 
          background: #1e1e1e; 
          color: #e0e0e0; 
          border: 1px solid #3a3a3a; 
          border-radius: 4px; 
          padding: 8px 12px; 
        }
        :deep(.el-collapse-item__wrap) { 
          background: #1e1e1e; 
          border: 1px solid #3a3a3a; 
          border-top: none; 
          border-radius: 0 0 4px 4px; 
        }
        :deep(.el-collapse-item__content) { padding: 8px 0 12px; }

        .search-box {
          padding: 8px 12px;
        }

        .device-tree {
          flex: 1;
          overflow-y: auto;
          padding: 8px;
          background: transparent;

          :deep(.el-tree-node__content) {
            height: 32px;
            &:hover { background: rgba(64, 158, 255, 0.1); }
          }
        }

        .tree-node {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 13px;
          cursor: pointer;
        }
      }

      .center-panel {
        flex: 1;
        display: flex;
        flex-direction: column;
        background: #1e1e1e;
        border: 1px solid #3a3a3a;
        border-radius: 4px;
        overflow: hidden;

        .player-section {
          flex: 1;
          display: flex;
          flex-direction: column;
          overflow: hidden;
          padding: 16px 16px 4px 16px;

          .player-grid {
            flex: 1;
            display: grid;
            gap: 8px;
            background: #000;
            min-height: 0;
            padding: 8px;
            border-radius: 4px;

            &.grid-1x1 { grid-template-columns: 1fr; grid-template-rows: 1fr; }
            &.grid-2x2 { grid-template-columns: repeat(2, 1fr); grid-template-rows: repeat(2, 1fr); }
            &.grid-2x3 { grid-template-columns: repeat(3, 1fr); grid-template-rows: repeat(2, 1fr); }
            &.grid-3x3 { grid-template-columns: repeat(3, 1fr); grid-template-rows: repeat(3, 1fr); }
            &.grid-4x4 { grid-template-columns: repeat(4, 1fr); grid-template-rows: repeat(4, 1fr); }

            .player-pane {
              position: relative;
              background: #0a0a0a;
              border: 2px solid #2f2f2f;
              border-radius: 4px;
              overflow: hidden;
              cursor: pointer;
              transition: border-color 0.2s;

              &.active {
                border-color: #409eff;
                box-shadow: 0 0 8px rgba(64, 158, 255, 0.35);
              }

              &.drag-over {
                border-color: #67c23a;
                background: rgba(103, 194, 58, 0.1);
              }

              .pane-video {
                width: 100%;
                height: 100%;
                background: #0e0e0e;
                object-fit: contain;
              }

              .pane-overlay {
                position: absolute;
                inset: 0;
                pointer-events: none;
                z-index: 1;

                .overlay-center {
                  position: absolute;
                  inset: 0;
                  display: flex;
                  flex-direction: column;
                  align-items: center;
                  justify-content: center;
                  gap: 8px;
                  color: #7a9aba;

                  .window-label { margin: 0; font-size: 14px; font-weight: 500; color: #a5c0db; }
                  .tip-text { margin: 0; font-size: 12px; color: #6c88a3; }

                  &.loading {
                    .loading-icon {
                      animation: rotate 1.5s linear infinite;
                      color: #409eff;
                    }
                    .window-label { color: #409eff; font-weight: 600; }
                  }

                  &.error {
                    pointer-events: auto;
                  }
                }

                .overlay-bottom {
                  position: absolute;
                  bottom: 0;
                  left: 0;
                  right: 0;
                  padding: 8px 12px;
                  background: linear-gradient(0deg, rgba(0,0,0,0.8) 0%, transparent 100%);
                  display: flex;
                  justify-content: space-between;
                  align-items: center;

                  .device-name { 
                    color: #fff; 
                    font-size: 12px;
                    display: flex;
                    align-items: center;
                    gap: 6px;

                    .live-dot {
                      width: 8px;
                      height: 8px;
                      border-radius: 50%;
                      background: #67c23a;
                      animation: pulse 1.5s infinite;
                    }
                  }

                  .protocol-tag {
                    font-size: 10px;
                    padding: 2px 6px;
                    background: rgba(103, 194, 58, 0.3);
                    border: 1px solid rgba(103, 194, 58, 0.5);
                    border-radius: 3px;
                    color: #67c23a;
                    
                    &.webrtc {
                      background: rgba(64, 158, 255, 0.3);
                      border: 1px solid rgba(64, 158, 255, 0.5);
                      color: #409eff;
                    }
                  }
                }

                .pane-toolbar {
                  position: absolute;
                  top: 8px;
                  right: 8px;
                  display: flex;
                  gap: 6px;
                  opacity: 0;
                  transition: opacity 0.2s;
                  pointer-events: auto;
                  z-index: 10;

                  .el-button {
                    background: rgba(0, 0, 0, 0.7);
                    border-color: rgba(255, 255, 255, 0.2);
                    color: #fff;

                    &:hover {
                      background: rgba(64, 158, 255, 0.8);
                      border-color: #409eff;
                    }
                  }
                  
                  .el-dropdown .el-button {
                    background: rgba(0, 0, 0, 0.7);
                    border-color: rgba(255, 255, 255, 0.2);
                    color: #fff;
                  }
                }
              }

              &:hover .pane-toolbar {
                opacity: 1;
              }
            }
          }

          .playback-controls {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 8px 12px;
            background: #1e1e1e;
            border-top: 1px solid #3a3a3a;
            margin-top: 8px;

            .controls-left {
              display: flex;
              align-items: center;
              gap: 8px;

              .protocol-info {
                display: flex;
                align-items: center;
                gap: 8px;
                padding: 4px 12px;
                background: rgba(103, 194, 58, 0.1);
                border: 1px solid rgba(103, 194, 58, 0.3);
                border-radius: 4px;
                font-size: 13px;
                color: #a5c0db;
              }
            }

            .controls-right {
              display: flex;
              align-items: center;
              gap: 8px;
            }
          }
        }
      }
    }
  }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
</style>
