<template>
  <ContentWrap :body-style="{ padding: '0', height: '100%', display: 'flex', flexDirection: 'column' }" style="height: calc(100vh - var(--page-top-gap, 70px)); padding-top: var(--page-top-gap, 70px)">
    <div class="task-edit-page">
      <div class="content">
        <div class="left">
          <div class="left-toolbar">
            <el-input v-model="deviceSearchKeyword" placeholder="搜索通道..." clearable size="small">
              <template #prefix>
                <Icon icon="ep:search" />
              </template>
            </el-input>
          </div>
          <el-tree 
            :data="cameraTreeData" 
            :props="treeProps" 
            :lazy="true" 
            :load="loadTreeNode" 
            :accordion="true" 
            node-key="id" 
            class="org-tree"
            :filter-node-method="filterNode"
            ref="treeRef"
          >
            <template #default="{ data }">
              <div 
                class="tree-node" 
                :class="{ 'is-channel': data.type === 'channel', 'is-online': data.isOnline, 'is-draggable': data.type === 'channel' }"
                :draggable="data.type === 'channel'" 
                @dragstart="handleDragStart($event, data)"
              >
                <Icon v-if="data.type === 'building'" icon="ep:office-building" style="color: #409eff" />
                <Icon v-else-if="data.type === 'floor'" icon="ep:tickets" style="color: #67c23a" />
                <Icon 
                  v-else-if="data.type === 'channel'" 
                  icon="ep:video-camera-filled" 
                  :style="{ color: data.isOnline ? '#67c23a' : '#909399' }"
                />
                <span>{{ data.name }}</span>
              </div>
            </template>
          </el-tree>
          <el-collapse v-model="activePreviewPanel">
            <el-collapse-item title="预览" name="preview">
              <div class="preview-box">
                <div v-if="!previewChannel" class="preview-placeholder">
                  <Icon icon="ep:video-camera" :size="32" />
                  <span>点击"预览"查看通道</span>
                </div>
                <div v-else-if="previewPane.isLoading" class="preview-loading">
                  <Icon icon="ep:loading" :size="24" class="is-loading" />
                  <span>加载中...</span>
                </div>
                <div v-else-if="previewPane.error" class="preview-error">
                  <Icon icon="ep:warning" :size="24" />
                  <span>{{ previewPane.error }}</span>
                </div>
                <!-- 大华 SDK 播放器容器 -->
                <div v-show="previewChannel && !previewPane.isLoading && !previewPane.error" class="dahua-preview" ref="dahuaPreviewContainerRef"></div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
        <div class="center">
          <div class="header">
            <div class="header-left">
              <el-form inline>
                <el-form-item label="任务名称">
                  <el-input v-model="form.taskName" placeholder="任务名称" style="width: 180px" size="small" />
                </el-form-item>
                <el-form-item label="任务时长">
                  <el-input-number 
                    v-model="form.duration" 
                    :min="10" 
                    :max="3600" 
                    size="small" 
                    @change="handleTaskDurationChange"
                  />
                  <span class="unit">秒</span>
                </el-form-item>
              </el-form>
            </div>
            <div class="header-right">
              <el-select v-model="gridLayout" placeholder="分屏布局" style="width: 100px" size="small">
                <el-option label="1×1" value="1x1" />
                <el-option label="2×2" value="2x2" />
                <el-option label="2×3" value="2x3" />
                <el-option label="3×3" value="3x3" />
                <el-option label="3×4" value="3x4" />
                <el-option label="4×4" value="4x4" />
              </el-select>
            </div>
          </div>
          <div class="center-scroll">
            <div class="grid-preview" :class="gridClass">
              <div 
                v-for="(_, i) in panes" 
                :key="i" 
                class="pane"
                :class="{ 'drag-over': dragOverPane === i, 'selected': selectedPane === i }"
                @click="selectedPane = i"
                @drop="handleDrop($event, i)"
                @dragover.prevent="handleDragOver($event, i)"
                @dragleave="handleDragLeave"
              >
                <div v-if="getPaneChannels(i).length === 0" class="pane-empty">
                  <Icon icon="ep:plus" :size="24" />
                  <span>拖拽通道到此</span>
                </div>
                <div v-else class="pane-channels">
                  <div class="channel-count">格子 {{ i + 1 }} - {{ getPaneChannels(i).length }} 个通道</div>
                  <div class="channel-list">
                    <div v-for="ch in getPaneChannels(i).slice(0, 3)" :key="ch.id" class="channel-item">
                      <Icon icon="ep:video-camera" />
                      <span>{{ ch.channelName }}</span>
                    </div>
                    <div v-if="getPaneChannels(i).length > 3" class="channel-more">
                      +{{ getPaneChannels(i).length - 3 }} 更多
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="table-wrap">
            <el-table :data="currentPaneChannels" height="220">
              <el-table-column type="index" label="序号" width="60" />
              <el-table-column prop="gridPosition" label="格子位置" width="100">
                <template #default="{ row }">
                  格子 {{ row.gridPosition }}
                </template>
              </el-table-column>
              <el-table-column prop="channelName" label="通道名称" show-overflow-tooltip />
              <el-table-column prop="duration" label="播放时长" width="150">
                <template #default="{ row }">
                  <el-input-number 
                    v-model="row.duration" 
                    :min="1" 
                    :max="600" 
                    size="small"
                    @change="handleDurationChange(row)"
                  />
                  <span style="margin-left: 4px">秒</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180">
                <template #default="{ row, $index }">
                  <div class="ops">
                    <el-button size="small" @click="handlePreview(row)" :disabled="!row.channelNo">预览</el-button>
                    <span class="op-icon" title="上移" @click="handleMoveUp($index)"><Icon icon="ep:arrow-up" /></span>
                    <span class="op-icon" title="下移" @click="handleMoveDown($index)"><Icon icon="ep:arrow-down" /></span>
                    <span class="op-icon" title="删除" @click="handleRemoveRow($index)"><Icon icon="ep:delete" /></span>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="footer">
            <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
            <el-button @click="handleCancel">取消</el-button>
          </div>
        </div>
      </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ContentWrap } from '@/components/ContentWrap'
