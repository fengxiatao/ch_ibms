import { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { zhCN } from 'date-fns/locale';

export function useCurrentTime() {
  const [currentTime, setCurrentTime] = useState(new Date());

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  const formattedTime = format(currentTime, 'yyyy-MM-dd HH:mm:ss', { locale: zhCN });
  const formattedDate = format(currentTime, 'yyyy年MM月dd日', { locale: zhCN });

  return {
    currentTime,
    formattedTime,
    formattedDate
  };
}
