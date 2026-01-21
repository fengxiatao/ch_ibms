<template>
  <div class="camera-selector-compact">
    <!-- Tab 切换 - 三种查找方式 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="compact-tabs" size="small">
      <!-- 按空间查找 -->
      <el-tab-pane name="space">
        <template #label>
          <span class="tab-label">
            <Icon icon="ep:office-building" />
            <span>按空间</span>
          </span>
        </template>
        <div class="space-section">
          <el-input
            v-model="spaceFilter"
            placeholder="搜索空间"
            clearable
            prefix-icon="Search"
            size="small"
            class="search-input"
          />
          <div class="space-tree-container" v-loading="spaceLoading">
            <el-tree
              ref="spaceTreeRef"
              :data="spaceTree"
              :props="{ label: 'name', children: 'children' }"
              :filter-node-method="filterSpaceNode"
              node-key="id"
              highlight-current
              @node-click="handleSpaceClick"
              class="space-tree"
              default-expand-all
            >
              <template #default="{ node, data }">
                <span class="tree-node">
                  <Icon :icon="getSpaceIcon(data)" />
                  <span class="node-label">{{ node.label }}</span>
                </span>
              </template>
            </el-tree>
            <el-empty v-if="!spaceLoading && spaceTree.length === 0" description="暂无空间数据" :image-size="50" />
          </div>
        </div>
      </el-tab-pane>
      
      <!-- 按NVR查找 -->
      <el-tab-pane name="nvr">
        <template #label>
          <span class="tab-label">
            <Icon icon="ep:video-camera" />
            <span>按NVR</span>
          </span>
        </template>
        <div class="nvr-section">
          <el-input
            v-model="nvrSearchText"
            placeholder="搜索 NVR 名称或编码"
            clearable
            prefix-icon="Search"
            size="small"
            class="search-input"
            @input="handleNvrSearch"
          />
          <div class="nvr-list-container" v-loading="nvrLoading">
            <div class="nvr-list">
              <div
                v-for="nvr in nvrList"
                :key="nvr.id"
                class="nvr-item"
                :class="{ 'is-active': selectedNvr?.id === nvr.id }"
                @click="handleNvrClick(nvr)"
              >
                <Icon icon="ep:video-camera" class="item-icon" />
                <div class="item-info">
                  <div class="item-name">{{ nvr.name }}</div>
                  <div class="item-code">{{ nvr.code }}</div>
                </div>
                <el-tag v-if="nvr.status === 1" type="success" size="small">在线</el-tag>
                <el-tag v-else type="danger" size="small">离线</el-tag>
              </div>
            </div>
            <el-empty v-if="!nvrLoading && nvrList.length === 0" description="暂无NVR数据" :image-size="50" />
          </div>
          <!-- NVR 分页 -->
          <div class="pagination-mini" v-if="nvrTotal > 0">
            <el-pagination
              v-model:current-page="nvrPagination.page"
              v-model:page-size="nvrPagination.size"
              :total="nvrTotal"
              :page-sizes="[5, 10, 20]"
              layout="total, prev, pager, next, sizes"
              small
              @current-change="loadNvrList"
              @size-change="loadNvrList"
            />
          </div>
        </div>
      </el-tab-pane>
      
      <!-- 按名称查找 -->
      <el-tab-pane name="camera">
        <template #label>
          <span class="tab-label">
            <Icon icon="ep:search" />
            <span>按名称</span>
          </span>
        </template>
        <div class="camera-search-section">
          <el-input
            v-model="cameraSearchText"
            placeholder="输入摄像头名称或编码"
            clearable
            prefix-icon="Search"
            size="small"
            class="search-input"
            @input="handleCameraSearch"
          />
          <div class="camera-search-list" v-loading="cameraSearchLoading">
            <div class="camera-list">
              <div
                v-for="camera in cameraSearchList"
                :key="camera.id"
                class="camera-item"
                @click="handleSelect(camera)"
              >
                <Icon icon="ep:video-camera-filled" class="item-icon" />
                <div class="item-info">
                  <div class="item-name">{{ camera.name }}</div>
                  <div class="item-code">{{ camera.code }}</div>
                </div>
                <el-tag v-if="camera.status === 1" type="success" size="small">在线</el-tag>
                <el-tag v-else type="danger" size="small">离线</el-tag>
              </div>
            </div>
            <el-empty v-if="!cameraSearchLoading && cameraSearchList.length === 0" description="请输入关键词搜索" :image-size="50" />
          </div>
          <!-- 摄像头搜索分页 -->
          <div class="pagination-mini" v-if="cameraSearchTotal > 0">
            <el-pagination
              v-model:current-page="cameraSearchPagination.page"
              v-model:page-size="cameraSearchPagination.size"
              :total="cameraSearchTotal"
              :page-sizes="[5, 10, 20]"
              layout="total, prev, pager, next, sizes"
              small
              @current-change="searchCameras"
              @size-change="searchCameras"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 摄像头列表 -->
    <div class="camera-list-section">
      <div class="section-header">
        <span class="section-title">摄像头列表</span>
        <el-button size="small" text @click="handleRefresh">
          <Icon icon="ep:refresh" /> 刷新
        </el-button>
      </div>
      
      <div class="camera-list" v-loading="loading">
        <div
          v-for="camera in cameraList"
          :key="camera.id"
          class="camera-item"
          @click="handleSelect(camera)"
        >
          <Icon icon="ep:video-camera-filled" class="camera-icon" />
          <div class="camera-info">
            <div class="camera-name">{{ camera.name }}</div>
            <div class="camera-code">{{ camera.code }}</div>
          </div>
          <el-tag v-if="camera.status === 1" type="success" size="small">在线</el-tag>
          <el-tag v-else type="danger" size="small">离线</el-tag>
        </div>
        
        <el-empty v-if="!loading && cameraList.length === 0" description="暂无视频通道" :image-size="60" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getNvrPage, getNvrChannels } from '@/api/iot/video/nvr'
