<template>
 <ContentWrap style="margin-top: 70px;">
  <div class="ibms-fire dark-theme-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <Icon icon="ep:warning" :size="24" />
        <h1>智慧消防</h1>
      </div>
      <div class="fire-status">
        <div class="status-indicator" :class="fireStatus.level">
          <Icon :icon="getStatusIcon(fireStatus.level)" />
          <span>{{ fireStatus.text }}</span>
        </div>
        <div class="last-check">
          最后检查: {{ formatTime(fireStatus.lastCheck) }}
        </div>
      </div>
    </div>

    <!-- 消防概览 -->
    <div class="fire-overview">
      <div class="overview-card alarms">
        <div class="card-header">
          <div class="card-icon">
            <Icon icon="ep:warning" />
          </div>
          <div class="card-title">自动报警</div>
          <div class="card-status" :class="fireData.alarms.status">
            {{ getSystemStatusText(fireData.alarms.status) }}
          </div>
        </div>
        <div class="card-content">
          <div class="metric">
            <span class="metric-label">报警点位</span>
            <span class="metric-value">{{ fireData.alarms.total }}</span>
          </div>
          <div class="metric">
            <span class="metric-label">在线数量</span>
            <span class="metric-value online">{{ fireData.alarms.online }}</span>
          </div>
          <div class="metric">
            <span class="metric-label">今日报警</span>
            <span class="metric-value alarm">{{ fireData.alarms.todayAlarms }}</span>
          </div>
        </div>
      </div>

      <div class="overview-card electric">
        <div class="card-header">
          <div class="card-icon">
            <Icon icon="ep:lightning" />
          </div>
          <div class="card-title">电气火灾</div>
          <div class="card-status" :class="fireData.electric.status">
            {{ getSystemStatusText(fireData.electric.status) }}
          </div>
        </div>
        <div class="card-content">
          <div class="metric">
            <span class="metric-label">监测点位</span>
            <span class="metric-value">{{ fireData.electric.total }}</span>
          </div>
          <div class="metric">
            <span class="metric-label">正常运行</span>
            <span class="metric-value online">{{ fireData.electric.normal }}</span>
          </div>
          <div class="metric">
            <span class="metric-label">异常报警</span>
            <span class="metric-value alarm">{{ fireData.electric.abnormal }}</span>
          </div>
        </div>
      </div>

      <div class="overview-card power">
        <div class="card-header">
          <div class="card-icon">
            <Icon icon="ep:switch-button" />
          </div>
          <div class="card-title">消防电源</div>
          <div class="card-status" :class="fireData.power.status">
            {{ getSystemStatusText(fireData.power.status) }}
          </div>
        </div>
        <div class="card-content">
          <div class="metric">
            <span class="metric-label">电源设备</span>
            <span class="metric-value">{{ fireData.power.total }}</span>
          </div>
          <div class="metric">
            <span class="metric-label">供电正常</span>
            <span class="metric-value online">{{ fireData.power.normal }}</span>
          </div>
          <div class="metric">
            <span class="metric-label">故障数量</span>
            <span class="metric-value alarm">{{ fireData.power.fault }}</span>
          </div>
        </div>
      </div>

      <div class="overview-card lighting">
        <div class="card-header">
          <div class="card-icon">
            <Icon icon="ep:sunny" />
          </div>
          <div class="card-title">应急照明</div>
          <div class="card-status" :class="fireData.lighting.status">
            {{ getSystemStatusText(fireData.lighting.status) }}
          </div>
        </div>
        <div class="card-content">
          <div class="metric">
            <span class="metric-label">照明设备</span>
            <span class="metric-value">{{ fireData.lighting.total }}</span>
          </div>
          <div class="metric">
            <span class="metric-label">工作正常</span>
            <span class="metric-value online">{{ fireData.lighting.normal }}</span>
          </div>
          <div class="metric">
            <span class="metric-label">需要维护</span>
            <span class="metric-value warning">{{ fireData.lighting.maintenance }}</span>
          </div>
        </div>
      </div>

      <div class="overview-card water">
        <div class="card-header">
          <div class="card-icon">
            <Icon icon="ep:water-cup" />
          </div>
          <div class="card-title">消防水</div>
          <div class="card-status" :class="fireData.water.status">
            {{ getSystemStatusText(fireData.water.status) }}
          </div>
        </div>
        <div class="card-content">
          <div class="metric">
            <span class="metric-label">水压</span>
            <span class="metric-value">{{ fireData.water.pressure }}MPa</span>
          </div>
          <div class="metric">
            <span class="metric-label">水位</span>
            <span class="metric-value online">{{ fireData.water.level }}%</span>
          </div>
          <div class="metric">
            <span class="metric-label">泵组状态</span>
            <span class="metric-value">{{ fireData.water.pumpStatus }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧监控面板 -->
      <div class="monitor-panel">
        <div class="panel-header">
          <h3>消防监控</h3>
          <div class="monitor-controls">
            <el-select v-model="selectedSystem" placeholder="选择系统" size="small" style="width: 150px">
              <el-option label="全部系统" value="" />
              <el-option label="自动报警" value="alarm" />
              <el-option label="电气火灾" value="electric" />
              <el-option label="消防电源" value="power" />
              <el-option label="应急照明" value="lighting" />
              <el-option label="消防水" value="water" />
            </el-select>
            <el-button size="small" @click="refreshMonitor">
              <Icon icon="ep:refresh" />
            </el-button>
          </div>
        </div>

        <!-- 消防平面图 -->
        <div class="fire-map">
          <div class="map-container">
            <div class="map-placeholder">
              <el-icon class="map-icon"><Location /></el-icon>
              <p>消防设备电子地图</p>
              <p class="map-desc">此处将显示消防设备的实时位置和状态</p>
            </div>
          </div>
          
          <!-- 图例 -->
          <div class="map-legend">
            <div class="legend-title">图例</div>
            <div class="legend-items">
              <div class="legend-item">
                <div class="legend-dot normal"></div>
                <span>正常设备</span>
              </div>
              <div class="legend-item">
                <div class="legend-dot warning"></div>
                <span>告警设备</span>
              </div>
              <div class="legend-item">
                <div class="legend-dot fault"></div>
                <span>故障设备</span>
              </div>
              <div class="legend-item">
                <div class="legend-dot offline"></div>
                <span>离线设备</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧管理面板 -->
      <div class="management-panel">
        <el-tabs v-model="activeTab" type="border-card">
          <!-- 设备监测 -->
          <el-tab-pane label="设备监测" name="devices">
            <div class="device-monitor">
              <div class="device-filter">
                <el-select v-model="deviceTypeFilter" placeholder="设备类型" size="small">
                  <el-option label="全部设备" value="" />
                  <el-option label="烟感探测器" value="smoke" />
                  <el-option label="温感探测器" value="temperature" />
                  <el-option label="手动报警按钮" value="manual" />
                  <el-option label="声光报警器" value="sounder" />
                  <el-option label="消火栓" value="hydrant" />
                </el-select>
                <el-input 
                  v-model="deviceSearch" 
                  placeholder="搜索设备"
                  :prefix-icon="Search"
                  size="small"
                  style="width: 150px"
                />
              </div>

              <div class="device-list">
                <div 
                  v-for="device in filteredFireDevices" 
                  :key="device.id"
                  :class="['device-item', device.status]"
                >
                  <div class="device-header">
                    <div class="device-icon">
                      <Icon :icon="getFireDeviceIcon(device.type)" />
                    </div>
                    <div class="device-info">
                      <div class="device-name">{{ device.name }}</div>
                      <div class="device-location">{{ device.location }}</div>
                      <div class="device-code">设备编号: {{ device.code }}</div>
                    </div>
                    <div class="device-status" :class="device.status">
                      {{ getDeviceStatusText(device.status) }}
                    </div>
                  </div>

                  <div class="device-metrics">
                    <div 
                      v-for="metric in device.metrics" 
                      :key="metric.name"
                      class="metric-item"
                    >
                      <span class="metric-name">{{ metric.name }}</span>
                      <span
class="metric-value" :class="{ 
                        warning: metric.warning, 
                        danger: metric.danger 
                      }">
                        {{ metric.value }}{{ metric.unit }}
                      </span>
                    </div>
                  </div>

                  <div class="device-actions">
                    <el-button size="small" @click="testDevice(device)">测试</el-button>
                    <el-button size="small" @click="viewDeviceHistory(device)">历史</el-button>
                    <el-button size="small" @click="deviceMaintenance(device)">维护</el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 消防告警 -->
          <el-tab-pane label="消防告警" name="alarms">
            <div class="fire-alarms">
              <div class="alarm-summary">
                <div class="summary-card fire">
                  <div class="summary-count">{{ alarmStats.fire }}</div>
                  <div class="summary-label">火警</div>
                </div>
                <div class="summary-card fault">
                  <div class="summary-count">{{ alarmStats.fault }}</div>
                  <div class="summary-label">故障</div>
                </div>
                <div class="summary-card shield">
                  <div class="summary-count">{{ alarmStats.shield }}</div>
                  <div class="summary-label">屏蔽</div>
                </div>
                <div class="summary-card test">
                  <div class="summary-count">{{ alarmStats.test }}</div>
                  <div class="summary-label">测试</div>
                </div>
              </div>

              <div class="alarm-filter">
                <el-select v-model="alarmTypeFilter" placeholder="告警类型" size="small">
                  <el-option label="全部告警" value="" />
                  <el-option label="火警" value="fire" />
                  <el-option label="故障报警" value="fault" />
                  <el-option label="屏蔽报警" value="shield" />
                  <el-option label="测试报警" value="test" />
                </el-select>
                <el-select v-model="alarmStatusFilter" placeholder="处理状态" size="small">
                  <el-option label="全部状态" value="" />
                  <el-option label="未处理" value="pending" />
                  <el-option label="处理中" value="processing" />
                  <el-option label="已处理" value="handled" />
                </el-select>
              </div>

              <div class="alarm-list">
                <div 
                  v-for="alarm in filteredFireAlarms" 
                  :key="alarm.id"
                  :class="['alarm-item', alarm.type, alarm.level]"
                >
                  <div class="alarm-time">{{ formatTime(alarm.time) }}</div>
                  <div class="alarm-content">
                    <div class="alarm-header">
                      <div class="alarm-type" :class="alarm.type">
                        {{ getAlarmTypeText(alarm.type) }}
                      </div>
                      <div class="alarm-level" :class="alarm.level">
                        {{ getAlarmLevelText(alarm.level) }}
                      </div>
                    </div>
                    <div class="alarm-title">{{ alarm.title }}</div>
                    <div class="alarm-desc">{{ alarm.description }}</div>
                    <div class="alarm-location">
                      <Icon icon="ep:location" />
                      <span>{{ alarm.location }}</span>
                    </div>
                  </div>
                  <div class="alarm-actions">
                    <el-button 
                      v-if="alarm.status === 'pending'"
                      size="small" 
                      type="primary" 
                      @click="confirmAlarm(alarm)"
                    >
                      确认
                    </el-button>
                    <el-button 
                      v-if="alarm.type === 'fire'"
                      size="small" 
                      type="danger" 
                      @click="handleFireAlarm(alarm)"
                    >
                      处置
                    </el-button>
                    <el-button size="small" @click="viewAlarmDetail(alarm)">详情</el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 应急管理 -->
          <el-tab-pane label="应急管理" name="emergency">
            <div class="emergency-management">
              <div class="emergency-plans">
                <div class="plans-header">
                  <h4>应急预案</h4>
                  <el-button size="small" type="primary" @click="addEmergencyPlan">
                    新增预案
                  </el-button>
                </div>
                <div class="plans-list">
                  <div 
                    v-for="plan in emergencyPlans" 
                    :key="plan.id"
                    class="plan-item"
                  >
                    <div class="plan-header">
                      <div class="plan-name">{{ plan.name }}</div>
                      <div class="plan-status" :class="plan.status">
                        {{ getPlanStatusText(plan.status) }}
                      </div>
                    </div>
                    <div class="plan-desc">{{ plan.description }}</div>
                    <div class="plan-meta">
                      <div class="meta-item">
                        <span class="label">适用场景:</span>
                        <span class="value">{{ plan.scenario }}</span>
                      </div>
                      <div class="meta-item">
                        <span class="label">更新时间:</span>
                        <span class="value">{{ formatDate(plan.updateTime) }}</span>
                      </div>
                    </div>
                    <div class="plan-actions">
                      <el-button size="small" @click="viewPlan(plan)">查看</el-button>
                      <el-button size="small" @click="editPlan(plan)">编辑</el-button>
                      <el-button 
                        size="small" 
                        type="warning" 
                        @click="activatePlan(plan)"
                      >
                        启动
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>

              <div class="emergency-drills">
                <div class="drills-header">
                  <h4>应急演练</h4>
                  <el-button size="small" type="primary" @click="scheduledrill">
                    安排演练
                  </el-button>
                </div>
                <div class="drills-calendar">
                  <div class="calendar-header">
                    <el-date-picker
                      v-model="selectedMonth"
                      type="month"
                      placeholder="选择月份"
                      size="small"
                      @change="loadDrillSchedule"
                    />
                  </div>
                  <div class="drill-schedule">
                    <div 
                      v-for="drill in drillSchedule" 
                      :key="drill.id"
                      class="drill-item"
                    >
                      <div class="drill-date">{{ formatDate(drill.date) }}</div>
                      <div class="drill-info">
                        <div class="drill-name">{{ drill.name }}</div>
                        <div class="drill-type">{{ drill.type }}</div>
                        <div class="drill-participants">参与人数: {{ drill.participants }}</div>
                      </div>
                      <div class="drill-status" :class="drill.status">
                        {{ getDrillStatusText(drill.status) }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 维护记录 -->
          <el-tab-pane label="维护记录" name="maintenance">
            <div class="maintenance-records">
              <div class="maintenance-filter">
                <el-date-picker
                  v-model="maintenanceDate"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  size="small"
                  @change="filterMaintenanceRecords"
                />
                <el-select v-model="maintenanceTypeFilter" placeholder="维护类型" size="small">
                  <el-option label="全部类型" value="" />
                  <el-option label="定期检查" value="inspection" />
                  <el-option label="设备维修" value="repair" />
                  <el-option label="系统测试" value="test" />
                  <el-option label="清洁保养" value="cleaning" />
                </el-select>
              </div>

              <div class="maintenance-list">
                <div 
                  v-for="record in filteredMaintenanceRecords" 
                  :key="record.id"
                  class="maintenance-item"
                >
                  <div class="maintenance-header">
                    <div class="maintenance-type" :class="record.type">
                      {{ getMaintenanceTypeText(record.type) }}
                    </div>
                    <div class="maintenance-date">{{ formatDate(record.date) }}</div>
                  </div>
                  <div class="maintenance-content">
                    <div class="maintenance-device">设备: {{ record.device }}</div>
                    <div class="maintenance-desc">{{ record.description }}</div>
                    <div class="maintenance-result">结果: {{ record.result }}</div>
                  </div>
                  <div class="maintenance-footer">
                    <div class="maintenance-person">维护人员: {{ record.person }}</div>
                    <div class="maintenance-actions">
                      <el-button size="small" @click="viewMaintenanceDetail(record)">详情</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Location } from '@element-plus/icons-vue'
