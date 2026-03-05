<template>
  <div class="building-floor-plan">
    <!-- 头部：返回按钮 + 建筑信息 -->
    <div class="plan-header">
      <el-button @click="backToMap" icon="ArrowLeft" type="primary" plain>
        返回地图
      </el-button>
      <div class="building-info">
        <h3>{{ buildingName }}</h3>
        <span class="building-desc">建筑平面图</span>
      </div>
      <div class="floor-selector" v-if="floors.length > 0">
        <el-button-group>
          <el-button
            v-for="floor in floors"
            :key="floor.id"
            :type="currentFloor === floor.floor_number ? 'primary' : ''"
            @click="switchFloor(floor.floor_number)"
            size="small"
          >
            {{ floor.floor_number > 0 ? floor.floor_number + 'F' : 'B' + Math.abs(floor.floor_number) }}
          </el-button>
        </el-button-group>
      </div>
      <div class="floor-selector" v-else>
        <el-tag type="info" size="large">暂无楼层</el-tag>
      </div>
    </div>

    <!-- 平面图容器 -->
    <div class="plan-container" ref="planContainer">
      <!-- 🔧 建筑完全无楼层时显示空状态 -->
      <el-empty 
        v-if="floors.length === 0" 
        description="该建筑暂无楼层数据"
        :image-size="200"
        style="height: 100%; display: flex; align-items: center; justify-content: center;"
      >
        <template #description>
          <div style="margin-top: 20px;">
            <p style="font-size: 16px; color: #606266; margin-bottom: 10px;">该建筑暂无楼层数据</p>
            <p style="font-size: 14px; color: #909399;">请先在系统中配置该建筑的楼层信息</p>
          </div>
        </template>
      </el-empty>

      <!-- 有楼层时显示地图（即使楼层内无数据） -->
      <template v-else>
        <div :id="planMapId" class="plan-map" :style="{ width: '100%', height: planHeight }">
          <!-- 🔧 楼层有但内容为空时的提示 -->
          <div v-if="rooms.length === 0 && devices.length === 0" class="floor-empty-hint">
            <el-empty 
              description="当前楼层暂无区域和设备数据"
              :image-size="120"
            >
              <template #description>
                <div>
                  <p style="font-size: 14px; color: #606266;">当前楼层暂无区域和设备数据</p>
                  <p style="font-size: 12px; color: #909399; margin-top: 8px;">可以在系统中为该楼层添加区域和设备</p>
                </div>
              </template>
            </el-empty>
          </div>
        </div>
        
        <!-- 图例 -->
        <div class="plan-legend">
          <div class="legend-item">
            <span class="legend-color room-normal"></span>
            <span>普通房间</span>
          </div>
          <div class="legend-item">
            <span class="legend-color room-selected"></span>
            <span>选中房间</span>
          </div>
          <div class="legend-item">
            <span class="legend-color device-point"></span>
            <span>设备位置</span>
          </div>
        </div>
      </template>
    </div>

    <!-- 房间详情侧边栏 -->
    <el-drawer
      v-model="roomDrawerVisible"
      :title="selectedRoom.name"
      size="400px"
      direction="rtl"
    >
      <template v-if="selectedRoom.data">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="区域编号">
            {{ selectedRoom.data.code || selectedRoom.data.id }}
          </el-descriptions-item>
          <el-descriptions-item label="区域名称">
            {{ selectedRoom.data.name }}
          </el-descriptions-item>
          <el-descriptions-item label="区域类型">
            {{ getAreaTypeLabel(selectedRoom.data.type) }}
          </el-descriptions-item>
          <el-descriptions-item label="子类型" v-if="selectedRoom.data.sub_type">
            {{ getAreaSubTypeLabel(selectedRoom.data.sub_type) }}
          </el-descriptions-item>
          <el-descriptions-item label="面积">
            {{ selectedRoom.data.area_sqm || selectedRoom.data.area || 'N/A' }} m²
          </el-descriptions-item>
          <el-descriptions-item label="设备数量">
            <el-tag type="primary">{{ selectedRoom.deviceCount }} 个</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 区域内设备列表 -->
        <div class="device-list" v-if="selectedRoom.devices && selectedRoom.devices.length > 0">
          <h4 style="margin-top: 20px; margin-bottom: 12px;">区域内设备</h4>
          <el-table :data="selectedRoom.devices" stripe size="small" max-height="400">
            <el-table-column prop="name" label="设备名称" width="140" />
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag 
                  :type="row.status === 'online' ? 'success' : row.status === 'offline' ? 'info' : 'danger'"
                  size="small"
                >
                  {{ row.status === 'online' ? '在线' : row.status === 'offline' ? '离线' : '故障' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else description="该区域暂无设备" :image-size="80" style="margin-top: 20px;" />
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import Map from 'ol/Map'
import View from 'ol/View'
import { Tile as TileLayer, Vector as VectorLayer } from 'ol/layer'
import { XYZ, Vector as VectorSource } from 'ol/source'
import { Feature } from 'ol'
import { Polygon, Point } from 'ol/geom'
import { Style, Fill, Stroke, Circle as CircleStyle, Text, Icon } from 'ol/style'
import { defaults as defaultControls } from 'ol/control'
import * as GisApi from '@/api/iot/gis' // 引入GIS API
import { ElMessage, ElMessageBox } from 'element-plus'

interface Props {
  buildingId: number
  buildingName?: string
  planHeight?: string
}

const props = withDefaults(defineProps<Props>(), {
  buildingName: '建筑平面图',
  planHeight: '700px'
})

const emit = defineEmits(['back'])

// 地图相关
const planMapId = `plan-map-${Date.now()}`
const planContainer = ref<HTMLElement>()
let map: Map | null = null

// 楼层数据
const floors = ref<any[]>([])
const currentFloor = ref(1)

// 房间数据
const rooms = ref<any[]>([])
const devices = ref<any[]>([])

// 选中的房间
const roomDrawerVisible = ref(false)
const selectedRoom = ref<any>({
  name: '',
  data: null,
  devices: [],
  deviceCount: 0
})

// 🎨 区域类型标签映射
const getAreaTypeLabel = (type: string): string => {
  const typeMap: Record<string, string> = {
    'ROOM': '房间',
    'CORRIDOR': '走廊',
    'ELEVATOR': '电梯',
    'STAIRCASE': '楼梯',
    'LOBBY': '大堂',
    'RESTROOM': '卫生间',
    'STORAGE': '储藏室',
    'PARKING': '停车位'
  }
  return typeMap[type] || type
}

// 🎨 区域子类型标签映射
const getAreaSubTypeLabel = (subType: string): string => {
  const subTypeMap: Record<string, string> = {
    'meeting_room': '会议室',
    'office': '办公室',
    'workspace': '工作区',
    'server_room': '机房',
    'break_room': '休息室',
    'reception': '接待区',
    'kitchen': '茶水间'
  }
  return subTypeMap[subType] || subType
}

// 图层
let roomLayer: VectorLayer<any> | null = null
let deviceLayer: VectorLayer<any> | null = null

// 返回地图
const backToMap = () => {
  emit('back')
}

// 切换楼层
const switchFloor = async (floorNumber: number) => {
  currentFloor.value = floorNumber
  await loadFloorData(floorNumber)
  renderFloorPlan()
}

// 加载楼层数据
const loadFloorData = async (floorNumber: number) => {
  try {
    // 1. 找到对应楼层的ID
    const floor = floors.value.find(f => f.floor_number === floorNumber)
    if (!floor) {
      console.error(`❌ 未找到楼层号 ${floorNumber} 的数据`)
      ElMessage.error(`未找到 ${floorNumber} 层的数据`)
      return
    }
    
    console.log(`🔄 加载楼层数据: ${floor.name} (ID: ${floor.id})`)
    
    // 2. 调用真实 API 获取楼层可视化数据
    let rawData = await GisApi.getFloorVisualizationData(floor.id)
    
    console.log(`✅ API原始返回:`, typeof rawData, rawData)
    
    // 🔧 修复：后端返回的是JSON字符串，需要解析
    let data: any
    if (typeof rawData === 'string') {
      console.log(`🔄 检测到JSON字符串，正在解析...`)
      data = JSON.parse(rawData)
    } else {
      data = rawData
    }
    
    console.log(`✅ 解析后的数据:`, data)
    console.log(`🔍 data.areas存在:`, !!data?.areas)
    console.log(`🔍 data.areas是数组:`, Array.isArray(data?.areas))
    console.log(`🔍 data.areas长度:`, data?.areas?.length)
    console.log(`🔍 data.devices长度:`, data?.devices?.length)
    
    if (!data) {
      throw new Error('API返回数据为空')
    }
    
    // 3. 处理区域数据（Area）
    if (data.areas && Array.isArray(data.areas)) {
      console.log(`🔍 原始areas数据:`, data.areas.slice(0, 2))  // 只打印前2个
      
      rooms.value = data.areas.map(area => {
        const coords = parseGeometry(area.geom || area.geometry)
        console.log(`🔍 区域 ${area.name} 坐标:`, coords.slice(0, 3))  // 只打印前3个点
        
        return {
          id: area.id,
          name: area.name || `区域${area.id}`,
          type: area.type || area.area_type || area.areaType || 'ROOM',  // 修复：添加 area.type
          coordinates: coords,
          properties: {
            floor_id: area.floor_id || area.floorId,
            area: area.area_sqm || area.area,
            capacity: area.capacity,
            status: area.status,
            fill_color: area.fill_color || area.fillColor,
            stroke_color: area.stroke_color || area.strokeColor,
            opacity: area.opacity
          }
        }
      })
      console.log(`✅ 加载了 ${rooms.value.length} 个区域`)
    } else {
      console.warn('⚠️  没有区域数据')
      rooms.value = []
    }
    
    // 4. 处理设备数据（Device）
    if (data.devices && Array.isArray(data.devices)) {
      devices.value = data.devices.map(device => ({
        id: device.id,
        name: device.name || `设备${device.id}`,
        type: device.type || device.device_type || 'unknown',
        status: device.status || 'online',
        coordinates: parseDeviceGeometry(device.geom || device.geometry),
        properties: {
          code: device.code,
          area_id: device.area_id || device.areaId,
          manufacturer: device.manufacturer,
          model: device.model
        }
      }))
      console.log(`✅ 加载了 ${devices.value.length} 个设备`)
    } else {
      console.warn('⚠️  没有设备数据')
      devices.value = []
    }
    
  } catch (error) {
    console.error('❌ 加载楼层数据失败:', error)
    ElMessage.error({
      message: `加载楼层数据失败: ${error.message}`,
      duration: 3000
    })
    
    // 降级方案：使用空数据
    rooms.value = []
    devices.value = []
  }
}

// 解析几何数据（POLYGON 或 GeoJSON）
const parseGeometry = (geom: any): number[][] => {
  if (!geom) return []
  
  try {
    // 如果是字符串，尝试解析为GeoJSON
    if (typeof geom === 'string') {
      // WKT格式: POLYGON((x1 y1, x2 y2, ...))
      if (geom.startsWith('POLYGON')) {
        const coordsStr = geom.match(/\(\(([^)]+)\)\)/)?.[1]
        if (coordsStr) {
          return coordsStr.split(',').map(pair => {
            const [x, y] = pair.trim().split(' ').map(Number)
            return [x, y]
          })
        }
      }
      
      // 尝试解析为JSON
      const parsed = JSON.parse(geom)
      if (parsed.type === 'Polygon' && parsed.coordinates) {
        return parsed.coordinates[0]
      }
    }
    
    // 如果是对象（GeoJSON）
    if (typeof geom === 'object') {
      if (geom.type === 'Polygon' && geom.coordinates) {
        return geom.coordinates[0]
      }
    }
    
    console.warn('⚠️  无法解析几何数据:', geom)
    return []
  } catch (e) {
    console.error('❌ 解析几何数据失败:', e, geom)
    return []
  }
}

// 解析设备几何数据（POINT 或 GeoJSON）
const parseDeviceGeometry = (geom: any): [number, number] => {
  if (!geom) return [0, 0]
  
  try {
    // 如果是字符串，尝试解析
    if (typeof geom === 'string') {
      // WKT格式: POINT(x y)
      if (geom.startsWith('POINT')) {
        const coordsStr = geom.match(/\(([^)]+)\)/)?.[1]
        if (coordsStr) {
          const [x, y] = coordsStr.trim().split(' ').map(Number)
          return [x, y]
        }
      }
      
      // 尝试解析为JSON
      const parsed = JSON.parse(geom)
      if (parsed.type === 'Point' && parsed.coordinates) {
        return [parsed.coordinates[0], parsed.coordinates[1]]
      }
    }
    
    // 如果是对象（GeoJSON）
    if (typeof geom === 'object') {
      if (geom.type === 'Point' && geom.coordinates) {
        return [geom.coordinates[0], geom.coordinates[1]]
      }
    }
    
    console.warn('⚠️  无法解析设备坐标:', geom)
    return [0, 0]
  } catch (e) {
    console.error('❌ 解析设备坐标失败:', e, geom)
    return [0, 0]
  }
}

