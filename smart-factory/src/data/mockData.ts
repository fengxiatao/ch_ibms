// ==================== 驾驶舱核心指标 ====================
export const dashboardMetrics = {
  equipmentOnlineRate: { value: 96.8, target: 95, unit: '%', trend: '+2.3%' },
  alarmCount: { value: 12, target: 10, unit: '条', trend: '-3条' },
  environmentCompliance: { value: 99.2, target: 100, unit: '%', trend: '达标' },
};

// ==================== 告警数据 ====================
export const alarmStats = {
  emergency: 3,
  important: 5,
  normal: 12,
  info: 20,
};

export const realTimeAlarms = [
  { id: 1, type: 'temperature', title: '温度超标', location: '车间A-产线1', level: 'emergency', time: '2分钟前', status: 'unhandled', cameraId: 'CAM-001' },
  { id: 2, type: 'conductivity', title: '电导率异常', location: '纯水系统', level: 'important', time: '5分钟前', status: 'handling', cameraId: 'CAM-002' },
  { id: 3, type: 'maintenance', title: '维保到期提醒', location: '空压机-01', level: 'info', time: '10分钟前', status: 'unhandled', cameraId: null },
  { id: 4, type: 'energy', title: '能耗异常波动', location: '车间B', level: 'normal', time: '15分钟前', status: 'handled', cameraId: 'CAM-003' },
  { id: 5, type: 'environment', title: '湿度超标', location: '洁净区C', level: 'important', time: '20分钟前', status: 'handling', cameraId: 'CAM-004' },
  { id: 6, type: 'security', title: '周界入侵告警', location: '厂区东侧', level: 'emergency', time: '1分钟前', status: 'unhandled', cameraId: 'CAM-006' },
  { id: 7, type: 'intrusion', title: '区域入侵告警', location: '仓库区域', level: 'emergency', time: '3分钟前', status: 'unhandled', cameraId: 'CAM-005' },
  { id: 8, type: 'behavior', title: '异常聚集检测', location: '办公区入口', level: 'important', time: '8分钟前', status: 'handled', cameraId: 'CAM-007' },
];

// ==================== 视频监控数据 ====================
export const videoList = [
  { id: 'CAM-001', name: '车间A入口', location: '生产车间', status: 'online', type: '半球机', area: 'production' },
  { id: 'CAM-002', name: '制作区全景', location: '制作区', status: 'online', type: '球机', area: 'production' },
  { id: 'CAM-003', name: '灌装区全景', location: '灌装区', status: 'online', type: '球机', area: 'production' },
  { id: 'CAM-004', name: '包装区全景', location: '包装区', status: 'offline', type: '枪机', area: 'production' },
  { id: 'CAM-005', name: '仓库入口', location: '仓储区', status: 'online', type: '枪机', area: 'storage' },
  { id: 'CAM-006', name: '周界东侧', location: '厂区边界', status: 'online', type: '球机', area: 'perimeter' },
  { id: 'CAM-007', name: '周界南侧', location: '厂区边界', status: 'online', type: '球机', area: 'perimeter' },
  { id: 'CAM-008', name: '厂区全景', location: '厂区高点', status: 'online', type: '鹰眼', area: 'overview' },
  { id: 'CAM-009', name: '门禁入口', location: '门禁区域', status: 'online', type: '人脸识别', area: 'security' },
  { id: 'CAM-010', name: '物流入口', location: '物流通道', status: 'online', type: '车牌识别', area: 'security' },
  { id: 'CAM-011', name: '研发区入口', location: '研发楼', status: 'online', type: '枪机', area: 'office' },
  { id: 'CAM-012', name: '食堂区域', location: '食堂', status: 'online', type: '半球机', area: 'office' },
];

// 视频回放记录
export const videoPlaybackRecords = [
  { id: 'REC-001', cameraId: 'CAM-001', startTime: '2024-01-15 08:00', endTime: '2024-01-15 08:30', type: '告警回放', reason: '温度超标事件' },
  { id: 'REC-002', cameraId: 'CAM-006', startTime: '2024-01-15 09:00', endTime: '2024-01-15 09:15', type: '周界入侵', reason: '周界入侵告警' },
  { id: 'REC-003', cameraId: 'CAM-002', startTime: '2024-01-15 10:00', endTime: '2024-01-15 10:20', type: '人工巡检', reason: '定时巡检' },
];

