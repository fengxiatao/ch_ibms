<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="auto-alarm-container dark-theme-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>自动报警</h2>
        <div class="system-status">
          <div class="status-item">
            <span class="label">总数:</span>
            <span class="value">{{ deviceStats.total }}台</span>
          </div>
          <div class="status-item">
            <span class="label">在线:</span>
            <span class="value online">{{ deviceStats.online }}台</span>
          </div>
          <div class="status-item">
            <span class="label">离线:</span>
            <span class="value offline">{{ deviceStats.offline }}台</span>
          </div>
          <div class="status-item">
            <span class="label">故障:</span>
            <span class="value fault">{{ deviceStats.fault }}台</span>
          </div>
          <div class="status-item">
            <span class="label">报警:</span>
            <span class="value alarm">{{ deviceStats.alarm }}台</span>
          </div>
        </div>
      </div>
      <div class="header-right">
        <el-button-group>
          <el-button 
            :type="viewMode === 'card' ? 'primary' : ''" 
            @click="viewMode = 'card'"
            :icon="Grid"
          >
            卡片列表
          </el-button>
          <el-button 
            :type="viewMode === 'map' ? 'primary' : ''" 
            @click="viewMode = 'map'"
            :icon="Location"
          >
            电子地图
          </el-button>
        </el-button-group>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-toolbar">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="专业系统">
          <el-select v-model="filterForm.systemType" placeholder="请选择专业系统" style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="火灾自动报警系统" value="fire_alarm" />
            <el-option label="消防联动控制系统" value="fire_control" />
            <el-option label="电气火灾监控系统" value="electric_fire" />
          </el-select>
        </el-form-item>
        <el-form-item label="运行状态">
          <el-select v-model="filterForm.status" placeholder="请选择运行状态" style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="正常" value="normal" />
            <el-option label="报警" value="alarm" />
            <el-option label="故障" value="fault" />
            <el-option label="离线" value="offline" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input 
            v-model="filterForm.keyword" 
            placeholder="请输入设备名称" 
            :prefix-icon="Search"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleFilter">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button type="primary" :icon="Download" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 卡片视图 -->
    <div v-show="viewMode === 'card'" class="card-view">
      <div class="device-cards">
        <div 
          v-for="device in filteredDevices" 
          :key="device.id"
          :class="['device-card', device.status]"
          @click="handleDeviceClick(device)"
        >
          <div class="card-header">
            <div class="device-icon">
              <Icon :icon="getDeviceIcon(device.type)" />
            </div>
            <div class="device-info">
              <h4>{{ device.name }}</h4>
              <span class="device-code">{{ device.code }}</span>
            </div>
            <div class="status-badge" :class="device.status">
              {{ getStatusText(device.status) }}
            </div>
          </div>
          <div class="card-content">
            <div class="location">
              <Icon icon="ep:location" />
              <span>{{ device.location }}</span>
            </div>
            <div class="metrics">
              <div class="metric-item">
                <span class="label">运行状态:</span>
                <span :class="['value', device.status]">{{ getStatusText(device.status) }}</span>
              </div>
              <div class="metric-item">
                <span class="label">最后通讯:</span>
                <span class="value">{{ formatTime(device.lastComm) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 地图视图 -->
    <div v-show="viewMode === 'map'" class="map-view">
      <div class="map-container">
        <div class="floor-selector">
          <el-radio-group v-model="selectedFloor" @change="handleFloorChange">
            <el-radio-button label="3F">3楼</el-radio-button>
            <el-radio-button label="2F">2楼</el-radio-button>
            <el-radio-button label="1F">1楼</el-radio-button>
          </el-radio-group>
        </div>
        
        <!-- 3D楼层图 -->
        <div class="floor-map" ref="floorMapRef">
          <img :src="getCurrentFloorImage()" alt="楼层平面图" class="floor-image" />
          
          <!-- 设备点位 -->
          <div 
            v-for="device in getFloorDevices(selectedFloor)"
            :key="device.id"
            :class="['device-point', device.status]"
            :style="{ left: device.x + '%', top: device.y + '%' }"
            @click="handleDeviceClick(device)"
          >
            <div class="point-indicator"></div>
            <div class="device-tooltip">
              {{ device.name }}<br />
              状态：{{ getStatusText(device.status) }}
            </div>
          </div>
        </div>

        <!-- 地图图例 -->
        <div class="map-legend">
          <div class="legend-item">
            <div class="legend-dot normal"></div>
            <span>正常</span>
          </div>
          <div class="legend-item">
            <div class="legend-dot alarm"></div>
            <span>报警</span>
          </div>
          <div class="legend-item">
            <div class="legend-dot fault"></div>
            <span>故障</span>
          </div>
          <div class="legend-item">
            <div class="legend-dot offline"></div>
            <span>离线</span>
          </div>
        </div>
      </div>

      <!-- 右侧统计面板 -->
      <div class="stats-panel">
        <div class="panel-header">
          <h3>消防设备实时统计</h3>
        </div>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon normal">
              <Icon icon="ep:check" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ deviceStats.online }}</div>
              <div class="stat-label">正常</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon alarm">
              <Icon icon="ep:warning" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ deviceStats.alarm }}</div>
              <div class="stat-label">报警</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon fault">
              <Icon icon="ep:close" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ deviceStats.fault }}</div>
              <div class="stat-label">故障</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon offline">
              <Icon icon="ep:minus" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ deviceStats.offline }}</div>
              <div class="stat-label">离线</div>
            </div>
          </div>
        </div>

        <!-- 24小时消防水监控位变化趋势 -->
        <div class="trend-chart">
          <h4>24小时报警设备变化趋势</h4>
          <div class="chart-container" ref="trendChartRef"></div>
        </div>
      </div>
    </div>

    <!-- 设备详情弹窗 -->
    <el-dialog v-model="deviceDialogVisible" title="设备详情" width="600px">
      <div v-if="currentDevice" class="device-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="设备名称">{{ currentDevice.name }}</el-descriptions-item>
          <el-descriptions-item label="设备编号">{{ currentDevice.code }}</el-descriptions-item>
          <el-descriptions-item label="设备类型">{{ currentDevice.type }}</el-descriptions-item>
          <el-descriptions-item label="运行状态">
            <el-tag :type="getStatusTagType(currentDevice.status)">
              {{ getStatusText(currentDevice.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="安装位置">{{ currentDevice.location }}</el-descriptions-item>
          <el-descriptions-item label="最后通讯">{{ formatTime(currentDevice.lastComm) }}</el-descriptions-item>
          <el-descriptions-item label="制造商">{{ currentDevice.manufacturer }}</el-descriptions-item>
          <el-descriptions-item label="设备型号">{{ currentDevice.model }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Grid, Location, Search, Refresh, Download } from '@element-plus/icons-vue'
import { Icon } from '@/components/Icon'

// 响应式数据
const viewMode = ref<'card' | 'map'>('card')
const selectedFloor = ref('3F')
const deviceDialogVisible = ref(false)
const currentDevice = ref<any>(null)
const floorMapRef = ref<HTMLElement>()
const trendChartRef = ref<HTMLElement>()

// 筛选表单
const filterForm = ref({
  systemType: '',
  status: '',
  keyword: ''
})

// 分页
const pagination = ref({
  page: 1,
  size: 20,
  total: 0
})

// 设备统计
const deviceStats = ref({
  total: 14,
  online: 9,
  offline: 1,
  fault: 2,
  alarm: 2
})

// 设备数据
const deviceList = ref([
  {
    id: 1,
    name: '火灾报警控制器001',
    code: 'FACP-001',
    type: '火灾报警控制器',
    location: '3F_消防控制室',
    status: 'normal',
    lastComm: new Date(Date.now() - 120000),
    manufacturer: '海湾安全',
    model: 'GST-DJ9000',
    x: 15,
    y: 20,
    floor: '3F'
  },
  {
    id: 2,
    name: '烟感探测器002',
    code: 'SD-002',
    type: '烟感探测器',
    location: '3F_办公区A',
    status: 'alarm',
    lastComm: new Date(Date.now() - 60000),
    manufacturer: '海湾安全',
    model: 'JTY-GD-G3',
    x: 25,
    y: 30,
    floor: '3F'
  },
  {
    id: 3,
    name: '温感探测器003',
    code: 'TD-003',
    type: '温感探测器',
    location: '3F_会议室',
    status: 'normal',
    lastComm: new Date(Date.now() - 180000),
    manufacturer: '海湾安全',
    model: 'JTW-ZD-G3',
    x: 35,
    y: 25,
    floor: '3F'
  },
  {
    id: 4,
    name: '手动报警按钮004',
    code: 'MCP-004',
    type: '手动报警按钮',
    location: '3F_走廊',
    status: 'fault',
    lastComm: new Date(Date.now() - 300000),
    manufacturer: '海湾安全',
    model: 'J-SAP-M-G3',
    x: 45,
    y: 40,
    floor: '3F'
  },
  {
    id: 5,
    name: '声光报警器005',
    code: 'SAW-005',
    type: '声光报警器',
    location: '2F_大厅',
    status: 'normal',
    lastComm: new Date(Date.now() - 90000),
    manufacturer: '海湾安全',
    model: 'GST-LD8301',
    x: 20,
    y: 35,
    floor: '2F'
  },
  {
    id: 6,
    name: '消火栓按钮006',
    code: 'HB-006',
    type: '消火栓按钮',
    location: '2F_走廊',
    status: 'offline',
    lastComm: new Date(Date.now() - 1800000),
    manufacturer: '海湾安全',
    model: 'J-SAP-M-963',
    x: 30,
    y: 45,
    floor: '2F'
  }
])

// 计算属性
const filteredAllDevices = computed(() => {
  let devices = deviceList.value
  
  if (filterForm.value.systemType) {
    devices = devices.filter(device => device.type.includes(filterForm.value.systemType))
  }
  
  if (filterForm.value.status) {
    devices = devices.filter(device => device.status === filterForm.value.status)
  }
  
  if (filterForm.value.keyword) {
    devices = devices.filter(device => 
      device.name.includes(filterForm.value.keyword) || 
      device.code.includes(filterForm.value.keyword)
    )
  }
  
  return devices
})

const filteredDevices = computed(() => {
  const devices = filteredAllDevices.value
  const start = (pagination.value.page - 1) * pagination.value.size
  const end = start + pagination.value.size
  
  return devices.slice(start, end)
})

// 监听过滤结果更新分页总数
watch(filteredAllDevices, (newDevices) => {
  pagination.value.total = newDevices.length
}, { immediate: true })

// 方法
const getDeviceIcon = (type: string) => {
  const iconMap: Record<string, string> = {
    '火灾报警控制器': 'ep:cpu',
    '烟感探测器': 'fa:cloud',
    '温感探测器': 'ep:sunny',
    '手动报警按钮': 'ep:switch-button',
    '声光报警器': 'ep:bell',
    '消火栓按钮': 'fa:fire-extinguisher'
  }
  return iconMap[type] || 'ep:cpu'
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'normal': '正常',
    'alarm': '报警',
    'fault': '故障',
    'offline': '离线'
  }
  return statusMap[status] || '未知'
}

const getStatusTagType = (status: string) => {
  const tagMap: Record<string, string> = {
    'normal': 'success',
    'alarm': 'danger',
    'fault': 'warning',
    'offline': 'info'
  }
  return tagMap[status] || 'info'
}

const formatTime = (date: Date) => {
  return date.toLocaleString('zh-CN')
}

const getCurrentFloorImage = () => {
  // 这里应该返回对应楼层的平面图
  return '/images/floor-plan-3f.jpg'
}

const getFloorDevices = (floor: string) => {
  return deviceList.value.filter(device => device.floor === floor)
}

const handleFilter = () => {
  pagination.value.page = 1
  // 过滤逻辑已在计算属性中实现
}

const handleReset = () => {
  filterForm.value = {
    systemType: '',
    status: '',
    keyword: ''
  }
  pagination.value.page = 1
}

const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

const handleSizeChange = (size: number) => {
  pagination.value.size = size
  pagination.value.page = 1
}

const handleCurrentChange = (page: number) => {
  pagination.value.page = page
}

const handleFloorChange = (floor: string) => {
  selectedFloor.value = floor
}

const handleDeviceClick = (device: any) => {
  currentDevice.value = device
  deviceDialogVisible.value = true
}

onMounted(() => {
  // 初始化数据
})
</script>

<style lang="scss" scoped>@use '@/styles/dark-theme.scss';

.auto-alarm-container {
  padding: 20px;
  background: #0a1628;
  min-height: 100vh;
  color: #ffffff;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 20px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 8px;

    .header-left {
      h2 {
        margin: 0 0 10px 0;
        color: #00d4ff;
        font-size: 24px;
      }

      .system-status {
        display: flex;
        gap: 20px;

        .status-item {
          .label {
            color: #8892b0;
            margin-right: 5px;
          }

          .value {
            font-weight: bold;
            
            &.online { color: #52c41a; }
            &.offline { color: #8892b0; }
            &.fault { color: #faad14; }
            &.alarm { color: #ff4d4f; }
          }
        }
      }
    }
  }

  .filter-toolbar {
    margin-bottom: 20px;
    padding: 15px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 8px;

    .filter-form {
      :deep(.el-form-item__label) {
        color: #ffffff;
      }
    }
  }

  .card-view {
    .device-cards {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 16px;
      margin-bottom: 20px;

      .device-card {
        background: rgba(255, 255, 255, 0.05);
        border-radius: 8px;
        padding: 16px;
        cursor: pointer;
        transition: all 0.3s;
        border: 1px solid transparent;

        &:hover {
          background: rgba(255, 255, 255, 0.08);
          transform: translateY(-2px);
        }

        &.alarm {
          border-color: #ff4d4f;
          box-shadow: 0 0 10px rgba(255, 77, 79, 0.3);
        }

        &.fault {
          border-color: #faad14;
          box-shadow: 0 0 10px rgba(250, 173, 20, 0.3);
        }

        .card-header {
          display: flex;
          align-items: center;
          margin-bottom: 12px;

          .device-icon {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background: rgba(0, 212, 255, 0.2);
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 12px;
            color: #00d4ff;
          }

          .device-info {
            flex: 1;

            h4 {
              margin: 0;
              font-size: 14px;
              color: #ffffff;
            }

            .device-code {
              font-size: 12px;
              color: #8892b0;
            }
          }

          .status-badge {
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 12px;

            &.normal {
              background: rgba(82, 196, 26, 0.2);
              color: #52c41a;
            }

            &.alarm {
              background: rgba(255, 77, 79, 0.2);
              color: #ff4d4f;
            }

            &.fault {
              background: rgba(250, 173, 20, 0.2);
              color: #faad14;
            }

            &.offline {
              background: rgba(136, 146, 176, 0.2);
              color: #8892b0;
            }
          }
        }

        .card-content {
          .location {
            display: flex;
            align-items: center;
            margin-bottom: 8px;
            color: #8892b0;
            font-size: 12px;

            .el-icon {
              margin-right: 4px;
            }
          }

          .metrics {
            .metric-item {
              display: flex;
              justify-content: space-between;
              margin-bottom: 4px;
              font-size: 12px;

              .label {
                color: #8892b0;
              }

              .value {
                &.normal { color: #52c41a; }
                &.alarm { color: #ff4d4f; }
                &.fault { color: #faad14; }
                &.offline { color: #8892b0; }
              }
            }
          }
        }
      }
    }

    .pagination-wrapper {
      display: flex;
      justify-content: center;
      margin-top: 20px;
    }
  }

  .map-view {
    display: flex;
    gap: 20px;

    .map-container {
      flex: 1;
      background: rgba(255, 255, 255, 0.05);
      border-radius: 8px;
      padding: 20px;
      position: relative;

      .floor-selector {
        margin-bottom: 15px;
      }

      .floor-map {
        position: relative;
        width: 100%;
        height: 500px;
        overflow: hidden;
        border-radius: 8px;

        .floor-image {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .device-point {
          position: absolute;
          width: 20px;
          height: 20px;
          cursor: pointer;
          z-index: 10;

          .point-indicator {
            width: 100%;
            height: 100%;
            border-radius: 50%;
            border: 2px solid #ffffff;
            animation: pulse 2s infinite;

            &.normal {
              background: #52c41a;
            }

            &.alarm {
              background: #ff4d4f;
            }

            &.fault {
              background: #faad14;
            }

            &.offline {
              background: #8892b0;
            }
          }

          .device-tooltip {
            position: absolute;
            bottom: 25px;
            left: 50%;
            transform: translateX(-50%);
            background: rgba(0, 0, 0, 0.8);
            color: #ffffff;
            padding: 8px 12px;
            border-radius: 4px;
            font-size: 12px;
            white-space: nowrap;
            opacity: 0;
            transition: opacity 0.3s;
            pointer-events: none;
          }

          &:hover .device-tooltip {
            opacity: 1;
          }
        }
      }

      .map-legend {
        position: absolute;
        bottom: 20px;
        right: 20px;
        background: rgba(0, 0, 0, 0.7);
        padding: 15px;
        border-radius: 8px;

        .legend-item {
          display: flex;
          align-items: center;
          margin-bottom: 8px;

          &:last-child {
            margin-bottom: 0;
          }

          .legend-dot {
            width: 12px;
            height: 12px;
            border-radius: 50%;
            margin-right: 8px;

            &.normal { background: #52c41a; }
            &.alarm { background: #ff4d4f; }
            &.fault { background: #faad14; }
            &.offline { background: #8892b0; }
          }

          span {
            font-size: 12px;
            color: #ffffff;
          }
        }
      }
    }

    .stats-panel {
      width: 350px;
      background: rgba(255, 255, 255, 0.05);
      border-radius: 8px;
      padding: 20px;

      .panel-header {
        margin-bottom: 20px;

        h3 {
          margin: 0;
          color: #00d4ff;
          font-size: 16px;
        }
      }

      .stats-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 15px;
        margin-bottom: 30px;

        .stat-card {
          display: flex;
          align-items: center;
          padding: 15px;
          background: rgba(255, 255, 255, 0.05);
          border-radius: 8px;

          .stat-icon {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 12px;

            &.normal {
              background: rgba(82, 196, 26, 0.2);
              color: #52c41a;
            }

            &.alarm {
              background: rgba(255, 77, 79, 0.2);
              color: #ff4d4f;
            }

            &.fault {
              background: rgba(250, 173, 20, 0.2);
              color: #faad14;
            }

            &.offline {
              background: rgba(136, 146, 176, 0.2);
              color: #8892b0;
            }
          }

          .stat-info {
            .stat-value {
              font-size: 20px;
              font-weight: bold;
              color: #ffffff;
              line-height: 1;
            }

            .stat-label {
              font-size: 12px;
              color: #8892b0;
              margin-top: 2px;
            }
          }
        }
      }

      .trend-chart {
        h4 {
          margin: 0 0 15px 0;
          color: #ffffff;
          font-size: 14px;
        }

        .chart-container {
          height: 200px;
          background: rgba(255, 255, 255, 0.05);
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #8892b0;
        }
      }
    }
  }

  @keyframes pulse {
    0% {
      box-shadow: 0 0 0 0 rgba(255, 255, 255, 0.7);
    }
    70% {
      box-shadow: 0 0 0 10px rgba(255, 255, 255, 0);
    }
    100% {
      box-shadow: 0 0 0 0 rgba(255, 255, 255, 0);
    }
  }
}
</style>
