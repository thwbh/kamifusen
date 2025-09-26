import React, { useState } from 'react'
import { AppAdminResourceApi, Configuration } from '../../../api'
// import kamifusenIcon from '../../../assets/icons/kamifusen_3.svg'
import kamifusenIcon from '../../../assets/icons/logo_128.png'
import { LoadingSpinner, Panel, PanelHeader, PanelContent, Button, Form, FormConfig } from 'crt-dojo';
import Footer from '../../../shared/components/Footer';

interface PasswordChangeProps {
  onSuccess: () => void
}

const AuthPasswordChange: React.FC<PasswordChangeProps> = ({ onSuccess }) => {
  const [error, setError] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (data: any) => {
    setError('');
    setIsLoading(true);

    try {
      const config = new Configuration({
        basePath: '',
      });
      const api = new AppAdminResourceApi(config);

      await api.updateAdmin(data.newUsername, data.newPassword, data.confirmPassword);
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

  const passwordChangeFormConfig: FormConfig = {
    mode: 'create',
    submitLabel: 'UPDATE PASSWORD',
    submitLoadingLabel: 'UPDATING...',
    validationTrigger: 'onSubmit',
    fields: [
      {
        key: 'newUsername',
        type: 'text',
        label: 'NEW USERNAME',
        placeholder: 'Enter new username',
        required: true,
        validation: [
          { type: 'required', message: 'New username is required' }
        ],
        getValue: (data: any) => data?.newUsername ?? 'admin',
        setValue: (data: any, value: string) => { data.newUsername = value }
      },
      {
        key: 'newPassword',
        type: 'password',
        label: 'NEW PASSWORD',
        placeholder: 'Enter new password (min 8 characters)',
        required: true,
        validation: [
          { type: 'required', message: 'New password is required' },
          { type: 'minLength', value: 8, message: 'Password must be at least 8 characters long' }
        ],
        getValue: (data: any) => data?.newPassword,
        setValue: (data: any, value: string) => { data.newPassword = value }
      },
      {
        key: 'confirmPassword',
        type: 'password',
        label: 'CONFIRM NEW PASSWORD',
        placeholder: 'Confirm new password',
        required: true,
        validation: [
          { type: 'required', message: 'Password confirmation is required' },
          {
            type: 'custom',
            message: 'Passwords do not match',
            validator: (value: string, formData: any) => {
              console.log('Password validation:', { value, newPassword: formData.newPassword, match: value === formData.newPassword });
              return value === formData.newPassword
            }
          }
        ],
        getValue: (data: any) => data?.confirmPassword,
        setValue: (data: any, value: string) => { data.confirmPassword = value }
      }
    ],
    onSubmit: handleSubmit
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
        <Panel>
          <PanelHeader>
            Change Default Password
          </PanelHeader>
          <PanelContent>
            <div className="mb-6">
              <p className="text-tui-muted text-sm">
                For security reasons, you must change the default admin password before continuing.
              </p>
            </div>

            {error && (
              <div className="bg-tui-red bg-opacity-20 border border-tui-red p-3 rounded-sm mb-6">
                <p className="text-tui-light text-sm font-mono">{error}</p>
              </div>
            )}
            <Form
              config={passwordChangeFormConfig}
              initialData={{ newUsername: 'admin' }}
            />
            {isLoading && <div className="mt-4 text-center text-tui-accent">Submitting form...</div>}
          </PanelContent>
        </Panel>

        {/* Footer */}
        <Footer />
      </div>
    </div>
  );
};

export default AuthPasswordChange;
