<template>
  <ContentWrap :body-style="{ padding: '0', height: '100%', display: 'flex', flexDirection: 'column' }" style="height: 100%; margin-bottom: 0">
    <div class="dark-theme-page">
      <div class="video-playback-container">
        <!-- 顶部导航栏 -->
        <div class="top-nav">
          <div class="nav-left">
            <span class="page-title">智慧安防 / 视频监控 / 录像回放</span>
          </div>

          <div class="nav-right">
            <el-button @click="handleRefresh">
              <Icon icon="ep:refresh" />
              刷新
            </el-button>
          </div>
        </div>

        <!-- 主要内容区域 -->
        <div class="main-layout">
          <!-- SmartPSS Plus 风格布局 -->
          <div class="smartpss-layout">
            <!-- 左侧面板：设备树 + 时间筛选 -->
            <div class="left-panel">
              <!-- 设备树 -->
              <div class="panel-section">
                <div class="section-header">
                  <Icon icon="ep:video-camera" />
                  <span>视频通道</span>
                </div>
                <div class="search-box">
                  <el-input 
                    v-model="deviceSearchKeyword" 
                    placeholder="搜索通道名称..." 
                    clearable
                    size="small"
                    @keyup.enter="handleChannelSearch"
                    @clear="handleSearchClear"
                  >
                    <template #prefix>
                      <Icon icon="ep:search" />
                    </template>
                    <template #append>
                      <el-button :icon="Search" @click="handleChannelSearch" />
                    </template>
                  </el-input>
                </div>
                <el-tree
                  ref="cameraTreeRef"
                  :data="cameraTreeData"
                  :props="treeProps"
                  :lazy="true"
                  :load="loadTreeNode"
                  :accordion="true"
                  node-key="id"
                  show-checkbox
                  :check-strictly="false"
                  :render-after-expand="false"
                  @node-click="handleCameraSelect"
                  @check="handleChannelCheck"
                  class="device-tree"
                >
                  <template #default="{ data }">
                    <div 
                      class="tree-node" 
                      :class="'node-type-' + data.type"
                      :draggable="data.type === 'channel'"
                      @dragstart="handleDragStart($event, data)"
                    >
                      <Icon
                        v-if="data.type === 'building'"
                        icon="ep:office-building"
                        style="color: #409eff"
                      />
                      <Icon
                        v-else-if="data.type === 'floor'"
                        icon="ep:tickets"
                        style="color: #67c23a"
                      />
                      <Icon
                        v-else-if="data.type === 'area'"
                        icon="ep:location"
                        style="color: #e6a23c"
                      />
                      <Icon
                        v-else-if="data.type === 'channels'"
                        icon="ep:folder"
                        style="color: #909399"
                      />
                      <Icon
                        v-else-if="data.type === 'channel'"
                        icon="ep:video-camera"
                        style="color: #f56c6c"
                      />
                      <Icon v-else icon="ep:video-camera" style="color: #f56c6c" />
                      <span>{{ data.name }}</span>
                    </div>
                  </template>
                </el-tree>
              </div>

              <!-- 时间筛选 -->
              <div class="panel-section">
                <div class="section-header">
                  <Icon icon="ep:calendar" />
                  <span>时间</span>
                </div>
                <div class="time-filter">
                  <!-- <el-radio-group v-model="recordingMode" size="small" class="recording-mode">
                    <el-radio-button value="video">实录</el-radio-button>
                  </el-radio-group> -->
                  
                  <div class="filter-item">
                    <label>录像类型:</label>
                    <el-select v-model="selectedRecordingType" size="small" style="width: 100%">
                      <el-option label="所有录像" :value="0" />
                      <el-option label="主录像" :value="1" />
                    </el-select>
                  </div>
                  
                  <div class="filter-item">
                    <label>码流类型:</label>
                    <el-select v-model="selectedTimeRange" size="small" style="width: 100%">
                      <el-option label="主码流" :value="0" />
                      <el-option label="子码流" :value="1" />
                    </el-select>
                  </div>
                  
                  <div class="filter-item">
                    <label>时间段:</label>
                    <el-date-picker
                      v-model="filterForm.timeRange"
                      type="datetimerange"
                      start-placeholder="开始时间"
                      end-placeholder="结束时间"
                      size="small"
                      style="width: 100%"
                      format="MM/DD HH:mm"
                      value-format="YYYY-MM-DD HH:mm:ss"
                    />
                  </div>
                  
                  <el-button type="primary" size="small" @click="handleSearch" style="width: 100%">
                    搜索
                  </el-button>
                </div>
              </div>
            </div>

            <!-- 中间面板：播放器网格 + 时间轴 -->
            <div class="center-panel">
              <!-- 多窗口播放器区 -->
              <div class="player-section">
                <!-- 顶部不再显示标题与工具栏，统一移至底部控制条 -->
                
                <!-- 多窗口播放器网格 -->
                <div class="player-grid" :class="gridLayoutClass" ref="playerGridRef">
                  <div
                    v-for="(pane, idx) in panes"
                    :key="idx"
                    class="player-pane"
                    :class="{ active: activePane === idx, 'drag-over': dragOverPane === idx }"
                    @click="activePane = idx"
                    @drop="handleDrop($event, idx)"
                    @dragover.prevent="handleDragOver($event, idx)"
                    @dragleave="handleDragLeave"
                  >
                    <!-- ZLMediaKit + mpegts.js 播放器 -->
                    <div 
                      class="zlm-player-container" 
                      style="width: 100%; height: 100%; position: relative; background: #000;"
                      :style="{ cursor: pane.isPlaying ? 'pointer' : 'default' }"
                      @click="pane.isPlaying && togglePanePlayPause(idx)"
                      :title="pane.isPlaying ? (pane.isPaused ? '点击播放' : '点击暂停') : ''"
                    >
                      <video 
                        :ref="(el) => setPaneVideoRef(el, idx)" 
                        :id="`zlm_video_${idx}`" 
                        class="pane-video"
                        :muted="pane.muted"
                        playsinline
                        style="width: 100%; height: 100%; object-fit: contain;"
                      ></video>
                    </div>
                    
                    <!-- 播放器覆盖层（播放时或加载中显示加载层） -->
                    <div v-if="!pane.isPlaying || pane.isLoading" class="pane-overlay">
                      <div class="overlay-top">
                        <span class="window-title">窗口 {{ idx + 1 }}</span>
                      </div>
                      
                      <!-- 加载中状态 -->
                      <div v-if="pane.isLoading" class="overlay-center loading">
                        <div class="spinner" aria-label="loading" style="width:64px;height:64px;border:4px solid rgba(64,158,255,0.2);border-top-color:#409eff;border-radius:50%;animation:rotate 1s linear infinite"></div>
                        <p class="window-label">正在加载录像...</p>
                        <p class="tip-text">{{ pane.channelName }}</p>
                      </div>
                      
                      <div v-else-if="!pane.recording" class="overlay-center">
                        <Icon icon="ep:video-pause" :size="64" />
                        <p class="window-label">窗口 {{ idx + 1 }}</p>
                        <p v-if="!pane.channelName" class="tip-text">拖拽摄像头到此处添加视频</p>
                        <p v-if="!pane.channelName" class="tip-text">或右键摄像头添加视频</p>
                        <p v-if="pane.channelName && !pane.hasRecording" class="tip-text">{{ pane.channelName }}</p>
                        <p v-if="pane.channelName && !pane.hasRecording" class="tip-text">当前时间范围内无录像</p>
                        <p v-if="pane.channelName && pane.hasRecording" class="tip-text">{{ pane.channelName }}</p>
                        <p v-if="pane.channelName && pane.hasRecording" class="tip-text">点击时间轴播放录像</p>
                      </div>
                      
                      <div v-else class="overlay-center playing">
                        <Icon icon="ep:video-play" :size="64" class="play-icon" />
                      </div>
                      
                      <div class="overlay-bottom">
                        <span v-if="pane.recording" class="device-name">{{ pane.recording.deviceName }}</span>
                        <span v-else-if="pane.channelName" class="device-name">
                          {{ pane.channelName }}
                          <span v-if="pane.hasRecording" class="status-indicator has-recording">●</span>
                          <span v-else class="status-indicator no-recording">●</span>
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
                
                <!-- 时间序列刻度 -->
                <div class="time-scale">
                  <div class="scale-marks">
                    <div
                      v-for="(mark, i) in scaleMarks"
                      :key="i"
                      class="scale-mark"
                      :class="{ long: mark.long }"
                      :style="{ left: mark.left + '%' }"
                    >
                      <span v-if="mark.long" class="mark-time">{{ mark.label }}</span>
                    </div>
                  </div>
                </div>
                <div class="timeline-container">
                  <!-- 时间轴 -->
                  <div class="timeline">
                    <div class="timeline-bar" @click="handleTimelineClick" @mousedown="startTimelineDrag">
                      <!-- 录像时间段 -->
                      <div
                        v-for="(seg, i) in timelineSegments"
                        :key="i"
                        class="timeline-seg"
                        :style="{ left: seg.left + '%', width: seg.width + '%' }"
                        :title="seg.label"
                      ></div>
                      <!-- 当前播放位置指示器 -->
                      <div 
                        class="timeline-cursor" 
                        :style="{ left: currentTimePercent + '%' }"
                        v-if="syncPlayback"
                      >
                        <div class="cursor-line"></div>
                        <div class="cursor-time">{{ currentTimeLabel }}</div>
                      </div>
                    </div>
                    
                  </div>
                  
                  <!-- 播放控制条 -->
                <div class="playback-controls">
                    <div class="controls-left">
                      <!-- 剪切进度显示（仅在剪切进行中显示） -->
                      <div v-if="currentCutTask && currentCutTask.status === 'cutting'" class="cut-progress-display">
                        <span class="progress-text">[剪切进度] {{ currentCutTask.progress }}%</span>
                      </div>
                      <el-button size="small" @click="showTimeCutDialog" title="剪切">
                        <Icon icon="ep:scissor" />
                      </el-button>
                      <el-button size="small" @click="handleScreenshot" title="截图">
                        <Icon icon="ep:camera" />
                      </el-button>
                      <el-popover placement="bottom" :width="280" trigger="click">
                        <template #reference>
                          <el-button size="small" title="截图设置">
                            <Icon icon="ep:setting" />
                          </el-button>
                        </template>
                        <div style="padding: 8px 0;">
                          <div style="margin-bottom: 12px;">
                            <el-checkbox v-model="screenshotSettings.uploadToServer">
                              截图自动上传到服务器
                            </el-checkbox>
                            <div style="font-size: 12px; color: #909399; margin-top: 4px; margin-left: 24px;">
                              开启后，截图会同时保存到本地和服务器
                            </div>
                          </div>
                        </div>
                      </el-popover>
                    </div>
                    <div class="controls-center">
                      <el-button size="small" @click="toggleSyncPlayback" :type="syncPlayback ? 'primary' : ''" title="同步回放">
                        <Icon icon="ep:connection" />
                      </el-button>
                      <el-button size="small" circle @click="handleBackward" title="后退">
                        <Icon icon="ep:d-arrow-left" />
                      </el-button>
                      <el-button size="small" circle type="primary" @click="togglePlayPause" title="播放/暂停">
                        <Icon :icon="isPlaying ? 'ep:video-pause' : 'ep:video-play'" />
                      </el-button>
                      <el-button size="small" circle @click="handleStop" title="停止">
                        <Icon icon="ep:close" />
                      </el-button>
                      <el-button size="small" circle @click="handleForward" title="前进">
                        <Icon icon="ep:d-arrow-right" />
                      </el-button>
                      <el-select v-model="playbackSpeed" size="small" style="width: 56px" @change="handleSpeedChange">
                        <el-option :value="0.5" label="0.5x" />
                        <el-option :value="1" label="1x" />
                        <el-option :value="2" label="2x" />
                        <el-option :value="4" label="4x" />
                        <el-option :value="8" label="8x" />
                      </el-select>
                      <el-button size="small" @click="toggleMuteAll" title="静音">
                        <Icon :icon="isMutedAll ? 'ep:mute' : 'ep:microphone'" />
                      </el-button>
                    </div>
                    <div class="controls-right">
                      <el-select
                        v-model="gridLayout"
                        size="small"
                        style="width: 96px"
                        @change="setLayout"
                        title="分屏布局"
                      >
                        <el-option :value="1" label="1×1" />
                        <el-option :value="4" label="2×2" />
                        <el-option :value="6" label="2×3" />
                        <el-option :value="9" label="3×3" />
                        <el-option :value="12" label="3×4" />
                        <el-option :value="16" label="4×4" />
                      </el-select>
                      <el-button size="small" @click="handleFullscreen" title="全屏">
                        <Icon icon="ep:full-screen" />
                      </el-button>
                    </div>
                </div>
                </div>
              </div>

            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 时间剪切对话框 -->
    <el-dialog v-model="timeCutDialogVisible" title="时间剪切" width="500px">
      <div class="time-cut-form">
        <div class="time-cut-info">
          <div class="info-item">
            <label>开始时间：</label>
            <el-date-picker
              v-model="timeCutForm.startTime"
              type="datetime"
              placeholder="选择开始时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </div>
          <div class="info-item">
            <label>结束时间：</label>
            <el-date-picker
              v-model="timeCutForm.endTime"
              type="datetime"
              placeholder="选择结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </div>
          <div class="info-item">
            <label>时长：</label>
            <span class="duration-display">{{ cutDuration }}</span>
          </div>
          <div class="info-item">
            <label>下载速度：</label>
              <el-select v-model="timeCutForm.downloadSpeed" style="width: 120px">
                <el-option :value="1" label="1x" />
                <el-option :value="2" label="2x" />
                <el-option :value="4" label="4x" />
                <el-option :value="8" label="8x" />
              </el-select>
          </div>
          <div class="info-item">
            <label>上传到服务器：</label>
            <el-switch v-model="timeCutForm.uploadToServer" />
            <span style="margin-left: 8px; font-size: 12px; color: #909399;">
              {{ timeCutForm.uploadToServer ? '剪切完成后自动上传' : '仅保存到本地' }}
            </span>
          </div>
        </div>
        
        <!-- 时间轴预览 -->
        <div class="time-cut-timeline">
          <div class="timeline-preview">
            <div 
              class="cut-range" 
              :style="{ 
                left: cutRangeLeft + '%', 
                width: cutRangeWidth + '%' 
              }"
            ></div>
          </div>
        </div>

        <!-- 剪切进度 -->
        <div v-if="currentCutTask" class="cut-progress" style="margin-top: 12px;">
          <el-progress :percentage="currentCutTask.progress" :status="currentCutTask.status === 'error' ? 'exception' : (currentCutTask.status === 'success' ? 'success' : undefined)" />
          <div class="progress-info" style="margin-top: 6px; display: flex; justify-content: space-between;">
            <span>{{ currentCutTask.channelName || ('通道' + currentCutTask.channelId) }}</span>
            <span>{{ currentCutTask.statusText }}</span>
          </div>
        </div>

        <!-- 剪切历史 -->
        <el-divider content-position="left">剪切历史</el-divider>
        <el-table :data="cutHistoryList" height="200px" style="width: 100%">
          <el-table-column prop="channelName" label="通道" width="120" />
          <el-table-column prop="startTime" label="开始时间" width="160" />
          <el-table-column prop="endTime" label="结束时间" width="160" />
          <el-table-column label="时长" width="100">
            <template #default="{ row }">{{ Math.floor(row.durationSec / 60) }}分{{ row.durationSec % 60 }}秒</template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.status === 'success'" type="success">已完成</el-tag>
              <el-tag v-else-if="row.status === 'cutting'" type="warning">剪切中</el-tag>
              <el-tag v-else-if="row.status === 'error'" type="danger">失败</el-tag>
              <el-tag v-else>待开始</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="timeCutDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleConfirmTimeCut"
          :disabled="!!currentCutTask && currentCutTask.status === 'cutting'"
        >
          {{ currentCutTask && currentCutTask.status === 'cutting' ? '剪切中...' : '确定剪切' }}
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 上传录像对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传录像" width="500px">
      <div class="upload-form">
        <div class="form-item">
          <label>摄像头</label>
          <el-input v-model="uploadForm.cameraId" placeholder="请先在左侧树选择摄像头" disabled />
        </div>
        <div class="form-item">
          <label>录像类型</label>
          <el-select v-model="uploadForm.recordingType">
            <el-option :value="1" label="手动录像" />
            <el-option :value="2" label="定时录像" />
            <el-option :value="3" label="报警触发" />
            <el-option :value="4" label="移动侦测" />
          </el-select>
        </div>
        <div class="form-item">
          <label>选择文件</label>
          <input type="file" accept="video/*" @change="handleUploadFileChange" />
        </div>
      </div>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="submitUpload">上传</el-button>
      </template>
    </el-dialog>
  </ContentWrap>
