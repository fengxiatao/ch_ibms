<template>
  <ContentWrap style="margin-top: 70px;">
  <div class="plan-management-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧消防</el-breadcrumb-item>
        <el-breadcrumb-item>应急管理</el-breadcrumb-item>
        <el-breadcrumb-item>预案管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="100px">
        <el-form-item label="请输入预案名称" prop="planName">
          <el-input
            v-model="queryParams.planName"
            placeholder="请输入预案名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="请选择预案类型" prop="planType">
          <el-select v-model="queryParams.planType" placeholder="请选择预案类型" clearable style="width: 160px">
            <el-option label="全部类型" value="" />
            <el-option label="公司总体应急预案" value="company_general" />
            <el-option label="消防火灾应急预案" value="fire_emergency" />
            <el-option label="专项应急预案" value="special_emergency" />
          </el-select>
        </el-form-item>
        <el-form-item label="请选择事件类型" prop="eventType">
          <el-select v-model="queryParams.eventType" placeholder="请选择事件类型" clearable style="width: 160px">
            <el-option label="全部事件" value="" />
            <el-option label="火灾应急" value="fire" />
            <el-option label="地震应急" value="earthquake" />
            <el-option label="停电应急" value="power_outage" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :icon="Search">搜索</el-button>
          <el-button @click="resetQuery" :icon="Refresh">重置</el-button>
          <el-button type="primary" @click="handleAdd" :icon="Plus">新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <h3>预案管理</h3>
      </div>
      
      <el-table
        v-loading="loading"
        :data="planList"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column prop="planCode" label="预案编号" width="150" show-overflow-tooltip />
        <el-table-column prop="planName" label="预案名称" width="200" show-overflow-tooltip />
        <el-table-column prop="planType" label="预案类型" width="150">
          <template #default="{ row }">
            {{ getPlanTypeText(row.planType) }}
          </template>
        </el-table-column>
        <el-table-column prop="eventType" label="事件类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getEventTypeColor(row.eventType)" size="small">
              {{ getEventTypeText(row.eventType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="组织部门" width="120" show-overflow-tooltip />
        <el-table-column prop="coordinator" label="协助部门" width="120" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建日期" width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)" :icon="View">
              查看
            </el-button>
            <el-button type="success" size="small" @click="handleEdit(row)" :icon="Edit">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)" :icon="Delete">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-show="total > 0"
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :small="false"
          :disabled="loading"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog 
      v-model="formVisible" 
      :title="formTitle" 
      width="900px" 
      append-to-body
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预案类型" prop="planType">
              <el-select v-model="form.planType" placeholder="请选择预案类型" style="width: 100%">
                <el-option label="公司总体应急预案" value="company_general" />
                <el-option label="消防火灾应急预案" value="fire_emergency" />
                <el-option label="专项应急预案" value="special_emergency" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="事件类型" prop="eventType">
              <el-select v-model="form.eventType" placeholder="请选择事件类型" style="width: 100%">
                <el-option label="火灾应急" value="fire" />
                <el-option label="地震应急" value="earthquake" />
                <el-option label="停电应急" value="power_outage" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="预案名称" prop="planName">
          <el-input v-model="form.planName" placeholder="请输入预案名称" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组织部门" prop="department">
              <el-select v-model="form.department" placeholder="请选择组织部门" style="width: 100%">
                <el-option label="行政部" value="admin" />
                <el-option label="安全部" value="safety" />
                <el-option label="工程部" value="engineering" />
                <el-option label="物业部" value="property" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="协助部门" prop="coordinator">
              <el-input v-model="form.coordinator" placeholder="请输入协助部门" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="预案内容" prop="content">
          <div class="editor-container">
            <div class="editor-toolbar">
              <span>文件</span>
              <span>编辑</span>
              <span>插入</span>
              <span>查看</span>
              <span>格式</span>
              <span>表格</span>
              <span>工具</span>
            </div>
            <div class="editor-content" contenteditable="true" @input="handleContentChange">
              {{ form.content }}
            </div>
          </div>
        </el-form-item>
        <el-form-item label="协助部门图">
          <div class="upload-area">
            <el-icon class="upload-icon"><Plus /></el-icon>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="formVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="formLoading">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Refresh, Plus, View, Edit, Delete 
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const formLoading = ref(false)
const total = ref(0)
const planList = ref<any[]>([])
const formVisible = ref(false)
const selectedRows = ref([])
const isEdit = ref(false)

// 查询表单
const queryFormRef = ref()
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  planName: '',
  planType: '',
  eventType: ''
})