import * as FireApi from '@/api/fire'
import type { FireDashboardStatisticsVO, FireAlarmVO, FireDeviceVO, EmergencyEventVO } from '@/api/fire'

// 响应式数据
const selectedSystem = ref('')
const activeTab = ref('devices')
const deviceTypeFilter = ref('')
const deviceSearch = ref('')
const alarmTypeFilter = ref('')
const alarmStatusFilter = ref('')
const selectedMonth = ref(new Date())
const maintenanceDate = ref<[Date, Date]>([new Date(), new Date()])
const maintenanceTypeFilter = ref('')

// 仪表板统计数据
const dashboardData = ref<FireDashboardStatisticsVO>({
  alarms: {
    total: 0,
    online: 0,
    todayAlarms: 0,
    status: 'normal'
  },
  electric: {
    total: 0,
    online: 0,
    todayAlarms: 0,
    status: 'normal'
  },
  water: {
    total: 0,
    online: 0,
    todayAlarms: 0,
    status: 'normal'
  },
  smoke: {
    total: 0,
    online: 0,
    todayAlarms: 0,
    status: 'normal'
  },
  equipment: {
    extinguishers: 0,
    hydrants: 0,
    emergencyLights: 0,
    exits: 0
  },
  systemStatus: {
    level: 'normal',
    text: '系统正常',
    lastCheck: new Date()
  }
})

