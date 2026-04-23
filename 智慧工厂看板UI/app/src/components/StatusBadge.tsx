import { motion } from 'framer-motion';
import type { DeviceStatus, AlertLevel, AlertStatus } from '@/types';

interface StatusBadgeProps {
  status: DeviceStatus | AlertLevel | AlertStatus | 'normal' | 'warning' | 'danger';
  type: 'device' | 'alert' | 'environment';
  text?: string;
}

const deviceStatusMap: Record<DeviceStatus, { class: string; text: string }> = {
  running: { class: 'status-normal', text: '运行中' },
  standby: { class: 'bg-[rgba(54,191,250,0.2)] text-[#36BFFA] px-2 py-0.5 rounded text-xs font-medium', text: '待机' },
  fault: { class: 'status-danger animate-pulse-red', text: '故障' },
  offline: { class: 'status-offline', text: '离线' }
};

const alertLevelMap: Record<AlertLevel, { class: string; text: string }> = {
  general: { class: 'status-warning', text: '一般' },
  serious: { class: 'bg-[rgba(234,88,12,0.2)] text-[#EA580C] px-2 py-0.5 rounded text-xs font-medium', text: '严重' },
  emergency: { class: 'status-danger animate-pulse-red', text: '紧急' }
};

const alertStatusMap: Record<AlertStatus, { class: string; text: string }> = {
  pending: { class: 'status-warning', text: '未处理' },
  processing: { class: 'bg-[rgba(54,191,250,0.2)] text-[#36BFFA] px-2 py-0.5 rounded text-xs font-medium', text: '处理中' },
  resolved: { class: 'status-normal', text: '已处理' }
};

const environmentStatusMap: Record<string, { class: string; text: string }> = {
  normal: { class: 'status-normal', text: '正常' },
  warning: { class: 'status-warning animate-pulse-yellow', text: '预警' },
  danger: { class: 'status-danger animate-pulse-red', text: '超标' }
};

export function StatusBadge({ status, type, text }: StatusBadgeProps) {
  let config = { class: '', text: text || '' };
  
  switch (type) {
    case 'device':
      config = deviceStatusMap[status as DeviceStatus] || config;
      break;
    case 'alert':
      if (['general', 'serious', 'emergency'].includes(status)) {
        config = alertLevelMap[status as AlertLevel];
      } else {
        config = alertStatusMap[status as AlertStatus];
      }
      break;
    case 'environment':
      config = environmentStatusMap[status] || config;
      break;
  }

  return (
    <motion.span
      initial={{ scale: 0.8, opacity: 0 }}
      animate={{ scale: 1, opacity: 1 }}
      transition={{ duration: 0.2 }}
      className={config.class}
    >
      {config.text}
    </motion.span>
  );
}
