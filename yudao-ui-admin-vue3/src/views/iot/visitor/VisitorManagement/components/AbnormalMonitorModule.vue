<template>
  <div class="abnormal-monitor">
    <!-- 异常统计概览 -->
    <div class="stats-overview">
      <div class="stat-item stat-item--danger">
        <div class="stat-item__icon">
          <Icon icon="ep:warning-filled" />
        </div>
        <div class="stat-item__content">
          <span class="stat-item__value">{{ stats.critical }}</span>
          <span class="stat-item__label">严重异常</span>
        </div>
      </div>
      <div class="stat-item stat-item--warning">
        <div class="stat-item__icon">
          <Icon icon="ep:bell-filled" />
        </div>
        <div class="stat-item__content">
          <span class="stat-item__value">{{ stats.warning }}</span>
          <span class="stat-item__label">一般异常</span>
        </div>
      </div>
      <div class="stat-item stat-item--info">
        <div class="stat-item__icon">
          <Icon icon="ep:clock" />
        </div>
        <div class="stat-item__content">
          <span class="stat-item__value">{{ stats.timeout }}</span>
          <span class="stat-item__label">超时未离</span>
        </div>
      </div>
      <div class="stat-item stat-item--success">
        <div class="stat-item__icon">
          <Icon icon="ep:circle-check-filled" />
        </div>
        <div class="stat-item__content">
          <span class="stat-item__value">{{ stats.processed }}</span>
          <span class="stat-item__label">今日已处理</span>
        </div>
      </div>
    </div>

    <!-- 异常列表 -->
    <div class="abnormal-list">
      <div class="list-header">
        <h3>异常事件列表</h3>
        <div class="list-header__actions">
          <el-select v-model="filterType" placeholder="异常类型" clearable size="default" style="width: 140px">
            <el-option label="全部类型" value="" />
            <el-option label="超时未离" value="timeout" />
            <el-option label="越权访问" value="unauthorized" />
            <el-option label="黑名单人员" value="blacklist" />
            <el-option label="身份异常" value="identity" />
          </el-select>
          <el-select v-model="filterStatus" placeholder="处理状态" clearable size="default" style="width: 120px">
            <el-option label="全部状态" value="" />
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已处理" value="processed" />
          </el-select>
          <el-button type="primary" @click="handleBatchProcess" :disabled="selectedRows.length === 0">
            批量处理 ({{ selectedRows.length }})
          </el-button>
        </div>
      </div>

      <!-- 异常卡片列表 -->
      <div class="abnormal-cards">
        <div
          v-for="item in filteredList"
          :key="item.id"
          class="abnormal-card"
          :class="[`abnormal-card--${item.level}`, { 'abnormal-card--selected': selectedRows.includes(item.id) }]"
          @click="toggleSelect(item.id)"
        >
          <div class="abnormal-card__checkbox">
            <el-checkbox :model-value="selectedRows.includes(item.id)" @click.stop />
          </div>
          
          <div class="abnormal-card__content">
            <div class="abnormal-card__header">
              <el-tag :type="getLevelTagType(item.level)" size="small" effect="dark">
                {{ getLevelLabel(item.level) }}
              </el-tag>
              <span class="abnormal-card__type">{{ getTypeLabel(item.type) }}</span>
              <span class="abnormal-card__time">{{ item.time }}</span>
            </div>
            
            <div class="abnormal-card__person">
              <el-avatar :size="36">{{ item.visitorName.charAt(0) }}</el-avatar>
              <div>
                <p class="abnormal-card__name">{{ item.visitorName }}</p>
                <p class="abnormal-card__company">{{ item.company }}</p>
              </div>
            </div>
            
            <div class="abnormal-card__desc">
              <Icon icon="ep:warning" class="text-orange-500" />
              <span>{{ item.description }}</span>
            </div>
            
            <div class="abnormal-card__location">
              <Icon icon="ep:location" />
              <span>{{ item.location }}</span>
            </div>
          </div>

          <div class="abnormal-card__actions">
            <el-button 
              v-if="item.status === 'pending'" 
              type="primary" 
              size="small"
              @click.stop="handleProcess(item)"
            >
              立即处理
            </el-button>
            <el-button 
              v-else-if="item.status === 'processing'"
              type="warning"
              size="small"
              @click.stop="handleComplete(item)"
            >
              完成处理
            </el-button>
            <el-tag v-else type="success" size="small">已处理</el-tag>
            
            <el-dropdown @command="(cmd: string) => handleCommand(cmd, item)" trigger="click" @click.stop>
              <el-button :icon="MoreFilled" circle size="small" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="detail">查看详情</el-dropdown-item>
                  <el-dropdown-item command="contact">联系访客</el-dropdown-item>
                  <el-dropdown-item command="track">查看轨迹</el-dropdown-item>
                  <el-dropdown-item command="blacklist" divided>加入黑名单</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="filteredList.length === 0" description="暂无异常记录" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MoreFilled } from '@element-plus/icons-vue'
import { Icon } from '@iconify/vue'

const stats = reactive({
  critical: 2,
  warning: 5,
  timeout: 8,
  processed: 15
})

const filterType = ref('')
const filterStatus = ref('')
const selectedRows = ref<number[]>([])

