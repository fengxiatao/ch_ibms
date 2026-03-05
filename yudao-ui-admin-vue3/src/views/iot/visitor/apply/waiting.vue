<template>
  <div class="visitor-waiting-page">
    <!-- 搜索区域 -->
    <ContentWrap class="visitor-waiting-page__header">
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
        <el-form-item label="访客姓名" prop="visitorName">
          <el-input
            v-model="queryParams.visitorName"
            placeholder="请输入访客姓名"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="来访事由" prop="visitReason">
          <el-select v-model="queryParams.visitReason" placeholder="请选择来访事由" clearable>
            <el-option
              v-for="item in visitReasonOptions"
              :key="item.id"
              :label="item.reasonName"
              :value="item.reasonName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="被访人姓名" prop="visiteeName">
          <el-input
            v-model="queryParams.visiteeName"
            placeholder="请输入被访人姓名"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="来访状态" prop="visitStatus">
          <el-select v-model="queryParams.visitStatus" placeholder="请选择来访状态" clearable>
            <el-option
              v-for="item in VisitStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <!-- 列表区域 -->
    <ContentWrap class="visitor-waiting-page__content">
      <div class="table-header">
        <span class="table-title">待访列表</span>
      </div>

      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column label="访客姓名" prop="visitorName" width="100" align="center" />
        <el-table-column label="访客联系电话" prop="visitorPhone" width="130" align="center" />
        <el-table-column label="被访人姓名" prop="visiteeName" width="100" align="center" />
        <el-table-column label="来访事由" prop="visitReason" width="100" align="center" />
        <el-table-column label="来访状态" width="90" align="center">
          <template #default="{ row }">
            <span :class="getVisitStatusClass(row.visitStatus)">
              {{ row.visitStatusName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="权限下发状态" width="110" align="center">
          <template #default="{ row }">
            <span :class="getAuthStatusClass(row.authStatus)">
              {{ row.authStatusName || '未下发' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="签到时间" width="160" align="center">
          <template #default="{ row }">
            {{ row.actualVisitTime ? formatDate(row.actualVisitTime, 'YYYY/MM/DD HH:mm:ss') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="签离时间" width="160" align="center">
          <template #default="{ row }">
            {{ row.actualLeaveTime ? formatDate(row.actualLeaveTime, 'YYYY/MM/DD HH:mm:ss') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDetail(row)"> 详情 </el-button>
            <!-- 签到签离 -->
            <el-button
              v-if="row.visitStatus === 0"
              link
              type="success"
              size="small"
              @click="handleCheckIn(row)"
            >
              签到
            </el-button>
            <el-button
              v-if="row.visitStatus === 1"
              link
              type="warning"
              size="small"
              @click="handleCheckOut(row)"
            >
              签离
            </el-button>
            <!-- 权限操作 -->
            <el-button
              v-if="row.authStatus !== 2 && row.authStatus !== 3"
              link
              type="primary"
              size="small"
              @click="openDispatch(row)"
              v-hasPermi="['iot:visitor-apply:dispatch']"
            >
              下发权限
            </el-button>
            <el-button
              v-if="row.authStatus === 2"
              link
              type="primary"
              size="small"
              @click="openModifyAuth(row)"
              v-hasPermi="['iot:visitor-apply:dispatch']"
            >
              修改权限
            </el-button>
            <el-button
              v-if="row.authStatus === 2"
              link
              type="danger"
              size="small"
              @click="handleRevoke(row)"
              v-hasPermi="['iot:visitor-apply:dispatch']"
            >
              回收权限
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </ContentWrap>

    <!-- 分页 -->
    <ContentWrap class="visitor-waiting-page__footer">
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>

    <!-- 详情弹窗 -->
    <VisitorApplyDetail ref="detailRef" />

    <!-- 权限下发弹窗 -->
    <VisitorAuthDispatch ref="dispatchRef" @success="getList" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate } from '@/utils/formatTime'
import {
  VisitorApplyApi,
  VisitorReasonApi,
  VisitStatusOptions,
  type VisitorApplyVO
} from '@/api/iot/visitor'
import VisitorApplyDetail from './VisitorApplyDetail.vue'
import VisitorAuthDispatch from './VisitorAuthDispatch.vue'

defineOptions({ name: 'VisitorWaitingList' })

const loading = ref(false)
const list = ref<VisitorApplyVO[]>([])
const total = ref(0)
const visitReasonOptions = ref<any[]>([])

const queryFormRef = ref()
const detailRef = ref()
const dispatchRef = ref()

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  visitorName: undefined,
  visiteeName: undefined,
  visitReason: undefined,
  visitStatus: undefined,
  approveStatus: 1 // 只查询已通过的申请
})

// 获取来访状态样式
const getVisitStatusClass = (status: number) => {
  switch (status) {
    case 0:
      return 'status-waiting'
    case 1:
      return 'status-visiting'
    case 2:
      return 'status-left'
    default:
      return ''
  }
}

// 获取授权状态样式
const getAuthStatusClass = (status: number) => {
  switch (status) {
    case 0:
      return 'status-pending'
    case 1:
      return 'status-dispatching'
    case 2:
      return 'status-dispatched'
    case 3:
      return 'status-revoked'
    case 4:
      return 'status-failed'
    default:
      return 'status-pending'
  }
}

// 获取来访事由列表
const getVisitReasonList = async () => {
  try {
    visitReasonOptions.value = await VisitorReasonApi.getReasonList()
  } catch (e) {
    console.error('获取来访事由失败', e)
  }
}

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const res = await VisitorApplyApi.getApplyPage(queryParams)
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  queryParams.visitStatus = undefined
  queryParams.visitReason = undefined
  handleQuery()
}

// 打开详情
const openDetail = (row: VisitorApplyVO) => {
  detailRef.value?.open(row.id)
}

// 打开权限下发
const openDispatch = (row: VisitorApplyVO) => {
  dispatchRef.value?.open(row.id)
}

// 打开修改权限
const openModifyAuth = (row: VisitorApplyVO) => {
  dispatchRef.value?.open(row.id, true) // 传入编辑模式
}

// 签到
const handleCheckIn = async (row: VisitorApplyVO) => {
  try {
    await ElMessageBox.confirm(`确定访客"${row.visitorName}"已到访签到吗？`, '提示', {
      type: 'info'
    })
    await VisitorApplyApi.checkIn(row.id)
    ElMessage.success('签到成功')
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('签到失败', e)
    }
  }
}

// 签离
const handleCheckOut = async (row: VisitorApplyVO) => {
  try {
    await ElMessageBox.confirm(
      `确定访客"${row.visitorName}"已离访签离吗？\n签离后将自动回收门禁权限。`,
      '提示',
      { type: 'warning' }
    )
    await VisitorApplyApi.checkOut(row.id)
    ElMessage.success('签离成功')
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('签离失败', e)
    }
  }
}

// 回收权限
const handleRevoke = async (row: VisitorApplyVO) => {
  try {
    await ElMessageBox.confirm(`确定回收访客"${row.visitorName}"的门禁权限吗？`, '提示', {
      type: 'warning'
    })
    await VisitorApplyApi.revokeAuth(row.id)
    ElMessage.success('权限回收中')
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('回收权限失败', e)
    }
  }
}

onMounted(() => {
  getVisitReasonList()
  getList()
})
</script>

<style scoped lang="scss">
.visitor-waiting-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  box-sizing: border-box;
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
}

.visitor-waiting-page__header {
  flex-shrink: 0;
}

.visitor-waiting-page__content {
  flex: 1;
  min-height: 0;
  overflow: auto;
  margin-bottom: 0 !important;

  .table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .table-title {
      font-size: 16px;
      font-weight: 500;
      color: #303133;
    }
  }
}

.visitor-waiting-page__footer {
  flex-shrink: 0;
  margin-bottom: 0 !important;
}

// 来访状态样式
.status-waiting {
  color: #909399;
}

.status-visiting {
  color: #67c23a;
}

.status-left {
  color: #909399;
}

// 授权状态样式
.status-pending {
  color: #909399;
}

.status-dispatching {
  color: #409eff;
}

.status-dispatched {
  color: #67c23a;
}

.status-revoked {
  color: #909399;
}

.status-failed {
  color: #f56c6c;
}
</style>
