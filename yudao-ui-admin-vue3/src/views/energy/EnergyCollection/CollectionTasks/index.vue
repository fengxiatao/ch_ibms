<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧能源</el-breadcrumb-item>
        <el-breadcrumb-item>能耗采集</el-breadcrumb-item>
        <el-breadcrumb-item>采集任务</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :model="searchForm" label-width="80px" :inline="true">
        <el-form-item label="任务名称">
          <el-input v-model="searchForm.taskName" placeholder="请输入任务名称" clearable />
        </el-form-item>
        <el-form-item label="任务状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="运行中" value="running" />
            <el-option label="已停止" value="stopped" />
            <el-option label="异常" value="error" />
            <el-option label="暂停" value="paused" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据类型">
          <el-select v-model="searchForm.dataType" placeholder="请选择数据类型" clearable>
            <el-option label="电量" value="electricity" />
            <el-option label="水量" value="water" />
            <el-option label="燃气" value="gas" />
            <el-option label="蒸汽" value="steam" />
            <el-option label="制冷" value="cooling" />
            <el-option label="采暖" value="heating" />
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

    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <h2>采集任务管理</h2>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增任务
        </el-button>
        <el-button type="success" @click="handleBatchStart" :disabled="selectedRows.length === 0">
          <el-icon><VideoPlay /></el-icon>
          批量启动
        </el-button>
        <el-button type="warning" @click="handleBatchStop" :disabled="selectedRows.length === 0">
          <el-icon><VideoPause /></el-icon>
          批量停止
        </el-button>
        <el-button type="info" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon running">
                <el-icon><VideoPlay /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.runningCount }}</div>
                <div class="stat-label">运行中</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon stopped">
                <el-icon><VideoPause /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.stoppedCount }}</div>
                <div class="stat-label">已停止</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon error">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.errorCount }}</div>
                <div class="stat-label">异常</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon total">
                <el-icon><Collection /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.totalCount }}</div>
                <div class="stat-label">总任务数</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 任务列表 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="taskList"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="taskName" label="任务名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="dataType" label="数据类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getDataTypeColor(row.dataType)">
              {{ getDataTypeText(row.dataType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deviceCount" label="设备数量" width="100" />
        <el-table-column prop="interval" label="采集频率" width="120" />
        <el-table-column prop="lastCollectTime" label="最近采集时间" width="160" show-overflow-tooltip />
        <el-table-column prop="collectCount" label="采集次数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">
              <el-icon v-if="row.status === 'running'" class="loading-icon"><Loading /></el-icon>
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button 
              link 
              :type="row.status === 'running' ? 'warning' : 'success'" 
              @click="handleToggleStatus(row)"
            >
              <el-icon v-if="row.status === 'running'"><VideoPause /></el-icon>
              <el-icon v-else><VideoPlay /></el-icon>
              {{ row.status === 'running' ? '停止' : '启动' }}
            </el-button>
            <el-button link type="info" @click="handleViewLogs(row)">
              <el-icon><Document /></el-icon>
              日志
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
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <!-- 新增/编辑任务弹框 -->
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
            <el-form-item label="任务名称" prop="taskName">
              <el-input v-model="formData.taskName" placeholder="请输入任务名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数据类型" prop="dataType">
              <el-select v-model="formData.dataType" placeholder="请选择数据类型">
                <el-option label="电量" value="electricity" />
                <el-option label="水量" value="water" />
                <el-option label="燃气" value="gas" />
                <el-option label="蒸汽" value="steam" />
                <el-option label="制冷" value="cooling" />
                <el-option label="采暖" value="heating" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="采集频率" prop="interval">
              <el-select v-model="formData.interval" placeholder="请选择采集频率">
                <el-option label="每分钟" value="1分钟" />
                <el-option label="每5分钟" value="5分钟" />
                <el-option label="每15分钟" value="15分钟" />
                <el-option label="每30分钟" value="30分钟" />
                <el-option label="每小时" value="1小时" />
                <el-option label="每日" value="1天" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="存储天数" prop="retentionDays">
              <el-input-number
                v-model="formData.retentionDays"
                :min="1"
                :max="3650"
                controls-position="right"
              />
              <span style="margin-left: 8px;">天</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="设备选择" prop="selectedDevices">
          <el-transfer
            v-model="formData.selectedDevices"
            :data="deviceOptions"
            :titles="['可选设备', '已选设备']"
            filterable
            filter-placeholder="搜索设备"
          />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入任务描述"
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

    <!-- 详情弹框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="任务详情"
      width="800px"
      destroy-on-close
    >
      <div v-if="currentTask" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务名称">{{ currentTask.taskName }}</el-descriptions-item>
          <el-descriptions-item label="数据类型">
            <el-tag :type="getDataTypeColor(currentTask.dataType)">
              {{ getDataTypeText(currentTask.dataType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="采集频率">{{ currentTask.interval }}</el-descriptions-item>
          <el-descriptions-item label="存储天数">{{ currentTask.retentionDays }}天</el-descriptions-item>
          <el-descriptions-item label="设备数量">{{ currentTask.deviceCount }}个</el-descriptions-item>
          <el-descriptions-item label="采集次数">{{ currentTask.collectCount }}次</el-descriptions-item>
          <el-descriptions-item label="最近采集时间">{{ currentTask.lastCollectTime }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusColor(currentTask.status)">
              {{ getStatusText(currentTask.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentTask.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ currentTask.updateTime }}</el-descriptions-item>
          <el-descriptions-item label="任务描述" span="2">{{ currentTask.description || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 日志弹框 -->
    <el-dialog
      v-model="logDialogVisible"
      title="采集日志"
      width="1000px"
      destroy-on-close
    >
      <div class="log-content">
        <div class="log-header">
          <el-button type="primary" @click="refreshLogs">刷新日志</el-button>
          <el-button @click="clearLogs">清空日志</el-button>
        </div>
        <div class="log-list">
          <div v-for="log in taskLogs" :key="log.id" :class="['log-item', log.level]">
            <span class="log-time">{{ log.time }}</span>
            <span class="log-level">{{ log.level.toUpperCase() }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ContentWrap } from '@/components/ContentWrap'
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Refresh, VideoPlay, VideoPause, Download, Warning, Collection,
  View, Edit, Delete, Document, Loading
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const logDialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMode = ref<'add' | 'edit'>('add')
const currentTask = ref(null)
const selectedRows = ref([])

// 搜索表单
const searchForm = reactive({
  taskName: '',
  status: '',
  dataType: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表单数据
const formData = reactive({
  id: '',
  taskName: '',
  dataType: '',
  interval: '',
  retentionDays: 30,
  selectedDevices: [],
  description: ''
})

// 表单验证规则
const formRules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  dataType: [{ required: true, message: '请选择数据类型', trigger: 'change' }],
  interval: [{ required: true, message: '请选择采集频率', trigger: 'change' }],
  selectedDevices: [{ required: true, message: '请选择设备', trigger: 'change' }]
}

// 统计数据
const stats = reactive({
  runningCount: 12,
  stoppedCount: 8,
  errorCount: 2,
  totalCount: 22
})

// 设备选项
const deviceOptions = ref([
  { key: 'device_001', label: '电表001 - 1号楼' },
  { key: 'device_002', label: '电表002 - 2号楼' },
  { key: 'device_003', label: '水表001 - 1号楼' },
  { key: 'device_004', label: '燃气表001 - 1号楼' },
  { key: 'device_005', label: '蒸汽表001 - 锅炉房' }
])

// 任务列表数据
const taskList = ref([
  {
    id: '1',
    taskName: '1号楼电量采集',
    dataType: 'electricity',
    deviceCount: 15,
    interval: '15分钟',
    lastCollectTime: '2024-01-22 14:30:00',
    collectCount: 2580,
    status: 'running',
    retentionDays: 365,
    description: '1号楼所有电表的电量数据采集',
    createTime: '2024-01-01 09:00:00',
    updateTime: '2024-01-22 14:30:00'
  },
  {
    id: '2',
    taskName: '园区水量采集',
    dataType: 'water',
    deviceCount: 8,
    interval: '30分钟',
    lastCollectTime: '2024-01-22 14:00:00',
    collectCount: 1290,
    status: 'running',
    retentionDays: 180,
    description: '园区所有水表的用水量数据采集',
    createTime: '2024-01-05 10:30:00',
    updateTime: '2024-01-22 14:00:00'
  },
  {
    id: '3',
    taskName: '燃气表数据采集',
    dataType: 'gas',
    deviceCount: 5,
    interval: '1小时',
    lastCollectTime: '2024-01-22 13:00:00',
    collectCount: 645,
    status: 'stopped',
    retentionDays: 365,
    description: '燃气表的用气量数据采集',
    createTime: '2024-01-10 15:20:00',
    updateTime: '2024-01-22 13:00:00'
  }
])

// 任务日志
const taskLogs = ref([
  { id: 1, time: '2024-01-22 14:30:15', level: 'info', message: '采集任务开始执行' },
  { id: 2, time: '2024-01-22 14:30:16', level: 'info', message: '连接设备: 电表001' },
  { id: 3, time: '2024-01-22 14:30:17', level: 'success', message: '设备电表001数据采集成功，电量: 1250.5 kWh' },
  { id: 4, time: '2024-01-22 14:30:18', level: 'error', message: '设备电表002连接失败，重试中...' },
  { id: 5, time: '2024-01-22 14:30:20', level: 'success', message: '设备电表002重连成功，电量: 980.2 kWh' }
])

const formRef = ref()

// 获取数据类型颜色
const getDataTypeColor = (type: string) => {
  const colors = {
    electricity: 'primary',
    water: 'success',
    gas: 'warning',
    steam: 'danger',
    cooling: 'info',
    heating: ''
  }
  return colors[type] || 'info'
}

// 获取数据类型文本
const getDataTypeText = (type: string) => {
  const texts = {
    electricity: '电量',
    water: '水量',
    gas: '燃气',
    steam: '蒸汽',
    cooling: '制冷',
    heating: '采暖'
  }
  return texts[type] || type
}

// 获取状态颜色
const getStatusColor = (status: string) => {
  const colors = {
    running: 'success',
    stopped: 'info',
    error: 'danger',
    paused: 'warning'
  }
  return colors[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const texts = {
    running: '运行中',
    stopped: '已停止',
    error: '异常',
    paused: '暂停'
  }
  return texts[status] || status
}

// 事件处理
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    taskName: '',
    status: '',
    dataType: ''
  })
  handleSearch()
}

const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
}

const handleBatchStart = async () => {
  try {
    await ElMessageBox.confirm(`确认启动选中的 ${selectedRows.value.length} 个任务吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    selectedRows.value.forEach((row: any) => {
      if (row.status !== 'running') {
        row.status = 'running'
        row.lastCollectTime = new Date().toLocaleString('zh-CN')
      }
    })
    
    ElMessage.success('批量启动成功')
  } catch {
    // 用户取消
  }
}

const handleBatchStop = async () => {
  try {
    await ElMessageBox.confirm(`确认停止选中的 ${selectedRows.value.length} 个任务吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    selectedRows.value.forEach((row: any) => {
      if (row.status === 'running') {
        row.status = 'stopped'
      }
    })
    
    ElMessage.success('批量停止成功')
  } catch {
    // 用户取消
  }
}

const handleCreate = () => {
  dialogMode.value = 'add'
  dialogTitle.value = '新增采集任务'
  resetFormData()
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑采集任务'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleView = (row: any) => {
  currentTask.value = row
  detailDialogVisible.value = true
}

const handleToggleStatus = async (row: any) => {
  const action = row.status === 'running' ? '停止' : '启动'
  
  try {
    await ElMessageBox.confirm(`确认${action}该采集任务吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    if (row.status === 'running') {
      row.status = 'stopped'
    } else {
      row.status = 'running'
      row.lastCollectTime = new Date().toLocaleString('zh-CN')
    }
    
    ElMessage.success(`${action}成功`)
  } catch {
    // 用户取消
  }
}

const handleViewLogs = (row: any) => {
  currentTask.value = row
  logDialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该采集任务吗？删除后无法恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const index = taskList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      taskList.value.splice(index, 1)
    }
    
    ElMessage.success('删除成功')
  } catch {
    // 用户取消删除
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (dialogMode.value === 'add') {
      const newTask = {
        ...formData,
        id: `task_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        deviceCount: formData.selectedDevices.length,
        lastCollectTime: '-',
        collectCount: 0,
        status: 'stopped',
        createTime: new Date().toLocaleString('zh-CN'),
        updateTime: new Date().toLocaleString('zh-CN')
      }
      taskList.value.unshift(newTask)
      ElMessage.success('创建成功')
    } else if (dialogMode.value === 'edit') {
      const index = taskList.value.findIndex(item => item.id === formData.id)
      if (index > -1) {
        Object.assign(taskList.value[index], {
          ...formData,
          deviceCount: formData.selectedDevices.length,
          updateTime: new Date().toLocaleString('zh-CN')
        })
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
    taskName: '',
    dataType: '',
    interval: '',
    retentionDays: 30,
    selectedDevices: [],
    description: ''
  })
}

const refreshLogs = () => {
  ElMessage.success('日志已刷新')
}

const clearLogs = async () => {
  try {
    await ElMessageBox.confirm('确认清空日志吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    taskLogs.value = []
    ElMessage.success('日志已清空')
  } catch {
    // 用户取消
  }
}

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    pagination.total = taskList.value.length
    loading.value = false
  }, 500)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">@use '@/styles/dark-theme.scss';

.app-container {
  padding: 20px;

  .breadcrumb-container {
    margin-bottom: 20px;
  }

  .search-container {
    background: #1a1a1a;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    margin-bottom: 20px;

    .el-form-item {
      margin-bottom: 16px;
    }
  }

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 20px;
    background: #1a1a1a;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .header-title {
      h2 {
        margin: 0;
        color: #303133;
        font-size: 24px;
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .stats-container {
    margin-bottom: 20px;

    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;

        .stat-icon {
          width: 50px;
          height: 50px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
          color: #fff;

          &.running {
            background: linear-gradient(45deg, #67c23a, #85ce61);
          }

          &.stopped {
            background: linear-gradient(45deg, #909399, #b3b3b3);
          }

          &.error {
            background: linear-gradient(45deg, #f56c6c, #f78989);
          }

          &.total {
            background: linear-gradient(45deg, #409eff, #66b1ff);
          }
        }

        .stat-info {
          .stat-value {
            font-size: 24px;
            font-weight: bold;
            color: #303133;
            line-height: 1;
          }

          .stat-label {
            font-size: 14px;
            color: #909399;
            margin-top: 4px;
          }
        }
      }
    }
  }

  .table-container {
    background: #1a1a1a;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .loading-icon {
      animation: rotate 2s linear infinite;
    }

    @keyframes rotate {
      from {
        transform: rotate(0deg);
      }
      to {
        transform: rotate(360deg);
      }
    }

    .el-pagination {
      margin-top: 20px;
      text-align: right;
    }
  }
}

.detail-content {
  .el-descriptions {
    margin-bottom: 20px;
  }
}

.log-content {
  .log-header {
    margin-bottom: 16px;
    display: flex;
    gap: 12px;
  }

  .log-list {
    max-height: 400px;
    overflow-y: auto;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    padding: 10px;
    background: #f8f9fa;

    .log-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 8px 0;
      border-bottom: 1px solid #e4e7ed;
      font-family: 'Courier New', monospace;
      font-size: 12px;

      &:last-child {
        border-bottom: none;
      }

      .log-time {
        color: #909399;
        min-width: 140px;
      }

      .log-level {
        min-width: 60px;
        font-weight: bold;
      }

      .log-message {
        flex: 1;
        color: #303133;
      }

      &.info .log-level {
        color: #409eff;
      }

      &.success .log-level {
        color: #67c23a;
      }

      &.error .log-level {
        color: #f56c6c;
      }

      &.warning .log-level {
        color: #e6a23c;
      }
    }
  }
}
</style>






