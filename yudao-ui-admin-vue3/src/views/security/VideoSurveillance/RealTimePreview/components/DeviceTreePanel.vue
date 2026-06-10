<template>
  <div class="device-tree-panel">
    <el-collapse v-model="activePanels" accordion>
      <!-- 设备/通道面板 -->
      <el-collapse-item name="device">
        <template #title>
          <span class="panel-title">
            <Icon icon="ep:video-camera" />
            设备
          </span>
        </template>

        <!-- 搜索框和更新按钮 -->
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索通道名称..."
            clearable
            size="small"
            @keyup.enter="handleSearch"
            @clear="handleSearchClear"
          >
            <template #prefix>
              <Icon icon="ep:search" />
            </template>
          </el-input>
          <el-tooltip content="从NVR/IPC设备同步最新通道名称" placement="top">
            <el-button
              type="primary"
              size="small"
              :icon="Refresh"
              :loading="syncLoading"
              class="sync-btn"
              @click="handleSyncAllChannels"
            />
          </el-tooltip>
        </div>

        <!-- 空间树 -->
        <el-tree
          ref="treeRef"
          :data="deviceTreeData"
          :props="treeProps"
          lazy
          :load="loadTreeNode"
          accordion
          node-key="id"
          class="device-tree"
          :highlight-current="false"
        >
          <template #default="{ data }">
            <div
              class="tree-node"
              :draggable="data.type === 'channel'"
              @dragstart="handleTreeDragStart($event, data)"
              @dblclick="handleTreeNodeDoubleClick(data)"
            >
              <Icon
                v-if="data.type === 'space'"
                icon="ep:office-building"
                style="color: #409eff"
              />
              <Icon
                v-else-if="data.type === 'channel'"
                :icon="data.ibmsChannel?.onlineStatus === 1 ? 'ep:video-camera-filled' : 'ep:video-camera'"
                :style="{ color: data.ibmsChannel?.onlineStatus === 1 ? '#67c23a' : '#909399' }"
              />
              <span>{{ data.name }}</span>
            </div>
          </template>
        </el-tree>
      </el-collapse-item>

      <!-- 视图面板 -->
      <el-collapse-item name="views">
        <template #title>
          <span class="panel-title">
            <Icon icon="ep:film" />
            视图
          </span>
        </template>
        <slot name="views"></slot>
      </el-collapse-item>

      <!-- 云台面板 -->
      <el-collapse-item name="ptz" :disabled="!ptzEnabled">
        <template #title>
          <span class="panel-title" :class="{ disabled: !ptzEnabled }">
            <Icon icon="ep:aim" />
            云台
          </span>
          <span v-if="!ptzEnabled" class="ptz-tip">（请选择通道）</span>
        </template>
        <slot name="ptz"></slot>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { Icon } from '@/components/Icon'
import { syncAllNvrChannels } from '@/api/iot/channel'
import { getSpaceTree, type IbmsSpaceTreeNodeRespVO } from '@/api/iot/ibms/space'
import { getChannelPage as getIbmsChannelPage, getChannel as getIbmsChannel } from '@/api/iot/ibms/channel'
import type { DeviceTreeNode, IbmsChannel } from '../types'

// Props
interface Props {
  playingChannelIds?: (number | string)[]  // 正在播放的通道ID列表
  ptzEnabled?: boolean  // 是否启用云台面板
}

const props = withDefaults(defineProps<Props>(), {
  playingChannelIds: () => [],
  ptzEnabled: false
})

// Emits
const emit = defineEmits<{
  (e: 'channel-play', ibmsChannel: IbmsChannel): void
  (e: 'channel-drag-start', event: DragEvent, ibmsChannel: IbmsChannel): void
}>()

// 状态
// 手风琴模式下，activePanels 是字符串（当前展开的面板名称）
const activePanels = ref<string>('device')

// ==================== 云台面板控制 ====================

