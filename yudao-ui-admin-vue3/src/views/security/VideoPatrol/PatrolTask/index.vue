<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="dark-theme-page">
    <div class="multi-screen-preview-page">
      <!-- 顶部工具栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <span class="page-title">智慧安防 / 视频巡更 / 巡检任务</span>
          
          <!-- 当前任务提示 -->
          <div class="current-task-badge" v-if="currentTask">
            <el-tag type="primary" size="large" effect="dark">
              <Icon icon="ep:document" style="margin-right: 6px;" />
              {{ currentTask.taskName }}
            </el-tag>
            <el-tag type="info" size="large" style="margin-left: 8px;">
              {{ currentTask.layout }}
            </el-tag>
            <el-tag 
              :type="getTaskStatusType(currentTask.status || 'draft')" 
              size="large" 
              style="margin-left: 8px;"
            >
              {{ getTaskStatusText(currentTask.status || 'draft') }}
            </el-tag>
          </div>
        </div>
        <div class="toolbar-right">
          <el-button 
            type="primary" 
            :disabled="!currentTask"
            @click="handleOpenTaskEditDialog"
          >
            <Icon icon="ep:edit" />
            编辑任务 (SmartPSS 风格)
          </el-button>
          <el-button @click="handleFullScreen">
            <Icon icon="ep:full-screen" />
            全屏
          </el-button>
          <el-button 
            type="success" 
            :disabled="!currentTask"
            @click="handleSaveCurrentTask"
          >
            <Icon icon="ep:document-checked" />
            保存任务
          </el-button>
        </div>
      </div>

      <div class="main-content">
        <!-- 左侧区域面板 -->
        <div class="left-panel">
          <div class="panel-content">
            <el-tabs v-model="activeLeftTab" class="left-tabs">
              <el-tab-pane label="空间" name="space">
                <!-- 未选择任务提示 -->
                <div v-if="!currentTask" class="no-task-hint">
                  <el-alert
                    title="请先选择或创建任务"
                    type="warning"
                    :closable="false"
                    show-icon
                  >
                    <template #default>
                      <p>在配置通道前，请先在"任务"标签页中选择或创建一个任务</p>
                      <el-button 
                        type="primary" 
                        size="small" 
                        style="margin-top: 10px;"
                        @click="activeLeftTab = 'task'"
                      >
                        前往任务列表
                      </el-button>
                    </template>
                  </el-alert>
                </div>
                
                <!-- 搜索框 -->
                <div v-else class="search-box">
                  <el-input
                    v-model="searchKeyword"
                    placeholder="请输入关键字"
                    clearable
                    size="small"
                  >
                    <template #prefix>
                      <Icon icon="ep:search" />
                    </template>
                  </el-input>
                </div>
                
                <!-- 区域树 -->
                <div v-if="currentTask" class="area-tree">
                  <el-tree
                    :data="areaTreeData"
                    :props="treeProps"
                    :expand-on-click-node="false"
                    :lazy="true"
                    :load="loadTreeNode"
                    :accordion="true"
                    node-key="id"
                    ref="areaTreeRef"
                    @node-click="handleAreaNodeClick"
                  >
                    <template #default="{ node, data }">
                      <span 
                        class="tree-node"
                        :class="{ 'draggable-device': data.type === 'device' }"
                        :draggable="data.type === 'device'"
                        @dragstart="handleDeviceDragStart($event, data)"
                        @dragend="handleDeviceDragEnd"
                      >
                        <Icon :icon="getAreaIcon(data.type)" :size="16" />
                        <span class="node-label">{{ node.label }}</span>
                        <span v-if="data.count" class="node-count">{{ data.count }}</span>
                        <span v-if="data.type === 'device'" class="drag-hint">
                          <Icon icon="ep:rank" :size="12" />
                        </span>
                      </span>
                    </template>
                  </el-tree>
                </div>
              </el-tab-pane>
              <el-tab-pane label="NVR" name="nvr">
                <!-- 未选择任务提示 -->
                <div v-if="!currentTask" class="no-task-hint">
                  <el-alert
                    title="请先选择或创建任务"
                    type="warning"
                    :closable="false"
                    show-icon
                  >
                    <template #default>
                      <p>在配置通道前，请先在"任务"标签页中选择或创建一个任务</p>
                      <el-button 
                        type="primary" 
                        size="small" 
                        style="margin-top: 10px;"
                        @click="activeLeftTab = 'task'"
                      >
                        前往任务列表
                      </el-button>
                    </template>
                  </el-alert>
                </div>
                
                <!-- NVR 搜索框 -->
                <div v-else class="search-box" style="display:flex; gap:8px; align-items:center;">
                  <el-input
                    v-model="nvrSearchKeyword"
                    placeholder="搜索 NVR 或通道"
                    clearable
                    size="small"
                    @input="loadNvrList"
                  >
                    <template #prefix>
                      <Icon icon="ep:search" />
                    </template>
                  </el-input>
                  <el-button size="small" @click="loadNvrList">
                    <Icon icon="ep:refresh" />
                    刷新
                  </el-button>
                </div>
                
                <!-- NVR / 通道树 -->
                <div v-if="currentTask" class="area-tree">
                  <el-tree
                    :data="nvrTreeData"
                    :props="nvrTreeProps"
                    :expand-on-click-node="false"
                    :lazy="true"
                    :load="loadNvrTreeNode"
                    node-key="id"
                    ref="nvrTreeRef"
                    @node-click="handleNvrNodeClick"
                  >
                    <template #default="{ node, data }">
                      <span 
                        class="tree-node"
                        :class="{ 'draggable-device': data.type === 'device' }"
                        :draggable="data.type === 'device'"
                        @dragstart="handleDeviceDragStart($event, data)"
                        @dragend="handleDeviceDragEnd"
                      >
                        <!-- NVR 图标和状态 -->
                        <template v-if="data.type === 'nvr'">
                          <Icon :icon="'ep:cpu'" :size="16" />
                          <span class="node-label">{{ node.label }}</span>
                          <el-tag 
                            :type="data.state === 1 ? 'success' : 'info'" 
                            size="small" 
                            style="margin-left: 8px;"
                          >
                            {{ data.state === 1 ? '在线' : '离线' }}
                          </el-tag>
                          <el-button 
                            size="small" 
                            type="text" 
                            @click.stop="refreshNvrChannels(data.nvrId)"
                            style="margin-left: 8px;"
                          >
                            <Icon icon="ep:refresh" :size="12" />
                          </el-button>
                        </template>
                        <!-- 通道图标 -->
                        <template v-else>
                          <Icon :icon="'ep:video-camera'" :size="16" />
                          <span class="node-label">{{ node.label }}</span>
                          <span v-if="data.type === 'device'" class="drag-hint">
                            <Icon icon="ep:rank" :size="12" />
                          </span>
                        </template>
                      </span>
                    </template>
                  </el-tree>
                </div>
              </el-tab-pane>
              
              <!-- 任务标签页 -->
              <el-tab-pane label="任务" name="task">
                <!-- 搜索框 -->
                <div class="search-box">
                  <el-input
                    v-model="taskSearchKeyword"
                    placeholder="搜索任务"
                    clearable
                    size="small"
                  >
                    <template #prefix>
                      <Icon icon="ep:search" />
                    </template>
                  </el-input>
                </div>
                
                <!-- 任务列表 -->
                <div class="task-list">
                  <div
                    v-for="task in filteredTaskList"
                    :key="task.id"
                    class="task-item"
                    :class="{ active: currentTask?.id === task.id, running: task.id === runningTaskId }"
                    @click="handleLoadTask(task.id!)"
                  >
                    <div class="task-info">
                      <!-- 状态图标 -->
                      <Icon 
                        :icon="getTaskStatusIcon(task.status || 'draft')" 
                        :class="`status-${task.status || 'draft'}`"
                        :size="16"
                      />
                      
                      <!-- 任务名称 -->
                      <span class="task-name" :title="task.taskName">{{ task.taskName }}</span>
                      
                      <!-- 运行中标识 -->
                      <el-tag v-if="task.id === runningTaskId" type="success" size="small" effect="dark">
                        运行中
                      </el-tag>
                    </div>
                    
                    <!-- 操作按钮 -->
                    <div class="task-actions" @click.stop>
                      <!-- 试运行按钮 -->
                      <el-button 
                        v-if="task.id !== runningTaskId"
                        link 
                        size="small" 
                        type="success"
                        @click="handleTrialRun(task)"
                        :title="'试运行'"
                      >
                        <Icon icon="ep:video-play" :size="14" />
                      </el-button>
                      <!-- 停止按钮 -->
                      <el-button 
                        v-else
                        link 
                        size="small" 
                        type="warning"
                        @click="handleStopRun"
                        :title="'停止'"
                      >
                        <Icon icon="ep:video-pause" :size="14" />
                      </el-button>
                      
                      <el-button 
                        link 
                        size="small" 
                        @click="handleEditTaskName(task)"
                        :title="'编辑'"
                      >
                        <Icon icon="ep:edit" :size="14" />
                      </el-button>
                      <el-button 
                        link 
                        size="small" 
                        type="danger"
                        @click="handleDeleteTask(task.id!)"
                        :title="'删除'"
                      >
                        <Icon icon="ep:delete" :size="14" />
                      </el-button>
                    </div>
                  </div>
                  
                  <!-- 空状态 -->
                  <div v-if="filteredTaskList.length === 0" class="empty-state">
                    <el-empty description="暂无任务" :image-size="80">
                      <el-button type="primary" @click="handleNewTask">
                        <Icon icon="ep:plus" />
                        创建第一个任务
                      </el-button>
                    </el-empty>
                  </div>
                </div>
                
                <!-- 新建任务按钮 -->
                <div class="task-footer">
                  <el-button 
                    type="primary" 
                    style="width: 100%"
                    @click="handleNewTask"
                  >
                    <Icon icon="ep:plus" />
                    新建任务
                  </el-button>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>

          <!-- 云台控制 -->
          <div class="ptz-control" v-show="selectedCamera && selectedCamera.canPtz && currentNvrId">
            <div class="control-title">
              <h4>云台控制</h4>
              <span v-if="selectedCamera" class="selected-camera">
                当前控制: {{ selectedCamera.name }}
              </span>
            </div>
            
            <div class="control-panel">
              <!-- 方向控制 -->
              <div class="direction-control">
                <div class="direction-pad">
                  <!-- 上 -->
                  <button 
                    class="direction-btn up" 
                    @mousedown="startPtz('up')" 
                    @mouseup="stopPtz"
                    @mouseleave="stopPtz"
                  >
                    <Icon icon="ep:arrow-up" />
                  </button>
                  
                  <!-- 左 -->
                  <button 
                    class="direction-btn left" 
                    @mousedown="startPtz('left')" 
                    @mouseup="stopPtz"
                    @mouseleave="stopPtz"
                  >
                    <Icon icon="ep:arrow-left" />
                  </button>
                  
                  <!-- 中心 -->
                  <button class="direction-btn center" @click="handlePtzHome">
                    <Icon icon="ep:aim" />
                  </button>
                  
                  <!-- 右 -->
                  <button 
                    class="direction-btn right" 
                    @mousedown="startPtz('right')" 
                    @mouseup="stopPtz"
                    @mouseleave="stopPtz"
                  >
                    <Icon icon="ep:arrow-right" />
                  </button>
                  
                  <!-- 下 -->
                  <button 
                    class="direction-btn down" 
                    @mousedown="startPtz('down')" 
                    @mouseup="stopPtz"
                    @mouseleave="stopPtz"
                  >
                    <Icon icon="ep:arrow-down" />
                  </button>
                </div>
              </div>
              
              <!-- 缩放控制 -->
              <div class="zoom-control">
                <div class="zoom-buttons">
                  <button 
                    class="zoom-btn" 
                    @mousedown="startPtz('zoomIn')" 
                    @mouseup="stopPtz"
                    @mouseleave="stopPtz"
                  >
                    <Icon icon="ep:zoom-in" />
                    <span>缩放</span>
                  </button>
                  <button 
                    class="zoom-btn" 
                    @mousedown="startPtz('zoomOut')" 
                    @mouseup="stopPtz"
                    @mouseleave="stopPtz"
                  >
                    <Icon icon="ep:zoom-out" />
                    <span>缩放</span>
                  </button>
                </div>
                
                <div class="focus-buttons">
                  <button 
                    class="focus-btn" 
                    @mousedown="startPtz('focusNear')" 
                    @mouseup="stopPtz"
                    @mouseleave="stopPtz"
                  >
                    <Icon icon="ep:connection" />
                    <span>聚焦</span>
                  </button>
                  <button 
                    class="focus-btn" 
                    @mousedown="startPtz('focusFar')" 
                    @mouseup="stopPtz"
                    @mouseleave="stopPtz"
                  >
                    <Icon icon="ep:connection" />
                    <span>聚焦</span>
                  </button>
                </div>
                
                <div class="iris-buttons">
                  <button 
                    class="iris-btn" 
                    @mousedown="startPtz('irisOpen')" 
                    @mouseup="stopPtz"
                    @mouseleave="stopPtz"
                  >
                    <Icon icon="ep:sunny" />
                    <span>光圈</span>
                  </button>
                  <button 
                    class="iris-btn" 
                    @mousedown="startPtz('irisClose')" 
                    @mouseup="stopPtz"
                    @mouseleave="stopPtz"
                  >
                    <Icon icon="ep:moon" />
                    <span>光圈</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧视频区域 -->
        <div class="right-content">
          <!-- 视频网格 -->
          <div class="video-grid" :class="gridLayoutClass">
            <div
              v-for="(screen, index) in videoScreens"
              :key="index"
              class="video-screen"
              :class="{ 
                active: selectedScreen === index,
                'drag-over': dragOverScreenIndex === index
              }"
              @click="handleScreenClick(index)"
              @drop="handleScreenDrop($event, index)"
              @dragover="handleScreenDragOver($event, index)"
              @dragleave="handleScreenDragLeave(index)"
            >
              <div v-if="screen.camera" class="video-container">
                <!-- 大华播放器区域 -->
                <div class="video-player">
                  <!-- Canvas播放器（大华SDK） -->
                  <canvas 
                    :id="`h5_canvas_${index}`"
                    class="dahua-canvas"
                    :width="400"
                    :height="300"
                    style="width: 100%; height: 100%; object-fit: contain; background: #000;"
                  ></canvas>
                  
                  <!-- Video元素（大华SDK需要） -->
                  <video 
                    :id="`h5_video_${index}`"
                    class="dahua-video"
                    muted autoplay playsinline webkit-playsinline
                    style="display: none; width: 100%; height: 100%; object-fit: contain; background: #000;"
                  ></video>
                  
                  <!-- 加载状态 -->
                  <div 
                    :id="`h5_loading_${index}`"
                    v-show="screen.loading"
                    class="dahua-loading"
                  >
                    <el-icon class="is-loading"><Loading /></el-icon>
                    <span>加载中...</span>
                  </div>
                  
                  <!-- 备用快照显示 -->
                  <img
                    v-if="!screen.isPlaying && screen.camera.snapshot"
                    :src="screen.camera.snapshot"
                    :alt="screen.camera.name"
                    style="width: 100%; height: 100%; object-fit: cover; position: absolute; top: 0; left: 0;"
                  />
                </div>
                
                <!-- 视频信息叠加层 -->
                <div class="video-overlay">
                  <div class="camera-info">
                    <span class="camera-name">{{ screen.camera.name }}</span>
                    <span class="camera-location">{{ screen.camera.location }}</span>
                  </div>
                  <div class="video-controls">
                    <el-button size="small" @click.stop="handleManageCell(index)" title="管理通道">
                      <Icon icon="ep:list" />
                    </el-button>
                    <el-button size="small" @click.stop="handleRecord(screen.camera)">
                      <Icon icon="ep:video-camera" />
                    </el-button>
                    <el-button size="small" @click.stop="handleSnapshot(screen.camera)">
                      <Icon icon="ep:camera" />
                    </el-button>
                    <el-button size="small" @click.stop="handleCloseVideo(index)">
                      <Icon icon="ep:close" />
                    </el-button>
                  </div>
                </div>
                
                <!-- 录制状态指示 -->
                <div v-if="screen.camera.recording" class="recording-indicator">
                  <Icon icon="ep:video-camera" />
                  <span>录制中</span>
                </div>
              </div>
              
              <!-- 空窗口 -->
              <div v-else class="empty-screen" @contextmenu.prevent="handleCellRightClick($event, index)">
                <!-- 有配置通道时显示预览 -->
                <template v-if="cellChannelsData.has(index) && cellChannelsData.get(index)!.length > 0">
                  <div class="channel-preview">
                    <!-- 截图预览 -->
                    <img 
                      v-if="snapshotBlobUrls.get(index)"
                      :src="snapshotBlobUrls.get(index)"
                      :alt="cellChannelsData.get(index)![0].name"
                      class="preview-image"
                      @error="handleSnapshotError($event, index)"
                    />
                    <div v-else class="no-snapshot">
                      <Icon icon="ep:picture" :size="48" />
                      <p>加载中...</p>
                    </div>
                    
                    <!-- 通道信息叠加层 -->
                    <div class="preview-overlay">
                      <div class="channel-name">
                        {{ cellChannelsData.get(index)![0].name }}
                      </div>
                      <div class="channel-count" v-if="cellChannelsData.get(index)!.length > 1">
                        共 {{ cellChannelsData.get(index)!.length }} 个通道
                      </div>
                    </div>
                    
                    <!-- 播放按钮 -->
                    <div class="play-button" @click.stop="handlePlayFirstChannel(index)">
                      <Icon icon="ep:video-play" :size="48" />
                    </div>
                    
                    <!-- 管理按钮 -->
                    <div class="manage-button" @click.stop="handleManageCell(index)">
                      <Icon icon="ep:setting" />
                    </div>
                  </div>
                </template>
                
                <!-- 未配置通道时显示空状态 -->
                <template v-else>
                  <Icon icon="ep:video-pause" :size="48" />
                  <p>窗口 {{ index + 1 }}</p>
                  <p class="tip">拖拽摄像头到此处添加</p>
                  <p class="tip">或右键添加通道</p>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 布局选择 -->
      <div class="layout-selector">
        <div class="layout-options">
          <div
            v-for="layout in layoutOptions"
            :key="layout.type"
            class="layout-option"
            :class="{ active: currentLayout === layout.type }"
            @click="changeLayout(layout.type)"
          >
            <div class="layout-icon" v-html="layout.icon"></div>
            <span>{{ layout.name }}</span>
          </div>
        </div>
      </div>

      <!-- 右键菜单 -->
      <div 
        v-show="contextMenu.visible"
        class="context-menu"
        :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
        @click="contextMenu.visible = false"
      >
        <div class="menu-item" @click="handleAddChannel">
          <Icon icon="ep:plus" />
          <span>添加通道</span>
        </div>
        <div class="menu-item" @click="handleManageChannels">
          <Icon icon="ep:list" />
          <span>管理通道</span>
        </div>
        <div class="menu-item" @click="handleClearCell">
          <Icon icon="ep:delete" />
          <span>清空格子</span>
        </div>
      </div>

      <!-- 通道管理对话框 -->
      <el-dialog
        v-model="channelManageVisible"
        title="管理格子通道"
        width="700px"
      >
        <div class="channel-manage">
          <el-empty v-if="currentCellChannels.length === 0" description="暂无数据" />
          
          <div v-else>
            <el-alert
              title="提示：可以拖拽行来调整通道播放顺序"
              type="info"
              :closable="false"
              show-icon
              style="margin-bottom: 12px;"
            />
            
            <el-table 
              ref="channelTableRef"
              :data="currentCellChannels" 
              style="width: 100%;"
              row-key="channelId"
            >
              <el-table-column label="拖拽" width="60" align="center">
                <template #default>
                  <Icon icon="ep:rank" class="drag-handle" style="cursor: move; font-size: 18px;" />
                </template>
              </el-table-column>
              <el-table-column type="index" label="序号" width="60" />
              <el-table-column prop="channelName" label="通道名称" />
              <el-table-column label="播放时长" width="180">
                <template #default="{ row }">
                  <el-input-number
                    v-model="row.duration"
                    :min="0"
                    :max="300"
                    :step="1"
                    size="small"
                    style="width: 120px;"
                  />
                  <span style="margin-left: 8px;">秒</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="{ $index }">
                  <el-button
                    type="danger"
                    size="small"
                    link
                    @click="handleDeleteChannel($index)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <div v-if="currentCellChannels.length > 0" class="channel-tips">
            <el-alert
              title="提示"
              type="info"
              :closable="false"
              show-icon
            >
              <template #default>
                <p>• 播放时长为0或留空表示持续播放（不轮播）</p>
                <p>• 多个通道将按设置的时长依次轮播</p>
                <p>• 单个通道将持续播放，不受时长限制</p>
              </template>
            </el-alert>
          </div>
        </div>
        
        <template #footer>
          <el-button @click="channelManageVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveChannels">保存</el-button>
        </template>
      </el-dialog>

      <!-- 摄像头列表对话框 -->
      <el-dialog
        v-model="cameraListVisible"
        title="选择摄像头"
        width="800px"
        :before-close="handleCloseCameraList"
      >
        <div class="camera-list">
          <div class="camera-search">
            <el-input
              v-model="cameraSearchKeyword"
              placeholder="搜索摄像头"
              clearable
            >
              <template #prefix>
                <Icon icon="ep:search" />
              </template>
            </el-input>
          </div>
          
          <div class="camera-grid">
            <div
              v-for="camera in filteredCameras"
              :key="camera.id"
              class="camera-item"
              @click="handleSelectCamera(camera)"
            >
              <div class="camera-preview">
                <img :src="camera.snapshot" :alt="camera.name" />
                <div class="camera-overlay">
                  <Icon icon="ep:video-play" :size="24" />
                </div>
              </div>
              <div class="camera-info">
                <div class="camera-name">{{ camera.name }}</div>
                <div class="camera-location">{{ camera.location }}</div>
                <div class="camera-status" :class="camera.status">
                  {{ getStatusText(camera.status) }}
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="handleCloseCameraList">取消</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- SmartPSS 风格任务编辑对话框 -->
      <TaskEditDialog
        v-model="taskEditDialogVisible"
        :task-data="currentTask"
        @save="handleSaveTaskFromDialog"
      />
    </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts" name="PatrolTask">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { DeviceApi } from '@/api/iot/device/device'
