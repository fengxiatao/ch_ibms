<template>
  <div class="energy-settings-page dark-theme-page">
    <!-- 电费设置 -->
    <div class="settings-card">
      <div class="card-header">
        <div class="card-title">
          <Icon icon="mdi:flash" class="icon electric" /> 电费费率设置
        </div>
        <el-button type="primary" size="small" @click="openRateForm('create', 1)">
          <Icon icon="ep:plus" class="mr-5px" /> 添加费率
        </el-button>
      </div>
      
      <el-table :data="electricRates" stripe class="dark-table settings-table">
        <el-table-column label="费率名称" prop="rateName" min-width="120" />
        <el-table-column label="时段类型" prop="rateLevel" width="100" align="center">
          <template #default="{ row }">
            <span class="rate-type" :class="getRateLevelClass(row.rateLevel)">
              {{ getRateLevelName(row.rateLevel) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="单价(元/kWh)" prop="unitPrice" width="140" align="right">
          <template #default="{ row }">
            <span class="price-value">¥{{ formatPrice(row.unitPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="时间范围" width="140" align="center">
          <template #default="{ row }">{{ row.startTime || '--' }} - {{ row.endTime || '--' }}</template>
        </el-table-column>
        <el-table-column label="生效日期" prop="effectiveDate" width="120" align="center" />
        <el-table-column label="状态" prop="status" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openRateForm('update', 1, row)">编辑</el-button>
            <el-button link type="danger" @click="handleDeleteRate(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 水费设置 -->
    <div class="settings-card">
      <div class="card-header">
        <div class="card-title">
          <Icon icon="mdi:water" class="icon water" /> 水费费率设置
        </div>
        <el-button type="primary" size="small" @click="openRateForm('create', 2)">
          <Icon icon="ep:plus" class="mr-5px" /> 添加费率
        </el-button>
      </div>
      
      <el-table :data="waterRates" stripe class="dark-table settings-table">
        <el-table-column label="费率名称" prop="rateName" min-width="120" />
        <el-table-column label="费率等级" prop="rateLevel" width="100" align="center">
          <template #default="{ row }">
            <span class="rate-type">{{ getRateLevelName(row.rateLevel) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单价(元/m³)" prop="unitPrice" width="140" align="right">
          <template #default="{ row }">
            <span class="price-value">¥{{ formatPrice(row.unitPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="生效日期" prop="effectiveDate" width="120" align="center" />
        <el-table-column label="状态" prop="status" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openRateForm('update', 2, row)">编辑</el-button>
            <el-button link type="danger" @click="handleDeleteRate(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 燃气费设置 -->
    <div class="settings-card">
      <div class="card-header">
        <div class="card-title">
          <Icon icon="mdi:fire" class="icon gas" /> 燃气费费率设置
        </div>
        <el-button type="primary" size="small" @click="openRateForm('create', 3)">
          <Icon icon="ep:plus" class="mr-5px" /> 添加费率
        </el-button>
      </div>
      
      <el-table :data="gasRates" stripe class="dark-table settings-table">
        <el-table-column label="费率名称" prop="rateName" min-width="120" />
        <el-table-column label="费率等级" prop="rateLevel" width="100" align="center">
          <template #default="{ row }">
            <span class="rate-type">{{ getRateLevelName(row.rateLevel) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单价(元/m³)" prop="unitPrice" width="140" align="right">
          <template #default="{ row }">
            <span class="price-value">¥{{ formatPrice(row.unitPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="生效日期" prop="effectiveDate" width="120" align="center" />
        <el-table-column label="状态" prop="status" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openRateForm('update', 3, row)">编辑</el-button>
            <el-button link type="danger" @click="handleDeleteRate(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 费率编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      class="dark-dialog"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        class="dark-form"
      >
        <el-form-item label="费率名称" prop="rateName">
          <el-input v-model="formData.rateName" placeholder="请输入费率名称" />
        </el-form-item>
        <el-form-item label="费率等级" prop="rateLevel">
          <el-select v-model="formData.rateLevel" placeholder="请选择费率等级" class="w-full">
            <template v-if="formData.energyType === 1">
              <el-option label="峰时" :value="1" />
              <el-option label="平时" :value="2" />
              <el-option label="谷时" :value="3" />
            </template>
            <template v-else>
              <el-option label="基础费率" :value="1" />
              <el-option label="阶梯一" :value="2" />
              <el-option label="阶梯二" :value="3" />
              <el-option label="阶梯三" :value="4" />
            </template>
          </el-select>
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number
            v-model="formData.unitPrice"
            :min="0"
            :precision="4"
            :step="0.01"
            class="w-full"
            controls-position="right"
          />
        </el-form-item>
        <el-row v-if="formData.energyType === 1" :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-time-picker
                v-model="formData.startTime"
                format="HH:mm"
                value-format="HH:mm"
                placeholder="选择时间"
                class="w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-time-picker
                v-model="formData.endTime"
                format="HH:mm"
                value-format="HH:mm"
                placeholder="选择时间"
                class="w-full"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="生效日期" prop="effectiveDate">
          <el-date-picker
            v-model="formData.effectiveDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="formData.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import * as EnergyApi from '@/api/iot/building/energy'
import type { IbmsEnergyRateVO, IbmsEnergyRateSaveReqVO } from '@/api/iot/building/energy'
import { useMessage } from '@/hooks/web/useMessage'

defineOptions({ name: 'BuildingEnergySettings' })

const message = useMessage()

// 费率数据
const electricRates = ref<IbmsEnergyRateVO[]>([])
const waterRates = ref<IbmsEnergyRateVO[]>([])
const gasRates = ref<IbmsEnergyRateVO[]>([])

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const submitLoading = ref(false)
const formData = reactive<IbmsEnergyRateSaveReqVO>({
  id: undefined,
  rateName: '',
  energyType: 1,
  rateLevel: undefined,
  unitPrice: 0,
  startTime: '',
  endTime: '',
  effectiveDate: '',
  status: 1,
  remark: ''
})

const formRules = {
  rateName: [{ required: true, message: '请输入费率名称', trigger: 'blur' }],
  rateLevel: [{ required: true, message: '请选择费率等级', trigger: 'change' }],
  unitPrice: [{ required: true, message: '请输入单价', trigger: 'blur' }],
  effectiveDate: [{ required: true, message: '请选择生效日期', trigger: 'change' }]
}

// 工具函数
const formatPrice = (value?: number) => {
  if (value === undefined || value === null) return '0.00'
  return value.toFixed(4)
}

const getRateLevelName = (level?: number) => {
  const map: Record<number, string> = { 1: '峰时', 2: '平时', 3: '谷时', 4: '阶梯三' }
  return map[level || 0] || '基础'
}

const getRateLevelClass = (level?: number) => {
  const map: Record<number, string> = { 1: 'peak', 2: 'flat', 3: 'valley' }
  return map[level || 0] || ''
}

// 加载费率数据
const loadRates = async () => {
  try {
    const [electric, water, gas] = await Promise.all([
      EnergyApi.getRateListByEnergyType(1),
      EnergyApi.getRateListByEnergyType(2),
      EnergyApi.getRateListByEnergyType(3)
    ])
    electricRates.value = electric
    waterRates.value = water
    gasRates.value = gas
  } catch (e) {
    console.error('加载费率数据失败', e)
    // 使用默认数据
    electricRates.value = [
      { id: 1, rateName: '峰时电价', energyType: 1, rateLevel: 1, unitPrice: 1.2, startTime: '08:00', endTime: '22:00', effectiveDate: '2026-01-01', status: 1 },
      { id: 2, rateName: '谷时电价', energyType: 1, rateLevel: 3, unitPrice: 0.6, startTime: '22:00', endTime: '08:00', effectiveDate: '2026-01-01', status: 1 }
    ]
    waterRates.value = [
      { id: 3, rateName: '居民用水', energyType: 2, rateLevel: 1, unitPrice: 3.5, effectiveDate: '2026-01-01', status: 1 }
    ]
    gasRates.value = [
      { id: 4, rateName: '居民用气', energyType: 3, rateLevel: 1, unitPrice: 2.8, effectiveDate: '2026-01-01', status: 1 }
    ]
  }
}

// 打开费率表单
const openRateForm = (type: string, energyType: number, row?: IbmsEnergyRateVO) => {
  const typeMap: Record<number, string> = { 1: '电费', 2: '水费', 3: '燃气费' }
  dialogTitle.value = type === 'create' ? `添加${typeMap[energyType]}费率` : `编辑${typeMap[energyType]}费率`
  
  if (row) {
    Object.assign(formData, row)
  } else {
    Object.assign(formData, {
      id: undefined,
      rateName: '',
      energyType: energyType,
      rateLevel: undefined,
      unitPrice: 0,
      startTime: '',
      endTime: '',
      effectiveDate: '',
      status: 1,
      remark: ''
    })
  }
  
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  await formRef.value?.validate()
  
  submitLoading.value = true
  try {
    if (formData.id) {
      await EnergyApi.updateRate(formData)
      message.success('更新成功')
    } else {
      await EnergyApi.createRate(formData)
      message.success('创建成功')
    }
    dialogVisible.value = false
    loadRates()
  } catch (e) {
    console.error('保存失败', e)
    message.error('保存失败，请重试')
  } finally {
    submitLoading.value = false
  }
}

// 删除费率
const handleDeleteRate = async (row: IbmsEnergyRateVO) => {
  try {
    await message.confirm(`确认删除费率【${row.rateName}】吗？`)
    await EnergyApi.deleteRate(row.id!)
    message.success('删除成功')
    loadRates()
  } catch (e) {
    console.error('删除失败', e)
  }
}

// 初始化
onMounted(() => {
  loadRates()
})
</script>

<style lang="scss" scoped>
@use '@/styles/dark-theme.scss' as *;

.energy-settings-page {
  padding: 20px;
  padding-top: calc(
    20px + max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 20px)))
  );
  height: 100%;
  box-sizing: border-box;
  overflow: auto;
  background: #1a1a2e;
}

.settings-card {
  background: #252532;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #3a3a4a;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #3a3a4a;
    
    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #ffffff;
      display: flex;
      align-items: center;
      gap: 8px;
      
      .icon {
        font-size: 20px;
        
        &.electric { color: #fbbf24; }
        &.water { color: #22d3ee; }
        &.gas { color: #fb923c; }
      }
    }
  }
  
  .settings-table {
    --el-table-bg-color: transparent;
    --el-table-header-bg-color: #1a1a2e;
    --el-table-tr-bg-color: transparent;
    --el-table-row-hover-bg-color: #2d2d3a;
    --el-table-border-color: #3a3a4a;
    --el-table-text-color: #ffffff;
    --el-table-header-text-color: #a0a0b0;
    
    .rate-type {
      padding: 4px 10px;
      border-radius: 4px;
      font-size: 12px;
      font-weight: 600;
      background: rgba(16, 185, 129, 0.2);
      color: #10b981;
      
      &.peak {
        background: rgba(239, 68, 68, 0.2);
        color: #f87171;
      }
      
      &.flat {
        background: rgba(59, 130, 246, 0.2);
        color: #60a5fa;
      }
      
      &.valley {
        background: rgba(34, 197, 94, 0.2);
        color: #4ade80;
      }
    }
    
    .price-value {
      font-weight: 600;
      color: #10b981;
      font-family: 'Courier New', monospace;
    }
  }
}

// 深色弹窗
:deep(.dark-dialog) {
  .el-dialog {
    background: #252532 !important;
    border: 1px solid #3a3a4a;
    
    .el-dialog__header {
      background: #1a1a2e !important;
      border-bottom: 1px solid #3a3a4a;
      
      .el-dialog__title {
        color: #ffffff !important;
      }
    }
    
    .el-dialog__body {
      background: #252532 !important;
    }
    
    .el-dialog__footer {
      background: #1a1a2e !important;
      border-top: 1px solid #3a3a4a;
    }
  }
}

.dark-form {
  :deep(.el-form-item__label) {
    color: #a0a0b0 !important;
  }
  
  :deep(.el-input), :deep(.el-select), :deep(.el-input-number), :deep(.el-date-picker), :deep(.el-time-picker) {
    .el-input__wrapper, .el-select__wrapper {
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
  
  :deep(.el-textarea__inner) {
    background: #1a1a2e !important;
    border-color: #3a3a4a !important;
    color: #ffffff !important;
  }
}

.w-full {
  width: 100%;
}
</style>
