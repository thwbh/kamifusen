import React, { useState, useEffect } from 'react';
import DataTable from './DataTable';
import MetricsPanel from './MetricsPanel';
import ActionPanel from './ActionPanel';
import StatusBar from './StatusBar';
import TimeSlots from './TimeSlots';
import MainContent from './MainContent';

interface DataItem {
  name: string;
  value: string;
  color: string;
}

interface DashboardProps {
  onSignOut: () => void;
}

const Dashboard: React.FC<DashboardProps> = ({ onSignOut }) => {
  const [selectedMaterial, setSelectedMaterial] = useState(0);
  const [currentCapacity, setCurrentCapacity] = useState(3612);

  const materials: DataItem[] = [
    { name: 'Corundum (Raw)', value: '+2%', color: 'text-tui-green' },
    { name: 'Gold (Ore)', value: '+1%', color: 'text-tui-green' },
    { name: 'Laranite (Raw)', value: '+2%', color: 'text-tui-green' },
    { name: 'Quartz (Raw)', value: '-3%', color: 'text-tui-red' },
    { name: 'Copper (Ore)', value: '+4%', color: 'text-tui-green' },
    { name: 'Aluminum (Ore)', value: '+7%', color: 'text-tui-green' },
  ];

  const timeSlots = [
    '09:01', '13:46', '04:37', '03:00', '04:00', '10:1A', 'AB:HS',
    '07:00', '12:22', '18:22', '02:09', '06:10', '15:CE', 'AQ:CE'
  ];

  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentCapacity(prev => prev + Math.floor(Math.random() * 5) - 2);
    }, 2000);

    return () => clearInterval(interval);
  }, []);

  const handleKeyPress = (event: React.KeyboardEvent) => {
    switch (event.key) {
      case 'ArrowUp':
        setSelectedMaterial(prev => Math.max(0, prev - 1));
        break;
      case 'ArrowDown':
        setSelectedMaterial(prev => Math.min(materials.length - 1, prev + 1));
        break;
      case 'Escape':
        onSignOut();
        break;
      default:
        break;
    }
  };

  return (
    <div className="flex h-full" onKeyDown={handleKeyPress} tabIndex={0}>
      <div className="w-80 bg-tui-dark border-r border-tui-border flex flex-col">
        <DataTable 
          title="STATION PROFILE"
          subtitle="MATERIAL SPECIALIZATIONS"
          data={materials}
          selectedIndex={selectedMaterial}
          onItemSelect={setSelectedMaterial}
        />
        <MetricsPanel 
          title="REFINERY CAPACITY"
          subtitle="REFINERY CURRENTLY HAS AN EXTREME WORKLOAD. A LARGE SURCHARGE WILL BE ADDED."
          primaryMetric={{
            label: "CURRENT CAPACITY",
            value: currentCapacity.toString(),
            unit: "%"
          }}
        />
        <ActionPanel 
          title="USER DETAILS"
          subtitle="MATERIAL SELECTION"
          actions={[
            { label: "Select Material Location" }
          ]}
        />
        <StatusBar />
      </div>

      <div className="flex-1 bg-tui-dark relative">
        <MainContent />
        <TimeSlots timeSlots={timeSlots} />
      </div>
    </div>
  );
};

export default Dashboard;