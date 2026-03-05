<template>
  <div class="energy-page">
    <!-- 能耗总览 -->
    <el-row :gutter="16" class="mb-16px">
      <el-col :span="6">
        <el-card shadow="hover" class="overview-card electric">
          <div class="card-icon"><Icon icon="mdi:flash" /></div>
          <div class="card-content">
            <div class="card-title">用电量</div>
            <div class="card-value">{{ overview.todayElectricity || 0 }} <span>kWh</span></div>
            <div class="card-sub">
              本月: {{ overview.monthElectricity || 0 }} kWh
              <el-tag :type="getGrowthType(overview.electricityMom)" size="small">
                {{ getGrowthLabel(overview.electricityMom) }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="overview-card water">
          <div class="card-icon"><Icon icon="mdi:water" /></div>
          <div class="card-content">
            <div class="card-title">用水量</div>
            <div class="card-value">{{ overview.todayWater || 0 }} <span>m³</span></div>
            <div class="card-sub">
              本月: {{ overview.monthWater || 0 }} m³
              <el-tag :type="getGrowthType(overview.waterMom)" size="small">
                {{ getGrowthLabel(overview.waterMom) }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="overview-card gas">
          <div class="card-icon"><Icon icon="mdi:fire" /></div>
          <div class="card-content">
            <div class="card-title">燃气量</div>
            <div class="card-value">{{ overview.todayGas || 0 }} <span>m³</span></div>
            <div class="card-sub">本月: {{ overview.monthGas || 0 }} m³</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="overview-card cold">
          <div class="card-icon"><Icon icon="mdi:snowflake" /></div>
          <div class="card-content">
            <div class="card-title">冷量</div>
            <div class="card-value">{{ overview.todayCold || 0 }} <span>kWh</span></div>
            <div class="card-sub">本月: {{ overview.monthCold || 0 }} kWh</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 仪表状态 -->
    <el-row :gutter="16" class="mb-16px">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span>仪表状态</span></template>
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="meter-stat">
                <div class="stat-num">{{ overview.meterTotalCount || 0 }}</div>
                <div class="stat-label">总数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="meter-stat">
                <div class="stat-num success">{{ overview.meterOnlineCount || 0 }}</div>
                <div class="stat-label">在线</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="meter-stat">
                <div class="stat-num info">{{ overview.meterOfflineCount || 0 }}</div>
                <div class="stat-label">离线</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="meter-stat">
                <div class="stat-num danger">{{ overview.meterFaultCount || 0 }}</div>
                <div class="stat-label">故障</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span>仪表类型分布</span></template>
          <div class="meter-type-list">
            <div class="type-item"
              ><Icon icon="mdi:flash" color="#409EFF" /> 电表:
              {{ overview.electricMeterCount || 0 }}</div
            >
            <div class="type-item"
              ><Icon icon="mdi:water" color="#67C23A" /> 水表:
              {{ overview.waterMeterCount || 0 }}</div
            >
            <div class="type-item"
              ><Icon icon="mdi:fire" color="#E6A23C" /> 燃气表:
              {{ overview.gasMeterCount || 0 }}</div
            >
            <div class="type-item"
              ><Icon icon="mdi:snowflake" color="#909399" /> 冷/热量表:
              {{ (overview.coldMeterCount || 0) + (overview.heatMeterCount || 0) }}</div
            >
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="alarm-card">
          <template #header><span>告警信息</span></template>
          <div class="alarm-stat">
            <div class="alarm-item">
              <div class="alarm-num danger">{{ overview.unhandledAlarmCount || 0 }}</div>
              <div class="alarm-label">未处理告警</div>
            </div>
            <el-button type="danger" size="small" @click="goToAlarm">查看详情</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 能耗趋势图表 -->
    <ContentWrap title="能耗趋势">
      <div ref="chartRef" class="chart-container"></div>
    </ContentWrap>

    <!-- 仪表列表 -->
    <ContentWrap title="能耗仪表">
      <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true">
        <el-form-item label="仪表" prop="meterName">
          <el-input
            v-model="queryParams.meterName"
            placeholder="编码/名称"
            clearable
            @keyup.enter="handleQuery"
            class="!w-160px"
          />
        </el-form-item>
        <el-form-item label="类型" prop="meterType">
          <el-select
            v-model="queryParams.meterType"
            placeholder="请选择类型"
            clearable
            class="!w-140px"
          >
            <el-option label="电表" :value="1" />
            <el-option label="水表" :value="2" />
            <el-option label="燃气表" :value="3" />
            <el-option label="冷量表" :value="4" />
            <el-option label="热量表" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            class="!w-120px"
          >
            <el-option label="在线" :value="1" />
            <el-option label="离线" :value="0" />
            <el-option label="故障" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" />搜索</el-button>
          <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" />重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="meterList" :stripe="true" class="mt-16px">
        <el-table-column label="仪表编码" prop="meterCode" width="140" />
        <el-table-column label="仪表名称" prop="meterName" min-width="160" />
        <el-table-column label="类型" prop="meterType" width="100">
          <template #default="{ row }">{{ getMeterTypeName(row.meterType) }}</template>
        </el-table-column>
        <el-table-column label="区域" prop="areaName" width="120" />
        <el-table-column label="当前读数" prop="currentReading" width="120" align="right">
          <template #default="{ row }">{{ row.currentReading?.toFixed(1) || '--' }}</template>
        </el-table-column>
        <el-table-column label="今日用量" prop="todayUsage" width="120" align="right">
          <template #default="{ row }">{{ row.todayUsage?.toFixed(2) || '--' }}</template>
        </el-table-column>
        <el-table-column label="本月用量" prop="monthUsage" width="120" align="right">
          <template #default="{ row }">{{ row.monthUsage?.toFixed(2) || '--' }}</template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{
              getStatusLabel(row.status)
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openMeterDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import * as EnergyApi from '@/api/iot/building/energy'
import type { IbmsEnergyMeterVO, IbmsEnergyOverviewVO } from '@/api/iot/building/energy'
import * as echarts from 'echarts'

defineOptions({ name: 'BuildingEnergy' })

const router = useRouter()
const loading = ref(true)
const meterList = ref<IbmsEnergyMeterVO[]>([])
const overview = ref<IbmsEnergyOverviewVO>({})
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  meterName: undefined,
  meterType: undefined,
  status: undefined
})
const queryFormRef = ref()
const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

