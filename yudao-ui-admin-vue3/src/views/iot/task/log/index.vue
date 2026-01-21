<template>
  <ContentWrap>
    <!-- 筛选条件 -->
    <el-form :model="queryParams" :inline="true" label-width="100px">
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="queryParams.startTime"
          type="daterange"
          range-separator="-"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          class="!w-300px"
        />
      </el-form-item>
      <el-form-item label="实体类型">
        <el-select v-model="queryParams.entityType" clearable class="!w-200px">
          <el-option label="设备" value="DEVICE" />
          <el-option label="产品" value="PRODUCT" />
          <el-option label="楼层" value="FLOOR" />
        </el-select>
      </el-form-item>
      <el-form-item label="执行状态">
        <el-select v-model="queryParams.executionStatus" clearable class="!w-200px">
          <el-option label="成功" value="SUCCESS" />
          <el-option label="失败" value="FAILED" />
          <el-option label="运行中" value="RUNNING" />
        </el-select>
      </el-form-item>
      <el-form-item label="实体名称">
        <el-input
          v-model="queryParams.entityName"
          placeholder="请输入实体名称"
          clearable
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" />
          重置
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 日志列表 -->
    <el-table v-loading="loading" :data="logList" :stripe="true">
      <el-table-column label="开始时间" width="180">
        <template #default="{ row }">
          {{ formatDate(new Date(row.startTime)) }}
        </template>
      </el-table-column>
      <el-table-column label="任务名称" prop="jobName" min-width="150" />
      <el-table-column label="实体" min-width="150">
        <template #default="{ row }">
          <div>{{ row.entityName }}</div>
          <div class="text-xs text-gray-400">{{ row.entityType }}</div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.executionStatus)">
            {{ getStatusLabel(row.executionStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="耗时" width="100" align="center">
        <template #default="{ row }">
          {{ row.durationMs ? row.durationMs + 'ms' : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="影响数量" width="100" align="center" prop="affectedCount" />
      <el-table-column label="执行结果" min-width="200" show-overflow-tooltip prop="resultSummary" />
      <el-table-column label="操作" fixed="right" width="80" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleViewDetail(row)">
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 日志详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="执行日志详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-descriptions v-if="currentLog" :column="2" border>
        <el-descriptions-item label="任务名称">{{ currentLog.jobName }}</el-descriptions-item>
        <el-descriptions-item label="执行状态">
          <el-tag :type="getStatusType(currentLog.executionStatus)">
            {{ getStatusLabel(currentLog.executionStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="实体">
          {{ currentLog.entityName }} ({{ currentLog.entityType }})
        </el-descriptions-item>
        <el-descriptions-item label="执行器">{{ currentLog.executorInfo }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">
          {{ formatDate(new Date(currentLog.startTime)) }}
        </el-descriptions-item>
        <el-descriptions-item label="结束时间">
          {{ currentLog.endTime ? formatDate(new Date(currentLog.endTime)) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="执行时长">
          {{ currentLog.durationMs ? currentLog.durationMs + 'ms' : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="影响数量">
          {{ currentLog.affectedCount || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="执行结果" :span="2">
          {{ currentLog.resultSummary || '-' }}
        </el-descriptions-item>
        <el-descriptions-item v-if="currentLog.resultDetail" label="详细结果" :span="2">
          <pre class="result-detail">{{ currentLog.resultDetail }}</pre>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentLog.errorMessage" label="错误信息" :span="2">
          <div class="error-message">{{ currentLog.errorMessage }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentLog.errorStack" label="异常堆栈" :span="2">
          <pre class="error-stack">{{ currentLog.errorStack }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </ContentWrap>
</template>

<script setup lang="ts">
import { TaskLogApi } from '@/api/iot/task/log'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'IotTaskLog' })

const route = useRoute()

const loading = ref(false)
const logList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const currentLog = ref<any>(null)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  taskConfigId: undefined,
  entityType: '',
  entityId: undefined,
  entityName: '',
  jobType: '',
  executionStatus: '',
  startTime: []
})

const getList = async () => {
  loading.value = true
  try {
    const data = await TaskLogApi.getLogPage(queryParams)
    logList.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  Object.assign(queryParams, {
    pageNo: 1,
    pageSize: 20,
    taskConfigId: route.query.taskConfigId,
    entityType: '',
    entityId: undefined,
    entityName: '',
    jobType: '',
    executionStatus: '',
    startTime: []
  })
  getList()
}

const handleViewDetail = async (row: any) => {
  currentLog.value = await TaskLogApi.getLogDetail(row.id)
  dialogVisible.value = true
}

const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    SUCCESS: 'success',
    FAILED: 'danger',
    RUNNING: 'primary',
    TIMEOUT: 'warning'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    SUCCESS: '成功',
    FAILED: '失败',
    RUNNING: '运行中',
    TIMEOUT: '超时'
  }
  return map[status] || status
}

onMounted(() => {
  // 如果从监控页面跳转过来，带有taskConfigId参数
  if (route.query.taskConfigId) {
    queryParams.taskConfigId = Number(route.query.taskConfigId)
  }
  getList()
})
</script>

<style scoped lang="scss">
.result-detail,
.error-stack {
  max-height: 300px;
  overflow: auto;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}

.error-message {
  color: #f56c6c;
  padding: 8px;
  background: #fef0f0;
  border-radius: 4px;
  border-left: 3px solid #f56c6c;
}
</style>




