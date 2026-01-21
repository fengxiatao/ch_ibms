<template>
<ContentWrap
    :body-style="{
      padding: '10px',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      minHeight: '0',
      backgroundColor: 'var(--el-bg-color)'
    }"
    style="height: calc(100vh - var(--page-top-gap, 70px)); padding-top: var(--page-top-gap, 70px)"
  >
  <div class="access-operation-log-container">
    <ContentWrap
      :body-style="{ padding: '0', height: '100%', display: 'flex', flexDirection: 'column', minHeight: '0' }"
    >
      <!-- 搜索栏 -->
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
        <el-form-item label="操作类型" prop="operationType">
          <el-select v-model="queryParams.operationType" placeholder="请选择操作类型" clearable style="width: 150px">
            <el-option v-for="item in AccessOperationTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作结果" prop="result">
          <el-select v-model="queryParams.result" placeholder="请选择结果" clearable style="width: 120px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery"><Icon icon="ep:search" class="mr-5px" />搜索</el-button>
          <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" />重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 数据表格 -->
      <div class="table-scroll">
        <el-table v-loading="loading" :data="list" stripe>
          <el-table-column label="操作时间" prop="operationTime" width="180" :formatter="formatDateTime" />
          <el-table-column label="操作类型" prop="operationType" width="140">
            <template #default="{ row }">{{ row.operationTypeName || getOperationTypeLabel(row.operationType) }}</template>
          </el-table-column>
          <el-table-column label="操作人" prop="operatorName" width="100" />
          <el-table-column label="操作对象" width="200">
            <template #default="{ row }">
              <span v-if="row.deviceName && row.channelName">{{ row.deviceName }}：{{ row.channelName }}</span>
              <span v-else-if="row.deviceName">{{ row.deviceName }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作内容" prop="resultDesc" show-overflow-tooltip />
          <el-table-column label="结果" prop="result" width="80">
            <template #default="{ row }">
              <el-tag :type="row.result === 1 ? 'success' : 'danger'">
                {{ row.result === 1 ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="错误信息" prop="errorMessage" width="200" show-overflow-tooltip />
        </el-table>
      </div>
      
      <!-- 分页 -->
      <Pagination v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </ContentWrap>
  </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { AccessOperationLogApi, AccessOperationTypeOptions, type AccessOperationLogVO } from '@/api/iot/access'
import { ContentWrap } from '@/components/ContentWrap'
import Pagination from '@/components/Pagination/index.vue'
import { formatDateTime as formatDateTimeUtil } from '@/utils/formatTime'

defineOptions({ name: 'AccessOperationLog' })

const loading = ref(false)
const list = ref<AccessOperationLogVO[]>([])
const total = ref(0)
const dateRange = ref<string[]>([])
const queryFormRef = ref()

// 格式化日期时间
const formatDateTime = (_row: any, _column: any, cellValue: any) => {
  if (!cellValue) return '-'
  return formatDateTimeUtil(cellValue, 'yyyy-MM-dd HH:mm:ss')
}

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  operationType: undefined as string | undefined,
  result: undefined as number | undefined,
  startTime: undefined as string | undefined,
  endTime: undefined as string | undefined
})

watch(dateRange, (val) => {
  queryParams.startTime = val?.[0]
  queryParams.endTime = val?.[1]
})

const getOperationTypeLabel = (type: string) => {
  const item = AccessOperationTypeOptions.find(i => i.value === type)
  return item?.label || type
}

const getList = async () => {
  loading.value = true
  try {
    const res = await AccessOperationLogApi.getLogPage(queryParams)
    list.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('获取操作日志失败:', error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  dateRange.value = []
  handleQuery()
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.access-operation-log-container { 
  padding: 10px;
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.table-scroll {
  flex: 1;
  min-height: 0;
  overflow: auto;
}
</style>
