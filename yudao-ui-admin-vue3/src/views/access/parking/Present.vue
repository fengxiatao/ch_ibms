<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AccessDoorApi, type AccessDoorVO } from '@/api/access'
import {
  ParkingPresentApi,
  type ParkingPresentPageReqVO,
  type ParkingPresentRespVO
} from '@/api/access/parkingPresent'
import {
  getParkingDashboardOverview,
  type ParkingDashboardOverviewRespVO
} from '@/api/access/parkingDashboard'

type VehicleType = 'temp' | 'monthly' | 'inner' | 'visitor' | 'noplate'
type WarningType = 'all' | 'space' | 'longtime' | 'noplate' | 'device'
type BatchAction = 'warn' | 'exit' | 'ignore' | 'export'
type WarningHandleStatus = 'pending' | 'processing' | 'done' | 'ignored'
type RemoteGateReason = 'normal' | 'emergency' | 'device' | 'other'

const DEFAULT_PARK_ID = 1

const loading = ref(false)
const overviewLoading = ref(false)
const list = ref<ParkingPresentRespVO[]>([])
const total = ref(0)
const overview = ref<ParkingDashboardOverviewRespVO>()
const lastUpdateTime = ref('')

const doorList = ref<AccessDoorVO[]>([])
const doorOptions = computed(() => {
  return doorList.value
    .filter((d) => typeof d.id === 'number')
    .map((d) => ({ id: d.id as number, name: d.doorName }))
})
const doorNameMap = computed(() => {
  return new Map<number, string>(doorOptions.value.map((d) => [d.id, d.name]))
})

const queryParams = ref<ParkingPresentPageReqVO>({
  pageNo: 1,
  pageSize: 10
})

const filters = reactive({
  plateNo: '',
  vehicleType: 'all' as 'all' | VehicleType,
  warningType: 'all' as WarningType,
  entryLaneId: undefined as number | undefined,
  inTimeRange: [] as [string, string] | []
})

const selection = ref<ParkingPresentRespVO[]>([])

const warningStats = ref<{ space: number; longtime: number; noplate: number; device: number }>()
const dismissedWarningKeys = ref<string[]>([])
const warningDialogVisible = ref(false)

const fetchWarningStats = async () => {
  try {
    const baseParams: any = { pageNo: 1, pageSize: 1, lotId: DEFAULT_PARK_ID }
    const [spaceRes, longtimeRes, noplateRes, deviceRes] = await Promise.all([
      ParkingPresentApi.getPage({ ...baseParams, warningType: 1 }),
      ParkingPresentApi.getPage({ ...baseParams, warningType: 2 }),
      ParkingPresentApi.getPage({ ...baseParams, warningType: 3 }),
      ParkingPresentApi.getPage({ ...baseParams, warningType: 4 })
    ])
    warningStats.value = {
      space: Number(spaceRes?.total || 0),
      longtime: Number(longtimeRes?.total || 0),
      noplate: Number(noplateRes?.total || 0),
      device: Number(deviceRes?.total || 0)
    }
  } catch (error) {
    warningStats.value = undefined
  }
}

const warningBar = computed(() => {
  const v = warningStats.value
  const space = v?.space ?? 0
  const longtime = v?.longtime ?? 0
  const noplate = v?.noplate ?? 0
  const device = v?.device ?? 0
  return [
    {
      key: 'space',
      title: '余位不足预警',
      content: space ? `当前 ${space} 条，请及时引导车辆` : '暂无余位不足预警',
      count: space,
      type: 'warning' as const
    },
    {
      key: 'longtime',
      title: '长时间停车预警',
      content: longtime ? `当前 ${longtime} 条，需及时联系车主` : '暂无长时间停车预警',
      count: longtime,
      type: 'danger' as const
    },
    {
      key: 'noplate',
      title: '无牌车预警',
      content: noplate ? `当前 ${noplate} 条，需人工核实登记` : '暂无无牌车预警',
      count: noplate,
      type: 'danger' as const
    },
    {
      key: 'device',
      title: '设备异常预警',
      content: device ? `当前 ${device} 条，请运维人员检修` : '暂无设备异常预警',
      count: device,
      type: 'info' as const
    }
  ]
})

