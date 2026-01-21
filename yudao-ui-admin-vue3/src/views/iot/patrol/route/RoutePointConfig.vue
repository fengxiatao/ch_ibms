<template>
  <Dialog title="配置巡更点位" v-model="dialogVisible" width="900px">
    <div v-loading="formLoading">
      <!-- 线路信息 -->
      <el-alert :closable="false" type="info" class="mb-3">
        <template #title>
          <span class="font-bold">{{ routeInfo.name }}</span>
          <span class="ml-4 text-sm">
            规则：<el-tag size="small">{{ routeInfo.rule === 1 ? '顺序' : '无序' }}</el-tag>
          </span>
          <span class="ml-2 text-sm">
            已配置：<el-tag size="small" type="success">{{ selectedPoints.length }}</el-tag> 个点位
          </span>
        </template>
      </el-alert>

      <el-row :gutter="20">
        <!-- 左侧：可选点位列表 -->
        <el-col :span="12">
          <div class="panel">
            <div class="panel-header">
              <span>可选点位</span>
              <el-input
                v-model="searchKeyword"
                placeholder="搜索点位名称"
                clearable
                size="small"
                class="!w-200px"
              >
                <template #prefix>
                  <Icon icon="ep:search" />
                </template>
              </el-input>
            </div>
            <div class="panel-body">
              <el-scrollbar height="400px">
                <div
                  v-for="point in filteredAvailablePoints"
                  :key="point.id"
                  class="point-item"
                  @click="addPoint(point)"
                >
                  <div class="point-info">
                    <div class="point-name">{{ point.name }}</div>
                    <div class="point-code">{{ point.code }}</div>
                  </div>
                  <el-button type="primary" link>
                    <Icon icon="ep:plus" />
                  </el-button>
                </div>
                <el-empty v-if="filteredAvailablePoints.length === 0" description="暂无可选点位" />
              </el-scrollbar>
            </div>
          </div>
        </el-col>

        <!-- 右侧：已选点位列表 -->
        <el-col :span="12">
          <div class="panel">
            <div class="panel-header">
              <span>已选点位（可拖拽排序）</span>
            </div>
            <div class="panel-body">
              <el-scrollbar height="400px">
                <draggable
                  v-model="selectedPoints"
                  item-key="pointId"
                  handle=".drag-handle"
                  animation="300"
                >
                  <template #item="{ element, index }">
                    <div class="point-item selected">
                      <div class="point-info">
                        <Icon icon="ep:menu" class="drag-handle" />
                        <span class="point-order">{{ index + 1 }}</span>
                        <div>
                          <div class="point-name">{{ element.pointName }}</div>
                          <el-input-number
                            v-model="element.expectedDuration"
                            :min="0"
                            :max="999"
                            size="small"
                            class="mt-1"
                            placeholder="预计分钟"
                          />
                        </div>
                      </div>
                      <el-button type="danger" link @click="removePoint(index)">
                        <Icon icon="ep:delete" />
                      </el-button>
                    </div>
                  </template>
                </draggable>
                <el-empty v-if="selectedPoints.length === 0" description="请从左侧添加点位" />
              </el-scrollbar>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm" :loading="formLoading">保 存</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import draggable from 'vuedraggable'
import * as PatrolRouteApi from '@/api/iot/patrol/route'
import * as PatrolPointApi from '@/api/iot/patrol/point'

const message = useMessage()
const emit = defineEmits(['success'])

const dialogVisible = ref(false)
const formLoading = ref(false)
const searchKeyword = ref('')

const routeInfo = ref<any>({
  id: undefined,
  name: '',
  rule: 1
})

const allPoints = ref<PatrolPointApi.PatrolPointVO[]>([])
const selectedPoints = ref<PatrolRouteApi.PatrolRoutePointVO[]>([])

// 过滤可选点位
const filteredAvailablePoints = computed(() => {
  const selectedIds = selectedPoints.value.map(p => p.pointId)
  let points = allPoints.value.filter(p => !selectedIds.includes(p.id!))
  
  if (searchKeyword.value) {
    points = points.filter(p => p.name.includes(searchKeyword.value))
  }
  
  return points
})

// 打开弹窗
const open = async (id: number) => {
  dialogVisible.value = true
  searchKeyword.value = ''
  formLoading.value = true
  
  try {
    // 加载线路信息
    const routeData = await PatrolRouteApi.getPatrolRoute(id)
    routeInfo.value = routeData
    selectedPoints.value = routeData.points || []
    
    // 加载所有点位
    await loadAllPoints()
  } finally {
    formLoading.value = false
  }
}

// 加载所有点位
const loadAllPoints = async () => {
  try {
    const data = await PatrolPointApi.getPatrolPointPage({
      pageNo: 1,
      pageSize: 1000
    })
    allPoints.value = data.list
  } catch (error) {
    console.error('加载点位失败', error)
  }
}

// 添加点位
const addPoint = (point: PatrolPointApi.PatrolPointVO) => {
  selectedPoints.value.push({
    pointId: point.id!,
    pointName: point.name,
    pointOrder: selectedPoints.value.length + 1,
    expectedDuration: 5 // 默认5分钟
  })
}

// 移除点位
const removePoint = (index: number) => {
  selectedPoints.value.splice(index, 1)
  // 重新排序
  selectedPoints.value.forEach((p, i) => {
    p.pointOrder = i + 1
  })
}

// 提交表单
const submitForm = async () => {
  if (selectedPoints.value.length === 0) {
    message.warning('请至少添加一个巡更点位')
    return
  }
  
  formLoading.value = true
  try {
    // 更新点位顺序
    selectedPoints.value.forEach((p, i) => {
      p.pointOrder = i + 1
    })
    
    const data = {
      ...routeInfo.value,
      points: selectedPoints.value,
      pointCount: selectedPoints.value.length
    }
    
    await PatrolRouteApi.updatePatrolRoute(data)
    message.success('配置成功')
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

defineExpose({ open })

onMounted(() => {
  loadAllPoints()
})
</script>

<style scoped lang="scss">
.panel {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  
  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background-color: #f5f7fa;
    border-bottom: 1px solid #dcdfe6;
    font-weight: bold;
  }
  
  .panel-body {
    padding: 12px;
  }
}

.point-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  margin-bottom: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    border-color: #409eff;
    background-color: #ecf5ff;
  }
  
  &.selected {
    background-color: #f0f9ff;
    border-color: #b3d8ff;
    cursor: move;
  }
  
  .point-info {
    display: flex;
    align-items: center;
    flex: 1;
    gap: 12px;
    
    .drag-handle {
      cursor: move;
      font-size: 18px;
      color: #909399;
    }
    
    .point-order {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 24px;
      height: 24px;
      background-color: #409eff;
      color: #fff;
      border-radius: 50%;
      font-size: 12px;
      font-weight: bold;
    }
    
    .point-name {
      font-weight: bold;
      margin-bottom: 4px;
    }
    
    .point-code {
      font-size: 12px;
      color: #909399;
    }
  }
}
</style>


























