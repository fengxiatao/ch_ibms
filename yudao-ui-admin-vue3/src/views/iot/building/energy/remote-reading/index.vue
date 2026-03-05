<template>
  <div class="energy-remote-reading-page dark-theme-page">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card electric">
        <div class="stat-icon"><Icon icon="mdi:flash" /></div>
        <div class="stat-content">
          <div class="stat-label">本月累计用电</div>
          <div class="stat-value">{{ formatNumber(monthStats.electricity) }}<span class="stat-unit">kWh</span></div>
          <div class="stat-change up">同比 +12%</div>
        </div>
      </div>
      <div class="stat-card water">
        <div class="stat-icon"><Icon icon="mdi:water" /></div>
        <div class="stat-content">
          <div class="stat-label">本月累计用水</div>
          <div class="stat-value">{{ formatNumber(monthStats.water) }}<span class="stat-unit">m³</span></div>
          <div class="stat-change down">同比 -3%</div>
        </div>
      </div>
      <div class="stat-card gas">
        <div class="stat-icon"><Icon icon="mdi:fire" /></div>
        <div class="stat-content">
          <div class="stat-label">本月累计用气</div>
          <div class="stat-value">{{ formatNumber(monthStats.gas) }}<span class="stat-unit">m³</span></div>
          <div class="stat-change up">同比 +5%</div>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="filter-section">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="表计类型">
          <el-select v-model="queryParams.meterType" placeholder="全部表计" clearable class="!w-120px">
            <el-option label="全部表计" :value="undefined" />
            <el-option label="电表" :value="1" />
            <el-option label="水表" :value="2" />
            <el-option label="燃气表" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="月份">
          <el-date-picker
            v-model="queryParams.month"
            type="month"
            placeholder="选择月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
            class="!w-140px"
          />
        </el-form-item>
        <el-form-item label="搜索">
          <el-input
            v-model="queryParams.meterCode"
            placeholder="表计编号/名称"
            clearable
            @keyup.enter="handleQuery"
            class="!w-160px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
        </el-form-item>
        <el-form-item class="flex-grow text-right">
          <el-button type="success" @click="handleExport">
            <Icon icon="ep:download" class="mr-5px" /> 导出数据
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="list"
        stripe
        border
        class="dark-table"
      >
        <el-table-column label="抄表日期" prop="collectTime" width="120" align="center">
          <template #default="{ row }">{{ formatDateStr(row.collectTime) }}</template>
        </el-table-column>
        <el-table-column label="表计编号" prop="meterCode" width="140" />
        <el-table-column label="表计名称" prop="meterName" min-width="180" />
        <el-table-column label="类型" prop="meterType" width="90" align="center">
          <template #default="{ row }">
            <span class="energy-badge" :class="getEnergyBadgeClass(row.meterType)">
              {{ getMeterTypeName(row.meterType) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="所属区域" prop="areaName" width="120" />
        <el-table-column label="上期读数" prop="lastReading" width="110" align="right">
          <template #default="{ row }">{{ formatNumber(row.lastReading) }}</template>
        </el-table-column>
        <el-table-column label="本期读数" prop="reading" width="110" align="right">
          <template #default="{ row }">
            <span class="reading-value">{{ formatNumber(row.reading) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实际用量" prop="usage" width="120" align="right">
          <template #default="{ row }">
            <span class="usage-value">{{ formatUsage(row.usage, row.meterType) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="抄表方式" width="100" align="center">
          <template #default>
            <span class="read-method auto">远程抄表</span>
          </template>
        </el-table-column>
        <el-table-column label="数据状态" width="100" align="center">
          <template #default="{ row }">
            <span class="data-status" :class="getDataStatusClass(row)">
              {{ getDataStatusText(row) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-section">
        <span class="total-text">共 {{ total }} 条抄表记录</span>
        <el-pagination
          v-model:current-page="queryParams.pageNo"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="sizes, prev, pager, next, jumper"
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import * as EnergyApi from '@/api/iot/building/energy'
import type { IbmsEnergyRecordVO, IbmsEnergyOverviewVO } from '@/api/iot/building/energy'
import { useMessage } from '@/hooks/web/useMessage'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'BuildingEnergyRemoteReading' })

const message = useMessage()

// 数据
const loading = ref(false)
const list = ref<IbmsEnergyRecordVO[]>([])
const total = ref(0)
const monthStats = ref({
  electricity: 52400,
  water: 580,
  gas: 1420
})

// 查询参数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  meterCode: undefined as string | undefined,
  meterType: undefined as number | undefined,
  month: undefined as string | undefined
})

// 工具函数
const formatNumber = (value?: number) => {
  if (value === undefined || value === null) return '--'
  return value.toLocaleString('zh-CN', { maximumFractionDigits: 1 })
}

const formatUsage = (value?: number, type?: number) => {
  if (value === undefined || value === null) return '--'
  const unitMap: Record<number, string> = { 1: 'kWh', 2: 'm³', 3: 'm³' }
  return value.toLocaleString('zh-CN', { maximumFractionDigits: 2 }) + ' ' + (unitMap[type || 1] || '')
}

const formatDateStr = (date?: Date | string) => {
  if (!date) return '--'
  return formatDate(date, 'YYYY-MM-DD')
}

const getMeterTypeName = (type?: number) => {
  const map: Record<number, string> = { 1: '电表', 2: '水表', 3: '燃气表' }
  return map[type || 0] || '未知'
}

const getEnergyBadgeClass = (type?: number) => {
  const map: Record<number, string> = { 1: 'badge-electric', 2: 'badge-water', 3: 'badge-gas' }
  return map[type || 0] || ''
}

const getDataStatusClass = (row: IbmsEnergyRecordVO) => {
  // 根据用量异常判断
  if (row.usage && row.usage < 0) return 'abnormal'
  return 'normal'
}

const getDataStatusText = (row: IbmsEnergyRecordVO) => {
  if (row.usage && row.usage < 0) return '数据异常'
  return '正常'
}

// 加载数据
const getList = async () => {
  loading.value = true
  try {
    const params: any = {
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      meterCode: queryParams.meterCode
    }
    
    // 处理月份筛选
    if (queryParams.month) {
      const [year, month] = queryParams.month.split('-')
      const startDate = new Date(parseInt(year), parseInt(month) - 1, 1)
      const endDate = new Date(parseInt(year), parseInt(month), 0)
      params.startTime = startDate.toISOString()
      params.endTime = endDate.toISOString()
    }
    
    const res = await EnergyApi.getRecordPage(params)
    list.value = res.list
    total.value = res.total
  } catch (e) {
    console.error('加载抄表记录失败', e)
  } finally {
    loading.value = false
  }
}

const loadMonthStats = async () => {
  try {
    const overview = await EnergyApi.getOverview()
    monthStats.value = {
      electricity: overview.monthElectricity || 0,
      water: overview.monthWater || 0,
      gas: overview.monthGas || 0
    }
  } catch (e) {
    console.error('加载月度统计失败', e)
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.meterCode = undefined
  queryParams.meterType = undefined
  queryParams.month = undefined
  handleQuery()
}

const handleExport = () => {
  message.success('导出功能开发中...')
}

// 初始化
onMounted(() => {
  getList()
  loadMonthStats()
})
</script>

<style lang="scss" scoped>
@use '@/styles/dark-theme.scss' as *;

.energy-remote-reading-page {
  padding: 20px;
  padding-top: calc(
    20px + max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 20px)))
  );
  height: 100%;
  box-sizing: border-box;
  overflow: auto;
  background: #1a1a2e;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: linear-gradient(135deg, #2d2d3a 0%, #252532 100%);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #3a3a4a;
  
  .stat-icon {
    width: 50px;
    height: 50px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: #fff;
  }
  
  &.electric .stat-icon {
    background: linear-gradient(135deg, #f59e0b, #d97706);
  }
  
  &.water .stat-icon {
    background: linear-gradient(135deg, #06b6d4, #0891b2);
  }
  
  &.gas .stat-icon {
    background: linear-gradient(135deg, #f97316, #ea580c);
  }
  
  .stat-content {
    flex: 1;
    
    .stat-label {
      font-size: 13px;
      color: #a0a0b0;
      margin-bottom: 4px;
    }
    
    .stat-value {
      font-size: 24px;
      font-weight: 700;
      color: #ffffff;
      font-family: 'Courier New', monospace;
      
      .stat-unit {
        font-size: 13px;
        font-weight: normal;
        color: #808090;
        margin-left: 4px;
      }
    }
    
    .stat-change {
      font-size: 12px;
      margin-top: 4px;
      
      &.up { color: #f87171; }
      &.down { color: #4ade80; }
    }
  }
}

.filter-section {
  background: #252532;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 16px;
  border: 1px solid #3a3a4a;
  
  .filter-form {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    
    :deep(.el-form-item) {
      margin-bottom: 0;
      margin-right: 16px;
      
      .el-form-item__label {
        color: #a0a0b0;
      }
    }
    
    .flex-grow {
      flex: 1;
      display: flex;
      justify-content: flex-end;
    }
  }
}

.table-section {
  background: #252532;
  border-radius: 8px;
  border: 1px solid #3a3a4a;
  overflow: hidden;
  
  .dark-table {
    --el-table-bg-color: #252532;
    --el-table-header-bg-color: #1a1a2e;
    --el-table-tr-bg-color: #252532;
    --el-table-row-hover-bg-color: #2d2d3a;
    --el-table-border-color: #3a3a4a;
    --el-table-text-color: #ffffff;
    --el-table-header-text-color: #a0a0b0;
  }
  
  .energy-badge {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 3px 10px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    
    &.badge-electric {
      background: rgba(245, 158, 11, 0.2);
      color: #fbbf24;
    }
    
    &.badge-water {
      background: rgba(6, 182, 212, 0.2);
      color: #22d3ee;
    }
    
    &.badge-gas {
      background: rgba(249, 115, 22, 0.2);
      color: #fb923c;
    }
  }
  
  .reading-value {
    font-weight: 600;
    color: #ffffff;
    font-family: 'Courier New', monospace;
  }
  
  .usage-value {
    font-weight: 600;
    color: #10b981;
    font-family: 'Courier New', monospace;
  }
  
  .read-method {
    padding: 3px 8px;
    border-radius: 4px;
    font-size: 12px;
    
    &.auto {
      background: rgba(16, 185, 129, 0.2);
      color: #10b981;
    }
  }
  
  .data-status {
    padding: 3px 10px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
    
    &.normal {
      background: rgba(16, 185, 129, 0.2);
      color: #10b981;
    }
    
    &.abnormal {
      background: rgba(239, 68, 68, 0.2);
      color: #f87171;
    }
  }
}

.pagination-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid #3a3a4a;
  background: #1a1a2e;
  
  .total-text {
    color: #a0a0b0;
    font-size: 13px;
  }
  
  :deep(.el-pagination) {
    --el-pagination-bg-color: transparent;
    --el-pagination-text-color: #a0a0b0;
    
    .el-pager li {
      background: #2d2d3a;
      border: 1px solid #3a3a4a;
      color: #ffffff;
      
      &.is-active {
        background: #10b981;
        border-color: #10b981;
      }
    }
  }
}

// 深色表单控件
:deep(.el-select), :deep(.el-date-picker) {
  .el-select__wrapper, .el-input__wrapper {
    background: #1a1a2e !important;
    border-color: #3a3a4a !important;
    box-shadow: none !important;
  }
}

:deep(.el-input) {
  .el-input__wrapper {
    background: #1a1a2e !important;
    border-color: #3a3a4a !important;
    box-shadow: none !important;
    
    .el-input__inner {
      color: #ffffff !important;
      
      &::placeholder {
        color: #6b7280 !important;
      }
    }
  }
}
</style>
