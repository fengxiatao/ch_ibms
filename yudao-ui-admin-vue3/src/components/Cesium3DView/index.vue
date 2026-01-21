<template>
  <div class="cesium-3d-view">
    <!-- 头部：返回按钮 + 建筑信息 -->
    <div class="viewer-header">
      <el-button @click="backToMap" icon="ArrowLeft" type="primary" plain>
        返回地图
      </el-button>
      <div class="building-info">
        <h3>{{ buildingName }}</h3>
        <span class="building-desc">3D 建筑展示</span>
      </div>
      <div class="view-controls">
        <el-button-group>
          <el-button @click="resetView" icon="Refresh" size="small">重置视角</el-button>
          <el-button @click="switchTo2D" icon="Grid" size="small">切换2D</el-button>
        </el-button-group>
      </div>
    </div>

    <!-- Cesium 容器 -->
    <div ref="cesiumContainer" class="cesium-container" :style="{ height: viewerHeight }">
      <div id="cesiumViewer" style="width: 100%; height: 100%;"></div>
      
      <!-- 加载中状态 -->
      <div v-if="loading" class="loading-overlay">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>正在加载3D场景...</p>
      </div>
      
      <!-- 错误提示 -->
      <div v-if="error" class="error-overlay">
        <el-result icon="error" title="加载失败" :sub-title="error">
          <template #extra>
            <el-button type="primary" @click="retryLoad">重试</el-button>
            <el-button @click="backToMap">返回</el-button>
          </template>
        </el-result>
      </div>
    </div>

    <!-- 图层控制面板 -->
    <div class="layer-control">
      <!-- 房间聚焦提示 -->
      <el-alert
        v-if="focusedArea"
        type="success"
        :closable="false"
        style="margin-bottom: 10px;"
      >
        <template #title>
          <div style="display: flex; align-items: center; justify-content: space-between;">
            <span>📍 {{ focusedArea.name }}</span>
            <el-button size="small" @click="exitAreaFocus">返回楼层</el-button>
          </div>
        </template>
      </el-alert>
      
      <el-card>
        <template #header>
          <span>{{ focusedArea ? '房间视图' : '图层控制' }}</span>
        </template>
        <el-checkbox v-model="showBuilding" @change="toggleBuildingLayer">建筑模型</el-checkbox>
        <el-checkbox v-model="showTerrain" @change="toggleTerrainLayer">地形</el-checkbox>
        <el-checkbox v-model="showDevices" @change="toggleDeviceLayer">设备点位</el-checkbox>
      </el-card>
      
      <!-- 楼层切换 -->
      <el-card v-if="floors.length > 0" style="margin-top: 10px;">
        <template #header>
          <span>楼层切换</span>
        </template>
        <el-select 
          v-model="currentFloor" 
          @change="switchFloor"
          placeholder="选择楼层"
          size="small"
          style="width: 100%;"
        >
          <el-option
            v-for="floor in floors"
            :key="floor.id"
            :label="`${floor.name}`"
            :value="floor.floorNumber"
          />
        </el-select>
        
        <el-divider style="margin: 12px 0;" />
        
        <el-button 
          @click="toggleViewMode" 
          type="warning" 
          size="small"
          style="width: 100%;"
        >
          {{ showIndoorView ? '🏢 外观模式' : '🏠 室内模式' }}
        </el-button>
      </el-card>
    </div>

    <!-- 建筑信息面板 -->
    <div class="info-panel" v-if="buildingInfo">
      <el-card>
        <template #header>
          <span>建筑信息</span>
        </template>
        <el-descriptions :column="1" size="small">
          <el-descriptions-item label="名称">{{ buildingInfo.name }}</el-descriptions-item>
          <el-descriptions-item label="编码">{{ buildingInfo.code }}</el-descriptions-item>
          <el-descriptions-item label="总楼层">{{ buildingInfo.total_floors || buildingInfo.totalFloors }}</el-descriptions-item>
          <el-descriptions-item label="高度">{{ buildingInfo.building_height || buildingInfo.height }}m</el-descriptions-item>
          <el-descriptions-item label="建筑面积">{{ buildingInfo.building_area || buildingInfo.builtArea }}m²</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import * as GisApi from '@/api/iot/gis'

interface Props {
  buildingId: number
  buildingName?: string
  buildingData?: any
  viewerHeight?: string
}

const props = withDefaults(defineProps<Props>(), {
  buildingName: '建筑',
  viewerHeight: 'calc(100vh - 60px)'
})

const emit = defineEmits(['back', 'switch-to-2d'])

// 状态
const cesiumContainer = ref<HTMLElement>()
const loading = ref(true)
const error = ref('')
const viewer = ref<any>(null)

// 图层控制
const showBuilding = ref(true)
const showTerrain = ref(false) // 默认关闭地形（使用椭球体）
const showDevices = ref(true)

// 建筑信息
const buildingInfo = ref<any>(null)

// 楼层和室内数据
const floors = ref<any[]>([])
const currentFloor = ref(1)
const floorData = ref<any>(null)
const showIndoorView = ref(true) // 是否显示室内视图
const focusedArea = ref<any>(null) // 当前聚焦的区域（房间）

// 返回地图
const backToMap = () => {
  emit('back')
}

// 切换到2D视图
const switchTo2D = () => {
  emit('switch-to-2d')
}

