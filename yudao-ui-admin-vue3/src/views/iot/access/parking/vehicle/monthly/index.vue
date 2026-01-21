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
      <el-form-item label="车牌号" prop="plateNumber">
        <el-input v-model="queryParams.plateNumber" placeholder="请输入车牌号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="车主姓名" prop="ownerName">
        <el-input v-model="queryParams.ownerName" placeholder="请输入车主姓名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="所属车场" prop="lotId">
        <el-select v-model="queryParams.lotId" placeholder="请选择车场" clearable>
          <el-option v-for="item in lotList" :key="item.id" :label="item.lotName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="正常" :value="0" />
          <el-option label="停用" :value="1" />
          <el-option label="过期" :value="2" />
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
      <el-table-column label="车牌号" prop="plateNumber" width="120" />
      <el-table-column label="车主姓名" prop="ownerName" width="100" />
      <el-table-column label="车主电话" prop="ownerPhone" width="130" />
      <el-table-column label="所属车场" prop="lotName" width="150" />
      <el-table-column label="月租费" prop="monthlyFee" width="100">
        <template #default="{ row }">
          {{ row.monthlyFee ? `¥${row.monthlyFee}` : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="有效期开始" prop="validStart" width="160" :formatter="dateFormatter" />
      <el-table-column label="有效期结束" prop="validEnd" width="160" :formatter="dateFormatter" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : row.status === 1 ? 'danger' : 'warning'">
            {{ row.status === 0 ? '正常' : row.status === 1 ? '停用' : '过期' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="openForm('update', row.id)">编辑</el-button>
          <el-button link type="success" @click="openRechargeForm(row)">续费</el-button>
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
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="车牌号" prop="plateNumber">
        <el-input v-model="formData.plateNumber" placeholder="请输入车牌号" />
      </el-form-item>
      <el-form-item label="车主姓名" prop="ownerName">
        <el-input v-model="formData.ownerName" placeholder="请输入车主姓名" />
      </el-form-item>
      <el-form-item label="车主电话" prop="ownerPhone">
        <el-input v-model="formData.ownerPhone" placeholder="请输入车主电话" />
      </el-form-item>
      <el-form-item label="车辆类型" prop="vehicleType">
        <el-select v-model="formData.vehicleType" placeholder="请选择车辆类型">
          <el-option v-for="item in VehicleTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="所属车场" prop="lotId">
        <el-select v-model="formData.lotId" placeholder="请选择车场">
          <el-option v-for="item in lotList" :key="item.id" :label="item.lotName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="月租费" prop="monthlyFee">
        <el-input-number v-model="formData.monthlyFee" :min="0" :precision="2" controls-position="right" />
      </el-form-item>
      <el-form-item label="有效期" prop="validTime">
        <el-date-picker
          v-model="validTimeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
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

  <!-- 续费弹窗 -->
  <Dialog v-model="rechargeDialogVisible" title="月卡续费" width="400px">
    <el-form ref="rechargeFormRef" v-loading="rechargeLoading" :model="rechargeData" :rules="rechargeRules" label-width="80px">
      <el-form-item label="车牌号">
        <el-input :value="currentVehicle?.plateNumber" disabled />
      </el-form-item>
      <el-form-item label="续费月数" prop="months">
        <el-input-number v-model="rechargeData.months" :min="1" :max="36" controls-position="right" />
      </el-form-item>
      <el-form-item label="应收金额">
        <span>¥{{ (currentVehicle?.monthlyFee || 0) * (rechargeData.months || 0) }}</span>
      </el-form-item>
      <el-form-item label="实收金额" prop="paidAmount">
        <el-input-number v-model="rechargeData.paidAmount" :min="0" :precision="2" controls-position="right" />
      </el-form-item>
      <el-form-item label="支付方式" prop="paymentMethod">
        <el-select v-model="rechargeData.paymentMethod" placeholder="请选择支付方式">
          <el-option v-for="item in PaymentMethodOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="rechargeDialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitRecharge" :loading="rechargeLoading">确 定</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dateFormatter } from '@/utils/formatTime'
import { ParkingMonthlyVehicleApi, ParkingLotApi, VehicleTypeOptions, PaymentMethodOptions } from '@/api/iot/parking'
import { ContentWrap } from '@/components/ContentWrap'
import { Dialog } from '@/components/Dialog'
import { Pagination } from '@/components/Pagination'

defineOptions({ name: 'ParkingMonthlyVehicle' })

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const lotList = ref<any[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  plateNumber: undefined,
  ownerName: undefined,
  lotId: undefined,
  status: undefined
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formRef = ref()
const formData = ref<any>({
  id: undefined,
  plateNumber: '',
  ownerName: '',
  ownerPhone: '',
  vehicleType: 1,
  lotId: undefined,
  monthlyFee: 0,
  validStart: undefined,
  validEnd: undefined,
  status: 0,
  remark: ''
})

const validTimeRange = computed({
  get: () => {
    if (formData.value.validStart && formData.value.validEnd) {
      return [formData.value.validStart, formData.value.validEnd]
    }
    return []
  },
  set: (val: any[]) => {
    if (val && val.length === 2) {
      formData.value.validStart = val[0]
      formData.value.validEnd = val[1]
    } else {
      formData.value.validStart = undefined
      formData.value.validEnd = undefined
    }
  }
})

const formRules = {
  plateNumber: [{ required: true, message: '车牌号不能为空', trigger: 'blur' }]
}

// 续费相关
const rechargeDialogVisible = ref(false)
const rechargeLoading = ref(false)
const rechargeFormRef = ref()
const currentVehicle = ref<any>(null)
const rechargeData = ref({
  months: 1,
  paidAmount: 0,
  paymentMethod: 'wechat'
})
const rechargeRules = {
  months: [{ required: true, message: '请输入续费月数', trigger: 'blur' }],
  paidAmount: [{ required: true, message: '请输入实收金额', trigger: 'blur' }],
  paymentMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}

const getLotList = async () => {
  lotList.value = await ParkingLotApi.getSimpleList()
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ParkingMonthlyVehicleApi.getMonthlyVehiclePage(queryParams)
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
  queryParams.plateNumber = undefined
  queryParams.ownerName = undefined
  queryParams.lotId = undefined
  queryParams.status = undefined
  handleQuery()
}

const openForm = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增月租车' : '编辑月租车'
  formData.value = {
    id: undefined,
    plateNumber: '',
    ownerName: '',
    ownerPhone: '',
    vehicleType: 1,
    lotId: undefined,
    monthlyFee: 0,
    validStart: undefined,
    validEnd: undefined,
    status: 0,
    remark: ''
  }
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ParkingMonthlyVehicleApi.getMonthlyVehicle(id)
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
      await ParkingMonthlyVehicleApi.updateMonthlyVehicle(formData.value)
      ElMessage.success('更新成功')
    } else {
      await ParkingMonthlyVehicleApi.createMonthlyVehicle(formData.value)
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
    await ElMessageBox.confirm('确定要删除该月租车吗?', '提示', { type: 'warning' })
    await ParkingMonthlyVehicleApi.deleteMonthlyVehicle(id)
    ElMessage.success('删除成功')
    getList()
  } catch {}
}

const openRechargeForm = (row: any) => {
  currentVehicle.value = row
  rechargeData.value = {
    months: 1,
    paidAmount: row.monthlyFee || 0,
    paymentMethod: 'wechat'
  }
  rechargeDialogVisible.value = true
}

const submitRecharge = async () => {
  if (!rechargeFormRef.value) return
  const valid = await rechargeFormRef.value.validate()
  if (!valid) return

  rechargeLoading.value = true
  try {
    await ParkingMonthlyVehicleApi.rechargeMonthlyVehicle(
      currentVehicle.value.id,
      rechargeData.value.months,
      rechargeData.value.paidAmount,
      rechargeData.value.paymentMethod
    )
    ElMessage.success('续费成功')
    rechargeDialogVisible.value = false
    getList()
  } finally {
    rechargeLoading.value = false
  }
}

onMounted(() => {
  getLotList()
  getList()
})
</script>
