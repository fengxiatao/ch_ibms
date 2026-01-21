<template>
  <div style="padding: 20px;">
    <h2>报警主机权限测试</h2>
    
    <el-card style="margin-bottom: 20px;">
      <template #header>
        <span>权限检测结果</span>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <h4>当前用户权限列表：</h4>
          <ul>
            <li v-for="permission in userPermissions" :key="permission">
              {{ permission }}
            </li>
          </ul>
        </el-col>
        
        <el-col :span="12">
          <h4>报警主机权限检测：</h4>
          <div style="margin-bottom: 10px;">
            <el-tag :type="hasCreatePermission ? 'success' : 'danger'">
              iot:alarm-host:create {{ hasCreatePermission ? '✓' : '✗' }}
            </el-tag>
          </div>
          <div style="margin-bottom: 10px;">
            <el-tag :type="hasUpdatePermission ? 'success' : 'danger'">
              iot:alarm-host:update {{ hasUpdatePermission ? '✓' : '✗' }}
            </el-tag>
          </div>
          <div style="margin-bottom: 10px;">
            <el-tag :type="hasDeletePermission ? 'success' : 'danger'">
              iot:alarm-host:delete {{ hasDeletePermission ? '✓' : '✗' }}
            </el-tag>
          </div>
          <div style="margin-bottom: 10px;">
            <el-tag :type="hasArmPermission ? 'success' : 'danger'">
              iot:alarm-host:arm {{ hasArmPermission ? '✓' : '✗' }}
            </el-tag>
          </div>
          <div style="margin-bottom: 10px;">
            <el-tag :type="hasClearPermission ? 'success' : 'danger'">
              iot:alarm-host:clear-alarm {{ hasClearPermission ? '✓' : '✗' }}
            </el-tag>
          </div>
        </el-col>
      </el-row>
    </el-card>
    
    <el-card>
      <template #header>
        <span>按钮显示测试</span>
      </template>
      
      <div style="margin-bottom: 20px;">
        <el-button
          type="primary"
          v-hasPermi="['iot:alarm-host:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增（应该显示）
        </el-button>
        
        <el-button
          type="success"
          v-hasPermi="['iot:alarm-host:update']"
        >
          <Icon icon="ep:edit" class="mr-5px" /> 修改（应该显示）
        </el-button>
        
        <el-button
          type="danger"
          v-hasPermi="['iot:alarm-host:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 删除（应该显示）
        </el-button>
      </div>
      
      <div>
        <el-button
          type="warning"
          v-hasPermi="['non-existent-permission']"
        >
          不存在的权限（不应该显示）
        </el-button>
      </div>
    </el-card>
    
    <el-card style="margin-top: 20px;">
      <template #header>
        <span>解决方案</span>
      </template>
      
      <el-alert
        v-if="!hasCreatePermission"
        title="权限缺失"
        type="warning"
        :closable="false"
        style="margin-bottom: 15px;"
      >
        <template #default>
          <p>当前用户缺少 <code>iot:alarm-host:create</code> 权限，请按以下步骤解决：</p>
          <ol>
            <li>执行 <code>alarm_host_menu.sql</code> 创建菜单权限</li>
            <li>执行 <code>assign_alarm_host_permissions.sql</code> 分配权限给角色</li>
            <li>清理Redis缓存或重启应用</li>
            <li>重新登录系统</li>
          </ol>
        </template>
      </el-alert>
      
      <el-alert
        v-else
        title="权限正常"
        type="success"
        :closable="false"
      >
        权限配置正确，"新增"按钮应该能正常显示！
      </el-alert>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/modules/user'
import { hasPermission } from '@/directives/permission/hasPermi'

defineOptions({ name: 'AlarmHostPermissionTest' })

const userStore = useUserStore()

// 获取当前用户的所有权限
const userPermissions = computed(() => {
  return Array.from(userStore.permissions)
})

// 检测各个权限
const hasCreatePermission = computed(() => hasPermission(['iot:alarm-host:create']))
const hasUpdatePermission = computed(() => hasPermission(['iot:alarm-host:update']))
const hasDeletePermission = computed(() => hasPermission(['iot:alarm-host:delete']))
const hasArmPermission = computed(() => hasPermission(['iot:alarm-host:arm']))
const hasClearPermission = computed(() => hasPermission(['iot:alarm-host:clear-alarm']))
</script>

<style scoped>
code {
  background-color: #f5f5f5;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
}

.el-tag {
  margin-right: 10px;
}
</style>
