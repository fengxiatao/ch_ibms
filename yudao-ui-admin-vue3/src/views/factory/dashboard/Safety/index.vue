<script setup lang="ts">
import FactoryDashboardShell from '../components/FactoryDashboardShell.vue'
import FactoryDashboardHeader from '../components/FactoryDashboardHeader.vue'
import FactoryPanel from '../components/FactoryPanel.vue'

defineOptions({ name: 'FactoryDashboardSafety' })
</script>

<template>
  <FactoryDashboardShell
    title="智慧工厂驾驶舱"
    subtitle="安全监控页已预留路由与页面骨架，用于后续接入门禁、周界、安防告警与视频联动能力。"
    status-text="安全监控页面骨架已创建"
  >
    <FactoryDashboardHeader
      title="安全监控"
      subtitle="第二阶段接入安防告警分级、围界状态、巡更事件与视频联动。"
    />

    <FactoryPanel title="页面状态" subtitle="当前已具备菜单与路由挂载能力" accent="amber">
      <ElEmpty description="安全监控页面待接入真实业务模块" />
    </FactoryPanel>
  </FactoryDashboardShell>
</template>
