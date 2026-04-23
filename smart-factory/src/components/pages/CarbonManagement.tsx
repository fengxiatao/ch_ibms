import { Leaf, TrendingUp, Factory, Target, Download, Calendar, TrendingDown } from 'lucide-react';
import { useState } from 'react';
import { LineChart } from '@/components/charts/Charts';
import { carbonData } from '@/data/mockData';

export function CarbonManagement() {
  const [activeTab, setActiveTab] = useState<'overview' | 'source' | 'trade'>('overview');

  return (
    <div className="h-full flex flex-col p-4 gap-4 overflow-auto">
      {/* Top Stats */}
      <div className="grid grid-cols-4 gap-4">
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-green-500/20 rounded-lg flex items-center justify-center">
            <Leaf className="w-6 h-6 text-green-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">Carbon Emissions (This Month)</p>
            <p className="text-xl font-bold text-white">{carbonData.totalEmission.toLocaleString()} tCO2</p>
          </div>
        </div>
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-blue-500/20 rounded-lg flex items-center justify-center">
            <Factory className="w-6 h-6 text-blue-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">Unit Emission</p>
            <p className="text-xl font-bold text-blue-400">{carbonData.unitEmission} tCO2/t</p>
          </div>
        </div>
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-yellow-500/20 rounded-lg flex items-center justify-center">
            <Target className="w-6 h-6 text-yellow-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">Annual Target</p>
            <p className="text-xl font-bold text-yellow-400">{carbonData.target.toLocaleString()} tCO2</p>
          </div>
        </div>
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-purple-500/20 rounded-lg flex items-center justify-center">
            {carbonData.progress >= 50 ? (
              <TrendingDown className="w-6 h-6 text-green-400" />
            ) : (
              <TrendingUp className="w-6 h-6 text-yellow-400" />
            )}
          </div>
          <div>
            <p className="text-xs text-gray-400">Target Completion</p>
            <p className="text-xl font-bold text-purple-400">{carbonData.progress}%</p>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          {[
            { id: 'overview', label: 'Carbon Overview' },
            { id: 'source', label: 'Emission Sources' },
            { id: 'trade', label: 'Carbon Trading' },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as any)}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                activeTab === tab.id ? 'bg-blue-500 text-white' : 'bg-[#1F2937] text-gray-400 hover:text-white'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
        <div className="flex items-center gap-2">
          <button className="px-4 py-2 bg-[#1F2937] rounded-lg text-sm text-gray-400 hover:text-white flex items-center gap-2">
            <Calendar className="w-4 h-4" />
            Select Date
          </button>
          <button className="px-4 py-2 bg-blue-500 text-white rounded-lg text-sm hover:bg-blue-600 flex items-center gap-2">
            <Download className="w-4 h-4" />
            Export Report
          </button>
        </div>
      </div>

      {/* Carbon Overview */}
      {activeTab === 'overview' && (
        <>
          {/* Trend Chart */}
          <div className="data-card flex-1">
            <h3 className="text-sm font-medium text-white mb-4">Carbon Emission Trend</h3>
            <LineChart
              data={carbonData.trend}
              lines={[
                { key: 'emission', name: 'Emissions(tCO2)', color: '#22C55E' },
              ]}
              xAxisKey="month"
              height={300}
            />
          </div>

          {/* Target Progress */}
          <div className="data-card">
            <h4 className="text-sm font-medium text-white mb-4">Annual Target Progress</h4>
            <div className="flex items-center gap-6">
              <div className="relative w-32 h-32">
                <svg className="w-32 h-32 transform -rotate-90">
                  <circle cx="64" cy="64" r="56" stroke="#374151" strokeWidth="12" fill="none" />
                  <circle
                    cx="64"
                    cy="64"
                    r="56"
                    stroke="#22C55E"
                    strokeWidth="12"
                    fill="none"
                    strokeDasharray={`${(carbonData.totalEmission / carbonData.target) * 351.86} 351.86`}
                    strokeLinecap="round"
                  />
                </svg>
                <div className="absolute inset-0 flex items-center justify-center">
                  <span className="text-2xl font-bold text-white">{Math.round((carbonData.totalEmission / carbonData.target) * 100)}%</span>
                </div>
              </div>
              <div className="flex-1">
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="text-gray-400">Annual Target</span>
                    <span className="text-white font-medium">{carbonData.target.toLocaleString()} tCO2</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-gray-400">Emitted</span>
                    <span className="text-white font-medium">{carbonData.totalEmission.toLocaleString()} tCO2</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-gray-400">Remaining Quota</span>
                    <span className="text-green-400 font-medium">{(carbonData.target - carbonData.totalEmission).toLocaleString()} tCO2</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </>
      )}

      {/* Emission Sources */}
      {activeTab === 'source' && (
        <div className="flex-1 grid grid-cols-2 gap-4">
          {/* Pie Chart */}
          <div className="data-card">
            <h4 className="text-sm font-medium text-white mb-4">Emission Source Ratio</h4>
            <div className="flex items-center justify-center">
              <div className="relative w-48 h-48">
                <svg className="w-48 h-48 transform -rotate-90">
                  {(() => {
                    let offset = 0;
                    const colors = ['#3B82F6', '#22C55E', '#EAB308', '#6B7280'];
                    return carbonData.sources.map((source, i) => {
                      const dashArray = (source.percentage / 100) * 314.16;
                      const dashOffset = -offset;
                      offset += dashArray;
                      return (
                        <circle
                          key={i}
                          cx="96"
                          cy="96"
                          r="50"
                          stroke={colors[i]}
                          strokeWidth="20"
                          fill="none"
                          strokeDasharray={`${dashArray} 314.16`}
                          strokeDashoffset={dashOffset}
                        />
                      );
                    });
                  })()}
                </svg>
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3 mt-4">
              {carbonData.sources.map((source, i) => {
                const colors = ['#3B82F6', '#22C55E', '#EAB308', '#6B7280'];
                return (
                  <div key={i} className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded-full" style={{ backgroundColor: colors[i] }} />
                    <span className="text-sm text-gray-400">{source.name}</span>
                    <span className="text-sm text-white ml-auto">{source.percentage}%</span>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Source Details */}
          <div className="data-card">
            <h4 className="text-sm font-medium text-white mb-4">Emission Source Details</h4>
            <div className="space-y-3">
              {carbonData.sources.map((source, i) => {
                const colors = ['#3B82F6', '#22C55E', '#EAB308', '#6B7280'];
                return (
                  <div key={i} className="p-3 bg-[#1F2937]/50 rounded-lg">
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center gap-2">
                        <div className="w-3 h-3 rounded-full" style={{ backgroundColor: colors[i] }} />
                        <span className="text-white font-medium">{source.name}</span>
                      </div>
                      <span className="text-white">{source.value} tCO2</span>
                    </div>
                    <div className="w-full h-2 bg-[#374151] rounded-full overflow-hidden">
                      <div
                        className="h-full rounded-full"
                        style={{ width: `${source.percentage}%`, backgroundColor: colors[i] }}
                      />
                    </div>
                    <div className="flex items-center justify-between mt-1 text-xs text-gray-500">
                      <span>Ratio {source.percentage}%</span>
                      <span>YoY -{(5 + i * 2)}%</span>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}

      {/* Carbon Trading */}
      {activeTab === 'trade' && (
        <div className="flex-1">
          <div className="grid grid-cols-3 gap-4 mb-4">
            <div className="data-card text-center">
              <p className="text-sm text-gray-400 mb-2">Carbon Quota Balance</p>
              <p className="text-2xl font-bold text-green-400">{(carbonData.target - carbonData.totalEmission).toLocaleString()} t</p>
            </div>
            <div className="data-card text-center">
              <p className="text-sm text-gray-400 mb-2">Carbon Price</p>
              <p className="text-2xl font-bold text-white">78 / t</p>
            </div>
            <div className="data-card text-center">
              <p className="text-sm text-gray-400 mb-2">Estimated Carbon Assets</p>
              <p className="text-2xl font-bold text-blue-400">{((carbonData.target - carbonData.totalEmission) * 78 / 10000).toFixed(2)} M</p>
            </div>
          </div>
          <div className="data-card flex-1">
            <h4 className="text-sm font-medium text-white mb-4">Carbon Trading Records</h4>
            <div className="space-y-2">
              {[
                { date: '2024-01-10', type: 'Buy', amount: 100, price: 75, status: 'Completed' },
                { date: '2023-12-15', type: 'Sell', amount: 200, price: 68, status: 'Completed' },
                { date: '2023-11-20', type: 'Buy', amount: 150, price: 72, status: 'Completed' },
              ].map((trade, i) => (
                <div key={i} className="flex items-center justify-between p-3 bg-[#1F2937]/50 rounded-lg">
                  <div className="flex items-center gap-3">
                    <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${
                      trade.type === 'Buy' ? 'bg-green-500/20' : 'bg-red-500/20'
                    }`}>
                      <Leaf className={`w-5 h-5 ${trade.type === 'Buy' ? 'text-green-400' : 'text-red-400'}`} />
                    </div>
                    <div>
                      <p className="text-white">{trade.type} {trade.amount} tCO2</p>
                      <p className="text-xs text-gray-400">Price: {trade.price} | Total: {trade.amount * trade.price}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-4">
                    <span className="text-sm text-gray-400">{trade.date}</span>
                    <span className="px-2 py-1 bg-green-500/20 text-green-400 rounded text-xs">{trade.status}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
