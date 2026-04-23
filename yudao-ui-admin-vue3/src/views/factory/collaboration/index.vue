<!-- 智慧工厂业务协同四大域工作台 -->
<script setup lang="ts">
import type { EChartsOption } from 'echarts'
import type { FormInstance, FormRules } from 'element-plus'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import {
  completeMaintenanceOrder,
  createCarbonTrade,
  createDevice,
  createMaintenancePlan,
  createProductionPlan,
  getCarbonDashboard,
  getDeviceDashboard,
  getEnergyDashboard,
  getProductionBatchTraceDetail,
  getProductionDashboard,
  handleEnergySuggestion,
  updateProductionPlanStatus,
  type CarbonDashboardData,
  type CollaborationMetricCard,
  type DeviceDashboardData,
  type EnergyDashboardData,
  type ProductionBatchTraceDetailData,
  type ProductionDashboardData
} from '@/api/factory/collaboration'
import { Echart } from '@/components/Echart'
import { Icon } from '@/components/Icon'
import { useMessage } from '@/hooks/web/useMessage'
import FactoryDashboardShell from '../dashboard/components/FactoryDashboardShell.vue'
import FactoryMetricCard from '../dashboard/components/FactoryMetricCard.vue'
import FactoryPanel from '../dashboard/components/FactoryPanel.vue'

defineOptions({ name: 'FactoryCollaboration' })

type MainTab = 'production' | 'energy' | 'device' | 'carbon'

interface DeviceOption {
  label: string
  value: number
}

/**
 * 一级 Tab 配置
 */
const MAIN_TABS: Array<{ label: string; value: MainTab }> = [
  { label: '生产协同', value: 'production' },
  { label: '能源管理', value: 'energy' },
  { label: '设备管理', value: 'device' },
  { label: '碳资产', value: 'carbon' }
]

/**
 * 二级 Tab 配置
 */
const SUB_TAB_MAP: Record<MainTab, Array<{ label: string; value: string }>> = {
  production: [
    { label: '生产计划', value: 'production-plan' },
    { label: '批次追踪', value: 'batch-trace' }
  ],
  energy: [
    { label: '综合概览', value: 'overview' },
    { label: '电力', value: 'electricity' },
    { label: '水', value: 'water' },
    { label: '气', value: 'gas' }
  ],
  device: [
    { label: '设备列表', value: 'device-list' },
    { label: '维保计划', value: 'maintenance-plan' }
  ],
  carbon: [
    { label: 'Carbon Overview', value: 'carbon-overview' },
    { label: 'Emission Sources', value: 'emission-sources' },
    { label: 'Carbon Trading', value: 'carbon-trading' }
  ]
}

const route = useRoute()
const router = useRouter()
const message = useMessage()

const loading = ref(false)
const globalKeyword = ref('')
const activeMainTab = ref<MainTab>('production')
const selectedBatchId = ref<number>()
const selectedDeviceId = ref<number>()
const energyDateMode = ref<'day' | 'week' | 'month'>('day')
const energyDate = ref(dayjs().format('YYYY-MM-DD'))
const carbonDate = ref(dayjs().format('YYYY-MM-DD'))
const nowTime = ref(dayjs().format('YYYY/MM/DD HH:mm'))
const nowTimer = ref<number>()

const activeSubTabs = reactive<Record<MainTab, string>>({
  production: 'production-plan',
  energy: 'overview',
  device: 'device-list',
  carbon: 'carbon-overview'
})

const productionData = ref<ProductionDashboardData | null>(null)
const batchTraceDetail = ref<ProductionBatchTraceDetailData | null>(null)
const energyData = ref<EnergyDashboardData | null>(null)
const deviceData = ref<DeviceDashboardData | null>(null)
const carbonData = ref<CarbonDashboardData | null>(null)

const productionDialogVisible = ref(false)
const deviceDialogVisible = ref(false)
const maintenanceDialogVisible = ref(false)
const carbonTradeDialogVisible = ref(false)

const productionFormRef = ref<FormInstance>()
const deviceFormRef = ref<FormInstance>()
const maintenanceFormRef = ref<FormInstance>()
const carbonTradeFormRef = ref<FormInstance>()

const productionForm = reactive({
  planCode: '',
  productName: '',
  batchCode: '',
  lineName: '',
  plannedQuantity: 1000,
  operatorName: '',
  plannedStartTime: dayjs().startOf('hour').format('YYYY-MM-DD HH:mm:ss')
})

const deviceForm = reactive({
  deviceCode: '',
  deviceName: '',
  categoryName: '',
  modelName: '',
  areaName: '',
  online: true,
  runningStatus: '运行中',
  efficiencyRate: 88
})

const maintenanceForm = reactive({
  deviceId: undefined as number | undefined,
  planName: '',
  cycleType: '月度',
  nextExecuteDate: dayjs().add(1, 'day').format('YYYY-MM-DD'),
  ownerName: ''
})

const carbonTradeForm = reactive({
  tradeCode: '',
  tradeType: 'BUY',
  quantity: 100,
  unitPrice: 42,
  tradeDate: dayjs().format('YYYY-MM-DD'),
  counterparty: ''
})

const productionRules: FormRules = {
  planCode: [{ required: true, message: '请输入计划编号', trigger: 'blur' }],
  productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  batchCode: [{ required: true, message: '请输入批次编号', trigger: 'blur' }],
  lineName: [{ required: true, message: '请输入产线/区域', trigger: 'blur' }],
  plannedQuantity: [{ required: true, message: '请输入计划数量', trigger: 'blur' }],
  operatorName: [{ required: true, message: '请输入操作员', trigger: 'blur' }],
  plannedStartTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }]
}

const deviceRules: FormRules = {
  deviceCode: [{ required: true, message: '请输入设备编码', trigger: 'blur' }],
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  categoryName: [{ required: true, message: '请输入设备类别', trigger: 'blur' }],
  modelName: [{ required: true, message: '请输入设备型号', trigger: 'blur' }],
  areaName: [{ required: true, message: '请输入所属区域', trigger: 'blur' }]
}

const maintenanceRules: FormRules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  cycleType: [{ required: true, message: '请选择维保周期', trigger: 'change' }],
  nextExecuteDate: [{ required: true, message: '请选择执行日期', trigger: 'change' }],
  ownerName: [{ required: true, message: '请输入负责人', trigger: 'blur' }]
}

