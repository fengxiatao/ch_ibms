<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { EChartsOption } from 'echarts'
import dayjs from 'dayjs'
import { Echart } from '@/components/Echart'
import { ElMessage } from 'element-plus'
import {
  ParkingReportApi,
  type ParkingDurationRow,
  type ParkingDurationSummary,
  type ParkingPeakRow,
  type ParkingPeakSummary,
  type ParkingCarTypeRow,
  type ParkingCarTypeSummary,
  type ParkingRevenueRow,
  type ParkingRevenueSummary
} from '@/api/access/parkingReport'

defineOptions({ name: 'ParkingReport' })

type ReportTab = 'duration-analysis' | 'peak-analysis' | 'car-type-analysis' | 'revenue-trend'

const activeTab = ref<ReportTab>('duration-analysis')
const queryParams = reactive({
  startDate: '',
  endDate: '',
  granularity: 'day' as 'day' | 'week' | 'month'
})

const durationRows = ref<ParkingDurationRow[]>([])
const durationSummaryCards = ref<ParkingDurationSummary>({
  shortCount: 0,
  midCount: 0,
  longCount: 0,
  avgText: '0分钟'
})

const durationPieOption = computed<EChartsOption>(() => {
  return {
    title: {
      text: '停车时长占比',
      left: 'center',
      textStyle: { fontSize: 16, fontWeight: 600 }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} 辆 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: durationRows.value.map((r) => r.bucket)
    },
    series: [
      {
        name: '停车数量',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 600 } },
        labelLine: { show: false },
        color: ['#1890ff', '#40a9ff', '#69b1ff', '#91c5ff', '#b8d8ff', '#e6f7ff'],
        data: durationRows.value.map((r) => ({ name: r.bucket, value: r.count }))
      }
    ]
  }
})

const durationBarOption = computed<EChartsOption>(() => {
  const x = ['0-1h', '1-2h', '2-4h', '4-8h', '8-12h', '>12h']
  return {
    title: {
      text: '各时长停车数量',
      left: 'center',
      textStyle: { fontSize: 16, fontWeight: 600 }
    },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: x, axisLabel: { fontSize: 12 } },
    yAxis: { type: 'value', name: '车辆数量（辆）', axisLabel: { fontSize: 12 } },
    series: [
      {
        name: '停车数量',
        type: 'bar',
        barWidth: '60%',
        color: '#1890ff',
        data: durationRows.value.map((r) => r.count)
      }
    ]
  }
})

const peakRows = ref<ParkingPeakRow[]>([])
const peakSummaryCards = ref<ParkingPeakSummary>({
  morning: '',
  evening: '',
  normal: '',
  low: ''
})

const peakOption = computed<EChartsOption>(() => {
  const x = peakRows.value.map((r) => r.period)
  return {
    tooltip: { trigger: 'axis' },
    legend: { top: 0, data: ['入场车辆数', '离场车辆数'] },
    grid: { left: 24, right: 24, top: 56, bottom: 24, containLabel: true },
    xAxis: { type: 'category', data: x },
    yAxis: { type: 'value' },
    series: [
      {
        type: 'bar',
        name: '入场车辆数',
        data: peakRows.value.map((r) => r.inCount),
        barMaxWidth: 30,
        color: '#1890ff'
      },
      {
        type: 'bar',
        name: '离场车辆数',
        data: peakRows.value.map((r) => r.outCount),
        barMaxWidth: 30,
        color: '#52c41a'
      }
    ]
  }
})

const carTypeRows = ref<ParkingCarTypeRow[]>([])
const carTypeSummaryCards = ref<ParkingCarTypeSummary>({
  fixed: 0,
  temp: 0,
  free: 0,
  fixedIncomeRate: '0%'
})

const carTypeOption = computed<EChartsOption>(() => {
  return {
    tooltip: { trigger: 'item' },
    legend: { top: 'bottom' },
    series: [
      {
        type: 'pie',
        radius: '70%',
        label: { formatter: '{b}\n{d}%' },
        data: carTypeRows.value.map((r) => ({ name: r.type, value: r.count }))
      }
    ]
  }
})

