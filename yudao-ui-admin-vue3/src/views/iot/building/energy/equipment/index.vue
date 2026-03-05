<template>
  <div class="energy-equipment-page dark-theme-page">
    <!-- 搜索栏 -->
    <div class="filter-section">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="设备类型">
          <el-select v-model="queryParams.meterType" placeholder="全部类型" clearable class="!w-120px">
            <el-option label="全部类型" :value="undefined" />
            <el-option label="电表" :value="1" />
            <el-option label="水表" :value="2" />
            <el-option label="燃气表" :value="3" />
            <el-option label="冷量表" :value="4" />
            <el-option label="热量表" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable class="!w-100px">
            <el-option label="全部状态" :value="undefined" />
            <el-option label="在线" :value="1" />
            <el-option label="离线" :value="0" />
            <el-option label="故障" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="搜索">
          <el-input
            v-model="queryParams.meterName"
            placeholder="设备编号/名称"
            clearable
            @keyup.enter="handleQuery"
            class="!w-180px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
        </el-form-item>
        <el-form-item class="flex-grow text-right">
          <el-button type="primary" @click="openForm('create')">
            <Icon icon="ep:plus" class="mr-5px" /> 新增设备
          </el-button>
          <el-button type="success" @click="handleExport">
            <Icon icon="ep:download" class="mr-5px" /> 导出台账
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 设备列表 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="list"
        :row-class-name="getRowClassName"
        stripe
        border
        class="dark-table"
      >
        <el-table-column label="设备编号" prop="meterCode" width="140" fixed="left" />
        <el-table-column label="设备名称" prop="meterName" min-width="180" />
        <el-table-column label="设备类型" prop="meterType" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getMeterTypeTag(row.meterType)" size="small">
              {{ getMeterTypeName(row.meterType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="品牌" prop="brand" width="100" />
        <el-table-column label="型号" prop="model" width="120" />
        <el-table-column label="所属区域" prop="areaName" width="120" />
        <el-table-column label="安装位置" prop="installLocation" width="160" show-overflow-tooltip />
        <el-table-column label="倍率" prop="ratio" width="80" align="center">
          <template #default="{ row }">{{ row.ratio || 1 }}</template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <span class="status-badge" :class="getStatusClass(row.status)">
              <span class="status-dot"></span>
              {{ getStatusLabel(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openForm('update', row)">
              <Icon icon="ep:edit" /> 编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              <Icon icon="ep:delete" /> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-section">
        <span class="total-text">共 {{ total }} 台设备</span>
        <el-pagination
          v-model:current-page="queryParams.pageNo"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="sizes, prev, pager, next, jumper"
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      class="dark-dialog"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        class="dark-form"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="设备编号" prop="meterCode">
              <el-input v-model="formData.meterCode" placeholder="请输入设备编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备名称" prop="meterName">
              <el-input v-model="formData.meterName" placeholder="请输入设备名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="设备类型" prop="meterType">
              <el-select v-model="formData.meterType" placeholder="请选择设备类型" class="w-full">
                <el-option label="电表" :value="1" />
                <el-option label="水表" :value="2" />
                <el-option label="燃气表" :value="3" />
                <el-option label="冷量表" :value="4" />
                <el-option label="热量表" :value="5" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="formData.status" placeholder="请选择状态" class="w-full">
                <el-option label="在线" :value="1" />
                <el-option label="离线" :value="0" />
                <el-option label="故障" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-input v-model="formData.brand" placeholder="请输入品牌" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="型号" prop="model">
              <el-input v-model="formData.model" placeholder="请输入型号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属区域" prop="areaName">
              <el-input v-model="formData.areaName" placeholder="请输入所属区域" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="倍率" prop="ratio">
              <el-input-number v-model="formData.ratio" :min="1" :max="1000" :precision="0" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="安装位置" prop="installLocation">
          <el-input v-model="formData.installLocation" placeholder="请输入安装位置" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import * as EnergyApi from '@/api/iot/building/energy'
import type { IbmsEnergyMeterVO, IbmsEnergyMeterSaveReqVO } from '@/api/iot/building/energy'
import { useMessage } from '@/hooks/web/useMessage'

defineOptions({ name: 'BuildingEnergyEquipment' })

const message = useMessage()

// 数据
const loading = ref(false)
const list = ref<IbmsEnergyMeterVO[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  meterName: undefined as string | undefined,
  meterType: undefined as number | undefined,
  status: undefined as number | undefined
})

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const submitLoading = ref(false)
const formData = reactive<IbmsEnergyMeterSaveReqVO>({
  id: undefined,
  meterCode: '',
  meterName: '',
  meterType: undefined,
  brand: '',
  model: '',
  areaName: '',
  installLocation: '',
  ratio: 1,
  status: 1
})

const formRules = {
  meterCode: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
  meterName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  meterType: [{ required: true, message: '请选择设备类型', trigger: 'change' }]
}

// 工具函数
const getMeterTypeName = (type?: number) => {
  const map: Record<number, string> = { 1: '电表', 2: '水表', 3: '燃气表', 4: '冷量表', 5: '热量表' }
  return map[type || 0] || '未知'
}

const getMeterTypeTag = (type?: number) => {
  const map: Record<number, string> = { 1: 'warning', 2: 'success', 3: '', 4: 'info', 5: 'danger' }
  return map[type || 0] || 'info'
}

const getStatusLabel = (status?: number) => {
  const map: Record<number, string> = { 0: '离线', 1: '在线', 2: '故障' }
  return map[status ?? 0] || '未知'
}

const getStatusClass = (status?: number) => {
  const map: Record<number, string> = { 0: 'offline', 1: 'online', 2: 'fault' }
  return map[status ?? 0] || ''
}

const getRowClassName = ({ row }: { row: IbmsEnergyMeterVO }) => {
  if (row.status === 0) return 'row-offline'
  if (row.status === 2) return 'row-fault'
  return ''
}

// 查询
const getList = async () => {
  loading.value = true
  try {
    const res = await EnergyApi.getMeterPage(queryParams)
    list.value = res.list
    total.value = res.total
  } catch (e) {
    console.error('加载设备列表失败', e)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.meterName = undefined
  queryParams.meterType = undefined
  queryParams.status = undefined
  handleQuery()
}

// 打开表单
const openForm = (type: string, row?: IbmsEnergyMeterVO) => {
  dialogTitle.value = type === 'create' ? '新增设备' : '编辑设备'
  
  if (row) {
    Object.assign(formData, row)
  } else {
    Object.assign(formData, {
      id: undefined,
      meterCode: '',
      meterName: '',
      meterType: undefined,
      brand: '',
      model: '',
      areaName: '',
      installLocation: '',
      ratio: 1,
      status: 1
    })
  }
  
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  await formRef.value?.validate()
  
  submitLoading.value = true
  try {
    if (formData.id) {
      await EnergyApi.updateMeter(formData)
      message.success('更新成功')
    } else {
      await EnergyApi.createMeter(formData)
      message.success('创建成功')
    }
    dialogVisible.value = false
    getList()
  } catch (e) {
    console.error('保存失败', e)
    message.error('保存失败，请重试')
  } finally {
    submitLoading.value = false
  }
}

// 删除
const handleDelete = async (row: IbmsEnergyMeterVO) => {
  try {
    await message.confirm(`确认删除设备【${row.meterName}】吗？`)
    await EnergyApi.deleteMeter(row.id!)
    message.success('删除成功')
    getList()
  } catch (e) {
    console.error('删除失败', e)
  }
}

// 导出
const handleExport = () => {
  message.success('导出功能开发中...')
}

// 初始化
onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
@use '@/styles/dark-theme.scss' as *;

.energy-equipment-page {
  padding: 20px;
  padding-top: calc(
    20px + max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 20px)))
  );
  height: 100%;
  box-sizing: border-box;
  overflow: auto;
  background: #1a1a2e;
}

.filter-section {
  background: #252532;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 16px;
  border: 1px solid #3a3a4a;
  
  .filter-form {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    
    :deep(.el-form-item) {
      margin-bottom: 0;
      margin-right: 16px;
      
      .el-form-item__label {
        color: #a0a0b0;
      }
    }
    
    .flex-grow {
      flex: 1;
      display: flex;
      justify-content: flex-end;
    }
  }
}

.table-section {
  background: #252532;
  border-radius: 8px;
  border: 1px solid #3a3a4a;
  overflow: hidden;
  
  .dark-table {
    --el-table-bg-color: #252532;
    --el-table-header-bg-color: #1a1a2e;
    --el-table-tr-bg-color: #252532;
    --el-table-row-hover-bg-color: #2d2d3a;
    --el-table-border-color: #3a3a4a;
    --el-table-text-color: #ffffff;
    --el-table-header-text-color: #a0a0b0;
    
    :deep(.el-table__row) {
      &.row-offline {
        background: rgba(107, 114, 128, 0.1) !important;
        td { opacity: 0.7; }
      }
      
      &.row-fault {
        background: rgba(239, 68, 68, 0.1) !important;
      }
    }
  }
  
  .status-badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 4px 10px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 600;
    
    .status-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
    }
    
    &.online {
      background: rgba(16, 185, 129, 0.2);
      color: #10b981;
      .status-dot { background: #10b981; }
    }
    
    &.offline {
      background: rgba(107, 114, 128, 0.2);
      color: #9ca3af;
      .status-dot { background: #9ca3af; }
    }
    
    &.fault {
      background: rgba(239, 68, 68, 0.2);
      color: #f87171;
      .status-dot { background: #f87171; }
    }
  }
}

.pagination-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid #3a3a4a;
  background: #1a1a2e;
  
  .total-text {
    color: #a0a0b0;
    font-size: 13px;
  }
  
  :deep(.el-pagination) {
    --el-pagination-bg-color: transparent;
    --el-pagination-text-color: #a0a0b0;
    
    .el-pager li {
      background: #2d2d3a;
      border: 1px solid #3a3a4a;
      color: #ffffff;
      
      &.is-active {
        background: #10b981;
        border-color: #10b981;
      }
    }
  }
}

// 深色表单控件
:deep(.el-select), :deep(.el-input), :deep(.el-input-number) {
  .el-select__wrapper, .el-input__wrapper {
    background: #1a1a2e !important;
    border-color: #3a3a4a !important;
    box-shadow: none !important;
    
    .el-input__inner {
      color: #ffffff !important;
      
      &::placeholder {
        color: #6b7280 !important;
      }
    }
  }
}

// 深色弹窗
:deep(.dark-dialog) {
  .el-dialog {
    background: #252532 !important;
    border: 1px solid #3a3a4a;
    
    .el-dialog__header {
      background: #1a1a2e !important;
      border-bottom: 1px solid #3a3a4a;
      
      .el-dialog__title {
        color: #ffffff !important;
      }
    }
    
    .el-dialog__body {
      background: #252532 !important;
    }
    
    .el-dialog__footer {
      background: #1a1a2e !important;
      border-top: 1px solid #3a3a4a;
    }
  }
}

.dark-form {
  :deep(.el-form-item__label) {
    color: #a0a0b0 !important;
  }
}

.w-full {
  width: 100%;
}
</style>
