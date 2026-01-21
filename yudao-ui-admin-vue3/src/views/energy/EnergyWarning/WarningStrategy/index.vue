<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="warning-strategy dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧能源</el-breadcrumb-item>
        <el-breadcrumb-item>能耗预警</el-breadcrumb-item>
        <el-breadcrumb-item>预警策略</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-form :model="filterForm" label-width="80px" :inline="true">
        <el-form-item label="策略名称">
          <el-input v-model="filterForm.strategyName" placeholder="请输入策略名称" clearable />
        </el-form-item>
        <el-form-item label="能源类型">
          <el-select v-model="filterForm.energyType" placeholder="请选择">
            <el-option label="全部类型" value="" />
            <el-option label="电力" value="electricity" />
            <el-option label="水量" value="water" />
            <el-option label="燃气" value="gas" />
            <el-option label="热量" value="heat" />
          </el-select>
        </el-form-item>
        <el-form-item label="预警级别">
          <el-select v-model="filterForm.warningLevel" placeholder="请选择" clearable>
            <el-option label="一般" value="low" />
            <el-option label="重要" value="medium" />
            <el-option label="紧急" value="high" />
            <el-option label="严重" value="critical" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择" clearable>
            <el-option label="启用" value="enabled" />
            <el-option label="停用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计概览 -->
    <div class="overview-section">
      <el-row :gutter="20">
        <el-col :span="6" v-for="item in overviewData" :key="item.title">
          <div class="overview-card">
            <div class="card-icon" :style="{ backgroundColor: item.color }">
              <el-icon>
                <component :is="item.icon" />
              </el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">{{ item.title }}</div>
              <div class="card-value">{{ item.value }}</div>
              <div class="card-unit">{{ item.unit }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 预警策略表格 -->
    <div class="table-section">
      <el-card shadow="never">
        <template #header>
          <div class="table-header">
            <span>预警策略管理</span>
            <div class="header-actions">
              <el-button type="primary" @click="handleCreate">
                <el-icon><Plus /></el-icon>
                新增策略
              </el-button>
              <el-button type="success" @click="handleBatchEnable">
                <el-icon><CircleCheck /></el-icon>
                批量启用
              </el-button>
              <el-button type="warning" @click="handleBatchDisable">
                <el-icon><CircleClose /></el-icon>
                批量停用
              </el-button>
            </div>
          </div>
        </template>

        <el-table v-loading="loading" :data="strategyList" stripe border @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" />
          <el-table-column prop="strategyName" label="策略名称" min-width="180">
            <template #default="{ row }">
              <div class="strategy-info">
                <el-icon class="strategy-icon" :style="{ color: getLevelColor(row.warningLevel) }">
                  <Warning />
                </el-icon>
                <div>
                  <div class="strategy-name">{{ row.strategyName }}</div>
                  <div class="strategy-desc">{{ row.description }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="energyType" label="能源类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getEnergyTypeColor(row.energyType)">
                {{ getEnergyTypeName(row.energyType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="warningLevel" label="预警级别" width="100">
            <template #default="{ row }">
              <el-tag :type="getLevelTagType(row.warningLevel)">
                {{ getLevelName(row.warningLevel) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="threshold" label="预警阈值" width="120">
            <template #default="{ row }">
              {{ row.threshold }}{{ row.unit }}
            </template>
          </el-table-column>
          <el-table-column prop="triggerCondition" label="触发条件" width="120">
            <template #default="{ row }">
              {{ getConditionName(row.triggerCondition) }}
            </template>
          </el-table-column>
          <el-table-column prop="notificationMethod" label="通知方式" width="120">
            <template #default="{ row }">
              <div class="notification-methods">
                <el-tag v-for="method in row.notificationMethod" :key="method" size="small" style="margin-right: 4px;">
                  {{ getNotificationName(method) }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                active-value="enabled"
                inactive-value="disabled"
                @change="handleStatusChange(row)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160" show-overflow-tooltip />
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button link type="primary" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button link type="success" @click="handleTest(row)">
                <el-icon><Bell /></el-icon>
                测试
              </el-button>
              <el-button link type="danger" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          style="margin-top: 20px; text-align: right;"
          @size-change="loadData"
          @current-change="loadData"
        />
      </el-card>
    </div>

    <!-- 新增/编辑策略弹框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="策略名称" prop="strategyName">
              <el-input v-model="formData.strategyName" placeholder="请输入策略名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="能源类型" prop="energyType">
              <el-select v-model="formData.energyType" placeholder="请选择能源类型">
                <el-option label="电力" value="electricity" />
                <el-option label="水量" value="water" />
                <el-option label="燃气" value="gas" />
                <el-option label="热量" value="heat" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预警级别" prop="warningLevel">
              <el-select v-model="formData.warningLevel" placeholder="请选择预警级别">
                <el-option label="一般" value="low" />
                <el-option label="重要" value="medium" />
                <el-option label="紧急" value="high" />
                <el-option label="严重" value="critical" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="触发条件" prop="triggerCondition">
              <el-select v-model="formData.triggerCondition" placeholder="请选择触发条件">
                <el-option label="大于" value="gt" />
                <el-option label="小于" value="lt" />
                <el-option label="等于" value="eq" />
                <el-option label="大于等于" value="gte" />
                <el-option label="小于等于" value="lte" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预警阈值" prop="threshold">
              <el-input-number
                v-model="formData.threshold"
                :precision="2"
                :min="0"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-input v-model="formData.unit" placeholder="如：kWh、m³等" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="通知方式" prop="notificationMethod">
          <el-checkbox-group v-model="formData.notificationMethod">
            <el-checkbox label="email">邮件</el-checkbox>
            <el-checkbox label="sms">短信</el-checkbox>
            <el-checkbox label="wechat">微信</el-checkbox>
            <el-checkbox label="system">系统消息</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="策略描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入策略描述"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
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
  Search,
  Refresh,
  Plus,
  Edit,
  Delete,
  Warning,
  Bell,
  Setting,
  DataAnalysis,
  View,
  CircleCheck,
  CircleClose
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMode = ref<'add' | 'edit'>('add')
const selectedStrategies = ref<any[]>([])

const filterForm = reactive({
  strategyName: '',
  energyType: '',
  warningLevel: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表单数据
const formData = reactive({
  id: '',
  strategyName: '',
  energyType: '',
  warningLevel: '',
  threshold: 0,
  unit: '',
  triggerCondition: '',
  notificationMethod: [],
  description: '',
  status: 'enabled'
})

// 表单验证规则
const formRules = {
  strategyName: [{ required: true, message: '请输入策略名称', trigger: 'blur' }],
  energyType: [{ required: true, message: '请选择能源类型', trigger: 'change' }],
  warningLevel: [{ required: true, message: '请选择预警级别', trigger: 'change' }],
  threshold: [{ required: true, message: '请输入预警阈值', trigger: 'blur' }],
  triggerCondition: [{ required: true, message: '请选择触发条件', trigger: 'change' }],
  notificationMethod: [{ required: true, message: '请选择通知方式', trigger: 'change' }]
}

// 概览数据
const overviewData = ref([
  {
    title: '策略总数',
    value: '24',
    unit: '个',
    icon: Setting,
    color: '#3b82f6'
  },
  {
    title: '启用策略',
    value: '18',
    unit: '个',
    icon: CircleCheck,
    color: '#10b981'
  },
  {
    title: '今日触发',
    value: '12',
    unit: '次',
    icon: Warning,
    color: '#f59e0b'
  },
  {
    title: '处理率',
    value: '92.3',
    unit: '%',
    icon: DataAnalysis,
    color: '#ef4444'
  }
])

// 策略列表
const strategyList = ref([
  {
    id: '1',
    strategyName: '电力能耗超标预警',
    description: '当电力能耗超过设定阈值时触发预警',
    energyType: 'electricity',
    warningLevel: 'high',
    threshold: 1000,
    unit: 'kWh',
    triggerCondition: 'gt',
    notificationMethod: ['email', 'sms', 'system'],
    status: 'enabled',
    createTime: '2024-01-15 09:30:00'
  },
  {
    id: '2',
    strategyName: '水量异常消耗预警',
    description: '当水量消耗异常增长时触发预警',
    energyType: 'water',
    warningLevel: 'medium',
    threshold: 500,
    unit: 'm³',
    triggerCondition: 'gt',
    notificationMethod: ['email', 'system'],
    status: 'enabled',
    createTime: '2024-01-10 14:20:00'
  },
  {
    id: '3',
    strategyName: '燃气泄漏风险预警',
    description: '当燃气消耗量异常时触发安全预警',
    energyType: 'gas',
    warningLevel: 'critical',
    threshold: 100,
    unit: 'm³',
    triggerCondition: 'gt',
    notificationMethod: ['email', 'sms', 'wechat', 'system'],
    status: 'enabled',
    createTime: '2024-01-08 16:45:00'
  },
  {
    id: '4',
    strategyName: '热量供应不足预警',
    description: '当热量供应低于最低要求时触发预警',
    energyType: 'heat',
    warningLevel: 'medium',
    threshold: 50,
    unit: 'GJ',
    triggerCondition: 'lt',
    notificationMethod: ['email', 'system'],
    status: 'disabled',
    createTime: '2024-01-05 11:15:00'
  }
])

const formRef = ref()

// 方法
const getEnergyTypeColor = (type: string) => {
  const colors = {
    electricity: 'primary',
    water: 'success',
    gas: 'warning',
    heat: 'danger'
  }
  return colors[type] || 'info'
}

const getEnergyTypeName = (type: string) => {
  const names = {
    electricity: '电力',
    water: '水量',
    gas: '燃气',
    heat: '热量'
  }
  return names[type] || type
}

const getLevelColor = (level: string) => {
  const colors = {
    low: '#10b981',
    medium: '#f59e0b',
    high: '#ef4444',
    critical: '#dc2626'
  }
  return colors[level] || '#6b7280'
}

const getLevelTagType = (level: string) => {
  const types = {
    low: 'success',
    medium: 'warning',
    high: 'danger',
    critical: 'danger'
  }
  return types[level] || 'info'
}

const getLevelName = (level: string) => {
  const names = {
    low: '一般',
    medium: '重要',
    high: '紧急',
    critical: '严重'
  }
  return names[level] || level
}

const getConditionName = (condition: string) => {
  const names = {
    gt: '大于',
    lt: '小于',
    eq: '等于',
    gte: '大于等于',
    lte: '小于等于'
  }
  return names[condition] || condition
}

const getNotificationName = (method: string) => {
  const names = {
    email: '邮件',
    sms: '短信',
    wechat: '微信',
    system: '系统'
  }
  return names[method] || method
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(filterForm, {
    strategyName: '',
    energyType: '',
    warningLevel: '',
    status: ''
  })
  handleSearch()
}

const handleCreate = () => {
  dialogMode.value = 'add'
  dialogTitle.value = '新增预警策略'
  resetFormData()
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑预警策略'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleView = (row: any) => {
  ElMessage.info(`查看策略详情: ${row.strategyName}`)
}

const handleTest = (row: any) => {
  ElMessage.success(`测试策略: ${row.strategyName}`)
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该预警策略吗？删除后无法恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const index = strategyList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      strategyList.value.splice(index, 1)
    }
    
    ElMessage.success('删除成功')
  } catch {
    // 用户取消删除
  }
}

const handleStatusChange = (row: any) => {
  const statusText = row.status === 'enabled' ? '启用' : '停用'
  ElMessage.success(`${statusText}成功`)
}

const handleSelectionChange = (selection: any[]) => {
  selectedStrategies.value = selection
}

const handleBatchEnable = () => {
  if (selectedStrategies.value.length === 0) {
    ElMessage.warning('请选择要启用的策略')
    return
  }
  ElMessage.success('批量启用成功')
}

const handleBatchDisable = () => {
  if (selectedStrategies.value.length === 0) {
    ElMessage.warning('请选择要停用的策略')
    return
  }
  ElMessage.success('批量停用成功')
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (dialogMode.value === 'add') {
      const newStrategy = {
        ...formData,
        id: `strategy_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        createTime: new Date().toLocaleString('zh-CN')
      }
      strategyList.value.unshift(newStrategy)
      ElMessage.success('创建成功')
    } else if (dialogMode.value === 'edit') {
      const index = strategyList.value.findIndex(item => item.id === formData.id)
      if (index > -1) {
        Object.assign(strategyList.value[index], formData)
      }
      ElMessage.success('更新成功')
    }
    
    dialogVisible.value = false
  } catch {
    // 表单验证失败
  }
}

const resetFormData = () => {
  Object.assign(formData, {
    id: '',
    strategyName: '',
    energyType: '',
    warningLevel: '',
    threshold: 0,
    unit: '',
    triggerCondition: '',
    notificationMethod: [],
    description: '',
    status: 'enabled'
  })
}

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    pagination.total = strategyList.value.length
    loading.value = false
  }, 500)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">@use '@/styles/dark-theme.scss';

.warning-strategy {
  padding: 20px;

  .breadcrumb-container {
    margin-bottom: 20px;
  }

  .filter-section {
    background: #1a1a1a;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .el-form-item {
      margin-bottom: 16px;
    }
  }

  .overview-section {
    margin-bottom: 20px;

    .overview-card {
      background: #1a1a1a;
      border-radius: 8px;
      padding: 20px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      display: flex;
      align-items: center;
      height: 100px;

      .card-icon {
        width: 50px;
        height: 50px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 15px;
        color: #fff;
        font-size: 20px;
      }

      .card-content {
        flex: 1;

        .card-title {
          font-size: 14px;
          color: #909399;
          margin-bottom: 5px;
        }

        .card-value {
          font-size: 20px;
          font-weight: 600;
          color: #303133;
        }

        .card-unit {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }

  .table-section {
    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: 600;

      .header-actions {
        display: flex;
        gap: 10px;
      }
    }

    .strategy-info {
      display: flex;
      align-items: center;
      gap: 12px;

      .strategy-icon {
        font-size: 20px;
      }

      .strategy-name {
        font-weight: 500;
        color: #303133;
        line-height: 1.2;
      }

      .strategy-desc {
        font-size: 12px;
        color: #909399;
        margin-top: 2px;
      }
    }

    .notification-methods {
      display: flex;
      flex-wrap: wrap;
      gap: 2px;
    }
  }
}
</style>