// 初始化Cesium
const initCesium = async () => {
  try {
    loading.value = true
    error.value = ''

    // 🌍 使用全局Cesium对象（通过CDN加载）
    const Cesium = (window as any).Cesium
    if (!Cesium) {
      throw new Error('Cesium库未加载，请检查网络连接')
    }
    
    // 🔧 不使用Ion Token，使用免费的地图服务
    // 使用OpenStreetMap作为底图，不需要Token
    const osmProvider = new Cesium.OpenStreetMapImageryProvider({
      url: 'https://a.tile.openstreetmap.org/'
    })

    // 创建Cesium Viewer
    viewer.value = new Cesium.Viewer('cesiumViewer', {
      terrainProvider: new Cesium.EllipsoidTerrainProvider(), // 使用椭球体地形，不需要Token
      animation: false,
      timeline: false,
      fullscreenButton: false,
      vrButton: false,
      geocoder: false,
      homeButton: true,
      infoBox: true,
      sceneModePicker: true,
      selectionIndicator: true,
      navigationHelpButton: true, // 启用导航帮助
      baseLayerPicker: false, // 禁用底图选择器，避免使用Ion服务
      imageryProvider: osmProvider, // 使用OpenStreetMap底图
      // 启用所有鼠标控制
      scene3DOnly: false, // 允许2D/3D切换
      shouldAnimate: true // 启用动画
    })

    // 🎮 启用完整的相机控制
    const scene = viewer.value.scene
    
    // 启用所有相机控制选项
    scene.screenSpaceCameraController.enableRotate = true      // 启用旋转
    scene.screenSpaceCameraController.enableTranslate = true   // 启用平移
    scene.screenSpaceCameraController.enableZoom = true        // 启用缩放
    scene.screenSpaceCameraController.enableTilt = true        // 启用倾斜
    scene.screenSpaceCameraController.enableLook = true        // 启用视角查看
    
    // 调整控制灵敏度
    scene.screenSpaceCameraController.zoomEventTypes = [
      Cesium.CameraEventType.WHEEL,
      Cesium.CameraEventType.PINCH
    ]
    scene.screenSpaceCameraController.tiltEventTypes = [
      Cesium.CameraEventType.RIGHT_DRAG,
      Cesium.CameraEventType.PINCH,
      {
        eventType: Cesium.CameraEventType.LEFT_DRAG,
        modifier: Cesium.KeyboardEventModifier.CTRL
      }
    ]
    scene.screenSpaceCameraController.rotateEventTypes = [
      Cesium.CameraEventType.LEFT_DRAG
    ]
    
    // 设置最小和最大缩放距离
    scene.screenSpaceCameraController.minimumZoomDistance = 5   // 最近5米
    scene.screenSpaceCameraController.maximumZoomDistance = 5000 // 最远5000米

    // 📍 监听相机移动事件（用于LOD动态更新）
    let lastLoggedHeight = 0
    viewer.value.camera.moveEnd.addEventListener(() => {
      const currentHeight = getCameraHeight()
      // 每变化20米输出一次日志
      if (Math.abs(currentHeight - lastLoggedHeight) > 20) {
        console.log(`📏 相机高度变化: ${currentHeight.toFixed(1)}m (LOD级别: ${getLODLevel(currentHeight)})`)
        lastLoggedHeight = currentHeight
      }
    })

    // 🖱️ 监听实体点击事件（用于房间聚焦和设备详情）
    const handler = new Cesium.ScreenSpaceEventHandler(viewer.value.scene.canvas)
    handler.setInputAction((click: any) => {
      const pickedObject = viewer.value.scene.pick(click.position)
      if (Cesium.defined(pickedObject) && Cesium.defined(pickedObject.id)) {
        const entity = pickedObject.id
        
        // 🔧 关键逻辑：根据当前状态决定点击行为
        
        // 情况1：点击设备 - 显示设备详情
        if (entity.id && entity.id.startsWith('device-')) {
          const deviceData = entity.properties
          if (deviceData) {
            showDeviceDetailDialog(entity)
          }
        }
        // 情况2：点击区域（房间）
        else if (entity.id && entity.id.startsWith('area-')) {
          // ⚠️ 只有在楼层视图模式下，点击房间才会进入房间聚焦模式
          // 如果已经在房间聚焦模式下，点击房间应该切换到该房间
          if (!focusedArea.value) {
            // 🏠 楼层视图模式 -> 进入房间聚焦模式
            const areaData = entity.properties?.areaData?.getValue()
            if (areaData) {
              focusOnArea(areaData)
            }
          } else {
            // 🔄 房间聚焦模式 -> 切换到其他房间
            const areaData = entity.properties?.areaData?.getValue()
            if (areaData && areaData.id !== focusedArea.value.id) {
              console.log(`🔄 切换房间: ${focusedArea.value.name} -> ${areaData.name}`)
              focusOnArea(areaData)
            }
          }
        }
      }
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK)

    // 加载建筑数据
    await loadBuildingData()

    loading.value = false
    
  } catch (err: any) {
    console.error('❌ 初始化Cesium失败:', err)
    error.value = err.message || '初始化3D场景失败'
    loading.value = false
    ElMessage.error('初始化3D场景失败: ' + error.value)
  }
}

// 加载建筑数据
const loadBuildingData = async () => {
  try {
    // 获取建筑详细信息
    if (props.buildingData) {
      buildingInfo.value = props.buildingData
    } else {
      // 如果没有传入建筑数据，直接使用props中的信息
      buildingInfo.value = {
        id: props.buildingId,
        name: props.buildingName
      }
    }

    console.log('🏢 建筑信息:', buildingInfo.value)

    // 🏗️ 加载楼层数据
    await loadFloors()
    
    // 🏢 如果有楼层数据且启用室内视图，渲染室内3D
    if (floors.value.length > 0 && showIndoorView.value) {
      console.log(`🎯 准备渲染室内视图: 楼层${currentFloor.value}`)
      try {
        await loadFloorData(currentFloor.value)
        
        // 检查数据是否加载成功
        if (!floorData.value) {
          console.error('❌ 楼层数据为空，无法渲染')
          ElMessage.warning(`楼层 ${currentFloor.value} 数据加载失败，切换到建筑外观`)
          showIndoorView.value = false
          showBuildingMarker()
        } else {
          console.log(`✅ 楼层数据已加载，开始渲染3D场景`)
          await renderIndoor3D()
        }
      } catch (err) {
        console.error('❌ 渲染室内场景失败:', err)
        ElMessage.error('渲染室内场景失败，已切换到外观模式')
        showIndoorView.value = false
        showBuildingMarker()
      }
    } else {
      // 否则渲染建筑外观
      console.log(`🏢 渲染建筑外观模式 (楼层数: ${floors.value.length}, 室内视图: ${showIndoorView.value})`)
      if (buildingInfo.value.geom || buildingInfo.value.geometry) {
        await renderBuilding3D()
      } else {
        showBuildingMarker()
      }
    }

    // 飞到建筑位置
    flyToBuilding()

  } catch (err: any) {
    console.error('❌ 加载建筑数据失败:', err)
    ElMessage.warning('无法加载建筑3D数据，将显示默认视图')
  }
}

// 🏗️ 加载楼层列表
const loadFloors = async () => {
  try {
    const data = await GisApi.getBuildingFloors(props.buildingId)
    if (data && data.length > 0) {
      floors.value = data
      currentFloor.value = data[0].floorNumber
      console.log(`✅ 加载了 ${data.length} 个楼层`)
    } else {
      floors.value = []
      console.log('ℹ️ 该建筑没有楼层数据，使用建筑外观模式')
    }
  } catch (err) {
    console.error('❌ 加载楼层数据失败:', err)
    floors.value = []
  }
}

// 🏢 加载楼层详细数据（区域、设备）
const loadFloorData = async (floorNumber: number) => {
  try {
    const floor = floors.value.find(f => f.floorNumber === floorNumber)
    if (!floor) {
      console.error(`❌ 未找到楼层号 ${floorNumber} 的数据`)
      ElMessage.error(`未找到楼层 ${floorNumber}`)
      return
    }
    
    console.log(`🔄 加载楼层数据: ${floor.name} (ID: ${floor.id})`)
    const data = await GisApi.getFloorVisualizationData(floor.id)
    
    console.log(`📦 API返回数据类型: ${typeof data}`, data)
    
    // 解析数据（可能是JSON字符串）
    let parsedData = data
    if (typeof data === 'string') {
      console.log('📝 数据是字符串，尝试JSON解析')
      parsedData = JSON.parse(data)
    }
    
    // 验证数据结构
    if (!parsedData) {
      console.error('❌ 解析后的数据为空')
      floorData.value = null
      ElMessage.error('楼层数据为空')
      return
    }
    
    const areas = parsedData.areas || []
    const devices = parsedData.devices || []
    console.log(`✅ 楼层 ${floorNumber} 数据解析成功: ${areas.length} 个区域, ${devices.length} 个设备`)
    
    if (areas.length === 0 && devices.length === 0) {
      console.warn(`⚠️ 楼层 ${floorNumber} 没有区域和设备数据`)
    }
    
    floorData.value = parsedData
  } catch (err) {
    console.error(`❌ 加载楼层 ${floorNumber} 数据失败:`, err)
    ElMessage.error(`加载楼层 ${floorNumber} 数据失败: ${(err as Error).message}`)
    floorData.value = null
  }
}

// 🎨 渲染室内3D场景（房间+设备）
const renderIndoor3D = async () => {
  if (!viewer.value) {
    console.error('❌ Viewer未初始化')
    return
  }
  
  if (!floorData.value) {
    console.error('❌ 楼层数据为空')
    ElMessage.warning('楼层数据为空，无法渲染3D场景')
    return
  }
  
  try {
    const areas = floorData.value.areas || []
    const devices = floorData.value.devices || []
    const floorInfo = floorData.value.floor_info || {}
    
    console.log(`🏗️ 渲染室内3D: ${areas.length} 个区域, ${devices.length} 个设备`)
    
    // ⚠️ 检查数据完整性
    if (areas.length === 0 && devices.length === 0) {
      console.warn('⚠️ 当前楼层无区域和设备数据')
      ElMessage.warning('当前楼层暂无数据')
      return
    }
    
    // 📊 构建区域-设备映射（用于密度计算）
    const areaDeviceMap = new Map<number, number>()
    devices.forEach((device: any) => {
      const areaId = device.area_id
      areaDeviceMap.set(areaId, (areaDeviceMap.get(areaId) || 0) + 1)
    })
    
    // 1. 渲染区域（房间、走廊等）
    let areaRendered = 0
    areas.forEach((area: any) => {
      try {
        const deviceCount = areaDeviceMap.get(area.id) || 0
        renderArea3D(area, floorInfo, deviceCount)
        areaRendered++
      } catch (err) {
        console.error(`❌ 渲染区域失败: ${area.name}`, err)
      }
    })
    
    // 2. 渲染设备（带密度信息）
    let deviceRendered = 0
    devices.forEach((device: any) => {
      try {
        const deviceCount = areaDeviceMap.get(device.area_id) || 1
        renderDevice3D(device, floorInfo, deviceCount)
        deviceRendered++
      } catch (err) {
        console.error(`❌ 渲染设备失败: ${device.name}`, err)
      }
    })
    
    const cameraHeight = getCameraHeight()
    console.log(`📏 相机高度: ${cameraHeight.toFixed(1)}m`)
    console.log(`✅ 渲染完成: ${areaRendered}/${areas.length} 个区域, ${deviceRendered}/${devices.length} 个设备`)
    
    ElMessage.success(`已加载 ${areaRendered} 个区域和 ${deviceRendered} 个设备`)
  } catch (err) {
    console.error('❌ 渲染室内3D失败:', err)
    ElMessage.error('渲染3D场景失败: ' + (err as Error).message)
  }
}

// 🎨 生成程序化纹理（Canvas）- 暂时禁用，因为会在顶部也显示导致混乱
// const generateTexture = (type: string): string => {
//   const canvas = document.createElement('canvas')
//   canvas.width = 256
//   canvas.height = 256
//   const ctx = canvas.getContext('2d')
//   if (!ctx) return ''
//   
//   // 根据区域类型生成不同纹理
//   switch (type) {
//     case 'ROOM':
//       // 木地板纹理
//       ctx.fillStyle = '#D2B48C'
//       ctx.fillRect(0, 0, 256, 256)
//       ctx.strokeStyle = '#8B7355'
//       ctx.lineWidth = 2
//       for (let i = 0; i < 256; i += 32) {
//         ctx.beginPath()
//         ctx.moveTo(i, 0)
//         ctx.lineTo(i, 256)
//         ctx.stroke()
//       }
//       break
//     case 'CORRIDOR':
//       // 瓷砖纹理
//       ctx.fillStyle = '#E8E8E8'
//       ctx.fillRect(0, 0, 256, 256)
//       ctx.strokeStyle = '#CCCCCC'
//       ctx.lineWidth = 1
//       for (let i = 0; i < 256; i += 64) {
//         for (let j = 0; j < 256; j += 64) {
//           ctx.strokeRect(i, j, 64, 64)
//         }
//       }
//       break
//     case 'ELEVATOR':
//     case 'STAIRCASE':
//       // 金属纹理
//       ctx.fillStyle = '#C0C0C0'
//       ctx.fillRect(0, 0, 256, 256)
//       ctx.fillStyle = '#A8A8A8'
//       for (let i = 0; i < 256; i += 8) {
//         ctx.fillRect(i, 0, 4, 256)
//       }
//       break
//     default:
//       // 默认纹理
//       ctx.fillStyle = '#F5F5F5'
//       ctx.fillRect(0, 0, 256, 256)
//   }
//   
//   return canvas.toDataURL()
// }

// 🏠 渲染单个区域（3D）
const renderArea3D = (area: any, _floorInfo: any, deviceCount: number = 0) => {
  if (!viewer.value) return

  const Cesium = (window as any).Cesium
  
  try {
    // 获取区域几何数据
    const geometry = area.geometry
    if (!geometry || !geometry.coordinates || !geometry.coordinates[0]) return
    
    // 提取坐标（假设是Polygon）
    const coords = geometry.coordinates[0]
    
    // 转换为经纬度坐标（使用建筑的基准点）
    const baseLon = buildingInfo.value?.longitude || 113.264385
    const baseLat = buildingInfo.value?.latitude || 23.129163
    
    // 📍 优化：精确的坐标转换（考虑纬度影响）
    const positions = coords.map((coord: any[]) => {
      const [x, y] = coord
      // 📐 精确转换：
      // 纬度1度 ≈ 111km (恒定)
      // 经度1度 ≈ 111km * cos(latitude) (随纬度变化)
      const latRadians = baseLat * Math.PI / 180
      const metersPerDegreeLat = 111320  // 纬度方向米数
      const metersPerDegreeLon = 111320 * Math.cos(latRadians)  // 经度方向米数（考虑纬度）
      
      const lon = baseLon + (x / metersPerDegreeLon)
      const lat = baseLat + (y / metersPerDegreeLat)
      return [lon, lat]
    }).flat()
    
    // 📍 计算楼层高度（修正：楼层从1开始）
    const floorNumber = currentFloor.value
    const floorHeight = 3.5 // 标准层高：3.5米
    const baseHeight = (floorNumber - 1) * floorHeight  // 楼层1从高度0开始
    
    // 🎨 优化：使用矮墙高度（参考BIM俯视图效果）
    // 矮墙高度约1.2-1.5m，便于俯视时看清室内布局和设备
    // 不使用全高墙体（3.5m），因为俯视时会遮挡内部
    const wallHeight = 1.2  // 矮墙高度（类似BIM俯视图）
    const extrudeHeight = baseHeight + wallHeight
    
    // 🔍 调试信息（仅首个区域）
    if (area.id === 1 || area.name.includes('01')) {
      console.log(`📐 区域 ${area.name}:`, {
        楼层: floorNumber,
        基础高度: baseHeight,
        墙体高度: wallHeight,
        挤出高度: extrudeHeight,
        虚拟坐标示例: coords[0],
        经纬度示例: [positions[0], positions[1]]
      })
    }
    
    // 区域类型颜色
    const typeColors: Record<string, string> = {
      'ROOM': '#E3F2FD',
      'CORRIDOR': '#F5F5F5',
      'ELEVATOR': '#FFF9C4',
      'STAIRCASE': '#FFECB3',
      'LOBBY': '#E8F5E9',
      'RESTROOM': '#FCE4EC',
      'STORAGE': '#EFEBE9',
      'PARKING': '#ECEFF1'
    }
    
    const fillColor = area.fill_color || typeColors[area.type] || '#E3F2FD'
    const strokeColor = area.stroke_color || '#2196F3'
    
    // 🎨 优化：使用半透明墙体（类似BIM俯视图）
    // 矮墙+半透明，俯视时可以清楚看到室内布局和设备
    const material = Cesium.Color.fromCssColorString(fillColor).withAlpha(0.4)  // 降低不透明度到0.4
    
    // 创建3D多边形（地板+墙体）
    viewer.value.entities.add({
      name: area.name,
      id: `area-${area.id}`,  // 添加ID用于点击识别
      polygon: {
        hierarchy: Cesium.Cartesian3.fromDegreesArray(positions),
        height: baseHeight,
        extrudedHeight: extrudeHeight,
        material: material,
        outline: true,
        outlineColor: Cesium.Color.fromCssColorString(strokeColor),
        outlineWidth: 3
      },
      // 存储区域数据用于点击事件
      properties: {
        areaId: area.id,
        areaName: area.name,
        areaType: area.type,
        areaData: area,
        centerCoords: coords[Math.floor(coords.length / 2)]  // 区域中心坐标
      },
      description: `
        <div style="padding: 15px; min-width: 250px;">
          <h3 style="margin: 0 0 10px 0; color: #333;">${area.name}</h3>
          <table style="width: 100%; border-collapse: collapse;">
            <tr>
              <td style="padding: 5px 10px; color: #666;">类型</td>
              <td style="padding: 5px 10px; font-weight: bold;">${area.type}</td>
            </tr>
            <tr>
              <td style="padding: 5px 10px; color: #666;">面积</td>
              <td style="padding: 5px 10px;">${area.area_sqm}m²</td>
            </tr>
            <tr>
              <td style="padding: 5px 10px; color: #666;">设备数量</td>
              <td style="padding: 5px 10px;">
                <span style="display: inline-block; padding: 2px 8px; border-radius: 3px; background: #2196F3; color: white; font-size: 12px;">
                  ${deviceCount} 个
                </span>
              </td>
            </tr>
            <tr>
              <td style="padding: 5px 10px; color: #666;">楼层</td>
              <td style="padding: 5px 10px;">${currentFloor.value}层</td>
            </tr>
          </table>
        </div>
      `
    })
    
  } catch (err) {
    console.error('渲染区域失败:', area.name, err)
  }
}

// 📏 获取相机高度（用于LOD）
const getCameraHeight = (): number => {
  if (!viewer.value) return 100
  const Cesium = (window as any).Cesium
  const camera = viewer.value.camera
  const cartographic = Cesium.Cartographic.fromCartesian(camera.position)
  return cartographic.height
}

// 🎯 获取LOD级别描述
const getLODLevel = (height: number): string => {
  if (height > 100) return '级别1-俯瞰（隐藏标签）'
  if (height > 50) return '级别2-楼层视角（故障标签）'
  if (height > 20) return '级别3-区域视角（部分标签）'
  return '级别4-近距离（完整标签）'
}

// 🎯 根据相机距离和设备密度计算图标大小（放宽LOD限制）
const getDeviceIconSize = (areaDeviceCount: number): { width: number, height: number, scale: number } => {
  const cameraHeight = getCameraHeight()
  
  // 🔧 平衡优化：既要保持合理比例，又要能清楚看到图标形状
  let baseSize = 32  // 基础尺寸32像素（在48和16之间取平衡）
  let scale = 0.8    // 缩放系数0.8（在1.5和0.4之间取平衡）
  
  // 级别1：俯瞰（>150m）- 较小
  if (cameraHeight > 150) {
    baseSize = 24
    scale = 0.6
  }
  // 级别2：远距离（80-150m）- 中等
  else if (cameraHeight > 80) {
    baseSize = 28
    scale = 0.7
  }
  // 级别3：中距离（40-80m）- 正常
  else if (cameraHeight > 40) {
    baseSize = 32
    scale = 0.8
  }
  // 级别4：近距离（<40m）- 清晰
  else {
    baseSize = 36
    scale = 0.9
  }
  
  // 根据设备密度调整（设备越多，稍微缩小）
  if (areaDeviceCount > 10) {
    scale *= 0.8  // 密集区域适度缩小
  } else if (areaDeviceCount > 5) {
    scale *= 0.9
  }
  
  return {
    width: baseSize,
    height: baseSize,
    scale: scale
  }
}

// 🏷️ 判断是否应该显示标签（放宽限制）
const shouldShowLabel = (device: any, cameraHeight: number): boolean => {
  // 级别1：俯瞰（>150m）- 只显示故障设备
  if (cameraHeight > 150) {
    return device.status === 'fault'
  }
  
  // 级别2：远距离（80-150m）- 显示故障和离线
  if (cameraHeight > 80) {
    return device.status !== 'online'
  }
  
  // 级别3：中距离（40-80m）- 显示所有标签
  if (cameraHeight > 40) {
    return true
  }
  
  // 级别4：近距离（<40m）- 显示所有标签
  return true
}

// 📍 获取设备图标路径
const getDeviceIcon = (deviceType: string): string => {
  // 设备类型映射到图标文件
  const iconMap: Record<string, string> = {
    '摄像头': '26、半球摄像机.svg',
    '监控': '27、枪型摄像机.svg',
    '门禁': '36、人脸门禁一体机.svg',
    '传感器': '50、温湿度传感器.svg',
    '温感': '49、温感.svg',
    '烟感': '52、烟感.svg',
    '空调': '35、空调机组.svg',
    '消防': '30、声光报警器.svg',
    '灯具': '53、应急照明灯.svg',
    '广播': '43、广播媒体.svg',
    '电源': '38、UPS电源.svg',
    '网络设备': '38、UPS电源.svg',
    '报警': '29、报警主机.svg',
    '闸机': '33、人行闸机.svg',
    '道闸': '40、车道闸.svg',
    '电梯': '41、垂直电梯.svg',
    '水泵': '46、水泵.svg'
  }
  
  // 查找匹配的图标
  for (const [key, icon] of Object.entries(iconMap)) {
    if (deviceType && deviceType.includes(key)) {
      return `/icons/devices/${icon}`
    }
  }
  
  // 默认图标
  return '/icons/devices/65、环境传感器.svg'
}

// 📍 智能计算设备高度（根据设备类型）
const getDeviceHeight = (deviceType: string, floorHeight: number = 3.5): number => {
  const type = deviceType?.toLowerCase() || ''
  
  // 🏢 地面设备（0.2-0.5m）
  const groundDevices = ['ups', '电源', '闸机', '道闸', '充电', '配电', '水泵', '机柜']
  if (groundDevices.some(keyword => type.includes(keyword))) {
    return 0.3  // 地面30cm
  }
  
  // 🚪 墙面设备（1.2-1.5m）- 门禁、开关、消防栓等
  const wallDevices = ['门禁', '开关', '插座', '消防栓', '按钮', '面板', '读卡器']
  if (wallDevices.some(keyword => type.includes(keyword))) {
    return 1.4  // 墙面1.4m（人眼高度）
  }
  
  // 🎥 天花板设备（层高-0.3m）- 摄像头、烟感、灯具、空调等
  const ceilingDevices = ['摄像', '监控', '烟感', '温感', '灯', '照明', '空调', '风口', '喷淋', '广播', '扬声']
  if (ceilingDevices.some(keyword => type.includes(keyword))) {
    return floorHeight - 0.3  // 天花板下方30cm（例如3.2m）
  }
  
  // 📡 中高位设备（2.0-2.5m）- 报警器、探测器等
  const highDevices = ['报警', '探测', '探头', '声光']
  if (highDevices.some(keyword => type.includes(keyword))) {
    return 2.3  // 中高位2.3m
  }
  
  // 默认：桌面/设备高度（0.8-1.2m）
  return 1.0  // 默认1.0m（桌面高度）
}

// 📍 渲染单个设备（3D - LOD + 密度自适应）
const renderDevice3D = (device: any, _floorInfo: any, areaDeviceCount: number = 1) => {
  if (!viewer.value) return

  const Cesium = (window as any).Cesium
  
  try {
    // 获取设备坐标
    const geometry = device.geometry
    if (!geometry || !geometry.coordinates) return
    
    const [x, y] = geometry.coordinates
    
    // 📍 转换为经纬度（精确转换）
    const baseLon = buildingInfo.value?.longitude || 113.264385
    const baseLat = buildingInfo.value?.latitude || 23.129163
    
    const latRadians = baseLat * Math.PI / 180
    const metersPerDegreeLat = 111320
    const metersPerDegreeLon = 111320 * Math.cos(latRadians)
    
    const lon = baseLon + (x / metersPerDegreeLon)
    const lat = baseLat + (y / metersPerDegreeLat)
    
    // 📍 计算设备高度（智能定位）
    const floorNumber = currentFloor.value
    const floorHeight = 3.5
    const baseHeight = (floorNumber - 1) * floorHeight
    
    // 🔧 智能设备定位：
    // - 如果有 z_coordinate 且合理（0-5m），使用实际值
    // - 否则根据设备类型智能计算高度
    let deviceZ = 1.5  // 默认高度
    if (device.z_coordinate && device.z_coordinate > 0 && device.z_coordinate < 5) {
      deviceZ = device.z_coordinate  // 使用实际高度
    } else {
      deviceZ = getDeviceHeight(device.type, floorHeight)  // 智能计算
    }
    const deviceHeight = baseHeight + deviceZ
    
    // 🎯 LOD：获取相机高度和对应的图标大小
    const cameraHeight = getCameraHeight()
    const iconSize = getDeviceIconSize(areaDeviceCount)
    
    // 🏷️ LOD：判断是否显示标签
    const showLabel = shouldShowLabel(device, cameraHeight)
    
    // 设备状态颜色
    const statusColors: Record<string, any> = {
      'online': Cesium.Color.fromCssColorString('#4CAF50'),
      'offline': Cesium.Color.fromCssColorString('#9E9E9E'),
      'fault': Cesium.Color.fromCssColorString('#F44336')
    }
    
    const deviceColor = statusColors[device.status] || Cesium.Color.fromCssColorString('#2196F3')
    
    // 获取设备图标
    const iconPath = getDeviceIcon(device.type)
    
    // 🎨 创建设备标记（LOD优化）
    const entityConfig: any = {
      name: device.name,
      id: `device-${device.id}`,  // 添加ID用于识别
      position: Cesium.Cartesian3.fromDegrees(lon, lat, deviceHeight),
      // 存储设备数据用于聚焦
      properties: {
        deviceId: device.id,
        deviceName: device.name,
        deviceAreaId: device.area_id,  // 关键：存储设备所属区域ID
        deviceType: device.type,
        deviceStatus: device.status
      },
      billboard: {
        image: iconPath,
        width: iconSize.width,
        height: iconSize.height,
        color: deviceColor.withAlpha(0.9),
        scale: iconSize.scale,
        verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
        horizontalOrigin: Cesium.HorizontalOrigin.CENTER,
        heightReference: Cesium.HeightReference.NONE,
        disableDepthTestDistance: Number.POSITIVE_INFINITY,
        // LOD：远距离缩小，近距离略放大（保持设备合理比例）
        scaleByDistance: new Cesium.NearFarScalar(
          10, iconSize.scale * 1.0,  // 近距离不再额外放大（原来1.2倍太大）
          200, iconSize.scale * 0.5  // 远距离适度缩小
        )
      },
      description: `
        <div style="padding: 15px; min-width: 220px;">
          <h3 style="margin: 0 0 10px 0; color: #333;">${device.name}</h3>
          <table style="width: 100%; border-collapse: collapse;">
            <tr>
              <td style="padding: 5px 10px; color: #666;">类型</td>
              <td style="padding: 5px 10px; font-weight: bold;">${device.type}</td>
            </tr>
            <tr>
              <td style="padding: 5px 10px; color: #666;">状态</td>
              <td style="padding: 5px 10px;">
                <span style="display: inline-block; padding: 2px 8px; border-radius: 3px; background: ${device.status === 'online' ? '#4CAF50' : device.status === 'fault' ? '#F44336' : '#9E9E9E'}; color: white; font-size: 12px;">
                  ${device.status === 'online' ? '在线' : device.status === 'fault' ? '故障' : '离线'}
                </span>
              </td>
            </tr>
            <tr>
              <td style="padding: 5px 10px; color: #666;">楼层</td>
              <td style="padding: 5px 10px;">${floorNumber}层</td>
            </tr>
            <tr>
              <td style="padding: 5px 10px; color: #666;">高度</td>
              <td style="padding: 5px 10px;">${deviceHeight.toFixed(2)}m</td>
            </tr>
            <tr>
              <td style="padding: 5px 10px; color: #666;">区域密度</td>
              <td style="padding: 5px 10px;">${areaDeviceCount} 个设备</td>
            </tr>
          </table>
        </div>
      `
    }
    
    // 🏷️ 根据LOD决定是否添加标签
    if (showLabel) {
      entityConfig.label = {
        text: device.name,
        font: `${Math.max(8, 12 * iconSize.scale)}pt sans-serif`,
        fillColor: Cesium.Color.WHITE,
        outlineColor: Cesium.Color.BLACK,
        outlineWidth: 2,
        style: Cesium.LabelStyle.FILL_AND_OUTLINE,
        verticalOrigin: Cesium.VerticalOrigin.TOP,
        pixelOffset: new Cesium.Cartesian2(0, 3),
        // 故障设备标签更醒目
        backgroundColor: device.status === 'fault' 
          ? Cesium.Color.fromCssColorString('#F44336').withAlpha(0.7)
          : Cesium.Color.TRANSPARENT,
        backgroundPadding: new Cesium.Cartesian2(4, 2),
        scaleByDistance: new Cesium.NearFarScalar(10, 1.0, 100, 0.5)
      }
    }
    
    viewer.value.entities.add(entityConfig)
    
  } catch (err) {
    console.error('渲染设备失败:', device.name, err)
  }
}

// 渲染建筑3D模型
const renderBuilding3D = async () => {
  if (!viewer.value || !buildingInfo.value) return

  const Cesium = (window as any).Cesium
  
  try {
    // 优先级1: 检查是否有自定义glTF模型URL（需要在数据库中配置）
    if (buildingInfo.value.model_url) {
      await loadGLTFModel(buildingInfo.value.model_url)
      return
    }

    // 优先级2: 解析建筑几何数据
    const geom = buildingInfo.value.geom || buildingInfo.value.geometry
    let geometry: any

    if (typeof geom === 'string') {
      // WKT格式
      geometry = parseWKT(geom)
    } else {
      // GeoJSON格式
      geometry = geom
    }

    // 创建3D建筑实体
    const height = buildingInfo.value.building_height || buildingInfo.value.height || 50
    const floors = buildingInfo.value.total_floors || buildingInfo.value.totalFloors || 10
    
    // 根据楼层数计算高度（如果没有高度信息）
    const buildingHeight = height > 0 ? height : floors * 3.5

    // 添加建筑实体
    viewer.value.entities.add({
      name: buildingInfo.value.name,
      polygon: {
        hierarchy: Cesium.Cartesian3.fromDegreesArray(
          extractCoordinates(geometry)
        ),
        extrudedHeight: buildingHeight,
        material: Cesium.Color.fromCssColorString('#2196F3').withAlpha(0.7),
        outline: true,
        outlineColor: Cesium.Color.WHITE,
        outlineWidth: 2
      },
      description: `
        <div style="padding: 10px;">
          <h3>${buildingInfo.value.name}</h3>
          <p><strong>楼层数：</strong>${floors}</p>
          <p><strong>高度：</strong>${buildingHeight.toFixed(1)}m</p>
          <p><strong>建筑面积：</strong>${buildingInfo.value.building_area || buildingInfo.value.builtArea || 'N/A'}m²</p>
        </div>
      `
    })

  } catch (err) {
    console.error('❌ 渲染3D模型失败:', err)
    showBuildingMarker()
  }
}

// 加载glTF/GLB模型（可选，需要模型文件）
const loadGLTFModel = async (modelUrl: string) => {
  if (!viewer.value || !buildingInfo.value) return

  const Cesium = (window as any).Cesium

  const longitude = buildingInfo.value.longitude || 113.264385
  const latitude = buildingInfo.value.latitude || 23.129163
  const buildingHeight = buildingInfo.value.building_height || buildingInfo.value.height || 35
  const modelScale = buildingInfo.value.model_scale || 1.0

  const position = Cesium.Cartesian3.fromDegrees(longitude, latitude, 0)
  const heading = Cesium.Math.toRadians(buildingInfo.value.model_rotation || 0)
  const pitch = 0
  const roll = 0
  const hpr = new Cesium.HeadingPitchRoll(heading, pitch, roll)
  const orientation = Cesium.Transforms.headingPitchRollQuaternion(position, hpr)

  viewer.value.entities.add({
    name: buildingInfo.value.name,
    position: position,
    orientation: orientation,
    model: {
      uri: modelUrl,
      scale: modelScale,
      minimumPixelSize: 128,
      maximumScale: 20000,
      heightReference: Cesium.HeightReference.CLAMP_TO_GROUND
    },
    description: `
      <div style="padding: 10px;">
        <h3>${buildingInfo.value.name}</h3>
        <p><strong>编码：</strong>${buildingInfo.value.code || 'N/A'}</p>
        <p><strong>楼层数：</strong>${buildingInfo.value.total_floors || buildingInfo.value.totalFloors || 'N/A'}</p>
        <p><strong>高度：</strong>${buildingHeight.toFixed(1)}m</p>
        <p><strong>类型：</strong>3D模型</p>
      </div>
    `
  })

  // 添加建筑标签
  viewer.value.entities.add({
    position: Cesium.Cartesian3.fromDegrees(longitude, latitude, buildingHeight + 5),
    label: {
      text: buildingInfo.value.name,
      font: 'bold 16pt sans-serif',
      fillColor: Cesium.Color.WHITE,
      outlineColor: Cesium.Color.BLACK,
      outlineWidth: 3,
      style: Cesium.LabelStyle.FILL_AND_OUTLINE,
      verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
      pixelOffset: new Cesium.Cartesian2(0, -10)
    }
  })

  ElMessage.success(`已加载3D模型: ${buildingInfo.value.name}`)
}

// 显示建筑标记（程序化生成逼真建筑模型）
const showBuildingMarker = async () => {
  if (!viewer.value || !buildingInfo.value) return

  const Cesium = (window as any).Cesium

  // 建筑基本数据
  const longitude = buildingInfo.value.longitude || 113.264385
  const latitude = buildingInfo.value.latitude || 23.129163
  const totalFloors = buildingInfo.value.total_floors || buildingInfo.value.totalFloors || 10
  const buildingHeight = buildingInfo.value.building_height || buildingInfo.value.height || totalFloors * 3.5
  const buildingArea = buildingInfo.value.building_area || buildingInfo.value.builtArea || 4800
  const buildingType = buildingInfo.value.building_type || 'office'

  // 根据面积计算底面尺寸（假设矩形）
  const baseWidth = Math.sqrt(buildingArea * 1.2) // 宽度
  const baseDepth = Math.sqrt(buildingArea / 1.2)  // 深度
  const floorHeight = buildingHeight / totalFloors

  // 根据建筑类型选择颜色
  const typeColors = {
    'office': '#2196F3',        // 蓝色 - 办公楼
    'production': '#FF9800',    // 橙色 - 生产楼
    'residential': '#4CAF50',   // 绿色 - 住宅
    'warehouse': '#9E9E9E',     // 灰色 - 仓库
    'parking': '#607D8B'        // 青灰色 - 停车楼
  }
  const buildingColor = typeColors[buildingType] || '#2196F3'

  // 1. 创建建筑主体
  viewer.value.entities.add({
    name: buildingInfo.value.name,
    position: Cesium.Cartesian3.fromDegrees(longitude, latitude, buildingHeight / 2),
    box: {
      dimensions: new Cesium.Cartesian3(baseWidth, baseDepth, buildingHeight),
      material: Cesium.Color.fromCssColorString(buildingColor).withAlpha(0.8),
      outline: true,
      outlineColor: Cesium.Color.WHITE,
      outlineWidth: 2
    },
    description: `
      <div style="padding: 10px;">
        <h3>${buildingInfo.value.name}</h3>
        <p><strong>编码：</strong>${buildingInfo.value.code || 'N/A'}</p>
        <p><strong>楼层数：</strong>${totalFloors}</p>
        <p><strong>高度：</strong>${buildingHeight.toFixed(1)}m</p>
        <p><strong>建筑面积：</strong>${buildingArea}m²</p>
      </div>
    `
  })

  // 2. 添加楼层分割线（增强立体感）
  for (let i = 1; i < totalFloors; i++) {
    const height = i * floorHeight
    const positions = [
      // 前面
      longitude - baseWidth / 222640, latitude - baseDepth / 222640, height,
      longitude + baseWidth / 222640, latitude - baseDepth / 222640, height,
      // 右面
      longitude + baseWidth / 222640, latitude - baseDepth / 222640, height,
      longitude + baseWidth / 222640, latitude + baseDepth / 222640, height,
      // 后面
      longitude + baseWidth / 222640, latitude + baseDepth / 222640, height,
      longitude - baseWidth / 222640, latitude + baseDepth / 222640, height,
      // 左面
      longitude - baseWidth / 222640, latitude + baseDepth / 222640, height,
      longitude - baseWidth / 222640, latitude - baseDepth / 222640, height,
    ]
    
    viewer.value.entities.add({
      polyline: {
        positions: Cesium.Cartesian3.fromDegreesArrayHeights(positions),
        width: 1.5,
        material: new Cesium.PolylineDashMaterialProperty({
          color: Cesium.Color.WHITE.withAlpha(0.6),
          dashLength: 8.0
        })
      }
    })
  }

  // 3. 添加窗户效果（使用小方块模拟）
  const windowSpacing = 4
  const floorsToShow = Math.min(totalFloors, 5) // 只显示部分楼层的窗户（性能考虑）
  
  for (let floor = 0; floor < floorsToShow; floor++) {
    const floorLevel = (floor + 0.5) * floorHeight
    const windowsPerSide = Math.floor(baseWidth / windowSpacing)
    
    // 只在正面添加窗户效果
    for (let w = 0; w < windowsPerSide; w++) {
      const offsetX = (w - windowsPerSide / 2) * windowSpacing / 111320
      viewer.value.entities.add({
        position: Cesium.Cartesian3.fromDegrees(
          longitude + offsetX,
          latitude - baseDepth / 222640,
          floorLevel
        ),
        point: {
          pixelSize: 3,
          color: Cesium.Color.CYAN.withAlpha(0.8),
          outlineColor: Cesium.Color.WHITE,
          outlineWidth: 1
        }
      })
    }
  }

  // 4. 添加建筑标签
  viewer.value.entities.add({
    position: Cesium.Cartesian3.fromDegrees(longitude, latitude, buildingHeight + 5),
    label: {
      text: buildingInfo.value.name,
      font: 'bold 16pt sans-serif',
      fillColor: Cesium.Color.WHITE,
      outlineColor: Cesium.Color.BLACK,
      outlineWidth: 3,
      style: Cesium.LabelStyle.FILL_AND_OUTLINE,
      verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
      pixelOffset: new Cesium.Cartesian2(0, -10),
      distanceDisplayCondition: new Cesium.DistanceDisplayCondition(0, 5000)
    }
  })

  // 5. 添加建筑类型标签
  viewer.value.entities.add({
    position: Cesium.Cartesian3.fromDegrees(longitude, latitude, buildingHeight + 2),
    label: {
      text: `${totalFloors}层 | ${buildingHeight.toFixed(0)}m`,
      font: '10pt sans-serif',
      fillColor: Cesium.Color.LIGHTGRAY,
      outlineColor: Cesium.Color.BLACK,
      outlineWidth: 2,
      style: Cesium.LabelStyle.FILL_AND_OUTLINE,
      verticalOrigin: Cesium.VerticalOrigin.TOP,
      pixelOffset: new Cesium.Cartesian2(0, 5)
    }
  })
}

// 飞到建筑位置
const flyToBuilding = async () => {
  if (!viewer.value) return

  const Cesium = (window as any).Cesium

  const longitude = buildingInfo.value?.longitude || 113.264385
  const latitude = buildingInfo.value?.latitude || 23.129163
  
  // 🏠 室内视图：近距离俯视角度
  if (showIndoorView.value && floors.value.length > 0) {
    const floorHeight = 3.5
    // 📍 重要修复：计算当前楼层的实际高度
    const currentHeight = (currentFloor.value - 1) * floorHeight  // 楼层从1开始，但高度从0开始
    
    // 📍 相机位置：在当前楼层上方30-40米（更合适的距离）
    const cameraHeight = currentHeight + 35
    
    // 📍 使用更优的相机角度（较低俯视角，避免看到顶部）
    viewer.value.camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(longitude, latitude, cameraHeight),
      orientation: {
        heading: Cesium.Math.toRadians(0),      // 正北
        pitch: Cesium.Math.toRadians(-45),      // 45度俯视（避免看到太多顶面）
        roll: 0.0
      },
      duration: 1.5,  // 稍快的飞行
      complete: function() {
        console.log(`✅ 相机已定位到楼层 ${currentFloor.value}，高度: ${currentHeight}m，相机高度: ${cameraHeight}m`)
      }
    })
  } else {
    // 🏢 建筑外观：较远的视角
    const height = (buildingInfo.value?.building_height || buildingInfo.value?.height || 50) * 2.5

    viewer.value.camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(longitude, latitude, height),
      orientation: {
        heading: Cesium.Math.toRadians(0),
        pitch: Cesium.Math.toRadians(-45),
        roll: 0.0
      },
      duration: 2
    })
  }
}

