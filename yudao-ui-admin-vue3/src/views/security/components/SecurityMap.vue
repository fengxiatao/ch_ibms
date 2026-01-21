<template>
  <div class="security-map dark-theme-page">
    <div class="map-container" ref="mapContainer">
      <!-- 建筑平面图 -->
      <div class="building-layout">
        <svg viewBox="0 0 500 500" class="floor-plan">
          <!-- 建筑轮廓 -->
          <rect x="50" y="50" width="400" height="300" fill="none" stroke="#334155" stroke-width="2"/>
          <rect x="50" y="350" width="200" height="100" fill="none" stroke="#334155" stroke-width="2"/>
          <rect x="300" y="350" width="150" height="100" fill="none" stroke="#334155" stroke-width="2"/>
          
          <!-- 房间分隔 -->
          <line x1="150" y1="50" x2="150" y2="350" stroke="#475569" stroke-width="1"/>
          <line x1="250" y1="50" x2="250" y2="350" stroke="#475569" stroke-width="1"/>
          <line x1="350" y1="50" x2="350" y2="350" stroke="#475569" stroke-width="1"/>
          <line x1="50" y1="150" x2="450" y2="150" stroke="#475569" stroke-width="1"/>
          <line x1="50" y1="250" x2="450" y2="250" stroke="#475569" stroke-width="1"/>
          
          <!-- 门 -->
          <rect x="145" y="45" width="10" height="10" fill="#00d4ff"/>
          <rect x="245" y="45" width="10" height="10" fill="#00d4ff"/>
          <rect x="345" y="45" width="10" height="10" fill="#00d4ff"/>
          <rect x="45" y="195" width="10" height="10" fill="#00d4ff"/>
          <rect x="445" y="195" width="10" height="10" fill="#00d4ff"/>
          
          <!-- 摄像头位置 -->
          <g v-for="camera in cameras" :key="camera.id">
            <circle 
              :cx="camera.x" 
              :cy="camera.y" 
              :r="camera.online ? 8 : 6"
              :fill="camera.online ? '#22c55e' : '#ef4444'"
              :stroke="selectedCamera?.id === camera.id ? '#00d4ff' : '#ffffff'"
              :stroke-width="selectedCamera?.id === camera.id ? 3 : 1"
              class="camera-dot"
              @click="selectCamera(camera)"
            />
            <!-- 摄像头视野范围 -->
            <path 
              v-if="camera.online && showCoverage"
              :d="getCameraViewPath(camera)"
              fill="rgba(0, 212, 255, 0.1)"
              stroke="rgba(0, 212, 255, 0.3)"
              stroke-width="1"
            />
            <!-- 摄像头标签 -->
            <text 
              :x="camera.x" 
              :y="camera.y - 15" 
              text-anchor="middle" 
              font-size="10" 
              fill="#94a3b8"
              class="camera-label"
            >
              {{ camera.name }}
            </text>
          </g>
          
          <!-- 告警标记 -->
          <g v-for="alarm in mapAlarms" :key="alarm.id">
            <circle 
              :cx="alarm.x" 
              :cy="alarm.y" 
              r="12"
              fill="rgba(239, 68, 68, 0.8)"
              stroke="#ef4444"
              stroke-width="2"
              class="alarm-marker"
            >
              <animate attributeName="r" values="12;16;12" dur="1.5s" repeatCount="indefinite"/>
            </circle>
            <text 
              :x="alarm.x" 
              :y="alarm.y + 4" 
              text-anchor="middle" 
              font-size="12" 
              fill="#ffffff"
              font-weight="bold"
            >
              !
            </text>
          </g>
        </svg>
      </div>
      
      <!-- 地图控制面板 -->
      <div class="map-controls">
        <div class="control-group">
          <div class="control-title">显示选项</div>
          <div class="control-options">
            <label class="control-option">
              <input type="checkbox" v-model="showCoverage" />
              <span>显示监控范围</span>
            </label>
            <label class="control-option">
              <input type="checkbox" v-model="showAlarms" />
              <span>显示告警位置</span>
            </label>
            <label class="control-option">
              <input type="checkbox" v-model="showOffline" />
              <span>显示离线设备</span>
            </label>
          </div>
        </div>
        
        <div class="control-group">
          <div class="control-title">图例</div>
          <div class="legend">
            <div class="legend-item">
              <div class="legend-dot online"></div>
              <span>在线摄像头</span>
            </div>
            <div class="legend-item">
              <div class="legend-dot offline"></div>
              <span>离线摄像头</span>
            </div>
            <div class="legend-item">
              <div class="legend-dot alarm"></div>
              <span>告警位置</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 摄像头详情面板 -->
    <div v-if="selectedCamera" class="camera-detail-panel">
      <div class="panel-header">
        <h4>{{ selectedCamera.name }}</h4>
        <el-button size="small" @click="closeDetail">
          <Icon icon="ep:close" />
        </el-button>
      </div>
      <div class="panel-content">
        <div class="detail-item">
          <span class="label">位置:</span>
          <span class="value">{{ selectedCamera.location }}</span>
        </div>
        <div class="detail-item">
          <span class="label">状态:</span>
          <span class="value" :class="{ online: selectedCamera.online, offline: !selectedCamera.online }">
            {{ selectedCamera.online ? '在线' : '离线' }}
          </span>
        </div>
        <div class="detail-item">
          <span class="label">分辨率:</span>
          <span class="value">1920x1080</span>
        </div>
        <div class="detail-item">
          <span class="label">帧率:</span>
          <span class="value">25fps</span>
        </div>
      </div>
      <div class="panel-actions">
        <el-button size="small" type="primary" @click="viewCamera">查看监控</el-button>
        <el-button size="small" @click="editCamera">编辑设置</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

