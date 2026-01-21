<template>
  <Dialog
    :title="`${deviceName} - 定时任务配置`"
    v-model="dialogVisible"
    width="800px"
  >
    <el-alert
      title="说明"
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 20px"
    >
      <template #default>
        <div><strong>设备级配置优先于产品级配置</strong></div>
        <div style="margin-top: 5px; font-size: 12px; color: #909399">
          • 设备启用任务：无论产品配置如何，都会执行设备的配置<br />
          • 设备禁用任务：即使产品启用，也不会执行该任务<br />
          • 设备未配置：继承产品的配置（如果产品有配置）
        </div>
        <div v-if="productConfig" style="margin-top: 10px; padding: 8px; background: #f0f9ff; border-radius: 4px;">
          <div style="font-size: 12px; color: #1890ff;">
            <Icon icon="ep:info-filled" /> 当前产品配置：
          </div>
          <div style="font-size: 11px; color: #666; margin-top: 4px;">
            离线检查: {{ productConfig.offlineCheck?.enabled ? `启用 (${productConfig.offlineCheck.interval}${productConfig.offlineCheck.unit === 'MINUTE' ? '分钟' : '小时'})` : '禁用' }} |
            健康检查: {{ productConfig.healthCheck?.enabled ? `启用 (${productConfig.healthCheck.interval}${productConfig.healthCheck.unit === 'MINUTE' ? '分钟' : '小时'})` : '禁用' }} |
            数据采集: {{ productConfig.dataCollect?.enabled ? `启用 (${productConfig.dataCollect.interval}${productConfig.dataCollect.unit === 'MINUTE' ? '分钟' : '小时'})` : '禁用' }}
          </div>
        </div>
      </template>
    </el-alert>

    <el-form
      ref="formRef"
      :model="formData"
      label-width="120px"
      v-loading="formLoading"
    >
      <!-- 设备离线检查 -->
      <el-card shadow="never" style="margin-bottom: 20px">
        <template #header>
          <div style="display: flex; align-items: center; justify-content: space-between">
            <span>
              <el-icon style="margin-right: 5px"><Monitor /></el-icon>
              设备离线检查
            </span>
            <div style="display: flex; align-items: center; gap: 10px">
              <el-tag v-if="productConfig?.offlineCheck?.enabled" size="small" type="info">
                产品配置: {{ productConfig.offlineCheck.interval }}{{ productConfig.offlineCheck.unit === 'MINUTE' ? '分钟' : '小时' }}
              </el-tag>
              <el-switch
                v-model="formData.offlineCheck.enabled"
                active-text="启用"
                inactive-text="禁用"
              />
            </div>
          </div>
        </template>
        
        <el-form-item label="检查间隔">
          <el-input-number
            v-model="formData.offlineCheck.interval"
            :min="1"
            :max="1440"
            :disabled="!formData.offlineCheck.enabled"
            style="width: 150px"
          />
          <el-select
            v-model="formData.offlineCheck.unit"
            :disabled="!formData.offlineCheck.enabled"
            style="width: 100px; margin-left: 10px"
          >
            <el-option label="分钟" value="MINUTE" />
            <el-option label="小时" value="HOUR" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="任务优先级">
          <el-select
            v-model="formData.offlineCheck.priority"
            :disabled="!formData.offlineCheck.enabled"
            style="width: 100%"
          >
            <el-option label="关键任务(1)" :value="1" />
            <el-option label="高优先级(3)" :value="3" />
            <el-option label="普通优先级(5)" :value="5" />
            <el-option label="低优先级(7)" :value="7" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input
            v-model="formData.offlineCheck.description"
            :disabled="!formData.offlineCheck.enabled"
            placeholder="任务描述"
          />
        </el-form-item>
      </el-card>

      <!-- 设备健康检查 -->
      <el-card shadow="never" style="margin-bottom: 20px">
        <template #header>
          <div style="display: flex; align-items: center; justify-content: space-between">
            <span>
              <el-icon style="margin-right: 5px"><CircleCheck /></el-icon>
              设备健康检查
            </span>
            <div style="display: flex; align-items: center; gap: 10px">
              <el-tag v-if="productConfig?.healthCheck?.enabled" size="small" type="info">
                产品配置: {{ productConfig.healthCheck.interval }}{{ productConfig.healthCheck.unit === 'MINUTE' ? '分钟' : '小时' }}
              </el-tag>
              <el-switch
                v-model="formData.healthCheck.enabled"
                active-text="启用"
                inactive-text="禁用"
              />
            </div>
          </div>
        </template>
        
        <el-form-item label="检查间隔">
          <el-input-number
            v-model="formData.healthCheck.interval"
            :min="1"
            :max="1440"
            :disabled="!formData.healthCheck.enabled"
            style="width: 150px"
          />
          <el-select
            v-model="formData.healthCheck.unit"
            :disabled="!formData.healthCheck.enabled"
            style="width: 100px; margin-left: 10px"
          >
            <el-option label="分钟" value="MINUTE" />
            <el-option label="小时" value="HOUR" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="任务优先级">
          <el-select
            v-model="formData.healthCheck.priority"
            :disabled="!formData.healthCheck.enabled"
            style="width: 100%"
          >
            <el-option label="关键任务(1)" :value="1" />
            <el-option label="高优先级(3)" :value="3" />
            <el-option label="普通优先级(5)" :value="5" />
            <el-option label="低优先级(7)" :value="7" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input
            v-model="formData.healthCheck.description"
            :disabled="!formData.healthCheck.enabled"
            placeholder="任务描述"
          />
        </el-form-item>
      </el-card>

      <!-- 设备数据采集 -->
      <el-card shadow="never">
        <template #header>
          <div style="display: flex; align-items: center; justify-content: space-between">
            <span>
              <el-icon style="margin-right: 5px"><DataAnalysis /></el-icon>
              设备数据采集
            </span>
            <div style="display: flex; align-items: center; gap: 10px">
              <el-tag v-if="productConfig?.dataCollect?.enabled" size="small" type="info">
                产品配置: {{ productConfig.dataCollect.interval }}{{ productConfig.dataCollect.unit === 'MINUTE' ? '分钟' : '小时' }}
              </el-tag>
              <el-switch
                v-model="formData.dataCollect.enabled"
                active-text="启用"
                inactive-text="禁用"
              />
            </div>
          </div>
        </template>
        
        <el-form-item label="采集间隔">
          <el-input-number
            v-model="formData.dataCollect.interval"
            :min="1"
            :max="1440"
            :disabled="!formData.dataCollect.enabled"
            style="width: 150px"
          />
          <el-select
            v-model="formData.dataCollect.unit"
            :disabled="!formData.dataCollect.enabled"
            style="width: 100px; margin-left: 10px"
          >
            <el-option label="分钟" value="MINUTE" />
            <el-option label="小时" value="HOUR" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="任务优先级">
          <el-select
            v-model="formData.dataCollect.priority"
            :disabled="!formData.dataCollect.enabled"
            style="width: 100%"
          >
            <el-option label="关键任务(1)" :value="1" />
            <el-option label="高优先级(3)" :value="3" />
            <el-option label="普通优先级(5)" :value="5" />
            <el-option label="低优先级(7)" :value="7" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input
            v-model="formData.dataCollect.description"
            :disabled="!formData.dataCollect.enabled"
            placeholder="任务描述"
          />
        </el-form-item>
      </el-card>
    </el-form>
    
    <template #footer>
      <el-button type="primary" @click="submitForm" :loading="formLoading">保 存</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { Monitor, CircleCheck, DataAnalysis } from '@element-plus/icons-vue'
