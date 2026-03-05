<template>
  <div class="env-alarm-page">
    <div class="alarm-layout">
      <!-- 左侧设备分类树 -->
      <div class="tree-panel">
        <div class="tree-title">🗂️ 设备分类</div>
        <el-input v-model="treeSearch" placeholder="搜索分类..." class="tree-search" />
        <div
          class="tree-node"
          :class="{ 'is-active': selectedCategory === 'all' }"
          @click="selectCategory('all')"
        >
          <span>📁</span>
          <span>全部设备</span>
          <span class="device-count">{{ statistics.totalCount || 45 }}</span>
        </div>
        <div
          class="tree-node"
          :class="{ 'is-active': selectedCategory === 'weather' }"
          @click="selectCategory('weather')"
          style="padding-left: 30px"
        >
          <span>🌡️</span>
          <span>温湿度传感器</span>
          <span class="device-count">{{ statistics.weatherCount || 25 }}</span>
        </div>
        <div
          class="tree-node"
          :class="{ 'is-active': selectedCategory === 'air' }"
          @click="selectCategory('air')"
          style="padding-left: 30px"
        >
          <span>🌫️</span>
          <span>空气质量传感器</span>
          <span class="device-count">{{ statistics.airCount || 20 }}</span>
        </div>
      </div>

      <!-- 右侧主内容 -->
      <div class="main-content">
        <!-- 统计卡片 -->
        <el-row :gutter="20" class="mb-20px">
          <el-col :span="8">
            <el-card shadow="hover" class="stat-card success">
              <div class="stat-label">正常设备</div>
              <div class="stat-value">{{ statistics.normalCount || 42 }}</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" class="stat-card warning">
              <div class="stat-label">告警设备</div>
              <div class="stat-value">{{ statistics.alarmCount || 3 }}</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" class="stat-card danger">
              <div class="stat-label">离线设备</div>
              <div class="stat-value">{{ statistics.offlineCount || 0 }}</div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 操作栏 -->
        <div class="action-bar">
          <div class="filter-tags">
            <el-tag
              v-for="tag in filterTags"
              :key="tag.value"
              :type="activeTag === tag.value ? '' : 'info'"
              :effect="activeTag === tag.value ? 'dark' : 'plain'"
              class="filter-tag"
              @click="activeTag = tag.value"
            >
              {{ tag.label }}
            </el-tag>
          </div>
          <div class="action-buttons">
            <el-button @click="refreshData">
              <Icon icon="ep:refresh" class="mr-5px" /> 刷新数据
            </el-button>
            <el-button type="primary" @click="exportExcel">
              <Icon icon="ep:download" class="mr-5px" /> 导出Excel
            </el-button>
          </div>
        </div>

        <!-- 数据表格 -->
        <el-table v-loading="loading" :data="list" stripe>
          <el-table-column label="区域位置" prop="areaName" width="120">
            <template #default="{ row }"> 📍 {{ row.areaName }} </template>
          </el-table-column>
          <el-table-column label="设备信息" prop="sensorName" min-width="180">
            <template #default="{ row }">
              <div style="font-weight: 600">{{ row.sensorName }}</div>
              <div style="color: #909399; font-size: 12px">
                {{ row.sensorType === 'weather' ? '温湿度监测' : '空气质量监测' }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="设备ID" prop="sensorCode" width="120">
            <template #default="{ row }">
              <span style="font-family: monospace; color: #909399">{{ row.sensorCode }}</span>
            </template>
          </el-table-column>
          <el-table-column label="设备状态" prop="status" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="实时数值" prop="value" width="150">
            <template #default="{ row }">
              <span v-if="row.status === 0" style="color: #909399">--</span>
              <span v-else style="color: #409eff; font-weight: 600">
                {{
                  row.sensorType === 'weather'
                    ? `${row.temperature}°C / ${row.humidity}%`
                    : `PM2.5: ${row.pm25}μg/m³`
                }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="最后更新时间" prop="lastUpdateTime" width="180">
            <template #default="{ row }">
              {{ formatDate(row.lastUpdateTime) }}
            </template>
          </el-table-column>
        </el-table>

        <Pagination
          :total="total"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'EnvAlarm' })

const message = useMessage()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const treeSearch = ref('')
const selectedCategory = ref('all')
const activeTag = ref('all')

const statistics = ref({
  totalCount: 45,
  weatherCount: 25,
  airCount: 20,
  normalCount: 42,
  alarmCount: 3,
  offlineCount: 0
})

