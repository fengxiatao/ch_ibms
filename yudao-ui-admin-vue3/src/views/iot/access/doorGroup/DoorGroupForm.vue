<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="门组名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入门组名称" />
      </el-form-item>
      <el-form-item label="备注" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入备注信息"
        />
      </el-form-item>
      <el-form-item label="绑定设备" prop="devices">
        <el-transfer
          v-model="formData.devices"
          :data="deviceList"
          :titles="['可选设备', '已选设备']"
          :props="{ key: 'id', label: 'name' }"
          filterable
          filter-placeholder="搜索设备"
          style="text-align: left; display: inline-block"
        />
        <div class="mt-2 text-sm text-gray-500">
          已选择 {{ formData.devices?.length || 0 }} 个设备
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts" name="DoorGroupForm">
import { ref, reactive } from 'vue'
import * as DoorGroupApi from '@/api/iot/access/doorGroup'
import * as DeviceApi from '@/api/iot/device/device'
import { ElMessage } from 'element-plus'

const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formRef = ref()
const formData = ref({
  id: undefined,
  name: '',
  description: '',
  devices: [] as number[]
})

const formRules = reactive({
  name: [{ required: true, message: '请输入门组名称', trigger: 'blur' }]
})

// 设备列表
const deviceList = ref<any[]>([])

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增门组' : '编辑门组'
  resetForm()
  
  // 加载设备列表
  await loadDeviceList()
  
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      const doorGroup = await DoorGroupApi.getDoorGroup(id)
      formData.value = {
        id: doorGroup.id,
        name: doorGroup.name || '',
        description: doorGroup.description || '',
        devices: doorGroup.devices || []
      }
    } finally {
      formLoading.value = false
    }
  }
}

/** 加载设备列表 */
const loadDeviceList = async () => {
  try {
    const res = await DeviceApi.getDevicePage({
      pageNo: 1,
      pageSize: 100,
      subsystemCode: 'access.door'
    })
    deviceList.value = res.list.map(device => ({
      id: device.id,
      name: `${device.deviceName || device.nickname || '未知设备'} (${device.ipAddress || '无IP'})`
    }))
  } catch (error) {
    console.error('[门组管理] 加载设备列表失败:', error)
    ElMessage.error('加载设备列表失败')
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    name: '',
    description: '',
    devices: []
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
    const data = formData.value as any
    if (data.id) {
      await DoorGroupApi.updateDoorGroup(data)
      message.success('更新成功')
    } else {
      await DoorGroupApi.createDoorGroup(data)
      message.success('创建成功')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const emit = defineEmits(['success'])
defineExpose({ open })
</script>





















