import React from 'react';

const MainContent: React.FC = () => {
  return (
    <div className="absolute inset-0 bg-tui-dark" style={{ bottom: '80px' }}>
      {/* Center loading indicator */}
      <div className="flex items-center justify-center h-full">
        <div className="w-16 h-16 border-2 border-tui-muted rounded-full animate-spin border-t-tui-accent"></div>
      </div>
    </div>
  );
};

export default MainContent;