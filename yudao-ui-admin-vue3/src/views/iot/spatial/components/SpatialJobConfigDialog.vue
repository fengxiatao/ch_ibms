<template>
  <Dialog
    :title="`${entityName} - 定时任务配置`"
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
          <div>{{ getEntityTypeDescription() }}</div>
          <div style="margin-top: 5px; font-size: 12px; color: #909399">
            根据不同的空间设施配置不同类型的定时任务
          </div>
        </template>
      </el-alert>

      <!-- 根据实体类型动态显示不同的任务配置 -->
      
      <!-- 园区：设备统计任务 -->
      <el-card v-if="entityType === 'campus'" shadow="never" style="margin-bottom: 20px">
        <template #header>
          <div style="display: flex; align-items: center; justify-content: space-between">
            <span>
              <el-icon style="margin-right: 5px"><DataAnalysis /></el-icon>
              设备统计
            </span>
            <el-switch
              v-model="formData.deviceStatistics.enabled"
              active-text="启用"
              inactive-text="禁用"
            />
          </div>
        </template>
        
        <el-form-item label="统计间隔">
          <el-input-number
            v-model="formData.deviceStatistics.interval"
            :min="1"
            :max="1440"
            :disabled="!formData.deviceStatistics.enabled"
            style="width: 150px"
          />
          <el-select
            v-model="formData.deviceStatistics.unit"
            :disabled="!formData.deviceStatistics.enabled"
            style="width: 100px; margin-left: 10px"
          >
            <el-option label="分钟" value="MINUTE" />
            <el-option label="小时" value="HOUR" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="任务优先级">
          <el-select
            v-model="formData.deviceStatistics.priority"
            :disabled="!formData.deviceStatistics.enabled"
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
            v-model="formData.deviceStatistics.description"
            :disabled="!formData.deviceStatistics.enabled"
            placeholder="任务描述"
          />
        </el-form-item>
      </el-card>

      <!-- 建筑：能耗统计任务 -->
      <el-card v-if="entityType === 'building'" shadow="never" style="margin-bottom: 20px">
        <template #header>
          <div style="display: flex; align-items: center; justify-content: space-between">
            <span>
              <el-icon style="margin-right: 5px"><TrendCharts /></el-icon>
              能耗统计
            </span>
            <el-switch
              v-model="formData.energyStatistics.enabled"
              active-text="启用"
              inactive-text="禁用"
            />
          </div>
        </template>
        
        <el-form-item label="统计间隔">
          <el-input-number
            v-model="formData.energyStatistics.interval"
            :min="1"
            :max="1440"
            :disabled="!formData.energyStatistics.enabled"
            style="width: 150px"
          />
          <el-select
            v-model="formData.energyStatistics.unit"
            :disabled="!formData.energyStatistics.enabled"
            style="width: 100px; margin-left: 10px"
          >
            <el-option label="分钟" value="MINUTE" />
            <el-option label="小时" value="HOUR" />
            <el-option label="天" value="DAY" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="任务优先级">
          <el-select
            v-model="formData.energyStatistics.priority"
            :disabled="!formData.energyStatistics.enabled"
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
            v-model="formData.energyStatistics.description"
            :disabled="!formData.energyStatistics.enabled"
            placeholder="任务描述"
          />
        </el-form-item>
      </el-card>

      <!-- 楼层：环境监测任务 -->
      <el-card v-if="entityType === 'floor'" shadow="never" style="margin-bottom: 20px">
        <template #header>
          <div style="display: flex; align-items: center; justify-content: space-between">
            <span>
              <el-icon style="margin-right: 5px"><Odometer /></el-icon>
              环境监测
            </span>
            <el-switch
              v-model="formData.envMonitor.enabled"
              active-text="启用"
              inactive-text="禁用"
            />
          </div>
        </template>
        
        <el-form-item label="监测间隔">
          <el-input-number
            v-model="formData.envMonitor.interval"
            :min="1"
            :max="1440"
            :disabled="!formData.envMonitor.enabled"
            style="width: 150px"
          />
          <el-select
            v-model="formData.envMonitor.unit"
            :disabled="!formData.envMonitor.enabled"
            style="width: 100px; margin-left: 10px"
          >
            <el-option label="分钟" value="MINUTE" />
            <el-option label="小时" value="HOUR" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="任务优先级">
          <el-select
            v-model="formData.envMonitor.priority"
            :disabled="!formData.envMonitor.enabled"
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
            v-model="formData.envMonitor.description"
            :disabled="!formData.envMonitor.enabled"
            placeholder="任务描述"
          />
        </el-form-item>
      </el-card>

      <!-- 区域：设备巡检任务 -->
      <el-card v-if="entityType === 'area'" shadow="never" style="margin-bottom: 20px">
        <template #header>
          <div style="display: flex; align-items: center; justify-content: space-between">
            <span>
              <el-icon style="margin-right: 5px"><Operation /></el-icon>
              设备巡检
            </span>
            <el-switch
              v-model="formData.deviceInspection.enabled"
              active-text="启用"
              inactive-text="禁用"
            />
          </div>
        </template>
        
        <el-form-item label="巡检间隔">
          <el-input-number
            v-model="formData.deviceInspection.interval"
            :min="1"
            :max="1440"
            :disabled="!formData.deviceInspection.enabled"
            style="width: 150px"
          />
          <el-select
            v-model="formData.deviceInspection.unit"
            :disabled="!formData.deviceInspection.enabled"
            style="width: 100px; margin-left: 10px"
          >
            <el-option label="分钟" value="MINUTE" />
            <el-option label="小时" value="HOUR" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="任务优先级">
          <el-select
            v-model="formData.deviceInspection.priority"
            :disabled="!formData.deviceInspection.enabled"
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
            v-model="formData.deviceInspection.description"
            :disabled="!formData.deviceInspection.enabled"
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
import { DataAnalysis, TrendCharts, Odometer, Operation } from '@element-plus/icons-vue'
import * as SpatialJobConfigApi from '@/api/iot/spatial/jobConfig'