import * as DeviceJobConfigApi from '@/api/iot/device/jobConfig'
import * as ProductJobConfigApi from '@/api/iot/product/jobConfig'

defineOptions({ name: 'DeviceJobConfigDialog' })

const message = useMessage()

const dialogVisible = ref(false)
const formLoading = ref(false)
const formRef = ref()
const deviceId = ref<number>()
const deviceName = ref('')
const productId = ref<number>()
const productConfig = ref<any>(null)

const formData = ref({
  offlineCheck: {
    enabled: false,
    interval: 10,
    unit: 'MINUTE',
    priority: 3,
    description: '检查设备是否在线'
  },
  healthCheck: {
    enabled: false,
    interval: 30,
    unit: 'MINUTE',
    priority: 5,
    description: '检查设备健康状态'
  },
  dataCollect: {
    enabled: false,
    interval: 15,
    unit: 'MINUTE',
    priority: 5,
    description: '采集设备传感器数据'
  }
})

/** 打开弹窗 */
const open = async (id: number, name: string, prodId: number) => {
  dialogVisible.value = true
  deviceId.value = id
  deviceName.value = name
  productId.value = prodId
  resetForm()
  
  // 加载产品配置
  await loadProductConfig()
  
  // 加载设备配置
  await loadDeviceJobConfig()
}

