<template>
  <div class="floor-plan-viewer">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button-group v-if="svgLoaded">
        <el-button :icon="ZoomIn" @click="zoomIn">放大</el-button>
        <el-button :icon="ZoomOut" @click="zoomOut">缩小</el-button>
        <el-button :icon="FullScreen" @click="fitView">适应窗口</el-button>
        <el-button :icon="Refresh" @click="resetView">重置</el-button>
      </el-button-group>

      <el-tag v-if="currentLayout" type="primary" class="status-tag">
        楼层: {{ currentLayout }}
      </el-tag>
      <el-tag v-if="selectedLayers.length > 0" type="success" class="status-tag">
        系统: {{ selectedLayers.length }} 个
      </el-tag>
      
      <!-- 调试信息 -->
      <el-tag type="info" class="status-tag">
        状态: {{ loading ? '加载中' : svgLoaded ? 'SVG已加载' : dxfLoaded ? 'DXF已加载' : '未加载' }}
      </el-tag>
    </div>

    <!-- 主内容区 -->
    <div class="content-area">
      <!-- SVG显示区域 -->
      <div class="svg-container" ref="svgContainer">
        <!-- 加载状态 -->
        <div v-show="loading" class="loading-state">
          <el-icon class="is-loading" style="font-size: 48px;"><Loading /></el-icon>
          <p>正在加载平面图...</p>
        </div>

        <!-- 错误状态 -->
        <div v-show="error && !loading" class="error-state">
          <el-icon style="font-size: 48px; color: #f56c6c;"><Warning /></el-icon>
          <p>{{ error }}</p>
        </div>

        <!-- 空状态 -->
        <div v-show="!svgLoaded && !loading && !error" class="empty-state">
          <el-empty description="请选择楼层和系统后点击'应用'按钮" />
        </div>
        
        <!-- SVG画布 - 始终存在于DOM中，通过v-show控制显示 -->
        <div v-show="svgLoaded && !loading && !error" id="svg-canvas" ref="svgCanvas"></div>
      </div>

      <!-- 右侧控制面板 -->
      <div v-if="dxfLoaded" class="control-panel">
        <el-card shadow="hover" class="panel-card">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">🏗️ 楼层选择</span>
            </div>
          </template>

          <!-- 楼层列表 -->
          <div class="floor-list">
            <el-radio-group v-model="currentLayout" @change="handleLayoutChange">
              <el-radio
                v-for="layout in layouts"
                :key="layout.name"
                :label="layout.name"
                class="floor-radio"
              >
                <span v-if="layout.name === 'Model'">默认布局 (Model)</span>
                <span v-else>{{ layout.name }}</span>
              </el-radio>
            </el-radio-group>

            <el-empty v-if="layouts.length === 0" description="未找到楼层信息" />
          </div>
        </el-card>

        <el-card shadow="hover" class="panel-card" style="margin-top: 16px">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">🔧 系统选择</span>
              <el-space>
                <el-button link size="small" @click="selectAllLayers">全选</el-button>
                <el-button link size="small" @click="clearAllLayers">清空</el-button>
              </el-space>
            </div>
          </template>

          <!-- 系统/图层列表 -->
          <div class="system-list">
            <el-checkbox-group v-model="selectedLayers" @change="handleLayerChange">
              <el-checkbox
                v-for="layer in layers"
                :key="layer.name"
                :label="layer.name"
                class="system-checkbox"
              >
                <span class="system-name">{{ layer.name }}</span>
                <span 
                  class="layer-color" 
                  :style="{ backgroundColor: getLayerColor(layer.colorIndex) }"
                ></span>
              </el-checkbox>
            </el-checkbox-group>

            <el-empty v-if="layers.length === 0" description="未找到图层信息" />
          </div>

          <div class="panel-footer" v-if="layers.length > 0">
            <el-button
              type="primary"
              :loading="loading"
              @click="applySelection"
              :disabled="!currentLayout || currentLayout.trim() === '' || selectedLayers.length === 0"
              style="width: 100%"
            >
              <span v-if="!currentLayout || currentLayout.trim() === ''">请先选择楼层</span>
              <span v-else-if="selectedLayers.length === 0">请至少选择一个系统</span>
              <span v-else>
                应用 ({{ currentLayout }} - {{ selectedLayers.length }} 个系统)
              </span>
            </el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ZoomIn, ZoomOut, FullScreen, Refresh, Loading, Warning } from '@element-plus/icons-vue'
