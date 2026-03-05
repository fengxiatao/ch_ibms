<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ParkingChargeApi,
  type ParkingChargeRuleVO,
  type ParkingRecordPageReqVO,
  type ParkingRecordRespVO,
  type ParkingRecordManualPayReqVO
} from '@/api/access/parkingCharge'

defineOptions({ name: 'ParkingCharge' })

type FeeVehicleType = 'small' | 'medium' | 'large'

interface FeeSetting {
  firstHour: number
  nextHour: number
  dailyCap: number
}

const DEFAULT_LOT_ID = 1

const feeSettings = reactive<Record<FeeVehicleType, FeeSetting>>({
  small: { firstHour: 5, nextHour: 2, dailyCap: 30 },
  medium: { firstHour: 8, nextHour: 3, dailyCap: 50 },
  large: { firstHour: 10, nextHour: 4, dailyCap: 80 }
})

const calcForm = reactive({
  plateNo: '',
  inTime: '',
  outTime: '',
  vehicleType: 'small' as FeeVehicleType,
  discountType: '' as '' | 'holiday' | 'vip' | 'custom',
  customDiscount: 1
})

const calcResultVisible = ref(false)

const discountFactor = computed(() => {
  if (calcForm.discountType === 'holiday') return 0.8
  if (calcForm.discountType === 'vip') return 0.9
  if (calcForm.discountType === 'custom') {
    const v = Number(calcForm.customDiscount)
    if (Number.isNaN(v)) return 1
    return Math.min(1, Math.max(0.1, v))
  }
  return 1
})

const calcDurationMinutes = computed(() => {
  if (!calcForm.inTime || !calcForm.outTime) return 0
  const start = dayjs(calcForm.inTime)
  const end = dayjs(calcForm.outTime)
  if (!start.isValid() || !end.isValid()) return 0
  const diff = end.diff(start, 'minute')
  return Math.max(0, diff)
})

const calcDurationHours = computed(() => Math.max(0, Math.ceil(calcDurationMinutes.value / 60)))

const calcExpectedAmount = computed(() => {
  const hours = calcDurationHours.value
  if (!hours) return 0
  const setting = feeSettings[calcForm.vehicleType]
  const first = Math.min(1, hours)
  const rest = Math.max(0, hours - 1)
  const raw = first * setting.firstHour + rest * setting.nextHour
  const days = Math.max(1, Math.ceil(calcDurationMinutes.value / (60 * 24)))
  const capped = Math.min(raw, days * setting.dailyCap)
  return Number((capped * discountFactor.value).toFixed(2))
})

const calcFee = () => {
  if (calcForm.discountType === 'custom') {
    const v = Number(calcForm.customDiscount)
    if (Number.isNaN(v) || v < 0.1 || v > 1) {
      ElMessage.warning('自定义折扣系数必须在0.1-1之间')
      return
    }
  }
  if (!calcForm.plateNo.trim()) {
    ElMessage.warning('请输入车牌号')
    return
  }
  if (!calcForm.inTime || !calcForm.outTime) {
    ElMessage.warning('请选择入场/出场时间')
    return
  }
  if (calcDurationMinutes.value <= 0) {
    ElMessage.warning('出场时间需晚于入场时间')
    return
  }
  calcResultVisible.value = true
  ElMessage.success('费用计算完成')
}

const recordsQuery = reactive({
  pageNo: 1,
  pageSize: 10,
  plateNo: '',
  startDate: '',
  endDate: ''
})

const recordsLoading = ref(false)
const list = ref<ParkingRecordRespVO[]>([])
const total = ref(0)

const vehicleTypeLabel = (t: any) => {
  if (t === 'small' || t === 'temp') return '小型车'
  if (t === 'medium') return '中型车'
  if (t === 'large') return '大型车'
  return '-'
}

