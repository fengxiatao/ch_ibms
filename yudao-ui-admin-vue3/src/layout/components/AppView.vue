<script lang="ts" setup>
import { useTagsViewStore } from '@/store/modules/tagsView'
import { useAppStore } from '@/store/modules/app'
import { Footer } from '@/layout/components/Footer'
import { useViewport } from '@/hooks/web/useViewport'

defineOptions({ name: 'AppView' })

const appStore = useAppStore()

const footer = computed(() => appStore.getFooter)

const tagsViewStore = useTagsViewStore()

const getCaches = computed((): string[] => {
  return tagsViewStore.getCachedViews
})

// 使用视口高度Hook
const { viewportHeight } = useViewport()

// 简化高度计算
const containerHeight = computed(() => {
  return `${viewportHeight.value}px`
})

//region 无感刷新
const routerAlive = ref(true)
// 无感刷新，防止出现页面闪烁白屏
const reload = () => {
  routerAlive.value = false
  nextTick(() => (routerAlive.value = true))
}
// 为组件后代提供刷新方法
provide('reload', reload)
//endregion

// 确保内容区域滚动到顶部
onMounted(() => {
  nextTick(() => {
    const contentElement = document.querySelector('.app-view-content') as HTMLElement
    if (contentElement) {
      contentElement.scrollTop = 0
    }
  })
})
</script>

<template>
  <div class="app-view-container" :style="{ height: containerHeight }">
    <!-- 主要内容区域 -->
    <section
      :class="[
        'app-view-content w-full bg-[var(--app-content-bg-color)] dark:bg-[var(--el-bg-color)]',
        {
          'pb-0': footer
        }
      ]"
    >
      <router-view v-if="routerAlive">
        <template #default="{ Component, route }">
          <keep-alive :include="getCaches">
            <component :is="Component" :key="route.fullPath" />
          </keep-alive>
        </template>
      </router-view>
    </section>
    
    <!-- 页脚区域 -->
    <Footer v-if="footer" class="app-view-footer" />
  </div>
</template>

<style lang="scss" scoped>
.app-view-container {
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 防止外层滚动 */
}

.app-view-content {
  flex: 1; /* 让内容区域占据剩余空间 */
  overflow-y: auto; /* 内容区域独立滚动 */
  overflow-x: hidden;
  padding: var(--app-content-padding);
  padding-top: calc(var(--app-content-padding) + 10px); /* 增加顶部内边距 */
  box-sizing: border-box;
}

.app-view-footer {
  flex-shrink: 0; /* footer固定高度，不收缩 */
}

/* 确保页面内容不会超出计算的高度 */
:deep(.dark-theme-page) {
  height: 100%;
  overflow: hidden;
  
  > * {
    max-height: 100%;
  }
}

/* 优化滚动条样式 */
.app-view-content::-webkit-scrollbar {
  width: 6px;
}

.app-view-content::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.app-view-content::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
  
  &:hover {
    background: rgba(255, 255, 255, 0.5);
  }
}

/* 响应式优化 */
@media (max-height: 600px) {
  .app-view-content {
    /* 小屏幕设备优化 */
    padding: calc(var(--app-content-padding) / 2);
  }
}

@media (orientation: landscape) and (max-height: 500px) {
  .app-view-content {
    /* 横屏小高度优化 */
    padding: 8px;
  }
}
</style>
