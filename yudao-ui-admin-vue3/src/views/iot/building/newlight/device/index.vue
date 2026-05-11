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
              <p class="text-sm text-slate-500 mt-1">智能网关与执行控制器列表</p>
            </div>
            <div class="text-sm text-slate-500">更新于 {{ refreshAtLabel }}</div>
          </div>

          <div class="stats-bar">
            <div class="stats-grid">
              <div class="stat-card">
                <div class="stat-label">设备总数</div>
                <div class="stat-value">{{ overviewStats.totalCount }}</div>
                <div class="stat-desc">
                  在线 {{ overviewStats.onlineCount }} · 离线 {{ overviewStats.offlineCount }}
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-label">执行控制器</div>
                <div class="stat-value">{{ overviewStats.controllerCount }}</div>
                <div class="stat-desc">智能网关 {{ overviewStats.gatewayCount }} 台</div>
              </div>
              <div class="stat-card">
                <div class="stat-label">额定总功率</div>
                <div class="stat-value">
                  {{ overviewStats.totalPower }}<span class="stat-unit">kW</span>
                </div>
                <div class="stat-desc">基于回路 rated_power 之和</div>
              </div>
              <div class="stat-card">
                <div class="stat-label">实时负载功率</div>
                <div class="stat-value">
                  {{ overviewStats.currentPower }}<span class="stat-unit">kW</span>
                </div>
                <div class="stat-desc">
                  共 {{ overviewStats.lightTotalCount }} 盏灯具
                </div>
              </div>
            </div>
          </div>

          <div class="filter-bar">
            <select v-model="filters.type" class="filter-select">
              <option value="all">全部设备类型</option>
              <option value="gateway">智能网关</option>
              <option value="controller">执行控制器</option>
            </select>

            <select v-model="filters.status" class="filter-select">
              <option value="all">全部状态</option>
              <option value="online">在线</option>
              <option value="offline">离线</option>
              <option value="fault">故障</option>
            </select>

            <select v-model="filters.area" class="filter-select">
              <option value="all">全部区域</option>
              <option v-for="area in areaOptions" :key="area" :value="area">{{ area }}</option>
            </select>

            <input
              v-model.trim="filters.keyword"
              type="text"
              class="filter-input"
              placeholder="搜索设备名称/编号/型号"
            />

            <button class="filter-btn" @click="applyFilters">
              <Icon icon="fa6-solid:magnifying-glass" class="mr-2" />
              查询
            </button>

            <button class="reset-btn" @click="resetFilters">
              <Icon icon="fa6-solid:arrow-rotate-left" class="mr-2" />
              重置
            </button>
          </div>

          <div class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>设备编号</th>
                  <th>设备名称</th>
                  <th>设备类型</th>
                  <th>品牌 / 型号</th>
                  <th>规格 / 网络</th>
                  <th>所属区域</th>
                  <th>状态</th>
                  <th>关联</th>
                  <th>最后通信</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="pagedDevices.length === 0">
                  <td colspan="10" class="empty-row">
                    {{ loading ? '加载中…' : '暂无设备数据' }}
                  </td>
                </tr>
                <tr v-for="row in pagedDevices" :key="`${row.type}-${row.id}`">
                  <td>{{ row.code }}</td>
                  <td>{{ row.name }}</td>
                  <td>
                    <span class="type-tag">{{ row.typeName }}</span>
                  </td>
                  <td>
                    <div class="spec-container">
                      <div class="font-medium">{{ row.brand }}</div>
                      <div class="text-xs text-slate-500">{{ row.model }}</div>
                    </div>
                  </td>
                  <td>
                    <div v-if="row.type === 'gateway'" class="spec-container">
                      <div class="text-xs text-slate-500">协议：{{ row.protocol || '-' }}</div>
                      <div class="text-xs text-slate-500">
                        {{ row.ipAddress || '-' }}<span v-if="row.port">:{{ row.port }}</span>
                      </div>
                    </div>
                    <div v-else class="spec-container">
                      <div class="text-xs text-slate-500">通道数：{{ row.channelCount ?? '-' }}</div>
                      <div class="text-xs text-slate-500">
                        调光：{{ row.dimmable ? '支持' : '不支持' }}
                      </div>
                    </div>
                  </td>
                  <td>{{ row.area }}</td>
                  <td>
                    <span class="status-badge" :class="row.status">
                      {{ row.status === 'online' ? '在线' : row.status === 'fault' ? '故障' : '离线' }}
                    </span>
                  </td>
                  <td>
                    <span v-if="row.type === 'controller' && row.gatewayName">
                      网关：{{ row.gatewayName }}
                    </span>
                    <span v-else>-</span>
                  </td>
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
              显示 <span>{{ startItem }}</span> - <span>{{ endItem }}</span> 条，共
              <span>{{ totalItems }}</span> 条
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

        <div class="modal" :class="{ active: detailModalVisible }" @click.self="closeDetail">
          <div class="modal-content">
            <div class="modal-header">
              <div class="modal-title">设备详情</div>
              <div class="modal-close" @click="closeDetail">
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
                <div class="detail-label">设备类型</div>
                <div class="detail-value">{{ detailRow.typeName }}</div>
              </div>
              <div class="detail-item">
                <div class="detail-label">品牌</div>
                <div class="detail-value">{{ detailRow.brand }}</div>
              </div>
              <div class="detail-item">
                <div class="detail-label">型号</div>
                <div class="detail-value">{{ detailRow.model }}</div>
              </div>
              <div class="detail-item">
                <div class="detail-label">所属区域</div>
                <div class="detail-value">{{ detailRow.area }}</div>
              </div>
              <div class="detail-item">
                <div class="detail-label">状态</div>
                <div class="detail-value">
                  {{
                    detailRow.status === 'online'
                      ? '在线'
                      : detailRow.status === 'fault'
                      ? '故障'
                      : '离线'
                  }}
                </div>
              </div>
              <div class="detail-item">
                <div class="detail-label">最后通信</div>
                <div class="detail-value">{{ detailRow.lastComm }}</div>
              </div>
              <template v-if="detailRow.type === 'gateway'">
                <div class="detail-item">
                  <div class="detail-label">协议类型</div>
                  <div class="detail-value">{{ detailRow.protocol || '-' }}</div>
                </div>
                <div class="detail-item">
                  <div class="detail-label">IP / 端口</div>
                  <div class="detail-value">
                    {{ detailRow.ipAddress || '-' }}<span v-if="detailRow.port">:{{ detailRow.port }}</span>
                  </div>
                </div>
              </template>
              <template v-else>
                <div class="detail-item">
                  <div class="detail-label">通道数</div>
                  <div class="detail-value">{{ detailRow.channelCount ?? '-' }}</div>
                </div>
                <div class="detail-item">
                  <div class="detail-label">支持调光</div>
                  <div class="detail-value">{{ detailRow.dimmable ? '是' : '否' }}</div>
                </div>
                <div class="detail-item">
                  <div class="detail-label">所属网关</div>
                  <div class="detail-value">{{ detailRow.gatewayName || '-' }}</div>
                </div>
              </template>
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
import * as LightingApi from '@/api/iot/building/lighting'

