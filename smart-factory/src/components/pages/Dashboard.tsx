import { 
  Factory, 
  TrendingUp, 
  Zap, 
  Thermometer, 
  AlertTriangle, 
  Video,
  Layers,
  Wifi,
  Activity,
  Monitor,
  RotateCcw
} from 'lucide-react';
import { GaugeChart } from '@/components/charts/GaugeChart';
import { LineChart } from '@/components/charts/Charts';
import { 
  dashboardMetrics, 
  realTimeAlarms, 
  energyTrendData,
  equipmentList,
  floorData 
} from '@/data/mockData';
import { Factory3D } from './Factory3D';

export function Dashboard() {
  return (
    <div className="h-full flex flex-col gap-4 p-4 overflow-auto">
      {/* 核心指标区 - 修改版 */}
      <div className="grid grid-cols-4 gap-4">
        {/* 设备在线率 - 新增 */}
        <div className="data-card flex items-center gap-4">
          <div className="w-[100px] h-[100px] rounded-full bg-blue-500/20 flex items-center justify-center relative">
            <Wifi className="w-10 h-10 text-blue-400" />
            <div className="absolute -bottom-1 -right-1 w-6 h-6 bg-green-500 rounded-full flex items-center justify-center">
              <span className="text-white text-xs font-bold">{dashboardMetrics.equipmentOnlineRate.value}</span>
            </div>
          </div>
          <div>
            <p className="text-sm text-gray-400">设备在线率</p>
            <p className="text-2xl font-bold text-white">{dashboardMetrics.equipmentOnlineRate.value}%</p>
            <p className="text-xs text-green-400">{dashboardMetrics.equipmentOnlineRate.trend} 较昨日</p>
          </div>
        </div>
        
        {/* 告警数 - 新增 */}
        <div className="data-card flex items-center gap-4">
          <div className="w-[100px] h-[100px] rounded-full bg-red-500/20 flex items-center justify-center relative">
            <AlertTriangle className="w-10 h-10 text-red-400" />
            <div className="absolute -bottom-1 -right-1 w-6 h-6 bg-red-500 rounded-full flex items-center justify-center">
              <span className="text-white text-xs font-bold">{dashboardMetrics.alarmCount.value}</span>
            </div>
          </div>
          <div>
            <p className="text-sm text-gray-400">告警数</p>
            <p className="text-2xl font-bold text-white">{dashboardMetrics.alarmCount.value} 条</p>
            <p className="text-xs text-green-400">{dashboardMetrics.alarmCount.trend} 较昨日</p>
          </div>
        </div>
        
        {/* 环境合格率 - 保留 */}
        <div className="data-card flex items-center gap-4">
          <GaugeChart 
            value={dashboardMetrics.environmentCompliance.value} 
            color="#10B981"
            size={100}
          />
          <div>
            <p className="text-sm text-gray-400">环境合格率</p>
            <p className="text-2xl font-bold text-white">{dashboardMetrics.environmentCompliance.value}%</p>
            <p className="text-xs text-green-400">{dashboardMetrics.environmentCompliance.trend}</p>
          </div>
        </div>
        
        {/* 能耗监测 - 保留 */}
        <div className="data-card flex items-center gap-4">
          <div className="w-[100px] h-[100px] rounded-full bg-green-500/20 flex items-center justify-center">
            <Zap className="w-10 h-10 text-green-400" />
          </div>
          <div>
            <p className="text-sm text-gray-400">今日能耗</p>
            <p className="text-2xl font-bold text-white">2,450 kWh</p>
            <p className="text-xs text-gray-500">同比 -5.2%</p>
          </div>
        </div>
      </div>
      
      {/* 主要内容区 */}
      <div className="flex-1 grid grid-cols-12 gap-4 min-h-0">
        {/* 左侧面板 */}
        <div className="col-span-3 flex flex-col gap-4">
          {/* 楼层切换 */}
          <div className="data-card">
            <div className="flex items-center gap-2 mb-3">
              <Layers className="w-4 h-4 text-blue-400" />
              <h3 className="text-sm font-medium text-white">楼层切换</h3>
            </div>
            <div className="grid grid-cols-2 gap-2">
              {floorData.map((floor, i) => (
                <button
                  key={floor.id}
                  className={`py-2 px-3 rounded-lg text-sm font-medium transition-colors flex items-center justify-center gap-1 ${
                    i === 0 
                      ? 'bg-blue-500 text-white' 
                      : 'bg-[#1F2937] text-gray-400 hover:text-white'
                  }`}
                >
                  {floor.id === 'all' && <Layers className="w-4 h-4" />}
                  {floor.id === '1f' && <Factory className="w-4 h-4" />}
                  {floor.id === '2f' && <Activity className="w-4 h-4" />}
                  {floor.id === '3f' && <Monitor className="w-4 h-4" />}
                  <span>{floor.name}</span>
                </button>
              ))}
            </div>
          </div>
          
          {/* 设备状态 */}
          <div className="data-card flex-1">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <Factory className="w-4 h-4 text-blue-400" />
                <h3 className="text-sm font-medium text-white">设备状态</h3>
              </div>
              <span className="px-2 py-1 bg-blue-500/20 text-blue-400 text-xs rounded-full">
                {equipmentList.filter(e => e.status === 'running').length}/{equipmentList.length}
              </span>
            </div>
            <div className="space-y-2 max-h-[180px] overflow-y-auto">
              {equipmentList.map((eq) => (
                <div key={eq.id} className="flex items-center justify-between p-2 rounded-lg bg-[#1F2937]/50 hover:bg-[#1F2937] transition-colors">
                  <div className="flex items-center gap-2">
                    <div className={`w-2 h-2 rounded-full ${
                      eq.status === 'running' ? 'bg-green-400' :
                      eq.status === 'fault' ? 'bg-red-400' :
                      eq.status === 'maintenance' ? 'bg-yellow-400' : 'bg-gray-400'
                    } ${eq.online ? 'status-pulse' : ''}`} />
                    <div>
                      <span className="text-sm text-white">{eq.name}</span>
                      <span className="text-xs text-gray-500 ml-2">{eq.location}</span>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    {eq.online && (
                      <span className="text-xs text-green-400">
                        <Wifi className="w-3 h-3" />
                      </span>
                    )}
                    <span className={`text-xs ${
                      eq.status === 'running' ? 'text-green-400' :
                      eq.status === 'fault' ? 'text-red-400' :
                      eq.status === 'maintenance' ? 'text-yellow-400' : 'text-gray-400'
                    }`}>
                      {eq.status === 'running' ? '运行' :
                       eq.status === 'fault' ? '故障' :
                       eq.status === 'maintenance' ? '维保' : '停机'}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
          
          {/* 告警事件 */}
          <div className="data-card flex-1">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <AlertTriangle className="w-4 h-4 text-red-400" />
                <h3 className="text-sm font-medium text-white">告警事件</h3>
              </div>
              <span className="px-2 py-1 bg-red-500/20 text-red-400 text-xs rounded-full">
                {realTimeAlarms.filter(a => a.status !== 'handled').length} 未处理
              </span>
            </div>
            <div className="space-y-2 max-h-[180px] overflow-y-auto">
              {realTimeAlarms.slice(0, 5).map((alarm) => (
                <div key={alarm.id} className="flex items-center justify-between p-2 rounded-lg bg-[#1F2937]/50 hover:bg-[#1F2937] transition-colors">
                  <div>
                    <p className="text-sm text-white">{alarm.title}</p>
                    <p className="text-xs text-gray-500">{alarm.location}</p>
                  </div>
                  <span className={`px-2 py-0.5 text-xs rounded ${
                    alarm.level === 'emergency' ? 'bg-red-500/20 text-red-400' :
                    alarm.level === 'important' ? 'bg-yellow-500/20 text-yellow-400' :
                    'bg-blue-500/20 text-blue-400'
                  }`}>
                    {alarm.status === 'unhandled' ? '未处理' :
                     alarm.status === 'handling' ? '处理中' : '已处理'}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
        
        {/* 中央3D场景区 */}
        <div className="col-span-6 flex flex-col">
          <div className="data-card flex-1 relative overflow-hidden">
            {/* 3D工厂模型 */}
            <Factory3D />
            
            {/* 底部工具栏 */}
            <div className="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2 bg-[#111827]/90 px-4 py-2 rounded-lg border border-[#1F2937]">
              <button className="px-3 py-1.5 text-gray-300 text-sm rounded-lg hover:bg-[#1F2937] transition-colors flex items-center gap-1">
                <RotateCcw className="w-4 h-4" />
                重置视角
              </button>
              <button className="px-3 py-1.5 text-gray-300 text-sm rounded-lg hover:bg-[#1F2937] transition-colors flex items-center gap-1">
                <Factory className="w-4 h-4" />
                设备定位
              </button>
              <button className="px-3 py-1.5 text-gray-300 text-sm rounded-lg hover:bg-[#1F2937] transition-colors flex items-center gap-1">
                <Activity className="w-4 h-4" />
                巡检漫游
              </button>
              <button className="px-3 py-1.5 text-gray-300 text-sm rounded-lg hover:bg-[#1F2937] transition-colors flex items-center gap-1">
                <Video className="w-4 h-4" />
                视频融合
              </button>
            </div>
          </div>
        </div>
        
        {/* 右侧面板 */}
        <div className="col-span-3 flex flex-col gap-4">
          {/* 实时视频 */}
          <div className="data-card">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <Video className="w-4 h-4 text-blue-400" />
                <h3 className="text-sm font-medium text-white">实时视频</h3>
              </div>
              <span className="text-xs text-green-400 flex items-center gap-1">
                <span className="w-2 h-2 bg-green-400 rounded-full status-pulse" />
                直播中
              </span>
            </div>
            <div className="aspect-video bg-gradient-to-br from-[#1F2937] to-[#111827] rounded-lg flex items-center justify-center relative overflow-hidden">
              <div className="absolute inset-0 flex items-center justify-center">
                <div className="w-16 h-16 border-2 border-blue-500/30 rounded-lg flex items-center justify-center">
                  <Video className="w-8 h-8 text-blue-400" />
                </div>
              </div>
              <div className="absolute bottom-2 left-2 bg-black/60 px-2 py-1 rounded text-xs text-white">
                CAM-001 | 车间A入口
              </div>
            </div>
          </div>
          
          {/* 能耗趋势 */}
          <div className="data-card flex-1">
            <div className="flex items-center gap-2 mb-3">
              <TrendingUp className="w-4 h-4 text-green-400" />
              <h3 className="text-sm font-medium text-white">能耗趋势</h3>
            </div>
            <LineChart 
              data={energyTrendData}
              lines={[
                { key: 'electricity', name: '电力', color: '#3B82F6' },
                { key: 'water', name: '水', color: '#06B6D4' },
              ]}
              xAxisKey="time"
              height={160}
            />
          </div>
          
          {/* 环境监测 */}
          <div className="data-card">
            <div className="flex items-center gap-2 mb-3">
              <Thermometer className="w-4 h-4 text-cyan-400" />
              <h3 className="text-sm font-medium text-white">环境监测</h3>
            </div>
            <div className="grid grid-cols-2 gap-2">
              <div className="p-3 bg-[#1F2937]/50 rounded-lg text-center">
                <p className="text-xs text-gray-400">温度</p>
                <p className="text-lg font-bold text-white">22°C</p>
              </div>
              <div className="p-3 bg-[#1F2937]/50 rounded-lg text-center">
                <p className="text-xs text-gray-400">湿度</p>
                <p className="text-lg font-bold text-white">55%</p>
              </div>
              <div className="p-3 bg-[#1F2937]/50 rounded-lg text-center">
                <p className="text-xs text-gray-400">压差</p>
                <p className="text-lg font-bold text-white">15Pa</p>
              </div>
              <div className="p-3 bg-[#1F2937]/50 rounded-lg text-center">
                <p className="text-xs text-gray-400">洁净度</p>
                <p className="text-lg font-bold text-white">Class 8</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
