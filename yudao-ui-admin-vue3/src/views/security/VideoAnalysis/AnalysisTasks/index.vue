<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="analysis-tasks-container">
      <!-- 搜索区域 -->
      <div class="search-section">
        <el-form :model="searchForm" inline class="search-form">
          <el-form-item label="请输入任务名称">
            <el-input 
              v-model="searchForm.taskName" 
              placeholder="请输入任务名称" 
              clearable
              style="width: 200px"
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="请输入运行设备">
            <el-input 
              v-model="searchForm.device" 
              placeholder="请输入运行设备" 
              clearable
              style="width: 200px"
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="请选择任务状态">
            <el-select 
              v-model="searchForm.status" 
              placeholder="请选择任务状态" 
              clearable
              style="width: 200px"
              @change="handleSearch"
            >
              <el-option label="已停用" value="stopped" />
              <el-option label="已启用" value="enabled" />
              <el-option label="运行中" value="running" />
              <el-option label="异常" value="error" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <!-- 工具栏 -->
      <div class="toolbar-section">
        <div class="left-tools">
          <h2 class="page-title">分析任务列表</h2>
        </div>
        <div class="right-tools">
          <el-button type="primary" @click="handleCreateTask">
            新增
          </el-button>
        </div>
      </div>

      <!-- 任务列表 -->
      <div class="tasks-grid">
        <div 
          v-for="task in filteredTasks" 
          :key="task.id"
          class="task-card"
          @click="handleViewTask(task)"
        >
          <div class="card-header">
            <div class="task-name">{{ task.name }}</div>
            <el-tag 
              :type="getStatusType(task.status)"
              class="status-tag"
            >
              {{ getStatusText(task.status) }}
            </el-tag>
          </div>
          <div class="card-content">
            <div class="task-info">
              <div class="info-item">
                <Icon icon="ep:monitor" class="info-icon" />
                <span>定时策略：{{ task.schedule }}</span>
              </div>
              <div class="info-item">
                <Icon icon="ep:cpu" class="info-icon" />
                <span>适用设备：{{ task.targetDevice }}</span>
              </div>
              <div class="info-item">
                <Icon icon="ep:finished" class="info-icon" />
                <span>已选择样本：{{ task.selectedSamples }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <!-- 任务详情对话框 -->
      <el-dialog
        v-model="detailDialogVisible"
        title="任务详情"
        width="1000px"
        :before-close="handleCloseDetail"
      >
        <div v-if="selectedTask" class="task-detail">
          <!-- 基础信息 -->
          <div class="detail-section">
            <div class="section-title">基础信息</div>
            <div class="info-grid">
              <div class="info-row">
                <div class="info-label">任务编号：</div>
                <div class="info-value">{{ selectedTask.taskCode }}</div>
                <div class="info-label">任务状态：</div>
                <div class="info-value">
                  <el-tag :type="getStatusType(selectedTask.status)">
                    {{ getStatusText(selectedTask.status) }}
                  </el-tag>
                </div>
              </div>
              <div class="info-row">
                <div class="info-label">任务名称：</div>
                <div class="info-value">{{ selectedTask.name }}</div>
                <div class="info-label">任务终端：</div>
                <div class="info-value">{{ selectedTask.terminal }}</div>
              </div>
              <div class="info-row">
                <div class="info-label">生效日期：</div>
                <div class="info-value">{{ selectedTask.effectiveDate }}</div>
                <div class="info-label">定时策略：</div>
                <div class="info-value">{{ selectedTask.schedule }}</div>
              </div>
              <div class="info-row">
                <div class="info-label">备注：</div>
                <div class="info-value">{{ selectedTask.remark || '-' }}</div>
                <div class="info-label"></div>
                <div class="info-value"></div>
              </div>
            </div>
          </div>

          <!-- 任务算法 -->
          <div class="detail-section">
            <div class="section-title">任务算法</div>
            <div class="algorithm-content">
              <div class="algorithm-info">
                <span>适用设备：{{ selectedTask.targetDevice }}</span>
              </div>
              <div class="algorithm-buttons">
                <el-button size="small" type="primary">人员统计</el-button>
                <el-button size="small">密度检测</el-button>
              </div>
            </div>
          </div>

          <!-- 区域规则 -->
          <div class="detail-section">
            <div class="section-title">区域规则</div>
            <div class="rules-table">
              <el-table :data="selectedTask.rules" style="width: 100%">
                <el-table-column prop="sequence" label="序号" width="80" />
                <el-table-column prop="ruleName" label="规则名称" width="200" />
                <el-table-column prop="ruleType" label="区域类型" width="150" />
                <el-table-column prop="method" label="智能算法" width="150" />
              </el-table>
              <div v-if="!selectedTask.rules || selectedTask.rules.length === 0" class="empty-rules">
                <el-empty description="暂无数据" />
              </div>
            </div>
          </div>
        </div>
        
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="handleCloseDetail">返回</el-button>
            <el-button type="primary" @click="handleEditTask">编辑</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ContentWrap } from '@/components/ContentWrap'
