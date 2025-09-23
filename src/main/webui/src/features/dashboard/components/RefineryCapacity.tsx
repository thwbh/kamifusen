import React from 'react';

interface RefineryCapacityProps {
  currentCapacity: number;
}

const RefineryCapacity: React.FC<RefineryCapacityProps> = ({ currentCapacity }) => {
  return (
    <div className="tui-panel mx-4 mb-4 flex-shrink-0">
      <div className="tui-panel-header">
        <h3 className="font-mono text-xs">{'//'} REFINERY CAPACITY</h3>
      </div>
      <div className="p-4">
        <div className="text-tui-muted text-xs mb-2">
          REFINERY CURRENTLY HAS AN EXTREME WORKLOAD. A LARGE SURCHARGE WILL BE ADDED.
        </div>
        <div className="text-tui-light text-sm mb-2">CURRENT CAPACITY</div>
        <div className="text-tui-accent text-3xl font-mono mb-4">{currentCapacity}%</div>
        
        {/* Capacity bar chart */}
        <div className="flex items-end space-x-1 h-16 mb-4">
          {Array.from({ length: 20 }, (_, i) => (
            <div
              key={i}
              className="w-2 bg-tui-red bg-opacity-60"
              style={{ height: `${Math.random() * 60 + 10}%` }}
            ></div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default RefineryCapacity;