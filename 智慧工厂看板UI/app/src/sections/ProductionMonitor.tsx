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
  Line
} from 'recharts';
import { productionKPIs, productionLines, devices, oeeTrendData, aiQualityData, batchHistory } from '@/data/mockData';
import { useCountUp } from '@/hooks/useCountUp';
import { StatusBadge } from '@/components/StatusBadge';
import { ChartCard } from '@/components/ChartCard';
import { 
  Package, 
  Target, 
  TrendingUp, 
  Clock, 
  ScanFace, 
  Eye, 
  UserCheck,
  Video,
  Play,
  ShieldCheck
} from 'lucide-react';
import { Progress } from '@/components/ui/progress';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';

export function ProductionMonitor() {
  const plannedValue = useCountUp(productionKPIs.planned, { duration: 1000, decimals: 0 });
  const actualValue = useCountUp(productionKPIs.actual, { duration: 1000, decimals: 0 });
  const achievementValue = useCountUp(productionKPIs.achievement, { duration: 1000, decimals: 1 });

  const getAchievementColor = (value: number) => {
    if (value >= 90) return 'text-[#27AE60]';
    if (value >= 80) return 'text-[#F39C12]';
    return 'text-[#E74C3C]';
  };

  const getAchievementBg = (value: number) => {
    if (value >= 90) return 'bg-[rgba(39,174,96,0.2)]';
    if (value >= 80) return 'bg-[rgba(243,156,18,0.2)]';
    return 'bg-[rgba(231,76,60,0.2)]';
  };

  return (
    <div className="h-full flex flex-col gap-4">
      {/* Top KPI Cards */}
      <div className="grid grid-cols-3 gap-4">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="kpi-card"
        >
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2 rounded-lg bg-[rgba(54,191,250,0.15)]">
              <Target className="w-5 h-5 text-[#36BFFA]" />
            </div>
            <span className="text-sm text-[#94A3B8]">当日计划产量</span>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-bold text-[#36BFFA]">{plannedValue.toLocaleString()}</span>
            <span className="text-sm text-[#64748B]">件</span>
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.1 }}
          className="kpi-card"
        >
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2 rounded-lg bg-[rgba(22,199,154,0.15)]">
              <Package className="w-5 h-5 text-[#16C79A]" />
            </div>
            <span className="text-sm text-[#94A3B8]">实际产量</span>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-bold text-[#16C79A]">{actualValue.toLocaleString()}</span>
            <span className="text-sm text-[#64748B]">件</span>
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.2 }}
          className={`kpi-card ${getAchievementBg(productionKPIs.achievement)}`}
        >
          <div className="flex items-center gap-3 mb-3">
            <div className={`p-2 rounded-lg ${getAchievementBg(productionKPIs.achievement)}`}>
              <TrendingUp className={`w-5 h-5 ${getAchievementColor(productionKPIs.achievement)}`} />
            </div>
            <span className="text-sm text-[#94A3B8]">达成率</span>
          </div>
          <div className="flex items-baseline gap-2">
            <span className={`text-3xl font-bold ${getAchievementColor(productionKPIs.achievement)}`}>
              {achievementValue}%
            </span>
          </div>
        </motion.div>
      </div>

      {/* Middle Section - OEE & AI Quality Control */}
      <div className="flex-1 grid grid-cols-2 gap-4">
        {/* OEE Chart */}
        <ChartCard title="产线OEE监控" delay={0.3}>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={productionLines} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(148,163,184,0.1)" />
              <XAxis dataKey="name" tick={{ fill: '#94A3B8', fontSize: 12 }} />
              <YAxis tick={{ fill: '#94A3B8', fontSize: 12 }} domain={[0, 100]} />
              <Tooltip 
                contentStyle={{ 
                  backgroundColor: '#1E293B', 
                  border: '1px solid rgba(148,163,184,0.2)',
                  borderRadius: '8px'
                }}
                labelStyle={{ color: '#F1F5F9' }}
              />
              <Bar 
                dataKey="oee" 
                name="OEE(%)" 
                fill="url(#oeeGradient)" 
                radius={[4, 4, 0, 0]}
                animationDuration={1000}
              />
              <defs>
                <linearGradient id="oeeGradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#36BFFA" />
                  <stop offset="100%" stopColor="#16C79A" />
                </linearGradient>
              </defs>
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>

        {/* AI Quality Control */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.4 }}
          className="factory-card factory-card-highlight p-4"
        >
          <h3 className="text-base font-semibold text-[#F1F5F9] mb-3 flex items-center gap-2">
            <ScanFace className="w-4 h-4 text-[#36BFFA]" />
            AI智能质控
            <span className="text-xs px-2 py-0.5 rounded bg-[rgba(39,174,96,0.2)] text-[#27AE60]">
              准确率 {aiQualityData.cleanroomDetection.accuracy}%
            </span>
          </h3>
          
          <Tabs defaultValue="cleanroom" className="w-full">
            <TabsList className="grid w-full grid-cols-3 bg-[rgba(30,41,59,0.6)]">
              <TabsTrigger value="cleanroom" className="text-xs data-[state=active]:bg-[rgba(54,191,250,0.2)] data-[state=active]:text-[#36BFFA]">
                <ShieldCheck className="w-3 h-3 mr-1" />
                洁净服检测
              </TabsTrigger>
              <TabsTrigger value="behavior" className="text-xs data-[state=active]:bg-[rgba(54,191,250,0.2)] data-[state=active]:text-[#36BFFA]">
                <Eye className="w-3 h-3 mr-1" />
                行为分析
              </TabsTrigger>
              <TabsTrigger value="access" className="text-xs data-[state=active]:bg-[rgba(54,191,250,0.2)] data-[state=active]:text-[#36BFFA]">
                <UserCheck className="w-3 h-3 mr-1" />
                准入联动
              </TabsTrigger>
            </TabsList>
            
            <TabsContent value="cleanroom" className="mt-3">
              <div className="grid grid-cols-3 gap-2 mb-3">
                <div className="text-center p-2 rounded-lg bg-[rgba(39,174,96,0.1)]">
                  <p className="text-lg font-bold text-[#27AE60]">{aiQualityData.cleanroomDetection.compliant}</p>
                  <p className="text-xs text-[#94A3B8]">合规人数</p>
                </div>
                <div className="text-center p-2 rounded-lg bg-[rgba(231,76,60,0.1)]">
                  <p className="text-lg font-bold text-[#E74C3C]">{aiQualityData.cleanroomDetection.violations}</p>
                  <p className="text-xs text-[#94A3B8]">违规次数</p>
                </div>
                <div className="text-center p-2 rounded-lg bg-[rgba(54,191,250,0.1)]">
                  <p className="text-lg font-bold text-[#36BFFA]">{aiQualityData.cleanroomDetection.total}</p>
                  <p className="text-xs text-[#94A3B8]">检测总数</p>
                </div>
              </div>
              <div className="space-y-2">
                <p className="text-xs text-[#94A3B8]">今日违规记录</p>
                {aiQualityData.cleanroomDetection.todayViolations.map((v, i) => (
                  <div key={i} className="flex items-center justify-between p-2 rounded bg-[rgba(231,76,60,0.1)] text-xs">
                    <span className="text-[#F1F5F9]">{v.time}</span>
                    <span className="text-[#94A3B8]">{v.location}</span>
                    <span className="text-[#E74C3C]">{v.type}</span>
                  </div>
                ))}
              </div>
            </TabsContent>
            
            <TabsContent value="behavior" className="mt-3">
              <div className="grid grid-cols-2 gap-2 mb-3">
                <div className="text-center p-2 rounded-lg bg-[rgba(39,174,96,0.1)]">
                  <p className="text-lg font-bold text-[#27AE60]">{aiQualityData.behaviorAnalysis.totalEvents - aiQualityData.behaviorAnalysis.violations}</p>
                  <p className="text-xs text-[#94A3B8]">正常行为</p>
                </div>
                <div className="text-center p-2 rounded-lg bg-[rgba(243,156,18,0.1)]">
                  <p className="text-lg font-bold text-[#F39C12]">{aiQualityData.behaviorAnalysis.violations}</p>
                  <p className="text-xs text-[#94A3B8]">违规行为</p>
                </div>
              </div>
              <div className="space-y-2">
                <p className="text-xs text-[#94A3B8]">今日违规事件</p>
                {aiQualityData.behaviorAnalysis.todayEvents.map((e, i) => (
                  <div key={i} className="flex items-center justify-between p-2 rounded bg-[rgba(243,156,18,0.1)] text-xs">
                    <span className="text-[#F1F5F9]">{e.time}</span>
                    <span className="text-[#94A3B8]">{e.location}</span>
                    <span className="text-[#F39C12]">{e.type}</span>
                  </div>
                ))}
              </div>
            </TabsContent>
            
            <TabsContent value="access" className="mt-3">
              <div className="grid grid-cols-3 gap-2 mb-3">
                <div className="text-center p-2 rounded-lg bg-[rgba(39,174,96,0.1)]">
                  <p className="text-lg font-bold text-[#27AE60]">{aiQualityData.faceRecognition.granted}</p>
                  <p className="text-xs text-[#94A3B8]">准入通过</p>
                </div>
                <div className="text-center p-2 rounded-lg bg-[rgba(231,76,60,0.1)]">
                  <p className="text-lg font-bold text-[#E74C3C]">{aiQualityData.faceRecognition.denied}</p>
                  <p className="text-xs text-[#94A3B8]">准入拒绝</p>
                </div>
                <div className="text-center p-2 rounded-lg bg-[rgba(54,191,250,0.1)]">
                  <p className="text-lg font-bold text-[#36BFFA]">{aiQualityData.faceRecognition.totalAccess}</p>
                  <p className="text-xs text-[#94A3B8]">总通行</p>
                </div>
              </div>
              <div className="space-y-2">
                <p className="text-xs text-[#94A3B8]">准入拒绝记录</p>
                {aiQualityData.faceRecognition.deniedReasons.map((r, i) => (
                  <div key={i} className="flex items-center justify-between p-2 rounded bg-[rgba(231,76,60,0.1)] text-xs">
                    <span className="text-[#F1F5F9]">{r.time}</span>
                    <span className="text-[#94A3B8]">{r.name}</span>
                    <span className="text-[#E74C3C]">{r.reason}</span>
                  </div>
                ))}
              </div>
            </TabsContent>
          </Tabs>
        </motion.div>
      </div>

      {/* Bottom Section - Batch Traceability & Device Status */}
      <div className="grid grid-cols-3 gap-4">
        {/* Batch Tracking with Video */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.5 }}
          className="factory-card factory-card-highlight p-4 col-span-1"
        >
          <h3 className="text-base font-semibold text-[#F1F5F9] mb-3 flex items-center gap-2">
            <Video className="w-4 h-4 text-[#36BFFA]" />
            全链条视频批次追溯
          </h3>
          <div className="space-y-2 mb-3">
            <div className="flex justify-between text-sm">
              <span className="text-[#94A3B8]">批次号</span>
              <span className="text-[#36BFFA] font-mono text-xs">BT-20260402-001</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-[#94A3B8]">产品系列</span>
              <span className="text-[#F1F5F9]">A系列产品</span>
            </div>
            <div className="space-y-1">
              <div className="flex justify-between text-xs">
                <span className="text-[#94A3B8]">工序进度</span>
                <span className="text-[#36BFFA]">5/8</span>
              </div>
              <Progress value={62.5} className="h-1.5 bg-[rgba(148,163,184,0.2)]" />
            </div>
          </div>
          
          {/* Process Timeline */}
          <div className="space-y-1 max-h-[120px] overflow-y-auto scrollbar-thin">
            <p className="text-xs text-[#94A3B8] mb-1">工序视频记录</p>
            {batchHistory.map((step, index) => (
              <div key={index} className="flex items-center gap-2 p-1.5 rounded text-xs">
                <div className={`w-2 h-2 rounded-full ${
                  step.status === 'completed' ? 'bg-[#27AE60]' :
                  step.status === 'processing' ? 'bg-[#36BFFA] animate-pulse' :
                  'bg-[#6B7280]'
                }`} />
                <span className="text-[#F1F5F9] flex-1">{step.process}</span>
                {step.status !== 'pending' && (
                  <button className="flex items-center gap-1 text-[#36BFFA] hover:text-[#0EA5E9]">
                    <Play className="w-3 h-3" />
                    <span className="text-[10px]">{step.time}</span>
                  </button>
                )}
              </div>
            ))}
          </div>
        </motion.div>

        {/* OEE Trend */}
        <ChartCard title="OEE趋势 (今日)" delay={0.6} className="col-span-1">
          <ResponsiveContainer width="100%" height={150}>
            <LineChart data={oeeTrendData} margin={{ top: 5, right: 30, left: 0, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(148,163,184,0.1)" />
              <XAxis dataKey="time" tick={{ fill: '#94A3B8', fontSize: 10 }} />
              <YAxis tick={{ fill: '#94A3B8', fontSize: 10 }} domain={[70, 100]} />
              <Tooltip 
                contentStyle={{ 
                  backgroundColor: '#1E293B', 
                  border: '1px solid rgba(148,163,184,0.2)',
                  borderRadius: '8px'
                }}
                labelStyle={{ color: '#F1F5F9' }}
              />
              <Line type="monotone" dataKey="lineA" name="产线A" stroke="#36BFFA" strokeWidth={2} dot={false} />
              <Line type="monotone" dataKey="lineB" name="产线B" stroke="#16C79A" strokeWidth={2} dot={false} />
              <Line type="monotone" dataKey="lineC" name="产线C" stroke="#A855F7" strokeWidth={2} dot={false} />
            </LineChart>
          </ResponsiveContainer>
        </ChartCard>

        {/* Device Status */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.7 }}
          className="factory-card factory-card-highlight p-4"
        >
          <h3 className="text-base font-semibold text-[#F1F5F9] mb-3 flex items-center gap-2">
            <Clock className="w-4 h-4 text-[#36BFFA]" />
            设备状态
          </h3>
          <div className="grid grid-cols-2 gap-2 max-h-[180px] overflow-y-auto scrollbar-thin">
            {devices.map((device, index) => (
              <motion.div
                key={device.id}
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ duration: 0.3, delay: 0.8 + index * 0.05 }}
                className={`p-2 rounded-lg border cursor-pointer transition-all hover:scale-105 ${
                  device.status === 'running' ? 'bg-[rgba(39,174,96,0.1)] border-[rgba(39,174,96,0.3)]' :
                  device.status === 'standby' ? 'bg-[rgba(54,191,250,0.1)] border-[rgba(54,191,250,0.3)]' :
                  device.status === 'fault' ? 'bg-[rgba(231,76,60,0.1)] border-[rgba(231,76,60,0.3)] animate-pulse-red' :
                  'bg-[rgba(107,114,128,0.1)] border-[rgba(107,114,128,0.3)]'
                }`}
              >
                <div className="flex items-center justify-between mb-1">
                  <span className="text-[10px] text-[#94A3B8]">{device.line}</span>
                  <StatusBadge status={device.status} type="device" />
                </div>
                <p className="text-xs font-medium text-[#F1F5F9] truncate">{device.name}</p>
                {device.faultCode && (
                  <p className="text-[10px] text-[#E74C3C] mt-0.5">{device.faultCode}</p>
                )}
              </motion.div>
            ))}
          </div>
        </motion.div>
      </div>
    </div>
  );
}
