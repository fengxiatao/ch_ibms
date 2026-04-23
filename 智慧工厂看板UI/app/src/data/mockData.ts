import type { 
  KPIData, 
  Device, 
  Alert, 
  ProductionLine, 
  BatchInfo,
  EnvironmentZone,
  SafetyDevice,
  DrillPlan,
  EnergyData,
  CarbonData,
  Camera,
  Building3D
} from '@/types';

// Global KPI Data - 新增GMP合规、碳排放
export const globalKPIs: KPIData[] = [
  {
    label: '厂区总人数',
    value: 1247,
    unit: '人',
    trend: 2.3,
    trendUp: true,
    icon: 'Users',
    status: 'normal'
  },
  {
    label: '设备运行率',
    value: 94.6,
    unit: '%',
    trend: 1.2,
    trendUp: true,
    icon: 'Activity',
    status: 'normal'
  },
  {
    label: '当日产量',
    value: 15680,
    unit: '件',
    trend: 5.8,
    trendUp: true,
    icon: 'Package',
    status: 'normal'
  },
  {
    label: '异常报警数',
    value: 3,
    unit: '个',
    trend: 2,
    trendUp: false,
    icon: 'AlertTriangle',
    status: 'warning'
  },
  {
    label: 'GMP合规区域',
    value: 24,
    unit: '个',
    trend: 0,
    trendUp: true,
    icon: 'ShieldCheck',
    status: 'normal'
  },
  {
    label: '碳排放实时',
    value: 8.52,
    unit: 't',
    trend: 3.2,
    trendUp: false,
    icon: 'Leaf',
    status: 'normal'
  }
];

// Production KPIs
export const productionKPIs = {
  planned: 18000,
  actual: 15680,
  achievement: 87.1
};

// Production Lines OEE Data
export const productionLines: ProductionLine[] = [
  { id: 'L1', name: '产线A', oee: 92.5, availability: 95.2, performance: 96.8, quality: 99.1 },
  { id: 'L2', name: '产线B', oee: 88.3, availability: 91.5, performance: 94.2, quality: 98.5 },
  { id: 'L3', name: '产线C', oee: 94.1, availability: 96.8, performance: 97.5, quality: 99.3 },
  { id: 'L4', name: '产线D', oee: 85.7, availability: 89.3, performance: 93.1, quality: 97.8 },
  { id: 'L5', name: '产线E', oee: 91.2, availability: 93.8, performance: 95.6, quality: 98.9 },
];

// Current Batch Info - 全链条视频批次追溯
export const currentBatch: BatchInfo = {
  id: 'BT-20260402-001',
  name: '产品批次-A系列',
  currentProcess: '组装工序',
  completedProcesses: 5,
  totalProcesses: 8,
  quality: 98.2
};

// Batch History for Video Traceability
export const batchHistory = [
  { process: '原料入库', time: '08:00', operator: '张三', videoId: 'V001', status: 'completed' },
  { process: '配料称量', time: '08:30', operator: '李四', videoId: 'V002', status: 'completed' },
  { process: '混合搅拌', time: '09:15', operator: '王五', videoId: 'V003', status: 'completed' },
  { process: '注塑成型', time: '10:00', operator: '赵六', videoId: 'V004', status: 'completed' },
  { process: '冷却定型', time: '11:30', operator: '系统', videoId: 'V005', status: 'completed' },
  { process: '组装工序', time: '13:00', operator: '孙七', videoId: 'V006', status: 'processing' },
  { process: '质量检测', time: '-', operator: '-', videoId: 'V007', status: 'pending' },
  { process: '包装入库', time: '-', operator: '-', videoId: 'V008', status: 'pending' },
];

