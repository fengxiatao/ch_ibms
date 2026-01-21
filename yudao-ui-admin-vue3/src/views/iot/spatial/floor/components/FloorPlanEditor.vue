<template>
  <div class="floor-plan-editor">
    <!-- 工具栏 -->
    <div class="editor-toolbar">
      <el-button-group>
        <el-button 
          :type="tool === 'select' ? 'primary' : ''" 
          @click="setTool('select')"
          size="small"
        >
          <Icon icon="ep:pointer" class="mr-5px" />
          选择
        </el-button>
        <el-button 
          :type="tool === 'pan' ? 'primary' : ''" 
          @click="setTool('pan')"
          size="small"
        >
          <Icon icon="ep:rank" class="mr-5px" />
          平移
        </el-button>
      </el-button-group>

      <el-divider direction="vertical" />

      <el-button-group>
        <el-button @click="zoomIn" size="small">
          <Icon icon="ep:zoom-in" />
        </el-button>
        <el-button @click="zoomOut" size="small">
          <Icon icon="ep:zoom-out" />
        </el-button>
        <el-button @click="zoomReset" size="small">
          100%
        </el-button>
      </el-button-group>

      <el-divider direction="vertical" />

      <el-button-group>
        <el-button @click="undo" :disabled="!canUndo" size="small">
          <Icon icon="ep:back" class="mr-5px" />
          撤销
        </el-button>
        <el-button @click="redo" :disabled="!canRedo" size="small">
          <Icon icon="ep:right" class="mr-5px" />
          重做
        </el-button>
      </el-button-group>

      <el-divider direction="vertical" />

      <el-button type="success" @click="saveFloorPlan" :loading="saving" size="small">
        <Icon icon="ep:document-checked" class="mr-5px" />
        保存
      </el-button>
    </div>

    <!-- 主内容区 -->
    <div class="editor-content">
      <!-- 左侧：画布区域 -->
      <div class="canvas-container">
        <canvas id="floor-plan-canvas"></canvas>
        
        <!-- 缩放显示 -->
        <div class="zoom-display">
          {{ Math.round(zoomLevel * 100) }}%
        </div>

        <!-- 坐标显示 -->
        <div class="coordinate-display" v-if="mousePosition">
          X: {{ mousePosition.x.toFixed(2) }}m, Y: {{ mousePosition.y.toFixed(2) }}m
        </div>
      </div>

      <!-- 右侧：属性面板 -->
      <div class="properties-panel">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>设备属性</span>
              <el-button 
                v-if="selectedDevice" 
                link 
                type="danger" 
                @click="deleteSelectedDevice"
              >
                <Icon icon="ep:delete" />
              </el-button>
            </div>
          </template>

          <!-- 未选中设备时 -->
          <el-empty 
            v-if="!selectedDevice" 
            description="请在画布中选择设备"
            :image-size="100"
          />

          <!-- 已选中设备时 -->
          <div v-else class="device-properties">
            <!-- 设备基本信息 -->
            <el-descriptions :column="1" border>
              <el-descriptions-item label="设备ID">
                {{ selectedDevice.deviceId }}
              </el-descriptions-item>
              <el-descriptions-item label="设备名称">
                <el-input 
                  v-model="selectedDevice.deviceName" 
                  size="small"
                  @change="updateDeviceProperty('name', selectedDevice.deviceName)"
                />
              </el-descriptions-item>
              <el-descriptions-item label="设备类型">
                {{ getDeviceTypeName(selectedDevice.deviceType) }}
              </el-descriptions-item>
            </el-descriptions>

            <el-divider />

            <!-- 设备图标选择 -->
            <div class="property-section">
              <div class="section-title">设备图标</div>
              <el-select 
                v-model="selectedDevice.icon" 
                @change="updateDeviceIcon"
                size="small"
                style="width: 100%"
              >
                <el-option
                  v-for="icon in deviceIconOptions"
                  :key="icon.value"
                  :label="icon.label"
                  :value="icon.value"
                >
                  <Icon :icon="icon.value" class="mr-5px" />
                  {{ icon.label }}
                </el-option>
              </el-select>

              <!-- 图标颜色 -->
              <div style="margin-top: 10px;">
                <el-text size="small">图标颜色</el-text>
                <el-color-picker 
                  v-model="selectedDevice.color" 
                  @change="updateDeviceColor"
                  size="small"
                  style="margin-left: 10px;"
                />
              </div>

              <!-- 图标大小 -->
              <div style="margin-top: 10px;">
                <el-text size="small">图标大小</el-text>
                <el-slider 
                  v-model="selectedDevice.iconSize" 
                  :min="10" 
                  :max="50"
                  @change="updateDeviceIconSize"
                  style="margin-left: 10px;"
                />
              </div>
            </div>

            <el-divider />

            <!-- 位置坐标 -->
            <div class="property-section">
              <div class="section-title">
                位置坐标
                <el-tooltip content="单位：米" placement="top">
                  <Icon icon="ep:question-filled" class="ml-5px" />
                </el-tooltip>
              </div>

              <el-form label-width="40px" size="small">
                <el-form-item label="X:">
                  <el-input-number
                    v-model="selectedDevice.x"
                    :precision="2"
                    :step="0.1"
                    @change="updateDevicePosition"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="Y:">
                  <el-input-number
                    v-model="selectedDevice.y"
                    :precision="2"
                    :step="0.1"
                    @change="updateDevicePosition"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="Z:">
                  <el-input-number
                    v-model="selectedDevice.z"
                    :precision="2"
                    :step="0.1"
                    @change="updateDevicePosition"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-form>

              <!-- 快捷定位按钮 -->
              <div style="margin-top: 10px;">
                <el-button size="small" @click="centerDevice" style="width: 100%">
                  <Icon icon="ep:aim" class="mr-5px" />
                  居中显示
                </el-button>
              </div>
            </div>

            <el-divider />

            <!-- 设备状态 -->
            <div class="property-section">
              <div class="section-title">设备状态</div>
              <el-radio-group 
                v-model="selectedDevice.status" 
                @change="updateDeviceStatus"
                size="small"
              >
                <el-radio :label="1">在线</el-radio>
                <el-radio :label="0">离线</el-radio>
              </el-radio-group>
            </div>

            <el-divider />

            <!-- 操作按钮 -->
            <div class="property-actions">
              <el-button 
                type="primary" 
                size="small" 
                @click="saveDeviceProperties"
                :loading="savingDevice"
                style="width: 100%"
              >
                <Icon icon="ep:check" class="mr-5px" />
                保存修改
              </el-button>
              <el-button 
                size="small" 
                @click="deselectDevice"
                style="width: 100%; margin-top: 10px;"
              >
                取消选择
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fabric } from 'fabric'
import request from '@/config/axios'

