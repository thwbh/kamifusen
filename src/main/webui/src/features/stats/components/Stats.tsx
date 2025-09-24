import React from 'react'
import { useStats } from '../hooks'
import { LoadingState } from '../../../shared'

interface ChartData {
  label: string
  value: number
  category: string
}

const Stats: React.FC = () => {
  const { statsData, timeRange, loading, error, setTimeRange } = useStats()

  if (loading) {
    return <LoadingState message="Loading statistics..." fullHeight />
  }

  if (error) {
    return (
      <div className="p-6 h-full flex items-center justify-center">
        <div className="text-tui-red">{error}</div>
      </div>
    )
  }

  const visitData: ChartData[] = statsData?.visitData || []
  const topPages = statsData?.topPages || []
  const domainStats = statsData?.domainStats || []

  const maxVisits = Math.max(...visitData.map(d => d.value))

  // Map categories to CSS classes
  const getCategoryColor = (category: string): string => {
    console.log(`category=${category}`);
    let color = '';

    switch (category) {
      case 'low':
        color = 'text-tui-yellow'; break;
      case 'high':
        color = 'text-tui-accent'; break;
      case 'normal':
      default:
        color = 'text-tui-green'; break;
    }

    return color;
  }

  return (
    <div className="p-6 h-full overflow-auto">
      <header className="mb-8 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-tui-accent mb-2">ANALYTICS</h1>
          <p className="text-tui-muted text-sm">Detailed traffic statistics and insights</p>
        </div>
        <select
          value={timeRange}
          onChange={(e) => setTimeRange(e.target.value)}
          className="bg-tui-darker border border-tui-border p-2 text-tui-light font-mono focus:border-tui-accent focus:outline-none"
        >
          <option value="24h">Last 24 Hours</option>
          <option value="7d">Last 7 Days</option>
          <option value="30d">Last 30 Days</option>
          <option value="90d">Last 90 Days</option>
        </select>
      </header>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        {/* Visit Trends Chart */}
        <div className="tui-panel">
          <div className="tui-panel-header">
            Visit Trends - {timeRange}
          </div>
          <div className="p-6">
            <div className="space-y-4">
              {visitData.map((day, index) => (
                <div key={index} className="flex items-center space-x-4">
                  <div className="w-12 text-tui-muted text-sm font-mono">{day.label}</div>
                  <div className="flex-1 flex items-center">
                    <div
                      className="bg-tui-accent h-6 mr-2 transition-all duration-300"
                      style={{ width: `${(day.value / maxVisits) * 100}%` }}
                    ></div>
                    <span className={`text-sm font-mono ${getCategoryColor(day.category)}`}>{day.value}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Top Pages */}
        <div className="tui-panel">
          <div className="tui-panel-header">
            Top Pages
          </div>
          <div className="p-0">
            <table className="tui-table">
              <thead>
                <tr>
                  <th>Domain</th>
                  <th>Path</th>
                  <th>Visits</th>
                  <th>Share</th>
                </tr>
              </thead>
              <tbody>
                {topPages.map((page, index) => (
                  <tr key={index}>
                    <td className="font-mono text-tui-muted">{page.domain}</td>
                    <td className="font-mono text-tui-light">{page.path}</td>
                    <td className="text-tui-green font-bold">{page.visits}</td>
                    <td className="text-tui-yellow">{page.percentage}%</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Domain Distribution */}
        <div className="tui-panel">
          <div className="tui-panel-header">
            Domain Distribution
          </div>
          <div className="p-6">
            <div className="space-y-4">
              {domainStats.map((domain, index) => (
                <div key={index} className="space-y-2">
                  <div className="flex justify-between items-center">
                    <span className="text-tui-light font-mono">{domain.domain}</span>
                    <div className="flex items-center space-x-2">
                      <span className="text-tui-green font-bold">{domain.visits}</span>
                      <span className="text-tui-yellow text-sm">({domain.percentage}%)</span>
                    </div>
                  </div>
                  <div className="w-full bg-tui-darker h-2 rounded">
                    <div
                      className="bg-tui-accent h-2 rounded transition-all duration-300"
                      style={{ width: `${domain.percentage}%` }}
                    ></div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Summary Stats */}
        <div className="tui-panel">
          <div className="tui-panel-header">
            Summary Metrics
          </div>
          <div className="p-6 space-y-6">
            <div className="grid grid-cols-3 gap-4">
              <div className="text-center">
                <div className="text-2xl font-bold text-tui-green">{statsData?.totalVisits || 0}</div>
                <div className="text-tui-muted text-sm">Total Visits</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-tui-yellow">{statsData?.totalPages || 0}</div>
                <div className="text-tui-muted text-sm">Total Pages</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-tui-accent">{statsData?.totalDomains || 0}</div>
                <div className="text-tui-muted text-sm">Domains</div>
              </div>
            </div>

            <div className="border-t border-tui-border pt-4">
              <div className="text-sm text-tui-muted">Last Updated:</div>
              <div className="text-tui-light font-mono">
                {new Date().toLocaleString()}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Stats
