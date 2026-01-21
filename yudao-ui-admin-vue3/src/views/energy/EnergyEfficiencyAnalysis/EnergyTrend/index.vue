<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧能源</el-breadcrumb-item>
        <el-breadcrumb-item>能效分析</el-breadcrumb-item>
        <el-breadcrumb-item>能耗趋势</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 查询条件 -->
    <div class="search-container">
      <el-form :model="searchForm" label-width="80px" :inline="true">
        <el-form-item label="分析对象">
          <el-select v-model="searchForm.analysisTarget" placeholder="请选择分析对象" style="width: 200px;">
            <el-option label="全部区域" value="all" />
            <el-option label="1号楼" value="building_1" />
            <el-option label="2号楼" value="building_2" />
            <el-option label="3号楼" value="building_3" />
            <el-option label="地下车库" value="parking" />
          </el-select>
        </el-form-item>
        <el-form-item label="能源类型">
          <el-select v-model="searchForm.energyType" placeholder="请选择能源类型" style="width: 150px;">
            <el-option label="全部" value="all" />
            <el-option label="电" value="electricity" />
            <el-option label="水" value="water" />
            <el-option label="燃气" value="gas" />
            <el-option label="蒸汽" value="steam" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm"
            style="width: 300px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 快速时间选择 -->
    <div class="quick-time-selector">
      <el-radio-group v-model="quickTime" @change="handleQuickTimeChange">
        <el-radio-button label="today">今日</el-radio-button>
        <el-radio-button label="yesterday">昨日</el-radio-button>
        <el-radio-button label="week">本周</el-radio-button>
        <el-radio-button label="month">本月</el-radio-button>
        <el-radio-button label="quarter">本季度</el-radio-button>
        <el-radio-button label="year">本年</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 趋势图表 -->
    <div class="charts-container">
      <el-row :gutter="20">
        <!-- 主趋势图 -->
        <el-col :span="18">
          <el-card class="trend-chart-card">
            <template #header>
              <div class="chart-header">
                <span>能耗趋势分析</span>
                <div class="chart-controls">
                  <el-radio-group v-model="chartType" size="small">
                    <el-radio-button label="line">折线图</el-radio-button>
                    <el-radio-button label="bar">柱状图</el-radio-button>
                    <el-radio-button label="area">面积图</el-radio-button>
                  </el-radio-group>
                  <el-select v-model="chartGranularity" size="small" style="width: 100px; margin-left: 12px;">
                    <el-option label="小时" value="hour" />
                    <el-option label="日" value="day" />
                    <el-option label="周" value="week" />
                    <el-option label="月" value="month" />
                  </el-select>
                </div>
              </div>
            </template>
            <div class="chart-container" style="height: 400px;">
              <!-- 这里集成真实的图表组件 -->
              <div class="chart-placeholder">
                <div class="chart-info">
                  <h3>{{ getChartTitle() }}</h3>
                  <div class="chart-legend">
                    <div v-for="item in chartLegend" :key="item.key" class="legend-item">
                      <div :class="['legend-color', item.key]"></div>
                      <span>{{ item.label }}</span>
                    </div>
                  </div>
                  <div class="chart-mock">
                    <p>{{ chartType === 'line' ? '折线' : chartType === 'bar' ? '柱状' : '面积' }}图表显示区域</p>
                    <p>按{{ getGranularityText() }}显示{{ getEnergyTypeText() }}趋势</p>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 统计信息 -->
        <el-col :span="6">
          <el-card class="stats-card">
            <template #header>
              <span>统计信息</span>
            </template>
            <div class="stats-content">
              <div class="stat-item">
                <div class="stat-label">总消耗</div>
                <div class="stat-value">{{ formatNumber(trendStats.total) }}</div>
                <div class="stat-unit">{{ getTrendUnit() }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">平均值</div>
                <div class="stat-value">{{ formatNumber(trendStats.average) }}</div>
                <div class="stat-unit">{{ getTrendUnit() }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">最大值</div>
                <div class="stat-value">{{ formatNumber(trendStats.max) }}</div>
                <div class="stat-unit">{{ getTrendUnit() }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">最小值</div>
                <div class="stat-value">{{ formatNumber(trendStats.min) }}</div>
                <div class="stat-unit">{{ getTrendUnit() }}</div>
              </div>
              <div class="stat-item trend">
                <div class="stat-label">趋势</div>
                <div class="trend-indicator">
                  <el-icon v-if="trendStats.trend > 0" class="trend-up"><CaretTop /></el-icon>
                  <el-icon v-else-if="trendStats.trend < 0" class="trend-down"><CaretBottom /></el-icon>
                  <el-icon v-else class="trend-stable"><Minus /></el-icon>
                  <span :class="getTrendClass()">{{ getTrendText() }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 对比分析 -->
    <div class="comparison-section">
      <el-row :gutter="20">
        <!-- 同比环比 -->
        <el-col :span="12">
          <el-card class="comparison-card">
            <template #header>
              <span>同比环比分析</span>
            </template>
            <div class="comparison-content">
              <div class="comparison-item">
                <div class="comparison-type">同比增长</div>
                <div class="comparison-value">
                  <span :class="comparisonData.yearOverYear >= 0 ? 'positive' : 'negative'">
                    {{ comparisonData.yearOverYear >= 0 ? '+' : '' }}{{ comparisonData.yearOverYear }}%
                  </span>
                </div>
                <div class="comparison-desc">与去年同期相比</div>
              </div>
              <div class="comparison-item">
                <div class="comparison-type">环比增长</div>
                <div class="comparison-value">
                  <span :class="comparisonData.monthOverMonth >= 0 ? 'positive' : 'negative'">
                    {{ comparisonData.monthOverMonth >= 0 ? '+' : '' }}{{ comparisonData.monthOverMonth }}%
                  </span>
                </div>
                <div class="comparison-desc">与上月相比</div>
              </div>
              <div class="comparison-item">
                <div class="comparison-type">日均增长</div>
                <div class="comparison-value">
                  <span :class="comparisonData.dayOverDay >= 0 ? 'positive' : 'negative'">
                    {{ comparisonData.dayOverDay >= 0 ? '+' : '' }}{{ comparisonData.dayOverDay }}%
                  </span>
                </div>
                <div class="comparison-desc">与昨日相比</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 峰值分析 -->
        <el-col :span="12">
          <el-card class="peak-analysis-card">
            <template #header>
              <span>峰值分析</span>
            </template>
            <div class="peak-content">
              <div class="peak-item">
                <div class="peak-label">日峰值时段</div>
                <div class="peak-time">{{ peakAnalysis.dailyPeakTime }}</div>
                <div class="peak-value">{{ formatNumber(peakAnalysis.dailyPeakValue) }} {{ getTrendUnit() }}</div>
              </div>
              <div class="peak-item">
                <div class="peak-label">周峰值时段</div>
                <div class="peak-time">{{ peakAnalysis.weeklyPeakTime }}</div>
                <div class="peak-value">{{ formatNumber(peakAnalysis.weeklyPeakValue) }} {{ getTrendUnit() }}</div>
              </div>
              <div class="peak-item">
                <div class="peak-label">月峰值时段</div>
                <div class="peak-time">{{ peakAnalysis.monthlyPeakTime }}</div>
                <div class="peak-value">{{ formatNumber(peakAnalysis.monthlyPeakValue) }} {{ getTrendUnit() }}</div>
              </div>
              <div class="peak-item">
                <div class="peak-label">负荷率</div>
                <div class="peak-value">{{ peakAnalysis.loadFactor }}%</div>
                <el-progress
                  :percentage="peakAnalysis.loadFactor"
                  :color="getLoadFactorColor(peakAnalysis.loadFactor)"
                  :show-text="false"
                  size="small"
                />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 预测分析 -->
    <div class="forecast-section">
      <el-card class="forecast-card">
        <template #header>
          <div class="forecast-header">
            <span>预测分析</span>
            <div class="forecast-controls">
              <el-select v-model="forecastPeriod" size="small" style="width: 120px;">
                <el-option label="未来7天" value="7days" />
                <el-option label="未来30天" value="30days" />
                <el-option label="未来3个月" value="3months" />
                <el-option label="未来1年" value="1year" />
              </el-select>
              <el-button size="small" type="primary" @click="handleForecast" style="margin-left: 12px;">
                <el-icon><DataAnalysis /></el-icon>
                生成预测
              </el-button>
            </div>
          </div>
        </template>
        <div class="forecast-content">
          <div class="forecast-chart" style="height: 300px;">
            <div class="forecast-placeholder">
              <div class="forecast-info">
                <h4>{{ getForecastTitle() }}</h4>
                <div class="forecast-stats">
                  <div class="forecast-stat">
                    <span class="stat-label">预测总量:</span>
                    <span class="stat-value">{{ formatNumber(forecastData.total) }} {{ getTrendUnit() }}</span>
                  </div>
                  <div class="forecast-stat">
                    <span class="stat-label">预测趋势:</span>
                    <span :class="['stat-value', forecastData.trend >= 0 ? 'positive' : 'negative']">
                      {{ forecastData.trend >= 0 ? '上升' : '下降' }} {{ Math.abs(forecastData.trend) }}%
                    </span>
                  </div>
                  <div class="forecast-stat">
                    <span class="stat-label">置信度:</span>
                    <span class="stat-value">{{ forecastData.confidence }}%</span>
                  </div>
                </div>
                <div class="forecast-chart-mock">
                  <p>预测图表显示区域</p>
                  <p>历史数据 + 预测数据趋势线</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 数据表格 -->
    <div class="data-table-section">
      <el-card class="table-card">
        <template #header>
          <div class="table-header">
            <span>详细数据</span>
            <div class="table-actions">
              <el-button size="small" @click="handleRefresh">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
              <el-button size="small" @click="handleExportTable">
                <el-icon><Download /></el-icon>
                导出表格
              </el-button>
            </div>
          </div>
        </template>
        
        <el-table :data="trendData" stripe border style="width: 100%" max-height="400">
          <el-table-column prop="time" label="时间" width="180" />
          <el-table-column prop="electricity" label="用电量(kWh)" width="120" />
          <el-table-column prop="water" label="用水量(m³)" width="120" />
          <el-table-column prop="gas" label="燃气量(m³)" width="120" />
          <el-table-column prop="total" label="总能耗" width="120" />
          <el-table-column prop="cost" label="费用(元)" width="120" />
          <el-table-column prop="efficiency" label="能效比" width="100" />
          <el-table-column prop="carbonEmission" label="碳排放(kg)" width="120" />
          <el-table-column prop="weather" label="天气" width="100" />
          <el-table-column prop="temperature" label="温度(°C)" width="100" />
        </el-table>

        <!-- 分页 -->
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
          style="margin-top: 20px; text-align: right;"
        />
      </el-card>
    </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ContentWrap } from '@/components/ContentWrap'
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search, Refresh, Download, DataAnalysis, CaretTop, CaretBottom, Minus
} from '@element-plus/icons-vue'

// 响应式数据
const quickTime = ref('today')
const chartType = ref('line')
const chartGranularity = ref('hour')
const forecastPeriod = ref('7days')

// 搜索表单
const searchForm = reactive({
  analysisTarget: 'all',
  energyType: 'all',
  timeRange: []
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 趋势统计数据
const trendStats = reactive({
  total: 125680,
  average: 5236,
  max: 8950,
  min: 2150,
  trend: 5.2
})

// 对比数据
const comparisonData = reactive({
  yearOverYear: 8.5,
  monthOverMonth: -2.3,
  dayOverDay: 1.8
})

// 峰值分析数据
const peakAnalysis = reactive({
  dailyPeakTime: '14:00-15:00',
  dailyPeakValue: 8950,
  weeklyPeakTime: '周二',
  weeklyPeakValue: 45680,
  monthlyPeakTime: '15日',
  monthlyPeakValue: 185600,
  loadFactor: 72.5
})

// 预测数据
const forecastData = reactive({
  total: 89560,
  trend: 3.2,
  confidence: 85
})

// 图表图例
const chartLegend = ref([
  { key: 'electricity', label: '用电量' },
  { key: 'water', label: '用水量' },
  { key: 'gas', label: '燃气量' }
])

// 趋势数据
const trendData = ref([
  {
    time: '2024-01-22 14:00',
    electricity: 285.6,
    water: 12.5,
    gas: 5.2,
    total: 303.3,
    cost: 256.8,
    efficiency: 0.85,
    carbonEmission: 142.5,
    weather: '晴',
    temperature: 15
  },
  {
    time: '2024-01-22 13:00',
    electricity: 312.8,
    water: 15.2,
    gas: 6.8,
    total: 334.8,
    cost: 285.6,
    efficiency: 0.82,
    carbonEmission: 156.8,
    weather: '晴',
    temperature: 14
  },
  // 更多数据...
])

// 计算属性和方法
const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toLocaleString()
}

const getChartTitle = () => {
  const targetText = searchForm.analysisTarget === 'all' ? '全部区域' : '选定区域'
  const energyText = getEnergyTypeText()
  return `${targetText}${energyText}趋势`
}

const getEnergyTypeText = () => {
  const texts = {
    all: '综合能耗',
    electricity: '用电量',
    water: '用水量',
    gas: '燃气量',
    steam: '蒸汽量'
  }
  return texts[searchForm.energyType] || '能耗'
}

const getGranularityText = () => {
  const texts = {
    hour: '小时',
    day: '天',
    week: '周',
    month: '月'
  }
  return texts[chartGranularity.value] || '时间'
}

const getTrendUnit = () => {
  if (searchForm.energyType === 'electricity') return 'kWh'
  if (searchForm.energyType === 'water') return 'm³'
  if (searchForm.energyType === 'gas') return 'm³'
  if (searchForm.energyType === 'steam') return 't'
  return 'kWh'
}

const getTrendClass = () => {
  if (trendStats.trend > 0) return 'trend-up'
  if (trendStats.trend < 0) return 'trend-down'
  return 'trend-stable'
}

const getTrendText = () => {
  if (trendStats.trend > 0) return `上升 ${Math.abs(trendStats.trend)}%`
  if (trendStats.trend < 0) return `下降 ${Math.abs(trendStats.trend)}%`
  return '稳定'
}

const getLoadFactorColor = (value: number) => {
  if (value >= 80) return '#f56c6c'
  if (value >= 60) return '#e6a23c'
  return '#67c23a'
}

const getForecastTitle = () => {
  const periods = {
    '7days': '未来7天',
    '30days': '未来30天',
    '3months': '未来3个月',
    '1year': '未来1年'
  }
  return `${periods[forecastPeriod.value]}${getEnergyTypeText()}预测`
}

// 事件处理
const handleSearch = () => {
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    analysisTarget: 'all',
    energyType: 'all',
    timeRange: []
  })
  quickTime.value = 'today'
  loadData()
}

const handleQuickTimeChange = () => {
  // 根据快速时间选择设置时间范围
  const now = new Date()
  let startTime, endTime
  
  switch (quickTime.value) {
    case 'today':
      startTime = new Date(now.getFullYear(), now.getMonth(), now.getDate())
      endTime = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59)
      break
    case 'yesterday':
      startTime = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 1)
      endTime = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 1, 23, 59, 59)
      break
    case 'week':
      const dayOfWeek = now.getDay()
      startTime = new Date(now.getFullYear(), now.getMonth(), now.getDate() - dayOfWeek + 1)
      endTime = now
      break
    case 'month':
      startTime = new Date(now.getFullYear(), now.getMonth(), 1)
      endTime = now
      break
    case 'quarter':
      const quarter = Math.floor(now.getMonth() / 3)
      startTime = new Date(now.getFullYear(), quarter * 3, 1)
      endTime = now
      break
    case 'year':
      startTime = new Date(now.getFullYear(), 0, 1)
      endTime = now
      break
  }
  
  searchForm.timeRange = [
    startTime.toISOString().slice(0, 16).replace('T', ' '),
    endTime.toISOString().slice(0, 16).replace('T', ' ')
  ]
  
  loadData()
}

