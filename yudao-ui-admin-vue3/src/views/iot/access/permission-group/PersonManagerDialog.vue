<template>
  <el-dialog 
    v-model="dialogVisible" 
    :title="`管理人员 - ${groupName}`" 
    width="900px" 
    append-to-body 
    class="dark-dialog"
    destroy-on-close
  >
    <div class="person-manager">
      <!-- 任务提示信息 - Requirements 1.4, 2.1 -->
      <el-alert 
        v-if="lastTaskId" 
        :title="`授权任务已创建，任务ID: ${lastTaskId}`"
        type="success" 
        show-icon 
        closable
        class="task-alert"
      >
        <template #default>
          <span>您可以在 </span>
          <el-button link type="primary" @click="goToTaskPage">授权任务</el-button>
          <span> 页面查看下发进度</span>
        </template>
      </el-alert>

      <!-- 已关联人员列表 -->
      <div class="section">
        <div class="section-header">
          <span class="title">已关联人员 ({{ groupPersons.length }})</span>
          <el-button 
            v-if="selectedRemoveIds.length" 
            type="danger" 
            size="small"
            @click="handleBatchRemove"
            :loading="removing"
          >
            移除选中 ({{ selectedRemoveIds.length }})
          </el-button>
        </div>
        <el-table
          :data="groupPersons"
          size="small"
          max-height="200"
          v-loading="loadingPersons"
          row-key="personId"
          @selection-change="handleRemoveSelectionChange"
        >
          <el-table-column type="selection" width="40" />
          <el-table-column label="编号" prop="personCode" width="100" />
          <el-table-column label="姓名" prop="personName" width="100" />
          <el-table-column label="部门" prop="deptName" show-overflow-tooltip />
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <el-button link type="danger" size="small" @click="handleRemovePerson(row)" :loading="removingPersonId === row.personId">
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分隔线 -->
      <el-divider>添加人员</el-divider>

      <!-- 添加人员区域 -->
      <div class="section add-section">
        <div class="selector-layout">
          <!-- 左侧部门树 -->
          <div class="dept-tree-panel">
            <div class="panel-header">部门</div>
            <div class="tree-container">
              <el-tree
                ref="deptTreeRef"
                :data="deptTree"
                :props="{ label: 'deptName', children: 'children' }"
                node-key="id"
                default-expand-all
                highlight-current
                @node-click="handleDeptClick"
              />
            </div>
          </div>

          <!-- 右侧人员列表 -->
          <div class="person-list-panel">
            <div class="panel-header">
              <span>可添加人员</span>
              <el-input 
                v-model="searchKeyword" 
                placeholder="搜索..." 
                size="small" 
                style="width: 150px" 
                clearable
                @input="handleSearch"
              >
                <template #prefix>
                  <Icon icon="ep:search" />
                </template>
              </el-input>
            </div>
            <el-table
              ref="addTableRef"
              :data="availablePersons"
              size="small"
              height="200"
              v-loading="loadingAvailable"
              @selection-change="handleAddSelectionChange"
            >
              <el-table-column type="selection" width="40" />
              <el-table-column label="编号" prop="personCode" width="100" />
              <el-table-column label="姓名" prop="personName" width="100" />
              <el-table-column label="部门" prop="deptName" show-overflow-tooltip />
            </el-table>
          </div>
        </div>

        <div class="selected-info">
          已选择 {{ selectedAddIds.length }} 人
          <el-button 
            v-if="selectedAddIds.length" 
            type="primary" 
            size="small" 
            class="ml-4"
            @click="handleBatchAdd"
            :loading="submitting"
          >
            添加并下发权限
          </el-button>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  AccessDepartmentApi, 
  AccessPersonApi, 
  AccessPermissionGroupApi, 
  type AccessDepartmentVO, 
  type AccessPersonVO 
} from '@/api/iot/access'

const router = useRouter()