// 重置视角
const resetView = () => {
  if (focusedArea.value) {
    exitAreaFocus()
  } else {
    flyToBuilding()
  }
}

// 📱 显示设备详情对话框
const showDeviceDetailDialog = (entity: any) => {
  const deviceId = entity.properties?.deviceId?.getValue()
  const deviceName = entity.properties?.deviceName?.getValue()
  const deviceType = entity.properties?.deviceType?.getValue()
  const deviceStatus = entity.properties?.deviceStatus?.getValue()
  
  const statusText = deviceStatus === 'online' ? '在线' : deviceStatus === 'fault' ? '故障' : '离线'
  const statusColor = deviceStatus === 'online' ? '#4CAF50' : deviceStatus === 'fault' ? '#F44336' : '#9E9E9E'
  
  // 使用Cesium内置的信息窗口显示设备详情
  if (entity.description) {
    // Cesium会自动显示description
    console.log(`📱 点击设备: ${deviceName} (${deviceType}) - ${statusText}`)
  }
  
  // 也可以使用ElMessageBox显示更详细的信息
  ElMessageBox.alert(
    `<div style="padding: 15px;">
      <h3 style="margin: 0 0 10px 0; color: #333;">${deviceName}</h3>
      <table style="width: 100%; border-collapse: collapse;">
        <tr>
          <td style="padding: 5px 10px; color: #666;">设备ID</td>
          <td style="padding: 5px 10px; font-weight: bold;">${deviceId}</td>
        </tr>
        <tr>
          <td style="padding: 5px 10px; color: #666;">类型</td>
          <td style="padding: 5px 10px; font-weight: bold;">${deviceType}</td>
        </tr>
        <tr>
          <td style="padding: 5px 10px; color: #666;">状态</td>
          <td style="padding: 5px 10px;">
            <span style="display: inline-block; padding: 2px 8px; border-radius: 3px; background: ${statusColor}; color: white; font-size: 12px;">
              ${statusText}
            </span>
          </td>
        </tr>
      </table>
    </div>`,
    '设备详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确定'
    }
  )
}

