<template>
  <div class="nvr-tree-container">
    <!-- 搜索框 -->
    <el-input
      v-model="searchKeyword"
      placeholder="搜索NVR/通道"
      clearable
      class="nvr-search"
    >
      <template #prefix>
        <Icon icon="ep:search" />
      </template>
    </el-input>

    <!-- NVR树 -->
    <el-tree
      ref="treeRef"
      :data="treeData"
      :props="treeProps"
      :filter-node-method="filterNode"
      lazy
      :load="loadNode"
      node-key="id"
      :expand-on-click-node="false"
      @node-click="handleNodeClick"
    >
      <template #default="{ node, data }">
        <div class="tree-node">
          <Icon 
            :icon="data.type === 'nvr' ? 'ep:monitor' : 'ep:video-camera'" 
            :style="{ color: data.type === 'nvr' ? '#409eff' : '#67c23a' }"
          />
          <span class="node-label">{{ node.label }}</span>
          <el-button
            v-if="data.type === 'nvr'"
            type="primary"
            size="small"
            link
            @click.stop="$emit('refresh-channels', data.id)"
          >
            <Icon icon="ep:refresh" />
          </el-button>
        </div>
      </template>
    </el-tree>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Icon } from '@/components/Icon'

// Props
interface Props {
  treeData: any[]
  loadNode: (node: any, resolve: Function) => Promise<void>
}

const props = defineProps<Props>()

// Emits
defineEmits<{
  'node-click': [data: any]
  'refresh-channels': [nvrId: number]
}>()

// Refs
const treeRef = ref()
const searchKeyword = ref('')

// 树属性
const treeProps = {
  children: 'children',
  label: 'name',
  isLeaf: (data: any) => data.type === 'device'
}

// 过滤节点
const filterNode = (value: string, data: any) => {
  if (!value) return true
  return data.name?.toLowerCase().includes(value.toLowerCase())
}

// 监听搜索关键字
watch(searchKeyword, (val) => {
  treeRef.value?.filter(val)
})

// 节点点击
const handleNodeClick = (data: any) => {
  // 只处理通道节点（device类型）
  if (data.type === 'device') {
    // 发送事件到父组件
    // 父组件会处理添加到格子的逻辑
  }
}
</script>

<style scoped lang="scss">
.nvr-tree-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 12px;
}

.nvr-search {
  margin-bottom: 12px;
}

:deep(.el-tree) {
  flex: 1;
  overflow-y: auto;
  background: transparent;
  color: #fff;

  .el-tree-node__content {
    background: transparent;
    border-radius: 4px;
    margin-bottom: 2px;

    &:hover {
      background: rgba(255, 255, 255, 0.08);
    }
  }

  .el-tree-node:focus > .el-tree-node__content {
    background: rgba(64, 158, 255, 0.15);
  }

  .el-tree-node__expand-icon {
    color: rgba(255, 255, 255, 0.6);
  }

  .el-tree-node__loading-icon {
    color: #409eff;
  }
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  padding: 4px 0;
}

.node-label {
  flex: 1;
  font-size: 14px;
}
</style>
