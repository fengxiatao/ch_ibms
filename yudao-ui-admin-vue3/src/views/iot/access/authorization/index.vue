<template>
  <ContentWrap
    :body-style="{
      padding: '10px',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      backgroundColor: 'var(--el-bg-color)'
    }"
    style="height: calc(100vh - var(--page-top-gap, 70px)); padding-top: var(--page-top-gap, 70px)"
  >
    <!-- Tab切换和视图切换 -->
    <div class="mb-4 flex justify-between items-center">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="物管授权" name="1" />
        <el-tab-pane label="租户授权" name="2" />
      </el-tabs>
      <el-button-group>
        <el-button
          :type="viewMode === 'list' ? 'primary' : ''"
          @click="viewMode = 'list'"
        >
          <Icon icon="ep:list" class="mr-5px" /> 列表视图
        </el-button>
        <el-button
          :type="viewMode === 'org' ? 'primary' : ''"
          @click="viewMode = 'org'"
        >
          <Icon icon="ep:office-building" class="mr-5px" /> 组织视图
        </el-button>
      </el-button-group>
    </div>

    <!-- 搜索栏（仅列表视图显示） -->
    <el-form
      v-if="viewMode === 'list'"
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="68px"
    >
      <el-form-item label="授权状态" prop="authStatus">
        <el-select
          v-model="queryParams.authStatus"
          placeholder="请选择授权状态"
          clearable
          class="!w-240px"
        >
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" /> 搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" /> 重置
        </el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['iot:access-authorization:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['iot:access-authorization:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表视图 -->
  <ContentWrap v-if="viewMode === 'list'">
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="授权类型" align="center" prop="authType" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.authType === 1" type="primary">物管授权</el-tag>
          <el-tag v-else-if="scope.row.authType === 2" type="success">租户授权</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="人员ID" align="center" prop="personId" width="100" />
      <el-table-column label="设备ID" align="center" prop="deviceId" width="100" />
      <el-table-column label="门组ID" align="center" prop="doorGroupId" width="100" />
      <el-table-column label="授权时间" align="center" min-width="180">
        <template #default="scope">
          <div v-if="scope.row.startTime" class="text-xs">
            <div>{{ formatDate(scope.row.startTime) }}</div>
            <div v-if="scope.row.endTime" class="text-gray-500">
              至 {{ formatDate(scope.row.endTime) }}
            </div>
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="星期限制" align="center" width="120">
        <template #default="scope">
          <span v-if="scope.row.weekDays">{{ scope.row.weekDays }}</span>
          <span v-else>无限制</span>
        </template>
      </el-table-column>
      <el-table-column label="授权状态" align="center" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.authStatus"
            :active-value="1"
            :inactive-value="2"
            @change="handleStatusChange(scope.row)"
            v-hasPermi="['iot:access-authorization:update']"
          />
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column label="操作" align="center" width="180" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['iot:access-authorization:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['iot:access-authorization:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 组织视图 -->
  <ContentWrap v-if="viewMode === 'org'">
    <el-row :gutter="20">
      <!-- 左侧：部门树 -->
      <el-col :span="6">
        <el-card shadow="hover" class="org-tree-card">
          <template #header>
            <div class="card-header">
              <Icon icon="ep:office-building" />
              <span class="ml-2">组织机构</span>
            </div>
          </template>
          <el-input
            v-model="deptSearchText"
            placeholder="搜索部门"
            clearable
            class="mb-4"
            @input="handleDeptSearch"
          >
            <template #prefix>
              <Icon icon="ep:search" />
            </template>
          </el-input>
          <el-tree
            ref="deptTreeRef"
            :data="deptTree"
            :props="{ label: 'name', children: 'children' }"
            :filter-node-method="filterDeptNode"
            default-expand-all
            highlight-current
            node-key="id"
            @node-click="handleDeptNodeClick"
          v-loading="deptLoading"
        >
          <!-- 移除未使用的 slot 参数 data，修复 ESLint 规则 vue/no-unused-vars -->
          <template #default="{ node }">
            <span class="custom-tree-node">
              <Icon icon="ep:office-building" class="mr-2" />
              <span>{{ node.label }}</span>
            </span>
          </template>
        </el-tree>
        </el-card>
      </el-col>

      <!-- 右侧：授权详情面板 -->
      <el-col :span="18">
        <el-card shadow="hover" class="org-detail-card">
          <template #header>
            <div class="card-header">
              <Icon icon="ep:document" />
              <span class="ml-2">{{ selectedDept?.name || '请选择组织' }}</span>
              <el-button
                v-if="selectedDept"
                type="primary"
                size="small"
                class="ml-auto"
                @click="handleAuthorizeOrg"
                v-hasPermi="['iot:access-authorization:create']"
              >
                <Icon icon="ep:plus" class="mr-5px" /> 授权
              </el-button>
            </div>
          </template>
          <div v-if="selectedDept" v-loading="orgDetailLoading">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="组织名称">{{ selectedDept.name }}</el-descriptions-item>
              <el-descriptions-item label="组织ID">{{ selectedDept.id }}</el-descriptions-item>
              <el-descriptions-item label="授权状态" :span="2">
                <el-tag v-if="orgAuthInfo.authStatus === 1" type="success">已授权</el-tag>
                <el-tag v-else type="info">未授权</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="通行门禁" :span="2">
                <el-tag
                  v-for="device in orgAuthInfo.devices"
                  :key="device.id"
                  class="mr-2 mb-2"
                >
                  {{ device.name }}
                </el-tag>
                <span v-if="orgAuthInfo.devices.length === 0" class="text-gray-400">无</span>
              </el-descriptions-item>
              <el-descriptions-item label="通行门组" :span="2">
                <el-tag
                  v-for="group in orgAuthInfo.doorGroups"
                  :key="group.id"
                  type="success"
                  class="mr-2 mb-2"
                >
                  {{ group.name }}
                </el-tag>
                <span v-if="orgAuthInfo.doorGroups.length === 0" class="text-gray-400">无</span>
              </el-descriptions-item>
              <el-descriptions-item label="备注" :span="2">
                {{ orgAuthInfo.remark || '无' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
          <el-empty v-else description="请在左侧选择组织查看授权详情" />
        </el-card>
      </el-col>
    </el-row>
  </ContentWrap>

  <!-- 表单弹窗 -->
  <AuthorizationForm ref="formRef" @success="getList" />
  
  <!-- 组织授权弹窗 -->
  <OrgAuthDialog ref="orgAuthRef" @success="handleOrgAuthSuccess" />
</template>

<script setup lang="ts" name="AccessAuthorization">
import { dateFormatter, formatDate } from '@/utils/formatTime'
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as AccessAuthorizationApi from '@/api/iot/access/authorization'
import * as DeptApi from '@/api/system/dept'
import * as DeviceApi from '@/api/iot/device/device'
import * as DoorGroupApi from '@/api/iot/access/doorGroup'
import { handleTree } from '@/utils/tree'
import AuthorizationForm from './AuthorizationForm.vue'
import OrgAuthDialog from './OrgAuthDialog.vue'
import download from '@/utils/download'
import { ElTree } from 'element-plus'

const message = useMessage()

// Tab和视图模式
const activeTab = ref('1')
const viewMode = ref<'list' | 'org'>('list')

// 列表数据
const loading = ref(true)
const list = ref<AccessAuthorizationApi.AccessAuthorizationVO[]>([])
const total = ref(0)

// 查询参数
const queryParams = ref({
  pageNo: 1,
  pageSize: 10,
  authType: 1, // 默认物管授权
  authStatus: undefined
})

// Tab切换处理
const handleTabChange = (tabName: string) => {
  queryParams.value.authType = Number(tabName)
  queryParams.value.pageNo = 1
  if (viewMode.value === 'list') {
    getList()
  } else {
    // 组织视图切换Tab时，清空选中组织
    selectedDept.value = null
    orgAuthInfo.value = {
      authStatus: 0,
      devices: [],
      doorGroups: [],
      remark: ''
    }
  }
}

// 导出加载
const exportLoading = ref(false)

// 组织视图相关
const deptTree = ref<any[]>([])
const deptLoading = ref(false)
const deptSearchText = ref('')
const deptTreeRef = ref<InstanceType<typeof ElTree>>()
const selectedDept = ref<any>(null)
const orgDetailLoading = ref(false)
const orgAuthInfo = ref({
  authStatus: 0,
  devices: [] as any[],
  doorGroups: [] as any[],
  remark: ''
})
const orgAuthRef = ref()

// 搜索
const handleQuery = () => {
  queryParams.value.pageNo = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.value = {
    pageNo: 1,
    pageSize: 10,
    authType: undefined,
    authStatus: undefined
  }
  handleQuery()
}

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const data = await AccessAuthorizationApi.getAccessAuthorizationPage(queryParams.value)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

// 新增/修改操作
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

// 状态切换
const handleStatusChange = async (row: AccessAuthorizationApi.AccessAuthorizationVO) => {
  try {
    const text = row.authStatus === 1 ? '启用' : '停用'
    await message.confirm(`确认要${text}该授权吗?`)
    await AccessAuthorizationApi.updateAuthStatus(row.id!, row.authStatus)
    message.success(`${text}成功`)
  } catch {
    row.authStatus = row.authStatus === 1 ? 2 : 1
  }
}

// 删除操作
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await AccessAuthorizationApi.deleteAccessAuthorization(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

// 导出
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await AccessAuthorizationApi.exportAccessAuthorizationExcel(queryParams.value)
    download.excel(data, '门禁授权.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

// 加载部门树
const loadDeptTree = async () => {
  deptLoading.value = true
  try {
    const depts = await DeptApi.getDeptList()
    deptTree.value = handleTree(depts, 'id', 'parentId')
  } catch (error) {
    console.error('[授权管理] 加载部门树失败:', error)
  } finally {
    deptLoading.value = false
  }
}

// 部门搜索
const handleDeptSearch = (value: string) => {
  deptTreeRef.value?.filter(value)
}

// 部门节点过滤
const filterDeptNode = (value: string, data: any) => {
  if (!value) return true
  return data.name.includes(value)
}

// 部门节点点击
const handleDeptNodeClick = (data: any) => {
  selectedDept.value = data
  loadOrgAuthInfo(data.id)
}

// 加载组织授权信息
const loadOrgAuthInfo = async (orgId: number) => {
  orgDetailLoading.value = true
  try {
    // 查询该组织的授权记录
    const res = await AccessAuthorizationApi.getAccessAuthorizationPage({
      pageNo: 1,
      pageSize: 100,
      authType: Number(activeTab.value),
      orgId: orgId
    })
    
    if (res.list && res.list.length > 0) {
      // 有授权记录
      orgAuthInfo.value.authStatus = 1
      // 收集所有设备和门组
      const deviceIds = new Set<number>()
      const doorGroupIds = new Set<number>()
      
      res.list.forEach((auth: any) => {
        if (auth.deviceId) deviceIds.add(auth.deviceId)
        if (auth.doorGroupId) doorGroupIds.add(auth.doorGroupId)
      })
      
      // 加载设备和门组详情
      const devicePromises = Array.from(deviceIds).map(id =>
        DeviceApi.getDevice(id).catch(() => null)
      )
      const groupPromises = Array.from(doorGroupIds).map(id =>
        DoorGroupApi.getDoorGroup(id).catch(() => null)
      )
      
      const devices = await Promise.all(devicePromises)
      const groups = await Promise.all(groupPromises)
      
      orgAuthInfo.value.devices = devices
        .filter(d => d !== null)
        .map(d => ({
          id: d!.id,
          name: d!.deviceName || d!.nickname || `设备${d!.id}`
        }))
      
      orgAuthInfo.value.doorGroups = groups
        .filter(g => g !== null)
        .map(g => ({
          id: g!.id,
          name: g!.name || `门组${g!.id}`
        }))
      
      // 使用第一条记录的备注
      orgAuthInfo.value.remark = res.list[0].remark || ''
    } else {
      // 无授权记录
      orgAuthInfo.value = {
        authStatus: 0,
        devices: [],
        doorGroups: [],
        remark: ''
      }
    }
  } catch (error) {
    console.error('[授权管理] 加载组织授权信息失败:', error)
    orgAuthInfo.value = {
      authStatus: 0,
      devices: [],
      doorGroups: [],
      remark: ''
    }
  } finally {
    orgDetailLoading.value = false
  }
}

// 组织授权操作
const handleAuthorizeOrg = () => {
  if (!selectedDept.value) {
    ElMessage.warning('请先选择组织')
    return
  }
  orgAuthRef.value?.open(selectedDept.value, Number(activeTab.value))
}

// 组织授权成功回调
const handleOrgAuthSuccess = (orgId: number) => {
  if (selectedDept.value && selectedDept.value.id === orgId) {
    loadOrgAuthInfo(orgId)
  }
  // 如果是列表视图，也刷新列表
  if (viewMode.value === 'list') {
    getList()
  }
}

// 监听视图模式切换
watch(viewMode, (newMode) => {
  if (newMode === 'org') {
    loadDeptTree()
  }
})

// 监听部门搜索
watch(deptSearchText, (value) => {
  deptTreeRef.value?.filter(value)
})

// 初始化
onMounted(() => {
  getList()
})
</script>






