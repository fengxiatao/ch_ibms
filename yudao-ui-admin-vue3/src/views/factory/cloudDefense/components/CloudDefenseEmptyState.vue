<script setup lang="ts">
defineOptions({ name: 'CloudDefenseEmptyState' })

defineProps<{
  title: string
  description: string
}>()
</script>

<template>
  <div class="cloud-defense-empty">
    <div class="cloud-defense-empty__title">{{ title }}</div>
    <div class="cloud-defense-empty__desc">{{ description }}</div>
  </div>
</template>

<style scoped lang="scss">
.cloud-defense-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 460px;
  padding: 24px;
  border: 1px dashed rgba(88, 140, 255, 0.22);
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(8, 23, 49, 0.94), rgba(7, 17, 35, 0.98));
}

.cloud-defense-empty__title {
  font-size: 20px;
  font-weight: 600;
  color: #eef6ff;
}

.cloud-defense-empty__desc {
  max-width: 520px;
  margin-top: 10px;
  font-size: 14px;
  line-height: 1.7;
  text-align: center;
  color: rgba(190, 214, 246, 0.76);
}
</style>
