<template>
  <div class="ptz-control-panel">
    <!-- 不支持云台时显示提示 -->
    <div v-if="!enabled" class="ptz-panel-disabled">
      <Icon icon="ep:warning-filled" :size="48" />
      <p class="main-tip">{{ disabledReason === 'no-ptz' ? '当前通道不支持云台控制' : '请先选择一个正在播放的通道' }}</p>
      <p class="sub-tip">{{ disabledReason === 'no-ptz' ? '该摄像头为固定镜头，无法进行云台操作' : '支持云台控制的摄像头可进行方向、变焦等操作' }}</p>
    </div>

    <!-- 支持云台时显示完整控制面板 -->
    <template v-else>
      <!-- 预设点列表 -->
      <div class="ptz-section">
        <div class="section-header">
          <span class="section-title">预设点列表</span>
          <span class="channel-badge" v-if="channelNo">{{ presetList.length }}/255</span>
        </div>
        <div class="preset-list" v-loading="presetLoading">
          <div 
            v-for="preset in presetList" 
            :key="preset.id" 
            class="preset-item"
            :class="{ active: selectedPreset?.id === preset.id }"
            @click="handleSelectPreset(preset)"
            @dblclick="handleGotoPreset(preset.presetNo)"
          >
            <Icon icon="ep:location" class="preset-icon" />
            <span class="preset-name">{{ preset.presetName }}</span>
          </div>
          <div v-if="presetList.length === 0 && !presetLoading" class="preset-empty">
            暂无预设点
          </div>
        </div>
        <!-- 预设点操作栏（单行布局） -->
        <div class="preset-actions">
          <el-input 
            v-model="presetName" 
            size="small" 
            :placeholder="selectedPreset ? selectedPreset.presetName : '预设点名称'"
            class="preset-name-input"
            clearable
            @clear="selectedPreset = null"
          />
          <el-tooltip content="添加预设点" placement="top">
            <el-button size="small" type="success" :icon="Plus" :loading="settingPreset" @click="handleSetPreset" />
          </el-tooltip>
          <el-tooltip content="转到预设点" placement="top">
            <el-button size="small" type="primary" :icon="VideoPlay" :disabled="!selectedPreset" @click="selectedPreset && handleGotoPreset(selectedPreset.presetNo)" />
          </el-tooltip>
          <el-tooltip content="删除预设点" placement="top">
            <el-button size="small" type="danger" :icon="Delete" :disabled="!selectedPreset" @click="handleDeletePreset" />
          </el-tooltip>
          <el-tooltip v-if="selectedPreset" content="取消选择" placement="top">
            <el-button size="small" :icon="Close" @click="selectedPreset = null" />
          </el-tooltip>
        </div>
      </div>

      <!-- 巡航路线 -->
      <div class="ptz-section">
        <div class="section-header">
          <span class="section-title">巡航路线</span>
          <el-button size="small" type="primary" link @click="handleOpenCruiseManager">
            <Icon icon="ep:setting" /> 管理
          </el-button>
        </div>
        <!-- 巡航选择和控制 -->
        <div class="cruise-control">
          <el-select
            v-model="selectedCruiseId"
            size="small"
            placeholder="选择巡航路线"
            clearable
            class="cruise-select"
            :disabled="!!activeCruise"
          >
            <el-option
              v-for="cruise in cruiseList"
              :key="cruise.id!"
              :label="cruise.cruiseName"
              :value="cruise.id!"
            />
          </el-select>
          <el-tooltip v-if="!activeCruise" content="启动巡航" placement="top">
            <el-button 
              size="small" 
              type="success" 
              :icon="VideoPlay" 
              :disabled="!selectedCruiseId"
              @click="handleStartCruise"
            />
          </el-tooltip>
          <el-tooltip v-else content="停止巡航" placement="top">
            <el-button 
              size="small" 
              type="danger" 
              :icon="VideoPause"
              @click="handleStopCruise"
            />
          </el-tooltip>
        </div>
        <!-- 巡航状态 -->
        <div class="cruise-status" v-if="activeCruise">
          <Icon icon="ep:loading" class="cruise-icon running" />
          <span class="cruise-running">{{ activeCruise.cruiseName }} 运行中</span>
        </div>
        <div class="cruise-status" v-else-if="cruiseList.length === 0">
          <span class="cruise-idle">暂无巡航路线，请先创建</span>
        </div>
      </div>

      <!-- 云台方向控制 -->
      <div class="ptz-section">
        <div class="section-header">
          <span class="section-title">云台控制</span>
          <span class="channel-badge" v-if="channelNo">通道 {{ channelNo }}</span>
        </div>
        <div class="ptz-dpad">
          <button
            class="ptz-dir up"
            @mousedown="handlePtzStart('up')"
            @mouseup="handlePtzStop"
            @mouseleave="handlePtzStop"
          >
            <Icon icon="ep:arrow-up-bold" />
          </button>
          <button
            class="ptz-dir left"
            @mousedown="handlePtzStart('left')"
            @mouseup="handlePtzStop"
            @mouseleave="handlePtzStop"
          >
            <Icon icon="ep:arrow-left-bold" />
          </button>
          <button class="ptz-center" title="停止" @click="handlePtzStop">
            <Icon icon="ep:aim" />
          </button>
          <button
            class="ptz-dir right"
            @mousedown="handlePtzStart('right')"
            @mouseup="handlePtzStop"
            @mouseleave="handlePtzStop"
          >
            <Icon icon="ep:arrow-right-bold" />
          </button>
          <button
            class="ptz-dir down"
            @mousedown="handlePtzStart('down')"
            @mouseup="handlePtzStop"
            @mouseleave="handlePtzStop"
          >
            <Icon icon="ep:arrow-down-bold" />
          </button>
        </div>

        <!-- 变焦/聚焦控制 -->
        <div class="ptz-zoom-grid">
          <button
            class="ptz-op"
            @mousedown="handlePtzStart('zoomin')"
            @mouseup="handlePtzStop"
            @mouseleave="handlePtzStop"
          >
            <Icon icon="ep:zoom-in" />
            <span>放大</span>
          </button>
          <button
            class="ptz-op"
            @mousedown="handlePtzStart('focusin')"
            @mouseup="handlePtzStop"
            @mouseleave="handlePtzStop"
          >
            <Icon icon="ep:aim" />
            <span>聚近</span>
          </button>
          <button
            class="ptz-op"
            @mousedown="handlePtzStart('zoomout')"
            @mouseup="handlePtzStop"
            @mouseleave="handlePtzStop"
          >
            <Icon icon="ep:zoom-out" />
            <span>缩小</span>
          </button>
          <button
            class="ptz-op"
            @mousedown="handlePtzStart('focusout')"
            @mouseup="handlePtzStop"
            @mouseleave="handlePtzStop"
          >
            <Icon icon="ep:aim" />
            <span>聚远</span>
          </button>
        </div>

        <!-- 区域放大与复位 -->
        <div class="area-zoom-section">
          <div class="area-zoom-buttons">
            <el-button 
              :type="areaZoomActive ? 'danger' : 'primary'" 
              class="area-zoom-btn"
              @click="handleToggleAreaZoom"
            >
              <Icon :icon="areaZoomActive ? 'ep:close' : 'ep:crop'" class="btn-icon" />
              <span>{{ areaZoomActive ? '取消框选' : '区域放大' }}</span>
            </el-button>
            <el-button 
              type="warning"
              class="ptz-reset-btn"
              :loading="resetting"
              @click="handlePtzReset"
            >
              <Icon icon="ep:refresh-left" class="btn-icon" />
              <span>复位</span>
            </el-button>
          </div>
          <p class="area-zoom-hint" v-if="areaZoomActive">
            在视频画面上框选区域进行放大
          </p>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, VideoPlay, VideoPause, Delete, Close } from '@element-plus/icons-vue'