import { Icon } from '@/components/Icon'

defineOptions({ name: 'AnalysisTasks' })

// 任务接口定义
interface AnalysisTask {
  id: number
  taskCode: string
  name: string
  status: string
  schedule: string
  targetDevice: string
  selectedSamples: number
  terminal: string
  effectiveDate: string
  remark?: string
  rules?: TaskRule[]
}

interface TaskRule {
  sequence: number
  ruleName: string
  ruleType: string
  method: string
}

// 响应式数据
const detailDialogVisible = ref(false)
const selectedTask = ref<AnalysisTask | null>(null)

const searchForm = reactive({
  taskName: '',
  device: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 1
})

// Mock数据
const analysisTasks = ref<AnalysisTask[]>([
  {
    id: 1,
    taskCode: '20TAI20241120001',
    name: '智能分析任务20241120163351',
    status: 'stopped',
    schedule: '2212',
    targetDevice: '办公区走廊人脸识别摄像机J03',
    selectedSamples: 2,
    terminal: '智能AI盒子',
    effectiveDate: '2024-11-20~2024-11-20',
    remark: '-',
    rules: []
  }
])

// 计算属性
const filteredTasks = computed(() => {
  return analysisTasks.value.filter(task => {
    const matchTaskName = !searchForm.taskName || 
      task.name.toLowerCase().includes(searchForm.taskName.toLowerCase())
    
    const matchDevice = !searchForm.device || 
      task.targetDevice.toLowerCase().includes(searchForm.device.toLowerCase())
    
    const matchStatus = !searchForm.status || task.status === searchForm.status
    
    return matchTaskName && matchDevice && matchStatus
  })
})

// 方法
const handleSearch = () => {
  console.log('搜索参数：', searchForm)
}

const handleCreateTask = () => {
  console.log('新增任务')
}

const handleViewTask = (task: AnalysisTask) => {
  selectedTask.value = task
  detailDialogVisible.value = true
}

const handleEditTask = () => {
  console.log('编辑任务：', selectedTask.value)
  handleCloseDetail()
}

const handleCloseDetail = () => {
  detailDialogVisible.value = false
  selectedTask.value = null
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
}

