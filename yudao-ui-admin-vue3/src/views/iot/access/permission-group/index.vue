<template>
  <ContentWrap
    :body-style="{
      padding: '0',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      backgroundColor: 'var(--el-bg-color)'
    }"
    style="height: calc(100vh - var(--page-top-gap, 70px)); padding-top: var(--page-top-gap, 70px)"
  >
    <div class="permission-group-page">
      <!-- 左侧：权限组列表（表格形式） -->
      <div class="left-panel">
        <div class="panel-toolbar">
          <el-button type="primary" size="small" @click="handleAddGroup">
            <Icon icon="ep:plus" />
          </el-button>
          <el-button type="danger" size="small" :disabled="!selectedGroup" @click="handleDeleteGroup">
            <Icon icon="ep:delete" />
          </el-button>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索..."
            size="small"
            style="width: 140px; margin-left: auto"
            clearable
            @input="handleSearch"
          >
            <template #suffix>
              <Icon icon="ep:search" />
            </template>
          </el-input>
        </div>
        <div class="group-table" v-loading="loading">
          <el-table
            :data="list"
            size="small"
            highlight-current-row
            @current-change="handleSelectGroup"
            :row-class-name="getRowClassName"
          >
            <el-table-column type="selection" width="40" />
            <el-table-column label="权限组" prop="groupName" show-overflow-tooltip />
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button link size="small" @click.stop="handleEditGroup(row)">
                    <Icon icon="ep:edit" />
                  </el-button>
                  <el-button link size="small" @click.stop="handleManagePersons(row)">
                    <Icon icon="ep:user" />
                  </el-button>
                  <el-button link type="danger" size="small" @click.stop="handleDeleteGroupRow(row)">
                    <Icon icon="ep:delete" />
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="panel-pagination">
          <el-pagination
            v-model:current-page="queryParams.pageNo"
            :page-size="queryParams.pageSize"
            :total="total"
            layout="prev, pager, next"
            small
            @current-change="getList"
          />
        </div>
      </div>

      <!-- 右侧：关联信息 -->
      <div class="right-panel">
        <div class="panel-header">
          <span class="panel-title">关联信息</span>
          <Icon icon="ep:question-filled" class="help-icon" />
        </div>

        <div class="panel-content" v-if="selectedGroup">
          <!-- 权限组详情 -->
          <div class="detail-section">
            <div class="detail-header">权限组详情</div>
            <div class="detail-body">
              <div class="detail-row">
                <span class="label">名称：</span>
                <span class="value">{{ selectedGroup.groupName }}</span>
              </div>
              <div class="detail-row">
                <span class="label">时间模板：</span>
                <span class="value">{{ selectedGroup.timeTemplateName || '全时间时间模板' }}</span>
              </div>
            </div>
          </div>

          <!-- 下方两列布局：设备列表 + 人员列表 -->
          <div class="two-column-layout">
            <!-- 设备列表 -->
            <div class="column device-column">
              <div class="column-header">
                <span>权限组({{ groupDevices.length }})</span>
              </div>
              <div class="column-body">
                <div v-if="groupDevices.length" class="device-list">
                  <div v-for="device in groupDevices" :key="device.id" class="device-item">
                    {{ device.deviceIp || device.deviceName }}-{{ device.channelName || '通道' }}
                  </div>
                </div>
                <div v-else class="no-data">暂无关联设备</div>
              </div>
            </div>

            <!-- 人员列表 -->
            <div class="column person-column">
              <div class="column-header">
                <span>人员列表({{ groupPersons.length }})</span>
                <el-button 
                  v-if="selectedPersonIds.length" 
                  type="danger" 
                  size="small" 
                  link
                  @click="handleRemoveSelectedPersons"
                >
                  移除选中({{ selectedPersonIds.length }})
                </el-button>
              </div>
              <div class="column-body">
                <el-table 
                  :data="groupPersons" 
                  size="small" 
                  max-height="300"
                  @selection-change="handlePersonSelectionChange"
                >
                  <el-table-column type="selection" width="40" />
                  <el-table-column label="用户名" prop="personName" width="100" />
                  <el-table-column label="部门" prop="deptName" show-overflow-tooltip />
                  <el-table-column label="操作" width="60" align="center">
                    <template #default="{ row }">
                      <el-button link type="danger" size="small" @click="handleRemovePerson(row)">
                        <Icon icon="ep:delete" />
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <div v-if="!groupPersons.length" class="no-data">暂无关联人员</div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <Icon icon="ep:info-filled" :size="32" />
          <p>请选择权限组查看关联信息</p>
        </div>
      </div>
    </div>

    <!-- 新增/编辑权限组抽屉 -->
    <PermissionGroupDrawer
      v-model="showFormDrawer"
      :edit-data="editGroupData"
      @success="handleDrawerSuccess"
    />

    <!-- 人员管理弹窗（支持添加和删除） -->
    <PersonManagerDialog
      v-model:visible="showPersonManagerDialog"
      :group-id="currentGroupId"
      :group-name="currentGroupName"
      @success="handlePersonManagerSuccess"
    />
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  AccessPermissionGroupApi,
  type AccessPermissionGroupVO
} from '@/api/iot/access'
import PersonManagerDialog from './PersonManagerDialog.vue'
import PermissionGroupDrawer from './PermissionGroupDrawer.vue'

