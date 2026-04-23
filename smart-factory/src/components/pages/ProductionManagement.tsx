import { Factory, Clipboard, TrendingUp, Clock, Plus, Search, ChevronRight, User, Wrench, Package, FlaskConical } from 'lucide-react';
import { useState } from 'react';
import { productionPlans, batchTraceData, realTimeAlarms } from '@/data/mockData';

export function ProductionManagement() {
  const [activeTab, setActiveTab] = useState<'plan' | 'batch'>('plan');
  const [selectedBatch, setSelectedBatch] = useState<any>(null);

  return (
    <div className="h-full flex flex-col p-4 gap-4 overflow-auto">
      {/* 顶部统计 */}
      <div className="grid grid-cols-4 gap-4">
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-blue-500/20 rounded-lg flex items-center justify-center">
            <Factory className="w-6 h-6 text-blue-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">今日生产批次</p>
            <p className="text-xl font-bold text-white">8 批次</p>
          </div>
        </div>
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-green-500/20 rounded-lg flex items-center justify-center">
            <TrendingUp className="w-6 h-6 text-green-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">完成率</p>
            <p className="text-xl font-bold text-green-400">87.5%</p>
          </div>
        </div>
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-purple-500/20 rounded-lg flex items-center justify-center">
            <Clipboard className="w-6 h-6 text-purple-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">待生产批次</p>
            <p className="text-xl font-bold text-purple-400">2 批次</p>
          </div>
        </div>
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-yellow-500/20 rounded-lg flex items-center justify-center">
            <Clock className="w-6 h-6 text-yellow-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">生产效率</p>
            <p className="text-xl font-bold text-yellow-400">98.2%</p>
          </div>
        </div>
      </div>

      {/* 标签页切换 */}
      <div className="flex items-center gap-2">
        <button
          onClick={() => setActiveTab('plan')}
          className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-2 ${
            activeTab === 'plan' ? 'bg-blue-500 text-white' : 'bg-[#1F2937] text-gray-400 hover:text-white'
          }`}
        >
          <Package className="w-4 h-4" />
          生产计划
        </button>
        <button
          onClick={() => setActiveTab('batch')}
          className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-2 ${
            activeTab === 'batch' ? 'bg-blue-500 text-white' : 'bg-[#1F2937] text-gray-400 hover:text-white'
          }`}
        >
          <FlaskConical className="w-4 h-4" />
          批次追溯
        </button>
      </div>

      {/* 生产计划 */}
      {activeTab === 'plan' && (
        <>
          <div className="data-card flex-1">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-sm font-medium text-white">生产计划列表</h3>
              <div className="flex items-center gap-2">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" />
                  <input
                    type="text"
                    placeholder="搜索计划..."
                    className="w-48 bg-[#1F2937] border border-[#374151] rounded-lg py-2 pl-10 pr-4 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-blue-500"
                  />
                </div>
                <button className="px-4 py-2 bg-blue-500 text-white rounded-lg text-sm hover:bg-blue-600 flex items-center gap-2">
                  <Plus className="w-4 h-4" />
                  新建计划
                </button>
              </div>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="text-left text-xs text-gray-400 border-b border-[#374151]">
                    <th className="pb-3 font-medium">计划编号</th>
                    <th className="pb-3 font-medium">产品</th>
                    <th className="pb-3 font-medium">批次</th>
                    <th className="pb-3 font-medium">数量</th>
                    <th className="pb-3 font-medium">操作员</th>
                    <th className="pb-3 font-medium">开始时间</th>
                    <th className="pb-3 font-medium">状态</th>
                    <th className="pb-3 font-medium">进度</th>
                    <th className="pb-3 font-medium">操作</th>
                  </tr>
                </thead>
                <tbody>
                  {productionPlans.map((plan) => (
                    <tr key={plan.id} className="border-b border-[#374151]/50 hover:bg-[#1F2937]/50">
                      <td className="py-3 text-sm text-white">{plan.id}</td>
                      <td className="py-3 text-sm text-white">{plan.product}</td>
                      <td className="py-3 text-sm text-gray-400">{plan.batch}</td>
                      <td className="py-3 text-sm text-white">{plan.quantity}</td>
                      <td className="py-3">
                        <div className="flex items-center gap-2">
                          <User className="w-4 h-4 text-gray-400" />
                          <span className="text-sm text-white">{plan.operator}</span>
                        </div>
                      </td>
                      <td className="py-3 text-sm text-gray-400">{plan.startTime}</td>
                      <td className="py-3">
                        <span className={`px-2 py-1 rounded text-xs ${
                          plan.status === '进行中' ? 'bg-green-500/20 text-green-400' :
                          plan.status === '待开始' ? 'bg-blue-500/20 text-blue-400' :
                          'bg-gray-500/20 text-gray-400'
                        }`}>
                          {plan.status}
                        </span>
                      </td>
                      <td className="py-3">
                        <div className="flex items-center gap-2">
                          <div className="w-24 h-2 bg-[#374151] rounded-full overflow-hidden">
                            <div 
                              className="h-full bg-blue-500 rounded-full"
                              style={{ width: `${plan.progress}%` }}
                            />
                          </div>
                          <span className="text-sm text-gray-400">{plan.progress}%</span>
                        </div>
                      </td>
                      <td className="py-3">
                        <button className="p-1.5 bg-blue-500/20 rounded text-blue-400 hover:bg-blue-500/30">
                          <ChevronRight className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* 关联告警 */}
          <div className="data-card">
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-sm font-medium text-white">生产关联告警</h3>
              <span className="px-2 py-0.5 bg-red-500/20 text-red-400 text-xs rounded">
                {realTimeAlarms.filter(a => ['temperature', 'conductivity', 'environment'].includes(a.type)).length} 条
              </span>
            </div>
            <div className="flex gap-2 overflow-x-auto pb-2">
              {realTimeAlarms.filter(a => ['temperature', 'conductivity', 'environment'].includes(a.type)).map((alarm) => (
                <div key={alarm.id} className="flex-shrink-0 p-3 bg-[#1F2937]/50 rounded-lg min-w-[200px]">
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-sm text-red-400">{alarm.title}</span>
                    <span className={`px-1.5 py-0.5 rounded text-xs ${
                      alarm.level === 'emergency' ? 'bg-red-500/20 text-red-400' : 'bg-yellow-500/20 text-yellow-400'
                    }`}>
                      {alarm.level === 'emergency' ? '紧急' : '重要'}
                    </span>
                  </div>
                  <p className="text-xs text-gray-400">{alarm.location}</p>
                  <div className="flex items-center justify-between mt-2">
                    <span className="text-xs text-gray-500">{alarm.time}</span>
                    <button className="text-xs text-blue-400 hover:text-blue-300">处理</button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </>
      )}

      {/* 批次追溯 */}
      {activeTab === 'batch' && (
        <div className="flex-1 flex gap-4">
          {/* 批次列表 */}
          <div className="w-80 data-card flex flex-col">
            <h3 className="text-sm font-medium text-white mb-4">批次列表</h3>
            <div className="flex-1 overflow-y-auto space-y-2">
              {batchTraceData.map((batch) => (
                <div 
                  key={batch.batchNo}
                  onClick={() => setSelectedBatch(batch)}
                  className={`p-3 rounded-lg cursor-pointer transition-colors ${
                    selectedBatch?.batchNo === batch.batchNo 
                      ? 'bg-blue-500/20 border border-blue-500/50' 
                      : 'bg-[#1F2937]/50 hover:bg-[#1F2937]'
                  }`}
                >
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-sm text-white font-medium">{batch.batchNo}</span>
                    <span className={`px-2 py-0.5 rounded text-xs ${
                      batch.status === '已完成' ? 'bg-green-500/20 text-green-400' : 'bg-blue-500/20 text-blue-400'
                    }`}>
                      {batch.status}
                    </span>
                  </div>
                  <p className="text-sm text-gray-400">{batch.product}</p>
                  <p className="text-xs text-gray-500 mt-1">{batch.startTime} - {batch.endTime}</p>
                </div>
              ))}
            </div>
          </div>

          {/* 批次详情 */}
          <div className="flex-1 data-card overflow-auto">
            {selectedBatch ? (
              <>
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <h3 className="text-lg font-medium text-white">{selectedBatch.batchNo}</h3>
                    <p className="text-sm text-gray-400">{selectedBatch.product} | 数量: {selectedBatch.quantity}</p>
                  </div>
                  <span className="px-3 py-1 bg-green-500/20 text-green-400 rounded-lg">
                    {selectedBatch.status}
                  </span>
                </div>

                <div className="grid grid-cols-2 gap-4 mb-4">
                  {/* 环境信息 */}
                  <div className="p-4 bg-[#1F2937]/50 rounded-lg">
                    <h4 className="text-sm font-medium text-white mb-3">环境信息</h4>
                    <div className="grid grid-cols-3 gap-3">
                      <div className="text-center">
                        <p className="text-xs text-gray-400">温度</p>
                        <p className="text-lg font-bold text-white">{selectedBatch.environment.temperature}</p>
                      </div>
                      <div className="text-center">
                        <p className="text-xs text-gray-400">湿度</p>
                        <p className="text-lg font-bold text-white">{selectedBatch.environment.humidity}</p>
                      </div>
                      <div className="text-center">
                        <p className="text-xs text-gray-400">压差</p>
                        <p className="text-lg font-bold text-white">{selectedBatch.environment.pressure}</p>
                      </div>
                    </div>
                  </div>

                  {/* 质量信息 */}
                  <div className="p-4 bg-[#1F2937]/50 rounded-lg">
                    <h4 className="text-sm font-medium text-white mb-3">质量信息</h4>
                    <div className="grid grid-cols-3 gap-3">
                      <div className="text-center">
                        <p className="text-xs text-gray-400">外观</p>
                        <p className="text-lg font-bold text-green-400">{selectedBatch.quality.appearance}</p>
                      </div>
                      <div className="text-center">
                        <p className="text-xs text-gray-400">pH值</p>
                        <p className="text-lg font-bold text-white">{selectedBatch.quality.ph}</p>
                      </div>
                      <div className="text-center">
                        <p className="text-xs text-gray-400">菌落</p>
                        <p className="text-lg font-bold text-white">{selectedBatch.quality.bacteria}</p>
                      </div>
                    </div>
                  </div>
                </div>

                {/* 人员记录 */}
                <div className="mb-4">
                  <h4 className="text-sm font-medium text-white mb-3 flex items-center gap-2">
                    <User className="w-4 h-4 text-blue-400" />
                    人员记录
                  </h4>
                  <div className="space-y-2">
                    {selectedBatch.personnel.map((person: any, i: number) => (
                      <div key={i} className="flex items-center justify-between p-3 bg-[#1F2937]/50 rounded-lg">
                        <div className="flex items-center gap-3">
                          <div className="w-8 h-8 bg-blue-500/20 rounded-full flex items-center justify-center">
                            <User className="w-4 h-4 text-blue-400" />
                          </div>
                          <div>
                            <p className="text-sm text-white">{person.name}</p>
                            <p className="text-xs text-gray-400">{person.role}</p>
                          </div>
                        </div>
                        <div className="text-right">
                          <p className="text-xs text-gray-400">{person.time}</p>
                          <p className="text-xs text-gray-500">{person.certificate}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                {/* 设备记录 */}
                <div className="mb-4">
                  <h4 className="text-sm font-medium text-white mb-3 flex items-center gap-2">
                    <Wrench className="w-4 h-4 text-purple-400" />
                    设备记录
                  </h4>
                  <div className="space-y-2">
                    {selectedBatch.equipment.map((eq: any, i: number) => (
                      <div key={i} className="flex items-center justify-between p-3 bg-[#1F2937]/50 rounded-lg">
                        <div className="flex items-center gap-3">
                          <div className="w-8 h-8 bg-purple-500/20 rounded-full flex items-center justify-center">
                            <Factory className="w-4 h-4 text-purple-400" />
                          </div>
                          <div>
                            <p className="text-sm text-white">{eq.name}</p>
                            <p className="text-xs text-gray-400">运行时长: {eq.runtime}</p>
                          </div>
                        </div>
                        <div className="text-right">
                          {Object.entries(eq.params).map(([key, value]: [string, any]) => (
                            <p key={key} className="text-xs text-gray-400">{key}: {value}</p>
                          ))}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                {/* 原料记录 */}
                <div>
                  <h4 className="text-sm font-medium text-white mb-3 flex items-center gap-2">
                    <Package className="w-4 h-4 text-green-400" />
                    原料记录
                  </h4>
                  <table className="w-full">
                    <thead>
                      <tr className="text-left text-xs text-gray-400 border-b border-[#374151]">
                        <th className="pb-2">批号</th>
                        <th className="pb-2">供应商</th>
                        <th className="pb-2">数量</th>
                        <th className="pb-2">检验</th>
                      </tr>
                    </thead>
                    <tbody>
                      {selectedBatch.materials.map((mat: any, i: number) => (
                        <tr key={i} className="border-b border-[#374151]/50">
                          <td className="py-2 text-sm text-white">{mat.batch}</td>
                          <td className="py-2 text-sm text-gray-400">{mat.supplier}</td>
                          <td className="py-2 text-sm text-white">{mat.quantity}</td>
                          <td className="py-2">
                            <span className="px-2 py-0.5 bg-green-500/20 text-green-400 text-xs rounded">
                              {mat.inspection}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </>
            ) : (
              <div className="h-full flex items-center justify-center">
                <div className="text-center text-gray-400">
                  <FlaskConical className="w-12 h-12 mx-auto mb-2 opacity-50" />
                  <p>请选择要查看的批次</p>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
