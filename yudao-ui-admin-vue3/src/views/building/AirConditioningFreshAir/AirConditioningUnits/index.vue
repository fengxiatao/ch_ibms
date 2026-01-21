<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="air-conditioning-units dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧建筑</el-breadcrumb-item>
        <el-breadcrumb-item>空调新风</el-breadcrumb-item>
        <el-breadcrumb-item>空调机组</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-form :model="filterForm" label-width="120px" :inline="true">
        <el-form-item label="请选择专业系统">
          <el-select v-model="filterForm.system" placeholder="请选择专业系统" clearable style="width: 200px;">
            <el-option label="暖通专业_空调系统_空调机组" value="hvac_air_conditioning" />
            <el-option label="暖通专业_新风系统_新风机组" value="hvac_fresh_air" />
          </el-select>
        </el-form-item>
        <el-form-item label="请选择运行状态">
          <el-select v-model="filterForm.status" placeholder="请选择运行状态" clearable style="width: 150px;">
            <el-option label="全部状态" value="" />
            <el-option label="正常" value="normal" />
            <el-option label="告警" value="alarm" />
            <el-option label="故障" value="fault" />
            <el-option label="停机" value="stopped" />
          </el-select>
        </el-form-item>
        <el-form-item label="请输入设备名称">
          <el-input 
            v-model="filterForm.deviceName" 
            placeholder="请输入设备名称" 
            clearable 
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            更多筛选
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计概览 -->
    <div class="statistics-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card normal">
            <div class="stat-icon">
              <el-icon><Cpu /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ statistics.normal }}</div>
              <div class="stat-label">正常</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card running">
            <div class="stat-icon">
              <el-icon><VideoPlay /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ statistics.running }}</div>
              <div class="stat-label">在线</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card stopped">
            <div class="stat-icon">
              <el-icon><VideoPause /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ statistics.stopped }}</div>
              <div class="stat-label">离线</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card alarm">
            <div class="stat-icon">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ statistics.alarm }}</div>
              <div class="stat-label">告警</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 空调机组列表 -->
    <div class="units-grid">
      <el-row :gutter="20">
        <el-col v-for="unit in pagedUnits" :key="unit.id" :span="8">
          <el-card class="unit-card" shadow="hover" @click="handleViewDetail(unit)">
            <div class="unit-header">
              <div class="unit-icon" :class="getStatusClass(unit.status)">
                <el-icon><component :is="getStatusIcon(unit.status)" /></el-icon>
              </div>
              <div class="unit-info">
                <div class="unit-name">{{ unit.name }}</div>
                <div class="unit-code">{{ unit.code }}</div>
              </div>
              <div class="unit-status">
                <el-tag :type="getStatusTagType(unit.status)" size="small">
                  {{ getStatusText(unit.status) }}
                </el-tag>
              </div>
            </div>
            <div class="unit-body">
              <div class="unit-location">
                <el-icon><Location /></el-icon>
                <span>{{ unit.location }}</span>
              </div>
              <div class="unit-params">
                <div class="param-item">
                  <span class="param-label">出风温度：</span>
                  <span class="param-value">{{ unit.outletTemp }}°C</span>
                </div>
                <div class="param-item">
                  <span class="param-label">风速模式：</span>
                  <span class="param-value">{{ unit.fanMode }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[9, 18, 36]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        background
        class="pagination-container"
      />
    </div>

    <!-- 设备详情对话框 -->
    <el-dialog v-model="detailDialogVisible" :title="currentUnit?.name" width="80%" draggable>
      <div v-if="currentUnit" class="unit-detail">
        <!-- 基本信息 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-descriptions title="基本信息" :column="1" border>
              <el-descriptions-item label="设备名称">{{ currentUnit.name }}</el-descriptions-item>
              <el-descriptions-item label="专业系统">{{ currentUnit.system }}</el-descriptions-item>
              <el-descriptions-item label="设备编码">{{ currentUnit.code }}</el-descriptions-item>
              <el-descriptions-item label="空间位置">{{ currentUnit.location }}</el-descriptions-item>
              <el-descriptions-item label="物联设备">{{ currentUnit.iotDevice ? '是' : '否' }}</el-descriptions-item>
              <el-descriptions-item label="运行状态">
                <el-tag :type="getStatusTagType(currentUnit.status)">{{ getStatusText(currentUnit.status) }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
          <el-col :span="12">
            <div class="control-panel">
              <h4>控制面板</h4>
              <div class="control-item">
                <span>温度控制</span>
                <div class="control-value">{{ currentUnit.targetTemp }}°C</div>
              </div>
              <div class="control-slider">
                <el-slider v-model="currentUnit.targetTemp" :min="16" :max="30" show-input />
              </div>
              <div class="control-item">
                <span>回风阀调节</span>
                <div class="control-slider">
                  <el-slider v-model="currentUnit.returnAirValve" :min="0" :max="100" show-input />
                </div>
              </div>
              <div class="control-item">
                <span>水阀开度调节</span>
                <div class="control-slider">
                  <el-slider v-model="currentUnit.waterValve" :min="0" :max="100" show-input />
                </div>
              </div>
              <div class="control-actions">
                <el-button type="primary" @click="handleControl">更多信息</el-button>
                <el-button type="success" @click="handleSaveControl">关闭</el-button>
              </div>
            </div>
          </el-col>
        </el-row>

        <!-- 运行参数图表 -->
        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane label="运行日志" name="logs">
            <div class="chart-container">
              <div ref="logsChartRef" style="width: 100%; height: 300px;"></div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="操作日志" name="operations">
            <div class="chart-container">
              <div ref="operationsChartRef" style="width: 100%; height: 300px;"></div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="历史数据" name="history">
            <div class="chart-container">
              <div ref="historyChartRef" style="width: 100%; height: 300px;"></div>
            </div>
          </el-tab-pane>
        </el-tabs>

        <!-- 详细参数 -->
        <div class="detailed-params">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="param-group">
                <h4>回风机手自状态</h4>
                <div class="param-chart">
                  <div class="status-indicator" :class="currentUnit.returnFanStatus">
                    <div class="status-dot"></div>
                  </div>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="param-group">
                <h4>过滤器状态</h4>
                <div class="param-chart">
                  <div class="status-indicator" :class="currentUnit.filterStatus">
                    <div class="status-dot"></div>
                  </div>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="param-group">
                <h4>风机故障状态</h4>
                <div class="param-chart">
                  <div class="status-indicator" :class="currentUnit.fanFaultStatus">
                    <div class="status-dot"></div>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :span="8">
              <div class="param-group">
                <h4>防冻开关状态</h4>
                <div class="param-chart">
                  <div class="status-indicator" :class="currentUnit.antiFreezeStatus">
                    <div class="status-dot"></div>
                  </div>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="param-group">
                <h4>在线状态</h4>
                <div class="param-chart">
                  <div class="status-indicator" :class="currentUnit.onlineStatus">
                    <div class="status-dot"></div>
                  </div>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="param-group">
                <h4>送风温度(°C/RH)</h4>
                <div class="param-chart">
                  <div class="status-indicator" :class="currentUnit.supplyAirStatus">
                    <div class="status-dot"></div>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search, Cpu, VideoPlay, VideoPause, Warning, Location
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// 接口定义
interface AirConditioningUnit {
  id: number
  name: string
  code: string
  system: string
  location: string
  status: 'normal' | 'alarm' | 'fault' | 'stopped'
  iotDevice: boolean
  outletTemp: number
  fanMode: string
  targetTemp: number
  returnAirValve: number
  waterValve: number
  returnFanStatus: string
  filterStatus: string
  fanFaultStatus: string
  antiFreezeStatus: string
  onlineStatus: string
  supplyAirStatus: string
}

// 响应式数据
const loading = ref(false)
const detailDialogVisible = ref(false)
const currentUnit = ref<AirConditioningUnit | null>(null)
const activeTab = ref('logs')
const logsChartRef = ref()
const operationsChartRef = ref()
const historyChartRef = ref()

const filterForm = reactive({
  system: '',
  status: '',
  deviceName: ''
})

const statistics = reactive({
  normal: 21,
  running: 10,
  stopped: 3,
  alarm: 4
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 9,
  total: 0
})

// 空调机组数据
const airConditioningUnits = ref<AirConditioningUnit[]>([
  {
    id: 1,
    name: '风机盘管004',
    code: 'NT_KT_KTJZ_0002',
    system: '暖通专业_空调系统_空调机组',
    location: '南区_科技研发楼_9F_办公区',
    status: 'normal',
    iotDevice: true,
    outletTemp: 25,
    fanMode: '自动',
    targetTemp: 25,
    returnAirValve: 75,
    waterValve: 60,
    returnFanStatus: 'normal',
    filterStatus: 'normal',
    fanFaultStatus: 'normal',
    antiFreezeStatus: 'normal',
    onlineStatus: 'online',
    supplyAirStatus: 'normal'
  },
  {
    id: 2,
    name: '风机盘管001',
    code: 'NT_KT_KTJZ_0001',
    system: '暖通专业_空调系统_空调机组',
    location: '南区_科技研发楼_8F_办公区',
    status: 'alarm',
    iotDevice: true,
    outletTemp: 28,
    fanMode: '手动',
    targetTemp: 24,
    returnAirValve: 80,
    waterValve: 70,
    returnFanStatus: 'alarm',
    filterStatus: 'normal',
    fanFaultStatus: 'normal',
    antiFreezeStatus: 'normal',
    onlineStatus: 'online',
    supplyAirStatus: 'normal'
  },
  {
    id: 3,
    name: '风机盘管002',
    code: 'NT_KT_KTJZ_0003',
    system: '暖通专业_空调系统_空调机组',
    location: '南区_科技研发楼_7F_会议室',
    status: 'normal',
    iotDevice: true,
    outletTemp: 23,
    fanMode: '自动',
    targetTemp: 23,
    returnAirValve: 65,
    waterValve: 55,
    returnFanStatus: 'normal',
    filterStatus: 'normal',
    fanFaultStatus: 'normal',
    antiFreezeStatus: 'normal',
    onlineStatus: 'online',
    supplyAirStatus: 'normal'
  },
  {
    id: 4,
    name: '风机盘管006',
    code: 'NT_KT_KTJZ_0006',
    system: '暖通专业_空调系统_空调机组',
    location: '北区_办公楼_5F_休息区',
    status: 'fault',
    iotDevice: false,
    outletTemp: 0,
    fanMode: '停机',
    targetTemp: 25,
    returnAirValve: 0,
    waterValve: 0,
    returnFanStatus: 'fault',
    filterStatus: 'normal',
    fanFaultStatus: 'fault',
    antiFreezeStatus: 'normal',
    onlineStatus: 'offline',
    supplyAirStatus: 'fault'
  },
  {
    id: 5,
    name: '风机盘管003',
    code: 'NT_KT_KTJZ_0004',
    system: '暖通专业_空调系统_空调机组',
    location: '东区_会议中心_3F_大厅',
    status: 'normal',
    iotDevice: true,
    outletTemp: 26,
    fanMode: '自动',
    targetTemp: 26,
    returnAirValve: 70,
    waterValve: 65,
    returnFanStatus: 'normal',
    filterStatus: 'normal',
    fanFaultStatus: 'normal',
    antiFreezeStatus: 'normal',
    onlineStatus: 'online',
    supplyAirStatus: 'normal'
  }
])

// 计算属性
const filteredUnits = computed(() => {
  let filtered = airConditioningUnits.value
  if (filterForm.system) {
    filtered = filtered.filter(unit => unit.system.includes(filterForm.system))
  }
  if (filterForm.status) {
    filtered = filtered.filter(unit => unit.status === filterForm.status)
  }
  if (filterForm.deviceName) {
    filtered = filtered.filter(unit => 
      unit.name.includes(filterForm.deviceName) || 
      unit.code.includes(filterForm.deviceName)
    )
  }
  return filtered
})

// 使用 watch 来更新 pagination.total
watch(filteredUnits, (newValue) => {
  pagination.total = newValue.length
}, { immediate: true })

const pagedUnits = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return filteredUnits.value.slice(start, end)
})

