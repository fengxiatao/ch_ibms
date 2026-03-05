<template>
    <ContentWrap :body-style="{ padding: '0', height: '100%', display: 'flex', flexDirection: 'column' }" style="height: calc(100vh - var(--page-top-gap, 70px)); padding-top: var(--page-top-gap, 70px); margin-bottom: 0">
  <div class="snapshot-record-container">
    <!-- 左侧面板 -->
    <div class="left-panel">
      <!-- 设备树 -->
      <div class="panel-section">
        <div class="section-header">
          <Icon icon="ep:video-camera" />
          <span>视频通道</span>
        </div>
        <div class="search-box">
          <el-input 
            v-model="deviceSearchKeyword" 
            placeholder="搜索通道名称..." 
            clearable
            size="small"
            @keyup.enter="handleChannelSearch"
            @clear="handleSearchClear"
          >
            <template #prefix>
              <Icon icon="ep:search" />
            </template>
            <template #append>
              <el-button :icon="Search" @click="handleChannelSearch" />
            </template>
          </el-input>
        </div>
        <el-tree
          ref="cameraTreeRef"
          :data="cameraTreeData"
          :props="treeProps"
          node-key="id"
          :lazy="true"
          :load="loadTreeNode"
          :accordion="true"
          show-checkbox
          :check-strictly="false"
          :render-after-expand="false"
          @node-click="handleCameraSelect"
          @check="handleChannelCheck"
          class="device-tree"
        >
          <template #default="{ data }">
            <div 
              class="tree-node" 
              :class="'node-type-' + data.type"
            >
              <Icon
                v-if="data.type === 'building'"
                icon="ep:office-building"
                style="color: #409eff"
              />
              <Icon
                v-else-if="data.type === 'floor'"
                icon="ep:tickets"
                style="color: #67c23a"
              />
              <Icon
                v-else-if="data.type === 'area'"
                icon="ep:location"
                style="color: #e6a23c"
              />
              <Icon
                v-else-if="data.type === 'channels'"
                icon="ep:folder"
                style="color: #909399"
              />
              <Icon
                v-else-if="data.type === 'channel'"
                icon="ep:video-camera"
                style="color: #f56c6c"
              />
              <Icon v-else icon="ep:video-camera" style="color: #f56c6c" />
              <span>{{ data.name }}</span>
            </div>
          </template>
        </el-tree>
      </div>

      <!-- 时间筛选 -->
      <div class="panel-section">
        <div class="section-header">
          <Icon icon="ep:calendar" />
          <span>时间</span>
        </div>
        <div class="time-filter">
          <div class="filter-item">
            <label>时间段:</label>
            <el-date-picker
              v-model="filterForm.timeRange"
              type="datetimerange"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              size="small"
              style="width: 100%"
              format="MM/DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </div>
          
          <el-button type="primary" size="small" @click="handleSearch" style="width: 100%">
            搜索
          </el-button>
        </div>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 抓图记录展示区域 -->
      <div class="snapshot-grid-section">
        <div class="section-header">
          <h3>抓图记录</h3>
          <div class="grid-controls">
            <span>共 {{ total }} 条</span>
            <el-button-group>
              <el-button :class="{ active: viewMode === 'grid' }" @click="viewMode = 'grid'">
                <el-icon><Grid /></el-icon>
              </el-button>
              <el-button :class="{ active: viewMode === 'list' }" @click="viewMode = 'list'">
                <el-icon><List /></el-icon>
              </el-button>
            </el-button-group>
          </div>
        </div>

        <!-- 网格视图 -->
        <div v-if="viewMode === 'grid'" class="snapshot-grid">
          <div
            v-for="item in snapshotList"
            :key="item.id"
            class="snapshot-item"
            @click="handlePreview(item)"
          >
            <div class="snapshot-image">
              <img :src="item.snapshotUrl" :alt="item.channelName" @error="handleImageError" />
              <div class="image-overlay">
                <el-icon class="preview-icon"><ZoomIn /></el-icon>
              </div>
            </div>
            <div class="snapshot-info">
              <div class="device-name">{{ item.channelName || '未知通道' }}</div>
              <div class="capture-time">{{ formatCaptureTime(item.captureTime) }}</div>
            </div>
          </div>
        </div>

        <!-- 列表视图 -->
        <div v-else class="snapshot-list">
          <!-- 批量操作工具栏 -->
          <div v-if="selectedSnapshots.length > 0" class="batch-toolbar">
            <span>已选择 {{ selectedSnapshots.length }} 项</span>
            <el-button type="danger" size="small" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="primary" size="small" @click="handleBatchDownload">批量下载</el-button>
            <el-button size="small" @click="handleClearSelection">取消选择</el-button>
          </div>
          
          <el-table 
            :data="snapshotList"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="channelName" label="通道名称" width="200" />
            <el-table-column label="抓拍时间" width="200">
              <template #default="scope">
                {{ formatCaptureTime(scope.row.captureTime) }}
              </template>
            </el-table-column>
            <el-table-column label="缩略图" width="120">
              <template #default="scope">
                <img :src="scope.row.snapshotUrl" class="table-thumbnail" @click="handlePreview(scope.row)" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handlePreview(scope.row)">查看</el-button>
                <el-button type="success" size="small" @click="handleDownload(scope.row)">下载</el-button>
                <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 分页 -->
        <div class="pagination-section">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>

    <!-- 图片预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="图片预览"
      width="80%"
      center
    >
      <div class="preview-content">
        <img :src="previewImage.snapshotUrl" :alt="previewImage.channelName" class="preview-img" />
        <div class="preview-info">
          <p><strong>通道名称：</strong>{{ previewImage.channelName }}</p>
          <p><strong>抓拍时间：</strong>{{ formatCaptureTime(previewImage.captureTime) }}</p>
          <p><strong>图片大小：</strong>{{ previewImage.fileSize }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="primary" @click="handlePreviewDownload">下载</el-button>
      </template>
    </el-dialog>
  </div>
    </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Grid,
  List,
  ZoomIn,
  Search
} from '@element-plus/icons-vue'
import { getBuildingList } from '@/api/iot/spatial/building'
import { getFloorListByBuildingId } from '@/api/iot/spatial/floor'
import { getAreaListByFloorId } from '@/api/iot/spatial/area'
import { getCameraSnapshotPage, deleteCameraSnapshot } from '@/api/iot/video'
import { getChannelPage } from '@/api/iot/channel'
import type { CameraSnapshotRespVO } from '@/api/iot/video'

