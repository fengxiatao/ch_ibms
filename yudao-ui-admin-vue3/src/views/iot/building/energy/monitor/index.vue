<template>
  <div class="energy-monitor-page dark-theme-page">
    <!-- 能耗统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card electric">
        <div class="stat-icon"><Icon icon="mdi:flash" /></div>
        <div class="stat-content">
          <div class="stat-label">今日用电</div>
          <div class="stat-value">{{ formatNumber(overview.todayElectricity) }}<span class="stat-unit">kWh</span></div>
          <div class="stat-change" :class="getChangeClass(overview.electricityMom)">
            同比昨日 {{ getChangeText(overview.electricityMom) }}
          </div>
        </div>
      </div>
      <div class="stat-card water">
        <div class="stat-icon"><Icon icon="mdi:water" /></div>
        <div class="stat-content">
          <div class="stat-label">今日用水</div>
          <div class="stat-value">{{ formatNumber(overview.todayWater) }}<span class="stat-unit">m³</span></div>
          <div class="stat-change" :class="getChangeClass(overview.waterMom)">
            同比昨日 {{ getChangeText(overview.waterMom) }}
          </div>
        </div>
      </div>
      <div class="stat-card gas">
        <div class="stat-icon"><Icon icon="mdi:fire" /></div>
        <div class="stat-content">
          <div class="stat-label">今日用气</div>
          <div class="stat-value">{{ formatNumber(overview.todayGas) }}<span class="stat-unit">m³</span></div>
          <div class="stat-change" :class="getChangeClass(0)">
            同比昨日 {{ getChangeText(0) }}
          </div>
        </div>
      </div>
    </div>

    <!-- 仪表卡片网格 -->
    <div class="meter-section">
      <div class="section-header">
        <span class="section-title">实时监测点</span>
        <div class="section-actions">
          <el-select v-model="filterType" placeholder="全部类型" clearable class="filter-select">
            <el-option label="全部类型" :value="undefined" />
            <el-option label="电表" :value="1" />
            <el-option label="水表" :value="2" />
            <el-option label="燃气表" :value="3" />
          </el-select>
          <el-select v-model="filterStatus" placeholder="全部状态" clearable class="filter-select">
            <el-option label="全部状态" :value="undefined" />
            <el-option label="在线" :value="1" />
            <el-option label="离线" :value="0" />
            <el-option label="告警" :value="2" />
          </el-select>
        </div>
      </div>
      
      <div v-loading="loading" class="meter-grid">
        <div
          v-for="meter in filteredMeterList"
          :key="meter.id"
          class="meter-card"
          :class="getMeterCardClass(meter)"
          @click="openMeterDetail(meter)"
        >
          <div class="meter-header">
            <span class="meter-name">{{ meter.meterName }}</span>
            <span class="meter-type-tag" :class="getMeterTypeClass(meter.meterType)">
              {{ getMeterTypeName(meter.meterType) }}
            </span>
          </div>
          <div class="meter-id">{{ meter.meterCode }}</div>
          
          <div class="meter-body">
            <div class="meter-param">
              <div class="meter-value" :class="{ warning: meter.status === 2 }">
                {{ formatReading(meter.currentReading, meter.meterType) }}
              </div>
              <div class="meter-label">当前读数</div>
            </div>
            <div class="meter-param">
              <div class="meter-value">
                {{ formatUsage(meter.todayUsage, meter.meterType) }}
              </div>
              <div class="meter-label">今日用量</div>
            </div>
          </div>
          
          <div class="meter-footer">
            <span class="meter-status" :class="getStatusClass(meter.status)">
              <span class="status-dot"></span>
              {{ getStatusLabel(meter.status) }}
            </span>
            <span class="meter-time">{{ formatTime(meter.lastCommunicateTime) }}</span>
          </div>
        </div>
        
        <el-empty v-if="filteredMeterList.length === 0 && !loading" description="暂无数据" />
      </div>
    </div>

    <!-- 仪表详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="currentMeter?.meterName || '仪表详情'"
      width="650px"
      class="dark-dialog"
      destroy-on-close
    >
      <div v-if="currentMeter" class="meter-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="仪表编码">{{ currentMeter.meterCode }}</el-descriptions-item>
          <el-descriptions-item label="仪表名称">{{ currentMeter.meterName }}</el-descriptions-item>
          <el-descriptions-item label="仪表类型">
            <el-tag :type="getMeterTypeTagType(currentMeter.meterType)" size="small">
              {{ getMeterTypeName(currentMeter.meterType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(currentMeter.status)" size="small">
              {{ getStatusLabel(currentMeter.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="所属区域">{{ currentMeter.areaName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="安装位置">{{ currentMeter.installLocation || '-' }}</el-descriptions-item>
          <el-descriptions-item label="当前读数">{{ formatReading(currentMeter.currentReading, currentMeter.meterType) }}</el-descriptions-item>
          <el-descriptions-item label="上期读数">{{ formatReading(currentMeter.lastReading, currentMeter.meterType) }}</el-descriptions-item>
          <el-descriptions-item label="今日用量">{{ formatUsage(currentMeter.todayUsage, currentMeter.meterType) }}</el-descriptions-item>
          <el-descriptions-item label="本月用量">{{ formatUsage(currentMeter.monthUsage, currentMeter.meterType) }}</el-descriptions-item>
          <el-descriptions-item label="最后通讯时间" :span="2">{{ formatTime(currentMeter.lastCommunicateTime) || '-' }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- 近期趋势图 -->
        <div class="detail-chart-section">
          <div class="chart-title">近7天用量趋势</div>
          <div ref="detailChartRef" class="detail-chart"></div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import * as EnergyApi from '@/api/iot/building/energy'
import type { IbmsEnergyMeterVO, IbmsEnergyOverviewVO } from '@/api/iot/building/energy'
import * as echarts from 'echarts'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'BuildingEnergyMonitor' })

// 数据
const loading = ref(false)
const overview = ref<IbmsEnergyOverviewVO>({})
const meterList = ref<IbmsEnergyMeterVO[]>([])

// 筛选
const filterType = ref<number | undefined>(undefined)
const filterStatus = ref<number | undefined>(undefined)

// 弹窗
const detailDialogVisible = ref(false)
const currentMeter = ref<IbmsEnergyMeterVO | null>(null)
const detailChartRef = ref<HTMLElement>()
let detailChartInstance: echarts.ECharts | null = null

// 计算属性：过滤后的仪表列表
const filteredMeterList = computed(() => {
  return meterList.value.filter(meter => {
    if (filterType.value !== undefined && meter.meterType !== filterType.value) return false
    if (filterStatus.value !== undefined && meter.status !== filterStatus.value) return false
    return true
  })
})

// 工具函数
const formatNumber = (value?: number) => {
  if (value === undefined || value === null) return '0'
  return value.toLocaleString('zh-CN', { maximumFractionDigits: 1 })
}

const formatReading = (value?: number, type?: number) => {
  if (value === undefined || value === null) return '--'
  const unit = type === 1 ? '' : ''
  return value.toLocaleString('zh-CN', { maximumFractionDigits: 1 }) + unit
}

const formatUsage = (value?: number, type?: number) => {
  if (value === undefined || value === null) return '--'
  const unitMap: Record<number, string> = { 1: 'kWh', 2: 'm³', 3: 'm³', 4: 'kWh', 5: 'kWh' }
  return value.toLocaleString('zh-CN', { maximumFractionDigits: 2 }) + ' ' + (unitMap[type || 1] || '')
}

const formatTime = (time?: Date | string) => {
  if (!time) return ''
  return formatDate(time, 'HH:mm:ss')
}

const getChangeClass = (value?: number) => {
  if (!value) return 'neutral'
  return value > 0 ? 'up' : 'down'
}

const getChangeText = (value?: number) => {
  if (!value) return '持平'
  return value > 0 ? `+${value}%` : `${value}%`
}

const getMeterTypeName = (type?: number) => {
  const map: Record<number, string> = { 1: '电表', 2: '水表', 3: '燃气表', 4: '冷量表', 5: '热量表' }
  return map[type || 0] || '未知'
}

const getMeterTypeClass = (type?: number) => {
  const map: Record<number, string> = { 1: 'type-electric', 2: 'type-water', 3: 'type-gas', 4: 'type-cold', 5: 'type-heat' }
  return map[type || 0] || ''
}

const getMeterTypeTagType = (type?: number) => {
  const map: Record<number, string> = { 1: 'warning', 2: 'success', 3: '', 4: 'info', 5: 'danger' }
  return map[type || 0] || 'info'
}

const getStatusLabel = (status?: number) => {
  const map: Record<number, string> = { 0: '离线', 1: '在线', 2: '告警' }
  return map[status ?? 0] || '未知'
}

const getStatusClass = (status?: number) => {
  const map: Record<number, string> = { 0: 'offline', 1: 'online', 2: 'warning' }
  return map[status ?? 0] || ''
}

const getStatusTagType = (status?: number) => {
  const map: Record<number, string> = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status ?? 0] || 'info'
}

const getMeterCardClass = (meter: IbmsEnergyMeterVO) => {
  if (meter.status === 2) return 'danger'
  if (meter.status === 0) return 'offline'
  return ''
}

// 数据加载
const loadOverview = async () => {
  try {
    overview.value = await EnergyApi.getOverview()
  } catch (e) {
    console.error('加载总览数据失败', e)
  }
}

const loadMeterList = async () => {
  loading.value = true
  try {
    const data = await EnergyApi.getMeterList()
    meterList.value = data
  } catch (e) {
    console.error('加载仪表列表失败', e)
  } finally {
    loading.value = false
  }
}

// 打开详情弹窗
const openMeterDetail = async (meter: IbmsEnergyMeterVO) => {
  currentMeter.value = meter
  detailDialogVisible.value = true
  
  await nextTick()
  loadDetailChart()
}

// 加载详情图表
const loadDetailChart = async () => {
  if (!detailChartRef.value || !currentMeter.value) return
  
  try {
    const endDate = new Date().toISOString().split('T')[0]
    const startDate = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
    const data = await EnergyApi.getMeterStatisticsByDateRange(currentMeter.value.id!, startDate, endDate)
    
    if (!detailChartInstance) {
      detailChartInstance = echarts.init(detailChartRef.value)
    }
    
    const dates = data.map(item => item.statisticsDate).sort()
    const usageData = dates.map(date => {
      const item = data.find(d => d.statisticsDate === date)
      return item?.dailyUsage || 0
    })
    
    detailChartInstance.setOption({
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(30, 30, 30, 0.9)',
        borderColor: '#404040',
        textStyle: { color: '#fff' }
      },
      grid: { top: 20, right: 20, bottom: 30, left: 50 },
      xAxis: {
        type: 'category',
        data: dates,
        axisLine: { lineStyle: { color: '#404040' } },
        axisLabel: { color: '#aaa' }
      },
      yAxis: {
        type: 'value',
        axisLine: { lineStyle: { color: '#404040' } },
        axisLabel: { color: '#aaa' },
        splitLine: { lineStyle: { color: '#333' } }
      },
      series: [{
        name: '用量',
        type: 'bar',
        data: usageData,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#10b981' },
            { offset: 1, color: '#059669' }
          ])
        }
      }]
    })
  } catch (e) {
    console.error('加载详情图表失败', e)
  }
}

// 监听弹窗关闭
watch(detailDialogVisible, (visible) => {
  if (!visible && detailChartInstance) {
    detailChartInstance.dispose()
    detailChartInstance = null
  }
})

// 生命周期
onMounted(() => {
  loadOverview()
  loadMeterList()
})

onUnmounted(() => {
  detailChartInstance?.dispose()
})
</script>

<style lang="scss" scoped>
@use '@/styles/dark-theme.scss' as *;

.energy-monitor-page {
  padding: 20px;
  padding-top: calc(
    20px + max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 20px)))
  );
  height: 100%;
  box-sizing: border-box;
  overflow: auto;
  background: #1a1a2e;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: linear-gradient(135deg, #2d2d3a 0%, #252532 100%);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #3a3a4a;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
  }
  
  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: #fff;
  }
  
  &.electric .stat-icon {
    background: linear-gradient(135deg, #f59e0b, #d97706);
  }
  
  &.water .stat-icon {
    background: linear-gradient(135deg, #06b6d4, #0891b2);
  }
  
  &.gas .stat-icon {
    background: linear-gradient(135deg, #f97316, #ea580c);
  }
  
  .stat-content {
    flex: 1;
    
    .stat-label {
      font-size: 13px;
      color: #a0a0b0;
      margin-bottom: 4px;
    }
    
    .stat-value {
      font-size: 28px;
      font-weight: 700;
      color: #ffffff;
      font-family: 'Courier New', monospace;
      
      .stat-unit {
        font-size: 14px;
        font-weight: normal;
        color: #808090;
        margin-left: 4px;
      }
    }
    
    .stat-change {
      font-size: 12px;
      margin-top: 4px;
      padding: 2px 8px;
      border-radius: 12px;
      display: inline-block;
      
      &.up {
        background: rgba(239, 68, 68, 0.2);
        color: #f87171;
      }
      
      &.down {
        background: rgba(34, 197, 94, 0.2);
        color: #4ade80;
      }
      
      &.neutral {
        background: rgba(148, 163, 184, 0.2);
        color: #94a3b8;
      }
    }
  }
}

.meter-section {
  background: #252532;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #3a3a4a;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #3a3a4a;
    
    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #ffffff;
    }
    
    .section-actions {
      display: flex;
      gap: 12px;
      
      .filter-select {
        width: 120px;
      }
    }
  }
}