const carbonTradeRules: FormRules = {
  tradeCode: [{ required: true, message: '请输入交易编号', trigger: 'blur' }],
  tradeType: [{ required: true, message: '请选择交易类型', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入交易数量', trigger: 'blur' }],
  unitPrice: [{ required: true, message: '请输入交易单价', trigger: 'blur' }],
  tradeDate: [{ required: true, message: '请选择交易日期', trigger: 'change' }],
  counterparty: [{ required: true, message: '请输入交易对手方', trigger: 'blur' }]
}

const currentSubTabs = computed(() => SUB_TAB_MAP[activeMainTab.value])
const currentSubTab = computed(() => activeSubTabs[activeMainTab.value])
const currentMetrics = computed<CollaborationMetricCard[]>(() => {
  if (activeMainTab.value === 'production') return productionData.value?.metrics || []
  if (activeMainTab.value === 'energy') return energyData.value?.metrics || []
  if (activeMainTab.value === 'device') return deviceData.value?.metrics || []
  return carbonData.value?.metrics || []
})

const currentUpdatedAt = computed(() => {
  if (activeMainTab.value === 'production') return productionData.value?.updatedAt
  if (activeMainTab.value === 'energy') return energyData.value?.updatedAt
  if (activeMainTab.value === 'device') return deviceData.value?.updatedAt
  return carbonData.value?.updatedAt
})

const deviceOptions = computed<DeviceOption[]>(() => {
  return (deviceData.value?.deviceList || []).map((item) => ({
    label: `${item.deviceName}（${item.deviceCode}）`,
    value: item.id
  }))
})

const productionTableData = computed(() => productionData.value?.planList || [])
const batchTableData = computed(() => productionData.value?.batchList || [])
const productionAlertList = computed(() => productionData.value?.alertList || [])
const deviceList = computed(() => deviceData.value?.deviceList || [])
const maintenancePlanList = computed(() => deviceData.value?.maintenancePlanList || [])
const carbonTradeList = computed(() => carbonData.value?.tradeList || [])
const carbonSourceList = computed(() => carbonData.value?.sourceList || [])
const batchTraceSummary = computed(() => batchTraceDetail.value?.summary || null)

const energyTrendOptions = computed<EChartsOption>(() => {
  const trendList = energyData.value?.trendList || []
  const labels = trendList.map((item) => item.label)
  const isOverview = currentSubTab.value === 'overview'
  return {
    tooltip: { trigger: 'axis' },
    legend: {
      top: 0,
      textStyle: { color: 'rgba(210, 231, 248, 0.78)' }
    },
    grid: { left: 48, right: 20, top: 48, bottom: 28 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLine: { lineStyle: { color: 'rgba(109, 143, 173, 0.35)' } },
      axisLabel: { color: 'rgba(188, 212, 232, 0.7)' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(109, 143, 173, 0.18)' } },
      axisLine: { show: false },
      axisLabel: { color: 'rgba(188, 212, 232, 0.7)' }
    },
    series: isOverview
      ? [
          buildLineSeries('电力', '#f3c214', trendList.map((item) => item.electricityValue)),
          buildLineSeries('水', '#4e8eff', trendList.map((item) => item.waterValue)),
          buildLineSeries('气', '#ff8a34', trendList.map((item) => item.gasValue))
        ]
      : [
          buildLineSeries(
            currentSubTab.value === 'electricity'
              ? '电力'
              : currentSubTab.value === 'water'
                ? '水'
                : '气',
            currentSubTab.value === 'electricity'
              ? '#f3c214'
              : currentSubTab.value === 'water'
                ? '#4e8eff'
                : '#ff8a34',
            trendList.map((item) => item.currentValue)
          )
        ]
  }
})

const carbonTrendOptions = computed<EChartsOption>(() => {
  const trendList = carbonData.value?.trendList || []
  return {
    tooltip: { trigger: 'axis' },
    legend: {
      top: 0,
      textStyle: { color: 'rgba(210, 231, 248, 0.78)' }
    },
    grid: { left: 48, right: 20, top: 48, bottom: 28 },
    xAxis: {
      type: 'category',
      data: trendList.map((item) => item.label),
      axisLine: { lineStyle: { color: 'rgba(109, 143, 173, 0.35)' } },
      axisLabel: { color: 'rgba(188, 212, 232, 0.7)' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(109, 143, 173, 0.18)' } },
      axisLine: { show: false },
      axisLabel: { color: 'rgba(188, 212, 232, 0.7)' }
    },
    series: [
      buildLineSeries('排放量', '#22c55e', trendList.map((item) => item.emissionValue)),
      buildLineSeries('目标线', '#60a5fa', trendList.map((item) => item.targetValue), 'dashed')
    ]
  }
})

/**
 * 组装折线图配置项
 *
 * @param name 图例名称
 * @param color 颜色
 * @param data 数据
 * @param lineType 线条类型
 * @returns series 配置
 */
function buildLineSeries(
  name: string,
  color: string,
  data: Array<number>,
  lineType: 'solid' | 'dashed' = 'solid'
) {
  return {
    name,
    type: 'line',
    smooth: true,
    symbolSize: 8,
    lineStyle: { color, width: 3, type: lineType },
    itemStyle: { color },
    areaStyle:
      lineType === 'solid'
        ? {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: `${color}66` },
                { offset: 1, color: `${color}05` }
              ]
            }
          }
        : undefined,
    data
  }
}

/**
 * 格式化数字
 *
 * @param value 数值
 * @param digits 小数位
 * @returns 展示文本
 */
function formatNumber(value?: number | string | null, digits = 0) {
  const numericValue = Number(value ?? 0)
  return numericValue.toLocaleString('zh-CN', {
    minimumFractionDigits: digits,
    maximumFractionDigits: digits
  })
}

/**
 * 格式化时间
 *
 * @param value 时间值
 * @param pattern 输出格式
 * @returns 格式化文本
 */
function formatTime(value?: string | null, pattern = 'YYYY-MM-DD HH:mm') {
  if (!value) {
    return '--'
  }
  return dayjs(value).format(pattern)
}

/**
 * 获取状态标签类型
 *
 * @param status 状态
 * @returns 标签类型
 */
function getStatusTagType(status?: string) {
  if (!status) return 'info'
  if (['IN_PROGRESS', '运行中', '处理中', '已登记', '在线'].includes(status)) return 'success'
  if (['PENDING', 'NOT_STARTED', '待开始', '待处理', '待执行', 'PLANNED'].includes(status)) return 'warning'
  if (['COMPLETED', '已完成'].includes(status)) return 'info'
  if (['故障'].includes(status)) return 'danger'
  return 'primary'
}

/**
 * 获取状态文本
 *
 * @param status 状态值
 * @returns 中文文本
 */
function getStatusText(status?: string) {
  const statusMap: Record<string, string> = {
    IN_PROGRESS: '进行中',
    PENDING: '待开始',
    NOT_STARTED: '待开始',
    COMPLETED: '已完成',
    PLANNED: '已计划',
    BUY: '买入',
    SELL: '卖出'
  }
  return status ? statusMap[status] || status : '--'
}

/**
 * 根据当前路由同步页面查询条件
 */
function applyRouteQuery() {
  const main = route.query.mainTab as MainTab | undefined
  const sub = route.query.subTab as string | undefined
  const keyword = route.query.keyword as string | undefined
  const energyDateQuery = route.query.energyDate as string | undefined
  const carbonDateQuery = route.query.carbonDate as string | undefined
  const energyModeQuery = route.query.energyMode as 'day' | 'week' | 'month' | undefined
  if (main && SUB_TAB_MAP[main]) {
    activeMainTab.value = main
  }
  if (sub && SUB_TAB_MAP[activeMainTab.value].some((item) => item.value === sub)) {
    activeSubTabs[activeMainTab.value] = sub
  }
  globalKeyword.value = keyword || ''
  energyDate.value = energyDateQuery || energyDate.value
  carbonDate.value = carbonDateQuery || carbonDate.value
  energyDateMode.value = energyModeQuery || energyDateMode.value
}

/**
 * 将当前 Tab 与筛选条件同步到地址栏 query
 */
function syncRouteQuery() {
  router.replace({
    path: route.path,
    query: {
      ...route.query,
      mainTab: activeMainTab.value,
      subTab: currentSubTab.value,
      keyword: globalKeyword.value || undefined,
      energyDate: energyDate.value,
      carbonDate: carbonDate.value,
      energyMode: energyDateMode.value
    }
  })
}

/**
 * 加载当前一级 Tab 对应的数据
 */
async function loadCurrentTabData() {
  loading.value = true
  try {
    if (activeMainTab.value === 'production') {
      productionData.value = await getProductionDashboard({
        subTab: currentSubTab.value,
        keyword: globalKeyword.value || undefined
      })
      if (currentSubTab.value === 'batch-trace') {
        const currentBatch =
          productionData.value.batchList.find((item) => item.id === selectedBatchId.value) ||
          productionData.value.batchList[0]
        if (currentBatch?.id) {
          selectedBatchId.value = currentBatch.id
          batchTraceDetail.value = await getProductionBatchTraceDetail({ batchId: currentBatch.id })
        } else {
          selectedBatchId.value = undefined
          batchTraceDetail.value = null
        }
      } else {
        selectedBatchId.value = undefined
        batchTraceDetail.value = null
      }
      return
    }
    if (activeMainTab.value === 'energy') {
      energyData.value = await getEnergyDashboard({
        subTab: currentSubTab.value,
        dateMode: energyDateMode.value,
        statDate: energyDate.value
      })
      return
    }
    if (activeMainTab.value === 'device') {
      deviceData.value = await getDeviceDashboard({
        subTab: currentSubTab.value,
        keyword: globalKeyword.value || undefined,
        selectedDeviceId: selectedDeviceId.value
      })
      if (!selectedDeviceId.value && deviceData.value?.detail?.id) {
        selectedDeviceId.value = deviceData.value.detail.id
      }
      return
    }
    carbonData.value = await getCarbonDashboard({
      subTab: currentSubTab.value,
      statDate: carbonDate.value
    })
  } finally {
    loading.value = false
  }
}

/**
 * 切换批次追溯明细
 *
 * @param batchId 批次主键
 */
async function selectBatch(batchId: number) {
  selectedBatchId.value = batchId
  batchTraceDetail.value = await getProductionBatchTraceDetail({ batchId })
}

/**
 * 切换一级 Tab
 *
 * @param tab 一级 Tab
 */
async function switchMainTab(tab: MainTab) {
  activeMainTab.value = tab
  syncRouteQuery()
  await loadCurrentTabData()
}

/**
 * 切换二级 Tab
 *
 * @param subTab 二级 Tab
 */
async function switchSubTab(subTab: string) {
  activeSubTabs[activeMainTab.value] = subTab
  syncRouteQuery()
  await loadCurrentTabData()
}

/**
 * 刷新当前工作台
 */
async function refreshCurrentTab() {
  await loadCurrentTabData()
  message.success('已刷新当前工作台')
}

/**
 * 执行全局搜索
 */
async function handleSearch() {
  syncRouteQuery()
  await loadCurrentTabData()
}

/**
 * 快速推进生产计划状态
 *
 * @param plan 计划项
 * @param status 新状态
 * @param progress 新进度
 */
async function handleProductionStatusChange(
  plan: ProductionDashboardData['planList'][number],
  status: string,
  progress: number
) {
  await message.confirm(`确认将计划 ${plan.planCode} 更新为 ${getStatusText(status)} 吗？`)
  await updateProductionPlanStatus({ id: plan.id, status, progress, remark: '业务协同工作台操作' })
  message.success('计划状态已更新')
  await loadCurrentTabData()
}

/**
 * 处理节能建议
 *
 * @param id 建议主键
 * @param status 新状态
 */
async function handleSuggestionStatus(id: number, status: string) {
  await handleEnergySuggestion({ id, status })
  message.success('建议状态已更新')
  await loadCurrentTabData()
}

/**
 * 选中设备卡片
 *
 * @param deviceId 设备主键
 */
async function selectDevice(deviceId: number) {
  selectedDeviceId.value = deviceId
  await loadCurrentTabData()
}

/**
 * 完成维保工单
 *
 * @param orderId 工单主键
 */
async function finishMaintenanceOrder(orderId?: number) {
  if (!orderId) {
    message.warning('当前计划没有待执行工单')
    return
  }
  await completeMaintenanceOrder({
    orderId,
    result: '正常',
    remark: '由业务协同工作台完成闭环'
  })
  message.success('维保工单已完成')
  await loadCurrentTabData()
}

/**
 * 导出当前页面真实数据
 */
function exportCurrentData() {
  const filename = `${MAIN_TABS.find((item) => item.value === activeMainTab.value)?.label || '业务协同'}-${dayjs().format(
    'YYYYMMDDHHmmss'
  )}.csv`
  let csvContent = ''

  if (activeMainTab.value === 'production') {
    const rows = currentSubTab.value === 'production-plan' ? productionTableData.value : batchTableData.value
    csvContent =
      currentSubTab.value === 'production-plan'
        ? ['计划编号,产品,批次,数量,操作员,开始时间,状态,进度']
            .concat(
              rows.map(
                (item) =>
                  `${(item as ProductionDashboardData['planList'][number]).planCode},${(item as ProductionDashboardData['planList'][number]).productName},${(item as ProductionDashboardData['planList'][number]).batchCode},${(item as ProductionDashboardData['planList'][number]).plannedQuantity},${(item as ProductionDashboardData['planList'][number]).operatorName},${formatTime((item as ProductionDashboardData['planList'][number]).plannedStartTime)},${getStatusText((item as ProductionDashboardData['planList'][number]).status)},${(item as ProductionDashboardData['planList'][number]).progress}%`
              )
            )
            .join('\n')
        : ['批次,产品,工序,位置,完成量,良率,状态,更新时间']
            .concat(
              rows.map(
                (item) =>
                  `${(item as ProductionDashboardData['batchList'][number]).batchCode},${(item as ProductionDashboardData['batchList'][number]).productName},${(item as ProductionDashboardData['batchList'][number]).currentProcess},${(item as ProductionDashboardData['batchList'][number]).currentLocation},${(item as ProductionDashboardData['batchList'][number]).completedQuantity},${formatNumber((item as ProductionDashboardData['batchList'][number]).yieldRate, 1)}%,${getStatusText((item as ProductionDashboardData['batchList'][number]).status)},${formatTime((item as ProductionDashboardData['batchList'][number]).updatedAt)}`
              )
            )
            .join('\n')
  } else if (activeMainTab.value === 'energy') {
    csvContent = ['时间,电力,水,气']
      .concat(
        (energyData.value?.trendList || []).map(
          (item) => `${item.label},${item.electricityValue},${item.waterValue},${item.gasValue}`
        )
      )
      .join('\n')
  } else if (activeMainTab.value === 'device') {
    csvContent =
      currentSubTab.value === 'device-list'
        ? ['设备编码,设备名称,类别,区域,在线状态,运行状态,健康状态,效率']
            .concat(
              deviceList.value.map(
                (item) =>
                  `${item.deviceCode},${item.deviceName},${item.categoryName},${item.areaName},${item.online ? '在线' : '离线'},${item.runningStatus},${item.healthStatus},${item.efficiencyRate}%`
              )
            )
            .join('\n')
        : ['计划名称,设备,周期,下次执行,负责人,状态,待执行工单']
            .concat(
              maintenancePlanList.value.map(
                (item) =>
                  `${item.planName},${item.deviceName},${item.cycleType},${item.nextExecuteDate},${item.ownerName},${getStatusText(item.status)},${item.pendingOrderCount}`
              )
            )
            .join('\n')
  } else {
    csvContent =
      currentSubTab.value === 'carbon-trading'
        ? ['交易编号,类型,数量,单价,金额,余额,日期,对手方,状态']
            .concat(
              carbonTradeList.value.map(
                (item) =>
                  `${item.tradeCode},${getStatusText(item.tradeType)},${item.quantity},${item.unitPrice},${item.amount},${item.balanceAfter},${item.tradeDate},${item.counterparty},${item.status}`
              )
            )
            .join('\n')
        : ['来源,类型,排放量,占比']
            .concat(
              carbonSourceList.value.map(
                (item) =>
                  `${item.sourceName},${item.sourceType},${item.emissionValue},${formatNumber(item.proportion, 2)}%`
              )
            )
            .join('\n')
  }

  const blob = new Blob([`\ufeff${csvContent}`], { type: 'text/csv;charset=utf-8;' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  window.URL.revokeObjectURL(url)
}

/**
 * 提交生产计划
 */
async function submitProductionForm() {
  const valid = await productionFormRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  await createProductionPlan({ ...productionForm })
  productionDialogVisible.value = false
  resetProductionForm()
  message.success('生产计划已创建')
  await loadCurrentTabData()
}

/**
 * 提交设备新增
 */
async function submitDeviceForm() {
  const valid = await deviceFormRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  await createDevice({ ...deviceForm })
  deviceDialogVisible.value = false
  resetDeviceForm()
  message.success('设备已新增')
  await loadCurrentTabData()
}

/**
 * 提交维保计划
 */
async function submitMaintenanceForm() {
  const valid = await maintenanceFormRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  await createMaintenancePlan({
    deviceId: Number(maintenanceForm.deviceId),
    planName: maintenanceForm.planName,
    cycleType: maintenanceForm.cycleType,
    nextExecuteDate: maintenanceForm.nextExecuteDate,
    ownerName: maintenanceForm.ownerName
  })
  maintenanceDialogVisible.value = false
  resetMaintenanceForm()
  message.success('维保计划已创建')
  await loadCurrentTabData()
}

/**
 * 提交碳交易
 */
async function submitCarbonTradeForm() {
  const valid = await carbonTradeFormRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  await createCarbonTrade({
    tradeCode: carbonTradeForm.tradeCode,
    tradeType: carbonTradeForm.tradeType,
    quantity: Number(carbonTradeForm.quantity),
    unitPrice: Number(carbonTradeForm.unitPrice),
    tradeDate: carbonTradeForm.tradeDate,
    counterparty: carbonTradeForm.counterparty
  })
  carbonTradeDialogVisible.value = false
  resetCarbonTradeForm()
  message.success('碳交易已登记')
  await loadCurrentTabData()
}

/**
 * 重置生产计划表单
 */
function resetProductionForm() {
  productionForm.planCode = `PLAN-${dayjs().format('HHmmss')}`
  productionForm.productName = ''
  productionForm.batchCode = `BATCH-${dayjs().format('HHmmss')}`
  productionForm.lineName = ''
  productionForm.plannedQuantity = 1000
  productionForm.operatorName = ''
  productionForm.plannedStartTime = dayjs().startOf('hour').format('YYYY-MM-DD HH:mm:ss')
  productionFormRef.value?.clearValidate()
}

/**
 * 重置设备表单
 */
function resetDeviceForm() {
  deviceForm.deviceCode = `EQ-${dayjs().format('HHmmss')}`
  deviceForm.deviceName = ''
  deviceForm.categoryName = ''
  deviceForm.modelName = ''
  deviceForm.areaName = ''
  deviceForm.online = true
  deviceForm.runningStatus = '运行中'
  deviceForm.efficiencyRate = 88
  deviceFormRef.value?.clearValidate()
}

/**
 * 重置维保表单
 */
function resetMaintenanceForm() {
  maintenanceForm.deviceId = deviceOptions.value[0]?.value
  maintenanceForm.planName = ''
  maintenanceForm.cycleType = '月度'
  maintenanceForm.nextExecuteDate = dayjs().add(1, 'day').format('YYYY-MM-DD')
  maintenanceForm.ownerName = ''
  maintenanceFormRef.value?.clearValidate()
}

/**
 * 重置碳交易表单
 */
function resetCarbonTradeForm() {
  carbonTradeForm.tradeCode = `CT-${dayjs().format('YYYYMMDDHHmmss')}`
  carbonTradeForm.tradeType = 'BUY'
  carbonTradeForm.quantity = 100
  carbonTradeForm.unitPrice = 42
  carbonTradeForm.tradeDate = dayjs().format('YYYY-MM-DD')
  carbonTradeForm.counterparty = ''
  carbonTradeFormRef.value?.clearValidate()
}

/**
 * 初始化页面数据
 */
async function initializePage() {
  applyRouteQuery()
  resetProductionForm()
  resetDeviceForm()
  resetMaintenanceForm()
  resetCarbonTradeForm()
  await loadCurrentTabData()
}

watch(
  () => route.query,
  () => {
    applyRouteQuery()
  }
)

watch(selectedDeviceId, async (value, oldValue) => {
  if (activeMainTab.value !== 'device' || value === oldValue || !value) {
    return
  }
  await loadCurrentTabData()
})

onMounted(async () => {
  nowTimer.value = window.setInterval(() => {
    nowTime.value = dayjs().format('YYYY/MM/DD HH:mm')
  }, 1000)
  await initializePage()
})

onBeforeUnmount(() => {
  if (nowTimer.value) {
    window.clearInterval(nowTimer.value)
  }
})
</script>

<template>
  <FactoryDashboardShell
    title="业务协同"
    subtitle="计划跟踪、能效分析、设备运维、碳资产闭环统一承接。"
    hide-hero
  >
    <div class="collaboration-page" v-loading="loading">
      <section class="collaboration-header">
        <section class="main-tabs" aria-label="业务域">
          <button
            v-for="tab in MAIN_TABS"
            :key="tab.value"
            type="button"
            class="main-tabs__item"
            :class="{ 'main-tabs__item--active': tab.value === activeMainTab }"
            @click="switchMainTab(tab.value)"
          >
            {{ tab.label }}
          </button>
        </section>
        <div class="collaboration-header__actions">
          <ElInput
            v-model="globalKeyword"
            placeholder="搜索设备、告警、区域..."
            clearable
            class="collaboration-header__search"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <Icon icon="ep:search" />
            </template>
          </ElInput>
          <div class="collaboration-header__time">{{ nowTime }}</div>
          <ElButton class="collaboration-header__refresh" @click="refreshCurrentTab">
            <Icon icon="ep:refresh" />
          </ElButton>
          <div class="collaboration-header__user">
            <div class="collaboration-header__avatar">
              <Icon icon="ep:user" />
            </div>
            <div>
              <div class="collaboration-header__user-name">管理员</div>
              <div class="collaboration-header__user-role">超级管理员</div>
            </div>
          </div>
        </div>
      </section>

      <section class="metric-grid">
        <FactoryMetricCard
          v-for="card in currentMetrics"
          :key="card.key"
          :title="card.title"
          :value="card.value"
          :unit="card.unit"
          :hint="card.hint"
          :trend="card.trend"
          :icon="card.icon"
          :theme="card.theme || 'cyan'"
        />
      </section>

      <section class="sub-tabs">
        <button
          v-for="tab in currentSubTabs"
          :key="tab.value"
          type="button"
          class="sub-tabs__item"
          :class="{ 'sub-tabs__item--active': tab.value === currentSubTab }"
          @click="switchSubTab(tab.value)"
        >
          {{ tab.label }}
        </button>
        <div class="sub-tabs__spacer"></div>

        <template v-if="activeMainTab === 'energy'">
          <ElButtonGroup class="sub-tabs__button-group">
            <ElButton :type="energyDateMode === 'day' ? 'primary' : 'default'" @click="energyDateMode = 'day'; loadCurrentTabData()">
              日
            </ElButton>
            <ElButton :type="energyDateMode === 'week' ? 'primary' : 'default'" @click="energyDateMode = 'week'; loadCurrentTabData()">
              周
            </ElButton>
            <ElButton :type="energyDateMode === 'month' ? 'primary' : 'default'" @click="energyDateMode = 'month'; loadCurrentTabData()">
              月
            </ElButton>
          </ElButtonGroup>
          <ElDatePicker
            v-model="energyDate"
            type="date"
            value-format="YYYY-MM-DD"
            class="sub-tabs__date"
            @change="loadCurrentTabData"
          />
          <ElButton type="primary" plain @click="exportCurrentData">
            <Icon icon="ep:download" class="mr-6px" /> 导出报表
          </ElButton>
        </template>

        <template v-else-if="activeMainTab === 'carbon'">
          <ElDatePicker
            v-model="carbonDate"
            type="date"
            value-format="YYYY-MM-DD"
            class="sub-tabs__date"
            @change="loadCurrentTabData"
          />
          <ElButton type="primary" plain @click="exportCurrentData">
            <Icon icon="ep:download" class="mr-6px" /> 导出报表
          </ElButton>
          <ElButton v-if="currentSubTab === 'carbon-trading'" type="primary" @click="carbonTradeDialogVisible = true">
            <Icon icon="ep:plus" class="mr-6px" /> 登记交易
          </ElButton>
        </template>

        <template v-else-if="activeMainTab === 'production'">
          <ElButton type="primary" @click="productionDialogVisible = true">
            <Icon icon="ep:plus" class="mr-6px" /> 新建计划
          </ElButton>
        </template>

        <template v-else-if="activeMainTab === 'device'">
          <ElButton type="primary" plain @click="exportCurrentData">
            <Icon icon="ep:download" class="mr-6px" /> 导出列表
          </ElButton>
          <ElButton type="primary" @click="deviceDialogVisible = true">
            <Icon icon="ep:plus" class="mr-6px" /> 添加设备
          </ElButton>
          <ElButton v-if="currentSubTab === 'maintenance-plan'" type="primary" plain @click="maintenanceDialogVisible = true">
            <Icon icon="ep:plus" class="mr-6px" /> 新增维保计划
          </ElButton>
        </template>
      </section>

      <div v-if="currentUpdatedAt" class="page-updated-at">
        数据更新时间：{{ formatTime(currentUpdatedAt) }}
      </div>

      <template v-if="activeMainTab === 'production'">
        <template v-if="currentSubTab === 'production-plan'">
          <FactoryPanel title="生产计划列表" subtitle="所有统计、进度与状态均来自后端实时数据" accent="cyan">
            <template #extra>
              <ElButton text type="primary" @click="handleSearch">
                <Icon icon="ep:search" class="mr-6px" /> 查询
              </ElButton>
            </template>

            <ElTable :data="productionTableData" class="dark-table">
              <ElTableColumn prop="planCode" label="计划编号" min-width="110" />
              <ElTableColumn prop="productName" label="产品" min-width="100" />
              <ElTableColumn prop="batchCode" label="批次" min-width="110" />
              <ElTableColumn prop="plannedQuantity" label="数量" min-width="90" />
              <ElTableColumn prop="operatorName" label="操作员" min-width="100" />
              <ElTableColumn prop="plannedStartTime" label="开始时间" min-width="160">
                <template #default="{ row }">{{ formatTime(row.plannedStartTime) }}</template>
              </ElTableColumn>
              <ElTableColumn prop="status" label="状态" min-width="100">
                <template #default="{ row }">
                  <ElTag :type="getStatusTagType(row.status)">{{ getStatusText(row.status) }}</ElTag>
                </template>
              </ElTableColumn>
              <ElTableColumn label="进度" min-width="180">
                <template #default="{ row }">
                  <div class="progress-cell">
                    <ElProgress :percentage="row.progress" :stroke-width="10" :show-text="false" />
                    <span>{{ row.progress }}%</span>
                  </div>
                </template>
              </ElTableColumn>
              <ElTableColumn label="操作" width="220" fixed="right">
                <template #default="{ row }">
                  <ElButton
                    size="small"
                    type="primary"
                    link
                    @click="handleProductionStatusChange(row, 'IN_PROGRESS', Math.max(row.progress, 50))"
                  >
                    推进
                  </ElButton>
                  <ElButton
                    size="small"
                    type="success"
                    link
                    @click="handleProductionStatusChange(row, 'COMPLETED', 100)"
                  >
                    完工
                  </ElButton>
                </template>
              </ElTableColumn>
            </ElTable>
          </FactoryPanel>

          <FactoryPanel title="生产关联告警" subtitle="异常与计划、批次形成真实联动闭环" accent="amber">
            <div class="alert-card-grid">
              <div v-for="alert in productionAlertList" :key="alert.id" class="alert-card">
                <div class="alert-card__header">
                  <span class="alert-card__title">{{ alert.alertTitle }}</span>
                  <ElTag size="small" :type="getStatusTagType(alert.status)">{{ alert.levelLabel }}</ElTag>
                </div>
                <div class="alert-card__meta">{{ alert.lineName }}</div>
                <div class="alert-card__meta">{{ formatTime(alert.happenedAt, 'HH:mm') }} · {{ alert.handlerName }}</div>
                <div class="alert-card__action">{{ getStatusText(alert.status) }}</div>
              </div>
            </div>
          </FactoryPanel>
        </template>

        <template v-else>
          <div class="batch-trace-layout">
            <FactoryPanel title="批次列表" subtitle="选择左侧批次查看完整追溯信息" accent="cyan">
              <div v-if="batchTableData.length" class="batch-list">
                <button
                  v-for="item in batchTableData"
                  :key="item.id"
                  type="button"
                  class="batch-list-card"
                  :class="{ 'batch-list-card--active': item.id === selectedBatchId }"
                  @click="selectBatch(item.id)"
                >
                  <div class="batch-list-card__header">
                    <span class="batch-list-card__code">{{ item.batchCode }}</span>
                    <ElTag size="small" :type="getStatusTagType(item.status)">{{ getStatusText(item.status) }}</ElTag>
                  </div>
                  <div class="batch-list-card__name">{{ item.productName }}</div>
                  <div class="batch-list-card__meta">工序：{{ item.currentProcess }}</div>
                  <div class="batch-list-card__meta">位置：{{ item.currentLocation }}</div>
                  <div class="batch-list-card__stats">
                    <span>完成 {{ formatNumber(item.completedQuantity) }}</span>
                    <span>良率 {{ formatNumber(item.yieldRate, 1) }}%</span>
                  </div>
                  <div class="batch-list-card__time">更新时间 {{ formatTime(item.updatedAt) }}</div>
                </button>
              </div>
              <ElEmpty v-else description="暂无批次追溯数据" />
            </FactoryPanel>

            <div class="batch-trace-detail-stack">
              <FactoryPanel title="批次追溯详情" subtitle="按原型结构展示真实追溯记录" accent="violet">
                <div v-if="batchTraceSummary" class="batch-prototype-card">
                  <div class="batch-prototype-card__header">
                    <div>
                      <h3 class="batch-prototype-card__title">{{ batchTraceSummary.batchCode }}</h3>
                      <p class="batch-prototype-card__subtitle">
                        {{ batchTraceSummary.productName }} | 数量: {{ formatNumber(batchTraceSummary.plannedQuantity) }}
                      </p>
                    </div>
                    <span
                      class="batch-prototype-status"
                      :class="{
                        'is-success': ['COMPLETED', '已完成'].includes(batchTraceSummary.status),
                        'is-warning': ['PENDING', 'NOT_STARTED', '待开始'].includes(batchTraceSummary.status),
                        'is-primary': !['COMPLETED', '已完成', 'PENDING', 'NOT_STARTED', '待开始'].includes(batchTraceSummary.status)
                      }"
                    >
                      {{ getStatusText(batchTraceSummary.status) }}
                    </span>
                  </div>

                  <div class="batch-prototype-overview-grid">
                    <div class="batch-prototype-metric-card">
                      <h4 class="batch-prototype-section-title">环境信息</h4>
                      <div
                        v-if="batchTraceDetail?.environmentRecords?.length"
                        class="batch-prototype-metric-card__body batch-prototype-metric-grid batch-prototype-metric-grid--three"
                      >
                        <div class="batch-prototype-metric-item">
                          <p>温度</p>
                          <strong>{{ formatNumber(batchTraceDetail.environmentRecords[0].temperatureValue, 1) }}°C</strong>
                        </div>
                        <div class="batch-prototype-metric-item">
                          <p>湿度</p>
                          <strong>{{ formatNumber(batchTraceDetail.environmentRecords[0].humidityValue, 1) }}%</strong>
                        </div>
                        <div class="batch-prototype-metric-item">
                          <p>压差</p>
                          <strong>{{ formatNumber(batchTraceDetail.environmentRecords[0].pressureValue, 1) }}kPa</strong>
                        </div>
                      </div>
                      <div v-else class="batch-prototype-empty">暂无环境记录</div>
                    </div>

                    <div class="batch-prototype-metric-card">
                      <h4 class="batch-prototype-section-title">质量信息</h4>
                      <div
                        v-if="batchTraceDetail?.qualityRecords?.length"
                        class="batch-prototype-metric-card__body batch-prototype-metric-grid batch-prototype-metric-grid--three"
                      >
                        <div
                          v-for="item in batchTraceDetail.qualityRecords.slice(0, 3)"
                          :key="item.id"
                          class="batch-prototype-metric-item"
                        >
                          <p>{{ item.inspectionItem }}</p>
                          <strong :class="{ 'is-highlight': item.resultStatus === 'PASS' }">
                            {{ item.resultStatus === 'PASS' ? '合格' : item.measuredValue }}
                          </strong>
                        </div>
                      </div>
                      <div v-else class="batch-prototype-empty">暂无质量记录</div>
                    </div>
                  </div>

                  <div class="batch-prototype-section">
                    <h4 class="batch-prototype-section-heading">
                      <Icon icon="ep:user" class="batch-prototype-section-icon is-blue" />
                      人员记录
                    </h4>
                    <div v-if="batchTraceDetail?.personnelRecords?.length" class="batch-prototype-list batch-prototype-scroll-area">
                      <div
                        v-for="item in batchTraceDetail.personnelRecords"
                        :key="item.id"
                        class="batch-prototype-list-item"
                      >
                        <div class="batch-prototype-list-item__main">
                          <div class="batch-prototype-list-item__badge is-blue">
                            <Icon icon="ep:user" />
                          </div>
                          <div>
                            <p class="batch-prototype-list-item__name">{{ item.staffName }}</p>
                            <p class="batch-prototype-list-item__desc">{{ item.roleName }}</p>
                          </div>
                        </div>
                        <div class="batch-prototype-list-item__aside">
                          <p>{{ formatTime(item.recordTime, 'HH:mm') }} · {{ item.durationMinutes }} 分钟</p>
                          <p>{{ item.remark || item.operationName }}</p>
                        </div>
                      </div>
                    </div>
                    <div v-else class="batch-prototype-empty batch-prototype-scroll-area">暂无人员记录</div>
                  </div>

                  <div class="batch-prototype-section">
                    <h4 class="batch-prototype-section-heading">
                      <Icon icon="ep:tools" class="batch-prototype-section-icon is-purple" />
                      设备记录
                    </h4>
                    <div v-if="batchTraceDetail?.deviceRecords?.length" class="batch-prototype-list batch-prototype-scroll-area">
                      <div
                        v-for="item in batchTraceDetail.deviceRecords"
                        :key="item.id"
                        class="batch-prototype-list-item"
                      >
                        <div class="batch-prototype-list-item__main">
                          <div class="batch-prototype-list-item__badge is-purple">
                            <Icon icon="ep:operation" />
                          </div>
                          <div>
                            <p class="batch-prototype-list-item__name">{{ item.deviceName }}</p>
                            <p class="batch-prototype-list-item__desc">{{ item.operationName }}</p>
                          </div>
                        </div>
                        <div class="batch-prototype-list-item__aside">
                          <p>{{ item.runningStatus }}</p>
                          <p>{{ item.parameterSummary || item.operatorName || '--' }}</p>
                        </div>
                      </div>
                    </div>
                    <div v-else class="batch-prototype-empty batch-prototype-scroll-area">暂无设备记录</div>
                  </div>

                  <div class="batch-prototype-section">
                    <h4 class="batch-prototype-section-heading">
                      <Icon icon="ep:box" class="batch-prototype-section-icon is-green" />
                      原料记录
                    </h4>
                    <div v-if="batchTraceDetail?.materialRecords?.length" class="batch-prototype-table-wrap batch-prototype-scroll-area">
                      <table class="batch-prototype-table">
                        <thead>
                          <tr>
                            <th>批号</th>
                            <th>投料人</th>
                            <th>数量</th>
                            <th>类型</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr v-for="item in batchTraceDetail.materialRecords" :key="item.id">
                            <td>{{ item.materialBatchNo }}</td>
                            <td>{{ item.feederName || '--' }}</td>
                            <td>
                              {{ formatNumber(item.actualQuantity, 1) }}{{ item.unit }}
                            </td>
                            <td>
                              <span class="batch-prototype-table-tag">{{ item.materialType }}</span>
                            </td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                    <div v-else class="batch-prototype-empty batch-prototype-scroll-area">暂无原料记录</div>
                  </div>
                </div>
                <ElEmpty v-else description="请选择批次查看详情" />
              </FactoryPanel>
            </div>
          </div>
        </template>
      </template>

      <template v-else-if="activeMainTab === 'energy'">
        <FactoryPanel title="能耗趋势" subtitle="综合概览与单能源视图共用真实时序数据" accent="amber">
          <Echart :options="energyTrendOptions" :height="360" />
        </FactoryPanel>

        <div class="triple-grid">
          <FactoryPanel title="区域用量排名" accent="cyan">
            <div class="rank-list">
              <div v-for="(item, index) in energyData?.areaRanking || []" :key="item.name" class="rank-item">
                <div class="rank-item__name">
                  <span class="rank-item__index">{{ index + 1 }}</span>
                  <span>{{ item.name }}</span>
                </div>
                <div class="rank-item__value">{{ formatNumber(item.value, 1) }} {{ item.unit }}</div>
              </div>
            </div>
          </FactoryPanel>

          <FactoryPanel title="设备用量排名" accent="violet">
            <div class="rank-list">
              <div v-for="(item, index) in energyData?.deviceRanking || []" :key="item.name" class="rank-item">
                <div class="rank-item__name">
                  <span class="rank-item__index">{{ index + 1 }}</span>
                  <span>{{ item.name }}</span>
                </div>
                <div class="rank-item__value">{{ formatNumber(item.value, 1) }} {{ item.unit }}</div>
              </div>
            </div>
          </FactoryPanel>

          <FactoryPanel title="节能建议" accent="emerald">
            <div class="suggestion-list">
              <div v-for="item in energyData?.suggestionList || []" :key="item.id" class="suggestion-card">
                <div class="suggestion-card__title-row">
                  <span>{{ item.title }}</span>
                  <ElTag size="small" :type="getStatusTagType(item.status)">{{ item.levelLabel }}</ElTag>
                </div>
                <div class="suggestion-card__content">{{ item.content }}</div>
                <div class="suggestion-card__footer">
                  <ElButton
                    size="small"
                    type="primary"
                    link
                    @click="handleSuggestionStatus(item.id, item.status === '已处理' ? '待处理' : '已处理')"
                  >
                    {{ item.status === '已处理' ? '改为待处理' : '标记已处理' }}
                  </ElButton>
                </div>
              </div>
            </div>
          </FactoryPanel>
        </div>
      </template>

      <template v-else-if="activeMainTab === 'device'">
        <div class="device-layout">
          <FactoryPanel
            :title="currentSubTab === 'device-list' ? '设备列表' : '维保计划'"
            subtitle="设备台账、状态监控与维保闭环统一由后端真实数据驱动"
            accent="cyan"
          >
            <div v-if="currentSubTab === 'device-list'" class="device-card-grid">
              <button
                v-for="item in deviceList"
                :key="item.id"
                type="button"
                class="device-card"
                :class="{ 'device-card--active': item.id === selectedDeviceId }"
                @click="selectDevice(item.id)"
              >
                <div class="device-card__header">
                  <div class="device-card__icon">
                    <Icon icon="ep:tools" />
                  </div>
                  <div class="device-card__arrow">
                    <Icon icon="ep:arrow-right" />
                  </div>
                </div>
                <div class="device-card__name">{{ item.deviceName }}</div>
                <div class="device-card__desc">{{ item.categoryName }}</div>
                <div class="device-card__meta">{{ item.areaName }}</div>
                <div class="device-card__status-row">
                  <span :class="['device-card__status', item.online ? 'is-online' : 'is-offline']">
                    {{ item.online ? '在线' : '离线' }}
                  </span>
                  <span>{{ item.runningStatus }}</span>
                </div>
                <div class="device-card__efficiency">效率 {{ formatNumber(item.efficiencyRate, 0) }}%</div>
              </button>
            </div>

            <ElTable v-else :data="maintenancePlanList" class="dark-table">
              <ElTableColumn prop="planName" label="计划名称" min-width="140" />
              <ElTableColumn prop="deviceName" label="设备" min-width="120" />
              <ElTableColumn prop="cycleType" label="周期" min-width="90" />
              <ElTableColumn prop="nextExecuteDate" label="下次执行" min-width="110" />
              <ElTableColumn prop="ownerName" label="负责人" min-width="100" />
              <ElTableColumn prop="status" label="状态" min-width="100">
                <template #default="{ row }">
                  <ElTag :type="getStatusTagType(row.status)">{{ getStatusText(row.status) }}</ElTag>
                </template>
              </ElTableColumn>
              <ElTableColumn prop="pendingOrderCount" label="待执行工单" min-width="110" />
              <ElTableColumn label="操作" width="120">
                <template #default="{ row }">
                  <ElButton
                    size="small"
                    type="primary"
                    link
                    @click="finishMaintenanceOrder(row.latestOrderId)"
                  >
                    完成工单
                  </ElButton>
                </template>
              </ElTableColumn>
            </ElTable>
          </FactoryPanel>

          <FactoryPanel title="设备详情" subtitle="点击设备卡片查看实时详情" accent="violet">
            <div v-if="deviceData?.detail" class="device-detail">
              <div class="device-detail__title">{{ deviceData.detail.deviceName }}</div>
              <div class="device-detail__pair"><span>设备编码</span><strong>{{ deviceData.detail.deviceCode }}</strong></div>
              <div class="device-detail__pair"><span>设备类别</span><strong>{{ deviceData.detail.categoryName }}</strong></div>
              <div class="device-detail__pair"><span>设备型号</span><strong>{{ deviceData.detail.modelName }}</strong></div>
              <div class="device-detail__pair"><span>所属区域</span><strong>{{ deviceData.detail.areaName }}</strong></div>
              <div class="device-detail__pair"><span>在线状态</span><strong>{{ deviceData.detail.online ? '在线' : '离线' }}</strong></div>
              <div class="device-detail__pair"><span>运行状态</span><strong>{{ deviceData.detail.runningStatus }}</strong></div>
              <div class="device-detail__pair"><span>健康状态</span><strong>{{ deviceData.detail.healthStatus }}</strong></div>
              <div class="device-detail__pair">
                <span>效率</span>
                <strong>{{ formatNumber(deviceData.detail.efficiencyRate, 0) }}%</strong>
              </div>
              <div class="device-detail__pair"><span>维保负责人</span><strong>{{ deviceData.detail.ownerName || '--' }}</strong></div>
              <div class="device-detail__pair">
                <span>上次维保</span>
                <strong>{{ deviceData.detail.lastMaintenanceDate || '--' }}</strong>
              </div>
              <div class="device-detail__pair">
                <span>下次维保</span>
                <strong>{{ deviceData.detail.nextMaintenanceDate || '--' }}</strong>
              </div>
              <div class="device-detail__remark">{{ deviceData.detail.remark || '暂无补充说明' }}</div>
            </div>
            <ElEmpty v-else description="请选择设备查看详情" />
          </FactoryPanel>
        </div>
      </template>

      <template v-else>
        <FactoryPanel title="Carbon Emission Trend" subtitle="碳排趋势、来源结构与交易明细全部来自真实数据库" accent="emerald">
          <Echart :options="carbonTrendOptions" :height="360" />
        </FactoryPanel>

        <div class="carbon-grid">
          <FactoryPanel
            :title="currentSubTab === 'carbon-trading' ? 'Carbon Trading' : 'Emission Sources'"
            accent="cyan"
          >
            <ElTable v-if="currentSubTab === 'carbon-trading'" :data="carbonTradeList" class="dark-table">
              <ElTableColumn prop="tradeCode" label="交易编号" min-width="130" />
              <ElTableColumn prop="tradeType" label="类型" min-width="90">
                <template #default="{ row }">
                  <ElTag :type="row.tradeType === 'BUY' ? 'success' : 'warning'">{{ getStatusText(row.tradeType) }}</ElTag>
                </template>
              </ElTableColumn>
              <ElTableColumn prop="quantity" label="数量" min-width="90" />
              <ElTableColumn prop="unitPrice" label="单价" min-width="90" />
              <ElTableColumn prop="amount" label="金额" min-width="100" />
              <ElTableColumn prop="balanceAfter" label="余额" min-width="100" />
              <ElTableColumn prop="tradeDate" label="日期" min-width="110" />
              <ElTableColumn prop="counterparty" label="对手方" min-width="140" />
            </ElTable>

            <div v-else class="source-list">
              <div v-for="item in carbonSourceList" :key="item.id" class="source-item">
                <div class="source-item__name">
                  <span>{{ item.sourceName }}</span>
                  <small>{{ item.sourceType }}</small>
                </div>
                <div class="source-item__value">{{ formatNumber(item.emissionValue, 1) }} tCO2</div>
                <div class="source-item__bar">
                  <div class="source-item__bar-fill" :style="{ width: `${item.proportion}%` }"></div>
                </div>
                <div class="source-item__percent">{{ formatNumber(item.proportion, 1) }}%</div>
              </div>
            </div>
          </FactoryPanel>

          <FactoryPanel title="Annual Target Progress" accent="amber">
            <div class="target-card">
              <div class="target-card__ring">
                <div class="target-card__ring-value">
                  {{ formatNumber(carbonData?.targetCard?.completionRate, 0) }}%
                </div>
              </div>
              <div class="target-card__meta">
                <div class="target-card__row">
                  <span>Annual Target</span>
                  <strong>{{ formatNumber(carbonData?.targetCard?.annualTargetValue, 0) }} tCO2</strong>
                </div>
                <div class="target-card__row">
                  <span>Emitted</span>
                  <strong>{{ formatNumber(carbonData?.targetCard?.emittedValue, 0) }} tCO2</strong>
                </div>
                <div class="target-card__row">
                  <span>Remaining Quota</span>
                  <strong :class="{ 'is-negative': Number(carbonData?.targetCard?.remainingValue || 0) < 0 }">
                    {{ formatNumber(carbonData?.targetCard?.remainingValue, 0) }} tCO2
                  </strong>
                </div>
              </div>
            </div>
          </FactoryPanel>
        </div>
      </template>
    </div>

    <ElDialog v-model="productionDialogVisible" title="新建生产计划" width="560px">
      <ElForm ref="productionFormRef" :model="productionForm" :rules="productionRules" label-width="92px">
        <ElFormItem label="计划编号" prop="planCode"><ElInput v-model="productionForm.planCode" /></ElFormItem>
        <ElFormItem label="产品名称" prop="productName"><ElInput v-model="productionForm.productName" /></ElFormItem>
        <ElFormItem label="批次编号" prop="batchCode"><ElInput v-model="productionForm.batchCode" /></ElFormItem>
        <ElFormItem label="产线区域" prop="lineName"><ElInput v-model="productionForm.lineName" /></ElFormItem>
        <ElFormItem label="计划数量" prop="plannedQuantity">
          <ElInputNumber v-model="productionForm.plannedQuantity" :min="1" class="dialog-full-width" />
        </ElFormItem>
        <ElFormItem label="操作员" prop="operatorName"><ElInput v-model="productionForm.operatorName" /></ElFormItem>
        <ElFormItem label="开始时间" prop="plannedStartTime">
          <ElDatePicker
            v-model="productionForm.plannedStartTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            class="dialog-full-width"
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="productionDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="submitProductionForm">确认创建</ElButton>
      </template>
    </ElDialog>

    <ElDialog v-model="deviceDialogVisible" title="添加设备" width="560px">
      <ElForm ref="deviceFormRef" :model="deviceForm" :rules="deviceRules" label-width="92px">
        <ElFormItem label="设备编码" prop="deviceCode"><ElInput v-model="deviceForm.deviceCode" /></ElFormItem>
        <ElFormItem label="设备名称" prop="deviceName"><ElInput v-model="deviceForm.deviceName" /></ElFormItem>
        <ElFormItem label="设备类别" prop="categoryName"><ElInput v-model="deviceForm.categoryName" /></ElFormItem>
        <ElFormItem label="设备型号" prop="modelName"><ElInput v-model="deviceForm.modelName" /></ElFormItem>
        <ElFormItem label="所属区域" prop="areaName"><ElInput v-model="deviceForm.areaName" /></ElFormItem>
        <ElFormItem label="运行状态" prop="runningStatus">
          <ElSelect v-model="deviceForm.runningStatus" class="dialog-full-width">
            <ElOption label="运行中" value="运行中" />
            <ElOption label="停机" value="停机" />
            <ElOption label="维保中" value="维保中" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="在线状态" prop="online">
          <ElSwitch v-model="deviceForm.online" />
        </ElFormItem>
        <ElFormItem label="效率(%)" prop="efficiencyRate">
          <ElInputNumber v-model="deviceForm.efficiencyRate" :min="0" :max="100" class="dialog-full-width" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="deviceDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="submitDeviceForm">确认新增</ElButton>
      </template>
    </ElDialog>

    <ElDialog v-model="maintenanceDialogVisible" title="新增维保计划" width="560px">
      <ElForm ref="maintenanceFormRef" :model="maintenanceForm" :rules="maintenanceRules" label-width="104px">
        <ElFormItem label="设备" prop="deviceId">
          <ElSelect v-model="maintenanceForm.deviceId" class="dialog-full-width" filterable>
            <ElOption v-for="item in deviceOptions" :key="item.value" :label="item.label" :value="item.value" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="计划名称" prop="planName"><ElInput v-model="maintenanceForm.planName" /></ElFormItem>
        <ElFormItem label="维保周期" prop="cycleType">
          <ElSelect v-model="maintenanceForm.cycleType" class="dialog-full-width">
            <ElOption label="周度" value="周度" />
            <ElOption label="月度" value="月度" />
            <ElOption label="季度" value="季度" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="执行日期" prop="nextExecuteDate">
          <ElDatePicker
            v-model="maintenanceForm.nextExecuteDate"
            type="date"
            value-format="YYYY-MM-DD"
            class="dialog-full-width"
          />
        </ElFormItem>
        <ElFormItem label="负责人" prop="ownerName"><ElInput v-model="maintenanceForm.ownerName" /></ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="maintenanceDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="submitMaintenanceForm">确认创建</ElButton>
      </template>
    </ElDialog>

    <ElDialog v-model="carbonTradeDialogVisible" title="登记碳交易" width="560px">
      <ElForm ref="carbonTradeFormRef" :model="carbonTradeForm" :rules="carbonTradeRules" label-width="92px">
        <ElFormItem label="交易编号" prop="tradeCode"><ElInput v-model="carbonTradeForm.tradeCode" /></ElFormItem>
        <ElFormItem label="交易类型" prop="tradeType">
          <ElSelect v-model="carbonTradeForm.tradeType" class="dialog-full-width">
            <ElOption label="买入" value="BUY" />
            <ElOption label="卖出" value="SELL" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="交易数量" prop="quantity">
          <ElInputNumber v-model="carbonTradeForm.quantity" :min="1" class="dialog-full-width" />
        </ElFormItem>
        <ElFormItem label="交易单价" prop="unitPrice">
          <ElInputNumber v-model="carbonTradeForm.unitPrice" :min="0" class="dialog-full-width" />
        </ElFormItem>
        <ElFormItem label="交易日期" prop="tradeDate">
          <ElDatePicker
            v-model="carbonTradeForm.tradeDate"
            type="date"
            value-format="YYYY-MM-DD"
            class="dialog-full-width"
          />
        </ElFormItem>
        <ElFormItem label="对手方" prop="counterparty"><ElInput v-model="carbonTradeForm.counterparty" /></ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="carbonTradeDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="submitCarbonTradeForm">确认登记</ElButton>
      </template>
    </ElDialog>
  </FactoryDashboardShell>
</template>

<style scoped lang="scss">
.collaboration-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-height: 0;
}

.collaboration-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 2px;
}

