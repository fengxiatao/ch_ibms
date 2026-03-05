<script setup lang="ts">
import type { FormInstance } from 'element-plus'
import EnergyPageContainer from '../components/EnergyPageContainer.vue'
import EnergyPageHeader from '../components/EnergyPageHeader.vue'

defineOptions({ name: 'EnergySystemSettings' })

type MenuKey = 'basic' | 'alarmCommon' | 'warningStrategy' | 'billing' | 'partition'
type EnergyType = 'electricity' | 'water' | 'gas'

const activeMenu = ref<MenuKey>('basic')

const saveRef = ref<FormInstance>()

const basicForm = reactive({
  buildingName: '智慧园区一期',
  buildingType: 'office',
  organization: '集团总部',
  totalArea: 50000
})

const quotaForm = reactive({
  quotaScope: 'device' as 'device' | 'area',
  energyType: 'electricity' as EnergyType,
  deviceId: '',
  areaId: '',
  devicePower: undefined as number | undefined,
  deviceCapacity: undefined as number | undefined,
  deviceFlow: undefined as number | undefined,
  deviceDailyVolume: undefined as number | undefined,
  areaPower: undefined as number | undefined,
  areaCapacity: undefined as number | undefined,
  areaWaterSupply: undefined as number | undefined
})

const deviceOptions = [
  { label: '1号配电柜 (PDG-001)', value: 'D001' },
  { label: '2号配电柜 (PDG-002)', value: 'D002' },
  { label: '主供水泵 (WP-001)', value: 'D003' },
  { label: '备用供水泵 (WP-002)', value: 'D004' },
  { label: '燃气总表 (GM-001)', value: 'D005' },
  { label: '锅炉房燃气表 (GM-002)', value: 'D006' }
]

const areaOptions = [
  { label: '办公区', value: 'A001' },
  { label: '生产区', value: 'A002' },
  { label: '配套设施区', value: 'A003' },
  { label: '仓储区', value: 'A004' },
  { label: '宿舍区', value: 'A005' },
  { label: '食堂', value: 'A006' }
]

interface QuotaItem {
  id: string
  scope: 'device' | 'area'
  scopeName: string
  energyType: EnergyType
  quotaText: string
}

const configuredQuotaList = ref<QuotaItem[]>([
  {
    id: 'Q1',
    scope: 'device',
    scopeName: '1号配电柜 (PDG-001)',
    energyType: 'electricity',
    quotaText: '额定功率 120kW / 额定容量 150kVA'
  },
  {
    id: 'Q2',
    scope: 'area',
    scopeName: '办公区',
    energyType: 'electricity',
    quotaText: '总额定功率 600kW / 总额定容量 800kVA'
  }
])

const clearDeviceQuota = () => {
  quotaForm.devicePower = undefined
  quotaForm.deviceCapacity = undefined
  quotaForm.deviceFlow = undefined
  quotaForm.deviceDailyVolume = undefined
}

const saveDeviceQuota = () => {
  const device = deviceOptions.find((d) => d.value === quotaForm.deviceId)
  if (!device) {
    ElMessage.warning('请选择设备')
    return
  }
  const quotaText =
    quotaForm.energyType === 'electricity'
      ? `额定功率 ${quotaForm.devicePower ?? '-'}kW / 额定容量 ${quotaForm.deviceCapacity ?? '-'}kVA`
      : `额定流量 ${quotaForm.deviceFlow ?? '-'}m³/h / 日最大供应量 ${quotaForm.deviceDailyVolume ?? '-'}m³/日`
  configuredQuotaList.value.unshift({
    id: `Q${Date.now()}`,
    scope: 'device',
    scopeName: device.label,
    energyType: quotaForm.energyType,
    quotaText
  })
  ElMessage.success('已保存设备额度（示例）')
}

const clearAreaQuota = () => {
  quotaForm.areaPower = undefined
  quotaForm.areaCapacity = undefined
  quotaForm.areaWaterSupply = undefined
}

const saveAreaQuota = () => {
  const area = areaOptions.find((d) => d.value === quotaForm.areaId)
  if (!area) {
    ElMessage.warning('请选择区域')
    return
  }
  const quotaText =
    quotaForm.energyType === 'electricity'
      ? `总额定功率 ${quotaForm.areaPower ?? '-'}kW / 总额定容量 ${quotaForm.areaCapacity ?? '-'}kVA`
      : `区域总日供水量 ${quotaForm.areaWaterSupply ?? '-'}m³/日`
  configuredQuotaList.value.unshift({
    id: `Q${Date.now()}`,
    scope: 'area',
    scopeName: area.label,
    energyType: quotaForm.energyType,
    quotaText
  })
  ElMessage.success('已保存区域额度（示例）')
}

const resetBasicSettings = () => {
  basicForm.buildingName = '智慧园区一期'
  basicForm.buildingType = 'office'
  basicForm.organization = '集团总部'
  basicForm.totalArea = 50000
  ElMessage.success('已重置')
}

const saveBasicSettings = () => {
  ElMessage.success('已保存基础信息（示例）')
}

const alarmCommonForm = reactive({
  suppressMinutes: 30,
  autoRecoverConfirmMinutes: 5,
  dndStart: '22:00',
  dndEnd: '08:00',
  escalateMinutes: 30
})

const resetAlarmSettings = () => {
  alarmCommonForm.suppressMinutes = 30
  alarmCommonForm.autoRecoverConfirmMinutes = 5
  alarmCommonForm.dndStart = '22:00'
  alarmCommonForm.dndEnd = '08:00'
  alarmCommonForm.escalateMinutes = 30
  ElMessage.success('已恢复默认')
}

const saveAlarmSettings = () => {
  ElMessage.success('告警通用设置保存成功（示例）')
}

type StrategyApplicableType = 'device' | 'area'
type StrategyStatus = 'pending' | 'active' | 'inactive' | 'expired'

interface StrategyItem {
  id: string
  energyType: EnergyType
  name: string
  applicableType: StrategyApplicableType
  targetName: string
  thresholdText: string
  effectiveText: string
  status: StrategyStatus
}

const strategyState = reactive({
  energyType: 'electricity' as EnergyType,
  applicableType: 'all' as 'all' | StrategyApplicableType,
  status: 'all' as 'all' | StrategyStatus,
  keyword: ''
})

