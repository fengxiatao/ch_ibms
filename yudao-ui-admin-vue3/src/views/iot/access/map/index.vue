<template>
<ContentWrap
    :body-style="{
      padding: '10px',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      backgroundColor: 'var(--el-bg-color)'
    }"
    style="height: calc(100vh - var(--page-top-gap, 70px)); padding-top: var(--page-top-gap, 70px)"
  >
  <div class="access-map-container">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="设备总数" :value="statistics.total">
            <template #prefix>
              <Icon icon="ep:document" class="stat-icon" />
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card success">
          <el-statistic title="在线设备" :value="statistics.online">
            <template #prefix>
              <Icon icon="ep:circle-check" class="stat-icon" />
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card warning">
          <el-statistic title="离线设备" :value="statistics.offline">
            <template #prefix>
              <Icon icon="ep:warning" class="stat-icon" />
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card danger">
          <el-statistic title="告警设备" :value="statistics.alarm">
            <template #prefix>
              <Icon icon="ep:circle-close" class="stat-icon" />
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 左侧：实时通行记录 + 图表 -->
      <el-col :span="8">
        <!-- 实时通行记录 -->
        <el-card shadow="hover" class="realtime-card mb-4">
          <template #header>
            <div class="card-header">
              <Icon icon="ep:user" />
              <span class="ml-2">实时通行记录</span>
              <el-tag type="success" size="small" class="ml-2">实时更新</el-tag>
            </div>
          </template>
          <el-scrollbar height="300px">
            <div v-loading="realtimeLoading" class="realtime-records">
              <div
                v-for="record in realtimeRecords"
                :key="record.id"
                class="record-item"
                :class="record.openResult === 1 ? 'success' : 'fail'"
              >
                <el-avatar :src="record.imageUrl || '/default-avatar.png'" class="avatar" />
                <div class="record-info">
                  <div class="person-name">{{ record.personName || '未知人员' }}</div>
                  <div class="record-time">{{ formatTime(record.openTime) }}</div>
                </div>
                <div class="record-result">
                  <el-tag v-if="record.openResult === 1" type="success" size="small">
                    成功
                  </el-tag>
                  <el-tag v-else type="danger" size="small">
                    失败
                  </el-tag>
                </div>
              </div>
              <el-empty v-if="realtimeRecords.length === 0" description="暂无通行记录" :image-size="80" />
            </div>
          </el-scrollbar>
        </el-card>

        <!-- 本月门禁告警类型统计 -->
        <el-card shadow="hover" class="chart-card mb-4">
          <template #header>
            <div class="card-header">
              <Icon icon="ep:pie-chart" />
              <span class="ml-2">本月门禁告警类型统计</span>
            </div>
          </template>
          <div ref="alarmChartRef" style="height: 300px;"></div>
        </el-card>

        <!-- 人员通行方式统计 -->
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <Icon icon="ep:histogram" />
              <span class="ml-2">人员通行方式统计</span>
            </div>
          </template>
          <div ref="accessMethodChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>

      <!-- 中间：楼层平面图 - 使用统一的空间布局基础组件 -->
      <el-col :span="12">
        <SpatialLayoutBase
          :devices="accessDevicesForMap"
          device-label="门禁设备"
          :show-left-panel="false"
          :show-right-panel="false"
          :enable-edit="false"
          @floor-change="handleFloorChangeFromBase"
          @device-click="handleDeviceClick"
        />
      </el-col>

      <!-- 右侧：24小时人员进出统计 -->
      <el-col :span="4">
        <el-card shadow="hover" class="chart-card-vertical">
          <template #header>
            <div class="card-header">
              <Icon icon="ep:data-line" />
              <span class="ml-2">24小时人员进出</span>
            </div>
          </template>
          <div ref="hourlyTrafficChartRef" style="height: 850px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
  </ContentWrap>
</template>

<script setup lang="ts" name="AccessMap">
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { formatDate } from '@/utils/formatTime'
import echarts from '@/plugins/echarts'
import * as FloorApi from '@/api/iot/spatial/floor'
import * as DeviceApi from '@/api/iot/device/device'
import * as AccessRecordApi from '@/api/iot/access/record'
import * as AccessAlarmApi from '@/api/iot/access/alarm'
import { AccessOpenTypeEnum } from '@/api/iot/access/alarm'
import SpatialLayoutBase from '@/components/SpatialLayoutBase/index.vue'

