<template>
  <div class="weather-widget">
    <div class="weather-widget__main">
      <div class="weather-widget__icon">
        <Icon :icon="weatherIcon" :size="48" :color="iconColor" />
      </div>
      <div class="weather-widget__temp">
        <span class="weather-widget__temp-value">{{ temperature }}</span>
        <span class="weather-widget__temp-unit">°C</span>
      </div>
      <div class="weather-widget__desc">{{ description }}</div>
    </div>
    
    <div class="weather-widget__details">
      <div class="weather-widget__detail">
        <Icon icon="mdi:water-percent" :size="16" color="#3b82f6" />
        <span>{{ humidity }}%</span>
      </div>
      <div class="weather-widget__detail">
        <Icon icon="mdi:weather-windy" :size="16" color="#10b981" />
        <span>{{ windSpeed }}m/s</span>
      </div>
      <div class="weather-widget__detail">
        <Icon icon="mdi:air-filter" :size="16" color="#8b5cf6" />
        <span>AQI {{ aqi }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Icon } from '@/components/Icon'

interface Props {
  temperature?: number
  description?: string
  humidity?: number
  windSpeed?: number
  aqi?: number
  weatherType?: 'sunny' | 'cloudy' | 'rainy' | 'snowy' | 'foggy'
}

const props = withDefaults(defineProps<Props>(), {
  temperature: 26,
  description: '晴',
  humidity: 58,
  windSpeed: 3.2,
  aqi: 65,
  weatherType: 'sunny'
})

const weatherIcon = computed(() => {
  const icons: Record<string, string> = {
    sunny: 'ep:sunny',
    cloudy: 'ep:cloudy',
    rainy: 'mdi:weather-rainy',
    snowy: 'mdi:weather-snowy',
    foggy: 'mdi:weather-fog'
  }
  return icons[props.weatherType] || 'ep:sunny'
})

const iconColor = computed(() => {
  const colors: Record<string, string> = {
    sunny: '#f59e0b',
    cloudy: '#94a3b8',
    rainy: '#3b82f6',
    snowy: '#e2e8f0',
    foggy: '#9ca3af'
  }
  return colors[props.weatherType] || '#f59e0b'
})
</script>

<style lang="scss" scoped>
.weather-widget {
  padding: 10px 12px;
  background: linear-gradient(135deg, 
    rgba(10, 30, 60, 0.8) 0%, 
    rgba(5, 20, 45, 0.9) 100%
  );
  border: 1px solid rgba(0, 180, 255, 0.2);
  border-radius: 6px;
  flex-shrink: 0;
  
  &__main {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 10px;
  }
  
  &__icon {
    width: 44px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(245, 158, 11, 0.1);
    border-radius: 10px;
  }
  
  &__temp {
    display: flex;
    align-items: flex-start;
    
    &-value {
      font-size: 28px;
      font-weight: 700;
      color: #fff;
      line-height: 1;
    }
    
    &-unit {
      font-size: 14px;
      font-weight: 400;
      color: rgba(255, 255, 255, 0.7);
      margin-top: 2px;
    }
  }
  
  &__desc {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
    margin-left: auto;
  }
  
  &__details {
    display: flex;
    gap: 14px;
    padding-top: 8px;
    border-top: 1px solid rgba(0, 180, 255, 0.15);
  }
  
  &__detail {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 11px;
    color: rgba(255, 255, 255, 0.7);
  }
}
</style>
