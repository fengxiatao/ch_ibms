interface GaugeChartProps {
  value: number;
  color?: string;
  size?: number;
  label?: string;
}

export function GaugeChart({ value, color = '#3B82F6', size = 120, label }: GaugeChartProps) {
  const strokeWidth = size * 0.08;
  const radius = (size - strokeWidth) / 2;
  const circumference = radius * Math.PI;
  const progress = Math.min(value, 100) / 100;
  const strokeDashoffset = circumference * (1 - progress);

  return (
    <div className="relative inline-flex items-center justify-center">
      <svg width={size} height={size * 0.6} viewBox={`0 0 ${size} ${size * 0.6}`}>
        {/* Background arc */}
        <path
          d={`M ${strokeWidth} ${size * 0.3} A ${radius} ${radius} 0 0 1 ${size - strokeWidth} ${size * 0.3}`}
          fill="none"
          stroke="#374151"
          strokeWidth={strokeWidth}
          strokeLinecap="round"
        />
        {/* Progress arc */}
        <path
          d={`M ${strokeWidth} ${size * 0.3} A ${radius} ${radius} 0 0 1 ${size - strokeWidth} ${size * 0.3}`}
          fill="none"
          stroke={color}
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={strokeDashoffset}
          style={{ transition: 'stroke-dashoffset 0.5s ease' }}
        />
        {/* Center text */}
        <text
          x={size / 2}
          y={size * 0.35}
          textAnchor="middle"
          fill="#fff"
          fontSize={size * 0.15}
          fontWeight="bold"
        >
          {value}%
        </text>
        {label && (
          <text
            x={size / 2}
            y={size * 0.5}
            textAnchor="middle"
            fill="#9CA3AF"
            fontSize={size * 0.08}
          >
            {label}
          </text>
        )}
      </svg>
    </div>
  );
}
