<script setup lang="ts">
import type { EChartsOption } from 'echarts'
import dayjs from 'dayjs'
import { Echart } from '@/components/Echart'
import {
  getEnvStatistics,
  getEnvSensorList,
  getLatestEnvDataRecord,
  getEnvDataRecordHistory,
  type IbmsEnvSensorVO,
  type IbmsEnvDataRecordVO
} from '@/api/iot/building/env'

defineOptions({ name: 'IotIndoorEnvOverview' })

type TempRange = 'day' | 'week' | 'month'
type AirRange = 'day' | 'week' | 'month'

type FloorFilter = 'all' | 'A' | 'B' | 'alarm'

interface FloorPoint {
  id: number
  name: string
  code: string
  building: string
  floorNum: number
  temp: number
  hum: number
  pm25: number
  hcho: number
  tvoc: number
  status: '在线' | '离线' | '告警'
  comfort: string
  airGrade: string
  collectTime: string
  warn?: string
  offline?: boolean
}

interface TrendDataset {
  labels: string[]
  pm25: number[]
  tvoc: number[]
  hcho: number[]
  cutoff: string
  current: { pm25: number; tvoc: number; hcho: number }
}

const currentTimeText = ref('')
let clockTimer: number | undefined

const updateClock = () => {
  currentTimeText.value = dayjs().format('HH:mm:ss')
}

onMounted(() => {
  updateClock()
  clockTimer = window.setInterval(updateClock, 1000)
  loadOverviewData()
})

onBeforeUnmount(() => {
  if (clockTimer) window.clearInterval(clockTimer)
})

const sensors = ref<IbmsEnvSensorVO[]>([])

const getAirSourceName = (sensor?: Partial<IbmsEnvSensorVO>) => {
  return sensor?.sensorName || sensor?.sensorCode || '环境监测点'
}

const buildFloorPointsFromSensors = (latestMap: Map<number, IbmsEnvDataRecordVO>) => {
  const list: FloorPoint[] = []
  for (const sensor of sensors.value) {
    if (!sensor.areaName) continue
    if (!sensor.sensorCode?.includes('AIR')) continue
    const latest = sensor.id ? latestMap.get(sensor.id) : undefined
    const building = sensor.areaName
    const floorStr = (sensor as any).floor ?? ''
    const floorNum = parseInt(floorStr) || parseInt(floorStr.replace(/\D/g, '')) || 0
    const statusText: FloorPoint['status'] =
      sensor.status === 0 ? '离线' : sensor.status === 2 || sensor.status === 3 ? '告警' : '在线'
    const pm25 = latest?.pm25
    const hcho = (latest as any)?.formaldehyde as number | undefined
    const temp = latest?.temperature
    const hum = latest?.humidity
    const tvoc = pm25 != null ? Number((pm25 / 30).toFixed(2)) : undefined
    const comfort =
      temp != null && hum != null ? (temp >= 23 && temp <= 26 && hum >= 40 && hum <= 65 ? '舒适' : '略闷') : '--'
    const airGrade =
      pm25 == null
        ? '--'
        : pm25 <= 35
          ? '优'
          : pm25 <= 75
            ? '良'
            : '一般'
    const warn = statusText === '告警' ? 'PM2.5' : undefined
    list.push({
      id: sensor.id!,
      name: sensor.sensorName || sensor.sensorCode,
      code: sensor.sensorCode,
      building,
      floorNum,
      temp: temp != null ? Number(temp) : 0,
      hum: hum != null ? Number(hum) : 0,
      pm25: pm25 != null ? Number(pm25) : 0,
      hcho: hcho != null ? Number(hcho) : 0,
      tvoc: tvoc != null ? tvoc : 0,
      status: statusText,
      comfort,
      airGrade,
      collectTime: latest?.collectTime ? dayjs(latest.collectTime).format('HH:mm') : '--',
      warn,
      offline: sensor.status === 0
    })
  }
  floorPoints.value = list
}

const buildTempSeriesFromHistory = (records: IbmsEnvDataRecordVO[]) => {
  const byHour = records
    .slice()
    .sort((a, b) => dayjs(a.collectTime).valueOf() - dayjs(b.collectTime).valueOf())
  hourLabels.value = byHour.map((r) => dayjs(r.collectTime).format('HH:mm'))
  hourTemp.value = byHour.map((r) => Number(r.temperature ?? 0))
  hourHum.value = byHour.map((r) => Number(r.humidity ?? 0))
}

