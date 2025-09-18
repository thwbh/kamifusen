import React from 'react';

const StatusBar: React.FC = () => {
  return (
    <div className="bg-tui-darker p-3 border-t border-tui-border flex items-center">
      <span className="text-tui-muted text-xs">LIVE HUD VIEWER</span>
    </div>
  );
};

export default StatusBar;