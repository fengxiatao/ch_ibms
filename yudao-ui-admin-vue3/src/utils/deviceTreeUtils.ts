/**
 * 设备树选择工具函数
 * 用于处理设备-通道树形结构的级联选择逻辑
 */

/** 树节点数据结构 */
export interface DeviceTreeNode {
  nodeKey: string
  label: string
  isDevice: boolean
  deviceId?: number
  channelId?: number
  children?: DeviceTreeNode[]
}

/** 选中的通道信息 */
export interface SelectedChannelInfo {
  deviceId: number
  channelId: number
}

/**
 * 计算设备节点的选中状态
 * @param device 设备节点
 * @param selectedChannelIds 已选中的通道ID集合
 * @returns 'all' | 'partial' | 'none'
 */
export function getDeviceCheckState(
  device: DeviceTreeNode,
  selectedChannelIds: Set<number>
): 'all' | 'partial' | 'none' {
  if (!device.children || device.children.length === 0) {
    return 'none'
  }
  
  const channelIds = device.children
    .filter((child) => !child.isDevice && child.channelId)
    .map((child) => child.channelId!)
  
  if (channelIds.length === 0) {
    return 'none'
  }
  
  const selectedCount = channelIds.filter((id) => selectedChannelIds.has(id)).length
  
  if (selectedCount === 0) {
    return 'none'
  } else if (selectedCount === channelIds.length) {
    return 'all'
  } else {
    return 'partial'
  }
}

/**
 * 选中设备节点时，获取应该选中的所有通道ID
 * @param device 设备节点
 * @returns 通道ID数组
 */
export function selectDeviceChannels(device: DeviceTreeNode): number[] {
  if (!device.children || device.children.length === 0) {
    return []
  }
  
  return device.children
    .filter((child) => !child.isDevice && child.channelId)
    .map((child) => child.channelId!)
}

/**
 * 取消选中设备节点时，获取应该取消的所有通道ID
 * @param device 设备节点
 * @returns 通道ID数组
 */
export function deselectDeviceChannels(device: DeviceTreeNode): number[] {
  return selectDeviceChannels(device)
}

/**
 * 计算选中通道后的新选中集合（级联选择设备）
 * @param currentSelected 当前选中的通道ID集合
 * @param device 设备节点
 * @param select 是否选中
 * @returns 新的选中通道ID集合
 */
export function cascadeSelectDevice(
  currentSelected: Set<number>,
  device: DeviceTreeNode,
  select: boolean
): Set<number> {
  const newSelected = new Set(currentSelected)
  const channelIds = selectDeviceChannels(device)
  
  if (select) {
    channelIds.forEach((id) => newSelected.add(id))
  } else {
    channelIds.forEach((id) => newSelected.delete(id))
  }
  
  return newSelected
}

/**
 * 计算已选择的通道数量
 * @param treeData 树形数据
 * @param selectedChannelIds 已选中的通道ID集合
 * @returns 选中的通道数量
 */
export function countSelectedChannels(
  treeData: DeviceTreeNode[],
  selectedChannelIds: Set<number>
): number {
  let count = 0
  
  for (const device of treeData) {
    if (device.children) {
      for (const channel of device.children) {
        if (!channel.isDevice && channel.channelId && selectedChannelIds.has(channel.channelId)) {
          count++
        }
      }
    }
  }
  
  return count
}

/**
 * 搜索过滤设备树
 * @param treeData 树形数据
 * @param keyword 搜索关键词
 * @param deviceIpMap 设备ID到IP的映射
 * @returns 过滤后的树形数据
 */
export function filterDeviceTree(
  treeData: DeviceTreeNode[],
  keyword: string,
  deviceIpMap?: Map<number, string>
): DeviceTreeNode[] {
  if (!keyword || keyword.trim() === '') {
    return treeData
  }
  
  const lowerKeyword = keyword.toLowerCase().trim()
  
  return treeData.filter((device) => {
    // 按设备名称匹配
    if (device.label.toLowerCase().includes(lowerKeyword)) {
      return true
    }
    
    // 按设备IP匹配
    if (deviceIpMap && device.deviceId) {
      const ip = deviceIpMap.get(device.deviceId)
      if (ip && ip.toLowerCase().includes(lowerKeyword)) {
        return true
      }
    }
    
    return false
  })
}
