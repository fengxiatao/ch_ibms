import { Video, LayoutGrid, Maximize2, Volume2, Settings, Play, Pause, RefreshCw, Search, Clock, Eye, AlertCircle, Bot, History, X, ChevronRight } from 'lucide-react';
import { useState } from 'react';
import { videoList, videoPlaybackRecords, aiRecognitionRecords, realTimeAlarms } from '@/data/mockData';

export function VideoFusion() {
  const [selectedCamera, setSelectedCamera] = useState<string | null>('CAM-001');
  const [viewMode, setViewMode] = useState<'grid' | 'single'>('grid');
  const [isPlaying, setIsPlaying] = useState(true);
  const [showPopup, setShowPopup] = useState(false);
  const [popupCamera, setPopupCamera] = useState<any>(null);
  const [activeTab, setActiveTab] = useState<'live' | 'playback' | 'ai'>('live');
  const [searchTerm, setSearchTerm] = useState('');
  const [filterArea, setFilterArea] = useState<string>('all');
  const [showAIOverlay, setShowAIOverlay] = useState(true);

  const filteredVideos = videoList.filter(video => {
    const matchesSearch = video.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          video.location.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesArea = filterArea === 'all' || video.area === filterArea;
    return matchesSearch && matchesArea;
  });

  const handleCameraClick = (camera: any) => {
    setSelectedCamera(camera.id);
    setViewMode('single');
  };

  const handlePopupVideo = (camera: any) => {
    setPopupCamera(camera);
    setShowPopup(true);
  };

  const handleAlarmLinkage = (cameraId: string | null) => {
    if (cameraId) {
      const camera = videoList.find(v => v.id === cameraId);
      if (camera) {
        setSelectedCamera(cameraId);
        setViewMode('single');
      }
    }
  };

  const currentCamera = videoList.find(v => v.id === selectedCamera);
  const relatedAlarms = realTimeAlarms.filter(a => a.cameraId === selectedCamera);

  return (
    <div className="h-full flex flex-col">
      {/* 顶部标签页 */}
      <div className="flex items-center justify-between px-4 py-2 bg-[#111827] border-b border-[#1F2937]">
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2 bg-[#1F2937] rounded-lg p-1">
            {['live', 'playback', 'ai'].map((tab) => (
              <button
                key={tab}
                onClick={() => setActiveTab(tab as any)}
                className={`px-4 py-1.5 rounded text-sm font-medium transition-colors flex items-center gap-2 ${
                  activeTab === tab ? 'bg-blue-500 text-white' : 'text-gray-400 hover:text-white'
                }`}
              >
                {tab === 'live' && <Video className="w-4 h-4" />}
                {tab === 'playback' && <History className="w-4 h-4" />}
                {tab === 'ai' && <Bot className="w-4 h-4" />}
                {tab === 'live' ? '实时视频' : tab === 'playback' ? '视频回放' : 'AI识别'}
              </button>
            ))}
          </div>
          
          {activeTab === 'live' && (
            <div className="flex items-center gap-2">
              <span className="text-xs text-gray-400">
                在线: <span className="text-green-400">{videoList.filter(v => v.status === 'online').length}</span> / {videoList.length}
              </span>
            </div>
          )}
        </div>

        <div className="flex items-center gap-3">
          {/* AI叠加开关 */}
          {activeTab === 'live' && (
            <label className="flex items-center gap-2 text-sm text-gray-400 cursor-pointer">
              <input 
                type="checkbox" 
                checked={showAIOverlay}
                onChange={(e) => setShowAIOverlay(e.target.checked)}
                className="w-4 h-4 rounded bg-[#1F2937] border-[#374151] text-blue-500 focus:ring-blue-500"
              />
              AI叠加
            </label>
          )}
          
          {/* 视图切换 */}
          {activeTab === 'live' && (
            <div className="flex items-center gap-1 bg-[#1F2937] rounded-lg p-1">
              <button
                onClick={() => setViewMode('grid')}
                className={`p-1.5 rounded ${viewMode === 'grid' ? 'bg-blue-500 text-white' : 'text-gray-400 hover:text-white'}`}
              >
                <LayoutGrid className="w-4 h-4" />
              </button>
              <button
                onClick={() => setViewMode('single')}
                className={`p-1.5 rounded ${viewMode === 'single' ? 'bg-blue-500 text-white' : 'text-gray-400 hover:text-white'}`}
              >
                <Maximize2 className="w-4 h-4" />
              </button>
            </div>
          )}
          
          <button className="p-2 bg-[#1F2937] rounded-lg text-gray-400 hover:text-white transition-colors">
            <RefreshCw className="w-4 h-4" />
          </button>
          <button className="p-2 bg-[#1F2937] rounded-lg text-gray-400 hover:text-white transition-colors">
            <Settings className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* 主内容区 */}
      <div className="flex-1 flex overflow-hidden">
        {/* 视频监控区域 */}
        <div className="flex-1 flex flex-col">
          {/* 搜索和筛选 */}
          {activeTab === 'live' && (
            <div className="flex items-center gap-4 px-4 py-3 bg-[#111827]/50">
              <div className="relative flex-1 max-w-md">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" />
                <input
                  type="text"
                  placeholder="搜索摄像头..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full bg-[#1F2937] border border-[#374151] rounded-lg py-2 pl-10 pr-4 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-blue-500"
                />
              </div>
              <select
                value={filterArea}
                onChange={(e) => setFilterArea(e.target.value)}
                className="bg-[#1F2937] border border-[#374151] rounded-lg py-2 px-3 text-sm text-white focus:outline-none focus:border-blue-500"
              >
                <option value="all">全部区域</option>
                <option value="production">生产区</option>
                <option value="storage">仓储区</option>
                <option value="perimeter">周界</option>
                <option value="office">办公区</option>
                <option value="security">安防区</option>
              </select>
            </div>
          )}

          {/* 视频显示区 */}
          <div className="flex-1 relative bg-gradient-to-br from-[#0a0f1c] to-[#111827]">
            {activeTab === 'live' && (
              <>
                {viewMode === 'single' ? (
                  <div className="h-full flex items-center justify-center relative">
                    {/* 主视频区域 */}
                    <div className="w-full h-full flex items-center justify-center">
                      <div className="text-center">
                        <div className="w-32 h-32 border-2 border-blue-500/30 rounded-xl flex items-center justify-center mx-auto mb-4 bg-[#1F2937]/50">
                          <Video className="w-16 h-16 text-blue-400" />
                        </div>
                        <p className="text-white text-xl font-medium">{currentCamera?.name}</p>
                        <p className="text-gray-400 text-sm mt-1">{currentCamera?.location} | {currentCamera?.type}</p>
                        <div className="flex items-center justify-center gap-2 mt-3">
                          <span className={`px-2 py-1 rounded text-xs ${
                            currentCamera?.status === 'online' ? 'bg-green-500/20 text-green-400' : 'bg-red-500/20 text-red-400'
                          }`}>
                            {currentCamera?.status === 'online' ? '在线' : '离线'}
                          </span>
                        </div>
                      </div>
                    </div>

                    {/* AI识别叠加层 */}
                    {showAIOverlay && (
                      <div className="absolute inset-0 pointer-events-none">
                        {/* 人脸识别框 */}
                        <div className="absolute top-1/4 left-1/4 w-20 h-24 border-2 border-green-400 rounded">
                          <span className="absolute -top-6 left-0 bg-green-500/80 text-white text-xs px-2 py-0.5 rounded">人脸:张三</span>
                        </div>
                        {/* 区域检测框 */}
                        <div className="absolute bottom-1/3 right-1/4 w-32 h-20 border-2 border-yellow-400 rounded">
                          <span className="absolute -top-6 left-0 bg-yellow-500/80 text-white text-xs px-2 py-0.5 rounded">聚集:8人</span>
                        </div>
                      </div>
                    )}

                    {/* 关联告警提示 */}
                    {relatedAlarms.length > 0 && (
                      <div className="absolute top-4 left-4 bg-red-500/90 text-white px-4 py-2 rounded-lg flex items-center gap-2">
                        <AlertCircle className="w-4 h-4" />
                        <span className="text-sm font-medium">关联告警: {relatedAlarms[0].title}</span>
                        <button 
                          onClick={() => handleAlarmLinkage(relatedAlarms[0].cameraId)}
                          className="ml-2 px-2 py-0.5 bg-white/20 rounded text-xs hover:bg-white/30"
                        >
                          查看
                        </button>
                      </div>
                    )}

                    {/* 视频控制栏 */}
                    <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/90 to-transparent p-4">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3">
                          <button 
                            onClick={() => setIsPlaying(!isPlaying)}
                            className="p-2 bg-white/10 rounded-lg text-white hover:bg-white/20"
                          >
                            {isPlaying ? <Pause className="w-5 h-5" /> : <Play className="w-5 h-5" />}
                          </button>
                          <div className="w-64 h-1 bg-gray-600 rounded-full overflow-hidden">
                            <div className="w-1/3 h-full bg-blue-500" />
                          </div>
                          <span className="text-xs text-gray-300">08:32:15 / 23:59:59</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <button className="p-2 bg-white/10 rounded-lg text-white hover:bg-white/20">
                            <Volume2 className="w-4 h-4" />
                          </button>
                          <button 
                            onClick={() => currentCamera && handlePopupVideo(currentCamera)}
                            className="p-2 bg-white/10 rounded-lg text-white hover:bg-white/20"
                          >
                            <Maximize2 className="w-4 h-4" />
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                ) : (
                  /* 网格视图 */
                  <div className="h-full p-4 overflow-auto">
                    <div className="grid grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 h-full">
                      {filteredVideos.map((camera) => (
                        <div 
                          key={camera.id}
                          onClick={() => handleCameraClick(camera)}
                          onDoubleClick={() => handlePopupVideo(camera)}
                          className={`relative bg-[#1F2937] rounded-lg overflow-hidden cursor-pointer hover:ring-2 hover:ring-blue-500/50 transition-all ${
                            selectedCamera === camera.id ? 'ring-2 ring-blue-500' : ''
                          } ${camera.status === 'offline' ? 'opacity-60' : ''}`}
                        >
                          <div className="aspect-video bg-gradient-to-br from-[#1F2937] to-[#0a0f1c] flex items-center justify-center">
                            <div className="w-12 h-12 border-2 border-blue-500/30 rounded-lg flex items-center justify-center">
                              <Video className="w-6 h-6 text-blue-400" />
                            </div>
                          </div>
                          <div className="absolute inset-0 opacity-0 hover:opacity-100 bg-black/50 flex items-center justify-center transition-opacity">
                            <Eye className="w-8 h-8 text-white" />
                          </div>
                          {/* 状态指示 */}
                          <div className="absolute top-2 right-2 flex items-center gap-1">
                            <span className={`w-2 h-2 rounded-full ${camera.status === 'online' ? 'bg-green-400' : 'bg-red-400'}`} />
                            <span className="text-xs text-white bg-black/50 px-1.5 py-0.5 rounded">{camera.status === 'online' ? '在线' : '离线'}</span>
                          </div>
                          <div className="p-2">
                            <p className="text-sm text-white font-medium truncate">{camera.name}</p>
                            <p className="text-xs text-gray-400">{camera.location}</p>
                          </div>
                          {/* 关联告警标识 */}
                          {realTimeAlarms.some(a => a.cameraId === camera.id) && (
                            <div className="absolute top-2 left-2">
                              <span className="px-1.5 py-0.5 bg-red-500 text-white text-xs rounded flex items-center gap-1">
                                <AlertCircle className="w-3 h-3" />
                                告警
                              </span>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </>
            )}

            {activeTab === 'playback' && (
              <div className="h-full flex flex-col p-4">
                {/* 回放筛选 */}
                <div className="flex items-center gap-4 mb-4">
                  <div className="flex items-center gap-2">
                    <Clock className="w-4 h-4 text-gray-400" />
                    <input
                      type="datetime-local"
                      className="bg-[#1F2937] border border-[#374151] rounded-lg py-2 px-3 text-sm text-white focus:outline-none focus:border-blue-500"
                      defaultValue="2024-01-15T08:00"
                    />
                    <span className="text-gray-400">至</span>
                    <input
                      type="datetime-local"
                      className="bg-[#1F2937] border border-[#374151] rounded-lg py-2 px-3 text-sm text-white focus:outline-none focus:border-blue-500"
                      defaultValue="2024-01-15T18:00"
/>
                  </div>
                  <select className="bg-[#1F2937] border border-[#374151] rounded-lg py-2 px-3 text-sm text-white focus:outline-none focus:border-blue-500">
                    <option value="">全部类型</option>
                    <option value="alarm">告警回放</option>
                    <option value="patrol">人工巡检</option>
                    <option value="event">事件回放</option>
                  </select>
                  <button className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 flex items-center gap-2">
                    <Search className="w-4 h-4" />
                    搜索
                  </button>
                </div>

                {/* 回放列表 */}
                <div className="flex-1 overflow-auto">
                  <div className="space-y-2">
                    {videoPlaybackRecords.map((record) => (
                      <div key={record.id} className="flex items-center justify-between p-3 bg-[#1F2937]/50 rounded-lg hover:bg-[#1F2937] cursor-pointer transition-colors">
                        <div className="flex items-center gap-3">
                          <div className="w-16 h-12 bg-gradient-to-br from-[#1F2937] to-[#0a0f1c] rounded flex items-center justify-center">
                            <Video className="w-6 h-6 text-blue-400" />
                          </div>
                          <div>
                            <p className="text-white font-medium">{record.reason}</p>
                            <p className="text-sm text-gray-400">
                              {videoList.find(v => v.id === record.cameraId)?.name} | {record.startTime} - {record.endTime}
                            </p>
                          </div>
                        </div>
                        <div className="flex items-center gap-3">
                          <span className={`px-2 py-1 rounded text-xs ${
                            record.type === '告警回放' ? 'bg-red-500/20 text-red-400' :
                            record.type === '人工巡检' ? 'bg-blue-500/20 text-blue-400' :
                            'bg-green-500/20 text-green-400'
                          }`}>
                            {record.type}
                          </span>
                          <button className="p-2 bg-blue-500/20 rounded-lg text-blue-400 hover:bg-blue-500/30">
                            <Play className="w-4 h-4" />
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'ai' && (
              <div className="h-full flex flex-col p-4">
                <h3 className="text-white font-medium mb-4">AI智能识别记录</h3>
                <div className="flex-1 overflow-auto">
                  <div className="space-y-2">
                    {aiRecognitionRecords.map((record) => (
                      <div key={record.id} className="flex items-center justify-between p-3 bg-[#1F2937]/50 rounded-lg hover:bg-[#1F2937] cursor-pointer transition-colors">
                        <div className="flex items-center gap-3">
                          <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${
                            record.type === '人脸识别' ? 'bg-blue-500/20' :
                            record.type === '车牌识别' ? 'bg-green-500/20' :
                            record.type === '行为分析' ? 'bg-yellow-500/20' :
                            'bg-purple-500/20'
                          }`}>
                            {record.type === '人脸识别' && <span className="text-blue-400 text-lg">👤</span>}
                            {record.type === '车牌识别' && <span className="text-green-400 text-lg">🚗</span>}
                            {record.type === '行为分析' && <span className="text-yellow-400 text-lg">⚠️</span>}
                            {record.type === '周界检测' && <span className="text-purple-400 text-lg">🛡️</span>}
                          </div>
                          <div>
                            <p className="text-white font-medium">{record.type}</p>
                            <p className="text-sm text-gray-400">
                              {record.type === '人脸识别' && `人员: ${record.person}`}
                              {record.type === '车牌识别' && `车牌: ${record.plate}`}
                              {record.type === '行为分析' && `行为: ${record.behavior}`}
                              {record.type === '周界检测' && `事件: ${record.event}`}
                            </p>
                          </div>
                        </div>
                        <div className="flex items-center gap-3">
                          <span className="text-sm text-gray-400">{record.time}</span>
                          <span className={`px-2 py-1 rounded text-xs ${
                            record.status === '识别成功' ? 'bg-green-500/20 text-green-400' : 'bg-red-500/20 text-red-400'
                          }`}>
                            {record.status}
                          </span>
                          <button className="p-2 bg-blue-500/20 rounded-lg text-blue-400 hover:bg-blue-500/30">
                            <Video className="w-4 h-4" />
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
        
        {/* 右侧摄像头列表 */}
        {activeTab === 'live' && (
          <div className="w-72 bg-[#111827] border-l border-[#1F2937] flex flex-col">
            <div className="p-4 border-b border-[#1F2937]">
              <h3 className="text-sm font-medium text-white">摄像头列表</h3>
              <p className="text-xs text-gray-400 mt-1">{filteredVideos.length} 个摄像头</p>
            </div>
            <div className="flex-1 overflow-y-auto p-2 space-y-1">
              {filteredVideos.map((camera) => (
                <div 
                  key={camera.id}
                  onClick={() => handleCameraClick(camera)}
                  className={`p-3 rounded-lg cursor-pointer transition-colors ${
                    selectedCamera === camera.id 
                      ? 'bg-blue-500/20 border border-blue-500/50' 
                      : 'bg-[#1F2937]/50 hover:bg-[#1F2937]'
                  } ${camera.status === 'offline' ? 'opacity-60' : ''}`}
                >
                  <div className="flex items-center justify-between mb-1">
                    <div className="flex items-center gap-2">
                      <span className={`w-2 h-2 rounded-full ${camera.status === 'online' ? 'bg-green-400' : 'bg-red-400'}`} />
                      <span className="text-sm text-white">{camera.name}</span>
                    </div>
                    {realTimeAlarms.some(a => a.cameraId === camera.id) && (
                      <AlertCircle className="w-4 h-4 text-red-400" />
                    )}
                  </div>
                  <p className="text-xs text-gray-500 ml-4">{camera.location}</p>
                  <div className="flex items-center gap-2 ml-4 mt-1">
                    <span className="text-xs text-gray-600">{camera.type}</span>
                    <button 
                      onClick={(e) => { e.stopPropagation(); handlePopupVideo(camera); }}
                      className="text-xs text-blue-400 hover:text-blue-300 flex items-center gap-1"
                    >
                      弹窗 <ChevronRight className="w-3 h-3" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* 视频弹窗 */}
      {showPopup && popupCamera && (
        <div className="fixed inset-0 bg-black/80 flex items-center justify-center z-50" onClick={() => setShowPopup(false)}>
          <div className="bg-[#111827] rounded-xl w-[900px] max-h-[80vh] overflow-hidden" onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between p-4 border-b border-[#1F2937]">
              <div>
                <h3 className="text-white font-medium">{popupCamera.name}</h3>
                <p className="text-sm text-gray-400">{popupCamera.location}</p>
              </div>
              <button onClick={() => setShowPopup(false)} className="p-2 hover:bg-[#1F2937] rounded-lg">
                <X className="w-5 h-5 text-gray-400" />
              </button>
            </div>
            <div className="aspect-video bg-gradient-to-br from-[#1F2937] to-[#0a0f1c] flex items-center justify-center relative">
              <div className="text-center">
                <div className="w-24 h-24 border-2 border-blue-500/30 rounded-xl flex items-center justify-center mx-auto mb-4">
                  <Video className="w-12 h-12 text-blue-400" />
                </div>
                <p className="text-white">视频流加载中...</p>
              </div>
              {showAIOverlay && (
                <div className="absolute inset-0 pointer-events-none">
                  <div className="absolute top-1/4 left-1/4 w-20 h-24 border-2 border-green-400 rounded" />
                  <div className="absolute bottom-1/3 right-1/4 w-32 h-20 border-2 border-yellow-400 rounded" />
                </div>
              )}
            </div>
            <div className="p-4 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <button className="p-2 bg-white/10 rounded-lg text-white hover:bg-white/20">
                  <Play className="w-4 h-4" />
                </button>
                <span className="text-sm text-gray-400">00:00:00 / 23:59:59</span>
              </div>
              <div className="flex items-center gap-2">
                <button className="p-2 bg-white/10 rounded-lg text-white hover:bg-white/20">
                  <Volume2 className="w-4 h-4" />
                </button>
                <label className="flex items-center gap-2 text-sm text-gray-400">
                  <input 
                    type="checkbox" 
                    checked={showAIOverlay}
                    onChange={(e) => setShowAIOverlay(e.target.checked)}
                    className="w-4 h-4"
                  />
                  AI叠加
                </label>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
