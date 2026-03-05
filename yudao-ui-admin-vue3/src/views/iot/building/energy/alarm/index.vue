<template>
  <div class="energy-alarm-page">
    <ContentWrap>
      <el-form
        ref="queryFormRef"
        :inline="true"
        :model="queryParams"
        class="-mb-15px"
        label-width="80px"
      >
        <el-form-item label="仪表名称" prop="meterName">
          <el-input
            v-model="queryParams.meterName"
            placeholder="请输入仪表编码/名称"
            clearable
            @keyup.enter="handleQuery"
            class="!w-200px"
          />
        </el-form-item>
        <el-form-item label="告警类型" prop="alarmType">
          <el-select
            v-model="queryParams.alarmType"
            placeholder="请选择告警类型"
            clearable
            class="!w-160px"
          >
            <el-option label="超限告警" :value="1" />
            <el-option label="通讯异常" :value="2" />
            <el-option label="设备故障" :value="3" />
            <el-option label="功率过载" :value="4" />
            <el-option label="电压异常" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="告警级别" prop="alarmLevel">
          <el-select
            v-model="queryParams.alarmLevel"
            placeholder="请选择告警级别"
            clearable
            class="!w-140px"
          >
            <el-option label="低" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="高" :value="3" />
            <el-option label="紧急" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态" prop="status">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择处理状态"
            clearable
            class="!w-140px"
          >
            <el-option label="未处理" :value="0" />
            <el-option label="已处理" :value="1" />
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
            class="!w-360px"
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
    </ContentWrap>

    <ContentWrap>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="ID" align="center" prop="id" width="80" />
        <el-table-column label="仪表编码" align="center" prop="meterCode" width="140" />
        <el-table-column label="仪表名称" align="center" prop="meterName" min-width="150" />
        <el-table-column label="告警类型" align="center" prop="alarmType" width="100">
          <template #default="{ row }">
            <el-tag :type="getAlarmTypeTag(row.alarmType)">{{
              getAlarmTypeName(row.alarmType)
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="告警级别" align="center" prop="alarmLevel" width="100">
          <template #default="{ row }">
            <el-tag :type="getAlarmLevelTag(row.alarmLevel)">{{
              getAlarmLevelName(row.alarmLevel)
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="告警内容"
          align="center"
          prop="alarmContent"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column label="告警值" align="center" prop="alarmValue" width="100">
          <template #default="{ row }">{{ row.alarmValue?.toFixed(2) || '-' }}</template>
        </el-table-column>
        <el-table-column label="阈值" align="center" prop="threshold" width="100">
          <template #default="{ row }">{{ row.threshold?.toFixed(2) || '-' }}</template>
        </el-table-column>
        <el-table-column label="告警时间" align="center" prop="alarmTime" width="180">
          <template #default="{ row }">
            {{ row.alarmTime ? formatDate(row.alarmTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="danger">未处理</el-tag>
            <el-tag v-else type="success">已处理</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理人" align="center" prop="handler" width="100" />
        <el-table-column label="处理时间" align="center" prop="handleTime" width="180">
          <template #default="{ row }">
            {{ row.handleTime ? formatDate(row.handleTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" link type="warning" @click="openHandleDialog(row)">
              处理
            </el-button>
            <el-button link type="primary" @click="openDetailDialog(row)"> 详情 </el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>

    <el-dialog v-model="handleDialogVisible" title="处理告警" width="500px" append-to-body>
      <el-form ref="handleFormRef" :model="handleForm" :rules="handleFormRules" label-width="100px">
        <el-form-item label="告警内容">
          <div class="text-gray-500">{{ currentAlarm?.alarmContent }}</div>
        </el-form-item>
        <el-form-item label="处理人" prop="handler">
          <el-input v-model="handleForm.handler" placeholder="请输入处理人" />
        </el-form-item>
        <el-form-item label="处理备注" prop="handleRemark">
          <el-input
            v-model="handleForm.handleRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入处理备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="告警详情" width="600px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="仪表编码">{{ currentAlarm?.meterCode }}</el-descriptions-item>
        <el-descriptions-item label="仪表名称">{{ currentAlarm?.meterName }}</el-descriptions-item>
        <el-descriptions-item label="告警类型">
          <el-tag :type="getAlarmTypeTag(currentAlarm?.alarmType)">
            {{ getAlarmTypeName(currentAlarm?.alarmType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="告警级别">
          <el-tag :type="getAlarmLevelTag(currentAlarm?.alarmLevel)">
            {{ getAlarmLevelName(currentAlarm?.alarmLevel) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="告警值">{{
          currentAlarm?.alarmValue?.toFixed(2) || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="阈值">{{
          currentAlarm?.threshold?.toFixed(2) || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="告警内容" :span="2">{{
          currentAlarm?.alarmContent
        }}</el-descriptions-item>
        <el-descriptions-item label="告警时间" :span="2">
          {{ currentAlarm?.alarmTime ? formatDate(currentAlarm.alarmTime) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag v-if="currentAlarm?.status === 0" type="danger">未处理</el-tag>
          <el-tag v-else type="success">已处理</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理人">{{
          currentAlarm?.handler || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="处理时间" :span="2">
          {{ currentAlarm?.handleTime ? formatDate(currentAlarm.handleTime) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="处理备注" :span="2">{{
          currentAlarm?.handleRemark || '-'
        }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="currentAlarm?.status === 0"
          type="warning"
          @click="detailDialogVisible = false; openHandleDialog(currentAlarm)"
        >
          处理
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import * as EnergyApi from '@/api/iot/building/energy'
import type { IbmsEnergyAlarmVO } from '@/api/iot/building/energy'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'BuildingEnergyAlarm' })

const message = useMessage()

// 列表数据
const loading = ref(true)
const list = ref<IbmsEnergyAlarmVO[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  meterName: undefined as string | undefined,
  alarmType: undefined as number | undefined,
  alarmLevel: undefined as number | undefined,
  status: undefined as number | undefined,
  alarmTime: undefined as [string, string] | undefined
})

const queryFormRef = ref()

// 处理弹窗
const handleDialogVisible = ref(false)
const handleFormRef = ref()
const submitLoading = ref(false)
const currentAlarm = ref<IbmsEnergyAlarmVO | null>(null)
const handleForm = reactive({
  handler: '',
  handleRemark: ''
})
const handleFormRules = {
  handler: [{ required: true, message: '请输入处理人', trigger: 'blur' }]
}

// 详情弹窗
const detailDialogVisible = ref(false)

// 告警类型映射
const alarmTypeMap: Record<number, string> = {
  1: '超限告警',
  2: '通讯异常',
  3: '设备故障',
  4: '功率过载',
  5: '电压异常'
}

const getAlarmTypeName = (type?: number) => {
  if (!type) return '-'
  return alarmTypeMap[type] || '未知'
}

const getAlarmTypeTag = (type?: number) => {
  const map: Record<number, string> = {
    1: 'warning',
    2: 'info',
    3: 'danger',
    4: 'danger',
    5: 'warning'
  }
  return map[type || 0] || 'info'
}

// 告警级别映射
const alarmLevelMap: Record<number, string> = {
  1: '低',
  2: '中',
  3: '高',
  4: '紧急'
}

const getAlarmLevelName = (level?: number) => {
  if (!level) return '-'
  return alarmLevelMap[level] || '未知'
}

const getAlarmLevelTag = (level?: number) => {
  const map: Record<number, string> = {
    1: 'info',
    2: '',
    3: 'warning',
    4: 'danger'
  }
  return map[level || 0] || 'info'
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const params: any = {
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      meterName: queryParams.meterName,
      alarmType: queryParams.alarmType,
      alarmLevel: queryParams.alarmLevel,
      status: queryParams.status
    }
    // 处理时间范围
    if (queryParams.alarmTime && queryParams.alarmTime.length === 2) {
      params.startTime = queryParams.alarmTime[0]
      params.endTime = queryParams.alarmTime[1]
    }
    const res = await EnergyApi.getAlarmPage(params)
    list.value = res.list
    total.value = res.total
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
  queryFormRef.value?.resetFields()
  handleQuery()
}

/** 打开处理弹窗 */
const openHandleDialog = (alarm: IbmsEnergyAlarmVO) => {
  currentAlarm.value = alarm
  handleForm.handler = ''
  handleForm.handleRemark = ''
  handleDialogVisible.value = true
}

/** 提交处理 */
const submitHandle = async () => {
  await handleFormRef.value?.validate()
  submitLoading.value = true
  try {
    await EnergyApi.handleAlarm(
      currentAlarm.value!.id!,
      handleForm.handler,
      handleForm.handleRemark
    )
    message.success('处理成功')
    handleDialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

/** 打开详情弹窗 */
const openDetailDialog = (alarm: IbmsEnergyAlarmVO) => {
  currentAlarm.value = alarm
  detailDialogVisible.value = true
}

// 初始化
onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.energy-alarm-page {
  box-sizing: border-box;
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
  height: 100%;
  overflow: auto;
}
</style>
