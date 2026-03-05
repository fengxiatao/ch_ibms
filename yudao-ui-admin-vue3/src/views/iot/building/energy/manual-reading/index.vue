<template>
  <div class="energy-manual-reading-page dark-theme-page">
    <div class="manual-read-container">
      <!-- 左侧录入表单 -->
      <div class="read-form">
        <div class="form-title">
          <Icon icon="mdi:pencil" class="mr-8px" /> 录入表计读数
        </div>
        
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="100px"
          class="dark-form"
        >
          <el-form-item label="选择表计" prop="meterId">
            <el-select
              v-model="formData.meterId"
              placeholder="请选择表计..."
              filterable
              class="w-full"
              @change="onMeterChange"
            >
              <el-option
                v-for="meter in meterList"
                :key="meter.id"
                :label="`${meter.meterCode} ${meter.meterName} (${getMeterTypeName(meter.meterType)})`"
                :value="meter.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="抄表日期" prop="readingTime">
            <el-date-picker
              v-model="formData.readingTime"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              class="w-full"
            />
          </el-form-item>
          
          <el-form-item label="本期读数" prop="readingValue">
            <el-input-number
              v-model="formData.readingValue"
              :min="0"
              :precision="1"
              :step="0.1"
              placeholder="请输入表盘读数"
              class="w-full"
              controls-position="right"
            />
          </el-form-item>
          
          <el-form-item label="抄表人员">
            <el-input v-model="formData.reader" readonly class="w-full" />
          </el-form-item>
          
          <el-form-item label="备注说明">
            <el-input
              v-model="formData.remark"
              type="textarea"
              :rows="2"
              placeholder="如有异常请备注..."
              class="w-full"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="submitReading" :loading="submitLoading" class="w-full submit-btn">
              <Icon icon="mdi:check" class="mr-5px" /> 确认录入
            </el-button>
          </el-form-item>
        </el-form>
        
        <!-- 选中表计信息 -->
        <div v-if="selectedMeter" class="meter-info">
          <div class="info-title">表计信息</div>
          <div class="info-row">
            <span class="info-label">上期读数:</span>
            <span class="info-value">{{ formatNumber(selectedMeter.lastReading) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">安装位置:</span>
            <span class="info-value">{{ selectedMeter.installLocation || '--' }}</span>
          </div>
          <div v-if="formData.readingValue && selectedMeter.lastReading" class="info-row">
            <span class="info-label">预计用量:</span>
            <span class="info-value highlight">
              {{ formatNumber(formData.readingValue - selectedMeter.lastReading) }}
              {{ getMeterUnit(selectedMeter.meterType) }}
            </span>
          </div>
        </div>
      </div>

      <!-- 右侧记录列表 -->
      <div class="read-records">
        <div class="records-header">
          <div class="records-title">
            <Icon icon="mdi:clipboard-list" class="mr-8px" /> 今日已录入记录
          </div>
          <el-button type="success" size="small" @click="handleExport">
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </div>
        
        <el-table
          v-loading="recordLoading"
          :data="todayRecords"
          stripe
          class="dark-table"
        >
          <el-table-column label="时间" prop="readingTime" width="80" align="center">
            <template #default="{ row }">{{ formatTime(row.readingTime) }}</template>
          </el-table-column>
          <el-table-column label="表计名称" prop="meterName" min-width="160" />
          <el-table-column label="读数" prop="readingValue" width="100" align="right">
            <template #default="{ row }">
              <span class="reading-value">{{ formatNumber(row.readingValue) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="录入人" prop="reader" width="80" align="center" />
          <el-table-column label="状态" prop="status" width="100" align="center">
            <template #default="{ row }">
              <span class="status-tag" :class="getStatusClass(row.status)">
                {{ getStatusText(row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 0"
                link
                type="primary"
                size="small"
                @click="reviewReading(row)"
              >
                复核
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 待抄表提醒 -->
        <div v-if="pendingMeters.length > 0" class="pending-alert">
          <div class="alert-title">
            <Icon icon="mdi:alert" class="mr-8px" /> 待抄表提醒
          </div>
          <div class="alert-content">
            <div v-for="meter in pendingMeters" :key="meter.id" class="alert-item">
              • {{ meter.meterCode }} {{ meter.meterName }} - 数据异常需人工复核
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import * as EnergyApi from '@/api/iot/building/energy'
import type { IbmsEnergyMeterVO, IbmsEnergyManualReadingVO } from '@/api/iot/building/energy'
import { useMessage } from '@/hooks/web/useMessage'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'BuildingEnergyManualReading' })

const message = useMessage()

// 数据
const meterList = ref<IbmsEnergyMeterVO[]>([])
const todayRecords = ref<IbmsEnergyManualReadingVO[]>([])
const pendingMeters = ref<IbmsEnergyMeterVO[]>([])
const submitLoading = ref(false)
const recordLoading = ref(false)

// 表单
const formRef = ref()
const formData = reactive({
  meterId: undefined as number | undefined,
  readingTime: new Date().toISOString().split('T')[0],
  readingValue: undefined as number | undefined,
  reader: '当前用户',
  remark: ''
})

const formRules = {
  meterId: [{ required: true, message: '请选择表计', trigger: 'change' }],
  readingTime: [{ required: true, message: '请选择抄表日期', trigger: 'change' }],
  readingValue: [{ required: true, message: '请输入本期读数', trigger: 'blur' }]
}

// 选中的仪表
const selectedMeter = computed(() => {
  if (!formData.meterId) return null
  return meterList.value.find(m => m.id === formData.meterId) || null
})

// 工具函数
const formatNumber = (value?: number) => {
  if (value === undefined || value === null) return '--'
  return value.toLocaleString('zh-CN', { maximumFractionDigits: 1 })
}

const formatTime = (time?: Date | string) => {
  if (!time) return '--'
  return formatDate(time, 'HH:mm')
}

const getMeterTypeName = (type?: number) => {
  const map: Record<number, string> = { 1: '电表', 2: '水表', 3: '燃气表', 4: '冷量表', 5: '热量表' }
  return map[type || 0] || '未知'
}

const getMeterUnit = (type?: number) => {
  const map: Record<number, string> = { 1: 'kWh', 2: 'm³', 3: 'm³', 4: 'kWh', 5: 'kWh' }
  return map[type || 1] || ''
}

const getStatusClass = (status?: number) => {
  const map: Record<number, string> = { 0: 'pending', 1: 'confirmed', 2: 'voided' }
  return map[status ?? 0] || ''
}

const getStatusText = (status?: number) => {
  const map: Record<number, string> = { 0: '待复核', 1: '已确认', 2: '已作废' }
  return map[status ?? 0] || '未知'
}

// 仪表选择变化
const onMeterChange = () => {
  formData.readingValue = undefined
}

// 加载仪表列表
const loadMeterList = async () => {
  try {
    // 只加载水表和燃气表（电表通常远程抄表）
    const waterMeters = await EnergyApi.getMeterListByType(2)
    const gasMeters = await EnergyApi.getMeterListByType(3)
    meterList.value = [...waterMeters, ...gasMeters]
  } catch (e) {
    console.error('加载仪表列表失败', e)
  }
}

// 加载今日记录
const loadTodayRecords = async () => {
  recordLoading.value = true
  try {
    const records = await EnergyApi.getTodayManualReadings()
    todayRecords.value = records
  } catch (e) {
    console.error('加载今日记录失败', e)
  } finally {
    recordLoading.value = false
  }
}

// 加载待抄表仪表
const loadPendingMeters = async () => {
  try {
    // 查找离线或异常的仪表
    const allMeters = await EnergyApi.getMeterList()
    pendingMeters.value = allMeters.filter(m => m.status === 0 || m.status === 2)
  } catch (e) {
    console.error('加载待抄表仪表失败', e)
  }
}

// 提交抄表
const submitReading = async () => {
  await formRef.value?.validate()
  
  submitLoading.value = true
  try {
    await EnergyApi.createManualReading({
      meterId: formData.meterId,
      readingValue: formData.readingValue,
      readingTime: new Date(formData.readingTime),
      reader: formData.reader,
      remark: formData.remark
    })
    
    message.success('录入成功')
    
    // 重置表单
    formData.meterId = undefined
    formData.readingValue = undefined
    formData.remark = ''
    
    // 刷新记录
    loadTodayRecords()
  } catch (e) {
    console.error('录入失败', e)
    message.error('录入失败，请重试')
  } finally {
    submitLoading.value = false
  }
}

// 复核记录
const reviewReading = async (record: IbmsEnergyManualReadingVO) => {
  try {
    await EnergyApi.reviewManualReading(record.id!, formData.reader)
    message.success('复核成功')
    loadTodayRecords()
  } catch (e) {
    console.error('复核失败', e)
    message.error('复核失败，请重试')
  }
}

// 导出
const handleExport = () => {
  message.success('导出功能开发中...')
}

// 初始化
onMounted(() => {
  loadMeterList()
  loadTodayRecords()
  loadPendingMeters()
})
</script>

<style lang="scss" scoped>
@use '@/styles/dark-theme.scss' as *;

.energy-manual-reading-page {
  padding: 20px;
  padding-top: calc(
    20px + max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 20px)))
  );
  height: 100%;
  box-sizing: border-box;
  overflow: auto;
  background: #1a1a2e;
}

.manual-read-container {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 20px;
}

.read-form {
  background: #252532;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #3a3a4a;
  height: fit-content;
  
  .form-title {
    font-size: 16px;
    font-weight: 600;
    color: #ffffff;
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 2px solid #3a3a4a;
    display: flex;
    align-items: center;
  }
  
  .dark-form {
    :deep(.el-form-item__label) {
      color: #a0a0b0;
    }
    
    :deep(.el-select), :deep(.el-date-picker) {
      .el-select__wrapper, .el-input__wrapper {
        background: #1a1a2e !important;
        border-color: #3a3a4a !important;
        box-shadow: none !important;
      }
    }
    
    :deep(.el-input), :deep(.el-textarea) {
      .el-input__wrapper, .el-textarea__inner {
        background: #1a1a2e !important;
        border-color: #3a3a4a !important;
        box-shadow: none !important;
        color: #ffffff !important;
        
        &::placeholder {
          color: #6b7280 !important;
        }
      }
    }
    
    :deep(.el-input-number) {
      .el-input__wrapper {
        background: #1a1a2e !important;
        border-color: #3a3a4a !important;
        box-shadow: none !important;
      }
      
      .el-input-number__decrease, .el-input-number__increase {
        background: #2d2d3a !important;
        border-color: #3a3a4a !important;
        color: #a0a0b0 !important;
        
        &:hover {
          color: #10b981 !important;
        }
      }
    }
  }
  
  .submit-btn {
    height: 44px;
    font-size: 15px;
    font-weight: 600;
  }
  
  .meter-info {
    margin-top: 20px;
    padding: 16px;
    background: #1a1a2e;
    border-radius: 8px;
    border-left: 3px solid #10b981;
    
    .info-title {
      font-weight: 600;
      color: #ffffff;
      margin-bottom: 12px;
      font-size: 14px;
    }
    
    .info-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
      font-size: 13px;
      
      .info-label {
        color: #6b7280;
      }
      
      .info-value {
        color: #ffffff;
        
        &.highlight {
          color: #10b981;
          font-weight: 600;
        }
      }
    }
  }
}

.read-records {
  background: #252532;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #3a3a4a;
  
  .records-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #3a3a4a;
    
    .records-title {
      font-size: 16px;
      font-weight: 600;
      color: #ffffff;
      display: flex;
      align-items: center;
    }
  }
  
  .dark-table {
    --el-table-bg-color: #252532;
    --el-table-header-bg-color: #1a1a2e;
    --el-table-tr-bg-color: #252532;
    --el-table-row-hover-bg-color: #2d2d3a;
    --el-table-border-color: #3a3a4a;
    --el-table-text-color: #ffffff;
    --el-table-header-text-color: #a0a0b0;
  }
  
  .reading-value {
    font-weight: 600;
    color: #ffffff;
    font-family: 'Courier New', monospace;
  }
  
  .status-tag {
    padding: 4px 10px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
    
    &.pending {
      background: rgba(245, 158, 11, 0.2);
      color: #fbbf24;
    }
    
    &.confirmed {
      background: rgba(16, 185, 129, 0.2);
      color: #10b981;
    }
    
    &.voided {
      background: rgba(107, 114, 128, 0.2);
      color: #9ca3af;
    }
  }
  
  .pending-alert {
    margin-top: 20px;
    padding: 16px;
    background: rgba(245, 158, 11, 0.1);
    border-radius: 8px;
    border-left: 4px solid #f59e0b;
    
    .alert-title {
      font-weight: 600;
      color: #fbbf24;
      margin-bottom: 8px;
      display: flex;
      align-items: center;
    }
    
    .alert-content {
      .alert-item {
        font-size: 13px;
        color: #d97706;
        line-height: 1.8;
      }
    }
  }
}

.w-full {
  width: 100%;
}
</style>
