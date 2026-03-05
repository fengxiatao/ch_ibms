<template>
  <div 
    class="subsystem-card" 
    :class="{ 'subsystem-card--error': hasError }"
    @click="$emit('click')"
  >
    <div class="subsystem-card__icon" :style="{ background: iconBg }">
      <Icon :icon="icon" :size="18" :color="iconColor" />
    </div>
    <div class="subsystem-card__content">
      <div class="subsystem-card__name">{{ name }}</div>
      <div class="subsystem-card__stats">
        <span class="subsystem-card__stat subsystem-card__stat--online">
          <span class="subsystem-card__dot subsystem-card__dot--online"></span>
          {{ onlineCount }} 在线
        </span>
        <span class="subsystem-card__stat subsystem-card__stat--offline">
          <span class="subsystem-card__dot subsystem-card__dot--offline"></span>
          {{ offlineCount }} 离线
        </span>
        <span v-if="errorCount > 0" class="subsystem-card__stat subsystem-card__stat--error">
          <span class="subsystem-card__dot subsystem-card__dot--error"></span>
          {{ errorCount }} 告警
        </span>
      </div>
    </div>
    <div class="subsystem-card__arrow">
      <Icon icon="ep:arrow-right" :size="16" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { Icon } from '@/components/Icon'

interface Props {
  name: string
  icon: string
  iconColor?: string
  iconBg?: string
  onlineCount: number
  offlineCount: number
  errorCount?: number
}

const props = withDefaults(defineProps<Props>(), {
  iconColor: '#00b4ff',
  iconBg: 'rgba(0, 180, 255, 0.15)',
  errorCount: 0
})

defineEmits<{
  click: []
}>()

const hasError = computed(() => props.errorCount > 0)
</script>

<style lang="scss" scoped>
.subsystem-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: linear-gradient(135deg, 
    rgba(10, 30, 60, 0.7) 0%, 
    rgba(5, 20, 45, 0.8) 100%
  );
  border: 1px solid rgba(0, 180, 255, 0.2);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.25s ease;
  
  &:hover {
    background: rgba(0, 180, 255, 0.1);
    border-color: rgba(0, 180, 255, 0.5);
    transform: translateX(4px);
    
    .subsystem-card__arrow {
      transform: translateX(4px);
      color: #00b4ff;
    }
  }
  
  &--error {
    border-left: 3px solid #ef4444;
    background: linear-gradient(135deg, 
      rgba(239, 68, 68, 0.08) 0%, 
      rgba(5, 20, 45, 0.8) 100%
    );
  }
  
  &__icon {
    width: 36px;
    height: 36px;
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
  
  &__name {
    font-size: 13px;
    font-weight: 500;
    color: #fff;
    margin-bottom: 4px;
  }
  
  &__stats {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
  }
  
  &__stat {
    display: flex;
    align-items: center;
    font-size: 11px;
    
    &--online { color: #10b981; }
    &--offline { color: #64748b; }
    &--error { color: #ef4444; }
  }
  
  &__dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    margin-right: 5px;
    
    &--online { 
      background: #10b981;
      box-shadow: 0 0 6px #10b981;
    }
    &--offline { background: #64748b; }
    &--error { 
      background: #ef4444;
      box-shadow: 0 0 6px #ef4444;
      animation: blink 1s ease-in-out infinite;
    }
  }
  
  &__arrow {
    color: rgba(255, 255, 255, 0.3);
    transition: all 0.25s ease;
  }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
</style>
