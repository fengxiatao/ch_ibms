<template>
  <div class="carbon-emission-chart dark-theme-page">
    <div ref="chartRef" class="chart-container"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'

interface Props {
  data?: any[]
  timeRange?: string
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [],
  timeRange: 'realtime'
})

const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)
  
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(15, 23, 42, 0.9)',
      borderColor: '#00d4ff',
      textStyle: {
        color: '#ffffff'
      }
    },
    legend: {
      data: ['碳排放量', '目标值'],
      textStyle: {
        color: '#94a3b8'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月'],
      axisLine: {
        lineStyle: {
          color: '#334155'
        }
      },
      axisLabel: {
        color: '#94a3b8'
      }
    },
    yAxis: {
      type: 'value',
      name: '碳排放(kg CO₂)',
      axisLine: {
        lineStyle: {
          color: '#334155'
        }
      },
      axisLabel: {
        color: '#94a3b8'
      },
      splitLine: {
        lineStyle: {
          color: '#334155',
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: '碳排放量',
        type: 'bar',
        data: [1200, 1350, 1100, 1400, 1250, 1600, 1450, 1300, 1423],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#ef4444' },
            { offset: 1, color: '#dc2626' }
          ])
        }
      },
      {
        name: '目标值',
        type: 'line',
        data: [1300, 1300, 1300, 1300, 1300, 1300, 1300, 1300, 1300],
        lineStyle: {
          color: '#f59e0b',
          width: 2,
          type: 'dashed'
        },
        itemStyle: {
          color: '#f59e0b'
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

const resizeChart = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', resizeChart)
})

watch(() => [props.data, props.timeRange], () => {
  if (chartInstance) {
    initChart()
  }
}, { deep: true })
</script>

<style lang="scss" scoped>@use '@/styles/dark-theme.scss';

.carbon-emission-chart {
  width: 100%;
  height: 100%;

  .chart-container {
    width: 100%;
    height: 200px;
  }
}
</style>





















