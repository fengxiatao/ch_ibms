<template>
  <ContentWrap
    :body-style="{ padding: '0', height: '100%', display: 'flex', flexDirection: 'column' }"
    style="
      height: calc(100vh - var(--page-top-gap, 70px));
      padding-top: var(--page-top-gap, 70px);
      margin-bottom: 0;
    "
  >
    <div class="dark-theme-page">
      <div class="video-playback-container">
        <div class="main-layout">
          <div class="smartpss-layout">
            <div class="left-panel">
              <el-collapse v-model="leftPanelActive">
                <el-collapse-item name="device">
                  <template #title>
                    <span>设备</span>
                  </template>
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
                    :data="cameraTreeData"
                    :props="treeProps"
                    :lazy="true"
                    :load="loadTreeNode"
                    :accordion="true"
                    node-key="id"
                    @node-click="handleCameraSelect"
                    class="device-tree"
                    :allow-drag="allowDrag"
                  >
                    <template #default="{ data }">
                      <div
                        class="tree-node"
                        :draggable="data.type === 'channel'"
                        @dragstart="handleDragStart($event, data)"
                        @dblclick="handleChannelDoubleClick(data)"
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
                          icon="ep:folder-opened"
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
                </el-collapse-item>
                <el-collapse-item name="views">
                  <template #title>
                    <span>视图</span>
                  </template>
                  <div class="views-panel">
                    <!-- 新建分组按钮 -->
                    <div class="group-actions" style="padding: 8px; border-bottom: 1px solid #eee">
                      <el-button
                        size="small"
                        type="primary"
                        @click="handleCreateGroup"
                        style="width: 100%"
                      >
                        <Icon icon="ep:folder-add" style="margin-right: 4px" />
                        新建分组
                      </el-button>
                    </div>

                    <el-tree :data="viewGroups" node-key="id" @node-click="handleViewTreeNodeClick">
                      <template #default="{ data }">
                        <div
                          class="tree-node"
                          @contextmenu.prevent="
                            data.type === 'view'
                              ? handleViewContextMenu($event, data)
                              : data.type === 'group'
                                ? handleGroupContextMenu($event, data)
                                : null
                          "
                          :style="{ cursor: 'pointer' }"
                        >
                          <Icon :icon="data.icon" />
                          <span>{{ data.name }}</span>
                          <span v-if="data.type === 'view' && data.gridLayout" class="view-info">
                            ({{ data.gridLayout }}分屏)
                          </span>
                          <span
                            v-if="
                              data.type === 'group' && data.children && data.children.length > 0
                            "
                            class="view-count"
                          >
                            ({{ data.children.length }})
                          </span>
                        </div>
                      </template>
                    </el-tree>
                  </div>
                </el-collapse-item>
                <el-collapse-item name="ptz" :disabled="!activePaneSupportsPtz">
                  <template #title>
                    <span :class="{ 'disabled-text': !activePaneSupportsPtz }">云台</span>
                    <span v-if="!activePaneSupportsPtz" class="ptz-tip"
                      >（当前通道不支持云台）</span
                    >
                  </template>
                  <div class="ptz-panel">
                    <!-- 预设点列表 - 放在最上面 -->
                    <div class="ptz-preset-list">
                      <div class="preset-list-header">
                        <span>预设点列表</span>
                        <span v-if="presetList.length > 0" class="preset-count"
                          >{{ presetList.length }}/255</span
                        >
                      </div>
                      <div class="preset-list-items" v-if="presetList.length > 0">
                        <div
                          v-for="preset in presetList"
                          :key="preset.id"
                          class="preset-item"
                          :class="{ active: selectedPresetNo === preset.presetNo }"
                          @click="handleSelectPreset(preset)"
                        >
                          <span class="preset-no">{{ preset.presetNo }}</span>
                          <span class="preset-name">{{
                            preset.presetName || `预设点${preset.presetNo}`
                          }}</span>
                          <el-icon
                            class="preset-delete"
                            @click.stop="handleDeletePreset(preset.id)"
                          >
                            <Close />
                          </el-icon>
                        </div>
                      </div>
                      <div class="preset-empty" v-else>
                        <span>暂无预设点</span>
                      </div>
                    </div>

                    <!-- 预设点添加 -->
                    <div class="ptz-preset-add">
                      <el-input
                        v-model="presetPointName"
                        size="small"
                        placeholder="预设点名称"
                        clearable
                      />
                      <el-button size="small" type="primary" @click="handleAddPreset"
                        >添加</el-button
                      >
                    </div>

                    <!-- 巡航管理 -->
                    <div class="ptz-cruise-section">
                      <div class="cruise-header">
                        <span>巡航路线</span>
                        <el-button
                          size="small"
                          type="primary"
                          @click="showCruiseDialog = true"
                          :disabled="presetList.length < 2"
                        >
                          创建巡航
                        </el-button>
                      </div>
                      <div class="cruise-list" v-if="cruiseList.length > 0">
                        <div
                          v-for="cruise in cruiseList"
                          :key="cruise.id"
                          class="cruise-item"
                          :class="{ running: cruise.status === 1 }"
                        >
                          <div class="cruise-info">
                            <Icon
                              v-if="cruise.status === 1"
                              icon="ep:loading"
                              class="status-icon running"
                            />
                            <span class="cruise-name">{{ cruise.cruiseName }}</span>
                            <span class="cruise-points">{{ cruise.points?.length || 0 }}个点</span>
                          </div>
                          <div class="cruise-actions">
                            <el-button
                              v-if="cruise.status === 0"
                              size="small"
                              type="success"
                              :icon="VideoPlay"
                              circle
                              title="启动巡航"
                              @click="handleStartCruise(cruise)"
                            />
                            <el-button
                              v-else
                              size="small"
                              type="warning"
                              :icon="VideoPause"
                              circle
                              title="停止巡航"
                              @click="handleStopCruise(cruise)"
                            />
                            <el-button
                              size="small"
                              :icon="Edit"
                              circle
                              title="编辑"
                              @click="handleEditCruise(cruise)"
                            />
                            <el-button
                              size="small"
                              type="danger"
                              :icon="Delete"
                              circle
                              title="删除"
                              @click="handleDeleteCruise(cruise.id)"
                            />
                          </div>
                        </div>
                      </div>
                      <div class="cruise-empty" v-else>
                        <span>暂无巡航路线</span>
                        <span v-if="presetList.length < 2" class="tip-text"
                          >（至少需要2个预设点）</span
                        >
                      </div>
                    </div>

                    <div class="ptz-card">
                      <div class="ptz-title">云台控制</div>
                      <div class="ptz-dpad">
                        <button
                          class="ptz-dir up"
                          @mousedown="handlePtzMoveStart('up')"
                          @mouseup="handlePtzMoveStop"
                          @mouseleave="handlePtzMoveStop"
                        >
                          <Icon icon="ep:arrow-up-bold" />
                        </button>
                        <button
                          class="ptz-dir left"
                          @mousedown="handlePtzMoveStart('left')"
                          @mouseup="handlePtzMoveStop"
                          @mouseleave="handlePtzMoveStop"
                        >
                          <Icon icon="ep:arrow-left-bold" />
                        </button>
                        <button
                          class="ptz-center"
                          @mousedown="handlePtzStart()"
                          @mouseup="handlePtzStop()"
                        >
                          <Icon icon="ep:aim" />
                        </button>
                        <button
                          class="ptz-dir right"
                          @mousedown="handlePtzMoveStart('right')"
                          @mouseup="handlePtzMoveStop"
                          @mouseleave="handlePtzMoveStop"
                        >
                          <Icon icon="ep:arrow-right-bold" />
                        </button>
                        <button
                          class="ptz-dir down"
                          @mousedown="handlePtzMoveStart('down')"
                          @mouseup="handlePtzMoveStop"
                          @mouseleave="handlePtzMoveStop"
                        >
                          <Icon icon="ep:arrow-down-bold" />
                        </button>
                      </div>
                      <div class="ptz-step">
                        <span>步长</span>
                        <el-slider v-model="ptzStep" :min="1" :max="10" :show-tooltip="false" />
                      </div>
                      <div class="ptz-grid">
                        <button
                          class="ptz-op"
                          @mousedown="handlePtzZoomInStart"
                          @mouseup="handlePtzOperationStop"
                          @mouseleave="handlePtzOperationStop"
                        >
                          <Icon icon="ep:zoom-in" />
                          <span>缩放</span>
                        </button>
                        <button
                          class="ptz-op"
                          @mousedown="handlePtzFocusInStart"
                          @mouseup="handlePtzOperationStop"
                          @mouseleave="handlePtzOperationStop"
                        >
                          <Icon icon="ep:filter" />
                          <span>聚焦</span>
                        </button>
                        <button
                          class="ptz-op"
                          @mousedown="handlePtzIrisOpenStart"
                          @mouseup="handlePtzOperationStop"
                          @mouseleave="handlePtzOperationStop"
                        >
                          <Icon icon="ep:sunny" />
                          <span>光圈</span>
                        </button>
                        <button
                          class="ptz-op"
                          @mousedown="handlePtzZoomOutStart"
                          @mouseup="handlePtzOperationStop"
                          @mouseleave="handlePtzOperationStop"
                        >
                          <Icon icon="ep:zoom-out" />
                          <span>缩放</span>
                        </button>
                        <button
                          class="ptz-op"
                          @mousedown="handlePtzFocusOutStart"
                          @mouseup="handlePtzOperationStop"
                          @mouseleave="handlePtzOperationStop"
                        >
                          <Icon icon="ep:filter" />
                          <span>聚焦</span>
                        </button>
                        <button
                          class="ptz-op"
                          @mousedown="handlePtzIrisCloseStart"
                          @mouseup="handlePtzOperationStop"
                          @mouseleave="handlePtzOperationStop"
                        >
                          <Icon icon="ep:moon" />
                          <span>光圈</span>
                        </button>
                      </div>
                    </div>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </div>

            <div class="center-panel">
              <div class="player-section">
                <div class="player-grid" :class="gridLayoutClass" ref="playerGridRef">
                  <div
                    v-for="(pane, idx) in panes"
                    :key="idx"
                    class="player-pane"
                    :class="{
                      active: activePane === idx && !isPatrolling,
                      'drag-over': dragOverPane === idx,
                      'patrol-mode': isPatrolling
                    }"
                    @click="handlePaneClick(idx, $event)"
                    @drop="handleDrop($event, idx)"
                    @dragover.prevent="handleDragOver($event, idx)"
                    @dragleave="handleDragLeave"
                  >
                    <video
                      class="pane-video"
                      :ref="setPaneVideoRef"
                      :data-index="idx"
                      :muted="pane.muted"
                      playsinline
                      autoplay
                    ></video>
                    <div class="pane-overlay">
                      <!-- 加载中状态 -->
                      <div v-if="pane.isLoading" class="overlay-center loading">
                        <Icon icon="ep:loading" :size="64" class="loading-icon" />
                        <p class="window-label">正在加载视频流...</p>
                        <p class="tip-text">{{
                          pane.channel?.channelName || pane.channel?.name
                        }}</p>
                      </div>
                      <!-- 未播放时显示提示信息 -->
                      <div v-else-if="!pane.isPlaying" class="overlay-center idle">
                        <Icon icon="ep:video-pause" :size="64" />
                        <p class="window-label">窗口 {{ idx + 1 }}</p>
                        <p class="tip-text">拖拽通道到此处播放实时视频</p>
                        <p class="tip-text">或点击通道选择窗口播放</p>
                      </div>
                      <!-- 暂停状态提示（轮巡时不显示） -->
                      <div
                        v-if="pane.isPlaying && pane.isPaused && !isPatrolling"
                        class="overlay-center paused"
                      >
                        <Icon
                          icon="ep:video-play"
                          :size="80"
                          style="color: rgba(255, 255, 255, 0.9)"
                        />
                        <p
                          style="color: rgba(255, 255, 255, 0.9); font-size: 16px; margin-top: 12px"
                          >已暂停</p
                        >
                        <p style="color: rgba(255, 255, 255, 0.7); font-size: 12px; margin-top: 4px"
                          >点击上方按钮继续播放</p
                        >
                      </div>

                      <!-- 播放时不显示任何覆盖层，保持画面清晰 -->
                      <!-- 底部通道名称也在播放时隐藏 -->
                      <div v-if="pane.channel && !pane.isPlaying" class="overlay-bottom"
                        ><span class="device-name">{{
                          pane.channel.channelName || pane.channel.name
                        }}</span></div
                      >

                      <!-- 悬停工具栏（仅在播放时显示，轮巡时隐藏） -->
                      <div v-if="pane.isPlaying && !isPatrolling" class="pane-toolbar">
                        <!-- 停止按钮 -->
                        <el-button
                          size="small"
                          @click.stop="handleStopPlay(idx)"
                          title="停止播放"
                          type="danger"
                        >
                          <Icon icon="ep:video-camera-filled" />
                        </el-button>

                        <!-- 暂停/继续按钮 -->
                        <el-button
                          size="small"
                          @click.stop="handleTogglePause(idx)"
                          :title="pane.isPaused ? '继续播放' : '暂停播放'"
                        >
                          <Icon :icon="pane.isPaused ? 'ep:video-play' : 'ep:video-pause'" />
                        </el-button>

                        <!-- 截图按钮 -->
                        <el-button
                          size="small"
                          @click.stop="handlePaneScreenshot(idx)"
                          title="截图"
                        >
                          <Icon icon="ep:camera" />
                        </el-button>

                        <!-- 截图设置 -->
                        <el-popover placement="bottom" :width="280" trigger="click">
                          <template #reference>
                            <el-button size="small" title="截图设置">
                              <Icon icon="ep:setting" />
                            </el-button>
                          </template>
                          <div style="padding: 8px 0">
                            <div style="margin-bottom: 12px">
                              <el-checkbox v-model="screenshotSettings.uploadToServer">
                                截图自动上传到服务器
                              </el-checkbox>
                              <div
                                style="
                                  font-size: 12px;
                                  color: #909399;
                                  margin-top: 4px;
                                  margin-left: 24px;
                                "
                              >
                                开启后，截图会同时保存到本地和服务器
                              </div>
                            </div>
                          </div>
                        </el-popover>

                        <!-- 录像按钮 -->
                        <el-button
                          v-if="!pane.isRecording"
                          size="small"
                          @click.stop="handleStartRecording(idx)"
                          title="开始录像"
                          type="danger"
                        >
                          <Icon icon="ep:video-camera" />
                        </el-button>
                        <el-button
                          v-else
                          size="small"
                          @click.stop="handleStopRecording(idx)"
                          title="停止录像"
                          type="success"
                        >
                          <Icon icon="ep:video-pause" />
                        </el-button>

                        <!-- 码流切换 -->
                        <el-dropdown
                          @command="(cmd) => handleStreamSwitch(idx, cmd)"
                          trigger="click"
                        >
                          <el-button size="small" title="码流切换">
                            <Icon icon="ep:switch" />
                            <span style="margin-left: 4px">{{
                              pane.streamType === 'sub' ? '子' : '主'
                            }}</span>
                          </el-button>
                          <template #dropdown>
                            <el-dropdown-menu>
                              <el-dropdown-item
                                command="main"
                                :disabled="pane.streamType !== 'sub'"
                              >
                                主码流
                              </el-dropdown-item>
                              <el-dropdown-item command="sub" :disabled="pane.streamType === 'sub'">
                                子码流
                              </el-dropdown-item>
                            </el-dropdown-menu>
                          </template>
                        </el-dropdown>

                        <!-- 分辨率切换 -->
                        <el-dropdown
                          @command="(cmd) => handleResolutionSwitch(idx, cmd)"
                          trigger="click"
                        >
                          <el-button size="small" title="分辨率">
                            <Icon icon="ep:monitor" />
                          </el-button>
                          <template #dropdown>
                            <el-dropdown-menu>
                              <el-dropdown-item command="1920x1080">1920×1080</el-dropdown-item>
                              <el-dropdown-item command="1280x720">1280×720</el-dropdown-item>
                              <el-dropdown-item command="704x576">704×576</el-dropdown-item>
                              <el-dropdown-item command="352x288">352×288</el-dropdown-item>
                            </el-dropdown-menu>
                          </template>
                        </el-dropdown>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="playback-controls">
                  <div class="controls-left">
                    <!-- 当前视图显示 -->
                    <div class="current-view-info" v-if="currentView">
                      <Icon icon="ep:video-camera" style="color: #409eff" />
                      <span class="view-name">{{ currentView.name }}</span>
                      <el-tag size="small" type="info">{{ currentView.gridLayout }}分屏</el-tag>
                    </div>
                    <div class="current-view-info" v-else>
                      <Icon icon="ep:video-camera" style="color: #909399" />
                      <span class="view-name" style="color: #909399">未加载视图</span>
                    </div>

                    <!-- 视图操作按钮 -->
                    <el-button-group size="small">
                      <el-button @click="handleNewView" title="新建视图">
                        <Icon icon="ep:plus" />
                        新建
                      </el-button>
                      <el-button
                        @click="handleSaveView"
                        :disabled="!currentView"
                        title="保存当前视图"
                      >
                        <Icon icon="ep:document-checked" />
                        保存
                      </el-button>
                      <el-button @click="handleSaveAsView" title="另存为新视图">
                        <Icon icon="ep:document-copy" />
                        另存为
                      </el-button>
                      <el-button
                        @click="handleUpdateView"
                        :disabled="!currentView"
                        title="更新当前视图"
                      >
                        <Icon icon="ep:edit" />
                        更新
                      </el-button>
                    </el-button-group>

                    <!-- 轮巡功能 -->
                    <el-divider direction="vertical" />
                    <el-select
                      v-model="selectedPatrolPlan"
                      size="small"
                      style="width: 140px"
                      placeholder="选择轮巡计划"
                      :disabled="isPatrolling"
                    >
                      <el-option
                        v-for="p in patrolPlans"
                        :key="p.id"
                        :value="p.id"
                        :label="p.name"
                      />
                    </el-select>
                    <el-button
                      size="small"
                      :type="isPatrolling ? 'danger' : 'success'"
                      @click="handleStartPatrol"
                      :title="isPatrolling ? '停止轮巡' : '开始轮巡'"
                    >
                      <Icon :icon="isPatrolling ? 'ep:video-pause' : 'ep:video-play'" />
                      {{ isPatrolling ? '停止' : '开始' }}
                    </el-button>
                    <el-button
                      size="small"
                      @click="handleOpenPatrolConfig"
                      title="轮巡配置"
                      :disabled="isPatrolling"
                    >
                      <Icon icon="ep:setting" />
                    </el-button>
                  </div>

                  <div class="controls-right">
                    <!-- 停止所有播放器按钮 -->
                    <el-button
                      size="small"
                      @click="handleStopAllPlayers"
                      type="danger"
                      title="停止所有播放器"
                    >
                      <Icon icon="ep:video-camera-filled" />
                      停止全部
                    </el-button>

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

    <el-dialog v-model="exportDialogVisible" title="导出录像" width="600px">
      <el-tabs v-model="exportTab" class="export-tabs">
        <el-tab-pane label="正在导出" name="exporting">
          <div class="export-toolbar">
            <el-button size="small" @click="handlePauseExport"
              ><Icon icon="ep:video-pause" />暂停</el-button
            >
            <el-button size="small" @click="handleOpenExport"
              ><Icon icon="ep:download" />打开</el-button
            >
            <el-button size="small" @click="handleDeleteExport"
              ><Icon icon="ep:delete" />删除</el-button
            >
          </div>
          <el-table :data="exportingList" style="width: 100%" height="300">
            <el-table-column type="selection" width="55" />
            <el-table-column prop="name" label="全部" width="180" />
            <el-table-column prop="startTime" label="开始时间" width="150" />
            <el-table-column prop="endTime" label="结束时间" width="150" />
            <el-table-column label="大小 (KB)" width="100">
              <template #default="scope">{{ (scope.row.size / 1024).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope"
                ><el-progress :percentage="scope.row.progress" :status="scope.row.status"
              /></template>
            </el-table-column>
            <el-table-column label="操作">
              <template #default="scope"
                ><el-button size="small" type="text" @click="handleCancelExport(scope.row)"
                  >取消</el-button
                ></template
              >
            </el-table-column>
          </el-table>
          <div class="export-footer"
            ><span>已导出大小: {{ totalExportedSize }} KB</span
            ><el-checkbox v-model="autoOpenAfterExport">下次不再导出</el-checkbox></div
          >
        </el-tab-pane>
        <el-tab-pane label="已导出" name="exported">
          <el-table :data="exportedList" style="width: 100%" height="300">
            <el-table-column type="selection" width="55" />
            <el-table-column prop="name" label="文件名" width="200" />
            <el-table-column prop="startTime" label="开始时间" width="150" />
            <el-table-column prop="endTime" label="结束时间" width="150" />
            <el-table-column label="大小 (KB)" width="100">
              <template #default="scope">{{ (scope.row.size / 1024).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="操作">
              <template #default="scope">
                <el-button size="small" type="text" @click="handleOpenExportedFile(scope.row)"
                  >打开</el-button
                >
                <el-button size="small" type="text" @click="handleDeleteExportedFile(scope.row)"
                  >删除</el-button
                >
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <el-dialog v-model="timeCutDialogVisible" title="时间剪切" width="500px">
      <div class="time-cut-form">
        <div class="time-cut-info">
          <div class="info-item"
            ><label>开始时间：</label
            ><el-date-picker
              v-model="timeCutForm.startTime"
              type="datetime"
              placeholder="选择开始时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
          /></div>
          <div class="info-item"
            ><label>结束时间：</label
            ><el-date-picker
              v-model="timeCutForm.endTime"
              type="datetime"
              placeholder="选择结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
          /></div>
          <div class="info-item"
            ><label>时长：</label><span class="duration-display">{{ cutDuration }}</span></div
          >
        </div>
        <div class="time-cut-timeline"
          ><div class="timeline-preview"
            ><div
              class="cut-range"
              :style="{ left: cutRangeLeft + '%', width: cutRangeWidth + '%' }"
            ></div></div
        ></div>
      </div>
      <template #footer
        ><el-button @click="timeCutDialogVisible = false">取消</el-button
        ><el-button type="primary" @click="handleConfirmTimeCut">确定剪切</el-button></template
      >
    </el-dialog>

    <el-dialog v-model="uploadDialogVisible" title="上传录像" width="500px">
      <div class="upload-form">
        <div class="form-item"
          ><label>摄像头</label
          ><el-input v-model="uploadForm.cameraId" placeholder="请先在左侧树选择摄像头" disabled
        /></div>
        <div class="form-item"
          ><label>录像类型</label
          ><el-select v-model="uploadForm.recordingType"
            ><el-option :value="1" label="手动录像" /><el-option
              :value="2"
              label="定时录像" /><el-option :value="3" label="报警触发" /><el-option
              :value="4"
              label="移动侦测" /></el-select
        ></div>
        <div class="form-item"
          ><label>选择文件</label
          ><input type="file" accept="video/*" @change="handleUploadFileChange"
        /></div>
      </div>
      <template #footer
        ><el-button @click="uploadDialogVisible = false">取消</el-button
        ><el-button type="primary" :loading="uploading" @click="submitUpload"
          >上传</el-button
        ></template
      >
    </el-dialog>

    <!-- 巡航对话框 -->
    <el-dialog
      v-model="showCruiseDialog"
      :title="cruiseForm.id ? '编辑巡航路线' : '创建巡航路线'"
      width="600px"
      @close="handleCruiseDialogClose"
    >
      <el-form :model="cruiseForm" label-width="120px">
        <el-form-item label="巡航名称" required>
          <el-input
            v-model="cruiseForm.cruiseName"
            placeholder="请输入巡航路线名称"
            maxlength="100"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="cruiseForm.description"
            type="textarea"
            :rows="2"
            placeholder="请输入描述"
            maxlength="500"
          />
        </el-form-item>
        <el-form-item label="停留时间" required>
          <el-input-number v-model="cruiseForm.dwellTime" :min="1" :max="300" /> 秒
          <span class="form-tip">（每个预设点的默认停留时间）</span>
        </el-form-item>
        <el-form-item label="是否循环" required>
          <el-switch v-model="cruiseForm.loopEnabled" />
          <span class="form-tip">（开启后将循环执行巡航）</span>
        </el-form-item>
        <el-form-item label="预设点" required>
          <div class="cruise-points-selector">
            <div class="available-presets">
              <div class="preset-header">可选预设点</div>
              <div class="preset-list">
                <div
                  v-for="preset in availablePresets"
                  :key="preset.id"
                  class="preset-option"
                  @click="handleAddCruisePoint(preset)"
                >
                  <span>{{ preset.presetNo }}. {{ preset.presetName }}</span>
                  <el-icon><Plus /></el-icon>
                </div>
              </div>
            </div>
            <div class="selected-presets">
              <div class="preset-header">
                已选预设点 ({{ cruiseForm.points.length }})
                <span v-if="cruiseForm.points.length < 2" class="error-tip">至少需要2个</span>
              </div>
              <div class="preset-list">
                <div
                  v-for="(point, index) in cruiseForm.points"
                  :key="index"
                  class="preset-selected"
                >
                  <div class="preset-order">
                    <el-button size="small" :disabled="index === 0" @click="movePointUp(index)">
                      <el-icon><ArrowUp /></el-icon>
                    </el-button>
                    <span class="order-num">{{ index + 1 }}</span>
                    <el-button
                      size="small"
                      :disabled="index === cruiseForm.points.length - 1"
                      @click="movePointDown(index)"
                    >
                      <el-icon><ArrowDown /></el-icon>
                    </el-button>
                  </div>
                  <div class="preset-info">
                    <span>{{ getPresetName(point.presetId) }}</span>
                    <el-input-number
                      v-model="point.dwellTime"
                      :min="1"
                      :max="300"
                      size="small"
                      placeholder="默认"
                    />
                    <span class="unit">秒</span>
                  </div>
                  <el-button size="small" type="danger" @click="handleRemoveCruisePoint(index)">
                    <el-icon><Close /></el-icon>
                  </el-button>
                </div>
              </div>
              <div v-if="cruiseForm.points.length === 0" class="empty-tip">
                请从左侧选择预设点
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCruiseDialog = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleSaveCruise"
          :disabled="!cruiseForm.cruiseName || cruiseForm.points.length < 2"
        >
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewSaveDialogVisible" title="保存视图" width="420px">
      <div class="view-save-form">
        <el-form label-width="88px">
          <el-form-item label="视图名称">
            <el-input v-model="viewSaveForm.name" placeholder="请输入视图名称" />
          </el-form-item>
          <el-form-item label="所属分组">
            <el-select
              v-model="viewSaveForm.groupIds"
              multiple
              placeholder="请选择分组（可多选）"
              style="width: 100%"
            >
              <el-option v-for="g in viewGroups" :key="g.id" :label="g.name" :value="g.id" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="viewSaveForm.saveToPatrol">保存到轮巡计划</el-checkbox>
          </el-form-item>
          <el-form-item v-if="viewSaveForm.saveToPatrol" label="轮巡计划">
            <el-select v-model="viewSaveForm.patrolPlanId" style="width: 100%">
              <el-option v-for="p in patrolPlans" :key="p.id" :label="p.name" :value="p.id" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="viewSaveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSaveView">确定</el-button>
      </template>
    </el-dialog>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoPlay, VideoPause, Edit, Delete, Search } from '@element-plus/icons-vue'
