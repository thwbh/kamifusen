import React, { useState } from 'react'
import { AppAdminResourceApi, Configuration } from '../../../api'
import kamifusenIcon from '../../../assets/icons/kamifusen_3.svg'

interface PasswordChangeProps {
  onSuccess: () => void
}

const AdminPasswordChange: React.FC<PasswordChangeProps> = ({ onSuccess }) => {
  const [newUsername, setNewUsername] = useState('admin');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setError('');
    setIsLoading(true);

    if (!newPassword) {
      setError('New password is required');
      setIsLoading(false);
      return;
    }

    if (newPassword !== confirmPassword) {
      setError('Passwords do not match');
      setIsLoading(false);
      return;
    }

    if (newPassword.length < 8) {
      setError('Password must be at least 8 characters long');
      setIsLoading(false);
      return;
    }

    try {
      const config = new Configuration({
        basePath: '',
      });
      const api = new AppAdminResourceApi(config);

      await api.updateAdmin(newUsername, newPassword, confirmPassword);
      onSuccess();
    } catch (err: any) {
      if (err.response?.status === 400) {
        setError(err.response.data || 'Invalid credentials or validation failed');
      } else {
        setError('Failed to update password. Please try again.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-8 animate-fade-in">
      <div className="text-center max-w-md w-full">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center justify-center space-x-3 mb-4">
            <div className="w-12 h-12 bg-tui-accent rounded-sm flex items-center justify-center">
              <img src={kamifusenIcon} alt="Kamifusen" className="w-8 h-8" />
            </div>
            <h1 className="text-tui-accent font-bold text-2xl tracking-widest">KAMIFUSEN</h1>
          </div>
          <p className="text-tui-muted text-sm uppercase tracking-wide">Password change required</p>
        </div>

        {/* Password Change Form */}
        <div className="tui-panel">
          <div className="tui-panel-header">
            Change Default Password
          </div>
          <div className="p-6">
            <div className="mb-6">
              <p className="text-tui-muted text-sm">
                For security reasons, you must change the default admin password before continuing.
              </p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6">
              {error && (
                <div className="bg-tui-red bg-opacity-20 border border-tui-red p-3 rounded-sm">
                  <p className="text-tui-light text-sm font-mono">{error}</p>
                </div>
              )}

              <div>
                <label htmlFor="newUsername" className="block text-tui-light text-sm mb-2 uppercase tracking-wide">
                  New Username
                </label>
                <input
                  id="newUsername"
                  type="text"
                  value={newUsername}
                  onChange={(e) => setNewUsername(e.target.value)}
                  className="w-full px-3 py-2 bg-tui-dark border border-tui-border text-tui-light rounded-sm focus:outline-none focus:border-tui-accent transition-colors font-mono"
                  placeholder="Enter new username"
                  disabled={isLoading}
                  autoComplete="username"
                />
              </div>

              <div>
                <label htmlFor="newPassword" className="block text-tui-light text-sm mb-2 uppercase tracking-wide">
                  New Password
                </label>
                <input
                  id="newPassword"
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className="w-full px-3 py-2 bg-tui-dark border border-tui-border text-tui-light rounded-sm focus:outline-none focus:border-tui-accent transition-colors font-mono"
                  placeholder="Enter new password (min 8 characters)"
                  disabled={isLoading}
                  autoComplete="new-password"
                />
              </div>

              <div>
                <label htmlFor="confirmPassword" className="block text-tui-light text-sm mb-2 uppercase tracking-wide">
                  Confirm New Password
                </label>
                <input
                  id="confirmPassword"
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="w-full px-3 py-2 bg-tui-dark border border-tui-border text-tui-light rounded-sm focus:outline-none focus:border-tui-accent transition-colors font-mono"
                  placeholder="Confirm new password"
                  disabled={isLoading}
                  autoComplete="new-password"
                />
              </div>

              <button
                type="submit"
                disabled={isLoading}
                className="w-full tui-button tui-focus text-lg py-3 uppercase tracking-widest disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isLoading ? 'UPDATING...' : 'UPDATE PASSWORD'}
              </button>
            </form>
          </div>
        </div>

        {/* Footer */}
        <div className="text-center mt-8">
          <div className="text-tui-muted text-xs">
            <p>&copy; 2024 tohuwabohu.io</p>
            <p className="mt-1">Security Setup Required</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminPasswordChange;