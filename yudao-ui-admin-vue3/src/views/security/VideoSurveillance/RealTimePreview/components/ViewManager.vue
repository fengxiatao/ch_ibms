<template>
  <div class="view-manager">
    <!-- 新建分组按钮 -->
    <div class="group-actions">
      <el-button size="small" type="primary" @click="handleCreateGroup" style="width: 100%">
        <Icon icon="ep:folder-add" style="margin-right: 4px" />
        新建分组
      </el-button>
    </div>

    <!-- 视图列表 -->
    <div class="view-list">
      <div
        v-for="group in viewGroups"
        :key="group.id"
        class="view-group"
      >
        <!-- 分组标题 -->
        <div 
          class="group-header"
          @click="handleGroupClick(group)"
          @contextmenu.prevent="handleContextMenu($event, group)"
        >
          <div class="group-title">
            <!-- 有视图显示+/-图标，没有视图显示文件夹图标 -->
            <Icon v-if="group.viewCount > 0" :icon="group.expanded ? 'ep:minus' : 'ep:plus'" class="expand-icon" />
            <Icon v-else icon="ep:folder" class="folder-icon" />
            <span class="group-name">{{ group.name }}</span>
            <span v-if="group.viewCount > 0" class="view-count">({{ group.viewCount }})</span>
          </div>
        </div>
        
        <!-- 视图列表（展开时显示） -->
        <div v-if="group.expanded && group.children?.length" class="view-items">
          <div
            v-for="view in group.children"
            :key="view.id"
            class="view-item"
            :class="{ active: currentViewId === view.id }"
            @click="handleViewClick(view)"
            @contextmenu.prevent="handleContextMenu($event, view)"
          >
            <Icon icon="ep:video-camera" />
            <span class="view-name">{{ view.name }}</span>
            <span class="view-info">({{ view.gridLayout || view.paneCount }}分屏)</span>
            <Icon 
              icon="ep:delete" 
              class="view-delete-icon"
              @click.stop="handleDeleteView(view)"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 保存视图对话框 -->
    <el-dialog v-model="saveDialogVisible" title="保存视图" width="420px">
      <el-form label-width="88px">
        <el-form-item label="视图名称">
          <el-input v-model="saveForm.name" placeholder="请输入视图名称" />
        </el-form-item>
        <el-form-item label="所属分组">
          <el-select
            v-model="saveForm.groupIds"
            multiple
            placeholder="请选择分组（可多选）"
            style="width: 100%"
          >
            <el-option
              v-for="g in viewGroups"
              :key="g.id"
              :label="g.name"
              :value="g.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="saveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Icon } from '@/components/Icon'
import {
  getVideoViewTree,
  getVideoView,
  deleteVideoView,
  createVideoViewGroup,
  deleteVideoViewGroup
} from '@/api/iot/video/videoView'
import type { VideoView, VideoViewGroup } from '../types'

// 扩展分组类型
interface ViewGroupData {
  id: number
  name: string
  icon: string
  type: 'group'
  viewCount: number
  expanded: boolean
  children: ViewItemData[]
}

interface ViewItemData {
  id: number
  name: string
  type: 'view'
  gridLayout?: number
  paneCount?: number
}

// Emits
const emit = defineEmits<{
  (e: 'load-view', view: VideoView): void
  (e: 'save-view', viewInfo: { id?: number; name: string; groupIds: number[] }): void
  (e: 'groups-loaded', groups: VideoViewGroup[]): void
}>()

// 状态
const viewGroups = ref<ViewGroupData[]>([])
const currentViewId = ref<number | null>(null)
const saveDialogVisible = ref(false)
const saveForm = ref({
  id: undefined as number | undefined,
  name: '',
  groupIds: [] as number[]
})

// 加载视图分组列表（包含视图数量）
const loadViewGroups = async () => {
  try {
    // 获取完整的树结构（包含视图）
    const treeData = await getVideoViewTree()
    
    viewGroups.value = treeData.map((group: any) => ({
      id: group.id,
      name: group.name,
      icon: group.icon || 'ep:folder',
      type: 'group' as const,
      viewCount: group.children?.length || 0,
      expanded: false,
      children: (group.children || []).map((view: any) => ({
        id: view.id,
        name: view.name,
        type: 'view' as const,
        gridLayout: view.gridLayout,
        paneCount: view.paneCount
      }))
    }))
    
    emit('groups-loaded', viewGroups.value as any)
    console.log('[视图管理] 加载分组完成:', viewGroups.value.length)
  } catch (error) {
    console.error('[视图管理] 加载分组失败:', error)
    ElMessage.error('加载视图分组失败')
  }
}

// 切换分组展开/收起
const toggleGroup = (group: ViewGroupData) => {
  group.expanded = !group.expanded
}

// 分组点击
const handleGroupClick = (group: ViewGroupData) => {
  // 如果有视图，切换展开状态
  if (group.viewCount > 0) {
    toggleGroup(group)
  }
}

