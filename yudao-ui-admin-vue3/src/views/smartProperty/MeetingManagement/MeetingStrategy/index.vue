<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="meeting-strategy">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <Icon icon="ep:document" :size="24" />
        <h1>会议策略</h1>
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
          <el-input
            v-model="searchForm.strategyName"
            placeholder="请输入策略名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-select
            v-model="searchForm.meetingRoom"
            placeholder="请选择会议室名称"
            clearable
            style="width: 180px"
          >
            <el-option label="大会议室" value="大会议室" />
            <el-option label="小会议室" value="小会议室" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="启用" value="启用" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <Icon icon="ep:search" />
            搜索
          </el-button>
          <el-button @click="handleReset">
            <Icon icon="ep:refresh" />
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 会议策略表格 -->
    <div class="strategy-section">
      <h2>会议策略</h2>
      <el-table
        v-loading="loading"
        :data="strategyList"
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="sequence" label="序号" width="80" align="center" />
        <el-table-column prop="strategyCode" label="策略编号" width="180" />
        <el-table-column prop="strategyName" label="策略名称" min-width="140" />
        <el-table-column prop="meetingRoom" label="会议室名称" width="120" />
        <el-table-column prop="bookingTime" label="预约时间" width="180" />
        <el-table-column prop="bookingDuration" label="预约时长限制" width="140" />
        <el-table-column prop="delayTime" label="延迟时间" width="120" />
        <el-table-column prop="delayDuration" label="延迟时长限制" width="140" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '启用' ? 'success' : 'danger'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              查看
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              删除
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

    <!-- 策略编辑/新增弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'add' ? '新增策略' : dialogMode === 'edit' ? '编辑策略' : '查看策略'"
      width="900px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div class="strategy-form">
        <h3>基本信息</h3>
        <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="策略名称" prop="strategyName">
                <el-input
                  v-model="formData.strategyName"
                  placeholder="请输入策略名称"
                  :disabled="dialogMode === 'view'"
                />
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
              <el-form-item label="备注" prop="remark">
                <el-input
                  v-model="formData.remark"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入备注"
                  maxlength="300"
                  show-word-limit
                  :disabled="dialogMode === 'view'"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>

        <!-- 提前执行动作 -->
        <div class="action-section">
          <h3>提前执行动作</h3>
          <div class="action-controls">
            <el-form-item label="提前时间：" style="margin-bottom: 16px;">
              <el-switch v-model="formData.advanceEnabled" :disabled="dialogMode === 'view'" />
            </el-form-item>
            
            <div v-if="formData.advanceEnabled" class="time-input-group">
              <el-input-number
                v-model="formData.advanceMinutes"
                :min="0"
                :max="60"
                placeholder="分钟"
                :disabled="dialogMode === 'view'"
              />
              <span>秒</span>
            </div>

            <el-form-item label="联动设备：" style="margin-top: 16px;">
              <el-link type="primary" @click="showDeviceSelector" :disabled="dialogMode === 'view'">
                请选择设备
                <Icon icon="ep:question-filled" />
              </el-link>
            </el-form-item>
          </div>

          <!-- 设备动作表格 -->
          <div class="device-table-container">
            <el-table
              :data="advanceDeviceActions"
              style="width: 100%"
              border
              :show-header="true"
            >
              <el-table-column prop="sequence" label="序号" width="80" align="center" />
              <el-table-column prop="deviceCode" label="设备编码" width="150" />
              <el-table-column prop="deviceName" label="设备名称" min-width="120" />
              <el-table-column prop="system" label="专业系统" width="100" />
              <el-table-column prop="location" label="空间位置" width="150" />
              <el-table-column prop="snCode" label="SN编码" width="100" />
              <el-table-column prop="deviceStatus" label="设备状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.deviceStatus === '正常' ? 'success' : 'danger'" size="small">
                    {{ row.deviceStatus }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="executeAction" label="执行动作" width="100" />
              <el-table-column label="操作" width="100" fixed="right" align="center">
                <template #default="{ $index }">
                  <el-button link type="danger" @click="removeAdvanceDeviceAction($index)" :disabled="dialogMode === 'view'">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 延迟执行动作 -->
        <div class="action-section">
          <h3>延迟执行动作</h3>
          <div class="action-controls">
            <el-form-item label="延迟时间：" style="margin-bottom: 16px;">
              <el-switch v-model="formData.delayEnabled" :disabled="dialogMode === 'view'" />
            </el-form-item>
            
            <div v-if="formData.delayEnabled" class="time-input-group">
              <el-input-number
                v-model="formData.delayMinutes"
                :min="0"
                :max="60"
                placeholder="分钟"
                :disabled="dialogMode === 'view'"
              />
              <span>秒</span>
            </div>

            <el-form-item label="联动设备：" style="margin-top: 16px;">
              <el-link type="primary" @click="showDelayDeviceSelector" :disabled="dialogMode === 'view'">
                请选择设备
                <Icon icon="ep:question-filled" />
              </el-link>
            </el-form-item>
          </div>

          <!-- 延迟设备动作表格 -->
          <div class="device-table-container">
            <el-table
              :data="delayDeviceActions"
              style="width: 100%"
              border
              :show-header="true"
            >
              <el-table-column prop="sequence" label="序号" width="80" align="center" />
              <el-table-column prop="deviceCode" label="设备编码" width="150" />
              <el-table-column prop="deviceName" label="设备名称" min-width="120" />
              <el-table-column prop="system" label="专业系统" width="100" />
              <el-table-column prop="location" label="空间位置" width="150" />
              <el-table-column prop="snCode" label="SN编码" width="100" />
              <el-table-column prop="deviceStatus" label="设备状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.deviceStatus === '正常' ? 'success' : 'danger'" size="small">
                    {{ row.deviceStatus }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="executeAction" label="执行动作" width="100" />
              <el-table-column label="操作" width="100" fixed="right" align="center">
                <template #default="{ $index }">
                  <el-button link type="danger" @click="removeDelayDeviceAction($index)" :disabled="dialogMode === 'view'">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
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

    <!-- 设备选择弹窗 -->
    <el-dialog
      v-model="deviceSelectorVisible"
      title="选择设备"
      width="800px"
      destroy-on-close
    >
      <div class="device-selector">
        <!-- 设备搜索 -->
        <div class="device-search">
          <el-form inline>
            <el-form-item>
              <el-input
                v-model="deviceSearchForm.deviceName"
                placeholder="请输入设备名称"
                clearable
                style="width: 200px"
              />
            </el-form-item>
            <el-form-item>
              <el-select
                v-model="deviceSearchForm.system"
                placeholder="请选择专业系统"
                clearable
                style="width: 150px"
              >
                <el-option label="照明系统" value="照明系统" />
                <el-option label="空调系统" value="空调系统" />
                <el-option label="安防系统" value="安防系统" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchDevices">搜索</el-button>
              <el-button @click="resetDeviceSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 设备列表 -->
        <el-table
          ref="deviceTableRef"
          :data="availableDevices"
          style="width: 100%"
          @selection-change="handleDeviceSelection"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="deviceCode" label="设备编码" width="150" />
          <el-table-column prop="deviceName" label="设备名称" min-width="120" />
          <el-table-column prop="system" label="专业系统" width="100" />
          <el-table-column prop="location" label="空间位置" width="150" />
          <el-table-column prop="snCode" label="SN编码" width="100" />
          <el-table-column prop="deviceStatus" label="设备状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.deviceStatus === '正常' ? 'success' : 'danger'" size="small">
                {{ row.deviceStatus }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="deviceSelectorVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmDeviceSelection">确认</el-button>
        </div>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Icon } from '@/components/Icon'

interface Strategy {
  id: string
  sequence: number
  strategyCode: string
  strategyName: string
  meetingRoom: string
  bookingTime: string
  bookingDuration: string
  delayTime: string
  delayDuration: string
  status: string
  remark?: string
}

interface DeviceAction {
  sequence: number
  deviceCode: string
  deviceName: string
  system: string
  location: string
  snCode: string
  deviceStatus: string
  executeAction: string
}

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const deviceSelectorVisible = ref(false)
const dialogMode = ref<'add' | 'edit' | 'view'>('add')
const currentActionType = ref<'advance' | 'delay'>('advance')

const searchForm = reactive({
  strategyName: '',
  meetingRoom: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  strategyName: '',
  meetingRoom: '',
  remark: '',
  advanceEnabled: false,
  advanceMinutes: 0,
  delayEnabled: false,
  delayMinutes: 0
})

const formRules = {
  strategyName: [{ required: true, message: '请输入策略名称', trigger: 'blur' }],
  meetingRoom: [{ required: true, message: '请选择会议室', trigger: 'change' }]
}

const deviceSearchForm = reactive({
  deviceName: '',
  system: ''
})

// 提前执行设备动作
const advanceDeviceActions = ref<DeviceAction[]>([])

// 延迟执行设备动作
const delayDeviceActions = ref<DeviceAction[]>([])

// 可选设备列表
const availableDevices = ref([
  {
    deviceCode: 'LED_001',
    deviceName: '会议室照明',
    system: '照明系统',
    location: '大会议室',
    snCode: 'SN001',
    deviceStatus: '正常'
  },
  {
    deviceCode: 'AC_001',
    deviceName: '会议室空调',
    system: '空调系统',
    location: '大会议室',
    snCode: 'SN002',
    deviceStatus: '正常'
  },
  {
    deviceCode: 'CAM_001',
    deviceName: '会议室监控',
    system: '安防系统',
    location: '大会议室',
    snCode: 'SN003',
    deviceStatus: '正常'
  }
])

const selectedDevices = ref<any[]>([])

// 策略列表数据（暂时为空，根据图片显示暂无数据）
const strategyList = ref<Strategy[]>([])

// 事件处理
const handleSearch = () => {
  pagination.page = 1
  loadData()
  ElMessage.success('搜索完成')
}

const handleReset = () => {
  Object.assign(searchForm, {
    strategyName: '',
    meetingRoom: '',
    status: ''
  })
  handleSearch()
}

const handleCreate = () => {
  dialogMode.value = 'add'
  resetFormData()
  dialogVisible.value = true
}

const handleEdit = (row: Strategy) => {
  dialogMode.value = 'edit'
  Object.assign(formData, {
    strategyName: row.strategyName,
    meetingRoom: row.meetingRoom,
    remark: row.remark || '',
    advanceEnabled: false,
    advanceMinutes: 0,
    delayEnabled: false,
    delayMinutes: 0
  })
  dialogVisible.value = true
}

const handleView = (row: Strategy) => {
  dialogMode.value = 'view'
  Object.assign(formData, {
    strategyName: row.strategyName,
    meetingRoom: row.meetingRoom,
    remark: row.remark || '',
    advanceEnabled: false,
    advanceMinutes: 0,
    delayEnabled: false,
    delayMinutes: 0
  })
  dialogVisible.value = true
}

const handleDelete = (row: Strategy) => {
  ElMessageBox.confirm('确定要删除这个策略吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = strategyList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      strategyList.value.splice(index, 1)
      // 重新编号
      strategyList.value.forEach((item, idx) => {
        item.sequence = idx + 1
      })
      pagination.total = strategyList.value.length
      ElMessage.success('删除成功')
    }
  })
}

