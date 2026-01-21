<template>
  <div class="scene-configurator">
    <el-button type="primary" @click="addScene" style="margin-bottom: 16px;">
      <Icon icon="ep:plus" /> 添加场景
    </el-button>
    
    <el-empty v-if="scenes.length === 0" description="暂无场景，请添加巡更场景" />
    
    <div v-for="(scene, index) in scenes" :key="scene.id || index" class="scene-card">
      <el-card shadow="hover">
        <template #header>
          <div class="scene-header">
            <span class="scene-title">
              <Icon icon="ep:video-camera" />
              场景 {{ index + 1 }}: {{ scene.sceneName || '未命名' }}
            </span>
            <div class="scene-actions">
              <el-button 
                size="small" 
                @click="moveScene(index, 'up')" 
                :disabled="index === 0"
                title="上移"
              >
                <Icon icon="ep:top" />
              </el-button>
              <el-button 
                size="small" 
                @click="moveScene(index, 'down')" 
                :disabled="index === scenes.length - 1"
                title="下移"
              >
                <Icon icon="ep:bottom" />
              </el-button>
              <el-button 
                size="small" 
                type="danger" 
                @click="removeScene(index)"
                title="删除"
              >
                <Icon icon="ep:delete" />
              </el-button>
            </div>
          </div>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="场景名称">
              <el-input 
                v-model="scene.sceneName" 
                placeholder="如：主入口监控"
                @input="emitUpdate"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="播放时长(秒)">
              <el-input-number 
                v-model="scene.duration" 
                :min="5" 
                :max="600" 
                :step="5"
                style="width: 100%;"
                @change="emitUpdate"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="分屏布局">
              <el-select 
                v-model="scene.gridLayout" 
                @change="onGridLayoutChange(scene)"
                style="width: 100%;"
              >
                <el-option label="1分屏 (1x1)" value="1x1">
                  <span>1分屏 (1x1)</span>
                </el-option>
                <el-option label="4分屏 (2x2)" value="2x2">
                  <span>4分屏 (2x2)</span>
                </el-option>
                <el-option label="9分屏 (3x3)" value="3x3">
                  <span>9分屏 (3x3)</span>
                </el-option>
                <el-option label="16分屏 (4x4)" value="4x4">
                  <span>16分屏 (4x4)</span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 通道配置网格 -->
        <el-divider content-position="left">
          <Icon icon="ep:grid" /> 通道配置 ({{ getConfiguredCount(scene) }}/{{ scene.gridCount }})
        </el-divider>
        
        <div class="channel-grid" :class="`grid-${scene.gridLayout}`">
          <div 
            v-for="position in scene.gridCount" 
            :key="position"
            class="grid-cell"
            @click="openChannelSelector(index, position)"
          >
            <div v-if="getChannelByPosition(scene, position)" class="channel-info">
              <div class="camera-icon">
                <Icon icon="ep:video-camera-filled" />
              </div>
              <div class="camera-name">
                {{ getChannelByPosition(scene, position).cameraName }}
              </div>
              <div class="camera-code">
                {{ getChannelByPosition(scene, position).cameraCode }}
              </div>
              <el-button 
                size="small" 
                type="danger" 
                circle
                class="remove-btn"
                @click.stop="removeChannel(scene, position)"
              >
                <Icon icon="ep:close" />
              </el-button>
            </div>
            <div v-else class="empty-cell">
              <Icon icon="ep:plus" class="add-icon" />
              <span>点击添加摄像头</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 摄像头选择对话框 -->
    <el-dialog 
      v-model="channelSelectorVisible" 
      title="选择摄像头" 
      width="900px"
      append-to-body
    >
      <CameraSelector 
        @select="onCameraSelect"
        @cancel="channelSelectorVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CameraSelector from './CameraSelector.vue'

interface ChannelConfig {
  gridPosition: number
  cameraId?: number | null
  cameraName?: string
  cameraCode?: string
  streamUrl?: string
  streamType?: number
  isEmpty?: boolean
  // ✅ 新增播放视频所需的字段
  nvrId?: number
  channelNo?: number
  ip?: string
  ptzSupport?: boolean
}

interface PatrolScene {
  id?: number
  sceneName: string
  sceneOrder: number
  duration: number
  gridLayout: '1x1' | '2x2' | '3x3' | '4x4'
  gridCount: number
  channels: ChannelConfig[]
}

const props = withDefaults(defineProps<{
  modelValue?: PatrolScene[]
}>(), {
  modelValue: () => []
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: PatrolScene[]): void
}>()

const GRID_LAYOUTS = {
  '1x1': { rows: 1, cols: 1, count: 1 },
  '2x2': { rows: 2, cols: 2, count: 4 },
  '3x3': { rows: 3, cols: 3, count: 9 },
  '4x4': { rows: 4, cols: 4, count: 16 }
}

const scenes = computed({
  get: () => props.modelValue || [],
  set: (value) => emit('update:modelValue', value)
})

const channelSelectorVisible = ref(false)
const currentSceneIndex = ref(0)
const currentGridPosition = ref(0)

// 添加场景
const addScene = () => {
  const newScene: PatrolScene = {
    sceneName: `场景${scenes.value.length + 1}`,
    sceneOrder: scenes.value.length + 1,
    duration: 30,
    gridLayout: '2x2',
    gridCount: 4,
    channels: []
  }
  scenes.value = [...scenes.value, newScene]
}

