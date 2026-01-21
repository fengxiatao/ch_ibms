<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="material-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <Icon icon="ep:folder" :size="24" />
        <h1>素材管理</h1>
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
          <el-select
            v-model="searchForm.materialType"
            placeholder="请选择素材类型"
            clearable
            style="width: 180px"
          >
            <el-option label="图片" value="image" />
            <el-option label="视频" value="video" />
            <el-option label="音频" value="audio" />
            <el-option label="文档" value="document" />
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
          <el-select
            v-model="searchForm.approvalStatus"
            placeholder="请选择审核状态"
            clearable
            style="width: 180px"
          >
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已驳回" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleMoreFilters">
            更多筛选
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 素材管理列表 -->
    <div class="material-section">
      <h2>素材管理</h2>
      
      <!-- 卡片视图 -->
      <div class="material-cards">
        <div
          v-for="material in materialList"
          :key="material.id"
          class="material-card"
        >
          <div class="card-preview">
            <div class="preview-container">
              <Icon 
                :icon="getFileIcon(material.fileName)" 
                :size="48" 
                :color="getFileIconColor(material.fileName)"
              />
              <div class="file-info">
                <div class="file-name">{{ material.fileName }}</div>
                <div class="file-size">{{ material.fileSize }}</div>
              </div>
            </div>
            <div class="sync-status">
              <el-tag :type="getSyncStatusType(material.syncStatus)" size="small">
                {{ getSyncStatusText(material.syncStatus) }}
              </el-tag>
            </div>
          </div>
          
          <div class="card-content">
            <div class="material-info">
              <div class="info-item">
                <Icon icon="ep:document" />
                <span>申请编号：{{ material.applicationCode }}</span>
              </div>
              <div class="info-item">
                <Icon icon="ep:clock" />
                <span>同步状态：{{ material.syncStatus }}</span>
              </div>
            </div>
            <div class="card-actions">
              <el-button link type="primary" @click="handlePreview(material)">
                预览
              </el-button>
              <el-button link type="primary" @click="handleEdit(material)">
                编辑
              </el-button>
              <el-button link type="danger" @click="handleDelete(material)">
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>

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
      title="新增素材"
      width="600px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div class="upload-section">
        <div class="upload-tips">
          <p><Icon icon="ep:info-filled" color="#409eff" /> 支持格式：jpg/png/bmp/jpeg，单个文件最大30M</p>
          <p><Icon icon="ep:info-filled" color="#409eff" /> 支持音频：mp3/wav/wma，单个文件最大200M</p>
          <p><Icon icon="ep:info-filled" color="#409eff" /> 支持视频：avi/mov/asf/mkv/wmv/dav，单个文件最大2G</p>
          <p><Icon icon="ep:info-filled" color="#409eff" /> 支持文档：txt/pdf/doc/docx/ppt/pptx/xls/xlsx，单个文件最大200M</p>
          <p><Icon icon="ep:warning-filled" color="#e6a23c" /> 服务器会计算内容，严禁上传包含违法内容，不得违反等非法内容</p>
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

    <!-- 预览弹窗 -->
    <el-dialog
      v-model="previewDialogVisible"
      :title="'预览 - ' + currentMaterial.fileName"
      width="800px"
      destroy-on-close
    >
      <div class="preview-content">
        <div class="preview-info">
          <div class="info-row">
            <label>文件名称：</label>
            <span>{{ currentMaterial.fileName }}</span>
          </div>
          <div class="info-row">
            <label>文件大小：</label>
            <span>{{ currentMaterial.fileSize }}</span>
          </div>
          <div class="info-row">
            <label>申请编号：</label>
            <span>{{ currentMaterial.applicationCode }}</span>
          </div>
          <div class="info-row">
            <label>同步状态：</label>
            <el-tag :type="getSyncStatusType(currentMaterial.syncStatus)" size="small">
              {{ getSyncStatusText(currentMaterial.syncStatus) }}
            </el-tag>
          </div>
        </div>
        
        <div class="preview-display">
          <div v-if="isImageFile(currentMaterial.fileName)" class="image-preview">
            <img :src="'/api/preview/' + currentMaterial.id" alt="预览图" />
          </div>
          <div v-else-if="isVideoFile(currentMaterial.fileName)" class="video-preview">
            <video controls>
              <source :src="'/api/preview/' + currentMaterial.id" type="video/mp4" />
              您的浏览器不支持视频播放
            </video>
          </div>
          <div v-else-if="isAudioFile(currentMaterial.fileName)" class="audio-preview">
            <audio controls>
              <source :src="'/api/preview/' + currentMaterial.id" type="audio/mpeg" />
              您的浏览器不支持音频播放
            </audio>
          </div>
          <div v-else class="file-preview">
            <Icon :icon="getFileIcon(currentMaterial.fileName)" :size="80" />
            <p>该文件类型不支持预览</p>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="previewDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="handleDownload(currentMaterial)">下载</el-button>
        </div>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