// 🎯 聚焦到指定区域（房间）
const focusOnArea = async (area: any) => {
  if (!viewer.value) return
  
  console.log(`🎯 聚焦房间: ${area.name}`, area)
  focusedArea.value = area
  
  const Cesium = (window as any).Cesium
  
  // 计算房间中心点
  const geometry = area.geometry
  if (!geometry || !geometry.coordinates || !geometry.coordinates[0]) return
  
  const coords = geometry.coordinates[0]
  const centerX = coords.reduce((sum: number, coord: any[]) => sum + coord[0], 0) / coords.length
  const centerY = coords.reduce((sum: number, coord: any[]) => sum + coord[1], 0) / coords.length
  
  // 转换为经纬度
  const baseLon = buildingInfo.value?.longitude || 113.264385
  const baseLat = buildingInfo.value?.latitude || 23.129163
  const latRadians = baseLat * Math.PI / 180
  const metersPerDegreeLon = 111320 * Math.cos(latRadians)
  const metersPerDegreeLat = 111320
  
  const centerLon = baseLon + (centerX / metersPerDegreeLon)
  const centerLat = baseLat + (centerY / metersPerDegreeLat)
  
  // 计算房间地板高度
  const floorHeight = 3.5
  const floorBaseHeight = (currentFloor.value - 1) * floorHeight
  
  // 🎯 优化视角：提供更真实的"进入房间"体验
  // 选项1：人眼高度（1.6m）- 最真实，但可能看不全设备
  // 选项2：中等高度（4m）- 折中方案，既能看到设备又不太高
  // const eyeHeight = 1.6  // 人眼高度（备选）
  const mediumHeight = 4.0  // 中等高度
  const cameraHeight = floorBaseHeight + mediumHeight  // 使用中等高度
  
  // 飞入房间
  viewer.value.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(centerLon, centerLat, cameraHeight),
    orientation: {
      heading: Cesium.Math.toRadians(0),
      pitch: Cesium.Math.toRadians(-20),  // 20度俯视（接近平视，可以看到设备）
      roll: 0.0
    },
    duration: 1.5,
    complete: function() {
      console.log(`✅ 已进入房间: ${area.name}`)
      ElMessage.success(`已进入 ${area.name}`)
      
      // 高亮显示当前房间的设备
      highlightAreaDevices(area.id)
    }
  })
}

