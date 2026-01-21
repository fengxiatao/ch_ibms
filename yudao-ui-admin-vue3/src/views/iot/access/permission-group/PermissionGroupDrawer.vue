<template>
  <el-drawer
    v-model="visible"
    :title="isEdit ? '编辑权限组' : '新增权限组'"
    size="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
      <!-- 基本信息区域 -->
      <el-divider content-position="left">基本信息</el-divider>
      
      <el-form-item label="组名" prop="groupName">
        <el-input v-model="formData.groupName" placeholder="请输入权限组名称" />
      </el-form-item>
      
      <el-form-item label="备注" prop="description">
        <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="请输入备注" />
      </el-form-item>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="权限组类型" prop="groupType">
            <el-select v-model="formData.groupType" placeholder="请选择权限组类型" style="width: 100%">
              <el-option label="门禁权限组" value="ACCESS" />
              <el-option label="考勤权限组" value="ATTENDANCE" />
              <el-option label="消费权限组" value="CONSUMPTION" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="时间模板" prop="timeTemplateId">
            <el-select v-model="formData.timeTemplateId" placeholder="请选择时间模板" style="width: 100%">
              <el-option
                v-for="template in timeTemplateList"
                :key="template.id"
                :label="template.templateName"
                :value="template.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      
      <!-- 认证方式区域 -->
      <el-divider content-position="left">认证方式</el-divider>
      
      <el-form-item label="认证方式" prop="authModes">
        <el-checkbox-group v-model="formData.authModes">
          <el-checkbox label="CARD">卡</el-checkbox>
          <el-checkbox label="FINGERPRINT">指纹</el-checkbox>
          <el-checkbox label="PASSWORD">密码</el-checkbox>
          <el-checkbox label="FACE">人脸</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      
      <!-- 设备选择区域 -->
      <el-divider content-position="left">设备选择</el-divider>
      
      <div class="device-selection-area">
        <el-row :gutter="20">
          <el-col :span="14">
            <div class="section-title">所有设备</div>
            <DeviceChannelTree
              ref="deviceTreeRef"
              v-model="selectedChannels"
              style="height: 300px"
            />
          </el-col>
          <el-col :span="10">
            <SelectedDeviceList
              v-model="selectedChannels"
              style="height: 300px"
              @remove="handleChannelRemove"
            />
          </el-col>
        </el-row>
      </div>
    </el-form>
    
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">确定</el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, reactive, watch, nextTick } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import DeviceChannelTree from './DeviceChannelTree.vue'
import SelectedDeviceList from './SelectedDeviceList.vue'
import type { SelectedChannel } from './DeviceChannelTree.vue'
import { AccessPermissionGroupApi, AccessTimeTemplateApi } from '@/api/iot/access'
import type { AccessPermissionGroupVO, AccessTimeTemplateVO } from '@/api/iot/access'
import { serializeAuthModes, deserializeAuthModes } from '@/utils/authModeUtils'


const props = defineProps<{
  modelValue: boolean
  editData?: AccessPermissionGroupVO | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}>()

const visible = ref(false)
const loading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const deviceTreeRef = ref<InstanceType<typeof DeviceChannelTree>>()

// 时间模板列表
const timeTemplateList = ref<AccessTimeTemplateVO[]>([])

// 选中的通道
const selectedChannels = ref<SelectedChannel[]>([])

// 表单数据
const formData = reactive({
  id: undefined as number | undefined,
  groupName: '',
  description: '',
  groupType: 'ACCESS',
  timeTemplateId: undefined as number | undefined,
  authModes: [] as string[],
  status: 1
})

// 表单验证规则
const formRules: FormRules = {
  groupName: [
    { required: true, message: '请输入权限组名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  authModes: [
    { required: true, message: '请至少选择一种认证方式', trigger: 'change', type: 'array', min: 1 }
  ]
}

/** 加载时间模板列表 */
const loadTimeTemplates = async () => {
  try {
    const res = await AccessTimeTemplateApi.getTemplateList()
    timeTemplateList.value = res || []
  } catch (error) {
    console.error('加载时间模板失败:', error)
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.id = undefined
  formData.groupName = ''
  formData.description = ''
  formData.groupType = 'ACCESS'
  formData.timeTemplateId = undefined
  formData.authModes = []
  formData.status = 1
  selectedChannels.value = []
  formRef.value?.resetFields()
}

/** 回填编辑数据 */
const fillEditData = (data: AccessPermissionGroupVO) => {
  formData.id = data.id
  formData.groupName = data.groupName
  formData.description = data.description || ''
  formData.timeTemplateId = data.timeTemplateId
  formData.status = data.status
  
  // 解析认证方式
  if (data.authMode) {
    // 如果是数字类型，转换为字符串数组
    if (typeof data.authMode === 'number') {
      // 旧格式兼容：数字映射到认证方式
      const modeMap: Record<number, string[]> = {
        1: ['CARD'],
        2: ['PASSWORD'],
        3: ['FINGERPRINT'],
        4: ['FACE'],
        5: ['CARD', 'PASSWORD'],
        6: ['CARD', 'FINGERPRINT'],
        7: ['CARD', 'FACE']
      }
      formData.authModes = modeMap[data.authMode] || []
    } else if (typeof data.authMode === 'string') {
      formData.authModes = deserializeAuthModes(data.authMode)
    }
  }
  
  // 回填已关联的设备通道
  if (data.devices && data.devices.length > 0) {
    selectedChannels.value = data.devices.map((device: any) => ({
      deviceId: device.deviceId,
      deviceName: device.deviceName || '',
      deviceIp: device.deviceIp || '',
      channelId: device.channelId,
      channelNo: device.channelNo || 0,
      channelName: device.channelName || ''
    }))
    // 等待树加载完成后设置选中状态
    nextTick(() => {
      setTimeout(() => {
        deviceTreeRef.value?.setCheckedByValue()
      }, 500)
    })
  }
}

/** 处理通道移除 */
const handleChannelRemove = (item: SelectedChannel) => {
  // 同步更新设备树的选中状态
  nextTick(() => {
    deviceTreeRef.value?.setCheckedByValue()
  })
}

/** 提交表单 */
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      const submitData = {
        ...formData,
        authMode: serializeAuthModes(formData.authModes),
        deviceIds: selectedChannels.value.map((c) => c.deviceId),
        channelIds: selectedChannels.value.map((c) => c.channelId)
      }
      
      if (isEdit.value && formData.id) {
        await AccessPermissionGroupApi.updateGroup(submitData as any)
        ElMessage.success('更新成功')
      } else {
        await AccessPermissionGroupApi.createGroup(submitData as any)
        ElMessage.success('创建成功')
      }
      
      emit('success')
      handleClose()
    } catch (error) {
      console.error('保存失败:', error)
    } finally {
      loading.value = false
    }
  })
}

/** 关闭抽屉 */
const handleClose = () => {
  visible.value = false
  emit('update:modelValue', false)
}

/** 打开抽屉 */
const open = (data?: AccessPermissionGroupVO | null) => {
  resetForm()
  loadTimeTemplates()
  
  if (data) {
    isEdit.value = true
    fillEditData(data)
  } else {
    isEdit.value = false
  }
  
  visible.value = true
}

// 监听外部visible变化
watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      open(props.editData)
    } else {
      visible.value = false
    }
  }
)

// 暴露方法
defineExpose({
  open
})
</script>

<style scoped lang="scss">
.device-selection-area {
  .section-title {
    font-weight: 500;
    margin-bottom: 12px;
    color: var(--el-text-color-primary);
  }
}
</style>
