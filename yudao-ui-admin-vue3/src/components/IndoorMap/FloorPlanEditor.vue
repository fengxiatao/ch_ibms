<!--
  楼层平面图编辑器（核心组件）
  功能：
  1. 显示楼层平面图、区域、设备
  2. 支持用户点击添加设备
  3. 设备可拖拽
  4. 坐标反向入库
  5. 支持曲线走廊
-->

<template>
  <div class="floor-plan-editor">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <!-- 楼层选择 -->
        <el-select
          v-model="selectedFloorId"
          placeholder="选择楼层"
          style="width: 200px"
          @change="loadFloorData"
        >
          <el-option
            v-for="floor in floors"
            :key="floor.id"
            :label="`${floor.name} (${floor.floorNumber}F)`"
            :value="floor.id"
          >
            <span>{{ floor.name }}</span>
            <span style="color: #8492a6; font-size: 12px; margin-left: 8px">
              {{ floor.areaCount || 0 }} 个区域
            </span>
          </el-option>
        </el-select>

        <el-divider direction="vertical" />

        <!-- 视图控制 -->
        <el-button-group>
          <el-button :icon="ZoomIn" @click="zoomIn">放大</el-button>
          <el-button :icon="ZoomOut" @click="zoomOut">缩小</el-button>
          <el-button :icon="Refresh" @click="resetView">重置</el-button>
        </el-button-group>

        <el-divider direction="vertical" />

        <!-- 图层控制 -->
        <el-checkbox v-model="layerConfig.showAreas" @change="handleLayerChange">
          显示区域
        </el-checkbox>
        <el-checkbox v-model="layerConfig.showDevices" @change="handleLayerChange">
          显示设备
        </el-checkbox>
        <el-checkbox v-model="layerConfig.showLabels" @change="handleLayerChange">
          显示标签
        </el-checkbox>
      </div>

      <div class="toolbar-right">
        <!-- 添加设备模式 -->
        <el-dropdown @command="handleAddDeviceCommand">
          <el-button type="primary" :icon="Plus">
            添加设备
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="LIGHTING">💡 照明设备</el-dropdown-item>
              <el-dropdown-item command="AC">❄️ 空调</el-dropdown-item>
              <el-dropdown-item command="CAMERA">📷 摄像头</el-dropdown-item>
              <el-dropdown-item command="SENSOR">📡 传感器</el-dropdown-item>
              <el-dropdown-item command="DOOR">🚪 门禁</el-dropdown-item>
              <el-dropdown-item command="ALARM">🔔 报警器</el-dropdown-item>
              <el-dropdown-item command="SMOKE_DETECTOR">🚨 烟感</el-dropdown-item>
              <el-dropdown-item command="FIRE">🧯 消防设备</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- 编辑模式切换 -->
        <el-switch
          v-model="editMode"
          active-text="编辑模式"
          inactive-text="查看模式"
          style="margin-left: 12px"
        />

        <!-- 保存按钮 -->
        <el-button
          v-if="hasUnsavedChanges"
          type="success"
          :icon="Check"
          @click="saveChanges"
          :loading="saving"
        >
          保存更改 ({{ unsavedCount }})
        </el-button>
      </div>
    </div>

    <!-- 状态提示 -->
    <div v-if="addDeviceMode" class="add-device-hint">
      <el-alert
        title="添加设备模式"
        :description="`正在添加: ${deviceTypeLabels[pendingDeviceType]} - 点击平面图上的任意位置放置设备`"
        type="info"
        :closable="false"
      >
        <template #default>
          <el-button size="small" @click="cancelAddDevice">取消</el-button>
        </template>
      </el-alert>
    </div>

    <!-- Konva 画布 -->
    <div class="canvas-container">
      <v-stage
        ref="stageRef"
        :config="stageConfig"
        @wheel="handleWheel"
        @click="handleStageClick"
        @mousedown="handleMouseDown"
        @mousemove="handleMouseMove"
        @mouseup="handleMouseUp"
      >
        <!-- 背景层 -->
        <v-layer>
          <!-- 网格 -->
          <v-line
            v-for="(line, idx) in gridLines"
            :key="`grid-${idx}`"
            :config="{
              points: line,
              stroke: '#e0e0e0',
              strokeWidth: 1,
              dash: [5, 5],
              listening: false
            }"
          />

          <!-- 坐标轴标注 -->
          <v-text
            :config="{
              x: 10,
              y: 10,
              text: `缩放: ${(scale * 100).toFixed(0)}%  |  原点偏移: (${offset.x.toFixed(0)}, ${offset.y.toFixed(0)})`,
              fontSize: 12,
              fill: '#666',
              listening: false
            }"
          />
        </v-layer>

        <!-- 区域层 -->
        <v-layer v-if="layerConfig.showAreas">
          <AreaPolygon
            v-for="area in areas"
            :key="`area-${area.id}`"
            :area="area"
            :selected="selectedAreaId === area.id"
            :edit-mode="editMode"
            :show-label="layerConfig.showLabels"
            @click="handleAreaClick"
            @hover="handleAreaHover"
            @hover-end="handleAreaHoverEnd"
            @control-point-move="handleAreaControlPointMove"
            @control-point-delete="handleAreaControlPointDelete"
          />
        </v-layer>

        <!-- 设备层 -->
        <v-layer v-if="layerConfig.showDevices">
          <DeviceMarker
            v-for="device in devices"
            :key="`device-${device.id || device.tempId}`"
            :device="device"
            :selected="selectedDeviceId === device.id"
            :draggable="editMode"
            :show-label="layerConfig.showLabels"
            @click="handleDeviceClick"
            @dblclick="handleDeviceDoubleClick"
            @hover="handleDeviceHover"
            @hover-end="handleDeviceHoverEnd"
            @drag-end="handleDeviceDragEnd"
          />
        </v-layer>

        <!-- 工具提示层 -->
        <v-layer>
          <v-label v-if="tooltip.visible" :config="{ x: tooltip.x, y: tooltip.y }">
            <v-tag :config="{ fill: 'rgba(0, 0, 0, 0.8)', cornerRadius: 5 }" />
            <v-text
              :config="{
                text: tooltip.text,
                fontSize: 14,
                padding: 10,
                fill: 'white'
              }"
            />
          </v-label>
        </v-layer>
      </v-stage>
    </div>

    <!-- 侧边栏：设备/区域详情 -->
    <el-drawer
      v-model="detailDrawerVisible"
      :title="drawerTitle"
      size="450px"
      direction="rtl"
    >
      <!-- 设备详情 -->
      <template v-if="selectedDevice">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="设备ID" v-if="!selectedDevice.isNew">
            {{ selectedDevice.id }}
          </el-descriptions-item>
          <el-descriptions-item label="设备名称">
            <el-input
              v-if="editMode"
              v-model="selectedDevice.name"
              placeholder="请输入设备名称"
              @change="markAsChanged"
            />
            <span v-else>{{ selectedDevice.name }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="设备类型">
            {{ deviceTypeLabels[selectedDevice.type] || selectedDevice.type }}
          </el-descriptions-item>
          <el-descriptions-item label="所属区域">
            <el-select
              v-if="editMode"
              v-model="selectedDevice.areaId"
              placeholder="选择区域"
              @change="markAsChanged"
            >
              <el-option
                v-for="area in areas"
                :key="area.id"
                :label="area.name"
                :value="area.id"
              />
            </el-select>
            <span v-else>{{ selectedDevice.areaName || '未分配' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="Canvas坐标">
            X: {{ selectedDevice.x.toFixed(1) }}px, Y: {{ selectedDevice.y.toFixed(1) }}px
          </el-descriptions-item>
          <el-descriptions-item label="本地坐标（米）">
            X: {{ (selectedDevice.localX || 0).toFixed(2) }}m,
            Y: {{ (selectedDevice.localY || 0).toFixed(2) }}m
          </el-descriptions-item>
          <el-descriptions-item label="状态" v-if="!selectedDevice.isNew">
            <el-tag :type="selectedDevice.status === 'online' ? 'success' : 'danger'">
              {{ selectedDevice.status === 'online' ? '在线' : '离线' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 新设备保存提示 -->
        <el-alert
          v-if="selectedDevice.isNew"
          title="新设备"
          description="这是一个新添加的设备，请完善信息后保存"
          type="warning"
          style="margin-top: 16px"
          :closable="false"
        />

        <!-- 实时数据 -->
        <div v-if="selectedDevice.realtimeData && !selectedDevice.isNew" class="realtime-data">
          <el-divider>实时数据</el-divider>
          <div v-for="(value, key) in selectedDevice.realtimeData" :key="key" class="data-item">
            <div class="data-label">{{ key }}</div>
            <div class="data-value">{{ value }}</div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="drawer-actions">
          <el-button
            v-if="selectedDevice.isNew"
            type="primary"
            @click="confirmNewDevice"
            :disabled="!selectedDevice.name"
          >
            确认添加
          </el-button>
          <el-button v-if="selectedDevice.isNew" @click="cancelNewDevice">取消</el-button>
          <el-button v-if="!selectedDevice.isNew" type="danger" @click="deleteDevice">
            删除设备
          </el-button>
        </div>
      </template>

      <!-- 区域详情 -->
      <template v-else-if="selectedArea">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="区域ID">{{ selectedArea.id }}</el-descriptions-item>
          <el-descriptions-item label="区域名称">{{ selectedArea.name }}</el-descriptions-item>
          <el-descriptions-item label="区域编码">{{ selectedArea.code }}</el-descriptions-item>
          <el-descriptions-item label="区域类型">
            {{ areaTypeLabels[selectedArea.type] || selectedArea.type }}
          </el-descriptions-item>
          <el-descriptions-item label="面积">
            {{ calculateAreaSize(selectedArea.points).toFixed(2) }} m²
          </el-descriptions-item>
        </el-descriptions>

        <!-- 区域内设备列表 -->
        <el-divider>区域内设备 ({{ areaDevices.length }})</el-divider>
        <el-table :data="areaDevices" style="width: 100%" size="small">
          <el-table-column prop="name" label="设备名称" />
          <el-table-column prop="type" label="类型" width="80">
            <template #default="{ row }">
              {{ deviceTypeLabels[row.type] }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="60">
            <template #default="{ row }">
              <el-tag
                :type="row.status === 'online' ? 'success' : 'danger'"
                size="small"
              >
                {{ row.status === 'online' ? '在线' : '离线' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-drawer>

    <!-- 创建楼层对话框 -->
    <el-dialog
      v-model="createFloorDialogVisible"
      title="创建楼层"
      width="500px"
    >
      <el-form :model="newFloorForm" label-width="100px">
        <el-form-item label="楼层名称" required>
          <el-input v-model="newFloorForm.name" placeholder="例如：一层" />
        </el-form-item>
        <el-form-item label="楼层编码" required>
          <el-input v-model="newFloorForm.code" placeholder="例如：F1" />
        </el-form-item>
        <el-form-item label="楼层号" required>
          <el-input-number v-model="newFloorForm.floorNumber" :min="-5" :max="100" />
          <el-text size="small" type="info" style="margin-left: 8px">
            负数表示地下楼层
          </el-text>
        </el-form-item>
        <el-form-item label="楼层类型">
          <el-select v-model="newFloorForm.floorType" placeholder="请选择">
            <el-option label="标准层" value="STANDARD" />
            <el-option label="设备层" value="EQUIPMENT" />
            <el-option label="架空层" value="ELEVATED" />
            <el-option label="屋顶" value="ROOF" />
            <el-option label="地下室" value="BASEMENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="层高(米)">
          <el-input-number v-model="newFloorForm.floorHeight" :min="2" :max="10" :step="0.1" :precision="1" />
        </el-form-item>
        <el-form-item label="建筑面积(㎡)">
          <el-input-number v-model="newFloorForm.floorArea" :min="0" :step="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createFloorDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createFloor">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ZoomIn,
  ZoomOut,
  Refresh,
  Plus,
  Check,
  ArrowDown
} from '@element-plus/icons-vue'
import AreaPolygon, { type Area } from './AreaPolygon.vue'
import DeviceMarker, { type Device } from './DeviceMarker.vue'
import { parseGeometry, localToCanvas, canvasToLocal, getPolygonCenter } from '@/utils/coordinate/transform'
import * as FloorApi from '@/api/iot/gis/floor'
import * as AreaApi from '@/api/iot/gis/area'
import * as DeviceApi from '@/api/iot/device'

// Props
const props = defineProps<{
  buildingId: number
  buildingName: string
}>()

// Emits
const emit = defineEmits<{
  back: []
}>()

// ========== 数据状态 ==========

// 画布配置
const stageConfig = reactive({
  width: 0,
  height: 0
})

// 视图状态
const scale = ref(1)
const offset = reactive({ x: 50, y: 50 })
const stageRef = ref<any>(null)

// 楼层数据
const floors = ref<any[]>([])
const selectedFloorId = ref<number>(0)

// 区域数据
const areas = ref<Area[]>([])
const selectedAreaId = ref<number | null>(null)

// 设备数据
const devices = ref<Device[]>([])
const selectedDeviceId = ref<number | null>(null)

// 图层配置
const layerConfig = reactive({
  showAreas: true,
  showDevices: true,
  showLabels: true
})

// 编辑模式
const editMode = ref(false)

// 添加设备模式
const addDeviceMode = ref(false)
const pendingDeviceType = ref<string>('')
let tempDeviceId = 1

// 工具提示
const tooltip = reactive({
  visible: false,
  x: 0,
  y: 0,
  text: ''
})

// 详情抽屉
const detailDrawerVisible = ref(false)

// 未保存更改
const hasUnsavedChanges = ref(false)
const unsavedDevices = ref<Set<number | string>>(new Set())
const saving = ref(false)

// 创建楼层对话框
const createFloorDialogVisible = ref(false)
const newFloorForm = reactive({
  name: '',
  code: '',
  floorNumber: 1,
  floorType: 'STANDARD',
  floorHeight: 3.6,
  floorArea: 0
})

// 常量
const deviceTypeLabels: Record<string, string> = {
  LIGHTING: '照明',
  AC: '空调',
  CAMERA: '摄像头',
  SENSOR: '传感器',
  DOOR: '门禁',
  ALARM: '报警器',
  SMOKE_DETECTOR: '烟感',
  FIRE: '消防设备',
  SWITCH: '开关',
  METER: '电表',
  HVAC: '暖通'
}

const areaTypeLabels: Record<string, string> = {
  ROOM: '房间',
  CORRIDOR: '走廊',
  ELEVATOR: '电梯',
  STAIRCASE: '楼梯',
  RESTROOM: '洗手间',
  EQUIPMENT_ROOM: '设备房',
  PUBLIC: '公共区域'
}

// ========== 计算属性 ==========

const selectedDevice = computed(() => {
  return devices.value.find((d) => d.id === selectedDeviceId.value || d.tempId === selectedDeviceId.value)
})

const selectedArea = computed(() => {
  return areas.value.find((a) => a.id === selectedAreaId.value)
})

const areaDevices = computed(() => {
  if (!selectedAreaId.value) return []
  return devices.value.filter((d) => d.areaId === selectedAreaId.value)
})

const drawerTitle = computed(() => {
  if (selectedDevice.value) {
    return selectedDevice.value.isNew ? '新设备' : '设备详情'
  }
  if (selectedArea.value) {
    return '区域详情'
  }
  return ''
})

const unsavedCount = computed(() => unsavedDevices.value.size)

// 网格线
const gridLines = computed(() => {
  const lines: number[][] = []
  const gridSize = 50 * scale.value

  for (let x = 0; x < stageConfig.width; x += gridSize) {
    lines.push([x, 0, x, stageConfig.height])
  }

  for (let y = 0; y < stageConfig.height; y += gridSize) {
    lines.push([0, y, stageConfig.width, y])
  }

  return lines
})

// ========== 方法 ==========

/**
 * 加载楼层列表
 */
async function loadFloors() {
  try {
    const data = await FloorApi.getFloorList({ buildingId: props.buildingId })
    floors.value = data.list || []

    if (floors.value.length > 0) {
      selectedFloorId.value = floors.value[0].id
      await loadFloorData()
    } else {
      // 没有楼层数据时的友好提示
      ElMessageBox.confirm(
        `建筑 "${props.buildingName}" 暂无楼层数据。是否现在创建楼层？`,
        '暂无楼层数据',
        {
          confirmButtonText: '创建楼层',
          cancelButtonText: '返回',
          type: 'warning'
        }
      ).then(() => {
        // 打开创建楼层对话框
        showCreateFloorDialog()
      }).catch(() => {
        // 用户选择返回
        emit('back')
      })
    }
  } catch (error) {
    console.error('加载楼层失败:', error)
    ElMessage.error('加载楼层数据失败，请检查网络连接或联系管理员')
  }
}

/**
 * 加载楼层数据（区域 + 设备）
 */
async function loadFloorData() {
  if (!selectedFloorId.value) return

  try {
    // 加载区域
    const areasData = await AreaApi.getAreaList({ floorId: selectedFloorId.value })
    areas.value = (areasData.list || []).map((area: any) => {
      const geomCoords = parseGeometry(area.localGeom || area.geom)
      const points = localToCanvas(geomCoords, 10, offset.x, offset.y)

      return {
        id: area.id,
        name: area.name,
        code: area.code,
        type: area.areaType,
        points,
        color: area.fillColor
      }
    })

    // 加载设备
    const devicesData = await DeviceApi.getDeviceList({ floorId: selectedFloorId.value })
    devices.value = (devicesData.list || []).map((device: any) => ({
      id: device.id,
      name: device.name,
      type: device.deviceType,
      status: device.status,
      x: (device.localX || 0) * 10 + offset.x,
      y: -(device.localY || 0) * 10 + offset.y,
      localX: device.localX,
      localY: device.localY,
      localZ: device.localZ,
      areaId: device.areaId,
      areaName: device.areaName,
      realtimeData: device.realtimeData || {}
    }))

    ElMessage.success(
      `已加载楼层: ${floors.value.find((f) => f.id === selectedFloorId.value)?.name}`
    )
  } catch (error) {
    console.error('加载楼层数据失败:', error)
    ElMessage.error('加载楼层数据失败')
  }
}

/**
 * 缩放
 */
function zoomIn() {
  scale.value = Math.min(scale.value * 1.2, 5)
  applyScale()
}

function zoomOut() {
  scale.value = Math.max(scale.value / 1.2, 0.2)
  applyScale()
}

function resetView() {
  scale.value = 1
  offset.x = 50
  offset.y = 50
  const stage = stageRef.value?.getNode()
  if (stage) {
    stage.position({ x: 0, y: 0 })
    stage.scale({ x: 1, y: 1 })
  }
}

function applyScale() {
  const stage = stageRef.value?.getNode()
  if (stage) {
    stage.scale({ x: scale.value, y: scale.value })
  }
}

/**
 * 鼠标滚轮缩放
 */
function handleWheel(e: any) {
  e.evt.preventDefault()

  const stage = e.target.getStage()
  const oldScale = stage.scaleX()
  const pointer = stage.getPointerPosition()

  const scaleBy = 1.1
  const newScale = e.evt.deltaY < 0 ? oldScale * scaleBy : oldScale / scaleBy

  if (newScale < 0.2 || newScale > 5) return

  stage.scale({ x: newScale, y: newScale })
  scale.value = newScale

  const newPos = {
    x: pointer.x - ((pointer.x - stage.x()) / oldScale) * newScale,
    y: pointer.y - ((pointer.y - stage.y()) / oldScale) * newScale
  }
  stage.position(newPos)
}

/**
 * 画布点击（添加设备）
 */
function handleStageClick(e: any) {
  // 只有在添加设备模式下才响应
  if (!addDeviceMode.value) return

  // 确保点击的是画布背景，不是其他元素
  if (e.target !== e.target.getStage()) return

  const stage = e.target.getStage()
  const pointer = stage.getPointerPosition()

  // 添加新设备
  addDeviceAtPosition(pointer.x, pointer.y)
}

/**
 * 在指定位置添加设备
 */
function addDeviceAtPosition(canvasX: number, canvasY: number) {
  // 转换为本地坐标
  const local = canvasToLocal(canvasX, canvasY, 10, offset.x, offset.y)

  // 创建新设备
  const newDevice: Device = {
    tempId: `temp-${tempDeviceId++}`,
    name: `${deviceTypeLabels[pendingDeviceType.value]}-${tempDeviceId}`,
    type: pendingDeviceType.value,
    status: 'offline',
    x: canvasX,
    y: canvasY,
    localX: local.x,
    localY: local.y,
    localZ: 0,
    floorId: selectedFloorId.value,
    isNew: true
  }

  devices.value.push(newDevice)

  // 自动选中新设备
  selectedDeviceId.value = newDevice.tempId
  detailDrawerVisible.value = true

  // 标记为未保存
  unsavedDevices.value.add(newDevice.tempId!)
  hasUnsavedChanges.value = true

  ElMessage.success('设备已添加到平面图，请完善信息后保存')

  // 退出添加模式
  addDeviceMode.value = false
  pendingDeviceType.value = ''
}

/**
 * 处理添加设备命令
 */
function handleAddDeviceCommand(deviceType: string) {
  addDeviceMode.value = true
  pendingDeviceType.value = deviceType
  ElMessage.info(`请在平面图上点击要放置 ${deviceTypeLabels[deviceType]} 的位置`)
}

/**
 * 取消添加设备
 */
function cancelAddDevice() {
  addDeviceMode.value = false
  pendingDeviceType.value = ''
  ElMessage.info('已取消添加设备')
}

/**
 * 确认新设备
 */
async function confirmNewDevice() {
  if (!selectedDevice.value) return

  try {
    // 保存到后端
    const deviceData = {
      name: selectedDevice.value.name,
      deviceType: selectedDevice.value.type,
      floorId: selectedFloorId.value,
      areaId: selectedDevice.value.areaId,
      localX: selectedDevice.value.localX,
      localY: selectedDevice.value.localY,
      localZ: selectedDevice.value.localZ || 0
    }

    const result = await DeviceApi.createDevice(deviceData)

    // 更新设备ID
    selectedDevice.value.id = result.id
    selectedDevice.value.isNew = false

    // 移除未保存标记
    unsavedDevices.value.delete(selectedDevice.value.tempId!)
    if (unsavedDevices.value.size === 0) {
      hasUnsavedChanges.value = false
    }

    ElMessage.success('设备添加成功')
    detailDrawerVisible.value = false

    // 重新加载数据
    await loadFloorData()
  } catch (error) {
    console.error('保存设备失败:', error)
    ElMessage.error('保存设备失败')
  }
}

/**
 * 取消新设备
 */
function cancelNewDevice() {
  if (!selectedDevice.value) return

  const tempId = selectedDevice.value.tempId
  devices.value = devices.value.filter((d) => d.tempId !== tempId)
  unsavedDevices.value.delete(tempId!)

  if (unsavedDevices.value.size === 0) {
    hasUnsavedChanges.value = false
  }

  detailDrawerVisible.value = false
  ElMessage.info('已取消添加设备')
}

/**
 * 删除设备
 */
async function deleteDevice() {
  if (!selectedDevice.value) return

  try {
    await ElMessageBox.confirm('确定要删除此设备吗？', '提示', {
      type: 'warning'
    })

    await DeviceApi.deleteDevice(selectedDevice.value.id!)

    ElMessage.success('设备已删除')
    detailDrawerVisible.value = false

    await loadFloorData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除设备失败:', error)
      ElMessage.error('删除设备失败')
    }
  }
}

/**
 * 设备拖拽结束
 */
async function handleDeviceDragEnd(device: Device, newPos: { x: number; y: number }) {
  // 更新设备位置
  device.x = newPos.x
  device.y = newPos.y

  // 转换为本地坐标
  const local = canvasToLocal(newPos.x, newPos.y, 10, offset.x, offset.y)
  device.localX = local.x
  device.localY = local.y

  // 标记为未保存
  if (device.id) {
    unsavedDevices.value.add(device.id)
    hasUnsavedChanges.value = true
  }

  ElMessage.info('设备位置已更新，请保存更改')
}

/**
 * 保存所有更改
 */
async function saveChanges() {
  try {
    saving.value = true

    const updates: Promise<any>[] = []

    // 保存所有修改过的设备
    for (const deviceId of unsavedDevices.value) {
      const device = devices.value.find((d) => d.id === deviceId)
      if (device && !device.isNew) {
        updates.push(
          DeviceApi.updateDevice(device.id!, {
            localX: device.localX,
            localY: device.localY,
            localZ: device.localZ,
            areaId: device.areaId
          })
        )
      }
    }

    await Promise.all(updates)

    unsavedDevices.value.clear()
    hasUnsavedChanges.value = false

    ElMessage.success(`成功保存 ${updates.length} 个设备的更改`)

    await loadFloorData()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

/**
 * 标记为已更改
 */
function markAsChanged() {
  if (selectedDevice.value && selectedDevice.value.id) {
    unsavedDevices.value.add(selectedDevice.value.id)
    hasUnsavedChanges.value = true
  }
}

/**
 * 显示创建楼层对话框
 */
function showCreateFloorDialog() {
  // 重置表单
  newFloorForm.name = ''
  newFloorForm.code = ''
  newFloorForm.floorNumber = 1
  newFloorForm.floorType = 'STANDARD'
  newFloorForm.floorHeight = 3.6
  newFloorForm.floorArea = 0
  
  createFloorDialogVisible.value = true
}

/**
 * 创建楼层
 */
async function createFloor() {
  // 验证表单
  if (!newFloorForm.name || !newFloorForm.code) {
    ElMessage.warning('请填写楼层名称和编码')
    return
  }

  try {
    await FloorApi.createFloor({
      buildingId: props.buildingId,
      name: newFloorForm.name,
      code: newFloorForm.code,
      floorNumber: newFloorForm.floorNumber,
      floorType: newFloorForm.floorType,
      floorHeight: newFloorForm.floorHeight,
      floorArea: newFloorForm.floorArea
    })

    ElMessage.success('楼层创建成功')
    createFloorDialogVisible.value = false

    // 重新加载楼层列表
    await loadFloors()
  } catch (error) {
    console.error('创建楼层失败:', error)
    ElMessage.error('创建楼层失败，请稍后重试')
  }
}

/**
 * 区域点击
 */
function handleAreaClick(area: Area) {
  selectedAreaId.value = area.id
  selectedDeviceId.value = null
  detailDrawerVisible.value = true
}

/**
 * 设备点击
 */
function handleDeviceClick(device: Device) {
  selectedDeviceId.value = device.id || device.tempId
  selectedAreaId.value = null
  detailDrawerVisible.value = true
}

/**
 * 设备双击
 */
function handleDeviceDoubleClick(device: Device) {
  // 可以打开设备控制面板
  console.log('设备双击:', device)
}

/**
 * 区域悬停
 */
function handleAreaHover(area: Area) {
  tooltip.visible = true
  tooltip.text = `${area.name} (${areaTypeLabels[area.type]})`
}

/**
 * 设备悬停
 */
function handleDeviceHover(device: Device) {
  tooltip.visible = true
  tooltip.text = `${device.name}\n类型: ${deviceTypeLabels[device.type]}\n状态: ${device.status}`
}

/**
 * 悬停结束
 */
function handleAreaHoverEnd() {
  tooltip.visible = false
}

function handleDeviceHoverEnd() {
  tooltip.visible = false
}

/**
 * 区域控制点移动
 */
function handleAreaControlPointMove(area: Area, pointIndex: number, newPos: { x: number; y: number }) {
  // 更新区域点
  area.points[pointIndex * 2] = newPos.x
  area.points[pointIndex * 2 + 1] = newPos.y
}

/**
 * 区域控制点删除
 */
function handleAreaControlPointDelete(area: Area, pointIndex: number) {
  // 删除点
  area.points.splice(pointIndex * 2, 2)
}

/**
 * 图层切换
 */
function handleLayerChange() {
  // 图层配置已自动更新
}

/**
 * 计算区域面积
 */
function calculateAreaSize(points: number[]): number {
  // 简单的多边形面积计算（鞋带公式）
  let area = 0
  for (let i = 0; i < points.length - 2; i += 2) {
    area += points[i] * points[i + 3] - points[i + 2] * points[i + 1]
  }
  // 转换回平方米（points 是像素，缩放为10）
  return Math.abs(area / 2) / (10 * 10)
}

// 拖拽画布
function handleMouseDown() {}
function handleMouseMove() {}
function handleMouseUp() {}

/**
 * 响应式调整画布大小
 */
function updateCanvasSize() {
  stageConfig.width = window.innerWidth - 100
  stageConfig.height = window.innerHeight - 220
}

// 生命周期

onMounted(() => {
  updateCanvasSize()
  window.addEventListener('resize', updateCanvasSize)
  loadFloors()
})

onUnmounted(() => {
  window.removeEventListener('resize', updateCanvasSize)
})

// 离开提示
window.addEventListener('beforeunload', (e) => {
  if (hasUnsavedChanges.value) {
    e.preventDefault()
    e.returnValue = ''
  }
})
</script>

<style scoped lang="scss">
.floor-plan-editor {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;

  .toolbar {
    padding: 12px 16px;
    background: white;
    border-bottom: 1px solid #e4e7ed;
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;

    .toolbar-left,
    .toolbar-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }

  .add-device-hint {
    padding: 8px 16px;
    background: #ecf5ff;
    border-bottom: 1px solid #b3d8ff;

    :deep(.el-alert) {
      padding: 8px 12px;
    }
  }

  .canvas-container {
    flex: 1;
    overflow: hidden;
    position: relative;
    background: #ffffff;
  }

  .realtime-data {
    margin-top: 16px;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;

    .data-item {
      padding: 12px;
      background: #f5f7fa;
      border-radius: 4px;

      .data-label {
        font-size: 12px;
        color: #909399;
        margin-bottom: 4px;
      }

      .data-value {
        font-size: 20px;
        font-weight: 600;
        color: #303133;
      }
    }
  }

  .drawer-actions {
    margin-top: 24px;
    display: flex;
    gap: 12px;
    justify-content: flex-end;
  }
}
</style>

