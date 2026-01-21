<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="800px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px" v-loading="formLoading">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="所属建筑" prop="buildingId">
            <el-select v-model="formData.buildingId" placeholder="请选择所属建筑" class="!w-full">
              <el-option v-for="building in buildingList" :key="building.id" :label="building.name" :value="building.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="楼层号" prop="floorNumber">
            <el-input-number v-model="formData.floorNumber" placeholder="请输入楼层号" class="!w-full" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="楼层名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入楼层名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="楼层编码" prop="code">
            <el-input v-model="formData.code" placeholder="请输入楼层编码" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="楼层类型" prop="floorType">
            <el-input v-model="formData.floorType" placeholder="请输入楼层类型" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="主要功能" prop="primaryFunction">
            <el-input v-model="formData.primaryFunction" placeholder="请输入主要功能" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="楼层高度(m)" prop="floorHeight">
            <el-input-number v-model="formData.floorHeight" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="楼层面积(㎡)" prop="floorArea">
            <el-input-number v-model="formData.floorArea" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="可用面积(㎡)" prop="usableArea">
            <el-input-number v-model="formData.usableArea" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="最大容纳人数" prop="maxOccupancy">
            <el-input-number v-model="formData.maxOccupancy" :min="0" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="紧急出口数量" prop="emergencyExitCount">
            <el-input-number v-model="formData.emergencyExitCount" :min="0" class="!w-full" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="喷淋系统" prop="hasSprinkler">
            <el-switch v-model="formData.hasSprinkler" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="烟雾探测器" prop="hasSmokeDetector">
            <el-switch v-model="formData.hasSmokeDetector" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="紧急出口" prop="hasEmergencyExit">
            <el-switch v-model="formData.hasEmergencyExit" />
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
import * as FloorApi from '@/api/iot/spatial/floor'
import * as BuildingApi from '@/api/iot/spatial/building'

defineOptions({ name: 'FloorForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const buildingList = ref([])
const formData = ref({
  id: undefined,
  buildingId: undefined,
  name: '',
  code: '',
  floorNumber: undefined,
  floorType: '',
  floorHeight: undefined,
  floorArea: undefined,
  usableArea: undefined,
  primaryFunction: '',
  maxOccupancy: undefined,
  hasSprinkler: false,
  hasSmokeDetector: false,
  hasEmergencyExit: false,
  emergencyExitCount: undefined,
  description: ''
})
const formRules = reactive({
  buildingId: [{ required: true, message: '所属建筑不能为空', trigger: 'change' }],
  name: [{ required: true, message: '楼层名称不能为空', trigger: 'blur' }],
  code: [{ required: true, message: '楼层编码不能为空', trigger: 'blur' }],
  floorNumber: [{ required: true, message: '楼层号不能为空', trigger: 'blur' }]
})
const formRef = ref()

const getBuildingList = async () => {
  buildingList.value = await BuildingApi.getBuildingList()
}

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  await getBuildingList()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await FloorApi.getFloor(id)
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
    const data = formData.value as unknown as FloorApi.FloorVO
    if (formType.value === 'create') {
      await FloorApi.createFloor(data)
      message.success(t('common.createSuccess'))
    } else {
      await FloorApi.updateFloor(data)
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
    buildingId: undefined,
    name: '',
    code: '',
    floorNumber: undefined,
    floorType: '',
    floorHeight: undefined,
    floorArea: undefined,
    usableArea: undefined,
    primaryFunction: '',
    maxOccupancy: undefined,
    hasSprinkler: false,
    hasSmokeDetector: false,
    hasEmergencyExit: false,
    emergencyExitCount: undefined,
    description: ''
  }
  formRef.value?.resetFields()
}
</script>

