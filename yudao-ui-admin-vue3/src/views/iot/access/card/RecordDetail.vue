<template>
  <Dialog v-model="dialogVisible" title="通行记录详情" width="700px">
    <el-descriptions :column="2" border v-loading="loading">
      <el-descriptions-item label="记录ID">{{ record.id }}</el-descriptions-item>
      <el-descriptions-item label="设备名称">{{ record.deviceName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="人员姓名">{{ record.personName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="卡号">{{ record.cardNo || '-' }}</el-descriptions-item>
      <el-descriptions-item label="开门类型">
        <el-tag v-if="record.openType === 1" type="primary">远程开门</el-tag>
        <el-tag v-else-if="record.openType === 2" type="success">二维码</el-tag>
        <el-tag v-else-if="record.openType === 3" type="info">刷卡</el-tag>
        <el-tag v-else-if="record.openType === 4" type="warning">人脸</el-tag>
        <el-tag v-else-if="record.openType === 5" type="danger">指纹</el-tag>
        <el-tag v-else-if="record.openType === 6">密码</el-tag>
        <span v-else>-</span>
      </el-descriptions-item>
      <el-descriptions-item label="开门结果">
        <el-tag :type="record.openResult === 1 ? 'success' : 'danger'" size="small">
          {{ record.openResult === 1 ? '成功' : '失败' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="开门时间" :span="2">
        {{ record.openTime ? dateFormatter(new Date(record.openTime)) : '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="体温" v-if="record.temperature">
        {{ record.temperature }}°C
      </el-descriptions-item>
      <el-descriptions-item label="备注" :span="2">
        {{ record.remark || '无' }}
      </el-descriptions-item>
      <el-descriptions-item label="抓拍图片" :span="2" v-if="record.imageUrl">
        <el-image
          :src="record.imageUrl"
          :preview-src-list="[record.imageUrl]"
          fit="cover"
          style="width: 200px; height: 200px; cursor: pointer"
          :preview-teleported="true"
        />
      </el-descriptions-item>
    </el-descriptions>

    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts" name="RecordDetail">
import { ref } from 'vue'
import { dateFormatter } from '@/utils/formatTime'
import * as AccessRecordApi from '@/api/iot/access/record'

const dialogVisible = ref(false)
const loading = ref(false)
const record = ref<AccessRecordApi.AccessRecordVO>({} as AccessRecordApi.AccessRecordVO)

const open = async (id: number) => {
  dialogVisible.value = true
  loading.value = true
  try {
    record.value = await AccessRecordApi.getAccessRecord(id)
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>

