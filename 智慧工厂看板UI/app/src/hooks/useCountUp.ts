import { useState, useEffect, useRef } from 'react';

interface UseCountUpOptions {
  duration?: number;
  decimals?: number;
}

export function useCountUp(
  endValue: number,
  options: UseCountUpOptions = {}
) {
  const { duration = 800, decimals = 0 } = options;
  const [displayValue, setDisplayValue] = useState(0);
  const startTimeRef = useRef<number | null>(null);
  const startValueRef = useRef(0);
  const animationRef = useRef<number | null>(null);

  useEffect(() => {
    const animate = (timestamp: number) => {
      if (!startTimeRef.current) {
        startTimeRef.current = timestamp;
        startValueRef.current = displayValue;
      }

      const elapsed = timestamp - startTimeRef.current;
      const progress = Math.min(elapsed / duration, 1);
      
      // Ease out expo
      const easeOutExpo = progress === 1 ? 1 : 1 - Math.pow(2, -10 * progress);
      
      const currentValue = startValueRef.current + (endValue - startValueRef.current) * easeOutExpo;
      
      setDisplayValue(Number(currentValue.toFixed(decimals)));

      if (progress < 1) {
        animationRef.current = requestAnimationFrame(animate);
      } else {
        startTimeRef.current = null;
      }
    };

    animationRef.current = requestAnimationFrame(animate);

    return () => {
      if (animationRef.current) {
        cancelAnimationFrame(animationRef.current);
      }
      startTimeRef.current = null;
    };
  }, [endValue, duration, decimals]);

  return displayValue;
}
