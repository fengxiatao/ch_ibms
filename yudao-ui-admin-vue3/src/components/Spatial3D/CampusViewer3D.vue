<template>
  <div class="campus-viewer-3d">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button-group>
        <el-button :type="viewMode === '2d' ? 'primary' : ''" @click="switchViewMode('2d')">
          <el-icon><Location /></el-icon>
          2D平面图
        </el-button>
        <el-button :type="viewMode === '3d' ? 'primary' : ''" @click="switchViewMode('3d')">
          <el-icon><Box /></el-icon>
          3D建筑群
        </el-button>
      </el-button-group>

      <el-button-group class="ml-4">
        <el-button @click="zoomIn">
          <el-icon><ZoomIn /></el-icon>
        </el-button>
        <el-button @click="zoomOut">
          <el-icon><ZoomOut /></el-icon>
        </el-button>
        <el-button @click="resetView">
          <el-icon><Refresh /></el-icon>
          重置视图
        </el-button>
      </el-button-group>

      <el-button type="success" class="ml-4" @click="showAddBuildingDialog = true">
        <el-icon><Plus /></el-icon>
        添加建筑
      </el-button>

      <el-button @click="$emit('back')">
        <el-icon><Back /></el-icon>
        返回
      </el-button>

      <div class="campus-info">
        <span class="campus-name">{{ campusName }}</span>
        <el-tag>建筑数: {{ buildings.length }}</el-tag>
      </div>
    </div>

    <!-- 2D 平面视图 -->
    <div v-show="viewMode === '2d'" class="view-container">
      <v-stage :config="stageConfig" @wheel="handleWheel" @mousedown="handleMouseDown">
        <v-layer>
          <!-- 园区边界 -->
          <v-rect
            v-if="campusData.geom"
            :config="campusBoundaryConfig"
          />

          <!-- 建筑物 2D 矩形 -->
          <v-group
            v-for="building in buildings"
            :key="`building-${building.id}`"
            @click="handleBuildingClick(building)"
            @mouseover="handleBuildingHover(building)"
            @mouseout="handleBuildingMouseOut"
          >
            <v-rect :config="getBuildingRectConfig(building)" />
            <v-text :config="getBuildingLabelConfig(building)" />
          </v-group>

          <!-- 高亮选中的建筑 -->
          <v-rect
            v-if="selectedBuilding"
            :config="selectedBuildingHighlightConfig"
          />
        </v-layer>
      </v-stage>

      <!-- 悬浮信息卡片 -->
      <div
        v-if="hoveredBuilding"
        class="hover-card"
        :style="{ left: hoverCardPosition.x + 'px', top: hoverCardPosition.y + 'px' }"
      >
        <h4>{{ hoveredBuilding.buildingName }}</h4>
        <p>楼层数: {{ hoveredBuilding.aboveGroundFloors }}</p>
        <p>建筑面积: {{ hoveredBuilding.buildingArea }} m²</p>
        <p>运营状态: {{ hoveredBuilding.operationStatus }}</p>
      </div>
    </div>

    <!-- 3D Cesium 视图 -->
    <div v-show="viewMode === '3d'" ref="cesiumContainer" class="view-container cesium-container">
      <!-- Cesium 将在这里初始化 -->
    </div>

    <!-- 添加建筑对话框（优化版 - 使用预设模型）-->
    <el-dialog
      v-model="showAddBuildingDialog"
      title="添加建筑"
      width="800px"
      @close="resetBuildingForm"
    >
      <el-form :model="newBuilding" label-width="120px">
        <!-- 步骤1: 选择建筑模板 -->
        <el-divider content-position="left">
          <el-icon><Box /></el-icon>
          选择建筑模板
        </el-divider>
        
        <el-form-item label="建筑模板">
          <el-radio-group v-model="selectedBuildingTemplate" @change="applyBuildingTemplate">
            <el-space wrap :size="16">
              <el-card
                v-for="template in buildingTemplates"
                :key="template.id"
                :class="{ 'template-card': true, 'selected': selectedBuildingTemplate === template.id }"
                shadow="hover"
                @click="selectedBuildingTemplate = template.id; applyBuildingTemplate(template.id)"
              >
                <div class="template-content">
                  <div class="template-icon" :style="{ background: template.color }">
                    {{ template.emoji }}
                  </div>
                  <div class="template-info">
                    <div class="template-name">{{ template.name }}</div>
                    <div class="template-desc">{{ template.floors }}层 | {{ template.area }}m²</div>
                  </div>
                </div>
              </el-card>
            </el-space>
          </el-radio-group>
        </el-form-item>

        <!-- 步骤2: 自定义参数 -->
        <el-divider content-position="left">
          <el-icon><Edit /></el-icon>
          自定义参数（可选）
        </el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="建筑名称" required>
              <el-input v-model="newBuilding.buildingName" placeholder="请输入建筑名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="建筑别名">
              <el-input v-model="newBuilding.alias" placeholder="如：A栋" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="楼层数">
              <el-input-number v-model="newBuilding.aboveGroundFloors" :min="1" :max="200" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="建筑面积(m²)">
              <el-input-number v-model="newBuilding.buildingArea" :min="0" :step="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="建筑高度(m)">
              <el-input-number v-model="newBuilding.buildingHeight" :min="0" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 步骤3: 点击地图选择位置 -->
        <el-divider content-position="left">
          <el-icon><Location /></el-icon>
          选择位置
        </el-divider>

        <el-alert type="info" :closable="false" style="margin-bottom: 16px">
          <template #title>
            <el-icon><InfoFilled /></el-icon>
            点击下方的"在地图上选择位置"按钮，然后点击地图任意位置来放置建筑
          </template>
        </el-alert>

        <el-form-item>
          <el-button 
            :type="isSelectingLocation ? 'danger' : 'success'" 
            @click="toggleLocationSelection"
            style="width: 100%"
          >
            <el-icon><Location /></el-icon>
            {{ isSelectingLocation ? '取消选择位置（当前坐标: ' + newBuilding.x + ', ' + newBuilding.y + ')' : '在地图上选择位置' }}
          </el-button>
        </el-form-item>

        <el-row :gutter="20" v-if="!isSelectingLocation">
          <el-col :span="12">
            <el-form-item label="X坐标(米)">
              <el-input-number v-model="newBuilding.x" :step="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Y坐标(米)">
              <el-input-number v-model="newBuilding.y" :step="10" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 3D预览 -->
        <el-divider content-position="left">
          <el-icon><View /></el-icon>
          3D预览
        </el-divider>

        <div class="building-preview">
          <div class="preview-building" :style="getBuildingPreviewStyle()">
            <div class="building-label">{{ newBuilding.buildingName || '新建筑' }}</div>
          </div>
          <div class="preview-info">
            <el-tag>{{ newBuilding.aboveGroundFloors }}层</el-tag>
            <el-tag type="success">{{ newBuilding.buildingHeight }}m</el-tag>
            <el-tag type="warning">{{ newBuilding.buildingArea }}m²</el-tag>
          </div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showAddBuildingDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddBuilding" :loading="addingBuilding">
          <el-icon><Plus /></el-icon>
          确定添加
        </el-button>
      </template>
    </el-dialog>

    <!-- 建筑详情抽屉 -->
    <el-drawer
      v-model="showBuildingDetail"
      :title="selectedBuilding?.buildingName || '建筑详情'"
      size="400px"
    >
      <div v-if="selectedBuilding" class="building-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="建筑名称">
            {{ selectedBuilding.buildingName }}
          </el-descriptions-item>
          <el-descriptions-item label="别名">
            {{ selectedBuilding.alias || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="建筑类型">
            {{ selectedBuilding.buildingType }}
          </el-descriptions-item>
          <el-descriptions-item label="地上楼层">
            {{ selectedBuilding.aboveGroundFloors }}
          </el-descriptions-item>
          <el-descriptions-item label="建筑面积">
            {{ selectedBuilding.buildingArea }} m²
          </el-descriptions-item>
          <el-descriptions-item label="建筑高度">
            {{ selectedBuilding.buildingHeight }} m
          </el-descriptions-item>
          <el-descriptions-item label="运营状态">
            <el-tag :type="selectedBuilding.operationStatus === 'OPERATING' ? 'success' : 'info'">
              {{ selectedBuilding.operationStatus }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <div class="actions">
          <el-button type="primary" @click="viewBuildingFloors">
            <el-icon><Guide /></el-icon>
            查看楼层
          </el-button>
          <el-button type="warning" @click="editBuilding">
            <el-icon><Edit /></el-icon>
            编辑建筑
          </el-button>
          <el-button type="danger" @click="deleteBuilding">
            <el-icon><Delete /></el-icon>
            删除建筑
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
  Location, 
  Box, 
  ZoomIn, 
  ZoomOut, 
  Refresh, 
  Plus, 
  Back, 
  Guide, 
  Edit, 
  Delete 
} from '@element-plus/icons-vue'
// Cesium 3D引擎（暂时注释，需要配置Token）
// import * as Cesium from 'cesium'
// import 'cesium/Build/Cesium/Widgets/widgets.css'

// Props
interface Props {
  campusId: number
  campusName: string
}
const props = withDefaults(defineProps<Props>(), {
  campusId: 0,
  campusName: ''
})

// Emits
const emit = defineEmits(['back', 'view-building'])

// State
const viewMode = ref<'2d' | '3d'>('2d')
const scale = ref(1.0)
const offset = reactive({ x: 0, y: 0 })
const isDragging = ref(false)
const lastPointerPosition = reactive({ x: 0, y: 0 })

const campusData = ref<any>({})
const buildings = ref<any[]>([])
const selectedBuilding = ref<any>(null)
const hoveredBuilding = ref<any>(null)
const hoverCardPosition = reactive({ x: 0, y: 0 })

const showAddBuildingDialog = ref(false)
const showBuildingDetail = ref(false)
const addingBuilding = ref(false)
const isSelectingLocation = ref(false)
const selectedBuildingTemplate = ref('office-medium')

// 建筑模板预设
const buildingTemplates = [
  {
    id: 'office-small',
    name: '小型办公楼',
    emoji: '🏢',
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    floors: 5,
    area: 2000,
    height: 20,
    type: 'OFFICE'
  },
  {
    id: 'office-medium',
    name: '中型办公楼',
    emoji: '🏛️',
    color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    floors: 10,
    area: 5000,
    height: 40,
    type: 'OFFICE'
  },
  {
    id: 'office-large',
    name: '大型办公楼',
    emoji: '🏗️',
    color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    floors: 20,
    area: 12000,
    height: 80,
    type: 'OFFICE'
  },
  {
    id: 'residential',
    name: '住宅楼',
    emoji: '🏠',
    color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    floors: 15,
    area: 8000,
    height: 60,
    type: 'RESIDENTIAL'
  },
  {
    id: 'commercial',
    name: '商业综合体',
    emoji: '🏬',
    color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    floors: 8,
    area: 15000,
    height: 35,
    type: 'COMMERCIAL'
  },
  {
    id: 'industrial',
    name: '工业厂房',
    emoji: '🏭',
    color: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
    floors: 3,
    area: 10000,
    height: 15,
    type: 'INDUSTRIAL'
  }
]

const newBuilding = reactive({
  buildingName: '',
  alias: '',
  campusId: props.campusId,
  aboveGroundFloors: 10,
  buildingArea: 5000,
  buildingHeight: 40,
  buildingType: 'OFFICE',
  x: 0,
  y: 0
})

// Cesium（暂时注释）
const cesiumContainer = ref<HTMLDivElement | null>(null)
// let cesiumViewer: Cesium.Viewer | null = null

// Konva Stage Config
const stageConfig = computed(() => ({
  width: 1200,
  height: 700,
  draggable: false
}))

const campusBoundaryConfig = computed(() => ({
  x: offset.x * scale.value,
  y: offset.y * scale.value,
  width: 1000 * scale.value,
  height: 600 * scale.value,
  stroke: '#409eff',
  strokeWidth: 3,
  dash: [10, 5],
  fill: 'rgba(64, 158, 255, 0.05)'
}))

const getBuildingRectConfig = (building: any) => {
  // 世界坐标
  const worldX = building.x || 0
  const worldY = building.y || 0
  
  // 转换到Canvas坐标：(世界坐标 + offset) * scale
  const canvasX = (worldX + offset.x) * scale.value
  const canvasY = (worldY + offset.y) * scale.value
  
  // 建筑尺寸（基于面积）
  const width = Math.sqrt(building.buildingArea || 1000) * 0.5
  const height = width * 1.2

  return {
    x: canvasX,
    y: canvasY,
    width: width * scale.value,
    height: height * scale.value,
    fill: building.operationStatus === 'OPERATING' ? '#67c23a' : '#909399',
    stroke: '#303133',
    strokeWidth: 2,
    shadowColor: 'black',
    shadowBlur: 10,
    shadowOpacity: 0.3,
    opacity: 0.8,
    cornerRadius: 5
  }
}

const getBuildingLabelConfig = (building: any) => {
  // 世界坐标
  const worldX = building.x || 0
  const worldY = building.y || 0
  
  // 转换到Canvas坐标
  const canvasX = (worldX + offset.x) * scale.value
  const canvasY = (worldY + offset.y - 20) * scale.value // 标签在建筑上方20米处
  
  const width = Math.sqrt(building.buildingArea || 1000) * 0.5

  return {
    x: canvasX,
    y: canvasY,
    text: building.buildingName,
    fontSize: 14 * scale.value,
    fill: '#303133',
    fontStyle: 'bold',
    align: 'center',
    width: width * scale.value
  }
}

const selectedBuildingHighlightConfig = computed(() => {
  if (!selectedBuilding.value) return {}
  
  // 世界坐标
  const worldX = selectedBuilding.value.x || 0
  const worldY = selectedBuilding.value.y || 0
  
  // 转换到Canvas坐标
  const canvasX = (worldX + offset.x) * scale.value
  const canvasY = (worldY + offset.y) * scale.value
  
  const width = Math.sqrt(selectedBuilding.value.buildingArea || 1000) * 0.5
  const height = width * 1.2

  return {
    x: canvasX - 5,
    y: canvasY - 5,
    width: (width + 10 / scale.value) * scale.value,
    height: (height + 10 / scale.value) * scale.value,
    stroke: '#409eff',
    strokeWidth: 4,
    dash: [10, 5],
    cornerRadius: 5
  }
})

// Methods
const switchViewMode = (mode: '2d' | '3d') => {
  viewMode.value = mode
  if (mode === '3d') {
    initCesiumViewer()
  }
}

const zoomIn = () => {
  scale.value = Math.min(scale.value * 1.2, 5.0)
}

const zoomOut = () => {
  scale.value = Math.max(scale.value / 1.2, 0.2)
}

const resetView = () => {
  scale.value = 1.0
  offset.x = 0
  offset.y = 0
  // if (cesiumViewer) {
  //   cesiumViewer.camera.flyHome(0)
  // }
  
  // 重新居中显示所有建筑
  setTimeout(() => {
    if (buildings.value.length > 0) {
      centerViewToBuildings()
    }
  }, 100)
}

const handleWheel = (e: any) => {
  e.evt.preventDefault()
  const scaleBy = 1.05
  const oldScale = scale.value
  const pointer = e.target.getStage().getPointerPosition()
  const mousePointTo = {
    x: (pointer.x - offset.x * oldScale) / oldScale,
    y: (pointer.y - offset.y * oldScale) / oldScale
  }

  scale.value = e.evt.deltaY < 0 ? oldScale * scaleBy : oldScale / scaleBy
  scale.value = Math.max(0.2, Math.min(5.0, scale.value))

  const newScale = scale.value
  offset.x = (pointer.x - mousePointTo.x * newScale) / newScale
  offset.y = (pointer.y - mousePointTo.y * newScale) / newScale
}

const handleMouseDown = (e: any) => {
  // 如果正在选择位置，则记录点击的坐标
  if (isSelectingLocation.value) {
    const stage = e.target.getStage()
    const pointerPosition = stage.getPointerPosition()
    
    // 转换canvas坐标到世界坐标
    newBuilding.x = Math.round((pointerPosition.x / scale.value - offset.x))
    newBuilding.y = Math.round((pointerPosition.y / scale.value - offset.y))
    
    ElMessage.success(`已选择位置: (${newBuilding.x}, ${newBuilding.y})`)
    isSelectingLocation.value = false
    return
  }
  
  // 正常的拖拽逻辑
  if (e.target === e.target.getStage()) {
    isDragging.value = true
    lastPointerPosition.x = e.evt.clientX
    lastPointerPosition.y = e.evt.clientY
  }
}

const handleBuildingClick = (building: any) => {
  selectedBuilding.value = building
  showBuildingDetail.value = true
}

const handleBuildingHover = (building: any) => {
  hoveredBuilding.value = building
  // 获取鼠标位置
  hoverCardPosition.x = building.x * scale.value + 100
  hoverCardPosition.y = building.y * scale.value
}

const handleBuildingMouseOut = () => {
  hoveredBuilding.value = null
}

const fetchCampusData = async () => {
  try {
    // TODO: 调用API获取园区数据
    campusData.value = {
      id: props.campusId,
      campusName: props.campusName,
      geom: 'POLYGON((0 0, 1000 0, 1000 600, 0 600, 0 0))'
    }
  } catch (error) {
    ElMessage.error('获取园区数据失败')
  }
}

const fetchBuildings = async () => {
  try {
    // TODO: 调用API获取建筑列表
    // const res = await getBuildingListByCampusId(props.campusId)
    // buildings.value = res.data

    // Mock数据
    buildings.value = [
      {
        id: 1,
        buildingName: 'A栋办公楼',
        alias: 'Building A',
        campusId: props.campusId,
        buildingType: 'OFFICE',
        aboveGroundFloors: 20,
        buildingArea: 15000,
        buildingHeight: 80,
        operationStatus: 'OPERATING',
        x: 100,
        y: 100
      },
      {
        id: 2,
        buildingName: 'B栋办公楼',
        alias: 'Building B',
        campusId: props.campusId,
        buildingType: 'OFFICE',
        aboveGroundFloors: 15,
        buildingArea: 12000,
        buildingHeight: 60,
        operationStatus: 'OPERATING',
        x: 350,
        y: 100
      },
      {
        id: 3,
        buildingName: 'C栋商业楼',
        alias: 'Building C',
        campusId: props.campusId,
        buildingType: 'COMMERCIAL',
        aboveGroundFloors: 5,
        buildingArea: 8000,
        buildingHeight: 25,
        operationStatus: 'OPERATING',
        x: 100,
        y: 350
      }
    ]
    
    console.log('🏢 建筑列表加载完成:', buildings.value.length, '个')
    console.log('📋 建筑详情:', buildings.value)
  } catch (error) {
    console.error('❌ 获取建筑列表失败:', error)
    ElMessage.error('获取建筑列表失败')
  }
}

/** 应用建筑模板 */
const applyBuildingTemplate = (templateId: string) => {
  const template = buildingTemplates.find(t => t.id === templateId)
  if (template) {
    newBuilding.aboveGroundFloors = template.floors
    newBuilding.buildingArea = template.area
    newBuilding.buildingHeight = template.height
    newBuilding.buildingType = template.type
    
    // 自动生成建筑名称
    if (!newBuilding.buildingName) {
      const count = buildings.value.length + 1
      newBuilding.buildingName = `${template.name}${count}`
    }
    
    ElMessage.success(`已应用 ${template.name} 模板`)
  }
}

/** 切换位置选择模式 */
const toggleLocationSelection = () => {
  isSelectingLocation.value = !isSelectingLocation.value
  if (isSelectingLocation.value) {
    ElMessage.info('请点击地图上的任意位置来放置建筑')
  }
}

/** 获取建筑预览样式 */
const getBuildingPreviewStyle = () => {
  const width = Math.sqrt(newBuilding.buildingArea) * 0.3
  const height = newBuilding.buildingHeight * 2
  
  return {
    width: `${width}px`,
    height: `${height}px`,
    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    boxShadow: '0 10px 30px rgba(0,0,0,0.3)'
  }
}

const handleAddBuilding = async () => {
  try {
    addingBuilding.value = true
    
    // 验证必填字段
    if (!newBuilding.buildingName) {
      ElMessage.warning('请输入建筑名称')
      return
    }
    
    // TODO: 调用API创建建筑
    // await createBuilding(newBuilding)
    
    // Mock: 添加到本地列表
    const newBuildingData = {
      id: buildings.value.length + 1,
      ...newBuilding,
      operationStatus: 'OPERATING'
    }
    buildings.value.push(newBuildingData)
    
    ElMessage.success('建筑添加成功！已在地图上显示')
    showAddBuildingDialog.value = false
    isSelectingLocation.value = false
    
    // 重新渲染地图（居中显示新建筑）
    setTimeout(() => {
      // 计算所有建筑的中心点
      centerViewToBuildings()
    }, 100)
    
  } catch (error) {
    ElMessage.error('建筑添加失败')
  } finally {
    addingBuilding.value = false
  }
}

/** 居中显示所有建筑 */
const centerViewToBuildings = () => {
  if (buildings.value.length === 0) {
    console.log('⚠️ 没有建筑，无法居中')
    return
  }
  
  console.log('🎯 开始居中显示建筑:', buildings.value.length, '个')
  
  // 计算所有建筑的边界
  let minX = Infinity, minY = Infinity
  let maxX = -Infinity, maxY = -Infinity
  
  buildings.value.forEach(b => {
    const x = b.x || 0
    const y = b.y || 0
    minX = Math.min(minX, x)
    minY = Math.min(minY, y)
    maxX = Math.max(maxX, x)
    maxY = Math.max(maxY, y)
  })
  
  // 计算中心点
  const centerX = (minX + maxX) / 2
  const centerY = (minY + maxY) / 2
  
  console.log('📊 建筑边界:', { minX, minY, maxX, maxY })
  console.log('📍 中心点:', { centerX, centerY })
  
  // Canvas尺寸（假设1200x700）
  const canvasWidth = 1200
  const canvasHeight = 700
  
  // 计算需要的缩放比例（使所有建筑都在视野内）
  const rangeX = maxX - minX + 200 // 加200米边距
  const rangeY = maxY - minY + 200
  const scaleX = canvasWidth / rangeX
  const scaleY = canvasHeight / rangeY
  const targetScale = Math.min(scaleX, scaleY, 2) // 最大放大2倍
  
  console.log('🔍 目标缩放:', targetScale)
  
  // 更新缩放
  scale.value = targetScale
  
  // 更新offset使中心点位于canvas中心
  offset.x = canvasWidth / 2 / scale.value - centerX
  offset.y = canvasHeight / 2 / scale.value - centerY
  
  console.log('📐 新offset:', { x: offset.x, y: offset.y })
  console.log('✅ 居中完成')
  
  ElMessage.success(`已居中显示${buildings.value.length}个建筑`)
}

const resetBuildingForm = () => {
  Object.assign(newBuilding, {
    buildingName: '',
    alias: '',
    campusId: props.campusId,
    aboveGroundFloors: 10,
    buildingArea: 5000,
    buildingHeight: 40,
    buildingType: 'OFFICE',
    x: 0,
    y: 0
  })
  selectedBuildingTemplate.value = 'office-medium'
  isSelectingLocation.value = false
}

const viewBuildingFloors = () => {
  if (selectedBuilding.value) {
    emit('view-building', selectedBuilding.value)
  }
}

const editBuilding = () => {
  // TODO: 实现编辑功能
  ElMessage.info('编辑功能开发中')
}

const deleteBuilding = async () => {
  try {
    await ElMessageBox.confirm('确定要删除该建筑吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // TODO: 调用API删除建筑
    // await deleteBuilding(selectedBuilding.value.id)
    ElMessage.success('建筑删除成功')
    showBuildingDetail.value = false
    fetchBuildings()
  } catch (error) {
    // 用户取消删除
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

  const viewer = new Cesium.Viewer(cesiumContainer.value, {
    terrainProvider: new Cesium.EllipsoidTerrainProvider(),
    animation: false,
    timeline: false,
    baseLayerPicker: false,
    geocoder: false,
    homeButton: true,
    sceneModePicker: true,
    navigationHelpButton: false,
    fullscreenButton: false,
    imageryProvider: osmProvider
  })

  // 添加建筑物3D模型
  buildings.value.forEach((building) => {
    const position = Cesium.Cartesian3.fromDegrees(
      113.3625 + (building.x || 0) / 10000,
      23.1325 + (building.y || 0) / 10000,
      0
    )

    viewer.entities.add({
      name: building.buildingName,
      position: position,
      box: {
        dimensions: new Cesium.Cartesian3(
          Math.sqrt(building.buildingArea) * 0.5,
          Math.sqrt(building.buildingArea) * 0.6,
          building.buildingHeight
        ),
        material: Cesium.Color.fromCssColorString(
          building.operationStatus === 'OPERATING' ? '#67c23a' : '#909399'
        ).withAlpha(0.8),
        outline: true,
        outlineColor: Cesium.Color.BLACK
      },
      description: `
        <h3>${building.buildingName}</h3>
        <p>建筑类型: ${building.buildingType}</p>
        <p>楼层数: ${building.aboveGroundFloors}</p>
        <p>建筑面积: ${building.buildingArea} m²</p>
        <p>建筑高度: ${building.buildingHeight} m</p>
      `
    })
  })

  // 飞到园区位置
  viewer.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(113.3625, 23.1325, 1500),
    orientation: {
      heading: Cesium.Math.toRadians(0),
      pitch: Cesium.Math.toRadians(-45),
      roll: 0.0
    }
  })
  
  ElMessage.success('3D场景加载完成')
}

// Lifecycle
onMounted(async () => {
  await fetchCampusData()
  await fetchBuildings()
  
  // 初始化后自动居中显示所有建筑
  setTimeout(() => {
    if (buildings.value.length > 0) {
      centerViewToBuildings()
    }
  }, 500)
})

onUnmounted(() => {
  // if (cesiumViewer) {
  //   cesiumViewer.destroy()
  //   cesiumViewer = null
  // }
})
</script>

<style scoped lang="scss">
.campus-viewer-3d {
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

    .campus-info {
      margin-left: auto;
      display: flex;
      align-items: center;
      gap: 12px;

      .campus-name {
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
    position: relative;

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
    pointer-events: none;
    min-width: 200px;

    h4 {
      margin: 0 0 8px 0;
      color: #303133;
      font-size: 16px;
    }

    p {
      margin: 4px 0;
      color: #606266;
      font-size: 14px;
    }
  }

  .building-detail {
    .actions {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-top: 16px;

      .el-button {
        width: 100%;
      }
    }
  }
}

// 建筑模板卡片样式
:deep(.template-card) {
  width: 160px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;

  &.selected {
    border-color: #409eff;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
  }

  .template-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;

    .template-icon {
      width: 64px;
      height: 64px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 32px;
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    }

    .template-info {
      text-align: center;

      .template-name {
        font-size: 14px;
        font-weight: bold;
        color: #303133;
        margin-bottom: 4px;
      }

      .template-desc {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

// 建筑预览样式
.building-preview {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="50" cy="50" r="40" fill="none" stroke="white" stroke-width="0.5" opacity="0.1"/></svg>') repeat;
    opacity: 0.2;
  }

  .preview-building {
    position: relative;
    border-radius: 8px;
    display: flex;
    align-items: flex-end;
    justify-content: center;
    transition: all 0.3s;
    animation: float 3s ease-in-out infinite;

    .building-label {
      position: absolute;
      bottom: -30px;
      background: white;
      padding: 4px 12px;
      border-radius: 4px;
      font-size: 12px;
      font-weight: bold;
      color: #303133;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      white-space: nowrap;
    }
  }

  .preview-info {
    display: flex;
    gap: 8px;
    position: relative;
    z-index: 1;
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}
</style>

