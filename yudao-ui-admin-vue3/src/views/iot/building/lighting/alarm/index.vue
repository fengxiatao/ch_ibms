<template>
  <div class="lighting-alarm-page">
    <!-- 告警统计 -->
    <el-row :gutter="16" class="mb-16px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card danger">
          <div class="stat-value">{{ statistics.unhandledCount || 3 }}</div>
          <div class="stat-label">未处理告警</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card warning">
          <div class="stat-value">{{ statistics.handlingCount || 1 }}</div>
          <div class="stat-label">处理中</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card success">
          <div class="stat-value">{{ statistics.resolvedCount || 28 }}</div>
          <div class="stat-label">今日已处理</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card primary">
          <div class="stat-value">{{ statistics.handleRate || 98 }}%</div>
          <div class="stat-label">处理率</div>
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
            <el-option label="严重" :value="3" />
            <el-option label="警告" :value="2" />
            <el-option label="提示" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态" prop="handleStatus">
          <el-select
            v-model="queryParams.handleStatus"
            placeholder="全部状态"
            clearable
            class="!w-140px"
          >
            <el-option label="未处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已解决" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="告警时间" prop="alarmTime">
          <el-date-picker
            v-model="queryParams.alarmTime"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            class="!w-160px"
          />
        </el-form-item>
        <el-form-item label="搜索" prop="keyword">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索设备名称/告警内容"
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
            <Icon icon="ep:download" class="mr-5px" /> 导出告警
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="告警时间" prop="alarmTime" width="180">
          <template #default="{ row }">
            <span style="font-family: monospace">{{ formatDate(row.alarmTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="告警级别" prop="alarmLevel" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.alarmLevel)" size="small">
              {{ getLevelLabel(row.alarmLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="设备名称" prop="deviceName" min-width="160" />
        <el-table-column
          label="告警内容"
          prop="alarmContent"
          min-width="250"
          show-overflow-tooltip
        />
        <el-table-column label="持续时间" prop="duration" width="100" />
        <el-table-column label="处理状态" prop="handleStatus" width="100">
          <template #default="{ row }">
            <el-tag :type="getHandleStatusType(row.handleStatus)" size="small">
              {{ getHandleStatusLabel(row.handleStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理人" prop="handler" width="100" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.handleStatus !== 2" link type="warning" @click="handleAlarm(row)">
              处理
            </el-button>
            <span v-else style="color: #909399">-</span>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'LightingAlarm' })

const message = useMessage()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const statistics = ref({})
const handleDialogVisible = ref(false)
const currentAlarm = ref<any>(null)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  alarmLevel: undefined as number | undefined,
  handleStatus: undefined as number | undefined,
  alarmTime: undefined as string | undefined,
  keyword: undefined as string | undefined
})

const handleForm = reactive({
  handler: '',
  handleRemark: ''
})

const getLevelType = (level: number) => {
  const map: Record<number, string> = { 3: 'danger', 2: 'warning', 1: 'info' }
  return map[level] || 'info'
}

const getLevelLabel = (level: number) => {
  const map: Record<number, string> = { 3: '严重', 2: '警告', 1: '提示' }
  return map[level] || '未知'
}

const getHandleStatusType = (status: number) => {
  const map: Record<number, string> = { 0: 'danger', 1: 'warning', 2: 'success' }
  return map[status] || 'info'
}

const getHandleStatusLabel = (status: number) => {
  const map: Record<number, string> = { 0: '未处理', 1: '处理中', 2: '已解决' }
  return map[status] || '未知'
}

const getList = async () => {
  loading.value = true
  try {
    // 模拟数据
    list.value = [
      {
        id: 1,
        alarmTime: new Date(),
        alarmLevel: 3,
        deviceName: '照明执行控制器-B1',
        alarmContent: '通道3电流过载，超过额定值120%',
        duration: '5分钟',
        handleStatus: 1,
        handler: '张工'
      },
      {
        id: 2,
        alarmTime: new Date(),
        alarmLevel: 2,
        deviceName: '智能照明网关-2F',
        alarmContent: '信号强度低于阈值（当前-75dBm）',
        duration: '15分钟',
        handleStatus: 0,
        handler: '-'
      },
      {
        id: 3,
        alarmTime: new Date(),
        alarmLevel: 1,
        deviceName: '照明回路-A1-08',
        alarmContent: '灯具离线，通信超时',
        duration: '已恢复',
        handleStatus: 2,
        handler: '系统自动'
      },
      {
        id: 4,
        alarmTime: new Date(),
        alarmLevel: 2,
        deviceName: '调光回路-B1-02',
        alarmContent: '色温调节异常，反馈值与设定值偏差过大',
        duration: '10分钟',
        handleStatus: 0,
        handler: '-'
      }
    ]
    total.value = 32
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
  queryParams.handleStatus = undefined
  queryParams.alarmTime = undefined
  queryParams.keyword = undefined
  handleQuery()
}

const handleExport = () => {
  console.log('导出告警信息')
}

const handleAlarm = (row: any) => {
  currentAlarm.value = row
  handleForm.handler = ''
  handleForm.handleRemark = ''
  handleDialogVisible.value = true
}

const submitHandle = () => {
  if (!handleForm.handler) {
    message.warning('请输入处理人')
    return
  }
  if (currentAlarm.value.handleStatus === 0) {
    currentAlarm.value.handleStatus = 1
    currentAlarm.value.handler = handleForm.handler
    message.success('已开始处理告警')
  } else {
    currentAlarm.value.handleStatus = 2
    message.success('告警已标记为已解决')
  }
  handleDialogVisible.value = false
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.lighting-alarm-page {
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

  &.success .stat-value {
    color: var(--el-color-success);
  }

  &.primary .stat-value {
    color: var(--el-color-primary);
  }
}
</style>
