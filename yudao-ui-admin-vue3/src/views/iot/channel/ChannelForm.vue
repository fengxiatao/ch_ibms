<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="800px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="所属设备" prop="deviceId">
            <el-input v-model="formData.deviceId" placeholder="请输入设备ID" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="设备类型" prop="deviceType">
            <el-select v-model="formData.deviceType" placeholder="请选择设备类型" @change="handleDeviceTypeChange">
              <el-option label="NVR" value="NVR" />
              <el-option label="DVR" value="DVR" />
              <el-option label="独立摄像头" value="IPC" />
              <el-option label="门禁控制器" value="ACCESS_CONTROLLER" />
              <el-option label="消防主机" value="FIRE_PANEL" />
              <el-option label="电表" value="METER" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="通道号" prop="channelNo">
            <el-input-number v-model="formData.channelNo" :min="1" placeholder="请输入通道号" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="通道名称" prop="channelName">
            <el-input v-model="formData.channelName" placeholder="请输入通道名称" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="通道类型" prop="channelType">
            <el-select v-model="formData.channelType" placeholder="请选择通道类型">
              <el-option label="视频" value="VIDEO" />
              <el-option label="门禁" value="ACCESS" />
              <el-option label="消防" value="FIRE" />
              <el-option label="能源" value="ENERGY" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="通道编码" prop="channelCode">
            <el-input v-model="formData.channelCode" placeholder="请输入通道编码" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="安装位置" prop="location">
        <el-input v-model="formData.location" placeholder="请输入安装位置" />
      </el-form-item>

      <!-- 视频通道专用字段 -->
      <template v-if="formData.channelType === 'VIDEO'">
        <el-divider content-position="left">视频通道配置</el-divider>
        
        <!-- 独立摄像头：直接填写摄像头IP -->
        <template v-if="formData.deviceType === 'IPC'">
          <el-alert 
            title="独立摄像头模式：直接连接摄像头，不通过NVR" 
            type="info" 
            :closable="false"
            style="margin-bottom: 15px"
          />
          <el-form-item label="摄像头IP" prop="targetIp">
            <el-input v-model="formData.targetIp" placeholder="请输入摄像头IP地址，如：192.168.1.206" />
          </el-form-item>
        </template>
        
        <!-- NVR/DVR：需要填写目标摄像头信息 -->
        <template v-else-if="formData.deviceType === 'NVR' || formData.deviceType === 'DVR'">
          <el-alert 
            title="NVR通道模式：通过NVR访问摄像头" 
            type="info" 
            :closable="false"
            style="margin-bottom: 15px"
          />
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="目标摄像头IP" prop="targetIp">
                <el-input v-model="formData.targetIp" placeholder="摄像头实际IP" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="物理通道号" prop="targetChannelNo">
                <el-input-number v-model="formData.targetChannelNo" :min="1" placeholder="摄像头物理通道" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <el-form-item label="主码流地址" prop="streamUrlMain">
          <el-input v-model="formData.streamUrlMain" placeholder="rtsp://..." />
        </el-form-item>

        <el-form-item label="子码流地址" prop="streamUrlSub">
          <el-input v-model="formData.streamUrlSub" placeholder="rtsp://..." />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="云台支持" prop="ptzSupport">
              <el-switch v-model="formData.ptzSupport" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="音频支持" prop="audioSupport">
              <el-switch v-model="formData.audioSupport" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分辨率" prop="resolution">
              <el-input v-model="formData.resolution" placeholder="1920x1080" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="加入巡更" prop="isPatrol">
              <el-switch v-model="formData.isPatrol" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="加入监控墙" prop="isMonitor">
              <el-switch v-model="formData.isMonitor" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="巡更时长(秒)" prop="patrolDuration">
              <el-input-number v-model="formData.patrolDuration" :min="5" :max="300" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </template>

      <!-- 门禁通道专用字段 -->
      <template v-if="formData.channelType === 'ACCESS'">
        <el-divider content-position="left">门禁通道配置</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="门点名称" prop="doorName">
              <el-input v-model="formData.doorName" placeholder="请输入门点名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="门方向" prop="doorDirection">
              <el-select v-model="formData.doorDirection" placeholder="请选择门方向">
                <el-option label="进" value="IN" />
                <el-option label="出" value="OUT" />
                <el-option label="双向" value="BOTH" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="读卡器类型" prop="cardReaderType">
              <el-select v-model="formData.cardReaderType" placeholder="请选择读卡器类型">
                <el-option label="IC卡" value="IC" />
                <el-option label="ID卡" value="ID" />
                <el-option label="指纹" value="FINGERPRINT" />
                <el-option label="人脸" value="FACE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="锁类型" prop="lockType">
              <el-select v-model="formData.lockType" placeholder="请选择锁类型">
                <el-option label="电插锁" value="ELECTRIC" />
                <el-option label="磁力锁" value="MAGNETIC" />
                <el-option label="电机锁" value="MOTOR" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 门禁通道位置配置 -->
        <el-divider content-position="left">位置信息</el-divider>
        <el-alert 
          title="门禁通道通常需要单独标注位置（门的实际位置），不同于门禁控制器的位置" 
          type="warning" 
          :closable="false"
          style="margin-bottom: 15px"
        />
        <el-form-item label="使用自定义位置">
          <el-switch v-model="formData.useCustomPosition" />
          <span style="margin-left: 10px; color: #909399; font-size: 12px">
            开启后可以在平面图上单独标注此门点的位置
          </span>
        </el-form-item>
        <template v-if="formData.useCustomPosition">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="X坐标(米)" prop="xCoordinate">
                <el-input-number v-model="formData.xCoordinate" :precision="2" :step="0.1" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="Y坐标(米)" prop="yCoordinate">
                <el-input-number v-model="formData.yCoordinate" :precision="2" :step="0.1" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="Z坐标(米)" prop="zCoordinate">
                <el-input-number v-model="formData.zCoordinate" :precision="2" :step="0.1" :min="0" placeholder="安装高度" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-alert 
            title="提示：Z坐标表示安装高度，如读卡器1.2米、烟感2.8米、摄像头3.5米" 
            type="info" 
            :closable="false"
            style="margin-bottom: 15px"
          />
        </template>
      </template>

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
import * as ChannelApi from '@/api/iot/channel'

