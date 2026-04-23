<!--
  将 public 下的静态原型 HTML 以“非 iframe”的方式加载进 Vue 页面：
  - 仅提取指定选择器的内容（通常是 main 或 .main-content），以去掉原型左侧菜单
  - 动态注入原型内的 <style> 到 head，并在卸载时移除，避免污染其他页面
  - 使用 v-dompurify-html 防止 v-html 安全风险
-->
<template>
  <div class="proto-html-loader">
    <div v-if="loading" class="proto-loading">加载中...</div>
    <div v-else-if="error" class="proto-error">{{ error }}</div>
    <div v-else class="proto-html" v-dompurify-html="html"></div>
  </div>
</template>

<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'

defineOptions({ name: 'PrototypeHtmlLoader' })

const props = defineProps<{
  srcUrl: string
  contentSelector: string
  styleSelector?: string
  extraCss?: string
}>()

const loading = ref(true)
const error = ref<string | null>(null)
const html = ref('')

let styleEl: HTMLStyleElement | null = null

const defaultThemeCss = `
  .proto-html {
    background: var(--app-content-bg-color);
    color: var(--el-text-color-primary);
    --primary: var(--el-color-primary);
    --success: var(--el-color-success);
    --warning: var(--el-color-warning);
    --danger: var(--el-color-danger);
    --info: var(--el-color-info);
    --border: var(--el-border-color);
    --text: var(--el-text-color-primary);
    --text-regular: var(--el-text-color-regular);
    --text-primary: var(--el-text-color-primary);
    --text-secondary: var(--el-text-color-regular);
    --bg: var(--app-content-bg-color);
    --bg-primary: var(--app-content-bg-color);
    --bg-secondary: var(--el-bg-color-overlay);
  }

  .dark .proto-html {
    background: var(--app-content-bg-color);
    color: var(--el-text-color-primary);
  }

  .dark .proto-html .card,
  .dark .proto-html .panel,
  .dark .proto-html .table-container,
  .dark .proto-html .modal-content,
  .dark .proto-html .filter-bar,
  .dark .proto-html .stats-bar,
  .dark .proto-html .history-panel,
  .dark .proto-html .analysis-panel,
  .dark .proto-html .settings-card {
    background: var(--el-bg-color-overlay) !important;
    border-color: var(--el-border-color) !important;
  }
`

/**
 * 安全移除注入的样式
 */
const cleanupStyle = () => {
  try {
    styleEl?.remove()
  } finally {
    styleEl = null
  }
}

onMounted(async () => {
  loading.value = true
  error.value = null
  html.value = ''

  try {
    const res = await fetch(props.srcUrl, { credentials: 'same-origin' })
    if (!res.ok) {
      throw new Error(`加载失败：${res.status} ${res.statusText}`)
    }
    const text = await res.text()
    const doc = new DOMParser().parseFromString(text, 'text/html')

    const contentEl = doc.querySelector(props.contentSelector)
    if (!contentEl) {
      throw new Error(`未找到内容节点：${props.contentSelector}`)
    }

    const styleSel = props.styleSelector || 'head style'
    const styleNode = doc.querySelector(styleSel)
    const cssText = `${styleNode?.textContent ?? ''}\n${defaultThemeCss}\n${props.extraCss ?? ''}`.trim()
    if (cssText) {
      styleEl = document.createElement('style')
      styleEl.setAttribute('data-newlight-proto-style', props.srcUrl)
      styleEl.textContent = cssText
      document.head.appendChild(styleEl)
    }

    html.value = contentEl.outerHTML
  } catch (e) {
    cleanupStyle()
    error.value = (e as any)?.message ?? String(e)
  } finally {
    loading.value = false
  }
})

onBeforeUnmount(() => {
  cleanupStyle()
})
</script>

<style scoped>
.proto-loading,
.proto-error {
  padding: 16px;
  color: var(--el-text-color-regular);
}
</style>
