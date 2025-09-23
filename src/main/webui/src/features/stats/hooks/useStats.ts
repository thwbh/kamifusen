import { useState, useEffect, useCallback, useMemo } from 'react'
import { AppAdminResourceApi, AggregatedStatsDto} from '../../../api'

interface UseStatsState {
  statsData: AggregatedStatsDto | null
  timeRange: string
  loading: boolean
  error: string | null
}

interface UseStatsActions {
  setTimeRange: (range: string) => void
  refreshStats: () => Promise<void>
}

export const useStats = (): UseStatsState & UseStatsActions => {
  const [statsData, setStatsData] = useState<AggregatedStatsDto | null>(null)
  const [timeRange, setTimeRange] = useState('7d')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const adminApi = useMemo(() => new AppAdminResourceApi(), [])

  const refreshStats = useCallback(async () => {
    try {
      setLoading(true)
      setError(null)

      const response = await adminApi.getStats(timeRange)
      if (response.status === 200) {
        setStatsData(response.data)
      } else {
        throw new Error(`Failed to fetch stats: ${response.status}`)
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to load statistics'
      setError(errorMessage)
      console.error('Error loading statistics:', err)
    } finally {
      setLoading(false)
    }
  }, [adminApi, timeRange])

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
    refreshStats
  }
}