.collaboration-header__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.collaboration-header__search {
  width: 360px;
}

:deep(.collaboration-header__search .el-input__wrapper) {
  background: rgba(10, 20, 35, 0.82);
  box-shadow: 0 0 0 1px rgba(72, 129, 181, 0.2) inset;
}

.collaboration-header__time {
  padding: 0 4px;
  font-size: 14px;
  color: #d6ebff;
}

.collaboration-header__refresh {
  width: 40px;
  height: 40px;
}

.collaboration-header__user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-left: 12px;
  border-left: 1px solid rgba(76, 128, 171, 0.25);
}

.collaboration-header__avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  color: #bce9ff;
  border-radius: 999px;
  background: linear-gradient(180deg, rgba(45, 136, 255, 0.82), rgba(30, 107, 228, 0.82));
}

.collaboration-header__user-name {
  font-size: 14px;
  font-weight: 600;
  color: #f3fbff;
}

.collaboration-header__user-role {
  margin-top: 2px;
  font-size: 12px;
  color: rgba(194, 221, 244, 0.66);
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.main-tabs,
.sub-tabs {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.main-tabs {
  flex: 1;
  flex-wrap: nowrap;
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: thin;
}

.main-tabs::-webkit-scrollbar {
  height: 6px;
}

.main-tabs::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: rgba(72, 129, 181, 0.24);
}

.main-tabs__item,
.sub-tabs__item {
  padding: 10px 16px;
  font-size: 14px;
  color: rgba(205, 229, 247, 0.78);
  background: rgba(15, 28, 44, 0.88);
  border: 1px solid rgba(75, 118, 159, 0.24);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.main-tabs__item--active,
.sub-tabs__item--active {
  color: #ffffff;
  border-color: rgba(64, 158, 255, 0.52);
  background: linear-gradient(180deg, rgba(64, 158, 255, 0.92), rgba(38, 111, 226, 0.92));
  box-shadow: 0 10px 24px rgba(25, 87, 177, 0.28);
}

.sub-tabs__spacer {
  flex: 1;
}

.sub-tabs__button-group :deep(.el-button) {
  background: rgba(15, 28, 44, 0.88);
  border-color: rgba(75, 118, 159, 0.24);
}

.sub-tabs__date {
  width: 180px;
}

:deep(.sub-tabs__date .el-input__wrapper) {
  background: rgba(10, 20, 35, 0.82);
  box-shadow: 0 0 0 1px rgba(72, 129, 181, 0.2) inset;
}

.page-updated-at {
  margin-top: -8px;
  font-size: 12px;
  color: rgba(179, 208, 233, 0.68);
}

.dark-table {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(16, 30, 49, 0.92);
  --el-table-row-hover-bg-color: rgba(44, 94, 155, 0.14);
  --el-table-border-color: rgba(79, 120, 160, 0.18);
  --el-table-text-color: #dceeff;
  --el-table-header-text-color: rgba(200, 224, 244, 0.8);
}

:deep(.dark-table .el-table__inner-wrapper::before) {
  display: none;
}

:deep(.dark-table .el-table__body-wrapper .el-scrollbar__bar.is-horizontal),
:deep(.dark-table .el-table__body-wrapper .el-scrollbar__bar.is-vertical) {
  opacity: 0.35;
}

.progress-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-cell :deep(.el-progress) {
  flex: 1;
}

.alert-card-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.alert-card {
  padding: 16px;
  border: 1px solid rgba(123, 82, 152, 0.22);
  border-radius: 14px;
  background: rgba(14, 24, 39, 0.78);
}

.alert-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.alert-card__title {
  font-size: 15px;
  font-weight: 600;
  color: #ffb4b4;
}

.alert-card__meta {
  margin-top: 8px;
  font-size: 13px;
  color: rgba(188, 212, 232, 0.7);
}

.alert-card__action {
  margin-top: 10px;
  font-size: 13px;
  color: #7db7ff;
}

.batch-trace-layout {
  display: grid;
  grid-template-columns: minmax(280px, 360px) minmax(0, 1fr);
  gap: 16px;
}

.batch-trace-detail-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.batch-prototype-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.batch-prototype-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.batch-prototype-card__title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #ffffff;
}

.batch-prototype-card__subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: rgba(194, 221, 244, 0.68);
}