/**
 * 抓图记录页面组件
 * 提供视频监控抓图记录的查询、展示和管理功能
 */

// 响应式数据定义
const viewMode = ref('grid') // 视图模式：grid 网格视图，list 列表视图
const previewVisible = ref(false) // 预览对话框显示状态
const total = ref(0) // 总记录数

// 筛选表单数据
const filterForm = reactive({
  deviceId: '', // 设备ID
  timeRange: [] as string[] // 时间范围
})

/**
 * 格式化抓拍时间（时间戳转换为 年-月-日 时:分:秒）
 * @param timestamp 时间戳或日期字符串
 * @return 格式化后的时间字符串
 */
const formatCaptureTime = (timestamp: string | number | undefined): string => {
  if (!timestamp) return '-'
  
  let date: Date
  
  // 处理数字类型的时间戳
  if (typeof timestamp === 'number' || /^\d+$/.test(String(timestamp))) {
    const ts = Number(timestamp)
    // 兼容秒级和毫秒级时间戳
    date = new Date(ts > 9999999999 ? ts : ts * 1000)
  } else {
    // 处理日期字符串
    date = new Date(timestamp)
  }
  
  if (isNaN(date.getTime())) return String(timestamp)
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 设备搜索关键字
const deviceSearchKeyword = ref('')

// 分页数据
const pagination = reactive({
  page: 1, // 当前页码
  size: 20 // 每页大小
})

// 预览图片信息
const previewImage = reactive<{
  snapshotUrl: string
  channelName: string
  captureTime: string
  fileSize: string
}>({
  snapshotUrl: '', // 图片URL
  channelName: '', // 设备名称
  captureTime: '', // 抓拍时间
  fileSize: '' // 文件大小
})

// 摄像头树形数据配置
const treeProps = {
  children: 'children',
  label: 'name',
  isLeaf: (data: any) => {
    // 只有通道节点是叶子节点
    return data.type === 'channel'
  }
}

// 摄像头树形数据（从空间管理API加载）
const cameraTreeData = ref<any[]>([])

// 设备列表数据（从摄像头树中提取）
const deviceList = ref<any[]>([])

// 抓图记录列表数据
const snapshotList = ref<CameraSnapshotRespVO[]>([])

// 是否正在加载数据
const loading = ref(false)

// 树组件引用
const cameraTreeRef = ref()

// 选中的通道列表
const selectedChannels = ref<any[]>([])

// 选中的抓图记录列表
const selectedSnapshots = ref<CameraSnapshotRespVO[]>([])

/**
 * 判断设备是否为摄像机（基于设备能力，不硬编码产品ID）
 * @param device 设备对象
 * @returns 是否为摄像机
 */
const isCameraDevice = (device: any): boolean => {
  try {
    // 解析设备配置
    const config = device.config ? JSON.parse(device.config) : {}
    
    // 判断标准：具有以下摄像机特征之一即可
    // 1. 有RTSP端口配置（视频流协议）
    const hasRtspPort = config.rtspPort !== undefined && config.rtspPort !== null
    
    // 2. 支持ONVIF协议（网络视频设备标准协议）
    const hasOnvifSupport = config.onvifSupported === true || config.onvifPort !== undefined
    
    // 3. 有快照接口配置
    const hasSnapshotConfig = config.snapshot !== undefined && config.snapshot !== null
    
    // 4. 有视频厂商标识（hikvision、dahua、uniview等）
    const hasVendor = config.vendor !== undefined && config.vendor !== null
    
    // 满足任一条件即判定为摄像机
    return hasRtspPort || hasOnvifSupport || hasSnapshotConfig || hasVendor
    
  } catch (error) {
    console.warn(`⚠️ 设备配置解析失败 [${device.deviceName}]:`, error)
    return false
  }
}

/**
 * 搜索视频通道（根据通道名称模糊查询）
 */
const handleChannelSearch = async () => {
  const keyword = deviceSearchKeyword.value.trim()
  
  if (!keyword) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  try {
    console.log('[快照记录] 🔍 搜索通道:', keyword)
    
    // 使用通道分页查询接口进行搜索
    const result = await getChannelPage({
      channelName: keyword,
      channelType: 'video',
      pageNo: 1,
      pageSize: 100
    })
    
    if (result.list && result.list.length > 0) {
      // 将搜索结果转换为树节点格式
      cameraTreeData.value = result.list.map((channel: any) => ({
        id: `channel-${channel.id}`,
        name: channel.channelName,
        type: 'channel',
        channelId: channel.id,
        channelNo: channel.channelNo,
        deviceId: channel.deviceId,
        channel: channel
      }))
      
      ElMessage.success(`找到 ${result.list.length} 个匹配的视频通道`)
      console.log(`[快照记录] ✅ 搜索完成，找到 ${result.list.length} 个通道`)
    } else {
      cameraTreeData.value = []
      ElMessage.info('未找到匹配的视频通道')
      console.log('[快照记录] ℹ️ 未找到匹配的通道')
    }
  } catch (error: any) {
    console.error('[快照记录] ❌ 搜索失败:', error)
    ElMessage.error('搜索失败: ' + (error?.message || '未知错误'))
  }
}

/**
 * 清除搜索
 */
const handleSearchClear = () => {
  deviceSearchKeyword.value = ''
  // 重新加载完整树
  loadSpaceTree()
  console.log('[快照记录] 🔄 已清除搜索，重新加载树')
}

/**
 * 加载空间树（初始只加载建筑列表）
 */
const loadSpaceTree = async () => {
  try {
    console.log('[快照记录] 🔄 开始加载空间树（懒加载模式）...')
    
    // 回退方案：只加载建筑列表，不加载子节点
    const buildings = await getBuildingList()
    
    console.log('[快照记录] ✅ 加载到建筑:', buildings.length, '个')
    
    // 将建筑转换为树节点格式
    const treeData = buildings.map((building: any) => ({
      id: `building-${building.id}`,
      name: building.name,
      type: 'building',
      buildingId: building.id,
      // 不预先加载 children，由 loadTreeNode 按需加载
    }))
    
    cameraTreeData.value = treeData
    
    ElMessage.success(`已加载 ${treeData.length} 个建筑，请展开查看楼层`)
    
  } catch (error: any) {
    console.error('[快照记录] ❌ 加载失败:', error)
    ElMessage.error('加载空间树失败: ' + error.message)
    cameraTreeData.value = []
  }
}

/**
 * 懒加载树节点（按需加载子节点）
 * @param node 当前节点
 * @param resolve 回调函数
 */
const loadTreeNode = async (node: any, resolve: Function) => {
  try {
    const data = node.data
    let children: any[] = []

    console.log('[快照记录] 🔄 懒加载节点:', data.type, data.name)

    // 根据节点类型加载不同的子节点
    if (data.type === 'building') {
      // 添加"通道"节点
      children.push({
        id: `channels-building-${data.buildingId}`,
        name: '通道',
        type: 'channels',
        buildingId: data.buildingId
      })
      
      // 建筑节点 -> 加载楼层列表
      const floors = await getFloorListByBuildingId(data.buildingId)
      console.log('[快照记录] ✅ 建筑', data.name, '有', floors.length, '个楼层')
      
      children.push(...floors.map((floor: any) => ({
        id: `floor-${floor.id}`,
        name: floor.name,
        type: 'floor',
        floorId: floor.id,
        buildingId: data.buildingId,
        floor: floor
      })))

    } else if (data.type === 'floor') {
      // 添加"通道"节点
      children.push({
        id: `channels-floor-${data.floorId}`,
        name: '通道',
        type: 'channels',
        floorId: data.floorId,
        buildingId: data.buildingId
      })
      
      // 楼层节点 -> 加载区域列表
      const areas = await getAreaListByFloorId(data.floorId)
      console.log('[快照记录] ✅ 楼层', data.name, '有', areas.length, '个区域')
      
      children.push(...areas.map((area: any) => ({
        id: `area-${area.id}`,
        name: area.name,
        type: 'area',
        areaId: area.id,
        floorId: data.floorId
      })))

    } else if (data.type === 'area') {
      // 添加"通道"节点
      children.push({
        id: `channels-area-${data.areaId}`,
        name: '通道',
        type: 'channels',
        areaId: data.areaId,
        floorId: data.floorId,
        buildingId: data.buildingId
      })
      
    } else if (data.type === 'channels') {
      // 加载通道列表
      const params: any = {
        pageNo: 1,
        pageSize: 100
      }
      
      // 根据层级添加筛选条件
      if (data.buildingId) params.buildingId = data.buildingId
      if (data.floorId) params.floorId = data.floorId
      if (data.areaId) params.areaId = data.areaId
      if (data.spaceId) params.spaceId = data.spaceId
      
      const channelsRes = await getChannelPage(params)
      const channels = channelsRes.list || []
      
      console.log('[快照记录] ✅ 加载到', channels.length, '个通道')
      
      children = channels.map((ch: any) => ({
        id: `channel-${ch.id}`,
        name: ch.channelName || `通道${ch.channelNo}`,
        type: 'channel',
        channelId: ch.id,
        channel: ch
      }))
    }

    resolve(children)

  } catch (error: any) {
    console.error('[快照记录] ❌ 懒加载节点失败:', error)
    ElMessage.error('加载子节点失败: ' + error.message)
    resolve([]) // 失败时返回空数组
  }
}

/**
 * 从树数据中提取所有设备到列表（递归）
 * 注意：在懒加载模式下，此函数已不再使用
 * 设备列表会在 loadTreeNode 中按需动态添加
 */

/**
 * 获取节点图标
 */
const getNodeIcon = (type: string) => {
  switch (type) {
    case 'building':
      return 'ep:office-building'  // 🏢 建筑图标
    case 'floor':
      return 'ep:grid'              // 📐 楼层图标
    case 'area':
      return 'ep:location'          // 📍 区域图标
    case 'device':
      return 'ep:video-camera'      // 📹 设备图标
    default:
      return 'ep:folder'            // 📁 默认图标
  }
}

/**
 * 处理摄像头选择事件
 * @param data 选中的节点数据
 */
const handleCameraSelect = (data: any) => {
  console.log('[快照记录] 点击节点:', data)
  
  // 只有通道节点才能选择
  if (data.type === 'channel' && data.channel) {
    // 通道节点点击时，可以显示该通道的信息
    ElMessage.info(`已选择通道: ${data.channel.channelName || data.name}`)
  } else {
    // 点击建筑、楼层、区域节点，显示该节点信息
    const typeText = data.type === 'building' ? '建筑' : 
                     data.type === 'floor' ? '楼层' : 
                     data.type === 'area' ? '区域' :
                     data.type === 'channels' ? '通道文件夹' : '节点'
    ElMessage.info(`选中${typeText}: ${data.name}`)
  }
}

/**
 * 处理通道复选框选中事件
 */
const handleChannelCheck = (data: any, checked: any) => {
  console.log('[快照记录] 复选框变化:', data, checked)
  
  // 获取所有选中的节点
  const checkedNodes = cameraTreeRef.value?.getCheckedNodes() || []
  
  // 只保留通道类型的节点
  selectedChannels.value = checkedNodes.filter((node: any) => node.type === 'channel')
  
  console.log('[快照记录] 当前选中通道:', selectedChannels.value.length, '个')
  
  // 如果有选中的通道，自动更新筛选条件
  if (selectedChannels.value.length > 0) {
    ElMessage.success(`已选择 ${selectedChannels.value.length} 个通道`)
  }
}


/**
 * 处理搜索事件
 */
const handleSearch = () => {
  pagination.page = 1
  loadSnapshotData()
  ElMessage.success('搜索完成')
}

/**
 * 处理重置事件
 */
const handleReset = () => {
  filterForm.deviceId = ''
  filterForm.timeRange = []
  pagination.page = 1
  loadSnapshotData()
  ElMessage.success('重置完成')
}

/**
 * 处理图片预览事件
 * @param item 抓图记录项
 */
const handlePreview = (item: CameraSnapshotRespVO) => {
  previewImage.snapshotUrl = item.snapshotUrl
  previewImage.channelName = item.channelName || '未知通道'
  previewImage.captureTime = item.captureTime
  previewImage.fileSize = item.fileSize ? formatFileSize(item.fileSize) : '未知'
  previewVisible.value = true
}

/**
 * 格式化文件大小
 * @param bytes 字节数
 * @return 格式化后的字符串（如 "65 KB"）
 */
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i)) + ' ' + sizes[i]
}

