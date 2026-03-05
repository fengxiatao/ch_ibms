<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { getParkingDashboardOverview, type ParkingDashboardOverviewRespVO } from '@/api/access/parkingDashboard'
import {
  ParkingRecordApi,
  type ParkingRecordPageReqVO,
  type ParkingRecordRespVO,
  type ParkingRecordUpdateReqVO
} from '@/api/access/parkingRecord'

defineOptions({ name: 'ParkingRecords' })

type ParkingVehicleType = 'temp' | 'monthly' | 'inner' | 'visitor' | 'noplate'
type ParkingIoType = 'in' | 'out'
type ParkingPayStatus = 'paid' | 'unpaid' | 'free' | 'exception'
type ParkingPayType = 'wechat' | 'alipay' | 'cash' | 'free'

interface ParkingRecordRow extends ParkingRecordRespVO {
  feeAmount: number
  payType?: ParkingPayType
  inPhotoUrl?: string
  outPhotoUrl?: string
  tradeNo?: string
}

const overviewLoading = ref(false)
const overview = ref<ParkingDashboardOverviewRespVO>()
const loading = ref(false)
const list = ref<ParkingRecordRow[]>([])
const total = ref(0)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  plateNo: '',
  vehicleType: '' as '' | ParkingVehicleType,
  ioType: '' as '' | ParkingIoType,
  payStatus: '' as '' | ParkingPayStatus,
  startTime: '',
  endTime: '',
  payType: '' as '' | ParkingPayType,
  durationBucket: '' as '' | '0-60' | '60-240' | '240-720' | '720+',
  feeBucket: '' as '' | '0-10' | '10-50' | '50-100' | '100+',
  operator: '',
  remarkKeyword: '',
  abnormal: '' as '' | 'true' | 'false'
})

const advancedVisible = ref(false)

const detailVisible = ref(false)
const detailRow = ref<ParkingRecordRow>()

const photoPreviewVisible = ref(false)
const photoPreviewUrl = ref('')

const vehicleTypeLabel = (v: ParkingVehicleType) => {
  if (v === 'temp') return '临时车'
  if (v === 'monthly') return '月卡车'
  if (v === 'inner') return '内部车'
  if (v === 'visitor') return '访客车'
  return '无牌车'
}

const payStatusLabel = (s: any) => {
  if (s === 1 || s === 'paid') return '已支付'
  if (s === 0 || s === 'unpaid') return '未支付'
  if (s === 'free') return '免费'
  return '异常'
}

const payStatusTagType = (s: any) => {
  if (s === 1 || s === 'paid') return 'success'
  if (s === 0 || s === 'unpaid') return 'warning'
  if (s === 'free') return 'info'
  return 'danger'
}

const applyFilter = (rows: ParkingRecordRow[]) => {
  const plateNo = queryParams.plateNo.trim()
  const operator = queryParams.operator.trim()
  const remarkKeyword = queryParams.remarkKeyword.trim()
  const abnormal =
    queryParams.abnormal === '' ? undefined : queryParams.abnormal === 'true'
  const begin = queryParams.startTime ? dayjs(queryParams.startTime) : undefined
  const end = queryParams.endTime ? dayjs(queryParams.endTime) : undefined
  const durationBucket = queryParams.durationBucket
  const feeBucket = queryParams.feeBucket

  return rows.filter((r) => {
    if (plateNo && !r.plateNo.includes(plateNo)) return false
    if (queryParams.vehicleType && r.vehicleType !== queryParams.vehicleType) return false
    if (queryParams.ioType && r.ioType !== queryParams.ioType) return false
    if (queryParams.payStatus) {
      if (queryParams.payStatus === 'paid' && !(r.payStatus === 1)) return false
      if (queryParams.payStatus === 'unpaid' && !(r.payStatus === 0)) return false
    }
    if (queryParams.payType && (r.payType || '') !== queryParams.payType) return false
    if (operator && !(r.operator || '').includes(operator)) return false
    if (remarkKeyword && !(r.remark || '').includes(remarkKeyword)) return false
    if (abnormal !== undefined && r.abnormal !== abnormal) return false
    if (begin && dayjs(r.inTime).isBefore(begin)) return false
    if (end && dayjs(r.inTime).isAfter(end)) return false
    if (durationBucket) {
      const m = r.durationMinutes
      if (durationBucket === '0-60' && !(m >= 0 && m < 60)) return false
      if (durationBucket === '60-240' && !(m >= 60 && m < 240)) return false
      if (durationBucket === '240-720' && !(m >= 240 && m < 720)) return false
      if (durationBucket === '720+' && !(m >= 720)) return false
    }
    if (feeBucket) {
      const f = r.feeAmount
      if (feeBucket === '0-10' && !(f >= 0 && f < 10)) return false
      if (feeBucket === '10-50' && !(f >= 10 && f < 50)) return false
      if (feeBucket === '50-100' && !(f >= 50 && f < 100)) return false
      if (feeBucket === '100+' && !(f >= 100)) return false
    }
    return true
  })
}

