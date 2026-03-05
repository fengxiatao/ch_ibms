<template>
  <div class="lighting-scene-page">
    <ContentWrap>
      <div class="page-header">
        <h2>场景列表</h2>
        <el-button type="primary" @click="openSceneModal()">
          <Icon icon="ep:plus" class="mr-5px" /> 新建场景
        </el-button>
      </div>

      <div v-loading="loading" class="scene-grid">
        <div
          v-for="scene in sceneList"
          :key="scene.id"
          class="scene-card"
          :class="{ 'is-active': activeSceneId === scene.id }"
          @click="applyScene(scene)"
        >
          <div class="scene-icon">{{ getSceneEmoji(scene.sceneIcon) }}</div>
          <div class="scene-name">{{ scene.sceneName }}</div>
          <div class="scene-desc">{{ scene.description || '点击执行场景' }}</div>
          <div class="scene-actions">
            <el-button size="small" @click.stop="editScene(scene)">编辑</el-button>
            <el-button size="small" type="danger" plain @click.stop="deleteScene(scene)"
              >删除</el-button
            >
          </div>
        </div>
      </div>
    </ContentWrap>

    <!-- 新建/编辑场景弹窗 -->
    <el-dialog
      v-model="sceneDialogVisible"
      :title="editingScene ? '编辑场景' : '新建场景'"
      width="700px"
    >
      <el-form :model="sceneForm" label-width="100px">
        <el-form-item label="场景名称" required>
          <el-input v-model="sceneForm.sceneName" placeholder="例如：会议模式、午休模式" />
        </el-form-item>
        <el-form-item label="场景图标">
          <el-select v-model="sceneForm.sceneIcon" placeholder="选择图标">
            <el-option label="🌅 日出" value="sun" />
            <el-option label="🌙 月亮" value="moon" />
            <el-option label="📊 会议" value="meeting" />
            <el-option label="🍽️ 午餐" value="lunch" />
            <el-option label="✨ 星星" value="star" />
          </el-select>
        </el-form-item>
        <el-form-item label="场景描述">
          <el-input
            v-model="sceneForm.description"
            type="textarea"
            :rows="2"
            placeholder="描述场景效果"
          />
        </el-form-item>
        <el-form-item label="选择设备">
          <div class="device-select-header">
            <el-checkbox v-model="selectAll" @change="toggleSelectAll">全选</el-checkbox>
            <span class="selected-count">已选择 {{ selectedDevices.length }} 个设备</span>
          </div>
          <div class="device-select-list">
            <div
              v-for="device in allDevices"
              :key="device.id"
              class="device-select-item"
              :class="{ 'is-selected': selectedDevices.includes(device.id) }"
              @click="toggleDevice(device.id)"
            >
              <el-checkbox :model-value="selectedDevices.includes(device.id)" />
              <div class="device-info">
                <div class="device-name">{{ device.circuitName }}</div>
                <div class="device-area"
                  >{{ device.areaName }} ·
                  {{ device.circuitType === 4 ? '可调光' : '普通回路' }}</div
                >
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sceneDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveScene">保存场景</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import * as LightingApi from '@/api/iot/building/lighting'

defineOptions({ name: 'LightingScene' })

const message = useMessage()
const loading = ref(false)
const sceneList = ref<any[]>([])
const activeSceneId = ref<number | null>(null)
const sceneDialogVisible = ref(false)
const editingScene = ref<any>(null)
const allDevices = ref<any[]>([])
const selectedDevices = ref<number[]>([])

const sceneForm = reactive({
  sceneName: '',
  sceneIcon: 'sun',
  description: ''
})

const selectAll = computed({
  get: () =>
    selectedDevices.value.length === allDevices.value.length && allDevices.value.length > 0,
  set: () => {}
})

const getSceneEmoji = (icon: string) => {
  const map: Record<string, string> = {
    sun: '🌅',
    moon: '🌙',
    meeting: '📊',
    lunch: '🍽️',
    star: '✨',
    leaf: '🌿'
  }
  return map[icon] || '✨'
}

