<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧能源</el-breadcrumb-item>
        <el-breadcrumb-item>能耗分析</el-breadcrumb-item>
        <el-breadcrumb-item>能耗概览</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 时间选择 -->
    <div class="time-selector">
      <el-radio-group v-model="timeRange" @change="handleTimeRangeChange">
        <el-radio-button label="today">今日</el-radio-button>
        <el-radio-button label="week">本周</el-radio-button>
        <el-radio-button label="month">本月</el-radio-button>
        <el-radio-button label="year">本年</el-radio-button>
        <el-radio-button label="custom">自定义</el-radio-button>
      </el-radio-group>
      <el-date-picker
        v-if="timeRange === 'custom'"
        v-model="customTimeRange"
        type="datetimerange"
        range-separator="至"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        format="YYYY-MM-DD HH:mm"
        value-format="YYYY-MM-DD HH:mm"
        @change="handleCustomTimeChange"
        style="margin-left: 16px;"
      />
    </div>

    <!-- 总览统计卡片 -->
    <div class="overview-stats">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card electricity">
            <div class="card-content">
              <div class="card-icon">
                <el-icon><Lightning /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">总用电量</div>
                <div class="card-value">{{ formatNumber(energyStats.totalElectricity) }}</div>
                <div class="card-unit">kWh</div>
                <div class="card-change">
                  <span :class="energyStats.electricityChange >= 0 ? 'increase' : 'decrease'">
                    {{ energyStats.electricityChange >= 0 ? '+' : '' }}{{ energyStats.electricityChange }}%
                  </span>
                  <span class="change-label">较昨日</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card water">
            <div class="card-content">
              <div class="card-icon">
                <el-icon><Drizzling /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">总用水量</div>
                <div class="card-value">{{ formatNumber(energyStats.totalWater) }}</div>
                <div class="card-unit">m³</div>
                <div class="card-change">
                  <span :class="energyStats.waterChange >= 0 ? 'increase' : 'decrease'">
                    {{ energyStats.waterChange >= 0 ? '+' : '' }}{{ energyStats.waterChange }}%
                  </span>
                  <span class="change-label">较昨日</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card gas">
            <div class="card-content">
              <div class="card-icon">
                <el-icon><Sunny /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">总燃气量</div>
                <div class="card-value">{{ formatNumber(energyStats.totalGas) }}</div>
                <div class="card-unit">m³</div>
                <div class="card-change">
                  <span :class="energyStats.gasChange >= 0 ? 'increase' : 'decrease'">
                    {{ energyStats.gasChange >= 0 ? '+' : '' }}{{ energyStats.gasChange }}%
                  </span>
                  <span class="change-label">较昨日</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card cost">
            <div class="card-content">
              <div class="card-icon">
                <el-icon><Money /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">总费用</div>
                <div class="card-value">{{ formatNumber(energyStats.totalCost) }}</div>
                <div class="card-unit">元</div>
                <div class="card-change">
                  <span :class="energyStats.costChange >= 0 ? 'increase' : 'decrease'">
                    {{ energyStats.costChange >= 0 ? '+' : '' }}{{ energyStats.costChange }}%
                  </span>
                  <span class="change-label">较昨日</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <el-row :gutter="20">
        <!-- 能耗趋势图 -->
        <el-col :span="16">
          <el-card class="chart-card">
            <template #header>
              <div class="chart-header">
                <span>能耗趋势</span>
                <el-radio-group v-model="trendChartType" size="small">
                  <el-radio-button label="electricity">用电</el-radio-button>
                  <el-radio-button label="water">用水</el-radio-button>
                  <el-radio-button label="gas">燃气</el-radio-button>
                  <el-radio-button label="all">全部</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div class="chart-container" id="trend-chart" style="height: 300px;">
              <!-- 这里集成图表组件 -->
              <div class="chart-placeholder">
                <p>能耗趋势图表</p>
                <p>显示{{ getTrendChartTitle() }}趋势变化</p>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 能源结构饼图 -->
        <el-col :span="8">
          <el-card class="chart-card">
            <template #header>
              <span>能源结构</span>
            </template>
            <div class="chart-container" id="structure-chart" style="height: 300px;">
              <div class="pie-chart-placeholder">
                <div class="pie-center">
                  <div class="pie-total">{{ formatNumber(energyStats.totalEnergy) }}</div>
                  <div class="pie-unit">kWh</div>
                  <div class="pie-label">总能耗</div>
                </div>
                <div class="pie-legend">
                  <div v-for="item in energyStructure" :key="item.name" class="legend-item">
                    <div :class="['legend-color', item.name]"></div>
                    <span class="legend-name">{{ item.label }}</span>
                    <span class="legend-value">{{ item.percentage }}%</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 分区能耗排行 -->
    <div class="ranking-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="ranking-card">
            <template #header>
              <div class="ranking-header">
                <span>分区能耗排行</span>
                <el-select v-model="rankingType" size="small" style="width: 120px;">
                  <el-option label="用电量" value="electricity" />
                  <el-option label="用水量" value="water" />
                  <el-option label="燃气量" value="gas" />
                  <el-option label="总费用" value="cost" />
                </el-select>
              </div>
            </template>
            <div class="ranking-list">
              <div v-for="(item, index) in zoneRanking" :key="item.id" class="ranking-item">
                <div class="ranking-number" :class="getRankingClass(index)">
                  {{ index + 1 }}
                </div>
                <div class="ranking-info">
                  <div class="ranking-name">{{ item.name }}</div>
                  <div class="ranking-progress">
                    <el-progress
                      :percentage="item.percentage"
                      :color="getRankingColor(index)"
                      :show-text="false"
                      size="small"
                    />
                  </div>
                </div>
                <div class="ranking-value">
                  {{ formatNumber(item.value) }} {{ getRankingUnit() }}
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 设备状态统计 -->
        <el-col :span="12">
          <el-card class="device-status-card">
            <template #header>
              <span>设备状态统计</span>
            </template>
            <div class="device-status">
              <div class="status-overview">
                <div class="status-item online">
                  <div class="status-icon">
                    <el-icon><Connection /></el-icon>
                  </div>
                  <div class="status-info">
                    <div class="status-count">{{ deviceStatus.online }}</div>
                    <div class="status-label">在线设备</div>
                  </div>
                </div>
                <div class="status-item offline">
                  <div class="status-icon">
                    <el-icon><Remove /></el-icon>
                  </div>
                  <div class="status-info">
                    <div class="status-count">{{ deviceStatus.offline }}</div>
                    <div class="status-label">离线设备</div>
                  </div>
                </div>
                <div class="status-item fault">
                  <div class="status-icon">
                    <el-icon><Warning /></el-icon>
                  </div>
                  <div class="status-info">
                    <div class="status-count">{{ deviceStatus.fault }}</div>
                    <div class="status-label">故障设备</div>
                  </div>
                </div>
                <div class="status-item maintenance">
                  <div class="status-icon">
                    <el-icon><Tools /></el-icon>
                  </div>
                  <div class="status-info">
                    <div class="status-count">{{ deviceStatus.maintenance }}</div>
                    <div class="status-label">维护设备</div>
                  </div>
                </div>
              </div>
              
              <div class="device-alerts">
                <h4>设备告警</h4>
                <div class="alert-list">
                  <div v-for="alert in deviceAlerts" :key="alert.id" class="alert-item">
                    <div class="alert-icon" :class="alert.level">
                      <el-icon><Warning /></el-icon>
                    </div>
                    <div class="alert-content">
                      <div class="alert-message">{{ alert.message }}</div>
                      <div class="alert-time">{{ alert.time }}</div>
                    </div>
                    <div class="alert-actions">
                      <el-button link type="primary" size="small">处理</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 实时数据表格 -->
    <div class="realtime-data">
      <el-card class="data-card">
        <template #header>
          <div class="data-header">
            <span>实时数据</span>
            <div class="header-actions">
              <el-button size="small" @click="handleRefresh">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
              <el-button size="small" @click="handleExport">
                <el-icon><Download /></el-icon>
                导出
              </el-button>
            </div>
          </div>
        </template>
        
        <el-table :data="realtimeData" stripe border style="width: 100%">
          <el-table-column prop="deviceName" label="设备名称" min-width="150" />
          <el-table-column prop="location" label="安装位置" width="120" />
          <el-table-column prop="energyType" label="能源类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getEnergyTypeColor(row.energyType)" size="small">
                {{ getEnergyTypeText(row.energyType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="currentValue" label="当前读数" width="120" />
          <el-table-column prop="instantPower" label="瞬时功率" width="120" />
          <el-table-column prop="todayConsumption" label="今日消耗" width="120" />
          <el-table-column prop="monthConsumption" label="本月消耗" width="120" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="getStatusColor(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="updateTime" label="更新时间" width="160" show-overflow-tooltip />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleViewDevice(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button link type="primary" size="small" @click="handleAnalyzeDevice(row)">
                <el-icon><DataAnalysis /></el-icon>
                分析
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ContentWrap } from '@/components/ContentWrap'
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Lightning, Drizzling, Sunny, Money, Connection, Remove, Warning, Tools,
  Refresh, Download, View, DataAnalysis
} from '@element-plus/icons-vue'

// 响应式数据
const timeRange = ref('today')
const customTimeRange = ref([])
const trendChartType = ref('electricity')
const rankingType = ref('electricity')
const refreshTimer = ref(null)

// 能耗统计数据
const energyStats = reactive({
  totalElectricity: 125680,
  electricityChange: 5.2,
  totalWater: 8960,
  waterChange: -2.1,
  totalGas: 2150,
  gasChange: 1.8,
  totalCost: 89560,
  costChange: 3.7,
  totalEnergy: 136790
})

// 能源结构数据
const energyStructure = ref([
  { name: 'electricity', label: '电力', percentage: 78.5 },
  { name: 'water', label: '用水', percentage: 12.3 },
  { name: 'gas', label: '燃气', percentage: 6.8 },
  { name: 'others', label: '其他', percentage: 2.4 }
])

// 分区排行数据
const zoneRanking = ref([
  { id: '1', name: '1号楼', value: 45680, percentage: 100 },
  { id: '2', name: '2号楼', value: 38920, percentage: 85.2 },
  { id: '3', name: '3号楼', value: 28150, percentage: 61.6 },
  { id: '4', name: '地下车库', value: 13230, percentage: 29.0 },
  { id: '5', name: '食堂', value: 8950, percentage: 19.6 }
])

// 设备状态数据
const deviceStatus = reactive({
  online: 156,
  offline: 12,
  fault: 3,
  maintenance: 5
})

// 设备告警数据
const deviceAlerts = ref([
  {
    id: '1',
    level: 'high',
    message: '1号楼主配电柜负载过高',
    time: '2024-01-22 14:35:20'
  },
  {
    id: '2',
    level: 'medium',
    message: '水表WM-003通信异常',
    time: '2024-01-22 14:28:15'
  },
  {
    id: '3',
    level: 'low',
    message: '燃气表GM-001读数异常',
    time: '2024-01-22 14:15:30'
  }
])

// 实时数据
const realtimeData = ref([
  {
    id: '1',
    deviceName: '主配电柜-MC001',
    location: '1号楼配电室',
    energyType: 'electricity',
    currentValue: '12580.5 kWh',
    instantPower: '285.6 kW',
    todayConsumption: '680.5 kWh',
    monthConsumption: '18560.2 kWh',
    status: 'online',
    updateTime: '2024-01-22 14:35:20'
  },
  {
    id: '2',
    deviceName: '智能水表-WM001',
    location: '水泵房',
    energyType: 'water',
    currentValue: '8960.2 m³',
    instantPower: '12.5 m³/h',
    todayConsumption: '45.8 m³',
    monthConsumption: '1256.3 m³',
    status: 'online',
    updateTime: '2024-01-22 14:35:18'
  },
  {
    id: '3',
    deviceName: '燃气表-GM001',
    location: '食堂厨房',
    energyType: 'gas',
    currentValue: '2150.8 m³',
    instantPower: '5.2 m³/h',
    todayConsumption: '18.5 m³',
    monthConsumption: '485.6 m³',
    status: 'offline',
    updateTime: '2024-01-22 14:20:05'
  }
])

// 格式化数字
const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toLocaleString()
}