// 🆕 门禁告警类型枚举（与后端保持一致）
const AlarmTypeNames: Record<number, string> = {
  1: '非法闯入',
  2: '长时间未关门',
  3: '门磁异常',
  4: '刷卡失败',
  5: '强制开门',
  6: '设备故障'
}

// 🆕 通行方式枚举（与后端保持一致）
const OpenTypeNames: Record<number, string> = {
  1: '远程开门',
  2: '二维码',
  3: '刷卡',
  4: '人脸',
  5: '指纹',
  6: '密码'
}

// 统计数据
const statistics = reactive({
  total: 0,
  online: 0,
  offline: 0,
  alarm: 0
})

// 实时通行记录
const realtimeLoading = ref(false)
const realtimeRecords = ref<any[]>([])
let realtimeTimer: any = null

// 楼层选择
const selectedFloorId = ref<number | undefined>()
const accessDevicesOnFloor = ref<any[]>([])

// 🆕 计算属性：转换为地图展示格式的设备数据
const accessDevicesForMap = computed(() => {
  return accessDevicesOnFloor.value.map(device => ({
    ...device,
    deviceName: device.deviceName || device.nickname,
    state: device.state,
    localX: device.localX,
    localY: device.localY,
    localZ: device.localZ || 0
  }))
})

// 图表引用
const alarmChartRef = ref()
const accessMethodChartRef = ref()
const hourlyTrafficChartRef = ref()
let alarmChart: any = null
let accessMethodChart: any = null
let hourlyTrafficChart: any = null

// 🆕 图表数据
const alarmChartData = ref<any[]>([])
const accessMethodChartData = ref<any[]>([])
const hourlyTrafficChartData = ref<any[]>([])

