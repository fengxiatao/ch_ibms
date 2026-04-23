import { useState, useRef, Suspense } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import { OrbitControls, Grid, Html, PerspectiveCamera } from '@react-three/drei';
import { motion } from 'framer-motion';
import * as THREE from 'three';
import type { Building3D } from '@/types';
import { buildings3D, cameras } from '@/data/mockData';
import { Camera, Info, Layers, Maximize2, RotateCcw } from 'lucide-react';
import { Button } from '@/components/ui/button';

// Building Component
function Building({ 
  data, 
  isSelected, 
  onClick 
}: { 
  data: Building3D; 
  isSelected: boolean; 
  onClick: () => void;
}) {
  const meshRef = useRef<THREE.Mesh>(null);
  const [hovered, setHovered] = useState(false);

  useFrame((state) => {
    if (meshRef.current && isSelected) {
      const material = meshRef.current.material as THREE.MeshStandardMaterial;
      if (material && material.emissiveIntensity !== undefined) {
        material.emissiveIntensity = 0.3 + Math.sin(state.clock.elapsedTime * 2) * 0.1;
      }
    }
  });

  return (
    <group position={data.position}>
      {/* Main building */}
      <mesh
        ref={meshRef}
        onClick={onClick}
        onPointerOver={() => setHovered(true)}
        onPointerOut={() => setHovered(false)}
      >
        <boxGeometry args={data.size} />
        <meshStandardMaterial
          color={data.color}
          emissive={isSelected ? data.borderColor : '#000000'}
          emissiveIntensity={isSelected ? 0.3 : 0}
          transparent
          opacity={0.9}
        />
      </mesh>
      
      {/* Border highlight */}
      <lineSegments>
        <edgesGeometry args={[new THREE.BoxGeometry(...data.size)]} />
        <lineBasicMaterial 
          color={data.borderColor} 
          linewidth={2}
          transparent
          opacity={isSelected || hovered ? 1 : 0.5}
        />
      </lineSegments>
      
      {/* Label */}
      {(isSelected || hovered) && (
        <Html position={[0, data.size[1] / 2 + 2, 0]} center>
          <div className="bg-[rgba(15,23,42,0.9)] border border-[rgba(54,191,250,0.5)] rounded-lg px-3 py-2 whitespace-nowrap">
            <p className="text-sm font-medium text-[#F1F5F9]">{data.name}</p>
            <p className="text-xs text-[#94A3B8]">点击查看详情</p>
          </div>
        </Html>
      )}
    </group>
  );
}

// Camera Icon Component
function CameraIcon({ position, name, onClick }: { position: [number, number, number]; name: string; onClick: () => void }) {
  const [hovered, setHovered] = useState(false);
  
  return (
    <group position={position}>
      <mesh onClick={onClick} onPointerOver={() => setHovered(true)} onPointerOut={() => setHovered(false)}>
        <sphereGeometry args={[0.8, 16, 16]} />
        <meshStandardMaterial 
          color={hovered ? '#36BFFA' : '#16C79A'} 
          emissive={hovered ? '#36BFFA' : '#000000'}
          emissiveIntensity={hovered ? 0.5 : 0}
        />
      </mesh>
      {hovered && (
        <Html position={[0, 1.5, 0]} center>
          <div className="bg-[rgba(15,23,42,0.9)] border border-[rgba(22,199,154,0.5)] rounded px-2 py-1">
            <p className="text-xs text-[#F1F5F9]">{name}</p>
          </div>
        </Html>
      )}
    </group>
  );
}

// Scene Component
function Scene({ selectedBuilding, onBuildingClick }: { selectedBuilding: string | null; onBuildingClick: (id: string) => void }) {
  return (
    <>
      <PerspectiveCamera makeDefault position={[60, 50, 60]} fov={45} />
      <OrbitControls 
        enablePan={true} 
        enableZoom={true} 
        enableRotate={true}
        minDistance={20}
        maxDistance={150}
        maxPolarAngle={Math.PI / 2 - 0.1}
      />
      
      {/* Lighting */}
      <ambientLight intensity={0.4} />
      <directionalLight position={[50, 50, 50]} intensity={0.8} castShadow />
      <pointLight position={[0, 30, 0]} intensity={0.5} color="#36BFFA" />
      
      {/* Ground Grid */}
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
      
      {/* Buildings */}
      {buildings3D.map((building) => (
        <Building
          key={building.id}
          data={building}
          isSelected={selectedBuilding === building.id}
          onClick={() => onBuildingClick(building.id)}
        />
      ))}
      
      {/* Camera Icons */}
      {cameras.map((cam) => (
        <CameraIcon
          key={cam.id}
          position={[cam.position.x, cam.position.y, cam.position.z]}
          name={cam.name}
          onClick={() => {}}
        />
      ))}
    </>
  );
}

// Loading Component
function LoadingScene() {
  return (
    <div className="flex items-center justify-center h-full">
      <div className="text-center">
        <div className="w-12 h-12 border-4 border-[#36BFFA] border-t-transparent rounded-full animate-spin mx-auto mb-4" />
        <p className="text-[#94A3B8]">加载3D场景中...</p>
      </div>
    </div>
  );
}

