<script setup lang="ts">
import dayjs from 'dayjs'
import EnergyPageContainer from '../components/EnergyPageContainer.vue'
import EnergyPageHeader from '../components/EnergyPageHeader.vue'
import * as EnergyApi from '@/api/iot/building/energy'
import type { IbmsEnergyAlarmVO } from '@/api/iot/building/energy'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'EnergyAlarmManagement' })

type AlarmLevel = 'critical' | 'major' | 'minor' | 'info'
type AlarmStatus = 'unhandled' | 'handled'

interface AlarmItem {
  id: number
  time: string
  level: AlarmLevel
  typeText: string
  device: string
  desc: string
  status: AlarmStatus
}

const nowText = ref(dayjs().format('YYYY-MM-DD HH:mm:ss'))
let timer: number | undefined
onMounted(() => {
  timer = window.setInterval(() => {
    nowText.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
  }, 1000)
})
onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
})

// 查询参数（直接传给后端分页接口）
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  alarmLevel: undefined as number | undefined,
  status: undefined as number | undefined,
  keyword: '',
  alarmTime: undefined as [string, string] | undefined
})

// 后端原始数据 & 映射后的展示数据
const rawAlarms = ref<IbmsEnergyAlarmVO[]>([])
const alarms = computed<AlarmItem[]>(() =>
  (rawAlarms.value || []).map((a) => ({
    id: a.id!,
    time: a.alarmTime ? formatDate(a.alarmTime as any) : '',
    level: mapAlarmLevel(a.alarmLevel),
    typeText: getAlarmTypeText(a.alarmType),
    device: a.meterName || a.meterCode || '-',
    desc: a.alarmContent || '',
    status:
      a.status === 0 || a.status === 1
        ? ('unhandled' as const)
        : ('handled' as const)
  }))
)

const loading = ref(false)
const total = ref(0)

// 告警级别映射：后端 1-一般 2-重要 3-紧急 -> 前端展示
const mapAlarmLevel = (level?: number | null): AlarmLevel => {
  switch (level) {
    case 3:
      return 'critical'
    case 2:
      return 'major'
    case 1:
    default:
      return 'minor'
  }
}

// 告警类型文案映射：后端 alarmType 1-超标 2-异常 3-离线 4-通信 5-电压异常
const alarmTypeMap: Record<number, string> = {
  1: '用量超标',
  2: '异常波动',
  3: '设备离线',
  4: '通讯异常',
  5: '电压异常'
}
const getAlarmTypeText = (type?: number | null) =>
  type ? alarmTypeMap[type] || '其他告警' : '其他告警'

