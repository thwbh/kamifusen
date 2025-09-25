import React, {useEffect, useState} from 'react'
import { Background, ErrorBoundary } from 'crt-dojo'
import { NavigationWrapper } from './shared'
import { AuthLogin, AdminPasswordChange } from './features/auth'

type AppState = 'welcome' | 'authenticated' | 'change-password'

function App() {
  const [currentState, setCurrentState] = useState<AppState>(() => {
    // Check URL path and parameters on initial load
    const pathname = window.location.pathname
    const urlParams = new URLSearchParams(window.location.search)
    const page = urlParams.get('page')

    // Handle backend redirects to /admin/landing
    if (pathname === '/admin/landing') {
      // This means we were redirected from successful login
      // We need to call the landing endpoint to determine next action
      return 'welcome' // Temporarily show welcome while we redirect
    }

    // Handle URL parameters
    if (page === 'change-password') return 'change-password'
    if (page === 'dashboard') return 'authenticated'

    // Handle error parameter from failed form auth
    const error = urlParams.get('error')
    if (error === 'invalid-credentials') {
      return 'welcome' // Stay on welcome page but with error
    }

    return 'welcome'
  })

  const handleBegin = () => {
    setCurrentState('authenticated')
  }

  const handleSignOut = () => {
    setCurrentState('welcome')
  }

  const handleNavigate = (page: string) => {
    setCurrentState(page as AppState)
  }

  useEffect(() => {
    // Handle /admin/landing redirect - just wait for backend to redirect us
    if (window.location.pathname === '/admin/landing') {
      // The backend landing endpoint will handle the redirect based on password change requirement
      // We don't need to make additional API calls here
      console.log('Waiting for backend to redirect from landing page...');
    }
  }, [])

  const renderContent = () => {
    switch (currentState) {
      case 'welcome':
        return <AuthLogin onBegin={handleBegin} onNavigate={handleNavigate} />;
      case 'change-password':
        return <AdminPasswordChange onSuccess={handleBegin} />;
      case 'authenticated':
        return <NavigationWrapper onSignOut={handleSignOut} />;
      default:
        return <AuthLogin onBegin={handleBegin} onNavigate={handleNavigate} />;
    }
  }

  const shouldShowRedLines = currentState === 'welcome';

    // <Background showRedLines={shouldShowRedLines}>
    //     {renderContent()}
    // </Background>

  return (
    <ErrorBoundary>
        {renderContent()}
    </ErrorBoundary>
  )
}

export default App;