.batch-prototype-status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 88px;
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 600;
  border-radius: 12px;
}

.batch-prototype-status.is-success {
  color: #63d17d;
  background: rgba(34, 197, 94, 0.18);
}

.batch-prototype-status.is-warning {
  color: #f3c214;
  background: rgba(243, 194, 20, 0.16);
}

.batch-prototype-status.is-primary {
  color: #7db7ff;
  background: rgba(64, 158, 255, 0.16);
}

.batch-prototype-overview-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  align-items: stretch;
}

.batch-prototype-metric-card,
.batch-prototype-section,
.batch-prototype-list-item {
  padding: 16px;
  border-radius: 16px;
  background: rgba(31, 41, 55, 0.5);
}

.batch-prototype-metric-card {
  display: flex;
  flex-direction: column;
  min-height: 140px;
  height: clamp(140px, 18vh, 190px);
}

.batch-prototype-metric-card__body {
  flex: 1;
  align-content: center;
  overflow: auto;
}

.batch-prototype-section-title,
.batch-prototype-section-heading {
  margin: 0 0 14px;
  font-size: 14px;
  font-weight: 600;
  color: #ffffff;
}

.batch-prototype-section-heading {
  display: flex;
  align-items: center;
  gap: 8px;
}

.batch-prototype-section-icon {
  font-size: 16px;
}

