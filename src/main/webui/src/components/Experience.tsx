import React, { useEffect } from 'react';
import Timeline from './timeline/Timeline';
import { experienceData } from '../data/timelineData';

interface ExperienceProps {}

const Experience: React.FC<ExperienceProps> = () => {
  useEffect(() => {
    // Auto-focus the component when it mounts
    const timer = setTimeout(() => {
      const experienceDiv = document.querySelector('[data-experience-container]') as HTMLElement;
      if (experienceDiv) {
        experienceDiv.focus({ preventScroll: true });
      }
    }, 100);
    return () => clearTimeout(timer);
  }, []);

  const handleKeyPress = (event: React.KeyboardEvent) => {
    // ESC is handled by NavigationDashboard
  };

  return (
    <div 
      className="min-h-screen animate-fade-in overflow-y-auto focus:outline-2 focus:outline-tui-accent focus:outline-offset-2" 
      onKeyDown={handleKeyPress} 
      tabIndex={0}
      data-experience-container
    >
      <div className="max-w-4xl mx-auto p-8">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-tui-light text-3xl font-bold mb-2 tracking-widest uppercase">
            EXPERIENCE TIMELINE
          </h1>
          <h2 className="text-tui-muted text-lg tracking-[0.2em] uppercase">
            PROFESSIONAL CAREER TRAJECTORY
          </h2>
          <div className="text-tui-accent text-xs mt-4">
            {'//'} {experienceData.length} CAREER MILESTONES
          </div>
        </div>

        <Timeline 
          items={experienceData}
          interactive={true}
        />
      </div>
    </div>
  );
};

export default Experience;