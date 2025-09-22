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
        <button className="text-tui-red hover:text-tui-accent text-sm">
          DELETE
        </button>
      )
    })
  ], [columnHelper])

  // Get unique domains and filter pages by selected domain
  const domains = useMemo(() => {
    const uniqueDomains = [...new Set(pages.map(page => page.domain).filter(Boolean))]
    return uniqueDomains.sort()
  }, [pages])

  const filteredPages = useMemo(() => {
    if (!selectedDomain) return pages
    return pages.filter(page => page.domain === selectedDomain)
  }, [pages, selectedDomain])

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
  }, [])

  const loadPages = async () => {
    try {
      setLoading(true)
      setError(null)
      const response = await adminApi.listPages()
      setPages(response.data)
    } catch (err) {
      setError('Failed to load pages')
      console.error('Error loading pages:', err)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="p-6 h-full overflow-auto">
      <header className="mb-8 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-tui-accent mb-2">PAGE MANAGEMENT</h1>
          <p className="text-tui-muted text-sm">Manage tracked pages and domains</p>
        </div>
        <div className="flex items-center space-x-4">
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
      </header>

      {/* Pages Table */}
      <div className="tui-panel">
        <div className="tui-panel-header">
          {selectedDomain ? `Pages for ${selectedDomain}` : 'All Pages'} ({filteredPages.length})
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
