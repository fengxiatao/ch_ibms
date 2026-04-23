<script setup lang="ts">
defineOptions({ name: 'FactoryDashboardShell' })

withDefaults(
  defineProps<{
    title: string
    subtitle?: string
    statusText?: string
    hideHero?: boolean
  }>(),
  {
    subtitle: '',
    statusText: '',
    hideHero: false
  }
)
</script>

<template>
  <div class="factory-dashboard-shell" :class="{ 'factory-dashboard-shell--hero-hidden': hideHero }">
    <div v-if="!hideHero" class="factory-dashboard-shell__hero">
      <div class="factory-dashboard-shell__hero-content">
        <div class="factory-dashboard-shell__meta">
          <div class="factory-dashboard-shell__eyebrow">SMART FACTORY</div>
          <div class="factory-dashboard-shell__title">{{ title }}</div>
          <div v-if="subtitle" class="factory-dashboard-shell__subtitle">{{ subtitle }}</div>
        </div>
        <div class="factory-dashboard-shell__actions">
          <slot name="hero-actions"></slot>
        </div>
      </div>
      <div v-if="statusText" class="factory-dashboard-shell__status">{{ statusText }}</div>
    </div>

    <div class="factory-dashboard-shell__content">
      <slot></slot>
    </div>
  </div>
</template>

<style scoped lang="scss">
.factory-dashboard-shell {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - var(--page-top-gap, 70px));
  padding-top: calc(var(--page-top-gap, 70px) + 4px);
  padding-bottom: 20px;
  background:
    radial-gradient(circle at top left, rgba(0, 222, 255, 0.2), transparent 34%),
    radial-gradient(circle at top right, rgba(0, 120, 255, 0.16), transparent 28%),
    linear-gradient(180deg, #07111f 0%, #081725 34%, #060d18 100%);
}

.factory-dashboard-shell--hero-hidden {
  min-height: calc(100vh - var(--page-top-gap, 70px));
  padding-top: max(12px, calc(var(--page-top-gap, 70px) - var(--app-content-padding, 20px) + 6px));
  padding-bottom: 12px;
  overflow: visible;
  box-sizing: border-box;
}

.factory-dashboard-shell__hero {
  position: relative;
  margin: 0 12px 18px;
  padding: 24px 28px;
  overflow: hidden;
  background:
    linear-gradient(135deg, rgba(6, 23, 40, 0.96), rgba(8, 41, 70, 0.9)),
    linear-gradient(90deg, rgba(0, 194, 255, 0.1), transparent);
  border: 1px solid rgba(77, 189, 255, 0.24);
  border-radius: 22px;
  box-shadow: 0 18px 48px rgba(0, 0, 0, 0.26);
}

.factory-dashboard-shell__hero::before,
.factory-dashboard-shell__hero::after {
  position: absolute;
  content: '';
  pointer-events: none;
}

.factory-dashboard-shell__hero::before {
  top: -80px;
  right: -20px;
  width: 240px;
  height: 240px;
  background: radial-gradient(circle, rgba(0, 224, 255, 0.18), transparent 70%);
}

.factory-dashboard-shell__hero::after {
  inset: 0;
  background-image:
    linear-gradient(rgba(102, 195, 255, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(102, 195, 255, 0.08) 1px, transparent 1px);
  background-size: 36px 36px;
  opacity: 0.18;
}

.factory-dashboard-shell__hero-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.factory-dashboard-shell__eyebrow {
  margin-bottom: 8px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.28em;
  color: #5be7ff;
}

.factory-dashboard-shell__title {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
  color: #f3fbff;
}

.factory-dashboard-shell__subtitle {
  max-width: 720px;
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.7;
  color: rgba(220, 241, 255, 0.78);
}

.factory-dashboard-shell__actions {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.factory-dashboard-shell__status {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  margin-top: 18px;
  padding: 8px 14px;
  color: #d9f7ff;
  font-size: 13px;
  border: 1px solid rgba(89, 219, 255, 0.18);
  border-radius: 999px;
  background: rgba(7, 20, 35, 0.42);
  backdrop-filter: blur(8px);
}

.factory-dashboard-shell__content {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  gap: 18px;
  padding: 0 12px;
}

@media (max-width: 1024px) {
  .factory-dashboard-shell__hero-content {
    flex-direction: column;
  }

  .factory-dashboard-shell__actions {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>