import { Icon } from '@/components/Icon'
import { ElMessage } from 'element-plus'
import {
  getPatrolTask,
  createPatrolTask,
  updatePatrolTask,
  getPatrolSceneByTaskId,
  savePatrolSceneWithChannels,
  type PatrolTaskVO,
  type PatrolSceneVO,
  type PatrolSceneChannelVO
} from '@/api/iot/patrolplan'
import { getBuildingList, getFloorListByBuildingId } from '@/api/iot/spatial'
import { getChannelPage } from '@/api/iot/channel'
import { useDahuaPlayer, DEFAULT_NVR_CONFIG } from '@/composables/useDahuaPlayer'

// IBMS 通道类型
interface IbmsChannel {
  id: number
  deviceId: number
  channelNo: number
  channelName: string
  channelType: string
  onlineStatus?: number
  targetIp?: string
  targetPort?: number
  rtspPort?: number        // RTSP 端口（默认 554）
  username?: string
  password?: string
  streamUrlMain?: string
  streamUrlSub?: string
  ptzSupport?: boolean
}

const route = useRoute()
const router = useRouter()
const planId = Number(route.query.planId)
const taskId = route.query.taskId ? Number(route.query.taskId) : undefined
const mode = route.query.mode as 'add' | 'edit'

const form = ref({
  taskName: '新任务',
  duration: 60,
  taskOrder: 1
})
const deviceSearchKeyword = ref('')
const saving = ref(false)
const treeRef = ref()

// ==================== 大华 SDK 播放器 ====================

const { checkSDK, createEmptyPane, startPreview, stopPlayer } = useDahuaPlayer()

// IBMS 通道缓存
const channelCache = new Map<number, IbmsChannel>()

// ==================== 空间树相关 ====================

interface TreeNode {
  id: string
  name: string
  type: 'building' | 'floor' | 'channel'
  buildingId?: number
  floorId?: number
  channelId?: number
  ibmsChannel?: IbmsChannel
  isOnline?: boolean
  children?: TreeNode[]
}

const cameraTreeData = ref<TreeNode[]>([])
const treeProps = { label: 'name', children: 'children', isLeaf: (data: TreeNode) => data.type === 'channel' }

// 搜索过滤
watch(deviceSearchKeyword, (val) => {
  treeRef.value?.filter(val)
})

const filterNode = (value: string, data: TreeNode) => {
  if (!value) return true
  return data.name.toLowerCase().includes(value.toLowerCase())
}