// AI识别记录
export const aiRecognitionRecords = [
  { id: 'AI-001', cameraId: 'CAM-009', type: '人脸识别', person: '员工-张三', time: '08:30:15', status: '识别成功' },
  { id: 'AI-002', cameraId: 'CAM-010', type: '车牌识别', plate: '粤A12345', time: '08:32:22', status: '识别成功' },
  { id: 'AI-003', cameraId: 'CAM-007', type: '行为分析', behavior: '异常聚集', time: '09:15:33', status: '告警触发' },
  { id: 'AI-004', cameraId: 'CAM-006', type: '周界检测', event: '越界告警', time: '09:45:12', status: '告警触发' },
];

// ==================== 立体化云防数据 ====================
export const cloudDefenseDevices = [
  { id: 'CF-001', name: '高清球机-01', type: '球机', location: '厂区东侧', status: 'online', ptz: true, area: 'perimeter' },
  { id: 'CF-002', name: '高清球机-02', type: '球机', location: '厂区南侧', status: 'online', ptz: true, area: 'perimeter' },
  { id: 'CF-003', name: '全景鹰眼', type: '全景', location: '厂区高点', status: 'online', ptz: false, area: 'overview' },
  { id: 'CF-004', name: '热成像相机', type: '热成像', location: '周界', status: 'online', ptz: false, area: 'perimeter' },
  { id: 'CF-005', name: '人脸识别相机', type: '人脸识别', location: '门禁入口', status: 'online', ptz: false, area: 'security' },
  { id: 'CF-006', name: '车牌识别相机', type: '车牌识别', location: '物流入口', status: 'online', ptz: false, area: 'security' },
];

// 防区数据
export const defenseZones = [
  { id: 'ZONE-001', name: '周界防护区', status: 'armed', type: 'perimeter', alarms: 0, devices: 4 },
  { id: 'ZONE-002', name: '仓储防护区', status: 'armed', type: 'storage', alarms: 1, devices: 2 },
  { id: 'ZONE-003', name: '生产防护区', status: 'disarmed', type: 'production', alarms: 0, devices: 3 },
  { id: 'ZONE-004', name: '办公防护区', status: 'disarmed', type: 'office', alarms: 0, devices: 2 },
  { id: 'ZONE-005', name: '重点区域A', status: 'armed', type: 'sensitive', alarms: 0, devices: 2 },
];

// 入侵告警记录
export const intrusionRecords = [
  { id: 'INV-001', zone: 'ZONE-001', type: '周界入侵', location: '东侧围栏', time: '09:45:12', status: '未处理', level: 'emergency' },
  { id: 'INV-002', zone: 'ZONE-002', type: '区域闯入', location: '仓库区域', time: '08:30:45', status: '已处理', level: 'important' },
  { id: 'INV-003', zone: 'ZONE-003', type: '禁区闯入', location: '洁净区', time: '07:15:22', status: '已处理', level: 'normal' },
];

// 行为分析告警
export const behaviorAlarms = [
  { id: 'BEH-001', cameraId: 'CAM-007', type: '异常聚集', location: '办公区入口', time: '09:15:33', count: 8, status: '已处理' },
  { id: 'BEH-002', cameraId: 'CAM-002', type: '奔跑检测', location: '制作区', time: '08:45:11', status: '已处理' },
  { id: 'BEH-003', cameraId: 'CAM-001', type: '跌倒检测', location: '车间A', time: '08:20:05', status: '已确认' },
];

// 轨迹追踪记录
export const trajectoryRecords = [
  { id: 'TRAJ-001', subject: '员工-张三', type: 'person', startTime: '08:00:00', endTime: '17:30:00', path: ['门禁', '办公区', '生产区', '食堂', '门禁'] },
  { id: 'TRAJ-002', subject: '粤A12345', type: 'vehicle', startTime: '08:30:00', endTime: '09:15:00', path: ['物流入口', '仓库', '物流出口'] },
];

// 安全态势评分
export const securitySituation = {
  overallScore: 92,
  trend: '上升',
  factors: [
    { name: '周界防护', score: 95 },
    { name: '区域管控', score: 90 },
    { name: '行为监测', score: 88 },
    { name: '应急响应', score: 94 },
    { name: '设备状态', score: 93 },
  ],
  prediction: '未来24小时安全态势平稳',
};

