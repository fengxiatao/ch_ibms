// Dashboard Types
export type DashboardTab = 'overview' | 'production' | 'environment' | 'safety' | 'energy' | 'video';

// KPI Types
export interface KPIData {
  label: string;
  value: number;
  unit: string;
  trend: number;
  trendUp: boolean;
  icon: string;
  status?: 'normal' | 'warning' | 'danger';
}

// Device Status
export type DeviceStatus = 'running' | 'standby' | 'fault' | 'offline';

export interface Device {
  id: string;
  name: string;
  status: DeviceStatus;
  line: string;
  faultCode?: string;
  faultTime?: string;
}

// Alert Types
export type AlertLevel = 'general' | 'serious' | 'emergency';
export type AlertStatus = 'pending' | 'processing' | 'resolved';

export interface Alert {
  id: string;
  level: AlertLevel;
  message: string;
  location: string;
  time: string;
  status: AlertStatus;
  type: string;
}

// Production Types
export interface ProductionLine {
  id: string;
  name: string;
  oee: number;
  availability: number;
  performance: number;
  quality: number;
}

export interface BatchInfo {
  id: string;
  name: string;
  currentProcess: string;
  completedProcesses: number;
  totalProcesses: number;
  quality: number;
}

// Environment Types
export interface EnvironmentZone {
  id: string;
  name: string;
  level: string;
  temperature: number;
  humidity: number;
  pressure: number;
  particles05: number;
  particles5: number;
  bacteria: number;
  status: 'normal' | 'warning' | 'danger';
}

// Safety Types
export interface SafetyDevice {
  id: string;
  name: string;
  type: string;
  location: string;
  status: 'normal' | 'fault' | 'expired';
  installDate: string;
  lastMaintenance: string;
}

export interface DrillPlan {
  id: string;
  name: string;
  plannedDate: string;
  completedDate?: string;
  status: 'planned' | 'completed';
  passRate?: number;
}

// Energy Types
export interface EnergyData {
  type: 'electricity' | 'water' | 'steam' | 'air';
  name: string;
  daily: number;
  monthly: number;
  yoy: number;
  mom: number;
  unit: string;
}

export interface CarbonData {
  daily: number;
  monthly: number;
  target: number;
  progress: number;
}

// Video Types
export interface Camera {
  id: string;
  name: string;
  location: string;
  status: 'online' | 'offline';
  position: { x: number; y: number; z: number };
  type?: 'high' | 'low';
}

// 3D Scene Types
export interface Building3D {
  id: string;
  name: string;
  type: 'workshop' | 'warehouse' | 'power' | 'office' | 'cleanroom' | 'explosion' | 'hazmat';
  position: [number, number, number];
  size: [number, number, number];
  color: string;
  borderColor?: string;
}
