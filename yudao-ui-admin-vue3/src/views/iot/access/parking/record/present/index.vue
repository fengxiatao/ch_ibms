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
      <el-form-item label="所属车场" prop="lotId">
        <el-select v-model="queryParams.lotId" placeholder="请选择车场" clearable>
          <el-option v-for="item in lotList" :key="item.id" :label="item.lotName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="车辆类别" prop="vehicleCategory">
        <el-select v-model="queryParams.vehicleCategory" placeholder="请选择车辆类别" clearable>
          <el-option v-for="item in VehicleCategoryOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="长期停车" prop="longTermFlag">
        <el-select v-model="queryParams.longTermFlag" placeholder="请选择" clearable>
          <el-option label="否" :value="0" />
          <el-option label="超一个月" :value="1" />
          <el-option label="超三个月" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery"><Icon icon="ep:search" class="mr-5px" />搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" />重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="车牌号" prop="plateNumber" width="120" />
      <el-table-column label="车辆类别" width="100">
        <template #default="{ row }">
          <el-tag :type="getCategoryType(row.vehicleCategory)">
            {{ getCategoryLabel(row.vehicleCategory) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="所属车场" prop="lotName" width="150" />
      <el-table-column label="入场时间" prop="entryTime" width="180" :formatter="dateFormatter" />
      <el-table-column label="停车时长" width="120">
        <template #default="{ row }">
          {{ formatDuration(row.parkingDuration) }}
        </template>
      </el-table-column>
      <el-table-column label="入场照片" width="100">
        <template #default="{ row }">
          <el-image v-if="row.entryPhotoUrl" :src="row.entryPhotoUrl" :preview-src-list="[row.entryPhotoUrl]" fit="cover" style="width: 60px; height: 40px;" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="长期停车" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.longTermFlag === 1" type="warning">超一个月</el-tag>
          <el-tag v-else-if="row.longTermFlag === 2" type="danger">超三个月</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="180">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleCalculateFee(row)">计算费用</el-button>
          <el-button link type="warning" @click="handleForceExit(row)">强制出场</el-button>
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

  <!-- 费用弹窗 -->
  <Dialog v-model="feeDialogVisible" title="停车费用" width="400px">
    <div class="fee-info">
      <p><strong>车牌号：</strong>{{ currentVehicle?.plateNumber }}</p>
      <p><strong>入场时间：</strong>{{ currentVehicle?.entryTime }}</p>
      <p><strong>停车时长：</strong>{{ formatDuration(currentVehicle?.parkingDuration) }}</p>
      <p class="fee-amount"><strong>应收费用：</strong><span class="amount">¥{{ parkingFee }}</span></p>
    </div>
    <template #footer>
      <el-button @click="feeDialogVisible = false">关闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dateFormatter } from '@/utils/formatTime'
import { ParkingRecordApi, ParkingLotApi, VehicleCategoryOptions } from '@/api/iot/parking'
import { ContentWrap } from '@/components/ContentWrap'
import { Dialog } from '@/components/Dialog'
import { Pagination } from '@/components/Pagination'

defineOptions({ name: 'ParkingPresentVehicle' })

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const lotList = ref<any[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  plateNumber: undefined,
  lotId: undefined,
  vehicleCategory: undefined,
  longTermFlag: undefined
})

const feeDialogVisible = ref(false)
const currentVehicle = ref<any>(null)
const parkingFee = ref(0)

const getCategoryType = (category: string) => {
  const types: any = { free: 'success', monthly: 'primary', temporary: 'warning' }
  return types[category] || 'info'
}

const getCategoryLabel = (category: string) => {
  const item = VehicleCategoryOptions.find(i => i.value === category)
  return item?.label || '-'
}

const formatDuration = (minutes: number) => {
  if (!minutes) return '-'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 24) {
    const days = Math.floor(hours / 24)
    return `${days}天${hours % 24}小时${mins}分钟`
  }
  return hours > 0 ? `${hours}小时${mins}分钟` : `${mins}分钟`
}

const getLotList = async () => {
  lotList.value = await ParkingLotApi.getSimpleList()
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ParkingRecordApi.getPresentVehiclePage(queryParams)
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
  queryParams.lotId = undefined
  queryParams.vehicleCategory = undefined
  queryParams.longTermFlag = undefined
  handleQuery()
}

const handleCalculateFee = async (row: any) => {
  currentVehicle.value = row
  try {
    parkingFee.value = await ParkingRecordApi.calculateFee(row.plateNumber, row.lotId)
    feeDialogVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

const handleForceExit = async (row: any) => {
  try {
    const { value: remark } = await ElMessageBox.prompt('请输入强制出场原因', '强制出场', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入原因（可选）'
    })
    await ParkingRecordApi.forceExit(row.id, remark)
    ElMessage.success('强制出场成功')
    getList()
  } catch {}
}

onMounted(() => {
  getLotList()
  getList()
})
</script>

<style scoped>
.fee-info p {
  margin: 10px 0;
  font-size: 14px;
}
.fee-amount {
  margin-top: 20px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}
.amount {
  color: #f56c6c;
  font-size: 24px;
  font-weight: bold;
}
</style>
