<!-- 设备信息（头部） -->
<template>
  <div v-if="device && product">
    <div class="flex items-start justify-between">
      <div>
        <el-col>
          <el-row>
            <span class="text-xl font-bold">{{ device.deviceName || '-' }}</span>
          </el-row>
        </el-col>
      </div>
      <div>
        <!-- 右上：按钮 -->
        <el-button
          v-if="product.status === 0 && device.id"
          v-hasPermi="['iot:device:update']"
          @click="openForm('update', device.id)"
        >
          编辑
        </el-button>
      </div>
    </div>
  </div>
  <ContentWrap v-if="product" style="margin-top: 70px;" class="mt-10px">
    <el-descriptions :column="5" direction="horizontal">
      <el-descriptions-item label="产品">
        <el-link v-if="product.id" @click="goToProductDetail(product.id)">{{ product.name || '-' }}</el-link>
        <span v-else>{{ product.name || '-' }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="ProductKey">
        {{ product.productKey || '-' }}
        <el-button v-if="product.productKey" @click="copyToClipboard(product.productKey)">复制</el-button>
      </el-descriptions-item>
    </el-descriptions>
  </ContentWrap>
  <!-- 表单弹窗：添加/修改 -->
  <DeviceForm ref="formRef" @success="emit('refresh')" />
</template>
<script setup lang="ts">
import { onBeforeUnmount, nextTick } from 'vue'
import DeviceForm from '@/views/iot/device/device/DeviceForm.vue'
import { ProductVO } from '@/api/iot/product/product'
import { DeviceVO } from '@/api/iot/device/device'

const message = useMessage()
const router = useRouter()

const { product, device } = defineProps<{ product: ProductVO; device: DeviceVO }>()
const emit = defineEmits(['refresh'])

// 组件卸载标记
let isUnmounted = false

// 安全消息提示
const safeMessage = {
  success: (msg: string) => {
    if (!isUnmounted) {
      nextTick(() => {
        if (!isUnmounted) message.success(msg)
      })
    }
  },
  error: (msg: string) => {
    if (!isUnmounted) {
      nextTick(() => {
        if (!isUnmounted) message.error(msg)
      })
    }
  }
}

/** 操作修改 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  if (!formRef.value) return // 防止 ref 为空
  formRef.value.open(type, id)
}

/** 复制到剪贴板方法 */
const copyToClipboard = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    safeMessage.success('复制成功')
  } catch (error) {
    safeMessage.error('复制失败')
  }
}

/** 跳转到产品详情页面 */
const goToProductDetail = (productId: number) => {
  if (!isUnmounted) {
    router.push({ name: 'IoTProductDetail', params: { id: productId } })
  }
}

/** 组件卸载前清理 */
onBeforeUnmount(() => {
  isUnmounted = true
})
</script>
