<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="patrol-routes dark-theme-page">
      <!-- 页面标题 -->
      <div class="page-title">
        <h2>巡更线路</h2>
        <div class="view-controls">
          <el-button-group>
            <el-button :type="viewMode === 'list' ? 'primary' : 'default'" @click="viewMode = 'list'">
              <Icon icon="ep:grid" />
            </el-button>
            <el-button :type="viewMode === 'map' ? 'primary' : 'default'" @click="viewMode = 'map'">
              <Icon icon="ep:view" />
            </el-button>
          </el-button-group>
        </div>
      </div>

      <div class="main-container">
        <!-- 左侧线路列表 -->
        <div class="left-panel">
          <div class="panel-header">
            <h3>线路列表</h3>
            <el-button type="text" size="small" @click="handleCreateRoute">+ 新增</el-button>
          </div>
          <div class="search-box">
            <el-input 
              v-model="searchKeyword" 
              placeholder="请输入关键字"
              clearable
            />
          </div>
          <div class="routes-container">
            <div v-if="!hasSearchResults" class="no-results">
              <el-empty description="未找到匹配的线路" />
            </div>
            <template v-else>
              <div v-if="shouldShowCategory('main')" class="route-category">
                <div 
                  class="category-header" 
                  :class="{ expanded: categoryExpanded.main }"
                  @click="toggleCategory('main')"
                >
                  <Icon :icon="categoryExpanded.main ? 'ep:arrow-down' : 'ep:arrow-right'" />
                  <span>主干路</span>
                </div>
                <div v-show="categoryExpanded.main" class="category-content">
                  <div 
                    v-for="route in filteredRoutes.main" 
                    :key="route.id"
                    class="route-item"
                    :class="{ active: selectedRoute?.id === route.id }"
                    @click="selectRoute(route)"
                  >
                    <span class="route-name">{{ route.name }}</span>
                    <div class="route-meta">
                      <span class="point-count">{{ route.points.length }}个点位</span>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="shouldShowCategory('global')" class="route-category">
                <div 
                  class="category-header" 
                  :class="{ expanded: categoryExpanded.global }"
                  @click="toggleCategory('global')"
                >
                  <Icon :icon="categoryExpanded.global ? 'ep:arrow-down' : 'ep:arrow-right'" />
                  <span>全局路</span>
                </div>
                <div v-show="categoryExpanded.global" class="category-content">
                  <div 
                    v-for="route in filteredRoutes.global" 
                    :key="route.id"
                    class="route-item"
                    :class="{ active: selectedRoute?.id === route.id }"
                    @click="selectRoute(route)"
                  >
                    <span class="route-name">{{ route.name }}</span>
                    <div class="route-meta">
                      <span class="point-count">{{ route.points.length }}个点位</span>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="shouldShowCategory('new')" class="route-category">
                <div 
                  class="category-header" 
                  :class="{ expanded: categoryExpanded.new }"
                  @click="toggleCategory('new')"
                >
                  <Icon :icon="categoryExpanded.new ? 'ep:arrow-down' : 'ep:arrow-right'" />
                  <span>新的线路</span>
                </div>
                <div v-show="categoryExpanded.new" class="category-content">
                  <div 
                    v-for="route in filteredRoutes.new" 
                    :key="route.id"
                    class="route-item"
                    :class="{ active: selectedRoute?.id === route.id }"
                    @click="selectRoute(route)"
                  >
                    <span class="route-name">{{ route.name }}</span>
                    <div class="route-meta">
                      <span class="point-count">{{ route.points.length }}个点位</span>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="shouldShowCategory('ordered')" class="route-category">
                <div 
                  class="category-header" 
                  :class="{ expanded: categoryExpanded.ordered }"
                  @click="toggleCategory('ordered')"
                >
                  <Icon :icon="categoryExpanded.ordered ? 'ep:arrow-down' : 'ep:arrow-right'" />
                  <span>有序巡更</span>
                </div>
                <div v-show="categoryExpanded.ordered" class="category-content">
                  <div 
                    v-for="route in filteredRoutes.ordered" 
                    :key="route.id"
                    class="route-item"
                    :class="{ active: selectedRoute?.id === route.id }"
                    @click="selectRoute(route)"
                  >
                    <span class="route-name">{{ route.name }}</span>
                    <div class="route-meta">
                      <span class="point-count">{{ route.points.length }}个点位</span>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="shouldShowCategory('unordered')" class="route-category">
                <div 
                  class="category-header" 
                  :class="{ expanded: categoryExpanded.unordered }"
                  @click="toggleCategory('unordered')"
                >
                  <Icon :icon="categoryExpanded.unordered ? 'ep:arrow-down' : 'ep:arrow-right'" />
                  <span>无序巡更</span>
                </div>
                <div v-show="categoryExpanded.unordered" class="category-content">
                  <div 
                    v-for="route in filteredRoutes.unordered" 
                    :key="route.id"
                    class="route-item"
                    :class="{ active: selectedRoute?.id === route.id }"
                    @click="selectRoute(route)"
                  >
                    <span class="route-name">{{ route.name }}</span>
                    <div class="route-meta">
                      <span class="point-count">{{ route.points.length }}个点位</span>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </div>

        <!-- 右侧内容区 -->
        <div class="right-content">
          <div v-if="selectedRoute" class="route-details">
            <!-- 搜索筛选区 -->
            <div class="search-section">
              <el-form inline>
                <el-form-item>
                  <el-input 
                    v-model="pointSearchKeyword" 
                    placeholder="请输入巡更点名称"
                    style="width: 200px;"
                    clearable
                  />
                </el-form-item>
              </el-form>
            </div>

            <!-- 列表视图 -->
            <div v-if="viewMode === 'list'" class="points-section">
              <div class="section-title">投入机</div>
              
              <!-- 投入机表格 -->
              <div class="route-table">
                <el-table :data="routePointsTable" stripe style="width: 100%">
                  <el-table-column prop="sequence" label="序号" width="80" />
                  <el-table-column prop="pointCode" label="巡更点编号" width="180" />
                  <el-table-column prop="pointName" label="巡更点名称" width="150" />
                  <el-table-column prop="pointType" label="巡更点类型" width="150" />
                  <el-table-column prop="location" label="所在位置" width="180" />
                  <el-table-column prop="organCode" label="空间编码" width="120" />
                  <el-table-column prop="timeLimit" label="时间限制(分钟)" width="140" />
                  <el-table-column prop="enableTime" label="现实时间(分钟)" width="140" />
                </el-table>
              </div>

              <!-- 底部分页 -->
              <div class="pagination-container">
                <el-pagination
                  v-model:current-page="pagination.page"
                  v-model:page-size="pagination.size"
                  :total="pagination.total"
                  :page-sizes="[10, 20, 50]"
                  layout="total, sizes, prev, pager, next, jumper"
                  @size-change="handleSizeChange"
                  @current-change="handleCurrentChange"
                />
              </div>
            </div>

            <!-- 地图视图 -->
            <div v-else class="map-section">
              <div class="section-title">巡更路线地图</div>
              
              <div class="map-container">
                <div class="map-placeholder">
                  <Icon icon="ep:location" class="map-icon" />
                  <h3>地图视图</h3>
                  <p>当前线路：{{ selectedRoute.name }}</p>
                  <p>包含 {{ routePointsTable.length }} 个巡更点位</p>
                  <div class="map-points">
                    <div 
                      v-for="point in routePointsTable" 
                      :key="point.sequence"
                      class="map-point"
                    >
                      <div class="point-marker">{{ point.sequence }}</div>
                      <div class="point-info">
                        <div class="point-name">{{ point.pointName }}</div>
                        <div class="point-location">{{ point.location || '未设置位置' }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 未选择线路时的空状态 -->
          <div v-else class="empty-state">
            <el-empty description="请选择左侧线路查看详情" />
          </div>
        </div>
      </div>

      <!-- 新增线路弹窗 -->
      <el-dialog
        v-model="createDialogVisible"
        title="新增"
        width="800px"
        :before-close="handleCloseCreate"
      >
        <div class="create-form">
          <!-- 线路类型选择 -->
          <div class="route-type-section">
            <div class="form-label">线路类型：</div>
            <el-radio-group v-model="createForm.routeType">
              <el-radio value="main">主干路</el-radio>
              <el-radio value="global">全局路</el-radio>
            </el-radio-group>
          </div>

          <!-- 线路名称 -->
          <div class="route-name-section">
            <div class="form-label">线路名称：</div>
            <el-input 
              v-model="createForm.routeName" 
              placeholder="请输入线路名称"
              style="width: 300px;"
            />
          </div>

          <!-- 巡更点位选择 -->
          <div class="route-points-section">
            <div class="section-title">巡更点位</div>
            
            <div class="points-selection">
              <!-- 左侧：可选点位 -->
              <div class="selection-panel">
                <div class="panel-header">
                  <span class="panel-title">请选择主机</span>
                  <el-button type="text" size="small">刷新全部</el-button>
                </div>
                <div class="search-input">
                  <el-input 
                    placeholder="请输入要更点名称"
                    v-model="createForm.availableSearch"
                    clearable
                  />
                </div>
                <div class="points-list">
                  <el-table 
                    :data="createForm.availablePoints" 
                    @selection-change="handleCreateAvailableSelectionChange"
                    height="250"
                  >
                    <el-table-column prop="order" label="序号" width="60" />
                    <el-table-column prop="name" label="巡更点名称" />
                  </el-table>
                </div>
              </div>

              <!-- 右侧：已选点位 -->
              <div class="selection-panel">
                <div class="panel-header">
                  <span class="panel-title">已选({{ createForm.selectedPoints.length }})</span>
                  <el-button type="text" size="small">清除全部</el-button>
                </div>
                <div class="search-input">
                  <el-input 
                    placeholder="请输入要更点名称"
                    v-model="createForm.selectedSearch"
                    clearable
                  />
                </div>
                <div class="points-list">
                  <div v-if="!createForm.selectedPoints.length" class="empty-state">
                    <div class="empty-content">
                      <Icon icon="ep:user" class="empty-icon" />
                      <div class="empty-text">暂无数据</div>
                    </div>
                  </div>
                  <el-table 
                    v-else
                    :data="createForm.selectedPoints" 
                    height="250"
                  >
                    <el-table-column prop="name" label="巡更点名称" />
                    <el-table-column label="操作" width="80">
                      <template #default="{ row }">
                        <el-button size="small" type="danger" @click="removeCreatePoint(row)">
                          移除
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </div>
            </div>

            <!-- 底部操作按钮 -->
            <div class="points-actions">
              <el-button type="primary" @click="handleAddPoints">取消</el-button>
              <el-button @click="handleConfirmPoints">确认</el-button>
            </div>
          </div>
        </div>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="handleCloseCreate">取消</el-button>
            <el-button type="primary" @click="handleSaveCreate">确认</el-button>
    </div>
        </template>
      </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ContentWrap } from '@/components/ContentWrap'
