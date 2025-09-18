import React, { useEffect } from 'react';

interface AboutProps {}

const About: React.FC<AboutProps> = () => {
  useEffect(() => {
    // Auto-focus the component when it mounts, but prevent scrolling
    const timer = setTimeout(() => {
      const aboutDiv = document.querySelector('[data-about-container]') as HTMLElement;
      if (aboutDiv) {
        aboutDiv.focus({ preventScroll: true });
      }
    }, 100);
    return () => clearTimeout(timer);
  }, []);

  const handleKeyPress = (event: React.KeyboardEvent) => {
    // ESC is handled by NavigationDashboard
  };

  const skills = [
    'Java', 'Kotlin', 'Spring Boot', 'Apache Kafka',
    'PostgreSQL', 'MongoDB', 'AWS', 'Kubernetes',
    'Docker', 'Jenkins', 'React', 'Angular'
  ];

  return (
    <div
      className="min-h-screen animate-fade-in overflow-y-auto focus:outline-2 focus:outline-tui-accent focus:outline-offset-2"
      onKeyDown={handleKeyPress}
      tabIndex={0}
      data-about-container
    >
      <div className="max-w-4xl mx-auto p-8">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-tui-light text-3xl font-bold mb-2 tracking-widest uppercase">
            PERSONNEL FILE
          </h1>
          <h2 className="text-tui-muted text-lg tracking-[0.2em] uppercase">
            STEFAN POINDL - SENIOR SOFTWARE ENGINEER
          </h2>
          <div className="text-tui-accent text-xs mt-4">
            {'//'} CLEARANCE LEVEL: AUTHORIZED PERSONNEL ONLY
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Personal Information */}
          <div className="space-y-6">
            <div className="tui-panel">
              <div className="tui-panel-header">
                <h3 className="font-mono text-sm">{'//'} BASIC INFORMATION</h3>
              </div>
              <div className="p-4">
                <div className="space-y-3">
                  <div>
                    <div className="text-tui-accent text-xs">DESIGNATION</div>
                    <div className="text-tui-light text-sm">Senior Software Engineer</div>
                  </div>
                  <div>
                    <div className="text-tui-accent text-xs">SERVICE RECORD</div>
                    <div className="text-tui-light text-sm">Professional Experience Since 2013</div>
                  </div>
                  <div>
                    <div className="text-tui-accent text-xs">CURRENT ASSIGNMENT</div>
                    <div className="text-tui-light text-sm">OpenValue GmbH</div>
                  </div>
                  <div>
                    <div className="text-tui-accent text-xs">SPECIALIZATION</div>
                    <div className="text-tui-light text-sm">Distributed Systems & Backend Architecture</div>
                  </div>
                </div>
              </div>
            </div>

            <div className="tui-panel">
              <div className="tui-panel-header">
                <h3 className="font-mono text-sm">{'//'} MISSION STATEMENT</h3>
              </div>
              <div className="p-4">
                <p className="text-tui-light text-sm leading-relaxed mb-4">
                  Consultant for governmental institutions and globally distributed
                  fintech companies. Technical anchor for development of new features
                  and bug fixes in agile environments.
                </p>
                <p className="text-tui-light text-sm leading-relaxed">
                  Specialized in wearing multiple operational hats: Lead Developer,
                  DevOps Engineer, Software Architect, and Technical Lead. Passionate
                  about clean architecture, modern technology stacks, and open-source development.
                </p>
              </div>
            </div>
          </div>

          {/* Technical Skills */}
          <div className="space-y-6">
            <div className="tui-panel">
              <div className="tui-panel-header">
                <h3 className="font-mono text-sm">{'//'} TECHNICAL PROFICIENCIES</h3>
              </div>
              <div className="p-4">
                <div className="text-tui-accent text-xs mb-3">CORE TECHNOLOGIES</div>
                <div className="grid grid-cols-2 gap-2 mb-4">
                  {skills.map((skill, index) => (
                    <div
                      key={skill}
                      className="text-tui-light text-xs font-mono p-2 bg-tui-darker border border-tui-border"
                    >
                      {skill}
                    </div>
                  ))}
                </div>
                <div className="text-tui-muted text-xs">
                  <p>• Backend systems and microservices architecture</p>
                  <p>• Event-driven systems with Apache Kafka</p>
                  <p>• Cloud-native deployments on AWS and Kubernetes</p>
                  <p>• Modern frontend frameworks and reactive programming</p>
                </div>
              </div>
            </div>

            <div className="tui-panel">
              <div className="tui-panel-header">
                <h3 className="font-mono text-sm">{'//'} EXTERNAL RESOURCES</h3>
              </div>
              <div className="p-4">
                <div className="space-y-3">
                  <a
                    href="https://tohuwabohu.io"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center justify-between p-3 bg-tui-darker border border-tui-accent hover:bg-tui-border hover:bg-opacity-20 transition-colors focus:outline-2 focus:outline-tui-accent focus:outline-offset-2"
                  >
                    <div>
                      <div className="text-tui-light text-sm">Personal Blog</div>
                      <div className="text-tui-muted text-xs">tohuwabohu.io</div>
                    </div>
                    <span className="text-tui-accent">→</span>
                  </a>
                  <a
                    href="https://github.com/thwbh"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center justify-between p-3 bg-tui-darker border border-tui-accent hover:bg-tui-border hover:bg-opacity-20 transition-colors focus:outline-2 focus:outline-tui-accent focus:outline-offset-2"
                  >
                    <div>
                      <div className="text-tui-light text-sm">GitHub Profile</div>
                      <div className="text-tui-muted text-xs">github.com/thwbh</div>
                    </div>
                    <span className="text-tui-accent">→</span>
                  </a>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Career Highlights */}
        <div className="mt-8">
          <div className="tui-panel">
            <div className="tui-panel-header">
              <h3 className="font-mono text-sm">{'//'} CAREER HIGHLIGHTS</h3>
            </div>
            <div className="p-4">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div>
                  <div className="text-tui-accent text-xs mb-2">CONSULTATION</div>
                  <div className="text-tui-light text-sm mb-2">Government & Fintech</div>
                  <div className="text-tui-muted text-xs">
                    Technical consulting for high-stakes governmental and financial institutions
                  </div>
                </div>
                <div>
                  <div className="text-tui-accent text-xs mb-2">ARCHITECTURE</div>
                  <div className="text-tui-light text-sm mb-2">System Design</div>
                  <div className="text-tui-muted text-xs">
                    Leading architectural decisions for distributed systems and microservices
                  </div>
                </div>
                <div>
                  <div className="text-tui-accent text-xs mb-2">INNOVATION</div>
                  <div className="text-tui-light text-sm mb-2">Open Source</div>
                  <div className="text-tui-muted text-xs">
                    Active contribution to open-source projects and modern development practices
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default About;
