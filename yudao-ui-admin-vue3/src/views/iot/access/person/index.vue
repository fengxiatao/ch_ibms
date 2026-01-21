<template>
  <ContentWrap
    :body-style="{ padding: '0', height: '100%', display: 'flex', flexDirection: 'column' }"
    style="
      height: calc(100vh - var(--page-top-gap, 70px));
      padding-top: var(--page-top-gap, 70px);
      margin-bottom: 0;
    "
  >
    <div class="access-person-management">
      <!-- 主内容区 -->
      <div class="main-content">
        <!-- 人事管理内容 -->
        <div class="content-layout">
          <!-- 左侧部门树面板 -->
          <div class="dept-panel">
            <div class="panel-header">
              <span class="panel-title">部门组织树</span>
              <div class="panel-actions">
                <el-tooltip content="添加部门" placement="top">
                  <el-button link size="small" @click="handleAddDept"
                    ><Icon icon="ep:plus"
                  /></el-button>
                </el-tooltip>
                <el-tooltip content="编辑部门" placement="top">
                  <el-button link size="small" @click="handleEditDept"
                    ><Icon icon="ep:edit"
                  /></el-button>
                </el-tooltip>
                <el-tooltip content="删除部门" placement="top">
                  <el-button link size="small" @click="handleDeleteDept"
                    ><Icon icon="ep:delete"
                  /></el-button>
                </el-tooltip>
              </div>
            </div>
            <div class="dept-search">
              <el-input
                v-model="deptFilterText"
                placeholder="搜索部门..."
                clearable
                size="small"
                prefix-icon="ep:search"
              />
            </div>
            <div class="dept-tree-container">
              <el-tree
                ref="deptTreeRef"
                :data="deptTree"
                :props="{ label: 'deptName', children: 'children' }"
                node-key="id"
                default-expand-all
                highlight-current
                :filter-node-method="filterDeptNode"
                @node-click="handleDeptClick"
              >
                <template #default="{ node }">
                  <span class="tree-node">
                    <Icon icon="ep:folder" class="tree-icon" />
                    <span class="tree-label">{{ node.label }}</span>
                  </span>
                </template>
              </el-tree>
            </div>
          </div>

          <!-- 中间人员列表区域 -->
          <div class="person-panel">
            <!-- 工具栏 -->
            <div class="toolbar">
              <div class="toolbar-left">
                <el-button type="primary" size="small" @click="handleAdd">
                  <Icon icon="ep:plus" class="mr-4px" />添加
                </el-button>
                <el-button size="small" @click="handleBatchDelete">
                  <Icon icon="ep:delete" class="mr-4px" />删除
                </el-button>
                <el-divider direction="vertical" />
                <el-button size="small" @click="handleImport">
                  <Icon icon="ep:download" class="mr-4px" />导入
                </el-button>
                <el-button size="small" @click="handleExport">
                  <Icon icon="ep:upload" class="mr-4px" />导出
                </el-button>
                <el-divider direction="vertical" />
                <el-button size="small" @click="handleBatchAdd">批量添加</el-button>
                <el-button size="small" @click="handleBatchCard">批量发卡</el-button>
                <el-button size="small" @click="handleBatchEdit">批量编辑</el-button>
              </div>
              <div class="toolbar-right">
                <el-input
                  v-model="searchKeyword"
                  placeholder="编号/姓名..."
                  clearable
                  size="small"
                  style="width: 160px"
                  @keyup.enter="handleQuery"
                >
                  <template #prefix><Icon icon="ep:search" /></template>
                </el-input>
                <el-button-group class="ml-8px view-toggle">
                  <el-button
                    size="small"
                    :class="{ active: viewMode === 'table' }"
                    @click="viewMode = 'table'"
                  >
                    <Icon icon="ep:menu" />
                  </el-button>
                  <el-button
                    size="small"
                    :class="{ active: viewMode === 'card' }"
                    @click="viewMode = 'card'"
                  >
                    <Icon icon="ep:grid" />
                  </el-button>
                </el-button-group>
              </div>
            </div>

            <!-- 表格视图 -->
            <div class="table-container" v-if="viewMode === 'table'">
              <el-table
                v-loading="loading"
                :data="list"
                @selection-change="handleSelectionChange"
                @row-click="handleRowClick"
                :row-class-name="getRowClassName"
                height="100%"
                size="small"
              >
                <el-table-column type="selection" width="40" />
                <el-table-column label="编号" prop="personCode" width="120" />
                <el-table-column label="姓名" prop="personName" width="100" />
                <el-table-column label="人员类型" prop="personType" width="90">
                  <template #default="{ row }">
                    <el-tag size="small" :type="getPersonTypeTag(row.personType)">
                      {{ getPersonTypeLabel(row.personType) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column
                  label="部门"
                  prop="deptName"
                  min-width="120"
                  show-overflow-tooltip
                />
                <el-table-column label="指纹数" width="70" align="center">
                  <template #default="{ row }">{{ row.fingerprintCount || 0 }}</template>
                </el-table-column>
                <el-table-column label="操作" width="100" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="primary" size="small" @click.stop="handleEdit(row)"
                      >编辑</el-button
                    >
                    <el-button link type="danger" size="small" @click.stop="handleDelete(row)"
                      >删除</el-button
                    >
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <!-- 卡片视图 -->
            <div class="card-container" v-else>
              <div
                class="person-card"
                v-for="item in list"
                :key="item.id"
                :class="{ active: selectedPerson?.id === item.id }"
                @click="handleRowClick(item)"
              >
                <div class="card-avatar">
                  <el-avatar :size="50" :src="item.faceUrl">
                    <Icon icon="ep:user" :size="24" />
                  </el-avatar>
                </div>
                <div class="card-info">
                  <div class="card-name">{{ item.personName }}</div>
                  <div class="card-code">{{ item.personCode }}</div>
                </div>
              </div>
            </div>

            <!-- 分页 -->
            <div class="pagination-container">
              <span class="page-info">共 {{ total }} 条</span>
              <el-pagination
                v-model:current-page="queryParams.pageNo"
                v-model:page-size="queryParams.pageSize"
                :page-sizes="[20, 50, 100]"
                :total="total"
                layout="sizes, prev, pager, next"
                small
                @size-change="handleQuery"
                @current-change="getList"
              />
            </div>
          </div>

          <!-- 右侧详情面板 -->
          <PersonDetailPanel
            v-if="selectedPerson"
            :person="selectedPerson"
            :dept-tree="deptTree"
            @update="handlePersonUpdate"
            @close="selectedPerson = null"
          />
          <div v-else class="detail-placeholder">
            <Icon icon="ep:user" :size="48" />
            <p>请选择人员查看详情</p>
          </div>
        </div>
      </div>

      <!-- 人员表单弹窗 -->
      <PersonForm ref="personFormRef" @success="handleFormSuccess" :dept-tree="deptTree" />
      <!-- 部门表单弹窗 -->
      <DeptForm ref="deptFormRef" @success="loadDeptTree" />
      <!-- 隐藏的文件上传 -->
      <input
        ref="importInputRef"
        type="file"
        accept=".xlsx,.xls"
        style="display: none"
        @change="handleImportFile"
      />
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox, ElTree } from 'element-plus'
import {
  AccessPersonApi,
  AccessPersonTypeOptions,
  AccessDepartmentApi,
  type AccessPersonVO,
  type AccessDepartmentVO
} from '@/api/iot/access'
import PersonForm from './PersonForm.vue'
import DeptForm from './DeptForm.vue'
import PersonDetailPanel from './PersonDetailPanel.vue'

defineOptions({ name: 'AccessPersonManagement' })

const loading = ref(false)
const list = ref<AccessPersonVO[]>([])
const total = ref(0)
const deptTree = ref<AccessDepartmentVO[]>([])
const deptTreeRef = ref<InstanceType<typeof ElTree>>()
const deptFilterText = ref('')
const searchKeyword = ref('')
const viewMode = ref<'table' | 'card'>('table')
const selectedRows = ref<AccessPersonVO[]>([])
const selectedDept = ref<AccessDepartmentVO | null>(null)
const selectedPerson = ref<AccessPersonVO | null>(null)

const personFormRef = ref()
const deptFormRef = ref()

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  personCode: '',
  personName: '',
  deptId: undefined as number | undefined
})

