<template>
  <div class="video-playback-container">
    <el-card class="box-card">
      <!-- 页头 -->
      <template #header>
        <div class="card-header">
          <span>录像回放 - {{ deviceName }}</span>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryForm" :inline="true" label-width="100px">
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="queryForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="queryForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="startPlayback">
            开始回放
          </el-button>
          <el-button v-if="streamInfo" type="danger" @click="stopPlayback">停止回放</el-button>
        </el-form-item>
      </el-form>

      <!-- 视频播放器 -->
      <div v-if="false" class="player-section"></div>

      <!-- 占位符 -->
      <div v-else class="placeholder">
        <!-- <el-empty description="请选择时间段后点击"开始回放"查看录像" /> -->
        <el-empty description="请选择时间段后点击『开始回放』查看录像" />
      </div>

      <!-- 回放信息 -->
      <el-divider />
      <div class="playback-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="设备ID">{{ deviceId }}</el-descriptions-item>
          <el-descriptions-item label="设备名称">{{ deviceName }}</el-descriptions-item>
          <el-descriptions-item label="回放开始时间">
            {{ queryForm.startTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="回放结束时间">
            {{ queryForm.endTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="流ID" :span="2">
            {{ streamInfo?.streamId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="HLS地址" :span="2">
            <el-text type="success" size="small">{{ streamInfo?.hlsUrl || '-' }}</el-text>
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
 
import { getPlaybackStream, stopVideoStream, type VideoStreamRespVO } from '@/api/iot/video/stream'
import { getDevice } from '@/api/iot/device'

const route = useRoute()
const deviceId = ref<number>(Number(route.query.deviceId))
const deviceName = ref<string>('')
const loading = ref(false)
const streamInfo = ref<VideoStreamRespVO>()
const playerRef = ref()

const queryForm = ref({
  startTime: '',
  endTime: ''
})

// 禁用未来日期
const disabledDate = (time: Date) => {
  return time.getTime() > Date.now()
}

// 获取设备信息
const fetchDeviceInfo = async () => {
  try {
    const device = await getDevice(deviceId.value)
    deviceName.value = device.deviceName || ''
  } catch (error) {
    console.error('获取设备信息失败:', error)
  }
}

// 开始录像回放
const startPlayback = async () => {
  if (!queryForm.value.startTime || !queryForm.value.endTime) {
    ElMessage.warning('请选择回放时间段')
    return
  }

  if (queryForm.value.startTime >= queryForm.value.endTime) {
    ElMessage.warning('结束时间必须大于开始时间')
    return
  }

  loading.value = true
  try {
    streamInfo.value = await getPlaybackStream(
      deviceId.value,
      queryForm.value.startTime,
      queryForm.value.endTime
    )
    ElMessage.success('录像回放流创建成功')
  } catch (error) {
    console.error('创建录像回放流失败:', error)
    ElMessage.error('创建录像回放流失败，请检查时间段内是否有录像')
  } finally {
    loading.value = false
  }
}

// 停止录像回放
const stopPlayback = async () => {
  if (!streamInfo.value) return

  try {
    await ElMessageBox.confirm('确定要停止录像回放吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await stopVideoStream(streamInfo.value.streamId)
    streamInfo.value = undefined
    ElMessage.success('录像回放已停止')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('停止录像回放失败:', error)
      ElMessage.error('停止录像回放失败')
    }
  }
}

// 播放器事件处理
const handlePlayerLoaded = () => {
  console.log('录像播放器已加载')
}

const handlePlayerError = (error: string) => {
  console.error('录像播放器错误:', error)
  ElMessage.error(`录像播放器错误: ${error}`)
}

// 生命周期
onMounted(() => {
  fetchDeviceInfo()
  
  // 设置默认时间（当前时间往前推1小时）
  const now = new Date()
  const oneHourAgo = new Date(now.getTime() - 60 * 60 * 1000)
  queryForm.value.endTime = now.toISOString().slice(0, 19)
  queryForm.value.startTime = oneHourAgo.toISOString().slice(0, 19)
})

onUnmounted(() => {
  // 页面卸载时自动停止流
  if (streamInfo.value) {
    stopVideoStream(streamInfo.value.streamId).catch((e) => {
      console.error('自动停止录像回放失败:', e)
    })
  }
})
</script>

<style scoped lang="scss">
.video-playback-container {
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

    .playback-info {
      margin-top: 20px;
    }
  }
}
</style>












