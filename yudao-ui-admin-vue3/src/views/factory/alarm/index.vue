<script setup lang="ts">
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import {
  getAlarmEvent,
  getAlarmEventPage,
  getAlarmEventStats,
  type IotAlarmEventStatsVO,
  type IotAlarmEventVO
} from '@/api/iot/alarm/event'
import { Icon } from '@/components/Icon'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

defineOptions({ name: 'FactoryAlarm' })

type AlarmFilterKey = 'all' | 'pending' | 'processed' | 'ignored'

interface AlarmSummaryCard {
  key: string
  label: string
  value: number
  icon: string
  accent: 'danger' | 'warning' | 'primary' | 'muted'
}

const loading = ref(false)
const detailLoading = ref(false)
const loadError = ref('')
const keyword = ref('')
const activeFilter = ref<AlarmFilterKey>('all')
const detailVisible = ref(false)
const alarmList = ref<IotAlarmEventVO[]>([])
const currentAlarm = ref<IotAlarmEventVO | null>(null)
const stats = ref<IotAlarmEventStatsVO | null>(null)

const FILTER_STATUS_MAP: Record<Exclude<AlarmFilterKey, 'all'>, number> = {
  pending: 0,
  processed: 1,
  ignored: 2
}

const filterOptions: Array<{ key: AlarmFilterKey; label: string }> = [
  { key: 'all', label: '全部' },
  { key: 'pending', label: '未处理' },
  { key: 'processed', label: '已处理' },
  { key: 'ignored', label: '已忽略' }
]

const normalizeLevel = (eventLevel?: string) => {
  return String(eventLevel || '')
    .trim()
    .toUpperCase()
}

const getLevelAccent = (eventLevel?: string) => {
  const level = normalizeLevel(eventLevel)
  if (['CRITICAL', 'EMERGENCY', 'ERROR'].includes(level)) {
    return 'danger'
  }
  if (['WARNING', 'WARN', 'MAJOR'].includes(level)) {
    return 'warning'
  }
  if (['NOTICE', 'INFO'].includes(level)) {
    return 'primary'
  }
  return 'muted'
}

const getLevelText = (eventLevel?: string) => {
  const level = normalizeLevel(eventLevel)
  if (['CRITICAL', 'EMERGENCY'].includes(level)) return '紧急'
  if (['ERROR', 'WARNING', 'WARN', 'MAJOR'].includes(level)) return '重要'
  if (['NOTICE', 'INFO'].includes(level)) return '一般'
  return '提示'
}

const getStatusText = (status?: number) => {
  if (status === 1) return '已处理'
  if (status === 2) return '已忽略'
  return '未处理'
}

const getStatusClass = (status?: number) => {
  if (status === 1) return 'is-success'
  if (status === 2) return 'is-muted'
  return 'is-danger'
}

const getAlarmMeta = (row: IotAlarmEventVO) => {
  const location = [row.area, row.point].filter(Boolean).join(' · ') || '未配置位置'
  const timeText = row.eventTime ? dayjs(row.eventTime).fromNow() : '刚刚'
  return `${location} · ${timeText}`
}

const matchesKeyword = (row: IotAlarmEventVO) => {
  const query = keyword.value.trim().toLowerCase()
  if (!query) return true
  return [row.eventName, row.area, row.point, row.eventCode, row.hostName, row.paramDesc]
    .filter(Boolean)
    .some((item) => String(item).toLowerCase().includes(query))
}

const summaryCards = computed<AlarmSummaryCard[]>(() => {
  const source = alarmList.value
  const levelCounts = source.reduce(
    (accumulator, item) => {
      const accent = getLevelAccent(item.eventLevel)
      if (accent === 'danger') accumulator.danger += 1
      else if (accent === 'warning') accumulator.warning += 1
      else if (accent === 'primary') accumulator.primary += 1
      else accumulator.muted += 1
      return accumulator
    },
    { danger: 0, warning: 0, primary: 0, muted: 0 }
  )

  return [
    { key: 'danger', label: '紧急告警', value: stats.value?.urgentCount ?? levelCounts.danger, icon: 'ep:warning-filled', accent: 'danger' },
    { key: 'warning', label: '重要告警', value: levelCounts.warning, icon: 'ep:bell-filled', accent: 'warning' },
    { key: 'primary', label: '一般告警', value: levelCounts.primary, icon: 'ep:notification', accent: 'primary' },
    { key: 'muted', label: '提示信息', value: stats.value?.other ?? levelCounts.muted, icon: 'ep:message-box', accent: 'muted' }
  ]
})