const strategyList = ref<StrategyItem[]>([
  {
    id: 'S1',
    energyType: 'electricity',
    name: '机房电力过载预警',
    applicableType: 'device',
    targetName: '1号机房配电柜',
    thresholdText: '800kWh (P0)',
    effectiveText: '2026-02-01 至 2026-12-31',
    status: 'active'
  },
  {
    id: 'S2',
    energyType: 'electricity',
    name: '办公区电力节能预警',
    applicableType: 'area',
    targetName: '办公区',
    thresholdText: '5000kWh/天 (P1)',
    effectiveText: '2026-02-05 至 2026-12-31',
    status: 'active'
  },
  {
    id: 'S3',
    energyType: 'water',
    name: '水泵房流量异常预警',
    applicableType: 'device',
    targetName: '水泵房监测器',
    thresholdText: '15m³/h (P0)',
    effectiveText: '2026-01-20 至 2026-06-30',
    status: 'inactive'
  },
  {
    id: 'S4',
    energyType: 'gas',
    name: '燃气泄漏预警',
    applicableType: 'area',
    targetName: '配套设施区',
    thresholdText: '25m³/h (P0)',
    effectiveText: '2026-03-01 至 2026-12-31',
    status: 'pending'
  }
])

const filteredStrategyList = computed(() => {
  return strategyList.value.filter((s) => {
    if (s.energyType !== strategyState.energyType) return false
    if (strategyState.applicableType !== 'all' && s.applicableType !== strategyState.applicableType)
      return false
    if (strategyState.status !== 'all' && s.status !== strategyState.status) return false
    if (strategyState.keyword.trim()) {
      const k = strategyState.keyword.trim()
      const text = `${s.name} ${s.targetName} ${s.thresholdText}`
      if (!text.includes(k)) return false
    }
    return true
  })
})

const strategyStatusText: Record<StrategyStatus, string> = {
  pending: '未开始',
  active: '已启用',
  inactive: '已停用',
  expired: '已过期'
}

const strategyStatusTagType: Record<StrategyStatus, any> = {
  pending: 'info',
  active: 'success',
  inactive: 'warning',
  expired: 'danger'
}

const resetStrategyFilters = () => {
  strategyState.applicableType = 'all'
  strategyState.status = 'all'
  strategyState.keyword = ''
}

const strategyDialog = reactive({
  visible: false,
  mode: 'add' as 'add' | 'edit' | 'view',
  form: {
    id: '',
    energyType: 'electricity' as EnergyType,
    name: '',
    applicableType: 'device' as StrategyApplicableType,
    targetName: '',
    thresholdText: '',
    effectiveRange: [] as string[],
    status: 'active' as StrategyStatus
  }
})

const openAddStrategy = () => {
  strategyDialog.visible = true
  strategyDialog.mode = 'add'
  strategyDialog.form = {
    id: '',
    energyType: strategyState.energyType,
    name: '',
    applicableType: 'device',
    targetName: '',
    thresholdText: '',
    effectiveRange: [],
    status: 'active'
  }
}

const openViewStrategy = (row: StrategyItem) => {
  strategyDialog.visible = true
  strategyDialog.mode = 'view'
  strategyDialog.form = {
    id: row.id,
    energyType: row.energyType,
    name: row.name,
    applicableType: row.applicableType,
    targetName: row.targetName,
    thresholdText: row.thresholdText,
    effectiveRange: row.effectiveText.split(' 至 '),
    status: row.status
  }
}

const openEditStrategy = (row: StrategyItem) => {
  strategyDialog.visible = true
  strategyDialog.mode = 'edit'
  strategyDialog.form = {
    id: row.id,
    energyType: row.energyType,
    name: row.name,
    applicableType: row.applicableType,
    targetName: row.targetName,
    thresholdText: row.thresholdText,
    effectiveRange: row.effectiveText.split(' 至 '),
    status: row.status
  }
}

const deleteStrategy = (row: StrategyItem) => {
  strategyList.value = strategyList.value.filter((s) => s.id !== row.id)
  ElMessage.success('已删除（示例）')
}

const submitStrategy = () => {
  if (!strategyDialog.form.name.trim()) {
    ElMessage.warning('请输入策略名称')
    return
  }
  if (!strategyDialog.form.targetName.trim()) {
    ElMessage.warning('请输入监控对象')
    return
  }
  if (!strategyDialog.form.thresholdText.trim()) {
    ElMessage.warning('请输入预警阈值')
    return
  }
  const effectiveText =
    strategyDialog.form.effectiveRange.length === 2
      ? `${strategyDialog.form.effectiveRange[0]} 至 ${strategyDialog.form.effectiveRange[1]}`
      : '未设置'
  if (strategyDialog.mode === 'add') {
    strategyList.value.unshift({
      id: `S${Date.now()}`,
      energyType: strategyDialog.form.energyType,
      name: strategyDialog.form.name,
      applicableType: strategyDialog.form.applicableType,
      targetName: strategyDialog.form.targetName,
      thresholdText: strategyDialog.form.thresholdText,
      effectiveText,
      status: strategyDialog.form.status
    })
    ElMessage.success('已新增（示例）')
  } else if (strategyDialog.mode === 'edit') {
    strategyList.value = strategyList.value.map((s) =>
      s.id === strategyDialog.form.id
        ? {
            ...s,
            energyType: strategyDialog.form.energyType,
            name: strategyDialog.form.name,
            applicableType: strategyDialog.form.applicableType,
            targetName: strategyDialog.form.targetName,
            thresholdText: strategyDialog.form.thresholdText,
            effectiveText,
            status: strategyDialog.form.status
          }
        : s
    )
    ElMessage.success('已保存（示例）')
  }
  strategyDialog.visible = false
}

type BillingType = 'electricity' | 'water' | 'gas'
type ElectricityBillingMode = 'time-of-use' | 'fixed'
type SeasonType = 'summer' | 'winter' | 'shoulder'

const billingState = reactive({
  activeType: 'electricity' as BillingType,
  electricityMode: 'time-of-use' as ElectricityBillingMode,
  season: 'summer' as SeasonType,
  touRemark: '夏季尖峰时段为19:00-22:00，主要考虑空调负荷高峰',
  fixedPrice: 0.65,
  fixedEffectiveDate: '2026-01-01',
  waterPrice: 4.5,
  waterEffectiveDate: '2026-01-01',
  gasPrice: 3.2,
  gasEffectiveDate: '2026-01-01'
})

const electricityTou = reactive({
  jian: { start: '19:00', end: '22:00', price: 1.2 },
  feng: { start: '09:00', end: '12:00', price: 0.9 },
  ping: { start: '08:00', end: '09:00', price: 0.6 },
  gu: { start: '23:00', end: '07:00', price: 0.3 }
})

const electricityRuleList = ref([
  { id: 'E1', name: '夏季尖峰平谷规则', mode: '分时计费', status: '生效中' },
  { id: 'E2', name: '冬季尖峰平谷规则', mode: '分时计费', status: '未生效' }
])

