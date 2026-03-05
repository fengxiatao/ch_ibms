<script setup lang="ts">
import type { EChartsOption } from 'echarts'
import { Echart } from '@/components/Echart'
import EnergyPageContainer from '../components/EnergyPageContainer.vue'
import EnergyPageHeader from '../components/EnergyPageHeader.vue'

defineOptions({ name: 'EnergyDeviceManagement' })

type DeviceType = 'three' | 'single' | 'water'
type DeviceStatus = 'normal' | 'warning' | 'danger' | 'offline'

interface DeviceItem {
  id: string
  title: string
  area: string
  areaKey: string
  type: DeviceType
  typeName: string
  status: DeviceStatus
  name: string
  voltage?: number
  current?: number
  power?: number
  flow?: number
  totalFlow?: number
}

const areas = [
  { label: '全部区域', value: '' },
  { label: '一号配电室', value: 'area1' },
  { label: '二号配电室', value: 'area2' },
  { label: '办公区A', value: 'area3' },
  { label: '生产区B', value: 'area4' },
  { label: '水务区', value: 'area5' }
]

const types = [
  { label: '全部类型', value: '' },
  { label: '三相电表', value: 'three' },
  { label: '单相电表', value: 'single' },
  { label: '水表', value: 'water' }
]

const query = reactive({
  area: '',
  type: '' as '' | DeviceType,
  view: 'card' as 'card' | 'list'
})

const devices = ref<DeviceItem[]>([
  {
    id: 'EL-001',
    title: '⚡ 1#配电柜',
    area: '一号配电室',
    areaKey: 'area1',
    type: 'three',
    typeName: '三相电表',
    status: 'normal',
    name: '主进线柜',
    voltage: 380.5,
    current: 20.1,
    power: 10.75
  },
  {
    id: 'EL-002',
    title: '⚡ 2#配电柜',
    area: '二号配电室',
    areaKey: 'area2',
    type: 'three',
    typeName: '三相电表',
    status: 'normal',
    name: '备用进线柜',
    voltage: 380.2,
    current: 5.2,
    power: 3.25
  },
  {
    id: 'A-S-001',
    title: '🔌 办公区A单相电表001',
    area: '办公区A',
    areaKey: 'area3',
    type: 'single',
    typeName: '单相电表',
    status: 'normal',
    name: '办公区A层照明',
    voltage: 220.3,
    current: 8.5,
    power: 1.87
  },
  {
    id: 'B-T-001',
    title: '⚡ 生产区B三相电表001',
    area: '生产区B',
    areaKey: 'area4',
    type: 'three',
    typeName: '三相电表',
    status: 'warning',
    name: '生产线B1动力',
    voltage: 381.2,
    current: 45.6,
    power: 28.5
  },
  {
    id: 'W-001',
    title: '💧 总进水水表001',
    area: '水务区',
    areaKey: 'area5',
    type: 'water',
    typeName: '水表',
    status: 'normal',
    name: '厂区总进水',
    flow: 45.6,
    totalFlow: 12568
  }
])

const filteredDevices = computed(() => {
  return devices.value.filter((d) => {
    if (query.area && d.areaKey !== query.area) return false
    if (query.type && d.type !== query.type) return false
    return true
  })
})

const statusTextMap: Record<DeviceStatus, string> = {
  normal: '正常',
  warning: '注意',
  danger: '告警',
  offline: '离线'
}

const statusTagTypeMap: Record<DeviceStatus, any> = {
  normal: 'success',
  warning: 'warning',
  danger: 'danger',
  offline: 'info'
}

const typeTagTypeMap: Record<DeviceType, any> = {
  three: 'warning',
  single: 'info',
  water: 'success'
}

const detailVisible = ref(false)
const currentDevice = ref<DeviceItem | null>(null)

const openDetail = (item: DeviceItem) => {
  currentDevice.value = item
  detailVisible.value = true
}

const refresh = () => {
  ElMessage.success('已刷新设备数据')
}

const exportData = () => {
  ElMessage.success('已开始导出（示例）')
}

const applyFilter = () => {
  ElMessage.success('已应用筛选')
}

const resetFilter = () => {
  query.area = ''
  query.type = ''
  ElMessage.success('已重置筛选')
}

