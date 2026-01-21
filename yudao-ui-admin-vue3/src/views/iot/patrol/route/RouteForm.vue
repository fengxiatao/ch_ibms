<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="线路名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入线路名称" />
      </el-form-item>
      <el-form-item label="线路规则" prop="rule">
        <el-radio-group v-model="formData.rule">
          <el-radio :label="1">顺序巡更</el-radio>
          <el-radio :label="2">无序巡更</el-radio>
        </el-radio-group>
        <div class="el-form-item-msg">
          顺序巡更：必须按照设定的顺序巡更各个点位<br/>
          无序巡更：可以任意顺序巡更各个点位
        </div>
      </el-form-item>
      <el-form-item label="预计时长" prop="duration">
        <el-input-number
          v-model="formData.duration"
          :min="1"
          :max="9999"
          placeholder="请输入预计时长"
          class="!w-full"
        />
        <div class="el-form-item-msg">单位：分钟</div>
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入线路描述"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm" :loading="formLoading">确 定</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import * as PatrolRouteApi from '@/api/iot/patrol/route'

const message = useMessage()
const emit = defineEmits(['success'])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref({
  id: undefined,
  name: '',
  description: '',
  rule: 1,
  duration: undefined
})

const formRules = reactive({
  name: [{ required: true, message: '线路名称不能为空', trigger: 'blur' }],
  rule: [{ required: true, message: '线路规则不能为空', trigger: 'change' }]
})

const formRef = ref()

// 打开弹窗
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增巡更线路' : '修改巡更线路'
  formType.value = type
  resetForm()
  
  // 修改时，加载数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await PatrolRouteApi.getPatrolRoute(id)
    } finally {
      formLoading.value = false
    }
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return
  
  formLoading.value = true
  try {
    const data = formData.value
    if (formType.value === 'create') {
      await PatrolRouteApi.createPatrolRoute(data)
      message.success('新增成功')
    } else {
      await PatrolRouteApi.updatePatrolRoute(data)
      message.success('修改成功')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.value = {
    id: undefined,
    name: '',
    description: '',
    rule: 1,
    duration: undefined
  }
  formRef.value?.resetFields()
}

defineExpose({ open })
</script>

<style scoped>
.el-form-item-msg {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.5;
}
</style>


























