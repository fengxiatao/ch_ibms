<template>
  <div class="realtime-alarm">
    <div class="realtime-alarm__header">
      <div class="realtime-alarm__indicator">
        <span class="realtime-alarm__pulse"></span>
        <span>实时监控中</span>
      </div>
      <el-button text type="primary" size="small" @click="$emit('viewAll')">
        查看全部 <Icon icon="ep:arrow-right" :size="14" />
      </el-button>
    </div>
    
    <div v-if="loading" class="realtime-alarm__loading">
      <el-skeleton :rows="4" animated />
    </div>
    
    <div v-else-if="list.length === 0" class="realtime-alarm__empty">
      <Icon icon="ep:circle-check-filled" :size="48" color="#10b981" />
      <p>系统运行正常，暂无告警</p>
    </div>
    
    <TransitionGroup v-else name="slide" tag="div" class="realtime-alarm__list">
      <div
        v-for="item in list"
        :key="item.id"
        class="alarm-item"
        :class="`alarm-item--${item.level}`"
        @click="$emit('itemClick', item)"
      >
        <div class="alarm-item__level">
          <Icon :icon="getLevelIcon(item.level)" :size="18" />
        </div>
        <div class="alarm-item__content">
          <div class="alarm-item__title">{{ item.title }}</div>
          <div class="alarm-item__meta">
            <span class="alarm-item__device">{{ item.deviceName }}</span>
            <span class="alarm-item__separator">·</span>
            <span class="alarm-item__location">{{ item.location }}</span>
          </div>
        </div>
        <div class="alarm-item__time">{{ formatTime(item.time) }}</div>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup lang="ts">
import { Icon } from '@/components/Icon'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

interface AlarmItem {
  id: string | number
  title: string
  deviceName: string
  location: string
  level: 'critical' | 'warning' | 'info'
  time: string | Date
}

interface Props {
  list: AlarmItem[]
  loading?: boolean
}

defineProps<Props>()

defineEmits<{
  viewAll: []
  itemClick: [item: AlarmItem]
}>()

const getLevelIcon = (level: string) => {
  const icons: Record<string, string> = {
    critical: 'ep:warning-filled',
    warning: 'ep:bell-filled',
    info: 'ep:info-filled'
  }
  return icons[level] || 'ep:info-filled'
}

const formatTime = (time: string | Date) => {
  return dayjs(time).fromNow()
}
</script>

<style lang="scss" scoped>
.realtime-alarm {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
    flex-shrink: 0;
  }
  
  &__indicator {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 11px;
    color: #10b981;
  }
  
  &__pulse {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: #10b981;
    animation: pulse 1.5s ease-in-out infinite;
  }
  
  &__loading,
  &__empty {
    padding: 20px 0;
    text-align: center;
  }
  
  &__empty {
    color: rgba(255, 255, 255, 0.5);
    
    p {
      margin-top: 8px;
      font-size: 12px;
    }
  }
  
  &__list {
    flex: 1;
    overflow-y: auto;
    min-height: 0;
    
    &::-webkit-scrollbar {
      width: 4px;
    }
    
    &::-webkit-scrollbar-track {
      background: transparent;
    }
    
    &::-webkit-scrollbar-thumb {
      background: rgba(0, 180, 255, 0.3);
      border-radius: 2px;
    }
  }
}

.alarm-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px;
  margin-bottom: 6px;
  background: rgba(5, 20, 45, 0.5);
  border: 1px solid rgba(0, 180, 255, 0.15);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    background: rgba(0, 180, 255, 0.08);
    border-color: rgba(0, 180, 255, 0.3);
  }
  
  &--critical {
    border-left: 3px solid #ef4444;
    
    .alarm-item__level { color: #ef4444; }
  }
  
  &--warning {
    border-left: 3px solid #f59e0b;
    
    .alarm-item__level { color: #f59e0b; }
  }
  
  &--info {
    border-left: 3px solid #3b82f6;
    
    .alarm-item__level { color: #3b82f6; }
  }
  
  &__level {
    flex-shrink: 0;
    padding-top: 2px;
  }
  
  &__content {
    flex: 1;
    min-width: 0;
  }
  
  &__title {
    font-size: 12px;
    color: #fff;
    margin-bottom: 2px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  &__meta {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 10px;
    color: rgba(255, 255, 255, 0.5);
  }
  
  &__separator {
    color: rgba(255, 255, 255, 0.3);
  }
  
  &__time {
    flex-shrink: 0;
    font-size: 10px;
    color: rgba(255, 255, 255, 0.4);
  }
}

@keyframes pulse {
  0%, 100% { 
    opacity: 1; 
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.4);
  }
  50% { 
    opacity: 0.7; 
    transform: scale(1.1);
    box-shadow: 0 0 0 6px rgba(16, 185, 129, 0);
  }
}

// 过渡动画
.slide-enter-active {
  animation: slide-in 0.3s ease;
}

.slide-leave-active {
  animation: slide-out 0.3s ease;
}

@keyframes slide-in {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slide-out {
  from {
    opacity: 1;
    transform: translateX(0);
  }
  to {
    opacity: 0;
    transform: translateX(20px);
  }
}
</style>
