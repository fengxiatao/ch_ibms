<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <h2>人员识别</h2>
        <p>实时人员识别和智能分析</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleStartRecognition">
          <el-icon><VideoPlay /></el-icon>
          启动识别
        </el-button>
        <el-button type="warning" @click="handleStopRecognition">
          <el-icon><VideoPause /></el-icon>
          停止识别
        </el-button>
      </div>
    </div>

    <!-- 实时监控区域 -->
    <div class="monitor-container">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card class="video-card">
            <template #header>
              <div class="card-header">
                <span>实时监控画面</span>
                <el-select v-model="selectedCamera" placeholder="选择摄像头" style="width: 200px;">
                  <el-option
                    v-for="camera in cameraOptions"
                    :key="camera.value"
                    :label="camera.label"
                    :value="camera.value"
                  />
                </el-select>
              </div>
            </template>
            <div class="video-container">
              <div class="video-placeholder">
                <el-icon size="120"><VideoCamera /></el-icon>
                <p>实时视频监控画面</p>
                <p class="video-status" :class="{ active: isRecognizing }">
                  {{ isRecognizing ? '识别中...' : '暂停识别' }}
                </p>
              </div>
              <!-- 识别框标注 -->
              <div v-if="isRecognizing" class="recognition-boxes">
                <div
                  v-for="detection in currentDetections"
                  :key="detection.id"
                  class="detection-box"
                  :style="detection.style"
                >
                  <div class="detection-label">
                    {{ detection.name }}
                    <span class="confidence">({{ detection.confidence }}%)</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card class="detection-card">
            <template #header>
              <span>识别结果</span>
            </template>
            <div class="detection-list">
              <div
                v-for="detection in recentDetections"
                :key="detection.id"
                class="detection-item"
              >
                <el-avatar :src="detection.avatar" :size="50">
                  {{ detection.name.charAt(0) }}
                </el-avatar>
                <div class="detection-info">
                  <div class="person-name">{{ detection.name }}</div>
                  <div class="person-details">
                    <span class="confidence">{{ detection.confidence }}%</span>
                    <span class="time">{{ detection.time }}</span>
                  </div>
                  <div class="person-status">
                    <el-tag :type="getStatusTagType(detection.status)" size="small">
                      {{ detection.status }}
                    </el-tag>
                  </div>
                </div>
              </div>
              <div v-if="recentDetections.length === 0" class="no-detection">
                <el-icon><Warning /></el-icon>
                <p>暂无识别结果</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 统计信息 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <el-statistic title="今日识别人次" :value="stats.todayRecognition" />
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon>
              <span>12.5%</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <el-statistic title="识别准确率" :value="stats.accuracy" suffix="%" />
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon>
              <span>2.3%</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <el-statistic title="陌生人数" :value="stats.strangerCount" />
            <div class="stat-trend down">
              <el-icon><CaretBottom /></el-icon>
              <span>5.1%</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <el-statistic title="活跃摄像头" :value="stats.activeCameras" />
            <div class="stat-trend stable">
              <el-icon><Minus /></el-icon>
              <span>0%</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 识别设置 -->
    <div class="settings-container">
      <el-card>
        <template #header>
          <span>识别设置</span>
        </template>
        <el-form :model="recognitionSettings" label-width="120px" :inline="true">
          <el-form-item label="识别阈值">
            <el-slider
              v-model="recognitionSettings.threshold"
              :min="0"
              :max="100"
              :step="1"
              show-input
              style="width: 200px;"
            />
          </el-form-item>
          <el-form-item label="检测间隔">
            <el-input-number
              v-model="recognitionSettings.interval"
              :min="100"
              :max="5000"
              :step="100"
              controls-position="right"
            />
            <span style="margin-left: 8px;">毫秒</span>
          </el-form-item>
          <el-form-item label="启用人员库">
            <el-select v-model="recognitionSettings.enabledLibraries" multiple placeholder="选择人员库">
              <el-option
                v-for="library in libraryOptions"
                :key="library.value"
                :label="library.label"
                :value="library.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="陌生人警告">
            <el-switch v-model="recognitionSettings.strangerAlert" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSaveSettings">
              <el-icon><Check /></el-icon>
              保存设置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 识别历史记录 -->
    <div class="history-container">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>识别历史</span>
            <div class="header-actions">
              <el-date-picker
                v-model="historyDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                size="small"
              />
              <el-button type="primary" size="small" @click="loadHistory">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
            </div>
          </div>
        </template>
        
        <el-table
          :data="historyList"
          stripe
          border
          style="width: 100%"
        >
          <el-table-column prop="avatar" label="头像" width="80">
            <template #default="{ row }">
              <el-avatar :src="row.avatar" :size="40">{{ row.name.charAt(0) }}</el-avatar>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="cameraName" label="摄像头" width="140" show-overflow-tooltip />
          <el-table-column prop="recognitionTime" label="识别时间" width="160" />
          <el-table-column prop="confidence" label="置信度" width="100">
            <template #default="{ row }">
              <el-progress
                :percentage="row.confidence"
                :color="getConfidenceColor(row.confidence)"
                :show-text="true"
                :stroke-width="6"
              />
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)" size="small">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="location" label="位置" min-width="120" show-overflow-tooltip />
        </el-table>

        <el-pagination
          v-model:current-page="historyPagination.page"
          v-model:page-size="historyPagination.size"
          :total="historyPagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          style="margin-top: 20px; text-align: right;"
          @size-change="loadHistory"
          @current-change="loadHistory"
        />
      </el-card>
    </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  VideoPlay, VideoPause, VideoCamera, Warning, CaretTop, CaretBottom,
  Minus, Check, Search
} from '@element-plus/icons-vue'