// 加载空间树
const loadSpaceTree = async () => {
  try {
    const buildings = await getBuildingList()
    const treeData = buildings.map((b: any) => ({
      id: `building-${b.id}`,
      name: b.name,
      type: 'building' as const,
      buildingId: b.id
    }))
    cameraTreeData.value = treeData
  } catch (e: any) {
    console.error('加载空间树失败:', e)
  }
}

// 懒加载树节点（直接在建筑/楼层下加载通道，不需要区域和中间的"通道"节点）
const loadTreeNode = async (node: any, resolve: Function) => {
  try {
    const data = node.data as TreeNode
    let children: TreeNode[] = []
    
    if (data.type === 'building') {
      // 加载楼层
      const floors = await getFloorListByBuildingId(data.buildingId!)
      children = floors.map((f: any) => ({
        id: `floor-${f.id}`,
        name: f.name,
        type: 'floor' as const,
        floorId: f.id,
        buildingId: data.buildingId
      }))
      
      // 加载建筑下的直属通道
      const params: any = { pageNo: 1, pageSize: 100, channelType: 'video', buildingId: data.buildingId }
      const channelsRes = await getChannelPage(params)
      const channelsList = channelsRes.list || []
      
      // 将通道添加到建筑下（过滤掉有楼层的通道）
      const buildingChannels = channelsList.filter((ch: any) => !ch.floorId)
      for (const ch of buildingChannels) {
        const ibmsChannel = ch as IbmsChannel
        channelCache.set(ch.id, ibmsChannel)
        children.push({
          id: `channel-${ch.id}`,
          name: ch.channelName || `通道${ch.channelNo}`,
          type: 'channel',
          channelId: ch.id,
          ibmsChannel: ibmsChannel,
          isOnline: ch.onlineStatus === 1
        })
      }
    } else if (data.type === 'floor') {
      // 直接加载楼层下的通道
      const params: any = { pageNo: 1, pageSize: 100, channelType: 'video', floorId: data.floorId }
      const channelsRes = await getChannelPage(params)
      const channelsList = channelsRes.list || []
      
      children = channelsList.map((ch: any) => {
        const ibmsChannel = ch as IbmsChannel
        channelCache.set(ch.id, ibmsChannel)
        return {
          id: `channel-${ch.id}`,
          name: ch.channelName || `通道${ch.channelNo}`,
          type: 'channel' as const,
          channelId: ch.id,
          ibmsChannel: ibmsChannel,
          isOnline: ch.onlineStatus === 1
        }
      })
    }
    
    resolve(children)
  } catch (e: any) {
    console.error('加载节点失败:', e)
    resolve([])
  }
}

// ==================== 拖拽相关 ====================

const dragOverPane = ref(-1)
const selectedPane = ref(0)

const gridLayout = ref<'1x1' | '2x2' | '2x3' | '3x3' | '3x4' | '4x4'>('2x3')
const gridClass = computed(() => `grid-${gridLayout.value}`)
const paneCount = computed(() => {
  const map: Record<string, number> = { '1x1': 1, '2x2': 4, '2x3': 6, '3x3': 9, '3x4': 12, '4x4': 16 }
  return map[gridLayout.value]
})
const panes = computed(() => Array.from({ length: paneCount.value }, () => ({ bindCount: 1 })))

// 场景通道数据
interface SceneChannelData extends PatrolSceneChannelVO {
  ibmsChannel?: IbmsChannel  // 关联的 IBMS 通道数据
}

const channels = ref<SceneChannelData[]>([])
const sceneId = ref<number>()

// 获取指定格子的通道列表
const getPaneChannels = (paneIndex: number) => {
  const position = paneIndex + 1
  return channels.value.filter(ch => ch.gridPosition === position)
}

// 当前选中格子的通道列表
const currentPaneChannels = computed(() => {
  return getPaneChannels(selectedPane.value)
})

// 拖拽开始
const handleDragStart = (e: DragEvent, data: TreeNode) => {
  if (data.type !== 'channel') {
    e.preventDefault()
    return
  }
  
  if (!data.ibmsChannel) {
    ElMessage.warning('该通道数据未加载，请稍后重试')
    e.preventDefault()
    return
  }
  
  console.log('[拖拽] 开始拖拽通道:', data.ibmsChannel.channelName)
  e.dataTransfer!.effectAllowed = 'copy'
  e.dataTransfer!.setData('text/plain', JSON.stringify({
    ibmsChannel: data.ibmsChannel
  }))
}

