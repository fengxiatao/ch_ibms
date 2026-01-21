<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧能源</el-breadcrumb-item>
        <el-breadcrumb-item>能效分析</el-breadcrumb-item>
        <el-breadcrumb-item>能耗追踪</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 追踪配置 -->
    <div class="tracking-config">
      <el-card class="config-card">
        <template #header>
          <span>追踪配置</span>
        </template>
        <el-form :model="trackingForm" label-width="100px" :inline="true">
          <el-form-item label="追踪对象">
            <el-select v-model="trackingForm.target" placeholder="请选择追踪对象" style="width: 200px;">
              <el-option label="1号楼" value="building_1" />
              <el-option label="2号楼" value="building_2" />
              <el-option label="3号楼" value="building_3" />
              <el-option label="地下车库" value="parking" />
              <el-option label="食堂" value="canteen" />
            </el-select>
          </el-form-item>
          <el-form-item label="能源类型">
            <el-select v-model="trackingForm.energyType" placeholder="请选择能源类型" style="width: 150px;">
              <el-option label="电" value="electricity" />
              <el-option label="水" value="water" />
              <el-option label="燃气" value="gas" />
              <el-option label="蒸汽" value="steam" />
            </el-select>
          </el-form-item>
          <el-form-item label="追踪周期">
            <el-select v-model="trackingForm.period" placeholder="请选择周期" style="width: 120px;">
              <el-option label="实时" value="realtime" />
              <el-option label="小时" value="hour" />
              <el-option label="日" value="day" />
              <el-option label="周" value="week" />
              <el-option label="月" value="month" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleStartTracking">
              <el-icon><VideoPlay /></el-icon>
              开始追踪
            </el-button>
            <el-button type="warning" @click="handleStopTracking" :disabled="!isTracking">
              <el-icon><VideoPause /></el-icon>
              停止追踪
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 实时数据 -->
    <div v-if="isTracking" class="realtime-data">
      <el-row :gutter="20">
        <!-- 实时图表 -->
        <el-col :span="16">
          <el-card class="realtime-chart-card">
            <template #header>
              <div class="chart-header">
                <span>实时能耗追踪</span>
                <div class="chart-status">
                  <el-tag :type="isTracking ? 'success' : 'info'" class="blinking">
                    {{ isTracking ? '追踪中' : '已停止' }}
                  </el-tag>
                  <span class="update-time">更新时间: {{ lastUpdateTime }}</span>
                </div>
              </div>
            </template>
            <div class="chart-container" style="height: 350px;">
              <div class="chart-placeholder">
                <div class="realtime-chart-content">
                  <h3>{{ getTrackingTitle() }}</h3>
                  <div class="current-value">
                    <span class="value">{{ formatNumber(currentValue) }}</span>
                    <span class="unit">{{ getEnergyUnit() }}</span>
                  </div>
                  <div class="trend-indicator">
                    <el-icon v-if="trend > 0" class="trend-up"><CaretTop /></el-icon>
                    <el-icon v-else-if="trend < 0" class="trend-down"><CaretBottom /></el-icon>
                    <el-icon v-else class="trend-stable"><Minus /></el-icon>
                    <span :class="getTrendClass()">{{ getTrendText() }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 实时统计 -->
        <el-col :span="8">
          <el-card class="realtime-stats-card">
            <template #header>
              <span>实时统计</span>
            </template>
            <div class="stats-content">
              <div class="stat-item">
                <div class="stat-label">当前功率</div>
                <div class="stat-value">{{ formatNumber(realtimeStats.power) }}</div>
                <div class="stat-unit">{{ getPowerUnit() }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">今日累计</div>
                <div class="stat-value">{{ formatNumber(realtimeStats.todayTotal) }}</div>
                <div class="stat-unit">{{ getEnergyUnit() }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">本月累计</div>
                <div class="stat-value">{{ formatNumber(realtimeStats.monthTotal) }}</div>
                <div class="stat-unit">{{ getEnergyUnit() }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">平均负载</div>
                <div class="stat-value">{{ realtimeStats.avgLoad }}%</div>
                <el-progress
                  :percentage="realtimeStats.avgLoad"
                  :color="getLoadColor(realtimeStats.avgLoad)"
                  :show-text="false"
                  size="small"
                />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 历史追踪数据 -->
    <div class="historical-tracking">
      <el-card class="historical-card">
        <template #header>
          <div class="historical-header">
            <span>历史追踪记录</span>
            <div class="header-actions">
              <el-date-picker
                v-model="dateRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm"
                @change="loadHistoricalData"
              />
              <el-button size="small" @click="handleExport">
                <el-icon><Download /></el-icon>
                导出
              </el-button>
            </div>
          </div>
        </template>

        <!-- 历史图表 -->
        <div class="historical-chart" style="height: 300px; margin-bottom: 20px;">
          <div class="chart-placeholder">
            <div class="historical-chart-content">
              <h4>历史追踪数据图表</h4>
              <p>显示选定时间范围内的能耗变化趋势</p>
              <div class="chart-stats">
                <span>最大值: {{ formatNumber(historicalStats.max) }} {{ getEnergyUnit() }}</span>
                <span>最小值: {{ formatNumber(historicalStats.min) }} {{ getEnergyUnit() }}</span>
                <span>平均值: {{ formatNumber(historicalStats.avg) }} {{ getEnergyUnit() }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 历史数据表格 -->
        <el-table :data="historicalData" stripe border style="width: 100%" max-height="400">
          <el-table-column prop="time" label="时间" width="160" />
          <el-table-column prop="value" label="能耗值" width="120">
            <template #default="{ row }">
              {{ formatNumber(row.value) }} {{ getEnergyUnit() }}
            </template>
          </el-table-column>
          <el-table-column prop="power" label="功率" width="120">
            <template #default="{ row }">
              {{ formatNumber(row.power) }} {{ getPowerUnit() }}
            </template>
          </el-table-column>
          <el-table-column prop="change" label="变化率" width="100">
            <template #default="{ row }">
              <span :class="row.change >= 0 ? 'positive' : 'negative'">
                {{ row.change >= 0 ? '+' : '' }}{{ row.change }}%
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusColor(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="temperature" label="环境温度" width="100" />
          <el-table-column prop="humidity" label="环境湿度" width="100" />
          <el-table-column prop="weather" label="天气" width="100" />
          <el-table-column prop="remarks" label="备注" min-width="150" show-overflow-tooltip />
        </el-table>

        <!-- 分页 -->
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadHistoricalData"
          @current-change="loadHistoricalData"
          style="margin-top: 20px; text-align: right;"
        />
      </el-card>
    </div>

    <!-- 预警设置 -->
    <div class="alert-settings">
      <el-card class="alert-card">
        <template #header>
          <div class="alert-header">
            <span>预警设置</span>
            <el-switch
              v-model="alertEnabled"
              active-text="启用预警"
              inactive-text="关闭预警"
              @change="handleAlertToggle"
            />
          </div>
        </template>
        
        <el-form :model="alertForm" label-width="120px" :disabled="!alertEnabled">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="上限阈值">
                <el-input-number
                  v-model="alertForm.upperThreshold"
                  :precision="2"
                  :min="0"
                  controls-position="right"
                  style="width: 100%"
                />
                <span style="margin-left: 8px;">{{ getEnergyUnit() }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="下限阈值">
                <el-input-number
                  v-model="alertForm.lowerThreshold"
                  :precision="2"
                  :min="0"
                  controls-position="right"
                  style="width: 100%"
                />
                <span style="margin-left: 8px;">{{ getEnergyUnit() }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="变化率阈值">
                <el-input-number
                  v-model="alertForm.changeThreshold"
                  :precision="1"
                  :min="0"
                  :max="100"
                  controls-position="right"
                  style="width: 100%"
                />
                <span style="margin-left: 8px;">%</span>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="通知方式">
            <el-checkbox-group v-model="alertForm.notificationMethods">
              <el-checkbox label="email">邮件</el-checkbox>
              <el-checkbox label="sms">短信</el-checkbox>
              <el-checkbox label="wechat">微信</el-checkbox>
              <el-checkbox label="system">系统通知</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSaveAlertSettings">
              <el-icon><Setting /></el-icon>
              保存设置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 预警记录 -->
    <div class="alert-records">
      <el-card class="records-card">
        <template #header>
          <div class="records-header">
            <span>预警记录</span>
            <el-button size="small" @click="handleClearAlerts">
              <el-icon><Delete /></el-icon>
              清除记录
            </el-button>
          </div>
        </template>
        
        <el-timeline>
          <el-timeline-item
            v-for="alert in alertRecords"
            :key="alert.id"
            :timestamp="alert.time"
            :type="getAlertType(alert.level)"
          >
            <div class="alert-content">
              <div class="alert-title">{{ alert.title }}</div>
              <div class="alert-message">{{ alert.message }}</div>
              <div class="alert-actions">
                <el-button link type="primary" size="small" @click="handleViewAlert(alert)">查看详情</el-button>
                <el-button link type="success" size="small" @click="handleAckAlert(alert)">确认处理</el-button>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ContentWrap } from '@/components/ContentWrap'
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  VideoPlay, VideoPause, Download, CaretTop, CaretBottom, Minus, Setting, Delete
} from '@element-plus/icons-vue'

// 响应式数据
const isTracking = ref(false)
const alertEnabled = ref(true)
const lastUpdateTime = ref('')
const currentValue = ref(285.6)
const trend = ref(5.2)
const dateRange = ref([])
const trackingInterval = ref(null)

// 追踪表单
const trackingForm = reactive({
  target: 'building_1',
  energyType: 'electricity',
  period: 'realtime'
})

// 实时统计
const realtimeStats = reactive({
  power: 285.6,
  todayTotal: 6850.5,
  monthTotal: 185600.2,
  avgLoad: 68.5
})

// 历史统计
const historicalStats = reactive({
  max: 450.8,
  min: 120.3,
  avg: 285.6
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 预警表单
const alertForm = reactive({
  upperThreshold: 400,
  lowerThreshold: 100,
  changeThreshold: 20,
  notificationMethods: ['email', 'system']
})

// 历史数据
const historicalData = ref([
  {
    time: '2024-01-22 14:30',
    value: 285.6,
    power: 285.6,
    change: 5.2,
    status: 'normal',
    temperature: 15,
    humidity: 65,
    weather: '晴',
    remarks: '正常运行'
  },
  {
    time: '2024-01-22 14:00',
    value: 312.8,
    power: 312.8,
    change: -2.3,
    status: 'normal',
    temperature: 14,
    humidity: 68,
    weather: '晴',
    remarks: '正常运行'
  },
  {
    time: '2024-01-22 13:30',
    value: 295.4,
    power: 295.4,
    change: 8.7,
    status: 'warning',
    temperature: 13,
    humidity: 70,
    weather: '多云',
    remarks: '负载略高'
  }
])

// 预警记录
const alertRecords = ref([
  {
    id: '1',
    level: 'warning',
    title: '能耗超出上限阈值',
    message: '1号楼用电量达到 420.5 kWh，超出设定上限阈值 400 kWh',
    time: '2024-01-22 14:25:30',
    status: 'pending'
  },
  {
    id: '2',
    level: 'info',
    title: '能耗变化率异常',
    message: '1号楼用电量变化率达到 25.8%，超出设定阈值 20%',
    time: '2024-01-22 13:45:15',
    status: 'acknowledged'
  },
  {
    id: '3',
    level: 'success',
    title: '能耗恢复正常',
    message: '1号楼用电量已恢复正常范围',
    time: '2024-01-22 13:20:40',
    status: 'resolved'
  }
])

// 计算属性和方法
const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toLocaleString()
}

const getTrackingTitle = () => {
  const targets = {
    building_1: '1号楼',
    building_2: '2号楼',
    building_3: '3号楼',
    parking: '地下车库',
    canteen: '食堂'
  }
  const energyTypes = {
    electricity: '用电量',
    water: '用水量',
    gas: '燃气量',
    steam: '蒸汽量'
  }
  return `${targets[trackingForm.target]}${energyTypes[trackingForm.energyType]}追踪`
}

const getEnergyUnit = () => {
  const units = {
    electricity: 'kWh',
    water: 'm³',
    gas: 'm³',
    steam: 't'
  }
  return units[trackingForm.energyType] || 'kWh'
}

const getPowerUnit = () => {
  const units = {
    electricity: 'kW',
    water: 'm³/h',
    gas: 'm³/h',
    steam: 't/h'
  }
  return units[trackingForm.energyType] || 'kW'
}

const getTrendClass = () => {
  if (trend.value > 0) return 'trend-up'
  if (trend.value < 0) return 'trend-down'
  return 'trend-stable'
}

const getTrendText = () => {
  if (trend.value > 0) return `上升 ${Math.abs(trend.value)}%`
  if (trend.value < 0) return `下降 ${Math.abs(trend.value)}%`
  return '稳定'
}

const getLoadColor = (load: number) => {
  if (load >= 80) return '#f56c6c'
  if (load >= 60) return '#e6a23c'
  return '#67c23a'
}

const getStatusColor = (status: string) => {
  const colors = {
    normal: 'success',
    warning: 'warning',
    error: 'danger'
  }
  return colors[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts = {
    normal: '正常',
    warning: '警告',
    error: '异常'
  }
  return texts[status] || status
}

const getAlertType = (level: string) => {
  const types = {
    warning: 'warning',
    info: 'primary',
    success: 'success',
    error: 'danger'
  }
  return types[level] || 'primary'
}

// 事件处理
const handleStartTracking = () => {
  isTracking.value = true
  lastUpdateTime.value = new Date().toLocaleString('zh-CN')
  
  // 启动实时数据更新
  trackingInterval.value = setInterval(() => {
    updateRealtimeData()
  }, 5000) // 5秒更新一次
  
  ElMessage.success('开始追踪')
}

const handleStopTracking = () => {
  isTracking.value = false
  
  // 停止实时数据更新
  if (trackingInterval.value) {
    clearInterval(trackingInterval.value)
    trackingInterval.value = null
  }
  
  ElMessage.success('停止追踪')
}

const updateRealtimeData = () => {
  // 模拟实时数据更新
  const baseValue = 285.6
  currentValue.value = baseValue + (Math.random() - 0.5) * 50
  trend.value = (Math.random() - 0.5) * 20
  realtimeStats.power = currentValue.value
  realtimeStats.todayTotal += Math.random() * 10
  lastUpdateTime.value = new Date().toLocaleString('zh-CN')
  
  // 检查预警条件
  if (alertEnabled.value) {
    checkAlerts()
  }
}

const checkAlerts = () => {
  if (currentValue.value > alertForm.upperThreshold) {
    // 触发上限预警
    const alert = {
      id: Date.now().toString(),
      level: 'warning',
      title: '能耗超出上限阈值',
      message: `当前值 ${currentValue.value.toFixed(1)} 超出上限阈值 ${alertForm.upperThreshold}`,
      time: new Date().toLocaleString('zh-CN'),
      status: 'pending'
    }
    alertRecords.value.unshift(alert)
  }
  
  if (Math.abs(trend.value) > alertForm.changeThreshold) {
    // 触发变化率预警
    const alert = {
      id: Date.now().toString(),
      level: 'info',
      title: '能耗变化率异常',
      message: `变化率 ${Math.abs(trend.value).toFixed(1)}% 超出阈值 ${alertForm.changeThreshold}%`,
      time: new Date().toLocaleString('zh-CN'),
      status: 'pending'
    }
    alertRecords.value.unshift(alert)
  }
}

const loadHistoricalData = () => {
  console.log('Loading historical data...', dateRange.value)
  pagination.total = 100 // 模拟总数
}

const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

const handleAlertToggle = () => {
  const statusText = alertEnabled.value ? '启用' : '关闭'
  ElMessage.success(`预警已${statusText}`)
}

const handleSaveAlertSettings = () => {
  ElMessage.success('预警设置已保存')
}

const handleClearAlerts = async () => {
  try {
    await ElMessageBox.confirm('确认清除所有预警记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    alertRecords.value = []
    ElMessage.success('预警记录已清除')
  } catch {
    // 用户取消
  }
}

const handleViewAlert = (alert: any) => {
  ElMessage.info(`查看预警详情: ${alert.title}`)
}

const handleAckAlert = (alert: any) => {
  alert.status = 'acknowledged'
  ElMessage.success('预警已确认处理')
}

onMounted(() => {
  loadHistoricalData()
})

onUnmounted(() => {
  if (trackingInterval.value) {
    clearInterval(trackingInterval.value)
  }
})
</script>

<style scoped lang="scss">@use '@/styles/dark-theme.scss';

.app-container {
  padding: 20px;

  .breadcrumb-container {
    margin-bottom: 20px;
  }

  .tracking-config {
    margin-bottom: 20px;

    .config-card {
      .el-form-item {
        margin-bottom: 16px;
      }
    }
  }

  .realtime-data {
    margin-bottom: 20px;

    .realtime-chart-card {
      .chart-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .chart-status {
          display: flex;
          align-items: center;
          gap: 12px;

          .blinking {
            animation: blink 2s infinite;
          }

          @keyframes blink {
            0%, 50% {
              opacity: 1;
            }
            51%, 100% {
              opacity: 0.3;
            }
          }

          .update-time {
            font-size: 12px;
            color: #909399;
          }
        }
      }

      .chart-container {
        .chart-placeholder {
          display: flex;
          align-items: center;
          justify-content: center;
          height: 100%;
          background: #f8f9fa;
          border-radius: 8px;

          .realtime-chart-content {
            text-align: center;

            h3 {
              margin: 0 0 20px 0;
              color: #303133;
            }

            .current-value {
              margin-bottom: 16px;

              .value {
                font-size: 36px;
                font-weight: bold;
                color: #409eff;
              }

              .unit {
                font-size: 18px;
                color: #606266;
                margin-left: 8px;
              }
            }

            .trend-indicator {
              display: flex;
              align-items: center;
              justify-content: center;
              gap: 6px;

              .trend-up {
                color: #f56c6c;
              }

              .trend-down {
                color: #67c23a;
              }

              .trend-stable {
                color: #909399;
              }
            }
          }
        }
      }
    }

    .realtime-stats-card {
      .stats-content {
        .stat-item {
          margin-bottom: 24px;

          &:last-child {
            margin-bottom: 0;
          }

          .stat-label {
            font-size: 14px;
            color: #909399;
            margin-bottom: 8px;
          }

          .stat-value {
            font-size: 20px;
            font-weight: bold;
            color: #303133;
            margin-bottom: 4px;
          }

          .stat-unit {
            font-size: 12px;
            color: #606266;
            margin-bottom: 8px;
          }
        }
      }
    }
  }

  .historical-tracking {
    margin-bottom: 20px;

    .historical-card {
      .historical-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .header-actions {
          display: flex;
          align-items: center;
          gap: 12px;
        }
      }

      .historical-chart {
        .chart-placeholder {
          display: flex;
          align-items: center;
          justify-content: center;
          height: 100%;
          background: #f8f9fa;
          border-radius: 8px;

          .historical-chart-content {
            text-align: center;

            h4 {
              margin: 0 0 16px 0;
              color: #303133;
            }

            p {
              margin: 0 0 16px 0;
              color: #909399;
              font-size: 14px;
            }

            .chart-stats {
              display: flex;
              justify-content: center;
              gap: 20px;
              font-size: 12px;
              color: #606266;
            }
          }
        }
      }

      .positive {
        color: #f56c6c;
      }

      .negative {
        color: #67c23a;
      }
    }
  }

  .alert-settings {
    margin-bottom: 20px;

    .alert-card {
      .alert-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
    }
  }

  .alert-records {
    .records-card {
      .records-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .alert-content {
        .alert-title {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 8px;
        }

        .alert-message {
          font-size: 14px;
          color: #606266;
          margin-bottom: 12px;
          line-height: 1.6;
        }

        .alert-actions {
          display: flex;
          gap: 8px;
        }
      }
    }
  }
}
</style>