// 表单数据
const formRef = ref()
const form = reactive({
  id: null,
  planCode: '',
  planName: '',
  planType: '',
  eventType: '',
  department: '',
  coordinator: '',
  content: ''
})

const formRules = {
  planType: [
    { required: true, message: '请选择预案类型', trigger: 'change' }
  ],
  eventType: [
    { required: true, message: '请选择事件类型', trigger: 'change' }
  ],
  planName: [
    { required: true, message: '请输入预案名称', trigger: 'blur' }
  ],
  department: [
    { required: true, message: '请选择组织部门', trigger: 'change' }
  ]
}

// 计算属性
const formTitle = computed(() => {
  return isEdit.value ? '编辑预案' : '新增预案'
})

// 模拟数据
const mockData = [
  {
    id: 1,
    planCode: '20SYA20240210002',
    planName: '公司总体应急预案',
    planType: 'company_general',
    eventType: 'fire',
    department: '行政部',
    coordinator: '-',
    createTime: '2024-02-21 14:26:36',
    content: '公司总体应急预案内容...'
  },
  {
    id: 2,
    planCode: '20SYA20240210001', 
    planName: '消防火灾应急预案',
    planType: 'fire_emergency',
    eventType: 'fire',
    department: '安全部',
    coordinator: '-',
    createTime: '2024-02-21 14:22:49',
    content: '消防火灾应急预案内容...'
  },
  {
    id: 3,
    planCode: '20SYA20241080001',
    planName: '火灾消防应急预案',
    planType: 'special_emergency', 
    eventType: 'fire',
    department: '安全部',
    coordinator: '物业部',
    createTime: '2024-01-08 14:25:33',
    content: '火灾消防应急预案内容...'
  }
]

// 方法
const getPlanTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    'company_general': '公司总体应急预案',
    'fire_emergency': '消防火灾应急预案',
    'special_emergency': '专项应急预案'
  }
  return textMap[type] || type
}

const getEventTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    'fire': 'danger',
    'earthquake': 'warning',
    'power_outage': 'info'
  }
  return colorMap[type] || ''
}

const getEventTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    'fire': '火灾应急',
    'earthquake': '地震应急',
    'power_outage': '停电应急'
  }
  return textMap[type] || type
}

const getList = () => {
  loading.value = true
  
  // 模拟API调用
  setTimeout(() => {
    let filteredData = [...mockData]
    
    // 简单的筛选逻辑
    if (queryParams.planName) {
      filteredData = filteredData.filter(item => 
        item.planName.includes(queryParams.planName)
      )
    }
    if (queryParams.planType) {
      filteredData = filteredData.filter(item => item.planType === queryParams.planType)
    }
    if (queryParams.eventType) {
      filteredData = filteredData.filter(item => item.eventType === queryParams.eventType)
    }
    
    total.value = filteredData.length
    planList.value = filteredData.slice(
      (queryParams.pageNum - 1) * queryParams.pageSize,
      queryParams.pageNum * queryParams.pageSize
    )
    loading.value = false
  }, 500)
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
}

const handleSizeChange = (size: number) => {
  queryParams.pageSize = size
  getList()
}

const handleCurrentChange = (page: number) => {
  queryParams.pageNum = page
  getList()
}

const resetForm = () => {
  form.id = null
  form.planCode = ''
  form.planName = ''
  form.planType = ''
  form.eventType = ''
  form.department = ''
  form.coordinator = ''
  form.content = ''
}

const handleAdd = () => {
  resetForm()
  isEdit.value = false
  formVisible.value = true
}

const handleView = (row: any) => {
  ElMessage.info('查看功能开发中...')
}

const handleEdit = (row: any) => {
  resetForm()
  isEdit.value = true
  
  // 填充表单数据
  Object.assign(form, row)
  
  formVisible.value = true
}

const handleContentChange = (event: Event) => {
  const target = event.target as HTMLElement
  form.content = target.textContent || ''
}

