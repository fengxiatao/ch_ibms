<template>
  <div class="video-monitor-system">
    <!-- 顶部工具栏 -->
    <div class="monitor-header">
      <div class="header-left">
        <h3>多路视频监控系统</h3>
        <span class="device-count">在线设备: {{ onlineCount }} / {{ totalCount }}</span>
      </div>
      
      <div class="header-center">
        <!-- 分屏布局切换 -->
        <el-radio-group v-model="screenLayout" size="default" @change="handleLayoutChange">
          <el-radio-button :value="1">
            <Icon icon="ep:grid" />
            1分屏
          </el-radio-button>
          <el-radio-button :value="4">
            <Icon icon="ep:grid" />
            4分屏
          </el-radio-button>
          <el-radio-button :value="9">
            <Icon icon="ep:grid" />
            9分屏
          </el-radio-button>
          <el-radio-button :value="16">
            <Icon icon="ep:grid" />
            16分屏
          </el-radio-button>
        </el-radio-group>
      </div>
      
      <div class="header-right">
        <el-button @click="handleClearAll">清空所有</el-button>
        <el-button type="primary" @click="handleSaveLayout">保存布局</el-button>
      </div>
    </div>

    <div class="monitor-content">
      <!-- 左侧面板 -->
      <div class="left-panel" :class="{ collapsed: leftPanelCollapsed }">
        <div class="panel-header">
          <el-tabs v-model="activeTab" class="panel-tabs">
            <el-tab-pane label="空间树" name="tree">
              <!-- 空间树 -->
              <div class="space-tree-container">
                <el-input
                  v-model="treeFilterText"
                  placeholder="搜索设备/位置"
                  :prefix-icon="Search"
                  clearable
                  @input="handleTreeFilter"
                />
                
                <el-tree
                  ref="spaceTreeRef"
                  :data="spaceTreeData"
                  :props="treeProps"
                  :filter-node-method="filterNode"
                  node-key="id"
                  default-expand-all
                  :expand-on-click-node="false"
                  class="space-tree"
                >
                  <template #default="{ node, data }">
                    <div 
                      class="tree-node"
                      :draggable="data.type === 'device'"
                      @dragstart="handleDragStart($event, data)"
                      @dragend="handleDragEnd"
                    >
                      <Icon 
                        :icon="getTreeIcon(data.type)" 
                        :color="getTreeIconColor(data)" 
                      />
                      <span class="node-label">{{ node.label }}</span>
                      <el-tag 
                        v-if="data.type === 'device'" 
                        :type="data.online ? 'success' : 'info'" 
                        size="small"
                      >
                        {{ data.online ? '在线' : '离线' }}
                      </el-tag>
                    </div>
                  </template>
                </el-tree>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="设备列表" name="list">
              <!-- 设备列表（虚拟滚动） -->
              <div class="device-list-container">
                <el-input
                  v-model="listFilterText"
                  placeholder="搜索设备名称"
                  :prefix-icon="Search"
                  clearable
                  @input="handleListFilter"
                />
                
                <div class="filter-bar">
                  <el-select v-model="filterStatus" placeholder="状态" size="small" @change="loadDevices">
                    <el-option label="全部" :value="null" />
                    <el-option label="在线" :value="1" />
                    <el-option label="离线" :value="2" />
                  </el-select>
                  <el-select v-model="filterBuilding" placeholder="建筑" size="small" @change="loadDevices">
                    <el-option label="全部建筑" :value="null" />
                    <el-option 
                      v-for="building in buildings" 
                      :key="building.id" 
                      :label="building.name" 
                      :value="building.id" 
                    />
                  </el-select>
                </div>

                <!-- 虚拟滚动列表 -->
                <el-scrollbar class="device-list-scroll" height="calc(100vh - 280px)">
                  <div 
                    v-for="device in filteredDevices" 
                    :key="device.id"
                    class="device-item"
                    :class="{ 'device-offline': !device.online }"
                    draggable="true"
                    @dragstart="handleDragStart($event, device)"
                    @dragend="handleDragEnd"
                  >
                    <div class="device-icon">
                      <Icon icon="ep:video-camera" :size="24" />
                      <span 
                        class="status-dot" 
                        :class="device.online ? 'status-online' : 'status-offline'"
                      ></span>
                    </div>
                    <div class="device-info">
                      <div class="device-name">{{ device.deviceName }}</div>
                      <div class="device-location">{{ device.location }}</div>
                    </div>
                    <el-button 
                      size="small" 
                      text 
                      @click="handleQuickPlay(device)"
                      :disabled="!device.online"
                    >
                      快速播放
                    </el-button>
                  </div>
                  
                  <!-- 加载更多 -->
                  <div v-if="hasMore" class="load-more">
                    <el-button text @click="loadMore">加载更多...</el-button>
                  </div>
                </el-scrollbar>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
        
        <!-- 折叠按钮 -->
        <div class="panel-collapse-btn" @click="toggleLeftPanel">
          <Icon :icon="leftPanelCollapsed ? 'ep:d-arrow-right' : 'ep:d-arrow-left'" />
        </div>
      </div>

      <!-- 右侧分屏区域 -->
      <div class="right-panel">
        <div 
          class="screen-grid" 
          :class="`layout-${screenLayout}`"
        >
          <div
            v-for="(screen, index) in screens"
            :key="index"
            class="screen-cell"
            :class="{ 
              'cell-active': screen.device, 
              'cell-focused': focusedScreenIndex === index,
              'cell-drop-over': dropTargetIndex === index
            }"
            @click="handleCellClick(index)"
            @drop="handleDrop($event, index)"
            @dragover="handleDragOver($event, index)"
            @dragleave="handleDragLeave(index)"
          >
            <!-- 视频播放器占位 -->
            <div v-if="screen.device && screen.playUrl" class="screen-player"></div>
            
            <!-- 空白状态 -->
            <div v-else class="screen-empty">
              <Icon icon="ep:video-camera" :size="48" color="#909399" />
              <div class="empty-text">拖拽设备到此处播放</div>
              <div class="empty-hint">或双击选择设备</div>
            </div>

            <!-- 格子信息覆盖层 -->
            <div v-if="screen.device" class="screen-overlay">
              <div class="overlay-header">
                <span class="device-name">{{ screen.device.deviceName }}</span>
                <div class="overlay-actions">
                  <el-button-group size="small">
                    <el-tooltip content="截图">
                      <el-button @click.stop="handleScreenshot(index)">
                        <Icon icon="ep:camera" />
                      </el-button>
                    </el-tooltip>
                    <el-tooltip content="录像">
                      <el-button @click.stop="handleRecord(index)">
                        <Icon icon="ep:video-camera" />
                      </el-button>
                    </el-tooltip>
                    <el-tooltip content="全屏">
                      <el-button @click.stop="handleFullscreen(index)">
                        <Icon icon="ep:full-screen" />
                      </el-button>
                    </el-tooltip>
                    <el-tooltip content="静音/取消静音">
                      <el-button @click.stop="toggleMute(index)">
                        <Icon :icon="screen.muted ? 'ep:mute' : 'ep:microphone'" />
                      </el-button>
                    </el-tooltip>
                    <el-tooltip content="清除">
                      <el-button type="danger" @click.stop="handleClearScreen(index)">
                        <Icon icon="ep:close" />
                      </el-button>
                    </el-tooltip>
                  </el-button-group>
                </div>
              </div>
              
              <div class="overlay-footer">
                <span class="device-location">{{ screen.device.location }}</span>
                <el-tag v-if="screen.isLoading" size="small" type="info">
                  <Icon icon="ep:loading" class="loading-icon" />
                  加载中
                </el-tag>
                <el-tag v-else size="small" type="success">正常</el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
