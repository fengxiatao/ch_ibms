<template>
  <div class="changhui-data-container">
    <!-- Tab切换 -->
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 实时数据Tab -->
      <el-tab-pane label="实时数据" name="realtime">
        <!-- 设备选择 -->
        <el-card class="mb-4">
          <template #header>
            <div class="card-header">
              <span>设备选择</span>
              <el-switch
                v-model="autoRefresh"
                active-text="自动刷新"
                inactive-text=""
                @change="handleAutoRefreshChange"
              />
            </div>
          </template>
          <el-select
            v-model="selectedDevice"
            placeholder="请选择设备"
            filterable
            clearable
            style="width: 300px"
            @change="handleDeviceChange"
          >
            <el-option
              v-for="device in deviceList"
              :key="device.id"
              :label="`${device.deviceName} (${device.stationCode})`"
              :value="device.stationCode"
            />
          </el-select>
          <el-button type="primary" class="ml-4" @click="refreshData" :loading="loading">
            <Icon icon="ep:refresh" class="mr-1" />刷新数据
          </el-button>
        </el-card>

        <!-- 实时数据展示 -->
        <el-row :gutter="20" v-if="selectedDevice">
          <el-col :span="6">
            <el-card class="data-card">
              <template #header>
                <div class="data-card-header">
                  <Icon icon="ep:water" class="icon" />
                  <span>水位</span>
                </div>
              </template>
              <div class="data-value">{{ formatValue(latestData.waterLevel) }}</div>
              <div class="data-unit">m</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="data-card">
              <template #header>
                <div class="data-card-header">
                  <Icon icon="ep:odometer" class="icon" />
                  <span>瞬时流量</span>
                </div>
              </template>
              <div class="data-value">{{ formatValue(latestData.instantFlow) }}</div>
              <div class="data-unit">L/s</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="data-card">
              <template #header>
                <div class="data-card-header">
                  <Icon icon="ep:position" class="icon" />
                  <span>闸位</span>
                </div>
              </template>
              <div class="data-value">{{ formatValue(latestData.gatePosition) }}</div>
              <div class="data-unit">mm</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="data-card">
              <template #header>
                <div class="data-card-header">
                  <Icon icon="ep:sunny" class="icon" />
                  <span>温度</span>
                </div>
              </template>
              <div class="data-value">{{ formatValue(latestData.temperature) }}</div>
              <div class="data-unit">°C</div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 更多指标 -->
        <el-row :gutter="20" class="mt-4" v-if="selectedDevice">
          <el-col :span="6">
            <el-card class="data-card">
              <template #header>
                <div class="data-card-header">
                  <Icon icon="ep:timer" class="icon" />
                  <span>瞬时流速</span>
                </div>
              </template>
              <div class="data-value">{{ formatValue(latestData.instantVelocity) }}</div>
              <div class="data-unit">m/s</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="data-card">
              <template #header>
                <div class="data-card-header">
                  <Icon icon="ep:data-analysis" class="icon" />
                  <span>累计流量</span>
                </div>
              </template>
              <div class="data-value">{{ formatValue(latestData.cumulativeFlow) }}</div>
              <div class="data-unit">m³</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="data-card">
              <template #header>
                <div class="data-card-header">
                  <Icon icon="ep:coin" class="icon" />
                  <span>渗透水压力</span>
                </div>
              </template>
              <div class="data-value">{{ formatValue(latestData.seepagePressure) }}</div>
              <div class="data-unit">kPa</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="data-card">
              <template #header>
                <div class="data-card-header">
                  <Icon icon="ep:scale-to-original" class="icon" />
                  <span>荷重</span>
                </div>
              </template>
              <div class="data-value">{{ formatValue(latestData.load) }}</div>
              <div class="data-unit">kN</div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 无设备选择提示 -->
        <el-empty v-if="!selectedDevice" description="请选择设备查看实时数据" />
      </el-tab-pane>

      <!-- 历史数据Tab -->
      <el-tab-pane label="历史数据" name="history">
        <HistoryDataPanel 
          :device-list="deviceList" 
          :selected-device="selectedDevice"
          @device-change="handleHistoryDeviceChange"
        />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>


<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as ChanghuiApi from '@/api/iot/changhui'
import HistoryDataPanel from './HistoryDataPanel.vue'

const activeTab = ref('realtime')
const loading = ref(false)
const selectedDevice = ref('')
const deviceList = ref<any[]>([])
const latestData = ref<any>({})
const autoRefresh = ref(false)
let refreshTimer: any = null

// 格式化数值
const formatValue = (value: any) => {
  if (value === null || value === undefined) return '--'
  if (typeof value === 'number') {
    return value.toFixed(2)
  }
  return value
}

// 获取设备列表
const getDeviceList = async () => {
  try {
    const res = await ChanghuiApi.getChanghuiDevicePage({ pageNo: 1, pageSize: 100 })
    deviceList.value = res.list || []
  } catch (e) {
    console.error('获取设备列表失败', e)
  }
}

// 设备切换
const handleDeviceChange = () => {
  if (selectedDevice.value) {
    refreshData()
  } else {
    latestData.value = {}
  }
}

// 历史数据面板设备切换
const handleHistoryDeviceChange = (stationCode: string) => {
  selectedDevice.value = stationCode
}

// 刷新数据
const refreshData = async () => {
  if (!selectedDevice.value) return
  loading.value = true
  try {
    const res = await ChanghuiApi.getChanghuiLatestData(selectedDevice.value)
    latestData.value = res || {}
  } catch (e) {
    console.error('获取数据失败', e)
  } finally {
    loading.value = false
  }
}

// 自动刷新切换
const handleAutoRefreshChange = (val: boolean) => {
  if (val) {
    refreshTimer = setInterval(() => {
      if (selectedDevice.value) {
        refreshData()
      }
    }, 5000) // 5秒刷新一次
  } else {
    if (refreshTimer) {
      clearInterval(refreshTimer)
      refreshTimer = null
    }
  }
}

onMounted(() => {
  getDeviceList()
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.changhui-data-container {
  padding: 20px;
  padding-top: calc(var(--page-top-gap) + 20px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.data-card {
  text-align: center;
}

.data-card-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.data-card-header .icon {
  font-size: 18px;
  color: #409eff;
}

.data-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  line-height: 1.5;
}

.data-unit {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.mt-4 {
  margin-top: 16px;
}

.mb-4 {
  margin-bottom: 16px;
}

.ml-4 {
  margin-left: 16px;
}

.mr-1 {
  margin-right: 4px;
}
</style>
