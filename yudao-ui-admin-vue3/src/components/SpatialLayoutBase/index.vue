<template>
  <div class="spatial-layout-base">
    <el-row :gutter="20" class="layout-row">
      <!-- 左侧面板（可选） -->
      <el-col v-if="showLeftPanel" :span="leftPanelSpan" class="left-panel-col">
        <el-card shadow="hover" class="panel-card">
          <template #header>
            <div class="card-header">
              <Icon icon="ep:office-building" />
              <span class="ml-2">建筑楼层</span>
            </div>
          </template>
          <slot name="left-panel">
            <!-- 默认显示建筑楼层树 -->
            <BuildingFloorTree
              :selected-floor-id="floorId"
              @floor-select="handleFloorSelect"
            />
          </slot>
        </el-card>
      </el-col>

      <!-- 中间主区域：平面图展示 -->
      <el-col :span="mainAreaSpan" class="main-area-col">
        <el-card shadow="hover" class="main-card">
          <template #header>
            <div class="card-header">
              <Icon icon="ep:location" />
              <span class="ml-2">{{ currentFloorName || '请选择楼层' }}</span>
              <el-tag v-if="floorId && deviceCount > 0" type="success" class="ml-2" size="small">
                {{ deviceCount }} 个{{ deviceLabel }}
              </el-tag>
            </div>
          </template>

          <!-- 紧凑工具栏（当右侧面板隐藏时显示） -->
          <div v-if="!showRightPanel" class="compact-toolbar">
            <SpatialControlPanel
              :selected-floor-id="floorId"
              :enable-edit="enableEdit"
              @building-change="handleBuildingChange"
              @floor-change="handleFloorChange"
              @layer-change="handleLayerChange"
              @refresh="handleRefresh"
              @fullscreen="handleFullscreen"
              @add="handleAdd"
            />
          </div>

          <!-- 平面图容器 -->
          <FloorMapContainer
            :floor-id="floorId"
            :devices="devices"
            :visible-layers="visibleLayers"
            :show-offline-devices="showOfflineDevices"
            @device-click="handleDeviceClick"
          />
        </el-card>
      </el-col>

      <!-- 右侧控制面板（可选） -->
      <el-col v-if="showRightPanel" :span="rightPanelSpan" class="right-panel-col">
        <el-card shadow="hover" class="control-card">
          <template #header>
            <div class="card-header">
              <Icon :icon="rightPanelTitleIcon" />
              <span class="ml-2">{{ rightPanelTitle }}</span>
            </div>
          </template>

          <slot name="right-panel">
            <!-- 默认使用统一的空间控制面板 -->
            <SpatialControlPanel
              :selected-floor-id="floorId"
              :enable-edit="enableEdit"
              @building-change="handleBuildingChange"
              @floor-change="handleFloorChange"
              @layer-change="handleLayerChange"
              @refresh="handleRefresh"
              @fullscreen="handleFullscreen"
              @add="handleAdd"
            />
          </slot>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import FloorMapContainer from './FloorMapContainer.vue'
import SpatialControlPanel from './SpatialControlPanel.vue'
import BuildingFloorTree from './BuildingFloorTree.vue'
import * as FloorApi from '@/api/iot/spatial/floor'

interface Props {
  // 设备数据
  devices?: any[]
  // 设备标签
  deviceLabel?: string
  // 是否显示离线设备
  showOfflineDevices?: boolean
  // 是否启用编辑模式
  enableEdit?: boolean
  // 面板显示控制
  showLeftPanel?: boolean
  showRightPanel?: boolean
  leftPanelSpan?: number
  rightPanelSpan?: number
  // 初始楼层ID
  initialFloorId?: number
  // 右侧面板标题
  rightPanelTitle?: string
  rightPanelTitleIcon?: string
}

const props = withDefaults(defineProps<Props>(), {
  devices: () => [],
  deviceLabel: '设备',
  showOfflineDevices: true,
  enableEdit: false,
  showLeftPanel: false,
  showRightPanel: true,
  leftPanelSpan: 6,
  rightPanelSpan: 6,
  initialFloorId: undefined,
  rightPanelTitle: '空间控制',
  rightPanelTitleIcon: 'ep:setting'
})

const emit = defineEmits<{
  floorChange: [floorId: number | undefined]
  deviceClick: [device: any]
  refresh: []
  fullscreen: []
  add: []
}>()

// 状态
const floorId = ref<number | undefined>(props.initialFloorId)
const currentFloorName = ref<string>('')
const visibleLayers = ref<string[]>(['device', 'area'])

// 计算属性
const mainAreaSpan = computed(() => {
  let span = 24
  if (props.showLeftPanel) span -= props.leftPanelSpan
  if (props.showRightPanel) span -= props.rightPanelSpan
  return span
})

const deviceCount = computed(() => {
  if (!floorId.value) return 0
  return props.devices.filter(device => device.floorId === floorId.value).length
})

// 方法
const loadFloorInfo = async (floorIdValue: number) => {
  try {
    const floor = await FloorApi.getFloor(floorIdValue)
    currentFloorName.value = floor.name || ''
  } catch (error) {
    console.error('[空间布局基础组件] 加载楼层信息失败:', error)
    currentFloorName.value = ''
  }
}

const handleFloorSelect = (floor: any) => {
  handleFloorChange(floor.id)
}

const handleFloorChange = async (newFloorId: number | undefined) => {
  floorId.value = newFloorId
  if (newFloorId) {
    await loadFloorInfo(newFloorId)
  } else {
    currentFloorName.value = ''
  }
  emit('floorChange', newFloorId)
}

const handleBuildingChange = (_buildingId: number | undefined) => {
  // 建筑变化时，可以触发相关事件
  // 如果需要，可以在这里处理
}

const handleLayerChange = (layers: string[]) => {
  visibleLayers.value = layers
}

const handleDeviceClick = (device: any) => {
  emit('deviceClick', device)
}

const handleRefresh = () => {
  emit('refresh')
}

const handleFullscreen = () => {
  emit('fullscreen')
}

const handleAdd = () => {
  emit('add')
}

// 监听初始楼层ID
watch(() => props.initialFloorId, (newFloorId) => {
  if (newFloorId && newFloorId !== floorId.value) {
    handleFloorChange(newFloorId)
  }
}, { immediate: true })

// 初始化
if (props.initialFloorId) {
  loadFloorInfo(props.initialFloorId)
}
</script>

<style scoped lang="scss">
.spatial-layout-base {
  width: 100%;
  min-height: 600px;

  .layout-row {
    min-height: 600px;
  }

  .left-panel-col,
  .right-panel-col {
    .panel-card {
      height: 100%;
      min-height: 600px;
    }
  }

  .main-area-col {
    .main-card {
      height: 100%;
      min-height: 600px;
      display: flex;
      flex-direction: column;

      :deep(.el-card__body) {
        flex: 1;
        display: flex;
        flex-direction: column;
        overflow: hidden;
      }

      .card-header {
        display: flex;
        align-items: center;
        font-weight: 500;
      }

      .compact-toolbar {
        margin-bottom: 16px;
        padding: 12px;
        background: #f5f7fa;
        border-radius: 4px;
        flex-shrink: 0;
      }
    }
  }

  .right-panel-col {
    .control-card {
      height: 100%;
      min-height: 600px;

      .card-header {
        display: flex;
        align-items: center;
        font-weight: 500;
      }
    }
  }
}
</style>

