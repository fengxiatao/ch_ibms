<template>
  <ContentWrap style="margin-top: 70px;">
  <div class="emergency-drill-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧消防</el-breadcrumb-item>
        <el-breadcrumb-item>应急管理</el-breadcrumb-item>
        <el-breadcrumb-item>应急演练</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="100px">
        <el-form-item label="请输入演练名称" prop="drillName">
          <el-input
            v-model="queryParams.drillName"
            placeholder="请输入演练名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="请选择演练类型" prop="drillType">
          <el-select v-model="queryParams.drillType" placeholder="请选择演练类型" clearable style="width: 160px">
            <el-option label="全部类型" value="" />
            <el-option label="消防演练" value="fire_drill" />
            <el-option label="地震演练" value="earthquake_drill" />
            <el-option label="综合演练" value="comprehensive_drill" />
          </el-select>
        </el-form-item>
        <el-form-item label="请选择演练状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择演练状态" clearable style="width: 160px">
            <el-option label="全部状态" value="" />
            <el-option label="计划中" value="planned" />
            <el-option label="进行中" value="ongoing" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
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
        <h3>应急演练</h3>
      </div>
      
      <el-table
        v-loading="loading"
        :data="drillList"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column prop="drillCode" label="演练编号" width="150" show-overflow-tooltip />
        <el-table-column prop="drillName" label="演练名称" width="200" show-overflow-tooltip />
        <el-table-column prop="drillType" label="演练类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getDrillTypeColor(row.drillType)" size="small">
              {{ getDrillTypeText(row.drillType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="演练地点" width="150" show-overflow-tooltip />
        <el-table-column prop="plannedDate" label="计划时间" width="160" show-overflow-tooltip />
        <el-table-column prop="duration" label="演练时长" width="100" align="center">
          <template #default="{ row }">
            {{ row.duration }}分钟
          </template>
        </el-table-column>
        <el-table-column prop="participants" label="参与人数" width="100" align="center">
          <template #default="{ row }">
            {{ row.participants }}人
          </template>
        </el-table-column>
        <el-table-column prop="status" label="演练状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="演练评分" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.score" class="score-text" :class="getScoreClass(row.score)">
              {{ row.score }}分
            </span>
            <span v-else class="no-score">未评分</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)" :icon="View">
              查看
            </el-button>
            <el-button 
              v-if="row.status === 'planned'" 
              type="success" 
              size="small" 
              @click="handleStart(row)"
              :icon="VideoPlay"
            >
              开始
            </el-button>
            <el-button 
              v-if="row.status === 'completed' && !row.score" 
              type="warning" 
              size="small" 
              @click="handleEvaluate(row)"
              :icon="Star"
            >
              评分
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
      width="800px" 
      append-to-body
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="演练类型" prop="drillType">
              <el-select v-model="form.drillType" placeholder="请选择演练类型" style="width: 100%">
                <el-option label="消防演练" value="fire_drill" />
                <el-option label="地震演练" value="earthquake_drill" />
                <el-option label="综合演练" value="comprehensive_drill" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="演练地点" prop="location">
              <el-input v-model="form.location" placeholder="请输入演练地点" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="演练名称" prop="drillName">
          <el-input v-model="form.drillName" placeholder="请输入演练名称" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划时间" prop="plannedDate">
              <el-date-picker
                v-model="form.plannedDate"
                type="datetime"
                placeholder="选择计划时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="演练时长" prop="duration">
              <el-input-number 
                v-model="form.duration" 
                placeholder="请输入演练时长" 
                :min="1" 
                :max="480"
                style="width: 100%"
              />
              <span style="margin-left: 8px; color: #999;">分钟</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="参与人数" prop="participants">
              <el-input-number 
                v-model="form.participants" 
                placeholder="请输入参与人数" 
                :min="1"
                style="width: 100%"
              />
              <span style="margin-left: 8px; color: #999;">人</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人" prop="supervisor">
              <el-input v-model="form.supervisor" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="演练目标" prop="objective">
          <el-input
            v-model="form.objective"
            type="textarea"
            :rows="3"
            placeholder="请输入演练目标"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="演练内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请输入演练内容"
            maxlength="1000"
            show-word-limit
          />
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

    <!-- 评分弹窗 -->
    <el-dialog v-model="evaluateVisible" title="演练评分" width="600px" append-to-body>
      <el-form :model="evaluateForm" :rules="evaluateRules" ref="evaluateFormRef" label-width="120px">
        <el-form-item label="演练名称">
          <span>{{ currentDrill?.drillName }}</span>
        </el-form-item>
        <el-form-item label="演练评分" prop="score">
          <el-rate
            v-model="evaluateForm.rateScore"
            :max="5"
            show-score
            text-color="#ff9900"
            score-template="{value} 星"
            @change="handleRateChange"
          />
          <div style="margin-top: 10px;">
            <el-input-number 
              v-model="evaluateForm.score" 
              :min="0" 
              :max="100" 
              :precision="1"
              style="width: 120px;"
            />
            <span style="margin-left: 8px; color: #999;">分 (0-100分)</span>
          </div>
        </el-form-item>
        <el-form-item label="评价等级" prop="level">
          <el-select v-model="evaluateForm.level" placeholder="请选择评价等级" style="width: 200px">
            <el-option label="优秀" value="excellent" />
            <el-option label="良好" value="good" />
            <el-option label="合格" value="qualified" />
            <el-option label="不合格" value="unqualified" />
          </el-select>
        </el-form-item>
        <el-form-item label="评价内容" prop="evaluation">
          <el-input
            v-model="evaluateForm.evaluation"
            type="textarea"
            :rows="4"
            placeholder="请输入评价内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="改进建议" prop="suggestions">
          <el-input
            v-model="evaluateForm.suggestions"
            type="textarea"
            :rows="3"
            placeholder="请输入改进建议"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="evaluateVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEvaluate" :loading="evaluateLoading">
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
  Search, Refresh, Plus, View, Edit, Delete, VideoPlay, Star
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const formLoading = ref(false)
const evaluateLoading = ref(false)
const total = ref(0)
const drillList = ref<any[]>([])
const formVisible = ref(false)
const evaluateVisible = ref(false)
const selectedRows = ref([])
const isEdit = ref(false)
const currentDrill = ref(null)

// 查询表单
const queryFormRef = ref()
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  drillName: '',
  drillType: '',
  status: ''
})

