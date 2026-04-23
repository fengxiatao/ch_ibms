import { motion } from 'framer-motion';
import { useCurrentTime } from '@/hooks/useCurrentTime';
import { Wifi, Database, RefreshCw } from 'lucide-react';

export function StatusBar() {
  const { formattedTime } = useCurrentTime();

  return (
    <motion.footer
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, delay: 0.3 }}
      className="fixed bottom-0 left-0 right-0 z-50 h-10 bg-[rgba(15,23,42,0.9)] backdrop-blur-md border-t border-[rgba(148,163,184,0.2)]"
    >
      <div className="h-full px-6 flex items-center justify-between text-xs">
        <div className="flex items-center gap-6">
          <span className="text-[#64748B]">
            系统版本: <span className="text-[#94A3B8]">V2.1.0</span>
          </span>
          <span className="text-[#64748B]">
            数据更新: <span className="text-[#27AE60] flex items-center gap-1 inline-flex"><RefreshCw className="w-3 h-3 animate-spin" /> 实时同步中</span>
          </span>
        </div>
        
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-2 text-[#64748B]">
            <Wifi className="w-3 h-3 text-[#27AE60]" />
            <span>网络连接正常</span>
          </div>
          <div className="flex items-center gap-2 text-[#64748B]">
            <Database className="w-3 h-3 text-[#36BFFA]" />
            <span>数据库连接正常</span>
          </div>
          <span className="text-[#94A3B8] font-mono">
            最后更新: {formattedTime}
          </span>
        </div>
      </div>
    </motion.footer>
  );
}