const props = defineProps<{
  floorId: number
  svgData?: string  // DXF 转换的 SVG 背景图
  devices?: any[]   // 设备列表
  coordinateScale?: number  // 坐标比例（像素/米）
}>()

const emit = defineEmits(['success', 'device-updated'])

// 画布实例
const canvas = ref<fabric.Canvas | null>(null)
const tool = ref('select')
const zoomLevel = ref(1)
const mousePosition = ref<{ x: number; y: number } | null>(null)
const coordinateScale = ref(props.coordinateScale || 38.02)

// 历史记录
const history = ref<any[]>([])
const historyStep = ref(0)
const canUndo = ref(false)
const canRedo = ref(false)

// 选中的设备
const selectedDevice = ref<any>(null)
const savingDevice = ref(false)
const saving = ref(false)

// 设备图标选项
const deviceIconOptions = [
  { label: '摄像头', value: 'ep:camera' },
  { label: '门禁', value: 'ep:lock' },
  { label: '传感器', value: 'ep:cpu' },
  { label: '灯光', value: 'ep:sunny' },
  { label: '空调', value: 'ep:wind-power' },
  { label: '电梯', value: 'ep:more-filled' },
  { label: '停车', value: 'ep:parking' },
  { label: '消防', value: 'ep:warning' },
  { label: '报警', value: 'ep:bell' },
  { label: '通用', value: 'ep:setting' }
]

/**
 * 初始化画布
 */
onMounted(() => {
  initCanvas()
  loadSVGBackground()
  loadDevices()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  canvas.value?.dispose()
})

/**
 * 初始化 Fabric.js 画布
 */
const initCanvas = () => {
  canvas.value = new fabric.Canvas('floor-plan-canvas', {
    width: 1000,
    height: 700,
    backgroundColor: '#f5f5f5',
    selection: true
  })

  // 监听对象选中事件
  canvas.value.on('selection:created', handleObjectSelected)
  canvas.value.on('selection:updated', handleObjectSelected)
  canvas.value.on('selection:cleared', handleObjectDeselected)

  // 监听对象移动事件
  canvas.value.on('object:modified', handleObjectModified)

  // 监听鼠标移动（显示坐标）
  canvas.value.on('mouse:move', handleMouseMove)

  // 监听历史记录
  canvas.value.on('object:modified', saveHistory)
  canvas.value.on('object:added', saveHistory)
  canvas.value.on('object:removed', saveHistory)
}

