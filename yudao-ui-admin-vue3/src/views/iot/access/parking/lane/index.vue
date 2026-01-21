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
      <el-form-item label="车道名称" prop="laneName">
        <el-input v-model="queryParams.laneName" placeholder="请输入车道名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="所属车场" prop="lotId">
        <el-select v-model="queryParams.lotId" placeholder="请选择车场" clearable>
          <el-option v-for="item in lotList" :key="item.id" :label="item.lotName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="出入口类型" prop="direction">
        <el-select v-model="queryParams.direction" placeholder="请选择类型" clearable>
          <el-option v-for="item in DirectionOptions" :key="item.value" :label="item.label" :value="item.value" />
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
      <el-table-column label="车道名称" prop="laneName" width="150" />
      <el-table-column label="车道编码" prop="laneCode" width="120" />
      <el-table-column label="所属车场" prop="lotName" width="150" />
      <el-table-column label="出入口配置" width="100">
        <template #default="{ row }">
          <el-tag :type="row.direction === 1 ? 'success' : row.direction === 2 ? 'warning' : 'primary'">
            {{ row.direction === 1 ? '入口' : row.direction === 2 ? '出口' : '出入口' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="主相机" prop="mainCameraId" width="100">
        <template #default="{ row }">{{ row.mainCameraId || '-' }}</template>
      </el-table-column>
      <el-table-column label="主显屏幕" prop="mainScreenId" width="100">
        <template #default="{ row }">{{ row.mainScreenId || '-' }}</template>
      </el-table-column>
      <el-table-column label="辅助相机" prop="auxCameraId" width="100">
        <template #default="{ row }">{{ row.auxCameraId || '-' }}</template>
      </el-table-column>
      <el-table-column label="辅显屏幕" prop="auxScreenId" width="100">
        <template #default="{ row }">{{ row.auxScreenId || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'">
            {{ row.status === 0 ? '正常' : '停用' }}
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
      <el-form-item label="车道名称" prop="laneName">
        <el-input v-model="formData.laneName" placeholder="请输入车道名称" />
      </el-form-item>
      <el-form-item label="车道编码" prop="laneCode">
        <el-input v-model="formData.laneCode" placeholder="请输入车道编码" />
      </el-form-item>
      <el-form-item label="所属车场" prop="lotId">
        <el-select v-model="formData.lotId" placeholder="请选择车场">
          <el-option v-for="item in lotList" :key="item.id" :label="item.lotName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="出入口配置" prop="direction">
        <el-select v-model="formData.direction" placeholder="请选择出入口类型">
          <el-option v-for="item in DirectionOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="主相机ID" prop="mainCameraId">
        <el-input-number v-model="formData.mainCameraId" :min="0" controls-position="right" placeholder="主相机设备ID" />
      </el-form-item>
      <el-form-item label="主显屏幕ID" prop="mainScreenId">
        <el-input-number v-model="formData.mainScreenId" :min="0" controls-position="right" placeholder="主显屏幕设备ID" />
      </el-form-item>
      <el-form-item label="辅助相机ID" prop="auxCameraId">
        <el-input-number v-model="formData.auxCameraId" :min="0" controls-position="right" placeholder="辅助相机设备ID" />
      </el-form-item>
      <el-form-item label="辅显屏幕ID" prop="auxScreenId">
        <el-input-number v-model="formData.auxScreenId" :min="0" controls-position="right" placeholder="辅显屏幕设备ID" />
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
import { ParkingLaneApi, ParkingLotApi, DirectionOptions } from '@/api/iot/parking'
import { ContentWrap } from '@/components/ContentWrap'
import { Dialog } from '@/components/Dialog'
import { Pagination } from '@/components/Pagination'

defineOptions({ name: 'ParkingLane' })

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const lotList = ref<any[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  laneName: undefined,
  lotId: undefined,
  direction: undefined
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formRef = ref()
const formData = ref<any>({
  id: undefined,
  laneName: '',
  laneCode: '',
  lotId: undefined,
  direction: 1,
  mainCameraId: undefined,
  mainScreenId: undefined,
  auxCameraId: undefined,
  auxScreenId: undefined,
  status: 0,
  remark: ''
})

const formRules = {
  laneName: [{ required: true, message: '车道名称不能为空', trigger: 'blur' }],
  lotId: [{ required: true, message: '请选择所属车场', trigger: 'change' }]
}

const getLotList = async () => {
  lotList.value = await ParkingLotApi.getSimpleList()
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ParkingLaneApi.getParkingLanePage(queryParams)
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
  queryParams.laneName = undefined
  queryParams.lotId = undefined
  queryParams.direction = undefined
  handleQuery()
}

const openForm = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增车道' : '编辑车道'
  formData.value = {
    id: undefined,
    laneName: '',
    laneCode: '',
    lotId: undefined,
    direction: 1,
    mainCameraId: undefined,
    mainScreenId: undefined,
    auxCameraId: undefined,
    auxScreenId: undefined,
    status: 0,
    remark: ''
  }
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ParkingLaneApi.getParkingLane(id)
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
      await ParkingLaneApi.updateParkingLane(formData.value)
      ElMessage.success('更新成功')
    } else {
      await ParkingLaneApi.createParkingLane(formData.value)
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
    await ElMessageBox.confirm('确定要删除该车道吗?', '提示', { type: 'warning' })
    await ParkingLaneApi.deleteParkingLane(id)
    ElMessage.success('删除成功')
    getList()
  } catch {}
}

onMounted(() => {
  getLotList()
  getList()
})
</script>
