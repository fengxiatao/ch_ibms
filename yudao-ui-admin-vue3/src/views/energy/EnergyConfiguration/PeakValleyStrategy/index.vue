<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="peak-valley-strategy-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item>智慧能源</el-breadcrumb-item>
        <el-breadcrumb-item>能耗配置</el-breadcrumb-item>
        <el-breadcrumb-item>峰谷策略</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 策略列表 -->
    <div class="strategy-list-section">
      <el-card class="list-card" shadow="never">
        <template #header>
          <div class="list-header">
            <span class="list-title">峰谷策略</span>
            <el-button type="primary" @click="handleAddStrategy">
              <el-icon><Plus /></el-icon>
              新增
            </el-button>
          </div>
        </template>

        <!-- 筛选区域 -->
        <div class="filter-area">
          <el-form :model="filterForm" :inline="true" label-width="80px">
            <el-form-item label="策略名称">
              <el-input
                v-model="filterForm.strategyName"
                placeholder="请输入策略名称"
                clearable
                style="width: 200px"
              />
            </el-form-item>
            <el-form-item label="策略状态">
              <el-select
                v-model="filterForm.status"
                placeholder="请选择策略状态"
                clearable
                style="width: 150px"
              >
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

        <!-- 策略表格 -->
        <el-table :data="strategyList" v-loading="loading">
          <el-table-column prop="id" label="序号" width="80" />
          <el-table-column prop="strategyCode" label="策略编号" width="180" />
          <el-table-column prop="name" label="策略名称" min-width="120" />
          <el-table-column prop="effectiveDate" label="生效日期" width="180" />
          <el-table-column prop="peakHours" label="尖时段数" width="100" align="center" />
          <el-table-column prop="normalHours" label="峰时段数" width="100" align="center" />
          <el-table-column prop="valleyHours" label="平时段数" width="100" align="center" />
          <el-table-column prop="offPeakHours" label="谷时段数" width="100" align="center" />
          <el-table-column prop="status" label="策略状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'enabled' ? 'success' : 'info'">
                {{ row.status === 'enabled' ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button link type="warning" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button link type="danger" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 新增/编辑策略对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="900px" destroy-on-close>
      <el-form :model="strategyForm" :rules="strategyRules" ref="strategyFormRef" label-width="120px">
        <!-- 基本信息 -->
        <div class="form-section">
          <h4 class="section-title">基本信息</h4>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="策略名称" prop="name">
                <el-input v-model="strategyForm.name" placeholder="请输入策略名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="生效日期" prop="effectiveDate">
                <el-date-picker
                  v-model="strategyForm.effectiveDate"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="备注">
            <el-input
              v-model="strategyForm.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入备注信息"
            />
          </el-form-item>
        </div>

        <!-- 时间配置 -->
        <div class="form-section">
          <h4 class="section-title">时间配置</h4>
          <div class="time-grid">
            <!-- 时间头部 -->
            <div class="time-header">
              <div class="time-label"></div>
              <div v-for="hour in 24" :key="hour" class="hour-label">
                {{ String(hour - 1).padStart(2, '0') }}:00
              </div>
            </div>
            
            <!-- 时间段配置行 -->
            <div v-for="period in timePeriods" :key="period.key" class="time-row">
              <div class="period-label" :style="{ color: period.color }">
                {{ period.label }}
              </div>
              <div
                v-for="hour in 24"
                :key="hour"
                class="time-cell"
                :class="{ 
                  'active': isHourActive(period.key, hour - 1),
                  [period.key]: isHourActive(period.key, hour - 1)
                }"
                @click="toggleHour(period.key, hour - 1)"
              >
                {{ isHourActive(period.key, hour - 1) ? period.shortLabel : '' }}
              </div>
            </div>
          </div>
        </div>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button @click="handleReturn">返回</el-button>
          <el-button type="primary" @click="handleSubmit">确认</el-button>
        </div>
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
  Plus,
  Search,
  Refresh,
  View,
  Edit,
  Delete
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增策略')
const strategyFormRef = ref()

// 表单数据
const filterForm = reactive({
  strategyName: '',
  status: ''
})

const strategyForm = reactive({
  name: '',
  effectiveDate: [],
  remark: '',
  timeConfig: {
    peak: new Array(24).fill(false),    // 尖时段
    normal: new Array(24).fill(false),  // 峰时段
    flat: new Array(24).fill(false),    // 平时段
    valley: new Array(24).fill(false)   // 谷时段
  }
})

// 验证规则
const strategyRules = {
  name: [{ required: true, message: '请输入策略名称', trigger: 'blur' }],
  effectiveDate: [{ required: true, message: '请选择生效日期', trigger: 'change' }]
}

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 时间段配置
const timePeriods = [
  {
    key: 'peak',
    label: '尖',
    shortLabel: '尖峰',
    color: '#ff4757'
  },
  {
    key: 'normal',
    label: '峰',
    shortLabel: '峰段',
    color: '#ff6b35'
  },
  {
    key: 'flat',
    label: '平',
    shortLabel: '平段',
    color: '#3742fa'
  },
  {
    key: 'valley',
    label: '谷',
    shortLabel: '谷段',
    color: '#2ed573'
  }
]

// 策略列表数据
const strategyList = ref([
  {
    id: 1,
    strategyCode: '20FGP20250723001',
    name: '滑试',
    effectiveDate: '2025-07-24~2025-07-25',
    peakHours: 13,
    normalHours: 4,
    valleyHours: 3,
    offPeakHours: 4,
    status: 'enabled'
  },
  {
    id: 2,
    strategyCode: '20FGP20241205001',
    name: '12月份',
    effectiveDate: '2024-12-15~2024-12-31',
    peakHours: 24,
    normalHours: 0,
    valleyHours: 0,
    offPeakHours: 0,
    status: 'enabled'
  },
  {
    id: 3,
    strategyCode: '20FGP20240108001',
    name: '峰谷策略',
    effectiveDate: '2024-01-08~2024-01-31',
    peakHours: 5,
    normalHours: 6,
    valleyHours: 6,
    offPeakHours: 7,
    status: 'disabled'
  }
])

// 方法
const isHourActive = (periodKey: string, hour: number) => {
  return strategyForm.timeConfig[periodKey][hour]
}

const toggleHour = (periodKey: string, hour: number) => {
  // 首先清除该小时在所有时段的设置
  Object.keys(strategyForm.timeConfig).forEach(key => {
    strategyForm.timeConfig[key][hour] = false
  })
  
  // 然后设置当前时段
  strategyForm.timeConfig[periodKey][hour] = true
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    ElMessage.success('查询完成')
    loading.value = false
  }, 1000)
}

