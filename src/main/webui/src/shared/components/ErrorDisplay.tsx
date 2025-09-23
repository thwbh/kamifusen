import React, { forwardRef } from 'react'

interface ErrorDisplayProps {
  error: string | null
  onClearError: () => void
}

const ErrorDisplay = forwardRef<HTMLDivElement, ErrorDisplayProps>(
  ({ error, onClearError }, ref) => {
    if (!error) return null

    return (
      <div ref={ref} className="tui-panel mb-6 border-tui-red">
        <div className="tui-panel-header bg-tui-red text-tui-dark">
          Error
        </div>
        <div className="p-4">
          <p className="text-tui-red">{error}</p>
          <button
            onClick={onClearError}
            className="mt-2 text-tui-muted hover:text-tui-accent text-sm"
          >
            DISMISS
          </button>
        </div>
      </div>
    )
  }
)

ErrorDisplay.displayName = 'ErrorDisplay'

export default ErrorDisplay