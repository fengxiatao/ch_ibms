<template>
  <el-dialog v-model="visible" :title="title" width="500px" destroy-on-close>
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
      <el-form-item label="部门名称" prop="deptName">
        <el-input v-model="formData.deptName" placeholder="请输入部门名称" />
      </el-form-item>
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
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" :min="0" style="width: 100%" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { AccessDepartmentApi, type AccessDepartmentVO } from '@/api/iot/access'

const emit = defineEmits(['success'])

const visible = ref(false)
const loading = ref(false)
const formType = ref<'create' | 'update'>('create')
const formRef = ref()
const deptTree = ref<AccessDepartmentVO[]>([])

const formData = reactive({
  id: undefined as number | undefined,
  deptName: '',
  parentId: undefined as number | undefined,
  sort: 0,
  remark: ''
})

const rules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

const title = computed(() => formType.value === 'create' ? '新增部门' : '编辑部门')

const open = async (type: 'create' | 'update', id?: number) => {
  formType.value = type
  resetForm()
  
  // 加载部门树
  try {
    deptTree.value = await AccessDepartmentApi.getDepartmentTree()
  } catch (error) {
    console.error('加载部门树失败:', error)
  }

  if (type === 'create' && id) {
    formData.parentId = id
  }

  if (type === 'update' && id) {
    try {
      const data = await AccessDepartmentApi.getDepartment(id)
      Object.assign(formData, data)
    } catch (error) {
      console.error('获取部门详情失败:', error)
    }
  }

  visible.value = true
}

const resetForm = () => {
  formData.id = undefined
  formData.deptName = ''
  formData.parentId = undefined
  formData.sort = 0
  formData.remark = ''
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true

    if (formType.value === 'create') {
      await AccessDepartmentApi.createDepartment(formData)
      ElMessage.success('新增成功')
    } else {
      await AccessDepartmentApi.updateDepartment(formData)
      ElMessage.success('修改成功')
    }

    visible.value = false
    emit('success')
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>
