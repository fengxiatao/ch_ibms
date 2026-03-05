<template>
  <el-dialog
    v-model="visible"
    title=""
    width="1200px"
    :close-on-click-modal="false"
    class="permission-dialog"
  >
    <template #header>
      <div class="dialog-header">
        <div class="header-left">
          <div class="header-icon">
            <Icon icon="ep:user-filled" />
          </div>
          <div class="header-info">
            <h3>人员权限管理</h3>
            <p>配置系统操作人员的访问权限和功能权限</p>
          </div>
        </div>
        <div class="header-right">
          <el-button link @click="showPermissionLog">
            <Icon icon="ep:clock" class="mr-4px" />
            权限变更日志
          </el-button>
        </div>
      </div>
    </template>

    <div class="permission-content">
      <!-- 左侧：人员列表 -->
      <aside class="user-sidebar">
        <div class="sidebar-search">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索姓名、账号..."
            prefix-icon="Search"
            clearable
          />
        </div>

        <div class="user-list">
          <div
            v-for="user in filteredUsers"
            :key="user.id"
            class="user-item"
            :class="{ 'user-item--active': selectedUser?.id === user.id, 'user-item--disabled': !user.enabled }"
            @click="selectUser(user)"
          >
            <div class="user-avatar" :class="getAvatarClass(user.role)">
              <Icon :icon="getRoleIcon(user.role)" />
            </div>
            <div class="user-info">
              <div class="user-name">{{ user.name }}</div>
              <div class="user-account">{{ user.account }} • {{ user.roleName }}</div>
            </div>
            <span 
              class="user-status" 
              :class="{ 'user-status--online': user.online, 'user-status--disabled': !user.enabled }"
            ></span>
            <el-tag v-if="!user.enabled" type="danger" size="small">已停用</el-tag>
          </div>
        </div>

        <div class="sidebar-footer">
          <el-button type="primary" @click="openAddUserDrawer" style="width: 100%">
            <Icon icon="ep:plus" class="mr-4px" />
            添加人员
          </el-button>
        </div>
      </aside>

      <!-- 右侧：权限配置 -->
      <main class="permission-main">
        <template v-if="selectedUser">
          <div class="permission-header">
            <div class="header-user">
              <div class="user-avatar user-avatar--large" :class="getAvatarClass(selectedUser.role)">
                <Icon :icon="getRoleIcon(selectedUser.role)" size="32" />
              </div>
              <div class="user-detail">
                <h3>{{ selectedUser.name }}</h3>
                <p>{{ selectedUser.account }} • {{ selectedUser.roleName }}</p>
                <el-tag :type="selectedUser.enabled ? 'success' : 'danger'" size="small">
                  {{ selectedUser.enabled ? '账号启用中' : '账号已停用' }}
                </el-tag>
              </div>
            </div>
            <el-button type="primary" @click="openEditDrawer">
              <Icon icon="ep:edit" class="mr-4px" />
              编辑权限
            </el-button>
          </div>

          <div class="permission-sections">
            <!-- 授权主机 -->
            <div class="permission-section">
              <h4>
                <Icon icon="ep:monitor" class="mr-8px" />
                授权主机范围
              </h4>
              <div class="host-tags">
                <el-tag 
                  v-for="host in selectedUser.authorizedHosts" 
                  :key="host"
                  type="primary"
                >
                  {{ host }}
                </el-tag>
                <el-tag v-if="selectedUser.allHosts" type="success">全部主机</el-tag>
              </div>
            </div>

            <!-- 权限矩阵 -->
            <div class="permission-section">
              <h4>
                <Icon icon="ep:grid" class="mr-8px" />
                功能权限明细
              </h4>
              <el-table :data="permissionMatrix" border stripe>
                <el-table-column prop="module" label="功能模块" width="200">
                  <template #default="{ row }">
                    <Icon :icon="row.icon" class="mr-8px" />
                    {{ row.module }}
                  </template>
                </el-table-column>
                <el-table-column label="查看" width="100" align="center">
                  <template #default="{ row }">
                    <Icon 
                      :icon="row.view ? 'ep:circle-check-filled' : 'ep:circle-close'" 
                      :class="row.view ? 'text-green-500' : 'text-gray-300'"
                      size="18"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="控制" width="100" align="center">
                  <template #default="{ row }">
                    <Icon 
                      v-if="row.controlDisabled"
                      icon="ep:remove"
                      class="text-gray-300"
                      size="18"
                    />
                    <Icon 
                      v-else
                      :icon="row.control ? 'ep:circle-check-filled' : 'ep:circle-close'" 
                      :class="row.control ? 'text-green-500' : 'text-gray-300'"
                      size="18"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="配置" width="100" align="center">
                  <template #default="{ row }">
                    <Icon 
                      v-if="row.configDisabled"
                      icon="ep:remove"
                      class="text-gray-300"
                      size="18"
                    />
                    <Icon 
                      v-else
                      :icon="row.config ? 'ep:circle-check-filled' : 'ep:circle-close'" 
                      :class="row.config ? 'text-green-500' : 'text-gray-300'"
                      size="18"
                    />
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <!-- 最近操作 -->
            <div class="permission-section">
              <h4>
                <Icon icon="ep:clock" class="mr-8px" />
                最近操作记录
              </h4>
              <div class="recent-operations">
                <div class="operation-item" v-for="op in recentOperations" :key="op.id">
                  <span class="operation-text">{{ op.text }}</span>
                  <span class="operation-time">{{ op.time }}</span>
                </div>
              </div>
            </div>
          </div>
        </template>

        <template v-else>
          <div class="empty-state">
            <Icon icon="ep:user-filled" size="64" class="text-gray-200" />
            <p>请选择左侧人员查看或编辑权限</p>
            <span>点击"添加人员"创建新账号并配置权限</span>
          </div>
        </template>
      </main>
    </div>
  </el-dialog>

  <!-- 添加/编辑人员抽屉 -->
  <el-drawer
    v-model="drawerVisible"
    :title="isEditing ? '编辑人员权限' : '添加人员'"
    direction="rtl"
    size="600px"
  >
    <el-form :model="userForm" label-width="100px">
      <!-- 基本信息 -->
      <div class="form-section">
        <h4>
          <Icon icon="ep:postcard" class="mr-8px" />
          基本信息
        </h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名" required>
              <el-input v-model="userForm.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="登录账号" required>
              <el-input v-model="userForm.account" placeholder="建议使用工号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="userForm.phone" placeholder="11位手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属部门">
              <el-select v-model="userForm.department" style="width: 100%">
                <el-option label="安保部" value="security" />
                <el-option label="物业部" value="property" />
                <el-option label="技术部" value="tech" />
                <el-option label="行政部" value="admin" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-checkbox v-model="userForm.enabled">启用账号</el-checkbox>
        </el-form-item>
      </div>

      <!-- 角色模板 -->
      <div class="form-section">
        <h4>
          <Icon icon="ep:avatar" class="mr-8px" />
          角色模板
        </h4>
        <el-radio-group v-model="userForm.roleTemplate" @change="applyRoleTemplate">
          <el-radio-button label="admin">超级管理员</el-radio-button>
          <el-radio-button label="operator">操作员</el-radio-button>
          <el-radio-button label="monitor">监控员</el-radio-button>
          <el-radio-button label="maintainer">维护员</el-radio-button>
        </el-radio-group>
        <p class="role-desc">{{ roleDescriptions[userForm.roleTemplate] }}</p>
      </div>

      <!-- 主机权限 -->
      <div class="form-section">
        <h4>
          <Icon icon="ep:monitor" class="mr-8px" />
          主机访问权限
        </h4>
        <el-checkbox-group v-model="userForm.authorizedHosts">
          <el-checkbox label="all">全部主机</el-checkbox>
          <el-checkbox label="building1">长辉大厦-1906主机</el-checkbox>
          <el-checkbox label="garage">长辉大厦-地下车库A区</el-checkbox>
          <el-checkbox label="rdcenter">研发中心-主机01</el-checkbox>
        </el-checkbox-group>
      </div>

      <!-- 功能权限 -->
      <div class="form-section">
        <h4>
          <Icon icon="ep:grid" class="mr-8px" />
          功能权限明细
        </h4>
        <el-table :data="editPermissionMatrix" border size="small">
          <el-table-column prop="module" label="功能模块" width="180" />
          <el-table-column label="查看" width="80" align="center">
            <template #default="{ row }">
              <el-checkbox v-model="row.view" :disabled="row.viewDisabled" />
            </template>
          </el-table-column>
          <el-table-column label="控制" width="80" align="center">
            <template #default="{ row }">
              <el-checkbox v-model="row.control" :disabled="row.controlDisabled" />
            </template>
          </el-table-column>
          <el-table-column label="配置" width="80" align="center">
            <template #default="{ row }">
              <el-checkbox v-model="row.config" :disabled="row.configDisabled" />
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 时间限制 -->
      <div class="form-section">
        <h4>
          <Icon icon="ep:timer" class="mr-8px" />
          操作时间限制
        </h4>
        <el-checkbox v-model="userForm.timeLimit.enabled" @change="toggleTimeLimit">
          启用操作时间限制
        </el-checkbox>
        <div class="time-limit-config" v-if="userForm.timeLimit.enabled">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="开始时间">
                <el-time-picker v-model="userForm.timeLimit.startTime" placeholder="开始时间" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="结束时间">
                <el-time-picker v-model="userForm.timeLimit.endTime" placeholder="结束时间" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-checkbox-group v-model="userForm.timeLimit.weekdays">
            <el-checkbox label="1">周一</el-checkbox>
            <el-checkbox label="2">周二</el-checkbox>
            <el-checkbox label="3">周三</el-checkbox>
            <el-checkbox label="4">周四</el-checkbox>
            <el-checkbox label="5">周五</el-checkbox>
            <el-checkbox label="6">周六</el-checkbox>
            <el-checkbox label="0">周日</el-checkbox>
          </el-checkbox-group>
        </div>
      </div>
    </el-form>

    <template #footer>
      <div class="drawer-footer">
        <el-button v-if="isEditing" type="danger" text @click="handleDeleteUser">
          <Icon icon="ep:delete" class="mr-4px" />
          删除人员
        </el-button>
        <div class="footer-right">
          <el-button @click="drawerVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveUser">
            <Icon icon="ep:check" class="mr-4px" />
            保存配置
          </el-button>
        </div>
      </div>
    </template>
  </el-drawer>

  <!-- 权限变更日志弹窗 -->
  <el-dialog v-model="logVisible" title="权限变更日志" width="800px">
    <div class="log-list">
      <div class="log-item" v-for="log in permissionLogs" :key="log.id">
        <div class="log-icon" :class="getLogIconClass(log.type)">
          <Icon :icon="getLogIcon(log.type)" />
        </div>
        <div class="log-content">
          <div class="log-header">
            <span class="log-title">{{ log.title }}</span>
            <span class="log-time">{{ log.time }}</span>
          </div>
          <div class="log-desc">{{ log.description }}</div>
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="logVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