defineOptions({ name: 'ChannelForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  deviceId: undefined,
  deviceType: '',
  channelNo: undefined as number | undefined,
  channelName: '',
  channelCode: '',
  channelType: 'VIDEO',
  location: '',
  targetIp: '',
  targetChannelNo: 1 as number | undefined,
  streamUrlMain: '',
  streamUrlSub: '',
  ptzSupport: false,
  audioSupport: false,
  resolution: '1920x1080',
  isPatrol: false,
  isMonitor: false,
  patrolDuration: 30,
  doorName: '',
  doorDirection: '',
  cardReaderType: '',
  lockType: '',
  useCustomPosition: false,
  xCoordinate: undefined as number | undefined,
  yCoordinate: undefined as number | undefined,
  zCoordinate: undefined as number | undefined,
  description: ''
})
const formRules = reactive({
  deviceId: [{ required: true, message: '所属设备不能为空', trigger: 'blur' }],
  deviceType: [{ required: true, message: '设备类型不能为空', trigger: 'change' }],
  channelNo: [{ required: true, message: '通道号不能为空', trigger: 'blur' }],
  channelName: [{ required: true, message: '通道名称不能为空', trigger: 'blur' }],
  channelType: [{ required: true, message: '通道类型不能为空', trigger: 'change' }]
})
const formRef = ref() // 表单 Ref

/** 设备类型切换处理 */
const handleDeviceTypeChange = (deviceType: string) => {
  // 独立摄像头：通道号默认为1，不需要目标设备信息
  if (deviceType === 'IPC') {
    formData.value.channelNo = 1
    formData.value.targetChannelNo = 1
    formData.value.channelType = 'VIDEO'
  }
  // NVR/DVR：需要填写目标设备信息
  else if (deviceType === 'NVR' || deviceType === 'DVR') {
    formData.value.channelType = 'VIDEO'
  }
  // 门禁控制器
  else if (deviceType === 'ACCESS_CONTROLLER') {
    formData.value.channelType = 'ACCESS'
  }
  // 消防主机
  else if (deviceType === 'FIRE_PANEL') {
    formData.value.channelType = 'FIRE'
  }
  // 电表
  else if (deviceType === 'METER') {
    formData.value.channelType = 'ENERGY'
  }
}

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ChannelApi.getChannel(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as ChannelApi.ChannelVO
    if (formType.value === 'create') {
      await ChannelApi.createChannel(data)
      message.success(t('common.createSuccess'))
    } else {
      await ChannelApi.updateChannel(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    deviceId: undefined,
    deviceType: '',
    channelNo: undefined,
    channelName: '',
    channelCode: '',
    channelType: 'VIDEO',
    location: '',
    targetIp: '',
    targetChannelNo: 1,
    streamUrlMain: '',
    streamUrlSub: '',
    ptzSupport: false,
    audioSupport: false,
    resolution: '1920x1080',
    isPatrol: false,
    isMonitor: false,
    patrolDuration: 30,
    doorName: '',
    doorDirection: '',
    cardReaderType: '',
    lockType: '',
    useCustomPosition: false,
    xCoordinate: undefined,
    yCoordinate: undefined,
    zCoordinate: undefined,
    description: ''
  }
  formRef.value?.resetFields()
}
</script>