const carTypeRevenueOption = computed<EChartsOption>(() => {
  return {
    title: { text: '各类型收入贡献', left: 'center', textStyle: { fontSize: 16, fontWeight: 600 } },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 24, right: 24, top: 56, bottom: 24, containLabel: true },
    xAxis: { type: 'category', data: carTypeRows.value.map((r) => r.type) },
    yAxis: { type: 'value' },
    series: [
      {
        type: 'bar',
        name: '收入(元)',
        barMaxWidth: 32,
        color: '#1890ff',
        data: carTypeRows.value.map((r) => r.income)
      }
    ]
  }
})

const revenueRows = ref<ParkingRevenueRow[]>([])
const revenueSummaryCards = ref<ParkingRevenueSummary>({
  avgIncome: '¥0.00',
  weekendVsWorkday: '1:1',
  holidayGrowth: '0%',
  monthlyGrowth: '0%'
})

const revenueOption = computed<EChartsOption>(() => {
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 24, right: 24, top: 24, bottom: 24, containLabel: true },
    xAxis: { type: 'category', data: revenueRows.value.map((r) => r.period) },
    yAxis: { type: 'value' },
    series: [
      {
        type: 'line',
        name: '总收入',
        data: revenueRows.value.map((r) => r.total),
        smooth: true
      }
    ]
  }
})

const handleQuery = async () => {
  if (!queryParams.startDate || !queryParams.endDate) {
    ElMessage.warning('请先选择开始和结束日期')
    return
  }
  const res = await ParkingReportApi.getOverview({
    startDate: queryParams.startDate,
    endDate: queryParams.endDate,
    granularity: queryParams.granularity
  })
  durationRows.value = res.durationRows || []
  durationSummaryCards.value = res.durationSummary || durationSummaryCards.value
  peakRows.value = res.peakRows || []
  peakSummaryCards.value = res.peakSummary || peakSummaryCards.value
  carTypeRows.value = res.carTypeRows || []
  carTypeSummaryCards.value = res.carTypeSummary || carTypeSummaryCards.value
  revenueRows.value = res.revenueRows || []
  revenueSummaryCards.value = res.revenueSummary || revenueSummaryCards.value
  ElMessage.success('查询完成')
}

const resetQuery = () => {
  const end = dayjs()
  queryParams.startDate = end.subtract(12, 'day').format('YYYY-MM-DD')
  queryParams.endDate = end.format('YYYY-MM-DD')
  queryParams.granularity = 'day'
}