.meter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  min-height: 200px;
}

.meter-card {
  background: #2d2d3a;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #3a3a4a;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    border-color: #10b981;
    box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
  }
  
  &.offline {
    opacity: 0.7;
    border-left: 3px solid #6b7280;
  }
  
  &.danger {
    border-left: 3px solid #ef4444;
    background: linear-gradient(to right, rgba(239, 68, 68, 0.1), #2d2d3a);
  }
  
  .meter-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    
    .meter-name {
      font-weight: 600;
      font-size: 15px;
      color: #ffffff;
    }
    
    .meter-type-tag {
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 11px;
      font-weight: 600;
      
      &.type-electric {
        background: rgba(245, 158, 11, 0.2);
        color: #fbbf24;
      }
      
      &.type-water {
        background: rgba(6, 182, 212, 0.2);
        color: #22d3ee;
      }
      
      &.type-gas {
        background: rgba(249, 115, 22, 0.2);
        color: #fb923c;
      }
      
      &.type-cold {
        background: rgba(59, 130, 246, 0.2);
        color: #60a5fa;
      }
      
      &.type-heat {
        background: rgba(239, 68, 68, 0.2);
        color: #f87171;
      }
    }
  }
  
  .meter-id {
    font-size: 11px;
    color: #6b7280;
    font-family: monospace;
    background: #1a1a2e;
    padding: 2px 6px;
    border-radius: 4px;
    display: inline-block;
    margin-bottom: 12px;
  }
  
  .meter-body {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
    padding: 12px 0;
    border-top: 1px dashed #3a3a4a;
    
    .meter-param {
      text-align: center;
      
      .meter-value {
        font-size: 20px;
        font-weight: 700;
        color: #ffffff;
        font-family: 'Courier New', monospace;
        
        &.warning {
          color: #f59e0b;
        }
        
        &.danger {
          color: #ef4444;
        }
      }
      
      .meter-label {
        font-size: 11px;
        color: #6b7280;
        margin-top: 4px;
      }
    }
  }
  
  .meter-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px dashed #3a3a4a;
    
    .meter-status {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      
      .status-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
      }
      
      &.online {
        color: #10b981;
        .status-dot { background: #10b981; }
      }
      
      &.offline {
        color: #6b7280;
        .status-dot { background: #6b7280; }
      }
      
      &.warning {
        color: #f59e0b;
        .status-dot { background: #f59e0b; }
      }
    }
    
    .meter-time {
      font-size: 11px;
      color: #6b7280;
    }
  }
}

