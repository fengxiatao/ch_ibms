<template>
  <div class="visitor-management-entry">
    <div class="entry-tabs">
      <el-tabs v-model="activeName" type="card">
        <el-tab-pane label="首页" name="home">
          <VisitorHome v-if="activeName === 'home'" />
        </el-tab-pane>
        <el-tab-pane label="访客预约" name="appointment">
          <VisitorAppointment v-if="activeName === 'appointment'" />
        </el-tab-pane>
        <el-tab-pane label="访客管理" name="manage">
          <VisitorManage v-if="activeName === 'manage'" />
        </el-tab-pane>
        <el-tab-pane label="来访记录" name="visitRecord">
          <VisitorVisitRecord v-if="activeName === 'visitRecord'" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import VisitorHome from '../Home/index.vue'
import VisitorAppointment from '../Appointment/index.vue'
import VisitorManage from '../Manage/index.vue'
import VisitorVisitRecord from '../VisitRecord/index.vue'

defineOptions({ name: 'VisitorManagement' })

const activeName = ref('home')

const route = useRoute()

function resolveTabFromRoute() {
  const tab = (route.query?.tab as string) || ''
  if (tab === 'home' || tab === 'appointment' || tab === 'manage' || tab === 'visitRecord') {
    activeName.value = tab
    return
  }
  const path = String(route.path || '')
  if (path.endsWith('/home')) activeName.value = 'home'
  else if (path.endsWith('/appointment')) activeName.value = 'appointment'
  else if (path.endsWith('/manage')) activeName.value = 'manage'
  else if (path.endsWith('/visitRecord')) activeName.value = 'visitRecord'
}

watch(
  () => route.fullPath,
  () => resolveTabFromRoute(),
  { immediate: true }
)
</script>

<style lang="scss" scoped>
.visitor-management-entry {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--el-bg-color-page);
  overflow: hidden;
  box-sizing: border-box;
  padding-top: max(
    0px,
    calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px))
  );
}

.entry-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  :deep(.el-tabs__header) {
    margin: 0 20px;
    padding-top: 12px;
  }
  :deep(.el-tabs__content) {
    flex: 1;
    overflow: auto;
    padding: 0;
  }
  :deep(.el-tab-pane) {
    height: 100%;
  }
}
</style>