// 修复：Icon 组件应从 '@/components/Icon' 名称导出引用，避免找不到 Icon.vue
import { Icon } from '@/components/Icon'
 
import * as DeviceApi from '@/api/iot/device/device'
import * as SecurityOverviewApi from '@/api/iot/security-overview'
// 修复：建筑与楼层 API 正确路径应为 spatial 模块
// 建筑 API：提供 getBuildingList、getBuildingPage 等空间管理接口
import * as BuildingApi from '@/api/iot/spatial/building'
// 楼层 API：提供 getFloorListByBuildingId 等空间管理接口
import * as FloorApi from '@/api/iot/spatial/floor'

// ========== 数据定义 ==========

/** 分屏布局 */
const screenLayout = ref<1 | 4 | 9 | 16>(4)

/** 屏幕列表 */
interface Screen {
  device: any | null
  playUrl: string | null
  muted: boolean
  isLoading: boolean
  error: string | null
}

const screens = ref<Screen[]>([])

/** 当前聚焦的屏幕索引 */
const focusedScreenIndex = ref<number | null>(null)

/** 拖拽目标索引 */
const dropTargetIndex = ref<number | null>(null)

/** 左侧面板折叠状态 */
const leftPanelCollapsed = ref(false)

/** 活动标签页 */
const activeTab = ref('tree')

