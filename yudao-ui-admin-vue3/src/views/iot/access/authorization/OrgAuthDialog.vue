<template>
  <Dialog v-model="dialogVisible" :title="`组织授权 - ${org?.name || ''}`" width="900px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="组织名称">
        <el-input :value="org?.name" disabled />
      </el-form-item>
      <el-form-item label="授权类型">
        <el-tag :type="authType === 1 ? 'primary' : 'success'">
          {{ authType === 1 ? '物管授权' : '租户授权' }}
        </el-tag>
      </el-form-item>
      
      <el-divider>授权设备/门组</el-divider>
      
      <el-form-item label="授权方式" prop="authMethod">
        <el-radio-group v-model="authMethod">
          <el-radio label="device">门禁设备</el-radio>
          <el-radio label="group">门组</el-radio>
          <el-radio label="both">设备+门组</el-radio>
        </el-radio-group>
      </el-form-item>
      
      <!-- 门禁设备选择 -->
      <el-form-item
        label="选择设备"
        prop="deviceIds"
        v-if="authMethod === 'device' || authMethod === 'both'"
      >
        <el-transfer
          v-model="formData.deviceIds"
          :data="deviceList"
          :titles="['可选设备', '已选设备']"
          :props="{ key: 'id', label: 'name' }"
          filterable
          filter-placeholder="搜索设备"
          style="text-align: left; display: inline-block"
        />
        <div class="mt-2 text-sm text-gray-500">
          已选择 {{ formData.deviceIds?.length || 0 }} 个设备
        </div>
      </el-form-item>
      
      <!-- 门组选择 -->
      <el-form-item
        label="选择门组"
        prop="doorGroupIds"
        v-if="authMethod === 'group' || authMethod === 'both'"
      >
        <el-transfer
          v-model="formData.doorGroupIds"
          :data="doorGroupList"
          :titles="['可选门组', '已选门组']"
          :props="{ key: 'id', label: 'name' }"
          filterable
          filter-placeholder="搜索门组"
          style="text-align: left; display: inline-block"
        />
        <div class="mt-2 text-sm text-gray-500">
          已选择 {{ formData.doorGroupIds?.length || 0 }} 个门组
        </div>
      </el-form-item>
      
      <el-divider>授权时间</el-divider>
      
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

<script setup lang="ts" name="OrgAuthDialog">
import { ref, reactive, watch } from 'vue'
import * as AccessAuthorizationApi from '@/api/iot/access/authorization'
import * as DeviceApi from '@/api/iot/device/device'
import * as DoorGroupApi from '@/api/iot/access/doorGroup'
import { ElMessage } from 'element-plus'

const message = useMessage()

const dialogVisible = ref(false)
const formLoading = ref(false)
const formRef = ref()
const authMethod = ref<'device' | 'group' | 'both'>('device')
const timeRange = ref<[string, string] | null>(null)
const org = ref<any>(null)
const authType = ref(1)

const formData = ref({
  deviceIds: [] as number[],
  doorGroupIds: [] as number[],
  startTime: undefined,
  endTime: undefined,
  weekDays: '',
  timeSlots: '',
  authStatus: 1,
  remark: '',
  weekDaysArray: [] as string[]
})

const formRules = reactive({
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
const open = async (orgData: any, type: number) => {
  dialogVisible.value = true
  org.value = orgData
  authType.value = type
  resetForm()
  
  // 加载基础数据
  await Promise.all([
    loadDeviceList(),
    loadDoorGroupList()
  ])
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
    console.error('[组织授权] 加载设备列表失败:', error)
    ElMessage.error('加载设备列表失败')
  }
}

/** 加载门组列表 */
const loadDoorGroupList = async () => {
  try {
    const res = await DoorGroupApi.getDoorGroupPage({
      pageNo: 1,
      pageSize: 1000
    })
    doorGroupList.value = res.list.map(group => ({
      id: group.id,
      name: group.name
    }))
  } catch (error) {
    console.error('[组织授权] 加载门组列表失败:', error)
    ElMessage.error('加载门组列表失败')
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    deviceIds: [],
    doorGroupIds: [],
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
  
  // 验证至少选择一个设备或门组
  if (authMethod.value === 'device' && formData.value.deviceIds.length === 0) {
    ElMessage.warning('请至少选择一个设备')
    return
  }
  if (authMethod.value === 'group' && formData.value.doorGroupIds.length === 0) {
    ElMessage.warning('请至少选择一个门组')
    return
  }
  if (authMethod.value === 'both' && 
      formData.value.deviceIds.length === 0 && 
      formData.value.doorGroupIds.length === 0) {
    ElMessage.warning('请至少选择一个设备或门组')
    return
  }
  
  formLoading.value = true
  try {
    const promises: Promise<any>[] = []
    
    // 为每个设备创建授权
    if (formData.value.deviceIds.length > 0) {
      formData.value.deviceIds.forEach(deviceId => {
        const data = {
          authType: authType.value,
          orgId: org.value.id,
          deviceId: deviceId,
          startTime: formData.value.startTime,
          endTime: formData.value.endTime,
          weekDays: formData.value.weekDays,
          timeSlots: formData.value.timeSlots,
          authStatus: formData.value.authStatus,
          remark: formData.value.remark
        }
        promises.push(AccessAuthorizationApi.createAccessAuthorization(data))
      })
    }
    
    // 为每个门组创建授权
    if (formData.value.doorGroupIds.length > 0) {
      formData.value.doorGroupIds.forEach(doorGroupId => {
        const data = {
          authType: authType.value,
          orgId: org.value.id,
          doorGroupId: doorGroupId,
          startTime: formData.value.startTime,
          endTime: formData.value.endTime,
          weekDays: formData.value.weekDays,
          timeSlots: formData.value.timeSlots,
          authStatus: formData.value.authStatus,
          remark: formData.value.remark
        }
        promises.push(AccessAuthorizationApi.createAccessAuthorization(data))
      })
    }
    
    await Promise.all(promises)
    message.success(`成功创建 ${promises.length} 条授权记录`)
    dialogVisible.value = false
    emit('success', org.value.id)
  } catch (error) {
    console.error('[组织授权] 创建授权失败:', error)
    ElMessage.error('创建授权失败')
  } finally {
    formLoading.value = false
  }
}

const emit = defineEmits(['success'])
defineExpose({ open })
</script>