const props = defineProps<{
  visible: boolean
  groupId?: number
  groupName?: string
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

const dialogVisible = ref(false)
const deptTree = ref<AccessDepartmentVO[]>([])
const allPersons = ref<AccessPersonVO[]>([])
const groupPersons = ref<AccessPersonVO[]>([])
const selectedAddIds = ref<number[]>([])
const selectedRemoveIds = ref<number[]>([])
const searchKeyword = ref('')
const submitting = ref(false)
const removing = ref(false)
const removingPersonId = ref<number>()
const loadingPersons = ref(false)
const loadingAvailable = ref(false)
const selectedDeptId = ref<number>()
const lastTaskId = ref<number>()  // 最近创建的任务ID - Requirements 1.4

// 已关联人员的ID集合
const groupPersonIdSet = computed(() => new Set(groupPersons.value.map(p => p.id)))

// 可添加的人员（排除已关联的）
const availablePersons = computed(() => {
  let list = allPersons.value.filter(p => !groupPersonIdSet.value.has(p.id))
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter(p => 
      p.personName?.toLowerCase().includes(keyword) ||
      p.personCode?.toLowerCase().includes(keyword)
    )
  }
  return list
})

watch(() => props.visible, (val) => {
  dialogVisible.value = val
  if (val && props.groupId) {
    loadDeptTree()
    loadGroupPersons()
    loadAllPersons()
  }
})

watch(dialogVisible, (val) => {
  emit('update:visible', val)
  if (!val) {
    // 关闭时重置状态
    selectedAddIds.value = []
    selectedRemoveIds.value = []
    searchKeyword.value = ''
    lastTaskId.value = undefined
  }
})

const loadDeptTree = async () => {
  try {
    deptTree.value = await AccessDepartmentApi.getDepartmentTree()
  } catch (error) {
    console.error('加载部门树失败:', error)
  }
}

const loadGroupPersons = async () => {
  if (!props.groupId) return
  loadingPersons.value = true
  try {
    const persons = await AccessPermissionGroupApi.getGroupPersons(props.groupId)
    groupPersons.value = persons || []
  } catch (error) {
    console.error('加载关联人员失败:', error)
    groupPersons.value = []
  } finally {
    loadingPersons.value = false
  }
}

const loadAllPersons = async (deptId?: number) => {
  loadingAvailable.value = true
  try {
    const res = await AccessPersonApi.getPersonPage({
      pageNo: 1,
      pageSize: 100,
      deptId
    })
    allPersons.value = res.list
  } catch (error) {
    console.error('加载人员列表失败:', error)
  } finally {
    loadingAvailable.value = false
  }
}

const handleDeptClick = (data: AccessDepartmentVO) => {
  selectedDeptId.value = data.id
  loadAllPersons(data.id)
}

const handleSearch = () => {
  // 搜索通过 computed 自动过滤
}

const handleAddSelectionChange = (rows: AccessPersonVO[]) => {
  selectedAddIds.value = rows.map(p => p.id)
}

const handleRemoveSelectionChange = (rows: any[]) => {
  // 注意：groupPersons 返回的是 IotAccessPermissionGroupPersonRespVO，字段是 personId 而不是 id
  selectedRemoveIds.value = rows.map(p => p.personId)
}

// 批量添加人员并触发授权下发 - Requirements 1.1, 1.4
const handleBatchAdd = async () => {
  if (!props.groupId || !selectedAddIds.value.length) return
  
  submitting.value = true
  try {
    // 调用带下发的接口 - Requirements 1.4
    const taskId = await AccessPermissionGroupApi.addPersonsWithDispatch(props.groupId, selectedAddIds.value)
    lastTaskId.value = taskId
    ElMessage.success(`成功添加 ${selectedAddIds.value.length} 人，授权任务ID: ${taskId}`)
    selectedAddIds.value = []
    loadGroupPersons()
    emit('success')
  } catch (error) {
    console.error('添加人员失败:', error)
    ElMessage.error('添加人员失败')
  } finally {
    submitting.value = false
  }
}

