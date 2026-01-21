<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="alarm-record-page">
      <!-- 统计卡片 -->
      <div class="stats-container">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-card total">
              <div class="stat-icon">
                <el-icon><Bell /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.total }}</div>
                <div class="stat-label">今日事件总数</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card alarm">
              <div class="stat-icon">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.alarm }}</div>
                <div class="stat-label">报警事件</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card restore">
              <div class="stat-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.restore }}</div>
                <div class="stat-label">恢复事件</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card other">
              <div class="stat-icon">
                <el-icon><InfoFilled /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.other }}</div>
                <div class="stat-label">其他事件</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 实时事件列表 -->
      <div class="event-list-container">
        <div class="list-header">
          <h3>实时事件记录</h3>
          <div class="header-actions">
            <el-tag :type="wsConnected ? 'success' : 'danger'">
              {{ wsConnected ? '● 已连接' : '● 未连接' }}
            </el-tag>
            <el-button size="small" type="primary" @click="testWebSocket">测试连接</el-button>
            <el-button size="small" @click="clearEvents">清空列表</el-button>
          </div>
        </div>
        
        <div class="event-list" ref="eventListRef">
          <div
            v-for="(event, index) in events"
            :key="event.id || index"
            class="event-item"
            :class="getEventClass(event)"
          >
            <div class="event-index">{{ index + 1 }}</div>
            <div class="event-time">{{ formatEventTime(event.timestamp) }}</div>
            <div class="event-content">
              <span class="event-source">{{ event.paramDesc }}[{{ event.area }}]</span>
              <span class="event-name">{{ event.eventName }}</span>
            </div>
            <div class="event-location" v-if="event.point">
              {{ event.point }}
            </div>
          </div>
          
          <div v-if="events.length === 0" class="empty-state">
            <el-empty description="暂无事件记录" />
          </div>
        </div>
      </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell, Warning, CircleCheck, InfoFilled } from '@element-plus/icons-vue'
import { iotWebSocket } from '@/utils/iotWebSocket'
import { getAlarmEventStats } from '@/api/iot/alarm/event'

// 事件接口定义 - 匹配后端 AlarmEventMessage 字段
interface AlarmEvent {
  id?: number
  hostId?: number
  hostName?: string
  eventCode?: string
  eventType?: string         // 事件类型：ALARM/RESTORE/ARM/DISARM
  title?: string             // 事件描述（对应 eventName）
  level?: string             // 事件级别（对应 eventLevel）
  areaNo?: number            // 分区号
  zoneNo?: number            // 防区号
  isNewEvent?: boolean       // 是否新事件
  createTime?: string        // 创建时间
  timestamp?: number         // 时间戳
  // 以下为前端补充字段
  eventName?: string         // 事件名称（取自 title）
  eventLevel?: string        // 事件级别（取自 level）
  area?: string              // 分区号字符串
  point?: string             // 防区号字符串
  paramDesc?: string         // 设备描述
  isAlarm?: boolean          // 是否报警事件
  isRestore?: boolean        // 是否恢复事件
}

// 统计数据
const stats = reactive({
  total: 0,
  alarm: 0,
  restore: 0,
  other: 0
})

// 事件列表（最多保留100条）
const events = ref<AlarmEvent[]>([])
const MAX_EVENTS = 100

// WebSocket 连接
const wsConnected = ref(false)
const eventListRef = ref<HTMLElement>()

/** 初始化 WebSocket */
const initWebSocket = () => {
  console.log('[PerimeterAlarmRecord] 初始化 WebSocket...')
  console.log('[PerimeterAlarmRecord] 当前连接状态:', iotWebSocket.isConnected())
  
  // 复用全局 IoT WebSocket（/ws/iot?userId=xxx）
  if (!iotWebSocket.isConnected()) {
    console.log('[PerimeterAlarmRecord] WebSocket 未连接，正在连接...')
    iotWebSocket.connect()
  } else {
    console.log('[PerimeterAlarmRecord] WebSocket 已连接')
    wsConnected.value = true
  }
  
  // 注册事件监听
  iotWebSocket.on('connected', handleConnected)
  iotWebSocket.on('alarm_event', handleAlarmEvent)
  
  // 监听所有消息类型（调试用）
  iotWebSocket.on('device_status', (data: any) => {
    console.log('[PerimeterAlarmRecord] 收到 device_status:', data)
  })
  iotWebSocket.on('device_stats', (data: any) => {
    console.log('[PerimeterAlarmRecord] 收到 device_stats:', data)
  })
}

