<template>
  <Dialog v-model="dialogVisible" title="下发记录详情" width="700px">
    <el-descriptions :column="2" border v-loading="loading">
      <el-descriptions-item label="记录ID">{{ dispatch.id }}</el-descriptions-item>
      <el-descriptions-item label="设备名称">{{ dispatch.deviceName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="授权ID">{{ dispatch.authorizationId || '-' }}</el-descriptions-item>
      <el-descriptions-item label="下发类型">
        <el-tag v-if="dispatch.dispatchType === 1" type="primary">授权下发</el-tag>
        <el-tag v-else-if="dispatch.dispatchType === 2" type="warning">取消授权</el-tag>
        <el-tag v-else-if="dispatch.dispatchType === 3" type="info">时间表下发</el-tag>
        <span v-else>-</span>
      </el-descriptions-item>
      <el-descriptions-item label="下发状态">
        <el-tag v-if="dispatch.dispatchStatus === 0" type="info">待下发</el-tag>
        <el-tag v-else-if="dispatch.dispatchStatus === 1" type="warning">下发中</el-tag>
        <el-tag v-else-if="dispatch.dispatchStatus === 2" type="success">下发成功</el-tag>
        <el-tag v-else-if="dispatch.dispatchStatus === 3" type="danger">下发失败</el-tag>
        <span v-else>-</span>
      </el-descriptions-item>
      <el-descriptions-item label="下发时间" :span="2">
        {{ dispatch.dispatchTime ? dateFormatter(new Date(dispatch.dispatchTime)) : '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="响应时间" :span="2">
        {{ dispatch.responseTime ? dateFormatter(new Date(dispatch.responseTime)) : '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="错误信息" :span="2">
        {{ dispatch.errorMsg || '无' }}
      </el-descriptions-item>
      <el-descriptions-item label="创建时间" :span="2">
        {{ dispatch.createTime ? dateFormatter(new Date(dispatch.createTime)) : '-' }}
      </el-descriptions-item>
    </el-descriptions>

    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
      <el-button
        v-if="dispatch.dispatchStatus === 3"
        type="warning"
        @click="handleRetry"
        v-hasPermi="['iot:access-dispatch:redispatch']"
      >
        重新下发
      </el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts" name="DispatchDetail">
import { ref } from 'vue'
import { dateFormatter } from '@/utils/formatTime'
import * as AccessDispatchApi from '@/api/iot/access/dispatch'
import { ElMessage, ElMessageBox } from 'element-plus'

const message = useMessage()

const dialogVisible = ref(false)
const loading = ref(false)
const dispatch = ref<AccessDispatchApi.AccessDispatchVO>({} as AccessDispatchApi.AccessDispatchVO)

const emit = defineEmits(['success'])

const open = async (id: number) => {
  dialogVisible.value = true
  loading.value = true
  try {
    dispatch.value = await AccessDispatchApi.getAccessDispatch(id)
  } finally {
    loading.value = false
  }
}

const handleRetry = async () => {
  try {
    await ElMessageBox.confirm('确认要重新下发该记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await AccessDispatchApi.retryDispatch(dispatch.value.id!)
    message.success('重新下发成功')
    emit('success')
    await open(dispatch.value.id!)
  } catch (error: any) {
    if (error !== 'cancel') {
      message.error('重新下发失败：' + (error.message || '未知错误'))
    }
  }
}

defineExpose({ open })
</script>




















