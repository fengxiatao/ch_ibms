<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧建筑</el-breadcrumb-item>
        <el-breadcrumb-item>冷源群控</el-breadcrumb-item>
        <el-breadcrumb-item>冷热源设备</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon online">
                <el-icon><Monitor /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ deviceStats.online }}</div>
                <div class="stats-label">在线设备</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon offline">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ deviceStats.offline }}</div>
                <div class="stats-label">离线设备</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon running">
                <el-icon><VideoPlay /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ deviceStats.running }}</div>
                <div class="stats-label">运行中</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon alarm">
                <el-icon><Bell /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ deviceStats.alarm }}</div>
                <div class="stats-label">告警设备</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 查询条件 -->
    <div class="search-container">
      <el-form :model="searchForm" label-width="80px" :inline="true">
        <el-form-item label="设备名称">
          <el-input
            v-model="searchForm.deviceName"
            placeholder="请输入设备名称"
            clearable
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="设备类型">
          <el-select v-model="searchForm.deviceType" placeholder="请选择设备类型" clearable style="width: 150px;">
            <el-option label="冷水机组" value="chiller" />
            <el-option label="冷却塔" value="cooling_tower" />
            <el-option label="冷冻水泵" value="chilled_water_pump" />
            <el-option label="冷却水泵" value="cooling_water_pump" />
            <el-option label="热泵机组" value="heat_pump" />
          </el-select>
        </el-form-item>
        <el-form-item label="运行状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px;">
            <el-option label="运行" value="running" />
            <el-option label="停止" value="stopped" />
            <el-option label="故障" value="fault" />
            <el-option label="维护" value="maintenance" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属区域">
          <el-select v-model="searchForm.area" placeholder="请选择区域" clearable style="width: 150px;">
            <el-option label="1号冷机房" value="room_1" />
            <el-option label="2号冷机房" value="room_2" />
            <el-option label="屋顶机房" value="roof_room" />
            <el-option label="地下机房" value="basement_room" />
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
          <el-button type="success" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增设备
          </el-button>
          <el-button type="warning" @click="handleBatchControl">
            <el-icon><Operation /></el-icon>
            批量控制
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 设备列表 -->
    <div class="device-list">
      <el-card class="list-card">
        <template #header>
          <div class="card-header">
            <span>冷热源设备列表</span>
            <div class="header-actions">
              <el-radio-group v-model="viewMode" size="small">
                <el-radio-button label="table">表格视图</el-radio-button>
                <el-radio-button label="card">卡片视图</el-radio-button>
              </el-radio-group>
            </div>
          </div>
        </template>

        <!-- 表格视图 -->
        <div v-if="viewMode === 'table'">
          <el-table :data="deviceList" stripe border style="width: 100%">
            <el-table-column type="selection" width="55" />
            <el-table-column prop="deviceCode" label="设备编号" width="120" />
            <el-table-column prop="deviceName" label="设备名称" width="150" />
            <el-table-column prop="deviceType" label="设备类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getDeviceTypeColor(row.deviceType)">
                  {{ getDeviceTypeText(row.deviceType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="area" label="所属区域" width="120" />
            <el-table-column prop="status" label="运行状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusColor(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="power" label="功率(kW)" width="100" />
            <el-table-column prop="temperature" label="温度(°C)" width="100" />
            <el-table-column prop="pressure" label="压力(MPa)" width="100" />
            <el-table-column prop="flowRate" label="流量(m³/h)" width="110" />
            <el-table-column prop="efficiency" label="能效比" width="100" />
            <el-table-column prop="lastMaintenanceTime" label="上次维护" width="120" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="handleView(row)">
                  <el-icon><View /></el-icon>
                  查看
                </el-button>
                <el-button link type="primary" size="small" @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button link type="success" size="small" @click="handleControl(row)">
                  <el-icon><Setting /></el-icon>
                  控制
                </el-button>
                <el-button link type="danger" size="small" @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 卡片视图 -->
        <div v-else class="card-view">
          <el-row :gutter="20">
            <el-col v-for="device in deviceList" :key="device.id" :span="8">
              <el-card class="device-card" :class="{ 'device-alarm': device.hasAlarm }">
                <template #header>
                  <div class="device-header">
                    <div class="device-title">
                      <el-icon class="device-icon"><Monitor /></el-icon>
                      <span>{{ device.deviceName }}</span>
                    </div>
                    <el-tag :type="getStatusColor(device.status)" size="small">
                      {{ getStatusText(device.status) }}
                    </el-tag>
                  </div>
                </template>
                
                <div class="device-content">
                  <div class="device-info">
                    <div class="info-row">
                      <span class="label">设备编号:</span>
                      <span class="value">{{ device.deviceCode }}</span>
                    </div>
                    <div class="info-row">
                      <span class="label">设备类型:</span>
                      <span class="value">{{ getDeviceTypeText(device.deviceType) }}</span>
                    </div>
                    <div class="info-row">
                      <span class="label">所属区域:</span>
                      <span class="value">{{ device.area }}</span>
                    </div>
                  </div>
                  
                  <div class="device-params">
                    <div class="param-item">
                      <div class="param-value">{{ device.power }}</div>
                      <div class="param-label">功率(kW)</div>
                    </div>
                    <div class="param-item">
                      <div class="param-value">{{ device.temperature }}</div>
                      <div class="param-label">温度(°C)</div>
                    </div>
                    <div class="param-item">
                      <div class="param-value">{{ device.efficiency }}</div>
                      <div class="param-label">能效比</div>
                    </div>
                  </div>
                  
                  <div class="device-actions">
                    <el-button size="small" @click="handleView(device)">查看</el-button>
                    <el-button size="small" type="primary" @click="handleControl(device)">控制</el-button>
                    <el-button size="small" type="success" @click="handleEdit(device)">编辑</el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 分页 -->
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
          style="margin-top: 20px; text-align: right;"
        />
      </el-card>
    </div>

    <!-- 设备详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="设备详情" width="80%" draggable>
      <div v-if="currentDevice" class="device-detail">
        <el-tabs v-model="activeTab" type="border-card">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <div class="basic-info">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form label-width="100px">
                    <el-form-item label="设备编号">
                      <span>{{ currentDevice.deviceCode }}</span>
                    </el-form-item>
                    <el-form-item label="设备名称">
                      <span>{{ currentDevice.deviceName }}</span>
                    </el-form-item>
                    <el-form-item label="设备类型">
                      <span>{{ getDeviceTypeText(currentDevice.deviceType) }}</span>
                    </el-form-item>
                    <el-form-item label="所属区域">
                      <span>{{ currentDevice.area }}</span>
                    </el-form-item>
                    <el-form-item label="安装位置">
                      <span>{{ currentDevice.location }}</span>
                    </el-form-item>
                  </el-form>
                </el-col>
                <el-col :span="12">
                  <el-form label-width="100px">
                    <el-form-item label="制造商">
                      <span>{{ currentDevice.manufacturer }}</span>
                    </el-form-item>
                    <el-form-item label="型号规格">
                      <span>{{ currentDevice.model }}</span>
                    </el-form-item>
                    <el-form-item label="额定功率">
                      <span>{{ currentDevice.ratedPower }} kW</span>
                    </el-form-item>
                    <el-form-item label="投运日期">
                      <span>{{ currentDevice.commissionDate }}</span>
                    </el-form-item>
                    <el-form-item label="保修期限">
                      <span>{{ currentDevice.warrantyPeriod }}</span>
                    </el-form-item>
                  </el-form>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>

          <!-- 运行参数 -->
          <el-tab-pane label="运行参数" name="parameters">
            <div class="parameters-info">
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-card class="param-card">
                    <template #header>温度参数</template>
                    <div class="param-list">
                      <div class="param-row">
                        <span>出水温度:</span>
                        <span class="param-value">{{ currentDevice.outletTemp }}°C</span>
                      </div>
                      <div class="param-row">
                        <span>回水温度:</span>
                        <span class="param-value">{{ currentDevice.returnTemp }}°C</span>
                      </div>
                      <div class="param-row">
                        <span>环境温度:</span>
                        <span class="param-value">{{ currentDevice.ambientTemp }}°C</span>
                      </div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card class="param-card">
                    <template #header>压力参数</template>
                    <div class="param-list">
                      <div class="param-row">
                        <span>出水压力:</span>
                        <span class="param-value">{{ currentDevice.outletPressure }} MPa</span>
                      </div>
                      <div class="param-row">
                        <span>回水压力:</span>
                        <span class="param-value">{{ currentDevice.returnPressure }} MPa</span>
                      </div>
                      <div class="param-row">
                        <span>压差:</span>
                        <span class="param-value">{{ currentDevice.pressureDiff }} MPa</span>
                      </div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card class="param-card">
                    <template #header>流量功率</template>
                    <div class="param-list">
                      <div class="param-row">
                        <span>流量:</span>
                        <span class="param-value">{{ currentDevice.flowRate }} m³/h</span>
                      </div>
                      <div class="param-row">
                        <span>当前功率:</span>
                        <span class="param-value">{{ currentDevice.power }} kW</span>
                      </div>
                      <div class="param-row">
                        <span>能效比:</span>
                        <span class="param-value">{{ currentDevice.efficiency }}</span>
                      </div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>

          <!-- 控制操作 -->
          <el-tab-pane label="控制操作" name="control">
            <div class="control-panel">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-card class="control-card">
                    <template #header>设备控制</template>
                    <div class="control-actions">
                      <el-button type="success" size="large" @click="handleDeviceStart">
                        <el-icon><VideoPlay /></el-icon>
                        启动
                      </el-button>
                      <el-button type="danger" size="large" @click="handleDeviceStop">
                        <el-icon><VideoPause /></el-icon>
                        停止
                      </el-button>
                      <el-button type="warning" size="large" @click="handleDeviceReset">
                        <el-icon><RefreshRight /></el-icon>
                        复位
                      </el-button>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card class="control-card">
                    <template #header>参数设置</template>
                    <el-form label-width="120px">
                      <el-form-item label="目标温度">
                        <el-input-number v-model="controlParams.targetTemp" :min="0" :max="50" />
                        <span style="margin-left: 8px;">°C</span>
                      </el-form-item>
                      <el-form-item label="目标压力">
                        <el-input-number v-model="controlParams.targetPressure" :min="0" :max="10" :precision="2" />
                        <span style="margin-left: 8px;">MPa</span>
                      </el-form-item>
                      <el-form-item>
                        <el-button type="primary" @click="handleSetParams">应用设置</el-button>
                      </el-form-item>
                    </el-form>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>

          <!-- 维护记录 -->
          <el-tab-pane label="维护记录" name="maintenance">
            <div class="maintenance-records">
              <el-table :data="maintenanceRecords" stripe style="width: 100%">
                <el-table-column prop="date" label="维护日期" width="120" />
                <el-table-column prop="type" label="维护类型" width="120" />
                <el-table-column prop="content" label="维护内容" />
                <el-table-column prop="operator" label="操作人员" width="100" />
                <el-table-column prop="cost" label="维护费用" width="100" />
                <el-table-column prop="nextDate" label="下次维护" width="120" />
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- 设备编辑对话框 -->
    <el-dialog v-model="editDialogVisible" :title="isEdit ? '编辑设备' : '新增设备'" width="60%" draggable>
      <el-form ref="deviceFormRef" :model="deviceForm" :rules="deviceRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备编号" prop="deviceCode">
              <el-input v-model="deviceForm.deviceCode" placeholder="请输入设备编号" />
            </el-form-item>
            <el-form-item label="设备名称" prop="deviceName">
              <el-input v-model="deviceForm.deviceName" placeholder="请输入设备名称" />
            </el-form-item>
            <el-form-item label="设备类型" prop="deviceType">
              <el-select v-model="deviceForm.deviceType" placeholder="请选择设备类型" style="width: 100%;">
                <el-option label="冷水机组" value="chiller" />
                <el-option label="冷却塔" value="cooling_tower" />
                <el-option label="冷冻水泵" value="chilled_water_pump" />
                <el-option label="冷却水泵" value="cooling_water_pump" />
                <el-option label="热泵机组" value="heat_pump" />
              </el-select>
            </el-form-item>
            <el-form-item label="所属区域" prop="area">
              <el-select v-model="deviceForm.area" placeholder="请选择区域" style="width: 100%;">
                <el-option label="1号冷机房" value="room_1" />
                <el-option label="2号冷机房" value="room_2" />
                <el-option label="屋顶机房" value="roof_room" />
                <el-option label="地下机房" value="basement_room" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="制造商">
              <el-input v-model="deviceForm.manufacturer" placeholder="请输入制造商" />
            </el-form-item>
            <el-form-item label="型号规格">
              <el-input v-model="deviceForm.model" placeholder="请输入型号规格" />
            </el-form-item>
            <el-form-item label="额定功率">
              <el-input-number v-model="deviceForm.ratedPower" :min="0" style="width: 100%;" />
            </el-form-item>
            <el-form-item label="安装位置">
              <el-input v-model="deviceForm.location" placeholder="请输入安装位置" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave">确定</el-button>
        </span>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, Plus, Operation, View, Edit, Delete, Setting,
  Monitor, Warning, VideoPlay, Bell, VideoPause, RefreshRight
} from '@element-plus/icons-vue'

// 响应式数据
const viewMode = ref('table')
const detailDialogVisible = ref(false)
const editDialogVisible = ref(false)
const isEdit = ref(false)
const activeTab = ref('basic')
const currentDevice = ref(null)

// 设备统计
const deviceStats = reactive({
  online: 45,
  offline: 3,
  running: 38,
  alarm: 2
})

// 搜索表单
const searchForm = reactive({
  deviceName: '',
  deviceType: '',
  status: '',
  area: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 设备表单
const deviceForm = reactive({
  deviceCode: '',
  deviceName: '',
  deviceType: '',
  area: '',
  manufacturer: '',
  model: '',
  ratedPower: 0,
  location: ''
})

// 控制参数
const controlParams = reactive({
  targetTemp: 7,
  targetPressure: 0.6
})

// 表单验证规则
const deviceRules = {
  deviceCode: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  deviceType: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
  area: [{ required: true, message: '请选择所属区域', trigger: 'change' }]
}

// 设备列表数据
const deviceList = ref([
  {
    id: 1,
    deviceCode: 'CHL-001',
    deviceName: '1号冷水机组',
    deviceType: 'chiller',
    area: '1号冷机房',
    status: 'running',
    power: 285.6,
    temperature: 7.2,
    pressure: 0.65,
    flowRate: 180,
    efficiency: 4.2,
    lastMaintenanceTime: '2024-01-15',
    hasAlarm: false,
    location: '1号冷机房东侧',
    manufacturer: '开利',
    model: 'AquaEdge 19XR',
    ratedPower: 500,
    commissionDate: '2020-06-15',
    warrantyPeriod: '5年',
    outletTemp: 7.2,
    returnTemp: 12.8,
    ambientTemp: 25.6,
    outletPressure: 0.65,
    returnPressure: 0.58,
    pressureDiff: 0.07,
    flowRate: 180
  },
  {
    id: 2,
    deviceCode: 'CHL-002',
    deviceName: '2号冷水机组',
    deviceType: 'chiller',
    area: '1号冷机房',
    status: 'stopped',
    power: 0,
    temperature: 25.6,
    pressure: 0,
    flowRate: 0,
    efficiency: 0,
    lastMaintenanceTime: '2024-01-10',
    hasAlarm: false,
    location: '1号冷机房西侧',
    manufacturer: '开利',
    model: 'AquaEdge 19XR',
    ratedPower: 500,
    commissionDate: '2020-06-15',
    warrantyPeriod: '5年',
    outletTemp: 25.6,
    returnTemp: 25.8,
    ambientTemp: 25.6,
    outletPressure: 0,
    returnPressure: 0,
    pressureDiff: 0,
    flowRate: 0
  },
  {
    id: 3,
    deviceCode: 'CT-001',
    deviceName: '1号冷却塔',
    deviceType: 'cooling_tower',
    area: '屋顶机房',
    status: 'running',
    power: 45.8,
    temperature: 32.5,
    pressure: 0.15,
    flowRate: 220,
    efficiency: 3.8,
    lastMaintenanceTime: '2024-01-20',
    hasAlarm: true,
    location: '屋顶东侧',
    manufacturer: '良机',
    model: 'LBC-200',
    ratedPower: 75,
    commissionDate: '2020-08-10',
    warrantyPeriod: '3年',
    outletTemp: 32.5,
    returnTemp: 37.2,
    ambientTemp: 28.9,
    outletPressure: 0.15,
    returnPressure: 0.12,
    pressureDiff: 0.03,
    flowRate: 220
  }
])

// 维护记录数据
const maintenanceRecords = ref([
  {
    date: '2024-01-15',
    type: '定期保养',
    content: '更换滤芯、检查制冷剂、清洁冷凝器',
    operator: '张师傅',
    cost: 1200,
    nextDate: '2024-04-15'
  },
  {
    date: '2023-10-20',
    type: '故障维修',
    content: '更换压缩机轴承',
    operator: '李师傅',
    cost: 3500,
    nextDate: '2024-01-20'
  }
])

// 计算属性和方法
const getDeviceTypeColor = (type: string) => {
  const colors = {
    chiller: 'primary',
    cooling_tower: 'success',
    chilled_water_pump: 'info',
    cooling_water_pump: 'warning',
    heat_pump: 'danger'
  }
  return colors[type] || 'info'
}

const getDeviceTypeText = (type: string) => {
  const texts = {
    chiller: '冷水机组',
    cooling_tower: '冷却塔',
    chilled_water_pump: '冷冻水泵',
    cooling_water_pump: '冷却水泵',
    heat_pump: '热泵机组'
  }
  return texts[type] || type
}

const getStatusColor = (status: string) => {
  const colors = {
    running: 'success',
    stopped: 'info',
    fault: 'danger',
    maintenance: 'warning'
  }
  return colors[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts = {
    running: '运行',
    stopped: '停止',
    fault: '故障',
    maintenance: '维护'
  }
  return texts[status] || status
}

// 事件处理
const handleSearch = () => {
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    deviceName: '',
    deviceType: '',
    status: '',
    area: ''
  })
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(deviceForm, {
    deviceCode: '',
    deviceName: '',
    deviceType: '',
    area: '',
    manufacturer: '',
    model: '',
    ratedPower: 0,
    location: ''
  })
  editDialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  Object.assign(deviceForm, {
    deviceCode: row.deviceCode,
    deviceName: row.deviceName,
    deviceType: row.deviceType,
    area: row.area,
    manufacturer: row.manufacturer,
    model: row.model,
    ratedPower: row.ratedPower,
    location: row.location
  })
  editDialogVisible.value = true
}

const handleView = (row: any) => {
  currentDevice.value = row
  activeTab.value = 'basic'
  detailDialogVisible.value = true
}

const handleControl = (row: any) => {
  currentDevice.value = row
  activeTab.value = 'control'
  detailDialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认删除设备 "${row.deviceName}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // 用户取消删除
  }
}

const handleBatchControl = () => {
  ElMessage.info('批量控制功能开发中...')
}

const handleSave = () => {
  ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
  editDialogVisible.value = false
  loadData()
}

const handleDeviceStart = () => {
  ElMessage.success('设备启动指令已发送')
}

const handleDeviceStop = () => {
  ElMessage.success('设备停止指令已发送')
}

const handleDeviceReset = () => {
  ElMessage.success('设备复位指令已发送')
}

const handleSetParams = () => {
  ElMessage.success('参数设置已应用')
}

const loadData = () => {
  console.log('Loading device data...', searchForm)
  pagination.total = deviceList.value.length
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
          margin-right: 16px;
          
          .el-icon {
            font-size: 24px;
            color: white;
          }
          
          &.online {
            background: linear-gradient(135deg, #67c23a, #85ce61);
          }
          
          &.offline {
            background: linear-gradient(135deg, #f56c6c, #f78989);
          }
          
          &.running {
            background: linear-gradient(135deg, #409eff, #66b1ff);
          }
          
          &.alarm {
            background: linear-gradient(135deg, #e6a23c, #ebb563);
          }
        }
        
        .stats-info {
          .stats-value {
            font-size: 32px;
            font-weight: bold;
            color: #303133;
            line-height: 1;
          }
          
          .stats-label {
            font-size: 14px;
            color: #909399;
            margin-top: 4px;
          }
        }
      }
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

  .device-list {
    .list-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
    }

    .card-view {
      .device-card {
        margin-bottom: 20px;
        transition: all 0.3s;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }

        &.device-alarm {
          border-left: 4px solid #f56c6c;
        }

        .device-header {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .device-title {
            display: flex;
            align-items: center;
            gap: 8px;

            .device-icon {
              color: #409eff;
            }
          }
        }

        .device-content {
          .device-info {
            margin-bottom: 16px;

            .info-row {
              display: flex;
              justify-content: space-between;
              margin-bottom: 8px;

              .label {
                color: #909399;
                font-size: 14px;
              }

              .value {
                color: #303133;
                font-weight: 500;
              }
            }
          }

          .device-params {
            display: flex;
            justify-content: space-around;
            margin-bottom: 16px;
            padding: 16px 0;
            background: #f8f9fa;
            border-radius: 6px;

            .param-item {
              text-align: center;

              .param-value {
                font-size: 18px;
                font-weight: bold;
                color: #409eff;
              }

              .param-label {
                font-size: 12px;
                color: #909399;
                margin-top: 4px;
              }
            }
          }

          .device-actions {
            display: flex;
            justify-content: space-around;
          }
        }
      }
    }
  }

  .device-detail {
    .basic-info {
      .el-form-item {
        margin-bottom: 20px;
      }
    }

    .parameters-info {
      .param-card {
        .param-list {
          .param-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 12px;
            padding: 8px 0;
            border-bottom: 1px solid #f0f0f0;

            &:last-child {
              border-bottom: none;
            }

            .param-value {
              font-weight: 600;
              color: #409eff;
            }
          }
        }
      }
    }

    .control-panel {
      .control-card {
        .control-actions {
          display: flex;
          justify-content: space-around;
          padding: 20px 0;

          .el-button {
            width: 120px;
            height: 60px;
          }
        }
      }
    }

    .maintenance-records {
      .el-table {
        margin-top: 0;
      }
    }
  }
}
</style>