// 生成模拟房间数据（使用虚拟坐标，不是真实地理坐标）
const generateMockRooms = (floorNumber: number) => {
  const mockRooms = []
  const roomsPerRow = 5
  const roomsPerCol = 4
  const roomWidth = 1  // 虚拟坐标单位（放大比例）
  const roomHeight = 1
  const startLon = 0  // 起始坐标（虚拟）
  const startLat = 0  // 起始坐标（虚拟）

  for (let row = 0; row < roomsPerCol; row++) {
    for (let col = 0; col < roomsPerRow; col++) {
      const roomNumber = row * roomsPerRow + col + 1
      const minLon = startLon + col * roomWidth
      const maxLon = minLon + roomWidth * 0.9  // 留出走廊空间
      const minLat = startLat + row * roomHeight
      const maxLat = minLat + roomHeight * 0.9

      mockRooms.push({
        id: floorNumber * 100 + roomNumber,
        code: `${floorNumber}0${roomNumber}`,
        name: `${floorNumber}层${roomNumber}号房间`,
        floor_number: floorNumber,
        room_type: ['办公室', '会议室', '机房', '储藏室', '休息区'][Math.floor(Math.random() * 5)],
        area: 50 + Math.random() * 50,
        coordinates: [
          [minLon, minLat],
          [maxLon, minLat],
          [maxLon, maxLat],
          [minLon, maxLat],
          [minLon, minLat]
        ]
      })
    }
  }

  return mockRooms
}