// ========== 空间树相关 ==========

const spaceTreeRef = ref()
const treeFilterText = ref('')
const spaceTreeData = ref<any[]>([])

const treeProps = {
  children: 'children',
  label: 'name'
}

/** 加载空间树 */
const loadSpaceTree = async () => {
  try {
    console.log('[视频监控] 加载空间树...')
    
    // 1. 加载建筑列表
    const buildingsRes = await BuildingApi.getBuildingList()
    const buildingsList = buildingsRes || []
    
    console.log('[视频监控] 建筑列表:', buildingsList)
    
    // 2. 为每个建筑加载楼层和设备
    const treeData = await Promise.all(
      buildingsList.map(async (building: any) => {
        // 加载楼层
        // 修复：使用空间管理楼层接口按建筑 ID 获取楼层列表
        // 参数说明：building.id 为建筑主键 ID
        // 返回：楼层数组，包含 id、name 等基本信息
        // 异常：接口请求失败将抛出错误，在外层 try/catch 捕获
        const floorsRes = await FloorApi.getFloorListByBuildingId(building.id)
        const floorsList = floorsRes || []
        
        // 为每个楼层加载设备
        const floors = await Promise.all(
          floorsList.map(async (floor: any) => {
            // 加载该楼层的设备
            const devicesRes = await DeviceApi.getDevicePage({
              pageNo: 1,
              pageSize: 100,
              floorId: floor.id,
              state: null // 全部设备
            })
            
            const devices = (devicesRes.list || [])
              .filter((device: any) => {
                // 只显示摄像头设备
                return device.productName?.includes('摄像') || 
                       device.deviceName?.includes('摄像头') ||
                       device.productId === 61 || 
                       device.productId === 64
              })
              .map((device: any) => ({
                id: `device-${device.id}`,
                name: device.deviceName || device.nickname,
                type: 'device',
                device: device,
                online: device.state === 1
              }))
            
            return {
              id: `floor-${floor.id}`,
              name: floor.name,
              type: 'floor',
              floorId: floor.id,
              children: devices
            }
          })
        )
        
        return {
          id: `building-${building.id}`,
          name: building.name,
          type: 'building',
          buildingId: building.id,
          children: floors
        }
      })
    )
    
    spaceTreeData.value = treeData
    console.log('[视频监控] 空间树加载完成:', treeData)
    
  } catch (error: any) {
    console.error('[视频监控] 加载空间树失败:', error)
    ElMessage.error('加载空间树失败: ' + error.message)
  }
}

/** 树节点过滤 */
const filterNode = (value: string, data: any) => {
  if (!value) return true
  return data.name.toLowerCase().includes(value.toLowerCase())
}

const handleTreeFilter = () => {
  spaceTreeRef.value?.filter(treeFilterText.value)
}