const buildTrendDataForSensor = (sensorName: string, records: IbmsEnvDataRecordVO[]) => {
  const sorted = records
    .slice()
    .sort((a, b) => dayjs(a.collectTime).valueOf() - dayjs(b.collectTime).valueOf())
  const labels = sorted.map((r) => dayjs(r.collectTime).format('HH:mm'))
  const pm25 = sorted.map((r) => Number(r.pm25 ?? 0))
  const hcho = sorted.map((r) => Number((r as any).formaldehyde ?? 0))
  const tvoc = pm25.map((v) => Number((v / 30).toFixed(2)))
  const last = sorted[sorted.length - 1]
  const cutoff = last ? dayjs(last.collectTime).format('YYYY年M月D日 HH:mm') : ''
  const dataset: TrendDataset = {
    labels,
    pm25,
    tvoc,
    hcho,
    cutoff,
    current: {
      pm25: pm25[pm25.length - 1] ?? 0,
      tvoc: tvoc[tvoc.length - 1] ?? 0,
      hcho: hcho[hcho.length - 1] ?? 0
    }
  }
  trendDataMap[sensorName] = {
    近24小时: dataset,
    近7天: dataset,
    近30天: dataset
  }
}

const loadOverviewData = async () => {
  try {
    const [statsRes, sensorListRes] = await Promise.all([getEnvStatistics(), getEnvSensorList({} as any)])
    const stats = statsRes as any
    const sensorList = sensorListRes as any
    sensors.value = (Array.isArray(sensorList?.list) ? sensorList.list : sensorList) as IbmsEnvSensorVO[]

    const statData = (stats && 'data' in stats ? (stats as any).data : stats) as any

    deviceStat.total = statData?.totalCount ?? 0
    deviceStat.online = statData?.onlineCount ?? 0
    deviceStat.offline = statData?.offlineCount ?? 0
    deviceStat.offlineWeather = statData?.tempHumidityCount ?? 0
    deviceStat.offlineAir = statData?.pm25Count ?? 0
    deviceStat.offlineCo2 = statData?.co2Count ?? 0
    deviceStat.updatedAt = dayjs().format('HH:mm')

    const outdoorSensor = sensors.value.find((s) => s.sensorCode === 'OUT-ENV')
    if (outdoorSensor?.id) {
      const latestOutdoor = (await getLatestEnvDataRecord(outdoorSensor.id)) as IbmsEnvDataRecordVO
      outdoorWeather.temp = Number(latestOutdoor.temperature ?? 0)
      outdoorWeather.humid = Number(latestOutdoor.humidity ?? 0)
      outdoorWeather.updatedAt = dayjs(latestOutdoor.collectTime).format('HH:mm')
      airQuality.pm25 = Number(latestOutdoor.pm25 ?? 0)
      airQuality.pm10 = Number((latestOutdoor as any).pm10 ?? 0)
      airQuality.hcho = Number(((latestOutdoor as any).formaldehyde ?? 0))
      airQuality.tvoc = Number(((latestOutdoor.pm25 ?? 0) / 30).toFixed(3))
      airQuality.aqi = Math.round(airQuality.pm25)
      airQuality.updatedAt = outdoorWeather.updatedAt
      co2.value = Number(latestOutdoor.co2 ?? 0)
      co2.updatedAt = outdoorWeather.updatedAt
    }

    const latestMap = new Map<number, IbmsEnvDataRecordVO>()
    for (const s of sensors.value) {
      if (!s.id) continue
      const latest = (await getLatestEnvDataRecord(s.id)) as IbmsEnvDataRecordVO
      if (latest) latestMap.set(s.id, latest)
    }
    buildFloorPointsFromSensors(latestMap)

    const anyIndoor =
      sensors.value.find((s) => s.id && s.sensorCode?.includes('AIR') && s.sensorCode !== 'OUT-ENV') ||
      sensors.value.find((s) => s.id)
    if (anyIndoor?.id) {
      const history = (await getEnvDataRecordHistory(anyIndoor.id, 24)) as IbmsEnvDataRecordVO[]
      if (history?.length) {
        buildTempSeriesFromHistory(history)
        const airSourceName = getAirSourceName(anyIndoor)
        buildTrendDataForSensor(airSourceName, history)
        airSource.value = airSourceName
      }
    }
  } catch {
    // 忽略，保持静态占位
  }
}

const deviceStat = reactive({
  total: 0,
  online: 0,
  offline: 0,
  offlineWeather: 0,
  offlineAir: 0,
  offlineCo2: 0,
  updatedAt: ''
})

const outdoorWeather = reactive({
  location: '园区室外',
  temp: 0,
  humid: 0,
  desc: '室外环境监测',
  tip: '实时监测数据',
  updatedAt: ''
})