// 生成模拟设备数据
const generateMockDevices = (roomsList: any[]) => {
  const mockDevices: any[] = []
  
  roomsList.forEach(room => {
    const deviceCount = Math.floor(Math.random() * 5) + 1
    for (let i = 0; i < deviceCount; i++) {
      const coords = room.coordinates[0]  // 房间左下角
      const roomWidth = room.coordinates[1][0] - coords[0]
      const roomHeight = room.coordinates[2][1] - coords[1]
      
      mockDevices.push({
        id: room.id * 10 + i,
        name: `设备-${room.code}-${i + 1}`,
        device_type: ['空调', '照明', '门禁', '监控', '传感器'][Math.floor(Math.random() * 5)],
        status: Math.random() > 0.2 ? '在线' : '离线',
        room_id: room.id,
        lon: coords[0] + roomWidth * (0.2 + Math.random() * 0.6),
        lat: coords[1] + roomHeight * (0.2 + Math.random() * 0.6)
      })
    }
  })

  return mockDevices
}

// 初始化平面图
const initFloorPlan = () => {
  // 创建底图（使用高德地图瓦片，国内访问更稳定）
  const baseLayer = new TileLayer({
    source: new XYZ({
      url: 'https://webrd0{1-4}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}',
      crossOrigin: 'anonymous'
    }),
    opacity: 0.1  // 非常淡的底图，只作为参考
  })

  // 创建房间矢量层
  const roomSource = new VectorSource()
  roomLayer = new VectorLayer({
    source: roomSource,
    style: (feature) => {
      const isSelected = feature.get('selected')
      const properties = feature.get('properties') || {}
      
      // 🎨 使用从API返回的颜色
      let fillColor = 'rgba(33, 150, 243, 0.3)' // 默认蓝色
      if (properties.fill_color && properties.opacity) {
        // 将十六进制颜色转换为rgba
        const hex = properties.fill_color.replace('#', '')
        const r = parseInt(hex.substr(0, 2), 16)
        const g = parseInt(hex.substr(2, 2), 16)
        const b = parseInt(hex.substr(4, 2), 16)
        fillColor = `rgba(${r}, ${g}, ${b}, ${properties.opacity})`
      }
      
      if (isSelected) {
        fillColor = 'rgba(255, 193, 7, 0.8)' // 选中时黄色高亮
      }
      
      const strokeColor = isSelected ? '#FFC107' : (properties.stroke_color || '#2196F3')
      
      return new Style({
        fill: new Fill({
          color: fillColor
        }),
        stroke: new Stroke({
          color: strokeColor,
          width: 2
        }),
        text: new Text({
          text: feature.get('name'),
          font: '12px sans-serif',
          fill: new Fill({ color: '#333' }),
          stroke: new Stroke({ color: '#fff', width: 2 })
        })
      })
    },
    zIndex: 1
  })

  // 创建设备矢量层
  const deviceSource = new VectorSource()
  deviceLayer = new VectorLayer({
    source: deviceSource,
    style: (feature) => {
      const status = feature.get('status') || 'online'
      
      // 🎨 根据设备状态显示不同颜色
      let color = '#4CAF50' // 默认绿色（在线）
      if (status === 'offline') {
        color = '#9E9E9E' // 灰色（离线）
      } else if (status === 'fault') {
        color = '#F44336' // 红色（故障）
      }
      
      return new Style({
        image: new CircleStyle({
          radius: 5,
          fill: new Fill({ color: color }),
          stroke: new Stroke({ color: '#fff', width: 1.5 })
        })
      })
    },
    zIndex: 2
  })

  // 创建地图（使用虚拟坐标系统，不需要投影转换）
  map = new Map({
    target: planMapId,
    layers: [baseLayer, roomLayer, deviceLayer],
    view: new View({
      center: [0, 0],  // 虚拟中心点（会在加载数据后自动调整）
      zoom: 3,  // 初始缩放级别
      minZoom: 1,
      maxZoom: 10,
      projection: 'EPSG:4326'  // 使用 WGS84，但作为虚拟坐标
    }),
    controls: defaultControls({
      attribution: false,
      zoom: true
    })
  })

  // 点击事件
  map.on('click', handleMapClick)
}

