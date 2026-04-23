import { useState } from 'react';
import { Sidebar, BusinessSubSidebar } from '@/components/layout/Sidebar';
import { Header } from '@/components/layout/Header';
import { Dashboard } from '@/components/pages/Dashboard';
import { AlarmManagement } from '@/components/pages/AlarmManagement';
import { VideoFusion } from '@/components/pages/VideoFusion';
import { CloudDefense } from '@/components/pages/CloudDefense';
import { ProductionManagement } from '@/components/pages/ProductionManagement';
import { EnergyManagement } from '@/components/pages/EnergyManagement';
import { EquipmentManagement } from '@/components/pages/EquipmentManagement';
import { CarbonManagement } from '@/components/pages/CarbonManagement';
import { ComplianceManagement } from '@/components/pages/ComplianceManagement';
import { EnvironmentalMonitoring } from '@/components/pages/EnvironmentalMonitoring';
import { ReportCenter } from '@/components/pages/ReportCenter';
import { BrandShowcase } from '@/components/pages/BrandShowcase';

type ModuleType = 
  | 'dashboard' 
  | 'alarm' 
  | 'video' 
  | 'cloudDefense'
  | 'business' 
  | 'production' 
  | 'energy' 
  | 'equipment' 
  | 'carbon' 
  | 'compliance' 
  | 'environmental' 
  | 'report' 
  | 'showcase';

const moduleTitles: Record<ModuleType, { title: string; subtitle?: string }> = {
  dashboard: { title: '智慧工厂驾驶舱', subtitle: '数字孪生 · 态势感知 · 全局监控' },
  alarm: { title: '告警与联动处置', subtitle: '实时监控 · 智能联动 · 处置闭环' },
  video: { title: '视频融合', subtitle: '多源视频 · 全景覆盖 · 智能分析' },
  cloudDefense: { title: '立体化云防', subtitle: '周界防护 · 人车管控 · 应急指挥' },
  business: { title: '业务协同管理', subtitle: '生产 · 能源 · 设备 · 碳资产' },
  production: { title: '生产协同管理', subtitle: '计划跟踪 · 批次关联 · 环境联动' },
  energy: { title: '能源管理', subtitle: '能耗监控 · 能效分析 · 优化建议' },
  equipment: { title: '设备管理', subtitle: '台账管理 · 状态监控 · 维保计划' },
  carbon: { title: '碳资产管理', subtitle: '碳排核算 · 趋势分析 · 减排建议' },
  compliance: { title: '合规管理', subtitle: '批次追溯 · GMP合规 · 环保监测' },
  environmental: { title: '环保监测', subtitle: '废气排放 · 废水监测 · 噪声管控' },
  report: { title: '报表中心', subtitle: '模板管理 · 数据报表 · 导出分析' },
  showcase: { title: '品牌与展示', subtitle: '智能参观 · 数据脱敏 · 企业形象' },
};

function App() {
  const [activeModule, setActiveModule] = useState<ModuleType>('dashboard');
  
  const isBusinessModule = ['production', 'energy', 'equipment', 'carbon'].includes(activeModule);
  
  const renderContent = () => {
    switch (activeModule) {
      case 'dashboard':
        return <Dashboard />;
      case 'alarm':
        return <AlarmManagement />;
      case 'video':
        return <VideoFusion />;
      case 'cloudDefense':
        return <CloudDefense />;
      case 'production':
        return <ProductionManagement />;
      case 'energy':
        return <EnergyManagement />;
      case 'equipment':
        return <EquipmentManagement />;
      case 'carbon':
        return <CarbonManagement />;
      case 'compliance':
        return <ComplianceManagement />;
      case 'environmental':
        return <EnvironmentalMonitoring />;
      case 'report':
        return <ReportCenter />;
      case 'showcase':
        return <BrandShowcase />;
      case 'business':
        setActiveModule('production');
        return <ProductionManagement />;
      default:
        return <Dashboard />;
    }
  };
  
  return (
    <div className="h-screen flex bg-[#0A0F1C] overflow-hidden">
      {/* 侧边栏 */}
      <Sidebar 
        activeModule={activeModule} 
        onModuleChange={(module) => setActiveModule(module as ModuleType)} 
      />
      
      {/* 业务子侧边栏 */}
      {isBusinessModule && (
        <BusinessSubSidebar 
          activeModule={activeModule} 
          onModuleChange={(module) => setActiveModule(module as ModuleType)} 
        />
      )}
      
      {/* 主内容区 */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* 顶部栏 */}
        <Header 
          title={moduleTitles[activeModule]?.title || '智慧工厂集成管理平台'} 
          subtitle={moduleTitles[activeModule]?.subtitle}
        />
        
        {/* 页面内容 */}
        <main className="flex-1 overflow-hidden">
          {renderContent()}
        </main>
      </div>
    </div>
  );
}

export default App;
