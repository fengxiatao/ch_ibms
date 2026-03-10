<template>
  <div class="visitor-home">
    <div class="page-header">
      <h1 class="page-title">访客管理仪表盘</h1>
    </div>

    <!-- 顶部统计卡片 -->
    <div class="stats-row">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-card__head">
          <span class="stat-card__label">待审核申请</span>
          <Icon icon="ep:document" class="stat-card__icon stat-card__icon--primary" />
        </div>
        <div class="stat-card__value">{{ stats.pendingApproval ?? 0 }}</div>
        <p class="stat-card__tip">等待审核的访客预约申请</p>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-card__head">
          <span class="stat-card__label">当前在园</span>
          <Icon icon="ep:user" class="stat-card__icon stat-card__icon--info" />
        </div>
        <div class="stat-card__value">{{ stats.currentVisitors ?? 0 }}</div>
        <p class="stat-card__tip">当前在园访客数</p>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-card__head">
          <span class="stat-card__label">今日访客</span>
          <Icon icon="ep:calendar" class="stat-card__icon stat-card__icon--success" />
        </div>
        <div class="stat-card__value">{{ stats.todayAppointments ?? 0 }}</div>
        <p class="stat-card__tip">今日签到访客数</p>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-card__head">
          <span class="stat-card__label">总访客数</span>
          <Icon icon="ep:data-analysis" class="stat-card__icon stat-card__icon--warning" />
        </div>
        <div class="stat-card__value">{{ formatNum(stats.monthlyTotal) }}</div>
        <p class="stat-card__tip">累计访客总数</p>
      </el-card>
    </div>

    <!-- 日期筛选 -->
    <el-card class="filter-card" shadow="never">
      <div class="filter-row">
        <span class="filter-label">日期筛选</span>
        <div class="filter-btns">
          <el-button
            :type="dateRange === 'today' ? 'primary' : 'default'"
            size="small"
            @click="dateRange = 'today'"
          >
            今日
          </el-button>
          <el-button
            :type="dateRange === 'week' ? 'primary' : 'default'"
            size="small"
            @click="dateRange = 'week'"
          >
            本周
          </el-button>
          <el-button
            :type="dateRange === 'month' ? 'primary' : 'default'"
            size="small"
            @click="dateRange = 'month'"
          >
            本月
          </el-button>
          <el-button
            :type="dateRange === 'year' ? 'primary' : 'default'"
            size="small"
            @click="dateRange = 'year'"
          >
            本年
          </el-button>
        </div>
        <div class="filter-date">
          <el-date-picker
            v-model="dateScope"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            size="small"
            class="date-picker"
          />
          <el-button size="small" @click="handleResetDate">重置</el-button>
        </div>
      </div>
    </el-card>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <el-card class="chart-card" shadow="never">
        <h3 class="chart-title">来访事由分布</h3>
        <p class="chart-desc">各来访事由访客占比统计</p>
        <div class="chart-wrap">
          <Echart :options="reasonChartOption" height="260" />
        </div>
      </el-card>
      <el-card class="chart-card" shadow="never">
        <h3 class="chart-title">被访人排行</h3>
        <p class="chart-desc">人员被访问次数排行</p>
        <el-table :data="hostRankList" size="small" stripe>
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="name" label="被访人员" />
          <el-table-column prop="dept" label="被访单位/部门" min-width="140" />
          <el-table-column prop="count" label="被访次数" width="90" />
          <el-table-column prop="percent" label="次数占比" width="90">
            <template #default="{ row }">
              <span class="text-primary">{{ row.percent }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
      <el-card class="chart-card" shadow="never">
        <h3 class="chart-title">访客趋势</h3>
        <p class="chart-desc">最近7天访客数量变化</p>
        <div class="chart-wrap">
          <Echart :options="trendChartOption" height="260" />
        </div>
      </el-card>
      <el-card class="chart-card" shadow="never">
        <h3 class="chart-title">异常事件分布</h3>
        <p class="chart-desc">访客异常行为分布情况</p>
        <div class="chart-wrap">
          <Echart :options="abnormalChartOption" height="260" />
        </div>
      </el-card>
      <el-card class="chart-card chart-card--full" shadow="never">
        <h3 class="chart-title">来访时段分布</h3>
        <p class="chart-desc">访客签到时段分布情况</p>
        <div class="chart-wrap">
          <Echart :options="timeChartOption" height="260" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { Icon } from '@iconify/vue'
import type { EChartsOption } from 'echarts'
import { Echart } from '@/components/Echart'
import { NewVisitorManagementApi } from '@/api/iot/visitor/newVisitorManagement'

defineOptions({ name: 'VisitorHome' })

const dateRange = ref<'today' | 'week' | 'month' | 'year'>('today')
const dateScope = ref<[string, string] | null>(null)

const stats = ref({
  pendingApproval: 0,
  currentVisitors: 0,
  todayAppointments: 0,
  monthlyTotal: 0
})

const formatNum = (n: number) => (n != null ? n.toLocaleString() : '0')

const handleResetDate = () => {
  dateRange.value = 'today'
  dateScope.value = null
}

// 仪表盘数据（从接口获取）
const hostRankList = ref<{ name: string; dept: string; count: number; percent: string }[]>([])
const reasonDistribution = ref<{ name: string; value: number }[]>([])
const trendData = ref<{ date: string; count: number }[]>([])
const abnormalDistribution = ref<{ name: string; value: number }[]>([])
const timeDistribution = ref<{ name: string; value: number }[]>([])

const loadDashboard = () => {
  let dateFrom: string | undefined
  let dateTo: string | undefined
  if (dateScope.value && dateScope.value.length === 2) {
    dateFrom = dateScope.value[0]
    dateTo = dateScope.value[1]
  } else {
    const today = new Date()
    const pad = (n: number) => String(n).padStart(2, '0')
    if (dateRange.value === 'today') {
      dateFrom = dateTo = `${today.getFullYear()}-${pad(today.getMonth() + 1)}-${pad(today.getDate())}`
    } else if (dateRange.value === 'week') {
      const d = new Date(today)
      d.setDate(d.getDate() - 6)
      dateFrom = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
      dateTo = `${today.getFullYear()}-${pad(today.getMonth() + 1)}-${pad(today.getDate())}`
    } else if (dateRange.value === 'month') {
      dateFrom = `${today.getFullYear()}-${pad(today.getMonth() + 1)}-01`
      dateTo = `${today.getFullYear()}-${pad(today.getMonth() + 1)}-${pad(today.getDate())}`
    } else {
      dateFrom = `${today.getFullYear()}-01-01`
      dateTo = `${today.getFullYear()}-${pad(today.getMonth() + 1)}-${pad(today.getDate())}`
    }
  }
  NewVisitorManagementApi.getDashboard({ dateFrom, dateTo })
    .then((res: any) => {
      const d = res?.data
      if (!d) return
      hostRankList.value = (d.hostRank || []).map((r: any) => ({
        name: r.name || '',
        dept: r.dept || '',
        count: r.count ?? 0,
        percent: r.percent || '0%'
      }))
      reasonDistribution.value = (d.reasonDistribution || []).map((r: any) => ({
        name: r.name != null ? String(r.name) : '',
        value: Number(r.value) || 0
      }))
      trendData.value = (d.trend || []).map((t: any) => ({
        date: t.date != null ? String(t.date) : '',
        count: Number(t.count) || 0
      }))
      abnormalDistribution.value = (d.abnormalDistribution || []).map((a: any) => ({
        name: a.name != null ? String(a.name) : '',
        value: Number(a.value) || 0
      }))
      timeDistribution.value = (d.timeDistribution || []).map((t: any) => ({
        name: t.name != null ? String(t.name) : '',
        value: Number(t.value) || 0
      }))
    })
    .catch(() => {})
}

const reasonChartOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, type: 'scroll' },
  series: [
    {
      type: 'pie',
      radius: ['40%', '65%'],
      data: reasonDistribution.value.map((r) => ({ value: r.value, name: r.name })),
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.2)' } }
    }
  ]
}))

const trendChartOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: trendData.value.map((t) => t.date)
  },
  yAxis: { type: 'value', minInterval: 1 },
  series: [
    {
      name: '访客数',
      type: 'line',
      data: trendData.value.map((t) => t.count),
      smooth: true,
      areaStyle: { opacity: 0.2 }
    }
  ]
}))

const abnormalChartOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: abnormalDistribution.value.map((a) => a.name)
  },
  yAxis: { type: 'value', minInterval: 1 },
  series: [
    {
      type: 'bar',
      data: abnormalDistribution.value.map((a) => a.value),
      itemStyle: { borderRadius: [4, 4, 0, 0] }
    }
  ]
}))

const timeChartOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: timeDistribution.value.map((t) => t.name)
  },
  yAxis: { type: 'value', minInterval: 1 },
  series: [
    {
      type: 'bar',
      data: timeDistribution.value.map((t) => t.value),
      itemStyle: { borderRadius: [4, 4, 0, 0] }
    }
  ]
}))

onMounted(() => {
  NewVisitorManagementApi.getStats()
    .then((res: any) => {
      const d = res?.data
      if (d) {
        stats.value = {
          pendingApproval: d.pendingApproval ?? d.pending_approval ?? 0,
          currentVisitors: d.currentVisitors ?? d.current_visitors ?? 0,
          todayAppointments: d.todayAppointments ?? d.today_appointments ?? 0,
          monthlyTotal: d.monthlyTotal ?? d.monthly_total ?? 0
        }
      }
    })
    .catch(() => {})
  loadDashboard()
})

// 日期筛选变化时重新拉取仪表盘
watch([dateRange, dateScope], () => {
  loadDashboard()
})
</script>

<style lang="scss" scoped>
.visitor-home {
  padding: 20px;
  background: var(--el-bg-color-page);
}

.page-header {
  margin-bottom: 20px;
  .page-title {
    font-size: 18px;
    font-weight: 600;
    margin: 0 0 4px;
    color: var(--el-text-color-primary);
  }
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  :deep(.el-card__body) {
    padding: 20px;
  }
  &__head {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;
  }
  &__label {
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
  &__icon {
    font-size: 20px;
    &--primary { color: var(--el-color-primary); }
    &--info { color: var(--el-color-info); }
    &--success { color: var(--el-color-success); }
    &--warning { color: var(--el-color-warning); }
  }
  &__value {
    font-size: 28px;
    font-weight: 700;
    color: var(--el-text-color-primary);
    margin-bottom: 4px;
  }
  &__tip {
    font-size: 12px;
    color: var(--el-text-color-placeholder);
    margin: 0;
  }
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
  :deep(.el-card__body) {
    padding: 16px;
  }
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.filter-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-regular);
  margin-right: 8px;
}

.filter-btns {
  display: flex;
  gap: 8px;
}

.filter-date {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
  .date-picker {
    width: 240px;
  }
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-card {
  border-radius: 12px;
  :deep(.el-card__body) {
    padding: 20px;
  }
  &--full {
    grid-column: span 2;
  }
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 4px;
  color: var(--el-text-color-primary);
}

.chart-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin: 0 0 16px;
}

.chart-wrap {
  height: 260px;
}

@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
  .charts-grid {
    grid-template-columns: 1fr;
  }
  .chart-card--full {
    grid-column: span 1;
  }
}
</style>
