<template>
  <div class="visitor-manage">
    <div class="page-header">
      <h1 class="page-title">访客实时管理</h1>
    </div>

    <!-- 提醒事项 -->
    <div class="alerts-section">
      <div class="alerts-head">
        <h3 class="alerts-title">提醒事项</h3>
        <div class="alerts-actions">
          <el-tag type="danger" size="small">{{ alertList.length }}条提醒</el-tag>
          <el-button size="small" @click="loadAlerts">
            <Icon icon="ep:refresh" class="mr-1" /> 刷新
          </el-button>
        </div>
      </div>
      <div class="alerts-scroll">
        <div class="alerts-list">
          <div
            v-for="item in alertList"
            :key="item.id"
            class="alert-card"
            :class="`alert-card--${item.type}`"
          >
            <div class="alert-card__inner">
              <Icon :icon="item.icon" class="alert-card__icon" />
              <div class="alert-card__body">
                <h4 class="alert-card__title">{{ item.title }}</h4>
                <p class="alert-card__desc">{{ item.desc }}</p>
                <p class="alert-card__time">{{ item.time }}</p>
                <div class="alert-card__btns">
                  <template v-if="item.type === 'unauthorized'">
                    <el-button size="small" type="primary" link>电话通知</el-button>
                    <el-button size="small" link>忽略</el-button>
                  </template>
                  <template v-else-if="item.type === 'overtime_out'">
                    <el-tag size="small" type="info">未签离</el-tag>
                    <el-button size="small" type="warning" link>延长访问</el-button>
                    <el-button size="small" type="danger" link @click="handleSignOut(item)">一键签离</el-button>
                    <el-button size="small" link>忽略</el-button>
                  </template>
                  <template v-else>
                    <el-tag size="small" type="info">未签到</el-tag>
                    <el-button size="small" type="primary" link>电话通知</el-button>
                    <el-button size="small" link>忽略</el-button>
                  </template>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选 -->
    <el-card class="filter-card" shadow="never">
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

    <!-- 访客列表 -->
    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <h3 class="table-title">访客列表</h3>
        <span class="table-total">共 <strong>{{ total }}</strong> 条记录</span>
      </div>
      <el-table v-loading="loading" :data="tableData" stripe size="default" class="visitor-table">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="访客姓名" width="100" />
        <el-table-column prop="phone" label="联系电话" width="120" />
        <el-table-column prop="carNo" label="预约车辆" width="120" />
        <el-table-column prop="host" label="被访人姓名" width="100" />
        <el-table-column prop="reason" label="来访事由" width="100" />
        <el-table-column label="来访状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getVisitStatusType(row)" size="small">{{ getVisitStatusLabel(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="权限下发状态" width="110">
          <template #default="{ row }">
            {{ row.authStatus ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="签到时间" width="140">
          <template #default="{ row }">
            {{ row.signInTime ? formatTime(row.signInTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="签离时间" width="140">
          <template #default="{ row }">
            {{ row.signOutTime ? formatTime(row.signOutTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDetail(row)">详情</el-button>
            <template v-if="!row.signInTime">
              <el-button type="primary" link size="small">签到</el-button>
              <el-button type="primary" link size="small" @click="openAuthDispatch(row)">下发权限</el-button>
            </template>
            <template v-else-if="!row.signOutTime">
              <el-button type="warning" link size="small" @click="handleSignOut(row)">签离</el-button>
              <el-button type="primary" link size="small" @click="openAuthDispatch(row)">修改权限</el-button>
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

    <VisitorDetailDrawer v-model:visible="detailVisible" :visitor="currentRow" @sign-out="handleSignOut" />
    <AuthDispatchModal v-model:visible="authDispatchVisible" :visitor="currentRow" @success="loadData" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Icon } from '@iconify/vue'
import { NewVisitorManagementApi } from '@/api/iot/visitor/newVisitorManagement'
import VisitorDetailDrawer from '../VisitorManagement/components/VisitorDetailDrawer.vue'
import AuthDispatchModal from '../VisitorManagement/components/AuthDispatchModal.vue'

defineOptions({ name: 'VisitorManage' })

const loading = ref(false)
const total = ref(0)
const tableData = ref<any[]>([])
const alertList = ref<any[]>([])
const detailVisible = ref(false)
const authDispatchVisible = ref(false)
const currentRow = ref<any>(null)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: '',
  carNo: '',
  reason: '',
  host: '',
  visitStatus: ''
})

const showRange = computed(() => {
  const start = (queryParams.pageNo - 1) * queryParams.pageSize + 1
  const end = Math.min(queryParams.pageNo * queryParams.pageSize, total.value)
  return total.value > 0 ? `${start}-${end}` : '0-0'
})

const formatTime = (t: string) => {
  if (!t) return '-'
  return t.slice(0, 19).replace('T', ' ')
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

const loadAlerts = () => {
  // 提醒可从异常/今日在访接口组装
  NewVisitorManagementApi.getAbnormalPage({ pageNo: 1, pageSize: 10 })
    .then((res: any) => {
      const list = res?.data?.list ?? []
      alertList.value = list.slice(0, 5).map((it: any, i: number) => ({
        id: it.id || i,
        type: it.abnormalType === 'unauthorized' ? 'unauthorized' : it.abnormalType === 'overtime' ? 'overtime_out' : 'overtime_in',
        title: it.abnormalType === 'unauthorized' ? '访客非法闯入' : it.abnormalType === 'overtime' ? '访客超时未签离' : '访客超时未签到',
        desc: `${it.visitorName || ''}（${it.visitorPhone || ''}）${it.details || ''}`,
        time: it.eventTime ? it.eventTime.slice(0, 19).replace('T', ' ') : '',
        icon: it.abnormalType === 'unauthorized' ? 'ep:warning-filled' : 'ep:clock',
        appointmentId: it.appointmentId
      }))
    })
    .catch(() => {
      alertList.value = []
    })
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await NewVisitorManagementApi.getTodayPage({
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      type: queryParams.visitStatus || undefined
    })
    const list = res?.data?.list ?? []
    const totalCount = res?.data?.total ?? 0
    tableData.value = list.map((it: any) => ({
      ...it,
      authStatus: it.signInTime && !it.signOutTime ? '已下发' : it.signOutTime ? '已回收' : '-'
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
  queryParams.visitStatus = ''
  queryParams.pageNo = 1
  loadData()
}

const openDetail = (row: any) => {
  currentRow.value = { ...row, status: row.signOutTime ? 'completed' : row.signInTime ? 'in' : 'pending', time: row.signInTime ? formatTime(row.signInTime).slice(11, 19) : '', location: row.currentLocation }
  detailVisible.value = true
}

const openAuthDispatch = (row: any) => {
  currentRow.value = row
  authDispatchVisible.value = true
}

const handleSignOut = async (row: any) => {
  const id = row.appointmentId ?? row.id
  if (!id) return
  const name = row.name ?? row.visitorName
  await ElMessageBox.confirm(`确认为访客 ${name || '该访客'} 办理签离？`, '确认签离')
  await NewVisitorManagementApi.signOut(id)
  ElMessage.success('签离成功')
  loadData()
  loadAlerts()
  detailVisible.value = false
}

onMounted(() => {
  loadData()
  loadAlerts()
})
</script>

<style lang="scss" scoped>
.visitor-manage {
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

.alerts-section {
  margin-bottom: 20px;
}

.alerts-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.alerts-title {
  font-size: 15px;
  font-weight: 500;
  margin: 0;
  color: var(--el-text-color-primary);
}

.alerts-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.alerts-scroll {
  overflow-x: auto;
  padding-bottom: 8px;
}

.alerts-list {
  display: flex;
  gap: 16px;
  min-width: min-content;
}

.alert-card {
  flex-shrink: 0;
  width: 300px;
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter);
  border-left-width: 4px;
  background: var(--el-bg-color);
  padding: 16px;
  &--unauthorized {
    border-left-color: var(--el-color-danger);
  }
  &--overtime_out {
    border-left-color: var(--el-color-danger);
  }
  &--overtime_in {
    border-left-color: var(--el-color-warning);
  }
  &__inner {
    display: flex;
    gap: 12px;
  }
  &__icon {
    font-size: 20px;
    color: var(--el-color-danger);
    margin-top: 2px;
  }
  &__body {
    flex: 1;
    min-width: 0;
  }
  &__title {
    font-size: 14px;
    font-weight: 500;
    margin: 0 0 4px;
    color: var(--el-text-color-primary);
  }
  &__desc, &__time {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin: 0 0 8px;
  }
  &__btns {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 8px;
  }
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
  :deep(.el-card__body) {
    padding: 20px;
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
