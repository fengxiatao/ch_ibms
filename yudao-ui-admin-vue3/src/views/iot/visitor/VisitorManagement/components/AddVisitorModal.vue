<template>
  <el-dialog
    v-model="dialogVisible"
    title="新增访客预约"
    width="650px"
    :close-on-click-modal="false"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
    >
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="访客姓名" prop="name">
            <el-input v-model="formData.name" placeholder="请输入姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="formData.phone" placeholder="请输入手机号" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="访客类型" prop="type">
            <el-select v-model="formData.type" placeholder="请选择" style="width: 100%">
              <el-option label="商务访客" value="business" />
              <el-option label="VIP访客" value="vip" />
              <el-option label="面试候选" value="interview" />
              <el-option label="外协人员" value="contractor" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属单位" prop="company">
            <el-input v-model="formData.company" placeholder="请输入单位名称" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="被访人" prop="host">
        <el-input v-model="formData.host" placeholder="请输入被访人姓名或工号" />
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="预约日期" prop="visitDate">
            <el-date-picker
              v-model="formData.visitDate"
              type="date"
              placeholder="选择日期"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="预约时间" prop="visitTime">
            <el-time-picker
              v-model="formData.visitTime"
              placeholder="选择时间"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="来访事由" prop="reason">
        <el-input v-model="formData.reason" placeholder="请输入来访事由" />
      </el-form-item>

      <el-form-item label="访问区域" prop="areas">
        <el-checkbox-group v-model="formData.areas">
          <el-checkbox value="lobby">大堂</el-checkbox>
          <el-checkbox value="meeting">会议室</el-checkbox>
          <el-checkbox value="office">办公区</el-checkbox>
          <el-checkbox value="cafeteria">餐厅</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="身份证号">
            <el-input v-model="formData.idCard" placeholder="选填" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="车牌号">
            <el-input v-model="formData.carNo" placeholder="选填，如：京A·12345" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="备注说明">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="其他需要说明的信息"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确认预约</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { NewVisitorManagementApi } from '@/api/iot/visitor/newVisitorManagement'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits(['update:visible', 'success'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const formRef = ref<FormInstance>()

const formData = reactive({
  name: '',
  phone: '',
  type: 'business',
  company: '',
  host: '',
  visitDate: '',
  visitTime: '',
  reason: '',
  areas: ['lobby'],
  idCard: '',
  carNo: '',
  remark: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入访客姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择访客类型', trigger: 'change' }],
  host: [{ required: true, message: '请输入被访人', trigger: 'blur' }],
  visitDate: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
  visitTime: [{ required: true, message: '请选择预约时间', trigger: 'change' }],
  reason: [{ required: true, message: '请输入来访事由', trigger: 'blur' }],
  areas: [{ required: true, message: '请选择访问区域', trigger: 'change', type: 'array', min: 1 }]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate()

  const visitTime = (() => {
    const date = formData.visitDate ? new Date(formData.visitDate as any) : null
    const time = formData.visitTime ? new Date(formData.visitTime as any) : null
    if (!date || !time) return ''
    const d = new Date(date)
    d.setHours(time.getHours(), time.getMinutes(), 0, 0)
    return d.toISOString()
  })()

  await NewVisitorManagementApi.createAppointment({
    name: formData.name,
    phone: formData.phone,
    type: formData.type,
    company: formData.company,
    host: formData.host,
    visitTime,
    reason: formData.reason,
    areas: formData.areas,
    idCard: formData.idCard || undefined,
    carNo: formData.carNo || undefined,
    remark: formData.remark || undefined
  })

  ElMessage.success('预约提交成功，等待审批')
  emit('success')
  dialogVisible.value = false

  // 重置表单
  formRef.value.resetFields()
}
</script>
