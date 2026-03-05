<script setup lang="ts">
import { useAppStore } from '@/store/modules/app'

defineOptions({ name: 'EnergyPageContainer' })

const props = withDefaults(
  defineProps<{
    showBreadcrumb?: boolean
    breadcrumb?: string[]
  }>(),
  {
    showBreadcrumb: false,
    breadcrumb: () => []
  }
)

const appStore = useAppStore()
const isDark = computed(() => appStore.getIsDark)
</script>

<template>
  <div class="energy-page-container" :class="{ 'is-dark': isDark }">
    <div v-if="props.showBreadcrumb && props.breadcrumb.length" class="energy-page-breadcrumb">
      <ElBreadcrumb separator="/">
        <ElBreadcrumbItem v-for="(item, index) in props.breadcrumb" :key="index">
          {{ item }}
        </ElBreadcrumbItem>
      </ElBreadcrumb>
    </div>
    <div class="energy-page-content">
      <slot></slot>
    </div>
  </div>
</template>

<style scoped lang="scss">
.energy-page-container {
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
}

.energy-page-breadcrumb {
  padding: 14px 16px;
  margin-bottom: 14px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
}

.energy-page-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