const saveBilling = (type: BillingType) => {
  if (type === 'electricity') ElMessage.success('已保存电价规则（示例）')
  if (type === 'water') ElMessage.success('已保存水费规则（示例）')
  if (type === 'gas') ElMessage.success('已保存燃气费规则（示例）')
}

type PartitionType = 'building' | 'floor' | 'area' | 'room'

interface PartitionRow {
  id: string
  name: string
  type: PartitionType
  parentId: string | null
  parentName: string
  meters: string[]
}

const partitionState = reactive({
  selectedId: 'root',
  typeFilter: 'all' as 'all' | PartitionType,
  keyword: ''
})

const partitionTreeData = ref([
  {
    id: 'root',
    label: '智慧园区一期',
    children: [
      {
        id: 'F1',
        label: '1层',
        children: [
          { id: 'F1-01', label: '1层大堂' },
          { id: 'F1-02', label: '1层接待区' }
        ]
      },
      {
        id: 'F2',
        label: '2层',
        children: [
          { id: 'F2-01', label: '2层办公区A' },
          { id: 'F2-02', label: '2层办公区B' },
          { id: 'F2-03', label: '2层会议室' }
        ]
      },
      {
        id: 'F3',
        label: '3层',
        children: [{ id: 'F3-01', label: '3层会议室集群' }]
      },
      {
        id: 'B1',
        label: '地下1层',
        children: [{ id: 'B1-01', label: '地下停车场' }]
      }
    ]
  }
])

const partitionRows = ref<PartitionRow[]>([
  {
    id: '1',
    name: '智慧园区一期',
    type: 'building',
    parentId: null,
    parentName: '-',
    meters: ['总电表M-001', '总水表W-001']
  },
  {
    id: '2',
    name: '1层',
    type: 'floor',
    parentId: '1',
    parentName: '智慧园区一期',
    meters: ['1层电表M-101']
  },
  {
    id: '3',
    name: '1层大堂',
    type: 'area',
    parentId: '2',
    parentName: '1层',
    meters: ['大堂电表M-102', '大堂水表W-102']
  },
  {
    id: '4',
    name: '2层',
    type: 'floor',
    parentId: '1',
    parentName: '智慧园区一期',
    meters: ['2层电表M-201']
  },
  {
    id: '5',
    name: '2层办公区A',
    type: 'area',
    parentId: '4',
    parentName: '2层',
    meters: ['办公区A电表M-202', '办公区A水表W-202']
  },
  {
    id: '6',
    name: '2层办公区B',
    type: 'area',
    parentId: '4',
    parentName: '2层',
    meters: ['办公区B电表M-203']
  }
])

const partitionTypeText: Record<PartitionType, string> = {
  building: '建筑',
  floor: '楼层',
  area: '功能区域',
  room: '房间'
}

const partitionTypeTagType: Record<PartitionType, any> = {
  building: 'primary',
  floor: 'warning',
  area: 'success',
  room: 'info'
}

const filteredPartitionRows = computed(() => {
  return partitionRows.value.filter((r) => {
    if (partitionState.selectedId !== 'root') {
      if (r.parentId !== partitionState.selectedId && r.id !== partitionState.selectedId)
        return false
    }
    if (partitionState.typeFilter !== 'all' && r.type !== partitionState.typeFilter) return false
    if (partitionState.keyword.trim()) {
      const k = partitionState.keyword.trim()
      if (!r.name.includes(k) && !r.parentName.includes(k) && !r.meters.join(' ').includes(k))
        return false
    }
    return true
  })
})

const partitionDialog = reactive({
  visible: false,
  mode: 'add' as 'add' | 'edit',
  form: {
    id: '',
    name: '',
    type: 'area' as PartitionType,
    parentId: '1',
    meters: [] as string[]
  }
})

const meterOptions = [
  { label: '总电表M-001', value: '总电表M-001' },
  { label: '总水表W-001', value: '总水表W-001' },
  { label: '1层电表M-101', value: '1层电表M-101' },
  { label: '大堂电表M-102', value: '大堂电表M-102' },
  { label: '大堂水表W-102', value: '大堂水表W-102' },
  { label: '2层电表M-201', value: '2层电表M-201' },
  { label: '办公区A电表M-202', value: '办公区A电表M-202' },
  { label: '办公区A水表W-202', value: '办公区A水表W-202' },
  { label: '办公区B电表M-203', value: '办公区B电表M-203' }
]

const openAddPartition = () => {
  partitionDialog.visible = true
  partitionDialog.mode = 'add'
  partitionDialog.form = { id: '', name: '', type: 'area', parentId: '1', meters: [] }
}

const openEditPartition = (row: PartitionRow) => {
  partitionDialog.visible = true
  partitionDialog.mode = 'edit'
  partitionDialog.form = {
    id: row.id,
    name: row.name,
    type: row.type,
    parentId: row.parentId || '1',
    meters: [...row.meters]
  }
}

const deletePartition = (row: PartitionRow) => {
  partitionRows.value = partitionRows.value.filter((r) => r.id !== row.id)
  ElMessage.success('已删除（示例）')
}

const submitPartition = () => {
  if (!partitionDialog.form.name.trim()) {
    ElMessage.warning('请输入分区名称')
    return
  }
  const parentRow = partitionRows.value.find((r) => r.id === partitionDialog.form.parentId)
  const parentName = parentRow?.name || '-'
  if (partitionDialog.mode === 'add') {
    partitionRows.value.unshift({
      id: `${Date.now()}`,
      name: partitionDialog.form.name,
      type: partitionDialog.form.type,
      parentId: partitionDialog.form.parentId || null,
      parentName,
      meters: [...partitionDialog.form.meters]
    })
    ElMessage.success('已新增关联（示例）')
  } else {
    partitionRows.value = partitionRows.value.map((r) =>
      r.id === partitionDialog.form.id
        ? {
            ...r,
            name: partitionDialog.form.name,
            type: partitionDialog.form.type,
            parentId: partitionDialog.form.parentId,
            parentName,
            meters: [...partitionDialog.form.meters]
          }
        : r
    )
    ElMessage.success('已保存（示例）')
  }
  partitionDialog.visible = false
}
</script>

