<template>
  <div class="video-preview-container">
    <el-card class="box-card">
      <!-- 页头 -->
      <template #header>
        <div class="card-header">
          <span>视频预览 - {{ deviceName }}</span>
          <el-button-group>
            <el-button
              v-if="streamInfo"
              :type="streamInfo.status === 'running' ? 'danger' : 'success'"
              @click="toggleStream"
            >
              {{ streamInfo.status === 'running' ? '停止预览' : '开始预览' }}
            </el-button>
            <el-button v-else type="primary" :loading="loading" @click="startLiveStream">
              开始预览
            </el-button>
          </el-button-group>
        </div>
      </template>

      <!-- 视频播放器优先使用 WebSocket-FLV/Jessibuca，回退 HLS -->
      <div v-if="false" class="player-section"></div>

      <!-- 占位符 -->
      <div v-else class="placeholder">
        <!-- <el-empty description="请点击"开始预览"按钮查看实时视频" /> -->
        <el-empty description="请点击『开始预览』按钮查看实时视频" />
      </div>

      <!-- 设备信息 -->
      <el-divider />
      <div class="device-info">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="设备ID">{{ deviceId }}</el-descriptions-item>
          <el-descriptions-item label="设备名称">{{ deviceName }}</el-descriptions-item>
          <el-descriptions-item label="设备IP">{{ deviceIp }}</el-descriptions-item>
          <el-descriptions-item label="流ID" :span="3">{{ streamInfo?.streamId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="RTSP地址" :span="3">
            <el-text type="info" size="small">{{ streamInfo?.rtspUrl || '-' }}</el-text>
          </el-descriptions-item>
          <el-descriptions-item label="HLS地址" :span="3">
            <el-text type="success" size="small">{{ streamInfo?.hlsUrl || '-' }}</el-text>
          </el-descriptions-item>
          <el-descriptions-item label="流状态">
            <el-tag v-if="streamInfo" :type="getStatusType(streamInfo.status)">
              {{ getStatusText(streamInfo.status) }}
            </el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ streamInfo?.createTime || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
 
import { getLiveStream, stopVideoStream, type VideoStreamRespVO } from '@/api/iot/video/stream'
import { getDevice } from '@/api/iot/device'

const route = useRoute()
const deviceId = ref<number>(Number(route.query.deviceId))
const deviceName = ref<string>('')
const deviceIp = ref<string>('')
const loading = ref(false)
const streamInfo = ref<VideoStreamRespVO>()
const playerRef = ref()

// 获取设备信息
const fetchDeviceInfo = async () => {
  try {
    const device = await getDevice(deviceId.value)
    deviceName.value = device.deviceName || ''
    deviceIp.value = device.ipAddress || ''
  } catch (error) {
    console.error('获取设备信息失败:', error)
  }
}

// 开始实时预览
const startLiveStream = async () => {
  loading.value = true
  try {
    streamInfo.value = await getLiveStream(deviceId.value)
    ElMessage.success('视频流创建成功')
  } catch (error) {
    console.error('创建视频流失败:', error)
    ElMessage.error('创建视频流失败，请检查设备是否在线')
  } finally {
    loading.value = false
  }
}

// 停止预览
const stopLiveStream = async () => {
  if (!streamInfo.value) return

  try {
    await ElMessageBox.confirm('确定要停止视频预览吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await stopVideoStream(streamInfo.value.streamId)
    streamInfo.value = undefined
    ElMessage.success('视频流已停止')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('停止视频流失败:', error)
      ElMessage.error('停止视频流失败')
    }
  }
}

// 切换预览状态
const toggleStream = () => {
  if (streamInfo.value?.status === 'running') {
    stopLiveStream()
  } else {
    startLiveStream()
  }
}

// 获取状态类型
const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    running: 'success',
    stopped: 'info',
    error: 'danger'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    running: '运行中',
    stopped: '已停止',
    error: '错误'
  }
  return map[status] || status
}

// 播放器事件处理
const handlePlayerLoaded = () => {
  console.log('播放器已加载')
}

const handlePlayerPlay = () => {
  console.log('播放器开始播放')
}

const handlePlayerPause = () => {
  console.log('播放器已暂停')
}

const handlePlayerError = (error: string) => {
  console.error('播放器错误:', error)
  ElMessage.error(`播放器错误: ${error}`)
}

// 生命周期
onMounted(() => {
  fetchDeviceInfo()
})

onUnmounted(() => {
  // 页面卸载时自动停止流
  if (streamInfo.value) {
    stopVideoStream(streamInfo.value.streamId).catch((e) => {
      console.error('自动停止流失败:', e)
    })
  }
})
</script>

<style scoped lang="scss">
.video-preview-container {
  padding: 20px;

  .box-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .player-section {
      margin-bottom: 20px;
    }

    .placeholder {
      min-height: 600px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f5f7fa;
      border-radius: 4px;
    }

    .device-info {
      margin-top: 20px;
    }
  }
}
</style>