// 获取人员类型标签
const getPersonTypeLabel = (type: number) => {
  const item = AccessPersonTypeOptions.find((i) => i.value === type)
  return item?.label || '未知'
}

const getPersonTypeTag = (type: number) => {
  const tags = { 1: '', 2: 'warning', 3: 'info', 4: 'success' }
  return tags[type] || 'info'
}

// 获取行样式
const getRowClassName = ({ row }: { row: AccessPersonVO }) => {
  return selectedPerson.value?.id === row.id ? 'selected-row' : ''
}

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    if (searchKeyword.value) {
      queryParams.personCode = searchKeyword.value
      queryParams.personName = searchKeyword.value
    } else {
      queryParams.personCode = ''
      queryParams.personName = ''
    }
    const res = await AccessPersonApi.getPersonPage(queryParams)
    list.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('获取人员列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载部门树
const loadDeptTree = async () => {
  try {
    deptTree.value = await AccessDepartmentApi.getDepartmentTree()
  } catch (error) {
    console.error('加载部门树失败:', error)
  }
}

// 部门树过滤
const filterDeptNode = (value: string, data: any) => {
  if (!value) return true
  return (data.deptName || '').includes(value)
}

watch(deptFilterText, (val) => {
  deptTreeRef.value?.filter(val)
})

// 部门点击
const handleDeptClick = (data: AccessDepartmentVO) => {
  selectedDept.value = data
  queryParams.deptId = data.id
  handleQuery()
}

// 搜索
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

// 表格选择
const handleSelectionChange = (rows: AccessPersonVO[]) => {
  selectedRows.value = rows
}

// 行点击 - 选中人员显示详情
const handleRowClick = (row: AccessPersonVO) => {
  selectedPerson.value = row
}

// 新增人员
const handleAdd = () => {
  personFormRef.value?.open('create', undefined, selectedDept.value?.id)
}

// 编辑人员
const handleEdit = (row: AccessPersonVO) => {
  personFormRef.value?.open('update', row.id)
}

// 删除人员
const handleDelete = async (row: AccessPersonVO) => {
  try {
    await ElMessageBox.confirm(`确定要删除人员"${row.personName}"吗？`, '提示', { type: 'warning' })
    await AccessPersonApi.deletePerson(row.id)
    ElMessage.success('删除成功')
    if (selectedPerson.value?.id === row.id) {
      selectedPerson.value = null
    }
    getList()
    // 刷新部门树以更新人员计数
    loadDeptTree()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的人员')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个人员吗？`, '提示', {
      type: 'warning'
    })
    loading.value = true
    const ids = selectedRows.value.map((r) => r.id)
    const results = await Promise.allSettled(ids.map((id) => AccessPersonApi.deletePerson(id)))
    const successCount = results.filter((r) => r.status === 'fulfilled').length
    const failCount = results.length - successCount
    if (failCount === 0) {
      ElMessage.success(`删除成功（${successCount} 人）`)
    } else {
      ElMessage.warning(`删除完成：成功 ${successCount} 人，失败 ${failCount} 人（请查看控制台）`)
      // 输出失败原因便于排查权限/被引用等问题
      results.forEach((r, idx) => {
        if (r.status === 'rejected') {
          console.error('[批量删除失败]', { id: ids[idx], error: r.reason })
        }
      })
    }
    // 清理右侧详情
    if (selectedPerson.value && ids.includes(selectedPerson.value.id)) {
      selectedPerson.value = null
    }
    getList()
    // 刷新部门树以更新人员计数
    loadDeptTree()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
    }
  } finally {
    loading.value = false
  }
}

