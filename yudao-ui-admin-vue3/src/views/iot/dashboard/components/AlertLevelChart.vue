<template>
  <div ref="chartRef" :style="{ width: '100%', height: height }"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Object as PropType<Record<string, number>>,
    default: () => ({})
  },
  height: {
    type: String,
    default: '300px'
  }
})

const chartRef = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts | null = null

const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)

  const option: echarts.EChartsOption = {
    title: {
      text: '告警级别分布',
      left: 'center',
      textStyle: {
        color: '#333',
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 'bottom'
    },
    series: [
      {
        name: '告警级别',
        type: 'pie',
        radius: '60%',
        center: ['50%', '45%'],
        data: Object.entries(props.data).map(([name, value]) => ({
          name,
          value,
          itemStyle: {
            color: getColorByLevel(name)
          }
        })),
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

const getColorByLevel = (level: string) => {
  const colorMap: Record<string, string> = {
    'CRITICAL': '#F56C6C',
    'ERROR': '#E6A23C',
    'WARNING': '#E6A23C',
    'INFO': '#409EFF',
    '未知': '#909399'
  }
  return colorMap[level] || '#409EFF'
}

watch(
  () => props.data,
  () => {
    initChart()
  },
  { deep: true }
)

onMounted(() => {
  initChart()
  window.addEventListener('resize', () => {
    chartInstance?.resize()
  })
})

onUnmounted(() => {
  chartInstance?.dispose()
})
</script>