// 从后端加载列表（分页）
const getList = async () => {
  loading.value = true
  try {
    const params: any = {
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      alarmLevel: queryParams.alarmLevel,
      status: queryParams.status
    }
    if (queryParams.keyword?.trim()) {
      // 关键字简单映射到表具名称模糊搜索
      params.meterName = queryParams.keyword.trim()
    }
    if (queryParams.alarmTime && queryParams.alarmTime.length === 2) {
      params.startTime = queryParams.alarmTime[0]
      params.endTime = queryParams.alarmTime[1]
    }
    const res = await EnergyApi.getAlarmPage(params)
    rawAlarms.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

// 查询条件重置
const resetQuery = () => {
  queryParams.pageNo = 1
  queryParams.alarmLevel = undefined
  queryParams.status = undefined
  queryParams.keyword = ''
  queryParams.alarmTime = undefined
  getList()
}

// 统计信息基于当前页数据
const stats = computed(() => {
  const all = alarms.value
  const count = (level: AlarmLevel) => all.filter((a) => a.level === level).length
  return {
    critical: count('critical'),
    major: count('major'),
    minor: count('minor'),
    total: all.length
  }
})

const levelLabel: Record<AlarmLevel, string> = {
  critical: '紧急',
  major: '重要',
  minor: '次要',
  info: '提示'
}

const levelTagType: Record<AlarmLevel, any> = {
  critical: 'danger',
  major: 'warning',
  minor: 'success',
  info: 'info'
}

const statusLabel: Record<AlarmStatus, string> = {
  unhandled: '未处理',
  handled: '已处理'
}

const statusTagType: Record<AlarmStatus, any> = {
  unhandled: 'danger',
  handled: 'success'
}

const detailVisible = ref(false)
const handleVisible = ref(false)
const currentAlarm = ref<AlarmItem | null>(null)

const openDetail = (row: AlarmItem) => {
  currentAlarm.value = row
  detailVisible.value = true
}

const openHandle = (row: AlarmItem) => {
  currentAlarm.value = row
  handleVisible.value = true
}

const handleForm = reactive({
  opinion: '',
  person: '管理员'
})

// 提交处理：调用后端 /alarm/handle 接口
const submitHandle = async () => {
  if (!currentAlarm.value) return
  if (!handleForm.opinion.trim()) {
    ElMessage.warning('请输入处理意见')
    return
  }
  try {
    await EnergyApi.handleAlarm(
      currentAlarm.value.id,
      handleForm.person,
      handleForm.opinion
    )
    ElMessage.success('告警处理已提交')
    handleVisible.value = false
    detailVisible.value = false
    handleForm.opinion = ''
    await getList()
  } catch (e) {
    ElMessage.error('处理失败，请稍后重试')
  }
}

// 初始化加载
onMounted(() => {
  getList()
})
</script>

<template>
  <EnergyPageContainer>
    <EnergyPageHeader title="告警管理" subtitle="告警列表与处理闭环">
      <template #actions>
        <ElTag effect="plain"><Icon icon="ep:calendar" class="mr-5px" /> {{ nowText }}</ElTag>
        <ElTag type="info" effect="plain"><Icon icon="ep:user" class="mr-5px" /> 管理员</ElTag>
      </template>
    </EnergyPageHeader>

    <div class="alarm-stats">
      <div class="alarm-stat alarm-stat--critical">
        <div class="alarm-stat__value">{{ stats.critical }}</div>
        <div class="alarm-stat__label">紧急告警</div>
      </div>
      <div class="alarm-stat alarm-stat--major">
        <div class="alarm-stat__value">{{ stats.major }}</div>
        <div class="alarm-stat__label">重要告警</div>
      </div>
      <div class="alarm-stat alarm-stat--minor">
        <div class="alarm-stat__value">{{ stats.minor }}</div>
        <div class="alarm-stat__label">次要告警</div>
      </div>
      <div class="alarm-stat alarm-stat--total">
        <div class="alarm-stat__value">{{ stats.total }}</div>
        <div class="alarm-stat__label">总计告警</div>
      </div>
    </div>

    <ElCard shadow="never">
      <div class="table-header">
        <div class="table-title">🚨 告警列表</div>
        <div class="table-filters">
          <ElSelect v-model="queryParams.alarmLevel" placeholder="全部级别" clearable class="w-140px">
            <ElOption label="紧急" :value="3" />
            <ElOption label="重要" :value="2" />
            <ElOption label="次要" :value="1" />
          </ElSelect>
          <ElSelect v-model="queryParams.status" placeholder="全部状态" clearable class="w-140px">
            <ElOption label="未处理" :value="0" />
            <ElOption label="已处理" :value="2" />
          </ElSelect>
          <ElInput v-model="queryParams.keyword" placeholder="搜索告警（仪表名称）..." clearable class="w-220px" />
          <ElDatePicker
            v-model="queryParams.alarmTime"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            class="w-360px"
          />
          <ElButton type="primary" @click="() => { queryParams.pageNo = 1; getList(); }">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </ElButton>
          <ElButton @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</ElButton>
        </div>
      </div>

      <ElTable :data="alarms" stripe v-loading="loading">
        <ElTableColumn label="告警ID" prop="id" width="120" />
        <ElTableColumn label="告警时间" prop="time" width="180" />
        <ElTableColumn label="级别" width="100">
          <template #default="{ row }">
            <ElTag size="small" :type="levelTagType[row.level]" effect="dark">{{ levelLabel[row.level] }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="类型" width="100">
          <template #default="{ row }">
            <ElTag size="small" type="info" effect="plain">{{ row.typeText }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="设备/区域" prop="device" min-width="160" />
        <ElTableColumn label="描述" prop="desc" min-width="220" show-overflow-tooltip />
        <ElTableColumn label="状态" width="100">
          <template #default="{ row }">
            <ElTag size="small" :type="statusTagType[row.status]" effect="dark">{{ statusLabel[row.status] }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <ElButton link type="primary" @click="openDetail(row)">查看</ElButton>
            <ElButton v-if="row.status === 'unhandled'" link type="warning" @click="openHandle(row)">处理</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ElCard>

    <ElDialog v-model="detailVisible" title="告警详情" width="720px">
      <template v-if="currentAlarm">
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="告警ID">{{ currentAlarm.id }}</ElDescriptionsItem>
          <ElDescriptionsItem label="告警级别">
            <ElTag size="small" :type="levelTagType[currentAlarm.level]" effect="dark">
              {{ levelLabel[currentAlarm.level] }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="告警时间">{{ currentAlarm.time }}</ElDescriptionsItem>
        <ElDescriptionsItem label="告警类型">
          <ElTag size="small" type="info" effect="plain">
            {{ currentAlarm.typeText }}
          </ElTag>
        </ElDescriptionsItem>
          <ElDescriptionsItem label="所属区域/设备" :span="2">{{ currentAlarm.device }}</ElDescriptionsItem>
          <ElDescriptionsItem label="当前状态" :span="2">
            <ElTag size="small" :type="statusTagType[currentAlarm.status]" effect="dark">
              {{ statusLabel[currentAlarm.status] }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="告警描述" :span="2">{{ currentAlarm.desc }}</ElDescriptionsItem>
        </ElDescriptions>
      </template>
      <template #footer>
        <ElButton @click="detailVisible = false">关闭</ElButton>
        <ElButton v-if="currentAlarm?.status === 'unhandled'" type="primary" @click="openHandle(currentAlarm)">
          处理告警
        </ElButton>
      </template>
    </ElDialog>

    <ElDialog v-model="handleVisible" :title="`处理告警 - ${currentAlarm?.id || ''}`" width="720px">
      <ElForm label-position="top">
        <ElFormItem label="处理意见">
          <ElInput v-model="handleForm.opinion" type="textarea" :rows="4" placeholder="请描述处理过程、结果和后续建议..." />
          <div class="handle-tip">提示：建议包含处理人、处理时间、处理措施、处理结果等关键信息</div>
        </ElFormItem>
        <ElFormItem label="处理人">
          <ElInput v-model="handleForm.person" placeholder="请输入处理人姓名" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="handleVisible = false">取消</ElButton>
        <ElButton type="primary" @click="submitHandle">提交</ElButton>
      </template>
    </ElDialog>
  </EnergyPageContainer>
</template>

<style scoped lang="scss">
.alarm-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.alarm-stat {
  border-radius: 14px;
  padding: 18px 18px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
}

.alarm-stat__value {
  font-size: 28px;
  font-weight: 900;
}

.alarm-stat__label {
  margin-top: 6px;
  font-size: 12px;
  font-weight: 800;
  color: var(--el-text-color-secondary);
}

.alarm-stat--critical .alarm-stat__value {
  color: var(--el-color-danger);
}
.alarm-stat--major .alarm-stat__value {
  color: var(--el-color-warning);
}
.alarm-stat--minor .alarm-stat__value {
  color: var(--el-color-success);
}
.alarm-stat--total .alarm-stat__value {
  color: var(--el-color-primary);
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: nowrap;
  margin-bottom: 12px;
}

.table-title {
  font-weight: 900;
  color: var(--el-text-color-primary);
  flex-shrink: 0;
}

.table-filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: nowrap;
  white-space: nowrap;
  flex: 1;
  justify-content: flex-end;
  overflow-x: auto;
}

.handle-tip {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

@media (max-width: 1200px) {
  .alarm-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
