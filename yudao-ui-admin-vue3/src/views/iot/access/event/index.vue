<template>
<ContentWrap
    :body-style="{
      padding: '10px',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      backgroundColor: 'var(--el-bg-color)'
    }"
    style="height: calc(100vh - var(--page-top-gap, 70px)); padding-top: var(--page-top-gap, 70px)"
  >
  <div class="access-event-container">
    <ContentWrap
      :body-style="{ padding: '0', height: '100%', display: 'flex', flexDirection: 'column' }"
    >
      <!-- 统计概览栏 (Requirements: 8.1, 8.2, 8.3) -->
      <div class="statistics-bar">
        <div 
          class="stat-card" 
          :class="{ active: !queryParams.eventCategory }"
          @click="handleStatCardClick('')"
        >
          <div class="stat-value">{{ statistics.total }}</div>
          <div class="stat-label">今日总事件</div>
        </div>
        <div 
          class="stat-card alarm" 
          :class="{ active: queryParams.eventCategory === EventCategory.ALARM }"
          @click="handleStatCardClick(EventCategory.ALARM)"
        >
          <el-icon class="stat-icon"><Warning /></el-icon>
          <div class="stat-value">{{ statistics.alarmCount }}</div>
          <div class="stat-label">报警事件</div>
        </div>
        <div 
          class="stat-card abnormal" 
          :class="{ active: queryParams.eventCategory === EventCategory.ABNORMAL }"
          @click="handleStatCardClick(EventCategory.ABNORMAL)"
        >
          <el-icon class="stat-icon"><CircleClose /></el-icon>
          <div class="stat-value">{{ statistics.abnormalCount }}</div>
          <div class="stat-label">异常事件</div>
        </div>
        <div 
          class="stat-card normal" 
          :class="{ active: queryParams.eventCategory === EventCategory.NORMAL }"
          @click="handleStatCardClick(EventCategory.NORMAL)"
        >
          <el-icon class="stat-icon"><CircleCheck /></el-icon>
          <div class="stat-value">{{ statistics.normalCount }}</div>
          <div class="stat-label">正常事件</div>
        </div>
      </div>

      <!-- 实时监控状态栏 -->
      <div class="realtime-status-bar">
        <div class="status-left">
          <el-tag :type="connected ? 'success' : 'danger'" effect="dark" size="small">
            <el-icon class="status-icon"><component :is="connected ? 'Connection' : 'Disconnection'" /></el-icon>
            {{ connected ? '实时监控中' : '连接断开' }}
          </el-tag>
          <span v-if="reconnectAttempts > 0" class="reconnect-info">
            (重连中: {{ reconnectAttempts }}次)
          </span>
          <el-button v-if="!connected" type="primary" link size="small" @click="handleReconnect">
            <el-icon><Refresh /></el-icon>重新连接
          </el-button>
        </div>
        <div class="status-right">
          <el-switch
            v-model="realtimeEnabled"
            active-text="实时更新"
            inactive-text="暂停"
            @change="handleRealtimeToggle"
          />
          <el-badge :value="newEventCount" :hidden="newEventCount === 0" class="new-event-badge">
            <el-button type="primary" size="small" @click="scrollToTop" :disabled="newEventCount === 0">
              <el-icon><Top /></el-icon>新事件
            </el-button>
          </el-badge>
        </div>
      </div>

      <!-- 搜索栏 -->
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px" class="search-form">
        <el-form-item label="设备" prop="deviceId">
          <el-select v-model="queryParams.deviceId" placeholder="请选择设备" clearable style="width: 180px">
            <el-option v-for="item in deviceList" :key="item.id" :label="item.deviceName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="事件类别" prop="eventCategory">
          <el-select 
            v-model="queryParams.eventCategory" 
            placeholder="请选择类别" 
            clearable 
            style="width: 130px"
            @change="handleCategoryChange"
          >
            <el-option 
              v-for="item in AccessEventCategoryOptions" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="事件类型" prop="eventType">
          <el-select v-model="queryParams.eventType" placeholder="请选择事件类型" clearable style="width: 180px">
            <el-option v-for="item in filteredEventTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="验证结果" prop="verifyResult">
          <el-select v-model="queryParams.verifyResult" placeholder="请选择结果" clearable style="width: 100px">
            <el-option v-for="item in AccessVerifyResultOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 340px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery"><Icon icon="ep:search" class="mr-5px" />搜索</el-button>
          <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" />重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 数据表格 -->
      <div class="table-scroll">
        <el-table 
          ref="tableRef"
          v-loading="loading" 
          :data="displayList" 
          stripe
          :row-class-name="getRowClassName"
          @row-click="handleRowClick"
        >
          <el-table-column label="事件时间" prop="eventTime" width="170">
            <template #default="{ row }">{{ formatEventTime(row.eventTime) }}</template>
          </el-table-column>
          <el-table-column label="设备名称" prop="deviceName" width="140" />
          <el-table-column label="通道名称" prop="channelName" width="100" />
          <el-table-column label="人员姓名" prop="personName" width="90">
            <template #default="{ row }">{{ row.personName || '-' }}</template>
          </el-table-column>
          <!-- 事件类别列 (Requirements: 4.1) -->
          <el-table-column label="事件类别" width="100">
            <template #default="{ row }">
              <el-tag :type="getEventCategoryTagType(row.eventType)" size="small">
                {{ getEventCategoryLabel(row.eventType) }}
              </el-tag>
            </template>
          </el-table-column>
          <!-- 事件类型列 (Requirements: 4.2) -->
          <el-table-column label="事件类型" prop="eventType" width="150">
            <template #default="{ row }">
              <span :class="getEventTypeClass(row.eventType)">
                {{ getEventTypeNameDisplay(row.eventType) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="卡号" prop="cardNo" width="120">
            <template #default="{ row }">{{ row.cardNo || '-' }}</template>
          </el-table-column>
          <el-table-column label="验证结果" prop="verifyResult" width="90">
            <template #default="{ row }">
              <el-tooltip 
                v-if="row.failReason" 
                :content="row.failReason" 
                placement="top"
              >
                <el-tag :type="getVerifyResultType(row)" size="small">
                  {{ getVerifyResultLabel(row) }}
                </el-tag>
              </el-tooltip>
              <el-tag v-else :type="getVerifyResultType(row)" size="small">
                {{ getVerifyResultLabel(row) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="抓拍图片" width="80">
            <template #default="{ row }">
              <el-image 
                v-if="row.captureUrl || row.snapshotUrl" 
                :src="row.captureUrl || row.snapshotUrl" 
                :preview-src-list="[row.captureUrl || row.snapshotUrl]" 
                style="width: 40px; height: 40px" 
                fit="cover" 
              />
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="来源" width="70">
            <template #default="{ row }">
              <el-tag v-if="row.isRealtime" type="success" size="small" effect="plain">实时</el-tag>
              <el-tag v-else type="info" size="small" effect="plain">历史</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 分页 -->
      <Pagination v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </ContentWrap>
  </div>
  </ContentWrap>
</template>


<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed, nextTick } from 'vue'
import { 
  AccessEventLogApi, 
  AccessDeviceApi, 
  AccessEventCategoryOptions,
  AccessVerifyResultOptions, 
  EventCategory,
  getEventCategory,
  getEventTypeName,
  getCategoryStyle,
  getEventTypeOptionsByCategory,
  type AccessEventLogVO, 
  type AccessDeviceVO 
} from '@/api/iot/access'
import { ContentWrap } from '@/components/ContentWrap'
import Pagination from '@/components/Pagination/index.vue'
import { formatDateTime } from '@/utils/formatTime'
import { useAccessEventWebSocket, type AccessEventPushMessage } from './useAccessEventWebSocket'
import { 
  calculateStatistics, 
  updateStatistics, 
  createEmptyStatistics,
  type EventStatistics 
} from '@/utils/accessEventStatistics'
import { ElMessage, ElNotification } from 'element-plus'
import { Connection, Disconnection, Refresh, Top, Warning, CircleClose, CircleCheck } from '@element-plus/icons-vue'

defineOptions({ name: 'AccessEvent' })

const loading = ref(false)
const list = ref<AccessEventLogVO[]>([])
const realtimeEvents = ref<AccessEventLogVO[]>([]) // 实时事件列表
const total = ref(0)
const deviceList = ref<AccessDeviceVO[]>([])
const dateRange = ref<string[]>([])
const queryFormRef = ref()
const tableRef = ref()
const realtimeEnabled = ref(true) // 是否启用实时更新
const newEventCount = ref(0) // 新事件计数
const maxRealtimeEvents = 50 // 最大实时事件数量
const statistics = ref<EventStatistics>(createEmptyStatistics()) // 统计数据

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  deviceId: undefined as number | undefined,
  eventCategory: '' as string, // 事件类别筛选
  eventType: undefined as string | undefined, // 事件类型代码（字符串）
  verifyResult: undefined as number | undefined,
  startTime: undefined as string | undefined,
  endTime: undefined as string | undefined
})

// 根据选中的类别过滤事件类型选项
const filteredEventTypeOptions = computed(() => {
  return getEventTypeOptionsByCategory(queryParams.eventCategory as EventCategory | '')
})

// 合并显示列表：实时事件 + 历史事件
const displayList = computed(() => {
  if (realtimeEnabled.value && realtimeEvents.value.length > 0) {
    // 实时模式：显示实时事件在前，历史事件在后
    return [...realtimeEvents.value, ...list.value]
  }
  return list.value
})

// WebSocket 连接 (Requirements: 5.2, 5.4)
const { connected, reconnectAttempts, connect, disconnect, reconnect } = useAccessEventWebSocket({
  autoConnect: true,
  reconnectInterval: 5000,
  maxReconnectAttempts: 10,
  heartbeatInterval: 30000,
  onEvent: handleAccessEvent,
  onConnectionChange: (isConnected) => {
    if (isConnected) {
      ElMessage.success('实时监控连接成功')
    } else {
      ElMessage.warning('实时监控连接断开')
    }
  },
  onError: (error) => {
    console.error('WebSocket 错误:', error)
  }
})

/**
 * 处理实时门禁事件 (Requirements: 3.2, 3.5)
 */
function handleAccessEvent(event: AccessEventPushMessage) {
  // 计算事件类别
  const category = getEventCategory(event.eventType)
  
  if (!realtimeEnabled.value) {
    // 暂停模式下只计数
    newEventCount.value++
    return
  }

  // 转换为列表显示格式
  const eventLog: AccessEventLogVO = {
    id: event.eventId,
    eventTime: normalizeEventTime(event.eventTime, event.timestamp),
    deviceId: event.deviceId,
    deviceName: event.deviceName,
    channelId: event.channelId,
    channelName: event.channelName,
    personId: event.personId,
    personName: event.personName,
    personCode: event.personCode,
    eventType: event.eventType,
    cardNo: event.cardNo,
    verifyResult: event.verifyResult,
    success: event.success,
    verifyResultDesc: event.verifyResultDesc,
    verifyMode: event.verifyMode,
    failReason: event.failReason,
    captureUrl: event.captureUrl,
    temperature: event.temperature,
    maskStatus: event.maskStatus,
    isRealtime: true, // 标记为实时事件
    _highlight: true // 高亮标记
  }

  // 插入到实时事件列表顶部
  realtimeEvents.value.unshift(eventLog)

  // 限制实时事件数量
  if (realtimeEvents.value.length > maxRealtimeEvents) {
    realtimeEvents.value = realtimeEvents.value.slice(0, maxRealtimeEvents)
  }

  // 更新统计数据 (Requirements: 8.2)
  statistics.value = updateStatistics(statistics.value, eventLog)

  // 报警事件通知 (Requirements: 3.5)
  if (category === EventCategory.ALARM) {
    ElNotification({
      title: '报警事件',
      message: `${event.deviceName} - ${getEventTypeName(event.eventType)}`,
      type: 'error',
      duration: 5000
    })
  }

  // 3秒后移除高亮
  setTimeout(() => {
    const idx = realtimeEvents.value.findIndex(e => e.id === eventLog.id)
    if (idx !== -1) {
      realtimeEvents.value[idx]._highlight = false
    }
  }, 3000)

  console.log('[AccessEvent] 📨 收到实时事件:', getEventTypeName(event.eventType), event.personName)
}

/**
 * 手动重连
 */
function handleReconnect() {
  reconnect()
}

/**
 * 切换实时更新
 */
function handleRealtimeToggle(enabled: boolean) {
  if (enabled) {
    newEventCount.value = 0
    ElMessage.info('已开启实时更新')
  } else {
    ElMessage.info('已暂停实时更新')
  }
}

/**
 * 滚动到顶部
 */
function scrollToTop() {
  newEventCount.value = 0
  realtimeEnabled.value = true
  nextTick(() => {
    tableRef.value?.$el?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  })
}

/**
 * 获取行样式类名
 */
function getRowClassName({ row }: { row: AccessEventLogVO }) {
  if (row._highlight) {
    return 'highlight-row'
  }
  if (row.isRealtime) {
    return 'realtime-row'
  }
  return ''
}

/**
 * 行点击事件
 */
function handleRowClick(row: AccessEventLogVO) {
  // 可以在这里添加查看详情的逻辑
  console.log('点击事件:', row)
}

/**
 * 统计卡片点击 (Requirements: 8.3)
 */
function handleStatCardClick(category: string) {
  queryParams.eventCategory = category
  queryParams.eventType = undefined // 清空事件类型
  handleQuery()
}

/**
 * 事件类别变化时清空事件类型
 */
function handleCategoryChange() {
  queryParams.eventType = undefined
}

/**
 * 获取事件类别标签类型
 */
function getEventCategoryTagType(eventType: number): string {
  const category = getEventCategory(eventType)
  const style = getCategoryStyle(category)
  return style.color
}

/**
 * 获取事件类别标签文本
 */
function getEventCategoryLabel(eventType: number): string {
  const category = getEventCategory(eventType)
  const style = getCategoryStyle(category)
  return style.label
}

/**
 * 获取事件类型名称显示
 */
function getEventTypeNameDisplay(eventType: string | number): string {
  if (typeof eventType === 'number') {
    return getEventTypeName(eventType)
  }
  // 兼容字符串类型
  const typeMap: Record<string, string> = {
    'CARD_SWIPE': '刷卡开门',
    'FACE_RECOGNIZE': '人脸开门',
    'FINGERPRINT': '指纹开门',
    'PASSWORD': '密码开门',
    'REMOTE_OPEN': '远程开门',
    'DOOR_OPEN': '开门',
    'DOOR_CLOSE': '关门',
    'QR_CODE': '二维码开门'
  }
  return typeMap[eventType.toUpperCase()] || eventType
}

/**
 * 获取事件类型样式类
 */
function getEventTypeClass(eventType: number): string {
  const category = getEventCategory(eventType)
  switch (category) {
    case EventCategory.ALARM:
      return 'event-type-alarm'
    case EventCategory.ABNORMAL:
      return 'event-type-abnormal'
    default:
      return 'event-type-normal'
  }
}

watch(dateRange, (val) => {
  queryParams.startTime = val?.[0]
  queryParams.endTime = val?.[1]
})

/**
 * 格式化事件时间
 */
function formatEventTime(time: string | Date) {
  if (!time) return '-'
  return formatDateTime(time, 'yyyy-MM-dd HH:mm:ss')
}

/**
 * 规范化事件时间：兼容后端推送可能出现的未替换占位符（如 'YYYY-MM-DD'）
 * 优先使用 fallback（如 WebSocket 的 timestamp 或后端的 createTime）
 * @param time 原始事件时间（字符串或 Date）
 * @param fallback 备用时间（时间戳或 Date）
 * @returns 标准格式 'yyyy-MM-dd HH:mm:ss'
 */
function normalizeEventTime(time: string | Date | undefined, fallback?: number | Date) {
  if (!time) {
    return fallback ? formatDateTime(fallback, 'yyyy-MM-dd HH:mm:ss') : '-'
  }
  if (typeof time === 'string') {
    // 检测未替换的占位符（大写的 Y/D 等），或明显不可解析的格式
    const hasPlaceholders = /[Yy]{2,}|[D]{1,}/.test(time)
    const parsed = new Date(time)
    const invalid = Number.isNaN(parsed.getTime())
    if (hasPlaceholders || invalid) {
      return fallback ? formatDateTime(fallback, 'yyyy-MM-dd HH:mm:ss') : formatDateTime(new Date(), 'yyyy-MM-dd HH:mm:ss')
    }
  }
  return formatDateTime(time, 'yyyy-MM-dd HH:mm:ss')
}

/**
 * 获取验证结果类型
 */
function getVerifyResultType(row: AccessEventLogVO) {
  if (row.success === true || row.verifyResult === 1) {
    return 'success'
  }
  return 'danger'
}

/**
 * 获取验证结果标签
 */
function getVerifyResultLabel(row: AccessEventLogVO) {
  if (row.success === true || row.verifyResult === 1) {
    return '成功'
  }
  return '失败'
}

const getList = async () => {
  loading.value = true
  try {
    const res = await AccessEventLogApi.getEventPage(queryParams)
    list.value = res.list.map(item => ({
      ...item,
      isRealtime: false,
      eventTime: normalizeEventTime(item.eventTime, (item as any).timestamp || item.createTime)
    }))
    total.value = res.total
    
    // 不再从分页数据计算统计，统计数据由 loadTodayStatistics 加载
  } catch (error) {
    console.error('获取事件列表失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 加载今日统计数据 (Requirements: 7.1)
 */
const loadTodayStatistics = async () => {
  try {
    const stats = await AccessEventLogApi.getTodayStatistics()
    statistics.value = {
      total: stats.total,
      alarmCount: stats.alarmCount,
      abnormalCount: stats.abnormalCount,
      normalCount: stats.normalCount
    }
  } catch (error) {
    console.error('获取今日统计失败:', error)
    // 失败时使用空统计
    statistics.value = createEmptyStatistics()
  }
}

const loadDeviceList = async () => {
  try {
    deviceList.value = await AccessDeviceApi.getDeviceList()
  } catch (error) {
    console.error('加载设备列表失败:', error)
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  // 查询时清空实时事件
  realtimeEvents.value = []
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  dateRange.value = []
  queryParams.eventCategory = ''
  realtimeEvents.value = []
  handleQuery()
}

onMounted(() => {
  loadDeviceList()
  loadTodayStatistics() // 加载今日统计
  getList()
})
</script>


<style scoped lang="scss">
.access-event-container { 
  padding: 10px;
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

/* 统计概览栏 */
.statistics-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
  padding: 10px 0;

  .stat-card {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 15px;
    border-radius: 8px;
    background: var(--el-fill-color-light);
    cursor: pointer;
    transition: all 0.3s;
    position: relative;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    &.active {
      border: 2px solid var(--el-color-primary);
    }

    .stat-icon {
      position: absolute;
      top: 8px;
      right: 8px;
      font-size: 16px;
      opacity: 0.6;
    }

    .stat-value {
      font-size: 28px;
      font-weight: bold;
      color: var(--el-text-color-primary);
    }

    .stat-label {
      font-size: 13px;
      color: var(--el-text-color-secondary);
      margin-top: 4px;
    }

    &.alarm {
      background: rgba(245, 108, 108, 0.1);
      .stat-value { color: var(--el-color-danger); }
      .stat-icon { color: var(--el-color-danger); }
      &.active { border-color: var(--el-color-danger); }
    }

    &.abnormal {
      background: rgba(230, 162, 60, 0.1);
      .stat-value { color: var(--el-color-warning); }
      .stat-icon { color: var(--el-color-warning); }
      &.active { border-color: var(--el-color-warning); }
    }

    &.normal {
      background: rgba(103, 194, 58, 0.1);
      .stat-value { color: var(--el-color-success); }
      .stat-icon { color: var(--el-color-success); }
      &.active { border-color: var(--el-color-success); }
    }
  }
}

.realtime-status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  margin-bottom: 10px;
  border-bottom: 1px solid var(--el-border-color-lighter);

  .status-left {
    display: flex;
    align-items: center;
    gap: 10px;

    .status-icon {
      margin-right: 4px;
    }

    .reconnect-info {
      color: var(--el-color-warning);
      font-size: 12px;
    }
  }

  .status-right {
    display: flex;
    align-items: center;
    gap: 15px;

    .new-event-badge {
      :deep(.el-badge__content) {
        top: -5px;
        right: 5px;
      }
    }
  }
}

.search-form {
  margin-bottom: 10px;
}

/* 表格滚动容器：确保中部可滚动，底部分页可见 */
.table-scroll {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

/* 事件类型样式 */
.event-type-alarm {
  color: var(--el-color-danger);
  font-weight: 500;
}

.event-type-abnormal {
  color: var(--el-color-warning);
  font-weight: 500;
}

.event-type-normal {
  color: var(--el-text-color-primary);
}

/* 实时事件行样式 */
:deep(.realtime-row) {
  background-color: rgba(64, 158, 255, 0.05);
}

/* 高亮新事件行 */
:deep(.highlight-row) {
  background-color: rgba(103, 194, 58, 0.15) !important;
  animation: highlight-fade 3s ease-out;
}

@keyframes highlight-fade {
  0% {
    background-color: rgba(103, 194, 58, 0.3);
  }
  100% {
    background-color: rgba(103, 194, 58, 0.05);
  }
}
</style>
