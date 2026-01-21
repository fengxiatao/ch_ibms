<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧建筑</el-breadcrumb-item>
        <el-breadcrumb-item>冷源群控</el-breadcrumb-item>
        <el-breadcrumb-item>系统拓扑</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-card class="toolbar-card">
        <div class="toolbar-content">
          <div class="toolbar-left">
            <el-select v-model="selectedSystem" @change="handleSystemChange" style="width: 200px;">
              <el-option label="冷水系统" value="chilled_water" />
              <el-option label="冷却水系统" value="cooling_water" />
              <el-option label="热水系统" value="hot_water" />
              <el-option label="综合系统" value="integrated" />
            </el-select>
            <el-button-group style="margin-left: 16px;">
              <el-button :type="viewMode === 'topology' ? 'primary' : ''" @click="viewMode = 'topology'">
                <el-icon><Share /></el-icon>
                拓扑视图
              </el-button>
              <el-button :type="viewMode === 'flow' ? 'primary' : ''" @click="viewMode = 'flow'">
                <el-icon><Connection /></el-icon>
                流程图
              </el-button>
            </el-button-group>
          </div>
          
          <div class="toolbar-right">
            <el-button @click="handleRefresh">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button @click="handleFullscreen">
              <el-icon><FullScreen /></el-icon>
              全屏
            </el-button>
            <el-button @click="handleExport">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 系统状态概览 -->
    <div class="system-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="overview-content">
              <div class="overview-icon online">
                <el-icon><Monitor /></el-icon>
              </div>
              <div class="overview-info">
                <div class="overview-value">{{ systemStats.onlineDevices }}</div>
                <div class="overview-label">在线设备</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="overview-content">
              <div class="overview-icon running">
                <el-icon><VideoPlay /></el-icon>
              </div>
              <div class="overview-info">
                <div class="overview-value">{{ systemStats.runningDevices }}</div>
                <div class="overview-label">运行设备</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="overview-content">
              <div class="overview-icon power">
                <el-icon><Lightning /></el-icon>
              </div>
              <div class="overview-info">
                <div class="overview-value">{{ systemStats.totalPower }}</div>
                <div class="overview-label">总功率(kW)</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="overview-content">
              <div class="overview-icon efficiency">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="overview-info">
                <div class="overview-value">{{ systemStats.avgEfficiency }}</div>
                <div class="overview-label">平均能效</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 拓扑图 -->
    <div class="topology-container">
      <el-card class="topology-card">
        <template #header>
          <div class="topology-header">
            <span>{{ getSystemTitle() }}</span>
            <div class="topology-controls">
              <el-button-group size="small">
                <el-button @click="handleZoomIn">
                  <el-icon><ZoomIn /></el-icon>
                </el-button>
                <el-button @click="handleZoomOut">
                  <el-icon><ZoomOut /></el-icon>
                </el-button>
                <el-button @click="handleZoomReset">
                  <el-icon><Aim /></el-icon>
                </el-button>
              </el-button-group>
              <el-switch
                v-model="showRealTimeData"
                active-text="实时数据"
                inactive-text="静态视图"
                style="margin-left: 16px;"
              />
            </div>
          </div>
        </template>

        <!-- 拓扑视图 -->
        <div v-if="viewMode === 'topology'" class="topology-view" :style="{ transform: `scale(${zoomLevel})` }">
          <div class="topology-content">
            <!-- 冷水机组区域 -->
            <div class="device-group chiller-group">
              <div class="group-title">冷水机组</div>
              <div class="device-container">
                <div
                  v-for="chiller in chillerDevices"
                  :key="chiller.id"
                  :class="['device-node', 'chiller-node', getDeviceStatusClass(chiller.status)]"
                  @click="handleDeviceClick(chiller)"
                >
                  <div class="device-icon">
                    <el-icon><Monitor /></el-icon>
                  </div>
                  <div class="device-info">
                    <div class="device-name">{{ chiller.name }}</div>
                    <div v-if="showRealTimeData" class="device-data">
                      <span>{{ chiller.power }}kW</span>
                      <span>{{ chiller.temperature }}°C</span>
                    </div>
                  </div>
                  <div v-if="chiller.hasAlarm" class="device-alarm">
                    <el-icon><Warning /></el-icon>
                  </div>
                </div>
              </div>
            </div>

            <!-- 管道连接 -->
            <div class="pipeline-container">
              <!-- 冷冻水主管 -->
              <div class="pipeline chilled-water-main">
                <div class="pipeline-label">冷冻水主管</div>
                <div class="flow-direction">
                  <div class="flow-arrow"></div>
                  <div class="flow-arrow"></div>
                  <div class="flow-arrow"></div>
                </div>
              </div>
              
              <!-- 冷却水主管 -->
              <div class="pipeline cooling-water-main">
                <div class="pipeline-label">冷却水主管</div>
                <div class="flow-direction">
                  <div class="flow-arrow"></div>
                  <div class="flow-arrow"></div>
                  <div class="flow-arrow"></div>
                </div>
              </div>
            </div>

            <!-- 水泵区域 -->
            <div class="device-group pump-group">
              <div class="group-title">水泵系统</div>
              <div class="device-container">
                <div
                  v-for="pump in pumpDevices"
                  :key="pump.id"
                  :class="['device-node', 'pump-node', getDeviceStatusClass(pump.status)]"
                  @click="handleDeviceClick(pump)"
                >
                  <div class="device-icon">
                    <el-icon><Operation /></el-icon>
                  </div>
                  <div class="device-info">
                    <div class="device-name">{{ pump.name }}</div>
                    <div v-if="showRealTimeData" class="device-data">
                      <span>{{ pump.power }}kW</span>
                      <span>{{ pump.flowRate }}m³/h</span>
                    </div>
                  </div>
                  <div v-if="pump.hasAlarm" class="device-alarm">
                    <el-icon><Warning /></el-icon>
                  </div>
                </div>
              </div>
            </div>

            <!-- 冷却塔区域 -->
            <div class="device-group cooling-tower-group">
              <div class="group-title">冷却塔</div>
              <div class="device-container">
                <div
                  v-for="tower in coolingTowerDevices"
                  :key="tower.id"
                  :class="['device-node', 'tower-node', getDeviceStatusClass(tower.status)]"
                  @click="handleDeviceClick(tower)"
                >
                  <div class="device-icon">
                    <el-icon><Cpu /></el-icon>
                  </div>
                  <div class="device-info">
                    <div class="device-name">{{ tower.name }}</div>
                    <div v-if="showRealTimeData" class="device-data">
                      <span>{{ tower.power }}kW</span>
                      <span>{{ tower.temperature }}°C</span>
                    </div>
                  </div>
                  <div v-if="tower.hasAlarm" class="device-alarm">
                    <el-icon><Warning /></el-icon>
                  </div>
                </div>
              </div>
            </div>

            <!-- 末端区域 -->
            <div class="device-group terminal-group">
              <div class="group-title">末端设备</div>
              <div class="terminal-container">
                <div v-for="terminal in terminalDevices" :key="terminal.id" class="terminal-node">
                  <div class="terminal-icon">
                    <el-icon><House /></el-icon>
                  </div>
                  <div class="terminal-label">{{ terminal.name }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 流程图视图 -->
        <div v-else class="flow-view">
          <div class="flow-content">
            <div class="flow-placeholder">
              <div class="flow-info">
                <h3>{{ getSystemTitle() }}流程图</h3>
                <p>显示系统工作流程和逻辑关系</p>
                <div class="flow-steps">
                  <div class="flow-step">
                    <div class="step-number">1</div>
                    <div class="step-content">
                      <div class="step-title">冷水机组启动</div>
                      <div class="step-desc">根据负荷需求启动相应数量的冷水机组</div>
                    </div>
                  </div>
                  <div class="flow-step">
                    <div class="step-number">2</div>
                    <div class="step-content">
                      <div class="step-title">水泵联动</div>
                      <div class="step-desc">冷冻水泵和冷却水泵按顺序启动</div>
                    </div>
                  </div>
                  <div class="flow-step">
                    <div class="step-number">3</div>
                    <div class="step-content">
                      <div class="step-title">冷却塔运行</div>
                      <div class="step-desc">冷却塔风机根据冷却水温度调节运行</div>
                    </div>
                  </div>
                  <div class="flow-step">
                    <div class="step-number">4</div>
                    <div class="step-content">
                      <div class="step-title">系统优化</div>
                      <div class="step-desc">根据实时数据优化系统运行参数</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 设备详情侧边栏 -->
    <el-drawer v-model="deviceDrawerVisible" :title="selectedDevice?.name" size="400px">
      <div v-if="selectedDevice" class="device-detail">
        <div class="detail-section">
          <h4>基本信息</h4>
          <div class="detail-item">
            <span class="label">设备编号:</span>
            <span class="value">{{ selectedDevice.code }}</span>
          </div>
          <div class="detail-item">
            <span class="label">设备类型:</span>
            <span class="value">{{ selectedDevice.type }}</span>
          </div>
          <div class="detail-item">
            <span class="label">运行状态:</span>
            <el-tag :type="getStatusColor(selectedDevice.status)">
              {{ getStatusText(selectedDevice.status) }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="label">所属区域:</span>
            <span class="value">{{ selectedDevice.area }}</span>
          </div>
        </div>

        <div class="detail-section">
          <h4>实时参数</h4>
          <div class="params-grid">
            <div class="param-card">
              <div class="param-value">{{ selectedDevice.power }}</div>
              <div class="param-label">功率(kW)</div>
            </div>
            <div class="param-card">
              <div class="param-value">{{ selectedDevice.temperature }}</div>
              <div class="param-label">温度(°C)</div>
            </div>
            <div class="param-card">
              <div class="param-value">{{ selectedDevice.pressure || 0.65 }}</div>
              <div class="param-label">压力(MPa)</div>
            </div>
            <div class="param-card">
              <div class="param-value">{{ selectedDevice.flowRate || 180 }}</div>
              <div class="param-label">流量(m³/h)</div>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4>设备控制</h4>
          <div class="control-buttons">
            <el-button type="success" @click="handleDeviceStart">启动</el-button>
            <el-button type="danger" @click="handleDeviceStop">停止</el-button>
            <el-button type="warning" @click="handleDeviceReset">复位</el-button>
          </div>
        </div>

        <div class="detail-section">
          <h4>告警信息</h4>
          <div v-if="selectedDevice.hasAlarm" class="alarm-list">
            <div class="alarm-item">
              <el-icon class="alarm-icon"><Warning /></el-icon>
              <span>高温告警：当前温度超出设定范围</span>
            </div>
          </div>
          <div v-else class="no-alarm">
            <el-icon><CircleCheck /></el-icon>
            <span>设备运行正常</span>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 图例 -->
    <div class="topology-legend">
      <el-card class="legend-card">
        <template #header>
          <span>图例</span>
        </template>
        <div class="legend-content">
          <div class="legend-item">
            <div class="legend-icon running"></div>
            <span>运行</span>
          </div>
          <div class="legend-item">
            <div class="legend-icon stopped"></div>
            <span>停止</span>
          </div>
          <div class="legend-item">
            <div class="legend-icon fault"></div>
            <span>故障</span>
          </div>
          <div class="legend-item">
            <div class="legend-icon maintenance"></div>
            <span>维护</span>
          </div>
        </div>
      </el-card>
    </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Share, Connection, Refresh, FullScreen, Download, Monitor, VideoPlay, Lightning,
  TrendCharts, ZoomIn, ZoomOut, Aim, Warning, Operation, Cpu, House, CircleCheck
} from '@element-plus/icons-vue'

// 响应式数据
const selectedSystem = ref('chilled_water')
const viewMode = ref('topology')
const showRealTimeData = ref(true)
const zoomLevel = ref(1)
const deviceDrawerVisible = ref(false)
const selectedDevice = ref(null)

// 系统统计
const systemStats = reactive({
  onlineDevices: 12,
  runningDevices: 8,
  totalPower: 1285.6,
  avgEfficiency: 4.2
})

// 设备数据
const chillerDevices = ref([
  {
    id: 'CHL-001',
    name: '1号冷水机组',
    code: 'CHL-001',
    type: '冷水机组',
    status: 'running',
    area: '1号冷机房',
    power: 485.6,
    temperature: 7.2,
    hasAlarm: false
  },
  {
    id: 'CHL-002',
    name: '2号冷水机组',
    code: 'CHL-002',
    type: '冷水机组',
    status: 'stopped',
    area: '1号冷机房',
    power: 0,
    temperature: 25.6,
    hasAlarm: false
  }
])

const pumpDevices = ref([
  {
    id: 'CWP-001',
    name: '1号冷冻水泵',
    code: 'CWP-001',
    type: '冷冻水泵',
    status: 'running',
    area: '泵房',
    power: 45.8,
    flowRate: 180,
    hasAlarm: false
  },
  {
    id: 'CWP-002',
    name: '2号冷冻水泵',
    code: 'CWP-002',
    type: '冷冻水泵',
    status: 'running',
    area: '泵房',
    power: 42.3,
    flowRate: 165,
    hasAlarm: false
  }
])

const coolingTowerDevices = ref([
  {
    id: 'CT-001',
    name: '1号冷却塔',
    code: 'CT-001',
    type: '冷却塔',
    status: 'running',
    area: '屋顶',
    power: 35.2,
    temperature: 32.5,
    hasAlarm: true
  },
  {
    id: 'CT-002',
    name: '2号冷却塔',
    code: 'CT-002',
    type: '冷却塔',
    status: 'stopped',
    area: '屋顶',
    power: 0,
    temperature: 28.9,
    hasAlarm: false
  }
])

const terminalDevices = ref([
  { id: 'T-001', name: '1号楼' },
  { id: 'T-002', name: '2号楼' },
  { id: 'T-003', name: '3号楼' },
  { id: 'T-004', name: '地下车库' }
])

// 计算属性和方法
const getSystemTitle = () => {
  const titles = {
    chilled_water: '冷水系统拓扑',
    cooling_water: '冷却水系统拓扑',
    hot_water: '热水系统拓扑',
    integrated: '综合系统拓扑'
  }
  return titles[selectedSystem.value] || '系统拓扑'
}

const getDeviceStatusClass = (status: string) => {
  return `status-${status}`
}

const getStatusColor = (status: string) => {
  const colors = {
    running: 'success',
    stopped: 'info',
    fault: 'danger',
    maintenance: 'warning'
  }
  return colors[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts = {
    running: '运行',
    stopped: '停止',
    fault: '故障',
    maintenance: '维护'
  }
  return texts[status] || status
}

// 事件处理
const handleSystemChange = () => {
  ElMessage.success(`切换到${getSystemTitle()}`)
}

const handleRefresh = () => {
  ElMessage.success('数据已刷新')
}

const handleFullscreen = () => {
  ElMessage.info('全屏功能开发中...')
}

const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

const handleZoomIn = () => {
  if (zoomLevel.value < 2) {
    zoomLevel.value += 0.1
  }
}

const handleZoomOut = () => {
  if (zoomLevel.value > 0.5) {
    zoomLevel.value -= 0.1
  }
}

const handleZoomReset = () => {
  zoomLevel.value = 1
}

const handleDeviceClick = (device: any) => {
  selectedDevice.value = device
  deviceDrawerVisible.value = true
}

const handleDeviceStart = () => {
  ElMessage.success('设备启动指令已发送')
}

const handleDeviceStop = () => {
  ElMessage.success('设备停止指令已发送')
}

const handleDeviceReset = () => {
  ElMessage.success('设备复位指令已发送')
}

onMounted(() => {
  // 初始化拓扑图
})
</script>

<style scoped lang="scss">@use '@/styles/dark-theme.scss';

.app-container {
  padding: 20px;

  .breadcrumb-container {
    margin-bottom: 20px;
  }

  .toolbar {
    margin-bottom: 20px;

    .toolbar-card {
      .toolbar-content {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .toolbar-left {
          display: flex;
          align-items: center;
        }

        .toolbar-right {
          display: flex;
          gap: 8px;
        }
      }
    }
  }

  .system-overview {
    margin-bottom: 20px;

    .overview-card {
      .overview-content {
        display: flex;
        align-items: center;

        .overview-icon {
          width: 50px;
          height: 50px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;

          .el-icon {
            font-size: 20px;
            color: white;
          }

          &.online {
            background: linear-gradient(135deg, #67c23a, #85ce61);
          }

          &.running {
            background: linear-gradient(135deg, #409eff, #66b1ff);
          }

          &.power {
            background: linear-gradient(135deg, #e6a23c, #ebb563);
          }

          &.efficiency {
            background: linear-gradient(135deg, #f56c6c, #f78989);
          }
        }

        .overview-info {
          .overview-value {
            font-size: 24px;
            font-weight: bold;
            color: #303133;
            line-height: 1;
          }

          .overview-label {
            font-size: 14px;
            color: #909399;
            margin-top: 4px;
          }
        }
      }
    }
  }

  .topology-container {
    margin-bottom: 20px;

    .topology-card {
      min-height: 600px;

      .topology-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .topology-controls {
          display: flex;
          align-items: center;
        }
      }

      .topology-view {
        height: 550px;
        overflow: auto;
        transform-origin: center;
        transition: transform 0.3s;

        .topology-content {
          position: relative;
          width: 1200px;
          height: 800px;
          background: #f8f9fa;
          border-radius: 8px;
          padding: 20px;

          .device-group {
            position: absolute;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            background: #1a1a1a;
            padding: 16px;

            .group-title {
              font-size: 14px;
              font-weight: 600;
              color: #303133;
              margin-bottom: 12px;
              text-align: center;
            }

            &.chiller-group {
              top: 50px;
              left: 50px;
              width: 300px;
            }

            &.pump-group {
              top: 250px;
              left: 450px;
              width: 250px;
            }

            &.cooling-tower-group {
              top: 50px;
              right: 50px;
              width: 250px;
            }

            &.terminal-group {
              bottom: 50px;
              left: 50%;
              transform: translateX(-50%);
              width: 400px;
            }
          }

          .device-container {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
          }

          .device-node {
            position: relative;
            width: 120px;
            height: 80px;
            border: 2px solid #d0d0d0;
            border-radius: 6px;
            background: #1a1a1a;
            cursor: pointer;
            transition: all 0.3s;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 8px;

            &:hover {
              transform: translateY(-2px);
              box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            }

            &.status-running {
              border-color: #67c23a;
              background: #f0f9ff;
            }

            &.status-stopped {
              border-color: #909399;
              background: #f5f5f5;
            }

            &.status-fault {
              border-color: #f56c6c;
              background: #fef0f0;
            }

            .device-icon {
              .el-icon {
                font-size: 24px;
                color: #409eff;
              }
            }

            .device-info {
              text-align: center;
              margin-top: 4px;

              .device-name {
                font-size: 12px;
                font-weight: 500;
                color: #303133;
              }

              .device-data {
                font-size: 10px;
                color: #606266;
                margin-top: 2px;

                span {
                  display: block;
                }
              }
            }

            .device-alarm {
              position: absolute;
              top: -5px;
              right: -5px;
              width: 20px;
              height: 20px;
              background: #f56c6c;
              border-radius: 50%;
              display: flex;
              align-items: center;
              justify-content: center;

              .el-icon {
                font-size: 12px;
                color: white;
              }
            }
          }

          .terminal-container {
            display: flex;
            justify-content: space-around;

            .terminal-node {
              text-align: center;

              .terminal-icon {
                width: 40px;
                height: 40px;
                background: #409eff;
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                margin: 0 auto 8px;

                .el-icon {
                  font-size: 20px;
                  color: white;
                }
              }

              .terminal-label {
                font-size: 12px;
                color: #303133;
              }
            }
          }

          .pipeline-container {
            .pipeline {
              position: absolute;
              height: 20px;
              background: linear-gradient(90deg, #409eff, #66b1ff);
              border-radius: 10px;
              display: flex;
              align-items: center;
              justify-content: center;

              &.chilled-water-main {
                top: 200px;
                left: 100px;
                width: 500px;
              }

              &.cooling-water-main {
                top: 150px;
                right: 100px;
                width: 300px;
              }

              .pipeline-label {
                color: white;
                font-size: 12px;
                font-weight: 500;
              }

              .flow-direction {
                position: absolute;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                display: flex;
                align-items: center;
                justify-content: space-around;

                .flow-arrow {
                  width: 0;
                  height: 0;
                  border-left: 8px solid white;
                  border-top: 4px solid transparent;
                  border-bottom: 4px solid transparent;
                  animation: flow 2s infinite;
                }

                @keyframes flow {
                  0% {
                    opacity: 0.3;
                  }
                  50% {
                    opacity: 1;
                  }
                  100% {
                    opacity: 0.3;
                  }
                }
              }
            }
          }
        }
      }

      .flow-view {
        height: 550px;
        display: flex;
        align-items: center;
        justify-content: center;

        .flow-content {
          .flow-placeholder {
            text-align: center;
            background: #f8f9fa;
            border-radius: 8px;
            padding: 40px;
            width: 800px;

            .flow-info {
              h3 {
                margin: 0 0 16px 0;
                color: #303133;
              }

              p {
                margin: 0 0 30px 0;
                color: #909399;
              }

              .flow-steps {
                display: flex;
                flex-direction: column;
                gap: 20px;

                .flow-step {
                  display: flex;
                  align-items: flex-start;
                  text-align: left;

                  .step-number {
                    width: 40px;
                    height: 40px;
                    background: #409eff;
                    color: white;
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-weight: bold;
                    margin-right: 16px;
                    flex-shrink: 0;
                  }

                  .step-content {
                    .step-title {
                      font-size: 16px;
                      font-weight: 600;
                      color: #303133;
                      margin-bottom: 4px;
                    }

                    .step-desc {
                      font-size: 14px;
                      color: #606266;
                      line-height: 1.6;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  .device-detail {
    .detail-section {
      margin-bottom: 24px;

      h4 {
        margin: 0 0 16px 0;
        color: #303133;
        font-size: 16px;
        border-bottom: 1px solid #e0e0e0;
        padding-bottom: 8px;
      }

      .detail-item {
        display: flex;
        justify-content: space-between;
        margin-bottom: 12px;

        .label {
          color: #606266;
          font-size: 14px;
        }

        .value {
          color: #303133;
          font-weight: 500;
        }
      }

      .params-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 12px;

        .param-card {
          text-align: center;
          padding: 16px;
          background: #f8f9fa;
          border-radius: 6px;

          .param-value {
            font-size: 20px;
            font-weight: bold;
            color: #409eff;
            margin-bottom: 4px;
          }

          .param-label {
            font-size: 12px;
            color: #909399;
          }
        }
      }

      .control-buttons {
        display: flex;
        gap: 8px;
      }

      .alarm-list {
        .alarm-item {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 8px;
          background: #fef0f0;
          border-radius: 4px;
          margin-bottom: 8px;

          .alarm-icon {
            color: #f56c6c;
          }
        }
      }

      .no-alarm {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #67c23a;
      }
    }
  }

  .topology-legend {
    position: fixed;
    top: 120px;
    right: 20px;
    width: 120px;

    .legend-card {
      .legend-content {
        .legend-item {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;

          .legend-icon {
            width: 16px;
            height: 16px;
            border-radius: 2px;

            &.running {
              background: #67c23a;
            }

            &.stopped {
              background: #909399;
            }

            &.fault {
              background: #f56c6c;
            }

            &.maintenance {
              background: #e6a23c;
            }
          }

          span {
            font-size: 12px;
            color: #606266;
          }
        }
      }
    }
  }
}
</style>






