<script setup lang="ts">
defineOptions({ name: 'FactoryPanel' })

withDefaults(
  defineProps<{
    title: string
    subtitle?: string
    accent?: 'cyan' | 'emerald' | 'amber' | 'violet'
  }>(),
  {
    subtitle: '',
    accent: 'cyan'
  }
)
</script>

<template>
  <section class="factory-panel" :class="`factory-panel--${accent}`">
    <header class="factory-panel__header">
      <div>
        <div class="factory-panel__title">{{ title }}</div>
        <div v-if="subtitle" class="factory-panel__subtitle">{{ subtitle }}</div>
      </div>
      <div class="factory-panel__extra">
        <slot name="extra"></slot>
      </div>
    </header>
    <div class="factory-panel__body">
      <slot></slot>
    </div>
  </section>
</template>

<style scoped lang="scss">
.factory-panel {
  position: relative;
  overflow: hidden;
  background: linear-gradient(180deg, rgba(8, 18, 32, 0.96), rgba(5, 13, 24, 0.94));
  border: 1px solid rgba(78, 170, 235, 0.22);
  border-radius: 20px;
  box-shadow:
    inset 0 1px 0 rgba(160, 223, 255, 0.08),
    0 12px 34px rgba(0, 0, 0, 0.2);
}

.factory-panel::before {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 1px;
  content: '';
  opacity: 0.8;
}

.factory-panel--cyan::before {
  background: linear-gradient(90deg, rgba(74, 222, 255, 0), rgba(74, 222, 255, 0.9), rgba(74, 222, 255, 0));
}

.factory-panel--emerald::before {
  background: linear-gradient(90deg, rgba(16, 185, 129, 0), rgba(16, 185, 129, 0.9), rgba(16, 185, 129, 0));
}

.factory-panel--amber::before {
  background: linear-gradient(90deg, rgba(245, 158, 11, 0), rgba(245, 158, 11, 0.9), rgba(245, 158, 11, 0));
}

.factory-panel--violet::before {
  background: linear-gradient(90deg, rgba(139, 92, 246, 0), rgba(139, 92, 246, 0.9), rgba(139, 92, 246, 0));
}

.factory-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 20px 14px;
}

.factory-panel__title {
  font-size: 16px;
  font-weight: 700;
  color: #f3fbff;
}

.factory-panel__subtitle {
  margin-top: 6px;
  font-size: 12px;
  color: rgba(206, 229, 246, 0.68);
}

.factory-panel__extra {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.factory-panel__body {
  padding: 0 20px 20px;
}
</style>
