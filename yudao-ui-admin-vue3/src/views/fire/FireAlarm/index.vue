<template>
  <ContentWrap style="margin-top: 70px;">
  <div class="fire-alarm-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧消防</el-breadcrumb-item>
        <el-breadcrumb-item>消防告警</el-breadcrumb-item>
        <el-breadcrumb-item>消防告警记录</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="68px">
        <el-form-item label="请选择空间位置" prop="location">
          <el-select v-model="queryParams.location" placeholder="请选择空间位置" clearable style="width: 200px">
            <el-option label="全部位置" value="" />
            <el-option label="A座1楼" value="A座1楼" />
            <el-option label="A座2楼" value="A座2楼" />
            <el-option label="B座1楼" value="B座1楼" />
            <el-option label="B座2楼" value="B座2楼" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="queryParams.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="queryParams.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="请选择状态或类型" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择状态或类型" clearable style="width: 160px">
            <el-option label="全部状态" value="" />
            <el-option label="火灾报警" value="fire" />
            <el-option label="故障报警" value="fault" />
            <el-option label="监管报警" value="supervision" />
            <el-option label="屏蔽报警" value="shield" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :icon="Search">搜索</el-button>
          <el-button @click="resetQuery" :icon="Refresh">重置</el-button>
          <el-button type="primary" @click="handleExport" :icon="Download">导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <h3>消防告警记录</h3>
      </div>
      
      <el-table
        v-loading="loading"
        :data="alarmList"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column prop="eventCode" label="事件编号" width="150" show-overflow-tooltip />
        <el-table-column prop="deviceName" label="设备名称" width="180" show-overflow-tooltip />
        <el-table-column prop="location" label="空间位置" width="150" show-overflow-tooltip />
        <el-table-column prop="alarmType" label="告警事件" width="120">
          <template #default="{ row }">
            <el-tag :type="getAlarmTypeColor(row.alarmType)" size="small">
              {{ getAlarmTypeText(row.alarmType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alarmLevel" label="告警等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getAlarmLevelColor(row.alarmLevel)" size="small">
              {{ getAlarmLevelText(row.alarmLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alarmTime" label="告警时间" width="160" show-overflow-tooltip />
        <el-table-column prop="status" label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)" :icon="View">
              查看
            </el-button>
            <el-button 
              v-if="row.status === 'pending'" 
              type="success" 
              size="small" 
              @click="handleProcess(row)"
              :icon="Check"
            >
              处理
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)" :icon="Delete">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-show="total > 0"
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :small="false"
          :disabled="loading"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="detailVisible" title="告警详情" width="600px" append-to-body>
      <el-descriptions v-if="currentAlarm" :column="2" border>
        <el-descriptions-item label="事件编号">{{ currentAlarm.eventCode }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ currentAlarm.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="空间位置">{{ currentAlarm.location }}</el-descriptions-item>
        <el-descriptions-item label="告警类型">
          <el-tag :type="getAlarmTypeColor(currentAlarm.alarmType)" size="small">
            {{ getAlarmTypeText(currentAlarm.alarmType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="告警等级">
          <el-tag :type="getAlarmLevelColor(currentAlarm.alarmLevel)" size="small">
            {{ getAlarmLevelText(currentAlarm.alarmLevel) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="告警时间">{{ currentAlarm.alarmTime }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="getStatusColor(currentAlarm.status)" size="small">
            {{ getStatusText(currentAlarm.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理人员">{{ currentAlarm.handler || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间" span="2">{{ currentAlarm.handleTime || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="告警描述" span="2">
          {{ currentAlarm.description || '暂无描述' }}
        </el-descriptions-item>
        <el-descriptions-item label="处理备注" span="2">
          {{ currentAlarm.handleRemark || '暂无备注' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 处理告警弹窗 -->
    <el-dialog v-model="processVisible" title="处理告警" width="500px" append-to-body>
      <el-form :model="processForm" :rules="processRules" ref="processFormRef" label-width="80px">
        <el-form-item label="处理方式" prop="handleType">
          <el-select v-model="processForm.handleType" placeholder="请选择处理方式">
            <el-option label="确认处理" value="confirm" />
            <el-option label="误报处理" value="false_alarm" />
            <el-option label="屏蔽处理" value="shield" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理备注" prop="handleRemark">
          <el-input
            v-model="processForm.handleRemark"
            type="textarea"
            :rows="4"
            placeholder="请输入处理备注"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="processVisible = false">取消</el-button>
          <el-button type="primary" @click="submitProcess" :loading="processLoading">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Refresh, Download, View, Check, Delete 
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const total = ref(0)
const alarmList = ref([])
const detailVisible = ref(false)
const processVisible = ref(false)
const processLoading = ref(false)
const currentAlarm = ref(null)
const selectedRows = ref([])

// 查询表单
const queryFormRef = ref()
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  location: '',
  startTime: '',
  endTime: '',
  status: ''
})

// 处理表单
const processFormRef = ref()
const processForm = reactive({
  handleType: '',
  handleRemark: ''
})

const processRules = {
  handleType: [
    { required: true, message: '请选择处理方式', trigger: 'change' }
  ],
  handleRemark: [
    { required: true, message: '请输入处理备注', trigger: 'blur' }
  ]
}

// 模拟数据
const mockData = [
  {
    id: 1,
    eventCode: 'FIRE-2024-001',
    deviceName: '烟感探测器001',
    location: 'A座3楼办公区',
    alarmType: 'fire',
    alarmLevel: 'high',
    alarmTime: '2024-09-27 14:30:25',
    status: 'pending',
    description: '检测到烟雾浓度超标',
    handler: '',
    handleTime: '',
    handleRemark: ''
  },
  {
    id: 2,
    eventCode: 'FAULT-2024-002',
    deviceName: '手动报警按钮002',
    location: 'B座2楼走廊',
    alarmType: 'fault',
    alarmLevel: 'medium',
    alarmTime: '2024-09-27 13:15:10',
    status: 'processed',
    description: '设备通信故障',
    handler: '张三',
    handleTime: '2024-09-27 13:20:00',
    handleRemark: '已更换设备通信模块'
  },
  {
    id: 3,
    eventCode: 'SUP-2024-003',
    deviceName: '消火栓按钮003',
    location: 'A座1楼大厅',
    alarmType: 'supervision',
    alarmLevel: 'low',
    alarmTime: '2024-09-27 12:45:30',
    status: 'processed',
    description: '监管报警',
    handler: '李四',
    handleTime: '2024-09-27 12:50:00',
    handleRemark: '定期检查，设备正常'
  }
]

// 方法
const getAlarmTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    'fire': 'danger',
    'fault': 'warning', 
    'supervision': 'info',
    'shield': ''
  }
  return colorMap[type] || ''
}

const getAlarmTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    'fire': '火灾报警',
    'fault': '故障报警',
    'supervision': '监管报警', 
    'shield': '屏蔽报警'
  }
  return textMap[type] || type
}

const getAlarmLevelColor = (level: string) => {
  const colorMap: Record<string, string> = {
    'high': 'danger',
    'medium': 'warning',
    'low': 'info'
  }
  return colorMap[level] || ''
}

const getAlarmLevelText = (level: string) => {
  const textMap: Record<string, string> = {
    'high': '高级',
    'medium': '中级',
    'low': '低级'
  }
  return textMap[level] || level
}

const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'pending': 'warning',
    'processed': 'success',
    'ignored': 'info'
  }
  return colorMap[status] || ''
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'pending': '待处理',
    'processed': '已处理',
    'ignored': '已忽略'
  }
  return textMap[status] || status
}

const getList = () => {
  loading.value = true
  
  // 模拟API调用
  setTimeout(() => {
    // 这里应该调用真实的API
    let filteredData = [...mockData]
    
    // 简单的筛选逻辑
    if (queryParams.location) {
      filteredData = filteredData.filter(item => item.location.includes(queryParams.location))
    }
    if (queryParams.status) {
      filteredData = filteredData.filter(item => item.alarmType === queryParams.status)
    }
    
    total.value = filteredData.length
    alarmList.value = filteredData.slice(
      (queryParams.pageNum - 1) * queryParams.pageSize,
      queryParams.pageNum * queryParams.pageSize
    )
    loading.value = false
  }, 500)
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
}

const handleSizeChange = (size: number) => {
  queryParams.pageSize = size
  getList()
}

const handleCurrentChange = (page: number) => {
  queryParams.pageNum = page
  getList()
}

const handleView = (row: any) => {
  currentAlarm.value = row
  detailVisible.value = true
}

const handleProcess = (row: any) => {
  currentAlarm.value = row
  processForm.handleType = ''
  processForm.handleRemark = ''
  processVisible.value = true
}

const submitProcess = () => {
  processFormRef.value?.validate((valid: boolean) => {
    if (valid) {
      processLoading.value = true
      
      // 模拟API调用
      setTimeout(() => {
        ElMessage.success('处理成功')
        processLoading.value = false
        processVisible.value = false
        
        // 更新数据
        if (currentAlarm.value) {
          const index = alarmList.value.findIndex(item => item.id === currentAlarm.value.id)
          if (index !== -1) {
            alarmList.value[index].status = 'processed'
            alarmList.value[index].handler = '当前用户'
            alarmList.value[index].handleTime = new Date().toLocaleString()
            alarmList.value[index].handleRemark = processForm.handleRemark
          }
        }
      }, 1000)
    }
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除告警记录"${row.eventCode}"吗？`,
    '系统提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 模拟删除操作
    const index = alarmList.value.findIndex(item => item.id === row.id)
    if (index !== -1) {
      alarmList.value.splice(index, 1)
      total.value--
      ElMessage.success('删除成功')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>@use '@/styles/dark-theme.scss';

.fire-alarm-container {
  padding: 20px;
  background: #0a1628;
  min-height: 100vh;

  .breadcrumb-container {
    margin-bottom: 20px;
  }

  .search-card {
    margin-bottom: 20px;

    :deep(.el-card__body) {
      padding-bottom: 10px;
    }
  }

  .table-card {
    .table-header {
      margin-bottom: 16px;
      
      h3 {
        margin: 0;
        font-size: 16px;
        color: #303133;
      }
    }
  }

  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }

  .dialog-footer {
    text-align: right;
  }
}
</style>
