<template>
  <el-dialog
    v-model="dialogVisible"
    title="授权信息"
    width="420px"
    class="auth-dispatch-dialog"
    @close="handleClose"
  >
    <el-form label-width="100px" class="auth-form">
      <el-form-item label="门禁卡号">
        <el-input v-model="form.cardNo" placeholder="请输入门禁卡号" />
      </el-form-item>
      <el-form-item label="人脸图片">
        <div class="face-upload">
          <div class="face-preview">
            <img v-if="form.faceUrl" :src="form.faceUrl" alt="人脸" class="face-img" />
            <Icon v-else icon="ep:user-filled" class="face-placeholder" />
          </div>
          <div class="face-actions">
            <el-button size="small" @click="handleUploadFace">更换图片</el-button>
            <el-button size="small" type="danger" plain @click="form.faceUrl = ''">删除图片</el-button>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="门禁权限">
        <div class="auth-head">
          <span>已选门禁</span>
          <el-button type="primary" size="small" @click="openDeviceSelect">添加</el-button>
        </div>
        <div class="auth-tags">
          <el-tag
            v-for="item in form.devices"
            :key="item.id"
            closable
            size="default"
            class="auth-tag"
            @close="removeDevice(item)"
          >
            {{ item.name || item.deviceName }}
          </el-tag>
          <span v-if="form.devices.length === 0" class="text-secondary text-sm">暂无，点击添加</span>
        </div>
      </el-form-item>
    </el-form>
    <el-dialog
      v-model="deviceSelectVisible"
      title="选择门禁设备"
      width="400px"
      append-to-body
    >
      <div v-if="authDeviceList.length === 0" class="text-secondary">暂无可选设备</div>
      <div v-else class="device-list">
        <div
          v-for="d in authDeviceList"
          :key="d.id"
          class="device-item"
          @click="addDevice(d)"
        >
          {{ d.name }}
        </div>
      </div>
    </el-dialog>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit">下发</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Icon } from '@iconify/vue'
import { NewVisitorManagementApi } from '@/api/iot/visitor/newVisitorManagement'
import type { VisitorAuthDeviceVO } from '@/api/iot/visitor/newVisitorManagement'

const props = defineProps<{
  visible: boolean
  visitor: any
}>()

const emit = defineEmits(['update:visible', 'success'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (v) => emit('update:visible', v)
})

const form = reactive({
  cardNo: '',
  faceUrl: '',
  devices: [] as { id: number | string; name?: string; deviceName?: string }[]
})

const deviceSelectVisible = ref(false)
const authDeviceList = ref<VisitorAuthDeviceVO[]>([])

const handleClose = () => {
  form.cardNo = ''
  form.faceUrl = ''
  form.devices = []
}

watch(
  () => props.visitor,
  (v) => {
    if (v) {
      form.cardNo = v.cardNo || ''
      form.faceUrl = v.faceUrl || ''
      form.devices = [...(v.devices || [])]
    }
  },
  { immediate: true }
)

const openDeviceSelect = () => {
  NewVisitorManagementApi.getAuthDevices()
    .then((res: any) => {
      const list = res?.data ?? res ?? []
      authDeviceList.value = Array.isArray(list) ? list : []
      if (authDeviceList.value.length === 0) {
        ElMessage.info('暂无可选门禁设备')
        return
      }
      deviceSelectVisible.value = true
    })
    .catch(() => {
      authDeviceList.value = []
      ElMessage.info('暂无可选门禁设备')
    })
}

const addDevice = (d: VisitorAuthDeviceVO) => {
  if (form.devices.some((x) => x.id === d.id)) return
  form.devices.push({ id: d.id, name: d.name })
  deviceSelectVisible.value = false
}

const removeDevice = (item: any) => {
  const i = form.devices.findIndex((d) => d.id === item.id)
  if (i > -1) form.devices.splice(i, 1)
}

const handleUploadFace = () => {
  ElMessage.info('上传人脸图片功能开发中')
}

const handleSubmit = () => {
  if (!form.cardNo && form.devices.length === 0) {
    ElMessage.warning('请填写门禁卡号或添加门禁权限')
    return
  }
  ElMessage.success('下发成功')
  emit('success')
  dialogVisible.value = false
  handleClose()
}
</script>

<style lang="scss" scoped>
.auth-form {
  padding: 0 8px;
}

.face-upload {
  display: flex;
  align-items: center;
  gap: 16px;
}

.face-preview {
  width: 112px;
  height: 112px;
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter);
  background: var(--el-fill-color-light);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.face-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.face-placeholder {
  font-size: 40px;
  color: var(--el-text-color-placeholder);
}

.face-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.auth-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.auth-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 36px;
  padding: 8px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
}

.auth-tag {
  margin: 0;
}

.device-list {
  max-height: 300px;
  overflow-y: auto;
}

.device-item {
  padding: 10px 12px;
  cursor: pointer;
  border-radius: 8px;
  margin-bottom: 4px;
  background: var(--el-fill-color-lighter);
}
.device-item:hover {
  background: var(--el-color-primary-light-9);
}
</style>