import { getBuildingList } from '@/api/iot/spatial/building'
import { getFloorListByBuildingId } from '@/api/iot/spatial/floor'
import { getAreaListByFloorId } from '@/api/iot/spatial/area'
import { getNvrList, getNvrChannels, ptzControl, type PtzControlReq } from '@/api/iot/video/nvr'
import { iotWebSocket } from '@/utils/iotWebSocket'
import { 
  getInspectionTaskList, 
  createInspectionTask, 
  updateInspectionTask, 
  deleteInspectionTask, 
  getInspectionTask 
} from '@/api/iot/videoPatrol'
import type { InspectionTask, InspectionScene, InspectionChannelConfig, InspectionTaskStatus } from '@/api/iot/videoPatrol/types'
import TaskEditDialog from './components/TaskEditDialog.vue'

/** 巡检任务管理 */
defineOptions({ name: 'PatrolTask' })

// 搜索关键字
const searchKeyword = ref('')
const cameraSearchKeyword = ref('')
// 左侧TAB：空间 / NVR / 任务
const activeLeftTab = ref<'space' | 'nvr' | 'task'>('task')
const nvrSearchKeyword = ref('')
const nvrTreeData = ref<any[]>([])
const nvrTreeRef = ref()
const nvrTreeProps = {
  children: 'children',
  label: 'name',
  isLeaf: (data: any) => data.type === 'device'
}

// 右键菜单
const contextMenu = ref({
  visible: false,
  x: 0,
  y: 0,
  cellIndex: -1
})

// 通道管理对话框
const channelManageVisible = ref(false)

// SmartPSS 风格任务编辑对话框
const taskEditDialogVisible = ref(false)

// 当前格子的通道列表
const currentCellChannels = ref<any[]>([])

// 格子通道数据存储（每个格子可以有多个通道）
const cellChannelsData = ref<Map<number, any[]>>(new Map())

// 表格引用
const channelTableRef = ref()

// Sortable 实例
let sortableInstance: any = null

// 解析形如 "192.168.1.200:88" 的主机:端口，默认端口80
const parseHostPort = (addr?: string) => {
  const def = { host: addr || '', port: 80 }
  if (!addr) return def
  // 简单IPv4处理：如果包含冒号，取最后一个冒号后的数字作为端口
  const idx = addr.lastIndexOf(':')
  if (idx > -1 && idx < addr.length - 1) {
    const host = addr.substring(0, idx)
    const p = parseInt(addr.substring(idx + 1), 10)
    if (!Number.isNaN(p)) return { host, port: p }
  }
  return { host: addr, port: 80 }
}

// 加载 NVR 列表
const loadNvrList = async () => {
  try {
    const resp = await getNvrList()
    const arr: any[] = (resp as any)?.data ?? (resp as any) ?? []
    nvrTreeData.value = arr
      .filter((n: any) => !nvrSearchKeyword.value || (n.name || '').includes(nvrSearchKeyword.value))
      .map((n: any) => {
        const parsed = parseHostPort(n.ipAddress)
        return { 
          id: `nvr-${n.id}`, 
          name: n.name || `NVR-${n.id}`, 
          type: 'nvr', 
          nvrId: n.id, 
          ipAddress: parsed.host, 
          httpPort: parsed.port,
          state: n.state  // ✅ 添加状态字段（1=在线，0=离线）
        }
      })
    console.log('[NVR列表] 加载完成:', nvrTreeData.value)
  } catch (e) {
    console.error('[NVR列表] 加载失败:', e)
    nvrTreeData.value = []
  }
}

// 懒加载 NVR 的通道
const loadNvrTreeNode = async (node: any, resolve: Function) => {
  try {
    const data = node.data
    if (data && data.type === 'nvr') {
      const resp = await getNvrChannels(data.nvrId)
      const list: any[] = (resp as any)?.data ?? (resp as any) ?? []
      const children = list
        .filter((ch: any) => !nvrSearchKeyword.value || (ch.name || '').includes(nvrSearchKeyword.value))
        .map((ch: any) => ({
          id: `device-${ch.id}`,
          name: ch.name || `通道${(typeof ch.channelNo === 'number' && ch.channelNo >= 1) ? ch.channelNo : ((ch.channelNo || 0) + 1)}`,
          type: 'device',
          deviceId: ch.id,
          channelNo: ch.channelNo,
          ipAddress: ch.ipAddress,
          nvrId: data.nvrId,
          nvrIp: data.ipAddress,
          nvrPort: data.httpPort || 80,
          ptzSupport: ch.ptzSupport,
          streamUrl: ch.streamUrl,  // ✅ 添加streamUrl
          subStreamUrl: ch.subStreamUrl,  // ✅ 添加subStreamUrl
          snapshotUrl: ch.snapshotUrl,  // ✅ 添加snapshotUrl
          device: {
            id: ch.id,
            deviceName: ch.name,
            state: ch.state,
            ipAddress: ch.ipAddress,
            config: ''
          }
        }))
      resolve(children)
      return
    }
  } catch (e) {}
  resolve([])
}

// 刷新 NVR 通道
const refreshNvrChannels = async (nvrId: number) => {
  try {
    ElMessage.info('正在刷新通道...')
    const resp = await getNvrChannels(nvrId, 1) // refresh=1
    const channels: any[] = (resp as any)?.data ?? (resp as any) ?? []
    
    console.log('[NVR刷新] 获取到通道数据:', channels)
    
    // 详细打印每个通道的信息
    channels.forEach((ch: any, index: number) => {
      console.log(`[通道${index}] ID: ${ch.id}, Name: "${ch.name}", ChannelNo: ${ch.channelNo}`)
    })
    
    ElMessage.success(`刷新完成，共${channels.length || 0}个通道`)
    
    // 转换通道数据格式
    const children = channels
      .filter((ch: any) => !nvrSearchKeyword.value || (ch.name || '').includes(nvrSearchKeyword.value))
      .map((ch: any) => ({
        id: `device-${ch.id}`,
        name: ch.name || `通道${(ch.channelNo || 0) + 1}`,
        type: 'device',
        deviceId: ch.id,
        channelNo: ch.channelNo,
        ipAddress: ch.ipAddress,
        nvrId: nvrId,
        ptzSupport: ch.ptzSupport,
        streamUrl: ch.streamUrl,  // ✅ 添加streamUrl
        subStreamUrl: ch.subStreamUrl,  // ✅ 添加subStreamUrl
        snapshotUrl: ch.snapshotUrl,  // ✅ 添加snapshotUrl
        device: {
          id: ch.id,
          deviceName: ch.name,
          state: ch.state,
          ipAddress: ch.ipAddress,
          config: ''
        }
      }))
    
    const tree = nvrTreeRef.value
    if (tree) {
      const key = `nvr-${nvrId}`
      const nvrNode = tree.getNode(key)
      if (nvrNode) {
        // 先清空旧的子节点，避免累加
        const olds = [...nvrNode.childNodes]
        olds.forEach((cn: any) => tree.remove(cn.data))
        
        // 添加新的子节点
        if (typeof tree.updateKeyChildren === 'function') {
          tree.updateKeyChildren(key, children)
        } else {
          children.forEach((child: any) => tree.append(child, nvrNode))
        }
        
        nvrNode.expanded = true
        console.log('[NVR刷新] 已更新树节点，子节点数:', children.length)
      }
    }
    
    // 刷新通道后，清空右侧所有视频窗口
    stopAllPlayers()
    initVideoScreens()
    ElMessage.info('已清空视频窗口，请重新拖拽通道')
    
  } catch (e: any) {
    console.error('[NVR刷新] 刷新失败:', e)
    ElMessage.error('刷新通道失败: ' + (e.message || '未知错误'))
  }
}

// 选择下一个空闲窗口
const getNextFreeScreen = () => {
  const idx = videoScreens.value.findIndex((s: any) => !s.isPlaying && !s.loading && !s.camera)
  if (idx !== -1) return idx
  const idx2 = videoScreens.value.findIndex((s: any) => !s.isPlaying && !s.loading)
  return idx2 !== -1 ? idx2 : selectedScreen.value || 0
}

