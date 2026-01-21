<template>
  <div class="building-viewer-3d">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button-group>
        <el-button :type="viewMode === '2d' ? 'primary' : ''" @click="switchViewMode('2d')">
          <el-icon><Document /></el-icon>
          2D剖面图
        </el-button>
        <el-button :type="viewMode === '3d' ? 'primary' : ''" @click="switchViewMode('3d')">
          <el-icon><OfficeBuilding /></el-icon>
          3D建筑模型
        </el-button>
      </el-button-group>

      <el-button type="success" class="ml-4" @click="showAddFloorDialog = true">
        <el-icon><Plus /></el-icon>
        添加楼层
      </el-button>

      <el-button @click="$emit('back')">
        <el-icon><Back /></el-icon>
        返回园区
      </el-button>

      <div class="building-info">
        <span class="building-name">{{ buildingName }}</span>
        <el-tag>楼层数: {{ floors.length }}</el-tag>
      </div>
    </div>

    <!-- 2D 剖面视图（显示所有楼层的垂直剖面）-->
    <div v-show="viewMode === '2d'" class="view-container">
      <v-stage :config="stageConfig" @wheel="handleWheel">
        <v-layer>
          <!-- 建筑外框 -->
          <v-rect :config="buildingOutlineConfig" />

          <!-- 楼层 -->
          <v-group
            v-for="(floor, index) in sortedFloors"
            :key="`floor-${floor.id}`"
            @click="handleFloorClick(floor)"
            @mouseover="handleFloorHover(floor)"
            @mouseout="hoveredFloor = null"
          >
            <!-- 楼层矩形 -->
            <v-rect :config="getFloorRectConfig(floor, index)" />
            <!-- 楼层标签 -->
            <v-text :config="getFloorLabelConfig(floor, index)" />
            <!-- 楼层信息 -->
            <v-text :config="getFloorInfoConfig(floor, index)" />
          </v-group>

          <!-- 选中高亮 -->
          <v-rect
            v-if="selectedFloor"
            :config="selectedFloorHighlightConfig"
          />
        </v-layer>
      </v-stage>

      <!-- 悬浮信息 -->
      <div
        v-if="hoveredFloor"
        class="hover-card"
        :style="{ left: hoverCardPosition.x + 'px', top: hoverCardPosition.y + 'px' }"
      >
        <h4>{{ hoveredFloor.floorNumber }}F - {{ hoveredFloor.floorName }}</h4>
        <p>楼层类型: {{ hoveredFloor.floorType }}</p>
        <p>楼层面积: {{ hoveredFloor.floorArea }} m²</p>
        <p>主要功能: {{ hoveredFloor.primaryFunction }}</p>
      </div>
    </div>

    <!-- 3D BIM 视图 -->
    <div v-show="viewMode === '3d'" ref="cesiumContainer" class="view-container cesium-container">
      <!-- Cesium 3D 建筑模型 -->
    </div>

    <!-- 添加楼层对话框 -->
    <el-dialog
      v-model="showAddFloorDialog"
      title="添加楼层"
      width="500px"
      @close="resetFloorForm"
    >
      <el-form :model="newFloor" label-width="120px">
        <el-form-item label="楼层编号" required>
          <el-input-number v-model="newFloor.floorNumber" :min="-5" :max="200" />
        </el-form-item>
        <el-form-item label="楼层名称" required>
          <el-input v-model="newFloor.floorName" placeholder="请输入楼层名称" />
        </el-form-item>
        <el-form-item label="楼层类型">
          <el-select v-model="newFloor.floorType" placeholder="请选择楼层类型">
            <el-option label="标准层" value="STANDARD" />
            <el-option label="设备层" value="EQUIPMENT" />
            <el-option label="地下层" value="UNDERGROUND" />
            <el-option label="屋顶层" value="ROOF" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层面积(m²)" required>
          <el-input-number v-model="newFloor.floorArea" :min="0" :step="100" />
        </el-form-item>
        <el-form-item label="层高(m)" required>
          <el-input-number v-model="newFloor.floorHeight" :min="0" :step="0.1" :precision="1" />
        </el-form-item>
        <el-form-item label="主要功能">
          <el-input v-model="newFloor.primaryFunction" placeholder="如：办公、商业等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddFloorDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddFloor">确定</el-button>
      </template>
    </el-dialog>

    <!-- 楼层详情抽屉 -->
    <el-drawer
      v-model="showFloorDetail"
      :title="`${selectedFloor?.floorNumber}F - ${selectedFloor?.floorName}`"
      size="400px"
    >
      <div v-if="selectedFloor" class="floor-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="楼层编号">
            {{ selectedFloor.floorNumber }}F
          </el-descriptions-item>
          <el-descriptions-item label="楼层名称">
            {{ selectedFloor.floorName }}
          </el-descriptions-item>
          <el-descriptions-item label="楼层类型">
            {{ selectedFloor.floorType }}
          </el-descriptions-item>
          <el-descriptions-item label="楼层面积">
            {{ selectedFloor.floorArea }} m²
          </el-descriptions-item>
          <el-descriptions-item label="层高">
            {{ selectedFloor.floorHeight }} m
          </el-descriptions-item>
          <el-descriptions-item label="主要功能">
            {{ selectedFloor.primaryFunction || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <div class="actions">
          <el-button type="primary" @click="viewFloorAreas">
            <el-icon><Guide /></el-icon>
            查看区域和设备
          </el-button>
          <el-button type="warning" @click="editFloor">
            <el-icon><Edit /></el-icon>
            编辑楼层
          </el-button>
          <el-button type="danger" @click="deleteFloor">
            <el-icon><Delete /></el-icon>
            删除楼层
          </el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document,
  OfficeBuilding,
  Plus,
  Back,
  Guide,
  Edit,
  Delete
} from '@element-plus/icons-vue'
// Cesium 3D引擎（暂时注释）
// import * as Cesium from 'cesium'
// import 'cesium/Build/Cesium/Widgets/widgets.css'

// Props
interface Props {
  buildingId: number
  buildingName: string
}
const props = withDefaults(defineProps<Props>(), {
  buildingId: 0,
  buildingName: ''
})

// Emits
const emit = defineEmits(['back', 'view-floor'])

// State
const viewMode = ref<'2d' | '3d'>('2d')
const scale = ref(1.0)

const floors = ref<any[]>([])
const selectedFloor = ref<any>(null)
const hoveredFloor = ref<any>(null)
const hoverCardPosition = reactive({ x: 0, y: 0 })

const showAddFloorDialog = ref(false)
const showFloorDetail = ref(false)
const newFloor = reactive({
  buildingId: props.buildingId,
  floorNumber: 1,
  floorName: '1F',
  floorType: 'STANDARD',
  floorArea: 1000,
  floorHeight: 3.6,
  primaryFunction: '办公'
})

// Cesium（暂时注释）
const cesiumContainer = ref<HTMLDivElement | null>(null)
// let cesiumViewer: Cesium.Viewer | null = null

// Computed
const sortedFloors = computed(() => {
  return [...floors.value].sort((a, b) => a.floorNumber - b.floorNumber)
})

const stageConfig = computed(() => ({
  width: 1200,
  height: 700
}))

const buildingOutlineConfig = computed(() => ({
  x: 50,
  y: 50,
  width: 500,
  height: 600,
  stroke: '#303133',
  strokeWidth: 3,
  fill: 'rgba(0, 0, 0, 0.02)'
}))

const getFloorRectConfig = (floor: any, index: number) => {
  const floorHeight = 40 // 每层的显示高度
  const y = 550 - index * floorHeight // 从下往上排列

  return {
    x: 55,
    y: y,
    width: 490,
    height: floorHeight - 2,
    fill: floor.floorType === 'EQUIPMENT' ? '#e6a23c' : '#67c23a',
    stroke: '#303133',
    strokeWidth: 1,
    opacity: 0.7,
    cornerRadius: 2
  }
}

const getFloorLabelConfig = (floor: any, index: number) => {
  const floorHeight = 40
  const y = 550 - index * floorHeight

  return {
    x: 65,
    y: y + 10,
    text: `${floor.floorNumber}F`,
    fontSize: 18,
    fill: '#303133',
    fontStyle: 'bold'
  }
}

const getFloorInfoConfig = (floor: any, index: number) => {
  const floorHeight = 40
  const y = 550 - index * floorHeight

  return {
    x: 150,
    y: y + 12,
    text: `${floor.floorName} | ${floor.floorArea}m² | ${floor.primaryFunction || ''}`,
    fontSize: 14,
    fill: '#606266'
  }
}

const selectedFloorHighlightConfig = computed(() => {
  if (!selectedFloor.value) return {}
  const index = sortedFloors.value.findIndex((f) => f.id === selectedFloor.value.id)
  const floorHeight = 40
  const y = 550 - index * floorHeight

  return {
    x: 52,
    y: y - 2,
    width: 496,
    height: floorHeight + 2,
    stroke: '#409eff',
    strokeWidth: 4,
    dash: [10, 5]
  }
})

// Methods
const switchViewMode = (mode: '2d' | '3d') => {
  viewMode.value = mode
  if (mode === '3d') {
    initCesiumViewer()
  }
}

const handleWheel = (e: any) => {
  e.evt.preventDefault()
  const scaleBy = 1.05
  scale.value = e.evt.deltaY < 0 ? scale.value * scaleBy : scale.value / scaleBy
  scale.value = Math.max(0.5, Math.min(2.0, scale.value))
}

const handleFloorClick = (floor: any) => {
  selectedFloor.value = floor
  showFloorDetail.value = true
}

const handleFloorHover = (floor: any) => {
  hoveredFloor.value = floor
  hoverCardPosition.x = 600
  hoverCardPosition.y = 100
}

const fetchFloors = async () => {
  try {
    // TODO: 调用API获取楼层列表
    // const res = await getFloorListByBuildingId(props.buildingId)
    // floors.value = res.data

    // Mock数据
    floors.value = Array.from({ length: 15 }, (_, i) => ({
      id: i + 1,
      buildingId: props.buildingId,
      floorNumber: i + 1,
      floorName: `${i + 1}F`,
      floorType: i === 0 ? 'EQUIPMENT' : 'STANDARD',
      floorArea: 1200,
      floorHeight: 3.6,
      primaryFunction: i === 0 ? '设备层' : '办公'
    }))
  } catch (error) {
    ElMessage.error('获取楼层列表失败')
  }
}

const handleAddFloor = async () => {
  try {
    // TODO: 调用API创建楼层
    ElMessage.success('楼层添加成功')
    showAddFloorDialog.value = false
    fetchFloors()
  } catch (error) {
    ElMessage.error('楼层添加失败')
  }
}

const resetFloorForm = () => {
  Object.assign(newFloor, {
    buildingId: props.buildingId,
    floorNumber: 1,
    floorName: '1F',
    floorType: 'STANDARD',
    floorArea: 1000,
    floorHeight: 3.6,
    primaryFunction: '办公'
  })
}

const viewFloorAreas = () => {
  if (selectedFloor.value) {
    emit('view-floor', selectedFloor.value)
  }
}

const editFloor = () => {
  ElMessage.info('编辑功能开发中')
}

const deleteFloor = async () => {
  try {
    await ElMessageBox.confirm('确定要删除该楼层吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('楼层删除成功')
    showFloorDetail.value = false
    fetchFloors()
  } catch (error) {
    // 用户取消
  }
}

const initCesiumViewer = () => {
  // 使用全局Cesium对象（通过CDN加载）
  const Cesium = (window as any).Cesium
  if (!Cesium) {
    ElMessage.warning('Cesium库未加载，3D模式需要配置CDN')
    return
  }
  
  if (!cesiumContainer.value) return

  // 使用OpenStreetMap作为底图，不需要Token
  const osmProvider = new Cesium.OpenStreetMapImageryProvider({
    url: 'https://a.tile.openstreetmap.org/'
  })

  // 创建Cesium Viewer
  const viewer = new Cesium.Viewer(cesiumContainer.value, {
    terrainProvider: new Cesium.EllipsoidTerrainProvider(),
    animation: false,
    timeline: false,
    baseLayerPicker: false,
    imageryProvider: osmProvider
  })

  // 添加建筑3D模型（按楼层叠加）
  sortedFloors.value.forEach((floor, index) => {
    const position = Cesium.Cartesian3.fromDegrees(
      113.3625,
      23.1325,
      index * floor.floorHeight
    )

    viewer.entities.add({
      name: floor.floorName,
      position: position,
      box: {
        dimensions: new Cesium.Cartesian3(40, 60, floor.floorHeight),
        material:
          floor.floorType === 'EQUIPMENT'
            ? Cesium.Color.ORANGE.withAlpha(0.7)
            : Cesium.Color.GREEN.withAlpha(0.7),
        outline: true,
        outlineColor: Cesium.Color.BLACK
      }
    })
  })

  viewer.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(113.3625, 23.1325, 150),
    orientation: {
      heading: Cesium.Math.toRadians(45),
      pitch: Cesium.Math.toRadians(-30),
      roll: 0.0
    }
  })
  
  ElMessage.success('3D场景加载完成')
}