const showDeviceSelector = () => {
  currentActionType.value = 'advance'
  deviceSelectorVisible.value = true
}

const showDelayDeviceSelector = () => {
  currentActionType.value = 'delay'
  deviceSelectorVisible.value = true
}

const handleDeviceSelection = (selection: any[]) => {
  selectedDevices.value = selection
}

const confirmDeviceSelection = () => {
  const targetActions = currentActionType.value === 'advance' ? advanceDeviceActions : delayDeviceActions
  
  selectedDevices.value.forEach((device) => {
    const existingIndex = targetActions.value.findIndex(action => action.deviceCode === device.deviceCode)
    if (existingIndex === -1) {
      targetActions.value.push({
        sequence: targetActions.value.length + 1,
        ...device,
        executeAction: '开启'
      })
    }
  })
  
  deviceSelectorVisible.value = false
  ElMessage.success(`已添加 ${selectedDevices.value.length} 个设备`)
}

const removeAdvanceDeviceAction = (index: number) => {
  advanceDeviceActions.value.splice(index, 1)
  // 重新编号
  advanceDeviceActions.value.forEach((item, idx) => {
    item.sequence = idx + 1
  })
}

const removeDelayDeviceAction = (index: number) => {
  delayDeviceActions.value.splice(index, 1)
  // 重新编号
  delayDeviceActions.value.forEach((item, idx) => {
    item.sequence = idx + 1
  })
}