const getMeterTypeName = (type: number) => {
  const map = { 1: '电表', 2: '水表', 3: '燃气表', 4: '冷量表', 5: '热量表' }
  return map[type] || '未知'
}

const getStatusType = (status: number) => {
  const map = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const getStatusLabel = (status: number) => {
  const map = { 0: '离线', 1: '在线', 2: '故障' }
  return map[status] || '未知'
}

const getGrowthType = (value: number) => {
  if (value > 0) return 'danger'
  if (value < 0) return 'success'
  return 'info'
}

const getGrowthLabel = (value: number) => {
  if (!value) return '持平'
  return value > 0 ? `↑${value}%` : `↓${Math.abs(value)}%`
}

const getOverview = async () => {
  try {
    overview.value = await EnergyApi.getOverview()
  } catch (e) {
    console.error('获取能耗总览失败', e)
  }
}

const getList = async () => {
  loading.value = true
  try {
    const data = await EnergyApi.getMeterPage(queryParams)
    meterList.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

const loadChart = async () => {
  if (!chartRef.value) return
  try {
    const endDate = new Date().toISOString().split('T')[0]
    const startDate = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
    const data = await EnergyApi.getStatisticsByDateRange(startDate, endDate)

    if (!chartInstance) {
      chartInstance = echarts.init(chartRef.value)
    }

    // 按日期分组
    const dateMap = new Map<string, { electricity: number; water: number; gas: number }>()
    data.forEach((item) => {
      const date = item.statisticsDate
      if (!dateMap.has(date)) {
        dateMap.set(date, { electricity: 0, water: 0, gas: 0 })
      }
      const stat = dateMap.get(date)!
      if (item.meterType === 1) stat.electricity += item.dailyUsage || 0
      if (item.meterType === 2) stat.water += item.dailyUsage || 0
      if (item.meterType === 3) stat.gas += item.dailyUsage || 0
    })

    const dates = Array.from(dateMap.keys()).sort()
    const electricityData = dates.map((d) => dateMap.get(d)?.electricity || 0)
    const waterData = dates.map((d) => dateMap.get(d)?.water || 0)
    const gasData = dates.map((d) => dateMap.get(d)?.gas || 0)

    chartInstance.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['用电量(kWh)', '用水量(m³)', '燃气量(m³)'] },
      xAxis: { type: 'category', data: dates },
      yAxis: [
        { type: 'value', name: '用电量(kWh)', position: 'left' },
        { type: 'value', name: '用水/燃气量', position: 'right' }
      ],
      series: [
        { name: '用电量(kWh)', type: 'bar', data: electricityData },
        { name: '用水量(m³)', type: 'line', yAxisIndex: 1, data: waterData },
        { name: '燃气量(m³)', type: 'line', yAxisIndex: 1, data: gasData }
      ]
    })
  } catch (e) {
    console.error('加载图表失败', e)
  }
}