const warningTickerItems = computed(() => {
  return warningBar.value.filter((w) => !dismissedWarningKeys.value.includes(w.key) && w.count > 0)
})

const currentWarningCount = computed(
  () => list.value.filter((r) => (r.warningType || 0) > 0).length
)
const abnormalCount = computed(() => list.value.filter((r) => r.status === 2).length)
const tempFreeSpaces = computed(() => {
  const free = overview.value?.freeSpaces ?? 0
  return Math.max(0, Math.round(free * 0.6))
})

const overviewCards = computed(() => {
  const v = overview.value
  return [
    { label: '总车位数', value: v?.totalSpaces ?? 0 },
    { label: '在用车位', value: v?.usedSpaces ?? 0 },
    { label: '剩余车位', value: v?.freeSpaces ?? 0 },
    { label: '今日收费总额(元)', value: v?.todayIncome ?? 0 },
    { label: '今日异常记录', value: v?.alertCount ?? abnormalCount.value }
  ]
})

const mapWarningTypeToBackend = (type: WarningType): number | undefined => {
  if (type === 'all') return undefined
  if (type === 'space') return 1
  if (type === 'longtime') return 2
  if (type === 'noplate') return 3
  if (type === 'device') return 4
  return undefined
}

const warningTypeLabel = (backendWarningType?: number) => {
  if (!backendWarningType || backendWarningType === 0) return '无'
  if (backendWarningType === 1) return '余位不足预警'
  if (backendWarningType === 2) return '长时间停车预警'
  if (backendWarningType === 3) return '无牌车预警'
  if (backendWarningType === 4) return '设备离线预警'
  return '预警'
}

const vehicleTypeLabel = (v?: any) => {
  if (v === 'temp') return '临时车'
  if (v === 'monthly') return '月卡车'
  if (v === 'inner') return '内部车'
  if (v === 'visitor') return '访客车'
  if (v === 'noplate') return '无牌车'
  return v || '-'
}

const durationText = (minutes?: number) => {
  const m = Number(minutes || 0)
  if (m <= 0) return '-'
  if (m < 60) return `${m}分钟`
  const h = Math.floor(m / 60)
  const mm = m % 60
  return mm ? `${h}小时${mm}分` : `${h}小时`
}

const getGateName = (entryLaneId?: number) => {
  if (!entryLaneId) return '-'
  return doorNameMap.value.get(entryLaneId) || `#${entryLaneId}`
}

const vehicleStatusLabel = (row: ParkingPresentRespVO) => {
  const warning = (row.warningType || 0) > 0
  if (row.status === 2) return '异常'
  if (warning) return '预警'
  return '正常'
}

const vehicleStatusTagType = (row: ParkingPresentRespVO) => {
  const warning = (row.warningType || 0) > 0
  if (row.status === 2) return 'danger'
  if (warning) return 'warning'
  return 'success'
}

const fetchOverview = async () => {
  overviewLoading.value = true
  try {
    overview.value = await getParkingDashboardOverview()
    lastUpdateTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
  } finally {
    overviewLoading.value = false
  }
}

const fetchDoorList = async () => {
  try {
    doorList.value = await AccessDoorApi.getList(DEFAULT_PARK_ID)
  } catch {
    doorList.value = []
  }
}

const applyFiltersToQueryParams = () => {
  queryParams.value.plateNo = filters.plateNo?.trim() || undefined
  queryParams.value.vehicleType =
    filters.vehicleType === 'all' ? undefined : (filters.vehicleType as any)
  ;(queryParams.value as any).warningType = mapWarningTypeToBackend(filters.warningType)
  ;(queryParams.value as any).entryLaneId = filters.entryLaneId || undefined
  const range = filters.inTimeRange
  queryParams.value.beginInTime = range.length === 2 ? range[0] : undefined
  queryParams.value.endInTime = range.length === 2 ? range[1] : undefined
  delete queryParams.value.status
}

