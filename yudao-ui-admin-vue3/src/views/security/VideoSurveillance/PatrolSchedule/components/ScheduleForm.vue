<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="600px"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="计划名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入计划名称" />
      </el-form-item>
      <el-form-item label="轮巡计划" prop="patrolPlanId">
        <el-select v-model="formData.patrolPlanId" placeholder="请选择轮巡计划" style="width: 100%">
          <el-option
            v-for="plan in patrolPlans"
            :key="plan.id"
            :label="plan.planName"
            :value="plan.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="计划类型" prop="scheduleType">
        <el-radio-group v-model="formData.scheduleType">
          <el-radio :label="1">日计划</el-radio>
          <el-radio :label="2">周计划</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="formData.scheduleType === 2" label="执行日期" prop="weekDays">
        <el-checkbox-group v-model="weekDaysArray">
          <el-checkbox :label="1">周一</el-checkbox>
          <el-checkbox :label="2">周二</el-checkbox>
          <el-checkbox :label="3">周三</el-checkbox>
          <el-checkbox :label="4">周四</el-checkbox>
          <el-checkbox :label="5">周五</el-checkbox>
          <el-checkbox :label="6">周六</el-checkbox>
          <el-checkbox :label="7">周日</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item label="开始时间" prop="startTime">
        <el-time-picker
          v-model="formData.startTime"
          format="HH:mm"
          value-format="HH:mm:ss"
          placeholder="选择开始时间"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="结束时间" prop="endTime">
        <el-time-picker
          v-model="formData.endTime"
          format="HH:mm"
          value-format="HH:mm:ss"
          placeholder="选择结束时间"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as PatrolScheduleApi from '@/api/iot/video/patrolSchedule'
import * as PatrolPlanApi from '@/api/iot/video/patrolPlan'

const emit = defineEmits(['success'])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formType = ref('')
const submitLoading = ref(false)
const formRef = ref()

const formData = reactive({
  id: undefined,
  name: '',
  patrolPlanId: undefined,
  scheduleType: 1,
  startTime: '',
  endTime: '',
  weekDays: '',
  status: 1,
  remark: ''
})

// 星期数组
const weekDaysArray = ref<number[]>([])

// 监听星期数组变化
watch(weekDaysArray, (val) => {
  formData.weekDays = val.sort((a, b) => a - b).join(',')
}, { deep: true })

// 轮巡计划列表
const patrolPlans = ref([])
const loadPatrolPlans = async () => {
  try {
    const data = await PatrolPlanApi.getVideoPatrolPlanList()
    patrolPlans.value = data
  } catch (error) {
    console.error('加载轮巡计划失败:', error)
    ElMessage.error('加载轮巡计划列表失败')
  }
}

// 表单校验
const formRules = {
  name: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  patrolPlanId: [{ required: true, message: '请选择轮巡计划', trigger: 'change' }],
  scheduleType: [{ required: true, message: '请选择计划类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

// 打开对话框
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  formType.value = type
  dialogTitle.value = type === 'create' ? '新增定时计划' : '编辑定时计划'
  
  await loadPatrolPlans()
  
  if (id) {
    try {
      const data = await PatrolScheduleApi.getVideoPatrolSchedule(id)
      Object.assign(formData, data)
      if (data.weekDays) {
        weekDaysArray.value = data.weekDays.split(',').map(Number)
      }
    } catch (error) {
      console.error('加载定时计划失败:', error)
    }
  } else {
    resetForm()
  }
}

// 重置表单
const resetForm = () => {
  formData.id = undefined
  formData.name = ''
  formData.patrolPlanId = undefined
  formData.scheduleType = 1
  formData.startTime = ''
  formData.endTime = ''
  formData.weekDays = ''
  formData.status = 1
  formData.remark = ''
  weekDaysArray.value = []
}

// 关闭对话框
const handleClose = () => {
  formRef.value?.resetFields()
  resetForm()
}

// 提交表单
const submitForm = async () => {
  await formRef.value.validate()
  
  // 验证周计划必须选择执行日期
  if (formData.scheduleType === 2 && !formData.weekDays) {
    ElMessage.warning('请选择执行日期')
    return
  }
  
  submitLoading.value = true
  try {
    if (formType.value === 'create') {
      await PatrolScheduleApi.createVideoPatrolSchedule(formData)
      ElMessage.success('创建成功')
    } else {
      await PatrolScheduleApi.updateVideoPatrolSchedule(formData)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    emit('success')
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

defineExpose({ open })
</script>
