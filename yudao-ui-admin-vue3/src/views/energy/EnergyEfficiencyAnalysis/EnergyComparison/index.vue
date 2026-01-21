<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧能源</el-breadcrumb-item>
        <el-breadcrumb-item>能效分析</el-breadcrumb-item>
        <el-breadcrumb-item>能耗对比</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 对比配置 -->
    <div class="comparison-config">
      <el-card class="config-card">
        <template #header>
          <span>对比配置</span>
        </template>
        <el-form :model="comparisonForm" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="对比类型">
                <el-select v-model="comparisonForm.comparisonType" @change="handleComparisonTypeChange">
                  <el-option label="时间对比" value="time" />
                  <el-option label="区域对比" value="area" />
                  <el-option label="设备对比" value="device" />
                  <el-option label="能源对比" value="energy" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="能源类型">
                <el-select v-model="comparisonForm.energyType">
                  <el-option label="电" value="electricity" />
                  <el-option label="水" value="water" />
                  <el-option label="燃气" value="gas" />
                  <el-option label="蒸汽" value="steam" />
                  <el-option label="综合" value="total" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="对比维度">
                <el-select v-model="comparisonForm.dimension">
                  <el-option label="消耗量" value="consumption" />
                  <el-option label="费用" value="cost" />
                  <el-option label="单位面积能耗" value="unitArea" />
                  <el-option label="碳排放" value="carbon" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 时间对比配置 -->
          <div v-if="comparisonForm.comparisonType === 'time'" class="time-comparison-config">
            <el-form-item label="基准时间">
              <el-date-picker
                v-model="comparisonForm.baseTime"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm"
              />
            </el-form-item>
            <el-form-item label="对比时间">
              <div class="multiple-time-ranges">
                <div v-for="(timeRange, index) in comparisonForm.compareTimeRanges" :key="index" class="time-range-item">
                  <el-date-picker
                    v-model="timeRange.range"
                    type="datetimerange"
                    range-separator="至"
                    start-placeholder="开始时间"
                    end-placeholder="结束时间"
                    format="YYYY-MM-DD HH:mm"
                    value-format="YYYY-MM-DD HH:mm"
                  />
                  <el-input v-model="timeRange.label" placeholder="时间段名称" style="width: 120px; margin-left: 12px;" />
                  <el-button type="danger" link @click="removeTimeRange(index)" style="margin-left: 12px;">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
                <el-button type="primary" @click="addTimeRange" style="margin-top: 12px;">
                  <el-icon><Plus /></el-icon>
                  添加时间段
                </el-button>
              </div>
            </el-form-item>
          </div>

          <!-- 区域对比配置 -->
          <div v-if="comparisonForm.comparisonType === 'area'" class="area-comparison-config">
            <el-form-item label="对比区域">
              <el-transfer
                v-model="comparisonForm.selectedAreas"
                :data="areaOptions"
                :titles="['可选区域', '对比区域']"
                filterable
                filter-placeholder="搜索区域"
              />
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="comparisonForm.timeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm"
              />
            </el-form-item>
          </div>

          <!-- 设备对比配置 -->
          <div v-if="comparisonForm.comparisonType === 'device'" class="device-comparison-config">
            <el-form-item label="对比设备">
              <el-transfer
                v-model="comparisonForm.selectedDevices"
                :data="deviceOptions"
                :titles="['可选设备', '对比设备']"
                filterable
                filter-placeholder="搜索设备"
              />
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="comparisonForm.timeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm"
              />
            </el-form-item>
          </div>

          <!-- 能源对比配置 -->
          <div v-if="comparisonForm.comparisonType === 'energy'" class="energy-comparison-config">
            <el-form-item label="对比范围">
              <el-select v-model="comparisonForm.scope">
                <el-option label="全部区域" value="all" />
                <el-option label="1号楼" value="building_1" />
                <el-option label="2号楼" value="building_2" />
                <el-option label="3号楼" value="building_3" />
              </el-select>
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="comparisonForm.timeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm"
              />
            </el-form-item>
          </div>

          <el-form-item>
            <el-button type="primary" @click="handleGenerateComparison">
              <el-icon><DataAnalysis /></el-icon>
              生成对比
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
            <el-button type="success" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出报告
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 对比结果 -->
    <div v-if="showResults" class="comparison-results">
      <!-- 对比图表 -->
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card class="chart-card">
            <template #header>
              <div class="chart-header">
                <span>{{ getChartTitle() }}</span>
                <div class="chart-controls">
                  <el-radio-group v-model="chartType" size="small">
                    <el-radio-button label="bar">柱状图</el-radio-button>
                    <el-radio-button label="line">折线图</el-radio-button>
                    <el-radio-button label="radar">雷达图</el-radio-button>
                  </el-radio-group>
                </div>
              </div>
            </template>
            <div class="chart-container" style="height: 400px;">
              <div class="chart-placeholder">
                <div class="chart-content">
                  <h3>{{ getChartTitle() }}</h3>
                  <div class="chart-legend">
                    <div v-for="item in chartLegend" :key="item.key" class="legend-item">
                      <div :class="['legend-color', item.key]"></div>
                      <span>{{ item.label }}</span>
                    </div>
                  </div>
                  <div class="chart-mock">
                    <p>{{ chartType === 'bar' ? '柱状' : chartType === 'line' ? '折线' : '雷达' }}图对比展示</p>
                    <p>显示{{ getDimensionText() }}对比数据</p>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card class="summary-card">
            <template #header>
              <span>对比摘要</span>
            </template>
            <div class="summary-content">
              <div class="summary-item">
                <div class="summary-label">最高值</div>
                <div class="summary-value">{{ formatNumber(summaryData.max.value) }}</div>
                <div class="summary-target">{{ summaryData.max.target }}</div>
              </div>
              <div class="summary-item">
                <div class="summary-label">最低值</div>
                <div class="summary-value">{{ formatNumber(summaryData.min.value) }}</div>
                <div class="summary-target">{{ summaryData.min.target }}</div>
              </div>
              <div class="summary-item">
                <div class="summary-label">平均值</div>
                <div class="summary-value">{{ formatNumber(summaryData.average) }}</div>
                <div class="summary-unit">{{ getDimensionUnit() }}</div>
              </div>
              <div class="summary-item">
                <div class="summary-label">差异率</div>
                <div class="summary-value" :class="summaryData.difference >= 0 ? 'positive' : 'negative'">
                  {{ summaryData.difference >= 0 ? '+' : '' }}{{ summaryData.difference }}%
                </div>
                <div class="summary-desc">最高与最低</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 详细数据表格 -->
      <el-card class="data-table-card" style="margin-top: 20px;">
        <template #header>
          <div class="table-header">
            <span>详细对比数据</span>
            <div class="table-controls">
              <el-button size="small" @click="handleSortByValue">
                <el-icon><Sort /></el-icon>
                按值排序
              </el-button>
              <el-button size="small" @click="handleExportTable">
                <el-icon><Download /></el-icon>
                导出表格
              </el-button>
            </div>
          </div>
        </template>
        
        <el-table :data="comparisonData" stripe border style="width: 100%">
          <el-table-column prop="target" label="对比对象" width="150" />
          <el-table-column prop="value" label="数值" width="120">
            <template #default="{ row }">
              {{ formatNumber(row.value) }} {{ getDimensionUnit() }}
            </template>
          </el-table-column>
          <el-table-column prop="percentage" label="占比" width="100">
            <template #default="{ row }">
              {{ row.percentage }}%
            </template>
          </el-table-column>
          <el-table-column prop="change" label="变化" width="120">
            <template #default="{ row }">
              <span :class="row.change >= 0 ? 'positive' : 'negative'">
                {{ row.change >= 0 ? '+' : '' }}{{ row.change }}%
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="rank" label="排名" width="80">
            <template #default="{ row }">
              <el-tag :type="getRankColor(row.rank)" size="small">
                第{{ row.rank }}名
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="efficiency" label="能效评级" width="120">
            <template #default="{ row }">
              <el-rate
                v-model="row.efficiency"
                :max="5"
                size="small"
                disabled
                show-score
                text-color="#ff9900"
              />
            </template>
          </el-table-column>
          <el-table-column prop="cost" label="费用" width="120">
            <template #default="{ row }">
              ¥{{ formatNumber(row.cost) }}
            </template>
          </el-table-column>
          <el-table-column prop="carbonEmission" label="碳排放(kg)" width="120" />
          <el-table-column prop="remarks" label="备注" min-width="150" show-overflow-tooltip />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleViewDetail(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button link type="primary" size="small" @click="handleAnalyze(row)">
                <el-icon><DataAnalysis /></el-icon>
                分析
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 分析结论 -->
      <el-card class="conclusion-card" style="margin-top: 20px;">
        <template #header>
          <span>分析结论与建议</span>
        </template>
        <div class="conclusion-content">
          <div class="conclusion-section">
            <h4>关键发现</h4>
            <ul class="finding-list">
              <li v-for="finding in analysisFindings" :key="finding.id" class="finding-item">
                <el-icon :class="finding.type"><Warning /></el-icon>
                <span>{{ finding.content }}</span>
              </li>
            </ul>
          </div>
          
          <div class="conclusion-section">
            <h4>优化建议</h4>
            <div class="suggestions">
              <div v-for="suggestion in optimizationSuggestions" :key="suggestion.id" class="suggestion-item">
                <div class="suggestion-title">{{ suggestion.title }}</div>
                <div class="suggestion-content">{{ suggestion.content }}</div>
                <div class="suggestion-benefit">预期收益：{{ suggestion.benefit }}</div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ContentWrap } from '@/components/ContentWrap'
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Plus, Delete, DataAnalysis, Refresh, Download, Sort, View, Warning
} from '@element-plus/icons-vue'