// 方法
const getStatusClass = (status: string) => {
  return `status-${status}`
}

const getStatusIcon = (status: string) => {
  const icons = {
    normal: Cpu,
    alarm: Warning,
    fault: Warning,
    stopped: VideoPause
  }
  return icons[status] || Cpu
}

const getStatusTagType = (status: string) => {
  const types = {
    normal: 'success',
    alarm: 'warning',
    fault: 'danger',
    stopped: 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts = {
    normal: '正常',
    alarm: '告警',
    fault: '故障',
    stopped: '停机'
  }
  return texts[status] || '未知'
}

const handleSearch = () => {
  pagination.currentPage = 1
}

const handleSizeChange = (val: number) => {
  pagination.pageSize = val
}

const handleCurrentChange = (val: number) => {
  pagination.currentPage = val
}

const handleViewDetail = (unit: AirConditioningUnit) => {
  currentUnit.value = { ...unit }
  detailDialogVisible.value = true
  nextTick(() => {
    initCharts()
  })
}

const handleControl = () => {
  ElMessage.success('控制指令已发送')
}

const handleSaveControl = () => {
  detailDialogVisible.value = false
}

const initCharts = () => {
  // 运行日志图表
  if (logsChartRef.value) {
    const logsChart = echarts.init(logsChartRef.value)
    logsChart.setOption({
      title: { text: '24小时空调运行日志', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00']
      },
      yAxis: { type: 'value' },
      series: [{
        data: [22, 21, 24, 26, 25, 23, 22],
        type: 'line',
        smooth: true,
        itemStyle: { color: '#409eff' }
      }]
    })
  }

  // 操作日志图表
  if (operationsChartRef.value) {
    const operationsChart = echarts.init(operationsChartRef.value)
    operationsChart.setOption({
      title: { text: '操作统计', left: 'center' },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '60%',
        data: [
          { value: 35, name: '自动调节' },
          { value: 25, name: '手动调节' },
          { value: 15, name: '定时控制' },
          { value: 25, name: '其他操作' }
        ]
      }]
    })
  }

  // 历史数据图表
  if (historyChartRef.value) {
    const historyChart = echarts.init(historyChartRef.value)
    historyChart.setOption({
      title: { text: '历史运行数据', left: 'center' },
      tooltip: { trigger: 'axis' },
      legend: { top: 30 },
      xAxis: {
        type: 'category',
        data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      },
      yAxis: { type: 'value' },
      series: [
        {
          name: '温度',
          data: [23, 24, 25, 24, 26, 25, 24],
          type: 'line',
          itemStyle: { color: '#67c23a' }
        },
        {
          name: '湿度',
          data: [45, 50, 48, 52, 49, 47, 46],
          type: 'line',
          itemStyle: { color: '#e6a23c' }
        }
      ]
    })
  }
}

