import React, { useEffect } from 'react';
import ProjectCard from './projects/ProjectCard';
import { projectsData } from '../data/projectsData';

interface ProjectsProps {}

const Projects: React.FC<ProjectsProps> = () => {
  useEffect(() => {
    // Auto-focus the component when it mounts
    const timer = setTimeout(() => {
      const projectsDiv = document.querySelector('[data-projects-container]') as HTMLElement;
      if (projectsDiv) {
        projectsDiv.focus({ preventScroll: true });
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
      data-projects-container
    >
      <div className="max-w-4xl mx-auto p-8">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-tui-light text-3xl font-bold mb-2 tracking-widest uppercase">
            PROJECT ARCHIVE
          </h1>
          <h2 className="text-tui-muted text-lg tracking-[0.2em] uppercase">
            PERSONAL DEVELOPMENT INITIATIVES
          </h2>
          <div className="text-tui-accent text-xs mt-4">
            {'//'} {projectsData.length} ACTIVE REPOSITORIES
          </div>
        </div>

        {/* Projects Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
          {projectsData.map((project) => (
            <ProjectCard
              key={project.id}
              project={project}
            />
          ))}
        </div>

        {/* Additional Info */}
        <div className="tui-panel">
          <div className="tui-panel-header">
            <h3 className="font-mono text-sm">{'//'} DEVELOPMENT NOTES</h3>
          </div>
          <div className="p-4">
            <p className="text-tui-light text-sm leading-relaxed mb-4">
              All projects are developed with a focus on clean architecture, 
              modern technology stacks, and open-source principles. Each 
              repository includes comprehensive documentation and setup instructions.
            </p>
            <div className="text-tui-muted text-xs">
              <p>• All projects are hosted on GitHub under MIT or similar licenses</p>
              <p>• Technologies primarily include Kotlin, React, and PostgreSQL</p>
              <p>• Focus on serverless-friendly architectures and cloud deployment</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Projects;