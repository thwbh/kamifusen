import React from 'react';

interface TimeSlotsProps {
  timeSlots: string[];
}

const TimeSlots: React.FC<TimeSlotsProps> = ({ timeSlots }) => {
  return (
    <div className="absolute bottom-0 left-0 right-0 bg-tui-darker border-t border-tui-border">
      <div className="flex">
        {timeSlots.map((slot, index) => (
          <div
            key={index}
            className={`flex-1 p-2 text-center text-xs border-r border-tui-border ${
              index < 6 ? 'text-tui-muted' : index < 10 ? 'text-tui-green' : 'text-tui-red'
            }`}
          >
            {slot}
          </div>
        ))}
      </div>
      <div className="flex text-xs text-tui-muted">
        <div className="flex-1 text-center py-1 border-r border-tui-border">SYNC</div>
        <div className="flex-1 text-center py-1 border-r border-tui-border">CONFIGURATION</div>
        <div className="flex-1 text-center py-1 border-r border-tui-border">UNSALTERED</div>
        <div className="flex-1 text-center py-1 border-r border-tui-border">UNSALTERED</div>
        <div className="flex-1 text-center py-1 border-r border-tui-border">UNSALTERED</div>
        <div className="flex-1 text-center py-1 border-r border-tui-border">UNSALTERED</div>
        <div className="flex-1 text-center py-1">FUNC-01</div>
      </div>
    </div>
  );
};

export default TimeSlots;