// NVR 树节点点击：点击通道配置到选中的格子或下一个空闲格子
const handleNvrNodeClick = async (data: any) => {
  if (!data || data.type !== 'device') return
  
  let screenIndex: number
  
  // 如果有选中的格子，使用选中的格子
  if (selectedScreen.value !== null) {
    screenIndex = selectedScreen.value
    
    // 如果当前格子正在播放，询问是否替换
    if (videoScreens.value[screenIndex]?.isPlaying) {
      try {
        await ElMessageBox.confirm(
          '当前格子正在播放视频，是否替换？',
          '提示',
          {
            confirmButtonText: '替换',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        // 用户确认替换，停止当前播放
        stopPlayer(screenIndex)
      } catch {
        // 用户取消，不执行操作
        return
      }
    }
  } else {
    // 没有选中格子，自动查找下一个空闲格子
    screenIndex = getNextFreeScreen()
  }
  
  const channel = {
    channelNo: data.channelNo,
    name: data.name,
    id: data.deviceId,
    ipAddress: data.ipAddress,
    nvrId: data.nvrId
  }
  // 切换到当前NVR的IP，避免误连到摄像机IP
  if (data.nvrIp) {
    nvrConfig.ip = data.nvrIp
    if (data.nvrPort) { nvrConfig.httpPort = data.nvrPort }
    console.log('[NVR] 已切换到当前NVR IP与端口:', nvrConfig.ip + ':' + (nvrConfig as any).httpPort)
  }
  if (data.nvrId) {
    currentNvrId.value = data.nvrId
  }
  const screen = videoScreens.value[screenIndex]
  if (screen) {
    // ✅ 调试日志
    console.log('[拖拽通道] ptzSupport值:', {
      'data.ptzSupport': data.ptzSupport,
      'channel.ptzSupport': (channel as any).ptzSupport,
      '类型': typeof data.ptzSupport,
      '最终canPtz': data.ptzSupport === true
    })
    
    screen.camera = {
      id: channel.id,
      deviceId: channel.id,
      name: channel.name,
      location: channel.ipAddress ? `IP: ${channel.ipAddress}` : '',
      status: 'online',
      snapshot: channel.snapshotUrl || '',
      streamUrl: channel.streamUrl || '',  // ✅ 使用后端返回的streamUrl
      canPtz: data.ptzSupport === true,
      config: {
        username: nvrConfig.username || 'admin',
        password: nvrConfig.password || 'admin123',
        ip: data.nvrIp || nvrConfig.ip,
        httpPort: data.nvrPort || nvrConfig.httpPort || 80
      },
      ipAddress: channel.ipAddress,
      productId: null,
      channelNo: channel.channelNo,
      nvrId: channel.nvrId
    }
    selectedCamera.value = screen.camera
    
    // ✅ 调试日志
    console.log('[拖拽通道] 设置后的camera:', {
      name: screen.camera.name,
      canPtz: screen.camera.canPtz,
      channelNo: screen.camera.channelNo
    })
  }
  
  // 配置完成后，取消格子选中状态
  selectedScreen.value = null
  
  enqueuePlay(screenIndex, channel)
}

// 入队并处理
const enqueuePlay = (screenIndex: number, channel: any) => {
  playQueue.push({ screenIndex, channel })
  processPlayQueue()
}

const processPlayQueue = async () => {
  if (isStartingPlay) return
  isStartingPlay = true
  try {
    while (playQueue.length > 0) {
      const task = playQueue.shift()!
      try { await startPlayChannel(task.screenIndex, task.channel) } catch {}
      // 较长间隔，避免解码器连续初始化导致内存峰值
      await new Promise(r => setTimeout(r, 1000))
    }
  } finally {
    isStartingPlay = false
  }
}

// 大华SDK初始化（统一从 /static/video/module 加载）
const initDahuaSDK = async (): Promise<boolean> => {
  try {
    console.log('[大华SDK] 开始初始化...')
    if (isSDKLoaded) return true
    if (isLoadingSDK) { return sdkReadyPromise || Promise.resolve(false) }
    if ((window as any).PlayerControl) {
      PlayerControl = (window as any).PlayerControl
      isSDKLoaded = true
      initPlayerInstances()
      console.log('[大华SDK] ✅ 已存在的PlayerControl，直接就绪')
      return true
    }
    isLoadingSDK = true
    
    // 统一前缀为 /static/video/module，与项目 public 目录一致
    const base = '/static/video/module'
    const ver = (window as any).__dh_v || (Date.now().toString())
    ;(window as any).__dh_v = ver
    ;(window as any).Module = (window as any).Module || {}
    ;(window as any).Module.locateFile = function (path: string) {
      return `${base}/${path}?v=${ver}`
    }
    ;(window as any).Module.INITIAL_MEMORY = (window as any).__dh_mem || (384 * 1024 * 1024)
    ;(window as any).Module.TOTAL_MEMORY = (window as any).Module.INITIAL_MEMORY
    await loadScript(`${base}/PlayerControl.js`)
    
    // 检查SDK是否加载成功
    if ((window as any).PlayerControl) {
      PlayerControl = (window as any).PlayerControl
      isSDKLoaded = true
      console.log('[大华SDK] ✅ 初始化成功')
      
      // 初始化播放器实例占位（按需创建）
      initPlayerInstances()
      isLoadingSDK = false
      return true
    } else {
      throw new Error('PlayerControl未找到')
    }
  } catch (error) {
    console.error('[大华SDK] ❌ 初始化失败:', error)
    ElMessage.error('视频播放组件初始化失败')
    isLoadingSDK = false
    return false
  }
}

// 动态加载脚本
const loadScript = (src: string): Promise<void> => {
  return new Promise((resolve, reject) => {
    // 检查是否已经加载
    const base = src.split('?')[0]
    if (document.querySelector(`script[src^="${base}"]`)) {
      resolve()
      return
    }

    const script = document.createElement('script')
    const version = Date.now()
    const withQs = src.includes('?') ? `${src}&v=${version}` : `${src}?v=${version}`
    script.src = withQs
    script.onload = () => resolve()
    script.onerror = () => reject(new Error(`加载脚本失败: ${src}`))
    document.head.appendChild(script)
  })
}

const ensureScreenDisplay = (screenIndex: number, mode: 'video' | 'canvas' | 'auto' = 'video') => {
  const videoEl = document.getElementById(`h5_video_${screenIndex}`) as HTMLVideoElement | null
  const canvasEl = document.getElementById(`h5_canvas_${screenIndex}`) as HTMLCanvasElement | null
  const loadingEl = document.getElementById(`h5_loading_${screenIndex}`) as HTMLElement | null
  if (loadingEl) loadingEl.style.display = 'none'

  let preferVideo = mode === 'video'
  if (mode === 'auto') {
    const logs = (window as any).__dh_decodeLog as Array<{ screenIndex: number; decodeMode: string; ts: number }> | undefined
    const last = Array.isArray(logs) ? [...logs].reverse().find((e) => e.screenIndex === screenIndex) : null
    const videoReady = !!(
      videoEl &&
      videoEl.readyState >= 2 &&
      (((videoEl as any).videoWidth && (videoEl as any).videoWidth > 0) || !videoEl.paused)
    )
    if (last) {
      if (last.decodeMode === 'canvas') {
        preferVideo = false
      } else if (last.decodeMode === 'video') {
        preferVideo = videoReady
      } else {
        preferVideo = videoReady
      }
    } else {
      preferVideo = videoReady
    }
  }

  if (preferVideo) {
    if (videoEl) {
      videoEl.style.display = ''
      videoEl.muted = true
      videoEl.autoplay = true
      videoEl.setAttribute('playsinline', 'true')
      videoEl.setAttribute('webkit-playsinline', 'true')
      if (typeof (videoEl as any).play === 'function' && videoEl.paused) {
        ;(videoEl as any).play().catch(() => {})
      }
    }
    if (canvasEl) {
      canvasEl.style.display = 'none'
    }
  } else {
    if (canvasEl) {
      canvasEl.style.display = ''
    }
    if (videoEl) {
      videoEl.style.display = 'none'
    }
  }
}

// 初始化播放器实例
const initPlayerInstances = () => {
  const screenCount = getScreenCount(currentLayout.value)
  playerInstances = Array.from({ length: screenCount }, () => null)
  decodeModes = Array.from({ length: screenCount }, () => null)
}

const stopPlayer = (screenIndex: number) => {
  const p = playerInstances[screenIndex]
  try { if (p && typeof p.stop === 'function') p.stop() } catch {}
  try { if (p && typeof p.close === 'function') p.close() } catch {}
  playerInstances[screenIndex] = null
  decodeModes[screenIndex] = null
  const videoEl = document.getElementById(`h5_video_${screenIndex}`) as HTMLVideoElement | null
  if (videoEl) {
    try { (videoEl as any).srcObject = null } catch {}
    videoEl.removeAttribute('src')
  }
}

const stopAllPlayers = () => {
  for (let i = 0; i < playerInstances.length; i++) {
    stopPlayer(i)
  }
}

// 播放开始回调
const handlePlayStart = (screenIndex: number) => {
  const screen = videoScreens.value[screenIndex]
  if (screen) {
    screen.isPlaying = true
    screen.loading = false
    console.log(`[播放器] 屏幕${screenIndex + 1}开始播放`)
    // 若 SDK 未抛出 DecodeStart，也强制展示视频，避免 Chrome 黑屏
    ensureScreenDisplay(screenIndex, 'auto')
  }
}

// 播放停止回调
const handlePlayStop = (screenIndex: number) => {
  const screen = videoScreens.value[screenIndex]
  if (screen) {
    screen.isPlaying = false
    screen.loading = false
    console.log(`[播放器] 屏幕${screenIndex + 1}停止播放`)
  }
}

// 播放错误回调
const handlePlayError = (screenIndex: number, error: any) => {
  const screen = videoScreens.value[screenIndex]
  if (screen) {
    screen.loading = false
    screen.isPlaying = false
  }
  console.error(`[播放器] 屏幕${screenIndex + 1}播放错误:`, error)
  ElMessage.error(`屏幕${screenIndex + 1}播放失败: ${error.message || '未知错误'}`)
}

// 开始播放通道（返回是否成功）
const startPlayChannel = async (screenIndex: number, channel: any): Promise<boolean> => {
  if (!isSDKLoaded) {
    // 等待SDK初始化完成，避免竞态
    sdkReadyPromise = sdkReadyPromise || initDahuaSDK()
    await sdkReadyPromise
    if (!isSDKLoaded) { ElMessage.error('播放器未初始化'); return false }
  }

  const screen = videoScreens.value[screenIndex]
  if (screen) {
    screen.loading = true
  }

  try {
    console.log(`[播放器] 开始播放通道: 屏幕${screenIndex + 1}, 通道${channel.channelNo}`)

    // 若已有实例，先关闭
    const old = playerInstances[screenIndex]
    try {
      if (old && typeof old.stop === 'function') old.stop()
      if (old && typeof old.close === 'function') old.close()
    } catch {}

    // ✅ 优先使用通道自带的配置，否则使用全局nvrConfig
    const channelConfig = channel.config || {}
    const ip = channelConfig.ip || channel.ipAddress || nvrConfig.ip
    const httpPort = channelConfig.httpPort || channelConfig.port || (nvrConfig as any).httpPort || (nvrConfig as any).port || 80
    const username = channelConfig.username || nvrConfig.username || 'admin'
    const password = channelConfig.password || nvrConfig.password || 'admin123'
    
    const wsScheme = location.protocol === 'https:' ? 'wss' : 'ws'
    const rawCh = Number(channel.channelNo)
    const chan = (Number.isFinite(rawCh) ? rawCh : 0) + 1
    const subtype = (browserType === 'firefox' || browserType === '360') ? 2 : 1 // 优先低码流以降低内存
    const wsURL = `${wsScheme}://${ip}:${httpPort}/rtspoverwebsocket`
    const rtspURL = `rtsp://${ip}:${httpPort}/cam/realmonitor?channel=${chan}&subtype=${subtype}&proto=Private3`
    console.log('[播放器] 连接参数:', { 
      wsURL, rtspURL, ip, httpPort, wsScheme, rawCh, chan,
      channelName: channel.name || channel.channelName,
      hasConfig: !!channel.config,
      channelData: channel  // ✅ 输出完整通道数据用于调试
    })

    if ((browserType === 'firefox' || browserType === '360') && countActiveCanvasPlayers() >= maxCanvasDecoders) {
      ElMessage.warning('当前浏览器解码能力有限，建议将子码流设置为H.264和更低分辨率以提高并发')
    }

    // 创建实例
    const player = new PlayerControl({
      wsURL,
      rtspURL,
      username,  // ✅ 使用从channel配置中获取的用户名
      password,  // ✅ 使用从channel配置中获取的密码
      isPrivateProtocol: false,
      playback: false,
      realm: (window as any)?.RPC?.realm,
      lessRateCanvas: true,
      h265AccelerationEnabled: !(browserType === 'firefox' || browserType === '360')
    })

    // 绑定事件
    let settle: (v: boolean)=>void
    let decodeFallbackTimer: number | null = null
    const handleDecode = (mode: any) => {
      if (decodeFallbackTimer !== null) {
        window.clearTimeout(decodeFallbackTimer)
        decodeFallbackTimer = null
      }
      const decodeMode = mode === 'canvas' ? 'canvas' : mode === 'video' ? 'video' : mode?.decodeMode || mode?.decodeType
      ;(window as any).__dh_decodeLog = (window as any).__dh_decodeLog || []
      ;(window as any).__dh_decodeLog.push({ screenIndex, decodeMode, ts: Date.now() })
      decodeModes[screenIndex] = decodeMode === 'canvas' ? 'canvas' : 'video'
      ensureScreenDisplay(screenIndex, decodeMode === 'canvas' ? 'canvas' : 'video')
    }
    const started = new Promise<boolean>(resolve => { settle = resolve })
    if (typeof player.on === 'function') {
      player.on('PlayStart', () => {
        handlePlayStart(screenIndex)
        settle!(true)
      })
      player.on('DecodeStart', (e: any) => handleDecode(e?.decodeMode))
      if (typeof player.on === 'function') {
        player.on('DecodeType', (e: any) => handleDecode(e?.decodeType || e))
        player.on('DecodeMode', (e: any) => handleDecode(e?.decodeMode || e))
      }
      player.on('Error', (err: any) => {
        handlePlayError(screenIndex, err)
        try { if (typeof player.stop === 'function') player.stop() } catch {}
        try { if (typeof player.close === 'function') player.close() } catch {}
        if (decodeFallbackTimer !== null) {
          window.clearTimeout(decodeFallbackTimer)
          decodeFallbackTimer = null
        }
        settle!(false)
      })
      player.on('WorkerReady', () => {
        try { player.connect() } catch (e) { console.error(e) }
      })
    }

    // 初始化并连接（等待DOM渲染出 canvas/video）
    await nextTick()
    let canvasEl: any = document.getElementById(`h5_canvas_${screenIndex}`)
    let videoEl: any = document.getElementById(`h5_video_${screenIndex}`)
    if (!canvasEl || !videoEl) {
      // 再等一帧，最多重试3次
      for (let i = 0; i < 3 && (!canvasEl || !videoEl); i++) {
        await new Promise(r => setTimeout(r, 50))
        canvasEl = document.getElementById(`h5_canvas_${screenIndex}`)
        videoEl = document.getElementById(`h5_video_${screenIndex}`)
      }
    }
    if (typeof player.init === 'function') {
      player.init(canvasEl, videoEl)
    }

    decodeFallbackTimer = window.setTimeout(() => {
      console.warn(`[播放器] 屏幕${screenIndex + 1}未收到 DecodeStart/DecodeType，使用兜底显示`)
      ensureScreenDisplay(screenIndex, 'auto')
    }, 600)

    playerInstances[screenIndex] = player
    const ok = await Promise.race([
      started,
      new Promise<boolean>(r => setTimeout(() => r(false), 5000))
    ])
    return ok
  } catch (error) {
    console.error(`[播放器] 播放失败:`, error)
    if (screen) {
      screen.loading = false
    }
    return false
  }
}

onMounted(async () => {
  loadNvrList()
  sdkReadyPromise = initDahuaSDK()
  await sdkReadyPromise
})

// 当前布局
const currentLayout = ref('2x2')

// 选中的屏幕（null 表示未选中任何格子）
const selectedScreen = ref<number | null>(0)

// 选中的摄像头
const selectedCamera = ref<any>(null)
const currentNvrId = ref<number | null>(null)

// 摄像头列表对话框
const cameraListVisible = ref(false)

// 区域树引用
const areaTreeRef = ref()

// PTZ控制定时器
let ptzTimer: any = null
let lastPtzMode: string | null = null

// 将方向映射为 PTZ 向量与模式
const mapDirectionToVector = (direction: string): { pan?: number; tilt?: number; zoom?: number; mode: 'panTilt' | 'zoom' | 'none' } => {
  const speed = 0.2
  switch (direction) {
    case 'up':
      return { pan: 0, tilt: speed, mode: 'panTilt' }
    case 'down':
      return { pan: 0, tilt: -speed, mode: 'panTilt' }
    case 'left':
      return { pan: -speed, tilt: 0, mode: 'panTilt' }
    case 'right':
      return { pan: speed, tilt: 0, mode: 'panTilt' }
    case 'zoomIn':
      return { zoom: speed, mode: 'zoom' }
    case 'zoomOut':
      return { zoom: -speed, mode: 'zoom' }
    default:
      return { mode: 'none' }
  }
}

// 拖拽相关状态
const draggedDevice = ref<any>(null)
const dragOverScreenIndex = ref<number | null>(null)

// 树属性
const treeProps = {
  children: 'children',
  label: 'name',
  isLeaf: (data: any) => {
    // 只有设备节点是叶子节点
    return data.type === 'device'
  }
}

// 区域树数据（从真实API加载）
const areaTreeData = ref<any[]>([])

// 布局选项
// 注意：type 表示行×列，例如 2x2 = 2行×2列 = 4个格子 = 4分屏
const layoutOptions = [
  {
    type: '1x1',
    name: '1分屏',
    icon: '<div style="width:20px;height:20px;border:1px solid #00d4ff;"></div>'
  },
  {
    type: '2x2',
    name: '4分屏 (2×2)',
    icon: '<div style="display:grid;grid-template-columns:1fr 1fr;grid-template-rows:1fr 1fr;gap:1px;width:20px;height:20px;"><div style="border:1px solid #00d4ff;"></div><div style="border:1px solid #00d4ff;"></div><div style="border:1px solid #00d4ff;"></div><div style="border:1px solid #00d4ff;"></div></div>'
  },
  {
    type: '3x3',
    name: '9分屏 (3×3)',
    icon: '<div style="display:grid;grid-template-columns:1fr 1fr 1fr;grid-template-rows:1fr 1fr 1fr;gap:1px;width:20px;height:20px;"><div style="border:1px solid #00d4ff;"></div><div style="border:1px solid #00d4ff;"></div><div style="border:1px solid #00d4ff;"></div><div style="border:1px solid #00d4ff;"></div><div style="border:1px solid #00d4ff;"></div><div style="border:1px solid #00d4ff;"></div><div style="border:1px solid #00d4ff;"></div><div style="border:1px solid #00d4ff;"></div><div style="border:1px solid #00d4ff;"></div></div>'
  },
  {
    type: '4x4',
    name: '16分屏 (4×4)',
    icon: '<div style="display:grid;grid-template-columns:repeat(4,1fr);grid-template-rows:repeat(4,1fr);gap:1px;width:20px;height:20px;background:#00d4ff;"></div>'
  }
]

// 视频屏幕数据
const videoScreens = ref<any[]>([])

// 摄像头数据 - 从IoT模块动态获取真实设备
const cameras = ref<any[]>([])

// 大华SDK相关
let PlayerControl: any = null
let playerInstances: any[] = []
let isSDKLoaded = false
let sdkReadyPromise: Promise<boolean> | null = null
let isLoadingSDK = false
// 播放串行队列，避免并发初始化解码库
let playQueue: Array<{ screenIndex: number, channel: any }> = []
let isStartingPlay = false

let decodeModes: Array<'video' | 'canvas' | null> = []
const detectBrowser = (): 'firefox' | 'edge' | 'chrome' | 'safari' | '360' | 'other' => {
  const ua = navigator.userAgent.toLowerCase()
  if (ua.includes('firefox')) return 'firefox'
  if (ua.includes('edg/')) return 'edge'
  if (ua.includes('qihoobrowser') || ua.includes('360se') || ua.includes('360ee') || ua.includes('360browser')) return '360'
  if (ua.includes('chrome')) return 'chrome'
  if (ua.includes('safari')) return 'safari'
  return 'other'
}
const browserType = detectBrowser()
const getMaxCanvasDecoders = (b: string) => (b === 'firefox' ? 2 : b === '360' ? 1 : 9999)
const maxCanvasDecoders = getMaxCanvasDecoders(browserType)
const countActiveCanvasPlayers = () => decodeModes.filter((m) => m === 'canvas').length

// NVR配置（HTTP端口用于无插件SDK的 WS/RTSP）
const nvrConfig = {
  ip: '192.168.1.200',
  httpPort: 80,
  username: 'admin',
  password: 'admin123'
}

/** 加载空间树（初始只加载建筑列表） */
const loadSpaceTree = async () => {
  try {
    console.log('[空间树] 🔄 开始加载空间树（懒加载模式）...')
    
    // 只加载建筑列表，不加载子节点
    const buildings = await getBuildingList()
    
    console.log('[空间树] ✅ 加载到建筑:', buildings.length, '个')
    
    // 将建筑转换为树节点格式
    const treeData = buildings.map((building: any) => ({
      id: `building-${building.id}`,
      name: building.name,
      type: 'building',
      buildingId: building.id,
      // 不预先加载 children，由 loadTreeNode 按需加载
    }))
    
    areaTreeData.value = treeData
    
    ElMessage.success(`已加载 ${treeData.length} 个建筑，请展开查看楼层`)
    
  } catch (error: any) {
    console.error('[空间树] ❌ 加载失败:', error)
    ElMessage.error('加载空间树失败: ' + error.message)
    areaTreeData.value = []
  }
}

/**
 * 懒加载树节点（按需加载子节点）
 * @param node 当前节点
 * @param resolve 回调函数
 */
const loadTreeNode = async (node: any, resolve: Function) => {
  try {
    const data = node.data
    let children: any[] = []

    console.log('[空间树] 🔄 懒加载节点:', data.type, data.name)

    // 根据节点类型加载不同的子节点
    if (data.type === 'building') {
      // 建筑节点 -> 加载楼层列表
      const floors = await getFloorListByBuildingId(data.buildingId)
      console.log('[空间树] ✅ 建筑', data.name, '有', floors.length, '个楼层')
      
      children = floors.map((floor: any) => ({
        id: `floor-${floor.id}`,
        name: floor.name,
        type: 'floor',
        floorId: floor.id,
        buildingId: data.buildingId
      }))

    } else if (data.type === 'floor') {
      // 楼层节点 -> 加载区域列表
      const areas = await getAreaListByFloorId(data.floorId)
      console.log('[空间树] ✅ 楼层', data.name, '有', areas.length, '个区域')
      
      children = areas.map((area: any) => ({
        id: `area-${area.id}`,
        name: area.name,
        type: 'area',
        areaId: area.id,
        floorId: data.floorId,
        buildingId: data.buildingId
      }))

    } else if (data.type === 'area') {
      // 区域节点 -> 加载设备列表（使用areaId/roomId过滤）
      const devicesRes = await DeviceApi.getDevicePage({
        floorId: data.floorId,
        areaId: data.areaId,  // 后端会映射到roomId字段
        pageNo: 1,
        pageSize: 100
      })
      
      const allDevices = devicesRes.list || []
      
      // 过滤摄像头设备（基于设备能力判断，不硬编码）
      // 注意：设备表中使用roomId字段存储区域ID
      const cameraDevices = allDevices.filter((device: any) => {
        const belongsToArea = device.roomId === data.areaId  // 注意：这里是roomId
        const isCamera = isCameraDevice(device)  // 使用统一的判断函数
        const isOnline = device.state === 1
        return belongsToArea && isCamera && isOnline
      })
      
      console.log('[空间树] ✅ 区域', data.name, '有', cameraDevices.length, '个在线摄像头')
      
      children = cameraDevices.map((device: any) => ({
        id: `device-${device.id}`,
        name: device.deviceName,
        type: 'device',
        deviceId: device.id,
        device: device
      }))
    }

    resolve(children)

  } catch (error: any) {
    console.error('[空间树] ❌ 懒加载节点失败:', error)
    ElMessage.error('加载子节点失败: ' + error.message)
    resolve([]) // 失败时返回空数组
  }
}

/**
 * 判断设备是否为摄像机（基于设备能力，不硬编码产品ID）
 * @param device 设备对象
 * @returns 是否为摄像机
 */
const isCameraDevice = (device: any): boolean => {
  try {
    // 解析设备配置
    const config = device.config ? JSON.parse(device.config) : {}
    
    // 判断标准：具有以下摄像机特征之一即可
    // 1. 有RTSP端口配置（视频流协议）
    const hasRtspPort = config.rtspPort !== undefined && config.rtspPort !== null
    
    // 2. 支持ONVIF协议（网络视频设备标准协议）
    const hasOnvifSupport = config.onvifSupported === true || config.onvifPort !== undefined
    
    // 3. 有快照接口配置
    const hasSnapshotConfig = config.snapshot !== undefined && config.snapshot !== null
    
    // 4. 有视频厂商标识（hikvision、dahua、uniview等）
    const hasVendor = config.vendor !== undefined && config.vendor !== null
    
    // 满足任一条件即判定为摄像机
    return hasRtspPort || hasOnvifSupport || hasSnapshotConfig || hasVendor
    
  } catch (error) {
    console.warn(`⚠️ 设备配置解析失败 [${device.deviceName}]:`, error)
    return false
  }
}

// 从IoT模块加载真实的摄像机设备
const loadCamerasFromIoT = async () => {
  try {
    console.log('🔄 开始从IoT模块加载摄像机设备...')
    console.log('📍 当前选中的空间节点:', selectedSpaceNode.value)
    
    // 构建查询参数（根据选中的空间节点过滤）
    const queryParams: any = {
      pageNo: 1,
      pageSize: 100,
      productId: null
    }
    
    // 根据选中的空间节点添加过滤条件
    if (selectedSpaceNode.value) {
      const node = selectedSpaceNode.value
      if (node.type === 'building' && node.buildingId) {
        // 选中建筑：使用buildingId过滤
        queryParams.buildingId = node.buildingId
        console.log('🏢 过滤建筑:', node.name, 'buildingId:', node.buildingId)
      } else if (node.type === 'floor' && node.floorId) {
        // 选中楼层：使用floorId过滤
        queryParams.floorId = node.floorId
        console.log('🏢 过滤楼层:', node.name, 'floorId:', node.floorId)
      } else if (node.type === 'area' && node.areaId) {
        // 选中区域：同时使用floorId和areaId过滤（后端会将areaId映射到roomId字段）
        queryParams.floorId = node.floorId
        queryParams.areaId = node.areaId
        console.log('📍 过滤区域:', node.name, 'floorId:', node.floorId, 'areaId:', node.areaId)
      }
    } else {
      console.log('🌐 未选中空间节点，显示所有设备')
    }
    
    // 获取IoT设备
    const deviceRes = await DeviceApi.getDevicePage(queryParams)
    
    console.log('📡 IoT设备API响应:', deviceRes)
    
    if (!deviceRes || !deviceRes.list) {
      throw new Error('IoT设备API返回数据格式错误')
    }
    
    const allDevices = deviceRes.list
    console.log('📋 获取到所有IoT设备:', allDevices.length, '个', allDevices)
    
    // 过滤摄像机设备（使用统一的判断函数）
    let cameraDevices = allDevices.filter((device: any) => {
      const isCamera = isCameraDevice(device)
      const isOnline = device.state === 1
      
      if (isCamera) {
        console.log(`✅ 摄像机 [${device.deviceName}]: 在线=${isOnline}`)
      }
      
      return isCamera && isOnline
    })
    
    // 后端API已经支持buildingId/floorId/areaId过滤，这里不需要额外的前端过滤
    // 只在控制台输出过滤结果
    if (selectedSpaceNode.value) {
      const node = selectedSpaceNode.value
      const typeText = node.type === 'building' ? '建筑' : node.type === 'floor' ? '楼层' : '区域'
      console.log(`✅ ${typeText}过滤完成: ${node.name}, 剩余 ${cameraDevices.length} 个摄像头`)
    }
    
    console.log('🎯 过滤后的摄像机设备:', cameraDevices.length, '个', cameraDevices)
    
    // 转换为前端需要的格式
    const transformedCameras = cameraDevices.map((device: any) => {
      const config = device.config ? JSON.parse(device.config) : {}
      const streamUrl = generateRTSPUrl(config)
      
      const transformedDevice = {
        id: device.id,
        deviceId: device.id,  // ✅ 添加 deviceId 字段用于保存配置
        name: device.deviceName,
        location: device.address || `IP: ${device.ipAddress}` || '未知位置',
        status: device.state === 1 ? 'online' : 'offline',
        snapshot: generateCameraSnapshot(device.deviceName),
        streamUrl: streamUrl,
        canPtz: true, // 海康和大华摄像机都支持云台控制
        config: config,
        ipAddress: device.ipAddress,
        productId: device.productId
      }
      
      console.log('🔄 转换设备:', device.deviceName, '->', transformedDevice)
      return transformedDevice
    })
    
    cameras.value = transformedCameras
    
    console.log('✅ 成功加载真实IoT摄像机设备:', cameras.value.length, '个')
    console.log('📸 摄像机列表:', cameras.value)
    
    // 如果找到了真实设备，显示成功消息
    if (cameras.value.length > 0) {
      ElMessage.success(`已加载 ${cameras.value.length} 个真实摄像机设备`)
    } else {
      console.warn('⚠️ 没有找到在线的摄像机设备')
      ElMessage.warning('没有找到在线的摄像机设备，请检查IoT模块中的设备状态')
      
      // 添加提示信息
      cameras.value = [
        {
          id: 'no-devices',
          name: '未找到在线摄像机设备',
          location: '请在智慧物联模块中检查设备状态',
          status: 'offline',
          snapshot: generateCameraSnapshot('未找到设备'),
          streamUrl: '#',
          canPtz: false
        }
      ]
    }
    
  } catch (error: any) {
    console.error('❌ 加载IoT摄像机设备失败:', error)
    ElMessage.error(`加载摄像机设备失败: ${error.message || error}`)
    
    // 失败时使用错误提示
    cameras.value = [
      {
        id: 'error-1',
        name: '加载失败 - API连接错误',
        location: '请检查IoT模块和网络连接',
        status: 'offline',
        snapshot: generateCameraSnapshot('连接错误'),
        streamUrl: '#',
        canPtz: false
      }
    ]
  }
}

// 生成RTSP流地址
const generateRTSPUrl = (config: any) => {
  if (!config.ip || !config.username || !config.password) {
    return '#'
  }
  
  // 根据不同品牌生成RTSP地址
  const { ip, username, password } = config
  
  // 海康威视RTSP地址格式
  if (config.brand === 'hikvision' || ip === '192.168.1.201') {
    return `rtsp://${username}:${password}@${ip}:554/Streaming/Channels/101`
  }
  
  // 大华RTSP地址格式  
  if (config.brand === 'dahua' || ip === '192.168.1.202') {
    return `rtsp://${username}:${password}@${ip}:554/cam/realmonitor?channel=1&subtype=0`
  }
  
  // 通用RTSP地址
  return `rtsp://${username}:${password}@${ip}:554/stream1`
}

// 生成摄像机缩略图
const generateCameraSnapshot = (name: string) => {
  const svg = `<svg width="300" height="200" viewBox="0 0 300 200" fill="none" xmlns="http://www.w3.org/2000/svg">
    <rect width="300" height="200" fill="#333"/>
    <text x="150" y="100" fill="#fff" text-anchor="middle" dy=".3em">${name}</text>
  </svg>`
  return 'data:image/svg+xml;base64,' + btoa(unescape(encodeURIComponent(svg)))
}

// 计算网格布局类名
const gridLayoutClass = computed(() => {
  switch (currentLayout.value) {
    case '1x1':
      return 'grid-1x1'
    case '2x2':
      return 'grid-2x2'
    case '3x3':
      return 'grid-3x3'
    case '4x4':
      return 'grid-4x4'
    default:
      return 'grid-2x2'
  }
})

// 过滤后的摄像头
const filteredCameras = computed(() => {
  if (!cameraSearchKeyword.value) {
    return cameras.value
  }
  return cameras.value.filter(camera =>
    camera.name.toLowerCase().includes(cameraSearchKeyword.value.toLowerCase()) ||
    camera.location.toLowerCase().includes(cameraSearchKeyword.value.toLowerCase())
  )
})

// 初始化视频屏幕
const initVideoScreens = () => {
  const screenCount = getScreenCount(currentLayout.value)
  videoScreens.value = Array.from({ length: screenCount }, () => ({
    camera: null,
    isPlaying: false,
    loading: false
  }))
}

// 获取屏幕数量
const getScreenCount = (layout: string) => {
  switch (layout) {
    case '1x1':
      return 1
    case '2x2':
      return 4
    case '3x3':
      return 9
    case '4x4':
      return 16
    default:
      return 4
  }
}

// 获取区域图标
const getAreaIcon = (type: string) => {
  switch (type) {
    case 'building':
      return 'ep:office-building'
    case 'floor':
      return 'ep:grid'
    case 'area':
      return 'ep:location'
    case 'device':
      return 'ep:video-camera'
    case 'zone':
      return 'ep:grid'
    default:
      return 'ep:folder'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'online':
      return '在线'
    case 'offline':
      return '离线'
    default:
      return '未知'
  }
}

// 处理区域节点点击
// ========== 拖拽相关函数 ==========

// 开始拖拽设备
const handleDeviceDragStart = (event: DragEvent, data: any) => {
  if (data.type !== 'device' || !data.device) {
    event.preventDefault()
    return
  }
  
  // 检查设备是否在线
  if (data.device.state !== 1) {
    ElMessage.warning('设备离线，无法拖拽')
    event.preventDefault()
    return
  }
  
  draggedDevice.value = data
  console.log('[拖拽] 开始拖拽设备:', data.name)
  
  // 设置拖拽效果
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'copy'
    event.dataTransfer.setData('text/plain', data.device.id)
  }
}

// 结束拖拽
const handleDeviceDragEnd = () => {
  draggedDevice.value = null
  dragOverScreenIndex.value = null
  console.log('[拖拽] 拖拽结束')
}

// 拖拽进入分屏格子
const handleScreenDragOver = (event: DragEvent, screenIndex: number) => {
  event.preventDefault()
  
  if (draggedDevice.value) {
    dragOverScreenIndex.value = screenIndex
    
    if (event.dataTransfer) {
      event.dataTransfer.dropEffect = 'copy'
    }
  }
}

// 拖拽离开分屏格子
const handleScreenDragLeave = (screenIndex: number) => {
  if (dragOverScreenIndex.value === screenIndex) {
    dragOverScreenIndex.value = null
  }
}

// 放置到分屏格子
const handleScreenDrop = (event: DragEvent, screenIndex: number) => {
  event.preventDefault()
  dragOverScreenIndex.value = null
  
  if (!draggedDevice.value || draggedDevice.value.type !== 'device') {
    return
  }
  
  const data = draggedDevice.value
  const screen = videoScreens.value[screenIndex]
  
  // 检查设备是否在线
  if (data.device.state !== 1) {
    ElMessage.warning('设备离线，无法播放')
    draggedDevice.value = null
    return
  }
  
  // 转换设备数据
  const config = data.device.config ? JSON.parse(data.device.config) : {}
  const streamUrl = generateRTSPUrl(config)
  
  console.log('[拖拽] 设备完整信息:', data)
  console.log('[拖拽] 设备类型:', data.device.productId)
  console.log('[拖拽] 父节点信息:', data.parent)
  
  // 判断是否是 NVR 通道
  // 如果设备有 channelNo 属性，说明是 NVR 的通道
  let isNvrChannel = false
  let channelNo = config.channelNo
  
  // 检查设备是否有 channelNo 属性（可能在 data 或 device 中）
  if (data.channelNo !== undefined) {
    isNvrChannel = true
    channelNo = data.channelNo
    console.log('[拖拽] 从 data.channelNo 获取:', channelNo)
  } else if (data.device.channelNo !== undefined) {
    isNvrChannel = true
    channelNo = data.device.channelNo
    console.log('[拖拽] 从 device.channelNo 获取:', channelNo)
  } else if (config.channelNo !== undefined) {
    isNvrChannel = true
    console.log('[拖拽] 从 config.channelNo 获取:', channelNo)
  }
  
  // 如果有 nvrId，也说明是 NVR 通道
  if (data.nvrId || data.device.nvrId) {
    isNvrChannel = true
    console.log('[拖拽] 检测到 nvrId:', data.nvrId || data.device.nvrId)
  }
  
  const camera = {
    id: data.device.id,
    deviceId: data.device.id,
    channelId: data.device.id,
    channelName: data.device.deviceName,
    name: data.device.deviceName,
    location: data.device.address || `IP: ${data.device.ipAddress}` || '未知位置',
    status: 'online',
    snapshot: generateCameraSnapshot(data.device.deviceName),
    streamUrl: streamUrl,
    canPtz: true,
    config: { ...config, channelNo: channelNo },  // 确保 channelNo 在 config 中
    ipAddress: data.device.ipAddress,
    productId: data.device.productId,
    duration: 10,  // 默认10秒
    nvrId: data.nvrId || data.device.nvrId,  // 保存 nvrId
    channelNo: channelNo  // 直接保存 channelNo
  }
  
  console.log('[拖拽] 是否是 NVR 通道:', isNvrChannel)
  console.log('[拖拽] 最终 channelNo:', channelNo)
  
  // 获取当前格子的通道列表
  let channels = cellChannelsData.value.get(screenIndex) || []
  
  // 检查是否已存在该通道
  const existingIndex = channels.findIndex(ch => ch.channelId === camera.channelId)
  if (existingIndex >= 0) {
    ElMessage.warning(`通道 ${camera.name} 已存在于窗口 ${screenIndex + 1}`)
    draggedDevice.value = null
    return
  }
  
  // 添加到通道列表
  channels.push(camera)
  
  // 更新时长规则：单通道时长为0，多通道时长为10
  if (channels.length === 1) {
    channels[0].duration = 0  // 单通道持续播放
  } else if (channels.length === 2) {
    // 从单通道变为多通道，第一个通道改为10秒
    channels[0].duration = 10
    channels[1].duration = 10
  } else {
    // 已有多通道，新通道默认10秒
    camera.duration = 10
  }
  
  // 保存通道列表
  cellChannelsData.value.set(screenIndex, channels)
  
  // 切换播放最新添加的通道
  screen.camera = camera
  screen.isPlaying = false  // 先设为false，等播放成功后会自动设为true
  screen.loading = true     // 显示加载状态
  selectedScreen.value = screenIndex
  selectedCamera.value = camera
  
  console.log('[拖拽] 通道已添加到窗口', screenIndex + 1, camera)
  console.log('[拖拽] 当前通道列表', channels)
  
  // 启动播放（如果是NVR通道）
  if (channelNo !== undefined) {
    console.log('[拖拽] ✅ 检测到 NVR 通道，准备播放...')
    console.log('[拖拽] channelNo:', channelNo)
    console.log('[拖拽] camera.config:', camera.config)
    // NVR通道，使用大华播放器
    startPlayChannel(screenIndex, {
      channelNo: channelNo,
      name: camera.name
    })
  } else {
    // 非NVR通道，暂不支持自动播放
    screen.loading = false
    console.warn('[拖拽] ❌ 非NVR通道，需要手动点击播放')
    console.warn('[拖拽] 这可能是普通摄像头，不是 NVR 的通道')
    console.warn('[拖拽] 请确认设备树中拖拽的是 NVR 下的通道，而不是独立的摄像头设备')
  }
  
  ElMessage.success(`已添加 ${camera.name} 到窗口 ${screenIndex + 1}（共 ${channels.length} 个通道）`)
  
  // 自动保存布局配置
  saveLayoutConfig()
  
  draggedDevice.value = null
}

// ========== 空间树节点点击 ==========

// 当前选中的空间节点（用于过滤设备列表）
const selectedSpaceNode = ref<any>(null)

// 处理空间树节点点击
const handleAreaNodeClick = (data: any) => {
  console.log('[空间树] 点击节点:', data)
  
  // 如果点击的是设备节点，尝试将其添加到当前选中的分屏格子
  if (data.type === 'device' && data.device) {
    const screenIndex = selectedScreen.value !== null ? selectedScreen.value : 0
    const screen = videoScreens.value[screenIndex]
    
    if (screen.camera) {
      ElMessage.warning('当前格子已有视频，请先清除或选择其他格子')
      return
    }
    
    // 检查设备是否在线
    if (data.device.state !== 1) {
      ElMessage.warning('设备离线，无法播放')
      return
    }
    
    // 转换设备数据并添加到格子
    const config = data.device.config ? JSON.parse(data.device.config) : {}
    const streamUrl = generateRTSPUrl(config)
    
    const camera = {
      id: data.device.id,
      deviceId: data.device.id,  // ✅ 添加 deviceId 字段用于保存配置
      name: data.device.deviceName,
      location: data.device.address || `IP: ${data.device.ipAddress}` || '未知位置',
      status: 'online',
      snapshot: generateCameraSnapshot(data.device.deviceName),
      streamUrl: streamUrl,
      canPtz: true,
      config: config,
      ipAddress: data.device.ipAddress,
      productId: data.device.productId
    }
    
    screen.camera = camera
    selectedCamera.value = camera
    
    // ✅ 自动保存布局配置
    saveLayoutConfig()
    
    ElMessage.success(`已添加设备：${camera.name}`)
  } else {
    // 点击建筑、楼层或区域节点，更新选中节点并过滤设备列表
    selectedSpaceNode.value = data
    
    // 重新加载并过滤设备列表
    loadCamerasFromIoT()
    
    const typeText = data.type === 'building' ? '建筑' : data.type === 'floor' ? '楼层' : '区域'
    const countText = data.count ? `（${data.count} 个摄像头）` : ''
    ElMessage.info(`选中${typeText}: ${data.name} ${countText}`)
  }
}

// 处理屏幕点击
const handleScreenClick = (index: number) => {
  selectedScreen.value = index
  const screen = videoScreens.value[index]
  
  // 只选中格子，不弹出对话框
  if (screen.camera) {
    selectedCamera.value = screen.camera
    // 根据摄像头来源决定是否开启 NVR PTZ 上下文
    if (typeof screen.camera.channelNo === 'number' && screen.camera.nvrId) {
      currentNvrId.value = screen.camera.nvrId
    } else {
      currentNvrId.value = null
    }
  } else {
    selectedCamera.value = null
    currentNvrId.value = null
  }
  
  // 提示用户操作
  if (!screen.camera) {
    ElMessage.info('已选中格子 ' + (index + 1) + '，请拖拽左侧通道到此处')
  }
}

// 右键菜单处理（只在空格子上显示）
const handleCellRightClick = (event: MouseEvent, cellIndex: number) => {
  contextMenu.value = {
    visible: true,
    x: event.clientX,
    y: event.clientY,
    cellIndex: cellIndex
  }
  
  // 点击其他地方关闭菜单
  const closeMenu = () => {
    contextMenu.value.visible = false
    document.removeEventListener('click', closeMenu)
  }
  setTimeout(() => {
    document.addEventListener('click', closeMenu)
  }, 100)
}

// 管理格子通道（从视频控制按钮调用）
const handleManageCell = (cellIndex: number) => {
  contextMenu.value.cellIndex = cellIndex
  handleManageChannels()
}

// 播放第一个通道
const handlePlayFirstChannel = (cellIndex: number) => {
  const channels = cellChannelsData.value.get(cellIndex)
  if (!channels || channels.length === 0) {
    ElMessage.warning('该格子没有配置通道')
    return
  }
  
  const firstChannel = channels[0]
  console.log('[播放第一个通道] 格子:', cellIndex, '通道:', firstChannel.name)
  
  // 先设置camera对象，这样DOM元素才会被渲染
  const screen = videoScreens.value[cellIndex]
  if (screen) {
    screen.camera = {
      id: firstChannel.id,
      deviceId: firstChannel.deviceId,
      name: firstChannel.name,
      location: firstChannel.location || '',
      status: 'online',
      snapshot: firstChannel.snapshot || '',
      streamUrl: firstChannel.streamUrl || '',
      canPtz: firstChannel.canPtz || false,
      config: firstChannel.config,
      ipAddress: firstChannel.ipAddress,
      productId: firstChannel.productId,
      channelNo: firstChannel.channelNo,
      nvrId: firstChannel.nvrId
    }
    selectedCamera.value = screen.camera
  }
  
  // 使用nextTick确保DOM已更新后再播放
  nextTick(() => {
    enqueuePlay(cellIndex, firstChannel)
  })
}

// 截图URL缓存（避免重复请求）- 使用响应式Map
const snapshotBlobUrls = ref<Map<number, string>>(new Map())

// 加载格子的截图
const loadCellSnapshot = async (cellIndex: number) => {
  const url = await getChannelSnapshotUrl(cellIndex)
  if (url) {
    // 触发响应式更新
    snapshotBlobUrls.value = new Map(snapshotBlobUrls.value)
  }
}

// 获取通道截图URL（异步加载）
const getChannelSnapshotUrl = async (cellIndex: number) => {
  const channels = cellChannelsData.value.get(cellIndex)
  if (!channels || channels.length === 0) {
    console.log(`[截图URL] 格子${cellIndex}: 没有通道数据`)
    return ''
  }
  
  const firstChannel = channels[0]
  console.log(`[截图URL] 格子${cellIndex}:`, {
    name: firstChannel.name,
    snapshot: firstChannel.snapshot,
    nvrId: firstChannel.nvrId,
    channelNo: firstChannel.channelNo,
    ipAddress: firstChannel.ipAddress,
    config: firstChannel.config
  })
  
  // 如果已有缓存的Blob URL，直接返回
  if (snapshotBlobUrls.value.has(cellIndex)) {
    console.log(`[截图URL] 格子${cellIndex}: 使用缓存的Blob URL`)
    return snapshotBlobUrls.value.get(cellIndex)!
  }
  
  // 优先使用snapshot字段（但排除SVG占位图）
  if (firstChannel.snapshot && !firstChannel.snapshot.startsWith('data:image/svg+xml')) {
    console.log(`[截图URL] 格子${cellIndex}: 使用snapshot字段:`, firstChannel.snapshot)
    return firstChannel.snapshot
  }
  
  // 从config中提取认证信息，优先使用NVR配置
  let username = nvrConfig.username || 'admin'
  let password = nvrConfig.password || 'admin123'
  
  if (firstChannel.config) {
    console.log(`[截图URL] 格子${cellIndex}: config原始值:`, firstChannel.config)
    
    try {
      const config = typeof firstChannel.config === 'string' 
        ? JSON.parse(firstChannel.config) 
        : firstChannel.config
      
      console.log(`[截图URL] 格子${cellIndex}: config解析后:`, config)
      
      // 如果config中有认证信息，优先使用config中的
      if (config.username) username = config.username
      if (config.password) password = config.password
      
      console.log(`[截图URL] 格子${cellIndex}: 使用认证信息:`, { username, password: password ? '***' : '(empty)', source: config.username ? 'config' : 'nvr' })
    } catch (e) {
      console.error(`[截图URL] 格子${cellIndex}: config解析失败:`, e)
    }
  } else {
    console.log(`[截图URL] 格子${cellIndex}: config为空，使用NVR配置`)
  }
  
  // 使用后端代理接口获取截图（解决CORS跨域问题）
  if (firstChannel.nvrId && firstChannel.channelNo !== undefined) {
    // 调用后端代理接口（使用/admin-api前缀，会被vite代理到后端）
    const snapshotUrl = `/admin-api/iot/video/nvr/${firstChannel.nvrId}/snapshot?channel=${firstChannel.channelNo}`
    console.log(`[截图URL] 格子${cellIndex}: 使用后端代理:`, snapshotUrl)
    
    // 缓存URL（后端代理接口可以直接用于img标签）
    snapshotBlobUrls.value.set(cellIndex, snapshotUrl)
    
    console.log(`[截图URL] 格子${cellIndex}: ✅ 截图URL已设置`)
    return snapshotUrl
  }
  
  console.warn(`[截图URL] 格子${cellIndex}: 缺少nvrId或channelNo`)
  return ''
}

// 截图加载失败处理
const handleSnapshotError = (event: Event, cellIndex: number) => {
  console.warn(`[截图加载失败] 格子${cellIndex}`)
  // 可以设置一个默认图片或者隐藏图片
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
}

// 添加通道
const handleAddChannel = () => {
  const cellIndex = contextMenu.value.cellIndex
  if (cellIndex < 0) return
  
  // 打开摄像头选择对话框
  cameraListVisible.value = true
  contextMenu.value.visible = false
}

// 管理通道
const handleManageChannels = () => {
  const cellIndex = contextMenu.value.cellIndex
  if (cellIndex < 0) return
  
  // 加载当前格子的通道列表
  const channels = cellChannelsData.value.get(cellIndex) || []
  currentCellChannels.value = JSON.parse(JSON.stringify(channels))
  
  console.log('[管理通道] 格子索引:', cellIndex)
  console.log('[管理通道] 通道数据:', channels)
  console.log('[管理通道] cellChannelsData 全部:', Array.from(cellChannelsData.value.entries()))
  
  channelManageVisible.value = true
  contextMenu.value.visible = false
  
  // 初始化拖拽排序
  nextTick(() => {
    initTableDragSort()
  })
}

// 初始化表格拖拽排序
const initTableDragSort = () => {
  if (!channelTableRef.value) return
  
  // 销毁旧实例
  if (sortableInstance) {
    sortableInstance.destroy()
    sortableInstance = null
  }
  
  // 获取表格的 tbody 元素
  const tbody = channelTableRef.value.$el.querySelector('.el-table__body-wrapper tbody')
  if (!tbody) return
  
  // 动态导入 Sortable
  import('sortablejs').then((module) => {
    const Sortable = module.default
    
    sortableInstance = Sortable.create(tbody, {
      handle: '.drag-handle',  // 只能通过拖拽图标拖动
      animation: 150,
      onEnd: (evt: any) => {
        const { oldIndex, newIndex } = evt
        if (oldIndex !== newIndex && oldIndex !== undefined && newIndex !== undefined) {
          // 更新数组顺序
          const movedItem = currentCellChannels.value.splice(oldIndex, 1)[0]
          currentCellChannels.value.splice(newIndex, 0, movedItem)
          
          console.log('[拖拽排序] 从', oldIndex, '移动到', newIndex)
          ElMessage.success('已调整顺序')
        }
      }
    })
  }).catch((err) => {
    console.error('[拖拽排序] 加载 Sortable 失败:', err)
  })
}

// 清空格子
const handleClearCell = async () => {
  const cellIndex = contextMenu.value.cellIndex
  if (cellIndex < 0) return
  
  try {
    await ElMessageBox.confirm(
      '确认清空该格子的所有通道吗？',
      '清空确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 清空通道数据
    cellChannelsData.value.delete(cellIndex)
    
    // 停止播放
    const screen = videoScreens.value[cellIndex]
    if (screen) {
      stopPlayer(cellIndex)
      screen.camera = null
      screen.isPlaying = false
    }
    
    ElMessage.success('清空成功')
  } catch (error) {
    // 用户取消
  }
  
  contextMenu.value.visible = false
}

// 删除通道
const handleDeleteChannel = (index: number) => {
  currentCellChannels.value.splice(index, 1)
  
  // 更新时长规则：删除后如果只剩1个通道，时长改为0
  if (currentCellChannels.value.length === 1) {
    currentCellChannels.value[0].duration = 0
  }
}

// 保存通道配置
const handleSaveChannels = () => {
  const cellIndex = contextMenu.value.cellIndex
  if (cellIndex < 0) return
  
  // 验证数据
  for (const channel of currentCellChannels.value) {
    if (!channel.channelName) {
      ElMessage.warning('通道名称不能为空')
      return
    }
  }
  
  // 应用时长规则
  if (currentCellChannels.value.length === 1) {
    // 单通道持续播放
    currentCellChannels.value[0].duration = 0
  } else if (currentCellChannels.value.length > 1) {
    // 多通道，确保每个通道都有时长
    currentCellChannels.value.forEach(channel => {
      if (!channel.duration || channel.duration === 0) {
        channel.duration = 10  // 默认10秒
      }
    })
  }
  
  // 保存到存储
  cellChannelsData.value.set(cellIndex, JSON.parse(JSON.stringify(currentCellChannels.value)))
  
  console.log('[保存通道] 格子索引:', cellIndex)
  console.log('[保存通道] 保存的通道:', currentCellChannels.value)
  
  // 如果当前格子正在播放，更新为第一个通道
  const screen = videoScreens.value[cellIndex]
  if (currentCellChannels.value.length > 0 && screen) {
    const firstChannel = currentCellChannels.value[0]
    screen.camera = firstChannel
    screen.isPlaying = false
    screen.loading = true
    
    console.log('[保存通道] 切换到第一个通道:', firstChannel)
    
    // 如果是NVR通道，重新播放
    const config = firstChannel.config
    if (config && config.channelNo !== undefined) {
      nextTick(async () => {
        try {
          const success = await startPlayChannel(cellIndex, {
            channelNo: config.channelNo,
            name: firstChannel.name
          })
          if (!success) {
            screen.loading = false
            ElMessage.error(`播放失败: ${firstChannel.name}`)
          }
        } catch (error) {
          screen.loading = false
          console.error('[保存通道] 播放失败:', error)
        }
      })
    } else {
      screen.loading = false
    }
  } else if (currentCellChannels.value.length === 0 && screen) {
    // 没有通道了，清空格子
    stopPlayer(cellIndex)
    screen.camera = null
    screen.isPlaying = false
  }
  
  ElMessage.success('保存成功')
  channelManageVisible.value = false
}

// 处理选择摄像头
const handleSelectCamera = (camera: any) => {
  const screenIndex = selectedScreen.value !== null ? selectedScreen.value : 0
  const screen = videoScreens.value[screenIndex]
  screen.camera = { ...camera, recording: false }
  selectedCamera.value = camera
  currentNvrId.value = null
  cameraListVisible.value = false
  
  // ✅ 自动保存布局配置
  saveLayoutConfig()
  
  ElMessage.success(`已添加摄像头: ${camera.name}`)
}

// 关闭摄像头列表
const handleCloseCameraList = () => {
  cameraListVisible.value = false
}

// 关闭视频
const handleCloseVideo = (index: number) => {
  stopPlayer(index)
  videoScreens.value[index].camera = null
  if (selectedScreen.value === index) {
    selectedCamera.value = null
    currentNvrId.value = null
  }
  
  // 自动保存布局配置
  saveLayoutConfig()
  
  ElMessage.info('已关闭视频')
}

// 改变布局
const changeLayout = async (layout: string) => {
  // 1. 先保存当前布局的配置
  saveLayoutConfig()
  
  // 2. 切换到新布局
  stopAllPlayers()
  currentLayout.value = layout
  initVideoScreens()
  selectedScreen.value = 0
  selectedCamera.value = null
  currentNvrId.value = null
  
  // 3. 如果有当前任务，同步更新任务的布局信息
  if (currentTask.value) {
    currentTask.value.layout = layout
    console.log('[切换分屏] 已更新当前任务的布局:', layout)
  }
  
  const layoutName = layoutOptions.find(l => l.type === layout)?.name
  
  // 4. 尝试恢复新布局的配置
  await loadLayoutConfig(layout)
  
  // 如果没有恢复任何摄像头，显示提示
  const hasCameras = videoScreens.value.some(screen => screen.camera !== null)
  if (!hasCameras) {
    ElMessage.info(`已切换到 ${layoutName}，请添加摄像头`)
  }
}

// 开始PTZ控制（长按持续控制）
const startPtz = async (direction: string) => {
  const camera = selectedCamera.value
  const nvrId = currentNvrId.value
  
  // 静默验证，避免频繁弹窗
  if (!camera || !camera.canPtz || !nvrId || typeof camera.channelNo !== 'number') {
    console.warn('[PTZ] 无法控制: 缺少必要信息', { camera, nvrId })
    return
  }

  // 清理上一次的定时器
  if (ptzTimer) {
    clearInterval(ptzTimer)
    ptzTimer = null
  }

  // 获取 NVR 配置信息
  const nvrConfig = getNvrConfigFromCamera(camera)
  if (!nvrConfig) {
    console.warn('[PTZ] 无法获取 NVR 配置信息')
    return
  }

  // ✅ 保存当前方向（在发送命令之前）
  lastPtzMode = direction
  console.log(`[PTZ] 开始控制: ${direction}, 已保存lastPtzMode`)

  // 定义发送控制命令的函数
  const sendPtzCommand = async () => {
    // 判断是否直接登录球机（IP 以 192.168.1 开头）
    const isDirect = nvrConfig.ip.startsWith('192.168.1')
    // 通道号：直接登录球机使用通道1，否则使用NVR通道号
    const channelNo = isDirect ? 1 : camera.channelNo
    
    const controlReq: PtzControlReq = {
      ip: nvrConfig.ip,
      port: nvrConfig.port || 37777,
      username: nvrConfig.username || 'admin',
      password: nvrConfig.password || 'admin123',
      channel: channelNo,  // SDK通道号（1-based）
      command: direction,
      speed: 4,
      stop: false
    }

    try {
      await ptzControl(controlReq)
      console.log(`[PTZ] 控制中: ${direction}, 通道=${channelNo}`)
    } catch (e) {
      console.error('[PTZ] 控制失败', e)
      // 不弹窗，避免干扰用户
    }
  }

  // 立即发送第一次命令
  await sendPtzCommand()

  // 每隔 500ms 持续发送控制命令，保持云台移动
  ptzTimer = setInterval(sendPtzCommand, 500)
}

// 停止PTZ控制
const stopPtz = async () => {
  // ✅ 先保存 lastPtzMode，避免被提前清空
  const savedDirection = lastPtzMode
  
  // 清理定时器
  if (ptzTimer) {
    clearInterval(ptzTimer)
    ptzTimer = null
  }

  // ✅ 如果没有保存的方向，说明没有正在进行的控制，直接返回
  if (!savedDirection) {
    console.log('[PTZ] 没有正在进行的控制，跳过停止')
    return
  }

  // ✅ 立即清空 lastPtzMode，防止重复调用
  lastPtzMode = null

  const camera = selectedCamera.value
  const nvrId = currentNvrId.value
  
  // 静默验证
  if (!camera || !nvrId || typeof camera.channelNo !== 'number') {
    console.warn('[PTZ] 停止失败: 缺少必要信息')
    return
  }

  // 获取 NVR 配置信息
  const nvrConfig = getNvrConfigFromCamera(camera)
  if (!nvrConfig) {
    console.warn('[PTZ] 停止失败: 无法获取NVR配置')
    return
  }

  console.log(`[PTZ] 准备停止: ${savedDirection}`)

  // 判断是否直接登录球机（IP 以 192.168.1 开头）
  const isDirect = nvrConfig.ip.startsWith('192.168.1')
  // 通道号：直接登录球机使用通道1，否则使用NVR通道号
  const channelNo = isDirect ? 1 : camera.channelNo

  const controlReq: PtzControlReq = {
    ip: nvrConfig.ip,
    port: nvrConfig.port || 37777,
    username: nvrConfig.username || 'admin',
    password: nvrConfig.password || 'admin123',
    channel: channelNo,
    command: savedDirection,  // ✅ 使用保存的方向
    speed: 4,
    stop: true  // 停止标志
  }

  try {
    await ptzControl(controlReq)
    console.log(`[PTZ] ✅ 停止成功: ${savedDirection}, 通道=${channelNo}`)
  } catch (e) {
    console.error('[PTZ] ❌ 停止失败', e)
    // 不弹窗，避免干扰用户
  }
}

// 从摄像头对象获取 NVR 配置信息
const getNvrConfigFromCamera = (camera: any) => {
  try {
    // 尝试从 camera.config 解析
    if (camera.config) {
      const config = typeof camera.config === 'string' ? JSON.parse(camera.config) : camera.config
      return {
        ip: config.nvrIp || camera.nvrIp || camera.ipAddress,
        port: config.tcpPort || config.port || 37777,
        username: config.username || 'admin',
        password: config.password || 'admin123'
      }
    }
    
    // 回退：使用摄像头自身的属性
    return {
      ip: camera.nvrIp || camera.ipAddress,
      port: camera.nvrPort || 37777,
      username: 'admin',
      password: 'admin123'
    }
  } catch (e) {
    console.error('[PTZ] 解析 NVR 配置失败', e)
    return null
  }
}

// 获取PTZ方向文本
const getPtzDirectionText = (direction: string) => {
  const directionMap: Record<string, string> = {
    up: '向上移动',
    down: '向下移动',
    left: '向左移动',
    right: '向右移动',
    zoomIn: '放大',
    zoomOut: '缩小',
    focusNear: '近焦',
    focusFar: '远焦',
    irisOpen: '光圈开',
    irisClose: '光圈关'
  }
  return directionMap[direction] || direction
}

// PTZ归位
const handlePtzHome = () => {
  if (!selectedCamera.value || !selectedCamera.value.canPtz) {
    ElMessage.warning('当前摄像头不支持云台控制')
    return
  }
  
  ElMessage.success('云台归位')
}

// 录制
const handleRecord = (camera: any) => {
  camera.recording = !camera.recording
  ElMessage.success(camera.recording ? '开始录制' : '停止录制')
}

// 截图
const handleSnapshot = (camera: any) => {
  ElMessage.success(`已截图: ${camera.name}`)
}

// 全屏
const handleFullScreen = () => {
  if (document.fullscreenElement) {
    document.exitFullscreen()
  } else {
    document.documentElement.requestFullscreen()
  }
}

// 刷新布局
const handleRefresh = () => {
  stopAllPlayers()
  initVideoScreens()
  ElMessage.success('布局已刷新')
}

// 刷新设备列表
const refreshDevices = async () => {
  ElMessage.info('正在刷新设备列表...')
  await loadCamerasFromIoT()
}

/**
 * 保存布局配置到 localStorage（为每个分屏模式单独保存）
 */
const saveLayoutConfig = () => {
  try {
    // 获取所有布局的配置
    const allConfigs = JSON.parse(localStorage.getItem('multi-screen-preview-configs') || '{}')
    
    // 保存当前布局模式的配置
    allConfigs[currentLayout.value] = {
      screens: videoScreens.value.map(screen => {
        if (!screen.camera) {
          return null
        }
        // 只保存必要的摄像头信息（用于恢复时匹配）
        return {
          deviceId: screen.camera.deviceId,
          deviceName: screen.camera.name,
          location: screen.camera.location
        }
      })
    }
    
    // 同时记录最后使用的布局模式
    allConfigs._lastLayout = currentLayout.value
    
    localStorage.setItem('multi-screen-preview-configs', JSON.stringify(allConfigs))
    console.log(`✅ 已保存 ${currentLayout.value} 的布局配置:`, allConfigs[currentLayout.value])
  } catch (error) {
    console.error('❌ 保存布局配置失败:', error)
  }
}

/**
 * 从 localStorage 恢复布局配置（支持多布局模式）
 * @param targetLayout 目标布局模式，如果不指定则使用最后一次的布局
 */
const loadLayoutConfig = async (targetLayout?: string) => {
  try {
    // 迁移旧版本的单一配置到多布局配置
    const oldConfigStr = localStorage.getItem('multi-screen-preview-config')
    if (oldConfigStr) {
      try {
        const oldConfig = JSON.parse(oldConfigStr)
        const newConfigs: any = {}
        newConfigs[oldConfig.layout] = { screens: oldConfig.screens }
        newConfigs._lastLayout = oldConfig.layout
        localStorage.setItem('multi-screen-preview-configs', JSON.stringify(newConfigs))
        localStorage.removeItem('multi-screen-preview-config')
        console.log('✅ 已迁移旧配置到多布局模式')
      } catch (error) {
        console.warn('⚠️ 迁移旧配置失败:', error)
      }
    }
    
    const configsStr = localStorage.getItem('multi-screen-preview-configs')
    if (!configsStr) {
      console.log('ℹ️ 未找到保存的布局配置，使用默认配置')
      return
    }
    
    const allConfigs = JSON.parse(configsStr)
    
    // 确定要恢复的布局模式
    let layoutToRestore = targetLayout || allConfigs._lastLayout || currentLayout.value
    
    // 如果是首次加载且有保存的布局模式，切换到该模式
    if (!targetLayout && allConfigs._lastLayout && currentLayout.value !== allConfigs._lastLayout) {
      currentLayout.value = allConfigs._lastLayout
      initVideoScreens()
      layoutToRestore = allConfigs._lastLayout
    }
    
    const config = allConfigs[layoutToRestore]
    
    if (!config) {
      console.log(`ℹ️ 未找到 ${layoutToRestore} 的保存配置`)
      return
    }
    
    console.log(`📋 正在恢复 ${layoutToRestore} 的布局配置:`, config)
    
    // 等待摄像头列表加载完成
    if (cameras.value.length === 0) {
      console.log('⏳ 等待摄像头列表加载...')
      // 最多等待5秒
      let retries = 50
      while (cameras.value.length === 0 && retries > 0) {
        await new Promise(resolve => setTimeout(resolve, 100))
        retries--
      }
    }
    
    if (cameras.value.length === 0) {
      console.warn('⚠️ 摄像头列表为空，无法恢复布局')
      return
    }
    
    // 恢复每个格子的摄像头
    if (config.screens && Array.isArray(config.screens)) {
      let restoredCount = 0
      
      config.screens.forEach((screenConfig: any, index: number) => {
        if (screenConfig && screenConfig.deviceId && videoScreens.value[index]) {
          // 根据 deviceId 查找对应的摄像头
          const camera = cameras.value.find((c: any) => c.id === screenConfig.deviceId)
          
          if (camera) {
            videoScreens.value[index].camera = {
              id: camera.id,
              deviceId: camera.id,
              name: camera.name || screenConfig.deviceName,  // ✅ 修复：使用 camera.name
              location: camera.location || screenConfig.location || '未知位置',  // ✅ 修复：使用 camera.location
              snapshot: camera.snapshot || '',  // ✅ 修复：使用 camera.snapshot
              streamUrl: '',  // ⚠️ 清空 streamUrl，避免恢复时使用无效URL
              canPtz: camera.canPtz || false,
              recording: false
            }
            console.log(`✅ 已恢复窗口 ${index + 1} 的摄像头: ${camera.name}`)  // ✅ 修复：使用 camera.name
            restoredCount++
          } else {
            console.warn(`⚠️ 未找到设备ID为 ${screenConfig.deviceId} 的摄像头`)
          }
        }
      })
      
      if (restoredCount > 0) {
        const layoutName = layoutOptions.find(l => l.type === layoutToRestore)?.name
        ElMessage.success(`已恢复 ${layoutName} 的配置 (${restoredCount} 个摄像头)`)
      }
    }
    
  } catch (error) {
    console.error('❌ 恢复布局配置失败:', error)
    ElMessage.warning('恢复布局配置失败，使用默认配置')
  }
}

// 保存布局（手动保存按钮 - 已废弃，改用保存任务）
const handleSaveLayout = () => {
  saveLayoutConfig()
  ElMessage.success('布局已保存到本地')
}

// 打开任务编辑对话框（SmartPSS 风格）
const handleOpenTaskEditDialog = () => {
  if (!currentTask.value) {
    ElMessage.warning('请先选择一个任务')
    return
  }
  taskEditDialogVisible.value = true
}

// 从对话框保存任务
const handleSaveTaskFromDialog = async (data: any) => {
  console.log('[从对话框保存任务] 数据:', data)
  
  if (!currentTask.value || !currentTask.value.id) {
    ElMessage.warning('没有选中的任务')
    return
  }
  
  try {
    // 更新任务数据
    const taskData = {
      taskName: data.taskName,
      // 这里可以添加更多字段的处理
      // channels: data.channels,
      // videoCells: data.videoCells
    }
    
    await updateInspectionTask(currentTask.value.id, taskData)
    
    // 更新本地任务对象
    currentTask.value = { ...currentTask.value, ...taskData }
    
    // 刷新任务列表
    await loadTaskList()
    
    ElMessage.success(`任务"${data.taskName}"已保存`)
  } catch (error) {
    console.error('[从对话框保存任务] 失败:', error)
    ElMessage.error('保存任务失败')
  }
}

// 保存当前任务（更新现有任务）
const handleSaveCurrentTask = async () => {
  console.log('[保存任务] currentTask.value:', currentTask.value)
  console.log('[保存任务] currentTask.value?.id:', currentTask.value?.id)
  
  if (!currentTask.value || !currentTask.value.id) {
    console.error('[保存任务] ❌ 检查失败: currentTask.value=', currentTask.value, ', id=', currentTask.value?.id)
    ElMessage.warning('没有选中的任务')
    return
  }
  
  try {
    // 将当前布局转换为任务数据
    const taskData = convertToTask(currentTask.value.taskName)
    taskData.id = currentTask.value.id
    taskData.status = currentTask.value.status
    
    // 调用更新接口
    await updateInspectionTask(currentTask.value.id, taskData)
    
    // 更新本地任务对象
    currentTask.value = { ...currentTask.value, ...taskData }
    
    // 刷新任务列表
    await loadTaskList()
    
    ElMessage.success(`任务"${currentTask.value.taskName}"已保存`)
  } catch (error) {
    console.error('[保存任务] 失败:', error)
    ElMessage.error('保存任务失败')
  }
}

// 另存为新任务
const handleSaveAsNewTask = async () => {
  try {
    const { value: taskName } = await ElMessageBox.prompt('请输入新任务名称', '另存为任务', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '任务名称不能为空'
    })
    
    if (!taskName) return
    
    // 将当前布局转换为任务数据
    const task = convertToTask(taskName)
    
    const res = await createInspectionTask(task)
    ElMessage.success('任务创建成功')
    
    // 重新加载任务列表
    await loadTaskList()
    
    // 设置为当前任务
    currentTask.value = (res as any)?.data || { ...task, id: (res as any) }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('[另存为任务] 失败:', error)
      ElMessage.error('创建任务失败')
    }
  }
}