// 实时报警数据
const realtimeAlarms = ref<FireAlarmVO[]>([])

// 消防设备数据
const fireDevices = ref<FireDeviceVO[]>([])

// 应急事件数据
const emergencyEvents = ref<EmergencyEventVO[]>([])

// 消防系统状态（计算属性）
const fireStatus = computed(() => dashboardData.value.systemStatus)

// 消防数据概览（计算属性）
const fireData = computed(() => ({
  alarms: {
    status: dashboardData.value.alarms.status,
    total: dashboardData.value.alarms.total,
    online: dashboardData.value.alarms.online,
    todayAlarms: dashboardData.value.alarms.todayAlarms
  },
  electric: {
    status: dashboardData.value.electric.status,
    total: dashboardData.value.electric.total,
    normal: dashboardData.value.electric.online,
    abnormal: dashboardData.value.electric.todayAlarms
  },
  power: {
    status: 'normal',
    total: 24,
    normal: 24,
    fault: 0
  },
  lighting: {
    status: 'normal',
    total: dashboardData.value.equipment.emergencyLights,
    normal: dashboardData.value.equipment.emergencyLights - 2,
    maintenance: 2
  },
  water: {
    status: dashboardData.value.water.status,
    pressure: 0.65,
    level: 85,
    pumpStatus: dashboardData.value.water.status === 'normal' ? '正常' : '异常'
  }
}))