// 获取趋势图标题
const getTrendChartTitle = () => {
  const titles = {
    electricity: '用电量',
    water: '用水量',
    gas: '燃气量',
    all: '综合能耗'
  }
  return titles[trendChartType.value] || '能耗'
}

// 获取排行榜样式
const getRankingClass = (index: number) => {
  if (index === 0) return 'first'
  if (index === 1) return 'second'
  if (index === 2) return 'third'
  return 'normal'
}

// 获取排行榜颜色
const getRankingColor = (index: number) => {
  const colors = ['#f56c6c', '#e6a23c', '#409eff', '#67c23a', '#909399']
  return colors[index] || '#909399'
}

// 获取排行榜单位
const getRankingUnit = () => {
  const units = {
    electricity: 'kWh',
    water: 'm³',
    gas: 'm³',
    cost: '元'
  }
  return units[rankingType.value] || ''
}

// 获取能源类型颜色
const getEnergyTypeColor = (type: string) => {
  const colors = {
    electricity: 'primary',
    water: 'success',
    gas: 'warning',
    steam: 'danger'
  }
  return colors[type] || 'info'
}

// 获取能源类型文本
const getEnergyTypeText = (type: string) => {
  const texts = {
    electricity: '电',
    water: '水',
    gas: '燃气',
    steam: '蒸汽'
  }
  return texts[type] || type
}

