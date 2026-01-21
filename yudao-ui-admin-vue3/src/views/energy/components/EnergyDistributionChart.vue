<template>
  <div class="energy-distribution-chart dark-theme-page">
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
      trigger: 'item',
      backgroundColor: 'rgba(15, 23, 42, 0.9)',
      borderColor: '#00d4ff',
      textStyle: {
        color: '#ffffff'
      },
      formatter: '{a} <br/>{b}: {c}kWh ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      textStyle: {
        color: '#94a3b8'
      }
    },
    series: [
      {
        name: '能源分布',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        data: [
          { value: 1048, name: '照明系统', itemStyle: { color: '#00d4ff' } },
          { value: 735, name: '空调系统', itemStyle: { color: '#22c55e' } },
          { value: 580, name: '电梯设备', itemStyle: { color: '#f59e0b' } },
          { value: 484, name: '办公设备', itemStyle: { color: '#8b5cf6' } },
          { value: 300, name: '其他设备', itemStyle: { color: '#ef4444' } }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
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

.energy-distribution-chart {
  width: 100%;
  height: 100%;

  .chart-container {
    width: 100%;
    height: 200px;
  }
}
</style>





















