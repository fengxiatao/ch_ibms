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
            :allow-drag="allowDrag"
          >
            <template #default="{ data }">
              <div class="tree-node" :draggable="data.type === 'channel'" @dragstart="handleDragStart($event, data)">
                <Icon v-if="data.type === 'building'" icon="ep:office-building" style="color: #409eff" />
                <Icon v-else-if="data.type === 'floor'" icon="ep:tickets" style="color: #67c23a" />
                <Icon v-else-if="data.type === 'area'" icon="ep:location" style="color: #e6a23c" />
                <Icon v-else-if="data.type === 'channels'" icon="ep:folder-opened" style="color: #909399" />
                <Icon v-else-if="data.type === 'channel'" icon="ep:video-camera" style="color: #f56c6c" />
                <span>{{ data.name }}</span>
              </div>
            </template>
          </el-tree>
          <el-collapse v-model="activePreviewPanel">
            <el-collapse-item title="预览" name="preview">
              <div class="preview-box">
                <VideoPlayer 
                  v-if="previewChannel"
                  ref="videoPlayerRef"
                  :channel="previewChannel"
                  container-id="preview-container"
                  :wnd-index="0"
                  :auto-play="true"
                  @play-start="handlePreviewStart"
                  @error="handlePreviewError"
                />
                <div v-else class="preview-placeholder">点击"预览"查看通道</div>
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
                  <el-input-number v-model="form.duration" :min="10" :max="3600" size="small" />
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
                v-for="(pane, i) in panes" 
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
                    <el-button size="small" @click="handlePreview(row)">预览</el-button>
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
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ContentWrap } from '@/components/ContentWrap'
import { Icon } from '@/components/Icon'
import VideoPlayer from '@/components/VideoPlayer/index.vue'
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
import { getBuildingList, getFloorListByBuildingId, getAreaListByFloorId } from '@/api/iot/spatial'
import { getChannelPage } from '@/api/iot/channel'

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

// 空间树相关
const cameraTreeData = ref<any[]>([])
const treeProps = { label: 'name', children: 'children', isLeaf: 'leaf' }
const allowDrag = (node: any) => node.data.type === 'channel'

// 拖拽相关
const dragOverPane = ref(-1)
const selectedPane = ref(0)

// 预览相关
const activePreviewPanel = ref<string[]>([]) // 控制预览折叠面板
const previewChannel = ref<any>(null)
const videoPlayerRef = ref()

const gridLayout = ref<'1x1' | '2x2' | '2x3' | '3x3' | '3x4' | '4x4'>('2x3')
const gridClass = computed(() => `grid-${gridLayout.value}`)
const paneCount = computed(() => {
  const map: Record<string, number> = { '1x1': 1, '2x2': 4, '2x3': 6, '3x3': 9, '3x4': 12, '4x4': 16 }
  return map[gridLayout.value]
})
const panes = computed(() => Array.from({ length: paneCount.value }, () => ({ bindCount: 1 })))

