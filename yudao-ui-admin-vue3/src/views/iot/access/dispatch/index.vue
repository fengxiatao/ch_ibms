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
    <!-- 搜索栏 -->
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="80px"
    >
      <el-form-item label="设备名称" prop="deviceId">
        <el-select
          v-model="queryParams.deviceId"
          placeholder="请选择设备"
          clearable
          filterable
          class="!w-240px"
        >
          <el-option
            v-for="device in deviceList"
            :key="device.id"
            :label="device.deviceName || device.nickname"
            :value="device.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="下发类型" prop="dispatchType">
        <el-select
          v-model="queryParams.dispatchType"
          placeholder="请选择下发类型"
          clearable
          class="!w-240px"
        >
          <el-option label="授权下发" :value="1" />
          <el-option label="取消授权" :value="2" />
          <el-option label="时间表下发" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="下发状态" prop="dispatchStatus">
        <el-select
          v-model="queryParams.dispatchStatus"
          placeholder="请选择下发状态"
          clearable
          class="!w-240px"
        >
          <el-option label="待下发" :value="0" />
          <el-option label="下发中" :value="1" />
          <el-option label="下发成功" :value="2" />
          <el-option label="下发失败" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="下发时间" prop="dispatchTime">
        <el-date-picker
          v-model="queryParams.dispatchTime"
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
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['iot:access-dispatch:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="设备名称" align="center" prop="deviceName" min-width="150" />
      <el-table-column label="下发类型" align="center" prop="dispatchType" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.dispatchType === 1" type="primary">授权下发</el-tag>
          <el-tag v-else-if="scope.row.dispatchType === 2" type="warning">取消授权</el-tag>
          <el-tag v-else-if="scope.row.dispatchType === 3" type="info">时间表下发</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="下发状态" align="center" prop="dispatchStatus" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.dispatchStatus === 0" type="info">待下发</el-tag>
          <el-tag v-else-if="scope.row.dispatchStatus === 1" type="warning">下发中</el-tag>
          <el-tag v-else-if="scope.row.dispatchStatus === 2" type="success">下发成功</el-tag>
          <el-tag v-else-if="scope.row.dispatchStatus === 3" type="danger">下发失败</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="下发时间" align="center" prop="dispatchTime" width="180">
        <template #default="scope">
          {{ scope.row.dispatchTime ? dateFormatter(new Date(scope.row.dispatchTime)) : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="响应时间" align="center" prop="responseTime" width="180">
        <template #default="scope">
          {{ scope.row.responseTime ? dateFormatter(new Date(scope.row.responseTime)) : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="错误信息" align="center" prop="errorMsg" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="150" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openDetail(scope.row.id)"
            v-hasPermi="['iot:access-dispatch:query']"
          >
            详情
          </el-button>
          <el-button
            v-if="scope.row.dispatchStatus === 3"
            link
            type="warning"
            @click="handleRetry(scope.row.id)"
            v-hasPermi="['iot:access-dispatch:redispatch']"
          >
            重新下发
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
    />
  </ContentWrap>

  <!-- 详情弹窗 -->
  <DispatchDetail ref="detailRef" />
</template>

<script setup lang="ts" name="AccessDispatch">
import { dateFormatter } from '@/utils/formatTime'
import { ref, onMounted } from 'vue'
import * as AccessDispatchApi from '@/api/iot/access/dispatch'
import * as DeviceApi from '@/api/iot/device/device'
import DispatchDetail from './DispatchDetail.vue'
import download from '@/utils/download'
import { ElMessage, ElMessageBox } from 'element-plus'

const message = useMessage()

// 列表数据
const loading = ref(true)
const list = ref<AccessDispatchApi.AccessDispatchVO[]>([])
const total = ref(0)
const exportLoading = ref(false)

// 查询参数
const queryParams = ref({
  pageNo: 1,
  pageSize: 10,
  deviceId: undefined,
  dispatchType: undefined,
  dispatchStatus: undefined,
  dispatchTime: undefined as [string, string] | undefined
})

// 设备列表
const deviceList = ref<any[]>([])

// 查询表单引用
const queryFormRef = ref()
const detailRef = ref()

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const res = await AccessDispatchApi.getAccessDispatchPage(queryParams.value)
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

/** 加载设备列表 */
const loadDeviceList = async () => {
  try {
    const res = await DeviceApi.getDevicePage({
      pageNo: 1,
      pageSize: 100,
      subsystemCode: 'access.door'
    })
    deviceList.value = res.list
  } catch (error) {
    console.error('[下发记录] 加载设备列表失败:', error)
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 详情操作 */
const openDetail = (id: number) => {
  detailRef.value.open(id)
}

/** 重新下发 */
const handleRetry = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认要重新下发该记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await AccessDispatchApi.retryDispatch(id)
    message.success('重新下发成功')
    await getList()
  } catch (error: any) {
    if (error !== 'cancel') {
      message.error('重新下发失败：' + (error.message || '未知错误'))
    }
  }
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await AccessDispatchApi.exportAccessDispatchExcel(queryParams.value)
    download.excel(data, '下发记录.xls')
  } finally {
    exportLoading.value = false
  }
}

// 初始化
onMounted(() => {
  loadDeviceList()
  getList()
})
</script>




















