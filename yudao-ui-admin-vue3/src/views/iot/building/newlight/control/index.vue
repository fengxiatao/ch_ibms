<!--
  原型复刻：02 照明控制.html
  说明：不包含原型左侧菜单（项目统一侧边栏已提供）；页面使用等比缩放容器适配 main 区域。
-->
<template>
  <div class="newlight-page wh-full">
    <ProtoScaleContainer :design-width="1440" :design-height="900" scale-by="width" class="wh-full">
      <div class="proto-root">
        <main class="main-content">
          <div class="view-section active">
            <div class="top-header">
              <div class="breadcrumb">首页 / 智慧照明 / 照明控制</div>
              <div style="color: #606266">👤 管理员</div>
            </div>

            <div class="device-management">
              <div class="filter-bar">
                <select v-model="filters.area" class="filter-select" @change="applyFilter">
                  <option value="all">全部区域</option>
                  <option v-for="area in areaOptions" :key="area" :value="area">{{ area }}</option>
                </select>
                <select v-model="filters.status" class="filter-select" @change="applyFilter">
                  <option value="all">全部状态</option>
                  <option value="on">开启</option>
                  <option value="off">关闭</option>
                  <option value="fault">故障</option>
                </select>
                <input v-model.trim="filters.keyword" type="text" class="filter-input" placeholder="请输入回路名称或编号" />
                <button class="filter-btn" @click="applyFilter">筛选</button>
                <button class="reset-btn" @click="resetFilter">重置</button>
                <span class="ml-auto text-xs text-slate-500">更新于 {{ refreshAtLabel }}</span>
              </div>

              <div class="group-tabs">
                <div class="group-tab" :class="{ active: activeTab === 'normal' }" @click="switchTab('normal')">
                  普通照明回路控制
                </div>
                <div class="group-tab" :class="{ active: activeTab === 'dimming' }" @click="switchTab('dimming')">
                  调光设备控制
                </div>
              </div>

              <div class="device-grid">
                <div v-if="filteredDevices.length === 0" class="empty-wrap">
                  <div style="font-size: 48px; margin-bottom: 16px">💡</div>
                  <div style="font-size: 16px; margin-bottom: 8px">暂无符合条件的设备</div>
                  <div style="font-size: 13px">请调整筛选条件后重试</div>
                </div>

                <div
                  v-for="device in filteredDevices"
                  v-else
                  :key="device.id"
                  class="device-card"
                  :class="{ on: device.status === 'on', 'active-control': expandedId === device.id }"
                  @click="openControl(device.id)"
                >
                  <div class="card-header">
                    <div class="device-name">{{ device.name }}</div>
                    <span v-if="device.status === 'on'" class="status-tag status-on">开启</span>
                    <span v-else-if="device.status === 'off'" class="status-tag status-off">关闭</span>
                    <span v-else class="status-tag status-fault">故障</span>
                  </div>

                  <div class="card-body">
                    <div class="info-row">
                      <span class="info-icon">📍</span>
                      <span>所属区域：{{ device.area || '-' }}</span>
                    </div>
                    <div class="info-row">
                      <span class="info-icon">🔢</span>
                      <span>编号：{{ device.code }}</span>
                    </div>
                    <div class="info-row">
                      <span class="info-icon">⚡</span>
                      <span>额定功率：{{ device.power }}</span>
                    </div>
                    <div class="info-row">
                      <Icon icon="fa6-solid:lightbulb" class="info-icon" />
                      <span>灯具数：{{ device.lightCount ?? '-' }} 盏</span>
                    </div>
                    <div v-if="activeTab === 'dimming'" class="info-row">
                      <Icon icon="fa6-solid:sun" class="info-icon" />
                      <span>当前亮度：{{ device.brightness }}%</span>
                    </div>
                  </div>

                  <button v-if="expandedId === device.id" class="close-control" @click.stop="closeControl">×</button>

                  <div v-if="expandedId === device.id" class="card-control-panel" @click.stop>
                    <div class="control-title">{{ activeTab === 'normal' ? '设备控制' : '调光控制' }}</div>

                    <div class="control-buttons">
                      <button
                        class="control-btn"
                        :class="{ active: device.status === 'on' }"
                        @click="controlDevice(device.id, 'on')"
                      >
                        <span>🔛 开启</span>
                      </button>
                      <button
                        class="control-btn"
                        :class="{ active: device.status === 'off' }"
                        @click="controlDevice(device.id, 'off')"
                      >
                        <span>🔌 关闭</span>
                      </button>
                      <button
                        v-if="activeTab === 'normal' && device.status === 'fault'"
                        class="control-btn"
                        @click="clearFault(device.id)"
                      >
                        <span>🔧 故障复位</span>
                      </button>
                    </div>

                    <div v-if="activeTab === 'dimming'" class="slider-container">
                      <div class="slider-label">
                        <span>亮度调节</span>
                        <span>{{ device.brightness }}%</span>
                      </div>
                      <input
                        class="range-slider"
                        type="range"
                        min="0"
                        max="100"
                        :value="device.brightness"
                        :disabled="device.status === 'off'"
                        @input="onBrightnessInput(device.id, ($event.target as HTMLInputElement).value)"
                      />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </main>

        <div v-if="toast.visible" class="toast">{{ toast.message }}</div>
      </div>
    </ProtoScaleContainer>
  </div>