// 渲染楼层平面图
const renderFloorPlan = () => {
  if (!roomLayer || !deviceLayer) return

  const roomSource = roomLayer.getSource()
  const deviceSource = deviceLayer.getSource()

  if (!roomSource || !deviceSource) return

  // 清空现有要素
  roomSource.clear()
  deviceSource.clear()

  // 添加房间（直接使用虚拟坐标，不进行投影转换）
  rooms.value.forEach(room => {
    const polygon = new Polygon([room.coordinates])
    const feature = new Feature({
      geometry: polygon,
      id: room.id,
      name: room.code,
      data: room
    })
    roomSource.addFeature(feature)
  })

  // 添加设备
  devices.value.forEach(device => {
    // 🔧 修复：使用 coordinates 而不是 lon/lat
    if (device.coordinates && device.coordinates.length === 2) {
      const [x, y] = device.coordinates
      const point = new Point([x, y])
      const feature = new Feature({
        geometry: point,
        type: 'device',  // 🔧 标记为设备类型，用于点击事件判断
        id: device.id,
        name: device.name,
        deviceType: device.type,  // 设备的业务类型
        status: device.status,
        properties: device.properties
      })
      deviceSource.addFeature(feature)
    }
  })

  // 调整视图以适应所有房间
  if (rooms.value.length > 0) {
    const extent = roomSource.getExtent()
    map?.getView().fit(extent, { padding: [50, 50, 50, 50] })
  }
}

