<template>
  <div class="auth-progress">
    <div class="progress-header">
      <div class="header-left">
        <el-select v-model="queryParams.status" placeholder="状态筛选" clearable size="small" style="width: 120px" @change="handleQuery">
          <el-option v-for="item in AccessAuthTaskStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button size="small" @click="handleQuery"><Icon icon="ep:refresh" class="mr-4px" />刷新</el-button>
      </div>
    </div>

    <div class="progress-table">
      <el-table v-loading="loading" :data="list" size="small" height="100%">
        <el-table-column label="权限组" prop="groupName" width="150" />
        <el-table-column label="设备名称" prop="deviceName" width="150" />
        <el-table-column label="下发进度" width="200">
          <template #default="{ row }">
            <el-progress :percentage="getProgress(row)" :status="getProgressStatus(row)" :stroke-width="8" />
          </template>
        </el-table-column>
        <el-table-column label="成功/失败/总数" width="130">
          <template #default="{ row }">
            <span class="text-success">{{ row.successCount }}</span> / 
            <span class="text-danger">{{ row.failCount }}</span> / 
            {{ row.totalCount }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="160">
          <template #default="{ row }">{{ formatDate(row.startTime, 'YYYY-MM-DD HH:mm:ss') }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 3 || row.status === 4" link type="warning" size="small" @click="handleRetry(row)">重试</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="progress-pagination">
      <el-pagination
        v-model:current-page="queryParams.pageNo"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        small
        @current-change="getList"
      />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="任务详情" width="700px" append-to-body class="dark-dialog">
      <el-table :data="detailList" size="small" max-height="400">
        <el-table-column label="人员姓名" prop="personName" width="100" />
        <el-table-column label="设备名称" prop="deviceName" width="150" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="错误信息" prop="errorMessage" show-overflow-tooltip />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { AccessAuthTaskApi, AccessAuthTaskStatusOptions, type AccessAuthTaskVO, type AccessAuthTaskDetailVO } from '@/api/iot/access'
import { formatDate } from '@/utils/formatTime'

const loading = ref(false)
const list = ref<AccessAuthTaskVO[]>([])
const total = ref(0)
const detailDialogVisible = ref(false)
const detailList = ref<AccessAuthTaskDetailVO[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  status: undefined as number | undefined
})

const getProgress = (row: AccessAuthTaskVO) => {
  if (row.totalCount === 0) return 0
  return Math.round(((row.successCount + row.failCount) / row.totalCount) * 100)
}

const getProgressStatus = (row: AccessAuthTaskVO) => {
  if (row.status === 2) return 'success'
  if (row.status === 4) return 'exception'
  return undefined
}

const getStatusType = (status: number) => {
  const item = AccessAuthTaskStatusOptions.find(i => i.value === status)
  return item?.type || 'info'
}

const getStatusLabel = (status: number) => {
  const item = AccessAuthTaskStatusOptions.find(i => i.value === status)
  return item?.label || '未知'
}

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

const handleViewDetail = async (row: AccessAuthTaskVO) => {
  try {
    detailList.value = await AccessAuthTaskApi.getTaskDetail(row.id)
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取任务详情失败:', error)
  }
}

const handleRetry = async (row: AccessAuthTaskVO) => {
  try {
    await AccessAuthTaskApi.retryTask(row.id)
    ElMessage.success('重试任务已提交')
    getList()
  } catch (error) {
    console.error('重试失败:', error)
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.auth-progress {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #1e1e2d;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #252532;
  border-bottom: 1px solid #2d2d3a;

  .header-left {
    display: flex;
    gap: 8px;
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

.progress-table {
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

  .text-success { color: #67c23a; }
  .text-danger { color: #f56c6c; }
}

.progress-pagination {
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