import { getChannelPage } from '@/api/iot/channel'
import { iotWebSocket } from '@/utils/iotWebSocket'

// TODO: 这些API需要后端提供
const getSpaceTree = async () => {
  // 临时模拟数据，实际应调用 @/api/system/space
  return {
    data: [
      {
        id: '1',
        name: 'A栋智能制造中心',
        children: [
          { id: '1-1', name: 'A栋1层' },
          { id: '1-2', name: 'A栋2层' },
          { id: '1-3', name: 'A栋3层' },
          { id: '1-4', name: 'A栋4层' },
          { id: '1-5', name: 'A栋5层' },
          { id: '1-6', name: 'A栋6层' }
        ]
      },
      {
        id: '2',
        name: 'B栋研发大楼',
        children: [
          { id: '2-1', name: 'B栋1层' },
          { id: '2-2', name: 'B栋2层' }
        ]
      },
      {
        id: '3',
        name: 'C栋数据中心',
        children: [
          { id: '3-1', name: 'C栋1层' }
        ]
      },
      {
        id: '4',
        name: 'D栋办公大楼',
        children: []
      },
      {
        id: '5',
        name: 'E栋实验大楼',
        children: []
      }
    ]
  }
}

// 已移除本地 getNvrPage 函数，改用 API 导入的版本

interface Camera {
  id: number
  name: string
  code: string
  location?: string
  status: number
  streamUrl: string
}

interface SpaceNode {
  id: string | number
  name: string
  type?: string
  children?: SpaceNode[]
}

interface NVR {
  id: string | number
  name: string
  code: string
  status: number
  ip?: string  // ✅ NVR IP 地址
  httpPort?: number  // ✅ NVR HTTP 端口
}

const emit = defineEmits<{
  (e: 'select', camera: Camera): void
}>()

// Tab 切换
const activeTab = ref('space')

// ========== 空间相关 ==========
const spaceFilter = ref('')
const spaceTreeRef = ref()
const spaceTree = ref<SpaceNode[]>([])
const spaceLoading = ref(false)

// ========== NVR 相关 ==========
const nvrSearchText = ref('')
const selectedNvr = ref<NVR | null>(null)
const nvrList = ref<NVR[]>([])
const nvrLoading = ref(false)
const nvrTotal = ref(0)
const nvrPagination = ref({
  page: 1,
  size: 10
})

// ========== 摄像头搜索相关 ==========
const cameraSearchText = ref('')
const cameraSearchList = ref<Camera[]>([])
const cameraSearchLoading = ref(false)
const cameraSearchTotal = ref(0)
const cameraSearchPagination = ref({
  page: 1,
  size: 10
})

// ========== 摄像头列表（底部显示） ==========
const loading = ref(false)
const cameraList = ref<Camera[]>([])

// ========== API 调用方法 ==========

// 加载空间树
const loadSpaceTree = async () => {
  spaceLoading.value = true
  try {
    const res = await getSpaceTree()
    spaceTree.value = res.data || []
  } catch (error) {
    console.error('加载空间树失败:', error)
    ElMessage.error('加载空间树失败')
  } finally {
    spaceLoading.value = false
  }
}

