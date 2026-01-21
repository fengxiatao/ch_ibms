<template>
  <div class="selected-device-list">
    <!-- 标题和数量统计 -->
    <div class="list-header">
      <span class="title">已选择</span>
      <el-tag type="primary" size="small">{{ selectedItems.length }}</el-tag>
    </div>
    
    <!-- 已选择列表 -->
    <div class="list-container">
      <div
        v-for="item in selectedItems"
        :key="`${item.deviceId}-${item.channelId}`"
        class="list-item"
        @click="handleRemove(item)"
      >
        <div class="item-content">
          <span class="device-ip">{{ item.deviceIp }}</span>
          <span class="separator">-</span>
          <span class="channel-name">{{ item.channelName }}</span>
        </div>
        <Icon icon="ep:close" class="remove-icon" />
      </div>
      
      <el-empty v-if="selectedItems.length === 0" description="暂无选择" :image-size="60" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

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
  (e: 'remove', item: SelectedChannel): void
}>()

const selectedItems = computed(() => props.modelValue || [])

/** 移除选中项 */
const handleRemove = (item: SelectedChannel) => {
  const newValue = props.modelValue.filter(
    (i) => !(i.deviceId === item.deviceId && i.channelId === item.channelId)
  )
  emit('update:modelValue', newValue)
  emit('remove', item)
}
</script>

<style scoped lang="scss">
.selected-device-list {
  display: flex;
  flex-direction: column;
  height: 100%;
  
  .list-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
    
    .title {
      font-weight: 500;
      color: var(--el-text-color-primary);
    }
  }
  
  .list-container {
    flex: 1;
    overflow-y: auto;
    border: 1px solid var(--el-border-color-light);
    border-radius: 4px;
    padding: 8px;
    
    .list-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 8px 12px;
      margin-bottom: 4px;
      background: var(--el-fill-color-light);
      border-radius: 4px;
      cursor: pointer;
      transition: all 0.2s;
      
      &:hover {
        background: var(--el-fill-color);
        
        .remove-icon {
          opacity: 1;
        }
      }
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .item-content {
        display: flex;
        align-items: center;
        flex: 1;
        overflow: hidden;
        
        .device-ip {
          color: var(--el-color-primary);
          font-family: monospace;
        }
        
        .separator {
          margin: 0 6px;
          color: var(--el-text-color-secondary);
        }
        
        .channel-name {
          color: var(--el-text-color-regular);
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
      
      .remove-icon {
        color: var(--el-text-color-secondary);
        opacity: 0;
        transition: opacity 0.2s;
        
        &:hover {
          color: var(--el-color-danger);
        }
      }
    }
  }
}
</style>
