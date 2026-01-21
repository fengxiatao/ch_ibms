<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <h2>人员轨迹</h2>
        <p>追踪和分析人员活动轨迹</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleExportTrack">
          <el-icon><Download /></el-icon>
          导出轨迹
        </el-button>
        <el-button type="success" @click="handlePlayback">
          <el-icon><VideoPlay /></el-icon>
          轨迹回放
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :model="searchForm" label-width="100px" :inline="true">
        <el-form-item label="查询人员">
          <el-select
            v-model="searchForm.personId"
            placeholder="请选择或搜索人员"
            filterable
            clearable
            style="width: 200px;"
          >
            <el-option
              v-for="person in personOptions"
              :key="person.value"
              :label="person.label"
              :value="person.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="查询时间">
          <el-date-picker
            v-model="searchForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="区域范围">
          <el-select v-model="searchForm.area" placeholder="请选择区域" clearable>
            <el-option
              v-for="area in areaOptions"
              :key="area.value"
              :label="area.label"
              :value="area.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询轨迹
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 轨迹展示区域 -->
    <div class="track-container">
      <el-row :gutter="20">
        <!-- 地图区域 -->
        <el-col :span="16">
          <el-card class="map-card">
            <template #header>
              <div class="card-header">
                <span>轨迹地图</span>
                <div class="map-controls">
                  <el-radio-group v-model="mapType" size="small">
                    <el-radio-button label="floor">楼层图</el-radio-button>
                    <el-radio-button label="3d">3D视图</el-radio-button>
                  </el-radio-group>
                  <el-select v-model="selectedFloor" placeholder="选择楼层" size="small" style="width: 120px; margin-left: 12px;">
                    <el-option label="1楼" value="1" />
                    <el-option label="2楼" value="2" />
                    <el-option label="3楼" value="3" />
                  </el-select>
                </div>
              </div>
            </template>
            <div class="map-container">
              <!-- 模拟地图显示 -->
              <div class="map-placeholder">
                <div class="track-map">
                  <!-- 轨迹路径 -->
                  <svg class="track-svg" viewBox="0 0 600 400">
                    <!-- 背景网格 -->
                    <defs>
                      <pattern id="grid" width="20" height="20" patternUnits="userSpaceOnUse">
                        <path d="M 20 0 L 0 0 0 20" fill="none" stroke="#e0e0e0" stroke-width="1"/>
                      </pattern>
                    </defs>
                    <rect width="100%" height="100%" fill="url(#grid)" />
                    
                    <!-- 轨迹线条 -->
                    <polyline
                      v-if="trackPath.length > 0"
                      :points="trackPath.map(p => `${p.x},${p.y}`).join(' ')"
                      fill="none"
                      stroke="#409eff"
                      stroke-width="3"
                      stroke-dasharray="5,5"
                    />
                    
                    <!-- 轨迹点 -->
                    <g v-for="(point, index) in trackPoints" :key="index">
                      <circle
                        :cx="point.x"
                        :cy="point.y"
                        :r="point.type === 'start' ? 8 : point.type === 'end' ? 8 : 5"
                        :fill="point.type === 'start' ? '#67c23a' : point.type === 'end' ? '#f56c6c' : '#409eff'"
                        stroke="#fff"
                        stroke-width="2"
                      />
                      <text
                        :x="point.x"
                        :y="point.y - 15"
                        text-anchor="middle"
                        font-size="12"
                        fill="#303133"
                      >
                        {{ point.label }}
                      </text>
                    </g>
                  </svg>
                </div>
                
                <!-- 轨迹信息面板 -->
                <div v-if="selectedPerson" class="track-info-panel">
                  <div class="person-info">
                    <el-avatar :src="selectedPerson.avatar" :size="40">
                      {{ selectedPerson.name.charAt(0) }}
                    </el-avatar>
                    <div class="person-details">
                      <div class="person-name">{{ selectedPerson.name }}</div>
                      <div class="person-meta">{{ selectedPerson.department }}</div>
                    </div>
                  </div>
                  <div class="track-stats">
                    <div class="stat-item">
                      <span class="label">活动时长:</span>
                      <span class="value">{{ trackStats.duration }}</span>
                    </div>
                    <div class="stat-item">
                      <span class="label">轨迹点数:</span>
                      <span class="value">{{ trackStats.pointCount }}</span>
                    </div>
                    <div class="stat-item">
                      <span class="label">覆盖区域:</span>
                      <span class="value">{{ trackStats.areaCount }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <!-- 轨迹详情 -->
        <el-col :span="8">
          <el-card class="detail-card">
            <template #header>
              <span>轨迹详情</span>
            </template>
            <div class="track-timeline">
              <el-timeline>
                <el-timeline-item
                  v-for="item in trackTimeline"
                  :key="item.id"
                  :timestamp="item.time"
                  :type="item.type"
                  :color="item.color"
                  placement="top"
                >
                  <div class="timeline-content">
                    <div class="location">{{ item.location }}</div>
                    <div class="camera">{{ item.camera }}</div>
                    <div class="action" v-if="item.action">{{ item.action }}</div>
                  </div>
                </el-timeline-item>
              </el-timeline>
              
              <div v-if="trackTimeline.length === 0" class="no-track">
                <el-icon><Warning /></el-icon>
                <p>暂无轨迹数据</p>
                <p class="tip">请选择人员和时间范围进行查询</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 轨迹分析 -->
    <div class="analysis-container">
      <el-card>
        <template #header>
          <span>轨迹分析</span>
        </template>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="analysis-chart">
              <h4>活动热力图</h4>
              <div class="chart-placeholder">
                <div class="heatmap-legend">
                  <span class="legend-item">
                    <span class="color-box low"></span>
                    <span>低频</span>
                  </span>
                  <span class="legend-item">
                    <span class="color-box medium"></span>
                    <span>中频</span>
                  </span>
                  <span class="legend-item">
                    <span class="color-box high"></span>
                    <span>高频</span>
                  </span>
                </div>
                <div class="heatmap-grid">
                  <div
                    v-for="(cell, index) in heatmapData"
                    :key="index"
                    class="heatmap-cell"
                    :class="cell.level"
                    :title="`区域${index + 1}: ${cell.count}次访问`"
                  ></div>
                </div>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="analysis-chart">
              <h4>停留时长统计</h4>
              <div class="duration-chart">
                <div
                  v-for="area in durationStats"
                  :key="area.name"
                  class="duration-bar"
                >
                  <div class="area-name">{{ area.name }}</div>
                  <div class="bar-container">
                    <div
                      class="bar"
                      :style="{ width: `${(area.duration / maxDuration) * 100}%` }"
                    ></div>
                    <span class="duration-text">{{ area.duration }}分钟</span>
                  </div>
                </div>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="analysis-chart">
              <h4>频繁路径</h4>
              <div class="frequent-paths">
                <div
                  v-for="path in frequentPaths"
                  :key="path.id"
                  class="path-item"
                >
                  <div class="path-route">
                    <span class="start">{{ path.start }}</span>
                    <el-icon><Right /></el-icon>
                    <span class="end">{{ path.end }}</span>
                  </div>
                  <div class="path-stats">
                    <span class="frequency">{{ path.frequency }}次</span>
                    <span class="avg-time">平均{{ path.avgTime }}分钟</span>
                  </div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, VideoPlay, Search, Refresh, Warning, Right } from '@element-plus/icons-vue'

