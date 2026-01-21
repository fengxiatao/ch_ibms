<template>
  <div class="building-floor-tree">
    <el-tree
      :data="treeData"
      :props="{ label: 'name', children: 'floors' }"
      @node-click="handleNodeClick"
      :highlight-current="true"
      :default-expand-all="false"
      :expand-on-click-node="false"
    >
      <template #default="{ node, data }">
        <span class="custom-tree-node">
          <Icon :icon="data.floors ? 'ep:office-building' : 'ep:files'" />
          <span class="ml-2">{{ node.label }}</span>
          <el-tag v-if="!data.floors" size="small" class="ml-2" type="info">
            {{ getFloorDeviceCount(data.id) }} 个
          </el-tag>
        </span>
      </template>
    </el-tree>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import * as BuildingApi from '@/api/iot/spatial/building'
import * as FloorApi from '@/api/iot/spatial/floor'

interface Props {
  selectedFloorId?: number
  deviceCountMap?: Record<number, number>
}

const props = withDefaults(defineProps<Props>(), {
  deviceCountMap: () => ({})
})

const emit = defineEmits<{
  floorSelect: [floor: any]
}>()

const treeData = ref<any[]>([])

const loadTreeData = async () => {
  try {
    const buildings = await BuildingApi.getBuildingList()
    const data: any[] = []
    
    for (const building of buildings) {
      const floors = await FloorApi.getFloorListByBuildingId(building.id!)
      data.push({
        id: building.id,
        name: building.name,
        floors: floors.map(floor => ({
          id: floor.id,
          name: floor.name,
          buildingId: building.id,
          buildingName: building.name
        }))
      })
    }
    
    treeData.value = data
  } catch (error) {
    console.error('[建筑楼层树] 加载失败:', error)
  }
}

const handleNodeClick = (data: any) => {
  // 只处理楼层节点（没有 floors 属性）
  if (!data.floors) {
    emit('floorSelect', data)
  }
}

const getFloorDeviceCount = (floorId: number): number => {
  return props.deviceCountMap[floorId] || 0
}

watch(() => props.deviceCountMap, () => {
  // 设备数量变化时，可以触发更新
}, { deep: true })

onMounted(() => {
  loadTreeData()
})
</script>

<style scoped lang="scss">
.building-floor-tree {
  .custom-tree-node {
    display: flex;
    align-items: center;
    flex: 1;
    font-size: 14px;
  }
}
</style>

