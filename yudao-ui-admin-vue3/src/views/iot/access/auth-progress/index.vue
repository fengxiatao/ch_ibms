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
    <div class="auth-progress-page">
      <!-- 顶部工具栏 -->
      <div class="page-header">
        <div class="header-left">
          <el-input
            v-model="queryParams.personName"
            placeholder="人员姓名"
            clearable
            size="small"
            style="width: 140px"
            @keyup.enter="handleQuery"
          />
          <el-input
            v-model="queryParams.deviceName"
            placeholder="设备名称"
            clearable
            size="small"
            style="width: 140px"
            @keyup.enter="handleQuery"
          />
          <el-select
            v-model="queryParams.authStatus"
            placeholder="授权状态"
            clearable
            size="small"
            style="width: 130px"
            @change="handleQuery"
          >
            <el-option
              v-for="item in AuthStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-button size="small" type="primary" @click="handleQuery">
            <Icon icon="ep:search" class="mr-4px" />搜索
          </el-button>
          <el-button size="small" @click="handleReset">
            <Icon icon="ep:refresh" class="mr-4px" />重置
          </el-button>
        </div>
        <div class="header-right">
          <span class="total-info">共 {{ total }} 条记录</span>
        </div>
      </div>

      <!-- 数据表格 -->
      <div class="page-content">
        <el-table v-loading="loading" :data="list" size="small" height="100%">
          <el-table-column label="人员姓名" prop="personName" width="120" show-overflow-tooltip />
          <el-table-column label="人员编号" prop="personCode" width="120" show-overflow-tooltip />
          <el-table-column label="设备名称" prop="deviceName" width="180" show-overflow-tooltip />
          <el-table-column label="通道名称" prop="channelName" width="140" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.channelName || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="授权状态" width="110" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="getStatusType(row.authStatus)">
                {{ row.authStatusName }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="结果信息" prop="result" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <span :class="{ 'text-danger': row.authStatus === 3 }">
                {{ row.result || '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="最后下发时间" width="170">
            <template #default="{ row }">
              {{ row.lastDispatchTime ? formatDate(row.lastDispatchTime, 'YYYY-MM-DD HH:mm:ss') : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="更新时间" width="170">
            <template #default="{ row }">
              {{ row.updateTime ? formatDate(row.updateTime, 'YYYY-MM-DD HH:mm:ss') : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right" align="center">
            <template #default="{ row }">
              <el-button
                v-if="row.authStatus === 3"
                link
                type="warning"
                size="small"
                :loading="retryingId === row.id"
                @click="handleRetry(row)"
              >
                <Icon icon="ep:refresh-right" class="mr-2px" />重试
              </el-button>
              <span v-else class="text-gray-400">-</span>
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
          layout="total, sizes, prev, pager, next, jumper"
          small
          @size-change="handleQuery"
          @current-change="getList"
        />
      </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  AccessManagementApi,
  AuthStatusOptions,
  type PersonDeviceAuthVO,
  type AuthRecordPageReqVO
} from '@/api/iot/access'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'AccessAuthProgress' })

const loading = ref(false)
const list = ref<PersonDeviceAuthVO[]>([])
const total = ref(0)
const retryingId = ref<number | null>(null)

const queryParams = reactive<AuthRecordPageReqVO>({
  pageNo: 1,
  pageSize: 20,
  personName: undefined,
  deviceName: undefined,
  authStatus: undefined
})


// ========== 状态显示 ==========
const getStatusType = (status: number) => {
  const item = AuthStatusOptions.find((i) => i.value === status)
  return item?.type || 'info'
}

// ========== 数据获取 ==========
const getList = async () => {
  loading.value = true
  try {
    const res = await AccessManagementApi.getAuthRecordPage(queryParams)
    list.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('获取授权记录失败:', error)
    ElMessage.error('获取授权记录失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const handleReset = () => {
  queryParams.pageNo = 1
  queryParams.personName = undefined
  queryParams.deviceName = undefined
  queryParams.authStatus = undefined
  getList()
}

// ========== 重试操作 (Requirements: 7.3) ==========
const handleRetry = async (row: PersonDeviceAuthVO) => {
  try {
    await ElMessageBox.confirm(
      `确定要重试授权 "${row.personName}" 到 "${row.deviceName}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    retryingId.value = row.id
    await AccessManagementApi.retryFailedAuth(row.id)
    ElMessage.success('重试请求已提交')
    // 刷新列表
    await getList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('重试失败:', error)
      ElMessage.error('重试失败: ' + (error.message || '未知错误'))
    }
  } finally {
    retryingId.value = null
  }
}

// ========== 生命周期 ==========
onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.auth-progress-page {
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

    &.el-button--primary {
      background: #409eff;
      border-color: #409eff;
      color: #fff;

      &:hover {
        background: #66b1ff;
        border-color: #66b1ff;
      }
    }
  }

  :deep(.el-input__wrapper) {
    background: #2d2d3a;
    box-shadow: none;
    border: 1px solid #3d3d4a;
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

  .text-danger {
    color: #f56c6c;
  }

  .text-gray-400 {
    color: #6c6c7a;
  }
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
