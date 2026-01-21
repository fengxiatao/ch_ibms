<template>
  <div class="device-event-log">
    <ContentWrap>
      <!-- 搜索条件 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="设备ID" prop="deviceId">
          <el-input v-model="queryParams.deviceId" placeholder="请输入设备ID" clearable />
        </el-form-item>
        <el-form-item label="产品ID" prop="productId">
          <el-input v-model="queryParams.productId" placeholder="请输入产品ID" clearable />
        </el-form-item>
        <el-form-item label="事件类型" prop="eventType">
          <el-select v-model="queryParams.eventType" placeholder="请选择事件类型" clearable>
            <el-option label="信息" value="info" />
            <el-option label="告警" value="alert" />
            <el-option label="错误" value="error" />
          </el-select>
        </el-form-item>
        <el-form-item label="事件时间" prop="eventTime">
          <el-date-picker
            v-model="queryParams.eventTime"
            type="datetimerange"
            range-separator="-"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <!-- 事件列表 -->
    <ContentWrap>
      <el-table v-loading="loading" :data="eventList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="deviceName" label="设备名称" min-width="150" />
        <el-table-column prop="eventName" label="事件名称" min-width="120" />
        <el-table-column prop="eventType" label="事件类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.eventType === 'info'" type="info">信息</el-tag>
            <el-tag v-else-if="row.eventType === 'alert'" type="warning">告警</el-tag>
            <el-tag v-else-if="row.eventType === 'error'" type="danger">错误</el-tag>
            <el-tag v-else>{{ row.eventType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="eventTime" label="发生时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.eventTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="processed" label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.processed" type="success">已处理</el-tag>
            <el-tag v-else type="warning">待处理</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <Pagination
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </ContentWrap>

    <!-- 事件详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="事件详情" width="800px">
      <el-descriptions :column="2" border v-if="currentEvent">
        <el-descriptions-item label="事件ID">{{ currentEvent.id }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ currentEvent.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="事件标识符">{{ currentEvent.eventIdentifier }}</el-descriptions-item>
        <el-descriptions-item label="事件名称">{{ currentEvent.eventName }}</el-descriptions-item>
        <el-descriptions-item label="事件类型">
          <el-tag v-if="currentEvent.eventType === 'info'" type="info">信息</el-tag>
          <el-tag v-else-if="currentEvent.eventType === 'alert'" type="warning">告警</el-tag>
          <el-tag v-else-if="currentEvent.eventType === 'error'" type="danger">错误</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag v-if="currentEvent.processed" type="success">已处理</el-tag>
          <el-tag v-else type="warning">待处理</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发生时间" :span="2">
          {{ formatDate(currentEvent.eventTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="ONVIF Topic" :span="2">
          {{ currentEvent.onvifTopic || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="事件数据" :span="2">
          <pre style="max-height: 300px; overflow: auto;">{{ formatJson(currentEvent.eventData) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="触发的场景规则" :span="2">
          {{ currentEvent.triggeredSceneRuleIds || '[]' }}
        </el-descriptions-item>
        <el-descriptions-item label="生成的告警记录" :span="2">
          {{ currentEvent.generatedAlertRecordIds || '[]' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as DeviceEventApi from '@/api/iot/device/event'
import { formatDate } from '@/utils/formatTime'

// 状态管理
const loading = ref(false)
const total = ref(0)
const eventList = ref<DeviceEventApi.DeviceEventLogVO[]>([])
const detailDialogVisible = ref(false)
const currentEvent = ref<DeviceEventApi.DeviceEventLogVO | null>(null)

// 查询参数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  deviceId: undefined as number | undefined,
  productId: undefined as number | undefined,
  eventType: undefined as string | undefined,
  eventTime: undefined as Date[] | undefined
})

// 获取事件列表
const getList = async () => {
  loading.value = true
  try {
    const res = await DeviceEventApi.getDeviceEventLogPage(queryParams)
    eventList.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('获取事件列表失败', error)
    ElMessage.error('获取事件列表失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.deviceId = undefined
  queryParams.productId = undefined
  queryParams.eventType = undefined
  queryParams.eventTime = undefined
  handleQuery()
}

// 查看详情
const handleDetail = async (row: DeviceEventApi.DeviceEventLogVO) => {
  try {
    currentEvent.value = await DeviceEventApi.getDeviceEventLog(row.id)
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取事件详情失败', error)
    ElMessage.error('获取事件详情失败')
  }
}

// 格式化JSON
const formatJson = (jsonStr: string) => {
  try {
    return JSON.stringify(JSON.parse(jsonStr), null, 2)
  } catch {
    return jsonStr
  }
}

// 初始化
onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.device-event-log {
  padding: 20px;

  .search-form {
    margin-bottom: 20px;
  }
}
</style>












