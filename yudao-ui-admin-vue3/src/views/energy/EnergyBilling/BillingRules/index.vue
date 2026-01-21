<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="billing-rules-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item>智慧能源</el-breadcrumb-item>
        <el-breadcrumb-item>能耗计费</el-breadcrumb-item>
        <el-breadcrumb-item>计费规则</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">计费规则</h1>
      <p class="page-description">配置不同能源类型的计费标准和计费方式</p>
    </div>

    <!-- 计费规则卡片 -->
    <div class="billing-rules-grid">
      <el-row :gutter="20">
        <el-col :span="8" v-for="(rule, index) in billingRules" :key="index">
          <div class="billing-rule-card" @click="handleEditRule(rule)">
            <div class="rule-header">
              <div class="rule-icon">
                <el-icon :size="32" :color="rule.iconColor">
                  <component :is="rule.icon" />
                </el-icon>
              </div>
              <div class="rule-info">
                <h3 class="rule-title">{{ rule.title }}</h3>
                <p class="rule-subtitle">{{ rule.subtitle }}</p>
              </div>
            </div>
            
            <div class="rule-content">
              <div class="price-info">
                <div class="price-label">
                  <el-icon><Money /></el-icon>
                  计费单价：
                </div>
                <div class="price-value">{{ rule.price }}</div>
                <div class="price-unit">{{ rule.unit }}</div>
              </div>
              
              <div class="billing-info">
                <div class="billing-label">
                  <el-icon><Setting /></el-icon>
                  计费单位：
                </div>
                <div class="billing-value">{{ rule.billingUnit }}</div>
              </div>
              
              <div class="method-info">
                <div class="method-label">
                  <el-icon><Tools /></el-icon>
                  计费方式：
                </div>
                <div class="method-value">{{ rule.method }}</div>
              </div>
            </div>

            <div class="rule-footer">
              <el-tag :type="rule.status === 'active' ? 'success' : 'info'" size="small">
                {{ rule.status === 'active' ? '启用' : '停用' }}
              </el-tag>
              <span class="update-time">{{ rule.updateTime }}</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="primary" size="large" @click="handleBatchUpdate">
        <el-icon><Edit /></el-icon>
        批量编辑
      </el-button>
      <el-button type="success" size="large" @click="handleImportRules">
        <el-icon><Upload /></el-icon>
        导入规则
      </el-button>
      <el-button type="warning" size="large" @click="handleExportRules">
        <el-icon><Download /></el-icon>
        导出规则
      </el-button>
      <el-button type="info" size="large" @click="handleResetRules">
        <el-icon><Refresh /></el-icon>
        重置默认
      </el-button>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" :title="`编辑 - ${currentRule?.title}`" width="600px" destroy-on-close>
      <el-form :model="ruleForm" :rules="ruleRules" ref="ruleFormRef" label-width="120px" v-if="currentRule">
        <el-form-item label="规则类型">
          <el-input v-model="currentRule.title" disabled />
        </el-form-item>
        
        <el-form-item label="计费方式" prop="method">
          <el-select v-model="ruleForm.method" placeholder="请选择计费方式" style="width: 100%">
            <el-option label="固定费用" value="fixed" />
            <el-option label="阶梯计费" value="tiered" />
            <el-option label="峰谷计费" value="peak_valley" />
            <el-option label="实时计费" value="realtime" />
          </el-select>
        </el-form-item>

        <!-- 固定费用配置 -->
        <template v-if="ruleForm.method === 'fixed'">
          <el-form-item label="计费单价" prop="price">
            <el-input-number 
              v-model="ruleForm.price" 
              :precision="2" 
              :step="0.01" 
              :min="0"
              style="width: 200px"
            />
            <span style="margin-left: 10px; color: #909399;">{{ currentRule.unit }}</span>
          </el-form-item>
        </template>

        <!-- 阶梯计费配置 -->
        <template v-if="ruleForm.method === 'tiered'">
          <el-form-item label="阶梯配置">
            <div class="tiered-config">
              <div v-for="(tier, index) in ruleForm.tiers" :key="index" class="tier-item">
                <el-row :gutter="10" align="middle">
                  <el-col :span="6">
                    <el-input-number 
                      v-model="tier.from" 
                      placeholder="起始值"
                      :min="0"
                      size="small"
                    />
                  </el-col>
                  <el-col :span="2" style="text-align: center;">-</el-col>
                  <el-col :span="6">
                    <el-input-number 
                      v-model="tier.to" 
                      placeholder="结束值"
                      :min="tier.from"
                      size="small"
                    />
                  </el-col>
                  <el-col :span="6">
                    <el-input-number 
                      v-model="tier.price" 
                      placeholder="单价"
                      :precision="2"
                      :step="0.01"
                      :min="0"
                      size="small"
                    />
                  </el-col>
                  <el-col :span="4">
                    <el-button 
                      type="danger" 
                      size="small" 
                      @click="removeTier(index)"
                      v-if="ruleForm.tiers.length > 1"
                    >
                      删除
                    </el-button>
                  </el-col>
                </el-row>
              </div>
              <el-button type="primary" size="small" @click="addTier" style="margin-top: 10px;">
                添加阶梯
              </el-button>
            </div>
          </el-form-item>
        </template>

        <!-- 峰谷计费配置 -->
        <template v-if="ruleForm.method === 'peak_valley'">
          <el-form-item label="峰时单价" prop="peakPrice">
            <el-input-number 
              v-model="ruleForm.peakPrice" 
              :precision="2" 
              :step="0.01" 
              :min="0"
              style="width: 200px"
            />
            <span style="margin-left: 10px; color: #909399;">{{ currentRule.unit }}</span>
          </el-form-item>
          <el-form-item label="平时单价" prop="normalPrice">
            <el-input-number 
              v-model="ruleForm.normalPrice" 
              :precision="2" 
              :step="0.01" 
              :min="0"
              style="width: 200px"
            />
            <span style="margin-left: 10px; color: #909399;">{{ currentRule.unit }}</span>
          </el-form-item>
          <el-form-item label="谷时单价" prop="valleyPrice">
            <el-input-number 
              v-model="ruleForm.valleyPrice" 
              :precision="2" 
              :step="0.01" 
              :min="0"
              style="width: 200px"
            />
            <span style="margin-left: 10px; color: #909399;">{{ currentRule.unit }}</span>
          </el-form-item>
        </template>

        <!-- 实时计费配置 -->
        <template v-if="ruleForm.method === 'realtime'">
          <el-form-item label="基础单价" prop="basePrice">
            <el-input-number 
              v-model="ruleForm.basePrice" 
              :precision="2" 
              :step="0.01" 
              :min="0"
              style="width: 200px"
            />
            <span style="margin-left: 10px; color: #909399;">{{ currentRule.unit }}</span>
          </el-form-item>
          <el-form-item label="浮动系数" prop="floatFactor">
            <el-input-number 
              v-model="ruleForm.floatFactor" 
              :precision="2" 
              :step="0.01" 
              :min="0.1"
              :max="3.0"
              style="width: 200px"
            />
            <span style="margin-left: 10px; color: #909399;">倍数</span>
          </el-form-item>
        </template>

        <el-form-item label="状态">
          <el-switch
            v-model="ruleForm.status"
            active-text="启用"
            inactive-text="停用"
            :active-value="'active'"
            :inactive-value="'inactive'"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="ruleForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveRule">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量编辑对话框 -->
    <el-dialog v-model="batchDialogVisible" title="批量编辑计费规则" width="800px">
      <el-table :data="billingRules" stripe>
        <el-table-column prop="title" label="能源类型" width="120" />
        <el-table-column label="计费方式" width="150">
          <template #default="{ $index }">
            <el-select v-model="batchForms[$index].method" size="small">
              <el-option label="固定费用" value="fixed" />
              <el-option label="阶梯计费" value="tiered" />
              <el-option label="峰谷计费" value="peak_valley" />
              <el-option label="实时计费" value="realtime" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="基础单价" width="150">
          <template #default="{ $index }">
            <el-input-number 
              v-model="batchForms[$index].price" 
              :precision="2" 
              :step="0.01" 
              :min="0"
              size="small"
              style="width: 120px"
            />
          </template>
        </el-table-column>
        <el-table-column label="单位" width="80">
          <template #default="{ row }">
            {{ row.unit }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ $index }">
            <el-switch
              v-model="batchForms[$index].status"
              size="small"
              :active-value="'active'"
              :inactive-value="'inactive'"
            />
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveBatch">保存全部</el-button>
        </span>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ContentWrap } from '@/components/ContentWrap'
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Lightning, 
  MagicStick, 
  Sunny, 
  DataAnalysis,
  Refrigerator,
  Money,
  Setting,
  Tools,
  Edit,
  Upload,
  Download,
  Refresh
} from '@element-plus/icons-vue'