const metricChartOptions = computed<EChartsOption>(() => {
  const x = Array.from({ length: 12 }).map((_, i) => `${i + 1}`)
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 16, right: 16, top: 20, bottom: 10, containLabel: true },
    xAxis: { type: 'category', data: x },
    yAxis: { type: 'value' },
    series: [
      {
        type: 'line',
        smooth: true,
        data: [10, 12, 11, 13, 15, 14, 16, 18, 17, 16, 15, 14],
        lineStyle: { width: 2, color: 'var(--el-color-primary)' },
        itemStyle: { color: 'var(--el-color-primary)' },
        areaStyle: { color: 'rgba(16,185,129,0.12)' }
      }
    ]
  }
})
</script>

<template>
  <EnergyPageContainer>
    <EnergyPageHeader title="设备管理" subtitle="实时监测与设备台账">
      <template #actions>
        <ElButton type="primary" @click="refresh"><Icon icon="ep:refresh" class="mr-5px" /> 刷新数据</ElButton>
        <ElButton @click="exportData"><Icon icon="ep:download" class="mr-5px" /> 导出实时数据</ElButton>
        <ElButton type="danger" plain><Icon icon="ep:warning" class="mr-5px" /> 查看告警</ElButton>
      </template>
    </EnergyPageHeader>

    <ElCard shadow="never">
      <div class="filter-grid">
        <div class="filter-group">
          <div class="filter-label">按区域筛选</div>
          <ElSelect v-model="query.area" class="w-full" placeholder="全部区域" clearable>
            <ElOption v-for="o in areas" :key="o.value" :label="o.label" :value="o.value" />
          </ElSelect>
        </div>
        <div class="filter-group">
          <div class="filter-label">按设备类型筛选</div>
          <ElSelect v-model="query.type" class="w-full" placeholder="全部类型" clearable>
            <ElOption v-for="o in types" :key="o.value" :label="o.label" :value="o.value" />
          </ElSelect>
        </div>
        <div class="filter-group">
          <div class="filter-label">查看方式</div>
          <ElButtonGroup class="w-full">
            <ElButton class="w-1/2" :type="query.view === 'card' ? 'primary' : 'default'" @click="query.view = 'card'">
              🎴 卡片视图
            </ElButton>
            <ElButton class="w-1/2" :type="query.view === 'list' ? 'primary' : 'default'" @click="query.view = 'list'">
              📋 列表视图
            </ElButton>
          </ElButtonGroup>
        </div>
        <div class="filter-group">
          <div class="filter-label">筛选操作</div>
          <ElButton type="primary" class="w-full" @click="applyFilter">✅ 应用筛选</ElButton>
          <ElButton class="w-full mt-10px" @click="resetFilter">🔄 重置筛选</ElButton>
        </div>
      </div>
    </ElCard>

    <template v-if="query.view === 'card'">
      <div class="card-grid">
        <div v-for="item in filteredDevices" :key="item.id" class="device-card" @click="openDetail(item)">
          <div class="device-card__header">
            <div class="device-title">
              <span>{{ item.title }}</span>
              <ElTag size="small" :type="typeTagTypeMap[item.type]" effect="plain">{{ item.typeName }}</ElTag>
            </div>
            <ElTag size="small" :type="statusTagTypeMap[item.status]" effect="dark">
              {{ statusTextMap[item.status] }}
            </ElTag>
          </div>
          <div class="device-meta">
            <span class="device-name">{{ item.name }}</span>
            <span class="device-id">{{ item.id }}</span>
          </div>

          <div v-if="item.type !== 'water'" class="metrics-grid">
            <div class="metric-item">
              <div class="metric-value">{{ item.voltage?.toFixed(1) }}<span class="metric-unit">V</span></div>
              <div class="metric-label">电压</div>
            </div>
            <div class="metric-item">
              <div class="metric-value">{{ item.current?.toFixed(1) }}<span class="metric-unit">A</span></div>
              <div class="metric-label">电流</div>
            </div>
            <div class="metric-item">
              <div class="metric-value">{{ item.power?.toFixed(2) }}<span class="metric-unit">kW</span></div>
              <div class="metric-label">功率</div>
            </div>
          </div>

          <div v-else class="metrics-grid">
            <div class="metric-item">
              <div class="metric-value">{{ item.flow?.toFixed(1) }}<span class="metric-unit">m³/h</span></div>
              <div class="metric-label">瞬时流量</div>
            </div>
            <div class="metric-item">
              <div class="metric-value">{{ item.totalFlow }}<span class="metric-unit">m³</span></div>
              <div class="metric-label">累计流量</div>
            </div>
          </div>

          <div class="click-hint">点击查看详情 →</div>
        </div>
      </div>
    </template>

    <template v-else>
      <ElCard shadow="never">
        <ElTable :data="filteredDevices" stripe>
          <ElTableColumn label="设备ID" prop="id" width="120" />
          <ElTableColumn label="设备名称" prop="name" min-width="160" />
          <ElTableColumn label="区域" prop="area" width="140" />
          <ElTableColumn label="类型" width="120">
            <template #default="{ row }">
              <ElTag size="small" :type="typeTagTypeMap[row.type]" effect="plain">{{ row.typeName }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn label="状态" width="100">
            <template #default="{ row }">
              <ElTag size="small" :type="statusTagTypeMap[row.status]" effect="dark">{{ statusTextMap[row.status] }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <ElButton link type="primary" @click="openDetail(row)">详情</ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </ElCard>
    </template>

    <ElDrawer v-model="detailVisible" size="520px" :with-header="false">
      <template v-if="currentDevice">
        <div class="drawer-header">
          <div>
            <div class="drawer-title">{{ currentDevice.title }}</div>
            <div class="drawer-sub">{{ currentDevice.name }} · {{ currentDevice.id }}</div>
          </div>
          <ElTag size="small" :type="statusTagTypeMap[currentDevice.status]" effect="dark">
            {{ statusTextMap[currentDevice.status] }}
          </ElTag>
        </div>

        <ElDescriptions :column="2" border class="mt-12px">
          <ElDescriptionsItem label="区域">{{ currentDevice.area }}</ElDescriptionsItem>
          <ElDescriptionsItem label="类型">{{ currentDevice.typeName }}</ElDescriptionsItem>
          <ElDescriptionsItem v-if="currentDevice.type !== 'water'" label="电压">
            {{ currentDevice.voltage?.toFixed(1) }} V
          </ElDescriptionsItem>
          <ElDescriptionsItem v-if="currentDevice.type !== 'water'" label="电流">
            {{ currentDevice.current?.toFixed(1) }} A
          </ElDescriptionsItem>
          <ElDescriptionsItem v-if="currentDevice.type !== 'water'" label="功率">
            {{ currentDevice.power?.toFixed(2) }} kW
          </ElDescriptionsItem>
          <ElDescriptionsItem v-if="currentDevice.type === 'water'" label="瞬时流量">
            {{ currentDevice.flow?.toFixed(1) }} m³/h
          </ElDescriptionsItem>
          <ElDescriptionsItem v-if="currentDevice.type === 'water'" label="累计流量">
            {{ currentDevice.totalFlow }} m³
          </ElDescriptionsItem>
        </ElDescriptions>

        <ElCard shadow="never" class="mt-12px">
          <div class="drawer-chart-title">近 12 个采样点趋势</div>
          <Echart :options="metricChartOptions" height="260px" />
        </ElCard>
      </template>
    </ElDrawer>
  </EnergyPageContainer>
</template>

<style scoped lang="scss">
.filter-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-label {
  font-size: 12px;
  font-weight: 800;
  color: var(--el-text-color-secondary);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.device-card {
  padding: 16px 16px;
  border-radius: 14px;
  border: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.device-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.06);
}

.device-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.device-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 900;
  color: var(--el-text-color-primary);
}

.device-meta {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: var(--el-text-color-secondary);
}

.device-name {
  font-weight: 700;
}

.device-id {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.metrics-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.metric-item {
  padding: 10px 10px;
  border-radius: 12px;
  background: var(--el-bg-color-page);
  border: 1px solid var(--el-border-color-lighter);
}

.metric-value {
  font-size: 16px;
  font-weight: 900;
  color: var(--el-text-color-primary);
}

.metric-unit {
  margin-left: 3px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.metric-label {
  margin-top: 2px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.click-hint {
  margin-top: 10px;
  font-size: 12px;
  color: var(--el-color-primary);
  font-weight: 800;
}

.drawer-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.drawer-title {
  font-size: 16px;
  font-weight: 900;
  color: var(--el-text-color-primary);
}

.drawer-sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.drawer-chart-title {
  font-weight: 900;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
}

@media (max-width: 1400px) {
  .filter-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .card-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 900px) {
  .card-grid {
    grid-template-columns: 1fr;
  }
  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