import { SVG } from '@svgdotjs/svg.js'
import * as FloorDxfApi from '@/api/iot/spatial/floorDxf'
import { DeviceApi } from '@/api/iot/device/device'

defineOptions({ name: 'FloorPlanViewer' })

const props = defineProps<{
  floorId: number
}>()

// 响应式数据
const svgContainer = ref<HTMLDivElement>()
const svgCanvas = ref<HTMLDivElement>()
const loading = ref(false)
const svgLoaded = ref(false)
const dxfLoaded = ref(false)
const error = ref('')

// 楼层和系统选择
const layouts = ref<any[]>([])
const currentLayout = ref<string>('')
const layers = ref<any[]>([])
const selectedLayers = ref<string[]>([])

// 后端坐标参数（用于前端dxf-parser生成SVG）
const backendCoordParams = ref({
  buildingWidth: 0,
  buildingLength: 0,
  coordinateScale: 0,
  hasSvg: false
})

// 设备数据
const floorDevices = ref<any[]>([])

// SVG.js实例
let svgDraw: any = null
let currentZoom = 1
let panX = 0
let panY = 0
let isDragging = false
let startX = 0
let startY = 0

/**
 * 加载楼层DXF信息
 */
const loadFloorDxf = async () => {
  loading.value = true
  error.value = ''

  try {
    // 1. 获取后端坐标参数（用于前端dxf-parser生成SVG）
    await fetchBackendCoordParams()

    // 2. 加载设备数据
    await loadDevicesData()

    // 3. 获取布局（楼层）信息
    await fetchLayouts()

    // 4. 获取图层（系统）信息
    await fetchLayers()

    // 标记DXF已加载
    dxfLoaded.value = true
    svgLoaded.value = false

  } catch (err: any) {
    console.error('【楼层平面图】加载失败:', err)
    error.value = err.message || '加载失败'
  } finally {
    loading.value = false
  }
}

/**
 * 获取后端坐标参数
 */
const fetchBackendCoordParams = async () => {
  try {
    console.log('【楼层平面图】🎯 获取后端坐标参数...')
    const response = await FloorDxfApi.getDxfInfo(props.floorId)
    const data = response.data || response
    
    if (data.buildingWidth && data.buildingLength && data.coordinateScale) {
      backendCoordParams.value = {
        buildingWidth: data.buildingWidth,
        buildingLength: data.buildingLength,
        coordinateScale: data.coordinateScale,
        hasSvg: !!data.dxfLayer0Svg
      }
      console.log('【楼层平面图】✅ 后端坐标参数已获取:', backendCoordParams.value)
      console.log('【楼层平面图】🔑 coordinateScale =', data.coordinateScale.toFixed(2), '像素/米')
    } else {
      console.warn('【楼层平面图】⚠️ 后端坐标参数缺失')
    }
  } catch (error) {
    console.warn('【楼层平面图】⚠️ 后端坐标参数获取失败:', error)
  }
}

/**
 * 加载楼层设备数据
 */
const loadDevicesData = async () => {
  try {
    console.log('【楼层平面图】📍 加载设备数据...')
    const allDevices: any[] = []
    let pageNo = 1
    const pageSize = 100
    let hasMore = true
    
    while (hasMore) {
      const response = await DeviceApi.getDevicePage({
        floorId: props.floorId,
        pageNo: pageNo,
        pageSize: pageSize
      })
      
      if (response.list && response.list.length > 0) {
        allDevices.push(...response.list)
        hasMore = response.list.length === pageSize && response.total > allDevices.length
        pageNo++
      } else {
        hasMore = false
      }
    }
    
    floorDevices.value = allDevices
    console.log('【楼层平面图】✅ 设备数据加载完成:', floorDevices.value.length, '个设备')
  } catch (error) {
    console.error('【楼层平面图】❌ 加载设备数据失败:', error)
    floorDevices.value = []
  }
}

