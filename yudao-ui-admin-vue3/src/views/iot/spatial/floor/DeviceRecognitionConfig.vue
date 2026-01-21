<template>
  <div class="device-recognition-config">
    <el-alert type="info" :closable="false" style="margin-bottom: 20px;">
      <template #title>
        <span>配置要识别的设备类型和名称。CAD中的块名称、图层名称由设计师自定义，请根据实际情况配置。</span>
      </template>
    </el-alert>

    <!-- 快速模板 -->
    <el-card shadow="never" style="margin-bottom: 20px;">
      <template #header>
        <div class="card-header">
          <span>快速模板</span>
        </div>
      </template>
      
      <el-space wrap>
        <el-button size="small" @click="loadTemplate('security')">
          <Icon icon="ep:video-camera" />
          智慧安防模板
        </el-button>
        <el-button size="small" @click="loadTemplate('lighting')">
          <Icon icon="ep:sunny" />
          智慧照明模板
        </el-button>
        <el-button size="small" @click="loadTemplate('hvac')">
          <Icon icon="ep:wind-power" />
          暖通空调模板
        </el-button>
        <el-button size="small" @click="loadTemplate('fire')">
          <Icon icon="ep:warning" />
          消防系统模板
        </el-button>
        <el-button size="small" type="warning" @click="clearAll">
          <Icon icon="ep:delete" />
          清空配置
        </el-button>
      </el-space>
    </el-card>

    <!-- 配置表格 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>设备识别规则（{{ configList.length }} 条）</span>
          <el-button type="primary" size="small" @click="handleAdd">
            <Icon icon="ep:plus" />
            添加规则
          </el-button>
        </div>
      </template>

      <el-table :data="configList" border style="width: 100%">
        <el-table-column label="序号" type="index" width="60" align="center" />
        
        <el-table-column label="设备类型" width="140">
          <template #default="{ row }">
            <el-select v-model="row.deviceType" placeholder="选择类型" size="small">
              <el-option 
                v-for="type in deviceTypes" 
                :key="type.value" 
                :label="type.label" 
                :value="type.value"
              >
                <el-icon :style="{ color: type.color }">
                  <component :is="type.icon" />
                </el-icon>
                <span style="margin-left: 8px;">{{ type.label }}</span>
              </el-option>
            </el-select>
          </template>
        </el-table-column>

        <el-table-column label="匹配方式" width="120">
          <template #default="{ row }">
            <el-select v-model="row.matchType" size="small">
              <el-option label="块名称" value="block" />
              <el-option label="图层名称" value="layer" />
              <el-option label="两者之一" value="both" />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column label="名称匹配规则" min-width="250">
          <template #default="{ row }">
            <el-input 
              v-model="row.namePattern" 
              placeholder="支持多个，用逗号分隔，如: camera,摄像头,监控" 
              size="small"
            >
              <template #prepend>包含</template>
            </el-input>
          </template>
        </el-table-column>

        <el-table-column label="示例" min-width="180">
          <template #default="{ row }">
            <el-text type="info" size="small">
              {{ getPatternExample(row) }}
            </el-text>
          </template>
        </el-table-column>

        <el-table-column label="启用" width="80" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ $index }">
            <el-button 
              type="danger" 
              size="small" 
              link 
              @click="handleDelete($index)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 16px;">
        <el-text type="info" size="small">
          提示：匹配不区分大小写，支持模糊匹配。例如配置"camera"可匹配"Camera_01"、"球机camera"等。
        </el-text>
      </div>
    </el-card>

    <!-- 操作按钮 -->
    <div style="margin-top: 20px; text-align: right;">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSave">
        保存配置
      </el-button>
      <el-button type="success" @click="handleConfirm" :disabled="configList.length === 0">
        <Icon icon="ep:search" />
        开始识别
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  VideoCamera, 
  Sunny, 
  WindPower, 
  Warning,
  Monitor,
  Key,
  Switch,
  BellFilled
} from '@element-plus/icons-vue'

defineOptions({ name: 'DeviceRecognitionConfig' })

const emit = defineEmits(['confirm', 'cancel'])

// 设备类型定义
const deviceTypes = [
  { label: '摄像头', value: 'camera', icon: VideoCamera, color: '#409eff' },
  { label: '门禁', value: 'access_control', icon: Key, color: '#e6a23c' },
  { label: '传感器', value: 'sensor', icon: Monitor, color: '#67c23a' },
  { label: '灯光', value: 'light', icon: Sunny, color: '#f56c6c' },
  { label: '空调', value: 'air_conditioner', icon: WindPower, color: '#909399' },
  { label: '烟感', value: 'smoke_detector', icon: Warning, color: '#f56c6c' },
  { label: '开关', value: 'switch', icon: Switch, color: '#409eff' },
  { label: '报警器', value: 'alarm', icon: BellFilled, color: '#e6a23c' }
]

