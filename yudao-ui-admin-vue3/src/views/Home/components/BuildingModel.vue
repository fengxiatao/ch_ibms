<template>
  <div class="building-model">
    <div class="building-model__container">
      <!-- 3D建筑模型占位区 - 可替换为实际3D模型 -->
      <div class="building-model__visual">
        <div class="building-model__floors">
          <div 
            v-for="floor in floors" 
            :key="floor.id"
            class="building-model__floor"
            :class="{ 
              'building-model__floor--active': selectedFloor === floor.id,
              'building-model__floor--alert': floor.hasAlert 
            }"
            :style="{ 
              '--floor-height': floor.height + 'px',
              '--floor-color': floor.color 
            }"
            @click="handleFloorClick(floor)"
          >
            <span class="building-model__floor-label">{{ floor.name }}</span>
            <span v-if="floor.hasAlert" class="building-model__floor-alert">
              <Icon icon="ep:warning-filled" :size="14" />
            </span>
          </div>
        </div>
        
        <!-- 装饰元素 -->
        <div class="building-model__decoration">
          <div class="building-model__grid"></div>
          <div class="building-model__glow"></div>
        </div>
      </div>
      
      <!-- 楼层信息面板 -->
      <div v-if="selectedFloorInfo" class="building-model__info">
        <div class="building-model__info-header">
          <span>{{ selectedFloorInfo.name }}</span>
          <el-tag size="small" :type="selectedFloorInfo.hasAlert ? 'danger' : 'success'">
            {{ selectedFloorInfo.hasAlert ? '有告警' : '正常' }}
          </el-tag>
        </div>
        <div class="building-model__info-stats">
          <div class="building-model__info-item">
            <span class="building-model__info-value">{{ selectedFloorInfo.rooms }}</span>
            <span class="building-model__info-label">房间数</span>
          </div>
          <div class="building-model__info-item">
            <span class="building-model__info-value">{{ selectedFloorInfo.devices }}</span>
            <span class="building-model__info-label">设备数</span>
          </div>
          <div class="building-model__info-item">
            <span class="building-model__info-value">{{ selectedFloorInfo.temperature }}°C</span>
            <span class="building-model__info-label">温度</span>
          </div>
          <div class="building-model__info-item">
            <span class="building-model__info-value">{{ selectedFloorInfo.power }}kW</span>
            <span class="building-model__info-label">功率</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 环境数据环形展示 -->
    <div class="building-model__env-ring">
      <div class="building-model__env-item" v-for="env in envData" :key="env.label">
        <div class="building-model__env-progress">
          <svg viewBox="0 0 100 100">
            <circle 
              class="building-model__env-bg" 
              cx="50" cy="50" r="42" 
            />
            <circle 
              class="building-model__env-bar" 
              cx="50" cy="50" r="42"
              :style="{ 
                '--progress': env.percent,
                '--color': env.color 
              }"
            />
          </svg>
          <div class="building-model__env-value">
            <span>{{ env.value }}</span>
            <span>{{ env.unit }}</span>
          </div>
        </div>
        <div class="building-model__env-label">{{ env.label }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Icon } from '@/components/Icon'

interface Floor {
  id: number
  name: string
  height: number
  color: string
  hasAlert: boolean
  rooms: number
  devices: number
  temperature: number
  power: number
}

interface Props {
  floors?: Floor[]
  envData?: Array<{
    label: string
    value: number
    unit: string
    percent: number
    color: string
  }>
}

const props = withDefaults(defineProps<Props>(), {
  floors: () => [
    { id: 1, name: 'B1层', height: 35, color: '#374151', hasAlert: false, rooms: 15, devices: 45, temperature: 22, power: 32 },
    { id: 2, name: '1层', height: 45, color: '#0891b2', hasAlert: false, rooms: 20, devices: 68, temperature: 24, power: 56 },
    { id: 3, name: '2层', height: 45, color: '#0ea5e9', hasAlert: true, rooms: 25, devices: 82, temperature: 25, power: 78 },
    { id: 4, name: '3层', height: 45, color: '#38bdf8', hasAlert: false, rooms: 25, devices: 75, temperature: 24, power: 65 },
    { id: 5, name: '4层', height: 45, color: '#7dd3fc', hasAlert: false, rooms: 22, devices: 70, temperature: 23, power: 58 },
    { id: 6, name: '5层', height: 40, color: '#bae6fd', hasAlert: false, rooms: 18, devices: 52, temperature: 23, power: 42 }
  ],
  envData: () => [
    { label: '温度', value: 24.5, unit: '°C', percent: 65, color: '#f59e0b' },
    { label: '湿度', value: 58, unit: '%', percent: 58, color: '#3b82f6' },
    { label: 'PM2.5', value: 35, unit: 'μg', percent: 35, color: '#10b981' },
    { label: 'CO2', value: 420, unit: 'ppm', percent: 42, color: '#8b5cf6' }
  ]
})

const selectedFloor = ref<number>(3)

