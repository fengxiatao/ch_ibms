<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧建筑</el-breadcrumb-item>
        <el-breadcrumb-item>给排水</el-breadcrumb-item>
        <el-breadcrumb-item>给排水告警记录</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon total">
                <el-icon><Warning /></el-icon>
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
                <el-icon><Bell /></el-icon>
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
              <div class="stats-icon processed">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ alarmStats.processed }}</div>
                <div class="stats-label">已处理</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 查询条件 -->
    <el-card class="search-card">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="设备名称">
          <el-input
            v-model="queryParams.deviceName"
            placeholder="请输入设备名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="系统类型">
          <el-select
            v-model="queryParams.systemType"
            placeholder="请选择系统类型"
            clearable
            style="width: 150px"
          >
            <el-option label="给水系统" value="water_supply" />
            <el-option label="排水系统" value="drainage" />
            <el-option label="消防系统" value="fire_fighting" />
            <el-option label="热水系统" value="hot_water" />
          </el-select>
        </el-form-item>
        <el-form-item label="告警类型">
          <el-select
            v-model="queryParams.alarmType"
            placeholder="请选择告警类型"
            clearable
            style="width: 150px"
          >
            <el-option label="压力异常" value="pressure" />
            <el-option label="流量异常" value="flow" />
            <el-option label="水位异常" value="level" />
            <el-option label="设备故障" value="fault" />
            <el-option label="水质异常" value="quality" />
          </el-select>
        </el-form-item>
        <el-form-item label="告警级别">
          <el-select
            v-model="queryParams.alarmLevel"
            placeholder="请选择告警级别"
            clearable
            style="width: 120px"
          >
            <el-option label="紧急" value="urgent" />
            <el-option label="重要" value="important" />
            <el-option label="一般" value="normal" />
            <el-option label="提示" value="info" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择处理状态"
            clearable
            style="width: 120px"
          >
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已处理" value="processed" />
            <el-option label="已忽略" value="ignored" />
          </el-select>
        </el-form-item>
        <el-form-item label="告警时间">
          <el-date-picker
            v-model="queryParams.alarmTime"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 300px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 告警记录表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>给排水告警记录</span>
          <div class="header-actions">
            <el-button type="danger" :disabled="selectedAlarms.length === 0" @click="handleBatchProcess">
              <el-icon><Tools /></el-icon>
              批量处理
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="alarmList"
        @selection-change="handleSelectionChange"
        border
        stripe
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="alarmId" label="告警ID" width="140" />
        <el-table-column prop="deviceName" label="设备名称" min-width="140" />
        <el-table-column prop="deviceLocation" label="设备位置" min-width="120" />
        <el-table-column prop="systemType" label="系统类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getSystemTypeColor(row.systemType)">
              {{ getSystemTypeText(row.systemType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alarmType" label="告警类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getAlarmTypeColor(row.alarmType)">
              {{ getAlarmTypeText(row.alarmType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alarmLevel" label="告警级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getAlarmLevelColor(row.alarmLevel)">
              {{ getAlarmLevelText(row.alarmLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alarmContent" label="告警内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="currentValue" label="当前值" width="100" />
        <el-table-column prop="alarmTime" label="告警时间" width="160" />
        <el-table-column prop="status" label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="processTime" label="处理时间" width="160" />
        <el-table-column prop="processor" label="处理人" width="100" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button 
              v-if="row.status === 'pending'"
              type="success" 
              size="small" 
              @click="handleProcess(row)"
            >
              <el-icon><Tools /></el-icon>
              处理
            </el-button>
            <el-button 
              v-if="row.status === 'pending'"
              type="warning" 
              size="small" 
              @click="handleIgnore(row)"
            >
              <el-icon><Hide /></el-icon>
              忽略
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-show="total > 0"
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getList"
        @current-change="getList"
        style="margin-top: 20px;"
      />
    </el-card>

    <!-- 告警详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="告警详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="告警ID">{{ currentAlarm?.alarmId }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ currentAlarm?.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="设备位置">{{ currentAlarm?.deviceLocation }}</el-descriptions-item>
        <el-descriptions-item label="系统类型">
          <el-tag :type="getSystemTypeColor(currentAlarm?.systemType)">
            {{ getSystemTypeText(currentAlarm?.systemType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="告警类型">
          <el-tag :type="getAlarmTypeColor(currentAlarm?.alarmType)">
            {{ getAlarmTypeText(currentAlarm?.alarmType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="告警级别">
          <el-tag :type="getAlarmLevelColor(currentAlarm?.alarmLevel)">
            {{ getAlarmLevelText(currentAlarm?.alarmLevel) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="告警时间">{{ currentAlarm?.alarmTime }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="getStatusColor(currentAlarm?.status)">
            {{ getStatusText(currentAlarm?.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前值">{{ currentAlarm?.currentValue }}</el-descriptions-item>
        <el-descriptions-item label="阈值范围">{{ currentAlarm?.threshold }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ currentAlarm?.processTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ currentAlarm?.processor || '-' }}</el-descriptions-item>
        <el-descriptions-item label="持续时间">{{ currentAlarm?.duration }}</el-descriptions-item>
        <el-descriptions-item label="影响程度">{{ currentAlarm?.impact }}</el-descriptions-item>
        <el-descriptions-item label="告警内容" :span="2">{{ currentAlarm?.alarmContent }}</el-descriptions-item>
        <el-descriptions-item label="处理说明" :span="2">{{ currentAlarm?.processNote || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 处理告警对话框 -->
    <el-dialog v-model="processDialogVisible" title="处理告警" width="600px">
      <el-form :model="processForm" :rules="processRules" ref="processFormRef" label-width="100px">
        <el-form-item label="处理方式" prop="processType">
          <el-radio-group v-model="processForm.processType">
            <el-radio label="repair">设备维修</el-radio>
            <el-radio label="replace">设备更换</el-radio>
            <el-radio label="adjust">参数调整</el-radio>
            <el-radio label="clean">清洁疏通</el-radio>
            <el-radio label="other">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理说明" prop="processNote">
          <el-input
            v-model="processForm.processNote"
            type="textarea"
            :rows="4"
            placeholder="请输入处理说明"
          />
        </el-form-item>
        <el-form-item label="预计完成时间">
          <el-date-picker
            v-model="processForm.expectedTime"
            type="datetime"
            placeholder="选择预计完成时间"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="processForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="processDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleConfirmProcess">确定</el-button>
        </span>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Warning, Bell, Clock, CircleCheck, Search, Refresh, Download, Tools, 
  View, Hide
} from '@element-plus/icons-vue'
import '@/styles/dark-theme.scss'

// 接口定义
interface AlarmRecord {
  id: string
  alarmId: string
  deviceName: string
  deviceLocation: string
  systemType: string
  alarmType: string
  alarmLevel: string
  alarmContent: string
  currentValue: string
  threshold: string
  alarmTime: string
  status: string
  processTime?: string
  processor?: string
  processNote?: string
  duration: string
  impact: string
}

// 响应式数据
const loading = ref(false)
const total = ref(0)
const detailDialogVisible = ref(false)
const processDialogVisible = ref(false)
const currentAlarm = ref<AlarmRecord | null>(null)
const selectedAlarms = ref<AlarmRecord[]>([])

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  deviceName: '',
  systemType: '',
  alarmType: '',
  alarmLevel: '',
  status: '',
  alarmTime: []
})

const queryRef = ref()

// 告警统计
const alarmStats = reactive({
  total: 89,
  urgent: 5,
  pending: 18,
  processed: 66
})

// 处理表单
const processForm = reactive({
  processType: '',
  processNote: '',
  expectedTime: '',
  remark: ''
})

const processRules = {
  processType: [{ required: true, message: '请选择处理方式', trigger: 'change' }],
  processNote: [{ required: true, message: '请输入处理说明', trigger: 'blur' }]
}

const processFormRef = ref()

// 告警记录列表数据
const alarmList = ref<AlarmRecord[]>([
  {
    id: '1',
    alarmId: 'WSD-001-2024012214301',
    deviceName: '生活水泵-01',
    deviceLocation: '地下室水泵房',
    systemType: 'water_supply',
    alarmType: 'pressure',
    alarmLevel: 'urgent',
    alarmContent: '供水压力异常，低于设定最小值',
    currentValue: '0.25MPa',
    threshold: '0.3-0.6MPa',
    alarmTime: '2024-01-22 14:30:15',
    status: 'pending',
    duration: '2小时30分钟',
    impact: '影响1-3层用水'
  },
  {
    id: '2',
    alarmId: 'WSD-002-2024012213451',
    deviceName: '污水提升泵-02',
    deviceLocation: '地下室污水井',
    systemType: 'drainage',
    alarmType: 'level',
    alarmLevel: 'important',
    alarmContent: '污水井水位过高，接近溢流点',
    currentValue: '1.85m',
    threshold: '<1.5m',
    alarmTime: '2024-01-22 13:45:20',
    status: 'processing',
    processor: '李师傅',
    processTime: '2024-01-22 14:00:00',
    duration: '3小时15分钟',
    impact: '可能造成污水倒灌'
  },
  {
    id: '3',
    alarmId: 'WSD-003-2024012212301',
    deviceName: '消防水箱',
    deviceLocation: '屋顶消防水箱间',
    systemType: 'fire_fighting',
    alarmType: 'level',
    alarmLevel: 'urgent',
    alarmContent: '消防水箱水位不足，低于安全水位',
    currentValue: '65%',
    threshold: '>80%',
    alarmTime: '2024-01-22 12:30:45',
    status: 'processed',
    processor: '张工程师',
    processTime: '2024-01-22 13:15:30',
    processNote: '已开启补水阀，水位恢复正常',
    duration: '4小时20分钟',
    impact: '影响消防系统安全'
  },
  {
    id: '4',
    alarmId: 'WSD-004-2024012211151',
    deviceName: '热水循环泵-01',
    deviceLocation: '热水机房',
    systemType: 'hot_water',
    alarmType: 'fault',
    alarmLevel: 'important',
    alarmContent: '热水循环泵电机过载保护动作',
    currentValue: '12.5A',
    threshold: '<10A',
    alarmTime: '2024-01-22 11:15:30',
    status: 'pending',
    duration: '5小时45分钟',
    impact: '热水供应不稳定'
  },
  {
    id: '5',
    alarmId: 'WSD-005-2024012210301',
    deviceName: '给水管道',
    deviceLocation: '3层东侧管道井',
    systemType: 'water_supply',
    alarmType: 'flow',
    alarmLevel: 'normal',
    alarmContent: '管道流量异常，疑似管道堵塞',
    currentValue: '2.5m³/h',
    threshold: '5-15m³/h',
    alarmTime: '2024-01-22 10:30:15',
    status: 'processed',
    processor: '王师傅',
    processTime: '2024-01-22 11:45:20',
    processNote: '清理管道过滤器，流量恢复正常',
    duration: '6小时30分钟',
    impact: '影响局部供水流量'
  },
  {
    id: '6',
    alarmId: 'WSD-006-2024012209451',
    deviceName: '水质检测仪',
    deviceLocation: '水处理间',
    systemType: 'water_supply',
    alarmType: 'quality',
    alarmLevel: 'important',
    alarmContent: '水质浊度超标，需要更换滤芯',
    currentValue: '15NTU',
    threshold: '<5NTU',
    alarmTime: '2024-01-22 09:45:30',
    status: 'processing',
    processor: '陈技术员',
    processTime: '2024-01-22 10:30:00',
    duration: '7小时15分钟',
    impact: '影响供水水质'
  }
])

// 方法
const getSystemTypeColor = (type: string) => {
  const colors = {
    water_supply: 'primary',
    drainage: 'warning',
    fire_fighting: 'danger',
    hot_water: 'success'
  }
  return colors[type] || 'info'
}

const getSystemTypeText = (type: string) => {
  const texts = {
    water_supply: '给水',
    drainage: '排水',
    fire_fighting: '消防',
    hot_water: '热水'
  }
  return texts[type] || '未知'
}

const getAlarmTypeColor = (type: string) => {
  const colors = {
    pressure: 'warning',
    flow: 'info',
    level: 'primary',
    fault: 'danger',
    quality: 'warning'
  }
  return colors[type] || 'info'
}

const getAlarmTypeText = (type: string) => {
  const texts = {
    pressure: '压力异常',
    flow: '流量异常',
    level: '水位异常',
    fault: '设备故障',
    quality: '水质异常'
  }
  return texts[type] || '未知'
}

const getAlarmLevelColor = (level: string) => {
  const colors = {
    urgent: 'danger',
    important: 'warning',
    normal: 'success',
    info: 'info'
  }
  return colors[level] || 'info'
}

const getAlarmLevelText = (level: string) => {
  const texts = {
    urgent: '紧急',
    important: '重要',
    normal: '一般',
    info: '提示'
  }
  return texts[level] || '未知'
}

const getStatusColor = (status: string) => {
  const colors = {
    pending: 'warning',
    processing: 'primary',
    processed: 'success',
    ignored: 'info'
  }
  return colors[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts = {
    pending: '待处理',
    processing: '处理中',
    processed: '已处理',
    ignored: '已忽略'
  }
  return texts[status] || '未知'
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryRef.value?.resetFields()
  queryParams.pageNum = 1
  getList()
}

const getList = () => {
  loading.value = true
  // 模拟API调用
  setTimeout(() => {
    total.value = alarmList.value.length
    loading.value = false
  }, 500)
}

const handleSelectionChange = (selection: AlarmRecord[]) => {
  selectedAlarms.value = selection
}

const handleViewDetail = (row: AlarmRecord) => {
  currentAlarm.value = row
  detailDialogVisible.value = true
}

const handleProcess = (row: AlarmRecord) => {
  currentAlarm.value = row
  Object.assign(processForm, {
    processType: '',
    processNote: '',
    expectedTime: '',
    remark: ''
  })
  processDialogVisible.value = true
}

const handleIgnore = async (row: AlarmRecord) => {
  try {
    await ElMessageBox.confirm(
      `确定要忽略告警 "${row.alarmId}" 吗？`,
      '确认忽略',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    ElMessage.success('告警已忽略')
    getList()
  } catch {
    ElMessage.info('操作已取消')
  }
}

const handleBatchProcess = () => {
  ElMessage.info('批量处理功能开发中')
}

const handleExport = () => {
  ElMessage.success('导出功能开发中')
}

const handleConfirmProcess = async () => {
  try {
    await processFormRef.value?.validate()
    ElMessage.success('告警处理成功')
    processDialogVisible.value = false
    getList()
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

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
        margin-right: 15px;
        font-size: 24px;
        
        &.total {
          background: linear-gradient(45deg, #409eff, #66b3ff);
          color: white;
        }
        
        &.urgent {
          background: linear-gradient(45deg, #f56c6c, #f78989);
          color: white;
        }
        
        &.pending {
          background: linear-gradient(45deg, #e6a23c, #ebb563);
          color: white;
        }
        
        &.processed {
          background: linear-gradient(45deg, #67c23a, #85ce61);
          color: white;
        }
      }
      
      .stats-info {
        .stats-value {
          font-size: 28px;
          font-weight: bold;
          color: #303133;
          line-height: 1;
        }
        
        .stats-label {
          font-size: 14px;
          color: #909399;
          margin-top: 5px;
        }
      }
    }
  }
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>