export function Factory3DView() {
  const [selectedBuilding, setSelectedBuilding] = useState<string | null>(null);
  const [viewLevel, setViewLevel] = useState<'factory' | 'workshop' | 'line' | 'device'>('factory');

  const handleBuildingClick = (id: string) => {
    setSelectedBuilding(selectedBuilding === id ? null : id);
  };

  const selectedBuildingData = buildings3D.find(b => b.id === selectedBuilding);

  return (
    <div className="h-full flex gap-4">
      {/* 3D Scene */}
      <motion.div
        initial={{ opacity: 0, scale: 0.98 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.5 }}
        className="flex-1 factory-card overflow-hidden relative"
      >
        {/* Controls */}
        <div className="absolute top-4 left-4 z-10 flex flex-col gap-2">
          <div className="factory-card p-2 flex flex-col gap-2">
            <Button
              variant="ghost"
              size="icon"
              className="w-8 h-8 text-[#94A3B8] hover:text-[#36BFFA] hover:bg-[rgba(54,191,250,0.1)]"
              onClick={() => setViewLevel('factory')}
            >
              <Layers className="w-4 h-4" />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              className="w-8 h-8 text-[#94A3B8] hover:text-[#36BFFA] hover:bg-[rgba(54,191,250,0.1)]"
            >
              <RotateCcw className="w-4 h-4" />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              className="w-8 h-8 text-[#94A3B8] hover:text-[#36BFFA] hover:bg-[rgba(54,191,250,0.1)]"
            >
              <Maximize2 className="w-4 h-4" />
            </Button>
          </div>
        </div>

        {/* Legend */}
        <div className="absolute bottom-4 left-4 z-10">
          <div className="factory-card p-3">
            <p className="text-xs font-medium text-[#94A3B8] mb-2">区域图例</p>
            <div className="space-y-1.5">
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 border-2 border-[#36BFFA] rounded-sm" />
                <span className="text-xs text-[#F1F5F9]">生产车间</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 border-2 border-[#A855F7] rounded-sm" />
                <span className="text-xs text-[#F1F5F9]">洁净区</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 border-2 border-[#E74C3C] rounded-sm" />
                <span className="text-xs text-[#F1F5F9]">防爆区</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 border-2 border-[#EA580C] rounded-sm" />
                <span className="text-xs text-[#F1F5F9]">危化品库</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 border-2 border-[#F39C12] rounded-sm" />
                <span className="text-xs text-[#F1F5F9]">仓储区</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 border-2 border-[#27AE60] rounded-sm" />
                <span className="text-xs text-[#F1F5F9]">动力中心</span>
              </div>
            </div>
          </div>
        </div>

        {/* 3D Canvas */}
        <Canvas className="w-full h-full">
          <Suspense fallback={<LoadingScene />}>
            <Scene selectedBuilding={selectedBuilding} onBuildingClick={handleBuildingClick} />
          </Suspense>
        </Canvas>
      </motion.div>

      {/* Side Panel */}
      <motion.div
        initial={{ opacity: 0, x: 20 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.5, delay: 0.2 }}
        className="w-64 factory-card p-4 flex flex-col gap-4"
      >
        <div>
          <h3 className="text-base font-semibold text-[#F1F5F9] flex items-center gap-2">
            <Layers className="w-4 h-4 text-[#36BFFA]" />
            层级控制
          </h3>
          <div className="mt-3 space-y-2">
            {[
              { id: 'factory', label: '厂区级' },
              { id: 'workshop', label: '车间级' },
              { id: 'line', label: '产线级' },
              { id: 'device', label: '设备级' },
            ].map((level) => (
              <button
                key={level.id}
                onClick={() => setViewLevel(level.id as typeof viewLevel)}
                className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-all ${
                  viewLevel === level.id
                    ? 'bg-[rgba(54,191,250,0.2)] text-[#36BFFA]'
                    : 'text-[#94A3B8] hover:bg-[rgba(148,163,184,0.1)] hover:text-[#F1F5F9]'
                }`}
              >
                {level.label}
              </button>
            ))}
          </div>
        </div>

        {selectedBuildingData && (
          <div className="border-t border-[rgba(148,163,184,0.2)] pt-4">
            <h3 className="text-base font-semibold text-[#F1F5F9] flex items-center gap-2">
              <Info className="w-4 h-4 text-[#36BFFA]" />
              区域信息
            </h3>
            <div className="mt-3 space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-[#94A3B8]">名称</span>
                <span className="text-[#F1F5F9]">{selectedBuildingData.name}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-[#94A3B8]">类型</span>
                <span className="text-[#F1F5F9]">
                  {selectedBuildingData.type === 'workshop' && '生产车间'}
                  {selectedBuildingData.type === 'cleanroom' && '洁净车间'}
                  {selectedBuildingData.type === 'explosion' && '防爆车间'}
                  {selectedBuildingData.type === 'hazmat' && '危化品库'}
                  {selectedBuildingData.type === 'warehouse' && '仓库'}
                  {selectedBuildingData.type === 'power' && '动力中心'}
                  {selectedBuildingData.type === 'office' && '办公楼'}
                </span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-[#94A3B8]">摄像头</span>
                <span className="text-[#36BFFA]">{cameras.filter(c => c.location.includes(selectedBuildingData.name)).length} 个</span>
              </div>
            </div>
          </div>
        )}

        <div className="border-t border-[rgba(148,163,184,0.2)] pt-4">
          <h3 className="text-base font-semibold text-[#F1F5F9] flex items-center gap-2">
            <Camera className="w-4 h-4 text-[#16C79A]" />
            监控点位
          </h3>
          <div className="mt-3 text-sm text-[#94A3B8]">
            共 <span className="text-[#36BFFA] font-medium">{cameras.length}</span> 个摄像头
          </div>
        </div>
      </motion.div>
    </div>
  );
}