// 响应式数据
const showResults = ref(false)
const chartType = ref('bar')

// 对比表单
const comparisonForm = reactive({
  comparisonType: 'time',
  energyType: 'electricity',
  dimension: 'consumption',
  baseTime: [],
  compareTimeRanges: [
    { range: [], label: '去年同期' }
  ],
  selectedAreas: [],
  selectedDevices: [],
  timeRange: [],
  scope: 'all'
})

// 选项数据
const areaOptions = ref([
  { key: 'building_1', label: '1号楼' },
  { key: 'building_2', label: '2号楼' },
  { key: 'building_3', label: '3号楼' },
  { key: 'parking', label: '地下车库' },
  { key: 'canteen', label: '食堂' }
])

const deviceOptions = ref([
  { key: 'meter_001', label: '主配电柜-MC001' },
  { key: 'meter_002', label: '智能电表-EM001' },
  { key: 'meter_003', label: '水表-WM001' },
  { key: 'meter_004', label: '燃气表-GM001' },
  { key: 'meter_005', label: '蒸汽表-SM001' }
])

// 图表图例
const chartLegend = computed(() => {
  if (comparisonForm.comparisonType === 'time') {
    return [
      { key: 'base', label: '基准时间' },
      ...comparisonForm.compareTimeRanges.map((item, index) => ({
        key: `compare_${index}`,
        label: item.label || `对比${index + 1}`
      }))
    ]
  } else if (comparisonForm.comparisonType === 'area') {
    return comparisonForm.selectedAreas.map(areaKey => {
      const area = areaOptions.value.find(item => item.key === areaKey)
      return { key: areaKey, label: area?.label || areaKey }
    })
  } else if (comparisonForm.comparisonType === 'device') {
    return comparisonForm.selectedDevices.map(deviceKey => {
      const device = deviceOptions.value.find(item => item.key === deviceKey)
      return { key: deviceKey, label: device?.label || deviceKey }
    })
  } else {
    return [
      { key: 'electricity', label: '电' },
      { key: 'water', label: '水' },
      { key: 'gas', label: '燃气' },
      { key: 'steam', label: '蒸汽' }
    ]
  }
})

