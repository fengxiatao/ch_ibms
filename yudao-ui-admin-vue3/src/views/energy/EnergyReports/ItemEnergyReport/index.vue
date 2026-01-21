<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="item-energy-report-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item>智慧能源</el-breadcrumb-item>
        <el-breadcrumb-item>能耗报表</el-breadcrumb-item>
        <el-breadcrumb-item>分项能耗报表</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 能耗类型标签页 -->
    <el-card class="main-card" shadow="never">
      <el-tabs v-model="activeTab" type="card" @tab-change="handleTabChange">
        <!-- 电力报表 -->
        <el-tab-pane label="电力报表" name="electricity">
          <template #label>
            <div class="tab-label">
              <el-icon><Lightning /></el-icon>
              <span>电力报表</span>
            </div>
          </template>
          <EnergyReportContent 
            :report-type="'electricity'"
            :data="electricityData"
            :loading="loading"
            @refresh="handleRefresh"
            @export="handleExport"
          />
        </el-tab-pane>

        <!-- 水量报表 -->
        <el-tab-pane label="水量报表" name="water">
          <template #label>
            <div class="tab-label">
              <el-icon><MagicStick /></el-icon>
              <span>水量报表</span>
            </div>
          </template>
          <EnergyReportContent 
            :report-type="'water'"
            :data="waterData"
            :loading="loading"
            @refresh="handleRefresh"
            @export="handleExport"
          />
        </el-tab-pane>

        <!-- 燃气报表 -->
        <el-tab-pane label="燃气报表" name="gas">
          <template #label>
            <div class="tab-label">
              <el-icon><Sunny /></el-icon>
              <span>燃气报表</span>
            </div>
          </template>
          <EnergyReportContent 
            :report-type="'gas'"
            :data="gasData"
            :loading="loading"
            @refresh="handleRefresh"
            @export="handleExport"
          />
        </el-tab-pane>

        <!-- 热量报表 -->
        <el-tab-pane label="热量报表" name="heat">
          <template #label>
            <div class="tab-label">
              <el-icon><Sunny /></el-icon>
              <span>热量报表</span>
            </div>
          </template>
          <EnergyReportContent 
            :report-type="'heat'"
            :data="heatData"
            :loading="loading"
            @refresh="handleRefresh"
            @export="handleExport"
          />
        </el-tab-pane>

        <!-- 冷量报表 -->
        <el-tab-pane label="冷量报表" name="cooling">
          <template #label>
            <div class="tab-label">
              <el-icon><Refrigerator /></el-icon>
              <span>冷量报表</span>
            </div>
          </template>
          <EnergyReportContent 
            :report-type="'cooling'"
            :data="coolingData"
            :loading="loading"
            @refresh="handleRefresh"
            @export="handleExport"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ContentWrap } from '@/components/ContentWrap'
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Lightning, MagicStick, Sunny, Refrigerator } from '@element-plus/icons-vue'
import EnergyReportContent from './components/EnergyReportContent.vue'

// 响应式数据
const activeTab = ref('electricity')
const loading = ref(false)

// 各类型能耗数据
const electricityData = reactive({
  summary: {
    totalConsumption: 85423.5,
    unit: 'kWh',
    cost: 68338.8,
    currency: '元',
    avgDaily: 2847.45,
    peak: 3124.8,
    peakTime: '14:30'
  },
  categories: [
    { name: '照明插座用电', consumption: 25624.3, percentage: 30.0, cost: 20499.4 },
    { name: '空调用电', consumption: 34169.4, percentage: 40.0, cost: 27335.5 },
    { name: '动力用电', consumption: 17084.7, percentage: 20.0, cost: 13667.8 },
    { name: '特殊用电', consumption: 8545.1, percentage: 10.0, cost: 6836.1 }
  ],
  trends: {
    labels: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月'],
    data: [78543, 72156, 85423, 89765, 92341, 88567, 91234, 87654, 85423],
    targets: [85000, 85000, 85000, 85000, 85000, 85000, 85000, 85000, 85000]
  }
})

const waterData = reactive({
  summary: {
    totalConsumption: 12543.2,
    unit: 'm³',
    cost: 37629.6,
    currency: '元',
    avgDaily: 418.1,
    peak: 456.7,
    peakTime: '10:30'
  },
  categories: [
    { name: '生活用水', consumption: 7525.9, percentage: 60.0, cost: 22577.7 },
    { name: '景观用水', consumption: 2508.6, percentage: 20.0, cost: 7525.9 },
    { name: '消防用水', consumption: 1254.3, percentage: 10.0, cost: 3762.9 },
    { name: '其他用水', consumption: 1254.4, percentage: 10.0, cost: 3763.1 }
  ],
  trends: {
    labels: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月'],
    data: [11234, 10567, 12543, 13245, 14567, 15234, 14876, 13654, 12543],
    targets: [13000, 13000, 13000, 13000, 13000, 13000, 13000, 13000, 13000]
  }
})

