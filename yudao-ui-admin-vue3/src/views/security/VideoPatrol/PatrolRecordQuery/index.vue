<template>
  <div class="dark-theme-page">
    <!-- 搜索工具栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
        <el-form-item label="AI状态" prop="aiStatus">
          <el-select v-model="queryParams.aiStatus" placeholder="请选择" clearable style="width: 150px">
            <el-option label="正常" :value="1" />
            <el-option label="异常" :value="2" />
            <el-option label="无法判断" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否异常" prop="isAbnormal">
          <el-select v-model="queryParams.isAbnormal" placeholder="请选择" clearable style="width: 150px">
            <el-option label="正常" :value="false" />
            <el-option label="异常" :value="true" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态" prop="handled">
          <el-select v-model="queryParams.handled" placeholder="请选择" clearable style="width: 150px">
            <el-option label="未处理" :value="false" />
            <el-option label="已处理" :value="true" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :icon="Search">查询</el-button>
          <el-button @click="resetQuery" :icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="dataList"
        row-key="id"
        style="width: 100%"
        :header-cell-style="{ background: '#1a1a1a', color: 'rgba(255, 255, 255, 0.9)' }"
      >
        <el-table-column label="点位名称" prop="pointName" min-width="150" />
        <el-table-column label="摄像头" prop="cameraName" width="150" />
        <el-table-column label="巡更时间" prop="patrolTime" width="160" />
        <el-table-column label="AI判断" prop="aiStatus" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.aiStatus === 1" type="success">正常</el-tag>
            <el-tag v-else-if="row.aiStatus === 2" type="danger">异常</el-tag>
            <el-tag v-else-if="row.aiStatus === 3" type="info">无法判断</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="AI评分" prop="aiScore" width="80" />
        <el-table-column label="人工确认" prop="manualConfirmed" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.manualConfirmed" type="success">已确认</el-tag>
            <el-tag v-else type="info">未确认</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否异常" prop="isAbnormal" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isAbnormal" type="danger">异常</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理状态" prop="handled" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.handled" type="success">已处理</el-tag>
            <el-tag v-else type="warning">未处理</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)" :icon="View">查看</el-button>
            <el-button 
              v-if="!row.manualConfirmed" 
              link 
              type="success" 
              @click="handleConfirm(row)"
            >
              确认
            </el-button>
            <el-button 
              v-if="!row.handled && row.isAbnormal" 
              link 
              type="warning" 
              @click="handleMark(row)"
            >
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog
      title="巡更记录详情"
      v-model="detailVisible"
      width="800px"
      append-to-body
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="点位名称">{{ currentRecord?.pointName }}</el-descriptions-item>
        <el-descriptions-item label="摄像头">{{ currentRecord?.cameraName }}</el-descriptions-item>
        <el-descriptions-item label="巡更时间">{{ currentRecord?.patrolTime }}</el-descriptions-item>
        <el-descriptions-item label="AI判断状态">
          <el-tag v-if="currentRecord?.aiStatus === 1" type="success">正常</el-tag>
          <el-tag v-else-if="currentRecord?.aiStatus === 2" type="danger">异常</el-tag>
          <el-tag v-else type="info">无法判断</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="AI评分">{{ currentRecord?.aiScore }}</el-descriptions-item>
        <el-descriptions-item label="人工确认状态">
          <el-tag v-if="currentRecord?.manualConfirmed" type="success">已确认</el-tag>
          <el-tag v-else type="info">未确认</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 快照图片 -->
      <div v-if="currentRecord?.snapshotUrl" class="snapshot-container">
        <h4>巡更快照</h4>
        <el-image 
          :src="currentRecord.snapshotUrl" 
          fit="contain" 
          style="width: 100%; max-height: 400px"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="VideoPatrolRecordQuery">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, View } from '@element-plus/icons-vue'
import {
  getVideoPatrolRecordPage,
  manualConfirm,
  markAsHandled
} from '@/api/iot/videoPatrol'
import type { VideoPatrolRecordPageReqVO } from '@/api/iot/videoPatrol'

// 数据列表
const dataList = ref([])
const total = ref(0)
const loading = ref(false)

// 查询参数
const queryParams = reactive<VideoPatrolRecordPageReqVO>({
  pageNo: 1,
  pageSize: 20,
  aiStatus: undefined,
  isAbnormal: undefined,
  handled: undefined
})

// 详情对话框
const detailVisible = ref(false)
const currentRecord = ref<any>(null)

// 查询列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getVideoPatrolRecordPage(queryParams)
    dataList.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('[视频巡更记录] 加载失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.aiStatus = undefined
  queryParams.isAbnormal = undefined
  queryParams.handled = undefined
  handleQuery()
}

// 查看详情
const handleView = (row: any) => {
  currentRecord.value = row
  detailVisible.value = true
}

// 人工确认
const handleConfirm = async (row: any) => {
  try {
    await ElMessageBox.prompt('请输入确认意见', '人工确认', {
      confirmButtonText: '确认正常',
      cancelButtonText: '确认异常',
      inputPlaceholder: '请输入备注',
      distinguishCancelAndClose: true
    })
    await manualConfirm(row.id, 1, '')
    ElMessage.success('确认成功')
    getList()
  } catch (action: any) {
    if (action === 'cancel') {
      await manualConfirm(row.id, 2, '')
      ElMessage.success('确认成功')
      getList()
    }
  }
}

// 标记已处理
const handleMark = async (row: any) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入处理意见', '标记已处理', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入处理备注'
    })
    await markAsHandled(row.id, value)
    ElMessage.success('标记成功')
    getList()
  } catch (error) {
    console.error('[视频巡更记录] 标记失败:', error)
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.dark-theme-page {
  padding: 16px;
  background: #0a0a0a;
  min-height: calc(100vh - 84px);

  .search-card,
  .table-card {
    background: #1a1a1a;
    border: 1px solid #2d2d2d;
    margin-bottom: 16px;

    :deep(.el-card__body) {
      padding: 16px;
    }
  }

  :deep(.el-form-item__label) {
    color: rgba(255, 255, 255, 0.85);
  }

  :deep(.el-input__inner),
  :deep(.el-select .el-input__inner) {
    background: #2d2d2d;
    border-color: #404040;
    color: rgba(255, 255, 255, 0.85);
  }

  :deep(.el-table) {
    background-color: #1a1a1a !important;
    color: #ffffff;

    .el-table__header {
      .el-table__cell {
        background-color: #2a2a2a !important;
        border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
        color: #ffffff !important;
        font-weight: 600 !important;
      }
    }

    .el-table__body {
      .el-table__row {
        background-color: #1a1a1a !important;

        &:hover {
          background-color: #2a2a2a !important;
        }

        .el-table__cell {
          background-color: transparent !important;
          border-bottom: 1px solid rgba(255, 255, 255, 0.05) !important;
          color: #ffffff !important;
        }

        &.el-table__row--striped {
          background-color: #222222 !important;

          .el-table__cell {
            background-color: transparent !important;
          }

          &:hover {
            background-color: #2a2a2a !important;
          }
        }
      }
    }

    // 确保所有单元格文字都是白色
    td {
      color: #ffffff !important;
    }
  }

  .snapshot-container {
    margin-top: 20px;

    h4 {
      color: rgba(255, 255, 255, 0.9);
      margin-bottom: 10px;
    }
  }
}
</style>






