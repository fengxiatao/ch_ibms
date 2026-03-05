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
      <el-form-item label="路线名称" prop="routeName">
        <el-input
          v-model="queryParams.routeName"
          placeholder="请输入巡更路线名称"
          clearable
          @keyup.enter="handleQuery"
          style="width: 200px"
        />
      </el-form-item>
      <el-form-item label="巡更点位" prop="pointName">
        <el-input
          v-model="queryParams.pointName"
          placeholder="请输入巡更点位"
          clearable
          @keyup.enter="handleQuery"
          style="width: 200px"
        />
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
      <span class="table-title">巡更路线列表</span>
      <el-button type="primary" @click="openForm('create')">新增路线</el-button>
    </div>
    <el-table v-loading="loading" :data="list" stripe border>
      <el-table-column label="序号" type="index" width="80" align="center" />
      <el-table-column label="路线名称" prop="routeName" min-width="150" align="center" />
      <el-table-column label="巡更点位" min-width="250" align="center">
        <template #default="{ row }">
          <span class="point-names">{{ getPointNamesText(row.points) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="巡更时长" width="120" align="center">
        <template #default="{ row }">
          {{ formatDuration(row.totalDuration) }}
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
  <el-dialog :title="dialogTitle" v-model="dialogVisible" width="800px" append-to-body>
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="路线名称" prop="routeName">
        <el-input v-model="formData.routeName" placeholder="请输入路线名称" style="width: 250px" />
      </el-form-item>
      <el-form-item label="包含点位:" class="points-label">
        <div class="points-transfer">
          <!-- 左侧：巡更点位列表 -->
          <div class="transfer-panel">
            <div class="panel-header">
              <span>巡更点位列表</span>
              <el-button link type="primary" @click="selectAllPoints">选择全部</el-button>
            </div>
            <el-table :data="availablePoints" border max-height="300" size="small">
              <el-table-column label="序号" type="index" width="60" align="center" />
              <el-table-column label="点位名称" align="center">
                <template #default="{ row }">
                  <el-button link type="primary" @click="addPoint(row)">{{ row.pointName }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <!-- 右侧：已选点位列表 -->
          <div class="transfer-panel">
            <div class="panel-header">
              <span>已选点位列表</span>
              <el-button link type="primary" @click="clearAllPoints">清除全部</el-button>
            </div>
            <el-table :data="formData.points" border max-height="300" size="small">
              <el-table-column label="序号" type="index" width="60" align="center" />
              <el-table-column label="点位名称" prop="pointName" align="center" />
              <el-table-column label="操作" width="60" align="center">
                <template #default="{ $index }">
                  <el-button link type="danger" @click="removePoint($index)">
                    <Icon icon="ep:delete" />
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="巡更总时长">
        <div class="duration-input">
          <span>本巡更路线巡更总时长：</span>
          <el-input-number
            v-model="durationHours"
            :min="0"
            :max="23"
            controls-position="right"
            style="width: 100px"
          />
          <span>小时</span>
          <el-input-number
            v-model="durationMinutes"
            :min="0"
            :max="59"
            controls-position="right"
            style="width: 100px"
          />
          <span>分钟</span>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitForm" :loading="submitLoading">确认</el-button>
    </template>
  </el-dialog>

  <!-- 查看详情弹窗 -->
  <el-dialog title="路线详情" v-model="viewDialogVisible" width="700px" append-to-body>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="路线名称">{{ viewData.routeName }}</el-descriptions-item>
      <el-descriptions-item label="巡更时长">{{ formatDuration(viewData.totalDuration) }}</el-descriptions-item>
      <el-descriptions-item label="点位数量">{{ viewData.points?.length || 0 }} 个</el-descriptions-item>
    </el-descriptions>
    <div style="margin-top: 20px">
      <h4>巡更点位列表</h4>
      <el-table :data="viewData.points" border>
        <el-table-column label="顺序" prop="sort" width="80" align="center" />
        <el-table-column label="点位编号" prop="pointNo" align="center" />
        <el-table-column label="点位名称" prop="pointName" align="center" />
        <el-table-column label="点位位置" prop="pointLocation" align="center" />
      </el-table>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ContentWrap } from '@/components/ContentWrap'
import { Pagination } from '@/components/Pagination'
import * as EpatrolApi from '@/api/iot/epatrol'

defineOptions({ name: 'EPatrolRoute' })

// 列表相关
const loading = ref(false)
const list = ref<EpatrolApi.EpatrolRouteVO[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive<EpatrolApi.EpatrolRoutePageReqVO>({
  pageNo: 1,
  pageSize: 20,
  routeName: undefined,
  pointName: undefined
})

// 点位选项
const pointOptions = ref<EpatrolApi.EpatrolPointVO[]>([])

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const submitLoading = ref(false)
const formData = ref<EpatrolApi.EpatrolRouteVO>({
  routeName: '',
  points: [],
  totalDuration: 0
})

// 时长拆分为小时和分钟
const durationHours = ref(0)
const durationMinutes = ref(0)

const formRules = {
  routeName: [{ required: true, message: '请输入路线名称', trigger: 'blur' }]
}

// 查看弹窗
const viewDialogVisible = ref(false)
const viewData = ref<EpatrolApi.EpatrolRouteVO>({
  routeName: '',
  points: []
})

// 可选的点位列表（排除已选的）
const availablePoints = computed(() => {
  const selectedIds = (formData.value.points || []).map(p => p.pointId)
  return pointOptions.value.filter(p => !selectedIds.includes(p.id))
})

// 格式化点位名称
const getPointNamesText = (points?: EpatrolApi.RoutePointItem[]) => {
  if (!points || points.length === 0) return '-'
  return points.map(p => p.pointName).join('、')
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
    const res = await EpatrolApi.getEpatrolRoutePage(queryParams)
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

// 获取点位列表
const getPointOptions = async () => {
  const res = await EpatrolApi.getEnabledEpatrolPointList()
  pointOptions.value = res
}

// 查询
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.routeName = undefined
  queryParams.pointName = undefined
  handleQuery()
}

// 打开表单弹窗
const openForm = async (type: 'create' | 'update', id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增巡更路线' : '修改巡更路线'
  resetForm()
  if (id) {
    const data = await EpatrolApi.getEpatrolRoute(id)
    formData.value = data
    // 拆分时长
    const totalMinutes = data.totalDuration || 0
    durationHours.value = Math.floor(totalMinutes / 60)
    durationMinutes.value = totalMinutes % 60
  }
}

// 重置表单
const resetForm = () => {
  formData.value = {
    routeName: '',
    points: [],
    totalDuration: 0
  }
  durationHours.value = 0
  durationMinutes.value = 0
  formRef.value?.resetFields()
}

// 添加点位
const addPoint = (point: EpatrolApi.EpatrolPointVO) => {
  if (!formData.value.points) {
    formData.value.points = []
  }
  formData.value.points.push({
    pointId: point.id!,
    pointNo: point.pointNo,
    pointName: point.pointName,
    pointLocation: point.pointLocation,
    sort: formData.value.points.length + 1,
    intervalMinutes: 0
  })
}

// 选择全部点位
const selectAllPoints = () => {
  availablePoints.value.forEach(point => {
    addPoint(point)
  })
}

// 清除全部点位
const clearAllPoints = () => {
  formData.value.points = []
}

// 删除点位
const removePoint = (index: number) => {
  formData.value.points!.splice(index, 1)
  // 更新顺序
  formData.value.points!.forEach((p, i) => (p.sort = i + 1))
}

// 提交表单
const submitForm = async () => {
  await formRef.value?.validate()
  if (!formData.value.points || formData.value.points.length === 0) {
    ElMessage.warning('请添加至少一个巡更点位')
    return
  }
  // 计算总时长
  formData.value.totalDuration = durationHours.value * 60 + durationMinutes.value
  
  submitLoading.value = true
  try {
    if (formData.value.id) {
      await EpatrolApi.updateEpatrolRoute(formData.value)
      ElMessage.success('修改成功')
    } else {
      await EpatrolApi.createEpatrolRoute(formData.value)
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
  viewData.value = await EpatrolApi.getEpatrolRoute(id)
  viewDialogVisible.value = true
}

// 删除
const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确认删除该巡更路线吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await EpatrolApi.deleteEpatrolRoute(id)
  ElMessage.success('删除成功')
  getList()
}

onMounted(() => {
  getList()
  getPointOptions()
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

.point-names {
  color: #1890ff;
}

.points-label {
  :deep(.el-form-item__label) {
    font-weight: bold;
  }
}

.points-transfer {
  display: flex;
  gap: 20px;
  width: 100%;
}

.transfer-panel {
  flex: 1;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  overflow: hidden;

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
    background: var(--el-fill-color-light);
    border-bottom: 1px solid var(--el-border-color);
    font-weight: 500;
  }
}

.duration-input {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