// 详情弹窗
.meter-detail {
  .detail-chart-section {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #404040;
    
    .chart-title {
      font-size: 14px;
      font-weight: 600;
      color: #ffffff;
      margin-bottom: 12px;
    }
    
    .detail-chart {
      height: 200px;
    }
  }
}

// 深色弹窗样式
:deep(.dark-dialog) {
  .el-dialog {
    background: #2d2d3a !important;
    border: 1px solid #3a3a4a;
    
    .el-dialog__header {
      background: #252532 !important;
      border-bottom: 1px solid #3a3a4a;
      
      .el-dialog__title {
        color: #ffffff !important;
      }
    }
    
    .el-dialog__body {
      background: #2d2d3a !important;
    }
  }
  
  .el-descriptions {
    .el-descriptions__body {
      background: transparent !important;
      
      .el-descriptions__cell {
        background: #1a1a2e !important;
        border-color: #3a3a4a !important;
        
        .el-descriptions__label {
          color: #a0a0b0 !important;
          background: #252532 !important;
        }
        
        .el-descriptions__content {
          color: #ffffff !important;
        }
      }
    }
  }
}

// 深色select样式
:deep(.el-select) {
  .el-select__wrapper {
    background: #1a1a2e !important;
    border-color: #3a3a4a !important;
    
    .el-select__placeholder {
      color: #6b7280 !important;
    }
    
    .el-select__selected-item {
      color: #ffffff !important;
    }
  }
  
  &:hover .el-select__wrapper {
    border-color: #10b981 !important;
  }
}
</style>