defineExpose({ open })

/** 加载产品定时任务配置 */
const loadProductConfig = async () => {
  if (!productId.value) return
  
  try {
    const response = await ProductJobConfigApi.getProductJobConfig(productId.value)
    if (response && response !== '{}') {
      productConfig.value = typeof response === 'string' 
        ? JSON.parse(response) 
        : response
    }
  } catch (error) {
    console.error('加载产品定时任务配置失败:', error)
  }
}

/** 加载设备定时任务配置 */
const loadDeviceJobConfig = async () => {
  formLoading.value = true
  try {
    const response = await DeviceJobConfigApi.getDeviceJobConfig(deviceId.value!)
    const jobConfigStr = response
    
    // 解析 jobConfig JSON
    if (jobConfigStr && jobConfigStr !== '{}') {
      const config = typeof jobConfigStr === 'string' 
        ? JSON.parse(jobConfigStr) 
        : jobConfigStr
      
      // 设备离线检查
      if (config.offlineCheck) {
        formData.value.offlineCheck = {
          ...formData.value.offlineCheck,
          ...config.offlineCheck
        }
      }
      
      // 设备健康检查
      if (config.healthCheck) {
        formData.value.healthCheck = {
          ...formData.value.healthCheck,
          ...config.healthCheck
        }
      }
      
      // 设备数据采集
      if (config.dataCollect) {
        formData.value.dataCollect = {
          ...formData.value.dataCollect,
          ...config.dataCollect
        }
      }
    }
  } catch (error) {
    console.error('加载设备定时任务配置失败:', error)
  } finally {
    formLoading.value = false
  }
}

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  formLoading.value = true
  try {
    // 构造 jobConfig
    const jobConfig = {
      offlineCheck: formData.value.offlineCheck,
      healthCheck: formData.value.healthCheck,
      dataCollect: formData.value.dataCollect
    }
    
    // 调用后端 API 更新
    await DeviceJobConfigApi.saveDeviceJobConfig(deviceId.value!, JSON.stringify(jobConfig))
    
    message.success('设备定时任务配置保存成功')
    dialogVisible.value = false
    emit('success')
  } catch (error) {
    console.error('保存设备定时任务配置失败:', error)
    message.error('保存失败，请重试')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  productConfig.value = null
  formData.value = {
    offlineCheck: {
      enabled: false,
      interval: 10,
      unit: 'MINUTE',
      priority: 3,
      description: '检查设备是否在线'
    },
    healthCheck: {
      enabled: false,
      interval: 30,
      unit: 'MINUTE',
      priority: 5,
      description: '检查设备健康状态'
    },
    dataCollect: {
      enabled: false,
      interval: 15,
      unit: 'MINUTE',
      priority: 5,
      description: '采集设备传感器数据'
    }
  }
}
</script>