// 摘要数据
const summaryData = reactive({
  max: { value: 45680, target: '1号楼' },
  min: { value: 8960, target: '食堂' },
  average: 28420,
  difference: 409.7
})

// 对比数据
const comparisonData = ref([
  {
    target: '1号楼',
    value: 45680,
    percentage: 35.8,
    change: 8.5,
    rank: 1,
    efficiency: 4,
    cost: 38950,
    carbonEmission: 25680,
    remarks: '用电量最高，建议优化空调系统'
  },
  {
    target: '2号楼',
    value: 38920,
    percentage: 30.5,
    change: -2.3,
    rank: 2,
    efficiency: 4,
    cost: 33180,
    carbonEmission: 21850,
    remarks: '能耗控制良好'
  },
  {
    target: '3号楼',
    value: 28150,
    percentage: 22.1,
    change: 1.8,
    rank: 3,
    efficiency: 5,
    cost: 24000,
    carbonEmission: 15800,
    remarks: '节能效果显著'
  },
  {
    target: '地下车库',
    value: 13230,
    percentage: 10.4,
    change: -5.2,
    rank: 4,
    efficiency: 3,
    cost: 11280,
    carbonEmission: 7420,
    remarks: '照明系统有待优化'
  },
  {
    target: '食堂',
    value: 8960,
    percentage: 7.0,
    change: 12.6,
    rank: 5,
    efficiency: 2,
    cost: 7630,
    carbonEmission: 5030,
    remarks: '燃气用量增加较多'
  }
])

// 分析发现
const analysisFindings = ref([
  {
    id: 1,
    type: 'warning',
    content: '1号楼能耗同比增长8.5%，超出预期范围'
  },
  {
    id: 2,
    type: 'success',
    content: '3号楼节能改造效果显著，能效提升明显'
  },
  {
    id: 3,
    type: 'info',
    content: '食堂燃气用量增长12.6%，需关注设备运行状况'
  }
])

