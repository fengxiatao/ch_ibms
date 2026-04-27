<template>
  <ContentWrap>
    <!-- 列表视图 -->
    <el-table v-if="viewMode === 'list'" v-loading="loading" :data="list" stripe>
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="点位名称" align="center" prop="name" min-width="120" />
      <el-table-column label="点位编码" align="center" prop="code" min-width="120" />
      <el-table-column label="类型" align="center" prop="type" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.type === 1">普通</el-tag>
          <el-tag v-else-if="scope.row.type === 2" type="success">RFID</el-tag>
          <el-tag v-else-if="scope.row.type === 3" type="warning">二维码</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="园区" align="center" prop="campusName" min-width="100" />
      <el-table-column label="建筑" align="center" prop="buildingName" min-width="100" />
      <el-table-column label="楼层" align="center" prop="floorName" min-width="100" />
      <el-table-column label="坐标" align="center" min-width="150">
        <template #default="scope">
          <span v-if="scope.row.longitude && scope.row.latitude">
            {{ scope.row.longitude.toFixed(6) }}, {{ scope.row.latitude.toFixed(6) }}
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column label="操作" align="center" width="240" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['iot:epatrol-point:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['iot:epatrol-point:delete']"
          >
            删除
          </el-button>
          <el-button
            link
            type="success"
            @click="handleGenerateQrCode(scope.row.id)"
            v-if="scope.row.type === 3"
            v-hasPermi="['iot:epatrol-point:update']"
          >
            生成二维码
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination
      v-if="viewMode === 'list'"
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 🆕 地图视图 - 使用统一的空间布局基础组件 -->
    <SpatialLayoutBase
      v-if="viewMode === 'map'"
      :devices="pointsForMap"
      device-label="巡更点位"
      :show-left-panel="true"
      :show-right-panel="true"
      :enable-edit="true"
      :left-panel-span="6"
      :right-panel-span="6"
      right-panel-title="巡更点位列表"
      right-panel-title-icon="ep:location"
      @floor-change="handleFloorChange"
      @device-click="handlePointClick"
      @add="handleAddPoint"
      @refresh="handleRefreshMap"
    >
      <!-- 自定义右侧面板：点位列表 -->
      <template #right-panel>
        <el-scrollbar height="calc(100vh - 300px)">
          <div v-if="filteredPointsForMap.length === 0" class="empty-state">
            <el-empty description="当前楼层暂无巡更点位" :image-size="80" />
          </div>
          <div v-else class="point-items">
            <div
              v-for="point in filteredPointsForMap"
              :key="point.id"
              class="point-item"
              :class="{ active: selectedPointId === point.id }"
              @click="handlePointSelect(point)"
            >
              <div class="point-icon">
                <Icon icon="ep:location" />
              </div>
              <div class="point-info">
                <div class="point-name">{{ point.name }}</div>
                <div class="point-code">{{ point.code }}</div>
                <div class="point-type">
                  <el-tag v-if="point.type === 1" size="small">普通</el-tag>
                  <el-tag v-else-if="point.type === 2" type="success" size="small">RFID</el-tag>
                  <el-tag v-else-if="point.type === 3" type="warning" size="small">二维码</el-tag>
                </div>
              </div>
              <div class="point-actions">
                <el-button
                  link
                  type="primary"
                  size="small"
                  @click.stop="openForm('update', point.id)"
                >
                  编辑
                </el-button>
                <el-button
                  link
                  type="danger"
                  size="small"
                  @click.stop="handleDelete(point.id!)"
                  v-if="point.id"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </el-scrollbar>
      </template>
    </SpatialLayoutBase>
  </ContentWrap>

  <!-- 表单弹窗 -->
  <PointForm ref="formRef" @success="getList" />
  
  <!-- 🆕 点位详情弹窗（地图点击） -->
  <PointDetail ref="detailRef" />
</template>

<script setup lang="ts" name="PatrolPoint">
import { dateFormatter } from '@/utils/formatTime'
import { ref, computed, onMounted } from 'vue'
import * as PatrolPointApi from '@/api/iot/patrol/point'
import PointForm from './PointForm.vue'
import PointDetail from './PointDetail.vue'
import SpatialLayoutBase from '@/components/SpatialLayoutBase/index.vue'

import * as FloorDxfApi from '@/api/iot/spatial/floorDxf'
import { convertDxfToSvgWithBackendScale } from '@/utils/dxf/dxfToSvg'

const message = useMessage()

// 🆕 视图模式（默认使用地图视图）
const viewMode = ref<'list' | 'map'>('map')

// 列表数据
const loading = ref(true)
const list = ref<PatrolPointApi.PatrolPointVO[]>([])
const total = ref(0)

// 🆕 地图相关数据
const selectedFloorId = ref<number | undefined>()
const selectedPointId = ref<number | undefined>()

// 查询参数
const queryParams = ref({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  code: undefined,
  type: undefined
})

// 搜索（保留用于列表视图的搜索功能）
// const handleQuery = () => {
//   queryParams.value.pageNo = 1
//   getList()
// }

// 重置（保留用于列表视图的搜索功能）
// const resetQuery = () => {
//   queryParams.value = {
//     pageNo: 1,
//     pageSize: 10,
//     name: undefined,
//     code: undefined,
//     type: undefined
//   }
//   handleQuery()
// }

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const data = await PatrolPointApi.getPatrolPointPage(queryParams.value)
    if (data && Array.isArray(data.list)) {
      list.value = data.list
    } else if (data && data.list) {
      // 如果 data.list 不是数组，尝试转换
      list.value = [data.list].flat()
    } else {
      list.value = []
    }
    total.value = data?.total || 0
  } finally {
    loading.value = false
  }
}

