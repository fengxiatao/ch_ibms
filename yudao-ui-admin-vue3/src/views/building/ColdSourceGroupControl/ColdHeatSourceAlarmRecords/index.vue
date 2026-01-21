<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧建筑</el-breadcrumb-item>
        <el-breadcrumb-item>冷源群控</el-breadcrumb-item>
        <el-breadcrumb-item>冷热源告警记录</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon total">
                <el-icon><Bell /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ alarmStats.total }}</div>
                <div class="stats-label">总告警数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon urgent">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ alarmStats.urgent }}</div>
                <div class="stats-label">紧急告警</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon pending">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ alarmStats.pending }}</div>
                <div class="stats-label">待处理</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon resolved">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ alarmStats.resolved }}</div>
                <div class="stats-label">已处理</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 查询条件 -->
    <div class="search-container">
      <el-form :model="searchForm" label-width="80px" :inline="true">
        <el-form-item label="告警级别">
          <el-select v-model="searchForm.level" placeholder="请选择告警级别" clearable style="width: 120px;">
            <el-option label="紧急" value="urgent" />
            <el-option label="重要" value="important" />
            <el-option label="一般" value="normal" />
            <el-option label="提示" value="info" />
          </el-select>
        </el-form-item>
        <el-form-item label="告警状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px;">
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已处理" value="resolved" />
            <el-option label="已忽略" value="ignored" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备类型">
          <el-select v-model="searchForm.deviceType" placeholder="请选择设备类型" clearable style="width: 150px;">
            <el-option label="冷水机组" value="chiller" />
            <el-option label="冷却塔" value="cooling_tower" />
            <el-option label="冷冻水泵" value="chilled_water_pump" />
            <el-option label="冷却水泵" value="cooling_water_pump" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input
            v-model="searchForm.deviceName"
            placeholder="请输入设备名称"
            clearable
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm"
          />
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
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
          <el-button type="warning" @click="handleBatchProcess">
            <el-icon><Operation /></el-icon>
            批量处理
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 告警列表 -->
    <div class="alarm-list">
      <el-card class="list-card">
        <template #header>
          <div class="card-header">
            <span>冷热源告警记录</span>
            <div class="header-actions">
              <el-switch
                v-model="autoRefresh"
                active-text="自动刷新"
                inactive-text="手动刷新"
                @change="handleAutoRefreshChange"
              />
            </div>
          </div>
        </template>

        <el-table :data="alarmList" stripe border style="width: 100%" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" />
          <el-table-column prop="alarmTime" label="告警时间" width="160" sortable />
          <el-table-column prop="level" label="告警级别" width="100">
            <template #default="{ row }">
              <el-tag :type="getLevelColor(row.level)" :class="{ 'blinking': row.level === 'urgent' }">
                {{ getLevelText(row.level) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="deviceName" label="设备名称" width="150" />
          <el-table-column prop="deviceType" label="设备类型" width="120">
            <template #default="{ row }">
              <el-tag type="info" size="small">
                {{ getDeviceTypeText(row.deviceType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="alarmType" label="告警类型" width="120" />
          <el-table-column prop="alarmContent" label="告警内容" min-width="200" show-overflow-tooltip />
          <el-table-column prop="currentValue" label="当前值" width="100" />
          <el-table-column prop="thresholdValue" label="阈值" width="100" />
          <el-table-column prop="status" label="处理状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusColor(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="duration" label="持续时间" width="120" />
          <el-table-column prop="processor" label="处理人" width="100" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button 
                v-if="row.status === 'pending'" 
                link 
                type="success" 
                size="small" 
                @click="handleProcess(row)"
              >
                <el-icon><Check /></el-icon>
                处理
              </el-button>
              <el-button 
                v-if="row.status === 'pending'" 
                link 
                type="warning" 
                size="small" 
                @click="handleIgnore(row)"
              >
                <el-icon><Close /></el-icon>
                忽略
              </el-button>
              <el-button link type="info" size="small" @click="handleViewDevice(row)">
                <el-icon><Monitor /></el-icon>
                设备
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
          style="margin-top: 20px; text-align: right;"
        />
      </el-card>
    </div>

    <!-- 告警详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="告警详情" width="60%" draggable>
      <div v-if="currentAlarm" class="alarm-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="告警时间">
            {{ currentAlarm.alarmTime }}
          </el-descriptions-item>
          <el-descriptions-item label="告警级别">
            <el-tag :type="getLevelColor(currentAlarm.level)">
              {{ getLevelText(currentAlarm.level) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="设备名称">
            {{ currentAlarm.deviceName }}
          </el-descriptions-item>
          <el-descriptions-item label="设备编号">
            {{ currentAlarm.deviceCode }}
          </el-descriptions-item>
          <el-descriptions-item label="设备类型">
            {{ getDeviceTypeText(currentAlarm.deviceType) }}
          </el-descriptions-item>
          <el-descriptions-item label="安装位置">
            {{ currentAlarm.location }}
          </el-descriptions-item>
          <el-descriptions-item label="告警类型">
            {{ currentAlarm.alarmType }}
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="getStatusColor(currentAlarm.status)">
              {{ getStatusText(currentAlarm.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="告警内容" :span="2">
            {{ currentAlarm.alarmContent }}
          </el-descriptions-item>
          <el-descriptions-item label="当前值">
            {{ currentAlarm.currentValue }}
          </el-descriptions-item>
          <el-descriptions-item label="阈值范围">
            {{ currentAlarm.thresholdValue }}
          </el-descriptions-item>
          <el-descriptions-item label="持续时间">
            {{ currentAlarm.duration }}
          </el-descriptions-item>
          <el-descriptions-item label="影响等级">
            {{ currentAlarm.impact }}
          </el-descriptions-item>
          <el-descriptions-item label="建议处理" :span="2">
            {{ currentAlarm.suggestion }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 处理记录 -->
        <div v-if="currentAlarm.processRecords?.length" class="process-records">
          <h4>处理记录</h4>
          <el-timeline>
            <el-timeline-item
              v-for="record in currentAlarm.processRecords"
              :key="record.id"
              :timestamp="record.processTime"
              :type="getProcessType(record.action)"
            >
              <div class="process-content">
                <div class="process-action">{{ record.action }}</div>
                <div class="process-desc">{{ record.description }}</div>
                <div class="process-operator">处理人：{{ record.operator }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-dialog>

    <!-- 处理告警对话框 -->
    <el-dialog v-model="processDialogVisible" title="处理告警" width="50%" draggable>
      <el-form ref="processFormRef" :model="processForm" :rules="processRules" label-width="100px">
        <el-form-item label="处理方式" prop="action">
          <el-radio-group v-model="processForm.action">
            <el-radio label="resolve">解决</el-radio>
            <el-radio label="ignore">忽略</el-radio>
            <el-radio label="transfer">转派</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="processForm.action === 'transfer'" label="转派给" prop="assignee">
          <el-select v-model="processForm.assignee" placeholder="请选择处理人" style="width: 100%;">
            <el-option label="张工程师" value="zhang" />
            <el-option label="李技术员" value="li" />
            <el-option label="王主管" value="wang" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理说明" prop="description">
          <el-input
            v-model="processForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入处理说明"
          />
        </el-form-item>
        <el-form-item label="附件上传">
          <el-upload
            class="upload-demo"
            action="#"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :before-remove="beforeRemove"
            multiple
            :limit="3"
            :on-exceed="handleExceed"
            :file-list="fileList"
          >
            <el-button size="small" type="primary">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">
                只能上传jpg/png文件，且不超过500kb
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="processDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveProcess">确定</el-button>
        </span>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Bell, Warning, Clock, CircleCheck, Search, Refresh, Download, Operation,
  View, Check, Close, Monitor
} from '@element-plus/icons-vue'

// 响应式数据
const autoRefresh = ref(false)
const detailDialogVisible = ref(false)
const processDialogVisible = ref(false)
const currentAlarm = ref(null)
const selectedAlarms = ref([])
const fileList = ref([])
const refreshTimer = ref(null)

// 告警统计
const alarmStats = reactive({
  total: 156,
  urgent: 8,
  pending: 23,
  resolved: 125
})

// 搜索表单
const searchForm = reactive({
  level: '',
  status: '',
  deviceType: '',
  deviceName: '',
  dateRange: []
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 处理表单
const processForm = reactive({
  action: 'resolve',
  assignee: '',
  description: ''
})

// 表单验证规则
const processRules = {
  action: [{ required: true, message: '请选择处理方式', trigger: 'change' }],
  description: [{ required: true, message: '请输入处理说明', trigger: 'blur' }],
  assignee: [{ required: true, message: '请选择转派人员', trigger: 'change' }]
}

// 告警列表数据
const alarmList = ref([
  {
    id: 1,
    alarmTime: '2024-01-22 14:25:30',
    level: 'urgent',
    deviceName: '1号冷水机组',
    deviceCode: 'CHL-001',
    deviceType: 'chiller',
    location: '1号冷机房',
    alarmType: '高温告警',
    alarmContent: '冷水机组出水温度过高，当前温度15.8°C，超出设定阈值12°C',
    currentValue: '15.8°C',
    thresholdValue: '≤12°C',
    status: 'pending',
    duration: '25分钟',
    processor: '',
    impact: '严重',
    suggestion: '立即检查冷水机组制冷系统，检查制冷剂是否充足，冷凝器是否需要清洁',
    processRecords: []
  },
  {
    id: 2,
    alarmTime: '2024-01-22 13:45:15',
    level: 'important',
    deviceName: '1号冷却塔',
    deviceCode: 'CT-001',
    deviceType: 'cooling_tower',
    location: '屋顶机房',
    alarmType: '振动异常',
    alarmContent: '冷却塔风机振动值超标，当前振动值8.5mm/s，超出正常范围',
    currentValue: '8.5mm/s',
    thresholdValue: '≤6mm/s',
    status: 'processing',
    duration: '1小时15分钟',
    processor: '张工程师',
    impact: '中等',
    suggestion: '检查风机叶片平衡，检查轴承润滑情况，必要时进行维护',
    processRecords: [
      {
        id: 1,
        processTime: '2024-01-22 14:00:00',
        action: '开始处理',
        description: '已派遣维护人员前往现场检查',
        operator: '张工程师'
      }
    ]
  },
  {
    id: 3,
    alarmTime: '2024-01-22 12:30:45',
    level: 'normal',
    deviceName: '2号冷冻水泵',
    deviceCode: 'CWP-002',
    deviceType: 'chilled_water_pump',
    location: '泵房',
    alarmType: '流量偏低',
    alarmContent: '冷冻水泵流量低于设定值，当前流量145m³/h，设定流量180m³/h',
    currentValue: '145m³/h',
    thresholdValue: '≥180m³/h',
    status: 'resolved',
    duration: '2小时30分钟',
    processor: '李技术员',
    impact: '轻微',
    suggestion: '检查水泵出口阀门开度，检查管路是否有堵塞',
    processRecords: [
      {
        id: 1,
        processTime: '2024-01-22 13:00:00',
        action: '开始处理',
        description: '检查水泵运行参数',
        operator: '李技术员'
      },
      {
        id: 2,
        processTime: '2024-01-22 15:00:00',
        action: '问题解决',
        description: '调整出口阀门开度，流量已恢复正常',
        operator: '李技术员'
      }
    ]
  }
])

// 计算属性和方法
const getLevelColor = (level: string) => {
  const colors = {
    urgent: 'danger',
    important: 'warning',
    normal: 'primary',
    info: 'info'
  }
  return colors[level] || 'info'
}

const getLevelText = (level: string) => {
  const texts = {
    urgent: '紧急',
    important: '重要',
    normal: '一般',
    info: '提示'
  }
  return texts[level] || level
}

const getStatusColor = (status: string) => {
  const colors = {
    pending: 'warning',
    processing: 'primary',
    resolved: 'success',
    ignored: 'info'
  }
  return colors[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts = {
    pending: '待处理',
    processing: '处理中',
    resolved: '已处理',
    ignored: '已忽略'
  }
  return texts[status] || status
}

const getDeviceTypeText = (type: string) => {
  const texts = {
    chiller: '冷水机组',
    cooling_tower: '冷却塔',
    chilled_water_pump: '冷冻水泵',
    cooling_water_pump: '冷却水泵'
  }
  return texts[type] || type
}

const getProcessType = (action: string) => {
  const types = {
    '开始处理': 'primary',
    '问题解决': 'success',
    '转派处理': 'warning',
    '忽略告警': 'info'
  }
  return types[action] || 'primary'
}

// 事件处理
const handleSearch = () => {
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    level: '',
    status: '',
    deviceType: '',
    deviceName: '',
    dateRange: []
  })
  loadData()
}

const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

const handleBatchProcess = () => {
  if (selectedAlarms.value.length === 0) {
    ElMessage.warning('请选择要处理的告警记录')
    return
  }
  ElMessage.info('批量处理功能开发中...')
}

const handleSelectionChange = (selection: any[]) => {
  selectedAlarms.value = selection
}

const handleView = (row: any) => {
  currentAlarm.value = row
  detailDialogVisible.value = true
}

const handleProcess = (row: any) => {
  currentAlarm.value = row
  Object.assign(processForm, {
    action: 'resolve',
    assignee: '',
    description: ''
  })
  processDialogVisible.value = true
}

const handleIgnore = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认忽略此告警吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    row.status = 'ignored'
    ElMessage.success('告警已忽略')
    loadData()
  } catch {
    // 用户取消
  }
}

const handleViewDevice = (row: any) => {
  ElMessage.info(`查看设备: ${row.deviceName}`)
}

const handleSaveProcess = () => {
  // 表单验证
  if (!processForm.description) {
    ElMessage.warning('请输入处理说明')
    return
  }
  
  if (processForm.action === 'transfer' && !processForm.assignee) {
    ElMessage.warning('请选择转派人员')
    return
  }

  ElMessage.success('告警处理完成')
  processDialogVisible.value = false
  loadData()
}

const handleAutoRefreshChange = () => {
  if (autoRefresh.value) {
    refreshTimer.value = setInterval(() => {
      loadData()
    }, 30000) // 30秒刷新一次
    ElMessage.success('已开启自动刷新')
  } else {
    if (refreshTimer.value) {
      clearInterval(refreshTimer.value)
      refreshTimer.value = null
    }
    ElMessage.info('已关闭自动刷新')
  }
}

// 文件上传相关
const handlePreview = (file: any) => {
  console.log('预览文件:', file)
}

const handleRemove = (file: any, fileList: any[]) => {
  console.log('移除文件:', file, fileList)
}

const beforeRemove = (file: any) => {
  return ElMessageBox.confirm(`确定移除 ${file.name}？`)
}

const handleExceed = (files: any[], fileList: any[]) => {
  ElMessage.warning(`当前限制选择 3 个文件，本次选择了 ${files.length} 个文件，共选择了 ${files.length + fileList.length} 个文件`)
}

const loadData = () => {
  console.log('Loading alarm data...', searchForm)
  pagination.total = alarmList.value.length
}

onMounted(() => {
  loadData()
})

onUnmounted(() => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
  }
})
</script>

<style scoped lang="scss">@use '@/styles/dark-theme.scss';

.app-container {
  padding: 20px;

  .breadcrumb-container {
    margin-bottom: 20px;
  }

  .stats-cards {
    margin-bottom: 20px;

    .stats-card {
      .stats-content {
        display: flex;
        align-items: center;
        
        .stats-icon {
          width: 60px;
          height: 60px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;
          
          .el-icon {
            font-size: 24px;
            color: white;
          }
          
          &.total {
            background: linear-gradient(135deg, #409eff, #66b1ff);
          }
          
          &.urgent {
            background: linear-gradient(135deg, #f56c6c, #f78989);
          }
          
          &.pending {
            background: linear-gradient(135deg, #e6a23c, #ebb563);
          }
          
          &.resolved {
            background: linear-gradient(135deg, #67c23a, #85ce61);
          }
        }
        
        .stats-info {
          .stats-value {
            font-size: 32px;
            font-weight: bold;
            color: #303133;
            line-height: 1;
          }
          
          .stats-label {
            font-size: 14px;
            color: #909399;
            margin-top: 4px;
          }
        }
      }
    }
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

  .alarm-list {
    .list-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
    }

    .blinking {
      animation: blink 1s infinite;
    }

    @keyframes blink {
      0%, 50% {
        opacity: 1;
      }
      51%, 100% {
        opacity: 0.5;
      }
    }
  }

  .alarm-detail {
    .process-records {
      margin-top: 30px;

      h4 {
        margin: 0 0 16px 0;
        color: #303133;
        border-bottom: 1px solid #e0e0e0;
        padding-bottom: 8px;
      }

      .process-content {
        .process-action {
          font-weight: 600;
          color: #303133;
          margin-bottom: 4px;
        }

        .process-desc {
          color: #606266;
          margin-bottom: 4px;
        }

        .process-operator {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
}
</style>






