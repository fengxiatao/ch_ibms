<template>
  <el-card :body-style="{ padding: '20px' }" shadow="hover">
    <div class="stat-card">
      <div class="stat-icon" :style="{ backgroundColor: iconBgColor }">
        <Icon :icon="icon" :size="32" :color="iconColor" />
      </div>
      <div class="stat-content">
        <div class="stat-title">{{ title }}</div>
        <div class="stat-value">{{ value }}</div>
        <div v-if="subTitle" class="stat-subtitle">{{ subTitle }}</div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { Icon } from '@iconify/vue'

defineProps({
  title: {
    type: String,
    required: true
  },
  value: {
    type: [String, Number],
    required: true
  },
  subTitle: {
    type: String,
    default: ''
  },
  icon: {
    type: String,
    required: true
  },
  iconColor: {
    type: String,
    default: '#fff'
  },
  iconBgColor: {
    type: String,
    default: '#409EFF'
  }
})
</script>

<style scoped lang="scss">
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;

  .stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .stat-content {
    flex: 1;
    min-width: 0;

    .stat-title {
      font-size: 14px;
      color: #909399;
      margin-bottom: 8px;
    }

    .stat-value {
      font-size: 24px;
      font-weight: bold;
      color: #303133;
      margin-bottom: 4px;
    }

    .stat-subtitle {
      font-size: 12px;
      color: #67C23A;
    }
  }
}
</style>