const filteredAlarmList = computed(() => {
  const targetStatus = activeFilter.value === 'all' ? undefined : FILTER_STATUS_MAP[activeFilter.value]
  return alarmList.value.filter((item) => {
    const matchStatus = targetStatus === undefined ? true : (item.status ?? 0) === targetStatus
    return matchStatus && matchesKeyword(item)
  })
})

const statusSummaryText = computed(() => {
  if (!stats.value) {
    return `当前已接入 ${alarmList.value.length} 条真实告警记录`
  }
  return `总事件 ${stats.value.total} · 报警 ${stats.value.alarm} · 恢复 ${stats.value.restore} · 今日 ${stats.value.todayCount}`
})

const loadAlarmData = async () => {
  loading.value = true
  try {
    const [pageResult, statsResult] = await Promise.all([
      getAlarmEventPage({
        pageNo: 1,
        pageSize: 50
      }),
      getAlarmEventStats().catch(() => null)
    ])
    alarmList.value = pageResult?.list || []
    stats.value = statsResult
    loadError.value = ''
  } catch (error) {
    console.error('加载告警管理页失败:', error)
    loadError.value = '真实告警接口暂不可用，请稍后重试。'
  } finally {
    loading.value = false
  }
}

const openAlarmDetail = async (row: IotAlarmEventVO) => {
  detailVisible.value = true
  currentAlarm.value = row
  if (!row.id) {
    return
  }
  detailLoading.value = true
  try {
    currentAlarm.value = await getAlarmEvent(row.id)
  } catch (error) {
    console.error('加载告警详情失败:', error)
  } finally {
    detailLoading.value = false
  }
}

const resetSearch = () => {
  keyword.value = ''
  activeFilter.value = 'all'
}

onMounted(() => {
  loadAlarmData()
})
</script>

