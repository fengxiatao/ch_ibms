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
              今日已处理 <strong class="text-success">{{ processedCount }}</strong> 条
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
              v-model="selectedIds" 
              :value="item.id"
              :disabled="item.status !== 'pending'"
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

const props = defineProps<{
  visible: boolean
  pendingCount: number
}>()

const emit = defineEmits(['update:visible', 'refresh'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

// 状态
const loading = ref(false)
const processedCount = ref(5)

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
const currentItem = ref<any>(null)

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

// 模拟数据
const approvalList = ref([
  { id: 6, name: '赵总', phone: '138****9999', avatar: '', host: 'VP', hostDept: '战略部', reason: '战略合作洽谈', type: 'vip', status: 'approved', time: '15:00', company: '上海科技有限公司' },
  { id: 7, name: '孙工程师', phone: '139****1111', avatar: '', host: '运维经理', hostDept: 'IT部', reason: '系统巡检', type: 'contractor', status: 'approved', time: '16:30', company: '北京运维服务有限公司' },
  { id: 8, name: '周candidate', phone: '137****2222', avatar: '', host: '技术总监', hostDept: '研发部', reason: '终面', type: 'interview', status: 'pending', time: '14:30', company: '自由职业' },
  { id: 9, name: '吴顾问', phone: '136****3333', avatar: '', host: '咨询部', hostDept: '外部顾问', reason: '项目咨询', type: 'business', status: 'pending', time: '10:00', company: '深圳管理咨询公司' },
  { id: 10, name: '郑客户', phone: '135****4444', avatar: '', host: '销售经理', hostDept: '销售部', reason: '合同签约', type: 'business', status: 'approved', time: '17:00', company: '广州贸易有限公司' },
  { id: 21, name: '钱工', phone: '139****5555', avatar: '', host: '设施经理', hostDept: '行政部', reason: '空调维修', type: 'contractor', status: 'rejected', time: '09:00', company: '维修服务有限公司' }
])

// 计算
const filteredList = computed(() => {
  let list = approvalList.value
  
  if (filterStatus.value !== 'all') {
    list = list.filter(item => item.status === filterStatus.value)
  }
  
  if (filterTypes.value.length < 4) {
    list = list.filter(item => filterTypes.value.includes(item.type))
  }
  
  return list
})

const getStatusCount = (status: string) => {
  if (status === 'all') return approvalList.value.length
  return approvalList.value.filter(item => item.status === status).length
}

const getItemById = (id: number) => {
  return approvalList.value.find(item => item.id === id)
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
  
  // 更新状态
  selectedIds.value.forEach(id => {
    const item = approvalList.value.find(i => i.id === id)
    if (item) {
      item.status = action === 'approve' ? 'approved' : 'rejected'
    }
  })
  
  const actionText = action === 'approve' ? '通过' : '拒绝'
  ElMessage.success(`批量${actionText}成功，共处理 ${selectedIds.value.length} 条`)
  
  selectedIds.value = []
  selectAll.value = false
  batchModalVisible.value = false
  emit('refresh')
}

const openDetail = (item: any) => {
  currentItem.value = item
  detailModalVisible.value = true
}

const handleApprovalResult = (result: { id: number, action: string }) => {
  const item = approvalList.value.find(i => i.id === result.id)
  if (item) {
    item.status = result.action === 'approve' ? 'approved' : 'rejected'
  }
  emit('refresh')
}

const handleClose = () => {
  selectedIds.value = []
  selectAll.value = false
}

watch(dialogVisible, (val) => {
  if (val) {
    loading.value = true
    setTimeout(() => {
      loading.value = false
    }, 300)
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
