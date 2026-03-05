<template>
  <div class="visitor-apply-page">
    <!-- 搜索区域 -->
    <ContentWrap class="visitor-apply-page__header">
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
        <el-form-item label="来访状态" prop="approveStatus">
          <el-select v-model="queryParams.approveStatus" placeholder="请选择来访状态" clearable>
            <el-option
              v-for="item in ApproveStatusOptions"
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
    <ContentWrap class="visitor-apply-page__content">
      <div class="table-header">
        <span class="table-title">访客预约列表</span>
        <el-button
          type="primary"
          @click="openForm('create')"
          v-hasPermi="['iot:visitor-apply:create']"
        >
          新增预约
        </el-button>
      </div>

      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column label="访客姓名" prop="visitorName" width="100" align="center" />
        <el-table-column label="访客联系电话" prop="visitorPhone" width="130" align="center" />
        <el-table-column label="被访人姓名" prop="visiteeName" width="100" align="center" />
        <el-table-column label="来访事由" prop="visitReason" width="100" align="center" />
        <el-table-column label="审批状态" width="90" align="center">
          <template #default="{ row }">
            <span :class="getApproveStatusClass(row.approveStatus)">
              {{ row.approveStatusName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="来访时间" width="200" align="center">
          <template #default="{ row }">
            {{ formatDateRange(row.planVisitTime, row.planLeaveTime) }}
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="120" align="center">
          <template #default="{ row }">
            {{ formatDate(row.applyTime, 'YYYY/MM/DD') }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDetail(row)"> 详情 </el-button>
            <!-- 审批操作 -->
            <template v-if="row.approveStatus === 0">
              <el-button
                link
                type="success"
                size="small"
                @click="handleApprove(row)"
                v-hasPermi="['iot:visitor-apply:approve']"
              >
                通过
              </el-button>
              <el-button
                link
                type="danger"
                size="small"
                @click="handleReject(row)"
                v-hasPermi="['iot:visitor-apply:approve']"
              >
                拒绝
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </ContentWrap>

    <!-- 分页 -->
    <ContentWrap class="visitor-apply-page__footer">
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate } from '@/utils/formatTime'
import {
  VisitorApplyApi,
  VisitorReasonApi,
  ApproveStatusOptions,
  type VisitorApplyVO
} from '@/api/iot/visitor'
import VisitorApplyForm from './VisitorApplyForm.vue'
import VisitorApplyDetail from './VisitorApplyDetail.vue'

defineOptions({ name: 'VisitorApplyList' })

const loading = ref(false)
const list = ref<VisitorApplyVO[]>([])
const total = ref(0)
const visitReasonOptions = ref<any[]>([])

const queryFormRef = ref()
const formRef = ref()
const detailRef = ref()

const visitTimeRange = ref<[string, string]>()
const dateFilter = ref('week') // 默认近一周

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  visitorName: undefined,
  visiteeName: undefined,
  visitReason: undefined,
  approveStatus: undefined,
  visitTimeStart: undefined as string | undefined,
  visitTimeEnd: undefined as string | undefined
})

// 日期快捷筛选
// 访客预约列表：查看计划来访时间，"近一周"应该是从今天开始往后一周（未来一周）
const handleDateFilter = (filter: string) => {
  let start = new Date()
  start.setHours(0, 0, 0, 0)
  
  let end = new Date()
  switch (filter) {
    case 'today':
      // 今日：只看今天
      end.setHours(23, 59, 59, 999)
      break
    case 'week':
      // 近一周：从今天开始往后7天（未来一周的预约）
      end.setTime(start.getTime() + 7 * 24 * 60 * 60 * 1000)
      end.setHours(23, 59, 59, 999)
      break
    case 'month':
      // 近一月：从今天开始往后30天（未来一月的预约）
      end.setTime(start.getTime() + 30 * 24 * 60 * 60 * 1000)
      end.setHours(23, 59, 59, 999)
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

// 格式化日期范围显示
const formatDateRange = (start: any, end: any) => {
  if (!start) return '-'
  const startStr = formatDate(start, 'YYYY/MM/DD')
  const endStr = end ? formatDate(end, 'YYYY/MM/DD') : startStr
  return `${startStr} - ${endStr}`
}

// 获取审批状态样式
const getApproveStatusClass = (status: number) => {
  switch (status) {
    case 0:
      return 'status-pending'
    case 1:
      return 'status-approved'
    case 2:
      return 'status-rejected'
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

// 搜索
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  visitTimeRange.value = undefined
  dateFilter.value = 'week'
  handleDateFilter('week')
}

// 打开表单
const openForm = (type: string, row?: VisitorApplyVO) => {
  formRef.value?.open(type, row?.id)
}

// 打开详情
const openDetail = (row: VisitorApplyVO) => {
  detailRef.value?.open(row.id)
}

// 审批通过
const handleApprove = async (row: VisitorApplyVO) => {
  try {
    await ElMessageBox.confirm(`确定审批通过访客"${row.visitorName}"的申请吗？`, '提示', {
      type: 'info'
    })
    await VisitorApplyApi.approve(row.id)
    ElMessage.success('审批通过')
    getList()
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
  } catch (e) {
    if (e !== 'cancel') {
      console.error('拒绝失败', e)
    }
  }
}

onMounted(() => {
  getVisitReasonList()
  handleDateFilter('week') // 默认加载近一周数据
})
</script>

<style scoped lang="scss">
.visitor-apply-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  box-sizing: border-box;
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
}

.visitor-apply-page__header {
  flex-shrink: 0;
}

.visitor-apply-page__content {
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

.visitor-apply-page__footer {
  flex-shrink: 0;
  margin-bottom: 0 !important;
}

// 状态样式
.status-pending {
  color: #e6a23c;
}

.status-approved {
  color: #67c23a;
}

.status-rejected {
  color: #f56c6c;
}
</style>