// 响应式数据
const isRecognizing = ref(false)
const selectedCamera = ref('camera_01')
const historyDateRange = ref([])
let recognitionInterval: number | null = null

// 识别设置
const recognitionSettings = reactive({
  threshold: 85,
  interval: 1000,
  enabledLibraries: ['employee', 'visitor'],
  strangerAlert: true
})

// 统计数据
const stats = reactive({
  todayRecognition: 1247,
  accuracy: 96.8,
  strangerCount: 23,
  activeCameras: 8
})

// 分页信息
const historyPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 当前检测结果
const currentDetections = ref([
  {
    id: '1',
    name: '张三',
    confidence: 96.8,
    style: {
      left: '20%',
      top: '30%',
      width: '15%',
      height: '25%'
    }
  },
  {
    id: '2',
    name: '李四',
    confidence: 89.2,
    style: {
      left: '60%',
      top: '40%',
      width: '12%',
      height: '20%'
    }
  }
])

// 最近识别结果
const recentDetections = ref([
  {
    id: '1',
    name: '张三',
    confidence: 96.8,
    time: '14:30:25',
    status: '正常员工',
    avatar: ''
  },
  {
    id: '2',
    name: '李四',
    confidence: 89.2,
    time: '14:29:18',
    status: '访客',
    avatar: ''
  },
  {
    id: '3',
    name: '陌生人',
    confidence: 0,
    time: '14:28:42',
    status: '陌生人',
    avatar: ''
  }
])

// 历史记录
const historyList = ref([
  {
    id: '1',
    name: '张三',
    cameraName: '大门入口摄像头01',
    recognitionTime: '2024-01-20 14:30:25',
    confidence: 96.8,
    status: '正常员工',
    location: '主楼一层大厅',
    avatar: ''
  },
  {
    id: '2',
    name: '李四',
    cameraName: '停车场摄像头01',
    recognitionTime: '2024-01-20 14:29:18',
    confidence: 89.2,
    status: '访客',
    location: '地下停车场A区',
    avatar: ''
  },
  {
    id: '3',
    name: '王五',
    cameraName: '走廊摄像头01',
    recognitionTime: '2024-01-20 14:28:42',
    confidence: 93.5,
    status: '正常员工',
    location: '主楼二层走廊',
    avatar: ''
  }
])

// 选项数据
const cameraOptions = ref([
  { label: '大门入口摄像头01', value: 'camera_01' },
  { label: '大门入口摄像头02', value: 'camera_02' },
  { label: '停车场摄像头01', value: 'camera_03' },
  { label: '走廊摄像头01', value: 'camera_04' },
  { label: '电梯厅摄像头01', value: 'camera_05' }
])

const libraryOptions = ref([
  { label: '员工人员库', value: 'employee' },
  { label: '访客人员库', value: 'visitor' },
  { label: '黑名单人员库', value: 'blacklist' },
  { label: '白名单人员库', value: 'whitelist' }
])

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  const types = {
    '正常员工': 'success',
    '访客': 'warning',
    '陌生人': 'danger',
    '黑名单': 'danger',
    '白名单': 'success'
  }
  return types[status] || 'info'
}

