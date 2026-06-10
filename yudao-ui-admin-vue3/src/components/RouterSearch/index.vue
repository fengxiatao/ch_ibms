<template>
  <ElDialog v-if="isModal" v-model="showSearch" :show-close="false" title="菜单搜索">
    <el-select
      filterable
      :reserve-keyword="false"
      remote
      placeholder="请输入菜单内容"
      :remote-method="remoteMethod"
      style="width: 100%"
      @change="handleChange"
    >
      <el-option
        v-for="item in options"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
    </el-select>
  </ElDialog>
  <div v-else class="custom-hover" @click.stop="showTopSearch = !showTopSearch">
    <Icon icon="ep:search" />
    <el-select
      @click.stop
      filterable
      :reserve-keyword="false"
      remote
      placeholder="请输入菜单内容"
      :remote-method="remoteMethod"
      class="overflow-hidden transition-all-600"
      :class="showTopSearch ? '!w-220px ml2' : '!w-0'"
      @change="handleChange"
    >
      <el-option
        v-for="item in options"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
    </el-select>
  </div>
</template>

<script lang="ts" setup>
defineProps({
  isModal: {
    type: Boolean,
    default: true
  }
})

const router = useRouter() // 路由对象
const showSearch = ref(false) // 是否显示弹框
const showTopSearch = ref(false) // 是否显示顶部搜索框
const value: Ref = ref('') // 用户输入的值

const routers = router.getRoutes() // 路由对象

const isSearchableRoute = (item: any) => {
  const meta = item.meta || {}

  // 只允许搜索真正的业务页面；目录/布局/重定向路由会把 Layout 套进内容区，形成“页面画中画”。
  return (
    item.path &&
    item.name &&
    meta.title &&
    !item.redirect &&
    (!item.children || item.children.length === 0) &&
    (!meta.hidden || meta.canTo)
  )
}

const options = computed(() => {
  // 提示选项
  const keyword = String(value.value || '').trim().toLowerCase()
  if (!keyword) {
    return []
  }

  const matchedRoutes = routers.filter((item: any) => {
    if (!isSearchableRoute(item)) {
      return false
    }

    const title = String(item.meta.title || '').toLowerCase()
    const path = String(item.path || '').toLowerCase()
    return title.includes(keyword) || path.includes(keyword)
  })

  const existed = new Set<string>()
  return matchedRoutes.reduce<{ label: string; value: string }[]>((list, item: any) => {
    if (existed.has(item.path)) {
      return list
    }
    existed.add(item.path)
    list.push({
      label: `${item.meta.title} ${item.path}`,
      value: item.path
    })
    return list
  }, [])
})

function remoteMethod(data) {
  // 这里可以执行相应的操作（例如打开搜索框等）
  value.value = data
}

function handleChange(path) {
  router.push({ path })
  hiddenSearch()
  hiddenTopSearch()
}

function hiddenSearch() {
  showSearch.value = false
}

function hiddenTopSearch() {
  showTopSearch.value = false
}

onMounted(() => {
  window.addEventListener('keydown', listenKey)
  window.addEventListener('click', hiddenTopSearch)
})

onUnmounted(() => {
  window.removeEventListener('keydown', listenKey)
  window.removeEventListener('click', hiddenTopSearch)
})

// 监听 ctrl + k
function listenKey(event) {
  if ((event.ctrlKey || event.metaKey) && event.key === 'k') {
    // 阻止触发浏览器默认事件
    event.preventDefault()
    showSearch.value = !showSearch.value
    // 这里可以执行相应的操作（例如打开搜索框等）
  }
}

defineExpose({
  openSearch: () => {
    showSearch.value = true
  }
})
</script>