// 处理地图点击
const handleMapClick = (event: any) => {
  const pixel = event.pixel
  const features = map?.getFeaturesAtPixel(pixel)

  if (features && features.length > 0) {
    const feature = features[0]
    const featureType = feature.get('type')
    const data = feature.get('data')

    // 🔧 判断点击的是设备还是区域
    if (featureType === 'device') {
      // 点击的是设备
      const deviceData = {
        id: feature.get('id'),
        name: feature.get('name'),
        type: feature.get('deviceType'),  // 🔧 使用 deviceType 字段
        status: feature.get('status'),
        properties: feature.get('properties')
      }
      
      console.log(`🔍 点击设备:`, deviceData)
      
      // 显示设备详情
      showDeviceDetail(deviceData)
      
    } else if (data && data.type) {
      // 点击的是区域
      // 取消之前选中的房间
      const roomSource = roomLayer?.getSource()
      roomSource?.getFeatures().forEach((f: any) => {
        f.set('selected', false)
      })

      // 选中当前区域
      feature.set('selected', true)
      roomSource?.changed()

      // 🔧 修复：使用 area_id 而不是 room_id 筛选设备
      const areaDevices = devices.value.filter(d => {
        // 设备的 area_id 可能在 properties 中
        const deviceAreaId = d.properties?.area_id || d.area_id
        return deviceAreaId === data.id
      })
      
      console.log(`🔍 区域 ${data.name} (ID: ${data.id}) 内的设备:`, areaDevices.length, '个')

      // 显示区域详情
      selectedRoom.value = {
        name: data.name,
        data: data,
        devices: areaDevices,
        deviceCount: areaDevices.length
      }
      roomDrawerVisible.value = true
    }
  }
}

