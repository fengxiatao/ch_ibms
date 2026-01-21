<template>
  <Dialog title="巡更点位详情" v-model="dialogVisible" width="700px">
    <div v-loading="loading">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="点位ID">
          {{ pointData.id }}
        </el-descriptions-item>
        <el-descriptions-item label="点位类型">
          <el-tag v-if="pointData.type === 1">普通</el-tag>
          <el-tag v-else-if="pointData.type === 2" type="success">RFID</el-tag>
          <el-tag v-else-if="pointData.type === 3" type="warning">二维码</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="点位名称">
          {{ pointData.name }}
        </el-descriptions-item>
        <el-descriptions-item label="点位编码">
          {{ pointData.code || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="园区">
          {{ pointData.campusName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="建筑">
          {{ pointData.buildingName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="楼层">
          {{ pointData.floorName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDate(pointData.createTime) }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">位置信息</el-divider>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="经度">
          {{ pointData.longitude ? pointData.longitude.toFixed(6) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="纬度">
          {{ pointData.latitude ? pointData.latitude.toFixed(6) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="高度">
          {{ pointData.altitude ? pointData.altitude + 'm' : '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">RFID信息</el-divider>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="RFID标签">
          {{ pointData.rfidTag || '未绑定' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">二维码</el-divider>
      <div v-if="pointData.qrCodeUrl" class="qrcode-container">
        <el-image
          :src="pointData.qrCodeUrl"
          fit="contain"
          style="width: 200px; height: 200px;"
        />
      </div>
      <div v-else class="text-center text-gray-400">
        暂无二维码
      </div>

      <el-divider content-position="left">描述信息</el-divider>
      <el-input
        v-model="pointData.description"
        type="textarea"
        :rows="3"
        placeholder="暂无描述"
        readonly
      />
    </div>
    
    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { formatDate } from '@/utils/formatTime'

const dialogVisible = ref(false)
const loading = ref(false)
const pointData = ref<any>({})

// 打开弹窗
const open = (point: any) => {
  dialogVisible.value = true
  pointData.value = { ...point }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.qrcode-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
</style>


