/** 获取树节点图标 */
const getTreeIcon = (type: string) => {
  switch (type) {
    case 'building':
      return 'ep:office-building'
    case 'floor':
      return 'ep:files'
    case 'device':
      return 'ep:video-camera'
    default:
      return 'ep:folder'
  }
}

/** 获取树节点图标颜色 */
const getTreeIconColor = (data: any) => {
  if (data.type === 'device') {
    return data.online ? '#67c23a' : '#909399'
  }
  return '#409eff'
}

// ========== 设备列表相关 ==========

const listFilterText = ref('')
const filterStatus = ref<number | null>(null)
const filterBuilding = ref<number | null>(null)
const buildings = ref<any[]>([])
const devices = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(50)
const hasMore = ref(true)

/** 统计信息 */
const onlineCount = computed(() => 
  devices.value.filter(d => d.online).length
)

const totalCount = computed(() => devices.value.length)

/** 过滤后的设备列表 */
const filteredDevices = computed(() => {
  let result = devices.value
  
  // 按名称过滤
  if (listFilterText.value) {
    const keyword = listFilterText.value.toLowerCase()
    result = result.filter(d => 
      d.deviceName?.toLowerCase().includes(keyword) ||
      d.location?.toLowerCase().includes(keyword)
    )
  }
  
  // 按状态过滤
  if (filterStatus.value !== null) {
    result = result.filter(d => d.state === filterStatus.value)
  }
  
  // 按建筑过滤
  if (filterBuilding.value) {
    result = result.filter(d => d.buildingId === filterBuilding.value)
  }
  
  return result
})

/** 加载设备列表 */
const loadDevices = async (isLoadMore = false) => {
  try {
    if (!isLoadMore) {
      currentPage.value = 1
      devices.value = []
    }
    
    console.log('[视频监控] 加载设备列表，页码:', currentPage.value)
    
    const res = await DeviceApi.getDevicePage({
      pageNo: currentPage.value,
      pageSize: pageSize.value,
      state: filterStatus.value,
      buildingId: filterBuilding.value
    })
    
    const newDevices = (res.list || [])
      .filter((device: any) => {
        // 只显示摄像头设备
        return device.productName?.includes('摄像') || 
               device.deviceName?.includes('摄像头') ||
               device.productId === 61 || 
               device.productId === 64
      })
      .map((device: any) => ({
        ...device,
        online: device.state === 1,
        location: `${device.buildingName || ''} / ${device.floorName || '未设置'}`
      }))
    
    if (isLoadMore) {
      devices.value.push(...newDevices)
    } else {
      devices.value = newDevices
    }
    
    hasMore.value = res.list.length === pageSize.value
    
    console.log('[视频监控] 设备列表加载完成，当前共:', devices.value.length, '个设备')
    
  } catch (error: any) {
    console.error('[视频监控] 加载设备列表失败:', error)
    ElMessage.error('加载设备列表失败: ' + error.message)
  }
}

/** 加载更多 */
const loadMore = () => {
  currentPage.value++
  loadDevices(true)
}

const handleListFilter = () => {
  loadDevices()
}

/** 加载建筑列表（用于筛选） */
const loadBuildings = async () => {
  try {
    buildings.value = await BuildingApi.getBuildingList()
  } catch (error) {
    console.error('[视频监控] 加载建筑列表失败:', error)
  }
}

// ========== 拖拽相关 ==========

let draggedDevice: any = null

const handleDragStart = (event: DragEvent, data: any) => {
  console.log('[拖拽] 开始拖拽:', data)
  
  // 如果是树节点，提取device
  draggedDevice = data.type === 'device' ? data.device : data
  
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'copy'
    event.dataTransfer.setData('text/plain', draggedDevice.id)
  }
}

const handleDragEnd = () => {
  console.log('[拖拽] 结束拖拽')
  draggedDevice = null
  dropTargetIndex.value = null
}

