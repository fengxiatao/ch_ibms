<template>
  <div class="power-consumption-chart dark-theme-page">
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
      data: ['实时功率', '平均功率'],
      textStyle: {
        color: '#94a3b8'
      },
      top: 10
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
      boundaryGap: false,
      data: ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00'],
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
      name: '功率(kW)',
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
        name: '实时功率',
        type: 'line',
        data: [820, 932, 901, 934, 1290, 1330, 1320, 1200, 1100, 1350, 1200, 950],
        lineStyle: {
          color: '#00d4ff',
          width: 3
        },
        itemStyle: {
          color: '#00d4ff'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 212, 255, 0.3)' },
            { offset: 1, color: 'rgba(0, 212, 255, 0.1)' }
          ])
        },
        smooth: true
      },
      {
        name: '平均功率',
        type: 'line',
        data: [780, 890, 850, 900, 1200, 1250, 1280, 1150, 1050, 1300, 1150, 900],
        lineStyle: {
          color: '#22c55e',
          width: 2
        },
        itemStyle: {
          color: '#22c55e'
        },
        smooth: true
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

.power-consumption-chart {
  width: 100%;
  height: 100%;

  .chart-container {
    width: 100%;
    height: 200px;
  }
}
</style>





















