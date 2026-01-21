<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="700px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="点位名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入点位名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="点位编码" prop="code">
            <el-input v-model="formData.code" placeholder="请输入点位编码" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="点位类型" prop="type">
            <el-select v-model="formData.type" placeholder="请选择点位类型" class="w-full">
              <el-option label="普通" :value="1" />
              <el-option label="RFID" :value="2" />
              <el-option label="二维码" :value="3" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="RFID标签" prop="rfidTag" v-if="formData.type === 2">
            <el-input v-model="formData.rfidTag" placeholder="请输入RFID标签" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="位置信息">
        <el-row :gutter="10" class="w-full">
          <el-col :span="8">
            <el-form-item label="经度" prop="longitude" label-width="50px">
              <el-input-number
                v-model="formData.longitude"
                :precision="6"
                :step="0.000001"
                class="w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="纬度" prop="latitude" label-width="50px">
              <el-input-number
                v-model="formData.latitude"
                :precision="6"
                :step="0.000001"
                class="w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="海拔" prop="altitude" label-width="50px">
              <el-input-number v-model="formData.altitude" :precision="2" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>
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
import { ref, reactive } from 'vue'
import * as PatrolPointApi from '@/api/iot/patrol/point'

const message = useMessage()
const emit = defineEmits(['success'])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref({
  id: undefined,
  name: '',
  code: '',
  description: '',
  type: 1,
  longitude: undefined,
  latitude: undefined,
  altitude: undefined,
  rfidTag: '',
  floorId: undefined,
  buildingId: undefined,
  campusId: undefined
})

const formRules = reactive({
  name: [{ required: true, message: '点位名称不能为空', trigger: 'blur' }],
  code: [{ required: true, message: '点位编码不能为空', trigger: 'blur' }],
  type: [{ required: true, message: '点位类型不能为空', trigger: 'change' }]
})

const formRef = ref()

// 打开弹窗
const open = async (type: string, id?: number, presetFloorId?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增巡更点位' : '修改巡更点位'
  formType.value = type
  resetForm()
  
  // 新增时，如果预设了楼层ID，则设置
  if (type === 'create' && presetFloorId) {
    formData.value.floorId = presetFloorId
  }
  
  // 修改时，加载数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await PatrolPointApi.getPatrolPoint(id)
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
      await PatrolPointApi.createPatrolPoint(data)
      message.success('新增成功')
    } else {
      await PatrolPointApi.updatePatrolPoint(data)
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
    code: '',
    description: '',
    type: 1,
    longitude: undefined,
    latitude: undefined,
    altitude: undefined,
    rfidTag: '',
    floorId: undefined,
    buildingId: undefined,
    campusId: undefined
  }
  formRef.value?.resetFields()
}

defineExpose({ open })
</script>








