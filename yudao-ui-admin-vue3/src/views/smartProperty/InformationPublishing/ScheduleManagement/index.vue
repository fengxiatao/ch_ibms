<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="schedule-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <Icon icon="ep:calendar" :size="24" />
        <h1>节目管理</h1>
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
            v-model="searchForm.scheduleName"
            placeholder="请输入节目名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-select
            v-model="searchForm.scheduleStatus"
            placeholder="请选择节目状态"
            clearable
            style="width: 180px"
          >
            <el-option label="同步中" value="syncing" />
            <el-option label="同步成功" value="synced" />
            <el-option label="同步失败" value="failed" />
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

    <!-- 节目管理列表 -->
    <div class="schedule-section">
      <h2>节目管理</h2>
      
      <!-- 卡片视图 -->
      <div class="schedule-cards">
        <div
          v-for="schedule in scheduleList"
          :key="schedule.id"
          class="schedule-card"
        >
          <div class="card-header">
            <div class="schedule-icon">
              <Icon icon="ep:video-play" :size="32" color="#00d4ff" />
            </div>
            <div class="schedule-status">
              <el-tag :type="getStatusType(schedule.syncStatus)" size="small">
                {{ getStatusText(schedule.syncStatus) }}
              </el-tag>
            </div>
          </div>
          
          <div class="card-content">
            <h3 class="schedule-title">{{ schedule.scheduleName }}</h3>
            <div class="schedule-info">
              <div class="info-item">
                <Icon icon="ep:document" />
                <span>申请编号：{{ schedule.applicationCode }}</span>
              </div>
            </div>
            <div class="card-actions">
              <el-button link type="primary" @click="handleEdit(schedule)">
                编辑
              </el-button>
              <el-button link type="primary" @click="handlePreview(schedule)">
                预览
              </el-button>
              <el-button link type="danger" @click="handleDelete(schedule)">
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handlePageSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 新增节目弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'add' ? '新增节目' : '编辑节目'"
      width="600px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div class="schedule-form">
        <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
          <el-form-item label="节目名称" prop="scheduleName">
            <el-input
              v-model="formData.scheduleName"
              placeholder="请输入节目名称"
              :disabled="dialogMode === 'view'"
            />
          </el-form-item>
          
          <el-form-item label="分辨率" prop="resolution">
            <el-select
              v-model="formData.resolution"
              placeholder="请选择分辨率"
              style="width: 100%"
              :disabled="dialogMode === 'view'"
            >
              <el-option label="1920x1080" value="1920x1080" />
              <el-option label="1366x768" value="1366x768" />
              <el-option label="1280x720" value="1280x720" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="节目分组" prop="scheduleGroup">
            <el-select
              v-model="formData.scheduleGroup"
              placeholder="请选择节目分组"
              style="width: 100%"
              :disabled="dialogMode === 'view'"
            >
              <el-option label="默认分组" value="default" />
              <el-option label="重要节目" value="important" />
              <el-option label="普通节目" value="normal" />
            </el-select>
          </el-form-item>
          
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
import { ElMessage, ElMessageBox } from 'element-plus'

interface Schedule {
  id: string
  scheduleName: string
  applicationCode: string
  syncStatus: string
  resolution?: string
  scheduleGroup?: string
  remark?: string
  createTime: string
}

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref<'add' | 'edit' | 'view'>('add')

const searchForm = reactive({
  scheduleName: '',
  scheduleStatus: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 6
})

const formData = reactive({
  scheduleName: '',
  resolution: '',
  scheduleGroup: '',
  remark: ''
})

const formRules = {
  scheduleName: [{ required: true, message: '请输入节目名称', trigger: 'blur' }],
  resolution: [{ required: true, message: '请选择分辨率', trigger: 'change' }],
  scheduleGroup: [{ required: true, message: '请选择节目分组', trigger: 'change' }]
}