// 响应式数据
const editDialogVisible = ref(false)
const batchDialogVisible = ref(false)
const currentRule = ref(null)
const ruleFormRef = ref()

// 表单数据
const ruleForm = reactive({
  method: 'fixed',
  price: 0,
  peakPrice: 0,
  normalPrice: 0,
  valleyPrice: 0,
  basePrice: 0,
  floatFactor: 1.0,
  status: 'active',
  remark: '',
  tiers: [
    { from: 0, to: 100, price: 0 }
  ]
})

// 批量编辑表单
const batchForms = ref([])

// 表单验证规则
const ruleRules = {
  method: [{ required: true, message: '请选择计费方式', trigger: 'change' }],
  price: [{ required: true, message: '请输入计费单价', trigger: 'blur' }],
  peakPrice: [{ required: true, message: '请输入峰时单价', trigger: 'blur' }],
  normalPrice: [{ required: true, message: '请输入平时单价', trigger: 'blur' }],
  valleyPrice: [{ required: true, message: '请输入谷时单价', trigger: 'blur' }],
  basePrice: [{ required: true, message: '请输入基础单价', trigger: 'blur' }],
  floatFactor: [{ required: true, message: '请输入浮动系数', trigger: 'blur' }]
}

// 计费规则数据
const billingRules = ref([
  {
    title: '电费',
    subtitle: '电力计费规则',
    icon: Lightning,
    iconColor: '#f59e0b',
    price: '-',
    unit: '元/kWh',
    billingUnit: 'kWh',
    method: '保定费用',
    status: 'active',
    updateTime: '2025-09-27 10:30:00',
    config: {
      method: 'fixed',
      price: 1.30,
      peakPrice: 1.02,
      normalPrice: 0.72,
      valleyPrice: 0.42
    }
  },
  {
    title: '水费',
    subtitle: '水量计费规则',
    icon: MagicStick,
    iconColor: '#3b82f6',
    price: '4.10',
    unit: '元/m³',
    billingUnit: 'm³',
    method: '固定费用',
    status: 'active',
    updateTime: '2025-09-27 09:15:00',
    config: {
      method: 'fixed',
      price: 4.10
    }
  },
  {
    title: '燃气费',
    subtitle: '燃气计费规则',
    icon: Sunny,
    iconColor: '#ef4444',
    price: '4.17',
    unit: '元/m³',
    billingUnit: 'm³',
    method: '固定费用',
    status: 'active',
    updateTime: '2025-09-27 08:45:00',
    config: {
      method: 'fixed',
      price: 4.17
    }
  },
  {
    title: '热量费',
    subtitle: '热量计费规则',
    icon: DataAnalysis,
    iconColor: '#f97316',
    price: '1.00',
    unit: '元/GJ',
    billingUnit: 'GJ',
    method: '固定费用',
    status: 'active',
    updateTime: '2025-09-27 08:30:00',
    config: {
      method: 'fixed',
      price: 1.00
    }
  },
  {
    title: '冷量费',
    subtitle: '冷量计费规则',
    icon: Refrigerator,
    iconColor: '#06b6d4',
    price: '1.00',
    unit: '元/GJ',
    billingUnit: 'GJ',
    method: '固定费用',
    status: 'active',
    updateTime: '2025-09-27 08:00:00',
    config: {
      method: 'fixed',
      price: 1.00
    }
  },
  {
    title: '油费',
    subtitle: '燃油计费规则',
    icon: DataAnalysis,
    iconColor: '#8b5cf6',
    price: '7.50',
    unit: '元/L',
    billingUnit: 'L',
    method: '固定费用',
    status: 'active',
    updateTime: '2025-09-26 16:20:00',
    config: {
      method: 'fixed',
      price: 7.50
    }
  }
])