<template>
  <EnergyPageContainer>
    <EnergyPageHeader title="系统设置" subtitle="基础信息、告警、预警策略、计费与分区配置">
      <template #actions>
        <ElButton type="primary" @click="ElMessage.success('已保存（示例）')">
          <Icon icon="ep:check" class="mr-5px" /> 保存配置
        </ElButton>
      </template>
    </EnergyPageHeader>

    <ElTabs v-model="activeMenu" tab-position="left" class="settings-tabs">
      <ElTabPane label="基础信息配置" name="basic">
        <div class="page-title">📝 基础信息配置</div>
        <div class="page-desc"
          >配置系统基础信息，建筑名称和建筑面积将作为系统标识和能源管理的基础数据。</div
        >

        <div class="grid-2">
          <ElCard shadow="never">
            <div class="card-title">🏢 建筑基础信息</div>
            <ElForm :model="basicForm" label-position="top">
              <ElFormItem label="建筑名称" required>
                <ElInput
                  v-model="basicForm.buildingName"
                  placeholder="请输入建筑名称，如：智慧园区A区"
                />
                <div class="hint">建筑名称将用于报表导出和系统标识</div>
              </ElFormItem>
              <ElFormItem label="建筑类型">
                <ElSelect v-model="basicForm.buildingType">
                  <ElOption label="办公建筑" value="office" />
                  <ElOption label="商业建筑" value="commercial" />
                  <ElOption label="工业建筑" value="industrial" />
                  <ElOption label="综合建筑" value="mixed" />
                  <ElOption label="公共建筑" value="public" />
                </ElSelect>
              </ElFormItem>
              <ElFormItem label="所属组织">
                <ElInput v-model="basicForm.organization" placeholder="请输入所属组织" />
              </ElFormItem>
            </ElForm>
          </ElCard>

          <ElCard shadow="never">
            <div class="card-title">📐 建筑面积配置</div>
            <ElForm :model="basicForm" label-position="top">
              <ElFormItem label="总建筑面积 (m²)" required>
                <ElInputNumber v-model="basicForm.totalArea" class="w-full" :min="0" />
                <div class="hint">建筑面积用于系统基础数据统计</div>
              </ElFormItem>
            </ElForm>
          </ElCard>
        </div>

        <ElCard shadow="never" class="mt-16px">
          <div class="card-title">⚡ 能源系统额度配置</div>

          <div class="quota-header">
            <ElRadioGroup v-model="quotaForm.quotaScope">
              <ElRadioButton label="device">🔌 按设备设置</ElRadioButton>
              <ElRadioButton label="area">🏢 按区域设置</ElRadioButton>
            </ElRadioGroup>

            <ElTabs v-model="quotaForm.energyType" class="energy-tabs">
              <ElTabPane label="⚡ 电力" name="electricity" />
              <ElTabPane label="💧 用水" name="water" />
              <ElTabPane label="🔥 燃气" name="gas" />
            </ElTabs>
          </div>

          <div v-if="quotaForm.quotaScope === 'device'" class="quota-panel">
            <ElForm label-position="top">
              <ElFormItem label="选择设备">
                <ElSelect
                  v-model="quotaForm.deviceId"
                  placeholder="请选择设备..."
                  clearable
                  filterable
                >
                  <ElOption
                    v-for="d in deviceOptions"
                    :key="d.value"
                    :label="d.label"
                    :value="d.value"
                  />
                </ElSelect>
              </ElFormItem>

              <div v-if="quotaForm.deviceId" class="grid-2">
                <template v-if="quotaForm.energyType === 'electricity'">
                  <ElFormItem label="额定功率 (kW)">
                    <ElInputNumber v-model="quotaForm.devicePower" class="w-full" :min="0" />
                  </ElFormItem>
                  <ElFormItem label="额定容量 (kVA)">
                    <ElInputNumber v-model="quotaForm.deviceCapacity" class="w-full" :min="0" />
                  </ElFormItem>
                </template>
                <template v-else>
                  <ElFormItem label="额定流量 (m³/h)">
                    <ElInputNumber v-model="quotaForm.deviceFlow" class="w-full" :min="0" />
                  </ElFormItem>
                  <ElFormItem label="日最大供应量 (m³/日)">
                    <ElInputNumber v-model="quotaForm.deviceDailyVolume" class="w-full" :min="0" />
                  </ElFormItem>
                </template>
              </div>

              <div v-if="quotaForm.deviceId" class="actions-row">
                <ElButton @click="clearDeviceQuota">清空</ElButton>
                <ElButton type="primary" @click="saveDeviceQuota">保存设备额度</ElButton>
              </div>
            </ElForm>
          </div>

          <div v-else class="quota-panel">
            <ElForm label-position="top">
              <ElFormItem label="选择区域">
                <ElSelect
                  v-model="quotaForm.areaId"
                  placeholder="请选择区域..."
                  clearable
                  filterable
                >
                  <ElOption
                    v-for="a in areaOptions"
                    :key="a.value"
                    :label="a.label"
                    :value="a.value"
                  />
                </ElSelect>
              </ElFormItem>

              <div v-if="quotaForm.areaId" class="grid-2">
                <template v-if="quotaForm.energyType === 'electricity'">
                  <ElFormItem label="区域总额定功率 (kW)">
                    <ElInputNumber v-model="quotaForm.areaPower" class="w-full" :min="0" />
                  </ElFormItem>
                  <ElFormItem label="区域总额定容量 (kVA)">
                    <ElInputNumber v-model="quotaForm.areaCapacity" class="w-full" :min="0" />
                  </ElFormItem>
                </template>
                <template v-else>
                  <ElFormItem label="区域总日供水量 (m³/日)">
                    <ElInputNumber v-model="quotaForm.areaWaterSupply" class="w-full" :min="0" />
                  </ElFormItem>
                </template>
              </div>

              <div v-if="quotaForm.areaId" class="actions-row">
                <ElButton @click="clearAreaQuota">清空</ElButton>
                <ElButton type="primary" @click="saveAreaQuota">保存区域额度</ElButton>
              </div>
            </ElForm>
          </div>

          <ElCard shadow="never" class="mt-16px">
            <div class="sub-title">已配置额度</div>
            <ElTable :data="configuredQuotaList" size="small">
              <ElTableColumn label="对象" min-width="220">
                <template #default="{ row }">
                  <span class="font-700">{{ row.scopeName }}</span>
                </template>
              </ElTableColumn>
              <ElTableColumn label="维度" width="90">
                <template #default="{ row }">
                  <ElTag
                    size="small"
                    :type="row.scope === 'device' ? 'info' : 'warning'"
                    effect="plain"
                  >
                    {{ row.scope === 'device' ? '设备' : '区域' }}
                  </ElTag>
                </template>
              </ElTableColumn>
              <ElTableColumn label="能源类型" width="110">
                <template #default="{ row }">
                  <ElTag size="small" effect="plain">
                    {{
                      row.energyType === 'electricity'
                        ? '电力'
                        : row.energyType === 'water'
                          ? '用水'
                          : '燃气'
                    }}
                  </ElTag>
                </template>
              </ElTableColumn>
              <ElTableColumn label="额度" prop="quotaText" min-width="260" show-overflow-tooltip />
              <ElTableColumn label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <ElButton
                    link
                    type="danger"
                    @click="
                      configuredQuotaList = configuredQuotaList.filter((x) => x.id !== row.id)
                    "
                  >
                    删除
                  </ElButton>
                </template>
              </ElTableColumn>
            </ElTable>
          </ElCard>
        </ElCard>

        <div class="footer-actions">
          <ElButton @click="resetBasicSettings"
            ><Icon icon="ep:refresh" class="mr-5px" /> 重置</ElButton
          >
          <ElButton type="primary" @click="saveBasicSettings"
            ><Icon icon="ep:document-checked" class="mr-5px" /> 保存基础信息</ElButton
          >
        </div>
      </ElTabPane>

      <ElTabPane label="告警通用设置" name="alarmCommon">
        <div class="page-title">🚨 告警通用设置</div>
        <div class="page-desc"
          >配置系统通用告警参数，包括告警抑制周期、自动恢复确认时间、免打扰时段等全局设置。</div
        >

        <ElCard shadow="never" class="mt-16px">
          <div class="card-title">🔧 告警通用设置</div>
          <ElForm :model="alarmCommonForm" label-position="top">
            <div class="grid-2">
              <ElFormItem label="告警抑制周期（分钟）">
                <ElInputNumber v-model="alarmCommonForm.suppressMinutes" class="w-full" :min="0" />
                <div class="hint">防止告警风暴，相同告警在设定时间内只发送一次</div>
              </ElFormItem>
              <ElFormItem label="自动恢复确认时间（分钟）">
                <ElInputNumber
                  v-model="alarmCommonForm.autoRecoverConfirmMinutes"
                  class="w-full"
                  :min="0"
                />
                <div class="hint">指标恢复正常后，持续该时间才确认告警恢复</div>
              </ElFormItem>
              <ElFormItem label="免打扰时段">
                <div class="time-range">
                  <ElTimePicker
                    v-model="alarmCommonForm.dndStart"
                    format="HH:mm"
                    value-format="HH:mm"
                    class="w-160px"
                  />
                  <span class="time-sep">至</span>
                  <ElTimePicker
                    v-model="alarmCommonForm.dndEnd"
                    format="HH:mm"
                    value-format="HH:mm"
                    class="w-160px"
                  />
                </div>
                <div class="hint">该时段内仅紧急告警(P0)发送通知</div>
              </ElFormItem>
              <ElFormItem label="告警升级时间（分钟）">
                <ElInputNumber v-model="alarmCommonForm.escalateMinutes" class="w-full" :min="0" />
                <div class="hint">告警未处理超过该时间自动升级告警等级</div>
              </ElFormItem>
            </div>
          </ElForm>
        </ElCard>

        <div class="footer-actions">
          <ElButton @click="resetAlarmSettings"
            ><Icon icon="ep:refresh" class="mr-5px" /> 恢复默认</ElButton
          >
          <ElButton type="primary" @click="saveAlarmSettings"
            ><Icon icon="ep:document-checked" class="mr-5px" /> 保存告警设置</ElButton
          >
        </div>
      </ElTabPane>

      <ElTabPane label="预警策略配置" name="warningStrategy">
        <div class="page-title">🎛️ 预警策略配置</div>
        <div class="page-desc"
          >配置电力、水量、燃气等各类能源的预警策略，支持按设备、区域维度设置分级预警阈值。</div
        >

        <ElTabs v-model="strategyState.energyType" class="mt-16px">
          <ElTabPane label="⚡ 电力规则" name="electricity" />
          <ElTabPane label="💧 水量规则" name="water" />
          <ElTabPane label="🔥 燃气规则" name="gas" />
        </ElTabs>

        <ElCard shadow="never">
          <div class="filter-bar">
            <div class="filter-item">
              <span class="filter-label">适用类型:</span>
              <ElSelect v-model="strategyState.applicableType" class="w-140px">
                <ElOption label="全部" value="all" />
                <ElOption label="设备" value="device" />
                <ElOption label="区域" value="area" />
              </ElSelect>
            </div>
            <div class="filter-item">
              <span class="filter-label">策略状态:</span>
              <ElSelect v-model="strategyState.status" class="w-140px">
                <ElOption label="全部" value="all" />
                <ElOption label="未开始" value="pending" />
                <ElOption label="已启用" value="active" />
                <ElOption label="已停用" value="inactive" />
                <ElOption label="已过期" value="expired" />
              </ElSelect>
            </div>
            <div class="filter-item filter-item--grow">
              <ElInput
                v-model="strategyState.keyword"
                placeholder="搜索策略名称..."
                clearable
                class="w-260px"
              />
              <ElButton @click="resetStrategyFilters"
                ><Icon icon="ep:refresh" class="mr-5px" /> 重置</ElButton
              >
            </div>
          </div>
        </ElCard>

        <div class="strategy-grid">
          <ElCard
            v-for="s in filteredStrategyList"
            :key="s.id"
            shadow="never"
            class="strategy-card"
          >
            <div class="strategy-header">
              <div class="strategy-title">{{ s.name }}</div>
              <ElTag size="small" :type="strategyStatusTagType[s.status]" effect="plain">{{
                strategyStatusText[s.status]
              }}</ElTag>
            </div>
            <div class="strategy-meta">
              <div class="meta-item"
                ><span class="meta-label">适用类型:</span
                ><strong>{{ s.applicableType === 'device' ? '设备' : '区域' }}</strong></div
              >
              <div class="meta-item"
                ><span class="meta-label">监控对象:</span><strong>{{ s.targetName }}</strong></div
              >
              <div class="meta-item"
                ><span class="meta-label">预警阈值:</span
                ><strong>{{ s.thresholdText }}</strong></div
              >
              <div class="meta-item"
                ><span class="meta-label">生效日期:</span
                ><strong>{{ s.effectiveText }}</strong></div
              >
            </div>
            <div class="strategy-actions">
              <ElButton size="small" @click="openEditStrategy(s)">✏️ 编辑</ElButton>
              <ElButton size="small" type="primary" plain @click="openViewStrategy(s)"
                >👁️ 查看</ElButton
              >
              <ElButton size="small" type="danger" plain @click="deleteStrategy(s)"
                >🗑️ 删除</ElButton
              >
            </div>
          </ElCard>
        </div>

        <ElButton class="add-fab" type="primary" circle @click="openAddStrategy">+</ElButton>

        <ElDialog
          v-model="strategyDialog.visible"
          :title="
            strategyDialog.mode === 'add'
              ? '➕ 新增预警策略'
              : strategyDialog.mode === 'edit'
                ? '✏️ 编辑预警策略'
                : '👁️ 查看预警策略'
          "
          width="760px"
        >
          <ElForm label-position="top">
            <div class="grid-2">
              <ElFormItem label="能源类型">
                <ElSelect
                  v-model="strategyDialog.form.energyType"
                  :disabled="strategyDialog.mode === 'view'"
                >
                  <ElOption label="电力" value="electricity" />
                  <ElOption label="水量" value="water" />
                  <ElOption label="燃气" value="gas" />
                </ElSelect>
              </ElFormItem>
              <ElFormItem label="适用类型">
                <ElSelect
                  v-model="strategyDialog.form.applicableType"
                  :disabled="strategyDialog.mode === 'view'"
                >
                  <ElOption label="设备" value="device" />
                  <ElOption label="区域" value="area" />
                </ElSelect>
              </ElFormItem>
              <ElFormItem label="策略名称" class="col-span-2">
                <ElInput
                  v-model="strategyDialog.form.name"
                  :readonly="strategyDialog.mode === 'view'"
                  placeholder="请输入策略名称"
                />
              </ElFormItem>
              <ElFormItem label="监控对象" class="col-span-2">
                <ElInput
                  v-model="strategyDialog.form.targetName"
                  :readonly="strategyDialog.mode === 'view'"
                  placeholder="请输入监控对象"
                />
              </ElFormItem>
              <ElFormItem label="预警阈值" class="col-span-2">
                <ElInput
                  v-model="strategyDialog.form.thresholdText"
                  :readonly="strategyDialog.mode === 'view'"
                  placeholder="例如：800kWh (P0)"
                />
              </ElFormItem>
              <ElFormItem label="生效日期" class="col-span-2">
                <ElDatePicker
                  v-model="strategyDialog.form.effectiveRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="YYYY-MM-DD"
                  :disabled="strategyDialog.mode === 'view'"
                />
              </ElFormItem>
              <ElFormItem label="状态">
                <ElSelect
                  v-model="strategyDialog.form.status"
                  :disabled="strategyDialog.mode === 'view'"
                >
                  <ElOption label="未开始" value="pending" />
                  <ElOption label="已启用" value="active" />
                  <ElOption label="已停用" value="inactive" />
                  <ElOption label="已过期" value="expired" />
                </ElSelect>
              </ElFormItem>
            </div>
          </ElForm>
          <template #footer>
            <ElButton @click="strategyDialog.visible = false">关闭</ElButton>
            <ElButton v-if="strategyDialog.mode !== 'view'" type="primary" @click="submitStrategy"
              >保存</ElButton
            >
          </template>
        </ElDialog>
      </ElTabPane>

      <ElTabPane label="计费规则配置" name="billing">
        <div class="page-title">💰 计费规则配置</div>
        <div class="page-desc"
          >配置园区能源计费规则，支持电价分时计费（尖峰平谷）及固定单价模式。</div
        >

        <ElTabs v-model="billingState.activeType" class="mt-16px">
          <ElTabPane label="⚡ 电价配置" name="electricity" />
          <ElTabPane label="💧 水费配置" name="water" />
          <ElTabPane label="🔥 燃气费配置" name="gas" />
        </ElTabs>

        <div v-if="billingState.activeType === 'electricity'">
          <ElCard shadow="never">
            <div class="card-title">⚡ 电价计费规则</div>

            <ElForm label-position="top">
              <ElFormItem label="计费模式">
                <ElRadioGroup v-model="billingState.electricityMode">
                  <ElRadioButton label="time-of-use">尖峰平谷分时计费</ElRadioButton>
                  <ElRadioButton label="fixed">固定单价</ElRadioButton>
                </ElRadioGroup>
              </ElFormItem>
            </ElForm>

            <div v-if="billingState.electricityMode === 'time-of-use'">
              <ElForm label-position="top">
                <ElFormItem label="季节类型">
                  <ElRadioGroup v-model="billingState.season">
                    <ElRadioButton label="summer">夏季 (6-8月)</ElRadioButton>
                    <ElRadioButton label="winter">冬季 (12-2月)</ElRadioButton>
                    <ElRadioButton label="shoulder">春秋季 (其他)</ElRadioButton>
                  </ElRadioGroup>
                </ElFormItem>
              </ElForm>

              <div class="tou-grid">
                <ElCard shadow="never" class="tou-card tou-card--jian">
                  <div class="tou-title">🔴 尖峰时段</div>
                  <div class="tou-row">
                    <ElTimePicker
                      v-model="electricityTou.jian.start"
                      value-format="HH:mm"
                      format="HH:mm"
                    />
                    <span class="tou-sep">-</span>
                    <ElTimePicker
                      v-model="electricityTou.jian.end"
                      value-format="HH:mm"
                      format="HH:mm"
                    />
                  </div>
                  <div class="tou-price">
                    <ElInputNumber
                      v-model="electricityTou.jian.price"
                      :min="0"
                      :step="0.01"
                      class="w-full"
                    />
                    <div class="tou-unit">元/kWh</div>
                  </div>
                </ElCard>

                <ElCard shadow="never" class="tou-card tou-card--feng">
                  <div class="tou-title">🟠 高峰时段</div>
                  <div class="tou-row">
                    <ElTimePicker
                      v-model="electricityTou.feng.start"
                      value-format="HH:mm"
                      format="HH:mm"
                    />
                    <span class="tou-sep">-</span>
                    <ElTimePicker
                      v-model="electricityTou.feng.end"
                      value-format="HH:mm"
                      format="HH:mm"
                    />
                  </div>
                  <div class="tou-price">
                    <ElInputNumber
                      v-model="electricityTou.feng.price"
                      :min="0"
                      :step="0.01"
                      class="w-full"
                    />
                    <div class="tou-unit">元/kWh</div>
                  </div>
                </ElCard>

                <ElCard shadow="never" class="tou-card tou-card--ping">
                  <div class="tou-title">🔵 平段时段</div>
                  <div class="tou-row">
                    <ElTimePicker
                      v-model="electricityTou.ping.start"
                      value-format="HH:mm"
                      format="HH:mm"
                    />
                    <span class="tou-sep">-</span>
                    <ElTimePicker
                      v-model="electricityTou.ping.end"
                      value-format="HH:mm"
                      format="HH:mm"
                    />
                  </div>
                  <div class="tou-price">
                    <ElInputNumber
                      v-model="electricityTou.ping.price"
                      :min="0"
                      :step="0.01"
                      class="w-full"
                    />
                    <div class="tou-unit">元/kWh</div>
                  </div>
                </ElCard>

                <ElCard shadow="never" class="tou-card tou-card--gu">
                  <div class="tou-title">🟢 低谷时段</div>
                  <div class="tou-row">
                    <ElTimePicker
                      v-model="electricityTou.gu.start"
                      value-format="HH:mm"
                      format="HH:mm"
                    />
                    <span class="tou-sep">-</span>
                    <ElTimePicker
                      v-model="electricityTou.gu.end"
                      value-format="HH:mm"
                      format="HH:mm"
                    />
                  </div>
                  <div class="tou-price">
                    <ElInputNumber
                      v-model="electricityTou.gu.price"
                      :min="0"
                      :step="0.01"
                      class="w-full"
                    />
                    <div class="tou-unit">元/kWh</div>
                  </div>
                </ElCard>
              </div>

              <ElForm label-position="top" class="mt-16px">
                <ElFormItem label="备注说明">
                  <ElInput
                    v-model="billingState.touRemark"
                    type="textarea"
                    :rows="2"
                    placeholder="请输入该季节时段划分的备注说明..."
                  />
                </ElFormItem>
              </ElForm>
            </div>

            <div v-else class="grid-2 mt-16px">
              <ElCard shadow="never">
                <ElForm label-position="top">
                  <ElFormItem label="固定电价 (元/kWh)">
                    <ElInputNumber
                      v-model="billingState.fixedPrice"
                      class="w-full"
                      :min="0"
                      :step="0.01"
                    />
                  </ElFormItem>
                  <ElFormItem label="生效日期">
                    <ElDatePicker
                      v-model="billingState.fixedEffectiveDate"
                      type="date"
                      value-format="YYYY-MM-DD"
                      class="w-full"
                    />
                  </ElFormItem>
                </ElForm>
              </ElCard>
            </div>
          </ElCard>

          <ElCard shadow="never" class="mt-16px">
            <div class="card-title">📋 电价规则列表</div>
            <ElTable :data="electricityRuleList" size="small">
              <ElTableColumn label="规则名称" prop="name" min-width="220" />
              <ElTableColumn label="计费模式" prop="mode" width="120" />
              <ElTableColumn label="生效状态" prop="status" width="120" />
              <ElTableColumn label="操作" width="150" fixed="right">
                <template #default>
                  <ElButton size="small">编辑</ElButton>
                  <ElButton size="small" type="danger" plain>停用</ElButton>
                </template>
              </ElTableColumn>
            </ElTable>

            <div class="footer-actions">
              <ElButton type="primary" @click="saveBilling('electricity')"
                ><Icon icon="ep:document-checked" class="mr-5px" /> 保存电价规则</ElButton
              >
            </div>
          </ElCard>
        </div>

        <div v-else-if="billingState.activeType === 'water'">
          <ElCard shadow="never">
            <div class="card-title">💧 水费计费规则</div>
            <div class="grid-2">
              <ElForm label-position="top">
                <ElFormItem label="水费单价 (元/m³)">
                  <ElInputNumber
                    v-model="billingState.waterPrice"
                    class="w-full"
                    :min="0"
                    :step="0.01"
                  />
                  <div class="hint">包含自来水费及污水处理费</div>
                </ElFormItem>
                <ElFormItem label="生效日期">
                  <ElDatePicker
                    v-model="billingState.waterEffectiveDate"
                    type="date"
                    value-format="YYYY-MM-DD"
                    class="w-full"
                  />
                </ElFormItem>
              </ElForm>
            </div>
            <div class="footer-actions">
              <ElButton type="primary" @click="saveBilling('water')"
                ><Icon icon="ep:document-checked" class="mr-5px" /> 保存水费规则</ElButton
              >
            </div>
          </ElCard>
        </div>

        <div v-else>
          <ElCard shadow="never">
            <div class="card-title">🔥 燃气费计费规则</div>
            <div class="grid-2">
              <ElForm label-position="top">
                <ElFormItem label="燃气单价 (元/m³)">
                  <ElInputNumber
                    v-model="billingState.gasPrice"
                    class="w-full"
                    :min="0"
                    :step="0.01"
                  />
                  <div class="hint">根据当地燃气公司收费标准设置</div>
                </ElFormItem>
                <ElFormItem label="生效日期">
                  <ElDatePicker
                    v-model="billingState.gasEffectiveDate"
                    type="date"
                    value-format="YYYY-MM-DD"
                    class="w-full"
                  />
                </ElFormItem>
              </ElForm>
            </div>
            <div class="footer-actions">
              <ElButton type="primary" @click="saveBilling('gas')"
                ><Icon icon="ep:document-checked" class="mr-5px" /> 保存燃气费规则</ElButton
              >
            </div>
          </ElCard>
        </div>
      </ElTabPane>

      <ElTabPane label="能耗分区配置" name="partition">
        <div class="page-title">🏘️ 能耗分区配置</div>
        <div class="page-desc"
          >配置建筑能耗分区，建立分区层级结构，关联计量表计，实现分区分户能耗监测与管理。</div
        >

        <div class="partition-layout mt-16px">
          <ElCard shadow="never" class="partition-left">
            <div class="card-title">🗂️ 分区导航</div>
            <ElTree
              :data="partitionTreeData"
              node-key="id"
              :props="{ label: 'label', children: 'children' }"
              default-expand-all
              highlight-current
              @current-change="(node: any) => (partitionState.selectedId = node?.id || 'root')"
            />
          </ElCard>

          <ElCard shadow="never" class="partition-right">
            <div class="partition-right-header">
              <div class="card-title">🏢 分区列表</div>
              <ElButton type="primary" @click="openAddPartition">➕ 新增关联</ElButton>
            </div>

            <div class="partition-filter">
              <div class="filter-item">
                <span class="filter-label">分区类型:</span>
                <ElSelect v-model="partitionState.typeFilter" class="w-140px">
                  <ElOption label="全部" value="all" />
                  <ElOption label="建筑" value="building" />
                  <ElOption label="楼层" value="floor" />
                  <ElOption label="功能区域" value="area" />
                  <ElOption label="房间" value="room" />
                </ElSelect>
              </div>
              <div class="filter-item filter-item--grow">
                <ElInput
                  v-model="partitionState.keyword"
                  placeholder="搜索分区名称..."
                  clearable
                  class="w-260px"
                />
                <ElButton
                  @click="
                    () => {
                      partitionState.typeFilter = 'all'
                      partitionState.keyword = ''
                    }
                  "
                >
                  <Icon icon="ep:refresh" class="mr-5px" /> 重置
                </ElButton>
              </div>
            </div>

            <ElTable :data="filteredPartitionRows" stripe>
              <ElTableColumn label="序号" width="70" type="index" />
              <ElTableColumn label="分区名称" prop="name" min-width="160" />
              <ElTableColumn label="分区类型" width="120">
                <template #default="{ row }">
                  <ElTag size="small" :type="partitionTypeTagType[row.type]" effect="plain">
                    {{ partitionTypeText[row.type] }}
                  </ElTag>
                </template>
              </ElTableColumn>
              <ElTableColumn label="上级分区" prop="parentName" min-width="140" />
              <ElTableColumn label="关联表计" min-width="220">
                <template #default="{ row }">
                  <div class="meter-tags">
                    <ElTag
                      v-for="m in row.meters"
                      :key="m"
                      size="small"
                      effect="plain"
                      class="meter-tag"
                    >
                      {{ m }}
                    </ElTag>
                  </div>
                </template>
              </ElTableColumn>
              <ElTableColumn label="操作" width="160" fixed="right">
                <template #default="{ row }">
                  <ElButton size="small" @click="openEditPartition(row)">编辑</ElButton>
                  <ElButton size="small" type="danger" plain @click="deletePartition(row)"
                    >删除</ElButton
                  >
                </template>
              </ElTableColumn>
            </ElTable>

            <div class="partition-footer">
              <div class="partition-total">共 {{ filteredPartitionRows.length }} 条记录</div>
              <div class="partition-pager">
                <ElButton size="small" disabled>上一页</ElButton>
                <ElButton size="small" type="primary" disabled>1</ElButton>
                <ElButton size="small" disabled>下一页</ElButton>
              </div>
            </div>

            <ElDialog
              v-model="partitionDialog.visible"
              :title="partitionDialog.mode === 'add' ? '➕ 新增关联' : '✏️ 编辑分区'"
              width="720px"
            >
              <ElForm label-position="top">
                <div class="grid-2">
                  <ElFormItem label="分区名称" class="col-span-2">
                    <ElInput v-model="partitionDialog.form.name" placeholder="请输入分区名称" />
                  </ElFormItem>
                  <ElFormItem label="分区类型">
                    <ElSelect v-model="partitionDialog.form.type">
                      <ElOption label="建筑" value="building" />
                      <ElOption label="楼层" value="floor" />
                      <ElOption label="功能区域" value="area" />
                      <ElOption label="房间" value="room" />
                    </ElSelect>
                  </ElFormItem>
                  <ElFormItem label="上级分区">
                    <ElSelect v-model="partitionDialog.form.parentId">
                      <ElOption
                        v-for="p in partitionRows"
                        :key="p.id"
                        :label="p.name"
                        :value="p.id"
                      />
                    </ElSelect>
                  </ElFormItem>
                  <ElFormItem label="关联表计" class="col-span-2">
                    <ElSelect
                      v-model="partitionDialog.form.meters"
                      multiple
                      filterable
                      collapse-tags
                      collapse-tags-tooltip
                    >
                      <ElOption
                        v-for="m in meterOptions"
                        :key="m.value"
                        :label="m.label"
                        :value="m.value"
                      />
                    </ElSelect>
                  </ElFormItem>
                </div>
              </ElForm>
              <template #footer>
                <ElButton @click="partitionDialog.visible = false">取消</ElButton>
                <ElButton type="primary" @click="submitPartition">保存</ElButton>
              </template>
            </ElDialog>
          </ElCard>
        </div>
      </ElTabPane>
    </ElTabs>
  </EnergyPageContainer>
