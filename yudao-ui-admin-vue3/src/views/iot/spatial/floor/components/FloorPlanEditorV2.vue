<template>
  <div class="floor-plan-editor-v2">
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

      <el-button @click="loadData" size="small">
        <Icon icon="ep:refresh" class="mr-5px" />
        刷新数据
      </el-button>

      <el-divider direction="vertical" />

      <el-button type="success" @click="saveFloorPlan" :loading="saving" size="small">
        <Icon icon="ep:document-checked" class="mr-5px" />
        保存
      </el-button>
    </div>

    <!-- 主内容区：三列布局 -->
    <div class="editor-main">
      <!-- 左侧：设备工具栏 -->
      <div class="device-toolbox">
        <!-- 标题栏 -->
        <div class="toolbox-title">
          <Icon icon="ep:menu" class="mr-5px" />
          <span>设备工具箱</span>
        </div>

        <!-- Tab 导航（顶部）-->
        <el-tabs v-model="activeToolboxTab" class="toolbox-tabs" type="card">
          <!-- Tab 1: 设备模板 -->
          <el-tab-pane name="templates">
            <template #label>
              <div class="tab-label">
                <Icon icon="ep:tools" />
                <span>设备模板</span>
              </div>
            </template>
            
            <div class="tab-content">
              <el-scrollbar height="560px">
                <div class="device-template-list">
                  <div 
                    v-for="template in deviceTemplates" 
                    :key="template.type"
                    class="device-template-item"
                    :draggable="true"
                    @dragstart="handleDragStart($event, template)"
                    @click="addDeviceFromTemplate(template)"
                  >
                    <Icon :icon="template.icon" :size="24" :color="template.color" />
                    <span>{{ template.label }}</span>
                  </div>
                </div>
              </el-scrollbar>
              
              <div class="tab-footer">
                <el-text size="small" type="info">
                  <Icon icon="ep:info-filled" class="mr-5px" />
                  点击或拖放到画布添加设备
                </el-text>
              </div>
            </div>
          </el-tab-pane>

          <!-- Tab 2: 楼层设备 -->
          <el-tab-pane name="available">
            <template #label>
              <div class="tab-label">
                <Icon icon="ep:folder-opened" />
                <span>楼层设备</span>
                <el-badge 
                  :value="deviceStats.pending" 
                  :max="99" 
                  v-if="deviceStats.pending > 0"
                />
              </div>
            </template>
            
            <div class="tab-content">
              <div class="device-panel">
              <!-- 操作栏 -->
              <div class="device-actions-bar">
                <el-checkbox 
                  v-model="selectAll"
                  @change="handleSelectAll"
                  :indeterminate="isIndeterminate"
                >
                  全选
                </el-checkbox>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="importSelectedDevices"
                  :disabled="selectedDeviceIds.length === 0"
                >
                  <Icon icon="ep:download" class="mr-5px" />
                  导入选中 ({{ selectedDeviceIds.length }})
                </el-button>
              </div>

              <!-- 搜索和筛选 -->
              <div class="device-filter-bar">
                <el-input
                  v-model="deviceSearchKeyword"
                  placeholder="搜索设备名称"
                  size="small"
                  clearable
                  @input="handleDeviceSearch"
                >
                  <template #prefix>
                    <Icon icon="ep:search" />
                  </template>
                </el-input>
                <el-select
                  v-model="deviceTypeFilter"
                  placeholder="类型"
                  size="small"
                  clearable
                  @change="handleDeviceFilter"
                  style="width: 120px"
                >
                  <el-option label="全部" value="" />
                  <el-option 
                    v-for="type in deviceTypes" 
                    :key="type" 
                    :label="type" 
                    :value="type" 
                  />
                </el-select>
              </div>

              <!-- 设备统计 -->
              <div class="device-quick-stats">
                <div class="stat-item">
                  <span class="label">总数</span>
                  <span class="value">{{ deviceStats.total }}</span>
                </div>
                <div class="stat-item">
                  <span class="label">已导入</span>
                  <span class="value success">{{ deviceStats.imported }}</span>
                </div>
                <div class="stat-item">
                  <span class="label">待导入</span>
                  <span class="value primary">{{ deviceStats.pending }}</span>
                </div>
                <div class="stat-item" v-if="deviceStats.noCoordinates > 0">
                  <span class="label">待定位</span>
                  <span class="value warning">{{ deviceStats.noCoordinates }}</span>
                </div>
              </div>
              
              <!-- 操作提示 -->
              <el-alert
                v-if="deviceStats.noCoordinates > 0"
                type="warning"
                :closable="false"
                show-icon
                class="device-tip-alert"
              >
                <template #title>
                  <span style="font-size: 12px;">
                    有 <strong>{{ deviceStats.noCoordinates }}</strong> 个设备无坐标，请导入后在画布上指定位置
                  </span>
                </template>
              </el-alert>

              <!-- 设备列表（虚拟滚动优化） -->
              <el-scrollbar 
                ref="deviceScrollbar"
                height="440px"
              >
                <div class="compact-device-list" ref="deviceListContainer">
                  <!-- 空状态 -->
                  <el-empty 
                    v-if="filteredDevices.length === 0 && !loadingDevices"
                    :description="deviceSearchKeyword ? '未找到匹配设备' : '暂无设备'"
                    :image-size="80"
                  />

                  <!-- 设备项 -->
                  <div 
                    v-for="device in paginatedDevices" 
                    :key="device.id || device.tempId"
                    class="compact-device-item"
                    :class="{ 
                      'is-imported': isDeviceImported(device),
                      'is-selected': isDeviceSelected(device)
                    }"
                    @click="toggleDeviceSelection(device)"
                  >
                    <el-checkbox 
                      :model-value="isDeviceSelected(device)"
                      @click.stop
                      @change="toggleDeviceSelection(device)"
                      :disabled="isDeviceImported(device)"
                    />
                    
                    <Icon 
                      :icon="getDeviceIcon(device.deviceType)" 
                      :size="20" 
                      :color="getDeviceColor(device.deviceType)" 
                      class="device-icon-small"
                    />
                    
                    <div class="device-info-compact">
                      <div class="device-name-compact">
                        {{ getDeviceShortName(device.deviceName || device.name || '未命名') }}
                      </div>
                      <div class="device-meta">
                        <span class="device-type-tag">{{ device.deviceType || '未知' }}</span>
                        
                        <!-- 状态标签：已导入到画布 -->
                        <el-tag 
                          v-if="isDeviceImported(device)" 
                          type="success" 
                          size="small"
                          effect="plain"
                        >
                          <Icon icon="ep:circle-check" class="mr-2px" />
                          已导入
                        </el-tag>
                        
                        <!-- 状态标签：数据库设备，有坐标，未导入 -->
                        <el-tag 
                          v-else-if="device.id && device.localX != null && device.localY != null"
                          type="info" 
                          size="small"
                          effect="plain"
                        >
                          <Icon icon="ep:location-information" class="mr-2px" />
                          有坐标
                        </el-tag>
                        
                        <!-- 状态标签：数据库设备，无坐标，待定位 -->
                        <el-tag 
                          v-else-if="device.id"
                          type="warning"
                          size="small"
                          effect="plain"
                        >
                          <Icon icon="ep:location" class="mr-2px" />
                          无坐标·待定位
                        </el-tag>
                        
                        <!-- 状态标签：DXF识别设备，未保存 -->
                        <el-tag 
                          v-else
                          type="warning" 
                          size="small"
                          effect="plain"
                        >
                          <Icon icon="ep:warning" class="mr-2px" />
                          DXF识别
                        </el-tag>
                      </div>
                    </div>

                    <el-button 
                      v-if="isDeviceImported(device)"
                      type="text" 
                      size="small"
                      @click.stop="locateDevice(device)"
                      class="action-btn"
                    >
                      <Icon icon="ep:location" />
                    </el-button>
                  </div>

                  <!-- 加载更多提示 -->
                  <div 
                    v-if="hasMoreDevices" 
                    class="load-more-trigger"
                    v-loading="loadingMoreDevices"
                  >
                    <el-text size="small" type="info">
                      {{ loadingMoreDevices ? '加载中...' : '向下滚动加载更多' }}
                    </el-text>
                  </div>

                  <!-- 加载完成提示 -->
                  <div v-else-if="paginatedDevices.length > 20" class="load-complete-tip">
                    <el-text size="small" type="info">
                      已加载全部 {{ filteredDevices.length }} 个设备
                    </el-text>
                  </div>
                </div>
              </el-scrollbar>

              <!-- 底部提示 -->
              <div class="device-panel-footer">
                <el-text size="small" type="info">
                  <Icon icon="ep:info-filled" class="mr-5px" />
                  点击选择，批量导入设备
                </el-text>
              </div>
              </div>
              
              <div class="tab-footer">
                <el-text size="small" type="info">
                  <Icon icon="ep:info-filled" class="mr-5px" />
                  从列表中导入设备到平面图
                </el-text>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 中间：画布区域 -->
      <div class="canvas-container" @drop="handleDrop" @dragover.prevent @contextmenu="handleCanvasContextMenu">
        <canvas id="floor-plan-canvas-v2"></canvas>
        
        <!-- 右键菜单 -->
        <div 
          v-show="contextMenu.visible" 
          class="context-menu"
          :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
        >
          <div class="context-menu-header">添加设备</div>
          <div 
            v-for="template in deviceTemplates" 
            :key="template.type"
            class="context-menu-item"
            @click="addDeviceFromContextMenu(template)"
          >
            <Icon :icon="template.icon" :color="template.color" class="mr-5px" />
            {{ template.label }}
          </div>
        </div>

        <!-- 缩放显示 -->
        <div class="zoom-display">
          {{ Math.round(zoomLevel * 100) }}%
        </div>

        <!-- 坐标显示 -->
        <div class="coordinate-display" v-if="mousePosition">
          X: {{ mousePosition.x.toFixed(2) }}m, Y: {{ mousePosition.y.toFixed(2) }}m
        </div>

        <!-- 空状态提示 -->
        <div v-if="isEmpty" class="empty-canvas-tip">
          <Icon icon="ep:picture" :size="80" color="#dcdfe6" />
          <p>{{ emptyMessage }}</p>
          <el-button type="primary" @click="loadData" size="small">
            <Icon icon="ep:refresh" class="mr-5px" />
            重新加载数据
          </el-button>
        </div>
      </div>

      <!-- 右侧：属性面板 -->
      <div class="properties-panel">
        <!-- 调试信息（开发阶段显示） -->
        <el-card v-if="debugInfo.show" shadow="never" style="margin-bottom: 10px;">
          <template #header>
            <div class="card-header">
              <span>调试信息</span>
              <el-button link @click="debugInfo.show = false" size="small">
                <Icon icon="ep:close" />
              </el-button>
            </div>
          </template>
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="SVG数据">
              {{ debugInfo.svgLoaded ? '✅ 已加载' : '❌ 未加载' }} ({{ debugInfo.svgLength }} 字符)
            </el-descriptions-item>
            <el-descriptions-item label="设备数量">
              {{ debugInfo.deviceCount }} 个
            </el-descriptions-item>
            <el-descriptions-item label="有坐标设备">
              {{ debugInfo.devicesWithCoords }} 个
            </el-descriptions-item>
            <el-descriptions-item label="识别结果">
              {{ debugInfo.recognizedDevices }} 个
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>设备属性</span>
              <el-button 
                v-if="selectedDevice" 
                link 
                type="danger" 
                @click="deleteSelectedDevice"
                size="small"
              >
                <Icon icon="ep:delete" />
              </el-button>
            </div>
          </template>

          <!-- 未选中设备时 -->
          <el-empty 
            v-if="!selectedDevice" 
            description="请在画布中选择设备"
            :image-size="80"
          />

          <!-- 已选中设备时 -->
          <el-form v-else label-width="80px" size="small">
            <el-form-item label="设备名称">
              <el-input 
                v-model="selectedDevice.name" 
                @change="updateSelectedDevice"
                placeholder="输入设备名称"
              />
            </el-form-item>

            <el-form-item label="产品">
              <el-select 
                v-model="selectedDevice.productId" 
                @change="handleProductChange"
                placeholder="选择产品"
                filterable
                style="width: 100%"
              >
                <el-option 
                  v-for="product in productList" 
                  :key="product.id" 
                  :label="product.name" 
                  :value="product.id"
                >
                  <div style="display: flex; align-items: center;">
                    <Icon 
                      :icon="getIconByProductName(product.name)" 
                      :color="getColorByProduct(product)" 
                      class="mr-10px" 
                    />
                    <span>{{ product.name }}</span>
                  </div>
                </el-option>
              </el-select>
              <el-text size="small" type="info" style="margin-top: 4px;">
                <Icon icon="ep:info-filled" class="mr-5px" />
                选择产品会自动匹配图标和颜色
              </el-text>
            </el-form-item>

            <el-divider content-position="left">
              <span style="font-size: 12px; color: #909399;">图标样式</span>
            </el-divider>

            <el-form-item label="图标颜色">
              <el-color-picker 
                v-model="selectedDevice.color" 
                @change="updateSelectedDevice"
                show-alpha
              />
            </el-form-item>

            <el-form-item label="图标大小">
              <el-slider 
                v-model="selectedDevice.iconSize" 
                :min="10" 
                :max="50" 
                @change="updateSelectedDevice"
                show-input
              />
            </el-form-item>

            <el-divider />

            <el-form-item label="所属区域">
              <el-select 
                v-model="selectedDevice.roomId" 
                @change="updateSelectedDevice"
                placeholder="选择区域"
                clearable
                filterable
                style="width: 100%"
              >
                <el-option 
                  v-for="area in floorAreas" 
                  :key="area.id" 
                  :label="area.areaName" 
                  :value="area.id"
                >
                  <span>{{ area.areaName }}</span>
                  <span style="float: right; color: var(--el-text-color-secondary); font-size: 12px">
                    {{ area.areaType }}
                  </span>
                </el-option>
              </el-select>
            </el-form-item>

            <el-divider />

            <el-form-item label="X 坐标(m)">
              <el-input-number 
                v-model="selectedDevice.x" 
                :precision="2"
                :step="0.1"
                @change="updateDevicePosition"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="Y 坐标(m)">
              <el-input-number 
                v-model="selectedDevice.y" 
                :precision="2"
                :step="0.1"
                @change="updateDevicePosition"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="Z 坐标(m)">
              <el-input-number 
                v-model="selectedDevice.z" 
                :precision="2"
                :step="0.1"
                @change="updateSelectedDevice"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>

            <el-divider />

            <el-form-item>
              <el-button type="primary" @click="saveDeviceToServer" :loading="saving" block>
                <Icon icon="ep:document-checked" class="mr-5px" />
                保存到服务器
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fabric } from 'fabric'
import { DeviceApi } from '@/api/iot/device/device'
import { ProductApi } from '@/api/iot/product/product'  // 🔑 新增：产品API
import * as FloorDxfApi from '@/api/iot/spatial/floorDxf'
import * as FloorApi from '@/api/iot/spatial/floor'  // 🔑 新增：楼层API
import * as BuildingApi from '@/api/iot/spatial/building'  // 🔑 新增：建筑API
import * as AreaApi from '@/api/iot/spatial/area'
import { getDeviceIconSvg, ICON_CONFIGS, getIconConfigByProductName } from '@/assets/floorplan-icons'

