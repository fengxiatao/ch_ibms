<template>
  <div class="electricity-report-chart dark-theme-page">
    <div ref="chartRef" class="chart-container"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'

interface Props {
  data?: any[]
}

const props = withDefaults(defineProps<Props>(), {
  data: () => []
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
      data: ['用电量', '费用'],
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
    xAxis: [
      {
        type: 'category',
        data: ['1月', '2月', '3月', '4月', '5月', '6月'],
        axisPointer: {
          type: 'shadow'
        },
        axisLine: {
          lineStyle: {
            color: '#334155'
          }
        },
        axisLabel: {
          color: '#94a3b8'
        }
      }
    ],
    yAxis: [
      {
        type: 'value',
        name: '用电量(kWh)',
        position: 'left',
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
      {
        type: 'value',
        name: '费用(元)',
        position: 'right',
        axisLine: {
          lineStyle: {
            color: '#334155'
          }
        },
        axisLabel: {
          color: '#94a3b8'
        }
      }
    ],
    series: [
      {
        name: '用电量',
        type: 'bar',
        data: [28472, 31245, 29856, 32145, 30567, 28945],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#00d4ff' },
            { offset: 1, color: '#0099cc' }
          ])
        }
      },
      {
        name: '费用',
        type: 'line',
        yAxisIndex: 1,
        data: [42708, 46868, 44784, 48218, 45851, 43418],
        lineStyle: {
          color: '#22c55e',
          width: 3
        },
        itemStyle: {
          color: '#22c55e'
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

watch(() => props.data, () => {
  if (chartInstance) {
    initChart()
  }
}, { deep: true })
</script>

<style lang="scss" scoped>@use '@/styles/dark-theme.scss';

.electricity-report-chart {
  width: 100%;
  height: 100%;

  .chart-container {
    width: 100%;
    height: 200px;
  }
}
</style>





















