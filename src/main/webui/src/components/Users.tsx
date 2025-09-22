import React, { useState, useEffect, useMemo, useRef } from 'react'
import {
  useReactTable,
  getCoreRowModel,
  getSortedRowModel,
  flexRender,
  createColumnHelper,
  SortingState
} from '@tanstack/react-table'
import { AppAdminResourceApi, ApiUserDto } from '../api/gen/index'


const Users: React.FC = () => {
  const [users, setUsers] = useState<ApiUserDto[]>([])
  const [systemUsers, setSystemUsers] = useState<ApiUserDto[]>([])
  const [apiUsers, setApiUsers] = useState<ApiUserDto[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [showAddForm, setShowAddForm] = useState(false)
  const [newUser, setNewUser] = useState({ username: '', expiresAt: '', password: '' })
  const [generatedKey, setGeneratedKey] = useState('')
  const [isRenewalMode, setIsRenewalMode] = useState(false)
  const [isEditMode, setIsEditMode] = useState(false)
  const [editingUser, setEditingUser] = useState<ApiUserDto | null>(null)
  const [isApiKeyCreation, setIsApiKeyCreation] = useState(false)

  const [showCreateKey, setShowCreateKey] = useState(false)
  const [keyName, setKeyName] = useState('')
  const [keyExpiresAt, setKeyExpiresAt] = useState('')
  const [isEditingKey, setIsEditingKey] = useState(false)
  const [editingApiKey, setEditingApiKey] = useState<ApiUserDto | null>(null)
  const [sorting, setSorting] = useState<SortingState>([])

  const errorRef = useRef<HTMLDivElement>(null)
  const generatedKeyRef = useRef<HTMLDivElement>(null)
  const createKeyGeneratedRef = useRef<HTMLDivElement>(null)
  const systemUserGeneratedRef = useRef<HTMLDivElement>(null)
  const adminApi = new AppAdminResourceApi()

  const scrollToError = () => {
    if (errorRef.current) {
      errorRef.current.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
      })
    }
  }

  const scrollToGeneratedKey = () => {
    // Check which generated key display is currently visible and scroll to it
    if (showCreateKey && createKeyGeneratedRef.current) {
      createKeyGeneratedRef.current.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
      })
    } else if (showAddForm && systemUserGeneratedRef.current) {
      systemUserGeneratedRef.current.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
      })
    } else if (generatedKeyRef.current) {
      generatedKeyRef.current.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
      })
    }
  }

  const systemUserColumnHelper = createColumnHelper<ApiUserDto>()
  const apiKeyColumnHelper = createColumnHelper<ApiUserDto>()

  const systemUserColumns = useMemo(() => [
    systemUserColumnHelper.accessor('username', {
      header: 'Username',
      cell: info => <span className="font-mono text-tui-light">{info.getValue()}</span>
    }),
    systemUserColumnHelper.accessor('role', {
      header: 'Role',
      cell: info => <span className="text-tui-yellow">{info.getValue()}</span>
    }),
    systemUserColumnHelper.accessor('added', {
      header: 'Added',
      cell: info => (
        <span className="text-tui-muted">
          {info.getValue() ? new Date(info.getValue()).toLocaleDateString() : 'Unknown'}
        </span>
      )
    }),
    systemUserColumnHelper.display({
      id: 'actions',
      header: 'Actions',
      cell: info => {
        const user = info.row.original;
        const isAdmin = user.role === 'app-admin';

        return (
          <div>
            <button
              onClick={() => handleEditUser(user)}
              className="text-tui-accent hover:text-tui-accent-hover text-sm mr-2"
            >
              EDIT
            </button>
            {!isAdmin && (
              <button
                onClick={() => handleDeleteUser(user)}
                className="text-tui-red hover:text-tui-accent text-sm"
              >
                DELETE
              </button>
            )}
          </div>
        );
      }
    })
  ], [systemUserColumnHelper])

  const apiKeyColumns = useMemo(() => [
    apiKeyColumnHelper.accessor('username', {
      header: 'Name',
      cell: info => <span className="font-mono text-tui-light">{info.getValue()}</span>
    }),
    apiKeyColumnHelper.accessor('added', {
      header: 'Created',
      cell: info => (
        <span className="text-tui-muted">
          {info.getValue() ? new Date(info.getValue()).toLocaleDateString() : 'Unknown'}
        </span>
      )
    }),
    apiKeyColumnHelper.accessor('expiresAt', {
      header: 'Expires',
      cell: info => {
        const expiresAt = info.getValue();
        if (!expiresAt) {
          return <span className="text-tui-muted">Never</span>;
        }

        const expirationDate = new Date(expiresAt);
        const isExpired = expirationDate <= new Date();

        return (
          <span className={isExpired ? "text-tui-red" : "text-tui-muted"}>
            {expirationDate.toLocaleDateString()}
            {isExpired && <span className="ml-1 text-xs">(EXPIRED)</span>}
          </span>
        );
      }
    }),
    apiKeyColumnHelper.display({
      id: 'status',
      header: 'Status',
      cell: info => {
        const user = info.row.original;
        const isExpired = user.expiresAt && new Date(user.expiresAt) <= new Date();

        return (
          <span className={isExpired ? "text-tui-red text-sm" : "text-tui-green text-sm"}>
            {isExpired ? 'EXPIRED' : 'ACTIVE'}
          </span>
        );
      }
    }),
    apiKeyColumnHelper.display({
      id: 'actions',
      header: 'Actions',
      cell: info => {
        const user = info.row.original;
        const isExpired = user.expiresAt && new Date(user.expiresAt) <= new Date();

        return (
          <div>
            {isExpired ? (
              <>
                <button
                  onClick={() => handleRenewUser(user)}
                  className="text-tui-green hover:text-tui-accent text-sm mr-2"
                >
                  RENEW
                </button>
                <button
                  onClick={() => handleDeleteUser(user)}
                  className="text-tui-red hover:text-tui-accent text-sm"
                >
                  DELETE
                </button>
              </>
            ) : (
              <>
                <button
                  onClick={() => handleEditApiKey(user)}
                  className="text-tui-accent hover:text-tui-accent-hover text-sm mr-2"
                >
                  EDIT
                </button>
                <button
                  onClick={() => handleRetireUser(user)}
                  className="text-tui-red hover:text-tui-accent text-sm"
                >
                  REVOKE
                </button>
              </>
            )}
          </div>
        );
      }
    })
  ], [apiKeyColumnHelper])

  const systemUserTable = useReactTable({
    data: systemUsers,
    columns: systemUserColumns,
    state: {
      sorting,
    },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
  })

  const apiKeyTable = useReactTable({
    data: apiUsers,
    columns: apiKeyColumns,
    state: {
      sorting,
    },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
  })

  useEffect(() => {
    loadUsers()
  }, [])

  useEffect(() => {
    if (error) {
      // Small delay to ensure DOM is updated
      setTimeout(scrollToError, 100)
    }
  }, [error])

  useEffect(() => {
    if (generatedKey) {
      // Small delay to ensure DOM is updated
      setTimeout(scrollToGeneratedKey, 100)
    }
  }, [generatedKey])

  const loadUsers = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await adminApi.listUsers();

      console.log(response);

      // Extract users data from the API response
      if (response.data) {
        setUsers(response.data);

        // Split users by role
        const admins = response.data.filter(user =>
          user.role === 'app-admin' || user.role === 'api-admin'
        );
        const apiKeys = response.data.filter(user => user.role === 'api-user');

        setSystemUsers(admins);
        setApiUsers(apiKeys);
      } else {
        // Fallback to empty array if no data
        setUsers([]);
        setSystemUsers([]);
        setApiUsers([]);
      }
    } catch (err) {
      setError('Failed to load users');
      console.error('Error loading users:', err);
      // Set empty array on error
      setUsers([]);
      setSystemUsers([]);
      setApiUsers([]);
    } finally {
      setLoading(false);
    }
  }

  const handleAddUser = async () => {
    if (newUser.username) {
      try {
        if (isEditMode && editingUser?.id) {
          // Update existing system user (with optional password)
          if (newUser.password) {
            // If password is provided, use the password update endpoint
            await adminApi.updateUserPassword(editingUser.id, newUser.username, newUser.password);
          } else {
            // If no password, use the regular update endpoint (for API users with expiration)
            await adminApi.updateUser(editingUser.id, newUser.username, newUser.expiresAt || '');
          }
          setNewUser({ username: '', expiresAt: '', password: '' });
          setShowAddForm(false);
          setIsEditMode(false);
          setEditingUser(null);
          setIsApiKeyCreation(false);
          loadUsers(); // Reload users list
        } else if (isRenewalMode && editingUser?.id) {
          // Renew existing system user
          const response = await adminApi.renewApiKey(editingUser.id, newUser.expiresAt || '');
          setGeneratedKey(response.data);
          setNewUser({ username: '', expiresAt: '', password: '' });
          // Don't close the form yet - let user see the generated key
          // setShowAddForm(false);
          setIsRenewalMode(false);
          setEditingUser(null);
          setIsApiKeyCreation(false);
          loadUsers(); // Reload users list
        } else {
          // Create new system user
          const role = editingUser?.role || 'api-admin';
          const response = await adminApi.generateApiKey(newUser.username, role, newUser.expiresAt || '');
          setGeneratedKey(response.data);
          setNewUser({ username: '', expiresAt: '', password: '' });
          // Don't close the form yet - let user see the generated key
          // setShowAddForm(false);
          setIsRenewalMode(false);
          setIsApiKeyCreation(false);
          loadUsers(); // Reload users list
        }
      } catch (err) {
        setError(isEditMode ? 'Failed to update user' : 'Failed to create/renew user');
        console.error('Error with user operation:', err);
      }
    }
  }

  const handleCreateApiKey = async () => {
    if (keyName) {
      try {
        if (isEditingKey && editingApiKey?.id) {
          // Update existing API key
          await adminApi.updateUser(editingApiKey.id, keyName, keyExpiresAt || '');
          setKeyName('');
          setKeyExpiresAt('');
          setShowCreateKey(false);
          setIsEditingKey(false);
          setEditingApiKey(null);
          setIsRenewalMode(false);
          loadUsers(); // Reload users list
        } else if (isRenewalMode && editingApiKey?.role === 'api-user' && editingApiKey?.id) {
          // Renew API key (update existing user with new password)
          const response = await adminApi.renewApiKey(editingApiKey.id, keyExpiresAt || '');
          setGeneratedKey(response.data);
          setKeyName('');
          setKeyExpiresAt('');
          // Don't close the panel yet - let user see the generated key
          // setShowCreateKey(false);
          setIsRenewalMode(false);
          setEditingApiKey(null);
          loadUsers(); // Reload users list
        } else {
          // Create new API key
          const response = await adminApi.generateApiKey(keyName, 'api-user', keyExpiresAt || '');
          setGeneratedKey(response.data);
          setKeyName('');
          setKeyExpiresAt('');
          // Don't close the panel yet - let user see the generated key
          // setShowCreateKey(false);
          loadUsers(); // Reload users list
        }
      } catch (err) {
        const errorMessage = isEditingKey ? 'Failed to update API key'
                           : isRenewalMode ? 'Failed to renew API key'
                           : 'Failed to create API key';
        setError(errorMessage);
        console.error('Error with API key operation:', err);
      }
    }
  }

  const handleRetireUser = async (user: ApiUserDto) => {
    if (!user.id) {
      setError('Cannot retire user: missing user ID');
      return;
    }

    const confirmed = window.confirm(
      `Are you sure you want to retire user "${user.username}"?\n\nThis will set their expiration to the current timestamp and they will no longer be able to use their API key.`
    );

    if (confirmed) {
      try {
        await adminApi.retireApiKey(user.id);
        setError(null);
        loadUsers(); // Reload users list to show updated status
      } catch (err) {
        setError(`Failed to retire user "${user.username}"`);
        console.error('Error retiring user:', err);
      }
    }
  }

  const handleRenewUser = (user: ApiUserDto) => {
    // For API users, use the quick CREATE KEY panel
    if (user.role === 'api-user') {
      setKeyName(user.username);
      setKeyExpiresAt(''); // Start with empty expiration for renewal
      setIsEditingKey(false); // This is renewal, not editing
      setEditingApiKey(user); // Store the user being renewed
      setIsRenewalMode(true);
      setShowCreateKey(true);
      setGeneratedKey('');
    } else {
      // For system users, use the ADD USER panel in System Users section
      setNewUser({
        username: user.username,
        expiresAt: '',
        password: ''
      });
      setIsRenewalMode(true);
      setEditingUser(user);
      setShowAddForm(true);
      setGeneratedKey('');
    }
  }

  const handleEditUser = (user: ApiUserDto) => {
    // Pre-fill the form with the user's current data
    setNewUser({
      username: user.username,
      expiresAt: user.expiresAt ? new Date(user.expiresAt).toISOString().slice(0, 16) : '',
      password: ''
    });
    setIsEditMode(true);
    setEditingUser(user);
    setShowAddForm(true);
    setGeneratedKey('');
  }

  const handleEditApiKey = (user: ApiUserDto) => {
    // Pre-fill the CREATE KEY panel with the API key's current data
    setKeyName(user.username);
    setKeyExpiresAt(user.expiresAt ? new Date(user.expiresAt).toISOString().slice(0, 16) : '');
    setIsEditingKey(true);
    setEditingApiKey(user);
    setShowCreateKey(true);
    setGeneratedKey('');
  }

  const handleDeleteUser = async (user: ApiUserDto) => {
    if (!user.id) {
      setError('Cannot delete user: missing user ID');
      return;
    }

    const confirmed = window.confirm(
      `Are you sure you want to delete user "${user.username}"?\n\nThis action cannot be undone.`
    );

    if (confirmed) {
      try {
        await adminApi.deleteUser(user.id);
        setError(null);
        loadUsers(); // Reload users list
      } catch (err) {
        setError(`Failed to delete user "${user.username}"`);
        console.error('Error deleting user:', err);
      }
    }
  }

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text)
  }

  return (
    <div className="p-6 h-full overflow-auto">
      <header className="mb-8">
        <div>
          <h1 className="text-2xl font-bold text-tui-accent mb-2">USER MANAGEMENT</h1>
          <p className="text-tui-muted text-sm">Manage users and API keys</p>
        </div>
      </header>

      {/* Error Display */}
      {error && (
        <div ref={errorRef} className="tui-panel mb-6 border-tui-red">
          <div className="tui-panel-header bg-tui-red text-tui-dark">
            Error
          </div>
          <div className="p-4">
            <p className="text-tui-red">{error}</p>
            <button
              onClick={() => setError(null)}
              className="mt-2 text-tui-muted hover:text-tui-accent text-sm"
            >
              DISMISS
            </button>
          </div>
        </div>
      )}


      {/* System Users Section */}
      <div className="tui-panel mb-6">
        <div className="tui-panel-header flex justify-between items-center">
          <span>System Users ({systemUsers.length})</span>
          <button
            onClick={() => {
              setShowAddForm(!showAddForm);
              if (showAddForm) {
                // Reset all state when closing
                setIsRenewalMode(false);
                setIsEditMode(false);
                setEditingUser(null);
                setIsApiKeyCreation(false);
                setNewUser({ username: '', expiresAt: '', password: '' });
                setGeneratedKey('');
              }
            }}
            className="text-tui-accent hover:text-tui-accent-hover text-sm"
          >
            {showAddForm ? 'CANCEL' : 'ADD USER'}
          </button>
        </div>

        {/* Add User Panel */}
        {showAddForm && (
          <div className="p-4 border-b border-tui-border bg-tui-darker">
            {(isEditMode || isRenewalMode) && (
              <div className="mb-4">
                <h3 className="text-tui-accent text-sm font-mono">
                  {isEditMode ? `EDITING: ${editingUser?.username}`
                   : isRenewalMode ? `RENEWING: ${editingUser?.username}`
                   : ''}
                </h3>
              </div>
            )}
            <div className="space-y-4">
              <div>
                <label className="block text-tui-muted text-sm mb-2">Username</label>
                <input
                  type="text"
                  value={newUser.username}
                  onChange={(e) => setNewUser({ ...newUser, username: e.target.value })}
                  className="w-full bg-tui-dark border border-tui-border p-2 text-tui-light font-mono focus:border-tui-accent focus:outline-none"
                  placeholder={isEditMode ? "Edit username" : "Enter username"}
                />
              </div>
              {isEditMode && (
                <div>
                  <label className="block text-tui-muted text-sm mb-2">New Password</label>
                  <input
                    type="password"
                    value={newUser.password}
                    onChange={(e) => setNewUser({ ...newUser, password: e.target.value })}
                    className="w-full bg-tui-dark border border-tui-border p-2 text-tui-light font-mono focus:border-tui-accent focus:outline-none"
                    placeholder="Enter new password"
                  />
                  <p className="text-tui-muted text-xs mt-1">Leave empty to keep current password</p>
                </div>
              )}
              <button
                onClick={handleAddUser}
                disabled={!newUser.username}
                className="tui-button disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isEditMode ? 'UPDATE USER'
                 : isRenewalMode ? 'RENEW USER'
                 : 'CREATE USER'}
              </button>

              {generatedKey && !isEditMode && (
                <div ref={systemUserGeneratedRef} className="p-4 bg-tui-dark border border-tui-accent rounded mt-4">
                  <p className="text-tui-accent text-sm mb-2">
                    {isRenewalMode ? 'Renewed API Key (copy now, won\'t be shown again):'
                     : 'Generated API Key (copy now, won\'t be shown again):'}
                  </p>
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
                  <button
                    onClick={() => {
                      setGeneratedKey('');
                      setShowAddForm(false);
                      setIsRenewalMode(false);
                      setIsEditMode(false);
                      setEditingUser(null);
                    }}
                    className="mt-2 text-tui-muted hover:text-tui-accent text-sm"
                  >
                    DISMISS
                  </button>
                </div>
              )}
            </div>
          </div>
        )}
        <div className="p-0">
          <table className="tui-table">
            <thead>
              {systemUserTable.getHeaderGroups().map(headerGroup => (
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
              {systemUserTable.getRowModel().rows.map(row => (
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

      {/* API Keys Section */}
      <div className="tui-panel">
        <div className="tui-panel-header flex justify-between items-center">
          <span>API Keys ({apiUsers.length})</span>
          <button
            onClick={() => {
              setShowCreateKey(!showCreateKey);
              if (showCreateKey) {
                // Reset all state when closing
                setIsEditingKey(false);
                setIsRenewalMode(false);
                setEditingApiKey(null);
                setKeyName('');
                setKeyExpiresAt('');
                setGeneratedKey('');
              }
            }}
            className="text-tui-accent hover:text-tui-accent-hover text-sm"
          >
            {showCreateKey ? 'CANCEL' : 'CREATE KEY'}
          </button>
        </div>

        {/* Create API Key Panel */}
        {showCreateKey && (
          <div className="p-4 border-b border-tui-border bg-tui-darker">
            {(isEditingKey || isRenewalMode) && (
              <div className="mb-4">
                <h3 className="text-tui-accent text-sm font-mono">
                  {isEditingKey ? `EDITING: ${editingApiKey?.username}`
                   : isRenewalMode ? `RENEWING: ${editingApiKey?.username}`
                   : ''}
                </h3>
              </div>
            )}
            <div className="space-y-4">
              <div>
                <label className="block text-tui-muted text-sm mb-2">
                  {isEditingKey ? 'Key Name' : 'Key Name'}
                </label>
                <input
                  type="text"
                  value={keyName}
                  onChange={(e) => setKeyName(e.target.value)}
                  className="w-full bg-tui-dark border border-tui-border p-2 text-tui-light font-mono focus:border-tui-accent focus:outline-none"
                  placeholder={isEditingKey ? "Edit key name" : "Enter key name"}
                />
              </div>
              <div>
                <label className="block text-tui-muted text-sm mb-2">Expiration Date</label>
                <input
                  type="date"
                  value={keyExpiresAt ? keyExpiresAt.split('T')[0] : ''}
                  onChange={(e) => setKeyExpiresAt(e.target.value ? `${e.target.value}T23:59:59` : '')}
                  className="w-full bg-tui-dark border border-tui-border p-2 text-tui-light font-mono focus:border-tui-accent focus:outline-none"
                />
                <p className="text-tui-muted text-xs mt-1">Leave empty for no expiration</p>
              </div>
              <button
                onClick={handleCreateApiKey}
                disabled={!keyName}
                className="tui-button disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isEditingKey ? 'UPDATE KEY'
                 : isRenewalMode ? 'RENEW KEY'
                 : 'GENERATE KEY'}
              </button>

              {generatedKey && (
                <div ref={createKeyGeneratedRef} className="p-4 bg-tui-dark border border-tui-accent rounded mt-4">
                  <p className="text-tui-accent text-sm mb-2">
                    {isRenewalMode ? 'Renewed API Key (copy now, won\'t be shown again):'
                     : 'Generated API Key (copy now, won\'t be shown again):'}
                  </p>
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
                  <button
                    onClick={() => {
                      setGeneratedKey('');
                      setShowCreateKey(false);
                      setIsRenewalMode(false);
                      setIsEditingKey(false);
                      setEditingApiKey(null);
                    }}
                    className="mt-2 text-tui-muted hover:text-tui-accent text-sm"
                  >
                    DISMISS
                  </button>
                </div>
              )}
            </div>
          </div>
        )}

        <div className="p-0">
          <table className="tui-table">
            <thead>
              {apiKeyTable.getHeaderGroups().map(headerGroup => (
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
              {apiKeyTable.getRowModel().rows.map(row => (
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

export default Users
