<template>
  <ContentWrap style="margin-top: 70px;">

  <div class="fire-alarm-monitoring dark-theme-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <Icon icon="ep:warning-filled" :size="24" />
        <h1>火灾报警监控</h1>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleRefresh">
          <Icon icon="ep:refresh" />
          刷新数据
        </el-button>
        <el-button type="warning" @click="handleTestAlarm">
          <Icon icon="ep:bell" />
          测试报警
        </el-button>
      </div>
    </div>

    <!-- 实时状态概览 -->
    <div class="status-overview">
      <div class="status-card normal">
        <div class="status-icon">
          <Icon icon="ep:circle-check" :size="32" />
        </div>
        <div class="status-content">
          <div class="status-value">{{ statusStats.normal }}</div>
          <div class="status-label">正常设备</div>
        </div>
      </div>
      <div class="status-card alarm">
        <div class="status-icon">
          <Icon icon="ep:warning-filled" :size="32" />
        </div>
        <div class="status-content">
          <div class="status-value">{{ statusStats.alarm }}</div>
          <div class="status-label">报警设备</div>
        </div>
      </div>
      <div class="status-card fault">
        <div class="status-icon">
          <Icon icon="ep:circle-close" :size="32" />
        </div>
        <div class="status-content">
          <div class="status-value">{{ statusStats.fault }}</div>
          <div class="status-label">故障设备</div>
        </div>
      </div>
      <div class="status-card offline">
        <div class="status-icon">
          <Icon icon="ep:connection" :size="32" />
        </div>
        <div class="status-content">
          <div class="status-value">{{ statusStats.offline }}</div>
          <div class="status-label">离线设备</div>
        </div>
      </div>
    </div>

    <!-- 报警信息和系统状态 -->
    <div class="monitoring-content">
      <el-row :gutter="20">
        <!-- 左侧：实时报警信息 -->
        <el-col :span="14">
          <div class="alarm-panel">
            <div class="panel-header">
              <h3>
                <Icon icon="ep:bell" />
                实时报警信息
              </h3>
              <div class="panel-actions">
                <el-button size="small" type="danger" @click="handleClearAll">
                  清除所有
                </el-button>
              </div>
            </div>
            
            <div class="alarm-list">
              <div
                v-for="alarm in alarmList"
                :key="alarm.id"
                class="alarm-item"
                :class="alarm.level"
              >
                <div class="alarm-icon">
                  <Icon 
                    :icon="getAlarmIcon(alarm.level)" 
                    :size="24" 
                    :color="getAlarmColor(alarm.level)"
                  />
                </div>
                <div class="alarm-content">
                  <div class="alarm-title">{{ alarm.deviceName }} - {{ alarm.message }}</div>
                  <div class="alarm-info">
                    <span class="alarm-location">
                      <Icon icon="ep:location" />
                      {{ alarm.location }}
                    </span>
                    <span class="alarm-time">
                      <Icon icon="ep:clock" />
                      {{ alarm.time }}
                    </span>
                  </div>
                </div>
                <div class="alarm-actions">
                  <el-button size="small" type="primary" @click="handleAlarmDetail(alarm)">
                    详情
                  </el-button>
                  <el-button size="small" type="success" @click="handleConfirmAlarm(alarm)">
                    确认
                  </el-button>
                </div>
              </div>
              
              <div v-if="alarmList.length === 0" class="no-alarm">
                <Icon icon="ep:circle-check" :size="48" color="#67C23A" />
                <p>系统正常，暂无报警信息</p>
              </div>
            </div>
          </div>
        </el-col>

        <!-- 右侧：系统状态 -->
        <el-col :span="10">
          <div class="system-panel">
            <div class="panel-header">
              <h3>
                <Icon icon="ep:monitor" />
                系统状态
              </h3>
            </div>
            
            <div class="system-status">
              <div class="status-item">
                <div class="status-indicator running"></div>
                <span class="status-text">主控系统：运行正常</span>
              </div>
              <div class="status-item">
                <div class="status-indicator running"></div>
                <span class="status-text">通信网络：连接正常</span>
              </div>
              <div class="status-item">
                <div class="status-indicator running"></div>
                <span class="status-text">数据库：运行正常</span>
              </div>
              <div class="status-item">
                <div class="status-indicator warning"></div>
                <span class="status-text">备用电源：电量82%</span>
              </div>
            </div>

            <!-- 设备状态统计 -->
            <div class="device-stats">
              <h4>设备状态统计</h4>
              <div class="stats-grid">
                <div class="stat-item">
                  <div class="stat-label">烟感器</div>
                  <div class="stat-value">
                    <span class="normal">156</span> / <span class="total">160</span>
                  </div>
                </div>
                <div class="stat-item">
                  <div class="stat-label">温感器</div>
                  <div class="stat-value">
                    <span class="normal">89</span> / <span class="total">92</span>
                  </div>
                </div>
                <div class="stat-item">
                  <div class="stat-label">手报器</div>
                  <div class="stat-value">
                    <span class="normal">45</span> / <span class="total">48</span>
                  </div>
                </div>
                <div class="stat-item">
                  <div class="stat-label">声光器</div>
                  <div class="stat-value">
                    <span class="normal">32</span> / <span class="total">36</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 楼层分布图 -->
    <div class="floor-distribution">
      <div class="panel-header">
        <h3>
          <Icon icon="ep:office-building" />
          楼层分布监控
        </h3>
        <div class="floor-controls">
          <el-select v-model="selectedFloor" placeholder="选择楼层" style="width: 120px">
            <el-option
              v-for="floor in floorList"
              :key="floor.value"
              :label="floor.label"
              :value="floor.value"
            />
          </el-select>
        </div>
      </div>
      
      <div class="floor-map">
        <div class="map-container">
          <!-- 这里应该是楼层平面图，现在用占位符代替 -->
          <div class="floor-placeholder">
            <div class="device-points">
              <div
                v-for="device in floorDevices"
                :key="device.id"
                class="device-point"
                :class="device.status"
                :style="{ left: device.x + '%', top: device.y + '%' }"
                @click="handleDeviceClick(device)"
              >
                <div class="point-indicator"></div>
                <div class="device-tooltip">
                  {{ device.name }}<br />
                  状态：{{ getStatusText(device.status) }}
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="map-legend">
          <div class="legend-item">
            <div class="legend-color normal"></div>
            <span>正常</span>
          </div>
          <div class="legend-item">
            <div class="legend-color alarm"></div>
            <span>报警</span>
          </div>
          <div class="legend-item">
            <div class="legend-color fault"></div>
            <span>故障</span>
          </div>
          <div class="legend-item">
            <div class="legend-color offline"></div>
            <span>离线</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 报警详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="报警详情"
      width="600px"
      destroy-on-close
    >
      <div v-if="currentAlarm" class="alarm-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="设备名称">{{ currentAlarm.deviceName }}</el-descriptions-item>
          <el-descriptions-item label="设备编号">{{ currentAlarm.deviceCode }}</el-descriptions-item>
          <el-descriptions-item label="报警类型">{{ currentAlarm.type }}</el-descriptions-item>
          <el-descriptions-item label="报警级别">
            <el-tag :type="getAlarmTagType(currentAlarm.level)">{{ currentAlarm.level }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="设备位置">{{ currentAlarm.location }}</el-descriptions-item>
          <el-descriptions-item label="报警时间">{{ currentAlarm.time }}</el-descriptions-item>
          <el-descriptions-item label="报警信息" :span="2">{{ currentAlarm.message }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="alarm-actions-detail">
          <h4>处理操作</h4>
          <el-button type="success" @click="handleConfirmAlarm(currentAlarm)">
            <Icon icon="ep:check" />
            确认报警
          </el-button>
          <el-button type="warning" @click="handleIgnoreAlarm(currentAlarm)">
            <Icon icon="ep:close" />
            忽略报警
          </el-button>
          <el-button type="primary" @click="handleDispatch(currentAlarm)">
            <Icon icon="ep:user" />
            派遣处理
          </el-button>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Icon } from '@/components/Icon'

interface AlarmInfo {
  id: string
  deviceName: string
  deviceCode: string
  type: string
  level: string
  message: string
  location: string
  time: string
  status: string
}

interface FloorDevice {
  id: string
  name: string
  type: string
  status: string
  x: number
  y: number
}

// 响应式数据
const detailDialogVisible = ref(false)
const currentAlarm = ref<AlarmInfo | null>(null)
const selectedFloor = ref('F1')

// 状态统计
const statusStats = reactive({
  normal: 322,
  alarm: 3,
  fault: 2,
  offline: 1
})

// 实时报警列表
const alarmList = ref<AlarmInfo[]>([
  {
    id: '1',
    deviceName: '烟感器-01',
    deviceCode: 'SMK_001',
    type: '烟雾报警',
    level: 'high',
    message: '检测到烟雾浓度异常',
    location: '1号楼-3F-办公区A',
    time: '2024-09-20 14:25:30',
    status: 'active'
  },
  {
    id: '2',
    deviceName: '温感器-15',
    deviceCode: 'TMP_015',
    type: '温度报警',
    level: 'medium',
    message: '温度超过预设阈值',
    location: '2号楼-2F-会议室B',
    time: '2024-09-20 14:20:15',
    status: 'active'
  },
  {
    id: '3',
    deviceName: '手报器-08',
    deviceCode: 'MAN_008',
    type: '手动报警',
    level: 'high',
    message: '手动触发火灾报警',
    location: '1号楼-1F-大厅',
    time: '2024-09-20 14:18:45',
    status: 'active'
  }
])

// 楼层列表
const floorList = ref([
  { label: '1F', value: 'F1' },
  { label: '2F', value: 'F2' },
  { label: '3F', value: 'F3' },
  { label: '4F', value: 'F4' },
  { label: '5F', value: 'F5' }
])

// 楼层设备点位
const floorDevices = ref<FloorDevice[]>([
  { id: '1', name: '烟感器-01', type: 'smoke', status: 'alarm', x: 20, y: 30 },
  { id: '2', name: '温感器-02', type: 'temperature', status: 'normal', x: 40, y: 25 },
  { id: '3', name: '手报器-03', type: 'manual', status: 'normal', x: 60, y: 40 },
  { id: '4', name: '声光器-04', type: 'sounder', status: 'normal', x: 80, y: 35 },
  { id: '5', name: '烟感器-05', type: 'smoke', status: 'fault', x: 25, y: 60 },
  { id: '6', name: '温感器-06', type: 'temperature', status: 'offline', x: 70, y: 70 }
])

// 定时器
let refreshTimer: NodeJS.Timeout | null = null

// 工具函数
const getAlarmIcon = (level: string) => {
  switch (level) {
    case 'high':
      return 'ep:warning-filled'
    case 'medium':
      return 'ep:warning'
    case 'low':
      return 'ep:info-filled'
    default:
      return 'ep:bell'
  }
}

const getAlarmColor = (level: string) => {
  switch (level) {
    case 'high':
      return '#F56C6C'
    case 'medium':
      return '#E6A23C'
    case 'low':
      return '#409EFF'
    default:
      return '#909399'
  }
}

const getAlarmTagType = (level: string) => {
  switch (level) {
    case 'high':
      return 'danger'
    case 'medium':
      return 'warning'
    case 'low':
      return 'info'
    default:
      return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'normal':
      return '正常'
    case 'alarm':
      return '报警'
    case 'fault':
      return '故障'
    case 'offline':
      return '离线'
    default:
      return status
  }
}

// 事件处理
const handleRefresh = () => {
  ElMessage.success('数据刷新成功')
}

const handleTestAlarm = () => {
  ElMessage.warning('测试报警已触发')
}

const handleClearAll = () => {
  ElMessageBox.confirm('确定要清除所有报警信息吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    alarmList.value = []
    ElMessage.success('已清除所有报警信息')
  })
}

