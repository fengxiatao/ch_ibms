import { Eye, Award, Users, Globe, Play } from 'lucide-react';

export function BrandShowcase() {
  return (
    <div className="h-full flex flex-col p-4 gap-4 overflow-auto">
      {/* 顶部介绍 */}
      <div className="data-card">
        <div className="flex items-center gap-6">
          <div className="w-20 h-20 bg-gradient-to-br from-blue-500 to-cyan-500 rounded-2xl flex items-center justify-center">
            <Award className="w-10 h-10 text-white" />
          </div>
          <div>
            <h2 className="text-xl font-bold text-white mb-2">智慧工厂品牌展示中心</h2>
            <p className="text-gray-400">基于数字孪生技术的智能工厂展示平台，提供沉浸式参观体验</p>
          </div>
        </div>
      </div>

      {/* 功能入口 */}
      <div className="grid grid-cols-3 gap-4 flex-1">
        <div className="data-card flex flex-col items-center justify-center cursor-pointer hover:ring-2 hover:ring-blue-500/50 transition-all">
          <div className="w-16 h-16 bg-blue-500/20 rounded-2xl flex items-center justify-center mb-4">
            <Eye className="w-8 h-8 text-blue-400" />
          </div>
          <h3 className="text-lg font-medium text-white mb-2">智能参观导览</h3>
          <p className="text-sm text-gray-400 text-center">沉浸式VR参观体验</p>
          <button className="mt-4 px-6 py-2 bg-blue-500/20 text-blue-400 rounded-lg hover:bg-blue-500/30 flex items-center gap-2">
            <Play className="w-4 h-4" /> 开始体验
          </button>
        </div>

        <div className="data-card flex flex-col items-center justify-center cursor-pointer hover:ring-2 hover:ring-green-500/50 transition-all">
          <div className="w-16 h-16 bg-green-500/20 rounded-2xl flex items-center justify-center mb-4">
            <Users className="w-8 h-8 text-green-400" />
          </div>
          <h3 className="text-lg font-medium text-white mb-2">客户案例展示</h3>
          <p className="text-sm text-gray-400 text-center">成功案例与客户故事</p>
          <button className="mt-4 px-6 py-2 bg-green-500/20 text-green-400 rounded-lg hover:bg-green-500/30 flex items-center gap-2">
            <Play className="w-4 h-4" /> 查看详情
          </button>
        </div>

        <div className="data-card flex flex-col items-center justify-center cursor-pointer hover:ring-2 hover:ring-purple-500/50 transition-all">
          <div className="w-16 h-16 bg-purple-500/20 rounded-2xl flex items-center justify-center mb-4">
            <Globe className="w-8 h-8 text-purple-400" />
          </div>
          <h3 className="text-lg font-medium text-white mb-2">企业形象展示</h3>
          <p className="text-sm text-gray-400 text-center">企业文化与实力展示</p>
          <button className="mt-4 px-6 py-2 bg-purple-500/20 text-purple-400 rounded-lg hover:bg-purple-500/30 flex items-center gap-2">
            <Play className="w-4 h-4" /> 了解更多
          </button>
        </div>
      </div>
    </div>
  );
}
