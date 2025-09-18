import React from 'react';
import { Project } from '../../types/project';

interface ProjectCardProps {
  project: Project;
}

const ProjectCard: React.FC<ProjectCardProps> = ({ project }) => {
  return (
    <a 
      href={project.githubUrl}
      target="_blank"
      rel="noopener noreferrer"
      className="group block tui-panel mb-4 hover:bg-tui-border hover:bg-opacity-20 focus:outline-2 focus:outline-tui-accent focus:outline-offset-2 transition-all duration-200"
    >
      <div className="tui-panel-header">
        <div className="flex items-center justify-between">
          <h3 className="font-mono text-sm text-tui-light">{project.name}</h3>
          {project.status && (
            <span className="text-xs text-tui-accent uppercase tracking-wide">
              {project.status}
            </span>
          )}
        </div>
      </div>
      
      <div className="p-4">
        <div className="text-tui-accent text-xs mb-2">{'//'} DESCRIPTION</div>
        <p className="text-tui-light text-sm mb-4 leading-relaxed">
          {project.description}
        </p>
        
        <div className="text-tui-accent text-xs mb-2">{'//'} TECH STACK</div>
        <p className="text-tui-muted text-xs mb-4 font-mono">
          {project.stack}
        </p>
        
        <div className="flex items-center justify-between">
          <div className="text-tui-muted text-xs">
            GitHub: {project.githubUrl.split('/').pop()}
          </div>
          <span className="text-tui-accent text-xs group-hover:text-tui-light transition-colors duration-150">
            VIEW CODE →
          </span>
        </div>
      </div>
    </a>
  );
};

export default ProjectCard;