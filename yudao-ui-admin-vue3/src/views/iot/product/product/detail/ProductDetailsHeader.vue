<template>  
  <ContentWrap style="margin-top: 70px;" class="mt-10px">
  <div>
    <div class="flex items-start justify-between">
      <div>
        <el-col>
          <el-row>
            <span class="text-xl font-bold">{{ product.name }}</span>
          </el-row>
        </el-col>
      </div>
      <div>
        <!-- 右上：按钮 -->
        <el-button
          @click="openForm('update', product.id)"
          v-hasPermi="['iot:product:update']"
          :disabled="product.status === 1"
        >
          编辑
        </el-button>
        <el-button
          type="primary"
          @click="confirmPublish(product.id)"
          v-hasPermi="['iot:product:update']"
          v-if="product.status === 0"
        >
          发布
        </el-button>
        <el-button
          type="danger"
          @click="confirmUnpublish(product.id)"
          v-if="product.status === 1"
        >
          撤销发布
        </el-button>
      </div>
    </div>
  </div>
    <el-descriptions :column="1" direction="horizontal">
      <el-descriptions-item label="ProductKey">
        {{ product.productKey }}
        <el-button @click="copyToClipboard(product.productKey)">复制</el-button>
      </el-descriptions-item>
      <el-descriptions-item label="设备总数">
        <span class="ml-20px mr-10px">{{ product.deviceCount ?? '加载中...' }}</span>
        <el-button @click="goToDeviceList(product.id)">前往管理</el-button>
      </el-descriptions-item>
    </el-descriptions>
  <!-- 表单弹窗：添加/修改 -->
  <ProductForm ref="formRef" @success="emit('refresh')" />  
</ContentWrap>
</template>
<script setup lang="ts">
import ProductForm from '@/views/iot/product/product/ProductForm.vue'
import { ProductApi, ProductVO } from '@/api/iot/product/product'
import { onBeforeUnmount, nextTick } from 'vue'

const message = useMessage()

const { product } = defineProps<{ product: ProductVO }>() // 定义 Props

// 用于跟踪组件是否已卸载
let isUnmounted = false
onBeforeUnmount(() => {
  isUnmounted = true
})

// 安全显示消息的辅助函数
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

/** 复制到剪贴板方法 */
const copyToClipboard = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    safeMessage.success('复制成功')
  } catch (error) {
    safeMessage.error('复制失败')
  }
}

/** 路由跳转到设备管理 */
const { push } = useRouter()
const goToDeviceList = (productId: number) => {
  push({ name: 'IoTDevice', params: { productId } })
}

/** 修改操作 */
const emit = defineEmits(['refresh']) // 定义 Emits
const formRef = ref()
const openForm = (type: string, id?: number) => {
  if (!formRef.value) return // 防止 ref 为空
  formRef.value.open(type, id)
}

/** 发布操作 */
const confirmPublish = async (id: number) => {
  try {
    await ProductApi.updateProductStatus(id, 1)
    if (isUnmounted) return // 组件已卸载，不执行后续操作
    safeMessage.success('发布成功')
    emit('refresh')
  } catch (error) {
    if (isUnmounted) return // 组件已卸载，不显示错误消息
    safeMessage.error('发布失败')
  }
}

/** 撤销发布操作 */
const confirmUnpublish = async (id: number) => {
  try {
    await ProductApi.updateProductStatus(id, 0)
    if (isUnmounted) return // 组件已卸载，不执行后续操作
    safeMessage.success('撤销发布成功')
    emit('refresh')
  } catch (error) {
    if (isUnmounted) return // 组件已卸载，不显示错误消息
    safeMessage.error('撤销发布失败')
  }
}
</script>