// 方法
const handleEditRule = (rule: any) => {
  currentRule.value = rule
  Object.assign(ruleForm, rule.config)
  editDialogVisible.value = true
}

const handleSaveRule = () => {
  ruleFormRef.value?.validate((valid: boolean) => {
    if (valid) {
      // 更新规则配置
      currentRule.value.config = { ...ruleForm }
      currentRule.value.method = getMethodName(ruleForm.method)
      currentRule.value.status = ruleForm.status
      currentRule.value.updateTime = new Date().toLocaleString('zh-CN')
      
      // 更新显示价格
      updateDisplayPrice(currentRule.value)
      
      editDialogVisible.value = false
      ElMessage.success('保存成功')
    }
  })
}

const updateDisplayPrice = (rule: any) => {
  const config = rule.config
  switch (config.method) {
    case 'fixed':
      rule.price = config.price?.toFixed(2) || '-'
      break
    case 'tiered':
      rule.price = '阶梯计费'
      break
    case 'peak_valley':
      rule.price = `${config.peakPrice}/${config.normalPrice}/${config.valleyPrice}`
      break
    case 'realtime':
      rule.price = '实时计费'
      break
    default:
      rule.price = '-'
  }
}

const getMethodName = (method: string) => {
  const methodMap = {
    fixed: '固定费用',
    tiered: '阶梯计费',
    peak_valley: '峰谷计费',
    realtime: '实时计费'
  }
  return methodMap[method] || '固定费用'
}

