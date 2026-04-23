import { useState, useEffect, useCallback } from 'react';

interface UseRealtimeDataOptions<T> {
  initialData: T;
  updateInterval?: number;
  updateFn?: (prev: T) => T;
}

export function useRealtimeData<T>(
  options: UseRealtimeDataOptions<T>
) {
  const { initialData, updateInterval = 5000, updateFn } = options;
  const [data, setData] = useState<T>(initialData);
  const [lastUpdate, setLastUpdate] = useState<Date>(new Date());

  const refreshData = useCallback(() => {
    if (updateFn) {
      setData(prev => updateFn(prev));
    }
    setLastUpdate(new Date());
  }, [updateFn]);

  useEffect(() => {
    const timer = setInterval(refreshData, updateInterval);
    return () => clearInterval(timer);
  }, [refreshData, updateInterval]);

  return {
    data,
    setData,
    lastUpdate,
    refreshData
  };
}

// Simulate random data fluctuation
export function simulateDataChange(
  value: number,
  minChange: number = -5,
  maxChange: number = 5,
  minValue?: number,
  maxValue?: number
): number {
  const change = Math.random() * (maxChange - minChange) + minChange;
  let newValue = value + change;
  
  if (minValue !== undefined) {
    newValue = Math.max(newValue, minValue);
  }
  if (maxValue !== undefined) {
    newValue = Math.min(newValue, maxValue);
  }
  
  return Number(newValue.toFixed(1));
}