const handleReset = () => {
  filterForm.strategyName = ''
  filterForm.status = ''
  handleSearch()
}

const handleAddStrategy = () => {
  dialogTitle.value = '新增策略'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑策略'
  Object.assign(strategyForm, {
    name: row.name,
    effectiveDate: row.effectiveDate.split('~'),
    remark: row.remark || ''
  })
  
  // 模拟加载时间配置
  initTimeConfig()
  dialogVisible.value = true
}

const handleView = (row: any) => {
  ElMessage.info(`查看策略: ${row.name}`)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确认删除策略"${row.name}"？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  })
}

const handleSubmit = () => {
  strategyFormRef.value?.validate((valid: boolean) => {
    if (valid) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadStrategyData()
    }
  })
}

const handleReturn = () => {
  dialogVisible.value = false
}

const resetForm = () => {
  Object.assign(strategyForm, {
    name: '',
    effectiveDate: [],
    remark: '',
    timeConfig: {
      peak: new Array(24).fill(false),
      normal: new Array(24).fill(false),
      flat: new Array(24).fill(false),
      valley: new Array(24).fill(false)
    }
  })
}

const initTimeConfig = () => {
  // 模拟初始化时间配置
  // 尖峰时段: 03:00, 07:00
  strategyForm.timeConfig.peak[3] = true
  strategyForm.timeConfig.peak[7] = true
  
  // 峰时段: 08:00
  strategyForm.timeConfig.normal[8] = true
  
  // 谷时段: 09:00
  strategyForm.timeConfig.valley[9] = true
}

const loadStrategyData = () => {
  loading.value = true
  setTimeout(() => {
    pagination.total = strategyList.value.length
    loading.value = false
  }, 500)
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  loadStrategyData()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadStrategyData()
}

// 初始化
onMounted(() => {
  loadStrategyData()
})
</script>

<style lang="scss" scoped>@use '@/styles/dark-theme.scss';

.peak-valley-strategy-container {
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

  .strategy-list-section {
    .list-card {
      background: #1e293b !important;
      border: 1px solid #334155 !important;

      :deep(.el-card__header) {
        background: #1e293b !important;
        border-bottom: 1px solid #334155 !important;
      }

      :deep(.el-card__body) {
        background: #1e293b !important;
      }

      .list-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .list-title {
          color: #e5eaf3;
          font-weight: 600;
          font-size: 18px;
        }
      }

      .filter-area {
        margin-bottom: 20px;

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
      }
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

  .pagination-container {
    margin-top: 20px;
    text-align: right;

    :deep(.el-pagination) {
      .btn-next,
      .btn-prev,
      .el-pager li {
        background: #334155 !important;
        color: #e5eaf3 !important;
        border-color: #475569 !important;
      }

      .el-pager li.is-active {
        background: #3b82f6 !important;
        color: white !important;
      }
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

  .form-section {
    margin-bottom: 30px;

    .section-title {
      color: #e5eaf3;
      margin-bottom: 20px;
      font-size: 16px;
      font-weight: 600;
      border-bottom: 1px solid #334155;
      padding-bottom: 10px;
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

  :deep(.el-date-editor) {
    background: #334155 !important;
    border-color: #475569 !important;
    
    .el-input__inner {
      background: transparent !important;
      color: #e5eaf3 !important;
    }
  }

  :deep(.el-textarea__inner) {
    background: #334155 !important;
    border-color: #475569 !important;
    color: #e5eaf3 !important;
  }

  .time-grid {
    border: 1px solid #475569;
    border-radius: 8px;
    overflow: hidden;

    .time-header {
      display: flex;
      background: #334155;

      .time-label {
        width: 60px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-right: 1px solid #475569;
        font-weight: 600;
        color: #e5eaf3;
      }

      .hour-label {
        flex: 1;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-right: 1px solid #475569;
        font-size: 12px;
        color: #94a3b8;

        &:last-child {
          border-right: none;
        }
      }
    }

    .time-row {
      display: flex;
      border-top: 1px solid #475569;

      &:first-child {
        border-top: none;
      }

      .period-label {
        width: 60px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-right: 1px solid #475569;
        font-weight: 600;
        background: #1e293b;
      }

      .time-cell {
        flex: 1;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-right: 1px solid #475569;
        cursor: pointer;
        font-size: 10px;
        transition: all 0.3s;
        color: white;

        &:last-child {
          border-right: none;
        }

        &:hover {
          background: rgba(59, 130, 246, 0.2);
        }

        &.active {
          font-weight: 600;
        }

        &.peak {
          background: #ff4757;
        }

        &.normal {
          background: #ff6b35;
        }

        &.flat {
          background: #3742fa;
        }

        &.valley {
          background: #2ed573;
        }
      }
    }
  }
}
</style>