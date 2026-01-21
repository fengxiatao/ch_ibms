<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <h2>人员布控</h2>
        <p>智能人员布控管理和监控</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增布控
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :model="searchForm" label-width="80px" :inline="true">
        <el-form-item label="布控名称">
          <el-input v-model="searchForm.controlName" placeholder="请输入布控名称" clearable />
        </el-form-item>
        <el-form-item label="布控类型">
          <el-select v-model="searchForm.controlType" placeholder="请选择" clearable>
            <el-option label="人脸识别" value="face" />
            <el-option label="车牌识别" value="plate" />
            <el-option label="行为分析" value="behavior" />
          </el-select>
        </el-form-item>
        <el-form-item label="布控状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="启用" value="enabled" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="searchForm.createTime"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
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
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="controlList"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="controlName" label="布控名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="controlType" label="布控类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.controlType)">
              {{ getTypeText(row.controlType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetCount" label="目标数量" width="100" />
        <el-table-column prop="cameraCount" label="摄像头数量" width="120" />
        <el-table-column prop="alarmCount" label="告警次数" width="100" />
        <el-table-column prop="lastAlarmTime" label="最近告警" width="140" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="enabled"
              inactive-value="disabled"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
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

    <!-- 新增/编辑弹框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="布控名称" prop="controlName">
              <el-input v-model="formData.controlName" placeholder="请输入布控名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="布控类型" prop="controlType">
              <el-select v-model="formData.controlType" placeholder="请选择">
                <el-option label="人脸识别" value="face" />
                <el-option label="车牌识别" value="plate" />
                <el-option label="行为分析" value="behavior" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="formData.priority" placeholder="请选择">
                <el-option label="低" value="low" />
                <el-option label="中" value="medium" />
                <el-option label="高" value="high" />
                <el-option label="紧急" value="urgent" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="有效时间">
              <el-date-picker
                v-model="formData.validTime"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="目标人员库" prop="personnelLibrary">
          <el-select v-model="formData.personnelLibrary" multiple placeholder="请选择目标人员库">
            <el-option
              v-for="library in libraryOptions"
              :key="library.value"
              :label="library.label"
              :value="library.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="监控摄像头" prop="cameras">
          <el-select v-model="formData.cameras" multiple placeholder="请选择监控摄像头">
            <el-option
              v-for="camera in cameraOptions"
              :key="camera.value"
              :label="camera.label"
              :value="camera.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="告警方式">
          <el-checkbox-group v-model="formData.alarmMethods">
            <el-checkbox label="邮件通知" value="email" />
            <el-checkbox label="短信通知" value="sms" />
            <el-checkbox label="系统推送" value="push" />
            <el-checkbox label="语音提醒" value="voice" />
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="布控描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入布控描述"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, View, Edit, Delete } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMode = ref<'add' | 'edit' | 'view'>('add')

// 搜索表单
const searchForm = reactive({
  controlName: '',
  controlType: '',
  status: '',
  createTime: null
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表单数据
const formData = reactive({
  id: '',
  controlName: '',
  controlType: '',
  priority: '',
  validTime: null,
  personnelLibrary: [],
  cameras: [],
  alarmMethods: [],
  description: '',
  status: 'enabled'
})

// 表单验证规则
const formRules = {
  controlName: [{ required: true, message: '请输入布控名称', trigger: 'blur' }],
  controlType: [{ required: true, message: '请选择布控类型', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  personnelLibrary: [{ required: true, message: '请选择目标人员库', trigger: 'change' }],
  cameras: [{ required: true, message: '请选择监控摄像头', trigger: 'change' }]
}

// 表格数据
const controlList = ref([
  {
    id: '1',
    controlName: '重点人员监控',
    controlType: 'face',
    targetCount: 156,
    cameraCount: 24,
    alarmCount: 3,
    lastAlarmTime: '2024-01-20 14:30:25',
    status: 'enabled',
    priority: 'high',
    createTime: '2024-01-15 09:00:00'
  },
  {
    id: '2',
    controlName: '可疑车辆布控',
    controlType: 'plate',
    targetCount: 89,
    cameraCount: 16,
    alarmCount: 1,
    lastAlarmTime: '2024-01-19 16:45:12',
    status: 'enabled',
    priority: 'medium',
    createTime: '2024-01-16 10:30:00'
  },
  {
    id: '3',
    controlName: '异常行为检测',
    controlType: 'behavior',
    targetCount: 0,
    cameraCount: 32,
    alarmCount: 8,
    lastAlarmTime: '2024-01-20 11:20:48',
    status: 'enabled',
    priority: 'high',
    createTime: '2024-01-17 08:15:00'
  }
])

// 选项数据
const libraryOptions = ref([
  { label: '黑名单人员库', value: 'blacklist' },
  { label: '重点关注人员库', value: 'focus' },
  { label: '访客人员库', value: 'visitor' },
  { label: '员工人员库', value: 'employee' }
])

const cameraOptions = ref([
  { label: '大门入口摄像头01', value: 'camera_01' },
  { label: '大门入口摄像头02', value: 'camera_02' },
  { label: '停车场摄像头01', value: 'camera_03' },
  { label: '走廊摄像头01', value: 'camera_04' },
  { label: '电梯厅摄像头01', value: 'camera_05' }
])

const formRef = ref()

// 获取类型颜色
const getTypeColor = (type: string) => {
  const colors = {
    face: 'primary',
    plate: 'success',
    behavior: 'warning'
  }
  return colors[type] || 'info'
}

// 获取类型文本
const getTypeText = (type: string) => {
  const texts = {
    face: '人脸识别',
    plate: '车牌识别',
    behavior: '行为分析'
  }
  return texts[type] || type
}

// 事件处理
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    controlName: '',
    controlType: '',
    status: '',
    createTime: null
  })
  handleSearch()
}

const handleCreate = () => {
  dialogMode.value = 'add'
  dialogTitle.value = '新增布控'
  resetFormData()
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑布控'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleView = (row: any) => {
  dialogMode.value = 'view'
  dialogTitle.value = '查看布控'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该布控吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 这里应该调用删除API
    const index = controlList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      controlList.value.splice(index, 1)
    }
    
    ElMessage.success('删除成功')
  } catch {
    // 用户取消删除
  }
}

const handleStatusChange = (row: any) => {
  ElMessage.success(`${row.status === 'enabled' ? '启用' : '禁用'}成功`)
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (dialogMode.value === 'add') {
      const newControl = {
        ...formData,
        id: `control_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        targetCount: 0,
        alarmCount: 0,
        lastAlarmTime: '-',
        createTime: new Date().toLocaleString('zh-CN')
      }
      controlList.value.unshift(newControl)
      ElMessage.success('创建成功')
    } else if (dialogMode.value === 'edit') {
      const index = controlList.value.findIndex(item => item.id === formData.id)
      if (index > -1) {
        Object.assign(controlList.value[index], formData)
      }
      ElMessage.success('更新成功')
    }
    
    dialogVisible.value = false
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const resetFormData = () => {
  Object.assign(formData, {
    id: '',
    controlName: '',
    controlType: '',
    priority: '',
    validTime: null,
    personnelLibrary: [],
    cameras: [],
    alarmMethods: [],
    description: '',
    status: 'enabled'
  })
}

const loadData = () => {
  loading.value = true
  // 模拟API调用
  setTimeout(() => {
    pagination.total = controlList.value.length
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

.el-dialog {
  .el-form {
    .el-input,
    .el-select {
      width: 100%;
    }
  }
}
</style>






