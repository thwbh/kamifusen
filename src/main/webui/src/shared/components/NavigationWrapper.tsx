import React from 'react';
import { Dashboard } from '../../features/dashboard';
import { Pages } from '../../features/pages';
import { Stats } from '../../features/stats';
import { Users } from '../../features/users';
import Header from "./Header";
import { Navigation, NavigationConfig } from 'crt-dojo';

interface NavigationWrapperProps {
  onSignOut: () => void;
}

const NavigationWrapper: React.FC<NavigationWrapperProps> = ({ onSignOut }) => {
  const renderContent = (page: string) => {
    switch (page) {
      case 'dashboard':
        return <Dashboard onSignOut={onSignOut} />;
      case 'pages':
        return <Pages />;
      case 'stats':
        return <Stats />;
      case 'users':
        return <Users />;
      default:
        return <Dashboard onSignOut={onSignOut} />;
    }
  };

  const navigationConfig: NavigationConfig = {
    items: [
      { key: '1', label: 'Dashboard', page: 'dashboard' },
      { key: '2', label: 'Pages', page: 'pages' },
      { key: '3', label: 'Stats', page: 'stats' },
      { key: '4', label: 'Users', page: 'users' }
    ],
    initialPage: 'stats',
    headerComponent: Header,
    onSignOut: onSignOut,
    renderContent: renderContent,
    useModifierKey: true,           // Use Option/Alt + number
    showHotkeys: true,              // Show [⌥1] in navigation
    disableShortcutsOnInput: true   // Disable shortcuts when typing in forms
  };

  return <Navigation config={navigationConfig} />;
};

export default NavigationWrapper;
