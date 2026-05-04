<template>
  <div class="playback-device-tree">
    <!-- 设备树面板 -->
    <div class="panel-section device-section">
      <div class="section-header">
        <Icon icon="ep:video-camera" />
        <span>视频通道</span>
      </div>
      
      <!-- 搜索框 -->
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索通道名称..."
          clearable
          size="small"
          @clear="handleSearchClear"
        >
          <template #prefix>
            <Icon icon="ep:search" />
          </template>
        </el-input>
      </div>

      <!-- 建筑树 -->
      <el-tree
        ref="treeRef"
        :data="buildingTreeData"
        :props="treeProps"
        lazy
        :load="loadTreeNode"
        accordion
        node-key="id"
        show-checkbox
        :check-strictly="false"
        class="device-tree"
        @check="handleTreeCheck"
      >
        <template #default="{ data }">
          <div
            class="tree-node"
            :class="'node-type-' + data.type"
            :draggable="data.type === 'channel'"
            @dragstart="handleTreeDragStart($event, data)"
          >
            <Icon v-if="data.type === 'space'" icon="ep:office-building" style="color: #409eff" />
            <Icon
              v-else-if="data.type === 'channel'"
              :icon="data.ibmsChannel?.onlineStatus === 1 ? 'ep:video-camera-filled' : 'ep:video-camera'"
              :style="{ color: data.ibmsChannel?.onlineStatus === 1 ? '#67c23a' : '#909399' }"
            />
            <span>{{ data.name }}</span>
          </div>
        </template>
      </el-tree>
    </div>

    <!-- 时间筛选面板 -->
    <div class="panel-section time-section">
      <div class="section-header">
        <Icon icon="ep:calendar" />
        <span>时间筛选</span>
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
            :disabled-date="disabledFutureDate"
          />
        </div>
        
        <el-button
          type="primary"
          size="small"
          @click="handleSearch"
          style="width: 100%"
          :loading="searching"
          :disabled="searching"
        >
          搜索录像
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Icon } from '@/components/Icon'
import { getSpaceTree, type IbmsSpaceTreeNodeRespVO } from '@/api/iot/ibms/space'
import { getChannelPage as getIbmsChannelPage } from '@/api/iot/ibms/channel'
import type { DeviceTreeNode, IbmsChannel } from '../types'

// Emits
const emit = defineEmits<{
  (e: 'search', channels: IbmsChannel[], startTime: string, endTime: string): void
  (e: 'channel-drag-start', event: DragEvent, channel: IbmsChannel): void
  (e: 'channels-change', channels: IbmsChannel[]): void
}>()

// Props（由父组件控制 searching，避免 500ms 误解锁导致重复请求叠加）
const props = defineProps<{
  searching?: boolean
}>()

// 状态
const searchKeyword = ref('')
const treeRef = ref()
const searching = computed(() => props.searching === true)

// 选中的通道
const selectedChannels = ref<IbmsChannel[]>([])

// 空间树数据
const buildingTreeData = ref<DeviceTreeNode[]>([])
const spaceTreeChildrenMap = new Map<number, IbmsSpaceTreeNodeRespVO[]>()
const MAX_PAGE_SIZE = 100

// 筛选表单
const filterForm = reactive({
  timeRange: [] as string[]
})

// 树配置
const treeProps = {
  children: 'children',
  label: 'name',
  isLeaf: (data: DeviceTreeNode) => data.type === 'channel'
}

// 禁用未来日期
const disabledFutureDate = (time: Date) => {
  return time.getTime() > Date.now()
}

const mapStatusToOnlineStatus = (status?: string) => {
  if (status === 'online') return 1
  if (status === 'offline') return 0
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
    channelName: row.name || `通道${row.channelNo ?? 1}`,
    channelNo: row.channelNo ?? 1,
    targetIp: row.ip,
    targetPort: extra.targetPort,
    username: extra.username,
    password: extra.password,
    onlineStatus: mapStatusToOnlineStatus(row.status)
  }
}

const mapSpaceNode = (space: IbmsSpaceTreeNodeRespVO): DeviceTreeNode => ({
  id: `space-${space.id}`,
  name: space.name,
  type: 'space',
  spaceId: space.id
})

