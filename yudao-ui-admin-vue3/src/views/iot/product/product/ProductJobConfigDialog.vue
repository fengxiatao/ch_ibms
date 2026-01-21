<template>
  <Dialog
    :title="`${productName} - 定时任务配置`"
    v-model="dialogVisible"
    width="700px"
  >
    <el-form
      ref="formRef"
      :model="formData"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-alert
        title="说明"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      >
        <template #default>
          <div>配置此产品下所有设备的定时任务策略</div>
          <div style="margin-top: 5px; font-size: 12px; color: #909399">
            示例：海康摄像头每10分钟检查一次在线状态，大华摄像头每8分钟检查一次
          </div>
        </template>
      </el-alert>

      <!-- 设备离线检查 -->
      <el-card shadow="never" style="margin-bottom: 20px">
        <template #header>
          <div style="display: flex; align-items: center; justify-content: space-between">
            <span>
              <el-icon style="margin-right: 5px"><Monitor /></el-icon>
              设备离线检查
            </span>
            <el-switch
              v-model="formData.offlineCheck.enabled"
              active-text="启用"
              inactive-text="禁用"
            />
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
            <el-switch
              v-model="formData.healthCheck.enabled"
              active-text="启用"
              inactive-text="禁用"
            />
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
            <el-switch
              v-model="formData.dataCollect.enabled"
              active-text="启用"
              inactive-text="禁用"
            />
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
import * as ProductJobConfigApi from '@/api/iot/product/jobConfig'

defineOptions({ name: 'ProductJobConfigDialog' })

const message = useMessage()

const dialogVisible = ref(false)
const formLoading = ref(false)
const formRef = ref()
const productId = ref<number>()
const productName = ref('')

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
const open = async (id: number, name: string) => {
  dialogVisible.value = true
  productId.value = id
  productName.value = name
  resetForm()
  
  // 加载现有配置
  await loadJobConfig()
}

defineExpose({ open })

/** 加载定时任务配置 */
const loadJobConfig = async () => {
  formLoading.value = true
  try {
    const response = await ProductJobConfigApi.getProductJobConfig(productId.value!)
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
    console.error('加载定时任务配置失败:', error)
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
    await ProductJobConfigApi.saveProductJobConfig(productId.value!, JSON.stringify(jobConfig))
    
    message.success('定时任务配置保存成功')
    dialogVisible.value = false
    emit('success')
  } catch (error) {
    console.error('保存定时任务配置失败:', error)
    message.error('保存失败，请重试')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
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






