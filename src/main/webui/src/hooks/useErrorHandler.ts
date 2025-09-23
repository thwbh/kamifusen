import { useCallback, useState } from 'react'

interface UseErrorHandlerReturn {
  error: string | null
  setError: (error: string | null) => void
  handleError: (error: unknown) => void
  clearError: () => void
  withErrorHandling: <T>(fn: () => Promise<T>) => Promise<T | null>
}

export const useErrorHandler = (): UseErrorHandlerReturn => {
  const [error, setError] = useState<string | null>(null)

  const handleError = useCallback((error: unknown) => {
    console.error('Error occurred:', error)

    if (error instanceof Error) {
      setError(error.message)
    } else if (typeof error === 'string') {
      setError(error)
    } else {
      setError('An unknown error occurred')
    }
  }, [])

  const clearError = useCallback(() => {
    setError(null)
  }, [])

  const withErrorHandling = useCallback(async <T>(fn: () => Promise<T>): Promise<T | null> => {
    try {
      clearError()
      return await fn()
    } catch (err) {
      handleError(err)
      return null
    }
  }, [handleError, clearError])

  return {
    error,
    setError,
    handleError,
    clearError,
    withErrorHandling
  }
}