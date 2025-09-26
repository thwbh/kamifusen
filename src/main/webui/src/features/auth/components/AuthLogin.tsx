import React, { useState, useEffect } from 'react';
import kamifusenIcon from '../../../assets/icons/logo_128.png';
import { LoadingSpinner, Panel, PanelHeader, PanelContent, Button, Form, FormConfig } from 'crt-dojo';
import Footer from '../../../shared/components/Footer';

interface WelcomeProps {
  onBegin: () => void;
  onNavigate: (page: string) => void;
}

const AuthLogin: React.FC<WelcomeProps> = () => {
  const [error, setError] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (data: any) => {
    setError('');
    setIsLoading(true);

    // Create a hidden form and submit it traditionally
    // This allows the browser to handle the redirect properly
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/j_security_check';
    form.style.display = 'none';

    const usernameInput = document.createElement('input');
    usernameInput.type = 'hidden';
    usernameInput.name = 'username';
    usernameInput.value = data.username;

    const passwordInput = document.createElement('input');
    passwordInput.type = 'hidden';
    passwordInput.name = 'password';
    passwordInput.value = data.password;

    form.appendChild(usernameInput);
    form.appendChild(passwordInput);
    document.body.appendChild(form);

    // Submit the form - this will trigger the normal form auth flow
    form.submit();
  };

  const loginFormConfig: FormConfig = {
    mode: 'create',
    submitLabel: 'ACCESS SYSTEM',
    submitLoadingLabel: 'AUTHENTICATING...',
    validationTrigger: 'onSubmit',
    onSubmit: handleSubmit,
    fields: [
      {
        key: 'username',
        type: 'text',
        label: 'USERNAME',
        placeholder: 'Enter username',
        required: true,
        validation: [
          { type: 'required', message: 'Username is required' }
        ],
        getValue: (data: any) => data?.username,
        setValue: (data: any, value: string) => { data.username = value }
      },
      {
        key: 'password',
        type: 'password',
        label: 'PASSWORD',
        placeholder: 'Enter password',
        required: true,
        validation: [
          { type: 'required', message: 'Password is required' }
        ],
        getValue: (data: any) => data?.password,
        setValue: (data: any, value: string) => { data.password = value }
      }
    ]
  };

  useEffect(() => {
    // Check for error parameter in URL
    const urlParams = new URLSearchParams(window.location.search);
    const urlError = urlParams.get('error');
    if (urlError === 'invalid-credentials') {
      setError('Invalid username or password');
      // Clean the URL
      window.history.replaceState({}, '', '/');
    }
  }, []);


  return (
    <div className="min-h-screen flex items-center justify-center px-8 animate-fade-in">
      <div className="text-center max-w-md w-full">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center justify-center space-x-3 mb-4">
            <div className="w-12 h-12 bg-tui-accent rounded-sm flex items-center justify-center">
              <img src={kamifusenIcon} alt="kamifusen logo" className="w-8 h-8" />
            </div>
            <h1 className="text-tui-accent font-bold text-2xl tracking-widest">KAMIFUSEN</h1>
          </div>
          <p className="text-tui-muted text-sm uppercase tracking-wide">Admin Terminal Access</p>
        </div>

        {/* Login Form */}
        <Panel>
          <PanelHeader>
            System Authentication
          </PanelHeader>
          <PanelContent>
            {error && (
              <div className="bg-tui-red bg-opacity-20 border border-tui-red p-3 rounded-sm mb-6">
                <p className="text-tui-light text-sm font-mono">{error}</p>
              </div>
            )}
            <Form config={loginFormConfig} />
          </PanelContent>
        </Panel>

        {/* Footer */}
        <Footer />
      </div>
    </div>
  );
};

export default AuthLogin;
