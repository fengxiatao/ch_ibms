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
  <div class="access-department-container">
    <el-row :gutter="20">
      <!-- 左侧：组织树 -->
      <el-col :span="8">
        <ContentWrap title="组织架构">
          <template #extra>
            <el-button type="primary" size="small" @click="handleAddRoot">
              <Icon icon="ep:plus" class="mr-5px" /> 添加公司
            </el-button>
          </template>
          
          <el-input v-model="filterText" placeholder="搜索部门" class="mb-10px" clearable>
            <template #prefix>
              <Icon icon="ep:search" />
            </template>
          </el-input>
          
          <el-tree
            ref="treeRef"
            :data="deptTree"
            :props="treeProps"
            :filter-node-method="filterNode"
            node-key="id"
            default-expand-all
            highlight-current
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <span class="custom-tree-node">
                <span>{{ node.label }}</span>
                <span class="tree-actions">
                  <el-button link type="primary" size="small" @click.stop="handleAdd(data)">
                    <Icon icon="ep:plus" />
                  </el-button>
                  <el-button link type="primary" size="small" @click.stop="handleEdit(data)">
                    <Icon icon="ep:edit" />
                  </el-button>
                  <el-button link type="danger" size="small" @click.stop="handleDelete(data)">
                    <Icon icon="ep:delete" />
                  </el-button>
                </span>
              </span>
            </template>
          </el-tree>
        </ContentWrap>
      </el-col>
      
      <!-- 右侧：部门详情 -->
      <el-col :span="16">
        <ContentWrap :title="currentDept ? `部门详情 - ${currentDept.deptName}` : '部门详情'">
          <template v-if="currentDept">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="部门名称">{{ currentDept.deptName }}</el-descriptions-item>
              <el-descriptions-item label="部门编码">{{ currentDept.deptCode || '-' }}</el-descriptions-item>
              <el-descriptions-item label="负责人">{{ currentDept.leader || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ currentDept.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="排序">{{ currentDept.sort }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="currentDept.status === 1 ? 'success' : 'danger'">
                  {{ currentDept.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </template>
          <template v-else>
            <el-empty description="请选择部门查看详情" />
          </template>
        </ContentWrap>
      </el-col>
    </el-row>
    
    <!-- 部门表单弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="formData.parentId"
            :data="deptTree"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            placeholder="请选择上级部门"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="formData.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="部门编码" prop="deptCode">
          <el-input v-model="formData.deptCode" placeholder="请输入部门编码" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="formData.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
  </ContentWrap>
</template>


<script setup lang="ts">
import { ref, watch, reactive } from 'vue'
import { ElMessage, ElMessageBox, ElTree } from 'element-plus'
import { AccessDepartmentApi, type AccessDepartmentVO } from '@/api/iot/access'
import { ContentWrap } from '@/components/ContentWrap'

defineOptions({ name: 'AccessDepartment' })

const treeRef = ref<InstanceType<typeof ElTree>>()
const formRef = ref()
const filterText = ref('')
const deptTree = ref<AccessDepartmentVO[]>([])
const currentDept = ref<AccessDepartmentVO | null>(null)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)

const treeProps = {
  label: 'deptName',
  children: 'children'
}

const formData = reactive({
  id: undefined as number | undefined,
  parentId: 0,
  deptName: '',
  deptCode: '',
  leader: '',
  phone: '',
  sort: 0,
  status: 1
})

const formRules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

// 加载组织树
const loadDeptTree = async () => {
  try {
    deptTree.value = await AccessDepartmentApi.getDepartmentTree()
  } catch (error) {
    console.error('加载组织树失败:', error)
  }
}

// 过滤节点
const filterNode = (value: string, data: AccessDepartmentVO) => {
  if (!value) return true
  return data.deptName.includes(value)
}

// 监听搜索
watch(filterText, (val) => {
  treeRef.value?.filter(val)
})

// 点击节点
const handleNodeClick = (data: AccessDepartmentVO) => {
  currentDept.value = data
}

// 重置表单
const resetForm = () => {
  formData.id = undefined
  formData.parentId = 0
  formData.deptName = ''
  formData.deptCode = ''
  formData.leader = ''
  formData.phone = ''
  formData.sort = 0
  formData.status = 1
}

// 添加根节点
const handleAddRoot = () => {
  resetForm()
  isEdit.value = false
  dialogTitle.value = '添加公司'
  dialogVisible.value = true
}

// 添加子部门
const handleAdd = (data: AccessDepartmentVO) => {
  resetForm()
  formData.parentId = data.id
  isEdit.value = false
  dialogTitle.value = '添加部门'
  dialogVisible.value = true
}

// 编辑部门
const handleEdit = (data: AccessDepartmentVO) => {
  resetForm()
  Object.assign(formData, data)
  isEdit.value = true
  dialogTitle.value = '编辑部门'
  dialogVisible.value = true
}

// 删除部门
const handleDelete = async (data: AccessDepartmentVO) => {
  try {
    await ElMessageBox.confirm(`确定要删除部门"${data.deptName}"吗？`, '提示', {
      type: 'warning'
    })
    await AccessDepartmentApi.deleteDepartment(data.id)
    ElMessage.success('删除成功')
    loadDeptTree()
    if (currentDept.value?.id === data.id) {
      currentDept.value = null
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    if (isEdit.value) {
      await AccessDepartmentApi.updateDepartment(formData as any)
      ElMessage.success('修改成功')
    } else {
      await AccessDepartmentApi.createDepartment(formData)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadDeptTree()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

// 初始化
loadDeptTree()
</script>

<style scoped lang="scss">
.access-department-container {
  padding: 10px;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}

.tree-actions {
  display: none;
}

.el-tree-node__content:hover .tree-actions {
  display: inline-block;
}
</style>
