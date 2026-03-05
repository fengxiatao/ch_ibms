<template>
  <div class="bac-monitor-page">
    <!-- 筛选条件 -->
    <ContentWrap>
      <el-form :inline="true" :model="queryParams" class="-mb-15px">
        <el-form-item label="区域" prop="areaId">
          <el-select v-model="queryParams.areaId" placeholder="全部区域" clearable class="!w-160px">
            <el-option label="1层大堂" value="F1" />
            <el-option label="2层办公区" value="F2" />
            <el-option label="3层办公区" value="F3" />
            <el-option label="地下机房" value="B1" />
            <el-option label="地下二层泵房" value="B2" />
            <el-option label="屋顶机房" value="roof" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable class="!w-140px">
            <el-option label="运行中" :value="1" />
            <el-option label="停止" :value="0" />
            <el-option label="待机" :value="3" />
            <el-option label="故障" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备" prop="deviceName">
          <el-input
            v-model="queryParams.deviceName"
            placeholder="设备名称或编号"
            clearable
            class="!w-200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <!-- 设备分类标签页 -->
    <ContentWrap>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="空调机组" name="ac">
          <div v-loading="loading" class="device-grid">
            <div
              v-for="device in deviceList"
              :key="device.id"
              class="device-card"
              :class="getDeviceCardClass(device)"
            >
              <div class="card-header">
                <div class="device-name">
                  <span class="device-icon">❄️</span>
                  {{ device.deviceName }}
                </div>
                <el-tag :type="getStatusType(device)" size="small">
                  {{ getStatusLabel(device) }}
                </el-tag>
              </div>
              <div class="card-body">
                <div class="info-row">
                  <span class="info-icon">📍</span>
                  <span>{{ device.areaName || '未分配' }}</span>
                </div>
                <div class="info-row">
                  <span class="info-icon">🕐</span>
                  <span>累计运行：{{ device.runTime || '--' }}h</span>
                </div>
                <div class="info-row">
                  <span class="info-icon">🌡️</span>
                  <span>模式：{{ getModeName(device.runMode) }}</span>
                </div>
              </div>
              <div class="env-params">
                <div class="env-param">
                  <div class="param-value">{{ device.returnTemperature || '--' }}°</div>
                  <div class="param-label">室内温度</div>
                </div>
                <div class="env-param">
                  <div class="param-value">{{ device.setTemperature || '--' }}°</div>
                  <div class="param-label">设定温度</div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="新风机组" name="fresh">
          <div v-loading="loading" class="device-grid">
            <div
              v-for="device in deviceList"
              :key="device.id"
              class="device-card"
              :class="getDeviceCardClass(device)"
            >
              <div class="card-header">
                <div class="device-name">
                  <span class="device-icon">🌪️</span>
                  {{ device.deviceName }}
                </div>
                <el-tag :type="getStatusType(device)" size="small">
                  {{ getStatusLabel(device) }}
                </el-tag>
              </div>
              <div class="card-body">
                <div class="info-row">
                  <span class="info-icon">📍</span>
                  <span>{{ device.areaName || '未分配' }}</span>
                </div>
                <div class="info-row">
                  <span class="info-icon">🕐</span>
                  <span>累计运行：{{ device.runTime || '--' }}h</span>
                </div>
              </div>
              <div class="env-params">
                <div class="env-param">
                  <div class="param-value">{{ device.pressure || '--' }}</div>
                  <div class="param-label">风压Pa</div>
                </div>
                <div class="env-param">
                  <div
                    class="param-value"
                    :style="{ color: device.filterStatus === '需更换' ? '#f56c6c' : '#67c23a' }"
                  >
                    {{ device.filterStatus || '正常' }}
                  </div>
                  <div class="param-label">滤网状态</div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="送/排风机" name="fan">
          <div v-loading="loading" class="device-grid">
            <div
              v-for="device in deviceList"
              :key="device.id"
              class="device-card"
              :class="getDeviceCardClass(device)"
            >
              <div class="card-header">
                <div class="device-name">
                  <span class="device-icon">🌀</span>
                  {{ device.deviceName }}
                </div>
                <el-tag :type="getStatusType(device)" size="small">
                  {{ getStatusLabel(device) }}
                </el-tag>
              </div>
              <div class="card-body">
                <div class="info-row">
                  <span class="info-icon">📍</span>
                  <span>{{ device.areaName || '未分配' }}</span>
                </div>
                <div class="info-row">
                  <span class="info-icon">🕐</span>
                  <span>累计运行：{{ device.runTime || '--' }}h</span>
                </div>
                <div class="info-row">
                  <span class="info-icon">🌀</span>
                  <span>运行状态：{{ device.runningStatus === 1 ? '正常运行' : '停止' }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="给排水系统" name="water">
          <div v-loading="loading" class="device-grid">
            <div
              v-for="device in deviceList"
              :key="device.id"
              class="device-card water"
              :class="getDeviceCardClass(device)"
            >
              <div class="card-header">
                <div class="device-name">
                  <span class="device-icon" style="color: #13c2c2">💧</span>
                  {{ device.deviceName }}
                </div>
                <el-tag :type="getStatusType(device)" size="small">
                  {{ getStatusLabel(device) }}
                </el-tag>
              </div>
              <div class="card-body">
                <div class="info-row">
                  <span class="info-icon">📍</span>
                  <span>{{ device.areaName || '未分配' }}</span>
                </div>
              </div>
              <div class="env-params" v-if="device.deviceType === 1">
                <div class="env-param">
                  <div class="param-value water">{{ device.pressure?.toFixed(2) || '--' }}</div>
                  <div class="param-label">出口压力MPa</div>
                </div>
                <div class="env-param">
                  <div class="param-value">{{ device.runTime || '--' }}h</div>
                  <div class="param-label">累计运行</div>
                </div>
              </div>
              <div class="water-info" v-else>
                <div
                  ><strong>控制模式：</strong
                  >{{ device.controlMode === 'auto' ? '自动' : '手动' }}</div
                >
                <div><strong>累计运行：</strong>{{ device.runTime || '--' }}h</div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import * as BacApi from '@/api/iot/building/bac'

defineOptions({ name: 'BacMonitor' })

const loading = ref(false)
const activeTab = ref('ac')
const deviceList = ref<any[]>([])
const total = ref(0)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 12,
  areaId: undefined as string | undefined,
  status: undefined as number | undefined,
  deviceName: undefined as string | undefined,
  deviceType: 1 // 1-空调, 2-新风, 3-送风, 4-排风, 5-水泵等
})

