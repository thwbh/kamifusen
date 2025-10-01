import React, { useState, useEffect } from 'react';
import DataTable from './DataTable';
import MetricsPanel from './MetricsPanel';
import ActionPanel from './ActionPanel';
import StatusBar from './StatusBar';
import BottomSlots from './BottomSlots';
import MainContent from './MainContent';
import ActiveSessions from './ActiveSessions';
import SystemHealth from './SystemHealth';
import { useSystemHealth } from '../hooks/useSystemHealth';

interface DashboardProps {
  onSignOut: () => void;
}

const Dashboard: React.FC<DashboardProps> = ({ onSignOut }) => {
  const { healthData, loading, error, responseTime } = useSystemHealth(5000); // Refresh every 5 seconds
  const [loginTime] = useState(new Date()); // Store login timestamp

  useEffect(() => {
    // Set redirect timestamp when dashboard first mounts
    const formSubmitTime = sessionStorage.getItem('form-submit-ts');
    if (formSubmitTime && !sessionStorage.getItem('redirect-ts')) {
      sessionStorage.setItem('redirect-ts', Date.now().toString());
    }
  }, []);

  return (
    <div className="relative h-full">
      {/* Main content area */}
      <div className="flex flex-col h-full">
        <div className="flex flex-row">
          <div className="w-80 bg-tui-dark border-r border-tui-border flex flex-col">
            <div className="flex-1">
              <SystemHealth />
              <ActiveSessions />
            </div>
          </div>

          <div className="flex-1 bg-tui-dark relative">
            <MainContent />
            <ActionPanel
              title="MISSION CONTROL"
              subtitle="QUICK ACTIONS"
              actions={[
                { label: "View Live Activity Feed" },
                { label: "Export Analytics Data" },
                { label: "System Diagnostics" }
              ]}
            />
          </div>
        </div>
        <div className="flex bottom-0">
          <StatusBar />
          <BottomSlots healthData={healthData} loginTime={loginTime} responseTime={responseTime} />
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