// ==================== 能耗数据 ====================
export const energyTrendData = [
  { time: '00:00', electricity: 120, water: 80, gas: 60 },
  { time: '02:00', electricity: 110, water: 75, gas: 55 },
  { time: '04:00', electricity: 100, water: 70, gas: 50 },
  { time: '06:00', electricity: 130, water: 85, gas: 65 },
  { time: '08:00', electricity: 180, water: 120, gas: 90 },
  { time: '10:00', electricity: 200, water: 140, gas: 100 },
  { time: '12:00', electricity: 190, water: 130, gas: 95 },
  { time: '14:00', electricity: 210, water: 150, gas: 110 },
  { time: '16:00', electricity: 220, water: 160, gas: 115 },
  { time: '18:00', electricity: 200, water: 140, gas: 100 },
  { time: '20:00', electricity: 170, water: 110, gas: 80 },
  { time: '22:00', electricity: 140, water: 90, gas: 70 },
];

export const energyStats = {
  todayElectricity: 2450,
  todayWater: 180,
  todayGas: 120,
  monthElectricity: 73500,
  monthWater: 5400,
  monthGas: 3600,
  compareLastMonth: -5.2,
};

// ==================== 设备数据 ====================
export const equipmentList = [
  { id: 'EQ-001', name: '空压机-01', type: '空压机', status: 'running', location: '动力车间', runtime: '2023.09', online: true, efficiency: 95, temperature: 45, vibration: 2.1 },
  { id: 'EQ-002', name: '空压机-02', type: '空压机', status: 'running', location: '动力车间', runtime: '2023.09', online: true, efficiency: 92, temperature: 43, vibration: 2.3 },
  { id: 'EQ-003', name: '纯水系统-01', type: '纯水系统', status: 'maintenance', location: '水处理间', runtime: '2023.08', online: false, efficiency: 0, temperature: 0, vibration: 0 },
  { id: 'EQ-004', name: '空调机组-01', type: '空调', status: 'running', location: '车间A', runtime: '2023.07', online: true, efficiency: 90, temperature: 25, vibration: 1.5 },
  { id: 'EQ-005', name: '空调机组-02', type: '空调', status: 'fault', location: '车间B', runtime: '2023.06', online: true, efficiency: 0, temperature: 65, vibration: 8.2 },
  { id: 'EQ-006', name: '乳化罐-01', type: '生产设备', status: 'running', location: '制作区', runtime: '2024.01', online: true, efficiency: 88, temperature: 38, vibration: 1.8 },
  { id: 'EQ-007', name: '灌装机-01', type: '生产设备', status: 'running', location: '灌装区', runtime: '2024.01', online: true, efficiency: 91, temperature: 32, vibration: 1.2 },
  { id: 'EQ-008', name: '包装线-01', type: '包装设备', status: 'stopped', location: '包装区', runtime: '2024.01', online: true, efficiency: 0, temperature: 22, vibration: 0 },
  { id: 'EQ-009', name: '搅拌罐-01', type: '生产设备', status: 'running', location: '制作区', runtime: '2024.01', online: true, efficiency: 86, temperature: 35, vibration: 1.6 },
  { id: 'EQ-010', name: '真空乳化机', type: '生产设备', status: 'running', location: '制作区', runtime: '2024.01', online: true, efficiency: 89, temperature: 40, vibration: 1.4 },
];

// 设备维保计划
export const maintenancePlans = [
  { id: 'MNT-001', equipment: '空压机-01', type: '定期保养', nextDate: '2024-02-01', status: '待执行', handler: '维修组-李四' },
  { id: 'MNT-002', equipment: '纯水系统-01', type: '维修', nextDate: '2024-01-20', status: '进行中', handler: '维修组-王五' },
  { id: 'MNT-003', equipment: '空调机组-02', type: '故障维修', nextDate: '2024-01-16', status: '待执行', handler: '维修组-张三' },
];

// ==================== 生产数据 ====================
export const productionPlans = [
  { id: 'PLAN-001', product: '护肤霜', batch: 'BATCH001', quantity: 1000, progress: 75, status: '进行中', startTime: '2024-01-15 08:00', material: '原料A、原料B', operator: '张三' },
  { id: 'PLAN-002', product: '洁面乳', batch: 'BATCH002', quantity: 800, progress: 30, status: '进行中', startTime: '2024-01-15 10:00', material: '原料C、原料D', operator: '李四' },
  { id: 'PLAN-003', product: '精华液', batch: 'BATCH003', quantity: 1200, progress: 0, status: '待开始', startTime: '2024-01-15 14:00', material: '原料E、原料F', operator: '待分配' },
  { id: 'PLAN-004', product: '面膜', batch: 'BATCH004', quantity: 500, progress: 100, status: '已完成', startTime: '2024-01-14 08:00', material: '原料G、原料H', operator: '王五' },
];