const handleDragOver = (event: DragEvent, index: number) => {
  event.preventDefault()
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'copy'
  }
  dropTargetIndex.value = index
}

const handleDragLeave = (index: number) => {
  if (dropTargetIndex.value === index) {
    dropTargetIndex.value = null
  }
}

const handleDrop = async (event: DragEvent, index: number) => {
  event.preventDefault()
  dropTargetIndex.value = null
  
  if (!draggedDevice) {
    ElMessage.warning('无效的设备')
    return
  }
  
  if (!draggedDevice.online) {
    ElMessage.warning('设备离线，无法播放')
    return
  }
  
  console.log('[拖拽] 放置设备到格子:', index, draggedDevice)
  
  // 播放设备
  await playDeviceInScreen(draggedDevice, index)
}

// ========== 分屏控制 ==========

/** 初始化分屏 */
const initScreens = () => {
  const count = screenLayout.value
  screens.value = Array.from({ length: count }, () => ({
    device: null,
    playUrl: null,
    muted: true,
    isLoading: false,
    error: null
  }))
  
  console.log('[分屏] 初始化', count, '个分屏')
}

/** 切换分屏布局 */
const handleLayoutChange = () => {
  ElMessageBox.confirm(
    '切换布局将清空当前所有播放，是否继续？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    initScreens()
  }).catch(() => {
    // 取消操作，恢复原布局
  })
}

/** 在指定格子中播放设备 */
const playDeviceInScreen = async (device: any, screenIndex: number) => {
  const screen = screens.value[screenIndex]
  
  // 清除之前的播放
  screen.device = null
  screen.playUrl = null
  screen.isLoading = true
  screen.error = null
  
  try {
    console.log('[播放] 开始播放设备:', device.id, '在格子:', screenIndex)
    
    // 获取播放地址
    const playUrlData = await SecurityOverviewApi.getPlayUrl(device.id)
    
    // 优先使用 HTTP-FLV
    const playUrl = playUrlData.flvUrl || playUrlData.hlsUrl || playUrlData.wsFmp4Url
    
    if (!playUrl) {
      throw new Error('无可用的播放地址')
    }
    
    console.log('[播放] 获取到播放地址:', playUrl)
    
    // 更新screen状态
    screen.device = device
    screen.playUrl = playUrl
    screen.isLoading = false
    
    ElMessage.success(`开始播放: ${device.deviceName}`)
    
  } catch (error: any) {
    console.error('[播放] 播放失败:', error)
    screen.isLoading = false
    screen.error = error.message
    ElMessage.error('播放失败: ' + error.message)
  }
}

/** 快速播放（自动选择空闲格子） */
const handleQuickPlay = async (device: any) => {
  // 找到第一个空闲的格子
  const emptyIndex = screens.value.findIndex(s => !s.device)
  
  if (emptyIndex === -1) {
    ElMessage.warning('所有格子已占用，请先清空或切换到更大的分屏')
    return
  }
  
  await playDeviceInScreen(device, emptyIndex)
}

/** 格子点击 */
const handleCellClick = (index: number) => {
  focusedScreenIndex.value = index
}

/** 清除指定格子 */
const handleClearScreen = (index: number) => {
  const screen = screens.value[index]
  screen.device = null
  screen.playUrl = null
  screen.isLoading = false
  screen.error = null
  
  console.log('[分屏] 清除格子:', index)
}

/** 清空所有格子 */
const handleClearAll = () => {
  ElMessageBox.confirm(
    '确定要清空所有播放吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    initScreens()
    ElMessage.success('已清空所有播放')
  }).catch(() => {
    // 取消
  })
}

// ========== 格子操作 ==========

