import React, { useState } from 'react';
import TimelineCard from './TimelineCard';
import { TimelineItem } from '../../types/timeline';

interface TimelineProps {
  items: TimelineItem[];
  title?: string;
  interactive?: boolean;
}

const Timeline: React.FC<TimelineProps> = ({ items, title, interactive = false }) => {
  const [selectedIndex, setSelectedIndex] = useState<number | null>(null);

  const handleCardClick = (index: number) => {
    if (interactive) {
      setSelectedIndex(selectedIndex === index ? null : index);
    }
  };

  const handleKeyPress = (event: React.KeyboardEvent, index: number) => {
    if (event.key === 'Enter' || event.key === ' ') {
      handleCardClick(index);
    }
  };

  return (
    <div className="relative">
      
      <div className="tui-timeline relative">
        {/* Vertical timeline line */}
        <div className="tui-timeline-line"></div>
        
        {/* Timeline items */}
        <div className="space-y-8">
          {items.map((item, index) => (
            <div
              key={item.id}
              className="relative"
              tabIndex={interactive ? 0 : undefined}
              onKeyDown={interactive ? (e) => handleKeyPress(e, index) : undefined}
            >
              <TimelineCard
                item={item}
                isSelected={selectedIndex === index}
                onClick={interactive ? () => handleCardClick(index) : undefined}
              />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default Timeline;