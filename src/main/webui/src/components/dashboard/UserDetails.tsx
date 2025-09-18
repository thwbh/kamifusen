import React from 'react';

const UserDetails: React.FC = () => {
  return (
    <div className="tui-panel mx-4 mb-4 flex-grow">
      <div className="tui-panel-header">
        <h3 className="font-mono text-xs">USER DETAILS</h3>
      </div>
      <div className="p-4">
        <div className="text-tui-accent text-xs mb-2">{'//'} MATERIAL SELECTION</div>
        <div className="bg-tui-darker border border-tui-accent p-3">
          <div className="flex items-center">
            <span className="text-tui-light text-sm">Select Material Location</span>
            <span className="text-tui-accent ml-auto">→</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserDetails;