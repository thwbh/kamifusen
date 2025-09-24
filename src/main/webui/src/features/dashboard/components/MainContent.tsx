import React from 'react';
import { LoadingSpinner } from '../../../shared';

const MainContent: React.FC = () => {
  return (
    <div className="absolute inset-0 bg-tui-dark" style={{ bottom: '80px' }}>
      {/* Center loading indicator */}
      <div className="flex items-center justify-center h-full">
        <LoadingSpinner size="lg" />
      </div>
    </div>
  );
};

export default MainContent;