const selectedFloorInfo = computed(() => {
  return props.floors.find(f => f.id === selectedFloor.value)
})

const handleFloorClick = (floor: Floor) => {
  selectedFloor.value = floor.id
}
</script>

<style lang="scss" scoped>
.building-model {
  display: flex;
  gap: 16px;
  height: 100%;
  
  &__container {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  
  &__visual {
    position: relative;
    perspective: 600px;
  }
  
  &__floors {
    display: flex;
    flex-direction: column-reverse;
    gap: 3px;
    transform: rotateX(10deg) rotateY(-15deg);
    transform-style: preserve-3d;
  }
  
  &__floor {
    width: 130px;
    height: var(--floor-height, 28px);
    background: var(--floor-color, #0ea5e9);
    border-radius: 3px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 10px;
    cursor: pointer;
    transition: all 0.3s ease;
    position: relative;
    box-shadow: 
      3px 3px 0 rgba(0, 0, 0, 0.3),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
    
    &::before {
      content: '';
      position: absolute;
      right: -3px;
      top: 3px;
      width: 3px;
      height: 100%;
      background: rgba(0, 0, 0, 0.3);
      transform: skewY(-45deg);
      transform-origin: top;
    }
    
    &:hover {
      transform: translateX(-6px) scale(1.02);
      box-shadow: 
        6px 6px 0 rgba(0, 0, 0, 0.3),
        inset 0 1px 0 rgba(255, 255, 255, 0.2),
        0 0 15px rgba(0, 180, 255, 0.3);
    }
    
    &--active {
      background: #00b4ff !important;
      box-shadow: 
        3px 3px 0 rgba(0, 0, 0, 0.3),
        inset 0 1px 0 rgba(255, 255, 255, 0.2),
        0 0 20px rgba(0, 180, 255, 0.5);
    }
    
    &--alert {
      animation: floor-alert 1.5s ease-in-out infinite;
    }
    
    &-label {
      font-size: 11px;
      font-weight: 500;
      color: #fff;
    }
    
    &-alert {
      color: #fff;
      animation: blink 1s ease-in-out infinite;
    }
  }
  
  &__decoration {
    position: absolute;
    bottom: -20px;
    left: 50%;
    transform: translateX(-50%);
    width: 160px;
    height: 16px;
  }
  
  &__grid {
    width: 100%;
    height: 100%;
    background: 
      linear-gradient(90deg, rgba(0, 180, 255, 0.1) 1px, transparent 1px),
      linear-gradient(rgba(0, 180, 255, 0.1) 1px, transparent 1px);
    background-size: 8px 8px;
    transform: perspective(100px) rotateX(60deg);
  }
  
  &__glow {
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 100px;
    height: 3px;
    background: #00b4ff;
    filter: blur(6px);
  }
  
  &__info {
    margin-top: 16px;
    width: 100%;
    max-width: 240px;
    padding: 10px;
    background: rgba(5, 20, 45, 0.8);
    border: 1px solid rgba(0, 180, 255, 0.3);
    border-radius: 6px;
    
    &-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 8px;
      font-size: 12px;
      font-weight: 500;
      color: #fff;
    }
    
    &-stats {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 4px;
    }
    
    &-item {
      text-align: center;
    }
    
    &-value {
      display: block;
      font-size: 14px;
      font-weight: 600;
      color: #00b4ff;
    }
    
    &-label {
      font-size: 10px;
      color: rgba(255, 255, 255, 0.5);
    }
  }
  
  &__env-ring {
    display: flex;
    flex-direction: column;
    gap: 10px;
    width: 70px;
  }
  
  &__env-item {
    text-align: center;
  }
  
  &__env-progress {
    position: relative;
    width: 56px;
    height: 56px;
    margin: 0 auto;
    
    svg {
      width: 100%;
      height: 100%;
      transform: rotate(-90deg);
    }
  }
  
  &__env-bg {
    fill: none;
    stroke: rgba(0, 180, 255, 0.1);
    stroke-width: 8;
  }
  
  &__env-bar {
    fill: none;
    stroke: var(--color, #00b4ff);
    stroke-width: 8;
    stroke-linecap: round;
    stroke-dasharray: 264;
    stroke-dashoffset: calc(264 - (264 * var(--progress, 50) / 100));
    filter: drop-shadow(0 0 6px var(--color, #00b4ff));
    transition: stroke-dashoffset 0.5s ease;
  }
  
  &__env-value {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
    
    span:first-child {
      display: block;
      font-size: 12px;
      font-weight: 600;
      color: #fff;
    }
    
    span:last-child {
      font-size: 9px;
      color: rgba(255, 255, 255, 0.5);
    }
  }
  
  &__env-label {
    margin-top: 4px;
    font-size: 10px;
    color: rgba(255, 255, 255, 0.6);
  }
}

@keyframes floor-alert {
  0%, 100% { 
    background: var(--floor-color);
  }
  50% { 
    background: #ef4444;
  }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
</style>
