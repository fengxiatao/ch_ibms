<template>
  <div 
    class="stat-card"
    :class="[`stat-card--${theme}`, clickable && 'stat-card--clickable']"
    @click="handleClick"
  >
    <div class="stat-card__border stat-card__border--top-left"></div>
    <div class="stat-card__border stat-card__border--top-right"></div>
    <div class="stat-card__border stat-card__border--bottom-left"></div>
    <div class="stat-card__border stat-card__border--bottom-right"></div>
    
    <div class="stat-card__icon" :style="{ background: iconBg }">
      <Icon :icon="icon" :size="20" :color="iconColor" />
    </div>
    
    <div class="stat-card__content">
      <div class="stat-card__label">{{ label }}</div>
      <div class="stat-card__value">
        <CountTo 
          :start-val="0" 
          :end-val="value" 
          :duration="1500" 
          :decimals="decimals"
        />
        <span v-if="unit" class="stat-card__unit">{{ unit }}</span>
      </div>
      <div v-if="subLabel" class="stat-card__sub">
        <span class="stat-card__sub-label">{{ subLabel }}:</span>
        <span class="stat-card__sub-value" :class="subValueClass">
          {{ subValuePrefix }}{{ subValue }}
        </span>
      </div>
    </div>
    
    <div v-if="trend !== undefined" class="stat-card__trend" :class="trendClass">
      <Icon :icon="trendIcon" :size="16" />
      <span>{{ Math.abs(trend) }}%</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Icon } from '@/components/Icon'
import { CountTo } from '@/components/CountTo'

interface Props {
  label: string
  value: number
  unit?: string
  icon: string
  iconColor?: string
  iconBg?: string
  theme?: 'blue' | 'green' | 'orange' | 'red' | 'purple'
  decimals?: number
  subLabel?: string
  subValue?: number | string
  subValuePrefix?: string
  trend?: number
  clickable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  iconColor: '#00b4ff',
  iconBg: 'rgba(0, 180, 255, 0.15)',
  theme: 'blue',
  decimals: 0,
  subValuePrefix: '',
  clickable: false
})

const emit = defineEmits<{
  click: []
}>()

const trendClass = computed(() => {
  if (props.trend === undefined) return ''
  return props.trend >= 0 ? 'stat-card__trend--up' : 'stat-card__trend--down'
})

const trendIcon = computed(() => {
  if (props.trend === undefined) return ''
  return props.trend >= 0 ? 'ep:caret-top' : 'ep:caret-bottom'
})

const subValueClass = computed(() => {
  if (typeof props.subValue === 'number') {
    return props.subValue > 0 ? 'stat-card__sub-value--positive' : ''
  }
  return ''
})

const handleClick = () => {
  if (props.clickable) {
    emit('click')
  }
}
</script>

<style lang="scss" scoped>
.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: linear-gradient(135deg, 
    rgba(10, 30, 60, 0.9) 0%, 
    rgba(5, 20, 45, 0.95) 100%
  );
  border: 1px solid rgba(0, 180, 255, 0.2);
  border-radius: 4px;
  transition: all 0.3s ease;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: linear-gradient(90deg, transparent, var(--card-color, #00b4ff), transparent);
    opacity: 0.6;
  }
  
  &:hover {
    border-color: var(--card-color, #00b4ff);
    box-shadow: 0 0 20px rgba(0, 180, 255, 0.2);
    transform: translateY(-2px);
  }
  
  &--clickable {
    cursor: pointer;
  }
  
  // 角标装饰
  &__border {
    position: absolute;
    width: 12px;
    height: 12px;
    
    &--top-left {
      top: 0;
      left: 0;
      border-top: 2px solid var(--card-color, #00b4ff);
      border-left: 2px solid var(--card-color, #00b4ff);
    }
    
    &--top-right {
      top: 0;
      right: 0;
      border-top: 2px solid var(--card-color, #00b4ff);
      border-right: 2px solid var(--card-color, #00b4ff);
    }
    
    &--bottom-left {
      bottom: 0;
      left: 0;
      border-bottom: 2px solid var(--card-color, #00b4ff);
      border-left: 2px solid var(--card-color, #00b4ff);
    }
    
    &--bottom-right {
      bottom: 0;
      right: 0;
      border-bottom: 2px solid var(--card-color, #00b4ff);
      border-right: 2px solid var(--card-color, #00b4ff);
    }
  }
  
  // 主题色
  &--blue { --card-color: #00b4ff; }
  &--green { --card-color: #10b981; }
  &--orange { --card-color: #f59e0b; }
  &--red { --card-color: #ef4444; }
  &--purple { --card-color: #8b5cf6; }
  
  &__icon {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }
  
  &__content {
    flex: 1;
    min-width: 0;
  }
  
  &__label {
    font-size: 11px;
    color: rgba(255, 255, 255, 0.6);
    margin-bottom: 4px;
  }
  
  &__value {
    font-size: 20px;
    font-weight: 700;
    color: #fff;
    line-height: 1.2;
  }
  
  &__unit {
    font-size: 11px;
    font-weight: 400;
    color: rgba(255, 255, 255, 0.5);
    margin-left: 2px;
  }
  
  &__sub {
    margin-top: 4px;
    font-size: 10px;
    
    &-label {
      color: rgba(255, 255, 255, 0.5);
    }
    
    &-value {
      color: #fff;
      margin-left: 2px;
      
      &--positive {
        color: #10b981;
      }
    }
  }
  
  &__trend {
    position: absolute;
    top: 8px;
    right: 8px;
    display: flex;
    align-items: center;
    gap: 2px;
    font-size: 10px;
    padding: 2px 6px;
    border-radius: 3px;
    
    &--up {
      color: #10b981;
      background: rgba(16, 185, 129, 0.15);
    }
    
    &--down {
      color: #ef4444;
      background: rgba(239, 68, 68, 0.15);
    }
  }
}
</style>
