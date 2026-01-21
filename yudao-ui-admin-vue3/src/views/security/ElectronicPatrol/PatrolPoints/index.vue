<template>
  <div class="patrol-points-container">
    <!-- 左侧空间节点统计面板（简化占位） -->
    <div class="left-panel">
      <h3>空间节点统计</h3>
      <div class="stat-item">请选择右侧级联加载楼层平面图</div>
    </div>

    <!-- 中间楼层平面图：复刻实时预览的DXF→SVG布局 -->
    <div class="center-panel">
      <h3>楼层平面图</h3>
      <div class="floor-map" v-if="floorPlanSvg">
        <!-- SVG 背景（由DXF转换而来） -->
        <div class="svg-container" v-html="floorPlanSvg"></div>
        <!-- 巡更点标注：百分比定位，随容器自适应 -->
        <div
          v-for="point in patrolPointsOnFloor"
          :key="point.id"
          class="patrol-point"
          :style="{ left: point.xPercent + '%', top: point.yPercent + '%' }"
          @click="handlePointClick(point)"
        >
          <el-tooltip :content="point.pointName || point.name || '巡更点'" placement="top">
            <el-icon class="point-icon"><Location /></el-icon>
          </el-tooltip>
        </div>
      </div>
      <el-empty v-else :description="emptyDescription" />
    </div>

    <!-- 右侧控制面板：采用实时预览样式组件（含园区/建筑/楼层） -->
    <div class="right-panel">
      <RightMapPanel
        :campuses="campuses"
        :buildings="buildings"
        :floors="floors"
        v-model:campusId="currentCampusId"
        v-model:buildingId="currentBuildingId"
        v-model:floorId="currentFloorId"
        :show-campus="true"
        :show-building="true"
        :show-floor="true"
        :layer-options="layerOptions"
        v-model:selectedLayers="selectedLayers"
        :actions="panelActions"
        @campus-change="handleCampusChange"
        @building-change="handleBuildingChange"
        @floor-change="handleFloorChange"
        @floor-up="goUpFloor"
        @floor-down="goDownFloor"
        @action="handlePanelAction"
      />
      <div class="operations">
        <el-button type="primary" @click="addPoint">添加巡更点</el-button>
        <el-button type="warning" @click="editPoint">编辑巡更点</el-button>
        <el-button type="danger" @click="deletePoint">删除巡更点</el-button>
        <el-button @click="refreshFloorData">刷新平面图与点位</el-button>
      </div>
    </div>

    <!-- 详情弹窗（点击点位） -->
    <PointDetail ref="detailRef" />
  </div>
  
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import PointDetail from '@/views/iot/patrol/point/PointDetail.vue'
import RightMapPanel from '@/views/security/VideoSurveillance/components/RightMapPanel.vue'

// 空间管理 API（园区/建筑/楼层级联）
import * as CampusApi from '@/api/iot/spatial/campus'
import * as BuildingApi from '@/api/iot/spatial/building'
import * as FloorApi from '@/api/iot/spatial/floor'

// 楼层 DXF → SVG API 与工具（与实时预览一致）
import * as FloorDxfApi from '@/api/iot/spatial/floorDxf'
import { convertDxfToSvgWithBackendScale } from '@/utils/dxf/dxfToSvg'

// 巡更点位 API（统一入口）
import { getPatrolPointPage, type PatrolPointPageReqVO, type PatrolPointVO } from '@/api/iot/patrol'

/**
 * 文件级说明：
 * 电子巡更 - 巡更点位页面
 * 复刻视频监控实时预览的平面图加载逻辑（DXF→SVG），并叠加巡更点位的坐标标注。
 * - 右侧采用与实时预览一致的样式组件 RightMapPanel，支持园区/建筑/楼层选择与楼层上下切换。
 * - 选中楼层后加载其DXF并转换为SVG；从后端分页接口查询当前楼层的巡更点位，并将坐标转换为百分比定位叠加在SVG上。
 * 注意：当前将 point.longitude/point.latitude 视为“楼层局部坐标（米）”。如后端为地理坐标，则需增加映射。
 */

// 园区/建筑/楼层列表与当前选择（与实时预览一致形式）
const campuses = ref<any[]>([])
const buildings = ref<any[]>([])
const floors = ref<any[]>([])
const currentCampusId = ref<number | undefined>(undefined)
const currentBuildingId = ref<number | undefined>(undefined)
const currentFloorId = ref<number | undefined>(undefined)