// 显示设备详情
const showDeviceDetail = (device: any) => {
  ElMessageBox.alert(
    `
      <div style="line-height: 2;">
        <p><strong>设备名称：</strong>${device.name}</p>
        <p><strong>设备ID：</strong>${device.id}</p>
        <p><strong>设备类型：</strong>${device.type || 'N/A'}</p>
        <p><strong>设备状态：</strong>
          <span style="color: ${device.status === 'online' ? '#67C23A' : device.status === 'offline' ? '#909399' : '#F56C6C'}">
            ${device.status === 'online' ? '🟢 在线' : device.status === 'offline' ? '⚪ 离线' : '🔴 故障'}
          </span>
        </p>
        ${device.properties?.area_id ? `<p><strong>所属区域ID：</strong>${device.properties.area_id}</p>` : ''}
        ${device.properties?.code ? `<p><strong>设备编码：</strong>${device.properties.code}</p>` : ''}
      </div>
    `,
    `设备详情 - ${device.name}`,
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭',
      type: 'info'
    }
  )
}

// 加载建筑楼层列表
const loadFloors = async () => {
  try {
    console.log(`🏢 开始加载建筑 ${props.buildingName} (ID: ${props.buildingId}) 的楼层数据...`)
    
    // 调用真实 API 获取楼层数据
    const data = await GisApi.getBuildingFloors(props.buildingId)
    
    if (data && Array.isArray(data) && data.length > 0) {
      floors.value = data.map(floor => ({
        id: floor.id,
        floor_number: floor.floor_number || floor.floorNumber,
        name: floor.name || `${floor.floor_number || floor.floorNumber}层`
      }))
      
      console.log(`✅ 成功加载 ${floors.value.length} 个楼层:`, floors.value.map(f => f.name).join(', '))
    } else {
      console.warn('⚠️  API 返回数据为空或格式异常，使用降级方案')
      throw new Error(data && Array.isArray(data) && data.length === 0 ? '该建筑没有楼层数据' : '楼层数据格式错误')
    }
    
  } catch (error: any) {
    console.error('❌ 加载楼层列表失败:', error)
    
    // 🎯 友好的错误提示
    const errorMsg = error.message || '未知错误'
    if (errorMsg.includes('没有楼层数据')) {
      console.warn('⚠️  该建筑暂无楼层数据')
      ElMessage.warning({
        message: '该建筑暂无楼层数据，请先在系统中配置楼层信息',
        duration: 5000
      })
      // 🔧 不生成默认楼层，保持空数组
      floors.value = []
    } else {
      ElMessage.error({
        message: `加载楼层数据失败: ${errorMsg}`,
        duration: 5000
      })
      floors.value = []
    }
  }
}

onMounted(async () => {
  await loadFloors()
  
  // 🔧 只有在有楼层数据时才初始化地图
  if (floors.value.length > 0) {
    initFloorPlan()
    await loadFloorData(currentFloor.value)
    renderFloorPlan()
  } else {
    console.warn('⚠️  无楼层数据，跳过地图初始化')
  }
})

onBeforeUnmount(() => {
  if (map) {
    map.dispose()
    map = null
  }
})
</script>

<style scoped lang="scss">
.building-floor-plan {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;

  .plan-header {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px;
    background: white;
    border-bottom: 1px solid #e4e7ed;

    .building-info {
      flex: 1;

      h3 {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }

      .building-desc {
        font-size: 14px;
        color: #909399;
      }
    }

    .floor-selector {
      display: flex;
      gap: 8px;
    }
  }

  .plan-container {
    flex: 1;
    position: relative;
    background: #f5f7fa;

    .plan-map {
      width: 100%;
      height: 100%;
    }

    .floor-empty-hint {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      z-index: 100;
      background: white;
      padding: 30px;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    .plan-legend {
      position: absolute;
      bottom: 20px;
      left: 20px;
      background: white;
      padding: 12px;
      border-radius: 4px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);

      .legend-item {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        &:last-child {
          margin-bottom: 0;
        }

        .legend-color {
          width: 20px;
          height: 20px;
          border-radius: 2px;

          &.room-normal {
            background: rgba(33, 150, 243, 0.3);
            border: 2px solid #2196F3;
          }

          &.room-selected {
            background: rgba(255, 193, 7, 0.6);
            border: 2px solid #FFC107;
          }

          &.device-point {
            background: #F44336;
            border: 2px solid white;
            border-radius: 50%;
          }
        }

        span {
          font-size: 14px;
          color: #606266;
        }
      }
    }
  }

  .device-list {
    margin-top: 20px;

    h4 {
      margin: 0 0 12px 0;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }
}
</style>

