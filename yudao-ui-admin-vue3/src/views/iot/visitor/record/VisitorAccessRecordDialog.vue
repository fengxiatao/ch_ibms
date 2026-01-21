<template>
  <Dialog v-model="dialogVisible" :title="`${visitorInfo.visitorName} - 通行记录`" width="800px">
    <div v-loading="loading">
      <!-- 访客基本信息 -->
      <el-descriptions :column="3" border class="mb-16">
        <el-descriptions-item label="访客姓名">{{ visitorInfo.visitorName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ visitorInfo.visitorPhone }}</el-descriptions-item>
        <el-descriptions-item label="被访人">{{ visitorInfo.visiteeName }}</el-descriptions-item>
      </el-descriptions>

      <!-- 通行记录列表 -->
      <el-table :data="records" size="small" max-height="400" stripe>
        <el-table-column label="设备名称" prop="deviceName" width="150" />
        <el-table-column label="通道名称" prop="channelName" width="120" />
        <el-table-column label="事件时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.eventTime) }}
          </template>
        </el-table-column>
        <el-table-column label="验证方式" prop="verifyMode" width="100" align="center">
          <template #default="{ row }">
            {{ getVerifyModeName(row.verifyMode) }}
          </template>
        </el-table-column>
        <el-table-column label="验证结果" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.success ? 'success' : 'danger'" size="small">
              {{ row.success ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="抓拍" width="80" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.captureUrl"
              :src="row.captureUrl"
              :preview-src-list="[row.captureUrl]"
              fit="cover"
              style="width: 40px; height: 40px; border-radius: 4px;"
            />
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="records.length === 0" class="empty-tip">
        暂无通行记录
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
import { AccessEventLogApi, type AccessEventLogVO } from '@/api/iot/access'
import type { VisitorApplyVO } from '@/api/iot/visitor'

const dialogVisible = ref(false)
const loading = ref(false)
const visitorInfo = ref<Partial<VisitorApplyVO>>({})
const records = ref<AccessEventLogVO[]>([])

// 格式化日期
const formatDateTime = (date: any) => {
  if (!date) return '-'
  return formatDate(date, 'YYYY-MM-DD HH:mm:ss')
}

// 验证方式名称
const getVerifyModeName = (mode: string | number) => {
  const modeMap: Record<string, string> = {
    'CARD': '刷卡',
    'FACE': '人脸',
    'FINGERPRINT': '指纹',
    'PASSWORD': '密码',
    'QR_CODE': '二维码',
    '1': '刷卡',
    '2': '指纹',
    '3': '人脸',
    '4': '密码'
  }
  return modeMap[String(mode)] || mode || '-'
}

// 打开弹窗
const open = async (visitor: VisitorApplyVO) => {
  visitorInfo.value = visitor
  dialogVisible.value = true
  loading.value = true

  try {
    // 查询该访客的通行记录
    // 通过访客编号或卡号查询
    const res = await AccessEventLogApi.getEventPage({
      pageNo: 1,
      pageSize: 100,
      personId: visitor.visitorId,
      startTime: visitor.planVisitTime ? formatDate(visitor.planVisitTime, 'YYYY-MM-DD HH:mm:ss') : undefined,
      endTime: visitor.actualLeaveTime ? formatDate(visitor.actualLeaveTime, 'YYYY-MM-DD HH:mm:ss') : undefined
    })
    records.value = res.list || []
  } catch (e) {
    console.error('获取通行记录失败', e)
    records.value = []
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.mb-16 {
  margin-bottom: 16px;
}

.empty-tip {
  text-align: center;
  padding: 40px;
  color: var(--el-text-color-secondary);
}
</style>
