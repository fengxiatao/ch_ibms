<template>
  <div class="camera-card" :class="{ online: device.status === 1, offline: device.status !== 1 }" @click="handleClick">
    <div class="card-header">
      <div class="device-info">
        <h4 class="device-name">{{ device.deviceName }}</h4>
        <span class="device-location">{{ device.location || device.address || '未知位置' }}</span>
      </div>
      <div class="device-status" :class="statusClass">
        <Icon :icon="statusIcon" />
        <span>{{ statusText }}</span>
      </div>
    </div>
    
    <div class="card-body">
      <div class="thumbnail-container">
        <img 
          v-if="thumbnail" 
          :src="thumbnail" 
          :alt="device.deviceName"
          class="device-thumbnail"
          @error="handleImageError"
        />
        <div v-else class="thumbnail-placeholder">
          <Icon icon="ep:camera" :size="32" />
          <span>{{ device.deviceName }}</span>
        </div>
        
        <!-- 在线时显示播放按钮 -->
        <div v-if="device.status === 1" class="play-overlay" @click.stop="handlePreview">
          <Icon icon="ep:video-play" :size="24" />
        </div>
      </div>
      
      <div class="device-details">
        <div class="detail-item">
          <Icon icon="ep:location" />
          <span>{{ device.ipAddress || 'N/A' }}</span>
        </div>
        <div class="detail-item">
          <Icon icon="ep:timer" />
          <span>{{ lastUpdate }}</span>
        </div>
      </div>
    </div>
    
    <div class="card-footer">
      <el-button size="small" @click.stop="handlePreview" :disabled="device.status !== 1">
        <Icon icon="ep:view" />
        预览
      </el-button>
      <el-button size="small" @click.stop="handleManage">
        <Icon icon="ep:setting" />
        管理
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElButton } from 'element-plus'
import { useRouter } from 'vue-router'

interface DeviceInfo {
  id: number
  deviceName: string
  location?: string
  address?: string
  status: number
  ip?: string
  config?: any
  updateTime?: string
}

interface Props {
  device: DeviceInfo
  cardSize?: 'small' | 'medium' | 'large'
  showThumbnail?: boolean
  clickAction?: 'preview' | 'manage' | 'none'
}

const props = withDefaults(defineProps<Props>(), {
  cardSize: 'medium',
  showThumbnail: true,
  clickAction: 'preview'
})

const emit = defineEmits<{
  click: [device: DeviceInfo]
  preview: [device: DeviceInfo]
  manage: [device: DeviceInfo]
}>()

const router = useRouter()
const thumbnail = ref<string>()

// 设备状态计算属性
const statusClass = computed(() => {
  return props.device.status === 1 ? 'online' : 'offline'
})

const statusIcon = computed(() => {
  return props.device.status === 1 ? 'ep:video-camera' : 'ep:video-camera-filled'
})

const statusText = computed(() => {
  return props.device.status === 1 ? '在线' : '离线'
})

const lastUpdate = computed(() => {
  if (props.device.updateTime) {
    return new Date(props.device.updateTime).toLocaleString()
  }
  return '未知'
})

// 生成设备缩略图
const generateThumbnail = () => {
  const deviceName = props.device.deviceName
  const svg = `<svg width="200" height="120" viewBox="0 0 200 120" fill="none" xmlns="http://www.w3.org/2000/svg">
    <rect width="200" height="120" fill="#1a1a1a"/>
    <circle cx="100" cy="60" r="20" fill="#333"/>
    <text x="100" y="95" fill="#666" text-anchor="middle" font-size="12">${deviceName}</text>
  </svg>`
  
  const blob = new Blob([svg], { type: 'image/svg+xml' })
  thumbnail.value = URL.createObjectURL(blob)
}

// 处理图片加载错误
const handleImageError = () => {
  generateThumbnail()
}

// 事件处理
const handleClick = () => {
  if (props.clickAction === 'preview') {
    handlePreview()
  } else if (props.clickAction === 'manage') {
    handleManage()
  }
  emit('click', props.device)
}

const handlePreview = () => {
  emit('preview', props.device)
  router.push('/security/VideoSurveillance/MultiScreenPreview')
}

const handleManage = () => {
  emit('manage', props.device)
  router.push(`/iot/device/device?deviceId=${props.device.id}`)
}

// 初始化缩略图
generateThumbnail()
</script>

<style lang="scss" scoped>
.camera-card {
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    background: rgba(255, 255, 255, 0.05);
    border-color: rgba(255, 255, 255, 0.2);
    transform: translateY(-2px);
  }
  
  &.online {
    border-left: 3px solid #00d4ff;
  }
  
  &.offline {
    border-left: 3px solid #666;
    opacity: 0.7;
  }
}

.card-header {
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  
  .device-info {
    flex: 1;
    
    .device-name {
      margin: 0 0 4px 0;
      color: #ffffff;
      font-size: 14px;
      font-weight: 600;
    }
    
    .device-location {
      color: #999;
      font-size: 12px;
    }
  }
  
  .device-status {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    padding: 2px 8px;
    border-radius: 4px;
    
    &.online {
      color: #00d4ff;
      background: rgba(0, 212, 255, 0.1);
    }
    
    &.offline {
      color: #666;
      background: rgba(102, 102, 102, 0.1);
    }
  }
}

.card-body {
  padding: 12px;
  
  .thumbnail-container {
    position: relative;
    width: 100%;
    height: 120px;
    margin-bottom: 12px;
    border-radius: 4px;
    overflow: hidden;
    
    .device-thumbnail {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .thumbnail-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: #1a1a1a;
      color: #666;
      
      span {
        margin-top: 8px;
        font-size: 12px;
      }
    }
    
    .play-overlay {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 40px;
      height: 40px;
      background: rgba(0, 212, 255, 0.8);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #ffffff;
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        background: rgba(0, 212, 255, 1);
        transform: translate(-50%, -50%) scale(1.1);
      }
    }
  }
  
  .device-details {
    display: flex;
    flex-direction: column;
    gap: 6px;
    
    .detail-item {
      display: flex;
      align-items: center;
      gap: 6px;
      color: #999;
      font-size: 12px;
    }
  }
}

.card-footer {
  padding: 8px 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  gap: 8px;
  
  .el-button {
    flex: 1;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    color: #ffffff;
    
    &:hover {
      background: rgba(255, 255, 255, 0.1);
      border-color: rgba(255, 255, 255, 0.2);
    }
    
    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}
</style>





















