<template>
  <el-drawer
    v-model="drawerVisible"
    title=""
    size="600px"
    :with-header="false"
  >
    <div class="drawer-content" v-if="visitor">
      <!-- 头部 -->
      <div class="drawer-header">
        <div class="drawer-header__info">
          <el-avatar :src="visitor.avatar" :size="64">{{ visitor.name?.charAt(0) }}</el-avatar>
          <div>
            <h2 class="drawer-header__name">{{ visitor.name }}</h2>
            <div class="drawer-header__tags">
              <el-tag size="small" :type="getTypeTagType(visitor.type)">
                {{ getTypeLabel(visitor.type) }}
              </el-tag>
              <el-tag size="small" :type="getStatusTagType(visitor.status)">
                {{ getStatusLabel(visitor.status) }}
              </el-tag>
            </div>
          </div>
        </div>
        <el-button :icon="Close" circle @click="drawerVisible = false" />
      </div>

      <!-- 基础信息 -->
      <section class="info-section">
        <h3 class="section-title">
          <Icon icon="ep:postcard" class="text-blue-500" />
          基础信息
        </h3>
        <div class="info-grid">
          <div class="info-item">
            <label>联系电话</label>
            <span>{{ visitor.phone }}</span>
          </div>
          <div class="info-item">
            <label>身份证号</label>
            <span>11010119900101****</span>
          </div>
          <div class="info-item">
            <label>所属单位</label>
            <span>{{ visitor.company || '北京科技有限公司' }}</span>
          </div>
          <div class="info-item">
            <label>车牌号</label>
            <span>京A·88888</span>
          </div>
        </div>
      </section>

      <!-- 本次访问详情 -->
      <section class="info-section">
        <h3 class="section-title">
          <Icon icon="ep:place" class="text-blue-500" />
          本次访问详情
        </h3>
        
        <div class="visit-host">
          <div>
            <p class="visit-host__label">被访人</p>
            <p class="visit-host__name">{{ visitor.host }}</p>
            <p class="visit-host__dept">{{ visitor.hostDept }}</p>
          </div>
          <el-button type="primary" link>查看被访人信息</el-button>
        </div>

        <div class="visit-grid">
          <div class="visit-card">
            <p class="visit-card__label">来访事由</p>
            <p class="visit-card__value">{{ visitor.reason }}</p>
          </div>
          <div class="visit-card">
            <p class="visit-card__label">访问区域</p>
            <div class="visit-card__tags">
              <el-tag size="small" effect="plain">大堂</el-tag>
              <el-tag size="small" effect="plain">{{ visitor.location || '会议室A' }}</el-tag>
            </div>
          </div>
        </div>

        <div class="time-grid">
          <div class="time-card time-card--success">
            <p class="time-card__label">签到时间</p>
            <p class="time-card__value">{{ visitor.time || '09:30' }}</p>
            <p class="time-card__date">2026-02-03</p>
          </div>
          <div class="time-card time-card--gray">
            <p class="time-card__label">签离时间</p>
            <p class="time-card__value">--:--</p>
            <p class="time-card__date">预计 17:00</p>
          </div>
        </div>
      </section>

      <!-- 实时轨迹 -->
      <section class="info-section">
        <h3 class="section-title">
          <Icon icon="ep:guide" class="text-blue-500" />
          实时轨迹
        </h3>
        
        <div class="timeline">
          <div class="timeline-item">
            <div class="timeline-item__icon timeline-item__icon--success">
              <Icon icon="ep:check" />
            </div>
            <div class="timeline-item__content">
              <p class="timeline-item__title">闸机通行 - 1号门</p>
              <p class="timeline-item__desc">09:30:15 · 人脸识别 · 已通过</p>
            </div>
          </div>
          <div class="timeline-item">
            <div class="timeline-item__icon timeline-item__icon--blue">
              <Icon icon="ep:promotion" />
            </div>
            <div class="timeline-item__content">
              <p class="timeline-item__title">电梯使用 - A梯</p>
              <p class="timeline-item__desc">09:32:40 · 前往 15层</p>
            </div>
          </div>
          <div class="timeline-item">
            <div class="timeline-item__icon timeline-item__icon--orange">
              <Icon icon="ep:key" />
            </div>
            <div class="timeline-item__content">
              <p class="timeline-item__title">会议室A门禁</p>
              <p class="timeline-item__desc">09:35:12 · 二维码验证 · 已进入</p>
              <el-tag size="small" type="warning" effect="light">当前位置</el-tag>
            </div>
          </div>
          <div class="timeline__line"></div>
        </div>
      </section>

      <!-- 底部操作 -->
      <div class="drawer-footer">
        <el-button @click="handleContact">
          <Icon icon="ep:phone" class="mr-1" />联系访客
        </el-button>
        <el-button type="warning" @click="handleExtend">
          <Icon icon="ep:clock" class="mr-1" />延长访问
        </el-button>
        <el-button type="danger" @click="handleSignOut">
          <Icon icon="ep:switch-button" class="mr-1" />强制签离
        </el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
