<template>
  <div class="history-data-panel">
    <!-- 查询条件 -->
    <el-card class="mb-4">
      <el-form :inline="true" :model="queryParams" class="query-form">
        <el-form-item label="设备">
          <el-select
            v-model="queryParams.stationCode"
            placeholder="请选择设备"
            filterable
            clearable
            style="width: 240px"
            @change="handleDeviceChange"
          >
            <el-option
              v-for="device in deviceList"
              :key="device.id"
              :label="`${device.deviceName} (${device.stationCode})`"
              :value="device.stationCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="指标">
          <el-select v-model="queryParams.indicator" placeholder="选择指标" style="width: 160px">
            <el-option
              v-for="item in indicatorOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="queryParams.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            :shortcuts="dateShortcuts"
            style="width: 380px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :loading="loading">
            <Icon icon="ep:search" class="mr-1" />查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-1" />重置
          </el-button>
          <el-button type="success" @click="handleExport" :loading="exportLoading">
            <Icon icon="ep:download" class="mr-1" />导出
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 图表展示 -->
    <el-card class="mb-4" v-if="historyData.length > 0">
      <template #header>
        <div class="card-header">
          <span>数据趋势图</span>
          <el-radio-group v-model="chartType" size="small">
            <el-radio-button label="line">折线图</el-radio-button>
            <el-radio-button label="bar">柱状图</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="chartRef" class="chart-container"></div>
    </el-card>

    <!-- 数据表格 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>历史数据列表</span>
          <span class="data-count">共 {{ total }} 条记录</span>
        </div>
      </template>
      <el-table :data="historyData" border stripe v-loading="loading" max-height="400">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="timestamp" label="时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.timestamp) }}
          </template>
        </el-table-column>
        <el-table-column prop="indicator" label="指标" width="120" align="center">
          <template #default="{ row }">
            <el-tag>{{ getIndicatorLabel(row.indicator) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="value" label="数值" align="center">
          <template #default="{ row }">
            <span class="data-value">{{ formatValue(row.value) }}</span>
            <span class="data-unit">{{ getIndicatorUnit(row.indicator) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stationCode" label="测站编码" width="150" align="center" />
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNo"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>
  </div>
</template>


<script setup lang="ts">
import { ref, watch, onMounted, nextTick } from 'vue'
import * as ChanghuiApi from '@/api/iot/changhui'
import { formatDate } from '@/utils/formatTime'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  deviceList: any[]
  selectedDevice?: string
}>()

const emit = defineEmits(['device-change'])

// 指标选项
const indicatorOptions = [
  { value: 'waterLevel', label: '水位', unit: 'm' },
  { value: 'instantFlow', label: '瞬时流量', unit: 'L/s' },
  { value: 'instantVelocity', label: '瞬时流速', unit: 'm/s' },
  { value: 'cumulativeFlow', label: '累计流量', unit: 'm³' },
  { value: 'gatePosition', label: '闸位', unit: 'mm' },
  { value: 'temperature', label: '温度', unit: '°C' },
  { value: 'seepagePressure', label: '渗透水压力', unit: 'kPa' },
  { value: 'load', label: '荷重', unit: 'kN' }
]

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近1小时',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000)
      return [start, end]
    }
  },
  {
    text: '最近6小时',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 6)
      return [start, end]
    }
  },
  {
    text: '最近24小时',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24)
      return [start, end]
    }
  },
  {
    text: '最近7天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近30天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  }
]

const loading = ref(false)
const exportLoading = ref(false)
const historyData = ref<any[]>([])
const total = ref(0)
const chartRef = ref<HTMLElement>()
const chartType = ref('line')
let chartInstance: echarts.ECharts | null = null

const queryParams = ref({
  stationCode: '',
  indicator: 'waterLevel',
  timeRange: [] as string[],
  pageNo: 1,
  pageSize: 20
})

// 监听父组件传入的设备
watch(() => props.selectedDevice, (val) => {
  if (val) {
    queryParams.value.stationCode = val
  }
}, { immediate: true })

// 监听图表类型变化
watch(chartType, () => {
  if (historyData.value.length > 0) {
    renderChart()
  }
})


// 获取指标标签
const getIndicatorLabel = (value: string) => {
  const item = indicatorOptions.find(i => i.value === value)
  return item ? item.label : value
}

// 获取指标单位
const getIndicatorUnit = (value: string) => {
  const item = indicatorOptions.find(i => i.value === value)
  return item ? item.unit : ''
}

