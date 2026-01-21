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
      <el-form-item label="道闸名称" prop="gateName">
        <el-input v-model="queryParams.gateName" placeholder="请输入道闸名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="所属车场" prop="lotId">
        <el-select v-model="queryParams.lotId" placeholder="请选择车场" clearable>
          <el-option v-for="item in lotList" :key="item.id" :label="item.lotName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="方向" prop="direction">
        <el-select v-model="queryParams.direction" placeholder="请选择方向" clearable>
          <el-option v-for="item in DirectionOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="在线状态" prop="onlineStatus">
        <el-select v-model="queryParams.onlineStatus" placeholder="请选择在线状态" clearable>
          <el-option label="在线" :value="1" />
          <el-option label="离线" :value="0" />
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
      <el-table-column label="道闸名称" prop="gateName" width="180" />
      <el-table-column label="道闸编码" prop="gateCode" width="120" />
      <el-table-column label="所属车场" prop="lotName" width="150" />
      <el-table-column label="IP地址" prop="ipAddress" width="130" />
      <el-table-column label="端口" prop="port" width="80" />
      <el-table-column label="厂商" prop="manufacturer" width="100" />
      <el-table-column label="道闸类型" width="120">
        <template #default="{ row }">
          {{ row.gateType === 1 ? '车牌识别一体机' : '普通道闸' }}
        </template>
      </el-table-column>
      <el-table-column label="方向" width="80">
        <template #default="{ row }">
          <el-tag :type="row.direction === 1 ? 'success' : row.direction === 2 ? 'warning' : 'primary'">
            {{ row.direction === 1 ? '入口' : row.direction === 2 ? '出口' : '出入口' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="在线状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.onlineStatus === 1 ? 'success' : 'danger'">
            {{ row.onlineStatus === 1 ? '在线' : '离线' }}
          </el-tag>
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
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="道闸名称" prop="gateName">
        <el-input v-model="formData.gateName" placeholder="请输入道闸名称" />
      </el-form-item>
      <el-form-item label="道闸编码" prop="gateCode">
        <el-input v-model="formData.gateCode" placeholder="请输入道闸编码" />
      </el-form-item>
      <el-form-item label="所属车场" prop="lotId">
        <el-select v-model="formData.lotId" placeholder="请选择车场">
          <el-option v-for="item in lotList" :key="item.id" :label="item.lotName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="道闸类型" prop="gateType">
        <el-select v-model="formData.gateType" placeholder="请选择道闸类型">
          <el-option v-for="item in GateTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="方向" prop="direction">
        <el-select v-model="formData.direction" placeholder="请选择方向">
          <el-option v-for="item in DirectionOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="IP地址" prop="ipAddress">
        <el-input v-model="formData.ipAddress" placeholder="请输入IP地址" />
      </el-form-item>
      <el-form-item label="端口" prop="port">
        <el-input-number v-model="formData.port" :min="1" :max="65535" controls-position="right" />
      </el-form-item>
      <el-form-item label="厂商" prop="manufacturer">
        <el-input v-model="formData.manufacturer" placeholder="请输入厂商" />
      </el-form-item>
      <el-form-item label="型号" prop="model">
        <el-input v-model="formData.model" placeholder="请输入型号" />
      </el-form-item>
      <el-form-item label="用户名" prop="username">
        <el-input v-model="formData.username" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
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
import { ParkingGateApi, ParkingLotApi, DirectionOptions, GateTypeOptions } from '@/api/iot/parking'
import { ContentWrap } from '@/components/ContentWrap'
import { Dialog } from '@/components/Dialog'
import { Pagination } from '@/components/Pagination'

defineOptions({ name: 'ParkingGate' })

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const lotList = ref<any[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  gateName: undefined,
  lotId: undefined,
  direction: undefined,
  onlineStatus: undefined
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formRef = ref()
const formData = ref<any>({
  id: undefined,
  gateName: '',
  gateCode: '',
  lotId: undefined,
  gateType: 1,
  direction: 1,
  ipAddress: '',
  port: 80,
  manufacturer: '',
  model: '',
  username: '',
  password: '',
  status: 0,
  remark: ''
})

const formRules = {
  gateName: [{ required: true, message: '道闸名称不能为空', trigger: 'blur' }],
  lotId: [{ required: true, message: '请选择所属车场', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ParkingGateApi.getParkingGatePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const getLotList = async () => {
  lotList.value = await ParkingLotApi.getSimpleList()
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.gateName = undefined
  queryParams.lotId = undefined
  queryParams.direction = undefined
  queryParams.onlineStatus = undefined
  handleQuery()
}

const openForm = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增道闸' : '编辑道闸'
  formData.value = {
    id: undefined,
    gateName: '',
    gateCode: '',
    lotId: undefined,
    gateType: 1,
    direction: 1,
    ipAddress: '',
    port: 80,
    manufacturer: '',
    model: '',
    username: '',
    password: '',
    status: 0,
    remark: ''
  }
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ParkingGateApi.getParkingGate(id)
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
      await ParkingGateApi.updateParkingGate(formData.value)
      ElMessage.success('更新成功')
    } else {
      await ParkingGateApi.createParkingGate(formData.value)
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
    await ElMessageBox.confirm('确定要删除该道闸吗?', '提示', { type: 'warning' })
    await ParkingGateApi.deleteParkingGate(id)
    ElMessage.success('删除成功')
    getList()
  } catch {}
}

onMounted(() => {
  getLotList()
  getList()
})
</script>
