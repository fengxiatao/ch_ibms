import { useRef, useState } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import { OrbitControls, PerspectiveCamera, Html } from '@react-three/drei';
import * as THREE from 'three';
import { ZoomIn, ZoomOut, Move, Maximize2 } from 'lucide-react';

// 化妆品工厂3D模型组件
function FactoryModel() {
  const groupRef = useRef<THREE.Group>(null);
  
  useFrame((state) => {
    if (groupRef.current) {
      groupRef.current.rotation.y = Math.sin(state.clock.elapsedTime * 0.1) * 0.02;
    }
  });

  return (
    <group ref={groupRef} position={[0, 0, 0]}>
      {/* 原料仓库 */}
      <mesh position={[-3.5, 1.5, 2]} userData={{ id: 'raw-materials', name: '原料仓库' }}>
        <boxGeometry args={[2, 3, 2]} />
        <meshStandardMaterial color="#60A5FA" transparent opacity={0.7} />
      </mesh>
      
      {/* 原料预处理区 */}
      <mesh position={[-2, 1, 0.5]} userData={{ id: 'pre-treatment', name: '原料预处理区' }}>
        <boxGeometry args={[1.5, 2, 2]} />
        <meshStandardMaterial color="#F9A8D4" transparent opacity={0.7} />
      </mesh>
      
      {/* 更衣室 */}
      <mesh position={[-2, 0.5, -1.5]} userData={{ id: 'changing-room', name: '更衣室' }}>
        <boxGeometry args={[1, 1, 1]} />
        <meshStandardMaterial color="#86EFAC" transparent opacity={0.7} />
      </mesh>
      
      {/* 制作区 */}
      <mesh position={[0, 1.5, 0.5]} userData={{ id: 'making', name: '制作区' }}>
        <boxGeometry args={[4, 3, 3]} />
        <meshStandardMaterial color="#F472B6" transparent opacity={0.7} />
      </mesh>
      
      {/* 灌装区 */}
      <mesh position={[0, 1.5, -2]} userData={{ id: 'filling', name: '灌装区(万级)' }}>
        <boxGeometry args={[4, 3, 2.5]} />
        <meshStandardMaterial color="#DB2777" transparent opacity={0.7} />
      </mesh>
      
      {/* 包装区 */}
      <mesh position={[-3.5, 1, -2]} userData={{ id: 'packaging', name: '包装区' }}>
        <boxGeometry args={[2, 2, 2.5]} />
        <meshStandardMaterial color="#FBCFE8" transparent opacity={0.7} />
      </mesh>
      
      {/* 成品仓库 */}
      <mesh position={[-3.5, 1, -4]} userData={{ id: 'finished-goods', name: '成品仓库' }}>
        <boxGeometry args={[2, 2, 2]} />
        <meshStandardMaterial color="#60A5FA" transparent opacity={0.7} />
      </mesh>
      
      {/* 办公楼 */}
      <mesh position={[4, 2, 1.5]} userData={{ id: 'office', name: '办公楼' }}>
        <boxGeometry args={[2, 4, 3]} />
        <meshStandardMaterial color="#C4B5FD" transparent opacity={0.7} />
      </mesh>
      
      {/* 研发楼 */}
      <mesh position={[4, 1.5, -1]} userData={{ id: 'rd', name: '研发楼' }}>
        <boxGeometry args={[2, 3, 2.5]} />
        <meshStandardMaterial color="#C4B5FD" transparent opacity={0.7} />
      </mesh>
      
      {/* 食堂 */}
      <mesh position={[4, 1, -3.5]} userData={{ id: 'canteen', name: '食堂' }}>
        <boxGeometry args={[1.5, 2, 2]} />
        <meshStandardMaterial color="#C4B5FD" transparent opacity={0.7} />
      </mesh>
      
      {/* 连接通道 */}
      <mesh position={[0, 0.2, 0]} userData={{ id: 'corridor', name: '连廊' }}>
        <boxGeometry args={[8, 0.4, 8]} />
        <meshStandardMaterial color="#374151" transparent opacity={0.5} />
      </mesh>
      
      {/* 地面 */}
      <mesh position={[0, -0.1, 0]} rotation={[-Math.PI / 2, 0, 0]}>
        <planeGeometry args={[15, 15]} />
        <meshStandardMaterial color="#1F2937" transparent opacity={0.8} />
      </mesh>
    </group>
  );
}

// 设备标记点
function DeviceMarker({ position, status }: { position: [number, number, number]; status: 'running' | 'fault' | 'maintenance'; name?: string }) {
  const meshRef = useRef<THREE.Mesh>(null);
  
  useFrame((state) => {
    if (meshRef.current) {
      meshRef.current.position.y = position[1] + Math.sin(state.clock.elapsedTime * 2) * 0.1;
    }
  });

  const color = status === 'running' ? '#22C55E' : status === 'fault' ? '#EF4444' : '#EAB308';
  
  return (
    <mesh ref={meshRef} position={position}>
      <sphereGeometry args={[0.15, 16, 16]} />
      <meshStandardMaterial color={color} emissive={color} emissiveIntensity={0.5} />
    </mesh>
  );
}

