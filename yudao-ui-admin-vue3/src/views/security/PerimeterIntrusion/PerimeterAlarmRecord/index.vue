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
    <div class="alarm-record-page">
      <!-- 顶部统计条 -->
      <div class="top-stats-bar">
        <div class="top-stat-item">
          <span class="top-stat-value">{{ stats.total }}</span>
          <span class="top-stat-label">今日事件总数</span>
        </div>
        <div class="top-stat-item">
          <span class="top-stat-value alarm-color">{{ stats.alarm }}</span>
          <span class="top-stat-label">报警事件</span>
        </div>
        <div class="top-stat-item">
          <span class="top-stat-value restore-color">{{ stats.restore }}</span>
          <span class="top-stat-label">恢复事件</span>
        </div>
        <div class="top-stat-item">
          <span class="top-stat-value">{{ stats.other }}</span>
          <span class="top-stat-label">其他事件</span>
        </div>
      </div>

      <!-- 搜索区域 + 统计卡片 -->
      <div class="search-stats-container">
        <!-- 左侧：搜索表单 -->
        <div class="search-form">
          <div class="search-row">
            <el-form :model="searchForm" label-width="70px" :inline="true">
              <el-form-item label="告警时间">
                <el-date-picker
                  v-model="searchForm.alarmTime"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 320px"
                />
              </el-form-item>
            </el-form>
          </div>
          <div class="search-row">
            <el-form :model="searchForm" label-width="70px" :inline="true">
              <el-form-item label="报警主机">
                <el-select v-model="searchForm.hostId" placeholder="请选择主机" clearable style="width: 140px">
                  <el-option
                    v-for="host in hostOptions"
                    :key="host.value"
                    :label="host.label"
                    :value="host.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="处理状态">
                <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 100px">
                  <el-option label="待处理" value="pending" />
                  <el-option label="处理中" value="processing" />
                  <el-option label="已处理" value="completed" />
                  <el-option label="已忽略" value="ignored" />
                </el-select>
              </el-form-item>
              <el-form-item class="search-buttons">
                <el-button type="primary" @click="handleSearch">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
                <el-button @click="handleReset">
                  <el-icon><Refresh /></el-icon>
                  重置
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- 右侧：统计卡片 -->
        <div class="inline-stats">
          <el-card class="stat-card today">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.todayCount }}</div>
                <div class="stat-label">今日告警</div>
              </div>
            </div>
          </el-card>
          <el-card class="stat-card zones">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><MapLocation /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.activeHosts }}</div>
                <div class="stat-label">活跃主机</div>
              </div>
            </div>
          </el-card>
          <el-card class="stat-card processed">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.processedRate }}%</div>
                <div class="stat-label">处理率</div>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 告警记录表格 -->
      <div class="table-container">
        <el-table v-loading="loading" :data="alarmList" stripe border style="width: 100%">
          <el-table-column prop="eventTime" label="事件时间" width="160" show-overflow-tooltip />
          <!-- <el-table-column prop="eventLevel" label="事件级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelColor(row.eventLevel)" effect="dark">
              {{ getLevelText(row.eventLevel) }}
            </el-tag>
          </template>
        </el-table-column> -->
          <el-table-column prop="hostName" label="报警主机" width="140" show-overflow-tooltip />
          <el-table-column prop="eventName" label="事件名称" width="140" show-overflow-tooltip />
          <el-table-column prop="eventCode" label="事件码" width="100" />
          <el-table-column label="事件类型" width="100">
            <template #default="{ row }">
              <el-tag v-if="isAlarmEvent(row)" type="danger">报警</el-tag>
              <el-tag v-else-if="isRestoreEvent(row)" type="success">恢复</el-tag>
              <el-tag v-else type="info">其他</el-tag>
            </template>
          </el-table-column>
          <!-- <el-table-column label="参数" width="120">
          <template #default="{ row }">
            <span>{{ row.paramDesc || '-' }}: {{ row.point }}</span>
          </template>
        </el-table-column> -->
          <el-table-column prop="status" label="处理状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusColor(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="processor" label="处理人" width="100" show-overflow-tooltip />
          <el-table-column prop="processTime" label="处理时间" width="160" show-overflow-tooltip />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleViewDetail(row)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-button
                link
                type="success"
                @click="handleProcess(row)"
                :disabled="row.status === 'completed'"
              >
                <el-icon><Check /></el-icon>
                处理
              </el-button>
              <el-button
                link
                type="warning"
                @click="handleIgnore(row)"
                :disabled="row.status !== 'pending'"
              >
                <el-icon><Close /></el-icon>
                忽略
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>

      <!-- 详情弹框 -->
      <el-dialog v-model="detailDialogVisible" title="告警详情" width="800px" destroy-on-close>
        <div v-if="currentAlarm" class="alarm-detail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="事件时间">
              {{ currentAlarm.eventTime }}
            </el-descriptions-item>
            <!-- <el-descriptions-item label="事件级别">
            <el-tag :type="getLevelColor(currentAlarm.eventLevel)" effect="dark">
              {{ getLevelText(currentAlarm.eventLevel) }}
            </el-tag>
          </el-descriptions-item> -->
            <el-descriptions-item label="报警主机">
              {{ currentAlarm.hostName }}
            </el-descriptions-item>
            <el-descriptions-item label="事件名称">
              {{ currentAlarm.eventName }}
            </el-descriptions-item>
            <el-descriptions-item label="事件码">
              {{ currentAlarm.eventCode }}
            </el-descriptions-item>
            <el-descriptions-item label="序列号">
              {{ currentAlarm.sequence }}
            </el-descriptions-item>
            <el-descriptions-item label="分区">
              {{ currentAlarm.area }}
            </el-descriptions-item>
            <el-descriptions-item label="参数">
              {{ currentAlarm.paramDesc || '-' }}: {{ currentAlarm.point }}
            </el-descriptions-item>
            <el-descriptions-item label="处理状态">
              <el-tag :type="getStatusColor(currentAlarm.status)">
                {{ getStatusText(currentAlarm.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="是否需要记录">
              <el-tag :type="currentAlarm.needRecord ? 'success' : 'info'">
                {{ currentAlarm.needRecord ? '是' : '否' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="处理人" v-if="currentAlarm.processor">
              {{ currentAlarm.processor }}
            </el-descriptions-item>
            <el-descriptions-item label="处理时间" v-if="currentAlarm.processTime">
              {{ currentAlarm.processTime }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间" span="2">
              {{ currentAlarm.createTime }}
            </el-descriptions-item>
            <el-descriptions-item label="处理备注" span="2" v-if="currentAlarm.processRemark">
              {{ currentAlarm.processRemark }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-dialog>

      <!-- 处理弹框 -->
      <el-dialog
        v-model="processDialogVisible"
        title="处理告警"
        width="500px"
        destroy-on-close
        :close-on-click-modal="false"
      >
        <el-form
          ref="processFormRef"
          :model="processForm"
          :rules="processRules"
          label-width="100px"
        >
          <el-form-item label="处理结果" prop="result">
            <el-radio-group v-model="processForm.result">
              <el-radio label="confirmed">确认告警</el-radio>
              <el-radio label="false_alarm">误报</el-radio>
              <el-radio label="resolved">已解决</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="处理措施" prop="actions">
            <el-checkbox-group v-model="processForm.actions">
              <el-checkbox label="现场检查">现场检查</el-checkbox>
              <el-checkbox label="联系安保">联系安保</el-checkbox>
              <el-checkbox label="设备检修">设备检修</el-checkbox>
              <el-checkbox label="记录存档">记录存档</el-checkbox>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item label="处理备注" prop="remark">
            <el-input
              v-model="processForm.remark"
              type="textarea"
              :rows="4"
              placeholder="请输入处理备注信息"
            />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="processDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleConfirmProcess">确认处理</el-button>
        </template>
      </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Calendar,
  MapLocation,
  CircleCheck,
  View,
  Check,
  Close
} from '@element-plus/icons-vue'
import {
  getAlarmEventPage,
  getAlarmEvent,
  processAlarmEvent,
  ignoreAlarmEvent,
  getAlarmEventStats,
  exportAlarmEvent,
  type IotAlarmEventVO,
  type IotAlarmEventPageReqVO
} from '@/api/iot/alarm/event'
import { getAlarmHostPage } from '@/api/iot/alarm/host'
import { formatDateTime } from '@/utils/formatTime'
import { iotWebSocket } from '@/utils/iotWebSocket'

// 响应式数据
const loading = ref(false)
const detailDialogVisible = ref(false)
const processDialogVisible = ref(false)
const currentAlarm = ref<IotAlarmEventVO | null>(null)

// 使用全局 IoT WebSocket（见 utils/iotWebSocket.ts）

// 搜索表单
const searchForm = reactive({
  alarmTime: [] as string[],
  eventLevel: '',
  hostId: undefined as number | undefined,
  status: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 处理表单
const processForm = reactive({
  result: '',
  actions: [],
  remark: ''
})

const processRules = {
  result: [{ required: true, message: '请选择处理结果', trigger: 'change' }],
  remark: [{ required: true, message: '请输入处理备注', trigger: 'blur' }]
}

// 统计数据
const stats = reactive({
  urgentCount: 0,
  todayCount: 0,
  activeHosts: 0,
  processedRate: 0,
  // 下列字段用于顶部四个统计卡片
  total: 0,
  alarm: 0,
  restore: 0,
  other: 0
})

// 报警主机选项（从报警主机列表获取）
const hostOptions = ref<Array<{ label: string; value: number }>>([])

// 告警记录数据
const alarmList = ref<IotAlarmEventVO[]>([])

const processFormRef = ref()


// 判断是否为报警事件（用于颜色区分）
const isAlarmEvent = (event: IotAlarmEventVO) => {
  return event.isAlarm === true
}

// 判断是否为恢复事件
const isRestoreEvent = (event: IotAlarmEventVO) => {
  return event.isRestore === true
}

// 获取状态颜色
const getStatusColor = (status?: string) => {
  const colors: Record<string, string> = {
    pending: 'danger',
    processing: 'warning',
    completed: 'success',
    ignored: 'info'
  }
  return colors[status || 'pending'] || 'info'
}

// 获取状态文本
const getStatusText = (status?: string) => {
  const texts: Record<string, string> = {
    pending: '待处理',
    processing: '处理中',
    completed: '已处理',
    ignored: '已忽略'
  }
  return texts[status || 'pending'] || status || '待处理'
}

// 事件处理
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    alarmTime: [],
    eventLevel: '',
    hostId: undefined,
    status: ''
  })
  handleSearch()
}

// 导出/刷新按钮在当前模板中未使用（如后续需要，可在 header 区域补按钮后启用）

const handleViewDetail = async (row: IotAlarmEventVO) => {
  try {
    if (row.id) {
      const data = await getAlarmEvent(row.id)
      if (data) {
        currentAlarm.value = data
        detailDialogVisible.value = true
        return
      }
    }
    // 无 id 或后端未返回数据，回退使用行数据
    currentAlarm.value = row
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    // 404 或接口异常时，使用行数据回退展示，避免阻塞
    currentAlarm.value = row
    detailDialogVisible.value = true
    ElMessage.warning('无法获取详情，已使用当前行数据展示')
  }
}

const handleProcess = (row: IotAlarmEventVO) => {
  currentAlarm.value = row
  processForm.result = ''
  processForm.actions = []
  processForm.remark = ''
  processDialogVisible.value = true
}

const handleIgnore = async (row: IotAlarmEventVO) => {
  try {
    await ElMessageBox.confirm('确认忽略该告警吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    if (row.id) {
      await ignoreAlarmEvent(row.id)
      ElMessage.success('告警已忽略')
      await loadData()
      await loadStats()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('忽略失败:', error)
      ElMessage.error('忽略失败')
    }
  }
}

const handleConfirmProcess = async () => {
  if (!processFormRef.value || !currentAlarm.value?.id) return

  try {
    await processFormRef.value.validate()

    await processAlarmEvent({
      id: currentAlarm.value.id,
      result: processForm.result,
      actions: processForm.actions,
      remark: processForm.remark
    })

    processDialogVisible.value = false
    ElMessage.success('告警处理完成')
    await loadData()
    await loadStats()
  } catch (error) {
    console.error('处理失败:', error)
    ElMessage.error('处理失败')
  }
}

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    const params: IotAlarmEventPageReqVO = {
      pageNo: pagination.page,
      pageSize: pagination.size,
      startTime: searchForm.alarmTime?.[0],
      endTime: searchForm.alarmTime?.[1],
      eventLevel: searchForm.eventLevel,
      hostId: searchForm.hostId,
      status: searchForm.status
    }
    const data = await getAlarmEventPage(params)
    // 将后端字段映射为表格展示所需字段，补齐时间、主机、事件名称
    const list = (data.list || []).map((item: any) => {
      const rawTs =
        item?.timestamp ??
        (typeof item?.eventTime === 'string' ? Date.parse(item.eventTime) : item?.eventTime) ??
        item?.createTime ??
        Date.now()
      const tsNum = typeof rawTs === 'number' ? rawTs : Date.parse(rawTs)
      const eventTimeStr = formatDateTime(tsNum)
      const vo: IotAlarmEventVO = {
        id: item?.id,
        hostId: item?.hostId,
        hostName: item?.hostName || item?.account,
        eventCode: item?.eventCode || item?.code || '',
        eventName: item?.eventName || item?.eventDesc || item?.eventType || '',
        eventLevel: item?.eventLevel || item?.level || 'WARNING',
        area: item?.area?.toString?.() ?? item?.partitionNo?.toString?.() ?? '',
        point: item?.point?.toString?.() ?? item?.zoneNo?.toString?.() ?? '',
        sequence: item?.sequence?.toString?.() ?? '',
        timestamp: tsNum,
        eventTime: eventTimeStr,
        paramDesc: item?.paramDesc,
        isAlarm: item?.isAlarm,
        isRestore: item?.isRestore,
        needRecord: item?.needRecord,
        status: item?.status,
        processor: item?.processor,
        processTime: item?.processTime,
        processRemark: item?.processRemark,
        createTime: item?.createTime
      }
      return vo
    })
    alarmList.value = list
    pagination.total = data.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    const data = await getAlarmEventStats()
    Object.assign(stats, data)
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

// 加载报警主机选项
const loadHostOptions = async () => {
  try {
    const data = await getAlarmHostPage({ pageNo: 1, pageSize: 100 })
    hostOptions.value = (data.list || []).map((item: any) => ({
      label: item.hostName || item.account || `主机${item.id}`,
      value: item.id
    }))
  } catch (error) {
    console.error('加载主机选项失败:', error)
  }
}

// 实时事件处理（来自 iotWebSocket 的 alarm_event）
const handleRealtimeAlarmEvent = (data: any) => {
  try {
    const eventTimeStr = formatDateTime(data?.eventTime || data?.timestamp || Date.now())
    const vo: IotAlarmEventVO = {
      id: data?.id,
      hostId: data?.hostId,
      hostName: data?.hostName || data?.account,
      eventCode: data?.eventCode || data?.code || '',
      eventName: data?.eventName || data?.eventDesc || data?.eventType || '告警事件',
      eventLevel: data?.eventLevel || data?.level || 'WARNING',
      area: data?.area?.toString?.() ?? data?.partitionNo?.toString?.() ?? '',
      point: data?.point?.toString?.() ?? data?.zoneNo?.toString?.() ?? '',
      sequence: data?.sequence?.toString?.() ?? '',
      timestamp:
        data?.timestamp ||
        (typeof data?.eventTime === 'string' ? Date.parse(data?.eventTime) : Date.now()),
      eventTime: eventTimeStr,
      paramDesc: data?.paramDesc,
      isAlarm: data?.isAlarm,
      isRestore: data?.isRestore,
      needRecord: data?.needRecord,
      status: data?.status
    }

    // 添加到列表顶部
    alarmList.value.unshift(vo)
    if (alarmList.value.length > pagination.size) {
      alarmList.value.pop()
    }

    // 更新统计
    loadStats()

    // 提示音
    if (vo.eventLevel === 'CRITICAL' || vo.eventLevel === 'ERROR') {
      playAlarmSound()
    }

    // 通知
    ElMessage({
      message: `新报警：${vo.eventName} - ${vo.hostName || ''}`,
      type: vo.eventLevel === 'CRITICAL' ? 'error' : 'warning',
      duration: 5000
    })
  } catch (error) {
    console.error('[WS] 处理实时事件失败:', error)
  }
}

// 旧的原生 WebSocket 逻辑已移除，改为全局 iotWebSocket

// 播放报警提示音
const playAlarmSound = () => {
  try {
    const audio = new Audio('/sounds/alarm.mp3')
    audio.play().catch((err) => {
      console.warn('[提示音] 播放失败:', err)
    })
  } catch (error) {
    console.warn('[提示音] 创建失败:', error)
  }
}

onMounted(async () => {
  // 并行加载数据、统计、主机选项
  await Promise.all([loadData(), loadStats(), loadHostOptions()])
  // 复用全局 IoT WebSocket
  if (!iotWebSocket.isConnected()) {
    iotWebSocket.connect()
  }
  iotWebSocket.on('alarm_event', handleRealtimeAlarmEvent)
})

onUnmounted(() => {
  console.log('[周界报警记录] 组件卸载，取消 IoT WebSocket 监听')
  iotWebSocket.off('alarm_event', handleRealtimeAlarmEvent)
})
</script>

<style scoped lang="scss">
@use '@/styles/dark-theme.scss';

.alarm-record-page {
  padding: 10px;

  // 顶部统计条
  .top-stats-bar {
    display: flex;
    background-color: var(--el-bg-color-overlay);
    border: 1px solid var(--el-border-color);
    border-radius: 8px;
    margin-bottom: 12px;

    .top-stat-item {
      flex: 1;
      display: flex;
      align-items: center;
      padding: 12px 20px;
      border-right: 1px solid var(--el-border-color);

      &:last-child {
        border-right: none;
      }

      .top-stat-value {
        font-size: 18px;
        font-weight: 700;
        color: var(--el-text-color-primary);
        margin-right: 8px;

        &.alarm-color {
          color: #f56c6c;
        }

        &.restore-color {
          color: #67c23a;
        }
      }

      .top-stat-label {
        font-size: 13px;
        color: var(--el-text-color-secondary);
      }
    }
  }

  // 搜索 + 统计卡片容器
  .search-stats-container {
    display: flex;
    align-items: stretch; // 让左右两侧等高
    gap: 12px;
    margin-bottom: 12px;

    .search-form {
      flex: 1;
      background-color: var(--el-bg-color-overlay);
      border: 1px solid var(--el-border-color);
      border-radius: 8px;
      padding: 10px 14px;
      display: flex;
      flex-direction: column;
      justify-content: space-between;

      .search-row {
        display: flex;
        align-items: center;

        &:first-child {
          margin-bottom: 8px;
        }

        .el-form {
          margin-bottom: 0;
        }

        .el-form-item {
          margin-bottom: 0;
          margin-right: 10px;
        }

        .search-buttons {
          margin-left: auto;
          margin-right: 0;
        }
      }
    }

    .inline-stats {
      display: flex;
      gap: 10px;

      .stat-card {
        cursor: pointer;
        transition: all 0.3s;
        background-color: var(--el-bg-color-overlay);
        border: 1px solid var(--el-border-color);
        border-radius: 8px;
        padding: 0;
        min-width: 120px;
        display: flex;
        align-items: center;

        :deep(.el-card__body) {
          padding: 10px 14px;
          display: flex;
          align-items: center;
          height: 100%;
        }

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }

        .stat-content {
          display: flex;
          align-items: center;
          gap: 8px;

          .stat-icon {
            width: 36px;
            height: 36px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 20px;
            color: #fff;
          }

          .stat-info {
            .stat-value {
              font-size: 20px;
              font-weight: 700;
              color: var(--el-text-color-primary);
              line-height: 1;
            }

            .stat-label {
              font-size: 12px;
              color: var(--el-text-color-secondary);
              margin-top: 4px;
            }
          }
        }

        &.today .stat-icon {
          background: linear-gradient(45deg, #409eff, #66b1ff);
        }

        &.zones .stat-icon {
          background: linear-gradient(45deg, #e6a23c, #f1c40f);
        }

        &.processed .stat-icon {
          background: linear-gradient(45deg, #67c23a, #85ce61);
        }
      }
    }
  }

  .table-container {
    background-color: var(--el-bg-color-overlay);
    border: 1px solid var(--el-border-color);
    border-radius: 8px;
    padding: 16px;

    .el-pagination {
      margin-top: 16px;
      text-align: right;
    }
  }
}
</style>