// 响应式数据
const mapType = ref('floor')
const selectedFloor = ref('1')
const selectedPerson = ref(null)

// 搜索表单
const searchForm = reactive({
  personId: '',
  timeRange: null,
  area: ''
})

// 轨迹统计
const trackStats = reactive({
  duration: '2小时35分钟',
  pointCount: 24,
  areaCount: 8
})

// 选项数据
const personOptions = ref([
  { label: '张三 - 技术部', value: 'person_1' },
  { label: '李四 - 市场部', value: 'person_2' },
  { label: '王五 - 财务部', value: 'person_3' },
  { label: '赵六 - 人事部', value: 'person_4' }
])

const areaOptions = ref([
  { label: '全部区域', value: '' },
  { label: '办公区域', value: 'office' },
  { label: '会议区域', value: 'meeting' },
  { label: '公共区域', value: 'public' },
  { label: '停车区域', value: 'parking' }
])

// 轨迹路径数据
const trackPath = ref([
  { x: 50, y: 100 },
  { x: 150, y: 120 },
  { x: 250, y: 80 },
  { x: 350, y: 150 },
  { x: 450, y: 100 },
  { x: 550, y: 180 }
])

// 轨迹点数据
const trackPoints = ref([
  { x: 50, y: 100, type: 'start', label: '入口' },
  { x: 150, y: 120, type: 'normal', label: '电梯' },
  { x: 250, y: 80, type: 'normal', label: '办公室' },
  { x: 350, y: 150, type: 'normal', label: '会议室' },
  { x: 450, y: 100, type: 'normal', label: '茶水间' },
  { x: 550, y: 180, type: 'end', label: '停车场' }
])

