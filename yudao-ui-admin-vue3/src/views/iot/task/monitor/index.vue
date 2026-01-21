<template>
  <div class="task-monitor">
    <ContentWrap style="margin-top: 70px">
      <!-- 统计卡片 -->
      <el-row :gutter="20" class="mb-4">
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-icon total">
                <Icon icon="ep:data-analysis" :size="32" />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ statistics.total || 0 }}</div>
                <div class="stat-label">总任务数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-icon enabled">
                <Icon icon="ep:check" :size="32" />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ statistics.enabled || 0 }}</div>
                <div class="stat-label">已启用</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-icon running">
                <Icon icon="ep:loading" :size="32" />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ statistics.running || 0 }}</div>
                <div class="stat-label">运行中</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-icon failed">
                <Icon icon="ep:warning" :size="32" />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ statistics.recentFailed || 0 }}</div>
                <div class="stat-label">最近失败</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </ContentWrap>
    <!-- 筛选条件 -->
    <ContentWrap>
      <el-form :model="queryParams" :inline="true" label-width="80px">
        <el-form-item label="实体类型">
          <el-select
            v-model="queryParams.entityType"
            clearable
            placeholder="请选择"
            class="!w-240px"
          >
            <el-option label="设备" value="DEVICE" />
            <el-option label="产品" value="PRODUCT" />
            <el-option label="楼层" value="FLOOR" />
            <el-option label="建筑" value="BUILDING" />
            <el-option label="园区" value="CAMPUS" />
            <el-option label="区域" value="AREA" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务类型">
          <el-select v-model="queryParams.jobType" clearable placeholder="请选择" class="!w-240px">
            <el-option label="离线检查" value="IOT_DEVICE_OFFLINE_CHECK" />
            <el-option label="健康检查" value="IOT_DEVICE_HEALTH_CHECK" />
            <el-option label="数据采集" value="IOT_DEVICE_DATA_COLLECT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.enabled" clearable placeholder="请选择" class="!w-240px">
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="实体名称或任务名称"
            clearable
            class="!w-240px"
            @keyup.enter="handleQuery"
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
    </ContentWrap>

    <!-- 任务列表 -->
    <ContentWrap>
      <el-table v-loading="loading" :data="taskList" :stripe="true">
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="任务名称" prop="jobName" min-width="150" />
        <el-table-column label="实体" min-width="150">
          <template #default="{ row }">
            <div>{{ row.entityName }}</div>
            <div class="text-xs text-gray-400">{{ row.entityType }}</div>
          </template>
        </el-table-column>
        <el-table-column label="执行间隔" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.intervalSeconds">{{ Math.floor(row.intervalSeconds / 60) }}分钟</span>
            <span v-else-if="row.cronExpression">{{ row.cronExpression }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="执行统计" width="150" align="center">
          <template #default="{ row }">
            <div class="text-xs">
              成功: <span class="text-green-600">{{ row.successCount || 0 }}</span> / 失败:
              <span class="text-red-600">{{ row.failCount || 0 }}</span>
            </div>
            <div class="text-xs text-gray-400">
              平均耗时: {{ row.avgDurationMs ? row.avgDurationMs + 'ms' : '-' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="上次执行" width="180">
          <template #default="{ row }">
            <div v-if="row.lastExecutionTime">
              <div class="text-xs">{{ formatTime(row.lastExecutionTime) }}</div>
              <el-tag
                v-if="row.lastExecutionStatus"
                :type="getStatusType(row.lastExecutionStatus)"
                size="small"
              >
                {{ getStatusLabel(row.lastExecutionStatus) }}
              </el-tag>
            </div>
            <span v-else class="text-gray-400">未执行</span>
          </template>
        </el-table-column>
        <el-table-column label="下次执行" width="180">
          <template #default="{ row }">
            <span v-if="row.nextExecutionTime">{{ formatTime(row.nextExecutionTime) }}</span>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleToggle(row)">
              {{ row.enabled ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="warning" @click="handleExecuteNow(row)"> 立即执行 </el-button>
            <el-button link type="info" @click="handleViewLog(row)"> 日志 </el-button>
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
  </div>
</template>

<script setup lang="ts">
import { TaskConfigApi } from '@/api/iot/task/config'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'IotTaskMonitor' })

const { push } = useRouter()
const message = useMessage()

const loading = ref(false)
const taskList = ref([])
const total = ref(0)
const statistics = ref({
  total: 0,
  enabled: 0,
  disabled: 0,
  running: 0,
  recentSuccess: 0,
  recentFailed: 0
})

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  category: '',
  entityType: '',
  jobType: '',
  enabled: undefined,
  keyword: ''
})

const getList = async () => {
  loading.value = true
  try {
    const data = await TaskConfigApi.getTaskMonitorPage(queryParams)
    taskList.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const getStatistics = async () => {
  statistics.value = await TaskConfigApi.getStatistics()
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  Object.assign(queryParams, {
    pageNo: 1,
    pageSize: 10,
    category: '',
    entityType: '',
    jobType: '',
    enabled: undefined,
    keyword: ''
  })
  getList()
}

const handleToggle = async (row: any) => {
  try {
    await message.confirm(`确认${row.enabled ? '禁用' : '启用'}任务【${row.jobName}】吗？`)
    await TaskConfigApi.toggleTask(row.id, !row.enabled)
    message.success(row.enabled ? '禁用成功' : '启用成功')
    await getList()
    await getStatistics()
  } catch {}
}

const handleExecuteNow = async (row: any) => {
  try {
    await message.confirm(`确认立即执行任务【${row.jobName}】吗？`)
    await TaskConfigApi.executeNow(row.id)
    message.success('任务已提交执行')
  } catch {}
}

const handleViewLog = (row: any) => {
  push({ name: 'IotTaskLog', query: { taskConfigId: row.id } })
}

const formatTime = (time: any) => {
  return formatDate(new Date(time))
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
  getList()
  getStatistics()

  // 每30秒刷新一次统计数据
  setInterval(() => {
    getStatistics()
  }, 30000)
})
</script>

<style scoped lang="scss">
.task-monitor {
  .stat-card {
    display: flex;
    align-items: center;
    gap: 16px;

    .stat-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 60px;
      height: 60px;
      border-radius: 12px;

      &.total {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
      }

      &.enabled {
        background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
        color: white;
      }

      &.running {
        background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%);
        color: #409eff;
      }

      &.failed {
        background: linear-gradient(135deg, #ffa1a1 0%, #ffc1c1 100%);
        color: #f56c6c;
      }
    }

    .stat-content {
      flex: 1;

      .stat-value {
        font-size: 28px;
        font-weight: bold;
        color: #303133;
        line-height: 1;
      }

      .stat-label {
        margin-top: 8px;
        color: #909399;
        font-size: 14px;
      }
    }
  }
}
</style>