import { Icon } from '@/components/Icon'

/** 巡更线路 */
defineOptions({ name: 'PatrolRoutes' })

// 类型定义
interface PatrolPoint {
  id: number
  order: number
  name: string
  location?: string
}

interface PatrolRoute {
  id: number
  name: string
  category: string
  points: PatrolPoint[]
  description?: string
}

// 响应式数据
const viewMode = ref('list')
const searchKeyword = ref('')
const pointSearchKeyword = ref('')
const selectedRoute = ref<PatrolRoute | null>(null)
const createDialogVisible = ref(false)

// 分类展开状态
const categoryExpanded = reactive({
  main: true,
  global: true,
  new: true,
  ordered: true,
  unordered: true
})

// 选择状态
const selectedPoints = ref<PatrolPoint[]>([])

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 5
})

// 新增表单
const createForm = reactive({
  routeType: 'main',
  routeName: '',
  availableSearch: '',
  selectedSearch: '',
  availablePoints: [] as PatrolPoint[],
  selectedPoints: [] as PatrolPoint[],
  availableSelection: [] as PatrolPoint[]
})

// 投入机表格数据
const routePointsTable = ref([
  {
    sequence: 1,
    pointCode: '20XGP20240130003',
    pointName: '门点01',
    pointType: '设备巡更',
    location: '前台人员巡更厂门点',
    organCode: '南区_科技研发楼_9F_办...',
    timeLimit: 0,
    enableTime: 0
  },
  {
    sequence: 2,
    pointCode: '20XGP20240130002',
    pointName: '枪型03',
    pointType: '设备巡更',
    location: '办公区主要人员巡更地标03',
    organCode: '南区_科技研发楼_9F_办...',
    timeLimit: 0,
    enableTime: 0
  },
  {
    sequence: 3,
    pointCode: '20XGP20240130001',
    pointName: '球型监控机',
    pointType: '设备巡更',
    location: '办公区人脸识别球机01',
    organCode: '南区_科技研发楼_9F_办...',
    timeLimit: 0,
    enableTime: 0
  },
  {
    sequence: 4,
    pointCode: '20XGP20240108003',
    pointName: '巡更点3',
    pointType: '自定义',
    location: '-',
    organCode: '南区_科技研发楼_2F_203',
    timeLimit: 0,
    enableTime: 0
  },
  {
    sequence: 5,
    pointCode: '20XGP20240108002',
    pointName: '巡更点2',
    pointType: '自定义',
    location: '-',
    organCode: '南区_科技研发楼_2F_201',
    timeLimit: 0,
    enableTime: 0
  }
])

