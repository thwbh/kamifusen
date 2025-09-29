import React, { useState, useEffect } from 'react';
import type { SystemHealthDto } from '../../../api';
import { getAppVersion } from '../../../shared/utils/version';

interface BottomSlotsProps {
  healthData: SystemHealthDto | null;
  loginTime: Date;
}

const BottomSlots: React.FC<BottomSlotsProps> = ({ healthData, loginTime }) => {
  const [currentTime, setCurrentTime] = useState(new Date());

  // Update current time every second
  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  // Helper function to get metric value by name
  const getMetricValue = (name: string): string => {
    const metric = healthData?.metrics?.find(m => m.name === name);
    return metric?.value || '---';
  };

  // Format time as HH:MM
  const formatTime = (date: Date): string => {
    return date.toLocaleTimeString('en-GB', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    });
  };

  const javaVersion = getMetricValue('Java Version');
  const runtimeType = getMetricValue('Runtime Type');
  const memoryUsage = getMetricValue('Memory Usage');
  const dbSize = getMetricValue('Database Size');
  const appVersion = getAppVersion();

  const loginTimeFormatted = formatTime(loginTime);
  const utcTime = formatTime(new Date(currentTime.getTime() + currentTime.getTimezoneOffset() * 60000));

  const buildTime = healthData?.timestamp ?
    new Date(healthData.timestamp).toLocaleDateString('en-GB', { day: '2-digit', month: '2-digit' }) :
    '--/--';

  // Calculate additional values for the slots
  const nextHealthCheck = new Date(currentTime.getTime() + 5000); // 5 second refresh interval
  const nextCheckFormatted = formatTime(nextHealthCheck);

  const hashedLoginTime = (loginTime.getTime() % 10000).toString(16).toUpperCase().padStart(4, '0');
  const userName = 'admin'; // Could be extracted from context/props if available

  const refreshInterval = '5s';
  const healthResponseTime = '~120ms'; // Could be calculated from actual response times

  //  const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone.split('/').pop() || 'UTC';
  //  const currentTimestamp = formatTime(currentTime);

  // Create 7 dual-value slots
  const dualSlots = [
    { left: loginTimeFormatted, right: nextCheckFormatted }, // SYNC
    { left: appVersion, right: buildTime }, // CONF
    { left: memoryUsage, right: dbSize }, // MEM
    { left: javaVersion, right: runtimeType }, // RUN
    { left: hashedLoginTime, right: userName }, // USER
    { left: refreshInterval, right: healthResponseTime }, // INIT
    { left: utcTime, right: 'UTC' } // TIME
  ];

  return (
    <div className="absolute bottom-0 left-0 right-0 bg-tui-darker border-t border-tui-border">
      <div className="flex">
        {dualSlots.map((slot, index) => (
          <div
            key={index}
            className={`flex-1 p-1 text-center text-xs border-r border-tui-border ${index < 3 ? 'text-tui-muted' : index < 5 ? 'text-tui-green' : 'text-tui-red'
              }`}
          >
            <div className="flex justify-between">
              <span className="w-1/2 text-center border-r border-tui-border">{slot.left}</span>
              <span className="w-1/2 text-center">{slot.right}</span>
            </div>
          </div>
        ))}
      </div>
      <div className="flex text-xs text-tui-muted">
        <div className="flex-1 text-center py-1 border-r border-tui-border">SYNC</div>
        <div className="flex-1 text-center py-1 border-r border-tui-border">CONF</div>
        <div className="flex-1 text-center py-1 border-r border-tui-border">MEM</div>
        <div className="flex-1 text-center py-1 border-r border-tui-border">RUN</div>
        <div className="flex-1 text-center py-1 border-r border-tui-border">USER</div>
        <div className="flex-1 text-center py-1 border-r border-tui-border">INIT</div>
        <div className="flex-1 text-center py-1">TIME</div>
      </div>
    </div>
  );
};

export default BottomSlots;
