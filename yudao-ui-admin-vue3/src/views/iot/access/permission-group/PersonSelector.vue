<template>
  <el-dialog v-model="dialogVisible" title="添加人员到权限组" width="700px" append-to-body class="dark-dialog">
    <div class="person-selector">
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
            <span>人员列表</span>
            <el-input v-model="searchKeyword" placeholder="搜索..." size="small" style="width: 150px" prefix-icon="ep:search" />
          </div>
          <el-table
            ref="tableRef"
            :data="personList"
            size="small"
            height="300"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="40" />
            <el-table-column label="编号" prop="personCode" width="100" />
            <el-table-column label="姓名" prop="personName" width="80" />
            <el-table-column label="部门" prop="deptName" show-overflow-tooltip />
          </el-table>
        </div>
      </div>

      <div class="selected-info">
        已选择 {{ selectedPersons.length }} 人
      </div>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleConfirm" :loading="submitting" :disabled="!selectedPersons.length">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { AccessDepartmentApi, AccessPersonApi, AccessPermissionGroupApi, type AccessDepartmentVO, type AccessPersonVO } from '@/api/iot/access'

const props = defineProps<{
  visible: boolean
  groupId?: number
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

const dialogVisible = ref(false)
const deptTree = ref<AccessDepartmentVO[]>([])
const personList = ref<AccessPersonVO[]>([])
const selectedPersons = ref<AccessPersonVO[]>([])
const searchKeyword = ref('')
const submitting = ref(false)
const deptTreeRef = ref()
const tableRef = ref()

watch(() => props.visible, (val) => {
  dialogVisible.value = val
  if (val) {
    loadDeptTree()
    loadPersonList()
  }
})

watch(dialogVisible, (val) => {
  emit('update:visible', val)
})

const loadDeptTree = async () => {
  try {
    deptTree.value = await AccessDepartmentApi.getDepartmentTree()
  } catch (error) {
    console.error('加载部门树失败:', error)
  }
}

const loadPersonList = async (deptId?: number) => {
  try {
    const res = await AccessPersonApi.getPersonPage({
      pageNo: 1,
      pageSize: 100,
      deptId
    })
    personList.value = res.list
  } catch (error) {
    console.error('加载人员列表失败:', error)
  }
}

const handleDeptClick = (data: AccessDepartmentVO) => {
  loadPersonList(data.id)
}

const handleSelectionChange = (rows: AccessPersonVO[]) => {
  selectedPersons.value = rows
}

const handleConfirm = async () => {
  if (!props.groupId || !selectedPersons.value.length) return

  submitting.value = true
  try {
    const personIds = selectedPersons.value.map(p => p.id)
    await AccessPermissionGroupApi.addPersons(props.groupId, personIds)
    ElMessage.success('添加成功')
    dialogVisible.value = false
    emit('success')
  } catch (error) {
    console.error('添加人员失败:', error)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.person-selector {
  .selector-layout {
    display: flex;
    gap: 16px;
    height: 350px;
  }

  .dept-tree-panel {
    width: 200px;
    background: #252532;
    border-radius: 4px;
    display: flex;
    flex-direction: column;

    .panel-header {
      padding: 10px 12px;
      background: #1e1e2d;
      border-bottom: 1px solid #2d2d3a;
      font-size: 13px;
      color: #e0e0e0;
    }

    .tree-container {
      flex: 1;
      overflow: auto;
      padding: 8px;

      :deep(.el-tree) {
        background: transparent;
        color: #c4c4c4;

        .el-tree-node__content {
          height: 30px;
          &:hover { background: #2d2d3a; }
        }

        .el-tree-node.is-current > .el-tree-node__content {
          background: #3d3d4a;
          color: #409eff;
        }
      }
    }
  }

  .person-list-panel {
    flex: 1;
    background: #252532;
    border-radius: 4px;
    display: flex;
    flex-direction: column;

    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 12px;
      background: #1e1e2d;
      border-bottom: 1px solid #2d2d3a;
      font-size: 13px;
      color: #e0e0e0;

      :deep(.el-input__wrapper) {
        background: #2d2d3a;
        box-shadow: none;
        border: 1px solid #3d3d4a;
      }
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

  .selected-info {
    margin-top: 12px;
    font-size: 12px;
    color: #8c8c9a;
  }
}
</style>
