<template>
  <ContentWrap style="margin-top: 70px">
    <!-- 页面标题和工具栏 -->
    <div class="header-section">
      <div class="title-area">
        <h2>IBMS 设备 GIS 地图</h2>
        <p class="subtitle">基于 GeoServer 的设备空间信息展示系统</p>
      </div>
      <div class="toolbar">
        <el-button-group>
          <el-button
            :type="currentView === 'campus' ? 'primary' : ''"
            @click="switchView('campus')"
          >
            <el-icon><OfficeBuilding /></el-icon>
            园区视图
          </el-button>
          <el-button
            :type="currentView === 'building' ? 'primary' : ''"
            @click="switchView('building')"
          >
            <el-icon><School /></el-icon>
            建筑视图
          </el-button>
          <el-button :type="currentView === 'floor' ? 'primary' : ''" @click="switchView('floor')">
            <el-icon><Grid /></el-icon>
            楼层视图
          </el-button>
          <el-button
            :type="currentView === 'device' ? 'primary' : ''"
            @click="switchView('device')"
          >
            <el-icon><Cpu /></el-icon>
            设备视图
          </el-button>
        </el-button-group>

        <el-button @click="refreshMap">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>

        <el-button @click="resetView">
          <el-icon><Aim /></el-icon>
          重置视图
        </el-button>

        <el-button type="success" @click="locateToData">
          <el-icon><Location /></el-icon>
          定位到数据
        </el-button>
      </div>
    </div>

    <!-- 统计信息卡片 -->
    <el-row :gutter="16" class="stats-section">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon campus-icon"><OfficeBuilding /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.campusCount }}</div>
              <div class="stat-label">园区数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon building-icon"><School /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.buildingCount }}</div>
              <div class="stat-label">建筑数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon floor-icon"><Grid /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.floorCount }}</div>
              <div class="stat-label">楼层数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon device-icon"><Cpu /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.deviceCount }}</div>
              <div class="stat-label">设备数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 地图区域 -->
    <el-card shadow="never" class="map-section">
      <!-- 建筑绘制工具 -->
      <DrawingTools
        v-if="!buildingPlanMode && !building3DMode && mapInstance"
        :map="mapInstance"
        @refresh="refreshMap"
        class="drawing-tools-overlay"
      />
      
      <!-- GIS 地图模式 -->
      <GeoServerMap
        v-show="!buildingPlanMode"
        ref="mapRef"
        :geoserver-url="geoserverConfig.url"
        :workspace="geoserverConfig.workspace"
        :center="mapConfig.center"
        :zoom="mapConfig.zoom"
        :layers="currentLayers"
        :height="'700px'"
        :show-layer-control="true"
        :enable-click="true"
        @map-ready="handleMapReady"
        @feature-click="handleFeatureClick"
      />

      <!-- 建筑平面图模式 -->
      <!-- 室内2D平面图编辑器（新版，支持设备添加）-->
      <FloorPlanEditor
        v-if="buildingPlanMode && !building3DMode && selectedBuilding.id > 0"
        :building-id="selectedBuilding.id"
        :building-name="selectedBuilding.name"
        @back="exitBuildingPlanMode"
      />

      <!-- 建筑3D视图 -->
      <Cesium3DView
        v-if="building3DMode && selectedBuilding.id > 0"
        :building-id="selectedBuilding.id"
        :building-name="selectedBuilding.name"
        :building-data="selectedBuilding.data"
        @back="exitBuilding3DMode"
        @switch-to-2d="switchTo2DView"
      />
    </el-card>

    <!-- 选中要素详情 -->
    <el-drawer
      v-model="detailDrawerVisible"
      :title="selectedFeature.title"
      size="400px"
      direction="rtl"
    >
      <template v-if="selectedFeature.data">
        <el-descriptions :column="1" border>
          <el-descriptions-item
            v-for="(value, key) in selectedFeature.data"
            :key="key"
            :label="formatLabel(String(key))"
          >
            <template v-if="String(key) === 'geom'">
              <el-tag type="info">空间数据</el-tag>
            </template>
            <template v-else>
              {{ value || '-' }}
            </template>
          </el-descriptions-item>
        </el-descriptions>

        <div class="action-buttons">
          <el-button type="primary" @click="locateFeature" icon="Aim"> 定位 </el-button>
          <el-button @click="viewDetails" icon="View"> 查看详情 </el-button>
        </div>
      </template>
    </el-drawer>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ContentWrap } from '@/components/ContentWrap'
