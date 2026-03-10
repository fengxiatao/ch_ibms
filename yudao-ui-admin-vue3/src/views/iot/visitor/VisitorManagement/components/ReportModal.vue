<template>
  <el-dialog
    v-model="dialogVisible"
    title=""
    width="900px"
    :close-on-click-modal="false"
    class="report-modal"
  >
    <template #header>
      <div class="dialog-header">
        <div class="dialog-header__icon">
          <Icon icon="ep:data-analysis" />
        </div>
        <div>
          <h3>访客报表中心</h3>
          <p class="dialog-header__desc">生成和导出访客数据报表</p>
        </div>
      </div>
    </template>

    <div class="report-content">
      <!-- 快捷报表 -->
      <div class="quick-reports">
        <h4 class="section-title">快捷报表</h4>
        <div class="report-cards">
          <div 
            v-for="report in quickReports" 
            :key="report.id"
            class="report-card"
            :class="{ 'report-card--active': selectedReport === report.id }"
            @click="selectReport(report.id)"
          >
            <div class="report-card__icon" :style="{ background: report.bgColor }">
              <Icon :icon="report.icon" />
            </div>
            <div class="report-card__info">
              <h5>{{ report.name }}</h5>
              <p>{{ report.desc }}</p>
            </div>
            <el-radio :value="report.id" :model-value="selectedReport" />
          </div>
        </div>
      </div>

      <!-- 自定义报表配置 -->
      <div class="custom-config">
        <h4 class="section-title">报表配置</h4>
        
        <el-form label-width="100px" class="config-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="时间范围">
                <el-date-picker
                  v-model="config.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="访客类型">
                <el-select v-model="config.visitorType" placeholder="全部类型" clearable style="width: 100%">
                  <el-option label="全部类型" value="" />
                  <el-option label="商务访客" value="business" />
                  <el-option label="VIP访客" value="vip" />
                  <el-option label="面试候选" value="interview" />
                  <el-option label="外协人员" value="contractor" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="部门筛选">
                <el-select v-model="config.department" placeholder="全部部门" clearable style="width: 100%">
                  <el-option label="全部部门" value="" />
                  <el-option label="技术部" value="tech" />
                  <el-option label="市场部" value="market" />
                  <el-option label="人力资源" value="hr" />
                  <el-option label="行政部" value="admin" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="导出格式">
                <el-radio-group v-model="config.format">
                  <el-radio-button value="xlsx">Excel</el-radio-button>
                  <el-radio-button value="pdf">PDF</el-radio-button>
                  <el-radio-button value="csv">CSV</el-radio-button>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="包含字段">
            <el-checkbox-group v-model="config.fields">
              <el-checkbox value="name">访客姓名</el-checkbox>
              <el-checkbox value="phone">联系电话</el-checkbox>
              <el-checkbox value="company">所属单位</el-checkbox>
              <el-checkbox value="host">被访人</el-checkbox>
              <el-checkbox value="visitTime">来访时间</el-checkbox>
              <el-checkbox value="leaveTime">离开时间</el-checkbox>
              <el-checkbox value="reason">来访事由</el-checkbox>
              <el-checkbox value="area">访问区域</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-form>
      </div>

      <!-- 可视化看板 -->
      <div class="report-dashboard">
        <h4 class="section-title">可视化看板</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-card class="dashboard-card" shadow="never">
              <div class="dashboard-card__header">
                <span class="dashboard-card__title">{{ pieCardTitle }}</span>
                <el-tag size="small" type="info">按当前筛选</el-tag>
              </div>
              <Echart :options="statusPieOption" height="240px" />
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="dashboard-card" shadow="never">
              <div class="dashboard-card__header">
                <span class="dashboard-card__title">{{ barCardTitle }}</span>
                <el-tag size="small" type="info">按当前筛选</el-tag>
              </div>
              <Echart :options="trendBarOption" height="240px" />
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 预览 -->
      <div class="report-preview">
        <h4 class="section-title">
          数据预览
          <el-tag size="small" type="info">预计 {{ previewCount }} 条数据</el-tag>
        </h4>
        <el-table :data="previewData" border size="small" max-height="200">
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="name" label="访客姓名" width="100" />
          <el-table-column prop="company" label="所属单位" />
          <el-table-column prop="host" label="被访人" width="100" />
          <el-table-column prop="visitTime" label="来访时间" width="160" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.statusType" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <p class="preview-tip">仅显示前5条数据预览</p>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePreview">
          <Icon icon="ep:view" class="mr-1" />预览报表
        </el-button>
        <el-button type="success" @click="handleExport" :loading="exporting">
          <Icon icon="ep:download" class="mr-1" />导出报表
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Icon } from '@iconify/vue'
import type { EChartsOption } from 'echarts'
import { Echart } from '@/components/Echart'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits(['update:visible'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const selectedReport = ref('daily')
const exporting = ref(false)
const previewCount = ref(156)

const quickReports = [
  {
    id: 'daily',
    name: '每日访客汇总',
    desc: '当日所有访客的详细信息',
    icon: 'ep:calendar',
    bgColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    id: 'weekly',
    name: '本周访客统计',
    desc: '按部门和类型的周汇总',
    icon: 'ep:data-line',
    bgColor: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    id: 'monthly',
    name: '月度访客报告',
    desc: '包含趋势分析和同比数据',
    icon: 'ep:trend-charts',
    bgColor: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    id: 'abnormal',
    name: '异常事件报告',
    desc: '超时、越权等异常记录',
    icon: 'ep:warning-filled',
    bgColor: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
  }
]

const config = reactive({
  dateRange: [],
  visitorType: '',
  department: '',
  format: 'xlsx',
  fields: ['name', 'phone', 'company', 'host', 'visitTime']
})

type PreviewRow = {
  name: string
  company: string
  host: string
  visitTime: string
  status: string
  statusType: string
  visitorType: string
  department: string
}

type PieDataItem = {
  name: string
  value: number
}

const previewData = ref<PreviewRow[]>([])
const pieData = ref<PieDataItem[]>([])
const barCategories = ref<string[]>([])
const barValues = ref<number[]>([])

const pieCardTitle = computed(() => {
  const map: Record<string, string> = {
    daily: '访客状态分布',
    weekly: '访客类型分布',
    monthly: '访客状态分布',
    abnormal: '异常风险等级分布'
  }
  return map[selectedReport.value] || '分布统计'
})

const barCardTitle = computed(() => {
  const map: Record<string, string> = {
    daily: '来访时段分布',
    weekly: '近7天来访趋势',
    monthly: '近12个日期趋势',
    abnormal: '异常类型统计'
  }
  return map[selectedReport.value] || '趋势统计'
})

const statusPieOption = computed<EChartsOption>(() => {
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: 0,
      left: 'center'
    },
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        label: { show: false },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 600
          }
        },
        labelLine: { show: false },
        data: pieData.value
      }
    ]
  }
})