// 表单成功回调
const handleFormSuccess = () => {
  getList()
  // 刷新部门树以更新人员计数
  loadDeptTree()
}

// 人员更新回调
const handlePersonUpdate = (person: AccessPersonVO) => {
  selectedPerson.value = person
  getList()
}

// 部门操作
const handleAddDept = () => {
  deptFormRef.value?.open('create', selectedDept.value?.id)
}

const handleDeleteDept = async () => {
  if (!selectedDept.value) {
    ElMessage.warning('请选择要删除的部门')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除部门"${selectedDept.value.deptName}"吗？`, '提示', {
      type: 'warning'
    })
    await AccessDepartmentApi.deleteDepartment(selectedDept.value.id)
    ElMessage.success('删除成功')
    selectedDept.value = null
    queryParams.deptId = undefined
    loadDeptTree()
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除部门失败:', error)
    }
  }
}

const handleEditDept = () => {
  if (!selectedDept.value) {
    ElMessage.warning('请选择要编辑的部门')
    return
  }
  deptFormRef.value?.open('update', selectedDept.value.id)
}

// 导入人员
const importInputRef = ref<HTMLInputElement>()
const handleImport = () => {
  importInputRef.value?.click()
}

const handleImportFile = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  try {
    loading.value = true
    await AccessPersonApi.importPersons(file)
    ElMessage.success('导入成功')
    getList()
  } catch (error) {
    console.error('导入失败:', error)
    ElMessage.error('导入失败')
  } finally {
    loading.value = false
    target.value = '' // 清空文件选择
  }
}

// 导出模板
const handleExport = async () => {
  try {
    await AccessPersonApi.exportTemplate()
    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('导出模板失败:', error)
  }
}

// 批量添加
const handleBatchAdd = () => {
  personFormRef.value?.open('create', undefined, selectedDept.value?.id)
}

// 批量发卡
const handleBatchCard = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要发卡的人员')
    return
  }
  ElMessage.info('批量发卡功能需要连接发卡器')
}

// 批量编辑
const handleBatchEdit = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要编辑的人员')
    return
  }
  ElMessage.info('批量编辑功能开发中')
}

onMounted(() => {
  loadDeptTree()
  getList()
})
</script>

<style scoped lang="scss">
.access-person-management {
  display: flex;
  height: calc(100vh - 84px);
  background: #1e1e2d;
  color: #c4c4c4;
}

// 主内容区
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-layout {
  display: flex;
  height: 100%;
}

// 部门面板
.dept-panel {
  width: 220px;
  background: #252532;
  border-right: 1px solid #2d2d3a;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;

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

    .panel-actions {
      display: flex;
      gap: 4px;

      .el-button {
        color: #8c8c9a;
        &:hover {
          color: #409eff;
        }
      }
    }
  }

  .dept-search {
    padding: 8px;
    border-bottom: 1px solid #2d2d3a;

    :deep(.el-input__wrapper) {
      background: #1e1e2d;
      box-shadow: none;
      border: 1px solid #3d3d4a;
    }
  }

  .dept-tree-container {
    flex: 1;
    overflow: auto;
    padding: 8px;

    :deep(.el-tree) {
      background: transparent;
      color: #c4c4c4;

      .el-tree-node__content {
        height: 32px;
        &:hover {
          background: #2d2d3a;
        }
      }

      .el-tree-node.is-current > .el-tree-node__content {
        background: #3d3d4a;
        color: #409eff;
      }
    }

    .tree-node {
      display: flex;
      align-items: center;
      font-size: 13px;

      .tree-icon {
        margin-right: 6px;
        color: #6c6c7a;
      }
    }
  }
}

// 人员面板
.person-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 500px;

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
    background: #1e1e2d;
    border-bottom: 1px solid #2d2d3a;
    flex-wrap: wrap;
    gap: 8px;

    .toolbar-left,
    .toolbar-right {
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .el-button {
      background: #2d2d3a;
      border-color: #3d3d4a;
      color: #c4c4c4;

      &:hover {
        background: #3d3d4a;
        border-color: #4d4d5a;
        color: #e0e0e0;
      }

      &.el-button--primary {
        background: #409eff;
        border-color: #409eff;
        color: #fff;
        &:hover {
          background: #66b1ff;
        }
      }
    }

    .el-divider--vertical {
      border-color: #3d3d4a;
    }

    :deep(.el-input__wrapper) {
      background: #2d2d3a;
      box-shadow: none;
      border: 1px solid #3d3d4a;
    }

    .view-toggle {
      .el-button {
        &.active {
          background: #409eff;
          border-color: #409eff;
          color: #fff;
        }
      }
    }
  }

  .table-container {
    flex: 1;
    overflow: hidden;
    padding: 8px 12px;

    :deep(.el-table) {
      background: transparent;
      --el-table-bg-color: transparent;
      --el-table-tr-bg-color: transparent;
      --el-table-header-bg-color: #1e1e2d;
      --el-table-row-hover-bg-color: #2d2d3a;
      --el-table-border-color: #2d2d3a;
      --el-table-text-color: #c4c4c4;
      --el-table-header-text-color: #e0e0e0;

      .selected-row {
        background: #2d3a4a !important;
      }

      th.el-table__cell {
        background: #1e1e2d;
        border-bottom: 1px solid #2d2d3a;
      }

      td.el-table__cell {
        border-bottom: 1px solid #2d2d3a;
      }
    }
  }

  .card-container {
    flex: 1;
    overflow: auto;
    padding: 12px;
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
    gap: 10px;
    align-content: start;

    .person-card {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 12px 8px;
      background: #2d2d3a;
      border: 1px solid #3d3d4a;
      border-radius: 4px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover,
      &.active {
        border-color: #409eff;
        background: #2d3a4a;
      }

      .card-info {
        text-align: center;
        margin-top: 8px;

        .card-name {
          font-size: 13px;
          color: #e0e0e0;
          margin-bottom: 2px;
        }

        .card-code {
          font-size: 11px;
          color: #8c8c9a;
        }
      }
    }
  }

  .pagination-container {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    padding: 8px 12px;
    background: #1e1e2d;
    border-top: 1px solid #2d2d3a;
    gap: 12px;

    .page-info {
      font-size: 12px;
      color: #8c8c9a;
    }

    :deep(.el-pagination) {
      --el-pagination-bg-color: #2d2d3a;
      --el-pagination-text-color: #c4c4c4;
      --el-pagination-button-bg-color: #2d2d3a;
      --el-pagination-hover-color: #409eff;
    }
  }
}

// 右侧详情占位
.detail-placeholder {
  width: 320px;
  background: #252532;
  border-left: 1px solid #2d2d3a;
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
</style>
