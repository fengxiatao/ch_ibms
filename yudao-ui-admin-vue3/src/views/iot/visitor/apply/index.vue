<template>
      <ContentWrap
    :body-style="{ padding: '0', height: '100%', display: 'flex', flexDirection: 'column' }"
    style="
      height: calc(100vh - var(--page-top-gap, 70px));
      padding-top: var(--page-top-gap, 70px);
      margin-bottom: 0;
    "
  >
  <div class="visitor-apply-page">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-card visiting">
            <div class="stat-icon">
              <Icon icon="ep:user" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.currentVisitingCount || 0 }}</div>
              <div class="stat-label">当前在访</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card today">
            <div class="stat-icon">
              <Icon icon="ep:calendar" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.todayVisitorCount || 0 }}</div>
              <div class="stat-label">今日访客</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card pending">
            <div class="stat-icon">
              <Icon icon="ep:clock" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.pendingApproveCount || 0 }}</div>
              <div class="stat-label">待审批</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card total">
            <div class="stat-icon">
              <Icon icon="ep:data-line" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalVisitorCount || 0 }}</div>
              <div class="stat-label">历史总数</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索区域 -->
    <ContentWrap>
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
        <el-form-item label="访客姓名" prop="visitorName">
          <el-input v-model="queryParams.visitorName" placeholder="请输入访客姓名" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="被访人" prop="visiteeName">
          <el-input v-model="queryParams.visiteeName" placeholder="请输入被访人姓名" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="来访状态" prop="visitStatus">
          <el-select v-model="queryParams.visitStatus" placeholder="全部" clearable>
            <el-option v-for="item in VisitStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批状态" prop="approveStatus">
          <el-select v-model="queryParams.approveStatus" placeholder="全部" clearable>
            <el-option v-for="item in ApproveStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="来访时间" prop="visitTimeRange">
          <el-date-picker
            v-model="visitTimeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            :shortcuts="dateShortcuts"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" /> 搜索
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <!-- 列表区域 -->
    <ContentWrap>
      <!-- 快捷筛选Tab -->
      <div class="quick-filter-tabs mb-16px">
        <el-radio-group v-model="quickFilter" size="default" @change="handleQuickFilter">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="pending">待审批</el-radio-button>
          <el-radio-button label="waiting">待访</el-radio-button>
          <el-radio-button label="visiting">在访</el-radio-button>
          <el-radio-button label="left">已离访</el-radio-button>
        </el-radio-group>
      </div>
      <div class="table-header">
        <el-button type="primary" @click="openForm('create')" v-hasPermi="['iot:visitor-apply:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增预约
        </el-button>
      </div>

      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="申请编号" prop="applyCode" width="150" />
        <el-table-column label="访客姓名" prop="visitorName" width="100" />
        <el-table-column label="联系电话" prop="visitorPhone" width="130" />
        <el-table-column label="被访人" prop="visiteeName" width="100" />
        <el-table-column label="来访事由" prop="visitReason" min-width="150" show-overflow-tooltip />
        <el-table-column label="来访状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getVisitStatusType(row.visitStatus)" size="small">
              {{ row.visitStatusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审批状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getApproveStatusType(row.approveStatus)" size="small">
              {{ row.approveStatusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="授权状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.authStatus !== undefined" :type="getAuthStatusType(row.authStatus)" size="small">
              {{ row.authStatusName }}
            </el-tag>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>
        <el-table-column label="计划来访" width="160" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.planVisitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDetail(row)">
              查看
            </el-button>
            <!-- 审批操作 -->
            <template v-if="row.approveStatus === 0">
              <el-button link type="success" size="small" @click="handleApprove(row)" v-hasPermi="['iot:visitor-apply:approve']">
                通过
              </el-button>
              <el-button link type="danger" size="small" @click="handleReject(row)" v-hasPermi="['iot:visitor-apply:approve']">
                拒绝
              </el-button>
            </template>
            <!-- 签到签离 -->
            <template v-if="row.approveStatus === 1">
              <el-button v-if="row.visitStatus === 0" link type="success" size="small" @click="handleCheckIn(row)">
                签到
              </el-button>
              <el-button v-if="row.visitStatus === 1" link type="warning" size="small" @click="handleCheckOut(row)">
                签离
              </el-button>
            </template>
            <!-- 权限操作 -->
            <el-button 
              v-if="row.approveStatus === 1 && row.authStatus !== 2 && row.authStatus !== 3" 
              link type="primary" size="small" 
              @click="openDispatch(row)"
              v-hasPermi="['iot:visitor-apply:dispatch']"
            >
              下发权限
            </el-button>
            <el-button 
              v-if="row.authStatus === 2" 
              link type="danger" size="small" 
              @click="handleRevoke(row)"
              v-hasPermi="['iot:visitor-apply:dispatch']"
            >
              回收权限
            </el-button>
            <!-- 删除 -->
            <el-button 
              v-if="row.approveStatus === 0 || row.visitStatus === 3" 
              link type="danger" size="small" 
              @click="handleDelete(row)"
              v-hasPermi="['iot:visitor-apply:delete']"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>

    <!-- 新增/编辑弹窗 -->
    <VisitorApplyForm ref="formRef" @success="getList" />

    <!-- 详情弹窗 -->
    <VisitorApplyDetail ref="detailRef" />

    <!-- 权限下发弹窗 -->
    <VisitorAuthDispatch ref="dispatchRef" @success="getList" />
  </div>
</ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate } from '@/utils/formatTime'
import { 
  VisitorApplyApi, 
  VisitStatusOptions, 
  ApproveStatusOptions,
  VisitorAuthStatusOptions,
  type VisitorApplyVO,
  type VisitorStatisticsVO
} from '@/api/iot/visitor'
import VisitorApplyForm from './VisitorApplyForm.vue'
import VisitorApplyDetail from './VisitorApplyDetail.vue'
import VisitorAuthDispatch from './VisitorAuthDispatch.vue'

defineOptions({ name: 'VisitorApplyList' })

const loading = ref(false)
const list = ref<VisitorApplyVO[]>([])
const total = ref(0)
const statistics = ref<VisitorStatisticsVO>({
  currentVisitingCount: 0,
  todayVisitorCount: 0,
  totalVisitorCount: 0,
  pendingApproveCount: 0,
  pendingDispatchCount: 0
})

const queryFormRef = ref()
const formRef = ref()
const detailRef = ref()
const dispatchRef = ref()

const visitTimeRange = ref<[string, string]>()

// 快捷筛选状态
const quickFilter = ref('all')

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  visitorName: undefined,
  visiteeName: undefined,
  visitReason: undefined,
  visitStatus: undefined,
  approveStatus: undefined,
  visitTimeStart: undefined as string | undefined,
  visitTimeEnd: undefined as string | undefined
})

// 快捷筛选处理
const handleQuickFilter = (filter: string) => {
  // 重置筛选条件
  queryParams.visitStatus = undefined
  queryParams.approveStatus = undefined
  
  switch (filter) {
    case 'pending': // 待审批
      (queryParams as any).approveStatus = 0
      break
    case 'waiting': // 待访（已审批通过但未到访）
      (queryParams as any).approveStatus = 1;
      (queryParams as any).visitStatus = 0
      break
    case 'visiting': // 在访
      (queryParams as any).approveStatus = 1;
      (queryParams as any).visitStatus = 1
      break
    case 'left': // 已离访
      (queryParams as any).approveStatus = 1;
      (queryParams as any).visitStatus = 2
      break
    case 'all':
    default:
      // 不设置任何筛选条件
      break
  }
  handleQuery()
}

// 日期快捷选项
const dateShortcuts = [
  { text: '今天', value: () => {
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    const end = new Date()
    end.setHours(23, 59, 59, 999)
    return [today, end]
  }},
  { text: '最近一周', value: () => {
    const end = new Date()
    const start = new Date()
    start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
    return [start, end]
  }},
  { text: '最近一个月', value: () => {
    const end = new Date()
    const start = new Date()
    start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
    return [start, end]
  }}
]

// 监听时间范围变化
watch(visitTimeRange, (val) => {
  if (val && val.length === 2) {
    queryParams.visitTimeStart = val[0]
    queryParams.visitTimeEnd = val[1]
  } else {
    queryParams.visitTimeStart = undefined
    queryParams.visitTimeEnd = undefined
  }
})

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const res = await VisitorApplyApi.getApplyPage(queryParams)
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const getStatistics = async () => {
  try {
    statistics.value = await VisitorApplyApi.getStatistics()
  } catch (e) {
    console.error('获取统计数据失败', e)
  }
}

// 搜索
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  visitTimeRange.value = undefined
  handleQuery()
}

