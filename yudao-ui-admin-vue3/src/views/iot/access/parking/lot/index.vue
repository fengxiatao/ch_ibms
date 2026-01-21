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
      <el-form-item label="车场名称" prop="lotName">
        <el-input v-model="queryParams.lotName" placeholder="请输入车场名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="车场类型" prop="lotType">
        <el-select v-model="queryParams.lotType" placeholder="请选择车场类型" clearable>
          <el-option v-for="item in LotTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
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
      <el-table-column label="车场名称" prop="lotName" width="200" />
      <el-table-column label="车场编码" prop="lotCode" width="150" />
      <el-table-column label="车场类型" prop="lotType" width="100">
        <template #default="{ row }">
          <el-tag :type="row.lotType === 1 ? 'warning' : 'success'">
            {{ row.lotType === 1 ? '收费' : '免费' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="总车位数" prop="totalSpaces" width="100" />
      <el-table-column label="月租费用" prop="monthlyFee" width="100">
        <template #default="{ row }">
          {{ row.monthlyFee ? `¥${row.monthlyFee}` : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="联系人" prop="contactPerson" width="100" />
      <el-table-column label="联系电话" prop="contactPhone" width="130" />
      <el-table-column label="状态" prop="status" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'">
            {{ row.status === 0 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="180" :formatter="dateFormatter" />
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
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="车场名称" prop="lotName">
        <el-input v-model="formData.lotName" placeholder="请输入车场名称" />
      </el-form-item>
      <el-form-item label="车场编码" prop="lotCode">
        <el-input v-model="formData.lotCode" placeholder="请输入车场编码" />
      </el-form-item>
      <el-form-item label="车场类型" prop="lotType">
        <el-select v-model="formData.lotType" placeholder="请选择车场类型">
          <el-option v-for="item in LotTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="总车位数" prop="totalSpaces">
        <el-input-number v-model="formData.totalSpaces" :min="0" controls-position="right" />
      </el-form-item>
      <el-form-item label="月租费用" prop="monthlyFee">
        <el-input-number v-model="formData.monthlyFee" :min="0" :precision="2" controls-position="right" />
      </el-form-item>
      <el-form-item label="免费出场时间" prop="freeExitMinutes">
        <el-input-number v-model="formData.freeExitMinutes" :min="0" controls-position="right" />
        <span class="ml-10px">分钟</span>
      </el-form-item>
      <el-form-item label="地址" prop="address">
        <el-input v-model="formData.address" placeholder="请输入地址" />
      </el-form-item>
      <el-form-item label="联系人" prop="contactPerson">
        <el-input v-model="formData.contactPerson" placeholder="请输入联系人" />
      </el-form-item>
      <el-form-item label="联系电话" prop="contactPhone">
        <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dateFormatter } from '@/utils/formatTime'
import { ParkingLotApi, LotTypeOptions } from '@/api/iot/parking'
import { ContentWrap } from '@/components/ContentWrap'
import { Dialog } from '@/components/Dialog'
import { Pagination } from '@/components/Pagination'

defineOptions({ name: 'ParkingLot' })

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  lotName: undefined,
  lotType: undefined,
  status: undefined
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formRef = ref()
const formData = ref<any>({
  id: undefined,
  lotName: '',
  lotCode: '',
  lotType: 1,
  totalSpaces: 0,
  monthlyFee: 0,
  freeExitMinutes: 15,
  address: '',
  contactPerson: '',
  contactPhone: '',
  status: 0,
  remark: ''
})

const formRules = {
  lotName: [{ required: true, message: '车场名称不能为空', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ParkingLotApi.getParkingLotPage(queryParams)
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
  queryParams.lotName = undefined
  queryParams.lotType = undefined
  queryParams.status = undefined
  handleQuery()
}

const openForm = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增车场' : '编辑车场'
  formData.value = {
    id: undefined,
    lotName: '',
    lotCode: '',
    lotType: 1,
    totalSpaces: 0,
    monthlyFee: 0,
    freeExitMinutes: 15,
    address: '',
    contactPerson: '',
    contactPhone: '',
    status: 0,
    remark: ''
  }
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ParkingLotApi.getParkingLot(id)
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
      await ParkingLotApi.updateParkingLot(formData.value)
      ElMessage.success('更新成功')
    } else {
      await ParkingLotApi.createParkingLot(formData.value)
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
    await ElMessageBox.confirm('确定要删除该车场吗?', '提示', { type: 'warning' })
    await ParkingLotApi.deleteParkingLot(id)
    ElMessage.success('删除成功')
    getList()
  } catch {}
}

onMounted(() => {
  getList()
})
</script>
