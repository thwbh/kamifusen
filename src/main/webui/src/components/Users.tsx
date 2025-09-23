import React, {useState, useRef, useEffect, useCallback} from 'react'
import { ApiUserDto } from '../api/gen/index'
import { useUsers } from '../hooks'
import { SortingState } from '@tanstack/react-table'
import SystemUsersTable from './user/SystemUsersTable'
import ApiKeysTable from './user/ApiKeysTable'
import UserForm from './user/UserForm'
import ApiKeyForm from './user/ApiKeyForm'
import ErrorDisplay from './error/ErrorDisplay'

const Users: React.FC = () => {
  // Hook state
  const {
    systemUsers,
    apiUsers,
    loading,
    error,
    generateApiKey,
    renewApiKey,
    retireApiKey,
    deleteUser,
    updateUser,
    updateUserPassword,
    clearError
  } = useUsers()

  // Local component state
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

  const scrollToError = () => {
    if (errorRef.current) {
      errorRef.current.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
      })
    }
  }




  useEffect(() => {
    if (error) {
      // Small delay to ensure DOM is updated
      setTimeout(scrollToError, 100)
    }
  }, [error])



  const handleAddUser = async () => {
    if (newUser.username) {
      try {
        if (isEditMode && editingUser?.id) {
          // Update existing system user (with optional password)
          if (newUser.password) {
            await updateUserPassword(editingUser.id, newUser.username, newUser.password);
          } else {
            await updateUser(editingUser.id, newUser.username, newUser.expiresAt || '');
          }
          setNewUser({ username: '', expiresAt: '', password: '' });
          setShowAddForm(false);
          setIsEditMode(false);
          setEditingUser(null);
          setIsApiKeyCreation(false);
        } else if (isRenewalMode && editingUser?.id) {
          // Renew existing system user
          const response = await renewApiKey(editingUser.id, newUser.expiresAt || '');
          setGeneratedKey(response);
          setNewUser({ username: '', expiresAt: '', password: '' });
          setIsRenewalMode(false);
          setEditingUser(null);
          setIsApiKeyCreation(false);
        } else {
          // Create new system user
          const role = editingUser?.role || 'api-admin';
          const response = await generateApiKey(newUser.username, newUser.expiresAt || '');
          setGeneratedKey(response);
          setNewUser({ username: '', expiresAt: '', password: '' });
          setIsRenewalMode(false);
          setIsApiKeyCreation(false);
        }
      } catch (err) {
        console.error('Error with user operation:', err);
      }
    }
  }

  const handleCreateApiKey = async () => {
    if (keyName) {
      try {
        if (isEditingKey && editingApiKey?.id) {
          // Update existing API key
          await updateUser(editingApiKey.id, keyName, keyExpiresAt || '');
          setKeyName('');
          setKeyExpiresAt('');
          setShowCreateKey(false);
          setIsEditingKey(false);
          setEditingApiKey(null);
          setIsRenewalMode(false);
        } else if (isRenewalMode && editingApiKey?.role === 'api-user' && editingApiKey?.id) {
          // Renew API key (update existing user with new password)
          const response = await renewApiKey(editingApiKey.id, keyExpiresAt || '');
          setGeneratedKey(response);
          setKeyName('');
          setKeyExpiresAt('');
          setIsRenewalMode(false);
          setEditingApiKey(null);
        } else {
          // Create new API key
          const response = await generateApiKey(keyName, keyExpiresAt || '');
          setGeneratedKey(response);
          setKeyName('');
          setKeyExpiresAt('');
        }
      } catch (err) {
        console.error('Error with API key operation:', err);
      }
    }
  }

  const handleRetireUser = async (user: ApiUserDto) => {
    if (!user.id) {
      return;
    }

    const confirmed = window.confirm(
      `Are you sure you want to retire user "${user.username}"?\n\nThis will set their expiration to the current timestamp and they will no longer be able to use their API key.`
    );

    if (confirmed) {
      try {
        await retireApiKey(user.id);
      } catch (err) {
        console.error('Error retiring user:', err);
      }
    }
  }

  const handleRenewUser = useCallback((user: ApiUserDto) => {
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
  }, [])

  const handleEditUser = useCallback((user: ApiUserDto) => {
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
  }, [])

  const handleEditApiKey = useCallback((user: ApiUserDto) => {
    // Pre-fill the CREATE KEY panel with the API key's current data
    setKeyName(user.username);
    setKeyExpiresAt(user.expiresAt ? new Date(user.expiresAt).toISOString().slice(0, 16) : '');
    setIsEditingKey(true);
    setEditingApiKey(user);
    setShowCreateKey(true);
    setGeneratedKey('');
  }, [])

  const handleDeleteUser = useCallback(async (user: ApiUserDto) => {
    if (!user.id) {
      return;
    }

    const confirmed = window.confirm(
      `Are you sure you want to delete user "${user.username}"?\n\nThis action cannot be undone.`
    );

    if (confirmed) {
      try {
        await deleteUser(user.id);
      } catch (err) {
        console.error('Error deleting user:', err);
      }
    }
  }, [deleteUser])

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text)
  }

  const handleUserFormDismiss = () => {
    setGeneratedKey('')
    setShowAddForm(false)
    setIsRenewalMode(false)
    setIsEditMode(false)
    setEditingUser(null)
  }

  const handleApiKeyFormDismiss = () => {
    setGeneratedKey('')
    setShowCreateKey(false)
    setIsRenewalMode(false)
    setIsEditingKey(false)
    setEditingApiKey(null)
  }

  return (
    <div className="p-6 h-full overflow-auto">
      <header className="mb-8">
        <div>
          <h1 className="text-2xl font-bold text-tui-accent mb-2">USER MANAGEMENT</h1>
          <p className="text-tui-muted text-sm">Manage users and API keys</p>
        </div>
      </header>

      <ErrorDisplay ref={errorRef} error={error} onClearError={clearError} />


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

        {showAddForm && (
          <UserForm
            newUser={newUser}
            onUserChange={setNewUser}
            onSubmit={handleAddUser}
            isEditMode={isEditMode}
            isRenewalMode={isRenewalMode}
            editingUser={editingUser}
            generatedKey={generatedKey}
            onGeneratedKeyDismiss={handleUserFormDismiss}
            onCopyToClipboard={copyToClipboard}
          />
        )}
        <SystemUsersTable
          users={systemUsers}
          sorting={sorting}
          onSortingChange={setSorting}
          onEditUser={handleEditUser}
          onDeleteUser={handleDeleteUser}
        />
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

        {showCreateKey && (
          <ApiKeyForm
            keyName={keyName}
            keyExpiresAt={keyExpiresAt}
            onKeyNameChange={setKeyName}
            onKeyExpiresAtChange={setKeyExpiresAt}
            onSubmit={handleCreateApiKey}
            isEditingKey={isEditingKey}
            isRenewalMode={isRenewalMode}
            editingApiKey={editingApiKey}
            generatedKey={generatedKey}
            onGeneratedKeyDismiss={handleApiKeyFormDismiss}
            onCopyToClipboard={copyToClipboard}
          />
        )}

        <ApiKeysTable
          users={apiUsers}
          sorting={sorting}
          onSortingChange={setSorting}
          onEditApiKey={handleEditApiKey}
          onRenewUser={handleRenewUser}
          onRetireUser={handleRetireUser}
          onDeleteUser={handleDeleteUser}
        />
      </div>
    </div>
  )
}

export default Users
