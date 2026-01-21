<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧建筑</el-breadcrumb-item>
        <el-breadcrumb-item>变配电</el-breadcrumb-item>
        <el-breadcrumb-item>电力告警记录</el-breadcrumb-item>
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
            <el-option label="变压器" value="transformer" />
            <el-option label="UPS电源" value="ups" />
            <el-option label="油机" value="generator" />
            <el-option label="配电柜" value="switchgear" />
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
        </el-form-item>
      </el-form>
    </div>

    <!-- 告警列表 -->
    <div class="alarm-list">
      <el-card class="list-card">
        <template #header>
          <div class="card-header">
            <span>电力告警记录</span>
            <div class="header-actions">
              <el-switch
                v-model="autoRefresh"
                active-text="自动刷新"
                inactive-text="手动刷新"
              />
            </div>
          </div>
        </template>

        <el-table :data="alarmList" stripe border style="width: 100%">
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
          <el-table-column label="操作" width="180" fixed="right">
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
    <el-dialog v-model="detailDialogVisible" title="电力告警详情" width="60%" draggable>
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
      </div>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Warning, Bell, Clock, CircleCheck, Search, Refresh, Download,
  View, Check, Monitor
} from '@element-plus/icons-vue'

// 响应式数据
const autoRefresh = ref(false)
const detailDialogVisible = ref(false)
const currentAlarm = ref(null)

// 告警统计
const alarmStats = reactive({
  total: 89,
  urgent: 5,
  pending: 12,
  resolved: 72
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

// 告警列表数据
const alarmList = ref([
  {
    id: 1,
    alarmTime: '2024-01-22 14:25:30',
    level: 'urgent',
    deviceName: '1号主变压器',
    deviceCode: 'T-001',
    deviceType: 'transformer',
    location: '高压配电室',
    alarmType: '过载告警',
    alarmContent: '变压器负载超过额定容量90%，当前负载95%，存在过载风险',
    currentValue: '95%',
    thresholdValue: '≤90%',
    status: 'pending',
    duration: '15分钟',
    processor: '',
    impact: '严重',
    suggestion: '立即减少负载或启用备用变压器，避免设备损坏'
  },
  {
    id: 2,
    alarmTime: '2024-01-22 13:45:15',
    level: 'important',
    deviceName: 'UPS-001',
    deviceCode: 'UPS-001',
    deviceType: 'ups',
    location: '低压配电室',
    alarmType: '电池电压低',
    alarmContent: 'UPS电池组电压低于设定值，当前电压216V，可能影响停电时的供电时间',
    currentValue: '216V',
    thresholdValue: '≥220V',
    status: 'processing',
    duration: '45分钟',
    processor: '李技术员',
    impact: '中等',
    suggestion: '检查电池组状态，必要时更换老化电池'
  },
  {
    id: 3,
    alarmTime: '2024-01-22 12:30:45',
    level: 'normal',
    deviceName: '应急发电机',
    deviceCode: 'GEN-001',
    deviceType: 'generator',
    location: '发电机房',
    alarmType: '油位偏低',
    alarmContent: '发电机燃油箱油位低于正常值，当前油位65%，建议及时补充燃油',
    currentValue: '65%',
    thresholdValue: '≥70%',
    status: 'resolved',
    duration: '2小时',
    processor: '王师傅',
    impact: '轻微',
    suggestion: '及时补充燃油，确保应急供电能力'
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
    transformer: '变压器',
    ups: 'UPS电源',
    generator: '发电机',
    switchgear: '配电柜'
  }
  return texts[type] || type
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

const handleView = (row: any) => {
  currentAlarm.value = row
  detailDialogVisible.value = true
}

const handleProcess = (row: any) => {
  ElMessage.info(`处理告警: ${row.deviceName}`)
}

const handleViewDevice = (row: any) => {
  ElMessage.info(`查看设备: ${row.deviceName}`)
}

const loadData = () => {
  console.log('Loading power alarm data...', searchForm)
  pagination.total = alarmList.value.length
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
    .el-descriptions {
      margin-top: 0;
    }
  }
}
</style>






