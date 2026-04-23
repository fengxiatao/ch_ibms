<script setup lang="ts">
import FactoryDashboardShell from '../components/FactoryDashboardShell.vue'
import FactoryDashboardHeader from '../components/FactoryDashboardHeader.vue'
import FactoryPanel from '../components/FactoryPanel.vue'

defineOptions({ name: 'FactoryDashboardEnvironment' })
</script>

<template>
  <FactoryDashboardShell
    title="智慧工厂驾驶舱"
    subtitle="环境监控页已预留路由与页面骨架，用于后续接入 VOC、温湿度、粉尘与空气质量数据。"
    status-text="环境监控页面骨架已创建"
  >
    <FactoryDashboardHeader
      title="环境监控"
      subtitle="第二阶段接入环境传感器趋势、阈值告警与空间分布视图。"
    />

    <FactoryPanel title="页面状态" subtitle="当前已具备菜单与路由挂载能力" accent="cyan">
      <ElEmpty description="环境监控页面待接入真实业务模块" />
    </FactoryPanel>
  </FactoryDashboardShell>
</template>