const handleAlarmDetail = (alarm: AlarmInfo) => {
  currentAlarm.value = alarm
  detailDialogVisible.value = true
}

const handleConfirmAlarm = (alarm: AlarmInfo) => {
  const index = alarmList.value.findIndex(item => item.id === alarm.id)
  if (index > -1) {
    alarmList.value.splice(index, 1)
    ElMessage.success('报警已确认处理')
  }
  detailDialogVisible.value = false
}

const handleIgnoreAlarm = (alarm: AlarmInfo) => {
  const index = alarmList.value.findIndex(item => item.id === alarm.id)
  if (index > -1) {
    alarmList.value.splice(index, 1)
    ElMessage.info('报警已忽略')
  }
  detailDialogVisible.value = false
}

const handleDispatch = (alarm: AlarmInfo) => {
  ElMessage.success('已派遣相关人员处理')
  detailDialogVisible.value = false
}

const handleDeviceClick = (device: FloorDevice) => {
  ElMessage.info(`点击了设备：${device.name}，状态：${getStatusText(device.status)}`)
}

// 模拟实时数据更新
const startRealTimeUpdate = () => {
  refreshTimer = setInterval(() => {
    // 模拟状态更新
    if (Math.random() > 0.7) {
      // 随机更新设备状态
      const randomDevice = floorDevices.value[Math.floor(Math.random() * floorDevices.value.length)]
      const statuses = ['normal', 'alarm', 'fault', 'offline']
      randomDevice.status = statuses[Math.floor(Math.random() * statuses.length)]
    }
  }, 5000)
}