/**
 * 处理图片加载错误事件
 * @param event 错误事件
 */
const handleImageError = (event: Event) => {
  const target = event.target as HTMLImageElement
  target.src = '/src/assets/images/patrol-capture.svg'
}

/**
 * 处理删除事件
 * @param item 抓图记录项
 */
const handleDelete = async (item: CameraSnapshotRespVO) => {
  try {
    await ElMessageBox.confirm('确定要删除这条抓图记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用删除API
    await deleteCameraSnapshot(item.id)
    ElMessage.success('删除成功')
    
    // 重新加载数据
    await loadSnapshotData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('[快照记录] 删除失败:', error)
      const errorMsg = error?.message || error || '未知错误'
      ElMessage.error('删除失败: ' + errorMsg)
    } else {
      ElMessage.info('已取消删除')
    }
  }
}

/**
 * 下载图片（通过fetch获取blob后下载，解决跨域问题）
 * @param url 图片URL
 * @param fileName 下载文件名
 */
const downloadImage = async (url: string, fileName: string) => {
  try {
    ElMessage.info('正在下载图片...')
    
    // 通过fetch获取图片blob
    // 注意：不使用 credentials: 'include'，因为服务器返回 Access-Control-Allow-Origin: * 时不兼容
    const response = await fetch(url, {
      method: 'GET',
      mode: 'cors'
    })
    
    if (!response.ok) {
      throw new Error(`下载失败: ${response.status} ${response.statusText}`)
    }
    
    const blob = await response.blob()
    
    // 创建blob URL并下载
    const blobUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = blobUrl
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    // 释放blob URL
    window.URL.revokeObjectURL(blobUrl)
    
    ElMessage.success('下载成功')
  } catch (error: any) {
    console.error('[快照记录] 下载失败:', error)
    ElMessage.error('下载失败: ' + (error?.message || '网络错误'))
  }
}