/** 截图 */
const handleScreenshot = async (index: number) => {
  const screen = screens.value[index]
  if (!screen.device) return
  
  try {
    console.log('[截图] 设备:', screen.device.id)
    const snapshotBase64 = await SecurityOverviewApi.getDeviceSnapshot(screen.device.id)
    
    if (snapshotBase64) {
      // 下载截图
      const link = document.createElement('a')
      link.href = `data:image/jpeg;base64,${snapshotBase64}`
      link.download = `${screen.device.deviceName}_${Date.now()}.jpg`
      link.click()
      
      ElMessage.success('截图成功')
    }
  } catch (error: any) {
    console.error('[截图] 失败:', error)
    ElMessage.error('截图失败: ' + error.message)
  }
}

/** 录像 */
const handleRecord = (index: number) => {
  ElMessage.info('录像功能开发中...')
}

/** 全屏 */
const handleFullscreen = (index: number) => {
  const cellElement = document.querySelectorAll('.screen-cell')[index]
  if (cellElement) {
    cellElement.requestFullscreen()
  }
}

/** 切换静音 */
const toggleMute = (index: number) => {
  const screen = screens.value[index]
  screen.muted = !screen.muted
}

/** 播放器错误 */
const handlePlayerError = (index: number, error: Error) => {
  console.error('[播放器] 错误:', index, error)
  screens.value[index].error = error.message
  ElMessage.error(`播放器错误: ${error.message}`)
}

// ========== 布局保存 ==========

/** 保存布局 */
const handleSaveLayout = () => {
  const layout = {
    screenLayout: screenLayout.value,
    screens: screens.value.map(s => ({
      deviceId: s.device?.id,
      deviceName: s.device?.deviceName
    }))
  }
  
  localStorage.setItem('video-monitor-layout', JSON.stringify(layout))
  ElMessage.success('布局已保存')
}

/** 加载布局 */
const loadSavedLayout = () => {
  try {
    const saved = localStorage.getItem('video-monitor-layout')
    if (saved) {
      const layout = JSON.parse(saved)
      // TODO: 恢复布局
      console.log('[布局] 恢复保存的布局:', layout)
    }
  } catch (error) {
    console.error('[布局] 加载失败:', error)
  }
}

// ========== 左侧面板控制 ==========

const toggleLeftPanel = () => {
  leftPanelCollapsed.value = !leftPanelCollapsed.value
}

// ========== 生命周期 ==========

onMounted(async () => {
  console.log('[视频监控] 页面初始化')
  
  // 初始化分屏
  initScreens()
  
  // 加载数据
  await loadBuildings()
  await loadSpaceTree()
  await loadDevices()
  
  // 加载保存的布局
  loadSavedLayout()
})

// ========== 监听 ==========

watch(screenLayout, () => {
  console.log('[分屏] 布局切换:', screenLayout.value)
})
</script>

<style lang="scss" scoped>
.video-monitor-system {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #1a1a1a;
  color: #fff;
}

// ============= 顶部工具栏 =============
.monitor-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #2c2c2c;
  border-bottom: 1px solid #404040;

  .header-left {
    display: flex;
    align-items: center;
    gap: 20px;

    h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
    }

    .device-count {
      font-size: 14px;
      color: #909399;
    }
  }

  .header-center {
    flex: 1;
    display: flex;
    justify-content: center;
  }

  .header-right {
    display: flex;
    gap: 10px;
  }
}

// ============= 主内容区域 =============
.monitor-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

// ============= 左侧面板 =============
.left-panel {
  width: 320px;
  background: #252525;
  border-right: 1px solid #404040;
  display: flex;
  flex-direction: column;
  position: relative;
  transition: width 0.3s;

  &.collapsed {
    width: 0;
    min-width: 0;
  }

  .panel-header {
    flex: 1;
    overflow: hidden;
  }

  .panel-tabs {
    height: 100%;
    
    :deep(.el-tabs__content) {
      height: calc(100% - 55px);
      overflow: hidden;
    }
  }

  .panel-collapse-btn {
    position: absolute;
    right: -20px;
    top: 50%;
    transform: translateY(-50%);
    width: 20px;
    height: 60px;
    background: #2c2c2c;
    border: 1px solid #404040;
    border-left: none;
    border-radius: 0 8px 8px 0;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 10;

    &:hover {
      background: #404040;
    }
  }
}

