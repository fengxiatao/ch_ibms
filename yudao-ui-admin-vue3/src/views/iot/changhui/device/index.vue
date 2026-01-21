<template>
  <div class="ch-page">
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="80px"
    >
      <el-form-item label="测站编码" prop="stationCode">
        <el-input
          v-model="queryParams.stationCode"
          placeholder="请输入测站编码"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="设备名称" prop="deviceName">
        <el-input
          v-model="queryParams.deviceName"
          placeholder="请输入设备名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="设备类型" prop="deviceType">
        <el-select
          v-model="queryParams.deviceType"
          placeholder="请选择设备类型"
          clearable
          class="!w-200px"
        >
          <el-option
            v-for="item in deviceTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="设备状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择设备状态"
          clearable
          class="!w-200px"
        >
          <el-option
            v-for="item in ChanghuiDeviceStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" />
          重置
        </el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
        >
          <Icon icon="ep:plus" class="mr-5px" />
          新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="测站编码" align="center" prop="stationCode" width="200" />
      <el-table-column label="设备名称" align="center" prop="deviceName" />
      <el-table-column label="设备类型" align="center" prop="deviceType" width="160">
        <template #default="scope">
          {{ getDeviceTypeName(scope.row.deviceType) }}
        </template>
      </el-table-column>
      <el-table-column label="设备状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '在线' : '离线' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最后心跳" align="center" prop="lastHeartbeat" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.lastHeartbeat) }}
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['iot:changhui-device:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="primary"
            @click="openDetail(scope.row)"
          >
            详情
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['iot:changhui-device:delete']"
          >
            删除
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

  <!-- 表单弹窗：添加/修改 -->
  <ChanghuiDeviceForm ref="formRef" @success="getList" />
  <!-- 详情弹窗 -->
  <ChanghuiDeviceDetail ref="detailRef" />
  </div>
</template>


<script setup lang="ts">
import { formatDate } from '@/utils/formatTime'
import { DICT_TYPE, getIntDictOptions, getDictLabel } from '@/utils/dict'
import {
  ChanghuiDeviceApi,
  ChanghuiDeviceVO,
  ChanghuiDeviceStatusOptions
} from '@/api/iot/changhui'
import ChanghuiDeviceForm from './ChanghuiDeviceForm.vue'
import ChanghuiDeviceDetail from './ChanghuiDeviceDetail.vue'
import { useDeviceStatusWebSocket, DeviceStatusPushMessage, DeviceStateEnum } from '@/views/iot/device/device/useDeviceStatusWebSocket'

defineOptions({ name: 'ChanghuiDevice' })

/** 设备类型选项（从数据字典获取） */
const deviceTypeOptions = getIntDictOptions(DICT_TYPE.CHANGHUI_DEVICE_TYPE)

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<ChanghuiDeviceVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  stationCode: undefined,
  deviceName: undefined,
  deviceType: undefined,
  status: undefined
})
const queryFormRef = ref()

/** 获取设备类型名称（从数据字典） */
const getDeviceTypeName = (type: number) => {
  return getDictLabel(DICT_TYPE.CHANGHUI_DEVICE_TYPE, type) || '未知'
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await ChanghuiDeviceApi.getDevicePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 处理 WebSocket 设备状态变更 */
const handleDeviceStatusChange = (message: DeviceStatusPushMessage) => {
  // 只处理长辉设备类型
  if (message.deviceType !== 'CHANGHUI') {
    return
  }
  
  // 在列表中查找设备并更新状态
  const device = list.value.find(d => d.id === message.deviceId)
  if (device) {
    // 只有 ONLINE(1) 状态才视为在线
    const isOnline = message.newState === DeviceStateEnum.ONLINE
    device.status = isOnline ? 1 : 0
    console.log(`[长辉设备] 状态实时更新: ${device.deviceName} -> ${isOnline ? '在线' : '离线'}`)
  }
}

/** WebSocket 连接 - 订阅长辉设备状态变更 */
const { connected: wsConnected } = useDeviceStatusWebSocket({
  deviceTypes: ['CHANGHUI'],
  onStatusChange: handleDeviceStatusChange,
  onConnectionChange: (isConnected) => {
    console.log('[长辉设备] WebSocket 连接状态:', isConnected ? '已连接' : '已断开')
  }
})

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 详情操作 */
const detailRef = ref()
const openDetail = (row: ChanghuiDeviceVO) => {
  detailRef.value.open(row)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await ChanghuiDeviceApi.deleteDevice(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/** 初始化 */
onMounted(() => {
  getList()
})
</script>

<style scoped>
.ch-page {
  padding-top: var(--page-top-gap);
}
</style>
