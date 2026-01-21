<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧能源</el-breadcrumb-item>
        <el-breadcrumb-item>能耗配置</el-breadcrumb-item>
        <el-breadcrumb-item>智能表计</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :model="searchForm" label-width="80px" :inline="true">
        <el-form-item label="表计名称">
          <el-input v-model="searchForm.meterName" placeholder="请输入表计名称" clearable />
        </el-form-item>
        <el-form-item label="表计类型">
          <el-select v-model="searchForm.meterType" placeholder="请选择表计类型" clearable>
            <el-option label="电表" value="electricity" />
            <el-option label="水表" value="water" />
            <el-option label="燃气表" value="gas" />
            <el-option label="蒸汽表" value="steam" />
            <el-option label="热量表" value="heat" />
          </el-select>
        </el-form-item>
        <el-form-item label="安装位置">
          <el-input v-model="searchForm.location" placeholder="请输入安装位置" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="在线" value="online" />
            <el-option label="离线" value="offline" />
            <el-option label="故障" value="fault" />
            <el-option label="维护" value="maintenance" />
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
        <h2>智能表计管理</h2>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增表计
        </el-button>
        <el-button type="success" @click="handleImport">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
        <el-button type="info" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
        <el-button type="warning" @click="handleScanDevices">
          <el-icon><Refresh /></el-icon>
          扫描设备
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon online">
                <el-icon><Connection /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.onlineCount }}</div>
                <div class="stat-label">在线设备</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon offline">
                <el-icon><Remove /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.offlineCount }}</div>
                <div class="stat-label">离线设备</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon fault">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.faultCount }}</div>
                <div class="stat-label">故障设备</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon total">
                <el-icon><Collection /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.totalCount }}</div>
                <div class="stat-label">总设备数</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 表计列表 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="meterList"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="meterCode" label="表计编号" width="120" />
        <el-table-column prop="meterName" label="表计名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="meterType" label="表计类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getMeterTypeColor(row.meterType)">
              {{ getMeterTypeText(row.meterType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="model" label="型号" width="120" />
        <el-table-column prop="manufacturer" label="厂商" width="120" />
        <el-table-column prop="location" label="安装位置" min-width="140" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="120" />
        <el-table-column prop="currentValue" label="当前读数" width="120">
          <template #default="{ row }">
            {{ row.currentValue }} {{ row.unit }}
          </template>
        </el-table-column>
        <el-table-column prop="lastReadTime" label="最近读数时间" width="160" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)" :class="row.status === 'online' ? 'blinking' : ''">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button link type="success" @click="handleReadData(row)">
              <el-icon><Reading /></el-icon>
              读数
            </el-button>
            <el-button link type="info" @click="handleCalibrate(row)">
              <el-icon><Setting /></el-icon>
              校准
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

    <!-- 新增/编辑表计弹框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="900px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="表计编号" prop="meterCode">
                  <el-input v-model="formData.meterCode" placeholder="请输入表计编号" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="表计名称" prop="meterName">
                  <el-input v-model="formData.meterName" placeholder="请输入表计名称" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="表计类型" prop="meterType">
                  <el-select v-model="formData.meterType" placeholder="请选择表计类型" @change="handleTypeChange">
                    <el-option label="电表" value="electricity" />
                    <el-option label="水表" value="water" />
                    <el-option label="燃气表" value="gas" />
                    <el-option label="蒸汽表" value="steam" />
                    <el-option label="热量表" value="heat" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="计量单位" prop="unit">
                  <el-select v-model="formData.unit" placeholder="请选择计量单位">
                    <el-option v-for="unit in unitOptions" :key="unit" :label="unit" :value="unit" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="设备型号" prop="model">
                  <el-input v-model="formData.model" placeholder="请输入设备型号" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="制造厂商" prop="manufacturer">
                  <el-input v-model="formData.manufacturer" placeholder="请输入制造厂商" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="安装位置" prop="location">
                  <el-input v-model="formData.location" placeholder="请输入安装位置" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="安装日期" prop="installDate">
                  <el-date-picker
                    v-model="formData.installDate"
                    type="date"
                    placeholder="请选择安装日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="备注">
              <el-input
                v-model="formData.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入备注信息"
                maxlength="300"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 通信配置 -->
        <el-tab-pane label="通信配置" name="communication">
          <el-form :model="formData" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="通信协议" prop="protocol">
                  <el-select v-model="formData.protocol" placeholder="请选择通信协议">
                    <el-option label="Modbus RTU" value="modbus_rtu" />
                    <el-option label="Modbus TCP" value="modbus_tcp" />
                    <el-option label="DL/T645" value="dlt645" />
                    <el-option label="CJ/T188" value="cjt188" />
                    <el-option label="OPC UA" value="opc_ua" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="设备地址" prop="deviceAddress">
                  <el-input v-model="formData.deviceAddress" placeholder="请输入设备地址" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="IP地址" prop="ipAddress">
                  <el-input v-model="formData.ipAddress" placeholder="请输入IP地址" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="端口号" prop="port">
                  <el-input-number
                    v-model="formData.port"
                    :min="1"
                    :max="65535"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="波特率" prop="baudRate">
                  <el-select v-model="formData.baudRate" placeholder="请选择波特率">
                    <el-option label="9600" value="9600" />
                    <el-option label="19200" value="19200" />
                    <el-option label="38400" value="38400" />
                    <el-option label="57600" value="57600" />
                    <el-option label="115200" value="115200" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="数据位" prop="dataBits">
                  <el-select v-model="formData.dataBits" placeholder="请选择数据位">
                    <el-option label="7" value="7" />
                    <el-option label="8" value="8" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="停止位" prop="stopBits">
                  <el-select v-model="formData.stopBits" placeholder="请选择停止位">
                    <el-option label="1" value="1" />
                    <el-option label="2" value="2" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="校验位" prop="parity">
                  <el-select v-model="formData.parity" placeholder="请选择校验位">
                    <el-option label="无" value="none" />
                    <el-option label="奇校验" value="odd" />
                    <el-option label="偶校验" value="even" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>

        <!-- 参数配置 -->
        <el-tab-pane label="参数配置" name="parameters">
          <el-form :model="formData" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="变比" prop="ratio">
                  <el-input-number
                    v-model="formData.ratio"
                    :precision="3"
                    :step="0.001"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="脉冲常数" prop="pulseConstant">
                  <el-input-number
                    v-model="formData.pulseConstant"
                    :min="1"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="初始读数" prop="initialValue">
                  <el-input-number
                    v-model="formData.initialValue"
                    :precision="2"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="采集间隔" prop="collectInterval">
                  <el-select v-model="formData.collectInterval" placeholder="请选择采集间隔">
                    <el-option label="1分钟" value="1" />
                    <el-option label="5分钟" value="5" />
                    <el-option label="15分钟" value="15" />
                    <el-option label="30分钟" value="30" />
                    <el-option label="1小时" value="60" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="报警上限" prop="alarmHigh">
                  <el-input-number
                    v-model="formData.alarmHigh"
                    :precision="2"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="报警下限" prop="alarmLow">
                  <el-input-number
                    v-model="formData.alarmLow"
                    :precision="2"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
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
      title="表计详情"
      width="1000px"
      destroy-on-close
    >
      <div v-if="currentMeter" class="detail-content">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="表计编号">{{ currentMeter.meterCode }}</el-descriptions-item>
          <el-descriptions-item label="表计名称">{{ currentMeter.meterName }}</el-descriptions-item>
          <el-descriptions-item label="表计类型">
            <el-tag :type="getMeterTypeColor(currentMeter.meterType)">
              {{ getMeterTypeText(currentMeter.meterType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="设备型号">{{ currentMeter.model }}</el-descriptions-item>
          <el-descriptions-item label="制造厂商">{{ currentMeter.manufacturer }}</el-descriptions-item>
          <el-descriptions-item label="安装位置">{{ currentMeter.location }}</el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ currentMeter.ipAddress }}</el-descriptions-item>
          <el-descriptions-item label="设备地址">{{ currentMeter.deviceAddress }}</el-descriptions-item>
          <el-descriptions-item label="通信协议">{{ currentMeter.protocol }}</el-descriptions-item>
          <el-descriptions-item label="当前读数">{{ currentMeter.currentValue }} {{ currentMeter.unit }}</el-descriptions-item>
          <el-descriptions-item label="最近读数时间">{{ currentMeter.lastReadTime }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusColor(currentMeter.status)">
              {{ getStatusText(currentMeter.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="安装日期" span="3">{{ currentMeter.installDate }}</el-descriptions-item>
          <el-descriptions-item label="备注" span="3">{{ currentMeter.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ContentWrap } from '@/components/ContentWrap'
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Refresh, Upload, Download, Connection, Remove, Warning, Collection,
  View, Edit, Delete, Reading, Setting
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMode = ref<'add' | 'edit'>('add')
const activeTab = ref('basic')
const currentMeter = ref(null)

// 搜索表单
const searchForm = reactive({
  meterName: '',
  meterType: '',
  location: '',
  status: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表单数据
const formData = reactive({
  id: '',
  meterCode: '',
  meterName: '',
  meterType: '',
  unit: '',
  model: '',
  manufacturer: '',
  location: '',
  installDate: '',
  protocol: '',
  deviceAddress: '',
  ipAddress: '',
  port: 502,
  baudRate: '9600',
  dataBits: '8',
  stopBits: '1',
  parity: 'none',
  ratio: 1,
  pulseConstant: 1000,
  initialValue: 0,
  collectInterval: '15',
  alarmHigh: 10000,
  alarmLow: 0,
  remark: ''
})

// 表单验证规则
const formRules = {
  meterCode: [{ required: true, message: '请输入表计编号', trigger: 'blur' }],
  meterName: [{ required: true, message: '请输入表计名称', trigger: 'blur' }],
  meterType: [{ required: true, message: '请选择表计类型', trigger: 'change' }],
  unit: [{ required: true, message: '请选择计量单位', trigger: 'change' }],
  model: [{ required: true, message: '请输入设备型号', trigger: 'blur' }],
  location: [{ required: true, message: '请输入安装位置', trigger: 'blur' }],
  ipAddress: [
    { required: true, message: '请输入IP地址', trigger: 'blur' },
    { pattern: /^(\d{1,3}\.){3}\d{1,3}$/, message: '请输入正确的IP地址', trigger: 'blur' }
  ]
}

// 统计数据
const stats = reactive({
  onlineCount: 45,
  offlineCount: 8,
  faultCount: 3,
  totalCount: 56
})

// 表计列表数据
const meterList = ref([
  {
    id: '1',
    meterCode: 'EM001',
    meterName: '1号楼总电表',
    meterType: 'electricity',
    unit: 'kWh',
    model: 'DTSU666',
    manufacturer: '长沙威胜',
    location: '1号楼配电室',
    ipAddress: '192.168.1.101',
    deviceAddress: '01',
    protocol: 'Modbus TCP',
    currentValue: 12580.5,
    lastReadTime: '2024-01-22 14:30:00',
    status: 'online',
    installDate: '2023-01-15',
    remark: '主电表'
  },
  {
    id: '2',
    meterCode: 'WM001',
    meterName: '园区总水表',
    meterType: 'water',
    unit: 'm³',
    model: 'WM-100',
    manufacturer: '三川智慧',
    location: '水泵房',
    ipAddress: '192.168.1.102',
    deviceAddress: '02',
    protocol: 'CJ/T188',
    currentValue: 8960.2,
    lastReadTime: '2024-01-22 14:00:00',
    status: 'online',
    installDate: '2023-02-20',
    remark: '主水表'
  },
  {
    id: '3',
    meterCode: 'GM001',
    meterName: '食堂燃气表',
    meterType: 'gas',
    unit: 'm³',
    model: 'GAS-200',
    manufacturer: '金卡智能',
    location: '食堂厨房',
    ipAddress: '192.168.1.103',
    deviceAddress: '03',
    protocol: 'DL/T645',
    currentValue: 2150.8,
    lastReadTime: '2024-01-22 13:45:00',
    status: 'offline',
    installDate: '2023-03-10',
    remark: '厨房用气表'
  }
])

// 计量单位选项
const unitOptions = computed(() => {
  const unitMap = {
    electricity: ['kWh', 'MWh', 'kW', 'MW', 'A', 'V'],
    water: ['m³', 'L', 't'],
    gas: ['m³', 'Nm³'],
    steam: ['t', 'kg', 'GJ'],
    heat: ['GJ', 'MJ', 'kW', 'MW']
  }
  return unitMap[formData.meterType] || []
})

const formRef = ref()

// 获取表计类型颜色
const getMeterTypeColor = (type: string) => {
  const colors = {
    electricity: 'primary',
    water: 'success',
    gas: 'warning',
    steam: 'danger',
    heat: 'info'
  }
  return colors[type] || 'info'
}

// 获取表计类型文本
const getMeterTypeText = (type: string) => {
  const texts = {
    electricity: '电表',
    water: '水表',
    gas: '燃气表',
    steam: '蒸汽表',
    heat: '热量表'
  }
  return texts[type] || type
}

// 获取状态颜色
const getStatusColor = (status: string) => {
  const colors = {
    online: 'success',
    offline: 'info',
    fault: 'danger',
    maintenance: 'warning'
  }
  return colors[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const texts = {
    online: '在线',
    offline: '离线',
    fault: '故障',
    maintenance: '维护'
  }
  return texts[status] || status
}

// 事件处理
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    meterName: '',
    meterType: '',
    location: '',
    status: ''
  })
  handleSearch()
}

const handleImport = () => {
  ElMessage.success('导入功能开发中...')
}

const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

const handleScanDevices = async () => {
  loading.value = true
  try {
    // 模拟扫描过程
    await new Promise(resolve => setTimeout(resolve, 3000))
    ElMessage.success('设备扫描完成，发现2台新设备')
  } finally {
    loading.value = false
  }
}

const handleTypeChange = () => {
  // 根据表计类型重置单位
  formData.unit = ''
}

const handleCreate = () => {
  dialogMode.value = 'add'
  dialogTitle.value = '新增表计'
  activeTab.value = 'basic'
  resetFormData()
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑表计'
  activeTab.value = 'basic'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleView = (row: any) => {
  currentMeter.value = row
  detailDialogVisible.value = true
}

const handleReadData = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认立即读取该表计数据吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    loading.value = true
    // 模拟读数过程
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // 更新读数
    row.currentValue = (row.currentValue + Math.random() * 10).toFixed(2)
    row.lastReadTime = new Date().toLocaleString('zh-CN')
    
    ElMessage.success('读数成功')
  } catch {
    // 用户取消
  } finally {
    loading.value = false
  }
}

const handleCalibrate = (row: any) => {
  ElMessage.info('校准功能开发中...')
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该表计吗？删除后无法恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const index = meterList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      meterList.value.splice(index, 1)
    }
    
    ElMessage.success('删除成功')
  } catch {
    // 用户取消删除
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (dialogMode.value === 'add') {
      const newMeter = {
        ...formData,
        id: `meter_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        currentValue: formData.initialValue,
        lastReadTime: '-',
        status: 'offline'
      }
      meterList.value.unshift(newMeter)
      ElMessage.success('创建成功')
    } else if (dialogMode.value === 'edit') {
      const index = meterList.value.findIndex(item => item.id === formData.id)
      if (index > -1) {
        Object.assign(meterList.value[index], { ...formData })
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
    meterCode: '',
    meterName: '',
    meterType: '',
    unit: '',
    model: '',
    manufacturer: '',
    location: '',
    installDate: '',
    protocol: '',
    deviceAddress: '',
    ipAddress: '',
    port: 502,
    baudRate: '9600',
    dataBits: '8',
    stopBits: '1',
    parity: 'none',
    ratio: 1,
    pulseConstant: 1000,
    initialValue: 0,
    collectInterval: '15',
    alarmHigh: 10000,
    alarmLow: 0,
    remark: ''
  })
}

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    pagination.total = meterList.value.length
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

  .stats-container {
    margin-bottom: 20px;

    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;

        .stat-icon {
          width: 50px;
          height: 50px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
          color: #fff;

          &.online {
            background: linear-gradient(45deg, #67c23a, #85ce61);
          }

          &.offline {
            background: linear-gradient(45deg, #909399, #b3b3b3);
          }

          &.fault {
            background: linear-gradient(45deg, #f56c6c, #f78989);
          }

          &.total {
            background: linear-gradient(45deg, #409eff, #66b1ff);
          }
        }

        .stat-info {
          .stat-value {
            font-size: 24px;
            font-weight: bold;
            color: #303133;
            line-height: 1;
          }

          .stat-label {
            font-size: 14px;
            color: #909399;
            margin-top: 4px;
          }
        }
      }
    }
  }

  .table-container {
    background: #1a1a1a;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .blinking {
      animation: blink 2s infinite;
    }

    @keyframes blink {
      0%, 50% {
        opacity: 1;
      }
      51%, 100% {
        opacity: 0.3;
      }
    }

    .el-pagination {
      margin-top: 20px;
      text-align: right;
    }
  }
}

.detail-content {
  .el-descriptions {
    margin-bottom: 20px;
  }
}
</style>