// 消防设备列表
const fireDeviceList = ref([
  {
    id: 1,
    name: '烟感探测器SG001',
    type: 'smoke',
    location: 'A座1楼大厅',
    code: 'SG001',
    status: 'normal',
    metrics: [
      { name: '烟雾浓度', value: 0.02, unit: '%', warning: false, danger: false },
      { name: '电池电压', value: 3.6, unit: 'V', warning: false, danger: false }
    ]
  },
  {
    id: 2,
    name: '温感探测器WG002',
    type: 'temperature',
    location: 'A座2楼办公室',
    code: 'WG002',
    status: 'warning',
    metrics: [
      { name: '环境温度', value: 68, unit: '°C', warning: true, danger: false },
      { name: '报警阈值', value: 70, unit: '°C', warning: false, danger: false }
    ]
  },
  {
    id: 3,
    name: '手动报警按钮SD003',
    type: 'manual',
    location: 'A座3楼走廊',
    code: 'SD003',
    status: 'normal',
    metrics: [
      { name: '按钮状态', value: '正常', unit: '', warning: false, danger: false },
      { name: '通信状态', value: '在线', unit: '', warning: false, danger: false }
    ]
  }
])

// 消防告警统计
const alarmStats = ref({
  fire: 1,
  fault: 3,
  shield: 0,
  test: 2
})

// 消防告警列表
const fireAlarmList = ref([
  {
    id: 1,
    type: 'fire',
    level: 'high',
    title: '烟感探测器火警',
    description: 'A座2楼办公室检测到烟雾超标',
    location: 'A座2楼办公室201',
    time: new Date(Date.now() - 300000),
    status: 'pending'
  },
  {
    id: 2,
    type: 'fault',
    level: 'medium',
    title: '消防电源故障',
    description: '消防电源主备电切换异常',
    location: 'A座地下室配电间',
    time: new Date(Date.now() - 600000),
    status: 'processing'
  },
  {
    id: 3,
    type: 'test',
    level: 'low',
    title: '系统测试报警',
    description: '定期系统功能测试',
    location: 'A座消防控制室',
    time: new Date(Date.now() - 900000),
    status: 'handled'
  }
])

// 应急预案
const emergencyPlans = ref([
  {
    id: 1,
    name: '火灾应急预案',
    description: '建筑物发生火灾时的应急处置预案',
    scenario: '火灾事故',
    status: 'active',
    updateTime: new Date('2024-08-15')
  },
  {
    id: 2,
    name: '电气故障应急预案',
    description: '电气系统故障时的应急处置预案',
    scenario: '电气故障',
    status: 'active',
    updateTime: new Date('2024-07-20')
  },
  {
    id: 3,
    name: '人员疏散预案',
    description: '紧急情况下人员疏散的详细预案',
    scenario: '紧急疏散',
    status: 'draft',
    updateTime: new Date('2024-09-01')
  }
])

// 应急演练
const drillSchedule = ref([
  {
    id: 1,
    name: '消防疏散演练',
    type: '疏散演练',
    date: new Date('2024-09-20'),
    participants: 156,
    status: 'scheduled'
  },
  {
    id: 2,
    name: '灭火器使用培训',
    type: '技能培训',
    date: new Date('2024-09-25'),
    participants: 45,
    status: 'completed'
  }
])

// 维护记录
const maintenanceRecords = ref([
  {
    id: 1,
    type: 'inspection',
    device: '烟感探测器SG001',
    description: '定期检查烟感探测器工作状态',
    result: '设备工作正常，灵敏度符合标准',
    date: new Date('2024-09-10'),
    person: '张师傅'
  },
  {
    id: 2,
    type: 'repair',
    device: '声光报警器SL005',
    description: '声光报警器声音异常维修',
    result: '更换扬声器，测试正常',
    date: new Date('2024-09-08'),
    person: '李工程师'
  },
  {
    id: 3,
    type: 'cleaning',
    device: '消火栓XH012',
    description: '消火栓清洁保养',
    result: '清洁完成，水压测试正常',
    date: new Date('2024-09-05'),
    person: '王师傅'
  }
])

