<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-alert
        v-if="isBatch"
        type="info"
        :closable="false"
        class="mb-4"
      >
        已选择 {{ selectedIds.length }} 条告警记录
      </el-alert>
      
      <el-form-item label="处理备注" prop="handleRemark">
        <el-input
          v-model="formData.handleRemark"
          type="textarea"
          :rows="4"
          placeholder="请输入处理备注"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts" name="AlarmHandle">
import { ref, reactive } from 'vue'
import * as AccessAlarmApi from '@/api/iot/access/alarm'
import { ElMessage } from 'element-plus'

const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('处理告警')
const formLoading = ref(false)
const formRef = ref()
const isBatch = ref(false)
const selectedIds = ref<number[]>([])
const currentAlarm = ref<AccessAlarmApi.AccessAlarmVO | null>(null)

const formData = ref({
  id: undefined as number | undefined,
  handleRemark: ''
})

const formRules = reactive({
  handleRemark: [{ required: true, message: '请输入处理备注', trigger: 'blur' }]
})

/** 打开弹窗 - 单个处理 */
const open = (alarm: AccessAlarmApi.AccessAlarmVO) => {
  dialogVisible.value = true
  dialogTitle.value = '处理告警'
  isBatch.value = false
  currentAlarm.value = alarm
  formData.value = {
    id: alarm.id,
    handleRemark: ''
  }
  formRef.value?.resetFields()
}

/** 打开弹窗 - 批量处理 */
const openBatch = (ids: number[]) => {
  dialogVisible.value = true
  dialogTitle.value = `批量处理告警（${ids.length}条）`
  isBatch.value = true
  selectedIds.value = ids
  formData.value = {
    id: undefined,
    handleRemark: ''
  }
  formRef.value?.resetFields()
}

/** 提交表单 */
const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return
  
  formLoading.value = true
  try {
    if (isBatch.value) {
      // 批量处理
      await AccessAlarmApi.handleAccessAlarmBatch(selectedIds.value, formData.value.handleRemark)
      message.success(`成功处理 ${selectedIds.value.length} 条告警记录`)
    } else {
      // 单个处理
      if (!formData.value.id) {
        ElMessage.error('告警ID不能为空')
        return
      }
      await AccessAlarmApi.handleAccessAlarm({
        id: formData.value.id,
        handleRemark: formData.value.handleRemark
      })
      message.success('处理成功')
    }
    dialogVisible.value = false
    emit('success')
  } catch (error: any) {
    message.error('处理失败：' + (error.message || '未知错误'))
  } finally {
    formLoading.value = false
  }
}

const emit = defineEmits(['success'])
defineExpose({ open, openBatch })
</script>




















