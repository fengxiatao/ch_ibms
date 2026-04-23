<template>
  <ContentWrap
    :body-style="{
      padding: '0',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      overflow: 'hidden',
      backgroundColor: 'var(--el-bg-color)'
    }"
    style="height: 100%; margin-bottom: 0"
  >
    <section class="arming-plan-root">
      <div class="stage-inner">
        <div class="stage-header">
          <div class="stage-title">布防计划</div>
          <div class="stage-actions">
            <el-button type="primary" @click="openCreate">新增计划</el-button>
            <el-button @click="refreshTable">刷新</el-button>
          </div>
        </div>
 
        <div class="stats-row">
          <el-card class="stat-card" shadow="never">
            <div class="stat-value">{{ statTotal }}</div>
            <div class="stat-label">总计划数</div>
          </el-card>
          <el-card class="stat-card" shadow="never">
            <div class="stat-value">{{ statExecuting }}</div>
            <div class="stat-label">执行中</div>
          </el-card>
          <el-card class="stat-card" shadow="never">
            <div class="stat-value">{{ statPending }}</div>
            <div class="stat-label">待执行</div>
          </el-card>
          <el-card class="stat-card" shadow="never">
            <div class="stat-value">{{ statDoneToday }}</div>
            <div class="stat-label">今日完成</div>
          </el-card>
        </div>
 
        <el-card class="table-card" shadow="never">
          <template #header>
            <div class="table-card-header">
              <div class="table-card-title">布防计划管理</div>
              <div class="table-card-actions">
                <el-button type="primary" @click="openCreate">新增计划</el-button>
                <el-button @click="refreshTable">刷新</el-button>
              </div>
            </div>
          </template>
 
          <div class="table-wrap">
            <el-table :data="planList" size="small" border height="520">
              <el-table-column type="index" label="序号" width="60" />
              <el-table-column prop="name" label="计划名称" min-width="160" show-overflow-tooltip />
              <el-table-column prop="strategy" label="策略" width="90">
                <template #default="{ row }">
                  <el-tag :type="strategyTagType(row.strategy)" effect="dark">{{ strategyText(row.strategy) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="计划时间" width="160">
                <template #default="{ row }">{{ row.startTime }} - {{ row.endTime }}</template>
              </el-table-column>
              <el-table-column label="重复规则" min-width="160" show-overflow-tooltip>
                <template #default="{ row }">{{ repeatText(row) }}</template>
              </el-table-column>
              <el-table-column label="布防设备" width="110">
                <template #default="{ row }">{{ row.deviceIds.length }} 个</template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="statusTagType(row.status)">{{ row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="启用状态" width="110">
                <template #default="{ row }">
                  <el-switch v-model="row.enabled" inline-prompt active-text="启用" inactive-text="停用" @change="onToggleEnabled(row)" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="160" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
                  <el-button link type="danger" @click="removePlan(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
 
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="920px" align-center>
          <el-form ref="formRef" :model="formModel" :rules="formRules" label-width="88px">
            <el-form-item label="计划名称" prop="name">
              <el-input v-model="formModel.name" placeholder="例如：夜间居家布防" maxlength="40" show-word-limit />
            </el-form-item>
 
                <el-form-item label="布防策略" prop="strategy">
                  <div class="strategy-row">
                    <div
                      class="strategy-card"
                      :class="{ selected: formModel.strategy === 'home' }"
                      @click="setStrategy('home')"
                    >
                      <div class="strategy-icon">🏠</div>
                      <div class="strategy-name">居家布防</div>
                      <div class="strategy-desc">仅布防周边区域探测器</div>
                    </div>
                    <div
                      class="strategy-card"
                      :class="{ selected: formModel.strategy === 'away' }"
                      @click="setStrategy('away')"
                    >
                      <div class="strategy-icon">🚗</div>
                      <div class="strategy-name">外出布防</div>
                      <div class="strategy-desc">所有探测器全部布防</div>
                    </div>
                    <div
                      class="strategy-card"
                      :class="{ selected: formModel.strategy === 'custom' }"
                      @click="setStrategy('custom')"
                    >
                      <div class="strategy-icon">⚙️</div>
                      <div class="strategy-name">自定义</div>
                      <div class="strategy-desc">手动选择探测器设备</div>
                    </div>
                  </div>
                </el-form-item>
 
                <el-form-item label="布防设备" prop="deviceIds">
                  <div class="device-picker">
                    <el-tree
                      ref="treeRef"
                      :data="deviceTree"
                      node-key="id"
                      show-checkbox
                      default-expand-all
                      :props="{ label: 'label', children: 'children' }"
                      :check-strictly="false"
                      @check="onDeviceCheck"
                    />
                  </div>
                </el-form-item>
 
                <el-form-item label="布防时间" prop="startTime">
                  <el-time-picker v-model="formModel.startTime" value-format="HH:mm" format="HH:mm" style="width: 180px" />
                </el-form-item>
                <el-form-item label="撤防时间" prop="endTime">
                  <el-time-picker v-model="formModel.endTime" value-format="HH:mm" format="HH:mm" style="width: 180px" />
                </el-form-item>
 
                <el-form-item label="重复规则" prop="repeatRule">
                  <el-radio-group v-model="formModel.repeatRule">
                    <el-radio-button label="每天" />
                    <el-radio-button label="每周" />
                    <el-radio-button label="工作日" />
                    <el-radio-button label="周末" />
                    <el-radio-button label="单次" />
                  </el-radio-group>
                </el-form-item>
 
                <el-form-item v-if="formModel.repeatRule === '每周'" label="选择星期" prop="weekDays">
                  <el-checkbox-group v-model="formModel.weekDays">
                    <el-checkbox :label="1">周一</el-checkbox>
                    <el-checkbox :label="2">周二</el-checkbox>
                    <el-checkbox :label="3">周三</el-checkbox>
                    <el-checkbox :label="4">周四</el-checkbox>
                    <el-checkbox :label="5">周五</el-checkbox>
                    <el-checkbox :label="6">周六</el-checkbox>
                    <el-checkbox :label="0">周日</el-checkbox>
                  </el-checkbox-group>
                </el-form-item>
 
                <el-form-item v-if="formModel.repeatRule === '单次'" label="执行日期" prop="singleDate">
                  <el-date-picker
                    v-model="formModel.singleDate"
                    type="date"
                    value-format="YYYY-MM-DD"
                    format="YYYY-MM-DD"
                    style="width: 220px"
                  />
                </el-form-item>
 
                <el-form-item label="备注" prop="remark">
                  <el-input v-model="formModel.remark" type="textarea" :rows="3" placeholder="可选" />
                </el-form-item>
              </el-form>
 
              <template #footer>
                <div class="dialog-footer">
                  <el-button @click="dialogVisible = false">取消</el-button>
                  <el-button type="primary" @click="submitForm">保存</el-button>
                </div>
              </template>
            </el-dialog>
          </div>
    </section>
  </ContentWrap>
</template>
 
<script setup lang="ts">
import { computed, nextTick, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules, TagProps, TreeInstance } from 'element-plus'
 
defineOptions({ name: 'PerimeterIntrusionArmingPlan' })
 
type Strategy = 'home' | 'away' | 'custom'
type RepeatRule = '每天' | '每周' | '工作日' | '周末' | '单次'
type PlanStatus = '执行中' | '待执行' | '已停用'
 
interface ZoneItem {
  id: number
  name: string
  area: string
  type: 'perimeter' | 'interior'
  deviceType: string
  host: string
}
 
interface PlanRow {
  id: number
  name: string
  strategy: Strategy
  startTime: string
  endTime: string
  repeatRule: RepeatRule
  weekDays: number[]
  singleDate: string | null
  deviceIds: number[]
  status: PlanStatus
  enabled: boolean
  remark: string
  createTime: string
}
 
const zoneList: ZoneItem[] = [
  { id: 1, name: '大门', area: '出入口', type: 'perimeter', deviceType: '门磁', host: '长辉-主机01' },
  { id: 2, name: '后门', area: '出入口', type: 'perimeter', deviceType: '门磁', host: '长辉-主机01' },
  { id: 3, name: '窗户-东', area: '东侧外围', type: 'perimeter', deviceType: '红外', host: '长辉-主机01' },
  { id: 4, name: '窗户-西', area: '西侧外围', type: 'perimeter', deviceType: '红外', host: '长辉-主机01' },
  { id: 5, name: '客厅', area: '室内核心', type: 'interior', deviceType: '红外', host: '长辉-主机01' },
  { id: 6, name: '卧室', area: '室内休息区', type: 'interior', deviceType: '红外', host: '长辉-主机01' },
  { id: 7, name: '书房', area: '室内工作区', type: 'interior', deviceType: '红外', host: '长辉-主机01' },
  { id: 8, name: '厨房', area: '室内功能区', type: 'interior', deviceType: '燃气', host: '长辉-主机01' },
  { id: 9, name: '储藏室', area: '附属空间', type: 'interior', deviceType: '红外', host: '长辉-主机01' },
  { id: 10, name: '车库', area: '附属空间', type: 'perimeter', deviceType: '门磁', host: '长辉-主机02' }
]
 
const areaList = computed(() => {
  return Array.from(new Set(zoneList.map((z) => z.area)))
})
 
const deviceTree = computed(() => {
  return areaList.value.map((area) => {
    const children = zoneList
      .filter((z) => z.area === area)
      .map((z) => ({
        id: z.id,
        label: `${z.name}（${z.deviceType} · ${z.host}）`
      }))
    return {
      id: `area-${area}`,
      label: `📍 ${area}（${children.length}）`,
      children
    }
  })
})
 
const planList = reactive<PlanRow[]>([
  {
    id: 101,
    name: '夜间居家布防',
    strategy: 'home',
    startTime: '22:00',
    endTime: '06:00',
    repeatRule: '每天',
    weekDays: [],
    singleDate: null,
    deviceIds: zoneList.filter((z) => z.type === 'perimeter').map((z) => z.id),
    status: '待执行',
    enabled: true,
    remark: '夜间仅布防周边区域',
    createTime: '2026-03-01'
  },
  {
    id: 102,
    name: '工作日外出布防',
    strategy: 'away',
    startTime: '08:30',
    endTime: '18:00',
    repeatRule: '工作日',
    weekDays: [],
    singleDate: null,
    deviceIds: zoneList.map((z) => z.id),
    status: '执行中',
    enabled: true,
    remark: '工作日白天全布防',
    createTime: '2026-03-02'
  },
  {
    id: 103,
    name: '周末重点防护',
    strategy: 'custom',
    startTime: '20:00',
    endTime: '10:00',
    repeatRule: '周末',
    weekDays: [],
    singleDate: null,
    deviceIds: [1, 2, 10],
    status: '待执行',
    enabled: true,
    remark: '周末重点监控出入口',
    createTime: '2026-03-05'
  }
])
 
const statTotal = computed(() => planList.length)
const statExecuting = computed(() => planList.filter((p) => p.enabled && p.status === '执行中').length)
const statPending = computed(() => planList.filter((p) => p.enabled && p.status === '待执行').length)
const statDoneToday = computed(() => 0)
 
const refreshTable = () => {
  ElMessage.success('已刷新')
}
 
const strategyText = (strategy: Strategy) => {
  if (strategy === 'home') return '居家'
  if (strategy === 'away') return '外出'
  return '自定义'
}
 
const strategyTagType = (strategy: Strategy): TagProps['type'] => {
  if (strategy === 'home') return 'success'
  if (strategy === 'away') return 'danger'
  return 'warning'
}
 
const statusTagType = (status: PlanStatus): TagProps['type'] => {
  if (status === '执行中') return 'success'
  if (status === '待执行') return 'warning'
  return 'info'
}
 
const repeatText = (plan: PlanRow) => {
  if (plan.repeatRule === '每周' && plan.weekDays.length) {
    const map: Record<number, string> = { 0: '日', 1: '一', 2: '二', 3: '三', 4: '四', 5: '五', 6: '六' }
    const days = plan.weekDays.map((d) => map[d]).join('')
    return `每周（周${days}）`
  }
  if (plan.repeatRule === '单次' && plan.singleDate) return `单次（${plan.singleDate}）`
  return plan.repeatRule
}
 
const onToggleEnabled = (row: PlanRow) => {
  row.status = row.enabled ? '待执行' : '已停用'
  ElMessage.success(row.enabled ? '已启用' : '已停用')
}
 
const dialogVisible = ref(false)
const dialogTitle = ref('新增布防计划')
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const treeRef = ref<TreeInstance>()
 
const formModel = reactive({
  name: '',
  strategy: 'home' as Strategy,
  deviceIds: [] as number[],
  startTime: '18:00',
  endTime: '08:00',
  repeatRule: '每天' as RepeatRule,
  weekDays: [] as number[],
  singleDate: null as string | null,
  remark: ''
})
 
const formRules: FormRules = {
  name: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  strategy: [{ required: true, message: '请选择布防策略', trigger: 'change' }],
  deviceIds: [
    {
      validator: (_, value, callback) => {
        const ids = Array.isArray(value) ? value : []
        if (ids.length > 0) callback()
        else callback(new Error('请至少选择一个探测器'))
      },
      trigger: 'change'
    }
  ],
  startTime: [{ required: true, message: '请选择布防时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择撤防时间', trigger: 'change' }],
  repeatRule: [{ required: true, message: '请选择重复规则', trigger: 'change' }],
  weekDays: [
    {
      validator: (_, value, callback) => {
        if (formModel.repeatRule !== '每周') return callback()
        const days = Array.isArray(value) ? value : []
        if (days.length > 0) callback()
        else callback(new Error('请至少选择一天'))
      },
      trigger: 'change'
    }
  ],
  singleDate: [
    {
      validator: (_, value, callback) => {
        if (formModel.repeatRule !== '单次') return callback()
        if (value) callback()
        else callback(new Error('请选择执行日期'))
      },
      trigger: 'change'
    }
  ]
}
 
const resetForm = () => {
  formModel.name = ''
  formModel.strategy = 'home'
  formModel.deviceIds = zoneList.filter((z) => z.type === 'perimeter').map((z) => z.id)
  formModel.startTime = '18:00'
  formModel.endTime = '08:00'
  formModel.repeatRule = '每天'
  formModel.weekDays = []
  formModel.singleDate = null
  formModel.remark = ''
}
 
const syncTreeChecked = async () => {
  await nextTick()
  treeRef.value?.setCheckedKeys(formModel.deviceIds, false)
}
 
const openCreate = async () => {
  dialogTitle.value = '新增布防计划'
  editingId.value = null
  resetForm()
  dialogVisible.value = true
  await syncTreeChecked()
  await nextTick()
  formRef.value?.clearValidate()
}
 
const openEdit = async (row: PlanRow) => {
  dialogTitle.value = '编辑布防计划'
  editingId.value = row.id
  formModel.name = row.name
  formModel.strategy = row.strategy
  formModel.deviceIds = [...row.deviceIds]
  formModel.startTime = row.startTime
  formModel.endTime = row.endTime
  formModel.repeatRule = row.repeatRule
  formModel.weekDays = [...row.weekDays]
  formModel.singleDate = row.singleDate
  formModel.remark = row.remark
  dialogVisible.value = true
  await syncTreeChecked()
  await nextTick()
  formRef.value?.clearValidate()
}
 
const setStrategy = async (strategy: Strategy) => {
  formModel.strategy = strategy
  if (strategy === 'home') formModel.deviceIds = zoneList.filter((z) => z.type === 'perimeter').map((z) => z.id)
  else if (strategy === 'away') formModel.deviceIds = zoneList.map((z) => z.id)
  await syncTreeChecked()
}
 
const onDeviceCheck = () => {
  const keys = treeRef.value?.getCheckedKeys(false) ?? []
  formModel.deviceIds = keys.filter((k) => typeof k === 'number') as number[]
}
 
const submitForm = async () => {
  await formRef.value?.validate()
 
  if (!formModel.deviceIds.length) {
    ElMessage.error('请至少选择一个探测器')
    return
  }
 
  if (editingId.value != null) {
    const idx = planList.findIndex((p) => p.id === editingId.value)
    if (idx >= 0) {
      planList[idx] = {
        ...planList[idx],
        name: formModel.name,
        strategy: formModel.strategy,
        deviceIds: [...formModel.deviceIds],
        startTime: formModel.startTime,
        endTime: formModel.endTime,
        repeatRule: formModel.repeatRule,
        weekDays: [...formModel.weekDays],
        singleDate: formModel.singleDate,
        remark: formModel.remark
      }
    }
  } else {
    const id = Math.max(0, ...planList.map((p) => p.id)) + 1
    planList.unshift({
      id,
      name: formModel.name,
      strategy: formModel.strategy,
      startTime: formModel.startTime,
      endTime: formModel.endTime,
      repeatRule: formModel.repeatRule,
      weekDays: [...formModel.weekDays],
      singleDate: formModel.singleDate,
      deviceIds: [...formModel.deviceIds],
      status: '待执行',
      enabled: true,
      remark: formModel.remark,
      createTime: new Date().toISOString().slice(0, 10)
    })
  }
 
  dialogVisible.value = false
  ElMessage.success('已保存')
}
 
const removePlan = async (row: PlanRow) => {
  try {
    await ElMessageBox.confirm(`确认删除计划「${row.name}」？`, '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    const idx = planList.findIndex((p) => p.id === row.id)
    if (idx >= 0) planList.splice(idx, 1)
    ElMessage.success('已删除')
  } catch {
    return
  }
}
</script>
 
<style lang="scss" scoped>
.arming-plan-root {
  height: 100%;
  width: 100%;
  overflow: auto;
  background: var(--el-bg-color);
  padding-top: var(--page-top-gap, 0px);
  box-sizing: border-box;
}
 
.stage-inner {
  height: 100%;
  width: 100%;
  min-height: 100%;
  padding: 18px 18px 16px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
 
.stage-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
 
.stage-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}
 
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
 
.stat-card {
  border-radius: 14px;
  border: 1px solid var(--el-border-color);
  background: var(--el-bg-color-overlay);
}
 
.stat-card :deep(.el-card__body) {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
 
.stat-value {
  font-size: 30px;
  font-weight: 800;
  color: var(--el-text-color-primary);
  line-height: 1;
}
 
.stat-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
 
.table-card {
  flex: 1;
  min-height: 0;
  border-radius: 14px;
  border: 1px solid var(--el-border-color);
  background: var(--el-bg-color-overlay);
}
 
.table-card :deep(.el-card__header) {
  padding: 12px 14px;
  border-bottom: 1px solid var(--el-border-color);
}
 
.table-card :deep(.el-card__body) {
  padding: 0;
  height: calc(100% - 46px);
}
 
.table-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
 
.table-card-title {
  font-weight: 700;
  color: var(--el-text-color-primary);
}
 
.table-wrap {
  height: 100%;
  padding: 12px 12px 14px;
  box-sizing: border-box;
}
 
.table-wrap :deep(.el-table) {
  border-radius: 10px;
}
 
.strategy-row {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
 
.strategy-card {
  border-radius: 12px;
  border: 1px solid var(--el-border-color);
  background: var(--el-fill-color-lighter);
  padding: 12px 12px 10px;
  cursor: pointer;
  user-select: none;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  transition: 0.15s;
}
 
.strategy-card:hover {
  border-color: var(--el-color-primary);
}
 
.strategy-card.selected {
  border-color: var(--el-color-primary);
  background: color-mix(in srgb, var(--el-color-primary) 12%, var(--el-fill-color-lighter));
}
 
.strategy-icon {
  font-size: 30px;
  line-height: 1;
}
 
.strategy-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}
 
.strategy-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
 
.device-picker {
  width: 100%;
  max-height: 260px;
  overflow: auto;
  scrollbar-gutter: stable both-edges;
  border: 1px solid var(--el-border-color);
  border-radius: 10px;
  padding: 8px 10px;
  box-sizing: border-box;
  background: var(--el-fill-color-lighter);
}
 
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