// 拖拽经过（必须 preventDefault 才能触发 drop）
const handleDragOver = (e: DragEvent, paneIndex: number) => {
  e.preventDefault()
  e.dataTransfer!.dropEffect = 'copy'
  dragOverPane.value = paneIndex
}

// 拖拽离开
const handleDragLeave = () => {
  dragOverPane.value = -1
}

// 放置通道
const handleDrop = async (e: DragEvent, paneIndex: number) => {
  e.preventDefault()
  dragOverPane.value = -1
  
  try {
    const dataStr = e.dataTransfer!.getData('text/plain')
    if (!dataStr) {
      console.warn('[拖拽] 没有获取到拖拽数据')
      return
    }
    
    console.log('[拖拽] 接收到数据:', dataStr)
    const nodeData = JSON.parse(dataStr)
    const ibmsChannel = nodeData.ibmsChannel as IbmsChannel
    
    if (!ibmsChannel) {
      ElMessage.warning('无效的通道数据')
      return
    }
    
    // 计算该格子当前的总时长
    const currentPaneChannels = channels.value.filter(ch => ch.gridPosition === paneIndex + 1)
    const currentTotalDuration = currentPaneChannels.reduce((sum, ch) => sum + (ch.duration || 0), 0)
    
    // 默认新通道时长为10秒
    const newChannelDuration = 10
    const newTotalDuration = currentTotalDuration + newChannelDuration
    
    // 检查是否超过任务时长
    if (newTotalDuration > form.value.duration) {
      ElMessage.warning(
        `格子 ${paneIndex + 1} 的通道总时长（${newTotalDuration}秒）将超过任务时长（${form.value.duration}秒），` +
        `请先增加任务时长或减少该格子中其他通道的时长`
      )
      return
    }
    
    // 添加到通道列表（使用 IBMS 通道数据）
    const newChannel: SceneChannelData = {
      gridPosition: paneIndex + 1,
      duration: newChannelDuration,
      channelId: ibmsChannel.id,
      channelName: ibmsChannel.channelName || `通道${ibmsChannel.channelNo}`,
      deviceId: String(ibmsChannel.deviceId),
      channelNo: ibmsChannel.channelNo,
      streamUrlMain: ibmsChannel.streamUrlMain,
      streamUrlSub: ibmsChannel.streamUrlSub,
      ibmsChannel: ibmsChannel  // 存储完整的 IBMS 通道数据
    }
    
    channels.value.push(newChannel)
    ElMessage.success(
      `已添加到格子 ${paneIndex + 1}（当前格子总时长：${newTotalDuration}秒 / 任务时长：${form.value.duration}秒）`
    )
  } catch (err) {
    console.error('拖拽失败:', err)
    ElMessage.error('添加通道失败')
  }
}

// ==================== 预览相关 ====================

const activePreviewPanel = ref<string[]>([])
const previewChannel = ref<IbmsChannel | null>(null)
const dahuaPreviewContainerRef = ref<HTMLElement>()

// 预览播放器窗格
const previewPane = createEmptyPane(0)

// 停止预览
const stopPreview = async () => {
  if (previewPane.isPlaying || previewPane.player) {
    await stopPlayer(previewPane)
  }
  previewChannel.value = null
}

// 预览通道（使用大华 SDK）
const handlePreview = async (row: SceneChannelData) => {
  try {
    // 获取通道号（优先使用 ibmsChannel，否则使用保存的 channelNo）
    const channelNo = row.ibmsChannel?.channelNo || row.channelNo
    const channelName = row.ibmsChannel?.channelName || row.channelName
    
    // 检查是否有有效的通道号
    if (!channelNo || channelNo < 1) {
      ElMessage.warning('该通道无效，无法预览')
      return
    }
    
    // 检查 SDK 是否加载
    if (!checkSDK()) {
      ElMessage.error('大华播放器 SDK 未加载，请刷新页面')
      return
    }
    
    // 先展开预览面板
    if (!activePreviewPanel.value.includes('preview')) {
      activePreviewPanel.value = ['preview']
    }
    
    // 等待面板展开
    await nextTick()
    await new Promise(resolve => setTimeout(resolve, 300))
    
    // 停止之前的预览
    await stopPreview()
    
    // 设置预览通道信息（用于显示）
    previewChannel.value = row.ibmsChannel || { channelNo, channelName } as any
    
    // 确保容器存在
    await nextTick()
    if (!dahuaPreviewContainerRef.value) {
      throw new Error('播放器容器不存在')
    }
    
    // 设置容器
    previewPane.container = dahuaPreviewContainerRef.value
    previewPane.streamType = 'sub'  // 预览使用子码流
    
    // 使用大华 SDK 播放
    // 注意：必须通过 NVR 连接，IPC 不支持 WebSocket 直连
    const success = await startPreview(previewPane, {
      ip: DEFAULT_NVR_CONFIG.ip,           // 使用 NVR IP
      port: DEFAULT_NVR_CONFIG.port,       // NVR HTTP 端口
      rtspPort: DEFAULT_NVR_CONFIG.rtspPort,  // NVR RTSP 端口
      username: row.ibmsChannel?.username || DEFAULT_NVR_CONFIG.username,
      password: row.ibmsChannel?.password || DEFAULT_NVR_CONFIG.password,
      channelNo: channelNo,                // NVR 通道号
      subtype: 1  // 子码流
    }, channelName)
    
    if (!success) {
      throw new Error('播放失败')
    }
    
  } catch (e: any) {
    console.error('预览失败:', e)
    previewPane.error = e?.message || '预览失败'
    ElMessage.error('预览失败: ' + (e?.message || e))
  }
}

