import React from 'react'
import LoadingSpinner from './LoadingSpinner'

interface LoadingStateProps {
  message?: string
  fullHeight?: boolean
  className?: string
}

const LoadingState: React.FC<LoadingStateProps> = ({
  message = 'Loading...',
  fullHeight = false,
  className = ''
}) => {
  return (
    <div className={`flex flex-col items-center justify-center p-8 ${fullHeight ? 'min-h-64' : ''} ${className}`}>
      <LoadingSpinner size="lg" className="mb-4" />
      <p className="text-tui-muted text-sm font-mono">{message}</p>
    </div>
  )
}

export default LoadingState