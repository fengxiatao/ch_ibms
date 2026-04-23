<script setup lang="ts">
import FactoryDashboardShell from '../components/FactoryDashboardShell.vue'
import FactoryDashboardHeader from '../components/FactoryDashboardHeader.vue'
import FactoryPanel from '../components/FactoryPanel.vue'

defineOptions({ name: 'FactoryDashboardEnergy' })
</script>

<template>
  <FactoryDashboardShell
    title="智慧工厂驾驶舱"
    subtitle="能耗监控页已预留路由与页面骨架，用于后续与现有能耗模块对接，形成驾驶舱视角的汇总展示。"
    status-text="能耗监控页面骨架已创建"
  >
    <FactoryDashboardHeader
      title="能耗监控"
      subtitle="第二阶段接入能耗总览、分区排行、费用估算与异常分析。"
    />

    <FactoryPanel title="页面状态" subtitle="当前已具备菜单与路由挂载能力" accent="violet">
      <ElEmpty description="能耗监控页面待接入真实业务模块" />
    </FactoryPanel>
  </FactoryDashboardShell>
</template>
