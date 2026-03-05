<template>
  <div class="visitor-record-page">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon">
          <Icon icon="ep:user" />
        </div>
        <div class="stat-info">
          <div class="stat-label">当前在访人数</div>
          <div class="stat-value">{{ statistics.currentVisitingCount || 0 }}人</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <Icon icon="ep:calendar" />
        </div>
        <div class="stat-info">
          <div class="stat-label">今日访客人数</div>
          <div class="stat-value">{{ statistics.todayVisitorCount || 0 }}人</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <Icon icon="ep:data-line" />
        </div>
        <div class="stat-info">
          <div class="stat-label">历史访客总数</div>
          <div class="stat-value">{{ statistics.totalVisitorCount || 0 }}人</div>
        </div>
      </div>
    </div>

    <!-- 搜索区域 -->
    <ContentWrap class="visitor-record-page__header">
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
        <el-form-item label="日期筛选">
          <el-radio-group v-model="dateFilter" size="default" @change="handleDateFilter">
            <el-radio-button label="today">今日</el-radio-button>
            <el-radio-button label="week">近一周</el-radio-button>
            <el-radio-button label="month">近一月</el-radio-button>
          </el-radio-group>
          <el-date-picker
            v-model="visitTimeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="请选择开始日期"
            end-placeholder="请选择结束日期"
            value-format="YYYY-MM-DD"
            style="margin-left: 12px; width: 260px"
            @change="handleDateRangeChange"
          />
        </el-form-item>
      </el-form>
      <el-form :model="queryParams" ref="queryFormRef2" :inline="true" label-width="80px">
        <el-form-item label="访客姓名" prop="visitorName">
          <el-input
            v-model="queryParams.visitorName"
            placeholder="请输入访客姓名"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="来访事由" prop="visitReason">
          <el-select v-model="queryParams.visitReason" placeholder="请选择来访事由" clearable>
            <el-option
              v-for="item in visitReasonOptions"
              :key="item.id"
              :label="item.reasonName"
              :value="item.reasonName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="被访人姓名" prop="visiteeName">
          <el-input
            v-model="queryParams.visiteeName"
            placeholder="请输入被访人姓名"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="来访状态" prop="visitStatus">
          <el-select v-model="queryParams.visitStatus" placeholder="请选择来访状态" clearable>
            <el-option
              v-for="item in RecordStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <!-- 列表区域 -->
    <ContentWrap class="visitor-record-page__content">
      <div class="table-header">
        <span class="table-title">访客记录</span>
      </div>

      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column label="访客姓名" prop="visitorName" width="100" align="center" />
        <el-table-column label="访客联系电话" prop="visitorPhone" width="130" align="center" />
        <el-table-column label="被访人姓名" prop="visiteeName" width="100" align="center" />
        <el-table-column label="来访事由" prop="visitReason" width="100" align="center" />
        <el-table-column label="来访状态" width="90" align="center">
          <template #default="{ row }">
            <span :class="getVisitStatusClass(row.visitStatus)">
              {{ row.visitStatusName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="签到时间" width="160" align="center">
          <template #default="{ row }">
            {{ row.actualVisitTime ? formatDate(row.actualVisitTime, 'YYYY/MM/DD HH:mm:ss') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="签离时间" width="160" align="center">
          <template #default="{ row }">
            {{ row.actualLeaveTime ? formatDate(row.actualLeaveTime, 'YYYY/MM/DD HH:mm:ss') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDetail(row)"> 详情 </el-button>
            <el-button link type="primary" size="small" @click="viewAccessRecords(row)">
              开门记录
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </ContentWrap>

    <!-- 分页 -->
    <ContentWrap class="visitor-record-page__footer">
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>

    <!-- 详情弹窗 -->
    <VisitorApplyDetail ref="detailRef" />

    <!-- 开门记录弹窗 -->
    <VisitorAccessRecordDialog ref="accessRecordRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { formatDate } from '@/utils/formatTime'
import {
  VisitorApplyApi,
  VisitorReasonApi,
  type VisitorApplyVO,
  type VisitorStatisticsVO
} from '@/api/iot/visitor'
import VisitorApplyDetail from '../apply/VisitorApplyDetail.vue'
import VisitorAccessRecordDialog from './VisitorAccessRecordDialog.vue'

defineOptions({ name: 'VisitorRecordList' })

const loading = ref(false)
const list = ref<VisitorApplyVO[]>([])
const total = ref(0)
const visitReasonOptions = ref<any[]>([])
const statistics = ref<VisitorStatisticsVO>({
  currentVisitingCount: 0,
  todayVisitorCount: 0,
  totalVisitorCount: 0,
  pendingApproveCount: 0,
  pendingDispatchCount: 0
})

// 记录状态选项（只显示在访、离访）
const RecordStatusOptions = [
  { value: 1, label: '在访' },
  { value: 2, label: '离访' }
]

const queryFormRef = ref()
const detailRef = ref()
const accessRecordRef = ref()

const visitTimeRange = ref<[string, string]>()
const dateFilter = ref('today') // 默认今日

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  visitorName: undefined,
  visitReason: undefined,
  visiteeName: undefined,
  visitStatus: undefined,
  approveStatus: 1, // 只查询已通过的申请
  hasVisited: true, // 只查询已签到的记录
  visitTimeStart: undefined as string | undefined,
  visitTimeEnd: undefined as string | undefined
})

// 日期快捷筛选
// 来访记录列表：查看实际来访时间，"近一周"应该是从今天往前7天（过去一周）
const handleDateFilter = (filter: string) => {
  let end = new Date()
  end.setHours(23, 59, 59, 999)

  let start = new Date()
  switch (filter) {
    case 'today':
      // 今日：只看今天
      start.setHours(0, 0, 0, 0)
      break
    case 'week':
      // 近一周：从今天往前7天（过去一周的来访记录）
      start.setTime(end.getTime() - 7 * 24 * 60 * 60 * 1000)
      start.setHours(0, 0, 0, 0)
      break
    case 'month':
      // 近一月：从今天往前30天（过去一月的来访记录）
      start.setTime(end.getTime() - 30 * 24 * 60 * 60 * 1000)
      start.setHours(0, 0, 0, 0)
      break
  }

  visitTimeRange.value = [formatDate(start, 'YYYY-MM-DD'), formatDate(end, 'YYYY-MM-DD')]
  queryParams.visitTimeStart = formatDate(start, 'YYYY-MM-DD') + ' 00:00:00'
  queryParams.visitTimeEnd = formatDate(end, 'YYYY-MM-DD') + ' 23:59:59'
  handleQuery()
}

// 日期范围变化
const handleDateRangeChange = (val: any) => {
  if (val && val.length === 2) {
    queryParams.visitTimeStart = val[0] + ' 00:00:00'
    queryParams.visitTimeEnd = val[1] + ' 23:59:59'
    dateFilter.value = '' // 清除快捷筛选状态
  } else {
    queryParams.visitTimeStart = undefined
    queryParams.visitTimeEnd = undefined
  }
  handleQuery()
}

// 获取来访状态样式
const getVisitStatusClass = (status: number) => {
  switch (status) {
    case 1:
      return 'status-visiting'
    case 2:
      return 'status-left'
    default:
      return ''
  }
}

// 获取来访事由列表
const getVisitReasonList = async () => {
  try {
    visitReasonOptions.value = await VisitorReasonApi.getReasonList()
  } catch (e) {
    console.error('获取来访事由失败', e)
  }
}

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
  dateFilter.value = 'today'
  queryParams.visitStatus = undefined
  queryParams.visitReason = undefined
  handleDateFilter('today')
}

// 打开详情
const openDetail = (row: VisitorApplyVO) => {
  detailRef.value?.open(row.id)
}

// 查看开门记录
const viewAccessRecords = (row: VisitorApplyVO) => {
  accessRecordRef.value?.open(row)
}

onMounted(() => {
  getVisitReasonList()
  getStatistics()
  handleDateFilter('today') // 默认加载今日数据
})
</script>

<style scoped lang="scss">
.visitor-record-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  box-sizing: border-box;
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
}

.stats-cards {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
  flex-shrink: 0;

  .stat-card {
    flex: 1;
    display: flex;
    align-items: center;
    padding: 20px 24px;
    background: #fff;
    border-radius: 8px;
    border: 1px solid #e4e7ed;

    .stat-icon {
      width: 48px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #ecf5ff;
      border-radius: 50%;
      margin-right: 16px;
      font-size: 24px;
      color: #409eff;
    }

    .stat-info {
      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-bottom: 4px;
      }

      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: #409eff;
      }
    }
  }
}

.visitor-record-page__header {
  flex-shrink: 0;
}

.visitor-record-page__content {
  flex: 1;
  min-height: 0;
  overflow: auto;
  margin-bottom: 0 !important;

  .table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .table-title {
      font-size: 16px;
      font-weight: 500;
      color: #303133;
    }
  }
}

.visitor-record-page__footer {
  flex-shrink: 0;
  margin-bottom: 0 !important;
}

// 来访状态样式
.status-visiting {
  color: #67c23a;
}

.status-left {
  color: #909399;
}
</style>