const submitForm = () => {
  formRef.value?.validate((valid: boolean) => {
    if (valid) {
      formLoading.value = true
      
      // 模拟API调用
      setTimeout(() => {
        if (isEdit.value) {
          // 更新数据
          const index = planList.value.findIndex(item => item.id === form.id)
          if (index !== -1) {
            planList.value[index] = { ...form }
          }
          ElMessage.success('更新成功')
        } else {
          // 新增数据
          const newPlan = {
            ...form,
            id: Date.now(),
            planCode: `20SYA${new Date().getFullYear()}${String(Date.now()).slice(-8)}`,
            createTime: new Date().toLocaleString()
          }
          planList.value.unshift(newPlan)
          total.value++
          ElMessage.success('新增成功')
        }
        
        formLoading.value = false
        formVisible.value = false
      }, 1000)
    }
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除预案"${row.planName}"吗？`,
    '系统提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 模拟删除操作
    const index = planList.value.findIndex(item => item.id === row.id)
    if (index !== -1) {
      planList.value.splice(index, 1)
      total.value--
      ElMessage.success('删除成功')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>@use '@/styles/dark-theme.scss';

.plan-management-container {
  padding: 20px;
  background: #0a1628 !important;
  min-height: 100vh;
  
  :deep(.el-card) {
    background: rgba(255, 255, 255, 0.05) !important;
    border: 1px solid rgba(255, 255, 255, 0.1) !important;
    color: #ffffff !important;
  }
  
  :deep(.el-card__body) {
    background: transparent !important;
    color: #ffffff !important;
  }
  
  :deep(.el-breadcrumb) {
    color: #ffffff !important;
  }
  
  :deep(.el-breadcrumb__item) {
    color: #ffffff !important;
  }
  
  :deep(.el-table) {
    background: rgba(255, 255, 255, 0.05) !important;
    color: #ffffff !important;
  }
  
  :deep(.el-table__header) {
    background: rgba(255, 255, 255, 0.1) !important;
    color: #ffffff !important;
  }
  
  :deep(.el-table__body) {
    background: transparent !important;
    color: #ffffff !important;
  }
  
  :deep(.el-table td) {
    border-color: rgba(255, 255, 255, 0.1) !important;
    color: #ffffff !important;
  }
  
  :deep(.el-table th) {
    border-color: rgba(255, 255, 255, 0.1) !important;
    color: #ffffff !important;
    background: rgba(255, 255, 255, 0.1) !important;
  }
  
  :deep(.el-form-item__label) {
    color: #ffffff !important;
  }
  
  :deep(.el-input__inner) {
    background: rgba(255, 255, 255, 0.1) !important;
    border-color: rgba(255, 255, 255, 0.2) !important;
    color: #ffffff !important;
  }
  
  :deep(.el-select) {
    color: #ffffff !important;
  }
  
  :deep(.el-pagination) {
    color: #ffffff !important;
  }

  .breadcrumb-container {
    margin-bottom: 20px;
  }

  .search-card {
    margin-bottom: 20px;

    :deep(.el-card__body) {
      padding-bottom: 10px;
    }
  }

  .table-card {
    .table-header {
      margin-bottom: 16px;
      
      h3 {
        margin: 0;
        font-size: 16px;
        color: #303133;
      }
    }
  }

  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }

  .dialog-footer {
    text-align: right;
  }

  .editor-container {
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    overflow: hidden;

    .editor-toolbar {
      background: #f5f7fa;
      padding: 8px 12px;
      border-bottom: 1px solid #dcdfe6;
      font-size: 12px;
      
      span {
        margin-right: 16px;
        cursor: pointer;
        color: #606266;
        
        &:hover {
          color: #409eff;
        }
      }
    }

    .editor-content {
      min-height: 200px;
      padding: 12px;
      background: #1a1a1a;
      font-size: 14px;
      line-height: 1.5;
      color: #606266;
      outline: none;
    }
  }

  .upload-area {
    width: 100px;
    height: 100px;
    border: 2px dashed #dcdfe6;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: border-color 0.3s;

    &:hover {
      border-color: #409eff;
    }

    .upload-icon {
      font-size: 24px;
      color: #c0c4cc;
    }
  }
}
</style>