/**
 * 加载 SVG 背景图
 */
const loadSVGBackground = () => {
  if (!props.svgData || !canvas.value) return

  fabric.loadSVGFromString(props.svgData, (objects, options) => {
    const obj = fabric.util.groupSVGElements(objects, options)
    obj.set({
      selectable: false,
      evented: false,
      objectType: 'background'
    })
    canvas.value?.add(obj)
    canvas.value?.sendToBack(obj)
    canvas.value?.requestRenderAll()
  })
}

/**
 * 加载设备图标
 */
const loadDevices = () => {
  if (!props.devices || !canvas.value) return

  props.devices.forEach(device => {
    addDeviceToCanvas(device)
  })
}

/**
 * 添加设备到画布
 */
const addDeviceToCanvas = (device: any) => {
  if (!canvas.value) return

  const x = (device.localX || 0) * coordinateScale.value
  const y = (device.localY || 0) * coordinateScale.value
  const iconSize = device.deviceIconSize || 20
  const color = getDeviceColor(device.status)

  // 创建设备图标（圆形）
  const circle = new fabric.Circle({
    left: x,
    top: y,
    radius: iconSize / 2,
    fill: color,
    stroke: '#333',
    strokeWidth: 2,
    originX: 'center',
    originY: 'center'
  })

  // 创建设备名称标签
  const label = new fabric.Text(device.deviceName || device.name || '未命名', {
    left: x,
    top: y + iconSize / 2 + 5,
    fontSize: 12,
    fill: '#333',
    originX: 'center',
    originY: 'top',
    selectable: false
  })

  // 分组
  const group = new fabric.Group([circle, label], {
    left: x,
    top: y,
    originX: 'center',
    originY: 'center',
    hasControls: true,
    hasBorders: true,
    // 自定义属性
    objectType: 'device',
    deviceId: device.id,
    deviceData: {
      ...device,
      icon: device.deviceIcon || 'ep:camera',
      color: color,
      iconSize: iconSize,
      x: device.localX || 0,
      y: device.localY || 0,
      z: device.localZ || 0
    }
  } as any)

  canvas.value.add(group)
}

/**
 * 处理对象选中
 */
const handleObjectSelected = (e: any) => {
  const obj = e.selected?.[0] || e.target
  
  if (obj && obj.objectType === 'device') {
    const deviceData = obj.deviceData
    selectedDevice.value = {
      deviceId: obj.deviceId,
      deviceName: deviceData.deviceName || deviceData.name,
      deviceType: deviceData.deviceType,
      icon: deviceData.icon,
      color: deviceData.color,
      iconSize: deviceData.iconSize,
      x: deviceData.x,
      y: deviceData.y,
      z: deviceData.z,
      status: deviceData.status || 1,
      canvasObject: obj
    }
  }
}

/**
 * 处理对象取消选中
 */
const handleObjectDeselected = () => {
  selectedDevice.value = null
}

/**
 * 处理对象移动
 */
const handleObjectModified = (e: any) => {
  const obj = e.target
  
  if (obj && obj.objectType === 'device') {
    // 更新设备坐标
    const newX = obj.left / coordinateScale.value
    const newY = obj.top / coordinateScale.value
    
    obj.deviceData.x = newX
    obj.deviceData.y = newY
    
    if (selectedDevice.value && selectedDevice.value.deviceId === obj.deviceId) {
      selectedDevice.value.x = newX
      selectedDevice.value.y = newY
    }
  }
}

/**
 * 处理鼠标移动（显示坐标）
 */
const handleMouseMove = (e: any) => {
  const pointer = canvas.value?.getPointer(e.e)
  if (pointer) {
    mousePosition.value = {
      x: pointer.x / coordinateScale.value,
      y: pointer.y / coordinateScale.value
    }
  }
}

/**
 * 更新设备图标
 */
const updateDeviceIcon = () => {
  if (!selectedDevice.value || !canvas.value) return

  const obj = selectedDevice.value.canvasObject
  obj.deviceData.icon = selectedDevice.value.icon
  
  // 可以在这里更新图标显示
  canvas.value.requestRenderAll()
}

/**
 * 更新设备颜色
 */
