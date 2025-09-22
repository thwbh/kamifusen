import React, { useState, useEffect, useMemo } from 'react'
import {
  useReactTable,
  getCoreRowModel,
  getSortedRowModel,
  flexRender,
  createColumnHelper,
  SortingState
} from '@tanstack/react-table'
import { AppAdminResourceApi, PageWithStatsDto } from '../api/gen/index'

const Pages: React.FC = () => {
  const [pages, setPages] = useState<PageWithStatsDto[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedDomain, setSelectedDomain] = useState<string>('')
  const [sorting, setSorting] = useState<SortingState>([])
  const [showHidden, setShowHidden] = useState(false)
  const [hiddenCount, setHiddenCount] = useState(0)

  const adminApi = new AppAdminResourceApi()

  const columnHelper = createColumnHelper<PageWithStatsDto>()

  const columns = useMemo(() => [
    columnHelper.accessor('path', {
      header: 'Path',
      cell: info => <span className="font-mono text-tui-light">{info.getValue()}</span>
    }),
    columnHelper.accessor('domain', {
      header: 'Domain',
      cell: info => <span className="text-tui-muted">{info.getValue()}</span>
    }),
    columnHelper.accessor('visitCount', {
      header: 'Visits',
      cell: info => <span className="text-tui-accent">{info.getValue() || 0}</span>
    }),
    columnHelper.accessor('lastHit', {
      header: 'Last Visit',
      cell: info => (
        <span className="text-tui-muted">
          {info.getValue() ? new Date(info.getValue()!).toLocaleString() : 'Never'}
        </span>
      )
    }),
    columnHelper.display({
      id: 'actions',
      header: 'Actions',
      cell: info => (
        <button
          className="text-tui-red hover:text-tui-accent text-sm"
          onClick={() => showHidden ? handleRestorePage(info.row.original.id) : handleHidePage(info.row.original.id)}
        >
          {showHidden ? 'RESTORE' : 'HIDE'}
        </button>
      )
    })
  ], [columnHelper, showHidden])

  // Get unique domains and filter pages by selected domain
  const domains = useMemo(() => {
    const uniqueDomains = [...new Set(pages.map(page => page.domain).filter(Boolean))]
    return uniqueDomains.sort()
  }, [pages])

  const filteredPages = useMemo(() => {
    // When showing hidden pages, the API already filters by domain
    if (showHidden) return pages

    if (!selectedDomain) return pages
    return pages.filter(page => page.domain === selectedDomain)
  }, [pages, selectedDomain, showHidden])

  const table = useReactTable({
    data: filteredPages,
    columns,
    state: {
      sorting,
    },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
  })

  // Set first domain as default when domains are loaded
  useEffect(() => {
    if (domains.length > 0 && !selectedDomain) {
      setSelectedDomain(domains[0])
    }
  }, [domains, selectedDomain])

  useEffect(() => {
    loadPages()
  }, [showHidden, selectedDomain])

  useEffect(() => {
    loadHiddenCount()
  }, [selectedDomain])

  const loadPages = async () => {
    try {
      setLoading(true)
      setError(null)
      if (showHidden) {
        const response = await adminApi.listBlacklistedPages(selectedDomain || undefined)
        setPages(response.data)
      } else {
        const response = await adminApi.listPages()
        setPages(response.data)
      }
    } catch (err) {
      setError('Failed to load pages')
      console.error('Error loading pages:', err)
    } finally {
      setLoading(false)
    }
  }

  const loadHiddenCount = async () => {
    try {
      const response = await adminApi.listBlacklistedPages(selectedDomain || undefined)
      setHiddenCount(response.data.length)
    } catch (err) {
      console.error('Error loading hidden count:', err)
      setHiddenCount(0)
    }
  }

  const handleHidePage = async (pageId: string) => {
    if (!confirm('Are you sure you want to hide this page? This will unregister it from tracking.')) {
      return
    }

    try {
      await adminApi.unregisterPage(pageId)
      // Reload pages to reflect the change
      await loadPages()
      // Update hidden count if not currently showing hidden pages
      if (!showHidden) {
        await loadHiddenCount()
      }
    } catch (err) {
      setError('Failed to hide page')
      console.error('Error hiding page:', err)
    }
  }

  const handleRestorePage = async (pageId: string) => {
    if (!confirm('Are you sure you want to restore this page? This will make it visible again.')) {
      return
    }

    try {
      await adminApi.restorePage(pageId)
      // Reload pages to reflect the change
      await loadPages()
      // Update hidden count
      await loadHiddenCount()
    } catch (err) {
      setError('Failed to restore page')
      console.error('Error restoring page:', err)
    }
  }

  return (
    <div className="p-6 h-full overflow-auto">
      <header className="mb-8 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-tui-accent mb-2">PAGE MANAGEMENT</h1>
          <p className="text-tui-muted text-sm">Manage tracked pages and domains</p>
        </div>
        <div className="flex items-center space-x-6">
          <div className="flex items-center space-x-2">
            <label className="text-tui-muted text-sm">Filter by Domain:</label>
            <select
              value={selectedDomain}
              onChange={(e) => setSelectedDomain(e.target.value)}
              className="bg-tui-darker border border-tui-border p-2 text-tui-light font-mono focus:border-tui-accent focus:outline-none"
            >
              <option value="">All Domains</option>
              {domains.map(domain => (
                <option key={domain} value={domain}>{domain}</option>
              ))}
            </select>
          </div>

          <button
            onClick={() => setShowHidden(!showHidden)}
            className={`text-sm px-3 py-1 border rounded ${
              showHidden
                ? 'bg-tui-accent text-tui-darker border-tui-accent'
                : 'text-tui-muted border-tui-border hover:border-tui-accent hover:text-tui-accent'
            }`}
          >
            Show hidden {!showHidden && hiddenCount > 0 && `(${hiddenCount})`}
          </button>
        </div>
      </header>

      {/* Pages Table */}
      <div className="tui-panel">
        <div className="tui-panel-header">
          {showHidden ? 'Hidden Pages' : (selectedDomain ? `Pages for ${selectedDomain}` : 'All Pages')} ({filteredPages.length})
        </div>
        <div className="p-0">
          <table className="tui-table">
            <thead>
              {table.getHeaderGroups().map(headerGroup => (
                <tr key={headerGroup.id}>
                  {headerGroup.headers.map(header => (
                    <th
                      key={header.id}
                      className={header.column.getCanSort() ? 'cursor-pointer select-none' : ''}
                      onClick={header.column.getToggleSortingHandler()}
                    >
                      <div className="flex items-center space-x-1">
                        <span>
                          {flexRender(header.column.columnDef.header, header.getContext())}
                        </span>
                        {header.column.getCanSort() && (
                          <span className="text-tui-muted">
                            {{
                              asc: '↑',
                              desc: '↓',
                            }[header.column.getIsSorted() as string] ?? '↕'}
                          </span>
                        )}
                      </div>
                    </th>
                  ))}
                </tr>
              ))}
            </thead>
            <tbody>
              {table.getRowModel().rows.map(row => (
                <tr key={row.id}>
                  {row.getVisibleCells().map(cell => (
                    <td key={cell.id}>
                      {flexRender(cell.column.columnDef.cell, cell.getContext())}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}

export default Pages
