import { useCallback } from 'react'
import { useLoadingState } from './useLoadingState'
import { useErrorHandler } from './useErrorHandler'

interface UseAsyncOperationReturn {
  loading: boolean
  error: string | null
  execute: <T>(fn: () => Promise<T>) => Promise<T | null>
  clearError: () => void
}

export const useAsyncOperation = (): UseAsyncOperationReturn => {
  const { loading, withLoading } = useLoadingState()
  const { error, handleError, clearError } = useErrorHandler()

  const execute = useCallback(async <T>(fn: () => Promise<T>): Promise<T | null> => {
    try {
      return await withLoading(fn)
    } catch (err) {
      handleError(err)
      return null
    }
  }, [withLoading, handleError])

  return {
    loading,
    error,
    execute,
    clearError
  }
}