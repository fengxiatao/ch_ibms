<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧能源</el-breadcrumb-item>
        <el-breadcrumb-item>能耗配置</el-breadcrumb-item>
        <el-breadcrumb-item>能耗分区</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :model="searchForm" label-width="80px" :inline="true">
        <el-form-item label="分区名称">
          <el-input v-model="searchForm.zoneName" placeholder="请输入分区名称" clearable />
        </el-form-item>
        <el-form-item label="分区类型">
          <el-select v-model="searchForm.zoneType" placeholder="请选择分区类型" clearable>
            <el-option label="建筑" value="building" />
            <el-option label="楼层" value="floor" />
            <el-option label="房间" value="room" />
            <el-option label="功能区" value="functional" />
            <el-option label="部门" value="department" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="enabled" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <h2>能耗分区管理</h2>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增分区
        </el-button>
        <el-button type="success" @click="handleExpandAll">
          <el-icon><ArrowDown /></el-icon>
          展开全部
        </el-button>
        <el-button type="warning" @click="handleCollapseAll">
          <el-icon><ArrowUp /></el-icon>
          收起全部
        </el-button>
        <el-button type="info" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <!-- 分区树表 -->
    <div class="table-container">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="zoneList"
        stripe
        border
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        style="width: 100%"
      >
        <el-table-column prop="zoneName" label="分区名称" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="zone-name">
              <el-icon v-if="getZoneIcon(row.zoneType)" class="zone-icon">
                <component :is="getZoneIcon(row.zoneType)" />
              </el-icon>
              <span>{{ row.zoneName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="zoneCode" label="分区编码" width="120" />
        <el-table-column prop="zoneType" label="分区类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getZoneTypeColor(row.zoneType)">
              {{ getZoneTypeText(row.zoneType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="area" label="面积(m²)" width="100" />
        <el-table-column prop="deviceCount" label="设备数量" width="100" />
        <el-table-column prop="energyConsumption" label="能耗(kWh)" width="120">
          <template #default="{ row }">
            <span class="energy-value">{{ row.energyConsumption?.toLocaleString() || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="manager" label="负责人" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="enabled"
              inactive-value="disabled"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button link type="success" @click="handleAddChild(row)">
              <el-icon><Plus /></el-icon>
              添加子区
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑分区弹框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分区编码" prop="zoneCode">
              <el-input v-model="formData.zoneCode" placeholder="请输入分区编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分区名称" prop="zoneName">
              <el-input v-model="formData.zoneName" placeholder="请输入分区名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="上级分区" prop="parentId">
              <el-tree-select
                v-model="formData.parentId"
                :data="zoneTreeOptions"
                :props="{ label: 'zoneName', value: 'id' }"
                placeholder="请选择上级分区"
                clearable
                check-strictly
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分区类型" prop="zoneType">
              <el-select v-model="formData.zoneType" placeholder="请选择分区类型">
                <el-option label="建筑" value="building" />
                <el-option label="楼层" value="floor" />
                <el-option label="房间" value="room" />
                <el-option label="功能区" value="functional" />
                <el-option label="部门" value="department" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="面积(m²)" prop="area">
              <el-input-number
                v-model="formData.area"
                :precision="2"
                :min="0"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人" prop="manager">
              <el-input v-model="formData.manager" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio label="enabled">启用</el-radio>
                <el-radio label="disabled">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="分区描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分区描述"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="分区详情"
      width="1000px"
      destroy-on-close
    >
      <div v-if="currentZone" class="detail-content">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="分区编码">{{ currentZone.zoneCode }}</el-descriptions-item>
          <el-descriptions-item label="分区名称">{{ currentZone.zoneName }}</el-descriptions-item>
          <el-descriptions-item label="分区类型">
            <el-tag :type="getZoneTypeColor(currentZone.zoneType)">
              {{ getZoneTypeText(currentZone.zoneType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="面积">{{ currentZone.area }} m²</el-descriptions-item>
          <el-descriptions-item label="设备数量">{{ currentZone.deviceCount }} 个</el-descriptions-item>
          <el-descriptions-item label="能耗">{{ currentZone.energyConsumption?.toLocaleString() || 0 }} kWh</el-descriptions-item>
          <el-descriptions-item label="负责人">{{ currentZone.manager }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentZone.phone }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentZone.status === 'enabled' ? 'success' : 'danger'">
              {{ currentZone.status === 'enabled' ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
           <el-descriptions-item label="创建时间" :span="3">{{ currentZone.createTime }}</el-descriptions-item>
           <el-descriptions-item label="分区描述" :span="3">{{ currentZone.description || '无' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 子分区列表 -->
        <div v-if="currentZone.children && currentZone.children.length > 0" class="children-section">
          <h4>子分区列表</h4>
          <el-table :data="currentZone.children" size="small" border>
            <el-table-column prop="zoneName" label="分区名称" />
            <el-table-column prop="zoneType" label="分区类型">
              <template #default="{ row }">
                <el-tag :type="getZoneTypeColor(row.zoneType)" size="small">
                  {{ getZoneTypeText(row.zoneType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="area" label="面积(m²)" />
            <el-table-column prop="deviceCount" label="设备数量" />
            <el-table-column prop="manager" label="负责人" />
          </el-table>
        </div>

        <!-- 能耗设备列表 -->
        <div class="devices-section">
          <h4>关联设备</h4>
          <el-table :data="mockDevices" size="small" border>
            <el-table-column prop="deviceName" label="设备名称" />
            <el-table-column prop="deviceType" label="设备类型" />
            <el-table-column prop="currentValue" label="当前读数" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="row.status === 'online' ? 'success' : 'danger'" size="small">
                  {{ row.status === 'online' ? '在线' : '离线' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ContentWrap } from '@/components/ContentWrap'
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Refresh, ArrowDown, ArrowUp, Download, View, Edit, Delete,
  OfficeBuilding, House, Setting, Grid
} from '@element-plus/icons-vue'

// 类型定义
interface ZoneItem {
  id: string
  zoneCode: string
  zoneName: string
  parentId: string
  zoneType: string
  area: number
  deviceCount: number
  energyConsumption: number
  manager: string
  phone: string
  status: string
  createTime: string
  description: string
  children?: ZoneItem[]
  hasChildren?: boolean
}

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMode = ref<'add' | 'edit' | 'addChild'>('add')
const currentZone = ref<ZoneItem | null>(null)
const parentZone = ref<ZoneItem | null>(null)

// 搜索表单
const searchForm = reactive({
  zoneName: '',
  zoneType: '',
  status: ''
})

// 表单数据
const formData = reactive({
  id: '',
  zoneCode: '',
  zoneName: '',
  parentId: '',
  zoneType: '',
  area: 0,
  manager: '',
  phone: '',
  status: 'enabled',
  description: ''
})

// 表单验证规则
const formRules = {
  zoneCode: [{ required: true, message: '请输入分区编码', trigger: 'blur' }],
  zoneName: [{ required: true, message: '请输入分区名称', trigger: 'blur' }],
  zoneType: [{ required: true, message: '请选择分区类型', trigger: 'change' }],
  manager: [{ required: true, message: '请输入负责人', trigger: 'blur' }]
}

// 分区列表数据
const zoneList = ref<ZoneItem[]>([
  {
    id: '1',
    zoneCode: 'ZONE001',
    zoneName: '总部大楼',
    parentId: '',
    zoneType: 'building',
    area: 15000,
    deviceCount: 45,
    energyConsumption: 125800,
    manager: '张三',
    phone: '13800138000',
    status: 'enabled',
    createTime: '2024-01-15 09:30:00',
    description: '公司总部办公大楼',
    children: [
      {
        id: '2',
        zoneCode: 'ZONE002',
        zoneName: '1楼',
        parentId: '1',
        zoneType: 'floor',
        area: 3000,
        deviceCount: 8,
        energyConsumption: 18500,
        manager: '李四',
        phone: '13800138001',
        status: 'enabled',
        createTime: '2024-01-15 10:00:00',
        description: '1楼办公区域',
        children: [
          {
            id: '3',
            zoneCode: 'ZONE003',
            zoneName: '101会议室',
            parentId: '2',
            zoneType: 'room',
            area: 50,
            deviceCount: 2,
            energyConsumption: 850,
            manager: '王五',
            phone: '13800138002',
            status: 'enabled',
            createTime: '2024-01-15 10:30:00',
            description: '大型会议室'
          },
          {
            id: '4',
            zoneCode: 'ZONE004',
            zoneName: '财务部',
            parentId: '2',
            zoneType: 'department',
            area: 120,
            deviceCount: 3,
            energyConsumption: 2100,
            manager: '赵六',
            phone: '13800138003',
            status: 'enabled',
            createTime: '2024-01-15 11:00:00',
            description: '财务部办公区'
          }
        ]
      },
      {
        id: '5',
        zoneCode: 'ZONE005',
        zoneName: '2楼',
        parentId: '1',
        zoneType: 'floor',
        area: 3000,
        deviceCount: 12,
        energyConsumption: 25600,
        manager: '孙七',
        phone: '13800138004',
        status: 'enabled',
        createTime: '2024-01-15 11:30:00',
        description: '2楼办公区域'
      }
    ]
  },
  {
    id: '6',
    zoneCode: 'ZONE006',
    zoneName: '研发中心',
    parentId: '',
    zoneType: 'building',
    area: 8000,
    deviceCount: 28,
    energyConsumption: 65400,
    manager: '周八',
    phone: '13800138005',
    status: 'enabled',
    createTime: '2024-01-20 14:00:00',
    description: '技术研发中心大楼'
  }
])

// 模拟设备数据
const mockDevices = ref([
  { deviceName: '智能电表-001', deviceType: '电表', currentValue: '1250.5 kWh', status: 'online' },
  { deviceName: '空调系统-A1', deviceType: '空调', currentValue: '85.2 kW', status: 'online' },
  { deviceName: '照明系统-L1', deviceType: '照明', currentValue: '12.8 kW', status: 'offline' }
])

// 分区树形选项
const zoneTreeOptions = computed(() => {
  return buildTree(zoneList.value)
})

const tableRef = ref()
const formRef = ref()

// 构建树形结构
const buildTree = (data: any[]) => {
  return data.map(item => ({
    id: item.id,
    zoneName: item.zoneName,
    children: item.children ? buildTree(item.children) : []
  }))
}

// 获取分区类型图标
const getZoneIcon = (type: string) => {
  const icons = {
    building: OfficeBuilding,
    floor: House,
    room: Grid,
    functional: Setting,
    department: OfficeBuilding
  }
  return icons[type] || OfficeBuilding
}

// 获取分区类型颜色
const getZoneTypeColor = (type: string) => {
  const colors = {
    building: 'primary',
    floor: 'success',
    room: 'info',
    functional: 'warning',
    department: ''
  }
  return colors[type] || 'info'
}

// 获取分区类型文本
const getZoneTypeText = (type: string) => {
  const texts = {
    building: '建筑',
    floor: '楼层',
    room: '房间',
    functional: '功能区',
    department: '部门'
  }
  return texts[type] || type
}

// 事件处理
const handleSearch = () => {
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    zoneName: '',
    zoneType: '',
    status: ''
  })
  handleSearch()
}

const handleExpandAll = () => {
  if (tableRef.value) {
    // 遍历所有节点展开
    const expandRows = (data: any[]) => {
      data.forEach(row => {
        tableRef.value.toggleRowExpansion(row, true)
        if (row.children && row.children.length > 0) {
          expandRows(row.children)
        }
      })
    }
    expandRows(zoneList.value)
  }
}

const handleCollapseAll = () => {
  if (tableRef.value) {
    // 遍历所有节点收起
    const collapseRows = (data: any[]) => {
      data.forEach(row => {
        tableRef.value.toggleRowExpansion(row, false)
        if (row.children && row.children.length > 0) {
          collapseRows(row.children)
        }
      })
    }
    collapseRows(zoneList.value)
  }
}

const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

const handleStatusChange = (row: any) => {
  const statusText = row.status === 'enabled' ? '启用' : '禁用'
  ElMessage.success(`${statusText}成功`)
}

const handleCreate = () => {
  dialogMode.value = 'add'
  dialogTitle.value = '新增分区'
  parentZone.value = null
  resetFormData()
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑分区'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleView = (row: any) => {
  currentZone.value = row
  detailDialogVisible.value = true
}

const handleAddChild = (row: any) => {
  dialogMode.value = 'addChild'
  dialogTitle.value = `新增子分区 - ${row.zoneName}`
  parentZone.value = row
  resetFormData()
  formData.parentId = row.id
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  if (row.children && row.children.length > 0) {
    ElMessage.warning('该分区下还有子分区，无法删除')
    return
  }

  try {
    await ElMessageBox.confirm('确认删除该分区吗？删除后无法恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 递归删除节点
    const deleteNode = (data: any[], nodeId: string): boolean => {
      for (let i = 0; i < data.length; i++) {
        if (data[i].id === nodeId) {
          data.splice(i, 1)
          return true
        }
        if (data[i].children && deleteNode(data[i].children, nodeId)) {
          return true
        }
      }
      return false
    }
    
    deleteNode(zoneList.value, row.id)
    ElMessage.success('删除成功')
  } catch {
    // 用户取消删除
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    const newZone = {
      ...formData,
      id: `zone_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      deviceCount: 0,
      energyConsumption: 0,
      createTime: new Date().toLocaleString('zh-CN'),
      children: []
    }
    
    if (dialogMode.value === 'add') {
      if (formData.parentId) {
        // 添加到指定父节点
        const addToParent = (data: any[], parentId: string, node: any): boolean => {
          for (const item of data) {
            if (item.id === parentId) {
              if (!item.children) item.children = []
              item.children.push(node)
              return true
            }
            if (item.children && addToParent(item.children, parentId, node)) {
              return true
            }
          }
          return false
        }
        addToParent(zoneList.value, formData.parentId, newZone)
      } else {
        // 添加到根级
        zoneList.value.push(newZone)
      }
      ElMessage.success('创建成功')
     } else if (dialogMode.value === 'addChild') {
       // 添加到指定父节点
       if (parentZone.value) {
         if (!parentZone.value.children) {
           parentZone.value.children = []
         }
         parentZone.value.children.push(newZone)
       }
      ElMessage.success('创建成功')
    } else if (dialogMode.value === 'edit') {
      // 递归更新节点
      const updateNode = (data: any[], nodeId: string, newData: any): boolean => {
        for (const item of data) {
          if (item.id === nodeId) {
            Object.assign(item, newData)
            return true
          }
          if (item.children && updateNode(item.children, nodeId, newData)) {
            return true
          }
        }
        return false
      }
      updateNode(zoneList.value, formData.id, formData)
      ElMessage.success('更新成功')
    }
    
    dialogVisible.value = false
  } catch {
    // 表单验证失败
  }
}

const resetFormData = () => {
  Object.assign(formData, {
    id: '',
    zoneCode: '',
    zoneName: '',
    parentId: '',
    zoneType: '',
    area: 0,
    manager: '',
    phone: '',
    status: 'enabled',
    description: ''
  })
}

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">@use '@/styles/dark-theme.scss';

.app-container {
  padding: 20px;

  .breadcrumb-container {
    margin-bottom: 20px;
  }

  .search-container {
    background: #1a1a1a;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    margin-bottom: 20px;

    .el-form-item {
      margin-bottom: 16px;
    }
  }

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 20px;
    background: #1a1a1a;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .header-title {
      h2 {
        margin: 0;
        color: #303133;
        font-size: 24px;
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .table-container {
    background: #1a1a1a;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .zone-name {
      display: flex;
      align-items: center;
      gap: 8px;

      .zone-icon {
        color: #409eff;
      }
    }

    .energy-value {
      font-weight: 500;
      color: #e6a23c;
    }
  }
}

.detail-content {
  .el-descriptions {
    margin-bottom: 24px;
  }

  .children-section,
  .devices-section {
    margin-top: 24px;

    h4 {
      margin: 0 0 16px 0;
      color: #303133;
      font-size: 16px;
      font-weight: 600;
      border-left: 3px solid #409eff;
      padding-left: 12px;
    }
  }
}
</style>