</template>

<style scoped lang="scss">
.settings-tabs :deep(.el-tabs__header) {
  min-width: 180px;
}

.page-title {
  font-size: 18px;
  font-weight: 900;
  color: var(--el-text-color-primary);
}

.page-desc {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.card-title {
  margin-bottom: 12px;
  font-weight: 900;
  color: var(--el-text-color-primary);
}

.sub-title {
  margin-bottom: 10px;
  font-weight: 900;
  color: var(--el-text-color-primary);
}

.hint {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.col-span-2 {
  grid-column: span 2;
}

.quota-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.energy-tabs {
  min-width: 280px;
}

.quota-panel {
  margin-top: 8px;
}

.actions-row {
  display: flex;
  gap: 10px;
  margin-top: 8px;
}

.footer-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  flex-wrap: wrap;
}

.time-range {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.time-sep {
  color: var(--el-text-color-secondary);
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item--grow {
  flex: 1;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-label {
  font-size: 12px;
  font-weight: 800;
  color: var(--el-text-color-secondary);
}

.strategy-grid {
  display: grid;
  margin-top: 16px;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.strategy-card :deep(.el-card__body) {
  padding: 16px;
}

.strategy-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.strategy-title {
  font-weight: 900;
  color: var(--el-text-color-primary);
}

.strategy-meta {
  display: grid;
  margin-top: 10px;
  grid-template-columns: 1fr 1fr;
  gap: 8px 12px;
}

.meta-item {
  display: flex;
  gap: 6px;
  color: var(--el-text-color-regular);
}

.meta-label {
  color: var(--el-text-color-secondary);
}

.strategy-actions {
  display: flex;
  margin-top: 12px;
  gap: 8px;
  flex-wrap: wrap;
}

.add-fab {
  position: fixed;
  right: 24px;
  bottom: 24px;
  width: 44px;
  height: 44px;
  padding: 0;
  font-size: 20px;
}

.tou-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.tou-card :deep(.el-card__body) {
  padding: 14px;
}

.tou-title {
  margin-bottom: 10px;
  font-weight: 900;
  color: var(--el-text-color-primary);
}

.tou-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tou-sep {
  color: var(--el-text-color-secondary);
}

.tou-price {
  display: flex;
  margin-top: 10px;
  align-items: center;
  gap: 10px;
}

.tou-unit {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}

.tou-card--jian {
  border: 1px solid rgb(239 68 68 / 35%);
}

.tou-card--feng {
  border: 1px solid rgb(245 158 11 / 35%);
}

.tou-card--ping {
  border: 1px solid rgb(59 130 246 / 35%);
}

.tou-card--gu {
  border: 1px solid rgb(16 185 129 / 35%);
}

.partition-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 16px;
}

.partition-left :deep(.el-card__body) {
  padding: 16px;
}

.partition-right-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.partition-filter {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.meter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.meter-tag {
  margin-right: 0;
}

.partition-footer {
  display: flex;
  padding-top: 12px;
  margin-top: 12px;
  border-top: 1px solid var(--el-border-color-lighter);
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.partition-total {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.partition-pager {
  display: flex;
  gap: 6px;
}

@media (width <= 1400px) {
  .grid-2 {
    grid-template-columns: 1fr;
  }

  .strategy-grid {
    grid-template-columns: 1fr;
  }

  .tou-grid {
    grid-template-columns: 1fr;
  }

  .partition-layout {
    grid-template-columns: 1fr;
  }
}
</style>
