<template>
  <el-dialog
    v-model="dialogVisible"
    title="预设点管理"
    width="600px"
    :close-on-click-modal="false"
  >
    <div class="preset-manager">
      <!-- 添加预设点表单 -->
      <el-form :model="formData" :rules="rules" ref="formRef" inline class="preset-form">
        <el-form-item label="编号" prop="presetNo" style="width: 100px">
          <el-input-number v-model="formData.presetNo" :min="1" :max="255" size="small" />
        </el-form-item>
        <el-form-item label="名称" prop="presetName" style="width: 180px">
          <el-input v-model="formData.presetName" placeholder="预设点名称" size="small" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="small" @click="handleSave">
            {{ editingId ? '更新' : '添加' }}
          </el-button>
          <el-button v-if="editingId" size="small" @click="resetForm">取消</el-button>
        </el-form-item>
      </el-form>

      <!-- 预设点列表 -->
      <el-table :data="presetList" v-loading="loading" size="small" max-height="300">
        <el-table-column prop="presetNo" label="编号" width="60" align="center" />
        <el-table-column prop="presetName" label="名称" min-width="120" />
        <el-table-column prop="description" label="描述" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleGoto(row)">
              <Icon icon="ep:video-play" />转到
            </el-button>
            <el-button type="warning" size="small" link @click="handleEdit(row)">
              <Icon icon="ep:edit" />编辑
            </el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">
              <Icon icon="ep:delete" />删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="presetList.length === 0 && !loading" class="empty-tip">
        暂无预设点，请先添加
      </div>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import * as PresetApi from '@/api/iot/cameraPreset'

interface Props {
  channelId: number
  deviceId: number
}

const props = defineProps<Props>()
const emit = defineEmits(['goto'])

const dialogVisible = ref(false)
const loading = ref(false)
const presetList = ref<PresetApi.CameraPresetVO[]>([])
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const formData = reactive({
  presetNo: 1,
  presetName: '',
  description: ''
})

const rules: FormRules = {
  presetNo: [{ required: true, message: '请输入预设点编号', trigger: 'blur' }],
  presetName: [{ required: true, message: '请输入预设点名称', trigger: 'blur' }]
}

// 打开对话框
const open = () => {
  dialogVisible.value = true
  loadPresets()
}

// 加载预设点列表
const loadPresets = async () => {
  if (!props.channelId) return
  loading.value = true
  try {
    const res = await PresetApi.getPresetListByChannelId(props.channelId)
    presetList.value = res || []
  } catch (error) {
    console.error('加载预设点失败:', error)
  } finally {
    loading.value = false
  }
}

// 保存预设点
const handleSave = async () => {
  await formRef.value?.validate()
  try {
    const data: PresetApi.CameraPresetSaveReqVO = {
      channelId: props.channelId,
      presetNo: formData.presetNo,
      presetName: formData.presetName,
      description: formData.description
    }
    if (editingId.value) {
      data.id = editingId.value
      await PresetApi.updatePreset(data)
      ElMessage.success('更新预设点成功')
    } else {
      await PresetApi.createPreset(data)
      ElMessage.success('添加预设点成功')
    }
    resetForm()
    loadPresets()
  } catch (error: any) {
    ElMessage.error(error?.message || '操作失败')
  }
}

// 编辑预设点
const handleEdit = (row: PresetApi.CameraPresetVO) => {
  editingId.value = row.id!
  formData.presetNo = row.presetNo
  formData.presetName = row.presetName
  formData.description = row.description || ''
}

// 删除预设点
const handleDelete = async (row: PresetApi.CameraPresetVO) => {
  try {
    await ElMessageBox.confirm(`确定要删除预设点 "${row.presetName}" 吗？`, '提示', {
      type: 'warning'
    })
    await PresetApi.deletePreset(row.id!)
    ElMessage.success('删除成功')
    loadPresets()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '删除失败')
    }
  }
}

// 转到预设点
const handleGoto = (row: PresetApi.CameraPresetVO) => {
  emit('goto', row.presetNo)
  ElMessage.success(`正在转到预设点 ${row.presetName}`)
}

// 重置表单
const resetForm = () => {
  editingId.value = null
  formData.presetNo = 1
  formData.presetName = ''
  formData.description = ''
  formRef.value?.resetFields()
}

// 监听 channelId 变化
watch(() => props.channelId, () => {
  if (dialogVisible.value) {
    loadPresets()
  }
})

defineExpose({ open })
</script>

<style scoped lang="scss">
.preset-manager {
  .preset-form {
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .empty-tip {
    text-align: center;
    color: var(--el-text-color-secondary);
    padding: 40px 0;
  }
}
</style>
