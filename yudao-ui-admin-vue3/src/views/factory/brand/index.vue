<script setup lang="ts">
import FactoryModuleLanding from '@/views/factory/components/FactoryModuleLanding.vue'

defineOptions({ name: 'FactoryBrand' })

const highlights = [
  '承接品牌实力、工厂形象、资质荣誉与对外展示内容。',
  '后续结合真实品牌资产、案例素材与多媒体内容进行建设。',
  '展示内容统一由真实素材驱动，不补前端伪造品牌指标。'
]
</script>

<template>
  <FactoryModuleLanding
    title="品牌展示"
    subtitle="统一承接智慧工厂品牌形象、案例与宣传展示入口。"
    status-text="当前阶段：已预留正式页面入口，待接入真实展示素材。"
    accent="amber"
    :highlights="highlights"
  />
</template>
