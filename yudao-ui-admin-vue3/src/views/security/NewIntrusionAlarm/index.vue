<template>
  <div class="new-intrusion-alarm">
    <div class="module-tabs">
      <div class="tabs-container">
        <el-breadcrumb class="breadcrumb">
          <el-breadcrumb-item>智慧安防</el-breadcrumb-item>
          <el-breadcrumb-item>新入侵报警</el-breadcrumb-item>
          <el-breadcrumb-item>{{ currentModuleTitle }}</el-breadcrumb-item>
        </el-breadcrumb>

        <el-radio-group v-model="currentModule" class="module-switch">
          <el-radio-button label="host">
            <Icon icon="ep:monitor" class="mr-5px" />
            报警主机
          </el-radio-button>
          <el-radio-button label="operation">
            <Icon icon="ep:document" class="mr-5px" />
            操作记录
          </el-radio-button>
          <el-radio-button label="alarm">
            <span class="alarm-label">
              <Icon icon="ep:bell" class="mr-5px" />
              报警记录
              <el-badge v-if="alarmCount > 0" :value="alarmCount" class="alarm-badge" />
            </span>
          </el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div class="module-content">
      <AlarmHostModule
        v-if="currentModule === 'host'"
        @open-permission="openPermissionModal"
        @switch-module="switchModule"
      />
      <OperationLogModule v-else-if="currentModule === 'operation'" />
      <AlarmRecordModule v-else-if="currentModule === 'alarm'" />
    </div>

    <!-- 人员权限管理弹窗 -->
    <PermissionModal ref="permissionModalRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import AlarmHostModule from './components/AlarmHostModule.vue'
import OperationLogModule from './components/OperationLogModule.vue'
import AlarmRecordModule from './components/AlarmRecordModule.vue'
import PermissionModal from './components/PermissionModal.vue'

defineOptions({ name: 'NewIntrusionAlarm' })

// 当前模块
const currentModule = ref<'host' | 'operation' | 'alarm'>('host')

// 报警数量（示例）
const alarmCount = ref(9)

// 当前模块标题
const currentModuleTitle = computed(() => {
  const titles = {
    host: '报警主机管理',
    operation: '操作记录',
    alarm: '报警记录'
  }
  return titles[currentModule.value]
})

// 人员权限弹窗引用
const permissionModalRef = ref()

// 打开人员权限弹窗
const openPermissionModal = () => {
  permissionModalRef.value?.open()
}

// 切换模块
const switchModule = (module: 'host' | 'operation' | 'alarm') => {
  currentModule.value = module
}
</script>

<style lang="scss" scoped>
.new-intrusion-alarm {
  display: flex;
  flex-direction: column;
  height: 100%;
  box-sizing: border-box;
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 0px)));
  overflow: auto;
  background-color: var(--app-content-bg-color);
}

.module-tabs {
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);
  padding: 12px 16px;
  border-radius: 8px;
  flex-shrink: 0;

  .tabs-container {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 12px;
  }

  .breadcrumb {
    display: flex;
    align-items: center;
    font-size: 14px;
  }
}

.module-content {
  flex: 1;
  min-height: 0;
  padding: 16px 0 0 0;
}

.module-switch {
  display: inline-flex;
  flex-wrap: wrap;
  align-items: center;
  padding: 3px;
  border-radius: 8px;
  background: var(--el-fill-color-light);

  :deep(.el-radio-button__inner) {
    display: inline-flex;
    align-items: center;
    border: none;
    border-radius: 6px !important;
  }
}

.alarm-label {
  display: inline-flex;
  align-items: center;
}

.module-tabs {
  :deep(.el-breadcrumb__inner) {
    color: var(--el-text-color-regular);
  }

  :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
    color: var(--el-color-primary);
    font-weight: 600;
  }
}

.alarm-badge {
  margin-left: 6px;

  :deep(.el-badge__content) {
    font-size: 10px;
    height: 16px;
    line-height: 16px;
    padding: 0 5px;
  }
}
</style>
