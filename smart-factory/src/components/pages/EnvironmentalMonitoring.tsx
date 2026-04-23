import { Wind, Droplets, Volume2, Factory, AlertTriangle, CheckCircle, Bell } from 'lucide-react';
import { environmentalData, environmentalAlerts } from '@/data/mockData';

export function EnvironmentalMonitoring() {
  return (
    <div className="h-full flex flex-col p-4 gap-4 overflow-auto">
      {/* 顶部统计 */}
      <div className="grid grid-cols-4 gap-4">
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-purple-500/20 rounded-lg flex items-center justify-center">
            <Wind className="w-6 h-6 text-purple-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">VOCs浓度</p>
            <p className="text-xl font-bold text-white">{environmentalData.vocs.current} {environmentalData.vocs.unit}</p>
          </div>
        </div>
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-blue-500/20 rounded-lg flex items-center justify-center">
            <Droplets className="w-6 h-6 text-blue-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">废水COD</p>
            <p className="text-xl font-bold text-white">{environmentalData.wastewater.cod} mg/L</p>
          </div>
        </div>
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-green-500/20 rounded-lg flex items-center justify-center">
            <Volume2 className="w-6 h-6 text-green-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">噪声等级</p>
            <p className="text-xl font-bold text-white">{environmentalData.noise.current} {environmentalData.noise.unit}</p>
          </div>
        </div>
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-orange-500/20 rounded-lg flex items-center justify-center">
            <Factory className="w-6 h-6 text-orange-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">排气流量</p>
            <p className="text-xl font-bold text-white">{environmentalData.exhaust.current} {environmentalData.exhaust.unit}</p>
          </div>
        </div>
      </div>

      {/* 废气监测 */}
      <div className="data-card">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-sm font-medium text-white flex items-center gap-2">
            <Wind className="w-4 h-4 text-purple-400" />
            废气排放监测
          </h3>
          <span className="px-2 py-1 bg-green-500/20 text-green-400 text-xs rounded flex items-center gap-1">
            <CheckCircle className="w-3 h-3" />
            达标排放
          </span>
        </div>
        <div className="grid grid-cols-4 gap-4">
          {[
            { name: 'VOCs', value: environmentalData.vocs.current, limit: environmentalData.vocs.limit, unit: environmentalData.vocs.unit },
          ].map((item, i) => (
            <div key={i} className="p-4 bg-[#1F2937]/50 rounded-lg">
              <div className="flex items-center justify-between mb-2">
                <span className="text-white font-medium">{item.name}</span>
                <span className={`px-2 py-0.5 rounded text-xs ${
                  item.value < item.limit * 0.8 ? 'bg-green-500/20 text-green-400' :
                  item.value < item.limit ? 'bg-yellow-500/20 text-yellow-400' :
                  'bg-red-500/20 text-red-400'
                }`}>
                  {item.value < item.limit * 0.8 ? '正常' : item.value < item.limit ? '预警' : '超标'}
                </span>
              </div>
              <div className="flex items-center gap-2 mb-2">
                <span className="text-2xl font-bold text-white">{item.value}</span>
                <span className="text-sm text-gray-400">{item.unit}</span>
              </div>
              <div className="relative h-3 bg-[#374151] rounded-full overflow-hidden">
                <div 
                  className={`absolute h-full rounded-full ${
                    item.value / item.limit > 0.8 ? 'bg-red-500' :
                    item.value / item.limit > 0.6 ? 'bg-yellow-500' : 'bg-green-500'
                  }`}
                  style={{ width: `${Math.min((item.value / item.limit) * 100, 100)}%` }}
                />
                <div 
                  className="absolute top-0 w-0.5 h-full bg-white"
                  style={{ left: `${(item.limit / (item.limit * 1.2)) * 100}%` }}
                />
              </div>
              <div className="flex justify-between text-xs text-gray-500 mt-1">
                <span>0</span>
                <span>限值: {item.limit}</span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 废水监测 */}
      <div className="data-card">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-sm font-medium text-white flex items-center gap-2">
            <Droplets className="w-4 h-4 text-blue-400" />
            废水排放监测
          </h3>
          <span className="px-2 py-1 bg-green-500/20 text-green-400 text-xs rounded flex items-center gap-1">
            <CheckCircle className="w-3 h-3" />
            达标排放
          </span>
        </div>
        <div className="grid grid-cols-4 gap-4">
          <div className="p-4 bg-[#1F2937]/50 rounded-lg text-center">
            <p className="text-xs text-gray-400 mb-2">COD</p>
            <p className="text-2xl font-bold text-white">{environmentalData.wastewater.cod}</p>
            <p className="text-xs text-gray-500">mg/L</p>
            <div className="w-full h-2 bg-[#374151] rounded-full mt-2 overflow-hidden">
              <div className="h-full bg-green-500 rounded-full" style={{ width: '45%' }} />
            </div>
          </div>
          <div className="p-4 bg-[#1F2937]/50 rounded-lg text-center">
            <p className="text-xs text-gray-400 mb-2">氨氮</p>
            <p className="text-2xl font-bold text-white">{environmentalData.wastewater.ammonia}</p>
            <p className="text-xs text-gray-500">mg/L</p>
            <div className="w-full h-2 bg-[#374151] rounded-full mt-2 overflow-hidden">
              <div className="h-full bg-green-500 rounded-full" style={{ width: '57%' }} />
            </div>
          </div>
          <div className="p-4 bg-[#1F2937]/50 rounded-lg text-center">
            <p className="text-xs text-gray-400 mb-2">pH值</p>
            <p className="text-2xl font-bold text-white">{environmentalData.wastewater.ph}</p>
            <p className="text-xs text-gray-500">6-9范围内</p>
            <div className="w-full h-2 bg-[#374151] rounded-full mt-2 overflow-hidden">
              <div className="h-full bg-green-500 rounded-full" style={{ width: '60%' }} />
            </div>
          </div>
          <div className="p-4 bg-[#1F2937]/50 rounded-lg text-center">
            <p className="text-xs text-gray-400 mb-2">流量</p>
            <p className="text-2xl font-bold text-white">{environmentalData.wastewater.flow}</p>
            <p className="text-xs text-gray-500">m³/h</p>
            <div className="w-full h-2 bg-[#374151] rounded-full mt-2 overflow-hidden">
              <div className="h-full bg-blue-500 rounded-full" style={{ width: '60%' }} />
            </div>
          </div>
        </div>
      </div>

      {/* 噪声监测 */}
      <div className="grid grid-cols-2 gap-4">
        <div className="data-card">
          <h3 className="text-sm font-medium text-white mb-4 flex items-center gap-2">
            <Volume2 className="w-4 h-4 text-green-400" />
            噪声监测
          </h3>
          <div className="flex items-center justify-around">
            <div className="text-center">
              <div className="relative w-20 h-20">
                <svg className="w-20 h-20 transform -rotate-90">
                  <circle cx="40" cy="40" r="35" stroke="#374151" strokeWidth="6" fill="none" />
                  <circle 
                    cx="40" 
                    cy="40" 
                    r="35" 
                    stroke="#22C55E" 
                    strokeWidth="6" 
                    fill="none"
                    strokeDasharray={`${(environmentalData.noise.current / environmentalData.noise.limit) * 220} 220`}
                    strokeLinecap="round"
                  />
                </svg>
                <div className="absolute inset-0 flex items-center justify-center">
                  <span className="text-xl font-bold text-white">{environmentalData.noise.current}</span>
                </div>
              </div>
              <p className="text-sm text-gray-400 mt-2">昼间 dB</p>
            </div>
            <div className="text-center">
              <div className="relative w-20 h-20">
                <svg className="w-20 h-20 transform -rotate-90">
                  <circle cx="40" cy="40" r="35" stroke="#374151" strokeWidth="6" fill="none" />
                  <circle 
                    cx="40" 
                    cy="40" 
                    r="35" 
                    stroke="#EAB308" 
                    strokeWidth="6" 
                    fill="none"
                    strokeDasharray={`${(52 / environmentalData.noise.limit) * 220} 220`}
                    strokeLinecap="round"
                  />
                </svg>
                <div className="absolute inset-0 flex items-center justify-center">
                  <span className="text-xl font-bold text-yellow-400">52</span>
                </div>
              </div>
              <p className="text-sm text-gray-400 mt-2">夜间 dB</p>
            </div>
            <div className="text-center">
              <div className="w-20 h-20 bg-[#1F2937] rounded-full flex items-center justify-center">
                <span className="text-2xl font-bold text-white">{environmentalData.noise.limit}</span>
              </div>
              <p className="text-sm text-gray-400 mt-2">限值 dB</p>
            </div>
          </div>
        </div>

        <div className="data-card">
          <h3 className="text-sm font-medium text-white mb-4 flex items-center gap-2">
            <Bell className="w-4 h-4 text-yellow-400" />
            环保预警
          </h3>
          <div className="space-y-2">
            {environmentalAlerts.map((alert) => (
              <div 
                key={alert.id}
                className={`p-3 rounded-lg ${
                  alert.status === '预警' ? 'bg-yellow-500/10 border border-yellow-500/30' : 'bg-green-500/10 border border-green-500/30'
                }`}
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <AlertTriangle className={`w-4 h-4 ${alert.status === '预警' ? 'text-yellow-400' : 'text-green-400'}`} />
                    <span className="text-white font-medium">{alert.type} - {alert.location}</span>
                  </div>
                  <span className="text-xs text-gray-400">{alert.time}</span>
                </div>
                <p className="text-sm text-gray-400 mt-1">
                  当前值: {alert.value} {alert.type === 'VOCs' ? 'mg/m³' : 'mg/L'} | 限值: {alert.limit}
                </p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
