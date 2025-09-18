import React, { useState } from 'react';
import Background from './components/Background';
import Welcome from './components/Welcome';
import NavigationWrapper from './components/NavigationWrapper';

type AppState = 'welcome' | 'authenticated';

function App() {
  const [currentState, setCurrentState] = useState<AppState>('welcome');

  const handleBegin = () => {
    setCurrentState('authenticated');
  };

  const handleSignOut = () => {
    setCurrentState('welcome');
  };

  const handleNavigate = (page: string) => {
    setCurrentState(page as AppState);
  };

  const renderContent = () => {
    switch (currentState) {
      case 'welcome':
        return <Welcome onBegin={handleBegin} onNavigate={handleNavigate} />;
      case 'authenticated':
        return <NavigationWrapper onSignOut={handleSignOut} />;
      default:
        return <Welcome onBegin={handleBegin} onNavigate={handleNavigate} />;
    }
  };

  const shouldShowRedLines = currentState === 'welcome';

  return (
    <div className="App h-screen flex flex-col">
      <Background showRedLines={shouldShowRedLines}>
        {renderContent()}
      </Background>
    </div>
  );
}

export default App;