// ============= 空间树 =============
.space-tree-container {
  padding: 15px;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;

  .space-tree {
    flex: 1;
    overflow: auto;
    background: transparent;

    :deep(.el-tree-node__content) {
      height: 36px;
      color: #dcdcdc;

      &:hover {
        background: #404040;
      }
    }

    .tree-node {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;

      &[draggable="true"] {
        cursor: move;
      }

      .node-label {
        flex: 1;
      }
    }
  }
}

// ============= 设备列表 =============
.device-list-container {
  padding: 15px;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;

  .filter-bar {
    display: flex;
    gap: 10px;

    .el-select {
      flex: 1;
    }
  }

  .device-list-scroll {
    flex: 1;
  }

  .device-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    margin-bottom: 8px;
    background: #2c2c2c;
    border-radius: 8px;
    cursor: move;
    transition: all 0.3s;

    &:hover {
      background: #404040;
      transform: translateX(5px);
    }

    &.device-offline {
      opacity: 0.6;
    }

    .device-icon {
      position: relative;

      .status-dot {
        position: absolute;
        bottom: 0;
        right: 0;
        width: 8px;
        height: 8px;
        border-radius: 50%;
        border: 2px solid #2c2c2c;
      }

      .status-online {
        background: #67c23a;
      }

      .status-offline {
        background: #909399;
      }
    }

    .device-info {
      flex: 1;
      min-width: 0;

      .device-name {
        font-size: 14px;
        font-weight: 500;
        color: #dcdcdc;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .device-location {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }

  .load-more {
    text-align: center;
    padding: 10px;
  }
}

// ============= 右侧分屏 =============
.right-panel {
  flex: 1;
  padding: 20px;
  overflow: auto;
}

.screen-grid {
  width: 100%;
  height: 100%;
  display: grid;
  gap: 10px;

  &.layout-1 {
    grid-template-columns: 1fr;
    grid-template-rows: 1fr;
  }

  &.layout-4 {
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: repeat(2, 1fr);
  }

  &.layout-9 {
    grid-template-columns: repeat(3, 1fr);
    grid-template-rows: repeat(3, 1fr);
  }

  &.layout-16 {
    grid-template-columns: repeat(4, 1fr);
    grid-template-rows: repeat(4, 1fr);
  }
}

// ============= 分屏格子 =============
.screen-cell {
  position: relative;
  background: #000;
  border: 2px solid #404040;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;

  &.cell-focused {
    border-color: #409eff;
    box-shadow: 0 0 15px rgba(64, 158, 255, 0.5);
  }

  &.cell-drop-over {
    border-color: #67c23a;
    border-style: dashed;
    background: rgba(103, 194, 58, 0.1);
  }

  &.cell-active {
    border-color: #67c23a;
  }

  .screen-player {
    width: 100%;
    height: 100%;
  }

  .screen-empty {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 15px;

    .empty-text {
      font-size: 16px;
      color: #909399;
    }

    .empty-hint {
      font-size: 12px;
      color: #606266;
    }
  }

  // 覆盖层（鼠标悬停显示）
  .screen-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    opacity: 0;
    transition: opacity 0.3s;
    pointer-events: none;

    .overlay-header,
    .overlay-footer {
      position: absolute;
      left: 0;
      right: 0;
      padding: 10px;
      background: linear-gradient(to bottom, rgba(0, 0, 0, 0.7), transparent);
      pointer-events: auto;
    }

    .overlay-header {
      top: 0;
      display: flex;
      justify-content: space-between;
      align-items: center;

      .device-name {
        font-size: 14px;
        font-weight: 500;
        color: #fff;
      }
    }

    .overlay-footer {
      bottom: 0;
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);

      .device-location {
        font-size: 12px;
        color: #dcdcdc;
      }
    }
  }

  &:hover .screen-overlay {
    opacity: 1;
  }
}

// ============= 动画 =============
.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>