// ========== 巡检任务管理 ==========

// 任务相关状态
const taskList = ref<InspectionTask[]>([])
const currentTask = ref<InspectionTask | null>(null)
const taskSearchKeyword = ref('')
const runningTaskId = ref<number | null>(null)  // 当前运行的任务ID

// 通道轮播相关
interface ChannelRotation {
  cellIndex: number
  channels: any[]
  currentIndex: number
  timer: any
}
const channelRotations = ref<Map<number, ChannelRotation>>(new Map())

// 过滤后的任务列表
const filteredTaskList = computed(() => {
  if (!taskSearchKeyword.value) {
    return taskList.value
  }
  return taskList.value.filter(task => 
    task.taskName.includes(taskSearchKeyword.value)
  )
})

// 获取任务状态图标
const getTaskStatusIcon = (status: InspectionTaskStatus) => {
  const iconMap = {
    draft: 'ep:document',
    active: 'ep:video-play',
    paused: 'ep:video-pause'
  }
  return iconMap[status] || 'ep:document'
}

// 获取任务状态类型（用于el-tag）
const getTaskStatusType = (status: InspectionTaskStatus): 'success' | 'warning' | 'info' | 'primary' | 'danger' => {
  const typeMap: Record<InspectionTaskStatus, 'success' | 'warning' | 'info'> = {
    draft: 'info',
    active: 'success',
    paused: 'warning'
  }
  return typeMap[status] || 'info'
}

