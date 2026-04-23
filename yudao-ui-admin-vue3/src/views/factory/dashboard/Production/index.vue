<script setup lang="ts">
import FactoryDashboardShell from '../components/FactoryDashboardShell.vue'
import FactoryDashboardHeader from '../components/FactoryDashboardHeader.vue'
import FactoryPanel from '../components/FactoryPanel.vue'

defineOptions({ name: 'FactoryDashboardProduction' })
</script>

<template>
  <FactoryDashboardShell
    title="智慧工厂驾驶舱"
    subtitle="生产监控页已预留路由与页面骨架，用于后续接入产线节拍、工单达成率与设备状态数据。"
    status-text="生产监控页面骨架已创建"
  >
    <FactoryDashboardHeader
      title="生产监控"
      subtitle="第二阶段接入产线节拍、工位状态、工单看板与生产异常联动。"
    />

    <FactoryPanel title="页面状态" subtitle="当前已具备菜单与路由挂载能力" accent="emerald">
      <ElEmpty description="生产监控页面待接入真实业务模块" />
    </FactoryPanel>
  </FactoryDashboardShell>
</template>
