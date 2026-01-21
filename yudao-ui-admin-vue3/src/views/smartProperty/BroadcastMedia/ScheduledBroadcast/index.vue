<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="scheduled-broadcast">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <Icon icon="ep:timer" :size="24" />
        <h1>定时播放</h1>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <Icon icon="ep:plus" />
          新增
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item>
          <el-select
            v-model="searchForm.taskType"
            placeholder="请选择任务类型"
            clearable
            style="width: 180px"
          >
            <el-option label="定时播放" value="scheduled" />
            <el-option label="循环播放" value="loop" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select
            v-model="searchForm.taskStatus"
            placeholder="请选择任务状态"
            clearable
            style="width: 180px"
          >
            <el-option label="运行中" value="running" />
            <el-option label="已暂停" value="paused" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select
            v-model="searchForm.syncStatus"
            placeholder="请选择同步状态"
            clearable
            style="width: 180px"
          >
            <el-option label="同步中" value="syncing" />
            <el-option label="同步成功" value="synced" />
            <el-option label="同步失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleMoreFilters">
            更多筛选
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 定时播放列表 -->
    <div class="broadcast-section">
      <h2>定时播放</h2>
      
      <el-table
        v-loading="loading"
        :data="broadcastList"
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="sequence" label="序号" width="80" align="center" />
        <el-table-column prop="taskCode" label="申请编号" width="180" />
        <el-table-column prop="taskName" label="任务名称" min-width="140" />
        <el-table-column prop="taskType" label="任务类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getTaskTypeColor(row.taskType)" size="small">
              {{ getTaskTypeText(row.taskType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executeTime" label="执行时间" width="180" />
        <el-table-column prop="openTime" label="开始时间" width="120" />
        <el-table-column prop="taskStatus" label="审核状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getTaskStatusColor(row.taskStatus)" size="small">
              {{ row.taskStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="syncStatus" label="同步状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getSyncStatusColor(row.syncStatus)" size="small">
              {{ row.syncStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handlePageSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 3步骤新增播放计划弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="新增播放计划"
      width="900px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div class="broadcast-wizard">
        <!-- 步骤指示器 -->
        <div class="wizard-header">
          <el-steps :active="currentStep" align-center>
            <el-step title="基础信息" description="配置基础信息" />
            <el-step title="播放时段" description="配置播放时段及媒体" />
            <el-step title="播放终端" description="选择播放终端设备" />
          </el-steps>
        </div>
        
        <!-- 步骤内容 -->
        <div class="wizard-content">
          <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
            <!-- 第1步：基础信息 -->
            <div v-if="currentStep === 0" class="step-content">
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="任务名称" prop="taskName">
                    <el-input
                      v-model="formData.taskName"
                      placeholder="请输入任务名称"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="优先级" prop="priority">
                    <el-input-number
                      v-model="formData.priority"
                      :min="1"
                      :max="10"
                      placeholder="优先级"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="播放模式" prop="playMode">
                    <el-select
                      v-model="formData.playMode"
                      placeholder="请选择播放模式"
                      style="width: 100%"
                    >
                      <el-option label="列表播放" value="list" />
                      <el-option label="随机播放" value="random" />
                      <el-option label="单曲循环" value="single" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="任务音量" prop="volume">
                    <el-input-number
                      v-model="formData.volume"
                      :min="0"
                      :max="100"
                      placeholder="音量"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="24">
                <el-col :span="24">
                  <el-form-item label="备注" prop="remark">
                    <el-input
                      v-model="formData.remark"
                      type="textarea"
                      :rows="3"
                      placeholder="请输入备注"
                      maxlength="300"
                      show-word-limit
                    />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>

            <!-- 第2步：播放时段 -->
            <div v-if="currentStep === 1" class="step-content">
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="重复日期段" prop="dateRange">
                    <el-date-picker
                      v-model="formData.dateRange"
                      type="daterange"
                      range-separator="-"
                      start-placeholder="开始日期"
                      end-placeholder="结束日期"
                      format="YYYY-MM-DD"
                      value-format="YYYY-MM-DD"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="重复类型" prop="repeatType">
                    <el-radio-group v-model="formData.repeatType">
                      <el-radio value="daily">每日</el-radio>
                      <el-radio value="weekly">每周</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item v-if="formData.repeatType === 'weekly'" label="重复日期段">
                <el-checkbox-group v-model="formData.weekDays">
                  <el-checkbox value="monday">星期一</el-checkbox>
                  <el-checkbox value="tuesday">星期二</el-checkbox>
                  <el-checkbox value="wednesday">星期三</el-checkbox>
                  <el-checkbox value="thursday">星期四</el-checkbox>
                  <el-checkbox value="friday">星期五</el-checkbox>
                  <el-checkbox value="saturday">星期六</el-checkbox>
                  <el-checkbox value="sunday">星期日</el-checkbox>
                </el-checkbox-group>
              </el-form-item>

              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="播放时间段" prop="timeRange">
                    <el-time-picker
                      v-model="formData.timeRange"
                      is-range
                      range-separator="-"
                      start-placeholder="开始时间"
                      end-placeholder="结束时间"
                      format="HH:mm:ss"
                      value-format="HH:mm:ss"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="播放节目" prop="playProgram">
                    <el-select
                      v-model="formData.playProgram"
                      placeholder="请选择"
                      style="width: 100%"
                    >
                      <el-option label="紧急通知" value="urgent" />
                      <el-option label="日常广播" value="daily" />
                      <el-option label="背景音乐" value="background" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>

            <!-- 第3步：播放终端 -->
            <div v-if="currentStep === 2" class="step-content">
              <el-form-item label="播放终端：">
                <div class="terminal-section">
                  <div class="terminal-tabs">
                    <el-tabs v-model="activeTerminalTab">
                      <el-tab-pane label="设备列表" name="device">
                        <div class="device-search">
                          <el-input
                            v-model="terminalSearchForm.deviceName"
                            placeholder="请输入设备名称"
                            clearable
                            style="width: 200px"
                          />
                          <el-input
                            v-model="terminalSearchForm.deviceCode"
                            placeholder="请输入设备编码"
                            clearable
                            style="width: 200px; margin-left: 12px"
                          />
                        </div>
                        
                        <div class="device-list">
                          <el-table
                            ref="terminalTableRef"
                            :data="availableTerminals"
                            style="width: 100%"
                            @selection-change="handleTerminalSelection"
                            max-height="300"
                          >
                            <el-table-column type="selection" width="55" />
                            <el-table-column prop="deviceName" label="设备名称" width="120" />
                            <el-table-column prop="deviceCode" label="设备编码" width="150" />
                            <el-table-column label="操作" width="80" align="center">
                              <template #default>
                                <el-button link type="primary" size="small">
                                  编辑
                                </el-button>
                              </template>
                            </el-table-column>
                          </el-table>
                        </div>
                      </el-tab-pane>
                      
                      <el-tab-pane label="空白" name="empty">
                        <div class="empty-terminal">
                          <Icon icon="ep:folder-opened" :size="80" color="#909399" />
                          <p>暂无数据</p>
                        </div>
                      </el-tab-pane>
                    </el-tabs>
                  </div>
                </div>
              </el-form-item>
            </div>
          </el-form>
        </div>
        
        <!-- 步骤操作按钮 -->
        <div class="wizard-footer">
          <el-button v-if="currentStep > 0" @click="prevStep">上一步</el-button>
          <el-button v-if="currentStep < 2" type="primary" @click="nextStep">下一步</el-button>
          <el-button v-if="currentStep === 2" type="primary" @click="handleSubmit">确认</el-button>
        </div>
      </div>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Icon } from '@/components/Icon'

interface ScheduledBroadcast {
  id: string
  sequence: number
  taskCode: string
  taskName: string
  taskType: string
  executeTime: string
  openTime: string
  taskStatus: string
  syncStatus: string
}

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const currentStep = ref(0)
const activeTerminalTab = ref('device')

const searchForm = reactive({
  taskType: '',
  taskStatus: '',
  syncStatus: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 1
})

const formData = reactive({
  taskName: '',
  priority: 1,
  playMode: '',
  volume: 50,
  remark: '',
  dateRange: [],
  repeatType: 'daily',
  weekDays: [],
  timeRange: [],
  playProgram: ''
})

const formRules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  priority: [{ required: true, message: '请输入优先级', trigger: 'blur' }],
  playMode: [{ required: true, message: '请选择播放模式', trigger: 'change' }]
}

const terminalSearchForm = reactive({
  deviceName: '',
  deviceCode: ''
})

const selectedTerminals = ref<any[]>([])

// 定时播放列表数据
const broadcastList = ref<ScheduledBroadcast[]>([
  {
    id: '1',
    sequence: 1,
    taskCode: '20SGB20241080001',
    taskName: '午间音乐',
    taskType: '每周',
    executeTime: '2024-01-08~2024-01-13',
    openTime: '01:00:00',
    taskStatus: '已通过',
    syncStatus: '同步中'
  }
])

// 可选终端设备
const availableTerminals = ref([
  { deviceName: '广播010', deviceCode: 'MT_GB_GBZD_0014' },
  { deviceName: '广播007', deviceCode: 'MT_GB_GBZD_0013' },
  { deviceName: '广播005', deviceCode: 'MT_GB_GBZD_0012' },
  { deviceName: '广播012', deviceCode: 'MT_GB_GBZD_0011' },
  { deviceName: '广播001', deviceCode: 'MT_GB_GBZD_0010' },
  { deviceName: '广播002', deviceCode: 'MT_GB_GBZD_0009' },
  { deviceName: '广播008', deviceCode: 'MT_GB_GBZD_0008' }
])

// 工具函数
const getTaskTypeColor = (type: string) => {
  return type === '每周' ? 'primary' : 'success'
}

const getTaskTypeText = (type: string) => {
  return type
}

const getTaskStatusColor = (status: string) => {
  switch (status) {
    case '已通过':
      return 'success'
    case '待审核':
      return 'warning'
    case '已驳回':
      return 'danger'
    default:
      return ''
  }
}

const getSyncStatusColor = (status: string) => {
  switch (status) {
    case '同步成功':
      return 'success'
    case '同步失败':
      return 'danger'
    case '同步中':
      return 'warning'
    default:
      return ''
  }
}

// 事件处理
const handleMoreFilters = () => {
  ElMessage.success('更多筛选功能开发中...')
}

const handleCreate = () => {
  resetFormData()
  currentStep.value = 0
  dialogVisible.value = true
}

const handleView = (row: ScheduledBroadcast) => {
  ElMessage.info('查看功能开发中...')
}

const nextStep = () => {
  if (currentStep.value < 2) {
    currentStep.value++
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const handleTerminalSelection = (selection: any[]) => {
  selectedTerminals.value = selection
}

const handleSubmit = async () => {
  const newBroadcast: ScheduledBroadcast = {
    id: `broadcast_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    sequence: broadcastList.value.length + 1,
    taskCode: `20SGB${new Date().getFullYear()}${String(new Date().getMonth() + 1).padStart(2, '0')}${String(new Date().getDate()).padStart(2, '0')}${String(broadcastList.value.length + 1).padStart(3, '0')}`,
    taskName: formData.taskName,
    taskType: formData.repeatType === 'daily' ? '每日' : '每周',
    executeTime: formData.dateRange.join('~'),
    openTime: Array.isArray(formData.timeRange) ? formData.timeRange[0] : '',
    taskStatus: '已通过',
    syncStatus: '同步中'
  }

  broadcastList.value.unshift(newBroadcast)
  pagination.total = broadcastList.value.length
  dialogVisible.value = false
  ElMessage.success('创建成功')
}

const resetFormData = () => {
  Object.assign(formData, {
    taskName: '',
    priority: 1,
    playMode: '',
    volume: 50,
    remark: '',
    dateRange: [],
    repeatType: 'daily',
    weekDays: [],
    timeRange: [],
    playProgram: ''
  })
  selectedTerminals.value = []
}

const handlePageSizeChange = (size: number) => {
  pagination.size = size
  loadData()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  loadData()
}

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

// 生命周期
onMounted(() => {
  pagination.total = broadcastList.value.length
  loadData()
})
</script>

<style scoped lang="scss">
.scheduled-broadcast {
  padding: 20px;
  background: linear-gradient(135deg, #0a1628 0%, #1e3a8a 50%, #0f172a 100%);
  min-height: auto;
  color: #ffffff;

  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
    padding: 16px 24px;
    background: rgba(15, 23, 42, 0.8);
    backdrop-filter: blur(15px);
    border-radius: 12px;
    border: 1px solid rgba(0, 212, 255, 0.1);

    .header-title {
      display: flex;
      align-items: center;
      gap: 12px;

      .el-icon {
        color: #00d4ff;
      }

      h1 {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: #00d4ff;
      }
    }
  }

  .search-section {
    margin-bottom: 20px;
    padding: 20px;
    background: rgba(15, 23, 42, 0.7);
    border-radius: 8px;
    border: 1px solid rgba(0, 212, 255, 0.1);
  }

  .broadcast-section {
    background: rgba(15, 23, 42, 0.7);
    border-radius: 8px;
    padding: 20px;
    border: 1px solid rgba(0, 212, 255, 0.1);

    h2 {
      margin: 0 0 20px 0;
      color: #00d4ff;
      font-size: 18px;
      font-weight: 600;
    }

    :deep(.el-table) {
      background: transparent;
      color: #e2e8f0;

      .el-table__header {
        background: rgba(0, 212, 255, 0.1);
        
        th {
          background: transparent;
          color: #00d4ff;
          border-bottom: 1px solid rgba(0, 212, 255, 0.2);
          font-weight: 600;
        }
      }

      .el-table__body {
        tr {
          background: transparent;
          
          &:hover {
            background: rgba(0, 212, 255, 0.05);
          }

          td {
            border-bottom: 1px solid rgba(71, 85, 105, 0.3);
          }
        }
      }
    }

    .pagination-section {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;

      :deep(.el-pagination) {
        .el-pager li, .btn-prev, .btn-next {
          background: rgba(15, 23, 42, 0.7);
          color: #e2e8f0;
          border: 1px solid rgba(0, 212, 255, 0.2);

          &:hover {
            color: #00d4ff;
          }

          &.is-active {
            background: #00d4ff;
            color: #0f172a;
          }
        }

        .el-pagination__total,
        .el-pagination__jump {
          color: #e2e8f0;
        }
      }
    }
  }

  .broadcast-wizard {
    .wizard-header {
      margin-bottom: 30px;
      
      :deep(.el-steps) {
        .el-step__title {
          color: #374151;
        }
        
        .el-step__description {
          color: #6b7280;
        }
      }
    }

    .wizard-content {
      min-height: 400px;
      margin-bottom: 20px;

      .step-content {
        :deep(.el-form-item__label) {
          color: #374151;
        }
      }

      .terminal-section {
        width: 100%;

        .device-search {
          margin-bottom: 16px;
        }

        .device-list {
          :deep(.el-table) {
            border: 1px solid #e5e7eb;
            border-radius: 6px;
          }
        }

        .empty-terminal {
          text-align: center;
          padding: 40px;
          color: #909399;
        }
      }
    }

    .wizard-footer {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      padding-top: 20px;
      border-top: 1px solid #e5e7eb;
    }
  }
}

:deep(.el-dialog) {
  .el-dialog__header {
    background: #f8fafc;
    border-bottom: 1px solid #e5e7eb;
  }

  .el-dialog__title {
    color: #374151;
    font-weight: 600;
  }

  .el-dialog__body {
    padding: 24px;
  }

  .el-dialog__footer {
    background: #f8fafc;
    border-top: 1px solid #e5e7eb;
  }
}
</style>

