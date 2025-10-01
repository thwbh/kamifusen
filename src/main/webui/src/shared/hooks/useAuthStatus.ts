import { useState, useEffect, useCallback } from 'react'
import { appAdminApi } from '../../config/apiClient'

interface UseAuthStatusState {
  isAuthenticated: boolean | null // null = checking, true = authenticated, false = not authenticated
  error: string | null
}

interface UseAuthStatusActions {
  checkAuthStatus: () => Promise<void>
  clearError: () => void
}

export const useAuthStatus = (): UseAuthStatusState & UseAuthStatusActions => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(null)
  const [error, setError] = useState<string | null>(null)

  const checkAuthStatus = useCallback(async () => {
    try {
      setError(null)
      const response = await appAdminApi.getAuthStatus()

      if (response.status === 200) {
        setIsAuthenticated(true)
      } else {
        setIsAuthenticated(false)
        setError('Authentication failed')
      }
    } catch (err: any) {
      if (err.response?.status === 401) {
        setIsAuthenticated(false)
        setError('Session expired. Please login again.')
      } else {
        setIsAuthenticated(false)
        setError('Authentication check failed')
      }
    }
  }, [])

  const clearError = useCallback(() => {
    setError(null)
  }, [])

  useEffect(() => {
    checkAuthStatus()
  }, [checkAuthStatus])

  return {
    isAuthenticated,
    error,
    checkAuthStatus,
    clearError
  }
}