<template>
  <div class="alarm-record-module">
    <!-- 高级统计仪表盘 -->
    <div class="stats-dashboard">
      <div class="stat-card stat-card--large">
        <div class="stat-card__header">
          <span class="stat-card__title">今日事件统计</span>
          <span class="stat-card__subtitle">实时更新</span>
        </div>
        <div class="stat-card__body">
          <div class="stat-main">
            <div class="stat-main__value">{{ stats.totalEvents }}</div>
            <div class="stat-main__label">事件总数</div>
          </div>
          <div class="stat-details">
            <div class="stat-detail">
              <div class="stat-detail__value stat-detail__value--danger">{{ stats.alarmCount }}</div>
              <div class="stat-detail__label">报警</div>
            </div>
            <div class="stat-detail">
              <div class="stat-detail__value stat-detail__value--success">{{ stats.recoveryCount }}</div>
              <div class="stat-detail__label">恢复</div>
            </div>
            <div class="stat-detail">
              <div class="stat-detail__value stat-detail__value--info">{{ stats.otherCount }}</div>
              <div class="stat-detail__label">其他</div>
            </div>
          </div>
        </div>
        <div class="stat-progress">
          <div 
            class="stat-progress__bar stat-progress__bar--danger" 
            :style="{ width: getPercentage(stats.alarmCount) + '%' }"
          ></div>
          <div 
            class="stat-progress__bar stat-progress__bar--success" 
            :style="{ width: getPercentage(stats.recoveryCount) + '%' }"
          ></div>
          <div 
            class="stat-progress__bar stat-progress__bar--info" 
            :style="{ width: getPercentage(stats.otherCount) + '%' }"
          ></div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-card__header">
          <span class="stat-card__title">今日告警</span>
          <Icon icon="ep:bell" class="stat-card__icon stat-card__icon--primary" />
        </div>
        <div class="stat-card__value">{{ stats.todayAlarms }}</div>
        <div class="stat-card__trend stat-card__trend--success">
          <Icon icon="ep:bottom" class="mr-4px" />
          较昨日持平
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-card__header">
          <span class="stat-card__title">活跃主机</span>
          <Icon icon="ep:monitor" class="stat-card__icon stat-card__icon--warning" />
        </div>
        <div class="stat-card__value">{{ stats.activeHosts }}</div>
        <div class="stat-card__desc">产生事件设备</div>
      </div>

      <div class="stat-card stat-card--large">
        <div class="stat-card__header">
          <span class="stat-card__title">处理率</span>
          <span class="stat-card__rate">{{ stats.processRate }}%</span>
        </div>
        <div class="stat-card__body stat-card__body--row">
          <div class="progress-ring">
            <svg viewBox="0 0 64 64">
              <circle cx="32" cy="32" r="28" stroke="#e5e7eb" stroke-width="6" fill="none" />
              <circle 
                cx="32" cy="32" r="28" 
                stroke="#67c23a" 
                stroke-width="6" 
                fill="none"
                :stroke-dasharray="175.9"
                :stroke-dashoffset="175.9 * (1 - stats.processRate / 100)"
                transform="rotate(-90 32 32)"
              />
            </svg>
            <span class="progress-ring__value">{{ stats.processRate }}%</span>
          </div>
          <div class="process-stats">
            <div class="process-stat">
              <span class="process-stat__label">已处理</span>
              <span class="process-stat__value">{{ stats.processedCount }}</span>
            </div>
            <div class="process-stat">
              <span class="process-stat__label">未处理</span>
              <span class="process-stat__value process-stat__value--danger">{{ stats.unprocessedCount }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-toolbar">
      <div class="filter-toolbar__left">
        <el-radio-group v-model="filterType" size="default">
          <el-radio-button label="">全部事件</el-radio-button>
          <el-radio-button label="alarm">
            <span class="type-dot type-dot--danger"></span>
            报警
          </el-radio-button>
          <el-radio-button label="fault">
            <span class="type-dot type-dot--warning"></span>
            故障
          </el-radio-button>
          <el-radio-button label="other">
            <span class="type-dot type-dot--info"></span>
            其他
          </el-radio-button>
        </el-radio-group>

        <el-divider direction="vertical" />

        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 260px"
        />

        <el-select v-model="filterHost" placeholder="所有主机" clearable style="width: 140px">
          <el-option 
            v-for="host in hostList" 
            :key="host.id" 
            :label="host.name" 
            :value="String(host.id)" 
          />
        </el-select>

        <el-select v-model="filterStatus" placeholder="所有状态" clearable style="width: 120px">
          <el-option label="未处理" value="pending" />
          <el-option label="已处理" value="processed" />
          <el-option label="已忽略" value="ignored" />
        </el-select>
      </div>

      <div class="filter-toolbar__right">
        <el-button type="primary" @click="handleSearch">
          <Icon icon="ep:search" class="mr-4px" />
          查询
        </el-button>
        <el-button @click="handleReset">
          <Icon icon="ep:refresh" class="mr-4px" />
          重置
        </el-button>
      </div>
    </div>

    <!-- 批量操作栏 -->
    <div class="batch-actions" v-if="selectedRows.length > 0">
      <div class="batch-actions__left">
        <el-checkbox v-model="selectAll" @change="handleSelectAll" />
        <span class="batch-info">
          已选择 <strong>{{ selectedRows.length }}</strong> 条记录
        </span>
      </div>
      <div class="batch-actions__right">
        <el-button type="primary" plain size="small" @click="handleBatchConfirm">
          <Icon icon="ep:check" class="mr-4px" />
          批量确认
        </el-button>
        <el-button size="small" @click="handleBatchExport">
          <Icon icon="ep:download" class="mr-4px" />
          导出选中
        </el-button>
        <el-button text size="small" @click="clearSelection">取消</el-button>
      </div>
    </div>

    <!-- 报警列表 -->
    <div class="alarm-list">
      <div 
        v-for="alarm in alarmList" 
        :key="alarm.id"
        class="alarm-card"
        :class="getAlarmCardClass(alarm)"
      >
        <div class="alarm-card__main">
          <el-checkbox 
            v-model="alarm.selected" 
            @change="handleSelectionChange"
            class="alarm-card__checkbox"
          />
          
          <div class="alarm-card__icon" :class="getAlarmIconClass(alarm)">
            <Icon :icon="getAlarmIcon(alarm)" />
          </div>
          
          <div class="alarm-card__content">
            <div class="alarm-card__header">
              <h3 class="alarm-card__title">
                事件码: {{ alarm.eventCode }} - {{ alarm.eventName }}
              </h3>
              <el-tag :type="getStatusTagType(alarm.status)" size="small" :effect="alarm.status === 'pending' ? 'dark' : 'light'">
                {{ getStatusText(alarm.status) }}
              </el-tag>
              <el-tag :type="getTypeTagType(alarm.type)" size="small">
                {{ getTypeText(alarm.type) }}
              </el-tag>
              <el-tag v-if="alarm.repeatCount > 1" type="info" size="small">
                <Icon icon="ep:refresh" class="mr-4px" />
                重复 {{ alarm.repeatCount }}次
              </el-tag>
            </div>
            
            <div class="alarm-card__meta">
              <span class="meta-item">
                <Icon icon="ep:monitor" class="mr-4px" />
                <strong>{{ alarm.hostName }}</strong>
              </span>
              <span class="meta-item">
                <Icon icon="ep:tickets" class="mr-4px" />
                事件码: {{ alarm.eventCode }}
              </span>
              <span class="meta-item">
                <Icon icon="ep:clock" class="mr-4px" />
                {{ alarm.eventTime }}
              </span>
              <span class="meta-item" :class="{ 'meta-item--pending': !alarm.processor }">
                <Icon icon="ep:user" class="mr-4px" />
                处理人: {{ alarm.processor || '待分配' }}
              </span>
            </div>

            <div class="alarm-card__extra" v-if="alarm.repeatCount > 1">
              <el-tag type="warning" size="small" effect="plain">
                首次: {{ alarm.firstTime }}
              </el-tag>
              <el-tag type="warning" size="small" effect="plain">
                最近: {{ alarm.lastTime }}
              </el-tag>
            </div>

            <div class="alarm-card__remark" v-if="alarm.remark">
              <Icon icon="ep:chat-dot-square" class="mr-4px" />
              处理备注: {{ alarm.remark }}
            </div>
          </div>
          
          <div class="alarm-card__actions">
            <el-button link type="primary" size="small" @click="handleViewDetail(alarm)">
              <Icon icon="ep:view" class="mr-4px" />
              详情
            </el-button>
            <el-button 
              v-if="alarm.status === 'pending'"
              type="success" 
              size="small" 
              @click="handleProcess(alarm)"
            >
              <Icon icon="ep:check" class="mr-4px" />
              处理
            </el-button>
            <el-button 
              v-if="alarm.status === 'pending'"
              size="small" 
              @click="handleIgnore(alarm)"
            >
              忽略
            </el-button>
            <el-button 
              v-if="alarm.status !== 'pending'"
              link 
              type="info" 
              size="small" 
              @click="handleViewDetail(alarm)"
            >
              <Icon icon="ep:view" class="mr-4px" />
              查看
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <div class="pagination-left">
        <span>共 <strong>{{ total }}</strong> 条记录</span>
        <el-select v-model="pageSize" style="width: 100px; margin-left: 12px">
          <el-option :value="10" label="10条/页" />
          <el-option :value="20" label="20条/页" />
          <el-option :value="50" label="50条/页" />
        </el-select>
      </div>
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="prev, pager, next, jumper"
        background
      />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="事件详情" width="600px">
      <div class="detail-dialog" v-if="currentAlarm">
        <div class="detail-header">
          <div class="detail-icon" :class="getAlarmIconClass(currentAlarm)">
            <Icon :icon="getAlarmIcon(currentAlarm)" size="32" />
          </div>
          <div class="detail-title">
            <h2>事件码: {{ currentAlarm.eventCode }}</h2>
            <p>{{ currentAlarm.eventName }}</p>
          </div>
          <el-tag :type="getStatusTagType(currentAlarm.status)" size="large">
            {{ getStatusText(currentAlarm.status) }}
          </el-tag>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="报警主机">{{ currentAlarm.hostName }}</el-descriptions-item>
          <el-descriptions-item label="事件类型">{{ getTypeText(currentAlarm.type) }}</el-descriptions-item>
          <el-descriptions-item label="发生时间">{{ currentAlarm.eventTime }}</el-descriptions-item>
          <el-descriptions-item label="防区位置">{{ currentAlarm.location || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-timeline">
          <h4>处理记录</h4>
          <div class="timeline-list">
            <div class="timeline-item">
              <div class="timeline-dot"></div>
              <div class="timeline-content">
                <div class="timeline-text">系统自动产生报警</div>
                <div class="timeline-time">{{ currentAlarm.eventTime }}</div>
              </div>
            </div>
            <div class="timeline-item" v-if="currentAlarm.processTime">
              <div class="timeline-dot timeline-dot--success"></div>
              <div class="timeline-content">
                <div class="timeline-text">{{ currentAlarm.processor }} 处理完成</div>
                <div class="timeline-time">{{ currentAlarm.processTime }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="detail-action" v-if="currentAlarm.status === 'pending'">
          <h4>处理操作</h4>
          <el-input
            v-model="processRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入处理备注..."
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button 
          v-if="currentAlarm?.status === 'pending'"
          type="primary" 
          @click="handleConfirmProcess"
        >
          确认处理
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as AlarmEventApi from '@/api/iot/alarm/event'
import * as AlarmHostApi from '@/api/iot/alarm/host'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'AlarmRecordModule' })

// 加载状态
const loading = ref(false)

// 主机列表
const hostList = ref<any[]>([])

// 统计数据
const stats = reactive({
  totalEvents: 0,
  alarmCount: 0,
  recoveryCount: 0,
  otherCount: 0,
  todayAlarms: 0,
  activeHosts: 0,
  processRate: 0,
  processedCount: 0,
  unprocessedCount: 0
})

// 筛选条件
const filterType = ref('')
const dateRange = ref<[Date, Date] | null>(null)
const filterHost = ref('')
const filterStatus = ref('')

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 选择
const selectedRows = ref<number[]>([])
const selectAll = ref(false)

// 报警列表
const alarmList = ref<any[]>([])

// 详情弹窗
const detailVisible = ref(false)
const currentAlarm = ref<any>(null)
const processRemark = ref('')

// 加载主机列表
const loadHostList = async () => {
  try {
    const hosts = await AlarmHostApi.getAllAlarmHosts()
    hostList.value = hosts.map(h => ({
      id: h.id,
      name: h.hostName
    }))
  } catch (error) {
    console.error('加载主机列表失败:', error)
  }
}

// 转换事件级别为类型
const convertEventLevel = (level?: string): string => {
  if (!level) return 'other'
  const levelMap: Record<string, string> = {
    'URGENT': 'alarm',
    'WARNING': 'fault',
    'INFO': 'other'
  }
  return levelMap[level] || 'other'
}

// 转换状态
const convertStatus = (status?: number): string => {
  const statusMap: Record<number, string> = {
    0: 'pending',
    1: 'processed',
    2: 'ignored'
  }
  return statusMap[status || 0] || 'pending'
}

// 构建查询参数
const buildQueryParams = () => {
  const params: AlarmEventApi.IotAlarmEventPageReqVO = {
    pageNo: currentPage.value,
    pageSize: pageSize.value
  }
  
  if (filterHost.value) {
    params.hostId = Number(filterHost.value)
  }
  
  if (filterStatus.value) {
    const statusMap: Record<string, number> = {
      pending: 0,
      processed: 1,
      ignored: 2
    }
    params.status = statusMap[filterStatus.value]
  }
  
  if (filterType.value) {
    const levelMap: Record<string, string> = {
      alarm: 'URGENT',
      fault: 'WARNING',
      other: 'INFO'
    }
    params.eventLevel = levelMap[filterType.value]
  }
  
  if (dateRange.value && dateRange.value[0] && dateRange.value[1]) {
    params.startTime = formatDate(dateRange.value[0], 'YYYY-MM-DD HH:mm:ss')
    params.endTime = formatDate(dateRange.value[1], 'YYYY-MM-DD HH:mm:ss')
  }
  
  return params
}

// 加载报警事件数据
const loadAlarmEvents = async () => {
  loading.value = true
  try {
    const params = buildQueryParams()
    const res = await AlarmEventApi.getAlarmEventPage(params)
    
    // 转换数据格式
    alarmList.value = (res.list || []).map((event: AlarmEventApi.IotAlarmEventVO) => ({
      id: event.id,
      eventCode: event.eventCode,
      eventName: event.eventName || `事件码: ${event.eventCode}`,
      hostName: event.hostName || '-',
      type: convertEventLevel(event.eventLevel),
      status: convertStatus(event.status),
      eventTime: event.eventTime || formatDate(event.createTime, 'YYYY-MM-DD HH:mm:ss'),
      processor: event.processor || null,
      processTime: event.processTime || null,
      remark: event.processRemark || null,
      location: event.paramDesc || '-',
      selected: false,
      // 原始数据
      _raw: event
    }))
    
    total.value = res.total || 0
    
    // 更新统计数据
    loadStats()
  } catch (error) {
    console.error('加载报警事件失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    const statsData = await AlarmEventApi.getAlarmEventStats()
    if (statsData) {
      stats.totalEvents = statsData.total || 0
      stats.alarmCount = statsData.alarm || 0
      stats.recoveryCount = statsData.restore || 0
      stats.otherCount = statsData.other || 0
      stats.todayAlarms = statsData.todayCount || 0
      stats.activeHosts = statsData.activeHosts || 0
      
      // 计算处理率
      const processed = stats.totalEvents - statsData.urgentCount
      stats.processedCount = processed
      stats.unprocessedCount = statsData.urgentCount || 0
      stats.processRate = stats.totalEvents > 0 
        ? Math.round((processed / stats.totalEvents) * 1000) / 10 
        : 100
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 监听分页变化
watch([currentPage, pageSize], () => {
  loadAlarmEvents()
})

// 初始化加载
onMounted(() => {
  loadHostList()
  loadAlarmEvents()
})

// 计算百分比
const getPercentage = (value: number) => {
  const total = stats.alarmCount + stats.recoveryCount + stats.otherCount
  if (total === 0) return 0
  return (value / total) * 100
}

// 获取报警卡片类
const getAlarmCardClass = (alarm: any) => {
  const classes: Record<string, string> = {
    pending: 'alarm-card--pending',
    processed: 'alarm-card--processed',
    ignored: 'alarm-card--ignored'
  }
  
  const typeClasses: Record<string, string> = {
    alarm: 'alarm-card--danger',
    fault: 'alarm-card--warning',
    other: 'alarm-card--info'
  }
  
  return [classes[alarm.status], typeClasses[alarm.type]]
}

// 获取报警图标类
const getAlarmIconClass = (alarm: any) => {
  const classes: Record<string, string> = {
    alarm: 'icon--danger',
    fault: 'icon--warning',
    other: 'icon--info'
  }
  return classes[alarm.type] || 'icon--info'
}

// 获取报警图标
const getAlarmIcon = (alarm: any) => {
  const icons: Record<string, string> = {
    alarm: 'ep:warning',
    fault: 'ep:bell',
    other: 'ep:circle-check'
  }
  return alarm.status === 'processed' ? 'ep:circle-check' : (icons[alarm.type] || 'ep:bell')
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  const types: Record<string, string> = {
    pending: 'danger',
    processed: 'success',
    ignored: 'info'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    pending: '未处理',
    processed: '已处理',
    ignored: '已忽略'
  }
  return texts[status] || '未知'
}

// 获取类型标签类型
const getTypeTagType = (type: string) => {
  const types: Record<string, string> = {
    alarm: 'danger',
    fault: 'warning',
    other: 'primary'
  }
  return types[type] || 'info'
}

// 获取类型文本
const getTypeText = (type: string) => {
  const texts: Record<string, string> = {
    alarm: '报警',
    fault: '故障',
    other: '其他'
  }
  return texts[type] || '其他'
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadAlarmEvents()
}

// 重置
const handleReset = () => {
  filterType.value = ''
  dateRange.value = null
  filterHost.value = ''
  filterStatus.value = ''
  currentPage.value = 1
  loadAlarmEvents()
  ElMessage.success('已重置筛选条件')
}

// 选择变更
const handleSelectionChange = () => {
  selectedRows.value = alarmList.value.filter(a => a.selected).map(a => a.id)
}

// 全选
const handleSelectAll = (val: boolean) => {
  alarmList.value.forEach(a => a.selected = val)
  handleSelectionChange()
}

// 清除选择
const clearSelection = () => {
  selectAll.value = false
  handleSelectAll(false)
}

// 批量确认
const handleBatchConfirm = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要处理的记录')
    return
  }
  await ElMessageBox.confirm(`确认处理选中的 ${selectedRows.value.length} 条记录?`, '批量确认')
  try {
    const promises = selectedRows.value.map(id => 
      AlarmEventApi.processAlarmEvent({
        id,
        result: 'SUCCESS',
        actions: ['CONFIRM'],
        remark: '批量处理'
      })
    )
    await Promise.all(promises)
    ElMessage.success('批量处理成功')
    clearSelection()
    loadAlarmEvents()
  } catch (error) {
    ElMessage.error('部分记录处理失败')
    loadAlarmEvents()
  }
}

// 批量导出
const handleBatchExport = async () => {
  try {
    const params = buildQueryParams()
    await AlarmEventApi.exportAlarmEvent(params)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// 查看详情
const handleViewDetail = async (alarm: any) => {
  try {
    // 获取完整详情
    const detail = await AlarmEventApi.getAlarmEvent(alarm.id)
    currentAlarm.value = {
      ...alarm,
      ...detail
    }
    processRemark.value = ''
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

// 处理
const handleProcess = async (alarm: any) => {
  currentAlarm.value = alarm
  processRemark.value = ''
  detailVisible.value = true
}

// 确认处理
const handleConfirmProcess = async () => {
  if (!currentAlarm.value) return
  
  try {
    await AlarmEventApi.processAlarmEvent({
      id: currentAlarm.value.id,
      result: 'SUCCESS',
      actions: ['CONFIRM'],
      remark: processRemark.value || '已处理'
    })
    
    // 更新UI
    currentAlarm.value.status = 'processed'
    currentAlarm.value.processor = '当前用户'
    currentAlarm.value.processTime = formatDate(new Date(), 'YYYY-MM-DD HH:mm:ss')
    currentAlarm.value.remark = processRemark.value
    
    detailVisible.value = false
    ElMessage.success('处理成功')
    
    // 刷新列表
    loadAlarmEvents()
  } catch (error) {
    ElMessage.error('处理失败')
  }
}

// 忽略
const handleIgnore = async (alarm: any) => {
  await ElMessageBox.confirm('确认忽略该报警?', '确认')
  try {
    await AlarmEventApi.ignoreAlarmEvent(alarm.id)
    alarm.status = 'ignored'
    ElMessage.success('已忽略')
    loadAlarmEvents()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}
</script>

<style lang="scss" scoped>
.alarm-record-module {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  gap: 16px;
  --nia-surface-color: var(--el-bg-color);
  --nia-surface-muted-color: var(--el-fill-color-light);
  --nia-border-color: var(--el-border-color-light);
  --nia-text-primary: var(--el-text-color-primary);
  --nia-text-regular: var(--el-text-color-regular);
  --nia-text-secondary: var(--el-text-color-secondary);
}

// 统计仪表盘
.stats-dashboard {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 2fr;
  gap: 16px;
  flex-shrink: 0;
}

.stat-card {
  background: var(--nia-surface-color);
  border-radius: 12px;
  border: 1px solid var(--nia-border-color);
  padding: 16px;

  &--large {
    // 大卡片样式
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
  }

  &__title {
    font-size: 14px;
    color: var(--nia-text-regular);
  }

  &__subtitle {
    font-size: 12px;
    color: var(--nia-text-secondary);
  }

  &__icon {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;

    &--primary { background: var(--el-color-primary-light-9); color: var(--el-color-primary); }
    &--warning { background: var(--el-color-warning-light-9); color: var(--el-color-warning); }
  }

  &__value {
    font-size: 28px;
    font-weight: 700;
    color: var(--nia-text-primary);
  }

  &__rate {
    font-size: 20px;
    font-weight: 700;
    color: #67c23a;
  }

  &__trend {
    font-size: 12px;
    display: flex;
    align-items: center;

    &--success { color: #67c23a; }
    &--danger { color: #f56c6c; }
  }

  &__desc {
    font-size: 12px;
    color: var(--nia-text-secondary);
  }

  &__body {
    &--row {
      display: flex;
      align-items: center;
      gap: 16px;
    }
  }
}

.stat-main {
  text-align: center;

  &__value {
    font-size: 32px;
    font-weight: 700;
    color: var(--nia-text-primary);
  }

  &__label {
    font-size: 12px;
    color: var(--nia-text-secondary);
  }
}

.stat-details {
  display: flex;
  gap: 24px;
  margin-top: 12px;
}

.stat-detail {
  text-align: center;

  &__value {
    font-size: 20px;
    font-weight: 700;

    &--danger { color: #f56c6c; }
    &--success { color: #67c23a; }
    &--info { color: #409eff; }
  }

  &__label {
    font-size: 12px;
    color: var(--nia-text-secondary);
  }
}

.stat-progress {
  height: 4px;
  background: var(--nia-border-color);
  border-radius: 2px;
  margin-top: 12px;
  display: flex;
  overflow: hidden;

  &__bar {
    height: 100%;
    transition: width 0.3s;

    &--danger { background: var(--el-color-danger); }
    &--success { background: var(--el-color-success); }
    &--info { background: var(--el-color-primary); }
  }
}

.progress-ring {
  width: 64px;
  height: 64px;
  position: relative;

  svg {
    width: 100%;
    height: 100%;
  }

  &__value {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 700;
    color: #67c23a;
  }
}

.process-stats {
  flex: 1;
}

.process-stat {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 12px;

  &__label {
    color: var(--nia-text-regular);
  }

  &__value {
    font-weight: 500;
    color: var(--nia-text-primary);

    &--danger {
      color: #f56c6c;
    }
  }
}

// 筛选工具栏
.filter-toolbar {
  background: var(--nia-surface-color);
  border-radius: 12px;
  border: 1px solid var(--nia-border-color);
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  flex-shrink: 0;

  &__left {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
  }

  &__right {
    display: flex;
    gap: 8px;
  }
}

.type-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 4px;

  &--danger { background: #f56c6c; }
  &--warning { background: #e6a23c; }
  &--info { background: #409eff; }
}

// 批量操作栏
.batch-actions {
  background: #ecf5ff;
  border: 1px solid #b3d8ff;
  border-radius: 12px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;

  &__left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__right {
    display: flex;
    gap: 8px;
  }
}

.batch-info {
  font-size: 14px;
  color: #409eff;

  strong {
    font-weight: 700;
  }
}

// 报警列表
.alarm-list {
  flex: 1;
  overflow: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alarm-card {
  background: var(--nia-surface-color);
  border-radius: 12px;
  border: 1px solid var(--nia-border-color);
  border-left: 4px solid;
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  &--danger { border-left-color: var(--el-color-danger); }
  &--warning { border-left-color: var(--el-color-warning); }
  &--info { border-left-color: var(--el-color-primary); }

  &--processed {
    opacity: 0.75;
    border-left-color: var(--el-color-success);

    &:hover {
      opacity: 1;
    }
  }

  &__main {
    display: flex;
    align-items: flex-start;
    gap: 16px;
    padding: 16px;
  }

  &__checkbox {
    flex-shrink: 0;
    margin-top: 4px;
  }

  &__icon {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    flex-shrink: 0;

    &.icon--danger { background: var(--el-color-danger-light-9); color: var(--el-color-danger); }
    &.icon--warning { background: var(--el-color-warning-light-9); color: var(--el-color-warning); }
    &.icon--info { background: var(--el-color-primary-light-9); color: var(--el-color-primary); }
  }

  &__content {
    flex: 1;
    min-width: 0;
  }

  &__header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
    flex-wrap: wrap;
  }

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: var(--nia-text-primary);
    margin: 0;
  }

  &__meta {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 8px 16px;
    font-size: 13px;
    color: var(--nia-text-regular);
  }

  &__extra {
    display: flex;
    gap: 8px;
    margin-top: 8px;
  }

  &__remark {
    margin-top: 8px;
    padding: 8px 12px;
    background: var(--nia-surface-muted-color);
    border-radius: 6px;
    font-size: 12px;
    color: var(--nia-text-secondary);
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
  }
}

.meta-item {
  display: flex;
  align-items: center;

  strong {
    color: var(--nia-text-primary);
  }

  &--pending {
    color: var(--nia-text-secondary);
  }
}

// 分页
.pagination-wrapper {
  background: var(--nia-surface-color);
  border-radius: 12px;
  border: 1px solid var(--nia-border-color);
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.pagination-left {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: var(--nia-text-regular);

  strong {
    color: var(--nia-text-primary);
  }
}

// 详情弹窗
.detail-dialog {
  .detail-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 20px;
  }

  .detail-icon {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;

    &.icon--danger { background: var(--el-color-danger-light-9); color: var(--el-color-danger); }
    &.icon--warning { background: var(--el-color-warning-light-9); color: var(--el-color-warning); }
    &.icon--info { background: var(--el-color-primary-light-9); color: var(--el-color-primary); }
  }

  .detail-title {
    flex: 1;

    h2 {
      font-size: 20px;
      font-weight: 700;
      color: var(--nia-text-primary);
      margin: 0 0 4px;
    }

    p {
      font-size: 14px;
      color: var(--nia-text-secondary);
      margin: 0;
    }
  }

  .detail-timeline {
    margin-top: 20px;

    h4 {
      font-size: 14px;
      font-weight: 600;
      color: var(--nia-text-regular);
      margin: 0 0 12px;
    }
  }

  .timeline-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .timeline-item {
    display: flex;
    gap: 12px;
  }

  .timeline-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: var(--nia-border-color);
    margin-top: 6px;
    flex-shrink: 0;

    &--success {
      background: var(--el-color-success);
    }
  }

  .timeline-content {
    flex: 1;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--nia-border-color);
  }

  .timeline-text {
    font-size: 14px;
    color: var(--nia-text-primary);
  }

  .timeline-time {
    font-size: 12px;
    color: var(--nia-text-secondary);
    margin-top: 4px;
  }

  .detail-action {
    margin-top: 20px;

    h4 {
      font-size: 14px;
      font-weight: 600;
      color: var(--nia-text-regular);
      margin: 0 0 12px;
    }
  }
}

// 响应式
@media (max-width: 1200px) {
  .stats-dashboard {
    grid-template-columns: repeat(2, 1fr);
  }

  .alarm-card__meta {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-dashboard {
    grid-template-columns: 1fr;
  }

  .filter-toolbar {
    flex-direction: column;
    align-items: stretch;

    &__left,
    &__right {
      flex-direction: column;
    }
  }
}
</style>