// 删除场景
const removeScene = (index: number) => {
  ElMessageBox.confirm('确定要删除这个场景吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const newScenes = [...scenes.value]
    newScenes.splice(index, 1)
    // 重新排序
    newScenes.forEach((scene, idx) => {
      scene.sceneOrder = idx + 1
    })
    scenes.value = newScenes
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// 移动场景
const moveScene = (index: number, direction: 'up' | 'down') => {
  const newScenes = [...scenes.value]
  const targetIndex = direction === 'up' ? index - 1 : index + 1
  
  if (targetIndex >= 0 && targetIndex < newScenes.length) {
    const temp = newScenes[index]
    newScenes[index] = newScenes[targetIndex]
    newScenes[targetIndex] = temp
    
    // 更新顺序
    newScenes.forEach((scene, idx) => {
      scene.sceneOrder = idx + 1
    })
    
    scenes.value = newScenes
  }
}

// 分屏布局改变
const onGridLayoutChange = (scene: PatrolScene) => {
  const layout = GRID_LAYOUTS[scene.gridLayout]
  scene.gridCount = layout.count
  // 清理超出的通道配置
  scene.channels = scene.channels.filter(ch => ch.gridPosition <= layout.count)
  emitUpdate()
}

// 获取指定位置的通道
const getChannelByPosition = (scene: PatrolScene, position: number) => {
  return scene.channels.find(ch => ch.gridPosition === position)
}

// 获取已配置通道数
const getConfiguredCount = (scene: PatrolScene) => {
  return scene.channels.filter(ch => ch.cameraId).length
}

// 打开摄像头选择器
const openChannelSelector = (sceneIndex: number, position: number) => {
  currentSceneIndex.value = sceneIndex
  currentGridPosition.value = position
  channelSelectorVisible.value = true
}

// 选择摄像头回调
const onCameraSelect = (camera: any) => {
  const scene = scenes.value[currentSceneIndex.value]
  const existingIndex = scene.channels.findIndex(
    ch => ch.gridPosition === currentGridPosition.value
  )
  
  const channelConfig: ChannelConfig = {
    gridPosition: currentGridPosition.value,
    cameraId: camera.id,
    cameraName: camera.name,
    cameraCode: camera.code,
    streamUrl: camera.streamUrl,
    streamType: 1,
    // ✅ 保存播放视频所需的完整信息
    nvrId: camera.nvrId,
    channelNo: camera.channelNo,
    ipAddress: camera.ipAddress,
    ptzSupport: camera.ptzSupport
  }
  
  if (existingIndex >= 0) {
    scene.channels[existingIndex] = channelConfig
  } else {
    scene.channels.push(channelConfig)
  }
  
  channelSelectorVisible.value = false
  emitUpdate()
}

// 移除通道
const removeChannel = (scene: PatrolScene, position: number) => {
  ElMessageBox.confirm('确定要移除这个摄像头吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = scene.channels.findIndex(ch => ch.gridPosition === position)
    if (index >= 0) {
      scene.channels.splice(index, 1)
      emitUpdate()
      ElMessage.success('移除成功')
    }
  }).catch(() => {})
}

const emitUpdate = () => {
  emit('update:modelValue', scenes.value)
}
</script>

<style scoped lang="scss">
.scene-configurator {
  .scene-card {
    margin-bottom: 20px;
    
    .scene-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .scene-title {
        font-size: 16px;
        font-weight: 600;
        color: #ffffff;
        display: flex;
        align-items: center;
        gap: 8px;
      }
      
      .scene-actions {
        display: flex;
        gap: 8px;
      }
    }
  }
  
  .channel-grid {
    display: grid;
    gap: 12px;
    margin-top: 16px;
    
    &.grid-1x1 {
      grid-template-columns: repeat(1, 1fr);
    }
    
    &.grid-2x2 {
      grid-template-columns: repeat(2, 1fr);
    }
    
    &.grid-3x3 {
      grid-template-columns: repeat(3, 1fr);
    }
    
    &.grid-4x4 {
      grid-template-columns: repeat(4, 1fr);
    }
    
    .grid-cell {
      aspect-ratio: 16/9;
      border: 2px dashed rgba(255, 255, 255, 0.2);
      border-radius: 8px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: all 0.3s;
      background: #0a0a0a;
      
      &:hover {
        border-color: #1890ff;
        background: rgba(24, 144, 255, 0.1);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
      }
      
      .channel-info {
        width: 100%;
        height: 100%;
        padding: 12px;
        background: #1a1a1a;
        border: 2px solid #1890ff;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        position: relative;
        
        .camera-icon {
          font-size: 32px;
          color: #1890ff;
          margin-bottom: 8px;
        }
        
        .camera-name {
          font-size: 14px;
          font-weight: 600;
          color: #ffffff;
          margin-bottom: 4px;
          text-align: center;
          word-break: break-all;
        }
        
        .camera-code {
          font-size: 12px;
          color: rgba(255, 255, 255, 0.6);
        }
        
        .remove-btn {
          position: absolute;
          top: 4px;
          right: 4px;
          z-index: 10;
        }
      }
      
      .empty-cell {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        color: rgba(255, 255, 255, 0.4);
        
        .add-icon {
          font-size: 32px;
          margin-bottom: 8px;
        }
        
        span {
          font-size: 12px;
        }
      }
    }
  }
}
</style>
