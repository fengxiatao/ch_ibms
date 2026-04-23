import { motion } from 'framer-motion';
import { Factory, Activity, Thermometer, Shield, Zap, Video } from 'lucide-react';
import type { DashboardTab } from '@/types';
import { useCurrentTime } from '@/hooks/useCurrentTime';

interface NavigationBarProps {
  activeTab: DashboardTab;
  onTabChange: (tab: DashboardTab) => void;
}

const tabs: { id: DashboardTab; label: string; icon: React.ComponentType<{ className?: string }> }[] = [
  { id: 'overview', label: '工厂总览', icon: Factory },
  { id: 'production', label: '生产监控', icon: Activity },
  { id: 'environment', label: '环境监控', icon: Thermometer },
  { id: 'safety', label: '安全监控', icon: Shield },
  { id: 'energy', label: '能耗管理', icon: Zap },
  { id: 'video', label: '视频监控', icon: Video },
];

export function NavigationBar({ activeTab, onTabChange }: NavigationBarProps) {
  const { formattedTime } = useCurrentTime();

  return (
    <motion.header
      initial={{ opacity: 0, y: -20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
      className="fixed top-0 left-0 right-0 z-50 h-16 bg-[rgba(15,23,42,0.95)] backdrop-blur-md border-b border-[rgba(148,163,184,0.2)]"
    >
      <div className="h-full px-6 flex items-center justify-between">
        {/* Logo & Title */}
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-[#36BFFA] to-[#16C79A] flex items-center justify-center">
            <Factory className="w-5 h-5 text-[#0F172A]" />
          </div>
          <div>
            <h1 className="text-lg font-bold text-[#F1F5F9]">智慧工厂可视化看板</h1>
            <p className="text-xs text-[#64748B]">Smart Factory Dashboard</p>
          </div>
        </div>

        {/* Navigation Tabs */}
        <nav className="flex items-center gap-1">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;
            
            return (
              <button
                key={tab.id}
                onClick={() => onTabChange(tab.id)}
                className={`relative px-4 py-2 rounded-lg flex items-center gap-2 text-sm font-medium transition-all duration-200 ${
                  isActive 
                    ? 'text-[#36BFFA] bg-[rgba(54,191,250,0.1)]' 
                    : 'text-[#94A3B8] hover:text-[#F1F5F9] hover:bg-[rgba(148,163,184,0.1)]'
                }`}
              >
                <Icon className="w-4 h-4" />
                <span>{tab.label}</span>
                {isActive && (
                  <motion.div
                    layoutId="activeTab"
                    className="absolute bottom-0 left-2 right-2 h-0.5 bg-[#36BFFA] rounded-full"
                    transition={{ type: 'spring', stiffness: 500, damping: 30 }}
                  />
                )}
              </button>
            );
          })}
        </nav>

        {/* Time & Status */}
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 rounded-full bg-[#27AE60] animate-pulse" />
            <span className="text-sm text-[#94A3B8]">正常运行</span>
          </div>
          <div className="text-sm font-mono text-[#36BFFA]">
            {formattedTime}
          </div>
        </div>
      </div>
    </motion.header>
  );
}
