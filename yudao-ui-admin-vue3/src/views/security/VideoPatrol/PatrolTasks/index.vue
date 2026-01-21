<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="live-patrol-container">
      <!-- 顶部信息栏 -->
      <div class="info-bar">
        <div class="task-info">
          <div class="info-item">
            <span class="label">当前任务：</span>
            <span class="value">{{ currentTask?.taskName || '无' }}</span>
          </div>
          <div class="info-item">
            <span class="label">负责人：</span>
            <span class="value">{{ currentTask?.executorName || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">运行状态：</span>
            <el-tag v-if="currentTask?.runningStatus === 'running'" type="success">运行中</el-tag>
            <el-tag v-else-if="currentTask?.runningStatus === 'trial'" type="primary">试运行</el-tag>
            <el-tag v-else type="info">未运行</el-tag>
          </div>
        </div>
        <div class="scene-info">
          <div class="info-item">
            <span class="label">当前场景：</span>
            <span class="value">{{ currentScene?.sceneName || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">场景进度：</span>
            <span class="value">{{ currentSceneIndex + 1 }} / {{ totalScenes }}</span>
          </div>
          <div class="info-item">
            <span class="label">剩余时间：</span>
            <span class="value">{{ remainingTime }}秒</span>
          </div>
          <div class="info-item" v-if="isWaitingInterval">
            <span class="label">状态：</span>
            <el-tag type="warning">等待下一轮巡更</el-tag>
          </div>
        </div>
      </div>

      <!-- 视频播放区域 -->
      <div v-if="currentScene" class="video-container">
        <div 
          class="video-grid" 
          :class="`grid-layout-${currentScene.gridLayout || '2x2'}`"
        >
          <div 
            v-for="channel in currentScene.channels" 
            :key="channel.gridPosition"
            class="video-item"
            :class="{ 'empty-grid': channel.isEmpty }"
          >
            <div v-if="!channel.isEmpty" class="video-wrapper">
              <div class="video-header">
                <span class="camera-name">{{ channel.cameraName }}</span>
              </div>
              <div class="video-player">
                <!-- 这里集成实际的视频播放组件 -->
                <div class="placeholder-player">
                  <Icon icon="ep:video-camera" :size="48" />
                  <p>{{ channel.cameraName }}</p>
                  <p class="stream-url">{{ channel.streamUrl || 'Stream URL' }}</p>
                </div>
              </div>
            </div>
            <div v-else class="empty-placeholder">
              <Icon icon="ep:picture" :size="32" />
              <p>空白</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 无任务提示 -->
      <div v-else class="no-task">
        <Icon icon="ep:video-camera-filled" :size="80" />
        <p class="tip">当前无运行中的巡更任务</p>
        <el-button type="primary" @click="loadRunningTasks">刷新</el-button>
      </div>

      <!-- 底部控制栏 -->
      <div v-if="currentTask" class="control-bar">
        <div class="left-controls">
          <el-button 
            :type="isPlaying ? 'warning' : 'success'" 
            @click="togglePlay"
            :icon="isPlaying ? VideoPause : VideoPlay"
          >
            {{ isPlaying ? '暂停' : '播放' }}
          </el-button>
          <el-button @click="playNextScene" :icon="DArrowRight">下一场景</el-button>
          <el-button @click="playPrevScene" :icon="DArrowLeft">上一场景</el-button>
        </div>
        <div class="right-controls">
          <el-button @click="loadRunningTasks" :icon="Refresh">刷新任务</el-button>
        </div>
      </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts" name="LivePatrol">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { VideoPlay, VideoPause, DArrowRight, DArrowLeft, Refresh } from '@element-plus/icons-vue'
import { getVideoPatrolTaskPage, getVideoPatrolTask } from '@/api/iot/videoPatrol'
import type { VideoPatrolTaskVO } from '@/api/iot/videoPatrol'

const route = useRoute()

// 当前任务
const currentTask = ref<VideoPatrolTaskVO | null>(null)
const currentSceneIndex = ref(0)
const isPlaying = ref(false)
const remainingTime = ref(0)
const isWaitingInterval = ref(false) // 是否在等待巡更间隔

let sceneTimer: number | null = null
let countdownTimer: number | null = null

// 计算属性
const currentScene = computed(() => {
  if (!currentTask.value?.patrolScenes || currentTask.value.patrolScenes.length === 0) {
    return null
  }
  return currentTask.value.patrolScenes[currentSceneIndex.value]
})

const totalScenes = computed(() => {
  return currentTask.value?.patrolScenes?.length || 0
})

// 判断当前时间是否在任务的时间段内
const isInTimeSlot = (task: VideoPatrolTaskVO): boolean => {
  if (!task.timeSlots || task.timeSlots.length === 0) {
    return true // 如果没有配置时间段，默认全天运行
  }

  const now = new Date()
  const currentMinutes = now.getHours() * 60 + now.getMinutes()

  return task.timeSlots.some((slot: any) => {
    const [startHour, startMin] = slot.startTime.split(':').map(Number)
    const [endHour, endMin] = slot.endTime.split(':').map(Number)
    const startMinutes = startHour * 60 + startMin
    const endMinutes = endHour * 60 + endMin

    return currentMinutes >= startMinutes && currentMinutes <= endMinutes
  })
}

// 加载运行中的任务
const loadRunningTasks = async () => {
  try {
    // 如果URL中有taskId参数，直接加载指定任务
    const taskId = route.query.taskId as string
    if (taskId) {
      const task = await getVideoPatrolTask(parseInt(taskId))
      
      // 试运行或正式运行都可以播放
      if (task.runningStatus === 'running' || task.runningStatus === 'trial') {
        currentTask.value = task
        startPatrol()
        return
      } else {
        ElMessage.warning('该任务未运行')
      }
    }

    // 否则查询所有运行中的任务
    const res = await getVideoPatrolTaskPage({
      pageNo: 1,
      pageSize: 100,
      status: 1 // 启用状态
    })

    // 找到运行中且在时间段内的任务（试运行不考虑时间段）
    const runningTasks = res.list.filter((task: VideoPatrolTaskVO) => {
      if (task.runningStatus === 'trial') {
        // 试运行：不考虑时间段，直接播放
        return true
      } else if (task.runningStatus === 'running') {
        // 正式运行：需要在时间段内
        return isInTimeSlot(task)
      }
      return false
    })

    if (runningTasks.length > 0) {
      currentTask.value = runningTasks[0] // 取第一个运行中的任务
      startPatrol()
    } else {
      ElMessage.info('当前无运行中的巡更任务')
      currentTask.value = null
    }
  } catch (error) {
    console.error('[实时巡更] 加载任务失败:', error)
    ElMessage.error('加载任务失败')
  }
}

// 开始巡更
const startPatrol = () => {
  if (!currentTask.value || !currentTask.value.patrolScenes || currentTask.value.patrolScenes.length === 0) {
    ElMessage.warning('任务未配置场景')
    return
  }

  currentSceneIndex.value = 0
  isPlaying.value = true
  playCurrentScene()
}

// 播放当前场景
const playCurrentScene = () => {
  if (!currentScene.value) return

  // 设置剩余时间
  remainingTime.value = currentScene.value.duration || 30

  // 清除旧的定时器
  clearTimers()

  // 倒计时定时器
  countdownTimer = window.setInterval(() => {
    remainingTime.value--
    if (remainingTime.value <= 0) {
      playNextScene()
    }
  }, 1000)

  console.log('[实时巡更] 播放场景:', currentScene.value.sceneName, '时长:', currentScene.value.duration)
}

// 播放下一个场景
const playNextScene = () => {
  if (!currentTask.value?.patrolScenes) return

  const nextIndex = currentSceneIndex.value + 1
  
  // 如果已经是最后一个场景，需要等待巡更间隔
  if (nextIndex >= currentTask.value.patrolScenes.length) {
    const intervalMinutes = currentTask.value.intervalMinutes || 60
    const intervalSeconds = intervalMinutes * 60
    
    console.log(`[实时巡更] 所有场景已播放完毕，等待 ${intervalMinutes} 分钟后开始下一轮`)
    
    isWaitingInterval.value = true
    remainingTime.value = intervalSeconds
    
    // 清除旧的定时器
    clearTimers()
    
    // 倒计时定时器
    countdownTimer = window.setInterval(() => {
      remainingTime.value--
      if (remainingTime.value <= 0) {
        isWaitingInterval.value = false
        currentSceneIndex.value = 0
        playCurrentScene()
      }
    }, 1000)
  } else {
    currentSceneIndex.value = nextIndex
    
    if (isPlaying.value) {
      playCurrentScene()
    }
  }
}

// 播放上一个场景
const playPrevScene = () => {
  if (!currentTask.value?.patrolScenes) return

  currentSceneIndex.value = currentSceneIndex.value === 0 
    ? currentTask.value.patrolScenes.length - 1 
    : currentSceneIndex.value - 1
  
  if (isPlaying.value) {
    playCurrentScene()
  }
}

// 切换播放/暂停
const togglePlay = () => {
  isPlaying.value = !isPlaying.value
  
  if (isPlaying.value) {
    playCurrentScene()
  } else {
    clearTimers()
  }
}

// 清除定时器
const clearTimers = () => {
  if (sceneTimer) {
    clearTimeout(sceneTimer)
    sceneTimer = null
  }
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

// 页面加载
onMounted(() => {
  loadRunningTasks()
})

// 页面卸载
onUnmounted(() => {
  clearTimers()
})
</script>

<style scoped lang="scss">
.live-patrol-container {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
  background: #0a0a0a;
  padding: 16px;

  .info-bar {
    background: #1a1a1a;
    border: 1px solid #2d2d2d;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .task-info,
    .scene-info {
      display: flex;
      gap: 24px;
    }

    .info-item {
      display: flex;
      align-items: center;
      gap: 8px;

      .label {
        color: rgba(255, 255, 255, 0.6);
        font-size: 14px;
      }

      .value {
        color: rgba(255, 255, 255, 0.9);
        font-size: 14px;
        font-weight: 500;
      }
    }
  }

  .video-container {
    flex: 1;
    background: #1a1a1a;
    border: 1px solid #2d2d2d;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 16px;
    overflow: hidden;

    .video-grid {
      width: 100%;
      height: 100%;
      display: grid;
      gap: 8px;

      &.grid-layout-1x1 {
        grid-template-columns: 1fr;
        grid-template-rows: 1fr;
      }

      &.grid-layout-2x2 {
        grid-template-columns: repeat(2, 1fr);
        grid-template-rows: repeat(2, 1fr);
      }

      &.grid-layout-3x3 {
        grid-template-columns: repeat(3, 1fr);
        grid-template-rows: repeat(3, 1fr);
      }

      &.grid-layout-4x4 {
        grid-template-columns: repeat(4, 1fr);
        grid-template-rows: repeat(4, 1fr);
      }

      .video-item {
        background: #2d2d2d;
        border-radius: 4px;
        overflow: hidden;
        position: relative;

        &.empty-grid {
          background: #1a1a1a;
          border: 1px dashed #404040;
        }

        .video-wrapper {
          height: 100%;
          display: flex;
          flex-direction: column;

          .video-header {
            background: rgba(0, 0, 0, 0.6);
            padding: 8px 12px;
            display: flex;
            justify-content: space-between;
            align-items: center;

            .camera-name {
              color: #fff;
              font-size: 14px;
              font-weight: 500;
            }
          }

          .video-player {
            flex: 1;
            background: #000;
            display: flex;
            align-items: center;
            justify-content: center;

            .placeholder-player {
              text-align: center;
              color: rgba(255, 255, 255, 0.6);

              p {
                margin: 8px 0;
                font-size: 14px;
              }

              .stream-url {
                font-size: 12px;
                color: rgba(255, 255, 255, 0.4);
              }
            }
          }
        }

        .empty-placeholder {
          height: 100%;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          color: rgba(255, 255, 255, 0.3);

          p {
            margin-top: 8px;
            font-size: 14px;
          }
        }
      }
    }
  }

  .no-task {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: rgba(255, 255, 255, 0.4);

    .tip {
      margin: 16px 0;
      font-size: 16px;
    }
  }

  .control-bar {
    background: #1a1a1a;
    border: 1px solid #2d2d2d;
    border-radius: 8px;
    padding: 12px 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .left-controls,
    .right-controls {
      display: flex;
      gap: 8px;
    }
  }
}
</style>
