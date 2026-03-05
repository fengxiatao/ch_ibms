<template>
  <Dialog v-model="dialogVisible" :title="`${visitorInfo.visitorName} - 开门记录`" width="700px">
    <div v-loading="loading">
      <!-- 开门记录列表 -->
      <el-table :data="records" size="small" max-height="400" stripe>
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column label="开门通道" prop="channelName" min-width="150" align="center">
          <template #default="{ row }">
            {{ row.channelName || row.deviceName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="开门方式" width="100" align="center">
          <template #default="{ row }">
            <span class="door-method">{{ getVerifyModeName(row.verifyMode) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="开门时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.eventTime) }}
          </template>
        </el-table-column>
      </el-table>

      <div v-if="records.length === 0" class="empty-tip">
        暂无开门记录
      </div>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { VisitorAccessRecordApi } from '@/api/iot/visitor'
import type { VisitorApplyVO } from '@/api/iot/visitor'

const dialogVisible = ref(false)
const loading = ref(false)
const visitorInfo = ref<Partial<VisitorApplyVO>>({})
const records = ref<any[]>([])

// 格式化日期
const formatDateTime = (date: any) => {
  if (!date) return '-'
  return formatDate(date, 'YYYY/MM/DD HH:mm:ss')
}

// 验证方式名称
const getVerifyModeName = (mode: string | number) => {
  const modeMap: Record<string, string> = {
    'CARD': '刷卡开门',
    'FACE': '人脸开门',
    'FINGERPRINT': '指纹开门',
    'PASSWORD': '密码开门',
    'QR_CODE': '二维码开门',
    '1': '刷卡开门',
    '2': '指纹开门',
    '3': '人脸开门',
    '4': '密码开门'
  }
  return modeMap[String(mode)] || mode || '人脸开门'
}

// 打开弹窗
const open = async (visitor: VisitorApplyVO) => {
  visitorInfo.value = visitor
  dialogVisible.value = true
  loading.value = true

  try {
    // 查询该访客的开门记录
    const res = await VisitorAccessRecordApi.getAccessRecords(visitor.id)
    records.value = res || []
  } catch (e) {
    console.error('获取开门记录失败', e)
    records.value = []
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.empty-tip {
  text-align: center;
  padding: 40px;
  color: var(--el-text-color-secondary);
}

.door-method {
  color: #409eff;
}
</style>
