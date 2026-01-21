<template>
  <div ref="chartRef" :style="{ width: '100%', height: height }"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Object as PropType<{
      cpuUsage: number
      memoryUsage: number
      diskUsage: number
    }>,
    default: () => ({
      cpuUsage: 0,
      memoryUsage: 0,
      diskUsage: 0
    })
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
      text: '系统负载监控',
      left: 'center',
      textStyle: {
        color: '#333',
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
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
      data: ['CPU使用率', '内存使用率', '磁盘使用率']
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [
      {
        name: '使用率',
        type: 'bar',
        data: [
          {
            value: props.data.cpuUsage,
            itemStyle: {
              color: getColorByUsage(props.data.cpuUsage)
            }
          },
          {
            value: props.data.memoryUsage,
            itemStyle: {
              color: getColorByUsage(props.data.memoryUsage)
            }
          },
          {
            value: props.data.diskUsage,
            itemStyle: {
              color: getColorByUsage(props.data.diskUsage)
            }
          }
        ],
        label: {
          show: true,
          position: 'top',
          formatter: '{c}%'
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

const getColorByUsage = (usage: number) => {
  if (usage >= 80) return '#F56C6C'
  if (usage >= 60) return '#E6A23C'
  return '#67C23A'
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












