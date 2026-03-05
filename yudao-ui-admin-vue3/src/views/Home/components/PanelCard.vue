<template>
  <div class="panel-card" :class="[size && `panel-card--${size}`]">
    <div class="panel-card__header" v-if="title || $slots.headerRight">
      <div class="panel-card__title">
        <span class="panel-card__title-icon"></span>
        <span class="panel-card__title-text">{{ title }}</span>
      </div>
      <div class="panel-card__header-right" v-if="$slots.headerRight">
        <slot name="headerRight"></slot>
      </div>
    </div>
    <div class="panel-card__body" :style="bodyStyle">
      <slot></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { CSSProperties } from 'vue'

interface Props {
  title?: string
  size?: 'small' | 'medium' | 'large'
  bodyStyle?: CSSProperties
}

defineProps<Props>()
</script>

<style lang="scss" scoped>
.panel-card {
  position: relative;
  background: linear-gradient(135deg, 
    rgba(10, 30, 60, 0.85) 0%, 
    rgba(5, 20, 45, 0.9) 100%
  );
  border: 1px solid rgba(0, 180, 255, 0.25);
  border-radius: 4px;
  overflow: hidden;
  
  &::before,
  &::after {
    content: '';
    position: absolute;
    width: 16px;
    height: 16px;
    pointer-events: none;
  }
  
  &::before {
    top: 0;
    left: 0;
    border-top: 2px solid #00b4ff;
    border-left: 2px solid #00b4ff;
  }
  
  &::after {
    bottom: 0;
    right: 0;
    border-bottom: 2px solid #00b4ff;
    border-right: 2px solid #00b4ff;
  }
  
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 12px;
    background: linear-gradient(90deg, 
      rgba(0, 180, 255, 0.15) 0%, 
      transparent 100%
    );
    border-bottom: 1px solid rgba(0, 180, 255, 0.2);
  }
  
  &__title {
    display: flex;
    align-items: center;
    gap: 8px;
    
    &-icon {
      width: 3px;
      height: 12px;
      background: linear-gradient(180deg, #00b4ff, #0066ff);
      border-radius: 2px;
      box-shadow: 0 0 8px #00b4ff;
    }
    
    &-text {
      font-size: 13px;
      font-weight: 600;
      color: #fff;
      letter-spacing: 1px;
    }
  }
  
  &__header-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  &__body {
    padding: 10px;
  }
  
  &--small &__body {
    padding: 8px;
  }
  
  &--large &__body {
    padding: 16px;
  }
}
</style>
