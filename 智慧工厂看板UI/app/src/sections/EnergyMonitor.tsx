import { motion } from 'framer-motion';
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer,
  LineChart,
  Line,
  PieChart,
  Pie,
  Cell,
  Legend,
  AreaChart,
  Area
} from 'recharts';
import { energyData, energyTrendData, energyDistributionData, carbonData, aiEnergySuggestions } from '@/data/mockData';
import { ChartCard } from '@/components/ChartCard';
import { 
  Zap, 
  Droplets, 
  Wind, 
  Flame, 
  TrendingUp, 
  TrendingDown,
  Leaf,
  Lightbulb,
  Target,
  AlertTriangle
} from 'lucide-react';
import { useCountUp } from '@/hooks/useCountUp';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';

const energyIcons: Record<string, React.ComponentType<{ className?: string; style?: React.CSSProperties }>> = {
  electricity: Zap,
  water: Droplets,
  steam: Flame,
  air: Wind
};

const energyColors: Record<string, string> = {
  electricity: '#36BFFA',
  water: '#16C79A',
  steam: '#F39C12',
  air: '#A855F7'
};

// Carbon emission trend data
const carbonTrendData = [
  { date: '03-28', emission: 8.2, target: 9.0 },
  { date: '03-29', emission: 8.5, target: 9.0 },
  { date: '03-30', emission: 8.3, target: 9.0 },
  { date: '03-31', emission: 8.8, target: 9.0 },
  { date: '04-01', emission: 8.4, target: 9.0 },
  { date: '04-02', emission: 8.52, target: 9.0 },
];

