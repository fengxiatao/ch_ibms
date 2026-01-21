<template>
  <div class="geoserver-map-container">
    <div :id="mapId" class="map-view" :style="{ width: width, height: height }"></div>
    
    <!-- 图层控制面板 -->
    <div v-if="showLayerControl" class="layer-control-panel">
      <div class="panel-header">
        <span>图层控制</span>
        <el-icon @click="togglePanel" class="collapse-icon">
          <component :is="panelCollapsed ? 'DArrowRight' : 'DArrowLeft'" />
        </el-icon>
      </div>
      <div v-show="!panelCollapsed" class="panel-content">
        <div v-for="layer in layerConfigs" :key="layer.name" class="layer-item">
          <el-checkbox 
            v-model="layer.visible" 
            @change="toggleLayer(layer)"
          >
            {{ layer.label }}
          </el-checkbox>
          <el-slider
            v-show="layer.visible"
            v-model="layer.opacity"
            :min="0"
            :max="100"
            @input="changeOpacity(layer)"
            class="opacity-slider"
          />
        </div>
      </div>
    </div>

    <!-- 信息弹窗 -->
    <el-dialog
      v-model="featureDialogVisible"
      :title="featureInfo.title"
      width="600px"
      append-to-body
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item 
          v-for="(value, key) in featureInfo.properties" 
          :key="key"
          :label="key"
        >
          {{ value }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import OLMap from 'ol/Map'
import View from 'ol/View'
import TileLayer from 'ol/layer/Tile'
import TileWMS from 'ol/source/TileWMS'
import OSM from 'ol/source/OSM'
import XYZ from 'ol/source/XYZ'
import { defaults as defaultControls } from 'ol/control'
import type { Coordinate } from 'ol/coordinate'
import 'ol/ol.css'

// 定义属性
interface LayerConfig {
  name: string
  label: string
  workspace: string
  visible: boolean
  opacity: number
  zIndex: number
  cqlFilter?: string  // CQL 过滤器，用于过滤图层要素
}

interface Props {
  // GeoServer 配置
  geoserverUrl?: string
  workspace?: string
  // 地图配置
  center?: Coordinate
  zoom?: number
  minZoom?: number
  maxZoom?: number
  projection?: string
  width?: string
  height?: string
  // 图层配置
  layers?: LayerConfig[]
  baseMapType?: 'osm' | 'tianditu' | 'gaode' | 'none'
  showLayerControl?: boolean
  // 交互配置
  enableClick?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  geoserverUrl: '/geoserver',  // 使用相对路径，通过 Nginx 代理
  workspace: 'ibms_gis',
  center: () => [113.264385, 23.129112], // 广州
  zoom: 12,
  minZoom: 3,
  maxZoom: 20,
  projection: 'EPSG:4326',
  width: '100%',
  height: '600px',
  layers: () => [
    { name: 'campus', label: '园区一览图', workspace: 'ch_ibms_gis', visible: true, opacity: 100, zIndex: 5 },
    { name: 'building', label: '建筑一览图', workspace: 'ch_ibms_gis', visible: true, opacity: 100, zIndex: 4 },
    { name: 'floor', label: '楼层一览图', workspace: 'ch_ibms_gis', visible: false, opacity: 100, zIndex: 3 },
    { name: 'room', label: '房间一览图', workspace: 'ch_ibms_gis', visible: false, opacity: 100, zIndex: 2 },
    { name: 'device', label: '设备一览图', workspace: 'ch_ibms_gis', visible: true, opacity: 100, zIndex: 1 }
  ],
  baseMapType: 'osm',
  showLayerControl: true,
  enableClick: true
})

const emit = defineEmits(['featureClick', 'mapReady'])

// 响应式数据
const mapId = ref(`geoserver-map-${Date.now()}`)
const map = ref<OLMap | null>(null)
const panelCollapsed = ref(false)
const layerConfigs = ref<LayerConfig[]>([...props.layers])
const layerObjects = ref(new Map<string, TileLayer<TileWMS>>())
const featureDialogVisible = ref(false)
const featureInfo = ref<{ title: string; properties: Record<string, any> }>({
  title: '',
  properties: {}
})