const addTier = () => {
  const lastTier = ruleForm.tiers[ruleForm.tiers.length - 1]
  ruleForm.tiers.push({
    from: lastTier.to + 1,
    to: lastTier.to + 100,
    price: 0
  })
}

const removeTier = (index: number) => {
  ruleForm.tiers.splice(index, 1)
}

const handleBatchUpdate = () => {
  batchForms.value = billingRules.value.map(rule => ({
    method: rule.config.method,
    price: rule.config.price || 0,
    status: rule.status
  }))
  batchDialogVisible.value = true
}

const handleSaveBatch = () => {
  billingRules.value.forEach((rule, index) => {
    const form = batchForms.value[index]
    rule.config.method = form.method
    rule.config.price = form.price
    rule.status = form.status
    rule.method = getMethodName(form.method)
    rule.updateTime = new Date().toLocaleString('zh-CN')
    updateDisplayPrice(rule)
  })
  
  batchDialogVisible.value = false
  ElMessage.success('批量保存成功')
}

const handleImportRules = () => {
  ElMessage.info('导入功能开发中...')
}

const handleExportRules = () => {
  ElMessage.success('正在导出计费规则...')
}

const handleResetRules = () => {
  ElMessageBox.confirm('确认重置所有计费规则为默认值？', '确认重置', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('重置成功')
  })
}

// 初始化
onMounted(() => {
  // 初始化显示价格
  billingRules.value.forEach(rule => {
    updateDisplayPrice(rule)
  })
})
</script>

<style lang="scss" scoped>@use '@/styles/dark-theme.scss';