export function EnergyMonitor() {
  const carbonProgress = useCountUp(carbonData.progress, { duration: 1000, decimals: 1 });
  const carbonDaily = useCountUp(carbonData.daily, { duration: 1000, decimals: 2 });
  const carbonMonthly = useCountUp(carbonData.monthly, { duration: 1000, decimals: 1 });

  return (
    <div className="h-full flex flex-col gap-4">
      {/* Top Energy Cards */}
      <div className="grid grid-cols-4 gap-4">
        {energyData.map((energy, index) => {
          const Icon = energyIcons[energy.type];
          const color = energyColors[energy.type];
          const dailyValue = useCountUp(energy.daily, { duration: 1000, decimals: 0 });
          
          return (
            <motion.div
              key={energy.type}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: index * 0.1 }}
              className="kpi-card"
              style={{ borderColor: `${color}40` }}
            >
              <div className="flex items-center gap-3 mb-3">
                <div 
                  className="p-2 rounded-lg"
                  style={{ backgroundColor: `${color}20` }}
                >
                  <Icon className="w-5 h-5" style={{ color }} />
                </div>
                <span className="text-sm text-[#94A3B8]">{energy.name}消耗 (今日)</span>
              </div>
              <div className="flex items-baseline gap-2">
                <span className="text-2xl font-bold" style={{ color }}>
                  {dailyValue.toLocaleString()}
                </span>
                <span className="text-sm text-[#64748B]">{energy.unit}</span>
              </div>
              <div className="flex items-center gap-4 mt-2 text-xs">
                <div className={`flex items-center gap-1 ${energy.yoy >= 0 ? 'text-[#E74C3C]' : 'text-[#27AE60]'}`}>
                  {energy.yoy >= 0 ? <TrendingUp className="w-3 h-3" /> : <TrendingDown className="w-3 h-3" />}
                  <span>同比 {energy.yoy >= 0 ? '+' : ''}{energy.yoy}%</span>
                </div>
                <div className={`flex items-center gap-1 ${energy.mom >= 0 ? 'text-[#E74C3C]' : 'text-[#27AE60]'}`}>
                  {energy.mom >= 0 ? <TrendingUp className="w-3 h-3" /> : <TrendingDown className="w-3 h-3" />}
                  <span>环比 {energy.mom >= 0 ? '+' : ''}{energy.mom}%</span>
                </div>
              </div>
            </motion.div>
          );
        })}
      </div>

      {/* Middle Section */}
      <div className="flex-1 grid grid-cols-3 gap-4">
        {/* Energy Comparison */}
        <ChartCard title="车间能耗对比 (kWh)" delay={0.4}>
          <ResponsiveContainer width="100%" height={180}>
            <BarChart data={energyDistributionData} layout="vertical" margin={{ top: 5, right: 30, left: 40, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(148,163,184,0.1)" horizontal={false} />
              <XAxis type="number" tick={{ fill: '#94A3B8', fontSize: 12 }} />
              <YAxis dataKey="name" type="category" tick={{ fill: '#94A3B8', fontSize: 12 }} width={60} />
              <Tooltip 
                contentStyle={{ 
                  backgroundColor: '#1E293B', 
                  border: '1px solid rgba(148,163,184,0.2)',
                  borderRadius: '8px'
                }}
                labelStyle={{ color: '#F1F5F9' }}
              />
              <Bar dataKey="value" name="能耗占比(%)" radius={[0, 4, 4, 0]}>
                {energyDistributionData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>

        {/* Carbon Management - Enhanced */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.5 }}
          className="factory-card factory-card-highlight p-4"
        >
          <h3 className="text-base font-semibold text-[#F1F5F9] mb-3 flex items-center gap-2">
            <Leaf className="w-4 h-4 text-[#27AE60]" />
            碳排放管理
            <span className="text-xs px-2 py-0.5 rounded bg-[rgba(39,174,96,0.2)] text-[#27AE60]">自动核算</span>
          </h3>
          
          <Tabs defaultValue="overview" className="w-full">
            <TabsList className="grid w-full grid-cols-2 bg-[rgba(30,41,59,0.6)] mb-3">
              <TabsTrigger value="overview" className="text-xs data-[state=active]:bg-[rgba(54,191,250,0.2)] data-[state=active]:text-[#36BFFA]">
                <Target className="w-3 h-3 mr-1" />
                总览
              </TabsTrigger>
              <TabsTrigger value="trend" className="text-xs data-[state=active]:bg-[rgba(54,191,250,0.2)] data-[state=active]:text-[#36BFFA]">
                <TrendingUp className="w-3 h-3 mr-1" />
                趋势
              </TabsTrigger>
            </TabsList>
            
            <TabsContent value="overview" className="mt-0">
              <div className="space-y-3">
                {/* Carbon Gauge */}
                <div className="relative">
                  <ResponsiveContainer width="100%" height={100}>
                    <PieChart>
                      <Pie
                        data={[
                          { name: '已排放', value: carbonData.progress },
                          { name: '剩余额度', value: 100 - carbonData.progress }
                        ]}
                        cx="50%"
                        cy="100%"
                        startAngle={180}
                        endAngle={0}
                        innerRadius={40}
                        outerRadius={55}
                        paddingAngle={0}
                        dataKey="value"
                      >
                        <Cell fill="#27AE60" />
                        <Cell fill="rgba(148,163,184,0.2)" />
                      </Pie>
                    </PieChart>
                  </ResponsiveContainer>
                  <div className="absolute bottom-2 left-1/2 -translate-x-1/2 text-center">
                    <p className="text-xl font-bold text-[#27AE60]">{carbonProgress}%</p>
                    <p className="text-[10px] text-[#94A3B8]">减排进度</p>
                  </div>
                </div>

                {/* Carbon Data */}
                <div className="grid grid-cols-3 gap-2">
                  <div className="text-center p-2 rounded-lg bg-[rgba(30,41,59,0.6)]">
                    <p className="text-lg font-bold text-[#F1F5F9]">{carbonDaily}</p>
                    <p className="text-[10px] text-[#94A3B8]">今日(tCO₂)</p>
                  </div>
                  <div className="text-center p-2 rounded-lg bg-[rgba(30,41,59,0.6)]">
                    <p className="text-lg font-bold text-[#F1F5F9]">{carbonMonthly}</p>
                    <p className="text-[10px] text-[#94A3B8]">本月(tCO₂)</p>
                  </div>
                  <div className="text-center p-2 rounded-lg bg-[rgba(30,41,59,0.6)]">
                    <p className="text-lg font-bold text-[#36BFFA]">{carbonData.target}</p>
                    <p className="text-[10px] text-[#94A3B8]">目标(tCO₂)</p>
                  </div>
                </div>
              </div>
            </TabsContent>
            
            <TabsContent value="trend" className="mt-0">
              <ResponsiveContainer width="100%" height={150}>
                <AreaChart data={carbonTrendData} margin={{ top: 5, right: 30, left: 0, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(148,163,184,0.1)" />
                  <XAxis dataKey="date" tick={{ fill: '#94A3B8', fontSize: 10 }} />
                  <YAxis tick={{ fill: '#94A3B8', fontSize: 10 }} />
                  <Tooltip 
                    contentStyle={{ 
                      backgroundColor: '#1E293B', 
                      border: '1px solid rgba(148,163,184,0.2)',
                      borderRadius: '8px'
                    }}
                    labelStyle={{ color: '#F1F5F9' }}
                  />
                  <Area 
                    type="monotone" 
                    dataKey="emission" 
                    name="实际排放" 
                    stroke="#27AE60" 
                    fill="url(#carbonGradient)"
                    strokeWidth={2}
                  />
                  <Line type="monotone" dataKey="target" name="目标线" stroke="#F39C12" strokeDasharray="5 5" strokeWidth={2} dot={false} />
                  <defs>
                    <linearGradient id="carbonGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#27AE60" stopOpacity={0.3} />
                      <stop offset="100%" stopColor="#27AE60" stopOpacity={0.05} />
                    </linearGradient>
                  </defs>
                </AreaChart>
              </ResponsiveContainer>
            </TabsContent>
          </Tabs>
        </motion.div>

        {/* AI Energy Optimization */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.6 }}
          className="factory-card factory-card-highlight p-4"
        >
          <h3 className="text-base font-semibold text-[#F1F5F9] mb-3 flex items-center gap-2">
            <Lightbulb className="w-4 h-4 text-[#F39C12]" />
            AI能效优化
            <span className="text-xs px-2 py-0.5 rounded bg-[rgba(243,156,18,0.2)] text-[#F39C12]">ML驱动</span>
          </h3>
          
          <div className="space-y-2 max-h-[200px] overflow-y-auto scrollbar-thin">
            {aiEnergySuggestions.map((suggestion, index) => (
              <motion.div
                key={suggestion.id}
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.3, delay: 0.7 + index * 0.1 }}
                className={`p-2 rounded-lg border ${
                  suggestion.priority === 'high' 
                    ? 'bg-[rgba(231,76,60,0.05)] border-[rgba(231,76,60,0.2)]' 
                    : 'bg-[rgba(243,156,18,0.05)] border-[rgba(243,156,18,0.2)]'
                }`}
              >
                <div className="flex items-start justify-between mb-1">
                  <div className="flex items-center gap-1">
                    {suggestion.type === 'electricity' && <Zap className="w-3 h-3 text-[#36BFFA]" />}
                    {suggestion.type === 'water' && <Droplets className="w-3 h-3 text-[#16C79A]" />}
                    {suggestion.type === 'steam' && <Flame className="w-3 h-3 text-[#F39C12]" />}
                    {suggestion.type === 'air' && <Wind className="w-3 h-3 text-[#A855F7]" />}
                    <span className="text-xs font-medium text-[#F1F5F9]">{suggestion.title}</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <span className="text-xs text-[#27AE60]">省{suggestion.potentialSaving}</span>
                    {suggestion.priority === 'high' && <AlertTriangle className="w-3 h-3 text-[#E74C3C]" />}
                  </div>
                </div>
                <p className="text-[10px] text-[#94A3B8] line-clamp-2">{suggestion.description}</p>
              </motion.div>
            ))}
          </div>
          
          <div className="mt-3 p-2 rounded-lg bg-[rgba(39,174,96,0.1)] border border-[rgba(39,174,96,0.2)]">
            <div className="flex items-center justify-between">
              <span className="text-xs text-[#94A3B8]">预计月度节能</span>
              <span className="text-sm font-bold text-[#27AE60]">12,580 kWh</span>
            </div>
            <div className="flex items-center justify-between mt-1">
              <span className="text-xs text-[#94A3B8]">预计节约成本</span>
              <span className="text-sm font-bold text-[#27AE60]">¥ 8,650</span>
            </div>
          </div>
        </motion.div>
      </div>

      {/* Bottom Section - Energy Trend */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, delay: 0.7 }}
        className="factory-card factory-card-highlight p-4"
      >
        <h3 className="text-base font-semibold text-[#F1F5F9] mb-3 flex items-center gap-2">
          <TrendingUp className="w-4 h-4 text-[#36BFFA]" />
          能耗趋势 (近7日)
        </h3>
        <ResponsiveContainer width="100%" height={150}>
          <LineChart data={energyTrendData} margin={{ top: 5, right: 30, left: 0, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(148,163,184,0.1)" />
            <XAxis dataKey="date" tick={{ fill: '#94A3B8', fontSize: 12 }} />
            <YAxis tick={{ fill: '#94A3B8', fontSize: 12 }} />
            <Tooltip 
              contentStyle={{ 
                backgroundColor: '#1E293B', 
                border: '1px solid rgba(148,163,184,0.2)',
                borderRadius: '8px'
              }}
              labelStyle={{ color: '#F1F5F9' }}
            />
            <Legend wrapperStyle={{ color: '#94A3B8', fontSize: 12 }} />
            <Line type="monotone" dataKey="electricity" name="电(kWh)" stroke="#36BFFA" strokeWidth={2} dot={false} />
            <Line type="monotone" dataKey="water" name="水(m³)" stroke="#16C79A" strokeWidth={2} dot={false} />
            <Line type="monotone" dataKey="steam" name="蒸汽(t)" stroke="#F39C12" strokeWidth={2} dot={false} />
            <Line type="monotone" dataKey="air" name="压缩空气(m³)" stroke="#A855F7" strokeWidth={2} dot={false} />
          </LineChart>
        </ResponsiveContainer>
      </motion.div>
    </div>
  );
}