const handleQuery = async () => {
  loading.value = true
  try {
    const params: ParkingRecordPageReqVO = {
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      plateNo: queryParams.plateNo.trim() || undefined,
      vehicleType: queryParams.vehicleType || undefined,
      payStatus:
        queryParams.payStatus === ''
          ? undefined
          : queryParams.payStatus === 'paid'
            ? 1
            : queryParams.payStatus === 'unpaid'
              ? 0
              : undefined,
      beginInTime: queryParams.startTime || undefined,
      endInTime: queryParams.endTime || undefined
    }
    const res = await ParkingRecordApi.getPage(params)
    const rawRows = (res.list || []) as ParkingRecordRespVO[]
    const mapped: ParkingRecordRow[] = rawRows.map((r) => ({
      ...r,
      feeAmount: r.feePaid || 0,
      payType: (r.payChannel as ParkingPayType) || undefined
    }))
    const filtered = applyFilter(mapped)
    total.value = filtered.length
    list.value = filtered
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryParams.pageNo = 1
  queryParams.pageSize = 10
  queryParams.plateNo = ''
  queryParams.vehicleType = ''
  queryParams.ioType = ''
  queryParams.payStatus = ''
  queryParams.startTime = ''
  queryParams.endTime = ''
  queryParams.payType = ''
  queryParams.durationBucket = ''
  queryParams.feeBucket = ''
  queryParams.operator = ''
  queryParams.remarkKeyword = ''
  queryParams.abnormal = ''
  advancedVisible.value = false
  handleQuery()
}

const openDetail = (row: ParkingRecordRow) => {
  detailRow.value = row
  detailVisible.value = true
}

const editVisible = ref(false)
const editForm = reactive({
  id: 0,
  plateNo: '',
  inTime: '',
  outTime: '',
  feeAmount: 0,
  payStatus: 'paid' as ParkingPayStatus,
  payType: 'wechat' as ParkingPayType,
  remark: ''
})

const openEdit = (row: ParkingRecordRow) => {
  editForm.id = row.id
  editForm.plateNo = row.plateNo
  editForm.inTime = row.inTime
  editForm.outTime = row.outTime || ''
  editForm.feeAmount = row.feeAmount
  editForm.payStatus = row.payStatus
  editForm.payType = row.payType || 'wechat'
  editForm.remark = row.remark || ''
  editVisible.value = true
}

const saveEdit = () => {
  if (!editForm.plateNo.trim()) {
    ElMessage.warning('请输入车牌号')
    return
  }
  const req: ParkingRecordUpdateReqVO = {
    id: editForm.id,
    inTime: editForm.inTime || undefined,
    outTime: editForm.outTime || undefined,
    feePaid: Number(editForm.feeAmount || 0),
    payStatus:
      editForm.payStatus === 'paid'
        ? 1
        : editForm.payStatus === 'unpaid'
          ? 0
          : undefined,
    payChannel: editForm.payType,
    remark: editForm.remark || undefined
  }
  ParkingRecordApi.update(req).then(() => {
    ElMessage.success('编辑成功')
    editVisible.value = false
    handleQuery()
  })
}

const durationText = (minutes: number) => {
  if (minutes < 60) return `${minutes} 分钟`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return `${h} 小时 ${m} 分钟`
}

const openPhotoPreview = (url?: string) => {
  if (!url) return
  photoPreviewUrl.value = url
  photoPreviewVisible.value = true
}

const downloadPhoto = () => {
  const url = photoPreviewUrl.value
  if (!url) return
  const a = document.createElement('a')
  a.href = url
  a.download = `停车照片_${dayjs().format('YYYYMMDD_HHmmss')}.jpg`
  a.rel = 'noopener'
  a.target = '_blank'
  a.click()
}

const refreshData = () => {
  handleQuery()
}

const exportRow = (row: ParkingRecordRow) => {
  const headers = [
    '车牌号码',
    '车辆类型',
    '入场照片',
    '出场照片',
    '入场时间',
    '出场时间',
    '停车时长(分钟)',
    '出入口',
    '收费金额(元)',
    '支付状态',
    '支付方式',
    '支付时间',
    '操作员',
    '备注'
  ]
  const lines = [
    headers.join(','),
    [
      row.plateNo,
      vehicleTypeLabel(row.vehicleType),
      row.inPhotoUrl || '',
      row.outPhotoUrl || '',
      row.inTime,
      row.outTime || '',
      String(row.durationMinutes),
      row.gateName,
      row.feeAmount.toFixed(2),
      payStatusLabel(row.payStatus),
      row.payType || '',
      row.payTime || '',
      row.operator || '',
      (row.remark || '').replaceAll(',', ' ')
    ].join(',')
  ]
  const blob = new Blob([`\uFEFF${lines.join('\n')}`], { type: 'text/csv;charset=utf-8;' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `出入场记录_${row.plateNo || '单条'}_${dayjs().format('YYYYMMDD_HHmmss')}.csv`
  a.click()
  window.URL.revokeObjectURL(url)
}

const feeBreakdown = computed(() => {
  const row = detailRow.value
  if (!row) return []
  const hours = Math.max(1, Math.ceil(row.durationMinutes / 60))
  const unit = 5
  const first = Math.min(1, hours)
  const rest = Math.max(0, hours - 1)
  return [
    { item: '首小时', duration: `${first} 小时`, unitPrice: unit, amount: Number((first * unit).toFixed(2)) },
    { item: '后续计费', duration: `${rest} 小时`, unitPrice: unit, amount: Number((rest * unit).toFixed(2)) }
  ]
})

const exportCsv = () => {
  const filtered = applyFilter(list.value)
  if (!filtered.length) {
    ElMessage.warning('暂无可导出的数据')
    return
  }
  const headers = [
    '车牌号码',
    '车辆类型',
    '出入类型',
    '入场时间',
    '出场时间',
    '停车时长(分钟)',
    '出入口',
    '收费金额(元)',
    '支付状态',
    '操作员',
    '备注'
  ]
  const lines = [
    headers.join(','),
    ...filtered.map((r) =>
      [
        r.plateNo,
        vehicleTypeLabel(r.vehicleType),
        r.ioType === 'in' ? '入场' : '出场',
        r.inTime,
        r.outTime || '',
        String(r.durationMinutes),
        r.gateName,
        r.feeAmount.toFixed(2),
        payStatusLabel(r.payStatus),
        r.operator || '',
        (r.remark || '').replaceAll(',', ' ')
      ].join(',')
    )
  ]
  const blob = new Blob([`\uFEFF${lines.join('\n')}`], { type: 'text/csv;charset=utf-8;' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `出入场记录_${dayjs().format('YYYYMMDD_HHmmss')}.csv`
  a.click()
  window.URL.revokeObjectURL(url)
}

const buildDetailHtml = (row: ParkingRecordRow) => {
  const escape = (v: string) =>
    String(v || '')
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#39;')

  const cells = [
    ['车牌号码', row.plateNo],
    ['车辆类型', vehicleTypeLabel(row.vehicleType)],
    ['出入类型', row.ioType === 'in' ? '入场' : '出场'],
    ['入场时间', row.inTime],
    ['出场时间', row.outTime || '-'],
    ['停车时长', durationText(row.durationMinutes)],
    ['出入口', row.gateName],
    ['收费金额(元)', row.feeAmount.toFixed(2)],
    ['支付状态', payStatusLabel(row.payStatus)],
    ['支付方式', row.payType || '-'],
    ['支付时间', row.payTime || '-'],
    ['操作员', row.operator || '-'],
    ['备注', row.remark || '无']
  ]
  const rowsHtml = cells
    .map(
      ([k, v]) =>
        `<tr><td style="padding:8px;border:1px solid #ccc;background:#f7f7f7;width:140px;">${escape(k)}</td><td style="padding:8px;border:1px solid #ccc;">${escape(v)}</td></tr>`
    )
    .join('')

  const img = (label: string, url?: string) =>
    `<div style="flex:1;border:1px solid #ccc;padding:10px;">
      <div style="font-weight:700;margin-bottom:8px;">${escape(label)}</div>
      ${
        url
          ? `<img src="${escape(url)}" style="width:100%;max-height:260px;object-fit:cover;border-radius:6px;" />`
          : `<div style="color:#666;">暂无图片</div>`
      }
    </div>`

  return `<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width,initial-scale=1" />
  <title>出入场记录详情</title>
</head>
<body style="font-family:Microsoft YaHei, Arial; padding:16px; color:#111;">
  <h2 style="margin:0 0 12px;">出入场记录详情</h2>
  <table style="border-collapse:collapse;width:100%;margin-bottom:16px;">${rowsHtml}</table>
  <div style="display:flex;gap:12px;">${img('入场照片', row.inPhotoUrl)}${img('出场照片', row.outPhotoUrl)}</div>
</body>
</html>`
}

const printDetail = () => {
  const row = detailRow.value
  if (!row) return
  const w = window.open('', '_blank', 'noopener,noreferrer')
  if (!w) return
  w.document.open()
  w.document.write(buildDetailHtml(row))
  w.document.close()
  w.focus()
  setTimeout(() => w.print(), 200)
}

const exportDetailPdf = () => {
  printDetail()
  ElMessage.info('请在打印对话框选择“另存为 PDF”')
}

const fetchOverview = async () => {
  overviewLoading.value = true
  try {
    overview.value = await getParkingDashboardOverview()
  } catch (error) {
    overview.value = {
      totalSpaces: 500,
      usedSpaces: 312,
      freeSpaces: 188,
      tempVehicleCount: 260,
      monthlyVehicleCount: 52,
      usageRate: 0.624,
      todayInCount: 128,
      todayOutCount: 124,
      todayIncome: 1250,
      alertCount: 6
    }
  } finally {
    overviewLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchOverview(), handleQuery()])
})
</script>

<template>
  <div class="parking-page parking-proto">
    <ContentWrap>
    <el-row :gutter="16" class="mb-4" v-loading="overviewLoading">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="text-sm text-[var(--el-text-color-secondary)]">今日入场总数</div>
          <div class="text-2xl font-bold">{{ overview?.todayInCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="text-sm text-[var(--el-text-color-secondary)]">今日出场总数</div>
          <div class="text-2xl font-bold">{{ overview?.todayOutCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="text-sm text-[var(--el-text-color-secondary)]">今日收费笔数</div>
          <div class="text-2xl font-bold">{{ Math.round((overview?.todayOutCount ?? 0) * 0.85) }}</div>
          <div class="text-xs text-[var(--el-text-color-secondary)] mt-1">收费率：85%</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="text-sm text-[var(--el-text-color-secondary)]">今日异常记录</div>
          <div class="text-2xl font-bold">{{ overview?.alertCount ?? 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <ContentWrap>
      <el-form class="-mb-15px" :inline="true" label-position="top" @submit.prevent>
        <el-form-item label="车牌号码">
          <el-input v-model="queryParams.plateNo" placeholder="请输入车牌号" clearable class="!w-180px" />
        </el-form-item>
        <el-form-item label="车辆类型">
          <el-select v-model="queryParams.vehicleType" placeholder="全部" clearable class="!w-140px">
            <el-option label="临时车" value="temp" />
            <el-option label="月卡车" value="monthly" />
            <el-option label="内部车" value="inner" />
            <el-option label="访客车" value="visitor" />
            <el-option label="无牌车" value="noplate" />
          </el-select>
        </el-form-item>
        <el-form-item label="出入类型">
          <el-select v-model="queryParams.ioType" placeholder="全部" clearable class="!w-120px">
            <el-option label="入场" value="in" />
            <el-option label="出场" value="out" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="queryParams.payStatus" placeholder="全部" clearable class="!w-120px">
            <el-option label="已支付" value="paid" />
            <el-option label="未支付" value="unpaid" />
            <el-option label="免费" value="free" />
            <el-option label="异常" value="exception" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="queryParams.startTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择开始时间"
            class="!w-200px"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="queryParams.endTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择结束时间"
            class="!w-200px"
          />
        </el-form-item>
        <el-form-item v-if="advancedVisible" label="收费方式">
          <el-select v-model="queryParams.payType" placeholder="全部" clearable class="!w-120px">
            <el-option label="微信" value="wechat" />
            <el-option label="支付宝" value="alipay" />
            <el-option label="现金" value="cash" />
            <el-option label="免费" value="free" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="advancedVisible" label="停车时长">
          <el-select v-model="queryParams.durationBucket" placeholder="全部" clearable class="!w-150px">
            <el-option label="1小时内" value="0-60" />
            <el-option label="1-4小时" value="60-240" />
            <el-option label="4-12小时" value="240-720" />
            <el-option label="12小时以上" value="720+" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="advancedVisible" label="收费金额">
          <el-select v-model="queryParams.feeBucket" placeholder="全部" clearable class="!w-150px">
            <el-option label="0-10元" value="0-10" />
            <el-option label="10-50元" value="10-50" />
            <el-option label="50-100元" value="50-100" />
            <el-option label="100元以上" value="100+" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="advancedVisible" label="操作员">
          <el-input v-model="queryParams.operator" placeholder="请输入操作员姓名" clearable class="!w-160px" />
        </el-form-item>
        <el-form-item v-if="advancedVisible" label="备注关键词">
          <el-input v-model="queryParams.remarkKeyword" placeholder="请输入备注关键词" clearable class="!w-160px" />
        </el-form-item>
        <el-form-item v-if="advancedVisible" label="是否异常">
          <el-select v-model="queryParams.abnormal" placeholder="全部" clearable class="!w-120px">
            <el-option label="是" value="true" />
            <el-option label="否" value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
          <el-button @click="advancedVisible = !advancedVisible">
            <Icon :icon="advancedVisible ? 'ep:arrow-up' : 'ep:arrow-down'" class="mr-5px" />
            高级搜索
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <div class="flex items-center justify-between mb-3">
        <div class="font-bold">出入场记录列表</div>
        <div class="flex items-center gap-2">
          <el-button type="success" plain @click="exportCsv">
            <Icon icon="ep:download" class="mr-5px" />
            导出记录
          </el-button>
          <el-button type="primary" plain @click="refreshData">
            <Icon icon="ep:refresh" class="mr-5px" />
            刷新数据
          </el-button>
          <el-button type="warning" plain @click="ElMessage.info('导入能力未接入后端，当前仅展示原型入口')">
            <Icon icon="ep:upload" class="mr-5px" />
            导入记录
          </el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
        <el-table-column label="序号" type="index" align="center" width="70" />
        <el-table-column label="车牌号码" prop="plateNo" align="center" width="140" />
        <el-table-column label="车辆类型" align="center" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ vehicleTypeLabel(row.vehicleType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="入场照片" align="center" width="90">
          <template #default="{ row }">
            <el-image
              v-if="row.inPhotoUrl"
              :src="row.inPhotoUrl"
              :preview-src-list="[row.inPhotoUrl]"
              fit="cover"
              class="w-12 h-12 rounded"
              @click="openPhotoPreview(row.inPhotoUrl)"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="出场照片" align="center" width="90">
          <template #default="{ row }">
            <el-image
              v-if="row.outPhotoUrl"
              :src="row.outPhotoUrl"
              :preview-src-list="[row.outPhotoUrl]"
              fit="cover"
              class="w-12 h-12 rounded"
              @click="openPhotoPreview(row.outPhotoUrl)"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="入场时间" prop="inTime" align="center" width="170" />
        <el-table-column label="出场时间" prop="outTime" align="center" width="170">
          <template #default="{ row }">
            {{ row.outTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="停车时长" align="center" width="120">
          <template #default="{ row }">
            {{ durationText(row.durationMinutes) }}
          </template>
        </el-table-column>
        <el-table-column label="出入口" prop="gateName" align="center" min-width="120" />
        <el-table-column label="收费金额(元)" align="center" width="120">
          <template #default="{ row }">
            {{ row.feeAmount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="支付状态" align="center" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="payStatusTagType(row.payStatus)">
              {{ payStatusLabel(row.payStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link type="warning" @click="openEdit(row)">编辑</el-button>
            <el-button link type="success" @click="exportRow(row)">导出</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="handleQuery"
      />
    </ContentWrap>
  </ContentWrap>

  <Dialog title="出入场记录详情" v-model="detailVisible" width="980px" :appendToBody="true">
    <div v-if="detailRow">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="车牌号码">{{ detailRow.plateNo }}</el-descriptions-item>
        <el-descriptions-item label="车辆类型">{{ vehicleTypeLabel(detailRow.vehicleType) }}</el-descriptions-item>
        <el-descriptions-item label="出入类型">{{ detailRow.ioType === 'in' ? '入场' : '出场' }}</el-descriptions-item>
        <el-descriptions-item label="入场时间">{{ detailRow.inTime }}</el-descriptions-item>
        <el-descriptions-item label="出场时间">{{ detailRow.outTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="停车时长">{{ durationText(detailRow.durationMinutes) }}</el-descriptions-item>
        <el-descriptions-item label="出入口">{{ detailRow.gateName }}</el-descriptions-item>
        <el-descriptions-item label="收费金额(元)">{{ detailRow.feeAmount.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">{{ payStatusLabel(detailRow.payStatus) }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ detailRow.payType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ detailRow.payTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作员">{{ detailRow.operator || '-' }}</el-descriptions-item>
        <el-descriptions-item label="交易单号" :span="3">{{ detailRow.tradeNo || '-' }}</el-descriptions-item>
      </el-descriptions>

      <ContentWrap>
        <div class="font-bold mb-2">费用明细</div>
        <el-table :data="feeBreakdown" border>
          <el-table-column label="计费项" prop="item" />
          <el-table-column label="时长" prop="duration" align="center" width="120" />
          <el-table-column label="单价(元)" prop="unitPrice" align="right" width="120" />
          <el-table-column label="金额(元)" prop="amount" align="right" width="120" />
        </el-table>
      </ContentWrap>

      <el-row :gutter="16" class="mt-4">
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>入场照片</template>
            <el-image
              v-if="detailRow.inPhotoUrl"
              :src="detailRow.inPhotoUrl"
              :preview-src-list="[detailRow.inPhotoUrl]"
              fit="cover"
              class="w-full h-44 rounded"
              @click="openPhotoPreview(detailRow.inPhotoUrl)"
            />
            <div v-else class="text-[var(--el-text-color-secondary)]">暂无图片</div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>出场照片</template>
            <el-image
              v-if="detailRow.outPhotoUrl"
              :src="detailRow.outPhotoUrl"
              :preview-src-list="[detailRow.outPhotoUrl]"
              fit="cover"
              class="w-full h-44 rounded"
              @click="openPhotoPreview(detailRow.outPhotoUrl)"
            />
            <div v-else class="text-[var(--el-text-color-secondary)]">暂无图片</div>
          </el-card>
        </el-col>
      </el-row>

      <ContentWrap>
        <div class="font-bold mb-2">备注</div>
        <div class="text-[var(--el-text-color-secondary)]">{{ detailRow.remark || '无' }}</div>
      </ContentWrap>

      <div class="flex justify-end gap-2 mt-4">
        <el-button type="primary" plain @click="printDetail">
          <Icon icon="ep:printer" class="mr-5px" />
          打印详情
        </el-button>
        <el-button type="success" plain @click="exportDetailPdf">
          <Icon icon="ep:document" class="mr-5px" />
          导出PDF
        </el-button>
        <el-button @click="detailVisible = false">关闭</el-button>
      </div>
    </div>
  </Dialog>

  <Dialog title="编辑出入场记录" v-model="editVisible" width="720px" :appendToBody="true">
    <el-form label-width="90px" @submit.prevent>
      <el-form-item label="车牌号码">
        <el-input v-model="editForm.plateNo" disabled />
      </el-form-item>
      <el-form-item label="入场时间">
        <el-date-picker
          v-model="editForm.inTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="选择入场时间"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="出场时间">
        <el-date-picker
          v-model="editForm.outTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="选择出场时间"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="收费金额">
        <el-input-number v-model="editForm.feeAmount" :min="0" :max="999999" :step="0.1" class="!w-full" />
      </el-form-item>
      <el-form-item label="支付状态">
        <el-select v-model="editForm.payStatus" class="!w-full">
          <el-option label="已支付" value="paid" />
          <el-option label="未支付" value="unpaid" />
          <el-option label="免费" value="free" />
          <el-option label="异常" value="exception" />
        </el-select>
      </el-form-item>
      <el-form-item label="支付方式">
        <el-select v-model="editForm.payType" class="!w-full">
          <el-option label="微信" value="wechat" />
          <el-option label="支付宝" value="alipay" />
          <el-option label="现金" value="cash" />
          <el-option label="免费" value="free" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注信息">
        <el-input v-model="editForm.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
      </el-form-item>
      <div class="flex justify-end gap-2">
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </div>
    </el-form>
  </Dialog>

  <Dialog title="照片预览" v-model="photoPreviewVisible" width="820px" :appendToBody="true">
    <div class="flex flex-col_gap-3">
      <el-image v-if="photoPreviewUrl" :src="photoPreviewUrl" fit="contain" class="w-full h-420px" />
      <div class="flex justify-end gap-2">
        <el-button type="primary" plain @click="downloadPhoto">
          <Icon icon="ep:download" class="mr-5px" />
          下载照片
        </el-button>
        <el-button @click="photoPreviewVisible = false">关闭</el-button>
      </div>
    </div>
  </Dialog>
  </div>
</template>

<style scoped lang="scss">
@use './prototype.scss' as *;

.parking-page {
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
  box-sizing: border-box;
}
</style>

