import { FileText, Download, Calendar, Search, Clock, CheckCircle, Eye, Printer } from 'lucide-react';
import { useState } from 'react';
import { reportTemplates } from '@/data/mockData';

export function ReportCenter() {
  const [selectedType, setSelectedType] = useState<string>('all');
  const [searchTerm, setSearchTerm] = useState('');

  const filteredReports = reportTemplates.filter(report => 
    (selectedType === 'all' || report.type === selectedType) &&
    (report.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
     report.description.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  return (
    <div className="h-full flex flex-col p-4 gap-4 overflow-auto">
      {/* 顶部统计 */}
      <div className="grid grid-cols-4 gap-4">
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-blue-500/20 rounded-lg flex items-center justify-center">
            <FileText className="w-6 h-6 text-blue-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">报表总数</p>
            <p className="text-xl font-bold text-white">{reportTemplates.length}</p>
          </div>
        </div>
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-green-500/20 rounded-lg flex items-center justify-center">
            <CheckCircle className="w-6 h-6 text-green-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">已生成</p>
            <p className="text-xl font-bold text-green-400">128 份</p>
          </div>
        </div>
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-yellow-500/20 rounded-lg flex items-center justify-center">
            <Calendar className="w-6 h-6 text-yellow-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">今日生成</p>
            <p className="text-xl font-bold text-yellow-400">6 份</p>
          </div>
        </div>
        <div className="data-card flex items-center gap-3">
          <div className="w-12 h-12 bg-purple-500/20 rounded-lg flex items-center justify-center">
            <Clock className="w-6 h-6 text-purple-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">待生成</p>
            <p className="text-xl font-bold text-purple-400">2 份</p>
          </div>
        </div>
      </div>

      {/* 筛选和搜索 */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          {['all', 'daily', 'weekly', 'monthly', 'equipment', 'energy', 'quality'].map((type) => (
            <button
              key={type}
              onClick={() => setSelectedType(type)}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                selectedType === type ? 'bg-blue-500 text-white' : 'bg-[#1F2937] text-gray-400 hover:text-white'
              }`}
            >
              {type === 'all' ? '全部' :
               type === 'daily' ? '日报表' :
               type === 'weekly' ? '周报表' :
               type === 'monthly' ? '月报表' :
               type === 'equipment' ? '设备报表' :
               type === 'energy' ? '能耗报表' :
               '质量报表'}
            </button>
          ))}
        </div>
        <div className="flex items-center gap-2">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" />
            <input
              type="text"
              placeholder="搜索报表..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-64 bg-[#1F2937] border border-[#374151] rounded-lg py-2 pl-10 pr-4 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-blue-500"
            />
          </div>
          <button className="px-4 py-2 bg-blue-500 text-white rounded-lg text-sm hover:bg-blue-600 flex items-center gap-2">
            <FileText className="w-4 h-4" />
            新建报表
          </button>
        </div>
      </div>

      {/* 报表模板 */}
      <div className="data-card flex-1 overflow-auto">
        <h3 className="text-sm font-medium text-white mb-4">报表模板</h3>
        <div className="grid grid-cols-3 gap-4">
          {filteredReports.map((report) => (
            <div key={report.id} className="p-4 bg-[#1F2937]/50 rounded-lg hover:bg-[#1F2937] transition-colors cursor-pointer">
              <div className="flex items-start justify-between mb-3">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-blue-500/20 rounded-lg flex items-center justify-center">
                    <FileText className="w-5 h-5 text-blue-400" />
                  </div>
                  <div>
                    <p className="text-white font-medium">{report.name}</p>
                    <p className="text-xs text-gray-400">{report.type === 'daily' ? '日报表' :
                      report.type === 'weekly' ? '周报表' :
                      report.type === 'monthly' ? '月报表' :
                      report.type === 'equipment' ? '设备报表' :
                      report.type === 'energy' ? '能耗报表' :
                      '质量报表'}</p>
                  </div>
                </div>
              </div>
              <p className="text-sm text-gray-400 mb-3">{report.description}</p>
              <div className="flex items-center justify-between text-xs text-gray-500">
                <span>最后生成: {report.lastGenerate}</span>
              </div>
              <div className="flex items-center gap-2 mt-3">
                <button className="flex-1 py-1.5 bg-blue-500/20 text-blue-400 rounded text-sm hover:bg-blue-500/30 flex items-center justify-center gap-1">
                  <Eye className="w-3 h-3" />
                  预览
                </button>
                <button className="flex-1 py-1.5 bg-green-500/20 text-green-400 rounded text-sm hover:bg-green-500/30 flex items-center justify-center gap-1">
                  <Download className="w-3 h-3" />
                  生成
                </button>
                <button className="py-1.5 px-3 bg-[#374151] text-gray-400 rounded text-sm hover:text-white flex items-center justify-center">
                  <Printer className="w-3 h-3" />
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 历史报表 */}
      <div className="data-card">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-sm font-medium text-white">最近生成的报表</h3>
          <button className="text-sm text-blue-400 hover:text-blue-300">查看全部</button>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="text-left text-xs text-gray-400 border-b border-[#374151]">
                <th className="pb-3 font-medium">报表名称</th>
                <th className="pb-3 font-medium">类型</th>
                <th className="pb-3 font-medium">生成时间</th>
                <th className="pb-3 font-medium">操作</th>
              </tr>
            </thead>
            <tbody>
              {[
                { name: '日报表-20240115', type: '日报表', time: '2024-01-15 18:00' },
                { name: '设备运行报表-20240115', type: '设备报表', time: '2024-01-15 08:00' },
                { name: '能耗分析报表-20240115', type: '能耗报表', time: '2024-01-15 07:00' },
                { name: '质量分析报表-20240115', type: '质量报表', time: '2024-01-15 08:00' },
                { name: '周报表-第3周', type: '周报表', time: '2024-01-14 18:00' },
              ].map((report, i) => (
                <tr key={i} className="border-b border-[#374151]/50 hover:bg-[#1F2937]/50">
                  <td className="py-3 text-sm text-white">{report.name}</td>
                  <td className="py-3">
                    <span className="px-2 py-0.5 bg-blue-500/20 text-blue-400 text-xs rounded">{report.type}</span>
                  </td>
                  <td className="py-3 text-sm text-gray-400">{report.time}</td>
                  <td className="py-3">
                    <div className="flex items-center gap-2">
                      <button className="p-1.5 bg-blue-500/20 rounded text-blue-400 hover:bg-blue-500/30">
                        <Eye className="w-4 h-4" />
                      </button>
                      <button className="p-1.5 bg-green-500/20 rounded text-green-400 hover:bg-green-500/30">
                        <Download className="w-4 h-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