const handleConnected = () => {
  console.log('[PerimeterAlarmRecord] ✅ WebSocket 连接成功!')
  wsConnected.value = true
}

/** 处理报警事件（来自 /ws/iot 的 alarm_event） */
const handleAlarmEvent = (rawEvent: any) => {
  console.log('[PerimeterAlarmRecord] 收到报警事件:', rawEvent)
  
  // 映射后端字段到前端格式
  const event: AlarmEvent = {
    id: rawEvent.id,
    hostId: rawEvent.hostId,
    hostName: rawEvent.hostName || rawEvent.deviceName,
    eventCode: rawEvent.eventCode,
    eventType: rawEvent.eventType || rawEvent.type,
    // 映射字段
    eventName: rawEvent.title || rawEvent.eventName || getEventNameByCode(rawEvent.eventCode),
    eventLevel: rawEvent.level || rawEvent.eventLevel || 'INFO',
    area: rawEvent.areaNo != null ? String(rawEvent.areaNo) : (rawEvent.area || ''),
    point: rawEvent.zoneNo != null ? String(rawEvent.zoneNo) : (rawEvent.point || ''),
    // 时间戳处理
    timestamp: rawEvent.timestamp || (rawEvent.createTime ? new Date(rawEvent.createTime).getTime() : Date.now()),
    // 设备描述
    paramDesc: rawEvent.hostName || rawEvent.deviceName || '报警主机',
    // 事件类型判断
    isAlarm: rawEvent.isNewEvent || rawEvent.eventType === 'ALARM' || rawEvent.type === 'ALARM',
    isRestore: rawEvent.eventType === 'RESTORE' || rawEvent.type === 'RESTORE',
  }
  
  // 添加到列表顶部
  events.value.unshift(event)
  
  // 限制列表长度
  if (events.value.length > MAX_EVENTS) {
    events.value = events.value.slice(0, MAX_EVENTS)
  }
  
  // 更新统计
  updateStats(event)
  
  // 滚动到顶部
  nextTick(() => {
    if (eventListRef.value) {
      eventListRef.value.scrollTop = 0
    }
  })
}

/** 根据事件码获取事件名称 */
const getEventNameByCode = (eventCode?: string): string => {
  if (!eventCode) return '未知事件'
  // 常见事件码映射
  const eventMap: Record<string, string> = {
    '1130': '防区报警',
    '3130': '防区报警恢复',
    '1401': '撤防',
    '3401': '布防',
    '1570': '防区旁路',
    '3570': '防区取消旁路',
    '1973': '单防区布防',
    '3973': '单防区撤防',
    '3441': '居家布防',
  }
  return eventMap[eventCode] || `事件${eventCode}`
}

/** 更新统计 */
const updateStats = (event: AlarmEvent) => {
  stats.total++
  
  // 根据事件类型或级别判断
  const eventType = event.eventType || event.eventLevel
  if (event.isAlarm || eventType === 'ALARM' || event.eventLevel === 'ERROR' || event.eventLevel === 'CRITICAL') {
    stats.alarm++
  } else if (event.isRestore || eventType === 'RESTORE') {
    stats.restore++
  } else {
    stats.other++
  }
}

/** 获取事件样式类 */
const getEventClass = (event: AlarmEvent) => {
  const eventType = event.eventType || ''
  const level = event.eventLevel || ''
  
  // 报警事件
  if (event.isAlarm || eventType === 'ALARM' || level === 'ERROR' || level === 'CRITICAL') {
    return 'event-alarm'
  }
  // 恢复事件
  if (event.isRestore || eventType === 'RESTORE') {
    return 'event-restore'
  }
  // 其他事件（布防、撤防等）
  return 'event-other'
}