const stopRealTimeUpdate = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 生命周期
onMounted(() => {
  startRealTimeUpdate()
})

onUnmounted(() => {
  stopRealTimeUpdate()
})
</script>

<style scoped lang="scss">@use '@/styles/dark-theme.scss';

.fire-alarm-monitoring {
  padding: 20px;
  background: linear-gradient(135deg, #0a1628 0%, #1e3a8a 50%, #0f172a 100%);
  min-height: 100vh;
  color: #ffffff;

  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
    padding: 16px 24px;
    background: rgba(15, 23, 42, 0.8);
    backdrop-filter: blur(15px);
    border-radius: 12px;
    border: 1px solid rgba(0, 212, 255, 0.1);

    .header-title {
      display: flex;
      align-items: center;
      gap: 12px;

      .el-icon {
        color: #F56C6C;
      }

      h1 {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: #00d4ff;
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .status-overview {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 20px;

    .status-card {
      background: rgba(15, 23, 42, 0.7);
      border-radius: 8px;
      padding: 20px;
      border: 1px solid rgba(0, 212, 255, 0.1);
      display: flex;
      align-items: center;
      gap: 16px;

      .status-icon {
        width: 48px;
        height: 48px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .status-content {
        flex: 1;

        .status-value {
          font-size: 24px;
          font-weight: bold;
          margin-bottom: 4px;
        }

        .status-label {
          font-size: 14px;
          color: #e2e8f0;
        }
      }

      &.normal {
        .status-icon {
          background: rgba(103, 194, 58, 0.2);
          color: #67C23A;
        }
        .status-value {
          color: #67C23A;
        }
      }

      &.alarm {
        .status-icon {
          background: rgba(245, 108, 108, 0.2);
          color: #F56C6C;
        }
        .status-value {
          color: #F56C6C;
        }
      }

      &.fault {
        .status-icon {
          background: rgba(230, 162, 60, 0.2);
          color: #E6A23C;
        }
        .status-value {
          color: #E6A23C;
        }
      }

      &.offline {
        .status-icon {
          background: rgba(144, 147, 153, 0.2);
          color: #909399;
        }
        .status-value {
          color: #909399;
        }
      }
    }
  }

  .monitoring-content {
    margin-bottom: 20px;

    .alarm-panel, .system-panel {
      background: rgba(15, 23, 42, 0.7);
      border-radius: 8px;
      border: 1px solid rgba(0, 212, 255, 0.1);
      height: 500px;
      display: flex;
      flex-direction: column;

      .panel-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px 20px;
        border-bottom: 1px solid rgba(0, 212, 255, 0.1);

        h3 {
          margin: 0;
          color: #00d4ff;
          font-size: 16px;
          display: flex;
          align-items: center;
          gap: 8px;
        }
      }

      .alarm-list {
        flex: 1;
        padding: 16px;
        overflow-y: auto;

        .alarm-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 12px;
          margin-bottom: 8px;
          background: rgba(30, 58, 138, 0.3);
          border-radius: 6px;
          border-left: 4px solid;

          &.high {
            border-left-color: #F56C6C;
          }

          &.medium {
            border-left-color: #E6A23C;
          }

          &.low {
            border-left-color: #409EFF;
          }

          .alarm-content {
            flex: 1;

            .alarm-title {
              font-weight: 600;
              margin-bottom: 4px;
            }

            .alarm-info {
              display: flex;
              gap: 16px;
              font-size: 12px;
              color: #94a3b8;

              span {
                display: flex;
                align-items: center;
                gap: 4px;
              }
            }
          }

          .alarm-actions {
            display: flex;
            gap: 8px;
          }
        }

        .no-alarm {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          height: 200px;
          color: #94a3b8;

          p {
            margin-top: 12px;
          }
        }
      }

      .system-status {
        padding: 16px 20px;

        .status-item {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 12px;

          .status-indicator {
            width: 8px;
            height: 8px;
            border-radius: 50%;

            &.running {
              background: #67C23A;
            }

            &.warning {
              background: #E6A23C;
            }

            &.error {
              background: #F56C6C;
            }
          }

          .status-text {
            color: #e2e8f0;
          }
        }
      }

      .device-stats {
        padding: 16px 20px;
        border-top: 1px solid rgba(0, 212, 255, 0.1);

        h4 {
          margin: 0 0 16px 0;
          color: #00d4ff;
          font-size: 14px;
        }

        .stats-grid {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 12px;

          .stat-item {
            .stat-label {
              font-size: 12px;
              color: #94a3b8;
              margin-bottom: 4px;
            }

            .stat-value {
              .normal {
                color: #67C23A;
                font-weight: 600;
              }

              .total {
                color: #e2e8f0;
              }
            }
          }
        }
      }
    }
  }

  .floor-distribution {
    background: rgba(15, 23, 42, 0.7);
    border-radius: 8px;
    border: 1px solid rgba(0, 212, 255, 0.1);
    padding: 20px;

    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      h3 {
        margin: 0;
        color: #00d4ff;
        font-size: 16px;
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }

    .floor-map {
      position: relative;

      .map-container {
        height: 400px;
        background: rgba(30, 58, 138, 0.1);
        border-radius: 6px;
        position: relative;

        .floor-placeholder {
          width: 100%;
          height: 100%;
          background: linear-gradient(45deg, #1e3a8a 25%, transparent 25%), 
                      linear-gradient(-45deg, #1e3a8a 25%, transparent 25%), 
                      linear-gradient(45deg, transparent 75%, #1e3a8a 75%), 
                      linear-gradient(-45deg, transparent 75%, #1e3a8a 75%);
          background-size: 20px 20px;
          background-position: 0 0, 0 10px, 10px -10px, -10px 0px;
          position: relative;

          .device-points {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;

            .device-point {
              position: absolute;
              width: 12px;
              height: 12px;
              cursor: pointer;
              
              .point-indicator {
                width: 100%;
                height: 100%;
                border-radius: 50%;
                
                &.normal { background: #67C23A; }
                &.alarm { background: #F56C6C; }
                &.fault { background: #E6A23C; }
                &.offline { background: #909399; }
              }

              .device-tooltip {
                position: absolute;
                bottom: 100%;
                left: 50%;
                transform: translateX(-50%);
                background: rgba(0, 0, 0, 0.8);
                color: white;
                padding: 6px 8px;
                border-radius: 4px;
                font-size: 12px;
                white-space: nowrap;
                opacity: 0;
                transition: opacity 0.3s;
                pointer-events: none;
                z-index: 10;
              }

              &:hover .device-tooltip {
                opacity: 1;
              }

              &.normal .point-indicator { background: #67C23A; }
              &.alarm .point-indicator { background: #F56C6C; }
              &.fault .point-indicator { background: #E6A23C; }
              &.offline .point-indicator { background: #909399; }
            }
          }
        }
      }

      .map-legend {
        display: flex;
        gap: 20px;
        margin-top: 12px;
        justify-content: center;

        .legend-item {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 12px;

          .legend-color {
            width: 12px;
            height: 12px;
            border-radius: 50%;

            &.normal { background: #67C23A; }
            &.alarm { background: #F56C6C; }
            &.fault { background: #E6A23C; }
            &.offline { background: #909399; }
          }
        }
      }
    }
  }

  .alarm-detail {
    .alarm-actions-detail {
      margin-top: 20px;
      padding-top: 16px;
      border-top: 1px solid #e5e7eb;

      h4 {
        margin: 0 0 12px 0;
        color: #374151;
      }

      .el-button {
        margin-right: 8px;
      }
    }
  }
}

// 弹窗样式覆盖
:deep(.el-dialog) {
  .el-dialog__header {
    background: #f8fafc;
    border-bottom: 1px solid #e5e7eb;
  }

  .el-dialog__title {
    color: #374151;
    font-weight: 600;
  }

  .el-dialog__body {
    padding: 24px;
  }

  .el-dialog__footer {
    background: #f8fafc;
    border-top: 1px solid #e5e7eb;
  }
}
</style>



