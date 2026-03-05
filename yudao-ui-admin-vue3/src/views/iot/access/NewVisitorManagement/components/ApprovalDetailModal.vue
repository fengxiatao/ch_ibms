<template>
  <el-dialog
    v-model="dialogVisible"
    title=""
    width="700px"
    :close-on-click-modal="false"
    class="approval-detail-dialog"
  >
    <template #header>
      <div class="detail-header">
        <div class="detail-header__info">
          <el-avatar :src="visitor?.avatar" :size="48">{{ visitor?.name?.charAt(0) }}</el-avatar>
          <div>
            <h3 class="detail-header__name">{{ visitor?.name }}</h3>
            <p class="detail-header__type">
              {{ getTypeLabel(visitor?.type) }} · 预约编号 #{{ String(visitor?.id).padStart(8, '0') }}
            </p>
          </div>
        </div>
        <el-tag 
          :type="getStatusTagType(visitor?.status)"
          size="large"
        >
          {{ getStatusLabel(visitor?.status) }}
        </el-tag>
      </div>
    </template>

    <div class="detail-content" v-if="visitor">
      <!-- 审批流程 -->
      <div class="approval-steps">
        <div class="step step--completed">
          <div class="step__icon"><Icon icon="ep:check" /></div>
          <span class="step__label">提交预约</span>
          <span class="step__time">09:00</span>
        </div>
        <div class="step" :class="{ 'step--active': visitor.status === 'pending', 'step--completed': visitor.status !== 'pending' }">
          <div class="step__icon">{{ visitor.status === 'pending' ? '2' : '' }}<Icon v-if="visitor.status !== 'pending'" icon="ep:check" /></div>
          <span class="step__label">部门审批</span>
          <span class="step__time">{{ visitor.status === 'pending' ? '进行中' : '已完成' }}</span>
        </div>
        <div class="step" :class="{ 'step--completed': visitor.status === 'approved' }">
          <div class="step__icon">3</div>
          <span class="step__label">权限下发</span>
        </div>
        <div class="step">
          <div class="step__icon">4</div>
          <span class="step__label">完成</span>
        </div>
        <div class="step__line"></div>
      </div>

      <!-- 访客信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="info-card__header">
            <Icon icon="ep:user" class="text-blue-500" />
            <span>访客信息</span>
          </div>
        </template>
        <div class="info-grid">
          <div class="info-item">
            <label>联系电话</label>
            <span>{{ visitor.phone }}</span>
          </div>
          <div class="info-item">
            <label>身份证号</label>
            <span>{{ visitor.idCard || '未提供' }}</span>
          </div>
          <div class="info-item">
            <label>所属单位</label>
            <span>{{ visitor.company }}</span>
          </div>
          <div class="info-item">
            <label>车牌号</label>
            <span>{{ visitor.carNo || '无' }}</span>
          </div>
        </div>
      </el-card>

      <!-- 访问详情 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="info-card__header">
            <Icon icon="ep:calendar" class="text-blue-500" />
            <span>访问详情</span>
          </div>
        </template>
        <div class="visit-details">
          <div class="visit-item">
            <span class="visit-item__label">被访人</span>
            <div class="visit-item__value">
              <strong>{{ visitor.host }}</strong>
              <span class="text-gray-400 text-sm ml-2">{{ visitor.hostDept }}</span>
            </div>
          </div>
          <div class="visit-item">
            <span class="visit-item__label">预约时间</span>
            <span class="visit-item__value">2026-02-03 {{ visitor.time }}</span>
          </div>
          <div class="visit-item">
            <span class="visit-item__label">来访事由</span>
            <span class="visit-item__value">{{ visitor.reason }}</span>
          </div>
          <div class="visit-item">
            <span class="visit-item__label">访问区域</span>
            <div class="visit-item__value">
              <el-tag size="small" effect="plain">会议室B</el-tag>
              <el-tag size="small" effect="plain" class="ml-1">15层</el-tag>
            </div>
          </div>
          <div class="visit-item">
            <span class="visit-item__label">备注说明</span>
            <span class="visit-item__value">{{ visitor.remark || '无' }}</span>
          </div>
        </div>
      </el-card>

      <!-- 审批历史（非待审批状态显示） -->
      <el-card v-if="visitor.status !== 'pending'" class="info-card" shadow="never">
        <template #header>
          <div class="info-card__header">
            <Icon icon="ep:clock" class="text-blue-500" />
            <span>审批记录</span>
          </div>
        </template>
        <div class="history-item">
          <div class="history-item__icon" :class="visitor.status === 'approved' ? 'history-item__icon--success' : 'history-item__icon--danger'">
            <Icon :icon="visitor.status === 'approved' ? 'ep:check' : 'ep:close'" />
          </div>
          <div class="history-item__content">
            <div class="history-item__header">
              <strong>{{ visitor.status === 'approved' ? '审批通过' : '审批拒绝' }}</strong>
              <span class="text-gray-400 text-sm">{{ visitor.approvalTime || '2026-02-03 10:30' }}</span>
            </div>
            <p class="history-item__comment">{{ visitor.approvalComment || '同意预约' }}</p>
          </div>
        </div>
      </el-card>

      <!-- 审批操作（待审批状态显示） -->
      <div v-if="visitor.status === 'pending'" class="approval-action">
        <div class="approval-action__input">
          <label>审批意见 <span class="text-red-500">*</span></label>
          <el-input
            v-model="comment"
            type="textarea"
            :rows="3"
            placeholder="请输入审批意见，同意或拒绝的原因..."
          />
          <div class="quick-comments">
            <el-button size="small" plain @click="comment = '同意，请安排接待'">同意，请安排接待</el-button>
            <el-button size="small" plain @click="comment = '时间冲突，请改期'">时间冲突，请改期</el-button>
            <el-button size="small" plain @click="comment = '信息不全，请补充'">信息不全，请补充</el-button>
          </div>
        </div>

        <div class="approval-action__buttons">
          <el-button type="danger" size="large" @click="handleApproval('reject')">
            <Icon icon="ep:circle-close" class="mr-1" />拒绝预约
          </el-button>
          <el-button type="success" size="large" @click="handleApproval('approve')">
            <Icon icon="ep:circle-check" class="mr-1" />通过预约
          </el-button>
        </div>
        
        <el-button type="info" link class="transfer-btn" @click="handleTransfer">
          <Icon icon="ep:share" class="mr-1" />转交他人审批
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Icon } from '@iconify/vue'

