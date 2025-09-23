import { useState, useEffect, useCallback, useMemo } from 'react'
import { AppAdminResourceApi, PageWithStatsDto } from '../api/gen/index'

interface UsePagesState {
  pages: PageWithStatsDto[]
  loading: boolean
  error: string | null
  selectedDomain: string
  showHidden: boolean
  hiddenCount: number
  domains: string[]
  filteredPages: PageWithStatsDto[]
}

interface UsePagesActions {
  setSelectedDomain: (domain: string) => void
  setShowHidden: (showHidden: boolean) => void
  refreshPages: () => Promise<void>
  refreshHiddenCount: () => Promise<void>
  hidePage: (pageId: string) => Promise<void>
  restorePage: (pageId: string) => Promise<void>
}

export const usePages = (): UsePagesState & UsePagesActions => {
  const [pages, setPages] = useState<PageWithStatsDto[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedDomain, setSelectedDomain] = useState<string>('')
  const [showHidden, setShowHidden] = useState(false)
  const [hiddenCount, setHiddenCount] = useState(0)

  const adminApi = useMemo(() => new AppAdminResourceApi(), [])

  const domains = useMemo(() => {
    const uniqueDomains = [...new Set(pages.map(page => page.domain).filter(Boolean))]
    return uniqueDomains.sort()
  }, [pages])

  const filteredPages = useMemo(() => {
    if (showHidden) return pages
    if (!selectedDomain) return pages
    return pages.filter(page => page.domain === selectedDomain)
  }, [pages, selectedDomain, showHidden])

  const refreshPages = useCallback(async () => {
    try {
      setLoading(true)
      setError(null)

      let response
      if (showHidden) {
        response = await adminApi.listBlacklistedPages(selectedDomain || undefined)
      } else {
        response = await adminApi.listPages()
      }

      if (response.status === 200) {
        setPages(response.data)
      } else {
        throw new Error(`Failed to fetch pages: ${response.status}`)
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to load pages'
      setError(errorMessage)
      console.error('Error loading pages:', err)
    } finally {
      setLoading(false)
    }
  }, [adminApi, showHidden, selectedDomain])

  const refreshHiddenCount = useCallback(async () => {
    try {
      const response = await adminApi.listBlacklistedPages(selectedDomain || undefined)
      if (response.status === 200) {
        const hiddenPages = response.data;
        setHiddenCount(hiddenPages.length);
      } else {
        throw new Error(`Failed to fetch hidden pages count: ${response.status}`);
      }
    } catch (err) {
      console.error('Error loading hidden count:', err);
      setHiddenCount(0);
    }
  }, [adminApi, selectedDomain])

  const hidePage = useCallback(async (pageId: string): Promise<void> => {
    try {
      setError(null)

      const response = await adminApi.unregisterPage(pageId)

      if (response.status === 200) {
        await refreshPages()
        if (!showHidden) {
          await refreshHiddenCount()
        }
      } else {
        // const errorText = await response.text()
        throw new Error(/* errorText || */ `Failed to hide page: ${response.status}`)
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to hide page'
      setError(errorMessage)
      throw err
    }
  }, [adminApi, refreshPages, refreshHiddenCount, showHidden])

  const restorePage = useCallback(async (pageId: string): Promise<void> => {
    try {
      setError(null)

      const response = await adminApi.restorePage(pageId)

      if (response.status === 200) {
        await refreshPages()
        await refreshHiddenCount()
      } else {
        // const errorText = await response.text()
        throw new Error(/* errorText  || */ `Failed to restore page: ${response.status}`)
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to restore page'
      setError(errorMessage)
      throw err
    }
  }, [adminApi, refreshPages, refreshHiddenCount])

  const handleSetSelectedDomain = useCallback((domain: string) => {
    setSelectedDomain(domain)
  }, [])

  const handleSetShowHidden = useCallback((hidden: boolean) => {
    setShowHidden(hidden)
  }, [])

  // Set first domain as default when domains are loaded
  useEffect(() => {
    if (domains.length > 0 && !selectedDomain) {
      setSelectedDomain(domains[0])
    }
  }, [domains, selectedDomain])

  useEffect(() => {
    refreshPages()
  }, [refreshPages])

  useEffect(() => {
    refreshHiddenCount()
  }, [refreshHiddenCount])

  return {
    pages,
    loading,
    error,
    selectedDomain,
    showHidden,
    hiddenCount,
    domains,
    filteredPages,
    setSelectedDomain: handleSetSelectedDomain,
    setShowHidden: handleSetShowHidden,
    refreshPages,
    refreshHiddenCount,
    hidePage,
    restorePage
  }
}