<template>
  <el-dialog
    v-model="dialogVisible"
    title=""
    width="1200px"
    :close-on-click-modal="false"
    class="approval-center-dialog"
    @close="handleClose"
  >
    <template #header>
      <div class="dialog-header">
        <div class="dialog-header__left">
          <div class="dialog-header__icon">
            <Icon icon="ep:document-checked" />
          </div>
          <div>
            <h3 class="dialog-header__title">访客预约审批中心</h3>
            <p class="dialog-header__desc">
              待审批 <strong class="text-warning">{{ pendingCount }}</strong> 条 | 
              今日已处理 <strong class="text-success">{{ processedCountValue }}</strong> 条
            </p>
          </div>
        </div>
        <div class="dialog-header__actions">
          <el-button type="primary" @click="handleBatchApproval">
            <Icon icon="ep:finished" class="mr-1" />批量审批
          </el-button>
        </div>
      </div>
    </template>

    <div class="approval-content">
      <!-- 左侧筛选 -->
      <div class="filter-sidebar">
        <el-card class="filter-card" shadow="never">
          <h4 class="filter-title">审批状态</h4>
          <div class="filter-options">
            <el-button
              v-for="opt in statusOptions"
              :key="opt.value"
              :type="filterStatus === opt.value ? 'primary' : 'default'"
              :plain="filterStatus !== opt.value"
              size="small"
              class="filter-btn"
              @click="filterStatus = opt.value"
            >
              <span>{{ opt.label }}</span>
              <el-tag 
                :type="opt.tagType" 
                size="small" 
                effect="light"
                class="ml-2"
              >
                {{ getStatusCount(opt.value) }}
              </el-tag>
            </el-button>
          </div>
        </el-card>

        <el-card class="filter-card" shadow="never">
          <h4 class="filter-title">访客类型</h4>
          <div class="filter-checkboxes">
            <el-checkbox-group v-model="filterTypes">
              <el-checkbox value="vip" label="VIP访客" />
              <el-checkbox value="business" label="商务访客" />
              <el-checkbox value="interview" label="面试候选" />
              <el-checkbox value="contractor" label="外协人员" />
            </el-checkbox-group>
          </div>
        </el-card>

        <el-card class="filter-card" shadow="never">
          <h4 class="filter-title">预约时间</h4>
          <div class="filter-options">
            <el-button
              v-for="opt in timeOptions"
              :key="opt.value"
              :type="filterTime === opt.value ? 'primary' : 'default'"
              :plain="filterTime !== opt.value"
              size="small"
              class="filter-btn"
              @click="filterTime = opt.value"
            >
              {{ opt.label }}
            </el-button>
          </div>
        </el-card>
      </div>

      <!-- 右侧列表 -->
      <div class="list-content">
        <div class="list-header">
          <el-checkbox v-model="selectAll" @change="handleSelectAll">
            全选
          </el-checkbox>
          <el-select v-model="sortBy" placeholder="排序方式" size="small" style="width: 160px">
            <el-option label="排序：最新提交" value="latest" />
            <el-option label="排序：预约时间先后" value="time" />
            <el-option label="排序：访客类型" value="type" />
          </el-select>
        </div>

        <div class="approval-list" v-loading="loading">
          <div 
            v-for="item in filteredList" 
            :key="item.id" 
            class="approval-item"
            :class="{ 'approval-item--pending': item.status === 'pending' }"
          >
            <el-checkbox
              :model-value="selectedIds.includes(item.id)"
              :disabled="item.status !== 'pending'"
              @update:model-value="(v: boolean) => {
                if (v) selectedIds.push(item.id)
                else selectedIds = selectedIds.filter(id => id !== item.id)
              }"
            />
            
            <el-avatar :src="item.avatar" :size="48">{{ item.name?.charAt(0) }}</el-avatar>
            
            <div class="approval-item__info">
              <div class="approval-item__header">
                <span class="approval-item__name">{{ item.name }}</span>
                <el-tag size="small" :type="getTypeTagType(item.type)">
                  {{ getTypeLabel(item.type) }}
                </el-tag>
                <el-tag 
                  size="small" 
                  :type="getStatusTagType(item.status)"
                >
                  {{ getStatusLabel(item.status) }}
                </el-tag>
              </div>
              <p class="approval-item__reason">{{ item.reason }} · 拜访{{ item.host }}</p>
              <div class="approval-item__meta">
                <span><Icon icon="ep:clock" class="mr-1" />{{ item.time }}</span>
                <span><Icon icon="ep:office-building" class="mr-1" />{{ item.company }}</span>
              </div>
            </div>
            
            <div class="approval-item__actions">
              <el-button 
                v-if="item.status === 'pending'"
                type="primary" 
                size="small"
                @click="openDetail(item)"
              >
                审批
              </el-button>
              <el-button 
                v-else
                size="small"
                @click="openDetail(item)"
              >
                查看
              </el-button>
            </div>
          </div>
          
          <el-empty v-if="filteredList.length === 0" description="暂无审批记录" />
        </div>
      </div>
    </div>

    <!-- 批量审批弹窗 -->
    <el-dialog
      v-model="batchModalVisible"
      title="批量审批"
      width="500px"
      append-to-body
    >
      <div class="batch-content">
        <p class="batch-count">
          已选择 <strong>{{ selectedIds.length }}</strong> 条预约申请
        </p>
        <div class="batch-list">
          <div v-for="id in selectedIds" :key="id" class="batch-item">
            <el-avatar :size="32">{{ getItemById(id)?.name?.charAt(0) }}</el-avatar>
            <div class="batch-item__info">
              <span class="batch-item__name">{{ getItemById(id)?.name }}</span>
              <span class="batch-item__reason">{{ getItemById(id)?.reason }}</span>
            </div>
          </div>
        </div>
        <el-input
          v-model="batchComment"
          type="textarea"
          :rows="3"
          placeholder="请输入统一的审批意见..."
        />
      </div>
      <template #footer>
        <el-button @click="batchModalVisible = false">取消</el-button>
        <el-button type="danger" @click="submitBatchApproval('reject')">批量拒绝</el-button>
        <el-button type="success" @click="submitBatchApproval('approve')">批量通过</el-button>
      </template>
    </el-dialog>

    <!-- 审批详情弹窗 -->
    <ApprovalDetailModal 
      v-model:visible="detailModalVisible"
      :visitor="currentItem"
      @approve="handleApprovalResult"
    />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Icon } from '@iconify/vue'
