<template>
  <Dialog v-model="dialogVisible" title="下发访客权限" width="700px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" v-loading="loading">
      <!-- 访客信息（只读） -->
      <el-descriptions :column="2" border class="mb-16">
        <el-descriptions-item label="访客姓名">{{ applyInfo.visitorName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ applyInfo.visitorPhone }}</el-descriptions-item>
        <el-descriptions-item label="被访人">{{ applyInfo.visiteeName }}</el-descriptions-item>
        <el-descriptions-item label="来访事由">{{ applyInfo.visitReason }}</el-descriptions-item>
      </el-descriptions>

      <!-- 授权配置 -->
      <el-divider content-position="left">授权配置</el-divider>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="授权类型" prop="authType">
            <el-radio-group v-model="formData.authType">
              <el-radio :label="1">按时间段</el-radio>
              <el-radio :label="2">按次数</el-radio>
              <el-radio :label="3">时间+次数</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="时间模板" prop="timeTemplateId">
            <el-select v-model="formData.timeTemplateId" placeholder="可选择时间模板" clearable style="width: 100%">
              <el-option
                v-for="item in timeTemplates"
                :key="item.id"
                :label="item.templateName"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="授权开始" prop="authStartTime">
            <el-date-picker
              v-model="formData.authStartTime"
              type="datetime"
              placeholder="选择开始时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="授权结束" prop="authEndTime">
            <el-date-picker
              v-model="formData.authEndTime"
              type="datetime"
              placeholder="选择结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row v-if="formData.authType === 2 || formData.authType === 3" :gutter="20">
        <el-col :span="12">
          <el-form-item label="最大次数" prop="maxAccessCount">
            <el-input-number
              v-model="formData.maxAccessCount"
              :min="1"
              :max="9999"
              placeholder="总通行次数限制"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="每日限制" prop="dailyAccessLimit">
            <el-input-number
              v-model="formData.dailyAccessLimit"
              :min="1"
              :max="999"
              placeholder="每日通行次数（可选）"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 凭证配置 -->
      <el-divider content-position="left">凭证配置</el-divider>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="门禁卡号" prop="cardNo">
            <el-input v-model="formData.cardNo" placeholder="请输入门禁卡号（可选）" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="人脸照片" prop="faceUrl">
            <el-input v-model="formData.faceUrl" placeholder="人脸照片URL（可选）">
              <template #append>
                <el-button>上传</el-button>
              </template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 设备选择 -->
      <el-divider content-position="left">授权设备</el-divider>

      <el-form-item label="选择设备" prop="deviceIds">
        <el-tree-select
          v-model="formData.deviceIds"
          :data="deviceTree"
          :props="{ label: 'name', children: 'children', value: 'id' }"
          multiple
          filterable
          check-strictly
          collapse-tags
          collapse-tags-tooltip
          placeholder="请选择授权的门禁设备"
          style="width: 100%"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">下发权限</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { VisitorApplyApi, type VisitorApplyVO, type VisitorAuthDispatchReqVO } from '@/api/iot/visitor'
import { AccessTimeTemplateApi, AccessManagementApi, type AccessTimeTemplateVO } from '@/api/iot/access'

const emit = defineEmits(['success'])

const dialogVisible = ref(false)
const loading = ref(false)
const submitting = ref(false)
const applyId = ref<number>()
const applyInfo = ref<Partial<VisitorApplyVO>>({})
const timeTemplates = ref<AccessTimeTemplateVO[]>([])
const deviceTree = ref<any[]>([])

const formRef = ref()

const formData = reactive<VisitorAuthDispatchReqVO>({
  applyId: 0,
  authType: 1,
  timeTemplateId: undefined,
  authStartTime: undefined as unknown as Date,
  authEndTime: undefined as unknown as Date,
  maxAccessCount: undefined,
  dailyAccessLimit: undefined,
  cardNo: '',
  faceUrl: '',
  deviceIds: []
})