const exportCsv = (fileName: string, headers: string[], rows: Array<Array<string | number>>) => {
  const lines = [
    headers.join(','),
    ...rows.map((r) => r.map((c) => String(c).replaceAll(',', ' ')).join(','))
  ]
  const blob = new Blob([`\uFEFF${lines.join('\n')}`], { type: 'text/csv;charset=utf-8;' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = fileName
  a.click()
  window.URL.revokeObjectURL(url)
}

const handleExport = () => {
  const stamp = dayjs().format('YYYYMMDD_HHmmss')
  if (activeTab.value === 'duration-analysis') {
    exportCsv(
      `停车时长分布分析_${stamp}.csv`,
      ['时长区间', '车辆数量', '占比', '平均收费金额', '总收入贡献'],
      durationRows.value.map((r) => [
        r.bucket,
        r.count,
        `${(r.rate * 100).toFixed(1)}%`,
        `¥${r.avgFee.toFixed(2)}`,
        `¥${r.income.toFixed(2)}`
      ])
    )
    ElMessage.success('已导出报表')
    return
  }
  if (activeTab.value === 'peak-analysis') {
    exportCsv(
      `时段流量分析_${stamp}.csv`,
      ['时段', '入场车辆数', '离场车辆数', '净增车辆数', '平均通行时间', '拥堵指数'],
      peakRows.value.map((r) => [r.period, r.inCount, r.outCount, r.net, r.avgTime, r.congestion])
    )
    ElMessage.success('已导出报表')
    return
  }
  if (activeTab.value === 'car-type-analysis') {
    exportCsv(
      `车辆类型分析_${stamp}.csv`,
      ['车辆类型', '数量', '占比', '月均消费', '总收入贡献', '收入占比'],
      carTypeRows.value.map((r) => [
        r.type,
        r.count,
        `${(r.rate * 100).toFixed(1)}%`,
        r.monthlyAvgText,
        `¥${Number(r.income || 0).toFixed(2)}`,
        `${(r.incomeRate * 100).toFixed(0)}%`
      ])
    )
    ElMessage.success('已导出报表')
    return
  }
  exportCsv(
    `收益趋势分析_${stamp}.csv`,
    ['统计周期', '总收入', '环比增长', '同比增长', '日均收入'],
    revenueRows.value.map((r) => [
      r.period,
      `¥${r.total.toFixed(2)}`,
      r.momText,
      r.yoyText,
      r.avgText
    ])
  )
  ElMessage.success('已导出报表')
}

onMounted(async () => {
  const end = dayjs()
  queryParams.startDate = end.subtract(12, 'day').format('YYYY-MM-DD')
  queryParams.endDate = end.format('YYYY-MM-DD')
  await handleQuery()
})
</script>

<template>
  <div class="parking-page parking-proto">
    <ContentWrap>
      <ContentWrap>
        <el-form class="-mb-15px" :inline="true" label-position="top" @submit.prevent>
          <el-form-item label="开始日期">
            <el-date-picker
              v-model="queryParams.startDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择开始日期"
              class="!w-180px"
            />
          </el-form-item>
          <el-form-item label="结束日期">
            <el-date-picker
              v-model="queryParams.endDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择结束日期"
              class="!w-180px"
            />
          </el-form-item>
          <el-form-item label="统计粒度">
            <el-select v-model="queryParams.granularity" class="!w-120px">
              <el-option label="按天" value="day" />
              <el-option label="按周" value="week" />
              <el-option label="按月" value="month" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">
              <Icon icon="ep:search" class="mr-5px" />
              查询
            </el-button>
            <el-button @click="resetQuery">
              <Icon icon="ep:refresh" class="mr-5px" />
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </ContentWrap>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="停车时长分布分析" name="duration-analysis">
          <el-card shadow="hover" class="mb-4">
            <div class="flex items-center justify-between">
              <div class="font-bold">停车时长分布分析</div>
              <el-button type="primary" link @click="handleExport">导出报表</el-button>
            </div>
            <el-row :gutter="16" class="mt-4">
              <el-col :span="6">
                <el-card shadow="never">
                  <div class="text-sm text-[var(--el-text-color-secondary)]"
                    >短时停车（≤2小时）</div
                  >
                  <div class="text-2xl font-bold text-[var(--el-color-primary)]"
                    >{{ durationSummaryCards.shortCount }} 辆</div
                  >
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="never">
                  <div class="text-sm text-[var(--el-text-color-secondary)]"
                    >中时停车（2-8小时）</div
                  >
                  <div class="text-2xl font-bold text-[var(--el-color-primary)]"
                    >{{ durationSummaryCards.midCount }} 辆</div
                  >
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="never">
                  <div class="text-sm text-[var(--el-text-color-secondary)]"
                    >长时停车（>8小时）</div
                  >
                  <div class="text-2xl font-bold text-[var(--el-color-primary)]"
                    >{{ durationSummaryCards.longCount }} 辆</div
                  >
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="never">
                  <div class="text-sm text-[var(--el-text-color-secondary)]">平均停车时长</div>
                  <div class="text-2xl font-bold text-[var(--el-color-primary)]">{{
                    durationSummaryCards.avgText
                  }}</div>
                </el-card>
              </el-col>
            </el-row>
            <el-row :gutter="16" class="mt-4">
              <el-col :span="12">
                <Echart :options="durationPieOption" height="400px" />
              </el-col>
              <el-col :span="12">
                <Echart :options="durationBarOption" height="400px" />
              </el-col>
            </el-row>
            <el-table :data="durationRows" border class="mt-4">
              <el-table-column label="时长区间" prop="bucket" />
              <el-table-column label="车辆数量" prop="count" align="right" width="100" />
              <el-table-column label="占比" align="right" width="90">
                <template #default="{ row }">{{ (row.rate * 100).toFixed(1) }}%</template>
              </el-table-column>
              <el-table-column label="平均收费金额" align="right" width="140">
                <template #default="{ row }">¥{{ row.avgFee.toFixed(2) }}</template>
              </el-table-column>
              <el-table-column label="总收入贡献" align="right" width="140">
                <template #default="{ row }">¥{{ row.income.toFixed(2) }}</template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="时段流量分析" name="peak-analysis">
          <el-card shadow="hover" class="mb-4">
            <div class="flex items-center justify-between">
              <div class="font-bold">时段流量分析</div>
              <el-button type="primary" link @click="handleExport">导出报表</el-button>
            </div>
            <el-row :gutter="16" class="mt-4">
              <el-col :span="6">
                <el-card shadow="never">
                  <div class="text-sm text-[var(--el-text-color-secondary)]">早高峰时段</div>
                  <div class="text-2xl font-bold text-[var(--el-color-primary)]">{{
                    peakSummaryCards.morning
                  }}</div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="never">
                  <div class="text-sm text-[var(--el-text-color-secondary)]">晚高峰时段</div>
                  <div class="text-2xl font-bold text-[var(--el-color-primary)]">{{
                    peakSummaryCards.evening
                  }}</div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="never">
                  <div class="text-sm text-[var(--el-text-color-secondary)]">平峰时段</div>
                  <div class="text-2xl font-bold text-[var(--el-color-primary)]">{{
                    peakSummaryCards.normal
                  }}</div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="never">
                  <div class="text-sm text-[var(--el-text-color-secondary)]">低谷时段</div>
                  <div class="text-2xl font-bold text-[var(--el-color-primary)]">{{
                    peakSummaryCards.low
                  }}</div>
                </el-card>
              </el-col>
            </el-row>
            <div class="mt-4">
              <Echart :options="peakOption" height="400px" />
            </div>
            <el-table :data="peakRows" border class="mt-4">
              <el-table-column label="时段" prop="period" />
              <el-table-column label="入场车辆数" prop="inCount" align="right" width="110" />
              <el-table-column label="离场车辆数" prop="outCount" align="right" width="110" />
              <el-table-column label="净增车辆数" prop="net" align="right" width="110" />
              <el-table-column label="平均通行时间" prop="avgTime" align="center" width="120" />
              <el-table-column label="拥堵指数" prop="congestion" align="center" width="110" />
            </el-table>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="车辆类型分析" name="car-type-analysis">
          <el-card shadow="hover" class="mb-4">
            <div class="flex items-center justify-between">
              <div class="font-bold">车辆类型分析</div>
              <el-button type="primary" link @click="handleExport">导出报表</el-button>
            </div>
            <el-row :gutter="16" class="mt-4">
              <el-col :span="6">
                <el-card shadow="never">
                  <div class="text-sm text-[var(--el-text-color-secondary)]">固定车（月/年租）</div>
                  <div class="text-2xl font-bold text-[var(--el-color-primary)]"
                    >{{ carTypeSummaryCards.fixed }} 辆</div
                  >
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="never">
                  <div class="text-sm text-[var(--el-text-color-secondary)]">临时车</div>
                  <div class="text-2xl font-bold text-[var(--el-color-primary)]"
                    >{{ carTypeSummaryCards.temp }} 辆</div
                  >
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="never">
                  <div class="text-sm text-[var(--el-text-color-secondary)]">免费车</div>
                  <div class="text-2xl font-bold text-[var(--el-color-primary)]"
                    >{{ carTypeSummaryCards.free }} 辆</div
                  >
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="never">
                  <div class="text-sm text-[var(--el-text-color-secondary)]">月卡收入占比</div>
                  <div class="text-2xl font-bold text-[var(--el-color-primary)]">{{
                    carTypeSummaryCards.fixedIncomeRate
                  }}</div>
                </el-card>
              </el-col>
            </el-row>
            <el-row :gutter="16" class="mt-4">
              <el-col :span="12">
                <Echart :options="carTypeOption" height="400px" />
              </el-col>
              <el-col :span="12">
                <Echart :options="carTypeRevenueOption" height="400px" />
              </el-col>
            </el-row>
            <el-table :data="carTypeRows" border class="mt-4">
              <el-table-column label="车辆类型" prop="type" />
              <el-table-column label="数量" prop="count" align="right" width="90" />
              <el-table-column label="占比" align="right" width="90">
                <template #default="{ row }">{{ (row.rate * 100).toFixed(1) }}%</template>
              </el-table-column>
              <el-table-column label="月均消费" align="right" width="130">
                <template #default="{ row }">{{ row.monthlyAvgText }}</template>
              </el-table-column>
              <el-table-column label="总收入贡献" align="right" width="140">
                <template #default="{ row }">¥{{ Number(row.income || 0).toFixed(2) }}</template>
              </el-table-column>
              <el-table-column label="收入占比" align="right" width="90">
                <template #default="{ row }">{{ (row.incomeRate * 100).toFixed(1) }}%</template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="收益趋势分析" name="revenue-trend">
          <el-card shadow="hover" class="mb-4">
            <div class="flex items-center justify-between">
              <div class="font-bold">收益趋势分析</div>
              <el-button type="primary" link @click="handleExport">导出报表</el-button>
            </div>
            <el-row :gutter="16" class="mt-4">
              <el-col :span="6">
                <el-card shadow="never">
                  <div class="text-sm text-[var(--el-text-color-secondary)]">日均收入</div>
                  <div class="text-2xl font-bold text-[var(--el-color-primary)]">{{
                    revenueSummaryCards.avgIncome
                  }}</div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="never">
                  <div class="text-sm text-[var(--el-text-color-secondary)]">周末vs工作日</div>
                  <div class="text-2xl font-bold text-[var(--el-color-primary)]">{{
                    revenueSummaryCards.weekendVsWorkday
                  }}</div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="never">
                  <div class="text-sm text-[var(--el-text-color-secondary)]">节假日增长</div>
                  <div class="text-2xl font-bold text-[var(--el-color-primary)]">{{
                    revenueSummaryCards.holidayGrowth
                  }}</div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="never">
                  <div class="text-sm text-[var(--el-text-color-secondary)]">月度增长率</div>
                  <div class="text-2xl font-bold text-[var(--el-color-primary)]">{{
                    revenueSummaryCards.monthlyGrowth
                  }}</div>
                </el-card>
              </el-col>
            </el-row>
            <div class="mt-4">
              <Echart :options="revenueOption" height="400px" />
            </div>
            <el-table :data="revenueRows" border class="mt-4">
              <el-table-column label="统计周期" prop="period" />
              <el-table-column label="总收入" align="right" width="140">
                <template #default="{ row }">¥{{ row.total.toFixed(2) }}</template>
              </el-table-column>
              <el-table-column label="环比增长" align="right" width="100">
                <template #default="{ row }">{{ row.momText }}</template>
              </el-table-column>
              <el-table-column label="同比增长" align="right" width="100">
                <template #default="{ row }">{{ row.yoyText }}</template>
              </el-table-column>
              <el-table-column label="日均收入" align="right" width="120">
                <template #default="{ row }">{{ row.avgText }}</template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </ContentWrap>
  </div>
</template>

<style scoped lang="scss">
@use './prototype.scss' as *;

.parking-page {
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
  box-sizing: border-box;
}
</style>
