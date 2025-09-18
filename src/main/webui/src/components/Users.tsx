import React, { useState, useEffect } from 'react'

interface User {
  id: number
  username: string
  apiKeysCount: number
  lastLogin: string
  status: 'active' | 'inactive'
}

interface ApiKey {
  id: string
  name: string
  created: string
  lastUsed: string
  status: 'active' | 'revoked'
}

const Users: React.FC = () => {
  const [users, setUsers] = useState<User[]>([
    { id: 1, username: 'admin', apiKeysCount: 2, lastLogin: '2024-01-15 14:30', status: 'active' },
    { id: 2, username: 'user1', apiKeysCount: 1, lastLogin: '2024-01-14 09:15', status: 'active' },
    { id: 3, username: 'user2', apiKeysCount: 0, lastLogin: '2024-01-10 16:45', status: 'inactive' },
  ])

  const [apiKeys] = useState<ApiKey[]>([
    { id: 'key_abc123', name: 'Production Site', created: '2024-01-01', lastUsed: '2024-01-15 14:30', status: 'active' },
    { id: 'key_def456', name: 'Development', created: '2024-01-05', lastUsed: '2024-01-14 09:15', status: 'active' },
    { id: 'key_ghi789', name: 'Old Site', created: '2023-12-15', lastUsed: '2023-12-20 11:30', status: 'revoked' },
  ])

  const [showCreateKey, setShowCreateKey] = useState(false)
  const [keyName, setKeyName] = useState('')
  const [generatedKey, setGeneratedKey] = useState('')

  useEffect(() => {
    await fetch('/admin/users').then((response: Response) -> {
      setUsers(response)
    });

  }

  const handleCreateApiKey = () => {
    if (keyName) {
      // Simulate API key generation
      const newKey = 'key_' + Math.random().toString(36).substr(2, 16)
      setGeneratedKey(newKey)
      setKeyName('')
    }
  }

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text)
  }

  return (
    <div className="p-6 h-full overflow-auto">
      <header className="mb-8">
        <h1 className="text-2xl font-bold text-tui-accent mb-2">USER MANAGEMENT</h1>
        <p className="text-tui-muted text-sm">Manage users and API keys</p>
      </header>

      {/* Users Section */}
      <div className="tui-panel mb-6">
        <div className="tui-panel-header">
          System Users ({users.length})
        </div>
        <div className="p-0">
          <table className="tui-table">
            <thead>
              <tr>
                <th>Username</th>
                <th>Roles</th>
                <th>Expires</th>
                <th>Added</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td className="font-mono text-tui-light">{user.username}</td>
                  <td className="text-tui-yellow">{user.apiKeysCount}</td>
                  <td className="text-tui-muted">{user.lastLogin}</td>
                  <td>
                    <span className={`text-sm ${user.status === 'active' ? 'text-tui-green' : 'text-tui-red'}`}>
                      {user.status.toUpperCase()}
                    </span>
                  </td>
                  <td>
                    <button className="text-tui-accent hover:text-tui-accent-hover text-sm mr-2">
                      EDIT
                    </button>
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

      {/* API Keys Section */}
      <div className="tui-panel">
        <div className="tui-panel-header flex justify-between items-center">
          <span>API Keys ({apiKeys.length})</span>
          <button
            onClick={() => setShowCreateKey(!showCreateKey)}
            className="text-tui-accent hover:text-tui-accent-hover text-sm"
          >
            {showCreateKey ? 'CANCEL' : 'CREATE KEY'}
          </button>
        </div>

        {/* Create API Key Form */}
        {showCreateKey && (
          <div className="p-4 border-b border-tui-border bg-tui-darker">
            <div className="space-y-4">
              <div>
                <label className="block text-tui-muted text-sm mb-2">Key Name</label>
                <input
                  type="text"
                  value={keyName}
                  onChange={(e) => setKeyName(e.target.value)}
                  className="w-full bg-tui-dark border border-tui-border p-2 text-tui-light font-mono focus:border-tui-accent focus:outline-none"
                  placeholder="Enter key name"
                />
              </div>
              <button
                onClick={handleCreateApiKey}
                disabled={!keyName}
                className="tui-button disabled:opacity-50 disabled:cursor-not-allowed"
              >
                GENERATE KEY
              </button>

              {generatedKey && (
                <div className="p-4 bg-tui-dark border border-tui-accent rounded">
                  <p className="text-tui-accent text-sm mb-2">Generated API Key (copy now, won't be shown again):</p>
                  <div className="flex items-center space-x-2">
                    <code className="flex-1 p-2 bg-tui-darker text-tui-light font-mono text-sm">
                      {generatedKey}
                    </code>
                    <button
                      onClick={() => copyToClipboard(generatedKey)}
                      className="px-3 py-2 bg-tui-accent text-tui-dark text-sm font-mono hover:bg-tui-accent-hover"
                    >
                      COPY
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}

        <div className="p-0">
          <table className="tui-table">
            <thead>
              <tr>
                <th>Key ID</th>
                <th>Name</th>
                <th>Created</th>
                <th>Last Used</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {apiKeys.map((key) => (
                <tr key={key.id}>
                  <td className="font-mono text-tui-light">{key.id}</td>
                  <td className="text-tui-muted">{key.name}</td>
                  <td className="text-tui-muted">{key.created}</td>
                  <td className="text-tui-muted">{key.lastUsed}</td>
                  <td>
                    <span className={`text-sm ${key.status === 'active' ? 'text-tui-green' : 'text-tui-red'}`}>
                      {key.status.toUpperCase()}
                    </span>
                  </td>
                  <td>
                    {key.status === 'active' ? (
                      <button className="text-tui-red hover:text-tui-accent text-sm">
                        REVOKE
                      </button>
                    ) : (
                      <button className="text-tui-muted text-sm cursor-not-allowed">
                        REVOKED
                      </button>
                    )}
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

export default Users
