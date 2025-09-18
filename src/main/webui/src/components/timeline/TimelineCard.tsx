import React from 'react';
import { TimelineItem } from '../../types/timeline';

interface TimelineCardProps {
  item: TimelineItem;
  isSelected?: boolean;
  onClick?: () => void;
}

const TimelineCard: React.FC<TimelineCardProps> = ({ item, isSelected, onClick }) => {
  return (
    <div 
      className={`tui-timeline-card ${isSelected ? 'selected' : ''} ${onClick ? 'cursor-pointer' : ''}`}
      onClick={onClick}
    >
      {/* Timeline connector dot */}
      <div className="tui-timeline-dot"></div>
      
      {/* Card content */}
      <div className="tui-panel">
        <div className="tui-panel-header">
          <h3 className="font-mono text-sm text-tui-accent">{item.company}</h3>
          {item.url && (
            <a 
              href={item.url} 
              target="_blank" 
              rel="noopener noreferrer"
              className="text-tui-muted text-xs hover:text-tui-accent transition-colors duration-200"
              onClick={(e) => e.stopPropagation()}
            >
              {item.url}
            </a>
          )}
        </div>
        
        <div className="p-4 space-y-3">
          <div className="grid grid-cols-2 gap-4 text-xs">
            <div>
              <span className="text-tui-accent">{'//'} DURATION:</span>
              <div className="text-tui-light mt-1">{item.timeframe}</div>
            </div>
            <div>
              <span className="text-tui-accent">{'//'} ROLE:</span>
              <div className="text-tui-light mt-1">{item.role}</div>
            </div>
          </div>
          
          <div>
            <span className="text-tui-accent text-xs">{'//'} STACK:</span>
            <div className="text-tui-muted text-xs mt-1 font-mono leading-relaxed">
              {item.stack}
            </div>
          </div>
          
          <div>
            <span className="text-tui-accent text-xs">{'//'} DESCRIPTION:</span>
            <div className="text-tui-light text-xs mt-1 leading-relaxed">
              {item.description}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TimelineCard;