// ==================== 任务数据 ====================

// 加载任务数据
const loadTask = async () => {
  if (mode === 'edit' && taskId) {
    try {
      const task = await getPatrolTask(taskId)
      form.value = {
        taskName: task.taskName,
        duration: task.duration || 60,
        taskOrder: task.taskOrder || 1
      }
      
      // 加载场景数据
      const scene = await getPatrolSceneByTaskId(taskId)
      if (scene) {
        sceneId.value = scene.id
        gridLayout.value = scene.gridLayout as any
        
        // 转换通道数据，并尝试从缓存获取完整 IBMS 通道数据
        channels.value = (scene.channels || []).map(ch => ({
          ...ch,
          ibmsChannel: ch.channelId ? channelCache.get(ch.channelId) : undefined
        }))
      }
    } catch (error) {
      console.error('[任务编辑] 加载任务失败:', error)
      ElMessage.error('加载任务数据失败')
    }
  }
}

// 修改任务时长时验证
const handleTaskDurationChange = (newDuration: number) => {
  // 检查所有格子的通道时长是否超过新的任务时长
  const gridPositions = [...new Set(channels.value.map(ch => ch.gridPosition))]
  let hasWarning = false
  
  for (const position of gridPositions) {
    const paneChannels = channels.value.filter(ch => ch.gridPosition === position)
    const totalDuration = paneChannels.reduce((sum, ch) => sum + (ch.duration || 0), 0)
    
    if (totalDuration > newDuration) {
      ElMessage.warning(
        `格子 ${position} 的通道总时长（${totalDuration}秒）超过任务时长（${newDuration}秒），保存前请调整`
      )
      hasWarning = true
    }
  }
  
  if (!hasWarning && channels.value.length > 0) {
    ElMessage.success(`任务时长已更新为 ${newDuration} 秒`)
  }
}

// 修改通道时长时验证
const handleDurationChange = (row: SceneChannelData) => {
  const paneChannels = channels.value.filter(ch => ch.gridPosition === row.gridPosition)
  const totalDuration = paneChannels.reduce((sum, ch) => sum + (ch.duration || 0), 0)
  
  if (totalDuration > form.value.duration) {
    ElMessage.warning(
      `格子 ${row.gridPosition} 的通道总时长（${totalDuration}秒）超过任务时长（${form.value.duration}秒），` +
      `请调整通道时长或增加任务时长`
    )
  } else {
    ElMessage.success(
      `格子 ${row.gridPosition} 当前总时长：${totalDuration}秒 / 任务时长：${form.value.duration}秒`
    )
  }
}

// 上移通道
const handleMoveUp = (idx: number) => {
  const currentChannels = currentPaneChannels.value
  if (idx > 0) {
    const globalIdx = channels.value.findIndex(ch => ch === currentChannels[idx])
    const prevGlobalIdx = channels.value.findIndex(ch => ch === currentChannels[idx - 1])
    
    if (globalIdx > -1 && prevGlobalIdx > -1) {
      const temp = channels.value[globalIdx]
      channels.value[globalIdx] = channels.value[prevGlobalIdx]
      channels.value[prevGlobalIdx] = temp
    }
  }
}

