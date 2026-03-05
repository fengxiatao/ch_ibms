<template>
  <div class="energy-chart">
    <Echart :options="chartOptions" :height="height" />
  </div>
</template>

<script setup lang="ts">
import type { EChartsOption } from 'echarts'

interface Props {
  data?: {
    dates: string[]
    electricity: number[]
    water: number[]
    gas: number[]
  }
  height?: string | number
}

const props = withDefaults(defineProps<Props>(), {
  data: () => ({
    dates: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
    electricity: [320, 302, 341, 374, 390, 450, 520, 510, 420, 350, 310, 290],
    water: [120, 132, 101, 134, 90, 130, 140, 152, 135, 120, 110, 100],
    gas: [220, 182, 191, 234, 290, 330, 310, 298, 265, 230, 210, 195]
  }),
  height: '280px'
})

const chartOptions = computed<EChartsOption>(() => {
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(10, 30, 60, 0.95)',
      borderColor: 'rgba(0, 180, 255, 0.3)',
      textStyle: { color: '#fff' },
      axisPointer: {
        type: 'shadow',
        shadowStyle: {
          color: 'rgba(0, 180, 255, 0.08)'
        }
      }
    },
    legend: {
      top: 0,
      right: 0,
      textStyle: {
        color: 'rgba(255, 255, 255, 0.7)',
        fontSize: 12
      },
      itemWidth: 16,
      itemHeight: 8,
      itemGap: 20
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
      data: props.data.dates,
      axisLine: {
        lineStyle: { color: 'rgba(0, 180, 255, 0.3)' }
      },
      axisTick: { show: false },
      axisLabel: {
        color: 'rgba(255, 255, 255, 0.6)',
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
      name: '单位: 吨/千瓦时',
      nameTextStyle: {
        color: 'rgba(255, 255, 255, 0.5)',
        fontSize: 11
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(0, 180, 255, 0.1)',
          type: 'dashed'
        }
      },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: 'rgba(255, 255, 255, 0.6)',
        fontSize: 11
      }
    },
    series: [
      {
        name: '用电量',
        type: 'bar',
        barWidth: 10,
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: '#00b4ff' },
              { offset: 1, color: 'rgba(0, 180, 255, 0.3)' }
            ]
          }
        },
        data: props.data.electricity
      },
      {
        name: '用水量',
        type: 'bar',
        barWidth: 10,
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: '#10b981' },
              { offset: 1, color: 'rgba(16, 185, 129, 0.3)' }
            ]
          }
        },
        data: props.data.water
      },
      {
        name: '用气量',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 2,
          color: '#f59e0b'
        },
        itemStyle: {
          color: '#f59e0b',
          borderWidth: 2,
          borderColor: '#fff'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(245, 158, 11, 0.3)' },
              { offset: 1, color: 'rgba(245, 158, 11, 0)' }
            ]
          }
        },
        data: props.data.gas
      }
    ]
  }
})
</script>

<style lang="scss" scoped>
.energy-chart {
  width: 100%;
}
</style>