</template>

<script setup lang="ts">
/**
 * 文件说明：录像回放页面（参考 SmartPSS Plus 交互）
 * - 左侧为设备/NVR筛选与时间范围选择
 * - 右侧为多窗口播放器、时间轴与录像列表
 * - 使用 ZLMediaKit + mpegts.js 进行录像回放
 */
import { ref, reactive, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { ContentWrap } from '@/components/ContentWrap'
import Icon from '@/components/Icon/src/Icon.vue'
import mpegts from 'mpegts.js'

import {
  getCameraRecordingPage,
  deleteCameraRecording,
  uploadCameraRecording,
  uploadCameraSnapshot,
  type RecordingTimeSegmentVO
} from '@/api/iot/video'
import { getPlaybackUrl } from '@/api/iot/video/zlm'
import type { CameraRecordingRespVO } from '@/api/iot/video'
import { getBuildingList } from '@/api/iot/spatial/building'
import { getFloorListByBuildingId } from '@/api/iot/spatial/floor'
import { getAreaListByFloorId } from '@/api/iot/spatial/area'
import * as FloorDxfApi from '@/api/iot/spatial/floorDxf'
import { DeviceApi } from '@/api/iot/device/device'
import { getChannelPage, getDeviceTree, type DeviceTreeNode } from '@/api/iot/channel'

// Firefox 浏览器检测（用于 mpegts.js 配置优化）
const isFirefox = navigator.userAgent.toLowerCase().includes('firefox')

// 当前视图: 固定为 SmartPSS 风格
const currentView = ref<'smartpss'>('smartpss')

// 当前筛选TAB: 'space' | 'nvr'
const activeFilterTab = ref<'space' | 'nvr'>('space')

// 空间树相关（用于地图视图）
const spaceTreeData = ref<any[]>([])
const currentFloor = ref<any>(null)
const floorPlanSvg = ref('')
const mapContainerRef = ref<HTMLElement>()
const allDevices = ref<any[]>([])

// 摄像头树相关（用于列表视图的筛选面板）
const cameraTreeData = ref<any[]>([])
const cameraTreeRef = ref()

// 已移除 NVR 查询，统一从空间管理获取摄像头

// 树节点属性配置
const treeProps = {
  children: 'children',
  label: 'name',
  isLeaf: (data: any) => {
    return data.type === 'device' || data.type === 'channel'
  }
}

// 选中的通道列表
const selectedChannels = ref<any[]>([])

// 保持筛选表单中的 cameraIds 与选中通道同步
watch(
  () => selectedChannels.value,
  (chs) => {
    const rawIds = (chs || [])
      .map((c: any) => c?.channelId ?? c?.channel?.id ?? c?.id)
      .filter((id: any) => id != null)
    const normIds = rawIds
      .map((id: any) => {
        if (typeof id === 'string') {
          // 兼容树节点 id 为 'channel-123' 或 数字字符串 '123'
          if (id.startsWith('channel-')) return Number(id.slice('channel-'.length))
          const n = Number(id)
          return isNaN(n) ? undefined : n
        }
        return id
      })
      .filter((n: any) => typeof n === 'number' && !isNaN(n))
    filterForm.cameraIds = normIds as number[]
  },
  { deep: true }
)


// 筛选表单
const filterForm = reactive({
  timeRange: [] as string[],
  cameraId: undefined as number | undefined,
  cameraIds: [] as number[],  // 批量查询通道ID数组
  recordingTypes: [] as number[]
})

// 录像列表数据
const recordingList = ref<CameraRecordingRespVO[]>([])
const allRecordingList = ref<CameraRecordingRespVO[]>([]) // 用于时间轴显示的完整录像列表
const recordingTimeSegments = ref<RecordingTimeSegmentVO[]>([]) // 录像时间段分布数据
const currentRecording = ref<CameraRecordingRespVO | null>(null)
const loading = ref(false)
const total = ref(0)

// 分页
const pagination = reactive({
  page: 1,
  size: 10
})

// ======================== 播放控制状态 ========================
// 播放状态
const isPlaying = ref(false)
// 播放速度
const playbackSpeed = ref(1)
// 全局静音
const isMutedAll = ref(true)
// 同步回放开关
const syncPlayback = ref(false)
// 当前播放时间（毫秒）
const currentPlayTime = ref(0)

// ======================== 时间剪切 ========================
const timeCutDialogVisible = ref(false)
const timeCutForm = reactive({
  startTime: '',
  endTime: '',
  downloadSpeed: 8,
  uploadToServer: false  // 是否上传到服务器
})

// ======================== 截图设置 ========================
const screenshotSettings = reactive({
  uploadToServer: false  // 截图是否上传到服务器
})

// 剪切任务与历史（仅前端内存，不上传）
type CutTaskStatus = 'pending' | 'cutting' | 'success' | 'error'
interface CutTask {
  paneIndex: number
  channelId: number | null
  channelName: string | null
  startTime: string
  endTime: string
  durationSec: number
  progress: number
  status: CutTaskStatus
  statusText: string
  createdAt: string
  cutPlayer?: any
}

const cutTasks = reactive<{ [key: number]: CutTask | undefined }>({})
const currentCutTask = ref<CutTask | null>(null)
const cutHistoryList = ref<CutTask[]>([])

// ======================== SmartPSS 左侧面板状态 ========================
const deviceSearchKeyword = ref('')
const isSearchMode = ref(false) // 是否处于搜索模式
const recordingMode = ref('video')
const selectedRecordingType = ref(0)
const selectedTimeRange = ref(0)
// 已改为时间段选择（datetimerange），不再使用单日选择

// 上传录像对话框
const uploadDialogVisible = ref(false)
const uploadForm = reactive({
  cameraId: undefined as number | undefined,
  recordingType: 1 as number,
  file: null as File | null
})
const uploading = ref(false)

const handleUploadFileChange = (e: Event) => {
  const input = e.target as HTMLInputElement
  const files = input.files
  if (files && files.length > 0) {
    uploadForm.file = files[0]
  }
}

const submitUpload = async () => {
  if (!uploadForm.cameraId) {
    ElMessage.error('请在左侧选择一个摄像头')
    return
  }
  if (!uploadForm.file) {
    ElMessage.error('请选择要上传的录像文件')
    return
  }
  try {
    uploading.value = true
    await uploadCameraRecording(uploadForm.cameraId, uploadForm.file, uploadForm.recordingType)
    ElMessage.success('上传成功')
    uploadDialogVisible.value = false
    await loadRecordingData()
  } catch (error: any) {
    ElMessage.error('上传失败: ' + (error?.message || error))
  } finally {
    uploading.value = false
  }
}

// ======================== 多窗口播放器区域 ========================
/**
 * 播放布局：支持 1、4、6、9、16 窗口布局（SmartPSS Plus 风格）
 */
const gridLayout = ref<number>(6)

/**
 * 活动窗口索引（用户点击窗口切换）
 */
const activePane = ref<number>(0)

/**
 * 拖拽状态：当前拖拽悬停的窗口索引
 */
const dragOverPane = ref<number | null>(null)

/**
 * 播放窗口集合，每个窗口保存 mpegts/WebRTC 播放器相关数据
 */
const panes = ref<
  Array<{
    recording: CameraRecordingRespVO | null
    // mpegts.js / WebRTC 播放器相关
    player: mpegts.Player | null  // mpegts播放器实例
    pc: RTCPeerConnection | null  // WebRTC连接
    playMode: 'webrtc' | 'flv' | null  // 当前播放模式
    videoEl: HTMLVideoElement | null  // video元素
    isPlaying: boolean  // 是否正在播放
    isPaused: boolean  // 是否暂停
    isLoading: boolean  // 是否正在加载
    currentFile: any | null  // 当前播放的录像文件信息
    currentPlaySeconds: number  // 当前播放位置（秒）
    // 通道绑定相关
    channelId: number | null  // 绑定的通道ID
    channelNo: number | null  // 绑定的通道号
    channelName: string | null  // 绑定的通道名称
    hasRecording: boolean  // 该通道在当前时间范围内是否有录像
    // 录像时间范围
    playbackStartTime: string | null  // 回放开始时间
    playbackEndTime: string | null  // 回放结束时间
    // 浏览器端录制相关
    mediaRecorder: MediaRecorder | null
    recordedChunks: Blob[]
    // 其他
    muted: boolean
  }>
>([])

/**
 * 播放网格容器引用（用于全屏）
 */
const playerGridRef = ref<HTMLElement | null>(null)


/**
 * 计算网格布局类名
 */
const gridLayoutClass = computed<string>(() => {
  if (gridLayout.value === 1) return 'grid-1x1'
  if (gridLayout.value === 4) return 'grid-2x2'
  if (gridLayout.value === 6) return 'grid-2x3'
  if (gridLayout.value === 9) return 'grid-3x3'
  if (gridLayout.value === 12) return 'grid-3x4'
  if (gridLayout.value === 16) return 'grid-4x4'
  return 'grid-2x3'
})

/**
 * 初始化/变更布局时重置窗口集合
 * @param val 布局窗口数
 */
const setLayout = (val: number) => {
  // 切换布局前，先清空所有播放器，释放资源
  console.log('[布局切换] 清空所有播放器...')
  stopAllPlayers()
  
  gridLayout.value = val
  const count = val
  const arr: typeof panes.value = []
  for (let i = 0; i < count; i++) {
    arr.push({ 
      recording: null, 
      // mpegts/WebRTC 播放器相关
      player: null,
      pc: null,
      playMode: null,
      videoEl: null, 
      isPlaying: false,
      isPaused: false,
      isLoading: false,
      currentFile: null,
      currentPlaySeconds: 0,
      // 通道绑定相关
      channelId: null,
      channelNo: null,
      channelName: null,
      hasRecording: false,
      // 录像时间范围
      playbackStartTime: null,
      playbackEndTime: null,
      // 浏览器端录制相关
      mediaRecorder: null,
      recordedChunks: [],
      // 其他
      muted: true
    })
  }
  panes.value = arr
  activePane.value = 0
  
  console.log(`[布局切换] 已切换到 ${val} 窗口布局`)
}

/**
 * 绑定窗口 video 引用
 * @param el video 元素（模板传入时类型为 Element | ComponentPublicInstance | null）
 * @param idx 窗口索引
 */
const setPaneVideoRef = (el: any, idx: number) => {
  if (!el || idx < 0 || !panes.value[idx]) return
  if (el instanceof HTMLVideoElement) {
    panes.value[idx].videoEl = el
  }
}

// 已移除大华SDK相关的canvas/loading/ivs引用函数

/**
 * 获取活动窗口的 video 元素
 * @returns HTMLVideoElement | null
 */
const getActiveVideoEl = (): HTMLVideoElement | null => {
  const pane = panes.value[activePane.value]
  return pane?.videoEl || null
}

/**
 * 播放指定录像到某窗口
 * @param recording 录像记录
 * @param paneIndex 目标窗口索引（默认活动窗口）
 */
const playRecordingInPane = (recording: CameraRecordingRespVO, paneIndex?: number) => {
  const idx = typeof paneIndex === 'number' ? paneIndex : activePane.value
  const pane = panes.value[idx]
  if (!pane) return
  const video = pane.videoEl
  if (!video) {
    ElMessage.warning('视频窗口尚未就绪，请稍后再试')
    return
  }
  pane.recording = recording
  try {
    // 简单兼容：直接设置 src（若为 HLS/FLV 等协议，需后续接入对应库）
    const url = (recording as any).fileUrl || ''
    if (!url) {
      ElMessage.warning('该录像缺少可播放的文件地址')
      return
    }
    video.src = url
    video.play().catch(() => {})
  } catch (e: any) {
    ElMessage.error('载入视频失败：' + (e?.message || e))
  }
}

/**
 * 切换窗口播放/暂停
 * @param paneIndex 窗口索引
 */
const togglePanePlayPause = (paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (!pane || !pane.isPlaying) return
  
  const videoEl = pane.videoEl
  if (!videoEl) return
  
  try {
    if (pane.isPaused) {
      // 恢复播放
      videoEl.play().catch(e => console.warn('恢复播放失败:', e))
      pane.isPaused = false
      console.log(`[窗口${paneIndex + 1}] 恢复播放`)
    } else {
      // 暂停播放
      videoEl.pause()
      pane.isPaused = true
      console.log(`[窗口${paneIndex + 1}] 暂停播放`)
    }
  } catch (error) {
    console.error(`[窗口${paneIndex + 1}] 播放控制失败:`, error)
  }
}

/**
 * 全屏播放网格或活动窗口
 */
const handleFullscreen = () => {
  const el = playerGridRef.value || getActiveVideoEl()
  const target: any = el
  if (target && target.requestFullscreen) {
    target.requestFullscreen()
  }
}

// ======================== 拖拽功能 ========================
/**
 * 开始拖拽通道
 */
const handleDragStart = (event: DragEvent, data: any) => {
  if (data.type !== 'channel') return
  
  // 设置拖拽数据
  if (event.dataTransfer) {
    event.dataTransfer.setData('application/json', JSON.stringify(data))
    event.dataTransfer.effectAllowed = 'copy'
  }
  
  console.log('[录像回放] 开始拖拽通道:', data.name)
}

/**
 * 拖拽悬停在播放窗口上
 */
const handleDragOver = (event: DragEvent, paneIndex: number) => {
  event.preventDefault()
  dragOverPane.value = paneIndex
}

/**
 * 拖拽离开播放窗口
 */
const handleDragLeave = () => {
  dragOverPane.value = null
}

/**
 * 拖拽放置到播放窗口
 */
const handleDrop = async (event: DragEvent, paneIndex: number) => {
  event.preventDefault()
  dragOverPane.value = null
  
  try {
    const dataStr = event.dataTransfer?.getData('application/json')
    if (!dataStr) return
    
    const channelData = JSON.parse(dataStr)
    if (channelData.type !== 'channel') return
    
    console.log('[录像回放] 拖拽通道到窗口', paneIndex + 1, ':', channelData.name)
    
    const pane = panes.value[paneIndex]
    if (!pane) return
    
    // 绑定通道到窗口
    pane.channelId = channelData.channelId
    // 大华通道号：尝试多种可能的字段名
    pane.channelNo = channelData.channelNo || 
                     channelData.channel_no || 
                     channelData.channelNumber ||
                     (channelData.channel && channelData.channel.channelNo) ||
                     null
    pane.channelName = channelData.name
    
    console.log('[录像回放] 通道绑定信息:', {
      channelId: pane.channelId,
      channelNo: pane.channelNo,
      channelName: pane.channelName,
      '原始channelData': channelData, // 打印完整的通道数据，用于调试
      '可用的channelNo字段': {
        'channelData.channelNo': channelData.channelNo,
        'channelData.channel_no': channelData.channel_no,
        'channelData.channelNumber': channelData.channelNumber,
        'channelData.channel': channelData.channel,
        'channelData.channel.channelNo': channelData.channel?.channelNo,
        '最终使用的channelNo': pane.channelNo
      }
    })
    
    // 检查该通道在当前时间范围内是否有录像
    const hasRecording = await checkChannelRecording(channelData.channelId, pane.channelNo)
    pane.hasRecording = hasRecording
    
    if (hasRecording) {
      ElMessage.success(`已将 ${channelData.name} 绑定到窗口 ${paneIndex + 1}，点击时间轴可播放录像`)
    } else {
      ElMessage.warning(`通道 ${channelData.name} 在当前时间范围内无录像记录`)
    }
  } catch (error) {
    console.error('[录像回放] 拖拽处理失败:', error)
    ElMessage.error('拖拽操作失败')
  }
}

/**
 * 检查指定通道在当前时间范围内是否有录像
 * 注：由于使用 ZLMediaKit，录像检查将在播放时动态进行
 * 这里暂时返回 true，让用户点击后再查询是否有录像
 */
const checkChannelRecording = async (channelId: number, _channelNo?: number | null): Promise<boolean> => {
  try {
    if (!filterForm.timeRange || filterForm.timeRange.length !== 2) {
      return false
    }

    console.log(`[录像检查] 通道 ${channelId} 录像检查（ZLMediaKit模式）`)
    
    // ZLMediaKit 模式下，录像检查将在播放时动态进行
    // 这里暂时返回 true，表示假设有录像
    return true

  } catch (error: any) {
    console.error(`[录像检查] 通道 ${channelId} 检查失败:`, error)
    return false
  }
}

/**
 * 搜索视频通道（根据通道名称模糊查询）
 */
const handleChannelSearch = async () => {
  const keyword = deviceSearchKeyword.value.trim()
  
  if (!keyword) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  try {
    console.log('[录像回放] 🔍 搜索通道:', keyword)
    
    // 使用通道分页查询接口进行搜索
    const result = await getChannelPage({
      channelName: keyword,
      channelType: 'video',
      pageNo: 1,
      pageSize: 100
    })
    
    if (result.list && result.list.length > 0) {
      // 将搜索结果转换为树节点格式
      cameraTreeData.value = result.list.map((channel: any) => ({
        id: `channel-${channel.id}`,
        name: channel.channelName,
        type: 'channel',
        channelId: channel.id,
        channelNo: channel.channelNo,
        deviceId: channel.deviceId,
        channel: channel
      }))
      
      isSearchMode.value = true
      ElMessage.success(`找到 ${result.list.length} 个匹配的视频通道`)
      console.log(`[录像回放] ✅ 搜索完成，找到 ${result.list.length} 个通道`)
    } else {
      cameraTreeData.value = []
      ElMessage.info('未找到匹配的视频通道')
      console.log('[录像回放] ℹ️ 未找到匹配的通道')
    }
  } catch (error: any) {
    console.error('[录像回放] ❌ 搜索失败:', error)
    ElMessage.error('搜索失败: ' + (error?.message || '未知错误'))
  }
}

/**
 * 清除搜索
 */
const handleSearchClear = () => {
  deviceSearchKeyword.value = ''
  isSearchMode.value = false
  // 重新加载完整树
  loadSpaceTree()
  console.log('[录像回放] 🔄 已清除搜索，重新加载树')
}

/**
 * 加载空间树（初始只加载建筑列表）
 */
const loadSpaceTree = async () => {
  try {
    console.log('[录像回放] 🔄 开始加载空间树...')

    // 优先使用设备树API
    if (false) {
    try {
      const deviceTree = await getDeviceTree({
        channelType: 'video' // 只查询视频通道
      })
      
      if (deviceTree && deviceTree.length > 0) {
        // 转换设备树格式为页面需要的格式
        const treeData = deviceTree.map((node: DeviceTreeNode) => ({
          id: node.id,
          name: node.label,
          type: node.type === 'device' ? 'device' : 'channel',
          deviceId: node.deviceId,
          channelId: node.channelId,
          channelNo: node.channelNo,
          children: node.children?.map((child: DeviceTreeNode) => ({
            id: child.id,
            name: child.label,
            type: child.type === 'device' ? 'device' : 'channel',
            deviceId: child.deviceId,
            channelId: child.channelId,
            channelNo: child.channelNo,
            channel: child.raw
          }))
        }))
        
        cameraTreeData.value = treeData
        console.log(`[录像回放] ✅ 已加载设备树，共 ${treeData.length} 个设备`)
        return
      }
    } catch (apiError) {
      console.warn('[录像回放] ⚠️ 设备树API调用失败，回退到空间树:', apiError)
    }
    }

    // 回退方案：使用空间树
    const buildings = await getBuildingList()

    const treeData = buildings.map((building: any) => ({
      id: `building-${building.id}`,
      name: building.name,
      type: 'building',
      buildingId: building.id
    }))

    // 用于地图视图
    spaceTreeData.value = treeData
    // 用于列表视图的筛选面板
    cameraTreeData.value = treeData

    console.log(`[录像回放] ✅ 已加载 ${treeData.length} 个建筑`)
  } catch (error: any) {
    console.error('[录像回放] ❌ 加载失败:', error)
    const errorMsg = error?.message || error || '未知错误'
    ElMessage.error('加载空间树失败: ' + errorMsg)
  }
}

/**
 * 懒加载树节点（地图视图和列表视图共用）
 */
const loadTreeNode = async (node: any, resolve: Function) => {
  try {
    const data = node.data
    let children: any[] = []

    if (data.type === 'building') {
      // 添加"通道"节点
      children.push({
        id: `channels-building-${data.buildingId}`,
        name: '通道',
        type: 'channels',
        buildingId: data.buildingId
      })
      
      // 添加楼层节点
      const floors = await getFloorListByBuildingId(data.buildingId)
      children.push(...floors.map((floor: any) => ({
        id: `floor-${floor.id}`,
        name: floor.name,
        type: 'floor',
        floorId: floor.id,
        buildingId: data.buildingId,
        floor: floor
      })))
    } else if (data.type === 'floor') {
      // 添加"通道"节点
      children.push({
        id: `channels-floor-${data.floorId}`,
        name: '通道',
        type: 'channels',
        floorId: data.floorId,
        buildingId: data.buildingId
      })
      
      // 添加区域节点
      const areas = await getAreaListByFloorId(data.floorId)
      children.push(...areas.map((area: any) => ({
        id: `area-${area.id}`,
        name: area.name,
        type: 'area',
        areaId: area.id,
        floorId: data.floorId
      })))
    } else if (data.type === 'area') {
      // 添加"通道"节点
      children.push({
        id: `channels-area-${data.areaId}`,
        name: '通道',
        type: 'channels',
        areaId: data.areaId,
        floorId: data.floorId,
        buildingId: data.buildingId
      })
    } else if (data.type === 'channels') {
      // 加载通道列表
      const params: any = {
        pageNo: 1,
        pageSize: 100
      }
      
      // 根据层级添加筛选条件
      if (data.buildingId) params.buildingId = data.buildingId
      if (data.floorId) params.floorId = data.floorId
      if (data.areaId) params.areaId = data.areaId
      if (data.spaceId) params.spaceId = data.spaceId
      
      const channelsRes = await getChannelPage(params)
      const channels = channelsRes.list || []
      
      children = channels.map((ch: any) => ({
        id: `channel-${ch.id}`,
        name: ch.channelName || `通道${ch.channelNo}`,
        type: 'channel',
        channelId: ch.id,
        channelNo: ch.channelNo, // 添加 channelNo 字段，用于大华RPC查询
        channel: ch
      }))
    }

    resolve(children)
  } catch (error: any) {
    console.error('[录像回放] ❌ 懒加载失败:', error)
    const errorMsg = error?.message || error || '未知错误'
    ElMessage.error('加载节点失败: ' + errorMsg)
    resolve([])
  }
}

/**
 * 加载录像数据
 */
const loadRecordingData = async () => {
  try {
    loading.value = true

    console.log('🔍 [DEBUG] filterForm.cameraIds:', filterForm.cameraIds)
    console.log('🔍 [DEBUG] selectedChannels.value:', selectedChannels.value)

    // 兜底：如果 filterForm.cameraIds 为空，则从 selectedChannels 中推导
    const selectedIds = (selectedChannels.value && selectedChannels.value.length > 0)
      ? selectedChannels.value
          .map((c: any) => c?.channelId ?? c?.channel?.id ?? c?.id)
          .filter((id: any) => id != null)
      : []

    console.log('🔍 [DEBUG] selectedIds:', selectedIds)

    const effectiveCameraIds = (filterForm.cameraIds && filterForm.cameraIds.length > 0)
      ? filterForm.cameraIds
      : selectedIds

    console.log('🔍 [DEBUG] effectiveCameraIds:', effectiveCameraIds)

    const baseParams = {
      cameraId: filterForm.cameraId,
      cameraIds: effectiveCameraIds && effectiveCameraIds.length > 0 ? effectiveCameraIds : undefined,
      recordingType:
        filterForm.recordingTypes.length > 0 ? filterForm.recordingTypes[0] : undefined,
      startTime:
        filterForm.timeRange && filterForm.timeRange[0] ? filterForm.timeRange[0] : undefined,
      endTime: filterForm.timeRange && filterForm.timeRange[1] ? filterForm.timeRange[1] : undefined
    }

    // 1. 加载分页数据（用于列表显示）
    const paginatedParams = {
      ...baseParams,
      pageNo: pagination.page,
      pageSize: pagination.size
    }

    console.log('[录像回放] 分页查询参数:', paginatedParams)
    const paginatedRes = await getCameraRecordingPage(paginatedParams)

    recordingList.value = paginatedRes.list || []
    total.value = paginatedRes.total || 0

    // 2. 加载完整数据（用于时间轴显示）- 分批加载避免超过限制
    let allRecordings: any[] = []
    let currentPage = 1
    const maxPageSize = 100 // 后端限制的最大页面大小
    
    while (true) {
      const allParams = {
        ...baseParams,
        pageNo: currentPage,
        pageSize: maxPageSize
      }

      console.log('[录像回放] 完整查询参数 (页面', currentPage, '):', allParams)
      const allRes = await getCameraRecordingPage(allParams)
      const currentList = allRes.list || []
      
      allRecordings.push(...currentList)
      
      // 如果当前页的记录数少于最大页面大小，说明已经是最后一页
      if (currentList.length < maxPageSize) {
        break
      }
      
      currentPage++
      
      // 安全限制：最多加载10页，避免无限循环
      if (currentPage > 10) {
        console.warn('[录像回放] 达到最大页面限制，停止加载')
        break
      }
    }
    
    allRecordingList.value = allRecordings

    if (recordingList.value.length === 0) {
      ElMessage.info('未找到录像记录')
    } else {
      console.log('[录像回放] ✅ 加载成功:', recordingList.value.length, '条录像（分页）')
      console.log('[录像回放] ✅ 时间轴录像:', allRecordingList.value.length, '条录像（完整）')
    }
  } catch (error: any) {
    console.error('[录像回放] ❌ 加载失败:', error)
    const errorMsg = error?.message || error || '未知错误'
    ElMessage.error('加载录像数据失败: ' + errorMsg)
    recordingList.value = []
    allRecordingList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/**
 * 搜索录像数据（纯RPC方式）
 */
const handleSearch = async () => {
  // 简单的时间范围验证
  if (!filterForm.timeRange || !filterForm.timeRange[0] || !filterForm.timeRange[1]) {
    ElMessage.warning('请选择时间范围')
    return
  }
  
  loading.value = true
  try {
    // 清空所有正在播放的窗口，释放资源
    console.log('[录像搜索] 清空所有播放器...')
    stopAllPlayers()

    // 获取选中的通道ID列表
    const channelIds = getSelectedChannelIds()
    console.log('[录像搜索] 选中通道:', channelIds)
    if (channelIds.length === 0) {
      ElMessage.warning('请先选择要查询的通道')
      return
    }

    console.log('[录像搜索] 开始搜索录像数据（ZLMediaKit模式）')
    console.log('[录像搜索] 选中通道:', channelIds)
    console.log('[录像搜索] 时间范围:', filterForm.timeRange)

    // ZLMediaKit 模式下，不预先查询录像文件
    // 而是为每个通道创建一个表示整个时间段的 segment
    const startTime = new Date(filterForm.timeRange[0])
    const endTime = new Date(filterForm.timeRange[1])
    
    const timeSegments: RecordingTimeSegmentVO[] = channelIds.map(channelId => {
      // 获取通道信息
      const pane = panes.value.find(p => p.channelId === channelId)
      return {
        channelId,
        channelName: pane?.channelName || `通道 ${channelId}`,
        deviceName: 'NVR',
        segments: [{
          startTime: startTime.toISOString(),
          endTime: endTime.toISOString(),
          hasRecording: true  // 假设有录像，播放时再检查
        }]
      }
    })

    recordingTimeSegments.value = timeSegments
    
    // 自动分配通道到播放窗口（按顺序从左到右）
    await autoAssignChannelsToWindows(channelIds)
    
    console.log('[录像搜索] ✅ 搜索完成（ZLMediaKit模式）')
    console.log('[录像搜索] 已分配通道数:', timeSegments.length)
    
    ElMessage.success(`已选择 ${channelIds.length} 个通道，点击时间轴播放录像`)
  } catch (error: any) {
    console.error('[录像搜索] ❌ 搜索失败:', error)
    ElMessage.error(`搜索失败: ${error.message}`)
    recordingTimeSegments.value = []
  } finally {
    loading.value = false
  }
}
 

/**
 * 停止所有播放器，释放资源
 */
const stopAllPlayers = () => {
  console.log('[清空播放器] 开始清空所有播放窗口...')
  
  panes.value.forEach((pane, index) => {
    // 停止 mpegts 播放器
    if (pane.player) {
      try {
        console.log(`[清空播放器] 关闭窗口 ${index + 1} 的 mpegts 播放器`)
        pane.player.pause()
        pane.player.unload()
        pane.player.detachMediaElement()
        pane.player.destroy()
        pane.player = null
      } catch (error) {
        console.warn(`[清空播放器] 窗口 ${index + 1} mpegts 关闭失败:`, error)
      }
    }
    
    // 关闭 WebRTC 连接
    if (pane.pc) {
      try {
        console.log(`[清空播放器] 关闭窗口 ${index + 1} 的 WebRTC 连接`)
        pane.pc.close()
        pane.pc = null
      } catch (error) {
        console.warn(`[清空播放器] 窗口 ${index + 1} WebRTC 关闭失败:`, error)
      }
    }
    
    // 重置 video 元素
    if (pane.videoEl) {
      pane.videoEl.pause()
      pane.videoEl.srcObject = null
      pane.videoEl.src = ''
    }
    
    // 重置窗口状态
    pane.isPlaying = false
    pane.isPaused = false
    pane.isLoading = false
    pane.currentFile = null
    pane.playMode = null
    pane.channelId = null
    pane.channelNo = null
    pane.channelName = null
    pane.hasRecording = false
    pane.playbackStartTime = null
    pane.playbackEndTime = null
  })
  
  console.log('[清空播放器] ✅ 所有播放器已清空')
}

/**
 * 自动分配通道到播放窗口（按顺序从左到右）
 */
const autoAssignChannelsToWindows = async (channelIds: number[]) => {
  console.log('[自动分配] 开始分配通道到播放窗口:', channelIds)
  
  // 注意：不需要再次清空，因为handleSearch中已经调用了stopAllPlayers()
  
  // 重新设置通道绑定（不清空播放器，已在stopAllPlayers中处理）
  // 按顺序分配通道到窗口
  for (let i = 0; i < Math.min(channelIds.length, panes.value.length); i++) {
    const channelId = channelIds[i]
    const pane = panes.value[i]
    
    try {
      // 获取通道详细信息
      pane.channelId = channelId
      // 临时测试：直接设置channelNo为1、2、3...（按顺序）
      pane.channelNo = i + 1
      pane.channelName = `通道 ${i + 1}`
      
      console.log(`[自动分配] 🧪 测试模式 - 窗口 ${i + 1} 分配通道ID ${channelId}，使用测试channelNo: ${pane.channelNo}`)
      
      // 检查该通道是否有录像
      const hasRecording = await checkChannelRecording(channelId, pane.channelNo)
      pane.hasRecording = hasRecording
      
      console.log(`[自动分配] ✅ 窗口 ${i + 1} 分配完成，channelNo=${pane.channelNo}，有录像: ${hasRecording}`)
    } catch (error) {
      console.warn(`[自动分配] 窗口 ${i + 1} 分配通道 ${channelId} 失败:`, error)
    }
  }
  
  const assignedCount = Math.min(channelIds.length, panes.value.length)
  console.log(`[自动分配] ✅ 完成，已分配 ${assignedCount} 个通道到播放窗口`)
}

/**
 * 获取选中的通道ID列表
 */
const getSelectedChannelIds = (): number[] => {
   // 优先使用 filterForm.cameraIds
  if (filterForm.cameraIds && filterForm.cameraIds.length > 0) {
    return filterForm.cameraIds
  }
  // 其次使用 selectedChannels
  if (selectedChannels.value && selectedChannels.value.length > 0) {
    return selectedChannels.value
      .map((c: any) => c?.channelId ?? c?.channel?.id ?? c?.id)
      .filter((id: any) => id != null)
  }
  // 最后使用单个 cameraId
  if (filterForm.cameraId != null) {
    return [filterForm.cameraId]
  }
  return []
}

/**
 * 根据channelId获取完整的通道信息（包括channel_no）
 */
const getChannelInfo = (channelId: number) => {
  // 从 selectedChannels 中查找
  const channel = selectedChannels.value.find((c: any) => c.channelId === channelId)
  if (channel) {
    return {
      channelId: channel.channelId,
      channelNo: channel.channel_no || channel.channelNo,
      name: channel.name,
      deviceId: channel.deviceId
    }
  }
  return null
}

/**
 * 刷新
 */
const handleRefresh = () => {
  handleSearch()
}

/**
 * 处理摄像头选择（列表视图筛选面板）
 */
const handleCameraSelect = (data: any) => {
  console.log('[录像回放] 选择节点:', data)
  if (data.type === 'device') {
    filterForm.cameraId = data.deviceId
    // 禁用自动后端API调用
    // loadRecordingData()
  } else if (data.type === 'channel') {
    // 点击通道节点，使用通道ID查询录像
    filterForm.cameraId = data.channelId
    // 同步到批量查询数组，保证任意触发 load 都能带上通道
    filterForm.cameraIds = data.channelId != null ? [data.channelId] : []
    // 禁用自动后端API调用
    // loadRecordingData()
  }
}

/**
 * 处理通道复选框选中事件
 */
const handleChannelCheck = async (data: any, checked: any) => {
  console.log('[录像回放] 复选框变化:', data, checked)
  
  // 获取所有选中的节点
  const checkedNodes = cameraTreeRef.value?.getCheckedNodes() || []
  
  // 过滤出通道类型的节点，同时处理 channels 父节点
  const channelsMap = new Map() // 用 Map 去重
  
  for (const node of checkedNodes) {
    if (node.type === 'channel') {
      // 直接的通道节点
      if (node.channelId && !channelsMap.has(node.channelId)) {
        channelsMap.set(node.channelId, node)
      }
    } else if (node.type === 'channels') {
      // 通道分组节点，需要获取其子节点
      console.log('🔍 [DEBUG] 发现 channels 分组节点:', node)
      
      // 检查子节点是否已加载
      const treeNode = cameraTreeRef.value?.getNode(node.id)
      if (treeNode && treeNode.childNodes && treeNode.childNodes.length > 0) {
        // 子节点已加载
        const childChannels = treeNode.childNodes
          .map((child: any) => child.data)
          .filter((childData: any) => childData.type === 'channel')
        
        console.log('🔍 [DEBUG] 从树获取子通道节点:', childChannels)
        childChannels.forEach((ch: any) => {
          if (ch.channelId && !channelsMap.has(ch.channelId)) {
            channelsMap.set(ch.channelId, ch)
          }
        })
      } else {
        // 子节点未加载，自动展开节点
        console.log('🔍 [DEBUG] 子节点未加载，自动展开节点...')
        if (treeNode) {
          treeNode.expand(() => {
            console.log('🔍 [DEBUG] 节点已展开，重新触发复选框检查')
            // 展开后重新执行检查
            setTimeout(() => handleChannelCheck(data, checked), 100)
          })
        }
      }
    }
  }
  
  // 转为数组
  const channels = Array.from(channelsMap.values())
  selectedChannels.value = channels
  
  console.log('[录像回放] 已选中通道:', channels.length, '个', channels.map((c: any) => c.name))
  console.log('[录像回放] 通道详细信息:', channels.map((c: any) => ({
    channelId: c.channelId,
    channelNo: c.channel_no || c.channelNo,
    name: c.name
  })))
  
  if (channels.length > 0) {
    // 提取所有通道ID
    const channelIds = channels
      .map((c: any) => c.channelId)
      .filter((id: any) => id != null)
    
    console.log('🔍 [DEBUG] handleChannelCheck channelIds:', channelIds)
    
    if (channelIds.length > 0) {
      // 批量查询多个通道的录像
      filterForm.cameraId = undefined  // 清空单个ID
      filterForm.cameraIds = channelIds  // 设置ID数组
      
      console.log('🔍 [DEBUG] 设置 filterForm.cameraIds:', filterForm.cameraIds)
      
      // 禁用自动后端API调用，改为手动点击搜索按钮触发RPC查询
      // loadRecordingData()
    }
  } else {
    // 取消所有选择，清空录像列表
    filterForm.cameraId = undefined
    filterForm.cameraIds = []
    recordingList.value = []
    total.value = 0
  }
}


/**
 * 截图功能 - 支持本地保存和上传到服务器
 */
const handleScreenshot = async () => {
  const pane = panes.value[activePane.value]
  if (!pane) {
    ElMessage.warning('请先选择一个窗口')
    return
  }
  
  if (!pane.isPlaying || !pane.player) {
    ElMessage.warning('当前窗口没有正在播放的视频')
    return
  }
  
  if (!pane.channelId || !pane.channelNo) {
    ElMessage.warning('当前窗口未绑定通道')
    return
  }
  
  try {
    console.log('[截图] 开始截图:', {
      channelId: pane.channelId,
      channelNo: pane.channelNo,
      channelName: pane.channelName,
      uploadToServer: screenshotSettings.uploadToServer
    })
    
    const captureTime = new Date()
    const fileName = `snapshot_${pane.channelName || 'channel'}_${captureTime.getTime()}`
    
    // 1. 使用播放器的capture方法截图（自动下载到本地）
    if (typeof pane.player.capture === 'function') {
      pane.player.capture(fileName)
      console.log('[截图] 本地保存命令已发送')
    }
    
    // 2. 如果需要上传到服务器，从Video获取图片数据
    if (screenshotSettings.uploadToServer) {
      console.log('[截图] 准备上传到服务器...')
      
      let imageDataUrl: string
      if (pane.videoEl) {
        // 从Video元素截图
        const canvas = document.createElement('canvas')
        canvas.width = pane.videoEl.videoWidth || 1920
        canvas.height = pane.videoEl.videoHeight || 1080
        const ctx = canvas.getContext('2d')
        if (!ctx) {
          throw new Error('无法创建Canvas上下文')
        }
        ctx.drawImage(pane.videoEl, 0, 0, canvas.width, canvas.height)
        imageDataUrl = canvas.toDataURL('image/jpeg', 0.9)
      } else {
        throw new Error('无法获取视频画面')
      }
      
      // 转换为Blob并上传
      const blob = await (await fetch(imageDataUrl)).blob()
      const file = new File([blob], `${fileName}.jpg`, { type: 'image/jpeg' })
      
      await uploadCameraSnapshot(
        pane.channelId,
        file,
        1  // snapshotType: 1-手动抓图
      )
      
      console.log('[截图] 已上传到服务器')
      ElMessage.success('截图成功，已保存到本地并上传到服务器')
    } else {
      ElMessage.success('截图成功，文件已保存到本地下载目录')
    }
    
  } catch (error: any) {
    console.error('[截图] 失败:', error)
    ElMessage.error('截图失败：' + (error?.message || error))
  }
}

// ======================== 时间轴（基于录像列表） ========================
/**
 * 时间轴范围：使用筛选时间范围，否则使用当天 00:00-23:59
 */
const timelineStart = computed<number>(() => {
  if (filterForm.timeRange && filterForm.timeRange[0]) {
    return new Date(filterForm.timeRange[0]).getTime()
  }
  const d = new Date()
  d.setHours(0, 0, 0, 0)
  return d.getTime()
})

const timelineEnd = computed<number>(() => {
  if (filterForm.timeRange && filterForm.timeRange[1]) {
    return new Date(filterForm.timeRange[1]).getTime()
  }
  const d = new Date()
  d.setHours(23, 59, 59, 999)
  return d.getTime()
})

const scaleMarks = computed<Array<{ left: number; label: string; long: boolean }>>(() => {
  const start = timelineStart.value
  const end = timelineEnd.value
  const total = Math.max(end - start, 1)
  const majors = 12
  const minorsBetween = 3
  const res: Array<{ left: number; label: string; long: boolean }> = []
  for (let j = 0; j <= majors; j++) {
    const leftMajor = (j / majors) * 100
    const tMajor = start + (j / majors) * total
    const date = new Date(tMajor)
    const hh = String(date.getHours()).padStart(2, '0')
    const mm = String(date.getMinutes()).padStart(2, '0')
    res.push({ left: leftMajor, label: `${hh}:${mm}`, long: true })
    if (j < majors) {
      for (let k = 1; k <= minorsBetween; k++) {
        const leftMinor = ((j + k / (minorsBetween + 1)) / majors) * 100
        res.push({ left: leftMinor, label: '', long: false })
      }
    }
  }
  return res
})

/**
 * 时间轴片段：基于录像时间段分布数据生成时间轴片段
 */
const timelineSegments = computed((): { left: number; width: number; label: string }[] => {
  if (!recordingTimeSegments.value.length) return []
  
  const segments: { left: number; width: number; label: string }[] = []
  const timeRangeMs = new Date(filterForm.timeRange[1]).getTime() - new Date(filterForm.timeRange[0]).getTime()
  
  // 合并所有通道的录像时间段
  const allRecordingIntervals: { start: number; end: number }[] = []
  recordingTimeSegments.value.forEach(channel => {
    channel.segments.forEach(segment => {
      if (segment.hasRecording) {
        allRecordingIntervals.push({
          start: new Date(segment.startTime).getTime(),
          end: new Date(segment.endTime).getTime()
        })
      }
    })
  })
  
  // 按开始时间排序
  allRecordingIntervals.sort((a, b) => a.start - b.start)
  
  // 合并重叠的时间段
  const mergedIntervals: { start: number; end: number }[] = []
  for (const interval of allRecordingIntervals) {
    if (mergedIntervals.length === 0 || mergedIntervals[mergedIntervals.length - 1].end < interval.start) {
      mergedIntervals.push(interval)
    } else {
      mergedIntervals[mergedIntervals.length - 1].end = Math.max(mergedIntervals[mergedIntervals.length - 1].end, interval.end)
    }
  }
  
  // 转换为时间轴片段
  const startTime = new Date(filterForm.timeRange[0]).getTime()
  mergedIntervals.forEach(interval => {
    const left = ((interval.start - startTime) / timeRangeMs) * 100
    const width = ((interval.end - interval.start) / timeRangeMs) * 100
    
    // 过滤掉无效的片段
    if (left >= 0 && left <= 100 && width > 0) {
      segments.push({
        left: Math.max(0, left),
        width: Math.min(width, 100 - Math.max(0, left)),
        label: `${new Date(interval.start).toLocaleTimeString()} ~ ${new Date(interval.end).toLocaleTimeString()}`
      })
    }
  })
  
  return segments
})


/**
 * 时间轴点击事件
 */
const handleTimelineClick = async (event: MouseEvent) => {
  const timeline = event.currentTarget as HTMLElement
  const rect = timeline.getBoundingClientRect()
  const clickX = event.clientX - rect.left
  const percent = clickX / rect.width
  
  // 计算点击的时间
  if (!filterForm.timeRange || filterForm.timeRange.length !== 2) return
  
  const startTime = new Date(filterForm.timeRange[0]).getTime()
  const endTime = new Date(filterForm.timeRange[1]).getTime()
  const clickTime = new Date(startTime + (endTime - startTime) * percent)
  
  console.log('[录像回放] 时间轴点击:', {
    percent: (percent * 100).toFixed(2) + '%',
    clickTime: clickTime.toISOString()
  })
  
  // 更新当前播放时间
  currentPlayTime.value = clickTime.getTime()
  
  // 只播放当前活动窗口
  const pane = panes.value[activePane.value]
  
  if (!pane) {
    ElMessage.warning('请先选择一个播放窗口')
    return
  }
  
  // 调试信息：显示窗口状态
  console.log(`[录像回放] 活动窗口 ${activePane.value + 1} 状态检查:`, {
    channelId: pane.channelId,
    channelNo: pane.channelNo,
    hasRecording: pane.hasRecording,
    '满足播放条件': !!(pane.channelId && pane.hasRecording)
  })
  
  // 检查窗口是否绑定了通道且有录像
  if (!pane.channelId) {
    ElMessage.warning(`窗口 ${activePane.value + 1} 未绑定通道，请先拖拽通道到窗口`)
    return
  }
  
  // 检查 channelNo 是否有效
  if (pane.channelNo === null || pane.channelNo === undefined) {
    ElMessage.error(`窗口 ${activePane.value + 1} 通道号未设置，无法播放录像`)
    console.error('[录像回放] channelNo 未定义:', {
      channelId: pane.channelId,
      channelNo: pane.channelNo,
      channelName: pane.channelName
    })
    return
  }
  
  if (!pane.hasRecording) {
    ElMessage.warning(`窗口 ${activePane.value + 1} (${pane.channelName || '通道' + pane.channelNo}) 在当前时间范围内无录像`)
    return
  }
  
  // 播放当前活动窗口的录像
  try {
    await playRecordingAtTime(activePane.value, pane.channelId!, clickTime)
    console.log(`[录像回放] ✅ 窗口 ${activePane.value + 1} 播放成功，通道ID: ${pane.channelId}`)
  } catch (error) {
    console.error(`[录像回放] ❌ 窗口 ${activePane.value + 1} 播放失败:`, error)
    // 错误提示已在 playRecordingAtTime 中处理
  }
}

/**
 * 播放指定时间的录像到指定窗口（使用 ZLMediaKit + mpegts.js）
 * @param paneIndex 窗口索引
 * @param channelId 通道ID
 * @param clickTime 点击的时间
 */
const playRecordingAtTime = async (paneIndex: number, channelId: number, clickTime: Date) => {
  try {
    const pane = panes.value[paneIndex]
    if (!pane) {
      throw new Error('窗口不存在')
    }

    // 设置加载状态
    pane.isLoading = true
    pane.isPlaying = false

    // 计算回放时间范围：点击时间前后各1小时
    const startTime = new Date(clickTime.getTime() - 60 * 60 * 1000)
    const endTime = new Date(clickTime.getTime() + 60 * 60 * 1000)
    
    console.log(`[录像回放] 窗口 ${paneIndex + 1} 开始播放`)
    console.log(`[录像回放] 通道ID: ${channelId}, 点击时间: ${clickTime.toISOString()}`)
    console.log(`[录像回放] 回放范围: ${startTime.toISOString()} ~ ${endTime.toISOString()}`)

    // 停止该窗口的旧播放器
    stopPanePlayer(paneIndex)
    await new Promise(resolve => setTimeout(resolve, 100))

    // 获取回放播放地址
    const startTimeStr = startTime.toISOString()
    const endTimeStr = endTime.toISOString()
    
    const playUrls = await getPlaybackUrl(channelId, startTimeStr, endTimeStr)
    console.log(`[录像回放] 获取到播放地址:`, playUrls)

    if (!playUrls || (!playUrls.wsFlvUrl && !playUrls.webrtcUrl)) {
      throw new Error('未获取到有效的播放地址')
    }

    // 保存回放时间范围
    pane.playbackStartTime = startTimeStr
    pane.playbackEndTime = endTimeStr

    await nextTick()

    const videoEl = pane.videoEl
    if (!videoEl) {
      throw new Error('视频元素未找到')
    }

    let success = false

    // 优先尝试 WebRTC（延迟最低）
    if (playUrls.webrtcUrl) {
      console.log('[录像回放] 尝试 WebRTC 播放...')
      success = await playWithWebRTC(pane, videoEl, playUrls.webrtcUrl, paneIndex)
      if (success) {
        pane.playMode = 'webrtc'
      }
    }

    // WebRTC 失败则回退到 FLV
    if (!success && playUrls.wsFlvUrl) {
      console.log('[录像回放] WebRTC 失败，回退到 FLV 播放...')
      success = await playWithFLV(pane, videoEl, playUrls.wsFlvUrl, paneIndex)
      if (success) {
        pane.playMode = 'flv'
      }
    }

    if (success) {
      const mode = pane.playMode === 'webrtc' ? 'WebRTC' : 'FLV'
      console.log(`[录像回放] ✅ 窗口 ${paneIndex + 1} 播放成功 [${mode}]`)
      ElMessage.success(`窗口 ${paneIndex + 1} 录像播放开始`)
    } else {
      throw new Error('播放失败')
    }

  } catch (error: any) {
    console.error(`[录像回放] 窗口 ${paneIndex + 1} 播放失败:`, error)
    const pane = panes.value[paneIndex]
    if (pane) {
      pane.isLoading = false
      pane.hasRecording = false
    }
    ElMessage.error(`播放失败: ${error?.message || error}`)
    throw error
  }
}

/**
 * WebRTC 播放
 * ZLMediaKit WebRTC 接口返回 JSON 格式，需要解析获取 SDP
 */
const playWithWebRTC = async (pane: typeof panes.value[0], videoEl: HTMLVideoElement, webrtcUrl: string, idx: number): Promise<boolean> => {
  return new Promise((resolve) => {
    let resolved = false
    const safeResolve = (value: boolean) => {
      if (!resolved) {
        resolved = true
        resolve(value)
      }
    }
    
    try {
      const pc = new RTCPeerConnection({
        iceServers: [{ urls: 'stun:stun.l.google.com:19302' }]
      })

      pc.ontrack = (event) => {
        console.log(`[WebRTC] 窗口 ${idx + 1} 收到媒体流`)
        videoEl.srcObject = event.streams[0]
        videoEl.play().then(() => {
          pane.isPlaying = true
          pane.isLoading = false
          pane.pc = pc
          safeResolve(true)
        }).catch((e) => {
          console.warn('[WebRTC] 播放失败:', e)
          pc.close()
          safeResolve(false)
        })
      }

      pc.oniceconnectionstatechange = () => {
        if (pc.iceConnectionState === 'failed' || pc.iceConnectionState === 'disconnected') {
          console.warn(`[WebRTC] 窗口 ${idx + 1} 连接状态: ${pc.iceConnectionState}`)
          if (!pane.isPlaying) {
            pc.close()
            safeResolve(false)
          }
        }
      }

      // 添加收发器
      pc.addTransceiver('video', { direction: 'recvonly' })
      pc.addTransceiver('audio', { direction: 'recvonly' })

      // 创建 offer 并发送到 ZLMediaKit
      pc.createOffer().then((offer) => {
        return pc.setLocalDescription(offer)
      }).then(() => {
        // 发送 SDP 到 ZLMediaKit WebRTC 接口
        // ZLMediaKit 返回的是 JSON 格式: { "code": 0, "sdp": "v=0\r\n..." }
        return fetch(webrtcUrl, {
          method: 'POST',
          headers: { 'Content-Type': 'application/sdp' },
          body: pc.localDescription?.sdp
        })
      }).then((response) => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`)
        return response.text()
      }).then((responseText) => {
        // 尝试解析为 JSON（ZLMediaKit 格式）
        let sdp: string
        try {
          const json = JSON.parse(responseText)
          if (json.code !== 0) {
            throw new Error(json.msg || `ZLMediaKit 错误码: ${json.code}`)
          }
          sdp = json.sdp
        } catch (jsonError) {
          // 如果不是 JSON，直接当作 SDP 使用
          if (responseText.startsWith('v=')) {
            sdp = responseText
          } else {
            throw new Error('无效的 SDP 响应')
          }
        }
        
        if (!sdp || !sdp.startsWith('v=')) {
          throw new Error('SDP 格式无效')
        }
        
        return pc.setRemoteDescription(new RTCSessionDescription({ type: 'answer', sdp }))
      }).then(() => {
        console.log(`[WebRTC] 窗口 ${idx + 1} SDP 交换完成`)
      }).catch((e) => {
        console.warn('[WebRTC] 连接失败:', e)
        pc.close()
        safeResolve(false)
      })

      // 超时处理
      setTimeout(() => {
        if (!pane.isPlaying && !resolved) {
          console.warn(`[WebRTC] 窗口 ${idx + 1} 连接超时`)
          pc.close()
          safeResolve(false)
        }
      }, 8000)
    } catch (e) {
      console.warn('[WebRTC] 初始化失败:', e)
      safeResolve(false)
    }
  })
}

/**
 * mpegts.js FLV 播放
 */
const playWithFLV = async (pane: typeof panes.value[0], videoEl: HTMLVideoElement, wsFlvUrl: string, idx: number): Promise<boolean> => {
  return new Promise((resolve) => {
    let resolved = false
    let player: mpegts.Player | null = null
    
    const safeResolve = (value: boolean) => {
      if (!resolved) {
        resolved = true
        resolve(value)
      }
    }
    
    const cleanup = () => {
      if (player) {
        try {
          player.pause()
          player.unload()
          player.detachMediaElement()
          player.destroy()
        } catch (e) {
          // 忽略清理时的错误
        }
        player = null
      }
    }
    
    try {
      if (!mpegts.isSupported()) {
        throw new Error('浏览器不支持 FLV 播放')
      }

      player = mpegts.createPlayer({
        type: 'flv',
        url: wsFlvUrl,
        isLive: false, // 录像回放是非直播
        hasAudio: true,
        hasVideo: true
      }, {
        enableWorker: false,
        enableStashBuffer: true,
        stashInitialSize: isFirefox ? 384 : 256,
        lazyLoad: false,
        autoCleanupSourceBuffer: true,
        autoCleanupMaxBackwardDuration: isFirefox ? 120 : 60,
        autoCleanupMinBackwardDuration: isFirefox ? 60 : 30,
        fixAudioTimestampGap: true,
        // 关键：忽略不支持的音频编解码器，不抛出异常
        deferLoadAfterSourceOpen: false
      })

      // 错误计数器，防止刷屏
      let errorCount = 0
      const maxErrors = 3
      
      player.on(mpegts.Events.ERROR, (_errorType: any, errorDetail: any, errorInfo: any) => {
        const errorStr = String(errorDetail || '')
        const infoStr = String(errorInfo?.msg || errorInfo || '')
        
        // 忽略常见的非致命错误
        if (errorStr.includes('CodecUnsupported') && infoStr.toLowerCase().includes('audio')) {
          // 音频编解码器不支持，静默忽略
          return
        }
        if (errorStr.includes('MSEError') || errorStr.includes('SourceBuffer')) {
          // MSE/SourceBuffer 错误，通常是清理时的残留
          return
        }
        
        // 其他错误计数
        errorCount++
        if (errorCount <= maxErrors) {
          console.error(`[录像回放] 窗口 ${idx + 1} 播放错误 (${errorCount}/${maxErrors}):`, errorDetail)
        }
        
        // 太多错误则停止
        if (errorCount >= maxErrors && !resolved) {
          pane.isPlaying = false
          pane.isLoading = false
          cleanup()
          safeResolve(false)
        }
      })

      player.attachMediaElement(videoEl)
      player.load()

      const playDelay = isFirefox ? 800 : 300
      setTimeout(async () => {
        if (resolved) return
        
        try {
          await player!.play()
          pane.player = player
          pane.isPlaying = true
          pane.isLoading = false
          safeResolve(true)
        } catch (e) {
          console.warn(`[录像回放] 窗口 ${idx + 1} 播放启动失败:`, e)
          pane.isLoading = false
          cleanup()
          safeResolve(false)
        }
      }, playDelay)
      
      // 超时处理
      setTimeout(() => {
        if (!resolved) {
          console.warn(`[录像回放] 窗口 ${idx + 1} FLV 播放超时`)
          pane.isLoading = false
          cleanup()
          safeResolve(false)
        }
      }, 15000)
      
    } catch (e) {
      console.error('[录像回放] FLV 播放器创建失败:', e)
      safeResolve(false)
    }
  })
}

/**
 * 停止单个窗口的播放器
 */
const stopPanePlayer = (paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (!pane) return

  // 停止 mpegts 播放器
  if (pane.player) {
    try {
      pane.player.pause()
      pane.player.unload()
      pane.player.detachMediaElement()
      pane.player.destroy()
    } catch (e) {
      console.warn(`[录像回放] 窗口 ${paneIndex + 1} 停止 mpegts 播放器失败:`, e)
    }
    pane.player = null
  }

  // 停止 WebRTC 连接
  if (pane.pc) {
    try {
      pane.pc.close()
    } catch (e) {
      console.warn(`[录像回放] 窗口 ${paneIndex + 1} 关闭 WebRTC 连接失败:`, e)
    }
    pane.pc = null
  }

  // 重置 video 元素
  if (pane.videoEl) {
    pane.videoEl.pause()
    pane.videoEl.srcObject = null
    pane.videoEl.src = ''
  }

  // 重置状态
  pane.isPlaying = false
  pane.isPaused = false
  pane.isLoading = false
  pane.playMode = null
}
// 已移除大华 RPC 录像文件查询和登录相关代码
// 改用 ZLMediaKit 的回放接口

/**
 * 组件挂载时初始化
 */
onMounted(async () => {
  await loadSpaceTree()
  setLayout(gridLayout.value)
})

/**
 * 组件卸载时清理
 */
onUnmounted(() => {
  stopAllPlayers()
})

// 初始化默认时间段为当天 00:00:00 - 23:59:59
const initDefaultRange = () => {
  const d = new Date()
  const start = new Date(d)
  start.setHours(0, 0, 0, 0)
  const end = new Date(d)
  end.setHours(24, 0, 0, 0)
  const fmt = (dt: Date) => {
    const y = dt.getFullYear()
    const m = String(dt.getMonth() + 1).padStart(2, '0')
    const day = String(dt.getDate()).padStart(2, '0')
    const hh = String(dt.getHours()).padStart(2, '0')
    const mm = String(dt.getMinutes()).padStart(2, '0')
    const ss = String(dt.getSeconds()).padStart(2, '0')
    return `${y}-${m}-${day} ${hh}:${mm}:${ss}`
  }
  filterForm.timeRange = [fmt(start), fmt(end)]
}

initDefaultRange()

// ====== 播放控制与同步 ======
const togglePlayPause = () => {
  // 工具栏按钮只对当前活动窗口有效
  const pane = panes.value[activePane.value]
  
  if (!pane) {
    ElMessage.warning('请先选择一个窗口')
    return
  }
  
  if (!pane.player || !pane.isPlaying) {
    ElMessage.warning('当前窗口没有正在播放的视频')
    return
  }
  
  try {
    if (pane.isPaused) {
      // 恢复播放 - 大华播放器使用 play() 方法
      if (typeof pane.player.play === 'function') {
        pane.player.play()
      } else if (typeof pane.player.connect === 'function') {
        pane.player.connect()
      }
      pane.isPaused = false
      console.log(`[播放控制] 窗口 ${activePane.value + 1} 恢复播放`)
      ElMessage.success('已播放')
    } else {
      // 暂停播放 - 大华播放器使用 pause() 方法
      if (typeof pane.player.pause === 'function') {
        pane.player.pause()
      } else if (typeof pane.player.disconnect === 'function') {
        pane.player.disconnect()
      }
      pane.isPaused = true
      console.log(`[播放控制] 窗口 ${activePane.value + 1} 暂停播放`)
      ElMessage.success('已暂停')
    }
  } catch (error) {
    console.warn(`[播放控制] 窗口 ${activePane.value + 1} 暂停/恢复失败:`, error)
    console.warn(`[播放控制] 播放器对象:`, pane.player)
    console.warn(`[播放控制] 可用方法:`, Object.keys(pane.player || {}))
    ElMessage.error('播放控制失败，请查看控制台日志')
  }
  
  // 更新全局播放状态
  const anyPlaying = panes.value.some(p => p.isPlaying && !p.isPaused)
  isPlaying.value = anyPlaying
}

const handleStop = () => {
  // 工具栏按钮只对当前活动窗口有效
  const pane = panes.value[activePane.value]
  
  if (!pane) {
    ElMessage.warning('请先选择一个窗口')
    return
  }
  
  // 停止并关闭播放器实例 - mpegts.js 正确销毁顺序
  if (pane.player) {
    try {
      console.log(`[播放控制] 停止窗口 ${activePane.value + 1} 播放器`)
      // mpegts.js 正确销毁顺序：pause -> unload -> detachMediaElement -> destroy
      if (typeof pane.player.pause === 'function') {
        try { pane.player.pause() } catch (e) { console.warn('[播放控制] pause() 失败:', e) }
      }
      if (typeof pane.player.unload === 'function') {
        try { pane.player.unload() } catch (e) { console.warn('[播放控制] unload() 失败:', e) }
      }
      if (typeof pane.player.detachMediaElement === 'function') {
        try { pane.player.detachMediaElement() } catch (e) { console.warn('[播放控制] detachMediaElement() 失败:', e) }
      }
      if (typeof pane.player.destroy === 'function') {
        try { pane.player.destroy() } catch (e) { console.warn('[播放控制] destroy() 失败:', e) }
      }
      pane.player = null
    } catch (error) {
      console.warn(`[播放控制] 关闭播放器失败:`, error)
    }
  }
  
  // 暂停video元素
  const v = pane.videoEl
  if (v) {
    v.pause()
    try { v.currentTime = 0 } catch {}
  }
  
  // 更新窗口状态
  pane.isPlaying = false
  pane.isPaused = false
  pane.isLoading = false
  pane.currentFile = null
  pane.currentPlaySeconds = 0
  
  // 更新全局播放状态
  isPlaying.value = panes.value.some(p => p.isPlaying)
  
  ElMessage.success('已停止播放')
}

const handleBackward = () => {
  // 工具栏按钮只对当前活动窗口有效
  const pane = panes.value[activePane.value]
  
  if (!pane || !pane.player || !pane.isPlaying) {
    ElMessage.warning('当前窗口没有正在播放的视频')
    return
  }
  
  try {
    // 大华播放器使用 playByTime 跳转（单位：秒）
    // 获取当前播放时间，后退5秒
    const currentSeconds = pane.currentPlaySeconds || 0
    const newSeconds = Math.max(currentSeconds - 5, 0)
    pane.player.playByTime(newSeconds)
    pane.currentPlaySeconds = newSeconds
    console.log(`[播放控制] 窗口 ${activePane.value + 1} 后退5秒，当前位置: ${newSeconds}秒`)
  } catch (error) {
    console.warn('[播放控制] 后退失败:', error)
    ElMessage.error('后退失败')
  }
}

const handleForward = () => {
  // 工具栏按钮只对当前活动窗口有效
  const pane = panes.value[activePane.value]
  
  if (!pane || !pane.player || !pane.isPlaying || !pane.currentFile) {
    ElMessage.warning('当前窗口没有正在播放的视频')
    return
  }
  
  try {
    // 大华播放器使用 playByTime 跳转（单位：秒）
    // 获取当前播放时间，前进5秒
    const currentSeconds = pane.currentPlaySeconds || 0
    const fileStartTime = new Date(pane.currentFile.StartTime).getTime()
    const fileEndTime = new Date(pane.currentFile.EndTime).getTime()
    const fileDuration = (fileEndTime - fileStartTime) / 1000 // 转换为秒
    const newSeconds = Math.min(currentSeconds + 5, fileDuration)
    pane.player.playByTime(newSeconds)
    pane.currentPlaySeconds = newSeconds
    console.log(`[播放控制] 窗口 ${activePane.value + 1} 前进5秒，当前位置: ${newSeconds}秒`)
  } catch (error) {
    console.warn('[播放控制] 前进失败:', error)
    ElMessage.error('前进失败')
  }
}


/**
 * 改变播放速度（使用 HTML5 video playbackRate）
 * ZLMediaKit + mpegts.js 方案支持前端加速播放
 */
const handleSpeedChange = () => {
  const pane = panes.value[activePane.value]
  
  if (!pane || !pane.isPlaying || !pane.videoEl) {
    ElMessage.warning('当前窗口没有正在播放的视频')
    playbackSpeed.value = 1
    return
  }
  
  const speed = playbackSpeed.value
  
  try {
    // HTML5 video 支持 0.25 ~ 16 倍速，但实际使用时 0.5 ~ 4 较稳定
    if (speed < 0.25 || speed > 4) {
      ElMessage.warning('播放速度范围：0.25x ~ 4x')
      playbackSpeed.value = 1
      return
    }
    
    // 设置当前窗口的播放速度
    pane.videoEl.playbackRate = speed
    
    console.log(`[播放控制] 窗口 ${activePane.value + 1} 播放速度设置为: ${speed}x`)
    ElMessage.success(`播放速度: ${speed}x`)
    
  } catch (error) {
    console.error('[播放控制] 设置播放速度失败:', error)
    ElMessage.error('设置播放速度失败')
    playbackSpeed.value = 1
  }
}

/**
 * 设置所有播放窗口的播放速度
 */
const setAllPanesSpeed = (speed: number) => {
  panes.value.forEach((pane, index) => {
    if (pane.isPlaying && pane.videoEl) {
      try {
        pane.videoEl.playbackRate = speed
        console.log(`[播放控制] 窗口 ${index + 1} 播放速度: ${speed}x`)
      } catch (e) {
        console.warn(`[播放控制] 窗口 ${index + 1} 设置速度失败:`, e)
      }
    }
  })
}

const toggleMuteAll = () => {
  isMutedAll.value = !isMutedAll.value
  panes.value.forEach((p) => {
    p.muted = isMutedAll.value
    // 通过 video 元素控制音频
    if (p.videoEl) {
      p.videoEl.muted = isMutedAll.value
    }
  })
  ElMessage.success(isMutedAll.value ? '已静音' : '已取消静音')
}

/**
 * 显示剪切对话框
 */
const showTimeCutDialog = () => {
  const pane = panes.value[activePane.value]
  if (!pane) {
    ElMessage.warning('请先选择一个窗口')
    return
  }
  
  if (!pane.isPlaying || !pane.currentFile) {
    ElMessage.warning('当前窗口没有正在播放的视频')
    return
  }
  
  timeCutDialogVisible.value = true
  // 如果该窗口存在进行中的剪切，展示进度
  currentCutTask.value = cutTasks[activePane.value] || null
  
  // 设置默认时间范围：当前播放时间 + 5分钟
  const now = new Date(currentPlayTime.value || timelineStart.value)
  const end = new Date(now.getTime() + 5 * 60 * 1000)
  timeCutForm.startTime = formatDateTime(now)
  timeCutForm.endTime = formatDateTime(end)
}

const toggleSyncPlayback = async () => {
  syncPlayback.value = !syncPlayback.value
  
  if (syncPlayback.value) {
    // 根据当前活动窗口的时间同步所有正在播放的窗口
    const activeCurrentPane = panes.value[activePane.value]
    
    if (!activeCurrentPane) {
      ElMessage.warning('请先选择一个窗口')
      syncPlayback.value = false
      return
    }
    
    if (!activeCurrentPane.isPlaying || !activeCurrentPane.currentFile) {
      ElMessage.warning('当前活动窗口没有正在播放的视频')
      syncPlayback.value = false
      return
    }
    
    // 获取当前活动窗口的播放时间
    const fileStartTime = new Date(activeCurrentPane.currentFile.StartTime).getTime()
    const currentPlayTime = fileStartTime + (activeCurrentPane.currentPlaySeconds || 0) * 1000
    const syncTime = new Date(currentPlayTime)
    
    console.log(`[同步回放] 根据窗口 ${activePane.value + 1} 的时间同步: ${syncTime.toISOString()}`)
    
    // 只同步正在播放的窗口
    let syncCount = 0
    for (const pane of panes.value) {
      const paneIndex = panes.value.indexOf(pane)
      
      // 跳过当前活动窗口（已经在播放了）
      if (paneIndex === activePane.value) continue
      
      // 只同步正在播放的窗口
      if (!pane.isPlaying || !pane.player) {
        console.log(`[同步回放] 跳过窗口 ${paneIndex + 1}（未播放）`)
        continue
      }
      
      if (!pane.channelId || !pane.currentFile) {
        console.log(`[同步回放] 跳过窗口 ${paneIndex + 1}（无通道或录像文件）`)
        continue
      }
      
      try {
        console.log(`[同步回放] 同步窗口 ${paneIndex + 1} 到时间: ${syncTime.toISOString()}`)
        await playDahuaRecordingAtTime(paneIndex, pane.channelId, pane.channelNo!, syncTime)
        syncCount++
      } catch (error) {
        console.warn(`[同步回放] 窗口 ${paneIndex + 1} 同步失败:`, error)
      }
    }
    
    if (syncCount > 0) {
      ElMessage.success(`已开启同步回放，${syncCount} 个窗口已同步到窗口 ${activePane.value + 1} 的时间`)
    } else {
      ElMessage.info('已开启同步回放，但没有其他正在播放的窗口需要同步')
    }
  } else {
    ElMessage.info('已关闭同步回放')
  }
}

function formatDateTime(d: Date) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  const ss = String(d.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${day} ${hh}:${mm}:${ss}`
}

const syncToTime = async (ms: number) => {
  // 将所有窗口同步到指定时间
  const syncTime = new Date(ms)
  
  for (const pane of panes.value) {
    if (!pane.channelId || !pane.hasRecording) continue
    
    const paneIndex = panes.value.indexOf(pane)
    try {
      await playDahuaRecordingAtTime(paneIndex, pane.channelId, pane.channelNo!, syncTime)
    } catch (error) {
      console.warn(`[同步回放] 窗口 ${paneIndex + 1} 同步失败:`, error)
    }
  }
}

// ====== 时间轴与拖拽 ======
const draggingTimeline = ref(false)
let removeDragListeners: (() => void) | null = null

const currentTimePercent = computed<number>(() => {
  const start = timelineStart.value
  const end = timelineEnd.value
  const total = Math.max(end - start, 1)
  const t = Math.min(Math.max(currentPlayTime.value || start, start), end)
  return ((t - start) / total) * 100
})

const currentTimeLabel = computed<string>(() => {
  const t = currentPlayTime.value || timelineStart.value
  return new Date(t).toLocaleString()
})

const startTimelineDrag = (e: MouseEvent) => {
  draggingTimeline.value = true
  const bar = e.currentTarget as HTMLElement
  const rect = bar.getBoundingClientRect()
  const update = (clientX: number) => {
    const percent = Math.min(Math.max((clientX - rect.left) / rect.width, 0), 1)
    currentPlayTime.value = timelineStart.value + percent * (timelineEnd.value - timelineStart.value)
    if (syncPlayback.value) syncToTime(currentPlayTime.value)
  }
  const move = (ev: MouseEvent) => update(ev.clientX)
  const up = () => {
    draggingTimeline.value = false
    window.removeEventListener('mousemove', move)
    window.removeEventListener('mouseup', up)
    removeDragListeners = null
  }
  window.addEventListener('mousemove', move)
  window.addEventListener('mouseup', up)
  removeDragListeners = () => {
    window.removeEventListener('mousemove', move)
    window.removeEventListener('mouseup', up)
  }
  update(e.clientX)
}

onUnmounted(() => {
  if (removeDragListeners) removeDragListeners()
})

// ====== 导出与剪切 ======
const cutDuration = computed<string>(() => {
  if (!timeCutForm.startTime || !timeCutForm.endTime) return '--'
  const s = new Date(timeCutForm.startTime).getTime()
  const e = new Date(timeCutForm.endTime).getTime()
  const diff = Math.max(e - s, 0)
  const mm = Math.floor(diff / 60000)
  const ss = Math.floor((diff % 60000) / 1000)
  return `${mm}分${ss}秒`
})

const cutRangeLeft = computed<number>(() => {
  if (!timeCutForm.startTime) return 0
  const s = new Date(timeCutForm.startTime).getTime()
  const start = timelineStart.value
  const end = timelineEnd.value
  const total = Math.max(end - start, 1)
  return (Math.min(Math.max(s, start), end) - start) / total * 100
})

const cutRangeWidth = computed<number>(() => {
  if (!timeCutForm.startTime || !timeCutForm.endTime) return 0
  const s = new Date(timeCutForm.startTime).getTime()
  const e = new Date(timeCutForm.endTime).getTime()
  const start = timelineStart.value
  const end = timelineEnd.value
  const total = Math.max(end - start, 1)
  const left = Math.min(Math.max(s, start), end)
  const right = Math.min(Math.max(e, start), end)
  return Math.max(((right - left) / total) * 100, 0)
})

/**
 * 确认剪切 - 使用浏览器端 MediaRecorder 录制当前播放的视频流
 * 注意：ZLMediaKit 模式下，录制需要在播放过程中进行
 */
const handleConfirmTimeCut = async () => {
  if (!timeCutForm.startTime || !timeCutForm.endTime) {
    ElMessage.warning('请完善剪切时间段')
    return
  }
  
  const pane = panes.value[activePane.value]
  if (!pane || !pane.channelId) {
    ElMessage.warning('当前窗口未绑定通道')
    return
  }
  
  if (!pane.isPlaying || !pane.videoEl) {
    ElMessage.warning('当前窗口没有正在播放的视频')
    return
  }
  
  try {
    // 验证时间范围
    const cutStartTime = new Date(timeCutForm.startTime).getTime()
    const cutEndTime = new Date(timeCutForm.endTime).getTime()
    
    if (cutEndTime <= cutStartTime) {
      ElMessage.warning('结束时间必须大于开始时间')
      return
    }
    
    const duration = Math.floor((cutEndTime - cutStartTime) / 1000)
    if (duration < 1) {
      ElMessage.warning('剪切时长至少为1秒')
      return
    }
    
    if (duration > 600) {
      ElMessage.warning('单次录制时长不能超过10分钟')
      return
    }
    
    console.log('[录制视频] 开始录制:', {
      channelId: pane.channelId,
      channelName: pane.channelName,
      duration: duration + '秒'
    })
    
    // 获取视频流
    const videoEl = pane.videoEl
    let stream: MediaStream | null = null
    
    // 尝试从 video 元素获取流
    if (videoEl.srcObject instanceof MediaStream) {
      stream = videoEl.srcObject
    } else if (typeof (videoEl as any).captureStream === 'function') {
      stream = (videoEl as any).captureStream()
    } else if (typeof (videoEl as any).mozCaptureStream === 'function') {
      stream = (videoEl as any).mozCaptureStream()
    }
    
    if (!stream) {
      ElMessage.error('无法捕获视频流，请确保浏览器支持媒体录制')
      return
    }
    
    // 创建录制任务
    const paneIndex = activePane.value
    const task: CutTask = {
      paneIndex,
      channelId: pane.channelId,
      channelName: pane.channelName,
      startTime: timeCutForm.startTime,
      endTime: timeCutForm.endTime,
      durationSec: duration,
      progress: 0,
      status: 'cutting',
      statusText: '正在录制...',
      createdAt: formatDateTime(new Date())
    }
    cutTasks[paneIndex] = task
    currentCutTask.value = task
    
    // 初始化录制数据
    pane.recordedChunks = []
    
    // 创建 MediaRecorder
    const options: MediaRecorderOptions = { mimeType: 'video/webm;codecs=vp9' }
    if (!MediaRecorder.isTypeSupported(options.mimeType!)) {
      options.mimeType = 'video/webm;codecs=vp8'
    }
    if (!MediaRecorder.isTypeSupported(options.mimeType!)) {
      options.mimeType = 'video/webm'
    }
    
    const mediaRecorder = new MediaRecorder(stream, options)
    pane.mediaRecorder = mediaRecorder
    
    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) {
        pane.recordedChunks.push(event.data)
      }
    }
    
    mediaRecorder.onstop = () => {
      console.log('[录制视频] 录制完成，开始保存文件')
      
      // 创建 Blob 并下载
      const blob = new Blob(pane.recordedChunks, { type: options.mimeType })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `${pane.channelName || '录像'}_${formatDateTime(new Date()).replace(/[:\s]/g, '-')}.webm`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
      
      // 更新任务状态
      task.progress = 100
      task.status = 'success'
      task.statusText = '已完成'
      currentCutTask.value = task
      cutHistoryList.value.unshift({ ...task })
      cutTasks[paneIndex] = undefined
      pane.mediaRecorder = null
      pane.recordedChunks = []
      
      ElMessage.success('录制完成，文件已保存')
    }
    
    mediaRecorder.onerror = (event: any) => {
      console.error('[录制视频] 录制错误:', event)
      task.status = 'error'
      task.statusText = '录制失败'
      currentCutTask.value = task
      cutHistoryList.value.unshift({ ...task })
      cutTasks[paneIndex] = undefined
      pane.mediaRecorder = null
      pane.recordedChunks = []
      ElMessage.error('录制失败')
    }
    
    // 开始录制
    mediaRecorder.start(1000) // 每秒收集一次数据
    
    // 进度更新定时器
    const startTime = Date.now()
    const progressInterval = setInterval(() => {
      const elapsed = Math.floor((Date.now() - startTime) / 1000)
      const progress = Math.min(100, Math.floor((elapsed / duration) * 100))
      task.progress = progress
      task.statusText = `录制中 ${progress}%`
      if (currentCutTask.value) {
        currentCutTask.value.progress = progress
        currentCutTask.value.statusText = task.statusText
      }
    }, 1000)
    
    // 设置定时停止
    setTimeout(() => {
      clearInterval(progressInterval)
      if (mediaRecorder.state !== 'inactive') {
        mediaRecorder.stop()
      }
    }, duration * 1000)
    
    timeCutDialogVisible.value = false
    ElMessage.info(`录制任务已启动，将在 ${duration} 秒后自动停止`)
    
  } catch (error: any) {
    console.error('[录制视频] 失败:', error)
    ElMessage.error('录制失败：' + (error?.message || error))
  }
}

</script>

<style lang="scss" scoped>
@use '@/styles/dark-theme.scss';

.video-playback-container {
  height: 100%;
  display: flex;
  flex-direction: column;

  // 顶部导航栏
  .playback-controls {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 16px;
    background: rgba(0, 0, 0, 0.3);
    border-top: 1px solid rgba(255, 255, 255, 0.1);

    .controls-left,
    .controls-center,
    .controls-right {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .cut-progress-display {
      display: flex;
      align-items: center;
      padding: 4px 12px;
      background: rgba(103, 194, 58, 0.1);
      border: 1px solid rgba(103, 194, 58, 0.3);
      border-radius: 4px;
      margin-right: 8px;

      .progress-text {
        font-size: 12px;
        color: #67c23a;
        white-space: nowrap;
      }
    }

    .nav-left {
      flex: 1;

      .page-title {
        font-size: 16px;
        font-weight: 500;
        color: rgba(255, 255, 255, 0.9);
      }
    }

    .nav-tabs {
      flex: 1;
      display: flex;
      justify-content: center;
      gap: 8px;
      padding: 4px;
      background: rgba(0, 0, 0, 0.2);
      border-radius: 8px;

      .nav-tab {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 8px 20px;
        font-size: 14px;
        color: rgba(255, 255, 255, 0.6);
        cursor: pointer;
        border-radius: 6px;
        transition: all 0.3s;

        &:hover {
          color: rgba(255, 255, 255, 0.9);
          background: rgba(255, 255, 255, 0.05);
        }

        &.active {
          color: #fff;
          background: rgba(64, 158, 255, 0.3);
          font-weight: 500;
        }
      }
    }

    .nav-right {
      flex: 1;
      display: flex;
      justify-content: flex-end;
    }
  }

  // 主要内容区域
  .main-layout {
    flex: 1;
    overflow: hidden;

    // SmartPSS Plus 布局
    .smartpss-layout {
      height: 100%;
      display: flex;
      gap: 10px;
      padding: 10px;

      .left-panel {
        width: 240px;
        background: #1e1e1e;
        border: 1px solid #3a3a3a;
        border-radius: 4px;
        display: flex;
        flex-direction: column;
        overflow: auto;

        .panel-section {
          border-bottom: 1px solid #3a3a3a;
          &:last-of-type { // 时间筛选区的标题不需要显示
            .section-header { display: none; }
          }
          &:first-of-type { flex: 1; display: flex; flex-direction: column; min-height: 0; }
          
          .section-header {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 12px;
            background: #252525;
            font-size: 14px;
            font-weight: 500;
            color: #e0e0e0;
          }
          
          .search-box {
            padding: 8px 12px;
          }
          
          .device-tree {
            flex: 1;
            min-height: 0;
            overflow-y: auto;
            padding: 8px;
            
            // 默认隐藏所有复选框
            :deep(.el-tree-node__content) {
              .el-checkbox {
                display: none;
              }
            }
            
            // 显示"通道"文件夹节点的复选框
            :deep(.el-tree-node__content:has(.node-type-channels)) {
              .el-checkbox {
                display: inline-block;
              }
            }
            
            // 显示通道节点的复选框
            :deep(.el-tree-node__content:has(.node-type-channel)) {
              .el-checkbox {
                display: inline-block;
              }
            }
          }
          
          .time-filter {
            padding: 12px;
            flex-shrink: 0;
            position: sticky;
            bottom: 0;
            background: #1e1e1e;
            
            .recording-mode {
              width: 100%;
              margin-bottom: 12px;
            }
            
            .filter-item {
              margin-bottom: 12px;
              
              label {
                display: block;
                font-size: 12px;
                color: #909399;
                margin-bottom: 4px;
              }
            }
          }
        }
      }

      .center-panel {
        flex: 1;
        display: flex;
        flex-direction: column;
        background: #1e1e1e;
        border: 1px solid #3a3a3a;
        border-radius: 4px;
        overflow: auto;
        
      .player-section {
          flex: 1;
          display: flex;
          flex-direction: column;
          overflow: auto;
          padding: 16px 16px 4px 16px;
          
          .section-header { display: none; }
          
          .player-grid {
            flex: 1;
            display: grid;
            gap: 8px;
            background: #000;
            min-height: 0;
            padding: 8px;
            border-top: 1px solid #2f2f2f;
            
            &.grid-1x1 {
              grid-template-columns: 1fr;
              grid-template-rows: 1fr;
            }
            
            &.grid-2x2 {
              grid-template-columns: repeat(2, 1fr);
              grid-template-rows: repeat(2, 1fr);
            }
            
            &.grid-2x3 {
              grid-template-columns: repeat(3, 1fr);
              grid-template-rows: repeat(2, 1fr);
            }
            
            &.grid-3x3 {
              grid-template-columns: repeat(3, 1fr);
              grid-template-rows: repeat(3, 1fr);
            }
            
            &.grid-3x4 {
              grid-template-columns: repeat(4, 1fr);
              grid-template-rows: repeat(3, 1fr);
            }
            
            &.grid-4x4 {
              grid-template-columns: repeat(4, 1fr);
              grid-template-rows: repeat(4, 1fr);
            }

            .player-pane {
              position: relative;
              background: #0a0a0a;
              border: 1px solid #2f2f2f;
              border-radius: 4px;
              overflow: hidden;
              cursor: pointer;
              
              &.active {
                border-color: #409eff;
                box-shadow: 0 0 8px rgba(64, 158, 255, 0.35);
              }
              
              &.drag-over {
                border-color: #67c23a;
                box-shadow: 0 0 12px rgba(103, 194, 58, 0.5);
                background: rgba(103, 194, 58, 0.1);
              }
              
              .pane-video {
                width: 100%;
                height: 100%;
                background: #0e0e0e;
                object-fit: contain;
              }
              
              .pane-overlay {
                position: absolute;
                inset: 0;
                pointer-events: none;
                z-index: 1000;
                
                .overlay-top {
                  position: absolute;
                  top: 0;
                  left: 0;
                  right: 0;
                  padding: 6px 10px;
                  background: linear-gradient(180deg, rgba(0,0,0,0.6) 0%, transparent 100%);
                  
                  .window-title {
                    color: #fff;
                    font-size: 12px;
                    font-weight: 500;
                  }
                }
                
                .overlay-center {
                  position: absolute;
                  inset: 0;
                  display: flex;
                  flex-direction: column;
                  align-items: center;
                  justify-content: center;
                  gap: 8px;
                  color: #7a9aba;
                  
                  .window-label {
                    margin: 0;
                    font-size: 14px;
                    font-weight: 500;
                    color: #a5c0db;
                  }
                  .tip-text {
                    margin: 0;
                    font-size: 12px;
                    color: #6c88a3;
                  }
                  &.loading {
                    :deep(.loading-icon) {
                      animation: rotate 1.5s linear infinite;
                      display: inline-block;
                      transform-origin: 50% 50%;
                      color: #409eff;
                    }
                    :deep(.loading-icon svg) {
                      animation: rotate 1.5s linear infinite;
                      transform-origin: 50% 50%;
                    }
                    .spinner {
                      width: 64px;
                      height: 64px;
                      border: 4px solid rgba(64, 158, 255, 0.2);
                      border-top-color: #409eff;
                      border-radius: 50%;
                      animation: rotate 1s linear infinite;
                    }
                    .window-label {
                      color: #409eff;
                      font-weight: 600;
                    }
                  }
                }
                
                .overlay-bottom {
                  position: absolute;
                  bottom: 0;
                  left: 0;
                  right: 0;
                  padding: 6px 10px;
                  background: linear-gradient(0deg, rgba(0,0,0,0.6) 0%, transparent 100%);
                  
                  .device-name {
                    color: #fff;
                    font-size: 12px;
                    display: flex;
                    align-items: center;
                    gap: 6px;
                  }
                  
                  .status-indicator {
                    font-size: 8px;
                    
                    &.has-recording {
                      color: #67c23a;
                    }
                    
                    &.no-recording {
                      color: #f56c6c;
                    }
                  }
                }
              }
            }
          }
          
          .time-scale {
            flex-shrink: 0;
            height: 30px;
            background: #252525;
            border-top: 1px solid #3a3a3a;
            position: relative;
            margin-bottom: 0;
            
            .scale-marks {
              position: relative;
              height: 100%;
            }
            .scale-mark {
              position: absolute;
              bottom: 0;
              top: auto;
              width: 1px;
              height: 50%;
              background: #3a3a3a;
            }
            .scale-mark.long {
              background: #bfbfbf;
              height: 50%;
            }
            .scale-mark:not(.long) {
              height: 30%;
              bottom: 0;
            }
            .mark-time {
              position: absolute;
              bottom: calc(50% + 4px);
              left: 0;
              transform: translateX(-50%);
              font-size: 11px;
              color: #909399;
              white-space: nowrap;
            }
          }
          
          .timeline-container {
            flex-shrink: 0;
            padding: 0;
            
            .timeline {
              margin-top: 0;
              
              .timeline-bar {
                position: relative;
                height: 18px; /* 使用现在一半的高度 */
                background: rgba(255, 255, 255, 0.06);
                border: 1px solid rgba(255, 255, 255, 0.1);
                border-radius: 6px;
                cursor: pointer;
                overflow: hidden;
                
                .timeline-seg {
                  position: absolute;
                  top: 0;
                  bottom: 0;
                  background: #67c23a; /* 录像时间段使用绿色 */
                  opacity: 0.8;
                  border-radius: 2px;
                  min-width: 2px; /* 确保最小宽度可见 */
                }
                .timeline-cursor {
                  position: absolute;
                  top: 0;
                  bottom: 0;
                  width: 0;
                  transform: translateX(-1px);
                  pointer-events: none;
                  
                  .cursor-line {
                    position: absolute;
                    left: 0;
                    top: 0;
                    bottom: 0;
                    width: 2px;
                    background: #409eff;
                  }
                  .cursor-time {
                    position: absolute;
                    top: -24px;
                    left: -40px;
                    padding: 2px 8px;
                    font-size: 12px;
                    color: #333;
                    background: #eaeaea;
                    border-radius: 12px;
                  }
                }
              }
              .timeline-labels {
                display: flex;
                justify-content: space-between;
                margin-top: 6px;
                font-size: 12px;
                color: rgba(255, 255, 255, 0.6);
              }
            }
          }
          .playback-controls {
            display: grid;
            grid-template-columns: 1fr auto 1fr;
            align-items: center;
            gap: 8px;
            padding: 6px 12px;
            position: sticky;
            bottom: 0;
            left: 0;
            right: 0;
            background: #1e1e1e;
            border-top: 1px solid #3a3a3a;
            margin-bottom: 2px;
            z-index: 5;
            
            .controls-left { display: flex; gap: 6px; align-items: center; }
            .controls-center { display: flex; gap: 6px; align-items: center; justify-content: center; }
            .controls-right { display: flex; gap: 6px; align-items: center; justify-content: flex-end; }

            :deep(.el-button) { padding: 4px 8px; }
            :deep(.el-button.is-circle) { width: 28px; height: 28px; padding: 0; }
            :deep(.el-select) { width: 56px; }
          }
        }
      }

    }

    // 地图视图（保留旧样式）
    .map-view {
      height: 100%;
      display: flex;
      gap: 20px;
      padding: 20px;

      .left-panel {
        width: 280px;
        background: rgba(255, 255, 255, 0.05);
        border: 1px solid rgba(255, 255, 255, 0.1);
        border-radius: 8px;
        display: flex;
        flex-direction: column;
        overflow: hidden;

        .panel-header {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 16px;
          border-bottom: 1px solid rgba(255, 255, 255, 0.1);
          font-size: 16px;
          font-weight: 500;
          color: rgba(255, 255, 255, 0.9);
        }

        :deep(.el-tree) {
          flex: 1;
          overflow-y: auto;
          padding: 12px;
          background: transparent;
          color: rgba(255, 255, 255, 0.8);

          .el-tree-node__content {
            &:hover {
              background: rgba(255, 255, 255, 0.1);
            }
          }

          .tree-node {
            display: flex;
            align-items: center;
            gap: 8px;
          }
        }
      }

      .right-panel {
        .device-list {
          flex: 1;
          overflow-y: auto;
          padding: 12px;

          .device-card {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 12px;
            margin-bottom: 8px;
            background: rgba(255, 255, 255, 0.03);
            border: 1px solid rgba(255, 255, 255, 0.05);
            border-radius: 6px;
            cursor: pointer;
            transition: all 0.3s;

            &:hover {
              background: rgba(255, 255, 255, 0.08);
              border-color: rgba(64, 158, 255, 0.3);
            }

            .device-icon {
              display: flex;
              align-items: center;
              justify-content: center;
              width: 40px;
              height: 40px;
              background: rgba(64, 158, 255, 0.1);
              border-radius: 6px;
              color: #409eff;
            }

            .device-info {
              flex: 1;

              .device-name {
                font-size: 14px;
                font-weight: 500;
                color: rgba(255, 255, 255, 0.9);
                margin-bottom: 4px;
              }

              .device-status {
                font-size: 12px;
                color: rgba(255, 255, 255, 0.5);

                &.online {
                  color: #67c23a;
                }
              }
            }
          }
        }
      }

      .center-panel {
        flex: 1;
        background: rgba(255, 255, 255, 0.05);
        border: 1px solid rgba(255, 255, 255, 0.1);
        border-radius: 8px;
        display: flex;
        flex-direction: column;

        .map-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 16px;
          border-bottom: 1px solid rgba(255, 255, 255, 0.1);

          .map-title {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 16px;
            font-weight: 500;
            color: rgba(255, 255, 255, 0.9);
          }

          .map-actions {
            display: flex;
            gap: 8px;
          }
        }

        .map-container {
          flex: 1;
          overflow: auto;
          padding: 20px;

          .floor-plan-container {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;

            :deep(svg) {
              max-width: 100%;
              max-height: 100%;
              width: auto;
              height: auto;
            }
          }

          .floor-plan-placeholder {
            height: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            gap: 16px;
            color: rgba(255, 255, 255, 0.4);

            p {
              margin: 0;
              font-size: 14px;
            }
          }
        }
      }
    }

    // 列表视图
    .list-view {
      height: 100%;
      display: flex;
      gap: 20px;
      padding: 20px;

      .left-panel {
        width: 300px;
        background: rgba(255, 255, 255, 0.05);
        border-radius: 8px;
        border: 1px solid rgba(255, 255, 255, 0.1);
        display: flex;
        flex-direction: column;
        overflow-y: auto;

        .panel-header {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 16px;
          border-bottom: 1px solid rgba(255, 255, 255, 0.1);
          font-size: 16px;
          font-weight: 500;
          color: rgba(255, 255, 255, 0.9);
        }

        .filter-section {
          padding: 16px;
          border-bottom: 1px solid rgba(255, 255, 255, 0.05);

          .section-title {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 12px;
            font-size: 14px;
            font-weight: 500;
            color: rgba(255, 255, 255, 0.8);
          }

          .time-picker {
            width: 100%;
          }

          .camera-tree {
            max-height: 300px;
            overflow-y: auto;

            :deep(.el-tree) {
              background: transparent;
              color: rgba(255, 255, 255, 0.8);

              .el-tree-node__content {
                &:hover {
                  background: rgba(255, 255, 255, 0.1);
                }
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
                  color: #409eff;
                  font-size: 12px;
                }
              }
            }
          }

          :deep(.el-checkbox-group) {
            display: flex;
            flex-direction: column;
            gap: 8px;
          }
        }

        .filter-actions {
          display: flex;
          gap: 8px;
          padding: 16px;

          .el-button {
            flex: 1;
          }
        }
      }

      .right-panel {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 20px;
        overflow-y: auto;

        .recording-list-section,
        .video-player-section {
          background: rgba(255, 255, 255, 0.05);
          border-radius: 8px;
          border: 1px solid rgba(255, 255, 255, 0.1);
          padding: 20px;

          .section-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;

            h3 {
              margin: 0;
              font-size: 18px;
              font-weight: 500;
              color: rgba(255, 255, 255, 0.9);
            }

            .list-info {
              color: rgba(255, 255, 255, 0.6);
              font-size: 14px;
            }

            .player-info {
              color: rgba(255, 255, 255, 0.6);
              font-size: 14px;

              .separator {
                margin: 0 8px;
              }
            }
          }

          .recording-table {
            :deep(.el-table) {
              background: transparent;
              color: rgba(255, 255, 255, 0.8);

              th {
                background: rgba(255, 255, 255, 0.05);
                color: rgba(255, 255, 255, 0.9);
              }

              tr {
                background: transparent;

                &:hover > td {
                  background: rgba(255, 255, 255, 0.05);
                }
              }

              td {
                border-color: rgba(255, 255, 255, 0.1);
              }
            }

            .pagination-section {
              display: flex;
              justify-content: flex-end;
              margin-top: 16px;
            }
          }

          .player-container {
            min-height: 400px;
            background: #000;
            border-radius: 8px;
            overflow: hidden;
            margin-bottom: 16px;

            .video-player {
              width: 100%;
              height: 400px;
            }

            .no-video {
              height: 400px;
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              gap: 16px;
              color: rgba(255, 255, 255, 0.4);
            }
          }

          .player-controls {
            display: flex;
            align-items: center;
            gap: 6px;
            flex-wrap: wrap;
            justify-content: space-between;
            padding: 8px 0;
            
            .controls-left,
            .controls-center,
            .controls-ops {
              display: flex;
              align-items: center;
              gap: 6px;
              flex-wrap: wrap;
            }
            :deep(.el-button) {
              padding: 4px 6px;
            }
            :deep(.el-select) {
              width: 72px;
            }
          }
        }
      }
    }
  }
}

/* 新增：多窗口播放器与时间轴样式 */
.list-view .right-panel {
  .player-section {
    display: flex;
    flex-direction: column;
    height: 100%;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 8px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    padding: 20px;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      h3 {
        margin: 0;
        font-size: 18px;
        font-weight: 500;
        color: rgba(255, 255, 255, 0.9);
      }

      .player-toolbar {
        display: flex;
        gap: 8px;
        align-items: center;
      }
    }

    .player-grid {
      flex: 1;
      display: grid;
      gap: 8px;
      padding: 0;
      background: #1e1e1e;
      min-height: 400px;
      overflow: hidden;

      &.grid-1x1 {
        grid-template-columns: 1fr;
        grid-template-rows: 1fr;
      }

      &.grid-2x2 {
        grid-template-columns: 1fr 1fr;
        grid-template-rows: 1fr 1fr;
      }

      &.grid-2x3 {
        grid-template-columns: repeat(3, 1fr);
        grid-template-rows: repeat(2, 1fr);
      }

      &.grid-3x3 {
        grid-template-columns: repeat(3, 1fr);
        grid-template-rows: repeat(3, 1fr);
      }

      &.grid-3x4 {
        grid-template-columns: repeat(4, 1fr);
        grid-template-rows: repeat(3, 1fr);
      }

      &.grid-4x4 {
        grid-template-columns: repeat(4, 1fr);
        grid-template-rows: repeat(4, 1fr);
      }

      .player-pane {
        position: relative;
        background: #0a1929;
        border: 2px solid #1e3a5f;
        border-radius: 8px;
        overflow: hidden;
        cursor: pointer;
        transition: all 0.3s;

        &.active {
          border-color: #409eff;
          box-shadow: 0 0 10px rgba(64, 158, 255, 0.5);
        }

        .pane-video {
          width: 100%;
          height: 100%;
          background: #000;
          object-fit: contain;
        }
        
        .pane-canvas {
          width: 100%;
          height: 100%;
          background: #000;
          display: block;
        }

        .pane-overlay {
          position: absolute;
          inset: 0;
          pointer-events: none;
          z-index: 1000;
          
          .overlay-top {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            padding: 8px 12px;
            background: linear-gradient(180deg, rgba(0,0,0,0.6) 0%, transparent 100%);
            
            .window-title {
              color: #fff;
              font-size: 13px;
              font-weight: 500;
            }
          }
          
          .overlay-center {
            position: absolute;
            inset: 0;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            gap: 12px;
            color: #5a7a9a;
            
            .window-label {
              margin: 0;
              font-size: 16px;
              font-weight: 500;
              color: #7a9aba;
            }
            
            .tip-text {
              margin: 0;
              font-size: 12px;
              color: #4a6a8a;
            }
            
            &.loading {
              :deep(.loading-icon) {
                animation: rotate 1.5s linear infinite;
                display: inline-block;
                transform-origin: 50% 50%;
                color: #409eff;
              }
              :deep(.loading-icon svg) {
                animation: rotate 1.5s linear infinite;
                transform-origin: 50% 50%;
              }
              .spinner {
                width: 64px;
                height: 64px;
                border: 4px solid rgba(64, 158, 255, 0.2);
                border-top-color: #409eff;
                border-radius: 50%;
                animation: rotate 1s linear infinite;
              }
              .window-label {
                color: #409eff;
                font-weight: 600;
              }
            }
            
            &.playing {
              background: rgba(0, 0, 0, 0.3);
              opacity: 0;
              transition: opacity 0.3s;
              
              .play-icon {
                color: #fff;
              }
            }
          }
          
          .overlay-bottom {
            position: absolute;
            bottom: 0;
            left: 0;
            right: 0;
            padding: 8px 12px;
            background: linear-gradient(0deg, rgba(0,0,0,0.6) 0%, transparent 100%);
            
            .device-name {
              color: #fff;
              font-size: 12px;
            }
          }
        }
        
        &:hover .overlay-center.playing {
          opacity: 1;
        }
      }
      
      .time-scale {
        height: 30px;
        background: #252525;
        border-top: 1px solid #3a3a3a;
        position: relative;
        margin-bottom: 0;
        
        .scale-marks {
          position: relative;
          height: 100%;
        }
        .scale-mark {
          position: absolute;
          bottom: 0;
          top: auto;
          width: 1px;
          height: 50%;
          background: #3a3a3a;
        }
        .scale-mark.long {
          background: #bfbfbf;
          height: 50%;
        }
        .scale-mark:not(.long) {
          height: 30%;
          bottom: 0;
        }
        .mark-time {
          position: absolute;
          bottom: calc(50% + 4px);
          left: 0;
          transform: translateX(-50%);
          font-size: 11px;
          color: #909399;
          white-space: nowrap;
        }
      }

        .timeline {
        margin-top: 0;
        
        .timeline-bar {
          position: relative;
          height: 18px; /* 使用现在一半的高度 */
          background: rgba(255, 255, 255, 0.06);
          border: 1px solid rgba(255, 255, 255, 0.1);
          border-radius: 6px;
          cursor: pointer;
          overflow: hidden;

          .timeline-seg {
            position: absolute;
            top: 0;
            bottom: 0;
            background: #f5d11b; /* 搜索到录像的时间段改为黄色 */
          }

          .timeline-cursor {
            position: absolute;
            top: 0;
            bottom: 0;
            width: 0;
            transform: translateX(-1px);
            pointer-events: none;
            
            .cursor-line {
              position: absolute;
              left: 0;
              top: 0;
              bottom: 0;
              width: 2px;
              background: #409eff;
            }
            .cursor-time {
              position: absolute;
              top: -24px;
              left: -40px;
              padding: 2px 8px;
              font-size: 12px;
              color: #333;
              background: #eaeaea;
              border-radius: 12px;
            }
          }
        }
        
        .timeline-meta {
          display: flex;
          justify-content: space-between;
          margin-top: 6px;
          font-size: 12px;
          color: rgba(255, 255, 255, 0.6);
        }
        .timeline-labels {
          display: flex;
          justify-content: space-between;
          margin-top: 6px;
          font-size: 12px;
          color: rgba(255, 255, 255, 0.6);
        }
      }
    }
  }
}
</style>

<style lang="scss">
@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
