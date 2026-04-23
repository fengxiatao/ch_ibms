import { Zap, Droplets, Flame, TrendingUp, TrendingDown, Calendar, Download } from 'lucide-react';
import { useState } from 'react';
import { LineChart } from '@/components/charts/Charts';
import { energyTrendData, energyStats } from '@/data/mockData';

export function EnergyManagement() {
  const [timeRange, setTimeRange] = useState<'day' | 'week' | 'month'>('day');
  const [activeTab, setActiveTab] = useState<'overview' | 'electricity' | 'water' | 'gas'>('overview');

  return (
    <div className="h-full flex flex-col p-4 gap-4 overflow-auto">
      {/* 顶部统计 */}
      <div className="grid grid-cols-4 gap-4">
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-yellow-500/20 rounded-lg flex items-center justify-center">
            <Zap className="w-6 h-6 text-yellow-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">今日用电</p>
            <p className="text-xl font-bold text-white">{energyStats.todayElectricity.toLocaleString()} kWh</p>
          </div>
        </div>
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-blue-500/20 rounded-lg flex items-center justify-center">
            <Droplets className="w-6 h-6 text-blue-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">今日用水</p>
            <p className="text-xl font-bold text-white">{energyStats.todayWater} m³</p>
          </div>
        </div>
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-orange-500/20 rounded-lg flex items-center justify-center">
            <Flame className="w-6 h-6 text-orange-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">今日用气</p>
            <p className="text-xl font-bold text-white">{energyStats.todayGas} m³</p>
          </div>
        </div>
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-green-500/20 rounded-lg flex items-center justify-center">
            {energyStats.compareLastMonth < 0 ? (
              <TrendingDown className="w-6 h-6 text-green-400" />
            ) : (
              <TrendingUp className="w-6 h-6 text-red-400" />
            )}
          </div>
          <div>
            <p className="text-xs text-gray-400">同比节能</p>
            <p className={`text-xl font-bold ${energyStats.compareLastMonth < 0 ? 'text-green-400' : 'text-red-400'}`}>
              {energyStats.compareLastMonth > 0 ? '+' : ''}{energyStats.compareLastMonth}%
            </p>
          </div>
        </div>
      </div>

      {/* 标签页和筛选 */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          {['overview', 'electricity', 'water', 'gas'].map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab as any)}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                activeTab === tab ? 'bg-blue-500 text-white' : 'bg-[#1F2937] text-gray-400 hover:text-white'
              }`}
            >
              {tab === 'overview' ? '综合概览' :
               tab === 'electricity' ? '电力' :
               tab === 'water' ? '水' : '气'}
            </button>
          ))}
        </div>
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-1 bg-[#1F2937] rounded-lg p-1">
            {(['day', 'week', 'month'] as const).map((range) => (
              <button
                key={range}
                onClick={() => setTimeRange(range)}
                className={`px-3 py-1 rounded text-sm ${
                  timeRange === range ? 'bg-blue-500 text-white' : 'text-gray-400 hover:text-white'
                }`}
              >
                {range === 'day' ? '日' : range === 'week' ? '周' : '月'}
              </button>
            ))}
          </div>
          <button className="px-4 py-2 bg-[#1F2937] rounded-lg text-sm text-gray-400 hover:text-white flex items-center gap-2">
            <Calendar className="w-4 h-4" />
            选择日期
          </button>
          <button className="px-4 py-2 bg-blue-500 text-white rounded-lg text-sm hover:bg-blue-600 flex items-center gap-2">
            <Download className="w-4 h-4" />
            导出报表
          </button>
        </div>
      </div>

      {/* 能耗趋势图 */}
      <div className="data-card flex-1">
        <h3 className="text-sm font-medium text-white mb-4">能耗趋势</h3>
        <LineChart
          data={energyTrendData}
          lines={
            activeTab === 'overview' ? [
              { key: 'electricity', name: '电力(kWh)', color: '#EAB308' },
              { key: 'water', name: '水(m³)', color: '#3B82F6' },
              { key: 'gas', name: '气(m³)', color: '#F97316' },
            ] :
            activeTab === 'electricity' ? [
              { key: 'electricity', name: '电力(kWh)', color: '#EAB308' },
            ] :
            activeTab === 'water' ? [
              { key: 'water', name: '水(m³)', color: '#3B82F6' },
            ] : [
              { key: 'gas', name: '气(m³)', color: '#F97316' },
            ]
          }
          xAxisKey="time"
          height={350}
        />
      </div>

      {/* 区域能耗对比 */}
      <div className="grid grid-cols-3 gap-4">
        <div className="data-card">
          <h4 className="text-sm font-medium text-white mb-3">区域用电排名</h4>
          <div className="space-y-2">
            {[
              { name: '生产车间A', value: 850, percent: 35 },
              { name: '生产车间B', value: 720, percent: 29 },
              { name: '办公区', value: 380, percent: 15 },
              { name: '仓储区', value: 280, percent: 11 },
              { name: '食堂', value: 220, percent: 9 },
            ].map((item, i) => (
              <div key={i} className="flex items-center gap-3">
                <span className="w-6 text-sm text-gray-400">{i + 1}</span>
                <div className="flex-1">
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-sm text-white">{item.name}</span>
                    <span className="text-sm text-gray-400">{item.value} kWh</span>
                  </div>
                  <div className="h-2 bg-[#374151] rounded-full overflow-hidden">
                    <div 
                      className="h-full bg-yellow-500 rounded-full"
                      style={{ width: `${item.percent}%` }}
                    />
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="data-card">
          <h4 className="text-sm font-medium text-white mb-3">设备用电排名</h4>
          <div className="space-y-2">
            {[
              { name: '空压机-01', value: 420 },
              { name: '空调机组-01', value: 380 },
              { name: '乳化罐-01', value: 290 },
              { name: '灌装机-01', value: 250 },
              { name: '纯水系统-01', value: 180 },
            ].map((item, i) => (
              <div key={i} className="flex items-center gap-3 p-2 bg-[#1F2937]/50 rounded-lg">
                <span className="w-6 text-sm text-gray-400">{i + 1}</span>
                <span className="text-sm text-white flex-1">{item.name}</span>
                <span className="text-sm text-yellow-400">{item.value} kWh</span>
              </div>
            ))}
          </div>
        </div>

        <div className="data-card">
          <h4 className="text-sm font-medium text-white mb-3">节能建议</h4>
          <div className="space-y-3">
            <div className="p-3 bg-green-500/10 border border-green-500/30 rounded-lg">
              <div className="flex items-center gap-2 mb-1">
                <TrendingDown className="w-4 h-4 text-green-400" />
                <span className="text-sm text-green-400 font-medium">建议优化</span>
              </div>
              <p className="text-xs text-gray-400">空压机夜间运行效率偏低，建议配置变频控制，预计节能15%</p>
            </div>
            <div className="p-3 bg-blue-500/10 border border-blue-500/30 rounded-lg">
              <div className="flex items-center gap-2 mb-1">
                <Zap className="w-4 h-4 text-blue-400" />
                <span className="text-sm text-blue-400 font-medium">峰谷调节</span>
              </div>
              <p className="text-xs text-gray-400">非生产时段可错峰用电，避开9:00-12:00高峰期</p>
            </div>
            <div className="p-3 bg-yellow-500/10 border border-yellow-500/30 rounded-lg">
              <div className="flex items-center gap-2 mb-1">
                <Droplets className="w-4 h-4 text-yellow-400" />
                <span className="text-sm text-yellow-400 font-medium">用水优化</span>
              </div>
              <p className="text-xs text-gray-400">纯水系统产水率偏低，建议检查滤芯</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
