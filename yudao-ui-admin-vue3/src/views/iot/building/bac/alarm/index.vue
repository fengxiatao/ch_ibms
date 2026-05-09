<template>
  <div class="bac-alarm-page">
    <!-- 告警统计 -->
    <el-row :gutter="16" class="mb-16px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card danger">
          <div class="stat-value">{{ statistics.urgentAlarmCount || 0 }}</div>
          <div class="stat-label">紧急告警</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card warning">
          <div class="stat-value">{{ statistics.unhandledAlarmCount || 0 }}</div>
          <div class="stat-label">未处理</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card info">
          <div class="stat-value">{{ statistics.todayAlarmCount || 0 }}</div>
          <div class="stat-label">今日告警</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card success">
          <div class="stat-value">
            {{ Math.max(0, (statistics.todayAlarmCount || 0) - (statistics.unhandledAlarmCount || 0)) }}
          </div>
          <div class="stat-label">今日已处理</div>
        </el-card>
      </el-col>
    </el-row>

    <ContentWrap>
      <!-- 筛选条件 -->
      <el-form :inline="true" :model="queryParams" class="-mb-15px">
        <el-form-item label="告警级别" prop="alarmLevel">
          <el-select
            v-model="queryParams.alarmLevel"
            placeholder="全部级别"
            clearable
            class="!w-140px"
          >
            <el-option label="紧急" :value="3" />
            <el-option label="重要" :value="2" />
            <el-option label="提示" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="告警时间" prop="dateRange">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            class="!w-260px"
          />
        </el-form-item>
        <el-form-item label="处理状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部" clearable class="!w-120px">
            <el-option label="未处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已处理" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备" prop="deviceName">
          <el-input
            v-model="queryParams.deviceName"
            placeholder="搜索设备名称"
            clearable
            class="!w-200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
          <el-button type="success" @click="handleExport">
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="告警时间" prop="alarmTime" width="180">
          <template #default="{ row }">
            {{ formatDate(row.alarmTime) }}
          </template>
        </el-table-column>
        <el-table-column label="告警级别" prop="alarmLevel" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.alarmLevel)" size="small">
              {{ getLevelLabel(row.alarmLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="设备名称" prop="deviceName" min-width="150" />
        <el-table-column
          label="告警内容"
          prop="alarmContent"
          min-width="250"
          show-overflow-tooltip
        />
        <el-table-column label="持续时间" prop="duration" width="120">
          <template #default="{ row }">
            {{ row.duration || '持续中' }}
          </template>
        </el-table-column>
        <el-table-column label="处理状态" prop="status" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status !== 2" link type="warning" @click="handleAlarm(row)">
              处理
            </el-button>
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>

    <!-- 处理弹窗 -->
    <el-dialog v-model="handleDialogVisible" title="处理告警" width="500px">
      <el-form :model="handleForm" label-width="100px">
        <el-form-item label="告警内容">
          <div class="text-gray-500">{{ currentAlarm?.alarmContent }}</div>
        </el-form-item>
        <el-form-item label="处理人">
          <el-input v-model="handleForm.handler" placeholder="请输入处理人" />
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="handleForm.handleRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入处理备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="告警详情" width="600px">
      <el-descriptions v-if="currentAlarm" :column="2" border>
        <el-descriptions-item label="设备类型">
          {{ currentAlarm.deviceType === 1 ? '暖通' : currentAlarm.deviceType === 2 ? '给排水' : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ currentAlarm.deviceName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="设备编码">{{ currentAlarm.deviceCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="告警级别">
          <el-tag :type="getLevelType(currentAlarm.alarmLevel)" size="small">
            {{ getLevelLabel(currentAlarm.alarmLevel) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="告警时间" :span="2">
          {{ formatDate(currentAlarm.alarmTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="告警内容" :span="2">{{ currentAlarm.alarmContent || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="getStatusType(currentAlarm.status)" size="small">
            {{ getStatusLabel(currentAlarm.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理人">{{ currentAlarm.handler || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间" :span="2">
          {{ currentAlarm.handleTime ? formatDate(currentAlarm.handleTime) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="处理备注" :span="2">{{ currentAlarm.handleRemark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { formatDate } from '@/utils/formatTime'
import * as BacApi from '@/api/iot/building/bac'

defineOptions({ name: 'BacAlarm' })

const message = useMessage()
const loading = ref(false)
const list = ref<BacApi.IbmsBacAlarmVO[]>([])
const total = ref(0)
const statistics = ref<BacApi.IbmsBacStatisticsVO>({})

const queryParams = reactive<BacApi.IbmsBacAlarmPageReqVO>({
  pageNo: 1,
  pageSize: 20,
  alarmLevel: undefined,
  startTime: undefined,
  endTime: undefined,
  deviceName: undefined,
  status: undefined
})

const dateRange = ref<string[] | undefined>(undefined)

const handleDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentAlarm = ref<BacApi.IbmsBacAlarmVO | null>(null)
const handleForm = reactive({
  handler: '',
  handleRemark: ''
})

const getLevelType = (level?: number) => {
  const map: Record<number, string> = { 3: 'danger', 2: 'warning', 1: 'info' }
  return (level && map[level]) || 'info'
}

const getLevelLabel = (level?: number) => {
  const map: Record<number, string> = { 3: '紧急', 2: '重要', 1: '提示' }
  return (level && map[level]) || '未知'
}

const getStatusType = (status?: number) => {
  const map: Record<number, string> = { 0: 'danger', 1: 'warning', 2: 'success' }
  return (status !== undefined && map[status]) || 'info'
}

const getStatusLabel = (status?: number) => {
  const map: Record<number, string> = { 0: '未处理', 1: '处理中', 2: '已处理' }
  return (status !== undefined && map[status]) || '未知'
}

const fetchStatistics = async () => {
  try {
    statistics.value = (await BacApi.getStatistics()) || {}
  } catch (e) {
    statistics.value = {}
  }
}

const getList = async () => {
  loading.value = true
  try {
    if (dateRange.value && dateRange.value.length === 2) {
      queryParams.startTime = dateRange.value[0] + ' 00:00:00' as any
      queryParams.endTime = dateRange.value[1] + ' 23:59:59' as any
    } else {
      queryParams.startTime = undefined
      queryParams.endTime = undefined
    }
    const data = await BacApi.getAlarmPage(queryParams)
    list.value = data?.list || []
    total.value = data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.alarmLevel = undefined
  queryParams.deviceName = undefined
  queryParams.status = undefined
  dateRange.value = undefined
  handleQuery()
}

const handleExport = () => {
  message.warning('导出功能待后端接口支持')
}

const handleAlarm = (row: BacApi.IbmsBacAlarmVO) => {
  currentAlarm.value = row
  handleForm.handler = ''
  handleForm.handleRemark = ''
  handleDialogVisible.value = true
}

const submitHandle = async () => {
  if (!handleForm.handler) {
    message.warning('请输入处理人')
    return
  }
  if (!currentAlarm.value?.id) return
  await BacApi.handleAlarm(currentAlarm.value.id, handleForm.handler, handleForm.handleRemark)
  handleDialogVisible.value = false
  message.success('处理成功')
  await Promise.all([getList(), fetchStatistics()])
}

const viewDetail = (row: BacApi.IbmsBacAlarmVO) => {
  currentAlarm.value = row
  detailDialogVisible.value = true
}

onMounted(() => {
  fetchStatistics()
  getList()
})
</script>

<style lang="scss" scoped>
.bac-alarm-page {
  box-sizing: border-box;
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
}

.stat-card {
  text-align: center;
  padding: 20px;

  .stat-value {
    font-size: 32px;
    font-weight: bold;
  }

  .stat-label {
    color: var(--el-text-color-secondary);
    margin-top: 8px;
  }

  &.danger .stat-value {
    color: var(--el-color-danger);
  }

  &.warning .stat-value {
    color: var(--el-color-warning);
  }

  &.info .stat-value {
    color: var(--el-color-primary);
  }

  &.success .stat-value {
    color: var(--el-color-success);
  }
}
</style>
