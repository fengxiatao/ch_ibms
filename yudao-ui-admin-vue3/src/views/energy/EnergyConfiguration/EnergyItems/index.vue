<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧能源</el-breadcrumb-item>
        <el-breadcrumb-item>能耗配置</el-breadcrumb-item>
        <el-breadcrumb-item>能耗分项</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :model="searchForm" label-width="80px" :inline="true">
        <el-form-item label="分项名称">
          <el-input v-model="searchForm.itemName" placeholder="请输入分项名称" clearable />
        </el-form-item>
        <el-form-item label="能源类型">
          <el-select v-model="searchForm.energyType" placeholder="请选择能源类型" clearable>
            <el-option label="电" value="electricity" />
            <el-option label="水" value="water" />
            <el-option label="燃气" value="gas" />
            <el-option label="蒸汽" value="steam" />
            <el-option label="制冷" value="cooling" />
            <el-option label="采暖" value="heating" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="enabled" />
            <el-option label="禁用" value="disabled" />
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
        <h2>能耗分项管理</h2>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增分项
        </el-button>
        <el-button type="success" @click="handleImport">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
        <el-button type="info" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-container">
      <el-row :gutter="20">
        <el-col :span="4">
          <el-card class="stat-card electricity">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><Lightning /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.electricityCount }}</div>
                <div class="stat-label">电力分项</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card water">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><Drizzling /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.waterCount }}</div>
                <div class="stat-label">用水分项</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card gas">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><Sunny /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.gasCount }}</div>
                <div class="stat-label">燃气分项</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card steam">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><Cloudy /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.steamCount }}</div>
                <div class="stat-label">蒸汽分项</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card cooling">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><Refrigerator /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.coolingCount }}</div>
                <div class="stat-label">制冷分项</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card heating">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><Sunrise /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.heatingCount }}</div>
                <div class="stat-label">采暖分项</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 分项列表 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="itemList"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="itemCode" label="分项编码" width="120" />
        <el-table-column prop="itemName" label="分项名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="energyType" label="能源类型" width="100">
          <template #default="{ row }">
            <div class="energy-type">
              <el-icon class="energy-icon" :style="{ color: getEnergyTypeColor(row.energyType) }">
                <component :is="getEnergyTypeIcon(row.energyType)" />
              </el-icon>
              <span>{{ getEnergyTypeText(row.energyType) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="计量单位" width="100" />
        <el-table-column prop="deviceCount" label="设备数量" width="100" />
        <el-table-column prop="totalConsumption" label="总能耗" width="120">
          <template #default="{ row }">
            <span class="consumption-value">{{ row.totalConsumption?.toLocaleString() || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="proportion" label="占比" width="80">
          <template #default="{ row }">
            <span class="proportion-value">{{ row.proportion }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="manager" label="负责人" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="enabled"
              inactive-value="disabled"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" show-overflow-tooltip />
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
            <el-button link type="success" @click="handleBindDevices(row)">
              <el-icon><Link /></el-icon>
              绑定设备
            </el-button>
            <el-button link type="info" @click="handleAnalysis(row)">
              <el-icon><DataAnalysis /></el-icon>
              分析
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

    <!-- 新增/编辑分项弹框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分项编码" prop="itemCode">
              <el-input v-model="formData.itemCode" placeholder="请输入分项编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分项名称" prop="itemName">
              <el-input v-model="formData.itemName" placeholder="请输入分项名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="能源类型" prop="energyType">
              <el-select v-model="formData.energyType" placeholder="请选择能源类型" @change="handleEnergyTypeChange">
                <el-option label="电" value="electricity" />
                <el-option label="水" value="water" />
                <el-option label="燃气" value="gas" />
                <el-option label="蒸汽" value="steam" />
                <el-option label="制冷" value="cooling" />
                <el-option label="采暖" value="heating" />
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
            <el-form-item label="负责人" prop="manager">
              <el-input v-model="formData.manager" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number
                v-model="formData.sort"
                :min="0"
                :max="9999"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio label="enabled">启用</el-radio>
                <el-radio label="disabled">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="计算规则">
          <el-radio-group v-model="formData.calculationRule">
            <el-radio label="sum">累加</el-radio>
            <el-radio label="average">平均</el-radio>
            <el-radio label="max">最大值</el-radio>
            <el-radio label="min">最小值</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分项描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分项描述"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 绑定设备弹框 -->
    <el-dialog
      v-model="bindDialogVisible"
      title="绑定设备"
      width="1000px"
      destroy-on-close
    >
      <div class="bind-device-content">
        <div class="search-section">
          <el-input
            v-model="deviceSearchKeyword"
            placeholder="搜索设备名称或编号"
            clearable
            @input="handleDeviceSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        
        <div class="device-transfer">
          <el-transfer
            v-model="selectedDevices"
            :data="deviceOptions"
            :titles="['可选设备', '已绑定设备']"
            :button-texts="['移除', '绑定']"
            filterable
            filter-placeholder="搜索设备"
          >
            <template #default="{ option }">
              <div class="device-item">
                <div class="device-info">
                  <span class="device-name">{{ option.label }}</span>
                  <span class="device-type">{{ option.type }}</span>
                </div>
                <div class="device-status">
                  <el-tag :type="option.status === 'online' ? 'success' : 'danger'" size="small">
                    {{ option.status === 'online' ? '在线' : '离线' }}
                  </el-tag>
                </div>
              </div>
            </template>
          </el-transfer>
        </div>
      </div>

      <template #footer>
        <el-button @click="bindDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveDeviceBinding">确认绑定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="分项详情"
      width="1000px"
      destroy-on-close
    >
      <div v-if="currentItem" class="detail-content">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="分项编码">{{ currentItem.itemCode }}</el-descriptions-item>
          <el-descriptions-item label="分项名称">{{ currentItem.itemName }}</el-descriptions-item>
          <el-descriptions-item label="能源类型">
            <div class="energy-type">
              <el-icon class="energy-icon" :style="{ color: getEnergyTypeColor(currentItem.energyType) }">
                <component :is="getEnergyTypeIcon(currentItem.energyType)" />
              </el-icon>
              <span>{{ getEnergyTypeText(currentItem.energyType) }}</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="计量单位">{{ currentItem.unit }}</el-descriptions-item>
          <el-descriptions-item label="设备数量">{{ currentItem.deviceCount }} 个</el-descriptions-item>
          <el-descriptions-item label="总能耗">{{ currentItem.totalConsumption?.toLocaleString() || 0 }} {{ currentItem.unit }}</el-descriptions-item>
          <el-descriptions-item label="占比">{{ currentItem.proportion }}%</el-descriptions-item>
          <el-descriptions-item label="负责人">{{ currentItem.manager }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentItem.phone }}</el-descriptions-item>
          <el-descriptions-item label="计算规则">{{ getCalculationRuleText(currentItem.calculationRule) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentItem.status === 'enabled' ? 'success' : 'danger'">
              {{ currentItem.status === 'enabled' ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentItem.createTime }}</el-descriptions-item>
          <el-descriptions-item label="分项描述" span="3">{{ currentItem.description || '无' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 绑定设备列表 -->
        <div class="bound-devices-section">
          <h4>绑定设备</h4>
          <el-table :data="mockBoundDevices" size="small" border>
            <el-table-column prop="deviceName" label="设备名称" />
            <el-table-column prop="deviceCode" label="设备编号" />
            <el-table-column prop="deviceType" label="设备类型" />
            <el-table-column prop="currentValue" label="当前读数" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="row.status === 'online' ? 'success' : 'danger'" size="small">
                  {{ row.status === 'online' ? '在线' : '离线' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lastUpdateTime" label="最近更新时间" />
          </el-table>
        </div>
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
  Plus, Search, Refresh, Upload, Download, View, Edit, Delete, Link, 
  DataAnalysis, Lightning, Drizzling, Sunny, Cloudy, Refrigerator, Sunrise
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const bindDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMode = ref<'add' | 'edit'>('add')
const currentItem = ref(null)
const deviceSearchKeyword = ref('')
const selectedDevices = ref([])

// 搜索表单
const searchForm = reactive({
  itemName: '',
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
const formData = reactive({
  id: '',
  itemCode: '',
  itemName: '',
  energyType: '',
  unit: '',
  manager: '',
  phone: '',
  sort: 0,
  status: 'enabled',
  calculationRule: 'sum',
  description: ''
})

// 表单验证规则
const formRules = {
  itemCode: [{ required: true, message: '请输入分项编码', trigger: 'blur' }],
  itemName: [{ required: true, message: '请输入分项名称', trigger: 'blur' }],
  energyType: [{ required: true, message: '请选择能源类型', trigger: 'change' }],
  unit: [{ required: true, message: '请选择计量单位', trigger: 'change' }],
  manager: [{ required: true, message: '请输入负责人', trigger: 'blur' }]
}

// 统计数据
const stats = reactive({
  electricityCount: 8,
  waterCount: 3,
  gasCount: 2,
  steamCount: 1,
  coolingCount: 4,
  heatingCount: 2
})

// 分项列表数据
const itemList = ref([
  {
    id: '1',
    itemCode: 'ITEM001',
    itemName: '照明用电',
    energyType: 'electricity',
    unit: 'kWh',
    deviceCount: 24,
    totalConsumption: 15680,
    proportion: 25.8,
    manager: '张三',
    phone: '13800138000',
    sort: 1,
    status: 'enabled',
    calculationRule: 'sum',
    description: '所有照明设备的用电量统计',
    createTime: '2024-01-15 09:30:00'
  },
  {
    id: '2',
    itemCode: 'ITEM002',
    itemName: '空调用电',
    energyType: 'electricity',
    unit: 'kWh',
    deviceCount: 18,
    totalConsumption: 28450,
    proportion: 46.8,
    manager: '李四',
    phone: '13800138001',
    sort: 2,
    status: 'enabled',
    calculationRule: 'sum',
    description: '所有空调设备的用电量统计',
    createTime: '2024-01-15 10:00:00'
  },
  {
    id: '3',
    itemCode: 'ITEM003',
    itemName: '办公用水',
    energyType: 'water',
    unit: 'm³',
    deviceCount: 8,
    totalConsumption: 1250,
    proportion: 75.2,
    manager: '王五',
    phone: '13800138002',
    sort: 3,
    status: 'enabled',
    calculationRule: 'sum',
    description: '办公区域用水量统计',
    createTime: '2024-01-15 11:00:00'
  }
])

// 设备选项
const deviceOptions = ref([
  { key: 'device_001', label: '照明控制器-001', type: '照明', status: 'online' },
  { key: 'device_002', label: '空调主机-A1', type: '空调', status: 'online' },
  { key: 'device_003', label: '智能电表-EM001', type: '电表', status: 'online' },
  { key: 'device_004', label: '水表-WM001', type: '水表', status: 'offline' },
  { key: 'device_005', label: '燃气表-GM001', type: '燃气表', status: 'online' }
])

// 模拟绑定设备数据
const mockBoundDevices = ref([
  {
    deviceName: '照明控制器-001',
    deviceCode: 'LC001',
    deviceType: '照明',
    currentValue: '125.6 kWh',
    status: 'online',
    lastUpdateTime: '2024-01-22 14:30:00'
  },
  {
    deviceName: '照明控制器-002',
    deviceCode: 'LC002',
    deviceType: '照明',
    currentValue: '98.2 kWh',
    status: 'online',
    lastUpdateTime: '2024-01-22 14:30:00'
  }
])

// 计量单位选项
const unitOptions = computed(() => {
  const unitMap = {
    electricity: ['kWh', 'MWh', 'kW', 'MW'],
    water: ['m³', 'L', 't'],
    gas: ['m³', 'Nm³'],
    steam: ['t', 'kg', 'GJ'],
    cooling: ['kW', 'MW', 'RT'],
    heating: ['kW', 'MW', 'GJ']
  }
  return unitMap[formData.energyType] || []
})

const formRef = ref()

// 获取能源类型图标
const getEnergyTypeIcon = (type: string) => {
  const icons = {
    electricity: Lightning,
    water: Drizzling,
    gas: Sunny,
    steam: Cloudy,
    cooling: Refrigerator,
    heating: Sunrise
  }
  return icons[type] || Lightning
}

// 获取能源类型颜色
const getEnergyTypeColor = (type: string) => {
  const colors = {
    electricity: '#409eff',
    water: '#67c23a',
    gas: '#e6a23c',
    steam: '#909399',
    cooling: '#00d4ff',
    heating: '#f56c6c'
  }
  return colors[type] || '#409eff'
}

// 获取能源类型文本
const getEnergyTypeText = (type: string) => {
  const texts = {
    electricity: '电',
    water: '水',
    gas: '燃气',
    steam: '蒸汽',
    cooling: '制冷',
    heating: '采暖'
  }
  return texts[type] || type
}

// 获取计算规则文本
const getCalculationRuleText = (rule: string) => {
  const texts = {
    sum: '累加',
    average: '平均',
    max: '最大值',
    min: '最小值'
  }
  return texts[rule] || rule
}

// 事件处理
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    itemName: '',
    energyType: '',
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

const handleStatusChange = (row: any) => {
  const statusText = row.status === 'enabled' ? '启用' : '禁用'
  ElMessage.success(`${statusText}成功`)
}

const handleEnergyTypeChange = () => {
  // 根据能源类型重置单位
  formData.unit = ''
}

const handleDeviceSearch = () => {
  // 设备搜索逻辑
}

const handleCreate = () => {
  dialogMode.value = 'add'
  dialogTitle.value = '新增分项'
  resetFormData()
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑分项'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleView = (row: any) => {
  currentItem.value = row
  detailDialogVisible.value = true
}

const handleBindDevices = (row: any) => {
  currentItem.value = row
  // 加载已绑定的设备
  selectedDevices.value = ['device_001', 'device_002']
  bindDialogVisible.value = true
}

const handleAnalysis = (row: any) => {
  ElMessage.info('分析功能开发中...')
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该分项吗？删除后无法恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const index = itemList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      itemList.value.splice(index, 1)
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
      const newItem = {
        ...formData,
        id: `item_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        deviceCount: 0,
        totalConsumption: 0,
        proportion: 0,
        createTime: new Date().toLocaleString('zh-CN')
      }
      itemList.value.unshift(newItem)
      ElMessage.success('创建成功')
    } else if (dialogMode.value === 'edit') {
      const index = itemList.value.findIndex(item => item.id === formData.id)
      if (index > -1) {
        Object.assign(itemList.value[index], { ...formData })
      }
      ElMessage.success('更新成功')
    }
    
    dialogVisible.value = false
  } catch {
    // 表单验证失败
  }
}

const handleSaveDeviceBinding = () => {
  if (currentItem.value) {
    currentItem.value.deviceCount = selectedDevices.value.length
    ElMessage.success('设备绑定成功')
  }
  bindDialogVisible.value = false
}

const resetFormData = () => {
  Object.assign(formData, {
    id: '',
    itemCode: '',
    itemName: '',
    energyType: '',
    unit: '',
    manager: '',
    phone: '',
    sort: 0,
    status: 'enabled',
    calculationRule: 'sum',
    description: ''
  })
}

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    pagination.total = itemList.value.length
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
      &.electricity {
        border-left: 4px solid #409eff;
      }

      &.water {
        border-left: 4px solid #67c23a;
      }

      &.gas {
        border-left: 4px solid #e6a23c;
      }

      &.steam {
        border-left: 4px solid #909399;
      }

      &.cooling {
        border-left: 4px solid #00d4ff;
      }

      &.heating {
        border-left: 4px solid #f56c6c;
      }

      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;

        .stat-icon {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 20px;
          color: #fff;
          background: linear-gradient(45deg, #409eff, #66b1ff);
        }

        .stat-info {
          .stat-value {
            font-size: 20px;
            font-weight: bold;
            color: #303133;
            line-height: 1;
          }

          .stat-label {
            font-size: 12px;
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

    .energy-type {
      display: flex;
      align-items: center;
      gap: 6px;

      .energy-icon {
        font-size: 16px;
      }
    }

    .consumption-value {
      font-weight: 500;
      color: #e6a23c;
    }

    .proportion-value {
      font-weight: 500;
      color: #67c23a;
    }

    .el-pagination {
      margin-top: 20px;
      text-align: right;
    }
  }
}

.bind-device-content {
  .search-section {
    margin-bottom: 20px;
  }

  .device-transfer {
    .device-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;

      .device-info {
        display: flex;
        flex-direction: column;
        gap: 4px;

        .device-name {
          font-weight: 500;
          color: #303133;
        }

        .device-type {
          font-size: 12px;
          color: #909399;
        }
      }

      .device-status {
        margin-left: 8px;
      }
    }
  }
}

.detail-content {
  .el-descriptions {
    margin-bottom: 24px;
  }

  .bound-devices-section {
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
</style>