// 计算属性
const filteredDevices = computed(() => {
  if (!selectedSystem.value) return fireDeviceList.value
  return fireDeviceList.value.filter(device => {
    // 根据系统类型过滤设备
    const systemTypeMap = {
      alarm: ['smoke', 'temperature', 'manual', 'sounder'],
      electric: ['electric_monitor'],
      power: ['power_supply'],
      lighting: ['emergency_light'],
      water: ['hydrant', 'pump']
    }
    return systemTypeMap[selectedSystem.value]?.includes(device.type)
  })
})

const currentAlarms = computed(() => {
  return fireAlarmList.value.filter(alarm => alarm.status === 'pending')
})

const filteredFireDevices = computed(() => {
  let devices = fireDeviceList.value
  
  if (deviceTypeFilter.value) {
    devices = devices.filter(device => device.type === deviceTypeFilter.value)
  }
  
  if (deviceSearch.value) {
    devices = devices.filter(device => 
      device.name.includes(deviceSearch.value) || 
      device.code.includes(deviceSearch.value) ||
      device.location.includes(deviceSearch.value)
    )
  }
  
  return devices
})

const filteredFireAlarms = computed(() => {
  let alarms = fireAlarmList.value
  
  if (alarmTypeFilter.value) {
    alarms = alarms.filter(alarm => alarm.type === alarmTypeFilter.value)
  }
  
  if (alarmStatusFilter.value) {
    alarms = alarms.filter(alarm => alarm.status === alarmStatusFilter.value)
  }
  
  return alarms
})

const filteredMaintenanceRecords = computed(() => {
  let records = maintenanceRecords.value
  
  if (maintenanceTypeFilter.value) {
    records = records.filter(record => record.type === maintenanceTypeFilter.value)
  }
  
  return records
})

// 方法
const formatTime = (date: Date) => {
  return date.toLocaleString('zh-CN')
}

const formatDate = (date: Date) => {
  return date.toLocaleDateString('zh-CN')
}

const getStatusIcon = (level: string) => {
  const icons = {
    normal: 'ep:circle-check',
    warning: 'ep:warning',
    danger: 'ep:circle-close'
  }
  return icons[level] || 'ep:circle-check'
}

const getSystemStatusText = (status: string) => {
  const statusMap = {
    normal: '正常',
    warning: '告警',
    fault: '故障',
    offline: '离线'
  }
  return statusMap[status] || '未知'
}

const getFireDeviceIcon = (type: string) => {
  const iconMap = {
    smoke: 'ep:smoking',
    temperature: 'ep:thermometer',
    manual: 'ep:switch-button',
    sounder: 'ep:bell',
    hydrant: 'ep:water-cup'
  }
  return iconMap[type] || 'ep:monitor'
}

const getDeviceStatusText = (status: string) => {
  const statusMap = {
    normal: '正常',
    warning: '告警',
    fault: '故障',
    offline: '离线'
  }
  return statusMap[status] || '未知'
}

const getAlarmTypeText = (type: string) => {
  const typeMap = {
    fire: '火警',
    fault: '故障',
    shield: '屏蔽',
    test: '测试'
  }
  return typeMap[type] || '未知'
}

const getAlarmLevelText = (level: string) => {
  const levelMap = {
    high: '高级',
    medium: '中级',
    low: '低级'
  }
  return levelMap[level] || '未知'
}

const getPlanStatusText = (status: string) => {
  const statusMap = {
    active: '生效中',
    draft: '草稿',
    archived: '已归档'
  }
  return statusMap[status] || '未知'
}