defineOptions({ name: 'PermissionModal' })

// 弹窗状态
const visible = ref(false)
const drawerVisible = ref(false)
const logVisible = ref(false)
const isEditing = ref(false)

// 搜索
const searchKeyword = ref('')

// 用户列表
const users = ref([
  { id: 1, name: '系统管理员', account: 'admin', role: 'admin', roleName: '超级管理员', enabled: true, online: true, allHosts: true, authorizedHosts: [] },
  { id: 2, name: '张操作员', account: 'zhangsan', role: 'operator', roleName: '操作员', enabled: true, online: true, allHosts: false, authorizedHosts: ['长辉大厦-1906主机'] },
  { id: 3, name: '李保安', account: 'libaoan', role: 'monitor', roleName: '监控员', enabled: true, online: false, allHosts: false, authorizedHosts: ['地下车库'] },
  { id: 4, name: '王维护', account: 'wangwei', role: 'maintainer', roleName: '维护员', enabled: false, online: false, allHosts: false, authorizedHosts: ['设备维护'] }
])

const selectedUser = ref<any>(null)

// 过滤后的用户列表
const filteredUsers = computed(() => {
  if (!searchKeyword.value) return users.value
  const keyword = searchKeyword.value.toLowerCase()
  return users.value.filter(u => 
    u.name.toLowerCase().includes(keyword) || 
    u.account.toLowerCase().includes(keyword)
  )
})

// 权限矩阵
const permissionMatrix = computed(() => {
  if (!selectedUser.value) return []
  const role = selectedUser.value.role
  const permissions = rolePermissions[role] || {}
  
  return [
    { module: '报警主机管理', icon: 'ep:monitor', ...permissions.host },
    { module: '操作记录', icon: 'ep:document', ...permissions.operation, controlDisabled: true, configDisabled: true },
    { module: '报警记录处理', icon: 'ep:bell', ...permissions.alarm, configDisabled: true },
    { module: '撤防旁路操作', icon: 'ep:connection', ...permissions.bypass, configDisabled: true },
    { module: '人员权限管理', icon: 'ep:user', ...permissions.user },
    { module: '系统设置', icon: 'ep:setting', ...permissions.settings, controlDisabled: true }
  ]
})

// 角色权限配置
const rolePermissions: Record<string, any> = {
  admin: {
    host: { view: true, control: true, config: true },
    operation: { view: true },
    alarm: { view: true, control: true },
    bypass: { view: true, control: true },
    user: { view: true, control: true, config: true },
    settings: { view: true, config: true }
  },
  operator: {
    host: { view: true, control: true, config: false },
    operation: { view: true },
    alarm: { view: true, control: true },
    bypass: { view: true, control: false },
    user: { view: false, control: false, config: false },
    settings: { view: false, config: false }
  },
  monitor: {
    host: { view: true, control: false, config: false },
    operation: { view: true },
    alarm: { view: true, control: true },
    bypass: { view: true, control: false },
    user: { view: false, control: false, config: false },
    settings: { view: false, config: false }
  },
  maintainer: {
    host: { view: true, control: false, config: true },
    operation: { view: true },
    alarm: { view: true, control: false },
    bypass: { view: true, control: true },
    user: { view: false, control: false, config: false },
    settings: { view: true, config: false }
  }
}

// 角色描述
const roleDescriptions: Record<string, string> = {
  admin: '全部权限，可管理其他人员',
  operator: '布防/撤防/消警，不可配置系统',
  monitor: '仅查看和确认报警，无控制权限',
  maintainer: '设备维护和旁路操作'
}

// 最近操作
const recentOperations = ref([
  { id: 1, text: '居家布防 - 长辉-1906主机', time: '2026-02-03 09:43:12' },
  { id: 2, text: '撤防操作 - 长辉-1906主机', time: '2026-02-03 09:29:53' }
])

// 权限变更日志
const permissionLogs = ref([
  { id: 1, type: 'edit', title: '管理员 修改了 张操作员 的权限', time: '2026-02-03 14:30:25', description: '撤防旁路操作-控制 → 新增权限' },
  { id: 2, type: 'add', title: '管理员 添加了新人员 李保安', time: '2026-02-02 09:15:00', description: '初始角色：监控员，授权区域：地下车库' },
  { id: 3, type: 'disable', title: '管理员 停用了 王维护 的账号', time: '2026-01-28 16:45:12', description: '原因：离职' }
])

// 用户表单
const userForm = reactive({
  name: '',
  account: '',
  phone: '',
  department: 'security',
  enabled: true,
  roleTemplate: 'operator',
  authorizedHosts: [] as string[],
  timeLimit: {
    enabled: false,
    startTime: '',
    endTime: '',
    weekdays: ['1', '2', '3', '4', '5']
  }
})

// 编辑权限矩阵
const editPermissionMatrix = ref([
  { module: '报警主机管理', view: true, control: true, config: false, viewDisabled: true },
  { module: '操作记录', view: true, control: false, config: false, controlDisabled: true, configDisabled: true },
  { module: '报警记录处理', view: true, control: true, config: false, configDisabled: true },
  { module: '撤防旁路操作', view: true, control: false, config: false, configDisabled: true },
  { module: '人员权限管理', view: false, control: false, config: false },
  { module: '系统设置', view: false, control: false, config: false, controlDisabled: true }
])

// 获取头像类
const getAvatarClass = (role: string) => {
  const classes: Record<string, string> = {
    admin: 'avatar--purple',
    operator: 'avatar--blue',
    monitor: 'avatar--orange',
    maintainer: 'avatar--gray'
  }
  return classes[role] || 'avatar--gray'
}

// 获取角色图标
const getRoleIcon = (role: string) => {
  const icons: Record<string, string> = {
    admin: 'ep:user-filled',
    operator: 'ep:user',
    monitor: 'ep:view',
    maintainer: 'ep:tools'
  }
  return icons[role] || 'ep:user'
}

// 获取日志图标类
const getLogIconClass = (type: string) => {
  const classes: Record<string, string> = {
    edit: 'log-icon--purple',
    add: 'log-icon--green',
    disable: 'log-icon--red'
  }
  return classes[type] || 'log-icon--gray'
}

