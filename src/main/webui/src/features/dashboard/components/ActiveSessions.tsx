import React from 'react';
import { useActiveSessions } from '../hooks';

const ActiveSessions: React.FC = () => {
  const { activeCount, timestamp, loading, error } = useActiveSessions();

  const formatTimestamp = (date: Date | null) => {
    if (!date) return '';
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  };

  const getStatusColor = () => {
    if (loading) return 'text-tui-muted';
    if (error) return 'text-tui-red';
    if (activeCount === 0) return 'text-tui-muted';
    if (activeCount < 5) return 'text-tui-green';
    if (activeCount < 20) return 'text-tui-accent';
    return 'text-tui-red'; // High traffic
  };

  const generateActivityBars = () => {
    // Generate visual representation of activity levels
    return Array.from({ length: 20 }, (_, i) => {
      const height = loading ? 20 : Math.min(Math.max((activeCount / 50) * 100, 10), 100);
      const barHeight = height + (Math.random() * 20 - 10); // Add some variance
      const opacity = activeCount > i * 2.5 ? 1 : 0.3;

      return (
        <div
          key={i}
          className={`w-2 ${error ? 'bg-tui-red' : activeCount > 10 ? 'bg-tui-accent' : 'bg-tui-green'}`}
          style={{
            height: `${Math.max(barHeight, 15)}%`,
            opacity: opacity
          }}
        />
      );
    });
  };

  return (
    <div className="tui-panel mx-4 mb-4 flex-shrink-0">
      <div className="tui-panel-header">
        <h3 className="font-mono text-xs">{'//'} ACTIVE VISITORS</h3>
      </div>
      <div className="p-4">
        <div className="text-tui-muted text-xs mb-2">
          {error ? (
            'CONNECTION ERROR: UNABLE TO FETCH VISITOR DATA'
          ) : loading ? (
            'SYNCHRONIZING VISITOR DATA...'
          ) : activeCount === 0 ? (
            'NO ACTIVE VISITORS DETECTED IN LAST 30 MINUTES'
          ) : activeCount < 5 ? (
            'LOW TRAFFIC DETECTED. NORMAL OPERATION.'
          ) : activeCount < 20 ? (
            'MODERATE TRAFFIC LEVELS. SYSTEM OPERATING NORMALLY.'
          ) : (
            'HIGH TRAFFIC DETECTED. MONITORING SYSTEM PERFORMANCE.'
          )}
        </div>

        <div className="text-tui-light text-sm mb-2">LIVE VISITORS</div>
        <div className={`text-3xl font-mono mb-2 ${getStatusColor()}`}>
          {loading ? '---' : error ? 'ERR' : activeCount}
        </div>

        {timestamp && (
          <div className="text-tui-muted text-xs mb-4">
            LAST UPDATE: {formatTimestamp(timestamp)}
          </div>
        )}

        {/* Activity visualization */}
        <div className="flex items-end space-x-1 h-16 mb-4">
          {generateActivityBars()}
        </div>

        {/* Status indicator */}
        <div className="flex items-center space-x-2 text-xs">
          <div className={`w-2 h-2 rounded-full ${loading ? 'bg-tui-muted animate-pulse' : error ? 'bg-tui-red' : 'bg-tui-green'}`} />
          <span className="text-tui-muted">
            {loading ? 'SYNCING' : error ? 'OFFLINE' : 'ONLINE'}
          </span>
        </div>
      </div>
    </div>
  );
};

export default ActiveSessions;