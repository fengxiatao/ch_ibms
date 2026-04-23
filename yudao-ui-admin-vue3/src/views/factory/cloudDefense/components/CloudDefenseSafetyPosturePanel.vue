<script setup lang="ts">
defineOptions({ name: 'CloudDefenseSafetyPosturePanel' })

defineProps<{
  score: number
  level: string
  trendText: string
  summary: string
  dimensions: Array<{
    label: string
    value: number
  }>
  predictionTitle: string
  predictionSummary: string
  predictionTags: string[]
}>()

const buildRingStyle = (score: number) => {
  const normalized = Math.max(0, Math.min(100, score))
  return {
    background: `conic-gradient(#2f7cff 0deg ${normalized * 3.6}deg, rgba(63, 103, 180, 0.18) ${normalized * 3.6}deg 360deg)`
  }
}
</script>

<template>
  <section class="cloud-defense-posture">
    <header class="cloud-defense-posture__header">
      <div class="cloud-defense-posture__title">安全态势感知</div>
    </header>

    <div class="cloud-defense-posture__hero">
      <div class="cloud-defense-posture__ring" :style="buildRingStyle(score)">
        <div class="cloud-defense-posture__ring-core">
          <strong>{{ score }}</strong>
        </div>
      </div>

      <div class="cloud-defense-posture__content">
        <div class="cloud-defense-posture__headline">全域安全态势评分</div>
        <div class="cloud-defense-posture__level">
          <span>{{ level }}</span>
          <em>{{ trendText }}</em>
        </div>
        <div class="cloud-defense-posture__summary">{{ summary }}</div>
      </div>
    </div>

    <div class="cloud-defense-posture__dimensions">
      <article v-for="item in dimensions" :key="item.label" class="cloud-defense-posture__dimension">
        <div class="cloud-defense-posture__dimension-label">{{ item.label }}</div>
        <div class="cloud-defense-posture__dimension-value">{{ item.value }}</div>
        <div class="cloud-defense-posture__dimension-bar">
          <span :style="{ width: `${item.value}%` }"></span>
        </div>
      </article>
    </div>

    <div class="cloud-defense-posture__prediction">
      <div class="cloud-defense-posture__prediction-title">{{ predictionTitle }}</div>
      <div class="cloud-defense-posture__prediction-summary">{{ predictionSummary }}</div>
      <div class="cloud-defense-posture__prediction-tags">
        <span v-for="tag in predictionTags" :key="tag">{{ tag }}</span>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.cloud-defense-posture {
  display: flex;
  flex-direction: column;
  gap: 14px;
  height: 100%;
  padding: 16px;
  border: 1px solid rgba(72, 103, 170, 0.32);
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(7, 18, 39, 0.98), rgba(4, 13, 27, 0.98));
  box-shadow: inset 0 1px 0 rgba(104, 150, 255, 0.07);
}

.cloud-defense-posture__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.cloud-defense-posture__title {
  font-size: 14px;
  font-weight: 600;
  color: #f1f6ff;
}

.cloud-defense-posture__hero {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  gap: 18px;
  padding: 18px 16px;
  border: 1px solid rgba(76, 119, 202, 0.2);
  border-radius: 12px;
  background: linear-gradient(90deg, rgba(11, 28, 61, 0.96), rgba(32, 24, 74, 0.72));
}

.cloud-defense-posture__ring {
  position: relative;
  width: 88px;
  height: 88px;
  padding: 7px;
  border-radius: 999px;
}

.cloud-defense-posture__ring-core {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  border-radius: 999px;
  background: radial-gradient(circle at top, rgba(25, 58, 117, 0.96), rgba(6, 17, 35, 1));
  box-shadow: inset 0 0 0 1px rgba(96, 154, 255, 0.1);
}

.cloud-defense-posture__ring-core strong {
  font-size: 32px;
  line-height: 1;
  color: #edf6ff;
}

.cloud-defense-posture__headline {
  font-size: 24px;
  font-weight: 700;
  color: #eef6ff;
}

.cloud-defense-posture__level {
  display: flex;
  gap: 14px;
  margin-top: 8px;
  font-size: 13px;
  color: rgba(210, 226, 255, 0.84);
}

.cloud-defense-posture__level em {
  font-style: normal;
  color: #6ea8ff;
}

.cloud-defense-posture__summary {
  margin-top: 10px;
  font-size: 13px;
  color: rgba(180, 207, 248, 0.72);
}

.cloud-defense-posture__dimensions {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.cloud-defense-posture__dimension {
  padding: 14px 12px 12px;
  border: 1px solid rgba(69, 99, 156, 0.24);
  border-radius: 10px;
  background: rgba(10, 20, 42, 0.92);
}

.cloud-defense-posture__dimension-label {
  font-size: 12px;
  color: rgba(182, 208, 248, 0.68);
}

.cloud-defense-posture__dimension-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
  color: #edf6ff;
}

.cloud-defense-posture__dimension-bar {
  height: 4px;
  margin-top: 12px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(63, 103, 180, 0.2);
}

.cloud-defense-posture__dimension-bar span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #2f7cff, #52b9ff);
}

.cloud-defense-posture__prediction {
  padding: 14px 16px;
  border: 1px solid rgba(50, 151, 120, 0.24);
  border-radius: 12px;
  background: linear-gradient(90deg, rgba(4, 43, 45, 0.92), rgba(7, 34, 62, 0.92));
}

.cloud-defense-posture__prediction-title {
  font-size: 14px;
  font-weight: 600;
  color: #8ef2d4;
}

.cloud-defense-posture__prediction-summary {
  margin-top: 8px;
  font-size: 13px;
  color: rgba(190, 223, 239, 0.82);
}

.cloud-defense-posture__prediction-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 10px;
}

.cloud-defense-posture__prediction-tags span {
  font-size: 12px;
  color: #79f0b9;
}

@media (max-width: 1600px) {
  .cloud-defense-posture__dimensions {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1200px) {
  .cloud-defense-posture__hero {
    grid-template-columns: 1fr;
  }

  .cloud-defense-posture__dimensions {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