const channels = ref<PatrolSceneChannelVO[]>([])
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
        channels.value = scene.channels || []
      }
    } catch (error) {
      console.error('[任务编辑] 加载任务失败:', error)
      ElMessage.error('加载任务数据失败')
    }
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
      duration: form.value.duration, // 场景停留时长（使用任务时长）
      gridLayout: gridLayout.value,
      gridCount: cols * rows,
      description: '',
      status: 1,
      channels: channels.value
    }
    
    // 保存场景和通道
    try {
      await savePatrolSceneWithChannels(sceneData)
      ElMessage.success('保存成功')
      router.back()
    } catch (sceneError: any) {
      // 如果是 404 错误，说明后端接口未实现
      if (sceneError?.code === 404 || sceneError?.message?.includes('404')) {
        ElMessage.warning('场景保存接口暂未实现，任务已保存，场景配置将在后端接口实现后生效')
        console.log('[任务编辑] 场景数据（待后端实现）:', sceneData)
        // 任务已保存，返回列表
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

// 加载空间树
const loadSpaceTree = async () => {
  try {
    const buildings = await getBuildingList()
    const treeData = buildings.map((b: any) => ({
      id: `building-${b.id}`,
      name: b.name,
      type: 'building',
      buildingId: b.id
    }))
    cameraTreeData.value = treeData
  } catch (e: any) {
    console.error('加载空间树失败:', e)
  }
}

// 懒加载树节点
const loadTreeNode = async (node: any, resolve: Function) => {
  try {
    const data = node.data
    let children: any[] = []
    
    if (data.type === 'building') {
      children.push({
        id: `channels-building-${data.buildingId}`,
        name: '通道',
        type: 'channels',
        buildingId: data.buildingId
      })
      const floors = await getFloorListByBuildingId(data.buildingId)
      children.push(...floors.map((f: any) => ({
        id: `floor-${f.id}`,
        name: f.name,
        type: 'floor',
        floorId: f.id,
        buildingId: data.buildingId
      })))
    } else if (data.type === 'floor') {
      children.push({
        id: `channels-floor-${data.floorId}`,
        name: '通道',
        type: 'channels',
        floorId: data.floorId,
        buildingId: data.buildingId
      })
      const areas = await getAreaListByFloorId(data.floorId)
      children.push(...areas.map((a: any) => ({
        id: `area-${a.id}`,
        name: a.name,
        type: 'area',
        areaId: a.id,
        floorId: data.floorId
      })))
    } else if (data.type === 'area') {
      children.push({
        id: `channels-area-${data.areaId}`,
        name: '通道',
        type: 'channels',
        areaId: data.areaId,
        floorId: data.floorId,
        buildingId: data.buildingId
      })
    } else if (data.type === 'channels') {
      const params: any = { pageNo: 1, pageSize: 100 }
      if (data.buildingId) params.buildingId = data.buildingId
      if (data.floorId) params.floorId = data.floorId
      if (data.areaId) params.areaId = data.areaId
      
      const channelsRes = await getChannelPage(params)
      const channelsList = channelsRes.list || []
      children = channelsList.map((ch: any) => ({
        id: `channel-${ch.id}`,
        name: ch.channelName || `通道${ch.channelNo}`,
        type: 'channel',
        channelId: ch.id,
        channel: ch
      }))
    }
    
    resolve(children)
  } catch (e: any) {
    console.error('加载节点失败:', e)
    resolve([])
  }
}

// 拖拽开始
const handleDragStart = (e: DragEvent, data: any) => {
  if (data.type !== 'channel') return
  e.dataTransfer!.effectAllowed = 'copy'
  e.dataTransfer!.setData('channel', JSON.stringify(data))
}

// 拖拽经过
const handleDragOver = (e: DragEvent, paneIndex: number) => {
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
    const channelData = JSON.parse(e.dataTransfer!.getData('channel'))
    const channel = channelData.channel
    
    if (!channel) {
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
    
    // 添加到通道列表（允许重复添加同一通道）
    const newChannel: PatrolSceneChannelVO = {
      gridPosition: paneIndex + 1,
      duration: newChannelDuration,
      channelId: channel.id,
      channelName: channel.channelName || channel.name,
      deviceId: channel.deviceId,
      channelNo: channel.channelNo,
      streamUrlMain: channel.streamUrlMain,
      streamUrlSub: channel.streamUrlSub
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

// 修改通道时长时验证
const handleDurationChange = (row: any) => {
  // 计算该格子的总时长
  const paneChannels = channels.value.filter(ch => ch.gridPosition === row.gridPosition)
  const totalDuration = paneChannels.reduce((sum, ch) => sum + (ch.duration || 0), 0)
  
  // 检查是否超过任务时长
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

// 预览通道
const handlePreview = async (row: any) => {
  try {
    // 先展开预览面板
    if (!activePreviewPanel.value.includes('preview')) {
      activePreviewPanel.value = ['preview']
    }
    
    // 等待面板展开
    await nextTick()
    await new Promise(resolve => setTimeout(resolve, 300))
    
    // 获取通道详情
    const channelsRes = await getChannelPage({ 
      pageNo: 1, 
      pageSize: 1, 
      channelName: row.channelName 
    })
    const channel = channelsRes.list?.[0]
    
    if (!channel) {
      ElMessage.error('未找到通道信息')
      return
    }
    
    // 设置预览通道（VideoPlayer 组件会自动播放）
    previewChannel.value = channel
    
  } catch (e: any) {
    console.error('预览失败:', e)
    ElMessage.error('预览失败: ' + (e?.message || e))
  }
}

// 预览开始回调
const handlePreviewStart = () => {
  ElMessage.success('开始预览')
}

// 预览错误回调
const handlePreviewError = (error: any) => {
  console.error('预览错误:', error)
  ElMessage.error('预览失败: ' + (error?.message || error))
}

const handleCancel = () => { router.back() }

// 上移通道（仅在当前格子内）
const handleMoveUp = (idx: number) => {
  const currentChannels = currentPaneChannels.value
  if (idx > 0) {
    // 找到全局索引
    const globalIdx = channels.value.findIndex(ch => ch === currentChannels[idx])
    const prevGlobalIdx = channels.value.findIndex(ch => ch === currentChannels[idx - 1])
    
    if (globalIdx > -1 && prevGlobalIdx > -1) {
      const temp = channels.value[globalIdx]
      channels.value[globalIdx] = channels.value[prevGlobalIdx]
      channels.value[prevGlobalIdx] = temp
    }
  }
}

// 下移通道（仅在当前格子内）
const handleMoveDown = (idx: number) => {
  const currentChannels = currentPaneChannels.value
  if (idx < currentChannels.length - 1) {
    // 找到全局索引
    const globalIdx = channels.value.findIndex(ch => ch === currentChannels[idx])
    const nextGlobalIdx = channels.value.findIndex(ch => ch === currentChannels[idx + 1])
    
    if (globalIdx > -1 && nextGlobalIdx > -1) {
      const temp = channels.value[globalIdx]
      channels.value[globalIdx] = channels.value[nextGlobalIdx]
      channels.value[nextGlobalIdx] = temp
    }
  }
}

// 删除通道（从当前格子中删除）
const handleRemoveRow = (idx: number) => { 
  const currentChannels = currentPaneChannels.value
  const channelToRemove = currentChannels[idx]
  
  // 找到全局索引并删除
  const globalIdx = channels.value.findIndex(ch => ch === channelToRemove)
  if (globalIdx > -1) {
    channels.value.splice(globalIdx, 1)
    ElMessage.success('已删除')
  }
}

// 页面加载时获取数据
onMounted(async () => {
  // 加载空间树
  await loadSpaceTree()
  
  // 加载任务数据
  await loadTask()
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
.org-tree { flex: 1; padding: 8px; }
.placeholder { color: #94a3b8; padding: 8px; }
.preview-box { padding: 8px; height: 180px; display: flex; align-items: center; justify-content: center; background: #000; border-radius: 4px; }
.preview-video { width: 100%; height: 100%; object-fit: contain; }
.preview-placeholder { color: #909399; font-size: 12px; text-align: center; }
.tree-node { display: flex; align-items: center; gap: 4px; cursor: pointer; user-select: none; }
.tree-node[draggable="true"] { cursor: move; }
.tree-node[draggable="true"]:hover { background: rgba(64, 158, 255, 0.1); }
.center { display: grid; grid-template-rows: auto 1fr auto auto; height: 100%; min-height: 0; overflow: hidden; }
.center-scroll { min-height: 0; overflow: auto; margin-bottom: 2px; }
.grid-preview { display: grid; gap: 6px; border: 1px solid #e5e7eb; border-radius: 6px; padding: 2px; background: #0f172a; min-height: 0; height: 100%; }
.grid-preview.grid-1x1 { grid-template-columns: repeat(1, 1fr); grid-template-rows: repeat(1, 1fr); }
.grid-preview.grid-2x2 { grid-template-columns: repeat(2, 1fr); grid-template-rows: repeat(2, 1fr); }
.grid-preview.grid-2x3 { grid-template-columns: repeat(2, 1fr); grid-template-rows: repeat(3, 1fr); }
.grid-preview.grid-3x3 { grid-template-columns: repeat(3, 1fr); grid-template-rows: repeat(3, 1fr); }
.grid-preview.grid-3x4 { grid-template-columns: repeat(3, 1fr); grid-template-rows: repeat(4, 1fr); }
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