// 格式化日期
const formatDateTime = (date: any) => {
  if (!date) return '-'
  return formatDate(date, 'YYYY-MM-DD HH:mm')
}

// 状态类型
const getVisitStatusType = (status: number) => {
  const item = VisitStatusOptions.find(o => o.value === status)
  return item?.type || 'info'
}

const getApproveStatusType = (status: number) => {
  const item = ApproveStatusOptions.find(o => o.value === status)
  return item?.type || 'info'
}

const getAuthStatusType = (status: number) => {
  const item = VisitorAuthStatusOptions.find(o => o.value === status)
  return item?.type || 'info'
}

// 打开表单
const openForm = (type: string, row?: VisitorApplyVO) => {
  formRef.value?.open(type, row?.id)
}

// 打开详情
const openDetail = (row: VisitorApplyVO) => {
  detailRef.value?.open(row.id)
}

// 打开权限下发
const openDispatch = (row: VisitorApplyVO) => {
  dispatchRef.value?.open(row.id)
}

// 审批通过
const handleApprove = async (row: VisitorApplyVO) => {
  try {
    await ElMessageBox.confirm(`确定审批通过访客"${row.visitorName}"的申请吗？`, '提示', { type: 'info' })
    await VisitorApplyApi.approve(row.id)
    ElMessage.success('审批通过')
    getList()
    getStatistics()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('审批失败', e)
    }
  }
}