// 获取状态颜色
const getStatusColor = (status: string) => {
  const colors = {
    online: 'success',
    offline: 'info',
    fault: 'danger',
    maintenance: 'warning'
  }
  return colors[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const texts = {
    online: '在线',
    offline: '离线',
    fault: '故障',
    maintenance: '维护'
  }
  return texts[status] || status
}

// 事件处理
const handleTimeRangeChange = () => {
  loadData()
}

const handleCustomTimeChange = () => {
  loadData()
}

const handleRefresh = () => {
  loadData()
  ElMessage.success('数据已刷新')
}

const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

const handleViewDevice = (row: any) => {
  ElMessage.info(`查看设备: ${row.deviceName}`)
}

const handleAnalyzeDevice = (row: any) => {
  ElMessage.info(`分析设备: ${row.deviceName}`)
}

// 加载数据
const loadData = () => {
  // 模拟数据加载
  console.log('Loading data for:', timeRange.value)
}

// 启动定时刷新
const startAutoRefresh = () => {
  refreshTimer.value = setInterval(() => {
    // 模拟实时数据更新
    realtimeData.value.forEach(item => {
      if (item.status === 'online') {
        // 随机更新瞬时功率
        const baseValue = parseFloat(item.instantPower.split(' ')[0])
        const newValue = (baseValue + (Math.random() - 0.5) * 10).toFixed(1)
        item.instantPower = newValue + ' ' + item.instantPower.split(' ')[1]
        item.updateTime = new Date().toLocaleString('zh-CN')
      }
    })
  }, 30000) // 30秒刷新一次
}

// 停止定时刷新
const stopAutoRefresh = () => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
    refreshTimer.value = null
  }
}

