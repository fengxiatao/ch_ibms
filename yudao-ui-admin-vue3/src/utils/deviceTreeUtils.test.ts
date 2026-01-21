/**
 * 设备树选择工具函数属性测试
 * **Feature: permission-group-ui-redesign**
 * **Property 2: 设备节点级联选择**
 * **Property 3: 设备节点级联取消**
 * **Property 4: 半选状态计算**
 * **Property 5: 已选择数量一致性**
 * **Property 6: 搜索过滤正确性**
 * **Validates: Requirements 3.3, 3.4, 3.5, 4.1, 3.6**
 */
import { describe, it, expect } from 'vitest'
import * as fc from 'fast-check'
import {
  DeviceTreeNode,
  getDeviceCheckState,
  selectDeviceChannels,
  deselectDeviceChannels,
  cascadeSelectDevice,
  countSelectedChannels,
  filterDeviceTree
} from './deviceTreeUtils'

// 生成设备树节点的 Arbitrary
const channelNodeArb = fc.record({
  nodeKey: fc.string({ minLength: 1, maxLength: 20 }).map((s) => `channel-${s}`),
  label: fc.string({ minLength: 1, maxLength: 50 }),
  isDevice: fc.constant(false),
  channelId: fc.integer({ min: 1, max: 10000 })
})

const deviceNodeArb = fc.record({
  nodeKey: fc.string({ minLength: 1, maxLength: 20 }).map((s) => `device-${s}`),
  label: fc.string({ minLength: 1, maxLength: 50 }),
  isDevice: fc.constant(true),
  deviceId: fc.integer({ min: 1, max: 10000 }),
  children: fc.array(channelNodeArb, { minLength: 0, maxLength: 10 })
})

const treeDataArb = fc.array(deviceNodeArb, { minLength: 0, maxLength: 5 })