// 审批拒绝
const handleReject = async (row: VisitorApplyVO) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝申请', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入拒绝原因（可选）'
    })
    await VisitorApplyApi.reject(row.id, value)
    ElMessage.success('已拒绝')
    getList()
    getStatistics()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('拒绝失败', e)
    }
  }
}

// 签到
const handleCheckIn = async (row: VisitorApplyVO) => {
  try {
    await ElMessageBox.confirm(`确定访客"${row.visitorName}"已到访签到吗？`, '提示', { type: 'info' })
    await VisitorApplyApi.checkIn(row.id)
    ElMessage.success('签到成功')
    getList()
    getStatistics()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('签到失败', e)
    }
  }
}

// 签离
const handleCheckOut = async (row: VisitorApplyVO) => {
  try {
    await ElMessageBox.confirm(`确定访客"${row.visitorName}"已离访签离吗？\n签离后将自动回收门禁权限。`, '提示', { type: 'warning' })
    await VisitorApplyApi.checkOut(row.id)
    ElMessage.success('签离成功')
    getList()
    getStatistics()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('签离失败', e)
    }
  }
}

// 回收权限
const handleRevoke = async (row: VisitorApplyVO) => {
  try {
    await ElMessageBox.confirm(`确定回收访客"${row.visitorName}"的门禁权限吗？`, '提示', { type: 'warning' })
    await VisitorApplyApi.revokeAuth(row.id)
    ElMessage.success('权限回收中')
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('回收权限失败', e)
    }
  }
}

// 删除
const handleDelete = async (row: VisitorApplyVO) => {
  try {
    await ElMessageBox.confirm(`确定删除访客"${row.visitorName}"的申请记录吗？`, '提示', { type: 'warning' })
    await VisitorApplyApi.deleteApply(row.id)
    ElMessage.success('删除成功')
    getList()
    getStatistics()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('删除失败', e)
    }
  }
}

onMounted(() => {
  getList()
  getStatistics()
})
</script>

<style scoped lang="scss">
.visitor-apply-page {
  .quick-filter-tabs {
    margin-bottom: 12px;
    
    :deep(.el-radio-button__inner) {
      padding: 8px 20px;
    }
  }
  
  .stats-cards {
    margin-bottom: 16px;

    .stat-card {
      display: flex;
      align-items: center;
      padding: 20px;
      border-radius: 8px;
      background: linear-gradient(135deg, var(--bg-start) 0%, var(--bg-end) 100%);
      color: #fff;

      &.visiting {
        --bg-start: #667eea;
        --bg-end: #764ba2;
      }

      &.today {
        --bg-start: #f093fb;
        --bg-end: #f5576c;
      }

      &.pending {
        --bg-start: #4facfe;
        --bg-end: #00f2fe;
      }

      &.total {
        --bg-start: #43e97b;
        --bg-end: #38f9d7;
      }

      .stat-icon {
        width: 60px;
        height: 60px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 50%;
        margin-right: 16px;
        font-size: 28px;
      }

      .stat-info {
        .stat-value {
          font-size: 32px;
          font-weight: 600;
          line-height: 1.2;
        }

        .stat-label {
          font-size: 14px;
          opacity: 0.9;
          margin-top: 4px;
        }
      }
    }
  }

  .table-header {
    margin-bottom: 16px;
  }
}
</style>