.billing-rules-container {
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

  .page-header {
    margin-bottom: 30px;
    text-align: center;

    .page-title {
      color: #e5eaf3;
      font-size: 28px;
      font-weight: 600;
      margin-bottom: 10px;
    }

    .page-description {
      color: #94a3b8;
      font-size: 16px;
      margin: 0;
    }
  }

  .billing-rules-grid {
    margin-bottom: 40px;

    .billing-rule-card {
      background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
      border: 1px solid #475569;
      border-radius: 16px;
      padding: 24px;
      margin-bottom: 20px;
      cursor: pointer;
      transition: all 0.3s ease;
      height: 280px;
      display: flex;
      flex-direction: column;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 12px 24px rgba(0, 0, 0, 0.3);
        border-color: #3b82f6;
      }

      .rule-header {
        display: flex;
        align-items: center;
        margin-bottom: 20px;

        .rule-icon {
          margin-right: 15px;
          opacity: 0.9;
        }

        .rule-info {
          flex: 1;

          .rule-title {
            color: #e5eaf3;
            font-size: 20px;
            font-weight: 600;
            margin: 0 0 5px 0;
          }

          .rule-subtitle {
            color: #94a3b8;
            font-size: 14px;
            margin: 0;
          }
        }
      }

      .rule-content {
        flex: 1;
        
        .price-info,
        .billing-info,
        .method-info {
          display: flex;
          align-items: center;
          margin-bottom: 15px;
          
          .price-label,
          .billing-label,
          .method-label {
            display: flex;
            align-items: center;
            color: #94a3b8;
            font-size: 14px;
            width: 90px;
            
            .el-icon {
              margin-right: 6px;
            }
          }

          .price-value,
          .billing-value,
          .method-value {
            color: #e5eaf3;
            font-weight: 600;
            font-size: 16px;
            flex: 1;
          }

          .price-unit {
            color: #6b7485;
            font-size: 14px;
            margin-left: 8px;
          }
        }

        .price-value {
          color: #3b82f6 !important;
        }
      }

      .rule-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: auto;
        padding-top: 15px;
        border-top: 1px solid #475569;

        .update-time {
          color: #6b7485;
          font-size: 12px;
        }
      }
    }
  }

  .action-buttons {
    text-align: center;
    
    .el-button {
      margin: 0 10px;
      padding: 12px 24px;
      font-size: 16px;
    }
  }

  :deep(.el-dialog) {
    background: #1e293b !important;
    border: 1px solid #334155 !important;

    .el-dialog__header {
      background: #1e293b !important;
      border-bottom: 1px solid #334155 !important;
    }

    .el-dialog__title {
      color: #e5eaf3 !important;
    }

    .el-dialog__body {
      background: #1e293b !important;
      color: #e5eaf3 !important;
    }
  }

  :deep(.el-form-item__label) {
    color: #e5eaf3 !important;
  }

  :deep(.el-input__inner) {
    background: #334155 !important;
    border-color: #475569 !important;
    color: #e5eaf3 !important;
  }

  :deep(.el-select) {
    .el-input__inner {
      background: #334155 !important;
      border-color: #475569 !important;
      color: #e5eaf3 !important;
    }
  }

  :deep(.el-input-number) {
    .el-input__inner {
      background: #334155 !important;
      border-color: #475569 !important;
      color: #e5eaf3 !important;
    }
  }

  :deep(.el-textarea__inner) {
    background: #334155 !important;
    border-color: #475569 !important;
    color: #e5eaf3 !important;
  }

  :deep(.el-switch) {
    .el-switch__core {
      background: #6b7485 !important;
    }

    &.is-checked .el-switch__core {
      background: #3b82f6 !important;
    }
  }

  :deep(.el-table) {
    background: #1e293b !important;
    color: #e5eaf3 !important;

    th {
      background: #334155 !important;
      color: #e5eaf3 !important;
      border-color: #475569 !important;
    }

    td {
      background: #1e293b !important;
      border-color: #475569 !important;
    }

    .el-table__row:hover td {
      background: #334155 !important;
    }
  }

  .tiered-config {
    width: 100%;

    .tier-item {
      margin-bottom: 10px;
      
      :deep(.el-input-number) {
        width: 100%;
        
        .el-input__inner {
          background: #334155 !important;
          border-color: #475569 !important;
          color: #e5eaf3 !important;
        }
      }
    }
  }
}
</style>