onMounted(() => {
  loadData()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped lang="scss">@use '@/styles/dark-theme.scss';

.app-container {
  padding: 20px;

  .breadcrumb-container {
    margin-bottom: 20px;
  }

  .time-selector {
    margin-bottom: 20px;
    display: flex;
    align-items: center;
  }

  .overview-stats {
    margin-bottom: 20px;

    .stat-card {
      &.electricity {
        border-left: 4px solid #409eff;
      }

      &.water {
        border-left: 4px solid #67c23a;
      }

      &.gas {
        border-left: 4px solid #e6a23c;
      }

      &.cost {
        border-left: 4px solid #f56c6c;
      }

      .card-content {
        display: flex;
        align-items: center;
        gap: 16px;

        .card-icon {
          width: 60px;
          height: 60px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
          color: #fff;
          background: linear-gradient(45deg, #409eff, #66b1ff);
        }

        .card-info {
          flex: 1;

          .card-title {
            font-size: 14px;
            color: #909399;
            margin-bottom: 8px;
          }

          .card-value {
            font-size: 28px;
            font-weight: bold;
            color: #303133;
            line-height: 1;
            margin-bottom: 4px;
          }

          .card-unit {
            font-size: 14px;
            color: #606266;
            margin-bottom: 8px;
          }

          .card-change {
            font-size: 12px;

            .increase {
              color: #f56c6c;
            }

            .decrease {
              color: #67c23a;
            }

            .change-label {
              color: #909399;
              margin-left: 4px;
            }
          }
        }
      }
    }
  }

  .charts-container {
    margin-bottom: 20px;

    .chart-card {
      .chart-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .chart-container {
        .chart-placeholder {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          height: 100%;
          color: #909399;

          p {
            margin: 8px 0;
            font-size: 14px;
          }
        }

        .pie-chart-placeholder {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          height: 100%;

          .pie-center {
            text-align: center;
            margin-bottom: 24px;

            .pie-total {
              font-size: 24px;
              font-weight: bold;
              color: #303133;
            }

            .pie-unit {
              font-size: 14px;
              color: #606266;
            }

            .pie-label {
              font-size: 12px;
              color: #909399;
              margin-top: 4px;
            }
          }

          .pie-legend {
            display: flex;
            flex-direction: column;
            gap: 8px;

            .legend-item {
              display: flex;
              align-items: center;
              gap: 8px;

              .legend-color {
                width: 12px;
                height: 12px;
                border-radius: 2px;

                &.electricity {
                  background: #409eff;
                }

                &.water {
                  background: #67c23a;
                }

                &.gas {
                  background: #e6a23c;
                }

                &.others {
                  background: #909399;
                }
              }

              .legend-name {
                font-size: 12px;
                color: #606266;
                min-width: 40px;
              }

              .legend-value {
                font-size: 12px;
                color: #303133;
                font-weight: 500;
              }
            }
          }
        }
      }
    }
  }

  .ranking-section {
    margin-bottom: 20px;

    .ranking-card {
      .ranking-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .ranking-list {
        .ranking-item {
          display: flex;
          align-items: center;
          padding: 12px 0;
          border-bottom: 1px solid #f0f0f0;

          &:last-child {
            border-bottom: none;
          }

          .ranking-number {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
            color: #fff;
            margin-right: 16px;

            &.first {
              background: #f56c6c;
            }

            &.second {
              background: #e6a23c;
            }

            &.third {
              background: #409eff;
            }

            &.normal {
              background: #909399;
            }
          }

          .ranking-info {
            flex: 1;
            margin-right: 16px;

            .ranking-name {
              font-size: 14px;
              color: #303133;
              margin-bottom: 4px;
            }

            .ranking-progress {
              width: 100%;
            }
          }

          .ranking-value {
            font-size: 14px;
            font-weight: 500;
            color: #303133;
            min-width: 80px;
            text-align: right;
          }
        }
      }
    }

    .device-status-card {
      .device-status {
        .status-overview {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 16px;
          margin-bottom: 24px;

          .status-item {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 16px;
            border-radius: 8px;
            background: #f8f9fa;

            &.online {
              border-left: 4px solid #67c23a;
            }

            &.offline {
              border-left: 4px solid #909399;
            }

            &.fault {
              border-left: 4px solid #f56c6c;
            }

            &.maintenance {
              border-left: 4px solid #e6a23c;
            }

            .status-icon {
              width: 40px;
              height: 40px;
              border-radius: 50%;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 20px;
              color: #fff;
              background: linear-gradient(45deg, #67c23a, #85ce61);
            }

            .status-info {
              .status-count {
                font-size: 20px;
                font-weight: bold;
                color: #303133;
              }

              .status-label {
                font-size: 12px;
                color: #909399;
              }
            }
          }
        }

        .device-alerts {
          h4 {
            margin: 0 0 16px 0;
            color: #303133;
            font-size: 16px;
            font-weight: 600;
            border-left: 3px solid #409eff;
            padding-left: 12px;
          }

          .alert-list {
            .alert-item {
              display: flex;
              align-items: center;
              padding: 12px 0;
              border-bottom: 1px solid #f0f0f0;

              &:last-child {
                border-bottom: none;
              }

              .alert-icon {
                width: 24px;
                height: 24px;
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 12px;
                color: #fff;
                margin-right: 12px;

                &.high {
                  background: #f56c6c;
                }

                &.medium {
                  background: #e6a23c;
                }

                &.low {
                  background: #909399;
                }
              }

              .alert-content {
                flex: 1;

                .alert-message {
                  font-size: 14px;
                  color: #303133;
                  margin-bottom: 4px;
                }

                .alert-time {
                  font-size: 12px;
                  color: #909399;
                }
              }

              .alert-actions {
                margin-left: 12px;
              }
            }
          }
        }
      }
    }
  }

  .realtime-data {
    .data-card {
      .data-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .header-actions {
          display: flex;
          gap: 8px;
        }
      }
    }
  }
}
</style>






