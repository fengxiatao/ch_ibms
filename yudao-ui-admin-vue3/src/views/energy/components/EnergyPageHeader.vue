<script setup lang="ts">
defineOptions({ name: 'EnergyPageHeader' })

withDefaults(
  defineProps<{
    title: string
    subtitle?: string
  }>(),
  {
    subtitle: ''
  }
)
</script>

<template>
  <div class="energy-page-header">
    <div class="energy-page-header__left">
      <div class="energy-page-header__title">{{ title }}</div>
      <div v-if="subtitle" class="energy-page-header__subtitle">{{ subtitle }}</div>
    </div>
    <div class="energy-page-header__right">
      <slot name="actions"></slot>
    </div>
  </div>
</template>

<style scoped lang="scss">
.energy-page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
}

.energy-page-header__title {
  font-size: 18px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.energy-page-header__subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.energy-page-header__right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
</style>
