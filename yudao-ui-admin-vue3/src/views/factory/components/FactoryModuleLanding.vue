<script setup lang="ts">
import FactoryDashboardShell from '@/views/factory/dashboard/components/FactoryDashboardShell.vue'
import FactoryDashboardHeader from '@/views/factory/dashboard/components/FactoryDashboardHeader.vue'
import FactoryPanel from '@/views/factory/dashboard/components/FactoryPanel.vue'

defineOptions({ name: 'FactoryModuleLanding' })

withDefaults(
  defineProps<{
    title: string
    subtitle?: string
    statusText?: string
    accent?: 'cyan' | 'emerald' | 'amber' | 'violet'
    highlights?: string[]
  }>(),
  {
    subtitle: '',
    statusText: '',
    accent: 'cyan',
    highlights: () => []
  }
)
</script>

<template>
  <FactoryDashboardShell :title="title" :subtitle="subtitle" :status-text="statusText">
    <FactoryDashboardHeader :title="title" :subtitle="subtitle" />

    <div class="factory-module-landing__grid">
      <FactoryPanel title="当前状态" subtitle="菜单已按产品信息架构预留正式入口" :accent="accent">
        <ElEmpty description="当前页面不展示硬编码业务数据，待真实接口与业务模块接入后升级为正式页面。" />
      </FactoryPanel>

      <FactoryPanel title="接入原则" subtitle="后续扩展时保持与现有系统能力一致" accent="emerald">
        <ul class="factory-module-landing__list">
          <li class="factory-module-landing__item">继续沿用后端动态菜单与动态路由，不额外维护前端静态菜单。</li>
          <li class="factory-module-landing__item">页面数据统一走真实接口，不使用前端伪造统计值、图表与告警结果。</li>
          <li class="factory-module-landing__item">优先复用当前项目现有业务模块、权限模型与公共组件能力。</li>
        </ul>
      </FactoryPanel>
    </div>

    <FactoryPanel
      v-if="highlights.length"
      title="规划范围"
      subtitle="本入口后续承接的真实业务能力"
      accent="violet"
    >
      <ul class="factory-module-landing__list">
        <li v-for="item in highlights" :key="item" class="factory-module-landing__item">
          {{ item }}
        </li>
      </ul>
    </FactoryPanel>
  </FactoryDashboardShell>
</template>

<style scoped lang="scss">
.factory-module-landing__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.factory-module-landing__list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin: 0;
  padding-left: 18px;
}

.factory-module-landing__item {
  color: rgba(220, 241, 255, 0.82);
  line-height: 1.75;
}

@media (max-width: 960px) {
  .factory-module-landing__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
