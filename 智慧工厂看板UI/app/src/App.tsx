import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { NavigationBar } from '@/components/NavigationBar';
import { StatusBar } from '@/components/StatusBar';
import { KPICard } from '@/components/KPICard';
import { AlertList } from '@/components/AlertList';
import { Factory3DView } from '@/sections/Factory3DView';
import { ProductionMonitor } from '@/sections/ProductionMonitor';
import { EnvironmentMonitor } from '@/sections/EnvironmentMonitor';
import { SafetyMonitor } from '@/sections/SafetyMonitor';
import { EnergyMonitor } from '@/sections/EnergyMonitor';
import { VideoMonitor } from '@/sections/VideoMonitor';
import type { DashboardTab } from '@/types';
import { globalKPIs, alerts } from '@/data/mockData';
import './App.css';

function App() {
  const [activeTab, setActiveTab] = useState<DashboardTab>('overview');

  const renderContent = () => {
    switch (activeTab) {
      case 'overview':
        return (
          <motion.div
            key="overview"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.4 }}
            className="h-full flex gap-4"
          >
            <div className="flex-1">
              <Factory3DView />
            </div>
            <div className="w-80">
              <AlertList alerts={alerts} maxHeight="calc(100vh - 300px)" />
            </div>
          </motion.div>
        );
      case 'production':
        return (
          <motion.div
            key="production"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.4 }}
            className="h-full"
          >
            <ProductionMonitor />
          </motion.div>
        );
      case 'environment':
        return (
          <motion.div
            key="environment"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.4 }}
            className="h-full"
          >
            <EnvironmentMonitor />
          </motion.div>
        );
      case 'safety':
        return (
          <motion.div
            key="safety"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.4 }}
            className="h-full"
          >
            <SafetyMonitor />
          </motion.div>
        );
      case 'energy':
        return (
          <motion.div
            key="energy"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.4 }}
            className="h-full"
          >
            <EnergyMonitor />
          </motion.div>
        );
      case 'video':
        return (
          <motion.div
            key="video"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.4 }}
            className="h-full"
          >
            <VideoMonitor />
          </motion.div>
        );
      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen bg-[#0F172A] flex flex-col">
      {/* Navigation Bar */}
      <NavigationBar activeTab={activeTab} onTabChange={setActiveTab} />

      {/* Main Content */}
      <main className="flex-1 pt-20 pb-12 px-6 overflow-hidden">
        {/* Global KPIs - Show on all tabs except video */}
        {activeTab !== 'video' && (
          <motion.div
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className="mb-4"
          >
            <div className="grid grid-cols-6 gap-4">
              {globalKPIs.map((kpi, index) => (
                <KPICard key={kpi.label} data={kpi} index={index} />
              ))}
            </div>
          </motion.div>
        )}

        {/* Tab Content */}
        <div className={`${activeTab === 'video' ? 'h-[calc(100vh-100px)]' : 'h-[calc(100vh-260px)]'}`}>
          <AnimatePresence mode="wait">
            {renderContent()}
          </AnimatePresence>
        </div>
      </main>

      {/* Status Bar */}
      <StatusBar />
    </div>
  );
}

export default App;