// 下移通道
const handleMoveDown = (idx: number) => {
  const currentChannels = currentPaneChannels.value
  if (idx < currentChannels.length - 1) {
    const globalIdx = channels.value.findIndex(ch => ch === currentChannels[idx])
    const nextGlobalIdx = channels.value.findIndex(ch => ch === currentChannels[idx + 1])
    
    if (globalIdx > -1 && nextGlobalIdx > -1) {
      const temp = channels.value[globalIdx]
      channels.value[globalIdx] = channels.value[nextGlobalIdx]
      channels.value[nextGlobalIdx] = temp
    }
  }
}

// 删除通道
const handleRemoveRow = (idx: number) => { 
  const currentChannels = currentPaneChannels.value
  const channelToRemove = currentChannels[idx]
  
  const globalIdx = channels.value.findIndex(ch => ch === channelToRemove)
  if (globalIdx > -1) {
    channels.value.splice(globalIdx, 1)
    ElMessage.success('已删除')
  }
}

// 保存任务
const handleSave = async () => {
  if (!form.value.taskName.trim()) {
    ElMessage.warning('请输入任务名称')
    return
  }
  
  if (!form.value.duration || form.value.duration < 10) {
    ElMessage.warning('任务时长不能少于10秒')
    return
  }
  
  // 验证所有格子的通道时长总和不超过任务时长
  const gridPositions = [...new Set(channels.value.map(ch => ch.gridPosition))]
  for (const position of gridPositions) {
    const paneChannels = channels.value.filter(ch => ch.gridPosition === position)
    const totalDuration = paneChannels.reduce((sum, ch) => sum + (ch.duration || 0), 0)
    
    if (totalDuration > form.value.duration) {
      ElMessage.error(
        `格子 ${position} 的通道总时长（${totalDuration}秒）超过任务时长（${form.value.duration}秒），` +
        `请调整通道时长或增加任务时长后再保存`
      )
      return
    }
  }
  
  saving.value = true
  try {
    // 准备任务数据
    const taskData: PatrolTaskVO = {
      id: taskId,
      planId: planId,
      taskName: form.value.taskName,
      taskCode: `TASK_${Date.now()}`,
      taskOrder: form.value.taskOrder,
      duration: form.value.duration,
      scheduleType: 1,
      loopMode: 1,
      intervalMinutes: 5,
      status: 1
    }
    
    // 保存任务
    let savedTaskId: number
    if (mode === 'edit' && taskId) {
      await updatePatrolTask(taskData)
      savedTaskId = taskId
    } else {
      savedTaskId = await createPatrolTask(taskData) as number
    }
    
    // 准备场景数据
    const [cols, rows] = gridLayout.value.split('x').map(Number)
    const sceneData: PatrolSceneVO = {
      id: sceneId.value,
      taskId: savedTaskId,
      sceneName: form.value.taskName,
      sceneOrder: 0,
      duration: form.value.duration,
      gridLayout: gridLayout.value,
      gridCount: cols * rows,
      description: '',
      status: 1,
      channels: channels.value.map(ch => ({
        gridPosition: ch.gridPosition,
        duration: ch.duration,
        channelId: ch.channelId,
        channelName: ch.channelName,
        deviceId: ch.deviceId,
        channelNo: ch.channelNo,
        streamUrlMain: ch.streamUrlMain,
        streamUrlSub: ch.streamUrlSub
      }))
    }
    
    // 保存场景和通道
    try {
      await savePatrolSceneWithChannels(sceneData)
      ElMessage.success('保存成功')
      router.back()
    } catch (sceneError: any) {
      if (sceneError?.code === 404 || sceneError?.message?.includes('404')) {
        ElMessage.warning('场景保存接口暂未实现，任务已保存，场景配置将在后端接口实现后生效')
        console.log('[任务编辑] 场景数据（待后端实现）:', sceneData)
        setTimeout(() => router.back(), 1500)
      } else {
        throw sceneError
      }
    }
  } catch (error: any) {
    console.error('[任务编辑] 保存失败:', error)
    ElMessage.error('保存失败: ' + (error?.message || error))
  } finally {
    saving.value = false
  }
}

const handleCancel = () => { 
  stopPreview()
  router.back() 
}

// ==================== 生命周期 ====================

onMounted(async () => {
  // 加载空间树
  await loadSpaceTree()
  
  // 加载任务数据
  await loadTask()
})

onBeforeUnmount(() => {
  stopPreview()
})
</script>

