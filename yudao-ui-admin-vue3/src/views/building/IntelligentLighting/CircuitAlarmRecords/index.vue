<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="circuit-alarm-records dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧建筑</el-breadcrumb-item>
        <el-breadcrumb-item>智能照明</el-breadcrumb-item>
        <el-breadcrumb-item>回路告警记录</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-form :model="filterForm" label-width="80px" :inline="true">
        <el-form-item label="请选择空间位置">
          <el-select v-model="filterForm.location" placeholder="请选择空间位置" clearable style="width: 200px;">
            <el-option label="全部位置" value="" />
            <el-option label="南区_科技研发楼_9F" value="south_tech_9f" />
            <el-option label="北区_办公楼_5F" value="north_office_5f" />
            <el-option label="东区_会议中心_3F" value="east_meeting_3f" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="filterForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="filterForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item label="请选择处理状态">
          <el-select v-model="filterForm.status" placeholder="请选择处理状态" clearable style="width: 150px;">
            <el-option label="全部状态" value="" />
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已处理" value="resolved" />
            <el-option label="已忽略" value="ignored" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            更多筛选
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 告警记录表格 -->
    <div class="table-section">
      <el-card shadow="never">
        <template #header>
          <div class="table-header">
            <span>回路告警记录</span>
            <div class="header-actions">
              <el-button type="success" @click="handleBatchProcess">
                <el-icon><CircleCheck /></el-icon>
                批量处理
              </el-button>
              <el-button type="warning" @click="handleExport">
                <el-icon><Download /></el-icon>
                导出记录
              </el-button>
            </div>
          </div>
        </template>

        <el-table v-loading="loading" :data="alarmList" stripe border @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" />
          <el-table-column prop="serialNumber" label="序号" width="80" />
          <el-table-column prop="eventCode" label="事件编号" width="150" show-overflow-tooltip />
          <el-table-column prop="deviceName" label="设备名称" width="180" show-overflow-tooltip />
          <el-table-column prop="deviceType" label="专业系统" width="120" />
          <el-table-column prop="location" label="空间位置" width="200" show-overflow-tooltip />
          <el-table-column prop="alarmType" label="告警事件" width="120">
            <template #default="{ row }">
              <el-tag :type="getAlarmTypeColor(row.alarmType)">
                {{ getAlarmTypeText(row.alarmType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="alarmLevel" label="告警等级" width="100">
            <template #default="{ row }">
              <el-tag :type="getAlarmLevelColor(row.alarmLevel)">
                {{ getAlarmLevelText(row.alarmLevel) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="alarmTime" label="告警时间" width="160" />
          <el-table-column prop="status" label="处理状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusColor(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看详情
              </el-button>
              <el-button v-if="row.status === 'pending'" link type="success" @click="handleProcess(row)">
                <el-icon><CircleCheck /></el-icon>
                处理
              </el-button>
              <el-button v-if="row.status === 'pending'" link type="warning" @click="handleIgnore(row)">
                <el-icon><CircleClose /></el-icon>
                忽略
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

    <!-- 告警详情弹框 -->
    <el-dialog v-model="detailDialogVisible" title="告警记录详情" width="70%" draggable>
      <div v-if="currentAlarm" class="alarm-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="事件编号">{{ currentAlarm.eventCode }}</el-descriptions-item>
          <el-descriptions-item label="设备名称">{{ currentAlarm.deviceName }}</el-descriptions-item>
          <el-descriptions-item label="专业系统">{{ currentAlarm.deviceType }}</el-descriptions-item>
          <el-descriptions-item label="空间位置">{{ currentAlarm.location }}</el-descriptions-item>
          <el-descriptions-item label="告警事件">
            <el-tag :type="getAlarmTypeColor(currentAlarm.alarmType)">
              {{ getAlarmTypeText(currentAlarm.alarmType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="告警等级">
            <el-tag :type="getAlarmLevelColor(currentAlarm.alarmLevel)">
              {{ getAlarmLevelText(currentAlarm.alarmLevel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="告警时间">{{ currentAlarm.alarmTime }}</el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="getStatusColor(currentAlarm.status)">
              {{ getStatusText(currentAlarm.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="告警描述" :span="2">
            {{ currentAlarm.description || '暂无描述' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentAlarm.processTime" label="处理时间">
            {{ currentAlarm.processTime }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentAlarm.processUser" label="处理人员">
            {{ currentAlarm.processUser }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentAlarm.processNote" label="处理备注" :span="2">
            {{ currentAlarm.processNote }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button v-if="currentAlarm?.status === 'pending'" type="primary" @click="handleProcessFromDetail">
          立即处理
        </el-button>
      </template>
    </el-dialog>

    <!-- 处理告警弹框 -->
    <el-dialog v-model="processDialogVisible" title="处理告警" width="50%" draggable>
      <el-form ref="processFormRef" :model="processForm" :rules="processRules" label-width="100px">
        <el-form-item label="处理结果" prop="result">
          <el-radio-group v-model="processForm.result">
            <el-radio label="resolved">已解决</el-radio>
            <el-radio label="monitoring">持续监控</el-radio>
            <el-radio label="escalated">上报处理</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注" prop="note">
          <el-input
            v-model="processForm.note"
            type="textarea"
            :rows="4"
            placeholder="请输入处理备注"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleProcessSubmit">确认处理</el-button>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, CircleCheck, Download, View, CircleClose
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const detailDialogVisible = ref(false)
const processDialogVisible = ref(false)
const currentAlarm = ref(null)
const selectedAlarms = ref([])

const filterForm = reactive({
  location: '',
  startTime: '',
  endTime: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const processForm = reactive({
  result: '',
  note: ''
})

const processRules = {
  result: [{ required: true, message: '请选择处理结果', trigger: 'change' }],
  note: [{ required: true, message: '请输入处理备注', trigger: 'blur' }]
}

// 告警记录数据
const alarmList = ref([
  {
    serialNumber: 1,
    eventCode: 'AL_2024012801',
    deviceName: '普通照明回路11',
    deviceType: '照明回路',
    location: '南区_科技研发楼_9F_办公区',
    alarmType: 'circuit_fault',
    alarmLevel: 'high',
    alarmTime: '2024-01-28 14:30:25',
    status: 'pending',
    description: '回路电流异常，可能存在短路风险'
  },
  {
    serialNumber: 2,
    eventCode: 'AL_2024012802',
    deviceName: '应急照明回路5',
    deviceType: '应急照明',
    location: '南区_科技研发楼_8F_走廊',
    alarmType: 'power_failure',
    alarmLevel: 'critical',
    alarmTime: '2024-01-28 13:15:10',
    status: 'processing',
    processTime: '2024-01-28 13:45:00',
    processUser: '张维修',
    description: '应急照明电源故障，需要立即检修'
  },
  {
    serialNumber: 3,
    eventCode: 'AL_2024012803',
    deviceName: '景观照明回路3',
    deviceType: '景观照明',
    location: '室外_花园区_南侧',
    alarmType: 'overload',
    alarmLevel: 'medium',
    alarmTime: '2024-01-28 12:20:35',
    status: 'resolved',
    processTime: '2024-01-28 14:00:00',
    processUser: '李电工',
    processNote: '检查发现负载过大，已调整照明策略',
    description: '回路负载超出额定值15%'
  },
  {
    serialNumber: 4,
    eventCode: 'AL_2024012804',
    deviceName: '办公区照明回路7',
    deviceType: '普通照明',
    location: '北区_办公楼_5F_开放办公区',
    alarmType: 'communication_fault',
    alarmLevel: 'low',
    alarmTime: '2024-01-28 11:45:20',
    status: 'ignored',
    processTime: '2024-01-28 12:00:00',
    processUser: '系统管理员',
    processNote: '网络临时中断，已自动恢复',
    description: '设备通信中断，无法获取状态信息'
  },
  {
    serialNumber: 5,
    eventCode: 'AL_2024012805',
    deviceName: '会议室照明回路2',
    deviceType: '智能照明',
    location: '东区_会议中心_3F_大会议室',
    alarmType: 'sensor_fault',
    alarmLevel: 'medium',
    alarmTime: '2024-01-28 10:30:15',
    status: 'pending',
    description: '光照传感器异常，自动调节功能失效'
  }
])

const processFormRef = ref()

// 方法
const getAlarmTypeColor = (type: string) => {
  const colors = {
    circuit_fault: 'danger',
    power_failure: 'danger',
    overload: 'warning',
    communication_fault: 'info',
    sensor_fault: 'warning'
  }
  return colors[type] || 'info'
}

const getAlarmTypeText = (type: string) => {
  const texts = {
    circuit_fault: '回路故障',
    power_failure: '电源故障',
    overload: '负载过载',
    communication_fault: '通信故障',
    sensor_fault: '传感器故障'
  }
  return texts[type] || type
}

const getAlarmLevelColor = (level: string) => {
  const colors = {
    low: 'info',
    medium: 'warning',
    high: 'danger',
    critical: 'danger'
  }
  return colors[level] || 'info'
}

const getAlarmLevelText = (level: string) => {
  const texts = {
    low: '低',
    medium: '中',
    high: '高',
    critical: '紧急'
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

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleView = (alarm: any) => {
  currentAlarm.value = alarm
  detailDialogVisible.value = true
}

const handleProcess = (alarm: any) => {
  currentAlarm.value = alarm
  resetProcessForm()
  processDialogVisible.value = true
}

const handleProcessFromDetail = () => {
  detailDialogVisible.value = false
  resetProcessForm()
  processDialogVisible.value = true
}

const handleIgnore = async (alarm: any) => {
  try {
    await ElMessageBox.confirm(`确认忽略告警 "${alarm.eventCode}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    alarm.status = 'ignored'
    alarm.processTime = new Date().toLocaleString('zh-CN')
    alarm.processUser = '当前用户'
    alarm.processNote = '用户主动忽略'
    
    ElMessage.success('忽略成功')
  } catch {
    // 用户取消
  }
}

const handleSelectionChange = (selection: any[]) => {
  selectedAlarms.value = selection
}

const handleBatchProcess = () => {
  if (selectedAlarms.value.length === 0) {
    ElMessage.warning('请选择要处理的告警记录')
    return
  }
  ElMessage.success('批量处理功能开发中...')
}

const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

const handleProcessSubmit = async () => {
  if (!processFormRef.value) return
  
  try {
    await processFormRef.value.validate()
    
    if (currentAlarm.value) {
      currentAlarm.value.status = processForm.result === 'resolved' ? 'resolved' : 'processing'
      currentAlarm.value.processTime = new Date().toLocaleString('zh-CN')
      currentAlarm.value.processUser = '当前用户'
      currentAlarm.value.processNote = processForm.note
    }
    
    processDialogVisible.value = false
    ElMessage.success('处理成功')
  } catch {
    // 表单验证失败
  }
}

const resetProcessForm = () => {
  Object.assign(processForm, {
    result: '',
    note: ''
  })
}

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    pagination.total = alarmList.value.length
    loading.value = false
  }, 500)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
@use '@/styles/dark-theme.scss';

.circuit-alarm-records {
  padding: 20px;

  .breadcrumb-container {
    margin-bottom: 20px;
  }

  .filter-section {
    background: #2d2d2d;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .el-form-item {
      margin-bottom: 16px;
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
  }

  .alarm-detail {
    .el-descriptions {
      margin-bottom: 20px;
    }
  }
}
</style>