interface Material {
  id: string
  fileName: string
  fileSize: string
  applicationCode: string
  syncStatus: string
  uploadTime: string
}

// 响应式数据
const loading = ref(false)
const uploadDialogVisible = ref(false)
const previewDialogVisible = ref(false)

const searchForm = reactive({
  materialType: '',
  syncStatus: '',
  approvalStatus: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 8
})

const fileList = ref<any[]>([])
const currentMaterial = ref<Material>({
  id: '',
  fileName: '',
  fileSize: '',
  applicationCode: '',
  syncStatus: '',
  uploadTime: ''
})

// 素材列表数据
const materialList = ref<Material[]>([
  {
    id: '1',
    fileName: '097F7BA85D49...',
    fileSize: '39.77KB',
    applicationCode: '20SSC20250722001',
    syncStatus: '同步中',
    uploadTime: '2024-07-22 10:30:15'
  },
  {
    id: '2',
    fileName: 'WechatIMG2.jpg',
    fileSize: '118.98KB',
    applicationCode: '20SSC20250721001',
    syncStatus: '同步中',
    uploadTime: '2024-07-21 14:20:30'
  },
  {
    id: '3',
    fileName: '下载 (11).png',
    fileSize: '43.67KB',
    applicationCode: '20SSC20241029001',
    syncStatus: '-',
    uploadTime: '2024-10-29 09:15:45'
  },
  {
    id: '4',
    fileName: '构架图.png',
    fileSize: '153.71KB',
    applicationCode: '20SSC20241040004',
    syncStatus: '-',
    uploadTime: '2024-10-04 16:45:20'
  },
  {
    id: '5',
    fileName: '能源缴费.png',
    fileSize: '243.40KB',
    applicationCode: '20SSC20241040003',
    syncStatus: '同步成功',
    uploadTime: '2024-10-03 11:30:10'
  },
  {
    id: '6',
    fileName: '智农能耗智能监...',
    fileSize: '1.69MB',
    applicationCode: '20SSC20241040002',
    syncStatus: '同步失败',
    uploadTime: '2024-10-02 13:20:55'
  },
  {
    id: '7',
    fileName: '数字化助力企业...',
    fileSize: '6.02MB',
    applicationCode: '20SSC20241040001',
    syncStatus: '同步中',
    uploadTime: '2024-10-01 08:45:30'
  },
  {
    id: '8',
    fileName: '智农能耗智能监...',
    fileSize: '1.69MB',
    applicationCode: '20SSC20241020001',
    syncStatus: '同步中',
    uploadTime: '2024-10-20 15:10:25'
  }
])

// 工具函数
const getFileIcon = (fileName: string) => {
  const ext = fileName.split('.').pop()?.toLowerCase()
  switch (ext) {
    case 'jpg':
    case 'jpeg':
    case 'png':
    case 'gif':
    case 'bmp':
      return 'ep:picture'
    case 'mp4':
    case 'avi':
    case 'mov':
    case 'wmv':
      return 'ep:video-play'
    case 'mp3':
    case 'wav':
    case 'wma':
      return 'ep:headphones'
    case 'pdf':
      return 'ep:document'
    case 'doc':
    case 'docx':
      return 'ep:document'
    case 'xls':
    case 'xlsx':
      return 'ep:document'
    case 'ppt':
    case 'pptx':
      return 'ep:document'
    default:
      return 'ep:folder'
  }
}

const getFileIconColor = (fileName: string) => {
  const ext = fileName.split('.').pop()?.toLowerCase()
  switch (ext) {
    case 'jpg':
    case 'jpeg':
    case 'png':
    case 'gif':
    case 'bmp':
      return '#67C23A'
    case 'mp4':
    case 'avi':
    case 'mov':
    case 'wmv':
      return '#E6A23C'
    case 'mp3':
    case 'wav':
    case 'wma':
      return '#409EFF'
    default:
      return '#909399'
  }
}

