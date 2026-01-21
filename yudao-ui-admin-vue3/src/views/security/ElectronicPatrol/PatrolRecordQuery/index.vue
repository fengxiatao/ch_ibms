<template>
  <div class="dark-theme-page">
    <!-- 搜索工具栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
        <el-form-item label="签到类型" prop="checkinType">
          <el-select v-model="queryParams.checkinType" placeholder="请选择" clearable style="width: 150px">
            <el-option label="正常" :value="1" />
            <el-option label="提前" :value="2" />
            <el-option label="延迟" :value="3" />
            <el-option label="漏巡" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否异常" prop="isAbnormal">
          <el-select v-model="queryParams.isAbnormal" placeholder="请选择" clearable style="width: 150px">
            <el-option label="正常" :value="false" />
            <el-option label="异常" :value="true" />
          </el-select>
        </el-form-item>
        <el-form-item label="巡更时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 380px"
          />
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
        <el-table-column label="巡更人员" prop="userName" width="120" />
        <el-table-column label="计划时间" prop="scheduledTime" width="160" />
        <el-table-column label="实际时间" prop="actualTime" width="160" />
        <el-table-column label="时间差" prop="timeDiffMinutes" width="80">
          <template #default="{ row }">
            <span :class="{ 'text-success': row.timeDiffMinutes < 0, 'text-danger': row.timeDiffMinutes > 0 }">
              {{ row.timeDiffMinutes > 0 ? '+' : '' }}{{ row.timeDiffMinutes }}分钟
            </span>
          </template>
        </el-table-column>
        <el-table-column label="签到类型" prop="checkinType" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.checkinType === 1" type="success">正常</el-tag>
            <el-tag v-else-if="row.checkinType === 2" type="warning">提前</el-tag>
            <el-tag v-else-if="row.checkinType === 3" type="warning">延迟</el-tag>
            <el-tag v-else-if="row.checkinType === 4" type="danger">漏巡</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否异常" prop="isAbnormal" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isAbnormal" type="danger">异常</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="150" show-overflow-tooltip />
      </el-table>

      <Pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts" name="PatrolRecordQuery">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getPatrolRecordPage } from '@/api/iot/patrol'
import type { PatrolRecordPageReqVO } from '@/api/iot/patrol'

// 数据列表
const dataList = ref([])
const total = ref(0)
const loading = ref(false)
const dateRange = ref([])

// 查询参数
const queryParams = reactive<PatrolRecordPageReqVO>({
  pageNo: 1,
  pageSize: 20,
  checkinType: undefined,
  isAbnormal: undefined,
  startTime: undefined,
  endTime: undefined
})

// 查询列表
const getList = async () => {
  loading.value = true
  try {
    if (dateRange.value && dateRange.value.length === 2) {
      queryParams.startTime = dateRange.value[0]
      queryParams.endTime = dateRange.value[1]
    } else {
      queryParams.startTime = undefined
      queryParams.endTime = undefined
    }
    const res = await getPatrolRecordPage(queryParams)
    dataList.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('[巡更记录] 加载失败:', error)
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
  queryParams.checkinType = undefined
  queryParams.isAbnormal = undefined
  dateRange.value = []
  handleQuery()
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
    background: #1a1a1a;
    color: rgba(255, 255, 255, 0.85);

    tr {
      background: #1a1a1a;
    }

    .el-table__body tr:hover > td {
      background: #2d2d2d !important;
    }
  }

  .text-success {
    color: #67c23a;
  }

  .text-danger {
    color: #f56c6c;
  }
}
</style>






