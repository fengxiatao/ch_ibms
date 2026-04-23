import { motion } from 'framer-motion';
import { 
  LineChart, 
  Line, 
  ResponsiveContainer 
} from 'recharts';
import { safetyDevices, drillPlans, trajectoryData, explosionProofData, gasConcentrationTrend } from '@/data/mockData';
import { StatusBadge } from '@/components/StatusBadge';
import { 
  Shield, 
  FireExtinguisher, 
  AlertTriangle, 
  CheckCircle2, 
  Calendar,
  MapPin,
  Wrench,
  Clock,
  Video,
  Users,
  Package,
  Activity,
  Flame,
  Wind,
  Radio,
  Eye
} from 'lucide-react';
import { Progress } from '@/components/ui/progress';
import { useCountUp } from '@/hooks/useCountUp';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';

export function SafetyMonitor() {
  const normalDevices = safetyDevices.filter(d => d.status === 'normal').length;
  const faultDevices = safetyDevices.filter(d => d.status === 'fault').length;
  const expiredDevices = safetyDevices.filter(d => d.status === 'expired').length;
  
  const completedDrills = drillPlans.filter(d => d.status === 'completed').length;
  const totalDrills = drillPlans.length;
  const drillProgress = (completedDrills / totalDrills) * 100;

  const normalValue = useCountUp(normalDevices, { duration: 800 });
  const faultValue = useCountUp(faultDevices, { duration: 800 });
  const expiredValue = useCountUp(expiredDevices, { duration: 800 });

  const getDeviceIcon = (type: string) => {
    switch (type) {
      case '灭火器':
        return <FireExtinguisher className="w-4 h-4" />;
      case '消防栓':
        return <Shield className="w-4 h-4" />;
      default:
        return <CheckCircle2 className="w-4 h-4" />;
    }
  };

  return (
    <div className="h-full flex flex-col gap-4">
      {/* Top KPI Cards */}
      <div className="grid grid-cols-4 gap-4">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="kpi-card"
        >
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2 rounded-lg bg-[rgba(39,174,96,0.2)]">
              <CheckCircle2 className="w-5 h-5 text-[#27AE60]" />
            </div>
            <span className="text-sm text-[#94A3B8]">正常设备</span>
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
          className="kpi-card bg-[rgba(231,76,60,0.1)] border-[rgba(231,76,60,0.3)]"
        >
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2 rounded-lg bg-[rgba(231,76,60,0.2)]">
              <AlertTriangle className="w-5 h-5 text-[#E74C3C]" />
            </div>
            <span className="text-sm text-[#94A3B8]">故障设备</span>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-bold text-[#E74C3C]">{faultValue}</span>
            <span className="text-sm text-[#64748B]">个</span>
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.2 }}
          className="kpi-card bg-[rgba(243,156,18,0.1)] border-[rgba(243,156,18,0.3)]"
        >
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2 rounded-lg bg-[rgba(243,156,18,0.2)]">
              <Clock className="w-5 h-5 text-[#F39C12]" />
            </div>
            <span className="text-sm text-[#94A3B8]">过期设备</span>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-bold text-[#F39C12]">{expiredValue}</span>
            <span className="text-sm text-[#64748B]">个</span>
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.3 }}
          className="kpi-card"
        >
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2 rounded-lg bg-[rgba(54,191,250,0.2)]">
              <Calendar className="w-5 h-5 text-[#36BFFA]" />
            </div>
            <span className="text-sm text-[#94A3B8]">演练完成率</span>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-bold text-[#36BFFA]">{completedDrills}/{totalDrills}</span>
          </div>
          <Progress value={drillProgress} className="h-1.5 mt-2 bg-[rgba(148,163,184,0.2)]" />
        </motion.div>
      </div>

      {/* Middle Section - 立体化云防 & 时空轨迹 */}
      <div className="flex-1 grid grid-cols-3 gap-4">
        {/* 立体化云防 - 整合安防系统 */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.4 }}
          className="factory-card factory-card-highlight p-4"
        >
          <h3 className="text-base font-semibold text-[#F1F5F9] mb-3 flex items-center gap-2">
            <Video className="w-4 h-4 text-[#36BFFA]" />
            立体化云防
            <span className="text-xs px-2 py-0.5 rounded bg-[rgba(39,174,96,0.2)] text-[#27AE60]">在线</span>
          </h3>
          
          <div className="space-y-3">
            {/* 安防系统状态 */}
            <div className="grid grid-cols-2 gap-2">
              <div className="p-2 rounded-lg bg-[rgba(39,174,96,0.1)] border border-[rgba(39,174,96,0.2)]">
                <div className="flex items-center gap-2 mb-1">
                  <Video className="w-3 h-3 text-[#27AE60]" />
                  <span className="text-xs text-[#94A3B8]">视频监控</span>
                </div>
                <p className="text-lg font-bold text-[#27AE60]">24/24</p>
              </div>
              <div className="p-2 rounded-lg bg-[rgba(39,174,96,0.1)] border border-[rgba(39,174,96,0.2)]">
                <div className="flex items-center gap-2 mb-1">
                  <Radio className="w-3 h-3 text-[#27AE60]" />
                  <span className="text-xs text-[#94A3B8]">入侵检测</span>
                </div>
                <p className="text-lg font-bold text-[#27AE60]">正常</p>
              </div>
              <div className="p-2 rounded-lg bg-[rgba(39,174,96,0.1)] border border-[rgba(39,174,96,0.2)]">
                <div className="flex items-center gap-2 mb-1">
                  <Eye className="w-3 h-3 text-[#27AE60]" />
                  <span className="text-xs text-[#94A3B8]">电子巡更</span>
                </div>
                <p className="text-lg font-bold text-[#27AE60]">8/8</p>
              </div>
              <div className="p-2 rounded-lg bg-[rgba(39,174,96,0.1)] border border-[rgba(39,174,96,0.2)]">
                <div className="flex items-center gap-2 mb-1">
                  <Shield className="w-3 h-3 text-[#27AE60]" />
                  <span className="text-xs text-[#94A3B8]">门禁系统</span>
                </div>
                <p className="text-lg font-bold text-[#27AE60]">正常</p>
              </div>
            </div>
            
            {/* AR增强功能 */}
            <div className="p-3 rounded-lg bg-[rgba(54,191,250,0.05)] border border-[rgba(54,191,250,0.2)]">
              <p className="text-xs text-[#94A3B8] mb-2">AR视频增强功能</p>
              <div className="space-y-1 text-xs">
                <div className="flex items-center gap-2">
                  <div className="w-1.5 h-1.5 rounded-full bg-[#36BFFA]" />
                  <span className="text-[#F1F5F9]">设备信息叠加显示</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-1.5 h-1.5 rounded-full bg-[#36BFFA]" />
                  <span className="text-[#F1F5F9]">报警标签实时标注</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-1.5 h-1.5 rounded-full bg-[#36BFFA]" />
                  <span className="text-[#F1F5F9]">人员轨迹追踪显示</span>
                </div>
              </div>
            </div>
            
            {/* 高低点联动 */}
            <div className="p-3 rounded-lg bg-[rgba(168,85,247,0.05)] border border-[rgba(168,85,247,0.2)]">
              <div className="flex items-center justify-between">
                <span className="text-xs text-[#94A3B8]">高低点联动</span>
                <span className="text-xs text-[#A855F7]">已启用</span>
              </div>
              <p className="text-xs text-[#64748B] mt-1">高点鸟瞰全局，低点查看细节</p>
            </div>
          </div>
        </motion.div>

        {/* 时空轨迹追踪 */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.5 }}
          className="factory-card factory-card-highlight p-4"
        >
          <h3 className="text-base font-semibold text-[#F1F5F9] mb-3 flex items-center gap-2">
            <Activity className="w-4 h-4 text-[#36BFFA]" />
            时空轨迹追踪
          </h3>
          
          <Tabs defaultValue="personnel" className="w-full">
            <TabsList className="grid w-full grid-cols-3 bg-[rgba(30,41,59,0.6)] mb-3">
              <TabsTrigger value="personnel" className="text-xs data-[state=active]:bg-[rgba(54,191,250,0.2)] data-[state=active]:text-[#36BFFA]">
                <Users className="w-3 h-3 mr-1" />
                人员
              </TabsTrigger>
              <TabsTrigger value="materials" className="text-xs data-[state=active]:bg-[rgba(54,191,250,0.2)] data-[state=active]:text-[#36BFFA]">
                <Package className="w-3 h-3 mr-1" />
                物料
              </TabsTrigger>
              <TabsTrigger value="devices" className="text-xs data-[state=active]:bg-[rgba(54,191,250,0.2)] data-[state=active]:text-[#36BFFA]">
                <Activity className="w-3 h-3 mr-1" />
                设备
              </TabsTrigger>
            </TabsList>
            
            <TabsContent value="personnel" className="mt-0">
              <div className="space-y-2 max-h-[180px] overflow-y-auto scrollbar-thin">
                {trajectoryData.personnel.map((p, i) => (
                  <div key={i} className="p-2 rounded-lg bg-[rgba(30,41,59,0.6)] text-xs">
                    <div className="flex items-center justify-between mb-1">
                      <span className="text-[#F1F5F9] font-medium">{p.name}</span>
                      <span className="text-[#36BFFA]">{p.currentLocation}</span>
                    </div>
                    <div className="flex items-center gap-1 text-[#64748B]">
                      <span>轨迹:</span>
                      <span className="text-[#94A3B8]">{p.trajectory.join(' → ')}</span>
                    </div>
                    <div className="text-[#64748B] mt-1">入场: {p.entryTime}</div>
                  </div>
                ))}
              </div>
            </TabsContent>
            
            <TabsContent value="materials" className="mt-0">
              <div className="space-y-2 max-h-[180px] overflow-y-auto scrollbar-thin">
                {trajectoryData.materials.map((m, i) => (
                  <div key={i} className="p-2 rounded-lg bg-[rgba(30,41,59,0.6)] text-xs">
                    <div className="flex items-center justify-between mb-1">
                      <span className="text-[#F1F5F9] font-medium">{m.batchId}</span>
                      <span className="text-[#36BFFA]">{m.currentLocation}</span>
                    </div>
                    <div className="flex items-center gap-1 text-[#64748B]">
                      <span>轨迹:</span>
                      <span className="text-[#94A3B8]">{m.trajectory.join(' → ')}</span>
                    </div>
                    <div className="text-[#64748B] mt-1">{m.timestamp}</div>
                  </div>
                ))}
              </div>
            </TabsContent>
            
            <TabsContent value="devices" className="mt-0">
              <div className="space-y-2 max-h-[180px] overflow-y-auto scrollbar-thin">
                {trajectoryData.devices.map((d, i) => (
                  <div key={i} className="p-2 rounded-lg bg-[rgba(30,41,59,0.6)] text-xs">
                    <div className="flex items-center justify-between mb-1">
                      <span className="text-[#F1F5F9] font-medium">{d.name}</span>
                      <span className={`${d.status === 'running' ? 'text-[#27AE60]' : 'text-[#E74C3C]'}`}>
                        {d.status === 'running' ? '运行中' : '故障'}
                      </span>
                    </div>
                    <div className="text-[#94A3B8]">位置: {d.currentLocation}</div>
                    <div className="text-[#64748B] mt-1">
                      {d.status === 'running' ? `运行时长: ${d.runtime}分钟` : `停机时长: ${d.downtime}分钟`}
                    </div>
                  </div>
                ))}
              </div>
            </TabsContent>
          </Tabs>
        </motion.div>

        {/* 防爆区专属管控 */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.6 }}
          className="factory-card factory-card-highlight p-4"
        >
          <h3 className="text-base font-semibold text-[#F1F5F9] mb-3 flex items-center gap-2">
            <Flame className="w-4 h-4 text-[#E74C3C]" />
            防爆区管控
            <span className="text-xs px-2 py-0.5 rounded bg-[rgba(39,174,96,0.2)] text-[#27AE60]">正常</span>
          </h3>
          
          <div className="space-y-3">
            {/* 可燃气体浓度 */}
            <div className="p-3 rounded-lg bg-[rgba(30,41,59,0.6)]">
              <div className="flex items-center justify-between mb-2">
                <div className="flex items-center gap-2">
                  <Wind className="w-4 h-4 text-[#F39C12]" />
                  <span className="text-sm text-[#94A3B8]">可燃气体浓度</span>
                </div>
                <span className="text-lg font-bold text-[#27AE60]">{explosionProofData.gasConcentration.current} <span className="text-xs font-normal text-[#64748B]">LEL%</span></span>
              </div>
              <div className="h-16">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={gasConcentrationTrend}>
                    <Line type="monotone" dataKey="value" stroke="#F39C12" strokeWidth={2} dot={false} />
                  </LineChart>
                </ResponsiveContainer>
              </div>
              <div className="flex justify-between text-xs mt-1">
                <span className="text-[#64748B]">阈值: {explosionProofData.gasConcentration.threshold} LEL%</span>
                <span className="text-[#27AE60]">安全范围内</span>
              </div>
            </div>
            
            {/* 区域人员 */}
            <div className="p-3 rounded-lg bg-[rgba(30,41,59,0.6)]">
              <div className="flex items-center justify-between mb-2">
                <div className="flex items-center gap-2">
                  <Users className="w-4 h-4 text-[#36BFFA]" />
                  <span className="text-sm text-[#94A3B8]">区域内人员</span>
                </div>
                <span className="text-lg font-bold text-[#36BFFA]">{explosionProofData.personnel.count} <span className="text-xs font-normal text-[#64748B]">人</span></span>
              </div>
              <div className="space-y-1">
                {explosionProofData.personnel.list.map((p, i) => (
                  <div key={i} className="flex items-center justify-between text-xs">
                    <span className="text-[#F1F5F9]">{p.name}</span>
                    <span className="text-[#94A3B8]">工作时长: {Math.floor(p.workDuration / 60)}h{p.workDuration % 60}m</span>
                  </div>
                ))}
              </div>
            </div>
            
            {/* 防爆设备 */}
            <div className="space-y-1">
              <p className="text-xs text-[#94A3B8]">防爆设备状态</p>
              {explosionProofData.devices.map((d, i) => (
                <div key={i} className="flex items-center justify-between p-2 rounded bg-[rgba(39,174,96,0.1)] text-xs">
                  <span className="text-[#F1F5F9]">{d.name}</span>
                  <div className="flex items-center gap-2">
                    <span className="text-[#27AE60]">运行中</span>
                    {'battery' in d && <span className="text-[#94A3B8]">电量: {d.battery}%</span>}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </motion.div>
      </div>

      {/* Bottom Section - Safety Devices & Drill Plans */}
      <div className="grid grid-cols-2 gap-4">
        {/* Safety Devices */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.7 }}
          className="factory-card factory-card-highlight p-4"
        >
          <h3 className="text-base font-semibold text-[#F1F5F9] mb-3 flex items-center gap-2">
            <Shield className="w-4 h-4 text-[#36BFFA]" />
            消防设备状态
          </h3>
          <div className="grid grid-cols-3 gap-2">
            {safetyDevices.map((device, index) => (
              <motion.div
                key={device.id}
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ duration: 0.3, delay: 0.8 + index * 0.05 }}
                className={`p-2 rounded-lg border cursor-pointer transition-all hover:scale-[1.02] ${
                  device.status === 'normal' ? 'bg-[rgba(39,174,96,0.05)] border-[rgba(39,174,96,0.2)]' :
                  device.status === 'fault' ? 'bg-[rgba(231,76,60,0.05)] border-[rgba(231,76,60,0.2)] animate-pulse-red' :
                  'bg-[rgba(243,156,18,0.05)] border-[rgba(243,156,18,0.2)]'
                }`}
              >
                <div className="flex items-center justify-between mb-1">
                  <div className={`flex items-center gap-1 ${
                    device.status === 'normal' ? 'text-[#27AE60]' :
                    device.status === 'fault' ? 'text-[#E74C3C]' :
                    'text-[#F39C12]'
                  }`}>
                    {getDeviceIcon(device.type)}
                    <span className="text-xs font-medium text-[#F1F5F9]">{device.name}</span>
                  </div>
                </div>
                <div className="space-y-0.5 text-[10px] text-[#94A3B8]">
                  <div className="flex items-center gap-1">
                    <MapPin className="w-3 h-3" />
                    <span>{device.location}</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <Wrench className="w-3 h-3" />
                    <span>{device.lastMaintenance}</span>
                  </div>
                </div>
                <div className="mt-1">
                  <StatusBadge 
                    status={device.status === 'normal' ? 'normal' : device.status === 'fault' ? 'danger' : 'warning'} 
                    type="environment" 
                  />
                </div>
              </motion.div>
            ))}
          </div>
        </motion.div>

        {/* Drill Plans */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.8 }}
          className="factory-card factory-card-highlight p-4"
        >
          <h3 className="text-base font-semibold text-[#F1F5F9] mb-3 flex items-center gap-2">
            <Calendar className="w-4 h-4 text-[#36BFFA]" />
            应急演练计划
          </h3>
          <div className="space-y-2">
            {drillPlans.map((drill, index) => (
              <motion.div
                key={drill.id}
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.3, delay: 0.9 + index * 0.05 }}
                className="p-2 rounded-lg bg-[rgba(30,41,59,0.6)] border border-[rgba(148,163,184,0.15)]"
              >
                <div className="flex items-center justify-between mb-1">
                  <span className="text-sm font-medium text-[#F1F5F9]">{drill.name}</span>
                  <span className={`text-xs px-2 py-0.5 rounded ${
                    drill.status === 'completed' 
                      ? 'bg-[rgba(39,174,96,0.2)] text-[#27AE60]' 
                      : 'bg-[rgba(54,191,250,0.2)] text-[#36BFFA]'
                  }`}>
                    {drill.status === 'completed' ? '已完成' : '计划中'}
                  </span>
                </div>
                <div className="flex items-center justify-between text-xs text-[#94A3B8]">
                  <div className="flex items-center gap-2">
                    <Calendar className="w-3 h-3" />
                    <span>{drill.plannedDate}</span>
                  </div>
                  {drill.passRate && (
                    <span className="text-[#27AE60]">合格率: {drill.passRate}%</span>
                  )}
                </div>
              </motion.div>
            ))}
          </div>
        </motion.div>
      </div>
    </div>
  );
}