// 批次追溯数据
export const batchTraceData = [
  { 
    batchNo: '20240115-001', 
    product: '护肤霜', 
    quantity: 1000, 
    status: '已完成',
    startTime: '2024-01-15 08:00',
    endTime: '2024-01-15 16:00',
    environment: { temperature: '22°C', humidity: '55%', pressure: '101.3kPa' },
    personnel: [
      { name: '张三', role: '操作员', time: '08:00-16:00', certificate: '健康证-2024' },
      { name: '李四', role: '质检员', time: '14:00-15:00', certificate: '质检员证-2024' }
    ],
    equipment: [
      { name: '乳化罐-01', runtime: '8h', params: { temperature: '75°C', speed: '120r/min' } },
      { name: '灌装机-01', runtime: '6h', params: { speed: '50瓶/min', pressure: '0.5MPa' } }
    ],
    materials: [
      { batch: 'M-20240701', supplier: '广州化工厂', quantity: '500kg', inspection: '合格' },
      { batch: 'M-20240702', supplier: '深圳原料', quantity: '300kg', inspection: '合格' }
    ],
    quality: { appearance: '合格', ph: 6.5, bacteria: '<100cfu/g' }
  },
  { 
    batchNo: '20240114-001', 
    product: '面膜', 
    quantity: 500, 
    status: '已完成',
    startTime: '2024-01-14 08:00',
    endTime: '2024-01-14 14:00',
    environment: { temperature: '21°C', humidity: '52%', pressure: '101.2kPa' },
    personnel: [
      { name: '王五', role: '操作员', time: '08:00-14:00', certificate: '健康证-2024' },
      { name: '赵六', role: '质检员', time: '12:00-13:00', certificate: '质检员证-2024' }
    ],
    equipment: [
      { name: '搅拌罐-01', runtime: '4h', params: { temperature: '40°C', speed: '80r/min' } },
      { name: '灌装机-01', runtime: '3h', params: { speed: '45瓶/min', pressure: '0.4MPa' } }
    ],
    materials: [
      { batch: 'M-20240615', supplier: '上海原料', quantity: '200kg', inspection: '合格' },
      { batch: 'M-20240620', supplier: '北京原料', quantity: '150kg', inspection: '合格' }
    ],
    quality: { appearance: '合格', ph: 6.8, bacteria: '<50cfu/g' }
  },
];

// ==================== GMP合规数据 ====================
export const gmpComplianceData = {
  complianceRate: 98,
  exceedanceCount: 2,
  records: [
    { id: 1, monitorPoint: '洁净区A', compliancePoint: 4, exceedance: 0, status: '正常', lastCheck: '2024-01-15 08:00' },
    { id: 2, monitorPoint: '洁净区B', compliancePoint: 4, exceedance: 0, status: '正常', lastCheck: '2024-01-15 08:00' },
    { id: 3, monitorPoint: '洁净区C', compliancePoint: 3, exceedance: 1, status: '注意', lastCheck: '2024-01-15 08:00' },
    { id: 4, monitorPoint: '洁净区D', compliancePoint: 4, exceedance: 0, status: '正常', lastCheck: '2024-01-15 08:00' },
    { id: 5, monitorPoint: '洁净区E', compliancePoint: 3, exceedance: 1, status: '注意', lastCheck: '2024-01-15 08:00' },
    { id: 6, monitorPoint: '原料仓库', compliancePoint: 5, exceedance: 0, status: '正常', lastCheck: '2024-01-15 08:00' },
    { id: 7, monitorPoint: '成品仓库', compliancePoint: 5, exceedance: 0, status: '正常', lastCheck: '2024-01-15 08:00' },
  ],
};

// ==================== 环保监测数据 ====================
export const environmentalData = {
  vocs: { current: 12.5, limit: 20, unit: 'mg/m³', trend: '↓', status: 'normal' },
  wastewater: { cod: 45, ammonia: 8.5, ph: 7.2, flow: 120, status: 'normal' },
  noise: { current: 58, limit: 65, unit: 'dB', status: 'normal' },
  exhaust: { current: 85, limit: 100, unit: 'm³/h', status: 'normal' },
};

export const environmentalAlerts = [
  { id: 'ENV-001', type: 'VOCs', location: '生产车间', value: 18.5, limit: 20, time: '2024-01-15 10:30', status: '预警' },
  { id: 'ENV-002', type: '废水COD', location: '污水处理站', value: 85, limit: 100, time: '2024-01-15 09:00', status: '正常' },
];

