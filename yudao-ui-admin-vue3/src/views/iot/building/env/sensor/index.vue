<template>
  <div class="env-sensor-page">
    <!-- 筛选条件 -->
    <ContentWrap>
      <el-form :inline="true" :model="queryParams" class="-mb-15px">
        <el-form-item label="传感器类型" prop="sensorType">
          <el-select
            v-model="queryParams.sensorType"
            placeholder="全部类型"
            clearable
            class="!w-160px"
          >
            <el-option label="温湿度传感器" value="weather" />
            <el-option label="空气质量传感器" value="air" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable class="!w-140px">
            <el-option label="在线" :value="1" />
            <el-option label="离线" :value="0" />
            <el-option label="告警" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备" prop="sensorName">
          <el-input
            v-model="queryParams.sensorName"
            placeholder="请输入设备名称"
            clearable
            class="!w-200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" /> 筛选
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="环境传感器" name="sensor" />
      </el-tabs>

      <!-- 传感器卡片网格 -->
      <div v-loading="loading" class="sensor-grid">
        <div
          v-for="sensor in sensorList"
          :key="sensor.id"
          class="sensor-card"
          @click="openDetail(sensor)"
        >
          <div class="card-header">
            <div class="sensor-name">
              <span class="sensor-icon">{{ sensor.sensorType === 'weather' ? '🌡️' : '🌫️' }}</span>
              {{ sensor.sensorName }}
            </div>
            <el-tag :type="getStatusType(sensor.status)" size="small">
              {{ getStatusLabel(sensor.status) }}
            </el-tag>
          </div>
          <div class="card-body">
            <div class="info-row">
              <span class="info-icon">📍</span>
              <span>位置：{{ sensor.areaName || '未分配' }}</span>
            </div>
            <template v-if="sensor.sensorType === 'weather'">
              <div class="info-row">
                <span class="info-icon">🌡️</span>
                <span>温度 {{ sensor.temperature || '--' }}°C</span>
              </div>
              <div class="info-row">
                <span class="info-icon">💧</span>
                <span :style="{ color: sensor.status === 2 ? '#f56c6c' : '#67c23a' }">
                  湿度 {{ sensor.humidity || '--' }}%
                </span>
              </div>
            </template>
            <template v-else>
              <div class="info-row">
                <span class="info-icon">🌫️</span>
                <span>PM2.5 {{ sensor.pm25 || '--' }}μg/m³</span>
              </div>
              <div class="info-row">
                <span class="info-icon">🌫️</span>
                <span>PM10 {{ sensor.pm10 || '--' }}μg/m³</span>
              </div>
            </template>
          </div>
        </div>
      </div>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>

    <!-- 传感器详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="currentSensor?.sensorName" width="900px">
      <div v-if="currentSensor" class="sensor-detail">
        <div class="detail-sidebar">
          <div class="sensor-basic-info">
            <div class="sensor-large-icon">{{
              currentSensor.sensorType === 'weather' ? '🌡️' : '🌫️'
            }}</div>
            <div class="sensor-name-large">{{ currentSensor.sensorName }}</div>
            <el-tag :type="getStatusType(currentSensor.status)" class="sensor-status-tag">
              ● {{ getStatusLabel(currentSensor.status) }}
            </el-tag>
          </div>
          <div class="info-list">
            <div class="info-item">
              <div class="info-label">设备ID</div>
              <div class="info-value">{{ currentSensor.sensorCode }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">区域位置</div>
              <div class="info-value">{{ currentSensor.areaName }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">设备类型</div>
              <div class="info-value">{{
                currentSensor.sensorType === 'weather' ? '温湿度监测' : '空气质量监测'
              }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">安装时间</div>
              <div class="info-value">2026-01-15</div>
            </div>
            <div class="info-item">
              <div class="info-label">固件版本</div>
              <div class="info-value">v2.1.3</div>
            </div>
          </div>
        </div>
        <div class="detail-content">
          <el-tabs v-model="detailTab">
            <el-tab-pane label="操作日志" name="log">
              <el-table :data="logList" size="small">
                <el-table-column label="时间" prop="time" width="160" />
                <el-table-column label="操作人" prop="operator" width="100" />
                <el-table-column label="操作内容" prop="content" />
                <el-table-column label="类型" prop="type" width="80">
                  <template #default="{ row }">
                    <el-tag :type="row.type === 'info' ? 'primary' : 'warning'" size="small">
                      {{ row.type === 'info' ? '信息' : '配置' }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="告警信息" name="alarm">
              <el-table :data="alarmList" size="small">
                <el-table-column label="告警时间" prop="time" width="160" />
                <el-table-column label="告警级别" prop="level" width="100">
                  <template #default="{ row }">
                    <span :style="{ color: row.level === 'high' ? '#f56c6c' : '#e6a23c' }">
                      {{ row.level === 'high' ? '🔴 高级' : '⚠️ 中级' }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="告警内容" prop="content" />
                <el-table-column label="状态" prop="status" width="80">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 'resolved' ? 'success' : 'warning'" size="small">
                      {{ row.status === 'resolved' ? '已恢复' : '持续中' }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="报表数据" name="report">
              <el-table :data="reportList" size="small">
                <el-table-column label="设备名称" prop="sensorName" width="150" />
                <el-table-column label="采集时间" prop="collectTime" width="160" />
                <el-table-column label="参数名称" prop="paramName" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.paramType === 'temp' ? 'warning' : 'primary'" size="small">
                      {{ row.paramName }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="参数值" prop="paramValue" width="100">
                  <template #default="{ row }">
                    <span style="font-family: monospace; font-weight: 600">{{
                      row.paramValue
                    }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="80">
                  <template #default>
                    <el-button link type="primary" size="small">📥 下载</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import * as EnvApi from '@/api/iot/building/env'

defineOptions({ name: 'EnvSensor' })

const loading = ref(false)
const activeTab = ref('sensor')
const sensorList = ref<any[]>([])
const total = ref(0)
const detailVisible = ref(false)
const currentSensor = ref<any>(null)
const detailTab = ref('log')

const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  sensorType: undefined as string | undefined,
  status: undefined as number | undefined,
  sensorName: undefined as string | undefined
})

// 模拟日志数据
const logList = ref([
  { time: '2026-01-30 14:20:15', operator: '管理员', content: '远程重启设备', type: 'info' },
  { time: '2026-01-30 09:15:33', operator: '系统', content: '自动校准传感器', type: 'info' },
  {
    time: '2026-01-29 16:45:22',
    operator: '运维人员A',
    content: '修改采样频率为5分钟',
    type: 'warning'
  }
])

const alarmList = ref([
  {
    time: '2026-01-30 12:20:15',
    level: 'medium',
    content: '湿度超过阈值（当前65%，阈值60%）',
    status: 'resolved'
  }
])

const reportList = ref([
  {
    sensorName: '温湿度传感器001',
    collectTime: '2026-01-30 14:30:00',
    paramName: '温度',
    paramType: 'temp',
    paramValue: '26.5°C'
  },
  {
    sensorName: '温湿度传感器001',
    collectTime: '2026-01-30 14:30:00',
    paramName: '湿度',
    paramType: 'hum',
    paramValue: '48.2%'
  },
  {
    sensorName: '温湿度传感器001',
    collectTime: '2026-01-30 14:25:00',
    paramName: '温度',
    paramType: 'temp',
    paramValue: '26.3°C'
  },
  {
    sensorName: '温湿度传感器001',
    collectTime: '2026-01-30 14:25:00',
    paramName: '湿度',
    paramType: 'hum',
    paramValue: '47.8%'
  }
])

const getStatusType = (status: number) => {
  const map: Record<number, string> = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const getStatusLabel = (status: number) => {
  const map: Record<number, string> = { 0: '离线', 1: '在线', 2: '告警' }
  return map[status] || '未知'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await EnvApi.getEnvSensorPage(queryParams)
    sensorList.value = data.list
    total.value = data.total
  } catch (e) {
    // 使用模拟数据
    sensorList.value = Array.from({ length: 15 }, (_, i) => ({
      id: i + 1,
      sensorCode: `WS-${String(i + 1).padStart(3, '0')}`,
      sensorName: `温湿度传感器${String(i + 1).padStart(3, '0')}`,
      areaName: i < 8 ? 'A区一层' : 'A区二层',
      sensorType: i < 10 ? 'weather' : 'air',
      temperature: 20 + Math.floor(Math.random() * 8),
      humidity: 30 + Math.floor(Math.random() * 40),
      pm25: ['35', '28', '85', '42', '19'][Math.floor(Math.random() * 5)],
      pm10: ['50', '45', '120', '60', '35'][Math.floor(Math.random() * 5)],
      status: i === 8 ? 2 : 1
    }))
    total.value = 25
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.sensorType = undefined
  queryParams.status = undefined
  queryParams.sensorName = undefined
  handleQuery()
}

const openDetail = (sensor: any) => {
  currentSensor.value = sensor
  detailTab.value = 'log'
  detailVisible.value = true
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.env-sensor-page {
  box-sizing: border-box;
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
}

.sensor-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

@media (max-width: 1400px) {
  .sensor-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 1200px) {
  .sensor-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .sensor-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.sensor-card {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  padding: 15px;
  background: var(--el-bg-color);
  transition: all 0.3s;
  cursor: pointer;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
    border-color: var(--el-color-primary);
    transform: translateY(-2px);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .sensor-name {
    font-size: 14px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 8px;
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
}

.sensor-detail {
  display: flex;
  height: 500px;

  .detail-sidebar {
    width: 240px;
    background: var(--el-fill-color-light);
    border-right: 1px solid var(--el-border-color-lighter);
    padding: 20px;

    .sensor-basic-info {
      text-align: center;
      margin-bottom: 20px;
      padding-bottom: 20px;
      border-bottom: 1px solid var(--el-border-color-lighter);

      .sensor-large-icon {
        font-size: 48px;
        margin-bottom: 12px;
      }

      .sensor-name-large {
        font-size: 16px;
        font-weight: 600;
      }

      .sensor-status-tag {
        margin-top: 8px;
      }
    }

    .info-list {
      .info-item {
        margin-bottom: 12px;
        font-size: 13px;

        .info-label {
          color: var(--el-text-color-secondary);
          margin-bottom: 4px;
        }

        .info-value {
          color: var(--el-text-color-primary);
          font-weight: 500;
          word-break: break-all;
        }
      }
    }
  }

  .detail-content {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
  }
}
</style>
