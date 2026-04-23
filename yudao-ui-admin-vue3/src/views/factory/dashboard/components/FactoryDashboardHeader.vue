<script setup lang="ts">
defineOptions({ name: 'FactoryDashboardHeader' })

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
  <div class="factory-dashboard-header">
    <div class="factory-dashboard-header__content">
      <div class="factory-dashboard-header__title">{{ title }}</div>
      <div v-if="subtitle" class="factory-dashboard-header__subtitle">{{ subtitle }}</div>
    </div>
    <div class="factory-dashboard-header__actions">
      <slot name="actions"></slot>
    </div>
  </div>
</template>

<style scoped lang="scss">
.factory-dashboard-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  background: linear-gradient(180deg, rgba(9, 25, 42, 0.94), rgba(8, 19, 34, 0.9));
  border: 1px solid rgba(63, 148, 210, 0.24);
  border-radius: 18px;
  box-shadow: inset 0 1px 0 rgba(156, 222, 255, 0.08);
}

.factory-dashboard-header__title {
  font-size: 18px;
  font-weight: 700;
  color: #f3fbff;
}

.factory-dashboard-header__subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: rgba(210, 234, 255, 0.72);
}

.factory-dashboard-header__actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .factory-dashboard-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
