<template>
  <div class="spatial-control-panel">
    <!-- 空间管理 -->
    <div class="control-section">
      <div class="section-label">
        <Icon icon="ep:office-building" />
        <span class="ml-1">建筑</span>
      </div>
      <el-select
        v-model="selectedBuildingId"
        placeholder="选择建筑"
        @change="handleBuildingChange"
        class="control-select"
        size="small"
        clearable
      >
        <el-option
          v-for="building in buildingList"
          :key="building.id"
          :label="building.name"
          :value="building.id"
        />
      </el-select>
    </div>

    <div class="control-section">
      <div class="section-label">
        <Icon icon="ep:files" />
        <span class="ml-1">楼层</span>
      </div>
      <div class="floor-controls">
        <el-button
          :disabled="!canGoUp"
          @click="goUpFloor"
          size="small"
          circle
          class="floor-nav-btn"
        >
          <Icon icon="ep:arrow-up" />
        </el-button>
        <el-select
          v-model="selectedFloorId"
          placeholder="选择楼层"
          @change="handleFloorChange"
          class="control-select floor-select"
          size="small"
          clearable
        >
          <el-option
            v-for="floor in floors"
            :key="floor.id"
            :label="floor.name"
            :value="floor.id"
          />
        </el-select>
        <el-button
          :disabled="!canGoDown"
          @click="goDownFloor"
          size="small"
          circle
          class="floor-nav-btn"
        >
          <Icon icon="ep:arrow-down" />
        </el-button>
      </div>
    </div>

    <!-- 图层选择 -->
    <div class="control-section">
      <div class="section-label">
        <Icon icon="ep:menu" />
        <span class="ml-1">图层</span>
      </div>
      <el-checkbox-group v-model="visibleLayers" size="small" class="layer-checkbox-group">
        <el-checkbox label="device">
          <Icon icon="ep:monitor" class="mr-1" />
          设备
        </el-checkbox>
        <el-checkbox label="area">
          <Icon icon="ep:location" class="mr-1" />
          区域
        </el-checkbox>
        <el-checkbox label="path">
          <Icon icon="ep:connection" class="mr-1" />
          路径
        </el-checkbox>
      </el-checkbox-group>
    </div>

    <!-- 操作按钮 -->
    <div class="control-section">
      <div class="section-label">
        <Icon icon="ep:setting" />
        <span class="ml-1">操作</span>
      </div>
      <div class="action-buttons">
        <el-button @click="handleRefresh" size="small" :icon="Refresh" circle title="刷新" />
        <el-button @click="handleFullscreen" size="small" :icon="FullScreen" circle title="全屏" />
        <el-button
          v-if="enableEdit"
          @click="handleAdd"
          type="primary"
          size="small"
          :icon="Plus"
          circle
          title="添加"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { Refresh, FullScreen, Plus } from '@element-plus/icons-vue'
import * as BuildingApi from '@/api/iot/spatial/building'
import * as FloorApi from '@/api/iot/spatial/floor'

interface Props {
  selectedFloorId?: number
  enableEdit?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  enableEdit: false
})

const emit = defineEmits<{
  buildingChange: [buildingId: number | undefined]
  floorChange: [floorId: number | undefined]
  layerChange: [layers: string[]]
  refresh: []
  fullscreen: []
  add: []
}>()

// 状态
const selectedBuildingId = ref<number | undefined>()
const selectedFloorId = ref<number | undefined>(props.selectedFloorId)
const buildingList = ref<any[]>([])
const floors = ref<any[]>([])
const visibleLayers = ref<string[]>(['device', 'area'])

// 计算属性
const canGoUp = computed(() => {
  if (!selectedFloorId.value || floors.value.length === 0) return false
  const currentIndex = floors.value.findIndex(f => f.id === selectedFloorId.value)
  return currentIndex > 0
})

