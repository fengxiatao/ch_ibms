import { Shield, Video, User, Car, AlertTriangle, MapPin, Eye, Settings, Play, History, Target, Activity, Lock, Unlock } from 'lucide-react';
import { useState } from 'react';
import { cloudDefenseDevices, defenseZones, intrusionRecords, behaviorAlarms, trajectoryRecords, securitySituation } from '@/data/mockData';

export function CloudDefense() {
  const [selectedDevice, setSelectedDevice] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'perimeter' | 'intrusion' | 'behavior' | 'trajectory' | 'patrol' | 'situation'>('perimeter');
  const [selectedZone, setSelectedZone] = useState<string | null>(null);
  const [, setShowTrajectory] = useState(false);
  const [, setSelectedTrajectory] = useState<any>(null);

  const handleZoneToggle = (zoneId: string) => {
    const zone = defenseZones.find(z => z.id === zoneId);
    if (zone) {
      // 模拟设防/撤防操作
      alert(`${zone.name} ${zone.status === 'armed' ? '已撤防' : '已设防'}`);
    }
  };

  return (
    <div className="h-full flex flex-col">
      {/* 顶部统计卡片 */}
      <div className="grid grid-cols-4 gap-4 p-4 bg-[#111827]">
        <div className="data-card flex items-center gap-4">
          <div className="w-12 h-12 bg-green-500/20 rounded-lg flex items-center justify-center">
            <Shield className="w-6 h-6 text-green-400" />
          </div>
          <div>
            <p className="text-sm text-gray-400">防区状态</p>
            <p className="text-xl font-bold text-white">
              {defenseZones.filter(z => z.status === 'armed').length}/{defenseZones.length} 已设防
            </p>
          </div>
        </div>
        
        <div className="data-card flex items-center gap-4">
          <div className="w-12 h-12 bg-blue-500/20 rounded-lg flex items-center justify-center">
            <Video className="w-6 h-6 text-blue-400" />
          </div>
          <div>
            <p className="text-sm text-gray-400">在线设备</p>
            <p className="text-xl font-bold text-white">
              {cloudDefenseDevices.filter(d => d.status === 'online').length}/{cloudDefenseDevices.length}
            </p>
          </div>
        </div>
        
        <div className="data-card flex items-center gap-4">
          <div className="w-12 h-12 bg-red-500/20 rounded-lg flex items-center justify-center">
            <AlertTriangle className="w-6 h-6 text-red-400" />
          </div>
          <div>
            <p className="text-sm text-gray-400">今日入侵告警</p>
            <p className="text-xl font-bold text-white">{intrusionRecords.length} 次</p>
          </div>
        </div>
        
        <div className="data-card flex items-center gap-4">
          <div className="w-12 h-12 bg-purple-500/20 rounded-lg flex items-center justify-center">
            <Target className="w-6 h-6 text-purple-400" />
          </div>
          <div>
            <p className="text-sm text-gray-400">安全态势评分</p>
            <p className="text-xl font-bold text-white">{securitySituation.overallScore}分</p>
          </div>
        </div>
      </div>

      {/* 功能标签页 */}
      <div className="flex items-center gap-2 px-4 py-2 bg-[#111827]/50 border-b border-[#1F2937]">
        {[
          { id: 'perimeter', label: '周界防护', icon: Shield },
          { id: 'intrusion', label: '区域入侵', icon: Lock },
          { id: 'behavior', label: '行为分析', icon: Activity },
          { id: 'trajectory', label: '轨迹追踪', icon: History },
          { id: 'patrol', label: '智能巡检', icon: Play },
          { id: 'situation', label: '安全态势', icon: Target },
        ].map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id as any)}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-2 ${
              activeTab === tab.id 
                ? 'bg-blue-500 text-white' 
                : 'bg-[#1F2937] text-gray-400 hover:text-white'
            }`}
          >
            <tab.icon className="w-4 h-4" />
            {tab.label}
          </button>
        ))}
      </div>
      
      {/* 主内容区 */}
      <div className="flex-1 flex overflow-hidden p-4 gap-4">
        {/* 左侧主区域 */}
        <div className="flex-1 flex flex-col">
          {activeTab === 'perimeter' && (
            <>
              <div className="data-card flex-1 flex flex-col">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-sm font-medium text-white">周界防护态势图</h3>
                  <div className="flex items-center gap-4 text-xs">
                    <span className="flex items-center gap-1">
                      <Lock className="w-3 h-3 text-green-400" /> 已设防
                    </span>
                    <span className="flex items-center gap-1">
                      <Unlock className="w-3 h-3 text-gray-400" /> 未设防
                    </span>
                  </div>
                </div>
                
                {/* 周界地图 */}
                <div className="flex-1 bg-gradient-to-br from-[#1F2937] to-[#111827] rounded-lg relative overflow-hidden">
                  {/* 厂区轮廓 */}
                  <svg className="absolute inset-4" viewBox="0 0 400 300">
                    {/* 厂区边界 */}
                    <rect x="10" y="10" width="380" height="280" fill="none" stroke="#374151" strokeWidth="2" strokeDasharray="5,5" />
                    
                    {/* 周界围栏 */}
                    <rect x="20" y="20" width="360" height="260" fill="none" stroke="#3B82F6" strokeWidth="3" />
                    
                    {/* 内部区域 */}
                    <rect x="40" y="40" width="100" height="80" fill="#60A5FA" fillOpacity="0.1" stroke="#60A5FA" strokeWidth="1" />
                    <text x="90" y="85" textAnchor="middle" fill="#60A5FA" fontSize="10">仓储区</text>
                    
                    <rect x="160" y="40" width="180" height="120" fill="#F472B6" fillOpacity="0.1" stroke="#F472B6" strokeWidth="1" />
                    <text x="250" y="105" textAnchor="middle" fill="#F472B6" fontSize="10">生产区</text>
                    
                    <rect x="360" y="40" width="20" height="80" fill="#C4B5FD" fillOpacity="0.1" stroke="#C4B5FD" strokeWidth="1" />
                    
                    {/* 周界设备标记 */}
                    {[
                      { cx: 20, cy: 80, id: 'CF-001' },
                      { cx: 20, cy: 160, id: 'CF-002' },
                      { cx: 20, cy: 240, id: 'CF-003' },
                      { cx: 100, cy: 20, id: 'CF-004' },
                      { cx: 200, cy: 20, id: 'CF-005' },
                      { cx: 300, cy: 20, id: 'CF-006' },
                      { cx: 380, cy: 80, id: 'CF-001' },
                      { cx: 380, cy: 160, id: 'CF-002' },
                      { cx: 380, cy: 240, id: 'CF-003' },
                    ].map((point, i) => {
                      const device = cloudDefenseDevices[i % cloudDefenseDevices.length];
                      return (
                        <g key={i} onClick={() => setSelectedDevice(device.id)} className="cursor-pointer">
                          <circle 
                            cx={point.cx} 
                            cy={point.cy} 
                            r="8" 
                            fill={device.status === 'online' ? '#3B82F6' : '#EF4444'} 
                            opacity="0.8"
                          />
                          <circle 
                            cx={point.cx} 
                            cy={point.cy} 
                            r="12" 
                            fill="none" 
                            stroke={device.status === 'online' ? '#3B82F6' : '#EF4444'} 
                            strokeWidth="1"
                            className="animate-pulse"
                          />
                          <text x={point.cx} y={point.cy + 20} textAnchor="middle" fill="#9CA3AF" fontSize="6">
                            {device.id.split('-')[1]}
                          </text>
                        </g>
                      );
                    })}
                    
                    {/* 入侵告警位置 */}
                    {intrusionRecords.filter(r => r.status === '未处理').map((alarm, i) => {
                      const positions = [
                        { x: 30, y: 100 },
                        { x: 370, y: 200 },
                        { x: 200, y: 30 },
                      ];
                      return (
                        <g key={alarm.id}>
                          <circle 
                            cx={positions[i % 3].x} 
                            cy={positions[i % 3].y} 
                            r="15" 
                            fill="none" 
                            stroke="#EF4444" 
                            strokeWidth="2"
                            className="animate-ping"
                          />
                          <circle 
                            cx={positions[i % 3].x} 
                            cy={positions[i % 3].y} 
                            r="8" 
                            fill="#EF4444"
                          />
                        </g>
                      );
})}
                  </svg>
                  
                  {/* 图例 */}
                  <div className="absolute bottom-4 left-4 bg-[#111827]/90 border border-[#1F2937] rounded-lg p-3">
                    <div className="grid grid-cols-2 gap-x-4 gap-y-1 text-xs">
                      <div className="flex items-center gap-2">
                        <div className="w-3 h-3 bg-blue-500 rounded-full" />
                        <span className="text-gray-400">在线设备</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <div className="w-3 h-3 bg-red-500 rounded-full" />
                        <span className="text-gray-400">离线设备</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <div className="w-3 h-3 bg-red-500 rounded-full animate-ping" />
                        <span className="text-gray-400">入侵告警</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <div className="w-3 h-3 border-2 border-blue-500" />
                        <span className="text-gray-400">周界围栏</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              {/* 防区列表 */}
              <div className="data-card mt-4">
                <div className="flex items-center justify-between mb-3">
                  <h3 className="text-sm font-medium text-white">防区管理</h3>
                  <button className="px-3 py-1 bg-green-500/20 text-green-400 rounded-lg text-sm hover:bg-green-500/30">
                    全局设防
                  </button>
                </div>
                <div className="grid grid-cols-5 gap-2">
                  {defenseZones.map((zone) => (
                    <div 
                      key={zone.id}
                      className={`p-3 rounded-lg cursor-pointer transition-colors ${
                        selectedZone === zone.id ? 'ring-2 ring-blue-500' : ''
                      } ${
                        zone.status === 'armed' 
                          ? 'bg-green-500/20 border border-green-500/30' 
                          : 'bg-[#1F2937]/50 border border-[#374151]'
                      }`}
                      onClick={() => setSelectedZone(zone.id)}
                    >
                      <div className="flex items-center justify-between mb-2">
                        <span className="text-sm text-white">{zone.name}</span>
                        {zone.status === 'armed' ? (
                          <Lock className="w-4 h-4 text-green-400" />
                        ) : (
                          <Unlock className="w-4 h-4 text-gray-400" />
                        )}
                      </div>
                      <div className="flex items-center justify-between text-xs">
                        <span className="text-gray-400">{zone.devices}设备</span>
                        <span className={`${zone.alarms > 0 ? 'text-red-400' : 'text-gray-400'}`}>
                          {zone.alarms > 0 ? `${zone.alarms}告警` : '正常'}
                        </span>
                      </div>
                      <button 
                        onClick={(e) => { e.stopPropagation(); handleZoneToggle(zone.id); }}
                        className={`w-full mt-2 py-1 rounded text-xs font-medium ${
                          zone.status === 'armed' 
                            ? 'bg-red-500/20 text-red-400 hover:bg-red-500/30' 
                            : 'bg-green-500/20 text-green-400 hover:bg-green-500/30'
                        }`}
                      >
                        {zone.status === 'armed' ? '撤防' : '设防'}
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            </>
          )}

          {activeTab === 'intrusion' && (
            <div className="data-card flex-1">
              <h3 className="text-sm font-medium text-white mb-4">入侵告警记录</h3>
              <div className="space-y-2">
                {intrusionRecords.map((record) => (
                  <div 
                    key={record.id}
                    className={`p-4 rounded-lg border ${
                      record.status === '未处理' 
                        ? 'bg-red-500/10 border-red-500/30' 
                        : 'bg-[#1F2937]/50 border-[#374151]'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${
                          record.level === 'emergency' ? 'bg-red-500/20' : 'bg-yellow-500/20'
                        }`}>
                          <AlertTriangle className={`w-5 h-5 ${
                            record.level === 'emergency' ? 'text-red-400' : 'text-yellow-400'
                          }`} />
                        </div>
                        <div>
                          <p className="text-white font-medium">{record.type}</p>
                          <p className="text-sm text-gray-400">
                            位置: {record.location} | 防区: {defenseZones.find(z => z.id === record.zone)?.name}
                          </p>
                        </div>
                      </div>
                      <div className="flex items-center gap-3">
                        <span className="text-sm text-gray-400">{record.time}</span>
                        <span className={`px-3 py-1 rounded text-sm ${
                          record.status === '未处理' 
                            ? 'bg-red-500/20 text-red-400' 
                            : 'bg-green-500/20 text-green-400'
                        }`}>
                          {record.status}
                        </span>
                        <button className="px-3 py-1 bg-blue-500/20 text-blue-400 rounded hover:bg-blue-500/30">
                          处理
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {activeTab === 'behavior' && (
            <div className="data-card flex-1">
              <h3 className="text-sm font-medium text-white mb-4">行为分析告警</h3>
              <div className="grid grid-cols-2 gap-4 mb-4">
                <div className="p-4 bg-[#1F2937]/50 rounded-lg">
                  <p className="text-sm text-gray-400">异常聚集检测</p>
                  <p className="text-2xl font-bold text-yellow-400">{behaviorAlarms.filter(b => b.type === '异常聚集').length}</p>
                </div>
                <div className="p-4 bg-[#1F2937]/50 rounded-lg">
                  <p className="text-sm text-gray-400">奔跑检测</p>
                  <p className="text-2xl font-bold text-blue-400">{behaviorAlarms.filter(b => b.type === '奔跑检测').length}</p>
                </div>
                <div className="p-4 bg-[#1F2937]/50 rounded-lg">
                  <p className="text-sm text-gray-400">跌倒检测</p>
                  <p className="text-2xl font-bold text-red-400">{behaviorAlarms.filter(b => b.type === '跌倒检测').length}</p>
                </div>
                <div className="p-4 bg-[#1F2937]/50 rounded-lg">
                  <p className="text-sm text-gray-400">越界检测</p>
                  <p className="text-2xl font-bold text-purple-400">{intrusionRecords.length}</p>
                </div>
              </div>
              <div className="space-y-2">
                {behaviorAlarms.map((alarm) => (
                  <div key={alarm.id} className="flex items-center justify-between p-3 bg-[#1F2937]/50 rounded-lg">
                    <div className="flex items-center gap-3">
                      <span className="text-2xl">{
                        alarm.type === '异常聚集' ? '👥' :
                        alarm.type === '奔跑检测' ? '🏃' :
                        '⚠️'
                      }</span>
                      <div>
                        <p className="text-white">{alarm.type}</p>
                        <p className="text-sm text-gray-400">{alarm.location} | {alarm.count && `${alarm.count}人`}</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-3">
                      <span className="text-sm text-gray-400">{alarm.time}</span>
                      <span className={`px-2 py-1 rounded text-xs ${
                        alarm.status === '已处理' ? 'bg-green-500/20 text-green-400' : 'bg-yellow-500/20 text-yellow-400'
                      }`}>
                        {alarm.status}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {activeTab === 'trajectory' && (
            <div className="data-card flex-1">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-sm font-medium text-white">轨迹追踪</h3>
                <button 
                  onClick={() => setShowTrajectory(true)}
                  className="px-3 py-1 bg-blue-500/20 text-blue-400 rounded-lg text-sm hover:bg-blue-500/30"
                >
                  选择追踪对象
                </button>
              </div>
              <div className="space-y-2">
                {trajectoryRecords.map((record) => (
                  <div 
                    key={record.id}
                    onClick={() => setSelectedTrajectory(record)}
                    className="p-4 bg-[#1F2937]/50 rounded-lg cursor-pointer hover:bg-[#1F2937] transition-colors"
                  >
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center gap-2">
                        <span className="text-2xl">{record.type === 'person' ? '👤' : '🚗'}</span>
                        <div>
                          <p className="text-white font-medium">{record.subject}</p>
                          <p className="text-sm text-gray-400">类型: {record.type === 'person' ? '人员' : '车辆'}</p>
                        </div>
                      </div>
                      <button className="px-3 py-1 bg-blue-500/20 text-blue-400 rounded text-sm hover:bg-blue-500/30">
                        查看轨迹
                      </button>
                    </div>
                    <div className="flex items-center gap-2 text-sm text-gray-400">
                      <span>{record.startTime}</span>
                      <span>至</span>
                      <span>{record.endTime}</span>
                    </div>
                    <div className="flex items-center gap-1 mt-2">
                      {record.path.map((p, i) => (
                        <span key={i} className="px-2 py-0.5 bg-[#374151] rounded text-xs text-gray-300">
                          {p}
                          {i < record.path.length - 1 && <span className="ml-1 text-gray-500">→</span>}
                        </span>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {activeTab === 'patrol' && (
            <div className="data-card flex-1">
              <h3 className="text-sm font-medium text-white mb-4">智能巡检任务</h3>
              <div className="grid grid-cols-3 gap-4 mb-4">
                <div className="p-4 bg-green-500/10 border border-green-500/30 rounded-lg text-center">
                  <p className="text-sm text-gray-400 mb-1">巡检中</p>
                  <p className="text-2xl font-bold text-green-400">2</p>
                </div>
                <div className="p-4 bg-blue-500/10 border border-blue-500/30 rounded-lg text-center">
                  <p className="text-sm text-gray-400 mb-1">待执行</p>
                  <p className="text-2xl font-bold text-blue-400">5</p>
                </div>
                <div className="p-4 bg-gray-500/10 border border-gray-500/30 rounded-lg text-center">
                  <p className="text-sm text-gray-400 mb-1">已完成</p>
                  <p className="text-2xl font-bold text-gray-400">12</p>
                </div>
              </div>
              <div className="space-y-2">
                {[
                  { name: '周界巡检-01', type: '定时任务', status: 'running', progress: 65, area: '周界防护区' },
                  { name: '生产区巡检-01', type: '人工触发', status: 'pending', progress: 0, area: '生产防护区' },
                  { name: '夜间巡检-01', type: '定时任务', status: 'pending', progress: 0, area: '全域' },
                ].map((task, i) => (
                  <div key={i} className="p-4 bg-[#1F2937]/50 rounded-lg">
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center gap-2">
                        <Play className={`w-4 h-4 ${task.status === 'running' ? 'text-green-400' : 'text-gray-400'}`} />
                        <span className="text-white">{task.name}</span>
                        <span className="px-2 py-0.5 bg-blue-500/20 text-blue-400 text-xs rounded">{task.type}</span>
                      </div>
                      <span className={`px-2 py-1 rounded text-xs ${
                        task.status === 'running' ? 'bg-green-500/20 text-green-400' :
                        task.status === 'pending' ? 'bg-yellow-500/20 text-yellow-400' :
                        'bg-gray-500/20 text-gray-400'
                      }`}>
                        {task.status === 'running' ? '进行中' : task.status === 'pending' ? '待执行' : '已完成'}
                      </span>
                    </div>
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-gray-400">巡检区域: {task.area}</span>
                      {task.status === 'running' && (
                        <div className="flex items-center gap-2">
                          <div className="w-32 h-2 bg-[#374151] rounded-full overflow-hidden">
                            <div className="h-full bg-green-500" style={{ width: `${task.progress}%` }} />
                          </div>
                          <span className="text-gray-400">{task.progress}%</span>
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {activeTab === 'situation' && (
            <div className="data-card flex-1">
              <h3 className="text-sm font-medium text-white mb-4">安全态势感知</h3>
              
              {/* 总体评分 */}
              <div className="flex items-center gap-6 mb-6 p-4 bg-gradient-to-r from-blue-500/10 to-purple-500/10 rounded-lg">
                <div className="relative w-24 h-24">
                  <svg className="w-24 h-24 transform -rotate-90">
                    <circle cx="48" cy="48" r="40" stroke="#374151" strokeWidth="8" fill="none" />
                    <circle 
                      cx="48" 
                      cy="48" 
                      r="40" 
                      stroke="#3B82F6" 
                      strokeWidth="8" 
                      fill="none"
                      strokeDasharray={`${securitySituation.overallScore * 2.51} 251`}
                      strokeLinecap="round"
                    />
                  </svg>
                  <div className="absolute inset-0 flex items-center justify-center">
                    <span className="text-2xl font-bold text-white">{securitySituation.overallScore}</span>
                  </div>
                </div>
                <div>
                  <p className="text-lg text-white font-medium">全域安全态势评分</p>
                  <p className="text-sm text-gray-400">趋势: <span className="text-green-400">{securitySituation.trend}</span></p>
                  <p className="text-sm text-gray-400 mt-2">{securitySituation.prediction}</p>
                </div>
              </div>

              {/* 各项评分 */}
              <div className="grid grid-cols-5 gap-3 mb-4">
                {securitySituation.factors.map((factor, i) => (
                  <div key={i} className="p-3 bg-[#1F2937]/50 rounded-lg text-center">
                    <p className="text-sm text-gray-400 mb-1">{factor.name}</p>
                    <p className="text-xl font-bold text-white">{factor.score}</p>
                    <div className="w-full h-1 bg-[#374151] rounded-full mt-2 overflow-hidden">
                      <div className="h-full bg-blue-500" style={{ width: `${factor.score}%` }} />
                    </div>
                  </div>
                ))}
              </div>

              {/* 安全预测 */}
              <div className="p-4 bg-green-500/10 border border-green-500/30 rounded-lg">
                <div className="flex items-center gap-2 mb-2">
                  <Activity className="w-5 h-5 text-green-400" />
                  <span className="text-white font-medium">AI安全预测</span>
                </div>
                <p className="text-sm text-gray-300">{securitySituation.prediction}</p>
                <div className="flex items-center gap-4 mt-3 text-sm">
                  <span className="text-gray-400">周界防护: <span className="text-green-400">稳定</span></span>
                  <span className="text-gray-400">区域管控: <span className="text-green-400">正常</span></span>
                  <span className="text-gray-400">异常事件: <span className="text-yellow-400">1起</span></span>
                </div>
              </div>
            </div>
          )}
        </div>
        
        {/* 右侧设备列表 */}
        <div className="w-80 flex flex-col gap-4">
          <div className="data-card flex-1 flex flex-col">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-sm font-medium text-white">云防设备</h3>
              <button className="p-1 bg-[#1F2937] rounded text-gray-400 hover:text-white">
                <Settings className="w-4 h-4" />
              </button>
            </div>
            
            <div className="flex-1 overflow-y-auto space-y-2">
              {cloudDefenseDevices.map((device) => (
                <div 
                  key={device.id}
                  onClick={() => setSelectedDevice(device.id)}
                  className={`p-3 rounded-lg cursor-pointer transition-colors ${
                    selectedDevice === device.id 
                      ? 'bg-blue-500/20 border border-blue-500/50' 
                      : 'bg-[#1F2937]/50 hover:bg-[#1F2937]'
                  }`}
                >
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <div className={`w-8 h-8 rounded-lg flex items-center justify-center ${
                        device.status === 'online' ? 'bg-blue-500/20' : 'bg-red-500/20'
                      }`}>
                        {device.type === '球机' && <Video className={`w-4 h-4 ${device.status === 'online' ? 'text-blue-400' : 'text-red-400'}`} />}
                        {device.type === '全景' && <Eye className={`w-4 h-4 ${device.status === 'online' ? 'text-blue-400' : 'text-red-400'}`} />}
                        {device.type === '热成像' && <AlertTriangle className={`w-4 h-4 ${device.status === 'online' ? 'text-blue-400' : 'text-red-400'}`} />}
                        {device.type === '人脸识别' && <User className={`w-4 h-4 ${device.status === 'online' ? 'text-blue-400' : 'text-red-400'}`} />}
                        {device.type === '车牌识别' && <Car className={`w-4 h-4 ${device.status === 'online' ? 'text-blue-400' : 'text-red-400'}`} />}
                      </div>
                      <div>
                        <p className="text-sm text-white">{device.name}</p>
                        <p className="text-xs text-gray-500">{device.type}</p>
                      </div>
                    </div>
                    <span className={`px-2 py-0.5 text-xs rounded ${
                      device.status === 'online' ? 'bg-green-500/20 text-green-400' : 'bg-red-500/20 text-red-400'
                    }`}>
                      {device.status === 'online' ? '在线' : '离线'}
                    </span>
                  </div>
                  <div className="flex items-center gap-2 text-xs text-gray-500">
                    <MapPin className="w-3 h-3" />
                    <span>{device.location}</span>
                    {device.ptz && (
                      <span className="px-1 py-0.5 bg-purple-500/20 text-purple-400 rounded">可PTZ</span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
          
          {/* 实时报警 */}
          <div className="data-card">
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-sm font-medium text-white">实时报警</h3>
              <span className="px-2 py-0.5 bg-red-500/20 text-red-400 text-xs rounded">
                {intrusionRecords.filter(r => r.status === '未处理').length} 条
              </span>
            </div>
            <div className="space-y-2">
              {intrusionRecords.filter(r => r.status === '未处理').map((alarm) => (
                <div key={alarm.id} className="p-2 bg-red-500/10 border border-red-500/30 rounded-lg">
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-red-400">{alarm.type}</span>
                    <span className="text-xs text-gray-500">{alarm.time}</span>
                  </div>
                  <p className="text-xs text-gray-400">{alarm.location}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
