import React, { useEffect } from 'react';

interface ContactProps {}

interface ContactLink {
  label: string;
  value: string;
  url: string;
  type: 'email' | 'social' | 'website';
}

const Contact: React.FC<ContactProps> = () => {

  useEffect(() => {
    // Auto-focus the component when it mounts
    const timer = setTimeout(() => {
      const contactDiv = document.querySelector('[data-contact-container]') as HTMLElement;
      if (contactDiv) {
        contactDiv.focus({ preventScroll: true });
      }
    }, 100);
    return () => clearTimeout(timer);
  }, []);

  const contactLinks: ContactLink[] = [
    {
      label: 'EMAIL',
      value: 'contact@poindl.info',
      url: 'mailto:contact@poindl.info',
      type: 'email'
    },
    {
      label: 'LINKEDIN',
      value: 'stefan-poindl',
      url: 'https://www.linkedin.com/in/stefan-poindl/',
      type: 'social'
    },
    {
      label: 'XING',
      value: 'Stefan_Poindl',
      url: 'https://www.xing.com/profile/Stefan_Poindl/cv',
      type: 'social'
    },
    {
      label: 'GITHUB',
      value: 'thwbh',
      url: 'https://github.com/thwbh',
      type: 'social'
    },
    {
      label: 'BLOG',
      value: 'tohuwabohu.io',
      url: 'https://tohuwabohu.io',
      type: 'website'
    }
  ];

  const handleKeyPress = (event: React.KeyboardEvent) => {
    // ESC is handled by NavigationDashboard
  };


  return (
    <div
      className="min-h-screen animate-fade-in overflow-y-auto focus:outline-2 focus:outline-tui-accent focus:outline-offset-2"
      onKeyDown={handleKeyPress}
      tabIndex={0}
      data-contact-container
    >
      <div className="max-w-4xl mx-auto p-8">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-tui-light text-3xl font-bold mb-2 tracking-widest uppercase">
            COMMUNICATION CHANNELS
          </h1>
          <h2 className="text-tui-muted text-lg tracking-[0.2em] uppercase">
            PROFESSIONAL CONTACT PROTOCOLS
          </h2>
          <div className="text-tui-accent text-xs mt-4">
            {'//'} AUTHORIZED COMMUNICATION ENDPOINTS
          </div>
        </div>

        {/* Contact Methods */}
        <div className="tui-panel mb-8">
          <div className="tui-panel-header">
            <h3 className="font-mono text-sm">{'//'} CONTACT MATRIX</h3>
          </div>
          <div className="p-4">
            <div className="space-y-3">
              {contactLinks.map((contact, index) => (
                <a
                  key={contact.label}
                  href={contact.url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="group block p-3 border border-tui-border hover:bg-tui-border hover:bg-opacity-20 focus:border-tui-accent focus:bg-tui-accent focus:text-tui-dark transition-all duration-200 focus:outline-none"
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-4">
                      <div className="text-sm font-mono text-tui-accent group-focus:text-tui-dark">
                        {contact.label}
                      </div>
                      <div className="text-sm text-tui-light group-focus:text-tui-dark">
                        {contact.value}
                      </div>
                    </div>
                    <div className="flex items-center space-x-2">
                      <span className="text-xs text-tui-muted group-focus:text-tui-darker">
                        {contact.type.toUpperCase()}
                      </span>
                      <span className="text-tui-accent group-focus:text-tui-dark">→</span>
                    </div>
                  </div>
                </a>
              ))}
            </div>
          </div>
        </div>

        {/* Professional Information */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="tui-panel">
            <div className="tui-panel-header">
              <h3 className="font-mono text-sm">{'//'} PROFESSIONAL STATUS</h3>
            </div>
            <div className="p-4">
              <div className="space-y-3">
                <div>
                  <div className="text-tui-accent text-xs">CURRENT ROLE</div>
                  <div className="text-tui-light text-sm">Senior Software Engineer</div>
                </div>
                <div>
                  <div className="text-tui-accent text-xs">EXPERIENCE SINCE</div>
                  <div className="text-tui-light text-sm">2013</div>
                </div>
                <div>
                  <div className="text-tui-accent text-xs">LOCATION</div>
                  <div className="text-tui-light text-sm">Austria</div>
                </div>
              </div>
            </div>
          </div>

          <div className="tui-panel">
            <div className="tui-panel-header">
              <h3 className="font-mono text-sm">{'//'} RESPONSE PROTOCOLS</h3>
            </div>
            <div className="p-4">
              <div className="text-tui-light text-sm leading-relaxed mb-4">
                For professional inquiries, please use email as the primary
                communication channel. LinkedIn is preferred for networking
                and career opportunities.
              </div>
              <div className="text-tui-muted text-xs">
                <p>• Email: Best for detailed technical discussions</p>
                <p>• LinkedIn: Professional networking and opportunities</p>
                <p>• GitHub: Code reviews and project collaboration</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Contact;