const abnormalList = ref([
  {
    id: 1,
    level: 'critical',
    type: 'blacklist',
    visitorName: '张某某',
    company: '未知单位',
    description: '黑名单人员尝试入场，已被系统自动拦截',
    location: '1号门岗',
    time: '10:30',
    status: 'pending'
  },
  {
    id: 2,
    level: 'critical',
    type: 'unauthorized',
    visitorName: '李某',
    company: '北京科技公司',
    description: '尝试进入未授权区域：数据中心',
    location: 'B栋3层',
    time: '09:45',
    status: 'processing'
  },
  {
    id: 3,
    level: 'warning',
    type: 'timeout',
    visitorName: '王某某',
    company: '上海贸易公司',
    description: '访问时间已超时2小时，预约至16:00',
    location: 'A栋15层',
    time: '18:00',
    status: 'pending'
  },
  {
    id: 4,
    level: 'warning',
    type: 'identity',
    visitorName: '赵某',
    company: '深圳电子公司',
    description: '人脸识别置信度过低(67%)，需人工确认',
    location: '2号门岗',
    time: '14:20',
    status: 'pending'
  },
  {
    id: 5,
    level: 'info',
    type: 'timeout',
    visitorName: '刘某某',
    company: '广州物流公司',
    description: '访问时间已超时30分钟',
    location: 'C栋1层',
    time: '17:30',
    status: 'processed'
  }
])

const filteredList = computed(() => {
  return abnormalList.value.filter(item => {
    const matchType = !filterType.value || item.type === filterType.value
    const matchStatus = !filterStatus.value || item.status === filterStatus.value
    return matchType && matchStatus
  })
})

const getLevelTagType = (level: string) => {
  const map: Record<string, string> = {
    critical: 'danger',
    warning: 'warning',
    info: 'info'
  }
  return map[level] || ''
}

const getLevelLabel = (level: string) => {
  const map: Record<string, string> = {
    critical: '严重',
    warning: '警告',
    info: '提醒'
  }
  return map[level] || level
}

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    timeout: '超时未离',
    unauthorized: '越权访问',
    blacklist: '黑名单人员',
    identity: '身份异常'
  }
  return map[type] || type
}

const toggleSelect = (id: number) => {
  const index = selectedRows.value.indexOf(id)
  if (index > -1) {
    selectedRows.value.splice(index, 1)
  } else {
    selectedRows.value.push(id)
  }
}

const handleProcess = (item: any) => {
  item.status = 'processing'
  ElMessage.success(`开始处理: ${item.visitorName} 的异常`)
}

const handleComplete = (item: any) => {
  item.status = 'processed'
  stats.processed++
  if (item.level === 'critical') stats.critical--
  else if (item.level === 'warning') stats.warning--
  ElMessage.success('异常已处理完成')
}

const handleBatchProcess = async () => {
  await ElMessageBox.confirm(`确认批量处理选中的 ${selectedRows.value.length} 条异常?`, '批量处理')
  
  abnormalList.value.forEach(item => {
    if (selectedRows.value.includes(item.id)) {
      item.status = 'processed'
    }
  })
  
  stats.processed += selectedRows.value.length
  selectedRows.value = []
  ElMessage.success('批量处理成功')
}

const handleCommand = (command: string, item: any) => {
  switch (command) {
    case 'detail':
      ElMessage.info(`查看 ${item.visitorName} 的详情`)
      break
    case 'contact':
      ElMessage.info(`正在联系 ${item.visitorName}`)
      break
    case 'track':
      ElMessage.info(`查看 ${item.visitorName} 的轨迹`)
      break
    case 'blacklist':
      ElMessageBox.confirm(`确认将 ${item.visitorName} 加入黑名单?`, '加入黑名单', { type: 'warning' })
        .then(() => ElMessage.success('已加入黑名单'))
      break
  }
}
</script>

<style lang="scss" scoped>
.abnormal-monitor {
  padding: 16px;
}

// 统计概览
.stats-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #ebeef5;
  
  &__icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
  }
  
  &__value {
    font-size: 28px;
    font-weight: 700;
    display: block;
  }
  
  &__label {
    font-size: 13px;
    color: #909399;
  }
  
  &--danger {
    .stat-item__icon {
      background: #fee2e2;
      color: #dc2626;
    }
    .stat-item__value {
      color: #dc2626;
    }
  }
  
  &--warning {
    .stat-item__icon {
      background: #fef3c7;
      color: #d97706;
    }
    .stat-item__value {
      color: #d97706;
    }
  }
  
  &--info {
    .stat-item__icon {
      background: #dbeafe;
      color: #2563eb;
    }
    .stat-item__value {
      color: #2563eb;
    }
  }
  
  &--success {
    .stat-item__icon {
      background: #d1fae5;
      color: #059669;
    }
    .stat-item__value {
      color: #059669;
    }
  }
}

// 列表头部
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  h3 {
    font-size: 16px;
    font-weight: 600;
    margin: 0;
  }
  
  &__actions {
    display: flex;
    gap: 12px;
  }
}

// 异常卡片
.abnormal-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.abnormal-card {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    border-color: #409eff;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  }
  
  &--selected {
    border-color: #409eff;
    background: #f0f7ff;
  }
  
  &--critical {
    border-left: 4px solid #dc2626;
  }
  
  &--warning {
    border-left: 4px solid #d97706;
  }
  
  &--info {
    border-left: 4px solid #2563eb;
  }
  
  &__checkbox {
    padding-top: 4px;
  }
  
  &__content {
    flex: 1;
    min-width: 0;
  }
  
  &__header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
  }
  
  &__type {
    font-size: 14px;
    font-weight: 500;
  }
  
  &__time {
    margin-left: auto;
    font-size: 12px;
    color: #909399;
  }
  
  &__person {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
  }
  
  &__name {
    font-weight: 500;
    margin: 0;
  }
  
  &__company {
    font-size: 12px;
    color: #909399;
    margin: 2px 0 0;
  }
  
  &__desc {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: #606266;
    margin-bottom: 8px;
    padding: 8px 12px;
    background: #fffbeb;
    border-radius: 6px;
  }
  
  &__location {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #909399;
  }
  
  &__actions {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 8px;
  }
}
</style>
