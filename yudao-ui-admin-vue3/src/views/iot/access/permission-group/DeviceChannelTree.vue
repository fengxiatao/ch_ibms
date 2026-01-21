<template>
  <div class="device-channel-tree">
    <!-- 搜索框 -->
    <div class="search-box">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索设备名称或IP"
        clearable
        :prefix-icon="Search"
        @input="handleSearch"
      />
    </div>
    
    <!-- 设备树 -->
    <div class="tree-container">
      <el-tree
        ref="treeRef"
        :data="filteredTreeData"
        :props="treeProps"
        node-key="nodeKey"
        show-checkbox
        default-expand-all
        :check-strictly="false"
        @check="handleCheck"
        @check-change="handleCheckChange"
      >
        <template #default="{ data }">
          <span class="tree-node">
            <Icon :icon="data.isDevice ? 'ep:monitor' : 'ep:key'" class="node-icon" />
            <span class="node-label">{{ data.label }}</span>
            <el-tag v-if="data.isDevice" size="small" :type="data.state === 1 ? 'success' : 'info'" class="node-tag">
              {{ data.state === 1 ? '在线' : '离线' }}
            </el-tag>
          </span>
        </template>
      </el-tree>
      
      <el-empty v-if="filteredTreeData.length === 0" description="暂无设备数据" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import type { ElTree } from 'element-plus'
import type { AccessDeviceVO, AccessChannelVO } from '@/api/iot/access'
import { AccessDeviceApi, AccessChannelApi } from '@/api/iot/access'

/** 树节点数据结构 */
export interface TreeNode {
  nodeKey: string
  label: string
  isDevice: boolean
  deviceId?: number
  deviceIp?: string
  channelId?: number
  channelNo?: number
  state?: number
  children?: TreeNode[]
}

/** 选中的通道信息 */
export interface SelectedChannel {
  deviceId: number
  deviceName: string
  deviceIp: string
  channelId: number
  channelNo: number
  channelName: string
}

const props = defineProps<{
  modelValue: SelectedChannel[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: SelectedChannel[]): void
}>()

const treeRef = ref<InstanceType<typeof ElTree>>()
const searchKeyword = ref('')
const deviceList = ref<AccessDeviceVO[]>([])
const channelMap = ref<Map<number, AccessChannelVO[]>>(new Map())
const treeData = ref<TreeNode[]>([])

const treeProps = {
  children: 'children',
  label: 'label'
}


/** 过滤后的树数据 */
const filteredTreeData = computed(() => {
  if (!searchKeyword.value) {
    return treeData.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return treeData.value.filter((device) => {
    return (
      device.label.toLowerCase().includes(keyword) ||
      (device.deviceIp && device.deviceIp.toLowerCase().includes(keyword))
    )
  })
})

/** 加载设备列表 */
const loadDevices = async () => {
  try {
    const res = await AccessDeviceApi.getDeviceList()
    deviceList.value = res || []
    await loadAllChannels()
    buildTreeData()
  } catch (error) {
    console.error('加载设备列表失败:', error)
  }
}

/** 加载所有设备的通道 */
const loadAllChannels = async () => {
  channelMap.value.clear()
  for (const device of deviceList.value) {
    try {
      const channels = await AccessChannelApi.getChannelsByDevice(device.id)
      channelMap.value.set(device.id, channels || [])
    } catch (error) {
      console.error(`加载设备 ${device.id} 通道失败:`, error)
      channelMap.value.set(device.id, [])
    }
  }
}

/** 构建树形数据 */
const buildTreeData = () => {
  treeData.value = deviceList.value.map((device) => {
    const channels = channelMap.value.get(device.id) || []
    return {
      nodeKey: `device-${device.id}`,
      label: device.deviceName || device.ipAddress,
      isDevice: true,
      deviceId: device.id,
      deviceIp: device.ipAddress,
      state: device.state,
      children: channels.map((channel) => ({
        nodeKey: `channel-${channel.id}`,
        label: channel.channelName || `通道${channel.channelNo}`,
        isDevice: false,
        deviceId: device.id,
        deviceIp: device.ipAddress,
        channelId: channel.id,
        channelNo: channel.channelNo
      }))
    }
  })
}

/** 处理搜索 */
const handleSearch = () => {
  // 搜索时自动展开匹配的节点
}

/** 处理选中变化 */
const handleCheck = (data: TreeNode, checked: { checkedKeys: string[]; checkedNodes: TreeNode[] }) => {
  updateSelectedChannels(checked.checkedNodes)
}

const handleCheckChange = () => {
  // 获取当前选中的节点
  const checkedNodes = treeRef.value?.getCheckedNodes(false, false) as TreeNode[]
  updateSelectedChannels(checkedNodes || [])
}

/** 更新选中的通道列表 */
const updateSelectedChannels = (checkedNodes: TreeNode[]) => {
  const selectedChannels: SelectedChannel[] = []
  
  for (const node of checkedNodes) {
    if (!node.isDevice && node.channelId) {
      // 找到对应的设备信息
      const device = deviceList.value.find((d) => d.id === node.deviceId)
      if (device) {
        selectedChannels.push({
          deviceId: node.deviceId!,
          deviceName: device.deviceName || device.ipAddress,
          deviceIp: device.ipAddress,
          channelId: node.channelId,
          channelNo: node.channelNo!,
          channelName: node.label
        })
      }
    }
  }
  
  emit('update:modelValue', selectedChannels)
}

/** 根据外部值设置选中状态 */
const setCheckedByValue = () => {
  if (!treeRef.value) return
  
  const checkedKeys: string[] = []
  for (const item of props.modelValue) {
    if (item.channelId) {
      // 有channelId，直接使用
      checkedKeys.push(`channel-${item.channelId}`)
    } else if (item.deviceId) {
      // 没有channelId但有deviceId，选中该设备下的所有通道（兼容历史数据）
      const channels = channelMap.value.get(item.deviceId) || []
      for (const channel of channels) {
        checkedKeys.push(`channel-${channel.id}`)
      }
    }
  }
  treeRef.value.setCheckedKeys(checkedKeys)
}

/** 暴露方法供外部调用 */
defineExpose({
  refresh: loadDevices,
  setCheckedByValue
})

// 监听外部值变化
watch(
  () => props.modelValue,
  () => {
    setCheckedByValue()
  },
  { deep: true }
)

onMounted(() => {
  loadDevices()
})
</script>

<style scoped lang="scss">
.device-channel-tree {
  display: flex;
  flex-direction: column;
  height: 100%;
  
  .search-box {
    margin-bottom: 12px;
  }
  
  .tree-container {
    flex: 1;
    overflow-y: auto;
    border: 1px solid var(--el-border-color-light);
    border-radius: 4px;
    padding: 8px;
    
    .tree-node {
      display: flex;
      align-items: center;
      
      .node-icon {
        margin-right: 6px;
        color: var(--el-color-primary);
      }
      
      .node-label {
        flex: 1;
      }
      
      .node-tag {
        margin-left: 8px;
      }
    }
  }
}
</style>
