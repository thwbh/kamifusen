import React, { useState } from 'react';
import Dashboard from '../Dashboard';
import Pages from '../Pages';
import Stats from '../Stats';
import Users from '../Users';
import kamifusenIcon from '../../assets/icons/kamifusen_3.svg';

interface NavigationWrapperProps {
  onSignOut: () => void;
}

type Page = 'dashboard' | 'pages' | 'stats' | 'users';

const NavigationWrapper: React.FC<NavigationWrapperProps> = ({ onSignOut }) => {
  const [currentPage, setCurrentPage] = useState<Page>('dashboard');

  const handleKeyPress = (event: React.KeyboardEvent) => {
    switch (event.key) {
      case '1':
        setCurrentPage('dashboard');
        break;
      case '2':
        setCurrentPage('pages');
        break;
      case '3':
        setCurrentPage('stats');
        break;
      case '4':
        setCurrentPage('users');
        break;
      case 'Escape':
        onSignOut();
        break;
      default:
        break;
    }
  };

  const renderContent = () => {
    switch (currentPage) {
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

  const menuItems = [
    { key: '1', label: 'Dashboard', page: 'dashboard' as Page },
    { key: '2', label: 'Pages', page: 'pages' as Page },
    { key: '3', label: 'Stats', page: 'stats' as Page },
    { key: '4', label: 'Users', page: 'users' as Page }
  ];

  return (
    <div className="h-full flex flex-col" onKeyDown={handleKeyPress} tabIndex={0}>
      {/* Navigation Bar */}
      <div className="bg-tui-dark border-b border-tui-border p-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-8 h-8 bg-tui-accent rounded-sm flex items-center justify-center">
              <img src={kamifusenIcon} alt="Ashiato Icon" className="w-6 h-6" />
            </div>
            <h1 className="text-tui-accent font-bold text-xl tracking-wider">KAMIFUSEN ADMIN</h1>
          </div>

          <div className="flex items-center space-x-6">
            {menuItems.map(item => (
              <button
                key={item.key}
                onClick={() => setCurrentPage(item.page)}
                className={`px-3 py-1 text-sm uppercase tracking-wide transition-colors ${currentPage === item.page
                    ? 'text-tui-accent border-b border-tui-accent'
                    : 'text-tui-muted hover:text-tui-light'
                  }`}
              >
                [{item.key}] {item.label}
              </button>
            ))}

            <button
              onClick={onSignOut}
              className="px-3 py-1 text-sm uppercase tracking-wide text-tui-red hover:text-tui-red-light transition-colors"
            >
              [ESC] Sign Out
            </button>
          </div>
        </div>
      </div>

      {/* Content Area */}
      <div className="flex-1 overflow-hidden">
        {renderContent()}
      </div>
    </div>
  );
};

export default NavigationWrapper;