const fetchRecords = async () => {
  recordsLoading.value = true
  try {
    const params: ParkingRecordPageReqVO = {
      pageNo: recordsQuery.pageNo,
      pageSize: recordsQuery.pageSize,
      plateNo: recordsQuery.plateNo.trim() || undefined
    }
    if (recordsQuery.startDate) {
      params.beginInTime = `${recordsQuery.startDate} 00:00:00`
    }
    if (recordsQuery.endDate) {
      params.endInTime = `${recordsQuery.endDate} 23:59:59`
    }
    const res = await ParkingChargeApi.getRecordPage(params)
    list.value = res.list || []
    total.value = res.total || 0
  } finally {
    recordsLoading.value = false
  }
}

const queryRecords = () => {
  recordsQuery.pageNo = 1
  fetchRecords()
}

const resetRecordsQuery = () => {
  recordsQuery.pageNo = 1
  recordsQuery.pageSize = 10
  recordsQuery.plateNo = ''
  const today = dayjs().format('YYYY-MM-DD')
  recordsQuery.startDate = today
  recordsQuery.endDate = today
  fetchRecords()
}

const exportRecordsExcel = () => {
  const rows = list.value
  if (!rows.length) {
    ElMessage.warning('暂无可导出的数据')
    return
  }
  const headerHtml =
    '<tr>' +
    [
      '序号',
      '车牌号',
      '入场时间',
      '出场时间',
      '停车时长(小时)',
      '车辆类型',
      '应缴费用(元)',
      '实缴费用(元)',
      '缴费时间',
      '操作人'
    ]
      .map((h) => `<th style="border:1px solid #ddd;padding:8px;background:#f5f5f5;">${h}</th>`)
      .join('') +
    '</tr>'
  const bodyHtml = rows
    .map((r, idx) => {
      const durationHours = r.durationMinutes ? (r.durationMinutes / 60).toFixed(1) : ''
      const cols = [
        String(idx + 1),
        r.plateNo,
        r.inTime,
        r.outTime || '',
        durationHours,
        '-', // 车辆类型与计费记录弱关联，这里简单占位
        (r.feePaid || 0).toFixed(2),
        (r.feePaid || 0).toFixed(2),
        r.payTime || '',
        r.operator || ''
      ]
      return (
        '<tr>' +
        cols.map((c) => `<td style="border:1px solid #ddd;padding:8px;">${String(c)}</td>`).join('') +
        '</tr>'
      )
    })
    .join('')
  const html = `<!doctype html><html><head><meta charset="UTF-8"></head><body><table>${headerHtml}${bodyHtml}</table></body></html>`
  const blob = new Blob([html], { type: 'application/vnd.ms-excel;charset=utf-8;' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `缴费记录_${dayjs().format('YYYYMMDD_HHmmss')}.xls`
  a.click()
  window.URL.revokeObjectURL(url)
}

const loadFeeSettings = async () => {
  const rules = await ParkingChargeApi.getRules(DEFAULT_LOT_ID)
  if (rules && rules.length) {
    rules.forEach((rule: ParkingChargeRuleVO) => {
      const vt = rule.vehicleType as FeeVehicleType
      if (feeSettings[vt]) {
        feeSettings[vt].firstHour = Number(rule.firstHourFee || 0)
        feeSettings[vt].nextHour = Number(rule.extraHourFee || 0)
        feeSettings[vt].dailyCap = Number(rule.dailyCap || 0)
      }
    })
  }
}

const saveFeeSettings = async () => {
  await ElMessageBox.confirm('确认保存收费标准吗？', '提示')
  const data: ParkingChargeRuleVO[] = (['small', 'medium', 'large'] as FeeVehicleType[]).map((key) => {
    const setting = feeSettings[key]
    return {
      lotId: DEFAULT_LOT_ID,
      vehicleType: key,
      firstHourFee: setting.firstHour,
      extraHourFee: setting.nextHour,
      dailyCap: setting.dailyCap,
      enabled: 1
    }
  })
  await ParkingChargeApi.saveRules(data)
  ElMessage.success('收费标准保存成功！')
}

const confirmPayment = async () => {
  calcFee()
  if (!calcResultVisible.value) return
  const amount = calcExpectedAmount.value
  await ElMessageBox.confirm(`确认缴费金额 ¥ ${amount.toFixed(2)} 吗？`, '提示')
  const now = dayjs()
  const req: ParkingRecordManualPayReqVO = {
    lotId: DEFAULT_LOT_ID,
    plateNo: calcForm.plateNo.trim(),
    vehicleType: 'temp',
    inTime: calcForm.inTime,
    outTime: calcForm.outTime,
    durationMinutes: calcDurationMinutes.value,
    amount,
    discountType: calcForm.discountType || undefined,
    discountRate: discountFactor.value,
    payChannel: 'cash'
  }
  await ParkingChargeApi.manualPay(req)
  ElMessage.success('缴费成功，已记录')
  const today = now.format('YYYY-MM-DD')
  recordsQuery.startDate = recordsQuery.startDate || today
  recordsQuery.endDate = recordsQuery.endDate || today
  queryRecords()
}

onMounted(() => {
  const now = dayjs()
  calcForm.inTime = now.subtract(2, 'hour').format('YYYY-MM-DD HH:mm:ss')
  calcForm.outTime = now.format('YYYY-MM-DD HH:mm:ss')
  const today = now.format('YYYY-MM-DD')
  recordsQuery.startDate = today
  recordsQuery.endDate = today
  loadFeeSettings()
})
</script>

<template>
  <div class="parking-page parking-proto">
    <ContentWrap>
    <el-card shadow="hover" class="mb-4">
      <template #header>
        <div class="font-bold">停车费用计算</div>
      </template>
      <el-form :model="calcForm" label-width="90px" @submit.prevent>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="车牌号">
              <el-input v-model="calcForm.plateNo" placeholder="请输入车牌号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="入场时间">
              <el-date-picker
                v-model="calcForm.inTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="选择入场时间"
                class="!w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="出场时间">
              <el-date-picker
                v-model="calcForm.outTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="选择出场时间"
                class="!w-full"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="车辆类型">
              <el-select v-model="calcForm.vehicleType" class="!w-full">
                <el-option label="小型车 (首小时5元，后续每小时2元)" value="small" />
                <el-option label="中型车 (首小时8元，后续每小时3元)" value="medium" />
                <el-option label="大型车 (首小时10元，后续每小时4元)" value="large" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="优惠折扣">
              <div class="flex items-center gap-3 w-full">
                <el-select v-model="calcForm.discountType" class="!w-260px">
                  <el-option label="无折扣" value="" />
                  <el-option label="VIP会员 (9折)" value="vip" />
                  <el-option label="节假日优惠 (8折)" value="holiday" />
                  <el-option label="自定义折扣" value="custom" />
                </el-select>
                <el-input-number
                  v-if="calcForm.discountType === 'custom'"
                  v-model="calcForm.customDiscount"
                  :min="0.1"
                  :max="1"
                  :step="0.01"
                  placeholder="输入折扣系数"
                  class="!w-180px"
                />
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <div class="flex items-center gap-2">
          <el-button @click="calcFee">计算费用</el-button>
          <el-button type="primary" @click="confirmPayment">确认缴费</el-button>
        </div>
        <div
          v-show="calcResultVisible"
          class="mt-4 p-4 rounded"
          style="background: var(--el-fill-color-light); border: 1px solid var(--el-border-color-light)"
        >
          <div class="text-sm text-[var(--el-text-color-secondary)]">费用计算结果：</div>
          <div class="text-3xl font-bold text-[var(--el-color-primary)] mt-2">¥ {{ calcExpectedAmount.toFixed(2) }}</div>
        </div>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="mb-4">
      <template #header>
        <div class="font-bold">收费标准设置</div>
      </template>
      <el-form label-width="220px" @submit.prevent>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="小型车首小时费用 (元)">
              <el-input-number v-model="feeSettings.small.firstHour" :min="0" :step="0.5" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="小型车后续每小时费用 (元)">
              <el-input-number v-model="feeSettings.small.nextHour" :min="0" :step="0.5" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="小型车每日封顶费用 (元)">
              <el-input-number v-model="feeSettings.small.dailyCap" :min="0" class="!w-full" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="中型车首小时费用 (元)">
              <el-input-number v-model="feeSettings.medium.firstHour" :min="0" :step="0.5" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="中型车后续每小时费用 (元)">
              <el-input-number v-model="feeSettings.medium.nextHour" :min="0" :step="0.5" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="中型车每日封顶费用 (元)">
              <el-input-number v-model="feeSettings.medium.dailyCap" :min="0" class="!w-full" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="大型车首小时费用 (元)">
              <el-input-number v-model="feeSettings.large.firstHour" :min="0" :step="0.5" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="大型车后续每小时费用 (元)">
              <el-input-number v-model="feeSettings.large.nextHour" :min="0" :step="0.5" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="大型车每日封顶费用 (元)">
              <el-input-number v-model="feeSettings.large.dailyCap" :min="0" class="!w-full" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-button type="primary" @click="saveFeeSettings">保存收费标准</el-button>
      </el-form>
    </el-card>

    <el-card shadow="hover">
      <template #header>
        <div class="font-bold">缴费记录查询</div>
      </template>
      <el-form class="-mb-15px" :inline="true" label-width="150px" @submit.prevent>
        <el-form-item label="查询日期范围 - 开始">
          <el-date-picker
            v-model="recordsQuery.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择开始日期"
            class="!w-180px"
          />
        </el-form-item>
        <el-form-item label="查询日期范围 - 结束">
          <el-date-picker
            v-model="recordsQuery.endDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择结束日期"
            class="!w-180px"
          />
        </el-form-item>
        <el-form-item label="车牌号 (选填)">
          <el-input
            v-model="recordsQuery.plateNo"
            placeholder="请输入车牌号"
            clearable
            class="!w-180px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="queryRecords">查询记录</el-button>
          <el-button type="success" plain @click="exportRecordsExcel">导出Excel报表</el-button>
        </el-form-item>
      </el-form>

      <el-table
        v-loading="recordsLoading"
        :data="list"
        border
        :stripe="true"
        empty-text="暂无缴费记录"
        class="mt-4"
      >
        <el-table-column label="序号" type="index" align="center" width="70" />
        <el-table-column label="车牌号" prop="plateNo" align="center" width="140" />
        <el-table-column label="入场时间" prop="inTime" align="center" width="170" />
        <el-table-column label="出场时间" prop="outTime" align="center" width="170">
          <template #default="{ row }">
            {{ row.outTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="停车时长(小时)" align="center" width="120">
          <template #default="{ row }">
            {{ row.durationMinutes ? (row.durationMinutes / 60).toFixed(1) : '' }}
          </template>
        </el-table-column>
        <el-table-column label="车辆类型" align="center" width="150">
          <template #default="{ row }">{{ vehicleTypeLabel(row.vehicleType) }}</template>
        </el-table-column>
        <el-table-column label="应缴费用(元)" align="right" width="120">
          <template #default="{ row }">{{ (row.feePaid || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="实缴费用(元)" align="right" width="120">
          <template #default="{ row }">{{ (row.feePaid || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="缴费时间" prop="payTime" align="center" width="170" />
        <el-table-column label="操作人" prop="operator" align="center" width="120" />
      </el-table>

      <Pagination
        :total="total"
        v-model:page="recordsQuery.pageNo"
        v-model:limit="recordsQuery.pageSize"
        @pagination="fetchRecords"
      />
    </el-card>
    </ContentWrap>
  </div>
</template>

<style scoped lang="scss">
@use './prototype.scss' as *;

.parking-page {
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
  box-sizing: border-box;
}
</style>

