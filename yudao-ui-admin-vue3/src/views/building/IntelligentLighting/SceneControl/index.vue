<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧建筑</el-breadcrumb-item>
        <el-breadcrumb-item>智能照明</el-breadcrumb-item>
        <el-breadcrumb-item>场景控制</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 场景控制面板 -->
    <div class="scene-control-panel">
      <el-row :gutter="20">
        <!-- 场景列表 -->
        <el-col :span="8">
          <el-card class="scene-list-card">
            <template #header>
              <div class="card-header">
                <span>场景列表</span>
                <el-button size="small" type="primary" @click="handleAddScene">
                  <el-icon><Plus /></el-icon>
                  新增场景
                </el-button>
              </div>
            </template>
            
            <div class="scene-list">
              <div
                v-for="scene in sceneList"
                :key="scene.id"
                :class="['scene-item', { 'active': scene.id === activeSceneId }]"
                @click="handleSelectScene(scene)"
              >
                <div class="scene-icon">
                  <el-icon><component :is="getSceneIcon(scene.type)" /></el-icon>
                </div>
                <div class="scene-info">
                  <div class="scene-name">{{ scene.name }}</div>
                  <div class="scene-desc">{{ scene.description }}</div>
                </div>
                <div class="scene-actions">
                  <el-button 
                    :type="scene.status === 'active' ? 'success' : 'info'" 
                    size="small" 
                    @click.stop="handleExecuteScene(scene)"
                  >
                    {{ scene.status === 'active' ? '已启用' : '启用' }}
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 场景详情 -->
        <el-col :span="16">
          <el-card class="scene-detail-card">
            <template #header>
              <div class="card-header">
                <span>{{ currentScene?.name || '请选择场景' }}</span>
                <div class="header-actions" v-if="currentScene">
                  <el-button size="small" @click="handleEditScene">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-button>
                  <el-button size="small" type="success" @click="handleExecuteScene(currentScene)">
                    <el-icon><VideoPlay /></el-icon>
                    执行场景
                  </el-button>
                </div>
              </div>
            </template>

            <div v-if="currentScene" class="scene-detail">
              <!-- 场景信息 -->
              <div class="scene-info-section">
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="场景名称">{{ currentScene.name }}</el-descriptions-item>
                  <el-descriptions-item label="场景类型">{{ getSceneTypeText(currentScene.type) }}</el-descriptions-item>
                  <el-descriptions-item label="触发方式">{{ getTriggerTypeText(currentScene.triggerType) }}</el-descriptions-item>
                  <el-descriptions-item label="状态">
                    <el-tag :type="currentScene.status === 'active' ? 'success' : 'info'">
                      {{ currentScene.status === 'active' ? '已启用' : '未启用' }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="场景描述" :span="2">{{ currentScene.description }}</el-descriptions-item>
                </el-descriptions>
              </div>

              <!-- 设备控制列表 -->
              <div class="device-control-section">
                <h4>设备控制配置</h4>
                <el-table :data="currentScene.deviceControls" stripe border style="width: 100%">
                  <el-table-column prop="deviceName" label="设备名称" width="150" />
                  <el-table-column prop="deviceType" label="设备类型" width="120">
                    <template #default="{ row }">
                      <el-tag size="small">{{ getDeviceTypeText(row.deviceType) }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="location" label="位置" width="120" />
                  <el-table-column prop="controlType" label="控制类型" width="120" />
                  <el-table-column prop="targetValue" label="目标值" width="100">
                    <template #default="{ row }">
                      <span v-if="row.controlType === 'brightness'">{{ row.targetValue }}%</span>
                      <span v-else-if="row.controlType === 'switch'">{{ row.targetValue ? '开' : '关' }}</span>
                      <span v-else>{{ row.targetValue }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="delay" label="延时(秒)" width="100" />
                  <el-table-column label="当前状态" width="100">
                    <template #default="{ row }">
                      <el-tag :type="row.currentStatus === 'online' ? 'success' : 'danger'" size="small">
                        {{ row.currentStatus === 'online' ? '在线' : '离线' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="120">
                    <template #default="{ row }">
                      <el-button link type="primary" size="small" @click="handleTestDevice(row)">
                        <el-icon><Operation /></el-icon>
                        测试
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>

              <!-- 触发条件 -->
              <div v-if="currentScene.triggerType === 'auto'" class="trigger-condition-section">
                <h4>触发条件</h4>
                <el-descriptions :column="1" border>
                  <el-descriptions-item v-if="currentScene.timeCondition" label="时间条件">
                    {{ currentScene.timeCondition.startTime }} - {{ currentScene.timeCondition.endTime }}
                    ({{ currentScene.timeCondition.weekdays.join(', ') }})
                  </el-descriptions-item>
                  <el-descriptions-item v-if="currentScene.sensorCondition" label="传感器条件">
                    {{ currentScene.sensorCondition.sensorName }}: 
                    {{ currentScene.sensorCondition.operator }} 
                    {{ currentScene.sensorCondition.threshold }}{{ currentScene.sensorCondition.unit }}
                  </el-descriptions-item>
                  <el-descriptions-item v-if="currentScene.weatherCondition" label="天气条件">
                    {{ currentScene.weatherCondition.condition }}
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </div>

            <div v-else class="no-scene-selected">
              <el-empty description="请从左侧选择一个场景" />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 场景编辑对话框 -->
    <el-dialog v-model="editDialogVisible" :title="isEditMode ? '编辑场景' : '新增场景'" width="70%" draggable>
      <el-form ref="sceneFormRef" :model="sceneForm" :rules="sceneRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="场景名称" prop="name">
              <el-input v-model="sceneForm.name" placeholder="请输入场景名称" />
            </el-form-item>
            <el-form-item label="场景类型" prop="type">
              <el-select v-model="sceneForm.type" placeholder="请选择场景类型" style="width: 100%;">
                <el-option label="会议场景" value="meeting" />
                <el-option label="办公场景" value="office" />
                <el-option label="清洁场景" value="cleaning" />
                <el-option label="节能场景" value="energy_saving" />
                <el-option label="安防场景" value="security" />
              </el-select>
            </el-form-item>
            <el-form-item label="触发方式" prop="triggerType">
              <el-radio-group v-model="sceneForm.triggerType">
                <el-radio label="manual">手动触发</el-radio>
                <el-radio label="auto">自动触发</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="场景描述">
              <el-input
                v-model="sceneForm.description"
                type="textarea"
                :rows="3"
                placeholder="请输入场景描述"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 设备控制配置 -->
        <el-form-item label="设备控制">
          <el-table :data="sceneForm.deviceControls" border style="width: 100%">
            <el-table-column prop="deviceName" label="设备名称" width="150">
              <template #default="{ row }">
                <el-select v-model="row.deviceName" placeholder="选择设备" size="small">
                  <el-option v-for="device in availableDevices" :key="device.id" :label="device.name" :value="device.name" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="controlType" label="控制类型" width="120">
              <template #default="{ row }">
                <el-select v-model="row.controlType" placeholder="控制类型" size="small">
                  <el-option label="开关" value="switch" />
                  <el-option label="亮度" value="brightness" />
                  <el-option label="色温" value="color_temp" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="targetValue" label="目标值" width="120">
              <template #default="{ row }">
                <el-input-number v-model="row.targetValue" size="small" :min="0" :max="100" />
              </template>
            </el-table-column>
            <el-table-column prop="delay" label="延时(秒)" width="100">
              <template #default="{ row }">
                <el-input-number v-model="row.delay" size="small" :min="0" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ $index }">
                <el-button link type="danger" size="small" @click="removeDeviceControl($index)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button type="primary" size="small" @click="addDeviceControl" style="margin-top: 10px;">
            <el-icon><Plus /></el-icon>
            添加设备
          </el-button>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveScene">确定</el-button>
        </span>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Edit, VideoPlay, Operation, Delete, Sunny, Moon, 
  OfficeBuilding, Service, Lock
} from '@element-plus/icons-vue'

// 响应式数据
const activeSceneId = ref(1)
const editDialogVisible = ref(false)
const isEditMode = ref(false)

// 当前选中的场景
const currentScene = computed(() => {
  return sceneList.value.find(scene => scene.id === activeSceneId.value)
})

// 场景表单
const sceneForm = reactive({
  name: '',
  type: '',
  triggerType: 'manual',
  description: '',
  deviceControls: []
})

// 表单验证规则
const sceneRules = {
  name: [{ required: true, message: '请输入场景名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择场景类型', trigger: 'change' }],
  triggerType: [{ required: true, message: '请选择触发方式', trigger: 'change' }]
}

// 场景列表数据
const sceneList = ref([
  {
    id: 1,
    name: '会议模式',
    type: 'meeting',
    triggerType: 'manual',
    description: '适用于会议室，提供明亮均匀的照明',
    status: 'inactive',
    deviceControls: [
      {
        deviceName: '会议室主灯',
        deviceType: 'led_panel',
        location: '会议室A',
        controlType: 'brightness',
        targetValue: 80,
        delay: 0,
        currentStatus: 'online'
      },
      {
        deviceName: '会议室辅助灯',
        deviceType: 'spot_light',
        location: '会议室A',
        controlType: 'brightness',
        targetValue: 60,
        delay: 2,
        currentStatus: 'online'
      }
    ]
  },
  {
    id: 2,
    name: '办公模式',
    type: 'office',
    triggerType: 'auto',
    description: '日常办公照明，舒适节能',
    status: 'active',
    timeCondition: {
      startTime: '08:00',
      endTime: '18:00',
      weekdays: ['周一', '周二', '周三', '周四', '周五']
    },
    deviceControls: [
      {
        deviceName: '办公区主灯',
        deviceType: 'led_panel',
        location: '办公区',
        controlType: 'brightness',
        targetValue: 70,
        delay: 0,
        currentStatus: 'online'
      }
    ]
  },
  {
    id: 3,
    name: '节能模式',
    type: 'energy_saving',
    triggerType: 'auto',
    description: '夜间和非工作时间的节能照明',
    status: 'inactive',
    timeCondition: {
      startTime: '18:00',
      endTime: '08:00',
      weekdays: ['每天']
    },
    deviceControls: [
      {
        deviceName: '走廊灯',
        deviceType: 'led_strip',
        location: '走廊',
        controlType: 'brightness',
        targetValue: 30,
        delay: 0,
        currentStatus: 'online'
      }
    ]
  }
])

// 可用设备列表
const availableDevices = ref([
  { id: 1, name: '会议室主灯', type: 'led_panel' },
  { id: 2, name: '办公区主灯', type: 'led_panel' },
  { id: 3, name: '走廊灯', type: 'led_strip' },
  { id: 4, name: '应急灯', type: 'emergency_light' }
])

// 计算属性和方法
const getSceneIcon = (type: string) => {
  const icons = {
    meeting: OfficeBuilding,
    office: Service,
    cleaning: Service,
    energy_saving: Moon,
    security: Lock
  }
  return icons[type] || Sunny
}

const getSceneTypeText = (type: string) => {
  const texts = {
    meeting: '会议场景',
    office: '办公场景',
    cleaning: '清洁场景',
    energy_saving: '节能场景',
    security: '安防场景'
  }
  return texts[type] || type
}

const getTriggerTypeText = (type: string) => {
  const texts = {
    manual: '手动触发',
    auto: '自动触发'
  }
  return texts[type] || type
}

const getDeviceTypeText = (type: string) => {
  const texts = {
    led_panel: 'LED面板灯',
    spot_light: '射灯',
    led_strip: 'LED灯带',
    emergency_light: '应急灯'
  }
  return texts[type] || type
}

// 事件处理
const handleSelectScene = (scene: any) => {
  activeSceneId.value = scene.id
}

const handleAddScene = () => {
  isEditMode.value = false
  Object.assign(sceneForm, {
    name: '',
    type: '',
    triggerType: 'manual',
    description: '',
    deviceControls: []
  })
  editDialogVisible.value = true
}

const handleEditScene = () => {
  if (!currentScene.value) return
  
  isEditMode.value = true
  Object.assign(sceneForm, {
    name: currentScene.value.name,
    type: currentScene.value.type,
    triggerType: currentScene.value.triggerType,
    description: currentScene.value.description,
    deviceControls: [...currentScene.value.deviceControls]
  })
  editDialogVisible.value = true
}

const handleExecuteScene = async (scene: any) => {
  try {
    await ElMessageBox.confirm(`确认执行场景 "${scene.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    // 更新场景状态
    sceneList.value.forEach(s => {
      s.status = s.id === scene.id ? 'active' : 'inactive'
    })
    
    ElMessage.success(`场景 "${scene.name}" 执行成功`)
  } catch {
    // 用户取消
  }
}

const handleTestDevice = (device: any) => {
  ElMessage.success(`测试设备 "${device.deviceName}" 成功`)
}

const addDeviceControl = () => {
  sceneForm.deviceControls.push({
    deviceName: '',
    deviceType: '',
    location: '',
    controlType: 'brightness',
    targetValue: 50,
    delay: 0,
    currentStatus: 'online'
  })
}

const removeDeviceControl = (index: number) => {
  sceneForm.deviceControls.splice(index, 1)
}

const handleSaveScene = () => {
  if (!sceneForm.name || !sceneForm.type) {
    ElMessage.warning('请完善场景信息')
    return
  }

  if (sceneForm.deviceControls.length === 0) {
    ElMessage.warning('请至少添加一个设备控制')
    return
  }

  ElMessage.success(isEditMode.value ? '场景修改成功' : '场景创建成功')
  editDialogVisible.value = false
}

onMounted(() => {
  // 初始化
})
</script>

<style scoped lang="scss">@use '@/styles/dark-theme.scss';

.app-container {
  padding: 20px;

  .breadcrumb-container {
    margin-bottom: 20px;
  }

  .scene-control-panel {
    .scene-list-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .scene-list {
        .scene-item {
          display: flex;
          align-items: center;
          padding: 16px;
          margin-bottom: 12px;
          border: 1px solid #e0e0e0;
          border-radius: 8px;
          cursor: pointer;
          transition: all 0.3s;

          &:hover {
            border-color: #409eff;
            box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
          }

          &.active {
            border-color: #409eff;
            background: #f0f8ff;
          }

          .scene-icon {
            width: 50px;
            height: 50px;
            border-radius: 50%;
            background: #409eff;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 16px;

            .el-icon {
              font-size: 24px;
              color: white;
            }
          }

          .scene-info {
            flex: 1;

            .scene-name {
              font-size: 16px;
              font-weight: 600;
              color: #303133;
              margin-bottom: 4px;
            }

            .scene-desc {
              font-size: 14px;
              color: #606266;
            }
          }

          .scene-actions {
            margin-left: 12px;
          }
        }
      }
    }

    .scene-detail-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .header-actions {
          display: flex;
          gap: 8px;
        }
      }

      .scene-detail {
        .scene-info-section {
          margin-bottom: 30px;
        }

        .device-control-section {
          margin-bottom: 30px;

          h4 {
            margin: 0 0 16px 0;
            color: #303133;
            font-size: 16px;
            border-bottom: 1px solid #e0e0e0;
            padding-bottom: 8px;
          }
        }

        .trigger-condition-section {
          h4 {
            margin: 0 0 16px 0;
            color: #303133;
            font-size: 16px;
            border-bottom: 1px solid #e0e0e0;
            padding-bottom: 8px;
          }
        }
      }

      .no-scene-selected {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 400px;
      }
    }
  }
}
</style>






