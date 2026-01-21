<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <h2>车辆抓拍</h2>
        <p>智能车辆识别和车牌抓拍管理</p>
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
        <el-form-item label="车牌号码">
          <el-input v-model="searchForm.plateNumber" placeholder="请输入车牌号码" clearable />
        </el-form-item>
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
        <el-form-item label="车辆类型">
          <el-select v-model="searchForm.vehicleType" placeholder="请选择车辆类型" clearable>
            <el-option label="小型汽车" value="small" />
            <el-option label="大型汽车" value="large" />
            <el-option label="摩托车" value="motorcycle" />
            <el-option label="新能源车" value="electric" />
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
                <el-icon><Checked /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.recognizedCount }}</div>
                <div class="stat-label">识别成功</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon accuracy">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.accuracy }}%</div>
                <div class="stat-label">识别准确率</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon unique">
                <el-icon><Operation /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.uniqueVehicles }}</div>
                <div class="stat-label">不重复车辆</div>
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
        <el-table-column prop="vehicleImage" label="车辆图片" width="140">
          <template #default="{ row }">
            <el-image
              :src="row.vehicleImage"
              :preview-src-list="[row.vehicleImage]"
              fit="cover"
              style="width: 100px; height: 70px; border-radius: 4px;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="plateImage" label="车牌图片" width="120">
          <template #default="{ row }">
            <el-image
              :src="row.plateImage"
              :preview-src-list="[row.plateImage]"
              fit="cover"
              style="width: 80px; height: 30px; border-radius: 2px;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="plateNumber" label="车牌号码" width="120">
          <template #default="{ row }">
            <div class="plate-number">
              <span class="plate-text">{{ row.plateNumber }}</span>
              <el-tag v-if="row.isRecognized" type="success" size="small" effect="plain">
                已识别
              </el-tag>
              <el-tag v-else type="warning" size="small" effect="plain">
                未识别
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="vehicleType" label="车辆类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getVehicleTypeColor(row.vehicleType)">
              {{ getVehicleTypeText(row.vehicleType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="vehicleColor" label="车身颜色" width="100" />
        <el-table-column prop="confidence" label="置信度" width="100">
          <template #default="{ row }">
            <el-progress
              :percentage="row.confidence"
              :color="getConfidenceColor(row.confidence)"
              :show-text="true"
              :stroke-width="6"
            />
          </template>
        </el-table-column>
        <el-table-column prop="cameraName" label="摄像头" width="140" show-overflow-tooltip />
        <el-table-column prop="location" label="位置" width="140" show-overflow-tooltip />
        <el-table-column prop="captureTime" label="抓拍时间" width="160" show-overflow-tooltip />
        <el-table-column prop="direction" label="行驶方向" width="100">
          <template #default="{ row }">
            <el-icon v-if="row.direction === 'in'" style="color: #67c23a;"><Bottom /></el-icon>
            <el-icon v-else style="color: #f56c6c;"><Top /></el-icon>
            <span style="margin-left: 4px;">{{ row.direction === 'in' ? '进入' : '驶出' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button link type="success" @click="handleCorrectPlate(row)" :disabled="row.isRecognized">
              <el-icon><Edit /></el-icon>
              纠正
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
      title="车辆抓拍详情"
      width="900px"
      destroy-on-close
    >
      <div v-if="currentDetail" class="detail-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-images">
              <h4>车辆图片</h4>
              <el-image
                :src="currentDetail.vehicleImage"
                fit="contain"
                style="width: 100%; max-height: 250px; border: 1px solid #ddd; border-radius: 4px; margin-bottom: 16px;"
              />
              <h4>车牌图片</h4>
              <el-image
                :src="currentDetail.plateImage"
                fit="contain"
                style="width: 100%; max-height: 100px; border: 1px solid #ddd; border-radius: 4px;"
              />
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-info">
              <h4>识别信息</h4>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="车牌号码">
                  <span class="plate-display">{{ currentDetail.plateNumber }}</span>
                  <el-tag v-if="currentDetail.isRecognized" type="success" size="small" style="margin-left: 8px;">
                    已识别
                  </el-tag>
                  <el-tag v-else type="warning" size="small" style="margin-left: 8px;">
                    未识别
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="车辆类型">
                  <el-tag :type="getVehicleTypeColor(currentDetail.vehicleType)">
                    {{ getVehicleTypeText(currentDetail.vehicleType) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="车身颜色">
                  {{ currentDetail.vehicleColor }}
                </el-descriptions-item>
                <el-descriptions-item label="置信度">
                  {{ currentDetail.confidence }}%
                </el-descriptions-item>
                <el-descriptions-item label="行驶方向">
                  <el-icon v-if="currentDetail.direction === 'in'" style="color: #67c23a;"><Bottom /></el-icon>
                  <el-icon v-else style="color: #f56c6c;"><Top /></el-icon>
                  <span style="margin-left: 4px;">{{ currentDetail.direction === 'in' ? '进入' : '驶出' }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="抓拍时间">
                  {{ currentDetail.captureTime }}
                </el-descriptions-item>
                <el-descriptions-item label="摄像头">
                  {{ currentDetail.cameraName }}
                </el-descriptions-item>
                <el-descriptions-item label="位置">
                  {{ currentDetail.location }}
                </el-descriptions-item>
                <el-descriptions-item label="车速" v-if="currentDetail.speed">
                  {{ currentDetail.speed }} km/h
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

    <!-- 车牌纠正弹框 -->
    <el-dialog
      v-model="correctDialogVisible"
      title="车牌号码纠正"
      width="500px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div class="correct-content">
        <div class="plate-preview">
          <h4>车牌图片</h4>
          <el-image
            v-if="currentCapture"
            :src="currentCapture.plateImage"
            fit="contain"
            style="width: 200px; height: 80px; border: 1px solid #ddd; border-radius: 4px;"
          />
        </div>
        
        <el-form ref="correctFormRef" :model="correctForm" :rules="correctRules" label-width="100px" style="margin-top: 20px;">
          <el-form-item label="识别结果">
            <el-input v-model="currentCapture.plateNumber" disabled />
          </el-form-item>
          
          <el-form-item label="正确车牌" prop="correctPlate">
            <el-input
              v-model="correctForm.correctPlate"
              placeholder="请输入正确的车牌号码"
              style="text-transform: uppercase;"
            />
          </el-form-item>
          
          <el-form-item label="车辆类型" prop="vehicleType">
            <el-select v-model="correctForm.vehicleType" placeholder="请选择车辆类型">
              <el-option label="小型汽车" value="small" />
              <el-option label="大型汽车" value="large" />
              <el-option label="摩托车" value="motorcycle" />
              <el-option label="新能源车" value="electric" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="车身颜色">
            <el-select v-model="correctForm.vehicleColor" placeholder="请选择车身颜色">
              <el-option label="白色" value="白色" />
              <el-option label="黑色" value="黑色" />
              <el-option label="银色" value="银色" />
              <el-option label="红色" value="红色" />
              <el-option label="蓝色" value="蓝色" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="备注">
            <el-input
              v-model="correctForm.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入备注信息"
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="correctDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmCorrect">确认纠正</el-button>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Download, Search, Refresh, Camera, Checked, TrendCharts, Operation,
  View, Edit, Delete, Bottom, Top
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const detailDialogVisible = ref(false)
const correctDialogVisible = ref(false)
const currentDetail = ref(null)
const currentCapture = ref(null)

// 搜索表单
const searchForm = reactive({
  plateNumber: '',
  cameraId: '',
  vehicleType: '',
  captureTime: null
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 纠正表单
const correctForm = reactive({
  correctPlate: '',
  vehicleType: '',
  vehicleColor: '',
  remark: ''
})

const correctRules = {
  correctPlate: [{ required: true, message: '请输入正确的车牌号码', trigger: 'blur' }],
  vehicleType: [{ required: true, message: '请选择车辆类型', trigger: 'change' }]
}

// 统计数据
const stats = reactive({
  todayCapture: 2456,
  recognizedCount: 2234,
  accuracy: 91.0,
  uniqueVehicles: 1876
})

// 选项数据
const cameraOptions = ref([
  { label: '大门入口摄像头01', value: 'camera_01' },
  { label: '大门入口摄像头02', value: 'camera_02' },
  { label: '停车场摄像头01', value: 'camera_03' },
  { label: '停车场出入口摄像头', value: 'camera_04' },
  { label: '地下车库摄像头01', value: 'camera_05' }
])

// 抓拍记录数据
const captureList = ref([
  {
    id: '1',
    vehicleImage: 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg',
    plateImage: 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg',
    plateNumber: '京A12345',
    isRecognized: true,
    vehicleType: 'small',
    vehicleColor: '白色',
    confidence: 96.8,
    cameraName: '大门入口摄像头01',
    location: '主楼入口',
    captureTime: '2024-01-20 14:30:25',
    direction: 'in',
    speed: 15,
    remark: ''
  },
  {
    id: '2',
    vehicleImage: 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg',
    plateImage: 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg',
    plateNumber: '沪B67890',
    isRecognized: true,
    vehicleType: 'electric',
    vehicleColor: '蓝色',
    confidence: 89.2,
    cameraName: '停车场摄像头01',
    location: '地下停车场入口',
    captureTime: '2024-01-20 14:25:12',
    direction: 'in',
    speed: 12,
    remark: ''
  },
  {
    id: '3',
    vehicleImage: 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg',
    plateImage: 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg',
    plateNumber: '粤C***21',
    isRecognized: false,
    vehicleType: 'large',
    vehicleColor: '黑色',
    confidence: 65.4,
    cameraName: '大门出口摄像头',
    location: '主楼出口',
    captureTime: '2024-01-20 14:20:48',
    direction: 'out',
    speed: 18,
    remark: ''
  }
])

const correctFormRef = ref()

// 获取车辆类型颜色
const getVehicleTypeColor = (type: string) => {
  const colors = {
    small: 'primary',
    large: 'warning',
    motorcycle: 'info',
    electric: 'success'
  }
  return colors[type] || 'info'
}

// 获取车辆类型文本
const getVehicleTypeText = (type: string) => {
  const texts = {
    small: '小型汽车',
    large: '大型汽车',
    motorcycle: '摩托车',
    electric: '新能源车'
  }
  return texts[type] || type
}

// 获取置信度颜色
const getConfidenceColor = (confidence: number) => {
  if (confidence >= 90) return '#67c23a'
  if (confidence >= 70) return '#e6a23c'
  return '#f56c6c'
}

// 事件处理
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    plateNumber: '',
    cameraId: '',
    vehicleType: '',
    captureTime: null
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

const handleCorrectPlate = (row: any) => {
  currentCapture.value = row
  correctForm.correctPlate = ''
  correctForm.vehicleType = row.vehicleType
  correctForm.vehicleColor = row.vehicleColor
  correctForm.remark = ''
  correctDialogVisible.value = true
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

const handleConfirmCorrect = async () => {
  if (!correctFormRef.value) return
  
  try {
    await correctFormRef.value.validate()
    
    // 更新抓拍记录
    const index = captureList.value.findIndex(item => item.id === currentCapture.value.id)
    if (index > -1) {
      captureList.value[index].plateNumber = correctForm.correctPlate
      captureList.value[index].isRecognized = true
      captureList.value[index].vehicleType = correctForm.vehicleType
      captureList.value[index].vehicleColor = correctForm.vehicleColor
      captureList.value[index].confidence = 100.0
      captureList.value[index].remark = correctForm.remark
    }
    
    correctDialogVisible.value = false
    ElMessage.success('车牌纠正成功')
  } catch (error) {
    console.error('表单验证失败:', error)
  }
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

          &.accuracy {
            background: linear-gradient(45deg, #4facfe 0%, #00f2fe 100%);
          }

          &.unique {
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

    .plate-number {
      .plate-text {
        font-weight: bold;
        color: #303133;
        margin-right: 8px;
      }
    }

    .el-pagination {
      margin-top: 20px;
      text-align: right;
    }
  }
}

.detail-content {
  .detail-images,
  .detail-info {
    h4 {
      margin: 0 0 16px 0;
      color: #303133;
      font-size: 16px;
    }
  }

  .detail-info {
    .plate-display {
      font-weight: bold;
      font-size: 16px;
      color: #303133;
    }

    .el-descriptions {
      :deep(.el-descriptions__label) {
        width: 100px;
      }
    }
  }
}

.correct-content {
  .plate-preview {
    text-align: center;

    h4 {
      margin: 0 0 16px 0;
      color: #303133;
    }
  }
}
</style>