// 配置列表
const configList = ref<any[]>([])

// 预定义模板
const templates = {
  security: [
    { deviceType: 'camera', matchType: 'both', namePattern: 'camera,摄像头,监控,球机,枪机', enabled: true },
    { deviceType: 'access_control', matchType: 'both', namePattern: 'door,门禁,读卡器', enabled: true },
    { deviceType: 'alarm', matchType: 'both', namePattern: 'alarm,报警,警铃', enabled: true }
  ],
  lighting: [
    { deviceType: 'light', matchType: 'both', namePattern: 'light,灯,照明,筒灯,射灯', enabled: true },
    { deviceType: 'switch', matchType: 'both', namePattern: 'switch,开关,面板', enabled: true }
  ],
  hvac: [
    { deviceType: 'air_conditioner', matchType: 'both', namePattern: 'ac,空调,风机,hvac', enabled: true },
    { deviceType: 'sensor', matchType: 'both', namePattern: 'temp,温度,湿度,sensor', enabled: true }
  ],
  fire: [
    { deviceType: 'smoke_detector', matchType: 'both', namePattern: 'smoke,烟感,火灾', enabled: true },
    { deviceType: 'alarm', matchType: 'both', namePattern: 'alarm,警铃,声光', enabled: true }
  ]
}

/**
 * 加载模板
 */
const loadTemplate = (templateName: string) => {
  const template = templates[templateName]
  if (template) {
    // 追加到现有配置（避免覆盖）
    configList.value.push(...JSON.parse(JSON.stringify(template)))
    ElMessage.success(`已加载${getTemplateName(templateName)}模板`)
  }
}

/**
 * 获取模板名称
 */
const getTemplateName = (templateName: string) => {
  const nameMap = {
    security: '智慧安防',
    lighting: '智慧照明',
    hvac: '暖通空调',
    fire: '消防系统'
  }
  return nameMap[templateName] || templateName
}

/**
 * 清空所有配置
 */
const clearAll = () => {
  configList.value = []
  ElMessage.info('已清空配置')
}

/**
 * 添加新规则
 */
const handleAdd = () => {
  configList.value.push({
    deviceType: 'camera',
    matchType: 'both',
    namePattern: '',
    enabled: true
  })
}

/**
 * 删除规则
 */
const handleDelete = (index: number) => {
  configList.value.splice(index, 1)
}

/**
 * 获取匹配示例
 */
const getPatternExample = (row: any) => {
  if (!row.namePattern) return '请输入名称规则'
  
  const patterns = row.namePattern.split(',').map(p => p.trim()).filter(p => p)
  if (patterns.length === 0) return '请输入名称规则'
  
  const example = patterns[0]
  const matchTypeText = {
    block: '块名',
    layer: '图层',
    both: '块名或图层'
  }[row.matchType] || '名称'
  
  return `${matchTypeText}包含"${example}"`
}

/**
 * 取消
 */
const handleCancel = () => {
  emit('cancel')
}

/**
 * 保存配置（存储到localStorage）
 */
const handleSave = () => {
  try {
    localStorage.setItem('dxf_device_recognition_config', JSON.stringify(configList.value))
    ElMessage.success('配置已保存到本地')
  } catch (error) {
    ElMessage.error('保存配置失败')
  }
}

/**
 * 确认并开始识别
 */
const handleConfirm = () => {
  const enabledConfigs = configList.value.filter(c => c.enabled && c.namePattern)
  
  if (enabledConfigs.length === 0) {
    ElMessage.warning('请至少配置一条启用的识别规则')
    return
  }

  emit('confirm', enabledConfigs)
}

/**
 * 初始化：加载保存的配置
 */
const init = () => {
  try {
    const savedConfig = localStorage.getItem('dxf_device_recognition_config')
    if (savedConfig) {
      configList.value = JSON.parse(savedConfig)
    } else {
      // 默认加载安防模板
      loadTemplate('security')
    }
  } catch (error) {
    console.error('加载配置失败:', error)
    loadTemplate('security')
  }
}

// 组件挂载时初始化
init()

defineExpose({ init })
</script>

<style scoped lang="scss">
.device-recognition-config {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>



















