import { ContentWrap } from '@/components/ContentWrap'
import { Icon } from '@/components/Icon'
import {
  getCameraRecordingPage,
  deleteCameraRecording,
  uploadCameraRecording,
  uploadCameraSnapshot,
  getCameraPresetListByChannel,
  createCameraPreset,
  deleteCameraPreset,
  getCameraCruiseListByChannel,
  createCameraCruise,
  updateCameraCruise,
  deleteCameraCruise,
  getCameraCruise,
  startCameraCruise,
  stopCameraCruise
} from '@/api/iot/video'
import type {
  CameraRecordingRespVO,
  CameraPresetRespVO,
  CameraCruiseRespVO,
  CameraCruiseSaveReqVO,
  CameraCruisePointSaveReqVO
} from '@/api/iot/video'
import { getBuildingList } from '@/api/iot/spatial/building'
import { getFloorListByBuildingId } from '@/api/iot/spatial/floor'
import { getAreaListByFloorId } from '@/api/iot/spatial/area'
import * as FloorDxfApi from '@/api/iot/spatial/floorDxf'
import { DeviceApi } from '@/api/iot/device/device'
import { getChannelPage } from '@/api/iot/channel'
import { getLivePlayUrl, stopStream } from '@/api/iot/video/zlm'
import { nvrPtzControl } from '@/api/iot/video/nvr'
import mpegts from 'mpegts.js'
import {
  getPatrolPlanPage,
  getPatrolPlan,
  getPatrolTaskList,
  getPatrolSceneByTaskId
} from '@/api/iot/video/patrolPlan'
import {
  getVideoViewTree,
  getVideoView,
  createVideoView,
  updateVideoView,
  deleteVideoView,
  getVideoViewGroupList,
  createVideoViewGroup,
  deleteVideoViewGroup
} from '@/api/iot/video/videoView'

const currentViewMode = ref<'smartpss' | 'map' | 'list'>('smartpss')
const activeFilterTab = ref<'space' | 'nvr'>('space')
const spaceTreeData = ref<any[]>([])
const currentFloor = ref<any>(null)
const floorPlanSvg = ref('')
const mapContainerRef = ref<HTMLElement>()
const allDevices = ref<any[]>([])
const cameraTreeData = ref<any[]>([])
const treeProps = {
  children: 'children',
  label: 'name',
  isLeaf: (data: any) => data.type === 'device' || data.type === 'channel'
}
const filteredDevices = computed(() =>
  allDevices.value.filter((d: any) => recordingList.value.some((r) => r.cameraId === d.id))
)

const filterForm = reactive({
  timeRange: [] as string[],
  cameraId: undefined as number | undefined,
  recordingTypes: [] as number[]
})
const recordingList = ref<CameraRecordingRespVO[]>([])
const currentRecording = ref<CameraRecordingRespVO | null>(null)
const loading = ref(false)
const total = ref(0)
const pagination = reactive({ page: 1, size: 10 })

const isPlaying = ref(false)
const playbackSpeed = ref(1)
const isMutedAll = ref(true)

// ZLMediaKit + mpegts.js 播放器相关
const isFirefox = navigator.userAgent.toLowerCase().includes('firefox')
let playQueue: Promise<void> = Promise.resolve()

// 智能路由：内网/公网自动选择
const isIntranetAccess = (): boolean => {
  const hostname = window.location.hostname
  if (hostname === 'localhost' || hostname === '127.0.0.1') return true
  if (hostname.startsWith('192.168.')) return true
  if (hostname.startsWith('10.')) return true
  if (hostname.startsWith('172.')) {
    const secondOctet = parseInt(hostname.split('.')[1])
    if (secondOctet >= 16 && secondOctet <= 31) return true
  }
  return false
}

const adaptPlayUrls = (urls: any): any => {
  if (!urls) return urls
  const intranet = isIntranetAccess()
  if (intranet) return urls

  const adapted = { ...urls }
  const publicHost = window.location.hostname
  const publicPort = window.location.port ? parseInt(window.location.port) : 80
  const publicAddr =
    publicPort === 80 || publicPort === 443 ? publicHost : `${publicHost}:${publicPort}`
  const isHttps = window.location.protocol === 'https:'
  const httpProtocol = isHttps ? 'https' : 'http'
  const wsProtocol = isHttps ? 'wss' : 'ws'

  const replaceUrl = (url: string, isWs = false): string => {
    if (!url) return url
    return url
      .replace(/192\.168\.\d+\.\d+:\d+/g, publicAddr)
      .replace(/192\.168\.\d+\.\d+/g, publicHost)
      .replace(/^(http|ws):/, isWs ? `${wsProtocol}:` : `${httpProtocol}:`)
  }

  adapted.wsFlvUrl = replaceUrl(urls.wsFlvUrl, true)
  adapted.flvUrl = replaceUrl(urls.flvUrl)
  adapted.hlsUrl = replaceUrl(urls.hlsUrl)
  if (urls.webrtcUrl) adapted.webrtcUrl = replaceUrl(urls.webrtcUrl)

  return adapted
}
const syncPlayback = ref(false)
const currentPlayTime = ref(0)

const exportDialogVisible = ref(false)
const exportTab = ref('exporting')
const exportingList = ref<any[]>([])
const exportedList = ref<any[]>([])
const totalExportedSize = ref(0)
const autoOpenAfterExport = ref(false)

const timeCutDialogVisible = ref(false)
const timeCutForm = reactive({ startTime: '', endTime: '' })

const deviceSearchKeyword = ref('')
const handleChannelSearch = async () => {
  const keyword = deviceSearchKeyword.value.trim()
  if (!keyword) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  try {
    const result: any = await getChannelPage({
      channelName: keyword,
      channelType: 'video',
      pageNo: 1,
      pageSize: 100
    })
    const list = result?.list || []
    if (list.length > 0) {
      cameraTreeData.value = list.map((ch: any) => ({
        id: `channel-${ch.id}`,
        name: ch.channelName || `通道${ch.channelNo}`,
        type: 'channel',
        channelId: ch.id,
        channelNo: ch.channelNo,
        deviceId: ch.deviceId,
        channel: ch
      }))
      ElMessage.success(`找到 ${list.length} 个匹配的视频通道`)
    } else {
      cameraTreeData.value = []
      ElMessage.info('未找到匹配的视频通道')
    }
  } catch (e: any) {
    ElMessage.error('搜索失败: ' + (e?.message || e || '未知错误'))
  }
}
const handleSearchClear = () => {
  deviceSearchKeyword.value = ''
  loadSpaceTree()
}
const recordingMode = ref('video')
const selectedRecordingType = ref(0)
const selectedTimeRange = ref(0)