defineOptions({ name: 'NewLightDevice' })

type DeviceStatus = 'online' | 'offline' | 'fault'
type DeviceType = 'gateway' | 'controller'

type DeviceRow = {
  id: number
  code: string
  name: string
  type: DeviceType
  typeName: string
  area: string
  status: DeviceStatus
  rawStatus: number
  brand: string
  model: string
  protocol?: string
  ipAddress?: string
  port?: number
  channelCount?: number
  dimmable?: boolean
  gatewayId?: number
  gatewayName?: string
  lastComm: string
}

const devices = ref<DeviceRow[]>([])
const stats = ref<LightingApi.IbmsLightingStatisticsVO>({})
const loading = ref(false)
const lastRefreshAt = ref<Date | null>(null)
let refreshTimer: number | null = null

const fmtTime = (t: unknown): string => {
  if (!t) return '-'
  const d = new Date(t as string | number | Date)
  if (Number.isNaN(d.getTime())) return '-'
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  const ss = String(d.getSeconds()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}:${ss}`
}

const mapStatus = (s?: number): DeviceStatus => (s === 1 ? 'online' : s === 2 ? 'fault' : 'offline')

const refreshData = async () => {
  loading.value = true
  try {
    const [gw, ct, st] = await Promise.all([
      LightingApi.getGatewayPage({ pageNo: 1, pageSize: 100 } as LightingApi.IbmsLightingDevicePageReqVO),
      LightingApi.getControllerPage({ pageNo: 1, pageSize: 100 } as LightingApi.IbmsLightingDevicePageReqVO),
      LightingApi.getStatistics()
    ])
    const gws = (gw as { list?: LightingApi.IbmsLightingGatewayVO[] })?.list ?? []
    const cts = (ct as { list?: LightingApi.IbmsLightingControllerVO[] })?.list ?? []

    const rows: DeviceRow[] = []
    for (const g of gws) {
      rows.push({
        id: g.id ?? 0,
        code: g.gatewayCode ?? '-',
        name: g.gatewayName ?? '-',
        type: 'gateway',
        typeName: '智能网关',
        area: g.installLocation ?? '-',
        status: mapStatus(g.status),
        rawStatus: g.status ?? 0,
        brand: g.brand ?? '-',
        model: g.model ?? '-',
        protocol: g.protocolType ?? undefined,
        ipAddress: g.ipAddress ?? undefined,
        port: g.port ?? undefined,
        lastComm: fmtTime(g.lastCommunicateTime)
      })
    }
    for (const c of cts) {
      rows.push({
        id: c.id ?? 0,
        code: c.controllerCode ?? '-',
        name: c.controllerName ?? '-',
        type: 'controller',
        typeName: '执行控制器',
        area: c.installLocation ?? '-',
        status: mapStatus(c.status),
        rawStatus: c.status ?? 0,
        brand: c.brand ?? '-',
        model: c.model ?? '-',
        channelCount: c.channelCount ?? undefined,
        dimmable: c.dimmable ?? undefined,
        gatewayId: c.gatewayId ?? undefined,
        gatewayName: c.gatewayName ?? undefined,
        lastComm: fmtTime(c.lastCommunicateTime)
      })
    }
    devices.value = rows
    stats.value = (st ?? {}) as LightingApi.IbmsLightingStatisticsVO
    lastRefreshAt.value = new Date()
  } finally {
    loading.value = false
  }
}

const filters = reactive({
  type: 'all' as 'all' | DeviceType,
  status: 'all' as 'all' | DeviceStatus,
  area: 'all',
  keyword: ''
})

const page = ref(1)
const pageSize = 10

// 区域选项动态生成
const areaOptions = computed(() => {
  const set = new Set<string>()
  for (const d of devices.value) {
    if (d.area && d.area !== '-') set.add(d.area)
  }
  return Array.from(set).sort()
})

const filteredDevices = computed(() => {
  return devices.value
    .filter((d) => (filters.type === 'all' ? true : d.type === filters.type))
    .filter((d) => (filters.status === 'all' ? true : d.status === filters.status))
    .filter((d) => (filters.area === 'all' ? true : d.area === filters.area))
    .filter((d) => {
      if (!filters.keyword) return true
      const kw = filters.keyword.toLowerCase()
      return (
        d.name.toLowerCase().includes(kw) ||
        d.code.toLowerCase().includes(kw) ||
        d.model.toLowerCase().includes(kw)
      )
    })
})

const totalItems = computed(() => filteredDevices.value.length)
const totalPages = computed(() => Math.max(1, Math.ceil(totalItems.value / pageSize)))
const startItem = computed(() => (totalItems.value === 0 ? 0 : (page.value - 1) * pageSize + 1))
const endItem = computed(() => Math.min(totalItems.value, page.value * pageSize))

const pagedDevices = computed(() => {
  const start = (page.value - 1) * pageSize
  return filteredDevices.value.slice(start, start + pageSize)
})

const overviewStats = computed(() => {
  const totalCount = (stats.value.gatewayTotalCount ?? 0) + (stats.value.controllerTotalCount ?? 0)
  const onlineCount = (stats.value.gatewayOnlineCount ?? 0) + (stats.value.controllerOnlineCount ?? 0)
  const offlineCount = Math.max(0, totalCount - onlineCount)
  return {
    totalCount,
    onlineCount,
    offlineCount,
    gatewayCount: stats.value.gatewayTotalCount ?? 0,
    controllerCount: stats.value.controllerTotalCount ?? 0,
    lightTotalCount: stats.value.lightTotalCount ?? 0,
    totalPower: stats.value.totalPower ?? 0,
    currentPower: stats.value.currentPower ?? 0
  }
})

const refreshAtLabel = computed(() => {
  const d = lastRefreshAt.value
  if (!d) return '加载中…'
  return fmtTime(d)
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
const detailRow = ref<DeviceRow | null>(null)

const openDetail = (row: DeviceRow) => {
  detailRow.value = row
  detailModalVisible.value = true
}

const closeDetail = () => {
  detailModalVisible.value = false
}

onMounted(() => {
  refreshData()
  refreshTimer = window.setInterval(refreshData, 30_000)
})

onBeforeUnmount(() => {
  if (refreshTimer) window.clearInterval(refreshTimer)
  refreshTimer = null
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

.empty-row {
  text-align: center;
  padding: 32px 16px !important;
  color: var(--text-light);
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