// 节目列表数据
const scheduleList = ref<Schedule[]>([
  {
    id: '1',
    scheduleName: '节目20241206173018',
    applicationCode: '20SJM20241206002',
    syncStatus: '同步中',
    createTime: '2024-12-06 17:30:18'
  },
  {
    id: '2',
    scheduleName: '节目20241206172746',
    applicationCode: '20SJM20241206001',
    syncStatus: '同步中',
    createTime: '2024-12-06 17:27:46'
  },
  {
    id: '3',
    scheduleName: '节目20240815112053',
    applicationCode: '20SJM20240815001',
    syncStatus: '同步中',
    createTime: '2024-08-15 11:20:53'
  },
  {
    id: '4',
    scheduleName: '节目20240424172117',
    applicationCode: '20SJM20240424001',
    syncStatus: '同步中',
    createTime: '2024-04-24 17:21:17'
  },
  {
    id: '5',
    scheduleName: '节目20240118172118',
    applicationCode: '20SJM20240118001',
    syncStatus: '同步成功',
    createTime: '2024-01-18 17:21:18'
  },
  {
    id: '6',
    scheduleName: '节目20240105172906',
    applicationCode: '20SJM20240105001',
    syncStatus: '同步成功',
    createTime: '2024-01-05 17:29:06'
  }
])

// 工具函数
const getStatusType = (status: string) => {
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

const getStatusText = (status: string) => {
  return status
}

// 事件处理
const handleSearch = () => {
  pagination.page = 1
  loadData()
  ElMessage.success('搜索完成')
}

const handleReset = () => {
  Object.assign(searchForm, {
    scheduleName: '',
    scheduleStatus: ''
  })
  handleSearch()
}

const handleCreate = () => {
  dialogMode.value = 'add'
  resetFormData()
  dialogVisible.value = true
}

const handleEdit = (schedule: Schedule) => {
  dialogMode.value = 'edit'
  Object.assign(formData, {
    scheduleName: schedule.scheduleName,
    resolution: schedule.resolution || '1920x1080',
    scheduleGroup: schedule.scheduleGroup || 'default',
    remark: schedule.remark || ''
  })
  dialogVisible.value = true
}

const handlePreview = (schedule: Schedule) => {
  ElMessage.info('预览功能开发中...')
}

const handleDelete = (schedule: Schedule) => {
  ElMessageBox.confirm('确定要删除这个节目吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = scheduleList.value.findIndex(item => item.id === schedule.id)
    if (index > -1) {
      scheduleList.value.splice(index, 1)
      pagination.total = scheduleList.value.length
      ElMessage.success('删除成功')
    }
  })
}

const handleSubmit = async () => {
  const newSchedule: Schedule = {
    id: `schedule_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    scheduleName: formData.scheduleName,
    applicationCode: `20SJM${new Date().getFullYear()}${String(new Date().getMonth() + 1).padStart(2, '0')}${String(new Date().getDate()).padStart(2, '0')}${String(scheduleList.value.length + 1).padStart(3, '0')}`,
    syncStatus: '同步中',
    resolution: formData.resolution,
    scheduleGroup: formData.scheduleGroup,
    remark: formData.remark,
    createTime: new Date().toLocaleString('zh-CN')
  }

  if (dialogMode.value === 'add') {
    scheduleList.value.unshift(newSchedule)
    ElMessage.success('创建成功')
  } else {
    ElMessage.success('更新成功')
  }
  
  dialogVisible.value = false
  pagination.total = scheduleList.value.length
}

const resetFormData = () => {
  Object.assign(formData, {
    scheduleName: '',
    resolution: '',
    scheduleGroup: '',
    remark: ''
  })
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
  pagination.total = scheduleList.value.length
  loadData()
})
</script>

<style scoped lang="scss">
.schedule-management {
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

  .schedule-section {
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

    .schedule-cards {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 20px;
      margin-bottom: 20px;

      .schedule-card {
        background: rgba(30, 58, 138, 0.3);
        border: 1px solid rgba(0, 212, 255, 0.2);
        border-radius: 8px;
        padding: 20px;
        transition: all 0.3s ease;

        &:hover {
          border-color: #00d4ff;
          background: rgba(30, 58, 138, 0.5);
          transform: translateY(-2px);
        }

        .card-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 16px;

          .schedule-icon {
            width: 48px;
            height: 48px;
            background: rgba(0, 212, 255, 0.1);
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
          }

          .schedule-status {
            flex-shrink: 0;
          }
        }

        .card-content {
          .schedule-title {
            margin: 0 0 12px 0;
            color: #00d4ff;
            font-size: 16px;
            font-weight: 600;
          }

          .schedule-info {
            margin-bottom: 16px;

            .info-item {
              display: flex;
              align-items: center;
              gap: 8px;
              color: #e2e8f0;
              font-size: 14px;

              .el-icon {
                color: #00d4ff;
                font-size: 16px;
              }
            }
          }

          .card-actions {
            display: flex;
            gap: 12px;
            justify-content: flex-end;
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
  .schedule-form {
    :deep(.el-form-item__label) {
      color: #374151;
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