// 轨迹时间线
const trackTimeline = ref([
  {
    id: '1',
    time: '09:00:00',
    location: '主楼大厅',
    camera: '大门摄像头01',
    action: '进入',
    type: 'success',
    color: '#67c23a'
  },
  {
    id: '2',
    time: '09:05:30',
    location: '电梯厅',
    camera: '电梯摄像头01',
    action: '等待电梯',
    type: 'primary',
    color: '#409eff'
  },
  {
    id: '3',
    time: '09:08:15',
    location: '3楼办公区',
    camera: '办公区摄像头03',
    action: '到达工位',
    type: 'info',
    color: '#909399'
  },
  {
    id: '4',
    time: '10:30:00',
    location: '会议室A',
    camera: '会议室摄像头01',
    action: '参加会议',
    type: 'warning',
    color: '#e6a23c'
  },
  {
    id: '5',
    time: '11:45:20',
    location: '茶水间',
    camera: '公共区摄像头02',
    action: '短暂停留',
    type: 'info',
    color: '#909399'
  },
  {
    id: '6',
    time: '18:20:00',
    location: '地下停车场',
    camera: '停车场摄像头01',
    action: '离开',
    type: 'danger',
    color: '#f56c6c'
  }
])

// 热力图数据
const heatmapData = ref([
  { level: 'high', count: 35 },
  { level: 'medium', count: 18 },
  { level: 'low', count: 5 },
  { level: 'medium', count: 22 },
  { level: 'high', count: 42 },
  { level: 'low', count: 8 },
  { level: 'medium', count: 15 },
  { level: 'high', count: 38 },
  { level: 'low', count: 3 },
  { level: 'medium', count: 25 },
  { level: 'high', count: 31 },
  { level: 'low', count: 12 }
])

// 停留时长统计
const durationStats = ref([
  { name: '办公区', duration: 420 },
  { name: '会议室', duration: 180 },
  { name: '休息区', duration: 45 },
  { name: '茶水间', duration: 25 },
  { name: '卫生间', duration: 15 }
])

// 频繁路径
const frequentPaths = ref([
  { id: '1', start: '大厅', end: '办公区', frequency: 28, avgTime: 5 },
  { id: '2', start: '办公区', end: '会议室', frequency: 15, avgTime: 8 },
  { id: '3', start: '会议室', end: '休息区', frequency: 12, avgTime: 3 },
  { id: '4', start: '办公区', end: '茶水间', frequency: 22, avgTime: 2 }
])

// 计算最大停留时长
const maxDuration = computed(() => {
  return Math.max(...durationStats.value.map(item => item.duration))
})

// 事件处理
const handleSearch = () => {
  if (!searchForm.personId) {
    ElMessage.warning('请选择要查询的人员')
    return
  }
  
  if (!searchForm.timeRange) {
    ElMessage.warning('请选择查询时间范围')
    return
  }
  
  // 设置选中人员信息
  const person = personOptions.value.find(p => p.value === searchForm.personId)
  if (person) {
    selectedPerson.value = {
      name: person.label.split(' - ')[0],
      department: person.label.split(' - ')[1],
      avatar: ''
    }
  }
  
  ElMessage.success('轨迹查询完成')
}

const handleReset = () => {
  Object.assign(searchForm, {
    personId: '',
    timeRange: null,
    area: ''
  })
  selectedPerson.value = null
}

const handleExportTrack = () => {
  if (!selectedPerson.value) {
    ElMessage.warning('请先查询轨迹数据')
    return
  }
  ElMessage.info('轨迹导出功能开发中...')
}

const handlePlayback = () => {
  if (!selectedPerson.value) {
    ElMessage.warning('请先查询轨迹数据')
    return
  }
  ElMessage.info('轨迹回放功能开发中...')
}

onMounted(() => {
  // 组件挂载后的初始化操作
})
</script>

<style scoped lang="scss">@use '@/styles/dark-theme.scss';