<template>
  <ContentWrap
    :body-style="{
      padding: '0',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      background: 'transparent'
    }"
    style="height: calc(100vh - var(--page-top-gap, 70px)); padding-top: var(--page-top-gap, 70px); margin-bottom: 0"
  >
    <div class="factory-alarm-page">
      <ElAlert
        v-if="loadError"
        :title="loadError"
        type="warning"
        :closable="false"
        show-icon
        class="factory-alarm__banner"
      />

      <div v-loading="loading" class="factory-alarm">
        <div class="factory-alarm__metrics">
          <div
            v-for="card in summaryCards"
            :key="card.key"
            class="factory-alarm-metric"
            :class="`is-${card.accent}`"
          >
            <div class="factory-alarm-metric__icon">
              <Icon :icon="card.icon" />
            </div>
            <div class="factory-alarm-metric__content">
              <div class="factory-alarm-metric__label">{{ card.label }}</div>
              <div class="factory-alarm-metric__value">{{ card.value }}</div>
            </div>
          </div>
        </div>

        <div class="factory-alarm__toolbar">
          <div class="factory-alarm__filters">
            <button
              v-for="item in filterOptions"
              :key="item.key"
              class="factory-alarm-filter"
              :class="{ 'is-active': activeFilter === item.key }"
              type="button"
              @click="activeFilter = item.key"
            >
              {{ item.label }}
            </button>
          </div>
          <div class="factory-alarm__actions">
            <label class="factory-alarm-search">
              <Icon icon="ep:search" />
              <input v-model="keyword" type="text" placeholder="搜索告警..." />
            </label>
            <button class="factory-alarm-action" type="button" @click="resetSearch">
              <Icon icon="ep:filter" />
            </button>
          </div>
        </div>

        <div class="factory-alarm__list-wrap">
          <div v-if="filteredAlarmList.length" class="factory-alarm__list">
            <button
              v-for="item in filteredAlarmList"
              :key="item.id || `${item.eventCode}-${item.timestamp}`"
              class="factory-alarm-item"
              :class="`is-${getLevelAccent(item.eventLevel)}`"
              type="button"
              @click="openAlarmDetail(item)"
            >
              <div class="factory-alarm-item__main">
                <div class="factory-alarm-item__icon" :class="`is-${getLevelAccent(item.eventLevel)}`">
                  <Icon icon="ep:warning-filled" />
                </div>
                <div class="factory-alarm-item__content">
                  <div class="factory-alarm-item__title">{{ item.eventName || '未命名告警' }}</div>
                  <div class="factory-alarm-item__meta">{{ getAlarmMeta(item) }}</div>
                </div>
              </div>
              <div class="factory-alarm-item__aside">
                <span class="factory-alarm-level" :class="`is-${getLevelAccent(item.eventLevel)}`">
                  {{ getLevelText(item.eventLevel) }}
                </span>
                <div class="factory-alarm-item__action-row">
                  <span class="factory-alarm-status" :class="getStatusClass(item.status)">
                    {{ getStatusText(item.status) }}
                  </span>
                  <span class="factory-alarm-item__action">
                    {{ item.status === 0 ? '处理' : '查看' }}
                  </span>
                </div>
              </div>
            </button>
          </div>
          <ElEmpty v-else description="当前筛选条件下暂无告警数据" />
        </div>
      </div>

      <ElDrawer v-model="detailVisible" title="告警详情" size="420px">
        <div v-loading="detailLoading" class="factory-alarm-detail">
          <div class="factory-alarm-detail__title">
            {{ currentAlarm?.eventName || '未命名告警' }}
          </div>
          <div class="factory-alarm-detail__section">
            <div class="factory-alarm-detail__label">事件级别</div>
            <div class="factory-alarm-detail__value">{{ getLevelText(currentAlarm?.eventLevel) }}</div>
          </div>
          <div class="factory-alarm-detail__section">
            <div class="factory-alarm-detail__label">处理状态</div>
            <div class="factory-alarm-detail__value">{{ getStatusText(currentAlarm?.status) }}</div>
          </div>
          <div class="factory-alarm-detail__section">
            <div class="factory-alarm-detail__label">报警主机</div>
            <div class="factory-alarm-detail__value">{{ currentAlarm?.hostName || '--' }}</div>
          </div>
          <div class="factory-alarm-detail__section">
            <div class="factory-alarm-detail__label">位置</div>
            <div class="factory-alarm-detail__value">{{ [currentAlarm?.area, currentAlarm?.point].filter(Boolean).join(' · ') || '--' }}</div>
          </div>
          <div class="factory-alarm-detail__section">
            <div class="factory-alarm-detail__label">事件编码</div>
            <div class="factory-alarm-detail__value">{{ currentAlarm?.eventCode || '--' }}</div>
          </div>
          <div class="factory-alarm-detail__section">
            <div class="factory-alarm-detail__label">发生时间</div>
            <div class="factory-alarm-detail__value">{{ currentAlarm?.eventTime || '--' }}</div>
          </div>
          <div class="factory-alarm-detail__section">
            <div class="factory-alarm-detail__label">参数描述</div>
            <div class="factory-alarm-detail__value is-block">{{ currentAlarm?.paramDesc || '--' }}</div>
          </div>
          <div class="factory-alarm-detail__section">
            <div class="factory-alarm-detail__label">处理备注</div>
            <div class="factory-alarm-detail__value is-block">{{ currentAlarm?.processRemark || '--' }}</div>
          </div>
        </div>
      </ElDrawer>
    </div>
  </ContentWrap>
</template>