const updateDeviceColor = () => {
  if (!selectedDevice.value || !canvas.value) return

  const obj = selectedDevice.value.canvasObject
  const circle = obj._objects[0]  // 第一个对象是圆形
  
  circle.set('fill', selectedDevice.value.color)
  obj.deviceData.color = selectedDevice.value.color
  
  canvas.value.requestRenderAll()
}

/**
 * 更新设备图标大小
 */
const updateDeviceIconSize = () => {
  if (!selectedDevice.value || !canvas.value) return

  const obj = selectedDevice.value.canvasObject
  const circle = obj._objects[0]
  
  circle.set('radius', selectedDevice.value.iconSize / 2)
  obj.deviceData.iconSize = selectedDevice.value.iconSize
  
  canvas.value.requestRenderAll()
}

/**
 * 更新设备位置（从数字输入）
 */
const updateDevicePosition = () => {
  if (!selectedDevice.value || !canvas.value) return

  const obj = selectedDevice.value.canvasObject
  
  // 计算新的画布坐标
  const newLeft = selectedDevice.value.x * coordinateScale.value
  const newTop = selectedDevice.value.y * coordinateScale.value
  
  // 更新画布对象位置
  obj.set({
    left: newLeft,
    top: newTop
  })
  
  // 更新设备数据
  obj.deviceData.x = selectedDevice.value.x
  obj.deviceData.y = selectedDevice.value.y
  obj.deviceData.z = selectedDevice.value.z
  
  canvas.value.requestRenderAll()
  canvas.value.setActiveObject(obj)
}

/**
 * 更新设备状态
 */
const updateDeviceStatus = () => {
  if (!selectedDevice.value || !canvas.value) return

  const obj = selectedDevice.value.canvasObject
  obj.deviceData.status = selectedDevice.value.status
  
  // 更新颜色
  const newColor = getDeviceColor(selectedDevice.value.status)
  selectedDevice.value.color = newColor
  updateDeviceColor()
}

/**
 * 居中显示设备
 */
const centerDevice = () => {
  if (!selectedDevice.value || !canvas.value) return

  const obj = selectedDevice.value.canvasObject
  canvas.value.viewportCenterObject(obj)
  canvas.value.requestRenderAll()
}

/**
 * 取消选择设备
 */
const deselectDevice = () => {
  if (!canvas.value) return
  canvas.value.discardActiveObject()
  canvas.value.requestRenderAll()
}

/**
 * 删除选中的设备
 */
const deleteSelectedDevice = async () => {
  if (!selectedDevice.value || !canvas.value) return

  try {
    await ElMessageBox.confirm('确定要删除该设备吗？', '警告', {
      type: 'warning'
    })

    const obj = selectedDevice.value.canvasObject
    canvas.value.remove(obj)
    canvas.value.requestRenderAll()
    
    selectedDevice.value = null
    ElMessage.success('设备已删除')
  } catch {
    // 用户取消
  }
}

/**
 * 保存设备属性
 */
const saveDeviceProperties = async () => {
  if (!selectedDevice.value) return

  savingDevice.value = true
  try {
    await request.put({
      url: '/admin-api/iot/device/update',
      data: {
        id: selectedDevice.value.deviceId,
        deviceName: selectedDevice.value.deviceName,
        deviceIcon: selectedDevice.value.icon,
        deviceIconSize: selectedDevice.value.iconSize,
        localX: selectedDevice.value.x,
        localY: selectedDevice.value.y,
        localZ: selectedDevice.value.z,
        status: selectedDevice.value.status
      }
    })

    ElMessage.success('设备属性已保存')
    emit('device-updated', selectedDevice.value)
  } catch (error: any) {
    ElMessage.error('保存失败: ' + (error.message || '未知错误'))
  } finally {
    savingDevice.value = false
  }
}

/**
 * 保存平面图
 */