const uploadDialogVisible = ref(false)
const uploadForm = reactive({
  cameraId: undefined as number | undefined,
  recordingType: 1 as number,
  file: null as File | null
})
const uploading = ref(false)
const openUploadDialog = () => {
  uploadForm.cameraId = filterForm.cameraId
  uploadForm.recordingType = 1
  uploadForm.file = null
  uploadDialogVisible.value = true
}
const handleUploadFileChange = (e: Event) => {
  const input = e.target as HTMLInputElement
  const files = input.files
  if (files && files.length > 0) uploadForm.file = files[0]
}
const submitUpload = async () => {
  if (!uploadForm.cameraId) return ElMessage.error('请在左侧选择一个摄像头')
  if (!uploadForm.file) return ElMessage.error('请选择要上传的录像文件')
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

const leftPanelActive = ref<string[]>(['device', 'ptz'])

const gridLayout = ref<number>(6)
const activePane = ref<number>(0)

// 判断当前活动窗口的通道是否支持云台
const activePaneSupportsPtz = computed<boolean>(() => {
  const pane = panes.value[activePane.value]
  if (!pane || !pane.channel || !pane.isPlaying) {
    return false
  }

  const channel = pane.channel
  // 直接使用通道的 ptzSupport 字段（来自数据库的 ptz_support）
  // 兼容 true/1/'1' 等多种格式
  return channel.ptzSupport === true || channel.ptzSupport === 1 || channel.ptzSupport === '1'
})
// 窗格类型定义
interface PaneType {
  channel: any | null
  videoEl: HTMLVideoElement | null
  muted: boolean
  isPlaying: boolean
  isPaused?: boolean // 是否暂停
  isLoading?: boolean // 是否正在加载视频流
  isRecording?: boolean // 是否正在录像
  recordingStartTime?: Date // 录像开始时间
  recordPlayer?: any // 录像专用播放器实例
  streamType?: 'main' | 'sub' // 码流类型
}

const panes = ref<Array<PaneType>>([])
const playerGridRef = ref<HTMLElement | null>(null)
const dragOverPane = ref<number>(-1)
const gridCols = computed<number>(() =>
  gridLayout.value === 1
    ? 1
    : gridLayout.value === 4
      ? 2
      : gridLayout.value === 9
        ? 3
        : gridLayout.value === 16 || gridLayout.value === 12
          ? 4
          : 2
)
const gridLayoutClass = computed<string>(() =>
  gridLayout.value === 1
    ? 'grid-1x1'
    : gridLayout.value === 4
      ? 'grid-2x2'
      : gridLayout.value === 6
        ? 'grid-2x3'
        : gridLayout.value === 9
          ? 'grid-3x3'
          : gridLayout.value === 12
            ? 'grid-3x4'
            : gridLayout.value === 16
              ? 'grid-4x4'
              : 'grid-2x3'
)
const stopAllPlayersSilently = () => {
  if (isPatrolling.value) {
    stopPatrol()
  }
  for (let i = 0; i < panes.value.length; i++) {
    const pane: any = panes.value[i]
    try {
      if (pane?.isRecording && pane.recordPlayer) {
        try {
          pane.recordPlayer.startRecord(false)
        } catch {}
      }

      // 关闭 WebRTC 连接
      if (pane?.pc) {
        try {
          pane.pc.close()
        } catch {}
        pane.pc = null
      }

      // 正确销毁 mpegts.js 播放器：pause -> unload -> detachMediaElement -> destroy
      const player = pane?.player
      if (player) {
        try {
          if (typeof player.pause === 'function') player.pause()
        } catch {}
        try {
          if (typeof player.unload === 'function') player.unload()
        } catch {}
        try {
          if (typeof player.detachMediaElement === 'function') player.detachMediaElement()
        } catch {}
        try {
          if (typeof player.destroy === 'function') player.destroy()
        } catch {}
      }

      const videoEl = pane?.videoEl as HTMLVideoElement | null
      if (videoEl) {
        // 清理 WebRTC 流
        if (videoEl.srcObject) {
          try {
            const stream = videoEl.srcObject as MediaStream
            stream.getTracks().forEach((track) => track.stop())
          } catch {}
          videoEl.srcObject = null
        }
        try {
          videoEl.pause()
        } catch {}
        videoEl.src = ''
        try {
          videoEl.load()
        } catch {}
      }

      if (pane) {
        pane.channel = null
        pane.isPlaying = false
        pane.isPaused = false
        pane.isLoading = false
        pane.isRecording = false
        pane.recordingStartTime = undefined
        pane.recordPlayer = null
        pane.player = null
        pane.playMode = null
      }
    } catch {}
  }
}
const setLayout = (val: number) => {
  stopAllPlayersSilently()
  gridLayout.value = val
  panes.value = Array.from({ length: val }, () => ({
    channel: null,
    videoEl: null,
    muted: true,
    isPlaying: false,
    isPaused: false,
    isLoading: false,
    isRecording: false,
    streamType: 'main'
  }))
  activePane.value = 0
}
const allowDrag = (node: any) => node.data.type === 'channel'
const handleDragStart = (e: DragEvent, data: any) => {
  if (data.type !== 'channel') return
  e.dataTransfer!.effectAllowed = 'copy'
  e.dataTransfer!.setData('channel', JSON.stringify(data))
}
const handleDragOver = (e: DragEvent, paneIndex: number) => {
  dragOverPane.value = paneIndex
}
const handleDragLeave = () => {
  dragOverPane.value = -1
}
const handleDrop = async (e: DragEvent, paneIndex: number) => {
  e.preventDefault()
  dragOverPane.value = -1
  try {
    const channelData = JSON.parse(e.dataTransfer!.getData('channel'))
    await playChannelInPane(channelData, paneIndex)
  } catch (err) {
    console.error('拖拽失败:', err)
  }
}

/**
 * 处理通道双击事件
 * 自动添加到下一个空闲窗口
 */
const handleChannelDoubleClick = async (data: any) => {
  // 只处理通道类型
  if (data.type !== 'channel') return

  // 查找第一个空闲窗口（没有通道的窗口）
  const emptyPaneIndex = panes.value.findIndex((pane) => !pane.channel)

  if (emptyPaneIndex === -1) {
    // 没有空闲窗口，弹出提示
    ElMessage.warning('所有窗口都已占用，请先关闭一个窗口或拖拽到指定窗口')
    return
  }

  // 添加到空闲窗口
  try {
    await playChannelInPane(data, emptyPaneIndex)
    ElMessage.success(`已添加到窗口 ${emptyPaneIndex + 1}`)
  } catch (err) {
    console.error('[双击添加] 失败:', err)
    ElMessage.error('添加通道失败')
  }
}
const playChannelInPane = async (channelData: any, paneIndex?: number) => {
  const idx = typeof paneIndex === 'number' ? paneIndex : activePane.value
  const pane = panes.value[idx]
  if (!pane) return

  // 设置加载状态
  pane.isLoading = true
  pane.isPlaying = false

  // 停止之前的播放器
  stopPanePlayer(idx)

  const channel = channelData.channel || channelData
  pane.channel = channel

  try {
    const channelId = channel.id || channelData.channelId
    if (!channelId) {
      ElMessage.warning('无效的通道ID')
      pane.channel = null
      pane.isLoading = false
      return
    }

    console.log('[实时预览] 开始播放通道:', channel.channelName || channel.name, 'ID:', channelId)

    // 通过 ZLMediaKit API 获取播放地址
    const subtype = (pane as any).streamType === 'sub' ? 1 : 0
    const rawPlayUrls = await getLivePlayUrl(channelId, subtype)
    const playUrls = adaptPlayUrls(rawPlayUrls)

    console.log('[实时预览] 获取到播放地址:', playUrls)

    if (!playUrls || (!playUrls.wsFlvUrl && !playUrls.webrtcUrl)) {
      throw new Error('未获取到有效的播放地址')
    }

    await nextTick()

    const videoEl = pane.videoEl
    if (!videoEl) {
      throw new Error('视频元素未找到')
    }

    let success = false

    // 优先尝试 WebRTC（延迟最低 ~200ms）
    if (playUrls.webrtcUrl) {
      console.log('[实时预览] 尝试 WebRTC 播放...')
      success = await playWithWebRTC(pane, videoEl, playUrls.webrtcUrl, idx)
      if (success) {
        ;(pane as any).playMode = 'webrtc'
      }
    }

    // WebRTC 失败则回退到 FLV（延迟 ~500ms）
    if (!success && playUrls.wsFlvUrl) {
      console.log('[实时预览] WebRTC 失败，回退到 FLV 播放...')
      success = await playWithFLV(pane, videoEl, playUrls.wsFlvUrl, idx)
      if (success) {
        ;(pane as any).playMode = 'flv'
      }
    }

    if (success) {
      const mode = (pane as any).playMode === 'webrtc' ? 'WebRTC' : 'FLV'
      console.log(
        `[实时预览] ✅ 窗口 ${idx + 1} 播放成功 [${mode}]: ${channel.channelName || channel.name}`
      )
      ElMessage.success(`正在播放: ${channel.channelName || channel.name}`)
    } else {
      throw new Error('播放失败')
    }
  } catch (e: any) {
    console.error('[实时预览] 播放失败:', e)
    ElMessage.error('播放失败：' + (e?.message || e))
    pane.channel = null
    pane.isLoading = false
  }
}

// WebRTC 播放（延迟最低 ~200ms）
// ZLMediaKit WebRTC 接口返回 JSON 格式: { "code": 0, "sdp": "v=0\r\n..." }
const playWithWebRTC = async (
  pane: any,
  videoEl: HTMLVideoElement,
  webrtcUrl: string,
  idx: number
): Promise<boolean> => {
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
        videoEl
          .play()
          .then(() => {
            pane.isPlaying = true
            pane.isLoading = false
            ;(pane as any).pc = pc
            safeResolve(true)
          })
          .catch((e) => {
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
      pc.createOffer()
        .then((offer) => {
          return pc.setLocalDescription(offer)
        })
        .then(() => {
          // 发送 SDP 到 ZLMediaKit WebRTC 接口
          return fetch(webrtcUrl, {
            method: 'POST',
            headers: { 'Content-Type': 'application/sdp' },
            body: pc.localDescription?.sdp
          })
        })
        .then((response) => {
          if (!response.ok) throw new Error(`HTTP ${response.status}`)
          return response.text()
        })
        .then((responseText) => {
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

          return pc.setRemoteDescription({ type: 'answer', sdp })
        })
        .then(() => {
          console.log(`[WebRTC] 窗口 ${idx + 1} SDP 交换完成`)
        })
        .catch((e) => {
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

// mpegts.js FLV 播放
const playWithFLV = async (
  pane: any,
  videoEl: HTMLVideoElement,
  wsFlvUrl: string,
  idx: number,
  disableAudio: boolean = false
): Promise<boolean> => {
  const createPlayer = async (): Promise<boolean> => {
    return new Promise((resolve) => {
      try {
        if (!mpegts.isSupported()) {
          throw new Error('浏览器不支持 FLV 播放')
        }

        const player = mpegts.createPlayer(
          {
            type: 'flv',
            url: wsFlvUrl,
            isLive: true,
            hasAudio: !disableAudio, // 如果音频编码不支持，禁用音频
            hasVideo: true
          },
          {
            enableWorker: false,
            enableStashBuffer: true,
            stashInitialSize: isFirefox ? 256 : 128,
            lazyLoad: false,
            autoCleanupSourceBuffer: true,
            autoCleanupMaxBackwardDuration: isFirefox ? 5 : 3,
            autoCleanupMinBackwardDuration: isFirefox ? 2 : 1,
            liveBufferLatencyChasing: true,
            liveBufferLatencyMaxLatency: isFirefox ? 2.0 : 1.5,
            liveBufferLatencyMinRemain: isFirefox ? 0.5 : 0.3,
            liveSync: true,
            fixAudioTimestampGap: true
          }
        )

        let audioUnsupported = false

        player.on(mpegts.Events.ERROR, (_errorType: any, errorDetail: any) => {
          const errorStr = String(errorDetail || '')
          // 检测音频编解码器不支持
          if (errorStr.includes('CodecUnsupported') && errorStr.includes('audio')) {
            audioUnsupported = true
            console.warn(`[实时预览] 窗口 ${idx + 1} 音频编码不支持，将以纯视频模式重试`)
            // 停止当前播放器
            try {
              player.pause()
              player.unload()
              player.detachMediaElement()
              player.destroy()
            } catch {}
            // 以无音频模式重试
            playWithFLV(pane, videoEl, wsFlvUrl, idx, true).then(resolve)
            return
          }
          // 忽略其他 MSE 警告
          if (errorStr.includes('MSEError') || errorStr.includes('SourceBuffer')) return
          console.error(`[实时预览] 窗口 ${idx + 1} 播放错误:`, errorDetail)
          pane.isPlaying = false
          pane.isLoading = false
        })

        player.attachMediaElement(videoEl)
        player.load()

        const playDelay = isFirefox ? 500 : 100
        setTimeout(async () => {
          if (audioUnsupported) return // 如果已触发重试，不继续
          try {
            await player.play()
            ;(pane as any).player = player
            pane.isPlaying = true
            pane.isLoading = false
            if (disableAudio) {
              console.log(`[实时预览] ✅ 窗口 ${idx + 1} 以纯视频模式播放成功（音频编码不支持）`)
            }
            resolve(true)
          } catch {
            pane.isLoading = false
            resolve(false)
          }
        }, playDelay)
      } catch {
        resolve(false)
      }
    })
  }

  // Firefox 使用串行队列
  if (isFirefox) {
    return new Promise((resolve) => {
      playQueue = playQueue.then(async () => {
        const result = await createPlayer()
        await new Promise((r) => setTimeout(r, 300))
        resolve(result)
      })
    })
  }
  return createPlayer()
}

// 停止窗格播放器
const stopPanePlayer = (paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (!pane) return

  // 清理 WebRTC 连接
  if ((pane as any).pc) {
    try {
      ;(pane as any).pc.close()
    } catch (e) {
      console.warn('[实时预览] 关闭 WebRTC 连接异常:', e)
    }
    ;(pane as any).pc = null
  }

  // 清理 mpegts.js 播放器
  if ((pane as any).player) {
    try {
      const player = (pane as any).player
      if (typeof player.pause === 'function') player.pause()
      if (typeof player.unload === 'function') player.unload()
      if (typeof player.detachMediaElement === 'function') player.detachMediaElement()
      if (typeof player.destroy === 'function') player.destroy()
    } catch (e) {
      console.warn('[实时预览] 停止播放器异常:', e)
    }
    ;(pane as any).player = null
  }

  if (pane.videoEl) {
    pane.videoEl.srcObject = null
    pane.videoEl.src = ''
  }

  // 通知后端停止拉流（可选，节省资源）
  if (pane.channel) {
    const channelId = pane.channel.id || pane.channel.channelId
    if (channelId) {
      stopStream(channelId).catch(() => {})
    }
  }

  ;(pane as any).playMode = null
}
const setPaneVideoRef = (el: HTMLVideoElement) => {
  if (!el) return
  const idxStr = el.getAttribute('data-index')
  const idx = idxStr ? parseInt(idxStr, 10) : -1
  if (idx >= 0 && panes.value[idx]) panes.value[idx].videoEl = el
}
const getActiveVideoEl = (): HTMLVideoElement | null =>
  panes.value[activePane.value]?.videoEl || null

// ======================== 截图设置 ========================
const screenshotSettings = reactive({
  uploadToServer: false // 截图是否上传到服务器
})

/**
 * 窗格截图功能 - 支持本地保存和上传到服务器
 */
const handlePaneScreenshot = async (paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (!pane || !pane.isPlaying || !(pane as any).player) {
    ElMessage.warning('当前窗口没有正在播放的视频')
    return
  }

  if (!pane.channel) {
    ElMessage.warning('当前窗口未绑定通道')
    return
  }

  try {
    const player = (pane as any).player
    const channel = pane.channel

    console.log('[截图] 开始截图:', {
      paneIndex,
      channelId: channel.id,
      channelName: channel.channelName || channel.name,
      uploadToServer: screenshotSettings.uploadToServer
    })

    const captureTime = new Date()
    const fileName = `snapshot_${channel.channelName || channel.name}_${captureTime.getTime()}`

    // 1. 使用播放器的capture方法截图（自动下载到本地）
    if (typeof player.capture === 'function') {
      player.capture(fileName)
      console.log('[截图] 本地保存命令已发送')
    }

    // 2. 如果需要上传到服务器，从Canvas获取图片数据
    if (screenshotSettings.uploadToServer) {
      console.log('[截图] 准备上传到服务器...')

      let imageDataUrl: string
      if ((pane as any).canvasEl && (pane as any).canvasEl.width > 0) {
        // 从Canvas截图
        imageDataUrl = (pane as any).canvasEl.toDataURL('image/jpeg', 0.9)
      } else if (pane.videoEl) {
        // 从Video元素截图
        const canvas = document.createElement('canvas')
        canvas.width = pane.videoEl.videoWidth || 1920
        canvas.height = pane.videoEl.videoHeight || 1080
        const ctx = canvas.getContext('2d')
        if (ctx) {
          ctx.drawImage(pane.videoEl, 0, 0, canvas.width, canvas.height)
          imageDataUrl = canvas.toDataURL('image/jpeg', 0.9)
        } else {
          throw new Error('无法创建Canvas上下文')
        }
      } else {
        throw new Error('无法获取视频画面')
      }

      // 转换为Blob并上传
      const blob = await (await fetch(imageDataUrl)).blob()
      const file = new File([blob], `${fileName}.jpg`, { type: 'image/jpeg' })

      await uploadCameraSnapshot(
        channel.id, // channelId
        file,
        1 // snapshotType: 1-手动抓图
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

// ======================== 录像功能 ========================
/**
 * 开始录像（功能开发中）
 */
const handleStartRecording = async (paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (!pane || !pane.isPlaying || !pane.channel) {
    ElMessage.warning('当前窗口没有正在播放的视频')
    return
  }

  // 录像功能暂使用浏览器端录像，后续可扩展为服务端录像
  ElMessage.info('录像功能正在开发中，敬请期待')
}

/**
 * 停止录像
 */
const handleStopRecording = async (paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (!pane) return

  // 录像功能暂未实现
  pane.isRecording = false
  ElMessage.info('录像功能正在开发中')
}

// ======================== 暂停/继续播放 ========================
/**
 * 点击窗口区域
 * 仅切换活动窗口，暂停/继续播放请使用顶部工具栏按钮
 */
const handlePaneClick = (paneIndex: number, event?: MouseEvent) => {
  // 轮巡模式下不允许操作
  if (isPatrolling.value) return

  // 切换活动窗口
  if (activePane.value !== paneIndex) {
    activePane.value = paneIndex
    console.log(`[窗口] 切换到窗口 ${paneIndex + 1}`)
  }
}

/**
 * 切换暂停/继续状态
 */
const handleTogglePause = (paneIndex: number) => {
  const pane = panes.value[paneIndex] as any
  if (!pane || !pane.isPlaying) {
    ElMessage.warning('当前窗口没有正在播放的视频')
    return
  }

  try {
    const player = pane.player
    if (!player) {
      throw new Error('播放器实例不存在')
    }

    if (pane.isPaused) {
      // 继续播放
      if (typeof player.play === 'function') {
        player.play()
        pane.isPaused = false
        console.log('[播放控制] 继续播放 - 窗口', paneIndex + 1)
      } else {
        throw new Error('播放器不支持play方法')
      }
    } else {
      // 暂停播放
      if (typeof player.pause === 'function') {
        player.pause()
        pane.isPaused = true
        console.log('[播放控制] 暂停播放 - 窗口', paneIndex + 1)
      } else {
        throw new Error('播放器不支持pause方法')
      }
    }
  } catch (error: any) {
    console.error('[播放控制] 暂停/继续失败:', error)
    ElMessage.error('操作失败：' + (error?.message || error))
  }
}

// ======================== 停止播放 ========================
/**
 * 停止播放并关闭当前播放器
 */
const handleStopPlay = async (paneIndex: number) => {
  const pane = panes.value[paneIndex] as any
  if (!pane || !pane.isPlaying) {
    ElMessage.warning('当前窗口没有正在播放的视频')
    return
  }

  try {
    console.log(
      '[播放控制] 停止播放 - 窗口',
      paneIndex + 1,
      '通道:',
      pane.channel?.channelName || pane.channel?.name
    )

    // 如果正在录像，先停止录像
    if (pane.isRecording && pane.recordPlayer) {
      try {
        pane.recordPlayer.startRecord(false)
        console.log('[播放控制] 已停止录像')
      } catch (e) {
        console.error('[播放控制] 停止录像失败:', e)
      }
    }

    // 关闭 WebRTC 连接
    if (pane.pc) {
      try {
        pane.pc.close()
        console.log('[播放控制] 已关闭 WebRTC 连接')
      } catch (e) {
        console.warn('[播放控制] 关闭 WebRTC 连接失败:', e)
      }
      pane.pc = null
    }

    // 正确销毁 mpegts.js 播放器：pause -> unload -> detachMediaElement -> destroy
    const player = pane.player
    if (player) {
      try {
        if (typeof player.pause === 'function') {
          player.pause()
          console.log('[播放控制] 已调用 player.pause()')
        }
        if (typeof player.unload === 'function') {
          player.unload()
          console.log('[播放控制] 已调用 player.unload()')
        }
        if (typeof player.detachMediaElement === 'function') {
          player.detachMediaElement()
          console.log('[播放控制] 已调用 player.detachMediaElement()')
        }
        if (typeof player.destroy === 'function') {
          player.destroy()
          console.log('[播放控制] 已调用 player.destroy()')
        }
      } catch (e) {
        console.error('[播放控制] 关闭播放器失败:', e)
      }
    }

    // 清理video元素
    const videoEl = pane.videoEl
    if (videoEl) {
      // 清理 WebRTC 流
      if (videoEl.srcObject) {
        try {
          const stream = videoEl.srcObject as MediaStream
          stream.getTracks().forEach((track) => track.stop())
        } catch (e) {}
        videoEl.srcObject = null
      }
      videoEl.pause()
      videoEl.src = ''
      videoEl.load()
    }

    // 重置窗格状态
    pane.channel = null
    pane.isPlaying = false
    pane.isPaused = false
    pane.isLoading = false
    pane.isRecording = false
    pane.recordingStartTime = undefined
    pane.recordPlayer = null
    pane.player = null
    pane.playMode = null

    ElMessage.success('已停止播放')
    console.log('[播放控制] 窗格已重置')
  } catch (error: any) {
    console.error('[播放控制] 停止播放失败:', error)
    ElMessage.error('停止播放失败：' + (error?.message || error))
  }
}

// ======================== 停止所有播放器 ========================
/**
 * 停止所有正在播放的播放器
 */
const handleStopAllPlayers = async () => {
  try {
    // 如果正在轮巡，先停止轮巡
    if (isPatrolling.value) {
      console.log('[播放控制] 检测到轮巡正在进行，先停止轮巡')
      stopPatrol()
      return
    }

    // 统计正在播放或加载的窗口数量
    const activeCount = panes.value.filter((pane) => pane.isPlaying || pane.isLoading).length

    if (activeCount === 0) {
      ElMessage.warning('当前没有正在播放或加载的视频')
      return
    }

    // 确认操作
    await ElMessageBox.confirm(
      `确定要停止所有正在播放的视频吗？（共 ${activeCount} 个窗口）`,
      '停止所有播放器',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    console.log('[播放控制] 开始停止所有播放器，共', activeCount, '个窗口')

    let successCount = 0
    let failCount = 0

    // 遍历所有窗格，停止正在播放或加载的
    for (let i = 0; i < panes.value.length; i++) {
      const pane = panes.value[i] as any

      // 跳过空闲窗口（既不在播放也不在加载）
      if (!pane.isPlaying && !pane.isLoading) {
        continue
      }

      try {
        console.log('[播放控制] 停止窗口', i + 1)

        // 如果正在录像，先停止录像
        if (pane.isRecording && pane.recordPlayer) {
          try {
            pane.recordPlayer.startRecord(false)
            console.log('[播放控制] 已停止窗口', i + 1, '的录像')
          } catch (e) {
            console.error('[播放控制] 停止窗口', i + 1, '录像失败:', e)
          }
        }

        // 关闭 WebRTC 连接
        if (pane.pc) {
          try {
            pane.pc.close()
          } catch (e) {
            console.warn('[播放控制] 关闭窗口', i + 1, 'WebRTC 连接失败:', e)
          }
          pane.pc = null
        }

        // 正确销毁 mpegts.js 播放器：pause -> unload -> detachMediaElement -> destroy
        const player = pane.player
        if (player) {
          try {
            if (typeof player.pause === 'function') {
              player.pause()
            }
            if (typeof player.unload === 'function') {
              player.unload()
            }
            if (typeof player.detachMediaElement === 'function') {
              player.detachMediaElement()
            }
            if (typeof player.destroy === 'function') {
              player.destroy()
            }
          } catch (e) {
            console.error('[播放控制] 关闭窗口', i + 1, '播放器失败:', e)
          }
        }

        // 清理video元素
        const videoEl = pane.videoEl
        if (videoEl) {
          // 清理 WebRTC 流
          if (videoEl.srcObject) {
            try {
              const stream = videoEl.srcObject as MediaStream
              stream.getTracks().forEach((track) => track.stop())
            } catch (e) {}
            videoEl.srcObject = null
          }
          videoEl.pause()
          videoEl.src = ''
          videoEl.load()
        }

        // 重置窗格状态
        pane.channel = null
        pane.isPlaying = false
        pane.isPaused = false
        pane.isLoading = false
        pane.isRecording = false
        pane.recordingStartTime = undefined
        pane.recordPlayer = null
        pane.player = null
        pane.playMode = null

        successCount++
      } catch (error: any) {
        console.error('[播放控制] 停止窗口', i + 1, '失败:', error)
        failCount++
      }
    }

    // 显示结果
    if (failCount === 0) {
      ElMessage.success(`已成功停止所有播放器（${successCount}个窗口）`)
    } else {
      ElMessage.warning(`停止完成：成功 ${successCount} 个，失败 ${failCount} 个`)
    }

    console.log('[播放控制] 停止所有播放器完成，成功:', successCount, '失败:', failCount)
  } catch (error: any) {
    // 用户取消操作
    if (error === 'cancel') {
      console.log('[播放控制] 用户取消停止所有播放器')
      return
    }
    console.error('[播放控制] 停止所有播放器失败:', error)
    ElMessage.error('停止所有播放器失败：' + (error?.message || error))
  }
}

// ======================== 码流切换 ========================
const handleStreamSwitch = async (paneIndex: number, streamType: 'main' | 'sub') => {
  const pane = panes.value[paneIndex] as any
  if (!pane || !pane.channel) {
    return
  }

  try {
    ElMessage.info(`正在切换到${streamType === 'main' ? '主' : '子'}码流...`)

    // 保存当前状态
    const channel = pane.channel
    pane.streamType = streamType

    // 重新播放（使用新的码流）
    await playChannelInPane({ ...channel, streamType }, paneIndex)

    ElMessage.success(`已切换到${streamType === 'main' ? '主' : '子'}码流`)
  } catch (error: any) {
    console.error('[码流切换] 失败:', error)
    ElMessage.error('码流切换失败：' + (error?.message || error))
  }
}

// ======================== 分辨率切换 ========================
const handleResolutionSwitch = async (paneIndex: number, resolution: string) => {
  const pane = panes.value[paneIndex] as any
  if (!pane || !pane.channel) {
    return
  }

  try {
    ElMessage.info(`正在切换分辨率到 ${resolution}...`)

    // 保存当前状态
    const channel = pane.channel
    const [width, height] = resolution.split('x').map(Number)

    // 重新播放（使用新的分辨率）
    await playChannelInPane({ ...channel, resolution: { width, height } }, paneIndex)

    ElMessage.success(`已切换分辨率到 ${resolution}`)
  } catch (error: any) {
    console.error('[分辨率切换] 失败:', error)
    ElMessage.error('分辨率切换失败：' + (error?.message || error))
  }
}
const isActivePaneMuted = computed<boolean>(() => panes.value[activePane.value]?.muted ?? true)
const toggleMute = () => {
  const pane = panes.value[activePane.value]
  if (!pane) return
  pane.muted = !pane.muted
  const v = pane.videoEl
  if (v) v.muted = pane.muted
}
const handleFullscreen = () => {
  const el = playerGridRef.value || getActiveVideoEl()
  const target: any = el
  if (target && target.requestFullscreen) target.requestFullscreen()
}
const isCameraDevice = (device: any): boolean => {
  try {
    const c = device.config ? JSON.parse(device.config) : {}
    return (
      c.rtspPort !== undefined ||
      c.onvifSupported === true ||
      c.onvifPort !== undefined ||
      c.snapshot !== undefined ||
      c.vendor !== undefined
    )
  } catch {
    return false
  }
}
const loadSpaceTree = async () => {
  try {
    const buildings = await getBuildingList()
    const treeData = buildings.map((b: any) => ({
      id: `building-${b.id}`,
      name: b.name,
      type: 'building',
      buildingId: b.id
    }))
    spaceTreeData.value = treeData
    cameraTreeData.value = treeData
  } catch (e: any) {
    ElMessage.error('加载空间树失败: ' + (e?.message || e || '未知错误'))
  }
}
const loadTreeNode = async (node: any, resolve: Function) => {
  try {
    const data = node.data
    let children: any[] = []
    if (data.type === 'building') {
      children.push({
        id: `channels-building-${data.buildingId}`,
        name: '通道',
        type: 'channels',
        buildingId: data.buildingId
      })
      const floors = await getFloorListByBuildingId(data.buildingId)
      children.push(
        ...floors.map((f: any) => ({
          id: `floor-${f.id}`,
          name: f.name,
          type: 'floor',
          floorId: f.id,
          buildingId: data.buildingId,
          floor: f
        }))
      )
    } else if (data.type === 'floor') {
      children.push({
        id: `channels-floor-${data.floorId}`,
        name: '通道',
        type: 'channels',
        floorId: data.floorId,
        buildingId: data.buildingId
      })
      const areas = await getAreaListByFloorId(data.floorId)
      children.push(
        ...areas.map((a: any) => ({
          id: `area-${a.id}`,
          name: a.name,
          type: 'area',
          areaId: a.id,
          floorId: data.floorId
        }))
      )
    } else if (data.type === 'area') {
      children.push({
        id: `channels-area-${data.areaId}`,
        name: '通道',
        type: 'channels',
        areaId: data.areaId,
        floorId: data.floorId,
        buildingId: data.buildingId
      })
    } else if (data.type === 'channels') {
      const params: any = { pageNo: 1, pageSize: 100 }
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
        channel: ch
      }))
    }
    resolve(children)
  } catch (e: any) {
    ElMessage.error('加载节点失败: ' + (e?.message || e || '未知错误'))
    resolve([])
  }
}
const getNodeIcon = (type: string) =>
  type === 'building'
    ? 'ep:office-building'
    : type === 'floor'
      ? 'ep:tickets'
      : type === 'area'
        ? 'ep:location'
        : type === 'device'
          ? 'ep:video-camera'
          : 'ep:folder'
const loadRecordingData = async () => {
  try {
    loading.value = true
    const params = {
      pageNo: pagination.page,
      pageSize: pagination.size,
      cameraId: filterForm.cameraId,
      recordingType:
        filterForm.recordingTypes.length > 0 ? filterForm.recordingTypes[0] : undefined,
      startTime:
        filterForm.timeRange && filterForm.timeRange[0] ? filterForm.timeRange[0] : undefined,
      endTime: filterForm.timeRange && filterForm.timeRange[1] ? filterForm.timeRange[1] : undefined
    }
    const res = await getCameraRecordingPage(params)
    recordingList.value = res.list || []
    total.value = res.total || 0
  } catch (e: any) {
    ElMessage.error('加载录像数据失败: ' + (e?.message || e || '未知错误'))
    recordingList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}
const handleSearch = () => {
  pagination.page = 1
  loadRecordingData()
}
const handleReset = () => {
  initDefaultRange()
  filterForm.cameraId = undefined
  filterForm.recordingTypes = []
  pagination.page = 1
  loadRecordingData()
}
const handleDelete = async (r: CameraRecordingRespVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该录像记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCameraRecording(r.id)
    ElMessage.success('删除成功')
    await loadRecordingData()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('删除失败: ' + e.message)
  }
}
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadRecordingData()
}
const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadRecordingData()
}
const getRecordingTypeTag = (t: number) =>
  ({ 1: '', 2: 'success', 3: 'warning', 4: 'danger' })[t] || ''
const getRecordingTypeText = (t: number) =>
  ({ 1: '手动录像', 2: '定时录像', 3: '报警触发', 4: '移动侦测' })[t] || '未知'
const getStatusTag = (s: number) =>
  ({ 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' })[s] || ''
const getStatusText = (s: number) =>
  ({ 0: '录像中', 1: '已完成', 2: '已停止', 3: '异常' })[s] || '未知'
const formatFileSize = (b: number): string => {
  if (!b) return '0 B'
  const k = 1024,
    sizes = ['B', 'KB', 'MB', 'GB'],
    i = Math.floor(Math.log(b) / Math.log(k))
  return Math.round(b / Math.pow(k, i)) + ' ' + sizes[i]
}
const switchView = (v: 'smartpss' | 'map' | 'list') => {
  currentViewMode.value = v
}
const handleSpaceNodeClick = async (d: any) => {
  if (d.type === 'floor') {
    currentFloor.value = d.floor
    await loadFloorPlan(d.floorId)
  } else if (d.type === 'device') {
    filterForm.cameraId = d.deviceId
  }
}
const handleCameraSelect = (d: any) => {
  if (d.type === 'device') {
    filterForm.cameraId = d.deviceId
  } else if (d.type === 'channel') {
    filterForm.cameraId = d.channelId
  }
}
const loadFloorPlan = async (floorId: number) => {
  try {
    let allLayers = ''
    try {
      const layersRes = await FloorDxfApi.getLayers(floorId)
      const layerData = layersRes.data || layersRes
      const layers = layerData.layers || []
      allLayers = layers
        .filter((l: any) => l.isVisible)
        .map((l: any) => l.name)
        .join(',')
    } catch {}
    const res = await FloorDxfApi.getFloorPlanSvg(floorId, 'Model', allLayers || undefined)
    const svgData = res.data || res
    floorPlanSvg.value =
      svgData.svgContent && svgData.svgContent.trim() !== '' ? svgData.svgContent : ''
  } catch {
    floorPlanSvg.value = ''
  }
}
const handleDeviceClick = (device: any) => {
  filterForm.cameraId = device.id
  currentViewMode.value = 'list'
}
const handleZoomIn = () => {}
const handleZoomOut = () => {}
const handleResetZoom = () => {}
const handlePause = () => {
  const v = getActiveVideoEl()
  if (v) v.pause()
}
const handleFastForward = () => {
  const v = getActiveVideoEl()
  if (v) {
    v.playbackRate = Math.min((v.playbackRate || 1) + 0.5, 4)
    v.play().catch(() => {})
  }
}
const handleSlowMotion = () => {
  const v = getActiveVideoEl()
  if (v) {
    v.playbackRate = Math.max((v.playbackRate || 1) - 0.5, 0.25)
    v.play().catch(() => {})
  }
}
const handleScreenshot = () => {
  const v = getActiveVideoEl()
  if (!v) return
  const canvas = document.createElement('canvas')
  canvas.width = v.videoWidth
  canvas.height = v.videoHeight
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  ctx.drawImage(v, 0, 0, canvas.width, canvas.height)
  const url = canvas.toDataURL('image/png')
  const a = document.createElement('a')
  a.href = url
  const name =
    panes.value[activePane.value]?.channel?.channelName ||
    panes.value[activePane.value]?.channel?.name ||
    'screenshot'
  a.download = `${name}-${Date.now()}.png`
  a.click()
  ElMessage.success('截图已保存')
}
const handleDownload = () => {
  ElMessage.warning('下载功能暂未实现')
}
const timelineStart = computed<number>(() =>
  filterForm.timeRange && filterForm.timeRange[0]
    ? new Date(filterForm.timeRange[0]).getTime()
    : (() => {
        const d = new Date()
        d.setHours(0, 0, 0, 0)
        return d.getTime()
      })()
)
const timelineEnd = computed<number>(() =>
  filterForm.timeRange && filterForm.timeRange[1]
    ? new Date(filterForm.timeRange[1]).getTime()
    : (() => {
        const d = new Date()
        d.setHours(23, 59, 59, 999)
        return d.getTime()
      })()
)
const scaleMarks = computed<Array<{ left: number; label: string; long: boolean }>>(() => {
  const start = timelineStart.value,
    end = timelineEnd.value,
    total = Math.max(end - start, 1),
    majors = 12,
    minorsBetween = 3,
    res: Array<{ left: number; label: string; long: boolean }> = []
  for (let j = 0; j <= majors; j++) {
    const leftMajor = (j / majors) * 100,
      tMajor = start + (j / majors) * total,
      date = new Date(tMajor),
      hh = String(date.getHours()).padStart(2, '0'),
      mm = String(date.getMinutes()).padStart(2, '0')
    res.push({ left: leftMajor, label: `${hh}:${mm}`, long: true })
    if (j < majors)
      for (let k = 1; k <= minorsBetween; k++)
        res.push({ left: ((j + k / (minorsBetween + 1)) / majors) * 100, label: '', long: false })
  }
  return res
})
const timelineSegments = computed<
  Array<{ left: number; width: number; label: string; start: number; end: number }>
>(() => {
  const start = timelineStart.value,
    end = timelineEnd.value,
    total = Math.max(end - start, 1)
  return recordingList.value.map((rec) => {
    const s = Math.max(new Date(rec.startTime || 0).getTime(), start),
      e = Math.min(new Date(rec.endTime || 0).getTime(), end),
      left = ((s - start) / total) * 100,
      width = Math.max(((e - s) / total) * 100, 0.5),
      label = `${rec.deviceName} | ${rec.startTime} ~ ${rec.endTime}`
    return { left, width, label, start: s, end: e }
  })
})
const timelineLabelStart = computed<string>(() => new Date(timelineStart.value).toLocaleString())
const timelineLabelEnd = computed<string>(() => new Date(timelineEnd.value).toLocaleString())
const playbackRateLabel = computed<string>(() => {
  const v = getActiveVideoEl()
  const r = v ? v.playbackRate || 1 : 1
  return `速度：${r.toFixed(2)}x`
})
const handleTimelineClick = (e: MouseEvent) => {
  ElMessage.info('时间轴点击功能暂未实现')
}
onMounted(async () => {
  // 加载轮巡计划列表
  loadPatrolPlans()

  // 加载视图分组列表
  loadViewGroups()

  // 加载数据
  await loadSpaceTree()
  // 移除首屏自动查询录像，避免"未找到录像记录"提示干扰实时预览
  // 用户在选择摄像头、搜索或切换到列表视图时会自动触发 loadRecordingData()
  setLayout(gridLayout.value)
})
const initDefaultRange = () => {
  const d = new Date(),
    start = new Date(d),
    end = new Date(d)
  start.setHours(0, 0, 0, 0)
  end.setHours(24, 0, 0, 0)
  const fmt = (dt: Date) => {
    const y = dt.getFullYear(),
      m = String(dt.getMonth() + 1).padStart(2, '0'),
      day = String(dt.getDate()).padStart(2, '0'),
      hh = String(dt.getHours()).padStart(2, '0'),
      mm = String(dt.getMinutes()).padStart(2, '0'),
      ss = String(dt.getSeconds()).padStart(2, '0')
    return `${y}-${m}-${day} ${hh}:${mm}:${ss}`
  }
  filterForm.timeRange = [fmt(start), fmt(end)]
}
initDefaultRange()
const togglePlayPause = () => {
  isPlaying.value = !isPlaying.value
  const targetPanes = syncPlayback.value
    ? panes.value
    : [panes.value[activePane.value]].filter(Boolean)
  targetPanes.forEach((p) => {
    const v = p.videoEl
    if (!v) return
    if (isPlaying.value) v.play().catch(() => {})
    else v.pause()
  })
}
const handleStop = () => {
  const targetPanes = syncPlayback.value
    ? panes.value
    : [panes.value[activePane.value]].filter(Boolean)
  targetPanes.forEach((p) => {
    const v = p.videoEl
    if (!v) return
    v.pause()
    try {
      v.currentTime = 0
    } catch {}
  })
  isPlaying.value = false
}
const handleBackward = () => {
  const v = getActiveVideoEl()
  if (!v) return
  try {
    v.currentTime = Math.max((v.currentTime || 0) - 5, 0)
  } catch {}
}
const handleForward = () => {
  const v = getActiveVideoEl()
  if (!v) return
  try {
    v.currentTime = (v.currentTime || 0) + 5
  } catch {}
}
const handleFrameForward = () => {
  const v = getActiveVideoEl()
  if (!v) return
  const step = 1 / 30
  try {
    v.currentTime = (v.currentTime || 0) + step
  } catch {}
}
const handleSpeedChange = () => {
  const apply = (pane: any) => {
    const v: HTMLVideoElement | null = pane.videoEl
    if (v) v.playbackRate = playbackSpeed.value
  }
  if (syncPlayback.value) panes.value.forEach(apply)
  else {
    const p = panes.value[activePane.value]
    if (p) apply(p)
  }
}
const toggleMuteAll = () => {
  isMutedAll.value = !isMutedAll.value
  panes.value.forEach((p) => {
    p.muted = isMutedAll.value
    const v = p.videoEl
    if (v) v.muted = isMutedAll.value
  })
}
const showExportDialog = () => {
  exportDialogVisible.value = true
  exportTab.value = 'exporting'
}
const showTimeCutDialog = () => {
  timeCutDialogVisible.value = true
  const now = new Date(currentPlayTime.value || timelineStart.value),
    end = new Date(now.getTime() + 5 * 60 * 1000)
  timeCutForm.startTime = formatDateTime(now)
  timeCutForm.endTime = formatDateTime(end)
}
const toggleSyncPlayback = () => {
  syncPlayback.value = !syncPlayback.value
  if (syncPlayback.value) syncToTime(currentPlayTime.value || timelineStart.value)
}
function formatDateTime(d: Date) {
  const y = d.getFullYear(),
    m = String(d.getMonth() + 1).padStart(2, '0'),
    day = String(d.getDate()).padStart(2, '0'),
    hh = String(d.getHours()).padStart(2, '0'),
    mm = String(d.getMinutes()).padStart(2, '0'),
    ss = String(d.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${day} ${hh}:${mm}:${ss}`
}
const syncToTime = (ms: number) => {
  panes.value.forEach((p) => {
    const v = p.videoEl
    if (!v) return
    const dur = v.duration
    if (!isNaN(dur) && dur > 0) {
      const start = timelineStart.value,
        end = timelineEnd.value,
        total = Math.max(end - start, 1),
        ratio = (Math.min(Math.max(ms, start), end) - start) / total
      try {
        v.currentTime = ratio * dur
      } catch {}
    }
  })
}
const draggingTimeline = ref(false)
let removeDragListeners: (() => void) | null = null
const currentTimePercent = computed<number>(() => {
  const s = timelineStart.value,
    e = timelineEnd.value,
    total = Math.max(e - s, 1),
    t = Math.min(Math.max(currentPlayTime.value || s, s), e)
  return ((t - s) / total) * 100
})
const currentTimeLabel = computed<string>(() =>
  new Date(currentPlayTime.value || timelineStart.value).toLocaleString()
)
const startTimelineDrag = (e: MouseEvent) => {
  draggingTimeline.value = true
  const bar = e.currentTarget as HTMLElement,
    rect = bar.getBoundingClientRect()
  const update = (cx: number) => {
    const percent = Math.min(Math.max((cx - rect.left) / rect.width, 0), 1)
    currentPlayTime.value =
      timelineStart.value + percent * (timelineEnd.value - timelineStart.value)
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
// 清理所有播放器资源
const cleanupAllPlayers = () => {
  console.log('[实时预览] 开始清理所有播放器资源...')

  panes.value.forEach((pane, index) => {
    try {
      // 1. 关闭 WebRTC 连接
      if ((pane as any).pc) {
        try {
          console.log(`[实时预览] 关闭窗格 ${index + 1} 的 WebRTC 连接`)
          ;(pane as any).pc.close()
        } catch (e) {
          console.warn(`[实时预览] 关闭 WebRTC 连接异常:`, e)
        }
        ;(pane as any).pc = null
      }

      // 2. 停止并销毁 mpegts.js 播放器实例
      // 重要：mpegts.js 的正确销毁顺序是 pause -> unload -> detachMediaElement -> destroy
      if ((pane as any).player) {
        const player = (pane as any).player
        console.log(`[实时预览] 清理窗格 ${index + 1} 的 mpegts 播放器`)

        try {
          // mpegts.js 正确的销毁顺序
          if (typeof player.pause === 'function') {
            player.pause()
          }
          if (typeof player.unload === 'function') {
            player.unload()
          }
          if (typeof player.detachMediaElement === 'function') {
            player.detachMediaElement()
          }
          if (typeof player.destroy === 'function') {
            player.destroy()
          }
        } catch (e) {
          console.warn(`[实时预览] 销毁 mpegts 播放器异常:`, e)
        }

        ;(pane as any).player = null
      }

      // 3. 停止视频流并释放媒体资源
      if (pane.videoEl) {
        // 停止所有媒体轨道（WebRTC 场景）
        if (pane.videoEl.srcObject) {
          try {
            const stream = pane.videoEl.srcObject as MediaStream
            stream.getTracks().forEach((track) => {
              track.stop()
              console.log(`[实时预览] 停止媒体轨道: ${track.kind}`)
            })
          } catch (e) {
            console.warn(`[实时预览] 停止媒体轨道异常:`, e)
          }
          pane.videoEl.srcObject = null
        }

        // 暂停视频播放
        try {
          pane.videoEl.pause()
        } catch (e) {}

        // 清空视频源
        pane.videoEl.src = ''
        try {
          pane.videoEl.load()
        } catch (e) {}
      }

      // 4. 清空通道信息和播放状态
      pane.channel = null
      pane.isPlaying = false
      pane.isPaused = false
      pane.isLoading = false
      ;(pane as any).playMode = null
    } catch (error) {
      console.error(`[实时预览] 清理窗格 ${index + 1} 失败:`, error)
    }
  })

  console.log('[实时预览] 播放器资源清理完成')
}

// 监听活动窗口切换和云台面板展开，自动加载对应通道的预设点
watch([activePane, leftPanelActive], async ([paneIndex, panelNames]) => {
  const pane = panes.value[paneIndex]

  // 当活动窗口有通道且支持云台时，自动加载预设点
  if (pane && pane.channel && activePaneSupportsPtz.value) {
    // 如果是云台面板展开，或者切换了窗口
    const isPtzPanelOpen = Array.isArray(panelNames)
      ? panelNames.includes('ptz')
      : panelNames === 'ptz'
    if (isPtzPanelOpen || presetList.value.length === 0) {
      console.log(`[预设点] 自动加载通道 ${pane.channel.id} 的预设点列表`)
      await loadPresetList(pane.channel.id)
    }
  } else {
    // 如果窗口不支持云台，清空预设点列表
    presetList.value = []
    cruiseList.value = []
  }
})

// 组件卸载时清理资源
onUnmounted(() => {
  console.log('[实时预览] 组件卸载，清理资源...')

  // 停止轮巡
  if (isPatrolling.value) {
    stopPatrol()
  }

  // 清理巡航定时器
  if (cruiseTimer) {
    clearTimeout(cruiseTimer)
    cruiseTimer = null
  }

  // 清理所有播放器
  cleanupAllPlayers()

  // 清理拖拽监听器
  if (removeDragListeners) {
    removeDragListeners()
  }

  console.log('[实时预览] 资源清理完成')
})
const cutDuration = computed<string>(() => {
  if (!timeCutForm.startTime || !timeCutForm.endTime) return '--'
  const s = new Date(timeCutForm.startTime).getTime(),
    e = new Date(timeCutForm.endTime).getTime(),
    diff = Math.max(e - s, 0),
    mm = Math.floor(diff / 60000),
    ss = Math.floor((diff % 60000) / 1000)
  return `${mm}分${ss}秒`
})
const cutRangeLeft = computed<number>(() => {
  if (!timeCutForm.startTime) return 0
  const s = new Date(timeCutForm.startTime).getTime(),
    start = timelineStart.value,
    end = timelineEnd.value,
    total = Math.max(end - start, 1)
  return ((Math.min(Math.max(s, start), end) - start) / total) * 100
})
const cutRangeWidth = computed<number>(() => {
  if (!timeCutForm.startTime || !timeCutForm.endTime) return 0
  const s = new Date(timeCutForm.startTime).getTime(),
    e = new Date(timeCutForm.endTime).getTime(),
    start = timelineStart.value,
    end = timelineEnd.value,
    total = Math.max(end - start, 1),
    left = Math.min(Math.max(s, start), end),
    right = Math.min(Math.max(e, start), end)
  return Math.max(((right - left) / total) * 100, 0)
})
const handleConfirmTimeCut = () => {
  if (!timeCutForm.startTime || !timeCutForm.endTime) return ElMessage.warning('请完善剪切时间段')
  exportingList.value.push({
    name: panes.value[activePane.value]?.channel?.channelName || '剪切片段',
    startTime: timeCutForm.startTime,
    endTime: timeCutForm.endTime,
    size: 0,
    progress: 0,
    status: 'active'
  })
  exportDialogVisible.value = true
  timeCutDialogVisible.value = false
  exportTab.value = 'exporting'
}
const handlePauseExport = () => {}
const handleOpenExport = () => {}
const handleDeleteExport = () => {
  exportingList.value = []
}
const handleCancelExport = (row: any) => {
  exportingList.value = exportingList.value.filter((r) => r !== row)
}
const handleOpenExportedFile = (row: any) => {}
const handleDeleteExportedFile = (row: any) => {
  exportedList.value = exportedList.value.filter((r) => r !== row)
}
// 视图分组列表（从后端加载）
const viewGroups = ref<any[]>([])

// 加载轮巡计划列表
const loadPatrolPlans = async () => {
  try {
    const res: any = await getPatrolPlanPage({
      pageNo: 1,
      pageSize: 100,
      status: 1 // 只加载启用的计划
    })

    if (res && res.list) {
      patrolPlans.value = res.list.map((plan: any) => ({
        id: plan.id,
        name: plan.planName
      }))

      // 默认选中第一个
      if (patrolPlans.value.length > 0 && !selectedPatrolPlan.value) {
        selectedPatrolPlan.value = patrolPlans.value[0].id
      }
    }
  } catch (e) {
    console.error('[加载轮巡计划] 失败:', e)
  }
}

// 加载视图分组列表（不加载视图）
const loadViewGroups = async () => {
  try {
    console.log('[加载分组] 开始从后端加载视图分组...')

    // 从后端加载视图分组列表
    const groups = await getVideoViewGroupList()

    // 转换为树形结构格式
    viewGroups.value = groups.map((group: any) => ({
      id: group.id,
      name: group.name,
      icon: group.icon || 'ep:folder',
      type: 'group',
      children: [] // 初始为空，点击时再加载
    }))

    console.log('[加载分组] 已加载', viewGroups.value.length, '个分组')
  } catch (error) {
    console.error('[加载分组] 失败:', error)
    ElMessage.error('加载视图分组失败')
  }
}

// 加载指定分组下的视图列表
const loadGroupViews = async (groupId: number) => {
  try {
    console.log('[加载视图] 加载分组', groupId, '的视图列表...')

    // 从后端获取该分组的视图树
    const treeData = await getVideoViewTree()
    const groupData = treeData.find((g: any) => g.id === groupId)

    if (!groupData) {
      return []
    }

    // 转换为前端格式
    const views = (groupData.children || []).map((view: any) => ({
      id: view.id,
      name: view.name,
      icon: 'ep:video-camera',
      type: 'view',
      gridLayout: view.gridLayout,
      paneCount: view.paneCount
    }))

    console.log('[加载视图] 分组', groupId, '共有', views.length, '个视图')
    return views
  } catch (error) {
    console.error('[加载视图] 失败:', error)
    return []
  }
}

// 视图树节点点击处理
const handleViewTreeNodeClick = async (data: any, node: any) => {
  console.log('[视图树] 节点点击:', data)

  if (data.type === 'view') {
    // 点击视图：直接加载该视图
    handleLoadView(data)
  } else if (data.type === 'group') {
    // 点击分组：加载该分组下的视图列表
    if (!data.children || data.children.length === 0) {
      // 如果还没有加载视图，先加载
      const views = await loadGroupViews(data.id)

      // 使用 Vue.set 或直接修改 viewGroups 来触发响应式更新
      const groupIndex = viewGroups.value.findIndex((g) => g.id === data.id)
      if (groupIndex !== -1) {
        viewGroups.value[groupIndex].children = views
        console.log('[视图树] 已更新分组', data.name, '的子节点，共', views.length, '个视图')
      }
    }

    // 不自动加载第一个视图，让用户点击视图节点来加载
    // 这样用户可以看到视图列表
  }
}

// 加载视图
const handleLoadView = async (viewData: any) => {
  try {
    console.log('[加载视图] 开始加载:', viewData.name, 'ID:', viewData.id)

    // 1. 先清理所有播放器资源
    console.log('[加载视图] 清空旧视图数据...')
    cleanupAllPlayers()

    // 2. 从后端获取完整的视图详情
    const fullView = await getVideoView(viewData.id)
    console.log('[加载视图] 获取到完整视图数据:', fullView)

    // 3. 设置当前视图
    currentView.value = fullView

    // 4. 设置分屏格式（setLayout 会自动初始化 panes）
    setLayout(fullView.gridLayout)

    await nextTick()

    // 恢复每个窗格的通道
    for (const paneData of fullView.panes) {
      if (paneData.channelId && paneData.paneIndex < panes.value.length) {
        // 从 paneData 中恢复完整的通道信息
        const channel = {
          id: paneData.channelId,
          deviceId: paneData.deviceId,
          channelNo: paneData.channelNo,
          channelName: paneData.channelName,
          // 恢复设备连接信息
          targetIp: paneData.targetIp,
          target_ip: paneData.targetIp,
          targetChannelNo: paneData.targetChannelNo,
          target_channel_no: paneData.targetChannelNo,
          // 恢复流地址
          streamUrlMain: paneData.streamUrlMain,
          stream_url_main: paneData.streamUrlMain,
          streamUrlSub: paneData.streamUrlSub,
          stream_url_sub: paneData.streamUrlSub,
          // 恢复配置信息（包含用户名、密码、端口等）
          config: paneData.config
        }

        console.log('[加载视图] 恢复窗格', paneData.paneIndex + 1, '的通道:', channel)

        // 恢复通道信息
        panes.value[paneData.paneIndex].channel = channel

        // 播放通道（注意参数顺序：channelData, paneIndex）
        await playChannelInPane(channel, paneData.paneIndex)
      }
    }

    ElMessage.success(`视图"${fullView.name}"已加载`)
    console.log('[加载视图] 加载完成')
  } catch (error) {
    console.error('[加载视图] 失败:', error)
    ElMessage.error('加载视图失败')
  }
}

// 右键菜单处理
const handleViewContextMenu = (event: MouseEvent, data: any) => {
  event.preventDefault()
  ElMessageBox.confirm(`确定要删除视图"${data.name}"吗？`, '删除视图', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      handleDeleteView(data.id)
    })
    .catch(() => {})
}

// 删除视图
const handleDeleteView = async (viewId: number) => {
  try {
    console.log('[删除视图] 开始删除视图 ID:', viewId)

    // 调用后端API删除视图
    await deleteVideoView(viewId)

    // 重新加载分组列表
    await loadViewGroups()

    ElMessage.success('视图已删除')
    console.log('[删除视图] 删除成功')
  } catch (error) {
    console.error('[删除视图] 失败:', error)
    ElMessage.error('删除视图失败')
  }
}

// 新建分组
const handleCreateGroup = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入分组名称', '新建分组', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '分组名称不能为空'
    })

    // 调用后端API创建分组
    await createVideoViewGroup({
      name: value,
      icon: 'ep:folder',
      sort: viewGroups.value.length
    })

    // 重新加载分组列表
    await loadViewGroups()

    ElMessage.success(`分组"${value}"已创建`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('[创建分组] 失败:', error)
      ElMessage.error('创建分组失败')
    }
  }
}

// 分组右键菜单
const handleGroupContextMenu = async (event: MouseEvent, data: any) => {
  event.preventDefault()

  // 默认分组不允许删除
  if (data.id === 1) {
    ElMessage.warning('默认分组不能删除')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除分组"${data.name}"吗？\n删除分组后，该分组下的视图将保留在其他分组中。`,
      '删除分组',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 调用后端API删除分组
    await deleteVideoViewGroup(data.id)

    // 重新加载分组列表
    await loadViewGroups()

    ElMessage.success('分组已删除')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('[删除分组] 失败:', error)
      ElMessage.error('删除分组失败')
    }
  }
}
const ptzStep = ref(5)
const presetPointName = ref('') // 预设点名称
const presetList = ref<CameraPresetRespVO[]>([]) // 预设点列表
const selectedPresetNo = ref<number | null>(null) // 当前选中的预设点编号

// 巡航相关数据
const cruiseList = ref<CameraCruiseRespVO[]>([])
const showCruiseDialog = ref(false)
const cruiseForm = reactive<CameraCruiseSaveReqVO>({
  id: undefined,
  channelId: 0,
  cruiseName: '',
  description: '',
  dwellTime: 5,
  loopEnabled: true,
  points: []
})

// 巡航执行相关
let cruiseTimer: any = null
let currentCruiseIndex = 0
const currentCruise = ref<CameraCruiseRespVO | null>(null)

// 可选预设点（排除已选择的）
const availablePresets = computed(() => {
  const selectedIds = cruiseForm.points.map((p) => p.presetId)
  return presetList.value.filter((p) => !selectedIds.includes(p.id))
})

/**
 * 确保设备已连接（云台控制通过后端 API 实现）
 */
const ensureRpcLogin = async (_channel: any): Promise<boolean> => {
  // 云台控制现在通过后端 API 实现，不需要前端 RPC 登录
  return true
}

/**
 * 加载预设点列表
 */
const loadPresetList = async (channelId: number) => {
  try {
    // 先清空列表
    presetList.value = []
    cruiseList.value = []

    // 加载预设点列表
    const data = await getCameraPresetListByChannel(channelId)
    presetList.value = data
    console.log(`[预设点] 加载了 ${data.length} 个预设点`)

    // 同时加载巡航列表
    await loadCruiseList(channelId)
  } catch (error) {
    console.error('[预设点] 加载失败:', error)
    presetList.value = []
  }
}

/**
 * 加载巡航列表
 */
const loadCruiseList = async (channelId: number) => {
  try {
    const list = await getCameraCruiseListByChannel(channelId)
    cruiseList.value = list
    console.log('[巡航] 加载巡航列表成功:', list.length)
  } catch (error) {
    console.error('[巡航] 加载巡航列表失败:', error)
    cruiseList.value = []
  }
}

/**
 * 添加预设点到巡航
 */
const handleAddCruisePoint = (preset: CameraPresetRespVO) => {
  cruiseForm.points.push({
    presetId: preset.id,
    sortOrder: cruiseForm.points.length + 1,
    dwellTime: undefined
  })
}

/**
 * 移除巡航点
 */
const handleRemoveCruisePoint = (index: number) => {
  cruiseForm.points.splice(index, 1)
  cruiseForm.points.forEach((point, idx) => {
    point.sortOrder = idx + 1
  })
}

/**
 * 上移预设点
 */
const movePointUp = (index: number) => {
  if (index === 0) return
  const temp = cruiseForm.points[index]
  cruiseForm.points[index] = cruiseForm.points[index - 1]
  cruiseForm.points[index - 1] = temp
  cruiseForm.points.forEach((point, idx) => {
    point.sortOrder = idx + 1
  })
}

/**
 * 下移预设点
 */
const movePointDown = (index: number) => {
  if (index === cruiseForm.points.length - 1) return
  const temp = cruiseForm.points[index]
  cruiseForm.points[index] = cruiseForm.points[index + 1]
  cruiseForm.points[index + 1] = temp
  cruiseForm.points.forEach((point, idx) => {
    point.sortOrder = idx + 1
  })
}

/**
 * 获取预设点名称
 */
const getPresetName = (presetId: number) => {
  const preset = presetList.value.find((p) => p.id === presetId)
  return preset ? `${preset.presetNo}. ${preset.presetName}` : '未知预设点'
}

/**
 * 关闭对话框
 */
const handleCruiseDialogClose = () => {
  cruiseForm.id = undefined
  cruiseForm.cruiseName = ''
  cruiseForm.description = ''
  cruiseForm.dwellTime = 5
  cruiseForm.loopEnabled = true
  cruiseForm.points = []
}

/**
 * 保存巡航
 */
const handleSaveCruise = async () => {
  const pane = panes.value[activePane.value]
  if (!pane || !pane.channel) {
    ElMessage.warning('请先选择一个通道')
    return
  }

  if (!cruiseForm.cruiseName) {
    ElMessage.warning('请输入巡航路线名称')
    return
  }

  if (cruiseForm.points.length < 2) {
    ElMessage.warning('至少需要2个预设点')
    return
  }

  try {
    cruiseForm.channelId = pane.channel.id

    if (cruiseForm.id) {
      await updateCameraCruise(cruiseForm)
      ElMessage.success('更新巡航路线成功')
    } else {
      await createCameraCruise(cruiseForm)
      ElMessage.success('创建巡航路线成功')
    }

    showCruiseDialog.value = false
    await loadCruiseList(pane.channel.id)
  } catch (error: any) {
    console.error('[巡航] 保存失败:', error)
    ElMessage.error('保存巡航路线失败：' + (error?.message || error))
  }
}

/**
 * 编辑巡航
 */
const handleEditCruise = async (cruise: CameraCruiseRespVO) => {
  try {
    console.log('[巡航] 开始加载详情, cruise.id:', cruise.id)
    const detail = await getCameraCruise(cruise.id)
    console.log('[巡航] 接口返回详情:', detail)
    if (!detail) {
      ElMessage.error('巡航路线不存在')
      return
    }
    console.log('[巡航] detail.points:', detail.points)
    console.log('[巡航] detail.points 长度:', detail.points?.length)
    cruiseForm.id = detail.id
    cruiseForm.channelId = detail.channelId
    cruiseForm.cruiseName = detail.cruiseName
    cruiseForm.description = detail.description || ''
    cruiseForm.dwellTime = detail.dwellTime
    cruiseForm.loopEnabled = detail.loopEnabled
    cruiseForm.points = detail.points.map((p) => ({
      presetId: p.presetId,
      sortOrder: p.sortOrder,
      dwellTime: p.dwellTime
    }))
    console.log('[巡航] cruiseForm.points 赋值后:', cruiseForm.points)
    showCruiseDialog.value = true
  } catch (error: any) {
    console.error('[巡航] 加载详情失败:', error)
    ElMessage.error('加载巡航路线失败：' + (error?.message || error))
  }
}

/**
 * 删除巡航
 */
const handleDeleteCruise = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该巡航路线吗？', '删除巡航路线', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteCameraCruise(id)
    ElMessage.success('删除巡航路线成功')

    const pane = panes.value[activePane.value]
    if (pane && pane.channel) {
      await loadCruiseList(pane.channel.id)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('[巡航] 删除失败:', error)
      ElMessage.error('删除巡航路线失败：' + (error?.message || error))
    }
  }
}

/**
 * 启动巡航
 */
const handleStartCruise = async (cruise: CameraCruiseRespVO) => {
  const pane = panes.value[activePane.value]
  if (!pane || !pane.channel || !pane.isPlaying) {
    ElMessage.warning('请先选择一个正在播放的窗口')
    return
  }

  try {
    await startCameraCruise(cruise.id)
    await loadCruiseList(pane.channel.id)

    currentCruise.value = cruise
    currentCruiseIndex = 0
    executeCruiseStep()

    ElMessage.success(`巡航 "${cruise.cruiseName}" 已启动`)
  } catch (error: any) {
    console.error('[巡航] 启动失败:', error)
    ElMessage.error('启动巡航失败：' + (error?.message || error))
  }
}

/**
 * 停止巡航
 */
const handleStopCruise = async (cruise: CameraCruiseRespVO) => {
  try {
    if (cruiseTimer) {
      clearTimeout(cruiseTimer)
      cruiseTimer = null
    }

    await stopCameraCruise(cruise.id)

    const pane = panes.value[activePane.value]
    if (pane && pane.channel) {
      await loadCruiseList(pane.channel.id)
    }

    currentCruise.value = null
    currentCruiseIndex = 0

    ElMessage.success(`巡航 "${cruise.cruiseName}" 已停止`)
  } catch (error: any) {
    console.error('[巡航] 停止失败:', error)
    ElMessage.error('停止巡航失败：' + (error?.message || error))
  }
}

/**
 * 执行巡航步骤
 */
const executeCruiseStep = async () => {
  if (
    !currentCruise.value ||
    !currentCruise.value.points ||
    currentCruise.value.points.length === 0
  ) {
    return
  }

  const cruise = currentCruise.value
  const point = cruise.points[currentCruiseIndex]

  const preset = presetList.value.find((p) => p.id === point.presetId)
  if (!preset) {
    console.error('[巡航] 找不到预设点:', point.presetId)
    moveToNextCruisePoint()
    return
  }

  try {
    console.log(`[巡航] 转到预设点 ${preset.presetNo} "${preset.presetName}"`)
    await handleSelectPreset(preset)

    const dwellTime = point.dwellTime || cruise.dwellTime

    cruiseTimer = setTimeout(() => {
      moveToNextCruisePoint()
    }, dwellTime * 1000)
  } catch (error) {
    console.error('[巡航] 执行步骤失败:', error)
    moveToNextCruisePoint()
  }
}

/**
 * 移动到下一个巡航点
 */
const moveToNextCruisePoint = () => {
  if (!currentCruise.value) return

  currentCruiseIndex++

  if (currentCruiseIndex >= currentCruise.value.points.length) {
    if (currentCruise.value.loopEnabled) {
      currentCruiseIndex = 0
      executeCruiseStep()
    } else {
      console.log('[巡航] 巡航完成')
      currentCruise.value = null
      currentCruiseIndex = 0
    }
  } else {
    executeCruiseStep()
  }
}

/**
 * 云台控制 - 通过后端 API 调用 NVR SDK
 * 支持的命令：UP, DOWN, LEFT, RIGHT, LEFT_UP, RIGHT_UP, LEFT_DOWN, RIGHT_DOWN,
 *           ZOOM_IN (ZoomTele), ZOOM_OUT (ZoomWide), FOCUS_NEAR (FocusNear),
 *           FOCUS_FAR (FocusFar), IRIS_OPEN (IrisLarge), IRIS_CLOSE (IrisSmall)
 */
const executePtzCommand = async (code: string, isStop: boolean = false) => {
  const pane = panes.value[activePane.value]
  if (!pane || !pane.channel || !pane.isPlaying) {
    ElMessage.warning('请先选择一个正在播放的窗口')
    return
  }

  // 兼容 true/1/'1' 等多种格式
  const ptzSupport =
    pane.channel.ptzSupport === true ||
    pane.channel.ptzSupport === 1 ||
    pane.channel.ptzSupport === '1'
  if (!ptzSupport) {
    ElMessage.warning('当前通道不支持云台控制')
    return
  }

  try {
    const channel = pane.channel

    // 获取 NVR ID（deviceId）和通道号
    const nvrId = channel.deviceId || channel.nvrId
    if (!nvrId) {
      throw new Error('无法获取 NVR 设备 ID')
    }

    // 使用 NVR 的原始通道号，后端负责查询 target_ip 和 target_channel_no
    const channelNo = Number(channel.channelNo || 0)

    console.log('[云台控制] 通道信息:', {
      channelName: channel.channelName || channel.name,
      nvrId: nvrId,
      channelNo: channelNo
    })

    // 转换前端命令为后端命令格式
    const backendCommand = convertPtzCommand(code)

    console.log(
      `[云台控制] ${isStop ? '停止' : '开始'} - ${code} -> ${backendCommand}, 通道: ${channelNo}`
    )

    // 调用后端 PTZ 控制 API
    await nvrPtzControl(nvrId, {
      channelNo: channelNo,
      command: backendCommand,
      speed: ptzStep.value,
      stop: isStop
    })
  } catch (error: any) {
    console.error('[云台控制] 失败:', error)
    ElMessage.error('云台控制失败：' + (error?.message || error))
  }
}

/**
 * 转换前端 PTZ 命令为后端命令格式
 */
const convertPtzCommand = (code: string): string => {
  const commandMap: Record<string, string> = {
    Up: 'UP',
    Down: 'DOWN',
    Left: 'LEFT',
    Right: 'RIGHT',
    LeftUp: 'LEFT_UP',
    RightUp: 'RIGHT_UP',
    LeftDown: 'LEFT_DOWN',
    RightDown: 'RIGHT_DOWN',
    ZoomTele: 'ZOOM_IN',
    ZoomWide: 'ZOOM_OUT',
    FocusNear: 'FOCUS_NEAR',
    FocusFar: 'FOCUS_FAR',
    IrisLarge: 'IRIS_OPEN',
    IrisSmall: 'IRIS_CLOSE',
    AutoFocus: 'AUTO_FOCUS'
  }
  return commandMap[code] || code.toUpperCase()
}

// 方向控制 - 长按操作
let currentPtzCommand: string | null = null

const handlePtzMoveStart = (dir: 'up' | 'down' | 'left' | 'right') => {
  const codeMap = {
    up: 'Up',
    down: 'Down',
    left: 'Left',
    right: 'Right'
  }
  const code = codeMap[dir]
  currentPtzCommand = code
  console.log('[云台方向] 开始移动:', code)
  executePtzCommand(code, false)
}

const handlePtzMoveStop = () => {
  if (currentPtzCommand) {
    console.log('[云台方向] 停止移动:', currentPtzCommand)
    executePtzCommand(currentPtzCommand, true)
    currentPtzCommand = null
  }
}

// 中心点 - 自动聚焦
const handlePtzStart = () => {
  executePtzCommand('AutoFocus')
}

const handlePtzStop = () => {
  // 停止所有云台操作
  executePtzCommand('Up', true)
}

// 变倍 - 放大（长按）
const handlePtzZoomInStart = () => {
  currentPtzCommand = 'ZoomTele'
  executePtzCommand('ZoomTele', false)
}

// 变倍 - 缩小（长按）
const handlePtzZoomOutStart = () => {
  currentPtzCommand = 'ZoomWide'
  executePtzCommand('ZoomWide', false)
}

// 聚焦 - 近（长按）
const handlePtzFocusInStart = () => {
  currentPtzCommand = 'FocusNear'
  executePtzCommand('FocusNear', false)
}

// 聚焦 - 远（长按）
const handlePtzFocusOutStart = () => {
  currentPtzCommand = 'FocusFar'
  executePtzCommand('FocusFar', false)
}

// 光圈 - 打开（长按）
const handlePtzIrisOpenStart = () => {
  currentPtzCommand = 'IrisLarge'
  executePtzCommand('IrisLarge', false)
}

// 光圈 - 关闭（长按）
const handlePtzIrisCloseStart = () => {
  currentPtzCommand = 'IrisSmall'
  executePtzCommand('IrisSmall', false)
}

// 通用停止函数（用于所有云台操作）
const handlePtzOperationStop = () => {
  if (currentPtzCommand) {
    executePtzCommand(currentPtzCommand, true)
    currentPtzCommand = null
  }
}

/**
 * 添加预设点 - 自动分配编号
 */
const handleAddPreset = async () => {
  const pane = panes.value[activePane.value]
  if (!pane || !pane.channel || !pane.isPlaying) {
    ElMessage.warning('请先选择一个正在播放的窗口')
    return
  }

  if (presetList.value.length >= 255) {
    ElMessage.warning('预设点数量已达上限（255个）')
    return
  }

  try {
    const channel = pane.channel
    await ensureRpcLogin(channel)

    const RPC = (window as any).RPC
    const targetChannelNo = channel.targetChannelNo || channel.target_channel_no
    const channelNo =
      targetChannelNo !== undefined ? Number(targetChannelNo) : Number(channel.channelNo || 0)

    // 自动分配编号：找到第一个未使用的编号
    let newNo = 1
    const usedNos = presetList.value.map((p) => p.presetNo).sort((a, b) => a - b)
    for (let i = 1; i <= 255; i++) {
      if (!usedNos.includes(i)) {
        newNo = i
        break
      }
    }

    const presetName = presetPointName.value.trim() || `预设点${newNo}`

    console.log(`[预设点] 添加预设点 ${newNo}，通道: ${channelNo}，名称: ${presetName}`)

    // 设置预设点（设备会自动保存当前的 Pan、Tilt、Zoom 位置）
    const setResult = await RPC.PTZManager('start', channelNo, {
      code: 'SetPreset',
      arg1: newNo,
      arg2: 0,
      arg3: 0
    })

    if (!setResult || setResult.result === false) {
      throw new Error(setResult?.error?.message || '设置预设点失败')
    }

    // 保存到数据库（PTZ 位置由设备自动保存，数据库仅记录预设点编号和名称）
    await createCameraPreset({
      channelId: pane.channel.id,
      presetNo: newNo,
      presetName: presetName,
      description: `通过实时预览添加于${new Date().toLocaleString()}`
    })

    // 重新加载预设点列表
    await loadPresetList(pane.channel.id)

    ElMessage.success(`预设点 "${presetName}" 已添加`)

    // 清空名称输入框
    presetPointName.value = ''
  } catch (error: any) {
    console.error('[预设点] 添加失败:', error)
    const errorMsg = error?.message || error?.error?.message || JSON.stringify(error)
    ElMessage.error('添加预设点失败：' + errorMsg)
  }
}

/**
 * 删除预设点
 */
const handleDeletePreset = async (presetId: number) => {
  const pane = panes.value[activePane.value]
  if (!pane || !pane.channel || !pane.isPlaying) {
    ElMessage.warning('请先选择一个正在播放的窗口')
    return
  }

  try {
    const preset = presetList.value.find((p) => p.id === presetId)
    if (!preset) return

    await ElMessageBox.confirm(`确定要删除预设点 "${preset.presetName}" 吗？`, '删除预设点', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const channel = pane.channel
    await ensureRpcLogin(channel)

    const RPC = (window as any).RPC
    const targetChannelNo = channel.targetChannelNo || channel.target_channel_no
    const channelNo =
      targetChannelNo !== undefined ? Number(targetChannelNo) : Number(channel.channelNo || 0)

    console.log(`[预设点] 删除预设点 ${preset.presetNo}，通道: ${channelNo}`)

    // 清除预设点
    await RPC.PTZManager('start', channelNo, {
      code: 'ClearPreset',
      arg1: preset.presetNo,
      arg2: 0,
      arg3: 0
    })

    // 从数据库删除
    await deleteCameraPreset(presetId)

    // 重新加载预设点列表
    await loadPresetList(pane.channel.id)

    // 如果删除的是当前选中的，清除选中状态
    if (selectedPresetNo.value === preset.presetNo) {
      selectedPresetNo.value = null
    }

    ElMessage.success(`预设点 "${preset.presetName}" 已删除`)
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('[预设点] 删除失败:', error)
      ElMessage.error('删除预设点失败：' + (error?.message || error))
    }
  }
}

/**
 * 选择预设点（从列表点击） - 自动转到该位置
 */
const handleSelectPreset = async (preset: CameraPresetRespVO) => {
  const pane = panes.value[activePane.value]
  if (!pane || !pane.channel || !pane.isPlaying) {
    ElMessage.warning('请先选择一个正在播放的窗口')
    return
  }

  try {
    selectedPresetNo.value = preset.presetNo

    const channel = pane.channel
    await ensureRpcLogin(channel)

    const RPC = (window as any).RPC
    const targetChannelNo = channel.targetChannelNo || channel.target_channel_no
    const channelNo =
      targetChannelNo !== undefined ? Number(targetChannelNo) : Number(channel.channelNo || 0)

    console.log(`[预设点] 转到预设点 ${preset.presetNo} "${preset.presetName}"，通道: ${channelNo}`)

    // 调用预设点
    const gotoResult = await RPC.PTZManager('start', channelNo, {
      code: 'GotoPreset',
      arg1: preset.presetNo,
      arg2: 0,
      arg3: 0
    })

    if (!gotoResult || gotoResult.result === false) {
      throw new Error(gotoResult?.error?.message || '转到预设点失败')
    }

    ElMessage.success(`正在转到 "${preset.presetName}"`)
  } catch (error: any) {
    console.error('[预设点] 转到失败:', error)
    const errorMsg = error?.message || error?.error?.message || JSON.stringify(error)
    ElMessage.error('转到预设点失败：' + errorMsg)
    selectedPresetNo.value = null
  }
}

const currentControlledLabel = computed(() => {
  const pane = panes.value[activePane.value]
  if (!pane || !pane.channel) {
    return '未选择'
  }
  return pane.channel.channelName || pane.channel.name || '未命名通道'
})
const patrolPlans = ref<any[]>([])
const selectedPatrolPlan = ref<number>()
const viewSaveDialogVisible = ref(false)
const viewSaveForm = reactive({
  name: '',
  groupIds: [] as number[],
  saveToPatrol: false,
  patrolPlanId: 1
})

// 当前加载的视图
const currentView = ref<any>(null)

// 新建视图 - 清空当前视图和所有窗格
const handleNewView = () => {
  currentView.value = null

  // 清理所有播放器资源
  cleanupAllPlayers()

  ElMessage.success('已创建新视图，请添加通道')
}

// 保存视图 - 保存当前视图（如果已加载）或创建新视图
const handleSaveView = () => {
  if (currentView.value) {
    // 如果有当前视图，直接更新
    handleUpdateView()
  } else {
    // 否则另存为新视图
    handleSaveAsView()
  }
}

// 另存为新视图
const handleSaveAsView = () => {
  viewSaveForm.name = currentView.value ? `${currentView.value.name} - 副本` : ''
  viewSaveForm.groupIds = currentView.value?.groupIds || []
  viewSaveForm.patrolPlanId = selectedPatrolPlan.value || 0
  viewSaveDialogVisible.value = true
}

// 更新当前视图
const handleUpdateView = async () => {
  if (!currentView.value) {
    ElMessage.warning('当前没有加载视图')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要更新视图"${currentView.value.name}"吗？`, '更新视图', {
      confirmButtonText: '更新',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 收集当前分屏配置
    const viewData = {
      id: currentView.value.id,
      name: currentView.value.name,
      groupIds: currentView.value.groupIds,
      gridLayout: gridLayout.value,
      panes: panes.value
        .map((pane, index) => ({
          paneIndex: index,
          channelId: pane.channel?.id,
          deviceId: pane.channel?.deviceId,
          channelNo: pane.channel?.channelNo,
          channelName: pane.channel?.channelName || pane.channel?.name,
          targetIp: pane.channel?.targetIp || pane.channel?.target_ip,
          targetChannelNo: pane.channel?.targetChannelNo || pane.channel?.target_channel_no,
          streamUrlMain: pane.channel?.streamUrlMain || pane.channel?.stream_url_main,
          streamUrlSub: pane.channel?.streamUrlSub || pane.channel?.stream_url_sub,
          config: pane.channel?.config
        }))
        .filter((p) => p.channelId)
    }

    // 调用后端API更新
    await updateVideoView(viewData)

    // 更新当前视图信息
    currentView.value.gridLayout = gridLayout.value

    // 重新加载分组列表
    await loadViewGroups()

    ElMessage.success(`视图"${currentView.value.name}"已更新`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('[更新视图] 失败:', error)
      ElMessage.error('更新视图失败')
    }
  }
}

// 轮巡状态管理
const isPatrolling = ref(false)
const currentPatrolTaskIndex = ref(0)
const patrolTimer = ref<number | null>(null)
const currentPatrolPlan = ref<any>(null)
const currentPatrolTasks = ref<any[]>([])
// 窗格通道切换管理：记录每个窗格的通道列表和当前播放索引
const paneChannelQueues = ref<
  Map<number, { channels: any[]; currentIndex: number; timer: number | null }>
>(new Map())

// 开始轮巡
const handleStartPatrol = async () => {
  if (!selectedPatrolPlan.value) {
    ElMessage.warning('请先选择轮巡计划')
    return
  }

  if (isPatrolling.value) {
    // 如果正在轮巡，则停止
    stopPatrol()
    return
  }

  try {
    console.log('[轮巡] 开始执行轮巡计划 ID:', selectedPatrolPlan.value)

    // 1. 获取轮巡计划详情
    const plan = await getPatrolPlan(selectedPatrolPlan.value)
    currentPatrolPlan.value = plan

    console.log('[轮巡] 计划详情:', plan)

    // 2. 获取计划下的任务列表
    const tasks = await getPatrolTaskList(selectedPatrolPlan.value)

    if (!tasks || tasks.length === 0) {
      ElMessage.warning('该轮巡计划没有配置任务')
      return
    }

    // 按 taskOrder 排序
    currentPatrolTasks.value = tasks
      .filter((t: any) => t.status === 1) // 只执行启用的任务
      .sort((a: any, b: any) => (a.taskOrder || 0) - (b.taskOrder || 0))

    if (currentPatrolTasks.value.length === 0) {
      ElMessage.warning('该轮巡计划没有启用的任务')
      return
    }

    console.log('[轮巡] 共有', currentPatrolTasks.value.length, '个任务')

    // 3. 开始轮巡
    isPatrolling.value = true
    currentPatrolTaskIndex.value = 0

    ElMessage.success(`开始轮巡: ${plan.planName}`)

    // 执行第一个任务
    await executePatrolTask(currentPatrolTasks.value[0])
  } catch (error) {
    console.error('[轮巡] 启动失败:', error)
    ElMessage.error('启动轮巡失败')
    stopPatrol()
  }
}

// 播放窗格中的指定通道
const playPaneChannel = async (paneIndex: number, channelIndex: number) => {
  const queue = paneChannelQueues.value.get(paneIndex)
  if (!queue || channelIndex >= queue.channels.length) {
    console.log('[轮巡] 窗格', paneIndex + 1, '所有通道播放完成，停止播放')

    // 停止当前窗格的播放器 - mpegts.js 正确销毁顺序
    const pane = panes.value[paneIndex]
    if ((pane as any).player) {
      try {
        const player = (pane as any).player
        if (typeof player.pause === 'function') player.pause()
        if (typeof player.unload === 'function') player.unload()
        if (typeof player.detachMediaElement === 'function') player.detachMediaElement()
        if (typeof player.destroy === 'function') player.destroy()
      } catch (e) {
        console.error('[轮巡] 停止播放器失败:', e)
      }
      ;(pane as any).player = null
    }
    pane.channel = null
    return
  }

  const channelData = queue.channels[channelIndex]
  const duration = channelData.duration || 10 // 默认10秒

  console.log(
    '[轮巡] 窗格',
    paneIndex + 1,
    '播放通道',
    channelIndex + 1,
    '/',
    queue.channels.length,
    ':',
    channelData.channelName,
    '时长:',
    duration,
    '秒'
  )

  // 构建通道对象
  // 如果 targetIp 为空，尝试从 streamUrlMain 中提取 IP 地址
  let extractedIp = channelData.targetIp
  if (!extractedIp && channelData.streamUrlMain) {
    // 从 RTSP URL 中提取 IP，格式如: rtsp://user:pass@192.168.1.202:554/...
    const rtspMatch = channelData.streamUrlMain.match(/rtsp:\/\/[^@]*@([^:\/]+)/)
    if (rtspMatch && rtspMatch[1]) {
      extractedIp = rtspMatch[1]
    }
  }

  const channel = {
    id: channelData.channelId,
    deviceId: channelData.deviceId,
    channelNo: channelData.channelNo,
    channelName: channelData.channelName,
    targetIp: extractedIp,
    target_ip: extractedIp,
    targetChannelNo: channelData.targetChannelNo,
    target_channel_no: channelData.targetChannelNo,
    streamUrlMain: channelData.streamUrlMain,
    stream_url_main: channelData.streamUrlMain,
    streamUrlSub: channelData.streamUrlSub,
    stream_url_sub: channelData.streamUrlSub,
    config: channelData.config
  }

  try {
    // 播放通道
    await playChannelInPane(channel, paneIndex)

    // 更新当前播放索引
    queue.currentIndex = channelIndex

    // 设置定时器，在指定时长后切换到下一个通道
    if (queue.timer) {
      clearTimeout(queue.timer)
    }

    queue.timer = window.setTimeout(() => {
      if (!isPatrolling.value) {
        return
      }

      // 播放下一个通道
      playPaneChannel(paneIndex, channelIndex + 1)
    }, duration * 1000)
  } catch (error) {
    console.error('[轮巡] 播放通道失败:', channel.channelName, error)
    // 即使失败也继续下一个通道
    setTimeout(() => {
      if (isPatrolling.value) {
        playPaneChannel(paneIndex, channelIndex + 1)
      }
    }, 1000)
  }
}

// 清理所有窗格的通道切换定时器
const clearAllPaneChannelTimers = () => {
  paneChannelQueues.value.forEach((queue) => {
    if (queue.timer) {
      clearTimeout(queue.timer)
      queue.timer = null
    }
  })
  paneChannelQueues.value.clear()
}

// 停止轮巡
const stopPatrol = () => {
  console.log('[轮巡] 停止轮巡')

  isPatrolling.value = false

  // 清除任务切换定时器
  if (patrolTimer.value) {
    clearTimeout(patrolTimer.value)
    patrolTimer.value = null
  }

  // 清除所有窗格的通道切换定时器
  clearAllPaneChannelTimers()

  // 关闭所有播放器
  console.log('[轮巡] 关闭所有播放器')
  cleanupAllPlayers()

  ElMessage.info('轮巡已停止，所有播放器已关闭')
}

// 执行单个轮巡任务
const executePatrolTask = async (task: any) => {
  try {
    console.log('[轮巡] 执行任务:', task.taskName, 'Order:', task.taskOrder)

    // 1. 获取任务对应的场景
    const scene = await getPatrolSceneByTaskId(task.id)

    if (!scene) {
      console.warn('[轮巡] 任务没有配置场景，跳过:', task.taskName)
      // 继续下一个任务
      scheduleNextTask(task)
      return
    }

    console.log('[轮巡] 场景详情:', scene)
    console.log('[轮巡] 场景分屏配置:', {
      gridLayout: scene.gridLayout,
      gridLayoutType: typeof scene.gridLayout,
      gridCount: scene.gridCount,
      channelsLength: scene.channels?.length || 0
    })

    // 2. 清理所有播放器（在切换布局前）
    cleanupAllPlayers()

    // 3. 设置分屏布局
    // gridLayout 可能是 "2x2" 格式的字符串，需要转换为数字
    let gridLayoutValue: number
    if (typeof scene.gridLayout === 'string' && scene.gridLayout.includes('x')) {
      // 格式如 "2x2" -> 4, "2x3" -> 6
      const [cols, rows] = scene.gridLayout.split('x').map(Number)
      gridLayoutValue = cols * rows
    } else {
      // 直接是数字或可以转换为数字
      gridLayoutValue = parseInt(scene.gridLayout) || scene.gridCount || 6
    }

    console.log('[轮巡] 解析后的分屏布局值:', gridLayoutValue, '(原始值:', scene.gridLayout, ')')
    setLayout(gridLayoutValue)

    // 等待 DOM 更新完成（setLayout 会重新创建 panes 和 DOM 元素）
    await nextTick()

    // 再等待一帧，确保 video 元素已经被 Vue 渲染并通过 ref 回调设置到 panes 中
    await new Promise((resolve) => setTimeout(resolve, 100))

    console.log('[轮巡] DOM 更新完成，panes 数量:', panes.value.length)

    // 验证 videoEl 是否已设置
    const videoElsReady = panes.value.filter((p) => p.videoEl).length
    console.log('[轮巡] 已准备好的 video 元素数量:', videoElsReady, '/', panes.value.length)

    // 4. 加载场景中的通道
    if (scene.channels && scene.channels.length > 0) {
      console.log('[轮巡] 加载', scene.channels.length, '个通道')

      // 清理旧的窗格通道队列
      clearAllPaneChannelTimers()

      // 按窗格分组通道
      const paneChannelsMap = new Map<number, any[]>()
      for (const channelData of scene.channels) {
        // gridPosition 是从 1 开始的，需要转换为 0-based 索引
        const paneIndex = (channelData.gridPosition || 1) - 1

        if (paneIndex >= 0 && paneIndex < panes.value.length) {
          if (!paneChannelsMap.has(paneIndex)) {
            paneChannelsMap.set(paneIndex, [])
          }
          paneChannelsMap.get(paneIndex)!.push(channelData)
        } else {
          console.warn('[轮巡] 窗格索引', paneIndex, '超出范围，跳过')
        }
      }

      console.log(
        '[轮巡] 窗格分组:',
        Array.from(paneChannelsMap.entries())
          .map(([idx, chs]) => `窗格${idx + 1}: ${chs.length}个通道`)
          .join(', ')
      )

      // 为每个窗格启动通道播放队列
      for (const [paneIndex, channelList] of paneChannelsMap.entries()) {
        const pane = panes.value[paneIndex]

        // 检查 videoEl 是否存在
        if (!pane.videoEl) {
          console.warn('[轮巡] 窗格', paneIndex + 1, 'videoEl 未就绪，跳过')
          continue
        }

        // 初始化窗格的通道队列
        paneChannelQueues.value.set(paneIndex, {
          channels: channelList,
          currentIndex: 0,
          timer: null
        })

        // 播放第一个通道
        await playPaneChannel(paneIndex, 0)

        // 稍作延迟，避免同时创建太多播放器
        await new Promise((resolve) => setTimeout(resolve, 200))
      }
    }

    ElMessage.success(`正在执行: ${task.taskName} (${scene.sceneName})`)

    // 5. 根据场景时长安排下一个任务
    scheduleNextTask(task, scene.duration)
  } catch (error) {
    console.error('[轮巡] 执行任务失败:', error)
    // 继续下一个任务
    scheduleNextTask(task)
  }
}

// 安排下一个任务
const scheduleNextTask = (currentTask: any, duration?: number) => {
  // 使用场景时长，如果没有则使用任务时长，默认10秒
  const delaySeconds = duration || currentTask.duration || 10

  console.log('[轮巡] 将在', delaySeconds, '秒后切换到下一个任务')

  // 清除旧的定时器
  if (patrolTimer.value) {
    clearTimeout(patrolTimer.value)
  }

  // 设置新的定时器
  patrolTimer.value = window.setTimeout(() => {
    if (!isPatrolling.value) {
      return
    }

    // 移动到下一个任务
    currentPatrolTaskIndex.value++

    // 检查是否完成所有任务
    if (currentPatrolTaskIndex.value >= currentPatrolTasks.value.length) {
      // 检查循环模式
      const loopMode = currentPatrolPlan.value?.loopMode || 1

      if (loopMode === 1) {
        // 循环执行：重新开始
        console.log('[轮巡] 完成一轮，重新开始')
        currentPatrolTaskIndex.value = 0
        executePatrolTask(currentPatrolTasks.value[0])
      } else {
        // 执行一次：停止
        console.log('[轮巡] 完成所有任务，停止轮巡')
        stopPatrol()
        ElMessage.success('轮巡计划执行完成')
      }
    } else {
      // 执行下一个任务
      executePatrolTask(currentPatrolTasks.value[currentPatrolTaskIndex.value])
    }
  }, delaySeconds * 1000)
}
const router = useRouter()
const handleOpenPatrolConfig = () => {
  router.push('/security/video-surveillance/patrol-config')
}
const submitSaveView = async () => {
  if (!viewSaveForm.name || !viewSaveForm.name.trim()) {
    ElMessage.warning('请输入视图名称')
    return
  }

  try {
    // 收集当前分屏配置
    const viewData = {
      name: viewSaveForm.name,
      groupIds: viewSaveForm.groupIds.length > 0 ? viewSaveForm.groupIds : [1], // 默认分组ID为1
      gridLayout: gridLayout.value, // 分屏格式（1/4/6/9/16）
      panes: panes.value
        .map((pane, index) => ({
          paneIndex: index,
          channelId: pane.channel?.id,
          deviceId: pane.channel?.deviceId,
          channelNo: pane.channel?.channelNo,
          channelName: pane.channel?.channelName || pane.channel?.name,
          targetIp: pane.channel?.targetIp || pane.channel?.target_ip,
          targetChannelNo: pane.channel?.targetChannelNo || pane.channel?.target_channel_no,
          streamUrlMain: pane.channel?.streamUrlMain || pane.channel?.stream_url_main,
          streamUrlSub: pane.channel?.streamUrlSub || pane.channel?.stream_url_sub,
          config: pane.channel?.config
        }))
        .filter((p) => p.channelId) // 只保存有通道的窗格
    }

    // 调用后端API保存
    await createVideoView(viewData)

    // 重新加载分组列表
    await loadViewGroups()

    viewSaveDialogVisible.value = false
    ElMessage.success(`视图"${viewSaveForm.name}"已保存`)

    console.log('[保存视图] 视图已保存到数据库')
  } catch (error) {
    console.error('[保存视图] 失败:', error)
    ElMessage.error('保存视图失败')
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/dark-theme.scss';

.video-playback-container {
  height: 100%;
  display: flex;
  flex-direction: column;

  .top-nav {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background: rgba(255, 255, 255, 0.05);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 8px 8px 0 0;

    .nav-left {
      flex: 1;

      .page-title {
        font-size: 16px;
        font-weight: 500;
        color: rgba(255, 255, 255, 0.9);
      }
    }

    .nav-right {
      flex: 1;
      display: flex;
      justify-content: flex-end;
    }
  }

  .main-layout {
    flex: 1;
    overflow: hidden;

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
        overflow-y: auto;
        overflow-x: hidden;

        .panel-section {
          border-bottom: 1px solid #3a3a3a;

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
            max-height: 200px;
            overflow-y: auto;
            padding: 8px;
          }
          .views-panel {
            padding: 8px 12px;
            max-height: 150px;
            overflow-y: auto;
          }
          .ptz-panel {
            padding: 12px;
            max-height: 450px;
            overflow-y: auto;
          }
          .ptz-card {
            background: #15243a;
            border: 1px solid #233754;
            border-radius: 10px;
            padding: 16px;
            color: #cfe3ff;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.35);
          }
          .ptz-title {
            font-size: 14px;
            font-weight: 600;
            text-align: center;
            margin-bottom: 4px;
          }
          .ptz-current {
            text-align: center;
            font-size: 12px;
            color: #4ec1ff;
            margin-bottom: 12px;
          }
          .ptz-dpad {
            position: relative;
            width: 156px;
            height: 156px;
            margin: 0 auto 14px;
            background: radial-gradient(
              ellipse at center,
              rgba(67, 108, 167, 0.25),
              rgba(24, 36, 60, 0.6)
            );
            border: 1px solid #2b3e62;
            border-radius: 50%;
            display: grid;
            place-items: center;
          }
          .ptz-dir {
            position: absolute;
            width: 46px;
            height: 46px;
            border-radius: 50%;
            background: linear-gradient(#2a4672, #243a5e);
            border: 1px solid #3a5b8a;
            color: #cfe3ff;
            display: grid;
            place-items: center;
            box-shadow: inset 0 0 8px rgba(255, 255, 255, 0.06);
          }
          .ptz-dir.up {
            top: 8px;
            left: 50%;
            transform: translateX(-50%);
          }
          .ptz-dir.down {
            bottom: 8px;
            left: 50%;
            transform: translateX(-50%);
          }
          .ptz-dir.left {
            left: 8px;
            top: 50%;
            transform: translateY(-50%);
          }
          .ptz-dir.right {
            right: 8px;
            top: 50%;
            transform: translateY(-50%);
          }
          .ptz-center {
            position: absolute;
            width: 60px;
            height: 60px;
            border-radius: 50%;
            background: linear-gradient(#38679f, #2b5280);
            border: 1px solid #3a5b8a;
            color: #fff;
            display: grid;
            place-items: center;
            box-shadow: inset 0 0 10px rgba(255, 255, 255, 0.08);
          }
          .ptz-step {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 10px;
            padding: 0 8px;
          }
          .ptz-step span {
            font-size: 12px;
            color: #9fc0ff;
          }
          .ptz-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 10px;
            padding: 0 4px;
          }
          .ptz-op {
            display: grid;
            grid-template-rows: 1fr auto;
            align-items: center;
            justify-items: center;
            height: 60px;
            border-radius: 8px;
            border: 1px solid #2b3e62;
            background: #1a2a49;
            color: #cfe3ff;
          }
          .ptz-op span {
            font-size: 12px;
            color: #cfe3ff;
          }
          .ptz-op:hover {
            border-color: #4ea1ff;
            box-shadow: 0 0 8px rgba(78, 161, 255, 0.35);
          }

          :deep(.el-collapse) {
            background: transparent;
            border: none;
          }
          :deep(.el-collapse-item__header) {
            background: #1e1e1e;
            color: #e0e0e0;
            border: 1px solid #3a3a3a;
            border-radius: 4px;
            padding: 8px 12px;
          }
          :deep(.el-collapse-item__wrap) {
            background: #1e1e1e;
            border: 1px solid #3a3a3a;
            border-top: none;
            border-radius: 0 0 4px 4px;
          }
          :deep(.el-collapse-item__content) {
            padding: 8px 0 12px;
          }

          // 禁用状态样式
          :deep(.el-collapse-item.is-disabled) {
            .el-collapse-item__header {
              background: #1a1a1a;
              color: #666;
              cursor: not-allowed;
              opacity: 0.6;
            }
          }

          .disabled-text {
            color: #666;
          }

          .ptz-tip {
            font-size: 12px;
            color: #888;
            margin-left: 8px;
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
        overflow: hidden;
        min-height: 0;

        .player-section {
          flex: 1;
          display: flex;
          flex-direction: column;
          overflow: hidden;
          min-height: 0;
          padding: 24px 16px 4px 16px;

          .section-header {
            display: none;
          }

          .player-grid {
            flex: 1;
            display: grid;
            gap: 8px;
            background: #000;
            min-height: 0;
            padding: 12px 8px 8px;
            border-top: 1px solid #2f2f2f;
            overflow: hidden;

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

              // 轮巡模式：隐藏边框和阴影，提供纯净观看体验
              &.patrol-mode {
                cursor: default;

                &.active {
                  border-color: #2f2f2f;
                  box-shadow: none;
                }
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
                pointer-events: auto;
                z-index: 1;

                .overlay-top {
                  position: absolute;
                  top: 0;
                  left: 0;
                  right: 0;
                  padding: 6px 10px;
                  background: linear-gradient(180deg, rgba(0, 0, 0, 0.6) 0%, transparent 100%);
                  pointer-events: none;

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
                  pointer-events: none;

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
                    .loading-icon {
                      animation: rotate 1.5s linear infinite;
                      color: #409eff;
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
                  background: linear-gradient(0deg, rgba(0, 0, 0, 0.6) 0%, transparent 100%);
                  pointer-events: none;

                  .device-name {
                    color: #fff;
                    font-size: 12px;
                  }
                }

                .pane-toolbar {
                  position: absolute;
                  top: 8px;
                  right: 8px;
                  display: flex;
                  gap: 6px;
                  opacity: 0;
                  transition: opacity 0.2s;
                  pointer-events: auto;
                  z-index: 10;

                  .el-button {
                    background: rgba(0, 0, 0, 0.7);
                    border-color: rgba(255, 255, 255, 0.2);
                    color: #fff;

                    &:hover {
                      background: rgba(64, 158, 255, 0.8);
                      border-color: #409eff;
                    }
                  }
                }
              }

              &:hover .pane-toolbar {
                opacity: 1;
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
                height: 18px;
                background: rgba(255, 255, 255, 0.06);
                border: 1px solid rgba(255, 255, 255, 0.1);
                border-radius: 6px;
                cursor: pointer;
                overflow: hidden;
                .timeline-seg {
                  position: absolute;
                  top: 0;
                  bottom: 0;
                  background: #f5d11b;
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
            display: flex;
            align-items: center;
            justify-content: flex-start;
            gap: 6px;
            padding: 6px 12px;
            position: sticky;
            bottom: 0;
            left: 0;
            right: 0;
            background: #1e1e1e;
            border-top: 1px solid #3a3a3a;
            margin-bottom: 2px;
            z-index: 5;

            .controls-left,
            .controls-center,
            .controls-right {
              display: flex;
              align-items: center;
              gap: 6px;
            }
            .controls-right {
              margin-left: auto;
            }

            .current-view-info {
              display: flex;
              align-items: center;
              gap: 6px;
              padding: 4px 12px;
              background: rgba(64, 158, 255, 0.1);
              border: 1px solid rgba(64, 158, 255, 0.3);
              border-radius: 4px;
              margin-right: 8px;

              .view-name {
                font-size: 14px;
                font-weight: 500;
                color: rgba(255, 255, 255, 0.9);
              }
            }

            :deep(.el-button) {
              padding: 4px 8px;
            }
            :deep(.el-button.is-circle) {
              width: 28px;
              height: 28px;
              padding: 0;
            }
            :deep(.el-select) {
              width: 140px;
            }
            :deep(.el-divider--vertical) {
              height: 20px;
              margin: 0 8px;
            }
          }
        }
      }
    }
  }
}

/* 云台圆圈五按钮通用样式（避免嵌套导致作用域失效） */
.ptz-panel {
  padding: 12px;
}
.ptz-card {
  background: #15243a;
  border: 1px solid #233754;
  border-radius: 10px;
  padding: 16px;
  color: #cfe3ff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.35);
}
.ptz-title {
  font-size: 14px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 4px;
}
.ptz-current {
  text-align: center;
  font-size: 12px;
  color: #4ec1ff;
  margin-bottom: 12px;
}
.ptz-dpad {
  position: relative;
  width: 156px;
  height: 156px;
  margin: 0 auto 14px;
  background: radial-gradient(ellipse at center, rgba(67, 108, 167, 0.22), rgba(24, 36, 60, 0.55));
  border: 1px solid #2b3e62;
  border-radius: 50%;
  display: grid;
  place-items: center;
}
.ptz-dpad::before {
  content: '';
  position: absolute;
  inset: 8px;
  border: 1px solid rgba(160, 200, 255, 0.15);
  border-radius: 50%;
  pointer-events: none;
}
.ptz-dpad::after {
  content: '';
  position: absolute;
  inset: 0;
  border: 1px solid rgba(160, 200, 255, 0.12);
  border-radius: 50%;
  pointer-events: none;
}
.ptz-dir {
  position: absolute;
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: linear-gradient(#2a4672, #243a5e);
  border: 1px solid #3a5b8a;
  color: #aef;
  display: grid;
  place-items: center;
  box-shadow: inset 0 0 8px rgba(255, 255, 255, 0.06);
  border: none;
}
.ptz-dir.up {
  top: 12px;
  left: 50%;
  transform: translateX(-50%);
}
.ptz-dir.down {
  bottom: 12px;
  left: 50%;
  transform: translateX(-50%);
}
.ptz-dir.left {
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
}
.ptz-dir.right {
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
}
.ptz-center {
  position: absolute;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(#38679f, #2b5280);
  border: 1px solid #3a5b8a;
  color: #aef;
  display: grid;
  place-items: center;
  box-shadow: inset 0 0 10px rgba(255, 255, 255, 0.08);
  border: none;
}
.ptz-center:focus,
.ptz-dir:focus {
  outline: none;
}
.ptz-center :deep(svg),
.ptz-dir :deep(svg) {
  filter: drop-shadow(0 0 2px rgba(80, 210, 255, 0.6));
}
.ptz-step {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  padding: 0 8px;
}
.ptz-step span {
  font-size: 12px;
  color: #9fc0ff;
}
.ptz-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  padding: 0 4px;
}
.ptz-op {
  display: grid;
  grid-template-rows: 1fr auto;
  align-items: center;
  justify-items: center;
  height: 60px;
  border-radius: 8px;
  border: 1px solid #2b3e62;
  background: #1a2a49;
  color: #cfe3ff;
}
.ptz-op span {
  font-size: 12px;
  color: #cfe3ff;
}
.ptz-op:hover {
  border-color: #4ea1ff;
  box-shadow: 0 0 8px rgba(78, 161, 255, 0.35);
}

/* 预设点添加区域 */
.ptz-preset-add {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 6px;
  padding: 8px;
  background: rgba(21, 36, 58, 0.5);
  border: 1px solid #2b3e62;
  border-radius: 6px;
  margin-bottom: 8px;
}

.ptz-preset-add :deep(.el-input__inner) {
  background: #1a2a49;
  border-color: #2b3e62;
  color: #cfe3ff;
  height: 28px;
  line-height: 28px;
}

.ptz-preset-add :deep(.el-input:hover .el-input__inner) {
  border-color: #4ea1ff;
}

.ptz-preset-add :deep(.el-button) {
  padding: 4px 12px;
  height: 28px;
}

/* 预设点列表 */
.ptz-preset-list {
  background: rgba(21, 36, 58, 0.5);
  border: 1px solid #2b3e62;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 8px;
}

.preset-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 10px;
  background: rgba(30, 50, 80, 0.6);
  border-bottom: 1px solid #2b3e62;
  font-size: 11px;
  color: #9fc0ff;
  font-weight: 500;
}

.preset-count {
  font-size: 10px;
  color: #7a9aba;
}

.preset-list-items {
  max-height: 150px;
  overflow-y: auto;
}

.preset-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-bottom: 1px solid rgba(43, 62, 98, 0.5);
  cursor: pointer;
  transition: all 0.2s;
}

.preset-item:last-child {
  border-bottom: none;
}

.preset-item:hover {
  background: rgba(78, 161, 255, 0.1);
}

.preset-item.active {
  background: rgba(78, 161, 255, 0.2);
  border-left: 3px solid #4ea1ff;
}

.preset-no {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 24px;
  height: 18px;
  padding: 0 6px;
  background: rgba(78, 161, 255, 0.15);
  border: 1px solid rgba(78, 161, 255, 0.3);
  border-radius: 3px;
  font-size: 11px;
  font-weight: 500;
  color: #4ea1ff;
}

.preset-name {
  flex: 1;
  font-size: 12px;
  color: #cfe3ff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preset-delete {
  font-size: 13px;
  color: #7a9aba;
  cursor: pointer;
  opacity: 0;
  transition: all 0.2s;
}

.preset-item:hover .preset-delete {
  opacity: 1;
}

.preset-delete:hover {
  color: #ff6b6b;
}

/* 空状态 */
.preset-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 12px;
  color: #7a9aba;
  font-size: 12px;
}

/* 滚动条样式 */
.preset-list-items::-webkit-scrollbar {
  width: 6px;
}

.preset-list-items::-webkit-scrollbar-track {
  background: rgba(21, 36, 58, 0.3);
}

.preset-list-items::-webkit-scrollbar-thumb {
  background: rgba(78, 161, 255, 0.3);
  border-radius: 3px;
}

.preset-list-items::-webkit-scrollbar-thumb:hover {
  background: rgba(78, 161, 255, 0.5);
}

/* ==================== 巡航管理样式 ==================== */
.ptz-cruise-section {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;

  .cruise-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    font-weight: 600;
    font-size: 14px;
  }

  .cruise-list {
    max-height: 300px;
    overflow-y: auto;
  }

  .cruise-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 12px;
    margin-bottom: 6px;
    background: white;
    border-radius: 4px;
    border: 1px solid #e4e7ed;
    transition: all 0.3s;

    &.running {
      border-color: #67c23a;
      background: #f0f9ff;
    }

    &:hover {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    .cruise-info {
      display: flex;
      align-items: center;
      gap: 8px;
      flex: 1;
      min-width: 0;

      .status-icon {
        font-size: 16px;
        color: #67c23a;
        animation: rotate 1.5s linear infinite;
      }

      .cruise-name {
        font-weight: 500;
        color: #303133;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .cruise-points {
        font-size: 12px;
        color: #909399;
        white-space: nowrap;
      }
    }

    .cruise-actions {
      display: flex;
      gap: 8px;
      flex-shrink: 0;
      align-items: center;
    }
  }

  @keyframes rotate {
    from {
      transform: rotate(0deg);
    }
    to {
      transform: rotate(360deg);
    }
  }

  .cruise-empty {
    text-align: center;
    padding: 20px;
    color: #909399;
    font-size: 14px;

    .tip-text {
      display: block;
      margin-top: 4px;
      font-size: 12px;
      color: #c0c4cc;
    }
  }
}

/* 巡航对话框样式 */
.cruise-points-selector {
  display: flex;
  gap: 16px;
  height: 400px;

  .available-presets,
  .selected-presets {
    flex: 1;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    overflow: hidden;
    display: flex;
    flex-direction: column;

    .preset-header {
      padding: 10px;
      background: #f5f7fa;
      border-bottom: 1px solid #dcdfe6;
      font-weight: 600;
      font-size: 14px;
      display: flex;
      justify-content: space-between;
      align-items: center;

      .error-tip {
        color: #f56c6c;
        font-size: 12px;
        font-weight: normal;
      }
    }

    .preset-list {
      flex: 1;
      overflow-y: auto;
      padding: 8px;
    }
  }

  .preset-option {
    padding: 10px;
    margin-bottom: 6px;
    background: white;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    cursor: pointer;
    display: flex;
    justify-content: space-between;
    align-items: center;
    transition: all 0.3s;

    &:hover {
      background: #ecf5ff;
      border-color: #409eff;
    }
  }

  .preset-selected {
    padding: 10px;
    margin-bottom: 6px;
    background: #f0f9ff;
    border: 1px solid #b3d8ff;
    border-radius: 4px;
    display: flex;
    gap: 8px;
    align-items: center;

    .preset-order {
      display: flex;
      flex-direction: column;
      gap: 4px;

      .order-num {
        text-align: center;
        font-weight: 600;
        color: #409eff;
        min-width: 20px;
      }
    }

    .preset-info {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 8px;

      span {
        flex: 1;
      }

      .unit {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .empty-tip {
    text-align: center;
    padding: 40px 20px;
    color: #909399;
    font-size: 14px;
  }
}

.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
