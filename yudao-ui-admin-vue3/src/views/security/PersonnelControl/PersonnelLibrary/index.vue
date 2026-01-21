<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <h2>人员库管理</h2>
        <p>管理和维护人员识别库信息</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建人员库
        </el-button>
        <el-button type="success" @click="handleImport">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :model="searchForm" label-width="80px" :inline="true">
        <el-form-item label="库名称">
          <el-input v-model="searchForm.libraryName" placeholder="请输入库名称" clearable />
        </el-form-item>
        <el-form-item label="库类型">
          <el-select v-model="searchForm.libraryType" placeholder="请选择" clearable>
            <el-option label="黑名单" value="blacklist" />
            <el-option label="白名单" value="whitelist" />
            <el-option label="访客" value="visitor" />
            <el-option label="员工" value="employee" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="启用" value="enabled" />
            <el-option label="禁用" value="disabled" />
          </el-select>
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
        :data="libraryList"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="libraryName" label="库名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="libraryType" label="库类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.libraryType)">
              {{ getTypeText(row.libraryType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="personCount" label="人员数量" width="100" />
        <el-table-column prop="lastUpdateTime" label="最后更新" width="140" show-overflow-tooltip />
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
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewPersons(row)">
              <el-icon><User /></el-icon>
              人员管理
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
      width="600px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="库名称" prop="libraryName">
          <el-input v-model="formData.libraryName" placeholder="请输入库名称" />
        </el-form-item>

        <el-form-item label="库类型" prop="libraryType">
          <el-select v-model="formData.libraryType" placeholder="请选择">
            <el-option label="黑名单" value="blacklist" />
            <el-option label="白名单" value="whitelist" />
            <el-option label="访客" value="visitor" />
            <el-option label="员工" value="employee" />
          </el-select>
        </el-form-item>

        <el-form-item label="库描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入库描述"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 人员管理弹框 -->
    <el-dialog
      v-model="personDialogVisible"
      title="人员管理"
      width="1200px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div class="person-management">
        <div class="person-toolbar">
          <el-button type="primary" @click="handleAddPerson">
            <el-icon><Plus /></el-icon>
            添加人员
          </el-button>
          <el-button type="success" @click="handleImportPersons">
            <el-icon><Upload /></el-icon>
            批量导入
          </el-button>
          <el-input
            v-model="personSearchText"
            placeholder="搜索人员姓名..."
            style="width: 200px; margin-left: auto;"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <el-table
          :data="personList"
          stripe
          border
          style="width: 100%; margin-top: 16px;"
        >
          <el-table-column prop="avatar" label="头像" width="80">
            <template #default="{ row }">
              <el-avatar :src="row.avatar" :size="50">{{ row.name.charAt(0) }}</el-avatar>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" width="100" />
          <el-table-column prop="gender" label="性别" width="60" />
          <el-table-column prop="age" label="年龄" width="60" />
          <el-table-column prop="idCard" label="身份证号" width="160" show-overflow-tooltip />
          <el-table-column prop="phone" label="联系电话" width="120" />
          <el-table-column prop="department" label="部门" width="120" show-overflow-tooltip />
          <el-table-column prop="addTime" label="添加时间" width="140" show-overflow-tooltip />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleEditPerson(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button link type="danger" @click="handleDeletePerson(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="personPagination.page"
          v-model:page-size="personPagination.size"
          :total="personPagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          style="margin-top: 16px; text-align: right;"
        />
      </div>
    </el-dialog>

    <!-- 添加/编辑人员弹框 -->
    <el-dialog
      v-model="personFormDialogVisible"
      :title="personFormTitle"
      width="500px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form
        ref="personFormRef"
        :model="personFormData"
        :rules="personFormRules"
        label-width="80px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="personFormData.name" placeholder="请输入姓名" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="personFormData.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="personFormData.age" :min="1" :max="120" />
        </el-form-item>

        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="personFormData.idCard" placeholder="请输入身份证号" />
        </el-form-item>

        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="personFormData.phone" placeholder="请输入联系电话" />
        </el-form-item>

        <el-form-item label="部门">
          <el-input v-model="personFormData.department" placeholder="请输入部门" />
        </el-form-item>

        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="handleAvatarChange"
          >
            <img v-if="personFormData.avatar" :src="personFormData.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="personFormDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePersonSubmit">确认</el-button>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Upload, User } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMode = ref<'add' | 'edit'>('add')
const personDialogVisible = ref(false)
const personFormDialogVisible = ref(false)
const personFormTitle = ref('')
const personSearchText = ref('')

// 搜索表单
const searchForm = reactive({
  libraryName: '',
  libraryType: '',
  status: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const personPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表单数据
const formData = reactive({
  id: '',
  libraryName: '',
  libraryType: '',
  description: '',
  status: 'enabled'
})

const personFormData = reactive({
  id: '',
  name: '',
  gender: '男',
  age: 25,
  idCard: '',
  phone: '',
  department: '',
  avatar: ''
})

// 表单验证规则
const formRules = {
  libraryName: [{ required: true, message: '请输入库名称', trigger: 'blur' }],
  libraryType: [{ required: true, message: '请选择库类型', trigger: 'change' }]
}

const personFormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

// 表格数据
const libraryList = ref([
  {
    id: '1',
    libraryName: '黑名单人员库',
    libraryType: 'blacklist',
    personCount: 156,
    lastUpdateTime: '2024-01-20 14:30:25',
    status: 'enabled',
    description: '重点关注和监控的黑名单人员',
    createTime: '2024-01-15 09:00:00'
  },
  {
    id: '2',
    libraryName: '访客人员库',
    libraryType: 'visitor',
    personCount: 423,
    lastUpdateTime: '2024-01-20 16:45:12',
    status: 'enabled',
    description: '外来访客人员信息库',
    createTime: '2024-01-16 10:30:00'
  },
  {
    id: '3',
    libraryName: '员工人员库',
    libraryType: 'employee',
    personCount: 1205,
    lastUpdateTime: '2024-01-20 11:20:48',
    status: 'enabled',
    description: '公司员工人员信息库',
    createTime: '2024-01-17 08:15:00'
  }
])

const personList = ref([
  {
    id: '1',
    name: '张三',
    gender: '男',
    age: 28,
    idCard: '110101199001011234',
    phone: '13800138001',
    department: '技术部',
    avatar: '',
    addTime: '2024-01-15 09:00:00'
  },
  {
    id: '2',
    name: '李四',
    gender: '女',
    age: 26,
    idCard: '110101199201021235',
    phone: '13800138002',
    department: '市场部',
    avatar: '',
    addTime: '2024-01-16 10:30:00'
  }
])

const formRef = ref()
const personFormRef = ref()

// 获取类型颜色
const getTypeColor = (type: string) => {
  const colors = {
    blacklist: 'danger',
    whitelist: 'success',
    visitor: 'warning',
    employee: 'primary'
  }
  return colors[type] || 'info'
}

// 获取类型文本
const getTypeText = (type: string) => {
  const texts = {
    blacklist: '黑名单',
    whitelist: '白名单',
    visitor: '访客',
    employee: '员工'
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
    libraryName: '',
    libraryType: '',
    status: ''
  })
  handleSearch()
}

const handleCreate = () => {
  dialogMode.value = 'add'
  dialogTitle.value = '新建人员库'
  resetFormData()
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑人员库'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该人员库吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const index = libraryList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      libraryList.value.splice(index, 1)
    }
    
    ElMessage.success('删除成功')
  } catch {
    // 用户取消删除
  }
}

const handleStatusChange = (row: any) => {
  ElMessage.success(`${row.status === 'enabled' ? '启用' : '禁用'}成功`)
}

const handleImport = () => {
  ElMessage.info('批量导入功能开发中...')
}

const handleViewPersons = (row: any) => {
  personDialogVisible.value = true
  // 这里可以根据人员库ID加载对应的人员数据
}

const handleAddPerson = () => {
  personFormTitle.value = '添加人员'
  resetPersonFormData()
  personFormDialogVisible.value = true
}

const handleEditPerson = (row: any) => {
  personFormTitle.value = '编辑人员'
  Object.assign(personFormData, { ...row })
  personFormDialogVisible.value = true
}

const handleDeletePerson = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该人员吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const index = personList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      personList.value.splice(index, 1)
    }
    
    ElMessage.success('删除成功')
  } catch {
    // 用户取消删除
  }
}

