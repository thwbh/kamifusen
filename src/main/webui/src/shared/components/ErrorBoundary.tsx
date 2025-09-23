import React, { Component, ReactNode } from 'react'

interface ErrorBoundaryState {
  hasError: boolean
  error: Error | null
  errorInfo: React.ErrorInfo | null
}

interface ErrorBoundaryProps {
  children: ReactNode
  fallback?: (error: Error, errorInfo: React.ErrorInfo) => ReactNode
  onError?: (error: Error, errorInfo: React.ErrorInfo) => void
}

class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props)
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null
    }
  }

  static getDerivedStateFromError(error: Error): Partial<ErrorBoundaryState> {
    return {
      hasError: true,
      error
    }
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    this.setState({
      error,
      errorInfo
    })

    if (this.props.onError) {
      this.props.onError(error, errorInfo)
    }

    console.error('ErrorBoundary caught an error:', error, errorInfo)
  }

  render() {
    if (this.state.hasError && this.state.error && this.state.errorInfo) {
      if (this.props.fallback) {
        return this.props.fallback(this.state.error, this.state.errorInfo)
      }

      return (
        <div className="tui-panel border-tui-red">
          <div className="tui-panel-header bg-tui-red text-tui-dark">
            Application Error
          </div>
          <div className="p-4">
            <h3 className="text-tui-red text-sm font-mono mb-2">Something went wrong</h3>
            <details className="text-xs text-tui-muted font-mono">
              <summary className="cursor-pointer mb-2 text-tui-light">
                Error Details (click to expand)
              </summary>
              <div className="bg-tui-darker p-2 rounded border">
                <p className="text-tui-red mb-2">{this.state.error.toString()}</p>
                <pre className="whitespace-pre-wrap text-xs">
                  {this.state.errorInfo.componentStack}
                </pre>
              </div>
            </details>
            <button
              onClick={() => this.setState({ hasError: false, error: null, errorInfo: null })}
              className="mt-3 px-3 py-1 bg-tui-accent text-tui-dark text-sm font-mono hover:bg-tui-accent-hover"
            >
              TRY AGAIN
            </button>
          </div>
        </div>
      )
    }

    return this.props.children
  }
}

export default ErrorBoundary