<style scoped lang="scss">
.factory-alarm-page {
  display: flex;
  flex-direction: column;
  min-height: 100%;
  padding: 0 12px 12px;
  background:
    radial-gradient(circle at top left, rgba(0, 222, 255, 0.16), transparent 34%),
    radial-gradient(circle at top right, rgba(0, 120, 255, 0.14), transparent 28%),
    linear-gradient(180deg, #07111f 0%, #081725 34%, #060d18 100%);
  border-radius: 24px;
  box-sizing: border-box;
}

.factory-alarm__banner {
  margin-bottom: 16px;

  :deep(.el-alert) {
    border: 1px solid rgba(255, 193, 7, 0.18);
    border-radius: 18px;
    background: rgba(58, 38, 7, 0.4);
  }
}

.factory-alarm {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
  gap: 16px;
  padding-top: 8px;
  box-sizing: border-box;
}

.factory-alarm__metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  flex-shrink: 0;
  gap: 16px;
  margin-top: 0;
}

.factory-alarm-metric {
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 88px;
  padding: 18px;
  border: 1px solid rgba(87, 124, 162, 0.22);
  border-radius: 22px;
  background: linear-gradient(180deg, rgba(10, 22, 38, 0.96), rgba(7, 16, 29, 0.98));
  box-shadow:
    inset 0 1px 0 rgba(177, 222, 255, 0.06),
    0 14px 36px rgba(0, 0, 0, 0.22);
}

.factory-alarm-metric__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  font-size: 20px;
  border-radius: 14px;
}

.factory-alarm-metric__label {
  font-size: 12px;
  color: rgba(201, 222, 240, 0.62);
}

.factory-alarm-metric__value {
  margin-top: 6px;
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.factory-alarm-metric.is-danger .factory-alarm-metric__icon {
  color: #f87171;
  background: rgba(239, 68, 68, 0.18);
}

.factory-alarm-metric.is-danger .factory-alarm-metric__value {
  color: #f87171;
}

.factory-alarm-metric.is-warning .factory-alarm-metric__icon {
  color: #fbbf24;
  background: rgba(245, 158, 11, 0.18);
}

.factory-alarm-metric.is-warning .factory-alarm-metric__value {
  color: #fbbf24;
}

.factory-alarm-metric.is-primary .factory-alarm-metric__icon {
  color: #60a5fa;
  background: rgba(59, 130, 246, 0.18);
}

.factory-alarm-metric.is-primary .factory-alarm-metric__value {
  color: #60a5fa;
}

.factory-alarm-metric.is-muted .factory-alarm-metric__icon {
  color: #94a3b8;
  background: rgba(148, 163, 184, 0.18);
}

.factory-alarm-metric.is-muted .factory-alarm-metric__value {
  color: #cbd5e1;
}

.factory-alarm__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  gap: 16px;
}

.factory-alarm__filters,
.factory-alarm__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.factory-alarm-filter {
  min-height: 40px;
  padding: 0 18px;
  color: rgba(188, 207, 222, 0.72);
  border: none;
  border-radius: 14px;
  background: rgba(31, 41, 55, 0.88);
  cursor: pointer;
  transition: all 0.2s ease;
}

.factory-alarm-filter.is-active {
  color: #fff;
  background: #2563eb;
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.28);
}

.factory-alarm-search {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  width: 320px;
  min-height: 42px;
  padding: 0 14px;
  color: rgba(148, 163, 184, 0.82);
  border: 1px solid rgba(55, 65, 81, 0.9);
  border-radius: 14px;
  background: rgba(31, 41, 55, 0.9);
}

.factory-alarm-search input {
  width: 100%;
  color: #fff;
  border: none;
  background: transparent;
  outline: none;
}

.factory-alarm-search input::placeholder {
  color: rgba(148, 163, 184, 0.58);
}

.factory-alarm-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  color: rgba(188, 207, 222, 0.72);
  border: none;
  border-radius: 14px;
  background: rgba(31, 41, 55, 0.9);
  cursor: pointer;
}

.factory-alarm__list-wrap {
  flex: 1;
  min-height: 0;
  padding: 18px;
  overflow: hidden;
  border: 1px solid rgba(87, 124, 162, 0.22);
  border-radius: 24px;
  background: linear-gradient(180deg, rgba(10, 22, 38, 0.96), rgba(7, 16, 29, 0.98));
}