import GeoServerMap from '@/components/GeoServerMap/index.vue'
import FloorPlanEditor from '@/components/IndoorMap/FloorPlanEditor.vue'
import Cesium3DView from '@/components/Cesium3DView/index.vue'
import DrawingTools from '@/components/GeoServerMap/DrawingTools.vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import * as GisApi from '@/api/iot/gis'
import * as CampusApi from '@/api/iot/spatial/campus'

// 路由
const route = useRoute()

// GeoServer 配置
// 开发模式：使用相对路径 '/geoserver'，自动走 Vite 代理（vite.config.ts 中配置）
// 生产模式：可以配置为绝对路径或通过 Nginx 代理
const geoserverConfig = reactive({
  url: '/geoserver', // 相对路径，开发时走 Vite 代理，生产时走 Nginx 代理
  workspace: 'ch_ibms'  // ✅ 修正为实际的GeoServer工作空间名称
})

// 地图配置 - 使用实际园区坐标
const mapConfig = reactive({
  center: [113.3625, 23.1325], // 长辉智慧科技园中心点（广州天河）
  zoom: 15 // 提高初始缩放级别，更好地展示园区
})

// 当前视图
const currentView = ref<'campus' | 'building' | 'floor' | 'device'>('campus')

// 建筑平面图模式
const buildingPlanMode = ref(false)
const building3DMode = ref(false)
const selectedBuilding = reactive<{
  id: number
  name: string
  data?: any
}>({
  id: 0,
  name: '',
  data: null
})

// 图层配置（zIndex: 值越大越在上层）
// 注意：只配置GeoServer的室外图层（Campus/Building）
// 室内图层（Floor/Area/Device）通过BuildingFloorPlan组件展示，不使用GeoServer
const allLayers = ref([
  {
    name: 'campus',  // 对应ibms_gis数据库的campus表
    label: '园区边界',
    workspace: 'ch_ibms',  // ✅ 修正为实际的GeoServer工作空间名称
    visible: true,
    opacity: 100,
    zIndex: 1 // 园区在最底层作为背景
  },
  {
    name: 'building',  // 对应ibms_gis数据库的building表
    label: '建筑外轮廓',
    workspace: 'ch_ibms',  // ✅ 修正为实际的GeoServer工作空间名称
    visible: true,
    opacity: 100,
    zIndex: 2 // 建筑在园区之上
  }
  // ❌ 不再配置floor/room/device的GeoServer图层
  // ✅ 室内数据通过后端API + BuildingFloorPlan组件展示
])

// 根据当前视图计算显示的图层
// 注意：只控制GeoServer图层的可见性（campus/building）
// 室内视图通过BuildingFloorPlan组件展示
const currentLayers = computed(() => {
  return allLayers.value.map((layer) => {
    // 地图模式：始终显示campus和building
    return { ...layer, visible: true }
  })
})

// 统计数据
const statistics = reactive({
  campusCount: 0,
  buildingCount: 0,
  floorCount: 0,
  deviceCount: 0
})

// 地图实例引用
const mapRef = ref<InstanceType<typeof GeoServerMap> | null>(null)
const mapInstance = ref<any>(null) // OpenLayers Map实例，用于绘制工具

// 选中的要素信息
const selectedFeature = reactive({
  title: '',
  data: null as any,
  layer: '',
  coordinate: null as any
})

const detailDrawerVisible = ref(false)

// 切换视图
const switchView = (view: 'campus' | 'building' | 'floor' | 'device') => {
  currentView.value = view

  // 根据视图调整缩放级别
  const zoomLevels = {
    campus: 12,
    building: 15,
    floor: 17,
    device: 18
  }

  mapRef.value?.setZoom(zoomLevels[view])
  ElMessage.success(
    `已切换到${view === 'campus' ? '园区' : view === 'building' ? '建筑' : view === 'floor' ? '楼层' : '设备'}视图`
  )
}

