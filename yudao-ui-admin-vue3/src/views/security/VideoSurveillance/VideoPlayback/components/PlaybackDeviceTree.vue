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
        
        <el-button type="primary" size="small" @click="handleSearch" style="width: 100%" :loading="searching">
          搜索录像
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Icon } from '@/components/Icon'
import { getBuildingList } from '@/api/iot/spatial/building'
import { getFloorListByBuildingId } from '@/api/iot/spatial/floor'
import { getChannelPage } from '@/api/iot/channel'
import type { DeviceTreeNode, IbmsChannel } from '../types'

// Emits
const emit = defineEmits<{
  (e: 'search', channels: IbmsChannel[], startTime: string, endTime: string): void
  (e: 'channel-drag-start', event: DragEvent, channel: IbmsChannel): void
  (e: 'channels-change', channels: IbmsChannel[]): void
}>()

// 状态
const searchKeyword = ref('')
const treeRef = ref()
const searching = ref(false)

// 选中的通道
const selectedChannels = ref<IbmsChannel[]>([])

// 建筑树数据
const buildingTreeData = ref<DeviceTreeNode[]>([])

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

// ==================== 建筑树加载 ====================

const loadBuildingTree = async () => {
  try {
    const buildings = await getBuildingList()
    buildingTreeData.value = buildings.map((b: any) => ({
      id: `building-${b.id}`,
      name: b.name,
      type: 'building',
      buildingId: b.id
    }))
  } catch (e: any) {
    console.error('[建筑树] 加载失败:', e)
    ElMessage.error('加载建筑列表失败')
  }
}

const loadTreeNode = async (node: any, resolve: Function) => {
  try {
    const data = node.data as DeviceTreeNode
    let children: DeviceTreeNode[] = []

    if (data.type === 'building') {
      // 只加载楼层
      const floors = await getFloorListByBuildingId(data.buildingId!)
      children = floors.map((f: any) => ({
        id: `floor-${f.id}`,
        name: f.name,
        type: 'floor' as const,
        floorId: f.id,
        buildingId: data.buildingId
      }))
    } else if (data.type === 'floor') {
      // 直接加载该楼层的视频通道
      const params = { pageNo: 1, pageSize: 100, channelType: 'video', floorId: data.floorId }
      const channelsRes = await getChannelPage(params)
      const channels = channelsRes.list || []

      children = channels.map((ch: any) => {
        return {
          id: `channel-${ch.id}`,
          name: ch.channelName || `通道${ch.channelNo}`,
          type: 'channel' as const,
          channelId: ch.id,
          ibmsChannel: ch as IbmsChannel
        }
      })
    }

    resolve(children)
  } catch (e: any) {
    console.error('[树节点] 加载失败:', e)
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

  searching.value = true
  emit('search', selectedChannels.value, filterForm.timeRange[0], filterForm.timeRange[1])
  
  // 搜索完成后重置状态
  setTimeout(() => {
    searching.value = false
  }, 500)
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
  loadBuildingTree()
})

// 暴露方法
defineExpose({
  loadBuildingTree,
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