// AI Quality Control Data - AI智能质控
export const aiQualityData = {
  cleanroomDetection: {
    total: 1247,
    compliant: 1238,
    violations: 9,
    accuracy: 99.9,
    todayViolations: [
      { time: '09:23:15', location: '洁净区-A入口', type: '口罩未佩戴', image: 'capture_001' },
      { time: '10:45:22', location: '洁净区-B', type: '手套破损', image: 'capture_002' },
      { time: '11:12:08', location: '洁净区-C', type: '洁净服不规范', image: 'capture_003' },
    ]
  },
  behaviorAnalysis: {
    totalEvents: 156,
    violations: 3,
    todayEvents: [
      { time: '08:15:33', location: '产线A', type: '跨越隔离线', severity: 'warning' },
      { time: '13:42:18', location: '质检区', type: '违规接触设备', severity: 'serious' },
    ]
  },
  faceRecognition: {
    totalAccess: 2156,
    granted: 2148,
    denied: 8,
    deniedReasons: [
      { time: '07:45:12', name: '王某某', reason: '洁净服检测不合格' },
      { time: '08:30:45', name: '李某某', reason: '权限不足' },
    ]
  }
};

// Devices Data
export const devices: Device[] = [
  { id: 'D1', name: '注塑机-01', status: 'running', line: '产线A' },
  { id: 'D2', name: '注塑机-02', status: 'running', line: '产线A' },
  { id: 'D3', name: '冲压机-01', status: 'standby', line: '产线B' },
  { id: 'D4', name: '冲压机-02', status: 'running', line: '产线B' },
  { id: 'D5', name: '焊接机-01', status: 'fault', line: '产线C', faultCode: 'E-203', faultTime: '14:23:15' },
  { id: 'D6', name: '组装机-01', status: 'running', line: '产线C' },
  { id: 'D7', name: '检测机-01', status: 'running', line: '产线D' },
  { id: 'D8', name: '包装机-01', status: 'offline', line: '产线D' },
  { id: 'D9', name: '注塑机-03', status: 'running', line: '产线E' },
  { id: 'D10', name: '冲压机-03', status: 'running', line: '产线E' },
];

// Alerts Data
export const alerts: Alert[] = [
  { id: 'A1', level: 'emergency', message: '焊接机-01故障停机', location: '产线C', time: '14:23:15', status: 'pending', type: '设备故障' },
  { id: 'A2', level: 'serious', message: '包装机-01离线', location: '产线D', time: '13:45:22', status: 'processing', type: '设备离线' },
  { id: 'A3', level: 'general', message: '产线D产能不达标', location: '产线D', time: '12:30:00', status: 'pending', type: '产能异常' },
  { id: 'A4', level: 'general', message: '洁净区温度超标', location: '洁净区-B', time: '11:15:33', status: 'resolved', type: '环境异常' },
  { id: 'A5', level: 'serious', message: '消防栓压力不足', location: '仓库区', time: '10:22:18', status: 'processing', type: '安全异常' },
];

// Environment Zones Data
export const environmentZones: EnvironmentZone[] = [
  { id: 'Z1', name: '洁净区-A', level: '百级', temperature: 22.1, humidity: 45.2, pressure: 15.2, particles05: 120, particles5: 2, bacteria: 1, status: 'normal' },
  { id: 'Z2', name: '洁净区-B', level: '千级', temperature: 23.5, humidity: 48.6, pressure: 12.8, particles05: 350, particles5: 8, bacteria: 3, status: 'warning' },
  { id: 'Z3', name: '洁净区-C', level: '万级', temperature: 21.8, humidity: 52.1, pressure: 10.5, particles05: 2800, particles5: 35, bacteria: 8, status: 'normal' },
  { id: 'Z4', name: '洁净区-D', level: '千级', temperature: 22.3, humidity: 46.5, pressure: 13.2, particles05: 420, particles5: 12, bacteria: 4, status: 'normal' },
  { id: 'Z5', name: '洁净区-E', level: '百级', temperature: 21.9, humidity: 44.8, pressure: 16.1, particles05: 98, particles5: 1, bacteria: 0, status: 'normal' },
  { id: 'Z6', name: '洁净区-F', level: '万级', temperature: 24.2, humidity: 55.3, pressure: 9.8, particles05: 3200, particles5: 42, bacteria: 10, status: 'warning' },
];