const handleImportPersons = () => {
  ElMessage.info('批量导入人员功能开发中...')
}

const handleAvatarChange = (file: any) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    personFormData.avatar = e.target?.result as string
  }
  reader.readAsDataURL(file.raw)
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (dialogMode.value === 'add') {
      const newLibrary = {
        ...formData,
        id: `library_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        personCount: 0,
        lastUpdateTime: '-',
        createTime: new Date().toLocaleString('zh-CN')
      }
      libraryList.value.unshift(newLibrary)
      ElMessage.success('创建成功')
    } else if (dialogMode.value === 'edit') {
      const index = libraryList.value.findIndex(item => item.id === formData.id)
      if (index > -1) {
        Object.assign(libraryList.value[index], formData)
      }
      ElMessage.success('更新成功')
    }
    
    dialogVisible.value = false
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handlePersonSubmit = async () => {
  if (!personFormRef.value) return
  
  try {
    await personFormRef.value.validate()
    
    if (personFormTitle.value === '添加人员') {
      const newPerson = {
        ...personFormData,
        id: `person_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        addTime: new Date().toLocaleString('zh-CN')
      }
      personList.value.unshift(newPerson)
      ElMessage.success('添加成功')
    } else {
      const index = personList.value.findIndex(item => item.id === personFormData.id)
      if (index > -1) {
        Object.assign(personList.value[index], personFormData)
      }
      ElMessage.success('更新成功')
    }
    
    personFormDialogVisible.value = false
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const resetFormData = () => {
  Object.assign(formData, {
    id: '',
    libraryName: '',
    libraryType: '',
    description: '',
    status: 'enabled'
  })
}

const resetPersonFormData = () => {
  Object.assign(personFormData, {
    id: '',
    name: '',
    gender: '男',
    age: 25,
    idCard: '',
    phone: '',
    department: '',
    avatar: ''
  })
}

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    pagination.total = libraryList.value.length
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
    .el-select {
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

.person-management {
  .person-toolbar {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

.avatar-uploader .avatar {
  width: 100px;
  height: 100px;
  border-radius: 6px;
  display: block;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  text-align: center;
  line-height: 100px;
}
</style>






