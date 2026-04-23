import { motion } from 'framer-motion';
import type { ReactNode } from 'react';

interface ChartCardProps {
  title: string;
  children: ReactNode;
  className?: string;
  delay?: number;
}

export function ChartCard({ title, children, className = '', delay = 0 }: ChartCardProps) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ 
        duration: 0.5, 
        delay,
        ease: [0.25, 0.1, 0.25, 1]
      }}
      className={`factory-card factory-card-highlight p-4 ${className}`}
    >
      <h3 className="text-base font-semibold text-[#F1F5F9] mb-4">{title}</h3>
      {children}
    </motion.div>
  );
}