const props = defineProps({
  floorId: {
    type: Number,
    required: true
  },
  svgData: {
    type: String,
    default: ''
  },
  devices: {
    type: Array as () => any[],
    default: () => []
  },
  coordinateScale: {
    type: Number,
    default: 38.02  // 像素/米
  }
})

const emit = defineEmits(['success', 'device-updated'])

// 调试信息
const debugInfo = ref({
  show: true,
  svgLoaded: false,
  svgLength: 0,
  deviceCount: 0,
  devicesWithCoords: 0,
  recognizedDevices: 0
})

// 画布相关
const canvas = ref<fabric.Canvas | null>(null)
const tool = ref('select')
const zoomLevel = ref(1)
const mousePosition = ref({ x: 0, y: 0 })
const saving = ref(false)

// 右键菜单状态
const contextMenu = ref({
  visible: false,
  x: 0,
  y: 0,
  canvasX: 0,  // 画布坐标
  canvasY: 0,
  dxfX: 0,     // DXF坐标（米）
  dxfY: 0
})

// SVG 变换信息（用于设备坐标转换）
const svgTransform = ref({
  scale: 1,      // Fabric.js 缩放比例
  offsetX: 0,    // Fabric.js X 偏移
  offsetY: 0,    // Fabric.js Y 偏移
  svgWidth: 1920,   // SVG 画布宽度
  svgHeight: 1080,  // SVG 画布高度
  dxfOffsetX: 0,    // DXF→SVG 的 X 偏移（来自 dxfToSvg）
  dxfOffsetY: 0     // DXF→SVG 的 Y 偏移（来自 dxfToSvg）
})

// 选中的设备
const selectedDevice = ref<any>(null)

// 🎨 设备模板（产品名录）
// 💡 这是系统的产品名录，所有设备类型都应该在这里定义
// 🔑 产品列表（从后端加载）
const productList = ref<any[]>([])  // 所有可用产品
const defaultProductId = ref<number | null>(null)  // 默认产品ID（用于DXF识别设备）

// 🔑 楼层和建筑信息（用于设备保存时的默认值）
const floorInfo = ref<any>(null)  // 当前楼层信息
const buildingInfo = ref<any>(null)  // 当前建筑信息

/**
 * 🔑 根据产品名称智能匹配图标
 */
const getIconByProductName = (name: string): string => {
  if (!name) return 'ep:question-filled'
  
  const lowerName = name.toLowerCase()
  
  // 摄像机类
  if (lowerName.includes('摄像机') || lowerName.includes('摄像头') || lowerName.includes('camera')) {
    if (lowerName.includes('半球')) return 'ep:video-camera'
    if (lowerName.includes('枪型') || lowerName.includes('枪机')) return 'ep:camera'
    if (lowerName.includes('球型') || lowerName.includes('球机')) return 'ep:camera-filled'
    return 'ep:camera'
  }
  
  // 门禁类
  if (lowerName.includes('道闸')) return 'ep:unlock'
  if (lowerName.includes('闸机')) return lowerName.includes('人行') ? 'ep:lock' : 'ep:unlock'
  
  // 识别设备
  if (lowerName.includes('车辆识别') || lowerName.includes('车牌识别')) return 'ep:document-checked'
  if (lowerName.includes('人脸识别') || lowerName.includes('人脸')) return 'ep:user'
  
  // 计量设备
  if (lowerName.includes('水表')) return 'ep:operation'
  if (lowerName.includes('电表')) return 'ep:odometer'
  if (lowerName.includes('燃气') || lowerName.includes('气表')) return 'ep:aim'
  
  // 其他
  if (lowerName.includes('巡更')) return 'ep:location'
  if (lowerName.includes('考勤')) return 'ep:calendar'
  if (lowerName.includes('烟感') || lowerName.includes('火灾')) return 'ep:warning'
  if (lowerName.includes('温湿度')) return 'ep:partly-cloudy'
  if (lowerName.includes('风机') || lowerName.includes('空调')) return 'ep:wind-power'
  if (lowerName.includes('照明') || lowerName.includes('灯')) return 'ep:sunny'
  
  return 'ep:question-filled'  // 默认图标
}

/**
 * 🔑 根据产品品类或名称智能匹配颜色
 */
const getColorByProduct = (product: any): string => {
  const categoryName = product.categoryName || ''
  const productName = product.name || ''
  const combined = (categoryName + productName).toLowerCase()
  
  // 安防监控类 - 蓝色
  if (combined.includes('安防') || combined.includes('监控') || combined.includes('摄像')) {
    return '#1296db'
  }
  
  // 门禁通道类 - 绿色
  if (combined.includes('门禁') || combined.includes('通道') || combined.includes('闸机') || combined.includes('道闸')) {
    return '#67c23a'
  }
  
  // 能源计量类 - 红色/橙色
  if (combined.includes('能源') || combined.includes('计量') || combined.includes('表')) {
    if (combined.includes('水')) return '#00d4ff'  // 水表 - 青色
    if (combined.includes('电')) return '#f56c6c'  // 电表 - 红色
    if (combined.includes('燃气') || combined.includes('气')) return '#f56c6c'  // 燃气表 - 红色
    return '#e6a23c'  // 其他计量 - 橙色
  }
  
  // 消防安全类 - 橙色
  if (combined.includes('消防') || combined.includes('烟感') || combined.includes('火灾')) {
    return '#e6a23c'
  }
  
  // 环境监测类 - 绿色/青色
  if (combined.includes('环境') || combined.includes('温湿度') || combined.includes('空气')) {
    return '#67c23a'
  }
  
  // 照明类 - 黄色
  if (combined.includes('照明') || combined.includes('灯')) {
    return '#f39c12'
  }
  
  // 考勤人事类 - 灰色
  if (combined.includes('考勤') || combined.includes('人事')) {
    return '#909399'
  }
  
  return '#409eff'  // 默认颜色 - 主题蓝
}

/**
 * 🔑 设备模板：从产品列表动态生成
 * 
 * 优点：
 * - 与后端产品表完全同步
 * - 新增产品自动出现
 * - 删除产品自动移除
 * - 单一数据源，无需维护两份数据
 */
/**
 * 🆕 设备模板列表（基于产品列表生成）
 * 使用统一的图标配置（ICON_CONFIGS）确保一致性
 */
const deviceTemplates = computed(() => {
  if (productList.value.length === 0) {
    // 如果产品列表为空，返回空数组
    return []
  }
  
  return productList.value.map(product => {
    // 🔑 从统一的图标配置中获取图标和颜色
    const iconConfig = getIconConfigByProductName(product.name)
    
    // 🎯 图标名称映射（用于设备模板显示 Element Plus 图标）
    const getElementIconName = (config: any, productName: string) => {
      if (!config) return getIconByProductName(productName)  // 降级到原有逻辑
      
      // 映射图标配置到 Element Plus 图标名称
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
      
      return iconKeyMap[config.key] || 'ep:position'
    }
    
    return {
      type: product.name,  // 使用产品名称作为类型标识
      label: product.name,  // 显示标签
      icon: getElementIconName(iconConfig, product.name),  // 🎯 Element Plus 图标（用于模板显示）
      iconKey: iconConfig ? iconConfig.key : product.name,  // 🎯 图标键名（用于画布 SVG）
      color: iconConfig ? iconConfig.color : getColorByProduct(product),  // 🎯 优先使用图标配置颜色
      productId: product.id,  // 🔑 关联产品ID
      categoryName: product.categoryName  // 品类名称（用于筛选）
    }
  })
})

// 实际加载的数据
const loadedSvgData = ref('')
const loadedDevices = ref<any[]>([])  // 已导入到画布的设备

// 🆕 楼层现有设备（待导入列表）
const availableDevices = ref<any[]>([])  // 所有可用设备（数据库 + DXF识别）
const importedDeviceIds = ref<Set<string>>(new Set())  // 已导入设备的ID集合

// 设备工具箱标签页
const activeToolboxTab = ref('templates')

// 🆕 设备选择相关
const selectedDeviceIds = ref<string[]>([])  // 选中的设备ID列表
const selectAll = ref(false)  // 全选状态
const deviceSearchKeyword = ref('')  // 搜索关键词
const deviceTypeFilter = ref('')  // 设备类型筛选

// 📊 前端分页和懒加载相关
const deviceScrollbar = ref()  // 滚动容器引用
const deviceListContainer = ref()  // 设备列表容器引用
const currentDisplayCount = ref(20)  // 当前显示的设备数量（初始20个）
const displayPageSize = ref(20)  // 每页显示数量
const loadingDevices = ref(false)  // 是否正在加载设备
const loadingMoreDevices = ref(false)  // 是否正在加载更多
let scrollbarWrap: HTMLElement | null = null  // el-scrollbar 的内部滚动容器

// 🆕 设备类型列表（从可用设备中提取）
const deviceTypes = computed(() => {
  const types = new Set(availableDevices.value.map(d => d.deviceType).filter(Boolean))
  return Array.from(types)
})

// 🆕 过滤后的设备列表
const filteredDevices = computed(() => {
  let devices = availableDevices.value

  // 搜索过滤
  if (deviceSearchKeyword.value) {
    const keyword = deviceSearchKeyword.value.toLowerCase()
    devices = devices.filter(d => {
      const name = (d.deviceName || d.name || '').toLowerCase()
      return name.includes(keyword)
    })
  }

  // 类型过滤
  if (deviceTypeFilter.value) {
    devices = devices.filter(d => d.deviceType === deviceTypeFilter.value)
  }

  // 🆕 排序：无坐标设备 > 有坐标未导入 > 已导入
  devices = devices.sort((a, b) => {
    // 判断设备是否已导入（内联判断，避免函数作用域问题）
    const aKey = a.id || a.tempId
    const bKey = b.id || b.tempId
    const aImported = importedDeviceIds.value.has(aKey)
    const bImported = importedDeviceIds.value.has(bKey)
    
    // 判断设备是否无坐标
    const aNoCoords = a.id && (a.localX == null || a.localY == null)
    const bNoCoords = b.id && (b.localX == null || b.localY == null)
    
    // 1. 无坐标设备优先（排在最前面）⚠️
    if (aNoCoords && !bNoCoords) return -1
    if (!aNoCoords && bNoCoords) return 1
    
    // 2. 未导入设备优先于已导入
    if (!aImported && bImported) return -1
    if (aImported && !bImported) return 1
    
    // 3. 同类设备按名称排序
    const aName = a.deviceName || a.name || ''
    const bName = b.deviceName || b.name || ''
    return aName.localeCompare(bName)
  })

  return devices
})

// 📊 分页显示的设备列表（懒加载优化）
const paginatedDevices = computed(() => {
  return filteredDevices.value.slice(0, currentDisplayCount.value)
})

// 📊 是否还有更多设备
const hasMoreDevices = computed(() => {
  return currentDisplayCount.value < filteredDevices.value.length
})

// 🆕 半选状态（部分选中）
const isIndeterminate = computed(() => {
  const selectableDevices = filteredDevices.value.filter(d => !isDeviceImported(d))
  const selectedCount = selectedDeviceIds.value.length
  return selectedCount > 0 && selectedCount < selectableDevices.length
})

// 设备统计
const deviceStats = computed(() => {
  const imported = availableDevices.value.filter(d => isDeviceImported(d)).length
  const pending = availableDevices.value.filter(d => !isDeviceImported(d)).length
  const noCoordinates = availableDevices.value.filter(d => 
    d.id && (d.localX == null || d.localY == null)
  ).length
  
  return {
    total: availableDevices.value.length,
    imported,
    pending,
    noCoordinates
  }
})

// 楼层区域列表
const floorAreas = ref<any[]>([])

// 后端坐标参数（用于与后端SVG显示大小一致）
const backendCoordParams = ref({
  buildingWidth: 0,
  buildingLength: 0,
  coordinateScale: 0,
  hasSvg: false
})

// 计算属性
const isEmpty = computed(() => {
  return !canvas.value || (!loadedSvgData.value && loadedDevices.value.length === 0)
})

const emptyMessage = computed(() => {
  if (!loadedSvgData.value && loadedDevices.value.length === 0) {
    return '未找到平面图和设备数据，请先上传DXF文件并识别设备'
  } else if (!loadedSvgData.value) {
    return '未找到平面图数据，请先上传DXF文件'
  } else if (loadedDevices.value.length === 0) {
    return '未找到设备数据，请从左侧工具箱添加设备'
  }
  return ''
})

/**
 * 初始化
 */
onMounted(() => {
  console.log('[FloorPlanEditor] 组件挂载，floorId:', props.floorId)
  initCanvas()
  loadData()  // 自动加载数据
  
  // 添加事件监听
  window.addEventListener('resize', handleResize)
  window.addEventListener('keydown', handleKeyDown)
  
  // 初始化设备列表滚动监听
  initScrollListener()
  
  console.log('[FloorPlanEditor] 键盘事件监听已添加（Delete/Backspace 键删除设备）')
})

onBeforeUnmount(() => {
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('keydown', handleKeyDown)
  removeScrollListener()  // 移除滚动监听
  canvas.value?.dispose()
  
  console.log('[FloorPlanEditor] 事件监听已移除')
})

/**
 * 监听props变化
 */
// ⚠️ 注释掉 watch 监听，避免重复渲染
// 原因：props 变化时会触发重复加载，导致设备叠加显示
// 解决方案：只在 onMounted 时加载一次，后续通过手动刷新

/*
watch(() => props.svgData, (newVal) => {
  console.log('[FloorPlanEditor] svgData prop changed:', newVal?.substring(0, 100))
  if (newVal) {
    loadedSvgData.value = newVal
    loadSVGBackground()
  }
})

watch(() => props.devices, (newVal) => {
  console.log('[FloorPlanEditor] devices prop changed:', newVal?.length, '个设备')
  if (newVal && newVal.length > 0) {
    loadedDevices.value = newVal
    loadDevicesToCanvas()
  }
})
*/

/**
 * 加载所有数据（SVG + 可用设备列表 + 区域）
 * 
 * 🆕 优化后的流程：
 * 1. 加载 SVG 背景
 * 2. 加载可用设备列表（数据库 + DXF识别），但不自动导入到画布
 * 3. 加载已保存的设备（有坐标信息的），自动导入到画布
 * 4. 用户可以从左侧列表手动导入其他设备
 */