defineOptions({ name: 'AccessPermissionGroup' })

const loading = ref(false)
const list = ref<AccessPermissionGroupVO[]>([])
const total = ref(0)
const selectedGroup = ref<AccessPermissionGroupVO | null>(null)
const groupDevices = ref<any[]>([])
const groupPersons = ref<any[]>([])
const searchKeyword = ref('')
const showFormDrawer = ref(false)
const editGroupData = ref<AccessPermissionGroupVO | null>(null)
const showPersonManagerDialog = ref(false)
const currentGroupId = ref<number>()
const currentGroupName = ref<string>('')
const selectedPersonIds = ref<number[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  groupName: ''
})

const getRowClassName = ({ row }: { row: AccessPermissionGroupVO }) => {
  return selectedGroup.value?.id === row.id ? 'current-row' : ''
}

const getList = async () => {
  loading.value = true
  try {
    queryParams.groupName = searchKeyword.value
    const res = await AccessPermissionGroupApi.getGroupPage(queryParams)
    list.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('获取权限组列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNo = 1
  getList()
}

const handleSelectGroup = (group: AccessPermissionGroupVO | null) => {
  selectedGroup.value = group
  if (group) {
    loadGroupDevices()
    loadGroupPersons()
  }
}

const loadGroupDevices = async () => {
  if (!selectedGroup.value) return
  try {
    // 加载关联设备
    const devices = await AccessPermissionGroupApi.getGroupDevices(selectedGroup.value.id)
    groupDevices.value = devices || []
  } catch (error) {
    console.error('加载关联设备失败:', error)
    groupDevices.value = []
  }
}

const loadGroupPersons = async () => {
  if (!selectedGroup.value) return
  try {
    // 加载关联人员
    const persons = await AccessPermissionGroupApi.getGroupPersons(selectedGroup.value.id)
    groupPersons.value = persons || []
  } catch (error) {
    console.error('加载关联人员失败:', error)
    groupPersons.value = []
  }
}

const handleAddGroup = () => {
  editGroupData.value = null
  showFormDrawer.value = true
}

const handleEditGroup = async (row: AccessPermissionGroupVO) => {
  try {
    // 获取完整的权限组详情（包含设备列表）
    const groupDetail = await AccessPermissionGroupApi.getGroup(row.id)
    editGroupData.value = groupDetail
    showFormDrawer.value = true
  } catch (error) {
    console.error('获取权限组详情失败:', error)
    ElMessage.error('获取权限组详情失败')
  }
}

const handleDrawerSuccess = () => {
  getList()
  if (selectedGroup.value) {
    loadGroupDevices()
    loadGroupPersons()
  }
}

const handleManagePersons = (row: AccessPermissionGroupVO) => {
  currentGroupId.value = row.id
  currentGroupName.value = row.groupName || ''
  showPersonManagerDialog.value = true
}

const handlePersonManagerSuccess = () => {
  if (selectedGroup.value) {
    loadGroupPersons()
  }
}

const handleDeleteGroup = async () => {
  if (!selectedGroup.value) return
  await handleDeleteGroupRow(selectedGroup.value)
}

const handleDeleteGroupRow = async (row: AccessPermissionGroupVO) => {
  try {
    await ElMessageBox.confirm(`确定要删除权限组"${row.groupName}"吗？`, '提示', {
      type: 'warning'
    })
    await AccessPermissionGroupApi.deleteGroup(row.id)
    ElMessage.success('删除成功')
    if (selectedGroup.value?.id === row.id) {
      selectedGroup.value = null
    }
    getList()
  } catch (error) {
    if (error !== 'cancel') console.error('删除失败:', error)
  }
}

// 人员选择变化
const handlePersonSelectionChange = (rows: any[]) => {
  selectedPersonIds.value = rows.map(r => r.id)
}

// 移除单个人员并触发权限撤销
const handleRemovePerson = async (person: any) => {
  if (!selectedGroup.value) return
  try {
    await ElMessageBox.confirm(
      `确定要从权限组中移除"${person.personName}"吗？\n移除后将自动撤销该人员在关联设备上的权限。`, 
      '提示', 
      { type: 'warning' }
    )
    // 调用带撤销的接口
    await AccessPermissionGroupApi.removePersonsWithRevoke(selectedGroup.value.id, [person.id])
    ElMessage.success('移除成功，正在撤销关联设备权限')
    loadGroupPersons()
  } catch (error) {
    if (error !== 'cancel') console.error('移除人员失败:', error)
  }
}

// 批量移除选中人员并触发权限撤销
const handleRemoveSelectedPersons = async () => {
  if (!selectedGroup.value || !selectedPersonIds.value.length) return
  try {
    await ElMessageBox.confirm(
      `确定要从权限组中移除选中的 ${selectedPersonIds.value.length} 人吗？\n移除后将自动撤销这些人员在关联设备上的权限。`, 
      '提示', 
      { type: 'warning' }
    )
    // 调用带撤销的接口
    await AccessPermissionGroupApi.removePersonsWithRevoke(selectedGroup.value.id, selectedPersonIds.value)
    ElMessage.success('移除成功，正在撤销关联设备权限')
    selectedPersonIds.value = []
    loadGroupPersons()
  } catch (error) {
    if (error !== 'cancel') console.error('批量移除人员失败:', error)
  }
}

onMounted(() => {
  getList()
})
</script>


<style scoped lang="scss">
.permission-group-page {
  display: flex;
  height: 100%;
  background: var(--el-bg-color);
}

// 左侧面板 - 权限组列表
.left-panel {
  width: 320px;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--el-border-color-lighter);
  flex-shrink: 0;

  .panel-toolbar {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 12px;
    border-bottom: 1px solid var(--el-border-color-lighter);
    background: var(--el-fill-color-light);
  }

  .group-table {
    flex: 1;
    overflow: auto;

    :deep(.el-table) {
      --el-table-border-color: var(--el-border-color-lighter);

      .el-table__header-wrapper {
        th {
          background: var(--el-fill-color-light);
          font-weight: 500;
          font-size: 12px;
          padding: 8px 0;
        }
      }

      .el-table__body-wrapper {
        td {
          padding: 6px 0;
          font-size: 12px;
        }

        .el-table__row {
          cursor: pointer;

          &:hover {
            background: var(--el-fill-color-light);
          }

          &.current-row {
            background: var(--el-color-primary-light-9);
          }
        }
      }

      .el-button {
        padding: 4px;
      }
    }
  }

  .panel-pagination {
    padding: 8px 12px;
    border-top: 1px solid var(--el-border-color-lighter);
    display: flex;
    justify-content: center;
    background: var(--el-fill-color-light);
  }
}

// 右侧面板 - 关联信息
.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;

  .panel-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 16px;
    border-bottom: 1px solid var(--el-border-color-lighter);
    background: var(--el-fill-color-light);

    .panel-title {
      font-size: 14px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }

    .help-icon {
      color: var(--el-text-color-secondary);
      cursor: help;
    }
  }

  .panel-content {
    flex: 1;
    overflow: auto;
    padding: 16px;
  }

  .empty-state {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: var(--el-text-color-secondary);

    p {
      margin-top: 12px;
      font-size: 13px;
    }
  }
}