const gasData = reactive({
  summary: {
    totalConsumption: 8765.4,
    unit: 'm³',
    cost: 26296.2,
    currency: '元',
    avgDaily: 292.2,
    peak: 324.6,
    peakTime: '07:30'
  },
  categories: [
    { name: '采暖用气', consumption: 5259.2, percentage: 60.0, cost: 15777.7 },
    { name: '热水用气', consumption: 2629.6, percentage: 30.0, cost: 7888.9 },
    { name: '厨房用气', consumption: 876.6, percentage: 10.0, cost: 2629.6 }
  ],
  trends: {
    labels: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月'],
    data: [9234, 8567, 8765, 7543, 6234, 5876, 6543, 7234, 8765],
    targets: [9000, 9000, 9000, 9000, 9000, 9000, 9000, 9000, 9000]
  }
})

const heatData = reactive({
  summary: {
    totalConsumption: 15634.7,
    unit: 'GJ',
    cost: 46904.1,
    currency: '元',
    avgDaily: 521.2,
    peak: 578.9,
    peakTime: '08:00'
  },
  categories: [
    { name: '供暖热量', consumption: 12507.8, percentage: 80.0, cost: 37523.3 },
    { name: '生活热水', consumption: 3126.9, percentage: 20.0, cost: 9380.8 }
  ],
  trends: {
    labels: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月'],
    data: [18234, 16567, 15635, 12543, 8234, 5876, 6543, 9234, 15635],
    targets: [16000, 16000, 16000, 16000, 16000, 16000, 16000, 16000, 16000]
  }
})

const coolingData = reactive({
  summary: {
    totalConsumption: 23456.8,
    unit: 'GJ',
    cost: 70370.4,
    currency: '元',
    avgDaily: 781.9,
    peak: 867.2,
    peakTime: '14:00'
  },
  categories: [
    { name: '中央空调制冷', consumption: 18765.4, percentage: 80.0, cost: 56296.3 },
    { name: '分体空调制冷', consumption: 4691.4, percentage: 20.0, cost: 14074.1 }
  ],
  trends: {
    labels: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月'],
    data: [12345, 15234, 18765, 21234, 24567, 26789, 25432, 24567, 23457],
    targets: [24000, 24000, 24000, 24000, 24000, 24000, 24000, 24000, 24000]
  }
})

// 当前数据
const currentData = computed(() => {
  switch (activeTab.value) {
    case 'electricity': return electricityData
    case 'water': return waterData
    case 'gas': return gasData
    case 'heat': return heatData
    case 'cooling': return coolingData
    default: return electricityData
  }
})

// 方法
const handleTabChange = (tabName: string) => {
  activeTab.value = tabName
  loadData()
}

const handleRefresh = () => {
  loadData()
}

const handleExport = (format: string) => {
  const typeNames = {
    electricity: '电力',
    water: '水量',
    gas: '燃气',
    heat: '热量',
    cooling: '冷量'
  }
  ElMessage.success(`正在导出${typeNames[activeTab.value]}报表（${format}格式）...`)
}

const loadData = () => {
  loading.value = true
  // 模拟API调用
  setTimeout(() => {
    ElMessage.success('数据加载完成')
    loading.value = false
  }, 1000)
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>@use '@/styles/dark-theme.scss';

.item-energy-report-container {
  background: #0a1628 !important;
  min-height: 100vh;
  padding: 20px;

  .breadcrumb-container {
    margin-bottom: 20px;
    
    :deep(.el-breadcrumb__inner) {
      color: #e5eaf3 !important;
    }
    
    :deep(.el-breadcrumb__separator) {
      color: #6b7485 !important;
    }
  }

  .main-card {
    background: #1e293b !important;
    border: 1px solid #334155 !important;

    :deep(.el-card__body) {
      background: #1e293b !important;
      padding: 0;
    }

    :deep(.el-tabs) {
      .el-tabs__header {
        background: #1e293b !important;
        margin: 0;
        
        .el-tabs__nav-wrap {
          background: #1e293b !important;
          
          .el-tabs__nav {
            background: #1e293b !important;
            border: none;
            
            .el-tabs__item {
              background: #334155 !important;
              border: 1px solid #475569 !important;
              color: #94a3b8 !important;
              margin-right: 2px;
              border-radius: 6px 6px 0 0;
              
              &.is-active {
                background: #1e293b !important;
                color: #3b82f6 !important;
                border-bottom-color: #1e293b !important;
              }
              
              &:hover {
                color: #3b82f6 !important;
              }
            }
          }
        }
      }
      
      .el-tabs__content {
        background: #1e293b !important;
        padding: 20px;
      }
    }
  }

  .tab-label {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .el-icon {
      font-size: 16px;
    }
  }
}
</style>