// 监听云台启用状态，自动收缩云台面板
watch(
  () => props.ptzEnabled,
  (enabled) => {
    if (!enabled && activePanels.value === 'ptz') {
      // 云台不可用时，如果当前展开的是云台面板，自动切换到设备面板
      activePanels.value = 'device'
    }
  }
)
const searchKeyword = ref('')
const treeRef = ref()
const syncLoading = ref(false)

const deviceTreeData = ref<DeviceTreeNode[]>([])
const spaceTreeChildrenMap = new Map<number, IbmsSpaceTreeNodeRespVO[]>()

// 通道缓存 (channelNo -> IbmsChannel)，用于视图加载时快速查找
const channelCache = new Map<number, IbmsChannel>()
const channelIdCache = new Map<number, IbmsChannel>()

// 树配置
const treeProps = {
  children: 'children',
  label: 'name',
  isLeaf: (data: DeviceTreeNode) => data.type === 'channel'
}
const MAX_PAGE_SIZE = 100

const mapStatusToOnlineStatus = (status?: string) => {
  if (status === 'online') {
    return 1
  }
  if (status === 'offline') {
    return 0
  }
  return undefined
}

const mapChannelRowToIbmsChannel = (row: Record<string, any>): IbmsChannel => {
  let extra: Record<string, any> = {}
  if (row?.extra) {
    try {
      extra = JSON.parse(row.extra)
    } catch {
      extra = {}
    }
  }
  return {
    id: row.id,
    deviceId: row.deviceId,
    channelNo: row.channelNo,
    channelName: row.name || `通道${row.channelNo}`,
    channelCode: row.code,
    targetIp: row.ip,
    targetPort: extra.targetPort,
    rtspPort: extra.rtspPort,
    targetChannelNo: extra.targetChannelNo,
    username: extra.username,
    password: extra.password,
    streamUrlMain: extra.streamUrlMain,
    streamUrlSub: extra.streamUrlSub,
    ptzSupport: extra.ptzSupport,
    hasAudio: extra.hasAudio,
    onlineStatus: mapStatusToOnlineStatus(row.status),
    enableStatus: extra.enableStatus,
    spaceId: row.spaceId,
    gbChannelId: extra.gbChannelId,
    gbDeviceId: extra.gbDeviceId
  }
}

const mapSpaceNode = (space: IbmsSpaceTreeNodeRespVO): DeviceTreeNode => {
  return {
    id: `space-${space.id}`,
    name: space.name,
    type: 'space',
    spaceId: space.id
  }
}

const buildSpaceNodeIndex = (nodes: IbmsSpaceTreeNodeRespVO[]) => {
  spaceTreeChildrenMap.clear()
  const walk = (list: IbmsSpaceTreeNodeRespVO[]) => {
    for (const item of list) {
      spaceTreeChildrenMap.set(item.id, item.children || [])
      if (item.children?.length) {
        walk(item.children)
      }
    }
  }
  walk(nodes)
}

const loadSpaceTree = async () => {
  try {
    const spaces = await getSpaceTree()
    buildSpaceNodeIndex(spaces || [])
    deviceTreeData.value = [
      {
        id: 'all-video-channels',
        name: '全部视频通道',
        type: 'channels'
      },
      ...(spaces || []).map(mapSpaceNode)
    ]
    channelCache.clear()
  } catch (e: any) {
    console.error('[空间树] 加载失败:', e)
    ElMessage.error('加载空间列表失败')
  }
}

const loadChannelsBySpace = async (spaceId: number) => {
  const channels: any[] = []
  let pageNo = 1
  let total = 0
  do {
    const res = await getIbmsChannelPage({
      pageNo,
      pageSize: MAX_PAGE_SIZE,
      systemType: 'VI',
      spaceId
    })
    const page = (res && (res as any).list) ? res : (res as any)?.data
    const list = page?.list || []
    total = Number(page?.total || 0)
    channels.push(...list)
    if (!list.length || channels.length >= total) {
      break
    }
    pageNo += 1
  } while (true)

  return channels.map((row: any) => {
    const ibmsChannel = mapChannelRowToIbmsChannel(row)
    if (ibmsChannel.channelNo) {
      channelCache.set(ibmsChannel.channelNo, ibmsChannel)
    }
    if (ibmsChannel.id) {
      channelIdCache.set(ibmsChannel.id, ibmsChannel)
    }
    return {
      id: `channel-${row.id}`,
      name: ibmsChannel.channelName || `通道${ibmsChannel.channelNo}`,
      type: 'channel' as const,
      channelId: row.id,
      channelNo: ibmsChannel.channelNo,
      ibmsChannel
    } as DeviceTreeNode
  })
}

