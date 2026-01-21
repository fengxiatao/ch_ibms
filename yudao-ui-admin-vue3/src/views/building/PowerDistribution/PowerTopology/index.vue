<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧建筑</el-breadcrumb-item>
        <el-breadcrumb-item>变配电</el-breadcrumb-item>
        <el-breadcrumb-item>电力拓扑</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon total">
                <el-icon><Connection /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ topologyStats.totalNodes }}</div>
                <div class="stats-label">总节点数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon online">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ topologyStats.onlineNodes }}</div>
                <div class="stats-label">在线节点</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon load">
                <el-icon><DataAnalysis /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ topologyStats.totalLoad }}kW</div>
                <div class="stats-label">总负载</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon warning">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ topologyStats.alarmCount }}</div>
                <div class="stats-label">告警数量</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button-group>
          <el-button :type="viewMode === 'topology' ? 'primary' : 'default'" @click="viewMode = 'topology'">
            <el-icon><Share /></el-icon>
            拓扑视图
          </el-button>
          <el-button :type="viewMode === 'list' ? 'primary' : 'default'" @click="viewMode = 'list'">
            <el-icon><List /></el-icon>
            列表视图
          </el-button>
        </el-button-group>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="success" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <!-- 拓扑视图 -->
    <el-card v-if="viewMode === 'topology'" class="topology-container">
      <template #header>
        <div class="card-header">
          <span>电力系统拓扑图</span>
          <div class="header-tools">
            <el-button size="small" @click="handleZoomIn">
              <el-icon><ZoomIn /></el-icon>
              放大
            </el-button>
            <el-button size="small" @click="handleZoomOut">
              <el-icon><ZoomOut /></el-icon>
              缩小
            </el-button>
            <el-button size="small" @click="handleResetZoom">
              <el-icon><Aim /></el-icon>
              重置
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="topology-content">
        <div class="topology-legend">
          <div class="legend-item">
            <div class="legend-icon source"></div>
            <span>电源</span>
          </div>
          <div class="legend-item">
            <div class="legend-icon transformer"></div>
            <span>变压器</span>
          </div>
          <div class="legend-item">
            <div class="legend-icon switch"></div>
            <span>开关</span>
          </div>
          <div class="legend-item">
            <div class="legend-icon load"></div>
            <span>负载</span>
          </div>
        </div>
        
        <div id="topology-canvas" class="topology-canvas">
          <!-- 拓扑图将在这里渲染 -->
          <svg width="100%" height="600">
            <!-- 主电源 -->
            <g class="node source" transform="translate(100,50)">
              <rect width="80" height="60" rx="5" fill="#409eff" />
              <text x="40" y="35" text-anchor="middle" fill="white">主电源</text>
              <circle cx="40" cy="70" r="5" fill="#67c23a" />
            </g>
            
            <!-- 主变压器 -->
            <g class="node transformer" transform="translate(300,50)">
              <polygon points="0,30 40,0 80,30 40,60" fill="#e6a23c" />
              <text x="40" y="35" text-anchor="middle" fill="white">T1</text>
              <circle cx="40" cy="70" r="5" fill="#67c23a" />
            </g>
            
            <!-- 高压开关柜 -->
            <g class="node switch" transform="translate(500,50)">
              <rect width="80" height="60" rx="5" fill="#909399" />
              <text x="40" y="35" text-anchor="middle" fill="white">高压柜</text>
              <circle cx="40" cy="70" r="5" fill="#67c23a" />
            </g>
            
            <!-- 低压配电柜 -->
            <g class="node switch" transform="translate(250,200)">
              <rect width="80" height="60" rx="5" fill="#909399" />
              <text x="40" y="35" text-anchor="middle" fill="white">低压柜1</text>
              <circle cx="40" cy="70" r="5" fill="#67c23a" />
            </g>
            
            <g class="node switch" transform="translate(450,200)">
              <rect width="80" height="60" rx="5" fill="#909399" />
              <text x="40" y="35" text-anchor="middle" fill="white">低压柜2</text>
              <circle cx="40" cy="70" r="5" fill="#67c23a" />
            </g>
            
            <!-- 负载设备 -->
            <g class="node load" transform="translate(150,350)">
              <circle r="30" fill="#f56c6c" />
              <text x="0" y="5" text-anchor="middle" fill="white">照明</text>
              <circle cx="0" cy="40" r="5" fill="#67c23a" />
            </g>
            
            <g class="node load" transform="translate(350,350)">
              <circle r="30" fill="#f56c6c" />
              <text x="0" y="5" text-anchor="middle" fill="white">空调</text>
              <circle cx="0" cy="40" r="5" fill="#67c23a" />
            </g>
            
            <g class="node load" transform="translate(550,350)">
              <circle r="30" fill="#f56c6c" />
              <text x="0" y="5" text-anchor="middle" fill="white">电梯</text>
              <circle cx="0" cy="40" r="5" fill="#67c23a" />
            </g>
            
            <!-- 连接线 -->
            <line x1="180" y1="80" x2="300" y2="80" stroke="#409eff" stroke-width="3" />
            <line x1="380" y1="80" x2="500" y2="80" stroke="#409eff" stroke-width="3" />
            <line x1="340" y1="110" x2="290" y2="200" stroke="#409eff" stroke-width="2" />
            <line x1="340" y1="110" x2="490" y2="200" stroke="#409eff" stroke-width="2" />
            <line x1="290" y1="260" x2="180" y2="320" stroke="#409eff" stroke-width="2" />
            <line x1="290" y1="260" x2="350" y2="320" stroke="#409eff" stroke-width="2" />
            <line x1="490" y1="260" x2="550" y2="320" stroke="#409eff" stroke-width="2" />
          </svg>
        </div>
      </div>
    </el-card>

    <!-- 列表视图 -->
    <el-card v-if="viewMode === 'list'">
      <template #header>
        <div class="card-header">
          <span>设备节点列表</span>
        </div>
      </template>
      
      <el-table :data="nodeList" border stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="nodeName" label="节点名称" min-width="120" />
        <el-table-column prop="nodeType" label="节点类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getNodeTypeColor(row.nodeType)">{{ getNodeTypeName(row.nodeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="voltage" label="电压等级" width="100" />
        <el-table-column prop="current" label="电流(A)" width="100" />
        <el-table-column prop="power" label="功率(kW)" width="100" />
        <el-table-column prop="status" label="运行状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'online' ? 'success' : 'danger'">
              {{ row.status === 'online' ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" min-width="120" />
        <el-table-column prop="lastUpdate" label="最后更新" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button type="warning" size="small" @click="handleMonitor(row)">
              <el-icon><Monitor /></el-icon>
              监控
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 节点详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="节点详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="节点名称">{{ currentNode?.nodeName }}</el-descriptions-item>
        <el-descriptions-item label="节点类型">{{ getNodeTypeName(currentNode?.nodeType) }}</el-descriptions-item>
        <el-descriptions-item label="电压等级">{{ currentNode?.voltage }}</el-descriptions-item>
        <el-descriptions-item label="额定功率">{{ currentNode?.ratedPower }}kW</el-descriptions-item>
        <el-descriptions-item label="当前电流">{{ currentNode?.current }}A</el-descriptions-item>
        <el-descriptions-item label="当前功率">{{ currentNode?.power }}kW</el-descriptions-item>
        <el-descriptions-item label="运行状态">
          <el-tag :type="currentNode?.status === 'online' ? 'success' : 'danger'">
            {{ currentNode?.status === 'online' ? '在线' : '离线' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="安装位置">{{ currentNode?.location }}</el-descriptions-item>
        <el-descriptions-item label="制造商">{{ currentNode?.manufacturer }}</el-descriptions-item>
        <el-descriptions-item label="型号规格">{{ currentNode?.model }}</el-descriptions-item>
        <el-descriptions-item label="投运时间">{{ currentNode?.commissionDate }}</el-descriptions-item>
        <el-descriptions-item label="最后更新">{{ currentNode?.lastUpdate }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Connection, CircleCheck, DataAnalysis, Warning, Share, List, Refresh, Download,
  ZoomIn, ZoomOut, Aim, View, Monitor
} from '@element-plus/icons-vue'
import '@/styles/dark-theme.scss'

// 接口定义
interface TopologyNode {
  id: string
  nodeName: string
  nodeType: 'source' | 'transformer' | 'switch' | 'load'
  voltage: string
  current: number
  power: number
  ratedPower: number
  status: 'online' | 'offline'
  location: string
  manufacturer: string
  model: string
  commissionDate: string
  lastUpdate: string
}

// 响应式数据
const viewMode = ref('topology')
const detailDialogVisible = ref(false)
const currentNode = ref<TopologyNode | null>(null)

// 拓扑统计
const topologyStats = reactive({
  totalNodes: 12,
  onlineNodes: 11,
  totalLoad: 2850,
  alarmCount: 2
})

// 节点列表数据
const nodeList = ref<TopologyNode[]>([
  {
    id: '1',
    nodeName: '主电源',
    nodeType: 'source',
    voltage: '10kV',
    current: 285.5,
    power: 2850,
    ratedPower: 5000,
    status: 'online',
    location: '主配电室',
    manufacturer: '施耐德',
    model: 'MVS-12',
    commissionDate: '2023-01-15',
    lastUpdate: '2024-01-22 14:30:00'
  },
  {
    id: '2',
    nodeName: '主变压器T1',
    nodeType: 'transformer',
    voltage: '10kV/0.4kV',
    current: 142.8,
    power: 1425,
    ratedPower: 2500,
    status: 'online',
    location: '变压器室',
    manufacturer: 'ABB',
    model: 'ONAN-2500',
    commissionDate: '2023-01-15',
    lastUpdate: '2024-01-22 14:30:00'
  },
  {
    id: '3',
    nodeName: '高压开关柜',
    nodeType: 'switch',
    voltage: '10kV',
    current: 285.5,
    power: 2850,
    ratedPower: 3150,
    status: 'online',
    location: '高压配电室',
    manufacturer: '西门子',
    model: '8DJH-12',
    commissionDate: '2023-01-15',
    lastUpdate: '2024-01-22 14:30:00'
  },
  {
    id: '4',
    nodeName: '低压配电柜1',
    nodeType: 'switch',
    voltage: '0.4kV',
    current: 356.2,
    power: 890,
    ratedPower: 1600,
    status: 'online',
    location: '低压配电室A',
    manufacturer: '施耐德',
    model: 'Prisma-P',
    commissionDate: '2023-01-15',
    lastUpdate: '2024-01-22 14:30:00'
  },
  {
    id: '5',
    nodeName: '低压配电柜2',
    nodeType: 'switch',
    voltage: '0.4kV',
    current: 213.7,
    power: 535,
    ratedPower: 1600,
    status: 'online',
    location: '低压配电室B',
    manufacturer: '施耐德',
    model: 'Prisma-P',
    commissionDate: '2023-01-15',
    lastUpdate: '2024-01-22 14:30:00'
  },
  {
    id: '6',
    nodeName: '照明负载',
    nodeType: 'load',
    voltage: '0.22kV',
    current: 318.2,
    power: 350,
    ratedPower: 800,
    status: 'online',
    location: '1-3层照明',
    manufacturer: '飞利浦',
    model: 'LED-Panel',
    commissionDate: '2023-02-01',
    lastUpdate: '2024-01-22 14:30:00'
  },
  {
    id: '7',
    nodeName: '空调负载',
    nodeType: 'load',
    voltage: '0.38kV',
    current: 131.6,
    power: 450,
    ratedPower: 1200,
    status: 'online',
    location: '中央空调系统',
    manufacturer: '大金',
    model: 'VRV-X',
    commissionDate: '2023-02-01',
    lastUpdate: '2024-01-22 14:30:00'
  },
  {
    id: '8',
    nodeName: '电梯负载',
    nodeType: 'load',
    voltage: '0.38kV',
    current: 39.5,
    power: 85,
    ratedPower: 200,
    status: 'offline',
    location: '1-4号电梯',
    manufacturer: '奥的斯',
    model: 'Gen2-MRL',
    commissionDate: '2023-02-15',
    lastUpdate: '2024-01-22 13:45:00'
  }
])

// 方法
const getNodeTypeColor = (type: string) => {
  const colors = {
    source: 'success',
    transformer: 'warning',
    switch: 'info',
    load: 'danger'
  }
  return colors[type] || 'info'
}

const getNodeTypeName = (type: string) => {
  const names = {
    source: '电源',
    transformer: '变压器',
    switch: '开关',
    load: '负载'
  }
  return names[type] || '未知'
}

const handleRefresh = () => {
  ElMessage.success('数据刷新成功')
}

const handleExport = () => {
  ElMessage.success('导出功能开发中')
}

const handleZoomIn = () => {
  ElMessage.info('放大功能开发中')
}

const handleZoomOut = () => {
  ElMessage.info('缩小功能开发中')
}

const handleResetZoom = () => {
  ElMessage.info('重置缩放功能开发中')
}

const handleViewDetail = (row: TopologyNode) => {
  currentNode.value = row
  detailDialogVisible.value = true
}

const handleMonitor = (row: TopologyNode) => {
  ElMessage.info(`开始监控节点：${row.nodeName}`)
}

onMounted(() => {
  // 组件挂载后的初始化操作
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

.breadcrumb-container {
  margin-bottom: 20px;
}

.stats-cards {
  margin-bottom: 20px;
  
  .stats-card {
    .stats-content {
      display: flex;
      align-items: center;
      
      .stats-icon {
        width: 60px;
        height: 60px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 15px;
        font-size: 24px;
        
        &.total {
          background: linear-gradient(45deg, #409eff, #66b3ff);
          color: white;
        }
        
        &.online {
          background: linear-gradient(45deg, #67c23a, #85ce61);
          color: white;
        }
        
        &.load {
          background: linear-gradient(45deg, #e6a23c, #ebb563);
          color: white;
        }
        
        &.warning {
          background: linear-gradient(45deg, #f56c6c, #f78989);
          color: white;
        }
      }
      
      .stats-info {
        .stats-value {
          font-size: 28px;
          font-weight: bold;
          color: #303133;
          line-height: 1;
        }
        
        .stats-label {
          font-size: 14px;
          color: #909399;
          margin-top: 5px;
        }
      }
    }
  }
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.topology-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .topology-content {
    .topology-legend {
      display: flex;
      gap: 20px;
      margin-bottom: 20px;
      padding: 10px;
      background: #f5f7fa;
      border-radius: 6px;
      
      .legend-item {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .legend-icon {
          width: 20px;
          height: 20px;
          border-radius: 3px;
          
          &.source {
            background: #409eff;
          }
          
          &.transformer {
            background: #e6a23c;
          }
          
          &.switch {
            background: #909399;
          }
          
          &.load {
            background: #f56c6c;
          }
        }
      }
    }
    
    .topology-canvas {
      border: 1px solid #dcdfe6;
      border-radius: 6px;
      background: #fff;
      overflow: auto;
      
      svg {
        .node {
          cursor: pointer;
          
          &:hover {
            opacity: 0.8;
          }
          
          text {
            font-size: 12px;
            font-weight: bold;
          }
        }
      }
    }
  }
}
</style>