const goToAlarm = () => {
  router.push('/iot/building/energy/alarm')
}

const openMeterDetail = (meter: IbmsEnergyMeterVO) => {
  // TODO: 打开详情弹窗
  console.log('查看仪表详情', meter)
}

onMounted(() => {
  getOverview()
  getList()
  loadChart()
})

onUnmounted(() => {
  chartInstance?.dispose()
})
</script>

<style lang="scss" scoped>
.energy-page {
  padding: 16px;
  padding-top: calc(
    16px + max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 16px)))
  );
  box-sizing: border-box;
  height: 100%;
  overflow: auto;
}

.overview-card {
  display: flex;
  align-items: center;
  padding: 20px;
  .card-icon {
    width: 60px;
    height: 60px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: #fff;
    margin-right: 16px;
  }
  &.electric .card-icon {
    background: linear-gradient(135deg, #409eff, #79bbff);
  }
  &.water .card-icon {
    background: linear-gradient(135deg, #67c23a, #95d475);
  }
  &.gas .card-icon {
    background: linear-gradient(135deg, #e6a23c, #eebe77);
  }
  &.cold .card-icon {
    background: linear-gradient(135deg, #909399, #b1b3b8);
  }
  .card-content {
    flex: 1;
    .card-title {
      font-size: 14px;
      color: var(--el-text-color-secondary);
    }
    .card-value {
      font-size: 28px;
      font-weight: bold;
      color: var(--el-text-color-primary);
      span {
        font-size: 14px;
        font-weight: normal;
        color: var(--el-text-color-secondary);
      }
    }
    .card-sub {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      margin-top: 4px;
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }
}

.meter-stat {
  text-align: center;
  .stat-num {
    font-size: 24px;
    font-weight: bold;
    color: var(--el-text-color-primary);
    &.success {
      color: var(--el-color-success);
    }
    &.info {
      color: var(--el-color-info);
    }
    &.danger {
      color: var(--el-color-danger);
    }
  }
  .stat-label {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-top: 4px;
  }
}

.meter-type-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  .type-item {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: var(--el-text-color-regular);
  }
}

.alarm-card .alarm-stat {
  display: flex;
  justify-content: space-between;
  align-items: center;
  .alarm-item {
    text-align: center;
    .alarm-num {
      font-size: 32px;
      font-weight: bold;
      &.danger {
        color: var(--el-color-danger);
      }
    }
    .alarm-label {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
}

.chart-container {
  height: 300px;
}
</style>
