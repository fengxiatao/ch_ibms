<template>
  <ContentWrap
    :body-style="{
      padding: '0',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      backgroundColor: 'var(--el-bg-color)'
    }"
    style="height: calc(100vh - var(--page-top-gap, 70px)); padding-top: var(--page-top-gap, 70px)"
  >
    <div class="access-management-container">
      <!-- 左侧：控制器树形列表 -->
      <div class="controller-tree-panel">
        <div class="panel-header">
          <span class="panel-title">门禁控制器</span>
          <el-button type="primary" link :icon="Refresh" @click="loadControllerTree" :loading="treeLoading">
            刷新
          </el-button>
        </div>
        <div class="tree-content" v-loading="treeLoading">
          <el-tree
            ref="treeRef"
            :data="controllerTree"
            :props="treeProps"
            node-key="id"
            highlight-current
            default-expand-all
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <span class="node-icon">
                  <el-icon v-if="data.type === 'controller'" :color="data.online ? '#67c23a' : '#909399'">
                    <Monitor />
                  </el-icon>
                  <el-icon v-else :color="data.operable ? '#409eff' : '#909399'">
                    <Key />
                  </el-icon>
                </span>
                <span class="node-label">{{ node.label }}</span>
                <span v-if="data.type === 'controller'" class="node-status">
                  <el-tag :type="data.online ? 'success' : 'info'" size="small">
                    {{ data.online ? '在线' : '离线' }}
                  </el-tag>
                </span>
                <span v-else class="node-status">
                  <el-tag :type="getDoorStatusType(data.doorStatus)" size="small">
                    {{ data.doorStatusDesc || '未知' }}
                  </el-tag>
                </span>
              </div>
            </template>
          </el-tree>
          <el-empty v-if="!treeLoading && controllerTree.length === 0" description="暂无门禁控制器" />
        </div>
      </div>

      <!-- 右侧：门通道详情和操作 -->
      <div class="channel-detail-panel">
        <div class="panel-header">
          <span class="panel-title">
            {{ selectedController ? selectedController.deviceName : '请选择控制器' }}
          </span>
          <div v-if="selectedController" class="header-actions">
            <el-tag :type="selectedController.online ? 'success' : 'info'" class="mr-10px">
              {{ selectedController.online ? '在线' : '离线' }}
            </el-tag>
            <el-button type="primary" link :icon="Refresh" @click="refreshController" :loading="detailLoading">
              刷新状态
            </el-button>
          </div>
        </div>

        <div class="detail-content" v-loading="detailLoading">
          <!-- 控制器信息卡片 -->
          <template v-if="selectedController">
            <el-card class="controller-info-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <span>控制器信息</span>
                  <el-button 
                    v-if="selectedController.online && selectedController.supportVideo"
                    type="primary" 
                    size="small"
                    :icon="VideoCamera"
                    @click="handleVideoPreview()"
                  >
                    视频预览
                  </el-button>
                </div>
              </template>
              <el-descriptions :column="3" border size="small">
                <el-descriptions-item label="设备名称">{{ selectedController.deviceName }}</el-descriptions-item>
                <el-descriptions-item label="设备编码">{{ selectedController.deviceCode || '-' }}</el-descriptions-item>
                <el-descriptions-item label="IP地址">{{ selectedController.ipAddress || selectedController.config?.ipAddress || '-' }}:{{ selectedController.port || selectedController.config?.port || '-' }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="selectedController.online ? 'success' : 'info'">
                    {{ selectedController.stateDesc || (selectedController.online ? '在线' : '离线') }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="通道数量">{{ selectedController.channelCount || 0 }}</el-descriptions-item>
                <el-descriptions-item label="最后上线">
                  {{ selectedController.lastOnlineTime ? formatDate(selectedController.lastOnlineTime) : '-' }}
                </el-descriptions-item>
              </el-descriptions>
            </el-card>

            <!-- 门通道列表 -->
            <el-card class="channel-list-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <span>门通道列表</span>
                  <el-button 
                    v-if="selectedController.online" 
                    type="primary" 
                    size="small" 
                    @click="handleBatchOpenDoor"
                    :disabled="!selectedController.online"
                  >
                    全部开门
                  </el-button>
                </div>
              </template>
              <el-table :data="channelList" stripe border>
                <el-table-column label="通道号" prop="channelNo" width="80" align="center" />
                <el-table-column label="通道名称" prop="channelName" min-width="120">
                  <template #default="{ row }">
                    {{ row.channelName || `门${row.channelNo}` }}
                  </template>
                </el-table-column>
                <el-table-column label="门状态" width="80" align="center">
                  <template #default="{ row }">
                    <el-tag :type="getDoorStatusType(row.doorStatus)" size="small">
                      {{ row.doorStatusDesc || getDoorStatusLabel(row.doorStatus) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="锁状态" width="80" align="center">
                  <template #default="{ row }">
                    <el-tooltip 
                      :content="getLockModeTooltip(row.alwaysMode)" 
                      placement="top"
                      :disabled="row.alwaysMode === 0"
                    >
                      <el-tag :type="getAlwaysModeType(row.alwaysMode)" size="small">
                        {{ getAlwaysModeLabel(row.alwaysMode) }}
                      </el-tag>
                    </el-tooltip>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="280" align="center" fixed="right">
                  <template #default="{ row }">
                    <el-button-group>
                      <el-tooltip 
                        :content="getOpenDoorDisabledReason(row)" 
                        placement="top"
                        :disabled="canOpenDoor(row)"
                      >
                        <el-button 
                          type="success" 
                          size="small" 
                          :disabled="!canOpenDoor(row)"
                          :loading="operatingChannelId === row.channelId && operatingAction === 'OPEN_DOOR'"
                          @click="handleDoorControl(row, 'OPEN_DOOR')"
                        >
                          开门
                        </el-button>
                      </el-tooltip>
                      <el-button 
                        type="warning" 
                        size="small" 
                        :disabled="!row.operable"
                        :loading="operatingChannelId === row.channelId && operatingAction === 'CLOSE_DOOR'"
                        @click="handleDoorControl(row, 'CLOSE_DOOR')"
                      >
                        关门
                      </el-button>
                    </el-button-group>
                    <el-dropdown 
                      class="ml-10px" 
                      trigger="click" 
                      :disabled="!row.operable"
                      @command="(cmd) => handleDoorControl(row, cmd)"
                    >
                      <el-button type="primary" size="small" :disabled="!row.operable">
                        更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                      </el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item command="ALWAYS_OPEN" :disabled="row.alwaysMode === 1">
                            <el-icon><Unlock /></el-icon>设置常开
                          </el-dropdown-item>
                          <el-dropdown-item command="ALWAYS_CLOSED" :disabled="row.alwaysMode === 2">
                            <el-icon><Lock /></el-icon>设置常闭
                          </el-dropdown-item>
                          <el-dropdown-item command="CANCEL_ALWAYS" :disabled="row.alwaysMode === 0" divided>
                            <el-icon><Close /></el-icon>取消常开/常闭
                          </el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="channelList.length === 0" description="暂无门通道" />
            </el-card>
          </template>

          <!-- 未选择控制器时的提示 -->
          <el-empty v-else description="请从左侧选择一个门禁控制器" />
        </div>
      </div>
    </div>
    
    <!-- 视频预览弹窗 -->
    <VideoPreviewDialog
      v-model="videoPreviewVisible"
      :device-id="videoPreviewDeviceId"
      :device-name="videoPreviewDeviceName"
      :channel-no="videoPreviewChannelNo"
      :play-params="videoPreviewParams"
      @close="handleVideoPreviewClose"
    />
  </ContentWrap>
</template>


<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Monitor, Key, ArrowDown, Unlock, Lock, Close, VideoCamera } from '@element-plus/icons-vue'
import { ContentWrap } from '@/components/ContentWrap'
import { 
  AccessManagementApi, 
  AccessVideoApi,
  DoorStatusOptions, 
  LockStatusOptions, 
  AlwaysModeOptions,
  type AccessControllerTreeVO, 
  type DoorChannelVO,
  type DoorControlReqVO,
  type AccessVideoPlayParamsVO
} from '@/api/iot/access'
import { formatDate } from '@/utils/formatTime'
import VideoPreviewDialog from '../components/VideoPreviewDialog.vue'
import { 
  useAccessDeviceStatusWebSocket,
  type AccessDeviceStatusMessage,
  type UnifiedDeviceStatusMessage
} from './useAccessDeviceStatusWebSocket'

defineOptions({ name: 'AccessManagement' })

// ========== 状态定义 ==========
const treeRef = ref()
const treeLoading = ref(false)
const detailLoading = ref(false)
const controllerTree = ref<any[]>([])
const selectedController = ref<AccessControllerTreeVO | null>(null)
const channelList = ref<DoorChannelVO[]>([])
const operatingChannelId = ref<number | null>(null)
const operatingAction = ref<string | null>(null)

const isGen2 = computed(() => {
  const dt = (selectedController.value as any)?.deviceType
  return String(dt || '').toUpperCase() === 'ACCESS_GEN2'
})

// 视频预览状态
const videoPreviewVisible = ref(false)
const videoPreviewDeviceId = ref<number | null>(null)
const videoPreviewDeviceName = ref<string>('')
const videoPreviewChannelNo = ref<number>(0)
const videoPreviewParams = ref<AccessVideoPlayParamsVO | null>(null)
const videoSupportMap = ref<Map<number, boolean>>(new Map())

// 树形结构配置
const treeProps = {
  children: 'children',
  label: 'label'
}

const normalizeArrayResult = (result: any): any[] => {
  if (Array.isArray(result)) return result
  // 兼容：有些调用方可能拿到的是整包 { code, msg, data }
  if (Array.isArray(result?.data)) return result.data
  // 兼容：部分接口可能返回 { list: [...] }
  if (Array.isArray(result?.list)) return result.list
  // 兼容：如果调用方拿到的是 AxiosResponse（{ data: { code, msg, data } }）
  if (Array.isArray(result?.data?.data)) return result.data.data
  if (Array.isArray(result?.data?.list)) return result.data.list
  return []
}

// ========== 状态辅助函数 ==========
const getDoorStatusType = (status: number) => {
  const item = DoorStatusOptions.find(i => i.value === status)
  return item?.type || 'info'
}

const getDoorStatusLabel = (status: number) => {
  const item = DoorStatusOptions.find(i => i.value === status)
  return item?.label || '未知'
}

const getLockStatusType = (status: number) => {
  const item = LockStatusOptions.find(i => i.value === status)
  return item?.type || 'info'
}

const getLockStatusLabel = (status: number) => {
  const item = LockStatusOptions.find(i => i.value === status)
  return item?.label || '未知'
}

const getAlwaysModeType = (mode: number) => {
  const item = AlwaysModeOptions.find(i => i.value === mode)
  return item?.type || 'info'
}

const getAlwaysModeLabel = (mode: number) => {
  const item = AlwaysModeOptions.find(i => i.value === mode)
  return item?.label || '正常'
}

// 锁状态提示
const getLockModeTooltip = (mode: number) => {
  switch (mode) {
    case 1: return '门锁处于常开状态，无需刷卡即可通行'
    case 2: return '门锁处于常闭状态，所有开门操作将被禁止'
    default: return ''
  }
}

// 判断是否可以开门
const canOpenDoor = (row: any) => {
  if (!row.operable) return false  // 设备不在线
  if (row.alwaysMode === 2) return false  // 常闭状态
  return true
}

// 获取开门按钮禁用原因
const getOpenDoorDisabledReason = (row: any) => {
  if (!row.operable) return '设备离线，无法操作'
  if (row.alwaysMode === 2) return '门锁处于常闭状态，请先取消常闭后再开门'
  return ''
}

// ========== 数据加载 ==========
/**
 * 加载控制器树（仅从数据库读取，不触发状态检测）
 */
const loadControllerTreeFromDB = async () => {
  const raw = await AccessManagementApi.getControllerTree()
  const data = normalizeArrayResult(raw)
  // 转换为树形结构
  controllerTree.value = data.map(controller => ({
    id: `controller-${controller.deviceId}`,
    label: controller.deviceName,
    type: 'controller',
    ...controller,
    children: (controller.channels || []).map(channel => ({
      id: `channel-${channel.channelId}`,
      label: channel.channelName || `门${channel.channelNo}`,
      type: 'channel',
      controllerId: controller.deviceId,
      controllerName: controller.deviceName,
      controllerOnline: controller.online,
      ...channel
    }))
  }))
  return data
}

/**
 * 加载控制器树（带状态检测）
 * 会先加载数据，然后并行触发所有设备的状态检测，最后重新加载以获取最新状态
 */
const loadControllerTree = async () => {
  treeLoading.value = true
  try {
    // 1. 先从数据库加载树数据
    const data = await loadControllerTreeFromDB()
    
    // 2. 并行触发所有设备的状态检测（不等待结果，异步执行）
    if (data.length > 0) {
      const refreshPromises = data.map(controller => 
        AccessManagementApi.refreshControllerStatus(controller.deviceId).catch(err => {
          console.warn(`设备 ${controller.deviceId} 状态检测失败:`, err)
        })
      )
      
      // 等待所有状态检测完成（最多等待 3 秒）
      await Promise.race([
        Promise.all(refreshPromises),
        new Promise(resolve => setTimeout(resolve, 3000))
      ])
      
      // 3. 重新加载树数据以获取最新状态
      await loadControllerTreeFromDB()
    }
  } catch (error) {
    console.error('加载控制器树失败:', error)
    ElMessage.error('加载控制器列表失败')
  } finally {
    treeLoading.value = false
  }
}

const loadControllerDetail = async (deviceId: number) => {
  detailLoading.value = true
  try {
    const detail = await AccessManagementApi.getControllerDetail(deviceId)
    selectedController.value = detail as any
    channelList.value = detail.channels || []
  } catch (error) {
    console.error('加载控制器详情失败:', error)
    ElMessage.error('加载控制器详情失败')
  } finally {
    detailLoading.value = false
  }
}

// ========== 事件处理 ==========
const handleNodeClick = (data: any) => {
  if (data.type === 'controller') {
    loadControllerDetail(data.deviceId)
  } else if (data.type === 'channel') {
    // 点击通道时，加载其所属控制器
    loadControllerDetail(data.controllerId)
  }
}

const refreshController = async () => {
  if (!selectedController.value) return
  
  detailLoading.value = true
  try {
    await AccessManagementApi.refreshControllerStatus(selectedController.value.deviceId)
    await loadControllerDetail(selectedController.value.deviceId)
    // 同时刷新树
    await loadControllerTree()
    ElMessage.success('刷新成功')
  } catch (error) {
    console.error('刷新控制器状态失败:', error)
    ElMessage.error('刷新失败')
  } finally {
    detailLoading.value = false
  }
}

const handleDoorControl = async (channel: DoorChannelVO, action: string) => {
  if (!selectedController.value) return
  
  const actionLabels: Record<string, string> = {
    'OPEN_DOOR': '开门',
    'CLOSE_DOOR': '关门',
    'ALWAYS_OPEN': '设置常开',
    'ALWAYS_CLOSED': '设置常闭',
    'CANCEL_ALWAYS': '取消常开/常闭'
  }
  
  operatingChannelId.value = channel.channelId
  operatingAction.value = action
  
  try {
    const reqVO: DoorControlReqVO = {
      deviceId: selectedController.value.deviceId,
      channelId: channel.channelId,
      channelNo: channel.channelNo,
      action: action as any
    }
    
    const result = await AccessManagementApi.doorControl(reqVO)
    
    if (result.success) {
      ElMessage.success(`${actionLabels[action]}成功`)
      // 刷新通道状态：
      // - 后端会在门控命令后异步触发一次 QUERY_CHANNELS 同步通道到 DB（校验点）
      // - 这里等待一点时间，确保能读到最新的门状态/常开常闭状态
      await new Promise((resolve) => setTimeout(resolve, 2000))
      await loadControllerDetail(selectedController.value.deviceId)
    } else {
      ElMessage.error(result.errorMessage || `${actionLabels[action]}失败`)
    }
  } catch (error: any) {
    console.error('门控操作失败:', error)
    ElMessage.error(error.message || `${actionLabels[action]}失败`)
  } finally {
    operatingChannelId.value = null
    operatingAction.value = null
  }
}

const handleBatchOpenDoor = async () => {
  if (!selectedController.value || channelList.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(
      `确定要打开"${selectedController.value.deviceName}"的所有门吗？`,
      '批量开门确认'
    )
    
    for (const channel of channelList.value) {
      if (channel.operable) {
        await handleDoorControl(channel, 'OPEN_DOOR')
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量开门失败:', error)
    }
  }
}

// ========== 视频预览功能 ==========
const checkVideoSupport = async (deviceId: number): Promise<boolean> => {
  // 检查缓存
  if (videoSupportMap.value.has(deviceId)) {
    return videoSupportMap.value.get(deviceId) || false
  }
  
  try {
    const result = await AccessVideoApi.checkVideoSupport(deviceId)
    videoSupportMap.value.set(deviceId, result.supportVideo && result.online)
    return result.supportVideo && result.online
  } catch (error) {
    console.error('检查视频支持失败:', error)
    return false
  }
}

const handleVideoPreview = async (channel?: DoorChannelVO) => {
  if (!selectedController.value) return
  
  const deviceId = selectedController.value.deviceId
  const channelNo = channel?.channelNo ?? 0
  
  try {
    // 获取播放参数
    const params = await AccessVideoApi.getPlayParams(deviceId, channelNo)
    
    if (!params.online) {
      ElMessage.warning('设备离线，无法预览视频')
      return
    }
    
    // 设置预览参数
    videoPreviewDeviceId.value = deviceId
    videoPreviewDeviceName.value = selectedController.value.deviceName
    videoPreviewChannelNo.value = channelNo
    videoPreviewParams.value = params
    videoPreviewVisible.value = true
  } catch (error: any) {
    console.error('获取视频播放参数失败:', error)
    ElMessage.error(error.message || '获取视频播放参数失败')
  }
}

const handleVideoPreviewClose = () => {
  videoPreviewVisible.value = false
  videoPreviewParams.value = null
}


// ========== WebSocket 状态监听 (门禁设备状态推送：/ws/iot/access/device/status) ==========
// 使用单例 WebSocket Hook，避免重复连接
const { connect: connectWs, disconnect: disconnectWs } = useAccessDeviceStatusWebSocket({
  autoConnect: false, // 手动连接
  onAccessDeviceStatus: handleAccessDeviceStatusChange,
  onUnifiedDeviceStatus: handleUnifiedDeviceStatus,
  onConnectionChange: (connected) => {
    console.log(`[门禁管理] WebSocket ${connected ? '✅ 已连接' : '❌ 已断开'}`)
  }
})

/**
 * 门禁设备状态消息（后端：AccessDeviceStatusMessage）
 * data: { deviceId, deviceName, onlineStatus(0/1), statusType, activationStatus, ... }
 */
function handleAccessDeviceStatusChange(data: AccessDeviceStatusMessage) {
  if (!data?.deviceId) return
  const deviceId = Number(data.deviceId)
  const online = Number(data.onlineStatus) === 1

  // 更新树中的设备状态
  const controller = controllerTree.value.find((c) => c.deviceId === deviceId)
  if (controller) {
    controller.online = online
    ;(controller as any).activationStatus = data.activationStatus
    ;(controller as any).statusType = data.statusType
    if (controller.children) {
      controller.children.forEach((ch: any) => {
        ch.operable = online
        ch.controllerOnline = online
      })
    }
  }

  // 如果当前选中的是这个控制器，更新详情
  if (selectedController.value?.deviceId === deviceId) {
    selectedController.value.online = online
    channelList.value.forEach((ch) => {
      ch.operable = online
    })
  }

  ElMessage({
    type: online ? 'success' : 'warning',
    message: `设备 ${data.deviceName || controller?.deviceName || deviceId} ${online ? '已上线' : '已离线'}`
  })
}

/**
 * 统一设备状态消息（后端：UnifiedDeviceStatusMessage）
 * data: { deviceId, deviceType, status, timestamp }
 *
 * 说明：后端 DeviceMessagePushService 会通过 /ws/access/device/status 推送 type=DEVICE_STATUS
 */
function handleUnifiedDeviceStatus(data: UnifiedDeviceStatusMessage) {
  if (!data?.deviceId) return
  const deviceId = Number(data.deviceId)
  const status = String(data.status || '').toUpperCase()
  // 在线判定：只有 ONLINE 视为在线（ACTIVATED 状态已移除）
  const online = status === 'ONLINE'

  // 更新树中的设备状态
  const controller = controllerTree.value.find((c) => c.deviceId === deviceId)
  if (controller) {
    controller.online = online
    ;(controller as any).status = status
    ;(controller as any).deviceType = data.deviceType
    if (controller.children) {
      controller.children.forEach((ch: any) => {
        ch.operable = online
        ch.controllerOnline = online
      })
    }
  }

  // 如果当前选中的是这个控制器，更新详情
  if (selectedController.value?.deviceId === deviceId) {
    selectedController.value.online = online
    channelList.value.forEach((ch) => {
      ch.operable = online
    })
  }

  // 对门禁设备才提示（避免后续接入其它设备类型时弹太多）
  if (String(data.deviceType || '').toUpperCase().startsWith('ACCESS')) {
    ElMessage({
      type: online ? 'success' : 'warning',
      message: `设备 ${controller?.deviceName || deviceId} 状态更新：${status}`
    })
  }
}

// ========== 生命周期 ==========
onMounted(async () => {
  // 首次加载：仅从数据库读取（快速显示）
  treeLoading.value = true
  try {
    await loadControllerTreeFromDB()
  } catch (error) {
    console.error('首次加载控制器树失败:', error)
  } finally {
    treeLoading.value = false
  }
  
  // 使用单例 WebSocket，手动连接
  connectWs()
  
  // 延迟 2 秒后触发状态检测，确保显示最新的设备状态
  // 这样可以解决数据库状态与实际设备状态不同步的问题
  setTimeout(() => {
    loadControllerTree()
  }, 2000)
})

onUnmounted(() => {
  disconnectWs()
})
</script>

<style scoped lang="scss">
.access-management-container {
  display: flex;
  height: 100%;
  gap: 16px;
  padding: 16px;
  background-color: var(--el-bg-color-page);
}

.controller-tree-panel {
  width: 320px;
  min-width: 280px;
  background-color: var(--el-bg-color);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  box-shadow: var(--el-box-shadow-light);
}

.channel-detail-panel {
  flex: 1;
  background-color: var(--el-bg-color);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  box-shadow: var(--el-box-shadow-light);
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  
  .panel-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }
  
  .header-actions {
    display: flex;
    align-items: center;
  }
}

.tree-content {
  flex: 1;
  overflow: auto;
  padding: 8px;
}

.tree-node {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 4px 0;
  
  .node-icon {
    margin-right: 8px;
    display: flex;
    align-items: center;
  }
  
  .node-label {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .node-status {
    margin-left: 8px;
  }
}

.detail-content {
  flex: 1;
  overflow: auto;
  padding: 16px;
}

.controller-info-card {
  margin-bottom: 16px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.channel-list-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

:deep(.el-tree-node__content) {
  height: 40px;
}

:deep(.el-card__header) {
  padding: 12px 16px;
  background-color: var(--el-fill-color-light);
}

.mr-10px {
  margin-right: 10px;
}

.ml-10px {
  margin-left: 10px;
}
</style>
