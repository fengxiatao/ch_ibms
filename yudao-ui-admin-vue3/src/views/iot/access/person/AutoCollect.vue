<template>
  <div class="auto-collect">
    <div class="collect-header">
      <div class="header-title">
        <Icon icon="ep:camera" class="mr-8px" />
        <span>自动采集</span>
      </div>
      <div class="header-desc">从门禁设备自动采集人员信息和凭证数据</div>
    </div>

    <div class="collect-content">
      <!-- 设备选择 -->
      <div class="device-section">
        <div class="section-title">选择采集设备</div>
        <div class="device-list">
          <el-checkbox-group v-model="selectedDevices">
            <div v-for="device in deviceList" :key="device.id" class="device-item">
              <el-checkbox :value="device.id">
                <div class="device-info">
                  <Icon icon="ep:monitor" class="mr-8px" />
                  <span class="device-name">{{ device.deviceName }}</span>
                  <el-tag size="small" :type="device.state === 1 ? 'success' : 'danger'" class="ml-8px">
                    {{ device.state === 1 ? '在线' : '离线' }}
                  </el-tag>
                </div>
              </el-checkbox>
            </div>
          </el-checkbox-group>
          <div v-if="!deviceList.length" class="no-data">暂无可用设备</div>
        </div>
      </div>

      <!-- 采集选项 -->
      <div class="options-section">
        <div class="section-title">采集选项</div>
        <div class="options-list">
          <el-checkbox v-model="collectOptions.userInfo">采集用户基本信息</el-checkbox>
          <el-checkbox v-model="collectOptions.card">采集卡片信息</el-checkbox>
          <el-checkbox v-model="collectOptions.fingerprint">采集指纹数据</el-checkbox>
          <el-checkbox v-model="collectOptions.face">采集人脸数据</el-checkbox>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-section">
        <el-button type="primary" :loading="collecting" :disabled="!selectedDevices.length" @click="handleStartCollect">
          <Icon icon="ep:download" class="mr-4px" />开始采集
        </el-button>
        <el-button :disabled="!collecting" @click="handleStopCollect">
          <Icon icon="ep:video-pause" class="mr-4px" />停止采集
        </el-button>
      </div>

      <!-- 采集进度 -->
      <div class="progress-section" v-if="collectProgress.total > 0">
        <div class="section-title">采集进度</div>
        <div class="progress-info">
          <el-progress :percentage="progressPercent" :status="progressStatus" :stroke-width="12" />
          <div class="progress-detail">
            已采集 {{ collectProgress.current }} / {{ collectProgress.total }} 条数据
          </div>
        </div>
      </div>

      <!-- 采集结果 -->
      <div class="result-section" v-if="collectResults.length">
        <div class="section-title">采集结果</div>
        <el-table :data="collectResults" size="small" max-height="300">
          <el-table-column label="设备" prop="deviceName" width="150" />
          <el-table-column label="人员编号" prop="personCode" width="120" />
          <el-table-column label="姓名" prop="personName" width="100" />
          <el-table-column label="采集类型" prop="collectType" width="100" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="row.success ? 'success' : 'danger'">
                {{ row.success ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" show-overflow-tooltip />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { AccessDeviceApi, type AccessDeviceVO } from '@/api/iot/access'

const deviceList = ref<AccessDeviceVO[]>([])
const selectedDevices = ref<number[]>([])
const collecting = ref(false)

const collectOptions = reactive({
  userInfo: true,
  card: true,
  fingerprint: false,
  face: false
})

const collectProgress = reactive({
  current: 0,
  total: 0
})

const collectResults = ref<any[]>([])

const progressPercent = computed(() => {
  if (collectProgress.total === 0) return 0
  return Math.round((collectProgress.current / collectProgress.total) * 100)
})

const progressStatus = computed(() => {
  if (progressPercent.value === 100) return 'success'
  return undefined
})

const loadDevices = async () => {
  try {
    deviceList.value = await AccessDeviceApi.getDeviceList()
  } catch (error) {
    console.error('加载设备列表失败:', error)
  }
}

const handleStartCollect = () => {
  if (!selectedDevices.value.length) {
    ElMessage.warning('请选择要采集的设备')
    return
  }
  // 运行态禁止“模拟采集进度/结果”。该功能需要后端提供真实采集任务（Biz -> MQ -> newgateway -> 回调落库/WS）。
  ElMessage.warning('自动采集尚未接入真实设备采集链路（已移除模拟采集）。请对接后端采集任务后启用')
}

const handleStopCollect = () => {
  collecting.value = false
  ElMessage.info('采集已停止')
}

onMounted(() => {
  loadDevices()
})
</script>

<style scoped lang="scss">
.auto-collect {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #1e1e2d;
}

.collect-header {
  padding: 16px 20px;
  background: #252532;
  border-bottom: 1px solid #2d2d3a;

  .header-title {
    display: flex;
    align-items: center;
    font-size: 15px;
    font-weight: 500;
    color: #e0e0e0;
    margin-bottom: 4px;
  }

  .header-desc {
    font-size: 12px;
    color: #8c8c9a;
    margin-left: 24px;
  }
}

.collect-content {
  flex: 1;
  overflow: auto;
  padding: 20px;
}

.section-title {
  font-size: 13px;
  font-weight: 500;
  color: #e0e0e0;
  margin-bottom: 12px;
  padding-left: 8px;
  border-left: 3px solid #409eff;
}

.device-section, .options-section, .progress-section, .result-section {
  background: #252532;
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 16px;
}

.device-list {
  .device-item {
    padding: 8px 0;
    border-bottom: 1px solid #2d2d3a;

    &:last-child {
      border-bottom: none;
    }

    .device-info {
      display: flex;
      align-items: center;
      color: #c4c4c4;
    }
  }

  .no-data {
    text-align: center;
    padding: 20px;
    color: #6c6c7a;
    font-size: 13px;
  }

  :deep(.el-checkbox__label) {
    color: #c4c4c4;
  }
}

.options-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;

  :deep(.el-checkbox__label) {
    color: #c4c4c4;
  }
}

.action-section {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;

  .el-button {
    background: #2d2d3a;
    border-color: #3d3d4a;
    color: #c4c4c4;

    &:hover {
      background: #3d3d4a;
      color: #e0e0e0;
    }

    &.el-button--primary {
      background: #409eff;
      border-color: #409eff;
      color: #fff;
    }
  }
}

.progress-info {
  .progress-detail {
    margin-top: 8px;
    font-size: 12px;
    color: #8c8c9a;
    text-align: center;
  }
}

.result-section {
  :deep(.el-table) {
    background: transparent;
    --el-table-bg-color: transparent;
    --el-table-tr-bg-color: transparent;
    --el-table-header-bg-color: #1e1e2d;
    --el-table-row-hover-bg-color: #2d2d3a;
    --el-table-border-color: #2d2d3a;
    --el-table-text-color: #c4c4c4;
    --el-table-header-text-color: #e0e0e0;
  }
}
</style>
