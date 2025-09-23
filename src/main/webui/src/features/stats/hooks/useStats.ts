import { useState, useEffect, useCallback, useMemo } from 'react'
import { AppAdminResourceApi, AggregatedStatsDto} from '../../../api'
import { useAsyncOperation } from '../../../shared'

interface UseStatsState {
  statsData: AggregatedStatsDto | null
  timeRange: string
  loading: boolean
  error: string | null
}

interface UseStatsActions {
  setTimeRange: (range: string) => void
  refreshStats: () => Promise<void>
  clearError: () => void
}

export const useStats = (): UseStatsState & UseStatsActions => {
  const [statsData, setStatsData] = useState<AggregatedStatsDto | null>(null)
  const [timeRange, setTimeRange] = useState('7d')
  const { loading, error, execute, clearError } = useAsyncOperation()

  const adminApi = useMemo(() => new AppAdminResourceApi(), [])

  const refreshStats = useCallback(async () => {
    const result = await execute(async () => {
      const response = await adminApi.getStats(timeRange)
      if (response.status === 200) {
        return response.data
      } else {
        throw new Error(`Failed to fetch stats: ${response.status}`)
      }
    })

    if (result) {
      setStatsData(result)
    }
  }, [adminApi, timeRange, execute])

  const handleSetTimeRange = useCallback((range: string) => {
    setTimeRange(range)
  }, [])

  useEffect(() => {
    refreshStats()
  }, [refreshStats])

  return {
    statsData,
    timeRange,
    loading,
    error,
    setTimeRange: handleSetTimeRange,
    refreshStats,
    clearError
  }
}