import { Wrench, Wifi, AlertCircle, CheckCircle, Settings, Activity, Thermometer, Gauge, Clock, Plus, Search, ChevronRight } from 'lucide-react';
import { useState } from 'react';
import { equipmentList, maintenancePlans } from '@/data/mockData';

export function EquipmentManagement() {
  const [activeTab, setActiveTab] = useState<'list' | 'maintenance'>('list');
  const [selectedEquipment, setSelectedEquipment] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');

  const filteredEquipment = equipmentList.filter(eq => 
    eq.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    eq.location.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const selected = equipmentList.find(eq => eq.id === selectedEquipment);

  return (
    <div className="h-full flex gap-4 p-4 overflow-auto">
      {/* 左侧设备列表 */}
      <div className="flex-1 flex flex-col gap-4">
        {/* 顶部统计 */}
        <div className="grid grid-cols-4 gap-4">
          <div className="data-card flex items-center gap-3">
            <div className="w-12 h-12 bg-green-500/20 rounded-lg flex items-center justify-center">
              <CheckCircle className="w-6 h-6 text-green-400" />
            </div>
            <div>
              <p className="text-xs text-gray-400">设备总数</p>
              <p className="text-xl font-bold text-white">{equipmentList.length} 台</p>
            </div>
          </div>
          <div className="data-card flex items-center gap-3">
            <div className="w-12 h-12 bg-blue-500/20 rounded-lg flex items-center justify-center">
<Wifi className="w-6 h-6 text-blue-400" />
            </div>
            <div>
              <p className="text-xs text-gray-400">在线设备</p>
              <p className="text-xl font-bold text-blue-400">{equipmentList.filter(e => e.online).length}</p>
            </div>
          </div>
          <div className="data-card flex items-center gap-3">
            <div className="w-12 h-12 bg-red-500/20 rounded-lg flex items-center justify-center">
              <AlertCircle className="w-6 h-6 text-red-400" />
            </div>
            <div>
              <p className="text-xs text-gray-400">故障设备</p>
              <p className="text-xl font-bold text-red-400">{equipmentList.filter(e => e.status === 'fault').length}</p>
            </div>
          </div>
          <div className="data-card flex items-center gap-3">
            <div className="w-12 h-12 bg-yellow-500/20 rounded-lg flex items-center justify-center">
              <Wrench className="w-6 h-6 text-yellow-400" />
            </div>
            <div>
              <p className="text-xs text-gray-400">维保中</p>
              <p className="text-xl font-bold text-yellow-400">{equipmentList.filter(e => e.status === 'maintenance').length}</p>
            </div>
          </div>
        </div>

        {/* 标签页 */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <button
              onClick={() => setActiveTab('list')}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                activeTab === 'list' ? 'bg-blue-500 text-white' : 'bg-[#1F2937] text-gray-400 hover:text-white'
              }`}
            >
              设备列表
            </button>
            <button
              onClick={() => setActiveTab('maintenance')}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                activeTab === 'maintenance' ? 'bg-blue-500 text-white' : 'bg-[#1F2937] text-gray-400 hover:text-white'
              }`}
            >
              维保计划
            </button>
          </div>
          <div className="flex items-center gap-2">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" />
              <input
                type="text"
                placeholder="搜索设备..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-48 bg-[#1F2937] border border-[#374151] rounded-lg py-2 pl-10 pr-4 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-blue-500"
              />
            </div>
            <button className="px-4 py-2 bg-blue-500 text-white rounded-lg text-sm hover:bg-blue-600 flex items-center gap-2">
              <Plus className="w-4 h-4" />
              添加设备
            </button>
          </div>
        </div>

        {/* 设备列表 */}
        {activeTab === 'list' && (
          <div className="flex-1 data-card overflow-auto">
            <div className="grid grid-cols-2 lg:grid-cols-3 gap-4">
              {filteredEquipment.map((eq) => (
                <div 
                  key={eq.id}
                  onClick={() => setSelectedEquipment(eq.id)}
                  className={`p-4 bg-[#1F2937]/50 rounded-lg cursor-pointer hover:bg-[#1F2937] transition-colors ${
                    selectedEquipment === eq.id ? 'ring-2 ring-blue-500' : ''
                  }`}
                >
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center gap-3">
                      <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${
                        eq.status === 'running' ? 'bg-green-500/20' :
eq.status === 'fault' ? 'bg-red-500/20' :
                        eq.status === 'maintenance' ? 'bg-yellow-500/20' :
                        'bg-gray-500/20'
                      }`}>
                        <Wrench className={`w-5 h-5 ${
                          eq.status === 'running' ? 'text-green-400' :
                          eq.status === 'fault' ? 'text-red-400' :
                          eq.status === 'maintenance' ? 'text-yellow-400' :
                          'text-gray-400'
                        }`} />
                      </div>
                      <div>
                        <p className="text-white font-medium">{eq.name}</p>
                        <p className="text-xs text-gray-400">{eq.type}</p>
                      </div>
                    </div>
                    <button className="p-1.5 bg-[#374151] rounded-lg text-gray-400 hover:text-white">
                      <ChevronRight className="w-4 h-4" />
                    </button>
                  </div>
                  <div className="flex items-center justify-between text-sm mb-2">
                    <span className="text-gray-400">{eq.location}</span>
                    <div className="flex items-center gap-2">
                      {eq.online ? (
                        <span className="flex items-center gap-1 text-green-400">
                          <Wifi className="w-3 h-3" /> 在线
                        </span>
                      ) : (
                        <span className="text-gray-500">离线</span>
                      )}
                    </div>
                  </div>
                  <div className="flex items-center gap-4 text-xs">
                    <span className={`${
                      eq.status === 'running' ? 'text-green-400' :
                      eq.status === 'fault' ? 'text-red-400' :
                      eq.status === 'maintenance' ? 'text-yellow-400' :
                      'text-gray-400'
                    }`}>
                      {eq.status === 'running' ? '运行中' :
                       eq.status === 'fault' ? '故障' :
                       eq.status === 'maintenance' ? '维保中' : '停机'}
                    </span>
                    {eq.efficiency > 0 && (
                      <span className="text-gray-500">效率: {eq.efficiency}%</span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* 维保计划 */}
        {activeTab === 'maintenance' && (
          <div className="flex-1 data-card overflow-auto">
            <div className="space-y-3">
              {maintenancePlans.map((plan) => (
                <div key={plan.id} className="p-4 bg-[#1F2937]/50 rounded-lg hover:bg-[#1F2937] transition-colors">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${
                        plan.status === '进行中' ? 'bg-yellow-500/20' :
                        plan.status === '待执行' ? 'bg-blue-500/20' :
                        'bg-green-500/20'
                      }`}>
                        <Wrench className={`w-5 h-5 ${
                          plan.status === '进行中' ? 'text-yellow-400' :
                          plan.status === '待执行' ? 'text-blue-400' :
                          'text-green-400'
                        }`} />
                      </div>
                      <div>
                        <p className="text-white font-medium">{plan.equipment}</p>
                        <p className="text-sm text-gray-400">{plan.type}</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-4">
                      <div className="text-right">
                        <p className="text-sm text-gray-400">下次维保</p>
                        <p className="text-sm text-white">{plan.nextDate}</p>
                      </div>
                      <span className={`px-3 py-1 rounded text-sm ${
                        plan.status === '进行中' ? 'bg-yellow-500/20 text-yellow-400' :
                        plan.status === '待执行' ? 'bg-blue-500/20 text-blue-400' :
                        'bg-green-500/20 text-green-400'
                      }`}>
                        {plan.status}
                      </span>
                      <button className="px-3 py-1 bg-blue-500/20 text-blue-400 rounded text-sm hover:bg-blue-500/30">
                        处理
                      </button>
                    </div>
                  </div>
                  <div className="flex items-center gap-2 mt-2 text-xs text-gray-500">
                    <Clock className="w-3 h-3" />
                    <span>负责人: {plan.handler}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* 右侧详情面板 */}
      <div className="w-96 data-card flex flex-col">
        <h3 className="text-sm font-medium text-white mb-4">设备详情</h3>
        {selected ? (
          <div className="flex-1 overflow-auto space-y-4">
            {/* 基本信息 */}
            <div className="p-4 bg-[#1F2937]/50 rounded-lg">
              <h4 className="text-sm font-medium text-white mb-3">基本信息</h4>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-400">设备名称</span>
                  <span className="text-white">{selected.name}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-400">设备编号</span>
                  <span className="text-white">{selected.id}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-400">设备类型</span>
                  <span className="text-white">{selected.type}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-400">安装位置</span>
                  <span className="text-white">{selected.location}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-400">投运时间</span>
                  <span className="text-white">{selected.runtime}</span>
                </div>
              </div>
            </div>

            {/* 实时状态 */}
            <div className="p-4 bg-[#1F2937]/50 rounded-lg">
              <h4 className="text-sm font-medium text-white mb-3 flex items-center gap-2">
                <Activity className="w-4 h-4 text-blue-400" />
                实时状态
              </h4>
              <div className="grid grid-cols-3 gap-3">
                <div className="text-center p-2 bg-[#111827] rounded-lg">
                  <Thermometer className="w-5 h-5 text-yellow-400 mx-auto mb-1" />
                  <p className="text-lg font-bold text-white">{selected.temperature}°C</p>
                  <p className="text-xs text-gray-400">温度</p>
                </div>
                <div className="text-center p-2 bg-[#111827] rounded-lg">
                  <Activity className="w-5 h-5 text-blue-400 mx-auto mb-1" />
                  <p className="text-lg font-bold text-white">{selected.vibration}</p>
                  <p className="text-xs text-gray-400">振动(mm/s)</p>
                </div>
                <div className="text-center p-2 bg-[#111827] rounded-lg">
                  <Gauge className="w-5 h-5 text-green-400 mx-auto mb-1" />
                  <p className="text-lg font-bold text-white">{selected.efficiency}%</p>
                  <p className="text-xs text-gray-400">效率</p>
                </div>
              </div>
            </div>

            {/* 状态指示 */}
            <div className="p-4 bg-[#1F2937]/50 rounded-lg">
              <h4 className="text-sm font-medium text-white mb-3">运行状态</h4>
              <div className="flex items-center gap-3">
                <div className={`w-3 h-3 rounded-full ${
                  selected.status === 'running' ? 'bg-green-400 animate-pulse' :
                  selected.status === 'fault' ? 'bg-red-400' :
                  selected.status === 'maintenance' ? 'bg-yellow-400' :
                  'bg-gray-400'
                }`} />
                <span className={`text-lg font-medium ${
                  selected.status === 'running' ? 'text-green-400' :
                  selected.status === 'fault' ? 'text-red-400' :
                  selected.status === 'maintenance' ? 'text-yellow-400' :
                  'text-gray-400'
                }`}>
                  {selected.status === 'running' ? '运行中' :
                   selected.status === 'fault' ? '故障' :
                   selected.status === 'maintenance' ? '维保中' : '停机'}
                </span>
                {selected.online && (
                  <span className="px-2 py-0.5 bg-green-500/20 text-green-400 rounded text-xs flex items-center gap-1">
                    <Wifi className="w-3 h-3" /> 在线监控
                  </span>
                )}
              </div>
            </div>

            {/* 操作按钮 */}
            <div className="space-y-2">
              <button className="w-full py-2 bg-blue-500 text-white rounded-lg text-sm hover:bg-blue-600 flex items-center justify-center gap-2">
                <Settings className="w-4 h-4" />
                设备控制
              </button>
              <button className="w-full py-2 bg-[#1F2937] text-white rounded-lg text-sm hover:bg-[#374151] flex items-center justify-center gap-2">
                <Wrench className="w-4 h-4" />
                申请维保
              </button>
            </div>
          </div>
        ) : (
          <div className="flex-1 flex items-center justify-center text-gray-400">
            <div className="text-center">
              <Wrench className="w-12 h-12 mx-auto mb-2 opacity-50" />
              <p>请选择设备查看详情</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
