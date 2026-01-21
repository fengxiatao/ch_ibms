<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="terminal-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <Icon icon="ep:headphones" :size="24" />
        <h1>终端管理</h1>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <Icon icon="ep:plus" />
          新增
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item>
          <el-select
            v-model="searchForm.system"
            placeholder="请选择专业系统"
            clearable
            style="width: 180px"
          >
            <el-option label="广播系统" value="broadcast" />
            <el-option label="音响系统" value="audio" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select
            v-model="searchForm.runStatus"
            placeholder="请选择运行状态"
            clearable
            style="width: 180px"
          >
            <el-option label="在线" value="online" />
            <el-option label="离线" value="offline" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="searchForm.deviceName"
            placeholder="请输入设备名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleMoreFilters">
            更多筛选
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 功放管理内容 -->
    <div class="terminal-section">
      <div class="section-header">
        <h2>功放管理</h2>
        <div class="view-controls">
          <el-radio-group v-model="viewMode" @change="handleViewChange">
            <el-radio-button value="card">
              <Icon icon="ep:grid" />
            </el-radio-button>
            <el-radio-button value="list">
              <Icon icon="ep:menu" />
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- 卡片视图 -->
      <div v-if="viewMode === 'card'" class="terminal-cards">
        <div
          v-for="terminal in terminalList"
          :key="terminal.id"
          class="terminal-card"
          :class="{ 'status-error': terminal.status === '故障' }"
        >
          <div class="card-header">
            <div class="device-icon">
              <Icon 
                :icon="terminal.status === '在线' ? 'ep:headphones' : 'ep:warning-filled'" 
                :size="32" 
                :color="getStatusColor(terminal.status)"
              />
            </div>
            <div class="device-status">
              <el-tag :type="getStatusType(terminal.status)" size="small">
                {{ terminal.status }}
              </el-tag>
            </div>
          </div>
          
          <div class="card-content">
            <h3 class="device-name">{{ terminal.deviceName }}</h3>
            <div class="device-info">
              <div class="info-item">
                <Icon icon="ep:location" />
                <span>空间位置：{{ terminal.location }}</span>
              </div>
              <div class="info-item">
                <Icon icon="ep:cpu" />
                <span>占用状态：{{ terminal.occupyStatus }}</span>
              </div>
              <div class="info-item">
                <Icon icon="ep:data-analysis" />
                <span>当前音量：{{ terminal.currentVolume }}</span>
              </div>
            </div>
            <div class="card-actions">
              <el-button link type="primary" @click="handleControl(terminal)">
                控制
              </el-button>
              <el-button link type="primary" @click="handleEdit(terminal)">
                编辑
              </el-button>
              <el-button link type="danger" @click="handleDelete(terminal)">
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 3D空间地图视图 -->
      <div v-else class="space-map-container">
        <div class="map-tabs">
          <el-tabs v-model="activeMapTab">
            <el-tab-pane label="电子地图" name="electronic">
              <div class="electronic-map">
                <!-- 广播媒体系统空间运行状态 -->
                <div class="system-status-panel">
                  <h3>广播媒体系统空间运行状态</h3>
                  <div class="status-items">
                    <div class="status-item">
                      <div class="status-indicator success"></div>
                      <span class="status-count">14</span>
                      <span class="status-label">在线</span>
                    </div>
                    <div class="status-item">
                      <div class="status-indicator success"></div>
                      <span class="status-count">1</span>
                      <span class="status-label">在线</span>
                    </div>
                    <div class="status-item">
                      <div class="status-indicator neutral"></div>
                      <span class="status-count">1</span>
                      <span class="status-label">离线</span>
                    </div>
                    <div class="status-item">
                      <div class="status-indicator warning"></div>
                      <span class="status-count">1</span>
                      <span class="status-label">警告</span>
                    </div>
                    <div class="status-item">
                      <div class="status-indicator error"></div>
                      <span class="status-count">1</span>
                      <span class="status-label">故障</span>
                    </div>
                  </div>
                </div>

                <!-- 3D楼层视图 -->
                <div class="floor-view">
                  <div class="floor-image">
                    <!-- 这里应该是3D楼层图，现在用占位符代替 -->
                    <div class="floor-placeholder">
                      <div class="device-points">
                        <!-- 设备点位 -->
                        <div 
                          v-for="device in devicePoints" 
                          :key="device.id"
                          class="device-point"
                          :class="device.status"
                          :style="{ left: device.x + '%', top: device.y + '%' }"
                          @click="handleDeviceClick(device)"
                        >
                          <div class="point-indicator"></div>
                          <div class="device-tooltip">{{ device.name }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <!-- 控制面板 -->
                  <div class="floor-controls">
                    <el-button @click="zoomIn">
                      <Icon icon="ep:zoom-in" />
                    </el-button>
                    <el-button @click="zoomOut">
                      <Icon icon="ep:zoom-out" />
                    </el-button>
                  </div>
                </div>

                <!-- 媒体体系数据类型统计 -->
                <div class="media-stats-panel">
                  <h3>媒体体系数据类型统计</h3>
                  <div class="stats-chart">
                    <div class="chart-placeholder">
                      <div class="chart-value">0</div>
                      <div class="chart-label">个</div>
                    </div>
                  </div>
                </div>

                <!-- 广播媒体系统空间占用率 -->
                <div class="occupancy-panel">
                  <h3>广播媒体系统空间占用率</h3>
                  <div class="occupancy-stats">
                    <div class="occupancy-item">
                      <span class="occupancy-label">占用</span>
                      <span class="occupancy-count">2</span>
                      <span class="occupancy-unit">台</span>
                      <div class="occupancy-bar">
                        <div class="bar-fill" style="width: 33.33%"></div>
                      </div>
                      <span class="occupancy-percent">33.33%</span>
                    </div>
                    <div class="occupancy-item">
                      <span class="occupancy-label">空闲</span>
                      <span class="occupancy-count">4</span>
                      <span class="occupancy-unit">台</span>
                    </div>
                  </div>
                </div>

                <!-- 实时播放任务列表 -->
                <div class="task-list-panel">
                  <h3>实时播放任务列表</h3>
                  <div class="task-list-empty">
                    <Icon icon="ep:folder-opened" :size="60" color="#909399" />
                    <p>暂无数据</p>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="卡片列表" name="card">
              <!-- 这里可以显示卡片列表视图 -->
              <div class="card-list-view">
                <div class="terminal-cards">
                  <div
                    v-for="terminal in terminalList"
                    :key="terminal.id"
                    class="terminal-card small"
                  >
                    <div class="card-content">
                      <h4>{{ terminal.deviceName }}</h4>
                      <p>{{ terminal.location }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="viewMode === 'card'" class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handlePageSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 设备控制弹窗 -->
    <el-dialog
      v-model="controlDialogVisible"
      title="设备控制"
      width="600px"
      destroy-on-close
    >
      <div class="control-panel">
        <div class="device-info">
          <h3>{{ currentDevice.deviceName }}</h3>
          <p>设备编码：{{ currentDevice.deviceCode }}</p>
          <p>专业系统：{{ currentDevice.system }}</p>
          <p>设备编码：{{ currentDevice.deviceCode }}</p>
          <p>空间位置：{{ currentDevice.location }}</p>
          <p>物联设备：{{ currentDevice.iotDevice ? '是' : '否' }}</p>
          <p>运行状态：<el-tag :type="getStatusType(currentDevice.status)" size="small">{{ currentDevice.status }}</el-tag></p>
        </div>
        
        <div class="control-actions">
          <el-tabs v-model="activeControlTab">
            <el-tab-pane label="运行信息" name="runtime">
              <div class="runtime-info">
                <div class="info-grid">
                  <div class="info-item">
                    <label>在线状态：</label>
                    <span>-</span>
                  </div>
                  <div class="info-item">
                    <label>占用状态：</label>
                    <span>-</span>
                  </div>
                  <div class="info-item">
                    <label>当前音量(%)：</label>
                    <span>-</span>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="操作日志" name="logs">
              <div class="operation-logs">
                <p class="no-data">暂无数据</p>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="历史数据" name="history">
              <div class="history-data">
                <p class="no-data">暂无数据</p>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="更多信息" name="more">
              <div class="more-info">
                <p class="no-data">暂无数据</p>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
        
        <!-- 音量控制 -->
        <div class="volume-control">
          <h4>音量控制</h4>
          <div class="volume-slider">
            <el-slider
              v-model="volumeValue"
              :min="0"
              :max="100"
              show-tooltip
            />
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="controlDialogVisible = false">关闭</el-button>
          <el-button type="danger" @click="handleCloseDevice">关停</el-button>
        </div>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Icon } from '@/components/Icon'

interface Terminal {
  id: string
  deviceName: string
  deviceCode: string
  system: string
  location: string
  status: string
  occupyStatus: string
  currentVolume: string
  iotDevice: boolean
}

// 响应式数据
const loading = ref(false)
const controlDialogVisible = ref(false)
const viewMode = ref('card')
const activeMapTab = ref('electronic')
const activeControlTab = ref('runtime')
const volumeValue = ref(50)

const searchForm = reactive({
  system: '',
  runStatus: '',
  deviceName: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 14
})

const currentDevice = ref<Terminal>({
  id: '',
  deviceName: '',
  deviceCode: '',
  system: '',
  location: '',
  status: '',
  occupyStatus: '',
  currentVolume: '',
  iotDevice: false
})

// 终端列表数据
const terminalList = ref<Terminal[]>([
  { id: '1', deviceName: '广播010', deviceCode: 'MT_GB_GBZD_0014', system: '多媒体专业_广播系统_广播终端', location: '高区_科技研发楼_9F_办公区', status: '在线', occupyStatus: '-', currentVolume: '-', iotDevice: true },
  { id: '2', deviceName: '广播007', deviceCode: 'MT_GB_GBZD_0013', system: '多媒体专业_广播系统_广播终端', location: '高区_科技研发楼_9F_办公区', status: '在线', occupyStatus: '-', currentVolume: '-', iotDevice: true },
  { id: '3', deviceName: '广播005', deviceCode: 'MT_GB_GBZD_0012', system: '多媒体专业_广播系统_广播终端', location: '高区_科技研发楼_9F_办公区', status: '在线', occupyStatus: '-', currentVolume: '-', iotDevice: true },
  { id: '4', deviceName: '广播012', deviceCode: 'MT_GB_GBZD_0011', system: '多媒体专业_广播系统_广播终端', location: '高区_科技研发楼_9F_办公区', status: '在线', occupyStatus: '-', currentVolume: '-', iotDevice: true },
  { id: '5', deviceName: '广播001', deviceCode: 'MT_GB_GBZD_0010', system: '多媒体专业_广播系统_广播终端', location: '高区_科技研发楼_9F_办公区', status: '在线', occupyStatus: '-', currentVolume: '-', iotDevice: true },
  { id: '6', deviceName: '广播002', deviceCode: 'MT_GB_GBZD_0009', system: '多媒体专业_广播系统_广播终端', location: '高区_科技研发楼_9F_办公区', status: '故障', occupyStatus: '-', currentVolume: '-', iotDevice: true },
  { id: '7', deviceName: '广播008', deviceCode: 'MT_GB_GBZD_0008', system: '多媒体专业_广播系统_广播终端', location: '高区_科技研发楼_9F_办公区', status: '在线', occupyStatus: '-', currentVolume: '-', iotDevice: true },
  { id: '8', deviceName: '广播011', deviceCode: 'MT_GB_GBZD_0007', system: '多媒体专业_广播系统_广播终端', location: '高区_科技研发楼_9F_办公区', status: '在线', occupyStatus: '-', currentVolume: '-', iotDevice: true },
  { id: '9', deviceName: '广播013', deviceCode: 'MT_GB_GBZD_0006', system: '多媒体专业_广播系统_广播终端', location: '高区_科技研发楼_9F_办公区', status: '在线', occupyStatus: '-', currentVolume: '-', iotDevice: true },
  { id: '10', deviceName: '广播003', deviceCode: 'MT_GB_GBZD_0005', system: '多媒体专业_广播系统_广播终端', location: '高区_科技研发楼_9F_办公区', status: '故障', occupyStatus: '-', currentVolume: '-', iotDevice: true },
  { id: '11', deviceName: '广播009', deviceCode: 'MT_GB_GBZD_0004', system: '多媒体专业_广播系统_广播终端', location: '高区_科技研发楼_9F_办公区', status: '在线', occupyStatus: '-', currentVolume: '-', iotDevice: true },
  { id: '12', deviceName: '广播014', deviceCode: 'MT_GB_GBZD_0003', system: '多媒体专业_广播系统_广播终端', location: '高区_科技研发楼_9F_办公区', status: '在线', occupyStatus: '-', currentVolume: '-', iotDevice: true },
  { id: '13', deviceName: '广播006', deviceCode: 'MT_GB_GBZD_0002', system: '多媒体专业_广播系统_广播终端', location: '高区_科技研发楼_9F_办公区', status: '在线', occupyStatus: '-', currentVolume: '-', iotDevice: true },
  { id: '14', deviceName: '广播004', deviceCode: 'MT_GB_GBZD_0001', system: '多媒体专业_广播系统_广播终端', location: '高区_科技研发楼_9F_办公区', status: '在线', occupyStatus: '-', currentVolume: '-', iotDevice: true }
])

// 3D地图中的设备点位
const devicePoints = ref([
  { id: '1', name: '广播010', status: 'online', x: 20, y: 30 },
  { id: '2', name: '广播007', status: 'online', x: 40, y: 25 },
  { id: '3', name: '广播005', status: 'online', x: 60, y: 40 },
  { id: '4', name: '广播012', status: 'error', x: 80, y: 35 },
  { id: '5', name: '广播001', status: 'online', x: 25, y: 60 },
  { id: '6', name: '广播002', status: 'warning', x: 70, y: 70 }
])

// 工具函数
const getStatusType = (status: string) => {
  switch (status) {
    case '在线':
      return 'success'
    case '离线':
      return 'info'
    case '故障':
      return 'danger'
    case '警告':
      return 'warning'
    default:
      return ''
  }
}

const getStatusColor = (status: string) => {
  switch (status) {
    case '在线':
      return '#67C23A'
    case '离线':
      return '#909399'
    case '故障':
      return '#F56C6C'
    case '警告':
      return '#E6A23C'
    default:
      return '#409EFF'
  }
}

// 事件处理
const handleMoreFilters = () => {
  ElMessage.success('更多筛选功能开发中...')
}

const handleViewChange = (mode: string) => {
  viewMode.value = mode
}

const handleCreate = () => {
  ElMessage.info('新增功能开发中...')
}

const handleControl = (terminal: Terminal) => {
  currentDevice.value = { ...terminal }
  controlDialogVisible.value = true
}

const handleEdit = (terminal: Terminal) => {
  ElMessage.info('编辑功能开发中...')
}

const handleDelete = (terminal: Terminal) => {
  ElMessageBox.confirm('确定要删除这个设备吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  })
}

const handleDeviceClick = (device: any) => {
  ElMessage.info(`点击了设备：${device.name}`)
}

const zoomIn = () => {
  ElMessage.info('放大功能开发中...')
}

const zoomOut = () => {
  ElMessage.info('缩小功能开发中...')
}

const handleCloseDevice = () => {
  ElMessage.success('设备关停成功')
  controlDialogVisible.value = false
}

const handlePageSizeChange = (size: number) => {
  pagination.size = size
  loadData()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  loadData()
}

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

// 生命周期
onMounted(() => {
  pagination.total = terminalList.value.length
  loadData()
})
</script>

<style scoped lang="scss">
.terminal-management {
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
    border: 1px solid rgba(0, 212, 255, 0.1);

    .header-title {
      display: flex;
      align-items: center;
      gap: 12px;

      .el-icon {
        color: #00d4ff;
      }

      h1 {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: #00d4ff;
      }
    }
  }

  .search-section {
    margin-bottom: 20px;
    padding: 20px;
    background: rgba(15, 23, 42, 0.7);
    border-radius: 8px;
    border: 1px solid rgba(0, 212, 255, 0.1);
  }

  .terminal-section {
    background: rgba(15, 23, 42, 0.7);
    border-radius: 8px;
    padding: 20px;
    border: 1px solid rgba(0, 212, 255, 0.1);

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      h2 {
        margin: 0;
        color: #00d4ff;
        font-size: 18px;
        font-weight: 600;
      }

      .view-controls {
        :deep(.el-radio-group) {
          .el-radio-button__inner {
            background: rgba(15, 23, 42, 0.7);
            border-color: rgba(0, 212, 255, 0.2);
            color: #e2e8f0;

            &:hover {
              color: #00d4ff;
            }
          }

          .el-radio-button__original-radio:checked + .el-radio-button__inner {
            background: #00d4ff;
            border-color: #00d4ff;
            color: #0f172a;
          }
        }
      }
    }

    .terminal-cards {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 20px;
      margin-bottom: 20px;

      .terminal-card {
        background: rgba(30, 58, 138, 0.3);
        border: 1px solid rgba(0, 212, 255, 0.2);
        border-radius: 8px;
        padding: 20px;
        transition: all 0.3s ease;

        &:hover {
          border-color: #00d4ff;
          background: rgba(30, 58, 138, 0.5);
          transform: translateY(-2px);
        }

        &.status-error {
          border-color: rgba(245, 108, 108, 0.5);
          background: rgba(245, 108, 108, 0.1);
        }

        &.small {
          padding: 16px;
          min-height: auto;
        }

        .card-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 16px;

          .device-icon {
            width: 48px;
            height: 48px;
            background: rgba(0, 212, 255, 0.1);
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
          }

          .device-status {
            flex-shrink: 0;
          }
        }

        .card-content {
          .device-name {
            margin: 0 0 12px 0;
            color: #00d4ff;
            font-size: 16px;
            font-weight: 600;
          }

          .device-info {
            margin-bottom: 16px;

            .info-item {
              display: flex;
              align-items: center;
              gap: 8px;
              margin-bottom: 8px;
              color: #e2e8f0;
              font-size: 14px;

              .el-icon {
                color: #00d4ff;
                font-size: 16px;
              }

              &:last-child {
                margin-bottom: 0;
              }
            }
          }

          .card-actions {
            display: flex;
            gap: 12px;
            justify-content: flex-end;
          }
        }
      }
    }

    .space-map-container {
      .electronic-map {
        display: grid;
        grid-template-columns: 250px 1fr 250px;
        grid-template-rows: auto 1fr auto;
        gap: 20px;
        height: 600px;

        .system-status-panel {
          grid-column: 1;
          grid-row: 1;
          background: rgba(30, 58, 138, 0.3);
          border-radius: 8px;
          padding: 16px;

          h3 {
            color: #00d4ff;
            margin: 0 0 16px 0;
            font-size: 14px;
          }

          .status-items {
            display: flex;
            flex-direction: column;
            gap: 8px;

            .status-item {
              display: flex;
              align-items: center;
              gap: 8px;
              font-size: 12px;

              .status-indicator {
                width: 12px;
                height: 12px;
                border-radius: 50%;

                &.success { background: #67C23A; }
                &.neutral { background: #909399; }
                &.warning { background: #E6A23C; }
                &.error { background: #F56C6C; }
              }

              .status-count {
                font-weight: bold;
                color: #00d4ff;
              }
            }
          }
        }

        .floor-view {
          grid-column: 2;
          grid-row: 1 / 4;
          position: relative;
          background: rgba(30, 58, 138, 0.1);
          border-radius: 8px;
          overflow: hidden;

          .floor-placeholder {
            width: 100%;
            height: 100%;
            background: linear-gradient(45deg, #1e3a8a 25%, transparent 25%), linear-gradient(-45deg, #1e3a8a 25%, transparent 25%), linear-gradient(45deg, transparent 75%, #1e3a8a 75%), linear-gradient(-45deg, transparent 75%, #1e3a8a 75%);
            background-size: 20px 20px;
            background-position: 0 0, 0 10px, 10px -10px, -10px 0px;
            position: relative;

            .device-points {
              position: absolute;
              top: 0;
              left: 0;
              width: 100%;
              height: 100%;

              .device-point {
                position: absolute;
                width: 12px;
                height: 12px;
                cursor: pointer;
                
                .point-indicator {
                  width: 100%;
                  height: 100%;
                  border-radius: 50%;
                  background: #67C23A;
                  
                  &.error { background: #F56C6C; }
                  &.warning { background: #E6A23C; }
                }

                .device-tooltip {
                  position: absolute;
                  bottom: 100%;
                  left: 50%;
                  transform: translateX(-50%);
                  background: rgba(0, 0, 0, 0.8);
                  color: white;
                  padding: 4px 8px;
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
          }

          .floor-controls {
            position: absolute;
            top: 20px;
            right: 20px;
            display: flex;
            flex-direction: column;
            gap: 8px;
          }
        }

        .media-stats-panel {
          grid-column: 3;
          grid-row: 1;
          background: rgba(30, 58, 138, 0.3);
          border-radius: 8px;
          padding: 16px;

          h3 {
            color: #00d4ff;
            margin: 0 0 16px 0;
            font-size: 14px;
          }

          .stats-chart {
            text-align: center;

            .chart-placeholder {
              .chart-value {
                font-size: 24px;
                font-weight: bold;
                color: #00d4ff;
              }

              .chart-label {
                font-size: 12px;
                color: #e2e8f0;
              }
            }
          }
        }

        .occupancy-panel {
          grid-column: 1;
          grid-row: 2;
          background: rgba(30, 58, 138, 0.3);
          border-radius: 8px;
          padding: 16px;

          h3 {
            color: #00d4ff;
            margin: 0 0 16px 0;
            font-size: 14px;
          }

          .occupancy-stats {
            .occupancy-item {
              display: flex;
              align-items: center;
              gap: 8px;
              margin-bottom: 12px;
              font-size: 12px;

              .occupancy-bar {
                width: 60px;
                height: 6px;
                background: rgba(0, 0, 0, 0.2);
                border-radius: 3px;
                overflow: hidden;

                .bar-fill {
                  height: 100%;
                  background: #E6A23C;
                  transition: width 0.3s;
                }
              }
            }
          }
        }

        .task-list-panel {
          grid-column: 3;
          grid-row: 2 / 4;
          background: rgba(30, 58, 138, 0.3);
          border-radius: 8px;
          padding: 16px;

          h3 {
            color: #00d4ff;
            margin: 0 0 16px 0;
            font-size: 14px;
          }

          .task-list-empty {
            text-align: center;
            padding: 40px 0;
            color: #909399;

            p {
              margin-top: 12px;
              font-size: 14px;
            }
          }
        }
      }
    }

    .pagination-section {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;

      :deep(.el-pagination) {
        .el-pager li, .btn-prev, .btn-next {
          background: rgba(15, 23, 42, 0.7);
          color: #e2e8f0;
          border: 1px solid rgba(0, 212, 255, 0.2);

          &:hover {
            color: #00d4ff;
          }

          &.is-active {
            background: #00d4ff;
            color: #0f172a;
          }
        }

        .el-pagination__total,
        .el-pagination__jump {
          color: #e2e8f0;
        }
      }
    }
  }

  .control-panel {
    .device-info {
      margin-bottom: 20px;
      padding: 16px;
      background: #f8fafc;
      border-radius: 6px;

      h3 {
        margin: 0 0 12px 0;
        color: #374151;
      }

      p {
        margin: 4px 0;
        color: #6b7280;
        font-size: 14px;
      }
    }

    .control-actions {
      margin-bottom: 20px;

      .runtime-info {
        .info-grid {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 16px;

          .info-item {
            display: flex;
            justify-content: space-between;

            label {
              font-weight: 500;
              color: #374151;
            }

            span {
              color: #6b7280;
            }
          }
        }
      }

      .operation-logs,
      .history-data,
      .more-info {
        .no-data {
          text-align: center;
          color: #909399;
          padding: 40px 0;
        }
      }
    }

    .volume-control {
      h4 {
        margin: 0 0 12px 0;
        color: #374151;
      }

      .volume-slider {
        padding: 0 12px;
      }
    }
  }
}

:deep(.el-dialog) {
  .el-dialog__header {
    background: #f8fafc;
    border-bottom: 1px solid #e5e7eb;
  }

  .el-dialog__title {
    color: #374151;
    font-weight: 600;
  }

  .el-dialog__body {
    padding: 24px;
  }

  .el-dialog__footer {
    background: #f8fafc;
    border-top: 1px solid #e5e7eb;
  }
}
</style>