// 获取任务状态文本
const getTaskStatusText = (status: InspectionTaskStatus) => {
  const textMap = {
    draft: '草稿',
    active: '运行中',
    paused: '已暂停'
  }
  return textMap[status] || '未知'
}

// 加载任务列表
const loadTaskList = async () => {
  try {
    const res = await getInspectionTaskList()
    console.log('[任务列表] 原始响应:', res)
    console.log('[任务列表] res.data:', (res as any)?.data)
    console.log('[任务列表] res类型:', typeof res, Object.keys(res || {}))
    
    // 尝试多种可能的数据格式
    let tasks = []
    if (Array.isArray(res)) {
      tasks = res
    } else if ((res as any)?.data) {
      tasks = Array.isArray((res as any).data) ? (res as any).data : [(res as any).data]
    } else if ((res as any)?.list) {
      tasks = (res as any).list
    }
    
    taskList.value = tasks
    console.log('[任务列表] 加载完成:', taskList.value.length, '个任务', taskList.value)
  } catch (error) {
    console.error('[任务列表] 加载失败:', error)
    ElMessage.error('加载任务列表失败')
  }
}

// 新建任务
const handleNewTask = async () => {
  try {
    const { value: taskName } = await ElMessageBox.prompt('请输入任务名称', '新建任务', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '任务名称不能为空'
    })

    if (!taskName) return

    // 清空当前分屏配置
    cellChannelsData.value.clear()
    snapshotBlobUrls.value.clear()
    
    // 创建空白任务
    const task = {
      taskName: taskName,
      layout: currentLayout.value,
      scenes: [] // 空白配置
    }
    
    const res = await createInspectionTask(task)
    ElMessage.success('任务创建成功')
    
    // 重新加载任务列表
    await loadTaskList()
    
    // 设置为当前任务
    currentTask.value = (res as any)?.data || task
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('[新建任务] 失败:', error)
      ElMessage.error('创建任务失败')
    }
  }
}

