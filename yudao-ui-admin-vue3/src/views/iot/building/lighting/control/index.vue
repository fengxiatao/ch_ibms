<template>
  <div class="lighting-control-page">
    <!-- 筛选条件 -->
    <ContentWrap>
      <el-form :inline="true" :model="queryParams" class="-mb-15px">
        <el-form-item label="区域" prop="areaId">
          <el-select v-model="queryParams.areaId" placeholder="全部区域" clearable class="!w-160px">
            <el-option label="A区一层" value="A1" />
            <el-option label="A区二层" value="A2" />
            <el-option label="B区展厅" value="B1" />
            <el-option label="公共区域" value="public" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable class="!w-140px">
            <el-option label="开启" :value="1" />
            <el-option label="关闭" :value="0" />
            <el-option label="故障" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="回路" prop="circuitName">
          <el-input
            v-model="queryParams.circuitName"
            placeholder="请输入回路名称或编号"
            clearable
            class="!w-200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" /> 筛选
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <!-- 分类标签页 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="普通照明回路控制" name="normal" />
        <el-tab-pane label="调光设备控制" name="dimming" />
      </el-tabs>

      <!-- 设备卡片网格 -->
      <div v-loading="loading" class="device-grid">
        <div
          v-for="device in deviceList"
          :key="device.id"
          class="device-card"
          :class="{ 'is-on': device.status === 1, 'is-expanded': expandedId === device.id }"
          @click="toggleExpand(device)"
        >
          <div class="card-header">
            <div class="device-name">
              <span
                class="device-icon"
                :style="{
                  filter: device.status === 1 ? 'none' : 'grayscale(100%)',
                  opacity: device.status === 1 ? 1 : 0.6
                }"
              >
                {{ activeTab === 'dimming' ? '🔅' : '🔌' }}
              </span>
              {{ device.circuitName }}
            </div>
            <el-tag :type="getStatusType(device.status)" size="small">
              {{ getStatusLabel(device.status) }}
            </el-tag>
          </div>
          <div class="card-body">
            <div class="info-row">
              <span class="info-icon">📍</span>
              <span>{{ device.areaName || '未分配区域' }}</span>
            </div>
            <div class="info-row">
              <span class="info-icon">⚡</span>
              <span>{{ device.loadDesc || 'LED灯具' }}</span>
            </div>
            <div v-if="activeTab === 'dimming'" class="info-row">
              <span class="info-icon">💡</span>
              <span :style="{ color: device.status === 1 ? '#d48806' : '#909399' }">
                亮度 {{ device.brightness || 0 }}% · 色温 {{ device.colorTemp || 4000 }}K
              </span>
            </div>
            <div v-else class="info-row">
              <span class="info-icon">🔋</span>
              <span>额定 {{ device.ratedPower || 0 }}W</span>
            </div>
          </div>

          <!-- 展开的控制面板 -->
          <div v-if="expandedId === device.id" class="control-panel" @click.stop>
            <div class="control-title">{{ activeTab === 'dimming' ? '灯光调节' : '回路控制' }}</div>
            <div class="control-buttons">
              <el-button
                :type="device.status === 1 ? 'primary' : 'default'"
                @click="setDeviceStatus(device, 1)"
              >
                💡 开启
              </el-button>
              <el-button
                :type="device.status === 0 ? 'primary' : 'default'"
                @click="setDeviceStatus(device, 0)"
              >
                🌙 关闭
              </el-button>
            </div>

            <template v-if="activeTab === 'dimming'">
              <div class="slider-container">
                <div class="slider-label">
                  <span>亮度</span>
                  <span>{{ device.brightness || 0 }}%</span>
                </div>
                <el-slider
                  v-model="device.brightness"
                  :disabled="device.status !== 1"
                  @change="updateBrightness(device)"
                />
              </div>
              <div class="slider-container">
                <div class="slider-label">
                  <span>色温</span>
                  <span>{{ device.colorTemp || 4000 }}K</span>
                </div>
                <el-slider
                  v-model="device.colorTemp"
                  :min="2700"
                  :max="6500"
                  :step="100"
                  :disabled="device.status !== 1"
                  @change="updateColorTemp(device)"
                />
              </div>
            </template>
            <template v-else>
              <div class="delay-buttons">
                <el-button size="small" @click="setDelayOff(device, 10)">10分钟后关</el-button>
                <el-button size="small" @click="setDelayOff(device, 30)">30分钟后关</el-button>
              </div>
            </template>

            <el-button class="close-btn" link @click.stop="expandedId = null">
              <Icon icon="ep:close" />
            </el-button>
          </div>
        </div>
      </div>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import * as LightingApi from '@/api/iot/building/lighting'

