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
  const { healthData, loading, error } = useSystemHealth(5000); // Refresh every 5 seconds
  const [loginTime] = useState(new Date()); // Store login timestamp

  return (
    <div className="flex h-full">
      <div className="w-80 bg-tui-dark border-r border-tui-border flex flex-col">
        <SystemHealth healthData={healthData} loading={loading} error={error} />
        <ActiveSessions />
        <ActionPanel
          title="MISSION CONTROL"
          subtitle="QUICK ACTIONS"
          actions={[
            { label: "View Live Activity Feed" },
            { label: "Export Analytics Data" },
            { label: "System Diagnostics" }
          ]}
        />
        <StatusBar />
      </div>

      <div className="flex-1 bg-tui-dark relative">
        <MainContent />
        <BottomSlots healthData={healthData} loginTime={loginTime} />
      </div>
    </div>
  );
};

export default Dashboard;
