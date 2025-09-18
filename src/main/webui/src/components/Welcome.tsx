import React, { useState, useEffect } from 'react';

interface WelcomeProps {
  onBegin: () => void;
  onNavigate: (page: string) => void;
}

const Welcome: React.FC<WelcomeProps> = ({ onBegin, onNavigate }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    // Auto-focus the username input when component mounts
    const timer = setTimeout(() => {
      const usernameInput = document.querySelector('[data-username-input]') as HTMLElement;
      if (usernameInput) {
        usernameInput.focus({ preventScroll: true });
      }
    }, 100);
    return () => clearTimeout(timer);
  }, []);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setError('');
    setIsLoading(true);

    // Basic validation
    if (!username.trim()) {
      setError('Username is required');
      setIsLoading(false);
      return;
    }
    if (!password) {
      setError('Password is required');
      setIsLoading(false);
      return;
    }

    try {
      const formData = new FormData();
      formData.append('username', username);
      formData.append('password', password);

      const response = await fetch('/j_security_check', {
        method: 'POST',
        body: formData,
        redirect: 'manual' // Handle redirects manually
      }).then((res) => res) as Response & { redirected: boolean };

      console.log(response);

      if (response.status === 200) {
        // Successful login - Quarkus form auth redirects on success
        onBegin();
      } else if (response.status === 401) {
        // Invalid credentials
        setError('Invalid username or password');
      } else {
        // Other error
        setError('Login failed. Please try again.');
      }
    } catch (err) {
      setError('Network error. Please check your connection.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyPress = (event: React.KeyboardEvent) => {
    if (event.key === 'Enter') {
      handleSubmit(event as any);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-8 animate-fade-in">
      <div className="text-center max-w-md w-full">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center justify-center space-x-3 mb-4">
            <div className="w-12 h-12 bg-tui-accent rounded-sm flex items-center justify-center">
              <span className="text-tui-dark font-bold text-xl">K</span>
            </div>
            <h1 className="text-tui-accent font-bold text-2xl tracking-widest">KAMIFUSEN</h1>
          </div>
          <p className="text-tui-muted text-sm uppercase tracking-wide">Admin Terminal Access</p>
        </div>

        {/* Login Form */}
        <div className="tui-panel">
          <div className="tui-panel-header">
            System Authentication
          </div>
          <div className="p-6">
            <form onSubmit={handleSubmit} className="space-y-6">
              {error && (
                <div className="bg-tui-red bg-opacity-20 border border-tui-red p-3 rounded-sm">
                  <p className="text-tui-red text-sm font-mono">{error}</p>
                </div>
              )}

              <div>
                <label htmlFor="username" className="block text-tui-light text-sm mb-2 uppercase tracking-wide">
                  Username
                </label>
                <input
                  id="username"
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  onKeyDown={handleKeyPress}
                  data-username-input
                  className="w-full px-3 py-2 bg-tui-dark border border-tui-border text-tui-light rounded-sm focus:outline-none focus:border-tui-accent transition-colors font-mono"
                  placeholder="Enter username"
                  disabled={isLoading}
                  autoComplete="username"
                />
              </div>

              <div>
                <label htmlFor="password" className="block text-tui-light text-sm mb-2 uppercase tracking-wide">
                  Password
                </label>
                <input
                  id="password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  onKeyDown={handleKeyPress}
                  className="w-full px-3 py-2 bg-tui-dark border border-tui-border text-tui-light rounded-sm focus:outline-none focus:border-tui-accent transition-colors font-mono"
                  placeholder="Enter password"
                  disabled={isLoading}
                  autoComplete="current-password"
                />
              </div>

              <button
                type="submit"
                disabled={isLoading}
                className="w-full tui-button tui-focus text-lg py-3 uppercase tracking-widest disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isLoading ? 'AUTHENTICATING...' : 'ACCESS SYSTEM'}
              </button>
            </form>
          </div>
        </div>

        {/* Footer */}
        <div className="text-center mt-8">
          <div className="text-tui-muted text-xs">
            <p>&copy; 2024 tohuwabohu.io</p>
            <p className="mt-1">Terminal Interface v{process.env.NODE_ENV === 'development' ? 'DEV' : '1.0'}</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Welcome;