// Utility Engineering Data - 公用工程监控
export const utilityData = {
  purifiedWater: {
    name: '纯化水',
    status: 'running',
    flowRate: 1250,
    conductivity: 1.2,
    toc: 0.45,
    temperature: 25.2
  },
  wfi: {
    name: '注射用水',
    status: 'running',
    flowRate: 680,
    conductivity: 0.8,
    toc: 0.15,
    temperature: 70.5
  },
  compressedAir: {
    name: '压缩空气',
    status: 'running',
    pressure: 0.72,
    dewPoint: -42.5,
    oilContent: 0.001,
    flowRate: 5680
  },
  nitrogen: {
    name: '氮气',
    status: 'running',
    pressure: 0.65,
    purity: 99.999,
    flowRate: 2450
  }
};

// Environmental Emission Data - 环保排放监测
export const emissionData = {
  wastewater: {
    ph: 7.2,
    cod: 45,
    ammonia: 2.1,
    totalN: 8.5,
    status: 'normal'
  },
  exhaust: {
    particulates: 12,
    voc: 18,
    so2: 5,
    nox: 28,
    status: 'normal'
  }
};

// Safety Devices Data
export const safetyDevices: SafetyDevice[] = [
  { id: 'S1', name: '灭火器-MF1', type: '灭火器', location: '车间A-入口', status: 'normal', installDate: '2024-01-15', lastMaintenance: '2026-03-01' },
  { id: 'S2', name: '消防栓-XS1', type: '消防栓', location: '车间A-中部', status: 'fault', installDate: '2024-01-15', lastMaintenance: '2026-02-15' },
  { id: 'S3', name: '烟感-YG1', type: '烟感探测器', location: '车间A-顶部', status: 'normal', installDate: '2024-02-01', lastMaintenance: '2026-03-10' },
  { id: 'S4', name: '温感-WG1', type: '温感探测器', location: '仓库区-入口', status: 'normal', installDate: '2024-02-01', lastMaintenance: '2026-03-10' },
  { id: 'S5', name: '喷淋-PL1', type: '喷淋系统', location: '车间B-顶部', status: 'normal', installDate: '2024-01-20', lastMaintenance: '2026-02-28' },
  { id: 'S6', name: '灭火器-MF2', type: '灭火器', location: '仓库区-角落', status: 'expired', installDate: '2023-03-10', lastMaintenance: '2025-03-10' },
];

// Drill Plans Data
export const drillPlans: DrillPlan[] = [
  { id: 'DR1', name: '消防应急演练', plannedDate: '2026-01-15', completedDate: '2026-01-15', status: 'completed', passRate: 95 },
  { id: 'DR2', name: '疏散逃生演练', plannedDate: '2026-04-20', status: 'planned' },
  { id: 'DR3', name: '化学品泄漏演练', plannedDate: '2026-07-10', status: 'planned' },
  { id: 'DR4', name: '设备故障应急演练', plannedDate: '2026-10-05', status: 'planned' },
];

// Spatiotemporal Trajectory Data - 时空轨迹追踪
export const trajectoryData = {
  personnel: [
    { id: 'P001', name: '张三', currentLocation: '产线A', entryTime: '07:45:12', trajectory: ['入口', '更衣室', '产线A'] },
    { id: 'P002', name: '李四', currentLocation: '质检区', entryTime: '08:00:33', trajectory: ['入口', '更衣室', '洁净区-B', '质检区'] },
    { id: 'P003', name: '王五', currentLocation: '仓库', entryTime: '08:15:45', trajectory: ['入口', '仓库'] },
  ],
  materials: [
    { batchId: 'BT-20260402-001', material: '原料A', currentLocation: '产线A', trajectory: ['原料库', '配料区', '产线A'], timestamp: '2026-04-02 08:00:00' },
    { batchId: 'BT-20260402-002', material: '原料B', currentLocation: '配料区', trajectory: ['原料库', '配料区'], timestamp: '2026-04-02 09:30:00' },
  ],
  devices: [
    { id: 'D1', name: '注塑机-01', currentLocation: '产线A', status: 'running', runtime: 1245 },
    { id: 'D5', name: '焊接机-01', currentLocation: '产线C', status: 'fault', downtime: 45 },
  ]
};

