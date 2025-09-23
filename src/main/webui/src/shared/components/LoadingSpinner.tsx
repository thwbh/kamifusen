import React from 'react'

interface LoadingSpinnerProps {
  size?: 'sm' | 'md' | 'lg'
  className?: string
}

const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({ size = 'md', className = '' }) => {
  const sizeClasses = {
    sm: 'w-4 h-4',
    md: 'w-6 h-6',
    lg: 'w-8 h-8'
  }

  return (
    <div className={`animate-spin ${sizeClasses[size]} ${className}`}>
      <div className="border-2 border-tui-border border-t-tui-accent rounded-full w-full h-full"></div>
    </div>
  )
}

export default LoadingSpinner