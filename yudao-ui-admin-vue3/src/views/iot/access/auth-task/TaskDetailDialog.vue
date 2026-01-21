<template>
  <el-dialog
    v-model="dialogVisible"
    title="任务详情"
    width="900px"
    append-to-body
    class="dark-dialog task-detail-dialog"
    @open="handleOpen"
  >
    <!-- 任务摘要 -->
    <div class="detail-summary" v-if="task">
      <div class="summary-row">
        <div class="summary-item">
          <span class="label">任务类型：</span>
          <el-tag size="small" :type="getTaskTypeTag(task.taskType)">{{ getTaskTypeLabel(task.taskType) }}</el-tag>
        </div>
        <div class="summary-item">
          <span class="label">权限组：</span>
          <span class="value">{{ task.groupName || '-' }}</span>
        </div>
        <div class="summary-item">
          <span class="label">状态：</span>
          <el-tag size="small" :type="getStatusType(task.status)">{{ getStatusLabel(task.status) }}</el-tag>
        </div>
      </div>
      <div class="summary-row">
        <div class="summary-item">
          <span class="label">总数：</span>
          <span class="value">{{ task.totalCount }}</span>
        </div>
        <div class="summary-item">
          <span class="label">成功：</span>
          <span class="value count-success">{{ task.successCount }}</span>
        </div>
        <div class="summary-item">
          <span class="label">失败：</span>
          <span class="value count-fail">{{ task.failCount }}</span>
        </div>
        <div class="summary-item">
          <span class="label">进度：</span>
          <el-progress 
            :percentage="getProgress(task)" 
            :status="getProgressStatus(task)" 
            :stroke-width="12"
            style="width: 150px"
          />
        </div>
      </div>
      <div class="summary-row">
        <div class="summary-item">
          <span class="label">创建时间：</span>
          <span class="value">{{ task.createTime ? formatDateTime(task.createTime) : '-' }}</span>
        </div>
        <div class="summary-item" v-if="task.startTime">
          <span class="label">开始时间：</span>
          <span class="value">{{ formatDateTime(task.startTime) }}</span>
        </div>
        <div class="summary-item" v-if="task.endTime">
          <span class="label">结束时间：</span>
          <span class="value">{{ formatDateTime(task.endTime) }}</span>
        </div>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="detail-toolbar">
      <el-radio-group v-model="filterStatus" size="small" @change="handleFilterChange">
        <el-radio-button :label="undefined">全部 ({{ allCount }})</el-radio-button>
        <el-radio-button :label="1">成功 ({{ successCount }})</el-radio-button>
        <el-radio-button :label="2">失败 ({{ failCount }})</el-radio-button>
        <el-radio-button :label="0">待执行 ({{ pendingCount }})</el-radio-button>
      </el-radio-group>
      <el-button 
        v-if="canRetry" 
        type="warning" 
        size="small" 
        @click="handleRetryAll"
      >
        <Icon icon="ep:refresh-right" class="mr-4px" />重试失败项
      </el-button>
    </div>

    <!-- 明细列表 -->
    <el-table 
      v-loading="loading" 
      :data="filteredDetailList" 
      size="small" 
      max-height="400"
      class="detail-table"
    >
      <el-table-column label="人员编号" prop="personCode" width="120" />
      <el-table-column label="人员姓名" prop="personName" width="100" />
      <el-table-column label="设备名称" prop="deviceName" width="150" show-overflow-tooltip />
      <el-table-column label="凭证类型" prop="credentialTypes" width="120">
        <template #default="{ row }">
          <span v-if="row.credentialTypes">{{ formatCredentialTypes(row.credentialTypes) }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="getDetailStatusType(row.status)">
            {{ getDetailStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="错误信息" prop="errorMessage" min-width="200">
        <template #default="{ row }">
          <span v-if="row.errorMessage" class="error-message">
            <Icon icon="ep:warning-filled" class="mr-4px" />
            {{ row.errorMessage }}
          </span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="执行时间" width="160">
        <template #default="{ row }">
          {{ row.executeTime ? formatDateTime(row.executeTime) : '-' }}
        </template>
      </el-table-column>
    </el-table>

    <!-- 底部操作 -->
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button v-if="canRetry" type="warning" @click="handleRetryAll">
          <Icon icon="ep:refresh-right" class="mr-4px" />重试失败项
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  AccessAuthTaskApi, 
  AccessAuthTaskStatusOptions,
  AccessAuthTaskTypeOptions,
  type AccessAuthTaskVO,
  type AccessAuthTaskDetailVO
} from '@/api/iot/access'
import { formatDateTime } from '@/utils/formatTime'

const props = defineProps<{
  visible: boolean
  task: AccessAuthTaskVO | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'retry', taskId: number): void
}>()

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const loading = ref(false)
const detailList = ref<AccessAuthTaskDetailVO[]>([])
const filterStatus = ref<number | undefined>(undefined)

// ========== 统计数据 ==========
const allCount = computed(() => detailList.value.length)
const successCount = computed(() => detailList.value.filter(d => d.status === 1).length)
const failCount = computed(() => detailList.value.filter(d => d.status === 2).length)
const pendingCount = computed(() => detailList.value.filter(d => d.status === 0).length)

const canRetry = computed(() => {
  if (!props.task) return false
  return props.task.status === 3 || props.task.status === 4 // 部分失败或全部失败
})

// ========== 筛选后的列表 ==========
const filteredDetailList = computed(() => {
  if (filterStatus.value === undefined) {
    return detailList.value
  }
  return detailList.value.filter(d => d.status === filterStatus.value)
})

// ========== 进度计算 ==========
const getProgress = (task: AccessAuthTaskVO) => {
  if (task.totalCount === 0) return 0
  return Math.round(((task.successCount + task.failCount) / task.totalCount) * 100)
}

const getProgressStatus = (task: AccessAuthTaskVO) => {
  if (task.status === 2) return 'success'
  if (task.status === 4) return 'exception'
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

const getDetailStatusType = (status: number) => {
  switch (status) {
    case 0: return 'info'
    case 1: return 'success'
    case 2: return 'danger'
    default: return 'info'
  }
}

const getDetailStatusLabel = (status: number) => {
  switch (status) {
    case 0: return '待执行'
    case 1: return '成功'
    case 2: return '失败'
    default: return '未知'
  }
}

const formatCredentialTypes = (types: string) => {
  if (!types) return '-'
  const typeMap: Record<string, string> = {
    'FACE': '人脸',
    'CARD': '卡片',
    'FINGERPRINT': '指纹',
    'PASSWORD': '密码'
  }
  return types.split(',').map(t => typeMap[t.trim()] || t).join('、')
}

// ========== 数据加载 ==========
const loadDetails = async () => {
  if (!props.task) return
  
  loading.value = true
  try {
    detailList.value = await AccessAuthTaskApi.getTaskDetails(props.task.id)
  } catch (error) {
    console.error('获取任务明细失败:', error)
    ElMessage.error('获取任务明细失败')
  } finally {
    loading.value = false
  }
}

const handleOpen = () => {
  filterStatus.value = undefined
  loadDetails()
}

const handleFilterChange = () => {
  // 筛选状态变化时不需要重新加载数据，computed 会自动处理
}

// ========== 重试操作 ==========
const handleRetryAll = async () => {
  if (!props.task) return
  
  try {
    await ElMessageBox.confirm(
      `确定要重试 ${failCount.value} 条失败的任务明细吗？`,
      '重试确认',
      {
        confirmButtonText: '确定重试',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    emit('retry', props.task.id)
    dialogVisible.value = false
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('重试失败:', error)
    }
  }
}

// 监听 task 变化，自动刷新数据
watch(() => props.task, (newTask) => {
  if (newTask && props.visible) {
    loadDetails()
  }
})
</script>

<style scoped lang="scss">
.task-detail-dialog {
  :deep(.el-dialog__body) {
    padding: 16px 20px;
  }
}

.detail-summary {
  background: #252532;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 16px;

  .summary-row {
    display: flex;
    flex-wrap: wrap;
    gap: 24px;
    margin-bottom: 12px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .summary-item {
    display: flex;
    align-items: center;
    font-size: 13px;

    .label {
      color: #8c8c9a;
      margin-right: 8px;
      white-space: nowrap;
    }

    .value {
      color: #e0e0e0;
    }

    .count-success {
      color: #67c23a;
      font-weight: 600;
    }

    .count-fail {
      color: #f56c6c;
      font-weight: 600;
    }
  }
}

.detail-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  :deep(.el-radio-group) {
    .el-radio-button__inner {
      background: #2d2d3a;
      border-color: #3d3d4a;
      color: #c4c4c4;
    }

    .el-radio-button__original-radio:checked + .el-radio-button__inner {
      background: #409eff;
      border-color: #409eff;
      color: #fff;
    }
  }
}

.detail-table {
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

  .error-message {
    color: #f56c6c;
    font-size: 12px;
    display: flex;
    align-items: center;
  }

  .text-muted {
    color: #6c6c7a;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
