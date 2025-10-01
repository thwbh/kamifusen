import React, { useEffect } from 'react';
import { Pages } from '../../features/pages';
import { Stats } from '../../features/stats';
import { Users } from '../../features/users';
import Header from "./Header";
import { Navigation, NavigationConfig, LoadingSpinner } from 'crt-dojo';
import { useAuthStatus } from '../hooks';
import { Dashboard } from '../../features/dashboard';

interface NavigationWrapperProps {
  onSignOut: (reason?: string) => void;
}

const NavigationWrapper: React.FC<NavigationWrapperProps> = ({ onSignOut }) => {
  const { isAuthenticated, error, clearError } = useAuthStatus();

  useEffect(() => {
    if (isAuthenticated === false) {
      // Authentication failed, trigger sign out with appropriate reason
      if (error) {
        console.error('Authentication error:', error);
        onSignOut('session-expired');
      } else {
        onSignOut();
      }
    }
  }, [isAuthenticated, error, onSignOut]);

  // Show loading while checking authentication
  if (isAuthenticated === null) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <LoadingSpinner />
      </div>
    );
  }

  // If not authenticated, don't render the navigation (onSignOut will be called)
  if (isAuthenticated === false) {
    return null;
  }

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
      { key: '2', label: 'Stats', page: 'stats' },
      { key: '3', label: 'Pages', page: 'pages' },
      { key: '4', label: 'Users', page: 'users' }
    ],
    initialPage: 'dashboard',
    headerComponent: Header,
    onSignOut: onSignOut,
    renderContent: renderContent,
    showHotkeys: true,              // Show [⌥1] in navigation
    disableShortcutsOnInput: true   // Disable shortcuts when typing in forms
  };

  return <Navigation className="h-full" config={navigationConfig} />;
};

export default NavigationWrapper;