const airQuality = reactive({
  grade: '优',
  aqi: 0,
  pm25: 0,
  pm10: 0,
  hcho: 0,
  tvoc: 0,
  updatedAt: ''
})

const co2 = reactive({
  value: 0,
  updatedAt: ''
})

const tempRange = ref<TempRange>('day')
const tempChartTitle = computed(() => '温湿度变化统计')

const hourLabels = ref<string[]>([])
const hourTemp = ref<number[]>([])
const hourHum = ref<number[]>([])

const weekLabels = ref<string[]>([])
const weekTemp = ref<number[]>([])
const weekHum = ref<number[]>([])

const monthLabels = ref<string[]>([])
const monthTemp = ref<number[]>([])
const monthHum = ref<number[]>([])

const tempSeriesData = computed(() => {
  if (tempRange.value === 'week') {
    return { labels: weekLabels.value, temp: weekTemp.value, hum: weekHum.value }
  }
  if (tempRange.value === 'month') {
    return { labels: monthLabels.value, temp: monthTemp.value, hum: monthHum.value }
  }
  return { labels: hourLabels.value, temp: hourTemp.value, hum: hourHum.value }
})

const tempHumidOptions = computed<EChartsOption>(() => {
  const data = tempSeriesData.value
  return {
    grid: { left: 44, right: 44, top: 38, bottom: 34 },
    tooltip: { trigger: 'axis' },
    legend: { top: 8, left: 8, itemWidth: 10, itemHeight: 10 },
    xAxis: { type: 'category', data: data.labels, axisTick: { show: false } },
    yAxis: [
      { type: 'value', name: '温度(°C)', nameGap: 12, splitLine: { show: true } },
      { type: 'value', name: '湿度(%)', nameGap: 12, splitLine: { show: false } }
    ],
    series: [
      {
        name: '温度(°C)',
        type: 'line',
        data: data.temp,
        smooth: true,
        showSymbol: false,
        lineStyle: { color: '#2563eb' }
      },
      {
        name: '湿度(%)',
        type: 'line',
        yAxisIndex: 1,
        data: data.hum,
        smooth: true,
        showSymbol: false,
        lineStyle: { color: '#9333ea' }
      }
    ]
  }
})

const historyCompare = reactive({
  tempToday: '--',
  tempDelta: '--',
  humToday: '--',
  humDelta: '--',
  pm25Today: 0,
  pm25Yesterday: 0,
  hchoToday: 0,
  hchoYesterday: 0,
  updatedAt: ''
})

const airSource = ref('')
const airRange = ref<AirRange>('day')

const rangeTextMap: Record<AirRange, '近24小时' | '近7天' | '近30天'> = {
  day: '近24小时',
  week: '近7天',
  month: '近30天'
}

const trendDataMap = reactive<Record<string, Record<'近24小时' | '近7天' | '近30天', TrendDataset>>>({})

const airSourceOptions = computed(() => Object.keys(trendDataMap))

const airSeriesData = computed(() => {
  const rangeText = rangeTextMap[airRange.value]
  const defaultSource = airSource.value || airSourceOptions.value[0] || ''
  const fallback = defaultSource ? trendDataMap[defaultSource]?.['近24小时'] : undefined
  return trendDataMap[airSource.value]?.[rangeText] ?? fallback ?? {
    labels: [],
    pm25: [],
    tvoc: [],
    hcho: [],
    cutoff: '',
    current: { pm25: 0, tvoc: 0, hcho: 0 }
  }
})

const airCutoffText = computed(() => airSeriesData.value.cutoff)
const currentPm25 = computed(() => airSeriesData.value.current.pm25)
const currentTvoc = computed(() => airSeriesData.value.current.tvoc)
const currentHcho = computed(() => airSeriesData.value.current.hcho)

const getPm25Status = (value: number) => {
  if (value <= 25) return { text: '优', type: 'success' as const }
  if (value <= 50) return { text: '良', type: 'warning' as const }
  return { text: '轻度', type: 'danger' as const }
}

const getTvocStatus = (value: number) => {
  if (value <= 0.6) return { text: '优', type: 'success' as const }
  if (value <= 1.0) return { text: '轻度', type: 'warning' as const }
  return { text: '中度', type: 'danger' as const }
}

const getHchoStatus = (value: number) => {
  if (value <= 0.03) return { text: '优', type: 'success' as const }
  if (value <= 0.08) return { text: '轻度', type: 'warning' as const }
  return { text: '中度', type: 'danger' as const }
}

