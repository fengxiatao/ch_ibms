<script setup lang="ts">
import { Icon } from '@/components/Icon'

defineOptions({ name: 'CloudDefenseModeTabs' })

defineProps<{
  modelValue: string
  modes: Array<{
    code: string
    name: string
    icon?: string | null
    enabled: boolean
  }>
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()
</script>

<template>
  <div class="cloud-defense-tabs">
    <button
      v-for="item in modes"
      :key="item.code"
      class="cloud-defense-tab"
      :class="{ 'is-active': modelValue === item.code }"
      type="button"
      @click="emit('update:modelValue', item.code)"
    >
      <Icon :icon="item.icon || 'ep:grid'" />
      <span>{{ item.name }}</span>
    </button>
  </div>
</template>

<style scoped lang="scss">
.cloud-defense-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  padding: 8px 0 4px;
}

.cloud-defense-tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 30px;
  padding: 0 12px;
  color: rgba(176, 203, 243, 0.72);
  border: 1px solid rgba(66, 88, 129, 0.22);
  border-radius: 8px;
  background: rgba(8, 18, 38, 0.94);
  font-size: 12px;
  transition:
    color 0.2s ease,
    border-color 0.2s ease,
    background 0.2s ease;
}

.cloud-defense-tab:hover {
  color: #ecf5ff;
  border-color: rgba(84, 157, 255, 0.3);
}

.cloud-defense-tab.is-active {
  color: #ebf5ff;
  border-color: rgba(76, 160, 255, 0.44);
  background: linear-gradient(180deg, rgba(47, 108, 228, 0.94), rgba(30, 77, 178, 0.96));
  box-shadow: 0 6px 16px rgba(17, 72, 190, 0.2);
}
</style>