const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

const handleForecast = () => {
  ElMessage.success('正在生成预测数据...')
  // 模拟预测计算
  setTimeout(() => {
    ElMessage.success('预测分析完成')
  }, 2000)
}

const handleRefresh = () => {
  loadData()
  ElMessage.success('数据已刷新')
}

const handleExportTable = () => {
  ElMessage.success('表格导出功能开发中...')
}

const loadData = () => {
  console.log('Loading trend data...', searchForm)
  pagination.total = 100 // 模拟总数
}

onMounted(() => {
  handleQuickTimeChange() // 初始化时间范围
})
</script>

<style scoped lang="scss">@use '@/styles/dark-theme.scss';

.app-container {
  padding: 20px;

  .breadcrumb-container {
    margin-bottom: 20px;
  }

  .search-container {
    background: #1a1a1a;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    margin-bottom: 20px;

    .el-form-item {
      margin-bottom: 16px;
    }
  }

  .quick-time-selector {
    margin-bottom: 20px;
  }

  .charts-container {
    margin-bottom: 20px;

    .trend-chart-card {
      .chart-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .chart-controls {
          display: flex;
          align-items: center;
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

          .chart-info {
            text-align: center;

            h3 {
              margin: 0 0 16px 0;
              color: #303133;
            }

            .chart-legend {
              display: flex;
              justify-content: center;
              gap: 20px;
              margin-bottom: 20px;

              .legend-item {
                display: flex;
                align-items: center;
                gap: 6px;

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
                }
              }
            }

            .chart-mock {
              color: #909399;

              p {
                margin: 8px 0;
                font-size: 14px;
              }
            }
          }
        }
      }
    }

    .stats-card {
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
            font-size: 24px;
            font-weight: bold;
            color: #303133;
            margin-bottom: 4px;
          }

          .stat-unit {
            font-size: 12px;
            color: #606266;
          }

          &.trend {
            .trend-indicator {
              display: flex;
              align-items: center;
              gap: 4px;

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
  }

  .comparison-section {
    margin-bottom: 20px;

    .comparison-card {
      .comparison-content {
        display: flex;
        flex-direction: column;
        gap: 20px;

        .comparison-item {
          text-align: center;
          padding: 16px;
          border-radius: 8px;
          background: #f8f9fa;

          .comparison-type {
            font-size: 14px;
            color: #606266;
            margin-bottom: 8px;
          }

          .comparison-value {
            font-size: 20px;
            font-weight: bold;
            margin-bottom: 4px;

            .positive {
              color: #f56c6c;
            }

            .negative {
              color: #67c23a;
            }
          }

          .comparison-desc {
            font-size: 12px;
            color: #909399;
          }
        }
      }
    }

    .peak-analysis-card {
      .peak-content {
        .peak-item {
          margin-bottom: 20px;

          &:last-child {
            margin-bottom: 0;
          }

          .peak-label {
            font-size: 14px;
            color: #606266;
            margin-bottom: 8px;
          }

          .peak-time {
            font-size: 16px;
            font-weight: 500;
            color: #303133;
            margin-bottom: 4px;
          }

          .peak-value {
            font-size: 14px;
            color: #409eff;
            font-weight: 500;
            margin-bottom: 8px;
          }
        }
      }
    }
  }

  .forecast-section {
    margin-bottom: 20px;

    .forecast-card {
      .forecast-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .forecast-controls {
          display: flex;
          align-items: center;
        }
      }

      .forecast-content {
        .forecast-chart {
          .forecast-placeholder {
            display: flex;
            align-items: center;
            justify-content: center;
            height: 100%;
            background: #f8f9fa;
            border-radius: 8px;

            .forecast-info {
              text-align: center;

              h4 {
                margin: 0 0 16px 0;
                color: #303133;
              }

              .forecast-stats {
                display: flex;
                justify-content: center;
                gap: 40px;
                margin-bottom: 20px;

                .forecast-stat {
                  display: flex;
                  flex-direction: column;
                  gap: 4px;

                  .stat-label {
                    font-size: 12px;
                    color: #909399;
                  }

                  .stat-value {
                    font-size: 16px;
                    font-weight: 500;
                    color: #303133;

                    &.positive {
                      color: #f56c6c;
                    }

                    &.negative {
                      color: #67c23a;
                    }
                  }
                }
              }

              .forecast-chart-mock {
                color: #909399;

                p {
                  margin: 8px 0;
                  font-size: 14px;
                }
              }
            }
          }
        }
      }
    }
  }

  .data-table-section {
    .table-card {
      .table-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .table-actions {
          display: flex;
          gap: 8px;
        }
      }
    }
  }
}
</style>






