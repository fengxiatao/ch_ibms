<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="broadcast-task">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <Icon icon="ep:headphones" :size="24" />
        <h1>播放任务</h1>
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
            <el-option label="实时播放" value="realtime" />
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

    <!-- 播放任务列表 -->
    <div class="task-section">
      <h2>播放任务</h2>
      
      <!-- 空数据状态 -->
      <div v-if="taskList.length === 0" class="empty-state">
        <div class="empty-content">
          <Icon icon="ep:folder-opened" :size="80" color="#909399" />
          <p class="empty-text">暂无数据</p>
        </div>
      </div>

      <!-- 任务表格 -->
      <div v-else class="task-table">
        <el-table
          v-loading="loading"
          :data="taskList"
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
          <el-table-column prop="terminalCount" label="终端设备数" width="120" align="center" />
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
      </div>

      <!-- 分页 -->
      <div v-if="taskList.length > 0" class="pagination-section">
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

    <!-- 新增任务弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'add' ? '新增播放任务' : dialogMode === 'edit' ? '编辑播放任务' : '查看播放任务'"
      width="800px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div class="task-form">
        <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="任务名称" prop="taskName">
                <el-input
                  v-model="formData.taskName"
                  placeholder="请输入任务名称"
                  :disabled="dialogMode === 'view'"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="任务类型" prop="taskType">
                <el-select
                  v-model="formData.taskType"
                  placeholder="请选择任务类型"
                  style="width: 100%"
                  :disabled="dialogMode === 'view'"
                >
                  <el-option label="定时播放" value="scheduled" />
                  <el-option label="实时播放" value="realtime" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="执行时间" prop="executeTime">
                <el-date-picker
                  v-model="formData.executeTime"
                  type="datetime"
                  placeholder="请选择执行时间"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                  :disabled="dialogMode === 'view'"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="播放内容" prop="playContent">
                <el-select
                  v-model="formData.playContent"
                  placeholder="请选择播放内容"
                  style="width: 100%"
                  :disabled="dialogMode === 'view'"
                >
                  <el-option label="紧急通知" value="urgent" />
                  <el-option label="日常广播" value="daily" />
                  <el-option label="音乐播放" value="music" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :span="24">
              <el-form-item label="终端设备">
                <el-button type="primary" @click="showTerminalSelector" :disabled="dialogMode === 'view'">
                  选择终端设备
                </el-button>
                <span class="device-count">已选择 {{ selectedTerminals.length }} 个设备</span>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="24">
              <el-form-item label="任务描述" prop="description">
                <el-input
                  v-model="formData.description"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入任务描述"
                  maxlength="300"
                  show-word-limit
                  :disabled="dialogMode === 'view'"
                />
              </el-form-item>
            </el-col>
          </el-row>
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

    <!-- 终端选择弹窗 -->
    <el-dialog
      v-model="terminalSelectorVisible"
      title="选择终端设备"
      width="900px"
      destroy-on-close
    >
      <div class="terminal-selector">
        <!-- 设备搜索 -->
        <div class="device-search">
          <el-form inline>
            <el-form-item>
              <el-input
                v-model="terminalSearchForm.deviceName"
                placeholder="请输入设备名称"
                clearable
                style="width: 200px"
              />
            </el-form-item>
            <el-form-item>
              <el-select
                v-model="terminalSearchForm.location"
                placeholder="请选择位置"
                clearable
                style="width: 180px"
              >
                <el-option label="一楼大厅" value="floor1" />
                <el-option label="二楼办公区" value="floor2" />
                <el-option label="三楼会议室" value="floor3" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchTerminals">搜索</el-button>
              <el-button @click="resetTerminalSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 设备列表 -->
        <el-table
          ref="terminalTableRef"
          :data="availableTerminals"
          style="width: 100%"
          @selection-change="handleTerminalSelection"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="deviceName" label="设备名称" min-width="150" />
          <el-table-column prop="location" label="位置" width="150" />
          <el-table-column prop="deviceType" label="设备类型" width="120" />
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === '在线' ? 'success' : 'danger'" size="small">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="terminalSelectorVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmTerminalSelection">确认</el-button>
        </div>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

interface BroadcastTask {
  id: string
  sequence: number
  taskCode: string
  taskName: string
  taskType: string
  executeTime: string
  terminalCount: number
  taskStatus: string
  syncStatus: string
  description?: string
}

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const terminalSelectorVisible = ref(false)
const dialogMode = ref<'add' | 'edit' | 'view'>('add')

const searchForm = reactive({
  taskType: '',
  taskStatus: '',
  syncStatus: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  taskName: '',
  taskType: '',
  executeTime: '',
  playContent: '',
  description: ''
})

const formRules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  taskType: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  executeTime: [{ required: true, message: '请选择执行时间', trigger: 'change' }],
  playContent: [{ required: true, message: '请选择播放内容', trigger: 'change' }]
}