// 巡更线路数据
const routes = reactive({
  main: [
    { id: 1, name: '投入机线路', category: 'main', points: [], description: '主要投入机巡更线路' }
  ] as PatrolRoute[],
  global: [
    { id: 2, name: '全局巡更线路01', category: 'global', points: [], description: '全局覆盖巡更线路' },
    { id: 3, name: '全局巡更线路03', category: 'global', points: [], description: '全局覆盖巡更线路' }
  ] as PatrolRoute[],
  new: [
    { id: 4, name: '球型监控机线路', category: 'new', points: [], description: '球型监控设备巡更' },
    { id: 5, name: '巡更点4号线路', category: 'new', points: [], description: '4号区域巡更线路' },
    { id: 6, name: '巡更点3号线路', category: 'new', points: [], description: '3号区域巡更线路' },
    { id: 7, name: '巡更点2号线路', category: 'new', points: [], description: '2号区域巡更线路' }
  ] as PatrolRoute[],
  ordered: [] as PatrolRoute[],
  unordered: [] as PatrolRoute[]
})

// 可用巡更点位数据
const availablePoints = ref<PatrolPoint[]>([
  { id: 1, order: 1, name: '777' },
  { id: 2, order: 2, name: '门点01' },
  { id: 3, order: 3, name: '枪型03' },
  { id: 4, order: 4, name: '球型监控机' },
  { id: 5, order: 5, name: '巡更点4' },
  { id: 6, order: 6, name: '巡更点3' },
  { id: 7, order: 7, name: '巡更点2' }
])