import ApprovalDetailModal from './ApprovalDetailModal.vue'
import { NewVisitorManagementApi } from '@/api/iot/visitor/newVisitorManagement'
import type { VisitorAppointmentVO } from '@/api/iot/visitor/newVisitorManagement'

const props = defineProps<{
  visible: boolean
  pendingCount: number
  processedCount?: number
}>()

const emit = defineEmits(['update:visible', 'refresh'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

// 今日已处理：优先用父组件传入的 processedCount，否则请求 stats
const processedCountValue = ref(0)
const resolveProcessedCount = () => {
  if (props.processedCount !== undefined && props.processedCount !== null) {
    processedCountValue.value = props.processedCount
    return
  }
  NewVisitorManagementApi.getStats()
    .then((res: any) => {
      const d = res?.data
      processedCountValue.value = d?.todayProcessedCount ?? d?.today_processed_count ?? 0
    })
    .catch(() => {})
}

// 状态
const loading = ref(false)

// 筛选
const filterStatus = ref('all')
const filterTypes = ref(['vip', 'business', 'interview', 'contractor'])
const filterTime = ref('today')
const sortBy = ref('latest')

// 选择
const selectAll = ref(false)
const selectedIds = ref<number[]>([])

// 批量审批
const batchModalVisible = ref(false)
const batchComment = ref('')

// 详情
const detailModalVisible = ref(false)
const currentItem = ref<VisitorAppointmentVO | null>(null)

// 选项
const statusOptions = [
  { value: 'all', label: '全部', tagType: '' },
  { value: 'pending', label: '待我审批', tagType: 'warning' },
  { value: 'approved', label: '已通过', tagType: 'success' },
  { value: 'rejected', label: '已拒绝', tagType: 'danger' }
]

const timeOptions = [
  { value: 'today', label: '今日' },
  { value: 'tomorrow', label: '明日' },
  { value: 'week', label: '本周' }
]

// 列表数据从接口加载
const approvalList = ref<VisitorAppointmentVO[]>([])

function getVisitTimeRange() {
  const now = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  const fmt = (d: Date) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
  const fmtFull = (d: Date, hour: number, min: number) =>
    `${fmt(d)} ${String(hour).padStart(2, '0')}:${String(min).padStart(2, '0')}:00`
  if (filterTime.value === 'today') {
    const start = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0)
    const end = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59)
    return [fmtFull(start, 0, 0), fmtFull(end, 23, 59)]
  }
  if (filterTime.value === 'tomorrow') {
    const t = new Date(now)
    t.setDate(t.getDate() + 1)
    const start = new Date(t.getFullYear(), t.getMonth(), t.getDate(), 0, 0, 0)
    const end = new Date(t.getFullYear(), t.getMonth(), t.getDate(), 23, 59, 59)
    return [fmtFull(start, 0, 0), fmtFull(end, 23, 59)]
  }
  // week: 今起 7 天
  const start = new Date(now)
  const end = new Date(now)
  end.setDate(end.getDate() + 6)
  return [fmtFull(start, 0, 0), fmtFull(end, 23, 59)]
}