// 新增/修改操作
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

// 删除操作
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await PatrolPointApi.deletePatrolPoint(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

// 生成二维码
const handleGenerateQrCode = async (id: number) => {
  try {
    await PatrolPointApi.generateQrCode(id)
    message.success('二维码生成成功')
    await getList()
  } catch {}
}

// 🆕 计算属性：转换为地图展示格式的点位数据
const pointsForMap = computed(() => {
  return list.value.map(point => ({
    ...point,
    deviceName: point.name,
    state: 1, // 点位默认在线状态
    localX: point.longitude,
    localY: point.latitude,
    localZ: point.altitude || 0,
    type: 'patrol-point' // 标记为巡更点位
  }))
})

// 🆕 计算属性：当前楼层过滤后的点位
const filteredPointsForMap = computed(() => {
  if (!selectedFloorId.value) return []
  return list.value.filter(point => point.floorId === selectedFloorId.value)
})

// 🆕 楼层变化事件
const handleFloorChange = (floorId: number | undefined) => {
  selectedFloorId.value = floorId
  selectedPointId.value = undefined
}

// 🆕 地图点位点击事件
const detailRef = ref()
const handlePointClick = (point: any) => {
  console.log('[巡更点位] 点击点位:', point)
  selectedPointId.value = point.id
  detailRef.value.open(point)
}

// 🆕 点位选择事件（右侧列表）
const handlePointSelect = (point: any) => {
  selectedPointId.value = point.id
  // 可以在这里添加高亮显示逻辑
}

// 🆕 添加点位（在平面图上）
const handleAddPoint = () => {
  if (!selectedFloorId.value) {
    message.warning('请先选择楼层')
    return
  }
  // 打开表单，并预设楼层ID
  formRef.value?.open('create', undefined, selectedFloorId.value)
}

// 🆕 刷新地图
const handleRefreshMap = () => {
  getList()
}

// 初始化
onMounted(() => {
  getList()
})

// 引入 DXF 相关 API 和转换工具
const floorPlanSvg = ref<string>('')
const loadFloorPlan = async (floorId: number) => {
  try {
    // 获取坐标比例
    const infoRes = await FloorDxfApi.getDxfInfo(floorId)
    const coordinateScale = infoRes.data.coordinateScale || 37.55

    // 获取图层
    const layersRes = await FloorDxfApi.getLayers(floorId)
    const selectedLayers = layersRes.data.layers.filter(layer => layer.isVisible).map(layer => layer.name)

    // 获取 DXF 内容
    const dxfContent = await FloorDxfApi.getDxfFileContent(floorId)

    // 转换为 SVG
    const result = convertDxfToSvgWithBackendScale(dxfContent, selectedLayers, coordinateScale, 1920, 1080)
    floorPlanSvg.value = result.svg
  } catch (error) {
    message.error('加载 DXF 平面图失败')
    floorPlanSvg.value = ''
  }
}

// 在楼层变更时加载
watch(selectedFloorId, (newId) => {
  if (newId) loadFloorPlan(newId)
})

// 统一坐标转换（与设备体系一致）
// const pointsForMap = computed(() => {
//   return list.value.map(point => {
//     const { localX, localY, localZ } = point // 使用设备坐标字段
//     // 应用转换逻辑（与 FloorMap 一致）
//     const pixelX = localX * coordinateScale
//     const pixelY = localY * coordinateScale
//     const svgX = pixelX + dxfOffsetX
//     const svgY = svgHeight - (pixelY + dxfOffsetY)
//     const xPercent = (svgX / svgWidth) * 100
//     const yPercent = (svgY / svgHeight) * 100
//     return { ...point, x: xPercent, y: yPercent }
//   })
// })

// 在模板中渲染 SVG
// <div v-if="floorPlanSvg" class="floor-plan-background" v-html="floorPlanSvg"></div>
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
}

.point-items {
  .point-item {
    display: flex;
    align-items: center;
    padding: 12px;
    margin-bottom: 8px;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      background: #f5f7fa;
      border-color: #409eff;
    }

    &.active {
      background: #ecf5ff;
      border-color: #409eff;
    }

    .point-icon {
      font-size: 24px;
      color: #409eff;
      margin-right: 12px;
    }

    .point-info {
      flex: 1;

      .point-name {
        font-weight: 500;
        margin-bottom: 4px;
      }

      .point-code {
        font-size: 12px;
        color: #909399;
        margin-bottom: 4px;
      }

      .point-type {
        font-size: 12px;
      }
    }

    .point-actions {
      display: flex;
      gap: 8px;
    }
  }
}
</style>