// 🔙 退出区域聚焦
const exitAreaFocus = () => {
  if (!focusedArea.value) return
  
  console.log(`🔙 退出房间: ${focusedArea.value.name}`)
  focusedArea.value = null
  
  // 恢复所有设备显示
  restoreAllDevices()
  
  // 返回楼层视图
  flyToBuilding()
  ElMessage.success('已返回楼层视图')
}

// 🎨 高亮显示指定区域（隐藏其他房间）
const highlightAreaDevices = (areaId: number) => {
  if (!viewer.value) return
  
  const Cesium = (window as any).Cesium
  
  let currentRoomDeviceCount = 0
  let hiddenDeviceCount = 0
  let hiddenRoomCount = 0
  
  // 遍历所有实体
  const entities = viewer.value.entities.values
  entities.forEach((entity: any) => {
    // 处理设备
    if (entity.billboard) {
      const deviceAreaId = entity.properties?.deviceAreaId?.getValue()
      
      if (deviceAreaId === areaId) {
        // 当前房间设备：显示并适度放大（避免设备显得过大）
        entity.show = true
        entity.billboard.scale = new Cesium.ConstantProperty(1.5)  // 改为1.5倍（原来2.5倍太大）
        currentRoomDeviceCount++
      } else {
        // 其他房间设备：直接隐藏
        entity.show = false
        hiddenDeviceCount++
      }
    }
    
    // 处理房间区域
    if (entity.polygon && entity.id && entity.id.startsWith('area-')) {
      const entityAreaId = entity.properties?.areaId?.getValue()
      
      if (entityAreaId === areaId) {
        // 当前房间：保持显示，增强边框
        entity.show = true
        entity.polygon.outlineWidth = new Cesium.ConstantProperty(5)  // 加粗边框
        entity.polygon.material = new Cesium.ColorMaterialProperty(
          Cesium.Color.fromCssColorString('#2196F3').withAlpha(0.85)  // 增强不透明度
        )
      } else {
        // 其他房间：直接隐藏
        entity.show = false
        hiddenRoomCount++
      }
    }
  })
  
  console.log(`✨ 聚焦模式：显示 ${currentRoomDeviceCount} 个设备，隐藏 ${hiddenRoomCount} 个房间和 ${hiddenDeviceCount} 个设备`)
}