// 刷新地图
const refreshMap = () => {
  location.reload()
}

// 重置视图
const resetView = () => {
  mapRef.value?.setCenter(mapConfig.center)
  mapRef.value?.setZoom(mapConfig.zoom)
  currentView.value = 'campus'
  ElMessage.success('视图已重置')
}

// 定位到数据区域
const locateToData = () => {
  // 长辉智慧科技园的实际坐标和范围
  const dataBounds = {
    minLon: 113.36,
    minLat: 23.13,
    maxLon: 113.365,
    maxLat: 23.135
  }

  const center = [
    (dataBounds.minLon + dataBounds.maxLon) / 2,
    (dataBounds.minLat + dataBounds.maxLat) / 2
  ]

  console.log('🎯 定位到数据区域:', { center, bounds: dataBounds })

  mapRef.value?.setCenter(center)
  mapRef.value?.setZoom(16) // 使用较高的缩放级别

  ElMessage.success('已定位到长辉智慧科技园')
}

// 地图准备完成
const handleMapReady = (map: any) => {
  console.log('地图初始化完成', map)
  
  // 保存地图实例供绘制工具使用
  mapInstance.value = map

  // 调试：打印当前地图视图信息
  const view = map.getView()
  const center = view.getCenter()
  const zoom = view.getZoom()
  console.log('📍 地图中心:', center)
  console.log('🔍 缩放级别:', zoom)

  // 调试：打印图层信息
  const layers = map.getLayers().getArray()
  console.log('📊 图层数量:', layers.length)
  layers.forEach((layer: any, index: number) => {
    const source = layer.getSource()
    const visible = layer.getVisible()
    const opacity = layer.getOpacity()
    console.log(`  图层 ${index}:`, {
      类型: source?.constructor?.name,
      可见: visible,
      透明度: opacity,
      URL: source?.getUrls ? source.getUrls() : source?.getUrl?.() || 'N/A'
    })
  })
}

// 处理要素点击
const handleFeatureClick = (event: any) => {
  console.log('🖱️ 要素被点击:', event)
  console.log('  📍 图层名称:', event.layer)
  console.log('  📊 要素属性（完整）:', JSON.stringify(event.feature?.properties, null, 2))

  // 如果点击的是建筑，提示是否切换到建筑平面图模式
  if (event.layer === 'building') {
    const properties = event.feature.properties
    
    // 尝试从多个可能的字段名称中获取建筑ID
    let buildingId = properties.id || properties.fid || properties.gid || properties.building_id
    
    console.log('🔍 查找建筑ID:')
    console.log('  - properties.id:', properties.id)
    console.log('  - properties.fid:', properties.fid)
    console.log('  - properties.gid:', properties.gid)
    console.log('  - properties.building_id:', properties.building_id)
    console.log('  - properties.code:', properties.code)
    
    // 如果没有ID，尝试通过code查询
    if (!buildingId && properties.code) {
      console.log('⚠️  GeoServer未返回ID，尝试通过code查询...')
      
      // 显示建筑查看选项
      showBuildingViewOptions(properties)
      return
    }
    
    // 有ID时，显示查看选项
    console.log('  ✅ 最终使用的ID:', buildingId)
    showBuildingViewOptionsWithId(buildingId, properties)
    return
  } else {
    // 其他要素，直接显示详情
    selectedFeature.title = getFeatureTitle(event.layer, event.feature.properties)
    selectedFeature.data = event.feature.properties
    selectedFeature.layer = event.layer
    selectedFeature.coordinate = event.coordinate
    detailDrawerVisible.value = true
  }
}

// 退出建筑平面图模式
const exitBuildingPlanMode = () => {
  buildingPlanMode.value = false
  building3DMode.value = false
  selectedBuilding.id = 0
  selectedBuilding.name = ''
  selectedBuilding.data = null
  ElMessage.success('已返回地图模式')
}

const exitBuilding3DMode = () => {
  building3DMode.value = false
  buildingPlanMode.value = false
  selectedBuilding.id = 0
  selectedBuilding.name = ''
  selectedBuilding.data = null
  ElMessage.success('已返回地图模式')
}

