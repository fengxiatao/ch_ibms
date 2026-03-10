<template>
  <div class="visitor-appointment">
    <div class="page-header">
      <h1 class="page-title">访客预约管理</h1>
    </div>

    <!-- 筛选区域 -->
    <el-card class="filter-card" shadow="never">
      <div class="filter-date-row">
        <label class="filter-label">日期筛选</label>
        <div class="filter-btns">
          <el-button :type="dateQuick === 'today' ? 'primary' : 'default'" size="small" @click="dateQuick = 'today'">
            今日
          </el-button>
          <el-button :type="dateQuick === 'week' ? 'primary' : 'default'" size="small" @click="dateQuick = 'week'">
            近一周
          </el-button>
          <el-button :type="dateQuick === 'month' ? 'primary' : 'default'" size="small" @click="dateQuick = 'month'">
            近一月
          </el-button>
        </div>
        <div class="filter-date-range">
          <el-date-picker
            v-model="queryParams.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            size="small"
            class="date-picker"
          />
        </div>
      </div>
      <div class="filter-grid">
        <div class="filter-item">
          <label class="filter-item__label">访客姓名</label>
          <el-input v-model="queryParams.name" placeholder="请输入访客姓名" clearable size="small" />
        </div>
        <div class="filter-item">
          <label class="filter-item__label">访客车辆</label>
          <el-input v-model="queryParams.carNo" placeholder="请输入车牌号" clearable size="small" />
        </div>
        <div class="filter-item">
          <label class="filter-item__label">来访事由</label>
          <el-select v-model="queryParams.reason" placeholder="请选择" clearable size="small" class="w-full">
            <el-option label="参观" value="visit" />
            <el-option label="拜访" value="meet" />
            <el-option label="面试开会" value="interview" />
            <el-option label="其他" value="other" />
          </el-select>
        </div>
        <div class="filter-item">
          <label class="filter-item__label">被访人姓名</label>
          <el-input v-model="queryParams.host" placeholder="请输入被访人姓名" clearable size="small" />
        </div>
        <div class="filter-item">
          <label class="filter-item__label">审批状态</label>
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable size="small" class="w-full">
            <el-option label="待审批" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </div>
      </div>
      <div class="filter-actions">
        <el-button type="primary" size="small" @click="handleQuery">
          <Icon icon="ep:search" class="mr-1" /> 搜索
        </el-button>
        <el-button size="small" @click="handleReset">
          <Icon icon="ep:refresh" class="mr-1" /> 重置
        </el-button>
        <el-button type="primary" size="small" class="ml-auto" @click="openAddModal">
          <Icon icon="ep:plus" class="mr-1" /> 新增预约
        </el-button>
      </div>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <h3 class="table-title">访客预约列表</h3>
        <span class="table-total">共 <strong>{{ total }}</strong> 条记录</span>
      </div>
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        size="default"
        class="visitor-table"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="访客姓名" min-width="100" />
        <el-table-column prop="phone" label="联系电话" width="120" />
        <el-table-column prop="carNo" label="预约车辆" width="120" />
        <el-table-column prop="host" label="被访人" width="100" />
        <el-table-column prop="reason" label="来访事由" width="100" />
        <el-table-column label="审批状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预计来访时间" width="160">
          <template #default="{ row }">
            {{ formatVisitTime(row.visitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="申请日期" width="110">
          <template #default="{ row }">
            {{ row.createTime ? row.createTime.slice(0, 10) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDetail(row)">详情</el-button>
            <template v-if="row.status === 'pending'">
              <el-button type="success" link size="small" @click="handleApprove(row, 'approve')">通过</el-button>
              <el-button type="danger" link size="small" @click="handleApprove(row, 'reject')">拒绝</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <span class="pagination-info">
          显示 <strong>{{ showRange }}</strong> 条，共 <strong>{{ total }}</strong> 条
        </span>
        <el-pagination
          v-model:current-page="queryParams.pageNo"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="sizes, prev, pager, next"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <AddVisitorModal v-model:visible="addModalVisible" @success="loadData" />
    <ApprovalDetailModal v-model:visible="detailVisible" :visitor="currentRow" @approve="loadData" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Icon } from '@iconify/vue'
import { NewVisitorManagementApi } from '@/api/iot/visitor/newVisitorManagement'
import AddVisitorModal from '../VisitorManagement/components/AddVisitorModal.vue'
import ApprovalDetailModal from '../VisitorManagement/components/ApprovalDetailModal.vue'

defineOptions({ name: 'VisitorAppointment' })

const dateQuick = ref<'today' | 'week' | 'month'>('week')
const loading = ref(false)
const total = ref(0)
const tableData = ref<any[]>([])
const addModalVisible = ref(false)
const detailVisible = ref(false)
const currentRow = ref<any>(null)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: '',
  carNo: '',
  reason: '',
  host: '',
  status: '',
  dateRange: null as [string, string] | null
})

const showRange = computed(() => {
  const start = (queryParams.pageNo - 1) * queryParams.pageSize + 1
  const end = Math.min(queryParams.pageNo * queryParams.pageSize, total.value)
  return total.value > 0 ? `${start}-${end}` : '0-0'
})

const formatVisitTime = (visitTime: string) => {
  if (!visitTime) return '-'
  const d = visitTime.slice(0, 19).replace('T', ' ')
  return d.slice(0, 16)
}

const getStatusTagType = (status: string) => {
  const map: Record<string, string> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    pending: '待审批',
    approved: '已通过',
    rejected: '已拒绝'
  }
  return map[status] || status
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await NewVisitorManagementApi.getAppointmentPage({
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      name: queryParams.name || undefined,
      status: queryParams.status || undefined
    })
    const list = res?.data?.list ?? []
    const totalCount = res?.data?.total ?? 0
    tableData.value = list.map((it: any) => ({
      ...it,
      time: it.visitTime ? String(it.visitTime).slice(11, 16) : '',
      hostDept: it.hostDept
    }))
    total.value = totalCount
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  loadData()
}

const handleReset = () => {
  queryParams.name = ''
  queryParams.carNo = ''
  queryParams.reason = ''
  queryParams.host = ''
  queryParams.status = ''
  queryParams.dateRange = null
  queryParams.pageNo = 1
  loadData()
}

const openAddModal = () => {
  addModalVisible.value = true
}

const openDetail = (row: any) => {
  currentRow.value = { ...row, hostDept: row.hostDept }
  detailVisible.value = true
}

const handleApprove = async (row: any, action: 'approve' | 'reject') => {
  const text = action === 'approve' ? '通过' : '拒绝'
  await ElMessageBox.confirm(`确认${text}该预约申请？`, '确认')
  await NewVisitorManagementApi.approveAppointment(
    row.id,
    action,
    action === 'approve' ? '同意预约' : '拒绝预约'
  )
  ElMessage.success(`已${text}`)
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.visitor-appointment {
  padding: 20px;
  background: var(--el-bg-color-page);
}

.page-header {
  margin-bottom: 20px;
  .page-title {
    font-size: 18px;
    font-weight: 600;
    margin: 0 0 4px;
    color: var(--el-text-color-primary);
  }
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
  :deep(.el-card__body) {
    padding: 20px;
  }
}

.filter-date-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.filter-label {
  font-size: 14px;
  font-weight: 500;
  min-width: 70px;
  color: var(--el-text-color-regular);
}

.filter-btns {
  display: flex;
  gap: 8px;
}

.filter-date-range {
  .date-picker {
    width: 260px;
  }
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.filter-item {
  &__label {
    display: block;
    font-size: 13px;
    color: var(--el-text-color-regular);
    margin-bottom: 6px;
  }
  .w-full {
    width: 100%;
  }
}

.filter-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  .ml-auto {
    margin-left: auto;
  }
}

.table-card {
  border-radius: 12px;
  overflow: hidden;
  :deep(.el-card__body) {
    padding: 0;
  }
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: var(--el-fill-color-light);
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.table-title {
  font-size: 15px;
  font-weight: 500;
  margin: 0;
  color: var(--el-text-color-primary);
}

.table-total {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.visitor-table {
  width: 100%;
}

.pagination-wrap {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.pagination-info {
  font-size: 13px;
  color: var(--el-text-color-regular);
}
</style>