// 图层与操作（占位，可扩展）
const layerOptions = [] as Array<{ id: string; name: string }>
const selectedLayers = ref<string[]>([])
const panelActions = [
  { id: 'refresh', label: '刷新', icon: 'ep:refresh' }
]

// 楼层平面图与坐标参数
const floorPlanSvg = ref<string>('')
const backendCoordParams = ref({
  coordinateScale: 37.55, // 后端返回的像素/米比例（默认值）
  dxfOffsetX: 0,
  dxfOffsetY: 0,
  svgWidth: 1920,
  svgHeight: 1080
})

// 当前楼层巡更点（带百分比定位）
const patrolPointsOnFloor = ref<Array<PatrolPointVO & { xPercent: number; yPercent: number }>>([])

// 详情弹窗引用
const detailRef = ref()

/**
 * 加载园区列表
 * @returns {Promise<void>} 无返回值
 * @throws {Error} 当API调用失败时抛出错误
 */
const loadCampusList = async (): Promise<void> => {
  try {
    const data = await CampusApi.getCampusList()
    campuses.value = Array.isArray(data) ? data : []
    // 默认选中第一个园区
    currentCampusId.value = campuses.value[0]?.id
    if (currentCampusId.value) {
      await loadBuildingList(currentCampusId.value)
    }
  } catch (error) {
    console.error('[巡更点位] 加载园区失败:', error)
    ElMessage.error('加载园区失败')
  }
}

/**
 * 加载指定园区的建筑列表
 * @param {number} campusId - 园区ID
 * @returns {Promise<void>} 无返回值
 */
const loadBuildingList = async (campusId: number): Promise<void> => {
  try {
    const data = await BuildingApi.getBuildingListByCampusId(campusId)
    buildings.value = Array.isArray(data) ? data : []
    currentBuildingId.value = buildings.value[0]?.id
    if (currentBuildingId.value) {
      await loadFloorList(currentBuildingId.value)
    }
  } catch (error) {
    console.error('[巡更点位] 加载建筑失败:', error)
    ElMessage.error('加载建筑失败')
  }
}

/**
 * 加载指定建筑的楼层列表
 * @param {number} buildingId - 建筑ID
 * @returns {Promise<void>} 无返回值
 */
const loadFloorList = async (buildingId: number): Promise<void> => {
  try {
    const data = await FloorApi.getFloorListByBuildingId(buildingId)
    floors.value = Array.isArray(data) ? data : []
    currentFloorId.value = floors.value[0]?.id
    if (currentFloorId.value) {
      await handleFloorChange(currentFloorId.value)
    }
  } catch (error) {
    console.error('[巡更点位] 加载楼层失败:', error)
    ElMessage.error('加载楼层失败')
  }
}

/**
 * 处理园区变更
 * @param {number} id - 园区ID
 */
const handleCampusChange = async (id?: number | string): Promise<void> => {
  if (!id) return
  currentCampusId.value = Number(id)
  await loadBuildingList(currentCampusId.value)
}

/**
 * 处理建筑变更
 * @param {number} id - 建筑ID
 */
const handleBuildingChange = async (id?: number | string): Promise<void> => {
  if (!id) return
  currentBuildingId.value = Number(id)
  await loadFloorList(currentBuildingId.value)
}

/**
 * 楼层切换：加载DXF平面图与巡更点
 * @param {number} floorId - 楼层ID
 * @returns {Promise<void>} 无返回值
 */
const handleFloorChange = async (floorId?: number | string): Promise<void> => {
  if (!floorId) return
  currentFloorId.value = Number(floorId)
  await loadFloorPlan(currentFloorId.value)
  await loadPatrolPoints(currentFloorId.value)
}

/**
 * 楼层上移/下移（与RightMapPanel交互）
 */
const goUpFloor = (): void => {
  if (!floors.value.length || currentFloorId.value === undefined) return
  const idx = floors.value.findIndex((f: any) => f.id === currentFloorId.value)
  if (idx > 0) {
    handleFloorChange(floors.value[idx - 1].id)
  }
}

const goDownFloor = (): void => {
  if (!floors.value.length || currentFloorId.value === undefined) return
  const idx = floors.value.findIndex((f: any) => f.id === currentFloorId.value)
  if (idx >= 0 && idx < floors.value.length - 1) {
    handleFloorChange(floors.value[idx + 1].id)
  }
}

/**
 * 面板操作（刷新等）
 */