// 优化建议
const optimizationSuggestions = ref([
  {
    id: 1,
    title: '1号楼空调系统优化',
    content: '建议升级空调控制系统，采用变频技术，根据实际需求调节制冷/制热功率',
    benefit: '预计节能15-20%，年节约费用约8万元'
  },
  {
    id: 2,
    title: '地下车库照明改造',
    content: '将传统照明更换为LED灯具，并安装感应控制系统',
    benefit: '预计节能30-40%，年节约费用约2万元'
  },
  {
    id: 3,
    title: '食堂设备维护',
    content: '定期检查燃气设备运行状况，及时维护保养，避免能源浪费',
    benefit: '预计节能10-15%，年节约费用约1.5万元'
  }
])

// 计算属性和方法
const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toLocaleString()
}

const getChartTitle = () => {
  const typeTexts = {
    time: '时间对比',
    area: '区域对比',
    device: '设备对比',
    energy: '能源对比'
  }
  const energyText = getEnergyTypeText()
  const dimensionText = getDimensionText()
  return `${typeTexts[comparisonForm.comparisonType]} - ${energyText}${dimensionText}`
}

const getEnergyTypeText = () => {
  const texts = {
    electricity: '用电量',
    water: '用水量',
    gas: '燃气量',
    steam: '蒸汽量',
    total: '综合能耗'
  }
  return texts[comparisonForm.energyType] || '能耗'
}

const getDimensionText = () => {
  const texts = {
    consumption: '消耗量',
    cost: '费用',
    unitArea: '单位面积能耗',
    carbon: '碳排放'
  }
  return texts[comparisonForm.dimension] || '消耗量'
}

const getDimensionUnit = () => {
  if (comparisonForm.dimension === 'cost') return '元'
  if (comparisonForm.dimension === 'unitArea') return 'kWh/m²'
  if (comparisonForm.dimension === 'carbon') return 'kg'
  
  if (comparisonForm.energyType === 'electricity') return 'kWh'
  if (comparisonForm.energyType === 'water') return 'm³'
  if (comparisonForm.energyType === 'gas') return 'm³'
  if (comparisonForm.energyType === 'steam') return 't'
  return 'kWh'
}

const getRankColor = (rank: number) => {
  if (rank === 1) return 'danger'
  if (rank === 2) return 'warning'
  if (rank === 3) return 'primary'
  return 'info'
}

// 事件处理
const handleComparisonTypeChange = () => {
  // 重置相关配置
  comparisonForm.selectedAreas = []
  comparisonForm.selectedDevices = []
  comparisonForm.timeRange = []
  showResults.value = false
}

const addTimeRange = () => {
  comparisonForm.compareTimeRanges.push({
    range: [],
    label: `对比${comparisonForm.compareTimeRanges.length + 1}`
  })
}

const removeTimeRange = (index: number) => {
  comparisonForm.compareTimeRanges.splice(index, 1)
}

const handleGenerateComparison = () => {
  // 验证配置
  if (comparisonForm.comparisonType === 'time') {
    if (!comparisonForm.baseTime.length) {
      ElMessage.warning('请选择基准时间')
      return
    }
    if (comparisonForm.compareTimeRanges.some(item => !item.range.length)) {
      ElMessage.warning('请完善对比时间配置')
      return
    }
  } else if (comparisonForm.comparisonType === 'area') {
    if (comparisonForm.selectedAreas.length < 2) {
      ElMessage.warning('请至少选择2个区域进行对比')
      return
    }
  } else if (comparisonForm.comparisonType === 'device') {
    if (comparisonForm.selectedDevices.length < 2) {
      ElMessage.warning('请至少选择2个设备进行对比')
      return
    }
  }

  // 生成对比结果
  showResults.value = true
  ElMessage.success('对比分析完成')
}

const handleReset = () => {
  Object.assign(comparisonForm, {
    comparisonType: 'time',
    energyType: 'electricity',
    dimension: 'consumption',
    baseTime: [],
    compareTimeRanges: [
      { range: [], label: '去年同期' }
    ],
    selectedAreas: [],
    selectedDevices: [],
    timeRange: [],
    scope: 'all'
  })
  showResults.value = false
}

const handleExport = () => {
  ElMessage.success('导出报告功能开发中...')
}

const handleSortByValue = () => {
  comparisonData.value.sort((a, b) => b.value - a.value)
  ElMessage.success('已按数值排序')
}

const handleExportTable = () => {
  ElMessage.success('导出表格功能开发中...')
}