// 场景标签
function SceneLabel({ position, text }: { position: [number, number, number]; text: string }) {
  return (
    <Html position={position} center>
      <div className="bg-black/70 text-white px-2 py-1 rounded text-xs whitespace-nowrap">
        {text}
      </div>
    </Html>
  );
}

export function Factory3D() {
  const [, setZoom] = useState(1);
  
  const handleZoomIn = () => setZoom((prev: number) => Math.min(prev + 0.2, 2));
  const handleZoomOut = () => setZoom((prev: number) => Math.max(prev - 0.2, 0.5));
  const handleReset = () => setZoom(1);

  return (
    <div className="absolute inset-0">
      {/* 背景渐变 */}
      <div className="absolute inset-0 bg-gradient-to-b from-[#0A0F1C] via-[#111827] to-[#0A0F1C] opacity-80" />
      
      {/* 3D Canvas */}
      <Canvas>
        <PerspectiveCamera makeDefault position={[10, 8, 10]} fov={50} />
        <OrbitControls 
          enableZoom={true}
          enablePan={true}
          enableRotate={true}
          minDistance={5}
          maxDistance={30}
          maxPolarAngle={Math.PI / 2.2}
        />
        
        {/* 光照 */}
        <ambientLight intensity={0.4} />
        <directionalLight position={[10, 10, 5]} intensity={0.8} />
        <pointLight position={[-10, 10, -10]} intensity={0.4} color="#60A5FA" />
        
        {/* 3D工厂模型 */}
        <FactoryModel />
        
        {/* 设备标记 */}
        <DeviceMarker position={[-2, 3, 0.5]} status="running" name="空压机" />
        <DeviceMarker position={[0, 3, 0.5]} status="running" name="乳化罐" />
        <DeviceMarker position={[2, 3, 0.5]} status="fault" name="搅拌机" />
        <DeviceMarker position={[0, 3, -2]} status="running" name="灌装机" />
        <DeviceMarker position={[-3.5, 2.5, -2]} status="running" name="包装线" />
        
        {/* 区域标签 */}
        <SceneLabel position={[-3.5, 4, 2]} text="原料仓库" />
        <SceneLabel position={[0, 4.5, 0.5]} text="制作区" />
        <SceneLabel position={[0, 4.5, -2]} text="灌装区" />
        <SceneLabel position={[4, 5, 1.5]} text="办公楼" />
      </Canvas>
      
      {/* 顶部工具栏 */}
      <div className="absolute top-4 right-4 flex gap-2">
        <button 
          onClick={handleZoomIn}
          className="p-2 bg-[#111827]/90 border border-[#1F2937] rounded-lg text-gray-300 hover:bg-[#1F2937] transition-colors"
          title="放大"
        >
          <ZoomIn className="w-4 h-4" />
        </button>
        <button 
          onClick={handleZoomOut}
          className="p-2 bg-[#111827]/90 border border-[#1F2937] rounded-lg text-gray-300 hover:bg-[#1F2937] transition-colors"
          title="缩小"
        >
          <ZoomOut className="w-4 h-4" />
        </button>
        <button 
          onClick={handleReset}
          className="p-2 bg-[#111827]/90 border border-[#1F2937] rounded-lg text-gray-300 hover:bg-[#1F2937] transition-colors"
          title="重置"
        >
          <Maximize2 className="w-4 h-4" />
        </button>
      </div>
      
      {/* 左上角图例 */}
      <div className="absolute top-4 left-4 bg-[#111827]/90 border border-[#1F2937] rounded-lg p-3">
        <p className="text-xs text-gray-400 mb-2">区域图例</p>
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <div className="w-3 h-3 rounded" style={{ backgroundColor: '#60A5FA' }} />
            <span className="text-xs text-white">仓储区</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-3 h-3 rounded" style={{ backgroundColor: '#F472B6' }} />
            <span className="text-xs text-white">生产区</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-3 h-3 rounded" style={{ backgroundColor: '#DB2777' }} />
            <span className="text-xs text-white">洁净区</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-3 h-3 rounded" style={{ backgroundColor: '#86EFAC' }} />
            <span className="text-xs text-white">辅助区</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-3 h-3 rounded" style={{ backgroundColor: '#C4B5FD' }} />
            <span className="text-xs text-white">办公区</span>
          </div>
        </div>
      </div>
      
      {/* 左下角状态指示 */}
      <div className="absolute bottom-4 left-4 flex gap-4 bg-[#111827]/90 border border-[#1F2937] rounded-lg p-3">
        <div className="flex items-center gap-2">
          <div className="w-2 h-2 rounded-full bg-green-400 status-pulse" />
          <span className="text-xs text-white">运行中 5</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-2 h-2 rounded-full bg-red-400" />
          <span className="text-xs text-white">故障 1</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-2 h-2 rounded-full bg-yellow-400" />
          <span className="text-xs text-white">维保 1</span>
        </div>
      </div>
      
      {/* 底部中间提示 */}
      <div className="absolute bottom-4 right-4 bg-[#111827]/90 border border-[#1F2937] rounded-lg px-3 py-2">
        <p className="text-xs text-gray-400">
          <Move className="w-3 h-3 inline mr-1" />
          拖拽旋转 · 滚轮缩放
        </p>
      </div>
    </div>
  );
}