const terminalSearchForm = reactive({
  deviceName: '',
  location: ''
})

const selectedTerminals = ref<any[]>([])

// 任务列表数据（空数组，显示暂无数据状态）
const taskList = ref<BroadcastTask[]>([])

// 可选终端设备列表
const availableTerminals = ref([
  {
    id: '1',
    deviceName: '广播001',
    location: '一楼大厅',
    deviceType: '音响设备',
    status: '在线'
  },
  {
    id: '2',
    deviceName: '广播002',
    location: '二楼办公区',
    deviceType: '音响设备',
    status: '在线'
  },
  {
    id: '3',
    deviceName: '广播003',
    location: '三楼会议室',
    deviceType: '音响设备',
    status: '离线'
  }
])

// 工具函数
const getTaskTypeColor = (type: string) => {
  switch (type) {
    case 'scheduled':
      return 'primary'
    case 'realtime':
      return 'success'
    default:
      return ''
  }
}

const getTaskTypeText = (type: string) => {
  switch (type) {
    case 'scheduled':
      return '定时播放'
    case 'realtime':
      return '实时播放'
    default:
      return type
  }
}

const getTaskStatusColor = (status: string) => {
  switch (status) {
    case '运行中':
      return 'success'
    case '已暂停':
      return 'warning'
    case '已完成':
      return 'info'
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
  dialogMode.value = 'add'
  resetFormData()
  dialogVisible.value = true
}

const handleEdit = (task: BroadcastTask) => {
  dialogMode.value = 'edit'
  Object.assign(formData, {
    taskName: task.taskName,
    taskType: task.taskType,
    executeTime: task.executeTime,
    playContent: 'urgent',
    description: task.description || ''
  })
  dialogVisible.value = true
}

const handleView = (task: BroadcastTask) => {
  dialogMode.value = 'view'
  Object.assign(formData, {
    taskName: task.taskName,
    taskType: task.taskType,
    executeTime: task.executeTime,
    playContent: 'urgent',
    description: task.description || ''
  })
  dialogVisible.value = true
}

const handleDelete = (task: BroadcastTask) => {
  ElMessageBox.confirm('确定要删除这个播放任务吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = taskList.value.findIndex(item => item.id === task.id)
    if (index > -1) {
      taskList.value.splice(index, 1)
      pagination.total = taskList.value.length
      ElMessage.success('删除成功')
    }
  })
}

const showTerminalSelector = () => {
  terminalSelectorVisible.value = true
}

const handleTerminalSelection = (selection: any[]) => {
  selectedTerminals.value = selection
}

const confirmTerminalSelection = () => {
  terminalSelectorVisible.value = false
  ElMessage.success(`已选择 ${selectedTerminals.value.length} 个终端设备`)
}

const searchTerminals = () => {
  ElMessage.success('终端搜索功能开发中...')
}

const resetTerminalSearch = () => {
  Object.assign(terminalSearchForm, {
    deviceName: '',
    location: ''
  })
}

const handleSubmit = async () => {
  if (selectedTerminals.value.length === 0) {
    ElMessage.warning('请选择至少一个终端设备')
    return
  }

  const newTask: BroadcastTask = {
    id: `task_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    sequence: taskList.value.length + 1,
    taskCode: `20BT${new Date().getFullYear()}${String(new Date().getMonth() + 1).padStart(2, '0')}${String(new Date().getDate()).padStart(2, '0')}${String(taskList.value.length + 1).padStart(3, '0')}`,
    taskName: formData.taskName,
    taskType: formData.taskType,
    executeTime: formData.executeTime,
    terminalCount: selectedTerminals.value.length,
    taskStatus: '运行中',
    syncStatus: '同步中',
    description: formData.description
  }

  if (dialogMode.value === 'add') {
    taskList.value.unshift(newTask)
    ElMessage.success('创建成功')
  } else {
    ElMessage.success('更新成功')
  }
  
  dialogVisible.value = false
  pagination.total = taskList.value.length
}

const resetFormData = () => {
  Object.assign(formData, {
    taskName: '',
    taskType: '',
    executeTime: '',
    playContent: '',
    description: ''
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
  pagination.total = taskList.value.length
  loadData()
})
</script>

<style scoped lang="scss">
.broadcast-task {
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

  .task-section {
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

    .empty-state {
      padding: 60px 0;
      text-align: center;

      .empty-content {
        .empty-text {
          margin-top: 16px;
          color: #909399;
          font-size: 16px;
        }
      }
    }

    .task-table {
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
  .task-form {
    :deep(.el-form-item__label) {
      color: #374151;
    }

    .device-count {
      margin-left: 12px;
      color: #6b7280;
      font-size: 14px;
    }
  }

  .terminal-selector {
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






