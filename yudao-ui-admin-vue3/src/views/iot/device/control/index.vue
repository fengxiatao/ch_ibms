<template>
  <div class="device-control">
    <ContentWrap>
      <!-- 设备选择 -->
      <el-form :inline="true" :model="controlForm" class="control-form">
        <el-form-item label="选择设备" prop="deviceId">
          <el-select 
            v-model="controlForm.deviceId" 
            placeholder="请选择设备" 
            @change="handleDeviceChange"
          >
            <el-option label="测试摄像头-192.168.1.202" :value="1" />
            <!-- TODO: 动态从接口获取设备列表 -->
          </el-select>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <!-- 控制面板 -->
    <el-row :gutter="20" v-if="controlForm.deviceId">
      <!-- 快速操作 -->
      <el-col :span="12">
        <ContentWrap title="快速操作">
          <div class="quick-actions">
            <el-button 
              type="primary" 
              :icon="Camera" 
              :loading="snapshotLoading"
              @click="handleSnapshot"
            >
              抓拍
            </el-button>
            <el-button 
              type="success" 
              :icon="VideoCamera" 
              :loading="recordLoading"
              @click="handleStartRecording"
              v-if="!isRecording"
            >
              开始录像
            </el-button>
            <el-button 
              type="danger" 
              :icon="VideoPause" 
              :loading="recordLoading"
              @click="handleStopRecording"
              v-else
            >
              停止录像
            </el-button>
          </div>

          <!-- 抓拍结果 -->
          <div class="snapshot-result" v-if="snapshotUri">
            <el-divider content-position="left">抓拍结果</el-divider>
            <el-image 
              :src="snapshotUri" 
              fit="contain" 
              style="width: 100%; max-height: 400px;"
              :preview-src-list="[snapshotUri]"
            />
            <div class="snapshot-info">
              <span>抓拍时间：{{ snapshotTime }}</span>
              <el-button link type="primary" @click="downloadSnapshot">下载</el-button>
            </div>
          </div>
        </ContentWrap>
      </el-col>

      <!-- 云台控制 -->
      <el-col :span="12">
        <ContentWrap title="云台控制">
          <div class="ptz-control">
            <div class="ptz-buttons">
              <div class="ptz-row">
                <div class="ptz-spacer"></div>
                <el-button 
                  class="ptz-btn" 
                  :icon="ArrowUp"
                  @click="handlePtzControl('up')"
                >
                  上
                </el-button>
                <div class="ptz-spacer"></div>
              </div>
              <div class="ptz-row">
                <el-button 
                  class="ptz-btn" 
                  :icon="ArrowLeft"
                  @click="handlePtzControl('left')"
                >
                  左
                </el-button>
                <el-button 
                  class="ptz-btn ptz-center" 
                  :icon="Aim"
                >
                  中心
                </el-button>
                <el-button 
                  class="ptz-btn" 
                  :icon="ArrowRight"
                  @click="handlePtzControl('right')"
                >
                  右
                </el-button>
              </div>
              <div class="ptz-row">
                <div class="ptz-spacer"></div>
                <el-button 
                  class="ptz-btn" 
                  :icon="ArrowDown"
                  @click="handlePtzControl('down')"
                >
                  下
                </el-button>
                <div class="ptz-spacer"></div>
              </div>
            </div>

            <div class="ptz-zoom">
              <el-button 
                class="zoom-btn" 
                :icon="ZoomIn"
                @click="handlePtzControl('zoomIn')"
              >
                放大
              </el-button>
              <el-button 
                class="zoom-btn" 
                :icon="ZoomOut"
                @click="handlePtzControl('zoomOut')"
              >
                缩小
              </el-button>
            </div>

            <div class="ptz-speed">
              <span>速度：</span>
              <el-slider v-model="ptzSpeed" :min="0.1" :max="1" :step="0.1" style="width: 200px;" />
              <span>{{ ptzSpeed }}</span>
            </div>
          </div>
        </ContentWrap>
      </el-col>
    </el-row>

    <!-- 服务调用日志 -->
    <ContentWrap title="服务调用日志" v-if="controlForm.deviceId">
      <el-table :data="serviceLogList" stripe style="width: 100%">
        <el-table-column prop="serviceIdentifier" label="服务" width="120" />
        <el-table-column prop="requestTime" label="请求时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.requestTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="statusCode" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.statusCode === 200" type="success">成功</el-tag>
            <el-tag v-else-if="row.statusCode" type="danger">失败({{ row.statusCode }})</el-tag>
            <el-tag v-else type="info">处理中</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="responseMessage" label="响应消息" min-width="200" />
        <el-table-column prop="executionTime" label="耗时" width="100">
          <template #default="{ row }">
            <span v-if="row.executionTime">{{ row.executionTime }}ms</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewLog(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getServiceLogList"
      />
    </ContentWrap>

    <!-- 日志详情对话框 -->
    <el-dialog v-model="logDialogVisible" title="服务调用详情" width="700px">
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="服务名称">{{ currentLog.serviceName }}</el-descriptions-item>
        <el-descriptions-item label="服务标识">{{ currentLog.serviceIdentifier }}</el-descriptions-item>
        <el-descriptions-item label="请求ID" :span="2">{{ currentLog.requestId }}</el-descriptions-item>
        <el-descriptions-item label="请求时间" :span="2">
          {{ formatDate(currentLog.requestTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre style="max-height: 200px; overflow: auto;">{{ formatJson(currentLog.requestParams) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="响应状态">
          <el-tag v-if="currentLog.statusCode === 200" type="success">成功</el-tag>
          <el-tag v-else type="danger">失败({{ currentLog.statusCode }})</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="响应时间">
          {{ currentLog.responseTime ? formatDate(currentLog.responseTime) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="响应消息" :span="2">{{ currentLog.responseMessage }}</el-descriptions-item>
        <el-descriptions-item label="响应数据" :span="2">
          <pre style="max-height: 300px; overflow: auto;">{{ formatJson(currentLog.responseData) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="执行耗时">
          {{ currentLog.executionTime ? currentLog.executionTime + 'ms' : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog.operatorName }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Camera, VideoCamera, VideoPause, 
  ArrowUp, ArrowDown, ArrowLeft, ArrowRight, 
  ZoomIn, ZoomOut, Aim 
} from '@element-plus/icons-vue'
import * as DeviceServiceApi from '@/api/iot/device/service'
import { formatDate } from '@/utils/formatTime'

// 控制表单
const controlForm = reactive({
  deviceId: null as number | null
})

// 快速操作状态
const snapshotLoading = ref(false)
const recordLoading = ref(false)
const isRecording = ref(false)
const snapshotUri = ref('')
const snapshotTime = ref('')

// 云台控制
const ptzSpeed = ref(0.5)

// 服务日志
const serviceLogList = ref<DeviceServiceApi.DeviceServiceLogVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  deviceId: undefined as number | undefined
})
const logDialogVisible = ref(false)
const currentLog = ref<DeviceServiceApi.DeviceServiceLogVO | null>(null)

// 设备切换
const handleDeviceChange = () => {
  queryParams.deviceId = controlForm.deviceId || undefined
  getServiceLogList()
}

// 抓拍
const handleSnapshot = async () => {
  if (!controlForm.deviceId) {
    ElMessage.warning('请先选择设备')
    return
  }

  try {
    snapshotLoading.value = true
    const res = await DeviceServiceApi.invokeDeviceService({
      deviceId: controlForm.deviceId,
      serviceIdentifier: 'Snapshot',
      serviceName: '抓拍'
    })
    
    ElMessage.success('抓拍请求已发送')
    
    // 轮询获取结果
    setTimeout(() => {
      getServiceLogList()
      // TODO: 实际应该轮询查询结果，获取快照URI
      snapshotUri.value = 'http://192.168.1.202/snapshot.jpg'
      snapshotTime.value = new Date().toLocaleString()
    }, 1000)
    
  } catch (error) {
    ElMessage.error('抓拍失败')
  } finally {
    snapshotLoading.value = false
  }
}

// 开始录像
const handleStartRecording = async () => {
  if (!controlForm.deviceId) {
    ElMessage.warning('请先选择设备')
    return
  }

  try {
    recordLoading.value = true
    await DeviceServiceApi.invokeDeviceService({
      deviceId: controlForm.deviceId,
      serviceIdentifier: 'StartRecording',
      serviceName: '开始录像'
    })
    
    ElMessage.success('录像已开始')
    isRecording.value = true
    getServiceLogList()
    
  } catch (error) {
    ElMessage.error('开始录像失败')
  } finally {
    recordLoading.value = false
  }
}

// 停止录像
const handleStopRecording = async () => {
  if (!controlForm.deviceId) {
    ElMessage.warning('请先选择设备')
    return
  }

  try {
    recordLoading.value = true
    await DeviceServiceApi.invokeDeviceService({
      deviceId: controlForm.deviceId,
      serviceIdentifier: 'StopRecording',
      serviceName: '停止录像'
    })
    
    ElMessage.success('录像已停止')
    isRecording.value = false
    getServiceLogList()
    
  } catch (error) {
    ElMessage.error('停止录像失败')
  } finally {
    recordLoading.value = false
  }
}

// 云台控制
const handlePtzControl = async (direction: string) => {
  if (!controlForm.deviceId) {
    ElMessage.warning('请先选择设备')
    return
  }

  try {
    await DeviceServiceApi.invokeDeviceService({
      deviceId: controlForm.deviceId,
      serviceIdentifier: 'PtzControl',
      serviceName: '云台控制',
      serviceParams: {
        direction,
        speed: ptzSpeed.value
      }
    })
    
    ElMessage.success(`云台${direction}操作已执行`)
    getServiceLogList()
    
  } catch (error) {
    ElMessage.error('云台控制失败')
  }
}

// 获取服务日志列表
const getServiceLogList = async () => {
  try {
    const res = await DeviceServiceApi.getDeviceServiceLogPage(queryParams)
    serviceLogList.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('获取服务日志失败', error)
  }
}

// 查看日志详情
const handleViewLog = async (row: DeviceServiceApi.DeviceServiceLogVO) => {
  try {
    currentLog.value = await DeviceServiceApi.getDeviceServiceLog(row.id)
    logDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取日志详情失败')
  }
}

// 下载抓拍
const downloadSnapshot = () => {
  if (snapshotUri.value) {
    window.open(snapshotUri.value, '_blank')
  }
}

// 格式化JSON
const formatJson = (jsonStr: string) => {
  if (!jsonStr) return '-'
  try {
    return JSON.stringify(JSON.parse(jsonStr), null, 2)
  } catch {
    return jsonStr
  }
}

onMounted(() => {
  // 初始化
})
</script>

<style lang="scss" scoped>
.device-control {
  padding: 20px;

  .control-form {
    margin-bottom: 20px;
  }

  .quick-actions {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
  }

  .snapshot-result {
    margin-top: 20px;

    .snapshot-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 10px;
      padding: 10px;
      background-color: #f5f7fa;
      border-radius: 4px;
    }
  }

  .ptz-control {
    .ptz-buttons {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 10px;
      margin-bottom: 20px;

      .ptz-row {
        display: flex;
        gap: 10px;

        .ptz-spacer {
          width: 80px;
        }

        .ptz-btn {
          width: 80px;
          height: 60px;
        }

        .ptz-center {
          background-color: #409eff;
          color: white;
        }
      }
    }

    .ptz-zoom {
      display: flex;
      justify-content: center;
      gap: 20px;
      margin-bottom: 20px;

      .zoom-btn {
        width: 120px;
      }
    }

    .ptz-speed {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
    }
  }
}
</style>












