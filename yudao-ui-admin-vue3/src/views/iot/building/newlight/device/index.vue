<!--
  原型复刻：03 设备管理（2026.03.04）.html
  说明：不包含原型左侧菜单（项目统一侧边栏已提供）；页面使用等比缩放容器适配 main 区域。
-->
<template>
  <div class="newlight-page wh-full">
    <ProtoScaleContainer :design-width="1440" :design-height="900" scale-by="width" class="wh-full">
      <div class="proto-root">
        <div class="main-content">
          <div class="flex justify-between items-center mb-4">
            <div>
              <h1 class="text-xl font-bold">设备管理</h1>
              <p class="text-sm text-slate-500 mt-1">照明设备列表与规格参数</p>
            </div>
            <div class="text-sm text-slate-500">{{ currentTime }}</div>
          </div>

          <div class="stats-bar">
            <div class="stats-grid">
              <div class="stat-card">
                <div class="stat-label">设备总数</div>
                <div class="stat-value">{{ stats.totalCount }}</div>
                <div class="stat-desc">在线 {{ stats.onlineCount }} · 离线 {{ stats.offlineCount }}</div>
              </div>
              <div class="stat-card">
                <div class="stat-label">执行控制器</div>
                <div class="stat-value">{{ stats.controllerCount }}</div>
                <div class="stat-desc">管理 {{ stats.controlledCircuits }} 个回路</div>
              </div>
              <div class="stat-card">
                <div class="stat-label">今日总能耗</div>
                <div class="stat-value">
                  {{ stats.totalEnergy }}<span class="stat-unit">kWh</span>
                </div>
                <div class="stat-desc">预估电费 ¥{{ stats.totalCost }}</div>
              </div>
              <div class="stat-card">
                <div class="stat-label">控制器平均使用率</div>
                <div class="stat-value">
                  {{ stats.avgLoadRate }}<span class="stat-unit">%</span>
                </div>
                <div class="stat-desc">基于控制器容量</div>
              </div>
            </div>
          </div>

          <div class="analysis-panel">
            <div class="analysis-title">📊 执行控制器使用率分析</div>
            <div class="analysis-grid">
              <div class="analysis-item">
                <div class="analysis-label">控制器总容量</div>
                <div class="analysis-value">{{ stats.totalControllerCapacity }} W</div>
                <div class="text-xs text-slate-500 mt-1">可带载 {{ stats.maxCircuits }} 个回路</div>
              </div>
              <div class="analysis-item">
                <div class="analysis-label">当前总负载</div>
                <div class="analysis-value">{{ stats.totalControllerLoad }} W</div>
                <div class="text-xs text-slate-500 mt-1">已用容量 {{ stats.usedCapacityPercent }}%</div>
              </div>
              <div class="analysis-item">
                <div class="analysis-label">剩余容量</div>
                <div class="analysis-value">{{ stats.remainingCapacity }} W</div>
                <div class="text-xs text-slate-500 mt-1">可新增 {{ stats.remainingCircuits }} 个回路</div>
              </div>
            </div>
          </div>

          <div class="history-panel">
            <div class="history-title">
              <Icon icon="fa6-solid:clock-rotate-left" />
              历史数据查询
            </div>
            <div class="query-section">
              <div class="date-range">
                <div class="date-input-group">
                  <label>起始时间</label>
                  <input v-model="history.startTime" type="datetime-local" class="date-input" />
                </div>
                <div class="date-input-group">
                  <label>结束时间</label>
                  <input v-model="history.endTime" type="datetime-local" class="date-input" />
                </div>
              </div>

              <select v-model="history.dimension" class="dimension-select">
                <option value="custom">自定义</option>
                <option value="today">今日</option>
                <option value="yesterday">昨日</option>
                <option value="thisWeek">本周</option>
                <option value="lastWeek">上周</option>
                <option value="thisMonth">本月</option>
                <option value="lastMonth">上月</option>
              </select>

              <select v-model="history.type" class="dimension-select">
                <option value="all">全部设备</option>
                <option value="light">普通照明</option>
                <option value="dimmer">调光设备</option>
                <option value="controller">执行控制器</option>
              </select>

              <button class="query-btn" @click="queryHistoryData">
                <Icon icon="fa6-solid:magnifying-glass" />
                查询历史
              </button>
            </div>
          </div>

          <div class="filter-bar">
            <select v-model="filters.type" class="filter-select">
              <option value="all">全部设备类型</option>
              <option value="light">普通照明</option>
              <option value="dimmer">调光设备</option>
              <option value="gateway">智能网关</option>
              <option value="controller">执行控制器</option>
              <option value="sensor">传感器</option>
            </select>

            <select v-model="filters.status" class="filter-select">
              <option value="all">全部状态</option>
              <option value="online">在线</option>
              <option value="offline">离线</option>
            </select>

            <select v-model="filters.area" class="filter-select">
              <option value="all">全部区域</option>
              <option value="A座1层">A座1层</option>
              <option value="A座2层">A座2层</option>
              <option value="B座1层">B座1层</option>
              <option value="B座2层">B座2层</option>
              <option value="C座1层">C座1层</option>
            </select>

            <input v-model.trim="filters.keyword" type="text" class="filter-input" placeholder="搜索设备名称/编号/型号" />

            <button class="filter-btn" @click="applyFilters">
              <Icon icon="fa6-solid:magnifying-glass" class="mr-2" />
              查询
            </button>

            <button class="reset-btn" @click="resetFilters">
              <Icon icon="fa6-solid:arrow-rotate-left" class="mr-2" />
              重置
            </button>

            <button class="export-btn" @click="openExportModal">
              <Icon icon="fa6-solid:file-export" class="mr-2" />
              报表导出
            </button>
          </div>

          <div class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>设备编号</th>
                  <th>设备名称</th>
                  <th>设备类型</th>
                  <th>规格参数</th>
                  <th>所属区域</th>
                  <th>状态</th>
                  <th>所属控制器</th>
                  <th>功率(W)</th>
                  <th>今日能耗(kWh)</th>
                  <th>今日运行(h)</th>
                  <th>累计运行(h)</th>
                  <th>控制器使用率</th>
                  <th>最后通信</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in pagedDevices" :key="row.id">
                  <td>{{ row.code }}</td>
                  <td>{{ row.name }}</td>
                  <td><span class="type-tag">{{ row.typeName }}</span></td>
                  <td>
                    <div class="spec-container">
                      <div v-if="row.specType === 'light'" class="light-spec">
                        <div class="light-count">{{ row.lightCount }} 盏</div>
                        <div class="light-power">{{ row.lightPower }}W</div>
                      </div>
                      <div v-else-if="row.specType === 'dimmer'" class="dimmer-spec">
                        <Icon icon="fa6-solid:sliders" />
                        <div>{{ row.model }}</div>
                      </div>
                      <div v-else-if="row.specType === 'controller'" class="controller-spec">
                        <div class="controller-model">{{ row.model }}</div>
                        <div class="controller-params">
                          <span>{{ row.capacity }}W</span>
                          <span>{{ row.channels }}CH</span>
                        </div>
                      </div>
                      <div v-else-if="row.specType === 'gateway'" class="gateway-spec">
                        <div class="gateway-model">{{ row.model }}</div>
                        <div class="text-xs text-slate-500">MQTT/Modbus</div>
                      </div>
                      <div v-else class="sensor-spec">
                        <div class="sensor-model">{{ row.model }}</div>
                        <div class="sensor-range">{{ row.range }}</div>
                      </div>
                    </div>
                  </td>
                  <td>{{ row.area }}</td>
                  <td>
                    <span class="status-badge" :class="row.status">{{ row.status === 'online' ? '在线' : '离线' }}</span>
                  </td>
                  <td>{{ row.controllerId || '-' }}</td>
                  <td>{{ row.power }}</td>
                  <td>{{ row.energy }}</td>
                  <td>{{ row.runtimeToday }}</td>
                  <td>{{ row.totalRuntime }}</td>
                  <td>{{ row.loadRate }}%</td>
                  <td>{{ row.lastComm }}</td>
                  <td>
                    <span class="action-btn" @click="openDetail(row)">详情</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="pagination">
            <div class="pagination-info">
              显示 <span>{{ startItem }}</span> - <span>{{ endItem }}</span> 条，共 <span>{{ totalItems }}</span> 条
            </div>
            <div class="flex gap-2">
              <button class="pagination-btn" @click="goPage('first')">
                <Icon icon="fa6-solid:angles-left" />
              </button>
              <button class="pagination-btn" @click="goPage('prev')">
                <Icon icon="fa6-solid:angle-left" />
              </button>
              <span class="pagination-btn active">{{ page }}</span>
              <button class="pagination-btn" @click="goPage('next')">
                <Icon icon="fa6-solid:angle-right" />
              </button>
              <button class="pagination-btn" @click="goPage('last')">
                <Icon icon="fa6-solid:angles-right" />
              </button>
            </div>
          </div>
        </div>

        <div class="modal" :class="{ active: detailModalVisible }" @click.self="closeModal('detail')">
          <div class="modal-content">
            <div class="modal-header">
              <div class="modal-title">设备详情</div>
              <div class="modal-close" @click="closeModal('detail')">
                <Icon icon="fa6-solid:xmark" />
              </div>
            </div>
            <div v-if="detailRow" class="detail-grid">
              <div class="detail-item">
                <div class="detail-label">设备编号</div>
                <div class="detail-value">{{ detailRow.code }}</div>
              </div>
              <div class="detail-item">
                <div class="detail-label">设备名称</div>
                <div class="detail-value">{{ detailRow.name }}</div>
              </div>
              <div class="detail-item">
                <div class="detail-label">所属区域</div>
                <div class="detail-value">{{ detailRow.area }}</div>
              </div>
              <div class="detail-item">
                <div class="detail-label">最后通信</div>
                <div class="detail-value">{{ detailRow.lastComm }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="modal" :class="{ active: exportModalVisible }" @click.self="closeModal('export')">
          <div class="modal-content">
            <div class="modal-header">
              <div class="modal-title">导出报表</div>
              <div class="modal-close" @click="closeModal('export')">
                <Icon icon="fa6-solid:xmark" />
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium mb-3">选择导出内容</label>
              <div class="checkbox-group">
                <label class="checkbox-item">
                  <input v-model="exportConfig.basic" type="checkbox" class="rounded border-slate-300" />
                  <span class="text-sm">基础信息</span>
                </label>
                <label class="checkbox-item">
                  <input v-model="exportConfig.spec" type="checkbox" class="rounded border-slate-300" />
                  <span class="text-sm">规格参数</span>
                </label>
                <label class="checkbox-item">
                  <input v-model="exportConfig.power" type="checkbox" class="rounded border-slate-300" />
                  <span class="text-sm">功率数据</span>
                </label>
                <label class="checkbox-item">
                  <input v-model="exportConfig.energy" type="checkbox" class="rounded border-slate-300" />
                  <span class="text-sm">能耗数据</span>
                </label>
                <label class="checkbox-item">
                  <input v-model="exportConfig.runtime" type="checkbox" class="rounded border-slate-300" />
                  <span class="text-sm">运行数据</span>
                </label>
                <label class="checkbox-item">
                  <input v-model="exportConfig.load" type="checkbox" class="rounded border-slate-300" />
                  <span class="text-sm">负载数据</span>
                </label>
                <label class="checkbox-item">
                  <input v-model="exportConfig.status" type="checkbox" class="rounded border-slate-300" />
                  <span class="text-sm">状态信息</span>
                </label>
              </div>
            </div>

            <div class="mt-4">
              <label class="block text-sm font-medium mb-2">导出格式</label>
              <select v-model="exportConfig.format" class="filter-select w-full">
                <option value="csv">CSV文件（Excel可打开）</option>
              </select>
            </div>

            <div class="flex justify-end gap-3 mt-6">
              <button class="px-4 py-2 border border-slate-200 rounded-lg" @click="closeModal('export')">取消</button>
              <button class="px-4 py-2 bg-green-600 text-white rounded-lg" @click="exportReport">
                <Icon icon="fa6-solid:download" class="mr-2" />
                生成报表
              </button>
            </div>
          </div>
        </div>
      </div>
    </ProtoScaleContainer>
  </div>
</template>

<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import ProtoScaleContainer from '../ProtoScaleContainer.vue'

defineOptions({ name: 'NewLightDevice' })

type DeviceStatus = 'online' | 'offline'
type DeviceType = 'light' | 'dimmer' | 'gateway' | 'controller' | 'sensor'

type DeviceRow = {
  id: string
  code: string
  name: string
  type: DeviceType
  typeName: string
  area: string
  status: DeviceStatus
  controllerId?: string
  power: number
  energy: number
  runtimeToday: number
  totalRuntime: number
  loadRate: number
  lastComm: string
  specType: 'light' | 'dimmer' | 'gateway' | 'controller' | 'sensor'
  model: string
  capacity?: number
  channels?: number
  lightCount?: number
  lightPower?: number
  range?: string
}

const now = ref<Date>(new Date())
let timer: number | null = null

const currentTime = computed(() => {
  const d = now.value
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  const ss = String(d.getSeconds()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}:${ss}`
})

const devices = ref<DeviceRow[]>([
  {
    id: 'C001',
    code: 'LT-001',
    name: 'A1层走廊灯组',
    type: 'light',
    typeName: '普通照明',
    area: 'A座1层',
    status: 'online',
    controllerId: 'CT001',
    power: 76,
    energy: 12.4,
    runtimeToday: 8.5,
    totalRuntime: 128,
    loadRate: 42,
    lastComm: '2026-03-04 14:25:30',
    specType: 'light',
    model: 'Philips',
    lightCount: 6,
    lightPower: 72
  },
  {
    id: 'C002',
    code: 'LT-002',
    name: 'A1层会议室灯',
    type: 'light',
    typeName: '普通照明',
    area: 'A座1层',
    status: 'online',
    controllerId: 'CT001',
    power: 68,
    energy: 8.8,
    runtimeToday: 6.2,
    totalRuntime: 96,
    loadRate: 38,
    lastComm: '2026-03-04 14:20:15',
    specType: 'light',
    model: 'Philips',
    lightCount: 8,
    lightPower: 96
  },
  {
    id: 'D101',
    code: 'DM-101',
    name: 'B1展厅氛围灯',
    type: 'dimmer',
    typeName: '调光设备',
    area: 'B座1层',
    status: 'online',
    controllerId: 'CT002',
    power: 150,
    energy: 10.1,
    runtimeToday: 7.2,
    totalRuntime: 210,
    loadRate: 55,
    lastComm: '2026-03-04 14:18:40',
    specType: 'dimmer',
    model: 'DMX-1CH'
  },
  {
    id: 'GW01',
    code: 'GW-001',
    name: '智能网关-主机房',
    type: 'gateway',
    typeName: '智能网关',
    area: 'C座1层',
    status: 'offline',
    power: 12,
    energy: 0.2,
    runtimeToday: 0.5,
    totalRuntime: 520,
    loadRate: 0,
    lastComm: '2026-03-04 08:10:12',
    specType: 'gateway',
    model: 'GW-MQTT-01'
  },
  {
    id: 'CT001',
    code: 'CT-001',
    name: 'A座执行控制器',
    type: 'controller',
    typeName: '执行控制器',
    area: 'A座1层',
    status: 'online',
    power: 5,
    energy: 0.6,
    runtimeToday: 10.0,
    totalRuntime: 980,
    loadRate: 62,
    lastComm: '2026-03-04 14:26:01',
    specType: 'controller',
    model: 'LC-8CH-20A',
    capacity: 2000,
    channels: 8
  },
  {
    id: 'S01',
    code: 'SN-001',
    name: '照度传感器-大厅',
    type: 'sensor',
    typeName: '传感器',
    area: 'B座1层',
    status: 'online',
    power: 1,
    energy: 0.1,
    runtimeToday: 10.0,
    totalRuntime: 420,
    loadRate: 0,
    lastComm: '2026-03-04 14:25:55',
    specType: 'sensor',
    model: 'LUX-01',
    range: '0-2000lx'
  }
])

const filters = reactive({
  type: 'all',
  status: 'all',
  area: 'all',
  keyword: ''
})

const history = reactive({
  startTime: '2026-03-01T00:00',
  endTime: '2026-03-04T23:59',
  dimension: 'custom',
  type: 'all'
})

const page = ref(1)
const pageSize = 10

const filteredDevices = computed(() => {
  const list = devices.value
    .filter((d) => (filters.type === 'all' ? true : d.type === filters.type))
    .filter((d) => (filters.status === 'all' ? true : d.status === filters.status))
    .filter((d) => (filters.area === 'all' ? true : d.area === filters.area))
    .filter((d) => {
      if (!filters.keyword) return true
      const kw = filters.keyword.toLowerCase()
      return (
        d.name.toLowerCase().includes(kw) ||
        d.code.toLowerCase().includes(kw) ||
        d.model.toLowerCase().includes(kw) ||
        d.id.toLowerCase().includes(kw)
      )
    })
  return list
})

const totalItems = computed(() => filteredDevices.value.length)
const totalPages = computed(() => Math.max(1, Math.ceil(totalItems.value / pageSize)))
const startItem = computed(() => (totalItems.value === 0 ? 0 : (page.value - 1) * pageSize + 1))
const endItem = computed(() => Math.min(totalItems.value, page.value * pageSize))

const pagedDevices = computed(() => {
  const start = (page.value - 1) * pageSize
  return filteredDevices.value.slice(start, start + pageSize)
})

const stats = computed(() => {
  const totalCount = devices.value.length
  const onlineCount = devices.value.filter((d) => d.status === 'online').length
  const offlineCount = totalCount - onlineCount
  const controllerCount = devices.value.filter((d) => d.type === 'controller').length
  const controlledCircuits = devices.value.filter((d) => d.controllerId).length
  const totalEnergy = Number(
    devices.value.reduce((sum, d) => sum + (Number.isFinite(d.energy) ? d.energy : 0), 0).toFixed(1)
  )
  const totalCost = Number((totalEnergy * 0.8).toFixed(1))

  const controllers = devices.value.filter((d) => d.type === 'controller')
  const totalControllerCapacity = controllers.reduce((sum, d) => sum + (d.capacity || 0), 0)
  const totalControllerLoad = devices.value.filter((d) => d.controllerId).reduce((sum, d) => sum + d.power, 0)
  const usedCapacityPercent = totalControllerCapacity
    ? Math.round((totalControllerLoad / totalControllerCapacity) * 100)
    : 0
  const remainingCapacity = Math.max(0, totalControllerCapacity - totalControllerLoad)
  const maxCircuits = Math.floor(totalControllerCapacity / 100)
  const remainingCircuits = Math.floor(remainingCapacity / 100)
  const avgLoadRate = controllers.length
    ? Math.round(controllers.reduce((sum, d) => sum + d.loadRate, 0) / controllers.length)
    : 0

  return {
    totalCount,
    onlineCount,
    offlineCount,
    controllerCount,
    controlledCircuits,
    totalEnergy,
    totalCost,
    totalControllerCapacity,
    totalControllerLoad,
    usedCapacityPercent,
    remainingCapacity,
    maxCircuits,
    remainingCircuits,
    avgLoadRate
  }
})

const applyFilters = () => {
  page.value = 1
}

const resetFilters = () => {
  filters.type = 'all'
  filters.status = 'all'
  filters.area = 'all'
  filters.keyword = ''
  page.value = 1
}

type PageAction = 'first' | 'prev' | 'next' | 'last'
const goPage = (action: PageAction) => {
  if (action === 'first') page.value = 1
  if (action === 'prev') page.value = Math.max(1, page.value - 1)
  if (action === 'next') page.value = Math.min(totalPages.value, page.value + 1)
  if (action === 'last') page.value = totalPages.value
}

const detailModalVisible = ref(false)
const exportModalVisible = ref(false)
const detailRow = ref<DeviceRow | null>(null)

const exportConfig = reactive({
  basic: true,
  spec: true,
  power: true,
  energy: true,
  runtime: true,
  load: true,
  status: true,
  format: 'csv'
})

/**
 * 打开详情弹窗
 * @param row 设备行数据
 */
const openDetail = (row: DeviceRow) => {
  detailRow.value = row
  detailModalVisible.value = true
}

const openExportModal = () => {
  exportModalVisible.value = true
}

/**
 * 关闭弹窗
 * @param which 弹窗标识
 */
const closeModal = (which: 'detail' | 'export') => {
  if (which === 'detail') detailModalVisible.value = false
  if (which === 'export') exportModalVisible.value = false
}

const queryHistoryData = () => {
  // 原型为静态展示，这里不做真实查询，仅保留按钮交互位
}

const exportReport = () => {
  exportModalVisible.value = false
}

onMounted(() => {
  timer = window.setInterval(() => {
    now.value = new Date()
  }, 1000)
})

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
  timer = null
})
</script>

<style scoped>
.proto-root {
  font-family: 'Inter', system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Microsoft YaHei', Arial,
    sans-serif;
  background: var(--bg);
  color: var(--text);
  min-height: 100vh;
}

.proto-root {
  --primary: var(--el-color-primary);
  --success: var(--el-color-success);
  --warning: var(--el-color-warning);
  --danger: var(--el-color-danger);
  --text: var(--el-text-color-primary);
  --text-light: var(--el-text-color-regular);
  --border: var(--el-border-color);
  --bg: var(--app-content-bg-color);
  --panel-bg: var(--el-bg-color-overlay);
  --fill-light: var(--el-fill-color-light);
}

.main-content {
  margin-left: 0;
  padding: 20px;
}

.table-container {
  background: var(--panel-bg);
  border-radius: 12px;
  overflow-x: auto;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1600px;
}

.data-table th {
  background: var(--fill-light);
  padding: 14px 16px;
  text-align: left;
  font-weight: 500;
  font-size: 13px;
  color: #64748b;
  border-bottom: 1px solid var(--border);
  white-space: nowrap;
}

.data-table td {
  padding: 14px 16px;
  border-bottom: 1px solid var(--border);
  font-size: 14px;
  white-space: nowrap;
}

.data-table tr:hover td {
  background: var(--fill-light);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.online {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.offline {
  background: #fee2e2;
  color: #991b1b;
}

.type-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  background: var(--fill-light);
  color: var(--text);
}

.spec-container {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 180px;
}

.light-spec {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--fill-light);
  padding: 8px 12px;
  border-radius: 8px;
  border-left: 3px solid #3b82f6;
}

.light-count {
  font-weight: 600;
  color: #3b82f6;
}

.light-power {
  color: var(--text-light);
}

.dimmer-spec {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--fill-light);
  padding: 8px 12px;
  border-radius: 8px;
  border-left: 3px solid #8b5cf6;
}

.dimmer-spec :deep(svg) {
  color: #8b5cf6;
  font-size: 14px;
}

.controller-spec {
  display: flex;
  align-items: center;
  gap: 16px;
  background: var(--fill-light);
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #bfdbfe;
}

.controller-model {
  font-weight: 600;
  color: #1e40af;
}

.controller-params {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--text-light);
}

.gateway-spec {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--fill-light);
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #86efac;
}

.gateway-model {
  font-weight: 600;
  color: #166534;
}

.sensor-spec {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--fill-light);
  padding: 8px 12px;
  border-radius: 8px;
  border-left: 3px solid #10b981;
}

.sensor-model {
  font-weight: 500;
  color: #065f46;
}

.sensor-range {
  color: var(--text-light);
  font-size: 12px;
}

.action-btn {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: var(--primary);
  cursor: pointer;
  margin: 0 2px;
}

.action-btn:hover {
  background: var(--fill-light);
}

.filter-bar {
  background: var(--panel-bg);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.filter-select,
.filter-input {
  padding: 8px 12px;
  border: 1px solid var(--border);
  border-radius: 8px;
  font-size: 13px;
  min-width: 140px;
  background: var(--panel-bg);
}

.filter-input {
  min-width: 200px;
}

.filter-btn {
  padding: 8px 20px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
}

.reset-btn {
  padding: 8px 20px;
  background: var(--panel-bg);
  border: 1px solid var(--border);
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
}

.export-btn {
  padding: 8px 20px;
  background: var(--success);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
}

.stats-bar {
  background: var(--panel-bg);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  background: var(--fill-light);
  padding: 12px;
  border-radius: 8px;
  border-left: 3px solid var(--primary);
}

.stat-label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: var(--text);
}

.stat-unit {
  font-size: 12px;
  color: #94a3b8;
  margin-left: 2px;
}

.stat-desc {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
}

.pagination-info {
  font-size: 13px;
  color: #64748b;
}

.pagination-btn {
  padding: 6px 12px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: white;
  cursor: pointer;
  font-size: 12px;
}

.pagination-btn:hover {
  background: #f8fafc;
}

.pagination-btn.active {
  background: var(--primary);
  color: white;
  border-color: var(--primary);
}

.modal {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  align-items: center;
  justify-content: center;
}

.modal.active {
  display: flex;
}

.modal-content {
  background: var(--panel-bg);
  border-radius: 16px;
  width: 700px;
  max-width: 90%;
  padding: 24px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
}

.modal-close {
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: var(--fill-light);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.detail-item {
  background: var(--fill-light);
  padding: 12px;
  border-radius: 8px;
}

.detail-label {
  font-size: 12px;
  color: var(--text-light);
  margin-bottom: 4px;
}

.detail-value {
  font-size: 16px;
  font-weight: 600;
  word-break: break-word;
}

.history-panel {
  background: var(--panel-bg);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid var(--border);
}

.history-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--text);
  display: flex;
  align-items: center;
  gap: 8px;
}

.history-title :deep(svg) {
  color: var(--primary);
}

.query-section {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr auto;
  gap: 16px;
  align-items: end;
}

.date-range {
  display: flex;
  gap: 12px;
  align-items: center;
  background: var(--fill-light);
  padding: 12px;
  border-radius: 8px;
}

.date-input-group {
  flex: 1;
}

.date-input-group label {
  display: block;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
}

.date-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--border);
  border-radius: 6px;
  font-size: 13px;
}

.dimension-select {
  padding: 8px 12px;
  border: 1px solid var(--border);
  border-radius: 8px;
  font-size: 13px;
  min-width: 140px;
  background: white;
}

.query-btn {
  padding: 10px 24px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  height: 42px;
}

.query-btn:hover {
  background: #2563eb;
}

.analysis-panel {
  background: var(--panel-bg);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid var(--border);
}

.analysis-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--text);
}

.analysis-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.analysis-item {
  padding: 12px;
  background: var(--fill-light);
  border-radius: 8px;
}

.analysis-label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
}

.analysis-value {
  font-size: 18px;
  font-weight: 600;
}

.checkbox-group {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin: 16px 0;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: var(--fill-light);
  border-radius: 6px;
}
</style>