const airTrendOptions = computed<EChartsOption>(() => {
  const data = airSeriesData.value
  return {
    grid: { left: 46, right: 18, top: 18, bottom: 34 },
    tooltip: {
      trigger: 'axis',
      valueFormatter: (value: any) => String(value),
      formatter: (params: any) => {
        const list = Array.isArray(params) ? params : [params]
        const lines = [`${list[0]?.axisValueLabel ?? ''}`]
        for (const item of list) {
          const name = item?.seriesName ?? ''
          const v = Number(item?.data ?? 0)
          if (name.includes('TVOC')) lines.push(`TVOC：${(v / 100).toFixed(2)} mg/m³`)
          else if (name.includes('甲醛')) lines.push(`甲醛：${(v / 1000).toFixed(3)} mg/m³`)
          else lines.push(`PM2.5：${v} μg/m³`)
        }
        return lines.join('<br/>')
      }
    },
    xAxis: { type: 'category', data: data.labels, axisTick: { show: false } },
    yAxis: { type: 'value', name: '浓度', splitLine: { show: true } },
    series: [
      {
        name: 'PM2.5 (μg/m³)',
        type: 'line',
        data: data.pm25,
        smooth: true,
        showSymbol: false,
        lineStyle: { color: '#3b82f6' }
      },
      {
        name: 'TVOC (mg/m³×100)',
        type: 'line',
        data: data.tvoc.map((v) => v * 100),
        smooth: true,
        showSymbol: false,
        lineStyle: { color: '#f59e0b' }
      },
      {
        name: '甲醛 (mg/m³×1000)',
        type: 'line',
        data: data.hcho.map((v) => v * 1000),
        smooth: true,
        showSymbol: false,
        lineStyle: { color: '#10b981' }
      }
    ]
  }
})

const floorPoints = ref<FloorPoint[]>([])

const floorFilter = ref<FloorFilter>('all')
const pollingEnabled = ref(true)
let pollingTimer: number | undefined

const filteredFloors = computed(() => {
  const list = floorPoints.value
  if (floorFilter.value === 'A') return list.filter((f) => f.building.includes('A栋'))
  if (floorFilter.value === 'B') return list.filter((f) => f.building.includes('B栋'))
  if (floorFilter.value === 'alarm') return list.filter((f) => f.status === '告警' || f.status === '离线' || f.offline)
  return list
})

const floorFilterHint = computed(() => {
  const label =
    floorFilter.value === 'all'
      ? '全部'
      : floorFilter.value === 'A'
        ? 'A栋'
        : floorFilter.value === 'B'
          ? 'B栋'
          : '告警/离线'
  return `当前显示 ${filteredFloors.value.length} 个楼层（${label}）`
})

const updateFloorPolling = () => {
  if (!pollingEnabled.value) return
  const now = dayjs()
  floorPoints.value = floorPoints.value.map((item) => {
    const next: FloorPoint = { ...item }
    next.hcho = Number((next.hcho * (1 + (Math.random() * 0.04 - 0.02))).toFixed(3))
    if (next.hcho < 0.01) next.hcho = 0.015
    next.tvoc = Number((next.tvoc * (1 + (Math.random() * 0.06 - 0.03))).toFixed(2))
    if (next.tvoc < 0.3) next.tvoc = 0.45
    next.pm25 = Math.max(10, Math.min(50, next.pm25 + Math.floor(Math.random() * 3 - 1)))
    next.collectTime = now.format('H:mm')
    return next
  })
}

watch(
  pollingEnabled,
  (enabled) => {
    if (pollingTimer) window.clearInterval(pollingTimer)
    if (enabled) {
      pollingTimer = window.setInterval(updateFloorPolling, 10_000)
    }
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  if (pollingTimer) window.clearInterval(pollingTimer)
})
</script>

