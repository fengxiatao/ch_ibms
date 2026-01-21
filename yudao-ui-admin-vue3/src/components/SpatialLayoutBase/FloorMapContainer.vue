<template>
  <div class="floor-map-container">
    <div v-loading="loading" class="map-wrapper">
      <FloorMap
        v-if="floorId"
        :floor-id="floorId"
        :devices="filteredDevices"
        :show-offline-devices="showOfflineDevices"
        @device-click="handleDeviceClick"
      />
      <el-empty v-else description="请选择楼层查看平面图" :image-size="120" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import FloorMap from '@/components/FloorMap/index.vue'

interface Props {
  floorId?: number
  devices?: any[]
  visibleLayers?: string[]
  showOfflineDevices?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  devices: () => [],
  visibleLayers: () => ['device', 'area'],
  showOfflineDevices: true
})

const emit = defineEmits<{
  deviceClick: [device: any]
}>()

const loading = ref(false)

// 根据可见图层过滤设备
const filteredDevices = computed(() => {
  if (!props.floorId) return []
  
  let filtered = props.devices.filter(device => {
    // 根据楼层过滤
    return device.floorId === props.floorId
  })
  
  // 根据可见图层过滤
  if (!props.visibleLayers.includes('device')) {
    filtered = []
  }
  
  return filtered
})

const handleDeviceClick = (device: any) => {
  emit('deviceClick', device)
}
</script>

<style scoped lang="scss">
.floor-map-container {
  width: 100%;
  height: 100%;
  min-height: 500px;
  // 限制最大高度，避免覆盖整个页面
  max-height: calc(100vh - 350px);
  overflow: hidden; // 防止内容溢出

  .map-wrapper {
    width: 100%;
    height: 100%;
    min-height: 500px;
    // 限制最大高度
    max-height: calc(100vh - 350px);
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    overflow: hidden;
    background: #001f3f; // 调整为深蓝色背景
    position: relative; // 确保内部绝对定位元素相对于此容器
    
    // 确保 SVG 不会溢出
    :deep(.floor-map-container) {
      width: 100%;
      height: 100%;
      max-width: 100%;
      max-height: 100%;
      overflow: hidden;
    }
  }
}
</style>