// ==================== 碳排放数据 ====================
export const carbonData = {
  totalEmission: 1250,
  unitEmission: 0.85,
  target: 1000,
  progress: 75,
  trend: [
    { month: '1月', emission: 980 },
    { month: '2月', emission: 1050 },
    { month: '3月', emission: 1100 },
    { month: '4月', emission: 1150 },
    { month: '5月', emission: 1200 },
    { month: '6月', emission: 1250 },
  ],
  sources: [
    { name: '电力消耗', value: 650, percentage: 52 },
    { name: '蒸汽消耗', value: 350, percentage: 28 },
    { name: '运输物流', value: 150, percentage: 12 },
    { name: '其他', value: 100, percentage: 8 },
  ],
};

// ==================== 报表数据 ====================
export const reportTemplates = [
  { id: 'RPT-001', name: '日报表', type: 'daily', description: '每日生产统计报表', lastGenerate: '2024-01-15 18:00' },
  { id: 'RPT-002', name: '周报表', type: 'weekly', description: '每周生产汇总报表', lastGenerate: '2024-01-14 18:00' },
  { id: 'RPT-003', name: '月报表', type: 'monthly', description: '每月生产统计报表', lastGenerate: '2024-01-01 09:00' },
  { id: 'RPT-004', name: '设备运行报表', type: 'equipment', description: '设备运行状态报表', lastGenerate: '2024-01-15 08:00' },
  { id: 'RPT-005', name: '能耗分析报表', type: 'energy', description: '能耗分析与趋势报表', lastGenerate: '2024-01-15 07:00' },
  { id: 'RPT-006', name: '质量分析报表', type: 'quality', description: '产品质量分析报表', lastGenerate: '2024-01-15 08:00' },
];

// ==================== 楼层数据 ====================
export const floorData = [
  { id: 'all', name: '全部', icon: 'layers' },
  { id: '1f', name: '1F仓储', icon: 'warehouse' },
  { id: '2f', name: '2F生产', icon: 'factory' },
  { id: '3f', name: '3F办公', icon: 'building' },
];

// ==================== 化妆品工厂区域数据 ====================
export const factoryAreas = [
  { id: 'raw-materials', name: '原料仓库', x: 5, y: 60, width: 20, height: 25, type: 'storage', color: '#60A5FA', cameraId: 'CAM-005' },
  { id: 'pre-treatment', name: '原料预处理区', x: 30, y: 65, width: 15, height: 18, type: 'production', color: '#F9A8D4' },
  { id: 'changing-room1', name: '一更', x: 30, y: 85, width: 8, height: 10, type: 'auxiliary', color: '#86EFAC' },
  { id: 'changing-room2', name: '二更', x: 38, y: 85, width: 8, height: 10, type: 'auxiliary', color: '#86EFAC' },
  { id: 'buffer', name: '缓冲', x: 46, y: 85, width: 8, height: 10, type: 'auxiliary', color: '#86EFAC' },
  { id: 'lab', name: '实验室', x: 55, y: 85, width: 15, height: 10, type: 'auxiliary', color: '#86EFAC' },
  { id: 'making', name: '制作区', x: 30, y: 40, width: 40, height: 22, type: 'production', color: '#F472B6', cameraId: 'CAM-002' },
  { id: 'filling', name: '灌装区(万级)', x: 30, y: 15, width: 40, height: 22, type: 'production', color: '#DB2777', cameraId: 'CAM-003' },
  { id: 'packaging', name: '包装区', x: 5, y: 25, width: 22, height: 20, type: 'production', color: '#FBCFE8', cameraId: 'CAM-004' },
  { id: 'finished-goods', name: '成品仓库', x: 5, y: 5, width: 22, height: 17, type: 'storage', color: '#60A5FA' },
  { id: 'office', name: '办公楼', x: 75, y: 70, width: 20, height: 25, type: 'office', color: '#C4B5FD', cameraId: 'CAM-011' },
  { id: 'rd', name: '研发楼', x: 75, y: 40, width: 20, height: 25, type: 'office', color: '#C4B5FD' },
  { id: 'dormitory', name: '宿舍楼', x: 75, y: 15, width: 10, height: 20, type: 'office', color: '#C4B5FD' },
  { id: 'canteen', name: '食堂', x: 88, y: 15, width: 7, height: 20, type: 'office', color: '#C4B5FD', cameraId: 'CAM-012' },
];