// 加载 NVR 列表（使用分页API）
const loadNvrList = async () => {
  nvrLoading.value = true
  try {
    const res = await getNvrPage({
      pageNo: nvrPagination.value.page,
      pageSize: nvrPagination.value.size,
      name: nvrSearchText.value || undefined
    })
    // 映射字段：state -> status
    // request.get 会自动解包 data，所以 res 就是 { total, list }
    nvrList.value = (res.list || []).map(nvr => ({
      id: nvr.id,
      name: nvr.name,
      code: nvr.ipAddress || `NVR-${nvr.id}`,
      status: nvr.state || 0,  // ✅ state: 0=离线, 1=在线
      ipAddress: nvr.ipAddress,  // ✅ 保存 NVR IP
      httpPort: 80  // ✅ 默认 HTTP 端口
    }))
    nvrTotal.value = res.total || 0
  } catch (error) {
    console.error('加载NVR列表失败:', error)
    ElMessage.error('加载NVR列表失败')
  } finally {
    nvrLoading.value = false
  }
}

// 搜索摄像头（视频通道）
const searchCameras = async () => {
  if (!cameraSearchText.value || cameraSearchText.value.trim().length < 2) {
    cameraSearchList.value = []
    cameraSearchTotal.value = 0
    return
  }
  
  cameraSearchLoading.value = true
  try {
    const res = await getChannelPage({
      pageNo: cameraSearchPagination.value.page,
      pageSize: cameraSearchPagination.value.size,
      channelName: cameraSearchText.value || undefined,
      channelType: 'VIDEO' // 只查询视频通道
    })
    cameraSearchList.value = (res.data.list || []).map(ch => ({
      id: ch.id,
      name: ch.channelName || `通道${ch.channelNo}`,
      code: ch.channelNo,
      status: ch.onlineStatus || 0,
      streamUrl: ch.streamUrlMain || ''
    }))
    cameraSearchTotal.value = res.data.total || 0
  } catch (error) {
    console.error('搜索视频通道失败:', error)
    ElMessage.error('搜索视频通道失败')
  } finally {
    cameraSearchLoading.value = false
  }
}

// 根据空间加载摄像头（视频通道）
const loadCamerasBySpace = async (spaceId: string | number) => {
  loading.value = true
  try {
    // TODO: 后端需要支持根据空间ID查询通道
    const res = await getChannelPage({
      pageNo: 1,
      pageSize: 100,
      channelType: 'VIDEO'
    })
    cameraList.value = (res.data.list || []).map(ch => ({
      id: ch.id,
      name: ch.channelName || `通道${ch.channelNo}`,
      code: ch.channelNo,
      status: ch.onlineStatus || 0,
      streamUrl: ch.streamUrlMain || ''
    }))
  } catch (error) {
    console.error('加载视频通道失败:', error)
    ElMessage.error('加载视频通道失败')
  } finally {
    loading.value = false
  }
}

// 根据 NVR 加载视频通道
const loadCamerasByNvr = async (nvrId: string | number) => {
  loading.value = true
  try {
    // 调用 NVR 通道接口
    const res = await getNvrChannels(Number(nvrId))
    // request.get 会自动解包 data，所以 res 就是数组
    
    // 获取当前选中的 NVR 信息（用于提供 nvrIp 和 nvrPort）
    const currentNvr = selectedNvr.value
    
    // ✅ 使用与多屏预览相同的数据结构
    cameraList.value = (res || []).map((ch: any) => ({
      id: ch.id,
      name: ch.name || `通道${ch.channelNo}`,
      code: String(ch.channelNo ?? ''),
      status: ch.state || 0,
      // 完整的通道信息（与多屏预览保持一致）
      deviceId: ch.id,
      channelNo: ch.channelNo,
      ipAddress: ch.ipAddress,
      nvrId: ch.deviceId || nvrId,
      nvrIp: currentNvr?.ipAddress || '',  // NVR IP（从 config 中提取）
      nvrPort: 80,  // NVR HTTP 端口（默认80）
      ptzSupport: ch.ptzSupport || false,
      streamUrl: ch.streamUrl || '',
      subStreamUrl: ch.subStreamUrl || '',
      snapshotUrl: ch.snapshotUrl || '',
      // 设备对象（播放器可能需要）
      device: {
        id: ch.id,
        deviceName: ch.name,
        state: ch.state,
        ipAddress: ch.ipAddress,
        config: ''
      }
    }))
  } catch (error) {
    console.error('加载NVR通道失败:', error)
    ElMessage.error('加载NVR通道失败')
  } finally {
    loading.value = false
  }
}

// ========== 事件处理 ==========