const getSyncStatusType = (status: string) => {
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

const getSyncStatusText = (status: string) => {
  return status === '-' ? '未同步' : status
}

const isImageFile = (fileName: string) => {
  const ext = fileName.split('.').pop()?.toLowerCase()
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp'].includes(ext || '')
}

const isVideoFile = (fileName: string) => {
  const ext = fileName.split('.').pop()?.toLowerCase()
  return ['mp4', 'avi', 'mov', 'wmv'].includes(ext || '')
}

const isAudioFile = (fileName: string) => {
  const ext = fileName.split('.').pop()?.toLowerCase()
  return ['mp3', 'wav', 'wma'].includes(ext || '')
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
  const maxSize = getMaxFileSize(file.name)
  if (file.size > maxSize) {
    ElMessage.error(`文件大小超过限制`)
    return false
  }
  return true
}

const getMaxFileSize = (fileName: string) => {
  const ext = fileName.split('.').pop()?.toLowerCase()
  if (['jpg', 'jpeg', 'png', 'gif', 'bmp'].includes(ext || '')) {
    return 30 * 1024 * 1024 // 30MB
  } else if (['mp3', 'wav', 'wma'].includes(ext || '')) {
    return 200 * 1024 * 1024 // 200MB
  } else if (['avi', 'mov', 'asf', 'mkv', 'wmv', 'dav'].includes(ext || '')) {
    return 2 * 1024 * 1024 * 1024 // 2GB
  } else {
    return 200 * 1024 * 1024 // 200MB
  }
}

const confirmUpload = () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  // 模拟上传
  fileList.value.forEach((file, index) => {
    const newMaterial: Material = {
      id: `new_${Date.now()}_${index}`,
      fileName: file.name,
      fileSize: (file.size / 1024).toFixed(2) + 'KB',
      applicationCode: `20SSC${new Date().getFullYear()}${String(new Date().getMonth() + 1).padStart(2, '0')}${String(new Date().getDate()).padStart(2, '0')}${String(materialList.value.length + index + 1).padStart(3, '0')}`,
      syncStatus: '同步中',
      uploadTime: new Date().toLocaleString('zh-CN')
    }
    materialList.value.unshift(newMaterial)
  })

  pagination.total = materialList.value.length
  uploadDialogVisible.value = false
  ElMessage.success(`成功上传 ${fileList.value.length} 个文件`)
}

const handlePreview = (material: Material) => {
  currentMaterial.value = { ...material }
  previewDialogVisible.value = true
}

const handleEdit = (material: Material) => {
  ElMessage.info('编辑功能开发中...')
}

const handleDelete = (material: Material) => {
  ElMessageBox.confirm('确定要删除这个素材吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = materialList.value.findIndex(item => item.id === material.id)
    if (index > -1) {
      materialList.value.splice(index, 1)
      pagination.total = materialList.value.length
      ElMessage.success('删除成功')
    }
  })
}

const handleDownload = (material: Material) => {
  ElMessage.success('下载功能开发中...')
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
  pagination.total = materialList.value.length
  loadData()
})
</script>

<style scoped lang="scss">
.material-management {
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

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .search-section {
    margin-bottom: 20px;
    padding: 20px;
    background: rgba(15, 23, 42, 0.7);
    border-radius: 8px;
    border: 1px solid rgba(0, 212, 255, 0.1);

    :deep(.el-form-item__label) {
      color: #e2e8f0;
    }
  }

  .material-section {
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

    .material-cards {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;
      margin-bottom: 20px;

      .material-card {
        background: rgba(30, 58, 138, 0.3);
        border: 1px solid rgba(0, 212, 255, 0.2);
        border-radius: 8px;
        padding: 16px;
        transition: all 0.3s ease;

        &:hover {
          border-color: #00d4ff;
          background: rgba(30, 58, 138, 0.5);
          transform: translateY(-2px);
        }

        .card-preview {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 12px;

          .preview-container {
            display: flex;
            align-items: center;
            gap: 12px;

            .file-info {
              .file-name {
                color: #00d4ff;
                font-weight: 500;
                margin-bottom: 4px;
                max-width: 180px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              }

              .file-size {
                color: #e2e8f0;
                font-size: 12px;
              }
            }
          }

          .sync-status {
            flex-shrink: 0;
          }
        }

        .card-content {
          .material-info {
            .info-item {
              display: flex;
              align-items: center;
              gap: 8px;
              margin-bottom: 8px;
              color: #e2e8f0;
              font-size: 14px;

              .el-icon {
                color: #00d4ff;
                font-size: 16px;
              }

              &:last-child {
                margin-bottom: 12px;
              }
            }
          }

          .card-actions {
            display: flex;
            gap: 12px;
            justify-content: flex-end;
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

  // 弹窗内容样式
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
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;

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

  .preview-content {
    .preview-info {
      margin-bottom: 20px;
      padding: 16px;
      background: #f8fafc;
      border-radius: 6px;

      .info-row {
        display: flex;
        align-items: center;
        margin-bottom: 8px;

        &:last-child {
          margin-bottom: 0;
        }

        label {
          font-weight: 500;
          color: #374151;
          margin-right: 8px;
          min-width: 80px;
        }

        span {
          color: #6b7280;
        }
      }
    }

    .preview-display {
      text-align: center;
      min-height: 200px;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 1px solid #e5e7eb;
      border-radius: 6px;
      background: #f8fafc;

      .image-preview img {
        max-width: 100%;
        max-height: 400px;
      }

      .video-preview video {
        max-width: 100%;
        max-height: 400px;
      }

      .audio-preview audio {
        width: 100%;
      }

      .file-preview {
        color: #6b7280;

        p {
          margin-top: 16px;
          font-size: 16px;
        }
      }
    }
  }
}

// 全局弹窗样式覆盖
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
