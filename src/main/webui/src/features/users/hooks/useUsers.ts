import { useState, useEffect, useCallback, useMemo } from 'react'
import { AppAdminResourceApi, ApiUserDto } from '../../../api'

interface UseUsersState {
  users: ApiUserDto[]
  systemUsers: ApiUserDto[]
  apiUsers: ApiUserDto[]
  loading: boolean
  error: string | null
}

interface UseUsersActions {
  refreshUsers: () => Promise<void>
  generateApiKey: (username: string, expiresAt: string) => Promise<string>
  renewApiKey: (userId: string, expiresAt?: string) => Promise<string>
  retireApiKey: (userId: string) => Promise<void>
  deleteUser: (userId: string) => Promise<void>
  updateUser: (userId: string, username: string, expiresAt?: string) => Promise<void>
  updateUserPassword: (userId: string, username: string, password?: string) => Promise<void>
  clearError: () => void
}

export const useUsers = (): UseUsersState & UseUsersActions => {
  const [users, setUsers] = useState<ApiUserDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const adminApi = useMemo(() => new AppAdminResourceApi(), []);

  const systemUsers = useMemo(() => users.filter(user => user.role === 'app-admin'), [users]);
  const apiUsers = useMemo(() => users.filter(user => user.role === 'api-user'), [users]);

  const refreshUsers = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await adminApi.listUsers()
      if (response.status === 200) {
        setUsers(response.data);
      } else {
        throw new Error(`Failed to fetch users: ${response.status}`);
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred';
      setError(errorMessage);
      console.error('Error fetching users:', err);
    } finally {
      setLoading(false);
    }
  }, [adminApi])

  const generateApiKey = useCallback(async (username: string, expiresAt: string): Promise<string> => {
    try {
      setError(null)

      const response = await adminApi.generateApiKey(username, 'api-user', expiresAt)

      if (response.status === 200) {
        const generatedKey = response.data;

        // Manually refresh users list
        const usersResponse = await adminApi.listUsers()
        if (usersResponse.status === 200) {
          setUsers(usersResponse.data)
        }

        return generatedKey;
      } else {
        const errorText = response.data;
        throw new Error(errorText || `Failed to generate API key: ${response.status}`)
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      throw err
    }
  }, [adminApi])

  const renewApiKey = useCallback(async (userId: string, expiresAt?: string): Promise<string> => {
    try {
      setError(null)

      const response = await adminApi.renewApiKey(userId, expiresAt || '')

      if (response.status === 200) {
        const renewedKey = response.data;

        // Manually refresh users list
        const usersResponse = await adminApi.listUsers()
        if (usersResponse.status === 200) {
          setUsers(usersResponse.data)
        }

        return renewedKey
      } else {
        const errorText = response.data;
        throw new Error(errorText || `Failed to renew API key: ${response.status}`)
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      throw err
    }
  }, [adminApi])

  const retireApiKey = useCallback(async (userId: string): Promise<void> => {
    try {
      setError(null)

      const response = await adminApi.retireApiKey(userId)

      if (response.status === 200) {
        // Manually refresh users list
        const usersResponse = await adminApi.listUsers()
        if (usersResponse.status === 200) {
          setUsers(usersResponse.data)
        }
      } else {
        const errorText = response.data;
        throw new Error(errorText || `Failed to retire API key: ${response.status}`)
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      throw err
    }
  }, [adminApi])

  const deleteUser = useCallback(async (userId: string): Promise<void> => {
    try {
      setError(null)

      const response = await adminApi.deleteUser(userId)

      if (response.status === 200) {
        // Manually refresh users list
        const usersResponse = await adminApi.listUsers()
        if (usersResponse.status === 200) {
          setUsers(usersResponse.data)
        }
      } else {
        const errorText = response.data;
        throw new Error(errorText || `Failed to delete user: ${response.status}`)
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      throw err
    }
  }, [adminApi])

  const updateUser = useCallback(async (userId: string, username: string, expiresAt?: string): Promise<void> => {
    try {
      setError(null)

      const response = await adminApi.updateUser(userId, username, expiresAt || '')

      if (response.status === 200) {
        // Manually refresh users list
        const usersResponse = await adminApi.listUsers()
        if (usersResponse.status === 200) {
          setUsers(usersResponse.data)
        }
      } else {
        const errorText = response.data;
        throw new Error(errorText || `Failed to update user: ${response.status}`)
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      throw err
    }
  }, [adminApi])

  const updateUserPassword = useCallback(async (userId: string, username: string, password?: string): Promise<void> => {
    try {
      setError(null)

      const response = await adminApi.updateUserPassword(userId, username, password || '')

      if (response.status === 200) {
        // Manually refresh users list
        const usersResponse = await adminApi.listUsers()
        if (usersResponse.status === 200) {
          setUsers(usersResponse.data)
        }
      } else {
        const errorText = response.data;
        throw new Error(errorText || `Failed to update user password: ${response.status}`)
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      throw err
    }
  }, [adminApi])

  const clearError = useCallback(() => {
    setError(null)
  }, [])

  useEffect(() => {
    const loadInitialUsers = async () => {
      try {
        setLoading(true);
        setError(null);

        const response = await adminApi.listUsers()
        if (response.status === 200) {
          setUsers(response.data);
        } else {
          throw new Error(`Failed to fetch users: ${response.status}`);
        }
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred';
        setError(errorMessage);
        console.error('Error fetching users:', err);
      } finally {
        setLoading(false);
      }
    }

    loadInitialUsers()
  }, [adminApi])

  return {
    users,
    systemUsers,
    apiUsers,
    loading,
    error,
    refreshUsers,
    generateApiKey,
    renewApiKey,
    retireApiKey,
    deleteUser,
    updateUser,
    updateUserPassword,
    clearError
  }
}