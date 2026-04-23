<!--
  智能照明原型页面通用容器：根据外层容器尺寸对内容进行等比缩放（默认按宽度适配），以贴合 Layout 的 main 区域。
-->
<template>
  <div ref="hostRef" class="proto-scale-host">
    <div class="proto-scale-stage" :style="stageStyle">
      <slot></slot>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

defineOptions({ name: 'ProtoScaleContainer' })

type ScaleBy = 'width' | 'contain'

const props = withDefaults(
  defineProps<{
    designWidth?: number
    designHeight?: number
    maxScale?: number
    scaleBy?: ScaleBy
  }>(),
  {
    designWidth: 1440,
    designHeight: 900,
    maxScale: 1,
    scaleBy: 'width'
  }
)

const hostRef = ref<HTMLDivElement>()
const hostSize = ref({ width: 0, height: 0 })
let ro: ResizeObserver | null = null

/**
 * 计算缩放比例
 * @param hostWidth 外层容器宽度
 * @param hostHeight 外层容器高度
 * @param designWidth 设计稿宽度
 * @param designHeight 设计稿高度（仅 contain 模式使用）
 * @param mode 缩放策略
 * @param maxScale 最大缩放倍数
 * @returns 缩放比例（>=0）
 */
const calcScale = (
  hostWidth: number,
  hostHeight: number,
  designWidth: number,
  designHeight: number,
  mode: ScaleBy,
  maxScale: number
) => {
  if (!hostWidth || !designWidth) return 1
  const widthScale = hostWidth / designWidth
  const heightScale = designHeight ? hostHeight / designHeight : widthScale
  const raw = mode === 'contain' ? Math.min(widthScale, heightScale) : widthScale
  const safe = Number.isFinite(raw) ? raw : 1
  return Math.max(0, Math.min(safe, maxScale))
}

const scale = computed(() => {
  return calcScale(
    hostSize.value.width,
    hostSize.value.height,
    props.designWidth,
    props.designHeight,
    props.scaleBy,
    props.maxScale
  )
})

const stageStyle = computed(() => {
  return {
    width: `${props.designWidth}px`,
    transform: `scale(${scale.value})`,
    transformOrigin: '0 0'
  } as Record<string, string>
})

onMounted(() => {
  const host = hostRef.value
  if (!host) return

  const sync = () => {
    hostSize.value = { width: host.clientWidth, height: host.clientHeight }
  }
  sync()

  if (typeof ResizeObserver !== 'undefined') {
    ro = new ResizeObserver(() => sync())
    ro.observe(host)
  } else {
    window.addEventListener('resize', sync)
    ro = {
      observe: () => undefined,
      unobserve: () => undefined,
      disconnect: () => window.removeEventListener('resize', sync)
    } as unknown as ResizeObserver
  }
})

onBeforeUnmount(() => {
  ro?.disconnect()
  ro = null
})
</script>

<style scoped>
.proto-scale-host {
  width: 100%;
  height: 100%;
  overflow: auto;
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
  box-sizing: border-box;
}

.proto-scale-stage {
  height: auto;
  will-change: transform;
}
</style>
