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
      <el-form-item label="报警类型" prop="alarmType">
        <el-select
          v-model="queryParams.alarmType"
          placeholder="请选择报警类型"
          clearable
          class="!w-200px"
        >
          <el-option
            v-for="item in ChanghuiAlarmTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="报警状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择报警状态"
          clearable
          class="!w-200px"
        >
          <el-option
            v-for="item in ChanghuiAlarmStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="报警时间" prop="alarmTime">
        <el-date-picker
          v-model="alarmTimeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          class="!w-360px"
        />
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
      </el-form-item>
    </el-form>
  </ContentWrap>


  <!-- 列表 -->
  <ContentWrap>
    <!-- 统计信息 -->
    <div class="mb-4">
      <el-alert
        v-if="unacknowledgedCount > 0"
        :title="`当前有 ${unacknowledgedCount} 条未确认报警`"
        type="warning"
        show-icon
        :closable="false"
      />
    </div>

    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="测站编码" align="center" prop="stationCode" width="200" />
      <el-table-column label="报警类型" align="center" prop="alarmType" width="140">
        <template #default="scope">
          {{ getAlarmTypeName(scope.row.alarmType) }}
        </template>
      </el-table-column>
      <el-table-column label="报警值" align="center" prop="alarmValue" />
      <el-table-column label="报警时间" align="center" prop="alarmTime" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.alarmTime) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
            {{ scope.row.status === 1 ? '已确认' : '未确认' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="确认时间" align="center" prop="ackTime" width="180">
        <template #default="scope">
          {{ scope.row.ackTime ? formatDate(scope.row.ackTime) : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="确认人" align="center" prop="ackUser" width="120">
        <template #default="scope">
          {{ scope.row.ackUser || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="120" fixed="right">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === 0"
            link
            type="primary"
            @click="handleAcknowledge(scope.row.id)"
            v-hasPermi="['iot:changhui-alarm:update']"
          >
            确认
          </el-button>
          <span v-else class="text-gray-400">已处理</span>
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
  </div>
</template>

<script setup lang="ts">
import { formatDate } from '@/utils/formatTime'
import {
  ChanghuiAlarmApi,
  ChanghuiAlarmVO,
  ChanghuiAlarmTypeOptions,
  ChanghuiAlarmStatusOptions
} from '@/api/iot/changhui'

defineOptions({ name: 'ChanghuiAlarm' })

const message = useMessage()

const loading = ref(true)
const list = ref<ChanghuiAlarmVO[]>([])
const total = ref(0)
const unacknowledgedCount = ref(0)
const alarmTimeRange = ref<string[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  stationCode: undefined,
  alarmType: undefined,
  status: undefined,
  alarmTimeStart: undefined as Date | undefined,
  alarmTimeEnd: undefined as Date | undefined
})
const queryFormRef = ref()

/** 获取报警类型名称 */
const getAlarmTypeName = (type: string) => {
  const item = ChanghuiAlarmTypeOptions.find(i => i.value === type)
  return item ? item.label : type
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    // 处理时间范围
    if (alarmTimeRange.value && alarmTimeRange.value.length === 2) {
      queryParams.alarmTimeStart = new Date(alarmTimeRange.value[0])
      queryParams.alarmTimeEnd = new Date(alarmTimeRange.value[1])
    } else {
      queryParams.alarmTimeStart = undefined
      queryParams.alarmTimeEnd = undefined
    }
    const data = await ChanghuiAlarmApi.getAlarmPage(queryParams)
    list.value = data.list
    total.value = data.total
    // 获取未确认数量
    unacknowledgedCount.value = await ChanghuiAlarmApi.getUnacknowledgedCount()
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  alarmTimeRange.value = []
  handleQuery()
}

/** 确认报警 */
const handleAcknowledge = async (id: number) => {
  try {
    await message.confirm('确认要处理该报警吗？')
    await ChanghuiAlarmApi.acknowledgeAlarm(id)
    message.success('确认成功')
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
