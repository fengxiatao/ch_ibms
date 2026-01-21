<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="circuit-control dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧建筑</el-breadcrumb-item>
        <el-breadcrumb-item>智能照明</el-breadcrumb-item>
        <el-breadcrumb-item>回路控制</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="main-content">
      <el-row :gutter="20">
        <!-- 左侧空间机构树 -->
        <el-col :span="6">
          <el-card class="tree-card" shadow="never">
            <template #header>
              <div class="tree-header">
                <span>空间机构</span>
                <el-button link type="primary" @click="expandAll">
                  {{ isExpandAll ? '全部收起' : '全部展开' }}
                </el-button>
              </div>
            </template>
            
            <div class="search-box">
              <el-input
                v-model="searchKeyword"
                placeholder="请输入关键字"
                clearable
                @input="handleSearch"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>

            <el-tree
              ref="treeRef"
              :data="treeData"
              :props="treeProps"
              :filter-node-method="filterNode"
              :expand-on-click-node="false"
              highlight-current
              @node-click="handleNodeClick"
            >
              <template #default="{ node, data }">
                <div class="tree-node">
                  <el-icon>
                    <component :is="getNodeIcon(data)" />
                  </el-icon>
                  <span class="node-label">{{ node.label }}</span>
                  <span class="node-count" v-if="data.children && data.children.length > 0">
                    {{ data.children.length }}
                  </span>
                </div>
              </template>
            </el-tree>
          </el-card>
        </el-col>

        <!-- 右侧场景控制区域 -->
        <el-col :span="18">
          <div class="scene-control-area">
            <!-- 场景控制标题 -->
            <div class="control-header">
              <h3>场景控制</h3>
              <div class="header-actions">
                <el-button type="primary" @click="handleAddScene">
                  <el-icon><Plus /></el-icon>
                  新增
                </el-button>
              </div>
            </div>

            <!-- 场景卡片列表 -->
            <div class="scene-grid">
              <div
                v-for="scene in filteredScenes"
                :key="scene.id"
                class="scene-card"
                @click="handleSceneClick(scene)"
              >
                <div class="scene-card-header">
                  <div class="scene-icon">
                    <el-icon>
                      <component :is="getSceneIcon(scene.type)" />
                    </el-icon>
                  </div>
                  <div class="scene-actions">
                    <el-button link type="primary" @click.stop="handleEditScene(scene)">
                      <el-icon><Edit /></el-icon>
                    </el-button>
                    <el-dropdown @command="(command) => handleSceneCommand(command, scene)" trigger="click">
                      <el-button link type="primary" @click.stop>
                        <el-icon><MoreFilled /></el-icon>
                      </el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item command="copy">复制</el-dropdown-item>
                          <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </div>
                </div>

                <div class="scene-info">
                  <h4 class="scene-name">{{ scene.name }}</h4>
                  <div class="scene-location">
                    <el-icon><Location /></el-icon>
                    <span>{{ scene.location }}</span>
                  </div>
                </div>

                <div class="scene-stats">
                  <div class="stat-item">
                    <span class="stat-label">模式数量</span>
                    <span class="stat-value">{{ scene.modeCount }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">回路数量</span>
                    <span class="stat-value">{{ scene.circuitCount }}</span>
                  </div>
                </div>

                <div class="scene-controls">
                  <el-button
                    :type="scene.status === 'active' ? 'success' : 'primary'"
                    size="small"
                    @click.stop="handleToggleScene(scene)"
                  >
                    {{ scene.status === 'active' ? '切换模式' : '启用场景' }}
                  </el-button>
                </div>
              </div>
            </div>

            <!-- 分页 -->
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.size"
              :total="pagination.total"
              :page-sizes="[20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              class="pagination"
              @size-change="loadScenes"
              @current-change="loadScenes"
            />
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 场景详情弹框 -->
    <el-dialog v-model="sceneDialogVisible" :title="currentScene?.name || '场景详情'" width="80%" draggable>
      <div v-if="currentScene" class="scene-detail">
        <!-- 基础信息 -->
        <div class="detail-section">
          <h4>基础信息</h4>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="场景名称">{{ currentScene.name }}</el-descriptions-item>
            <el-descriptions-item label="场景类型">{{ getSceneTypeText(currentScene.type) }}</el-descriptions-item>
            <el-descriptions-item label="所在位置">{{ currentScene.location }}</el-descriptions-item>
            <el-descriptions-item label="执行时间">{{ currentScene.executeTime || '手动执行' }}</el-descriptions-item>
            <el-descriptions-item label="执行周期">{{ currentScene.executeCycle || '无' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentScene.status === 'active' ? 'success' : 'info'">
                {{ currentScene.status === 'active' ? '已启用' : '未启用' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 执行回路 -->
        <div class="detail-section">
          <h4>执行回路</h4>
          <el-table :data="currentScene.circuits || []" border>
            <el-table-column prop="serialNumber" label="序号" width="80" />
            <el-table-column prop="deviceCode" label="设备编号" width="150" />
            <el-table-column prop="deviceName" label="设备名称" width="180" />
            <el-table-column prop="deviceType" label="专业系统" width="120" />
            <el-table-column prop="location" label="空间位置" width="150" />
            <el-table-column prop="controllerType" label="所属回路1" width="120" />
            <el-table-column prop="snCode" label="SN编号" width="150" />
            <el-table-column prop="deviceStatus" label="设备状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.deviceStatus === '正常' ? 'success' : 'danger'">
                  {{ row.deviceStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="controlStatus" label="物联设备" width="100" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleControlDevice(row)">
                  <el-icon><Operation /></el-icon>
                  控制
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="sceneDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleExecuteCurrentScene">执行场景</el-button>
      </template>
    </el-dialog>

    <!-- 场景新增/编辑弹框 -->
    <el-dialog v-model="editDialogVisible" :title="isEditMode ? '编辑场景' : '新增场景'" width="70%" draggable>
      <el-form ref="sceneFormRef" :model="sceneForm" :rules="sceneRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="场景名称" prop="name">
              <el-input v-model="sceneForm.name" placeholder="请输入场景名称" />
            </el-form-item>
            <el-form-item label="场景类型" prop="type">
              <el-select v-model="sceneForm.type" placeholder="请选择场景类型" style="width: 100%;">
                <el-option label="会议场景" value="meeting" />
                <el-option label="办公场景" value="office" />
                <el-option label="清洁场景" value="cleaning" />
                <el-option label="节能场景" value="energy_saving" />
                <el-option label="安防场景" value="security" />
              </el-select>
            </el-form-item>
            <el-form-item label="所在位置" prop="location">
              <el-input v-model="sceneForm.location" placeholder="请输入位置" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="执行时间">
              <el-time-picker
                v-model="sceneForm.executeTime"
                placeholder="请选择执行时间"
                style="width: 100%;"
              />
            </el-form-item>
            <el-form-item label="执行周期">
              <el-checkbox-group v-model="sceneForm.executeDays">
                <el-checkbox label="周一">周一</el-checkbox>
                <el-checkbox label="周二">周二</el-checkbox>
                <el-checkbox label="周三">周三</el-checkbox>
                <el-checkbox label="周四">周四</el-checkbox>
                <el-checkbox label="周五">周五</el-checkbox>
                <el-checkbox label="周六">周六</el-checkbox>
                <el-checkbox label="周日">周日</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveScene">确定</el-button>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ContentWrap } from '@/components/ContentWrap'
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Plus, Edit, MoreFilled, Location, Operation,
  OfficeBuilding, Grid, House, Cpu
} from '@element-plus/icons-vue'

// 响应式数据
const treeRef = ref()
const isExpandAll = ref(false)
const searchKeyword = ref('')
const sceneDialogVisible = ref(false)
const editDialogVisible = ref(false)
const isEditMode = ref(false)
const currentScene = ref(null)
const selectedNode = ref(null)

// 分页数据
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 表单数据
const sceneForm = reactive({
  name: '',
  type: '',
  location: '',
  executeTime: null,
  executeDays: []
})

const sceneRules = {
  name: [{ required: true, message: '请输入场景名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择场景类型', trigger: 'change' }],
  location: [{ required: true, message: '请输入位置', trigger: 'blur' }]
}

// 树形数据配置
const treeProps = {
  children: 'children',
  label: 'label'
}

// 空间机构树数据
const treeData = ref([
  {
    label: 'ces',
    value: 'ces',
    type: 'building',
    children: [
      {
        label: 'dsd',
        value: 'dsd',
        type: 'floor',
        children: [
          { label: '会议室', value: 'meeting_room', type: 'room' },
          { label: '办公区', value: 'office_area', type: 'room' }
        ]
      },
      {
        label: '2',
        value: '2',
        type: 'floor',
        children: [
          { label: '接待室', value: 'reception', type: 'room' },
          { label: '休息区', value: 'rest_area', type: 'room' }
        ]
      }
    ]
  },
  {
    label: '南区',
    value: 'south_area',
    type: 'area',
    children: [
      {
        label: '55',
        value: '55',
        type: 'floor',
        children: [
          { label: '实验室', value: 'lab', type: 'room' },
          { label: '存储室', value: 'storage', type: 'room' }
        ]
      }
    ]
  },
  {
    label: '室外',
    value: 'outdoor',
    type: 'outdoor',
    children: [
      { label: '停车场', value: 'parking', type: 'area' },
      { label: '花园', value: 'garden', type: 'area' }
    ]
  },
  {
    label: '北区',
    value: 'north_area',
    type: 'area',
    children: [
      {
        label: '节能模式',
        value: 'energy_mode',
        type: 'mode',
        children: [
          { label: '夜间模式', value: 'night_mode', type: 'scene' },
          { label: '待机模式', value: 'standby_mode', type: 'scene' }
        ]
      },
      {
        label: '会议照明',
        value: 'meeting_lighting',
        type: 'mode',
        children: [
          { label: '演示模式', value: 'presentation_mode', type: 'scene' },
          { label: '讨论模式', value: 'discussion_mode', type: 'scene' }
        ]
      },
      {
        label: '餐厅照明',
        value: 'dining_lighting',
        type: 'mode',
        children: [
          { label: '用餐模式', value: 'dining_mode', type: 'scene' },
          { label: '清洁模式', value: 'cleaning_mode', type: 'scene' }
        ]
      }
    ]
  }
])

// 场景数据
const sceneList = ref([
  {
    id: 1,
    name: 'dsd',
    type: 'office',
    location: 'ces',
    modeCount: 1,
    circuitCount: 2,
    status: 'active',
    executeTime: '02:00:00',
    executeCycle: '星期二, 星期日',
    circuits: [
      {
        serialNumber: 1,
        deviceCode: 'JZ_ZM_PTHL_0010',
        deviceName: '普通照明11',
        deviceType: '普通回路',
        location: '南区_科技研发楼_9F_办公区',
        controllerType: '物业公司测试_产品部',
        snCode: '-',
        deviceStatus: '正常',
        controlStatus: '是'
      },
      {
        serialNumber: 2,
        deviceCode: 'JZ_ZM_PTHL_0009',
        deviceName: '云物-普通照明1',
        deviceType: '普通回路',
        location: '南区_科技研发楼_9F_办公区',
        controllerType: '物业公司测试_产品部',
        snCode: '-',
        deviceStatus: '正常',
        controlStatus: '是'
      }
    ]
  },
  {
    id: 2,
    name: '2',
    type: 'meeting',
    location: 'ces',
    modeCount: 3,
    circuitCount: 3,
    status: 'inactive',
    circuits: [
      {
        serialNumber: 1,
        deviceCode: 'JZ_ZM_PTHL_0008',
        deviceName: '普通照明10',
        deviceType: '普通回路',
        location: '南区_科技研发楼_9F_办公区',
        controllerType: '-',
        snCode: '-',
        deviceStatus: '正常',
        controlStatus: '是'
      }
    ]
  },
  {
    id: 3,
    name: '55',
    type: 'energy_saving',
    location: '南区',
    modeCount: 2,
    circuitCount: 2,
    status: 'inactive',
    circuits: []
  },
  {
    id: 4,
    name: '节能模式',
    type: 'energy_saving',
    location: '南区_科技研发楼',
    modeCount: 1,
    circuitCount: 3,
    status: 'inactive',
    circuits: []
  },
  {
    id: 5,
    name: '会议照明',
    type: 'meeting',
    location: '南区_科技研发楼',
    modeCount: 2,
    circuitCount: 2,
    status: 'inactive',
    circuits: []
  },
  {
    id: 6,
    name: '餐厅照明',
    type: 'cleaning',
    location: '南区',
    modeCount: 2,
    circuitCount: 3,
    status: 'inactive',
    circuits: []
  }
])

// 计算属性
const filteredScenes = computed(() => {
  if (!selectedNode.value) {
    return sceneList.value
  }
  // 根据选中的节点过滤场景
  return sceneList.value.filter(scene => {
    return scene.location.includes(selectedNode.value.label) || 
           scene.name.includes(selectedNode.value.label)
  })
})

// 方法
const getNodeIcon = (data) => {
  const iconMap = {
    building: OfficeBuilding,
    floor: Grid,
    room: House,
    area: OfficeBuilding,
    outdoor: House,
    mode: Cpu,
    scene: Grid
  }
  return iconMap[data.type] || House
}

const getSceneIcon = (type) => {
  const iconMap = {
    meeting: OfficeBuilding,
    office: Grid,
    cleaning: House,
    energy_saving: Cpu,
    security: OfficeBuilding
  }
  return iconMap[type] || Grid
}

const getSceneTypeText = (type) => {
  const textMap = {
    meeting: '会议场景',
    office: '办公场景',
    cleaning: '清洁场景',
    energy_saving: '节能场景',
    security: '安防场景'
  }
  return textMap[type] || type
}

const expandAll = () => {
  isExpandAll.value = !isExpandAll.value
  if (isExpandAll.value) {
    treeRef.value?.setExpandedKeys(getAllNodeKeys(treeData.value))
  } else {
    treeRef.value?.setExpandedKeys([])
  }
}

const getAllNodeKeys = (nodes) => {
  const keys = []
  const traverse = (nodeList) => {
    nodeList.forEach(node => {
      keys.push(node.value)
      if (node.children) {
        traverse(node.children)
      }
    })
  }
  traverse(nodes)
  return keys
}

const handleSearch = (value) => {
  treeRef.value?.filter(value)
}

const filterNode = (value, data) => {
  if (!value) return true
  return data.label.toLowerCase().includes(value.toLowerCase())
}

const handleNodeClick = (data) => {
  selectedNode.value = data
  loadScenes()
}

const handleSceneClick = (scene) => {
  currentScene.value = scene
  sceneDialogVisible.value = true
}

const handleAddScene = () => {
  isEditMode.value = false
  Object.assign(sceneForm, {
    name: '',
    type: '',
    location: selectedNode.value?.label || '',
    executeTime: null,
    executeDays: []
  })
  editDialogVisible.value = true
}

const handleEditScene = (scene) => {
  isEditMode.value = true
  Object.assign(sceneForm, {
    name: scene.name,
    type: scene.type,
    location: scene.location,
    executeTime: scene.executeTime || null,
    executeDays: scene.executeCycle ? scene.executeCycle.split(', ') : []
  })
  editDialogVisible.value = true
}

const handleSceneCommand = async (command, scene) => {
  if (command === 'copy') {
    ElMessage.success(`复制场景 "${scene.name}" 成功`)
  } else if (command === 'delete') {
    try {
      await ElMessageBox.confirm(`确认删除场景 "${scene.name}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      const index = sceneList.value.findIndex(s => s.id === scene.id)
      if (index > -1) {
        sceneList.value.splice(index, 1)
      }
      ElMessage.success('删除成功')
    } catch {
      // 用户取消
    }
  }
}

const handleToggleScene = async (scene) => {
  if (scene.status === 'active') {
    // 显示切换模式选项
    ElMessage.info('切换模式功能开发中...')
  } else {
    try {
      await ElMessageBox.confirm(`确认启用场景 "${scene.name}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      })
      
      // 先将所有场景设为非活跃状态
      sceneList.value.forEach(s => {
        s.status = 'inactive'
      })
      
      // 启用当前场景
      scene.status = 'active'
      ElMessage.success(`场景 "${scene.name}" 启用成功`)
    } catch {
      // 用户取消
    }
  }
}

const handleControlDevice = (device) => {
  ElMessage.success(`控制设备 "${device.deviceName}" 成功`)
}

const handleExecuteCurrentScene = () => {
  if (currentScene.value) {
    ElMessage.success(`执行场景 "${currentScene.value.name}" 成功`)
    sceneDialogVisible.value = false
  }
}

const handleSaveScene = () => {
  if (!sceneForm.name || !sceneForm.type || !sceneForm.location) {
    ElMessage.warning('请完善场景信息')
    return
  }

  if (isEditMode.value) {
    ElMessage.success('场景修改成功')
  } else {
    const newScene = {
      id: Date.now(),
      ...sceneForm,
      modeCount: 1,
      circuitCount: Math.floor(Math.random() * 5) + 1,
      status: 'inactive',
      circuits: []
    }
    sceneList.value.push(newScene)
    ElMessage.success('场景创建成功')
  }
  
  editDialogVisible.value = false
}

const loadScenes = () => {
  // 模拟加载数据
  pagination.total = filteredScenes.value.length
}

onMounted(() => {
  loadScenes()
})
</script>

<style scoped lang="scss">
@use '@/styles/dark-theme.scss';

.circuit-control {
  padding: 20px;

  .breadcrumb-container {
    margin-bottom: 20px;
  }

  .main-content {
    .tree-card {
      height: calc(100vh - 200px);

      .tree-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-weight: 600;
      }

      .search-box {
        margin-bottom: 16px;
      }

      .tree-node {
        display: flex;
        align-items: center;
        width: 100%;

        .el-icon {
          margin-right: 8px;
          color: #409eff;
        }

        .node-label {
          flex: 1;
        }

        .node-count {
          background: #f0f2f5;
          color: #606266;
          padding: 2px 8px;
          border-radius: 10px;
          font-size: 12px;
          margin-left: 8px;
        }
      }
    }

    .scene-control-area {
      .control-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;

        h3 {
          margin: 0;
          color: #303133;
          font-size: 18px;
        }

        .header-actions {
          display: flex;
          gap: 10px;
        }
      }

      .scene-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
        gap: 20px;
        margin-bottom: 20px;

        .scene-card {
          background: #2d2d2d;
          border: 1px solid #404040;
          border-radius: 12px;
          padding: 20px;
          cursor: pointer;
          transition: all 0.3s;

          &:hover {
            border-color: #409eff;
            box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
            transform: translateY(-2px);
          }

          .scene-card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;

            .scene-icon {
              width: 40px;
              height: 40px;
              background: linear-gradient(135deg, #409eff, #67c23a);
              border-radius: 50%;
              display: flex;
              align-items: center;
              justify-content: center;

              .el-icon {
                font-size: 20px;
                color: white;
              }
            }

            .scene-actions {
              display: flex;
              gap: 4px;
            }
          }

          .scene-info {
            margin-bottom: 16px;

            .scene-name {
              margin: 0 0 8px 0;
              font-size: 16px;
              font-weight: 600;
              color: #ffffff;
            }

            .scene-location {
              display: flex;
              align-items: center;
              color: #cccccc;
              font-size: 14px;

              .el-icon {
                margin-right: 4px;
                font-size: 14px;
              }
            }
          }

          .scene-stats {
            display: flex;
            gap: 20px;
            margin-bottom: 16px;

            .stat-item {
              display: flex;
              flex-direction: column;
              align-items: center;

              .stat-label {
                font-size: 12px;
                color: #cccccc;
                margin-bottom: 4px;
              }

              .stat-value {
                font-size: 18px;
                font-weight: 600;
                color: #409eff;
              }
            }
          }

          .scene-controls {
            display: flex;
            justify-content: center;

            .el-button {
              width: 100%;
            }
          }
        }
      }

      .pagination {
        display: flex;
        justify-content: center;
        margin-top: 20px;
      }
    }
  }

  .scene-detail {
    .detail-section {
      margin-bottom: 30px;

      h4 {
        margin: 0 0 16px 0;
        color: #ffffff;
        font-size: 16px;
        border-bottom: 1px solid #404040;
        padding-bottom: 8px;
      }
    }
  }
}
</style>



