import { useState, useEffect, useCallback } from 'react'
import { appAdminApi } from '../../../config/apiClient'
import { useAsyncOperation } from 'crt-dojo'

interface UseActiveSessionsState {
  activeCount: number
  timestamp: Date | null
  loading: boolean
  error: string | null
}

interface UseActiveSessionsActions {
  refreshSessions: () => Promise<void>
  clearError: () => void
}

export const useActiveSessions = (): UseActiveSessionsState & UseActiveSessionsActions => {
  const [activeCount, setActiveCount] = useState<number>(0)
  const [timestamp, setTimestamp] = useState<Date | null>(null)
  const { loading, error, execute, clearError } = useAsyncOperation()

  const refreshSessions = useCallback(async () => {
    const result = await execute(async () => {
      const response = await appAdminApi.getActiveSessions()
      if (response.status === 200) {
        return response.data
      } else {
        throw new Error(`Failed to fetch active sessions: ${response.status}`)
      }
    })

    if (result) {
      setActiveCount(result.activeCount)
      setTimestamp(new Date(result.timestamp))
    }
  }, [execute])

  // Auto-refresh every 30 seconds
  useEffect(() => {
    // Initial load
    refreshSessions()

    // Set up interval for updates
    const interval = setInterval(() => {
      refreshSessions()
    }, 30000) // 30 seconds

    return () => clearInterval(interval)
  }, [refreshSessions])

  return {
    activeCount,
    timestamp,
    loading,
    error,
    refreshSessions,
    clearError
  }
}