// 🔄 恢复所有房间和设备显示
const restoreAllDevices = () => {
  if (!viewer.value) return
  
  const Cesium = (window as any).Cesium
  
  let restoredRoomCount = 0
  let restoredDeviceCount = 0
  
  // 遍历所有实体，恢复原始状态
  const entities = viewer.value.entities.values
  entities.forEach((entity: any) => {
    // 恢复设备显示
    if (entity.billboard) {
      entity.show = true  // 显示所有设备
      entity.billboard.scale = new Cesium.ConstantProperty(1.0)  // 恢复正常大小
      entity.billboard.color = new Cesium.ConstantProperty(Cesium.Color.WHITE)  // 恢复颜色
      restoredDeviceCount++
    }
    
    // 恢复房间显示
    if (entity.polygon && entity.id && entity.id.startsWith('area-')) {
      entity.show = true  // 显示所有房间
      entity.polygon.outlineWidth = new Cesium.ConstantProperty(3)  // 恢复边框宽度
      
      // 恢复原始材质颜色（从properties中获取）
      const areaData = entity.properties?.areaData?.getValue()
      if (areaData) {
        const fillColor = areaData.fill_color || '#E3F2FD'
        entity.polygon.material = new Cesium.ColorMaterialProperty(
          Cesium.Color.fromCssColorString(fillColor).withAlpha(0.7)
        )
      }
      restoredRoomCount++
    }
  })
  
  console.log(`🔄 已恢复显示：${restoredRoomCount} 个房间，${restoredDeviceCount} 个设备`)
}

