import React from 'react';

interface ActionItem {
  label: string;
  description?: string;
  onClick?: () => void;
}

interface ActionPanelProps {
  title: string;
  subtitle?: string;
  actions: ActionItem[];
}

const ActionPanel: React.FC<ActionPanelProps> = ({ title, subtitle, actions }) => {
  return (
    <div className="tui-panel mx-4 mb-4 flex-grow">
      <div className="tui-panel-header">
        <h3 className="font-mono text-xs">{title}</h3>
      </div>
      <div className="p-4">
        {subtitle && (
          <div className="text-tui-accent text-xs mb-2">{'//'} {subtitle}</div>
        )}
        <div className="space-y-2">
          {actions.map((action, index) => (
            <div 
              key={index}
              className="bg-tui-darker border border-tui-accent p-3 cursor-pointer hover:bg-tui-border hover:bg-opacity-20 transition-colors duration-150"
              onClick={action.onClick}
            >
              <div className="flex items-center">
                <span className="text-tui-light text-sm">{action.label}</span>
                <span className="text-tui-accent ml-auto">→</span>
              </div>
              {action.description && (
                <div className="text-tui-muted text-xs mt-1">{action.description}</div>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ActionPanel;