// 获取日志图标
const getLogIcon = (type: string) => {
  const icons: Record<string, string> = {
    edit: 'ep:edit',
    add: 'ep:circle-plus',
    disable: 'ep:remove-filled'
  }
  return icons[type] || 'ep:info-filled'
}

// 打开弹窗
const open = () => {
  visible.value = true
}

// 选择用户
const selectUser = (user: any) => {
  selectedUser.value = user
}

// 打开添加抽屉
const openAddUserDrawer = () => {
  isEditing.value = false
  Object.assign(userForm, {
    name: '',
    account: '',
    phone: '',
    department: 'security',
    enabled: true,
    roleTemplate: 'operator',
    authorizedHosts: [],
    timeLimit: {
      enabled: false,
      startTime: '',
      endTime: '',
      weekdays: ['1', '2', '3', '4', '5']
    }
  })
  drawerVisible.value = true
}

// 打开编辑抽屉
const openEditDrawer = () => {
  if (!selectedUser.value) return
  isEditing.value = true
  Object.assign(userForm, {
    name: selectedUser.value.name,
    account: selectedUser.value.account,
    phone: '',
    department: 'security',
    enabled: selectedUser.value.enabled,
    roleTemplate: selectedUser.value.role,
    authorizedHosts: selectedUser.value.allHosts ? ['all'] : [],
    timeLimit: {
      enabled: false,
      startTime: '',
      endTime: '',
      weekdays: ['1', '2', '3', '4', '5']
    }
  })
  drawerVisible.value = true
}

// 应用角色模板
const applyRoleTemplate = (role: string) => {
  const permissions = rolePermissions[role]
  if (permissions) {
    editPermissionMatrix.value.forEach(item => {
      const key = item.module === '报警主机管理' ? 'host' :
                  item.module === '操作记录' ? 'operation' :
                  item.module === '报警记录处理' ? 'alarm' :
                  item.module === '撤防旁路操作' ? 'bypass' :
                  item.module === '人员权限管理' ? 'user' : 'settings'
      
      if (permissions[key]) {
        item.view = permissions[key].view ?? false
        item.control = permissions[key].control ?? false
        item.config = permissions[key].config ?? false
      }
    })
  }
}

// 切换时间限制
const toggleTimeLimit = (enabled: boolean) => {
  if (!enabled) {
    userForm.timeLimit.startTime = ''
    userForm.timeLimit.endTime = ''
  }
}

// 保存用户
const handleSaveUser = () => {
  if (!userForm.name) {
    ElMessage.warning('请输入姓名')
    return
  }
  
  drawerVisible.value = false
  ElMessage.success(isEditing.value ? '人员权限已更新' : '新人员已添加并配置权限')
}

// 删除用户
const handleDeleteUser = async () => {
  await ElMessageBox.confirm('确定要删除该人员吗？此操作不可恢复。', '删除确认', { type: 'warning' })
  drawerVisible.value = false
  ElMessage.success('人员已删除')
}

// 显示权限日志
const showPermissionLog = () => {
  logVisible.value = true
}

// 暴露方法
defineExpose({ open })
</script>

<style lang="scss" scoped>
.permission-dialog {
  --nia-surface-color: var(--el-bg-color);
  --nia-surface-muted-color: var(--el-fill-color-light);
  --nia-border-color: var(--el-border-color-light);
  --nia-text-primary: var(--el-text-color-primary);
  --nia-text-regular: var(--el-text-color-regular);
  --nia-text-secondary: var(--el-text-color-secondary);

  :deep(.el-dialog__header) {
    padding: 0;
    margin: 0;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }

  :deep(.el-dialog) {
    background: var(--nia-surface-color);
  }
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--nia-border-color);
  background: linear-gradient(to right, var(--el-color-primary-light-9), var(--nia-surface-color));

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .header-icon {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    background: var(--el-color-primary);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
  }

  .header-info {
    h3 {
      font-size: 16px;
      font-weight: 600;
      color: var(--nia-text-primary);
      margin: 0;
    }

    p {
      font-size: 12px;
      color: var(--nia-text-secondary);
      margin: 4px 0 0;
    }
  }
}

.permission-content {
  display: flex;
  height: 600px;
}

.user-sidebar {
  width: 300px;
  border-right: 1px solid var(--nia-border-color);
  display: flex;
  flex-direction: column;
  background: var(--nia-surface-muted-color);
}