const loadData = async () => {
  console.log('[FloorPlanEditor] ========== 开始加载数据 ==========')
  console.log('[FloorPlanEditor] 当前楼层ID:', props.floorId)
  
  try {
    // ✅ 重要：先清空现有数据，防止重复
    loadedSvgData.value = ''
    loadedDevices.value = []
    availableDevices.value = []
    importedDeviceIds.value.clear()
    floorAreas.value = []
    
    // ✅ 清空画布
    if (canvas.value) {
      canvas.value.clear()
      selectedDevice.value = null
    }
    
    console.log('[FloorPlanEditor] 已清空旧数据和画布')
    
    // 0. 🔑 加载楼层和建筑信息（用于设备保存时的默认值）
    console.log('[FloorPlanEditor] [0/6] 加载楼层和建筑信息...')
    await loadFloorAndBuildingInfo()
    
    // 1. 🔑 加载产品列表（优先加载，设备需要关联产品）
    console.log('[FloorPlanEditor] [1/6] 加载产品列表...')
    await loadProductList()
    
    // 2. 加载 SVG 数据
    console.log('[FloorPlanEditor] [2/6] 加载SVG数据...')
    await loadSVGData()
    
    // 3. 加载可用设备列表（不自动导入到画布）
    console.log('[FloorPlanEditor] [3/6] 加载可用设备列表...')
    await loadAvailableDevices()
    
    // 4. 自动导入已保存的设备（有坐标的设备）
    console.log('[FloorPlanEditor] [4/6] 自动导入已保存设备...')
    autoImportSavedDevices()
    
    // 5. 加载楼层区域数据
    console.log('[FloorPlanEditor] [5/6] 加载楼层区域数据...')
    await loadFloorAreas()
    
    // 6. 渲染到画布
    console.log('[FloorPlanEditor] [6/6] 渲染画布...')
    renderCanvas()
    
    // 更新调试信息
    updateDebugInfo()
    
    console.log('[FloorPlanEditor] ========== 数据加载完成 ==========')
    console.log('[FloorPlanEditor] SVG已加载:', !!loadedSvgData.value)
    console.log('[FloorPlanEditor] 可用设备数量:', availableDevices.value.length)
    console.log('[FloorPlanEditor] 已导入设备数量:', loadedDevices.value.length)
    console.log('[FloorPlanEditor] 区域数量:', floorAreas.value.length)
    
    // 切换到设备列表标签页
    activeToolboxTab.value = 'available'
    
    ElMessage.success('数据加载完成')
  } catch (error: any) {
    console.error('[FloorPlanEditor] 加载数据失败:', error)
    ElMessage.error('加载数据失败: ' + error.message)
  }
}

/**
 * 🔑 加载楼层和建筑信息
 * 
 * 用于设备保存时自动填充 campusId、buildingId 等默认值
 */
const loadFloorAndBuildingInfo = async () => {
  try {
    // 1. 加载楼层信息
    if (props.floorId) {
      const floor = await FloorApi.getFloor(props.floorId)
      floorInfo.value = floor
      console.log('[FloorPlanEditor] ✅ 楼层信息加载成功:', floor.name)
      
      // 2. 如果有建筑ID，加载建筑信息
      if (floor.buildingId) {
        const building = await BuildingApi.getBuilding(floor.buildingId)
        buildingInfo.value = building
        console.log('[FloorPlanEditor] ✅ 建筑信息加载成功:', building.name)
        console.log('[FloorPlanEditor]   园区ID:', building.campusId || '无')
      } else {
        console.warn('[FloorPlanEditor] ⚠️ 楼层没有关联建筑')
        buildingInfo.value = null
      }
    }
  } catch (error: any) {
    console.error('[FloorPlanEditor] 加载楼层/建筑信息失败:', error)
    // 不影响主流程，继续执行
    floorInfo.value = null
    buildingInfo.value = null
  }
}

/**
 * 🔑 加载产品列表
 * 
 * 从后端获取所有可用产品，用于设备创建时关联产品ID
 */
const loadProductList = async () => {
  try {
    const data = await ProductApi.getSimpleProductList()
    productList.value = data || []
    
    console.log('[FloorPlanEditor] ✅ 产品列表加载成功，共', productList.value.length, '个产品')
    
    // 🎯 设置默认产品（用于DXF识别的设备）
    // 优先级：待确认设备 > 待确认摄像机 > 摄像机类产品 > 第一个产品
    if (productList.value.length > 0) {
      let selectedProduct: any = null
      
      // 1. 优先选择"待确认设备"或"默认设备"（最通用）
      selectedProduct = productList.value.find(p => 
        p.name && (
          p.name.includes('待确认设备') ||
          p.name.includes('默认设备') ||
          p.name.toLowerCase().includes('default_device')
        )
      )
      
      // 2. 如果没有通用默认产品，选择"待确认摄像机"（针对摄像头）
      if (!selectedProduct) {
        selectedProduct = productList.value.find(p => 
          p.name && (
            p.name.includes('待确认摄像机') ||
            p.name.includes('默认摄像机') ||
            p.name.toLowerCase().includes('default_camera')
          )
        )
      }
      
      // 3. 如果没有默认产品，选择普通摄像机类产品
      if (!selectedProduct) {
        selectedProduct = productList.value.find(p => 
          p.name && (
            p.name.includes('摄像机') || 
            p.name.includes('摄像头') ||
            p.name.toLowerCase().includes('camera')
          )
        )
      }
      
      // 4. 如果都没有，使用第一个产品
      if (!selectedProduct) {
        selectedProduct = productList.value[0]
      }
      
      // 5. 最终检查
      if (selectedProduct) {
        defaultProductId.value = selectedProduct.id
        console.log('[FloorPlanEditor] 设置默认产品:', selectedProduct.name, 'ID:', selectedProduct.id)
        
        // 📢 提示用户
        if (selectedProduct.name.includes('待确认') || selectedProduct.name.includes('默认')) {
          console.log('[FloorPlanEditor] 💡 使用默认产品，DXF识别的设备将使用此产品，请在导入后根据实际情况修改')
        }
      }
    } else {
      console.warn('[FloorPlanEditor] ⚠️ 产品列表为空，无法设置默认产品')
      ElMessage.warning('系统中没有产品，请先创建产品后再添加设备')
    }
  } catch (error: any) {
    console.error('[FloorPlanEditor] 加载产品列表失败:', error)
    ElMessage.error('加载产品列表失败: ' + error.message)
    productList.value = []
    defaultProductId.value = null
  }
}

/**
 * 加载 SVG 数据（优先级：props > 前端DXF解析）
 * 
 * 策略：使用前端DXF解析（无水印），但获取后端坐标参数来匹配显示大小
 */
const loadSVGData = async () => {
  console.log('[FloorPlanEditor] 加载SVG数据...')
  
  // 1. 优先使用 props
  if (props.svgData) {
    console.log('[FloorPlanEditor] 使用 props.svgData:', props.svgData.substring(0, 100))
    loadedSvgData.value = props.svgData
    return
  }
  
  // 2. 首先获取后端坐标参数（必需，用于与Aspose.CAD保持一致）
  try {
    console.log('[FloorPlanEditor] 🎯 获取后端坐标参数（关键）...')
    const response = await FloorDxfApi.getDxfInfo(props.floorId)
    const data = response.data || response
    
    // 保存后端坐标参数
    if (data.buildingWidth && data.buildingLength && data.coordinateScale) {
      backendCoordParams.value = {
        buildingWidth: data.buildingWidth,
        buildingLength: data.buildingLength,
        coordinateScale: data.coordinateScale,
        hasSvg: !!data.dxfLayer0Svg
      }
      console.log('[FloorPlanEditor] ✅ 后端坐标参数已获取:', backendCoordParams.value)
      console.log('[FloorPlanEditor] 🔑 coordinateScale =', data.coordinateScale.toFixed(2), '像素/米（将用于前端生成SVG）')
    } else {
      console.warn('[FloorPlanEditor] ⚠️ 后端坐标参数缺失，可能导致显示大小不一致')
    }
  } catch (error) {
    console.warn('[FloorPlanEditor] ⚠️ 后端坐标参数获取失败:', error)
  }
  
  // 3. 🎯 前端解析DXF（使用后端coordinateScale，无水印且大小一致）✨
  if (backendCoordParams.value.coordinateScale > 0) {
    try {
      console.log('[FloorPlanEditor] 🎨 前端解析DXF（无水印，与后端大小一致）...')
      
      // 获取DXF文件内容
      const response = await FloorDxfApi.getDxfFileContent(props.floorId)
      const dxfContent = typeof response === 'string' ? response : (response as any).data
      
      if (dxfContent && typeof dxfContent === 'string' && dxfContent.length > 0) {
        console.log('[FloorPlanEditor] 成功获取DXF内容，长度:', dxfContent.length)
        
        // 🎯 使用后端coordinateScale转换为SVG（与Aspose.CAD一致）
        const { convertDxfToSvgWithBackendScale } = await import('@/utils/dxf/dxfToSvg')
        const result = convertDxfToSvgWithBackendScale(
          dxfContent, 
          ['0'], 
          backendCoordParams.value.coordinateScale,  // 🔑 关键：使用后端的坐标比例
          1920, 
          1080
        )
        
        if (result && result.svg) {
          console.log('[FloorPlanEditor] ✅✅✅ 前端解析成功（无水印，大小与后端一致）')
          console.log('[FloorPlanEditor]   SVG长度:', result.svg.length)
          console.log('[FloorPlanEditor]   建筑尺寸:', result.buildingWidth.toFixed(2), 'm x', result.buildingLength.toFixed(2), 'm')
          console.log('[FloorPlanEditor]   坐标比例:', result.coordinateScale.toFixed(2), '像素/米（来自后端）')
          console.log('[FloorPlanEditor]   SVG尺寸: 1920x1080（与Aspose.CAD一致）')
          
          // 🔑 保存DXF偏移参数（用于设备坐标转换）
          if (result.dxfOffsetX !== undefined && result.dxfOffsetY !== undefined) {
            svgTransform.value.dxfOffsetX = result.dxfOffsetX
            svgTransform.value.dxfOffsetY = result.dxfOffsetY
            console.log('[FloorPlanEditor] 🔑 保存DXF偏移参数:', result.dxfOffsetX.toFixed(2), ',', result.dxfOffsetY.toFixed(2))
          }
          
          loadedSvgData.value = result.svg
          
          // 更新调试信息
          debugInfo.value.svgLoaded = true
          debugInfo.value.svgLength = result.svg.length
          
          return  // 成功！使用无水印且大小一致的SVG
        }
      }
    } catch (error) {
      console.warn('[FloorPlanEditor] ❌ 前端解析DXF失败:', error)
      console.warn('[FloorPlanEditor] 降级到后端SVG（有水印）...')
    }
  } else {
    console.warn('[FloorPlanEditor] ⚠️ 无后端coordinateScale，跳过前端解析')
  }
  
  // 4. 从API获取后端生成的SVG（后备方案，有水印但显示大小正确）
  try {
    console.log('[FloorPlanEditor] 使用后端SVG（有水印，后备方案）...')
    const response = await FloorDxfApi.getDxfInfo(props.floorId)
    const data = response.data || response
    
    if (data.dxfLayer0Svg) {
      console.log('[FloorPlanEditor] 从API获取SVG:', data.dxfLayer0Svg.substring(0, 100))
      console.warn('[FloorPlanEditor] ⚠️ 注意：使用后端SVG，包含Aspose.CAD水印')
      loadedSvgData.value = data.dxfLayer0Svg
      
      // 更新调试信息
      debugInfo.value.svgLoaded = true
      debugInfo.value.svgLength = data.dxfLayer0Svg.length
    } else {
      console.warn('[FloorPlanEditor] API返回的数据中没有 dxfLayer0Svg')
    }
  } catch (error) {
    console.error('[FloorPlanEditor] 从API获取SVG失败:', error)
  }
}

/**
 * 加载设备数据（优先级：props > API）
 * ⚠️ 已废弃：由 loadAvailableDevices() 替代
 */
const _loadDevicesData = async () => {
  console.log('[FloorPlanEditor] 加载设备数据...')
  
  // 1. 优先使用 props
  if (props.devices && props.devices.length > 0) {
    console.log('[FloorPlanEditor] 使用 props.devices:', props.devices.length, '个设备')
    loadedDevices.value = props.devices
    return
  }
  
  // 2. 从API获取（分页加载所有设备）
  try {
    const allDevices: any[] = []
    let pageNo = 1
    const pageSize = 100 // 后端限制最大为 100
    let hasMore = true
    
    while (hasMore) {
      const response = await DeviceApi.getDevicePage({
        floorId: props.floorId,
        pageNo: pageNo,
        pageSize: pageSize
      })
      
      if (response.list && response.list.length > 0) {
        allDevices.push(...response.list)
        console.log(`[FloorPlanEditor] 第${pageNo}页: ${response.list.length}个设备, 累计: ${allDevices.length}个`)
        
        // 检查是否还有更多数据
        hasMore = response.list.length === pageSize && response.total > allDevices.length
        pageNo++
      } else {
        hasMore = false
      }
    }
    
    if (allDevices.length > 0) {
      console.log('[FloorPlanEditor] 从API获取设备总计:', allDevices.length, '个')
      loadedDevices.value = allDevices
    } else {
      console.warn('[FloorPlanEditor] API返回的设备列表为空')
      loadedDevices.value = []
    }
  } catch (error) {
    console.error('[FloorPlanEditor] 从API获取设备失败:', error)
    loadedDevices.value = []
  }
}

/**
 * 🆕 加载可用设备列表（数据库设备 + DXF识别设备）
 */