// 重试加载
const retryLoad = () => {
  initCesium()
}

// 切换图层
const toggleBuildingLayer = (show: boolean) => {
  if (!viewer.value) return
  viewer.value.entities.show = show
}

const toggleTerrainLayer = async (show: boolean) => {
  if (!viewer.value) return
  const Cesium = (window as any).Cesium
  // 🔧 不使用Ion的世界地形，始终使用椭球体
  viewer.value.terrainProvider = new Cesium.EllipsoidTerrainProvider()
  ElMessage.info(show ? '地形已启用（椭球体模式）' : '地形已禁用')
}

const toggleDeviceLayer = (show: boolean) => {
  // TODO: 实现设备图层切换
  console.log('切换设备图层:', show)
}

// 🔄 切换楼层
const switchFloor = async (floorNumber: number) => {
  console.log(`切换到楼层 ${floorNumber}`)
  
  // 清除当前场景中的实体
  if (viewer.value) {
    viewer.value.entities.removeAll()
  }
  
  // 重新加载楼层数据
  await loadFloorData(floorNumber)
  await renderIndoor3D()
  
  // 重新调整视角
  flyToBuilding()
  
  ElMessage.success(`已切换到 ${floorNumber} 层`)
}

// 🔄 切换视图模式（室内/外观）
const toggleViewMode = async () => {
  showIndoorView.value = !showIndoorView.value
  
  // 清除当前场景
  if (viewer.value) {
    viewer.value.entities.removeAll()
  }
  
  // 重新渲染
  if (showIndoorView.value && floors.value.length > 0) {
    await loadFloorData(currentFloor.value)
    await renderIndoor3D()
    ElMessage.success('已切换到室内视图模式')
  } else {
    if (buildingInfo.value.geom || buildingInfo.value.geometry) {
      await renderBuilding3D()
    } else {
      showBuildingMarker()
    }
    ElMessage.success('已切换到建筑外观模式')
  }
  
  // 重新调整视角
  flyToBuilding()
}

