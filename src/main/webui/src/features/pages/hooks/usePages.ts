import { useState, useEffect, useCallback, useMemo } from 'react'
import { AppAdminResourceApi, PageWithStatsDto} from '../../../api'
import { useAsyncOperation } from 'crt-dojo'

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
  clearError: () => void
}

export const usePages = (): UsePagesState & UsePagesActions => {
  const [pages, setPages] = useState<PageWithStatsDto[]>([])
  const [selectedDomain, setSelectedDomain] = useState<string>('')
  const [showHidden, setShowHidden] = useState(false)
  const [hiddenCount, setHiddenCount] = useState(0)
  const { loading, error, execute, clearError } = useAsyncOperation()

  const adminApi = useMemo(() => new AppAdminResourceApi(), [])

  const domains = useMemo(() => {
    const uniqueDomains = [...new Set(pages.map(page => page.domain).filter((domain): domain is string => Boolean(domain)))]
    return uniqueDomains.sort()
  }, [pages])

  const filteredPages = useMemo(() => {
    if (showHidden) return pages
    if (!selectedDomain) return pages
    return pages.filter(page => page.domain === selectedDomain)
  }, [pages, selectedDomain, showHidden])

  const refreshPages = useCallback(async () => {
    const result = await execute(async () => {
      let response
      if (showHidden) {
        response = await adminApi.listBlacklistedPages(selectedDomain || undefined)
      } else {
        response = await adminApi.listPages()
      }

      if (response.status === 200) {
        return response.data
      } else {
        throw new Error(`Failed to fetch pages: ${response.status}`)
      }
    })

    if (result) {
      setPages(result)
    }
  }, [adminApi, showHidden, selectedDomain, execute])

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
    await execute(async () => {
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
    });
  }, [adminApi, refreshPages, refreshHiddenCount, showHidden, execute])

  const restorePage = useCallback(async (pageId: string): Promise<void> => {
    await execute(async () => {
      const response = await adminApi.restorePage(pageId)

      if (response.status === 200) {
        await refreshPages()
        await refreshHiddenCount()
      } else {
        // const errorText = await response.text()
        throw new Error(/* errorText  || */ `Failed to restore page: ${response.status}`)
      }
    });
  }, [adminApi, refreshPages, refreshHiddenCount, execute])

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
    restorePage,
    clearError
  }
}