const loadTreeNode = async (node: any, resolve: Function) => {
  try {
    const data = node.data as DeviceTreeNode
    if (!data || data.type !== 'space' || !data.spaceId) {
      resolve([])
      return
    }

    const childSpaces = (spaceTreeChildrenMap.get(data.spaceId) || []).map(mapSpaceNode)
    const channels = await loadChannelsBySpace(data.spaceId)

    resolve([...childSpaces, ...channels])
  } catch (e: any) {
    console.error('[树节点] 加载失败:', e)
    ElMessage.error('加载节点失败')
    resolve([])
  }
}

// ==================== 事件处理 ====================

const handleSearch = () => {
  // TODO: 实现搜索过滤
}

const handleSearchClear = () => {
  searchKeyword.value = ''
}

// 同步所有NVR通道
const handleSyncAllChannels = async () => {
  syncLoading.value = true
  try {
    const result = await syncAllNvrChannels()
    if (result.nvrCount === 0) {
      ElMessage.info('没有找到NVR设备')
    } else {
      ElMessage.success(
        `同步完成：共 ${result.nvrCount} 台NVR，成功 ${result.successCount} 台，耗时 ${result.duration}ms`
      )
      await loadSpaceTree()
    }
  } catch (error) {
    console.error('[同步通道] 失败:', error)
    ElMessage.error('同步通道失败')
  } finally {
    syncLoading.value = false
  }
}

// 树节点拖拽
const handleTreeDragStart = (e: DragEvent, data: DeviceTreeNode) => {
  if (data.type !== 'channel') return
  
  e.dataTransfer!.effectAllowed = 'copy'
  
  // 传递 IBMS 通道数据（用于 ZLM 流媒体播放）
  if (data.ibmsChannel) {
    e.dataTransfer!.setData('ibmsChannel', JSON.stringify(data.ibmsChannel))
    emit('channel-drag-start', e, data.ibmsChannel)
  } else {
    ElMessage.warning('该通道未配置')
  }
}

// 树节点双击
const handleTreeNodeDoubleClick = (data: DeviceTreeNode) => {
  if (data.type !== 'channel') return
  
  if (data.ibmsChannel) {
    emit('channel-play', data.ibmsChannel)
  } else {
    ElMessage.warning('该通道未配置')
  }
}

// ==================== 通道查找 ====================

/**
 * 通过 NVR 通道号查找 IBMS 通道
 * 用于视图加载时恢复播放
 */
const findIbmsChannelByChannelNo = async (channelNo: number | string): Promise<IbmsChannel | null> => {
  const no = typeof channelNo === 'string' ? parseInt(channelNo) : channelNo
  if (isNaN(no)) return null
  
  // 先从缓存查找
  if (channelCache.has(no)) {
    return channelCache.get(no)!
  }
  
  // 缓存未命中，从 API 查询
  try {
    const result = await getIbmsChannelPage({
      pageNo: 1,
      pageSize: MAX_PAGE_SIZE,
      business: 'sa',
      typeCode: 'VT',
      keyword: String(no)
    })
    const page = (result && (result as any).list) ? result : (result as any)?.data
    const channel = page?.list?.find((ch: any) => ch.channelNo === no)
    if (channel) {
      const ibmsChannel = mapChannelRowToIbmsChannel(channel)
      channelCache.set(no, ibmsChannel)
      if (ibmsChannel.id) {
        channelIdCache.set(ibmsChannel.id, ibmsChannel)
      }
      return ibmsChannel
    }
  } catch (e) {
    console.error('[通道查找] 查询失败:', e)
  }
  
  return null
}