defineOptions({ name: 'SpatialJobConfigDialog' })

const message = useMessage()

const dialogVisible = ref(false)
const formLoading = ref(false)
const formRef = ref()
const entityType = ref<'campus' | 'building' | 'floor' | 'area'>('campus')
const entityId = ref<number>()
const entityName = ref('')

const formData = ref({
  deviceStatistics: {
    enabled: false,
    interval: 60,
    unit: 'MINUTE',
    priority: 7,
    description: '统计园区内设备数量和状态'
  },
  energyStatistics: {
    enabled: false,
    interval: 1,
    unit: 'DAY',
    priority: 7,
    description: '统计建筑能耗数据'
  },
  envMonitor: {
    enabled: false,
    interval: 30,
    unit: 'MINUTE',
    priority: 5,
    description: '监测楼层环境参数'
  },
  deviceInspection: {
    enabled: false,
    interval: 1,
    unit: 'HOUR',
    priority: 5,
    description: '巡检区域内设备状态'
  }
})

/** 打开弹窗 */
const open = async (type: 'campus' | 'building' | 'floor' | 'area', id: number, name: string) => {
  dialogVisible.value = true
  entityType.value = type
  entityId.value = id
  entityName.value = name
  resetForm()
  
  // 加载现有配置
  await loadJobConfig()
}

defineExpose({ open })

/** 获取实体类型描述 */
const getEntityTypeDescription = () => {
  const descriptions = {
    campus: '配置园区级定时任务，例如：设备统计、设备在线状态汇总等',
    building: '配置建筑级定时任务，例如：能耗统计、空调调度、照明控制等',
    floor: '配置楼层级定时任务，例如：环境监测、温湿度采集、空气质量检测等',
    area: '配置区域级定时任务，例如：设备巡检、占用率分析、清洁计划等'
  }
  return descriptions[entityType.value]
}

/** 加载定时任务配置 */
const loadJobConfig = async () => {
  formLoading.value = true
  try {
    let response
    switch (entityType.value) {
      case 'campus':
        response = await SpatialJobConfigApi.getCampusJobConfig(entityId.value!)
        break
      case 'building':
        response = await SpatialJobConfigApi.getBuildingJobConfig(entityId.value!)
        break
      case 'floor':
        response = await SpatialJobConfigApi.getFloorJobConfig(entityId.value!)
        break
      case 'area':
        response = await SpatialJobConfigApi.getAreaJobConfig(entityId.value!)
        break
    }
    
    const jobConfigStr = response
    
    // 解析 jobConfig JSON
    if (jobConfigStr && jobConfigStr !== '{}') {
      const config = typeof jobConfigStr === 'string' 
        ? JSON.parse(jobConfigStr) 
        : jobConfigStr
      
      // 根据实体类型加载对应的配置
      if (entityType.value === 'campus' && config.deviceStatistics) {
        formData.value.deviceStatistics = {
          ...formData.value.deviceStatistics,
          ...config.deviceStatistics
        }
      } else if (entityType.value === 'building' && config.energyStatistics) {
        formData.value.energyStatistics = {
          ...formData.value.energyStatistics,
          ...config.energyStatistics
        }
      } else if (entityType.value === 'floor' && config.envMonitor) {
        formData.value.envMonitor = {
          ...formData.value.envMonitor,
          ...config.envMonitor
        }
      } else if (entityType.value === 'area' && config.deviceInspection) {
        formData.value.deviceInspection = {
          ...formData.value.deviceInspection,
          ...config.deviceInspection
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
    // 根据实体类型构造 jobConfig
    let jobConfig: any = {}
    
    switch (entityType.value) {
      case 'campus':
        jobConfig.deviceStatistics = formData.value.deviceStatistics
        break
      case 'building':
        jobConfig.energyStatistics = formData.value.energyStatistics
        break
      case 'floor':
        jobConfig.envMonitor = formData.value.envMonitor
        break
      case 'area':
        jobConfig.deviceInspection = formData.value.deviceInspection
        break
    }
    
    // 调用后端 API 更新
    switch (entityType.value) {
      case 'campus':
        await SpatialJobConfigApi.saveCampusJobConfig(entityId.value!, JSON.stringify(jobConfig))
        break
      case 'building':
        await SpatialJobConfigApi.saveBuildingJobConfig(entityId.value!, JSON.stringify(jobConfig))
        break
      case 'floor':
        await SpatialJobConfigApi.saveFloorJobConfig(entityId.value!, JSON.stringify(jobConfig))
        break
      case 'area':
        await SpatialJobConfigApi.saveAreaJobConfig(entityId.value!, JSON.stringify(jobConfig))
        break
    }
    
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
    deviceStatistics: {
      enabled: false,
      interval: 60,
      unit: 'MINUTE',
      priority: 7,
      description: '统计园区内设备数量和状态'
    },
    energyStatistics: {
      enabled: false,
      interval: 1,
      unit: 'DAY',
      priority: 7,
      description: '统计建筑能耗数据'
    },
    envMonitor: {
      enabled: false,
      interval: 30,
      unit: 'MINUTE',
      priority: 5,
      description: '监测楼层环境参数'
    },
    deviceInspection: {
      enabled: false,
      interval: 1,
      unit: 'HOUR',
      priority: 5,
      description: '巡检区域内设备状态'
    }
  }
}
</script>