defineOptions({ name: 'LightingControl' })

const message = useMessage()
const loading = ref(false)
const activeTab = ref('normal')
const deviceList = ref<any[]>([])
const total = ref(0)
const expandedId = ref<number | null>(null)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 12,
  areaId: undefined as string | undefined,
  status: undefined as number | undefined,
  circuitName: undefined as string | undefined,
  circuitType: 1 // 1-普通, 4-调光
})

const getStatusType = (status: number) => {
  const map: Record<number, string> = { 0: 'info', 1: 'warning', 2: 'danger' }
  return map[status] || 'info'
}

const getStatusLabel = (status: number) => {
  const map: Record<number, string> = { 0: '关闭', 1: '开启', 2: '故障' }
  return map[status] || '未知'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await LightingApi.getCircuitPage(queryParams)
    deviceList.value = data.list
    total.value = data.total
  } catch (e) {
    console.error('获取回路列表失败', e)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.areaId = undefined
  queryParams.status = undefined
  queryParams.circuitName = undefined
  handleQuery()
}

const handleTabChange = (tab: string) => {
  queryParams.circuitType = tab === 'dimming' ? 4 : 1
  queryParams.pageNo = 1
  expandedId.value = null
  getList()
}

const toggleExpand = (device: any) => {
  if (expandedId.value === device.id) {
    expandedId.value = null
  } else {
    expandedId.value = device.id
  }
}

const setDeviceStatus = async (device: any, status: number) => {
  try {
    await LightingApi.controlCircuit(device.id, status, 'admin')
    device.status = status
    message.success(status === 1 ? '回路已开启' : '回路已关闭')
  } catch (e) {
    message.error('操作失败')
  }
}

const updateBrightness = async (device: any) => {
  try {
    await LightingApi.dimCircuit(device.id, device.brightness, 'admin')
    message.success('亮度调节成功')
  } catch (e) {
    message.error('调光失败')
  }
}

const updateColorTemp = (device: any) => {
  message.success(`色温已调节至 ${device.colorTemp}K`)
}

const setDelayOff = (device: any, minutes: number) => {
  message.success(`${device.circuitName} 将在 ${minutes} 分钟后关闭`)
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.lighting-control-page {
  box-sizing: border-box;
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
}

.device-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
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
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 16px;
  background: var(--el-bg-color);
  transition: all 0.3s;
  cursor: pointer;
  position: relative;
  overflow: hidden;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    border-color: var(--el-color-primary);
  }

  &.is-on {
    border-color: var(--el-color-warning);
  }

  &.is-expanded {
    grid-column: span 2;
    border: 2px solid var(--el-color-primary);
    z-index: 10;
  }

  &.is-on::before {
    content: '';
    position: absolute;
    inset: 0;
    background: rgba(var(--el-color-warning-rgb), 0.12);
    pointer-events: none;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .device-name {
    font-size: 14px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .card-body {
    padding-top: 10px;
    border-top: 1px solid var(--el-border-color-lighter);
  }

  .info-row {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
    color: var(--el-text-color-regular);
    font-size: 13px;

    .info-icon {
      width: 24px;
      text-align: center;
      margin-right: 5px;
    }
  }

  .control-panel {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 2px solid var(--el-color-primary);
    animation: slideDown 0.3s ease;

    .control-title {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      margin-bottom: 12px;
      text-transform: uppercase;
      letter-spacing: 1px;
    }

    .control-buttons {
      display: flex;
      gap: 10px;
      margin-bottom: 16px;
    }

    .delay-buttons {
      display: flex;
      gap: 8px;
      margin-top: 12px;
    }

    .slider-container {
      margin-top: 12px;

      .slider-label {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;
        font-size: 13px;
        color: var(--el-text-color-regular);
      }
    }

    .close-btn {
      position: absolute;
      top: 10px;
      right: 10px;
      font-size: 18px;
    }
  }
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