// 计算属性

// 过滤线路的计算属性
const filteredRoutes = computed(() => {
  if (!searchKeyword.value) return routes
  
  const keyword = searchKeyword.value.toLowerCase()
  const filtered = {
    main: routes.main.filter(route => route.name.toLowerCase().includes(keyword)),
    global: routes.global.filter(route => route.name.toLowerCase().includes(keyword)),
    new: routes.new.filter(route => route.name.toLowerCase().includes(keyword)),
    ordered: routes.ordered.filter(route => route.name.toLowerCase().includes(keyword)),
    unordered: routes.unordered.filter(route => route.name.toLowerCase().includes(keyword))
  }
  
  return filtered
})

// 检查是否有搜索结果
const hasSearchResults = computed(() => {
  if (!searchKeyword.value) return true
  
  const filtered = filteredRoutes.value
  return filtered.main.length > 0 || 
         filtered.global.length > 0 || 
         filtered.new.length > 0 || 
         filtered.ordered.length > 0 || 
         filtered.unordered.length > 0
})

// 检查分类是否应该显示
const shouldShowCategory = (categoryName: string) => {
  if (!searchKeyword.value) return true
  return filteredRoutes.value[categoryName].length > 0
}

// 方法
const toggleCategory = (category: string) => {
  categoryExpanded[category] = !categoryExpanded[category]
}

const selectRoute = (route: PatrolRoute) => {
  selectedRoute.value = route
  selectedPoints.value = [...route.points]
}

const handleCreateRoute = () => {
  createForm.routeType = 'main'
  createForm.routeName = ''
  createForm.availableSearch = ''
  createForm.selectedSearch = ''
  createForm.availablePoints = [...availablePoints.value]
  createForm.selectedPoints = []
  createForm.availableSelection = []
  createDialogVisible.value = true
}

const handleCloseCreate = () => {
  createDialogVisible.value = false
}