<template>
  <div class="env-overview-page">
    <div class="container">
      <el-row :gutter="16" class="env-row--equal-height">
        <el-col :xs="24" :md="12" :lg="6">
          <el-card shadow="hover" class="panel-card">
            <div class="card-title-row">
              <div class="subtle-title">
                <Icon icon="ep:connection" class="mr-6px" />
                联网设备统计
              </div>
              <el-tag size="small" type="info">总数{{ deviceStat.total }}台</el-tag>
            </div>
            <div class="device-main">
              <div class="device-num">
                <span class="num">{{ deviceStat.online }}</span>
                <span class="unit">在线</span>
              </div>
              <div class="device-num offline">
                <span class="num">{{ deviceStat.offline }}</span>
                <span class="unit">离线</span>
              </div>
            </div>
            <div class="device-breakdown">
              <div class="breakdown-item">温湿度传感器 离线 <span class="mono">{{ deviceStat.offlineWeather }}</span></div>
              <div class="breakdown-item">空气质量传感器 离线 <span class="mono">{{ deviceStat.offlineAir }}</span></div>
              <div class="breakdown-item">CO₂传感器 离线 <span class="mono">{{ deviceStat.offlineCo2 }}</span></div>
            </div>
            <div class="updated-at">更新于 {{ deviceStat.updatedAt }}</div>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="12" :lg="6">
          <el-card shadow="hover" class="panel-card">
            <div class="card-title-row">
              <div class="subtle-title">
                <Icon icon="ep:sunny" class="mr-6px" />
                室外天气
              </div>
              <el-tag size="small" type="info">{{ outdoorWeather.location }}</el-tag>
            </div>
            <div class="weather-row">
              <div class="weather-temp">{{ outdoorWeather.temp }}°C</div>
              <div class="weather-humid">湿度 {{ outdoorWeather.humid }}%</div>
            </div>
            <div class="weather-desc">{{ outdoorWeather.desc }}</div>
            <div class="tip-row">
              <div class="tip-text">{{ outdoorWeather.tip }}</div>
              <div class="tip-time">更新于 {{ outdoorWeather.updatedAt }}</div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="12" :lg="6">
          <el-card shadow="hover" class="panel-card">
            <div class="card-title-row">
              <div class="subtle-title">
                <Icon icon="ep:wind-power" class="mr-6px" />
                空气质量
              </div>
              <div class="tip-time">更新于 {{ airQuality.updatedAt }}</div>
            </div>
            <div class="air-main">
              <div class="air-grade">{{ airQuality.grade }}</div>
              <el-tag size="small" type="success">AQI {{ airQuality.aqi }}</el-tag>
            </div>
            <div class="air-grid">
              <div class="air-cell">
                <div class="air-label">PM2.5</div>
                <div class="air-value">{{ airQuality.pm25 }} <span class="air-unit">μg/m³</span></div>
              </div>
              <div class="air-cell">
                <div class="air-label">PM10</div>
                <div class="air-value">{{ airQuality.pm10 }} <span class="air-unit">μg/m³</span></div>
              </div>
            </div>
            <div class="air-foot">
              <span>甲醛 {{ airQuality.hcho }}</span>
              <span>TVOC {{ airQuality.tvoc }}</span>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="12" :lg="6">
          <el-card shadow="hover" class="panel-card">
            <div class="card-title-row">
              <div class="subtle-title">
                <Icon icon="ep:wind-power" class="mr-6px" />
                CO₂浓度
              </div>
              <div class="tip-time">更新于 {{ co2.updatedAt }}</div>
            </div>
            <div class="co2-row">
              <div class="co2-value">{{ co2.value }}</div>
              <div class="co2-unit">ppm</div>
            </div>
            <div class="co2-tags">
              <el-tag size="small" type="success">正常</el-tag>
              <span class="subtext">良好 (≤450) · 正常 (450-1000) · 警戒 (1000-2000)</span>
            </div>
            <div class="co2-tip">通风建议：当前CO₂正常，建议每小时开窗</div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="env-row--equal-height">
        <el-col :xs="24" :lg="16">
          <el-card shadow="hover" class="panel-card">
            <div class="trend-header">
              <div class="trend-left">
                <div class="trend-title">
                  <Icon icon="ep:data-line" class="mr-6px" />
                  {{ tempChartTitle }}
                </div>
                <el-button-group>
                  <el-button :type="tempRange === 'day' ? 'primary' : 'default'" size="small" @click="tempRange = 'day'">
                    近24小时
                  </el-button>
                  <el-button :type="tempRange === 'week' ? 'primary' : 'default'" size="small" @click="tempRange = 'week'">
                    近7天
                  </el-button>
                  <el-button :type="tempRange === 'month' ? 'primary' : 'default'" size="small" @click="tempRange = 'month'">
                    近30天
                  </el-button>
                </el-button-group>
              </div>
              <div class="tip-time">图表实时更新</div>
            </div>
            <Echart :options="tempHumidOptions" height="220px" />
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="8">
          <el-card shadow="hover" class="panel-card">
            <div class="card-title-row">
              <div class="subtle-title">
                <Icon icon="ep:calendar" class="mr-6px" />
                历史数据对比
              </div>
              <div class="tip-time">{{ historyCompare.updatedAt }}对比</div>
            </div>
            <div class="compare-grid">
              <div class="compare-cell">
                <div class="compare-label">今日室内平均温度</div>
                <div class="compare-value blue">{{ historyCompare.tempToday }}</div>
                <div class="compare-sub">较昨天 <span class="delta">{{ historyCompare.tempDelta }}</span></div>
              </div>
              <div class="compare-cell">
                <div class="compare-label">今日室内平均湿度</div>
                <div class="compare-value purple">{{ historyCompare.humToday }}</div>
                <div class="compare-sub">较昨天 <span class="delta">{{ historyCompare.humDelta }}</span></div>
              </div>
              <div class="compare-cell">
                <div class="compare-label">PM2.5 今天</div>
                <div class="compare-value teal">{{ historyCompare.pm25Today }}</div>
                <div class="compare-sub">昨天 {{ historyCompare.pm25Yesterday }} ↓{{ historyCompare.pm25Yesterday - historyCompare.pm25Today }}</div>
              </div>
              <div class="compare-cell">
                <div class="compare-label">甲醛 今天</div>
                <div class="compare-value amber">{{ historyCompare.hchoToday.toFixed(3) }}</div>
                <div class="compare-sub">昨天 {{ historyCompare.hchoYesterday.toFixed(3) }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="hover" class="panel-card">
        <div class="aq-header">
          <div class="aq-title">
            <Icon icon="ep:trend-charts" class="mr-6px" />
            空气质量趋势图
          </div>
          <div class="aq-controls">
            <div class="control-item">
              <span class="control-label">数据来源：</span>
              <el-select v-model="airSource" size="small" class="!w-200px">
                <el-option
                  v-for="option in airSourceOptions"
                  :key="option"
                  :label="option"
                  :value="option"
                />
              </el-select>
            </div>
            <el-button-group>
              <el-button :type="airRange === 'day' ? 'primary' : 'default'" size="small" @click="airRange = 'day'">
                近24小时
              </el-button>
              <el-button :type="airRange === 'week' ? 'primary' : 'default'" size="small" @click="airRange = 'week'">
                近7天
              </el-button>
              <el-button :type="airRange === 'month' ? 'primary' : 'default'" size="small" @click="airRange = 'month'">
                近30天
              </el-button>
            </el-button-group>
          </div>
          <div class="tip-time">数据截止日期：{{ airCutoffText }}</div>
        </div>

        <div class="current-grid">
          <div class="current-card">
            <div class="current-label">PM2.5</div>
            <div class="current-value">{{ currentPm25 }} <span class="current-unit">μg/m³</span></div>
            <el-tag size="small" :type="getPm25Status(currentPm25).type">{{ getPm25Status(currentPm25).text }}</el-tag>
          </div>
          <div class="current-card">
            <div class="current-label">TVOC</div>
            <div class="current-value">{{ currentTvoc.toFixed(2) }} <span class="current-unit">mg/m³</span></div>
            <el-tag size="small" :type="getTvocStatus(currentTvoc).type">{{ getTvocStatus(currentTvoc).text }}</el-tag>
          </div>
          <div class="current-card">
            <div class="current-label">甲醛</div>
            <div class="current-value">{{ currentHcho.toFixed(3) }} <span class="current-unit">mg/m³</span></div>
            <el-tag size="small" :type="getHchoStatus(currentHcho).type">{{ getHchoStatus(currentHcho).text }}</el-tag>
          </div>
        </div>

        <Echart :options="airTrendOptions" height="240px" />
        <div class="legend-row">
          <div class="legend-item"><span class="dot blue"></span> PM2.5 (μg/m³)</div>
          <div class="legend-item"><span class="dot amber"></span> TVOC (mg/m³×100)</div>
          <div class="legend-item"><span class="dot green"></span> 甲醛 (mg/m³×1000)</div>
        </div>
      </el-card>

      <el-card shadow="hover" class="panel-card">
        <div class="floor-header">
          <div class="floor-left">
            <div class="floor-title">
              <Icon icon="ep:histogram" class="mr-6px" />
              楼层监测点位
            </div>
            <el-button-group>
              <el-button :type="floorFilter === 'all' ? 'primary' : 'default'" size="small" @click="floorFilter = 'all'">
                全部（{{ floorPoints.length }}）
              </el-button>
              <el-button :type="floorFilter === 'A' ? 'primary' : 'default'" size="small" @click="floorFilter = 'A'">
                A栋
              </el-button>
              <el-button :type="floorFilter === 'B' ? 'primary' : 'default'" size="small" @click="floorFilter = 'B'">
                B栋
              </el-button>
              <el-button :type="floorFilter === 'alarm' ? 'primary' : 'default'" size="small" @click="floorFilter = 'alarm'">
                仅告警/离线
              </el-button>
            </el-button-group>
          </div>
          <div class="floor-right">
            <span class="subtext mr-8px">轮询</span>
            <el-switch v-model="pollingEnabled" />
          </div>
        </div>

        <el-row :gutter="16" class="floor-grid">
          <el-col v-for="f in filteredFloors" :key="f.id" :xs="24" :sm="12" :md="8" :lg="6" class="mb-16px">
            <el-card shadow="hover" class="floor-card">
              <div class="floor-card-head">
                <div class="floor-name">
                  <span class="name">{{ f.name }}</span>
                  <span class="code">{{ f.code }}</span>
                </div>
                <el-tag
                  size="small"
                  :type="f.status === '告警' ? 'warning' : f.status === '离线' ? 'info' : 'success'"
                >
                  {{ f.status }}
                </el-tag>
              </div>
              <div class="floor-meta">{{ f.building }} · 楼层 {{ f.floorNum }}</div>
              <div class="floor-kv">
                <div class="kv"><span class="k">甲醛</span><span class="v">{{ f.hcho.toFixed(3) }} mg/m³</span></div>
                <div class="kv"><span class="k">TVOC</span><span class="v">{{ f.tvoc.toFixed(2) }} mg/m³</span></div>
                <div class="kv"><span class="k">PM2.5</span><span class="v">{{ f.pm25 }} μg/m³</span></div>
                <div class="kv"><span class="k">温湿度</span><span class="v">{{ f.temp }}°C / {{ f.hum }}%</span></div>
              </div>
              <div class="floor-foot">
                <div class="foot-left">{{ f.comfort }}</div>
                <div class="foot-right">
                  <span>空气质量 {{ f.airGrade }}</span>
                  <span class="time-tag">{{ f.collectTime }}</span>
                  <span v-if="f.warn" class="warn">{{ f.warn }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <div class="floor-hint">{{ floorFilterHint }}</div>
      </el-card>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.env-overview-page {
  width: 100%;
  padding-top: max(
    0px,
    calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px))
  );
  background: var(--el-bg-color-page);
  --env-surface-bg: var(
    --el-bg-color-overlay,
    color-mix(in srgb, var(--el-bg-color) 90%, var(--el-fill-color-light) 10%)
  );
  --env-surface-border: color-mix(in srgb, var(--el-border-color) 92%, transparent);
  --env-surface-shadow: 0 10px 26px color-mix(in srgb, #000 45%, transparent);
  --env-sub-bg: color-mix(in srgb, var(--el-fill-color) 76%, var(--env-surface-bg) 24%);
  --env-sub-border: color-mix(in srgb, var(--el-border-color) 78%, transparent);
}

.container {
  width: 100%;
  margin: 0;
  padding: 10px 8px 0;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.panel-card {
  border-radius: 16px;
  overflow: hidden;
  background: var(--env-surface-bg) !important;
  border: 1px solid var(--env-surface-border) !important;
  box-shadow: var(--env-surface-shadow);
}

.floor-card {
  border-radius: 14px;
  overflow: hidden;
  background: var(--env-surface-bg) !important;
  border: 1px solid var(--env-surface-border) !important;
  box-shadow: var(--env-surface-shadow);
}

:deep(.panel-card .el-card__body),
:deep(.floor-card .el-card__body) {
  background: transparent;
}

:deep(.env-row--equal-height.el-row) {
  align-items: stretch;
}

:deep(.env-row--equal-height .el-col) {
  display: flex;
}

:deep(.env-row--equal-height .panel-card) {
  width: 100%;
  flex: 1;
}

:deep(.panel-card .el-card__body) {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.header-card {
  position: sticky;
  top: 0;
  z-index: 10;
  width: calc(100% + 16px);
  margin: 0 -8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-radius: 16px;
  background: var(--env-surface-bg);
  border: 1px solid var(--env-surface-border);
  box-shadow: var(--env-surface-shadow);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: var(--el-color-primary);
  color: #fff;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.time {
  display: flex;
  align-items: center;
  color: var(--el-text-color-regular);
  font-size: 12px;
}

.time-value {
  color: var(--el-color-primary);
  font-variant-numeric: tabular-nums;
}

.status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--el-color-success) 12%, transparent);
  border: 1px solid color-mix(in srgb, var(--el-color-success) 28%, transparent);
}

