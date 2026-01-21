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
      <el-form-item label="告警类型" prop="alarmType">
        <el-select
          v-model="queryParams.alarmType"
          placeholder="请选择告警类型"
          clearable
          class="!w-240px"
        >
          <el-option label="非法开门" :value="1" />
          <el-option label="门未关" :value="2" />
          <el-option label="胁迫开门" :value="3" />
          <el-option label="设备故障" :value="4" />
          <el-option label="网络异常" :value="5" />
        </el-select>
      </el-form-item>
      <el-form-item label="告警级别" prop="alarmLevel">
        <el-select
          v-model="queryParams.alarmLevel"
          placeholder="请选择告警级别"
          clearable
          class="!w-240px"
        >
          <el-option label="低" :value="1" />
          <el-option label="中" :value="2" />
          <el-option label="高" :value="3" />
          <el-option label="紧急" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="处理状态" prop="handleStatus">
        <el-select
          v-model="queryParams.handleStatus"
          placeholder="请选择处理状态"
          clearable
          class="!w-240px"
        >
          <el-option label="未处理" :value="0" />
          <el-option label="处理中" :value="1" />
          <el-option label="已处理" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="告警时间" prop="alarmTime">
        <el-date-picker
          v-model="queryParams.alarmTime"
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
          type="warning"
          plain
          @click="handleBatchHandle"
          :disabled="selectedIds.length === 0"
          v-hasPermi="['iot:access-alarm:batch-handle']"
        >
          <Icon icon="ep:operation" class="mr-5px" /> 批量处理
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['iot:access-alarm:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table 
      v-loading="loading" 
      :data="list" 
      stripe
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="设备名称" align="center" prop="deviceName" min-width="150" />
      <el-table-column label="告警类型" align="center" prop="alarmType" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.alarmType === 1" type="danger">非法开门</el-tag>
          <el-tag v-else-if="scope.row.alarmType === 2" type="warning">门未关</el-tag>
          <el-tag v-else-if="scope.row.alarmType === 3" type="danger">胁迫开门</el-tag>
          <el-tag v-else-if="scope.row.alarmType === 4" type="info">设备故障</el-tag>
          <el-tag v-else-if="scope.row.alarmType === 5" type="warning">网络异常</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="告警级别" align="center" prop="alarmLevel" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.alarmLevel === 1" type="info">低</el-tag>
          <el-tag v-else-if="scope.row.alarmLevel === 2">中</el-tag>
          <el-tag v-else-if="scope.row.alarmLevel === 3" type="warning">高</el-tag>
          <el-tag v-else-if="scope.row.alarmLevel === 4" type="danger">紧急</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="告警内容" align="center" prop="alarmContent" min-width="200" show-overflow-tooltip />
      <el-table-column label="告警时间" align="center" prop="alarmTime" width="180">
        <template #default="scope">
          {{ scope.row.alarmTime ? dateFormatter(new Date(scope.row.alarmTime)) : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="处理状态" align="center" prop="handleStatus" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.handleStatus === 0" type="danger">未处理</el-tag>
          <el-tag v-else-if="scope.row.handleStatus === 1" type="warning">处理中</el-tag>
          <el-tag v-else-if="scope.row.handleStatus === 2" type="success">已处理</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="处理人" align="center" prop="handleUserName" width="100" />
      <el-table-column label="处理时间" align="center" prop="handleTime" width="180">
        <template #default="scope">
          {{ scope.row.handleTime ? dateFormatter(new Date(scope.row.handleTime)) : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openDetail(scope.row.id)"
            v-hasPermi="['iot:access-alarm:query']"
          >
            详情
          </el-button>
          <el-button
            v-if="scope.row.handleStatus !== 2"
            link
            type="warning"
            @click="openHandle(scope.row)"
            v-hasPermi="['iot:access-alarm:handle']"
          >
            处理
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
  <AlarmDetail ref="detailRef" @handle="openHandle" />
  
  <!-- 处理弹窗 -->
  <AlarmHandle ref="handleRef" @success="getList" />
</template>

<script setup lang="ts" name="AccessAlarm">
import { dateFormatter } from '@/utils/formatTime'
import { ref, onMounted } from 'vue'
import * as AccessAlarmApi from '@/api/iot/access/alarm'
import * as DeviceApi from '@/api/iot/device/device'
import AlarmDetail from './AlarmDetail.vue'
import AlarmHandle from './AlarmHandle.vue'
import download from '@/utils/download'

const message = useMessage()

// 列表数据
const loading = ref(true)
const list = ref<AccessAlarmApi.AccessAlarmVO[]>([])
const total = ref(0)
const exportLoading = ref(false)
const selectedIds = ref<number[]>([])

// 查询参数
const queryParams = ref({
  pageNo: 1,
  pageSize: 10,
  deviceId: undefined,
  alarmType: undefined,
  alarmLevel: undefined,
  handleStatus: undefined,
  alarmTime: undefined as [string, string] | undefined
})

// 设备列表
const deviceList = ref<any[]>([])

// 查询表单引用
const queryFormRef = ref()
const detailRef = ref()
const handleRef = ref()

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const res = await AccessAlarmApi.getAccessAlarmPage(queryParams.value)
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
    console.error('[告警记录] 加载设备列表失败:', error)
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

/** 选择变化 */
const handleSelectionChange = (selection: AccessAlarmApi.AccessAlarmVO[]) => {
  selectedIds.value = selection.map(item => item.id!)
}

/** 详情操作 */
const openDetail = (id: number) => {
  detailRef.value.open(id)
}

/** 处理操作 */
const openHandle = (alarm: AccessAlarmApi.AccessAlarmVO) => {
  handleRef.value.open(alarm)
}

/** 批量处理 */
const handleBatchHandle = () => {
  if (selectedIds.value.length === 0) {
    message.warning('请先选择要处理的告警记录')
    return
  }
  handleRef.value.openBatch(selectedIds.value)
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await AccessAlarmApi.exportAccessAlarmExcel(queryParams.value)
    download.excel(data, '告警记录.xls')
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