const getStatusType = (device: any) => {
  if (device.status === 2) return 'danger'
  if (device.status === 0) return 'info'
  return device.runningStatus === 1 ? 'success' : 'warning'
}

const getStatusLabel = (device: any) => {
  if (device.status === 2) return '故障'
  if (device.status === 0) return '离线'
  return device.runningStatus === 1 ? '运行中' : '待机'
}

const getDeviceCardClass = (device: any) => {
  if (device.status === 2) return 'is-fault'
  if (device.runningStatus === 1) return 'is-running'
  return ''
}

const getModeName = (mode: number) => {
  const map: Record<number, string> = { 1: '制冷', 2: '制热', 3: '通风', 4: '自动' }
  return map[mode] || '--'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await BacApi.getHvacDevicePage(queryParams)
    deviceList.value = data.list
    total.value = data.total
  } catch (e) {
    console.error('获取设备列表失败', e)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.areaId = undefined
  queryParams.status = undefined
  queryParams.deviceName = undefined
  handleQuery()
}

const handleTabChange = (tab: string) => {
  const typeMap: Record<string, number> = { ac: 1, fresh: 2, fan: 3, water: 4 }
  queryParams.deviceType = typeMap[tab] || 1
  queryParams.pageNo = 1
  getList()
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.bac-monitor-page {
  box-sizing: border-box;
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
}

.device-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

@media (max-width: 1400px) {
  .device-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1200px) {
  .device-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .device-grid {
    grid-template-columns: 1fr;
  }
}

.device-card {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 16px;
  background: var(--el-bg-color);
  transition: all 0.3s;
  position: relative;
  overflow: hidden;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    border-color: var(--el-color-primary);
  }

  &.is-running {
    border-left: 4px solid var(--el-color-success);
  }

  &.is-fault {
    border-left: 4px solid var(--el-color-danger);
  }

  &.water {
    border-left: 4px solid #13c2c2;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .device-name {
    font-size: 14px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .device-icon {
    font-size: 16px;
  }

  .card-body {
    padding-top: 10px;
    border-top: 1px solid var(--el-border-color-lighter);
  }

  .info-row {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
    color: var(--el-text-color-regular);
    font-size: 13px;

    .info-icon {
      width: 24px;
      text-align: center;
      margin-right: 5px;
    }
  }

  .env-params {
    display: flex;
    gap: 15px;
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px dashed var(--el-border-color-lighter);

    .env-param {
      flex: 1;
      text-align: center;

      .param-value {
        font-size: 18px;
        font-weight: bold;
        color: var(--el-color-primary);

        &.water {
          color: #13c2c2;
        }
      }

      .param-label {
        font-size: 12px;
        color: var(--el-text-color-secondary);
        margin-top: 4px;
      }
    }
  }

  .water-info {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px dashed var(--el-border-color-lighter);
    text-align: center;
    color: var(--el-text-color-regular);
    font-size: 13px;

    div {
      margin-bottom: 8px;
    }
  }
}
</style>