/**
 * 获取布局（楼层）信息
 */
const fetchLayouts = async () => {
  try {
    const response = await FloorDxfApi.getLayouts(props.floorId)
    console.log('【楼层平面图】布局响应:', response)
    
    // 注意：后端返回的数据在 response.data 中
    const layoutData = response.data || response
    layouts.value = layoutData.layouts || []
    
    console.log('【楼层平面图】布局数据:', layouts.value)
    
    // 如果后端没返回布局或返回空数组，强制使用默认布局
    if (!layouts.value || layouts.value.length === 0) {
      console.warn('【楼层平面图】后端未返回布局信息，使用默认布局')
      layouts.value = [{ name: 'Model', index: 0 }]
    }
    
    // 默认选中第一个楼层
    if (layouts.value.length > 0) {
      currentLayout.value = layouts.value[0].name
      console.log('【楼层平面图】默认选中楼层:', currentLayout.value)
    }
  } catch (error: any) {
    console.error('【楼层平面图】获取布局信息失败:', error)
    // 异常处理：使用默认布局
    layouts.value = [{ name: 'Model', index: 0 }]
    currentLayout.value = 'Model'
  }
}

/**
 * 获取图层（系统）信息
 */
const fetchLayers = async () => {
  try {
    const response = await FloorDxfApi.getLayers(props.floorId)
    console.log('【楼层平面图】图层响应:', response)
    
    // 注意：后端返回的数据在 response.data 中
    const layerData = response.data || response
    layers.value = layerData.layers || []
    
    // 默认选中所有可见图层
    selectedLayers.value = layers.value
      .filter((layer: any) => layer.isVisible)
      .map((layer: any) => layer.name)

    console.log('【楼层平面图】图层解析成功:', layers.value)
    console.log('【楼层平面图】默认选中图层:', selectedLayers.value)
  } catch (error: any) {
    console.error('【楼层平面图】获取图层信息失败:', error)
    throw error
  }
}

/**
 * 楼层切换
 */
const handleLayoutChange = (layout: string) => {
  console.log('【楼层平面图】切换楼层:', layout)
}

/**
 * 图层选择变化
 */
const handleLayerChange = (value: string[]) => {
  console.log('【楼层平面图】图层选择变化:', value)
}

/**
 * 应用选择（楼层+系统）
 * 
 * 🎯 使用前端dxf-parser生成SVG（无水印）+ 后端coordinateScale（保证坐标准确）
 */
const applySelection = async () => {
  if (!currentLayout.value || currentLayout.value.trim() === '') {
    ElMessage.warning('请选择楼层')
    return
  }

  if (selectedLayers.value.length === 0) {
    ElMessage.warning('请至少选择一个系统')
    return
  }

  loading.value = true

  try {
    let svgContent = ''
    
    // 🎯 方案A：前端dxf-parser生成SVG（优先，无水印）
    if (backendCoordParams.value.coordinateScale > 0) {
      console.log('【楼层平面图】🎨 使用前端dxf-parser生成SVG（无水印）...')
      svgContent = await generateSvgByFrontend()
    }
    
    // ⚠️ 方案B：后端Aspose.CAD生成SVG（降级，有水印）
    if (!svgContent) {
      console.log('【楼层平面图】使用后端SVG（有水印，后备方案）...')
      svgContent = await generateSvgByBackend()
    }

    if (!svgContent || svgContent.trim() === '') {
      throw new Error('SVG内容为空')
    }

    console.log('【楼层平面图】准备显示SVG')
    console.log('【楼层平面图】当前 svgCanvas.value:', svgCanvas.value)
    
    // 先标记为已加载，让v-show显示容器
    svgLoaded.value = true
    console.log('【楼层平面图】svgLoaded设置为true')
    
    // 等待DOM更新，让v-show生效
    await nextTick()
    console.log('【楼层平面图】DOM更新后，svgCanvas.value:', svgCanvas.value)
    
    // 显示SVG
    displaySvg(svgContent)

    ElMessage.success(`显示成功！${currentLayout.value} - ${selectedLayers.value.length} 个系统`)

  } catch (error: any) {
    console.error('【楼层平面图】转换失败:', error)
    ElMessage.error('转换失败: ' + (error.message || '未知错误'))
    svgLoaded.value = false
  } finally {
    loading.value = false
  }
}