const filterTags = [
  { label: '全部', value: 'all' },
  { label: '在线', value: 'online' },
  { label: '告警', value: 'alarm' },
  { label: '离线', value: 'offline' }
]

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  category: 'all'
})

const getStatusType = (status: number) => {
  const map: Record<number, string> = { 0: 'info', 1: 'success', 2: 'warning' }
  return map[status] || 'info'
}

const getStatusLabel = (status: number) => {
  const map: Record<number, string> = { 0: '离线', 1: '在线', 2: '告警' }
  return map[status] || '未知'
}

const getList = async () => {
  loading.value = true
  try {
    // 模拟数据
    const weatherDevices = Array.from({ length: 15 }, (_, i) => ({
      id: i + 1,
      sensorCode: `WS-${String(i + 1).padStart(3, '0')}`,
      sensorName: `温湿度传感器${String(i + 1).padStart(3, '0')}`,
      areaName: i < 8 ? 'A区一层' : 'A区二层',
      sensorType: 'weather',
      temperature: 20 + Math.floor(Math.random() * 8),
      humidity: 30 + Math.floor(Math.random() * 40),
      status: i === 8 ? 2 : 1,
      lastUpdateTime: new Date()
    }))

    const airDevices = Array.from({ length: 10 }, (_, i) => ({
      id: 100 + i + 1,
      sensorCode: `AQ-${String(i + 1).padStart(3, '0')}`,
      sensorName: `空气质量传感器${String(i + 1).padStart(3, '0')}`,
      areaName: i < 5 ? 'B区一层' : 'B区二层',
      sensorType: 'air',
      pm25: ['35', '28', '85', '42', '19'][Math.floor(Math.random() * 5)],
      pm10: ['50', '45', '120', '60', '35'][Math.floor(Math.random() * 5)],
      status: i === 2 ? 2 : 1,
      lastUpdateTime: new Date()
    }))

    let filtered = [...weatherDevices, ...airDevices]
    if (selectedCategory.value === 'weather') {
      filtered = weatherDevices
    } else if (selectedCategory.value === 'air') {
      filtered = airDevices
    }

    list.value = filtered
    total.value = filtered.length
  } finally {
    loading.value = false
  }
}

const selectCategory = (category: string) => {
  selectedCategory.value = category
  queryParams.category = category
  queryParams.pageNo = 1
  getList()
}

const refreshData = () => {
  message.success('数据已刷新')
  getList()
}

const exportExcel = () => {
  message.success('正在导出Excel报表...')
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.env-alarm-page {
  box-sizing: border-box;
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
  background: var(--el-bg-color-page, var(--el-bg-color));
}

.alarm-layout {
  display: flex;
  background: var(--el-bg-color);
  border-radius: 4px;
  min-height: calc(100vh - 200px);
}

.tree-panel {
  width: 260px;
  border-right: 1px solid var(--el-border-color-lighter);
  padding: 20px;
  background: var(--el-fill-color-light);

  .tree-title {
    font-weight: 600;
    margin-bottom: 15px;
    font-size: 16px;
  }

  .tree-search {
    margin-bottom: 15px;
  }

  .tree-node {
    padding: 10px;
    cursor: pointer;
    border-radius: 4px;
    margin-bottom: 5px;
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--el-text-color-regular);
    font-size: 14px;

    &:hover {
      background: rgba(var(--el-color-primary-rgb), 0.12);
      color: var(--el-color-primary);
    }

    &.is-active {
      background: var(--el-color-primary);
      color: white;
    }

    .device-count {
      margin-left: auto;
      font-size: 12px;
      background: var(--el-fill-color);
      padding: 2px 8px;
      border-radius: 10px;
    }

    &.is-active {
      .device-count {
        background: rgba(255, 255, 255, 0.22);
      }
    }
  }
}

.main-content {
  flex: 1;
  padding: 20px;
}

.stat-card {
  padding: 20px;
  border-left: 4px solid;

  &.success {
    border-left-color: var(--el-color-success);
  }

  &.warning {
    border-left-color: var(--el-color-warning);
  }

  &.danger {
    border-left-color: var(--el-color-danger);
  }

  .stat-label {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-bottom: 8px;
  }

  .stat-value {
    font-size: 28px;
    font-weight: bold;
    color: var(--el-text-color-primary);
  }
}

.action-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);

  .filter-tags {
    display: flex;
    gap: 10px;

    .filter-tag {
      cursor: pointer;
    }
  }

  .action-buttons {
    display: flex;
    gap: 10px;
  }
}
</style>