onMounted(() => {
  // 初始化统计数据
  pagination.total = airConditioningUnits.value.length
})
</script>

<style scoped lang="scss">
@use '@/styles/dark-theme.scss';

.air-conditioning-units {
  padding: 20px;

  .breadcrumb-container {
    margin-bottom: 20px;
  }

  .filter-section {
    background: #2d2d2d;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
  }

  .statistics-section {
    margin-bottom: 20px;

    .stat-card {
      display: flex;
      align-items: center;
      padding: 20px;
      border-radius: 8px;
      color: white;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
      }

      &.normal {
        background: linear-gradient(135deg, #67c23a, #85ce61);
      }

      &.running {
        background: linear-gradient(135deg, #409eff, #66b1ff);
      }

      &.stopped {
        background: linear-gradient(135deg, #909399, #b1b3b8);
      }

      &.alarm {
        background: linear-gradient(135deg, #e6a23c, #ebb563);
      }

      .stat-icon {
        font-size: 32px;
        margin-right: 15px;
      }

      .stat-content {
        .stat-number {
          font-size: 28px;
          font-weight: bold;
          line-height: 1;
        }

        .stat-label {
          font-size: 14px;
          opacity: 0.9;
          margin-top: 4px;
        }
      }
    }
  }

  .units-grid {
    .unit-card {
      margin-bottom: 20px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 25px rgba(64, 158, 255, 0.3);
      }

      .unit-header {
        display: flex;
        align-items: center;
        margin-bottom: 16px;

        .unit-icon {
          width: 50px;
          height: 50px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;
          font-size: 24px;
          color: white;

          &.status-normal {
            background: #67c23a;
          }

          &.status-alarm {
            background: #e6a23c;
          }

          &.status-fault {
            background: #f56c6c;
          }

          &.status-stopped {
            background: #909399;
          }
        }

        .unit-info {
          flex: 1;

          .unit-name {
            font-size: 16px;
            font-weight: 600;
            color: #ffffff;
            margin-bottom: 4px;
          }

          .unit-code {
            font-size: 12px;
            color: #b0b0b0;
          }
        }

        .unit-status {
          margin-left: 12px;
        }
      }

      .unit-body {
        .unit-location {
          display: flex;
          align-items: center;
          color: #b0b0b0;
          font-size: 14px;
          margin-bottom: 12px;

          .el-icon {
            margin-right: 6px;
          }
        }

        .unit-params {
          .param-item {
            display: flex;
            justify-content: space-between;
            margin-bottom: 8px;
            font-size: 14px;

            .param-label {
              color: #b0b0b0;
            }

            .param-value {
              color: #ffffff;
              font-weight: 500;
            }
          }
        }
      }
    }

    .pagination-container {
      margin-top: 30px;
      text-align: center;
    }
  }

  .unit-detail {
    .control-panel {
      background: #2d2d2d;
      padding: 20px;
      border-radius: 8px;

      h4 {
        color: #ffffff;
        margin: 0 0 20px 0;
        font-size: 16px;
      }

      .control-item {
        margin-bottom: 20px;

        > span {
          display: block;
          color: #ffffff;
          margin-bottom: 8px;
          font-size: 14px;
        }

        .control-value {
          color: #409eff;
          font-size: 18px;
          font-weight: 600;
          text-align: center;
          margin-bottom: 10px;
        }
      }

      .control-slider {
        margin-bottom: 20px;
      }

      .control-actions {
        text-align: center;
        margin-top: 30px;

        .el-button {
          margin: 0 10px;
        }
      }
    }

    .detail-tabs {
      margin: 30px 0;
    }

    .chart-container {
      background: #2d2d2d;
      border-radius: 8px;
      padding: 20px;
    }

    .detailed-params {
      margin-top: 30px;

      .param-group {
        background: #2d2d2d;
        padding: 20px;
        border-radius: 8px;
        text-align: center;

        h4 {
          color: #ffffff;
          margin: 0 0 15px 0;
          font-size: 14px;
        }

        .param-chart {
          .status-indicator {
            width: 60px;
            height: 60px;
            border-radius: 50%;
            margin: 0 auto;
            display: flex;
            align-items: center;
            justify-content: center;

            &.normal, &.online {
              background: #67c23a;
            }

            &.alarm {
              background: #e6a23c;
            }

            &.fault, &.offline {
              background: #f56c6c;
            }

            .status-dot {
              width: 20px;
              height: 20px;
              border-radius: 50%;
              background: white;
            }
          }
        }
      }
    }
  }
}
</style>