// 编辑任务名称
const handleEditTaskName = async (task: InspectionTask) => {
  try {
    const { value: newName } = await ElMessageBox.prompt('请输入新的任务名称', '编辑任务', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: task.taskName,
      inputPattern: /\S+/,
      inputErrorMessage: '任务名称不能为空'
    })

    if (!newName || newName === task.taskName) return

    await updateInspectionTask(task.id!, { taskName: newName })
    ElMessage.success('任务名称已更新')
    
    // 重新加载任务列表
    await loadTaskList()
    
    // 如果是当前任务，更新当前任务
    if (currentTask.value && currentTask.value.id === task.id) {
      currentTask.value.taskName = newName
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('[编辑任务] 失败:', error)
      ElMessage.error('更新任务失败')
    }
  }
}

// 删除任务
const handleDeleteTask = async (taskId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个任务吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteInspectionTask(taskId)
    ElMessage.success('任务已删除')
    
    // 如果删除的是当前任务，清空当前任务
    if (currentTask.value?.id === taskId) {
      currentTask.value = null
      // 清空布局
      cellChannelsData.value.clear()
      initVideoScreens()
    }
    
    // 重新加载任务列表
    await loadTaskList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('[删除任务] 失败:', error)
      ElMessage.error('删除任务失败')
    }
  }
}