/**
 * 前端生成SVG（使用dxf-parser）
 */
const generateSvgByFrontend = async (): Promise<string> => {
  try {
    console.log('【楼层平面图】🎨 前端解析DXF...')
    console.log('【楼层平面图】  选中图层:', selectedLayers.value)
    console.log('【楼层平面图】  坐标比例:', backendCoordParams.value.coordinateScale.toFixed(2), '像素/米')
    
    // 1. 获取DXF文件内容
    const response = await FloorDxfApi.getDxfFileContent(props.floorId)
    const dxfContent = typeof response === 'string' ? response : (response as any).data
    
    if (!dxfContent || typeof dxfContent !== 'string' || dxfContent.length === 0) {
      console.error('【楼层平面图】❌ DXF文件内容为空')
      return ''
    }
    
    console.log('【楼层平面图】成功获取DXF内容，长度:', dxfContent.length)
    
    // 2. 使用后端coordinateScale转换为SVG
    const { convertDxfToSvgWithBackendScale } = await import('@/utils/dxf/dxfToSvg')
    const result = convertDxfToSvgWithBackendScale(
      dxfContent,
      selectedLayers.value,  // 使用选中的图层
      backendCoordParams.value.coordinateScale,
      1920,
      1080
    )
    
    if (result && result.svg) {
      console.log('【楼层平面图】✅✅✅ 前端解析成功（无水印，与后端大小一致）')
      console.log('【楼层平面图】  SVG长度:', result.svg.length)
      console.log('【楼层平面图】  建筑尺寸:', result.buildingWidth.toFixed(2), 'm x', result.buildingLength.toFixed(2), 'm')
      console.log('【楼层平面图】  坐标比例:', result.coordinateScale.toFixed(2), '像素/米（来自后端）')
      
      return result.svg
    }
    
    return ''
  } catch (error) {
    console.error('【楼层平面图】❌ 前端解析DXF失败:', error)
    return ''
  }
}

/**
 * 后端生成SVG（降级方案）
 */
const generateSvgByBackend = async (): Promise<string> => {
  try {
    console.warn('【楼层平面图】⚠️ 使用后端Aspose.CAD生成SVG（包含水印）')
    
    const response = await FloorDxfApi.getFloorPlanSvg(
      props.floorId,
      currentLayout.value,
      selectedLayers.value.join(',')
    )
    
    const svgData = response.data || response
    return svgData.svgContent || ''
  } catch (error) {
    console.error('【楼层平面图】❌ 后端生成SVG失败:', error)
    return ''
  }
}

/**
 * 显示SVG内容
 */
