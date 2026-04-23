<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Icon } from '@/components/Icon'

defineOptions({ name: 'FactoryVideoEntry' })

const props = withDefaults(
  defineProps<{
    title?: string
    description?: string
    routePath?: string
    badgeText?: string
  }>(),
  {
    title: '视频融合',
    description: '从告警、设备与空间上下文快速跳转到视频融合页面，作为智慧工厂的视频业务入口。',
    routePath: '/factory/video-fusion',
    badgeText: '轻量入口'
  }
)

const router = useRouter()

const navigateToVideo = () => {
  router.push(props.routePath)
}
</script>

<template>
  <div class="factory-video-entry">
    <div class="factory-video-entry__badge">
      <Icon icon="ep:video-camera-filled" />
      <span>{{ props.badgeText }}</span>
    </div>
    <div class="factory-video-entry__title">{{ props.title }}</div>
    <div class="factory-video-entry__description">{{ props.description }}</div>
    <div class="factory-video-entry__actions">
      <ElButton type="primary" @click="navigateToVideo">
        <Icon icon="ep:video-play" class="mr-6px" />
        进入视频融合
      </ElButton>
      <ElButton plain>
        <Icon icon="ep:bell" class="mr-6px" />
        联动规则待接入
      </ElButton>
    </div>
  </div>
</template>

<style scoped lang="scss">
.factory-video-entry {
  padding: 20px;
  min-height: 100%;
  background:
    linear-gradient(135deg, rgba(7, 33, 57, 0.95), rgba(6, 18, 32, 0.96)),
    radial-gradient(circle at top right, rgba(34, 211, 238, 0.18), transparent 35%);
  border: 1px solid rgba(88, 186, 255, 0.24);
  border-radius: 18px;
}

.factory-video-entry__badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  font-size: 12px;
  color: #8fe8ff;
  border: 1px solid rgba(87, 206, 255, 0.22);
  border-radius: 999px;
  background: rgba(8, 31, 47, 0.7);
}

.factory-video-entry__title {
  margin-top: 16px;
  font-size: 20px;
  font-weight: 700;
  color: #f2fbff;
}

.factory-video-entry__description {
  margin-top: 10px;
  font-size: 13px;
  line-height: 1.8;
  color: rgba(209, 233, 252, 0.74);
}

.factory-video-entry__actions {
  display: flex;
  gap: 10px;
  margin-top: 22px;
  flex-wrap: wrap;
}
</style>
