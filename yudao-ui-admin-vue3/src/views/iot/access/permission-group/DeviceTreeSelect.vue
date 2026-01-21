<template>
  <div class="device-tree-select">
    <el-select
      v-model="selectedIds"
      multiple
      collapse-tags
      collapse-tags-tooltip
      placeholder="选择门禁设备"
      style="width: 100%"
      @change="handleChange"
    >
      <el-option
        v-for="device in deviceList"
        :key="device.id"
        :label="device.deviceName"
        :value="device.id"
      >
        <div class="device-option">
          <Icon icon="ep:monitor" class="mr-8px" />
          <span>{{ device.deviceName }}</span>
          <el-tag size="small" :type="device.state === 1 ? 'success' : 'info'" class="ml-8px">
            {{ device.state === 1 ? '在线' : '离线' }}
          </el-tag>
        </div>
      </el-option>
    </el-select>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { AccessDeviceVO } from '@/api/iot/access'

const props = defineProps<{
  modelValue: number[]
  deviceList: AccessDeviceVO[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: number[]): void
}>()

const selectedIds = ref<number[]>([])

watch(() => props.modelValue, (val) => {
  selectedIds.value = val || []
}, { immediate: true })

const handleChange = (val: number[]) => {
  emit('update:modelValue', val)
}
</script>

<style scoped lang="scss">
.device-tree-select {
  width: 100%;

  :deep(.el-select__wrapper) {
    background: #1e1e2d;
    box-shadow: none;
    border: 1px solid #3d3d4a;
  }

  .device-option {
    display: flex;
    align-items: center;
  }
}
</style>