// 加载任务
const handleLoadTask = async (taskId: number) => {
  try {
    const res = await getInspectionTask(taskId)
    const task = (res as any)?.data || res
    
    // 设置为当前任务
    currentTask.value = task
    
    // 加载任务配置到当前布局
    loadFromTask(task)
    
    ElMessage.success(`已加载任务：${task.taskName}`)
  } catch (error) {
    console.error('[加载任务] 失败:', error)
    ElMessage.error('加载任务失败')
  }
}

// 将当前布局转换为任务数据
const convertToTask = (taskName: string): InspectionTask => {
  const scenes: InspectionScene[] = []
  
  // 遍历所有格子，收集有通道的格子
  cellChannelsData.value.forEach((channels, cellIndex) => {
    if (channels && channels.length > 0) {
      const inspectionChannels: InspectionChannelConfig[] = channels.map(ch => ({
        deviceId: ch.deviceId || ch.id,
        channelId: ch.channelId || ch.id,
        channelName: ch.channelName || ch.name,
        duration: ch.duration || 0,
        ipAddress: ch.ipAddress,
        productId: ch.productId,
        config: ch.config ? (typeof ch.config === 'string' ? ch.config : JSON.stringify(ch.config)) : '',
        streamUrl: ch.streamUrl,
        nvrId: ch.nvrId,
        channelNo: ch.channelNo,
        location: ch.location,
        snapshot: ch.snapshot
      }))
      
      scenes.push({
        cellIndex,
        channels: inspectionChannels
      })
    }
  })
  
  return {
    taskName,
    layout: currentLayout.value,
    scenes,
    status: 'draft'
  }
}

// 从任务数据加载到当前布局
const loadFromTask = (task: InspectionTask) => {
  console.log('[加载任务] ========== 开始加载任务 ==========')
  console.log('[加载任务] 任务名称:', task.taskName)
  console.log('[加载任务] 任务ID:', task.id)
  console.log('[加载任务] 任务分屏模式:', task.layout)
  console.log('[加载任务] 任务完整数据:', JSON.stringify(task, null, 2))
  
  // 检查scenes字段
  if (!task.scenes || !Array.isArray(task.scenes)) {
    console.warn('[加载任务] ⚠️ 任务没有scenes字段或scenes不是数组:', task.scenes)
    task.scenes = []
  }
  console.log('[加载任务] 场景数量:', task.scenes.length)
  
  // 停止所有正在运行的轮播
  stopAllRotations()
  
  // 切换到任务指定的分屏布局
  if (task.layout && task.layout !== currentLayout.value) {
    console.log(`[加载任务] 🔄 切换分屏: ${currentLayout.value} -> ${task.layout}`)
    currentLayout.value = task.layout
    initVideoScreens()
  } else {
    console.log('[加载任务] ✅ 分屏模式无需切换，当前:', currentLayout.value)
  }
  
  // 清空现有数据
  console.log('[加载任务] 🧹 清空现有配置...')
  cellChannelsData.value.clear()
  snapshotBlobUrls.value.clear()
  console.log('[加载任务] ✅ 清空完成')
  
  // 加载场景数据
  if (task.scenes.length === 0) {
    console.log('[加载任务] ℹ️ 任务没有配置场景，显示空白分屏')
    return
  }
  
  console.log('[加载任务] 📦 开始加载场景数据...')
  task.scenes.forEach(scene => {
    console.log(`[加载任务] 加载格子${scene.cellIndex}, 通道数: ${scene.channels.length}`)
    
    const channels = scene.channels.map(ch => {
      // 解析config字段（如果是字符串）
      let config = ch.config
      if (typeof config === 'string' && config) {
        try {
          config = JSON.parse(config)
        } catch (e) {
          console.warn('[加载任务] config解析失败:', e)
          config = {}
        }
      }
      
      return {
        id: ch.deviceId,
        deviceId: ch.deviceId,
        channelId: ch.channelId,
        name: ch.channelName,
        channelName: ch.channelName,
        duration: ch.duration || 0,
        ipAddress: ch.ipAddress,
        productId: ch.productId,
        config: config,
        streamUrl: ch.streamUrl,
        nvrId: ch.nvrId,
        channelNo: ch.channelNo,
        location: ch.location,
        snapshot: ch.snapshot,
        status: 'online',
        canPtz: !!ch.nvrId
      }
    })
    
    cellChannelsData.value.set(scene.cellIndex, channels)
    
    // 异步加载截图
    loadCellSnapshot(scene.cellIndex)
    
    console.log(`[加载任务] ✅ 格子${scene.cellIndex}已配置 ${channels.length} 个通道`)
    console.log(`[加载任务] 💡 提示：通道已加载，请手动点击格子播放视频`)
  })
  
  console.log('[加载任务] ✅ 任务加载完成:', task.taskName)
  console.log('[加载任务] 当前分屏:', currentLayout.value)
}

// 试运行任务
const handleTrialRun = async (task: InspectionTask) => {
  try {
    // 先加载任务
    await handleLoadTask(task.id!)
    
    // 设置为运行状态
    runningTaskId.value = task.id!
    
    // 启动所有格子的通道轮播
    task.scenes.forEach(scene => {
      if (scene.channels.length > 1) {
        // 多个通道，启动轮播
        startChannelRotation(scene.cellIndex, scene.channels)
      } else if (scene.channels.length === 1) {
        // 单个通道，直接播放
        const channel = scene.channels[0]
        if (channel.channelNo !== undefined) {
          // ✅ 传递完整的通道信息
          startPlayChannel(scene.cellIndex, channel)
        }
      }
    })
    
    ElMessage.success(`任务 "${task.taskName}" 开始试运行`)
    console.log('[试运行] 任务启动:', task.taskName)
  } catch (error) {
    console.error('[试运行] 启动失败:', error)
    ElMessage.error('启动试运行失败')
  }
}

// 停止运行
const handleStopRun = () => {
  // 停止所有轮播
  stopAllRotations()
  
  // 清空运行状态
  runningTaskId.value = null
  
  ElMessage.info('已停止试运行')
  console.log('[试运行] 已停止')
}

// 启动通道轮播
const startChannelRotation = (cellIndex: number, channels: any[]) => {
  // 先停止该格子的现有轮播
  stopChannelRotation(cellIndex)
  
  if (channels.length <= 1) {
    console.log(`[轮播] 格子${cellIndex}只有${channels.length}个通道，无需轮播`)
    return
  }
  
  console.log(`[轮播] 启动格子${cellIndex}的通道轮播，共${channels.length}个通道`)
  
  const rotation: ChannelRotation = {
    cellIndex,
    channels,
    currentIndex: 0,
    timer: null
  }
  
  // 播放第一个通道
  const firstChannel = channels[0]
  console.log(`[轮播] 第一个通道数据:`, firstChannel)
  if (firstChannel.channelNo !== undefined) {
    // ✅ 传递完整的通道信息，包括NVR配置
    startPlayChannel(cellIndex, firstChannel)
  }
  
  // 设置定时器，根据时长切换
  const scheduleNext = () => {
    const currentChannel = rotation.channels[rotation.currentIndex]
    const duration = currentChannel.duration || 10  // 默认10秒
    
    console.log(`[轮播] 格子${cellIndex} 当前通道: ${currentChannel.channelName}, 时长: ${duration}秒`)
    
    rotation.timer = setTimeout(() => {
      // 切换到下一个通道
      rotation.currentIndex = (rotation.currentIndex + 1) % rotation.channels.length
      const nextChannel = rotation.channels[rotation.currentIndex]
      
      console.log(`[轮播] 格子${cellIndex} 切换到: ${nextChannel.channelName}`, nextChannel)
      
      if (nextChannel.channelNo !== undefined) {
        // ✅ 传递完整的通道信息，包括NVR配置
        startPlayChannel(cellIndex, nextChannel)
      }
      
      // 继续下一轮
      scheduleNext()
    }, duration * 1000)
  }
  
  // 启动轮播
  scheduleNext()
  
  // 保存轮播信息
  channelRotations.value.set(cellIndex, rotation)
}

// 停止指定格子的轮播
const stopChannelRotation = (cellIndex: number) => {
  const rotation = channelRotations.value.get(cellIndex)
  if (rotation && rotation.timer) {
    clearTimeout(rotation.timer)
    rotation.timer = null
    console.log(`[轮播] 停止格子${cellIndex}的轮播`)
  }
  channelRotations.value.delete(cellIndex)
}

// 停止所有轮播
const stopAllRotations = () => {
  console.log('[轮播] 停止所有轮播')
  channelRotations.value.forEach((rotation, cellIndex) => {
    if (rotation.timer) {
      clearTimeout(rotation.timer)
    }
  })
  channelRotations.value.clear()
}

// ========== WebSocket 设备状态监听 ==========

/**
 * 处理设备状态变化（WebSocket 推送）
 */
const handleDeviceStatusChange = (data: any) => {
  console.log('[WebSocket] 设备状态变化:', data)
  
  const { deviceId, deviceName, status } = data
  const isOnline = status === 'online'
  
  // 更新 NVR 树中的状态
  updateNvrTreeStatus(deviceId, isOnline ? 1 : 0)
  
  // 显示通知
  const statusText = isOnline ? '上线' : '离线'
  const type = isOnline ? 'success' : 'warning'
  ElMessage({
    message: `设备 ${deviceName} 已${statusText}`,
    type,
    duration: 3000
  })
}

/**
 * 更新 NVR 树中的设备状态
 */
const updateNvrTreeStatus = (deviceId: number, newState: number) => {
  const nvr = nvrTreeData.value.find(n => n.nvrId === deviceId)
  if (nvr) {
    nvr.state = newState
    console.log(`[NVR状态更新] NVR ${nvr.name} 状态: ${newState === 1 ? '在线' : '离线'}`)
  }
}

onMounted(async () => {
  console.log('🚀 巡检任务页面初始化...')
  
  // 先初始化默认布局
  initVideoScreens()
  
  // 初始化大华SDK
  await initDahuaSDK()
  
  // 并行加载空间树、摄像头列表和任务列表
  await Promise.all([
    loadSpaceTree(),
    loadCamerasFromIoT(),
    loadTaskList()  // ✅ 加载任务列表
  ])
  
  // 摄像头列表加载完成后，尝试恢复上次的布局配置
  await loadLayoutConfig()
  
  // ✅ 连接 WebSocket，监听设备状态变化
  iotWebSocket.connect()
  iotWebSocket.on('device_status', handleDeviceStatusChange)
  
  console.log('✅ 巡检任务页面初始化完成')
})

