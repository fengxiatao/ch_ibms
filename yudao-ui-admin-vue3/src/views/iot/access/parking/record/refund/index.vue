<template>
  <ContentWrap
    :body-style="{
      padding: '10px',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      backgroundColor: 'var(--el-bg-color)'
    }"
    style="height: calc(100vh - var(--page-top-gap, 70px)); padding-top: var(--page-top-gap, 70px)"
  >
    <!-- 搜索工作栏 -->
    <el-form ref="queryFormRef" :model="queryParams" :inline="true" class="mb-15px">
      <el-form-item label="车牌号" prop="plateNumber">
        <el-input v-model="queryParams.plateNumber" placeholder="请输入车牌号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="退款状态" prop="refundStatus">
        <el-select v-model="queryParams.refundStatus" placeholder="请选择退款状态" clearable>
          <el-option v-for="item in RefundStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="原订单号" prop="outTradeNo">
        <el-input v-model="queryParams.outTradeNo" placeholder="请输入原订单号" clearable />
      </el-form-item>
      <el-form-item label="退款单号" prop="outRefundNo">
        <el-input v-model="queryParams.outRefundNo" placeholder="请输入退款单号" clearable />
      </el-form-item>
      <el-form-item label="申请时间" prop="applyTime">
        <el-date-picker
          v-model="queryParams.applyTime"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery"><Icon icon="ep:search" class="mr-5px" />搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" />重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="退款单号" prop="outRefundNo" width="180" />
      <el-table-column label="车牌号" prop="plateNumber" width="120" />
      <el-table-column label="原订单金额" width="110">
        <template #default="{ row }">
          <span class="text-primary">¥{{ row.totalFee }}</span>
        </template>
      </el-table-column>
      <el-table-column label="退款金额" width="110">
        <template #default="{ row }">
          <span class="text-danger">¥{{ row.refundFee }}</span>
        </template>
      </el-table-column>
      <el-table-column label="退款原因" prop="refundReason" min-width="150" show-overflow-tooltip />
      <el-table-column label="退款状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.refundStatus)">
            {{ getStatusLabel(row.refundStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="申请时间" prop="applyTime" width="160" :formatter="dateFormatter" />
      <el-table-column label="退款时间" prop="refundTime" width="160" :formatter="dateFormatter" />
      <el-table-column label="申请人" prop="applyUser" width="100" />
      <el-table-column label="审核人" prop="auditUser" width="100" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <template v-if="row.refundStatus === 0">
            <el-button 
              link 
              type="primary" 
              @click="handleExecuteRefund(row)"
              v-hasPermi="['iot:parking:refund:execute']"
            >
              执行退款
            </el-button>
            <el-button 
              link 
              type="danger" 
              @click="handleCloseRefund(row)"
              v-hasPermi="['iot:parking:refund:close']"
            >
              关闭
            </el-button>
          </template>
          <el-button 
            link 
            type="primary" 
            @click="handleSyncStatus(row)"
            v-if="row.refundStatus === 0"
            v-hasPermi="['iot:parking:refund:query']"
          >
            同步状态
          </el-button>
          <el-button link type="info" @click="handleDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="退款详情" width="600px">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="退款单号">{{ currentRow.outRefundNo }}</el-descriptions-item>
        <el-descriptions-item label="微信退款单号">{{ currentRow.refundId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="车牌号">{{ currentRow.plateNumber }}</el-descriptions-item>
        <el-descriptions-item label="原订单号">{{ currentRow.outTradeNo }}</el-descriptions-item>
        <el-descriptions-item label="原订单金额">¥{{ currentRow.totalFee }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">¥{{ currentRow.refundFee }}</el-descriptions-item>
        <el-descriptions-item label="退款状态">
          <el-tag :type="getStatusType(currentRow.refundStatus)">
            {{ getStatusLabel(currentRow.refundStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="退款原因">{{ currentRow.refundReason }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ formatDate(currentRow.applyTime) }}</el-descriptions-item>
        <el-descriptions-item label="退款时间">{{ formatDate(currentRow.refundTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentRow.applyUser }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ currentRow.auditUser || '-' }}</el-descriptions-item>
        <el-descriptions-item label="失败原因" :span="2" v-if="currentRow.failReason">
          <span class="text-danger">{{ currentRow.failReason }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 关闭原因弹窗 -->
    <el-dialog v-model="closeDialogVisible" title="关闭退款" width="400px">
      <el-form :model="closeForm" label-width="80px">
        <el-form-item label="关闭原因" required>
          <el-input v-model="closeForm.reason" type="textarea" :rows="3" placeholder="请输入关闭原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCloseRefund">确定</el-button>
      </template>
    </el-dialog>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ParkingRefundApi, RefundStatusOptions } from '@/api/iot/parking'
import { dateFormatter } from '@/utils/formatTime'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'ParkingRefundRecord' })

const message = useMessage()

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  plateNumber: undefined,
  refundStatus: undefined,
  outTradeNo: undefined,
  outRefundNo: undefined,
  applyTime: undefined
})

const detailDialogVisible = ref(false)
const currentRow = ref<any>(null)

const closeDialogVisible = ref(false)
const closeForm = reactive({
  id: undefined as number | undefined,
  reason: ''
})

/** 获取列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await ParkingRefundApi.getRefundRecordPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置 */
const resetQuery = () => {
  queryParams.plateNumber = undefined
  queryParams.refundStatus = undefined
  queryParams.outTradeNo = undefined
  queryParams.outRefundNo = undefined
  queryParams.applyTime = undefined
  handleQuery()
}

/** 获取状态类型 */
const getStatusType = (status: number) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'danger'
    case 3: return 'info'
    default: return 'info'
  }
}

/** 获取状态标签 */
const getStatusLabel = (status: number) => {
  const option = RefundStatusOptions.find(item => item.value === status)
  return option ? option.label : '未知'
}

/** 执行退款 */
const handleExecuteRefund = async (row: any) => {
  try {
    await message.confirm(`确定要执行退款吗？退款金额：¥${row.refundFee}`)
    await ParkingRefundApi.executeRefund(row.id)
    message.success('退款执行成功')
    await getList()
  } catch {}
}

/** 关闭退款 */
const handleCloseRefund = (row: any) => {
  closeForm.id = row.id
  closeForm.reason = ''
  closeDialogVisible.value = true
}

/** 提交关闭退款 */
const submitCloseRefund = async () => {
  if (!closeForm.reason) {
    message.warning('请输入关闭原因')
    return
  }
  try {
    await ParkingRefundApi.closeRefund(closeForm.id!, closeForm.reason)
    message.success('退款已关闭')
    closeDialogVisible.value = false
    await getList()
  } catch {}
}

/** 同步状态 */
const handleSyncStatus = async (row: any) => {
  try {
    await ParkingRefundApi.syncRefundStatus(row.id)
    message.success('状态同步成功')
    await getList()
  } catch {}
}

/** 查看详情 */
const handleDetail = (row: any) => {
  currentRow.value = row
  detailDialogVisible.value = true
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.text-primary {
  color: var(--el-color-primary);
  font-weight: 500;
}
.text-danger {
  color: var(--el-color-danger);
  font-weight: 500;
}
</style>
