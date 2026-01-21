<template>
  <div class="floor-map-container">
    <!-- SVG背景层（DXF生成） -->
    <div 
      v-if="floorPlanSvg" 
      class="floor-plan-background" 
      v-html="floorPlanSvg"
    ></div>
    
    <!-- 无平面图占位 -->
    <div v-else class="floor-plan-placeholder">
      <Icon icon="ep:map-location" :size="64" />
      <p>{{ placeholderText }}</p>
    </div>

    <!-- 设备标记层 -->
    <div 
      v-for="device in deviceMarkers" 
      :key="device.id"
      class="device-marker"
      :class="getMarkerClass(device)"
      :style="getMarkerStyle(device)"
      :title="device.deviceName || device.name"
      @click="handleDeviceClick(device)"
      @mouseenter="handleDeviceHover(device, $event)"
      @mouseleave="handleDeviceLeave"
    >
      <Icon :icon="getDeviceIcon(device)" />
      <div class="marker-pulse"></div>
    </div>

    <!-- 设备详情弹窗 -->
    <el-dialog
      v-model="deviceDialogVisible"
      :title="selectedDevice?.deviceName || selectedDevice?.name || '设备详情'"
      width="600px"
    >
      <div v-if="selectedDevice" class="device-details">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="设备名称">
            {{ selectedDevice.deviceName || selectedDevice.name }}
          </el-descriptions-item>
          <el-descriptions-item label="设备状态">
            <el-tag :type="selectedDevice.status === 1 ? 'success' : 'danger'">
              {{ selectedDevice.status === 1 ? '在线' : '离线' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="产品名称" v-if="selectedDevice.productName">
            {{ selectedDevice.productName }}
          </el-descriptions-item>
          <el-descriptions-item label="设备类型" v-if="selectedDevice.deviceType !== undefined">
            <el-tag>{{ getDeviceTypeText(selectedDevice.deviceType) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="所属建筑" v-if="selectedDevice.buildingName">
            {{ selectedDevice.buildingName }}
          </el-descriptions-item>
          <el-descriptions-item label="所属楼层" v-if="selectedDevice.floorName">
            {{ selectedDevice.floorName }}
          </el-descriptions-item>
          <el-descriptions-item label="安装位置" v-if="selectedDevice.installLocation">
            {{ selectedDevice.installLocation }}
          </el-descriptions-item>
          <el-descriptions-item label="坐标(m)" :span="2">
            X: {{ selectedDevice.localX?.toFixed(2) || '-' }}, 
            Y: {{ selectedDevice.localY?.toFixed(2) || '-' }}, 
            Z: {{ selectedDevice.localZ?.toFixed(2) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="IP地址" v-if="selectedDevice.ipAddress">
            {{ selectedDevice.ipAddress }}
          </el-descriptions-item>
          <el-descriptions-item label="固件版本" v-if="selectedDevice.firmwareVersion">
            {{ selectedDevice.firmwareVersion }}
          </el-descriptions-item>
          <el-descriptions-item label="上线时间" v-if="selectedDevice.activeTime">
            {{ formatDateTime(selectedDevice.activeTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="离线时间" v-if="selectedDevice.inactiveTime">
            {{ formatDateTime(selectedDevice.inactiveTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <el-button @click="deviceDialogVisible = false">关闭</el-button>
        <slot name="device-actions" :device="selectedDevice"></slot>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { DeviceApi } from '@/api/iot/device/device'
import * as ProductApi from '@/api/iot/product/product'  // 🆕 添加产品API
import * as FloorDxfApi from '@/api/iot/spatial/floorDxf'
import { convertDxfToSvgWithBackendScale } from '@/utils/dxf/dxfToSvg'
import { formatDate } from '@/utils/formatTime'
import { getIconConfigByProductName } from '@/assets/floorplan-icons'  // 🆕 添加图标配置

/**
 * 可复用的楼层电子地图组件
 * 
 * 功能：
 * 1. 显示DXF平面图背景（使用前端dxf-parser，无水印）
 * 2. 显示设备标记（根据坐标动态渲染）
 * 3. 点击设备显示详情弹窗
 * 4. 支持自定义设备操作按钮（通过slot）
 * 
 * 使用示例：
 * <FloorMap 
 *   :floor-id="currentFloorId" 
 *   @device-click="handleDeviceClick"
 * >
 *   <template #device-actions="{ device }">
 *     <el-button type="primary" @click="handlePreview(device)">实时预览</el-button>
 *   </template>
 * </FloorMap>
 */

defineOptions({ name: 'FloorMap' })

interface Props {
  floorId?: number  // 楼层ID
  showOfflineDevices?: boolean  // 是否显示离线设备
  placeholderText?: string  // 占位文字
}

const props = withDefaults(defineProps<Props>(), {
  showOfflineDevices: true,
  placeholderText: '请选择楼层或上传DXF文件'
})

const emit = defineEmits(['device-click', 'device-hover', 'load-complete'])

// ==================== 状态管理 ====================
const floorPlanSvg = ref<string>('')
const deviceMarkers = ref<any[]>([])
const productList = ref<any[]>([])  // 🆕 产品列表（用于匹配设备图标）
const selectedDevice = ref<any>(null)
const deviceDialogVisible = ref(false)
const backendCoordParams = ref({
  coordinateScale: 0,
  dxfOffsetX: 0,
  dxfOffsetY: 0,
  svgWidth: 1920,
  svgHeight: 1080
})

// ==================== 加载数据 ====================

/** 加载楼层平面图（使用前端 dxf-parser） */
const loadFloorPlan = async (floorId: number) => {
  try {
    console.info('[FloorMap] 🎨 加载楼层平面图:', floorId)
    
    // 1. 获取坐标比例
    let coordinateScale = 0
    try {
      const infoRes = await FloorDxfApi.getDxfInfo(floorId)
      const info = infoRes.data || infoRes
      coordinateScale = info.coordinateScale || 37.55
      console.info('[FloorMap] 📐 坐标比例:', coordinateScale, '像素/米')
    } catch (error: any) {
      console.warn('[FloorMap] ⚠️ 获取坐标参数失败:', error.message)
      coordinateScale = 37.55
    }
    
    // 2. 获取图层列表
    let selectedLayers: string[] = []
    try {
      const layersRes = await FloorDxfApi.getLayers(floorId)
      const layerData = layersRes.data || layersRes
      const layers = layerData.layers || []
      selectedLayers = layers
        .filter((layer: any) => layer.isVisible)
        .map((layer: any) => layer.name)
    } catch (error: any) {
      console.warn('[FloorMap] ⚠️ 获取图层失败:', error.message)
      selectedLayers = []
    }
    
    // 3. 获取 DXF 内容
    const dxfContent = await FloorDxfApi.getDxfFileContent(floorId)
    
    if (!dxfContent || dxfContent.length === 0) {
      console.warn('[FloorMap] ⚠️ DXF 内容为空')
      floorPlanSvg.value = ''
      return
    }
    
    // 4. 使用前端 dxf-parser 生成 SVG
    const result = convertDxfToSvgWithBackendScale(
      dxfContent,
      selectedLayers,
      coordinateScale,
      1920,
      1080
    )
    
    if (result && result.svg) {
      floorPlanSvg.value = result.svg
      backendCoordParams.value = {
        coordinateScale: coordinateScale,
        dxfOffsetX: result.dxfOffsetX || 0,
        dxfOffsetY: result.dxfOffsetY || 0,
        svgWidth: 1920,
        svgHeight: 1080
      }
      console.info('[FloorMap] ✅ 平面图加载成功（无水印）')
    } else {
      console.error('[FloorMap] ❌ DXF 解析失败')
      floorPlanSvg.value = ''
    }
  } catch (error: any) {
    if (error.message?.includes('没有绑定DXF文件')) {
      console.info('[FloorMap] ℹ️ 该楼层暂无平面图')
    } else {
      console.error('[FloorMap] ❌ 加载平面图失败:', error)
    }
    floorPlanSvg.value = ''
  }
}

/** 加载楼层设备（带坐标转换） */
const loadFloorDevices = async (floorId: number) => {
  try {
    console.info('[FloorMap] 📍 加载楼层设备:', floorId)
    
    // 分页获取所有设备
    let allDevices: any[] = []
    let pageNo = 1
    const pageSize = 100
    let hasMore = true
    
    while (hasMore) {
      const response = await DeviceApi.getDevicePage({
        floorId: floorId,
        pageNo: pageNo,
        pageSize: pageSize
      })
      
      const list = response?.list || []
      allDevices.push(...list)
      
      hasMore = list.length >= pageSize
      pageNo++
    }
    
    console.info('[FloorMap] 📦 获取到', allDevices.length, '个设备')
    
    // 过滤有坐标的设备
    let devicesWithCoords = allDevices.filter(device => 
      device.localX != null && device.localY != null
    )
    
    // 是否显示离线设备
    if (!props.showOfflineDevices) {
      devicesWithCoords = devicesWithCoords.filter(device => device.state === 1)
    }
    
    console.info('[FloorMap] 🎯 其中', devicesWithCoords.length, '个设备有坐标')
    
    // 坐标转换：DXF米 → SVG百分比
    const { coordinateScale, dxfOffsetX, dxfOffsetY, svgWidth, svgHeight } = backendCoordParams.value
    
    if (coordinateScale === 0) {
      console.warn('[FloorMap] ⚠️ 坐标比例为0，无法转换坐标')
      deviceMarkers.value = []
      return
    }
    
    deviceMarkers.value = devicesWithCoords.map(device => {
      // 🔄 坐标转换：DXF坐标(米) → SVG百分比坐标
      // 🎯 完全匹配 entitiesToSvgDirect 中的坐标转换逻辑
      
      // 步骤1：DXF米 → 像素
      const pixelX = device.localX * coordinateScale
      const pixelY = device.localY * coordinateScale
      
      // 步骤2 & 3：应用偏移和Y轴翻转（与entitiesToSvgDirect完全一致）
      // 🔑 SVG生成时的公式：
      //    tx = (x: number) => x + offsetX
      //    ty = (y: number) => height - (y + offsetY)
      const svgX = pixelX + dxfOffsetX
      const svgY = svgHeight - (pixelY + dxfOffsetY)
      
      // 步骤4：转换为百分比
      const xPercent = (svgX / svgWidth) * 100
      const yPercent = (svgY / svgHeight) * 100
      
      return {
        ...device,
        x: xPercent,
        y: yPercent,
        status: device.state || 0  // 0=离线, 1=在线
      }
    })
    
    console.info('[FloorMap] ✅ 设备标记已生成:', deviceMarkers.value.length, '个')
    emit('load-complete', { devices: deviceMarkers.value, svg: floorPlanSvg.value })
  } catch (error: any) {
    console.error('[FloorMap] ❌ 加载设备失败:', error)
    deviceMarkers.value = []
  }
}

/** 加载完整数据 */
const loadData = async () => {
  if (!props.floorId) {
    floorPlanSvg.value = ''
    deviceMarkers.value = []
    return
  }
  
  // 🆕 加载产品列表（用于设备图标匹配）
  if (productList.value.length === 0) {
    try {
      const data = await ProductApi.getSimpleProductList()
      productList.value = data || []
      console.log('[FloorMap] 📦 加载产品列表:', productList.value.length, '个')
    } catch (error) {
      console.error('[FloorMap] ❌ 加载产品列表失败:', error)
    }
  }
  
  await loadFloorPlan(props.floorId)
  await loadFloorDevices(props.floorId)
}

// ==================== 设备交互 ====================

/** 点击设备 */
const handleDeviceClick = (device: any) => {
  selectedDevice.value = device
  deviceDialogVisible.value = true
  emit('device-click', device)
}

/** 悬停设备 */
const handleDeviceHover = (device: any, event: MouseEvent) => {
  emit('device-hover', device, event)
}

/** 离开设备 */
const handleDeviceLeave = () => {
  // 可以在这里添加 tooltip 隐藏逻辑
}

// ==================== 样式和图标 ====================

/** 获取标记样式 */
const getMarkerStyle = (device: any) => {
  return {
    left: device.x + '%',
    top: device.y + '%'
  }
}

/** 获取标记class */
const getMarkerClass = (device: any) => {
  const classes: string[] = []
  
  // 状态
  if (device.status === 1) {
    classes.push('marker-online')
  } else {
    classes.push('marker-offline')
  }
  
  // 设备类型
  if (device.productName?.includes('摄像')) {
    classes.push('marker-camera')
  }
  
  return classes.join(' ')
}

/** 
 * 🆕 获取设备图标（优先使用产品图标）
 * 
 * @param device 设备对象（包含 productId、productName）
 * @returns 图标名称
 */
const getDeviceIcon = (device: any) => {
  // 🎯 优先级1：如果设备有产品ID，从产品列表查找（基于统一的图标配置）
  if (device && device.productId && productList.value.length > 0) {
    const product = productList.value.find(p => p.id === device.productId)
    if (product) {
      // 🔑 从统一的图标配置中获取图标
      const iconConfig = getIconConfigByProductName(product.name)
      
      // 优先使用产品表中的 icon 字段（Element Plus 图标）
      if (product.icon) {
        return product.icon
      }
      
      // 降级：使用图标配置（需要映射到 Element Plus 图标）
      if (iconConfig) {
        // 这里可以根据图标配置的 key 映射到 Element Plus 图标
        const iconKeyMap: any = {
          '枪型摄像机': 'ep:camera',
          '半球摄像机': 'ep:video-camera',
          '球形摄像机': 'ep:camera-filled',
          '人脸识别一体机': 'ep:user',
          '人行闸机': 'ep:lock',
          '车辆道闸': 'ep:unlock',
          '车辆识别一体机': 'ep:postcard',
          '巡更点': 'ep:location',
          '水表': 'ep:water',
          '电表': 'ep:lightning',
          '燃气表': 'ep:hot-water',
          '考勤机': 'ep:calendar'
        }
        return iconKeyMap[iconConfig.key] || 'ep:position'
      }
    }
  }
  
  // 🎯 优先级2：使用产品名称推断图标
  const productName = device.productName || ''
  
  // 摄像头
  if (productName.includes('摄像') || productName.includes('camera') || productName.includes('半球')) {
    if (productName.includes('枪型') || productName.includes('bullet')) {
      return 'ep:video-camera'
    }
    if (productName.includes('半球') || productName.includes('dome')) {
      return 'ep:camera-filled'
    }
    return 'ep:camera'
  }
  
  // 门禁
  if (productName.includes('门禁') || productName.includes('access')) {
    return 'ep:lock'
  }
  
  // 人脸识别
  if (productName.includes('人脸') || productName.includes('face')) {
    return 'ep:user'
  }
  
  // 道闸
  if (productName.includes('道闸') || productName.includes('gate')) {
    return 'ep:unlock'
  }
  
  // 传感器
  if (productName.includes('传感') || productName.includes('sensor')) {
    return 'ep:odometer'
  }
  
  // 烟感
  if (productName.includes('烟感') || productName.includes('smoke') || productName.includes('火灾')) {
    return 'ep:smoking'
  }
  
  // 默认
  return 'ep:position'
}

/** 获取设备类型文字 */
const getDeviceTypeText = (deviceType: number) => {
  const typeMap: Record<number, string> = {
    0: '直连设备',
    1: '网关子设备',
    2: '网关设备'
  }
  return typeMap[deviceType] || '未知'
}

/** 格式化时间 */
const formatDateTime = (dateTime: any) => {
  if (!dateTime) return '-'
  const date = typeof dateTime === 'string' ? new Date(dateTime) : dateTime
  return formatDate(date)
}

// ==================== 生命周期 ====================

// 监听 floorId 变化
watch(() => props.floorId, () => {
  loadData()
}, { immediate: true })

defineExpose({ loadData, selectedDevice })
</script>

<style scoped lang="scss">
.floor-map-container {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 500px;
  max-height: 100%; // 限制最大高度，防止溢出
  background: linear-gradient(135deg, #1a1f35 0%, #16213e 100%);  // 🎨 深蓝科技风
  border-radius: 8px;
  border: 1px solid rgba(74, 144, 226, 0.2);  // 🎨 科技蓝边框
  box-shadow: 0 4px 20px rgba(74, 144, 226, 0.1);  // 🎨 淡蓝光晕
  overflow: hidden;
  box-sizing: border-box; // 确保包含边框和内边距
  display: flex;
  align-items: center;
  justify-content: center;

  .floor-plan-background {
    // 🎯 保持SVG原始宽高比 (1920:1080 = 16:9)
    width: 100%;
    max-width: 100%;
    max-height: 100%; // 限制最大高度，防止溢出
    aspect-ratio: 1920 / 1080;
    position: relative;
    opacity: 0.8;
    overflow: hidden; // 防止内容溢出
    
    :deep(svg) {
      width: 100%;
      height: 100%;
      max-width: 100%;
      max-height: 100%;
      display: block; // 移除 SVG 默认的 inline 特性
    }
  }

  .floor-plan-placeholder {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
    color: #666;
    
    p {
      margin-top: 16px;
      font-size: 14px;
    }
  }

  .device-marker {
    position: absolute;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    transform: translate(-50%, -50%);
    cursor: pointer;
    z-index: 100;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;

    .el-icon {
      font-size: 18px;
      filter: drop-shadow(0 0 4px currentColor);
    }

    // 🎨 脉冲动画光晕（渐变扩散）
    .marker-pulse {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 100%;
      height: 100%;
      border-radius: 50%;
      background: radial-gradient(circle, currentColor 0%, transparent 70%);
      opacity: 0.6;
      animation: pulse-wave 2s ease-out infinite;
      pointer-events: none;
    }

    &:hover {
      transform: translate(-50%, -50%) scale(1.3);
      z-index: 101;
    }

    // 在线状态 - 🎨 科技绿（嵌套光晕）
    &.marker-online {
      background: rgba(0, 255, 136, 0.3);
      box-shadow: 
        0 0 0 2px #00ff88,
        0 0 0 4px rgba(0, 255, 136, 0.3),
        0 0 20px rgba(0, 255, 136, 0.4);
      
      .el-icon {
        color: #00ff88;
      }
    }

    // 离线状态 - 🎨 暗红（嵌套光晕，无动画）
    &.marker-offline {
      background: rgba(255, 107, 107, 0.3);
      box-shadow: 
        0 0 0 2px #ff6b6b,
        0 0 0 4px rgba(255, 107, 107, 0.3),
        0 0 10px rgba(255, 107, 107, 0.2);
      
      .el-icon {
        color: #ff6b6b;
      }
      
      .marker-pulse {
        animation: none;  // ❌ 离线设备不显示脉冲
      }
    }

    // 摄像头特殊样式 - 🎨 科技蓝（嵌套光晕）
    &.marker-camera.marker-online {
      background: rgba(74, 144, 226, 0.3);
      box-shadow: 
        0 0 0 2px #4a90e2,
        0 0 0 4px rgba(74, 144, 226, 0.3),
        0 0 20px rgba(74, 144, 226, 0.4);
      
      .el-icon {
        color: #4a90e2;
      }
    }

    // 悬停增强光晕
    &.marker-online:hover {
      box-shadow: 
        0 0 0 2px currentColor,
        0 0 0 6px rgba(0, 255, 136, 0.4),
        0 0 30px rgba(0, 255, 136, 0.6),
        0 0 50px rgba(0, 255, 136, 0.3);
    }

    &.marker-camera.marker-online:hover {
      box-shadow: 
        0 0 0 2px currentColor,
        0 0 0 6px rgba(74, 144, 226, 0.4),
        0 0 30px rgba(74, 144, 226, 0.6),
        0 0 50px rgba(74, 144, 226, 0.3);
    }
  }

  .device-details {
    max-height: 500px;
    overflow-y: auto;
  }
}

// 🎨 渐变扩散脉冲动画（嵌套光晕效果）
@keyframes pulse-wave {
  0% {
    transform: translate(-50%, -50%) scale(0.8);
    opacity: 0.8;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.5);
    opacity: 0.3;
  }
  100% {
    transform: translate(-50%, -50%) scale(2);
    opacity: 0;
  }
}
</style>

