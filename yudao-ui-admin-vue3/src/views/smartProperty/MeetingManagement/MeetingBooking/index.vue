<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="meeting-booking">
    <!-- 统计卡片区域 -->
    <div class="stats-section">
      <div class="stats-card">
        <div class="card-header">
          <h3>7日新增申请数量统计</h3>
        </div>
        <div class="chart-container">
          <!-- 这里可以集成ECharts图表 -->
          <div class="chart-placeholder">
            <div class="chart-content">
              <div class="chart-item">
                <div class="dot" style="background: #1890ff"></div>
                <span>申请数量</span>
              </div>
              <div class="chart-bars">
                <div class="bar" v-for="(value, index) in weeklyData" :key="index" :style="{ height: `${value * 2}px` }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="stats-card">
        <div class="card-header">
          <h3>审核状态统计</h3>
        </div>
        <div class="chart-container">
          <div class="pie-chart">
            <div class="pie-center">
              <div class="pie-number">0</div>
              <div class="pie-label">申请总数(个)</div>
            </div>
            <div class="pie-legend">
              <div class="legend-item">
                <div class="legend-color" style="background: #1890ff"></div>
                <span>待审核</span>
              </div>
              <div class="legend-item">
                <div class="legend-color" style="background: #52c41a"></div>
                <span>已通过</span>
              </div>
              <div class="legend-item">
                <div class="legend-color" style="background: #faad14"></div>
                <span>已驳回</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="stats-card">
        <div class="card-header">
          <h3>24小时审批数统计</h3>
        </div>
        <div class="chart-container">
          <div class="gauge-chart">
            <div class="gauge-center">
              <div class="gauge-number">0</div>
              <div class="gauge-label">24小时审批量</div>
            </div>
          </div>
        </div>
      </div>

      <div class="stats-card">
        <div class="card-header">
          <h3>平均审批时间统计</h3>
        </div>
        <div class="chart-container">
          <div class="time-chart">
            <div class="time-visualization">
              <!-- 可视化时间统计图 -->
              <div class="time-dots">
                <div class="dot active"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选和快速预约区域 -->
    <div class="filter-section">
      <div class="filter-left">
        <el-select v-model="filterForm.meetingRoom" placeholder="请选择会议室" clearable style="width: 180px">
          <el-option label="大会议室" value="large" />
          <el-option label="小会议室" value="small" />
        </el-select>
        <el-select v-model="filterForm.meetingStatus" placeholder="请选择会议状态" clearable style="width: 180px">
          <el-option label="已确认" value="confirmed" />
          <el-option label="待审核" value="pending" />
          <el-option label="已取消" value="cancelled" />
        </el-select>
        <el-select v-model="filterForm.approvalStatus" placeholder="请选择审核状态" clearable style="width: 180px">
          <el-option label="已通过" value="approved" />
          <el-option label="待审核" value="pending" />
          <el-option label="已驳回" value="rejected" />
        </el-select>
      </div>
      <div class="filter-right">
        <el-button type="primary" @click="showQuickBooking">
          更多预约
        </el-button>
      </div>
    </div>

    <!-- 会议预约列表 -->
    <div class="meeting-section">
      <div class="section-header">
        <h2>会议预约</h2>
        <el-button type="primary" @click="handleCreate">
          <Icon icon="ep:plus" />
          新增
        </el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="meetingList"
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="sequence" label="序号" width="80" align="center" />
        <el-table-column prop="meetingCode" label="申请编号" width="180" />
        <el-table-column prop="meetingTopic" label="会议主题" min-width="140" />
        <el-table-column prop="meetingRoom" label="会议室" width="120" />
        <el-table-column prop="organizer" label="主持人" width="100" />
        <el-table-column prop="attendees" label="参会人数" width="100" align="center" />
        <el-table-column prop="meetingTime" label="会议时间" width="180" />
        <el-table-column prop="meetingStatus" label="会议状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getMeetingStatusType(row.meetingStatus)" size="small">
              {{ row.meetingStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approvalStatus" label="审核状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getApprovalStatusType(row.approvalStatus)" size="small">
              {{ row.approvalStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              查看
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="primary" @click="handleExport(row)">
              导出登记表
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

    <!-- 快速预约弹窗 -->
    <el-dialog
      v-model="quickBookingVisible"
      title="快速预约"
      width="600px"
      destroy-on-close
    >
      <div class="quick-booking-form">
        <el-form :model="quickForm" :rules="quickRules" label-width="100px">
          <el-form-item label="申请编号：">
            <el-input v-model="quickForm.applicationCode" placeholder="请输入申请编号" />
          </el-form-item>
          <el-form-item label="会议主题：">
            <el-input v-model="quickForm.meetingTopic" placeholder="请输入会议主题" />
          </el-form-item>
          <el-form-item label="主持人：">
            <el-input v-model="quickForm.organizer" placeholder="请输入主持人" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="quickBookingVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmQuickBooking">查询</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 新增会议弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'add' ? '新增会议' : dialogMode === 'edit' ? '编辑会议' : '查看会议'"
      width="900px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div class="meeting-form">
        <h3>基本信息</h3>
        <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="会议主题" prop="meetingTopic">
                <el-input
                  v-model="formData.meetingTopic"
                  placeholder="请输入会议主题"
                  :disabled="dialogMode === 'view'"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="主持人" prop="organizer">
                <el-select
                  v-model="formData.organizer"
                  placeholder="请选择主持人"
                  style="width: 100%"
                  :disabled="dialogMode === 'view'"
                >
                  <el-option label="伍建伟(18022311370)" value="伍建伟(18022311370)" />
                  <el-option label="李文文" value="李文文" />
                  <el-option label="张三" value="张三" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="参会人员" prop="attendeeType">
                <el-radio-group v-model="formData.attendeeType" :disabled="dialogMode === 'view'">
                  <el-radio value="specified">指定人员</el-radio>
                  <el-radio value="department">指定部门</el-radio>
                  <el-radio value="group">指定分组</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="会议室" prop="meetingRoom">
                <el-select
                  v-model="formData.meetingRoom"
                  placeholder="请选择会议室"
                  style="width: 100%"
                  :disabled="dialogMode === 'view'"
                >
                  <el-option label="大会议室" value="大会议室" />
                  <el-option label="小会议室" value="小会议室" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :span="24">
              <el-form-item label="会议说明" prop="description">
                <el-input
                  v-model="formData.description"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入会议说明"
                  maxlength="300"
                  show-word-limit
                  :disabled="dialogMode === 'view'"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="预期时间" prop="expectedDuration">
                <div class="time-inputs">
                  <el-input-number
                    v-model="formData.expectedHours"
                    :min="0"
                    :max="23"
                    placeholder="小时"
                    :disabled="dialogMode === 'view'"
                  />
                  <span>小时</span>
                  <el-input-number
                    v-model="formData.expectedMinutes"
                    :min="0"
                    :max="59"
                    placeholder="分钟"
                    :disabled="dialogMode === 'view'"
                  />
                  <span>分钟</span>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="是否录制">
                <el-switch v-model="formData.isRecorded" :disabled="dialogMode === 'view'" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="会议方式" prop="meetingType">
                <el-select
                  v-model="formData.meetingType"
                  placeholder="请选择会议方式"
                  style="width: 100%"
                  :disabled="dialogMode === 'view'"
                >
                  <el-option label="线下会议" value="offline" />
                  <el-option label="线上会议" value="online" />
                  <el-option label="混合会议" value="hybrid" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="会议时间" prop="meetingDate">
                <el-date-picker
                  v-model="formData.meetingDate"
                  type="date"
                  placeholder="请选择会议时间"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                  :disabled="dialogMode === 'view'"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 会议时间网格 -->
          <el-form-item label="会议时间：">
            <div class="time-grid">
              <div class="time-grid-header">
                <div
                  v-for="hour in 24"
                  :key="hour - 1"
                  class="hour-label"
                >
                  {{ String(hour - 1).padStart(2, '0') }}:00
                </div>
              </div>
              <div class="time-grid-row">
                <div class="day-label">{{ formData.meetingRoom || '大会议室' }}</div>
                <div
                  v-for="hour in 24"
                  :key="hour - 1"
                  class="time-cell"
                  :class="{ 
                    'selected': isTimeSelected(hour - 1),
                    'disabled': dialogMode === 'view'
                  }"
                  @click="toggleTimeSlot(hour - 1)"
                >
                </div>
              </div>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">
            {{ dialogMode === 'view' ? '关闭' : '取消' }}
          </el-button>
          <el-button v-if="dialogMode !== 'view'" type="primary" @click="handleSubmit">
            确认
          </el-button>
        </div>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

interface Meeting {
  id: string
  sequence: number
  meetingCode: string
  meetingTopic: string
  meetingRoom: string
  organizer: string
  attendees: number
  meetingTime: string
  meetingStatus: string
  approvalStatus: string
}

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const quickBookingVisible = ref(false)
const dialogMode = ref<'add' | 'edit' | 'view'>('add')

const weeklyData = ref([20, 35, 15, 45, 30, 25, 40])

const filterForm = reactive({
  meetingRoom: '',
  meetingStatus: '',
  approvalStatus: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 15
})

const formData = reactive({
  meetingTopic: '',
  organizer: '',
  attendeeType: 'specified',
  meetingRoom: '',
  description: '',
  expectedHours: 0,
  expectedMinutes: 0,
  isRecorded: false,
  meetingType: '',
  meetingDate: '',
  timeSlots: [] as boolean[]
})

const formRules = {
  meetingTopic: [{ required: true, message: '请输入会议主题', trigger: 'blur' }],
  organizer: [{ required: true, message: '请选择主持人', trigger: 'change' }],
  meetingRoom: [{ required: true, message: '请选择会议室', trigger: 'change' }],
  meetingDate: [{ required: true, message: '请选择会议时间', trigger: 'change' }]
}

const quickForm = reactive({
  applicationCode: '',
  meetingTopic: '',
  organizer: ''
})

const quickRules = {
  applicationCode: [{ required: true, message: '请输入申请编号', trigger: 'blur' }]
}

// 时间调度表：24小时的布尔数组
const timeSlots = ref<boolean[]>(Array(24).fill(false))

// 会议列表数据
const meetingList = ref<Meeting[]>([
  {
    id: '1',
    sequence: 1,
    meetingCode: '20SHY20250910001',
    meetingTopic: '年中总结会议',
    meetingRoom: '大会议室',
    organizer: '李文文',
    attendees: 2,
    meetingTime: '2025-09-10 15:00~16:00',
    meetingStatus: '已确认',
    approvalStatus: '已通过'
  },
  {
    id: '2',
    sequence: 2,
    meetingCode: '20SHY20250825004',
    meetingTopic: '22',
    meetingRoom: '大会议室',
    organizer: '欧阳冰',
    attendees: 2,
    meetingTime: '2025-08-25 15:00~16:00',
    meetingStatus: '审核中',
    approvalStatus: '-'
  },
  {
    id: '3',
    sequence: 3,
    meetingCode: '20SHY20250825003',
    meetingTopic: '包含13-14',
    meetingRoom: '大会议室',
    organizer: '-',
    attendees: 3,
    meetingTime: '2025-08-25 12:30~14:30',
    meetingStatus: '审核中',
    approvalStatus: '-'
  },
  {
    id: '4',
    sequence: 4,
    meetingCode: '20SHY20250825002',
    meetingTopic: '13-14-2',
    meetingRoom: '大会议室',
    organizer: '-',
    attendees: 4,
    meetingTime: '2025-08-25 13:00~14:00',
    meetingStatus: '审核中',
    approvalStatus: '-'
  },
  {
    id: '5',
    sequence: 5,
    meetingCode: '20SHY20250825001',
    meetingTopic: '13-14-1',
    meetingRoom: '大会议室',
    organizer: '李文文',
    attendees: 2,
    meetingTime: '2025-08-25 13:00~14:00',
    meetingStatus: '已确认',
    approvalStatus: '已通过'
  },
  {
    id: '6',
    sequence: 6,
    meetingCode: '20SHY20250814001',
    meetingTopic: '11',
    meetingRoom: '大会议室',
    organizer: '李文文',
    attendees: 2,
    meetingTime: '2025-08-14 14:00~17:00',
    meetingStatus: '已确认',
    approvalStatus: '已通过'
  }
])

// 工具函数
const getMeetingStatusType = (status: string) => {
  switch (status) {
    case '已确认':
      return 'success'
    case '审核中':
      return 'warning'
    case '已取消':
      return 'danger'
    default:
      return ''
  }
}

const getApprovalStatusType = (status: string) => {
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

const isTimeSelected = (hourIndex: number) => {
  return timeSlots.value[hourIndex]
}

const toggleTimeSlot = (hourIndex: number) => {
  if (dialogMode.value === 'view') return
  timeSlots.value[hourIndex] = !timeSlots.value[hourIndex]
}

// 事件处理
const showQuickBooking = () => {
  quickBookingVisible.value = true
}

const confirmQuickBooking = () => {
  ElMessage.success('查询功能开发中...')
  quickBookingVisible.value = false
}

const handleCreate = () => {
  dialogMode.value = 'add'
  resetFormData()
  dialogVisible.value = true
}

const handleEdit = (row: Meeting) => {
  dialogMode.value = 'edit'
  // 填充表单数据
  Object.assign(formData, {
    meetingTopic: row.meetingTopic,
    organizer: row.organizer,
    meetingRoom: row.meetingRoom,
    description: '',
    expectedHours: 1,
    expectedMinutes: 0,
    isRecorded: false,
    meetingType: 'offline',
    meetingDate: '2025-09-19'
  })
  dialogVisible.value = true
}

const handleView = (row: Meeting) => {
  dialogMode.value = 'view'
  Object.assign(formData, {
    meetingTopic: row.meetingTopic,
    organizer: row.organizer,
    meetingRoom: row.meetingRoom,
    description: '',
    expectedHours: 1,
    expectedMinutes: 0,
    isRecorded: false,
    meetingType: 'offline',
    meetingDate: '2025-09-19'
  })
  dialogVisible.value = true
}

const handleExport = (row: Meeting) => {
  ElMessage.success('导出登记表功能开发中...')
}

const handleSubmit = async () => {
  const selectedTimes = timeSlots.value
    .map((selected, index) => selected ? `${String(index).padStart(2, '0')}:00` : null)
    .filter(time => time !== null)

  if (selectedTimes.length === 0) {
    ElMessage.warning('请至少选择一个会议时间段')
    return
  }

  const newMeeting: Meeting = {
    id: `meeting_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    sequence: meetingList.value.length + 1,
    meetingCode: `20SHY${new Date().getFullYear()}${String(new Date().getMonth() + 1).padStart(2, '0')}${String(new Date().getDate()).padStart(2, '0')}${String(meetingList.value.length + 1).padStart(3, '0')}`,
    meetingTopic: formData.meetingTopic,
    meetingRoom: formData.meetingRoom,
    organizer: formData.organizer,
    attendees: 1,
    meetingTime: `${formData.meetingDate} ${selectedTimes[0]}~${selectedTimes[selectedTimes.length - 1]}`,
    meetingStatus: '待审核',
    approvalStatus: '待审核'
  }

  if (dialogMode.value === 'add') {
    meetingList.value.unshift(newMeeting)
    ElMessage.success('创建成功')
  } else {
    ElMessage.success('更新成功')
  }
  
  dialogVisible.value = false
  pagination.total = meetingList.value.length
}

const resetFormData = () => {
  Object.assign(formData, {
    meetingTopic: '',
    organizer: '',
    attendeeType: 'specified',
    meetingRoom: '',
    description: '',
    expectedHours: 0,
    expectedMinutes: 0,
    isRecorded: false,
    meetingType: '',
    meetingDate: ''
  })
  timeSlots.value = Array(24).fill(false)
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
  pagination.total = meetingList.value.length
  loadData()
})
</script>

<style scoped lang="scss">
.meeting-booking {
  padding: 20px;
  background: linear-gradient(135deg, #0a1628 0%, #1e3a8a 50%, #0f172a 100%);
  min-height: auto;
  color: #ffffff;

  .stats-section {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 20px;

    .stats-card {
      background: rgba(15, 23, 42, 0.7);
      border-radius: 8px;
      padding: 20px;
      border: 1px solid rgba(0, 212, 255, 0.1);

      .card-header {
        margin-bottom: 16px;

        h3 {
          margin: 0;
          color: #00d4ff;
          font-size: 14px;
          font-weight: 500;
        }
      }

      .chart-container {
        height: 120px;
        display: flex;
        align-items: center;
        justify-content: center;

        .chart-placeholder {
          width: 100%;
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;

          .chart-content {
            text-align: center;

            .chart-item {
              display: flex;
              align-items: center;
              gap: 8px;
              margin-bottom: 8px;
              color: #e2e8f0;
              font-size: 12px;

              .dot {
                width: 8px;
                height: 8px;
                border-radius: 50%;
              }
            }

            .chart-bars {
              display: flex;
              align-items: end;
              gap: 4px;
              height: 60px;

              .bar {
                width: 12px;
                background: #1890ff;
                border-radius: 2px;
                min-height: 4px;
              }
            }
          }
        }

        .pie-chart {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 16px;

          .pie-center {
            text-align: center;

            .pie-number {
              font-size: 24px;
              font-weight: bold;
              color: #00d4ff;
            }

            .pie-label {
              font-size: 12px;
              color: #e2e8f0;
              margin-top: 4px;
            }
          }

          .pie-legend {
            display: flex;
            flex-direction: column;
            gap: 4px;

            .legend-item {
              display: flex;
              align-items: center;
              gap: 6px;
              font-size: 12px;
              color: #e2e8f0;

              .legend-color {
                width: 8px;
                height: 8px;
                border-radius: 2px;
              }
            }
          }
        }

        .gauge-chart {
          display: flex;
          align-items: center;
          justify-content: center;

          .gauge-center {
            text-align: center;

            .gauge-number {
              font-size: 24px;
              font-weight: bold;
              color: #00d4ff;
            }

            .gauge-label {
              font-size: 12px;
              color: #e2e8f0;
              margin-top: 4px;
            }
          }
        }

        .time-chart {
          display: flex;
          align-items: center;
          justify-content: center;

          .time-visualization {
            .time-dots {
              display: flex;
              gap: 8px;
              align-items: center;

              .dot {
                width: 12px;
                height: 12px;
                border-radius: 50%;
                background: rgba(0, 212, 255, 0.3);

                &.active {
                  background: #00d4ff;
                }
              }
            }
          }
        }
      }
    }
  }

  .filter-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 20px;
    background: rgba(15, 23, 42, 0.7);
    border-radius: 8px;
    border: 1px solid rgba(0, 212, 255, 0.1);

    .filter-left {
      display: flex;
      gap: 16px;
    }
  }

  .meeting-section {
    background: rgba(15, 23, 42, 0.7);
    border-radius: 8px;
    padding: 20px;
    border: 1px solid rgba(0, 212, 255, 0.1);

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      h2 {
        margin: 0;
        color: #00d4ff;
        font-size: 18px;
        font-weight: 600;
      }
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

  // 弹窗内容样式
  .quick-booking-form,
  .meeting-form {
    h3 {
      margin: 0 0 20px 0;
      color: #374151;
      font-size: 16px;
      font-weight: 600;
      border-bottom: 1px solid #e5e7eb;
      padding-bottom: 8px;
    }

    .time-inputs {
      display: flex;
      align-items: center;
      gap: 8px;

      span {
        color: #6b7280;
        font-size: 14px;
      }
    }

    .time-grid {
      border: 1px solid #e5e7eb;
      border-radius: 6px;
      overflow: hidden;
      width: 100%;

      .time-grid-header {
        display: grid;
        grid-template-columns: repeat(24, 1fr);
        background: #f8fafc;
        border-bottom: 1px solid #e5e7eb;

        .hour-label {
          padding: 8px 4px;
          border-right: 1px solid #e5e7eb;
          font-size: 10px;
          color: #6b7280;
          text-align: center;

          &:last-child {
            border-right: none;
          }
        }
      }

      .time-grid-row {
        display: grid;
        grid-template-columns: 80px repeat(24, 1fr);
        border-bottom: 1px solid #e5e7eb;

        .day-label {
          padding: 12px 8px;
          border-right: 1px solid #e5e7eb;
          background: #f8fafc;
          font-weight: 500;
          color: #374151;
          text-align: center;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .time-cell {
          width: 100%;
          height: 40px;
          border-right: 1px solid #e5e7eb;
          cursor: pointer;
          transition: all 0.2s ease;
          background: #ffffff;

          &:hover:not(.disabled) {
            background: #e0f2fe;
          }

          &.selected {
            background: #0ea5e9;
          }

          &.disabled {
            cursor: not-allowed;
            opacity: 0.6;
          }

          &:last-child {
            border-right: none;
          }
        }
      }
    }
  }
}

// 全局弹窗样式覆盖
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






