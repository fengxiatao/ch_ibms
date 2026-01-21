<template>
  

  <ContentWrap style="margin-top: 70px;">
    <IFrame v-if="!loading" v-loading="loading" :src="src" />
  </ContentWrap>
</template>
<script lang="ts" setup>
import * as ConfigApi from '@/api/infra/config'

defineOptions({ name: 'InfraSkyWalking' })

const loading = ref(true) // 是否加载中
const src = ref('http://skywalking.shop.changhui-tech.com')

/** 初始化 */
onMounted(async () => {
  try {
    const data = await ConfigApi.getConfigKey('url.skywalking')
    if (data && data.length > 0) {
      src.value = data
    }
  } finally {
    loading.value = false
  }
})
</script>