const trendBarOption = computed<EChartsOption>(() => {
  return {
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: 16,
      right: 16,
      top: 20,
      bottom: 32,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: barCategories.value,
      axisTick: { alignWithLabel: true }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '访客数',
        type: 'bar',
        barWidth: 18,
        data: barValues.value,
        itemStyle: {
          color: '#409eff'
        }
      }
    ]
  }
})

const stringToSeed = (str: string) => {
  let hash = 2166136261
  for (let i = 0; i < str.length; i++) {
    hash ^= str.charCodeAt(i)
    hash = Math.imul(hash, 16777619)
  }
  return hash >>> 0
}

const createSeededRandom = (seed: number) => {
  let x = seed || 123456789
  return () => {
    x ^= x << 13
    x ^= x >>> 17
    x ^= x << 5
    return ((x >>> 0) % 1000) / 1000
  }
}

const pad2 = (n: number) => String(n).padStart(2, '0')

const formatDate = (date: Date) => {
  return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}`
}

const buildMockPreviewRows = (rand: () => number, count: number) => {
  const names = ['张三', '李四', '王五', '赵六', '孙七', '周八', '吴九', '郑十']
  const companies = ['北京科技公司', '上海贸易公司', '深圳电子公司', '广州物流公司', '杭州互联网公司', '苏州制造企业']
  const hosts = ['李经理', '王总监', '赵主管', '钱经理', '周总', '技术总监', '行政主管']
  const departments = [
    { value: 'tech' },
    { value: 'market' },
    { value: 'hr' },
    { value: 'admin' }
  ]
  const visitorTypes = [
    { value: 'business' },
    { value: 'vip' },
    { value: 'interview' },
    { value: 'contractor' }
  ]
  const statuses = [
    { label: '已签离', tagType: 'info' },
    { label: '在访中', tagType: 'success' },
    { label: '待到访', tagType: 'warning' }
  ]

  const now = new Date()
  const baseDate = new Date(now.getFullYear(), now.getMonth(), now.getDate())

  const rows: PreviewRow[] = []
  for (let i = 0; i < count; i++) {
    const dept = departments[Math.floor(rand() * departments.length)]
    const type = visitorTypes[Math.floor(rand() * visitorTypes.length)]
    const status = statuses[Math.floor(rand() * statuses.length)]
    const hour = 8 + Math.floor(rand() * 10)
    const minute = Math.floor(rand() * 6) * 10
    const dayOffset = Math.floor(rand() * 7)
    const visitDate = new Date(baseDate.getTime() - dayOffset * 24 * 60 * 60 * 1000)
    rows.push({
      name: names[Math.floor(rand() * names.length)],
      company: companies[Math.floor(rand() * companies.length)],
      host: hosts[Math.floor(rand() * hosts.length)],
      visitTime: `${formatDate(visitDate)} ${pad2(hour)}:${pad2(minute)}`,
      status: status.label,
      statusType: status.tagType,
      visitorType: type.value,
      department: dept.value
    })
  }
  return rows
}

const applyFilters = (rows: PreviewRow[]) => {
  return rows.filter((r) => {
    if (config.visitorType && r.visitorType !== config.visitorType) return false
    if (config.department && r.department !== config.department) return false
    return true
  })
}

const updateMockReport = () => {
  const seedStr = JSON.stringify({
    report: selectedReport.value,
    dateRange: config.dateRange,
    visitorType: config.visitorType,
    department: config.department
  })
  const rand = createSeededRandom(stringToSeed(seedStr))

  const totalCountBaseMap: Record<string, [number, number]> = {
    daily: [80, 220],
    weekly: [200, 600],
    monthly: [800, 2200],
    abnormal: [10, 60]
  }
  const [minCount, maxCount] = totalCountBaseMap[selectedReport.value] || [100, 300]
  previewCount.value = Math.floor(minCount + rand() * (maxCount - minCount))

  const rows = buildMockPreviewRows(rand, 120)
  const filteredRows = applyFilters(rows)
  previewData.value = filteredRows.slice(0, 5)

  if (selectedReport.value === 'abnormal') {
    pieData.value = [
      { name: '高风险', value: Math.floor(3 + rand() * 10) },
      { name: '中风险', value: Math.floor(3 + rand() * 12) },
      { name: '低风险', value: Math.floor(3 + rand() * 14) }
    ]

    const abnormalTypes = ['超时滞留', '非法闯入', '未按时到访']
    barCategories.value = abnormalTypes
    barValues.value = abnormalTypes.map(() => Math.floor(1 + rand() * 15))
    return
  }

  if (selectedReport.value === 'weekly') {
    pieData.value = [
      { name: '商务访客', value: Math.floor(10 + rand() * 60) },
      { name: 'VIP访客', value: Math.floor(6 + rand() * 30) },
      { name: '面试候选', value: Math.floor(4 + rand() * 20) },
      { name: '外协人员', value: Math.floor(8 + rand() * 40) }
    ]

    const days: string[] = []
    for (let i = 6; i >= 0; i--) {
      const d = new Date()
      d.setDate(d.getDate() - i)
      days.push(`${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`)
    }
    barCategories.value = days
    barValues.value = days.map(() => Math.floor(10 + rand() * 80))
    return
  }

  if (selectedReport.value === 'daily') {
    pieData.value = [
      { name: '已签离', value: Math.floor(8 + rand() * 40) },
      { name: '在访中', value: Math.floor(8 + rand() * 50) },
      { name: '待到访', value: Math.floor(5 + rand() * 30) }
    ]

    barCategories.value = ['08:00', '10:00', '12:00', '14:00', '16:00', '18:00']
    barValues.value = barCategories.value.map(() => Math.floor(3 + rand() * 30))
    return
  }

  pieData.value = [
    { name: '已签离', value: Math.floor(40 + rand() * 180) },
    { name: '在访中', value: Math.floor(30 + rand() * 160) },
    { name: '待到访', value: Math.floor(20 + rand() * 120) }
  ]

  const labels: string[] = []
  for (let i = 11; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i * 2)
    labels.push(`${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`)
  }
  barCategories.value = labels
  barValues.value = labels.map(() => Math.floor(40 + rand() * 260))
}

const selectReport = (id: string) => {
  selectedReport.value = id
  updateMockReport()
}

const handlePreview = () => {
  ElMessage.info('正在生成预览...')
}

const handleExport = async () => {
  exporting.value = true
  
  // 模拟导出
  await new Promise(resolve => setTimeout(resolve, 1500))
  
  exporting.value = false
  ElMessage.success('报表导出成功，正在下载...')
  dialogVisible.value = false
}

watch(
  () => [selectedReport.value, config.dateRange, config.visitorType, config.department],
  () => {
    updateMockReport()
  },
  { deep: true, immediate: true }
)
</script>

<style lang="scss" scoped>
.report-modal {
  :deep(.el-dialog__header) {
    padding: 0;
    margin: 0;
  }
  
  :deep(.el-dialog__body) {
    padding: 0 24px 20px;
  }
}

.dialog-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  
  &__icon {
    width: 44px;
    height: 44px;
    border-radius: 10px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 22px;
  }
  
  h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
  }
  
  &__desc {
    font-size: 13px;
    color: var(--el-text-color-secondary);
    margin: 4px 0 0;
  }
}

.report-content {
  padding-top: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0 0 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.quick-reports {
  margin-bottom: 24px;
}

.report-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.report-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border: 2px solid var(--el-border-color-lighter);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    border-color: var(--el-border-color);
  }
  
  &--active {
    border-color: var(--el-color-primary);
    background: rgba(var(--el-color-primary-rgb), 0.12);
  }
  
  &__icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 18px;
    flex-shrink: 0;
  }
  
  &__info {
    flex: 1;
    min-width: 0;
    
    h5 {
      font-size: 14px;
      font-weight: 500;
      margin: 0 0 4px;
    }
    
    p {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      margin: 0;
    }
  }
}

.custom-config {
  margin-bottom: 24px;
}

.config-form {
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  padding: 16px;
  border-radius: 12px;
}

.report-dashboard {
  margin-bottom: 24px;
}

.dashboard-card {
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter);
  
  :deep(.el-card__body) {
    padding: 12px 12px 8px;
  }
}

.dashboard-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.dashboard-card__title {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.report-preview {
  margin-bottom: 16px;
}

.preview-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  text-align: center;
  margin: 8px 0 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
