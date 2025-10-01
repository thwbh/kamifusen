import React, { useEffect, useState } from 'react';
import { StatusEnum } from '../../../api';
import { useSystemHealth } from '../hooks/useSystemHealth';

const SystemHealth: React.FC = () => {
  const { healthData, responseTime, errorCount, totalRequests } = useSystemHealth(5000);
  const [selectedMetric, setSelectedMetric] = useState(0);
  const [loginDuration, setLoginDuration] = useState<number | null>(null);

  useEffect(() => {
    // Calculate login duration from form-submit to redirect
    const formSubmitTime = sessionStorage.getItem('form-submit-ts');
    const redirectTime = sessionStorage.getItem('redirect-ts');

    if (formSubmitTime && redirectTime) {
      const duration = parseInt(redirectTime) - parseInt(formSubmitTime);
      setLoginDuration(duration);
    }
  }, []);

  // Helper function to get metric value by name
  const getMetricValue = (name: string): string => {
    const metric = healthData?.metrics?.find(m => m.name === name);
    return metric?.value || '---';
  };

  const memoryUsage = getMetricValue('Memory Usage');
  const errorRate = totalRequests > 0 ? ((errorCount / totalRequests) * 100).toFixed(1) : '0.0';

  const healthMetrics = React.useMemo(() => {
    const metrics = [
      {
        name: 'Login Time',
        value: loginDuration ? `${loginDuration}ms` : '---',
        status: loginDuration && loginDuration < 1000 ? StatusEnum.Healthy : loginDuration && loginDuration < 2000 ? StatusEnum.Warning : StatusEnum.Critical,
        description: 'Time from form submission to dashboard redirect'
      },
      {
        name: 'Response Time',
        value: responseTime ? `${responseTime}ms` : '---',
        status: responseTime && responseTime < 200 ? StatusEnum.Healthy : responseTime && responseTime < 500 ? StatusEnum.Warning : StatusEnum.Critical,
        description: 'System health check API response time (5s interval)'
      },
      {
        name: 'Error Rate',
        value: `${errorRate}%`,
        status: parseFloat(errorRate) < 1 ? StatusEnum.Healthy : parseFloat(errorRate) < 5 ? StatusEnum.Warning : StatusEnum.Critical,
        description: `Failed health check requests in this session (${errorCount}/${totalRequests})`
      },
      {
        name: 'Memory Usage',
        value: memoryUsage,
        status: StatusEnum.Healthy,
        description: 'JVM heap memory utilization'
      }
    ];

    return metrics;
  }, [loginDuration, responseTime, errorRate, errorCount, totalRequests, memoryUsage]);

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
    const criticalCount = healthMetrics.filter(m => m.status === StatusEnum.Critical).length;
    const warningCount = healthMetrics.filter(m => m.status === StatusEnum.Warning).length;

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
            {healthMetrics && healthMetrics.map((metric, index) => (
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
        {healthMetrics && selectedMetric < healthMetrics.length && (
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