// 格式化时间（简短）
const formatTime = (time: Date | string | undefined) => {
  if (!time) return '-'
  const date = new Date(time)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}:${date.getSeconds().toString().padStart(2, '0')}`
}

// 加载设备统计
const loadStatistics = async () => {
  try {
    const res = await DeviceApi.getDevicePage({
      pageNo: 1,
      pageSize: 100,
      subsystemCode: 'access.door'
    })
    
    const devices = res.list
    statistics.total = devices.length
    statistics.online = devices.filter(d => d.state === 1).length
    statistics.offline = devices.filter(d => d.state === 0).length
    statistics.alarm = devices.filter(d => d.alarmStatus === 1).length
  } catch (error) {
    console.error('[门禁地图] 加载统计失败:', error)
  }
}

// 加载实时通行记录
const loadRealtimeRecords = async () => {
  try {
    const res = await AccessRecordApi.getAccessRecordPage({
      pageNo: 1,
      pageSize: 20,
      openTime: [new Date(Date.now() - 3600000), new Date()] // 最近1小时
    })
    
    realtimeRecords.value = res.list
  } catch (error) {
    console.error('[门禁地图] 加载实时记录失败:', error)
  }
}

// 启动实时记录定时刷新
const startRealtimeRefresh = () => {
  loadRealtimeRecords()
  realtimeTimer = setInterval(() => {
    loadRealtimeRecords()
  }, 10000) // 每10秒刷新
}

// 停止实时刷新
const stopRealtimeRefresh = () => {
  if (realtimeTimer) {
    clearInterval(realtimeTimer)
    realtimeTimer = null
  }
}

// 🆕 楼层变化事件（来自 SpatialLayoutBase）
const handleFloorChangeFromBase = async (floorId: number | undefined) => {
  selectedFloorId.value = floorId
  if (floorId) {
    await loadAccessDevicesOnFloor()
  } else {
    accessDevicesOnFloor.value = []
  }
}

// 加载楼层门禁设备
const loadAccessDevicesOnFloor = async () => {
  if (!selectedFloorId.value) return
  
  try {
    const res = await DeviceApi.getDevicePage({
      pageNo: 1,
      pageSize: 100,
      floorId: selectedFloorId.value,
      subsystemCode: 'access.door'
    })
    
    accessDevicesOnFloor.value = res.list
  } catch (error) {
    console.error('[门禁地图] 加载楼层设备失败:', error)
  }
}

// 设备点击事件
const handleDeviceClick = (device: any) => {
  console.log('[门禁地图] 点击设备:', device)
  // TODO: 显示设备详情弹窗
}

// 🆕 加载告警类型统计数据
const loadAlarmTypeStatistics = async () => {
  try {
    // 查询本月数据
    const startOfMonth = new Date(new Date().getFullYear(), new Date().getMonth(), 1)
    const endOfMonth = new Date()
    
    const data = await AccessAlarmApi.getAlarmTypeStatistics({
      startTime: startOfMonth,
      endTime: endOfMonth
    })
    
    // 转换数据格式
    const colors = ['#f56c6c', '#e6a23c', '#409eff', '#909399', '#67c23a', '#73c0de']
    alarmChartData.value = data.map((item: any, index: number) => ({
      value: item.count,
      name: AlarmTypeNames[item.alarmType] || `类型${item.alarmType}`,
      itemStyle: { color: colors[index % colors.length] }
    }))
    
    // 刷新图表
    if (alarmChart) {
      updateAlarmChart()
    }
  } catch (error) {
    console.error('[门禁地图] 加载告警统计失败:', error)
    alarmChartData.value = []
  }
}

// 初始化告警类型统计图表
const initAlarmChart = () => {
  if (!alarmChartRef.value) return
  
  alarmChart = echarts.init(alarmChartRef.value)
  updateAlarmChart()
}

// 🆕 更新告警类型统计图表
const updateAlarmChart = () => {
  if (!alarmChart) return
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: '10%',
      top: 'middle',
      textStyle: {
        fontSize: 12
      }
    },
    series: [
      {
        name: '告警类型',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['65%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: alarmChartData.value
      }
    ]
  }
  
  alarmChart.setOption(option)
}

// 🆕 加载通行方式统计数据
const loadAccessMethodStatistics = async () => {
  try {
    // 查询最近30天数据
    const startTime = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
    const endTime = new Date()
    
    const data = await AccessRecordApi.getAccessMethodStatistics({
      startTime,
      endTime
    })
    
    // 转换数据格式
    const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#9a60b4']
    accessMethodChartData.value = data.map((item: any, index: number) => ({
      name: OpenTypeNames[item.openType] || `方式${item.openType}`,
      value: item.count,
      itemStyle: { color: colors[index % colors.length] }
    }))
    
    // 刷新图表
    if (accessMethodChart) {
      updateAccessMethodChart()
    }
  } catch (error) {
    console.error('[门禁地图] 加载通行方式统计失败:', error)
    accessMethodChartData.value = []
  }
}

// 初始化通行方式统计图表
const initAccessMethodChart = () => {
  if (!accessMethodChartRef.value) return
  
  accessMethodChart = echarts.init(accessMethodChartRef.value)
  updateAccessMethodChart()
}

// 🆕 更新通行方式统计图表
const updateAccessMethodChart = () => {
  if (!accessMethodChart) return
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: accessMethodChartData.value.map(item => item.name),
      axisLabel: {
        interval: 0,
        rotate: 0,
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
      name: '次数'
    },
    series: [
      {
        name: '通行次数',
        type: 'bar',
        data: accessMethodChartData.value,
        barWidth: '50%'
      }
    ]
  }
  
  accessMethodChart.setOption(option)
}

// 🆕 加载24小时人员流量统计数据
const loadHourlyTrafficStatistics = async () => {
  try {
    // 查询今天数据
    const data = await AccessRecordApi.getHourlyTrafficStatistics({
      date: new Date()
    })
    
    // 构建完整24小时数据（填充空值为0）
    const hourData = Array.from({ length: 24 }, (_, hour) => {
      const found = data.find((item: any) => item.hour === hour)
      return {
        hour,
        inCount: found?.inCount || 0,
        outCount: found?.outCount || 0
      }
    })
    
    hourlyTrafficChartData.value = hourData
    
    // 刷新图表
    if (hourlyTrafficChart) {
      updateHourlyTrafficChart()
    }
  } catch (error) {
    console.error('[门禁地图] 加载24小时流量统计失败:', error)
    hourlyTrafficChartData.value = []
  }
}

// 初始化24小时人员流量图表
const initHourlyTrafficChart = () => {
  if (!hourlyTrafficChartRef.value) return
  
  hourlyTrafficChart = echarts.init(hourlyTrafficChartRef.value)
  updateHourlyTrafficChart()
}

// 🆕 更新24小时人员流量图表
const updateHourlyTrafficChart = () => {
  if (!hourlyTrafficChart) return
  
  // 生成24小时标签（倒序显示，从23:00到0:00）
  const hours = hourlyTrafficChartData.value.map(item => `${item.hour}:00`).reverse()
  const inData = hourlyTrafficChartData.value.map(item => item.inCount).reverse()
  const outData = hourlyTrafficChartData.value.map(item => item.outCount).reverse()
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['进入', '离开'],
      top: '2%',
      textStyle: {
        fontSize: 11
      }
    },
    grid: {
      left: '10%',
      right: '8%',
      top: '12%',
      bottom: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: '人数',
      nameTextStyle: {
        fontSize: 11
      },
      axisLabel: {
        fontSize: 10
      }
    },
    yAxis: {
      type: 'category',
      data: hours,
      axisLabel: {
        fontSize: 10
      }
    },
    series: [
      {
        name: '进入',
        type: 'bar',
        stack: 'total',
        data: inData,
        itemStyle: { color: '#5470c6' }
      },
      {
        name: '离开',
        type: 'bar',
        stack: 'total',
        data: outData,
        itemStyle: { color: '#91cc75' }
      }
    ]
  }
  
  hourlyTrafficChart.setOption(option)
}

// 窗口大小变化时重绘图表
const handleResize = () => {
  alarmChart?.resize()
  accessMethodChart?.resize()
  hourlyTrafficChart?.resize()
}

// 初始化
onMounted(async () => {
  await loadStatistics()
  startRealtimeRefresh()
  
  // 🆕 加载图表数据
  await Promise.all([
    loadAlarmTypeStatistics(),
    loadAccessMethodStatistics(),
    loadHourlyTrafficStatistics()
  ])
  
  // 初始化图表
  setTimeout(() => {
    initAlarmChart()
    initAccessMethodChart()
    initHourlyTrafficChart()
  }, 100)
  
  window.addEventListener('resize', handleResize)
})

// 清理
onBeforeUnmount(() => {
  stopRealtimeRefresh()
  alarmChart?.dispose()
  accessMethodChart?.dispose()
  hourlyTrafficChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="scss">
.access-map-container {
  padding: 20px;
  
  .stat-card {
    border-left: 4px solid #409eff;
    
    &.success {
      border-left-color: #67c23a;
    }
    
    &.warning {
      border-left-color: #e6a23c;
    }
    
    &.danger {
      border-left-color: #f56c6c;
    }
    
    .stat-icon {
      font-size: 24px;
    }
  }
  
  .card-header {
    display: flex;
    align-items: center;
    font-weight: bold;
  }
  
  .realtime-card {
    .realtime-records {
      .record-item {
        display: flex;
        align-items: center;
        padding: 12px;
        margin-bottom: 8px;
        border-radius: 8px;
        background-color: #f5f7fa;
        transition: all 0.3s;
        
        &:hover {
          background-color: #ecf5ff;
        }
        
        &.success {
          border-left: 4px solid #67c23a;
        }
        
        &.fail {
          border-left: 4px solid #f56c6c;
        }
        
        .avatar {
          margin-right: 12px;
        }
        
        .record-info {
          flex: 1;
          
          .person-name {
            font-weight: bold;
            margin-bottom: 4px;
          }
          
          .record-time {
            font-size: 12px;
            color: #909399;
          }
        }
      }
    }
  }
  
  .map-card {
    height: 920px;
    
    .map-content {
      height: 840px;
      position: relative;
      
      .empty-state {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 100%;
      }
    }
  }
  
  .chart-card {
    height: auto;
  }
  
  .chart-card-vertical {
    height: 920px;
  }
}
</style>