import { Icon } from '@/components/Icon'
import * as PresetApi from '@/api/iot/cameraPreset'
import * as CruiseApi from '@/api/iot/cameraCruise'

// Props
interface Props {
  enabled: boolean
  channelNo?: number | null
  channelId?: number | null  // 通道数据库ID，用于查询预设点
  disabledReason?: 'no-playing' | 'no-ptz' | null
}

const props = withDefaults(defineProps<Props>(), {
  enabled: false,
  channelNo: null,
  channelId: null,
  disabledReason: null
})

// Emits
const emit = defineEmits<{
  (e: 'ptz-move', command: string, speed: number): void
  (e: 'ptz-stop'): void
  (e: 'goto-preset', presetId: number): void
  (e: 'set-preset', presetNo: number, presetName: string): void
  (e: 'clear-preset', presetId: number): void
  (e: 'open-cruise-manager'): void
  (e: 'stop-cruise'): void
  (e: 'toggle-area-zoom', active: boolean): void
  (e: 'ptz-reset'): void
}>()

// 状态
const presetName = ref('')
const presetList = ref<PresetApi.CameraPresetVO[]>([])
const presetLoading = ref(false)
const settingPreset = ref(false)
const selectedPreset = ref<PresetApi.CameraPresetVO | null>(null)
const cruiseList = ref<CruiseApi.CameraCruiseVO[]>([])
const selectedCruiseId = ref<number | undefined>(undefined)
const activeCruise = ref<CruiseApi.CameraCruiseVO | null>(null)
const areaZoomActive = ref(false)
const resetting = ref(false)
let currentCommand: string | null = null

