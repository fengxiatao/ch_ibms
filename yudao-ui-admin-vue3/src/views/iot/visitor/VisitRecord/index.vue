<template>
  <div class="visitor-visit-record">
    <div class="page-header">
      <h1 class="page-title">访客统计与记录管理</h1>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-card__icon stat-card__icon--primary">
          <Icon icon="ep:user" />
        </div>
        <h3 class="stat-card__label">当前在园访人数</h3>
        <p class="stat-card__value">{{ stats.currentVisitors }}</p>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-card__icon stat-card__icon--success">
          <Icon icon="ep:circle-check" />
        </div>
        <h3 class="stat-card__label">今日访客签到人数</h3>
        <p class="stat-card__value">{{ stats.todaySignIn }}</p>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-card__icon stat-card__icon--info">
          <Icon icon="ep:clock" />
        </div>
        <h3 class="stat-card__label">历史访客总数</h3>
        <p class="stat-card__value">{{ stats.totalHistory }}</p>
      </el-card>
    </div>

    <!-- 筛选 -->
    <el-card class="filter-card" shadow="never">
      <div class="filter-date-row">
        <label class="filter-label">日期筛选</label>
        <div class="filter-btns">
          <el-button :type="dateQuick === 'today' ? 'default' : 'primary'" size="small" @click="dateQuick = 'today'">
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
          <el-input v-model="queryParams.carNo" placeholder="请输入预约车牌号" clearable size="small" />
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
          <label class="filter-item__label">来访状态</label>
          <el-select v-model="queryParams.visitStatus" placeholder="请选择" clearable size="small" class="w-full">
            <el-option label="待访" value="pending" />
            <el-option label="在访" value="visiting" />
            <el-option label="离访" value="visited" />
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
      </div>
    </el-card>

    <!-- 访客记录列表 -->
    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <h3 class="table-title">访客记录</h3>
        <span class="table-total">共 <strong>{{ total }}</strong> 条记录</span>
      </div>
      <el-table v-loading="loading" :data="tableData" stripe size="default" class="visitor-table">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="访客姓名" width="100" />
        <el-table-column prop="phone" label="访客联系电话" width="120" />
        <el-table-column prop="carNo" label="预约车辆" width="120" />
        <el-table-column prop="host" label="被访人姓名" width="100" />
        <el-table-column prop="reason" label="来访事由" width="100" />
        <el-table-column label="来访状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getVisitStatusType(row)" size="small">{{ getVisitStatusLabel(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="签到时间" width="140">
          <template #default="{ row }">
            {{ row.signInTime ? formatTime(row.signInTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="签离时间" width="140">
          <template #default="{ row }">
            {{ row.signOutTime ? formatTime(row.signOutTime) : '未签离' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDetail(row)">详情</el-button>
            <span class="op-divider">|</span>
            <el-button type="primary" link size="small" @click="openDoorRecord(row)">开门记录</el-button>
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

    <VisitorDetailDrawer v-model:visible="detailVisible" :visitor="currentRow" />
    <DoorRecordModal v-model:visible="doorRecordVisible" :visitor-id="currentRow?.id" :visitor-name="currentRow?.name" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { NewVisitorManagementApi } from '@/api/iot/visitor/newVisitorManagement'
import VisitorDetailDrawer from '../VisitorManagement/components/VisitorDetailDrawer.vue'
import DoorRecordModal from '../VisitorManagement/components/DoorRecordModal.vue'

defineOptions({ name: 'VisitorVisitRecord' })

const dateQuick = ref<'today' | 'week' | 'month'>('week')
const loading = ref(false)
const total = ref(0)
const tableData = ref<any[]>([])
const detailVisible = ref(false)
const doorRecordVisible = ref(false)
const currentRow = ref<any>(null)

const stats = ref({
  currentVisitors: 0,
  todaySignIn: 0,
  totalHistory: 0
})

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: '',
  carNo: '',
  reason: '',
  host: '',
  visitStatus: '',
  dateRange: null as [string, string] | null
})

const showRange = computed(() => {
  const start = (queryParams.pageNo - 1) * queryParams.pageSize + 1
  const end = Math.min(queryParams.pageNo * queryParams.pageSize, total.value)
  return total.value > 0 ? `${start}-${end}` : '0-0'
})

const formatTime = (t: string | number | Date | null | undefined) => {
  if (t === null || t === undefined || t === '') return '-'
  const date = t instanceof Date
    ? t
    : typeof t === 'number'
      ? new Date(t)
      : new Date(String(t))
  if (Number.isNaN(date.getTime())) return '-'
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const getVisitStatusType = (row: any) => {
  if (row.signOutTime) return 'info'
  if (row.signInTime) return 'success'
  return 'warning'
}

const getVisitStatusLabel = (row: any) => {
  if (row.signOutTime) return '离访'
  if (row.signInTime) return '在访'
  return '待访'
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await NewVisitorManagementApi.getHistoryPage({
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize
    })
    const list = res?.list ?? res?.data?.list ?? []
    const totalCount = res?.total ?? res?.data?.total ?? 0
    tableData.value = list.map((it: any) => ({ ...it }))
    total.value = totalCount
  } finally {
    loading.value = false
  }
}

const loadStats = () => {
  NewVisitorManagementApi.getStats()
    .then((res: any) => {
      const d = res
      if (d) {
        stats.value = {
          currentVisitors: d.currentVisitors ?? 0,
          todaySignIn: d.todayAppointments ?? 0,
          totalHistory: d.monthlyTotal ?? 0
        }
      }
    })
    .catch(() => {})
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
  queryParams.visitStatus = ''
  queryParams.dateRange = null
  queryParams.pageNo = 1
  loadData()
}

const openDetail = (row: any) => {
  currentRow.value = { ...row, status: row.signOutTime ? 'completed' : row.signInTime ? 'in' : 'pending', time: row.signInTime ? formatTime(row.signInTime).slice(11, 19) : '' }
  detailVisible.value = true
}

const openDoorRecord = (row: any) => {
  currentRow.value = row
  doorRecordVisible.value = true
}

onMounted(() => {
  loadData()
  loadStats()
})
</script>

<style lang="scss" scoped>
.visitor-visit-record {
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

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  text-align: center;
  :deep(.el-card__body) {
    padding: 24px;
  }
  &__icon {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 12px;
    font-size: 22px;
    &--primary {
      background: rgba(var(--el-color-primary-rgb), 0.1);
      color: var(--el-color-primary);
    }
    &--success {
      background: rgba(var(--el-color-success-rgb), 0.1);
      color: var(--el-color-success);
    }
    &--info {
      background: rgba(var(--el-color-info-rgb), 0.1);
      color: var(--el-color-info);
    }
  }
  &__label {
    font-size: 13px;
    color: var(--el-text-color-secondary);
    margin: 0 0 8px;
    font-weight: normal;
  }
  &__value {
    font-size: 28px;
    font-weight: 600;
    margin: 0;
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

.filter-date-range .date-picker {
  width: 260px;
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
  gap: 8px;
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

.op-divider {
  color: var(--el-border-color);
  margin: 0 4px;
  font-size: 12px;
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