// Explosion-proof Area Data - 防爆区专属管控
export const explosionProofData = {
  gasConcentration: {
    current: 12,
    threshold: 25,
    unit: 'LEL%',
    status: 'normal',
    trend: [-2, -1, 0, 1, 2, 1, 0, -1, 0, 1]
  },
  personnel: {
    count: 3,
    list: [
      { id: 'P101', name: '赵六', entryTime: '08:30:00', workDuration: 360 },
      { id: 'P102', name: '钱七', entryTime: '09:00:00', workDuration: 180 },
      { id: 'P103', name: '孙八', entryTime: '09:15:00', workDuration: 135 },
    ]
  },
  devices: [
    { name: '防爆风机-01', status: 'running', runtime: 7200 },
    { name: '防爆照明-01', status: 'running', runtime: 7200 },
    { name: '气体检测仪-01', status: 'running', battery: 85 },
  ]
};

// Energy Data
export const energyData: EnergyData[] = [
  { type: 'electricity', name: '电', daily: 15280, monthly: 458400, yoy: 5.2, mom: -2.1, unit: 'kWh' },
  { type: 'water', name: '水', daily: 2850, monthly: 85500, yoy: 3.8, mom: 1.5, unit: 'm³' },
  { type: 'steam', name: '蒸汽', daily: 1250, monthly: 37500, yoy: 8.5, mom: 4.2, unit: 't' },
  { type: 'air', name: '压缩空气', daily: 5680, monthly: 170400, yoy: 2.3, mom: -1.8, unit: 'm³' },
];

// Carbon Data
export const carbonData: CarbonData = {
  daily: 8.52,
  monthly: 255.6,
  target: 300,
  progress: 85.2
};

// AI Energy Optimization Suggestions - AI能效优化建议
export const aiEnergySuggestions = [
  {
    id: 'E001',
    type: 'electricity',
    title: '产线B电力优化',
    description: '14:00-16:00时段用电量异常，建议检查设备运行参数',
    potentialSaving: '8%',
    priority: 'high'
  },
  {
    id: 'E002',
    type: 'water',
    title: '冷却系统节水',
    description: '冷却系统水循环效率可提升，建议清洗换热器',
    potentialSaving: '15%',
    priority: 'medium'
  },
  {
    id: 'E003',
    type: 'steam',
    title: '蒸汽管道保温',
    description: '蒸汽管道存在热量损失，建议加强保温措施',
    potentialSaving: '10%',
    priority: 'medium'
  },
  {
    id: 'E004',
    type: 'air',
    title: '压缩空气泄漏检测',
    description: '检测到3处潜在泄漏点，建议及时维修',
    potentialSaving: '12%',
    priority: 'high'
  }
];

// Cameras Data - 新增AR增强信息
export const cameras: Camera[] = [
  { id: 'C1', name: '入口摄像头-01', location: '厂区主入口', status: 'online', position: { x: -50, y: 5, z: -30 }, type: 'high' },
  { id: 'C2', name: '产线A摄像头', location: '产线A-工位1', status: 'online', position: { x: -20, y: 4, z: -10 }, type: 'low' },
  { id: 'C3', name: '产线B摄像头', location: '产线B-工位2', status: 'online', position: { x: 0, y: 4, z: -10 }, type: 'low' },
  { id: 'C4', name: '质检区摄像头', location: '质检区域', status: 'online', position: { x: 20, y: 4, z: -5 }, type: 'low' },
  { id: 'C5', name: '仓库摄像头', location: '仓储区域', status: 'online', position: { x: 40, y: 6, z: 10 }, type: 'high' },
  { id: 'C6', name: '洁净区摄像头', location: '洁净区-B', status: 'online', position: { x: -10, y: 4, z: 10 }, type: 'low' },
  { id: 'C7', name: '防爆区摄像头', location: '防爆区', status: 'online', position: { x: 30, y: 4, z: 20 }, type: 'low' },
];

