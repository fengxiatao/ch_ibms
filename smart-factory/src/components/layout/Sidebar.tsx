import { 
  Home, 
  Bell, 
  BarChart3, 
  Eye, 
  Settings,
  Factory,
  Zap,
  Wrench,
  Leaf,
  Video,
  Shield,
  CloudRain,
  FileText,
  ClipboardCheck
} from 'lucide-react';

interface SidebarProps {
  activeModule: string;
  onModuleChange: (module: string) => void;
}

interface NavItem {
  id: string;
  label: string;
  icon: React.ElementType;
}

const navItems: NavItem[] = [
  { id: 'dashboard', label: '驾驶舱', icon: Home },
  { id: 'alarm', label: '告警管理', icon: Bell },
  { id: 'video', label: '视频融合', icon: Video },
  { id: 'cloudDefense', label: '立体化云防', icon: Shield },
  { id: 'business', label: '业务协同', icon: BarChart3 },
  { id: 'compliance', label: '合规管理', icon: ClipboardCheck },
  { id: 'environmental', label: '环保监测', icon: CloudRain },
  { id: 'report', label: '报表中心', icon: FileText },
  { id: 'showcase', label: '品牌展示', icon: Eye },
];

const businessSubItems: NavItem[] = [
  { id: 'production', label: '生产协同', icon: Factory },
  { id: 'energy', label: '能源管理', icon: Zap },
  { id: 'equipment', label: '设备管理', icon: Wrench },
  { id: 'carbon', label: '碳资产', icon: Leaf },
];

export function Sidebar({ activeModule, onModuleChange }: SidebarProps) {
  const isBusinessModule = ['production', 'energy', 'equipment', 'carbon'].includes(activeModule);
  
  return (
    <aside className="w-20 bg-[#111827] border-r border-[#1F2937] flex flex-col h-full">
      {/* Logo */}
      <div className="h-16 flex items-center justify-center border-b border-[#1F2937]">
        <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-cyan-500 rounded-lg flex items-center justify-center">
          <Factory className="w-6 h-6 text-white" />
        </div>
      </div>
      
      {/* Main Navigation */}
      <nav className="flex-1 py-4 px-2 space-y-1 overflow-y-auto">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = item.id === activeModule || (item.id === 'business' && isBusinessModule);
          
          return (
            <button
              key={item.id}
              onClick={() => onModuleChange(item.id)}
              className={`nav-item w-full ${isActive ? 'bg-blue-500/20 text-blue-400' : 'text-gray-500 hover:text-gray-300'}`}
              title={item.label}
            >
              <Icon className="w-6 h-6" />
              <span className="text-[10px] mt-1 truncate w-full text-center">{item.label}</span>
            </button>
          );
        })}
      </nav>
      
      {/* Bottom Actions */}
      <div className="py-4 px-2 border-t border-[#1F2937]">
        <button className="nav-item w-full text-gray-500 hover:text-gray-300">
          <Settings className="w-6 h-6" />
          <span className="text-[10px] mt-1">设置</span>
        </button>
      </div>
    </aside>
  );
}

export function BusinessSubSidebar({ activeModule, onModuleChange }: SidebarProps) {
  return (
    <aside className="w-16 bg-[#0d1219] border-r border-[#1F2937] flex flex-col h-full">
      <div className="h-10 flex items-center justify-center border-b border-[#1F2937]">
        <span className="text-[10px] text-gray-500">业务模块</span>
      </div>
      <nav className="flex-1 py-4 px-1 space-y-1">
        {businessSubItems.map((item) => {
          const Icon = item.icon;
          const isActive = item.id === activeModule;
          
          return (
            <button
              key={item.id}
              onClick={() => onModuleChange(item.id)}
              className={`flex flex-col items-center justify-center py-2 px-1 rounded-lg cursor-pointer transition-all duration-200 w-full ${
                isActive 
                  ? 'bg-blue-500/20 text-blue-400' 
                  : 'text-gray-500 hover:text-gray-300 hover:bg-[#1F2937]'
              }`}
              title={item.label}
            >
              <Icon className="w-5 h-5" />
              <span className="text-[9px] mt-1">{item.label}</span>
            </button>
          );
        })}
      </nav>
    </aside>
  );
}