const handleQuery = async () => {
  applyFiltersToQueryParams()
  loading.value = true
  try {
    const res = await ParkingPresentApi.getPage(queryParams.value)
    list.value = res.list
    total.value = res.total
    lastUpdateTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
    fetchWarningStats()
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  queryParams.value.pageNo = 1
  await handleQuery()
}

const resetQuery = async () => {
  filters.plateNo = ''
  filters.vehicleType = 'all'
  filters.warningType = 'all'
  filters.entryLaneId = undefined
  filters.inTimeRange = []
  queryParams.value.pageNo = 1
  queryParams.value.pageSize = 10
  await handleQuery()
}

const handleSelectionChange = (rows: ParkingPresentRespVO[]) => {
  selection.value = rows
}

const closeWarningBarItem = (key: string) => {
  if (!dismissedWarningKeys.value.includes(key)) {
    dismissedWarningKeys.value.push(key)
  }
}

const quickHandleWarning = async (key: string) => {
  if (key === 'space') filters.warningType = 'space'
  else if (key === 'longtime') filters.warningType = 'longtime'
  else if (key === 'noplate') filters.warningType = 'noplate'
  else if (key === 'device') filters.warningType = 'device'
  else filters.warningType = 'all'
  await handleSearch()
}

const warningModalVisible = ref(false)
const warningForm = reactive({
  warnType: '' as WarningType,
  vehicleId: undefined as number | undefined,
  plateNo: '',
  status: 'pending' as WarningHandleStatus,
  handler: '',
  remark: ''
})

const openWarningModal = (row: ParkingPresentRespVO) => {
  warningForm.warnType =
    row.warningType === 1
      ? 'space'
      : row.warningType === 2
        ? 'longtime'
        : row.warningType === 3
          ? 'noplate'
          : row.warningType === 4
            ? 'device'
            : 'all'
  warningForm.vehicleId = row.id
  warningForm.plateNo = row.plateNo || ''
  warningForm.status = 'pending'
  warningForm.handler = ''
  warningForm.remark = ''
  warningModalVisible.value = true
}

const saveWarningHandle = async () => {
  if (!warningForm.handler.trim()) {
    ElMessage.warning('请填写处理人')
    return
  }
  ElMessage.success('预警处理结果已保存')
  warningModalVisible.value = false
}

const detailModalVisible = ref(false)
const detailRow = ref<ParkingPresentRespVO>()

const openDetailModal = (row: ParkingPresentRespVO) => {
  detailRow.value = row
  detailModalVisible.value = true
}

const remoteModalVisible = ref(false)
const remoteForm = reactive({
  plateNo: '',
  gateId: undefined as number | undefined,
  gateName: '',
  reason: 'normal' as RemoteGateReason,
  remark: ''
})

const openRemoteModal = () => {
  remoteForm.plateNo = ''
  remoteForm.gateId = undefined
  remoteForm.gateName = ''
  remoteForm.reason = 'normal'
  remoteForm.remark = ''
  remoteModalVisible.value = true
}

const submitRemoteOpenGate = async () => {
  if (!remoteForm.plateNo.trim()) {
    ElMessage.warning('请输入车牌号')
    return
  }
  if (!remoteForm.gateId && !remoteForm.gateName.trim()) {
    ElMessage.warning('请选择出入口')
    return
  }
  if (!remoteForm.reason) {
    ElMessage.warning('请选择开闸原因')
    return
  }
  ElMessage.success('远程开闸指令已下发')
  remoteModalVisible.value = false
}

const batchModalVisible = ref(false)
const batchForm = reactive({
  action: 'warn' as BatchAction,
  remark: ''
})

const openBatchModal = () => {
  if (!selection.value.length) {
    ElMessage.warning('请先勾选要处理的车辆')
    return
  }
  batchForm.action = 'warn'
  batchForm.remark = ''
  batchModalVisible.value = true
}

const exportSelected = () => {
  const rows = selection.value
  const headers = [
    '车牌号码',
    '车辆类型',
    '进场时间',
    '停车时长',
    '出入口',
    '车位号',
    '应缴费用(元)',
    '车辆状态',
    '预警类型',
    '进场照片'
  ]
  const lines = [
    headers.join(','),
    ...rows.map((r) =>
      [
        r.plateNo,
        vehicleTypeLabel(r.vehicleType),
        r.inTime,
        durationText(r.durationMinutes),
        getGateName(r.entryLaneId),
        r.spaceNo ?? '',
        String(r.expectedFee ?? ''),
        vehicleStatusLabel(r),
        warningTypeLabel(r.warningType),
        r.snapshotUrl ?? ''
      ].join(',')
    )
  ]
  const blob = new Blob([`\uFEFF${lines.join('\n')}`], { type: 'text/csv;charset=utf-8;' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `在场车辆导出.csv`
  a.click()
  window.URL.revokeObjectURL(url)
}

const submitBatch = async () => {
  const ids = selection.value.map((r) => r.id)
  if (!ids.length) {
    ElMessage.warning('请先勾选要处理的车辆')
    return
  }
  if (batchForm.action === 'exit') {
    await ElMessageBox.confirm(`确认批量出场选中的 ${ids.length} 辆车吗？`, '提示')
    await ParkingPresentApi.forceOut(ids)
    ElMessage.success('操作成功')
    batchModalVisible.value = false
    await handleQuery()
    return
  }
  if (batchForm.action === 'export') {
    exportSelected()
    ElMessage.success('已导出')
    batchModalVisible.value = false
    return
  }
  ElMessage.success('批量操作已完成')
  batchModalVisible.value = false
}

const forceOutOne = async (row: ParkingPresentRespVO) => {
  await ElMessageBox.confirm(`确认对车辆 ${row.plateNo || ''} 执行出场吗？`, '提示')
  await ParkingPresentApi.forceOut([row.id])
  ElMessage.success('操作成功')
  await handleQuery()
}

const abnormalModalVisible = ref(false)
const abnormalForm = reactive({
  abnormalType: 'all' as 'all' | 'warn' | 'device' | 'noplate' | 'longtime',
  plateNo: ''
})
const abnormalList = computed(() => {
  const plateNo = abnormalForm.plateNo.trim()
  return list.value.filter((r) => {
    const isAbnormal = r.status === 2 || (r.warningType || 0) > 0
    if (!isAbnormal) return false
    if (plateNo && !r.plateNo?.includes(plateNo)) return false
    if (abnormalForm.abnormalType === 'all') return true
    if (abnormalForm.abnormalType === 'device') return r.warningType === 4
    if (abnormalForm.abnormalType === 'noplate') return r.warningType === 3
    if (abnormalForm.abnormalType === 'longtime') return r.warningType === 2
    if (abnormalForm.abnormalType === 'warn') return (r.warningType || 0) > 0
    return true
  })
})

const openAbnormalModal = () => {
  abnormalForm.abnormalType = 'all'
  abnormalForm.plateNo = ''
  abnormalModalVisible.value = true
}

onMounted(async () => {
  await Promise.all([fetchDoorList(), fetchOverview(), fetchWarningStats(), handleQuery()])
})
</script>

<template>
  <div class="app-container parking-present parking-page parking-proto">
    <div class="parking-present__ticker">
      <div class="parking-present__ticker-label">
        <span class="parking-present__ticker-dot"></span>
        预警提示
      </div>
      <div class="parking-present__ticker-viewport">
        <div v-if="warningTickerItems.length" class="parking-present__ticker-track">
          <div
            v-for="item in warningTickerItems"
            :key="item.key"
            class="parking-present__ticker-item"
          >
            <span class="parking-present__ticker-badge" :class="`is-${item.key}`">{{
              item.title
            }}</span>
            <span class="parking-present__ticker-text">{{ item.content }}</span>
          </div>
          <div
            v-for="item in warningTickerItems"
            :key="`dup-${item.key}`"
            class="parking-present__ticker-item"
          >
            <span class="parking-present__ticker-badge" :class="`is-${item.key}`">{{
              item.title
            }}</span>
            <span class="parking-present__ticker-text">{{ item.content }}</span>
          </div>
        </div>
        <div v-else class="parking-present__ticker-empty">暂无预警消息</div>
      </div>
      <div class="parking-present__ticker-actions">
        <el-button size="small" text type="primary" @click="warningDialogVisible = true"
          >全部</el-button
        >
      </div>
    </div>

    <div class="parking-present__content">
      <div class="flex items-center justify-between mb-3">
        <div class="font-bold">在场车辆监控</div>
        <div class="text-sm text-[var(--el-text-color-secondary)]">
          最后更新时间：{{ lastUpdateTime || '-' }}
        </div>
      </div>

      <el-row :gutter="16" class="mb-4" v-loading="overviewLoading">
        <el-col :span="24">
          <el-row :gutter="16">
            <el-col v-for="item in overviewCards" :key="item.label" :span="4">
              <el-card shadow="hover">
                <div class="text-sm text-[var(--el-text-color-secondary)]">{{ item.label }}</div>
                <div class="text-2xl font-bold">{{ item.value }}</div>
              </el-card>
            </el-col>
            <el-col :span="4">
              <el-card shadow="hover">
                <div class="text-sm text-[var(--el-text-color-secondary)]">临时车位剩余(估算)</div>
                <div class="text-2xl font-bold">{{ tempFreeSpaces }}</div>
              </el-card>
            </el-col>
          </el-row>
        </el-col>
      </el-row>

      <ContentWrap>
        <el-form :inline="true" label-position="top" @submit.prevent>
          <el-form-item label="车牌号码">
            <el-input
              v-model="filters.plateNo"
              placeholder="请输入车牌号（无牌车填：无）"
              clearable
              class="!w-220px"
            />
          </el-form-item>
          <el-form-item label="车辆类型">
            <el-select v-model="filters.vehicleType" class="!w-160px">
              <el-option label="全部类型" value="all" />
              <el-option label="临时车" value="temp" />
              <el-option label="月卡车" value="monthly" />
              <el-option label="内部车" value="inner" />
              <el-option label="访客车" value="visitor" />
              <el-option label="无牌车" value="noplate" />
            </el-select>
          </el-form-item>
          <el-form-item label="预警类型">
            <el-select v-model="filters.warningType" class="!w-180px">
              <el-option label="全部预警" value="all" />
              <el-option label="余位不足预警" value="space" />
              <el-option label="长时间停车预警" value="longtime" />
              <el-option label="无牌车预警" value="noplate" />
              <el-option label="设备离线预警" value="device" />
            </el-select>
          </el-form-item>
          <el-form-item label="出入口">
            <el-select
              v-model="filters.entryLaneId"
              clearable
              class="!w-220px"
              placeholder="请选择出入口"
            >
              <el-option v-for="d in doorOptions" :key="d.id" :label="d.name" :value="d.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="进场时间">
            <el-date-picker
              v-model="filters.inTimeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              class="!w-360px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <Icon icon="ep:search" class="mr-5px" />
              搜索
            </el-button>
            <el-button @click="resetQuery">
              <Icon icon="ep:refresh" class="mr-5px" />
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </ContentWrap>

      <ContentWrap>
        <div class="flex items-center justify-between mb-2">
          <div class="font-bold">
            在场车辆列表
            <el-tag v-if="currentWarningCount > 0" type="warning" class="ml-2">
              当前预警：{{ currentWarningCount }}条
            </el-tag>
          </div>
          <div class="flex gap-2">
            <el-button type="warning" plain @click="openAbnormalModal">异常记录</el-button>
            <el-button type="primary" plain @click="openRemoteModal">远程开闸</el-button>
            <el-button type="danger" @click="openBatchModal">批量处理</el-button>
          </div>
        </div>

        <el-table v-loading="loading" :data="list" border @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" />
          <el-table-column prop="plateNo" label="车牌号码" width="140" />
          <el-table-column label="车辆类型" width="120">
            <template #default="{ row }">
              {{ vehicleTypeLabel(row.vehicleType) }}
            </template>
          </el-table-column>
          <el-table-column prop="inTime" label="进场时间" width="180" />
          <el-table-column label="停车时长" width="120">
            <template #default="{ row }">
              {{ durationText(row.durationMinutes) }}
            </template>
          </el-table-column>
          <el-table-column label="出入口" width="140">
            <template #default="{ row }">
              {{ getGateName(row.entryLaneId) }}
            </template>
          </el-table-column>
          <el-table-column prop="spaceNo" label="车位号" width="120" />
          <el-table-column prop="expectedFee" label="应缴费用(元)" width="120" />
          <el-table-column label="车辆状态" width="120">
            <template #default="{ row }">
              <el-tag :type="vehicleStatusTagType(row)" size="small">{{
                vehicleStatusLabel(row)
              }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="预警类型" width="160">
            <template #default="{ row }">
              <el-tag v-if="(row.warningType || 0) === 0" type="success" size="small">无</el-tag>
              <el-tag v-else type="warning" size="small">{{
                warningTypeLabel(row.warningType)
              }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="进场照片" width="140">
            <template #default="{ row }">
              <el-image
                v-if="row.snapshotUrl"
                :src="row.snapshotUrl"
                fit="cover"
                :preview-src-list="[row.snapshotUrl]"
                preview-teleported
                class="parking-present__img"
              />
              <span v-else class="text-[var(--el-text-color-secondary)]">暂无</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetailModal(row)">详情</el-button>
              <el-button link type="danger" @click="forceOutOne(row)">出场</el-button>
              <el-button
                v-if="(row.warningType || 0) > 0"
                link
                type="warning"
                @click="openWarningModal(row)"
              >
                处理预警
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="queryParams.pageNo"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          layout="total, prev, pager, next, sizes"
          class="mt-4"
          @current-change="handleQuery"
          @size-change="handleQuery"
        />
      </ContentWrap>
    </div>
  </div>

  <Dialog title="预警消息列表" v-model="warningDialogVisible" width="760px" :appendToBody="true">
    <ContentWrap>
      <el-table :data="warningBar" border :stripe="true" height="420">
        <el-table-column prop="title" label="预警类型" width="160" />
        <el-table-column prop="count" label="数量" width="100" />
        <el-table-column prop="content" label="说明" min-width="260" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              link
              @click="warningDialogVisible = false; quickHandleWarning(row.key)"            >
              立即处理
            </el-button>
            <el-button size="small" link @click="closeWarningBarItem(row.key)">忽略</el-button>
          </template>
        </el-table-column>
      </el-table>
    </ContentWrap>
  </Dialog>

  <Dialog title="预警处理" v-model="warningModalVisible" width="720px" :appendToBody="true">
    <el-form label-width="90px" @submit.prevent>
      <el-form-item label="预警类型">
        <el-tag v-if="warningForm.warnType === 'space'" type="warning">余位不足预警处理</el-tag>
        <el-tag v-else-if="warningForm.warnType === 'longtime'" type="danger"
          >长时间停车预警处理</el-tag
        >
        <el-tag v-else-if="warningForm.warnType === 'noplate'" type="danger">无牌车预警处理</el-tag>
        <el-tag v-else-if="warningForm.warnType === 'device'" type="info">设备离线预警处理</el-tag>
        <el-tag v-else>预警处理</el-tag>
      </el-form-item>
      <el-form-item label="车牌号码">
        <el-input v-model="warningForm.plateNo" disabled />
      </el-form-item>
      <el-form-item label="处理状态">
        <el-select v-model="warningForm.status" class="!w-full">
          <el-option label="待处理" value="pending" />
          <el-option label="处理中" value="processing" />
          <el-option label="已完成" value="done" />
          <el-option label="已忽略" value="ignored" />
        </el-select>
      </el-form-item>
      <el-form-item label="处理人">
        <el-input v-model="warningForm.handler" placeholder="请输入处理人" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input
          v-model="warningForm.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注信息"
        />
      </el-form-item>
      <div class="flex justify-end gap-2">
        <el-button @click="warningModalVisible = false">取消</el-button>
        <el-button type="primary" @click="saveWarningHandle">保存</el-button>
      </div>
    </el-form>
  </Dialog>

  <Dialog title="车辆详情" v-model="detailModalVisible" width="720px" :appendToBody="true">
    <div v-if="detailRow">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="车牌号码">{{ detailRow.plateNo }}</el-descriptions-item>
        <el-descriptions-item label="车辆类型">{{
          vehicleTypeLabel(detailRow.vehicleType)
        }}</el-descriptions-item>
        <el-descriptions-item label="进场时间">{{ detailRow.inTime }}</el-descriptions-item>
        <el-descriptions-item label="停车时长">{{
          durationText(detailRow.durationMinutes)
        }}</el-descriptions-item>
        <el-descriptions-item label="出入口">{{
          getGateName(detailRow.entryLaneId)
        }}</el-descriptions-item>
        <el-descriptions-item label="车位号">{{ detailRow.spaceNo ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="应缴费用(元)">{{
          detailRow.expectedFee ?? '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="车辆状态">{{
          vehicleStatusLabel(detailRow)
        }}</el-descriptions-item>
        <el-descriptions-item label="预警类型">{{
          warningTypeLabel(detailRow.warningType)
        }}</el-descriptions-item>
        <el-descriptions-item label="进场照片" :span="2">
          <el-image
            v-if="detailRow.snapshotUrl"
            :src="detailRow.snapshotUrl"
            fit="cover"
            :preview-src-list="[detailRow.snapshotUrl]"
            preview-teleported
            class="parking-present__img--detail"
          />
          <span v-else class="text-[var(--el-text-color-secondary)]">暂无</span>
        </el-descriptions-item>
      </el-descriptions>
    </div>
  </Dialog>

  <Dialog title="远程开闸操作" v-model="remoteModalVisible" width="720px" :appendToBody="true">
    <el-form label-width="80px" @submit.prevent>
      <el-form-item label="车牌号">
        <el-input v-model="remoteForm.plateNo" placeholder="请输入车牌号" />
      </el-form-item>
      <el-form-item label="出入口">
        <el-select
          v-if="doorOptions.length"
          v-model="remoteForm.gateId"
          placeholder="请选择出入口"
          class="!w-full"
          clearable
        >
          <el-option v-for="d in doorOptions" :key="d.id" :label="d.name" :value="d.id" />
        </el-select>
        <el-input v-else v-model="remoteForm.gateName" placeholder="请输入出入口名称" />
      </el-form-item>
      <el-form-item label="原因">
        <el-select v-model="remoteForm.reason" class="!w-full">
          <el-option label="正常出场" value="normal" />
          <el-option label="紧急情况" value="emergency" />
          <el-option label="系统故障" value="device" />
          <el-option label="其他原因" value="other" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input
          v-model="remoteForm.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注信息（选填）"
        />
      </el-form-item>
      <div class="flex justify-end gap-2">
        <el-button @click="remoteModalVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRemoteOpenGate">确认开闸</el-button>
      </div>
    </el-form>
  </Dialog>

  <Dialog title="批量处理操作" v-model="batchModalVisible" width="720px" :appendToBody="true">
    <el-form label-width="110px" @submit.prevent>
      <el-form-item label="批量操作类型">
        <el-select v-model="batchForm.action" class="!w-full">
          <el-option label="批量处理预警" value="warn" />
          <el-option label="批量出场" value="exit" />
          <el-option label="批量忽略预警" value="ignore" />
          <el-option label="批量导出信息" value="export" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注说明">
        <el-input
          v-model="batchForm.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注信息（选填）"
        />
      </el-form-item>
      <div class="flex justify-end gap-2">
        <el-button @click="batchModalVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBatch">确认</el-button>
      </div>
    </el-form>
  </Dialog>

  <Dialog title="异常记录查询" v-model="abnormalModalVisible" width="980px" :appendToBody="true">
    <ContentWrap>
      <el-form :inline="true" label-width="80px" @submit.prevent>
        <el-form-item label="异常类型">
          <el-select v-model="abnormalForm.abnormalType" class="!w-200px">
            <el-option label="全部" value="all" />
            <el-option label="预警记录" value="warn" />
            <el-option label="设备异常" value="device" />
            <el-option label="无牌车" value="noplate" />
            <el-option label="长时间停车" value="longtime" />
          </el-select>
        </el-form-item>
        <el-form-item label="车牌号">
          <el-input
            v-model="abnormalForm.plateNo"
            placeholder="请输入车牌号"
            clearable
            class="!w-220px"
          />
        </el-form-item>
      </el-form>
    </ContentWrap>
    <ContentWrap>
      <el-table :data="abnormalList" border :stripe="true">
        <el-table-column prop="plateNo" label="车牌号码" width="140" />
        <el-table-column label="车辆类型" width="120">
          <template #default="{ row }">
            {{ vehicleTypeLabel(row.vehicleType) }}
          </template>
        </el-table-column>
        <el-table-column prop="inTime" label="入场时间" width="180" />
        <el-table-column prop="durationMinutes" label="停车时长(分钟)" width="140" />
        <el-table-column label="预警类型" width="160">
          <template #default="{ row }">
            {{ warningTypeLabel(row.warningType) }}
          </template>
        </el-table-column>
        <el-table-column prop="spaceNo" label="车位号" />
      </el-table>
    </ContentWrap>
  </Dialog>
</template>

<style scoped lang="scss">
@use './prototype.scss' as *;

.parking-page {
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
  box-sizing: border-box;
}

.parking-present__ticker {
  position: sticky;
  top: 0;
  z-index: 12;
  display: flex;
  height: 44px;
  padding: 0 12px;
  background: var(--el-fill-color-lighter);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  box-sizing: border-box;
  align-items: center;
  gap: 12px;
}

.parking-present__ticker-dot {
  width: 8px;
  height: 8px;
  background: #f59e0b;
  border-radius: 999px;
  box-shadow: 0 0 0 2px rgb(245 158 11 / 18%);
}

.parking-present__ticker-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  white-space: nowrap;
}

.parking-present__ticker-viewport {
  min-width: 0;
  overflow: hidden;
  flex: 1;
}

.parking-present__ticker-track {
  display: flex;
  align-items: center;
  gap: 22px;
  width: max-content;
  animation: present-marquee 28s linear infinite;
}

.parking-present__ticker-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.parking-present__ticker-badge {
  padding: 2px 8px;
  font-size: 12px;
  font-weight: 700;
  border: 1px solid transparent;
  border-radius: 999px;
}

.parking-present__ticker-badge.is-space {
  color: #92400e;
  background: rgb(245 158 11 / 12%);
  border-color: rgb(245 158 11 / 18%);
}

.parking-present__ticker-badge.is-longtime {
  color: #b91c1c;
  background: rgb(239 68 68 / 12%);
  border-color: rgb(239 68 68 / 18%);
}

.parking-present__ticker-badge.is-noplate {
  color: #b91c1c;
  background: rgb(239 68 68 / 12%);
  border-color: rgb(239 68 68 / 18%);
}

.parking-present__ticker-badge.is-device {
  color: #1d4ed8;
  background: rgb(59 130 246 / 12%);
  border-color: rgb(59 130 246 / 18%);
}

.parking-present__ticker-text {
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.parking-present__ticker-empty {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.parking-present__ticker-actions {
  white-space: nowrap;
}

@keyframes present-marquee {
  0% {
    transform: translateX(0);
  }

  100% {
    transform: translateX(-50%);
  }
}

.parking-present__content {
  padding-top: 8px;
}

.parking-present__img {
  width: 96px;
  height: 48px;
  overflow: hidden;
  border-radius: 6px;
}

.parking-present__img--detail {
  width: 240px;
  height: 120px;
  overflow: hidden;
  border-radius: 8px;
}
</style>
