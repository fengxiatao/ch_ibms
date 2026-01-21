<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="计划名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入计划名称" />
      </el-form-item>
      <el-form-item label="触发类型" prop="triggerType">
        <el-radio-group v-model="formData.triggerType">
          <el-radio :label="1">手动触发</el-radio>
          <el-radio :label="2">定时触发</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="Cron表达式" prop="cronExpression" v-if="formData.triggerType === 2">
        <el-input v-model="formData.cronExpression" placeholder="请输入Cron表达式，如：0 0 9 * * ?" />
        <div class="el-form-item-msg">示例：0 0 9 * * ? 表示每天上午9点执行</div>
      </el-form-item>
      <el-form-item label="开始时间" prop="startTime" v-if="formData.triggerType === 2">
        <el-date-picker
          v-model="formData.startTime"
          type="datetime"
          placeholder="选择开始时间"
          value-format="x"
        />
      </el-form-item>
      <el-form-item label="结束时间" prop="endTime" v-if="formData.triggerType === 2">
        <el-date-picker
          v-model="formData.endTime"
          type="datetime"
          placeholder="选择结束时间"
          value-format="x"
        />
      </el-form-item>
      <el-form-item label="巡更线路" prop="routeId">
        <el-select v-model="formData.routeId" placeholder="请选择巡更线路" class="w-full">
          <el-option
            v-for="route in routeList"
            :key="route.id"
            :label="route.name"
            :value="route.id!"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="2">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入描述"
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
import { ref, reactive, onMounted } from 'vue'
import * as PatrolPlanApi from '@/api/iot/patrol/plan'
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
  triggerType: 1,
  cronExpression: '',
  startTime: undefined,
  endTime: undefined,
  routeId: undefined,
  status: 1
})

const formRules = reactive({
  name: [{ required: true, message: '计划名称不能为空', trigger: 'blur' }],
  triggerType: [{ required: true, message: '触发类型不能为空', trigger: 'change' }],
  cronExpression: [
    {
      required: true,
      message: 'Cron表达式不能为空',
      trigger: 'blur',
      validator: (_rule: any, value: any, callback: any) => {
        if (formData.value.triggerType === 2 && !value) {
          callback(new Error('Cron表达式不能为空'))
        } else {
          callback()
        }
      }
    }
  ],
  routeId: [{ required: true, message: '巡更线路不能为空', trigger: 'change' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
})

const formRef = ref()
const routeList = ref<PatrolRouteApi.PatrolRouteVO[]>([])

// 打开弹窗
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增巡更计划' : '修改巡更计划'
  formType.value = type
  resetForm()
  
  // 加载巡更线路列表
  await loadRouteList()
  
  // 修改时，加载数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await PatrolPlanApi.getPatrolPlan(id)
    } finally {
      formLoading.value = false
    }
  }
}

// 加载线路列表
const loadRouteList = async () => {
  try {
    routeList.value = await PatrolRouteApi.getPatrolRouteList()
  } catch (error) {
    console.error('加载线路列表失败', error)
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
      await PatrolPlanApi.createPatrolPlan(data)
      message.success('新增成功')
    } else {
      await PatrolPlanApi.updatePatrolPlan(data)
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
    triggerType: 1,
    cronExpression: '',
    startTime: undefined,
    endTime: undefined,
    routeId: undefined,
    status: 1
  }
  formRef.value?.resetFields()
}

defineExpose({ open })

onMounted(() => {
  loadRouteList()
})
</script>

<style scoped>
.el-form-item-msg {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>


