// 表单数据
const formRef = ref()
const form = reactive({
  id: null,
  drillCode: '',
  drillName: '',
  drillType: '',
  location: '',
  plannedDate: '',
  duration: 60,
  participants: 1,
  supervisor: '',
  objective: '',
  content: '',
  status: 'planned'
})

const formRules = {
  drillType: [
    { required: true, message: '请选择演练类型', trigger: 'change' }
  ],
  drillName: [
    { required: true, message: '请输入演练名称', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入演练地点', trigger: 'blur' }
  ],
  plannedDate: [
    { required: true, message: '请选择计划时间', trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请输入演练时长', trigger: 'blur' }
  ],
  participants: [
    { required: true, message: '请输入参与人数', trigger: 'blur' }
  ],
  supervisor: [
    { required: true, message: '请输入负责人', trigger: 'blur' }
  ]
}

// 评分表单
const evaluateFormRef = ref()
const evaluateForm = reactive({
  rateScore: 0,
  score: 0,
  level: '',
  evaluation: '',
  suggestions: ''
})

const evaluateRules = {
  score: [
    { required: true, message: '请输入评分', trigger: 'blur' }
  ],
  level: [
    { required: true, message: '请选择评价等级', trigger: 'change' }
  ],
  evaluation: [
    { required: true, message: '请输入评价内容', trigger: 'blur' }
  ]
}

// 计算属性
const formTitle = computed(() => {
  return isEdit.value ? '编辑演练' : '新增演练'
})

// 模拟数据
const mockData = [
  {
    id: 1,
    drillCode: 'DRILL-2024-001',
    drillName: '消防疏散演练',
    drillType: 'fire_drill',
    location: 'A座办公楼',
    plannedDate: '2024-10-15 14:30:00',
    duration: 90,
    participants: 150,
    supervisor: '张三',
    objective: '提高员工消防安全意识，熟练掌握疏散路线',
    content: '模拟火灾场景，组织全员疏散演练',
    status: 'completed',
    score: 85.5,
    level: 'good',
    evaluation: '演练整体执行良好，疏散有序',
    suggestions: '建议加强楼梯疏散引导'
  },
  {
    id: 2,
    drillCode: 'DRILL-2024-002',
    drillName: '地震应急演练',
    drillType: 'earthquake_drill',
    location: 'B座办公楼',
    plannedDate: '2024-10-20 10:00:00',
    duration: 60,
    participants: 80,
    supervisor: '李四',
    objective: '提高地震应急响应能力',
    content: '模拟地震场景，组织避震和疏散',
    status: 'planned',
    score: null
  },
  {
    id: 3,
    drillCode: 'DRILL-2024-003',
    drillName: '综合应急演练',
    drillType: 'comprehensive_drill',
    location: '整个园区',
    plannedDate: '2024-10-25 09:00:00',
    duration: 120,
    participants: 300,
    supervisor: '王五',
    objective: '综合检验应急预案执行效果',
    content: '多场景综合应急演练',
    status: 'ongoing',
    score: null
  }
]

// 方法
const getDrillTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    'fire_drill': 'danger',
    'earthquake_drill': 'warning',
    'comprehensive_drill': 'success'
  }
  return colorMap[type] || ''
}

const getDrillTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    'fire_drill': '消防演练',
    'earthquake_drill': '地震演练',
    'comprehensive_drill': '综合演练'
  }
  return textMap[type] || type
}

const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'planned': 'info',
    'ongoing': 'warning',
    'completed': 'success',
    'cancelled': 'danger'
  }
  return colorMap[status] || ''
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'planned': '计划中',
    'ongoing': '进行中',
    'completed': '已完成',
    'cancelled': '已取消'
  }
  return textMap[status] || status
}

const getScoreClass = (score: number) => {
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 60) return 'score-qualified'
  return 'score-unqualified'
}

const getList = () => {
  loading.value = true
  
  // 模拟API调用
  setTimeout(() => {
    let filteredData = [...mockData]
    
    // 简单的筛选逻辑
    if (queryParams.drillName) {
      filteredData = filteredData.filter(item => 
        item.drillName.includes(queryParams.drillName)
      )
    }
    if (queryParams.drillType) {
      filteredData = filteredData.filter(item => item.drillType === queryParams.drillType)
    }
    if (queryParams.status) {
      filteredData = filteredData.filter(item => item.status === queryParams.status)
    }
    
    total.value = filteredData.length
    drillList.value = filteredData.slice(
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
  form.drillCode = ''
  form.drillName = ''
  form.drillType = ''
  form.location = ''
  form.plannedDate = ''
  form.duration = 60
  form.participants = 1
  form.supervisor = ''
  form.objective = ''
  form.content = ''
  form.status = 'planned'
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

const handleStart = (row: any) => {
  ElMessageBox.confirm(
    `确定要开始演练"${row.drillName}"吗？`,
    '系统提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    // 更新状态为进行中
    const index = drillList.value.findIndex(item => item.id === row.id)
    if (index !== -1) {
      drillList.value[index].status = 'ongoing'
      ElMessage.success('演练已开始')
    }
  })
}

const handleEvaluate = (row: any) => {
  currentDrill.value = row
  
  // 重置评分表单
  evaluateForm.rateScore = 0
  evaluateForm.score = 0
  evaluateForm.level = ''
  evaluateForm.evaluation = ''
  evaluateForm.suggestions = ''
  
  evaluateVisible.value = true
}

const handleRateChange = (value: number) => {
  // 星级评分转换为百分制
  evaluateForm.score = value * 20
  
  // 根据评分设置等级
  if (value >= 4.5) {
    evaluateForm.level = 'excellent'
  } else if (value >= 3.5) {
    evaluateForm.level = 'good'
  } else if (value >= 2.5) {
    evaluateForm.level = 'qualified'
  } else {
    evaluateForm.level = 'unqualified'
  }
}

const submitEvaluate = () => {
  evaluateFormRef.value?.validate((valid: boolean) => {
    if (valid) {
      evaluateLoading.value = true
      
      // 模拟API调用
      setTimeout(() => {
        // 更新演练评分
        const index = drillList.value.findIndex(item => item.id === currentDrill.value.id)
        if (index !== -1) {
          drillList.value[index].score = evaluateForm.score
          drillList.value[index].level = evaluateForm.level
          drillList.value[index].evaluation = evaluateForm.evaluation
          drillList.value[index].suggestions = evaluateForm.suggestions
        }
        
        ElMessage.success('评分成功')
        evaluateLoading.value = false
        evaluateVisible.value = false
      }, 1000)
    }
  })
}

const submitForm = () => {
  formRef.value?.validate((valid: boolean) => {
    if (valid) {
      formLoading.value = true
      
      // 模拟API调用
      setTimeout(() => {
        if (isEdit.value) {
          // 更新数据
          const index = drillList.value.findIndex(item => item.id === form.id)
          if (index !== -1) {
            drillList.value[index] = { ...form }
          }
          ElMessage.success('更新成功')
        } else {
          // 新增数据
          const newDrill = {
            ...form,
            id: Date.now(),
            drillCode: `DRILL-${new Date().getFullYear()}-${String(Date.now()).slice(-3)}`,
            score: null
          }
          drillList.value.unshift(newDrill)
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
    `确定要删除演练"${row.drillName}"吗？`,
    '系统提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 模拟删除操作
    const index = drillList.value.findIndex(item => item.id === row.id)
    if (index !== -1) {
      drillList.value.splice(index, 1)
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

.emergency-drill-container {
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

  .score-text {
    font-weight: bold;
    
    &.score-excellent {
      color: #67c23a;
    }
    
    &.score-good {
      color: #409eff;
    }
    
    &.score-qualified {
      color: #e6a23c;
    }
    
    &.score-unqualified {
      color: #f56c6c;
    }
  }

  .no-score {
    color: #999;
    font-style: italic;
  }
}
</style>
