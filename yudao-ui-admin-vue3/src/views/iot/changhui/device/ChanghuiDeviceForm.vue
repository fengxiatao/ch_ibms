<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="测站编码" prop="stationCode">
        <el-input v-model="formData.stationCode" placeholder="请输入测站编码（20位）" :disabled="formType === 'update'" />
      </el-form-item>
      <el-form-item label="设备名称" prop="deviceName">
        <el-input v-model="formData.deviceName" placeholder="请输入设备名称" />
      </el-form-item>
      <el-form-item label="设备类型" prop="deviceType">
        <el-select v-model="formData.deviceType" placeholder="请选择设备类型" class="!w-full">
          <el-option
            v-for="item in deviceTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="行政区代码" prop="provinceCode">
            <el-input v-model="formData.provinceCode" placeholder="行政区代码" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="管理处代码" prop="managementCode">
            <el-input v-model="formData.managementCode" placeholder="管理处代码" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="站所代码" prop="stationCodePart">
            <el-input v-model="formData.stationCodePart" placeholder="站所代码" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="设备厂家" prop="manufacturer">
            <el-input v-model="formData.manufacturer" placeholder="设备厂家" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="桩号（前）" prop="pileFront">
            <el-input v-model="formData.pileFront" placeholder="桩号前" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="桩号（后）" prop="pileBack">
            <el-input v-model="formData.pileBack" placeholder="桩号后" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="顺序编号" prop="sequenceNo">
        <el-input v-model="formData.sequenceNo" placeholder="顺序编号" />
      </el-form-item>
      <el-form-item label="TEA密钥" prop="teaKey">
        <el-input v-model="formData.teaKey" placeholder="TEA加密密钥（可选）" />
      </el-form-item>
      <el-form-item label="设备密码" prop="password">
        <el-input v-model="formData.password" placeholder="设备密码（可选）" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm" :loading="formLoading">确 定</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { ChanghuiDeviceApi, ChanghuiDeviceVO } from '@/api/iot/changhui'

defineOptions({ name: 'ChanghuiDeviceForm' })

/** 设备类型选项（从数据字典获取） */
const deviceTypeOptions = getIntDictOptions(DICT_TYPE.CHANGHUI_DEVICE_TYPE)

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<Partial<ChanghuiDeviceVO>>({
  stationCode: '',
  deviceName: '',
  deviceType: undefined,
  provinceCode: '',
  managementCode: '',
  stationCodePart: '',
  pileFront: '',
  pileBack: '',
  manufacturer: '',
  sequenceNo: '',
  teaKey: '',
  password: ''
})
const formRules = reactive({
  stationCode: [
    { required: true, message: '测站编码不能为空', trigger: 'blur' },
    { min: 20, max: 20, message: '测站编码必须为20位', trigger: 'blur' }
  ],
  deviceName: [{ required: true, message: '设备名称不能为空', trigger: 'blur' }],
  deviceType: [{ required: true, message: '设备类型不能为空', trigger: 'change' }]
})
const formRef = ref()

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增长辉设备' : '编辑长辉设备'
  formType.value = type
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ChanghuiDeviceApi.getDevice(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    if (formType.value === 'create') {
      await ChanghuiDeviceApi.registerDevice(formData.value as ChanghuiDeviceVO)
      message.success(t('common.createSuccess'))
    } else {
      await ChanghuiDeviceApi.updateDevice(formData.value as ChanghuiDeviceVO)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    stationCode: '',
    deviceName: '',
    deviceType: undefined,
    provinceCode: '',
    managementCode: '',
    stationCodePart: '',
    pileFront: '',
    pileBack: '',
    manufacturer: '',
    sequenceNo: '',
    teaKey: '',
    password: ''
  }
  formRef.value?.resetFields()
}
</script>