const displaySvg = (svgContent: string) => {
  try {
    console.log('【楼层平面图】开始显示SVG')
    console.log('【楼层平面图】svgCanvas.value:', svgCanvas.value)
    
    if (!svgCanvas.value) {
      console.error('【楼层平面图】❌ SVG容器未找到！')
      ElMessage.error('SVG容器未找到')
      return
    }

    console.log('【楼层平面图】✅ SVG容器已找到')

    // 清除旧内容
    if (svgDraw) {
      console.log('【楼层平面图】清除旧SVG内容')
      svgDraw.clear()
      svgDraw.remove()
    }

    // 清空容器
    svgCanvas.value.innerHTML = ''
    console.log('【楼层平面图】容器已清空')

    // 创建SVG.js实例
    svgDraw = SVG().addTo(svgCanvas.value).size('100%', '100%')
    console.log('【楼层平面图】SVG.js实例已创建')

    // 解析SVG内容
    const parser = new DOMParser()
    const svgDoc = parser.parseFromString(svgContent, 'image/svg+xml')
    const svgElement = svgDoc.documentElement
    
    console.log('【楼层平面图】SVG元素标签:', svgElement.tagName)
    console.log('【楼层平面图】SVG子元素数量:', svgElement.children.length)

    // 检查是否解析出错
    const parseError = svgDoc.querySelector('parsererror')
    if (parseError) {
      console.error('【楼层平面图】❌ SVG解析错误:', parseError.textContent)
      throw new Error('SVG解析失败')
    }

    // 获取原始SVG的viewBox或尺寸
    const viewBox = svgElement.getAttribute('viewBox')
    const width = svgElement.getAttribute('width')
    const height = svgElement.getAttribute('height')

    console.log('【楼层平面图】SVG viewBox:', viewBox)
    console.log('【楼层平面图】SVG width:', width, 'height:', height)

    // 将SVG内容导入到SVG.js
    svgDraw.svg(svgElement.innerHTML)
    console.log('【楼层平面图】SVG内容已导入')

    // 设置viewBox以保持纵横比
    if (viewBox) {
      svgDraw.viewbox(viewBox)
      console.log('【楼层平面图】使用viewBox:', viewBox)
    } else if (width && height) {
      svgDraw.viewbox(0, 0, parseFloat(width), parseFloat(height))
      console.log('【楼层平面图】使用width/height创建viewBox')
    }

    // 初始化交互
    initializeInteraction()
    console.log('【楼层平面图】交互已初始化')

    // 自动适应视图
    setTimeout(() => {
      fitView()
      console.log('【楼层平面图】视图已适应')
    }, 100)

    console.log('【楼层平面图】✅ SVG显示成功！')

    // 显示设备
    displayDevices()

  } catch (error) {
    console.error('【楼层平面图】❌ 显示SVG失败:', error)
    ElMessage.error('显示SVG失败: ' + (error as Error).message)
  }
}

/**
 * 显示设备到SVG上
 * 
 * ⚠️ 坐标转换逻辑：与 FloorPlanEditorV2.vue 保持一致
 */
const displayDevices = () => {
  console.log('【楼层平面图】🔍 检查设备显示条件:')
  console.log('  svgDraw:', !!svgDraw)
  console.log('  floorDevices.value.length:', floorDevices.value.length)
  console.log('  floorDevices.value:', floorDevices.value)
  
  if (!svgDraw || floorDevices.value.length === 0) {
    console.warn('【楼层平面图】⚠️ 无法显示设备:')
    console.warn('  - SVG实例存在:', !!svgDraw)
    console.warn('  - 设备数量:', floorDevices.value.length)
    return
  }

  console.log('【楼层平面图】📍 开始显示设备:', floorDevices.value.length, '个')

  // 获取SVG容器尺寸（1920x1080）
  const svgWidth = 1920
  const svgHeight = 1080

  // 获取实际显示尺寸
  const containerWidth = svgContainer.value?.clientWidth || 1000
  const containerHeight = svgContainer.value?.clientHeight || 700

  // 计算缩放比例（与SVG适配画布的比例一致）
  const scale = Math.min(
    (containerWidth * 0.9) / svgWidth,
    (containerHeight * 0.9) / svgHeight
  )

  // 计算居中偏移
  const offsetX = (containerWidth - svgWidth * scale) / 2
  const offsetY = (containerHeight - svgHeight * scale) / 2

  console.log('【楼层平面图】SVG变换参数:')
  console.log('  SVG尺寸:', svgWidth, 'x', svgHeight)
  console.log('  容器尺寸:', containerWidth, 'x', containerHeight)
  console.log('  缩放比例:', scale.toFixed(4))
  console.log('  偏移:', offsetX.toFixed(2), ',', offsetY.toFixed(2))

  let displayedCount = 0

  floorDevices.value.forEach((device: any) => {
    const dxfX = device.localX || device.x || 0
    const dxfY = device.localY || device.y || 0

    // 跳过无效坐标
    if (dxfX === 0 && dxfY === 0) {
      return
    }

    // 🎯 坐标转换（与 FloorPlanEditorV2 一致）
    // 步骤1：DXF坐标（米）→ SVG像素
    const svgRawX = dxfX * backendCoordParams.value.coordinateScale
    const svgRawY = dxfY * backendCoordParams.value.coordinateScale

    // 步骤2：Y轴翻转（DXF向上 → SVG向下）
    const svgX = svgRawX
    const svgY = -svgRawY

    // 步骤3：应用缩放和偏移到实际显示位置
    const x = svgX * scale + offsetX
    const y = svgY * scale + offsetY

    // 添加设备图标
    const iconSize = (device.deviceIconSize || 30) * scale
    const color = device.color || getDeviceColor(device.status || device.deviceType)

    // 创建设备分组
    const deviceGroup = svgDraw.group()

    // 添加图标
    const circle = svgDraw.circle(iconSize).fill(color).stroke('#333').strokeWidth(2)
    deviceGroup.add(circle)

    // 添加标签
    const label = svgDraw
      .text(device.deviceName || '未命名')
      .font({ size: 12 * scale, fill: '#333', anchor: 'middle' })
      .move(0, -iconSize / 2 - 8 * scale)

    deviceGroup.add(label)

    // 设置分组位置
    deviceGroup.move(x - iconSize / 2, y - iconSize / 2)

    // 添加提示信息
    deviceGroup.attr('title', `${device.deviceName}\n类型: ${device.deviceType}\n坐标: (${dxfX.toFixed(2)}m, ${dxfY.toFixed(2)}m)`)

    displayedCount++
  })

  console.log('【楼层平面图】✅ 设备显示完成:', displayedCount, '个设备')
}

