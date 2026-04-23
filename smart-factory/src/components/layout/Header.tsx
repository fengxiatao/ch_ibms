import { Bell, Search, User, RefreshCw } from 'lucide-react';
import { useState } from 'react';

interface HeaderProps {
  title: string;
  subtitle?: string;
}

export function Header({ title, subtitle }: HeaderProps) {
  const [notifications] = useState(3);
  const currentTime = new Date().toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });

  return (
    <header className="h-16 bg-[#111827] border-b border-[#1F2937] flex items-center justify-between px-6">
      {/* Left: Title */}
      <div className="flex items-center gap-4">
        <div>
          <h1 className="text-lg font-bold text-white">{title}</h1>
          {subtitle && <p className="text-xs text-gray-400">{subtitle}</p>}
        </div>
      </div>

      {/* Center: Search */}
      <div className="flex-1 max-w-md mx-4">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" />
          <input
            type="text"
            placeholder="搜索设备、告警、区域..."
            className="w-full bg-[#1F2937] border border-[#374151] rounded-lg py-2 pl-10 pr-4 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-blue-500 transition-colors"
          />
        </div>
      </div>

      {/* Right: Actions */}
      <div className="flex items-center gap-4">
        {/* Time */}
        <div className="text-sm text-gray-400 hidden md:block">
          {currentTime}
        </div>

        {/* Refresh */}
        <button className="p-2 rounded-lg hover:bg-[#1F2937] text-gray-400 hover:text-white transition-colors">
          <RefreshCw className="w-5 h-5" />
        </button>

        {/* Notifications */}
        <button className="relative p-2 rounded-lg hover:bg-[#1F2937] text-gray-400 hover:text-white transition-colors">
          <Bell className="w-5 h-5" />
          {notifications > 0 && (
            <span className="absolute top-1 right-1 w-4 h-4 bg-red-500 rounded-full text-[10px] text-white flex items-center justify-center">
              {notifications}
            </span>
          )}
        </button>

        {/* User */}
        <div className="flex items-center gap-2 pl-4 border-l border-[#374151]">
          <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-cyan-500 rounded-full flex items-center justify-center">
            <User className="w-4 h-4 text-white" />
          </div>
          <div className="hidden md:block">
            <p className="text-sm text-white">管理员</p>
            <p className="text-xs text-gray-400">超级管理员</p>
          </div>
        </div>
      </div>
    </header>
  );
}