// 初始化地图
const initMap = () => {
  const layers: any[] = []

  // 添加底图
  if (props.baseMapType === 'osm') {
    layers.push(
      new TileLayer({
        source: new OSM(),
        zIndex: 0
      })
    )
  } else if (props.baseMapType === 'tianditu') {
    const tiandituKey = 'YOUR_TIANDITU_KEY' // 需要替换为实际的天地图 key
    layers.push(
      new TileLayer({
        source: new XYZ({
          url: `http://t{0-7}.tianditu.gov.cn/DataServer?T=vec_w&x={x}&y={y}&l={z}&tk=${tiandituKey}`
        }),
        zIndex: 0
      })
    )
  }

  // 添加 GeoServer WMS 图层
  console.log('🗺️ 开始加载 GeoServer 图层...')
  console.log('  GeoServer URL:', props.geoserverUrl)
  console.log('  工作空间:', props.workspace)
  
  layerConfigs.value.forEach((layerConfig) => {
    const layerFullName = `${layerConfig.workspace}:${layerConfig.name}`
    const wmsUrl = `${props.geoserverUrl}/wms`
    
    console.log(`📍 创建图层: ${layerConfig.label} (${layerFullName})`, {
      URL: wmsUrl,
      可见: layerConfig.visible,
      透明度: layerConfig.opacity,
      zIndex: layerConfig.zIndex
    })
    
    // 构建 WMS 参数
    const wmsParams: any = {
      LAYERS: layerFullName,
      TILED: true,
      VERSION: '1.1.0',
      FORMAT: 'image/png',
      TRANSPARENT: true
    }
    
    // 如果有 CQL 过滤器，添加到参数中
    if (layerConfig.cqlFilter) {
      wmsParams.CQL_FILTER = layerConfig.cqlFilter
      console.log(`  🔍 应用 CQL 过滤器: ${layerConfig.cqlFilter}`)
    }
    
    const wmsSource = new TileWMS({
      url: wmsUrl,
      params: wmsParams,
      serverType: 'geoserver',
      crossOrigin: 'anonymous'
    })
    
    // 监听图层加载事件
    wmsSource.on('tileloadstart', () => {
      console.log(`⏳ ${layerConfig.label} 开始加载瓦片...`)
    })
    
    wmsSource.on('tileloadend', () => {
      console.log(`✅ ${layerConfig.label} 瓦片加载成功`)
    })
    
    wmsSource.on('tileloaderror', (event: any) => {
      const tile = event.tile
      const tileUrl = tile?.src_ || tile?.getKey?.() || 'unknown'
      console.error(`❌ ${layerConfig.label} 瓦片加载失败:`)
      console.error('  - URL:', tileUrl)
      console.error('  - Event:', event)
    })

    const tileLayer = new TileLayer({
      source: wmsSource,
      visible: layerConfig.visible,
      opacity: layerConfig.opacity / 100,
      zIndex: layerConfig.zIndex
    })

    layers.push(tileLayer)
    layerObjects.value.set(layerConfig.name, tileLayer)
  })

  // 创建地图
  map.value = new OLMap({
    target: mapId.value,
    layers: layers,
    view: new View({
      center: props.center,
      zoom: props.zoom,
      minZoom: props.minZoom,
      maxZoom: props.maxZoom,
      projection: props.projection
    }),
    controls: defaultControls({
      attribution: false,
      zoom: true
    })
  })

  // 点击事件
  if (props.enableClick) {
    map.value.on('singleclick', handleMapClick)
  }

  emit('mapReady', map.value)
}