.app-container {
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 20px;
    background: #1a1a1a;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .header-title {
      h2 {
        margin: 0 0 8px 0;
        color: #303133;
        font-size: 24px;
      }

      p {
        margin: 0;
        color: #909399;
        font-size: 14px;
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .search-container {
    background: #1a1a1a;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    margin-bottom: 20px;

    .el-form-item {
      margin-bottom: 16px;
    }
  }

  .track-container {
    margin-bottom: 20px;

    .map-card {
      height: 500px;

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .map-controls {
          display: flex;
          align-items: center;
        }
      }

      .map-container {
        height: 420px;
        position: relative;

        .map-placeholder {
          width: 100%;
          height: 100%;
          background: #f8f9fa;
          border-radius: 8px;
          position: relative;

          .track-map {
            width: 100%;
            height: 100%;

            .track-svg {
              width: 100%;
              height: 100%;
            }
          }

          .track-info-panel {
            position: absolute;
            top: 20px;
            right: 20px;
            width: 200px;
            background: rgba(255, 255, 255, 0.95);
            border-radius: 8px;
            padding: 16px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

            .person-info {
              display: flex;
              align-items: center;
              gap: 12px;
              margin-bottom: 16px;

              .person-details {
                .person-name {
                  font-weight: bold;
                  color: #303133;
                }

                .person-meta {
                  font-size: 12px;
                  color: #909399;
                }
              }
            }

            .track-stats {
              .stat-item {
                display: flex;
                justify-content: space-between;
                margin-bottom: 8px;
                font-size: 12px;

                .label {
                  color: #909399;
                }

                .value {
                  color: #303133;
                  font-weight: bold;
                }
              }
            }
          }
        }
      }
    }

    .detail-card {
      height: 500px;

      .track-timeline {
        height: 420px;
        overflow-y: auto;

        .timeline-content {
          .location {
            font-weight: bold;
            color: #303133;
            margin-bottom: 4px;
          }

          .camera {
            font-size: 12px;
            color: #909399;
            margin-bottom: 2px;
          }

          .action {
            font-size: 12px;
            color: #409eff;
          }
        }

        .no-track {
          text-align: center;
          color: #909399;
          padding: 60px 0;

          .el-icon {
            font-size: 48px;
            margin-bottom: 16px;
          }

          p {
            margin: 8px 0;
          }

          .tip {
            font-size: 12px;
          }
        }
      }
    }
  }

  .analysis-container {
    .analysis-chart {
      h4 {
        margin: 0 0 16px 0;
        color: #303133;
        font-size: 16px;
      }

      .chart-placeholder {
        .heatmap-legend {
          display: flex;
          gap: 16px;
          margin-bottom: 12px;
          font-size: 12px;

          .legend-item {
            display: flex;
            align-items: center;
            gap: 4px;

            .color-box {
              width: 12px;
              height: 12px;
              border-radius: 2px;

              &.low {
                background: #e8f4fd;
              }

              &.medium {
                background: #7ec8f3;
              }

              &.high {
                background: #409eff;
              }
            }
          }
        }

        .heatmap-grid {
          display: grid;
          grid-template-columns: repeat(4, 1fr);
          gap: 4px;

          .heatmap-cell {
            aspect-ratio: 1;
            border-radius: 4px;
            cursor: pointer;
            transition: all 0.3s;

            &.low {
              background: #e8f4fd;
            }

            &.medium {
              background: #7ec8f3;
            }

            &.high {
              background: #409eff;
            }

            &:hover {
              transform: scale(1.1);
            }
          }
        }
      }

      .duration-chart {
        .duration-bar {
          margin-bottom: 12px;

          .area-name {
            font-size: 12px;
            color: #303133;
            margin-bottom: 4px;
          }

          .bar-container {
            position: relative;
            height: 20px;
            background: #f0f0f0;
            border-radius: 10px;
            overflow: hidden;

            .bar {
              height: 100%;
              background: linear-gradient(90deg, #409eff, #67c23a);
              border-radius: 10px;
              transition: width 0.3s ease;
            }

            .duration-text {
              position: absolute;
              right: 8px;
              top: 50%;
              transform: translateY(-50%);
              font-size: 10px;
              color: #303133;
            }
          }
        }
      }

      .frequent-paths {
        .path-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 8px 0;
          border-bottom: 1px solid #f0f0f0;

          &:last-child {
            border-bottom: none;
          }

          .path-route {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 12px;

            .start,
            .end {
              color: #303133;
            }

            .el-icon {
              color: #909399;
            }
          }

          .path-stats {
            text-align: right;
            font-size: 10px;
            color: #909399;

            .frequency {
              display: block;
              color: #409eff;
              font-weight: bold;
            }

            .avg-time {
              display: block;
            }
          }
        }
      }
    }
  }
}
</style>