.factory-alarm__list {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 12px;
  overflow-y: auto;
}

.factory-alarm-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  width: 100%;
  padding: 18px;
  color: #fff;
  border: 1px solid transparent;
  border-radius: 18px;
  background: rgba(10, 18, 32, 0.88);
  cursor: pointer;
  transition: all 0.2s ease;
}

.factory-alarm-item:hover {
  transform: translateY(-1px);
}

.factory-alarm-item.is-danger {
  border-color: rgba(239, 68, 68, 0.28);
  background: rgba(127, 29, 29, 0.18);
}

.factory-alarm-item.is-warning {
  border-color: rgba(245, 158, 11, 0.28);
  background: rgba(120, 53, 15, 0.16);
}

.factory-alarm-item.is-primary {
  border-color: rgba(59, 130, 246, 0.28);
  background: rgba(30, 64, 175, 0.14);
}

.factory-alarm-item.is-muted {
  border-color: rgba(100, 116, 139, 0.24);
  background: rgba(51, 65, 85, 0.16);
}

.factory-alarm-item__main {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.factory-alarm-item__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  flex-shrink: 0;
  font-size: 20px;
  border-radius: 14px;
}

.factory-alarm-item__icon.is-danger {
  color: #f87171;
  background: rgba(239, 68, 68, 0.18);
}

.factory-alarm-item__icon.is-warning {
  color: #fbbf24;
  background: rgba(245, 158, 11, 0.18);
}

.factory-alarm-item__icon.is-primary {
  color: #60a5fa;
  background: rgba(59, 130, 246, 0.18);
}

.factory-alarm-item__icon.is-muted {
  color: #cbd5e1;
  background: rgba(148, 163, 184, 0.18);
}

.factory-alarm-item__content {
  min-width: 0;
  text-align: left;
}

.factory-alarm-item__title {
  font-size: 16px;
  font-weight: 600;
}

.factory-alarm-item__meta {
  margin-top: 6px;
  font-size: 13px;
  color: rgba(191, 204, 218, 0.7);
}

.factory-alarm-item__aside {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-shrink: 0;
}

.factory-alarm-level,
.factory-alarm-status,
.factory-alarm-item__action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 34px;
  padding: 0 14px;
  font-size: 13px;
  border-radius: 999px;
}

.factory-alarm-level.is-danger,
.factory-alarm-status.is-danger {
  color: #fca5a5;
  background: rgba(239, 68, 68, 0.18);
}

.factory-alarm-level.is-warning {
  color: #fcd34d;
  background: rgba(245, 158, 11, 0.18);
}

.factory-alarm-level.is-primary {
  color: #93c5fd;
  background: rgba(59, 130, 246, 0.18);
}

.factory-alarm-level.is-muted,
.factory-alarm-status.is-muted {
  color: #cbd5e1;
  background: rgba(100, 116, 139, 0.18);
}

.factory-alarm-status.is-success {
  color: #86efac;
  background: rgba(34, 197, 94, 0.18);
}

.factory-alarm-item__action-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.factory-alarm-item__action {
  color: #60a5fa;
  background: rgba(59, 130, 246, 0.18);
}

.factory-alarm-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.factory-alarm-detail__title {
  font-size: 18px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.factory-alarm-detail__section {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.factory-alarm-detail__label {
  flex-shrink: 0;
  color: var(--el-text-color-secondary);
}

.factory-alarm-detail__value {
  text-align: right;
  color: var(--el-text-color-primary);
}

.factory-alarm-detail__value.is-block {
  white-space: pre-wrap;
}

@media (max-width: 1200px) {
  .factory-alarm__metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .factory-alarm__toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .factory-alarm__filters,
  .factory-alarm__actions {
    flex-wrap: wrap;
  }

  .factory-alarm-search {
    width: 100%;
  }

  .factory-alarm-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .factory-alarm-item__aside {
    width: 100%;
    justify-content: space-between;
  }
}

@media (max-width: 640px) {
  .factory-alarm__metrics {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
