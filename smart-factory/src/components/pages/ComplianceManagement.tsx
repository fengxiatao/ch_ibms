import { Clipboard, CheckCircle, AlertTriangle, FileText, Download, Calendar, Shield, FlaskConical, Wind } from 'lucide-react';
import { useState } from 'react';
import { gmpComplianceData } from '@/data/mockData';

export function ComplianceManagement() {
  const [activeTab, setActiveTab] = useState<'gmp' | 'env' | 'trace'>('gmp');
  const [, setSelectedPoint] = useState<number | null>(null);

  return (
    <div className="h-full flex flex-col p-4 gap-4 overflow-auto">
      {/* 顶部统计 */}
      <div className="grid grid-cols-4 gap-4">
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-green-500/20 rounded-lg flex items-center justify-center">
            <CheckCircle className="w-6 h-6 text-green-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">GMP合规率</p>
            <p className="text-xl font-bold text-green-400">{gmpComplianceData.complianceRate}%</p>
          </div>
        </div>
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-blue-500/20 rounded-lg flex items-center justify-center">
            <Clipboard className="w-6 h-6 text-blue-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">监测点位</p>
            <p className="text-xl font-bold text-blue-400">{gmpComplianceData.records.length} 个</p>
          </div>
        </div>
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-yellow-500/20 rounded-lg flex items-center justify-center">
            <AlertTriangle className="w-6 h-6 text-yellow-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">超标次数</p>
            <p className="text-xl font-bold text-yellow-400">{gmpComplianceData.exceedanceCount} 次</p>
          </div>
        </div>
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-purple-500/20 rounded-lg flex items-center justify-center">
            <FileText className="w-6 h-6 text-purple-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">合规记录</p>
            <p className="text-xl font-bold text-purple-400">365 份</p>
          </div>
        </div>
      </div>

      {/* 标签页 */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          {[
            { id: 'gmp', label: 'GMP合规', icon: Shield },
            { id: 'env', label: '环保监测', icon: Wind },
            { id: 'trace', label: '批次追溯', icon: FlaskConical },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as any)}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-2 ${
                activeTab === tab.id ? 'bg-blue-500 text-white' : 'bg-[#1F2937] text-gray-400 hover:text-white'
              }`}
            >
              <tab.icon className="w-4 h-4" />
              {tab.label}
            </button>
          ))}
        </div>
        <div className="flex items-center gap-2">
          <button className="px-4 py-2 bg-[#1F2937] rounded-lg text-sm text-gray-400 hover:text-white flex items-center gap-2">
            <Calendar className="w-4 h-4" />
            历史记录
          </button>
          <button className="px-4 py-2 bg-blue-500 text-white rounded-lg text-sm hover:bg-blue-600 flex items-center gap-2">
            <Download className="w-4 h-4" />
            导出报告
          </button>
        </div>
      </div>

      {/* GMP合规监测 */}
      {activeTab === 'gmp' && (
        <>
          {/* 合规状态总览 */}
          <div className="grid grid-cols-3 gap-4">
            <div className="data-card">
              <div className="flex items-center gap-2 mb-2">
                <Shield className="w-5 h-5 text-green-400" />
                <span className="text-white font-medium">洁净区A</span>
              </div>
              <p className="text-xs text-gray-400 mb-2">温度: 22°C | 湿度: 55% | 压差: 15Pa</p>
              <div className="flex items-center gap-2">
                <div className="w-full h-2 bg-[#374151] rounded-full overflow-hidden">
                  <div className="h-full bg-green-500 rounded-full" style={{ width: '100%' }} />
                </div>
                <span className="text-green-400 text-sm">100%</span>
              </div>
            </div>
            <div className="data-card">
              <div className="flex items-center gap-2 mb-2">
                <Shield className="w-5 h-5 text-green-400" />
                <span className="text-white font-medium">洁净区B</span>
              </div>
              <p className="text-xs text-gray-400 mb-2">温度: 21°C | 湿度: 52% | 压差: 12Pa</p>
              <div className="flex items-center gap-2">
                <div className="w-full h-2 bg-[#374151] rounded-full overflow-hidden">
                  <div className="h-full bg-green-500 rounded-full" style={{ width: '100%' }} />
                </div>
                <span className="text-green-400 text-sm">100%</span>
              </div>
            </div>
            <div className="data-card border border-yellow-500/30">
              <div className="flex items-center gap-2 mb-2">
                <Shield className="w-5 h-5 text-yellow-400" />
                <span className="text-white font-medium">洁净区C</span>
              </div>
              <p className="text-xs text-gray-400 mb-2">温度: 23°C | 湿度: 58% | 压差: 8Pa ⚠️</p>
              <div className="flex items-center gap-2">
                <div className="w-full h-2 bg-[#374151] rounded-full overflow-hidden">
                  <div className="h-full bg-yellow-500 rounded-full" style={{ width: '75%' }} />
                </div>
                <span className="text-yellow-400 text-sm">75%</span>
              </div>
            </div>
          </div>

          {/* GMP合规监测表格 */}
          <div className="data-card flex-1 overflow-auto">
            <h3 className="text-sm font-medium text-white mb-4">GMP合规监测详情</h3>
            <table className="w-full">
              <thead>
                <tr className="text-left text-xs text-gray-400 border-b border-[#374151]">
                  <th className="pb-3 font-medium">监测点位</th>
                  <th className="pb-3 font-medium">合规点数</th>
                  <th className="pb-3 font-medium">超标次数</th>
                  <th className="pb-3 font-medium">状态</th>
                  <th className="pb-3 font-medium">最后检查</th>
                  <th className="pb-3 font-medium">详情</th>
                </tr>
              </thead>
              <tbody>
                {gmpComplianceData.records.map((record) => (
                  <tr 
                    key={record.id} 
                    className="border-b border-[#374151]/50 hover:bg-[#1F2937]/50 cursor-pointer"
                    onClick={() => setSelectedPoint(record.id)}
                  >
                    <td className="py-3">
                      <div className="flex items-center gap-2">
                        <Shield className={`w-4 h-4 ${
                          record.status === '正常' ? 'text-green-400' : 'text-yellow-400'
                        }`} />
                        <span className="text-sm text-white">{record.monitorPoint}</span>
                      </div>
                    </td>
                    <td className="py-3 text-sm text-white">{record.compliancePoint} 点</td>
                    <td className="py-3">
                      <span className={`text-sm ${record.exceedance > 0 ? 'text-red-400' : 'text-green-400'}`}>
                        {record.exceedance} 次
                      </span>
                    </td>
                    <td className="py-3">
                      <span className={`px-2 py-1 rounded text-xs ${
                        record.status === '正常' ? 'bg-green-500/20 text-green-400' : 'bg-yellow-500/20 text-yellow-400'
                      }`}>
                        {record.status}
                      </span>
                    </td>
                    <td className="py-3 text-sm text-gray-400">{record.lastCheck}</td>
                    <td className="py-3">
                      <button className="px-3 py-1 bg-blue-500/20 text-blue-400 rounded text-xs hover:bg-blue-500/30">
                        查看
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      {/* 环保监测 */}
      {activeTab === 'env' && (
        <div className="flex-1 grid grid-cols-2 gap-4">
          <div className="data-card">
            <h4 className="text-sm font-medium text-white mb-4">废气监测</h4>
            <div className="space-y-3">
              {[
                { name: 'VOCs', value: 12.5, limit: 20, unit: 'mg/m³' },
                { name: '颗粒物', value: 35, limit: 50, unit: 'mg/m³' },
                { name: '二氧化硫', value: 8, limit: 50, unit: 'mg/m³' },
                { name: '氮氧化物', value: 45, limit: 100, unit: 'mg/m³' },
              ].map((item, i) => (
                <div key={i} className="p-3 bg-[#1F2937]/50 rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-white font-medium">{item.name}</span>
                    <span className="text-sm text-gray-400">{item.value} / {item.limit} {item.unit}</span>
                  </div>
                  <div className="w-full h-2 bg-[#374151] rounded-full overflow-hidden">
                    <div 
                      className={`h-full rounded-full ${item.value / item.limit > 0.8 ? 'bg-red-500' : item.value / item.limit > 0.6 ? 'bg-yellow-500' : 'bg-green-500'}`}
                      style={{ width: `${(item.value / item.limit) * 100}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="data-card">
            <h4 className="text-sm font-medium text-white mb-4">废水监测</h4>
            <div className="space-y-3">
              {[
                { name: 'COD', value: 45, limit: 100, unit: 'mg/L' },
                { name: '氨氮', value: 8.5, limit: 15, unit: 'mg/L' },
                { name: 'pH值', value: 7.2, limit: '6-9', unit: '' },
                { name: '流量', value: 120, limit: 200, unit: 'm³/h' },
              ].map((item, i) => (
                <div key={i} className="p-3 bg-[#1F2937]/50 rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-white font-medium">{item.name}</span>
                    <span className="text-sm text-gray-400">
                      {typeof item.limit === 'number' ? `${item.value} / ${item.limit}` : item.value} {item.unit}
                    </span>
                  </div>
                  {typeof item.limit === 'number' && (
                    <div className="w-full h-2 bg-[#374151] rounded-full overflow-hidden">
                      <div 
                        className={`h-full rounded-full ${item.value / item.limit > 0.8 ? 'bg-red-500' : item.value / item.limit > 0.6 ? 'bg-yellow-500' : 'bg-green-500'}`}
                        style={{ width: `${(item.value / item.limit) * 100}%` }}
                      />
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>

          <div className="data-card">
            <h4 className="text-sm font-medium text-white mb-4">噪声监测</h4>
            <div className="p-4 bg-[#1F2937]/50 rounded-lg">
              <div className="flex items-center justify-center gap-8">
                <div className="text-center">
                  <p className="text-3xl font-bold text-green-400">58</p>
                  <p className="text-sm text-gray-400">昼间 dB</p>
                </div>
                <div className="w-px h-12 bg-[#374151]" />
                <div className="text-center">
                  <p className="text-3xl font-bold text-yellow-400">52</p>
                  <p className="text-sm text-gray-400">夜间 dB</p>
                </div>
                <div className="w-px h-12 bg-[#374151]" />
                <div className="text-center">
                  <p className="text-3xl font-bold text-white">65</p>
                  <p className="text-sm text-gray-400">限值 dB</p>
                </div>
              </div>
            </div>
          </div>

          <div className="data-card">
            <h4 className="text-sm font-medium text-white mb-4">环保预警</h4>
            <div className="space-y-2">
              <div className="p-3 bg-yellow-500/10 border border-yellow-500/30 rounded-lg">
                <div className="flex items-center justify-between">
                  <span className="text-yellow-400 font-medium">VOCs浓度预警</span>
                  <span className="text-xs text-gray-400">10:30</span>
                </div>
                <p className="text-xs text-gray-400 mt-1">生产车间VOCs浓度达到18.5mg/m³，接近限值</p>
              </div>
              <div className="p-3 bg-green-500/10 border border-green-500/30 rounded-lg">
                <div className="flex items-center justify-between">
                  <span className="text-green-400 font-medium">废水排放正常</span>
                  <span className="text-xs text-gray-400">09:00</span>
                </div>
                <p className="text-xs text-gray-400 mt-1">污水处理站运行正常，排放达标</p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 批次追溯 */}
      {activeTab === 'trace' && (
        <div className="data-card flex-1 overflow-auto">
          <h4 className="text-sm font-medium text-white mb-4">批次追溯查询</h4>
          <div className="flex items-center gap-4 mb-4">
            <input
              type="text"
              placeholder="输入批次号..."
              className="flex-1 bg-[#1F2937] border border-[#374151] rounded-lg py-2 px-4 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-blue-500"
            />
            <button className="px-4 py-2 bg-blue-500 text-white rounded-lg text-sm hover:bg-blue-600">
              查询
            </button>
          </div>
          <div className="text-center text-gray-400 py-8">
            <FlaskConical className="w-12 h-12 mx-auto mb-2 opacity-50" />
            <p>输入批次号进行追溯查询</p>
          </div>
        </div>
      )}
    </div>
  );
}