function loadList() {
  loading.value = true
  const [visitStart, visitEnd] = getVisitTimeRange()
  const params: any = {
    pageNo: 1,
    pageSize: 500,
    visitTime: [visitStart, visitEnd]
  }
  if (filterStatus.value !== 'all') {
    params.status = filterStatus.value
  }
  NewVisitorManagementApi.getAppointmentPage(params)
    .then((res: any) => {
      const list = res?.data?.list ?? res?.list ?? []
      const raw = Array.isArray(list) ? list : []
      approvalList.value = raw.map((item: any) => ({
        ...item,
        time: item.visitTime ? formatVisitTime(item.visitTime) : '',
        company: item.company || ''
      }))
    })
    .catch(() => {
      approvalList.value = []
    })
    .finally(() => {
      loading.value = false
    })
}

function formatVisitTime(s: string) {
  if (!s) return ''
  try {
    const d = new Date(s)
    const h = d.getHours()
    const m = d.getMinutes()
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
  } catch {
    return String(s).slice(11, 16)
  }
}

// 计算
const filteredList = computed(() => {
  let list = approvalList.value
  if (filterStatus.value !== 'all') {
    list = list.filter((item) => item.status === filterStatus.value)
  }
  if (filterTypes.value.length < 4) {
    list = list.filter((item) => item.type && filterTypes.value.includes(item.type))
  }
  return list
})

const getStatusCount = (status: string) => {
  if (status === 'all') return approvalList.value.length
  return approvalList.value.filter((item) => item.status === status).length
}

const getItemById = (id: number) => {
  return approvalList.value.find((item) => item.id === id)
}

// 方法
const getTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    business: '',
    vip: 'warning',
    contractor: 'info',
    interview: 'success'
  }
  return map[type] || ''
}

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    business: '商务',
    vip: 'VIP',
    contractor: '外协',
    interview: '面试'
  }
  return map[type] || type
}

const getStatusTagType = (status: string) => {
  const map: Record<string, string> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return map[status] || ''
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    pending: '待审批',
    approved: '已通过',
    rejected: '已拒绝'
  }
  return map[status] || status
}

const handleSelectAll = (val: boolean) => {
  if (val) {
    selectedIds.value = approvalList.value
      .filter(item => item.status === 'pending')
      .map(item => item.id)
  } else {
    selectedIds.value = []
  }
}