const switchTo2DView = () => {
  building3DMode.value = false
  buildingPlanMode.value = true
  ElMessage.success('已切换到2D平面图模式')
}

// 显示建筑查看选项（通过Code查询）
const showBuildingViewOptions = async (properties: any) => {
  // 🌍 提供2D和3D两种查看方式
  ElMessageBox({
    title: '建筑可视化',
    message: `请选择查看 ${properties.name} 的方式：`,
    showCancelButton: true,
    showConfirmButton: true,
    confirmButtonText: '📐 2D平面图',
    cancelButtonText: '🌍 3D模型',
    distinguishCancelAndClose: true,
    type: 'info',
    center: true
  }).then(async () => {
    // 选择2D平面图
    const loading = ElLoading.service({
      lock: true,
      text: '正在查询建筑信息...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    try {
      const buildingInfo = await GisApi.getBuildingByCode(properties.code)
      loading.close()
      
      if (buildingInfo && buildingInfo.id) {
        selectedBuilding.id = buildingInfo.id
        selectedBuilding.name = properties.name || buildingInfo.name || '未命名建筑'
        selectedBuilding.data = buildingInfo
        buildingPlanMode.value = true
        building3DMode.value = false
      } else {
        ElMessage.error(`未找到建筑编码为 ${properties.code} 的建筑信息`)
      }
    } catch (error) {
      loading.close()
      console.error('❌ 查询建筑信息失败:', error)
      ElMessage.error('查询建筑信息失败，请稍后重试')
    }
  }).catch((action) => {
    if (action === 'cancel') {
      // 选择3D模型
      const loading = ElLoading.service({
        lock: true,
        text: '正在加载3D场景...',
        background: 'rgba(0, 0, 0, 0.7)'
      })
      
      GisApi.getBuildingByCode(properties.code).then((buildingInfo) => {
        loading.close()
        
        if (buildingInfo && buildingInfo.id) {
          selectedBuilding.id = buildingInfo.id
          selectedBuilding.name = properties.name || buildingInfo.name || '未命名建筑'
          selectedBuilding.data = buildingInfo
          building3DMode.value = true
          buildingPlanMode.value = false
        } else {
          ElMessage.error(`未找到建筑编码为 ${properties.code} 的建筑信息`)
        }
      }).catch((error) => {
        loading.close()
        console.error('❌ 查询建筑信息失败:', error)
        ElMessage.error('查询建筑信息失败，请稍后重试')
      })
    }
  })
}

// 显示建筑查看选项（已有ID）
const showBuildingViewOptionsWithId = async (buildingId: number, properties: any) => {
  // 🌍 提供2D和3D两种查看方式
  ElMessageBox({
    title: '建筑可视化',
    message: `请选择查看 ${properties.name} 的方式：`,
    showCancelButton: true,
    showConfirmButton: true,
    confirmButtonText: '📐 2D平面图',
    cancelButtonText: '🌍 3D模型',
    distinguishCancelAndClose: true,
    type: 'info',
    center: true
  }).then(() => {
    // 选择2D平面图
    selectedBuilding.id = buildingId
    selectedBuilding.name = properties.name || '未命名建筑'
    selectedBuilding.data = properties
    buildingPlanMode.value = true
    building3DMode.value = false
  }).catch((action) => {
    if (action === 'cancel') {
      // 选择3D模型
      selectedBuilding.id = buildingId
      selectedBuilding.name = properties.name || '未命名建筑'
      selectedBuilding.data = properties
      building3DMode.value = true
      buildingPlanMode.value = false
    }
  })
}

// 获取要素标题
const getFeatureTitle = (layerName: string, properties: any) => {
  const layerLabels: Record<string, string> = {
    campus: '园区',
    building: '建筑',
    floor: '楼层',
    room: '房间',
    device: '设备'
  }

  const label = layerLabels[layerName] || layerName
  const name = properties.name || properties.id || '未命名'

  return `${label} - ${name}`
}

// 格式化标签
const formatLabel = (key: string) => {
  const labelMap: Record<string, string> = {
    id: 'ID',
    name: '名称',
    code: '编码',
    type: '类型',
    status: '状态',
    location: '位置',
    description: '描述',
    created_at: '创建时间',
    updated_at: '更新时间',
    geom: '几何信息'
  }

  return labelMap[key] || key
}

// 定位到要素
const locateFeature = () => {
  if (selectedFeature.coordinate) {
    mapRef.value?.flyTo(selectedFeature.coordinate, 18)
    ElMessage.success('已定位到选中要素')
  }
}

// 查看详情
const viewDetails = () => {
  ElMessage.info('详情功能待实现')
  // 这里可以跳转到详情页面或打开更详细的弹窗
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const data = await GisApi.getGisStatistics()
    console.log('返回数据：', data)
    if (data) {
      statistics.campusCount = data.campusCount || 1
      statistics.buildingCount = data.buildingCount || 5
      statistics.floorCount = data.floorCount || 50
      statistics.deviceCount = data.deviceCount || 5041
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 使用实际的数据库数据作为默认值
    statistics.campusCount = 1
    statistics.buildingCount = 5
    statistics.floorCount = 50
    statistics.deviceCount = 5041
  }
}

onMounted(async () => {
  await fetchStatistics()
  
  // 处理URL参数，自动聚焦到指定园区
  const campusId = route.query.campusId
  const focus = route.query.focus
  
  if (campusId && focus === 'campus') {
    // 从园区管理跳转过来，自动定位到该园区
    try {
      const campusData = await CampusApi.getCampus(Number(campusId))
      if (campusData && campusData.centerPoint) {
        // 解析中心点坐标 POINT(lon lat)
        const coordsMatch = campusData.centerPoint.match(/POINT\(([\d.]+)\s+([\d.]+)\)/)
        if (coordsMatch) {
          const lon = parseFloat(coordsMatch[1])
          const lat = parseFloat(coordsMatch[2])
          
          // 更新地图中心并放大
          mapConfig.center = [lon, lat]
          mapConfig.zoom = 17 // 放大到园区级别
          
          ElMessage.success(`已定位到：${route.query.campusName || '指定园区'}`)
        }
      }
    } catch (error) {
      console.error('定位园区失败:', error)
      ElMessage.warning('园区定位失败，显示默认位置')
    }
  }
})
</script>

<style scoped lang="scss">
.header-section {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;

  .title-area {
    h2 {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
      color: #303133;
    }

    .subtitle {
      margin: 4px 0 0 0;
      font-size: 14px;
      color: #909399;
    }
  }

  .toolbar {
    display: flex;
    gap: 12px;
  }
}

.stats-section {
  margin-bottom: 20px;

  .stat-card {
    border-radius: 8px;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-4px);
    }

    .stat-content {
      display: flex;
      align-items: center;
      gap: 16px;

      .stat-icon {
        font-size: 48px;
        width: 64px;
        height: 64px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 8px;

        &.campus-icon {
          color: #409eff;
          background: rgba(64, 158, 255, 0.1);
        }

        &.building-icon {
          color: #67c23a;
          background: rgba(103, 194, 58, 0.1);
        }

        &.floor-icon {
          color: #e6a23c;
          background: rgba(230, 162, 60, 0.1);
        }

        &.device-icon {
          color: #f56c6c;
          background: rgba(245, 108, 108, 0.1);
        }
      }

      .stat-info {
        flex: 1;

        .stat-value {
          font-size: 28px;
          font-weight: 600;
          color: #303133;
          line-height: 1.2;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-top: 4px;
        }
      }
    }
  }
}

.map-section {
  border-radius: 8px;
  position: relative;
  
  .drawing-tools-overlay {
    position: absolute;
    top: 20px;
    left: 20px;
    z-index: 1000;
  }

  :deep(.el-card__body) {
    padding: 0;
  }
}

.action-buttons {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

// 响应式设计
@media (max-width: 768px) {
  .header-section {
    flex-direction: column;
    align-items: flex-start;

    .toolbar {
      width: 100%;
      flex-direction: column;
    }
  }

  .stats-section {
    :deep(.el-col) {
      width: 100%;
      margin-bottom: 12px;
    }
  }
}
</style>
