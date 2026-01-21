<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="realtime-broadcast">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <Icon icon="ep:microphone" :size="24" />
        <h1>实时播放</h1>
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
            v-model="searchForm.taskName"
            placeholder="请输入任务名称"
            clearable
            style="width: 200px"
          />
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

    <!-- 实时播放列表 -->
    <div class="broadcast-section">
      <h2>实时播放</h2>
      
      <!-- 空数据状态 -->
      <div class="empty-state">
        <div class="empty-content">
          <Icon icon="ep:folder-opened" :size="80" color="#909399" />
          <p class="empty-text">暂无数据</p>
        </div>
      </div>
    </div>

    <!-- 新增实时播放弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="新增实时播放"
      width="600px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div class="broadcast-form">
        <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
          <el-form-item label="任务名称" prop="taskName">
            <el-input
              v-model="formData.taskName"
              placeholder="请输入任务名称"
            />
          </el-form-item>
          
          <el-form-item label="优先级" prop="priority">
            <el-input-number
              v-model="formData.priority"
              :min="1"
              :max="10"
              placeholder="优先级"
              style="width: 100%"
            />
          </el-form-item>
          
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
          
          <el-form-item label="媒体文件" prop="mediaFile">
            <el-select
              v-model="formData.mediaFile"
              placeholder="请选择媒体文件"
              style="width: 100%"
            >
              <el-option label="紧急通知.mp3" value="urgent.mp3" />
              <el-option label="背景音乐.mp3" value="background.mp3" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="播放终端">
            <el-button type="primary" @click="showTerminalSelector">
              选择播放终端
            </el-button>
            <span class="device-count">已选择 {{ selectedTerminals.length }} 个设备</span>
          </el-form-item>
          
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
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确认</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 终端选择弹窗 -->
    <el-dialog
      v-model="terminalSelectorVisible"
      title="选择播放终端"
      width="800px"
      destroy-on-close
    >
      <div class="terminal-selector">
        <el-table
          ref="terminalTableRef"
          :data="availableTerminals"
          style="width: 100%"
          @selection-change="handleTerminalSelection"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="deviceName" label="设备名称" min-width="150" />
          <el-table-column prop="location" label="位置" width="200" />
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
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

// 响应式数据
const dialogVisible = ref(false)
const terminalSelectorVisible = ref(false)

const searchForm = reactive({
  taskName: ''
})

const formData = reactive({
  taskName: '',
  priority: 1,
  playMode: '',
  mediaFile: '',
  remark: ''
})

const formRules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  priority: [{ required: true, message: '请输入优先级', trigger: 'blur' }],
  playMode: [{ required: true, message: '请选择播放模式', trigger: 'change' }],
  mediaFile: [{ required: true, message: '请选择媒体文件', trigger: 'change' }]
}

const selectedTerminals = ref<any[]>([])

// 可选终端设备
const availableTerminals = ref([
  { id: '1', deviceName: '广播001', location: '一楼大厅', status: '在线' },
  { id: '2', deviceName: '广播002', location: '二楼办公区', status: '在线' },
  { id: '3', deviceName: '广播003', location: '三楼会议室', status: '离线' }
])

// 事件处理
const handleSearch = () => {
  ElMessage.success('搜索完成')
}

const handleReset = () => {
  searchForm.taskName = ''
}

const handleCreate = () => {
  resetFormData()
  dialogVisible.value = true
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

const handleSubmit = () => {
  if (selectedTerminals.value.length === 0) {
    ElMessage.warning('请选择至少一个播放终端')
    return
  }
  
  ElMessage.success('实时播放任务创建成功')
  dialogVisible.value = false
}

const resetFormData = () => {
  Object.assign(formData, {
    taskName: '',
    priority: 1,
    playMode: '',
    mediaFile: '',
    remark: ''
  })
  selectedTerminals.value = []
}
</script>

<style scoped lang="scss">
.realtime-broadcast {
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
  }

  .broadcast-form {
    :deep(.el-form-item__label) {
      color: #374151;
    }

    .device-count {
      margin-left: 12px;
      color: #6b7280;
      font-size: 14px;
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






