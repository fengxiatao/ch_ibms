<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <h2>布控告警记录</h2>
        <p>人员布控告警记录查询和处理</p>
      </div>
      <div class="header-actions">
        <el-button type="danger" @click="handleBatchProcess" :disabled="selectedAlarms.length === 0">
          <el-icon><Operation /></el-icon>
          批量处理
        </el-button>
        <el-button type="primary" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出记录
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :model="searchForm" label-width="100px" :inline="true">
        <el-form-item label="告警时间">
          <el-date-picker
            v-model="searchForm.alarmTime"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="告警等级">
          <el-select v-model="searchForm.alarmLevel" placeholder="请选择" clearable>
            <el-option label="紧急" value="urgent" />
            <el-option label="重要" value="important" />
            <el-option label="一般" value="normal" />
            <el-option label="低级" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已处理" value="completed" />
            <el-option label="已忽略" value="ignored" />
          </el-select>
        </el-form-item>
        <el-form-item label="摄像头">
          <el-select v-model="searchForm.cameraId" placeholder="请选择摄像头" clearable>
            <el-option
              v-for="camera in cameraOptions"
              :key="camera.value"
              :label="camera.label"
              :value="camera.value"
            />
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

    <!-- 统计卡片 -->
    <div class="stats-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card urgent">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.urgentCount }}</div>
                <div class="stat-label">紧急告警</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card pending">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.pendingCount }}</div>
                <div class="stat-label">待处理</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card today">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.todayCount }}</div>
                <div class="stat-label">今日告警</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card processed">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.processedRate }}%</div>
                <div class="stat-label">处理率</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 告警记录表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="alarmList"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="alarmImage" label="告警图片" width="120">
          <template #default="{ row }">
            <el-image
              :src="row.alarmImage"
              :preview-src-list="[row.alarmImage]"
              fit="cover"
              style="width: 80px; height: 60px; border-radius: 4px;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="alarmTime" label="告警时间" width="160" show-overflow-tooltip />
        <el-table-column prop="alarmLevel" label="告警等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelColor(row.alarmLevel)" effect="dark">
              {{ getLevelText(row.alarmLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="personName" label="识别人员" width="120" show-overflow-tooltip />
        <el-table-column prop="controlName" label="布控名称" width="140" show-overflow-tooltip />
        <el-table-column prop="cameraName" label="摄像头" width="140" show-overflow-tooltip />
        <el-table-column prop="location" label="位置" width="140" show-overflow-tooltip />
        <el-table-column prop="confidence" label="相似度" width="100">
          <template #default="{ row }">
            <span>{{ row.confidence }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="processor" label="处理人" width="100" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button link type="success" @click="handleProcess(row)" :disabled="row.status === 'completed'">
              <el-icon><Check /></el-icon>
              处理
            </el-button>
            <el-button link type="warning" @click="handleIgnore(row)" :disabled="row.status !== 'pending'">
              <el-icon><Close /></el-icon>
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
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <!-- 详情弹框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="告警详情"
      width="900px"
      destroy-on-close
    >
      <div v-if="currentAlarm" class="alarm-detail">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-image">
              <h4>告警图片</h4>
              <el-image
                :src="currentAlarm.alarmImage"
                fit="contain"
                style="width: 100%; max-height: 300px; border: 1px solid #ddd; border-radius: 4px;"
              />
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-info">
              <h4>基本信息</h4>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="告警时间">
                  {{ currentAlarm.alarmTime }}
                </el-descriptions-item>
                <el-descriptions-item label="告警等级">
                  <el-tag :type="getLevelColor(currentAlarm.alarmLevel)" effect="dark">
                    {{ getLevelText(currentAlarm.alarmLevel) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="识别人员">
                  {{ currentAlarm.personName }}
                </el-descriptions-item>
                <el-descriptions-item label="布控名称">
                  {{ currentAlarm.controlName }}
                </el-descriptions-item>
                <el-descriptions-item label="摄像头">
                  {{ currentAlarm.cameraName }}
                </el-descriptions-item>
                <el-descriptions-item label="位置">
                  {{ currentAlarm.location }}
                </el-descriptions-item>
                <el-descriptions-item label="相似度">
                  {{ currentAlarm.confidence }}%
                </el-descriptions-item>
                <el-descriptions-item label="处理状态">
                  <el-tag :type="getStatusColor(currentAlarm.status)">
                    {{ getStatusText(currentAlarm.status) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="处理人" v-if="currentAlarm.processor">
                  {{ currentAlarm.processor }}
                </el-descriptions-item>
                <el-descriptions-item label="处理时间" v-if="currentAlarm.processTime">
                  {{ currentAlarm.processTime }}
                </el-descriptions-item>
                <el-descriptions-item label="处理备注" v-if="currentAlarm.processRemark">
                  {{ currentAlarm.processRemark }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-dialog>

    <!-- 处理弹框 -->
    <el-dialog
      v-model="processDialogVisible"
      title="处理告警"
      width="500px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="processFormRef" :model="processForm" :rules="processRules" label-width="100px">
        <el-form-item label="处理结果" prop="result">
          <el-radio-group v-model="processForm.result">
            <el-radio label="confirmed">确认告警</el-radio>
            <el-radio label="false_alarm">误报</el-radio>
            <el-radio label="resolved">已解决</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="处理措施" prop="actions">
          <el-checkbox-group v-model="processForm.actions">
            <el-checkbox label="现场核实">现场核实</el-checkbox>
            <el-checkbox label="联系安保">联系安保</el-checkbox>
            <el-checkbox label="通知相关人员">通知相关人员</el-checkbox>
            <el-checkbox label="记录存档">记录存档</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="处理备注" prop="remark">
          <el-input
            v-model="processForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入处理备注信息"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmProcess">确认处理</el-button>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Operation, Download, Search, Refresh, Warning, Clock, Calendar,
  CircleCheck, View, Check, Close
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const detailDialogVisible = ref(false)
const processDialogVisible = ref(false)
const currentAlarm = ref(null)
const selectedAlarms = ref([])

// 搜索表单
const searchForm = reactive({
  alarmTime: null,
  alarmLevel: '',
  status: '',
  cameraId: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 处理表单
const processForm = reactive({
  result: '',
  actions: [],
  remark: ''
})

const processRules = {
  result: [{ required: true, message: '请选择处理结果', trigger: 'change' }],
  remark: [{ required: true, message: '请输入处理备注', trigger: 'blur' }]
}

// 统计数据
const stats = reactive({
  urgentCount: 5,
  pendingCount: 23,
  todayCount: 47,
  processedRate: 89
})

// 选项数据
const cameraOptions = ref([
  { label: '大门入口摄像头01', value: 'camera_01' },
  { label: '大门入口摄像头02', value: 'camera_02' },
  { label: '停车场摄像头01', value: 'camera_03' },
  { label: '走廊摄像头01', value: 'camera_04' },
  { label: '电梯厅摄像头01', value: 'camera_05' }
])

// 告警记录数据
const alarmList = ref([
  {
    id: '1',
    alarmImage: 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg',
    alarmTime: '2024-01-20 14:30:25',
    alarmLevel: 'urgent',
    personName: '张某某',
    controlName: '黑名单人员监控',
    cameraName: '大门入口摄像头01',
    location: '主楼一层大厅',
    confidence: 96.8,
    status: 'pending',
    processor: '',
    processTime: '',
    processRemark: ''
  },
  {
    id: '2',
    alarmImage: 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg',
    alarmTime: '2024-01-20 14:25:12',
    alarmLevel: 'important',
    personName: '李某某',
    controlName: '重点人员布控',
    cameraName: '停车场摄像头01',
    location: '地下停车场A区',
    confidence: 89.2,
    status: 'processing',
    processor: '王安保',
    processTime: '',
    processRemark: ''
  },
  {
    id: '3',
    alarmImage: 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg',
    alarmTime: '2024-01-20 14:20:48',
    alarmLevel: 'normal',
    personName: '陌生人员',
    controlName: '陌生人检测',
    cameraName: '走廊摄像头01',
    location: '主楼二层走廊',
    confidence: 0,
    status: 'completed',
    processor: '李安保',
    processTime: '2024-01-20 14:35:20',
    processRemark: '已核实为正常访客，误报处理'
  }
])

const processFormRef = ref()

// 获取等级颜色
const getLevelColor = (level: string) => {
  const colors = {
    urgent: 'danger',
    important: 'warning',
    normal: 'info',
    low: 'success'
  }
  return colors[level] || 'info'
}

// 获取等级文本
const getLevelText = (level: string) => {
  const texts = {
    urgent: '紧急',
    important: '重要',
    normal: '一般',
    low: '低级'
  }
  return texts[level] || level
}

// 获取状态颜色
const getStatusColor = (status: string) => {
  const colors = {
    pending: 'danger',
    processing: 'warning',
    completed: 'success',
    ignored: 'info'
  }
  return colors[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const texts = {
    pending: '待处理',
    processing: '处理中',
    completed: '已处理',
    ignored: '已忽略'
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
    alarmTime: null,
    alarmLevel: '',
    status: '',
    cameraId: ''
  })
  handleSearch()
}

const handleSelectionChange = (selection: any[]) => {
  selectedAlarms.value = selection
}

const handleBatchProcess = () => {
  ElMessage.info('批量处理功能开发中...')
}

const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

const handleViewDetail = (row: any) => {
  currentAlarm.value = row
  detailDialogVisible.value = true
}

const handleProcess = (row: any) => {
  currentAlarm.value = row
  processForm.result = ''
  processForm.actions = []
  processForm.remark = ''
  processDialogVisible.value = true
}

const handleIgnore = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认忽略该告警吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const index = alarmList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      alarmList.value[index].status = 'ignored'
      alarmList.value[index].processor = '系统管理员'
      alarmList.value[index].processTime = new Date().toLocaleString('zh-CN')
      alarmList.value[index].processRemark = '用户主动忽略'
    }
    
    ElMessage.success('告警已忽略')
  } catch {
    // 用户取消忽略
  }
}

const handleConfirmProcess = async () => {
  if (!processFormRef.value) return
  
  try {
    await processFormRef.value.validate()
    
    const index = alarmList.value.findIndex(item => item.id === currentAlarm.value.id)
    if (index > -1) {
      alarmList.value[index].status = 'completed'
      alarmList.value[index].processor = '当前用户'
      alarmList.value[index].processTime = new Date().toLocaleString('zh-CN')
      alarmList.value[index].processRemark = processForm.remark
    }
    
    processDialogVisible.value = false
    ElMessage.success('告警处理完成')
  } catch (error) {
    console.error('表单验证失败:', error)
  }
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

<style scoped lang="scss">@use '@/styles/dark-theme.scss';

.app-container {
  padding: 20px;

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
        margin: 0 0 8px 0;
        color: #303133;
        font-size: 24px;
      }

      p {
        margin: 0;
        color: #909399;
        font-size: 14px;
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
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

    .el-input,
    .el-select,
    .el-date-picker {
      width: 200px;
    }
  }

  .stats-container {
    margin-bottom: 20px;

    .stat-card {
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }

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

      &.urgent .stat-icon {
        background: linear-gradient(45deg, #f56c6c, #ff8a8a);
      }

      &.pending .stat-icon {
        background: linear-gradient(45deg, #e6a23c, #f1c40f);
      }

      &.today .stat-icon {
        background: linear-gradient(45deg, #409eff, #66b1ff);
      }

      &.processed .stat-icon {
        background: linear-gradient(45deg, #67c23a, #85ce61);
      }
    }
  }

  .table-container {
    background: #1a1a1a;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .el-pagination {
      margin-top: 20px;
      text-align: right;
    }
  }
}

.alarm-detail {
  .detail-image,
  .detail-info {
    h4 {
      margin: 0 0 16px 0;
      color: #303133;
      font-size: 16px;
    }
  }

  .detail-info {
    .el-descriptions {
      :deep(.el-descriptions__label) {
        width: 120px;
      }
    }
  }
}
</style>