const findIbmsChannelById = async (channelId: number | string): Promise<IbmsChannel | null> => {
  const id = typeof channelId === 'string' ? parseInt(channelId) : channelId
  if (isNaN(id)) return null

  if (channelIdCache.has(id)) {
    return channelIdCache.get(id)!
  }

  try {
    const channel = await getIbmsChannel(id)
    if (channel) {
      const ibmsChannel = mapChannelRowToIbmsChannel(channel as any)
      channelIdCache.set(id, ibmsChannel)
      if (ibmsChannel.channelNo) {
        channelCache.set(ibmsChannel.channelNo, ibmsChannel)
      }
      return ibmsChannel
    }
  } catch (e) {
    console.error('[通道查找] 按ID查询失败:', e)
  }

  return null
}

// ==================== 生命周期 ====================

onMounted(async () => {
  loadSpaceTree()
})

const loadBuildingTree = async () => {
  await loadSpaceTree()
}

// 暴露方法给父组件
defineExpose({
  loadSpaceTree,
  loadBuildingTree,
  findIbmsChannelById,
  findIbmsChannelByChannelNo,
  channelCache,
  channelIdCache
})
</script>

<style lang="scss" scoped>
.device-tree-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  :deep(.el-collapse) {
    --el-collapse-border-color: #233754;
    --el-collapse-header-bg-color: transparent;
    --el-collapse-header-text-color: #cfe3ff;
    --el-collapse-content-bg-color: transparent;
    --el-collapse-content-text-color: #b8d4ff;
    border: none;
  }

  :deep(.el-collapse-item__header) {
    background: rgba(21, 36, 58, 0.6);
    border-bottom: 1px solid #233754;
    padding: 0 12px;
    height: 40px;
  }

  :deep(.el-collapse-item__wrap) {
    border-bottom: none;
  }

  :deep(.el-collapse-item__content) {
    padding: 0;
  }

  .panel-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;

    &.disabled {
      color: #666;
    }
  }

  .ptz-tip {
    font-size: 11px;
    color: #666;
    margin-left: 8px;
  }

  .search-box {
    padding: 8px 12px;
    display: flex;
    align-items: center;
    gap: 8px;
    
    .el-input {
      flex: 1;
    }
    
    .sync-btn {
      flex-shrink: 0;
      padding: 6px 8px;
    }
  }

  .device-tree {
    max-height: 300px;
    overflow-y: auto;
    padding: 8px;
    background: transparent !important;
    
    :deep(.el-tree) {
      --el-tree-node-hover-bg-color: rgba(64, 158, 255, 0.15);
      --el-fill-color-light: transparent;
      background: transparent !important;
      color: #b8d4ff;
    }

    :deep(.el-tree-node) {
      // 移除所有焦点相关样式
      outline: none !important;
      
      &:focus {
        outline: none !important;
      }
      
      &:focus-visible {
        outline: none !important;
      }
    }

    :deep(.el-tree-node__content) {
      background: transparent !important;
      outline: none !important;
      
      &:hover {
        background: rgba(64, 158, 255, 0.15) !important;
      }
      
      &:focus,
      &:focus-visible,
      &:focus-within {
        background: transparent !important;
        outline: none !important;
      }
    }

    // 当前选中节点样式
    :deep(.el-tree-node.is-current > .el-tree-node__content) {
      background: transparent !important;
    }

    // 展开节点样式
    :deep(.el-tree-node.is-expanded > .el-tree-node__content) {
      background: transparent !important;
    }

    // 聚焦时样式 - 所有状态组合
    :deep(.el-tree-node:focus > .el-tree-node__content),
    :deep(.el-tree-node.is-focusable:focus > .el-tree-node__content),
    :deep(.el-tree-node.is-focused > .el-tree-node__content) {
      background: transparent !important;
    }

    // 展开箭头颜色
    :deep(.el-tree-node__expand-icon) {
      color: #7a9cc7;
      
      &.is-leaf {
        color: transparent;
      }
    }

    .tree-node {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 4px 0;
      font-size: 13px;
    }
  }
}
</style>
