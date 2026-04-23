<script lang="ts" setup>
import type { EChartsOption } from 'echarts'
import echarts from '@/plugins/echarts'
import { debounce } from 'lodash-es'
import 'echarts-wordcloud'
import { propTypes } from '@/utils/propTypes'
import { PropType } from 'vue'
import { useAppStore } from '@/store/modules/app'
import { isString } from '@/utils/is'
import { useDesign } from '@/hooks/web/useDesign'
import { useRoute } from 'vue-router'

defineOptions({ name: 'EChart' })

const { getPrefixCls, variables } = useDesign()

const prefixCls = getPrefixCls('echart')

const appStore = useAppStore()
const route = useRoute()

const props = defineProps({
  options: {
    type: Object as PropType<EChartsOption>,
    required: true
  },
  width: propTypes.oneOfType([Number, String]).def(''),
  height: propTypes.oneOfType([Number, String]).def('500px')
})

const isDark = computed(() => appStore.getIsDark)

const theme = computed(() => {
  const echartTheme: boolean | string = unref(isDark) ? true : 'auto'

  return echartTheme
})

const options = computed(() => {
  return {
    ...(props.options as any),
    darkMode: unref(theme)
  } as EChartsOption
})

const elRef = ref<ElRef>()

let echartRef: Nullable<echarts.ECharts> = null

const contentEl = ref<Element>()
let resizeObserver: ResizeObserver | null = null

const getChartEl = () => unref(elRef) as unknown as HTMLElement | undefined

const hasSize = (el?: HTMLElement) => {
  if (!el) return false
  return el.clientWidth > 0 && el.clientHeight > 0
}

const safeResize = () => {
  if (!echartRef) return
  try {
    echartRef.resize()
  } catch (e) {
    // eslint-disable-next-line no-console
    console.error('[EChart] resize 失败', { err: e })
  }
}

const safeSetOption = (opt: EChartsOption) => {
  if (!echartRef) return false
  try {
    echartRef.setOption(opt)
    return true
  } catch (e) {
    // 常见原因：dataset.dimensions 与 source 字段不匹配（会抛 “value does not exist.”）
    const dataset: any = (opt as any)?.dataset
    const series: any = (opt as any)?.series
    const source =
      dataset && typeof dataset === 'object'
        ? Array.isArray(dataset.source)
          ? dataset.source.slice(0, 3)
          : dataset.source
        : undefined
    const seriesType = Array.isArray(series) ? series.map((s) => s?.type) : series?.type
    const seriesEncode = Array.isArray(series) ? series.map((s) => s?.encode) : series?.encode
    const titleText = Array.isArray((opt as any)?.title)
      ? (opt as any).title?.[0]?.text
      : (opt as any)?.title?.text
    const errMsg = (e as any)?.message ?? String(e)
    const errorKey = JSON.stringify({
      errMsg,
      fullPath: route.fullPath,
      titleText,
      seriesType,
      seriesEncode,
      datasetDimensions: dataset?.dimensions
    })
    ;(safeSetOption as any)._last ??= { key: '', at: 0 }
    const last = (safeSetOption as any)._last as { key: string; at: number }
    const now = Date.now()
    if (last.key === errorKey && now - last.at < 1500) {
      return false
    }
    last.key = errorKey
    last.at = now

    // eslint-disable-next-line no-console
    console.error('[EChart] setOption 失败', {
      err: e,
      route: route.fullPath,
      titleText,
      datasetDimensions: dataset?.dimensions,
      datasetSourceSample: source,
      seriesType,
      seriesEncode,
      optionKeys: opt ? Object.keys(opt as any) : []
    })
    // 不向上抛出，避免路由切换/TagsView 等场景被 ECharts 断言打断
    // 同时销毁实例，避免后续 resize/transition 继续触发 ECharts 内部异常
    try {
      echartRef?.dispose()
    } catch (disposeErr) {
      // eslint-disable-next-line no-console
      console.error('[EChart] dispose 失败', { err: disposeErr })
    } finally {
      echartRef = null
    }
    return false
  }
}

const styles = computed(() => {
  const width = isString(props.width) ? props.width : `${props.width}px`
  const height = isString(props.height) ? props.height : `${props.height}px`

  return {
    width,
    height
  }
})

const initChart = () => {
  const el = getChartEl()
  if (!props.options || !el) return
  if (echartRef) return
  if (!hasSize(el)) return
  echartRef = echarts.init(el)
  if (safeSetOption(unref(options))) safeResize()
}

watch(
  () => options.value,
  (options) => {
    if (!echartRef) initChart()
    if (!echartRef) return
    if (safeSetOption(options)) safeResize()
  },
  {
    deep: true
  }
)

const resizeHandler = debounce(() => {
  safeResize()
}, 100)

const contentResizeHandler = async (e: TransitionEvent) => {
  if (e.propertyName === 'width') {
    resizeHandler()
  }
}

onMounted(() => {
  initChart()

  window.addEventListener('resize', resizeHandler)

  contentEl.value = document.getElementsByClassName(`${variables.namespace}-layout-content`)[0]
  unref(contentEl) &&
    (unref(contentEl) as Element).addEventListener('transitionend', contentResizeHandler)

  const el = getChartEl()
  if (el && typeof ResizeObserver !== 'undefined') {
    resizeObserver = new ResizeObserver(() => {
      if (!echartRef) initChart()
      safeResize()
    })
    resizeObserver.observe(el)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeHandler)
  unref(contentEl) &&
    (unref(contentEl) as Element).removeEventListener('transitionend', contentResizeHandler)
  resizeObserver?.disconnect()
  resizeObserver = null
  if (echartRef) {
    echartRef.dispose()
    echartRef = null
  }
})

onActivated(() => {
  if (!echartRef) initChart()
  safeResize()
})
</script>

<template>
  <div ref="elRef" :class="[$attrs.class, prefixCls]" :style="styles"></div>
</template>