// 移除单个人员并触发权限撤销 - Requirements 2.1
const handleRemovePerson = async (person: any) => {
  if (!props.groupId) return
  try {
    await ElMessageBox.confirm(
      `确定要从权限组中移除"${person.personName}"吗？\n移除后将自动撤销该人员在关联设备上的权限。`, 
      '提示', 
      { type: 'warning' }
    )
    // 注意：groupPersons 返回的是 IotAccessPermissionGroupPersonRespVO，字段是 personId 而不是 id
    removingPersonId.value = person.personId
    // 调用带撤销的接口 - Requirements 2.1
    const taskId = await AccessPermissionGroupApi.removePersonsWithRevoke(props.groupId, [person.personId])
    lastTaskId.value = taskId
    ElMessage.success(`移除成功，撤销任务ID: ${taskId}`)
    loadGroupPersons()
    emit('success')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除人员失败:', error)
      ElMessage.error('移除人员失败')
    }
  } finally {
    removingPersonId.value = undefined
  }
}

// 批量移除人员并触发权限撤销 - Requirements 2.1
const handleBatchRemove = async () => {
  if (!props.groupId || !selectedRemoveIds.value.length) return
  try {
    await ElMessageBox.confirm(
      `确定要从权限组中移除选中的 ${selectedRemoveIds.value.length} 人吗？\n移除后将自动撤销这些人员在关联设备上的权限。`, 
      '提示', 
      { type: 'warning' }
    )
    removing.value = true
    // 调用带撤销的接口 - Requirements 2.1
    const taskId = await AccessPermissionGroupApi.removePersonsWithRevoke(props.groupId, selectedRemoveIds.value)
    lastTaskId.value = taskId
    ElMessage.success(`成功移除 ${selectedRemoveIds.value.length} 人，撤销任务ID: ${taskId}`)
    selectedRemoveIds.value = []
    loadGroupPersons()
    emit('success')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量移除人员失败:', error)
      ElMessage.error('批量移除人员失败')
    }
  } finally {
    removing.value = false
  }
}

// 跳转到授权任务页面查看进度 - Requirements 1.4
const goToTaskPage = () => {
  dialogVisible.value = false
  router.push('/smart-access/door/permission/auth-task')
}
</script>


<style scoped lang="scss">
.person-manager {
  .task-alert {
    margin-bottom: 16px;
  }

  .section {
    margin-bottom: 16px;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .title {
        font-size: 14px;
        font-weight: 500;
        color: var(--el-text-color-primary);
      }
    }
  }

  .add-section {
    .selector-layout {
      display: flex;
      gap: 16px;
      height: 280px;
    }

    .dept-tree-panel {
      width: 200px;
      background: var(--el-fill-color-light);
      border-radius: 4px;
      display: flex;
      flex-direction: column;
      border: 1px solid var(--el-border-color-lighter);

      .panel-header {
        padding: 10px 12px;
        background: var(--el-fill-color);
        border-bottom: 1px solid var(--el-border-color-lighter);
        font-size: 13px;
        font-weight: 500;
      }

      .tree-container {
        flex: 1;
        overflow: auto;
        padding: 8px;
      }
    }

    .person-list-panel {
      flex: 1;
      background: var(--el-fill-color-light);
      border-radius: 4px;
      display: flex;
      flex-direction: column;
      border: 1px solid var(--el-border-color-lighter);

      .panel-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 10px 12px;
        background: var(--el-fill-color);
        border-bottom: 1px solid var(--el-border-color-lighter);
        font-size: 13px;
        font-weight: 500;
      }
    }

    .selected-info {
      margin-top: 12px;
      font-size: 13px;
      color: var(--el-text-color-secondary);
      display: flex;
      align-items: center;
    }
  }
}

:deep(.el-divider__text) {
  background: var(--el-bg-color);
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
