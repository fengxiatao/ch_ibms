import { useState } from 'react';
import { motion } from 'framer-motion';
import { Canvas } from '@react-three/fiber';
import { OrbitControls, Grid, Html, PerspectiveCamera } from '@react-three/drei';
import * as THREE from 'three';
import { cameras, buildings3D, alerts } from '@/data/mockData';
import { 
  Camera, 
  Maximize2, 
  CameraOff, 
  Video, 
  Grid3X3, 
  Grid2X2, 
  Play, 
  Pause, 
  SkipBack, 
  SkipForward,
  AlertTriangle,
  MapPin,
  Thermometer,
  Activity,
  Eye,
  Layers,
  X
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { StatusBadge } from '@/components/StatusBadge';

// Simplified 3D Scene for Video Monitor with AR overlay
function VideoScene({ onCameraClick, selectedCamera, alertMode }: { 
  onCameraClick: (camera: typeof cameras[0]) => void;
  selectedCamera: typeof cameras[0] | null;
  alertMode: boolean;
}) {
  return (
    <>
      <PerspectiveCamera makeDefault position={[60, 50, 60]} fov={45} />
      <OrbitControls enablePan={true} enableZoom={true} enableRotate={true} />
      
      <ambientLight intensity={0.4} />
      <directionalLight position={[50, 50, 50]} intensity={0.8} />
      
      <Grid
        position={[0, -0.1, 0]}
        args={[200, 200]}
        cellSize={5}
        cellThickness={0.5}
        cellColor={new THREE.Color('#1E293B')}
        sectionSize={25}
        sectionThickness={1}
        sectionColor={new THREE.Color('#36BFFA')}
        fadeDistance={150}
        fadeStrength={1}
        infiniteGrid
      />
      
      {/* Simplified Buildings */}
      {buildings3D.map((building) => (
        <group key={building.id} position={building.position}>
          <mesh>
            <boxGeometry args={building.size} />
            <meshStandardMaterial color={building.color} transparent opacity={0.7} />
          </mesh>
          <lineSegments>
            <edgesGeometry args={[new THREE.BoxGeometry(...building.size)]} />
            <lineBasicMaterial color={building.borderColor} linewidth={1} />
          </lineSegments>
        </group>
      ))}
      
      {/* Camera Icons with AR overlay */}
      {cameras.map((cam) => (
        <group key={cam.id} position={[cam.position.x, cam.position.y, cam.position.z]}>
          {/* Camera sphere */}
          <mesh onClick={() => onCameraClick(cam)}>
            <sphereGeometry args={[1, 16, 16]} />
            <meshStandardMaterial 
              color={selectedCamera?.id === cam.id ? '#36BFFA' : '#16C79A'} 
              emissive={selectedCamera?.id === cam.id ? '#36BFFA' : '#000000'}
              emissiveIntensity={selectedCamera?.id === cam.id ? 0.5 : 0}
            />
          </mesh>
          
          {/* AR Info Overlay */}
          {(selectedCamera?.id === cam.id || alertMode) && (
            <Html position={[0, 3, 0]} center>
              <div className="bg-[rgba(15,23,42,0.95)] border border-[rgba(54,191,250,0.5)] rounded-lg p-2 min-w-[140px]">
                <p className="text-xs text-[#36BFFA] font-medium">{cam.name}</p>
                <p className="text-[10px] text-[#94A3B8]">{cam.location}</p>
                <div className="flex items-center gap-1 mt-1">
                  <div className="w-1.5 h-1.5 rounded-full bg-[#27AE60] animate-pulse" />
                  <span className="text-[10px] text-[#27AE60]">在线</span>
                </div>
                {alertMode && cam.location.includes('产线C') && (
                  <div className="mt-1 p-1 rounded bg-[rgba(231,76,60,0.2)]">
                    <p className="text-[10px] text-[#E74C3C] flex items-center gap-1">
                      <AlertTriangle className="w-3 h-3" />
                      设备故障报警
                    </p>
                  </div>
                )}
              </div>
            </Html>
          )}
        </group>
      ))}
      
      {/* Alert indicator in 3D scene */}
      {alertMode && (
        <group position={[0, 15, -10]}>
          <Html center>
            <div className="bg-[rgba(231,76,60,0.9)] border border-[#E74C3C] rounded-lg px-3 py-2 animate-pulse-red">
              <p className="text-sm text-white font-medium flex items-center gap-2">
                <AlertTriangle className="w-4 h-4" />
                产线C - 焊接机故障
              </p>
              <p className="text-xs text-white/80">自动追踪中...</p>
            </div>
          </Html>
        </group>
      )}
    </>
  );
}

// Mock Video Feed Component with AR overlay
function VideoFeed({ 
  camera, 
  isActive,
  arMode,
  alertInfo
}: { 
  camera: typeof cameras[0] | null; 
  isActive: boolean;
  arMode: boolean;
  alertInfo: typeof alerts[0] | null;
}) {
  if (!camera) {
    return (
      <div className="w-full h-full bg-[#0F172A] rounded-lg flex items-center justify-center border border-[rgba(148,163,184,0.2)]">
        <div className="text-center">
          <CameraOff className="w-8 h-8 text-[#64748B] mx-auto mb-2" />
          <p className="text-sm text-[#64748B]">选择摄像头查看</p>
        </div>
      </div>
    );
  }

  return (
    <div className={`relative bg-[#0F172A] rounded-lg overflow-hidden border ${
      isActive ? 'border-[#36BFFA]' : 'border-[rgba(148,163,184,0.2)]'
    }`}>
      {/* Mock Video Content */}
      <div className="w-full h-full bg-gradient-to-br from-[#1E293B] to-[#0F172A] flex items-center justify-center">
        <div className="text-center">
          <Video className={`w-12 h-12 mx-auto mb-3 ${isActive ? 'text-[#36BFFA]' : 'text-[#64748B]'}`} />
          <p className="text-sm text-[#94A3B8]">{camera.name}</p>
          <p className="text-xs text-[#64748B] mt-1">{camera.location}</p>
          <div className="flex items-center justify-center gap-2 mt-3">
            <div className="w-2 h-2 rounded-full bg-[#27AE60] animate-pulse" />
            <span className="text-xs text-[#27AE60]">实时直播中</span>
          </div>
        </div>
      </div>
      
      {/* AR Overlay */}
      {arMode && (
        <div className="absolute inset-0 pointer-events-none">
          {/* AR Crosshair */}
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2">
            <div className="w-8 h-8 border-2 border-[#36BFFA] rounded-full opacity-50" />
            <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-1 h-1 bg-[#36BFFA]" />
          </div>
          
          {/* AR Info Panel */}
          <div className="absolute top-4 left-4 bg-[rgba(15,23,42,0.9)] border border-[rgba(54,191,250,0.3)] rounded-lg p-2">
            <p className="text-xs text-[#36BFFA] font-medium">{camera.name}</p>
            <div className="flex items-center gap-2 mt-1 text-[10px] text-[#94A3B8]">
              <MapPin className="w-3 h-3" />
              <span>{camera.location}</span>
            </div>
            <div className="flex items-center gap-2 mt-1 text-[10px] text-[#94A3B8]">
              <Thermometer className="w-3 h-3" />
              <span>22.5°C</span>
            </div>
          </div>
          
          {/* AR Tags */}
          <div className="absolute top-1/3 right-1/4">
            <div className="flex items-center gap-1 bg-[rgba(39,174,96,0.8)] px-2 py-1 rounded text-[10px] text-white">
              <Activity className="w-3 h-3" />
              <span>设备正常</span>
            </div>
          </div>
        </div>
      )}
      
      {/* Alert Overlay */}
      {alertInfo && (
        <div className="absolute inset-0 pointer-events-none">
          <div className="absolute top-4 right-4 bg-[rgba(231,76,60,0.9)] border border-[#E74C3C] rounded-lg p-3 animate-pulse-red">
            <div className="flex items-center gap-2">
              <AlertTriangle className="w-5 h-5 text-white" />
              <div>
                <p className="text-sm text-white font-medium">{alertInfo.type}</p>
                <p className="text-xs text-white/80">{alertInfo.message}</p>
              </div>
            </div>
          </div>
          
          {/* Alert tracking box */}
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2">
            <div className="w-32 h-24 border-2 border-[#E74C3C] rounded animate-pulse">
              <div className="absolute -top-6 left-0 bg-[#E74C3C] text-white text-[10px] px-2 py-0.5 rounded">
                报警区域
              </div>
            </div>
          </div>
        </div>
      )}
      
      {/* Video Overlay Info */}
      <div className="absolute top-2 left-2 right-2 flex justify-between items-start">
        <div className="bg-[rgba(0,0,0,0.6)] px-2 py-1 rounded text-xs text-[#F1F5F9]">
          {camera.name}
        </div>
        <div className="bg-[rgba(0,0,0,0.6)] px-2 py-1 rounded text-xs text-[#94A3B8]">
          HD
        </div>
      </div>
      
      <div className="absolute bottom-2 left-2 text-xs text-[#94A3B8]">
        {new Date().toLocaleTimeString()}
      </div>
    </div>
  );
}

export function VideoMonitor() {
  const [selectedCamera, setSelectedCamera] = useState<typeof cameras[0] | null>(cameras[0]);
  const [layout, setLayout] = useState<'single' | 'quad' | 'grid'>('single');
  const [isPlaying, setIsPlaying] = useState(true);
  const [arMode, setArMode] = useState(false);
  const [alertTracking, setAlertTracking] = useState(false);
  const [trackedAlert, setTrackedAlert] = useState<typeof alerts[0] | null>(null);

  const handleCameraClick = (camera: typeof cameras[0]) => {
    setSelectedCamera(camera);
    if (alertTracking) {
      setAlertTracking(false);
      setTrackedAlert(null);
    }
  };

  const handleAlertTrack = (alert: typeof alerts[0]) => {
    setTrackedAlert(alert);
    setAlertTracking(true);
    // Find camera near the alert location
    const nearbyCamera = cameras.find(c => c.location.includes(alert.location.split('-')[0]));
    if (nearbyCamera) {
      setSelectedCamera(nearbyCamera);
    }
  };

  return (
    <div className="h-full flex gap-4">
      {/* 3D Scene */}
      <motion.div
        initial={{ opacity: 0, scale: 0.98 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.5 }}
        className="w-1/3 factory-card overflow-hidden relative"
      >
        <div className="absolute top-3 left-3 z-10">
          <h3 className="text-sm font-semibold text-[#F1F5F9] flex items-center gap-2">
            <Camera className="w-4 h-4 text-[#36BFFA]" />
            3D监控点位
          </h3>
        </div>
        
        {/* AR Mode Toggle */}
        <div className="absolute top-3 right-3 z-10">
          <button
            onClick={() => setArMode(!arMode)}
            className={`flex items-center gap-1 px-3 py-1.5 rounded-lg text-xs transition-all ${
              arMode 
                ? 'bg-[rgba(54,191,250,0.2)] text-[#36BFFA] border border-[#36BFFA]' 
                : 'bg-[rgba(30,41,59,0.8)] text-[#94A3B8] border border-transparent'
            }`}
          >
            <Layers className="w-3 h-3" />
            AR增强
          </button>
        </div>
        
        <Canvas className="w-full h-full">
          <VideoScene 
            onCameraClick={handleCameraClick} 
            selectedCamera={selectedCamera}
            alertMode={alertTracking}
          />
        </Canvas>
        
        {/* Camera List Overlay */}
        <div className="absolute bottom-3 left-3 right-3">
          <div className="factory-card p-2">
            <p className="text-xs text-[#94A3B8] mb-2">摄像头列表</p>
            <div className="flex gap-2 overflow-x-auto scrollbar-thin">
              {cameras.map((cam) => (
                <button
                  key={cam.id}
                  onClick={() => handleCameraClick(cam)}
                  className={`flex-shrink-0 px-3 py-1.5 rounded text-xs transition-all ${
                    selectedCamera?.id === cam.id
                      ? 'bg-[rgba(54,191,250,0.2)] text-[#36BFFA] border border-[#36BFFA]'
                      : 'bg-[rgba(30,41,59,0.8)] text-[#94A3B8] border border-transparent hover:border-[rgba(148,163,184,0.3)]'
                  }`}
                >
                  {cam.name}
                </button>
              ))}
            </div>
          </div>
        </div>
      </motion.div>

      {/* Video Player Area */}
      <motion.div
        initial={{ opacity: 0, x: 20 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.5, delay: 0.2 }}
        className="flex-1 flex flex-col gap-4"
      >
        {/* Video Controls */}
        <div className="factory-card p-3 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Button
              variant="ghost"
              size="icon"
              className={`w-8 h-8 ${layout === 'single' ? 'text-[#36BFFA] bg-[rgba(54,191,250,0.1)]' : 'text-[#94A3B8]'}`}
              onClick={() => setLayout('single')}
            >
              <Maximize2 className="w-4 h-4" />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              className={`w-8 h-8 ${layout === 'quad' ? 'text-[#36BFFA] bg-[rgba(54,191,250,0.1)]' : 'text-[#94A3B8]'}`}
              onClick={() => setLayout('quad')}
            >
              <Grid2X2 className="w-4 h-4" />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              className={`w-8 h-8 ${layout === 'grid' ? 'text-[#36BFFA] bg-[rgba(54,191,250,0.1)]' : 'text-[#94A3B8]'}`}
              onClick={() => setLayout('grid')}
            >
              <Grid3X3 className="w-4 h-4" />
            </Button>
            
            <div className="w-px h-6 bg-[rgba(148,163,184,0.2)] mx-2" />
            
            <Button
              variant="ghost"
              size="sm"
              className={`h-8 text-xs ${arMode ? 'text-[#36BFFA] bg-[rgba(54,191,250,0.1)]' : 'text-[#94A3B8]'}`}
              onClick={() => setArMode(!arMode)}
            >
              <Layers className="w-3 h-3 mr-1" />
              AR增强
            </Button>
          </div>
          
          <div className="flex items-center gap-2">
            <span className="text-sm text-[#94A3B8]">
              {selectedCamera?.name || '未选择'}
            </span>
            <span className="text-xs text-[#64748B]">
              {selectedCamera?.location}
            </span>
          </div>
          
          <div className="flex items-center gap-2">
            <Button variant="ghost" size="icon" className="w-8 h-8 text-[#94A3B8]">
              <SkipBack className="w-4 h-4" />
            </Button>
            <Button 
              variant="ghost" 
              size="icon" 
              className="w-8 h-8 text-[#36BFFA]"
              onClick={() => setIsPlaying(!isPlaying)}
            >
              {isPlaying ? <Pause className="w-4 h-4" /> : <Play className="w-4 h-4" />}
            </Button>
            <Button variant="ghost" size="icon" className="w-8 h-8 text-[#94A3B8]">
              <SkipForward className="w-4 h-4" />
            </Button>
          </div>
        </div>

        {/* Video Display */}
        <div className="flex-1 factory-card p-4">
          {layout === 'single' && (
            <VideoFeed 
              camera={selectedCamera} 
              isActive={true} 
              arMode={arMode}
              alertInfo={trackedAlert}
            />
          )}
          
          {layout === 'quad' && (
            <div className="grid grid-cols-2 gap-2 h-full">
              {cameras.slice(0, 4).map((cam) => (
                <div 
                  key={cam.id} 
                  className="cursor-pointer"
                  onClick={() => setSelectedCamera(cam)}
                >
                  <VideoFeed 
                    camera={cam} 
                    isActive={selectedCamera?.id === cam.id} 
                    arMode={false}
                    alertInfo={null}
                  />
                </div>
              ))}
            </div>
          )}
          
          {layout === 'grid' && (
            <div className="grid grid-cols-3 gap-2 h-full">
              {cameras.map((cam) => (
                <div 
                  key={cam.id} 
                  className="cursor-pointer"
                  onClick={() => setSelectedCamera(cam)}
                >
                  <VideoFeed 
                    camera={cam} 
                    isActive={selectedCamera?.id === cam.id} 
                    arMode={false}
                    alertInfo={null}
                  />
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Alert Tracking Panel */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.3 }}
          className="factory-card p-3"
        >
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm font-semibold text-[#F1F5F9] flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 text-[#E74C3C]" />
              报警联动追踪
            </h3>
            {alertTracking && (
              <button 
                onClick={() => { setAlertTracking(false); setTrackedAlert(null); }}
                className="text-xs text-[#94A3B8] hover:text-[#F1F5F9] flex items-center gap-1"
              >
                <X className="w-3 h-3" />
                取消追踪
              </button>
            )}
          </div>
          
          <div className="grid grid-cols-3 gap-2">
            {alerts.filter(a => a.status !== 'resolved').slice(0, 3).map((alert) => (
              <div 
                key={alert.id} 
                className={`p-2 rounded-lg cursor-pointer transition-all ${
                  trackedAlert?.id === alert.id 
                    ? 'bg-[rgba(231,76,60,0.2)] border border-[#E74C3C]' 
                    : 'bg-[rgba(30,41,59,0.6)] border border-transparent hover:border-[rgba(148,163,184,0.3)]'
                }`}
                onClick={() => handleAlertTrack(alert)}
              >
                <div className="flex items-center gap-1 mb-1">
                  <StatusBadge status={alert.level} type="alert" />
                </div>
                <p className="text-xs text-[#F1F5F9] truncate">{alert.message}</p>
                <p className="text-[10px] text-[#94A3B8] mt-1">{alert.location} · {alert.time}</p>
              </div>
            ))}
          </div>
          
          {alertTracking && trackedAlert && (
            <div className="mt-2 p-2 rounded-lg bg-[rgba(231,76,60,0.1)] border border-[rgba(231,76,60,0.3)]">
              <p className="text-xs text-[#E74C3C] flex items-center gap-2">
                <Eye className="w-3 h-3" />
                正在追踪: {trackedAlert.message} - 已自动切换至{selectedCamera?.name}
              </p>
            </div>
          )}
        </motion.div>

        {/* Video Info */}
        <div className="factory-card p-3">
          <div className="flex items-center justify-between text-sm">
            <div className="flex items-center gap-4">
              <span className="text-[#94A3B8]">
                摄像头: <span className="text-[#F1F5F9]">{selectedCamera?.name || '-'}</span>
              </span>
              <span className="text-[#94A3B8]">
                位置: <span className="text-[#F1F5F9]">{selectedCamera?.location || '-'}</span>
              </span>
              <span className="text-[#94A3B8]">
                类型: <span className="text-[#F1F5F9]">{selectedCamera?.type === 'high' ? '高点' : '低点'}</span>
              </span>
            </div>
            <div className="flex items-center gap-4">
              <span className="text-[#94A3B8]">
                状态: <span className="text-[#27AE60]">在线</span>
              </span>
              <span className="text-[#94A3B8]">
                清晰度: <span className="text-[#36BFFA]">高清</span>
              </span>
              <span className="text-[#94A3B8]">
                延迟: <span className="text-[#16C79A]">&lt;1s</span>
              </span>
            </div>
          </div>
        </div>
      </motion.div>
    </div>
  );
}