.batch-prototype-section-icon.is-blue {
  color: #60a5fa;
}

.batch-prototype-section-icon.is-purple {
  color: #c084fc;
}

.batch-prototype-section-icon.is-green {
  color: #4ade80;
}

.batch-prototype-metric-grid {
  display: grid;
  gap: 12px;
}

.batch-prototype-metric-grid--three {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.batch-prototype-metric-item {
  text-align: center;
}

.batch-prototype-metric-item p {
  margin: 0;
  font-size: 12px;
  color: rgba(194, 221, 244, 0.62);
}

.batch-prototype-metric-item strong {
  display: block;
  margin-top: 8px;
  font-size: 22px;
  font-weight: 700;
  color: #ffffff;
}

.batch-prototype-metric-item strong.is-highlight {
  color: #4ade80;
}

.batch-prototype-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  min-height: 72px;
  color: rgba(194, 221, 244, 0.52);
  font-size: 13px;
  text-align: center;
}

.batch-prototype-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.batch-prototype-scroll-area {
  min-height: 92px;
  max-height: 240px;
  overflow: auto;
}

.batch-prototype-list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.batch-prototype-list-item__main {
  display: flex;
  align-items: center;
  gap: 12px;
}

.batch-prototype-list-item__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 999px;
}

.batch-prototype-list-item__badge.is-blue {
  color: #60a5fa;
  background: rgba(59, 130, 246, 0.18);
}

.batch-prototype-list-item__badge.is-purple {
  color: #c084fc;
  background: rgba(168, 85, 247, 0.18);
}

.batch-prototype-list-item__name {
  margin: 0;
  font-size: 14px;
  color: #ffffff;
}

.batch-prototype-list-item__desc,
.batch-prototype-list-item__aside p {
  margin: 4px 0 0;
  font-size: 12px;
  color: rgba(194, 221, 244, 0.62);
}

.batch-prototype-list-item__aside {
  text-align: right;
}

.batch-prototype-list-item__aside p:first-child {
  color: rgba(194, 221, 244, 0.76);
}

.batch-prototype-table-wrap {
  overflow-x: auto;
}

.batch-prototype-table {
  width: 100%;
  border-collapse: collapse;
}

.batch-prototype-table th,
.batch-prototype-table td {
  padding: 10px 8px;
  text-align: left;
  border-bottom: 1px solid rgba(55, 65, 81, 0.5);
}

.batch-prototype-table th {
  font-size: 12px;
  font-weight: 400;
  color: rgba(194, 221, 244, 0.62);
}

.batch-prototype-table td {
  font-size: 13px;
  color: #ffffff;
}

.batch-prototype-table-tag {
  display: inline-flex;
  padding: 2px 8px;
  color: #4ade80;
  font-size: 12px;
  border-radius: 999px;
  background: rgba(34, 197, 94, 0.18);
}