// 区域放大切换
const handleToggleAreaZoom = () => {
  areaZoomActive.value = !areaZoomActive.value
  emit('toggle-area-zoom', areaZoomActive.value)
}

// 云台复位
const handlePtzReset = () => {
  resetting.value = true
  emit('ptz-reset')
  // 3秒后自动重置状态（复位操作大约需要3秒）
  setTimeout(() => {
    resetting.value = false
  }, 3500)
}

// 当通道切换或云台禁用时，取消区域放大模式
watch(() => props.enabled, (enabled) => {
  if (!enabled && areaZoomActive.value) {
    areaZoomActive.value = false
    emit('toggle-area-zoom', false)
  }
})

watch(() => props.channelNo, () => {
  if (areaZoomActive.value) {
    areaZoomActive.value = false
    emit('toggle-area-zoom', false)
  }
})

// 加载预设点列表
const loadPresetList = async () => {
  if (!props.channelId) {
    presetList.value = []
    return
  }
  presetLoading.value = true
  try {
    const res = await PresetApi.getPresetListByChannelId(props.channelId)
    presetList.value = res || []
  } catch (error) {
    console.error('加载预设点失败:', error)
    presetList.value = []
  } finally {
    presetLoading.value = false
  }
}

// 加载巡航列表和状态
const loadCruiseStatus = async () => {
  if (!props.channelId) {
    cruiseList.value = []
    activeCruise.value = null
    return
  }
  try {
    const cruises = await CruiseApi.getCruiseListByChannelId(props.channelId)
    cruiseList.value = cruises || []
    // 找到正在运行的巡航
    const running = cruises?.find((c: any) => c.status === 1) || null
    activeCruise.value = running
    // 如果有运行中的巡航，自动选中
    if (running) {
      selectedCruiseId.value = running.id!
    }
  } catch (error) {
    console.error('加载巡航状态失败:', error)
    cruiseList.value = []
    activeCruise.value = null
  }
}

// 监听通道变化，重新加载数据
watch(
  () => props.channelId,
  (newId) => {
    // 切换通道时重置选择状态
    selectedCruiseId.value = undefined
    selectedPreset.value = null
    if (newId && props.enabled) {
      loadPresetList()
      loadCruiseStatus()
    } else {
      presetList.value = []
      activeCruise.value = null
    }
  },
  { immediate: true }
)

watch(
  () => props.enabled,
  (enabled) => {
    if (enabled && props.channelId) {
      loadPresetList()
      loadCruiseStatus()
    }
  }
)

// 选择预设点
const handleSelectPreset = (preset: PresetApi.CameraPresetVO) => {
  selectedPreset.value = selectedPreset.value?.id === preset.id ? null : preset
}

// 转到预设点
const handleGotoPreset = (presetNo: number) => {
  emit('goto-preset', presetNo)
  ElMessage.success(`正在转到预设点 ${presetNo}`)
}