onUnmounted(() => {
  // 页面卸载时保存当前配置
  saveLayoutConfig()
  stopPtz()
  
  // ✅ 停止所有轮播
  stopAllRotations()
  
  // ✅ 取消 WebSocket 监听
  iotWebSocket.off('device_status', handleDeviceStatusChange)
  
  // 清理播放器资源
  if (playerInstances && playerInstances.length > 0) {
    playerInstances.forEach((player, index) => {
      if (player && player.stopPlay) {
        try {
          player.stopPlay()
          console.log(`[播放器] 清理播放器实例${index}`)
        } catch (error) {
          console.error(`[播放器] 清理播放器实例${index}失败:`, error)
        }
      }
    })
    playerInstances = []
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/dark-theme.scss';

.multi-screen-preview-page {
  background: linear-gradient(135deg, #0a1628 0%, #112240 100%);
  min-height: 100vh;
  display: flex;
  flex-direction: column;

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 20px;
    background: rgba(255, 255, 255, 0.05);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);

    .toolbar-left {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .page-title {
        color: #ffffff;
        font-size: 14px;
        font-weight: 500;
      }
      
      .current-task-badge {
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }

    .toolbar-right {
      display: flex;
      gap: 10px;

      :deep(.el-button) {
        background-color: rgba(255, 255, 255, 0.1);
        border-color: rgba(255, 255, 255, 0.2);
        color: #ffffff;

        &:hover {
          border-color: #00d4ff;
          color: #00d4ff;
        }

        &.el-button--primary {
          background-color: #00d4ff;
          border-color: #00d4ff;

          &:hover {
            background-color: rgba(0, 212, 255, 0.8);
          }
        }
      }
    }
  }

  .main-content {
    display: flex;
    flex: 1;
    min-height: 0;

    .left-panel {
      width: 320px;
      background: rgba(255, 255, 255, 0.05);
      border-right: 1px solid rgba(255, 255, 255, 0.1);
      display: flex;
      flex-direction: column;

      .panel-header {
        padding: 15px 20px;
        border-bottom: 1px solid rgba(255, 255, 255, 0.1);

        h3 {
          color: #ffffff;
          margin: 0;
          font-size: 16px;
          font-weight: 600;
        }
      }

      .panel-content {
        flex: 1;
        padding: 15px;
        overflow: auto;
        min-height: 0;

        .search-box {
          margin-bottom: 15px;

          :deep(.el-input) {
            .el-input__wrapper {
              background-color: rgba(255, 255, 255, 0.1);
              border: 1px solid rgba(255, 255, 255, 0.2);

              &:hover {
                border-color: #00d4ff;
              }

              &.is-focus {
                border-color: #00d4ff;
              }
            }

            .el-input__inner {
              color: #ffffff;

              &::placeholder {
                color: rgba(255, 255, 255, 0.5);
              }
            }
          }
        }

        .area-tree {
          :deep(.el-tree) {
            background: transparent;
            color: #ffffff;

            .el-tree-node {
              .el-tree-node__content {
                background: transparent;
                color: #ffffff;

                &:hover {
                  background-color: rgba(0, 212, 255, 0.1);
                }

                .el-tree-node__expand-icon {
                  color: #ffffff;
                }

                .tree-node {
                  display: flex;
                  align-items: center;
                  gap: 8px;
                  flex: 1;

                  .node-label {
                    flex: 1;
                  }

                  .node-count {
                    background: rgba(0, 212, 255, 0.2);
                    color: #00d4ff;
                    padding: 2px 6px;
                    border-radius: 10px;
                    font-size: 12px;
                  }
                  
                  .drag-hint {
                    color: rgba(0, 212, 255, 0.6);
                    display: inline-flex;
                    align-items: center;
                  }
                  
                  // 可拖拽设备样式
                  &.draggable-device {
                    cursor: move;
                    transition: all 0.3s;
                    
                    &:hover {
                      background: rgba(0, 212, 255, 0.15);
                      padding: 2px 6px;
                      border-radius: 4px;
                      
                      .drag-hint {
                        color: #00d4ff;
                      }
                    }
                    
                    &:active {
                      opacity: 0.7;
                    }
                  }
                }
              }
            }
          }
        }
      }

      // 未选择任务提示
      .no-task-hint {
        padding: 20px;
        
        :deep(.el-alert) {
          background: rgba(230, 162, 60, 0.1);
          border-color: rgba(230, 162, 60, 0.3);
          
          .el-alert__title {
            color: #e6a23c;
          }
          
          .el-alert__content {
            color: rgba(255, 255, 255, 0.8);
            
            p {
              margin: 0 0 10px 0;
              line-height: 1.6;
            }
          }
        }
      }

      // 任务列表样式
      .task-list {
        flex: 1;
        overflow-y: auto;
        padding: 10px;
        
        .task-item {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 12px;
          margin-bottom: 8px;
          background: rgba(255, 255, 255, 0.05);
          border: 1px solid rgba(255, 255, 255, 0.1);
          border-radius: 6px;
          cursor: pointer;
          transition: all 0.3s;
          
          &:hover {
            background: rgba(255, 255, 255, 0.1);
            border-color: rgba(0, 212, 255, 0.5);
          }
          
          &.active {
            background: rgba(0, 212, 255, 0.15);
            border-color: #00d4ff;
            
            .task-name {
              color: #00d4ff;
            }
          }
          
          &.running {
            border-color: #67c23a;
            box-shadow: 0 0 10px rgba(103, 194, 58, 0.3);
            animation: pulse 2s ease-in-out infinite;
          }
          
          @keyframes pulse {
            0%, 100% {
              box-shadow: 0 0 10px rgba(103, 194, 58, 0.3);
            }
            50% {
              box-shadow: 0 0 20px rgba(103, 194, 58, 0.5);
            }
          }
          
          .task-info {
            display: flex;
            align-items: center;
            gap: 10px;
            flex: 1;
            min-width: 0;
            
            .status-draft {
              color: #909399;
            }
            
            .status-active {
              color: #67c23a;
            }
            
            .status-paused {
              color: #e6a23c;
            }
            
            .task-name {
              color: #ffffff;
              font-size: 14px;
              flex: 1;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
          }
          
          .task-actions {
            display: flex;
            gap: 4px;
            opacity: 0;
            transition: opacity 0.3s;
          }
          
          &:hover .task-actions {
            opacity: 1;
          }
        }
      }
      
      .task-footer {
        padding: 10px;
        border-top: 1px solid rgba(255, 255, 255, 0.1);
      }

      // 左侧云台控制样式
      .ptz-control {
        background: rgba(255, 255, 255, 0.05);
        border-top: 1px solid rgba(255, 255, 255, 0.1);
        padding: 15px;
        margin-top: auto;

        .control-title {
          display: flex;
          flex-direction: column;
          align-items: center;
          margin-bottom: 15px;

          h4 {
            color: #ffffff;
            margin: 0 0 5px 0;
            font-size: 14px;
            font-weight: 600;
          }

          .selected-camera {
            color: #00d4ff;
            font-size: 12px;
            text-align: center;
            word-break: break-all;
          }
        }

        .control-panel {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 15px;

          .direction-control {
            .direction-pad {
              position: relative;
              width: 100px;
              height: 100px;
              border-radius: 50%;
              background: rgba(255, 255, 255, 0.1);
              border: 2px solid rgba(255, 255, 255, 0.2);

              .direction-btn {
                position: absolute;
                width: 30px;
                height: 30px;
                border: none;
                border-radius: 50%;
                background: rgba(0, 212, 255, 0.2);
                color: #00d4ff;
                cursor: pointer;
                transition: all 0.3s ease;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 12px;

                &:hover {
                  background: #00d4ff;
                  color: #000000;
                  transform: scale(1.1);
                }

                &:active {
                  transform: scale(0.95);
                }

                &.up {
                  top: 5px;
                  left: 50%;
                  transform: translateX(-50%);
                }

                &.down {
                  bottom: 5px;
                  left: 50%;
                  transform: translateX(-50%);
                }

                &.left {
                  left: 5px;
                  top: 50%;
                  transform: translateY(-50%);
                }

                &.right {
                  right: 5px;
                  top: 50%;
                  transform: translateY(-50%);
                }

                &.center {
                  top: 50%;
                  left: 50%;
                  transform: translate(-50%, -50%);
                  background: rgba(255, 255, 255, 0.1);
                }
              }
            }
          }

          .zoom-control {
            display: flex;
            gap: 8px;
            width: 100%;

            .zoom-buttons,
            .focus-buttons,
            .iris-buttons {
              display: flex;
              flex-direction: column;
              gap: 5px;
              flex: 1;

              .zoom-btn,
              .focus-btn,
              .iris-btn {
                width: 100%;
                height: 32px;
                border: 1px solid rgba(255, 255, 255, 0.2);
                border-radius: 4px;
                background: rgba(255, 255, 255, 0.1);
                color: #ffffff;
                cursor: pointer;
                transition: all 0.3s ease;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                gap: 2px;
                font-size: 10px;

                &:hover {
                  border-color: #00d4ff;
                  color: #00d4ff;
                }

                &:active {
                  background: rgba(0, 212, 255, 0.2);
                }

                span {
                  font-size: 8px;
                }
              }
            }
          }
        }
      }
    }

    .right-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      padding: 20px;
      overflow: hidden;

      .video-grid {
        flex: 1;
        display: grid;
        gap: 10px;
        min-height: 0;
        max-height: 100%;
        overflow: auto;

        &.grid-1x1 {
          grid-template-columns: 1fr;
          grid-template-rows: 1fr;
        }

        &.grid-2x2 {
          grid-template-columns: 1fr 1fr;
          grid-template-rows: 1fr 1fr;
        }

        &.grid-3x3 {
          grid-template-columns: repeat(3, 1fr);
          grid-template-rows: repeat(3, 1fr);
        }

        &.grid-4x4 {
          grid-template-columns: repeat(4, 1fr);
          grid-template-rows: repeat(4, 1fr);
        }

        .video-screen {
          background: rgba(255, 255, 255, 0.05);
          border: 2px solid rgba(255, 255, 255, 0.1);
          border-radius: 8px;
          overflow: hidden;
          cursor: pointer;
          position: relative;
          transition: all 0.3s ease;

          &:hover {
            border-color: #00d4ff;
          }

          &.active {
            border-color: #00d4ff;
            box-shadow: 0 0 10px rgba(0, 212, 255, 0.3);
          }
          
          // 拖拽悬停样式
          &.drag-over {
            border-color: #67c23a;
            border-style: dashed;
            border-width: 3px;
            background: rgba(103, 194, 58, 0.1);
            box-shadow: 0 0 15px rgba(103, 194, 58, 0.5);
            
            &::before {
              content: '松开鼠标添加设备';
              position: absolute;
              top: 50%;
              left: 50%;
              transform: translate(-50%, -50%);
              color: #67c23a;
              font-size: 16px;
              font-weight: bold;
              z-index: 10;
              text-shadow: 0 0 10px rgba(0, 0, 0, 0.8);
            }
          }

          .video-container {
            width: 100%;
            height: 100%;
            position: relative;

            .video-player {
              width: 100%;
              height: 100%;
              position: relative;
              background: #000;
              display: flex;
              align-items: center;
              justify-content: center;
              
              .dahua-canvas {
                display: block;
                background: #000;
              }
              
              .dahua-video {
                width: 100%;
                height: 100%;
                object-fit: contain;
                background: #000;
              }
              
              .dahua-loading {
                position: absolute;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                display: flex;
                flex-direction: column;
                align-items: center;
                gap: 10px;
                color: #fff;
                font-size: 14px;
                z-index: 10;
              }
            }

            .video-overlay {
              position: absolute;
              top: 0;
              left: 0;
              right: 0;
              bottom: 0;
              background: linear-gradient(
                to bottom,
                rgba(0, 0, 0, 0.6) 0%,
                transparent 30%,
                transparent 70%,
                rgba(0, 0, 0, 0.6) 100%
              );
              opacity: 0;
              transition: opacity 0.3s ease;
              pointer-events: none;

              .camera-info {
                position: absolute;
                top: 10px;
                left: 10px;
                color: #ffffff;

                .camera-name {
                  display: block;
                  font-size: 14px;
                  font-weight: 600;
                }

                .camera-location {
                  display: block;
                  font-size: 12px;
                  opacity: 0.8;
                }
              }

              .video-controls {
                position: absolute;
                top: 10px;
                right: 10px;
                display: flex;
                gap: 5px;
                pointer-events: auto;

                .el-button {
                  background: rgba(0, 0, 0, 0.6);
                  border: none;
                  color: #ffffff;

                  &:hover {
                    background: rgba(0, 212, 255, 0.8);
                  }
                }
              }
            }

            &:hover .video-overlay {
              opacity: 1;
            }

            .recording-indicator {
              position: absolute;
              bottom: 10px;
              left: 10px;
              background: rgba(255, 0, 0, 0.8);
              color: #ffffff;
              padding: 4px 8px;
              border-radius: 4px;
              font-size: 12px;
              display: flex;
              align-items: center;
              gap: 4px;
              animation: blink 1s infinite;
            }
          }

          .empty-screen {
            width: 100%;
            height: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            color: rgba(255, 255, 255, 0.6);
            position: relative;

            p {
              margin: 8px 0;
              font-size: 14px;

              &.tip {
                font-size: 12px;
                opacity: 0.7;
              }
            }

            // 通道预览容器
            .channel-preview {
              width: 100%;
              height: 100%;
              position: relative;
              overflow: hidden;

              .preview-image {
                width: 100%;
                height: 100%;
                object-fit: cover;
              }

              .no-snapshot {
                width: 100%;
                height: 100%;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                background: rgba(0, 0, 0, 0.3);
                color: rgba(255, 255, 255, 0.5);

                p {
                  margin-top: 12px;
                  font-size: 14px;
                }
              }

              .preview-overlay {
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                padding: 12px;
                background: linear-gradient(to bottom, rgba(0, 0, 0, 0.7), transparent);
                color: #fff;

                .channel-name {
                  font-size: 14px;
                  font-weight: 500;
                  margin-bottom: 4px;
                }

                .channel-count {
                  font-size: 12px;
                  opacity: 0.8;
                }
              }

              .play-button {
                position: absolute;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                color: rgba(255, 255, 255, 0.9);
                cursor: pointer;
                transition: all 0.3s;
                filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.3));

                &:hover {
                  color: #fff;
                  transform: translate(-50%, -50%) scale(1.15);
                  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.5));
                }
              }

              .manage-button {
                position: absolute;
                top: 10px;
                right: 10px;
                background: rgba(0, 0, 0, 0.6);
                color: #fff;
                width: 32px;
                height: 32px;
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                cursor: pointer;
                transition: all 0.3s;

                &:hover {
                  background: rgba(0, 212, 255, 0.9);
                  transform: scale(1.1);
                }

                svg {
                  font-size: 16px;
                }
              }
            }
          }
        }
      }
    }
  }

  .layout-selector {
    position: fixed;
    right: 20px;
    top: 50%;
    transform: translateY(-50%);
    background: rgba(255, 255, 255, 0.05);
    border-radius: 8px;
    padding: 10px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);

    .layout-options {
      display: flex;
      flex-direction: column;
      gap: 10px;

      .layout-option {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 5px;
        padding: 10px;
        border-radius: 6px;
        cursor: pointer;
        transition: all 0.3s ease;
        color: rgba(255, 255, 255, 0.7);

        &:hover {
          background: rgba(255, 255, 255, 0.1);
          color: #00d4ff;
        }

        &.active {
          background: rgba(0, 212, 255, 0.2);
          color: #00d4ff;
        }

        .layout-icon {
          width: 24px;
          height: 24px;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        span {
          font-size: 12px;
        }
      }
    }
  }
}

// 对话框样式
:deep(.el-dialog) {
  background-color: rgba(20, 30, 50, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);

  .el-dialog__header {
    background-color: transparent;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);

    .el-dialog__title {
      color: #ffffff;
    }
  }

  .el-dialog__body {
    background-color: transparent;
    color: #ffffff;

    .camera-list {
      .camera-search {
        margin-bottom: 20px;

        .el-input {
          .el-input__wrapper {
            background-color: rgba(255, 255, 255, 0.1);
            border: 1px solid rgba(255, 255, 255, 0.2);

            &:hover {
              border-color: #00d4ff;
            }

            &.is-focus {
              border-color: #00d4ff;
            }
          }

          .el-input__inner {
            color: #ffffff;

            &::placeholder {
              color: rgba(255, 255, 255, 0.5);
            }
          }
        }
      }

      .camera-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: 15px;
        max-height: 400px;
        overflow-y: auto;

        .camera-item {
          background: rgba(255, 255, 255, 0.05);
          border-radius: 8px;
          overflow: hidden;
          cursor: pointer;
          transition: all 0.3s ease;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
            border: 1px solid #00d4ff;
          }

          .camera-preview {
            position: relative;
            width: 100%;
            height: 120px;
            overflow: hidden;

            img {
              width: 100%;
              height: 100%;
              object-fit: cover;
            }

            .camera-overlay {
              position: absolute;
              top: 0;
              left: 0;
              right: 0;
              bottom: 0;
              background: rgba(0, 0, 0, 0.5);
              display: flex;
              align-items: center;
              justify-content: center;
              color: #ffffff;
              opacity: 0;
              transition: opacity 0.3s ease;
            }

            &:hover .camera-overlay {
              opacity: 1;
            }
          }

          .camera-info {
            padding: 12px;

            .camera-name {
              font-size: 14px;
              font-weight: 600;
              color: #ffffff;
              margin-bottom: 4px;
            }

            .camera-location {
              font-size: 12px;
              color: rgba(255, 255, 255, 0.7);
              margin-bottom: 8px;
            }

            .camera-status {
              font-size: 12px;
              padding: 2px 6px;
              border-radius: 4px;

              &.online {
                background: rgba(0, 255, 136, 0.2);
                color: #00ff88;
              }

              &.offline {
                background: rgba(255, 107, 107, 0.2);
                color: #ff6b6b;
              }
            }
          }
        }
      }
    }
  }
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0.5;
  }
}

// 右键菜单
.context-menu {
  position: fixed;
  z-index: 9999;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 4px 0;
  min-width: 150px;
  
  .menu-item {
    padding: 8px 16px;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 8px;
    transition: background-color 0.2s;
    color: #303133;
    font-size: 14px;
    
    &:hover {
      background-color: #f5f7fa;
    }
    
    :deep(.el-icon) {
      font-size: 16px;
    }
  }
}

// 通道管理
.channel-manage {
  .channel-list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    span {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
    }
  }
  
  .channel-tips {
    margin-top: 16px;
    
    p {
      margin: 4px 0;
      font-size: 13px;
    }
  }
  
  // 拖拽手柄样式
  .drag-handle {
    cursor: move;
    color: #909399;
    transition: color 0.2s;
    
    &:hover {
      color: #409eff;
    }
  }
  
  // 拖拽时的行样式
  :deep(.sortable-ghost) {
    opacity: 0.4;
    background: #f5f7fa;
  }
  
  :deep(.sortable-drag) {
    opacity: 0.8;
  }
}
</style>