// 处理地图点击
const handleMapClick = async (event: any) => {
  const viewResolution = map.value?.getView().getResolution()
  const coordinate = event.coordinate
  
  console.log('🖱️ 地图被点击:', {
    坐标: coordinate,
    分辨率: viewResolution
  })

  // 获取可见图层的 GetFeatureInfo 请求
  // 按 zIndex 从高到低排序，优先检查上层图层（设备、建筑等，而不是园区）
  const visibleLayers = layerConfigs.value
    .filter(layer => layer.visible)
    .sort((a, b) => b.zIndex - a.zIndex)  // 降序排序，zIndex 大的在前（设备 zIndex=5 最先检查）
  
  console.log('👁️ 可见图层（按 zIndex 降序排序）:', visibleLayers.map(l => `${l.name}(zIndex:${l.zIndex})`))
  
  for (const layerConfig of visibleLayers) {
    console.log(`🔍 检查图层: ${layerConfig.name}`)
    const layer = layerObjects.value.get(layerConfig.name)
    if (layer) {
      const source = layer.getSource()
      if (source instanceof TileWMS) {
        const url = source.getFeatureInfoUrl(
          coordinate,
          viewResolution!,
          props.projection,
          {
            INFO_FORMAT: 'application/json',
            FEATURE_COUNT: 1
          }
        )

        if (url) {
          console.log(`📡 GetFeatureInfo URL: ${url.substring(0, 150)}...`)
          try {
            const response = await fetch(url)
            const contentType = response.headers.get('content-type')
            console.log(`  📄 Content-Type: ${contentType}`)
            
            // 检查是否返回 JSON
            if (!contentType || !contentType.includes('application/json')) {
              console.warn(`⚠️ 图层 ${layerConfig.name} 未返回 JSON 格式数据`)
              continue
            }
            
            const text = await response.text()
            if (!text || text.trim() === '') {
              console.warn(`⚠️ 图层 ${layerConfig.name} 返回空数据`)
              continue
            }
            
            console.log(`  ✅ 获取到数据，长度: ${text.length}`)
            const data = JSON.parse(text)
            
            if (data.features && data.features.length > 0) {
              const feature = data.features[0]
              
              console.log('🎯 成功获取要素信息:')
              console.log('  📍 图层:', layerConfig.name)
              console.log('  📊 要素:', feature.properties)
              
              // 触发点击事件给父组件
              emit('featureClick', {
                layer: layerConfig.name,
                feature: feature,
                coordinate: coordinate
              })
              
              // 如果父组件没有处理，显示默认对话框
              // 这里给父组件一个短暂的时间来处理
              setTimeout(() => {
                if (!featureDialogVisible.value) {
                  featureInfo.value = {
                    title: `${layerConfig.label} - 详细信息`,
                    properties: feature.properties
                  }
                  // 父组件可以通过不同的处理来决定是否显示这个对话框
                }
              }, 100)
              
              break // 只显示第一个匹配的要素
            }
          } catch (error) {
            console.warn(`获取图层 ${layerConfig.name} 要素信息失败:`, error)
            // 继续尝试下一个图层，不要中断
            continue
          }
        }
      }
    }
  }
}

// 切换图层显示
const toggleLayer = (layer: LayerConfig) => {
  const layerObj = layerObjects.value.get(layer.name)
  if (layerObj) {
    layerObj.setVisible(layer.visible)
  }
}

// 改变图层透明度
const changeOpacity = (layer: LayerConfig) => {
  const layerObj = layerObjects.value.get(layer.name)
  if (layerObj) {
    layerObj.setOpacity(layer.opacity / 100)
  }
}

// 切换面板展开/收起
const togglePanel = () => {
  panelCollapsed.value = !panelCollapsed.value
}

// 缩放到图层范围
const zoomToLayer = (layerName: string) => {
  // 这里可以根据图层的 bbox 进行缩放
  console.log('缩放到图层:', layerName)
}

// 暴露给父组件的方法
const getMap = () => map.value

const setCenter = (center: Coordinate) => {
  map.value?.getView().setCenter(center)
}

const setZoom = (zoom: number) => {
  map.value?.getView().setZoom(zoom)
}

const flyTo = (coordinate: Coordinate, zoom?: number) => {
  const view = map.value?.getView()
  if (view) {
    view.animate({
      center: coordinate,
      zoom: zoom || view.getZoom(),
      duration: 1000
    })
  }
}

defineExpose({
  getMap,
  setCenter,
  setZoom,
  flyTo,
  zoomToLayer
})

// 生命周期
onMounted(() => {
  initMap()
})

onBeforeUnmount(() => {
  if (map.value) {
    map.value.setTarget(undefined)
    map.value = null
  }
})

// 监听图层配置变化
watch(() => props.layers, (newLayers) => {
  layerConfigs.value = [...newLayers]
}, { deep: true })
</script>

<style scoped lang="scss">
.geoserver-map-container {
  position: relative;
  width: 100%;
  height: 100%;
}

.map-view {
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.layer-control-panel {
  position: absolute;
  top: 10px;
  right: 10px;
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  min-width: 250px;
  max-width: 300px;
  z-index: 1000;

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    border-bottom: 1px solid #e8e8e8;
    font-weight: 600;
    cursor: pointer;

    .collapse-icon {
      cursor: pointer;
      transition: transform 0.3s;

      &:hover {
        color: var(--el-color-primary);
      }
    }
  }

  .panel-content {
    padding: 12px 16px;
    max-height: 400px;
    overflow-y: auto;

    .layer-item {
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }

      .opacity-slider {
        margin-top: 8px;
        padding-left: 24px;
      }
    }
  }
}

// 滚动条样式
.panel-content::-webkit-scrollbar {
  width: 6px;
}

.panel-content::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.panel-content::-webkit-scrollbar-track {
  background-color: rgba(0, 0, 0, 0.05);
}
</style>

