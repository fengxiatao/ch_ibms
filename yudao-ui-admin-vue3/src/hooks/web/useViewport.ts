import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 获取浏览器视口尺寸的Hook
 */
export const useViewport = () => {
  // 视口宽度
  const viewportWidth = ref(0)
  // 视口高度
  const viewportHeight = ref(0)
  // 可用高度（减去浏览器UI）
  const availableHeight = ref(0)

  // 更新视口尺寸
  const updateViewport = () => {
    viewportWidth.value = window.innerWidth
    viewportHeight.value = window.innerHeight
    availableHeight.value = window.innerHeight
    
    // 设置CSS自定义属性，供全局使用
    document.documentElement.style.setProperty('--viewport-width', `${viewportWidth.value}px`)
    document.documentElement.style.setProperty('--viewport-height', `${viewportHeight.value}px`)
    document.documentElement.style.setProperty('--available-height', `${availableHeight.value}px`)
  }

  // 防抖函数
  let resizeTimer: number | null = null
  const debouncedUpdate = () => {
    if (resizeTimer) {
      clearTimeout(resizeTimer)
    }
    resizeTimer = window.setTimeout(() => {
      updateViewport()
      resizeTimer = null
    }, 100)
  }

  onMounted(() => {
    // 初始化
    updateViewport()
    
    // 监听窗口大小变化
    window.addEventListener('resize', debouncedUpdate, { passive: true })
    
    // 监听屏幕方向变化（移动端）
    window.addEventListener('orientationchange', debouncedUpdate, { passive: true })
  })

  onUnmounted(() => {
    // 清理事件监听
    window.removeEventListener('resize', debouncedUpdate)
    window.removeEventListener('orientationchange', debouncedUpdate)
    
    if (resizeTimer) {
      clearTimeout(resizeTimer)
    }
  })

  return {
    viewportWidth,
    viewportHeight,
    availableHeight,
    updateViewport
  }
}

/**
 * 计算页面内容区域高度的Hook
 */
export const useContentHeight = () => {
  const { viewportHeight } = useViewport()
  
  // 计算内容区域高度（减去header、footer等固定高度）
  const getContentHeight = (excludeHeight = 0) => {
    return viewportHeight.value - excludeHeight
  }
  
  // 获取带单位的高度字符串
  const getContentHeightPx = (excludeHeight = 0) => {
    return `${getContentHeight(excludeHeight)}px`
  }
  
  // 获取CSS calc表达式
  const getContentHeightCalc = (excludeHeightVar = '') => {
    if (excludeHeightVar) {
      return `calc(var(--viewport-height) - ${excludeHeightVar})`
    }
    return 'var(--viewport-height)'
  }

  return {
    viewportHeight,
    getContentHeight,
    getContentHeightPx,
    getContentHeightCalc
  }
}