const handleSaveCreate = () => {
  if (!createForm.routeName.trim()) {
    ElMessage.warning('请输入线路名称')
    return
  }

  const newRoute: PatrolRoute = {
    id: Date.now(),
    name: createForm.routeName,
    category: createForm.routeType,
    points: [...createForm.selectedPoints]
  }

  routes[createForm.routeType].push(newRoute)
  ElMessage.success('线路创建成功')
  handleCloseCreate()
}

const handleCreateAvailableSelectionChange = (selection: PatrolPoint[]) => {
  createForm.availableSelection = selection
}

const removeCreatePoint = (point: PatrolPoint) => {
  const index = createForm.selectedPoints.findIndex(p => p.id === point.id)
  if (index > -1) {
    createForm.selectedPoints.splice(index, 1)
    ElMessage.success('已移除点位')
  }
}

const handleSizeChange = (size: number) => {
  pagination.size = size
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
}

const handleAddPoints = () => {
  ElMessage.info('取消操作')
}

const handleConfirmPoints = () => {
  ElMessage.success('确认操作')
}

// 监听视图模式变化，添加提示
watch(viewMode, (newMode) => {
  if (newMode === 'list') {
    ElMessage.info('已切换到列表视图')
  } else {
    ElMessage.info('已切换到地图视图')
  }
})

// 监听搜索关键字变化
watch(searchKeyword, () => {
  // 如果当前选中的线路不在搜索结果中，清除选择
  if (selectedRoute.value && searchKeyword.value) {
    const allFilteredRoutes = [
      ...filteredRoutes.value.main,
      ...filteredRoutes.value.global,
      ...filteredRoutes.value.new,
      ...filteredRoutes.value.ordered,
      ...filteredRoutes.value.unordered
    ]
    
    const isCurrentRouteInResults = allFilteredRoutes.some(route => route.id === selectedRoute.value?.id)
    if (!isCurrentRouteInResults) {
      selectedRoute.value = null
      selectedPoints.value = []
    }
  }
})

