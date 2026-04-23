import { motion } from 'framer-motion';
import { 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer,
  AreaChart,
  Area
} from 'recharts';
import { environmentZones, utilityData, emissionData } from '@/data/mockData';
import { StatusBadge } from '@/components/StatusBadge';
import { ChartCard } from '@/components/ChartCard';
import { 
  Thermometer, 
  Droplets, 
  Wind, 
  Microscope, 
  CheckCircle2, 
  AlertTriangle, 
  XCircle,
  Droplet,
  Cloud,
  Factory,
  Waves,
  Leaf
} from 'lucide-react';
import { useCountUp } from '@/hooks/useCountUp';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';



const particleTrendData = [
  { time: '08:00', particles05: 280, particles5: 25 },
  { time: '09:00', particles05: 320, particles5: 28 },
  { time: '10:00', particles05: 350, particles5: 32 },
  { time: '11:00', particles05: 380, particles5: 35 },
  { time: '12:00', particles05: 420, particles5: 38 },
  { time: '13:00', particles05: 390, particles5: 34 },
  { time: '14:00', particles05: 350, particles5: 30 },
];

export function EnvironmentMonitor() {
  const normalZones = environmentZones.filter(z => z.status === 'normal').length;
  const warningZones = environmentZones.filter(z => z.status === 'warning').length;
  const dangerZones = environmentZones.filter(z => z.status === 'danger').length;

  const normalValue = useCountUp(normalZones, { duration: 800 });
  const warningValue = useCountUp(warningZones, { duration: 800 });
  const dangerValue = useCountUp(dangerZones, { duration: 800 });

  return (
    <div className="h-full flex flex-col gap-4">
      {/* Top KPI Cards */}
      <div className="grid grid-cols-3 gap-4">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="kpi-card bg-[rgba(39,174,96,0.1)] border-[rgba(39,174,96,0.3)]"
        >
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2 rounded-lg bg-[rgba(39,174,96,0.2)]">
              <CheckCircle2 className="w-5 h-5 text-[#27AE60]" />
            </div>
            <span className="text-sm text-[#94A3B8]">合规区域</span>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-bold text-[#27AE60]">{normalValue}</span>
            <span className="text-sm text-[#64748B]">个</span>
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.1 }}
          className="kpi-card bg-[rgba(243,156,18,0.1)] border-[rgba(243,156,18,0.3)]"
        >
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2 rounded-lg bg-[rgba(243,156,18,0.2)]">
              <AlertTriangle className="w-5 h-5 text-[#F39C12]" />
            </div>
            <span className="text-sm text-[#94A3B8]">预警区域</span>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-bold text-[#F39C12]">{warningValue}</span>
            <span className="text-sm text-[#64748B]">个</span>
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.2 }}
          className="kpi-card bg-[rgba(231,76,60,0.1)] border-[rgba(231,76,60,0.3)]"
        >
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2 rounded-lg bg-[rgba(231,76,60,0.2)]">
              <XCircle className="w-5 h-5 text-[#E74C3C]" />
            </div>
            <span className="text-sm text-[#94A3B8]">超标区域</span>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-bold text-[#E74C3C]">{dangerValue}</span>
            <span className="text-sm text-[#64748B]">个</span>
          </div>
        </motion.div>
      </div>

      {/* Middle Section */}
      <div className="flex-1 grid grid-cols-3 gap-4">
        {/* Environment Zones Table */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.3 }}
          className="factory-card factory-card-highlight p-4 col-span-1 overflow-auto"
        >
          <h3 className="text-base font-semibold text-[#F1F5F9] mb-4 flex items-center gap-2">
            <Thermometer className="w-4 h-4 text-[#36BFFA]" />
            洁净环境监测
          </h3>
          <div className="space-y-3">
            {environmentZones.map((zone, index) => (
              <motion.div
                key={zone.id}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.3, delay: 0.4 + index * 0.05 }}
                className={`p-3 rounded-lg border cursor-pointer transition-all hover:scale-[1.02] ${
                  zone.status === 'normal' ? 'bg-[rgba(39,174,96,0.05)] border-[rgba(39,174,96,0.2)]' :
                  zone.status === 'warning' ? 'bg-[rgba(243,156,18,0.05)] border-[rgba(243,156,18,0.2)]' :
                  'bg-[rgba(231,76,60,0.05)] border-[rgba(231,76,60,0.2)]'
                }`}
              >
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm font-medium text-[#F1F5F9]">{zone.name}</span>
                  <StatusBadge status={zone.status} type="environment" />
                </div>
                <div className="grid grid-cols-2 gap-2 text-xs">
                  <div className="flex items-center gap-1">
                    <Thermometer className="w-3 h-3 text-[#94A3B8]" />
                    <span className="text-[#94A3B8]">{zone.temperature}°C</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <Droplets className="w-3 h-3 text-[#94A3B8]" />
                    <span className="text-[#94A3B8]">{zone.humidity}%</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <Wind className="w-3 h-3 text-[#94A3B8]" />
                    <span className="text-[#94A3B8]">{zone.pressure}Pa</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <Microscope className="w-3 h-3 text-[#94A3B8]" />
                    <span className="text-[#94A3B8]">{zone.particles05}/m³</span>
                  </div>
                </div>
              </motion.div>
            ))}
          </div>
        </motion.div>

        {/* Utility Engineering & Emission Monitor */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.4 }}
          className="factory-card factory-card-highlight p-4 col-span-1 overflow-auto"
        >
          <Tabs defaultValue="utility" className="w-full">
            <TabsList className="grid w-full grid-cols-2 bg-[rgba(30,41,59,0.6)] mb-3">
              <TabsTrigger value="utility" className="text-xs data-[state=active]:bg-[rgba(54,191,250,0.2)] data-[state=active]:text-[#36BFFA]">
                <Factory className="w-3 h-3 mr-1" />
                公用工程
              </TabsTrigger>
              <TabsTrigger value="emission" className="text-xs data-[state=active]:bg-[rgba(54,191,250,0.2)] data-[state=active]:text-[#36BFFA]">
                <Leaf className="w-3 h-3 mr-1" />
                环保排放
              </TabsTrigger>
            </TabsList>
            
            <TabsContent value="utility" className="mt-0">
              <div className="space-y-3">
                {/* Purified Water */}
                <div className="p-3 rounded-lg bg-[rgba(54,191,250,0.05)] border border-[rgba(54,191,250,0.2)]">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <Droplet className="w-4 h-4 text-[#36BFFA]" />
                      <span className="text-sm font-medium text-[#F1F5F9]">{utilityData.purifiedWater.name}</span>
                    </div>
                    <span className="text-xs px-2 py-0.5 rounded bg-[rgba(39,174,96,0.2)] text-[#27AE60]">运行中</span>
                  </div>
                  <div className="grid grid-cols-2 gap-2 text-xs">
                    <span className="text-[#94A3B8]">流量: {utilityData.purifiedWater.flowRate} L/h</span>
                    <span className="text-[#94A3B8]">电导率: {utilityData.purifiedWater.conductivity} μS/cm</span>
                    <span className="text-[#94A3B8]">TOC: {utilityData.purifiedWater.toc} mg/L</span>
                    <span className="text-[#94A3B8]">温度: {utilityData.purifiedWater.temperature}°C</span>
                  </div>
                </div>
                
                {/* WFI */}
                <div className="p-3 rounded-lg bg-[rgba(22,199,154,0.05)] border border-[rgba(22,199,154,0.2)]">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <Droplets className="w-4 h-4 text-[#16C79A]" />
                      <span className="text-sm font-medium text-[#F1F5F9]">{utilityData.wfi.name}</span>
                    </div>
                    <span className="text-xs px-2 py-0.5 rounded bg-[rgba(39,174,96,0.2)] text-[#27AE60]">运行中</span>
                  </div>
                  <div className="grid grid-cols-2 gap-2 text-xs">
                    <span className="text-[#94A3B8]">流量: {utilityData.wfi.flowRate} L/h</span>
                    <span className="text-[#94A3B8]">电导率: {utilityData.wfi.conductivity} μS/cm</span>
                    <span className="text-[#94A3B8]">TOC: {utilityData.wfi.toc} mg/L</span>
                    <span className="text-[#94A3B8]">温度: {utilityData.wfi.temperature}°C</span>
                  </div>
                </div>
                
                {/* Compressed Air */}
                <div className="p-3 rounded-lg bg-[rgba(243,156,18,0.05)] border border-[rgba(243,156,18,0.2)]">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <Wind className="w-4 h-4 text-[#F39C12]" />
                      <span className="text-sm font-medium text-[#F1F5F9]">{utilityData.compressedAir.name}</span>
                    </div>
                    <span className="text-xs px-2 py-0.5 rounded bg-[rgba(39,174,96,0.2)] text-[#27AE60]">运行中</span>
                  </div>
                  <div className="grid grid-cols-2 gap-2 text-xs">
                    <span className="text-[#94A3B8]">压力: {utilityData.compressedAir.pressure} MPa</span>
                    <span className="text-[#94A3B8]">露点: {utilityData.compressedAir.dewPoint}°C</span>
                    <span className="text-[#94A3B8]">油含量: {utilityData.compressedAir.oilContent} mg/m³</span>
                    <span className="text-[#94A3B8]">流量: {utilityData.compressedAir.flowRate} m³/h</span>
                  </div>
                </div>
                
                {/* Nitrogen */}
                <div className="p-3 rounded-lg bg-[rgba(168,85,247,0.05)] border border-[rgba(168,85,247,0.2)]">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <Cloud className="w-4 h-4 text-[#A855F7]" />
                      <span className="text-sm font-medium text-[#F1F5F9]">{utilityData.nitrogen.name}</span>
                    </div>
                    <span className="text-xs px-2 py-0.5 rounded bg-[rgba(39,174,96,0.2)] text-[#27AE60]">运行中</span>
                  </div>
                  <div className="grid grid-cols-2 gap-2 text-xs">
                    <span className="text-[#94A3B8]">压力: {utilityData.nitrogen.pressure} MPa</span>
                    <span className="text-[#94A3B8]">纯度: {utilityData.nitrogen.purity}%</span>
                    <span className="text-[#94A3B8]">流量: {utilityData.nitrogen.flowRate} m³/h</span>
                  </div>
                </div>
              </div>
            </TabsContent>
            
            <TabsContent value="emission" className="mt-0">
              <div className="space-y-3">
                {/* Wastewater */}
                <div className="p-3 rounded-lg bg-[rgba(54,191,250,0.05)] border border-[rgba(54,191,250,0.2)]">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <Waves className="w-4 h-4 text-[#36BFFA]" />
                      <span className="text-sm font-medium text-[#F1F5F9]">废水排放</span>
                    </div>
                    <span className="text-xs px-2 py-0.5 rounded bg-[rgba(39,174,96,0.2)] text-[#27AE60]">达标</span>
                  </div>
                  <div className="grid grid-cols-2 gap-2 text-xs">
                    <span className="text-[#94A3B8]">pH: {emissionData.wastewater.ph}</span>
                    <span className="text-[#94A3B8]">COD: {emissionData.wastewater.cod} mg/L</span>
                    <span className="text-[#94A3B8]">氨氮: {emissionData.wastewater.ammonia} mg/L</span>
                    <span className="text-[#94A3B8]">总氮: {emissionData.wastewater.totalN} mg/L</span>
                  </div>
                </div>
                
                {/* Exhaust Gas */}
                <div className="p-3 rounded-lg bg-[rgba(168,85,247,0.05)] border border-[rgba(168,85,247,0.2)]">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <Cloud className="w-4 h-4 text-[#A855F7]" />
                      <span className="text-sm font-medium text-[#F1F5F9]">废气排放</span>
                    </div>
                    <span className="text-xs px-2 py-0.5 rounded bg-[rgba(39,174,96,0.2)] text-[#27AE60]">达标</span>
                  </div>
                  <div className="grid grid-cols-2 gap-2 text-xs">
                    <span className="text-[#94A3B8]">颗粒物: {emissionData.exhaust.particulates} mg/m³</span>
                    <span className="text-[#94A3B8]">VOCs: {emissionData.exhaust.voc} mg/m³</span>
                    <span className="text-[#94A3B8]">SO₂: {emissionData.exhaust.so2} mg/m³</span>
                    <span className="text-[#94A3B8]">NOx: {emissionData.exhaust.nox} mg/m³</span>
                  </div>
                </div>
                
                <div className="p-3 rounded-lg bg-[rgba(39,174,96,0.05)] border border-[rgba(39,174,96,0.2)]">
                  <p className="text-xs text-[#94A3B8] mb-2">排放合规状态</p>
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded-full bg-[#27AE60] animate-pulse" />
                    <span className="text-sm text-[#27AE60]">所有排放指标均符合环保法规要求</span>
                  </div>
                  <p className="text-xs text-[#64748B] mt-2">上次检测: 2026-04-02 08:00:00</p>
                </div>
              </div>
            </TabsContent>
          </Tabs>
        </motion.div>

        {/* Particle Trend */}
        <ChartCard title="悬浮粒子趋势" delay={0.5} className="col-span-1">
          <ResponsiveContainer width="100%" height={200}>
            <AreaChart data={particleTrendData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(148,163,184,0.1)" />
              <XAxis dataKey="time" tick={{ fill: '#94A3B8', fontSize: 12 }} />
              <YAxis tick={{ fill: '#94A3B8', fontSize: 12 }} />
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
                dataKey="particles05" 
                name="0.5μm粒子" 
                stroke="#36BFFA" 
                fill="url(#particleGradient1)"
                strokeWidth={2}
              />
              <Area 
                type="monotone" 
                dataKey="particles5" 
                name="5μm粒子" 
                stroke="#16C79A" 
                fill="url(#particleGradient2)"
                strokeWidth={2}
              />
              <defs>
                <linearGradient id="particleGradient1" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#36BFFA" stopOpacity={0.3} />
                  <stop offset="100%" stopColor="#36BFFA" stopOpacity={0.05} />
                </linearGradient>
                <linearGradient id="particleGradient2" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#16C79A" stopOpacity={0.3} />
                  <stop offset="100%" stopColor="#16C79A" stopOpacity={0.05} />
                </linearGradient>
              </defs>
            </AreaChart>
          </ResponsiveContainer>
        </ChartCard>
      </div>

      {/* Bottom Section - GMP Standards */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, delay: 0.6 }}
        className="factory-card factory-card-highlight p-4"
      >
        <h3 className="text-base font-semibold text-[#F1F5F9] mb-4">GMP洁净区标准参考</h3>
        <div className="grid grid-cols-4 gap-4">
          <div className="p-3 rounded-lg bg-[rgba(30,41,59,0.6)]">
            <p className="text-sm font-medium text-[#A855F7] mb-2">百级洁净区</p>
            <div className="space-y-1 text-xs text-[#94A3B8]">
              <p>温度: 20-24°C</p>
              <p>湿度: 45-60%</p>
              <p>0.5μm: ≤3,520/m³</p>
              <p>5μm: ≤29/m³</p>
            </div>
          </div>
          <div className="p-3 rounded-lg bg-[rgba(30,41,59,0.6)]">
            <p className="text-sm font-medium text-[#36BFFA] mb-2">千级洁净区</p>
            <div className="space-y-1 text-xs text-[#94A3B8]">
              <p>温度: 20-24°C</p>
              <p>湿度: 45-60%</p>
              <p>0.5μm: ≤35,200/m³</p>
              <p>5μm: ≤293/m³</p>
            </div>
          </div>
          <div className="p-3 rounded-lg bg-[rgba(30,41,59,0.6)]">
            <p className="text-sm font-medium text-[#16C79A] mb-2">万级洁净区</p>
            <div className="space-y-1 text-xs text-[#94A3B8]">
              <p>温度: 20-24°C</p>
              <p>湿度: 45-60%</p>
              <p>0.5μm: ≤352,000/m³</p>
              <p>5μm: ≤2,930/m³</p>
            </div>
          </div>
          <div className="p-3 rounded-lg bg-[rgba(30,41,59,0.6)]">
            <p className="text-sm font-medium text-[#F39C12] mb-2">压差要求</p>
            <div className="space-y-1 text-xs text-[#94A3B8]">
              <p>相邻房间: ≥5Pa</p>
              <p>洁净区-非洁净区: ≥10Pa</p>
              <p>换气次数: ≥15次/h</p>
              <p>流向: 高洁净→低洁净</p>
            </div>
          </div>
        </div>
      </motion.div>
    </div>
  );
}
