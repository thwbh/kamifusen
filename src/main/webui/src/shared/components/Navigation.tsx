import React from 'react';

interface NavigationProps {
  currentUser: string;
  funds: string;
  onSignOut: () => void;
}

const Navigation: React.FC<NavigationProps> = ({ currentUser, funds, onSignOut }) => {

  const handleKeyPress = (event: React.KeyboardEvent) => {
    if (event.key === 'Enter' || event.key === ' ') {
      onSignOut();
    }
  };

  return (
    <div className="w-full bg-tui-darker border-b border-tui-border">
      <div className="flex items-center justify-between px-6 py-3">
        {/* Left side - System info */}
        <div className="flex items-center space-x-8">
          <div className="text-tui-accent text-sm font-mono">
            AI REFINERY SYSTEM CAT.09
          </div>
          <div className="text-tui-light text-sm">
            {'//'} MODULE-FPB7
          </div>
        </div>

        {/* Center - Main title */}
        <div className="text-tui-light text-lg font-mono tracking-wider">
          MIC-L1 SHALLOW FRONTIER STATION
          <span className="text-tui-muted ml-4">REFINEMENT CENTER</span>
        </div>

        {/* Right side - User info and controls */}
        <div className="flex items-center space-x-6">
          <div className="text-tui-accent text-sm">
            {'//'} USER
          </div>
          <div className="text-tui-light text-sm font-mono uppercase">
            {currentUser}
          </div>
          <div className="text-tui-accent text-sm">
            {'//'} FUNDS
          </div>
          <div className="text-tui-green text-sm font-mono">
            {funds} aUEC
          </div>

          {/* Sign out button */}
          <button
            className="tui-close-button"
            onClick={onSignOut}
            onKeyDown={handleKeyPress}
            tabIndex={0}
          >
            ×
          </button>
        </div>
      </div>
    </div>
  );
};

export default Navigation;
