import React, { useMemo, useState } from 'react'
import { SortingState } from '@tanstack/react-table'
import { DataTable, DataTableConfig, DataTableColumn, DataTableAction } from 'crt-dojo'
import { PageWithStatsDto } from '../../../api'
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

  const columns = useMemo((): DataTableColumn<PageWithStatsDto>[] => [
    {
      key: 'path',
      header: 'Path',
      accessor: 'path',
      cell: (value: string) => <span className="font-mono text-tui-light">{value}</span>
    },
    {
      key: 'domain',
      header: 'Domain',
      accessor: 'domain',
      cell: (value: string) => <span className="text-tui-muted">{value}</span>
    },
    {
      key: 'visitCount',
      header: 'Visits',
      accessor: 'visitCount',
      cell: (value: number) => <span className="text-tui-accent">{value || 0}</span>
    },
    {
      key: 'lastHit',
      header: 'Last Visit',
      accessor: 'lastHit',
      cell: (value: string) => (
        <span className="text-tui-muted">
          {value ? new Date(value).toLocaleString() : 'Never'}
        </span>
      )
    }
  ], [])

  const actions = useMemo((): DataTableAction<PageWithStatsDto>[] => [
    {
      label: showHidden ? 'RESTORE' : 'HIDE',
      variant: 'danger',
      onClick: (page: PageWithStatsDto) => showHidden ? handleRestorePage(page.id) : handleHidePage(page.id)
    }
  ], [showHidden])

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
            className={`text-sm px-3 py-1 border rounded ${showHidden
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
        <DataTable config={{
          data: filteredPages,
          columns,
          sorting,
          onSortingChange: setSorting,
          actions
        }} />
      </div>
    </div>
  )
}

export default Pages
