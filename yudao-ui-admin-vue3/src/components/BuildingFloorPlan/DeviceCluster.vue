<template>
  <div class="device-cluster-popup">
    <div class="cluster-header">
      <el-icon><Location /></el-icon>
      <span>此处有 {{ devices.length }} 个设备</span>
      <el-button text @click="$emit('close')" class="close-btn">
        <el-icon><Close /></el-icon>
      </el-button>
    </div>
    
    <div class="cluster-content">
      <!-- 按图层分组显示 -->
      <div v-for="(layerDevices, layer) in devicesByLayer" :key="layer" class="layer-group">
        <div class="layer-title">
          <el-tag :type="getLayerTagType(layer)" size="small">
            {{ getLayerLabel(layer) }}
          </el-tag>
          <span class="device-count">({{ layerDevices.length }}个)</span>
        </div>
        
        <div class="device-list">
          <div 
            v-for="device in layerDevices" 
            :key="device.id"
            class="device-item"
            @click="handleDeviceClick(device)"
          >
            <div class="device-icon">
              <el-icon :size="20" :color="getStatusColor(device.status)">
                <component :is="getDeviceIcon(device.device_type)" />
              </el-icon>
            </div>
            <div class="device-info">
              <div class="device-name">{{ device.name }}</div>
              <div class="device-meta">
                <el-tag :type="getStatusType(device.status)" size="small">
                  {{ getStatusLabel(device.status) }}
                </el-tag>
                <span class="device-type">{{ device.device_type }}</span>
              </div>
            </div>
            <div class="device-actions">
              <el-button text size="small" @click.stop="viewDevice(device)">
                查看
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="cluster-footer">
      <el-button size="small" @click="viewAll">查看全部设备</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Location, Close, VideoCamera, Lamp, Cpu } from '@element-plus/icons-vue'
import { getDeviceStatusLabel, getDeviceStatusType, getDeviceStatusColor } from '@/utils/iot-dict'

interface Device {
  id: number
  name: string
  device_type: string
  status: string
  z_coordinate: number
  z_layer: string
  [key: string]: any
}

interface Props {
  devices: Device[]
  position: { x: number; y: number }
}

const props = defineProps<Props>()
const emit = defineEmits(['close', 'deviceClick', 'viewAll'])

// 按图层分组设备
const devicesByLayer = computed(() => {
  const groups: Record<string, Device[]> = {}
  props.devices.forEach(device => {
    const layer = device.z_layer || 'wall'
    if (!groups[layer]) {
      groups[layer] = []
    }
    groups[layer].push(device)
  })
  
  // 按图层优先级排序
  const layerOrder = ['ceiling', 'hanging', 'wall', 'ground']
  const sorted: Record<string, Device[]> = {}
  layerOrder.forEach(layer => {
    if (groups[layer]) {
      sorted[layer] = groups[layer].sort((a, b) => b.z_coordinate - a.z_coordinate)
    }
  })
  
  return sorted
})

// 获取图层标签
const getLayerLabel = (layer: string): string => {
  const labels: Record<string, string> = {
    ceiling: '天花板',
    hanging: '吊装',
    wall: '墙面',
    ground: '地面'
  }
  return labels[layer] || layer
}

// 获取图层标签类型
const getLayerTagType = (layer: string) => {
  const types: Record<string, any> = {
    ceiling: 'warning',
    hanging: 'success',
    wall: 'info',
    ground: 'primary'
  }
  return types[layer] || 'info'
}

// 获取设备图标
const getDeviceIcon = (deviceType: string) => {
  const icons: Record<string, any> = {
    '摄像头': VideoCamera,
    'camera': VideoCamera,
    '灯具': Lamp,
    'light': Lamp,
    '传感器': Cpu,
    'sensor': Cpu
  }
  return icons[deviceType] || Cpu
}

// 获取状态标签
const getStatusLabel = (status: string): string => {
  return getDeviceStatusLabel(status)
}

// 获取状态类型
const getStatusType = (status: string) => {
  return getDeviceStatusType(status)
}

// 获取状态颜色
const getStatusColor = (status: string): string => {
  return getDeviceStatusColor(status)
}

// 点击设备
const handleDeviceClick = (device: Device) => {
  emit('deviceClick', device)
}

// 查看设备详情
const viewDevice = (device: Device) => {
  emit('deviceClick', device)
  emit('close')
}

// 查看全部
const viewAll = () => {
  emit('viewAll', props.devices)
  emit('close')
}
</script>

<style scoped lang="scss">
.device-cluster-popup {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  min-width: 320px;
  max-width: 400px;
  max-height: 500px;
  display: flex;
  flex-direction: column;
  
  .cluster-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    border-bottom: 1px solid #EBEEF5;
    background: #F5F7FA;
    border-radius: 8px 8px 0 0;
    
    span {
      flex: 1;
      font-weight: 600;
      color: #303133;
    }
    
    .close-btn {
      padding: 4px;
    }
  }
  
  .cluster-content {
    flex: 1;
    overflow-y: auto;
    padding: 12px;
    
    .layer-group {
      margin-bottom: 16px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .layer-title {
        display: flex;
        align-items: center;
        gap: 6px;
        margin-bottom: 8px;
        font-size: 13px;
        color: #606266;
        
        .device-count {
          color: #909399;
        }
      }
      
      .device-list {
        .device-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 10px;
          border-radius: 6px;
          cursor: pointer;
          transition: all 0.2s;
          border: 1px solid transparent;
          
          &:hover {
            background: #F5F7FA;
            border-color: #DCDFE6;
          }
          
          .device-icon {
            width: 40px;
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #F0F2F5;
            border-radius: 8px;
          }
          
          .device-info {
            flex: 1;
            min-width: 0;
            
            .device-name {
              font-size: 14px;
              font-weight: 500;
              color: #303133;
              margin-bottom: 4px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
            
            .device-meta {
              display: flex;
              align-items: center;
              gap: 8px;
              font-size: 12px;
              
              .device-type {
                color: #909399;
              }
            }
          }
          
          .device-actions {
            opacity: 0;
            transition: opacity 0.2s;
          }
          
          &:hover .device-actions {
            opacity: 1;
          }
        }
      }
    }
  }
  
  .cluster-footer {
    padding: 12px 16px;
    border-top: 1px solid #EBEEF5;
    text-align: center;
  }
}
</style>