const canGoDown = computed(() => {
  if (!selectedFloorId.value || floors.value.length === 0) return false
  const currentIndex = floors.value.findIndex(f => f.id === selectedFloorId.value)
  return currentIndex >= 0 && currentIndex < floors.value.length - 1
})

// 方法
const loadBuildingList = async () => {
  try {
    buildingList.value = await BuildingApi.getBuildingList()
    if (buildingList.value.length > 0 && !selectedBuildingId.value) {
      selectedBuildingId.value = buildingList.value[0].id
      await loadFloors(selectedBuildingId.value)
    }
  } catch (error) {
    console.error('[空间控制面板] 加载建筑列表失败:', error)
  }
}

const loadFloors = async (buildingId: number) => {
  try {
    floors.value = await FloorApi.getFloorListByBuildingId(buildingId)
    // 如果当前选中的楼层不在新建筑中，自动选择第一个楼层
    if (selectedFloorId.value && !floors.value.find(f => f.id === selectedFloorId.value)) {
      if (floors.value.length > 0) {
        selectedFloorId.value = floors.value[0].id
        emit('floorChange', selectedFloorId.value)
      } else {
        selectedFloorId.value = undefined
        emit('floorChange', undefined)
      }
    }
  } catch (error) {
    console.error('[空间控制面板] 加载楼层列表失败:', error)
  }
}

const handleBuildingChange = async (buildingId: number | undefined) => {
  if (!buildingId) {
    floors.value = []
    selectedFloorId.value = undefined
    emit('buildingChange', undefined)
    emit('floorChange', undefined)
    return
  }
  await loadFloors(buildingId)
  emit('buildingChange', buildingId)
}

const handleFloorChange = (floorId: number | undefined) => {
  selectedFloorId.value = floorId
  emit('floorChange', floorId)
}

const goUpFloor = () => {
  if (!selectedFloorId.value || floors.value.length === 0) return
  const currentIndex = floors.value.findIndex(f => f.id === selectedFloorId.value)
  if (currentIndex > 0) {
    const prevFloor = floors.value[currentIndex - 1]
    handleFloorChange(prevFloor.id)
  }
}

const goDownFloor = () => {
  if (!selectedFloorId.value || floors.value.length === 0) return
  const currentIndex = floors.value.findIndex(f => f.id === selectedFloorId.value)
  if (currentIndex >= 0 && currentIndex < floors.value.length - 1) {
    const nextFloor = floors.value[currentIndex + 1]
    handleFloorChange(nextFloor.id)
  }
}

const handleRefresh = () => {
  emit('refresh')
  loadBuildingList()
}

const handleFullscreen = () => {
  emit('fullscreen')
}

const handleAdd = () => {
  emit('add')
}

// 监听图层变化
watch(visibleLayers, (newLayers) => {
  emit('layerChange', newLayers)
}, { deep: true })

// 监听外部传入的 selectedFloorId
watch(() => props.selectedFloorId, (newFloorId) => {
  if (newFloorId !== selectedFloorId.value) {
    selectedFloorId.value = newFloorId
  }
})

// 生命周期
onMounted(() => {
  loadBuildingList()
})
</script>

<style scoped lang="scss">
.spatial-control-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 16px;
  background: #1a1a1a;
  border-radius: 8px;

  .control-section {
    .section-label {
      display: flex;
      align-items: center;
      color: #00d4ff;
      font-size: 14px;
      font-weight: 500;
      margin-bottom: 12px;
    }

    .control-select {
      width: 100%;
    }

    .floor-controls {
      display: flex;
      align-items: center;
      gap: 8px;

      .floor-select {
        flex: 1;
      }

      .floor-nav-btn {
        flex-shrink: 0;
      }
    }

    .layer-checkbox-group {
      display: flex;
      flex-direction: column;
      gap: 8px;

      :deep(.el-checkbox) {
        margin: 0;
        color: #fff;

        .el-checkbox__label {
          color: #fff;
        }
      }
    }

    .action-buttons {
      display: flex;
      gap: 8px;
      justify-content: center;
    }
  }
}
</style>