const getStatusType = (status: string) => {
  const statusMap = {
    'stopped': 'info',
    'enabled': 'success',
    'running': 'warning', 
    'error': 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const statusTextMap = {
    'stopped': '已停用',
    'enabled': '已启用',
    'running': '运行中',
    'error': '异常'
  }
  return statusTextMap[status] || status
}
</script>

<style scoped lang="scss">
@use '@/styles/dark-theme.scss';

.analysis-tasks-container {
  padding: 20px;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 8px;
  backdrop-filter: blur(10px);

  // 搜索区域
  .search-section {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 20px;
    border: 1px solid rgba(255, 255, 255, 0.1);

    .search-form {
      :deep(.el-form-item) {
        margin-bottom: 16px;
        margin-right: 24px;

        .el-form-item__label {
          color: rgba(255, 255, 255, 0.9);
          font-weight: 500;
        }

        .el-input {
          .el-input__wrapper {
            background: rgba(255, 255, 255, 0.08);
            border: 1px solid rgba(255, 255, 255, 0.2);
            box-shadow: none;

            &:hover {
              border-color: #1890ff;
            }

            &.is-focus {
              border-color: #1890ff;
              box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
            }

            .el-input__inner {
              color: #ffffff;

              &::placeholder {
                color: rgba(255, 255, 255, 0.5);
              }
            }
          }
        }

        .el-select {
          .el-select__wrapper {
            background: rgba(255, 255, 255, 0.08);
            border: 1px solid rgba(255, 255, 255, 0.2);
            box-shadow: none;

            &:hover {
              border-color: #1890ff;
            }

            &.is-focus {
              border-color: #1890ff;
              box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
            }

            .el-select__selected-item {
              color: #ffffff;
            }

            .el-select__placeholder {
              color: rgba(255, 255, 255, 0.5);
            }
          }
        }
      }
    }
  }

  // 工具栏
  .toolbar-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .left-tools {
      .page-title {
        margin: 0;
        color: #ffffff;
        font-size: 20px;
        font-weight: 600;
      }
    }

    .right-tools {
      display: flex;
      gap: 8px;
    }
  }

  // 任务网格
  .tasks-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
    gap: 20px;
    margin-bottom: 20px;

    .task-card {
      background: rgba(255, 255, 255, 0.05);
      border-radius: 8px;
      padding: 20px;
      border: 1px solid rgba(255, 255, 255, 0.1);
      cursor: pointer;
      transition: all 0.3s ease;
      backdrop-filter: blur(10px);

      &:hover {
        transform: translateY(-4px);
        border-color: rgba(24, 144, 255, 0.5);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
      }

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 16px;

        .task-name {
          color: #ffffff;
          font-size: 16px;
          font-weight: 600;
          flex: 1;
          margin-right: 12px;
          word-break: break-all;
        }

        .status-tag {
          flex-shrink: 0;
        }
      }

      .card-content {
        .task-info {
          .info-item {
            display: flex;
            align-items: center;
            margin-bottom: 8px;
            color: rgba(255, 255, 255, 0.8);
            font-size: 14px;

            .info-icon {
              margin-right: 8px;
              color: #1890ff;
              font-size: 16px;
            }
          }
        }
      }
    }
  }

  // 分页
  .pagination-container {
    display: flex;
    justify-content: center;
    margin-top: 20px;

    :deep(.el-pagination) {
      .el-pagination__total,
      .el-pagination__jump,
      .el-select .el-input .el-input__inner {
        color: rgba(255, 255, 255, 0.9);
      }

      .btn-prev,
      .btn-next,
      .el-pager li {
        background: rgba(255, 255, 255, 0.08);
        border: 1px solid rgba(255, 255, 255, 0.2);
        color: rgba(255, 255, 255, 0.9);

        &:hover:not(.is-disabled) {
          background: rgba(24, 144, 255, 0.3);
          border-color: #1890ff;
        }

        &.is-active {
          background: #1890ff;
          border-color: #1890ff;
          color: #ffffff;
        }
      }
    }
  }

  // 任务详情对话框
  :deep(.el-dialog) {
    background: rgba(30, 30, 30, 0.95);
    border: 1px solid rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(20px);

    .el-dialog__header {
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      padding: 20px 20px 16px;

      .el-dialog__title {
        color: #ffffff;
        font-size: 18px;
        font-weight: 600;
      }

      .el-dialog__headerbtn {
        .el-dialog__close {
          color: rgba(255, 255, 255, 0.8);

          &:hover {
            color: #ffffff;
          }
        }
      }
    }

    .el-dialog__body {
      padding: 20px;
      color: #ffffff;
    }

    .el-dialog__footer {
      border-top: 1px solid rgba(255, 255, 255, 0.1);
      padding: 16px 20px 20px;
    }
  }

  .task-detail {
    .detail-section {
      margin-bottom: 24px;

      .section-title {
        font-size: 16px;
        font-weight: 600;
        color: #ffffff;
        margin-bottom: 16px;
        padding-bottom: 8px;
        border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      }

      .info-grid {
        .info-row {
          display: grid;
          grid-template-columns: 120px 1fr 120px 1fr;
          gap: 16px;
          margin-bottom: 12px;
          align-items: center;

          .info-label {
            color: rgba(255, 255, 255, 0.8);
            font-size: 14px;
          }

          .info-value {
            color: #ffffff;
            font-size: 14px;
          }
        }
      }

      .algorithm-content {
        .algorithm-info {
          margin-bottom: 16px;
          color: rgba(255, 255, 255, 0.9);
          font-size: 14px;
        }

        .algorithm-buttons {
          display: flex;
          gap: 8px;
        }
      }

      .rules-table {
        .empty-rules {
          padding: 40px 0;
          text-align: center;
        }
      }
    }
  }

  // Element Plus 组件样式覆盖
  :deep(.el-button--primary) {
    background: #1890ff;
    border-color: #1890ff;

    &:hover {
      background: #40a9ff;
      border-color: #40a9ff;
    }
  }

  :deep(.el-button:not(.el-button--primary)) {
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: rgba(255, 255, 255, 0.9);

    &:hover {
      background: rgba(255, 255, 255, 0.12);
      border-color: rgba(255, 255, 255, 0.3);
    }
  }

  :deep(.el-tag) {
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: rgba(255, 255, 255, 0.9);
  }

  :deep(.el-tag.el-tag--success) {
    background: rgba(82, 196, 26, 0.2);
    border-color: rgba(82, 196, 26, 0.4);
    color: #52c41a;
  }

  :deep(.el-tag.el-tag--warning) {
    background: rgba(250, 173, 20, 0.2);
    border-color: rgba(250, 173, 20, 0.4);
    color: #faad14;
  }

  :deep(.el-tag.el-tag--danger) {
    background: rgba(255, 77, 79, 0.2);
    border-color: rgba(255, 77, 79, 0.4);
    color: #ff4d4f;
  }

  :deep(.el-tag.el-tag--info) {
    background: rgba(144, 147, 153, 0.2);
    border-color: rgba(144, 147, 153, 0.4);
    color: #909399;
  }

  :deep(.el-table) {
    background: transparent;
    color: #ffffff;

    .el-table__header {
      th {
        background: rgba(255, 255, 255, 0.08);
        border-bottom: 1px solid rgba(255, 255, 255, 0.1);
        color: rgba(255, 255, 255, 0.9);
        font-weight: 600;
      }
    }

    .el-table__body {
      tr {
        background: transparent;

        &:hover {
          background: rgba(255, 255, 255, 0.05) !important;
        }

        td {
          border-bottom: 1px solid rgba(255, 255, 255, 0.05);
          color: rgba(255, 255, 255, 0.9);
        }
      }

      .el-table__row--striped {
        background: rgba(255, 255, 255, 0.02);
      }
    }
  }
}

// 下拉菜单样式
:deep(.el-select-dropdown) {
  background: rgba(30, 30, 30, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(20px);

  .el-select-dropdown__item {
    color: rgba(255, 255, 255, 0.9);

    &:hover {
      background: rgba(24, 144, 255, 0.2);
    }

    &.is-selected {
      background: rgba(24, 144, 255, 0.3);
      color: #ffffff;
    }
  }
}
</style>