<style scoped lang="scss">
.task-edit-page { display: flex; flex-direction: column; height: 100%; }
.header { padding: 8px 12px; border-bottom: 1px solid #e5e7eb; display: flex; align-items: center; justify-content: space-between; position: sticky; top: 0; background: #fff; z-index: 3; gap: 8px; flex-wrap: nowrap; margin-bottom: 2px; }
.header-left { display: flex; align-items: center; gap: 8px; }
.header-right { display: flex; align-items: center; gap: 8px; margin-left: auto; }
.header .unit { margin-left: 6px; color: #64748b; }
.header-left :deep(.el-form-item) { margin-bottom: 0; }
.header .unit { line-height: 32px; }
.content { flex: 1; display: grid; grid-template-columns: 280px 1fr; gap: 10px; padding: 2px 10px; }
.left { border-right: 1px solid #e5e7eb; display: flex; flex-direction: column; }
.left-toolbar { display: grid; gap: 8px; padding: 8px; }
.org-tree { flex: 1; padding: 8px; overflow: auto; }

.tree-node { 
  display: flex; 
  align-items: center; 
  gap: 4px; 
  cursor: pointer; 
  user-select: none;
  padding: 2px 4px;
  border-radius: 4px;
  transition: background 0.2s;
}

.tree-node.is-channel {
  &:hover { 
    background: rgba(64, 158, 255, 0.1); 
  }
}

.tree-node.is-draggable {
  cursor: grab;
  &:active {
    cursor: grabbing;
  }
}

.preview-box { 
  height: 180px; 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  background: #000; 
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.preview-placeholder,
.preview-loading,
.preview-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 12px;
}

.preview-error {
  color: #f56c6c;
}

.dahua-preview {
  width: 100%;
  height: 100%;
}

.is-loading {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.center { display: grid; grid-template-rows: auto 1fr auto auto; height: 100%; min-height: 0; overflow: hidden; }
.center-scroll { min-height: 0; overflow: auto; margin-bottom: 2px; }
.grid-preview { display: grid; gap: 6px; border: 1px solid #e5e7eb; border-radius: 6px; padding: 2px; background: #0f172a; min-height: 0; height: 100%; }
.grid-preview.grid-1x1 { grid-template-columns: repeat(1, 1fr); grid-template-rows: repeat(1, 1fr); }
.grid-preview.grid-2x2 { grid-template-columns: repeat(2, 1fr); grid-template-rows: repeat(2, 1fr); }
.grid-preview.grid-2x3 { grid-template-columns: repeat(3, 1fr); grid-template-rows: repeat(2, 1fr); }  // 2行3列
.grid-preview.grid-3x3 { grid-template-columns: repeat(3, 1fr); grid-template-rows: repeat(3, 1fr); }
.grid-preview.grid-3x4 { grid-template-columns: repeat(4, 1fr); grid-template-rows: repeat(3, 1fr); }  // 3行4列
.grid-preview.grid-4x4 { grid-template-columns: repeat(4, 1fr); grid-template-rows: repeat(4, 1fr); }
.pane { display: flex; flex-direction: column; justify-content: center; align-items: center; border: 2px solid #334155; border-radius: 4px; color: #cbd5e1; background: #111827; cursor: pointer; transition: all 0.2s; }
.pane.drag-over { border-color: #409eff; background: #1a2332; }
.pane.selected { border-color: #67c23a; }
.pane-empty { display: flex; flex-direction: column; align-items: center; gap: 8px; color: #64748b; }
.pane-channels { width: 100%; padding: 8px; }
.channel-count { font-size: 12px; color: #67c23a; margin-bottom: 8px; text-align: center; }
.channel-list { display: flex; flex-direction: column; gap: 4px; }
.channel-item { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #cbd5e1; padding: 2px 4px; background: rgba(255,255,255,0.05); border-radius: 2px; }
.channel-more { font-size: 12px; color: #909399; text-align: center; margin-top: 4px; }
.table-wrap { border: 1px solid #e5e7eb; border-radius: 6px; overflow: hidden; background: #fff; height: 220px; margin-bottom: 2px; }
.footer { padding: 2px 8px; border-top: 1px solid #e5e7eb; display: flex; justify-content: flex-end; gap: 6px; background: #fff; }
.el-table .cell { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.ops { display: flex; align-items: center; gap: 8px; }
.op-icon { cursor: pointer; color: #64748b; display: inline-flex; align-items: center; font-size: 16px; }
.op-icon:hover { color: #3b82f6; }
</style>