const handlePanelAction = async (id: string): Promise<void> => {
  if (id === 'refresh') {
    await refreshFloorData()
  }
}

/**
 * 加载楼层平面图（DXF→SVG）
 * @param {number} floorId - 楼层ID
 * @returns {Promise<void>} 无返回值
 * @throws {Error} 当DXF信息或文件获取失败时抛出错误
 */
const loadFloorPlan = async (floorId: number): Promise<void> => {
  // 调试日志：开始加载指定楼层的平面图
  console.log('[巡更点位] 开始加载楼层平面图', { floorId })
  try {
    // 1) 获取坐标比例
    const infoRes = await FloorDxfApi.getDxfInfo(floorId)
    console.debug('[巡更点位] dxfInfo:', infoRes)
    const coordinateScale = infoRes?.data?.coordinateScale || 37.55

    // 2) 获取图层列表并筛选可见图层
    const layersRes = await FloorDxfApi.getLayers(floorId)
    const layers = Array.isArray(layersRes?.data?.layers) ? layersRes.data.layers : []
    console.debug('[巡更点位] layers:', layers)

    // 更新右侧图层选项，便于观察与选择
    layerOptions.splice(0, layerOptions.length, ...layers.map((l: any) => ({ id: l.name, name: l.name })))

    let visibleLayerNames = layers
      .filter((layer: any) => layer.isVisible)
      .map((layer: any) => layer.name)

    // 当图层过滤后为空时，回退到全部图层进行转换，避免出现空SVG
    if (visibleLayerNames.length === 0) {
      console.warn('[巡更点位] 可见图层为空，回退到全部图层进行转换')
      visibleLayerNames = layers.map((l: any) => l.name)
    }
    // 同步到右侧面板的选中图层
    selectedLayers.value = visibleLayerNames

    // 3) 拉取DXF文本内容
    const dxfContent = await FloorDxfApi.getDxfFileContent(floorId)
    const contentLength = typeof dxfContent === 'string' ? dxfContent.length : 0
    console.debug('[巡更点位] dxfContent length:', contentLength)

    // 4) 转换为SVG（与实时预览工具一致）
    const result = convertDxfToSvgWithBackendScale(
      dxfContent,
      visibleLayerNames,
      coordinateScale,
      1920,
      1080
    )

    console.debug('[巡更点位] convert result:', {
      svgLength: result?.svg?.length || 0,
      coordinateScale: result?.coordinateScale,
      dxfOffsetX: result?.dxfOffsetX,
      dxfOffsetY: result?.dxfOffsetY
    })

    floorPlanSvg.value = result?.svg || ''
    backendCoordParams.value = {
      coordinateScale: result?.coordinateScale || coordinateScale,
      dxfOffsetX: result?.dxfOffsetX || 0,
      dxfOffsetY: result?.dxfOffsetY || 0,
      svgWidth: 1920,
      svgHeight: 1080
    }

    // 根据结果更新空状态提示，便于定位问题
    emptyDescription.value = floorPlanSvg.value
      ? '平面图已加载'
      : '无图形数据（可能图层不可见或DXF为空）'
  } catch (error: any) {
    // 捕获并打印完整异常，便于快速定位解析失败问题
    console.error('[巡更点位] 加载楼层平面图失败:', error)
    ElMessage.error('加载楼层平面图失败：DXF解析异常')
    floorPlanSvg.value = ''
    emptyDescription.value = 'DXF解析失败，请检查文件与后端服务'
  }
}

/**
 * 加载当前楼层的巡更点位，并转换为SVG百分比坐标
 * @param {number} floorId - 楼层ID
 * @returns {Promise<void>} 无返回值
 */
const loadPatrolPoints = async (floorId: number): Promise<void> => {
  try {
    // 每页最大值为 100，循环分页拉取直至拿全
    const pageSize = 100
    let pageNo = 1
    const all: PatrolPointVO[] = []

    while (true) {
      const params: PatrolPointPageReqVO = { pageNo, pageSize, floorId, status: 1 }
      const data = await getPatrolPointPage(params)
      const list: PatrolPointVO[] = Array.isArray(data?.list) ? data.list : []
      all.push(...list)
      if (list.length < pageSize) break
      pageNo += 1
    }

    patrolPointsOnFloor.value = all.map((p) => {
      const { xPercent, yPercent } = convertPointToSvgPercent(p)
      return { ...p, xPercent, yPercent }
    })
  } catch (error) {
    console.error('[巡更点位] 加载楼层点位失败:', error)
    ElMessage.error('加载巡更点位失败')
    patrolPointsOnFloor.value = []
  }
}

