# 智慧工厂可视化看板 - 技术规划文档

---

## 1. 组件清单

### shadcn/ui 内置组件

| 组件名 | 用途 | 安装命令 |
|--------|------|----------|
| Card | KPI卡片、信息面板 | npx shadcn add card |
| Button | 交互按钮 | npx shadcn add button |
| Badge | 状态标签 | npx shadcn add badge |
| Tabs | 看板切换 | npx shadcn add tabs |
| ScrollArea | 滚动列表 | npx shadcn add scroll-area |
| Tooltip | 悬浮提示 | npx shadcn add tooltip |
| Progress | 进度条 | npx shadcn add progress |
| Separator | 分割线 | npx shadcn add separator |
| Avatar | 用户头像 | npx shadcn add avatar |
| Alert | 警告提示 | npx shadcn add alert |

### 第三方组件

| 组件名 | 来源 | 用途 |
|--------|------|------|
| @react-three/fiber | npm | 3D场景渲染 |
| @react-three/drei | npm | 3D辅助组件 |
| recharts | npm | 数据图表 |
| framer-motion | npm | 动画效果 |
| lucide-react | npm | 图标库 |
| date-fns | npm | 日期处理 |

### 自定义组件

| 组件名 | 用途 | 位置 |
|--------|------|------|
| KPICard | KPI数字卡片 | components/KPICard.tsx |
| StatusBadge | 状态标签 | components/StatusBadge.tsx |
| DashboardLayout | 看板布局容器 | components/DashboardLayout.tsx |
| Factory3DView | 3D工厂场景 | sections/Factory3DView.tsx |
| ProductionMonitor | 生产监控 | sections/ProductionMonitor.tsx |
| EnvironmentMonitor | 环境监控 | sections/EnvironmentMonitor.tsx |
| SafetyMonitor | 安全监控 | sections/SafetyMonitor.tsx |
| EnergyMonitor | 能耗监控 | sections/EnergyMonitor.tsx |
| VideoMonitor | 视频监控 | sections/VideoMonitor.tsx |
| AlertList | 报警列表 | components/AlertList.tsx |
| ChartCard | 图表卡片 | components/ChartCard.tsx |

---

## 2. 动画实现规划

| 动画效果 | 库 | 实现方式 | 复杂度 |
|----------|-----|----------|--------|
| 页面入场动画 | Framer Motion | AnimatePresence + motion.div | Medium |
| 数字跳动效果 | Framer Motion | useSpring + animate | Medium |
| 卡片悬浮效果 | Framer Motion | whileHover | Low |
| 标签切换动画 | Framer Motion | layoutId | Low |
| 图表数据动画 | Recharts | animationDuration | Low |
| 3D场景交互 | React Three Fiber | useFrame + useGesture | High |
| 报警闪烁效果 | CSS + Framer | keyframes + animate | Low |
| 滚动列表动画 | Framer Motion | motion.div + stagger | Medium |
| 进度条动画 | Framer Motion | useSpring | Low |
| 仪表盘动画 | Recharts + CSS | 自定义仪表盘组件 | Medium |

### 动画详细规格

**页面入场动画**
```typescript
// 使用 Framer Motion variants
const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.1,
      delayChildren: 0.2
    }
  }
}

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: {
      duration: 0.5,
      ease: [0.25, 0.1, 0.25, 1]
    }
  }
}
```

**数字跳动效果**
```typescript
// 使用 useSpring
const animatedValue = useSpring(value, {
  stiffness: 100,
  damping: 30,
  duration: 800
})
```

**报警闪烁效果**
```css
@keyframes pulse-red {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.alert-pulse {
  animation: pulse-red 1s ease-in-out infinite;
}
```

---

## 3. 项目文件结构

```
/mnt/okcomputer/output/app/
├── src/
│   ├── components/           # 通用组件
│   │   ├── KPICard.tsx
│   │   ├── StatusBadge.tsx
│   │   ├── DashboardLayout.tsx
│   │   ├── AlertList.tsx
│   │   ├── ChartCard.tsx
│   │   ├── NavigationBar.tsx
│   │   └── StatusBar.tsx
│   ├── sections/             # 看板区块
│   │   ├── Factory3DView.tsx
│   │   ├── ProductionMonitor.tsx
│   │   ├── EnvironmentMonitor.tsx
│   │   ├── SafetyMonitor.tsx
│   │   ├── EnergyMonitor.tsx
│   │   └── VideoMonitor.tsx
│   ├── hooks/                # 自定义Hooks
│   │   ├── useRealtimeData.ts
│   │   ├── useCountUp.ts
│   │   └── useCurrentTime.ts
│   ├── lib/                  # 工具函数
│   │   └── utils.ts
│   ├── types/                # TypeScript类型
│   │   └── index.ts
│   ├── data/                 # 模拟数据
│   │   └── mockData.ts
│   ├── App.tsx
│   ├── App.css
│   ├── main.tsx
│   └── index.css
├── public/                   # 静态资源
├── components/ui/            # shadcn组件
├── index.html
├── package.json
├── tailwind.config.js
├── tsconfig.json
└── vite.config.ts
```

---

## 4. 依赖安装清单

```bash
# 核心依赖
npm install framer-motion recharts @react-three/fiber @react-three/drei
npm install lucide-react date-fns

# shadcn组件
npx shadcn add card button badge tabs scroll-area tooltip progress separator avatar alert
```

---

## 5. 关键技术实现

### 5.1 3D场景渲染
- 使用 @react-three/fiber 创建3D画布
- 使用 @react-three/drei 的辅助组件(OrbitControls, Grid等)
- 简化的3D工厂模型(立方体组合表示建筑)
- 交互: 鼠标旋转、缩放、点击高亮

### 5.2 实时数据模拟
- 使用 setInterval 模拟实时数据更新
- 使用 React Context 管理全局状态
- 数据更新触发组件重渲染和动画

### 5.3 图表实现
- 使用 Recharts 实现各类图表
- 柱状图: 能耗对比、OEE监控
- 折线图: 趋势分析
- 饼图/环形图: 能耗占比
- 仪表盘: 碳排放进度

### 5.4 响应式布局
- 使用 Tailwind CSS 的响应式类
- 大屏: 完整多列布局
- 中等屏幕: 简化布局
- 使用 CSS Grid 和 Flexbox

---

## 6. 性能优化策略

1. **组件懒加载**: 使用 React.lazy 和 Suspense
2. **图表优化**: 使用 Recharts 的 isAnimationActive 控制
3. **3D优化**: 限制渲染帧率, 使用简单几何体
4. **动画优化**: 使用 will-change 和 transform
5. **数据优化**: 使用 useMemo 和 useCallback

---

## 7. 开发顺序

1. 项目初始化 + 基础配置
2. 通用组件开发 (KPICard, StatusBadge等)
3. 导航栏 + 布局框架
4. 工厂总览看板 (3D场景)
5. 生产监控看板
6. 环境监控看板
7. 安全监控看板
8. 能耗监控看板
9. 视频融合看板
10. 动画效果完善
11. 响应式适配
12. 构建部署