interface Camera {
  id: number
  name: string
  location: string
  online: boolean
  x: number
  y: number
}

interface Props {
  cameras: Camera[]
}

const props = defineProps<Props>()
const emit = defineEmits<{
  cameraSelected: [camera: Camera]
}>()

// 响应式数据
const selectedCamera = ref<Camera | null>(null)
const showCoverage = ref(true)
const showAlarms = ref(true)
const showOffline = ref(true)

// 模拟告警位置
const mapAlarms = ref([
  { id: 1, x: 120, y: 180, type: 'intrusion' },
  { id: 2, x: 280, y: 300, type: 'crowd' }
])

// 计算属性 - 过滤显示的摄像头
const visibleCameras = computed(() => {
  if (showOffline.value) {
    return props.cameras
  }
  return props.cameras.filter(camera => camera.online)
})

// 方法
const selectCamera = (camera: Camera) => {
  selectedCamera.value = camera
  emit('cameraSelected', camera)
}

const closeDetail = () => {
  selectedCamera.value = null
}

const getCameraViewPath = (camera: Camera) => {
  // 生成摄像头视野范围的SVG路径
  const radius = 40
  const angle = 60 // 视野角度
  const startAngle = -angle / 2
  const endAngle = angle / 2
  
  const x1 = camera.x + radius * Math.cos(startAngle * Math.PI / 180)
  const y1 = camera.y + radius * Math.sin(startAngle * Math.PI / 180)
  const x2 = camera.x + radius * Math.cos(endAngle * Math.PI / 180)
  const y2 = camera.y + radius * Math.sin(endAngle * Math.PI / 180)
  
  return `M ${camera.x} ${camera.y} L ${x1} ${y1} A ${radius} ${radius} 0 0 1 ${x2} ${y2} Z`
}

const viewCamera = () => {
  if (selectedCamera.value) {
    ElMessage.info(`切换到 ${selectedCamera.value.name} 监控画面`)
    emit('cameraSelected', selectedCamera.value)
  }
}

const editCamera = () => {
  if (selectedCamera.value) {
    ElMessage.info(`编辑 ${selectedCamera.value.name} 设置`)
  }
}
</script>

<style lang="scss" scoped>@use '@/styles/dark-theme.scss';

.security-map {
  position: relative;
  width: 100%;
  height: 100%;
  background: rgba(15, 23, 42, 0.5);
  border-radius: 8px;
  overflow: hidden;

  .map-container {
    width: 100%;
    height: 100%;
    position: relative;

    .building-layout {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 20px;

      .floor-plan {
        width: 100%;
        height: 100%;
        max-width: 600px;
        max-height: 500px;

        .camera-dot {
          cursor: pointer;
          transition: all 0.3s ease;

          &:hover {
            r: 10;
          }
        }

        .camera-label {
          pointer-events: none;
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
        }

        .alarm-marker {
          cursor: pointer;
        }
      }
    }

    .map-controls {
      position: absolute;
      top: 20px;
      right: 20px;
      background: rgba(15, 23, 42, 0.9);
      backdrop-filter: blur(10px);
      border-radius: 8px;
      padding: 16px;
      border: 1px solid rgba(0, 212, 255, 0.2);
      min-width: 200px;

      .control-group {
        margin-bottom: 16px;

        &:last-child {
          margin-bottom: 0;
        }

        .control-title {
          color: #00d4ff;
          font-size: 12px;
          font-weight: 500;
          margin-bottom: 8px;
        }

        .control-options {
          .control-option {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 6px;
            cursor: pointer;
            color: #94a3b8;
            font-size: 12px;

            &:last-child {
              margin-bottom: 0;
            }

            input[type="checkbox"] {
              accent-color: #00d4ff;
            }

            &:hover {
              color: #ffffff;
            }
          }
        }

        .legend {
          .legend-item {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 6px;
            font-size: 12px;
            color: #94a3b8;

            &:last-child {
              margin-bottom: 0;
            }

            .legend-dot {
              width: 8px;
              height: 8px;
              border-radius: 50%;

              &.online {
                background: #22c55e;
              }

              &.offline {
                background: #ef4444;
              }

              &.alarm {
                background: #ef4444;
                animation: pulse 1.5s infinite;
              }
            }
          }
        }
      }
    }
  }

  .camera-detail-panel {
    position: absolute;
    bottom: 20px;
    left: 20px;
    width: 280px;
    background: rgba(15, 23, 42, 0.95);
    backdrop-filter: blur(15px);
    border-radius: 12px;
    border: 1px solid rgba(0, 212, 255, 0.2);
    overflow: hidden;

    .panel-header {
      padding: 16px;
      background: rgba(0, 212, 255, 0.1);
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid rgba(0, 212, 255, 0.2);

      h4 {
        margin: 0;
        color: #00d4ff;
        font-size: 16px;
      }
    }

    .panel-content {
      padding: 16px;

      .detail-item {
        display: flex;
        justify-content: space-between;
        margin-bottom: 12px;
        font-size: 14px;

        &:last-child {
          margin-bottom: 0;
        }

        .label {
          color: #94a3b8;
        }

        .value {
          color: #ffffff;

          &.online {
            color: #22c55e;
          }

          &.offline {
            color: #ef4444;
          }
        }
      }
    }

    .panel-actions {
      padding: 16px;
      border-top: 1px solid rgba(0, 212, 255, 0.1);
      display: flex;
      gap: 8px;
    }
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
</style>





















