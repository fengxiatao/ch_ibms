import { motion, AnimatePresence } from 'framer-motion';
import { ScrollArea } from '@/components/ui/scroll-area';
import type { Alert } from '@/types';
import { StatusBadge } from './StatusBadge';
import { AlertTriangle, Clock, MapPin } from 'lucide-react';

interface AlertListProps {
  alerts: Alert[];
  maxHeight?: string;
}

export function AlertList({ alerts, maxHeight = '400px' }: AlertListProps) {
  const getAlertIcon = (level: string) => {
    switch (level) {
      case 'emergency':
        return <div className="w-2 h-2 rounded-full bg-[#E74C3C] animate-pulse-red" />;
      case 'serious':
        return <div className="w-2 h-2 rounded-full bg-[#EA580C]" />;
      default:
        return <div className="w-2 h-2 rounded-full bg-[#F39C12]" />;
    }
  };

  return (
    <div className="factory-card factory-card-highlight p-4">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-[#F1F5F9] flex items-center gap-2">
          <AlertTriangle className="w-5 h-5 text-[#F39C12]" />
          实时报警
        </h3>
        <span className="text-xs text-[#94A3B8]">{alerts.length} 条未处理</span>
      </div>
      
      <ScrollArea className={`h-[${maxHeight}] scrollbar-thin`} style={{ height: maxHeight }}>
        <div className="space-y-3">
          <AnimatePresence mode="popLayout">
            {alerts.map((alert, index) => (
              <motion.div
                key={alert.id}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: 20 }}
                transition={{ duration: 0.3, delay: index * 0.05 }}
                className="p-3 rounded-lg bg-[rgba(30,41,59,0.6)] border border-[rgba(148,163,184,0.15)] hover:border-[rgba(54,191,250,0.3)] transition-colors cursor-pointer"
              >
                <div className="flex items-start justify-between mb-2">
                  <div className="flex items-center gap-2">
                    {getAlertIcon(alert.level)}
                    <span className="text-sm font-medium text-[#F1F5F9]">{alert.type}</span>
                  </div>
                  <StatusBadge status={alert.level} type="alert" />
                </div>
                
                <p className="text-sm text-[#94A3B8] mb-2">{alert.message}</p>
                
                <div className="flex items-center gap-4 text-xs text-[#64748B]">
                  <span className="flex items-center gap-1">
                    <MapPin className="w-3 h-3" />
                    {alert.location}
                  </span>
                  <span className="flex items-center gap-1">
                    <Clock className="w-3 h-3" />
                    {alert.time}
                  </span>
                </div>
                
                <div className="mt-2 flex justify-end">
                  <StatusBadge status={alert.status} type="alert" />
                </div>
              </motion.div>
            ))}
          </AnimatePresence>
        </div>
      </ScrollArea>
    </div>
  );
}
