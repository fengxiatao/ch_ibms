<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="800px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px" v-loading="formLoading">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="所属楼层" prop="floorId">
            <el-select v-model="formData.floorId" placeholder="请选择所属楼层" class="!w-full">
              <el-option v-for="floor in floorList" :key="floor.id" :label="floor.name" :value="floor.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="区域类型" prop="areaType">
            <el-input v-model="formData.areaType" placeholder="请输入区域类型" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="区域名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入区域名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="区域编码" prop="code">
            <el-input v-model="formData.code" placeholder="请输入区域编码" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="主要用途" prop="primaryPurpose">
            <el-input v-model="formData.primaryPurpose" placeholder="请输入主要用途" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="是否启用" prop="isActive">
            <el-switch v-model="formData.isActive" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="区域面积(㎡)" prop="areaSize">
            <el-input-number v-model="formData.areaSize" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="可用面积(㎡)" prop="usableArea">
            <el-input-number v-model="formData.usableArea" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="最大容纳人数" prop="maxOccupancy">
            <el-input-number v-model="formData.maxOccupancy" :min="0" class="!w-full" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="责任人" prop="responsiblePerson">
            <el-input v-model="formData.responsiblePerson" placeholder="请输入责任人" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="监控" prop="hasCctv">
            <el-switch v-model="formData.hasCctv" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="门禁" prop="hasAccessControl">
            <el-switch v-model="formData.hasAccessControl" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="消防设施" prop="hasFireEquipment">
            <el-switch v-model="formData.hasFireEquipment" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="温度设定(℃)" prop="temperatureSetting">
            <el-input-number v-model="formData.temperatureSetting" :precision="1" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="湿度设定(%)" prop="humiditySetting">
            <el-input-number v-model="formData.humiditySetting" :min="0" :max="100" :precision="1" class="!w-full" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="描述" prop="description">
        <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import * as AreaApi from '@/api/iot/spatial/area'
import * as FloorApi from '@/api/iot/spatial/floor'

defineOptions({ name: 'AreaForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const floorList = ref([])
const formData = ref({
  id: undefined,
  floorId: undefined,
  name: '',
  code: '',
  areaType: '',
  areaSize: undefined,
  usableArea: undefined,
  maxOccupancy: undefined,
  currentOccupancy: undefined,
  primaryPurpose: '',
  responsiblePerson: '',
  contactPhone: '',
  isActive: true,
  hasCctv: false,
  hasAccessControl: false,
  hasFireEquipment: false,
  temperatureSetting: undefined,
  humiditySetting: undefined,
  description: ''
})
const formRules = reactive({
  floorId: [{ required: true, message: '所属楼层不能为空', trigger: 'change' }],
  name: [{ required: true, message: '区域名称不能为空', trigger: 'blur' }],
  code: [{ required: true, message: '区域编码不能为空', trigger: 'blur' }]
})
const formRef = ref()

const getFloorList = async () => {
  floorList.value = await FloorApi.getFloorList()
}

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  await getFloorList()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await AreaApi.getArea(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  formLoading.value = true
  try {
    const data = formData.value as unknown as AreaApi.AreaVO
    if (formType.value === 'create') {
      await AreaApi.createArea(data)
      message.success(t('common.createSuccess'))
    } else {
      await AreaApi.updateArea(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    id: undefined,
    floorId: undefined,
    name: '',
    code: '',
    areaType: '',
    areaSize: undefined,
    usableArea: undefined,
    maxOccupancy: undefined,
    currentOccupancy: undefined,
    primaryPurpose: '',
    responsiblePerson: '',
    contactPhone: '',
    isActive: true,
    hasCctv: false,
    hasAccessControl: false,
    hasFireEquipment: false,
    temperatureSetting: undefined,
    humiditySetting: undefined,
    description: ''
  }
  formRef.value?.resetFields()
}
</script>

