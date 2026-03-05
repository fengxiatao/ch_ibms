<template>
  <ContentWrap
    style="
      padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
    "
  >
    <!-- 搜索工具栏 -->
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      :inline="true"
      label-width="80px"
      class="-mb-15px"
    >
      <el-form-item label="计划名称" prop="planName">
        <el-input
          v-model="queryParams.planName"
          placeholder="请输入计划名称"
          clearable
          @keyup.enter="handleQuery"
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item label="巡更人员" prop="personName">
        <el-input
          v-model="queryParams.personName"
          placeholder="请输入巡更人员姓名"
          clearable
          @keyup.enter="handleQuery"
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item label="巡更路线" prop="routeName">
        <el-input
          v-model="queryParams.routeName"
          placeholder="请输入巡更路线名称"
          clearable
          @keyup.enter="handleQuery"
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item label="计划状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="全部" :value="undefined" />
          <el-option label="未开始" :value="0" />
          <el-option label="执行中" :value="1" />
          <el-option label="已过期" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <div class="table-header">
      <span class="table-title">巡更计划列表</span>
      <el-button type="primary" @click="openForm('create')">新增计划</el-button>
    </div>
    <el-table v-loading="loading" :data="list" stripe border>
      <el-table-column label="序号" type="index" width="80" align="center" />
      <el-table-column label="计划编号" prop="planCode" min-width="150" align="center" show-overflow-tooltip />
      <el-table-column label="计划名称" prop="planName" min-width="120" align="center" />
      <el-table-column label="巡更人员" min-width="120" align="center">
        <template #default="{ row }">
          {{ row.personNames?.join('、') || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="计划时间段" min-width="180" align="center">
        <template #default="{ row }">
          {{ formatDateValue(row.startDate) }}~{{ formatDateValue(row.endDate) }}
        </template>
      </el-table-column>
      <el-table-column label="巡更路线" min-width="180" align="center">
        <template #default="{ row }">
          <span class="route-names">{{ row.routeNames?.join('、') || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="计划状态" prop="status" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.status === 0" type="info">未开始</el-tag>
          <el-tag v-else-if="row.status === 1" type="success">执行中</el-tag>
          <el-tag v-else type="danger">已过期</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row.id)">
            <Icon icon="ep:view" />
          </el-button>
          <el-button link type="primary" @click="openForm('update', row.id)">
            <Icon icon="ep:edit" />
          </el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">
            <Icon icon="ep:delete" />
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 新增/修改弹窗 -->
  <el-dialog :title="dialogTitle" v-model="dialogVisible" width="900px" append-to-body>
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="计划名称" prop="planName">
        <el-input v-model="formData.planName" placeholder="请输入计划名称" style="width: 250px" />
      </el-form-item>
      <el-form-item label="计划时间段" prop="dateRange" required>
        <el-date-picker
          v-model="formData.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 320px"
        />
      </el-form-item>
      <el-form-item label="星期选择" prop="weekdays" required>
        <el-checkbox-group v-model="formData.weekdays">
          <el-checkbox :value="1">星期一</el-checkbox>
          <el-checkbox :value="2">星期二</el-checkbox>
          <el-checkbox :value="3">星期三</el-checkbox>
          <el-checkbox :value="4">星期四</el-checkbox>
          <el-checkbox :value="5">星期五</el-checkbox>
          <el-checkbox :value="6">星期六</el-checkbox>
          <el-checkbox :value="7">星期日</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item label="时段与人员分配表" required class="periods-label">
        <div class="periods-config">
          <div class="periods-toolbar">
            <el-button type="primary" size="small" @click="openPeriodDialog">新增分配</el-button>
          </div>
          <el-table :data="formData.periods" border style="margin-top: 10px">
            <el-table-column label="序号" type="index" width="80" align="center" />
            <el-table-column label="开始时间" width="100" align="center">
              <template #default="{ row }">
                {{ formatStartTime(row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column label="巡更时长" width="100" align="center">
              <template #default="{ row }">
                {{ formatDuration(row.durationMinutes) }}
              </template>
            </el-table-column>
            <el-table-column label="巡更路线" min-width="120" align="center">
              <template #default="{ row }">
                <span class="route-name">{{ row.routeName }}</span>
              </template>
            </el-table-column>
            <el-table-column label="巡更人员" min-width="100" align="center">
              <template #default="{ row }">
                {{ row.personNames?.join('、') || row.personName || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ $index }">
                <el-button link type="primary" @click="editPeriod($index)">
                  <Icon icon="ep:edit" />
                </el-button>
                <el-button link type="danger" @click="removePeriod($index)">
                  <Icon icon="ep:delete" />
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitForm" :loading="submitLoading">确认</el-button>
    </template>
  </el-dialog>

  <!-- 新增时段与人员分配弹窗 -->
  <el-dialog
    :title="periodDialogTitle"
    v-model="periodDialogVisible"
    width="450px"
    append-to-body
  >
    <el-form ref="periodFormRef" :model="periodFormData" :rules="periodFormRules" label-width="100px">
      <el-form-item label="开始时间" prop="startTime">
        <el-time-picker
          v-model="periodFormData.startTime"
          format="HH:mm"
          value-format="HH:mm:ss"
          placeholder="请选择开始时间"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="巡更路线" prop="routeId">
        <el-select v-model="periodFormData.routeId" placeholder="请选择巡更路线" style="width: 100%" @change="handleRouteChange">
          <el-option
            v-for="item in routeOptions"
            :key="item.id"
            :label="item.routeName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="巡更人员" prop="personId">
        <el-select v-model="periodFormData.personId" placeholder="请选择巡更人员" style="width: 100%">
          <el-option
            v-for="item in personOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="periodDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitPeriod">确认</el-button>
    </template>
  </el-dialog>

  <!-- 查看详情弹窗 -->
  <el-dialog title="计划详情" v-model="viewDialogVisible" width="800px" append-to-body>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="计划编号">{{ viewData.planCode }}</el-descriptions-item>
      <el-descriptions-item label="计划名称">{{ viewData.planName }}</el-descriptions-item>
      <el-descriptions-item label="计划时间段">{{ viewData.startDate }} ~ {{ viewData.endDate }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag v-if="viewData.status === 0" type="info">未开始</el-tag>
        <el-tag v-else-if="viewData.status === 1" type="success">执行中</el-tag>
        <el-tag v-else type="danger">已过期</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="执行星期" :span="2">
        {{ weekdaysText }}
      </el-descriptions-item>
    </el-descriptions>
    <div style="margin-top: 20px">
      <h4>时段与人员分配</h4>
      <el-table :data="viewData.periods" border>
        <el-table-column label="序号" type="index" width="80" align="center" />
        <el-table-column label="开始时间" width="100" align="center">
          <template #default="{ row }">
            {{ formatStartTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column label="巡更时长" width="100" align="center">
          <template #default="{ row }">
            {{ formatDuration(row.durationMinutes) }}
          </template>
        </el-table-column>
        <el-table-column label="巡更路线" prop="routeName" min-width="120" align="center" />
        <el-table-column label="巡更人员" min-width="100" align="center">
          <template #default="{ row }">
            {{ row.personNames?.join('、') || '-' }}
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ContentWrap } from '@/components/ContentWrap'
import { Pagination } from '@/components/Pagination'
import * as EpatrolApi from '@/api/iot/epatrol'

defineOptions({ name: 'EPatrolPlan' })

const WEEKDAY_MAP = {
  1: '星期一',
  2: '星期二',
  3: '星期三',
  4: '星期四',
  5: '星期五',
  6: '星期六',
  7: '星期日'
}

// 列表相关
const loading = ref(false)
const list = ref<EpatrolApi.EpatrolPlanVO[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive<EpatrolApi.EpatrolPlanPageReqVO>({
  pageNo: 1,
  pageSize: 20,
  planName: undefined,
  personName: undefined,
  routeName: undefined,
  status: undefined
})

// 选项数据
const routeOptions = ref<EpatrolApi.EpatrolRouteVO[]>([])
const personOptions = ref<EpatrolApi.EpatrolPersonVO[]>([])

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const submitLoading = ref(false)
const formData = ref<EpatrolApi.EpatrolPlanVO & { dateRange?: string[] }>({
  planName: '',
  startDate: '',
  endDate: '',
  dateRange: [],
  weekdays: [],
  periods: []
})

const formRules = {
  planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  dateRange: [{ required: true, message: '请选择计划时间段', trigger: 'change' }]
}

// 监听 dateRange 变化，同步到 startDate 和 endDate
watch(
  () => formData.value.dateRange,
  (val) => {
    if (val && val.length === 2) {
      formData.value.startDate = val[0]
      formData.value.endDate = val[1]
    } else {
      formData.value.startDate = ''
      formData.value.endDate = ''
    }
  },
  { deep: true }
)

// 时段分配弹窗
const periodDialogVisible = ref(false)
const periodDialogTitle = ref('新增时段与人员分配')
const periodFormRef = ref()
const editingPeriodIndex = ref(-1)
const periodFormData = ref({
  startTime: null as string | null,
  routeId: undefined as number | undefined,
  routeName: '',
  personId: undefined as number | undefined,
  personName: '',
  durationMinutes: 60
})

const periodFormRules = {
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  routeId: [{ required: true, message: '请选择巡更路线', trigger: 'change' }],
  personId: [{ required: true, message: '请选择巡更人员', trigger: 'change' }]
}

// 查看弹窗
const viewDialogVisible = ref(false)
const viewData = ref<EpatrolApi.EpatrolPlanVO>({
  planName: '',
  startDate: '',
  weekdays: [],
  periods: []
})

const weekdaysText = computed(() => {
  return (
    viewData.value.weekdays?.map((d) => WEEKDAY_MAP[d as keyof typeof WEEKDAY_MAP]).join('、') ||
    '-'
  )
})

// 格式化开始时间
const formatStartTime = (time: any) => {
  if (!time) return '-'
  // 如果是字符串，直接截取
  if (typeof time === 'string') {
    return time.substring(0, 5)
  }
  // 如果是数组格式 [hour, minute, second]
  if (Array.isArray(time) && time.length >= 2) {
    const [hour, minute] = time
    return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
  }
  // 如果是 Date 对象
  if (time instanceof Date) {
    const hours = time.getHours().toString().padStart(2, '0')
    const mins = time.getMinutes().toString().padStart(2, '0')
    return `${hours}:${mins}`
  }
  return '-'
}

// 格式化时长
const formatDuration = (minutes?: number) => {
  if (!minutes) return '-'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0 && mins > 0) {
    return `${hours}小时${mins}分钟`
  } else if (hours > 0) {
    return `${hours}小时`
  } else {
    return `${mins}分钟`
  }
}

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const res = await EpatrolApi.getEpatrolPlanPage(queryParams)
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

// 获取选项数据
const getOptions = async () => {
  routeOptions.value = await EpatrolApi.getEnabledEpatrolRouteList()
  personOptions.value = await EpatrolApi.getEnabledEpatrolPersonList()
}

// 查询
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.planName = undefined
  queryParams.personName = undefined
  queryParams.routeName = undefined
  queryParams.status = undefined
  handleQuery()
}

// 格式化日期（处理数组格式 [year, month, day] 或字符串格式）
const formatDateValue = (date: any): string => {
  if (!date) return ''
  if (typeof date === 'string') return date
  if (Array.isArray(date) && date.length >= 3) {
    // 数组格式 [year, month, day]
    const [year, month, day] = date
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  }
  return ''
}

// 打开表单弹窗
const openForm = async (type: 'create' | 'update', id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增巡更计划' : '修改巡更计划'
  resetForm()
  if (id) {
    const data = await EpatrolApi.getEpatrolPlan(id)
    // 格式化日期
    const startDate = formatDateValue(data.startDate)
    const endDate = formatDateValue(data.endDate)
    data.startDate = startDate
    data.endDate = endDate
    // 将 startDate 和 endDate 同步到 dateRange
    if (startDate && endDate) {
      data.dateRange = [startDate, endDate]
    } else {
      data.dateRange = []
    }
    formData.value = data
  }
}

// 重置表单
const resetForm = () => {
  formData.value = {
    planName: '',
    startDate: '',
    endDate: '',
    dateRange: [],
    weekdays: [1, 2, 3, 4, 5],
    periods: []
  }
  formRef.value?.resetFields()
}

// 打开时段分配弹窗
const openPeriodDialog = () => {
  periodDialogTitle.value = '新增时段与人员分配'
  editingPeriodIndex.value = -1
  periodFormData.value = {
    startTime: null,
    routeId: undefined,
    routeName: '',
    personId: undefined,
    personName: '',
    durationMinutes: 60
  }
  periodDialogVisible.value = true
}

// 编辑时段
const editPeriod = (index: number) => {
  periodDialogTitle.value = '修改时段与人员分配'
  editingPeriodIndex.value = index
  const period = formData.value.periods![index]
  
  // 确保 startTime 是有效的时间字符串格式，无效时设为 null
  let startTime: string | null = null
  if (period.startTime) {
    if (typeof period.startTime === 'string' && period.startTime.trim()) {
      // 如果是字符串，确保格式为 HH:mm:ss
      const trimmed = period.startTime.trim()
      startTime = trimmed.length === 5 ? trimmed + ':00' : trimmed
    } else if (Array.isArray(period.startTime) && period.startTime.length >= 2) {
      // 数组格式 [hour, minute, second?]
      const [hour, minute, second = 0] = period.startTime
      startTime = `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:${String(second).padStart(2, '0')}`
    } else if (period.startTime instanceof Date) {
      const hours = period.startTime.getHours().toString().padStart(2, '0')
      const mins = period.startTime.getMinutes().toString().padStart(2, '0')
      const secs = period.startTime.getSeconds().toString().padStart(2, '0')
      startTime = `${hours}:${mins}:${secs}`
    }
  }
  
  periodFormData.value = {
    startTime: startTime,
    routeId: period.routeId,
    routeName: period.routeName || '',
    personId: period.personIds?.[0],
    personName: period.personNames?.[0] || '',
    durationMinutes: period.durationMinutes || 60
  }
  periodDialogVisible.value = true
}

// 路线变更时获取时长
const handleRouteChange = (routeId: number) => {
  const route = routeOptions.value.find(r => r.id === routeId)
  if (route) {
    periodFormData.value.routeName = route.routeName || ''
    periodFormData.value.durationMinutes = route.totalDuration || 60
  }
}

// 提交时段
const submitPeriod = async () => {
  await periodFormRef.value?.validate()
  
  const person = personOptions.value.find(p => p.id === periodFormData.value.personId)
  
  // 确保 startTime 是字符串格式
  let startTimeStr: string = ''
  const rawTime = periodFormData.value.startTime
  if (rawTime) {
    if (rawTime instanceof Date) {
      const hours = rawTime.getHours().toString().padStart(2, '0')
      const mins = rawTime.getMinutes().toString().padStart(2, '0')
      const secs = rawTime.getSeconds().toString().padStart(2, '0')
      startTimeStr = `${hours}:${mins}:${secs}`
    } else if (typeof rawTime === 'string') {
      startTimeStr = rawTime
    } else {
      startTimeStr = String(rawTime)
    }
  }
  
  const periodItem = {
    startTime: startTimeStr,
    routeId: periodFormData.value.routeId,
    routeName: periodFormData.value.routeName,
    personIds: [periodFormData.value.personId!],
    personNames: [person?.name || ''],
    personName: person?.name || '',
    durationMinutes: periodFormData.value.durationMinutes
  }

  if (!formData.value.periods) {
    formData.value.periods = []
  }

  if (editingPeriodIndex.value >= 0) {
    // 编辑
    formData.value.periods[editingPeriodIndex.value] = periodItem
  } else {
    // 新增
    formData.value.periods.push(periodItem)
  }

  periodDialogVisible.value = false
}

// 删除时段
const removePeriod = (index: number) => {
  formData.value.periods!.splice(index, 1)
}

// 提交表单
const submitForm = async () => {
  await formRef.value?.validate()
  
  if (!formData.value.weekdays || formData.value.weekdays.length === 0) {
    ElMessage.warning('请选择执行星期')
    return
  }
  if (!formData.value.periods || formData.value.periods.length === 0) {
    ElMessage.warning('请添加至少一个时段分配')
    return
  }
  
  submitLoading.value = true
  try {
    // 提交时删除临时的 dateRange 字段和不需要的展示字段
    const submitData = { ...formData.value }
    delete (submitData as any).dateRange
    // 清理 periods 中的展示字段，只保留后端需要的字段
    if (submitData.periods) {
      submitData.periods = submitData.periods.map(p => ({
        id: p.id,
        routeId: p.routeId,
        startTime: p.startTime,
        durationMinutes: p.durationMinutes,
        personIds: p.personIds
      }))
    }
    
    if (formData.value.id) {
      await EpatrolApi.updateEpatrolPlan(submitData)
      ElMessage.success('修改成功')
    } else {
      await EpatrolApi.createEpatrolPlan(submitData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

// 查看详情
const handleView = async (id: number) => {
  viewData.value = await EpatrolApi.getEpatrolPlan(id)
  viewDialogVisible.value = true
}

// 删除
const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确认删除该巡更计划吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await EpatrolApi.deleteEpatrolPlan(id)
  ElMessage.success('删除成功')
  getList()
}

onMounted(() => {
  getList()
  getOptions()
})
</script>

<style scoped lang="scss">
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-title {
  font-size: 16px;
  font-weight: 500;
  color: #1890ff;
}

.route-names,
.route-name {
  color: #1890ff;
}

.periods-label {
  :deep(.el-form-item__label) {
    font-weight: bold;
  }
}

.periods-config {
  width: 100%;
}

.periods-toolbar {
  display: flex;
  justify-content: flex-end;
}
</style>
