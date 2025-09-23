import React, { useMemo, useState } from 'react'
import {
  useReactTable,
  getCoreRowModel,
  getSortedRowModel,
  flexRender,
  createColumnHelper,
  SortingState
} from '@tanstack/react-table'
import { PageWithStatsDto } from '../api'
import { usePages } from '../hooks'

const Pages: React.FC = () => {
  const [sorting, setSorting] = useState<SortingState>([])

  const {
    loading,
    error,
    selectedDomain,
    showHidden,
    hiddenCount,
    domains,
    filteredPages,
    setSelectedDomain,
    setShowHidden,
    hidePage,
    restorePage
  } = usePages()

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

  const handleHidePage = async (pageId: string) => {
    if (!confirm('Are you sure you want to hide this page? This will unregister it from tracking.')) {
      return
    }

    try {
      await hidePage(pageId)
    } catch (err) {
      console.error('Error hiding page:', err)
    }
  }

  const handleRestorePage = async (pageId: string) => {
    if (!confirm('Are you sure you want to restore this page? This will make it visible again.')) {
      return
    }

    try {
      await restorePage(pageId)
    } catch (err) {
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
