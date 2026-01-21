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
      <el-form-item label="所属车场" prop="lotId">
        <el-select v-model="queryParams.lotId" placeholder="请选择车场" clearable>
          <el-option v-for="item in lotList" :key="item.id" :label="item.lotName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="车辆类别" prop="vehicleCategory">
        <el-select v-model="queryParams.vehicleCategory" placeholder="请选择车辆类别" clearable>
          <el-option v-for="item in VehicleCategoryOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="支付状态" prop="paymentStatus">
        <el-select v-model="queryParams.paymentStatus" placeholder="请选择支付状态" clearable>
          <el-option label="未支付" :value="0" />
          <el-option label="已支付" :value="1" />
          <el-option label="免费" :value="2" />
          <el-option label="已退款" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="记录状态" prop="recordStatus">
        <el-select v-model="queryParams.recordStatus" placeholder="请选择记录状态" clearable>
          <el-option label="在场" :value="1" />
          <el-option label="已出场" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="入场时间" prop="entryTime">
        <el-date-picker
          v-model="queryParams.entryTime"
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
      <el-table-column label="车牌号" prop="plateNumber" width="120" />
      <el-table-column label="车辆类别" width="100">
        <template #default="{ row }">
          <el-tag :type="getCategoryType(row.vehicleCategory)">
            {{ getCategoryLabel(row.vehicleCategory) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="入场时间" prop="entryTime" width="160" :formatter="dateFormatter" />
      <el-table-column label="出场时间" prop="exitTime" width="160" :formatter="dateFormatter" />
      <el-table-column label="停车时长" width="100">
        <template #default="{ row }">
          {{ formatDuration(row.parkingDuration) }}
        </template>
      </el-table-column>
      <el-table-column label="应收金额" width="100">
        <template #default="{ row }">
          {{ row.chargeAmount ? `¥${row.chargeAmount}` : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="实收金额" width="100">
        <template #default="{ row }">
          {{ row.paidAmount ? `¥${row.paidAmount}` : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="支付方式" width="100">
        <template #default="{ row }">
          {{ getPaymentMethodLabel(row.paymentMethod) }}
        </template>
      </el-table-column>
      <el-table-column label="支付状态" width="90">
        <template #default="{ row }">
          <el-tag :type="getPaymentStatusType(row.paymentStatus)">
            {{ getPaymentStatusLabel(row.paymentStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="记录状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.recordStatus === 2 ? 'success' : 'primary'">
            {{ row.recordStatus === 2 ? '已出场' : '在场' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="放行方式" width="100">
        <template #default="{ row }">
          {{ row.exitType === 'normal' ? '正常' : row.exitType === 'force' ? '强制出场' : row.exitType === 'free' ? '免费放行' : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleViewDetail(row)">详情</el-button>
          <el-button 
            v-if="row.paymentStatus === 1" 
            link 
            type="danger" 
            @click="handleRefund(row)"
            v-hasPermi="['iot:parking:refund:apply']"
          >退款</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 详情弹窗 -->
  <Dialog v-model="detailDialogVisible" title="停车记录详情" width="700px">
    <el-descriptions :column="2" border v-if="currentRecord">
      <el-descriptions-item label="车牌号">{{ currentRecord.plateNumber }}</el-descriptions-item>
      <el-descriptions-item label="车辆类别">{{ getCategoryLabel(currentRecord.vehicleCategory) }}</el-descriptions-item>
      <el-descriptions-item label="入场时间">{{ currentRecord.entryTime }}</el-descriptions-item>
      <el-descriptions-item label="出场时间">{{ currentRecord.exitTime || '-' }}</el-descriptions-item>
      <el-descriptions-item label="停车时长">{{ formatDuration(currentRecord.parkingDuration) }}</el-descriptions-item>
      <el-descriptions-item label="应收金额">{{ currentRecord.chargeAmount ? `¥${currentRecord.chargeAmount}` : '-' }}</el-descriptions-item>
      <el-descriptions-item label="实收金额">{{ currentRecord.paidAmount ? `¥${currentRecord.paidAmount}` : '-' }}</el-descriptions-item>
      <el-descriptions-item label="支付方式">{{ getPaymentMethodLabel(currentRecord.paymentMethod) }}</el-descriptions-item>
      <el-descriptions-item label="支付时间">{{ currentRecord.paymentTime || '-' }}</el-descriptions-item>
      <el-descriptions-item label="放行方式">{{ currentRecord.exitType === 'normal' ? '正常' : currentRecord.exitType === 'force' ? '强制出场' : '-' }}</el-descriptions-item>
      <el-descriptions-item label="入场操作员">{{ currentRecord.entryOperator || '-' }}</el-descriptions-item>
      <el-descriptions-item label="出场操作员">{{ currentRecord.exitOperator || '-' }}</el-descriptions-item>
      <el-descriptions-item label="备注" :span="2">{{ currentRecord.remark || '-' }}</el-descriptions-item>
    </el-descriptions>
    <div class="photo-section mt-20px" v-if="currentRecord">
      <el-row :gutter="20">
        <el-col :span="12">
          <h4>入场照片</h4>
          <el-image v-if="currentRecord.entryPhotoUrl" :src="currentRecord.entryPhotoUrl" :preview-src-list="[currentRecord.entryPhotoUrl]" fit="contain" style="width: 100%; max-height: 200px;" />
          <el-empty v-else description="暂无照片" :image-size="60" />
        </el-col>
        <el-col :span="12">
          <h4>出场照片</h4>
          <el-image v-if="currentRecord.exitPhotoUrl" :src="currentRecord.exitPhotoUrl" :preview-src-list="[currentRecord.exitPhotoUrl]" fit="contain" style="width: 100%; max-height: 200px;" />
          <el-empty v-else description="暂无照片" :image-size="60" />
        </el-col>
      </el-row>
    </div>
    <template #footer>
      <el-button @click="detailDialogVisible = false">关闭</el-button>
    </template>
  </Dialog>

  <!-- 退款弹窗 -->
  <Dialog v-model="refundDialogVisible" title="申请退款" width="500px">
    <el-form :model="refundForm" label-width="100px" v-if="refundRecord">
      <el-form-item label="车牌号">
        <el-input :value="refundRecord.plateNumber" disabled />
      </el-form-item>
      <el-form-item label="原支付金额">
        <el-input :value="`¥${refundRecord.paidAmount}`" disabled />
      </el-form-item>
      <el-form-item label="退款金额" required>
        <el-input-number v-model="refundForm.refundFee" :min="0.01" :max="refundRecord.paidAmount" :precision="2" />
        <span class="ml-10px text-gray-500">元</span>
      </el-form-item>
      <el-form-item label="退款原因" required>
        <el-input v-model="refundForm.refundReason" type="textarea" :rows="2" placeholder="请输入退款原因" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="refundForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="refundDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitRefund">提交退款申请</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { dateFormatter } from '@/utils/formatTime'
import { ParkingRecordApi, ParkingLotApi, ParkingRefundApi, VehicleCategoryOptions, PaymentMethodOptions } from '@/api/iot/parking'
import { ContentWrap } from '@/components/ContentWrap'
import { Dialog } from '@/components/Dialog'
import { Pagination } from '@/components/Pagination'

defineOptions({ name: 'ParkingRecordHistory' })

const message = useMessage()

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const lotList = ref<any[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  plateNumber: undefined,
  lotId: undefined,
  vehicleCategory: undefined,
  paymentStatus: undefined,
  recordStatus: undefined,
  entryTime: undefined
})

const detailDialogVisible = ref(false)
const currentRecord = ref<any>(null)

const getCategoryType = (category: string) => {
  const types: any = { free: 'success', monthly: 'primary', temporary: 'warning' }
  return types[category] || 'info'
}

const getCategoryLabel = (category: string) => {
  const item = VehicleCategoryOptions.find(i => i.value === category)
  return item?.label || '-'
}

const getPaymentMethodLabel = (method: string) => {
  const item = PaymentMethodOptions.find(i => i.value === method)
  return item?.label || '-'
}

const getPaymentStatusType = (status: number) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'info'
    case 3: return 'danger'
    default: return 'info'
  }
}

const getPaymentStatusLabel = (status: number) => {
  switch (status) {
    case 0: return '未支付'
    case 1: return '已支付'
    case 2: return '免费'
    case 3: return '已退款'
    default: return '未知'
  }
}

const formatDuration = (minutes: number) => {
  if (!minutes) return '-'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 24) {
    const days = Math.floor(hours / 24)
    return `${days}天${hours % 24}时${mins}分`
  }
  return hours > 0 ? `${hours}时${mins}分` : `${mins}分钟`
}

const getLotList = async () => {
  lotList.value = await ParkingLotApi.getSimpleList()
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ParkingRecordApi.getRecordPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.plateNumber = undefined
  queryParams.lotId = undefined
  queryParams.vehicleCategory = undefined
  queryParams.paymentStatus = undefined
  queryParams.recordStatus = undefined
  queryParams.entryTime = undefined
  handleQuery()
}

const handleViewDetail = async (row: any) => {
  currentRecord.value = await ParkingRecordApi.getRecord(row.id)
  detailDialogVisible.value = true
}

// 退款相关
const refundDialogVisible = ref(false)
const refundForm = reactive({
  recordId: undefined as number | undefined,
  refundFee: undefined as number | undefined,
  refundReason: '',
  remark: ''
})
const refundRecord = ref<any>(null)

const handleRefund = (row: any) => {
  refundRecord.value = row
  refundForm.recordId = row.id
  refundForm.refundFee = row.paidAmount
  refundForm.refundReason = ''
  refundForm.remark = ''
  refundDialogVisible.value = true
}

const submitRefund = async () => {
  if (!refundForm.refundReason) {
    message.warning('请输入退款原因')
    return
  }
  try {
    await message.confirm(`确定要申请退款吗？退款金额：¥${refundForm.refundFee}`)
    await ParkingRefundApi.applyRefund({
      recordId: refundForm.recordId,
      refundFee: refundForm.refundFee,
      refundReason: refundForm.refundReason,
      remark: refundForm.remark
    })
    message.success('退款申请已提交')
    refundDialogVisible.value = false
    await getList()
  } catch {}
}

onMounted(() => {
  getLotList()
  getList()
})
</script>

<style scoped>
.photo-section h4 {
  margin-bottom: 10px;
  font-size: 14px;
  color: #666;
}
</style>
