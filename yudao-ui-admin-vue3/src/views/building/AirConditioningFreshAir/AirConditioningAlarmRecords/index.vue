<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="air-conditioning-alarm-records dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧建筑</el-breadcrumb-item>
        <el-breadcrumb-item>空调新风</el-breadcrumb-item>
        <el-breadcrumb-item>空调告警记录</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-form :model="filterForm" label-width="120px" :inline="true">
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
            placeholder="开始时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="filterForm.endTime"
            type="datetime"
            placeholder="结束时间"
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

    <!-- 空调告警记录 -->
    <el-card class="alarm-table-card">
      <template #header>
        <div class="card-header">
          <span>空调告警记录</span>
          <div>
            <el-button type="danger" size="small" :disabled="selectedAlarms.length === 0" @click="handleBatchProcess">
              <el-icon><Tools /></el-icon>
              批量处理
            </el-button>
            <el-button type="primary" size="small" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="pagedAlarms"
        style="width: 100%"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="serialNo" label="序号" width="80" />
        <el-table-column prop="eventCode" label="事件编号" width="120" />
        <el-table-column prop="deviceName" label="设备名称" width="150" />
        <el-table-column prop="location" label="空间位置" width="200" />
        <el-table-column prop="alarmType" label="告警类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getAlarmTypeTag(row.alarmType)" size="small">
              {{ getAlarmTypeText(row.alarmType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alarmLevel" label="告警等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getAlarmLevelTag(row.alarmLevel)" size="small">
              {{ getAlarmLevelText(row.alarmLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alarmTime" label="告警时间" width="180" />
        <el-table-column prop="status" label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button link type="success" size="small" :disabled="row.status !== 'pending'" @click="handleProcess(row)">
              <el-icon><CircleCheck /></el-icon>
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        background
        class="mt-20"
      />
    </el-card>

    <!-- 告警详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="告警详情" width="60%" draggable>
      <el-descriptions :column="2" border v-if="currentAlarm">
        <el-descriptions-item label="事件编号">{{ currentAlarm.eventCode }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ currentAlarm.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="空间位置">{{ currentAlarm.location }}</el-descriptions-item>
        <el-descriptions-item label="告警类型">
          <el-tag :type="getAlarmTypeTag(currentAlarm.alarmType)" size="small">
            {{ getAlarmTypeText(currentAlarm.alarmType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="告警等级">
          <el-tag :type="getAlarmLevelTag(currentAlarm.alarmLevel)" size="small">
            {{ getAlarmLevelText(currentAlarm.alarmLevel) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="告警时间">{{ currentAlarm.alarmTime }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="getStatusTag(currentAlarm.status)" size="small">
            {{ getStatusText(currentAlarm.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="告警描述" :span="2">{{ currentAlarm.description }}</el-descriptions-item>
        <el-descriptions-item v-if="currentAlarm.processTime" label="处理时间">{{ currentAlarm.processTime }}</el-descriptions-item>
        <el-descriptions-item v-if="currentAlarm.processUser" label="处理人">{{ currentAlarm.processUser }}</el-descriptions-item>
        <el-descriptions-item v-if="currentAlarm.processNote" label="处理备注" :span="2">{{ currentAlarm.processNote }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="success" v-if="currentAlarm?.status === 'pending'" @click="handleProcess(currentAlarm)">处理</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 处理告警对话框 -->
    <el-dialog v-model="processDialogVisible" title="处理告警" width="40%" draggable>
      <el-form :model="processForm" :rules="processRules" ref="processFormRef" label-width="100px">
        <el-form-item label="处理结果" prop="result">
          <el-radio-group v-model="processForm.result">
            <el-radio label="resolved">已处理</el-radio>
            <el-radio label="ignored">忽略</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理措施" prop="measures">
          <el-checkbox-group v-model="processForm.measures">
            <el-checkbox label="重启设备">重启设备</el-checkbox>
            <el-checkbox label="调整参数">调整参数</el-checkbox>
            <el-checkbox label="维修更换">维修更换</el-checkbox>
            <el-checkbox label="联系厂商">联系厂商</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="处理备注" prop="note">
          <el-input
            v-model="processForm.note"
            type="textarea"
            :rows="4"
            placeholder="请输入处理备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="processDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleProcessSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search, Tools, Download, View, CircleCheck
} from '@element-plus/icons-vue'

// 接口定义
interface AlarmRecord {
  serialNo: number
  eventCode: string
  deviceName: string
  location: string
  alarmType: string
  alarmLevel: string
  alarmTime: string
  status: string
  description: string
  processTime?: string
  processUser?: string
  processNote?: string
}

// 响应式数据
const loading = ref(false)
const detailDialogVisible = ref(false)
const processDialogVisible = ref(false)
const currentAlarm = ref<AlarmRecord | null>(null)
const selectedAlarms = ref<AlarmRecord[]>([])
const processFormRef = ref<any>(null)

const filterForm = reactive({
  location: '',
  startTime: '',
  endTime: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const processForm = reactive({
  result: 'resolved',
  measures: [] as string[],
  note: ''
})

const processRules = {
  result: [{ required: true, message: '请选择处理结果', trigger: 'change' }],
  note: [{ required: true, message: '请输入处理备注', trigger: 'blur' }]
}

// 告警记录数据
const alarmRecords = ref<AlarmRecord[]>([
  {
    serialNo: 1,
    eventCode: 'AC001',
    deviceName: '风机盘管004',
    location: '南区_科技研发楼_9F_办公区',
    alarmType: 'temperature_high',
    alarmLevel: 'high',
    alarmTime: '2024-06-18 14:30:00',
    status: 'pending',
    description: '空调出风温度过高，超过设定阈值'
  },
  {
    serialNo: 2,
    eventCode: 'AC002',
    deviceName: '风机盘管001',
    location: '南区_科技研发楼_8F_办公区',
    alarmType: 'fan_fault',
    alarmLevel: 'medium',
    alarmTime: '2024-06-18 13:15:00',
    status: 'processing',
    description: '风机运行异常，转速不稳定'
  },
  {
    serialNo: 3,
    eventCode: 'AC003',
    deviceName: '风机盘管002',
    location: '南区_科技研发楼_7F_会议室',
    alarmType: 'filter_block',
    alarmLevel: 'low',
    alarmTime: '2024-06-18 12:00:00',
    status: 'resolved',
    description: '过滤器堵塞，需要清洁或更换',
    processTime: '2024-06-18 15:00:00',
    processUser: '维修人员',
    processNote: '已更换过滤器，设备运行正常'
  },
  {
    serialNo: 4,
    eventCode: 'AC004',
    deviceName: '风机盘管006',
    location: '北区_办公楼_5F_休息区',
    alarmType: 'offline',
    alarmLevel: 'high',
    alarmTime: '2024-06-18 11:30:00',
    status: 'pending',
    description: '设备离线，无法通信'
  },
  {
    serialNo: 5,
    eventCode: 'AC005',
    deviceName: '风机盘管003',
    location: '东区_会议中心_3F_大厅',
    alarmType: 'water_valve_fault',
    alarmLevel: 'medium',
    alarmTime: '2024-06-18 10:45:00',
    status: 'ignored',
    description: '水阀调节异常，开度无法调节',
    processTime: '2024-06-18 16:00:00',
    processUser: '管理员',
    processNote: '计划维护期间，暂时忽略'
  }
])

// 计算属性
const filteredAlarms = computed(() => {
  let filtered = alarmRecords.value
  if (filterForm.location) {
    filtered = filtered.filter(alarm => alarm.location.includes(filterForm.location))
  }
  if (filterForm.status) {
    filtered = filtered.filter(alarm => alarm.status === filterForm.status)
  }
  if (filterForm.startTime && filterForm.endTime) {
    filtered = filtered.filter(alarm => {
      const alarmTime = new Date(alarm.alarmTime).getTime()
      const startTime = new Date(filterForm.startTime).getTime()
      const endTime = new Date(filterForm.endTime).getTime()
      return alarmTime >= startTime && alarmTime <= endTime
    })
  }
  return filtered
})

// 使用 watch 来更新 pagination.total
watch(filteredAlarms, (newValue) => {
  pagination.total = newValue.length
}, { immediate: true })

const pagedAlarms = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return filteredAlarms.value.slice(start, end)
})

// 方法
const getAlarmTypeText = (type: string) => {
  const texts = {
    temperature_high: '温度过高',
    fan_fault: '风机故障',
    filter_block: '过滤器堵塞',
    offline: '设备离线',
    water_valve_fault: '水阀故障'
  }
  return texts[type] || type
}

const getAlarmTypeTag = (type: string) => {
  const tags = {
    temperature_high: 'danger',
    fan_fault: 'warning',
    filter_block: 'info',
    offline: 'danger',
    water_valve_fault: 'warning'
  }
  return tags[type] || 'info'
}

const getAlarmLevelText = (level: string) => {
  const texts = {
    low: '低',
    medium: '中',
    high: '高'
  }
  return texts[level] || level
}

const getAlarmLevelTag = (level: string) => {
  const tags = {
    low: 'info',
    medium: 'warning',
    high: 'danger'
  }
  return tags[level] || 'info'
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

const getStatusTag = (status: string) => {
  const tags = {
    pending: 'warning',
    processing: 'primary',
    resolved: 'success',
    ignored: 'info'
  }
  return tags[status] || 'info'
}

const handleSearch = () => {
  pagination.currentPage = 1
}

const handleSelectionChange = (selection: AlarmRecord[]) => {
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

const handleSizeChange = (val: number) => {
  pagination.pageSize = val
}

const handleCurrentChange = (val: number) => {
  pagination.currentPage = val
}

const handleViewDetail = (row: AlarmRecord) => {
  currentAlarm.value = row
  detailDialogVisible.value = true
}

const handleProcess = (row: AlarmRecord) => {
  currentAlarm.value = row
  resetProcessForm()
  processDialogVisible.value = true
}

const handleProcessSubmit = async () => {
  if (!processFormRef.value) return
  
  try {
    await processFormRef.value.validate()
    
    if (currentAlarm.value) {
      currentAlarm.value.status = processForm.result
      currentAlarm.value.processTime = new Date().toLocaleString('zh-CN')
      currentAlarm.value.processUser = '当前用户'
      currentAlarm.value.processNote = processForm.note
    }
    
    processDialogVisible.value = false
    detailDialogVisible.value = false
    ElMessage.success('处理成功')
  } catch {
    // 表单验证失败
  }
}

const resetProcessForm = () => {
  Object.assign(processForm, {
    result: 'resolved',
    measures: [],
    note: ''
  })
}

onMounted(() => {
  pagination.total = alarmRecords.value.length
})
</script>

<style scoped lang="scss">
@use '@/styles/dark-theme.scss';

.air-conditioning-alarm-records {
  padding: 20px;

  .breadcrumb-container {
    margin-bottom: 20px;
  }

  .filter-section {
    background: #2d2d2d;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
  }

  .alarm-table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      > span {
        font-size: 16px;
        font-weight: 600;
      }

      > div {
        display: flex;
        gap: 10px;
      }
    }

    .mt-20 {
      margin-top: 20px;
    }
  }
}
</style>


