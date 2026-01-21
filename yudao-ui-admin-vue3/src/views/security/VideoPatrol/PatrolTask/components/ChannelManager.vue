<template>
  <el-dialog
    v-model="visible"
    title="管理通道"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="channel-manager">
      <!-- 通道列表 -->
      <el-table
        ref="tableRef"
        :data="channels"
        row-key="id"
        class="channel-table"
      >
        <el-table-column type="index" label="#" width="50" />
        
        <el-table-column label="通道名称" prop="name" min-width="200">
          <template #default="{ row }">
            <div class="channel-info">
              <Icon icon="ep:video-camera" style="color: #67c23a;" />
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="IP地址" prop="ip" width="150" />
        
        <el-table-column label="轮播时长" width="150">
          <template #default="{ row }">
            <el-input-number
              v-model="row.duration"
              :min="0"
              :max="300"
              :step="5"
              size="small"
              style="width: 120px;"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ $index }">
            <el-button
              type="danger"
              size="small"
              link
              @click="handleRemove($index)"
            >
              <Icon icon="ep:delete" />
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 拖拽提示 -->
      <div v-if="channels.length === 0" class="empty-hint">
        <Icon icon="ep:video-pause" :size="48" />
        <p>暂无通道</p>
        <p class="tip">从左侧拖拽设备到此处添加</p>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleConfirm">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Icon } from '@/components/Icon'

// Props
interface Props {
  modelValue: boolean
  channels: any[]
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'update:channels': [channels: any[]]
  'confirm': [channels: any[]]
}>()

// 内部状态
const visible = ref(props.modelValue)
const tableRef = ref()

// 监听外部变化
watch(() => props.modelValue, (val) => {
  visible.value = val
})

// 监听内部变化
watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 删除通道
const handleRemove = (index: number) => {
  const newChannels = [...props.channels]
  newChannels.splice(index, 1)
  emit('update:channels', newChannels)
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
}

// 确认
const handleConfirm = () => {
  emit('confirm', props.channels)
  visible.value = false
}
</script>

<style scoped lang="scss">
.channel-manager {
  min-height: 300px;
}

.channel-table {
  :deep(.el-table__body-wrapper) {
    max-height: 400px;
    overflow-y: auto;
  }
}

.channel-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: rgba(0, 0, 0, 0.4);

  p {
    margin-top: 12px;
    font-size: 14px;
  }

  .tip {
    font-size: 12px;
    color: rgba(0, 0, 0, 0.3);
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