// 格式化数值
const formatValue = (value: any) => {
  if (value === null || value === undefined) return '--'
  if (typeof value === 'number') {
    return value.toFixed(2)
  }
  return value
}

// 格式化日期时间
const formatDateTime = (timestamp: any) => {
  if (!timestamp) return '--'
  return formatDate(timestamp, 'YYYY-MM-DD HH:mm:ss')
}

// 设备切换
const handleDeviceChange = (val: string) => {
  emit('device-change', val)
}

// 查询历史数据
const handleQuery = async () => {
  if (!queryParams.value.stationCode) {
    ElMessage.warning('请选择设备')
    return
  }
  
  loading.value = true
  try {
    const params: any = {
      stationCode: queryParams.value.stationCode,
      indicator: queryParams.value.indicator,
      pageNo: queryParams.value.pageNo,
      pageSize: queryParams.value.pageSize
    }
    
    if (queryParams.value.timeRange?.length === 2) {
      params.startTime = queryParams.value.timeRange[0]
      params.endTime = queryParams.value.timeRange[1]
    }
    
    const res = await ChanghuiApi.ChanghuiDataApi.getHistoryData(params)
    historyData.value = res.list || []
    total.value = res.total || 0
    
    // 渲染图表
    await nextTick()
    if (historyData.value.length > 0) {
      renderChart()
    }
  } catch (e) {
    console.error('查询历史数据失败', e)
    ElMessage.error('查询历史数据失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const resetQuery = () => {
  queryParams.value = {
    stationCode: props.selectedDevice || '',
    indicator: 'waterLevel',
    timeRange: [],
    pageNo: 1,
    pageSize: 20
  }
  historyData.value = []
  total.value = 0
  if (chartInstance) {
    chartInstance.clear()
  }
}


// 导出数据
const handleExport = async () => {
  if (!queryParams.value.stationCode) {
    ElMessage.warning('请选择设备')
    return
  }
  
  if (historyData.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }
  
  exportLoading.value = true
  try {
    // 构建CSV数据
    const headers = ['时间', '指标', '数值', '单位', '测站编码']
    const rows = historyData.value.map(item => [
      formatDateTime(item.timestamp),
      getIndicatorLabel(item.indicator),
      formatValue(item.value),
      getIndicatorUnit(item.indicator),
      item.stationCode
    ])
    
    // 添加BOM以支持中文
    let csvContent = '\uFEFF'
    csvContent += headers.join(',') + '\n'
    csvContent += rows.map(row => row.join(',')).join('\n')
    
    // 创建Blob并下载
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `长辉设备历史数据_${queryParams.value.stationCode}_${new Date().getTime()}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('导出成功')
  } catch (e) {
    console.error('导出失败', e)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

// 渲染图表
const renderChart = () => {
  if (!chartRef.value) return
  
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  
  // 按时间排序
  const sortedData = [...historyData.value].sort((a, b) => {
    return new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime()
  })
  
  const xData = sortedData.map(item => formatDateTime(item.timestamp))
  const yData = sortedData.map(item => item.value)
  const indicatorLabel = getIndicatorLabel(queryParams.value.indicator)
  const indicatorUnit = getIndicatorUnit(queryParams.value.indicator)
  
  const option: echarts.EChartsOption = {
    title: {
      text: `${indicatorLabel}趋势`,
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const data = params[0]
        return `${data.name}<br/>${indicatorLabel}: ${formatValue(data.value)} ${indicatorUnit}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xData,
      axisLabel: {
        rotate: 45,
        formatter: (value: string) => {
          return value.split(' ')[1] || value
        }
      }
    },
    yAxis: {
      type: 'value',
      name: `${indicatorLabel} (${indicatorUnit})`,
      axisLabel: {
        formatter: (value: number) => value.toFixed(2)
      }
    },
    series: [
      {
        name: indicatorLabel,
        type: chartType.value,
        data: yData,
        smooth: true,
        itemStyle: {
          color: '#409eff'
        },
        areaStyle: chartType.value === 'line' ? {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ])
        } : undefined
      }
    ]
  }
  
  chartInstance.setOption(option, true)
}

// 窗口大小变化时重新渲染图表
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})
</script>

<style scoped>
.history-data-panel {
  padding: 0;
}

.mb-4 {
  margin-bottom: 16px;
}

.mr-1 {
  margin-right: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.data-count {
  font-size: 14px;
  color: #909399;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.query-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.data-value {
  font-weight: bold;
  color: #409eff;
  margin-right: 4px;
}

.data-unit {
  color: #909399;
  font-size: 12px;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