const saveFloorPlan = async () => {
  if (!canvas.value) return

  saving.value = true
  try {
    // 提取所有设备数据
    const devices: any[] = []
    canvas.value.getObjects().forEach((obj: any) => {
      if (obj.objectType === 'device') {
        devices.push({
          id: obj.deviceId,
          localX: obj.deviceData.x,
          localY: obj.deviceData.y,
          localZ: obj.deviceData.z,
          deviceIcon: obj.deviceData.icon,
          deviceIconSize: obj.deviceData.iconSize
        })
      }
    })

    // 保存到后端
    await request.post({
      url: '/admin-api/iot/floor/save-floor-plan',
      data: {
        floorId: props.floorId,
        planData: canvas.value.toJSON(['objectType', 'deviceId', 'deviceData']),
        devices: devices
      }
    })

    ElMessage.success('平面图已保存')
    emit('success')
  } catch (error: any) {
    ElMessage.error('保存失败: ' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

/**
 * 工具切换
 */
const setTool = (newTool: string) => {
  tool.value = newTool
  
  if (!canvas.value) return
  
  if (newTool === 'pan') {
    canvas.value.selection = false
    canvas.value.forEachObject((obj: any) => {
      if (obj.objectType !== 'background') {
        obj.selectable = false
        obj.evented = false
      }
    })
  } else {
    canvas.value.selection = true
    canvas.value.forEachObject((obj: any) => {
      if (obj.objectType !== 'background') {
        obj.selectable = true
        obj.evented = true
      }
    })
  }
}

/**
 * 缩放控制
 */
const zoomIn = () => {
  if (!canvas.value) return
  const newZoom = Math.min(zoomLevel.value * 1.2, 5)
  canvas.value.setZoom(newZoom)
  zoomLevel.value = newZoom
  canvas.value.requestRenderAll()
}

const zoomOut = () => {
  if (!canvas.value) return
  const newZoom = Math.max(zoomLevel.value * 0.8, 0.1)
  canvas.value.setZoom(newZoom)
  zoomLevel.value = newZoom
  canvas.value.requestRenderAll()
}

const zoomReset = () => {
  if (!canvas.value) return
  canvas.value.setZoom(1)
  zoomLevel.value = 1
  canvas.value.requestRenderAll()
}

/**
 * 历史记录
 */
const saveHistory = () => {
  if (!canvas.value) return
  
  const json = canvas.value.toJSON(['objectType', 'deviceId', 'deviceData'])
  history.value = history.value.slice(0, historyStep.value + 1)
  history.value.push(json)
  historyStep.value++
  
  updateHistoryButtons()
}

const undo = () => {
  if (!canUndo.value || !canvas.value) return
  
  historyStep.value--
  const json = history.value[historyStep.value]
  canvas.value.loadFromJSON(json, () => {
    canvas.value?.requestRenderAll()
  })
  
  updateHistoryButtons()
}

const redo = () => {
  if (!canRedo.value || !canvas.value) return
  
  historyStep.value++
  const json = history.value[historyStep.value]
  canvas.value.loadFromJSON(json, () => {
    canvas.value?.requestRenderAll()
  })
  
  updateHistoryButtons()
}

const updateHistoryButtons = () => {
  canUndo.value = historyStep.value > 0
  canRedo.value = historyStep.value < history.value.length - 1
}

/**
 * 窗口大小调整
 */
const handleResize = () => {
  // 可以在这里调整画布大小
}

/**
 * 辅助函数
 */
const getDeviceColor = (status: number) => {
  return status === 1 ? '#67C23A' : '#909399'
}

const getDeviceTypeName = (type: number) => {
  const types = {
    0: '直连设备',
    1: '网关子设备',
    2: '网关设备'
  }
  return types[type] || '未知'
}

defineExpose({
  canvas,
  saveFloorPlan,
  addDeviceToCanvas
})
</script>

<style scoped lang="scss">
.floor-plan-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;

  .editor-toolbar {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    background: #f5f7fa;
    border-bottom: 1px solid #dcdfe6;
  }

  .editor-content {
    display: flex;
    flex: 1;
    overflow: hidden;

    .canvas-container {
      flex: 1;
      position: relative;
      background: #e5e7eb;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;

      canvas {
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
      }

      .zoom-display {
        position: absolute;
        bottom: 16px;
        right: 16px;
        padding: 4px 12px;
        background: rgba(0, 0, 0, 0.7);
        color: #fff;
        border-radius: 4px;
        font-size: 12px;
      }

      .coordinate-display {
        position: absolute;
        top: 16px;
        left: 16px;
        padding: 4px 12px;
        background: rgba(0, 0, 0, 0.7);
        color: #fff;
        border-radius: 4px;
        font-size: 12px;
      }
    }

    .properties-panel {
      width: 350px;
      border-left: 1px solid #dcdfe6;
      overflow-y: auto;
      background: #f5f7fa;

      .el-card {
        height: 100%;
        border: none;
        border-radius: 0;

        .card-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
        }
      }

      .device-properties {
        .property-section {
          margin-bottom: 16px;

          .section-title {
            font-size: 14px;
            font-weight: 600;
            margin-bottom: 12px;
            color: #303133;
          }
        }

        .property-actions {
          margin-top: 20px;
        }
      }
    }
  }
}

:deep(.el-descriptions__label) {
  width: 80px;
}
</style>