// Tab 切换
const handleTabChange = (tabName: string) => {
  cameraList.value = []
  if (tabName === 'space' && spaceTree.value.length === 0) {
    loadSpaceTree()
  } else if (tabName === 'nvr' && nvrList.value.length === 0) {
    loadNvrList()
  }
}

// 空间树过滤
const filterSpaceNode = (value: string, data: SpaceNode) => {
  if (!value) return true
  return data.name.includes(value)
}

// 获取空间图标
const getSpaceIcon = (data: SpaceNode) => {
  if (data.children && data.children.length > 0) {
    return 'ep:office-building'
  }
  return 'ep:location'
}

watch(spaceFilter, (val) => {
  spaceTreeRef.value?.filter(val)
})

// 空间节点点击
const handleSpaceClick = (data: SpaceNode) => {
  loadCamerasBySpace(data.id)
}

// NVR 搜索（防抖）
let nvrSearchTimer: any = null
const handleNvrSearch = () => {
  clearTimeout(nvrSearchTimer)
  nvrSearchTimer = setTimeout(() => {
    nvrPagination.value.page = 1
    loadNvrList()
  }, 300)
}

// NVR 点击
const handleNvrClick = (nvr: NVR) => {
  selectedNvr.value = nvr
  loadCamerasByNvr(nvr.id)
}

// 摄像头搜索（防抖）
let cameraSearchTimer: any = null
const handleCameraSearch = () => {
  clearTimeout(cameraSearchTimer)
  cameraSearchTimer = setTimeout(() => {
    cameraSearchPagination.value.page = 1
    searchCameras()
  }, 300)
}

// 刷新
const handleRefresh = () => {
  if (activeTab.value === 'space') {
    loadSpaceTree()
  } else if (activeTab.value === 'nvr') {
    loadNvrList()
  } else if (activeTab.value === 'camera') {
    searchCameras()
  }
}

// 选择摄像头
const handleSelect = (camera: Camera) => {
  emit('select', camera)
}

// ========== WebSocket 设备状态监听 ==========

/**
 * 处理设备状态变化（WebSocket 推送）
 */
const handleDeviceStatusChange = (data: any) => {
  console.log('[CameraSelector] WebSocket 设备状态变化:', data)
  
  const { deviceId, deviceName, status } = data
  const isOnline = status === 'online'
  const newStatus = isOnline ? 1 : 0
  
  // 更新 NVR 列表中的状态
  const nvr = nvrList.value.find(n => n.id === deviceId)
  if (nvr) {
    nvr.status = newStatus
    console.log(`[CameraSelector] NVR ${deviceName} 状态更新: ${isOnline ? '在线' : '离线'}`)
  }
  
  // 更新摄像头列表中的状态（如果当前选中的 NVR 是该设备）
  if (selectedNvr.value?.id === deviceId) {
    // 重新加载该 NVR 的通道列表
    loadCamerasByNvr(deviceId)
  }
}

// ========== 生命周期 ==========
onMounted(() => {
  // 默认加载空间树
  loadSpaceTree()
  
  // ✅ 连接 WebSocket，监听设备状态变化
  iotWebSocket.connect()
  iotWebSocket.on('device_status', handleDeviceStatusChange)
})

onUnmounted(() => {
  // ✅ 取消 WebSocket 监听
  iotWebSocket.off('device_status', handleDeviceStatusChange)
})
</script>