// 详情区域
.detail-section {
  margin-bottom: 16px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  overflow: hidden;

  .detail-header {
    padding: 10px 16px;
    background: var(--el-fill-color-light);
    font-size: 13px;
    font-weight: 500;
    color: var(--el-text-color-primary);
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .detail-body {
    padding: 12px 16px;

    .detail-row {
      display: flex;
      margin-bottom: 8px;
      font-size: 13px;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        color: var(--el-text-color-secondary);
        width: 80px;
        flex-shrink: 0;
      }

      .value {
        color: var(--el-text-color-primary);
      }
    }
  }
}

// 两列布局
.two-column-layout {
  display: flex;
  gap: 16px;
  height: calc(100% - 120px);
  min-height: 300px;

  .column {
    flex: 1;
    display: flex;
    flex-direction: column;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 4px;
    overflow: hidden;
    min-width: 0;

    .column-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 16px;
      background: var(--el-fill-color-light);
      font-size: 13px;
      font-weight: 500;
      color: var(--el-text-color-primary);
      border-bottom: 1px solid var(--el-border-color-lighter);
    }

    .column-body {
      flex: 1;
      overflow: auto;
      padding: 0;

      .device-list {
        .device-item {
          padding: 10px 16px;
          font-size: 13px;
          color: var(--el-text-color-primary);
          border-bottom: 1px solid var(--el-border-color-lighter);

          &:last-child {
            border-bottom: none;
          }

          &:hover {
            background: var(--el-fill-color-light);
          }
        }
      }

      .no-data {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 100%;
        min-height: 100px;
        color: var(--el-text-color-secondary);
        font-size: 13px;
      }

      :deep(.el-table) {
        --el-table-border-color: var(--el-border-color-lighter);

        .el-table__header-wrapper th {
          background: var(--el-fill-color-light);
          font-size: 12px;
          padding: 8px 0;
        }

        .el-table__body-wrapper td {
          font-size: 12px;
          padding: 6px 0;
        }
      }
    }
  }
}

// 操作按钮样式 - 三个按钮同一行
.action-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

// 弹窗样式
:deep(.el-dialog) {
  .el-dialog__header {
    border-bottom: 1px solid var(--el-border-color-lighter);
    padding: 16px 20px;
    margin: 0;
  }

  .el-dialog__body {
    padding: 20px;
  }

  .el-dialog__footer {
    border-top: 1px solid var(--el-border-color-lighter);
    padding: 12px 20px;
  }
}
</style>
