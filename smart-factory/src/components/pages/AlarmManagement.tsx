import { Bell, AlertTriangle, Filter, Search } from 'lucide-react';
import { useState } from 'react';
import { realTimeAlarms, alarmStats } from '@/data/mockData';

export function AlarmManagement() {
  const [filter, setFilter] = useState<'all' | 'unhandled' | 'handling' | 'handled'>('all');

  return (
    <div className="h-full flex flex-col p-4 gap-4">
      {/* 顶部统计 */}
      <div className="grid grid-cols-4 gap-4">
        <div className="data-card flex items-center gap-3">
          <div className="w-10 h-10 bg-red-500/20 rounded-lg flex items-center justify-center">
            <AlertTriangle className="w-5 h-5 text-red-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">紧急告警</p>
            <p className="text-xl font-bold text-red-400">{alarmStats.emergency}</p>
          </div>
        </div>
        <div className="data-card flex items-center gap-3">
          <div className="w-10 h-10 bg-yellow-500/20 rounded-lg flex items-center justify-center">
            <AlertTriangle className="w-5 h-5 text-yellow-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">重要告警</p>
            <p className="text-xl font-bold text-yellow-400">{alarmStats.important}</p>
          </div>
        </div>
        <div className="data-card flex items-center gap-3">
          <div className="w-10 h-10 bg-blue-500/20 rounded-lg flex items-center justify-center">
            <Bell className="w-5 h-5 text-blue-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">一般告警</p>
            <p className="text-xl font-bold text-blue-400">{alarmStats.normal}</p>
          </div>
        </div>
        <div className="data-card flex items-center gap-3">
          <div className="w-10 h-10 bg-gray-500/20 rounded-lg flex items-center justify-center">
            <Bell className="w-5 h-5 text-gray-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">提示信息</p>
            <p className="text-xl font-bold text-gray-400">{alarmStats.info}</p>
          </div>
        </div>
      </div>

      {/* 筛选和搜索 */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          {['全部', '未处理', '处理中', '已处理'].map((item, i) => (
            <button
              key={item}
              onClick={() => setFilter(['all', 'unhandled', 'handling', 'handled'][i] as any)}
              className={`px-4 py-2 rounded-lg text-sm transition-colors ${
                filter === ['all', 'unhandled', 'handling', 'handled'][i]
                  ? 'bg-blue-500 text-white'
                  : 'bg-[#1F2937] text-gray-400 hover:text-white'
              }`}
            >
              {item}
            </button>
          ))}
        </div>
        <div className="flex items-center gap-2">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" />
            <input
              type="text"
              placeholder="搜索告警..."
              className="w-64 bg-[#1F2937] border border-[#374151] rounded-lg py-2 pl-10 pr-4 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-blue-500"
            />
          </div>
          <button className="p-2 bg-[#1F2937] rounded-lg text-gray-400 hover:text-white">
            <Filter className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* 告警列表 */}
      <div className="flex-1 overflow-auto">
        <div className="data-card">
          <div className="space-y-2">
            {realTimeAlarms.map((alarm) => (
              <div 
                key={alarm.id}
                className={`p-4 rounded-lg border transition-colors ${
                  alarm.level === 'emergency' ? 'bg-red-500/10 border-red-500/30' :
                  alarm.level === 'important' ? 'bg-yellow-500/10 border-yellow-500/30' :
                  alarm.level === 'normal' ? 'bg-blue-500/10 border-blue-500/30' :
                  'bg-gray-500/10 border-gray-500/30'
                }`}
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${
                      alarm.level === 'emergency' ? 'bg-red-500/20' :
                      alarm.level === 'important' ? 'bg-yellow-500/20' :
                      alarm.level === 'normal' ? 'bg-blue-500/20' :
                      'bg-gray-500/20'
                    }`}>
                      <AlertTriangle className={`w-5 h-5 ${
                        alarm.level === 'emergency' ? 'text-red-400' :
                        alarm.level === 'important' ? 'text-yellow-400' :
                        alarm.level === 'normal' ? 'text-blue-400' :
                        'text-gray-400'
                      }`} />
                    </div>
                    <div>
                      <p className="text-white font-medium">{alarm.title}</p>
                      <p className="text-sm text-gray-400">{alarm.location} · {alarm.time}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className={`px-3 py-1 rounded-full text-sm ${
                      alarm.status === 'unhandled' ? 'bg-red-500/20 text-red-400' :
                      alarm.status === 'handling' ? 'bg-yellow-500/20 text-yellow-400' :
                      'bg-green-500/20 text-green-400'
                    }`}>
                      {alarm.status === 'unhandled' ? '未处理' :
                       alarm.status === 'handling' ? '处理中' : '已处理'}
                    </span>
                    {alarm.status !== 'handled' && (
                      <button className="px-4 py-2 bg-blue-500/20 text-blue-400 rounded-lg hover:bg-blue-500/30">
                        处理
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