<style scoped lang="scss">
.camera-selector-compact {
  display: flex;
  flex-direction: column;
  height: 100%;
  
  .compact-tabs {
    flex: 0 0 auto;  // 不自动增长
    display: flex;
    flex-direction: column;
    max-height: 45%;  // 增加到45%
    
    .tab-label {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
    }
    
    :deep(.el-tabs__header) {
      margin-bottom: 8px;
    }
    
    :deep(.el-tabs__content) {
      flex: 1;
      overflow: hidden;
    }
    
    :deep(.el-tab-pane) {
      height: 100%;
    }
    
    // Tab标签样式优化
    :deep(.el-tabs__item) {
      color: rgba(255, 255, 255, 0.6);  // 未选中时的颜色
      font-size: 13px;
      padding: 0 16px;
      
      &:hover {
        color: rgba(255, 255, 255, 0.9);  // 悬停时更亮
      }
      
      &.is-active {
        color: #409eff;  // 选中时的颜色
      }
    }
  }
  
  // 通用搜索框
  .search-input {
    margin-bottom: 8px;
  }
  
  // 空间部分
  .space-section {
    display: flex;
    flex-direction: column;
    height: 200px;  // 增加高度
    
    .space-tree-container {
      flex: 1;
      overflow-y: auto;
      
      .space-tree {
        .tree-node {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 13px;
          
          .node-label {
            font-size: 13px;
          }
        }
        
        :deep(.el-tree-node__content) {
          height: 28px;
          padding: 0 6px;
        }
      }
    }
  }
  
  // NVR 部分
  .nvr-section {
    display: flex;
    flex-direction: column;
    height: 200px;  // 增加高度
    
    .nvr-list-container {
      flex: 1;
      overflow-y: auto;
      margin-bottom: 8px;
    }
    
    .nvr-list {
      
      .nvr-item {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 8px 12px;
        border-radius: 6px;
        cursor: pointer;
        transition: all 0.2s;
        margin-bottom: 4px;
        background: rgba(255, 255, 255, 0.02);
        
        &:hover {
          background: rgba(64, 158, 255, 0.1);
        }
        
        &.is-active {
          background: rgba(64, 158, 255, 0.2);
          border: 1px solid #409eff;
        }
        
        .item-icon {
          font-size: 18px;
          color: #409eff;
          flex-shrink: 0;
        }
        
        .item-info {
          flex: 1;
          min-width: 0;
          
          .item-name {
            font-size: 13px;
            font-weight: 600;
            color: #fff;
            margin-bottom: 2px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
          
          .item-code {
            font-size: 11px;
            color: rgba(255, 255, 255, 0.6);
          }
        }
      }
    }
    
    .pagination-mini {
      margin-top: 8px;
      
      :deep(.el-pagination) {
        justify-content: center;
        
        .el-pagination__total,
        .el-pager li,
        .btn-prev,
        .btn-next {
          font-size: 12px;
          min-width: 24px;
          height: 24px;
          line-height: 24px;
        }
      }
    }
  }
  
  // 摄像头搜索部分
  .camera-search-section {
    display: flex;
    flex-direction: column;
    height: 200px;  // 增加高度
    
    .camera-search-list {
      flex: 1;
      overflow-y: auto;
      margin-bottom: 8px;
      
      .camera-list {
        .camera-item {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 6px 10px;
          border-radius: 4px;
          cursor: pointer;
          transition: all 0.2s;
          margin-bottom: 3px;
          background: rgba(255, 255, 255, 0.02);
          
          &:hover {
            background: rgba(64, 158, 255, 0.1);
            transform: translateX(2px);
          }
          
          .item-icon {
            font-size: 18px;
            color: #409eff;
            flex-shrink: 0;
          }
          
          .item-info {
            flex: 1;
            min-width: 0;
            
            .item-name {
              font-size: 13px;
              font-weight: 600;
              color: #fff;
              margin-bottom: 2px;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }
            
            .item-code {
              font-size: 11px;
              color: rgba(255, 255, 255, 0.6);
            }
          }
        }
      }
    }
    
    .pagination-mini {
      margin-top: 8px;
      
      :deep(.el-pagination) {
        justify-content: center;
        
        .el-pagination__total,
        .el-pager li,
        .btn-prev,
        .btn-next {
          font-size: 12px;
          min-width: 24px;
          height: 24px;
          line-height: 24px;
        }
      }
    }
  }
  
  // 摄像头列表部分（占2/3空间）
  .camera-list-section {
    flex: 1;  // 自动占据剩余空间
    display: flex;
    flex-direction: column;
    margin-top: 8px;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    padding-top: 8px;
    min-height: 0;  // 允许flex收缩
    
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 6px;
      flex-shrink: 0;
      
      .section-title {
        font-size: 13px;
        font-weight: 600;
        color: #fff;
      }
    }
    
    .camera-list {
      flex: 1;  // 占据剩余空间
      overflow-y: auto;
      min-height: 0;
      
      .camera-item {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 8px 12px;
        border-radius: 6px;
        cursor: pointer;
        transition: all 0.2s;
        margin-bottom: 4px;
        background: rgba(255, 255, 255, 0.02);
        
        &:hover {
          background: rgba(64, 158, 255, 0.1);
          transform: translateX(2px);
        }
        
        .camera-icon {
          font-size: 20px;
          color: #409eff;
        }
        
        .camera-info {
          flex: 1;
          
          .camera-name {
            font-size: 14px;
            font-weight: 600;
            color: #fff;
            margin-bottom: 2px;
          }
          
          .camera-code {
            font-size: 12px;
            color: rgba(255, 255, 255, 0.6);
          }
        }
      }
    }
  }
}

// 滚动条样式
:deep(.el-tree),
:deep(.nvr-list),
:deep(.camera-list) {
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 3px;
    
    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  }
}
</style>
