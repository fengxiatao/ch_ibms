<template>
  <Dialog v-model="dialogVisible" :title="`${device.name || '设备'} - 通行记录`" width="1000px">
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="80px"
    >
      <el-form-item label="开门类型" prop="openType">
        <el-select
          v-model="queryParams.openType"
          placeholder="请选择开门类型"
          clearable
          class="!w-200px"
        >
          <el-option label="远程开门" :value="1" />
          <el-option label="二维码" :value="2" />
          <el-option label="刷卡" :value="3" />
          <el-option label="人脸" :value="4" />
          <el-option label="指纹" :value="5" />
          <el-option label="密码" :value="6" />
        </el-select>
      </el-form-item>
      <el-form-item label="开门结果" prop="openResult">
        <el-select
          v-model="queryParams.openResult"
          placeholder="请选择开门结果"
          clearable
          class="!w-200px"
        >
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围" prop="openTime">
        <el-date-picker
          v-model="queryParams.openTime"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          class="!w-380px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" /> 搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" /> 重置
        </el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" stripe border>
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="人员姓名" align="center" prop="personName" min-width="120" />
      <el-table-column label="卡号" align="center" prop="cardNo" min-width="120" />
      <el-table-column label="开门类型" align="center" prop="openType" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.openType === 1" type="primary">远程开门</el-tag>
          <el-tag v-else-if="scope.row.openType === 2" type="success">二维码</el-tag>
          <el-tag v-else-if="scope.row.openType === 3" type="info">刷卡</el-tag>
          <el-tag v-else-if="scope.row.openType === 4" type="warning">人脸</el-tag>
          <el-tag v-else-if="scope.row.openType === 5" type="danger">指纹</el-tag>
          <el-tag v-else-if="scope.row.openType === 6">密码</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="开门结果" align="center" prop="openResult" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.openResult === 1 ? 'success' : 'danger'" size="small">
            {{ scope.row.openResult === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="开门时间" align="center" prop="openTime" width="180">
        <template #default="scope">
          {{ scope.row.openTime ? dateFormatter(new Date(scope.row.openTime)) : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="抓拍图片" align="center" width="100">
        <template #default="scope">
          <el-image
            v-if="scope.row.imageUrl"
            :src="scope.row.imageUrl"
            :preview-src-list="[scope.row.imageUrl]"
            fit="cover"
            style="width: 60px; height: 60px; cursor: pointer"
            :preview-teleported="true"
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="100" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleViewDetail(scope.row)">
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
      class="mt-4"
    />

    <!-- 详情弹窗 -->
    <RecordDetail ref="detailRef" />
  </Dialog>
</template>

<script setup lang="ts" name="RecordDialog">
import { ref, reactive } from 'vue'
import { dateFormatter } from '@/utils/formatTime'
import * as AccessRecordApi from '@/api/iot/access/record'
import RecordDetail from './RecordDetail.vue'

const dialogVisible = ref(false)
const loading = ref(false)
const device = ref<any>({})
const list = ref<AccessRecordApi.AccessRecordVO[]>([])
const total = ref(0)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  deviceId: undefined as number | undefined,
  openType: undefined as number | undefined,
  openResult: undefined as number | undefined,
  openTime: undefined as [Date, Date] | undefined
})

const queryFormRef = ref()
const detailRef = ref()

const open = (deviceData: any) => {
  dialogVisible.value = true
  device.value = deviceData
  queryParams.deviceId = deviceData.id
  resetQuery()
  getList()
}

const getList = async () => {
  loading.value = true
  try {
    const res = await AccessRecordApi.getAccessRecordPage(queryParams)
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  queryParams.openType = undefined
  queryParams.openResult = undefined
  queryParams.openTime = undefined
  handleQuery()
}

const handleViewDetail = (record: AccessRecordApi.AccessRecordVO) => {
  detailRef.value?.open(record.id!)
}

defineExpose({ open })
</script>