const props = defineProps<{
  visible: boolean
  visitor: any
}>()

const emit = defineEmits(['update:visible', 'approve'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const comment = ref('')

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    business: '商务访客',
    vip: 'VIP访客',
    contractor: '外协人员',
    interview: '面试候选'
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

const handleApproval = (action: string) => {
  if (!comment.value.trim()) {
    ElMessage.warning('请输入审批意见')
    return
  }
  
  emit('approve', { id: props.visitor?.id, action })
  
  const actionText = action === 'approve' ? '通过' : '拒绝'
  ElMessage.success(`已${actionText}该预约申请`)
  
  dialogVisible.value = false
  comment.value = ''
}

const handleTransfer = () => {
  ElMessage.info('转交功能开发中...')
}
</script>

<style lang="scss" scoped>
.approval-detail-dialog {
  :deep(.el-dialog__header) {
    padding: 0;
    margin: 0;
  }
  
  :deep(.el-dialog__body) {
    padding: 20px 24px;
  }
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
  
  &__info {
    display: flex;
    align-items: center;
    gap: 16px;
  }
  
  &__name {
    font-size: 18px;
    font-weight: 600;
    margin: 0;
  }
  
  &__type {
    font-size: 14px;
    color: #909399;
    margin: 4px 0 0;
  }
}

// 审批流程
.approval-steps {
  display: flex;
  justify-content: space-between;
  position: relative;
  margin-bottom: 24px;
  padding: 0 20px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  position: relative;
  z-index: 1;
  
  &__icon {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    border: 2px solid #dcdfe6;
    background: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    color: #909399;
  }
  
  &__label {
    font-size: 12px;
    font-weight: 500;
    color: #606266;
  }
  
  &__time {
    font-size: 11px;
    color: #909399;
  }
  
  &--completed &__icon {
    border-color: #67c23a;
    background: #d1fae5;
    color: #67c23a;
  }
  
  &--active &__icon {
    border-color: #409eff;
    background: #ecf5ff;
    color: #409eff;
  }
  
  &__line {
    position: absolute;
    top: 20px;
    left: 60px;
    right: 60px;
    height: 2px;
    background: #dcdfe6;
    z-index: 0;
  }
}

// 信息卡片
.info-card {
  margin-bottom: 16px;
  
  :deep(.el-card__header) {
    padding: 12px 16px;
    background: #f5f7fa;
  }
  
  &__header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
  }
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  label {
    display: block;
    font-size: 12px;
    color: #909399;
    margin-bottom: 4px;
  }
  
  span {
    font-size: 14px;
    color: #303133;
  }
}

.visit-details {
  display: flex;
  flex-direction: column;
}

.visit-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
  
  &:last-child {
    border-bottom: none;
  }
  
  &__label {
    font-size: 14px;
    color: #909399;
  }
  
  &__value {
    font-size: 14px;
    color: #303133;
  }
}

// 审批历史
.history-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  
  &__icon {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    
    &--success {
      background: #d1fae5;
      color: #67c23a;
    }
    
    &--danger {
      background: #fee2e2;
      color: #f56c6c;
    }
  }
  
  &__content {
    flex: 1;
  }
  
  &__header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
  }
  
  &__comment {
    font-size: 14px;
    color: #606266;
    margin: 0;
    padding: 8px;
    background: #fff;
    border-radius: 4px;
  }
}

// 审批操作
.approval-action {
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  
  &__input {
    margin-bottom: 16px;
    
    label {
      display: block;
      font-size: 14px;
      font-weight: 500;
      margin-bottom: 8px;
    }
  }
  
  &__buttons {
    display: flex;
    gap: 12px;
    
    .el-button {
      flex: 1;
    }
  }
}

.quick-comments {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.transfer-btn {
  display: flex;
  justify-content: center;
  width: 100%;
  margin-top: 12px;
}
</style>
