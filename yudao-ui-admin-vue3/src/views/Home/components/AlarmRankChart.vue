<template>
  <div class="alarm-rank-chart">
    <Echart :options="chartOptions" :height="height" />
  </div>
</template>

<script setup lang="ts">
import type { EChartsOption } from 'echarts'

interface Props {
  data?: Array<{
    name: string
    value: number
  }>
  height?: string | number
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [
    { name: '消防系统', value: 28 },
    { name: '空调系统', value: 22 },
    { name: '电梯系统', value: 18 },
    { name: '照明系统', value: 15 },
    { name: '安防系统', value: 12 }
  ],
  height: '200px'
})

const chartOptions = computed<EChartsOption>(() => {
  const maxValue = Math.max(...props.data.map(d => d.value))
  
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(10, 30, 60, 0.95)',
      borderColor: 'rgba(0, 180, 255, 0.3)',
      textStyle: { color: '#fff' },
      axisPointer: { type: 'none' }
    },
    grid: {
      left: '3%',
      right: '15%',
      top: '3%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      max: maxValue * 1.2,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { show: false },
      splitLine: { show: false }
    },
    yAxis: {
      type: 'category',
      inverse: true,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: 'rgba(255, 255, 255, 0.8)',
        fontSize: 12,
        margin: 12
      },
      data: props.data.map(d => d.name)
    },
    series: [
      {
        name: '告警数',
        type: 'bar',
        barWidth: 16,
        showBackground: true,
        backgroundStyle: {
          color: 'rgba(0, 180, 255, 0.1)',
          borderRadius: 8
        },
        itemStyle: {
          borderRadius: 8,
          color: (params) => {
            const colors = [
              { offset: 0, color: '#ef4444' },
              { offset: 1, color: '#f97316' }
            ]
            if (params.dataIndex > 2) {
              colors[0].color = '#f59e0b'
              colors[1].color = '#fbbf24'
            }
            return {
              type: 'linear',
              x: 0, y: 0, x2: 1, y2: 0,
              colorStops: colors
            }
          }
        },
        label: {
          show: true,
          position: 'right',
          color: 'rgba(255, 255, 255, 0.7)',
          fontSize: 12,
          formatter: '{c}次'
        },
        data: props.data.map(d => d.value)
      }
    ]
  }
})
</script>

<style lang="scss" scoped>
.alarm-rank-chart {
  width: 100%;
}
</style>
