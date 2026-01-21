<template>
  <div class="device-config-container">
    <el-card class="box-card">
      <!-- 页头 -->
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span>设备配置 - {{ deviceConfig?.deviceName }}</span>
            <el-tag v-if="hasChanges" type="warning" size="small" style="margin-left: 10px">
              有未保存的修改
            </el-tag>
          </div>
          <el-button-group>
            <el-button type="primary" :icon="Upload" @click="applyToDevice" :disabled="!hasChanges">
              应用到设备
            </el-button>
            <el-button :icon="Refresh" @click="syncFromDevice">从设备同步</el-button>
            <el-button :icon="RefreshLeft" @click="resetConfig">重置配置</el-button>
          </el-button-group>
        </div>
      </template>

      <!-- 配置Tab -->
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 网络配置 -->
        <el-tab-pane label="网络配置" name="network">
          <el-form
            ref="networkFormRef"
            :model="networkForm"
            :rules="networkRules"
            label-width="140px"
            style="max-width: 800px"
          >
            <el-form-item label="DHCP启用" prop="dhcpEnabled">
              <el-switch
                v-model="networkForm.dhcpEnabled"
                @change="handleNetworkChange"
              />
              <span class="form-item-tip">启用后将自动获取IP地址</span>
            </el-form-item>

            <el-form-item label="IP地址" prop="ipAddress">
              <el-input
                v-model="networkForm.ipAddress"
                :disabled="networkForm.dhcpEnabled"
                placeholder="例如: 192.168.1.202"
                @change="handleNetworkChange"
              />
            </el-form-item>

            <el-form-item label="子网掩码" prop="subnetMask">
              <el-input
                v-model="networkForm.subnetMask"
                :disabled="networkForm.dhcpEnabled"
                placeholder="例如: 255.255.255.0"
                @change="handleNetworkChange"
              />
            </el-form-item>

            <el-form-item label="网关" prop="gateway">
              <el-input
                v-model="networkForm.gateway"
                :disabled="networkForm.dhcpEnabled"
                placeholder="例如: 192.168.1.1"
                @change="handleNetworkChange"
              />
            </el-form-item>

            <el-form-item label="DNS服务器" prop="dns">
              <el-input
                v-model="networkForm.dns"
                placeholder="例如: 8.8.8.8"
                @change="handleNetworkChange"
              />
            </el-form-item>

            <el-divider content-position="left">端口配置</el-divider>

            <el-form-item label="HTTP端口" prop="httpPort">
              <el-input-number
                v-model="networkForm.httpPort"
                :min="1"
                :max="65535"
                @change="handleNetworkChange"
              />
            </el-form-item>

            <el-form-item label="RTSP端口" prop="rtspPort">
              <el-input-number
                v-model="networkForm.rtspPort"
                :min="1"
                :max="65535"
                @change="handleNetworkChange"
              />
            </el-form-item>

            <el-form-item label="ONVIF端口" prop="onvifPort">
              <el-input-number
                v-model="networkForm.onvifPort"
                :min="1"
                :max="65535"
                @change="handleNetworkChange"
              />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 事件配置 -->
        <el-tab-pane label="事件配置" name="event">
          <el-form
            ref="eventFormRef"
            :model="eventForm"
            :rules="eventRules"
            label-width="160px"
            style="max-width: 800px"
          >
            <el-form-item label="运动检测" prop="motionDetectionEnabled">
              <el-switch
                v-model="eventForm.motionDetectionEnabled"
                @change="handleEventChange"
              />
              <span class="form-item-tip">检测画面中的移动物体</span>
            </el-form-item>

            <el-form-item
              label="运动检测灵敏度"
              prop="motionSensitivity"
              v-if="eventForm.motionDetectionEnabled"
            >
              <el-slider
                v-model="eventForm.motionSensitivity"
                :min="1"
                :max="100"
                show-input
                @change="handleEventChange"
              />
              <span class="form-item-tip">灵敏度越高，越容易触发检测</span>
            </el-form-item>

            <el-form-item label="视频丢失检测" prop="videoLossEnabled">
              <el-switch
                v-model="eventForm.videoLossEnabled"
                @change="handleEventChange"
              />
              <span class="form-item-tip">检测视频信号丢失</span>
            </el-form-item>

            <el-form-item label="视频遮挡检测" prop="tamperDetectionEnabled">
              <el-switch
                v-model="eventForm.tamperDetectionEnabled"
                @change="handleEventChange"
              />
              <span class="form-item-tip">检测镜头被遮挡或移动</span>
            </el-form-item>

            <el-form-item label="音频检测" prop="audioDetectionEnabled">
              <el-switch
                v-model="eventForm.audioDetectionEnabled"
                @change="handleEventChange"
              />
              <span class="form-item-tip">检测异常声音</span>
            </el-form-item>

            <el-form-item
              label="音频检测阈值"
              prop="audioThreshold"
              v-if="eventForm.audioDetectionEnabled"
            >
              <el-slider
                v-model="eventForm.audioThreshold"
                :min="1"
                :max="100"
                show-input
                @change="handleEventChange"
              />
              <span class="form-item-tip">声音超过此阈值将触发告警</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 视频配置 -->
        <el-tab-pane label="视频配置" name="video">
          <el-form
            ref="videoFormRef"
            :model="videoForm"
            :rules="videoRules"
            label-width="140px"
            style="max-width: 800px"
          >
            <el-form-item label="分辨率" prop="resolution">
              <el-select
                v-model="videoForm.resolution"
                placeholder="请选择分辨率"
                @change="handleVideoChange"
              >
                <el-option
                  v-for="res in capabilities?.supportedResolutions || []"
                  :key="res"
                  :label="res"
                  :value="res"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="编码格式" prop="codecType">
              <el-select
                v-model="videoForm.codecType"
                placeholder="请选择编码格式"
                @change="handleVideoChange"
              >
                <el-option
                  v-for="codec in capabilities?.supportedCodecs || []"
                  :key="codec"
                  :label="codec"
                  :value="codec"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="帧率" prop="frameRate">
              <el-input-number
                v-model="videoForm.frameRate"
                :min="1"
                :max="capabilities?.maxFrameRate || 60"
                @change="handleVideoChange"
              />
              <span class="form-item-tip">fps</span>
            </el-form-item>

            <el-form-item label="码率" prop="bitrate">
              <el-input-number
                v-model="videoForm.bitrate"
                :min="128"
                :max="16384"
                :step="128"
                @change="handleVideoChange"
              />
              <span class="form-item-tip">kbps</span>
            </el-form-item>

            <el-form-item label="图像质量" prop="quality">
              <el-slider
                v-model="videoForm.quality"
                :min="1"
                :max="100"
                show-input
                @change="handleVideoChange"
              />
              <span class="form-item-tip">质量越高，文件越大</span>
            </el-form-item>

            <el-form-item label="GOP长度" prop="gopLength">
              <el-input-number
                v-model="videoForm.gopLength"
                :min="1"
                :max="300"
                @change="handleVideoChange"
              />
              <span class="form-item-tip">关键帧间隔</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 设备信息 -->
        <el-tab-pane label="设备信息" name="info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="设备ID">
              {{ deviceConfig?.deviceId }}
            </el-descriptions-item>
            <el-descriptions-item label="设备名称">
              {{ deviceConfig?.deviceName }}
            </el-descriptions-item>
            <el-descriptions-item label="制造商">
              {{ capabilities?.manufacturer || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="型号">
              {{ capabilities?.model || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="固件版本" :span="2">
              {{ capabilities?.firmwareVersion || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="PTZ支持">
              <el-tag :type="capabilities?.ptzSupported ? 'success' : 'info'" size="small">
                {{ capabilities?.ptzSupported ? '支持' : '不支持' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="音频支持">
              <el-tag :type="capabilities?.audioSupported ? 'success' : 'info'" size="small">
                {{ capabilities?.audioSupported ? '支持' : '不支持' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="录像支持">
              <el-tag :type="capabilities?.recordingSupported ? 'success' : 'info'" size="small">
                {{ capabilities?.recordingSupported ? '支持' : '不支持' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="DHCP支持">
              <el-tag :type="capabilities?.dhcpSupported ? 'success' : 'info'" size="small">
                {{ capabilities?.dhcpSupported ? '支持' : '不支持' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Upload, Refresh, RefreshLeft } from '@element-plus/icons-vue'
import {
  getDeviceConfig,
  updateNetworkConfig,
  updateEventConfig,
  updateVideoConfig,
  syncConfigFromDevice,
  applyConfigToDevice,
  resetDeviceConfig,
  getDeviceCapabilities,
  type DeviceConfigVO,
  type NetworkConfig,
  type EventConfig,
  type VideoConfig,
  type DeviceCapabilitiesVO
} from '@/api/iot/device/config'

const route = useRoute()
const deviceId = ref<number>(Number(route.query.deviceId))
const activeTab = ref('network')

// 表单引用
const networkFormRef = ref<FormInstance>()
const eventFormRef = ref<FormInstance>()
const videoFormRef = ref<FormInstance>()

// 数据
const deviceConfig = ref<DeviceConfigVO>()
const capabilities = ref<DeviceCapabilitiesVO>()
const hasChanges = ref(false)

// 表单数据
const networkForm = ref<NetworkConfig>({})
const eventForm = ref<EventConfig>({})
const videoForm = ref<VideoConfig>({})

// 校验规则
const networkRules: FormRules = {
  ipAddress: [
    { required: true, message: '请输入IP地址', trigger: 'blur' },
    {
      pattern: /^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/,
      message: 'IP地址格式不正确',
      trigger: 'blur'
    }
  ]
}

const eventRules: FormRules = {
  motionSensitivity: [
    { type: 'number', min: 1, max: 100, message: '灵敏度必须在1-100之间', trigger: 'change' }
  ]
}

const videoRules: FormRules = {
  resolution: [{ required: true, message: '请选择分辨率', trigger: 'change' }],
  codecType: [{ required: true, message: '请选择编码格式', trigger: 'change' }]
}

// 加载设备配置
const loadDeviceConfig = async () => {
  try {
    deviceConfig.value = await getDeviceConfig(deviceId.value)
    
    // 初始化表单数据
    if (deviceConfig.value.networkConfig) {
      networkForm.value = { ...deviceConfig.value.networkConfig }
    }
    if (deviceConfig.value.eventConfig) {
      eventForm.value = { ...deviceConfig.value.eventConfig }
    }
    if (deviceConfig.value.videoConfig) {
      videoForm.value = { ...deviceConfig.value.videoConfig }
    }
    
    hasChanges.value = false
  } catch (error) {
    console.error('加载设备配置失败:', error)
    ElMessage.error('加载设备配置失败')
  }
}

// 加载设备能力集
const loadDeviceCapabilities = async () => {
  try {
    capabilities.value = await getDeviceCapabilities(deviceId.value)
  } catch (error) {
    console.error('加载设备能力集失败:', error)
  }
}

// 处理配置变更
const handleNetworkChange = () => {
  hasChanges.value = true
}

const handleEventChange = () => {
  hasChanges.value = true
}

const handleVideoChange = () => {
  hasChanges.value = true
}

// 从设备同步配置
const syncFromDevice = async () => {
  try {
    await ElMessageBox.confirm('确定要从设备同步配置吗？这将覆盖当前未保存的修改。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const config = await syncConfigFromDevice(deviceId.value)
    deviceConfig.value = config
    
    // 更新表单数据
    if (config.networkConfig) {
      networkForm.value = { ...config.networkConfig }
    }
    if (config.eventConfig) {
      eventForm.value = { ...config.eventConfig }
    }
    if (config.videoConfig) {
      videoForm.value = { ...config.videoConfig }
    }
    
    hasChanges.value = false
    ElMessage.success('配置同步成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('配置同步失败:', error)
      ElMessage.error('配置同步失败')
    }
  }
}

// 应用配置到设备
const applyToDevice = async () => {
  try {
    await ElMessageBox.confirm('确定要将配置应用到设备吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 更新各个配置
    await updateNetworkConfig(deviceId.value, networkForm.value)
    await updateEventConfig(deviceId.value, eventForm.value)
    await updateVideoConfig(deviceId.value, videoForm.value)

    // 应用到设备
    await applyConfigToDevice(deviceId.value)
    
    hasChanges.value = false
    ElMessage.success('配置已成功应用到设备')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('应用配置失败:', error)
      ElMessage.error('应用配置失败')
    }
  }
}

// 重置配置
const resetConfig = async () => {
  try {
    await ElMessageBox.confirm('确定要重置设备配置为默认值吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await resetDeviceConfig(deviceId.value)
    await loadDeviceConfig()
    
    ElMessage.success('配置已重置')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置配置失败:', error)
      ElMessage.error('重置配置失败')
    }
  }
}

// 生命周期
onMounted(() => {
  loadDeviceConfig()
  loadDeviceCapabilities()
})
</script>

<style scoped lang="scss">
.device-config-container {
  padding: 20px;

  .box-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-left {
        display: flex;
        align-items: center;
      }
    }

    .form-item-tip {
      margin-left: 10px;
      color: #909399;
      font-size: 12px;
    }
  }
}
</style>