/**
 * 获取设备颜色
 */
const getDeviceColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'online': '#67c23a',
    'offline': '#909399',
    'fault': '#f56c6c',
    'camera': '#409eff',
    'access_control': '#67c23a',
    'sensor': '#e6a23c',
    'smoke_detector': '#f56c6c'
  }
  return colorMap[status] || '#409eff'
}

/**
 * 初始化交互功能
 */
const initializeInteraction = () => {
  if (!svgCanvas.value || !svgDraw) return

  const canvas = svgCanvas.value

  // 鼠标拖拽平移
  canvas.addEventListener('mousedown', (e: MouseEvent) => {
    if (e.button === 0) {
      isDragging = true
      startX = e.clientX - panX
      startY = e.clientY - panY
      canvas.style.cursor = 'grabbing'
    }
  })

  canvas.addEventListener('mousemove', (e: MouseEvent) => {
    if (isDragging) {
      panX = e.clientX - startX
      panY = e.clientY - startY
      updateTransform()
    }
  })

  canvas.addEventListener('mouseup', () => {
    isDragging = false
    canvas.style.cursor = 'grab'
  })

  canvas.addEventListener('mouseleave', () => {
    isDragging = false
    canvas.style.cursor = 'default'
  })

  // 鼠标滚轮缩放
  canvas.addEventListener('wheel', (e: WheelEvent) => {
    e.preventDefault()
    const delta = e.deltaY > 0 ? 0.9 : 1.1
    currentZoom *= delta
    currentZoom = Math.max(0.1, Math.min(10, currentZoom))
    updateTransform()
  })

  canvas.style.cursor = 'grab'
}

/**
 * 更新变换
 */
const updateTransform = () => {
  if (!svgDraw) return
  const svgNode = svgDraw.node as SVGSVGElement
  svgNode.style.transform = `translate(${panX}px, ${panY}px) scale(${currentZoom})`
}

/**
 * 放大
 */
const zoomIn = () => {
  currentZoom *= 1.2
  currentZoom = Math.min(10, currentZoom)
  updateTransform()
}

/**
 * 缩小
 */
const zoomOut = () => {
  currentZoom *= 0.8
  currentZoom = Math.max(0.1, currentZoom)
  updateTransform()
}