// Lifecycle
onMounted(() => {
  fetchFloors()
})

onUnmounted(() => {
  if (cesiumViewer) {
    cesiumViewer.destroy()
    cesiumViewer = null
  }
})
</script>

<style scoped lang="scss">
.building-viewer-3d {
  width: 100%;
  height: 100vh;
  background: #f5f7fa;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1000;

  .toolbar {
    display: flex;
    align-items: center;
    padding: 16px;
    background: white;
    border-bottom: 1px solid #e4e7ed;
    gap: 16px;

    .building-info {
      margin-left: auto;
      display: flex;
      align-items: center;
      gap: 12px;

      .building-name {
        font-size: 16px;
        font-weight: bold;
        color: #303133;
      }
    }
  }

  .view-container {
    width: 100%;
    height: calc(100% - 73px);
    background: #ecf5ff;

    &.cesium-container {
      background: transparent;
    }
  }

  .hover-card {
    position: absolute;
    background: white;
    padding: 16px;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
    z-index: 1000;
    min-width: 200px;

    h4 {
      margin: 0 0 8px 0;
      color: #303133;
    }

    p {
      margin: 4px 0;
      color: #606266;
      font-size: 14px;
    }
  }

  .floor-detail {
    .actions {
      display: flex;
      flex-direction: column;
      gap: 12px;

      .el-button {
        width: 100%;
      }
    }
  }
}
</style>

