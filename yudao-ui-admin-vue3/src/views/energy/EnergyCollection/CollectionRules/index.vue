<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧能源</el-breadcrumb-item>
        <el-breadcrumb-item>能耗采集</el-breadcrumb-item>
        <el-breadcrumb-item>采集规则</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :model="searchForm" label-width="80px" :inline="true">
        <el-form-item label="规则名称">
          <el-input v-model="searchForm.ruleName" placeholder="请输入规则名称" clearable />
        </el-form-item>
        <el-form-item label="数据类型">
          <el-select v-model="searchForm.dataType" placeholder="请选择数据类型" clearable>
            <el-option label="电量" value="electricity" />
            <el-option label="水量" value="water" />
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
        <h2>采集规则管理</h2>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增规则
        </el-button>
        <el-button type="success" @click="handleImport">
          <el-icon><Upload /></el-icon>
          导入规则
        </el-button>
        <el-button type="info" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <!-- 规则列表 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="ruleList"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="ruleName" label="规则名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="dataType" label="数据类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getDataTypeColor(row.dataType)">
              {{ getDataTypeText(row.dataType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="protocol" label="通信协议" width="120" />
        <el-table-column prop="deviceModel" label="设备型号" width="140" />
        <el-table-column prop="registerAddress" label="寄存器地址" width="120" />
        <el-table-column prop="dataFormat" label="数据格式" width="100" />
        <el-table-column prop="coefficient" label="系数" width="80" />
        <el-table-column prop="unit" label="单位" width="80" />
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button link type="success" @click="handleCopy(row)">
              <el-icon><DocumentCopy /></el-icon>
              复制
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

    <!-- 新增/编辑规则弹框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="900px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 基础信息 -->
        <el-tab-pane label="基础信息" name="basic">
          <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="规则名称" prop="ruleName">
                  <el-input v-model="formData.ruleName" placeholder="请输入规则名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="数据类型" prop="dataType">
                  <el-select v-model="formData.dataType" placeholder="请选择数据类型">
                    <el-option label="电量" value="electricity" />
                    <el-option label="水量" value="water" />
                    <el-option label="燃气" value="gas" />
                    <el-option label="蒸汽" value="steam" />
                    <el-option label="制冷" value="cooling" />
                    <el-option label="采暖" value="heating" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="通信协议" prop="protocol">
                  <el-select v-model="formData.protocol" placeholder="请选择通信协议">
                    <el-option label="Modbus RTU" value="modbus_rtu" />
                    <el-option label="Modbus TCP" value="modbus_tcp" />
                    <el-option label="DL/T645" value="dlt645" />
                    <el-option label="CJ/T188" value="cjt188" />
                    <el-option label="OPC UA" value="opc_ua" />
                    <el-option label="MQTT" value="mqtt" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="设备型号" prop="deviceModel">
                  <el-input v-model="formData.deviceModel" placeholder="请输入设备型号" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="寄存器地址" prop="registerAddress">
                  <el-input v-model="formData.registerAddress" placeholder="请输入寄存器地址" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="数据格式" prop="dataFormat">
                  <el-select v-model="formData.dataFormat" placeholder="请选择数据格式">
                    <el-option label="16位整型" value="int16" />
                    <el-option label="32位整型" value="int32" />
                    <el-option label="32位浮点" value="float32" />
                    <el-option label="64位浮点" value="float64" />
                    <el-option label="BCD码" value="bcd" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="系数" prop="coefficient">
                  <el-input-number
                    v-model="formData.coefficient"
                    :precision="3"
                    :step="0.001"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="单位" prop="unit">
                  <el-select v-model="formData.unit" placeholder="请选择单位">
                    <el-option label="kWh" value="kWh" />
                    <el-option label="m³" value="m3" />
                    <el-option label="t" value="t" />
                    <el-option label="GJ" value="GJ" />
                    <el-option label="kW" value="kW" />
                    <el-option label="A" value="A" />
                    <el-option label="V" value="V" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="状态" prop="status">
                  <el-radio-group v-model="formData.status">
                    <el-radio label="enabled">启用</el-radio>
                    <el-radio label="disabled">禁用</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="规则描述">
              <el-input
                v-model="formData.description"
                type="textarea"
                :rows="3"
                placeholder="请输入规则描述"
                maxlength="300"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 高级配置 -->
        <el-tab-pane label="高级配置" name="advanced">
          <el-form :model="formData" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="数据校验">
                  <el-checkbox-group v-model="formData.validations">
                    <el-checkbox label="range_check">范围检查</el-checkbox>
                    <el-checkbox label="crc_check">CRC校验</el-checkbox>
                    <el-checkbox label="duplicate_check">重复检查</el-checkbox>
                  </el-checkbox-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="异常处理">
                  <el-select v-model="formData.errorHandling" placeholder="请选择异常处理方式">
                    <el-option label="忽略" value="ignore" />
                    <el-option label="重试" value="retry" />
                    <el-option label="告警" value="alarm" />
                    <el-option label="使用默认值" value="default" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20" v-if="formData.validations.includes('range_check')">
              <el-col :span="12">
                <el-form-item label="最小值">
                  <el-input-number
                    v-model="formData.minValue"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="最大值">
                  <el-input-number
                    v-model="formData.maxValue"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20" v-if="formData.errorHandling === 'retry'">
              <el-col :span="12">
                <el-form-item label="重试次数">
                  <el-input-number
                    v-model="formData.retryCount"
                    :min="1"
                    :max="10"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="重试间隔">
                  <el-input-number
                    v-model="formData.retryInterval"
                    :min="1"
                    :max="3600"
                    controls-position="right"
                  />
                  <span style="margin-left: 8px;">秒</span>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>

        <!-- 测试配置 -->
        <el-tab-pane label="测试配置" name="test">
          <div class="test-panel">
            <el-form :model="testData" label-width="120px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="测试设备IP">
                    <el-input v-model="testData.deviceIp" placeholder="请输入设备IP地址" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="测试端口">
                    <el-input-number
                      v-model="testData.devicePort"
                      :min="1"
                      :max="65535"
                      controls-position="right"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item>
                <el-button type="primary" @click="handleTestRule" :loading="testing">
                  <el-icon><Connection /></el-icon>
                  测试连接
                </el-button>
                <el-button @click="clearTestResult">清除结果</el-button>
              </el-form-item>
            </el-form>
            
            <div class="test-result" v-if="testResult">
              <h4>测试结果</h4>
              <div class="result-item">
                <span class="label">连接状态:</span>
                <el-tag :type="testResult.success ? 'success' : 'danger'">
                  {{ testResult.success ? '成功' : '失败' }}
                </el-tag>
              </div>
              <div class="result-item" v-if="testResult.success">
                <span class="label">读取数据:</span>
                <span class="value">{{ testResult.data }} {{ formData.unit }}</span>
              </div>
              <div class="result-item">
                <span class="label">响应时间:</span>
                <span class="value">{{ testResult.responseTime }}ms</span>
              </div>
              <div class="result-item" v-if="testResult.error">
                <span class="label">错误信息:</span>
                <span class="error">{{ testResult.error }}</span>
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
      title="规则详情"
      width="800px"
      destroy-on-close
    >
      <div v-if="currentRule" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="规则名称">{{ currentRule.ruleName }}</el-descriptions-item>
          <el-descriptions-item label="数据类型">
            <el-tag :type="getDataTypeColor(currentRule.dataType)">
              {{ getDataTypeText(currentRule.dataType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="通信协议">{{ currentRule.protocol }}</el-descriptions-item>
          <el-descriptions-item label="设备型号">{{ currentRule.deviceModel }}</el-descriptions-item>
          <el-descriptions-item label="寄存器地址">{{ currentRule.registerAddress }}</el-descriptions-item>
          <el-descriptions-item label="数据格式">{{ currentRule.dataFormat }}</el-descriptions-item>
          <el-descriptions-item label="系数">{{ currentRule.coefficient }}</el-descriptions-item>
          <el-descriptions-item label="单位">{{ currentRule.unit }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentRule.status === 'enabled' ? 'success' : 'danger'">
              {{ currentRule.status === 'enabled' ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentRule.createTime }}</el-descriptions-item>
          <el-descriptions-item label="规则描述" span="2">{{ currentRule.description || '无' }}</el-descriptions-item>
        </el-descriptions>
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
  Plus, Search, Refresh, Upload, Download, View, Edit, Delete, 
  DocumentCopy, Connection
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const testing = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMode = ref<'add' | 'edit'>('add')
const activeTab = ref('basic')
const currentRule = ref(null)
const testResult = ref(null)

// 搜索表单
const searchForm = reactive({
  ruleName: '',
  dataType: '',
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
  ruleName: '',
  dataType: '',
  protocol: '',
  deviceModel: '',
  registerAddress: '',
  dataFormat: '',
  coefficient: 1,
  unit: '',
  status: 'enabled',
  description: '',
  validations: [],
  errorHandling: 'retry',
  minValue: 0,
  maxValue: 9999,
  retryCount: 3,
  retryInterval: 5
})

// 测试数据
const testData = reactive({
  deviceIp: '192.168.1.100',
  devicePort: 502
})

// 表单验证规则
const formRules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  dataType: [{ required: true, message: '请选择数据类型', trigger: 'change' }],
  protocol: [{ required: true, message: '请选择通信协议', trigger: 'change' }],
  deviceModel: [{ required: true, message: '请输入设备型号', trigger: 'blur' }],
  registerAddress: [{ required: true, message: '请输入寄存器地址', trigger: 'blur' }],
  dataFormat: [{ required: true, message: '请选择数据格式', trigger: 'change' }],
  unit: [{ required: true, message: '请选择单位', trigger: 'change' }]
}

// 规则列表数据
const ruleList = ref([
  {
    id: '1',
    ruleName: '三相电表有功电能',
    dataType: 'electricity',
    protocol: 'Modbus RTU',
    deviceModel: 'DTSU666',
    registerAddress: '0x0000',
    dataFormat: 'int32',
    coefficient: 0.01,
    unit: 'kWh',
    status: 'enabled',
    description: '三相智能电表有功电能采集规则',
    createTime: '2024-01-15 09:30:00'
  },
  {
    id: '2',
    ruleName: '超声波水表流量',
    dataType: 'water',
    protocol: 'CJ/T188',
    deviceModel: 'WM-100',
    registerAddress: '0x901F',
    dataFormat: 'bcd',
    coefficient: 0.001,
    unit: 'm3',
    status: 'enabled',
    description: '超声波水表累计流量采集规则',
    createTime: '2024-01-10 14:20:00'
  },
  {
    id: '3',
    ruleName: '燃气表总量',
    dataType: 'gas',
    protocol: 'DL/T645',
    deviceModel: 'GAS-200',
    registerAddress: '0x0010',
    dataFormat: 'float32',
    coefficient: 1,
    unit: 'm3',
    status: 'disabled',
    description: '智能燃气表总用气量采集规则',
    createTime: '2024-01-08 16:45:00'
  }
])

const formRef = ref()

// 获取数据类型颜色
const getDataTypeColor = (type: string) => {
  const colors = {
    electricity: 'primary',
    water: 'success',
    gas: 'warning',
    steam: 'danger',
    cooling: 'info',
    heating: ''
  }
  return colors[type] || 'info'
}

// 获取数据类型文本
const getDataTypeText = (type: string) => {
  const texts = {
    electricity: '电量',
    water: '水量',
    gas: '燃气',
    steam: '蒸汽',
    cooling: '制冷',
    heating: '采暖'
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
    ruleName: '',
    dataType: '',
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

const handleCreate = () => {
  dialogMode.value = 'add'
  dialogTitle.value = '新增采集规则'
  activeTab.value = 'basic'
  resetFormData()
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑采集规则'
  activeTab.value = 'basic'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleView = (row: any) => {
  currentRule.value = row
  detailDialogVisible.value = true
}

const handleCopy = (row: any) => {
  dialogMode.value = 'add'
  dialogTitle.value = '复制采集规则'
  activeTab.value = 'basic'
  Object.assign(formData, { 
    ...row, 
    id: '',
    ruleName: `${row.ruleName}_副本`
  })
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该采集规则吗？删除后无法恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const index = ruleList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      ruleList.value.splice(index, 1)
    }
    
    ElMessage.success('删除成功')
  } catch {
    // 用户取消删除
  }
}

const handleTestRule = async () => {
  testing.value = true
  
  try {
    // 模拟测试过程
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // 模拟测试结果
    const isSuccess = Math.random() > 0.3
    testResult.value = {
      success: isSuccess,
      data: isSuccess ? (Math.random() * 1000).toFixed(2) : null,
      responseTime: Math.floor(Math.random() * 100) + 50,
      error: isSuccess ? null : '设备连接超时'
    }
    
    ElMessage[isSuccess ? 'success' : 'error'](
      isSuccess ? '连接测试成功' : '连接测试失败'
    )
  } finally {
    testing.value = false
  }
}

const clearTestResult = () => {
  testResult.value = null
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (dialogMode.value === 'add') {
      const newRule = {
        ...formData,
        id: `rule_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        createTime: new Date().toLocaleString('zh-CN')
      }
      ruleList.value.unshift(newRule)
      ElMessage.success('创建成功')
    } else if (dialogMode.value === 'edit') {
      const index = ruleList.value.findIndex(item => item.id === formData.id)
      if (index > -1) {
        Object.assign(ruleList.value[index], { ...formData })
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
    ruleName: '',
    dataType: '',
    protocol: '',
    deviceModel: '',
    registerAddress: '',
    dataFormat: '',
    coefficient: 1,
    unit: '',
    status: 'enabled',
    description: '',
    validations: [],
    errorHandling: 'retry',
    minValue: 0,
    maxValue: 9999,
    retryCount: 3,
    retryInterval: 5
  })
  testResult.value = null
}

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    pagination.total = ruleList.value.length
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

.test-panel {
  .test-result {
    margin-top: 20px;
    padding: 16px;
    background: #f8f9fa;
    border-radius: 6px;
    border: 1px solid #ebeef5;

    h4 {
      margin: 0 0 12px 0;
      color: #303133;
      font-size: 16px;
    }

    .result-item {
      display: flex;
      align-items: center;
      margin-bottom: 8px;

      .label {
        min-width: 80px;
        color: #606266;
        font-weight: 500;
      }

      .value {
        color: #303133;
        font-family: 'Courier New', monospace;
      }

      .error {
        color: #f56c6c;
      }
    }
  }
}
</style>






