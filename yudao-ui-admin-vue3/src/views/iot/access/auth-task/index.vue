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
  <div class="auth-task-page">
    <!-- 顶部工具栏 -->
    <div class="page-header">
      <div class="header-left">
        <el-select v-model="queryParams.status" placeholder="状态筛选" clearable size="small" style="width: 130px" @change="handleQuery">
          <el-option v-for="item in AccessAuthTaskStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="queryParams.taskType" placeholder="任务类型" clearable size="small" style="width: 130px" @change="handleQuery">
          <el-option v-for="item in AccessAuthTaskTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button size="small" @click="handleQuery">
          <Icon icon="ep:refresh" class="mr-4px" />刷新
        </el-button>
      </div>
      <div class="header-right">
        <span class="ws-status" :class="{ connected: wsConnected }">
          <Icon :icon="wsConnected ? 'ep:connection' : 'ep:warning'" class="mr-4px" />
          {{ wsConnected ? '实时更新已连接' : '实时更新未连接' }}
        </span>
        <span class="total-info">共 {{ total }} 条记录</span>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="page-content">
      <el-table v-loading="loading" :data="list" size="small" height="100%">
        <el-table-column label="任务类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getTaskTypeTag(row.taskType)">{{ getTaskTypeLabel(row.taskType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="权限组" prop="groupName" width="160" show-overflow-tooltip />
        <el-table-column label="下发进度" width="260">
          <template #default="{ row }">
            <div class="progress-cell">
              <el-progress :percentage="getProgress(row)" :status="getProgressStatus(row)" :stroke-width="10" />
              <span class="progress-text">{{ getProgress(row) }}%</span>
            </div>
            <div v-if="row.status === 1 && row.currentPersonName" class="progress-detail">
              正在处理: {{ row.currentPersonName }} → {{ row.currentDeviceName || '设备' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="成功/失败/总数" width="140" align="center">
          <template #default="{ row }">
            <span class="count-success">{{ row.successCount }}</span>
            <span class="count-divider">/</span>
            <span class="count-fail">{{ row.failCount }}</span>
            <span class="count-divider">/</span>
            <span class="count-total">{{ row.totalCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="执行时间" width="340">
          <template #default="{ row }">
            <template v-if="row.startTime">
              {{ formatDateTime(row.startTime) }}
              <span v-if="row.endTime"> ~ {{ formatDateTime(row.endTime) }}</span>
            </template>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">
              <Icon icon="ep:view" class="mr-2px" />详情
            </el-button>
            <el-button v-if="row.status === 3 || row.status === 4" link type="warning" size="small" @click="handleRetry(row)">
              <Icon icon="ep:refresh-right" class="mr-2px" />重试
            </el-button>
            <el-button v-if="row.status === 1" link type="danger" size="small" @click="handleCancel(row)">
              <Icon icon="ep:close" class="mr-2px" />取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="page-footer">
      <el-pagination
        v-model:current-page="queryParams.pageNo"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[20, 50, 100]"
        :total="total"
        layout="sizes, prev, pager, next, jumper"
        small
        @size-change="handleQuery"
        @current-change="getList"
      />
    </div>

    <!-- 详情弹窗 -->
    <TaskDetailDialog 
      v-model:visible="detailDialogVisible" 
      :task="currentTask" 
      @retry="handleRetryDetail"
    />
  </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  AccessAuthTaskApi, 
  AccessAuthTaskStatusOptions, 
  AccessAuthTaskTypeOptions,
  type AccessAuthTaskVO,
  type AuthTaskProgressMessage
} from '@/api/iot/access'
import { formatDate, formatDateTime } from '@/utils/formatTime'
import TaskDetailDialog from './TaskDetailDialog.vue'
import { useAuthTaskWebSocket } from './useAuthTaskWebSocket'

defineOptions({ name: 'AccessAuthTask' })

const loading = ref(false)
const list = ref<AccessAuthTaskVO[]>([])
const total = ref(0)
const detailDialogVisible = ref(false)
const currentTask = ref<AccessAuthTaskVO | null>(null)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  status: undefined as number | undefined,
  taskType: undefined as string | undefined
})

// ========== 进度计算 ==========
const getProgress = (row: AccessAuthTaskVO) => {
  if (row.totalCount === 0) return 0
  return Math.round(((row.successCount + row.failCount) / row.totalCount) * 100)
}

const getProgressStatus = (row: AccessAuthTaskVO) => {
  if (row.status === 2) return 'success'
  if (row.status === 4) return 'exception'
  return undefined
}

// ========== 状态和类型显示 ==========
const getStatusType = (status: number) => {
  const item = AccessAuthTaskStatusOptions.find(i => i.value === status)
  return item?.type || 'info'
}

const getStatusLabel = (status: number) => {
  const item = AccessAuthTaskStatusOptions.find(i => i.value === status)
  return item?.label || '未知'
}

const getTaskTypeLabel = (taskType: string) => {
  const item = AccessAuthTaskTypeOptions.find(i => i.value === taskType)
  return item?.label || taskType
}

const getTaskTypeTag = (taskType: string) => {
  switch (taskType) {
    case 'group_dispatch': return 'primary'
    case 'person_dispatch': return 'success'
    case 'revoke': return 'danger'
    case 'incremental': return 'warning'
    default: return 'info'
  }
}

// ========== 数据获取 ==========
const getList = async () => {
  loading.value = true
  try {
    const res = await AccessAuthTaskApi.getTaskPage(queryParams)
    list.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('获取任务列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

// ========== 任务操作 ==========
const handleViewDetail = async (row: AccessAuthTaskVO) => {
  currentTask.value = row
  detailDialogVisible.value = true
}

const handleRetry = async (row: AccessAuthTaskVO) => {
  try {
    await ElMessageBox.confirm('确定要重试失败的任务明细吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await AccessAuthTaskApi.retryTask(row.id)
    ElMessage.success('重试任务已提交')
    getList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('重试失败:', error)
    }
  }
}

const handleRetryDetail = async (taskId: number) => {
  try {
    await AccessAuthTaskApi.retryTask(taskId)
    ElMessage.success('重试任务已提交')
    getList()
  } catch (error) {
    console.error('重试失败:', error)
  }
}

const handleCancel = async (row: AccessAuthTaskVO) => {
  try {
    await ElMessageBox.confirm('确定要取消正在执行的任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await AccessAuthTaskApi.cancelTask(row.id)
    ElMessage.success('任务已取消')
    getList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('取消失败:', error)
    }
  }
}

// ========== WebSocket 连接 (Requirements: 13.5) ==========
// 使用单例 WebSocket Hook，避免重复连接
const { connected: wsConnected, connect: connectWs, disconnect: disconnectWs } = useAuthTaskWebSocket({
  autoConnect: false, // 手动连接
  onProgress: handleProgressUpdate,
  onCompleted: handleTaskCompleted,
  onConnectionChange: (connected) => {
    console.log(`[授权任务] WebSocket ${connected ? '✅ 已连接' : '❌ 已断开'}`)
  }
})

function handleProgressUpdate(progress: AuthTaskProgressMessage) {
  // 更新列表中对应任务的进度
  const index = list.value.findIndex(item => item.id === progress.taskId)
  if (index !== -1) {
    const task = list.value[index]
    task.successCount = progress.successCount
    task.failCount = progress.failCount
    task.status = progress.status
    // 添加实时处理信息
    ;(task as any).currentPersonName = progress.currentPersonName
    ;(task as any).currentDeviceName = progress.currentDeviceName
    
    // 如果有错误，显示提示
    if (progress.latestError) {
      console.warn('[AuthTask] 任务执行错误:', progress.latestError)
    }
  }
  
  // 如果当前正在查看该任务的详情，也更新
  if (currentTask.value && currentTask.value.id === progress.taskId) {
    currentTask.value.successCount = progress.successCount
    currentTask.value.failCount = progress.failCount
    currentTask.value.status = progress.status
  }
}

function handleTaskCompleted(progress: AuthTaskProgressMessage) {
  // 任务完成，刷新列表
  getList()
  
  // 显示完成通知
  if (progress.failCount === 0) {
    ElMessage.success(`任务完成：成功 ${progress.successCount} 条`)
  } else if (progress.successCount === 0) {
    ElMessage.error(`任务失败：失败 ${progress.failCount} 条`)
  } else {
    ElMessage.warning(`任务部分完成：成功 ${progress.successCount} 条，失败 ${progress.failCount} 条`)
  }
}

// ========== 生命周期 ==========
onMounted(() => {
  getList()
  // 使用单例 WebSocket，手动连接
  connectWs()
})

onUnmounted(() => {
  disconnectWs()
})
</script>


<style scoped lang="scss">
.auth-task-page {
  height: calc(100vh - 84px);
  display: flex;
  flex-direction: column;
  background: #1e1e2d;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #252532;
  border-bottom: 1px solid #2d2d3a;

  .header-left {
    display: flex;
    gap: 10px;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .ws-status {
      display: flex;
      align-items: center;
      font-size: 12px;
      color: #f56c6c;
      padding: 4px 8px;
      background: rgba(245, 108, 108, 0.1);
      border-radius: 4px;
      
      &.connected {
        color: #67c23a;
        background: rgba(103, 194, 58, 0.1);
      }
    }
    
    .total-info {
      font-size: 12px;
      color: #8c8c9a;
    }
  }

  .el-button {
    background: #2d2d3a;
    border-color: #3d3d4a;
    color: #c4c4c4;

    &:hover {
      background: #3d3d4a;
      color: #e0e0e0;
    }
  }

  :deep(.el-select__wrapper) {
    background: #2d2d3a;
    box-shadow: none;
    border: 1px solid #3d3d4a;
  }
}

.page-content {
  flex: 1;
  overflow: hidden;
  padding: 12px 16px;

  :deep(.el-table) {
    background: transparent;
    --el-table-bg-color: transparent;
    --el-table-tr-bg-color: transparent;
    --el-table-header-bg-color: #252532;
    --el-table-row-hover-bg-color: #2d2d3a;
    --el-table-border-color: #2d2d3a;
    --el-table-text-color: #c4c4c4;
    --el-table-header-text-color: #e0e0e0;

    th.el-table__cell {
      background: #252532;
    }
  }

  .progress-cell {
    display: flex;
    align-items: center;
    gap: 8px;

    .el-progress {
      flex: 1;
    }

    .progress-text {
      width: 40px;
      font-size: 12px;
      color: #8c8c9a;
    }
  }
  
  .progress-detail {
    font-size: 11px;
    color: #6c6c7a;
    margin-top: 4px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .count-success { color: #67c23a; font-weight: 500; }
  .count-fail { color: #f56c6c; font-weight: 500; }
  .count-total { color: #c4c4c4; }
  .count-divider { color: #6c6c7a; margin: 0 2px; }
  
  .text-muted { color: #6c6c7a; }
}

.page-footer {
  display: flex;
  justify-content: flex-end;
  padding: 12px 16px;
  background: #252532;
  border-top: 1px solid #2d2d3a;

  :deep(.el-pagination) {
    --el-pagination-bg-color: #2d2d3a;
    --el-pagination-text-color: #c4c4c4;
    --el-pagination-button-bg-color: #2d2d3a;
    --el-pagination-hover-color: #409eff;
  }
}
</style>