// 设置预设点（新增）
const handleSetPreset = async () => {
  if (!presetName.value.trim()) {
    ElMessage.warning('请输入预设点名称')
    return
  }
  if (!props.channelId) {
    ElMessage.warning('通道信息不完整')
    return
  }
  
  settingPreset.value = true
  try {
    // 1. 自动分配一个未使用的编号
    const usedNos = presetList.value.map(p => p.presetNo)
    let newNo = 1
    while (usedNos.includes(newNo) && newNo <= 255) {
      newNo++
    }
    if (newNo > 255) {
      ElMessage.error('预设点数量已达上限')
      return
    }
    
    // 2. 保存到数据库
    await PresetApi.createPreset({
      channelId: props.channelId,
      presetNo: newNo,
      presetName: presetName.value.trim()
    })
    
    // 3. 同步到设备（通过 RPC2 SetPreset + 后端 NetSDK 设置名称）
    emit('set-preset', newNo, presetName.value.trim())
    
    ElMessage.success(`预设点 "${presetName.value}" 已保存到设备 (编号: ${newNo})`)
    presetName.value = ''
    
    // 刷新列表
    await loadPresetList()
  } catch (error: any) {
    console.error('设置预设点失败:', error)
    ElMessage.error(error?.message || '设置预设点失败')
  } finally {
    settingPreset.value = false
  }
}

// 删除预设点
const handleDeletePreset = async () => {
  if (!selectedPreset.value) return
  
  try {
    // 1. 从设备删除（通过 RPC2 ClearPreset）
    emit('clear-preset', selectedPreset.value.presetNo)
    
    // 2. 从数据库删除
    await PresetApi.deletePreset(selectedPreset.value.id!)
    
    ElMessage.success(`预设点 "${selectedPreset.value.presetName}" 已删除`)
    selectedPreset.value = null
    
    // 刷新列表
    await loadPresetList()
  } catch (error: any) {
    console.error('删除预设点失败:', error)
    ElMessage.error(error?.message || '删除预设点失败')
  }
}

// 巡航操作
const handleOpenCruiseManager = () => {
  emit('open-cruise-manager')
}

const handleStartCruise = async () => {
  if (!selectedCruiseId.value) {
    ElMessage.warning('请选择巡航路线')
    return
  }
  try {
    await CruiseApi.startCruise(selectedCruiseId.value)
    ElMessage.success('巡航已启动')
    // 刷新状态
    await loadCruiseStatus()
  } catch (error: any) {
    ElMessage.error(error?.message || '启动巡航失败')
  }
}

const handleStopCruise = async () => {
  if (!activeCruise.value) return
  try {
    await CruiseApi.stopCruise(activeCruise.value.id!)
    ElMessage.success('巡航已停止')
    activeCruise.value = null
    // 刷新状态
    await loadCruiseStatus()
  } catch (error: any) {
    ElMessage.error(error?.message || '停止巡航失败')
  }
}

// 云台开始移动
const handlePtzStart = (command: string) => {
  currentCommand = command
  emit('ptz-move', command, 128) // 使用固定中等速度
}

// 云台停止
const handlePtzStop = () => {
  if (currentCommand) {
    emit('ptz-stop')
    currentCommand = null
  }
}

// 暴露方法
defineExpose({
  refreshPresetList: loadPresetList,
  refreshCruiseStatus: loadCruiseStatus
})
</script>

<style lang="scss" scoped>
.ptz-control-panel {
  padding: 12px;
  max-height: 600px;
  overflow-y: auto;

  .ptz-panel-disabled {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 20px;
    color: #909399;
    text-align: center;

    .main-tip {
      margin: 16px 0 8px 0;
      font-size: 14px;
      font-weight: 500;
      color: #e6a23c;
    }

    .sub-tip {
      margin: 0;
      font-size: 12px;
      color: #6c757d;
    }
  }

  .ptz-section {
    background: #15243a;
    border: 1px solid #233754;
    border-radius: 8px;
    padding: 10px;
    margin-bottom: 10px;

    .section-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 10px;
      
      .section-title {
        font-size: 13px;
        font-weight: 500;
        color: #cfe3ff;
      }
      
      .channel-badge {
        font-size: 11px;
        color: #67c23a;
        background: rgba(103, 194, 58, 0.15);
        padding: 2px 8px;
        border-radius: 4px;
      }
    }

    .preset-list {
      max-height: 120px;
      overflow-y: auto;
      margin-bottom: 10px;
      background: rgba(0, 0, 0, 0.2);
      border-radius: 4px;
      
      .preset-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 10px;
        cursor: pointer;
        transition: background 0.2s;
        border-bottom: 1px solid rgba(35, 55, 84, 0.5);
        
        &:last-child {
          border-bottom: none;
        }
        
        &:hover {
          background: rgba(64, 158, 255, 0.15);
        }
        
        &.active {
          background: rgba(64, 158, 255, 0.25);
          border-left: 2px solid #409eff;
        }
        
        .preset-icon {
          color: #409eff;
          font-size: 14px;
        }
        
        .preset-name {
          font-size: 12px;
          color: #b8d4ff;
        }
      }
      
      .preset-empty {
        padding: 20px;
        text-align: center;
        color: #666;
        font-size: 12px;
      }
    }

    .preset-actions {
      display: flex;
      gap: 4px;
      align-items: center;
      
      .preset-name-input {
        flex: 1;
        min-width: 80px;
      }
      
      .el-button {
        padding: 5px 8px;
      }
    }

    .cruise-control {
      display: flex;
      gap: 8px;
      align-items: center;
      margin-bottom: 8px;
      
      .cruise-select {
        flex: 1;
        
        :deep(.el-input__wrapper) {
          background: rgba(0, 0, 0, 0.3);
          border-color: rgba(255, 255, 255, 0.1);
        }
        
        :deep(.el-input__inner) {
          color: #fff;
        }
      }
      
      .el-button {
        padding: 5px 8px;
      }
    }

    .cruise-status {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 10px;
      background: rgba(0, 0, 0, 0.2);
      border-radius: 4px;
      font-size: 12px;
      color: #b8d4ff;
      
      .cruise-icon {
        font-size: 14px;
        color: #909399;
        
        &.running {
          color: #67c23a;
          animation: spin 1s linear infinite;
        }
      }
      
      .cruise-running {
        color: #67c23a;
      }
      
      .cruise-idle {
        color: #666;
      }
    }
  }

  .ptz-dpad {
    position: relative;
    width: 130px;
    height: 130px;
    margin: 0 auto 12px;
    background: radial-gradient(ellipse at center, rgba(67, 108, 167, 0.25), rgba(24, 36, 60, 0.6));
    border: 1px solid #2b3e62;
    border-radius: 50%;
    display: grid;
    place-items: center;
  }

  .ptz-dir {
    position: absolute;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: linear-gradient(#2a4672, #243a5e);
    border: 1px solid #3a5b8a;
    color: #cfe3ff;
    display: grid;
    place-items: center;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: linear-gradient(#3a5b8a, #2a4672);
      border-color: #4ea1ff;
    }

    &:active {
      transform: scale(0.95);
    }

    &.up {
      top: 6px;
      left: 50%;
      transform: translateX(-50%);
    }

    &.down {
      bottom: 6px;
      left: 50%;
      transform: translateX(-50%);
    }

    &.left {
      left: 6px;
      top: 50%;
      transform: translateY(-50%);
    }

    &.right {
      right: 6px;
      top: 50%;
      transform: translateY(-50%);
    }
  }

  .ptz-center {
    position: absolute;
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: linear-gradient(#38679f, #2b5280);
    border: 1px solid #3a5b8a;
    color: #fff;
    display: grid;
    place-items: center;
    cursor: pointer;
    
    &:hover {
      background: linear-gradient(#4a79b1, #3d6492);
    }
  }

  .ptz-zoom-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;

    .ptz-op {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 45px;
      border-radius: 6px;
      border: 1px solid #2b3e62;
      background: #1a2a49;
      color: #cfe3ff;
      cursor: pointer;
      transition: all 0.2s;

      span {
        font-size: 11px;
        margin-top: 2px;
      }

      &:hover {
        border-color: #4ea1ff;
        box-shadow: 0 0 8px rgba(78, 161, 255, 0.35);
      }

      &:active {
        transform: scale(0.95);
      }
    }
  }

  .area-zoom-section {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #2b3e62;

    .area-zoom-buttons {
      display: flex;
      gap: 8px;
    }

    .area-zoom-btn,
    .ptz-reset-btn {
      flex: 1;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      font-size: 13px;

      .btn-icon {
        font-size: 16px;
      }
    }

    .area-zoom-hint {
      margin-top: 8px;
      font-size: 11px;
      color: #f0a020;
      text-align: center;
      animation: blink 1.5s ease-in-out infinite;
    }
  }
}

@keyframes blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
