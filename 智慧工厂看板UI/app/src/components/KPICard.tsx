import { motion } from 'framer-motion';
import { Users, Activity, Package, AlertTriangle, TrendingUp, TrendingDown, ShieldCheck, Leaf } from 'lucide-react';
import { useCountUp } from '@/hooks/useCountUp';
import type { KPIData } from '@/types';

const iconMap: Record<string, React.ComponentType<{ className?: string }>> = {
  Users,
  Activity,
  Package,
  AlertTriangle,
  ShieldCheck,
  Leaf
};

interface KPICardProps {
  data: KPIData;
  index: number;
}

export function KPICard({ data, index }: KPICardProps) {
  const animatedValue = useCountUp(data.value, { 
    duration: 1000, 
    decimals: data.unit === '%' ? 1 : 0 
  });
  
  const Icon = iconMap[data.icon] || Activity;
  
  const getStatusColor = () => {
    switch (data.status) {
      case 'warning':
        return 'text-[#F39C12]';
      case 'danger':
        return 'text-[#E74C3C]';
      default:
        return 'text-[#36BFFA]';
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ 
        duration: 0.5, 
        delay: index * 0.1,
        ease: [0.25, 0.1, 0.25, 1]
      }}
      className="kpi-card"
    >
      <div className="flex items-start justify-between">
        <div className="flex items-center gap-3">
          <div className={`p-2 rounded-lg bg-[rgba(54,191,250,0.15)] ${getStatusColor()}`}>
            <Icon className="w-5 h-5" />
          </div>
          <div>
            <p className="text-sm text-[#94A3B8]">{data.label}</p>
            <div className="flex items-baseline gap-1">
              <span className={`text-2xl font-bold ${getStatusColor()}`}>
                {animatedValue.toLocaleString()}
              </span>
              <span className="text-sm text-[#64748B]">{data.unit}</span>
            </div>
          </div>
        </div>
        
        <div className={`flex items-center gap-1 text-xs ${
          data.trendUp ? 'text-[#27AE60]' : 'text-[#E74C3C]'
        }`}>
          {data.trendUp ? (
            <TrendingUp className="w-3 h-3" />
          ) : (
            <TrendingDown className="w-3 h-3" />
          )}
          <span>{data.trend > 0 ? `+${data.trend}` : data.trend}{data.unit === '%' || data.label.includes('率') ? '%' : ''}</span>
        </div>
      </div>
    </motion.div>
  );
}
