<template>
  <Dialog title="巡更记录详情" v-model="dialogVisible" width="800px">
    <div v-loading="loading">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="记录ID">
          {{ recordData.id }}
        </el-descriptions-item>
        <el-descriptions-item label="记录状态">
          <el-tag v-if="recordData.status === 1" type="success">正常</el-tag>
          <el-tag v-else-if="recordData.status === 2" type="danger">异常</el-tag>
          <el-tag v-else-if="recordData.status === 3" type="warning">超时</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="任务ID">
          {{ recordData.taskId }}
        </el-descriptions-item>
        <el-descriptions-item label="点位名称">
          {{ recordData.pointName }}
        </el-descriptions-item>
        <el-descriptions-item label="执行人">
          {{ recordData.executorName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="执行人ID">
          {{ recordData.executorId || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="记录时间">
          {{ formatDate(recordData.recordTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="是否超时">
          <el-tag v-if="recordData.isTimeout" type="warning">是</el-tag>
          <el-tag v-else type="success">否</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="超时时长" v-if="recordData.isTimeout">
          <el-tag type="danger">{{ recordData.timeoutDuration }} 分钟</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="recordData.isTimeout ? 1 : 2">
          {{ formatDate(recordData.createTime) }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 位置信息 -->
      <el-divider content-position="left">位置信息</el-divider>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="经度">
          {{ recordData.longitude || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="纬度">
          {{ recordData.latitude || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="高度">
          {{ recordData.altitude ? recordData.altitude + 'm' : '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 现场图片 -->
      <el-divider content-position="left">现场图片</el-divider>
      <div v-if="imageUrls.length > 0" class="image-gallery">
        <el-image
          v-for="(url, index) in imageUrls"
          :key="index"
          :src="url"
          :preview-src-list="imageUrls"
          :initial-index="index"
          fit="cover"
          class="gallery-item"
        />
      </div>
      <el-empty v-else description="暂无现场图片" :image-size="100" />

      <!-- 备注信息 -->
      <el-divider content-position="left">备注信息</el-divider>
      <el-input
        v-model="recordData.remark"
        type="textarea"
        :rows="4"
        placeholder="暂无备注"
        readonly
      />
    </div>
    
    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { formatDate } from '@/utils/formatTime'

const dialogVisible = ref(false)
const loading = ref(false)
const recordData = ref<any>({})

// 解析图片URL列表
const imageUrls = computed(() => {
  if (!recordData.value.imageUrls) return []
  try {
    const urls = JSON.parse(recordData.value.imageUrls)
    return Array.isArray(urls) ? urls : []
  } catch (error) {
    return []
  }
})

// 打开弹窗
const open = (record: any) => {
  dialogVisible.value = true
  recordData.value = { ...record }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.image-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
  
  .gallery-item {
    width: 100%;
    height: 150px;
    border-radius: 4px;
    cursor: pointer;
    transition: transform 0.3s;
    
    &:hover {
      transform: scale(1.05);
    }
  }
}
</style>


