/**
 * 分页拉取当前楼层全部巡更点位（工具函数）
 * @param {number} floorId - 楼层ID
 * @returns {Promise<PatrolPointVO[]>} 巡更点位数组
 * @throws {Error} 当接口调用异常时可能抛出错误
 */
const fetchAllPatrolPointsByFloor = async (floorId: number): Promise<PatrolPointVO[]> => {
  const pageSize = 100
  let pageNo = 1
  const all: PatrolPointVO[] = []
  while (true) {
    const params: PatrolPointPageReqVO = { pageNo, pageSize, floorId, status: 1 }
    const data = await getPatrolPointPage(params)
    const list: PatrolPointVO[] = Array.isArray(data?.list) ? data.list : []
    all.push(...list)
    if (list.length < pageSize) break
    pageNo += 1
  }
  return all
}

/**
 * 巡更点坐标转换：将局部坐标（米）转换为SVG百分比坐标
 * 注意：此处将 point.longitude / point.latitude 视为局部X/Y（米）。
 * 如后端为经纬度，请在此处加入坐标映射逻辑。
 * @param {PatrolPointVO} point - 巡更点位数据
 * @returns {{ xPercent: number; yPercent: number }} 百分比坐标
 */
const convertPointToSvgPercent = (point: PatrolPointVO): { xPercent: number; yPercent: number } => {
  const { coordinateScale, dxfOffsetX, dxfOffsetY, svgWidth, svgHeight } = backendCoordParams.value
  // 将经/纬度字段作为局部坐标（米）处理
  const localX = Number(point.longitude) || 0
  const localY = Number(point.latitude) || 0

  // 局部米→像素
  const pixelX = localX * coordinateScale
  const pixelY = localY * coordinateScale

  // 像素坐标应用DXF→SVG偏移，并进行Y轴翻转（SVG向下为正）
  const svgX = pixelX + dxfOffsetX
  const svgY = svgHeight - (pixelY + dxfOffsetY)

  // 转换为容器百分比
  const xPercent = (svgX / svgWidth) * 100
  const yPercent = (svgY / svgHeight) * 100
  return { xPercent, yPercent }
}

/**
 * 点击点位查看详情
 * @param {any} point - 点位数据
 * @returns {void}
 */
const handlePointClick = (point: any): void => {
  detailRef.value?.open(point)
}

/**
 * 添加巡更点（占位，调用统一表单或跳转至管理页）
 * @returns {void}
 */
const addPoint = (): void => {
  ElMessage.info('请在巡更点位管理页中新增点位或在地图添加点位功能中实现')
}

/**
 * 编辑巡更点（占位）
 * @returns {void}
 */
const editPoint = (): void => {
  ElMessage.info('请选择右侧列表或地图上的点位进行编辑')
}

/**
 * 删除巡更点（占位）
 * @returns {void}
 */
const deletePoint = (): void => {
  ElMessage.info('请在巡更点位列表中执行删除操作')
}

/**
 * 刷新当前楼层的平面图与点位
 * @returns {Promise<void>} 无返回值
 */
const refreshFloorData = async (): Promise<void> => {
  if (!currentFloorId.value) return
  await handleFloorChange(currentFloorId.value)
}

// 初始化：加载级联选项
onMounted(async () => {
  await loadCampusList()
})
</script>

<style scoped>
.patrol-points-container {
  display: flex;
  height: calc(100vh - 84px);
  background-color: #f0f2f5;
}

.left-panel {
  width: 280px;
  padding: 16px;
  background-color: #fff;
  border-right: 1px solid #e5e7eb;
  overflow-y: auto;
}

.center-panel {
  flex: 1;
  padding: 16px;
  background-color: #fafafa;
}

.floor-map {
  position: relative;
  width: 100%;
  height: calc(100vh - 200px);
  background-color: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  overflow: hidden;
}

.svg-container {
  width: 100%;
  height: 100%;
}

.patrol-point {
  position: absolute;
  transform: translate(-50%, -50%);
  cursor: pointer;
}

.point-icon {
  color: #409eff;
  font-size: 20px;
}

.right-panel {
  width: 300px;
  padding: 16px;
  background-color: #fff;
  border-left: 1px solid #e5e7eb;
}

.operations {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.stat-item {
  margin: 10px 0;
  font-size: 14px;
}
</style>