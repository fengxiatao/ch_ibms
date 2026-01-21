<template>
        <ContentWrap
    :body-style="{ padding: '0', height: '100%', display: 'flex', flexDirection: 'column' }"
    style="
      height: calc(100vh - var(--page-top-gap, 70px));
      padding-top: var(--page-top-gap, 70px);
      margin-bottom: 0;
    "
  >
  <div class="visitor-record-page">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="16">
        <el-col :span="8">
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
        <el-col :span="8">
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
        <el-col :span="8">
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
        <el-form-item label="来访事由" prop="visitReason">
          <el-input v-model="queryParams.visitReason" placeholder="请输入来访事由" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="被访人" prop="visiteeName">
          <el-input v-model="queryParams.visiteeName" placeholder="请输入被访人姓名" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="来访状态" prop="visitStatus">
          <el-select v-model="queryParams.visitStatus" placeholder="全部" clearable>
            <el-option v-for="item in RecordStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
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
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="访客姓名" prop="visitorName" width="100" />
        <el-table-column label="联系电话" prop="visitorPhone" width="130" />
        <el-table-column label="来访事由" prop="visitReason" min-width="150" show-overflow-tooltip />
        <el-table-column label="被访人" prop="visiteeName" width="100" />
        <el-table-column label="来访状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getVisitStatusType(row.visitStatus)" size="small">
              {{ row.visitStatusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="到访时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.actualVisitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="离访时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.actualLeaveTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDetail(row)">
              查看详情
            </el-button>
            <el-button link type="primary" size="small" @click="viewAccessRecords(row)">
              通行记录
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-hasPermi="['iot:visitor-apply:delete']">
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

    <!-- 详情弹窗 -->
    <VisitorApplyDetail ref="detailRef" />

    <!-- 通行记录弹窗 -->
    <VisitorAccessRecordDialog ref="accessRecordRef" />
  </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { 
  VisitorApplyApi, 
  VisitStatusOptions,
  type VisitorApplyVO,
  type VisitorStatisticsVO
} from '@/api/iot/visitor'
import VisitorApplyDetail from '../apply/VisitorApplyDetail.vue'
import VisitorAccessRecordDialog from './VisitorAccessRecordDialog.vue'

defineOptions({ name: 'VisitorRecordList' })

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

// 记录状态选项（只显示在访、离访、已取消）
const RecordStatusOptions = [
  { value: 1, label: '在访', type: 'success' },
  { value: 2, label: '离访', type: 'default' },
  { value: 3, label: '已取消', type: 'danger' }
]

const queryFormRef = ref()
const detailRef = ref()
const accessRecordRef = ref()

const visitTimeRange = ref<[string, string]>()

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  visitorName: undefined,
  visitReason: undefined, // 来访事由筛选
  visiteeName: undefined,
  visitStatus: undefined,
  approveStatus: 1, // 只查询已通过的申请
  visitTimeStart: undefined as string | undefined,
  visitTimeEnd: undefined as string | undefined
})

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
  queryParams.visitStatus = undefined
  queryParams.visitReason = undefined
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

// 打开详情
const openDetail = (row: VisitorApplyVO) => {
  detailRef.value?.open(row.id)
}

// 查看通行记录
const viewAccessRecords = (row: VisitorApplyVO) => {
  accessRecordRef.value?.open(row)
}

// 删除记录
const message = useMessage()
const handleDelete = async (row: VisitorApplyVO) => {
  try {
    await message.delConfirm('确定要删除该访客记录吗？')
    await VisitorApplyApi.deleteApply(row.id)
    message.success('删除成功')
    getList()
    getStatistics()
  } catch (e) {
    // 用户取消或删除失败
  }
}

onMounted(() => {
  getList()
  getStatistics()
})
</script>

<style scoped lang="scss">
.visitor-record-page {
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
}
</style>
