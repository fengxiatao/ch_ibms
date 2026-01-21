<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧能源</el-breadcrumb-item>
        <el-breadcrumb-item>能耗配置</el-breadcrumb-item>
        <el-breadcrumb-item>能耗链路</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :model="searchForm" label-width="80px" :inline="true">
        <el-form-item label="链路名称">
          <el-input v-model="searchForm.circuitName" placeholder="请输入链路名称" clearable />
        </el-form-item>
        <el-form-item label="能源类型">
          <el-select v-model="searchForm.energyType" placeholder="请选择能源类型" clearable>
            <el-option label="电" value="electricity" />
            <el-option label="水" value="water" />
            <el-option label="燃气" value="gas" />
            <el-option label="蒸汽" value="steam" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" value="normal" />
            <el-option label="异常" value="abnormal" />
            <el-option label="停用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <h2>能耗链路管理</h2>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增链路
        </el-button>
        <el-button type="success" @click="handleTopologyView">
          <el-icon><Connection /></el-icon>
          拓扑图
        </el-button>
        <el-button type="info" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <!-- 链路列表 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="circuitList"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="circuitCode" label="链路编码" width="120" />
        <el-table-column prop="circuitName" label="链路名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="energyType" label="能源类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getEnergyTypeColor(row.energyType)">
              {{ getEnergyTypeText(row.energyType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sourceNode" label="源节点" width="120" />
        <el-table-column prop="targetNode" label="目标节点" width="120" />
        <el-table-column prop="nodeCount" label="节点数量" width="100" />
        <el-table-column prop="length" label="链路长度" width="100">
          <template #default="{ row }">
            {{ row.length }} m
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="传输容量" width="120">
          <template #default="{ row }">
            {{ row.capacity }} {{ row.unit }}
          </template>
        </el-table-column>
        <el-table-column prop="currentLoad" label="当前负载" width="100">
          <template #default="{ row }">
            <div class="load-info">
              <span>{{ row.currentLoad }}%</span>
              <el-progress
                :percentage="row.currentLoad"
                :color="getLoadColor(row.currentLoad)"
                :show-text="false"
                size="small"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button link type="success" @click="handleMonitor()">
              <el-icon><Monitor /></el-icon>
              监控
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <!-- 新增/编辑链路弹框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="1000px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="链路编码" prop="circuitCode">
                  <el-input v-model="formData.circuitCode" placeholder="请输入链路编码" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="链路名称" prop="circuitName">
                  <el-input v-model="formData.circuitName" placeholder="请输入链路名称" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="能源类型" prop="energyType">
                  <el-select v-model="formData.energyType" placeholder="请选择能源类型">
                    <el-option label="电" value="electricity" />
                    <el-option label="水" value="water" />
                    <el-option label="燃气" value="gas" />
                    <el-option label="蒸汽" value="steam" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="传输容量" prop="capacity">
                  <el-input-number
                    v-model="formData.capacity"
                    :precision="2"
                    :min="0"
                    controls-position="right"
                    style="width: 80%"
                  />
                  <el-select v-model="formData.unit" style="width: 20%; margin-left: 8px;">
                    <el-option label="kW" value="kW" />
                    <el-option label="MW" value="MW" />
                    <el-option label="m³/h" value="m³/h" />
                    <el-option label="t/h" value="t/h" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="链路长度" prop="length">
                  <el-input-number
                    v-model="formData.length"
                    :precision="2"
                    :min="0"
                    controls-position="right"
                    style="width: 100%"
                  />
                  <span style="margin-left: 8px;">米</span>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="管径/线径" prop="diameter">
                  <el-input-number
                    v-model="formData.diameter"
                    :precision="1"
                    :min="0"
                    controls-position="right"
                    style="width: 100%"
                  />
                  <span style="margin-left: 8px;">mm</span>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="链路描述">
              <el-input
                v-model="formData.description"
                type="textarea"
                :rows="3"
                placeholder="请输入链路描述"
                maxlength="300"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 节点配置 -->
        <el-tab-pane label="节点配置" name="nodes">
          <div class="nodes-config">
            <div class="config-header">
              <el-button type="primary" size="small" @click="handleAddNode">
                <el-icon><Plus /></el-icon>
                添加节点
              </el-button>
              <el-button type="success" size="small" @click="handleAutoConnect">
                <el-icon><Connection /></el-icon>
                自动连接
              </el-button>
            </div>
            
            <el-table :data="formData.nodes" border size="small">
              <el-table-column type="index" label="序号" width="60" />
              <el-table-column prop="nodeName" label="节点名称" min-width="120">
               <template #default="{ row }">
                 <el-input v-model="row.nodeName" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="nodeType" label="节点类型" width="120">
               <template #default="{ row }">
                 <el-select v-model="row.nodeType" size="small">
                    <el-option label="源点" value="source" />
                    <el-option label="中继点" value="relay" />
                    <el-option label="终点" value="target" />
                    <el-option label="分支点" value="branch" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column prop="deviceName" label="关联设备" min-width="140">
               <template #default="{ row }">
                 <el-select v-model="row.deviceName" size="small" clearable>
                    <el-option v-for="device in deviceOptions" :key="device.id" :label="device.name" :value="device.name" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column prop="coordinates" label="坐标位置" width="160">
               <template #default="{ row }">
                 <div style="display: flex; gap: 4px;">
                    <el-input-number v-model="row.x" size="small" :precision="1" style="width: 70px;" />
                    <span>,</span>
                    <el-input-number v-model="row.y" size="small" :precision="1" style="width: 70px;" />
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80">
               <template #default="{ $index }">
                 <el-button link type="danger" size="small" @click="handleRemoveNode($index)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 拓扑预览 -->
        <el-tab-pane label="拓扑预览" name="topology">
          <div class="topology-preview">
            <div class="preview-canvas" ref="canvasRef">
              <!-- 简单的拓扑图预览 -->
              <div
v-for="node in formData.nodes" :key="node.id" 
                   :style="{ left: node.x + 'px', top: node.y + 'px' }"
                   class="topology-node">
                <div :class="['node-circle', node.nodeType]">
                  <span>{{ node.nodeName }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="链路详情"
      width="1000px"
      destroy-on-close
    >
      <div v-if="currentCircuit" class="detail-content">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="链路编码">{{ currentCircuit.circuitCode }}</el-descriptions-item>
          <el-descriptions-item label="链路名称">{{ currentCircuit.circuitName }}</el-descriptions-item>
          <el-descriptions-item label="能源类型">
            <el-tag :type="getEnergyTypeColor(currentCircuit.energyType)">
              {{ getEnergyTypeText(currentCircuit.energyType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="源节点">{{ currentCircuit.sourceNode }}</el-descriptions-item>
          <el-descriptions-item label="目标节点">{{ currentCircuit.targetNode }}</el-descriptions-item>
          <el-descriptions-item label="节点数量">{{ currentCircuit.nodeCount }} 个</el-descriptions-item>
          <el-descriptions-item label="链路长度">{{ currentCircuit.length }} m</el-descriptions-item>
          <el-descriptions-item label="传输容量">{{ currentCircuit.capacity }} {{ currentCircuit.unit }}</el-descriptions-item>
          <el-descriptions-item label="当前负载">{{ currentCircuit.currentLoad }}%</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusColor(currentCircuit.status)">
              {{ getStatusText(currentCircuit.status) }}
            </el-tag>
          </el-descriptions-item>
           <el-descriptions-item label="创建时间" :span="2">{{ currentCircuit.createTime }}</el-descriptions-item>
           <el-descriptions-item label="链路描述" :span="3">{{ currentCircuit.description || '无' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 节点列表 -->
        <div class="nodes-section">
          <h4>链路节点</h4>
          <el-table :data="mockNodes" size="small" border>
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="nodeName" label="节点名称" />
            <el-table-column prop="nodeType" label="节点类型">
              <template #default="{ row }">
                <el-tag :type="getNodeTypeColor(row.nodeType)" size="small">
                  {{ getNodeTypeText(row.nodeType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="deviceName" label="关联设备" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="row.status === 'normal' ? 'success' : 'danger'" size="small">
                  {{ row.status === 'normal' ? '正常' : '异常' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lastUpdateTime" label="最近更新时间" />
          </el-table>
        </div>
      </div>
    </el-dialog>

    <!-- 拓扑图弹框 -->
    <el-dialog
      v-model="topologyDialogVisible"
      title="链路拓扑图"
      width="1200px"
      destroy-on-close
    >
      <div class="topology-view">
        <div class="topology-toolbar">
          <el-button size="small" @click="handleZoomIn">
            <el-icon><ZoomIn /></el-icon>
            放大
          </el-button>
          <el-button size="small" @click="handleZoomOut">
            <el-icon><ZoomOut /></el-icon>
            缩小
          </el-button>
          <el-button size="small" @click="handleResetZoom">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
        <div class="topology-canvas">
          <!-- 这里可以集成更复杂的拓扑图组件 -->
          <div class="simple-topology">
            <div class="topology-info">
              <p>链路拓扑图功能开发中...</p>
              <p>将支持可视化展示链路节点连接关系、实时负载状态等</p>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ContentWrap } from '@/components/ContentWrap'
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Refresh, Download, Connection, View, Edit, Delete, Monitor,
  ZoomIn, ZoomOut
} from '@element-plus/icons-vue'

// 类型定义
interface CircuitNode {
  id: string
  nodeName: string
  nodeType: string
  deviceName: string
  x: number
  y: number
}

interface CircuitItem {
  id: string
  circuitCode: string
  circuitName: string
  energyType: string
  sourceNode: string
  targetNode: string
  nodeCount: number
  length: number
  capacity: number
  unit: string
  currentLoad: number
  status: string
  description: string
  createTime: string
  diameter?: number
}

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const topologyDialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMode = ref<'add' | 'edit'>('add')
const activeTab = ref('basic')
const currentCircuit = ref<CircuitItem | null>(null)
const canvasRef = ref()

// 搜索表单
const searchForm = reactive({
  circuitName: '',
  energyType: '',
  status: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表单数据
const formData = reactive<CircuitItem & { nodes: CircuitNode[]; diameter: number }>({
  id: '',
  circuitCode: '',
  circuitName: '',
  energyType: '',
  sourceNode: '',
  targetNode: '',
  nodeCount: 0,
  capacity: 0,
  unit: 'kW',
  length: 0,
  currentLoad: 0,
  status: 'normal',
  description: '',
  createTime: '',
  diameter: 0,
  nodes: []
})

// 表单验证规则
const formRules = {
  circuitCode: [{ required: true, message: '请输入链路编码', trigger: 'blur' }],
  circuitName: [{ required: true, message: '请输入链路名称', trigger: 'blur' }],
  energyType: [{ required: true, message: '请选择能源类型', trigger: 'change' }],
  capacity: [{ required: true, message: '请输入传输容量', trigger: 'blur' }]
}

// 设备选项
const deviceOptions = ref([
  { id: '1', name: '主配电柜' },
  { id: '2', name: '变压器-01' },
  { id: '3', name: '分配电箱-A1' },
  { id: '4', name: '终端设备-T1' }
])

// 链路列表数据
const circuitList = ref<CircuitItem[]>([
  {
    id: '1',
    circuitCode: 'CIRCUIT001',
    circuitName: '主配电至1号楼',
    energyType: 'electricity',
    sourceNode: '主配电柜',
    targetNode: '1号楼配电箱',
    nodeCount: 4,
    length: 350.5,
    capacity: 800,
    unit: 'kW',
    currentLoad: 65,
    status: 'normal',
    description: '主配电室到1号楼的供电链路',
    createTime: '2024-01-15 09:30:00'
  },
  {
    id: '2',
    circuitCode: 'CIRCUIT002',
    circuitName: '水泵房至园区',
    energyType: 'water',
    sourceNode: '主水泵',
    targetNode: '园区供水点',
    nodeCount: 6,
    length: 520.8,
    capacity: 200,
    unit: 'm³/h',
    currentLoad: 42,
    status: 'normal',
    description: '水泵房到园区各点的供水链路',
    createTime: '2024-01-10 14:20:00'
  },
  {
    id: '3',
    circuitCode: 'CIRCUIT003',
    circuitName: '燃气主管至食堂',
    energyType: 'gas',
    sourceNode: '燃气调压站',
    targetNode: '食堂燃气表',
    nodeCount: 3,
    length: 85.2,
    capacity: 50,
    unit: 'm³/h',
    currentLoad: 88,
    status: 'abnormal',
    description: '燃气调压站到食堂的供气链路',
    createTime: '2024-01-08 16:45:00'
  }
])

// 模拟节点数据
const mockNodes = ref([
  { nodeName: '主配电柜', nodeType: 'source', deviceName: '配电柜-001', status: 'normal', lastUpdateTime: '2024-01-22 14:30:00' },
  { nodeName: '变压器-01', nodeType: 'relay', deviceName: '变压器-T01', status: 'normal', lastUpdateTime: '2024-01-22 14:30:00' },
  { nodeName: '分配电箱-A1', nodeType: 'branch', deviceName: '配电箱-A1', status: 'normal', lastUpdateTime: '2024-01-22 14:30:00' },
  { nodeName: '1号楼配电箱', nodeType: 'target', deviceName: '配电箱-B1', status: 'normal', lastUpdateTime: '2024-01-22 14:30:00' }
])

const formRef = ref()

// 获取能源类型颜色
const getEnergyTypeColor = (type: string) => {
  const colors = {
    electricity: 'primary',
    water: 'success',
    gas: 'warning',
    steam: 'danger'
  }
  return colors[type] || 'info'
}

// 获取能源类型文本
const getEnergyTypeText = (type: string) => {
  const texts = {
    electricity: '电',
    water: '水',
    gas: '燃气',
    steam: '蒸汽'
  }
  return texts[type] || type
}

// 获取负载颜色
const getLoadColor = (load: number) => {
  if (load < 50) return '#67c23a'
  if (load < 80) return '#e6a23c'
  return '#f56c6c'
}

// 获取状态颜色
const getStatusColor = (status: string) => {
  const colors = {
    normal: 'success',
    abnormal: 'danger',
    disabled: 'info'
  }
  return colors[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const texts = {
    normal: '正常',
    abnormal: '异常',
    disabled: '停用'
  }
  return texts[status] || status
}

// 获取节点类型颜色
const getNodeTypeColor = (type: string) => {
  const colors = {
    source: 'success',
    relay: 'primary',
    target: 'warning',
    branch: 'info'
  }
  return colors[type] || 'info'
}

// 获取节点类型文本
const getNodeTypeText = (type: string) => {
  const texts = {
    source: '源点',
    relay: '中继点',
    target: '终点',
    branch: '分支点'
  }
  return texts[type] || type
}

// 事件处理
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    circuitName: '',
    energyType: '',
    status: ''
  })
  handleSearch()
}

const handleTopologyView = () => {
  topologyDialogVisible.value = true
}

const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

const handleCreate = () => {
  dialogMode.value = 'add'
  dialogTitle.value = '新增链路'
  activeTab.value = 'basic'
  resetFormData()
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑链路'
  activeTab.value = 'basic'
  Object.assign(formData, { ...row })
  // 模拟加载节点数据
  formData.nodes = [
    { id: '1', nodeName: '主配电柜', nodeType: 'source', deviceName: '配电柜-001', x: 50, y: 100 },
    { id: '2', nodeName: '变压器-01', nodeType: 'relay', deviceName: '变压器-T01', x: 200, y: 100 },
    { id: '3', nodeName: '1号楼配电箱', nodeType: 'target', deviceName: '配电箱-B1', x: 350, y: 100 }
  ]
  dialogVisible.value = true
}

const handleView = (row: any) => {
  currentCircuit.value = row
  detailDialogVisible.value = true
}

const handleMonitor = () => {
  ElMessage.info('监控功能开发中...')
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该链路吗？删除后无法恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const index = circuitList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      circuitList.value.splice(index, 1)
    }
    
    ElMessage.success('删除成功')
  } catch {
    // 用户取消删除
  }
}

const handleAddNode = () => {
  const newNode = {
    id: `node_${Date.now()}`,
    nodeName: '',
    nodeType: 'relay',
    deviceName: '',
    x: Math.random() * 400 + 50,
    y: Math.random() * 200 + 50
  }
  formData.nodes.push(newNode)
}

const handleRemoveNode = (index: number) => {
  formData.nodes.splice(index, 1)
}

const handleAutoConnect = () => {
  ElMessage.success('自动连接完成')
}

const handleZoomIn = () => {
  ElMessage.info('放大功能开发中...')
}

const handleZoomOut = () => {
  ElMessage.info('缩小功能开发中...')
}

const handleResetZoom = () => {
  ElMessage.info('重置缩放功能开发中...')
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (dialogMode.value === 'add') {
      const newCircuit = {
        ...formData,
        id: `circuit_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        sourceNode: formData.nodes.find(n => n.nodeType === 'source')?.nodeName || '',
        targetNode: formData.nodes.find(n => n.nodeType === 'target')?.nodeName || '',
        nodeCount: formData.nodes.length,
        currentLoad: 0,
        status: 'normal',
        createTime: new Date().toLocaleString('zh-CN')
      }
      circuitList.value.unshift(newCircuit)
      ElMessage.success('创建成功')
    } else if (dialogMode.value === 'edit') {
      const index = circuitList.value.findIndex(item => item.id === formData.id)
      if (index > -1) {
        Object.assign(circuitList.value[index], {
          ...formData,
          sourceNode: formData.nodes.find(n => n.nodeType === 'source')?.nodeName || '',
          targetNode: formData.nodes.find(n => n.nodeType === 'target')?.nodeName || '',
          nodeCount: formData.nodes.length
        })
      }
      ElMessage.success('更新成功')
    }
    
    dialogVisible.value = false
  } catch {
    // 表单验证失败
  }
}

const resetFormData = () => {
  Object.assign(formData, {
    id: '',
    circuitCode: '',
    circuitName: '',
    energyType: '',
    sourceNode: '',
    targetNode: '',
    nodeCount: 0,
    capacity: 0,
    unit: 'kW',
    length: 0,
    currentLoad: 0,
    status: 'normal',
    description: '',
    createTime: '',
    diameter: 0,
    nodes: []
  })
}

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    pagination.total = circuitList.value.length
    loading.value = false
  }, 500)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">@use '@/styles/dark-theme.scss';

.app-container {
  padding: 20px;

  .breadcrumb-container {
    margin-bottom: 20px;
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
        margin: 0;
        color: #303133;
        font-size: 24px;
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .table-container {
    background: #1a1a1a;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .load-info {
      display: flex;
      flex-direction: column;
      gap: 4px;
      align-items: center;

      span {
        font-size: 12px;
        font-weight: 500;
      }
    }

    .el-pagination {
      margin-top: 20px;
      text-align: right;
    }
  }
}

.nodes-config {
  .config-header {
    margin-bottom: 16px;
    display: flex;
    gap: 12px;
  }
}

.topology-preview {
  .preview-canvas {
    position: relative;
    height: 300px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    background: #f8f9fa;
    overflow: hidden;

    .topology-node {
      position: absolute;
      transform: translate(-50%, -50%);

      .node-circle {
        width: 60px;
        height: 60px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 10px;
        color: #fff;
        font-weight: 500;
        cursor: pointer;

        &.source {
          background: #67c23a;
        }

        &.relay {
          background: #409eff;
        }

        &.target {
          background: #e6a23c;
        }

        &.branch {
          background: #909399;
        }
      }
    }
  }
}

.detail-content {
  .el-descriptions {
    margin-bottom: 24px;
  }

  .nodes-section {
    margin-top: 24px;

    h4 {
      margin: 0 0 16px 0;
      color: #303133;
      font-size: 16px;
      font-weight: 600;
      border-left: 3px solid #409eff;
      padding-left: 12px;
    }
  }
}

.topology-view {
  .topology-toolbar {
    margin-bottom: 16px;
    display: flex;
    gap: 12px;
  }

  .topology-canvas {
    height: 500px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    background: #f8f9fa;

    .simple-topology {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100%;

      .topology-info {
        text-align: center;
        color: #909399;

        p {
          margin: 8px 0;
          font-size: 14px;
        }
      }
    }
  }
}
</style>