.sidebar-search {
  padding: 12px;
  border-bottom: 1px solid var(--nia-border-color);
  background: var(--nia-surface-color);
}

.user-list {
  flex: 1;
  overflow: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--nia-surface-color);
  border-radius: 8px;
  border: 1px solid var(--nia-border-color);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--el-color-primary);
  }

  &--active {
    border-color: var(--el-color-primary);
    box-shadow: 0 0 0 2px var(--el-color-primary-light-7);
  }

  &--disabled {
    opacity: 0.6;
  }
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;

  &--large {
    width: 64px;
    height: 64px;
    font-size: 32px;
  }

  &.avatar--purple { background: var(--el-color-primary-light-9); color: var(--el-color-primary); }
  &.avatar--blue { background: var(--el-color-primary-light-9); color: var(--el-color-primary); }
  &.avatar--orange { background: var(--el-color-warning-light-9); color: var(--el-color-warning); }
  &.avatar--gray { background: var(--nia-surface-muted-color); color: var(--nia-text-secondary); }
}

.user-info {
  flex: 1;
  min-width: 0;

  .user-name {
    font-size: 14px;
    font-weight: 500;
    color: var(--nia-text-primary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .user-account {
    font-size: 12px;
    color: var(--nia-text-secondary);
  }
}

.user-status {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #c0c4cc;

  &--online {
    background: #67c23a;
    box-shadow: 0 0 4px #67c23a;
  }

  &--disabled {
    background: #f56c6c;
  }
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid var(--nia-border-color);
  background: var(--nia-surface-color);
}

.permission-main {
  flex: 1;
  overflow: auto;
  padding: 20px;
}

.permission-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;

  .header-user {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .user-detail {
    h3 {
      font-size: 18px;
      font-weight: 600;
      color: var(--nia-text-primary);
      margin: 0 0 4px;
    }

    p {
      font-size: 13px;
      color: var(--nia-text-secondary);
      margin: 0 0 8px;
    }
  }
}

.permission-sections {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.permission-section {
  background: var(--nia-surface-muted-color);
  border-radius: 8px;
  padding: 16px;
  border: 1px solid var(--nia-border-color);

  h4 {
    font-size: 14px;
    font-weight: 600;
    color: var(--nia-text-regular);
    margin: 0 0 12px;
    display: flex;
    align-items: center;
  }
}

.host-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.recent-operations {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.operation-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--nia-border-color);
  font-size: 13px;

  &:last-child {
    border-bottom: none;
  }

  .operation-text {
    color: var(--nia-text-regular);
  }

  .operation-time {
    color: var(--nia-text-secondary);
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--nia-text-secondary);

  p {
    font-size: 16px;
    margin: 16px 0 8px;
    color: var(--nia-text-regular);
  }

  span {
    font-size: 13px;
  }
}

// 抽屉样式
.form-section {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--nia-border-color);

  &:last-child {
    border-bottom: none;
  }

  h4 {
    font-size: 14px;
    font-weight: 600;
    color: var(--nia-text-regular);
    margin: 0 0 16px;
    display: flex;
    align-items: center;
  }
}

.role-desc {
  font-size: 12px;
  color: var(--nia-text-secondary);
  margin-top: 8px;
}

.time-limit-config {
  margin-top: 16px;
  padding: 16px;
  background: var(--nia-surface-muted-color);
  border-radius: 8px;
}

.drawer-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .footer-right {
    display: flex;
    gap: 8px;
  }
}

// 日志样式
.log-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.log-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: var(--nia-surface-muted-color);
  border-radius: 8px;
}

.log-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;

  &--purple { background: var(--el-color-primary-light-9); color: var(--el-color-primary); }
  &--green { background: var(--el-color-success-light-9); color: var(--el-color-success); }
  &--red { background: var(--el-color-danger-light-9); color: var(--el-color-danger); }
  &--gray { background: var(--nia-surface-muted-color); color: var(--nia-text-secondary); }
}

.log-content {
  flex: 1;

  .log-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 4px;
  }

  .log-title {
    font-weight: 500;
    color: var(--nia-text-primary);
  }

  .log-time {
    font-size: 12px;
    color: var(--nia-text-secondary);
  }

  .log-desc {
    font-size: 13px;
    color: var(--nia-text-regular);
  }
}

:deep(.el-drawer) {
  background: var(--el-bg-color);
}
</style>