const getSceneList = async () => {
  loading.value = true
  try {
    sceneList.value = await LightingApi.getSceneSimpleList()
  } catch (e) {
    console.error('获取场景列表失败', e)
  } finally {
    loading.value = false
  }
}

const getAllDevices = async () => {
  try {
    const data = await LightingApi.getCircuitPage({ pageNo: 1, pageSize: 100 })
    allDevices.value = data.list
  } catch (e) {
    console.error('获取设备列表失败', e)
  }
}

const applyScene = async (scene: any) => {
  try {
    await message.confirm(`确定执行场景【${scene.sceneName}】吗？`)
    await LightingApi.executeScene(scene.id, 'admin')
    activeSceneId.value = scene.id
    message.success('场景执行成功')
    setTimeout(() => {
      activeSceneId.value = null
    }, 2000)
  } catch {}
}

const openSceneModal = (scene?: any) => {
  editingScene.value = scene || null
  if (scene) {
    sceneForm.sceneName = scene.sceneName
    sceneForm.sceneIcon = scene.sceneIcon
    sceneForm.description = scene.description
    selectedDevices.value = scene.deviceIds || []
  } else {
    sceneForm.sceneName = ''
    sceneForm.sceneIcon = 'sun'
    sceneForm.description = ''
    selectedDevices.value = []
  }
  sceneDialogVisible.value = true
}

const editScene = (scene: any) => {
  openSceneModal(scene)
}

const deleteScene = async (scene: any) => {
  try {
    await message.confirm(`确定要删除场景【${scene.sceneName}】吗？`)
    // await LightingApi.deleteScene(scene.id)
    sceneList.value = sceneList.value.filter((s) => s.id !== scene.id)
    message.success('场景已删除')
  } catch {}
}

const toggleSelectAll = (val: boolean) => {
  if (val) {
    selectedDevices.value = allDevices.value.map((d) => d.id)
  } else {
    selectedDevices.value = []
  }
}

const toggleDevice = (deviceId: number) => {
  const index = selectedDevices.value.indexOf(deviceId)
  if (index > -1) {
    selectedDevices.value.splice(index, 1)
  } else {
    selectedDevices.value.push(deviceId)
  }
}

const saveScene = async () => {
  if (!sceneForm.sceneName) {
    message.warning('请输入场景名称')
    return
  }
  try {
    if (editingScene.value) {
      message.success('场景已更新')
    } else {
      sceneList.value.push({
        id: Date.now(),
        ...sceneForm,
        deviceIds: selectedDevices.value
      })
      message.success('场景创建成功')
    }
    sceneDialogVisible.value = false
    getSceneList()
  } catch (e) {
    message.error('保存失败')
  }
}

onMounted(() => {
  getSceneList()
  getAllDevices()
})
</script>

<style lang="scss" scoped>
.lighting-scene-page {
  box-sizing: border-box;
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  h2 {
    font-size: 18px;
    font-weight: 600;
    margin: 0;
  }
}

.scene-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

@media (max-width: 1200px) {
  .scene-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .scene-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.scene-card {
  border: 2px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: var(--el-bg-color);

  &:hover {
    border-color: var(--el-color-primary);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  }

  &.is-active {
    border-color: var(--el-color-primary);
    background: rgba(64, 158, 255, 0.05);
  }

  .scene-icon {
    font-size: 36px;
    margin-bottom: 12px;
  }

  .scene-name {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 8px;
  }

  .scene-desc {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-bottom: 16px;
  }

  .scene-actions {
    display: flex;
    gap: 8px;
    justify-content: center;
  }
}

.device-select-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;

  .selected-count {
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
}

.device-select-list {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  padding: 10px;
}

.device-select-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: var(--el-fill-color-light);
  }

  &.is-selected {
    background: rgba(var(--el-color-primary-rgb), 0.16);
  }

  .el-checkbox {
    margin-right: 10px;
  }

  .device-info {
    flex: 1;

    .device-name {
      font-weight: 600;
      font-size: 14px;
    }

    .device-area {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
}
</style>
