<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="授权类型" prop="authType">
        <el-radio-group v-model="formData.authType">
          <el-radio :label="1">物管授权</el-radio>
          <el-radio :label="2">租户授权</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="人员ID" prop="personId" v-if="formData.authType === 2">
        <el-input-number v-model="formData.personId" placeholder="请输入人员ID" class="w-full" />
      </el-form-item>
      <el-form-item label="组织ID" prop="orgId" v-if="formData.authType === 1">
        <el-input-number v-model="formData.orgId" placeholder="请输入组织ID" class="w-full" />
      </el-form-item>
      <el-form-item label="授权方式" prop="authMethod">
        <el-radio-group v-model="authMethod">
          <el-radio label="device">门禁授权</el-radio>
          <el-radio label="group">门组授权</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="选择设备" prop="deviceId" v-if="authMethod === 'device'">
        <el-select
          v-model="formData.deviceId"
          placeholder="请选择门禁设备"
          clearable
          filterable
          class="w-full"
        >
          <el-option
            v-for="device in deviceList"
            :key="device.id"
            :label="`${device.name} (${device.ipAddress || '无IP'})`"
            :value="device.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="选择门组" prop="doorGroupId" v-if="authMethod === 'group'">
        <el-select
          v-model="formData.doorGroupId"
          placeholder="请选择门组"
          clearable
          filterable
          class="w-full"
        >
          <el-option
            v-for="group in doorGroupList"
            :key="group.id"
            :label="group.name"
            :value="group.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="授权时间" prop="timeRange">
        <el-date-picker
          v-model="timeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          class="w-full"
        />
      </el-form-item>
      <el-form-item label="星期限制" prop="weekDays">
        <el-checkbox-group v-model="formData.weekDaysArray">
          <el-checkbox label="1">周一</el-checkbox>
          <el-checkbox label="2">周二</el-checkbox>
          <el-checkbox label="3">周三</el-checkbox>
          <el-checkbox label="4">周四</el-checkbox>
          <el-checkbox label="5">周五</el-checkbox>
          <el-checkbox label="6">周六</el-checkbox>
          <el-checkbox label="7">周日</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item label="授权状态" prop="authStatus">
        <el-radio-group v-model="formData.authStatus">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="2">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注信息"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts" name="AuthorizationForm">
import { ref, reactive, watch } from 'vue'
import * as AccessAuthorizationApi from '@/api/iot/access/authorization'
import * as DeviceApi from '@/api/iot/device/device'
import * as DoorGroupApi from '@/api/iot/access/doorGroup'
import { ElMessage } from 'element-plus'

const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formRef = ref()
const authMethod = ref<'device' | 'group'>('device')
const timeRange = ref<[string, string] | null>(null)

const formData = ref({
  id: undefined,
  authType: 1,
  orgId: undefined,
  personId: undefined,
  deviceId: undefined,
  doorGroupId: undefined,
  startTime: undefined,
  endTime: undefined,
  weekDays: '',
  timeSlots: '',
  authStatus: 1,
  remark: '',
  weekDaysArray: [] as string[]
})

const formRules = reactive({
  authType: [{ required: true, message: '请选择授权类型', trigger: 'change' }],
  authStatus: [{ required: true, message: '请选择授权状态', trigger: 'change' }]
})

// 设备列表和门组列表
const deviceList = ref<any[]>([])
const doorGroupList = ref<any[]>([])

// 监听时间范围变化
watch(timeRange, (val) => {
  if (val && val.length === 2) {
    formData.value.startTime = new Date(val[0]) as any
    formData.value.endTime = new Date(val[1]) as any
  } else {
    formData.value.startTime = undefined
    formData.value.endTime = undefined
  }
})

// 监听星期数组变化
watch(() => formData.value.weekDaysArray, (val) => {
  formData.value.weekDays = val.sort().join(',')
}, { deep: true })

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增授权' : '编辑授权'
  resetForm()
  
  // 加载基础数据
  await Promise.all([
    loadDeviceList(),
    loadDoorGroupList()
  ])
  
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      const auth = await AccessAuthorizationApi.getAccessAuthorization(id)
      formData.value = {
        id: auth.id,
        authType: auth.authType,
        orgId: auth.orgId,
        personId: auth.personId,
        deviceId: auth.deviceId,
        doorGroupId: auth.doorGroupId,
        startTime: auth.startTime,
        endTime: auth.endTime,
        weekDays: auth.weekDays || '',
        timeSlots: auth.timeSlots || '',
        authStatus: auth.authStatus,
        remark: auth.remark || '',
        weekDaysArray: auth.weekDays ? auth.weekDays.split(',') : []
      }
      
      // 设置时间范围
      if (auth.startTime && auth.endTime) {
        timeRange.value = [
          new Date(auth.startTime).toISOString().slice(0, 19).replace('T', ' '),
          new Date(auth.endTime).toISOString().slice(0, 19).replace('T', ' ')
        ]
      }
      
      // 设置授权方式
      authMethod.value = auth.deviceId ? 'device' : 'group'
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
    deviceList.value = res.list
  } catch (error) {
    console.error('[授权管理] 加载设备列表失败:', error)
  }
}

/** 加载门组列表 */
const loadDoorGroupList = async () => {
  try {
    const res = await DoorGroupApi.getDoorGroupPage({
      pageNo: 1,
      pageSize: 1000
    })
    doorGroupList.value = res.list
  } catch (error) {
    console.error('[授权管理] 加载门组列表失败:', error)
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    authType: 1,
    orgId: undefined,
    personId: undefined,
    deviceId: undefined,
    doorGroupId: undefined,
    startTime: undefined,
    endTime: undefined,
    weekDays: '',
    timeSlots: '',
    authStatus: 1,
    remark: '',
    weekDaysArray: []
  }
  timeRange.value = null
  authMethod.value = 'device'
  formRef.value?.resetFields()
}

/** 提交表单 */
const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return
  
  // 验证授权方式
  if (authMethod.value === 'device' && !formData.value.deviceId) {
    ElMessage.warning('请选择门禁设备')
    return
  }
  if (authMethod.value === 'group' && !formData.value.doorGroupId) {
    ElMessage.warning('请选择门组')
    return
  }
  
  formLoading.value = true
  try {
    const data = { ...formData.value } as any
    // 根据授权方式清除另一个字段
    if (authMethod.value === 'device') {
      data.doorGroupId = undefined
    } else {
      data.deviceId = undefined
    }
    
    if (data.id) {
      await AccessAuthorizationApi.updateAccessAuthorization(data)
      message.success('更新成功')
    } else {
      await AccessAuthorizationApi.createAccessAuthorization(data)
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

