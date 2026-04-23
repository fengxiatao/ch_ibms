<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

defineOptions({ name: 'FactoryDashboardIndex' })

const router = useRouter()

onMounted(() => {
  router.replace('/factory/cockpit')
})
</script>

<template>
  <div class="factory-dashboard-index"></div>
</template>

<style scoped lang="scss">
.factory-dashboard-index {
  min-height: 1px;
}
</style>