const rules = {
  authType: [{ required: true, message: '请选择授权类型', trigger: 'change' }],
  authStartTime: [{ required: true, message: '请选择授权开始时间', trigger: 'change' }],
  authEndTime: [{ required: true, message: '请选择授权结束时间', trigger: 'change' }],
  deviceIds: [{ required: true, message: '请选择授权设备', trigger: 'change', type: 'array', min: 1 }]
}

// 加载时间模板
const loadTimeTemplates = async () => {
  try {
    timeTemplates.value = await AccessTimeTemplateApi.getTemplateList()
  } catch (e) {
    console.error('加载时间模板失败', e)
  }
}

// 加载设备树
const loadDeviceTree = async () => {
  try {
    const tree = await AccessManagementApi.getControllerTree()
    // 转换为树形结构
    deviceTree.value = tree.map(device => ({
      id: device.deviceId,
      name: `${device.deviceName}${device.online ? '' : ' (离线)'}`,
      disabled: !device.online,
      children: device.channels?.map(ch => ({
        id: `${device.deviceId}_${ch.channelId}`,
        name: ch.channelName || `通道${ch.channelNo}`,
        deviceId: device.deviceId,
        channelId: ch.channelId
      }))
    }))
  } catch (e) {
    console.error('加载设备树失败', e)
  }
}

// 打开弹窗
const open = async (id: number) => {
  applyId.value = id
  dialogVisible.value = true
  
  // 重置表单
  Object.assign(formData, {
    applyId: id,
    authType: 1,
    timeTemplateId: undefined,
    authStartTime: undefined,
    authEndTime: undefined,
    maxAccessCount: undefined,
    dailyAccessLimit: undefined,
    cardNo: '',
    faceUrl: '',
    deviceIds: []
  })

  loading.value = true
  try {
    // 加载申请信息
    const data = await VisitorApplyApi.getApply(id)
    applyInfo.value = data
    
    // 如果已有授权信息，回显
    if (data.authStatus !== undefined) {
      formData.authType = data.authType || 1
      formData.timeTemplateId = data.timeTemplateId
      formData.authStartTime = data.authStartTime as any
      formData.authEndTime = data.authEndTime as any
      formData.maxAccessCount = data.maxAccessCount
      formData.dailyAccessLimit = data.dailyAccessLimit
      formData.cardNo = data.cardNo || ''
      formData.faceUrl = data.faceUrl || ''
      
      // 回显已选设备
      if (data.authDevices && data.authDevices.length) {
        formData.deviceIds = data.authDevices.map(d => d.deviceId)
      }
    } else {
      // 默认授权时间为来访时间范围
      formData.authStartTime = data.planVisitTime as any
      formData.authEndTime = data.planLeaveTime as any
      formData.faceUrl = data.faceUrl || ''
    }
  } finally {
    loading.value = false
  }
}

// 提交
const handleSubmit = async () => {
  await formRef.value?.validate()
  
  submitting.value = true
  try {
    // 处理设备ID（可能包含通道）
    const deviceIds: number[] = []
    const channelIds: number[] = []
    formData.deviceIds.forEach(id => {
      if (typeof id === 'string' && id.includes('_')) {
        const [deviceId, channelId] = id.split('_').map(Number)
        if (!deviceIds.includes(deviceId)) {
          deviceIds.push(deviceId)
        }
        channelIds.push(channelId)
      } else {
        deviceIds.push(Number(id))
      }
    })

    await VisitorApplyApi.dispatchAuth({
      ...formData,
      deviceIds,
      channelIds: channelIds.length ? channelIds : undefined
    })
    ElMessage.success('权限下发中，请稍后查看下发结果')
    dialogVisible.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadTimeTemplates()
  loadDeviceTree()
})

defineExpose({ open })
</script>

<style scoped lang="scss">
.mb-16 {
  margin-bottom: 16px;
}
</style>