const buildSpaceNodeIndex = (nodes: IbmsSpaceTreeNodeRespVO[]) => {
  spaceTreeChildrenMap.clear()
  const walk = (list: IbmsSpaceTreeNodeRespVO[]) => {
    for (const item of list) {
      spaceTreeChildrenMap.set(item.id, item.children || [])
      if (item.children?.length) walk(item.children)
    }
  }
  walk(nodes)
}

const loadSpaceTree = async () => {
  try {
    const spaces = await getSpaceTree()
    buildSpaceNodeIndex(spaces || [])
    buildingTreeData.value = (spaces || []).map(mapSpaceNode)
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
      business: 'sa',
      typeCode: 'VT',
      spaceId
    })
    const page = (res && (res as any).list) ? res : (res as any)?.data
    const list = page?.list || []
    total = Number(page?.total || 0)
    channels.push(...list)
    if (!list.length || channels.length >= total) break
    pageNo += 1
  } while (true)

  return channels.map((row: any) => {
    const ibmsChannel = mapChannelRowToIbmsChannel(row)
    return {
      id: `channel-${row.id}`,
      name: ibmsChannel.channelName,
      type: 'channel' as const,
      channelId: row.id,
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
  if (!filterForm.timeRange || filterForm.timeRange.length !== 2) {
    ElMessage.warning('请选择时间范围')
    return
  }

  if (selectedChannels.value.length === 0) {
    ElMessage.warning('请先选择要查询的通道')
    return
  }
  emit('search', selectedChannels.value, filterForm.timeRange[0], filterForm.timeRange[1])
}

const handleSearchClear = () => {
  searchKeyword.value = ''
}

// 树节点勾选
const handleTreeCheck = () => {
  const checkedNodes = treeRef.value?.getCheckedNodes() || []
  
  // 过滤出通道节点
  const channels: IbmsChannel[] = []
  for (const node of checkedNodes) {
    if (node.type === 'channel' && node.ibmsChannel) {
      channels.push(node.ibmsChannel)
    }
  }
  
  selectedChannels.value = channels
  emit('channels-change', channels)
}

// 树节点拖拽
const handleTreeDragStart = (e: DragEvent, data: DeviceTreeNode) => {
  if (data.type !== 'channel' || !data.ibmsChannel) return
  
  e.dataTransfer!.effectAllowed = 'copy'
  e.dataTransfer!.setData('ibmsChannel', JSON.stringify(data.ibmsChannel))
  emit('channel-drag-start', e, data.ibmsChannel)
}

// ==================== 初始化 ====================

// 初始化默认时间段为当天
const initDefaultTimeRange = () => {
  const d = new Date()
  const start = new Date(d)
  start.setHours(0, 0, 0, 0)
  const end = new Date(d)
  end.setHours(23, 59, 59, 999)
  
  const fmt = (dt: Date) => {
    const y = dt.getFullYear()
    const m = String(dt.getMonth() + 1).padStart(2, '0')
    const day = String(dt.getDate()).padStart(2, '0')
    const hh = String(dt.getHours()).padStart(2, '0')
    const mm = String(dt.getMinutes()).padStart(2, '0')
    const ss = String(dt.getSeconds()).padStart(2, '0')
    return `${y}-${m}-${day} ${hh}:${mm}:${ss}`
  }
  
  filterForm.timeRange = [fmt(start), fmt(end)]
}

onMounted(() => {
  initDefaultTimeRange()
  loadSpaceTree()
})

// 暴露方法
defineExpose({
  loadBuildingTree: loadSpaceTree,
  loadSpaceTree,
  selectedChannels,
  filterForm
})
</script>

<style lang="scss" scoped>
.playback-device-tree {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .panel-section {
    border-bottom: 1px solid #3a3a3a;

    &.device-section {
      flex: 1;
      display: flex;
      flex-direction: column;
      min-height: 0;
      overflow: hidden;
    }

    &.time-section {
      flex-shrink: 0;
    }

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
  }

  .search-box {
    padding: 8px 12px;
  }

  .device-tree {
    flex: 1;
    overflow-y: auto;
    padding: 8px;
    background: transparent;
    
    :deep(.el-tree-node__content) {
      background: transparent;
      
      &:hover {
        background: rgba(64, 158, 255, 0.1);
      }
    }

    .tree-node {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 4px 0;
      font-size: 13px;
      color: #e0e0e0;
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
</style>
