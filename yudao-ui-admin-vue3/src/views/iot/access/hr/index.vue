<template>
<ContentWrap
    :body-style="{
      padding: '10px',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      backgroundColor: 'var(--el-bg-color)'
    }"
    style="height: calc(100vh - var(--page-top-gap, 70px)); padding-top: var(--page-top-gap, 70px)"
  >
  <div class="hr-page">
    <div class="layout">
      <div class="left-panel">
        <ContentWrap>
          <div class="panel-header">
            <div class="title">
              <Icon icon="ep:office-building" />
              <span>组织架构</span>
            </div>
            <div class="actions">
              <el-button type="primary" size="small" @click="handleAddRoot">
                <Icon icon="ep:plus" class="mr-5px" /> 添加公司
              </el-button>
            </div>
          </div>
          <el-input v-model="deptFilterText" placeholder="搜索部门" clearable class="mb-10px">
            <template #prefix>
              <Icon icon="ep:search" />
            </template>
          </el-input>
          <el-tree
            ref="deptTreeRef"
            :data="deptTree"
            :props="{ label: 'deptName', children: 'children' }"
            node-key="id"
            default-expand-all
            highlight-current
            :filter-node-method="filterDeptNode"
            @node-click="handleDeptClick"
          >
            <template #default="{ data, node }">
              <span class="tree-node">
                <span>{{ node.label }}</span>
                <span class="tree-actions">
                  <el-button link type="primary" size="small" @click.stop="handleAddDept(data)"><Icon icon="ep:plus" /></el-button>
                  <el-button link type="primary" size="small" @click.stop="handleEditDept(data)"><Icon icon="ep:edit" /></el-button>
                  <el-button link type="danger" size="small" @click.stop="handleDeleteDept(data)"><Icon icon="ep:delete" /></el-button>
                </span>
              </span>
            </template>
          </el-tree>
        </ContentWrap>
      </div>
      <div class="right-panel">
        <ContentWrap>
          <div class="panel-header">
            <div class="title">
              <Icon icon="ep:user" />
              <span>人员管理</span>
              <span v-if="currentDept" class="subtitle">（{{ currentDept.deptName }}）</span>
            </div>
            <div class="actions">
              <el-button type="primary" @click="handleAdd"><Icon icon="ep:plus" class="mr-5px" /> 新增</el-button>
              <el-button type="primary" @click="handleBatchAdd"><Icon icon="ep:document-add" class="mr-5px" /> 批量添加</el-button>
              <el-button type="success" @click="handleImport"><Icon icon="ep:upload" class="mr-5px" /> 导入</el-button>
              <el-button type="warning" @click="handleExportTemplate"><Icon icon="ep:download" class="mr-5px" /> 模板</el-button>
            </div>
          </div>
          <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px" class="mb-10px">
            <el-form-item label="人员编号" prop="personCode">
              <el-input v-model="queryParams.personCode" placeholder="人员编号" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="人员姓名" prop="personName">
              <el-input v-model="queryParams.personName" placeholder="人员姓名" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px">
                <el-option v-for="item in AccessPersonStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
              <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
            </el-form-item>
          </el-form>
          <el-table v-loading="loading" :data="list" stripe :show-overflow-tooltip="true" table-layout="auto">
            <el-table-column label="人员编号" prop="personCode" min-width="120" show-overflow-tooltip />
            <el-table-column label="人员姓名" prop="personName" min-width="120" show-overflow-tooltip />
            <el-table-column label="人员类型" prop="personType" min-width="100">
              <template #default="{ row }">
                <el-tag>{{ getPersonTypeLabel(row.personType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="所属部门" prop="deptName" min-width="150" show-overflow-tooltip />
            <el-table-column label="手机号" prop="phone" min-width="140" show-overflow-tooltip />
            <el-table-column label="有效期" min-width="220" show-overflow-tooltip>
              <template #default="{ row }">
                {{ formatDate(row.validStart) }} ~ {{ formatDate(row.validEnd) }}
              </template>
            </el-table-column>
            <el-table-column label="指纹数" prop="fingerprintCount" width="90" align="center">
              <template #default="{ row }">
                <span>{{ row.fingerprintCount || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" prop="status" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '冻结' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="320" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
                <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
                <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="handleFreeze(row)">
                  {{ row.status === 1 ? '冻结' : '解冻' }}
                </el-button>
                <el-button link type="primary" @click="handlePermission(row)">权限</el-button>
              </template>
            </el-table-column>
          </el-table>
          <Pagination v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
        </ContentWrap>
      </div>
    </div>

    <el-dialog v-model="deptDialogVisible" :title="deptDialogTitle" width="500px">
      <el-form ref="deptFormRef" :model="deptFormData" :rules="deptFormRules" label-width="100px">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="deptFormData.parentId"
            :data="deptTree"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            placeholder="请选择上级部门"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="deptFormData.deptName" />
        </el-form-item>
        <!-- 部门编码由后端自动生成，不需要用户输入 -->
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="deptFormData.leader" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="deptFormData.phone" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="deptFormData.sort" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="deptFormData.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deptDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDeptSubmit">确定</el-button>
      </template>
    </el-dialog>

    <PersonForm ref="personFormRef" @success="getList" :dept-tree="deptTree" />
    <CredentialDialog ref="credentialDialogRef" />
    <PermissionDialog ref="permissionDialogRef" />
    <BatchAddDialog ref="batchAddDialogRef" :dept-tree="deptTree" @success="getList" />
  </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox, ElTree } from 'element-plus'
import { ContentWrap } from '@/components/ContentWrap'
import Pagination from '@/components/Pagination/index.vue'
import { formatDate } from '@/utils/formatTime'
import { AccessPersonApi, AccessPersonStatusOptions, AccessPersonTypeOptions, AccessDepartmentApi, type AccessPersonVO, type AccessDepartmentVO } from '@/api/iot/access'
import PersonForm from '@/views/iot/access/person/PersonForm.vue'
import CredentialDialog from '@/views/iot/access/person/CredentialDialog.vue'
import PermissionDialog from '@/views/iot/access/person/PermissionDialog.vue'
import BatchAddDialog from './BatchAddDialog.vue'

defineOptions({ name: 'AccessHR' })

const loading = ref(false)
const list = ref<AccessPersonVO[]>([])
const total = ref(0)
const queryFormRef = ref()
const personFormRef = ref()
const credentialDialogRef = ref()
const permissionDialogRef = ref()
const batchAddDialogRef = ref()

const deptTree = ref<AccessDepartmentVO[]>([])
const deptTreeRef = ref<InstanceType<typeof ElTree>>()
const deptFilterText = ref('')
const currentDept = ref<AccessDepartmentVO | null>(null)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  personCode: '',
  personName: '',
  deptId: undefined as number | undefined,
  status: undefined as number | undefined
})

const getPersonTypeLabel = (type: number) => {
  const item = AccessPersonTypeOptions.find((i) => i.value === type)
  return item?.label || '未知'
}

const getList = async () => {
  loading.value = true
  try {
    const res = await AccessPersonApi.getPersonPage(queryParams)
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const loadDeptTree = async () => {
  deptTree.value = await AccessDepartmentApi.getDepartmentTree()
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const handleAdd = () => {
  personFormRef.value?.open('create')
}

const handleEdit = (row: AccessPersonVO) => {
  personFormRef.value?.open('update', row.id)
}

const handleDelete = async (row: AccessPersonVO) => {
  try {
    await ElMessageBox.confirm(`确定要删除人员"${row.personName}"吗？`, '提示', { type: 'warning' })
    await AccessPersonApi.deletePerson(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch {}
}

const handleCredential = (row: AccessPersonVO) => {
  credentialDialogRef.value?.open(row)
}

const handlePermission = (row: AccessPersonVO) => {
  permissionDialogRef.value?.open(row)
}

const handleFreeze = async (row: AccessPersonVO) => {
  const isFreeze = row.status === 1
  const action = isFreeze ? '冻结' : '解冻'
  const message = isFreeze 
    ? `冻结后该用户将无法正常使用，确认冻结"${row.personName}"？`
    : `确认解冻"${row.personName}"？`
  try {
    await ElMessageBox.confirm(message, '提示', { type: 'warning' })
    if (isFreeze) {
      await AccessPersonApi.freezePerson(row.id)
    } else {
      await AccessPersonApi.unfreezePerson(row.id)
    }
    ElMessage.success(`${action}成功`)
    getList()
  } catch {}
}

const handleBatchAdd = () => {
  batchAddDialogRef.value?.open()
}

const handleImport = () => {
  ElMessage.info('导入功能开发中')
}

const handleExportTemplate = async () => {
  try {
    await AccessPersonApi.exportTemplate()
    ElMessage.success('下载成功')
  } catch {}
}

const filterDeptNode = (value: string, data: any) => {
  if (!value) return true
  return (data.deptName || '').includes(value)
}

watch(deptFilterText, (val) => {
  deptTreeRef.value?.filter(val)
})

const handleDeptClick = (data: AccessDepartmentVO) => {
  currentDept.value = data
  queryParams.deptId = data.id
  handleQuery()
}

const deptDialogVisible = ref(false)
const deptDialogTitle = ref('')
const deptFormRef = ref()
const deptIsEdit = ref(false)
const deptFormData = reactive({
  id: undefined as number | undefined,
  parentId: 0,
  deptName: '',
  deptCode: '',
  leader: '',
  phone: '',
  sort: 0,
  status: 1
})

const deptFormRules = { deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }] }

const resetDeptForm = () => {
  deptFormData.id = undefined
  deptFormData.parentId = 0
  deptFormData.deptName = ''
  deptFormData.deptCode = ''
  deptFormData.leader = ''
  deptFormData.phone = ''
  deptFormData.sort = 0
  deptFormData.status = 1
}

const handleAddRoot = () => {
  resetDeptForm()
  deptIsEdit.value = false
  deptDialogTitle.value = '添加公司'
  deptDialogVisible.value = true
}

const handleAddDept = (data: AccessDepartmentVO) => {
  resetDeptForm()
  deptFormData.parentId = data.id
  deptIsEdit.value = false
  deptDialogTitle.value = '添加部门'
  deptDialogVisible.value = true
}

const handleEditDept = (data: AccessDepartmentVO) => {
  resetDeptForm()
  Object.assign(deptFormData, data)
  deptIsEdit.value = true
  deptDialogTitle.value = '编辑部门'
  deptDialogVisible.value = true
}

const handleDeleteDept = async (data: AccessDepartmentVO) => {
  try {
    await ElMessageBox.confirm(`确定要删除部门"${data.deptName}"吗？`, '提示', { type: 'warning' })
    await AccessDepartmentApi.deleteDepartment(data.id)
    ElMessage.success('删除成功')
    loadDeptTree()
    if (currentDept.value?.id === data.id) currentDept.value = null
  } catch {}
}

const handleDeptSubmit = async () => {
  try {
    await deptFormRef.value?.validate()
    if (deptIsEdit.value) {
      await AccessDepartmentApi.updateDepartment(deptFormData as any)
      ElMessage.success('修改成功')
    } else {
      await AccessDepartmentApi.createDepartment(deptFormData)
      ElMessage.success('添加成功')
    }
    deptDialogVisible.value = false
    await loadDeptTree()
  } catch {}
}

onMounted(() => {
  loadDeptTree()
  getList()
})
</script>

<style scoped lang="scss">
.hr-page { padding: 10px; }
.layout { display: flex; gap: 16px; }
.left-panel { width: 320px; flex: 0 0 320px; }
.right-panel { flex: 1; }
.panel-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.panel-header .title { display: flex; align-items: center; gap: 8px; font-weight: 600; }
.panel-header .subtitle { margin-left: 8px; color: var(--el-text-color-secondary); font-weight: 400; }
.tree-node { display: flex; align-items: center; justify-content: space-between; padding-right: 8px; }
.tree-actions { display: none; }
.el-tree-node__content:hover .tree-actions { display: inline-block; }
</style>
