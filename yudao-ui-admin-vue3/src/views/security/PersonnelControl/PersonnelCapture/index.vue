<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <h2>人员抓拍</h2>
        <p>实时监控和抓拍人员图像信息</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出记录
        </el-button>
        <el-button type="success" @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :model="searchForm" label-width="80px" :inline="true">
        <el-form-item label="摄像头">
          <el-select v-model="searchForm.cameraId" placeholder="请选择摄像头" clearable>
            <el-option
              v-for="camera in cameraOptions"
              :key="camera.value"
              :label="camera.label"
              :value="camera.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="抓拍时间">
          <el-date-picker
            v-model="searchForm.captureTime"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="识别状态">
          <el-select v-model="searchForm.recognitionStatus" placeholder="请选择" clearable>
            <el-option label="已识别" value="recognized" />
            <el-option label="未识别" value="unrecognized" />
            <el-option label="陌生人" value="stranger" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon today">
                <el-icon><Camera /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.todayCapture }}</div>
                <div class="stat-label">今日抓拍</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon recognized">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.recognizedCount }}</div>
                <div class="stat-label">已识别人员</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon stranger">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.strangerCount }}</div>
                <div class="stat-label">陌生人</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon total">
                <el-icon><PieChart /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.totalCapture }}</div>
                <div class="stat-label">总抓拍数</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 抓拍记录表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="captureList"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="captureImage" label="抓拍图片" width="120">
          <template #default="{ row }">
            <el-image
              :src="row.captureImage"
              :preview-src-list="[row.captureImage]"
              fit="cover"
              style="width: 80px; height: 60px; border-radius: 4px;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="cameraName" label="摄像头" width="140" show-overflow-tooltip />
        <el-table-column prop="captureTime" label="抓拍时间" width="160" show-overflow-tooltip />
        <el-table-column prop="recognitionStatus" label="识别状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.recognitionStatus)">
              {{ getStatusText(row.recognitionStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="personName" label="识别人员" width="120" show-overflow-tooltip />
        <el-table-column prop="confidence" label="相似度" width="100">
          <template #default="{ row }">
            <span v-if="row.confidence">{{ row.confidence }}%</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="gender" label="性别" width="80" />
        <el-table-column prop="location" label="位置" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button link type="success" @click="handleManualMatch(row)" :disabled="row.recognitionStatus === 'recognized'">
              <el-icon><EditPen /></el-icon>
              手动匹配
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <!-- 详情弹框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="抓拍详情"
      width="800px"
      destroy-on-close
    >
      <div v-if="currentDetail" class="detail-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-image">
              <el-image
                :src="currentDetail.captureImage"
                fit="contain"
                style="width: 100%; max-height: 400px;"
              />
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-info">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="抓拍时间">
                  {{ currentDetail.captureTime }}
                </el-descriptions-item>
                <el-descriptions-item label="摄像头">
                  {{ currentDetail.cameraName }}
                </el-descriptions-item>
                <el-descriptions-item label="位置">
                  {{ currentDetail.location }}
                </el-descriptions-item>
                <el-descriptions-item label="识别状态">
                  <el-tag :type="getStatusColor(currentDetail.recognitionStatus)">
                    {{ getStatusText(currentDetail.recognitionStatus) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="识别人员" v-if="currentDetail.personName">
                  {{ currentDetail.personName }}
                </el-descriptions-item>
                <el-descriptions-item label="相似度" v-if="currentDetail.confidence">
                  {{ currentDetail.confidence }}%
                </el-descriptions-item>
                <el-descriptions-item label="性别">
                  {{ currentDetail.gender }}
                </el-descriptions-item>
                <el-descriptions-item label="年龄">
                  {{ currentDetail.age }}
                </el-descriptions-item>
                <el-descriptions-item label="备注" v-if="currentDetail.remark">
                  {{ currentDetail.remark }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-dialog>

    <!-- 手动匹配弹框 -->
    <el-dialog
      v-model="matchDialogVisible"
      title="手动匹配人员"
      width="600px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div class="match-content">
        <div class="capture-preview">
          <h4>抓拍图片</h4>
          <el-image
            v-if="currentCapture"
            :src="currentCapture.captureImage"
            fit="contain"
            style="width: 200px; height: 150px; border: 1px solid #ddd; border-radius: 4px;"
          />
        </div>
        
        <el-form :model="matchForm" label-width="100px" style="margin-top: 20px;">
          <el-form-item label="选择人员库">
            <el-select v-model="matchForm.libraryId" placeholder="请选择人员库" @change="loadPersonOptions">
              <el-option
                v-for="library in libraryOptions"
                :key="library.value"
                :label="library.label"
                :value="library.value"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="选择人员">
            <el-select v-model="matchForm.personId" placeholder="请选择人员" filterable>
              <el-option
                v-for="person in personOptions"
                :key="person.value"
                :label="person.label"
                :value="person.value"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="备注">
            <el-input
              v-model="matchForm.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入备注信息"
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="matchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmMatch">确认匹配</el-button>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Download, Search, Refresh, Camera, User, Warning, PieChart,
  View, EditPen, Delete
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const detailDialogVisible = ref(false)
const matchDialogVisible = ref(false)
const currentDetail = ref(null)
const currentCapture = ref(null)

// 搜索表单
const searchForm = reactive({
  cameraId: '',
  captureTime: null,
  recognitionStatus: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 匹配表单
const matchForm = reactive({
  libraryId: '',
  personId: '',
  remark: ''
})

// 统计数据
const stats = reactive({
  todayCapture: 1247,
  recognizedCount: 956,
  strangerCount: 291,
  totalCapture: 15643
})

// 选项数据
const cameraOptions = ref([
  { label: '大门入口摄像头01', value: 'camera_01' },
  { label: '大门入口摄像头02', value: 'camera_02' },
  { label: '停车场摄像头01', value: 'camera_03' },
  { label: '走廊摄像头01', value: 'camera_04' },
  { label: '电梯厅摄像头01', value: 'camera_05' }
])

const libraryOptions = ref([
  { label: '员工人员库', value: 'employee' },
  { label: '访客人员库', value: 'visitor' },
  { label: '黑名单人员库', value: 'blacklist' }
])

const personOptions = ref([])

// 抓拍记录数据
const captureList = ref([
  {
    id: '1',
    captureImage: 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg',
    cameraName: '大门入口摄像头01',
    captureTime: '2024-01-20 14:30:25',
    recognitionStatus: 'recognized',
    personName: '张三',
    confidence: 96.8,
    age: 28,
    gender: '男',
    location: '主楼一层大厅',
    remark: ''
  },
  {
    id: '2',
    captureImage: 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg',
    cameraName: '停车场摄像头01',
    captureTime: '2024-01-20 14:25:12',
    recognitionStatus: 'stranger',
    personName: '',
    confidence: null,
    age: 35,
    gender: '女',
    location: '地下停车场A区',
    remark: ''
  },
  {
    id: '3',
    captureImage: 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg',
    cameraName: '走廊摄像头01',
    captureTime: '2024-01-20 14:20:48',
    recognitionStatus: 'unrecognized',
    personName: '',
    confidence: null,
    age: 42,
    gender: '男',
    location: '主楼二层走廊',
    remark: ''
  }
])

// 获取状态颜色
const getStatusColor = (status: string) => {
  const colors = {
    recognized: 'success',
    unrecognized: 'warning',
    stranger: 'danger'
  }
  return colors[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const texts = {
    recognized: '已识别',
    unrecognized: '未识别',
    stranger: '陌生人'
  }
  return texts[status] || status
}

// 事件处理
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    cameraId: '',
    captureTime: null,
    recognitionStatus: ''
  })
  handleSearch()
}

const handleRefresh = () => {
  loadData()
  ElMessage.success('数据已刷新')
}

const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

const handleViewDetail = (row: any) => {
  currentDetail.value = row
  detailDialogVisible.value = true
}

const handleManualMatch = (row: any) => {
  currentCapture.value = row
  matchForm.libraryId = ''
  matchForm.personId = ''
  matchForm.remark = ''
  personOptions.value = []
  matchDialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该抓拍记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const index = captureList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      captureList.value.splice(index, 1)
    }
    
    ElMessage.success('删除成功')
  } catch {
    // 用户取消删除
  }
}

const loadPersonOptions = (libraryId: string) => {
  // 模拟根据人员库加载人员选项
  const mockPersons = {
    employee: [
      { label: '张三 - 技术部', value: 'person_1' },
      { label: '李四 - 市场部', value: 'person_2' },
      { label: '王五 - 财务部', value: 'person_3' }
    ],
    visitor: [
      { label: '访客001', value: 'visitor_1' },
      { label: '访客002', value: 'visitor_2' }
    ],
    blacklist: [
      { label: '黑名单001', value: 'black_1' },
      { label: '黑名单002', value: 'black_2' }
    ]
  }
  
  personOptions.value = mockPersons[libraryId] || []
}

const handleConfirmMatch = () => {
  if (!matchForm.libraryId || !matchForm.personId) {
    ElMessage.warning('请选择人员库和人员')
    return
  }
  
  // 更新抓拍记录的识别状态
  const index = captureList.value.findIndex(item => item.id === currentCapture.value.id)
  if (index > -1) {
    const selectedPerson = personOptions.value.find(p => p.value === matchForm.personId)
    captureList.value[index].recognitionStatus = 'recognized'
    captureList.value[index].personName = selectedPerson?.label.split(' - ')[0] || '未知'
    captureList.value[index].confidence = 100.0
    captureList.value[index].remark = matchForm.remark
  }
  
  matchDialogVisible.value = false
  ElMessage.success('匹配成功')
}

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    pagination.total = captureList.value.length
    loading.value = false
  }, 500)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">@use '@/styles/dark-theme.scss';

.app-container {
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 20px;
    background: #1a1a1a;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .header-title {
      h2 {
        margin: 0 0 8px 0;
        color: #303133;
        font-size: 24px;
      }

      p {
        margin: 0;
        color: #909399;
        font-size: 14px;
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .search-container {
    background: #1a1a1a;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    margin-bottom: 20px;

    .el-form-item {
      margin-bottom: 16px;
    }

    .el-input,
    .el-select,
    .el-date-picker {
      width: 200px;
    }
  }

  .stats-container {
    margin-bottom: 20px;

    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;

        .stat-icon {
          width: 60px;
          height: 60px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
          color: #fff;

          &.today {
            background: linear-gradient(45deg, #667eea 0%, #764ba2 100%);
          }

          &.recognized {
            background: linear-gradient(45deg, #f093fb 0%, #f5576c 100%);
          }

          &.stranger {
            background: linear-gradient(45deg, #4facfe 0%, #00f2fe 100%);
          }

          &.total {
            background: linear-gradient(45deg, #43e97b 0%, #38f9d7 100%);
          }
        }

        .stat-info {
          .stat-value {
            font-size: 28px;
            font-weight: bold;
            color: #303133;
            line-height: 1;
          }

          .stat-label {
            font-size: 14px;
            color: #909399;
            margin-top: 4px;
          }
        }
      }
    }
  }

  .table-container {
    background: #1a1a1a;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .el-pagination {
      margin-top: 20px;
      text-align: right;
    }
  }
}

.detail-content {
  .detail-image {
    text-align: center;
  }

  .detail-info {
    .el-descriptions {
      :deep(.el-descriptions__label) {
        width: 100px;
      }
    }
  }
}

.match-content {
  .capture-preview {
    text-align: center;

    h4 {
      margin: 0 0 16px 0;
      color: #303133;
    }
  }
}
</style>