// 获取置信度颜色
const getConfidenceColor = (confidence: number) => {
  if (confidence >= 90) return '#67c23a'
  if (confidence >= 70) return '#e6a23c'
  return '#f56c6c'
}

// 事件处理
const handleStartRecognition = () => {
  if (isRecognizing.value) {
    ElMessage.warning('识别已在运行中')
    return
  }
  
  isRecognizing.value = true
  
  // 模拟识别过程
  recognitionInterval = setInterval(() => {
    // 模拟更新识别结果
    updateRecognitionResults()
  }, recognitionSettings.interval)
  
  ElMessage.success('识别已启动')
}

const handleStopRecognition = () => {
  if (!isRecognizing.value) {
    ElMessage.warning('识别未在运行')
    return
  }
  
  isRecognizing.value = false
  
  if (recognitionInterval) {
    clearInterval(recognitionInterval)
    recognitionInterval = null
  }
  
  ElMessage.success('识别已停止')
}

const handleSaveSettings = () => {
  ElMessage.success('设置已保存')
  
  // 如果识别正在运行，重新启动以应用新设置
  if (isRecognizing.value && recognitionInterval) {
    clearInterval(recognitionInterval)
    recognitionInterval = setInterval(() => {
      updateRecognitionResults()
    }, recognitionSettings.interval)
  }
}

const loadHistory = () => {
  // 模拟加载历史记录
  historyPagination.total = historyList.value.length
}

const updateRecognitionResults = () => {
  // 模拟更新实时识别结果
  const newDetection = {
    id: `detection_${Date.now()}`,
    name: `人员${Math.floor(Math.random() * 100)}`,
    confidence: Math.floor(Math.random() * 40) + 60,
    time: new Date().toLocaleTimeString('zh-CN'),
    status: ['正常员工', '访客', '陌生人'][Math.floor(Math.random() * 3)],
    avatar: ''
  }
  
  recentDetections.value.unshift(newDetection)
  if (recentDetections.value.length > 10) {
    recentDetections.value.pop()
  }
  
  // 更新统计数据
  stats.todayRecognition++
}

onMounted(() => {
  loadHistory()
})

onUnmounted(() => {
  if (recognitionInterval) {
    clearInterval(recognitionInterval)
  }
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

  .monitor-container {
    margin-bottom: 20px;

    .video-card {
      height: 400px;

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .video-container {
        position: relative;
        height: 320px;
        background: #f5f7fa;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;

        .video-placeholder {
          text-align: center;
          color: #909399;

          .el-icon {
            margin-bottom: 16px;
          }

          p {
            margin: 8px 0;
            font-size: 16px;
          }

          .video-status {
            font-size: 14px;
            font-weight: bold;

            &.active {
              color: #67c23a;
            }
          }
        }

        .recognition-boxes {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;

          .detection-box {
            position: absolute;
            border: 2px solid #67c23a;
            border-radius: 4px;

            .detection-label {
              position: absolute;
              top: -30px;
              left: 0;
              background: #67c23a;
              color: white;
              padding: 4px 8px;
              border-radius: 4px;
              font-size: 12px;
              white-space: nowrap;

              .confidence {
                opacity: 0.8;
              }
            }
          }
        }
      }
    }

    .detection-card {
      height: 400px;

      .detection-list {
        height: 320px;
        overflow-y: auto;

        .detection-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 12px;
          border-bottom: 1px solid #f0f0f0;

          &:last-child {
            border-bottom: none;
          }

          .detection-info {
            flex: 1;

            .person-name {
              font-weight: bold;
              color: #303133;
              margin-bottom: 4px;
            }

            .person-details {
              display: flex;
              gap: 12px;
              font-size: 12px;
              color: #909399;
              margin-bottom: 4px;

              .confidence {
                color: #67c23a;
                font-weight: bold;
              }
            }
          }
        }

        .no-detection {
          text-align: center;
          color: #909399;
          padding: 40px 0;

          .el-icon {
            font-size: 48px;
            margin-bottom: 16px;
          }
        }
      }
    }
  }

  .stats-section {
    margin-bottom: 20px;

    .stat-card {
      text-align: center;

      .stat-trend {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 4px;
        margin-top: 8px;
        font-size: 12px;

        &.up {
          color: #67c23a;
        }

        &.down {
          color: #f56c6c;
        }

        &.stable {
          color: #909399;
        }
      }
    }
  }

  .settings-container,
  .history-container {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-actions {
        display: flex;
        gap: 12px;
        align-items: center;
      }
    }
  }
}
</style>






