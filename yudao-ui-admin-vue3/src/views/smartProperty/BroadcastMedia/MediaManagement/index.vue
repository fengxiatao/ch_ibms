<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="media-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <Icon icon="ep:video-play" :size="24" />
        <h1>媒体管理</h1>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleUpload">
          <Icon icon="ep:plus" />
          新增
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item>
          <el-input
            v-model="searchForm.mediaName"
            placeholder="请输入媒体名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-select
            v-model="searchForm.mediaStatus"
            placeholder="请选择媒体状态"
            clearable
            style="width: 180px"
          >
            <el-option label="已通过" value="approved" />
            <el-option label="待审核" value="pending" />
            <el-option label="已驳回" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select
            v-model="searchForm.syncStatus"
            placeholder="请选择同步状态"
            clearable
            style="width: 180px"
          >
            <el-option label="同步中" value="syncing" />
            <el-option label="同步成功" value="synced" />
            <el-option label="同步失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleMoreFilters">
            更多筛选
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 媒体管理列表 -->
    <div class="media-section">
      <h2>媒体管理</h2>
      
      <el-table
        v-loading="loading"
        :data="mediaList"
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="sequence" label="序号" width="80" align="center" />
        <el-table-column prop="mediaCode" label="申请编号" width="180" />
        <el-table-column prop="mediaName" label="媒体名称" min-width="180" />
        <el-table-column prop="mediaSize" label="媒体大小" width="120" />
        <el-table-column prop="duration" label="时长" width="120" />
        <el-table-column prop="mediaStatus" label="审核状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getMediaStatusColor(row.mediaStatus)" size="small">
              {{ row.mediaStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="syncStatus" label="同步状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getSyncStatusColor(row.syncStatus)" size="small">
              {{ row.syncStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              查看
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handlePageSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 文件上传弹窗 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="新增媒体"
      width="600px"
      destroy-on-close
    >
      <div class="upload-section">
        <div class="upload-tips">
          <p><Icon icon="ep:info-filled" color="#409eff" /> 支持音频：mp3，单个文件最大200M</p>
          <p><Icon icon="ep:warning-filled" color="#e6a23c" /> 服务器会计算内容，严禁上传包含违法内容，不得违反等非法内容，上传10个</p>
        </div>
        
        <div class="upload-area">
          <el-upload
            ref="uploadRef"
            class="upload-dragger"
            drag
            :auto-upload="false"
            :multiple="true"
            :file-list="fileList"
            :on-change="handleFileChange"
            :before-upload="beforeUpload"
          >
            <Icon icon="ep:upload-filled" :size="48" color="#00d4ff" />
            <div class="upload-text">
              <p>将文件拖到此处，或<em>点击上传</em></p>
            </div>
          </el-upload>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="uploadDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmUpload">确认上传</el-button>
        </div>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

interface Media {
  id: string
  sequence: number
  mediaCode: string
  mediaName: string
  mediaSize: string
  duration: string
  mediaStatus: string
  syncStatus: string
}

// 响应式数据
const loading = ref(false)
const uploadDialogVisible = ref(false)

const searchForm = reactive({
  mediaName: '',
  mediaStatus: '',
  syncStatus: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 3
})

const fileList = ref<any[]>([])

// 媒体列表数据
const mediaList = ref<Media[]>([
  {
    id: '1',
    sequence: 1,
    mediaCode: '20SMT20250728001',
    mediaName: 'Rom脆歌-魅力万里.mp3',
    mediaSize: '8.19MB',
    duration: '00:03:34',
    mediaStatus: '已通过',
    syncStatus: '同步失败'
  },
  {
    id: '2',
    sequence: 2,
    mediaCode: '20SMT20241080002',
    mediaName: '凤凰传奇-荷塘月光.mp3',
    mediaSize: '3.62MB',
    duration: '00:03:57',
    mediaStatus: '已通过',
    syncStatus: '同步失败'
  },
  {
    id: '3',
    sequence: 3,
    mediaCode: '20SMT20241080001',
    mediaName: '保卫生-有没有人告诉你.mp3',
    mediaSize: '5.25MB',
    duration: '00:05:44',
    mediaStatus: '已通过',
    syncStatus: '同步失败'
  }
])

// 工具函数
const getMediaStatusColor = (status: string) => {
  switch (status) {
    case '已通过':
      return 'success'
    case '待审核':
      return 'warning'
    case '已驳回':
      return 'danger'
    default:
      return ''
  }
}

const getSyncStatusColor = (status: string) => {
  switch (status) {
    case '同步成功':
      return 'success'
    case '同步失败':
      return 'danger'
    case '同步中':
      return 'warning'
    default:
      return ''
  }
}

// 事件处理
const handleMoreFilters = () => {
  ElMessage.success('更多筛选功能开发中...')
}

const handleUpload = () => {
  uploadDialogVisible.value = true
  fileList.value = []
}

const handleFileChange = (file: any, files: any[]) => {
  fileList.value = files
}

const beforeUpload = (file: File) => {
  const maxSize = 200 * 1024 * 1024 // 200MB
  if (file.size > maxSize) {
    ElMessage.error('文件大小超过限制')
    return false
  }
  return true
}

const confirmUpload = () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  // 模拟上传
  fileList.value.forEach((file, index) => {
    const newMedia: Media = {
      id: `new_${Date.now()}_${index}`,
      sequence: mediaList.value.length + index + 1,
      mediaCode: `20SMT${new Date().getFullYear()}${String(new Date().getMonth() + 1).padStart(2, '0')}${String(new Date().getDate()).padStart(2, '0')}${String(mediaList.value.length + index + 1).padStart(3, '0')}`,
      mediaName: file.name,
      mediaSize: (file.size / 1024 / 1024).toFixed(2) + 'MB',
      duration: '00:03:30',
      mediaStatus: '已通过',
      syncStatus: '同步中'
    }
    mediaList.value.unshift(newMedia)
  })

  pagination.total = mediaList.value.length
  uploadDialogVisible.value = false
  ElMessage.success(`成功上传 ${fileList.value.length} 个文件`)
}

const handleView = (media: Media) => {
  ElMessage.info('查看功能开发中...')
}

const handleEdit = (media: Media) => {
  ElMessage.info('编辑功能开发中...')
}

const handleDelete = (media: Media) => {
  ElMessageBox.confirm('确定要删除这个媒体文件吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = mediaList.value.findIndex(item => item.id === media.id)
    if (index > -1) {
      mediaList.value.splice(index, 1)
      pagination.total = mediaList.value.length
      ElMessage.success('删除成功')
    }
  })
}

const handlePageSizeChange = (size: number) => {
  pagination.size = size
  loadData()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  loadData()
}

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

// 生命周期
onMounted(() => {
  pagination.total = mediaList.value.length
  loadData()
})
</script>

<style scoped lang="scss">
.media-management {
  padding: 20px;
  background: linear-gradient(135deg, #0a1628 0%, #1e3a8a 50%, #0f172a 100%);
  min-height: auto;
  color: #ffffff;

  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
    padding: 16px 24px;
    background: rgba(15, 23, 42, 0.8);
    backdrop-filter: blur(15px);
    border-radius: 12px;
    border: 1px solid rgba(0, 212, 255, 0.1);

    .header-title {
      display: flex;
      align-items: center;
      gap: 12px;

      .el-icon {
        color: #00d4ff;
      }

      h1 {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: #00d4ff;
      }
    }
  }

  .search-section {
    margin-bottom: 20px;
    padding: 20px;
    background: rgba(15, 23, 42, 0.7);
    border-radius: 8px;
    border: 1px solid rgba(0, 212, 255, 0.1);
  }

  .media-section {
    background: rgba(15, 23, 42, 0.7);
    border-radius: 8px;
    padding: 20px;
    border: 1px solid rgba(0, 212, 255, 0.1);

    h2 {
      margin: 0 0 20px 0;
      color: #00d4ff;
      font-size: 18px;
      font-weight: 600;
    }

    :deep(.el-table) {
      background: transparent;
      color: #e2e8f0;

      .el-table__header {
        background: rgba(0, 212, 255, 0.1);
        
        th {
          background: transparent;
          color: #00d4ff;
          border-bottom: 1px solid rgba(0, 212, 255, 0.2);
          font-weight: 600;
        }
      }

      .el-table__body {
        tr {
          background: transparent;
          
          &:hover {
            background: rgba(0, 212, 255, 0.05);
          }

          td {
            border-bottom: 1px solid rgba(71, 85, 105, 0.3);
          }
        }
      }
    }

    .pagination-section {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;

      :deep(.el-pagination) {
        .el-pager li, .btn-prev, .btn-next {
          background: rgba(15, 23, 42, 0.7);
          color: #e2e8f0;
          border: 1px solid rgba(0, 212, 255, 0.2);

          &:hover {
            color: #00d4ff;
          }

          &.is-active {
            background: #00d4ff;
            color: #0f172a;
          }
        }

        .el-pagination__total,
        .el-pagination__jump {
          color: #e2e8f0;
        }
      }
    }
  }

  .upload-section {
    .upload-tips {
      margin-bottom: 20px;
      padding: 16px;
      background: #f8fafc;
      border-radius: 6px;
      border-left: 4px solid #409eff;

      p {
        margin: 8px 0;
        color: #606266;
        font-size: 14px;
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }

    .upload-area {
      :deep(.el-upload-dragger) {
        background: rgba(15, 23, 42, 0.3);
        border: 2px dashed #00d4ff;
        border-radius: 8px;
        width: 100%;
        height: 200px;

        &:hover {
          border-color: #00d4ff;
          background: rgba(15, 23, 42, 0.5);
        }
      }

      .upload-text {
        margin-top: 16px;
        
        p {
          color: #e2e8f0;
          font-size: 16px;
          margin: 0;

          em {
            color: #00d4ff;
            font-style: normal;
            text-decoration: underline;
          }
        }
      }
    }
  }
}

:deep(.el-dialog) {
  .el-dialog__header {
    background: #f8fafc;
    border-bottom: 1px solid #e5e7eb;
  }

  .el-dialog__title {
    color: #374151;
    font-weight: 600;
  }

  .el-dialog__body {
    padding: 24px;
  }

  .el-dialog__footer {
    background: #f8fafc;
    border-top: 1px solid #e5e7eb;
  }
}
</style>



