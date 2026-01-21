<template>
  <el-dialog
    v-model="dialogVisible"
    title="任务编辑"
    width="92%"
    top="5vh"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    class="task-edit-dialog"
    @close="handleClose"
  >
    <div class="dialog-container">
      <!-- 顶部配置区 -->
      <div class="top-config">
        <el-form :model="formData" label-width="100px" inline class="config-form">
          <el-form-item label="任务名称:">
            <el-input 
              v-model="formData.taskName" 
              placeholder="任务 1" 
              style="width: 180px;" 
              size="default"
            />
          </el-form-item>
          <el-form-item label="任务专属时间:" style="margin-left: 20px;">
            <el-checkbox v-model="formData.dedicatedTime" style="margin-right: 8px;" />
            <el-input-number 
              v-model="formData.duration" 
              :min="1" 
              :max="999" 
              :controls="false"
              size="default"
              style="width: 80px;"
            />
            <span style="margin-left: 6px; color: #606266;">秒</span>
          </el-form-item>
          <div style="margin-left: auto; display: flex; gap: 8px;">
            <el-button type="primary" @click="handleSave" size="default">
              确定
            </el-button>
            <el-button @click="handleClose" size="default">
              取消
            </el-button>
          </div>
        </el-form>
      </div>

      <!-- 主体内容区 -->
      <div class="main-body">
        <!-- 左侧设备树 -->
        <div class="left-tree-panel">
          <div class="tree-header">
            <el-select v-model="treeType" size="small" style="width: 100%;" placeholder="选择视图">
              <el-option label="组织树" value="organization" />
              <el-option label="设备树" value="nvr" />
            </el-select>
          </div>
          
          <div class="tree-search">
            <el-input
              v-model="searchKeyword"
              placeholder="请输入关键字"
              size="small"
              clearable
            >
              <template #suffix>
                <Icon icon="ep:search" />
              </template>
            </el-input>
          </div>

          <div class="tree-content">
            <el-tree
              v-if="treeType === 'organization'"
              :data="organizationTreeData"
              :props="treeProps"
              :expand-on-click-node="false"
              :lazy="true"
              :load="loadTreeNode"
              node-key="id"
              ref="treeRef"
              @node-click="handleNodeClick"
            >
              <template #default="{ node, data }">
                <span 
                  class="tree-node"
                  :draggable="data.type === 'device'"
                  @dragstart="handleDragStart($event, data)"
                >
                  <Icon :icon="getNodeIcon(data.type)" :size="16" />
                  <span class="node-label">{{ node.label }}</span>
                  <el-tag v-if="data.type === 'nvr'" :type="data.online ? 'success' : 'info'" size="small">
                    {{ data.online ? '在线' : '离线' }}
                  </el-tag>
                </span>
              </template>
            </el-tree>

            <el-tree
              v-else
              :data="nvrTreeData"
              :props="treeProps"
              :expand-on-click-node="false"
              :lazy="true"
              :load="loadNvrNode"
              node-key="id"
              ref="nvrTreeRef"
              @node-click="handleNodeClick"
            >
              <template #default="{ node, data }">
                <span 
                  class="tree-node"
                  :draggable="data.type === 'device'"
                  @dragstart="handleDragStart($event, data)"
                >
                  <Icon :icon="getNodeIcon(data.type)" :size="16" />
                  <span class="node-label">{{ node.label }}</span>
                  <el-tag v-if="data.type === 'nvr'" :type="data.online ? 'success' : 'info'" size="small">
                    {{ data.online ? '在线' : '离线' }}
                  </el-tag>
                </span>
              </template>
            </el-tree>
          </div>

          <!-- 底部折叠面板 -->
          <div class="tree-footer">
            <el-collapse>
              <el-collapse-item title="场景树" name="scene">
                <div class="scene-list">
                  <div class="scene-item">场景 1</div>
                  <div class="scene-item">场景 2</div>
                </div>
              </el-collapse-item>
              <el-collapse-item title="预案" name="plan">
                <div class="plan-list">
                  <div class="plan-item">预案 1</div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </div>

        <!-- 中间视频预览区 -->
        <div class="center-video-panel">
          <div class="video-grid">
            <div
              v-for="(cell, index) in videoCells"
              :key="index"
              class="video-cell"
              :class="{ 'has-channel': cell.channels.length > 0 }"
              @drop="handleDrop($event, index)"
              @dragover.prevent
              @dragenter.prevent
            >
              <div v-if="cell.channels.length === 0" class="empty-cell">
                <div class="empty-hint">
                  <Icon icon="ep:plus" :size="24" style="color: #999;" />
                  <span style="color: #999; font-size: 12px; margin-top: 8px;">拖拽通道到此处</span>
                </div>
              </div>
              <div v-else class="channel-info">
                <div class="channel-count-badge">已绑定 {{ cell.channels.length }} 个通道</div>
                <div class="channel-preview">
                  <img v-if="cell.snapshot" :src="cell.snapshot" alt="预览" />
                  <div v-else class="preview-placeholder">
                    <Icon icon="ep:video-camera" :size="40" style="color: #666;" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧操作面板（可选） -->
        <div class="right-panel" v-if="false">
          <el-button type="primary" size="small">添加通道</el-button>
        </div>
      </div>

      <!-- 底部通道列表 -->
      <div class="bottom-channel-list">
        <div class="channel-table-header">
          <span>通道列表</span>
          <div class="table-actions">
            <el-button size="small" @click="handleClearAll">
              <Icon icon="ep:delete" />
              清空
            </el-button>
          </div>
        </div>
        
        <el-table
          :data="channelList"
          height="200"
          border
          size="small"
          class="channel-table"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="deviceName" label="设备名称" min-width="150">
            <template #default="{ row }">
              <div class="device-name-cell">
                <Icon icon="ep:video-camera" :size="14" />
                <span>{{ row.deviceName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="类型" width="80" align="center">
            <template #default="{ row }">
              <el-tag size="small">{{ row.type || '无' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="duration" label="停留时间" width="120" align="center">
            <template #default="{ row }">
              <el-input-number 
                v-model="row.duration" 
                :min="1" 
                :max="999" 
                size="small"
                style="width: 100px;"
              />
              <span style="margin-left: 4px;">s</span>
            </template>
          </el-table-column>
          <el-table-column prop="group" label="绑定组" width="100" align="center">
            <template #default="{ row }">
              <el-tag size="small" type="info">{{ row.group || '组1' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="150">
            <template #default="{ row }">
              <el-input 
                v-model="row.remark" 
                size="small" 
                placeholder="房出1楼"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="{ row, $index }">
              <el-button-group size="small">
                <el-button @click="handlePreview(row)">
                  <Icon icon="ep:view" />
                </el-button>
                <el-button @click="handleMoveUp($index)" :disabled="$index === 0">
                  <Icon icon="ep:top" />
                </el-button>
                <el-button @click="handleMoveDown($index)" :disabled="$index === channelList.length - 1">
                  <Icon icon="ep:bottom" />
                </el-button>
                <el-button type="danger" @click="handleRemoveChannel($index)">
                  <Icon icon="ep:delete" />
                </el-button>
              </el-button-group>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Icon } from '@/components/Icon'
import { ElMessage, ElMessageBox } from 'element-plus'

// Props
interface Props {
  modelValue: boolean
  taskData?: any
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  taskData: null
})

// Emits
const emit = defineEmits(['update:modelValue', 'save'])

// 对话框显示状态
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 表单数据
const formData = ref({
  taskName: '任务 1',
  dedicatedTime: false,
  duration: 30
})

// 树类型
const treeType = ref('organization')
const searchKeyword = ref('')

// 树配置
const treeProps = {
  label: 'name',
  children: 'children',
  isLeaf: 'isLeaf'
}

// 组织树数据
const organizationTreeData = ref([])
const nvrTreeData = ref([])

// 通道接口定义
interface ChannelItem {
  deviceId: string
  deviceName: string
  channelNo: number
  duration: number
  group: string
  type: string
  remark: string
}

// 视频格子接口定义
interface VideoCell {
  channels: ChannelItem[]
  snapshot: string | null
}

// 视频格子（2x2）
const videoCells = ref<VideoCell[]>([
  { channels: [], snapshot: null },
  { channels: [], snapshot: null },
  { channels: [], snapshot: null },
  { channels: [], snapshot: null }
])

// 通道列表
const channelList = ref<ChannelItem[]>([])

// 树节点图标
const getNodeIcon = (type: string) => {
  const iconMap = {
    organization: 'ep:office-building',
    nvr: 'ep:cpu',
    device: 'ep:video-camera',
    channel: 'ep:video-camera'
  }
  return iconMap[type] || 'ep:folder'
}

// 加载树节点
const loadTreeNode = async (node: any, resolve: any) => {
  if (node.level === 0) {
    // 加载根节点
    const data = [
      {
        id: '192.168.1.200',
        name: '192.168.1.200',
        type: 'nvr',
        online: true,
        isLeaf: false
      }
    ]
    resolve(data)
  } else if (node.data.type === 'nvr') {
    // 加载 NVR 下的通道
    const channels = Array.from({ length: 8 }, (_, i) => ({
      id: `${node.data.id}_ch${i + 1}`,
      name: `通道 ${i + 1}`,
      type: 'device',
      deviceId: `device_${i + 1}`,
      channelNo: i + 1,
      isLeaf: true
    }))
    resolve(channels)
  } else {
    resolve([])
  }
}

// 加载 NVR 节点
const loadNvrNode = async (node: any, resolve: any) => {
  // 同上
  loadTreeNode(node, resolve)
}

// 节点点击
const handleNodeClick = (data: any) => {
  console.log('节点点击:', data)
}

// 拖拽开始
const handleDragStart = (event: DragEvent, data: any) => {
  if (data.type === 'device') {
    event.dataTransfer!.effectAllowed = 'copy'
    event.dataTransfer!.setData('application/json', JSON.stringify(data))
  }
}

// 放置到格子
const handleDrop = (event: DragEvent, cellIndex: number) => {
  event.preventDefault()
  const data = JSON.parse(event.dataTransfer!.getData('application/json'))
  
  if (data.type === 'device') {
    // 添加通道到格子
    const channel = {
      deviceId: data.deviceId,
      deviceName: data.name,
      channelNo: data.channelNo,
      duration: 10,
      group: `组${cellIndex + 1}`,
      type: '无',
      remark: '房出1楼'
    }
    
    videoCells.value[cellIndex].channels.push(channel)
    channelList.value.push(channel)
    
    ElMessage.success(`已添加通道到格子 ${cellIndex + 1}`)
  }
}

// 预览通道
const handlePreview = (_row: ChannelItem) => {
  ElMessage.info('预览功能开发中...')
}

// 上移
const handleMoveUp = (index: number) => {
  if (index > 0) {
    const temp = channelList.value[index]
    channelList.value[index] = channelList.value[index - 1]
    channelList.value[index - 1] = temp
  }
}

// 下移
const handleMoveDown = (index: number) => {
  if (index < channelList.value.length - 1) {
    const temp = channelList.value[index]
    channelList.value[index] = channelList.value[index + 1]
    channelList.value[index + 1] = temp
  }
}

// 删除通道
const handleRemoveChannel = (index: number) => {
  ElMessageBox.confirm('确定要删除该通道吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    channelList.value.splice(index, 1)
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// 清空所有通道
const handleClearAll = () => {
  ElMessageBox.confirm('确定要清空所有通道吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    channelList.value = []
    videoCells.value.forEach(cell => {
      cell.channels = []
      cell.snapshot = null
    })
    ElMessage.success('已清空')
  }).catch(() => {})
}

// 保存
const handleSave = () => {
  const data = {
    ...formData.value,
    channels: channelList.value,
    videoCells: videoCells.value
  }
  emit('save', data)
  dialogVisible.value = false
}

// 关闭
const handleClose = () => {
  dialogVisible.value = false
}

// 监听任务数据变化
watch(() => props.taskData, (newVal) => {
  if (newVal) {
    formData.value = {
      taskName: newVal.taskName || '任务 1',
      dedicatedTime: newVal.dedicatedTime || false,
      duration: newVal.duration || 30
    }
    channelList.value = newVal.channels || []
  }
}, { immediate: true })
</script>

<style scoped lang="scss">
.task-edit-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
    height: 80vh;
  }
}

.dialog-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f5f5;
}

// 顶部配置区
.top-config {
  background: #fff;
  padding: 14px 20px;
  border-bottom: 1px solid #dcdfe6;
  
  .config-form {
    display: flex;
    align-items: center;
    margin: 0;
    
    :deep(.el-form-item) {
      margin-bottom: 0;
      margin-right: 0;
      
      .el-form-item__label {
        color: #606266;
        font-weight: normal;
      }
    }
  }
}

// 主体区域
.main-body {
  flex: 1;
  display: flex;
  gap: 1px;
  background: #e0e0e0;
  overflow: hidden;
}

// 左侧设备树
.left-tree-panel {
  width: 280px;
  background: #fff;
  display: flex;
  flex-direction: column;
  
  .tree-header {
    padding: 12px;
    border-bottom: 1px solid #e0e0e0;
  }
  
  .tree-search {
    padding: 12px;
    border-bottom: 1px solid #e0e0e0;
  }
  
  .tree-content {
    flex: 1;
    overflow-y: auto;
    padding: 8px;
    
    .tree-node {
      display: flex;
      align-items: center;
      gap: 6px;
      flex: 1;
      cursor: pointer;
      
      &[draggable="true"] {
        cursor: move;
      }
      
      .node-label {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
  
  .tree-footer {
    border-top: 1px solid #e0e0e0;
    
    :deep(.el-collapse) {
      border: none;
    }
    
    .scene-list, .plan-list {
      padding: 8px;
    }
    
    .scene-item, .plan-item {
      padding: 6px 12px;
      cursor: pointer;
      border-radius: 4px;
      
      &:hover {
        background: #f5f5f5;
      }
    }
  }
}

// 中间视频预览区
.center-video-panel {
  flex: 1;
  background: #2b2b2b;
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .video-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: repeat(2, 1fr);
    gap: 8px;
    width: 100%;
    height: 100%;
    max-width: 800px;
    max-height: 600px;
    
    .video-cell {
      background: #1a1a1a;
      border: 2px dashed #444;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 200px;
      transition: all 0.3s;
      
      &:hover {
        border-color: #666;
      }
      
      &.has-channel {
        border-style: solid;
        border-color: #409eff;
      }
      
      .empty-cell {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        
        .empty-hint {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          text-align: center;
        }
      }
      
      .channel-info {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        position: relative;
        
        .channel-count-badge {
          position: absolute;
          top: 12px;
          left: 50%;
          transform: translateX(-50%);
          background: rgba(0, 0, 0, 0.6);
          color: #fff;
          padding: 4px 12px;
          border-radius: 4px;
          font-size: 13px;
          white-space: nowrap;
          z-index: 1;
        }
        
        .channel-preview {
          flex: 1;
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 40px 20px 20px;
          
          img {
            max-width: 100%;
            max-height: 100%;
            object-fit: contain;
          }
          
          .preview-placeholder {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 100%;
            height: 100%;
          }
        }
      }
    }
  }
}

// 右侧面板
.right-panel {
  width: 200px;
  background: #fff;
  padding: 16px;
}

// 底部通道列表
.bottom-channel-list {
  background: #fff;
  border-top: 1px solid #e0e0e0;
  
  .channel-table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    border-bottom: 1px solid #e0e0e0;
    font-weight: 600;
    
    .table-actions {
      display: flex;
      gap: 8px;
    }
  }
  
  .channel-table {
    :deep(.el-table__body-wrapper) {
      &::-webkit-scrollbar {
        width: 8px;
        height: 8px;
      }
      
      &::-webkit-scrollbar-thumb {
        background: #ccc;
        border-radius: 4px;
      }
    }
    
    .device-name-cell {
      display: flex;
      align-items: center;
      gap: 6px;
    }
  }
}
</style>
