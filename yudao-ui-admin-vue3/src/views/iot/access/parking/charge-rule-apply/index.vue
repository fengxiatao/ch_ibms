<template>
  <ContentWrap
    :body-style="{
      padding: '10px',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      backgroundColor: 'var(--el-bg-color)'
    }"
    style="height: calc(100vh - var(--page-top-gap, 70px)); padding-top: var(--page-top-gap, 70px)"
  >
    <!-- 搜索工作栏 -->
    <el-form ref="queryFormRef" :model="queryParams" :inline="true" class="mb-15px">
      <el-form-item label="应用名称" prop="applyName">
        <el-input v-model="queryParams.applyName" placeholder="请输入应用名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="适用车场" prop="lotId">
        <el-select v-model="queryParams.lotId" placeholder="请选择车场" clearable>
          <el-option v-for="item in lotList" :key="item.id" :label="item.lotName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="正常" :value="0" />
          <el-option label="停用" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery"><Icon icon="ep:search" class="mr-5px" />搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" />重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb-10px">
      <el-col :span="1.5">
        <el-button type="primary" plain @click="openForm('create')">
          <Icon icon="ep:plus" class="mr-5px" />新增
        </el-button>
      </el-col>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="应用名称" prop="applyName" width="180" />
      <el-table-column label="适用车场" width="150">
        <template #default="{ row }">
          {{ formatLotIds(row.lotIds) }}
        </template>
      </el-table-column>
      <el-table-column label="车辆类型" width="150">
        <template #default="{ row }">
          {{ formatVehicleCategory(row.vehicleCategory) }}
        </template>
      </el-table-column>
      <el-table-column label="收费车型" width="150">
        <template #default="{ row }">
          {{ formatChargeTypes(row.chargeVehicleTypes) }}
        </template>
      </el-table-column>
      <el-table-column label="关联收费规则" prop="ruleName" width="180" />
      <el-table-column label="优先级" prop="priority" width="80" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'">
            {{ row.status === 0 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="170">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="openForm('update', row.id)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="650px">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="120px">
      <el-form-item label="应用名称" prop="applyName">
        <el-input v-model="formData.applyName" placeholder="请输入应用名称" />
      </el-form-item>
      <el-form-item label="适用车场" prop="lotIds">
        <el-select v-model="selectedLotIds" multiple placeholder="请选择适用车场" style="width: 100%">
          <el-option v-for="item in lotList" :key="item.id" :label="item.lotName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="车辆类型" prop="vehicleCategory">
        <el-select v-model="formData.vehicleCategory" placeholder="请选择车辆类型" clearable>
          <el-option v-for="item in VehicleCategoryOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="收费车型" prop="chargeVehicleTypes">
        <el-checkbox-group v-model="selectedChargeTypes">
          <el-checkbox v-for="item in VehicleTypeOptions" :key="item.value" :label="item.value">{{ item.label }}</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item label="关联收费规则" prop="ruleId">
        <el-select v-model="formData.ruleId" placeholder="请选择收费规则">
          <el-option v-for="item in chargeRuleList" :key="item.id" :label="item.ruleName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="优先级" prop="priority">
        <el-input-number v-model="formData.priority" :min="0" :max="100" controls-position="right" />
        <span class="ml-10px text-gray-400">(数值越大优先级越高)</span>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :label="0">正常</el-radio>
          <el-radio :label="1">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm" :loading="formLoading">确 定</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ParkingChargeRuleApplyApi, ParkingChargeRuleApi, ParkingLotApi, VehicleCategoryOptions, VehicleTypeOptions } from '@/api/iot/parking'
import { formatDate } from '@/utils/formatTime'
import { ContentWrap } from '@/components/ContentWrap'
import { Dialog } from '@/components/Dialog'
import { Pagination } from '@/components/Pagination'

defineOptions({ name: 'ParkingChargeRuleApply' })

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const lotList = ref<any[]>([])
const chargeRuleList = ref<any[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  applyName: undefined,
  lotId: undefined,
  status: undefined
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formRef = ref()
const formData = ref<any>({
  id: undefined,
  applyName: '',
  lotIds: '',
  vehicleCategory: undefined,
  chargeVehicleTypes: '',
  ruleId: undefined,
  priority: 10,
  status: 0,
  remark: ''
})

// 多选值
const selectedLotIds = ref<number[]>([])
const selectedChargeTypes = ref<number[]>([])

// 监听多选值变化，转换为JSON字符串
watch(selectedLotIds, (val) => { formData.value.lotIds = JSON.stringify(val) })
watch(selectedChargeTypes, (val) => { formData.value.chargeVehicleTypes = JSON.stringify(val) })

const formRules = {
  applyName: [{ required: true, message: '应用名称不能为空', trigger: 'blur' }],
  ruleId: [{ required: true, message: '请选择关联收费规则', trigger: 'change' }]
}

const formatLotIds = (lotIds: string) => {
  if (!lotIds) return '-'
  try {
    const ids = JSON.parse(lotIds)
    return lotList.value.filter(l => ids.includes(l.id)).map(l => l.lotName).join(', ') || '-'
  } catch {
    return '-'
  }
}

const formatVehicleCategory = (category: string) => {
  if (!category) return '-'
  const item = VehicleCategoryOptions.find(o => o.value === category)
  return item ? item.label : category
}

const formatChargeTypes = (types: string) => {
  if (!types) return '-'
  try {
    const arr = JSON.parse(types)
    return arr.map((t: number) => {
      const item = VehicleTypeOptions.find(o => o.value === t)
      return item ? item.label : t
    }).join(', ') || '-'
  } catch {
    return '-'
  }
}

const getLotList = async () => {
  lotList.value = await ParkingLotApi.getSimpleList()
}

const getChargeRuleList = async () => {
  chargeRuleList.value = await ParkingChargeRuleApi.getSimpleList()
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ParkingChargeRuleApplyApi.getChargeRuleApplyPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.applyName = undefined
  queryParams.lotId = undefined
  queryParams.status = undefined
  handleQuery()
}

const parseJsonArray = (str: string): any[] => {
  if (!str) return []
  try {
    return JSON.parse(str)
  } catch {
    return []
  }
}

const openForm = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增收费规则应用' : '编辑收费规则应用'
  // 重置表单
  formData.value = {
    id: undefined,
    applyName: '',
    lotIds: '',
    vehicleCategory: undefined,
    chargeVehicleTypes: '',
    ruleId: undefined,
    priority: 10,
    status: 0,
    remark: ''
  }
  selectedLotIds.value = []
  selectedChargeTypes.value = []
  
  if (id) {
    formLoading.value = true
    try {
      const data = await ParkingChargeRuleApplyApi.getChargeRuleApply(id)
      formData.value = data
      // 解析JSON字段
      selectedLotIds.value = parseJsonArray(data.lotIds)
      selectedChargeTypes.value = parseJsonArray(data.chargeVehicleTypes)
    } finally {
      formLoading.value = false
    }
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return
  
  formLoading.value = true
  try {
    if (formData.value.id) {
      await ParkingChargeRuleApplyApi.updateChargeRuleApply(formData.value)
      ElMessage.success('更新成功')
    } else {
      await ParkingChargeRuleApplyApi.createChargeRuleApply(formData.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } finally {
    formLoading.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该收费规则应用吗?', '提示', { type: 'warning' })
    await ParkingChargeRuleApplyApi.deleteChargeRuleApply(id)
    ElMessage.success('删除成功')
    getList()
  } catch {}
}

onMounted(() => {
  getLotList()
  getChargeRuleList()
  getList()
})
</script>