// 视图点击
const handleViewClick = async (view: ViewItemData) => {
  try {
    const fullView = await getVideoView(view.id)
    currentViewId.value = view.id
    emit('load-view', fullView)
    ElMessage.success(`视图 "${view.name}" 已加载`)
  } catch (error) {
    console.error('[视图管理] 加载视图失败:', error)
    ElMessage.error('加载视图失败')
  }
}

// 删除视图（从所有分组中删除）
const handleDeleteView = async (view: ViewItemData) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除视图 "${view.name}" 吗？\n该视图将从所有分组中移除。`, 
      '删除视图', 
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消'
      }
    )
    await deleteVideoView(view.id)
    if (currentViewId.value === view.id) {
      currentViewId.value = null
    }
    await loadViewGroups()
    ElMessage.success('视图已删除')
  } catch {
    // 取消
  }
}

// 右键菜单
const handleContextMenu = async (event: MouseEvent, data: ViewGroupData | ViewItemData) => {
  event.preventDefault()
  
  if (data.type === 'view') {
    // 复用删除视图方法
    await handleDeleteView(data)
  } else if (data.type === 'group') {
    if (data.id === 1) {
      ElMessage.warning('默认分组不能删除')
      return
    }
    try {
      await ElMessageBox.confirm(`确定要删除分组 "${data.name}" 吗？`, '删除分组', {
        type: 'warning'
      })
      await deleteVideoViewGroup(data.id)
      await loadViewGroups()
      ElMessage.success('分组已删除')
    } catch {
      // 取消
    }
  }
}

// 创建分组
const handleCreateGroup = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入分组名称', '新建分组', {
      inputPattern: /\S+/,
      inputErrorMessage: '分组名称不能为空'
    })
    
    await createVideoViewGroup({
      name: value,
      icon: 'ep:folder',
      sort: viewGroups.value.length
    })
    
    await loadViewGroups()
    ElMessage.success(`分组 "${value}" 已创建`)
  } catch {
    // 取消
  }
}

// 打开保存对话框
const openSaveDialog = (currentViewId?: number, currentViewName?: string, currentGroupIds?: number[]) => {
  saveForm.value = {
    id: currentViewId,
    name: currentViewName || '',
    groupIds: currentGroupIds || [1]
  }
  saveDialogVisible.value = true
}

// 保存视图提交
const handleSaveSubmit = async () => {
  if (!saveForm.value.name?.trim()) {
    ElMessage.warning('请输入视图名称')
    return
  }
  
  // 通知父组件保存（使用专门的 save-view 事件）
  emit('save-view', {
    id: saveForm.value.id,
    name: saveForm.value.name,
    groupIds: saveForm.value.groupIds.length > 0 ? saveForm.value.groupIds : [1]
  })
  
  saveDialogVisible.value = false
}

// 清除当前视图选中状态
const clearCurrentView = () => {
  currentViewId.value = null
}

// 设置当前视图（外部调用）
const setCurrentView = (viewId: number | null) => {
  currentViewId.value = viewId
}

// 生命周期
onMounted(() => {
  loadViewGroups()
})

// 暴露方法
defineExpose({
  loadViewGroups,
  openSaveDialog,
  clearCurrentView,
  setCurrentView,
  currentViewId
})
</script>

<style lang="scss" scoped>
.view-manager {
  .group-actions {
    padding: 8px 12px;
    border-bottom: 1px solid #233754;
  }

  .view-list {
    padding: 8px 0;
  }

  .view-group {
    margin-bottom: 4px;
  }

  .group-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 12px;
    cursor: pointer;
    border-radius: 4px;
    transition: background 0.2s;

    &:hover {
      background: rgba(64, 158, 255, 0.1);
    }

    .group-title {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #cfe3ff;
      font-size: 13px;

      .expand-icon {
        color: #409eff;
        font-size: 14px;
      }

      .folder-icon {
        color: #7c8db0;
        font-size: 14px;
      }

      .group-name {
        flex: 1;
      }

      .view-count {
        font-size: 11px;
        color: #7c8db0;
      }
    }
  }

  .view-items {
    padding-left: 20px;
  }

  .view-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 12px;
    cursor: pointer;
    border-radius: 4px;
    font-size: 13px;
    color: #a8c0dc;
    transition: all 0.2s;

    &:hover {
      background: rgba(64, 158, 255, 0.1);
      color: #cfe3ff;

      .view-delete-icon {
        opacity: 1;
      }
    }

    &.active {
      background: rgba(64, 158, 255, 0.2);
      color: #409eff;
    }

    .view-name {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .view-info {
      font-size: 11px;
      color: #7c8db0;
      flex-shrink: 0;
    }

    .view-delete-icon {
      opacity: 0;
      color: #f56c6c;
      font-size: 14px;
      flex-shrink: 0;
      transition: all 0.2s;
      margin-left: 4px;

      &:hover {
        color: #ff4d4f;
        transform: scale(1.1);
      }
    }
  }
}
</style>
