<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="meeting-room-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <Icon icon="ep:office-building" :size="24" />
        <h1>会议室管理</h1>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
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
            v-model="searchForm.location"
            placeholder="请选择空间位置"
            clearable
            style="width: 180px"
          >
            <el-option label="1楼会议室" value="1F" />
            <el-option label="2楼会议室" value="2F" />
            <el-option label="3楼会议室" value="3F" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="searchForm.roomName"
            placeholder="请输入会议室名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <Icon icon="ep:search" />
            搜索
          </el-button>
          <el-button @click="handleReset">
            <Icon icon="ep:refresh" />
            重置
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 视图切换 -->
      <div class="view-controls">
        <el-radio-group v-model="viewMode" @change="handleViewChange">
          <el-radio-button value="card">
            <Icon icon="ep:grid" />
          </el-radio-button>
          <el-radio-button value="list">
            <Icon icon="ep:menu" />
          </el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 会议室管理内容 -->
    <div class="room-section">
      <h2>会议室管理</h2>
      
      <!-- 卡片视图 -->
      <div v-if="viewMode === 'card'" class="room-cards">
        <div
          v-for="room in roomList"
          :key="room.id"
          class="room-card"
        >
          <div class="card-header">
            <div class="room-info">
              <h3>{{ room.roomName }}</h3>
              <div class="room-details">
                <div class="detail-item">
                  <Icon icon="ep:user" />
                  <span>负责人员：{{ room.manager }}</span>
                </div>
                <div class="detail-item">
                  <Icon icon="ep:user-filled" />
                  <span>容纳人数：{{ room.capacity }}</span>
                </div>
                <div class="detail-item">
                  <Icon icon="ep:circle-check" />
                  <span>状态：
                    <el-tag :type="room.status === '启用' ? 'success' : 'danger'" size="small">
                      {{ room.status }}
                    </el-tag>
                  </span>
                </div>
              </div>
            </div>
            <div class="card-actions">
              <el-button type="primary" size="small" @click="handleEdit(room)">
                编辑
              </el-button>
              <el-dropdown @command="(command: string) => handleDropdownCommand(command, room)">
                <el-button size="small">
                  更多
                  <Icon icon="ep:arrow-down" />
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="view">查看</el-dropdown-item>
                    <el-dropdown-item command="delete">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-else class="room-table">
        <el-table
          v-loading="loading"
          :data="roomList"
          style="width: 100%"
          row-key="id"
        >
          <el-table-column prop="sequence" label="序号" width="80" align="center" />
          <el-table-column prop="roomName" label="会议室名称" min-width="140" />
          <el-table-column prop="location" label="空间位置" width="150" />
          <el-table-column prop="capacity" label="容纳人数" width="100" align="center" />
          <el-table-column prop="manager" label="负责人员" width="120" />
          <el-table-column prop="contact" label="联系电话" width="140" />
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === '启用' ? 'success' : 'danger'" size="small">
                {{ row.status }}
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
      </div>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handlePageSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 新增/编辑会议室弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'add' ? '新增会议室' : dialogMode === 'edit' ? '编辑会议室' : '查看会议室'"
      width="600px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div class="room-form">
        <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
          <el-form-item label="会议室名称" prop="roomName">
            <el-input
              v-model="formData.roomName"
              placeholder="请输入会议室名称"
              :disabled="dialogMode === 'view'"
            />
          </el-form-item>
          
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="formData.status" :disabled="dialogMode === 'view'">
              <el-radio value="启用">启用</el-radio>
              <el-radio value="停用">停用</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="空间位置" prop="location">
            <el-select
              v-model="formData.location"
              placeholder="请选择空间位置"
              style="width: 100%"
              :disabled="dialogMode === 'view'"
            >
              <el-option label="1楼会议室" value="1楼会议室" />
              <el-option label="2楼会议室" value="2楼会议室" />
              <el-option label="3楼会议室" value="3楼会议室" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="容纳人数" prop="capacity">
            <el-input-number
              v-model="formData.capacity"
              :min="1"
              :max="100"
              placeholder="人"
              style="width: 100%"
              :disabled="dialogMode === 'view'"
            />
          </el-form-item>
          
          <el-form-item label="负责人员" prop="manager">
            <el-input
              v-model="formData.manager"
              placeholder="请输入负责人员"
              :disabled="dialogMode === 'view'"
            />
          </el-form-item>
          
          <el-form-item label="联系电话" prop="contact">
            <el-input
              v-model="formData.contact"
              placeholder="请输入联系电话"
              :disabled="dialogMode === 'view'"
            />
          </el-form-item>
          
          <el-form-item label="会议室设备">
            <el-input
              v-model="formData.equipment"
              type="textarea"
              :rows="3"
              placeholder="请输入会议室设备"
              maxlength="300"
              show-word-limit
              :disabled="dialogMode === 'view'"
            />
          </el-form-item>
          
          <el-form-item label="备注">
            <el-input
              v-model="formData.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入备注"
              maxlength="300"
              show-word-limit
              :disabled="dialogMode === 'view'"
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">
            {{ dialogMode === 'view' ? '关闭' : '取消' }}
          </el-button>
          <el-button v-if="dialogMode !== 'view'" type="primary" @click="handleSubmit">
            确认
          </el-button>
        </div>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

interface MeetingRoom {
  id: string
  sequence: number
  roomName: string
  location: string
  capacity: number
  manager: string
  contact: string
  status: string
  equipment?: string
  remark?: string
}

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref<'add' | 'edit' | 'view'>('add')
const viewMode = ref('card')

const searchForm = reactive({
  location: '',
  roomName: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 2
})

const formData = reactive({
  roomName: '',
  status: '启用',
  location: '',
  capacity: 0,
  manager: '',
  contact: '',
  equipment: '',
  remark: ''
})