/**
 * 适应窗口
 */
const fitView = () => {
  currentZoom = 1
  panX = 0
  panY = 0
  updateTransform()
}

/**
 * 重置视图
 */
const resetView = () => {
  fitView()
}

/**
 * 全选图层
 */
const selectAllLayers = () => {
  selectedLayers.value = layers.value.map((layer: any) => layer.name)
}

/**
 * 清空图层选择
 */
const clearAllLayers = () => {
  selectedLayers.value = []
}

/**
 * 获取图层颜色
 */
const getLayerColor = (colorIndex: number) => {
  const colorMap: Record<number, string> = {
    1: '#FF0000',
    2: '#FFFF00',
    3: '#00FF00',
    4: '#00FFFF',
    5: '#0000FF',
    6: '#FF00FF',
    7: '#FFFFFF',
    8: '#808080',
    9: '#C0C0C0'
  }
  return colorMap[colorIndex] || '#000000'
}

// 生命周期
onMounted(() => {
  console.log('【楼层平面图】组件已挂载，楼层ID:', props.floorId)
  loadFloorDxf()
})

onBeforeUnmount(() => {
  if (svgDraw) {
    svgDraw.remove()
  }
})
</script>

<style scoped lang="scss">
.floor-plan-viewer {
  .toolbar {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
    flex-wrap: wrap;

    .status-tag {
      margin-left: 8px;
    }
  }

  .content-area {
    display: flex;
    gap: 16px;
    height: 600px;

    .svg-container {
      flex: 1;
      background: #f5f7fa;
      border-radius: 4px;
      overflow: hidden;
      position: relative;

      .loading-state,
      .error-state,
      .empty-state {
        height: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 16px;
      }

      #svg-canvas {
        width: 100%;
        height: 100%;
        overflow: hidden;
      }
    }

    .control-panel {
      width: 320px;
      display: flex;
      flex-direction: column;
      gap: 16px;
      overflow-y: auto;

      .panel-card {
        :deep(.el-card__header) {
          padding: 12px 16px;
        }

        :deep(.el-card__body) {
          padding: 16px;
          max-height: 400px;
          overflow-y: auto;
        }

        .panel-header {
          display: flex;
          align-items: center;
          justify-content: space-between;

          .panel-title {
            font-weight: 600;
            font-size: 14px;
          }
        }

        .floor-list {
          .el-radio-group {
            display: flex;
            flex-direction: column;
            gap: 12px;
          }

          .floor-radio {
            width: 100%;
            height: auto;
            margin-right: 0;
            padding: 12px;
            border: 1px solid #dcdfe6;
            border-radius: 4px;
            transition: all 0.3s;

            &:hover {
              border-color: #409eff;
              background: #ecf5ff;
            }

            :deep(.el-radio__label) {
              font-size: 14px;
              font-weight: 500;
            }
          }

          :deep(.el-radio.is-checked) {
            border-color: #409eff;
            background: #ecf5ff;
          }
        }

        .system-list {
          .el-checkbox-group {
            display: flex;
            flex-direction: column;
            gap: 8px;
          }

          .system-checkbox {
            width: 100%;
            height: auto;
            margin-right: 0;
            padding: 8px;
            border: 1px solid #dcdfe6;
            border-radius: 4px;
            transition: all 0.3s;

            &:hover {
              border-color: #409eff;
              background: #f0f9ff;
            }

            :deep(.el-checkbox__label) {
              display: flex;
              align-items: center;
              justify-content: space-between;
              width: 100%;

              .system-name {
                font-size: 13px;
              }

              .layer-color {
                width: 16px;
                height: 16px;
                border-radius: 2px;
                border: 1px solid #dcdfe6;
                flex-shrink: 0;
              }
            }
          }

          :deep(.el-checkbox.is-checked) {
            border-color: #67c23a;
            background: #f0f9ff;
          }
        }

        .panel-footer {
          margin-top: 12px;
          padding-top: 12px;
          border-top: 1px solid #ebeef5;
        }
      }
    }
  }
}
</style>

