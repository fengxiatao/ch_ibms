<script setup lang="ts">
defineOptions({ name: 'CloudDefenseInsightPanel' })

defineProps<{
  title: string
  subtitle: string
  heroValue: string
  heroLabel: string
  heroDescription: string
  stats: Array<{
    label: string
    value: string
    accent?: 'blue' | 'cyan' | 'emerald' | 'orange'
  }>
  highlights: Array<{
    title: string
    description: string
    status: string
  }>
}>()
</script>

<template>
  <section class="cloud-defense-insight">
    <div class="cloud-defense-insight__hero">
      <div>
        <div class="cloud-defense-insight__title">{{ title }}</div>
        <div class="cloud-defense-insight__subtitle">{{ subtitle }}</div>
      </div>
      <div class="cloud-defense-insight__hero-value">
        <strong>{{ heroValue }}</strong>
        <span>{{ heroLabel }}</span>
      </div>
    </div>

    <div class="cloud-defense-insight__desc">{{ heroDescription }}</div>

    <div class="cloud-defense-insight__stats">
      <article
        v-for="item in stats"
        :key="item.label"
        class="cloud-defense-insight__stat"
        :class="item.accent ? `is-${item.accent}` : ''"
      >
        <div class="cloud-defense-insight__stat-label">{{ item.label }}</div>
        <div class="cloud-defense-insight__stat-value">{{ item.value }}</div>
      </article>
    </div>

    <div class="cloud-defense-insight__list">
      <article
        v-for="item in highlights"
        :key="`${item.title}-${item.status}`"
        class="cloud-defense-insight__item"
      >
        <div>
          <div class="cloud-defense-insight__item-title">{{ item.title }}</div>
          <div class="cloud-defense-insight__item-desc">{{ item.description }}</div>
        </div>
        <span class="cloud-defense-insight__item-status">{{ item.status }}</span>
      </article>
    </div>
  </section>
</template>

<style scoped lang="scss">
.cloud-defense-insight {
  display: flex;
  flex-direction: column;
  gap: 14px;
  height: 100%;
  padding: 16px;
  border: 1px solid rgba(72, 103, 170, 0.32);
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(7, 18, 39, 0.98), rgba(4, 13, 27, 0.98));
}

.cloud-defense-insight__hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding: 16px 18px;
  border: 1px solid rgba(73, 112, 195, 0.24);
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(10, 30, 62, 0.96), rgba(16, 20, 46, 0.88));
}

.cloud-defense-insight__title {
  font-size: 18px;
  font-weight: 700;
  color: #eef6ff;
}

.cloud-defense-insight__subtitle,
.cloud-defense-insight__desc,
.cloud-defense-insight__stat-label,
.cloud-defense-insight__item-desc {
  font-size: 13px;
  color: rgba(183, 210, 248, 0.74);
}

.cloud-defense-insight__subtitle {
  margin-top: 8px;
}

.cloud-defense-insight__hero-value {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.cloud-defense-insight__hero-value strong {
  font-size: 36px;
  line-height: 1;
  color: #eff7ff;
}

.cloud-defense-insight__hero-value span,
.cloud-defense-insight__item-status {
  color: #6ea8ff;
  font-size: 12px;
}

.cloud-defense-insight__stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.cloud-defense-insight__stat {
  padding: 14px;
  border: 1px solid rgba(73, 112, 195, 0.18);
  border-radius: 12px;
  background: rgba(8, 18, 39, 0.9);
}

.cloud-defense-insight__stat-value {
  margin-top: 10px;
  font-size: 26px;
  font-weight: 700;
  color: #eff7ff;
}

.cloud-defense-insight__stat.is-cyan {
  box-shadow: inset 0 0 0 1px rgba(55, 219, 220, 0.08);
}

.cloud-defense-insight__stat.is-emerald {
  box-shadow: inset 0 0 0 1px rgba(74, 224, 151, 0.08);
}

.cloud-defense-insight__stat.is-orange {
  box-shadow: inset 0 0 0 1px rgba(255, 141, 85, 0.08);
}

.cloud-defense-insight__list {
  display: grid;
  gap: 10px;
}

.cloud-defense-insight__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 14px 16px;
  border: 1px solid rgba(73, 112, 195, 0.16);
  border-radius: 12px;
  background: rgba(6, 14, 30, 0.82);
}

.cloud-defense-insight__item-title {
  font-size: 14px;
  font-weight: 600;
  color: #eef6ff;
}

@media (max-width: 1400px) {
  .cloud-defense-insight__stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .cloud-defense-insight__hero {
    flex-direction: column;
  }
}
</style>
