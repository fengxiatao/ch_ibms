<template>
  <Dialog v-model="dialogVisible" title="访客申请详情" width="750px">
    <div v-loading="loading" class="detail-content">
      <!-- 基本信息 -->
      <el-descriptions title="访客信息" :column="2" border>
        <el-descriptions-item label="申请编号">{{ detail.applyCode }}</el-descriptions-item>
        <el-descriptions-item label="访客姓名">{{ detail.visitorName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detail.visitorPhone }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ detail.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属单位" :span="2">{{ detail.company || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 被访人信息 -->
      <el-descriptions title="被访人信息" :column="2" border class="mt-16">
        <el-descriptions-item label="被访人">{{ detail.visiteeName }}</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ detail.visiteeDeptName || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 来访信息 -->
      <el-descriptions title="来访信息" :column="2" border class="mt-16">
        <el-descriptions-item label="来访事由" :span="2">{{ detail.visitReason }}</el-descriptions-item>
        <el-descriptions-item label="计划来访">{{ formatDateTime(detail.planVisitTime) }}</el-descriptions-item>
        <el-descriptions-item label="计划离开">{{ formatDateTime(detail.planLeaveTime) }}</el-descriptions-item>
        <el-descriptions-item label="实际到访">{{ formatDateTime(detail.actualVisitTime) }}</el-descriptions-item>
        <el-descriptions-item label="实际离开">{{ formatDateTime(detail.actualLeaveTime) }}</el-descriptions-item>
        <el-descriptions-item label="来访状态">
          <el-tag :type="getVisitStatusType(detail.visitStatus)" size="small">
            {{ detail.visitStatusName }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ formatDateTime(detail.applyTime) }}</el-descriptions-item>
      </el-descriptions>

      <!-- 审批信息 -->
      <el-descriptions title="审批信息" :column="2" border class="mt-16">
        <el-descriptions-item label="审批状态">
          <el-tag :type="getApproveStatusType(detail.approveStatus)" size="small">
            {{ detail.approveStatusName }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ formatDateTime(detail.approveTime) }}</el-descriptions-item>
        <el-descriptions-item label="审批人">{{ detail.approverName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批备注">{{ detail.approveRemark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 授权信息 -->
      <el-descriptions 
        v-if="detail.authStatus !== undefined" 
        title="授权信息" 
        :column="2" 
        border 
        class="mt-16"
      >
        <el-descriptions-item label="授权状态">
          <el-tag :type="getAuthStatusType(detail.authStatus)" size="small">
            {{ detail.authStatusName }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="授权类型">
          {{ getAuthTypeName(detail.authType) }}
        </el-descriptions-item>
        <el-descriptions-item label="门禁卡号">{{ detail.cardNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="时间模板">{{ detail.timeTemplateName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="授权开始">{{ formatDateTime(detail.authStartTime) }}</el-descriptions-item>
        <el-descriptions-item label="授权结束">{{ formatDateTime(detail.authEndTime) }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.authType === 2 || detail.authType === 3" label="通行次数">
          {{ detail.usedAccessCount || 0 }} / {{ detail.maxAccessCount || '不限' }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.dailyAccessLimit" label="每日限制">
          {{ detail.dailyUsedCount || 0 }} / {{ detail.dailyAccessLimit }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 授权设备 -->
      <div v-if="detail.authDevices && detail.authDevices.length" class="auth-devices mt-16">
        <div class="section-title">授权设备</div>
        <el-table :data="detail.authDevices" size="small" border>
          <el-table-column label="设备名称" prop="deviceName" />
          <el-table-column label="通道名称" prop="channelName">
            <template #default="{ row }">
              {{ row.channelName || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="下发状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getDispatchStatusType(row.dispatchStatus)" size="small">
                {{ getDispatchStatusName(row.dispatchStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="下发结果" prop="dispatchResult" show-overflow-tooltip />
        </el-table>
      </div>

      <!-- 备注 -->
      <el-descriptions v-if="detail.remark" :column="1" border class="mt-16">
        <el-descriptions-item label="备注">{{ detail.remark }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { 
  VisitorApplyApi, 
  VisitStatusOptions, 
  ApproveStatusOptions,
  VisitorAuthStatusOptions,
  AuthTypeOptions,
  type VisitorApplyVO 
} from '@/api/iot/visitor'

const dialogVisible = ref(false)
const loading = ref(false)
const detail = ref<VisitorApplyVO>({} as VisitorApplyVO)

// 格式化日期
const formatDateTime = (date: any) => {
  if (!date) return '-'
  return formatDate(date, 'YYYY-MM-DD HH:mm')
}

// 状态类型
const getVisitStatusType = (status: number) => {
  const item = VisitStatusOptions.find(o => o.value === status)
  return item?.type || 'info'
}

const getApproveStatusType = (status: number) => {
  const item = ApproveStatusOptions.find(o => o.value === status)
  return item?.type || 'info'
}

const getAuthStatusType = (status: number) => {
  const item = VisitorAuthStatusOptions.find(o => o.value === status)
  return item?.type || 'info'
}

const getAuthTypeName = (type: number) => {
  const item = AuthTypeOptions.find(o => o.value === type)
  return item?.label || '-'
}

const getDispatchStatusType = (status: number) => {
  switch (status) {
    case 0: return 'info'
    case 1: return 'primary'
    case 2: return 'success'
    case 3: return 'danger'
    default: return 'info'
  }
}

const getDispatchStatusName = (status: number) => {
  switch (status) {
    case 0: return '待下发'
    case 1: return '下发中'
    case 2: return '成功'
    case 3: return '失败'
    default: return '未知'
  }
}

// 打开弹窗
const open = async (id: number) => {
  dialogVisible.value = true
  loading.value = true
  try {
    detail.value = await VisitorApplyApi.getApply(id)
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.detail-content {
  .mt-16 {
    margin-top: 16px;
  }

  .section-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--el-text-color-primary);
    margin-bottom: 12px;
    padding-left: 8px;
    border-left: 3px solid var(--el-color-primary);
  }
}
</style>
