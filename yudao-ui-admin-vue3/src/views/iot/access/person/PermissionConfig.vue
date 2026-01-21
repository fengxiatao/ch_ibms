<template>
  <div class="permission-config">
    <div class="config-layout">
      <!-- 左侧权限组列表 -->
      <div class="group-list-panel">
        <div class="panel-header">
          <span class="panel-title">权限组列表</span>
          <el-button type="primary" size="small" @click="handleAddGroup">
            <Icon icon="ep:plus" class="mr-4px" />新增
          </el-button>
        </div>
        <div class="panel-search">
          <el-input v-model="searchKeyword" placeholder="搜索权限组..." size="small" prefix-icon="ep:search" clearable />
        </div>
        <div class="group-list">
          <div 
            v-for="group in filteredGroups" 
            :key="group.id" 
            :class="['group-item', { active: selectedGroup?.id === group.id }]"
            @click="handleSelectGroup(group)"
          >
            <div class="group-info">
              <span class="group-name">{{ group.groupName }}</span>
              <span class="group-count">{{ group.personCount || 0 }}人</span>
            </div>
            <el-tag size="small" :type="group.status === 1 ? 'success' : 'danger'">
              {{ group.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </div>
          <div v-if="!filteredGroups.length" class="no-data">暂无权限组</div>
        </div>
      </div>

      <!-- 中间权限组详情 -->
      <div class="group-detail-panel" v-if="selectedGroup">
        <div class="panel-header">
          <span class="panel-title">{{ selectedGroup.groupName }}</span>
          <div class="panel-actions">
            <el-button link type="primary" size="small" @click="handleEditGroup">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDeleteGroup">删除</el-button>
          </div>
        </div>
        <div class="detail-content">
          <div class="detail-item">
            <span class="label">认证方式：</span>
            <span class="value">{{ getAuthModeLabel(selectedGroup.authMode) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">关联设备：</span>
            <span class="value">{{ selectedGroup.deviceCount || 0 }} 个</span>
          </div>
          <div class="detail-item">
            <span class="label">描述：</span>
            <span class="value">{{ selectedGroup.description || '-' }}</span>
          </div>

          <!-- 人员列表 -->
          <div class="person-section">
            <div class="section-header">
              <span>关联人员 ({{ groupPersons.length }})</span>
              <el-button link type="primary" size="small" @click="showAddPersonDialog = true">添加人员</el-button>
            </div>
            <el-table :data="groupPersons" size="small" max-height="300">
              <el-table-column label="编号" prop="personCode" width="100" />
              <el-table-column label="姓名" prop="personName" width="80" />
              <el-table-column label="部门" prop="deptName" show-overflow-tooltip />
              <el-table-column label="操作" width="60">
                <template #default="{ row }">
                  <el-button link type="danger" size="small" @click="handleRemovePerson(row)">移除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
      <div v-else class="detail-placeholder">
        <Icon icon="ep:lock" :size="48" />
        <p>请选择权限组查看详情</p>
      </div>

      <!-- 右侧新增/编辑表单 -->
      <div class="group-form-panel" v-if="showForm">
        <div class="panel-header">
          <span class="panel-title">{{ isEdit ? '编辑权限组' : '新增权限组' }}</span>
          <el-button link @click="showForm = false"><Icon icon="ep:close" /></el-button>
        </div>
        <div class="form-content">
          <el-form :model="formData" :rules="formRules" ref="formRef" label-width="80px" size="small">
            <el-form-item label="权限组名" prop="groupName">
              <el-input v-model="formData.groupName" placeholder="请输入权限组名称" />
            </el-form-item>
            <el-form-item label="认证方式" prop="authMode">
              <el-select v-model="formData.authMode" style="width: 100%">
                <el-option v-for="item in AccessAuthModeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="关联设备">
              <el-select v-model="formData.deviceIds" multiple placeholder="选择设备" style="width: 100%">
                <el-option v-for="item in deviceList" :key="item.id" :label="item.deviceName" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="描述">
              <el-input v-model="formData.description" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="状态">
              <el-radio-group v-model="formData.status">
                <el-radio :value="1">正常</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
          <div class="form-actions">
            <el-button @click="showForm = false">取消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AccessPermissionGroupApi, AccessDeviceApi, AccessAuthModeOptions, type AccessPermissionGroupVO, type AccessDeviceVO } from '@/api/iot/access'

const loading = ref(false)
const groups = ref<AccessPermissionGroupVO[]>([])
const deviceList = ref<AccessDeviceVO[]>([])
const selectedGroup = ref<AccessPermissionGroupVO | null>(null)
const groupPersons = ref<any[]>([])
const searchKeyword = ref('')
const showForm = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const showAddPersonDialog = ref(false)

const formData = reactive({
  id: undefined as number | undefined,
  groupName: '',
  authMode: 1,
  deviceIds: [] as number[],
  description: '',
  status: 1
})

const formRules = {
  groupName: [{ required: true, message: '请输入权限组名称', trigger: 'blur' }]
}

const filteredGroups = computed(() => {
  if (!searchKeyword.value) return groups.value
  return groups.value.filter(g => g.groupName.includes(searchKeyword.value))
})

const getAuthModeLabel = (mode: number) => {
  const item = AccessAuthModeOptions.find(i => i.value === mode)
  return item?.label || '未知'
}

const loadGroups = async () => {
  loading.value = true
  try {
    const res = await AccessPermissionGroupApi.getGroupPage({ pageNo: 1, pageSize: 100 })
    groups.value = res.list
  } catch (error) {
    console.error('加载权限组失败:', error)
  } finally {
    loading.value = false
  }
}

const loadDevices = async () => {
  try {
    deviceList.value = await AccessDeviceApi.getDeviceList()
  } catch (error) {
    console.error('加载设备列表失败:', error)
  }
}

const handleSelectGroup = (group: AccessPermissionGroupVO) => {
  selectedGroup.value = group
  groupPersons.value = []
}

const handleAddGroup = () => {
  isEdit.value = false
  Object.assign(formData, { id: undefined, groupName: '', authMode: 1, deviceIds: [], description: '', status: 1 })
  showForm.value = true
}

const handleEditGroup = () => {
  if (!selectedGroup.value) return
  isEdit.value = true
  Object.assign(formData, selectedGroup.value)
  showForm.value = true
}

const handleDeleteGroup = async () => {
  if (!selectedGroup.value) return
  try {
    await ElMessageBox.confirm(`确定要删除权限组"${selectedGroup.value.groupName}"吗？`, '提示', { type: 'warning' })
    await AccessPermissionGroupApi.deleteGroup(selectedGroup.value.id)
    ElMessage.success('删除成功')
    selectedGroup.value = null
    loadGroups()
  } catch (error) {
    if (error !== 'cancel') console.error('删除失败:', error)
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitting.value = true
    if (isEdit.value) {
      await AccessPermissionGroupApi.updateGroup(formData as any)
      ElMessage.success('修改成功')
    } else {
      await AccessPermissionGroupApi.createGroup(formData)
      ElMessage.success('新增成功')
    }
    showForm.value = false
    loadGroups()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

const handleRemovePerson = (row: any) => {
  ElMessage.info('移除人员功能开发中')
}

onMounted(() => {
  loadGroups()
  loadDevices()
})
</script>

<style scoped lang="scss">
.permission-config {
  height: 100%;
  background: #1e1e2d;
}

.config-layout {
  display: flex;
  height: 100%;
}

.group-list-panel {
  width: 280px;
  background: #252532;
  border-right: 1px solid #2d2d3a;
  display: flex;
  flex-direction: column;

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 12px;
    background: #1e1e2d;
    border-bottom: 1px solid #2d2d3a;

    .panel-title {
      font-size: 13px;
      font-weight: 500;
      color: #e0e0e0;
    }
  }

  .panel-search {
    padding: 8px;
    border-bottom: 1px solid #2d2d3a;

    :deep(.el-input__wrapper) {
      background: #1e1e2d;
      box-shadow: none;
      border: 1px solid #3d3d4a;
    }
  }

  .group-list {
    flex: 1;
    overflow: auto;
    padding: 8px;

    .group-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 12px;
      margin-bottom: 4px;
      background: #1e1e2d;
      border-radius: 4px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover, &.active {
        background: #2d3a4a;
      }

      .group-info {
        .group-name {
          display: block;
          font-size: 13px;
          color: #e0e0e0;
        }
        .group-count {
          font-size: 11px;
          color: #8c8c9a;
        }
      }
    }

    .no-data {
      text-align: center;
      padding: 40px 0;
      color: #6c6c7a;
      font-size: 13px;
    }
  }
}

.group-detail-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 400px;

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 12px;
    background: #1e1e2d;
    border-bottom: 1px solid #2d2d3a;

    .panel-title {
      font-size: 14px;
      font-weight: 500;
      color: #e0e0e0;
    }
  }

  .detail-content {
    flex: 1;
    overflow: auto;
    padding: 16px;

    .detail-item {
      display: flex;
      margin-bottom: 12px;
      font-size: 13px;

      .label {
        width: 80px;
        color: #8c8c9a;
        flex-shrink: 0;
      }
      .value {
        color: #c4c4c4;
      }
    }

    .person-section {
      margin-top: 20px;
      background: #252532;
      border-radius: 4px;
      overflow: hidden;

      .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 10px 12px;
        background: #1e1e2d;
        border-bottom: 1px solid #2d2d3a;
        font-size: 13px;
        color: #e0e0e0;
      }

      :deep(.el-table) {
        background: transparent;
        --el-table-bg-color: transparent;
        --el-table-tr-bg-color: transparent;
        --el-table-header-bg-color: #1e1e2d;
        --el-table-row-hover-bg-color: #2d2d3a;
        --el-table-border-color: #2d2d3a;
        --el-table-text-color: #c4c4c4;
        --el-table-header-text-color: #e0e0e0;
      }
    }
  }
}

.detail-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #6c6c7a;

  p {
    margin-top: 12px;
    font-size: 13px;
  }
}

.group-form-panel {
  width: 320px;
  background: #252532;
  border-left: 1px solid #2d2d3a;
  display: flex;
  flex-direction: column;

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 12px;
    background: #1e1e2d;
    border-bottom: 1px solid #2d2d3a;

    .panel-title {
      font-size: 13px;
      font-weight: 500;
      color: #e0e0e0;
    }

    .el-button {
      color: #8c8c9a;
      &:hover { color: #409eff; }
    }
  }

  .form-content {
    flex: 1;
    overflow: auto;
    padding: 16px;

    :deep(.el-form-item) {
      margin-bottom: 16px;

      .el-form-item__label {
        color: #8c8c9a;
        font-size: 12px;
      }

      .el-input__wrapper, .el-select__wrapper, .el-textarea__inner {
        background: #1e1e2d;
        box-shadow: none;
        border: 1px solid #3d3d4a;
      }

      .el-input__inner, .el-textarea__inner {
        color: #c4c4c4;
      }
    }
  }

  .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    padding: 12px 16px;
    border-top: 1px solid #2d2d3a;
  }
}
</style>
