<template>
  <div class="device-status-chart">
    <Echart :options="chartOptions" :height="height" />
  </div>
</template>

<script setup lang="ts">
import type { EChartsOption } from 'echarts'

interface Props {
  data?: {
    online: number
    offline: number
    alarm: number
  }
  height?: string | number
}

const props = withDefaults(defineProps<Props>(), {
  data: () => ({ online: 1280, offline: 45, alarm: 12 }),
  height: '220px'
})

const chartOptions = computed<EChartsOption>(() => {
  const { online, offline, alarm } = props.data
  const total = online + offline + alarm
  
  return {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(10, 30, 60, 0.95)',
      borderColor: 'rgba(0, 180, 255, 0.3)',
      textStyle: { color: '#fff' },
      formatter: '{b}: {c}台 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      itemWidth: 12,
      itemHeight: 12,
      textStyle: {
        color: 'rgba(255, 255, 255, 0.7)',
        fontSize: 13
      },
      formatter: (name: string) => {
        const valueMap: Record<string, number> = {
          '在线': online,
          '离线': offline,
          '告警': alarm
        }
        return `${name}  ${valueMap[name]}`
      }
    },
    series: [
      {
        name: '设备状态',
        type: 'pie',
        radius: ['55%', '75%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: 'rgba(5, 20, 45, 0.9)',
          borderWidth: 2
        },
        label: {
          show: true,
          position: 'center',
          formatter: () => `{total|${total}}\n{label|设备总数}`,
          rich: {
            total: {
              fontSize: 28,
              fontWeight: 'bold',
              color: '#fff',
              lineHeight: 36
            },
            label: {
              fontSize: 12,
              color: 'rgba(255, 255, 255, 0.5)',
              lineHeight: 20
            }
          }
        },
        emphasis: {
          scale: true,
          scaleSize: 8,
          itemStyle: {
            shadowBlur: 20,
            shadowColor: 'rgba(0, 180, 255, 0.5)'
          }
        },
        data: [
          { 
            value: online, 
            name: '在线',
            itemStyle: { 
              color: {
                type: 'linear',
                x: 0, y: 0, x2: 1, y2: 1,
                colorStops: [
                  { offset: 0, color: '#10b981' },
                  { offset: 1, color: '#059669' }
                ]
              }
            }
          },
          { 
            value: offline, 
            name: '离线',
            itemStyle: { 
              color: {
                type: 'linear',
                x: 0, y: 0, x2: 1, y2: 1,
                colorStops: [
                  { offset: 0, color: '#64748b' },
                  { offset: 1, color: '#475569' }
                ]
              }
            }
          },
          { 
            value: alarm, 
            name: '告警',
            itemStyle: { 
              color: {
                type: 'linear',
                x: 0, y: 0, x2: 1, y2: 1,
                colorStops: [
                  { offset: 0, color: '#ef4444' },
                  { offset: 1, color: '#dc2626' }
                ]
              }
            }
          }
        ]
      }
    ]
  }
})
</script>

<style lang="scss" scoped>
.device-status-chart {
  width: 100%;
}
</style>