const formRules = {
  roomName: [{ required: true, message: '请输入会议室名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  location: [{ required: true, message: '请选择空间位置', trigger: 'change' }],
  capacity: [{ required: true, message: '请输入容纳人数', trigger: 'blur' }],
  manager: [{ required: true, message: '请输入负责人员', trigger: 'blur' }]
}

// 会议室列表数据
const roomList = ref<MeetingRoom[]>([
  {
    id: '1',
    sequence: 1,
    roomName: '小办会议室',
    location: '1楼会议室',
    capacity: 20,
    manager: '张强',
    contact: '13800138000',
    status: '启用',
    equipment: '投影仪、音响设备、白板',
    remark: '适合小型会议使用'
  },
  {
    id: '2',
    sequence: 2,
    roomName: '大会议室',
    location: '2楼会议室',
    capacity: 15,
    manager: '李木木',
    contact: '13900139000',
    status: '启用',
    equipment: '大屏幕、音响设备、视频会议系统',
    remark: '适合大型会议和培训使用'
  }
])

// 事件处理
const handleSearch = () => {
  pagination.page = 1
  loadData()
  ElMessage.success('搜索完成')
}

const handleReset = () => {
  Object.assign(searchForm, {
    location: '',
    roomName: ''
  })
  handleSearch()
}

const handleViewChange = (mode: string) => {
  viewMode.value = mode
}

const handleCreate = () => {
  dialogMode.value = 'add'
  resetFormData()
  dialogVisible.value = true
}

const handleEdit = (row: MeetingRoom) => {
  dialogMode.value = 'edit'
  Object.assign(formData, {
    roomName: row.roomName,
    status: row.status,
    location: row.location,
    capacity: row.capacity,
    manager: row.manager,
    contact: row.contact,
    equipment: row.equipment || '',
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

const handleView = (row: MeetingRoom) => {
  dialogMode.value = 'view'
  Object.assign(formData, {
    roomName: row.roomName,
    status: row.status,
    location: row.location,
    capacity: row.capacity,
    manager: row.manager,
    contact: row.contact,
    equipment: row.equipment || '',
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

const handleDelete = (row: MeetingRoom) => {
  ElMessageBox.confirm('确定要删除这个会议室吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = roomList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      roomList.value.splice(index, 1)
      // 重新编号
      roomList.value.forEach((item, idx) => {
        item.sequence = idx + 1
      })
      pagination.total = roomList.value.length
      ElMessage.success('删除成功')
    }
  })
}

const handleDropdownCommand = (command: string, row: MeetingRoom) => {
  switch (command) {
    case 'view':
      handleView(row)
      break
    case 'delete':
      handleDelete(row)
      break
  }
}

const handleSubmit = async () => {
  const newRoom: MeetingRoom = {
    id: `room_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    sequence: roomList.value.length + 1,
    roomName: formData.roomName,
    location: formData.location,
    capacity: formData.capacity,
    manager: formData.manager,
    contact: formData.contact,
    status: formData.status,
    equipment: formData.equipment,
    remark: formData.remark
  }

  if (dialogMode.value === 'add') {
    roomList.value.unshift(newRoom)
    ElMessage.success('创建成功')
  } else {
    // 更新现有会议室
    const index = roomList.value.findIndex(item => item.roomName === formData.roomName)
    if (index > -1) {
      Object.assign(roomList.value[index], {
        location: formData.location,
        capacity: formData.capacity,
        manager: formData.manager,
        contact: formData.contact,
        status: formData.status,
        equipment: formData.equipment,
        remark: formData.remark
      })
    }
    ElMessage.success('更新成功')
  }
  
  dialogVisible.value = false
  pagination.total = roomList.value.length
}

const resetFormData = () => {
  Object.assign(formData, {
    roomName: '',
    status: '启用',
    location: '',
    capacity: 0,
    manager: '',
    contact: '',
    equipment: '',
    remark: ''
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
  pagination.total = roomList.value.length
  loadData()
})
</script>

<style scoped lang="scss">
.meeting-room-management {
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
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 20px;
    background: rgba(15, 23, 42, 0.7);
    border-radius: 8px;
    border: 1px solid rgba(0, 212, 255, 0.1);

    :deep(.el-form-item__label) {
      color: #e2e8f0;
    }

    .view-controls {
      :deep(.el-radio-group) {
        .el-radio-button__inner {
          background: rgba(15, 23, 42, 0.7);
          border-color: rgba(0, 212, 255, 0.2);
          color: #e2e8f0;

          &:hover {
            color: #00d4ff;
          }
        }

        .el-radio-button__original-radio:checked + .el-radio-button__inner {
          background: #00d4ff;
          border-color: #00d4ff;
          color: #0f172a;
        }
      }
    }
  }

  .room-section {
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

    .room-cards {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
      gap: 20px;
      margin-bottom: 20px;

      .room-card {
        background: rgba(30, 58, 138, 0.3);
        border: 1px solid rgba(0, 212, 255, 0.2);
        border-radius: 8px;
        padding: 20px;
        transition: all 0.3s ease;

        &:hover {
          border-color: #00d4ff;
          background: rgba(30, 58, 138, 0.5);
          transform: translateY(-2px);
        }

        .card-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;

          .room-info {
            flex: 1;

            h3 {
              margin: 0 0 12px 0;
              color: #00d4ff;
              font-size: 16px;
              font-weight: 600;
            }

            .room-details {
              .detail-item {
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
                  margin-bottom: 0;
                }
              }
            }
          }

          .card-actions {
            display: flex;
            flex-direction: column;
            gap: 8px;
          }
        }
      }
    }

    .room-table {
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
  .room-form {
    :deep(.el-form-item__label) {
      color: #374151;
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






