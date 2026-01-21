<template>
  <div class="video-grid-container">
    <!-- 分屏布局选择 -->
    <div class="layout-selector">
      <el-button-group>
        <el-button
          v-for="layout in layoutOptions"
          :key="layout.type"
          :type="currentLayout === layout.type ? 'primary' : 'default'"
          @click="$emit('change-layout', layout.type)"
        >
          <Icon :icon="layout.icon" />
          {{ layout.name }}
        </el-button>
      </el-button-group>
    </div>

    <!-- 视频网格 -->
    <div 
      class="video-grid" 
      :class="`layout-${currentLayout}`"
    >
      <div
        v-for="(screen, index) in screens"
        :key="index"
        class="video-cell"
        :class="{ 
          selected: selectedScreen === index,
          'has-content': cellChannelsData.has(index)
        }"
        @click="$emit('select-screen', index)"
        @drop="handleDrop($event, index)"
        @dragover.prevent
        @dragenter="dragOverScreenIndex = index"
        @dragleave="dragOverScreenIndex = null"
        @contextmenu.prevent="handleRightClick($event, index)"
      >
        <!-- 已配置通道时显示截图 -->
        <template v-if="cellChannelsData.has(index)">
          <div class="video-preview">
            <!-- 截图 -->
            <img
              v-if="snapshotUrls.get(index)"
              :src="snapshotUrls.get(index)"
              :alt="cellChannelsData.get(index)![0].name"
              class="preview-image"
              @error="handleSnapshotError($event, index)"
            />
            <div v-else class="no-snapshot">
              <Icon icon="ep:picture" :size="48" />
              <p>加载中...</p>
            </div>
            
            <!-- 通道信息叠加层 -->
            <div class="preview-overlay">
              <div class="channel-name">
                {{ cellChannelsData.get(index)![0].name }}
              </div>
              <div class="channel-count" v-if="cellChannelsData.get(index)!.length > 1">
                共 {{ cellChannelsData.get(index)!.length }} 个通道
              </div>
            </div>
            
            <!-- 播放按钮 -->
            <div class="play-button" @click.stop="$emit('play-channel', index)">
              <Icon icon="ep:video-play" :size="48" />
            </div>
            
            <!-- 管理按钮 -->
            <div class="manage-button" @click.stop="$emit('manage-cell', index)">
              <Icon icon="ep:setting" />
            </div>
          </div>
        </template>
        
        <!-- 未配置通道时显示空状态 -->
        <template v-else>
          <Icon icon="ep:video-pause" :size="48" />
          <p>窗口 {{ index + 1 }}</p>
          <p class="tip">拖拽摄像头到此处添加</p>
          <p class="tip">或右键添加通道</p>
        </template>

        <!-- 视频播放容器 -->
        <div :id="`h5_video_container_${index}`" class="video-container" style="display: none;">
          <video 
            :id="`h5_video_${index}`" 
            class="video-player"
            autoplay
            muted
          ></video>
          <canvas 
            :id="`h5_canvas_${index}`" 
            class="video-canvas"
          ></canvas>
          <div :id="`h5_loading_${index}`" class="video-loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>正在加载...</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Icon } from '@/components/Icon'
import { Loading } from '@element-plus/icons-vue'

// Props
interface Props {
  screens: any[]
  currentLayout: string
  selectedScreen: number | null
  cellChannelsData: Map<number, any[]>
  snapshotUrls: Map<number, string>
  layoutOptions: any[]
}

defineProps<Props>()

// Emits
defineEmits<{
  'change-layout': [layout: string]
  'select-screen': [index: number]
  'play-channel': [index: number]
  'manage-cell': [index: number]
  'drop': [event: DragEvent, index: number]
  'right-click': [event: MouseEvent, index: number]
}>()

// 拖拽状态
const dragOverScreenIndex = ref<number | null>(null)

// 处理拖放
const handleDrop = (event: DragEvent, index: number) => {
  dragOverScreenIndex.value = null
  // 发送事件到父组件处理
}

// 处理右键菜单
const handleRightClick = (event: MouseEvent, index: number) => {
  // 发送事件到父组件处理
}

// 处理截图加载错误
const handleSnapshotError = (event: Event, index: number) => {
  console.warn(`[截图加载] 格子${index}截图加载失败`)
  // 可以设置默认图片或重试
}
</script>

<style scoped lang="scss">
.video-grid-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 12px;
  background: #0a0e27;
}

.layout-selector {
  margin-bottom: 12px;
  display: flex;
  justify-content: center;
}

.video-grid {
  flex: 1;
  display: grid;
  gap: 8px;
  
  &.layout-1x1 {
    grid-template-columns: 1fr;
    grid-template-rows: 1fr;
  }
  
  &.layout-2x2 {
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: repeat(2, 1fr);
  }
  
  &.layout-3x3 {
    grid-template-columns: repeat(3, 1fr);
    grid-template-rows: repeat(3, 1fr);
  }
  
  &.layout-4x4 {
    grid-template-columns: repeat(4, 1fr);
    grid-template-rows: repeat(4, 1fr);
  }
}

.video-cell {
  position: relative;
  background: rgba(255, 255, 255, 0.03);
  border: 2px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.5);

  &:hover {
    border-color: rgba(64, 158, 255, 0.5);
    background: rgba(255, 255, 255, 0.05);
  }

  &.selected {
    border-color: #409eff;
    box-shadow: 0 0 20px rgba(64, 158, 255, 0.3);
  }

  &.has-content {
    padding: 0;
  }

  .tip {
    font-size: 12px;
    margin: 4px 0;
  }
}

.video-preview {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-snapshot {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.5);
  
  p {
    margin-top: 8px;
    font-size: 14px;
  }
}

.preview-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  padding: 12px;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.6), transparent);
  color: #fff;
}

.channel-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.channel-count {
  font-size: 12px;
  opacity: 0.8;
}

.play-button {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: rgba(255, 255, 255, 0.9);
  cursor: pointer;
  transition: all 0.3s;
  filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.3));

  &:hover {
    color: #fff;
    transform: translate(-50%, -50%) scale(1.15);
    filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.5));
  }
}

.manage-button {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  backdrop-filter: blur(4px);

  &:hover {
    background: rgba(0, 0, 0, 0.8);
    transform: scale(1.1);
  }
}

.video-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #000;
}

.video-player,
.video-canvas {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.video-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #fff;
}
</style>