const loadAvailableDevices = async () => {
  console.log('[FloorPlanEditor] 加载可用设备列表...')
  
  // 设置加载状态
  loadingDevices.value = true
  
  try {
    const allDevices: any[] = []
    
    // 1. 加载数据库中的设备
    console.log('[FloorPlanEditor] 从数据库加载设备...')
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
        console.log(`[FloorPlanEditor] 第${pageNo}页: ${response.list.length}个设备, 累计: ${allDevices.length}个`)
        
        hasMore = response.list.length === pageSize && response.total > allDevices.length
        pageNo++
      } else {
        hasMore = false
      }
    }
    
    console.log('[FloorPlanEditor] 数据库设备加载完成:', allDevices.length, '个')
    
    // 2. 加载DXF识别的设备
    console.log('[FloorPlanEditor] 加载DXF识别设备...')
    try {
      const response = await FloorDxfApi.recognizeByFloorId(props.floorId, [])
      const data = response.data || response
      
      if (data.devices && data.devices.length > 0) {
        console.log('[FloorPlanEditor] DXF识别到设备:', data.devices.length, '个')
        
        // 去重：排除已在数据库中的设备
        const existingIds = new Set(allDevices.map(d => d.id).filter(Boolean))
        
        const recognizedDevices = data.devices.filter((d: any) => {
          // 如果设备有ID且已存在于数据库，跳过
          if (d.id && existingIds.has(d.id)) {
            return false
          }
          // 为DXF识别的设备生成临时ID
          if (!d.id) {
            d.tempId = `dxf_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
          }
          return true
        })
        
        allDevices.push(...recognizedDevices)
        console.log('[FloorPlanEditor] 新增DXF识别设备:', recognizedDevices.length, '个')
      }
    } catch (error) {
      console.error('[FloorPlanEditor] 加载DXF识别设备失败:', error)
    }
    
    // 保存到可用设备列表
    availableDevices.value = allDevices
    
    // 重置显示数量（首次加载显示20个）
    currentDisplayCount.value = displayPageSize.value
    
    console.log('[FloorPlanEditor] ✅ 可用设备列表加载完成，共', allDevices.length, '个设备')
    
  } catch (error) {
    console.error('[FloorPlanEditor] 加载可用设备列表失败:', error)
    ElMessage.error('加载设备列表失败')
  } finally {
    loadingDevices.value = false
  }
}

/**
 * 🆕 自动导入已保存的设备（有坐标信息的设备）
 */
const autoImportSavedDevices = () => {
  console.log('[FloorPlanEditor] 自动导入已保存设备...')
  
  // 筛选出已保存的设备（有坐标且有ID）
  const savedDevices = availableDevices.value.filter(d => {
    return d.id && d.localX != null && d.localY != null
  })
  
  if (savedDevices.length > 0) {
    console.log('[FloorPlanEditor] 找到', savedDevices.length, '个已保存设备，自动导入到画布')
    savedDevices.forEach(device => {
      importDevice(device, false)  // false = 不显示提示
    })
    
    ElMessage.success(`已自动导入 ${savedDevices.length} 个已保存设备`)
  } else {
    console.log('[FloorPlanEditor] 没有找到已保存的设备')
  }
}

// ⚠️ 已删除：importSingleDevice() 和 importAllAvailableDevices()
// 已被 importSelectedDevices() 替代，新方法支持批量选择和导入

/**
 * 🆕 导入设备到画布（核心方法）
 */
const importDevice = (device: any, showMessage: boolean = true) => {
  // 检查是否已导入
  const deviceKey = device.id || device.tempId
  if (importedDeviceIds.value.has(deviceKey)) {
    if (showMessage) {
      ElMessage.warning('该设备已导入')
    }
    return
  }
  
  // 🆕 如果设备没有坐标，分配默认坐标（画布中心）
  const deviceToImport = { ...device }
  
  if (deviceToImport.localX == null || deviceToImport.localY == null) {
    // 计算画布中心的 DXF 米坐标
    // SVG 默认尺寸 1920x1080，中心点 (960, 540)
    const svgWidth = svgTransform.value.svgWidth || 1920
    const svgHeight = svgTransform.value.svgHeight || 1080
    const svgCenterX = svgWidth / 2
    const svgCenterY = svgHeight / 2
    
    if (backendCoordParams.value.coordinateScale > 0) {
      // 反向转换：SVG像素 → DXF米坐标
      const dxfOffsetX = svgTransform.value.dxfOffsetX || 0
      const dxfOffsetY = svgTransform.value.dxfOffsetY || 0
      
      // Y轴翻转
      const svgRawX = svgCenterX
      const svgRawY = svgHeight - svgCenterY
      
      // 减去偏移
      const pixelX = svgRawX - dxfOffsetX
      const pixelY = svgRawY - dxfOffsetY
      
      // 转换为米
      deviceToImport.localX = pixelX / backendCoordParams.value.coordinateScale
      deviceToImport.localY = pixelY / backendCoordParams.value.coordinateScale
      deviceToImport.localZ = 0
      
      console.log('[FloorPlanEditor] 🎯 为无坐标设备分配默认坐标:', {
        deviceName: device.deviceName || device.name,
        localX: deviceToImport.localX.toFixed(2),
        localY: deviceToImport.localY.toFixed(2)
      })
    } else {
      // 如果没有坐标系统，使用 (0, 0, 0)
      deviceToImport.localX = 0
      deviceToImport.localY = 0
      deviceToImport.localZ = 0
      
      console.log('[FloorPlanEditor] ⚠️ 无坐标系统，为设备分配原点坐标 (0,0,0):', device.deviceName || device.name)
    }
  }
  
  // 添加到已导入设备列表
  loadedDevices.value.push(deviceToImport)
  importedDeviceIds.value.add(deviceKey)
  
  console.log('[FloorPlanEditor] ✅ 导入设备:', deviceToImport.deviceName || deviceToImport.name, deviceKey)
  
  if (showMessage) {
    ElMessage.success(`已导入设备: ${deviceToImport.deviceName || deviceToImport.name}`)
  }
}

/**
 * 🆕 判断设备是否已导入
 */
const isDeviceImported = (device: any): boolean => {
  const deviceKey = device.id || device.tempId
  return importedDeviceIds.value.has(deviceKey)
}

/**
 * 🆕 定位设备（高亮并居中显示）
 */
const locateDevice = (device: any) => {
  if (!canvas.value) return
  
  // 在画布上查找对应的设备对象
  const fabricObjects = canvas.value.getObjects()
  const deviceObj = fabricObjects.find((obj: any) => {
    const objDeviceId = obj.deviceData?.id || obj.deviceData?.tempId
    const targetId = device.id || device.tempId
    return obj.objectType === 'device' && objDeviceId === targetId
  })
  
  if (deviceObj) {
    // 选中设备
    canvas.value.setActiveObject(deviceObj)
    canvas.value.renderAll()
    
    // 居中显示
    canvas.value.centerObject(deviceObj)
    
    ElMessage.success(`已定位设备: ${device.deviceName || device.name}`)
  } else {
    ElMessage.warning('设备不在画布上')
  }
}

/**
 * 🆕 根据设备类型获取图标
 */
const getDeviceIcon = (deviceType: string): string => {
  const template = deviceTemplates.value.find(t => t.type === deviceType || t.label === deviceType)
  return template?.icon || 'ep:question-filled'
}

/**
 * 🆕 根据设备类型获取颜色
 */
const getDeviceColor = (deviceType: string): string => {
  const template = deviceTemplates.value.find(t => t.type === deviceType || t.label === deviceType)
  return template?.color || '#909399'
}

/**
 * 🆕 从完整设备名称中提取简称（用于画布显示）
 * 
 * @example
 * "A栋-1F-摄像头1" → "摄像头1"
 * "B栋-2楼-门禁1" → "门禁1"
 * "摄像头1" → "摄像头1"（无分隔符时返回原名称）
 */
const getDeviceShortName = (fullName: string): string => {
  if (!fullName) return '未命名'
  
  // 支持多种分隔符："-" "—" "_" 
  const separators = ['-', '—', '_']
  
  for (const sep of separators) {
    if (fullName.includes(sep)) {
      // 按分隔符拆分，取最后一部分
      const parts = fullName.split(sep)
      const shortName = parts[parts.length - 1].trim()
      return shortName || fullName // 如果为空则返回原名称
    }
  }
  
  // 没有分隔符，直接返回原名称
  return fullName
}

/**
 * 🆕 全选/反选
 */
const handleSelectAll = (checked: boolean) => {
  if (checked) {
    // 全选：选中所有未导入的设备
    const selectableDevices = filteredDevices.value.filter(d => !isDeviceImported(d))
    selectedDeviceIds.value = selectableDevices.map(d => d.id || d.tempId)
  } else {
    // 反选：清空选择
    selectedDeviceIds.value = []
  }
}

/**
 * 🆕 搜索处理
 */
const handleDeviceSearch = () => {
  // 搜索时清空选择
  selectedDeviceIds.value = []
  selectAll.value = false
  // 重置分页
  currentDisplayCount.value = displayPageSize.value
}

/**
 * 🆕 筛选处理
 */
const handleDeviceFilter = () => {
  // 筛选时清空选择
  selectedDeviceIds.value = []
  selectAll.value = false
  // 重置分页
  currentDisplayCount.value = displayPageSize.value
}

/**
 * 📊 处理设备列表滚动（懒加载）
 */
const handleDeviceScroll = () => {
  if (loadingMoreDevices.value || !hasMoreDevices.value) return
  
  // 获取 el-scrollbar 的内部滚动容器
  if (!scrollbarWrap) return
  
  // 计算滚动位置
  const scrollTop = scrollbarWrap.scrollTop
  const scrollHeight = scrollbarWrap.scrollHeight
  const clientHeight = scrollbarWrap.clientHeight
  
  // 当滚动到底部附近（距离底部100px）时加载更多
  const threshold = 100
  if (scrollHeight - scrollTop - clientHeight < threshold) {
    console.log('[FloorPlanEditor] 触发加载更多，scrollTop:', scrollTop, 'scrollHeight:', scrollHeight, 'clientHeight:', clientHeight)
    loadMoreDevices()
  }
}

/**
 * 📊 初始化滚动监听
 */
const initScrollListener = () => {
  nextTick(() => {
    if (deviceScrollbar.value) {
      // 获取 el-scrollbar 的内部滚动容器（wrap 元素）
      scrollbarWrap = deviceScrollbar.value.$refs.wrap as HTMLElement
      
      if (scrollbarWrap) {
        console.log('[FloorPlanEditor] 滚动监听器已初始化')
        scrollbarWrap.addEventListener('scroll', handleDeviceScroll)
      } else {
        console.warn('[FloorPlanEditor] 未找到滚动容器')
      }
    }
  })
}

/**
 * 📊 移除滚动监听
 */
const removeScrollListener = () => {
  if (scrollbarWrap) {
    scrollbarWrap.removeEventListener('scroll', handleDeviceScroll)
    scrollbarWrap = null
  }
}

/**
 * 📊 加载更多设备（懒加载）
 */
const loadMoreDevices = async () => {
  if (loadingMoreDevices.value || !hasMoreDevices.value) return
  
  loadingMoreDevices.value = true
  
  try {
    // 模拟加载延迟（可选，让用户看到加载动画）
    await new Promise(resolve => setTimeout(resolve, 300))
    
    // 增加显示数量
    currentDisplayCount.value += displayPageSize.value
    
    console.log('[FloorPlanEditor] 加载更多设备，当前显示:', currentDisplayCount.value)
  } catch (error) {
    console.error('[FloorPlanEditor] 加载更多设备失败:', error)
  } finally {
    loadingMoreDevices.value = false
  }
}

/**
 * 🆕 切换设备选择
 */
const toggleDeviceSelection = (device: any) => {
  // 已导入的设备不能选择
  if (isDeviceImported(device)) {
    return
  }

  const deviceKey = device.id || device.tempId
  const index = selectedDeviceIds.value.indexOf(deviceKey)

  if (index > -1) {
    // 取消选择
    selectedDeviceIds.value.splice(index, 1)
  } else {
    // 添加选择
    selectedDeviceIds.value.push(deviceKey)
  }

  // 更新全选状态
  const selectableDevices = filteredDevices.value.filter(d => !isDeviceImported(d))
  selectAll.value = selectedDeviceIds.value.length === selectableDevices.length
}

/**
 * 🆕 判断设备是否选中
 */
const isDeviceSelected = (device: any): boolean => {
  const deviceKey = device.id || device.tempId
  return selectedDeviceIds.value.includes(deviceKey)
}

/**
 * 🆕 导入选中的设备
 */
const importSelectedDevices = () => {
  if (selectedDeviceIds.value.length === 0) {
    ElMessage.warning('请先选择要导入的设备')
    return
  }

  console.log('[FloorPlanEditor] 批量导入设备:', selectedDeviceIds.value.length, '个')

  let importCount = 0
  selectedDeviceIds.value.forEach(deviceKey => {
    const device = availableDevices.value.find(d => 
      (d.id || d.tempId) === deviceKey
    )
    if (device && !isDeviceImported(device)) {
      importDevice(device, false)
      importCount++
    }
  })

  if (importCount > 0) {
    ElMessage.success(`已导入 ${importCount} 个设备`)
    // 清空选择
    selectedDeviceIds.value = []
    selectAll.value = false
    // 重新渲染画布
    nextTick(() => {
      renderCanvas()
    })
  } else {
    ElMessage.info('所有选中设备已导入')
  }
}

/**
 * 加载楼层区域数据
 */
const loadFloorAreas = async () => {
  console.log('[FloorPlanEditor] 加载楼层区域数据...')
  
  try {
    const response = await AreaApi.getAreaListByFloorId(props.floorId)
    
    if (response && response.length > 0) {
      console.log('[FloorPlanEditor] 获取楼层区域:', response.length, '个')
      floorAreas.value = response
    } else {
      console.warn('[FloorPlanEditor] 该楼层暂无区域数据')
      floorAreas.value = []
    }
  } catch (error) {
    console.error('[FloorPlanEditor] 获取楼层区域失败:', error)
    floorAreas.value = []
  }
}

/**
 * 加载识别结果（补充设备）
 * 
 * ⚠️ 智能去重策略：
 * 1. 优先按ID去重（如果设备已保存到数据库）
 * 2. 其次按坐标去重（如果坐标完全相同，认为是同一设备）
 * 3. 只添加真正的"新设备"
 * ⚠️ 已废弃：由 loadAvailableDevices() 替代
 */
const _loadRecognitionResults = async () => {
  console.log('[FloorPlanEditor] 加载识别结果...')
  
  try {
    const response = await FloorDxfApi.recognizeByFloorId(props.floorId, [])
    const data = response.data || response
    
    if (data.devices && data.devices.length > 0) {
      console.log('[FloorPlanEditor] 识别到设备:', data.devices.length, '个')
      debugInfo.value.recognizedDevices = data.devices.length
      
      // 🎯 智能去重：按 ID + 坐标
      const existingIds = new Set(
        loadedDevices.value
          .map((d: any) => d.id)
          .filter(Boolean)
      )
      
      const existingCoords = new Set(
        loadedDevices.value
          .filter((d: any) => d.localX && d.localY)
          .map((d: any) => `${d.localX.toFixed(2)},${d.localY.toFixed(2)}`)
      )
      
      const newDevices = data.devices.filter((d: any) => {
        // 1. 如果设备有ID且已存在，跳过
        if (d.id && existingIds.has(d.id)) {
          console.log('[FloorPlanEditor] 跳过已存在设备(ID):', d.deviceName || d.name, 'ID:', d.id)
          return false
        }
        
        // 2. 如果设备坐标已存在，跳过
        const x = d.localX || d.x || 0
        const y = d.localY || d.y || 0
        const coordKey = `${x.toFixed(2)},${y.toFixed(2)}`
        
        if (x !== 0 && y !== 0 && existingCoords.has(coordKey)) {
          console.log('[FloorPlanEditor] 跳过已存在设备(坐标):', d.deviceName || d.name, '坐标:', coordKey)
          return false
        }
        
        // 3. 真正的新设备
        return true
      })
      
      if (newDevices.length > 0) {
        console.log('[FloorPlanEditor] ✅ 新增识别设备:', newDevices.length, '个')
        loadedDevices.value = [...loadedDevices.value, ...newDevices]
      } else {
        console.log('[FloorPlanEditor] ℹ️ 所有识别设备均已存在，无需添加')
      }
    }
  } catch (error) {
    console.error('[FloorPlanEditor] 加载识别结果失败:', error)
  }
}

/**
 * 渲染画布
 */
const renderCanvas = () => {
  if (!canvas.value) return
  
  console.log('[FloorPlanEditor] 开始渲染画布...')
  
  // ✅ 完全清空画布（移除所有对象）
  canvas.value.clear()
  
  // ✅ 重置选中状态
  selectedDevice.value = null
  
  // 1. 加载 SVG 背景
  if (loadedSvgData.value) {
    console.log('[FloorPlanEditor] 加载SVG背景...')
    loadSVGBackground()
  }
  
  // 2. 加载设备
  if (loadedDevices.value.length > 0) {
    console.log('[FloorPlanEditor] 加载', loadedDevices.value.length, '个设备...')
    loadDevicesToCanvas()
  }
  
  console.log('[FloorPlanEditor] 画布渲染完成')
}

/**
 * 更新调试信息
 */
const updateDebugInfo = () => {
  debugInfo.value.svgLoaded = !!loadedSvgData.value
  debugInfo.value.svgLength = loadedSvgData.value?.length || 0
  debugInfo.value.deviceCount = loadedDevices.value.length
  debugInfo.value.devicesWithCoords = loadedDevices.value.filter((d: any) => 
    d.localX !== null && d.localX !== undefined
  ).length
  
  console.log('[FloorPlanEditor] 调试信息:', debugInfo.value)
}

/**
 * 初始化 Fabric.js 画布
 */
const initCanvas = () => {
  console.log('[FloorPlanEditor] 初始化画布...')
  
  canvas.value = new fabric.Canvas('floor-plan-canvas-v2', {
    width: 1000,
    height: 700,
    backgroundColor: '#f5f5f5',
    selection: true
  })

  // 监听事件
  canvas.value.on('selection:created', handleObjectSelected)
  canvas.value.on('selection:updated', handleObjectSelected)
  canvas.value.on('selection:cleared', handleObjectDeselected)
  canvas.value.on('object:modified', handleObjectModified)
  canvas.value.on('mouse:move', handleMouseMove)
  
  console.log('[FloorPlanEditor] 画布初始化完成')
}

/**
 * 加载 SVG 背景图
 * 
 * 策略：如果有后端坐标参数，使用它来匹配后端SVG的显示大小
 */
const loadSVGBackground = () => {
  if (!loadedSvgData.value || !canvas.value) {
    console.warn('[FloorPlanEditor] 无法加载SVG: svgData=', !!loadedSvgData.value, ', canvas=', !!canvas.value)
    return
  }

  console.log('[FloorPlanEditor] 开始加载SVG到画布...')
  
  fabric.loadSVGFromString(loadedSvgData.value, (objects, options) => {
    console.log('[FloorPlanEditor] SVG解析完成，对象数量:', objects.length)
    
    const obj = fabric.util.groupSVGElements(objects, options)
    
    // 获取画布尺寸
    const canvasWidth = canvas.value?.width || 1000
    const canvasHeight = canvas.value?.height || 700
    
    // 获取 SVG 原始尺寸
    const svgWidth = obj.width || 1
    const svgHeight = obj.height || 1
    
    console.log('[FloorPlanEditor] SVG原始尺寸:', svgWidth, 'x', svgHeight)
    console.log('[FloorPlanEditor] 画布尺寸:', canvasWidth, 'x', canvasHeight)
    
    // 🎯 简化的缩放逻辑（前端SVG已经是1920x1080，与后端一致）
    // 只需将1920x1080的SVG适配到画布大小即可（留10%边距）
    const scale = Math.min(
      (canvasWidth * 0.9) / svgWidth,
      (canvasHeight * 0.9) / svgHeight
    )
    
    console.log('[FloorPlanEditor] 📐 SVG缩放信息:')
    console.log('  SVG尺寸:', svgWidth, 'x', svgHeight, '（应该是1920x1080）')
    console.log('  画布尺寸:', canvasWidth, 'x', canvasHeight)
    console.log('  缩放比例:', scale.toFixed(4), '（适配画布，留10%边距）')
    
    if (backendCoordParams.value.coordinateScale > 0) {
      console.log('[FloorPlanEditor] ✅ SVG已加载（前端dxf-parser生成，使用后端coordinateScale，无水印）')
      console.log('[FloorPlanEditor]    坐标比例:', backendCoordParams.value.coordinateScale.toFixed(2), '像素/米')
    } else {
      console.warn('[FloorPlanEditor] ⚠️ 无后端coordinateScale，SVG可能与查看平面图大小不一致')
    }
    
    // 应用缩放
    obj.scale(scale)
    
    // 计算居中位置
    const scaledWidth = svgWidth * scale
    const scaledHeight = svgHeight * scale
    const left = (canvasWidth - scaledWidth) / 2
    const top = (canvasHeight - scaledHeight) / 2
    
    console.log('[FloorPlanEditor] 居中位置:', left.toFixed(2), ',', top.toFixed(2))
    
    // 保存变换信息（供设备坐标转换使用）
    svgTransform.value = {
      scale: scale,
      offsetX: left,
      offsetY: top,
      svgWidth: svgTransform.value.svgWidth,    // 保持现有值
      svgHeight: svgTransform.value.svgHeight,  // 保持现有值
      dxfOffsetX: svgTransform.value.dxfOffsetX, // 保持现有值
      dxfOffsetY: svgTransform.value.dxfOffsetY  // 保持现有值
    }
    
    console.log('[FloorPlanEditor] SVG变换信息已保存:', svgTransform.value)
    
    // 设置位置和属性
    obj.set({
      left: left,
      top: top,
      selectable: false,
      evented: false,
      objectType: 'background'
    } as any)
    
    canvas.value?.add(obj)
    canvas.value?.sendToBack(obj)
    canvas.value?.requestRenderAll()
    
    console.log('[FloorPlanEditor] ✅ SVG加载并居中完成')
  })
}

/**
 * 加载设备到画布
 */
const loadDevicesToCanvas = () => {
  if (!canvas.value || !loadedDevices.value || loadedDevices.value.length === 0) {
    console.warn('[FloorPlanEditor] 无法加载设备: canvas=', !!canvas.value, ', devices=', loadedDevices.value?.length)
    return
  }

  console.log('[FloorPlanEditor] 开始加载', loadedDevices.value.length, '个设备到画布...')
  
  loadedDevices.value.forEach((device: any) => {
    try {
      addDeviceToCanvas(device)
    } catch (error) {
      console.error('[FloorPlanEditor] 添加设备失败:', device, error)
    }
  })
  
  console.log('[FloorPlanEditor] 设备加载完成')
  canvas.value?.requestRenderAll()
}

/**
 * 添加设备到画布
 */
const addDeviceToCanvas = (device: any) => {
  if (!canvas.value) return

  // 获取设备的 DXF 坐标（米）
  const dxfX = device.localX ?? device.x ?? null
  const dxfY = device.localY ?? device.y ?? null
  
  // 🔍 检查设备是否有有效坐标（null 表示未设置）
  if (dxfX === null || dxfY === null) {
    console.warn('[FloorPlanEditor] ⚠️ 设备坐标为空，跳过:', device.deviceName || device.name)
    console.warn('  提示：该设备未设置坐标，已被跳过渲染')
    return
  }
  
  // 🆕 允许坐标为 (0, 0) 的设备显示（可能是原点位置或默认坐标）
  if (dxfX === 0 && dxfY === 0) {
    console.log('[FloorPlanEditor] 📍 设备坐标为原点(0,0):', device.deviceName || device.name)
  }
  
  // 🎯 统一的坐标转换逻辑（与 dxfToSvg.ts 完全一致）
  // 
  // 转换步骤（必须与SVG生成逻辑一致）：
  // 1. DXF坐标（米）→ 像素
  // 2. 应用DXF偏移（居中）
  // 3. Y轴翻转（DXF Y向上 → SVG Y向下）
  // 4. 应用Fabric.js变换（缩放+偏移）
  
  let x, y
  
  if (backendCoordParams.value.coordinateScale > 0) {
    // ✅ 完整的坐标转换流程
    
    // 步骤1：DXF坐标（米）→ 像素
    const pixelX = dxfX * backendCoordParams.value.coordinateScale
    const pixelY = dxfY * backendCoordParams.value.coordinateScale
    
    // 步骤2：应用DXF偏移（与SVG生成时一致）
    const dxfOffsetX = svgTransform.value.dxfOffsetX || 0
    const dxfOffsetY = svgTransform.value.dxfOffsetY || 0
    const svgRawX = pixelX + dxfOffsetX
    const svgRawY = pixelY + dxfOffsetY
    
    // 步骤3：Y轴翻转（与 dxfToSvg.ts 的 flipY 函数一致）
    const svgHeight = svgTransform.value.svgHeight || 1080
    const svgX = svgRawX
    const svgY = svgHeight - svgRawY  // 🔑 关键：flipY = height - (y + offsetY)，但offsetY已经加过了
    
    // 步骤4：应用Fabric.js的缩放和偏移
    x = svgX * svgTransform.value.scale + svgTransform.value.offsetX
    y = svgY * svgTransform.value.scale + svgTransform.value.offsetY
    
    console.log('[FloorPlanEditor] ✅ 添加设备:', device.deviceName || device.name)
    console.log('  1️⃣ DXF坐标(m):', dxfX.toFixed(2), ',', dxfY.toFixed(2))
    console.log('  2️⃣ 像素坐标(px):', pixelX.toFixed(2), ',', pixelY.toFixed(2))
    console.log('  3️⃣ +偏移(px):', svgRawX.toFixed(2), ',', svgRawY.toFixed(2), `(offset: ${dxfOffsetX.toFixed(2)}, ${dxfOffsetY.toFixed(2)})`)
    console.log('  4️⃣ Y轴翻转(px):', svgX.toFixed(2), ',', svgY.toFixed(2), `(height: ${svgHeight})`)
    console.log('  5️⃣ 画布坐标(px):', x.toFixed(2), ',', y.toFixed(2))
  } else {
    // ⚠️ 方案B：后备方案（无coordinateScale）
    x = dxfX * svgTransform.value.scale + svgTransform.value.offsetX
    y = dxfY * svgTransform.value.scale + svgTransform.value.offsetY
    
    console.log('[FloorPlanEditor] ⚠️ 添加设备（无coordinateScale）:', device.deviceName || device.name)
    console.log('  设备坐标:', dxfX.toFixed(2), ',', dxfY.toFixed(2))
    console.log('  画布坐标:', x.toFixed(2), ',', y.toFixed(2))
    console.warn('  警告：没有后端coordinateScale，可能导致设备位置不准确')
  }
  
  const iconSize = device.deviceIconSize || device.iconSize || 30
  const deviceType = device.deviceType || device.type || '枪型摄像机'
  
  // 🎯 优先使用产品图标（基于统一的图标配置）
  let deviceIconKey = deviceType  // 默认使用设备类型
  let color = device.color
  
  // 如果设备有产品ID，从产品列表中查找对应的产品
  if (device.productId && productList.value.length > 0) {
    const product = productList.value.find(p => p.id === device.productId)
    if (product) {
      // 🔑 从统一的图标配置中获取图标和颜色
      const iconConfig = getIconConfigByProductName(product.name)
      
      if (iconConfig) {
        deviceIconKey = iconConfig.key  // 使用图标配置的键名
        if (!color) {
          color = iconConfig.color  // 使用图标配置的颜色
        }
        console.log('[FloorPlanEditor] 📦 使用图标配置:', iconConfig.label, '→', iconConfig.key)
      } else {
        // 降级：使用产品名称
        deviceIconKey = product.name
        if (!color) {
          color = getColorByProduct(product)
        }
        console.log('[FloorPlanEditor] 📦 使用产品名称:', product.name)
      }
    }
  } else {
    // 没有产品ID，使用设备类型推断图标
    deviceIconKey = getDeviceIcon(deviceType)
    console.log('[FloorPlanEditor] 🔧 使用设备类型图标:', deviceType)
  }
  
  // 如果还没有颜色，根据状态或设备类型获取
  if (!color) {
    color = device.status ? getDeviceStatusColor(device.status) : getDeviceColor(deviceType)
  }

  // 获取设备的SVG图标（getDeviceIconSvg 会根据名称从 deviceIconMap 查找）
  const svgString = getDeviceIconSvg(deviceIconKey)
  
  // 创建设备名称标签（显示在图标上方）
  // 🎯 画布上只显示简称（最后一部分），完整名称保存在 deviceData 中
  const fullName = device.deviceName || device.name || '未命名'
  const displayName = getDeviceShortName(fullName)
  
  const label = new fabric.Text(displayName, {
    fontSize: 12,
    fill: '#333',
    originX: 'center',
    originY: 'bottom'
  })

  // 使用 fabric.loadSVGFromString 加载 SVG 图标
  fabric.loadSVGFromString(svgString, (objects: fabric.Object[], options: any) => {
    if (!canvas.value || !objects || objects.length === 0) {
      console.warn('[FloorPlanEditor] 加载SVG图标失败，使用圆形替代')
      
      // 降级方案：使用圆形
      const circle = new fabric.Circle({
        radius: iconSize / 2,
        fill: color,
        stroke: '#333',
        strokeWidth: 2
      })
      
      const group = new fabric.Group([circle, label], {
        left: x,
        top: y,
        originX: 'center',
        originY: 'center',
        hasControls: true,
        hasBorders: true,
        objectType: 'device',
        deviceId: device.id,
        deviceData: {
          id: device.id,
          name: device.deviceName || device.name || '未命名',
          icon: device.deviceIcon || deviceType,
          color: color,
          iconSize: iconSize,
          x: dxfX,
          y: dxfY,
          z: device.localZ || device.z || 0,
          ...device
        }
      } as any)
      
      if (canvas.value) {
        canvas.value.add(group)
      }
      return
    }
    
    // SVG 加载成功，创建 SVG 图标
    const svgIcon = fabric.util.groupSVGElements(objects, options)
    
    // 缩放 SVG 到合适大小
    const scale = iconSize / Math.max(svgIcon.width || 1, svgIcon.height || 1)
    svgIcon.scale(scale)
    
    // 设置SVG颜色
    svgIcon.set({
      fill: color,
      originX: 'center',
      originY: 'center'
    })
    
    // 标签位置偏移（在SVG上方）
    label.set({
      top: -(iconSize / 2) - 5  // SVG上方5px
    })
    
    // 分组：SVG图标 + 标签
    const group = new fabric.Group([svgIcon, label], {
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
        id: device.id,
        name: device.deviceName || device.name || '未命名',
        icon: device.deviceIcon || deviceType,
        color: color,
        iconSize: iconSize,
        x: dxfX,  // 保存原始 DXF 坐标
        y: dxfY,
        z: device.localZ || device.z || 0,
        ...device
      }
    } as any)

    canvas.value?.add(group)
    canvas.value?.requestRenderAll()
  })
}

/**
 * 获取设备状态颜色（根据状态）
 */
const getDeviceStatusColor = (status: string) => {
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
 * 处理对象选中
 */
const handleObjectSelected = (e: any) => {
  const obj = e.selected?.[0] || e.target
  
  if (obj && (obj as any).objectType === 'device') {
    const deviceData = (obj as any).deviceData
    selectedDevice.value = {
      ...deviceData,
      deviceType: deviceData.deviceType || deviceData.type || '枪型摄像机',  // 🔑 确保有设备类型
      productId: deviceData.productId || defaultProductId.value,  // 🔑 确保有产品ID
      canvasObject: obj
    }
    console.log('[FloorPlanEditor] 选中设备:', selectedDevice.value.name)
    console.log('[FloorPlanEditor] 设备类型:', selectedDevice.value.deviceType)
    console.log('[FloorPlanEditor] 产品ID:', selectedDevice.value.productId)
  }
}

/**
 * 处理取消选中
 */
const handleObjectDeselected = () => {
  selectedDevice.value = null
  console.log('[FloorPlanEditor] 取消选中设备')
}

/**
 * 处理对象修改（拖拽后）
 * 
 * ⚠️ 反向转换：画布坐标 → DXF坐标
 */
const handleObjectModified = (e: any) => {
  const obj = e.target
  
  if ((obj as any).objectType === 'device') {
    // 🔄 反向转换：Canvas坐标 → DXF坐标（与handleMouseMove一致）
    const canvasX = obj.left || 0
    const canvasY = obj.top || 0
    
    let dxfX, dxfY
    
    if (backendCoordParams.value.coordinateScale > 0) {
      // 反向转换步骤（与正向转换对称）
      
      // 步骤1：Canvas → SVG（撤销Fabric.js变换）
      const svgX = (canvasX - svgTransform.value.offsetX) / svgTransform.value.scale
      const svgY = (canvasY - svgTransform.value.offsetY) / svgTransform.value.scale
      
      // 步骤2：Y轴翻转（SVG向下 → DXF向上）
      const svgHeight = svgTransform.value.svgHeight || 1080
      const svgRawY = svgHeight - svgY
      const svgRawX = svgX
      
      // 步骤3：撤销DXF偏移
      const dxfOffsetX = svgTransform.value.dxfOffsetX || 0
      const dxfOffsetY = svgTransform.value.dxfOffsetY || 0
      const pixelX = svgRawX - dxfOffsetX
      const pixelY = svgRawY - dxfOffsetY
      
      // 步骤4：像素 → 米
      dxfX = pixelX / backendCoordParams.value.coordinateScale
      dxfY = pixelY / backendCoordParams.value.coordinateScale
      
      console.log('[FloorPlanEditor] 🔄 设备拖拽反向转换:')
      console.log('  1️⃣ 画布坐标(px):', canvasX.toFixed(2), ',', canvasY.toFixed(2))
      console.log('  2️⃣ SVG坐标(px):', svgX.toFixed(2), ',', svgY.toFixed(2))
      console.log('  3️⃣ Y翻转+原始:', svgRawX.toFixed(2), ',', svgRawY.toFixed(2))
      console.log('  4️⃣ -偏移(px):', pixelX.toFixed(2), ',', pixelY.toFixed(2))
      console.log('  5️⃣ DXF坐标(m):', dxfX.toFixed(2), ',', dxfY.toFixed(2))
    } else {
      // ⚠️ 方案B：后备方案
      const svgX = (canvasX - svgTransform.value.offsetX) / svgTransform.value.scale
      const svgY = (canvasY - svgTransform.value.offsetY) / svgTransform.value.scale
      
      dxfX = svgX
      dxfY = svgY
      
      console.log('[FloorPlanEditor] 设备拖拽（无coordinateScale）:')
      console.log('  画布坐标:', canvasX.toFixed(2), ',', canvasY.toFixed(2))
      console.log('  DXF坐标:', dxfX.toFixed(2), ',', dxfY.toFixed(2))
    }
    
    // 更新选中设备的坐标
    if (selectedDevice.value && (obj as any).deviceId === selectedDevice.value.id) {
      selectedDevice.value.x = dxfX
      selectedDevice.value.y = dxfY
      
      // 同步更新 deviceData
      const deviceData = (obj as any).deviceData
      if (deviceData) {
        deviceData.x = dxfX
        deviceData.y = dxfY
      }
    }
  }
}

/**
 * 处理鼠标移动（显示坐标）
 */
const handleMouseMove = (e: any) => {
  if (!e.pointer) return
  
  // 🔄 反向转换：Canvas坐标 → DXF坐标（与正向转换完全对称）
  const canvasX = e.pointer.x
  const canvasY = e.pointer.y
  
  if (backendCoordParams.value.coordinateScale > 0) {
    // 反向转换步骤（与addDeviceToCanvas的正向转换对称）
    
    // 步骤1：Canvas → SVG（撤销Fabric.js变换）
    const svgX = (canvasX - svgTransform.value.offsetX) / svgTransform.value.scale
    const svgY = (canvasY - svgTransform.value.offsetY) / svgTransform.value.scale
    
    // 步骤2：Y轴翻转（SVG向下 → DXF向上）
    const svgHeight = svgTransform.value.svgHeight || 1080
    const svgRawY = svgHeight - svgY  // 反向翻转
    const svgRawX = svgX
    
    // 步骤3：撤销DXF偏移
    const dxfOffsetX = svgTransform.value.dxfOffsetX || 0
    const dxfOffsetY = svgTransform.value.dxfOffsetY || 0
    const pixelX = svgRawX - dxfOffsetX
    const pixelY = svgRawY - dxfOffsetY
    
    // 步骤4：像素 → 米
    const x = pixelX / backendCoordParams.value.coordinateScale
    const y = pixelY / backendCoordParams.value.coordinateScale
    
    mousePosition.value = { x, y }
  } else {
    const x = (canvasX - svgTransform.value.offsetX) / svgTransform.value.scale
    const y = (canvasY - svgTransform.value.offsetY) / svgTransform.value.scale
    
    mousePosition.value = { x, y }
  }
}

/**
 * 🆕 处理产品变化
 * 
 * 当用户在属性面板中选择不同的产品时触发
 * 自动更新设备的图标、颜色和产品关联
 */
const handleProductChange = async () => {
  if (!selectedDevice.value || !selectedDevice.value.productId) return
  
  console.log('[FloorPlanEditor] 产品已变更，ID:', selectedDevice.value.productId)
  
  // 根据产品ID查找对应的产品信息
  const product = productList.value.find(p => p.id === selectedDevice.value!.productId)
  
  if (product) {
    console.log('[FloorPlanEditor] 应用产品:', product.name)
    
    // 🔑 根据产品自动匹配图标和颜色
    const icon = getIconByProductName(product.name)
    const color = getColorByProduct(product)
    
    console.log('[FloorPlanEditor]   图标:', icon)
    console.log('[FloorPlanEditor]   颜色:', color)
    
    // 🔑 更新设备属性
    selectedDevice.value.deviceType = product.name  // 设备类型=产品名称（用于显示）
    selectedDevice.value.icon = icon
    selectedDevice.value.color = color
    
    // 🔑 更新画布对象的 deviceData
    const obj = selectedDevice.value.canvasObject
    if (obj && obj.deviceData) {
      obj.deviceData.productId = product.id
      obj.deviceData.deviceType = product.name
      obj.deviceData.icon = icon
      obj.deviceData.color = color
    }
    
    // 重新加载设备图标（因为图标类型改变了）
    await reloadDeviceIcon()
    
    ElMessage.success(`产品已更改为: ${product.name}`)
  } else {
    console.warn('[FloorPlanEditor] 未找到产品ID:', selectedDevice.value.productId)
    updateSelectedDevice()
  }
}

/**
 * 更新选中设备属性
 */
const updateSelectedDevice = async () => {
  if (!selectedDevice.value || !canvas.value) return
  
  const obj = selectedDevice.value.canvasObject
  if (!obj) return
  
  // 检查图标类型是否改变
  const currentIconType = selectedDevice.value.canvasObject.deviceData?.icon
  const iconTypeChanged = currentIconType && currentIconType !== selectedDevice.value.icon
  
  if (iconTypeChanged) {
    // 图标类型改变，需要重新加载SVG图标
    await reloadDeviceIcon()
  } else {
    // 图标类型未变，只更新颜色和大小
    updateDeviceIconStyle()
  }
  
  // 更新名称标签
  // 🎯 画布上只显示简称（最后一部分）
  const label = obj._objects[1] as fabric.Text
  if (label) {
    const displayName = getDeviceShortName(selectedDevice.value.name)
    label.set({
      text: displayName
    })
  }
  
  canvas.value.requestRenderAll()
}

/**
 * 更新设备图标样式（颜色、大小）
 */
const updateDeviceIconStyle = () => {
  if (!selectedDevice.value || !canvas.value) return
  
  const obj = selectedDevice.value.canvasObject
  if (!obj) return
  
  const iconObj = obj._objects[0]
  
  if (iconObj) {
    // 处理圆形图标
    if (iconObj.type === 'circle') {
      (iconObj as fabric.Circle).set({
        fill: selectedDevice.value.color,
        radius: selectedDevice.value.iconSize / 2
      })
    } 
    // 处理 SVG 图标（Group）
    else if (iconObj.type === 'group') {
      // 更新 SVG 颜色
      iconObj.set({ fill: selectedDevice.value.color })
      
      // 更新 SVG 大小
      const newScale = selectedDevice.value.iconSize / 30  // 30 是默认大小
      iconObj.scale(newScale)
    }
  }
}

/**
 * 重新加载设备图标（当图标类型改变时）
 */
const reloadDeviceIcon = async () => {
  if (!selectedDevice.value || !canvas.value) return
  
  const obj = selectedDevice.value.canvasObject
  if (!obj) return
  
  // 保存当前位置和属性
  const currentLeft = obj.left
  const currentTop = obj.top
  const deviceData = obj.deviceData
  
  // 加载新的SVG图标
  const iconSize = selectedDevice.value.iconSize || 30
  const color = selectedDevice.value.color || '#409EFF'
  const deviceType = selectedDevice.value.icon || '枪型摄像机'
  const svgString = getDeviceIconSvg(deviceType)
  
  return new Promise<void>((resolve) => {
    fabric.loadSVGFromString(svgString, (objects: fabric.Object[], options: any) => {
      if (!canvas.value || !objects || objects.length === 0) {
        // 加载失败，保持原图标不变
        console.warn('[FloorPlanEditor] SVG图标加载失败，保持原图标')
        resolve()
        return
      }
      
      // 创建新的SVG图标
      const svgIcon = fabric.util.groupSVGElements(objects, options)
      const scale = iconSize / Math.max(svgIcon.width || 1, svgIcon.height || 1)
      svgIcon.scale(scale)
      svgIcon.set({ fill: color, originX: 'center', originY: 'center' })
      
      // 创建标签
      const label = new fabric.Text(selectedDevice.value!.name || '未命名', {
        fontSize: 12,
        fill: '#333',
        originX: 'center',
        originY: 'bottom'
      })
      label.set({ top: -(iconSize / 2) - 5 })
      
      // 创建新的Group
      const newGroup = new fabric.Group([svgIcon, label], {
        left: currentLeft,
        top: currentTop,
        originX: 'center',
        originY: 'center',
        objectType: 'device',
        deviceId: selectedDevice.value!.id,
        deviceData: {
          ...deviceData,
          icon: deviceType  // 更新图标类型
        }
      } as any)
      
      // 移除旧对象，添加新对象
      canvas.value!.remove(obj)
      canvas.value!.add(newGroup)
      
      // 选中新对象
      canvas.value!.setActiveObject(newGroup)
      
      // 更新 selectedDevice 的引用
      selectedDevice.value!.canvasObject = newGroup as any
      
      console.log('[FloorPlanEditor] 设备图标已更换:', deviceType)
      resolve()
    })
  })
}

/**
 * 更新设备位置（从输入框）
 * 
 * ⚠️ 正向转换：DXF坐标 → 画布坐标
 */
const updateDevicePosition = () => {
  if (!selectedDevice.value || !canvas.value) return
  
  const obj = selectedDevice.value.canvasObject
  if (!obj) return
  
  // 🎯 正向转换：DXF坐标 → 画布坐标（与addDeviceToCanvas一致）
  const dxfX = selectedDevice.value.x
  const dxfY = selectedDevice.value.y
  
  let canvasX, canvasY
  
  if (backendCoordParams.value.coordinateScale > 0) {
    // 正向转换步骤（与addDeviceToCanvas完全一致）
    
    // 步骤1：DXF坐标（米）→ 像素
    const pixelX = dxfX * backendCoordParams.value.coordinateScale
    const pixelY = dxfY * backendCoordParams.value.coordinateScale
    
    // 步骤2：应用DXF偏移
    const dxfOffsetX = svgTransform.value.dxfOffsetX || 0
    const dxfOffsetY = svgTransform.value.dxfOffsetY || 0
    const svgRawX = pixelX + dxfOffsetX
    const svgRawY = pixelY + dxfOffsetY
    
    // 步骤3：Y轴翻转（DXF向上 → SVG向下）
    const svgHeight = svgTransform.value.svgHeight || 1080
    const svgX = svgRawX
    const svgY = svgHeight - svgRawY
    
    // 步骤4：应用Fabric.js变换
    canvasX = svgX * svgTransform.value.scale + svgTransform.value.offsetX
    canvasY = svgY * svgTransform.value.scale + svgTransform.value.offsetY
    
    console.log('[FloorPlanEditor] 🎯 更新设备位置（正向转换）:')
    console.log('  1️⃣ DXF坐标(m):', dxfX.toFixed(2), ',', dxfY.toFixed(2))
    console.log('  2️⃣ 像素坐标(px):', pixelX.toFixed(2), ',', pixelY.toFixed(2))
    console.log('  3️⃣ +偏移(px):', svgRawX.toFixed(2), ',', svgRawY.toFixed(2))
    console.log('  4️⃣ Y翻转(px):', svgX.toFixed(2), ',', svgY.toFixed(2))
    console.log('  5️⃣ 画布坐标(px):', canvasX.toFixed(2), ',', canvasY.toFixed(2))
  } else {
    canvasX = dxfX * svgTransform.value.scale + svgTransform.value.offsetX
    canvasY = dxfY * svgTransform.value.scale + svgTransform.value.offsetY
    
    console.log('[FloorPlanEditor] 更新设备位置（无coordinateScale）:')
    console.log('  DXF坐标:', dxfX.toFixed(2), ',', dxfY.toFixed(2))
    console.log('  画布坐标:', canvasX.toFixed(2), ',', canvasY.toFixed(2))
  }
  
  obj.set({
    left: canvasX,
    top: canvasY
  })
  
  // 同步更新 deviceData
  const deviceData = (obj as any).deviceData
  if (deviceData) {
    deviceData.x = dxfX
    deviceData.y = dxfY
  }
  
  canvas.value.requestRenderAll()
}

/**
 * 保存设备到服务器（保存当前选中的单个设备）
 */
const saveDeviceToServer = async () => {
  if (!selectedDevice.value) return
  
  saving.value = true
  
  try {
    // 🔑 确定产品ID（必填字段）
    let productId = selectedDevice.value.productId || defaultProductId.value
    
    if (!productId) {
      ElMessage.error('缺少产品信息，无法保存设备。请先创建产品。')
      saving.value = false
      return
    }
    
    // 🔑 准备设备数据（包含所有必填字段和合理的默认值）
    const deviceData = {
      // === 基础信息 ===
      id: selectedDevice.value.id,
      deviceName: selectedDevice.value.name,
      nickname: selectedDevice.value.nickname || selectedDevice.value.name,  // 备注名称（默认=设备名称）
      productId: productId,  // 🔑 必填：产品编号
      deviceType: 0,  // 🔑 设备类型（0=直连设备）
      serialNumber: selectedDevice.value.serialNumber || null,  // 设备序列号（可选）
      
      // === 定位信息 ===
      locationType: 3,  // 🔑 必填：定位类型（3=手动定位）
      
      // === 空间定位字段 ===
      campusId: buildingInfo.value?.campusId || null,  // 🔑 所属园区ID（从建筑信息获取）
      buildingId: floorInfo.value?.buildingId || null,  // 🔑 所属建筑ID（从楼层信息获取）
      floorId: props.floorId,  // 🔑 所属楼层ID
      roomId: selectedDevice.value.roomId || null,  // 所属区域ID（房间）
      
      // === 坐标信息 ===
      localX: selectedDevice.value.x,  // X坐标（米）
      localY: selectedDevice.value.y,  // Y坐标（米）
      localZ: selectedDevice.value.z || 2.8,  // Z坐标（安装高度，米，默认2.8米吊顶高度）
      
      // === 安装信息 ===
      installHeightType: selectedDevice.value.installHeightType || 'ceiling',  // 🔑 安装高度类型（默认：ceiling天花板）
      installLocation: selectedDevice.value.installLocation || '天花板中央'  // 🔑 安装位置描述（默认值）
    } as any
    
    await DeviceApi.updateDevice(deviceData)
    
    // 🔑 同步更新画布对象的deviceData
    const obj = selectedDevice.value.canvasObject
    if (obj && obj.deviceData) {
      obj.deviceData.deviceName = deviceData.deviceName
      obj.deviceData.deviceType = deviceData.deviceType
      obj.deviceData.icon = deviceData.deviceIcon
      obj.deviceData.iconSize = deviceData.deviceIconSize
    }
    
    ElMessage.success('设备属性已保存')
    emit('device-updated', selectedDevice.value)
  } catch (error: any) {
    ElMessage.error('保存失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

/**
 * 删除选中设备
 */
const deleteSelectedDevice = () => {
  if (!selectedDevice.value || !canvas.value) return
  
  ElMessageBox.confirm(
    `确定要删除设备 "${selectedDevice.value.name}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    if (canvas.value && selectedDevice.value) {
      canvas.value.remove(selectedDevice.value.canvasObject)
      ElMessage.success('设备已删除')
      selectedDevice.value = null
      canvas.value.requestRenderAll()
    }
  }).catch(() => {
    // 取消删除
  })
}

/**
 * 处理键盘事件（Del 键删除设备）
 */
const handleKeyDown = (event: KeyboardEvent) => {
  // 检查是否在输入框中（避免误删除）
  const target = event.target as HTMLElement
  if (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA') {
    return
  }
  
  // Delete 或 Backspace 键
  if (event.key === 'Delete' || event.key === 'Backspace') {
    if (selectedDevice.value) {
      event.preventDefault()  // 阻止浏览器后退
      
      console.log('[FloorPlanEditor] 按下 Delete 键，删除设备:', selectedDevice.value.name)
      
      // 直接删除，不弹确认框（更快捷）
      canvas.value?.remove(selectedDevice.value.canvasObject)
      ElMessage.success(`已删除设备: ${selectedDevice.value.name}`)
      selectedDevice.value = null
      canvas.value?.requestRenderAll()
    }
  }
}

/**
 * 保存整个平面图（智能同步数据库与画布）
 * 
 * 策略：
 * 1. 画布上的设备：
 *    - 已有ID → 更新坐标和属性
 *    - 没有ID → 创建新设备
 * 2. 数据库中但不在画布上的设备：
 *    - 软删除（清空坐标，保留设备记录）
 * 
 * 这样确保数据库与画布完全同步，且不会真正删除设备数据
 */
const saveFloorPlan = async () => {
  saving.value = true
  
  try {
    // ========================================
    // 🔍 第一步：获取当前楼层所有设备（数据库）- 分页加载
    // ========================================
    let dbDevices: any[] = []
    try {
      let pageNo = 1
      const pageSize = 100  // 后端限制最大100
      let hasMore = true
      
      while (hasMore) {
        const response = await DeviceApi.getDevicePage({
          floorId: props.floorId,
          pageNo: pageNo,
          pageSize: pageSize
        })
        
        const list = response?.list || []
        dbDevices.push(...list)
        
        // 判断是否还有更多数据
        hasMore = list.length >= pageSize
        pageNo++
        
        console.log(`[FloorPlanEditor] 📄 加载第 ${pageNo - 1} 页，获取 ${list.length} 个设备`)
      }
      
      console.log('[FloorPlanEditor] 🗄️ 数据库中该楼层共有', dbDevices.length, '个设备')
    } catch (error) {
      console.error('[FloorPlanEditor] ❌ 获取数据库设备失败:', error)
    }
    
    // 🔑 创建数据库设备ID的映射（用于快速查找）
    const dbDeviceMap = new Map()
    dbDevices.forEach(device => {
      dbDeviceMap.set(device.id, device)
    })
    
    // ========================================
    // 🔍 第二步：获取画布上所有设备
    // ========================================
    const deviceObjects = canvas.value?.getObjects().filter((obj: any) => obj.objectType === 'device') || []
    console.log('[FloorPlanEditor] 🎨 画布上共有', deviceObjects.length, '个设备')
    
    // ========================================
    // 🔍 第三步：通过DXF实体ID关联已存在的设备
    // ========================================
    console.log('[FloorPlanEditor] 🔗 开始关联DXF设备...')
    
    // 🔑 创建DXF实体ID映射（用于快速查找）
    const dxfDeviceMap = new Map()
    dbDevices.forEach(device => {
      if (device.dxfEntityId) {
        dxfDeviceMap.set(device.dxfEntityId, device)
      }
    })
    
    console.log('[FloorPlanEditor] 📊 数据库中有', dxfDeviceMap.size, '个DXF设备')
    
    // 🔑 遍历画布设备，尝试通过DXF实体ID关联
    let dxfLinkCount = 0
    deviceObjects.forEach((obj: any) => {
      const deviceData = obj.deviceData
      const dxfEntityId = deviceData.dxfEntityId || deviceData.handle
      
      // 如果设备有DXF实体ID，且数据库中存在对应设备
      if (dxfEntityId && dxfDeviceMap.has(dxfEntityId)) {
        const existingDevice = dxfDeviceMap.get(dxfEntityId)
        
        // 🔑 关联已存在的设备（使用数据库中的ID和名称）
        if (!deviceData.id || deviceData.id !== existingDevice.id) {
          console.log(`[FloorPlanEditor] 🔗 关联DXF设备: "${deviceData.name}" → "${existingDevice.deviceName}" (ID: ${existingDevice.id})`)
          
          deviceData.id = existingDevice.id
          obj.deviceId = existingDevice.id
          deviceData.name = existingDevice.deviceName
          deviceData.deviceName = existingDevice.deviceName
          deviceData.nickname = existingDevice.nickname
          deviceData.isImported = true  // 标记为已导入
          
          dxfLinkCount++
        }
      }
    })
    
    console.log(`[FloorPlanEditor] ✅ 成功关联 ${dxfLinkCount} 个DXF设备`)
    
    // ========================================
    // 🔍 第四步：验证画布设备ID，清理无效ID
    // ========================================
    console.log('[FloorPlanEditor] 🔍 开始验证设备ID...')
    
    deviceObjects.forEach((obj: any) => {
      const deviceData = obj.deviceData
      
      // 🔑 关键：如果设备有ID，但在数据库中不存在，则清空ID
      if (deviceData.id && !dbDeviceMap.has(deviceData.id)) {
        console.warn(`[FloorPlanEditor] ⚠️ 设备 "${deviceData.name}" 的ID ${deviceData.id} 在数据库中不存在，已清空ID（将作为新设备创建）`)
        
        // 清空无效的ID
        deviceData.id = null
        obj.deviceId = null
        
        // 🔑 自动为设备名称添加序号，避免重名
        const baseName = deviceData.name || deviceData.deviceName || '未命名设备'
        const timestamp = Date.now().toString().slice(-4)  // 取时间戳后4位
        deviceData.name = `${baseName}_${timestamp}`
        deviceData.deviceName = deviceData.name
        
        console.log(`[FloorPlanEditor] ✏️ 已重命名为: ${deviceData.name}`)
      }
    })
    
    // ========================================
    // 🔍 第五步：重新计算画布设备ID集合（清理无效ID后）
    // ========================================
    const canvasDeviceIds = new Set(
      deviceObjects
        .map((obj: any) => obj.deviceData?.id)
        .filter((id: any) => id != null)
    )
    
    console.log('[FloorPlanEditor] 📊 有效的画布设备ID集合:', Array.from(canvasDeviceIds))
    
    // ========================================
    // 🔍 第六步：分类设备（更新 vs 创建）
    // ========================================
    const devicesToUpdate: any[] = []  // 需要更新的设备
    const devicesToCreate: any[] = []  // 需要创建的设备
    
    deviceObjects.forEach((obj: any) => {
      const deviceData = obj.deviceData
      const canvasX = obj.left || 0
      const canvasY = obj.top || 0
      
      // 🔄 反向转换：Canvas坐标 → DXF坐标（米）
      let dxfX, dxfY
      
      if (backendCoordParams.value.coordinateScale > 0) {
        // 反向转换步骤（与handleObjectModified一致）
        
        // 步骤1：Canvas → SVG
        const svgX = (canvasX - svgTransform.value.offsetX) / svgTransform.value.scale
        const svgY = (canvasY - svgTransform.value.offsetY) / svgTransform.value.scale
        
        // 步骤2：Y轴翻转（SVG向下 → DXF向上）
        const svgHeight = svgTransform.value.svgHeight || 1080
        const svgRawY = svgHeight - svgY
        const svgRawX = svgX
        
        // 步骤3：撤销DXF偏移
        const dxfOffsetX = svgTransform.value.dxfOffsetX || 0
        const dxfOffsetY = svgTransform.value.dxfOffsetY || 0
        const pixelX = svgRawX - dxfOffsetX
        const pixelY = svgRawY - dxfOffsetY
        
        // 步骤4：像素 → 米
        dxfX = pixelX / backendCoordParams.value.coordinateScale
        dxfY = pixelY / backendCoordParams.value.coordinateScale
      } else {
        // 方案B：后备方案
        const svgX = (canvasX - svgTransform.value.offsetX) / svgTransform.value.scale
        const svgY = (canvasY - svgTransform.value.offsetY) / svgTransform.value.scale
        
        dxfX = svgX
        dxfY = svgY
      }
      
      // 🔑 确定产品ID（必填字段）
      let productId = deviceData.productId || defaultProductId.value
      
      if (!productId && productList.value.length > 0) {
        // 如果没有productId，尝试根据设备类型匹配产品
        const matchedProduct = productList.value.find(p => 
          p.name && deviceData.deviceType && 
          p.name.includes(deviceData.deviceType)
        )
        productId = matchedProduct ? matchedProduct.id : productList.value[0].id
        console.log(`[FloorPlanEditor] 设备 ${deviceData.name || deviceData.deviceName} 自动匹配产品:`, matchedProduct?.name || productList.value[0].name)
      }
      
      if (!productId) {
        console.error(`[FloorPlanEditor] ❌ 设备 ${deviceData.name || deviceData.deviceName} 缺少产品ID，跳过保存`)
        failCount++
        return  // 跳过此设备
      }
      
      // 🔑 生成设备名称（新设备自动添加建筑+楼层前缀）
      let deviceName = deviceData.name || deviceData.deviceName
      
      // 如果是新设备（无ID），且名称未包含建筑信息，则自动添加前缀
      if (!deviceData.id && buildingInfo.value && floorInfo.value) {
        const buildingName = buildingInfo.value.name || ''
        const floorName = floorInfo.value.name || ''
        
        // 检查名称是否已包含建筑或楼层信息（避免重复添加）
        const hasPrefix = deviceName.includes(buildingName) || deviceName.includes(floorName)
        
        if (!hasPrefix && buildingName && floorName) {
          // 生成格式："建筑名-楼层名-设备名"
          // 例如："A栋-1F-摄像头1"
          deviceName = `${buildingName}-${floorName}-${deviceName}`
          console.log(`[FloorPlanEditor] 📝 自动生成设备名称: ${deviceName}`)
        }
      }
      
      // 🔑 准备设备数据（包含所有必填字段和合理的默认值）
      const deviceInfo = {
        // === 基础信息 ===
        id: deviceData.id,
        deviceName: deviceName,
        nickname: deviceData.nickname || deviceName,  // 备注名称（默认=设备名称）
        productId: productId,  // 🔑 必填：产品编号
        deviceType: 0,  // 🔑 设备类型（0=直连设备，1=网关子设备，2=网关设备）
        serialNumber: deviceData.serialNumber || null,  // 设备序列号（可选）
        dxfEntityId: deviceData.dxfEntityId || deviceData.handle || null,  // 🔑 DXF实体ID（用于设备绑定）
        
        // === 定位信息 ===
        locationType: 3,  // 🔑 必填：定位类型（3=手动定位）
        
        // === 空间定位字段 ===
        campusId: buildingInfo.value?.campusId || null,  // 🔑 所属园区ID（从建筑信息获取）
        buildingId: floorInfo.value?.buildingId || null,  // 🔑 所属建筑ID（从楼层信息获取）
        floorId: props.floorId,  // 🔑 所属楼层ID
        roomId: deviceData.roomId || null,  // 所属区域ID（房间）
        
        // === 坐标信息 ===
        localX: dxfX,  // X坐标（米）
        localY: dxfY,  // Y坐标（米）
        localZ: deviceData.z || 2.8,  // Z坐标（安装高度，米，默认2.8米吊顶高度）
        
        // === 安装信息 ===
        installHeightType: 'ceiling',  // 🔑 安装高度类型（默认：ceiling天花板）
        installLocation: deviceData.installLocation || '天花板中央'  // 🔑 安装位置描述（默认值）
      }
      
      if (deviceData.id) {
        // 已有ID：更新
        devicesToUpdate.push(deviceInfo)
      } else {
        // 没有ID：创建
        devicesToCreate.push(deviceInfo)
      }
    })
    
    console.log('[FloorPlanEditor] 📊 设备分类:')
    console.log('  需要更新:', devicesToUpdate.length, '个')
    console.log('  需要创建:', devicesToCreate.length, '个')
    
    let successCount = 0
    let failCount = 0
    
    // ========================================
    // 🔄 第七步：批量更新已有设备
    // ========================================
    if (devicesToUpdate.length > 0) {
      console.log('[FloorPlanEditor] 🔄 批量更新设备坐标...')
      for (const device of devicesToUpdate) {
        try {
          await DeviceApi.updateDevice(device)
          successCount++
          console.log(`[FloorPlanEditor] ✅ 更新成功: ${device.deviceName}`)
        } catch (error: any) {
          failCount++
          console.error(`[FloorPlanEditor] ❌ 更新失败: ${device.deviceName}`, error)
        }
      }
    }
    
    // ========================================
    // 🆕 第八步：批量创建新设备
    // ========================================
    if (devicesToCreate.length > 0) {
      console.log('[FloorPlanEditor] 🆕 批量创建新设备...')
      for (const device of devicesToCreate) {
        try {
          const result = await DeviceApi.createDevice(device)
          successCount++
          console.log(`[FloorPlanEditor] ✅ 创建成功: ${device.deviceName}`, result)
          
          // 🔑 重要：更新画布对象的ID（下次保存时就是更新而不是创建）
          const canvasObj = deviceObjects.find((obj: any) => 
            obj.deviceData.name === device.deviceName && !obj.deviceData.id
          ) as any
          if (canvasObj && result.id) {
            canvasObj.deviceId = result.id
            canvasObj.deviceData.id = result.id
          }
        } catch (error: any) {
          failCount++
          console.error(`[FloorPlanEditor] ❌ 创建失败: ${device.deviceName}`, error)
        }
      }
    }
    
    // ========================================
    // 🗑️ 第九步：删除数据库中但不在画布上的设备
    // ========================================
    const devicesToDelete: any[] = []
    
    if (dbDevices.length > 0) {
      // 找出数据库中有坐标，但不在画布上的设备
      dbDevices.forEach(dbDevice => {
        const isOnCanvas = canvasDeviceIds.has(dbDevice.id)
        const hasCoordinates = dbDevice.localX != null && dbDevice.localY != null
        
        if (!isOnCanvas && hasCoordinates) {
          // 这个设备在数据库中，有坐标，但不在画布上 → 需要删除
          devicesToDelete.push({
            id: dbDevice.id,
            deviceName: dbDevice.deviceName
          })
        }
      })
    }
    
    console.log('[FloorPlanEditor] 🗑️ 需要删除:', devicesToDelete.length, '个设备')
    
    let deleteCount = 0
    
    if (devicesToDelete.length > 0) {
      console.log('[FloorPlanEditor] 🗑️ 开始删除设备...')
      
      // 使用批量删除接口
      const idsToDelete = devicesToDelete.map(d => d.id)
      try {
        await DeviceApi.deleteDeviceList(idsToDelete)
        deleteCount = idsToDelete.length
        console.log(`[FloorPlanEditor] 🗑️ 批量删除成功: ${deleteCount} 个设备`)
        devicesToDelete.forEach(d => {
          console.log(`  - ${d.deviceName} (ID: ${d.id})`)
        })
      } catch (error: any) {
        console.error(`[FloorPlanEditor] ❌ 批量删除失败:`, error)
        ElMessage.error('删除设备失败: ' + error.message)
      }
    }
    
    // ========================================
    // 📊 第十步：显示保存结果
    // ========================================
    console.log('[FloorPlanEditor] 💾 保存完成:')
    console.log('  成功:', successCount, '个')
    console.log('  失败:', failCount, '个')
    console.log('  删除:', deleteCount, '个')
    
    // 根据结果显示不同的提示
    if (failCount === 0 && deleteCount === 0) {
      ElMessage.success(`平面图已保存！共 ${successCount} 个设备`)
      emit('success')
    } else if (failCount === 0 && deleteCount > 0) {
      ElMessage.success(`平面图已保存！保存 ${successCount} 个，删除 ${deleteCount} 个`)
      emit('success')
    } else if (successCount > 0) {
      ElMessage.warning(`部分保存成功：${successCount} 个成功，${failCount} 个失败，${deleteCount} 个已删除`)
    } else {
      ElMessage.error(`保存失败：${failCount} 个设备保存失败`)
    }
    
  } catch (error: any) {
    console.error('[FloorPlanEditor] 保存平面图失败:', error)
    ElMessage.error('保存失败: ' + error.message)
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
  zoomLevel.value = Math.min(zoomLevel.value + 0.1, 3)
  canvas.value?.setZoom(zoomLevel.value)
}

const zoomOut = () => {
  zoomLevel.value = Math.max(zoomLevel.value - 0.1, 0.5)
  canvas.value?.setZoom(zoomLevel.value)
}

const zoomReset = () => {
  zoomLevel.value = 1
  canvas.value?.setZoom(1)
  canvas.value?.viewportCenterObject(canvas.value.getObjects()[0])
}

const handleResize = () => {
  // TODO: 响应式调整画布大小
}

/**
 * 拖放功能：开始拖动
 */
const handleDragStart = (event: DragEvent, template: any) => {
  event.dataTransfer!.effectAllowed = 'copy'
  event.dataTransfer!.setData('deviceTemplate', JSON.stringify(template))
  console.log('[FloorPlanEditor] 开始拖动设备模板:', template.label)
}

/**
 * 拖放功能：放下
 */
const handleDrop = (event: DragEvent) => {
  event.preventDefault()
  
  const templateData = event.dataTransfer!.getData('deviceTemplate')
  if (!templateData) return
  
  const template = JSON.parse(templateData)
  
  // 计算放下位置
  const canvasEl = canvas.value?.getElement()
  if (!canvasEl) return
  
  const rect = canvasEl.getBoundingClientRect()
  const x = (event.clientX - rect.left) / props.coordinateScale
  const y = (event.clientY - rect.top) / props.coordinateScale
  
  console.log('[FloorPlanEditor] 放下设备模板:', template.label, 'at', x, y)
  
  // 创建新设备
  const newDevice = {
    name: template.label + '_' + Date.now(),
    deviceType: template.type,
    localX: x,
    localY: y,
    localZ: 0,
    deviceIcon: template.icon,
    color: template.color,
    iconSize: 20
  }
  
  addDeviceToCanvas(newDevice)
  canvas.value?.requestRenderAll()
  
  ElMessage.success('已添加设备: ' + newDevice.name)
}

/**
 * 从模板添加设备（点击）
 */
const addDeviceFromTemplate = (template: any) => {
  if (!canvas.value) return
  
  // 在画布中心添加
  const centerX = (canvas.value.width || 500) / 2 / props.coordinateScale
  const centerY = (canvas.value.height || 350) / 2 / props.coordinateScale
  
  const newDevice = {
    name: template.label + '_' + Date.now(),
    deviceType: template.type,
    localX: centerX,
    localY: centerY,
    localZ: 0,
    deviceIcon: template.icon,
    color: template.color,
    iconSize: 20
  }
  
  addDeviceToCanvas(newDevice)
  canvas.value.requestRenderAll()
  
  ElMessage.success('已添加设备: ' + newDevice.name)
}

/**
 * 处理画布右键菜单
 */
const handleCanvasContextMenu = (event: MouseEvent) => {
  event.preventDefault()
  
  if (!canvas.value) return
  
  // 获取画布容器
  const canvasContainer = event.currentTarget as HTMLElement
  const rect = canvasContainer.getBoundingClientRect()
  
  // 计算菜单显示位置（相对于画布容器）
  contextMenu.value.x = event.clientX - rect.left
  contextMenu.value.y = event.clientY - rect.top
  
  // 计算 Fabric.js 画布坐标
  const pointer = canvas.value.getPointer(event, true)
  contextMenu.value.canvasX = pointer.x
  contextMenu.value.canvasY = pointer.y
  
  // 🔄 计算 DXF 坐标（米）- 反向转换
  if (backendCoordParams.value.coordinateScale > 0) {
    // 反向转换步骤（与handleMouseMove一致）
    
    // 步骤1：Canvas → SVG
    const svgX = (pointer.x - svgTransform.value.offsetX) / svgTransform.value.scale
    const svgY = (pointer.y - svgTransform.value.offsetY) / svgTransform.value.scale
    
    // 步骤2：Y轴翻转
    const svgHeight = svgTransform.value.svgHeight || 1080
    const svgRawY = svgHeight - svgY
    const svgRawX = svgX
    
    // 步骤3：撤销DXF偏移
    const dxfOffsetX = svgTransform.value.dxfOffsetX || 0
    const dxfOffsetY = svgTransform.value.dxfOffsetY || 0
    const pixelX = svgRawX - dxfOffsetX
    const pixelY = svgRawY - dxfOffsetY
    
    // 步骤4：像素 → 米
    contextMenu.value.dxfX = pixelX / backendCoordParams.value.coordinateScale
    contextMenu.value.dxfY = pixelY / backendCoordParams.value.coordinateScale
  } else {
    // ⚠️ 方案B：后备方案
    const svgX = (pointer.x - svgTransform.value.offsetX) / svgTransform.value.scale
    const svgY = (pointer.y - svgTransform.value.offsetY) / svgTransform.value.scale
    
    contextMenu.value.dxfX = svgX
    contextMenu.value.dxfY = svgY
  }
  
  console.log('[FloorPlanEditor] 右键菜单:')
  console.log('  屏幕坐标:', event.clientX, event.clientY)
  console.log('  菜单位置:', contextMenu.value.x, contextMenu.value.y)
  console.log('  画布坐标(px):', pointer.x.toFixed(2), pointer.y.toFixed(2))
  console.log('  DXF坐标(m):', contextMenu.value.dxfX.toFixed(2), contextMenu.value.dxfY.toFixed(2), '(Y向上)')
  
  // 显示菜单
  contextMenu.value.visible = true
  
  // 点击其他地方关闭菜单
  setTimeout(() => {
    const closeMenu = () => {
      contextMenu.value.visible = false
      document.removeEventListener('click', closeMenu)
    }
    document.addEventListener('click', closeMenu)
  }, 100)
}

/**
 * 从右键菜单添加设备
 */
const addDeviceFromContextMenu = (template: any) => {
  if (!canvas.value) return
  
  console.log('[FloorPlanEditor] 从右键菜单添加设备:', template.label)
  console.log('  位置(DXF):', contextMenu.value.dxfX.toFixed(2), 'm,', contextMenu.value.dxfY.toFixed(2), 'm')
  
  // 在鼠标位置添加设备
  const newDevice = {
    name: template.label + '_' + Date.now(),
    deviceType: template.type,
    localX: contextMenu.value.dxfX,
    localY: contextMenu.value.dxfY,
    localZ: 0,
    deviceIcon: template.icon,
    color: template.color,
    iconSize: 20
  }
  
  addDeviceToCanvas(newDevice)
  canvas.value.requestRenderAll()
  
  // 关闭菜单
  contextMenu.value.visible = false
  
  ElMessage.success(`已在鼠标位置添加${template.label}`)
}
</script>

<style scoped lang="scss">
.floor-plan-editor-v2 {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;

  .editor-toolbar {
    display: flex;
    align-items: center;
    padding: 12px 16px;
    background: #f5f7fa;
    border-bottom: 1px solid #dcdfe6;
    flex-shrink: 0;
  }

  .editor-main {
    display: flex;
    flex: 1;
    overflow: hidden;

    // 左侧：设备工具箱
    .device-toolbox {
      width: 300px;
      border-right: 1px solid #dcdfe6;
      background: white;
      display: flex;
      flex-direction: column;

      // 标题栏
      .toolbox-title {
        padding: 16px;
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        border-bottom: 1px solid #dcdfe6;
        display: flex;
        align-items: center;
        background: white;
      }

      // Tab 导航（顶部）
      .toolbox-tabs {
        flex: 1;
        display: flex;
        flex-direction: column;
        background: #fafafa;

        // Card类型Tab样式
        :deep(.el-tabs__header) {
          margin: 0;
          background: #f5f7fa;
          padding: 8px 12px 0;
          border-bottom: 1px solid #dcdfe6;
        }

        :deep(.el-tabs__nav) {
          border: none;
        }

        :deep(.el-tabs__item) {
          padding: 0 20px;
          height: 36px;
          line-height: 36px;
          font-size: 14px;
          font-weight: 500;
          color: #606266;
          border: 1px solid transparent;
          border-radius: 4px 4px 0 0;
          margin-right: 4px;
          transition: all 0.2s;
          background: #e4e7ed;
          
          &.is-active {
            color: #409eff;
            background: white;
            border-color: #dcdfe6;
            border-bottom-color: white;
          }

          &:hover:not(.is-active) {
            color: #409eff;
            background: #ecf5ff;
          }
        }

        :deep(.el-tabs__content) {
          flex: 1;
          overflow: hidden;
          height: 0; // 强制flex布局
          background: white;
        }

        :deep(.el-tab-pane) {
          height: 100%;
        }

        .tab-label {
          display: flex;
          align-items: center;
          gap: 6px;
          
          span {
            line-height: 1;
          }
        }

        // Tab内容区
        .tab-content {
          height: 100%;
          display: flex;
          flex-direction: column;
          background: white;
        }

        // Tab底部
        .tab-footer {
          padding: 12px 16px;
          background: #f5f7fa;
          border-top: 1px solid #e4e7ed;
          flex-shrink: 0;
          text-align: center;
        }
      }

      // 🆕 设备面板
      .device-panel {
        display: flex;
        flex-direction: column;
        height: 100%;
        padding: 8px;
        gap: 8px;

        // 操作栏
        .device-actions-bar {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 8px 12px;
          background: white;
          border-radius: 6px;
          border: 1px solid #e4e7ed;
        }

        // 搜索筛选栏
        .device-filter-bar {
          display: flex;
          gap: 8px;
          
          .el-input {
            flex: 1;
          }
        }

        // 快速统计
        .device-quick-stats {
          display: flex;
          justify-content: space-around;
          padding: 12px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border-radius: 6px;

          .stat-item {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 4px;

            .label {
              font-size: 11px;
              color: rgba(255, 255, 255, 0.8);
            }

            .value {
              font-size: 20px;
              font-weight: bold;
              color: white;

              &.success {
                color: #67c23a;
              }

              &.primary {
                color: #ffd93d;
              }
              
              &.warning {
                color: #ff9800;
              }
            }
          }
        }
        
        // 设备操作提示框
        .device-tip-alert {
          margin: 12px 0;
          
          :deep(.el-alert__title) {
            line-height: 1.4;
          }
        }

        // 紧凑型设备列表
        .compact-device-list {
          padding: 4px;

          .compact-device-item {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 8px;
            margin-bottom: 4px;
            background: white;
            border: 1px solid #e4e7ed;
            border-radius: 6px;
            cursor: pointer;
            transition: all 0.2s;

            &:hover {
              border-color: #409eff;
              background: #f5f7fa;
            }

            &.is-selected {
              background: #ecf5ff;
              border-color: #409eff;
            }

            &.is-imported {
              background: #f0f9ff;
              border-color: #b3d8ff;
              cursor: default;

              &:hover {
                background: #f0f9ff;
              }
            }

            .device-icon-small {
              flex-shrink: 0;
            }

            .device-info-compact {
              flex: 1;
              min-width: 0;
              display: flex;
              flex-direction: column;
              gap: 4px;

              .device-name-compact {
                font-size: 13px;
                font-weight: 500;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
              }

              .device-meta {
                display: flex;
                align-items: center;
                gap: 4px;
                flex-wrap: wrap;

                .device-type-tag {
                  font-size: 11px;
                  color: #909399;
                  background: #f4f4f5;
                  padding: 2px 6px;
                  border-radius: 3px;
                }
              }
            }

            .action-btn {
              flex-shrink: 0;
              padding: 4px;
            }
          }
        }

        // 底部提示
        .device-panel-footer {
          padding: 8px 12px;
          background: #f5f7fa;
          border-radius: 6px;
          text-align: center;
        }

        // 加载更多触发器
        .load-more-trigger {
          padding: 16px;
          text-align: center;
          min-height: 50px;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        // 加载完成提示
        .load-complete-tip {
          padding: 12px;
          text-align: center;
          border-top: 1px solid #e4e7ed;
          background: #f9f9f9;
        }
      }

      // 设备模板列表
      .device-template-list {
        padding: 12px;

        .device-template-item {
          display: flex;
          align-items: center;
          gap: 10px;
          padding: 12px;
          margin-bottom: 8px;
          background: #fff;
          border: 1px solid #e4e7ed;
          border-radius: 6px;
          cursor: pointer;
          transition: all 0.2s;

          &:hover {
            border-color: #409eff;
            background: #ecf5ff;
            transform: translateY(-2px);
            box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
          }

          &:active {
            transform: scale(0.98);
          }

          span {
            font-size: 14px;
            color: #303133;
          }
        }
      }
    }

    // 中间：画布区域
    .canvas-container {
      flex: 1;
      position: relative;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f5f5;
      overflow: hidden;

      canvas {
        border: 1px solid #dcdfe6;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
      }

      // 右键菜单
      .context-menu {
        position: absolute;
        min-width: 160px;
        background: #fff;
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
        z-index: 1000;
        overflow: hidden;

        .context-menu-header {
          padding: 8px 12px;
          font-size: 12px;
          font-weight: 600;
          color: #909399;
          background: #f5f7fa;
          border-bottom: 1px solid #dcdfe6;
        }

        .context-menu-item {
          display: flex;
          align-items: center;
          padding: 8px 12px;
          font-size: 14px;
          cursor: pointer;
          transition: all 0.2s;

          &:hover {
            background: #ecf5ff;
            color: #409eff;
          }

          &:active {
            background: #d9ecff;
          }
        }
      }

      .zoom-display {
        position: absolute;
        bottom: 16px;
        right: 16px;
        padding: 6px 12px;
        background: rgba(0, 0, 0, 0.75);
        color: #fff;
        border-radius: 4px;
        font-size: 12px;
        font-weight: 600;
      }

      .coordinate-display {
        position: absolute;
        top: 16px;
        left: 16px;
        padding: 6px 12px;
        background: rgba(0, 0, 0, 0.75);
        color: #fff;
        border-radius: 4px;
        font-size: 12px;
        font-family: 'Courier New', monospace;
      }

      .empty-canvas-tip {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        text-align: center;
        color: #909399;

        p {
          margin: 16px 0;
          font-size: 14px;
        }
      }
    }

    // 右侧：属性面板
    .properties-panel {
      width: 300px;
      border-left: 1px solid #dcdfe6;
      background: #fafafa;
      overflow-y: auto;

      .card-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        font-weight: 600;
      }

      :deep(.el-card__body) {
        padding: 16px;
      }

      :deep(.el-form-item) {
        margin-bottom: 16px;
      }
    }
  }
}
</style>