import { Icon } from '@iconify/vue'

const props = defineProps<{
  visible: boolean
  visitor: any
}>()

const emit = defineEmits(['update:visible', 'sign-out'])

const drawerVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

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
    business: '商务访客',
    vip: 'VIP访客',
    contractor: '外协人员',
    interview: '面试候选'
  }
  return map[type] || type
}

const getStatusTagType = (status: string) => {
  const map: Record<string, string> = {
    in: 'success',
    completed: 'info',
    pending: 'warning'
  }
  return map[status] || ''
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    in: '在访中',
    completed: '已签离',
    pending: '待到访'
  }
  return map[status] || status
}

const handleContact = () => {
  ElMessage.info('正在拨打访客电话...')
}

const handleExtend = () => {
  ElMessage.success('访问时间已延长1小时')
}

const handleSignOut = async () => {
  await ElMessageBox.confirm(`确认为访客 ${props.visitor?.name} 办理强制签离?`, '确认签离', {
    type: 'warning'
  })
  emit('sign-out', props.visitor)
  drawerVisible.value = false
}
</script>

<style lang="scss" scoped>
.drawer-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24px;
  border-bottom: 1px solid #ebeef5;
  
  &__info {
    display: flex;
    gap: 16px;
  }
  
  &__name {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 8px;
  }
  
  &__tags {
    display: flex;
    gap: 8px;
  }
}

.info-section {
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin: 0 0 16px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 12px;
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
    font-weight: 500;
    color: #303133;
  }
}

.visit-host {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 12px;
  
  &__label {
    font-size: 12px;
    color: #909399;
    margin: 0;
  }
  
  &__name {
    font-size: 14px;
    font-weight: 500;
    margin: 4px 0 0;
  }
  
  &__dept {
    font-size: 12px;
    color: #909399;
    margin: 2px 0 0;
  }
}

.visit-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 12px;
}

.visit-card {
  padding: 12px 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  
  &__label {
    font-size: 12px;
    color: #909399;
    margin: 0 0 8px;
  }
  
  &__value {
    font-size: 14px;
    font-weight: 500;
    margin: 0;
  }
  
  &__tags {
    display: flex;
    gap: 4px;
    flex-wrap: wrap;
  }
}

.time-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.time-card {
  padding: 12px 16px;
  border-radius: 8px;
  
  &--success {
    background: #d1fae5;
    border: 1px solid #a7f3d0;
    
    .time-card__label { color: #059669; }
    .time-card__value { color: #047857; }
    .time-card__date { color: #10b981; }
  }
  
  &--gray {
    background: #f3f4f6;
    border: 1px solid #e5e7eb;
    opacity: 0.7;
    
    .time-card__label { color: #6b7280; }
    .time-card__value { color: #374151; }
    .time-card__date { color: #9ca3af; }
  }
  
  &__label {
    font-size: 12px;
    margin: 0 0 4px;
  }
  
  &__value {
    font-size: 20px;
    font-weight: 700;
    margin: 0;
  }
  
  &__date {
    font-size: 12px;
    margin: 4px 0 0;
  }
}

// 时间线
.timeline {
  position: relative;
  padding-left: 20px;
  
  &__line {
    position: absolute;
    left: 15px;
    top: 24px;
    bottom: 0;
    width: 2px;
    background: #e5e7eb;
  }
}

.timeline-item {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  position: relative;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  &__icon {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    flex-shrink: 0;
    z-index: 1;
    
    &--success {
      background: #d1fae5;
      color: #059669;
    }
    
    &--blue {
      background: #dbeafe;
      color: #2563eb;
    }
    
    &--orange {
      background: #fed7aa;
      color: #ea580c;
    }
  }
  
  &__content {
    flex: 1;
  }
  
  &__title {
    font-size: 14px;
    font-weight: 500;
    margin: 0 0 4px;
  }
  
  &__desc {
    font-size: 12px;
    color: #909399;
    margin: 0 0 4px;
  }
}

.drawer-footer {
  display: flex;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #ebeef5;
  margin-top: auto;
  
  .el-button {
    flex: 1;
  }
}
</style>
