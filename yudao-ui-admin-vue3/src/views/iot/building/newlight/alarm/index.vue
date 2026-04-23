<!--
  原型复刻：06 告警信息.html
  说明：不包含原型左侧菜单（项目统一侧边栏已提供）；页面使用等比缩放容器适配 main 区域。
-->
<template>
  <div class="newlight-page wh-full">
    <ProtoScaleContainer :design-width="1440" :design-height="900" scale-by="width" class="wh-full">
      <PrototypeHtmlLoader
        :src-url="srcUrl"
        content-selector="main.main-content"
        :extra-css="extraCss"
        class="proto-wrap"
      />
    </ProtoScaleContainer>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import ProtoScaleContainer from '../ProtoScaleContainer.vue'
import PrototypeHtmlLoader from '../PrototypeHtmlLoader.vue'

defineOptions({ name: 'NewLightAlarm' })

const srcUrl = computed(() => {
  return encodeURI('/智能照明V2 (20260310)/智能照明/06 告警信息.html')
})

const extraCss = `
  .main-content { margin-left: 0 !important; width: 100% !important; }
`
</script>

<style scoped>
.proto-wrap {
  width: 100%;
  min-height: 100vh;
}
</style>

