import React, { useState, useEffect } from 'react';

interface HealthMetric {
  name: string;
  value: string;
  status: 'healthy' | 'warning' | 'critical';
  description: string;
}

const SystemHealth: React.FC = () => {
  const [healthMetrics, setHealthMetrics] = useState<HealthMetric[]>([
    { name: 'API Response Time', value: '~120ms', status: 'healthy', description: 'Average response time for admin endpoints' },
    { name: 'Database Connection', value: 'ONLINE', status: 'healthy', description: 'PostgreSQL connection status' },
    { name: 'Error Rate', value: '<0.1%', status: 'healthy', description: 'Failed requests in last hour' },
    { name: 'Memory Usage', value: '67%', status: 'warning', description: 'JVM heap memory utilization' },
    { name: 'Session Store', value: 'ACTIVE', status: 'healthy', description: 'User session management status' },
  ]);

  const [selectedMetric, setSelectedMetric] = useState(0);

  // Simulate real-time updates
  useEffect(() => {
    const interval = setInterval(() => {
      setHealthMetrics(prev => prev.map((metric, index) => {
        if (metric.name === 'API Response Time') {
          const responseTime = 80 + Math.random() * 100; // 80-180ms
          const status = responseTime > 150 ? 'warning' : responseTime > 200 ? 'critical' : 'healthy';
          return { ...metric, value: `~${Math.round(responseTime)}ms`, status };
        }
        if (metric.name === 'Error Rate') {
          const errorRate = Math.random() * 0.5; // 0-0.5%
          const status = errorRate > 0.3 ? 'warning' : errorRate > 0.5 ? 'critical' : 'healthy';
          return { ...metric, value: `${errorRate.toFixed(2)}%`, status };
        }
        if (metric.name === 'Memory Usage') {
          const memUsage = 50 + Math.random() * 30; // 50-80%
          const status = memUsage > 75 ? 'warning' : memUsage > 85 ? 'critical' : 'healthy';
          return { ...metric, value: `${Math.round(memUsage)}%`, status };
        }
        return metric;
      }));
    }, 5000); // Update every 5 seconds

    return () => clearInterval(interval);
  }, []);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'healthy': return 'text-tui-green';
      case 'warning': return 'text-tui-yellow';
      case 'critical': return 'text-tui-red';
      default: return 'text-tui-muted';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'healthy': return 'bg-tui-green';
      case 'warning': return 'bg-tui-yellow animate-pulse';
      case 'critical': return 'bg-tui-red animate-pulse';
      default: return 'bg-tui-muted';
    }
  };

  const getOverallStatus = () => {
    const criticalCount = healthMetrics.filter(m => m.status === 'critical').length;
    const warningCount = healthMetrics.filter(m => m.status === 'warning').length;

    if (criticalCount > 0) return { status: 'critical', text: 'SYSTEM DEGRADED' };
    if (warningCount > 0) return { status: 'warning', text: 'MONITORING ALERTS' };
    return { status: 'healthy', text: 'ALL SYSTEMS NOMINAL' };
  };

  const overall = getOverallStatus();

  return (
    <div className="tui-panel m-4 flex-shrink-0">
      <div className="tui-panel-header">
        <h3 className="font-mono text-sm">SYSTEM HEALTH</h3>
      </div>
      <div className="p-4">
        <div className="text-tui-accent text-xs mb-2">{'//'} OPERATIONAL STATUS</div>

        {/* Overall status indicator */}
        <div className="mb-4 p-2 border border-tui-border rounded">
          <div className="flex items-center space-x-2">
            <div className={`w-3 h-3 rounded-full ${getStatusIcon(overall.status)}`} />
            <span className={`text-xs font-mono ${getStatusColor(overall.status)}`}>
              {overall.text}
            </span>
          </div>
        </div>

        <table className="w-full text-xs">
          <thead>
            <tr className="text-tui-muted">
              <th className="text-left pb-2">COMPONENT</th>
              <th className="text-right pb-2">STATUS</th>
            </tr>
          </thead>
          <tbody>
            {healthMetrics.map((metric, index) => (
              <tr
                key={index}
                className={`${index === selectedMetric ? 'bg-tui-accent bg-opacity-20' : ''} cursor-pointer hover:bg-tui-border hover:bg-opacity-50 transition-colors duration-150`}
                onClick={() => setSelectedMetric(index)}
                title={metric.description}
              >
                <td className="py-1 text-tui-light flex items-center">
                  <div className={`w-2 h-2 rounded-full mr-2 ${getStatusIcon(metric.status)}`}></div>
                  {metric.name}
                </td>
                <td className={`text-right py-1 font-mono ${getStatusColor(metric.status)}`}>
                  {metric.value}
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {/* Selected metric details */}
        {selectedMetric < healthMetrics.length && (
          <div className="mt-4 p-2 bg-tui-background border border-tui-border rounded">
            <div className="text-tui-muted text-xs">
              {healthMetrics[selectedMetric].description}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default SystemHealth;