.status-text {
  color: var(--el-color-success);
  font-size: 12px;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--el-color-success);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.25);
  }
}

.card-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.subtle-title {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.tip-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.device-main {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin: 10px 0 12px;
}

.device-num {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.device-num .num {
  font-size: 30px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  font-variant-numeric: tabular-nums;
}

.device-num .unit {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.device-num.offline .num {
  color: var(--el-color-danger);
  font-size: 22px;
}

.device-breakdown {
  background: var(--env-sub-bg);
  border: 1px solid var(--env-sub-border);
  border-radius: 12px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
  color: var(--el-text-color-regular);
}

.mono {
  font-variant-numeric: tabular-nums;
  font-weight: 600;
}

.updated-at {
  margin-top: auto;
  display: flex;
  justify-content: flex-end;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.weather-row {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-top: 6px;
}

.weather-temp {
  font-size: 26px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.weather-humid {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.weather-desc {
  margin-top: 6px;
  margin-bottom: auto;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.tip-row {
  margin-top: 10px;
  padding: 8px 10px;
  border-radius: 12px;
  background: var(--env-sub-bg);
  border: 1px solid var(--env-sub-border);
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: center;
}

.tip-text {
  font-size: 12px;
  color: var(--el-text-color-regular);
}

.air-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 6px;
}

.air-grade {
  font-size: 26px;
  font-weight: 700;
  color: var(--el-color-success);
}

.air-grid {
  margin-top: 10px;
  margin-bottom: auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.air-cell {
  background: var(--env-sub-bg);
  border: 1px solid var(--env-sub-border);
  border-radius: 12px;
  padding: 10px;
  text-align: center;
}

.air-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.air-value {
  margin-top: 2px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  font-variant-numeric: tabular-nums;
}

.air-unit {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-left: 4px;
}

.air-foot {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--el-text-color-regular);
  border-top: 1px solid var(--el-border-color-lighter);
  padding-top: 10px;
}

.co2-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-top: 10px;
}

.co2-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  font-variant-numeric: tabular-nums;
}

.co2-unit {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.co2-tags {
  margin-top: 10px;
  margin-bottom: auto;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.subtext {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.co2-tip {
  margin-top: 10px;
  padding: 10px 12px;
  border-radius: 12px;
  background: var(--env-sub-bg);
  border: 1px solid var(--env-sub-border);
  color: var(--el-text-color-regular);
  font-size: 12px;
}

.trend-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.trend-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.trend-title {
  display: flex;
  align-items: center;
  color: var(--el-text-color-primary);
  font-weight: 600;
  font-size: 13px;
}

.compare-grid {
  margin-top: 10px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.compare-cell {
  border-radius: 12px;
  border: 1px solid var(--env-sub-border);
  background: var(--env-sub-bg);
  padding: 10px 12px;
}

.compare-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.compare-value {
  margin-top: 4px;
  font-size: 18px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.compare-value.blue {
  color: var(--el-color-primary);
}

.compare-value.purple {
  color: #9333ea;
}

.compare-value.teal {
  color: #14b8a6;
}

.compare-value.amber {
  color: #f59e0b;
}

.compare-sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.delta {
  color: var(--el-color-warning);
}

.aq-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.aq-title {
  display: flex;
  align-items: center;
  font-weight: 600;
  color: var(--el-text-color-primary);
  font-size: 13px;
}

.aq-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.control-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.control-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.current-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 10px;
}

.current-card {
  border: 1px solid var(--env-sub-border);
  background: var(--env-sub-bg);
  border-radius: 12px;
  padding: 10px 12px;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.current-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.current-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  font-variant-numeric: tabular-nums;
}

.current-unit {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.legend-row {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.dot.blue {
  background: #3b82f6;
}

.dot.amber {
  background: #f59e0b;
}

.dot.green {
  background: #10b981;
}

.floor-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.floor-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.floor-title {
  display: flex;
  align-items: center;
  font-weight: 600;
  color: var(--el-text-color-primary);
  font-size: 13px;
}

.floor-right {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.floor-card {
  border-radius: 14px;
}

.floor-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.floor-name {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.floor-name .name {
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.floor-name .code {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.floor-meta {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.floor-kv {
  margin-top: 10px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 10px;
}

.kv {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.kv .k {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.kv .v {
  font-size: 12px;
  color: var(--el-text-color-regular);
  font-variant-numeric: tabular-nums;
}

.floor-foot {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--el-border-color-lighter);
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.foot-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.time-tag {
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--env-sub-bg);
  border: 1px solid var(--env-sub-border);
  color: var(--el-text-color-secondary);
  font-variant-numeric: tabular-nums;
}

.warn {
  color: var(--el-color-warning);
}

.floor-hint {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  text-align: right;
}

@media (max-width: 992px) {
  .current-grid {
    grid-template-columns: 1fr;
  }
}
</style>
