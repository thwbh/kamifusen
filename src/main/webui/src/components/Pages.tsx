import React, { useState } from 'react'

interface Page {
  id: number
  path: string
  domain: string
  visits: number
  lastVisit: string
}

const Pages: React.FC = () => {
  const [pages] = useState<Page[]>([
    { id: 1, path: '/home', domain: 'example.com', visits: 245, lastVisit: '2024-01-15 14:30' },
    { id: 2, path: '/about', domain: 'example.com', visits: 123, lastVisit: '2024-01-15 14:28' },
    { id: 3, path: '/contact', domain: 'test.org', visits: 67, lastVisit: '2024-01-15 14:25' },
    { id: 4, path: '/blog', domain: 'myblog.net', visits: 89, lastVisit: '2024-01-15 14:22' },
    { id: 5, path: '/products', domain: 'shop.com', visits: 156, lastVisit: '2024-01-15 14:20' },
  ])

  const [newPage, setNewPage] = useState({ path: '', domain: '' })
  const [showAddForm, setShowAddForm] = useState(false)

  const handleAddPage = () => {
    if (newPage.path && newPage.domain) {
      // Here you would typically send a POST request to your API
      console.log('Adding page:', newPage)
      setNewPage({ path: '', domain: '' })
      setShowAddForm(false)
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
              <tr>
                <th>Path</th>
                <th>Domain</th>
                <th>Total Visits</th>
                <th>Last Visit</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {pages.map((page) => (
                <tr key={page.id}>
                  <td className="font-mono text-tui-light">{page.path}</td>
                  <td className="text-tui-muted">{page.domain}</td>
                  <td className="text-tui-green font-bold">{page.visits}</td>
                  <td className="text-tui-muted">{page.lastVisit}</td>
                  <td>
                    <button className="text-tui-red hover:text-tui-accent text-sm">
                      DELETE
                    </button>
                  </td>
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