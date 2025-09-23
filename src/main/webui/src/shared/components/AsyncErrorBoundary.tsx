import React, { ReactNode } from 'react'
import ErrorBoundary from './ErrorBoundary'

interface AsyncErrorBoundaryProps {
  children: ReactNode
  error: string | null
  loading: boolean
  onRetry?: () => void
  loadingMessage?: string
  fallback?: (error: string) => ReactNode
}

const AsyncErrorBoundary: React.FC<AsyncErrorBoundaryProps> = ({
  children,
  error,
  loading,
  onRetry,
  loadingMessage = 'Loading...',
  fallback
}) => {
  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center p-8">
        <div className="animate-spin w-6 h-6 mb-4">
          <div className="border-2 border-tui-border border-t-tui-accent rounded-full w-full h-full"></div>
        </div>
        <p className="text-tui-muted text-sm font-mono">{loadingMessage}</p>
      </div>
    )
  }

  if (error) {
    if (fallback) {
      return <>{fallback(error)}</>
    }

    return (
      <div className="tui-panel border-tui-red">
        <div className="tui-panel-header bg-tui-red text-tui-dark">
          Error
        </div>
        <div className="p-4">
          <p className="text-tui-red text-sm mb-3">{error}</p>
          {onRetry && (
            <button
              onClick={onRetry}
              className="px-3 py-1 bg-tui-accent text-tui-dark text-sm font-mono hover:bg-tui-accent-hover"
            >
              RETRY
            </button>
          )}
        </div>
      </div>
    )
  }

  return (
    <ErrorBoundary>
      {children}
    </ErrorBoundary>
  )
}

export default AsyncErrorBoundary