onMounted(() => {
  // 默认选择第一个线路
  if (routes.main.length > 0) {
    selectRoute(routes.main[0])
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/dark-theme.scss';

.patrol-routes {
  padding: 20px;
  background: #0d1117;
  min-height: calc(100vh - 140px);
  color: #ffffff;

  // 页面标题
  .page-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
      color: #ffffff;
      font-size: 24px;
      font-weight: 600;
    }

    .view-controls {
      .el-button-group {
        .el-button {
          border-color: rgba(255, 255, 255, 0.2);
          background: transparent;
          color: #ffffff;

          &:hover {
            background: rgba(255, 255, 255, 0.1);
            border-color: #1890ff;
          }

          &.el-button--primary {
            background: #1890ff;
            border-color: #1890ff;
          }
        }
      }
    }
  }

  // 主容器
  .main-container {
    display: flex;
    gap: 20px;
    height: calc(100vh - 200px);

    // 左侧面板
    .left-panel {
      width: 320px; // 增加宽度
      background: rgba(255, 255, 255, 0.05);
      border-radius: 8px;
      padding: 20px; // 增加内边距
      backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.1);

      .panel-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px; // 增加间距
        padding-bottom: 12px;
        border-bottom: 1px solid rgba(255, 255, 255, 0.1);

        h3 {
          margin: 0;
          color: #ffffff;
          font-size: 18px; // 增大字体
          font-weight: 600;
        }

        .el-button {
          color: #1890ff;
          background: rgba(24, 144, 255, 0.1);
          border: 1px solid rgba(24, 144, 255, 0.3);
          border-radius: 4px;
          padding: 4px 8px;
          font-size: 12px;
          transition: all 0.3s ease;

          &:hover {
            color: #40a9ff;
            background: rgba(24, 144, 255, 0.2);
            border-color: #40a9ff;
          }
        }
      }

      .search-box {
        margin-bottom: 20px; // 增加间距
      }

      .routes-container {
        height: calc(100% - 100px); // 调整高度计算
        overflow-y: auto;
        padding-right: 8px; // 为滚动条留出空间

        // 自定义滚动条样式
        &::-webkit-scrollbar {
          width: 6px;
        }

        &::-webkit-scrollbar-track {
          background: rgba(255, 255, 255, 0.1);
          border-radius: 3px;
        }

        &::-webkit-scrollbar-thumb {
          background: rgba(24, 144, 255, 0.5);
          border-radius: 3px;
          
          &:hover {
            background: rgba(24, 144, 255, 0.7);
          }
        }

        .no-results {
          display: flex;
          align-items: center;
          justify-content: center;
          height: 200px;
          color: rgba(255, 255, 255, 0.6);
        }

        .route-category {
          margin-bottom: 16px; // 增加间距

          .category-header {
            display: flex;
            align-items: center;
            gap: 10px; // 增加间距
            padding: 12px 16px; // 增加内边距
            background: rgba(255, 255, 255, 0.08);
            border-radius: 6px;
            cursor: pointer;
            transition: all 0.3s ease;
            border: 1px solid rgba(255, 255, 255, 0.1);

            &:hover {
              background: rgba(255, 255, 255, 0.12);
              border-color: rgba(24, 144, 255, 0.3);
              transform: translateY(-1px);
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
            }

            .iconify {
              color: #1890ff;
              font-size: 16px; // 增大图标
              transition: transform 0.3s ease;
            }

            span {
              color: #ffffff;
              font-weight: 600;
              font-size: 14px;
              flex: 1; // 让文本占据剩余空间
            }

            // 展开时的图标动画
            &.expanded .iconify {
              transform: rotate(90deg);
            }
          }

          .category-content {
            margin-top: 8px;
            margin-left: 26px; // 与图标对齐
            border-left: 2px solid rgba(24, 144, 255, 0.3);
            padding-left: 12px;

            .route-item {
              padding: 10px 16px; // 增加内边距
              margin: 4px 0; // 增加间距
              border-radius: 6px;
              cursor: pointer;
              transition: all 0.3s ease;
              position: relative;
              border: 1px solid transparent;

              &:hover {
                background: rgba(255, 255, 255, 0.08);
                border-color: rgba(255, 255, 255, 0.2);
                transform: translateX(4px);
              }

              &.active {
                background: linear-gradient(135deg, #1890ff, #40a9ff);
                color: #ffffff;
                border-color: #1890ff;
                box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);

                &::before {
                  content: '';
                  position: absolute;
                  left: -14px;
                  top: 50%;
                  transform: translateY(-50%);
                  width: 4px;
                  height: 20px;
                  background: #ffffff;
                  border-radius: 2px;
                }
              }

              .route-name {
                color: rgba(255, 255, 255, 0.9);
                font-size: 14px;
                font-weight: 500;
                word-break: break-all; // 允许换行
                line-height: 1.4;
                display: block;
                width: 100%;
                margin-bottom: 4px;
              }

              .route-meta {
                display: flex;
                align-items: center;
                gap: 8px;
                margin-top: 4px;

                .point-count {
                  font-size: 11px;
                  color: rgba(255, 255, 255, 0.6);
                  background: rgba(255, 255, 255, 0.1);
                  padding: 2px 6px;
                  border-radius: 10px;
                  border: 1px solid rgba(255, 255, 255, 0.2);
                }
              }

              &.active {
                .route-name {
                  color: #ffffff;
                  font-weight: 600;
                }

                .route-meta .point-count {
                  background: rgba(255, 255, 255, 0.2);
                  border-color: rgba(255, 255, 255, 0.3);
                  color: rgba(255, 255, 255, 0.9);
                }
              }
            }
          }
        }
      }
    }

    // 右侧内容区
    .right-content {
      flex: 1;
      display: flex;
      flex-direction: column;

      .route-details {
        .search-section {
          background: rgba(255, 255, 255, 0.05);
          border-radius: 8px;
          padding: 16px;
          margin-bottom: 16px;
          backdrop-filter: blur(10px);
        }

        .points-section {
          background: rgba(255, 255, 255, 0.05);
          border-radius: 8px;
          padding: 16px;
          flex: 1;
          backdrop-filter: blur(10px);

          .section-title {
            font-size: 18px;
            font-weight: 600;
            margin-bottom: 16px;
            color: #ffffff;
          }

          .points-container {
            display: flex;
            gap: 20px;
            margin-bottom: 16px;

            .points-panel {
              flex: 1;
              background: rgba(255, 255, 255, 0.05);
              border-radius: 8px;
              padding: 16px;

              .panel-title {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 12px;

                span {
                  color: #1890ff;
                  font-weight: 600;
                }

                .el-button {
                  color: #1890ff;
                  background: transparent;
                  border: none;
                  padding: 0;

                  &:hover {
                    color: #40a9ff;
                  }
                }
              }

              .search-input {
                margin-bottom: 12px;
              }

              .points-list {
                .empty-state {
                  text-align: center;
                  padding: 40px;
                  color: rgba(255, 255, 255, 0.6);
                }
              }
            }
          }

          .operation-buttons {
            text-align: center;
            margin-bottom: 16px;

            .el-button {
              margin: 0 8px;
            }
          }

          .pagination-container {
            text-align: center;
          }
        }
      }

      // 地图视图样式
      .map-section {
        background: rgba(255, 255, 255, 0.05);
        border-radius: 8px;
        padding: 16px;
        flex: 1;
        backdrop-filter: blur(10px);

        .section-title {
          font-size: 18px;
          font-weight: 600;
          margin-bottom: 16px;
          color: #ffffff;
        }

        .map-container {
          height: 500px;
          background: rgba(255, 255, 255, 0.03);
          border-radius: 8px;
          border: 1px solid rgba(255, 255, 255, 0.1);

          .map-placeholder {
            height: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            text-align: center;

            .map-icon {
              font-size: 64px;
              color: #1890ff;
              margin-bottom: 16px;
            }

            h3 {
              margin: 0 0 12px 0;
              color: #ffffff;
              font-size: 20px;
            }

            p {
              margin: 4px 0;
              color: rgba(255, 255, 255, 0.8);
              font-size: 14px;
            }

            .map-points {
              margin-top: 20px;
              display: grid;
              grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
              gap: 12px;
              max-width: 600px;

              .map-point {
                display: flex;
                align-items: center;
                gap: 12px;
                padding: 8px 12px;
                background: rgba(255, 255, 255, 0.1);
                border-radius: 6px;
                border: 1px solid rgba(255, 255, 255, 0.2);

                .point-marker {
                  width: 24px;
                  height: 24px;
                  background: #1890ff;
                  color: #ffffff;
                  border-radius: 50%;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  font-size: 12px;
                  font-weight: 600;
                  flex-shrink: 0;
                }

                .point-info {
                  flex: 1;
                  text-align: left;

                  .point-name {
                    color: #ffffff;
                    font-size: 13px;
                    font-weight: 500;
                    margin-bottom: 2px;
                  }

                  .point-location {
                    color: rgba(255, 255, 255, 0.6);
                    font-size: 11px;
                  }
                }
              }
            }
          }
        }
      }

      .empty-state {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        background: rgba(255, 255, 255, 0.05);
        border-radius: 8px;
        backdrop-filter: blur(10px);
      }
    }
  }

  // 新增弹窗
  .create-form {
    .route-type-section,
    .route-name-section {
      display: flex;
      align-items: center;
      margin-bottom: 20px;

      .form-label {
        width: 100px;
        color: rgba(255, 255, 255, 0.8);
        font-weight: 500;
      }
    }

    .route-points-section {
      .section-title {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 16px;
        color: #ffffff;
      }

      .points-selection {
        display: flex;
        gap: 20px;

        .selection-panel {
          flex: 1;
          background: rgba(255, 255, 255, 0.05);
          border-radius: 8px;
          padding: 16px;

          .panel-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;

            .panel-title {
              color: #1890ff;
              font-weight: 600;
            }

            .el-button {
              color: #1890ff;
              background: transparent;
              border: none;
              padding: 0;

              &:hover {
                color: #40a9ff;
              }
            }
          }

          .search-input {
            margin-bottom: 12px;
          }

          .points-list {
            .empty-state {
              height: 250px;
    display: flex;
    align-items: center;
    justify-content: center;

              .empty-content {
                text-align: center;
                color: rgba(255, 255, 255, 0.6);

                .empty-icon {
                  font-size: 48px;
                  margin-bottom: 12px;
                  opacity: 0.3;
                }

                .empty-text {
                  font-size: 14px;
                }
              }
            }
          }
        }
      }

      .points-actions {
        margin-top: 20px;
        text-align: right;

        .el-button {
          margin-left: 10px;
        }
      }
    }
  }
}