.batch-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.batch-list-card {
  padding: 16px;
  text-align: left;
  border: 1px solid rgba(78, 137, 191, 0.16);
  border-radius: 16px;
  background: rgba(12, 23, 39, 0.82);
  cursor: pointer;
  transition: all 0.2s ease;
}

.batch-list-card--active {
  border-color: rgba(64, 158, 255, 0.56);
  box-shadow: 0 0 0 1px rgba(64, 158, 255, 0.2) inset;
}

.batch-list-card__header,
.trace-record-item__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.batch-list-card__code,
.batch-list-card__name {
  color: #f4fbff;
  font-weight: 600;
}

.batch-list-card__name {
  margin-top: 10px;
  font-size: 16px;
}

.batch-list-card__meta,
.batch-list-card__time,
.trace-record-item__footer {
  margin-top: 8px;
  font-size: 13px;
  color: rgba(194, 221, 244, 0.72);
}

.batch-list-card__stats,
.trace-record-item__metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 16px;
  margin-top: 10px;
  font-size: 13px;
  color: #bfe7ff;
}

.batch-summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.batch-summary-card,
.batch-summary-pair,
.trace-record-item {
  padding: 14px;
  border-radius: 14px;
  background: rgba(13, 25, 39, 0.82);
}

.batch-summary-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.batch-summary-card span,
.batch-summary-pair span {
  font-size: 12px;
  color: rgba(194, 221, 244, 0.66);
}

