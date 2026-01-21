<template>
  <ContentWrap style="margin-top: 70px">
    <div class="ibms-security dark-theme-page">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-title">
          <Icon icon="ep:video-camera" :size="24" />
          <h1>智慧安防</h1>
        </div>
        <div class="security-status">
          <div class="status-indicator" :class="securityStatus.level">
            <Icon :icon="getStatusIcon(securityStatus.level)" />
            <span>{{ securityStatus.text }}</span>
          </div>
          <div class="last-update"> 最后更新: {{ formatTime(securityStatus.lastUpdate) }} </div>
        </div>
      </div>

      <!-- 安防概览 -->
      <div class="security-overview">
        <div class="overview-card cameras">
          <div class="card-icon">
            <Icon icon="ep:video-camera" />
          </div>
          <div class="card-content">
            <div class="card-title">监控摄像头</div>
            <div class="card-value">
              <span class="total">{{ securityData.cameras.total }}</span>
              <span class="online">在线: {{ securityData.cameras.online }}</span>
            </div>
          </div>
        </div>

        <div class="overview-card alarms">
          <div class="card-icon">
            <Icon icon="ep:warning" />
          </div>
          <div class="card-content">
            <div class="card-title">今日告警</div>
            <div class="card-value">
              <span class="total">{{ securityData.alarms.today }}</span>
              <span class="pending">待处理: {{ securityData.alarms.pending }}</span>
            </div>
          </div>
        </div>

        <div class="overview-card access">
          <div class="card-icon">
            <Icon icon="ep:key" />
          </div>
          <div class="card-content">
            <div class="card-title">门禁点位</div>
            <div class="card-value">
              <span class="total">{{ securityData.access.total }}</span>
              <span class="normal">正常: {{ securityData.access.normal }}</span>
            </div>
          </div>
        </div>

        <div class="overview-card patrol">
          <div class="card-icon">
            <Icon icon="ep:user" />
          </div>
          <div class="card-content">
            <div class="card-title">巡更点位</div>
            <div class="card-value">
              <span class="total">{{ securityData.patrol.total }}</span>
              <span class="completed">已巡: {{ securityData.patrol.completed }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 主要内容区域 -->
      <div class="main-content">
        <!-- 左侧监控区域 -->
        <div class="monitor-panel">
          <div class="panel-header">
            <div class="header-left">
              <h3>实时监控大屏大模大样</h3>
              <div v-if="monitorView === 'grid' && totalPages > 0" class="page-indicator">
                <span class="current-page">{{ currentPage + 1 }}</span>
                <span class="separator">/</span>
                <span class="total-pages">{{ totalPages }}</span>
              </div>
            </div>
            <div class="view-controls">
              <el-radio-group v-model="monitorView" @change="switchMonitorView">
                <el-radio-button label="grid">六宫格</el-radio-button>
                <el-radio-button label="single">单画面</el-radio-button>
                <el-radio-button label="map">地图模式</el-radio-button>
              </el-radio-group>
            </div>
          </div>

          <!-- 六宫格监控视图 -->
          <div v-if="monitorView === 'grid'" class="monitor-grid">
            <div
              v-for="camera in displayCameras"
              :key="camera.id"
              :class="[
                'monitor-item',
                {
                  active: selectedCamera?.id === camera.id,
                  empty: camera.isEmpty
                }
              ]"
              @click="!camera.isEmpty && selectCamera(camera)"
            >
              <div class="monitor-video">
                <!-- 空槽位显示 -->
                <div v-if="camera.isEmpty" class="empty-placeholder">
                  <Icon icon="ep:video-camera" :size="60" />
                  <span>暂无摄像头</span>
                </div>
                <!-- 有设备但离线 -->
                <div v-else-if="!camera.online" class="offline-placeholder">
                  <Icon icon="ep:video-camera-filled" :size="40" />
                  <span>设备离线</span>
                </div>
                <!-- 有设备且在线，显示抓图 -->
                <img
                  v-else-if="camera.snapshotUrl"
                  :src="camera.snapshotUrl"
                  :alt="camera.name"
                  @error="handleImageError"
                />
                <!-- 在线但抓图未加载 -->
                <div v-else class="loading-placeholder">
                  <Icon icon="ep:loading" :size="40" class="loading-icon" />
                  <span>加载中...</span>
                </div>
              </div>
              <div v-if="!camera.isEmpty" class="monitor-info">
                <div class="camera-name">{{ camera.name }}</div>
                <div class="camera-location">{{ camera.location }}</div>
                <div
                  class="camera-status"
                  :class="{ online: camera.online, offline: !camera.online }"
                >
                  {{ camera.online ? '在线' : '离线' }}
                </div>
              </div>
            </div>
          </div>

          <!-- 单画面监控视图 -->
          <div v-if="monitorView === 'single'" class="monitor-single">
            <div class="main-monitor">
              <video
                v-if="selectedCamera?.online"
                :src="selectedCamera.streamUrl"
                autoplay
                muted
              ></video>
              <div v-else class="offline-placeholder">
                <Icon icon="ep:video-camera-filled" :size="80" />
                <span>请选择摄像头或设备离线</span>
              </div>
            </div>
            <div class="camera-controls">
              <div class="ptz-controls">
                <div class="ptz-title">云台控制</div>
                <div class="ptz-buttons">
                  <el-button class="ptz-btn up" @click="controlPTZ('up')">
                    <Icon icon="ep:arrow-up" />
                  </el-button>
                  <div class="ptz-row">
                    <el-button class="ptz-btn left" @click="controlPTZ('left')">
                      <Icon icon="ep:arrow-left" />
                    </el-button>
                    <el-button class="ptz-btn center" @click="controlPTZ('center')">
                      <Icon icon="ep:aim" />
                    </el-button>
                    <el-button class="ptz-btn right" @click="controlPTZ('right')">
                      <Icon icon="ep:arrow-right" />
                    </el-button>
                  </div>
                  <el-button class="ptz-btn down" @click="controlPTZ('down')">
                    <Icon icon="ep:arrow-down" />
                  </el-button>
                </div>
              </div>
              <div class="zoom-controls">
                <el-button @click="controlPTZ('zoom-in')">放大</el-button>
                <el-button @click="controlPTZ('zoom-out')">缩小</el-button>
              </div>
            </div>
          </div>

          <!-- 地图监控视图 -->
          <div v-if="monitorView === 'map'" class="monitor-map">
            <SecurityMap :cameras="cameraList" @camera-selected="selectCamera" />
          </div>
        </div>

        <!-- 右侧控制面板 -->
        <div class="control-panel">
          <el-tabs v-model="activeTab" type="border-card">
            <!-- 摄像头列表 -->
            <el-tab-pane label="摄像头" name="cameras">
              <div class="camera-list">
                <div class="list-header">
                  <el-input
                    v-model="cameraSearch"
                    placeholder="搜索摄像头"
                    :prefix-icon="Search"
                    size="small"
                  />
                </div>
                <div class="camera-tree">
                  <div
                    v-for="camera in filteredCameras"
                    :key="camera.id"
                    :class="[
                      'camera-item',
                      {
                        active: selectedCamera?.id === camera.id,
                        online: camera.online,
                        offline: !camera.online
                      }
                    ]"
                    @click="selectCamera(camera)"
                  >
                    <div class="camera-icon">
                      <Icon icon="ep:video-camera" />
                    </div>
                    <div class="camera-details">
                      <div class="camera-name">{{ camera.name }}</div>
                      <div class="camera-location">{{ camera.location }}</div>
                    </div>
                    <div class="camera-status-dot" :class="{ online: camera.online }"></div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- 告警信息 -->
            <el-tab-pane label="告警" name="alarms">
              <div class="alarm-list">
                <div class="alarm-filters">
                  <el-select v-model="alarmFilter" placeholder="告警类型" size="small">
                    <el-option label="全部" value="" />
                    <el-option label="入侵检测" value="intrusion" />
                    <el-option label="人员聚集" value="crowd" />
                    <el-option label="异常行为" value="behavior" />
                    <el-option label="设备故障" value="device" />
                  </el-select>
                </div>
                <div class="alarm-items">
                  <div
                    v-for="alarm in filteredAlarms"
                    :key="alarm.id"
                    :class="['alarm-item', alarm.level]"
                  >
                    <div class="alarm-icon">
                      <Icon :icon="getAlarmIcon(alarm.type)" />
                    </div>
                    <div class="alarm-content">
                      <div class="alarm-title">{{ alarm.title }}</div>
                      <div class="alarm-desc">{{ alarm.description }}</div>
                      <div class="alarm-time">{{ formatTime(alarm.time) }}</div>
                    </div>
                    <div class="alarm-actions">
                      <el-button size="small" @click="handleAlarm(alarm)">处理</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- 门禁管理 -->
            <el-tab-pane label="门禁" name="access">
              <div class="access-control">
                <div class="access-stats">
                  <div class="stat-item">
                    <span class="label">今日通行:</span>
                    <span class="value">{{ accessStats.todayCount }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="label">异常记录:</span>
                    <span class="value error">{{ accessStats.abnormalCount }}</span>
                  </div>
                </div>
                <div class="access-records">
                  <div class="records-header">
                    <h4>最近通行记录</h4>
                    <el-button size="small" @click="refreshAccessRecords">刷新</el-button>
                  </div>
                  <div class="records-list">
                    <div
                      v-for="record in accessRecords"
                      :key="record.id"
                      :class="['record-item', { abnormal: record.abnormal }]"
                    >
                      <div class="record-avatar">
                        <img v-if="record.avatar" :src="record.avatar" :alt="record.name" />
                        <Icon v-else icon="ep:user" />
                      </div>
                      <div class="record-info">
                        <div class="record-name">{{ record.name }}</div>
                        <div class="record-location">{{ record.location }}</div>
                        <div class="record-time">{{ formatTime(record.time) }}</div>
                      </div>
                      <div
                        class="record-status"
                        :class="{ success: !record.abnormal, error: record.abnormal }"
                      >
                        {{ record.abnormal ? '异常' : '正常' }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- 巡更管理 -->
            <el-tab-pane label="巡更" name="patrol">
              <div class="patrol-management">
                <div class="patrol-progress">
                  <div class="progress-header">
                    <h4>今日巡更进度</h4>
                    <div class="progress-stats">
                      {{ patrolData.completed }}/{{ patrolData.total }}
                    </div>
                  </div>
                  <el-progress
                    :percentage="patrolData.percentage"
                    :color="getProgressColor(patrolData.percentage)"
                  />
                </div>
                <div class="patrol-routes">
                  <div v-for="route in patrolRoutes" :key="route.id" class="route-item">
                    <div class="route-header">
                      <div class="route-name">{{ route.name }}</div>
                      <div class="route-status" :class="route.status">
                        {{ getRouteStatusText(route.status) }}
                      </div>
                    </div>
                    <div class="route-progress">
                      <el-progress
                        :percentage="route.progress"
                        :show-text="false"
                        :color="getProgressColor(route.progress)"
                        size="small"
                      />
                    </div>
                    <div class="route-info">
                      <span>巡更员: {{ route.guard }}</span>
                      <span>{{ route.completedPoints }}/{{ route.totalPoints }} 点位</span>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { ContentWrap } from '@/components/ContentWrap'
import SecurityMap from './components/SecurityMap.vue'
import * as DeviceApi from '@/api/iot/device'

// 响应式数据
const monitorView = ref('grid')
const activeTab = ref('cameras')
const selectedCamera = ref<any>(null)
const cameraSearch = ref('')
const alarmFilter = ref('')

// 安防概览摄像头数据
const securityCameras = ref<any[]>([]) // 所有安防概览摄像头
const currentPage = ref(0) // 当前显示的页码
const switchTimer = ref<any>(null) // 切换定时器
const refreshTimer = ref<any>(null) // 刷新定时器

// 安防状态
const securityStatus = ref({
  level: 'normal', // normal, warning, danger
  text: '系统正常',
  lastUpdate: new Date()
})

// 安防数据概览
const securityData = ref({
  cameras: { total: 48, online: 46 },
  alarms: { today: 3, pending: 1 },
  access: { total: 24, normal: 23 },
  patrol: { total: 12, completed: 8 }
})

// 摄像头列表（用于侧边栏，保留用于地图模式等其他功能）
const cameraList = ref<any[]>([])

// 告警列表
const alarmList = ref([
  {
    id: 1,
    type: 'intrusion',
    level: 'high',
    title: '入侵检测告警',
    description: 'A座大门检测到非授权人员入侵',
    time: new Date(Date.now() - 300000),
    handled: false
  },
  {
    id: 2,
    type: 'crowd',
    level: 'medium',
    title: '人员聚集告警',
    description: '停车场A区检测到人员聚集',
    time: new Date(Date.now() - 600000),
    handled: false
  },
  {
    id: 3,
    type: 'device',
    level: 'low',
    title: '设备离线',
    description: '电梯厅1F摄像头离线',
    time: new Date(Date.now() - 1800000),
    handled: true
  }
])

// 门禁统计
const accessStats = ref({
  todayCount: 1247,
  abnormalCount: 3
})

// 门禁记录
const accessRecords = ref([
  {
    id: 1,
    name: '张三',
    avatar: '',
    location: 'A座大门',
    time: new Date(Date.now() - 120000),
    abnormal: false
  },
  {
    id: 2,
    name: '李四',
    avatar: '',
    location: 'B座侧门',
    time: new Date(Date.now() - 300000),
    abnormal: true
  }
])

// 巡更数据
const patrolData = ref({
  total: 12,
  completed: 8,
  percentage: 67
})

const patrolRoutes = ref([
  {
    id: 1,
    name: '夜间巡更路线A',
    status: 'active',
    progress: 75,
    guard: '王五',
    completedPoints: 6,
    totalPoints: 8
  },
  {
    id: 2,
    name: '白班巡更路线B',
    status: 'completed',
    progress: 100,
    guard: '赵六',
    completedPoints: 4,
    totalPoints: 4
  }
])

// 计算属性
const displayCameras = computed(() => {
  // 六宫格显示6个摄像头，根据当前页码切换
  const startIndex = currentPage.value * 6
  const endIndex = startIndex + 6
  const cameras = securityCameras.value.slice(startIndex, endIndex)

  // ✅ 确保始终返回6个元素（不足的用空对象填充）
  const result = [...cameras]
  while (result.length < 6) {
    result.push({
      id: `empty_${result.length}`,
      name: '',
      location: '',
      online: false,
      snapshotUrl: '',
      streamUrl: '',
      deviceKey: '',
      isEmpty: true // 标记为空槽位
    })
  }

  return result
})

// 总页数
const totalPages = computed(() => {
  return Math.max(1, Math.ceil(securityCameras.value.length / 6))
})

const filteredCameras = computed(() => {
  // 使用安防概览的摄像头列表
  const cameras = securityCameras.value.length > 0 ? securityCameras.value : []
  if (!cameraSearch.value) return cameras
  return cameras.filter(
    (camera) =>
      camera.name?.includes(cameraSearch.value) || camera.location?.includes(cameraSearch.value)
  )
})

const filteredAlarms = computed(() => {
  if (!alarmFilter.value) return alarmList.value
  return alarmList.value.filter((alarm) => alarm.type === alarmFilter.value)
})

// 方法
const getStatusIcon = (level: string) => {
  const icons = {
    normal: 'ep:circle-check',
    warning: 'ep:warning',
    danger: 'ep:circle-close'
  }
  return icons[level] || 'ep:circle-check'
}

const getAlarmIcon = (type: string) => {
  const icons = {
    intrusion: 'ep:warning',
    crowd: 'ep:user',
    behavior: 'ep:view',
    device: 'ep:video-camera'
  }
  return icons[type] || 'ep:warning'
}

const formatTime = (date: Date) => {
  return date.toLocaleString('zh-CN')
}

const switchMonitorView = (view: string) => {
  ElMessage.info(`切换到${view === 'grid' ? '九宫格' : view === 'single' ? '单画面' : '地图'}模式`)
}

const selectCamera = (camera: any) => {
  selectedCamera.value = camera
  if (monitorView.value === 'grid') {
    monitorView.value = 'single'
  }
}

const controlPTZ = (direction: string) => {
  if (!selectedCamera.value) {
    ElMessage.warning('请先选择摄像头')
    return
  }
  ElMessage.info(`云台控制: ${direction}`)
}

const handleAlarm = (alarm: any) => {
  alarm.handled = true
  ElMessage.success('告警已处理')
}

const refreshAccessRecords = () => {
  ElMessage.success('门禁记录已刷新')
}

const getProgressColor = (percentage: number) => {
  if (percentage < 30) return '#ef4444'
  if (percentage < 70) return '#f59e0b'
  return '#22c55e'
}

const getRouteStatusText = (status: string) => {
  const statusMap = {
    active: '进行中',
    completed: '已完成',
    pending: '待开始',
    paused: '已暂停'
  }
  return statusMap[status] || '未知'
}

// ============= 安防概览摄像头相关方法 =============

/**
 * 加载安防概览摄像头列表
 */
const loadSecurityCameras = async () => {
  try {
    console.log('[安防概览] 开始加载摄像头列表...')

    // 获取所有设备（包括在线和离线）
    const res = await DeviceApi.getDeviceList({
      pageNo: 1,
      pageSize: 1000
    })

    console.log('[安防概览] 后端返回数据:', res)

    if (res && res.data && res.data.list) {
      // 筛选出产品配置中包含"安防概览"的设备
      const filteredDevices = res.data.list.filter((device: any) => {
        try {
          // 检查设备配置中是否有"安防概览"标签
          if (device.config) {
            const config =
              typeof device.config === 'string' ? JSON.parse(device.config) : device.config

            const hasFeature =
              config.features?.includes('安防概览') || config.displayConfig?.includes('安防概览')

            if (hasFeature) {
              console.log('[安防概览] 找到设备:', device.deviceName || device.name)
            }
            return hasFeature
          }
          return false
        } catch (e) {
          console.warn('[安防概览] 解析设备配置失败:', device.id, e)
          return false
        }
      })

      securityCameras.value = filteredDevices
        .map((device: any) => {
          try {
            const config = device.config
              ? typeof device.config === 'string'
                ? JSON.parse(device.config)
                : device.config
              : {}

            return {
              id: device.id,
              name: device.deviceName || device.name || `设备_${device.id}`,
              location: device.address || config.location || '未知位置',
              online: device.state === 1 || device.status === 'online',
              snapshotUrl: getSnapshotUrl(device),
              streamUrl: config.streamUrl || '',
              deviceKey: device.deviceKey
            }
          } catch (e) {
            console.error('[安防概览] 转换设备数据失败:', device.id, e)
            return null
          }
        })
        .filter(Boolean)

      console.log(`[安防概览] 成功加载 ${securityCameras.value.length} 个摄像头`)

      // 更新统计数据
      securityData.value.cameras.total = securityCameras.value.length
      securityData.value.cameras.online = securityCameras.value.filter((c) => c.online).length
    } else {
      console.warn('[安防概览] 后端返回数据格式不正确')
      securityCameras.value = []
    }
  } catch (error: any) {
    console.error('[安防概览] 加载摄像头失败:', error)
    ElMessage.error('加载摄像头列表失败: ' + (error?.message || '未知错误'))
    securityCameras.value = []
  }
}

/**
 * 获取设备抓图URL
 */
const getSnapshotUrl = (device: any) => {
  // ONVIF 标准抓图URL（通过后端API代理）
  // 注意：实际使用时可能需要根据不同厂商调整URL格式
  return `/admin-api/iot/device/snapshot/${device.id}?t=${Date.now()}`
}

/**
 * 刷新当前显示的摄像头抓图
 */
const refreshSnapshots = () => {
  displayCameras.value.forEach((camera) => {
    if (camera.online) {
      // 更新抓图URL，添加时间戳强制刷新
      camera.snapshotUrl = getSnapshotUrl({
        id: camera.id,
        address: camera.location,
        config: JSON.stringify({})
      })
    }
  })
}

/**
 * 切换到下一组摄像头
 */
const switchToNextPage = () => {
  if (totalPages.value === 0) return

  currentPage.value = (currentPage.value + 1) % totalPages.value
  console.log(`[安防概览] 切换到第 ${currentPage.value + 1}/${totalPages.value} 组`)

  // 切换后立即刷新抓图
  setTimeout(() => {
    refreshSnapshots()
  }, 100)
}

/**
 * 启动自动切换定时器（每1分钟）
 */
const startAutoSwitch = () => {
  // 清除旧定时器
  if (switchTimer.value) {
    clearInterval(switchTimer.value)
  }

  // 每1分钟切换一组
  switchTimer.value = setInterval(() => {
    switchToNextPage()
  }, 60000) // 60秒

  console.log('[安防概览] 自动切换定时器已启动（每1分钟）')
}

/**
 * 启动抓图刷新定时器（每10秒）
 */
const startSnapshotRefresh = () => {
  // 清除旧定时器
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
  }

  // 每10秒刷新一次抓图
  refreshTimer.value = setInterval(() => {
    refreshSnapshots()
  }, 10000) // 10秒

  console.log('[安防概览] 抓图刷新定时器已启动（每10秒）')
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

/**
 * 图片加载失败处理
 */
const handleImageError = (event: Event) => {
  console.warn('[安防概览] 抓图加载失败:', event)
}

// ============= 生命周期 =============

onMounted(async () => {
  console.log('[安防概览] 页面已挂载，开始初始化...')

  // 加载安防概览摄像头
  await loadSecurityCameras()

  // 初始化选中第一个摄像头（如果有）
  if (securityCameras.value.length > 0) {
    selectedCamera.value = securityCameras.value[0]
  }

  // 启动自动切换（每1分钟）
  if (securityCameras.value.length > 6) {
    startAutoSwitch()
  } else {
    console.log('[安防概览] 摄像头不足7个，不启动自动切换')
  }

  // 启动抓图刷新（每10秒）
  if (securityCameras.value.length > 0) {
    startSnapshotRefresh()
  }

  console.log('[安防概览] 初始化完成')
})

onUnmounted(() => {
  // 组件销毁时清除定时器
  stopTimers()
})
</script>

<style lang="scss" scoped>
@use '@/styles/dark-theme.scss';

.ibms-security {
  padding: 20px;
  background: linear-gradient(135deg, #0a1628 0%, #1e3a8a 50%, #0f172a 100%);
  min-height: auto;
  color: #ffffff;

  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
    padding: 16px 24px;
    background: rgba(15, 23, 42, 0.8);
    backdrop-filter: blur(15px);
    border-radius: 12px;
    border: 1px solid rgba(0, 212, 255, 0.1);

    .header-title {
      display: flex;
      align-items: center;
      gap: 12px;

      .el-icon {
        color: #00d4ff;
      }

      h1 {
        margin: 0;
        font-size: 24px;
        font-weight: 600;
        color: #00d4ff;
      }
    }

    .security-status {
      text-align: right;

      .status-indicator {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        font-weight: 500;
        margin-bottom: 4px;

        &.normal {
          color: #22c55e;
        }

        &.warning {
          color: #f59e0b;
        }

        &.danger {
          color: #ef4444;
        }
      }

      .last-update {
        font-size: 12px;
        color: #94a3b8;
      }
    }
  }

  .security-overview {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 20px;

    .overview-card {
      padding: 20px;
      background: rgba(15, 23, 42, 0.8);
      backdrop-filter: blur(15px);
      border-radius: 12px;
      border: 1px solid rgba(0, 212, 255, 0.2);
      display: flex;
      align-items: center;
      gap: 16px;

      .card-icon {
        width: 50px;
        height: 50px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;

        &.cameras {
          background: linear-gradient(135deg, #00d4ff 0%, #0099cc 100%);
        }
        &.alarms {
          background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
        }
        &.access {
          background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
        }
        &.patrol {
          background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
        }

        .el-icon {
          font-size: 24px;
          color: #ffffff;
        }
      }

      .card-content {
        .card-title {
          font-size: 14px;
          color: #94a3b8;
          margin-bottom: 8px;
        }

        .card-value {
          display: flex;
          flex-direction: column;
          gap: 4px;

          .total {
            font-size: 24px;
            font-weight: bold;
            color: #00d4ff;
          }

          .online,
          .pending,
          .normal,
          .completed {
            font-size: 12px;
            color: #94a3b8;
          }
        }
      }
    }
  }

  .main-content {
    display: flex;
    gap: 20px;
    height: calc(100vh - 320px);

    .monitor-panel {
      flex: 2;
      background: rgba(15, 23, 42, 0.8);
      backdrop-filter: blur(15px);
      border-radius: 12px;
      border: 1px solid rgba(0, 212, 255, 0.1);
      overflow: hidden;

      .panel-header {
        padding: 20px;
        border-bottom: 1px solid rgba(0, 212, 255, 0.1);
        display: flex;
        justify-content: space-between;
        align-items: center;

        .header-left {
          display: flex;
          align-items: center;
          gap: 16px;

          h3 {
            margin: 0;
            color: #00d4ff;
            font-size: 18px;
          }

          .page-indicator {
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
      }

      .monitor-grid {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        grid-template-rows: repeat(2, 1fr);
        gap: 15px;
        padding: 20px;
        height: calc(100% - 80px);

        .monitor-item {
          background: #1e293b;
          border-radius: 8px;
          overflow: hidden;
          cursor: pointer;
          border: 2px solid transparent;
          transition: all 0.3s ease;
          position: relative;

          &:hover:not(.empty),
          &.active {
            border-color: #00d4ff;
            box-shadow: 0 0 20px rgba(0, 212, 255, 0.3);
          }

          &.empty {
            cursor: default;
            opacity: 0.5;
            background: rgba(15, 23, 42, 0.5);
            border: 2px dashed rgba(100, 116, 139, 0.3);

            .monitor-video {
              height: 100% !important; // 空槽位占满整个高度
            }
          }

          .monitor-video {
            height: calc(100% - 60px);
            position: relative;
            background: #0f172a;

            img {
              width: 100%;
              height: 100%;
              object-fit: cover;
            }

            .empty-placeholder,
            .offline-placeholder,
            .loading-placeholder {
              width: 100%;
              height: 100%;
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              gap: 8px;

              span {
                font-size: 13px;
              }
            }

            .empty-placeholder {
              color: #475569;

              .el-icon {
                color: #334155;
              }

              span {
                color: #64748b;
              }
            }

            .offline-placeholder {
              color: #64748b;

              .el-icon {
                color: #ef4444;
              }
            }

            .loading-placeholder {
              color: #00d4ff;

              .loading-icon {
                animation: spin 1s linear infinite;
              }

              @keyframes spin {
                from {
                  transform: rotate(0deg);
                }
                to {
                  transform: rotate(360deg);
                }
              }
            }
          }

          .monitor-info {
            padding: 10px 12px;
            background: rgba(0, 0, 0, 0.6);
            backdrop-filter: blur(10px);
            display: flex;
            flex-direction: column;
            gap: 4px;

            .camera-name {
              font-size: 13px;
              font-weight: 500;
              color: #ffffff;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }

            .camera-location {
              font-size: 11px;
              color: #94a3b8;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }

            .camera-status {
              position: absolute;
              top: 12px;
              right: 12px;
              font-size: 10px;
              padding: 3px 8px;
              border-radius: 12px;
              font-weight: 500;
              backdrop-filter: blur(5px);

              &.online {
                background: rgba(34, 197, 94, 0.9);
                color: #ffffff;
                box-shadow: 0 0 10px rgba(34, 197, 94, 0.5);
              }

              &.offline {
                background: rgba(239, 68, 68, 0.9);
                color: #ffffff;
              }
            }
          }
        }
      }

      .monitor-single {
        padding: 20px;
        height: calc(100% - 80px);
        display: flex;
        gap: 20px;

        .main-monitor {
          flex: 1;
          background: #1e293b;
          border-radius: 8px;
          overflow: hidden;

          video {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }

          .offline-placeholder {
            width: 100%;
            height: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            color: #64748b;

            .el-icon {
              margin-bottom: 16px;
            }

            span {
              font-size: 16px;
            }
          }
        }

        .camera-controls {
          width: 200px;
          display: flex;
          flex-direction: column;
          gap: 20px;

          .ptz-controls {
            .ptz-title {
              color: #00d4ff;
              font-size: 14px;
              margin-bottom: 12px;
            }

            .ptz-buttons {
              display: flex;
              flex-direction: column;
              align-items: center;
              gap: 8px;

              .ptz-btn {
                width: 40px;
                height: 40px;
                padding: 0;
                border-radius: 50%;
              }

              .ptz-row {
                display: flex;
                gap: 8px;
              }
            }
          }

          .zoom-controls {
            display: flex;
            flex-direction: column;
            gap: 8px;
          }
        }
      }

      .monitor-map {
        height: calc(100% - 80px);
      }
    }

    .control-panel {
      width: 400px;

      :deep(.el-tabs) {
        height: 100%;
        background: rgba(15, 23, 42, 0.8);
        backdrop-filter: blur(15px);
        border: 1px solid rgba(0, 212, 255, 0.1);

        .el-tabs__header {
          background: rgba(0, 212, 255, 0.05);
          margin: 0;

          .el-tabs__nav {
            background: transparent;

            .el-tabs__item {
              color: #94a3b8;
              border-color: rgba(0, 212, 255, 0.1);

              &.is-active {
                color: #00d4ff;
                background: rgba(0, 212, 255, 0.1);
              }
            }
          }
        }

        .el-tabs__content {
          height: calc(100% - 60px);
          padding: 20px;
          color: #ffffff;

          .el-tab-pane {
            height: 100%;
          }
        }
      }

      .camera-list {
        height: 100%;
        display: flex;
        flex-direction: column;

        .list-header {
          margin-bottom: 16px;
        }

        .camera-tree {
          flex: 1;
          overflow-y: auto;

          .camera-item {
            padding: 12px;
            border-radius: 8px;
            margin-bottom: 8px;
            display: flex;
            align-items: center;
            gap: 12px;
            cursor: pointer;
            border: 1px solid transparent;
            transition: all 0.3s ease;

            &:hover {
              background: rgba(0, 212, 255, 0.05);
            }

            &.active {
              background: rgba(0, 212, 255, 0.1);
              border-color: #00d4ff;
            }

            .camera-icon {
              width: 32px;
              height: 32px;
              background: rgba(0, 212, 255, 0.2);
              border-radius: 6px;
              display: flex;
              align-items: center;
              justify-content: center;

              .el-icon {
                color: #00d4ff;
              }
            }

            .camera-details {
              flex: 1;

              .camera-name {
                font-size: 14px;
                color: #ffffff;
                margin-bottom: 2px;
              }

              .camera-location {
                font-size: 12px;
                color: #94a3b8;
              }
            }

            .camera-status-dot {
              width: 8px;
              height: 8px;
              border-radius: 50%;
              background: #ef4444;

              &.online {
                background: #22c55e;
              }
            }
          }
        }
      }

      .alarm-list {
        height: 100%;
        display: flex;
        flex-direction: column;

        .alarm-filters {
          margin-bottom: 16px;
        }

        .alarm-items {
          flex: 1;
          overflow-y: auto;

          .alarm-item {
            padding: 16px;
            border-radius: 8px;
            margin-bottom: 12px;
            display: flex;
            gap: 12px;
            border-left: 4px solid;

            &.high {
              background: rgba(239, 68, 68, 0.1);
              border-left-color: #ef4444;
            }

            &.medium {
              background: rgba(245, 158, 11, 0.1);
              border-left-color: #f59e0b;
            }

            &.low {
              background: rgba(34, 197, 94, 0.1);
              border-left-color: #22c55e;
            }

            .alarm-icon {
              width: 32px;
              height: 32px;
              border-radius: 6px;
              display: flex;
              align-items: center;
              justify-content: center;
              background: rgba(239, 68, 68, 0.2);

              .el-icon {
                color: #ef4444;
              }
            }

            .alarm-content {
              flex: 1;

              .alarm-title {
                font-size: 14px;
                color: #ffffff;
                margin-bottom: 4px;
              }

              .alarm-desc {
                font-size: 12px;
                color: #94a3b8;
                margin-bottom: 8px;
              }

              .alarm-time {
                font-size: 11px;
                color: #64748b;
              }
            }

            .alarm-actions {
              display: flex;
              align-items: flex-start;
            }
          }
        }
      }

      .access-control {
        height: 100%;
        display: flex;
        flex-direction: column;

        .access-stats {
          margin-bottom: 20px;
          padding: 16px;
          background: rgba(0, 212, 255, 0.05);
          border-radius: 8px;

          .stat-item {
            display: flex;
            justify-content: space-between;
            margin-bottom: 8px;

            .label {
              color: #94a3b8;
            }

            .value {
              color: #00d4ff;
              font-weight: bold;

              &.error {
                color: #ef4444;
              }
            }
          }
        }

        .access-records {
          flex: 1;

          .records-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;

            h4 {
              margin: 0;
              color: #00d4ff;
            }
          }

          .records-list {
            display: flex;
            flex-direction: column;
            gap: 12px;

            .record-item {
              padding: 12px;
              background: rgba(0, 212, 255, 0.05);
              border-radius: 8px;
              display: flex;
              align-items: center;
              gap: 12px;

              &.abnormal {
                background: rgba(239, 68, 68, 0.1);
                border-left: 3px solid #ef4444;
              }

              .record-avatar {
                width: 40px;
                height: 40px;
                border-radius: 50%;
                background: rgba(0, 212, 255, 0.2);
                display: flex;
                align-items: center;
                justify-content: center;
                overflow: hidden;

                img {
                  width: 100%;
                  height: 100%;
                  object-fit: cover;
                }

                .el-icon {
                  color: #00d4ff;
                }
              }

              .record-info {
                flex: 1;

                .record-name {
                  font-size: 14px;
                  color: #ffffff;
                  margin-bottom: 2px;
                }

                .record-location {
                  font-size: 12px;
                  color: #94a3b8;
                  margin-bottom: 2px;
                }

                .record-time {
                  font-size: 11px;
                  color: #64748b;
                }
              }

              .record-status {
                padding: 4px 8px;
                border-radius: 12px;
                font-size: 12px;

                &.success {
                  background: #22c55e;
                  color: #ffffff;
                }

                &.error {
                  background: #ef4444;
                  color: #ffffff;
                }
              }
            }
          }
        }
      }

      .patrol-management {
        height: 100%;
        display: flex;
        flex-direction: column;

        .patrol-progress {
          margin-bottom: 24px;
          padding: 16px;
          background: rgba(0, 212, 255, 0.05);
          border-radius: 8px;

          .progress-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;

            h4 {
              margin: 0;
              color: #00d4ff;
            }

            .progress-stats {
              color: #94a3b8;
              font-size: 14px;
            }
          }
        }

        .patrol-routes {
          flex: 1;
          display: flex;
          flex-direction: column;
          gap: 16px;

          .route-item {
            padding: 16px;
            background: rgba(0, 212, 255, 0.05);
            border-radius: 8px;

            .route-header {
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 12px;

              .route-name {
                color: #ffffff;
                font-size: 14px;
                font-weight: 500;
              }

              .route-status {
                padding: 4px 8px;
                border-radius: 12px;
                font-size: 12px;

                &.active {
                  background: #3b82f6;
                  color: #ffffff;
                }

                &.completed {
                  background: #22c55e;
                  color: #ffffff;
                }

                &.pending {
                  background: #94a3b8;
                  color: #ffffff;
                }
              }
            }

            .route-progress {
              margin-bottom: 12px;
            }

            .route-info {
              display: flex;
              justify-content: space-between;
              font-size: 12px;
              color: #94a3b8;
            }
          }
        }
      }
    }
  }
}
</style>