describe('deviceTreeUtils', () => {
  describe('getDeviceCheckState', () => {
    it('should return "none" for device with no children', () => {
      const device: DeviceTreeNode = {
        nodeKey: 'device-1',
        label: 'Device 1',
        isDevice: true,
        deviceId: 1,
        children: []
      }
      expect(getDeviceCheckState(device, new Set())).toBe('none')
    })

    it('should return "all" when all channels are selected', () => {
      const device: DeviceTreeNode = {
        nodeKey: 'device-1',
        label: 'Device 1',
        isDevice: true,
        deviceId: 1,
        children: [
          { nodeKey: 'channel-1', label: 'Channel 1', isDevice: false, channelId: 1 },
          { nodeKey: 'channel-2', label: 'Channel 2', isDevice: false, channelId: 2 }
        ]
      }
      expect(getDeviceCheckState(device, new Set([1, 2]))).toBe('all')
    })

    it('should return "partial" when some channels are selected', () => {
      const device: DeviceTreeNode = {
        nodeKey: 'device-1',
        label: 'Device 1',
        isDevice: true,
        deviceId: 1,
        children: [
          { nodeKey: 'channel-1', label: 'Channel 1', isDevice: false, channelId: 1 },
          { nodeKey: 'channel-2', label: 'Channel 2', isDevice: false, channelId: 2 }
        ]
      }
      expect(getDeviceCheckState(device, new Set([1]))).toBe('partial')
    })

    it('should return "none" when no channels are selected', () => {
      const device: DeviceTreeNode = {
        nodeKey: 'device-1',
        label: 'Device 1',
        isDevice: true,
        deviceId: 1,
        children: [
          { nodeKey: 'channel-1', label: 'Channel 1', isDevice: false, channelId: 1 },
          { nodeKey: 'channel-2', label: 'Channel 2', isDevice: false, channelId: 2 }
        ]
      }
      expect(getDeviceCheckState(device, new Set())).toBe('none')
    })
  })

  describe('selectDeviceChannels', () => {
    it('should return all channel IDs for a device', () => {
      const device: DeviceTreeNode = {
        nodeKey: 'device-1',
        label: 'Device 1',
        isDevice: true,
        deviceId: 1,
        children: [
          { nodeKey: 'channel-1', label: 'Channel 1', isDevice: false, channelId: 1 },
          { nodeKey: 'channel-2', label: 'Channel 2', isDevice: false, channelId: 2 }
        ]
      }
      expect(selectDeviceChannels(device)).toEqual([1, 2])
    })

    it('should return empty array for device with no children', () => {
      const device: DeviceTreeNode = {
        nodeKey: 'device-1',
        label: 'Device 1',
        isDevice: true,
        deviceId: 1
      }
      expect(selectDeviceChannels(device)).toEqual([])
    })
  })

  describe('cascadeSelectDevice', () => {
    it('should add all channel IDs when selecting device', () => {
      const device: DeviceTreeNode = {
        nodeKey: 'device-1',
        label: 'Device 1',
        isDevice: true,
        deviceId: 1,
        children: [
          { nodeKey: 'channel-1', label: 'Channel 1', isDevice: false, channelId: 1 },
          { nodeKey: 'channel-2', label: 'Channel 2', isDevice: false, channelId: 2 }
        ]
      }
      const result = cascadeSelectDevice(new Set(), device, true)
      expect(result).toEqual(new Set([1, 2]))
    })

    it('should remove all channel IDs when deselecting device', () => {
      const device: DeviceTreeNode = {
        nodeKey: 'device-1',
        label: 'Device 1',
        isDevice: true,
        deviceId: 1,
        children: [
          { nodeKey: 'channel-1', label: 'Channel 1', isDevice: false, channelId: 1 },
          { nodeKey: 'channel-2', label: 'Channel 2', isDevice: false, channelId: 2 }
        ]
      }
      const result = cascadeSelectDevice(new Set([1, 2, 3]), device, false)
      expect(result).toEqual(new Set([3]))
    })
  })

  describe('countSelectedChannels', () => {
    it('should count selected channels correctly', () => {
      const treeData: DeviceTreeNode[] = [
        {
          nodeKey: 'device-1',
          label: 'Device 1',
          isDevice: true,
          deviceId: 1,
          children: [
            { nodeKey: 'channel-1', label: 'Channel 1', isDevice: false, channelId: 1 },
            { nodeKey: 'channel-2', label: 'Channel 2', isDevice: false, channelId: 2 }
          ]
        },
        {
          nodeKey: 'device-2',
          label: 'Device 2',
          isDevice: true,
          deviceId: 2,
          children: [
            { nodeKey: 'channel-3', label: 'Channel 3', isDevice: false, channelId: 3 }
          ]
        }
      ]
      expect(countSelectedChannels(treeData, new Set([1, 3]))).toBe(2)
    })
  })

  describe('filterDeviceTree', () => {
    it('should return all devices when keyword is empty', () => {
      const treeData: DeviceTreeNode[] = [
        { nodeKey: 'device-1', label: 'Device 1', isDevice: true, deviceId: 1 },
        { nodeKey: 'device-2', label: 'Device 2', isDevice: true, deviceId: 2 }
      ]
      expect(filterDeviceTree(treeData, '')).toEqual(treeData)
    })

    it('should filter by device name', () => {
      const treeData: DeviceTreeNode[] = [
        { nodeKey: 'device-1', label: '公司大门', isDevice: true, deviceId: 1 },
        { nodeKey: 'device-2', label: '后门', isDevice: true, deviceId: 2 }
      ]
      expect(filterDeviceTree(treeData, '大门')).toEqual([treeData[0]])
    })

    it('should filter by device IP', () => {
      const treeData: DeviceTreeNode[] = [
        { nodeKey: 'device-1', label: 'Device 1', isDevice: true, deviceId: 1 },
        { nodeKey: 'device-2', label: 'Device 2', isDevice: true, deviceId: 2 }
      ]
      const ipMap = new Map([[1, '192.168.1.1'], [2, '192.168.1.2']])
      expect(filterDeviceTree(treeData, '192.168.1.1', ipMap)).toEqual([treeData[0]])
    })
  })


  /**
   * Property-Based Tests
   */
  describe('Property 2: 设备节点级联选择', () => {
    /**
     * **Feature: permission-group-ui-redesign, Property 2: 设备节点级联选择**
     * **Validates: Requirements 3.3**
     * *For any* 设备树结构，当选中一个设备节点时，该设备下的所有通道节点都应该被选中
     */
    it('selecting a device should select all its channels', () => {
      fc.assert(
        fc.property(deviceNodeArb, (device) => {
          const result = cascadeSelectDevice(new Set(), device, true)
          const expectedChannelIds = (device.children || [])
            .filter((c) => !c.isDevice && c.channelId)
            .map((c) => c.channelId!)
          
          // 所有通道都应该被选中
          for (const channelId of expectedChannelIds) {
            expect(result.has(channelId)).toBe(true)
          }
        }),
        { numRuns: 100 }
      )
    })
  })

  describe('Property 3: 设备节点级联取消', () => {
    /**
     * **Feature: permission-group-ui-redesign, Property 3: 设备节点级联取消**
     * **Validates: Requirements 3.4**
     * *For any* 设备树结构，当取消选中一个设备节点时，该设备下的所有通道节点都应该被取消选中
     */
    it('deselecting a device should deselect all its channels', () => {
      fc.assert(
        fc.property(deviceNodeArb, (device) => {
          const channelIds = (device.children || [])
            .filter((c) => !c.isDevice && c.channelId)
            .map((c) => c.channelId!)
          
          // 先选中所有通道
          const initialSelected = new Set(channelIds)
          
          // 取消选中设备
          const result = cascadeSelectDevice(initialSelected, device, false)
          
          // 所有通道都应该被取消选中
          for (const channelId of channelIds) {
            expect(result.has(channelId)).toBe(false)
          }
        }),
        { numRuns: 100 }
      )
    })
  })

  describe('Property 4: 半选状态计算', () => {
    /**
     * **Feature: permission-group-ui-redesign, Property 4: 半选状态计算**
     * **Validates: Requirements 3.5**
     * *For any* 设备节点，如果其部分（但非全部）子通道被选中，则该设备节点应显示为半选状态
     */
    it('partial selection should result in partial state', () => {
      fc.assert(
        fc.property(
          deviceNodeArb.filter((d) => d.children && d.children.length >= 2),
          (device) => {
            const channelIds = (device.children || [])
              .filter((c) => !c.isDevice && c.channelId)
              .map((c) => c.channelId!)
            
            if (channelIds.length < 2) return true // 跳过通道数不足的情况
            
            // 只选中第一个通道
            const partialSelected = new Set([channelIds[0]])
            const state = getDeviceCheckState(device, partialSelected)
            
            expect(state).toBe('partial')
            return true
          }
        ),
        { numRuns: 100 }
      )
    })

    it('all selected should result in all state', () => {
      fc.assert(
        fc.property(
          deviceNodeArb.filter((d) => d.children && d.children.length >= 1),
          (device) => {
            const channelIds = (device.children || [])
              .filter((c) => !c.isDevice && c.channelId)
              .map((c) => c.channelId!)
            
            if (channelIds.length === 0) return true
            
            const allSelected = new Set(channelIds)
            const state = getDeviceCheckState(device, allSelected)
            
            expect(state).toBe('all')
            return true
          }
        ),
        { numRuns: 100 }
      )
    })

    it('none selected should result in none state', () => {
      fc.assert(
        fc.property(deviceNodeArb, (device) => {
          const state = getDeviceCheckState(device, new Set())
          
          const hasChannels = (device.children || []).some((c) => !c.isDevice && c.channelId)
          if (!hasChannels) {
            expect(state).toBe('none')
          } else {
            expect(state).toBe('none')
          }
          return true
        }),
        { numRuns: 100 }
      )
    })
  })

  describe('Property 5: 已选择数量一致性', () => {
    /**
     * **Feature: permission-group-ui-redesign, Property 5: 已选择数量一致性**
     * **Validates: Requirements 4.1**
     * *For any* 设备树选择状态，已选择区域显示的数量应该等于实际被选中的通道数量
     */
    it('count should match actual selected channels', () => {
      fc.assert(
        fc.property(treeDataArb, fc.array(fc.integer({ min: 1, max: 10000 }), { maxLength: 20 }), (treeData, selectedIds) => {
          const selectedSet = new Set(selectedIds)
          const count = countSelectedChannels(treeData, selectedSet)
          
          // 手动计算实际选中的通道数
          let actualCount = 0
          for (const device of treeData) {
            if (device.children) {
              for (const channel of device.children) {
                if (!channel.isDevice && channel.channelId && selectedSet.has(channel.channelId)) {
                  actualCount++
                }
              }
            }
          }
          
          expect(count).toBe(actualCount)
        }),
        { numRuns: 100 }
      )
    })
  })

  describe('Property 6: 搜索过滤正确性', () => {
    /**
     * **Feature: permission-group-ui-redesign, Property 6: 搜索过滤正确性**
     * **Validates: Requirements 3.6**
     * *For any* 搜索关键词，过滤后的设备列表应该只包含名称或IP包含该关键词的设备
     */
    it('filtered results should only contain matching devices', () => {
      // 生成非空白字符的关键词
      const nonWhitespaceKeywordArb = fc.string({ minLength: 1, maxLength: 10 })
        .filter(s => s.trim().length > 0)
      
      fc.assert(
        fc.property(treeDataArb, nonWhitespaceKeywordArb, (treeData, keyword) => {
          const filtered = filterDeviceTree(treeData, keyword)
          const lowerKeyword = keyword.toLowerCase().trim()
          
          // 所有过滤结果都应该包含关键词（按 label 匹配）
          for (const device of filtered) {
            const matches = device.label.toLowerCase().includes(lowerKeyword)
            expect(matches).toBe(true)
          }
        }),
        { numRuns: 100 }
      )
    })

    it('empty keyword should return all devices', () => {
      fc.assert(
        fc.property(treeDataArb, (treeData) => {
          const filtered = filterDeviceTree(treeData, '')
          expect(filtered).toEqual(treeData)
        }),
        { numRuns: 100 }
      )
    })
  })
})
