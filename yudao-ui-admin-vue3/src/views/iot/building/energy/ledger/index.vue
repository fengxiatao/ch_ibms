<template>
  <div class="energy-ledger-page dark-theme-page">
    <!-- 搜索栏 -->
    <div class="filter-section">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="表计类型">
          <el-select v-model="queryParams.meterType" placeholder="全部类型" clearable class="!w-120px">
            <el-option label="全部类型" :value="undefined" />
            <el-option label="电表" :value="1" />
            <el-option label="水表" :value="2" />
            <el-option label="燃气表" :value="3" />
            <el-option label="冷量表" :value="4" />
            <el-option label="热量表" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable class="!w-100px">
            <el-option label="全部状态" :value="undefined" />
            <el-option label="在线" :value="1" />
            <el-option label="离线" :value="0" />
            <el-option label="故障" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="区域">
          <el-select v-model="queryParams.areaId" placeholder="全部区域" clearable class="!w-140px">
            <el-option label="全部区域" :value="undefined" />
            <el-option label="1层大堂" :value="1" />
            <el-option label="2层办公区" :value="2" />
            <el-option label="3层办公区" :value="3" />
            <el-option label="地下机房" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="搜索">
          <el-input
            v-model="queryParams.meterName"
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
            <Icon icon="ep:download" class="mr-5px" /> 导出台账
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="list"
        :row-class-name="getRowClassName"
        stripe
        border
        class="dark-table"
      >
        <el-table-column label="表计编号" prop="meterCode" width="140" fixed="left" />
        <el-table-column label="表计名称" prop="meterName" min-width="180" />
        <el-table-column label="类型" prop="meterType" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getMeterTypeTag(row.meterType)" size="small">
              {{ getMeterTypeName(row.meterType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="所属区域" prop="areaName" width="120" />
        <el-table-column label="安装位置" prop="installLocation" width="160" show-overflow-tooltip />
        <el-table-column label="本期读数" prop="currentReading" width="120" align="right">
          <template #default="{ row }">
            <span class="reading-value">{{ formatNumber(row.currentReading) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="上期读数" prop="lastReading" width="120" align="right">
          <template #default="{ row }">{{ formatNumber(row.lastReading) }}</template>
        </el-table-column>
        <el-table-column label="本期用量" prop="todayUsage" width="130" align="right">
          <template #default="{ row }">
            <span class="usage-value">{{ formatUsage(row.todayUsage, row.meterType) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <span class="status-badge" :class="getStatusClass(row.status)">
              <span class="status-dot"></span>
              {{ getStatusLabel(row.status) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-section">
        <span class="total-text">共 {{ total }} 个计量点</span>
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
import type { IbmsEnergyMeterVO } from '@/api/iot/building/energy'
import { useMessage } from '@/hooks/web/useMessage'

defineOptions({ name: 'BuildingEnergyLedger' })

const message = useMessage()

// 数据
const loading = ref(false)
const list = ref<IbmsEnergyMeterVO[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  meterName: undefined as string | undefined,
  meterType: undefined as number | undefined,
  status: undefined as number | undefined,
  areaId: undefined as number | undefined
})

// 工具函数
const formatNumber = (value?: number) => {
  if (value === undefined || value === null) return '--'
  return value.toLocaleString('zh-CN', { maximumFractionDigits: 1 })
}

const formatUsage = (value?: number, type?: number) => {
  if (value === undefined || value === null) return '--'
  const unitMap: Record<number, string> = { 1: 'kWh', 2: 'm³', 3: 'm³', 4: 'kWh', 5: 'kWh' }
  return value.toLocaleString('zh-CN', { maximumFractionDigits: 2 }) + ' ' + (unitMap[type || 1] || '')
}

const getMeterTypeName = (type?: number) => {
  const map: Record<number, string> = { 1: '电表', 2: '水表', 3: '燃气表', 4: '冷量表', 5: '热量表' }
  return map[type || 0] || '未知'
}

const getMeterTypeTag = (type?: number) => {
  const map: Record<number, string> = { 1: 'warning', 2: 'success', 3: '', 4: 'info', 5: 'danger' }
  return map[type || 0] || 'info'
}

const getStatusLabel = (status?: number) => {
  const map: Record<number, string> = { 0: '离线', 1: '在线', 2: '故障' }
  return map[status ?? 0] || '未知'
}

const getStatusClass = (status?: number) => {
  const map: Record<number, string> = { 0: 'offline', 1: 'online', 2: 'fault' }
  return map[status ?? 0] || ''
}

const getRowClassName = ({ row }: { row: IbmsEnergyMeterVO }) => {
  if (row.status === 0) return 'row-offline'
  if (row.status === 2) return 'row-fault'
  return ''
}

// 查询
const getList = async () => {
  loading.value = true
  try {
    const res = await EnergyApi.getMeterPage(queryParams)
    list.value = res.list
    total.value = res.total
  } catch (e) {
    console.error('加载台账数据失败', e)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.meterName = undefined
  queryParams.meterType = undefined
  queryParams.status = undefined
  queryParams.areaId = undefined
  handleQuery()
}

const handleExport = () => {
  message.success('导出功能开发中...')
}

// 初始化
onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
@use '@/styles/dark-theme.scss' as *;

.energy-ledger-page {
  padding: 20px;
  padding-top: calc(
    20px + max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 20px)))
  );
  height: 100%;
  box-sizing: border-box;
  overflow: auto;
  background: #1a1a2e;
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
  padding: 0;
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
    
    :deep(.el-table__row) {
      &.row-offline {
        background: rgba(107, 114, 128, 0.1) !important;
        td { opacity: 0.7; }
      }
      
      &.row-fault {
        background: rgba(239, 68, 68, 0.1) !important;
      }
    }
  }
  
  .reading-value {
    font-weight: 600;
    color: #10b981;
    font-family: 'Courier New', monospace;
  }
  
  .usage-value {
    font-weight: 600;
    color: #60a5fa;
    font-family: 'Courier New', monospace;
  }
  
  .status-badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 4px 10px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 600;
    
    .status-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
    }
    
    &.online {
      background: rgba(16, 185, 129, 0.2);
      color: #10b981;
      .status-dot { background: #10b981; }
    }
    
    &.offline {
      background: rgba(107, 114, 128, 0.2);
      color: #9ca3af;
      .status-dot { background: #9ca3af; }
    }
    
    &.fault {
      background: rgba(239, 68, 68, 0.2);
      color: #f87171;
      .status-dot { background: #f87171; }
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
    --el-pagination-button-bg-color: #2d2d3a;
    --el-pagination-button-color: #ffffff;
    --el-pagination-hover-color: #10b981;
    
    .el-pager li {
      background: #2d2d3a;
      border: 1px solid #3a3a4a;
      
      &.is-active {
        background: #10b981;
        border-color: #10b981;
      }
    }
  }
}

// 深色表单控件
:deep(.el-select) {
  .el-select__wrapper {
    background: #1a1a2e !important;
    border-color: #3a3a4a !important;
    color: #ffffff !important;
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