/**
 * 处理下载事件（列表/网格视图）
 */
const handleDownload = (item: CameraSnapshotRespVO) => {
  if (!item || !item.snapshotUrl) {
    ElMessage.error('快照URL无效')
    return
  }
  
  // 生成文件名（替换特殊字符）
  const safeChannelName = (item.channelName || '未知通道').replace(/[\\/:*?"<>|]/g, '_')
  const safeTime = String(item.captureTime || Date.now()).replace(/[\\/:*?"<>|]/g, '-')
  const fileName = `snapshot_${safeChannelName}_${safeTime}.jpg`
  
  // 使用blob方式下载
  downloadImage(item.snapshotUrl, fileName)
}

/**
 * 处理预览对话框下载事件
 */
const handlePreviewDownload = () => {
  if (!previewImage.snapshotUrl) {
    ElMessage.error('快照URL无效')
    return
  }
  
  // 生成文件名
  const safeChannelName = (previewImage.channelName || '未知通道').replace(/[\\/:*?"<>|]/g, '_')
  const safeTime = String(previewImage.captureTime || Date.now()).replace(/[\\/:*?"<>|]/g, '-')
  const fileName = `snapshot_${safeChannelName}_${safeTime}.jpg`
  
  // 使用blob方式下载
  downloadImage(previewImage.snapshotUrl, fileName)
}

/**
 * 处理选择变化
 */
const handleSelectionChange = (selection: CameraSnapshotRespVO[]) => {
  selectedSnapshots.value = selection
}

/**
 * 清除选择
 */
const handleClearSelection = () => {
  selectedSnapshots.value = []
}

/**
 * 批量删除
 */
const handleBatchDelete = async () => {
  if (selectedSnapshots.value.length === 0) {
    ElMessage.warning('请先选择要删除的记录')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedSnapshots.value.length} 条抓图记录吗？`, '批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 批量调用删除API
    const deletePromises = selectedSnapshots.value.map(item => deleteCameraSnapshot(item.id))
    await Promise.all(deletePromises)
    
    ElMessage.success(`成功删除 ${selectedSnapshots.value.length} 条记录`)
    selectedSnapshots.value = []
    
    // 重新加载数据
    await loadSnapshotData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('[快照记录] 批量删除失败:', error)
      ElMessage.error('批量删除失败: ' + (error?.message || '未知错误'))
    }
  }
}

/**
 * 批量下载
 */
const handleBatchDownload = async () => {
  if (selectedSnapshots.value.length === 0) {
    ElMessage.warning('请先选择要下载的记录')
    return
  }
  
  ElMessage.info(`开始下载 ${selectedSnapshots.value.length} 张图片...`)
  
  let successCount = 0
  let failCount = 0
  
  // 逐个下载（使用串行避免并发过多）
  for (const item of selectedSnapshots.value) {
    if (item.snapshotUrl) {
      try {
        const safeChannelName = (item.channelName || '未知通道').replace(/[\\/:*?"<>|]/g, '_')
        const safeTime = String(item.captureTime || Date.now()).replace(/[\\/:*?"<>|]/g, '-')
        const fileName = `snapshot_${safeChannelName}_${safeTime}.jpg`
        
        // 通过fetch获取图片blob
        // 注意：不使用 credentials: 'include'，因为服务器返回 Access-Control-Allow-Origin: * 时不兼容
        const response = await fetch(item.snapshotUrl, {
          method: 'GET',
          mode: 'cors'
        })
        
        if (response.ok) {
          const blob = await response.blob()
          const blobUrl = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = blobUrl
          link.download = fileName
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          window.URL.revokeObjectURL(blobUrl)
          successCount++
        } else {
          failCount++
        }
        
        // 延迟避免浏览器阻止
        await new Promise(resolve => setTimeout(resolve, 300))
      } catch (error) {
        console.error('[快照记录] 下载单个文件失败:', error)
        failCount++
      }
    }
  }
  
  if (failCount === 0) {
    ElMessage.success(`成功下载 ${successCount} 张图片`)
  } else {
    ElMessage.warning(`下载完成：成功 ${successCount} 张，失败 ${failCount} 张`)
  }
}


/**
 * 处理页码变化事件
 * @param page 新页码
 */
const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadSnapshotData()
}

/**
 * 处理每页大小变化事件
 * @param size 新的每页大小
 */
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadSnapshotData()
}

/**
 * 加载抓图数据
 */
const loadSnapshotData = async () => {
  try {
    loading.value = true
    
    console.log('[快照记录] 开始加载快照数据...')
    
    // 构建查询参数
    const params = {
      pageNo: pagination.page,
      pageSize: pagination.size,
      channelIds: selectedChannels.value.length > 0 
        ? selectedChannels.value.map(ch => ch.channelId) 
        : undefined, // 使用所有选中通道的ID数组
      startTime: filterForm.timeRange && filterForm.timeRange[0] ? filterForm.timeRange[0] : undefined,
      endTime: filterForm.timeRange && filterForm.timeRange[1] ? filterForm.timeRange[1] : undefined
    }
    
    console.log('[快照记录] 查询参数:', params)
    
    // 调用API获取数据
    const res = await getCameraSnapshotPage(params)
    
    // 处理响应数据（PageResult 直接包含 list 和 total）
    snapshotList.value = res.list || []
    total.value = res.total || 0
    
    console.log('[快照记录] ✅ 加载完成:', snapshotList.value.length, '条记录，共', total.value, '条')
    
    if (snapshotList.value.length === 0) {
      ElMessage.info('未找到快照记录')
    }
    
  } catch (error: any) {
    console.error('[快照记录] ❌ 加载失败:', error)
    const errorMsg = error?.message || error || '未知错误'
    ElMessage.error('加载快照数据失败: ' + errorMsg)
    
    // 失败时显示空数据
    snapshotList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/**
 * 组件挂载时初始化数据
 */
onMounted(async () => {
  // 加载空间树
  await loadSpaceTree()
  
  // 加载快照数据
  loadSnapshotData()
})
</script>

<style scoped lang="scss">
.snapshot-record-container {
  display: flex;
  height: calc(100vh - 84px);
  background: #0a0a0a;
  gap: 10px;
  padding: 10px;
  
  // 左侧面板
  .left-panel {
    width: 240px;
    background: #1e1e1e;
    border: 1px solid #3a3a3a;
    border-radius: 4px;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    
    .panel-section {
      border-bottom: 1px solid #3a3a3a;
      
      .section-header {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 12px;
        background: #252525;
        font-size: 14px;
        font-weight: 500;
        color: #e0e0e0;
      }
      
      .search-box {
        padding: 8px 12px;
      }
      
      .device-tree {
        max-height: 300px;
        overflow-y: auto;
        padding: 8px;
        background: transparent;
        color: #e0e0e0;
        
        :deep(.el-tree-node__content) {
          background: transparent;
          color: #e0e0e0;
          border-radius: 4px;
          margin: 2px 0;
          
          &:hover {
            background: rgba(255, 255, 255, 0.1);
          }
        }
        
        :deep(.el-tree-node__expand-icon) {
          color: #e0e0e0;
        }
        
        // 默认隐藏所有复选框
        :deep(.el-tree-node__content) {
          .el-checkbox {
            display: none;
          }
        }
        
        // 显示"通道"文件夹节点的复选框
        :deep(.el-tree-node__content:has(.node-type-channels)) {
          .el-checkbox {
            display: inline-block;
          }
        }
        
        // 显示通道节点的复选框
        :deep(.el-tree-node__content:has(.node-type-channel)) {
          .el-checkbox {
            display: inline-block;
          }
        }
        
        :deep(.el-checkbox__inner) {
          background-color: rgba(255, 255, 255, 0.1);
          border-color: rgba(255, 255, 255, 0.3);
        }
        
        :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
          background-color: #409eff;
          border-color: #409eff;
        }
        
        .tree-node {
          display: flex;
          align-items: center;
          gap: 8px;
          flex: 1;
          
          span {
            font-size: 14px;
          }
        }
      }
      
      .time-filter {
        padding: 12px;
        
        .filter-item {
          margin-bottom: 12px;
          
          label {
            display: block;
            font-size: 12px;
            color: #909399;
            margin-bottom: 4px;
          }
        }
      }
    }
  }
  
  // 主内容区域
  .main-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: #1e1e1e;
    border: 1px solid #3a3a3a;
    border-radius: 4px;
    overflow: hidden;
    
    // 抓图记录展示区域
    .snapshot-grid-section {
      flex: 1;
      padding: 16px;
      overflow-y: auto;
      background: #1e1e1e;
      
      .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        
        h3 {
          margin: 0;
          color: #ffffff;
          font-size: 18px;
          font-weight: 500;
        }
        
        .grid-controls {
          display: flex;
          align-items: center;
          gap: 15px;
          
          span {
            color: #ffffff;
            font-size: 14px;
          }
          
          :deep(.el-button-group .el-button) {
            background: rgba(255, 255, 255, 0.1);
            border-color: rgba(255, 255, 255, 0.2);
            color: #ffffff;
            
            &.active {
              background: #409eff;
              border-color: #409eff;
            }
          }
        }
      }
      
      // 网格视图
      .snapshot-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
        gap: 16px;
        
        .snapshot-item {
          background: #252525;
          border-radius: 6px;
          overflow: hidden;
          cursor: pointer;
          transition: all 0.3s ease;
          border: 1px solid #3a3a3a;
          
          &:hover {
            transform: translateY(-3px);
            box-shadow: 0 6px 16px rgba(0, 0, 0, 0.5);
            border-color: #409eff;
            
            .image-overlay {
              opacity: 1;
            }
          }
          
          .snapshot-image {
            position: relative;
            height: 200px;
            overflow: hidden;
            
            img {
              width: 100%;
              height: 100%;
              object-fit: cover;
              transition: transform 0.3s ease;
            }
            
            .image-overlay {
              position: absolute;
              top: 0;
              left: 0;
              right: 0;
              bottom: 0;
              background: rgba(0, 0, 0, 0.5);
              display: flex;
              align-items: center;
              justify-content: center;
              opacity: 0;
              transition: opacity 0.3s ease;
              
              .preview-icon {
                font-size: 24px;
                color: #ffffff;
              }
            }
          }
          
          .snapshot-info {
            padding: 15px;
            
            .device-name {
              color: #ffffff;
              font-size: 14px;
              font-weight: 500;
              margin-bottom: 8px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
            
            .capture-time {
              color: rgba(255, 255, 255, 0.7);
              font-size: 12px;
            }
          }
        }
      }
      
      // 列表视图
      .snapshot-list {
        :deep(.el-table) {
          background: #252525;
          color: #e0e0e0;
          
          .el-table__header {
            background: #2a2a2a;
            
            th {
              background: #2a2a2a;
              color: #e0e0e0;
              border-bottom: 1px solid #3a3a3a;
            }
          }
          
          .el-table__body {
            tr {
              background: #252525;
              
              &:hover {
                background: rgba(64, 158, 255, 0.1);
              }
              
              td {
                border-bottom: 1px solid #3a3a3a;
                color: #e0e0e0;
              }
            }
          }
        }
        
        .table-thumbnail {
          width: 60px;
          height: 40px;
          object-fit: cover;
          border-radius: 4px;
          cursor: pointer;
          transition: transform 0.3s ease;
          
          &:hover {
            transform: scale(1.1);
          }
        }
      }
      
      // 分页区域
      .pagination-section {
        margin-top: 16px;
        padding-top: 16px;
        border-top: 1px solid #3a3a3a;
        display: flex;
        justify-content: center;
        
        :deep(.el-pagination) {
          .el-pager li,
          .btn-prev,
          .btn-next {
            background: #2a2a2a;
            color: #e0e0e0;
            border: 1px solid #3a3a3a;
            
            &:hover {
              background: #409eff;
              color: #fff;
            }
            
            &.active {
              background: #409eff;
              border-color: #409eff;
              color: #fff;
            }
          }
          
          .el-select .el-input__inner {
            background: #2a2a2a;
            color: #e0e0e0;
            border-color: #3a3a3a;
          }
        }
      }
    }
  }
  
  // 预览对话框样式
  :deep(.el-dialog) {
    background: #1e1e1e;
    border: 1px solid #3a3a3a;
    
    .el-dialog__header {
      background: #252525;
      border-bottom: 1px solid #3a3a3a;
      
      .el-dialog__title {
        color: #e0e0e0;
      }
    }
    
    .el-dialog__body {
      background: #1e1e1e;
    }
    
    .el-dialog__footer {
      background: #252525;
      border-top: 1px solid #3a3a3a;
    }
  }
  
  .preview-content {
    text-align: center;
    
    .preview-img {
      max-width: 100%;
      max-height: 60vh;
      border-radius: 6px;
      margin-bottom: 20px;
      border: 1px solid #3a3a3a;
    }
    
    .preview-info {
      text-align: left;
      color: #e0e0e0;
      background: #252525;
      padding: 16px;
      border-radius: 6px;
      border: 1px solid #3a3a3a;
      
      p {
        margin: 8px 0;
        font-size: 14px;
        
        strong {
          color: #409eff;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .snapshot-record-container {
    .snapshot-grid-section .snapshot-grid {
      grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    }
  }
}

@media (max-width: 768px) {
  .snapshot-record-container {
    flex-direction: column;
    
    .sidebar {
      width: 100%;
      height: 200px;
    }
    
    .main-content {
      .filter-section .filter-row {
        flex-direction: column;
        align-items: stretch;
        
        .filter-item {
          flex-direction: column;
          align-items: stretch;
          
          :deep(.el-select),
          :deep(.el-date-editor) {
            width: 100%;
          }
        }
      }
      
      .snapshot-grid-section .snapshot-grid {
        grid-template-columns: 1fr;
      }
    }
  }
}
</style>






