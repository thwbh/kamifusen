import React, { useState, useEffect, useMemo } from 'react'
import {
  useReactTable,
  getCoreRowModel,
  getSortedRowModel,
  flexRender,
  createColumnHelper,
  SortingState
} from '@tanstack/react-table'
import { AppAdminResourceApi, Page } from '../api/gen/index'

const Pages: React.FC = () => {
  const [pages, setPages] = useState<Page[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [newPage, setNewPage] = useState({ path: '', domain: '' })
  const [showAddForm, setShowAddForm] = useState(false)
  const [sorting, setSorting] = useState<SortingState>([])

  const adminApi = new AppAdminResourceApi()

  const columnHelper = createColumnHelper<Page>()

  const columns = useMemo(() => [
    columnHelper.accessor('path', {
      header: 'Path',
      cell: info => <span className="font-mono text-tui-light">{info.getValue()}</span>
    }),
    columnHelper.accessor('domain', {
      header: 'Domain',
      cell: info => <span className="text-tui-muted">{info.getValue()}</span>
    }),
    columnHelper.accessor('visits', {
      header: 'Visits',
      cell: info => <span className="text-tui-accent">{info.getValue()}</span>
    }),
    columnHelper.accessor('lastVisit', {
      header: 'Last Visit',
      cell: info => (
        <span className="text-tui-muted">
          {info.getValue() || 'Never'}
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

  const table = useReactTable({
    data: pages,
    columns,
    state: {
      sorting,
    },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
  })

  useEffect(() => {
    loadPages()
  }, [])

  const loadPages = async () => {
    try {
      setLoading(true)
      setError(null)
      await adminApi.adminPagesGet().then(response => setPages(response.data));
      // Note: The API returns HTML, so we'll keep static data for now
      // In a real implementation, the backend would return JSON data
      /*      setPages([
              { id: 1, path: '/home', domain: 'example.com', visits: 245, lastVisit: '2024-01-15 14:30' },
              { id: 2, path: '/about', domain: 'example.com', visits: 123, lastVisit: '2024-01-15 14:28' },
              { id: 3, path: '/contact', domain: 'test.org', visits: 67, lastVisit: '2024-01-15 14:25' },
              { id: 4, path: '/blog', domain: 'myblog.net', visits: 89, lastVisit: '2024-01-15 14:22' },
              { id: 5, path: '/products', domain: 'shop.com', visits: 156, lastVisit: '2024-01-15 14:20' },
            ]) */
    } catch (err) {
      setError('Failed to load pages')
      console.error('Error loading pages:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleAddPage = async () => {
    if (newPage.path && newPage.domain) {
      try {
        await adminApi.adminPageaddPost(newPage.path, newPage.domain)
        setNewPage({ path: '', domain: '' })
        setShowAddForm(false)
        // Reload pages after adding
        loadPages()
      } catch (err) {
        setError('Failed to add page')
        console.error('Error adding page:', err)
      }
    }
  }

  return (
    <div className="p-6 h-full overflow-auto">
      <header className="mb-8 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-tui-accent mb-2">PAGE MANAGEMENT</h1>
          <p className="text-tui-muted text-sm">Manage tracked pages and domains</p>
        </div>
        <button
          onClick={() => setShowAddForm(!showAddForm)}
          className="tui-button"
        >
          {showAddForm ? 'CANCEL' : 'ADD PAGE'}
        </button>
      </header>

      {/* Add Page Form */}
      {showAddForm && (
        <div className="tui-panel mb-6">
          <div className="tui-panel-header">
            Add New Page
          </div>
          <div className="p-4 space-y-4">
            <div>
              <label className="block text-tui-muted text-sm mb-2">Path</label>
              <input
                type="text"
                value={newPage.path}
                onChange={(e) => setNewPage({ ...newPage, path: e.target.value })}
                className="w-full bg-tui-darker border border-tui-border p-2 text-tui-light font-mono focus:border-tui-accent focus:outline-none"
                placeholder="/example-path"
              />
            </div>
            <div>
              <label className="block text-tui-muted text-sm mb-2">Domain</label>
              <input
                type="text"
                value={newPage.domain}
                onChange={(e) => setNewPage({ ...newPage, domain: e.target.value })}
                className="w-full bg-tui-darker border border-tui-border p-2 text-tui-light font-mono focus:border-tui-accent focus:outline-none"
                placeholder="example.com"
              />
            </div>
            <button
              onClick={handleAddPage}
              className="tui-button"
            >
              ADD PAGE
            </button>
          </div>
        </div>
      )}

      {/* Pages Table */}
      <div className="tui-panel">
        <div className="tui-panel-header">
          Tracked Pages ({pages.length})
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