.batch-summary-card strong,
.batch-summary-pair strong {
  color: #f4fbff;
  font-size: 18px;
  font-weight: 700;
}

.batch-summary-card small {
  color: rgba(194, 221, 244, 0.7);
}

.batch-summary-pair {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.batch-trace-dual-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.batch-trace-triple-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.trace-record-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.triple-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.rank-list,
.suggestion-list,
.source-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rank-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(13, 25, 39, 0.78);
}

.rank-item__name {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #e2f2ff;
}

.rank-item__index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  color: #07111f;
  font-size: 12px;
  font-weight: 700;
  border-radius: 999px;
  background: #f3c214;
}

.rank-item__value {
  color: #f3c214;
  font-weight: 600;
}

.suggestion-card {
  padding: 14px;
  border-radius: 14px;
  background: rgba(14, 24, 39, 0.78);
  border: 1px solid rgba(67, 130, 97, 0.22);
}

.suggestion-card__title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: #def4ff;
  font-weight: 600;
}

.suggestion-card__content {
  margin-top: 10px;
  font-size: 13px;
  line-height: 1.6;
  color: rgba(196, 218, 236, 0.78);
}

.suggestion-card__footer {
  margin-top: 12px;
}

.device-layout,
.carbon-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.65fr) minmax(320px, 1fr);
  gap: 16px;
}

.device-card-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.device-card {
  padding: 16px;
  text-align: left;
  border: 1px solid rgba(78, 137, 191, 0.16);
  border-radius: 16px;
  background: rgba(12, 23, 39, 0.82);
  cursor: pointer;
  transition: all 0.2s ease;
}

.device-card--active {
  border-color: rgba(64, 158, 255, 0.56);
  box-shadow: 0 0 0 1px rgba(64, 158, 255, 0.2) inset;
}

.device-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.device-card__icon,
.device-card__arrow {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  color: #8cd8ff;
  border-radius: 10px;
  background: rgba(27, 53, 84, 0.62);
}

.device-card__name {
  margin-top: 12px;
  color: #f4fbff;
  font-size: 18px;
  font-weight: 600;
}

.device-card__desc,
.device-card__meta,
.device-card__status-row,
.device-card__efficiency {
  margin-top: 8px;
  font-size: 13px;
  color: rgba(194, 221, 244, 0.7);
}

.device-card__status-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.device-card__status.is-online {
  color: #63d17d;
}

.device-card__status.is-offline {
  color: #ff8f8f;
}

.device-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 100%;
}

.device-detail__title {
  font-size: 22px;
  font-weight: 700;
  color: #f3fbff;
}

.device-detail__pair {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 10px;
  font-size: 14px;
  color: rgba(194, 221, 244, 0.72);
  border-bottom: 1px solid rgba(79, 120, 160, 0.14);
}

.device-detail__pair strong {
  color: #e5f4ff;
}

.device-detail__remark {
  margin-top: auto;
  padding: 14px;
  border-radius: 14px;
  background: rgba(13, 25, 39, 0.82);
  color: rgba(194, 221, 244, 0.78);
  line-height: 1.7;
}

.source-item {
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(13, 25, 39, 0.82);
}

.source-item__name {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #e6f4ff;
  font-weight: 600;
}

.source-item__name small {
  font-size: 12px;
  color: rgba(194, 221, 244, 0.62);
}

.source-item__value,
.source-item__percent {
  margin-top: 10px;
  color: rgba(194, 221, 244, 0.78);
}

.source-item__bar {
  height: 8px;
  margin-top: 10px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(72, 90, 116, 0.28);
}

.source-item__bar-fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #34d399, #60a5fa);
}

.target-card {
  display: flex;
  align-items: center;
  gap: 24px;
  min-height: 240px;
}

.target-card__ring {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 150px;
  height: 150px;
  flex-shrink: 0;
  color: #f3fbff;
  font-size: 32px;
  font-weight: 700;
  border-radius: 999px;
  background:
    radial-gradient(circle, rgba(9, 19, 33, 0.92) 62%, transparent 64%),
    conic-gradient(#34d399 0 72%, rgba(42, 60, 81, 0.4) 72% 100%);
}

.target-card__meta {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 14px;
}

.target-card__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: rgba(194, 221, 244, 0.74);
}

.target-card__row strong {
  color: #f3fbff;
}

.target-card__row .is-negative {
  color: #41df83;
}

.dialog-full-width {
  width: 100%;
}

:deep(.el-dialog) {
  background: #091521;
  border: 1px solid rgba(78, 170, 235, 0.22);
  border-radius: 18px;
}

:deep(.el-dialog__title),
:deep(.el-form-item__label) {
  color: #e5f4ff;
}

:deep(.el-dialog__body) {
  color: rgba(194, 221, 244, 0.82);
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner),
:deep(.el-select__wrapper) {
  background: rgba(10, 20, 35, 0.86);
  box-shadow: 0 0 0 1px rgba(72, 129, 181, 0.2) inset;
}

@media (max-width: 1440px) {
  .metric-grid {
    gap: 12px;
  }

  .device-card-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1200px) {
  .device-layout,
  .carbon-grid,
  .batch-trace-layout,
  .batch-prototype-overview-grid,
  .alert-card-grid {
    grid-template-columns: 1fr;
  }

  .collaboration-header {
    flex-direction: column;
    align-items: stretch;
  }

  .collaboration-header__actions {
    justify-content: flex-start;
  }

  .collaboration-header__search {
    width: 100%;
  }
}

@media (max-width: 900px) {
  .triple-grid,
  .batch-prototype-metric-grid--three,
  .device-card-grid {
    grid-template-columns: 1fr;
  }

  .target-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .batch-prototype-card__header,
  .batch-prototype-list-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .batch-prototype-list-item__aside {
    text-align: left;
  }
}
</style>