/** 格式化事件时间 */
const formatEventTime = (timestamp?: number) => {
  if (!timestamp) return '--'
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/** 测试 WebSocket 连接 */
const testWebSocket = () => {
  const connected = iotWebSocket.isConnected()
  wsConnected.value = connected
  
  if (connected) {
    ElMessage.success('WebSocket 已连接，等待报警事件...')
    // 打印当前监听器状态
    console.log('[PerimeterAlarmRecord] WebSocket 连接状态: 已连接')
  } else {
    ElMessage.warning('WebSocket 未连接，正在尝试重新连接...')
    console.log('[PerimeterAlarmRecord] WebSocket 连接状态: 未连接，尝试重连')
    iotWebSocket.connect()
    
    // 3秒后再次检查
    setTimeout(() => {
      const newConnected = iotWebSocket.isConnected()
      wsConnected.value = newConnected
      if (newConnected) {
        ElMessage.success('WebSocket 重连成功!')
      } else {
        ElMessage.error('WebSocket 连接失败，请检查网络或 Nginx 配置')
        console.error('[PerimeterAlarmRecord] WebSocket 连接失败，可能原因：1.Nginx未配置WS代理 2.后端服务未启动 3.网络问题')
      }
    }, 3000)
  }
}

/** 清空事件列表 */
const clearEvents = () => {
  events.value = []
  stats.total = 0
  stats.alarm = 0
  stats.restore = 0
  stats.other = 0
  ElMessage.success('已清空事件列表')
}

/** 加载今日统计 */
const loadTodayStats = async () => {
  try {
    const data = await getAlarmEventStats()
    stats.total = data.total || 0
    stats.alarm = data.alarm || 0
    stats.restore = data.restore || 0
    stats.other = data.other || 0
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

// 生命周期
onMounted(() => {
  initWebSocket()
  loadTodayStats()
})

onUnmounted(() => {
  wsConnected.value = false
  iotWebSocket.off('connected', handleConnected)
  iotWebSocket.off('alarm_event', handleAlarmEvent)
})
</script>

<style scoped lang="scss">
.alarm-record-page {
  padding: 20px;
  background: #0a0e27;
  min-height: calc(100vh - 140px);
}

/* 统计卡片 */
.stats-container {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 24px;
  border-radius: 12px;
  background: linear-gradient(135deg, #1e2139 0%, #252a47 100%);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.4);
  }
  
  .stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    margin-right: 16px;
  }
  
  .stat-content {
    flex: 1;
  }
  
  .stat-value {
    font-size: 32px;
    font-weight: bold;
    line-height: 1.2;
    margin-bottom: 4px;
  }
  
  .stat-label {
    font-size: 14px;
    opacity: 0.7;
  }
  
  &.total {
    .stat-icon {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
    }
    .stat-value {
      color: #667eea;
    }
  }
  
  &.alarm {
    .stat-icon {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      color: #fff;
    }
    .stat-value {
      color: #f5576c;
    }
  }
  
  &.restore {
    .stat-icon {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      color: #fff;
    }
    .stat-value {
      color: #00f2fe;
    }
  }
  
  &.other {
    .stat-icon {
      background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
      color: #fff;
    }
    .stat-value {
      color: #38f9d7;
    }
  }
}

/* 事件列表容器 */
.event-list-container {
  background: linear-gradient(135deg, #1e2139 0%, #252a47 100%);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  
  h3 {
    margin: 0;
    font-size: 18px;
    color: #fff;
  }
  
  .header-actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

/* 事件列表 */
.event-list {
  max-height: 600px;
  overflow-y: auto;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 3px;
  }
  
  &::-webkit-scrollbar-track {
    background: rgba(0, 0, 0, 0.1);
  }
}

.event-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 8px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.05);
  border-left: 4px solid transparent;
  transition: all 0.3s;
  animation: slideIn 0.3s ease-out;
  
  &:hover {
    background: rgba(255, 255, 255, 0.08);
    transform: translateX(4px);
  }
  
  .event-index {
    width: 40px;
    font-size: 14px;
    color: rgba(255, 255, 255, 0.5);
    text-align: center;
  }
  
  .event-time {
    width: 180px;
    font-size: 13px;
    color: rgba(255, 255, 255, 0.7);
    font-family: 'Courier New', monospace;
  }
  
  .event-content {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 12px;
    
    .event-source {
      font-size: 14px;
      color: rgba(255, 255, 255, 0.6);
    }
    
    .event-name {
      font-size: 14px;
      font-weight: 500;
      color: #fff;
    }
  }
  
  .event-location {
    width: 120px;
    font-size: 13px;
    color: rgba(255, 255, 255, 0.5);
    text-align: right;
  }
  
  &.event-alarm {
    border-left-color: #f5576c;
    
    .event-name {
      color: #f5576c;
    }
  }
  
  &.event-restore {
    border-left-color: #00f2fe;
    
    .event-name {
      color: #00f2fe;
    }
  }
  
  &.event-other {
    border-left-color: #38f9d7;
    
    .event-name {
      color: #38f9d7;
    }
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.empty-state {
  padding: 60px 0;
  text-align: center;
  
  :deep(.el-empty__description) {
    color: rgba(255, 255, 255, 0.5);
  }
}
</style>