</template>

<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import ProtoScaleContainer from '../ProtoScaleContainer.vue'
import * as LightingApi from '@/api/iot/building/lighting'
import { useUserStore } from '@/store/modules/user'

defineOptions({ name: 'NewLightControl' })

type DeviceStatus = 'on' | 'off' | 'fault'

type CircuitDevice = {
  id: number
  code: string
  name: string
  area: string          // areaName
  status: DeviceStatus
  rawStatus: number
  power: string         // 显示用，如 "96 W"
  brightness: number    // 0-100
  lightCount?: number
  circuitType: number   // 1-普通, 2-调光, 3-应急
}

const circuits = ref<CircuitDevice[]>([])
const loading = ref(false)
const lastRefreshAt = ref<Date | null>(null)
let refreshTimer: number | null = null

const userStore = useUserStore()
const operator = computed(() => {
  const u = userStore.getUser as { username?: string; nickname?: string } | undefined
  return u?.nickname || u?.username || 'admin'
})

const mapStatus = (s?: number): DeviceStatus =>
  s === 1 ? 'on' : s === 2 ? 'fault' : 'off'

const fmtRefreshAt = (d: Date | null) => {
  if (!d) return '加载中…'
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}`
}

const refreshData = async () => {
  loading.value = true
  try {
    const res = await LightingApi.getCircuitPage({
      pageNo: 1,
      pageSize: 100
    } as LightingApi.IbmsLightingCircuitPageReqVO)
    const list = (res as { list?: LightingApi.IbmsLightingCircuitVO[] })?.list ?? []
    circuits.value = list.map((c) => {
      const rated = c.ratedPower != null ? Number(c.ratedPower) : null
      return {
        id: c.id ?? 0,
        code: c.circuitCode ?? '-',
        name: c.circuitName ?? '-',
        area: c.areaName ?? '-',
        status: mapStatus(c.status),
        rawStatus: c.status ?? 0,
        power: rated != null && Number.isFinite(rated) ? `${rated} W` : '-',
        brightness: c.brightness ?? 0,
        lightCount: c.lightCount,
        circuitType: c.circuitType ?? 1
      }
    })
    lastRefreshAt.value = new Date()
  } finally {
    loading.value = false
  }
}

const activeTab = ref<'normal' | 'dimming'>('normal')

const filters = reactive({
  area: 'all',
  status: 'all',
  keyword: ''
})

const expandedId = ref<number | null>(null)

const toast = reactive({
  visible: false,
  message: ''
})
let toastTimer: number | null = null

const showToast = (message: string) => {
  toast.message = message
  toast.visible = true
  if (toastTimer) window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toast.visible = false
  }, 2000)
}

// 区域选项动态生成
const areaOptions = computed(() => {
  const set = new Set<string>()
  for (const c of circuits.value) {
    if (c.area && c.area !== '-') set.add(c.area)
  }
  return Array.from(set).sort()
})

const refreshAtLabel = computed(() => fmtRefreshAt(lastRefreshAt.value))

// 普通照明：circuitType=1 或 3；调光：circuitType=2
const filteredDevices = computed<CircuitDevice[]>(() => {
  const wantDimming = activeTab.value === 'dimming'
  return circuits.value
    .filter((c) => (wantDimming ? c.circuitType === 2 : c.circuitType !== 2))
    .filter((c) => (filters.area === 'all' ? true : c.area === filters.area))
    .filter((c) => (filters.status === 'all' ? true : c.status === filters.status))
    .filter((c) => {
      if (!filters.keyword) return true
      const kw = filters.keyword.toLowerCase()
      return c.name.toLowerCase().includes(kw) || c.code.toLowerCase().includes(kw)
    })
})

const applyFilter = () => {
  expandedId.value = null
}

const resetFilter = () => {
  filters.area = 'all'
  filters.status = 'all'
  filters.keyword = ''
  expandedId.value = null
}

const switchTab = (tab: 'normal' | 'dimming') => {
  activeTab.value = tab
  expandedId.value = null
}

const openControl = (id: number) => {
  expandedId.value = id
}

const closeControl = () => {
  expandedId.value = null
}

/**
 * 控制回路（开/关）
 */
const controlDevice = async (deviceId: number, action: 'on' | 'off') => {
  const target = circuits.value.find((c) => c.id === deviceId)
  if (!target) return
  const targetStatus = action === 'on' ? 1 : 0
  try {
    await LightingApi.controlCircuit(deviceId, targetStatus, operator.value)
    target.status = action
    target.rawStatus = targetStatus
    if (target.circuitType === 2 && action === 'on' && target.brightness === 0) {
      target.brightness = 100
    }
    if (target.circuitType === 2 && action === 'off') {
      target.brightness = 0
    }
    showToast(`已${action === 'on' ? '开启' : '关闭'}：${target.name}`)
  } catch {
    showToast(`操作失败：${target.name}`)
  }
}

/**
 * 故障复位（按"关闭"语义提交，让后端清除故障状态）
 */
const clearFault = async (deviceId: number) => {
  const target = circuits.value.find((c) => c.id === deviceId)
  if (!target) return
  try {
    await LightingApi.controlCircuit(deviceId, 0, operator.value)
    target.status = 'off'
    target.rawStatus = 0
    showToast(`已尝试复位：${target.name}`)
  } catch {
    showToast(`复位失败：${target.name}`)
  }
}

/**
 * 调光亮度输入（防抖：200ms 内连续滑动只发最后一次）
 */
let dimDebounce: number | null = null
const onBrightnessInput = (deviceId: number, value: string) => {
  const target = circuits.value.find((c) => c.id === deviceId)
  if (!target) return
  const v = Math.max(0, Math.min(100, Number.parseInt(value, 10) || 0))
  target.brightness = v
  if (v > 0 && target.status === 'off') {
    target.status = 'on'
    target.rawStatus = 1
  }
  if (v === 0 && target.status === 'on') {
    target.status = 'off'
    target.rawStatus = 0
  }
  if (dimDebounce) window.clearTimeout(dimDebounce)
  dimDebounce = window.setTimeout(async () => {
    try {
      await LightingApi.dimCircuit(deviceId, v, operator.value)
      showToast(`已调整【${target.name}】亮度至 ${v}%`)
    } catch {
      showToast(`调光失败：${target.name}`)
    }
  }, 250)
}

watch(
  () => activeTab.value,
  () => {
    filters.status = 'all'
  }
)

onMounted(() => {
  refreshData()
  refreshTimer = window.setInterval(refreshData, 30_000)
})

onBeforeUnmount(() => {
  if (refreshTimer) window.clearInterval(refreshTimer)
  refreshTimer = null
  if (toastTimer) window.clearTimeout(toastTimer)
  toastTimer = null
  if (dimDebounce) window.clearTimeout(dimDebounce)
  dimDebounce = null
})
</script>

<style scoped>
.proto-root {
  width: 100%;
  min-height: 100vh;
  background-color: var(--bg);
  color: var(--text);
  font-size: 14px;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.proto-root {
  --primary: var(--el-color-primary);
  --success: var(--el-color-success);
  --warning: var(--el-color-warning);
  --danger: var(--el-color-danger);
  --text: var(--el-text-color-primary);
  --text-regular: var(--el-text-color-regular);
  --border: var(--el-border-color);
  --bg: var(--app-content-bg-color);
  --panel-bg: var(--el-bg-color-overlay);
  --fill-light: var(--el-fill-color-light);
}

.main-content {
  margin-left: 0;
  width: 100%;
}

.top-header {
  height: 60px;
  background: var(--panel-bg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: var(--el-box-shadow-light);
  margin-bottom: 20px;
}

.breadcrumb {
  color: var(--text-regular);
}

.device-management {
  padding: 0 20px 30px;
}

.filter-bar {
  background: var(--panel-bg);
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 15px;
}

.filter-select,
.filter-input {
  height: 36px;
  border: 1px solid var(--border);
  border-radius: 4px;
  padding: 0 12px;
  outline: none;
  transition: border-color 0.3s;
}

.filter-select {
  width: 140px;
  background: var(--panel-bg);
}

.filter-input {
  width: 220px;
}

.filter-select:focus,
.filter-input:focus {
  border-color: var(--primary);
}

.filter-btn,
.reset-btn {
  height: 36px;
  padding: 0 18px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.filter-btn {
  background: var(--primary);
  color: white;
  border: none;
}

.filter-btn:hover {
  background: color-mix(in srgb, var(--primary) 82%, #fff 18%);
}

.reset-btn {
  background: var(--panel-bg);
  color: var(--text-regular);
  border: 1px solid var(--border);
}

.reset-btn:hover {
  color: var(--primary);
  border-color: var(--primary);
}

.group-tabs {
  display: flex;
  background: var(--panel-bg);
  border-radius: 4px;
  margin-bottom: 20px;
  overflow: hidden;
}

.group-tab {
  flex: 1;
  text-align: center;
  padding: 14px 0;
  cursor: pointer;
  color: var(--text-regular);
  transition: all 0.3s;
  border-bottom: 2px solid transparent;
}

.group-tab:hover {
  color: var(--primary);
}

.group-tab.active {
  color: var(--primary);
  border-bottom-color: var(--primary);
  background: rgba(64, 158, 255, 0.05);
}

.device-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

@media (max-width: 1400px) {
  .device-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .device-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.device-card {
  background: var(--panel-bg);
  border-radius: 8px;
  padding: 18px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid var(--border);
  position: relative;
  overflow: hidden;
}

.device-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

.device-card.on {
  border-left: 4px solid var(--success);
}

.device-card.active-control {
  grid-column: span 2;
  border-color: var(--primary);
  box-shadow: 0 10px 28px rgba(64, 158, 255, 0.18);
}

@media (max-width: 900px) {
  .device-card.active-control {
    grid-column: span 2;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 14px;
}

.device-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text);
  line-height: 1.3;
  flex: 1;
  margin-right: 10px;
}

.status-tag {
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 12px;
  flex-shrink: 0;
}

.status-on {
  background: rgba(103, 194, 58, 0.12);
  color: var(--success);
}

.status-off {
  background: rgba(144, 147, 153, 0.12);
  color: var(--el-text-color-placeholder);
}

.status-fault {
  background: rgba(245, 108, 108, 0.12);
  color: var(--danger);
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  color: var(--text-regular);
  font-size: 13px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-icon {
  width: 20px;
  margin-right: 8px;
}

.close-control {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.06);
  cursor: pointer;
  color: var(--text-regular);
  font-size: 18px;
  line-height: 26px;
  text-align: center;
}

.card-control-panel {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border);
}

.control-title {
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--text);
}

.control-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.control-btn {
  flex: 1;
  min-width: 110px;
  border: 1px solid var(--border);
  background: var(--panel-bg);
  border-radius: 6px;
  padding: 10px 12px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-regular);
}

.control-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.control-btn.active {
  border-color: var(--primary);
  background: rgba(64, 158, 255, 0.08);
  color: var(--primary);
}

.slider-container {
  margin-top: 14px;
}

.slider-label {
  display: flex;
  justify-content: space-between;
  color: var(--text-regular);
  font-size: 13px;
  margin-bottom: 8px;
}

.range-slider {
  width: 100%;
  height: 6px;
  border-radius: 999px;
  background: var(--fill-light);
  outline: none;
  -webkit-appearance: none;
  appearance: none;
}

.range-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--primary);
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.35);
}

.range-slider:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.toast {
  position: fixed;
  left: 50%;
  bottom: 40px;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.75);
  color: #fff;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 13px;
  z-index: 9999;
  max-width: 80%;
  text-align: center;
}

.empty-wrap {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  color: var(--el-text-color-placeholder);
  background: var(--panel-bg);
  border-radius: 8px;
  border: 1px dashed var(--border);
}
</style>