// Element Plus 组件深色主题覆盖
:deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  box-shadow: none !important;

  .el-input__inner {
    color: #ffffff !important;

    &::placeholder {
      color: rgba(255, 255, 255, 0.5) !important;
    }
  }

  &:hover {
    border-color: #1890ff !important;
  }

  &.is-focus {
    border-color: #1890ff !important;
  }
}

:deep(.el-table) {
  background-color: rgba(255, 255, 255, 0.05) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  border-radius: 8px !important;

  .el-table__header {
    .el-table__cell {
      background-color: rgba(255, 255, 255, 0.1) !important;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
      color: #ffffff !important;
    }
  }

  .el-table__row {
    background-color: transparent !important;

    &:hover {
      background-color: rgba(255, 255, 255, 0.05) !important;
    }

    .el-table__cell {
      border-bottom: 1px solid rgba(255, 255, 255, 0.05) !important;
      color: #ffffff !important;
    }

    &.el-table__row--striped {
      background-color: rgba(255, 255, 255, 0.02) !important;

      &:hover {
        background-color: rgba(255, 255, 255, 0.05) !important;
      }
    }
  }

  .el-checkbox {
    .el-checkbox__input {
      .el-checkbox__inner {
        background-color: rgba(255, 255, 255, 0.1) !important;
        border-color: rgba(255, 255, 255, 0.3) !important;

        &:hover {
          border-color: #1890ff !important;
        }
      }

      &.is-checked .el-checkbox__inner {
        background-color: #1890ff !important;
        border-color: #1890ff !important;
      }
    }
  }
}