const getDrillStatusText = (status: string) => {
  const statusMap = {
    scheduled: '已安排',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || '未知'
}

const getMaintenanceTypeText = (type: string) => {
  const typeMap = {
    inspection: '定期检查',
    repair: '设备维修',
    test: '系统测试',
    cleaning: '清洁保养'
  }
  return typeMap[type] || '未知'
}

// 事件处理
const refreshMonitor = () => {
  ElMessage.success('监控数据已刷新')
}

const selectDevice = (device: any) => {
  ElMessage.info(`选择设备: ${device.name}`)
}

const testDevice = (device: any) => {
  ElMessage.info(`测试设备: ${device.name}`)
}

const viewDeviceHistory = (device: any) => {
  ElMessage.info(`查看设备历史: ${device.name}`)
}

const deviceMaintenance = (device: any) => {
  ElMessage.info(`设备维护: ${device.name}`)
}

const confirmAlarm = (alarm: any) => {
  alarm.status = 'processing'
  ElMessage.success(`告警 ${alarm.title} 已确认`)
}

const handleFireAlarm = (alarm: any) => {
  ElMessage.warning(`火警处置: ${alarm.title}`)
}

const viewAlarmDetail = (alarm: any) => {
  ElMessage.info(`查看告警详情: ${alarm.title}`)
}

const addEmergencyPlan = () => {
  ElMessage.info('新增应急预案功能开发中...')
}

const viewPlan = (plan: any) => {
  ElMessage.info(`查看预案: ${plan.name}`)
}

const editPlan = (plan: any) => {
  ElMessage.info(`编辑预案: ${plan.name}`)
}

const activatePlan = (plan: any) => {
  ElMessage.warning(`启动应急预案: ${plan.name}`)
}

const scheduledrill = () => {
  ElMessage.info('安排应急演练功能开发中...')
}

const loadDrillSchedule = () => {
  ElMessage.success('演练计划已加载')
}

const filterMaintenanceRecords = () => {
  // 过滤逻辑已在计算属性中实现
}

const viewMaintenanceDetail = (record: any) => {
  ElMessage.info(`查看维护记录详情: ${record.device}`)
}

// 获取仪表板统计数据
const getDashboardData = async () => {
  const data = await FireApi.getFireDashboardStatistics()
  dashboardData.value = data
}

// 获取实时报警数据
const getRealtimeAlarms = async () => {
  const data = await FireApi.getFireAlarmList({ pageSize: 10 })
  realtimeAlarms.value = data
}

// 获取消防设备数据
const getFireDevices = async () => {
  const data = await FireApi.getFireDeviceList({ pageSize: 20 })
  fireDevices.value = data
}

// 获取应急事件数据
const getEmergencyEvents = async () => {
  const data = await FireApi.getEmergencyEventList({ pageSize: 5 })
  emergencyEvents.value = data
}

// 刷新所有数据
const refreshAllData = async () => {
  await getDashboardData()
  await getRealtimeAlarms()
  await getFireDevices()
  await getEmergencyEvents()
  ElMessage.success('数据已刷新')
}

// 定时刷新数据
let refreshTimer: NodeJS.Timeout | null = null

const startAutoRefresh = () => {
  refreshTimer = setInterval(() => {
    getDashboardData()
    getRealtimeAlarms()
    getEmergencyEvents()
  }, 30000) // 30秒刷新一次
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

onMounted(async () => {
  // 初始化数据
  await getDashboardData()
  await getRealtimeAlarms()
  await getFireDevices()
  await getEmergencyEvents()
  // 开始自动刷新
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style lang="scss" scoped>@use '@/styles/dark-theme.scss';

.ibms-fire {
  padding: 20px;
  background: linear-gradient(135deg, #0a1628 0%, #1e3a8a 50%, #0f172a 100%);
  min-height: auto;
  color: #ffffff;

  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
    padding: 16px 24px;
    background: rgba(15, 23, 42, 0.8);
    backdrop-filter: blur(15px);
    border-radius: 12px;
    border: 1px solid rgba(239, 68, 68, 0.2);

    .header-title {
      display: flex;
      align-items: center;
      gap: 12px;

      .el-icon {
        color: #ef4444;
      }

      h1 {
        margin: 0;
        font-size: 24px;
        font-weight: 600;
        color: #ef4444;
      }
    }

    .fire-status {
      text-align: right;

      .status-indicator {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        font-weight: 500;
        margin-bottom: 4px;

        &.normal {
          color: #22c55e;
        }

        &.warning {
          color: #f59e0b;
        }

        &.danger {
          color: #ef4444;
        }
      }

      .last-check {
        font-size: 12px;
        color: #94a3b8;
      }
    }
  }

  .fire-overview {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 20px;
    margin-bottom: 20px;

    .overview-card {
      background: rgba(15, 23, 42, 0.8);
      backdrop-filter: blur(15px);
      border-radius: 12px;
      border: 1px solid rgba(239, 68, 68, 0.2);
      padding: 20px;

      .card-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 16px;

        .card-icon {
          width: 40px;
          height: 40px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);

          .el-icon {
            font-size: 20px;
            color: #ffffff;
          }
        }

        .card-title {
          flex: 1;
          margin-left: 12px;
          font-size: 16px;
          font-weight: 500;
          color: #ffffff;
        }

        .card-status {
          padding: 4px 8px;
          border-radius: 12px;
          font-size: 12px;

          &.normal {
            background: #22c55e;
            color: #ffffff;
          }

          &.warning {
            background: #f59e0b;
            color: #ffffff;
          }

          &.fault {
            background: #ef4444;
            color: #ffffff;
          }
        }
      }

      .card-content {
        .metric {
          display: flex;
          justify-content: space-between;
          margin-bottom: 8px;
          font-size: 14px;

          &:last-child {
            margin-bottom: 0;
          }

          .metric-label {
            color: #94a3b8;
          }

          .metric-value {
            color: #ef4444;
            font-weight: 500;

            &.online {
              color: #22c55e;
            }

            &.warning {
              color: #f59e0b;
            }

            &.alarm {
              color: #ef4444;
            }
          }
        }
      }
    }
  }

  .main-content {
    display: flex;
    gap: 20px;
    height: calc(100vh - 420px);

    .monitor-panel {
      flex: 2;
      background: rgba(15, 23, 42, 0.8);
      backdrop-filter: blur(15px);
      border-radius: 12px;
      border: 1px solid rgba(239, 68, 68, 0.1);
      display: flex;
      flex-direction: column;

      .panel-header {
        padding: 20px;
        border-bottom: 1px solid rgba(239, 68, 68, 0.1);
        display: flex;
        justify-content: space-between;
        align-items: center;

        h3 {
          margin: 0;
          color: #ef4444;
        }

        .monitor-controls {
          display: flex;
          gap: 12px;
          align-items: center;
        }
      }

      .fire-map {
        flex: 1;
        display: flex;

        .map-container {
          flex: 1;
          padding: 20px;
        }

        .map-legend {
          width: 150px;
          padding: 20px;
          border-left: 1px solid rgba(239, 68, 68, 0.1);

          .legend-title {
            color: #ef4444;
            font-size: 14px;
            margin-bottom: 12px;
          }

          .legend-items {
            display: flex;
            flex-direction: column;
            gap: 8px;

            .legend-item {
              display: flex;
              align-items: center;
              gap: 8px;
              font-size: 12px;
              color: #94a3b8;

              .legend-dot {
                width: 8px;
                height: 8px;
                border-radius: 50%;

                &.normal {
                  background: #22c55e;
                }

                &.warning {
                  background: #f59e0b;
                }

                &.fault {
                  background: #ef4444;
                }

                &.offline {
                  background: #64748b;
                }
              }
            }
          }
        }
      }
    }

    .management-panel {
      width: 400px;

      :deep(.el-tabs) {
        height: 100%;
        background: rgba(15, 23, 42, 0.8);
        backdrop-filter: blur(15px);
        border: 1px solid rgba(239, 68, 68, 0.1);

        .el-tabs__header {
          background: rgba(239, 68, 68, 0.05);
          margin: 0;

          .el-tabs__nav {
            background: transparent;

            .el-tabs__item {
              color: #94a3b8;
              border-color: rgba(239, 68, 68, 0.1);

              &.is-active {
                color: #ef4444;
                background: rgba(239, 68, 68, 0.1);
              }
            }
          }
        }

        .el-tabs__content {
          height: calc(100% - 60px);
          padding: 20px;
          color: #ffffff;

          .el-tab-pane {
            height: 100%;
            display: flex;
            flex-direction: column;
          }
        }
      }

      // 设备监测样式
      .device-monitor {
        height: 100%;
        display: flex;
        flex-direction: column;

        .device-filter {
          display: flex;
          gap: 12px;
          margin-bottom: 16px;
        }

        .device-list {
          flex: 1;
          overflow-y: auto;
          display: flex;
          flex-direction: column;
          gap: 12px;

          .device-item {
            padding: 16px;
            background: rgba(239, 68, 68, 0.05);
            border-radius: 8px;
            border-left: 4px solid;

            &.normal {
              border-left-color: #22c55e;
            }

            &.warning {
              border-left-color: #f59e0b;
            }

            &.fault {
              border-left-color: #ef4444;
            }

            &.offline {
              border-left-color: #64748b;
            }

            .device-header {
              display: flex;
              align-items: center;
              margin-bottom: 12px;

              .device-icon {
                width: 32px;
                height: 32px;
                background: rgba(239, 68, 68, 0.2);
                border-radius: 6px;
                display: flex;
                align-items: center;
                justify-content: center;
                margin-right: 12px;

                .el-icon {
                  color: #ef4444;
                }
              }

              .device-info {
                flex: 1;

                .device-name {
                  font-size: 14px;
                  color: #ffffff;
                  margin-bottom: 2px;
                }

                .device-location {
                  font-size: 12px;
                  color: #94a3b8;
                  margin-bottom: 2px;
                }

                .device-code {
                  font-size: 11px;
                  color: #64748b;
                }
              }

              .device-status {
                padding: 4px 8px;
                border-radius: 12px;
                font-size: 12px;

                &.normal { background: #22c55e; color: #ffffff; }
                &.warning { background: #f59e0b; color: #ffffff; }
                &.fault { background: #ef4444; color: #ffffff; }
                &.offline { background: #64748b; color: #ffffff; }
              }
            }

            .device-metrics {
              display: grid;
              grid-template-columns: 1fr 1fr;
              gap: 8px;
              margin-bottom: 12px;

              .metric-item {
                display: flex;
                justify-content: space-between;
                font-size: 12px;

                .metric-name {
                  color: #94a3b8;
                }

                .metric-value {
                  color: #ffffff;

                  &.warning {
                    color: #f59e0b;
                  }

                  &.danger {
                    color: #ef4444;
                  }
                }
              }
            }

            .device-actions {
              display: flex;
              gap: 8px;
            }
          }
        }
      }

      // 消防告警样式
      .fire-alarms {
        height: 100%;
        display: flex;
        flex-direction: column;

        .alarm-summary {
          display: flex;
          gap: 12px;
          margin-bottom: 16px;

          .summary-card {
            flex: 1;
            padding: 12px;
            border-radius: 8px;
            text-align: center;

            &.fire { background: rgba(239, 68, 68, 0.2); }
            &.fault { background: rgba(245, 158, 11, 0.2); }
            &.shield { background: rgba(148, 163, 184, 0.2); }
            &.test { background: rgba(34, 197, 94, 0.2); }

            .summary-count {
              font-size: 20px;
              font-weight: bold;
              color: #ffffff;
              margin-bottom: 4px;
            }

            .summary-label {
              font-size: 12px;
              color: #94a3b8;
            }
          }
        }

        .alarm-filter {
          display: flex;
          gap: 12px;
          margin-bottom: 16px;
        }

        .alarm-list {
          flex: 1;
          overflow-y: auto;
          display: flex;
          flex-direction: column;
          gap: 12px;

          .alarm-item {
            padding: 16px;
            border-radius: 8px;
            border-left: 4px solid;

            &.fire {
              background: rgba(239, 68, 68, 0.1);
              border-left-color: #ef4444;
            }

            &.fault {
              background: rgba(245, 158, 11, 0.1);
              border-left-color: #f59e0b;
            }

            &.test {
              background: rgba(34, 197, 94, 0.1);
              border-left-color: #22c55e;
            }

            .alarm-time {
              font-size: 11px;
              color: #64748b;
              margin-bottom: 8px;
            }

            .alarm-content {
              margin-bottom: 12px;

              .alarm-header {
                display: flex;
                gap: 12px;
                margin-bottom: 8px;

                .alarm-type {
                  padding: 2px 6px;
                  border-radius: 4px;
                  font-size: 10px;

                  &.fire { background: #ef4444; color: #ffffff; }
                  &.fault { background: #f59e0b; color: #ffffff; }
                  &.test { background: #22c55e; color: #ffffff; }
                }

                .alarm-level {
                  padding: 2px 6px;
                  border-radius: 4px;
                  font-size: 10px;

                  &.high { background: #ef4444; color: #ffffff; }
                  &.medium { background: #f59e0b; color: #ffffff; }
                  &.low { background: #22c55e; color: #ffffff; }
                }
              }

              .alarm-title {
                font-size: 14px;
                color: #ffffff;
                margin-bottom: 4px;
              }

              .alarm-desc {
                font-size: 12px;
                color: #94a3b8;
                margin-bottom: 8px;
              }

              .alarm-location {
                display: flex;
                align-items: center;
                gap: 4px;
                font-size: 11px;
                color: #64748b;
              }
            }

            .alarm-actions {
              display: flex;
              gap: 8px;
            }
          }
        }
      }

      // 应急管理样式
      .emergency-management {
        height: 100%;
        display: flex;
        flex-direction: column;

        .emergency-plans {
          margin-bottom: 24px;

          .plans-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;

            h4 {
              margin: 0;
              color: #ef4444;
            }
          }

          .plans-list {
            display: flex;
            flex-direction: column;
            gap: 12px;

            .plan-item {
              padding: 16px;
              background: rgba(239, 68, 68, 0.05);
              border-radius: 8px;

              .plan-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 8px;

                .plan-name {
                  font-size: 14px;
                  font-weight: 500;
                  color: #ffffff;
                }

                .plan-status {
                  padding: 4px 8px;
                  border-radius: 12px;
                  font-size: 12px;

                  &.active { background: #22c55e; color: #ffffff; }
                  &.draft { background: #f59e0b; color: #ffffff; }
                  &.archived { background: #64748b; color: #ffffff; }
                }
              }

              .plan-desc {
                font-size: 12px;
                color: #94a3b8;
                margin-bottom: 12px;
              }

              .plan-meta {
                margin-bottom: 12px;

                .meta-item {
                  display: flex;
                  margin-bottom: 4px;
                  font-size: 12px;

                  .label {
                    color: #94a3b8;
                    margin-right: 8px;
                  }

                  .value {
                    color: #ffffff;
                  }
                }
              }

              .plan-actions {
                display: flex;
                gap: 8px;
              }
            }
          }
        }

        .emergency-drills {
          flex: 1;

          .drills-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;

            h4 {
              margin: 0;
              color: #ef4444;
            }
          }

          .drills-calendar {
            .calendar-header {
              margin-bottom: 16px;
            }

            .drill-schedule {
              display: flex;
              flex-direction: column;
              gap: 12px;

              .drill-item {
                padding: 12px;
                background: rgba(239, 68, 68, 0.05);
                border-radius: 8px;
                display: flex;
                justify-content: space-between;
                align-items: center;

                .drill-date {
                  font-size: 12px;
                  color: #ef4444;
                  font-weight: 500;
                }

                .drill-info {
                  flex: 1;
                  margin-left: 16px;

                  .drill-name {
                    font-size: 14px;
                    color: #ffffff;
                    margin-bottom: 2px;
                  }

                  .drill-type {
                    font-size: 12px;
                    color: #94a3b8;
                    margin-bottom: 2px;
                  }

                  .drill-participants {
                    font-size: 11px;
                    color: #64748b;
                  }
                }

                .drill-status {
                  padding: 4px 8px;
                  border-radius: 12px;
                  font-size: 12px;

                  &.scheduled { background: #3b82f6; color: #ffffff; }
                  &.completed { background: #22c55e; color: #ffffff; }
                  &.cancelled { background: #64748b; color: #ffffff; }
                }
              }
            }
          }
        }
      }

      // 维护记录样式
      .maintenance-records {
        height: 100%;
        display: flex;
        flex-direction: column;

        .maintenance-filter {
          display: flex;
          gap: 12px;
          margin-bottom: 16px;
        }

        .maintenance-list {
          flex: 1;
          overflow-y: auto;
          display: flex;
          flex-direction: column;
          gap: 12px;

          .maintenance-item {
            padding: 16px;
            background: rgba(239, 68, 68, 0.05);
            border-radius: 8px;

            .maintenance-header {
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 12px;

              .maintenance-type {
                padding: 4px 8px;
                border-radius: 12px;
                font-size: 12px;

                &.inspection { background: #3b82f6; color: #ffffff; }
                &.repair { background: #ef4444; color: #ffffff; }
                &.test { background: #22c55e; color: #ffffff; }
                &.cleaning { background: #f59e0b; color: #ffffff; }
              }

              .maintenance-date {
                font-size: 12px;
                color: #94a3b8;
              }
            }

            .maintenance-content {
              margin-bottom: 12px;

              .maintenance-device {
                font-size: 14px;
                color: #ffffff;
                margin-bottom: 4px;
              }

              .maintenance-desc {
                font-size: 12px;
                color: #94a3b8;
                margin-bottom: 4px;
              }

              .maintenance-result {
                font-size: 12px;
                color: #22c55e;
              }
            }

            .maintenance-footer {
              display: flex;
              justify-content: space-between;
              align-items: center;

              .maintenance-person {
                font-size: 12px;
                color: #64748b;
              }

              .maintenance-actions {
                display: flex;
                gap: 8px;
              }
            }
          }
        }
      }
    }
  }
}
</style>