const handleBatchApproval = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请至少选择一条待审批记录')
    return
  }
  batchComment.value = ''
  batchModalVisible.value = true
}

const submitBatchApproval = (action: string) => {
  if (!batchComment.value.trim()) {
    ElMessage.warning('请输入审批意见')
    return
  }
  const actionVal = action === 'approve' ? 'approve' : 'reject'
  Promise.all(
    selectedIds.value.map((id) =>
      NewVisitorManagementApi.approveAppointment(id, actionVal, batchComment.value.trim())
    )
  )
    .then(() => {
      const actionText = action === 'approve' ? '通过' : '拒绝'
      ElMessage.success(`批量${actionText}成功，共处理 ${selectedIds.value.length} 条`)
      selectedIds.value = []
      selectAll.value = false
      batchModalVisible.value = false
      loadList()
      resolveProcessedCount()
      emit('refresh')
    })
    .catch(() => {
      ElMessage.error('批量审批失败')
    })
}

const openDetail = (item: any) => {
  currentItem.value = item
  detailModalVisible.value = true
}

const handleApprovalResult = (result: { id: number; action: string }) => {
  const item = approvalList.value.find((i) => i.id === result.id)
  if (item) {
    item.status = result.action === 'approve' ? 'approved' : 'rejected'
  }
  loadList()
  resolveProcessedCount()
  emit('refresh')
}

const handleClose = () => {
  selectedIds.value = []
  selectAll.value = false
}

watch(dialogVisible, (val) => {
  if (val) {
    resolveProcessedCount()
    loadList()
  }
})

watch([filterStatus, filterTime], () => {
  if (dialogVisible.value) {
    loadList()
  }
})
</script>

<style lang="scss" scoped>
.approval-center-dialog {
  :deep(.el-dialog__header) {
    padding: 0;
    margin: 0;
  }
  
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
  
  &__left {
    display: flex;
    align-items: center;
    gap: 16px;
  }
  
  &__icon {
    width: 48px;
    height: 48px;
    background: #fef3c7;
    color: #d97706;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
  }
  
  &__title {
    font-size: 18px;
    font-weight: 600;
    margin: 0 0 4px;
  }
  
  &__desc {
    font-size: 14px;
    color: #909399;
    margin: 0;
  }
}

.text-warning { color: #e6a23c; }
.text-success { color: #67c23a; }

.approval-content {
  display: flex;
  height: 600px;
}

.filter-sidebar {
  width: 260px;
  padding: 16px;
  background: #f5f7fa;
  border-right: 1px solid #ebeef5;
  overflow-y: auto;
}

.filter-card {
  margin-bottom: 12px;
  
  :deep(.el-card__body) {
    padding: 16px;
  }
}

.filter-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 12px;
}

.filter-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-btn {
  justify-content: space-between;
  width: 100%;
}

.filter-checkboxes {
  :deep(.el-checkbox-group) {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
}

.list-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid #ebeef5;
}

.approval-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
}

.approval-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  margin-bottom: 12px;
  transition: all 0.2s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }
  
  &--pending {
    background: #fffbeb;
    border-color: #fde68a;
  }
  
  &__info {
    flex: 1;
    min-width: 0;
  }
  
  &__header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 4px;
  }
  
  &__name {
    font-weight: 600;
    color: #303133;
  }
  
  &__reason {
    font-size: 14px;
    color: #606266;
    margin: 0 0 8px;
  }
  
  &__meta {
    display: flex;
    gap: 16px;
    font-size: 12px;
    color: #909399;
  }
}

// 批量审批
.batch-content {
  .batch-count {
    font-size: 14px;
    color: #606266;
    margin-bottom: 16px;
    
    strong {
      color: #409eff;
      font-size: 18px;
    }
  }
}

.batch-list {
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.batch-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 8px;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  &__info {
    display: flex;
    flex-direction: column;
  }
  
  &__name {
    font-weight: 500;
    font-size: 14px;
  }
  
  &__reason {
    font-size: 12px;
    color: #909399;
  }
}
</style>