const searchDevices = () => {
  ElMessage.success('设备搜索功能开发中...')
}

const resetDeviceSearch = () => {
  Object.assign(deviceSearchForm, {
    deviceName: '',
    system: ''
  })
}

const handleSubmit = async () => {
  const newStrategy: Strategy = {
    id: `strategy_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    sequence: strategyList.value.length + 1,
    strategyCode: `ST${new Date().getFullYear()}${String(new Date().getMonth() + 1).padStart(2, '0')}${String(new Date().getDate()).padStart(2, '0')}${String(strategyList.value.length + 1).padStart(3, '0')}`,
    strategyName: formData.strategyName,
    meetingRoom: formData.meetingRoom,
    bookingTime: '全天',
    bookingDuration: '2小时',
    delayTime: formData.delayEnabled ? `${formData.delayMinutes}秒` : '-',
    delayDuration: '30分钟',
    status: '启用',
    remark: formData.remark
  }

  if (dialogMode.value === 'add') {
    strategyList.value.unshift(newStrategy)
    ElMessage.success('创建成功')
  } else {
    ElMessage.success('更新成功')
  }
  
  dialogVisible.value = false
  pagination.total = strategyList.value.length
}

const resetFormData = () => {
  Object.assign(formData, {
    strategyName: '',
    meetingRoom: '',
    remark: '',
    advanceEnabled: false,
    advanceMinutes: 0,
    delayEnabled: false,
    delayMinutes: 0
  })
  advanceDeviceActions.value = []
  delayDeviceActions.value = []
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
  pagination.total = strategyList.value.length
  loadData()
})
</script>

<style scoped lang="scss">
.meeting-strategy {
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

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .search-section {
    margin-bottom: 20px;
    padding: 20px;
    background: rgba(15, 23, 42, 0.7);
    border-radius: 8px;
    border: 1px solid rgba(0, 212, 255, 0.1);

    :deep(.el-form-item__label) {
      color: #e2e8f0;
    }
  }

  .strategy-section {
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

  // 弹窗内容样式
  .strategy-form {
    h3 {
      margin: 0 0 20px 0;
      color: #374151;
      font-size: 16px;
      font-weight: 600;
      border-bottom: 1px solid #e5e7eb;
      padding-bottom: 8px;
    }

    .action-section {
      margin-top: 32px;

      .action-controls {
        margin-bottom: 20px;

        .time-input-group {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-top: 8px;

          span {
            color: #6b7280;
            font-size: 14px;
          }
        }
      }

      .device-table-container {
        border: 1px solid #e5e7eb;
        border-radius: 6px;

        :deep(.el-table) {
          .el-table__header {
            th {
              background: #f8fafc;
              color: #374151;
              font-weight: 500;
            }
          }

          .el-table__body {
            tr {
              &:hover {
                background: #f8fafc;
              }
            }
          }
        }
      }
    }
  }

  .device-selector {
    .device-search {
      margin-bottom: 20px;
      padding: 16px;
      background: #f8fafc;
      border-radius: 6px;
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