:deep(.el-pagination) {
  .el-pagination__total,
  .el-pagination__jump {
    color: rgba(255, 255, 255, 0.8) !important;
  }

  .btn-prev,
  .btn-next,
  .el-pager li {
    background-color: rgba(255, 255, 255, 0.1) !important;
    border: 1px solid rgba(255, 255, 255, 0.2) !important;
    color: #ffffff !important;

    &:hover {
      background-color: #1890ff !important;
      border-color: #1890ff !important;
    }

    &.is-active {
      background-color: #1890ff !important;
      border-color: #1890ff !important;
    }
  }

  .el-select {
    .el-input__wrapper {
      background-color: rgba(255, 255, 255, 0.1) !important;
      border: 1px solid rgba(255, 255, 255, 0.2) !important;
    }
  }
}

:deep(.el-dialog) {
  background-color: #1a1a1a !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;

  .el-dialog__header {
    border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;

    .el-dialog__title {
      color: #ffffff !important;
    }

    .el-dialog__headerbtn {
      .el-dialog__close {
        color: rgba(255, 255, 255, 0.6) !important;

        &:hover {
          color: #ffffff !important;
        }
      }
    }
  }

  .el-dialog__body {
    color: #ffffff !important;
  }

  .el-dialog__footer {
    border-top: 1px solid rgba(255, 255, 255, 0.1) !important;
  }
}

:deep(.el-radio) {
  .el-radio__input {
    .el-radio__inner {
      background-color: rgba(255, 255, 255, 0.1) !important;
      border-color: rgba(255, 255, 255, 0.3) !important;

      &:hover {
        border-color: #1890ff !important;
      }
    }

    &.is-checked .el-radio__inner {
      background-color: #1890ff !important;
      border-color: #1890ff !important;

      &::after {
        background-color: #ffffff !important;
      }
    }
  }

  .el-radio__label {
    color: #ffffff !important;
  }
}

:deep(.el-button) {
  &.el-button--primary {
    background-color: #1890ff !important;
    border-color: #1890ff !important;

    &:hover {
      background-color: #40a9ff !important;
      border-color: #40a9ff !important;
    }
  }

  &.el-button--default {
    background-color: rgba(255, 255, 255, 0.1) !important;
    border-color: rgba(255, 255, 255, 0.2) !important;
    color: #ffffff !important;

    &:hover {
      background-color: rgba(255, 255, 255, 0.2) !important;
      border-color: #1890ff !important;
    }
  }

  &.el-button--danger {
    background-color: #f56565 !important;
    border-color: #f56565 !important;

    &:hover {
      background-color: #fc8181 !important;
      border-color: #fc8181 !important;
    }
  }

  &.el-button--text {
    color: #1890ff !important;

    &:hover {
      color: #40a9ff !important;
    }
  }
}

:deep(.el-empty) {
  .el-empty__description {
    color: rgba(255, 255, 255, 0.6) !important;
  }
}
</style>


