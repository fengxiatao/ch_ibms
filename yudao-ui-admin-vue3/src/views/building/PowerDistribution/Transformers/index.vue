<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧建筑</el-breadcrumb-item>
        <el-breadcrumb-item>变配电</el-breadcrumb-item>
        <el-breadcrumb-item>变压器</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon online">
                <el-icon><Lightning /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ transformerStats.total }}</div>
                <div class="stats-label">变压器总数</div>
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
                <div class="stats-value">{{ transformerStats.running }}</div>
                <div class="stats-label">运行中</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon load">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ transformerStats.avgLoad }}%</div>
                <div class="stats-label">平均负载</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon power">
                <el-icon><Cpu /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ transformerStats.totalPower }}</div>
                <div class="stats-label">总容量(kVA)</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 查询条件 -->
    <div class="search-container">
      <el-form :model="searchForm" label-width="80px" :inline="true">
        <el-form-item label="变压器名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入变压器名称"
            clearable
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="变压器类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable style="width: 150px;">
            <el-option label="干式变压器" value="dry_type" />
            <el-option label="油浸式变压器" value="oil_immersed" />
            <el-option label="箱式变压器" value="box_type" />
          </el-select>
        </el-form-item>
        <el-form-item label="运行状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px;">
            <el-option label="运行" value="running" />
            <el-option label="停机" value="stopped" />
            <el-option label="故障" value="fault" />
            <el-option label="维护" value="maintenance" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属配电室">
          <el-select v-model="searchForm.location" placeholder="请选择配电室" clearable style="width: 150px;">
            <el-option label="高压配电室" value="high_voltage" />
            <el-option label="低压配电室" value="low_voltage" />
            <el-option label="1号配电室" value="room_1" />
            <el-option label="2号配电室" value="room_2" />
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
            新增变压器
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 变压器列表 -->
    <div class="transformer-list">
      <el-card class="list-card">
        <template #header>
          <div class="card-header">
            <span>变压器列表</span>
            <div class="header-actions">
              <el-button-group size="small">
                <el-button :type="viewMode === 'table' ? 'primary' : ''" @click="viewMode = 'table'">
                  表格视图
                </el-button>
                <el-button :type="viewMode === 'card' ? 'primary' : ''" @click="viewMode = 'card'">
                  卡片视图
                </el-button>
              </el-button-group>
            </div>
          </div>
        </template>

        <!-- 表格视图 -->
        <div v-if="viewMode === 'table'">
          <el-table :data="transformerList" stripe border style="width: 100%">
            <el-table-column prop="code" label="设备编号" width="120" />
            <el-table-column prop="name" label="变压器名称" width="150" />
            <el-table-column prop="type" label="类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getTypeColor(row.type)">
                  {{ getTypeText(row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="capacity" label="容量(kVA)" width="100" />
            <el-table-column prop="voltage" label="电压等级" width="120" />
            <el-table-column prop="location" label="所属配电室" width="120" />
            <el-table-column prop="status" label="运行状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusColor(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="load" label="负载率" width="100">
              <template #default="{ row }">
                <el-progress 
                  :percentage="row.load" 
                  :color="getLoadColor(row.load)"
                  :show-text="false"
                  size="small"
                />
                <span style="margin-left: 8px;">{{ row.load }}%</span>
              </template>
            </el-table-column>
            <el-table-column prop="temperature" label="温度(°C)" width="100" />
            <el-table-column prop="oilLevel" label="油位(mm)" width="100" />
            <el-table-column prop="lastMaintenance" label="上次维护" width="120" />
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
                <el-button link type="success" size="small" @click="handleMonitor(row)">
                  <el-icon><Monitor /></el-icon>
                  监控
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
            <el-col v-for="transformer in transformerList" :key="transformer.id" :span="8">
              <el-card class="transformer-card" :class="{ 'transformer-alarm': transformer.hasAlarm }">
                <template #header>
                  <div class="transformer-header">
                    <div class="transformer-title">
                      <el-icon class="transformer-icon"><Lightning /></el-icon>
                      <span>{{ transformer.name }}</span>
                    </div>
                    <el-tag :type="getStatusColor(transformer.status)" size="small">
                      {{ getStatusText(transformer.status) }}
                    </el-tag>
                  </div>
                </template>
                
                <div class="transformer-content">
                  <div class="transformer-info">
                    <div class="info-row">
                      <span class="label">设备编号:</span>
                      <span class="value">{{ transformer.code }}</span>
                    </div>
                    <div class="info-row">
                      <span class="label">变压器类型:</span>
                      <span class="value">{{ getTypeText(transformer.type) }}</span>
                    </div>
                    <div class="info-row">
                      <span class="label">容量:</span>
                      <span class="value">{{ transformer.capacity }} kVA</span>
                    </div>
                    <div class="info-row">
                      <span class="label">电压等级:</span>
                      <span class="value">{{ transformer.voltage }}</span>
                    </div>
                  </div>
                  
                  <div class="transformer-params">
                    <div class="param-item">
                      <div class="param-value">{{ transformer.load }}%</div>
                      <div class="param-label">负载率</div>
                      <el-progress 
                        :percentage="transformer.load" 
                        :color="getLoadColor(transformer.load)"
                        :show-text="false"
                        size="small"
                      />
                    </div>
                    <div class="param-item">
                      <div class="param-value">{{ transformer.temperature }}°C</div>
                      <div class="param-label">温度</div>
                    </div>
                    <div class="param-item">
                      <div class="param-value">{{ transformer.oilLevel }}mm</div>
                      <div class="param-label">油位</div>
                    </div>
                  </div>
                  
                  <div class="transformer-actions">
                    <el-button size="small" @click="handleView(transformer)">查看</el-button>
                    <el-button size="small" type="primary" @click="handleMonitor(transformer)">监控</el-button>
                    <el-button size="small" type="success" @click="handleEdit(transformer)">编辑</el-button>
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

    <!-- 变压器详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="变压器详情" width="80%" draggable>
      <div v-if="currentTransformer" class="transformer-detail">
        <el-tabs v-model="activeTab" type="border-card">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <div class="basic-info">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form label-width="100px">
                    <el-form-item label="设备编号">
                      <span>{{ currentTransformer.code }}</span>
                    </el-form-item>
                    <el-form-item label="变压器名称">
                      <span>{{ currentTransformer.name }}</span>
                    </el-form-item>
                    <el-form-item label="变压器类型">
                      <span>{{ getTypeText(currentTransformer.type) }}</span>
                    </el-form-item>
                    <el-form-item label="额定容量">
                      <span>{{ currentTransformer.capacity }} kVA</span>
                    </el-form-item>
                    <el-form-item label="电压等级">
                      <span>{{ currentTransformer.voltage }}</span>
                    </el-form-item>
                  </el-form>
                </el-col>
                <el-col :span="12">
                  <el-form label-width="100px">
                    <el-form-item label="制造商">
                      <span>{{ currentTransformer.manufacturer }}</span>
                    </el-form-item>
                    <el-form-item label="型号规格">
                      <span>{{ currentTransformer.model }}</span>
                    </el-form-item>
                    <el-form-item label="所属配电室">
                      <span>{{ currentTransformer.location }}</span>
                    </el-form-item>
                    <el-form-item label="投运日期">
                      <span>{{ currentTransformer.commissionDate }}</span>
                    </el-form-item>
                    <el-form-item label="保修期限">
                      <span>{{ currentTransformer.warrantyPeriod }}</span>
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
                    <template #header>电压参数</template>
                    <div class="param-list">
                      <div class="param-row">
                        <span>高压侧电压:</span>
                        <span class="param-value">{{ currentTransformer.highVoltage }} kV</span>
                      </div>
                      <div class="param-row">
                        <span>低压侧电压:</span>
                        <span class="param-value">{{ currentTransformer.lowVoltage }} V</span>
                      </div>
                      <div class="param-row">
                        <span>电压偏差:</span>
                        <span class="param-value">{{ currentTransformer.voltageDeviation }}%</span>
                      </div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card class="param-card">
                    <template #header>电流参数</template>
                    <div class="param-list">
                      <div class="param-row">
                        <span>A相电流:</span>
                        <span class="param-value">{{ currentTransformer.currentA }} A</span>
                      </div>
                      <div class="param-row">
                        <span>B相电流:</span>
                        <span class="param-value">{{ currentTransformer.currentB }} A</span>
                      </div>
                      <div class="param-row">
                        <span>C相电流:</span>
                        <span class="param-value">{{ currentTransformer.currentC }} A</span>
                      </div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card class="param-card">
                    <template #header>功率参数</template>
                    <div class="param-list">
                      <div class="param-row">
                        <span>有功功率:</span>
                        <span class="param-value">{{ currentTransformer.activePower }} kW</span>
                      </div>
                      <div class="param-row">
                        <span>无功功率:</span>
                        <span class="param-value">{{ currentTransformer.reactivePower }} kVar</span>
                      </div>
                      <div class="param-row">
                        <span>功率因数:</span>
                        <span class="param-value">{{ currentTransformer.powerFactor }}</span>
                      </div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
              
              <el-row :gutter="20" style="margin-top: 20px;">
                <el-col :span="12">
                  <el-card class="param-card">
                    <template #header>温度监测</template>
                    <div class="param-list">
                      <div class="param-row">
                        <span>绕组温度:</span>
                        <span class="param-value">{{ currentTransformer.windingTemp }}°C</span>
                      </div>
                      <div class="param-row">
                        <span>铁芯温度:</span>
                        <span class="param-value">{{ currentTransformer.coreTemp }}°C</span>
                      </div>
                      <div class="param-row">
                        <span>油温:</span>
                        <span class="param-value">{{ currentTransformer.oilTemp }}°C</span>
                      </div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card class="param-card">
                    <template #header>其他参数</template>
                    <div class="param-list">
                      <div class="param-row">
                        <span>负载率:</span>
                        <span class="param-value">{{ currentTransformer.load }}%</span>
                      </div>
                      <div class="param-row">
                        <span>油位:</span>
                        <span class="param-value">{{ currentTransformer.oilLevel }} mm</span>
                      </div>
                      <div class="param-row">
                        <span>绝缘电阻:</span>
                        <span class="param-value">{{ currentTransformer.insulationResistance }} MΩ</span>
                      </div>
                    </div>
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

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" :title="isEdit ? '编辑变压器' : '新增变压器'" width="60%" draggable>
      <el-form ref="transformerFormRef" :model="transformerForm" :rules="transformerRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备编号" prop="code">
              <el-input v-model="transformerForm.code" placeholder="请输入设备编号" />
            </el-form-item>
            <el-form-item label="变压器名称" prop="name">
              <el-input v-model="transformerForm.name" placeholder="请输入变压器名称" />
            </el-form-item>
            <el-form-item label="变压器类型" prop="type">
              <el-select v-model="transformerForm.type" placeholder="请选择类型" style="width: 100%;">
                <el-option label="干式变压器" value="dry_type" />
                <el-option label="油浸式变压器" value="oil_immersed" />
                <el-option label="箱式变压器" value="box_type" />
              </el-select>
            </el-form-item>
            <el-form-item label="额定容量" prop="capacity">
              <el-input-number v-model="transformerForm.capacity" :min="0" style="width: 100%;" />
              <span style="margin-left: 8px;">kVA</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电压等级" prop="voltage">
              <el-input v-model="transformerForm.voltage" placeholder="如: 10kV/0.4kV" />
            </el-form-item>
            <el-form-item label="制造商">
              <el-input v-model="transformerForm.manufacturer" placeholder="请输入制造商" />
            </el-form-item>
            <el-form-item label="型号规格">
              <el-input v-model="transformerForm.model" placeholder="请输入型号规格" />
            </el-form-item>
            <el-form-item label="所属配电室" prop="location">
              <el-select v-model="transformerForm.location" placeholder="请选择配电室" style="width: 100%;">
                <el-option label="高压配电室" value="high_voltage" />
                <el-option label="低压配电室" value="low_voltage" />
                <el-option label="1号配电室" value="room_1" />
                <el-option label="2号配电室" value="room_2" />
              </el-select>
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
  Lightning, VideoPlay, TrendCharts, Cpu, Search, Refresh, Plus,
  View, Edit, Monitor, Delete
} from '@element-plus/icons-vue'

// 响应式数据
const viewMode = ref('table')
const detailDialogVisible = ref(false)
const editDialogVisible = ref(false)
const isEdit = ref(false)
const activeTab = ref('basic')
const currentTransformer = ref(null)

// 变压器统计
const transformerStats = reactive({
  total: 8,
  running: 6,
  avgLoad: 75,
  totalPower: 4800
})

// 搜索表单
const searchForm = reactive({
  name: '',
  type: '',
  status: '',
  location: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 变压器表单
const transformerForm = reactive({
  code: '',
  name: '',
  type: '',
  capacity: 0,
  voltage: '',
  manufacturer: '',
  model: '',
  location: ''
})

// 表单验证规则
const transformerRules = {
  code: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入变压器名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择变压器类型', trigger: 'change' }],
  capacity: [{ required: true, message: '请输入额定容量', trigger: 'blur' }],
  voltage: [{ required: true, message: '请输入电压等级', trigger: 'blur' }],
  location: [{ required: true, message: '请选择所属配电室', trigger: 'change' }]
}

// 变压器列表数据
const transformerList = ref([
  {
    id: 1,
    code: 'T-001',
    name: '1号主变压器',
    type: 'oil_immersed',
    capacity: 1000,
    voltage: '10kV/0.4kV',
    location: '高压配电室',
    status: 'running',
    load: 78,
    temperature: 65,
    oilLevel: 850,
    lastMaintenance: '2024-01-15',
    hasAlarm: false,
    manufacturer: 'ABB',
    model: 'ONAN-1000/10',
    commissionDate: '2020-05-15',
    warrantyPeriod: '5年',
    highVoltage: 10.2,
    lowVoltage: 385,
    voltageDeviation: -3.8,
    currentA: 145,
    currentB: 142,
    currentC: 148,
    activePower: 780,
    reactivePower: 320,
    powerFactor: 0.92,
    windingTemp: 68,
    coreTemp: 62,
    oilTemp: 65,
    insulationResistance: 2500
  },
  {
    id: 2,
    code: 'T-002',
    name: '2号主变压器',
    type: 'oil_immersed',
    capacity: 1000,
    voltage: '10kV/0.4kV',
    location: '高压配电室',
    status: 'running',
    load: 65,
    temperature: 58,
    oilLevel: 870,
    lastMaintenance: '2024-01-10',
    hasAlarm: false,
    manufacturer: 'ABB',
    model: 'ONAN-1000/10',
    commissionDate: '2020-05-15',
    warrantyPeriod: '5年',
    highVoltage: 10.1,
    lowVoltage: 388,
    voltageDeviation: -3.0,
    currentA: 125,
    currentB: 128,
    currentC: 122,
    activePower: 650,
    reactivePower: 280,
    powerFactor: 0.90,
    windingTemp: 61,
    coreTemp: 55,
    oilTemp: 58,
    insulationResistance: 2800
  },
  {
    id: 3,
    code: 'T-003',
    name: '应急变压器',
    type: 'dry_type',
    capacity: 630,
    voltage: '10kV/0.4kV',
    location: '1号配电室',
    status: 'stopped',
    load: 0,
    temperature: 25,
    oilLevel: 0,
    lastMaintenance: '2024-01-20',
    hasAlarm: false,
    manufacturer: '西门子',
    model: 'GEAFOL-630/10',
    commissionDate: '2021-03-10',
    warrantyPeriod: '3年',
    highVoltage: 0,
    lowVoltage: 0,
    voltageDeviation: 0,
    currentA: 0,
    currentB: 0,
    currentC: 0,
    activePower: 0,
    reactivePower: 0,
    powerFactor: 0,
    windingTemp: 25,
    coreTemp: 25,
    oilTemp: 0,
    insulationResistance: 5000
  }
])

// 维护记录数据
const maintenanceRecords = ref([
  {
    date: '2024-01-15',
    type: '定期保养',
    content: '检查绝缘油质量、清洁散热器、检查接线端子',
    operator: '张师傅',
    cost: 2500,
    nextDate: '2024-07-15'
  },
  {
    date: '2023-07-20',
    type: '预防性维护',
    content: '更换绝缘油、检查分接开关、测试保护装置',
    operator: '李工程师',
    cost: 8500,
    nextDate: '2024-01-20'
  }
])

// 计算属性和方法
const getTypeColor = (type: string) => {
  const colors = {
    dry_type: 'primary',
    oil_immersed: 'success',
    box_type: 'warning'
  }
  return colors[type] || 'info'
}

const getTypeText = (type: string) => {
  const texts = {
    dry_type: '干式变压器',
    oil_immersed: '油浸式变压器',
    box_type: '箱式变压器'
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
    stopped: '停机',
    fault: '故障',
    maintenance: '维护'
  }
  return texts[status] || status
}

const getLoadColor = (load: number) => {
  if (load >= 90) return '#f56c6c'
  if (load >= 80) return '#e6a23c'
  if (load >= 60) return '#409eff'
  return '#67c23a'
}

// 事件处理
const handleSearch = () => {
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    type: '',
    status: '',
    location: ''
  })
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(transformerForm, {
    code: '',
    name: '',
    type: '',
    capacity: 0,
    voltage: '',
    manufacturer: '',
    model: '',
    location: ''
  })
  editDialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  Object.assign(transformerForm, {
    code: row.code,
    name: row.name,
    type: row.type,
    capacity: row.capacity,
    voltage: row.voltage,
    manufacturer: row.manufacturer,
    model: row.model,
    location: row.location
  })
  editDialogVisible.value = true
}

const handleView = (row: any) => {
  currentTransformer.value = row
  activeTab.value = 'basic'
  detailDialogVisible.value = true
}

const handleMonitor = (row: any) => {
  ElMessage.info(`进入${row.name}监控界面`)
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认删除变压器 "${row.name}" 吗？`, '提示', {
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

const handleSave = () => {
  ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
  editDialogVisible.value = false
  loadData()
}

const loadData = () => {
  console.log('Loading transformer data...', searchForm)
  pagination.total = transformerList.value.length
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
            background: linear-gradient(135deg, #409eff, #66b1ff);
          }
          
          &.running {
            background: linear-gradient(135deg, #67c23a, #85ce61);
          }
          
          &.load {
            background: linear-gradient(135deg, #e6a23c, #ebb563);
          }
          
          &.power {
            background: linear-gradient(135deg, #f56c6c, #f78989);
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

  .transformer-list {
    .list-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
    }

    .card-view {
      .transformer-card {
        margin-bottom: 20px;
        transition: all 0.3s;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }

        &.transformer-alarm {
          border-left: 4px solid #f56c6c;
        }

        .transformer-header {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .transformer-title {
            display: flex;
            align-items: center;
            gap: 8px;

            .transformer-icon {
              color: #409eff;
            }
          }
        }

        .transformer-content {
          .transformer-info {
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

          .transformer-params {
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
                margin-bottom: 4px;
              }

              .param-label {
                font-size: 12px;
                color: #909399;
                margin-bottom: 8px;
              }
            }
          }

          .transformer-actions {
            display: flex;
            justify-content: space-around;
          }
        }
      }
    }
  }

  .transformer-detail {
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

    .maintenance-records {
      .el-table {
        margin-top: 0;
      }
    }
  }
}
</style>






