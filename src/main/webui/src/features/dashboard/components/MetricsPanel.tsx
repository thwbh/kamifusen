import React from 'react';

interface MetricsPanelProps {
  title: string;
  subtitle?: string;
  primaryMetric: {
    label: string;
    value: string;
    unit?: string;
  };
  showChart?: boolean;
  chartColor?: string;
}

const MetricsPanel: React.FC<MetricsPanelProps> = ({ 
  title,
  subtitle,
  primaryMetric,
  showChart = true,
  chartColor = 'bg-tui-red'
}) => {
  return (
    <div className="tui-panel mx-4 mb-4 flex-shrink-0">
      <div className="tui-panel-header">
        <h3 className="font-mono text-xs">{'//'} {title}</h3>
      </div>
      <div className="p-4">
        {subtitle && (
          <div className="text-tui-muted text-xs mb-2">
            {subtitle}
          </div>
        )}
        <div className="text-tui-light text-sm mb-2">{primaryMetric.label}</div>
        <div className="text-tui-accent text-3xl font-mono mb-4">
          {primaryMetric.value}{primaryMetric.unit || ''}
        </div>
        
        {/* Chart */}
        {showChart && (
          <div className="flex items-end space-x-1 h-16 mb-4">
            {Array.from({ length: 20 }, (_, i) => (
              <div
                key={i}
                className={`w-2 ${chartColor} bg-opacity-60`}
                style={{ height: `${Math.random() * 60 + 10}%` }}
              ></div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default MetricsPanel;