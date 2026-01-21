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
      <el-form-item label="人员姓名" prop="personName">
        <el-input
          v-model="queryParams.personName"
          placeholder="请输入人员姓名"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="卡号" prop="cardNo">
        <el-input
          v-model="queryParams.cardNo"
          placeholder="请输入卡号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="开门类型" prop="openType">
        <el-select
          v-model="queryParams.openType"
          placeholder="请选择开门类型"
          clearable
          class="!w-240px"
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
          class="!w-240px"
        >
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="通行时间" prop="openTime">
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
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['iot:access-record:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="总记录数" :value="statistics.total">
            <template #prefix>
              <Icon icon="ep:document" class="mr-5px" />
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="成功记录" :value="statistics.success">
            <template #prefix>
              <Icon icon="ep:circle-check" class="mr-5px" style="color: #67c23a" />
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="失败记录" :value="statistics.failed">
            <template #prefix>
              <Icon icon="ep:circle-close" class="mr-5px" style="color: #f56c6c" />
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="今日记录" :value="statistics.today">
            <template #prefix>
              <Icon icon="ep:calendar" class="mr-5px" style="color: #409eff" />
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="设备名称" align="center" prop="deviceName" min-width="150" />
      <el-table-column label="人员姓名" align="center" prop="personName" min-width="120" />
      <el-table-column label="证件号" align="center" prop="idCard" min-width="160" show-overflow-tooltip />
      <el-table-column label="卡号" align="center" prop="cardNo" min-width="120" />
      <el-table-column label="出入状态" align="center" prop="direction" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.direction === 1" type="success">进门</el-tag>
          <el-tag v-else-if="scope.row.direction === 2" type="warning">出门</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
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
      <el-table-column label="体温" align="center" prop="temperature" width="80">
        <template #default="scope">
          <span v-if="scope.row.temperature">{{ scope.row.temperature }}°C</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="通行时间" align="center" prop="openTime" width="180">
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
          <el-button
            link
            type="primary"
            @click="openDetail(scope.row.id)"
            v-hasPermi="['iot:access-record:query']"
          >
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
    />
  </ContentWrap>

  <!-- 详情弹窗 -->
  <RecordDetail ref="detailRef" />
</template>

<script setup lang="ts" name="AccessRecord">
import { dateFormatter } from '@/utils/formatTime'
import { ref, reactive, onMounted } from 'vue'
import * as AccessRecordApi from '@/api/iot/access/record'
import * as DeviceApi from '@/api/iot/device/device'
import RecordDetail from './RecordDetail.vue'
import download from '@/utils/download'

const message = useMessage()

// 列表数据
const loading = ref(true)
const list = ref<AccessRecordApi.AccessRecordVO[]>([])
const total = ref(0)
const exportLoading = ref(false)

// 统计信息
const statistics = reactive({
  total: 0,
  success: 0,
  failed: 0,
  today: 0
})

// 查询参数
const queryParams = ref({
  pageNo: 1,
  pageSize: 10,
  deviceId: undefined,
  personName: undefined,
  cardNo: undefined,
  openType: undefined,
  openResult: undefined,
  openTime: undefined as [string, string] | undefined
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
    const res = await AccessRecordApi.getAccessRecordPage(queryParams.value)
    list.value = res.list
    total.value = res.total
    
    // 更新统计信息
    await loadStatistics()
  } finally {
    loading.value = false
  }
}

/** 加载统计信息 */
const loadStatistics = async () => {
  try {
    // 获取今日开始时间
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    const todayStart = today.toISOString().slice(0, 19).replace('T', ' ')
    
    // 查询统计
    const [totalRes, successRes, failedRes, todayRes] = await Promise.all([
      AccessRecordApi.getAccessRecordPage({ pageNo: 1, pageSize: 1 }),
      AccessRecordApi.getAccessRecordPage({ pageNo: 1, pageSize: 1, openResult: 1 }),
      AccessRecordApi.getAccessRecordPage({ pageNo: 1, pageSize: 1, openResult: 0 }),
      AccessRecordApi.getAccessRecordPage({ 
        pageNo: 1, 
        pageSize: 1, 
        openTime: [todayStart, new Date().toISOString().slice(0, 19).replace('T', ' ')]
      })
    ])
    
    statistics.total = totalRes.total
    statistics.success = successRes.total
    statistics.failed = failedRes.total
    statistics.today = todayRes.total
  } catch (error) {
    console.error('[通行记录] 加载统计信息失败:', error)
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
    console.error('[通行记录] 加载设备列表失败:', error)
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

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await AccessRecordApi.exportAccessRecordExcel(queryParams.value)
    download.excel(data, '通行记录.xls')
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




