const handleViewDetail = (row: any) => {
  ElMessage.info(`查看详情: ${row.target}`)
}

const handleAnalyze = (row: any) => {
  ElMessage.info(`深度分析: ${row.target}`)
}
</script>

<style scoped lang="scss">@use '@/styles/dark-theme.scss';

.app-container {
  padding: 20px;

  .breadcrumb-container {
    margin-bottom: 20px;
  }

  .comparison-config {
    margin-bottom: 20px;

    .config-card {
      .time-comparison-config,
      .area-comparison-config,
      .device-comparison-config,
      .energy-comparison-config {
        margin-top: 20px;

        .multiple-time-ranges {
          .time-range-item {
            display: flex;
            align-items: center;
            margin-bottom: 12px;
          }
        }
      }
    }
  }

  .comparison-results {
    .chart-card {
      .chart-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .chart-controls {
          display: flex;
          align-items: center;
        }
      }

      .chart-container {
        .chart-placeholder {
          display: flex;
          align-items: center;
          justify-content: center;
          height: 100%;
          background: #f8f9fa;
          border-radius: 8px;

          .chart-content {
            text-align: center;

            h3 {
              margin: 0 0 16px 0;
              color: #303133;
            }

            .chart-legend {
              display: flex;
              justify-content: center;
              gap: 20px;
              margin-bottom: 20px;

              .legend-item {
                display: flex;
                align-items: center;
                gap: 6px;

                .legend-color {
                  width: 12px;
                  height: 12px;
                  border-radius: 2px;

                  &.base {
                    background: #409eff;
                  }

                  &.compare_0 {
                    background: #67c23a;
                  }

                  &.compare_1 {
                    background: #e6a23c;
                  }

                  &.building_1 {
                    background: #f56c6c;
                  }

                  &.building_2 {
                    background: #909399;
                  }

                  &.building_3 {
                    background: #409eff;
                  }

                  &.electricity {
                    background: #409eff;
                  }

                  &.water {
                    background: #67c23a;
                  }

                  &.gas {
                    background: #e6a23c;
                  }
                }
              }
            }

            .chart-mock {
              color: #909399;

              p {
                margin: 8px 0;
                font-size: 14px;
              }
            }
          }
        }
      }
    }

    .summary-card {
      .summary-content {
        .summary-item {
          margin-bottom: 24px;

          &:last-child {
            margin-bottom: 0;
          }

          .summary-label {
            font-size: 14px;
            color: #909399;
            margin-bottom: 8px;
          }

          .summary-value {
            font-size: 20px;
            font-weight: bold;
            color: #303133;
            margin-bottom: 4px;

            &.positive {
              color: #f56c6c;
            }

            &.negative {
              color: #67c23a;
            }
          }

          .summary-target,
          .summary-unit,
          .summary-desc {
            font-size: 12px;
            color: #606266;
          }
        }
      }
    }

    .data-table-card {
      .table-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .table-controls {
          display: flex;
          gap: 8px;
        }
      }

      .positive {
        color: #f56c6c;
      }

      .negative {
        color: #67c23a;
      }
    }

    .conclusion-card {
      .conclusion-content {
        .conclusion-section {
          margin-bottom: 32px;

          &:last-child {
            margin-bottom: 0;
          }

          h4 {
            margin: 0 0 16px 0;
            color: #303133;
            font-size: 16px;
            font-weight: 600;
            border-left: 3px solid #409eff;
            padding-left: 12px;
          }

          .finding-list {
            list-style: none;
            padding: 0;
            margin: 0;

            .finding-item {
              display: flex;
              align-items: center;
              gap: 8px;
              padding: 12px;
              margin-bottom: 8px;
              background: #f8f9fa;
              border-radius: 6px;

              .el-icon {
                &.warning {
                  color: #e6a23c;
                }

                &.success {
                  color: #67c23a;
                }

                &.info {
                  color: #409eff;
                }
              }
            }
          }

          .suggestions {
            .suggestion-item {
              padding: 16px;
              margin-bottom: 16px;
              background: #f8f9fa;
              border-radius: 8px;
              border-left: 4px solid #409eff;

              &:last-child {
                margin-bottom: 0;
              }

              .suggestion-title {
                font-size: 16px;
                font-weight: 600;
                color: #303133;
                margin-bottom: 8px;
              }

              .suggestion-content {
                font-size: 14px;
                color: #606266;
                margin-bottom: 8px;
                line-height: 1.6;
              }

              .suggestion-benefit {
                font-size: 12px;
                color: #67c23a;
                font-weight: 500;
              }
            }
          }
        }
      }
    }
  }
}
</style>