// 3D Buildings Data
export const buildings3D: Building3D[] = [
  { id: 'B1', name: '生产车间A', type: 'workshop', position: [-20, 0, -10], size: [30, 12, 20], color: '#1E293B', borderColor: '#36BFFA' },
  { id: 'B2', name: '生产车间B', type: 'workshop', position: [10, 0, -10], size: [25, 12, 18], color: '#1E293B', borderColor: '#36BFFA' },
  { id: 'B3', name: '洁净车间', type: 'cleanroom', position: [-10, 0, 10], size: [20, 10, 15], color: '#1E293B', borderColor: '#A855F7' },
  { id: 'B4', name: '防爆车间', type: 'explosion', position: [30, 0, 15], size: [15, 10, 12], color: '#1E293B', borderColor: '#E74C3C' },
  { id: 'B5', name: '危化品库', type: 'hazmat', position: [45, 0, 20], size: [12, 8, 10], color: '#1E293B', borderColor: '#EA580C' },
  { id: 'B6', name: '仓库', type: 'warehouse', position: [40, 0, 5], size: [25, 10, 20], color: '#1E293B', borderColor: '#F39C12' },
  { id: 'B7', name: '动力中心', type: 'power', position: [-40, 0, 5], size: [15, 8, 12], color: '#1E293B', borderColor: '#27AE60' },
  { id: 'B8', name: '办公楼', type: 'office', position: [-50, 0, -25], size: [20, 15, 12], color: '#1E293B', borderColor: '#36BFFA' },
];

// Chart Data - OEE Trend
export const oeeTrendData = [
  { time: '08:00', lineA: 88, lineB: 85, lineC: 90, lineD: 82, lineE: 87 },
  { time: '09:00', lineA: 90, lineB: 86, lineC: 91, lineD: 83, lineE: 88 },
  { time: '10:00', lineA: 91, lineB: 87, lineC: 92, lineD: 84, lineE: 89 },
  { time: '11:00', lineA: 92, lineB: 88, lineC: 93, lineD: 85, lineE: 90 },
  { time: '12:00', lineA: 89, lineB: 85, lineC: 90, lineD: 82, lineE: 87 },
  { time: '13:00', lineA: 91, lineB: 87, lineC: 92, lineD: 84, lineE: 89 },
  { time: '14:00', lineA: 92, lineB: 88, lineC: 94, lineD: 85, lineE: 91 },
];

// Chart Data - Energy Trend
export const energyTrendData = [
  { date: '03-28', electricity: 14500, water: 2700, steam: 1180, air: 5400 },
  { date: '03-29', electricity: 15200, water: 2800, steam: 1220, air: 5600 },
  { date: '03-30', electricity: 14800, water: 2750, steam: 1200, air: 5500 },
  { date: '03-31', electricity: 15500, water: 2900, steam: 1280, air: 5800 },
  { date: '04-01', electricity: 15000, water: 2820, steam: 1240, air: 5650 },
  { date: '04-02', electricity: 15280, water: 2850, steam: 1250, air: 5680 },
];

// Chart Data - Energy Distribution
export const energyDistributionData = [
  { name: '产线A', value: 28, color: '#36BFFA' },
  { name: '产线B', value: 22, color: '#16C79A' },
  { name: '产线C', value: 20, color: '#A855F7' },
  { name: '产线D', value: 15, color: '#F39C12' },
  { name: '产线E', value: 10, color: '#27AE60' },
  { name: '其他', value: 5, color: '#6B7280' },
];

// Gas Concentration Trend for Explosion-proof Area
export const gasConcentrationTrend = [
  { time: '08:00', value: 10 },
  { time: '09:00', value: 11 },
  { time: '10:00', value: 12 },
  { time: '11:00', value: 13 },
  { time: '12:00', value: 12 },
  { time: '13:00', value: 11 },
  { time: '14:00', value: 12 },
];
