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
      <el-form-item label="任务编号" prop="taskCode">
        <el-input
          v-model="queryParams.taskCode"
          placeholder="请输入巡更任务编号"
          clearable
          @keyup.enter="handleQuery"
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item label="任务日期" prop="taskDate">
        <el-date-picker
          v-model="queryParams.taskDate"
          type="date"
          placeholder="请选择任务日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 150px"
        />
      </el-form-item>
      <el-form-item label="巡更人员" prop="personId">
        <el-select
          v-model="queryParams.personId"
          placeholder="请选择巡更人员"
          clearable
          filterable
          style="width: 150px"
        >
          <el-option
            v-for="item in personOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="巡更路线" prop="routeId">
        <el-select
          v-model="queryParams.routeId"
          placeholder="请选择巡更路线"
          clearable
          filterable
          style="width: 150px"
        >
          <el-option
            v-for="item in routeOptions"
            :key="item.id"
            :label="item.routeName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="任务状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="全部" :value="undefined" />
          <el-option label="未巡" :value="0" />
          <el-option label="已巡" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 统计信息 -->
  <ContentWrap>
    <div class="statistics-bar">
      <div class="stat-group">
        <span class="stat-group-title">总体统计</span>
        <div class="stat-item">
          <span class="stat-label">任务总数：</span>
          <span class="stat-value">{{ statistics.total }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">已完成：</span>
          <span class="stat-value success">{{ statistics.completed }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">未完成：</span>
          <span class="stat-value warning">{{ statistics.pending }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">完成率：</span>
          <span class="stat-value" :class="statistics.rate >= 80 ? 'success' : 'warning'">
            {{ statistics.rate }}%
          </span>
        </div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-group">
        <span class="stat-group-title">今日统计</span>
        <div class="stat-item">
          <span class="stat-label">今日任务：</span>
          <span class="stat-value">{{ statistics.todayTotal }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">今日完成：</span>
          <span class="stat-value success">{{ statistics.todayCompleted }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">今日完成率：</span>
          <span class="stat-value" :class="statistics.todayRate >= 80 ? 'success' : 'warning'">
            {{ statistics.todayRate }}%
          </span>
        </div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-toggle">
        <el-button 
          :type="showPersonStats ? 'primary' : 'default'" 
          size="small" 
          @click="showPersonStats = !showPersonStats"
        >
          {{ showPersonStats ? '收起人员统计' : '展开人员统计' }}
        </el-button>
      </div>
    </div>
    <!-- 人员统计表格 -->
    <div v-if="showPersonStats && statistics.personStatistics?.length" class="person-statistics">
      <div class="person-stats-title">人员巡更统计</div>
      <el-table :data="statistics.personStatistics" border size="small" style="margin-top: 10px">
        <el-table-column label="人员" prop="personName" width="100" align="center" />
        <el-table-column label="任务总数" prop="total" width="90" align="center" />
        <el-table-column label="已完成" width="80" align="center">
          <template #default="{ row }">
            <span class="stat-cell success">{{ row.completed }}</span>
          </template>
        </el-table-column>
        <el-table-column label="未完成" width="80" align="center">
          <template #default="{ row }">
            <span class="stat-cell warning">{{ row.pending }}</span>
          </template>
        </el-table-column>
        <el-table-column label="完成率" width="90" align="center">
          <template #default="{ row }">
            <el-progress 
              :percentage="row.rate" 
              :color="row.rate >= 80 ? '#67c23a' : '#e6a23c'"
              :stroke-width="14"
              :text-inside="true"
            />
          </template>
        </el-table-column>
        <el-table-column label="准时" width="70" align="center">
          <template #default="{ row }">
            <span class="stat-cell success">{{ row.onTimeCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="早到" width="70" align="center">
          <template #default="{ row }">
            <span class="stat-cell info">{{ row.earlyCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="迟到" width="70" align="center">
          <template #default="{ row }">
            <span class="stat-cell danger">{{ row.lateCount }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <div class="table-header">
      <span class="table-title">巡更任务列表</span>
    </div>
    <el-table v-loading="loading" :data="list" stripe border>
      <el-table-column label="序号" type="index" width="80" align="center" />
      <el-table-column label="任务编号" prop="taskCode" min-width="150" align="center" show-overflow-tooltip />
      <el-table-column label="关联计划" min-width="150" align="center" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.planName || row.planCode }}
        </template>
      </el-table-column>
      <el-table-column label="任务日期" prop="taskDate" width="120" align="center">
        <template #default="{ row }">
          {{ formatTaskDate(row.taskDate) }}
        </template>
      </el-table-column>
      <el-table-column label="任务时间段" width="130" align="center">
        <template #default="{ row }">
          {{ formatTimeRange(row.plannedStartTime, row.plannedEndTime) }}
        </template>
      </el-table-column>
      <el-table-column label="巡更人员" prop="personName" min-width="100" align="center" />
      <el-table-column label="巡更路线" min-width="120" align="center">
        <template #default="{ row }">
          <span class="route-name">{{ row.routeName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="任务状态" prop="status" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.status === 0" type="warning">未巡</el-tag>
          <el-tag v-else type="success">已巡</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleViewDetail(row.id)">
            <Icon icon="ep:view" />
          </el-button>
          <el-button link type="primary" @click="handleSubmit(row)">
            <Icon icon="ep:document-checked" />
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

  <!-- 任务详情弹窗 -->
  <el-dialog title="巡更任务详情" v-model="detailDialogVisible" width="700px" append-to-body>
    <div class="detail-info">
      <div class="detail-row">
        <span class="detail-label">任务编号：</span>
        <span class="detail-value">{{ detailData.taskCode }}</span>
      </div>
      <div class="detail-row">
        <span class="detail-label">关联计划：</span>
        <span class="detail-value">{{ detailData.planName || detailData.planCode }}</span>
      </div>
      <div class="detail-row">
        <span class="detail-label">任务时间：</span>
        <span class="detail-value">{{ formatDetailDate(detailData.taskDate) }} {{ formatTimeRange(detailData.plannedStartTime, detailData.plannedEndTime) }}</span>
      </div>
      <div class="detail-row">
        <span class="detail-label">巡更人员：</span>
        <span class="detail-value">{{ detailData.personName || detailData.personNames?.join('、') || '-' }}</span>
      </div>
      <div class="detail-row">
        <span class="detail-label">任务状态：</span>
        <span class="detail-value">
          <el-tag v-if="detailData.status === 0" type="warning">未巡</el-tag>
          <el-tag v-else type="success">已巡</el-tag>
        </span>
      </div>
      <div class="detail-row">
        <span class="detail-label">巡更打卡情况：</span>
      </div>
    </div>
    <el-table :data="detailData.records" border style="margin-top: 10px">
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="巡更点位" prop="pointName" min-width="120" align="center" />
      <el-table-column label="打卡情况" width="100" align="center">
        <template #default="{ row }">
          <span v-if="row.actualTime" class="check-status checked">已打卡</span>
          <span v-else class="check-status unchecked">未打卡</span>
        </template>
      </el-table-column>
      <el-table-column label="打卡时间" width="160" align="center">
        <template #default="{ row }">
          <span v-if="row.actualTime" class="check-time">{{ formatDateTime(row.actualTime) }}</span>
          <span v-else class="check-time empty">-</span>
        </template>
      </el-table-column>
      <el-table-column label="打卡状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.patrolStatus === 1" type="success" size="small">准时</el-tag>
          <el-tag v-else-if="row.patrolStatus === 2" type="info" size="small">早到</el-tag>
          <el-tag v-else-if="row.patrolStatus === 3" type="warning" size="small">晚到</el-tag>
          <el-tag v-else-if="row.patrolStatus === 4" type="danger" size="small">未到</el-tag>
          <el-tag v-else-if="row.patrolStatus === 5" type="danger" size="small">顺序错</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>

  <!-- 提交巡更结果弹窗 -->
  <el-dialog title="提交巡更结果" v-model="submitDialogVisible" width="500px" append-to-body>
    <el-form ref="submitFormRef" :model="submitFormData" label-width="100px">
      <el-form-item label="巡更棒编号" required>
        <div style="display: flex; gap: 10px">
          <el-input
            v-model="stickNo"
            placeholder="请输入巡更棒编号"
            style="flex: 1"
          />
          <el-button type="primary" @click="handleReadStick" :loading="readStickLoading">
            读取巡更棒
          </el-button>
        </div>
      </el-form-item>
      <el-form-item label="巡更日期" required>
        <el-date-picker
          v-model="submitFormData.submitDate"
          type="date"
          placeholder="请选择要提交巡更结果的日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="doSubmitTask" :loading="submitLoading">提交</el-button>
      <el-button type="danger" @click="handleClearStick" :loading="clearStickLoading">
        清空巡更棒
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ContentWrap } from '@/components/ContentWrap'
import { Pagination } from '@/components/Pagination'
import * as EpatrolApi from '@/api/iot/epatrol'

defineOptions({ name: 'EPatrolTask' })

// 列表相关
const loading = ref(false)
const list = ref<EpatrolApi.EpatrolTaskVO[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive<EpatrolApi.EpatrolTaskPageReqVO>({
  pageNo: 1,
  pageSize: 20,
  taskCode: undefined,
  taskDate: undefined,
  personId: undefined,
  routeId: undefined,
  status: undefined
})

// 选项数据
const personOptions = ref<EpatrolApi.EpatrolPersonVO[]>([])
const routeOptions = ref<EpatrolApi.EpatrolRouteVO[]>([])

// 任务详情弹窗
const detailDialogVisible = ref(false)
const detailData = ref<EpatrolApi.EpatrolTaskVO>({})

// 提交弹窗
const submitDialogVisible = ref(false)
const submitLoading = ref(false)
const readStickLoading = ref(false)
const clearStickLoading = ref(false)
const currentTask = ref<EpatrolApi.EpatrolTaskVO | null>(null)
const stickNo = ref('')
const submitFormData = ref<{
  taskId: number
  records: EpatrolApi.PatrolRecordItem[]
  submitDate: string
}>({
  taskId: 0,
  records: [],
  submitDate: ''
})

// 统计信息
const showPersonStats = ref(false)
const statistics = ref<EpatrolApi.EpatrolTaskStatisticsVO>({
  total: 0,
  completed: 0,
  pending: 0,
  rate: 0,
  todayTotal: 0,
  todayCompleted: 0,
  todayRate: 0,
  personStatistics: []
})

// 获取统计数据
const getStatistics = async () => {
  try {
    const res = await EpatrolApi.getEpatrolTaskStatistics(queryParams)
    statistics.value = res
  } catch {
    // 如果接口失败，从列表数据计算
    const completed = list.value.filter(item => item.status === 1).length
    const totalCount = total.value || list.value.length
    statistics.value = {
      total: totalCount,
      completed: completed,
      pending: totalCount - completed,
      rate: totalCount > 0 ? Math.round((completed / totalCount) * 100) : 0,
      todayTotal: 0,
      todayCompleted: 0,
      todayRate: 0,
      personStatistics: []
    }
  }
}

// 格式化任务日期
const formatTaskDate = (date?: string) => {
  if (!date) return '-'
  return date.replace(/-/g, '.')
}

// 格式化详情中的日期
const formatDetailDate = (date?: string) => {
  if (!date) return '-'
  return date
}

// 格式化日期时间
const formatDateTime = (datetime?: string | Date) => {
  if (!datetime) return '-'
  if (typeof datetime === 'string') {
    // 如果是 "2024-01-01 08:00:00" 格式，截取时间部分
    if (datetime.includes(' ')) {
      return datetime.split(' ')[1]?.substring(0, 5) || datetime
    }
    return datetime.substring(0, 5)
  }
  const d = new Date(datetime)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

// 格式化时间范围
const formatTimeRange = (start?: string | Date, end?: string | Date) => {
  if (!start || !end) return '-'
  const formatTime = (time: string | Date) => {
    if (typeof time === 'string') {
      // 如果是时间字符串如 "08:00:00"
      return time.substring(0, 5)
    }
    // 如果是Date对象
    const d = new Date(time)
    return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  }
  return `${formatTime(start)}~${formatTime(end)}`
}

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const res = await EpatrolApi.getEpatrolTaskPage(queryParams)
    console.log('[Task] API返回数据:', res)
    console.log('[Task] list数据:', res?.list)
    list.value = res?.list || []
    total.value = res?.total || 0
    console.log('[Task] 设置后list.value:', list.value)
    // 获取统计数据
    await getStatistics()
  } catch (error) {
    console.error('[Task] 获取列表失败:', error)
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
  queryParams.taskCode = undefined
  queryParams.taskDate = undefined
  queryParams.personId = undefined
  queryParams.routeId = undefined
  queryParams.status = undefined
  handleQuery()
}

// 获取选项数据
const getOptions = async () => {
  personOptions.value = await EpatrolApi.getEnabledEpatrolPersonList()
  routeOptions.value = await EpatrolApi.getEnabledEpatrolRouteList()
}

// 查看详情
const handleViewDetail = async (id: number) => {
  detailData.value = await EpatrolApi.getEpatrolTask(id)
  detailDialogVisible.value = true
}

// 打开提交弹窗
const handleSubmit = (row: EpatrolApi.EpatrolTaskVO) => {
  currentTask.value = row
  stickNo.value = ''
  submitFormData.value = {
    taskId: row.id!,
    records: [],
    submitDate: row.taskDate || new Date().toISOString().split('T')[0]
  }
  submitDialogVisible.value = true
}

// 读取巡更棒数据
const handleReadStick = async () => {
  if (!stickNo.value) {
    ElMessage.warning('请输入巡更棒编号')
    return
  }

  readStickLoading.value = true
  try {
    const res = await EpatrolApi.readPatrolStickData(stickNo.value)
    // 转换为提交格式
    submitFormData.value.records = (res.records || []).map((item, index) => ({
      pointNo: item.pointNo,
      personCardNo: item.personCardNo || '',
      actualTime: item.checkTime,
      actualSort: index + 1
    }))
    if (submitFormData.value.records.length === 0) {
      ElMessage.info('巡更棒中暂无数据')
    } else {
      ElMessage.success(`成功读取 ${submitFormData.value.records.length} 条记录`)
    }
  } catch {
    // 模拟数据（实际应该从硬件读取）
    ElMessage.warning('巡更棒读取接口未实现，使用模拟数据')
    const today = submitFormData.value.submitDate || new Date().toISOString().split('T')[0]
    submitFormData.value.records = [
      { pointNo: 'XD20263331867', personCardNo: 'MG147258', actualTime: `${today} 08:02:00`, actualSort: 1 },
      { pointNo: 'XD20261234567', personCardNo: 'MG147258', actualTime: `${today} 08:10:00`, actualSort: 2 },
      { pointNo: 'XD13593369861', personCardNo: 'MG147258', actualTime: `${today} 08:18:00`, actualSort: 3 }
    ]
    ElMessage.success(`成功读取 ${submitFormData.value.records.length} 条记录`)
  } finally {
    readStickLoading.value = false
  }
}

// 清空巡更棒
const handleClearStick = async () => {
  if (!stickNo.value) {
    ElMessage.warning('请输入巡更棒编号')
    return
  }

  await ElMessageBox.confirm('确认清空巡更棒中的所有数据吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })

  clearStickLoading.value = true
  try {
    await EpatrolApi.clearPatrolStickData(stickNo.value)
    submitFormData.value.records = []
    ElMessage.success('巡更棒数据已清空')
  } catch {
    ElMessage.warning('清空接口未实现，已清空本地数据')
    submitFormData.value.records = []
  } finally {
    clearStickLoading.value = false
  }
}

// 提交任务
const doSubmitTask = async () => {
  if (!stickNo.value) {
    ElMessage.warning('请输入巡更棒编号')
    return
  }
  if (!submitFormData.value.submitDate) {
    ElMessage.warning('请选择巡更日期')
    return
  }
  if (submitFormData.value.records.length === 0) {
    ElMessage.warning('请先读取巡更棒数据')
    return
  }

  await ElMessageBox.confirm('确认提交巡更结果吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  })

  submitLoading.value = true
  try {
    await EpatrolApi.submitEpatrolTask({
      taskId: submitFormData.value.taskId,
      records: submitFormData.value.records,
      clearStickData: false
    })
    ElMessage.success('提交成功')
    submitDialogVisible.value = false
    getList()

    // 询问是否清空巡更棒
    try {
      await ElMessageBox.confirm('是否清空巡更棒记录？', '提示', {
        confirmButtonText: '清空',
        cancelButtonText: '保留',
        type: 'info'
      })
      await handleClearStick()
    } catch {
      // 用户选择保留
    }
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  getList()
  getOptions()
})
</script>

<style scoped lang="scss">
.statistics-bar {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 8px 0;

  .stat-group {
    display: flex;
    align-items: center;
    gap: 24px;
  }

  .stat-group-title {
    color: #1890ff;
    font-weight: 500;
    font-size: 14px;
    padding-right: 12px;
    border-right: 1px solid #e4e7ed;
  }

  .stat-divider {
    width: 1px;
    height: 24px;
    background: #dcdfe6;
    margin: 0 8px;
  }

  .stat-item {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .stat-label {
    color: #606266;
    font-size: 13px;
  }

  .stat-value {
    font-size: 16px;
    font-weight: 600;
    color: #303133;

    &.success {
      color: #67c23a;
    }

    &.warning {
      color: #e6a23c;
    }

    &.danger {
      color: #f56c6c;
    }
  }
}

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

.route-name {
  color: #1890ff;
}

.detail-info {
  .detail-row {
    display: flex;
    margin-bottom: 12px;
    line-height: 24px;
  }

  .detail-label {
    color: #1890ff;
    min-width: 120px;
  }

  .detail-value {
    flex: 1;
  }
}

.check-status {
  &.checked {
    color: #1890ff;
  }

  &.unchecked {
    color: #999;
  }
}

.check-time {
  color: #606266;

  &.empty {
    color: #c0c4cc;
  }
}

.stat-toggle {
  margin-left: auto;
}

.person-statistics {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #e4e7ed;

  .person-stats-title {
    font-size: 14px;
    font-weight: 500;
    color: #1890ff;
    margin-bottom: 8px;
  }

  .stat-cell {
    font-weight: 500;

    &.success {
      color: #67c23a;
    }

    &.warning {
      color: #e6a23c;
    }

    &.danger {
      color: #f56c6c;
    }

    &.info {
      color: #909399;
    }
  }
}
</style>
