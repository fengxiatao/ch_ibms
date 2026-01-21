<template>
  <Dialog v-model="dialogVisible" title="告警记录详情" width="700px">
    <el-descriptions :column="2" border v-loading="loading">
      <el-descriptions-item label="告警ID">{{ alarm.id }}</el-descriptions-item>
      <el-descriptions-item label="设备名称">{{ alarm.deviceName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="告警类型">
        <el-tag v-if="alarm.alarmType === 1" type="danger">非法开门</el-tag>
        <el-tag v-else-if="alarm.alarmType === 2" type="warning">门未关</el-tag>
        <el-tag v-else-if="alarm.alarmType === 3" type="danger">胁迫开门</el-tag>
        <el-tag v-else-if="alarm.alarmType === 4" type="info">设备故障</el-tag>
        <el-tag v-else-if="alarm.alarmType === 5" type="warning">网络异常</el-tag>
        <span v-else>-</span>
      </el-descriptions-item>
      <el-descriptions-item label="告警级别">
        <el-tag v-if="alarm.alarmLevel === 1" type="info">低</el-tag>
        <el-tag v-else-if="alarm.alarmLevel === 2">中</el-tag>
        <el-tag v-else-if="alarm.alarmLevel === 3" type="warning">高</el-tag>
        <el-tag v-else-if="alarm.alarmLevel === 4" type="danger">紧急</el-tag>
        <span v-else>-</span>
      </el-descriptions-item>
      <el-descriptions-item label="告警内容" :span="2">
        {{ alarm.alarmContent || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="告警时间" :span="2">
        {{ alarm.alarmTime ? dateFormatter(new Date(alarm.alarmTime)) : '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="处理状态">
        <el-tag v-if="alarm.handleStatus === 0" type="danger">未处理</el-tag>
        <el-tag v-else-if="alarm.handleStatus === 1" type="warning">处理中</el-tag>
        <el-tag v-else-if="alarm.handleStatus === 2" type="success">已处理</el-tag>
        <span v-else>-</span>
      </el-descriptions-item>
      <el-descriptions-item label="处理人">
        {{ alarm.handleUserName || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="处理时间" :span="2">
        {{ alarm.handleTime ? dateFormatter(new Date(alarm.handleTime)) : '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="处理备注" :span="2">
        {{ alarm.handleRemark || '无' }}
      </el-descriptions-item>
      <el-descriptions-item label="创建时间" :span="2">
        {{ alarm.createTime ? dateFormatter(new Date(alarm.createTime)) : '-' }}
      </el-descriptions-item>
    </el-descriptions>

    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
      <el-button
        v-if="alarm.handleStatus !== 2"
        type="warning"
        @click="handleOpenHandle"
        v-hasPermi="['iot:access-alarm:handle']"
      >
        处理告警
      </el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts" name="AlarmDetail">
import { ref } from 'vue'
import { dateFormatter } from '@/utils/formatTime'
import * as AccessAlarmApi from '@/api/iot/access/alarm'

const dialogVisible = ref(false)
const loading = ref(false)
const alarm = ref<AccessAlarmApi.AccessAlarmVO>({} as AccessAlarmApi.AccessAlarmVO)

const emit = defineEmits(['handle'])

const open = async (id: number) => {
  dialogVisible.value = true
  loading.value = true
  try {
    alarm.value = await AccessAlarmApi.getAccessAlarm(id)
  } finally {
    loading.value = false
  }
}

const handleOpenHandle = () => {
  emit('handle', alarm.value)
  dialogVisible.value = false
}

defineExpose({ open })
</script>




