// 辅助函数：提取坐标
const extractCoordinates = (geometry: any): number[] => {
  if (!geometry) return []
  
  if (geometry.type === 'Polygon' && geometry.coordinates) {
    // GeoJSON Polygon
    return geometry.coordinates[0].flat()
  } else if (geometry.type === 'Point' && geometry.coordinates) {
    // GeoJSON Point
    return geometry.coordinates
  }
  
  return []
}

// 辅助函数：解析WKT
const parseWKT = (wkt: string): any => {
  // 简单的WKT解析（仅支持POLYGON）
  const match = wkt.match(/POLYGON\s*\(\((.*?)\)\)/)
  if (match) {
    const coords = match[1].split(',').map(coord => {
      const [lon, lat] = coord.trim().split(' ')
      return [parseFloat(lon), parseFloat(lat)]
    })
    return {
      type: 'Polygon',
      coordinates: [coords]
    }
  }
  return null
}

onMounted(() => {
  initCesium()
})

onBeforeUnmount(() => {
  if (viewer.value) {
    viewer.value.destroy()
    viewer.value = null
  }
})
</script>

<style scoped lang="scss">
.cesium-3d-view {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;

  .viewer-header {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 12px 20px;
    background: white;
    border-bottom: 1px solid #e4e7ed;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
    z-index: 1000;

    .building-info {
      flex: 1;
      
      h3 {
        margin: 0;
        font-size: 18px;
        color: #303133;
      }

      .building-desc {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .cesium-container {
    flex: 1;
    position: relative;

    .loading-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(255, 255, 255, 0.9);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      z-index: 2000;
      gap: 16px;

      p {
        font-size: 16px;
        color: #606266;
      }
    }

    .error-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: white;
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 2000;
    }
  }

  .layer-control {
    position: absolute;
    top: 80px;
    right: 20px;
    z-index: 1001;
    width: 200px;

    .el-checkbox {
      display: block;
      margin: 8px 0;
    }
  }

  .info-panel {
    position: absolute;
    bottom: 20px;
    left: 20px;
    z-index: 1001;
    width: 300px;
  }
}
</style>

