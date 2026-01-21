<template>
  <div class="permission-config-inline">
    <div class="section">
      <div class="section-header">
        <span class="section-title">已分配权限组</span>
        <el-button type="primary" size="small" @click="showAddGroupDialog = true">添加权限组</el-button>
      </div>
      <el-table :data="assignedGroups" size="small" v-loading="loading">
        <el-table-column label="权限组名称" prop="groupName" />
        <el-table-column label="关联设备数" prop="deviceCount" width="100" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button link type="danger" size="small" @click="handleRemoveGroup(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="assignedGroups.length === 0 && !loading" class="no-data">暂未分配权限组</div>
    </div>

    <!-- 添加权限组对话框 -->
    <el-dialog v-model="showAddGroupDialog" title="添加权限组" width="500px" append-to-body>
      <el-table :data="availableGroups" size="small" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column label="权限组名称" prop="groupName" />
        <el-table-column label="关联设备数" prop="deviceCount" width="100" />
      </el-table>
      <template #footer>
        <el-button @click="showAddGroupDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddGroups" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AccessPermissionGroupApi, AccessPersonPermissionApi, type AccessPermissionGroupVO } from '@/api/iot/access'

const props = defineProps<{
  personId?: number
}>()

const loading = ref(false)
const submitting = ref(false)
const assignedGroups = ref<any[]>([])
const allGroups = ref<AccessPermissionGroupVO[]>([])
const availableGroups = ref<AccessPermissionGroupVO[]>([])
const selectedGroups = ref<AccessPermissionGroupVO[]>([])
const showAddGroupDialog = ref(false)

const loadData = async () => {
  if (!props.personId) return
  loading.value = true
  try {
    // 加载所有权限组
    const groupRes = await AccessPermissionGroupApi.getGroupPage({ pageNo: 1, pageSize: 100 })
    allGroups.value = groupRes.list || []
    
    // 加载人员已分配的权限
    const permissionRes = await AccessPersonPermissionApi.getPersonPermission(props.personId)
    assignedGroups.value = permissionRes?.groups || []
    
    // 计算可用权限组
    const assignedIds = new Set(assignedGroups.value.map((g: any) => g.groupId))
    availableGroups.value = allGroups.value.filter(g => !assignedIds.has(g.id))
  } catch (error) {
    console.error('加载权限数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (selection: AccessPermissionGroupVO[]) => {
  selectedGroups.value = selection
}

const handleAddGroups = async () => {
  if (selectedGroups.value.length === 0) {
    ElMessage.warning('请选择权限组')
    return
  }
  submitting.value = true
  try {
    const groupIds = selectedGroups.value.map(g => g.id)
    // 调用带下发的接口，向权限组关联的设备下发授权
    await AccessPersonPermissionApi.assignByGroupWithDispatch(props.personId!, groupIds)
    ElMessage.success('添加成功，正在向关联设备下发授权')
    showAddGroupDialog.value = false
    selectedGroups.value = [] // 清空选择
    await loadData()
  } catch (error) {
    console.error('添加权限组失败:', error)
    ElMessage.error('添加权限组失败，请重试')
  } finally {
    submitting.value = false
  }
}

const handleRemoveGroup = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要移除权限组"${row.groupName}"吗？\n移除后将自动撤销该人员在该权限组关联设备上的权限。`, 
      '提示', 
      { type: 'warning' }
    )
    loading.value = true
    // 调用带撤销的接口，从权限组关联的设备撤销授权
    await AccessPersonPermissionApi.removeGroupsWithRevoke(props.personId!, [row.groupId])
    ElMessage.success('移除成功，正在撤销关联设备权限')
    await loadData()
  } catch (error) {
    // 用户取消确认框时，error 为 'cancel'，不需要提示
    if (error !== 'cancel') {
      console.error('移除权限组失败:', error)
      ElMessage.error('移除权限组失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

watch(() => props.personId, () => {
  loadData()
}, { immediate: true })

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.permission-config-inline {
  padding: 10px 0;
}
.section {
  margin-bottom: 20px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.section-title {
  font-weight: 600;
  font-size: 14px;
}
.no-data {
  text-align: center;
  padding: 30px;
  color: #909399;
  font-size: 13px;
}
</style>
